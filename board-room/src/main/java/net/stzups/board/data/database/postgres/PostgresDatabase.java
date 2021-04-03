package net.stzups.board.data.database.postgres;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.board.data.database.Database;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.data.objects.canvas.Canvas;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PostgresDatabase implements Database {
    private Connection connection;

    private Map<Long, User> users = new HashMap<>();
    private Map<Long, Document> documents = new HashMap<>();

    public PostgresDatabase(String url, String user, String password) throws Exception {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(url, user, password);
    }

    @Override
    public void addUser(User user) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(id, owned_documents, shared_documents) VALUES (?, ?, ?)");
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setArray(2, connection.createArrayOf("bigint", user.getOwnedDocuments().toArray()));
            preparedStatement.setArray(3, connection.createArrayOf("bigint", user.getSharedDocuments().toArray()));
            preparedStatement.execute();
            users.put(user.getId(), user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User getUser(long id) {
        try {
            User user = users.get(id);
            if (user == null) {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id=?");
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    System.out.println("user does not exist");
                    return null;
                }
                user = new User(id, new ArrayList<>(Arrays.asList((Long[]) resultSet.getArray("owned_documents").getArray())), new ArrayList<>(Arrays.asList((Long[]) resultSet.getArray("shared_documents").getArray())));
                users.put(user.getId(), user);
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Document createDocument(User owner) {
        Document document = new Document(owner);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO documents(id, owner, name) VALUES (?, ?, ?)");
            preparedStatement.setLong(1, document.getId());
            preparedStatement.setLong(2, document.getOwner().getId());
            preparedStatement.setString(3, document.getName());
            preparedStatement.execute();
            documents.put(document.getId(), document);
            return document;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Document getDocument(long id) {
        try {
            Document document = documents.get(id);
            if (document == null) {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM documents WHERE id=?");
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    System.out.println("document does not exist");
                    return null;
                }
                User user = getUser(resultSet.getLong("owner"));
                if (user == null) {
                    System.out.println("no owner for document");
                    return null;
                }
                document = new Document(id, user, resultSet.getString("name"));
                documents.put(document.getId(), document);
            }
            return document;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Canvas getCanvas(Document document) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM canvases WHERE document=?");
            preparedStatement.setLong(1, document.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Blob blob = resultSet.getBlob("data");
                return new Canvas(document, Unpooled.wrappedBuffer(blob.getBytes(1, (int) blob.length())));
            } else {
                return new Canvas(document);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;//todo error handling??
    }

    @Override
    public void saveCanvas(Canvas canvas) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO canvases(document, data) VALUES(?, ?) ON CONFLICT (document) DO UPDATE SET data=?");
            preparedStatement.setLong(1, canvas.getDocument().getId());
            ByteBuf byteBuf = Unpooled.buffer();
            canvas.serialize(byteBuf);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuf.array());
            preparedStatement.setBinaryStream(2, byteArrayInputStream);
            preparedStatement.setBinaryStream(3, byteArrayInputStream);//todo is this duplicate bad?
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDocument(Document document) {

    }

    @Override
    public void addUserSession(PersistentUserSession persistentUserSession) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO persistent_user_sessions(id, \"user\", creation_time, hashed_token) VALUES (?, ?, ?, ?)");
            preparedStatement.setLong(1, persistentUserSession.getId());
            preparedStatement.setLong(2, persistentUserSession.getUser());
            preparedStatement.setLong(3, persistentUserSession.getCreationTime());
            preparedStatement.setBinaryStream(4, new ByteArrayInputStream(persistentUserSession.getHashedToken()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a user session for a token and return the removed user session
     */
    @Override
    public PersistentUserSession removeUserSession(long id) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM persistent_user_sessions WHERE id=?");
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }
            PersistentUserSession persistentUserSession = new PersistentUserSession(id, resultSet.getLong("user"), resultSet.getLong("creation_time"), resultSet.getBinaryStream("hashed_token").readAllBytes());
            preparedStatement = connection.prepareStatement("DELETE FROM persistent_user_sessions WHERE id=?");
            preparedStatement.setLong(1, id);
            preparedStatement.execute();
            return persistentUserSession;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

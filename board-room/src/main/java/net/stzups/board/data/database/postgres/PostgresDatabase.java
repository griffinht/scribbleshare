package net.stzups.board.data.database.postgres;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.database.Database;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.canvas.Canvas;
import org.postgresql.util.PGobject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Array;
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

    private Map<Long, Document> documents = new HashMap<>();

    public PostgresDatabase(String url, String user, String password) throws Exception {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(url, user, password);
    }

    @Override
    public User createUser() {
        User user = new User(BoardRoom.getRandom().nextLong(), new ArrayList<>(), new ArrayList<>());
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(id, owned_documents, shared_documents) VALUES (?, ?, ?)")) {
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setArray(2, connection.createArrayOf("document_id", user.getOwnedDocuments().toArray()));
            preparedStatement.setArray(3, connection.createArrayOf("document_id", user.getSharedDocuments().toArray()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public User getUser(long id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id=?")) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(id,
                        new ArrayList<>(Arrays.asList(convertPostgresDomainArrayToLongArray(resultSet.getArray("owned_documents")))),
                        new ArrayList<>(Arrays.asList(convertPostgresDomainArrayToLongArray(resultSet.getArray("shared_documents")))));
            } else {
                BoardRoom.getLogger().warning("User with id " + id + " does not exist");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // normally (Long[]) resultSet.getArray("something").getArray()) fails with custom domain types
    private Long[] convertPostgresDomainArrayToLongArray(Array longArray) throws SQLException {
        Object[] objects = (Object[]) longArray.getArray();
        Long[] longs = new Long[objects.length];
        for (int i = 0; i < objects.length; i++) {
            //objects is actually an array of PGobject
            longs[i] = Long.parseLong(((PGobject) objects[i]).getValue());
        }
        return longs;
    }

    @Override
    public Document createDocument(User owner) {
        Document document = new Document(owner);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO documents(id, owner, name) VALUES (?, ?, ?)")){
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
        Document document = documents.get(id);
        if (document == null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM documents WHERE id=?")) {
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    long userId = resultSet.getLong("owner");
                    User user = getUser(userId);
                    if (user == null) {
                        BoardRoom.getLogger().warning("Document with id " + id + " has no owner with id " + userId);
                        return null;
                    }
                    document = new Document(id, user, resultSet.getString("name"));
                    documents.put(document.getId(), document);
                } else {
                    BoardRoom.getLogger().warning("Document with id " + id + " does not exist");
                    return null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return document;
    }

    @Override
    public Canvas getCanvas(Document document) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM canvases WHERE document=?")) {
            preparedStatement.setLong(1, document.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Canvas(document, Unpooled.wrappedBuffer(resultSet.getBinaryStream("data").readAllBytes()));
            } else {
                return new Canvas(document);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return null;//todo error handling??
        }
    }

    @Override
    public void saveCanvas(Canvas canvas) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO canvases(document, data) VALUES(?, ?) ON CONFLICT (document) DO UPDATE SET data=excluded.data")) {
            preparedStatement.setLong(1, canvas.getDocument().getId());
            ByteBuf byteBuf = Unpooled.buffer();
            canvas.serialize(byteBuf);
            preparedStatement.setBinaryStream(2, new ByteArrayInputStream(byteBuf.array()));
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
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO persistent_user_sessions(id, \"user\", creation_time, hashed_token) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setLong(1, persistentUserSession.getId());
            preparedStatement.setLong(2, persistentUserSession.getUser());
            preparedStatement.setTimestamp(3, persistentUserSession.getCreation());
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
        PersistentUserSession persistentUserSession;

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM persistent_user_sessions WHERE id=?")) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                persistentUserSession = new PersistentUserSession(id, resultSet.getLong("user"), resultSet.getTimestamp("creation_time"), resultSet.getBinaryStream("hashed_token").readAllBytes());
            } else {
                BoardRoom.getLogger().warning("PersistentUserSession with id " + id + " does not exist");
                return null;
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return null;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM persistent_user_sessions WHERE id=?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.execute();
            return persistentUserSession;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

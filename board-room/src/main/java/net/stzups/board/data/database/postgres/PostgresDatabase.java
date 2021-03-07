package net.stzups.board.data.database.postgres;

import net.stzups.board.data.database.Database;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.UserSession;

import java.io.ByteArrayInputStream;
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
    private Map<Long, UserSession> userSessions = new HashMap<>();

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
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO documents VALUES (?, ?, ?, ?)");
            preparedStatement.setLong(1, document.getId());
            preparedStatement.setLong(2, document.getOwner().getId());
            preparedStatement.setString(3, document.getName());
            preparedStatement.setBinaryStream(4, new ByteArrayInputStream(new byte[0]));
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
                User user = getUser(resultSet.getLong("owner_id"));
                if (user == null) {
                    System.out.println("no owner for document");
                    return null;
                }
                document = new Document(id, user, resultSet.getString("name"));//todo binary data
                documents.put(document.getId(), document);
            }
            return document;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void saveDocument(Document document) {

    }

    @Override
    public void addUserSession(UserSession userSession) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO user_sessions VALUES (?, ?, ?, ?)");
            preparedStatement.setLong(1, userSession.getToken());
            preparedStatement.setLong(2, userSession.getUserId());
            preparedStatement.setLong(3, userSession.getCreationTime());
            preparedStatement.setLong(4, userSession.getHash());
            preparedStatement.execute();
            userSessions.put(userSession.getUserId(), userSession);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a user session for a token and return the removed user session
     */
    @Override
    public UserSession removeUserSession(long token) {
        try {
            UserSession userSession = userSessions.remove(token);
            if (userSession == null) {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM user_sessions WHERE token=?");
                preparedStatement.setLong(1, token);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    System.out.println("user session does not exist in db");
                    return null;
                }
                userSession = new UserSession(token, resultSet.getLong("user_id"), resultSet.getLong("creation_time"), resultSet.getLong("hash"));
            }
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM user_sessions WHERE token=?");
            preparedStatement.setLong(1, token);
            preparedStatement.execute();
            return userSession;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

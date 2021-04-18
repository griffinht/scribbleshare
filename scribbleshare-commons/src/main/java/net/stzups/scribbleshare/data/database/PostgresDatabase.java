package net.stzups.scribbleshare.data.database;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import org.postgresql.util.PSQLException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PostgresDatabase implements Database {
    private static final Random random = new Random();
    private Connection connection;

    private final Map<Long, Document> documents = new HashMap<>();

    public PostgresDatabase(String url, String user, String password, int maxRetries) throws Exception {
        Class.forName("org.postgresql.Driver");
        int retries = 0;
        while (connection == null) {
            try {
                connection = DriverManager.getConnection(url, user, password);
            } catch (PSQLException e) {
                if (e.getCause() instanceof ConnectException && (maxRetries < 0 || retries < maxRetries)) {
                    System.out.println("Retrying PostgreSQL database connection (" + ++retries + "/" + maxRetries + " retries)");
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public User createUser() {
        User user = new User(random.nextLong(), new Long[0], new Long[0]);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(id, owned_documents, shared_documents) VALUES (?, ?, ?)")) {
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setArray(2, connection.createArrayOf("bigint", user.getOwnedDocuments().toArray()));
            preparedStatement.setArray(3, connection.createArrayOf("bigint", user.getSharedDocuments().toArray()));
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
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(id,
                            (Long[]) (resultSet.getArray("owned_documents").getArray()),
                            (Long[]) (resultSet.getArray("shared_documents").getArray()));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateUser(User user) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET owned_documents=?, shared_documents=? WHERE id=?")){
            preparedStatement.setArray(1, connection.createArrayOf("bigint", user.getOwnedDocuments().toArray()));
            preparedStatement.setArray(2, connection.createArrayOf("bigint", user.getSharedDocuments().toArray()));
            preparedStatement.setLong(3, user.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Document createDocument(User owner) {
        Document document = new Document(owner);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO documents(id, owner, resources, name) VALUES (?, ?, ?, ?)")){
            preparedStatement.setLong(1, document.getId());
            preparedStatement.setLong(2, document.getOwner());
            preparedStatement.setArray(3, connection.createArrayOf("bigint", document.getResources().toArray()));
            preparedStatement.setString(4, document.getName());
            preparedStatement.execute();
            documents.put(document.getId(), document);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        owner.getOwnedDocuments().add(document.getId());
        updateUser(owner);
        return document;
    }

    @Override
    public Document getDocument(long id) {
        Document document = documents.get(id);
        if (document == null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM documents WHERE id=?")) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        document = new Document(id,
                                resultSet.getLong("owner"),
                                new ArrayList<>(Arrays.asList((Long[]) resultSet.getArray("resources").getArray())),
                                resultSet.getString("name"));
                        documents.put(document.getId(), document);
                    } else {
                        System.out.println("Document with id " + id + " does not exist");
                        return null;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return document;
    }

    @Override
    public void updateDocument(Document document) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE documents SET name=? WHERE id=?")) {
            preparedStatement.setString(1, document.getName());
            preparedStatement.setLong(2, document.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteDocument(Document document) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM documents WHERE id=?; DELETE FROM canvases WHERE document=?; DELETE FROM invite_codes WHERE document=?")) {
            preparedStatement.setLong(1, document.getId());
            preparedStatement.setLong(2, document.getId());
            preparedStatement.setLong(3, document.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public InviteCode getInviteCode(String code) {//gets a document for an existing invite code
        if (code.length() != InviteCode.INVITE_CODE_LENGTH) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT document FROM invite_codes WHERE code=?")) {
            preparedStatement.setString(1, code);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new InviteCode(code, resultSet.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public InviteCode getInviteCode(Document document) {//gets an invite code for a document
        //check if invite code already exists, otherwise generate a new one
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT code FROM invite_codes WHERE document=?")) {
            preparedStatement.setLong(1, document.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new InviteCode(resultSet.getString(1), document.getId());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        //invite code does not already exist, so a new one must be made
        InviteCode inviteCode = new InviteCode(document);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO invite_codes(code, document) VALUES (?, ?)")) {
            preparedStatement.setString(1, inviteCode.getCode());
            preparedStatement.setLong(2, inviteCode.getDocument());
            preparedStatement.execute();
            return inviteCode;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addPersistentHttpSession(PersistentHttpSession persistentHttpSession) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO persistent_user_sessions(id, \"user\", creation_time, hashed_token) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setLong(1, persistentHttpSession.getId());
            preparedStatement.setLong(2, persistentHttpSession.getUser());
            preparedStatement.setTimestamp(3, persistentHttpSession.getCreation());
            preparedStatement.setBinaryStream(4, new ByteArrayInputStream(persistentHttpSession.getHashedToken()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HttpSession getHttpSession(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addResource(long id, byte[] data) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO resources(id, data) VALUES(?, ?) ON CONFLICT (document) DO UPDATE SET data=excluded.data")) {
            preparedStatement.setLong(1, id);
            preparedStatement.setBinaryStream(2, new ByteArrayInputStream(data));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getResource(long id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM resources WHERE document=?")) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBinaryStream("data").readAllBytes();
                } else {
                    return null;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return null;//todo error handling??
        }
    }

    /**
     * Remove a user session for a token and return the removed user session
     */
    @Override
    public PersistentHttpSession getAndRemovePersistentHttpSession(long id) {//todo combine
        PersistentHttpSession persistentHttpSession;

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM persistent_user_sessions WHERE id=?")) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    persistentHttpSession = new PersistentHttpSession(id, resultSet.getLong("user"), resultSet.getTimestamp("creation_time"), resultSet.getBinaryStream("hashed_token").readAllBytes());
                } else {
                    System.out.println("PersistentUserSession with id " + id + " does not exist");
                    return null;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return null;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM persistent_user_sessions WHERE id=?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.execute();
            return persistentHttpSession;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

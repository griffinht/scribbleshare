package net.stzups.scribbleshare.data.database.implementations;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PostgresDatabase implements AutoCloseable, ScribbleshareDatabase {
    @Override
    public Login getLogin(String username) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM logins WHERE username=?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Login(username, resultSet.getLong("user_id"), resultSet.getBytes("hashed_password"));
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
    public void addLogin(Login login) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO logins(username, user_id, hashed_password) VALUES(?, ?, ?)")) {
            preparedStatement.setString(1, login.getUsername());
            preparedStatement.setLong(2, login.getId());
            preparedStatement.setBinaryStream(3, new ByteArrayInputStream(login.getHashedPassword()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();//todo error handling
        }
    }

    public interface Config {
        String getUrl();
        String getUser();
        String getPassword();
        int getMaxRetries();
    }

    private static final Random RANDOM = new Random();
    private Connection connection;

    private final Map<Long, Document> documents = new HashMap<>();

    public PostgresDatabase(PostgresDatabase.Config config) throws Exception {
        Class.forName("org.postgresql.Driver");
        int retries = 0;
        while (connection == null) {
            try {
                connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
            } catch (PSQLException e) {
                if (e.getCause() instanceof ConnectException && (config.getMaxRetries() < 0 || retries < config.getMaxRetries())) {
                    Scribbleshare.getLogger().info("Retrying PostgreSQL database connection (" + ++retries + "/" + config.getMaxRetries() + " retries)");
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();    
    }

    @Override
    public void addUser(User user) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(id, owned_documents, shared_documents, username) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setArray(2, connection.createArrayOf("bigint", user.getOwnedDocuments().toArray()));
            preparedStatement.setArray(3, connection.createArrayOf("bigint", user.getSharedDocuments().toArray()));
            preparedStatement.setString(4, user.getUsername());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User getUser(long id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id=?")) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(id,
                            (Long[]) (resultSet.getArray("owned_documents").getArray()),
                            (Long[]) (resultSet.getArray("shared_documents").getArray()),
                            resultSet.getString("username"));
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
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO documents(id, owner, name) VALUES (?, ?, ?)")){
            preparedStatement.setLong(1, document.getId());
            preparedStatement.setLong(2, document.getOwner());
            preparedStatement.setString(3, document.getName());
            preparedStatement.execute();
            documents.put(document.getId(), document);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        addResource(document.getId(), document.getId(), new Resource(Canvas.getEmptyCanvas()));
        owner.getOwnedDocuments().add(document.getId());
        updateUser(owner);//todo
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
    public void deleteDocument(Document document) {//todo
        documents.remove(document.getId());
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM documents WHERE id=?; DELETE FROM resources WHERE owner=?; DELETE FROM invite_codes WHERE document=?;")) {
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
    public void addPersistentHttpSession(PersistentHttpUserSession persistentHttpSession) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO persistent_user_sessions(id, \"user\", creation_time, hashed_token) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setLong(1, persistentHttpSession.getId());
            preparedStatement.setLong(2, persistentHttpSession.getUser());
            preparedStatement.setTimestamp(3, persistentHttpSession.getCreated());
            preparedStatement.setBinaryStream(4, new ByteArrayInputStream(persistentHttpSession.getHashedToken()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();//todo error handling
        }
    }

    @Override
    public HttpUserSession getHttpSession(long id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM key_value WHERE key=?")) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new HttpUserSession(id,
                            resultSet.getTimestamp("created"),
                            resultSet.getTimestamp("expires"),
                            resultSet.getLong("user_id"),
                            Unpooled.wrappedBuffer(resultSet.getBinaryStream("data").readAllBytes()));
                } else {
                    return null;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addHttpSession(HttpUserSession httpSession) {
        ByteBuf byteBuf = Unpooled.buffer();
        httpSession.serialize(byteBuf);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO key_value(key, value) VALUES (?, ?)")) {
            preparedStatement.setLong(1, httpSession.getId());
            preparedStatement.setBinaryStream(2, new ByteBufInputStream(byteBuf));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeHttpSession(long id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM key_value WHERE key=?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long addResource(long owner, Resource resource) {
        long id = RANDOM.nextLong();
        addResource(id, owner, resource);
        return id;
    }

    private void addResource(long id, long owner, Resource resource) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO resources(id, owner, last_modified, data) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, owner);
            preparedStatement.setTimestamp(3, resource.getLastModified());
            preparedStatement.setBinaryStream(4, new ByteBufInputStream(resource.getData()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateResource(long id, long owner, Resource data) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE resources SET last_modified=?, data=? WHERE id=? AND owner=?")) {
            preparedStatement.setTimestamp(1, Timestamp.from(Instant.now()));
            preparedStatement.setBinaryStream(2, new ByteBufInputStream(data.getData()));
            preparedStatement.setLong(3, id);
            preparedStatement.setLong(4, owner);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resource getResource(long id, long owner) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM resources WHERE id=? AND owner=?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, owner);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Resource(resultSet.getTimestamp("last_modified"), Unpooled.wrappedBuffer(resultSet.getBinaryStream("data").readAllBytes()));
                } else {
                    return null;
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);//todo error handling??
        }
    }

    /**
     * Remove a user session for a token and return the removed user session
     */
    @Override
    public PersistentHttpUserSession getAndRemovePersistentHttpSession(long id) {//todo combine
        PersistentHttpUserSession persistentHttpSession;

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM persistent_user_sessions WHERE id=?")) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    persistentHttpSession = new PersistentHttpUserSession(id,
                            resultSet.getTimestamp("created"),
                            resultSet.getTimestamp("expires"),
                            resultSet.getLong("user_id"),
                            Unpooled.wrappedBuffer(resultSet.getBinaryStream("data").readAllBytes()));
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

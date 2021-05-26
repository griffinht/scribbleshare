package net.stzups.scribbleshare.data.database.implementations;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.exception.ConnectionException;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.login.Login;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import org.jetbrains.annotations.Nullable;

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
import java.util.logging.Level;

public class PostgresDatabase implements AutoCloseable, ScribbleshareDatabase {
    @Override
    public Login getLogin(String username) throws DatabaseException {
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
            Scribbleshare.getLogger().log(Level.WARNING, "Exception while getting login for username " + username, e);
            return null;
        }
    }

    @Override
    public boolean addLogin(Login login) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO logins(username, user_id, hashed_password) VALUES(?, ?, ?)")) {
            preparedStatement.setString(1, login.getUsername());
            preparedStatement.setLong(2, login.getId());
            preparedStatement.setBinaryStream(3, new ByteArrayInputStream(login.getHashedPassword()));
            preparedStatement.execute();
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // PostgreSQL error code unique_violation
                return false;
            } else {
                throw new DatabaseException(e);
            }
        }

        return true;
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

    public PostgresDatabase(PostgresDatabase.Config config) throws ConnectionException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Exception while finding PostgreSQL JDBC driver", e);
        }

        int retries = 0;
        while (connection == null) {
            try {
                connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
            } catch (SQLException e) {
                if (e.getCause() instanceof ConnectException && (config.getMaxRetries() < 0 || retries < config.getMaxRetries())) {
                    Scribbleshare.getLogger().info("Retrying PostgreSQL database connection (" + ++retries + "/" + config.getMaxRetries() + " retries)");
                } else {
                    throw new ConnectionException(e);
                }
            }
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    @Override
    public void addUser(User user) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(id, owned_documents, shared_documents, username) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setArray(2, connection.createArrayOf("bigint", user.getOwnedDocuments().toArray()));
            preparedStatement.setArray(3, connection.createArrayOf("bigint", user.getSharedDocuments().toArray()));
            preparedStatement.setString(4, user.getUsername());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Exception while getting " + user, e);
        }
    }

    @Override
    public @Nullable User getUser(long id) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id=?")) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new User(id,
                        (Long[]) (resultSet.getArray("owned_documents").getArray()),
                        (Long[]) (resultSet.getArray("shared_documents").getArray()),
                        resultSet.getString("username"));
            }
        } catch (SQLException e) {
            Scribbleshare.getLogger().log(Level.WARNING, "Exception while getting " + User.class.getSimpleName() + " with id " + id, e);
            return null;
        }
    }

    @Override
    public void updateUser(User user) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET owned_documents=?, shared_documents=? WHERE id=?")) {
            preparedStatement.setArray(1, connection.createArrayOf("bigint", user.getOwnedDocuments().toArray()));
            preparedStatement.setArray(2, connection.createArrayOf("bigint", user.getSharedDocuments().toArray()));
            preparedStatement.setLong(3, user.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Exception while updating user " + user, e);
        }
    }

    @Override
    public Document createDocument(User owner) throws DatabaseException {
        Document document = new Document(owner);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO documents(id, owner, name) VALUES (?, ?, ?)")) {
            preparedStatement.setLong(1, document.getId());
            preparedStatement.setLong(2, document.getOwner());
            preparedStatement.setString(3, document.getName());
            preparedStatement.execute();
            documents.put(document.getId(), document);
        } catch (SQLException e) {
            throw new DatabaseException("Exception while creating new " + Document.class.getSimpleName() + " with owner " + owner, e);
        }
        addResource(document.getId(), document.getId(), new Resource(Canvas.getEmptyCanvas()));
        owner.getOwnedDocuments().add(document.getId());
        updateUser(owner);//todo
        return document;
    }

    @Override
    public @Nullable Document getDocument(long id) throws DatabaseException {
        Document document = documents.get(id);
        if (document == null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM documents WHERE id=?")) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    document = new Document(id,
                            resultSet.getLong("owner"),
                            resultSet.getString("name"));
                    documents.put(document.getId(), document);
                }
            } catch (SQLException e) {
                throw new DatabaseException("Exception while getting " + Document.class.getSimpleName() + " with id " + id, e);
            }
        }
        return document;
    }

    @Override
    public void updateDocument(Document document) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE documents SET name=? WHERE id=?")) {
            preparedStatement.setString(1, document.getName());
            preparedStatement.setLong(2, document.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Exception while updating " + document, e);
        }
    }

    @Override
    public void deleteDocument(Document document) throws DatabaseException {//todo
        documents.remove(document.getId());
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM documents WHERE id=?; DELETE FROM resources WHERE owner=?; DELETE FROM invite_codes WHERE document=?;")) {
            preparedStatement.setLong(1, document.getId());
            preparedStatement.setLong(2, document.getId());
            preparedStatement.setLong(3, document.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Exception while deleting " + document, e);
        }
    }

    @Override
    public InviteCode getInviteCode(String code) throws DatabaseException {//gets a document for an existing invite code
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
            throw new DatabaseException("Exception while getting " + InviteCode.class.getSimpleName() + " with code " + code, e);
        }
    }

    @Override
    public @Nullable InviteCode getInviteCode(Document document) throws DatabaseException {//gets an invite code for a document
        //check if invite code already exists, otherwise generate a new one
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT code FROM invite_codes WHERE document=?")) {
            preparedStatement.setLong(1, document.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new InviteCode(resultSet.getString(1), document.getId());
                }
            }
        } catch (SQLException e) {
            Scribbleshare.getLogger().log(Level.WARNING, "Exception while getting " + InviteCode.class.getSimpleName() + " code for " + document, e);
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
            throw new DatabaseException("Exception while inserting new " + inviteCode + " for " + document, e);
        }
    }

    @Override
    public void addPersistentHttpUserSession(PersistentHttpUserSession persistentHttpSession) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO persistent_user_sessions(id, created, expired, user_id, data) VALUES (?, ?, ?, ?, ?)")) {
            preparedStatement.setLong(1, persistentHttpSession.getId());
            preparedStatement.setTimestamp(2, persistentHttpSession.getCreated());
            preparedStatement.setTimestamp(3, persistentHttpSession.getExpired());
            preparedStatement.setLong(4, persistentHttpSession.getUser());

            ByteBuf byteBuf = Unpooled.buffer();
            persistentHttpSession.serialize(byteBuf);
            preparedStatement.setBinaryStream(5, new ByteBufInputStream(byteBuf));
            byteBuf.release();

            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public @Nullable HttpUserSession getHttpSession(HttpSessionCookie cookie) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM user_sessions WHERE id=?")) {
            preparedStatement.setLong(1, cookie.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new HttpUserSession(
                        resultSet.getLong("id"),
                        resultSet.getTimestamp("created"),
                        resultSet.getTimestamp("expired"),
                        resultSet.getLong("user_id"),
                        Unpooled.wrappedBuffer(resultSet.getBinaryStream("data").readAllBytes()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception while getting " + HttpUserSession.class.getSimpleName() + " for " + cookie, e);
        } catch (SQLException e) {
            throw new DatabaseException("Exception while getting " + HttpUserSession.class.getSimpleName() + " for " + cookie, e);
        }
    }

    @Override
    public void addHttpSession(HttpUserSession httpUserSession) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO user_sessions(id, created, expired, user_id, data) VALUES (?, ?, ?, ?, ?)")) {
            preparedStatement.setLong(1, httpUserSession.getId());
            preparedStatement.setTimestamp(2, httpUserSession.getCreated());
            preparedStatement.setTimestamp(3, httpUserSession.getExpired());
            preparedStatement.setLong(4, httpUserSession.getUser());

            ByteBuf byteBuf = Unpooled.buffer();
            httpUserSession.serialize(byteBuf);
            preparedStatement.setBinaryStream(5, new ByteBufInputStream(byteBuf));
            byteBuf.release();

            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Exception while adding " + httpUserSession, e);
        }
    }

    @Override
    public void expireHttpSession(HttpUserSession httpUserSession) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE user_sessions SET expired=? WHERE id=?")) {
            preparedStatement.setTimestamp(1, Timestamp.from(Instant.now()));
            preparedStatement.setLong(2, httpUserSession.getUser());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long addResource(long owner, Resource resource) throws DatabaseException {
        long id = RANDOM.nextLong();
        addResource(id, owner, resource);
        return id;
    }

    private void addResource(long id, long owner, Resource resource) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO resources(id, owner, last_modified, data) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, owner);
            preparedStatement.setTimestamp(3, resource.getLastModified());
            preparedStatement.setBinaryStream(4, new ByteBufInputStream(resource.getData()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Exception while adding " + Resource.class.getSimpleName() + " with id " + id, e);
        }
    }

    @Override
    public void updateResource(long id, long owner, Resource data) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE resources SET last_modified=?, data=? WHERE id=? AND owner=?")) {
            preparedStatement.setTimestamp(1, Timestamp.from(Instant.now()));
            preparedStatement.setBinaryStream(2, new ByteBufInputStream(data.getData()));
            preparedStatement.setLong(3, id);
            preparedStatement.setLong(4, owner);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Exception while updating " + Resource.class.getSimpleName() + " with id " + id, e);
        }
    }

    @Override
    public @Nullable Resource getResource(long id, long owner) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM resources WHERE id=? AND owner=?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, owner);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new Resource(resultSet.getTimestamp("last_modified"), Unpooled.wrappedBuffer(resultSet.getBinaryStream("data").readAllBytes()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception while getting " + Resource.class.getSimpleName() + " with id " + id, e);
        } catch (SQLException e) {
            throw new DatabaseException("Exception while getting " + Resource.class.getSimpleName() + " with id " + id, e);
        }
    }

    @Override
    public @Nullable PersistentHttpUserSession getPersistentHttpUserSession(HttpSessionCookie cookie) throws DatabaseException {//todo combine
        PersistentHttpUserSession persistentHttpSession;

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM persistent_user_sessions WHERE id=?")) {
            preparedStatement.setLong(1, cookie.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                persistentHttpSession = new PersistentHttpUserSession(
                        resultSet.getLong("id"),
                        resultSet.getTimestamp("created"),
                        resultSet.getTimestamp("expired"),
                        resultSet.getLong("user_id"),
                        Unpooled.wrappedBuffer(resultSet.getBinaryStream("data").readAllBytes()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception while getting " + Resource.class.getSimpleName() + " for " + cookie, e);
        } catch (SQLException e) {
            throw new DatabaseException("Exception while getting " + PersistentHttpUserSession.class.getSimpleName() + " for " + cookie);
        }

        return persistentHttpSession;
    }

    @Override
    public void expirePersistentHttpUserSession(PersistentHttpUserSession session) throws DatabaseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE persistent_user_sessions SET expired=? WHERE id=?; ")) {
            preparedStatement.setTimestamp(1, Timestamp.from(Instant.now()));
            preparedStatement.setLong(2, session.getId());
        } catch (SQLException e) {
            throw new DatabaseException("Exception while expiring " + session, e);
        }
    }
}

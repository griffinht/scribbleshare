package net.stzups.board.data.database.postgres;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.board.BoardRoom;
import net.stzups.board.data.database.Database;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.InviteCode;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.canvas.Canvas;
import org.postgresql.util.PSQLException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PostgresDatabase implements Database {
    private Connection connection;

    private Map<Long, Document> documents = new HashMap<>();

    public PostgresDatabase(String url, String user, String password, int maxRetries) throws Exception {
        Class.forName("org.postgresql.Driver");
        int retries = 0;
        while (connection == null) {
            try {
                connection = DriverManager.getConnection(url, user, password);
            } catch (PSQLException e) {
                if (e.getCause() instanceof ConnectException && (maxRetries < 0 || retries < maxRetries)) {
                    BoardRoom.getLogger().info("Retrying PostgreSQL database connection (" + ++retries + "/" + maxRetries + " retries)");
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public User createUser() {
        User user = new User(BoardRoom.getRandom().nextLong(), new Long[0], new Long[0]);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(id, owned_documents, shared_documents) VALUES (?, ?, ?)")) {
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setArray(2, connection.createArrayOf("bigint", user.getOwnedDocuments()));
            preparedStatement.setArray(3, connection.createArrayOf("bigint", user.getSharedDocuments()));
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
                    BoardRoom.getLogger().warning("User with id " + id + " does not exist");
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
            preparedStatement.setArray(1, connection.createArrayOf("bigint", user.getOwnedDocuments()));
            preparedStatement.setArray(2, connection.createArrayOf("bigint", user.getSharedDocuments()));
            preparedStatement.setLong(3, user.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
/*
    // normally (Long[]) resultSet.getArray("something").getArray()) fails with custom domain types
    private Long[] convertPostgresDomainArrayToLongArray(Array longArray) throws SQLException {
        Object[] objects = (Object[]) longArray.getArray();
        Long[] longs = new Long[objects.length];
        for (int i = 0; i < objects.length; i++) {
            //objects is actually an array of PGobject
            longs[i] = Long.parseLong(((PGobject) objects[i]).getValue());
        }
        return longs;
    }*/

    @Override
    public Document createDocument(User owner) {
        Document document = new Document(owner);
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO documents(id, owner, name) VALUES (?, ?, ?)")){
            preparedStatement.setLong(1, document.getId());
            preparedStatement.setLong(2, document.getOwner().getId());
            preparedStatement.setString(3, document.getName());
            preparedStatement.execute();
            documents.put(document.getId(), document);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        owner.addOwnedDocument(document);
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
    public Canvas getCanvas(Document document) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM canvases WHERE document=?")) {
            preparedStatement.setLong(1, document.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Canvas(document, Unpooled.wrappedBuffer(resultSet.getBinaryStream("data").readAllBytes()));
                } else {
                    return new Canvas(document);
                }
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
    public PersistentUserSession removeUserSession(long id) {//todo combine
        PersistentUserSession persistentUserSession;

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM persistent_user_sessions WHERE id=?")) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    persistentUserSession = new PersistentUserSession(id, resultSet.getLong("user"), resultSet.getTimestamp("creation_time"), resultSet.getBinaryStream("hashed_token").readAllBytes());
                } else {
                    BoardRoom.getLogger().warning("PersistentUserSession with id " + id + " does not exist");
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
            return persistentUserSession;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

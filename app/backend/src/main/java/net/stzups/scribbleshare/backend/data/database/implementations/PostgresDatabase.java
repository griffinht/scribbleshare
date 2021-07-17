package net.stzups.scribbleshare.backend.data.database.implementations;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.backend.data.PersistentHttpUserSession;
import net.stzups.scribbleshare.backend.data.PersistentHttpUserSessionCookie;
import net.stzups.scribbleshare.backend.data.database.ScribbleshareBackendDatabase;
import net.stzups.scribbleshare.data.database.exception.ConnectionException;
import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class PostgresDatabase extends net.stzups.scribbleshare.data.database.implementations.PostgresDatabase implements ScribbleshareBackendDatabase {
    public PostgresDatabase(Config config) throws ConnectionException {
        super(config);
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
    public @Nullable PersistentHttpUserSession getPersistentHttpUserSession(PersistentHttpUserSessionCookie cookie) throws DatabaseException {//todo combine
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

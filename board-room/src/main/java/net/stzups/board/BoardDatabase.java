package net.stzups.board;

import net.stzups.board.data.database.Database;
import net.stzups.board.data.database.PostgresDatabase;
import net.stzups.board.data.database.RedisDatabase;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.InviteCode;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.canvas.Canvas;

public class BoardDatabase implements Database {
    private Database postgres;
    private Database redis;

    BoardDatabase() throws Exception {
        BoardRoom.getLogger().info("Connecting to Postgres database...");
        postgres = new PostgresDatabase(BoardRoom.getConfig().getString(BoardConfigKeys.POSTGRES_URL),
                BoardRoom.getConfig().getString(BoardConfigKeys.POSTGRES_USER),
                BoardRoom.getConfig().getString(BoardConfigKeys.POSTGRES_PASSWORD),
                BoardRoom.getConfig().getInteger(BoardConfigKeys.POSTGRES_RETRIES));
        BoardRoom.getLogger().info("Connected to Postgres database");

        BoardRoom.getLogger().info("Connecting to Redis database...");
        redis = new RedisDatabase(BoardRoom.getConfig().getString(BoardConfigKeys.REDIS_URL));
        BoardRoom.getLogger().info("Connected to Redis database");
    }
    @Override
    public User createUser() {
        return postgres.createUser();
    }

    @Override
    public User getUser(long id) {
        return postgres.getUser(id);
    }

    @Override
    public void updateUser(User user) {
        postgres.updateUser(user);
    }

    @Override
    public Document createDocument(User owner) {
        return postgres.createDocument(owner);
    }

    @Override
    public Document getDocument(long id) {
        return postgres.getDocument(id);
    }

    @Override
    public void updateDocument(Document document) {
        postgres.updateDocument(document);
    }

    @Override
    public void deleteDocument(Document document) {
        postgres.deleteDocument(document);
    }

    @Override
    public Canvas getCanvas(Document document) {
        return postgres.getCanvas(document);
    }

    @Override
    public void saveCanvas(Canvas canvas) {
        postgres.saveCanvas(canvas);
    }

    @Override
    public InviteCode getInviteCode(String code) {
        return postgres.getInviteCode(code);
    }

    @Override
    public InviteCode getInviteCode(Document document) {
        return postgres.getInviteCode(document);
    }

    @Override
    public PersistentUserSession removeUserSession(long id) {
        return postgres.removeUserSession(id);
    }

    @Override
    public void addUserSession(PersistentUserSession persistentUserSession) {
        postgres.addUserSession(persistentUserSession);
    }
}

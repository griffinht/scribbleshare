package net.stzups.board.data.database;

import net.stzups.board.BoardConfigKeys;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.InviteCode;
import net.stzups.board.data.objects.session.HttpSession;
import net.stzups.board.data.objects.session.PersistentHttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.canvas.Canvas;
import net.stzups.board.util.config.Config;

import java.util.logging.Logger;

public class BoardDatabase implements Database {
    private final Database postgres;
    private final Database keyDB;

    public BoardDatabase(Logger logger, Config config) throws Exception {
        logger.info("Connecting to Postgres database...");
        postgres = new PostgresDatabase(config.getString(BoardConfigKeys.POSTGRES_URL),
                config.getString(BoardConfigKeys.POSTGRES_USER),
                config.getString(BoardConfigKeys.POSTGRES_PASSWORD),
                config.getInteger(BoardConfigKeys.POSTGRES_RETRIES));

        logger.info("Connected to Postgres database");

        logger.info("Connecting to Redis database...");
        keyDB = new KeyDBDatabase(config.getString(BoardConfigKeys.REDIS_URL),
                config.getInteger(BoardConfigKeys.REDIS_PORT));
        logger.info("Connected to Redis database");
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
    public PersistentHttpSession getAndRemovePersistentHttpSession(long id) {
        return postgres.getAndRemovePersistentHttpSession(id);
    }

    @Override
    public void addPersistentHttpSession(PersistentHttpSession persistentHttpSession) {
        postgres.addPersistentHttpSession(persistentHttpSession);
    }

    @Override
    public HttpSession getHttpSession(long id) {
        return null;
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {

    }
}

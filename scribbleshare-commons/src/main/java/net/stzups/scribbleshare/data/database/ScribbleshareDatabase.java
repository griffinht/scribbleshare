package net.stzups.scribbleshare.data.database;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.util.config.Config;

import java.util.logging.Logger;

public class ScribbleshareDatabase implements Database {
    private final PostgresDatabase postgres;
    private final RedisDatabase keyDB;

    public ScribbleshareDatabase(Logger logger, Config config) throws Exception {
        logger.info("Connecting to Postgres database...");
        postgres = new PostgresDatabase(config.getString(ScribbleshareConfigKeys.POSTGRES_URL),
                config.getString(ScribbleshareConfigKeys.POSTGRES_USER),
                config.getString(ScribbleshareConfigKeys.POSTGRES_PASSWORD),
                config.getInteger(ScribbleshareConfigKeys.POSTGRES_RETRIES));

        logger.info("Connected to Postgres database");

        logger.info("Connecting to Redis database...");
        keyDB = new RedisDatabase(config.getString(ScribbleshareConfigKeys.REDIS_URL));
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
        return keyDB.getHttpSession(id);
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {
        keyDB.addHttpSession(httpSession);
    }

    @Override
    public long addResource(ByteBuf resource) {
        return postgres.addResource(resource);
    }

    @Override
    public boolean updateResource(long id, ByteBuf resource) {
        return postgres.updateResource(id, resource);
    }

    @Override
    public ByteBuf getResource(long id) {
        return postgres.getResource(id);
    }
}

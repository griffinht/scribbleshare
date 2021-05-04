package net.stzups.scribbleshare.data.database;

import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.util.config.Config;

import java.util.logging.Logger;

public class ScribbleshareDatabase implements Database {
    private final PostgresDatabase postgres;
    //private final RedisDatabase redis;

    public ScribbleshareDatabase() throws Exception {
        Scribbleshare.getLogger().info("Connecting to Postgres database...");
        postgres = new PostgresDatabase(Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.POSTGRES_URL),
                Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.POSTGRES_USER),
                Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.POSTGRES_PASSWORD),
                Scribbleshare.getConfig().getInteger(ScribbleshareConfigKeys.POSTGRES_RETRIES));

        Scribbleshare.getLogger().info("Connected to Postgres database");
/*
        logger.info("Connecting to Redis database...");
        redis = new RedisDatabase(config.getString(ScribbleshareConfigKeys.REDIS_URL));
        logger.info("Connected to Redis database");*/
    }

    @Override
    public void close() throws Exception {
        postgres.close();
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
/*
    @Override
    public HttpSession getHttpSession(long id) {
        return redis.getHttpSession(id);
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {
        redis.addHttpSession(httpSession);
    }*/


    @Override
    public HttpSession getHttpSession(long id) {
        return postgres.getHttpSession(id);
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {
        postgres.addHttpSession(httpSession);
    }

    @Override
    public long addResource(long owner, Resource resource) {
        return postgres.addResource(owner, resource);
    }

    @Override
    public void updateResource(long id, long owner, Resource resource) {
        postgres.updateResource(id, owner, resource);
    }

    @Override
    public Resource getResource(long id, long owner) {
        return postgres.getResource(id, owner);
    }
}

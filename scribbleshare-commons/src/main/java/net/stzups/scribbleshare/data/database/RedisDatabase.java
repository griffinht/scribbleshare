package net.stzups.scribbleshare.data.database;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.Resource;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import net.stzups.scribbleshare.data.objects.session.PersistentHttpSession;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import redis.clients.jedis.Jedis;

public class RedisDatabase implements Database {
    private final Jedis jedis;

    public RedisDatabase(String host) {
        jedis = new Jedis(host);
    }

    @Override
    public User createUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getUser(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateUser(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document createDocument(User owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getDocument(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDocument(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteDocument(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InviteCode getInviteCode(String code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InviteCode getInviteCode(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PersistentHttpSession getAndRemovePersistentHttpSession(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPersistentHttpSession(PersistentHttpSession persistentHttpSession) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getHttpSession(long id) {
        byte[] b = jedis.get(Unpooled.copyLong(id).array());
        if (b == null) {
            return null;
        }
        return new HttpSession(id, Unpooled.wrappedBuffer(b));
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {
        ByteBuf byteBuf = Unpooled.buffer(56, 56);
        httpSession.serialize(byteBuf);
        jedis.set(Unpooled.copyLong(httpSession.getId()).array(), byteBuf.array());
    }

    @Override
    public long addResource(long owner, Resource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateResource(long id, long owner, Resource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource getResource(long id, long owner) {
        throw new UnsupportedOperationException();
    }
/*
    @Override
    public void addResource(long id, byte[] resource) {
        jedis.set(Unpooled.copyLong(id).array(), resource);
    }

    @Override
    public byte[] getResource(long id) {
        return jedis.get(Unpooled.copyLong(id).array());
    }*/
}

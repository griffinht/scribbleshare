package net.stzups.board.data.database;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.InviteCode;
import net.stzups.board.data.objects.session.HttpSession;
import net.stzups.board.data.objects.session.PersistentHttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.canvas.Canvas;
import redis.clients.jedis.Jedis;

public class RedisDatabase implements Database {
    private Jedis jedis;

    public RedisDatabase(String host, int port) {
        jedis = new Jedis(host, port);
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
    public Canvas getCanvas(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveCanvas(Canvas canvas) {
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
        return new HttpSession(id, Unpooled.wrappedBuffer(jedis.get(Unpooled.copyLong(id).array())));
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {
        ByteBuf byteBuf = Unpooled.buffer();
        httpSession.serialize(byteBuf);
        jedis.set(Unpooled.copyLong(httpSession.getId()).array(), byteBuf.array());
    }
}

package net.stzups.scribbleshare.data.database.implementations;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.data.database.databases.SessionDatabase;
import net.stzups.scribbleshare.data.objects.session.HttpSession;
import redis.clients.jedis.Jedis;

public class RedisDatabase implements AutoCloseable, SessionDatabase {
    private final Jedis jedis;

    public RedisDatabase(String host) {
        jedis = new Jedis(host);
    }

    @Override
    public void close() {
        jedis.close();
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
        ByteBuf byteBuf = Unpooled.buffer();
        httpSession.serialize(byteBuf);
        jedis.set(Unpooled.copyLong(httpSession.getId()).array(), byteBuf.array());
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

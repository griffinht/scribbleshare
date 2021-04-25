package net.stzups.scribbleshare.data.objects;

import io.netty.buffer.ByteBuf;

import java.sql.Timestamp;
import java.time.Instant;


public class Resource {
    private final Timestamp lastModified; //negative value indicates immutable, 0 indicates no cache
    private final ByteBuf data;

    public Resource(ByteBuf data) {
        this.lastModified = Timestamp.from(Instant.now());
        this.data = data;
    }

    public Resource(Timestamp lastModified, ByteBuf data) {
        this.lastModified = lastModified;
        this.data = data;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public ByteBuf getData() {
        return data;
    }
}

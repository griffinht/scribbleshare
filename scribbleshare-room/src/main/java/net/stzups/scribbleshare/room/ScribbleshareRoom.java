package net.stzups.scribbleshare.room;

import io.netty.channel.ChannelFuture;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.room.server.ServerInitializer;

public class ScribbleshareRoom extends Scribbleshare implements AutoCloseable {
    private final ScribbleshareDatabase database;

    private ScribbleshareRoom(String[] args) throws Exception {
        super(args);
        database = new ScribbleshareDatabase();
    }

    public static void main(String[] args) throws Exception {
        Scribbleshare.getLogger().info("Starting scribbleshare-room server...");
        long start = System.currentTimeMillis();
        try (ScribbleshareRoom scribbleshareRoom = new ScribbleshareRoom()) {

            ChannelFuture closeFuture = scribbleshareRoom.start(new ServerInitializer(Scribbleshare.getConfig()));

            Scribbleshare.getLogger().info("Started scribbleshare-room server in " + (System.currentTimeMillis() - start) + "ms");

            closeFuture.sync();
        }
    }

    public ScribbleshareDatabase getDatabase() {
        return database;
    }

    @Override
    public void close() throws Exception {
        super.close();
        database.close();
    }
}

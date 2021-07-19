package net.stzups.scribbleshare.backend;

import io.netty.channel.ChannelFuture;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.backend.data.database.ScribbleshareBackendDatabase;
import net.stzups.scribbleshare.backend.data.database.implementations.PostgresDatabase;
import net.stzups.scribbleshare.backend.server.BackendHttpServerInitializer;

public class ScribbleshareBackend extends Scribbleshare implements AutoCloseable {
    private final ScribbleshareBackendConfig config;
    private final PostgresDatabase database;

    private ScribbleshareBackend(String[] args) throws Exception {
        this(args, new ScribbleshareBackendConfigImplementation());
    }

    private ScribbleshareBackend(String[] args, ScribbleshareBackendConfigImplementation config) throws Exception {
        super(args, config);
        this.config = config;
        this.database = new PostgresDatabase(config);
    }

    public static void main(String[] args) throws Exception {
        Scribbleshare.getLogger().info("Starting scribbleshare-backend server...");
        long start = System.currentTimeMillis();
        try (ScribbleshareBackend scribbleshareBackend = new ScribbleshareBackend(args)) {

            ChannelFuture closeFuture = scribbleshareBackend.start(new BackendHttpServerInitializer(scribbleshareBackend.getConfig(), scribbleshareBackend.getDatabase()));

            Scribbleshare.getLogger().info("Started scribbleshare-backend server in " + (System.currentTimeMillis() - start) + "ms");

            closeFuture.sync();
        }
    }

    public ScribbleshareBackendDatabase getDatabase() {
        return database;
    }

    @Override
    public ScribbleshareBackendConfig getConfig() {
        return config;
    }

    @Override
    public void close() throws Exception {
        super.close();
        database.close();
    }
}

package net.stzups.scribbleshare.room;

import io.netty.channel.ChannelFuture;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.ScribbleshareConfigKeys;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.Database;
import net.stzups.scribbleshare.room.server.ServerInitializer;
import net.stzups.scribbleshare.server.Server;
import net.stzups.scribbleshare.util.config.configs.ArgumentConfig;
import net.stzups.scribbleshare.util.config.configs.EnvironmentVariableConfig;
import net.stzups.scribbleshare.util.config.configs.PropertiesConfig;

public class ScribbleshareRoom {
    private static Database database;

    public static void main(String[] args) throws Exception {
        Scribbleshare.setLogger("scribbleshare-room");

        Scribbleshare.getLogger().info("Starting scribbleshare-room server...");

        long start = System.currentTimeMillis();

        Scribbleshare.getConfig()
                .addConfigProvider(new ArgumentConfig(args))
                .addConfigProvider(new EnvironmentVariableConfig(Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.ENVIRONMENT_VARIABLE_PREFIX)))
                .addConfigProvider(new PropertiesConfig(Scribbleshare.getConfig().getString(ScribbleshareConfigKeys.PROPERTIES)));

        database = new ScribbleshareDatabase();

        Server server = new Server();
        ChannelFuture closeFuture = server.start(new ServerInitializer());

        Scribbleshare.getLogger().info("Started scribbleshare-room server in " + (System.currentTimeMillis() - start) + "ms");

        closeFuture.sync();

        start = System.currentTimeMillis();

        Scribbleshare.getLogger().info("Stopping scribbleshare-room server");

        server.stop();//todo not necessary?

        Scribbleshare.getLogger().info("Stopped scribbleshare-room server in " + (System.currentTimeMillis() - start) + "ms");
    }

    public static Database getDatabase() {
        return database;
    }
}

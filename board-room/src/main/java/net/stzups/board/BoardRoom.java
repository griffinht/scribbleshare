package net.stzups.board;

import io.netty.channel.ChannelFuture;
import net.stzups.board.config.Config;
import net.stzups.board.config.ConfigBuilder;
import net.stzups.board.config.configs.ArgumentConfig;
import net.stzups.board.config.configs.EnvironmentVariableConfig;
import net.stzups.board.config.configs.PropertiesConfig;
import net.stzups.board.data.database.Database;
import net.stzups.board.data.database.memory.MemoryDatabase;
import net.stzups.board.data.database.postgres.PostgresDatabase;
import net.stzups.board.server.Server;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Logger;

public class BoardRoom {
    private static Logger logger;
    private static Config config;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Random random = new Random();
    private static MessageDigest SHA256MessageDigest;

    private static Database database;//user id -> user

    public static void main(String[] args) throws Exception {
        logger = LogFactory.getLogger("Board Room");

        logger.info("Starting Board Room server...");

        long start = System.currentTimeMillis();

        SHA256MessageDigest = MessageDigest.getInstance("SHA-256");

        config = new ConfigBuilder()
                .addConfig(new ArgumentConfig(args))
                .addConfig(new PropertiesConfig("board.properties"))
                .addConfig(new EnvironmentVariableConfig("board."))
                .build();

        Boolean postgres = config.getBoolean("postgres");
        if (postgres == null) {
            throw new RuntimeException("Failed to specify required runtime variable --postgres");
        } else {
            if (postgres) {
                logger.info("Connecting to Postgres database...");
                database = new PostgresDatabase(BoardRoom.getConfig().get("postgres.url"), BoardRoom.getConfig().get("postgres.user"), BoardRoom.getConfig().get("postgres.password"));
                logger.info("Connected to Postgres database");
            } else {
                logger.warning("Using debug only runtime database. No data will be persisted.");
                database = new MemoryDatabase();
            }
        }

        Server server = new Server();
        ChannelFuture channelFuture = server.start();

        logger.info("Started Board Room server in " + (System.currentTimeMillis() - start) + "ms");

        channelFuture.sync();

        start = System.currentTimeMillis();

        logger.info("Stopping Board Room server");

        server.stop();

        logger.info("Stopped Board Room server in " + (System.currentTimeMillis() - start) + "ms");
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Config getConfig() {
        return config;
    }

    public static Database getDatabase() {
        return database;
    }

    public static SecureRandom getSecureRandom() {
        return secureRandom;
    }

    public static MessageDigest getSHA256MessageDigest() {
        return SHA256MessageDigest;
    }

    public static Random getRandom() {
        return random;
    }
}

package net.stzups.board;

import io.netty.channel.ChannelFuture;
import net.stzups.board.config.Config;
import net.stzups.board.config.ConfigBuilder;
import net.stzups.board.config.configs.ArgumentConfig;
import net.stzups.board.config.configs.EnvironmentVariableConfig;
import net.stzups.board.config.configs.PropertiesConfig;
import net.stzups.board.data.database.Database;
import net.stzups.board.data.database.postgres.PostgresDatabase;
import net.stzups.board.data.database.runtime.RuntimeDatabase;
import net.stzups.board.server.Server;

import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Logger;

public class Board {
    private static Logger logger;
    private static Config config;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Random random = new Random();

    private static Database database;//user id -> user

    public static void main(String[] args) throws Exception {
        logger = LogFactory.getLogger("Board Server");

        logger.info("Starting Board server...");

        long start = System.currentTimeMillis();

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
                database = new PostgresDatabase(Board.getConfig().get("postgres.url"), Board.getConfig().get("postgres.user"), Board.getConfig().get("postgres.password"));
                logger.info("Connected to Postgres database");
            } else {
                logger.warning("Using debug only runtime database. No data will be persisted.");
                database = new RuntimeDatabase();
            }
        }

        Server server = new Server();
        ChannelFuture channelFuture = server.start();

        logger.info("Started Board server in " + (System.currentTimeMillis() - start) + "ms");

        channelFuture.sync();

        start = System.currentTimeMillis();

        logger.info("Stopping Board Server");

        server.stop();

        logger.info("Stopped Board Server in " + (System.currentTimeMillis() - start) + "ms");
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

    public static Random getRandom() {
        return random;
    }
}

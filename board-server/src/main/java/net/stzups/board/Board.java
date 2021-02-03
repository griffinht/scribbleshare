package net.stzups.board;

import io.netty.channel.ChannelFuture;
import net.stzups.board.config.ConfigProvider;
import net.stzups.board.config.ConfigProviderBuilder;
import net.stzups.board.config.configs.ArgumentConfig;
import net.stzups.board.config.configs.PropertiesConfig;
import net.stzups.board.data.DataAccessObject;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.HttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.server.Server;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

public class Board {
    private static Logger logger;
    private static ConfigProvider config;

    private static final int DOCUMENT_ID_LENGTH = 6;
    private static final String DEFAULT_DOCUMENT_NAME = "Untitled Document";

    private static DataAccessObject<HttpSession, User> users;
    private static DataAccessObject<String, Document> documents;

    public static User getUser(HttpSession httpSession) {
        User user = users.get(httpSession);
        if (user == null) {
            user = new User(httpSession);
            users.put(httpSession, user);
        }
        return user;
    }

    public static Document getDocument(String id) {
        return documents.get(id);
    }

    public static Collection<Document> getDocuments() {
        return documents.values();
    }

    public static Document createDocument(User owner) {
        String id;
        int a = 0;
        do {
            if (a++ > 1000) {
                Board.getLogger().warning("Infinite loop while getting document name for " + owner);
                return null;
            }
            id = RandomString.randomString(DOCUMENT_ID_LENGTH, RandomString.NUMERIC);
        } while (documents.containsKey(id));
        Document document = new Document(id, owner, DEFAULT_DOCUMENT_NAME);
        documents.put(document.getId(), document);
        return document;
    }


    public static void main(String[] args) throws Exception {
        logger = LogFactory.getLogger("Board Server");

        logger.info("Starting Board server...");

        long start = System.currentTimeMillis();

        config = new ConfigProviderBuilder()
                .addConfig(new ArgumentConfig(args))
                .addConfig(new PropertiesConfig("board.properties"))
                .build();

        Server server = new Server();
        ChannelFuture channelFuture = server.start();

        try {
            users = new DataAccessObject<>("users");
            documents = new DataAccessObject<>("documents");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

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

    public static ConfigProvider getConfig() {
        return config;
    }
}

package net.stzups.board;

import io.netty.channel.ChannelFuture;
import net.stzups.board.config.ConfigProvider;
import net.stzups.board.config.ConfigProviderBuilder;
import net.stzups.board.config.configs.ArgumentConfig;
import net.stzups.board.config.configs.PropertiesConfig;
import net.stzups.board.data.TokenGenerator;
import net.stzups.board.data.database.flatfile.FlatFileStorage;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.HttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.UserSession;
import net.stzups.board.server.Server;

import java.io.IOException;
import java.util.logging.Logger;

public class Board {
    private static Logger logger;
    private static ConfigProvider config;

    private static final String DEFAULT_DOCUMENT_NAME = "Untitled Document";

    private static FlatFileStorage<Long, User> users;//user id -> user
    private static FlatFileStorage<Long, Document> documents;//document id -> document
    private static FlatFileStorage<Long, HttpSession> httpSessions;//http session id -> http session todo unused
    private static FlatFileStorage<Long, UserSession> userSessions;//user session id -> user session

    public static Document getDocument(long id) {
        return documents.get(id);
    }


    public static void addUser(User user) {
        users.put(user.getId(), user);
    }

    public static User getUser(long id) {
        return users.get(id);
    }

    public static UserSession removeUserSession(long token) {
        return userSessions.remove(token);
    }

    public static void addUserSession(UserSession userSession) {
        userSessions.put(userSession.getToken(), userSession);
    }

    public static HttpSession getHttpSession(long token) {
        return httpSessions.get(token);
    }

    public static void addHttpSession(HttpSession httpSession) {
        httpSessions.put(httpSession.getToken(), httpSession);
    }

    public static Document createDocument(User owner) {
        Document document = new Document(TokenGenerator.getSecureRandom().nextLong(), owner, DEFAULT_DOCUMENT_NAME);
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
            users = new FlatFileStorage<>("users");
            documents = new FlatFileStorage<>("documents");
            httpSessions = new FlatFileStorage<>("httpSessions");
            userSessions = new FlatFileStorage<>("userSessions");
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

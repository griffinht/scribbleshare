package net.stzups.board.data.database.postgresql;

import net.stzups.board.data.database.Database;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.HttpSession;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.UserSession;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgresSQLDatabase implements Database {
    private Connection connection;

    public PostgresSQLDatabase(String url, String user, String password) throws Exception {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(url, user, password);
    }

    @Override
    public void addUser(User user) {

    }

    @Override
    public User getUser(long id) {
        return null;
    }

    @Override
    public Document getDocument(long id) {
        return null;
    }

    @Override
    public UserSession removeUserSession(long token) {
        return null;
    }

    @Override
    public void addUserSession(UserSession userSession) {

    }

    @Override
    public HttpSession getHttpSession(long token) {
        return null;
    }

    @Override
    public void addHttpSession(HttpSession httpSession) {

    }

    @Override
    public Document createDocument(User owner) {
        return null;
    }
}

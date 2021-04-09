package net.stzups.board.data.objects.session;

import io.netty.handler.codec.http.HttpRequest;
import net.stzups.board.data.objects.User;

import java.sql.Timestamp;

public class HttpSession extends Session {
    public HttpSession(User user) {
        super(user);
    }

    public HttpSession(long id, long user, Timestamp creation, byte[] hashedToken) {
        super(id, user, creation, hashedToken);
    }

    public boolean validate(HttpRequest httpRequest) {
        return true;
    }
}

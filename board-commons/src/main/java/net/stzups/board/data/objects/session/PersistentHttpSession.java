package net.stzups.board.data.objects.session;

import java.sql.Timestamp;

public class PersistentHttpSession extends HttpSession {


    public PersistentHttpSession(HttpSession httpSession) {
        super(httpSession.getId(), httpSession.getUser(), httpSession.getCreation(), httpSession.getHashedToken());
    }

    public PersistentHttpSession(long id, long user, Timestamp creation, byte[] hashedToken) {
        super(id, user, creation, hashedToken);
    }
}

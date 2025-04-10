package org.apache.mina.example.chat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IoHandler} implementation of a simple chat server protocol.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date: 2008/05/29 07:04:19 $
 */
public class ChatProtocolHandler extends IoHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<IoSession> sessions = Collections.synchronizedSet(new HashSet<IoSession>());

    private final Set<String> users = Collections.synchronizedSet(new HashSet<String>());

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        logger.warn("Unexpected exception.", cause);
        session.close();
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        Logger log = LoggerFactory.getLogger(ChatProtocolHandler.class);
        log.info("received: " + message);
        String theMessage = (String) message;
        String[] result = theMessage.split(" ", 2);
        String theCommand = result[0];
        try {
            ChatCommand command = ChatCommand.valueOf(theCommand);
            String user = (String) session.getAttribute("user");
            switch(command.toInt()) {
                case ChatCommand.QUIT:
                    session.write("QUIT OK");
                    session.close();
                    break;
                case ChatCommand.LOGIN:
                    if (user != null) {
                        session.write("LOGIN ERROR user " + user + " already logged in.");
                        return;
                    }
                    if (result.length == 2) {
                        user = result[1];
                    } else {
                        session.write("LOGIN ERROR invalid login command.");
                        return;
                    }
                    if (users.contains(user)) {
                        session.write("LOGIN ERROR the name " + user + " is already used.");
                        return;
                    }
                    sessions.add(session);
                    session.setAttribute("user", user);
                    MdcInjectionFilter.setProperty(session, "user", user);
                    users.add(user);
                    session.write("LOGIN OK");
                    broadcast("The user " + user + " has joined the chat session.");
                    break;
                case ChatCommand.BROADCAST:
                    if (result.length == 2) {
                        broadcast(user + ": " + result[1]);
                    }
                    break;
                default:
                    logger.info("Unhandled command: " + command);
                    break;
            }
        } catch (IllegalArgumentException e) {
            logger.debug("Illegal argument", e);
        }
    }

    public void broadcast(String message) {
        synchronized (sessions) {
            for (IoSession session : sessions) {
                if (session.isConnected()) {
                    session.write("BROADCAST OK " + message);
                }
            }
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        String user = (String) session.getAttribute("user");
        users.remove(user);
        sessions.remove(session);
        broadcast("The user " + user + " has left the chat session.");
    }

    public boolean isChatUser(String name) {
        return users.contains(name);
    }

    public int getNumberOfUsers() {
        return users.size();
    }

    public void kick(String name) {
        synchronized (sessions) {
            for (IoSession session : sessions) {
                if (name.equals(session.getAttribute("user"))) {
                    session.close();
                    break;
                }
            }
        }
    }
}

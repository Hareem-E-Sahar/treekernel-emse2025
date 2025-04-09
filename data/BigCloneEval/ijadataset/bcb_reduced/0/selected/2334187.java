package mathive.server;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mathive.client.ClientUser;
import mathive.client.UserService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import javax.jdo.Query;
import javax.jdo.PersistenceManager;
import mathive.server.persistant.SessionID;
import mathive.server.persistant.Test;
import mathive.server.persistant.User;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@SuppressWarnings("serial")
public class UserServiceImpl extends RemoteServiceServlet implements UserService {

    public String loginToServer(String username, String password) {
        PersistenceManager pm = PMF.getPersistenceManager();
        User user = getUser(username, pm);
        boolean successful = false;
        if (user != null) {
            String hashFromDB = user.getHashPassword();
            successful = BCrypt.checkpw(password, hashFromDB);
        }
        if (successful) {
            String sessionID = getThreadLocalRequest().getSession().getId();
            if (getSession(sessionID, pm) == null) {
                pm.makePersistent(new SessionID(sessionID, user.getKey()));
            }
            return sessionID;
        } else {
            return null;
        }
    }

    public ClientUser getUser(String session) {
        PersistenceManager pm = PMF.getPersistenceManager();
        SessionID sessionid = getSession(session, pm);
        User user = pm.getObjectById(User.class, sessionid.getUserKey());
        ClientUser clientUser = new ClientUser(user.getUsername(), user.getEmail(), user.getUsertype(), user.getScore());
        return clientUser;
    }

    public boolean logoutFromServer(String session) {
        PersistenceManager pm = PMF.getPersistenceManager();
        SessionID sessionid = getSession(session, pm);
        if (sessionid != null) {
            pm.deletePersistent(sessionid);
            return true;
        } else {
            return false;
        }
    }

    public boolean registerOnServer(String username, String password, String email) {
        PersistenceManager pm = PMF.getPersistenceManager();
        User user = getUser(username, pm);
        if (user == null) {
            pm.makePersistent(new User(username, BCrypt.hashpw(password, BCrypt.gensalt()), email));
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteUser(String session, String username) {
        PersistenceManager pm = PMF.getPersistenceManager();
        SessionID sessionid = getSession(session, pm);
        if (pm.getObjectById(User.class, sessionid.getUserKey()).getUsertype() == User.ADMIN) {
            User user = getUser(username, pm);
            pm.deletePersistent(user);
            return true;
        } else {
            return false;
        }
    }

    public boolean isOnline(String session) {
        PersistenceManager pm = PMF.getPersistenceManager();
        SessionID sessionid = getSession(session, pm);
        if (sessionid != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean addScore(String session, int score) {
        PersistenceManager pm = PMF.getPersistenceManager();
        SessionID sessionid = getSession(session, pm);
        if (sessionid != null) {
            User user = pm.getObjectById(User.class, sessionid.getUserKey());
            user.addScore(score);
            pm.close();
            return true;
        }
        return false;
    }

    public boolean resetPassword(String email) {
        PersistenceManager pm = PMF.getPersistenceManager();
        User user = getUserByEmail(email, pm);
        if (user != null) {
            Random randomGenerator = new Random();
            String password = Integer.toString(randomGenerator.nextInt(100000));
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            String msgBody = "This is your new password at Mathive Playground: " + password;
            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress("mackanhedvall@gmail.com", "Mathive Playground"));
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress("amaeha-6@student.ltu.se"));
                msg.setSubject("Your password has been reset");
                msg.setText(msgBody);
                Transport.send(msg);
                user.setHashPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                return true;
            } catch (AddressException e) {
            } catch (MessagingException e) {
            } catch (UnsupportedEncodingException e) {
            } finally {
                pm.close();
            }
        }
        return false;
    }

    public boolean ChangePassword(String session, String oldPassword, String newPassword) {
        PersistenceManager pm = PMF.getPersistenceManager();
        SessionID sessionid = getSession(session, pm);
        User user = pm.getObjectById(User.class, sessionid.getUserKey());
        if (BCrypt.checkpw(oldPassword, user.getHashPassword())) {
            user.setHashPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            pm.close();
            return true;
        }
        return false;
    }

    public boolean AdminChangePassword(String session, String username, String newPassword) {
        PersistenceManager pm = PMF.getPersistenceManager();
        SessionID sessionid = getSession(session, pm);
        User admin = pm.getObjectById(User.class, sessionid.getUserKey());
        User user = getUser(username, pm);
        if (admin.getUsertype() == User.ADMIN) {
            user.setHashPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            pm.close();
            return true;
        }
        return false;
    }

    public boolean changeUserType(String session, String username, int type) {
        PersistenceManager pm = PMF.getPersistenceManager();
        SessionID sessionid = getSession(session, pm);
        User admin = pm.getObjectById(User.class, sessionid.getUserKey());
        User user = getUser(username, pm);
        if (admin.getUsertype() == User.ADMIN) {
            user.setUsertype(type);
            pm.close();
            return true;
        }
        return false;
    }

    public String[] getAllUsers() {
        PersistenceManager pm = PMF.getPersistenceManager();
        Query query = pm.newQuery(User.class);
        List<User> list = (List<User>) query.execute();
        Iterator<User> iterator = list.iterator();
        SimpleDateFormat formatter;
        String[] userString = new String[list.size()];
        int i = 0;
        while (iterator.hasNext()) {
            User user = iterator.next();
            userString[i] = user.getUsername() + " " + user.getUsertype();
            i++;
        }
        return userString;
    }

    private User getUser(String username, PersistenceManager pm) {
        Query query = pm.newQuery(User.class);
        query.setUnique(true);
        query.declareParameters("String param");
        query.setFilter("username == param");
        return (User) query.execute(username);
    }

    private User getUserByEmail(String email, PersistenceManager pm) {
        Query query = pm.newQuery(User.class);
        query.setUnique(true);
        query.declareParameters("String param");
        query.setFilter("email == param");
        return (User) query.execute(email);
    }

    private SessionID getSession(String session, PersistenceManager pm) {
        Query query = pm.newQuery(SessionID.class);
        query.setUnique(true);
        query.declareParameters("String param");
        query.setFilter("sessionID == param");
        return (SessionID) query.execute(session);
    }
}

package uk.icat3.user;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.NoSuchUserException;

public class UserManager implements User {

    static Logger log = Logger.getLogger(UserManager.class);

    private User user;

    /** Creates a new instance of UserManager */
    public UserManager(String className) throws SessionException {
        user = (uk.icat3.user.User) createObject(className);
    }

    /** Creates a new instance of UserManager, uses default user implementation*/
    public UserManager(EntityManager manager) throws SessionException {
        user = (uk.icat3.user.User) createObject("uk.icat3.userdefault.facility.DefaultUser", manager);
    }

    /** Creates a new instance of UserManager with entity manager */
    public UserManager(String className, EntityManager manager) throws SessionException {
        user = (uk.icat3.user.User) createObject(className, manager);
    }

    public String getUserIdFromSessionId(String sessionId) throws SessionException {
        return user.getUserIdFromSessionId(sessionId);
    }

    public String login(String username, String password) throws SessionException {
        return user.login(username, password);
    }

    public String login(String username, String password, int lifetime) throws SessionException {
        return user.login(username, password, lifetime);
    }

    public boolean logout(String sessionId) {
        return user.logout(sessionId);
    }

    public UserDetails getUserDetails(String sessionId, String user) throws SessionException, NoSuchUserException {
        return this.user.getUserDetails(sessionId, user);
    }

    private static Object createObject(String className) {
        Object object = null;
        try {
            Class classDefinition = Class.forName(className);
            object = classDefinition.newInstance();
            log.trace("Object: " + object.toString());
        } catch (Exception e) {
            log.error(e);
        }
        return object;
    }

    private static Object createObject(String className, EntityManager manager) {
        Constructor entityManagerConstructor = null;
        Object object = null;
        try {
            Class[] entityManagerArgaClass = new Class[] { EntityManager.class };
            Object[] inArgs = new Object[] { manager };
            Class classDefinition = Class.forName(className);
            entityManagerConstructor = classDefinition.getConstructor(entityManagerArgaClass);
            log.trace("Constructor: " + entityManagerConstructor.toString());
            object = entityManagerConstructor.newInstance(inArgs);
            log.trace("Object: " + object.toString());
        } catch (Exception e) {
            log.error(e);
        }
        return object;
    }

    public String login(String adminUsername, String AdminPassword, String runAsUser) throws SessionException {
        return this.user.login(adminUsername, AdminPassword, runAsUser);
    }

    public String login(String credential) throws SessionException {
        return this.user.login(credential);
    }
}

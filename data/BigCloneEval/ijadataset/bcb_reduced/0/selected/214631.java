package org.helianto.core.filter.classic;

import java.lang.reflect.Constructor;
import org.helianto.core.Entity;
import org.helianto.core.User;

/**
 * Base class to filters that requires an <code>User</code>.
 * 
 * @author Mauricio Fernandes de Castro
 * @deprecated see AbstractPersonalFilterAdapter
 */
@SuppressWarnings("serial")
public abstract class AbstractUserBackedCriteriaFilter extends AbstractEntityBackedFilter {

    /**
     * Static factory method.
     * 
     * @param clazz
     * @param user
     */
    public static <T extends UserBackedFilter> T filterFactory(Class<T> clazz, User user) {
        try {
            T userBackedFilter = clazz.newInstance();
            userBackedFilter.setUser(user);
            return userBackedFilter;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to create filter ", e);
        }
    }

    /**
     * Static factory method.
     * 
     * @param enclosingInstance
     * @param clazz
     * @param user
     */
    @SuppressWarnings("unchecked")
    public static <T extends UserBackedFilter> T filterFactory(Object enclosingInstance, Class<T> clazz, User user) {
        try {
            Constructor<?> c = clazz.getConstructors()[0];
            T userBackedFilter = (T) c.newInstance(enclosingInstance);
            userBackedFilter.setUser(user);
            return userBackedFilter;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to create filter ", e);
        }
    }

    /**
	 * Default constructor
	 */
    protected AbstractUserBackedCriteriaFilter() {
        super();
    }

    /**
	 * User constructor.
	 */
    protected AbstractUserBackedCriteriaFilter(User user) {
        this();
        setUser(user);
    }

    /**
	 * Entity constructor.
	 */
    protected AbstractUserBackedCriteriaFilter(Entity entity) {
        this();
        setEntity(entity);
    }

    private User user;

    /**
     * User providing the current entity.
     */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user == null) {
            setEntity(null);
        } else {
            setEntity(user.getEntity());
        }
    }
}

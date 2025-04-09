package org.openwms.core.service.spring.aop;

import java.util.EventObject;
import org.openwms.core.annotation.FireAfterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

/**
 * An UserChangedEventAspect fires events after a method invocation completes.
 * It's main purpose is to fire events after a transaction succeeds, thereby the
 * advice must be enabled around Spring's Transaction advice.
 * <p>
 * Use the {@link FireAfterTransaction} event and declare some type of events
 * inside the <code>value</code> attribute. Instances of these events will then
 * be fired after the transaction completes.
 * </p>
 * Example: <blockquote>
 * 
 * <pre>
 * &#064;FireAfterTransaction(events = { UserChangedEvent.class })
 * public User save(User user) { .. }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * The component can be referenced by name {@value #COMPONENT_NAME}.
 * </p>
 * 
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version $Revision: 1539 $
 * @since 0.1
 * @see org.openwms.core.annotation.FireAfterTransaction
 */
@Component(UserChangedEventAspect.COMPONENT_NAME)
public class UserChangedEventAspect {

    @Autowired
    private ApplicationContext ctx;

    /**
     * Springs component name.
     */
    public static final String COMPONENT_NAME = "userChangedEventAspect";

    /**
     * Only {@link ApplicationEvent}s are created and published over Springs
     * {@link ApplicationContext}.
     * 
     * @param publisher
     *            The instance that is publishing the event
     * @param events
     *            A list of event classes to fire
     * @throws Throwable
     *             Any exception is re-thrown
     */
    public void fireUserEvent(Object publisher, FireAfterTransaction events) throws Throwable {
        for (int i = 0; i < events.events().length; i++) {
            Class<? extends EventObject> event = events.events()[i];
            if (ApplicationEvent.class.isAssignableFrom(event)) {
                ctx.publishEvent((ApplicationEvent) event.getConstructor(Object.class).newInstance(publisher));
            }
        }
    }
}

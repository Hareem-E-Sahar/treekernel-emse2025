package org.procol.framework.components;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import lights.interfaces.ITupleSpace;
import org.procol.framework.components.exceptions.ComponentInvocationException;

/**
 * A component that <b>sequentially</b> instantiates and executes
 * <i>multiple instances of a single component</i> specified  
 * by this component's constructor.
 * 
 * @author <a href="mailto:stefan.gudenkauf@offis.de">Stefan Gudenkauf</a>
 *
 */
public class MultipleInstanceSequentialComponent extends AbstractComponent {

    private final int invokeCount;

    private final Class<? extends AbstractComponent> processClass;

    /**
	 * Constructs new instances that should be executed sequentially, given a component (process) to be 
	 * instantiated, a number of references to the tuple spaces used for 
	 * communication, and the number of concurrent invocations of the component.
	 * 
	 * @param processClass The class of the component to be instantiated and executed concurrently
	 * @param spaces The tuple spaces used for communication
	 * @param invokeCount The number of concurrent invocations of the component
	 */
    public MultipleInstanceSequentialComponent(Class<? extends AbstractComponent> processClass, HashMap<String, ITupleSpace> spaces, int invokeCount) {
        super(spaces);
        this.processClass = processClass;
        if (invokeCount < 1) {
            this.invokeCount = 1;
        } else {
            this.invokeCount = invokeCount;
        }
    }

    public Boolean call() throws ComponentInvocationException {
        Constructor<? extends AbstractComponent> constructor;
        try {
            constructor = processClass.getConstructor(HashMap.class);
        } catch (SecurityException e1) {
            throw new ComponentInvocationException("The process to be executed could not be invoked.");
        } catch (NoSuchMethodException e1) {
            throw new ComponentInvocationException("The constructor of the process to be executed could not be found.");
        }
        for (int i = 0; i < this.invokeCount; i++) {
            IComponent proc;
            try {
                proc = constructor.newInstance(this.spaces);
            } catch (InstantiationException e) {
                throw new ComponentInvocationException("The process to be executed could not be instantiated.");
            } catch (IllegalAccessException e) {
                throw new ComponentInvocationException("The process to be executed is illegally accessed.");
            } catch (InvocationTargetException e) {
                throw new ComponentInvocationException("The process to be executed could not be invoked.");
            }
            proc.call();
        }
        return true;
    }
}

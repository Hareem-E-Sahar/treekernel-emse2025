package org.salamandra.web.spring.request;

import java.lang.reflect.Constructor;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.salamandra.web.core.config.AbstractTransformerFactory;
import org.salamandra.web.core.request.AbstractInspectRequest;
import org.springframework.web.servlet.mvc.Controller;

public class InspectRequestManager {

    private static Class<? extends AbstractInspectRequest>[] classes;

    private static Log LOG = LogFactory.getLog(InspectRequestManager.class);

    static {
        classes = (Class<? extends AbstractInspectRequest>[]) new Class[] { BeanEntityRequestC.class, JSEntityRequestC.class };
    }

    public Controller lookupController(HttpServletRequest request, AbstractTransformerFactory factory) {
        Class[] argsClass = new Class[] { HttpServletRequest.class };
        Object[] args = new Object[] { request };
        for (Class<? extends AbstractInspectRequest> c : classes) {
            try {
                Constructor<? extends AbstractInspectRequest> constructor = c.getConstructor(argsClass);
                AbstractInspectRequest inspectRequest = (AbstractInspectRequest) constructor.newInstance(args);
                if (inspectRequest.isValidate()) {
                    if (inspectRequest instanceof IControllerProvider) {
                        return ((IControllerProvider) inspectRequest).getController(factory);
                    }
                }
            } catch (Exception e) {
                LOG.error("Couldn't resolve AbstractInspectRequest class [" + c.getName() + "], using constructor: " + c, e);
            }
        }
        return null;
    }
}

package net.assimilator.resources.serviceui;

import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.JFrame;
import java.lang.reflect.*;
import net.jini.lookup.ui.factory.JFrameFactory;
import net.jini.core.lookup.ServiceItem;

/**
 * The UIFrameFactory class is a helper for use with the ServiceUI
 */
public class UIFrameFactory implements JFrameFactory, Serializable {

    private String className;

    private URL[] exportURL;

    public UIFrameFactory(URL exportUrl, String className) {
        this.className = className;
        this.exportURL = new URL[] { exportUrl };
    }

    public UIFrameFactory(URL[] exportURL, String className) {
        this.className = className;
        this.exportURL = exportURL;
    }

    public JFrame getJFrame(Object roleObject) {
        if (!(roleObject instanceof ServiceItem)) {
            throw new IllegalArgumentException("ServiceItem required");
        }
        ClassLoader cl = ((ServiceItem) roleObject).service.getClass().getClassLoader();
        JFrame component = null;
        final URLClassLoader uiLoader = URLClassLoader.newInstance(exportURL, cl);
        final Thread currentThread = Thread.currentThread();
        final ClassLoader parentLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                return (currentThread.getContextClassLoader());
            }
        });
        try {
            AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    currentThread.setContextClassLoader(uiLoader);
                    return (null);
                }
            });
            try {
                Class clazz = uiLoader.loadClass(className);
                Constructor constructor = clazz.getConstructor(new Class[] { Object.class });
                Object instanceObj = constructor.newInstance(new Object[] { roleObject });
                component = (JFrame) instanceObj;
            } catch (Throwable t) {
                if (t.getCause() != null) t = t.getCause();
                IllegalArgumentException e = new IllegalArgumentException("Unable to instantiate ServiceUI :" + t.getClass().getName() + ": " + t.getLocalizedMessage());
                e.initCause(t);
                throw e;
            }
        } finally {
            AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    currentThread.setContextClassLoader(parentLoader);
                    return (null);
                }
            });
        }
        return (component);
    }
}

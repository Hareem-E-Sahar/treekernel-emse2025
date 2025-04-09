package net.sf.hippopotam.designer;

import java.lang.reflect.Constructor;
import java.awt.Window;
import java.awt.Component;

/**
 *
 */
public class DesignerParameters {

    private String className;

    private Constructor constructor;

    private String[] messageBaseNames;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    public String[] getMessageBaseNames() {
        return messageBaseNames;
    }

    public void setMessageBaseNames(String[] messageBaseNames) {
        this.messageBaseNames = messageBaseNames;
    }

    public Object createBeanInstance() {
        try {
            Class[] parameterTypes = getConstructor().getParameterTypes();
            Object[] parameterObjects = new Object[parameterTypes.length];
            for (int i = 0; i < parameterObjects.length; i++) {
                Class clazz = parameterTypes[i];
                parameterObjects[i] = clazz.newInstance();
            }
            return getConstructor().newInstance(parameterObjects);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public Window createWindow(Object beanInstance) {
        Window result;
        if (beanInstance instanceof Window) {
            result = (Window) beanInstance;
        } else if (beanInstance instanceof Component) {
            Component panel = (Component) beanInstance;
            result = new ViewerFrame(panel);
        } else {
            throw new IllegalArgumentException(String.valueOf(beanInstance));
        }
        result.setSize(800, 600);
        result.setVisible(true);
        result.setLocationRelativeTo(null);
        return result;
    }
}

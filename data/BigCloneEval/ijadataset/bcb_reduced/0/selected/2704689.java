package org.allcolor.ywt.adapter.web;

import org.allcolor.xml.parser.CShaniDomParser;
import org.allcolor.ywt.filter.CContext;
import org.allcolor.ywt.filter.CMainFilter;
import org.allcolor.alc.webapp.SerializableObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 
DOCUMENT ME!
 *
 * @author Quentin Anciaux
 * @version 0.1.0
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class CSessionWrapper implements HttpSession, Serializable {

    /** DOCUMENT ME! */
    public static final long serialVersionUID = 1L;

    /** DOCUMENT ME! */
    private transient Constructor sessionBindingListenerBridge = null;

    /** DOCUMENT ME! */
    private transient List<HttpSessionListener> listSessionsListener = new ArrayList<HttpSessionListener>();

    /** DOCUMENT ME! */
    private transient Method getAttribute = null;

    /** DOCUMENT ME! */
    private transient Method getAttributeNames = null;

    /** DOCUMENT ME! */
    private transient Method getCreationTime = null;

    /** DOCUMENT ME! */
    private transient Method getId = null;

    /** DOCUMENT ME! */
    private transient Method getLastAccessedTime = null;

    /** DOCUMENT ME! */
    private transient Method getMaxInactiveInterval = null;

    /** DOCUMENT ME! */
    private transient Method invalidate = null;

    /** DOCUMENT ME! */
    private transient Method isNew = null;

    /** DOCUMENT ME! */
    private transient Method removeAttribute = null;

    /** DOCUMENT ME! */
    private transient Method setAttribute = null;

    /** DOCUMENT ME! */
    private transient Method setMaxInactiveInterval = null;

    /** DOCUMENT ME! */
    private transient Object delegate = null;

    /**
   * Creates a new CSessionWrapper object.
   * 
   * @param delegate
   *            DOCUMENT ME!
   */
    public CSessionWrapper(final Object delegate) {
        this.delegate = delegate;
        this.loadHttpSessionListener();
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param name DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
    public void createSessionBindingListenerBridge(final String name, final Object value) {
        try {
            if (this.sessionBindingListenerBridge == null) {
                final Class<?> clazz = CMainFilter.getInstance().getWebappLoader().loadClass("org.allcolor.alc.webapp.SessionBindingListenerBridge");
                this.sessionBindingListenerBridge = clazz.getConstructor(new Class[] { String.class, Object.class });
            }
            final Object binder = this.sessionBindingListenerBridge.newInstance(new Object[] { name, value });
            this._SetAttribute(name + ".session.binder.BRIDGE", binder);
        } catch (final Exception e) {
            ;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param arg0 DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public Object getAttribute(final String arg0) {
        try {
            if (this.getAttribute == null) {
                this.getAttribute = this.delegate.getClass().getMethod("getAttribute", new Class[] { String.class });
            }
            Object obj = this.getAttribute.invoke(this.delegate, new Object[] { arg0 });
            if (obj instanceof SerializableObject) {
                obj = ((SerializableObject) obj).getObj();
            }
            return obj;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public Enumeration getAttributeNames() {
        try {
            final Vector<String> attributeNames = new Vector<String>();
            if (this.getAttributeNames == null) {
                this.getAttributeNames = this.delegate.getClass().getMethod("getAttributeNames", (Class[]) null);
            }
            for (final Enumeration it = (Enumeration) this.getAttributeNames.invoke(this.delegate, (Object[]) null); it.hasMoreElements(); ) {
                attributeNames.add((String) it.nextElement());
            }
            return attributeNames.elements();
        } catch (final Exception e) {
            return null;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public long getCreationTime() {
        try {
            if (this.getCreationTime == null) {
                this.getCreationTime = this.delegate.getClass().getMethod("getCreationTime", (Class[]) null);
            }
            return ((Long) this.getCreationTime.invoke(this.delegate, (Object[]) null)).longValue();
        } catch (final Exception e) {
            return 0;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public Object getDelegate() {
        return this.delegate;
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public String getId() {
        try {
            if (this.getId == null) {
                this.getId = this.delegate.getClass().getMethod("getId", (Class[]) null);
            }
            return (String) this.getId.invoke(this.delegate, (Object[]) null);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public long getLastAccessedTime() {
        try {
            if (this.getLastAccessedTime == null) {
                this.getLastAccessedTime = this.delegate.getClass().getMethod("getLastAccessedTime", (Class[]) null);
            }
            return ((Long) this.getLastAccessedTime.invoke(this.delegate, (Object[]) null)).longValue();
        } catch (final Exception e) {
            return 0;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public int getMaxInactiveInterval() {
        try {
            if (this.getMaxInactiveInterval == null) {
                this.getMaxInactiveInterval = this.delegate.getClass().getMethod("getMaxInactiveInterval", (Class[]) null);
            }
            return ((Integer) this.getMaxInactiveInterval.invoke(this.delegate, (Object[]) null)).intValue();
        } catch (final Exception e) {
            return 0;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public ServletContext getServletContext() {
        return CContext.getInstance().getContext();
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public HttpSessionContext getSessionContext() {
        return null;
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param arg0 DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public Object getValue(final String arg0) {
        return this.getAttribute(arg0);
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public String[] getValueNames() {
        final List<String> result = new ArrayList<String>(0);
        for (final Enumeration it = this.getAttributeNames(); it.hasMoreElements(); ) {
            final String name = (String) it.nextElement();
            result.add(name);
        }
        return result.toArray(new String[result.size()]);
    }

    /**
	 * DOCUMENT ME!
	 */
    public void invalidate() {
        try {
            if (this.invalidate == null) {
                this.invalidate = this.delegate.getClass().getMethod("invalidate", (Class[]) null);
            }
            this.invalidate.invoke(this.delegate, (Object[]) null);
        } catch (final Exception e) {
            ;
        }
        try {
            this.finalize();
        } catch (final Throwable ignore) {
            if (ignore.getClass() == ThreadDeath.class) {
                throw (ThreadDeath) ignore;
            }
            Throwable cause = ignore.getCause();
            while (cause != null) {
                if (cause.getClass() == ThreadDeath.class) {
                    throw (ThreadDeath) cause;
                }
                cause = cause.getCause();
            }
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    public boolean isNew() {
        try {
            if (this.isNew == null) {
                this.isNew = this.delegate.getClass().getMethod("isNew", (Class[]) null);
            }
            return ((Boolean) this.isNew.invoke(this.delegate, (Object[]) null)).booleanValue();
        } catch (final Exception e) {
            return false;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param name DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
    public void putValue(final String name, final Object value) {
        this.setAttribute(name, value);
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param name DOCUMENT ME!
	 */
    public void removeAttribute(final String name) {
        if (!name.endsWith(".session.binder.BRIDGE")) {
            this.removeAttribute(name + ".session.binder.BRIDGE");
        }
        try {
            if (this.removeAttribute == null) {
                this.removeAttribute = this.delegate.getClass().getMethod("removeAttribute", new Class[] { String.class });
            }
            this.removeAttribute.invoke(this.delegate, new Object[] { name });
        } catch (final Exception e) {
            ;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param name DOCUMENT ME!
	 */
    public void removeValue(final String name) {
        this.removeAttribute(name);
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param name DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
    public void setAttribute(final String name, final Object value) {
        this._SetAttribute(name, value);
        if (value instanceof HttpSessionBindingListener) {
            this.createSessionBindingListenerBridge(name, value);
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param delegate DOCUMENT ME!
	 */
    public void setDelegate(final Object delegate) {
        this.delegate = delegate;
        if (this.delegate != null) {
            this.loadHttpSessionListener();
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param arg0 DOCUMENT ME!
	 */
    public void setMaxInactiveInterval(final int arg0) {
        try {
            if (this.setMaxInactiveInterval == null) {
                this.setMaxInactiveInterval = this.delegate.getClass().getMethod("setMaxInactiveInterval", new Class[] { int.class });
            }
            this.setMaxInactiveInterval.invoke(this.delegate, new Object[] { new Integer(arg0) });
        } catch (final Exception ignore) {
            ;
        }
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @throws Throwable DOCUMENT ME!
	 */
    @Override
    protected void finalize() throws Throwable {
        if (this.listSessionsListener == null) {
            return;
        }
        for (int i = 0; i < this.listSessionsListener.size(); i++) {
            final HttpSessionListener sl = this.listSessionsListener.get(i);
            sl.sessionDestroyed(new HttpSessionEvent(this));
        }
        this.listSessionsListener.clear();
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param name DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
    private void _SetAttribute(final String name, final Object value) {
        try {
            if (this.setAttribute == null) {
                this.setAttribute = this.delegate.getClass().getMethod("setAttribute", new Class[] { String.class, Object.class });
            }
            if ((value != null) && value.getClass().getName().equals("org.allcolor.alc.webapp.SessionBindingListenerBridge")) {
                this.setAttribute.invoke(this.delegate, new Object[] { name, value });
            } else {
                final SerializableObject so = new SerializableObject(value);
                this.setAttribute.invoke(this.delegate, new Object[] { name, so });
            }
        } catch (final Exception ignore) {
            ;
        }
    }

    /**
	 * DOCUMENT ME!
	 */
    private void loadHttpSessionListener() {
        try {
            if (this.listSessionsListener == null) {
                this.listSessionsListener = new ArrayList<HttpSessionListener>();
            }
            if (this.listSessionsListener.size() > 0) {
                return;
            }
            final CShaniDomParser parser = new CShaniDomParser();
            final Document doc = parser.parse(CContext.getInstance().getContext().getResource("/WEB-INF/config/servletcontext.xml"));
            final NodeList nl = doc.getElementsByTagNameNS("http://www.allcolor.org/xmlns/context", "session-listener");
            for (int i = 0; i < nl.getLength(); i++) {
                final Element listener = (Element) nl.item(i);
                try {
                    final Class clazz = Class.forName(listener.getAttribute("class"));
                    final HttpSessionListener hsl = (HttpSessionListener) clazz.newInstance();
                    hsl.sessionCreated(new HttpSessionEvent(this));
                    this.listSessionsListener.add(hsl);
                } catch (final Exception ignore) {
                    System.err.println("Error while creating listener: " + listener.getAttribute("class"));
                    ignore.printStackTrace();
                }
            }
        } catch (final Exception ignore) {
            System.err.println("Error while loading ServletContextListeners.");
            ignore.printStackTrace();
        }
    }
}

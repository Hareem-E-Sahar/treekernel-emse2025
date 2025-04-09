package it.ilz.hostingjava.deployer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Session;
import org.apache.tomcat.util.modeler.Registry;

/**
 *
 * @author luigi
 */
public class JMXDeployer {

    ObjectName oname = null;

    MBeanServer mBeanServer = null;

    /** Creates a new instance of JMXDeployer */
    public JMXDeployer() {
    }

    /** Creates a new instance of JMXDeployer */
    public JMXDeployer(String engine, String host) {
        this.engine = engine;
        this.host = host;
    }

    public Session[] findSessions(String path) throws MalformedObjectNameException, InstanceNotFoundException, MBeanException, ReflectionException {
        ObjectName manageroname = new ObjectName(engine + ":type=Manager,path=" + path + ",host=" + host);
        String[] params = {};
        String[] signature = {};
        Session[] sessions = null;
        sessions = (Session[]) mBeanServer.invoke(manageroname, "findSessions", params, signature);
        return sessions;
    }

    public void init() throws MalformedObjectNameException {
        oname = new ObjectName(engine + ":type=Deployer,host=" + host);
        mBeanServer = Registry.getRegistry(null, null).getMBeanServer();
    }

    /**
     * Invoke the isDeployed method on the deployer.
     */
    public boolean isDeployed(String name) throws InstanceNotFoundException, MBeanException, ReflectionException {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        Boolean result = (Boolean) mBeanServer.invoke(oname, "isDeployed", params, signature);
        return result.booleanValue();
    }

    /**
     * Invoke the check method on the deployer.
     */
    public void check(String name) throws InstanceNotFoundException, MBeanException, ReflectionException {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        mBeanServer.invoke(oname, "check", params, signature);
    }

    /**
     * Invoke the isServiced method on the deployer.
     */
    public boolean isServiced(String name) throws InstanceNotFoundException, MBeanException, ReflectionException {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        Boolean result = (Boolean) mBeanServer.invoke(oname, "isServiced", params, signature);
        return result.booleanValue();
    }

    /**
     * Invoke the addServiced method on the deployer.
     */
    public void addServiced(String name) throws InstanceNotFoundException, MBeanException, ReflectionException {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        mBeanServer.invoke(oname, "addServiced", params, signature);
    }

    public Host findHost() throws InstanceNotFoundException, MBeanException, ReflectionException, MalformedObjectNameException {
        ObjectName hostname = new ObjectName(engine + ":type=Host,host=" + host);
        return (Host) mBeanServer.getObjectInstance(hostname);
    }

    public void undeploy(String configBase, String path, Lifecycle context) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if (!isServiced(path)) {
            addServiced(path);
            try {
                if (context != null) context.stop();
            } catch (Throwable t) {
            }
            try {
                File xml = new File(configBase, getConfigFile(path) + ".xml");
                xml.delete();
                check(path);
            } finally {
                removeServiced(path);
            }
        }
    }

    public void deploy(String hjConfigBase, String configBase, String path) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if (!isServiced(path)) {
            addServiced(path);
            try {
                copy(new File(hjConfigBase, getConfigFile(path) + ".xml"), new File(configBase, getConfigFile(path) + ".xml"));
                check(path);
            } finally {
                removeServiced(path);
            }
        }
    }

    /**
     * Invoke the removeServiced method on the deployer.
     */
    public void removeServiced(String name) throws InstanceNotFoundException, MBeanException, ReflectionException {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        mBeanServer.invoke(oname, "removeServiced", params, signature);
    }

    /**
     * Holds value of property host.
     */
    private String host;

    /**
     * Getter for property host.
     * @return Value of property host.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Setter for property host.
     * @param host New value of property host.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Holds value of property engine.
     */
    private String engine;

    /**
     * Getter for property engine.
     * @return Value of property engine.
     */
    public String getEngine() {
        return this.engine;
    }

    /**
     * Setter for property engine.
     * @param engine New value of property engine.
     */
    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * Copy the specified file or directory to the destination.
     *
     * @param src File object representing the source
     * @param dest File object representing the destination
     */
    public static boolean copy(File src, File dest) {
        boolean result = false;
        try {
            if (src != null && !src.getCanonicalPath().equals(dest.getCanonicalPath())) {
                result = copyInternal(src, dest, new byte[4096]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Copy the specified file or directory to the destination.
     *
     * @param src File object representing the source
     * @param dest File object representing the destination
     */
    public static boolean copyInternal(File src, File dest, byte[] buf) {
        boolean result = true;
        String files[] = null;
        if (src.isDirectory()) {
            files = src.list();
            result = dest.mkdir();
        } else {
            files = new String[1];
            files[0] = "";
        }
        if (files == null) {
            files = new String[0];
        }
        for (int i = 0; (i < files.length) && result; i++) {
            File fileSrc = new File(src, files[i]);
            File fileDest = new File(dest, files[i]);
            if (fileSrc.isDirectory()) {
                result = copyInternal(fileSrc, fileDest, buf);
            } else {
                FileInputStream is = null;
                FileOutputStream os = null;
                try {
                    is = new FileInputStream(fileSrc);
                    os = new FileOutputStream(fileDest);
                    int len = 0;
                    while (true) {
                        len = is.read(buf);
                        if (len == -1) break;
                        os.write(buf, 0, len);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    result = false;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Given a context path, get the config file name.
     */
    protected String getConfigFile(String path) {
        String basename = null;
        if (path.equals("")) {
            basename = "ROOT";
        } else {
            basename = path.substring(1).replace('/', '#');
        }
        return (basename);
    }
}

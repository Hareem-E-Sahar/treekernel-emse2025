package org.parallelj.mda.rt.server;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.parallelj.mda.rt.logging.ParallelJLogger;
import org.parallelj.mda.rt.logging.MessageCode;
import org.parallelj.mda.rt.server.mbeans.EngineQueuerAdminRemote;
import org.parallelj.mda.rt.server.mbeans.ServerShutdownRemote;

/**
 * ParallelJ Server, used for Batch remote launching. Supports MBeans(JMX) and
 * telnet as ways to access the batches remotely
 * 
 * @author Atos Worldline
 * 
 */
public class ParallelJServer extends Thread {

    /**
	 * List of registered MBeans
	 */
    private List<ObjectName> taskMBeans = new ArrayList<ObjectName>();

    /**
	 * Inner class holding the ParallelJ server singleton
	 */
    public static class ParallelJServerHolder {

        static ParallelJServer instance = new ParallelJServer();
    }

    /**
	 * Returns ParallelJ Server current instance
	 * 
	 * @return ParallelJ Server instance
	 */
    public static ParallelJServer getInstance() {
        return ParallelJServerHolder.instance;
    }

    /**
	 * Returns the list of registered MBeans for this instance
	 * 
	 * @return
	 */
    public List<ObjectName> getTaskMBeans() {
        return this.taskMBeans;
    }

    /**
	 * JMX Connector Server instance
	 */
    private JMXConnectorServer jmxConnectorServer = null;

    /**
	 * Host for Telnet service
	 */
    private String telnetHost;

    /**
	 * Port for Telnet service
	 */
    private Integer telnetPort;

    /**
	 * Private constructor
	 * 
	 */
    private ParallelJServer() {
        this.setName("ParallelJ Server");
    }

    /**
	 * Starts the ParallelJ Server instance; with all the MBeans referenced in
	 * the ParallelJ.xml configuration file.
	 * 
	 */
    public void run() {
        try {
            synchronized (this) {
                ParallelJLogger.info(MessageCode.SRV01I);
                this.registerMBean(ServerShutdownRemote.class.getCanonicalName(), false);
                this.registerMBean(EngineQueuerAdminRemote.class.getCanonicalName(), false);
                List<String> classesName = ParallelJServerConfiguration.getMBeanNames();
                for (String className : classesName) this.registerMBean(className + "Remote", true);
                String host = "";
                int port = 0;
                List<String> mbeanHost = ParallelJServerConfiguration.getMBeanServerHosts();
                if (!mbeanHost.isEmpty()) host = mbeanHost.get(0);
                List<String> mbeanPort = ParallelJServerConfiguration.getMBeanServerPorts();
                if (!mbeanPort.isEmpty()) port = Integer.parseInt(mbeanPort.get(0));
                LocateRegistry.createRegistry(port);
                String serviceURL = "service:jmx:rmi://" + host + "/jndi/rmi://" + host + ":" + port + "/server";
                JMXServiceURL url = new JMXServiceURL(serviceURL);
                this.jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, ManagementFactory.getPlatformMBeanServer());
                this.jmxConnectorServer.start();
                ParallelJLogger.info(MessageCode.SRV02I, host, port, serviceURL);
                List<String> telnetHosts = ParallelJServerConfiguration.getTelnetServerHosts();
                List<String> telnetPorts = ParallelJServerConfiguration.getTelnetServerPorts();
                if (!telnetHosts.isEmpty() && !telnetPorts.isEmpty()) {
                    this.telnetPort = new Integer(telnetPorts.get(0));
                    this.telnetHost = telnetHosts.get(0);
                    Server telnetServer = new Server(10);
                    Service service = new TelnetService();
                    telnetServer.addService(service, this.telnetPort, this.telnetHost);
                    ParallelJLogger.info(MessageCode.SRV03I, this.telnetHost, this.telnetPort);
                }
                ParallelJLogger.info(MessageCode.SRV05I);
                this.wait();
            }
        } catch (Exception e) {
            this.taskMBeans.clear();
            ParallelJLogger.error(MessageCode.SRV09E, e, e.getClass().getCanonicalName());
        }
        ParallelJLogger.info(MessageCode.SRV06I);
    }

    /**
	 * Stops the ParallelJ instance
	 * 
	 */
    public void shutdown() {
        synchronized (this) {
            try {
                this.taskMBeans.clear();
                this.jmxConnectorServer.stop();
                ParallelJLogger.info(MessageCode.SRV08I);
                this.notify();
            } catch (Exception e) {
                ParallelJLogger.error(MessageCode.SRV10E, e, e.getClass().getCanonicalName());
            }
        }
    }

    /**
	 * Register a MBean in the current server instance
	 * 
	 * @param fqnName :
	 *            MBean class name
	 */
    private void registerMBean(String fqnName, boolean isTask) {
        try {
            String domain = fqnName.substring(0, fqnName.lastIndexOf('.'));
            String type = fqnName.substring(fqnName.lastIndexOf('.') + 1);
            ObjectName objectName = new ObjectName(domain + ":type=" + type);
            Class clazz = Class.forName(domain + "." + type);
            Constructor constructor = clazz.getConstructor((Class[]) null);
            if (!ManagementFactory.getPlatformMBeanServer().isRegistered(objectName)) {
                ManagementFactory.getPlatformMBeanServer().registerMBean(constructor.newInstance((Object[]) null), objectName);
                if (isTask) this.taskMBeans.add(objectName);
                ParallelJLogger.info(MessageCode.SRV07I, domain, type);
            } else {
                ParallelJLogger.info(MessageCode.SRV04I, domain, type);
            }
        } catch (Exception e) {
            ParallelJLogger.error(MessageCode.SRV11E, e, e.getClass().getCanonicalName());
        }
    }

    public static void main(String[] args) throws Exception {
        ParallelJServer.getInstance().start();
    }

    /**
	 * @return Server status
	 */
    public String getStatus() {
        return this.getState().toString();
    }
}

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.*;
import java.util.*;
import java.nio.*;

public class ApplicationHandler {

    ServerInterface si;

    Hashtable applications = new Hashtable();

    ApplicationHandler helper = null;

    LogonDialog logonDialog;

    Model model = null;

    Hashtable loaders = new Hashtable();

    public class Entry {

        public Object object;

        public boolean frameworkEvents;

        public Entry(Object object, boolean frameworkEvents) {
            this.object = object;
            this.frameworkEvents = frameworkEvents;
        }
    }

    public ApplicationHandler(ServerInterface si, Model model, LogonDialog logonDialog) {
        this.si = si;
        this.model = model;
        this.logonDialog = logonDialog;
        applications.put("system", new Entry(this, false));
    }

    public class AsyncLoader extends Thread {

        public String appname = null;

        public String appid = null;

        public String parameters = null;

        public NetworkClassLoader loader = null;

        AsyncLoader(String applicationName, String appID, String parameters, NetworkClassLoader loader) {
            this.parameters = parameters;
            this.appname = applicationName;
            this.appid = appID;
            this.loader = loader;
            start();
        }

        public void run() {
            String applicationName = appname;
            String appID = appid;
            String params = parameters;
            NetworkClassLoader cloader = loader;
            Object mainClass = null;
            try {
                System.out.println("Before loading __Main class\r\n");
                Class cl = cloader.loadClass("__Main");
                System.out.println("After loading __Main class\r\n");
                SystemDesktop desktopframe = new SystemDesktop();
                Constructor con = cl.getConstructor(new Class[] { si.getClass(), applicationName.getClass(), appID.getClass(), params.getClass(), helper.getClass(), desktopframe.getClass(), model.getClass() });
                mainClass = con.newInstance(new Object[] { si, applicationName, appID, params, helper, desktopframe, model });
                boolean eventsRequested = false;
                try {
                    Method method = mainClass.getClass().getMethod("frameworkEvents", null);
                    Object result = method.invoke(mainClass, null);
                    eventsRequested = ((Boolean) result).booleanValue();
                } catch (java.lang.NoSuchMethodException e) {
                    System.out.println("Warning: Method frameworkEvents not found.\r\n");
                }
                try {
                    Method method = mainClass.getClass().getMethod("initApplication", null);
                    method.invoke(mainClass, null);
                } catch (java.lang.NoSuchMethodException e) {
                    System.out.println("Warning: Method initApplication not found.\r\n");
                }
                for (Enumeration e = applications.elements(); e.hasMoreElements(); ) {
                    Object app = e.nextElement();
                    Entry entry = (Entry) app;
                    if (entry.frameworkEvents) {
                        Object[] args = new Object[1];
                        args[0] = new String(applicationName);
                        InvokeMethod(entry.object, "applicationLoadingFinished", args);
                    }
                }
                Entry entry = new Entry(mainClass, eventsRequested);
                String id = applicationName + " " + appID;
                System.out.println("#### New entry added to applications-list: " + id + "\r\n");
                applications.put(id, entry);
            } catch (Throwable throwable) {
                System.out.println("AsyncLoader exception " + throwable + "\r\n");
                throwable.printStackTrace();
            }
            ;
        }
    }

    public Object InvokeMethod(Object target, String methodname, Object[] args) {
        Method[] methods = target.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodname)) {
                Object res = null;
                try {
                    res = methods[i].invoke(target, args);
                } catch (Exception exc) {
                    System.out.println("exception: " + exc + "\r\n");
                }
                ;
                return res;
            }
        }
        return null;
    }

    public void execute(byte[] data, int msgtype) {
        System.out.println("ApplicationHandler, execute. Message type: " + msgtype + "\r\n");
        if (msgtype == 1) {
            String line = new String(data);
            StringTokenizer t = new StringTokenizer(line);
            String command = t.nextToken();
            System.out.println("SYSTEM CALLBACK >>> " + command + "\r\n");
            Object[] args = null;
            int count = t.countTokens();
            if (count > 0) {
                args = new Object[count];
                int i = 0;
                while (t.hasMoreTokens()) {
                    String x = t.nextToken();
                    System.out.println("Parameter >>> " + x + "\r\n");
                    args[i] = x;
                    i++;
                }
            }
            InvokeMethod(this, command, args);
            return;
        } else if (msgtype == 2) {
            String line = new String(data);
            model.ImportData(line);
            for (Enumeration e = applications.elements(); e.hasMoreElements(); ) {
                Object app = e.nextElement();
                Entry entry = (Entry) app;
                InvokeMethod(entry.object, "objectModelChangedX", null);
            }
            return;
        }
        String line = new String(data);
        StringTokenizer t = new StringTokenizer(line);
        int count = t.countTokens();
        String obj = t.nextToken();
        obj += " ";
        obj += t.nextToken();
        System.out.println("***********CALLBACK METODI: " + obj + "\r\n");
        Object application = applications.get(obj);
        if (application == null) {
            System.out.println("Error: Requested application not found " + obj + "\r\n");
            return;
        }
        count--;
        String command = t.nextToken();
        System.out.println("***********CALLBACK METODI: " + command + "  application: " + obj + "\r\n");
        int headerlen = obj.length() + command.length() + 2;
        Object[] args = new Object[1];
        byte[] result = new byte[data.length - headerlen];
        System.arraycopy(data, headerlen, result, 0, data.length - headerlen);
        args[0] = result;
        Entry entry = (Entry) application;
        Object interfaceClass = InvokeMethod(entry.object, "GetInterfaceClass", null);
        InvokeMethod(interfaceClass, command, args);
    }

    public Model getModel() {
        return model;
    }

    /***************************************************/
    public void hideProgressInfo() {
        logonDialog.hideProgressInfo();
    }

    /***************************************************/
    public void start(String applicationName, String appID, String timestamp, String parameters1, String parameters2) {
        String parameters = parameters1 + " " + parameters2;
        System.out.println("ApplicationHandler: start application > " + applicationName + "\r\n");
        helper = this;
        boolean fromServer = false;
        Object cl = loaders.get(applicationName);
        if (cl == null) {
            cl = new NetworkClassLoader(si, applicationName, timestamp);
            loaders.put(applicationName, cl);
            fromServer = true;
        } else if (((NetworkClassLoader) cl).getTimestamp().equals(timestamp) == false) {
            loaders.remove(applicationName);
            cl = new NetworkClassLoader(si, applicationName, timestamp);
            loaders.put(applicationName, cl);
            fromServer = true;
        }
        for (Enumeration e = applications.elements(); e.hasMoreElements(); ) {
            Object app = e.nextElement();
            Entry entry = (Entry) app;
            if (entry.frameworkEvents) {
                Object[] args = new Object[2];
                args[0] = new String(applicationName);
                args[1] = new Boolean(fromServer);
                InvokeMethod(entry.object, "applicationLoadingStarted", args);
            }
        }
        new AsyncLoader(applicationName, appID, parameters, (NetworkClassLoader) cl);
    }

    public void stop(String applicationName, String appID) {
        System.out.println("STOP APPLICATION: " + applicationName + " " + appID + "\r\n");
        Entry entry = (Entry) applications.get(applicationName + " " + appID);
        Object application = entry.object;
        applications.remove(applicationName + " " + appID);
        try {
            Method method = application.getClass().getMethod("stopApplication", null);
            method.invoke(application, null);
        } catch (Exception exc) {
            System.out.println("exception: " + exc + "\r\n");
        }
        ;
    }

    public void shutdown() {
        int size = applications.size();
        String applicationnames[] = new String[size];
        Enumeration e = applications.keys();
        int n = 0;
        while (e.hasMoreElements()) {
            applicationnames[n] = (String) e.nextElement();
            n++;
        }
        for (int t = 0; t < size; t++) {
            if (!applicationnames[t].equals("system")) {
                StringTokenizer temppi = new StringTokenizer(applicationnames[t]);
                stop(temppi.nextToken(), temppi.nextToken());
            }
        }
        si.disconnect();
        System.exit(0);
    }

    public void logout() {
        int size = applications.size();
        String applicationnames[] = new String[size];
        Enumeration e = applications.keys();
        int n = 0;
        while (e.hasMoreElements()) {
            applicationnames[n] = (String) e.nextElement();
            n++;
        }
        for (int t = 0; t < size; t++) {
            if (!applicationnames[t].equals("system")) {
                StringTokenizer temppi = new StringTokenizer(applicationnames[t]);
                stop(temppi.nextToken(), temppi.nextToken());
            }
        }
        si.disconnect();
    }
}

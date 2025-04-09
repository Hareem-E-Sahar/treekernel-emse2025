package worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import servermonitor.DynamicDeserializer;
import util.JarClassLoader;
import util.NetUtilities;

public class Action extends Thread {

    private static final String TIME_ESTIMATION_CMD = "/HerschelApi/HerschelApi?cmd=TimeEst";

    private static final String SUBMIT_CMD = "/HerschelApi/HerschelApi?cmd=ProposalSubmit";

    private String m_server;

    private int m_port;

    private String m_worker;

    private boolean m_active;

    private ClassLoader m_classLoader;

    public Action(String server, int port, String worker, ClassLoader classLoader) {
        super();
        m_server = server;
        m_port = port;
        m_worker = worker;
        m_classLoader = classLoader;
    }

    private void execute() {
        String[] comment = null;
        float estimation = -99;
        try {
            String className = "herschel.phs.gui.data.HerschelRequest";
            double ra = 12.3;
            double dec = 23.3;
            Hashtable<String, Object> pset = new Hashtable<String, Object>();
            pset.put("source", "small");
            pset.put("nRepetitions", (Number) 2L);
            pset.put("choppingAvoidOn", false);
            String obsModeClassName = "herschel.phs.gui.data.SpirePhoto";
            Object request = createObservationRequest(m_classLoader, obsModeClassName, "P", ra, dec, pset);
            Vector<Object> vIn = new Vector<Object>();
            vIn.add(request);
            Vector out = processRequest(vIn, m_server, m_port, m_worker, TIME_ESTIMATION_CMD, className, m_classLoader);
            Class execStatusClass = Class.forName("edu.caltech.ipac.util.ExecStatus", false, m_classLoader);
            Class spirePhotoSmallEstimates = Class.forName("herschel.phs.gui.data.timeest.SpirePhotoSmallEstimates", false, m_classLoader);
            Object es = out.get(0);
            Method isOk = execStatusClass.getMethod("isOK", null);
            Boolean resultIsOk = (Boolean) isOk.invoke(es, null);
            if (resultIsOk.booleanValue()) {
                Object spireEstimates = out.get(1);
                Method getMessages = spirePhotoSmallEstimates.getMethod("getMessages", null);
                comment = (String[]) getMessages.invoke(spireEstimates, null);
                Method getEstimation = spirePhotoSmallEstimates.getMethod("getEstDuration", null);
                estimation = ((Float) getEstimation.invoke(spireEstimates, null)).floatValue();
            } else {
                Method getException = execStatusClass.getMethod("getException", null);
                comment = new String[] { getException.invoke(es, null).toString() };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long st = 0;
        long et = 0;
        long ct = 0;
        long av = 0;
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        long total = 0;
        int ntm = 1;
        try {
            m_active = true;
            while (m_active) {
                st = System.currentTimeMillis();
                execute();
                et = System.currentTimeMillis();
                ct = et - st;
                total += ct;
                av = total / ntm;
                if (ct > max) {
                    max = ct;
                }
                if (ct < min) {
                    min = ct;
                }
                System.out.println("Iteration " + ntm + "\t" + "Current " + ct + " ms\t" + "Average " + av + " ms\t" + "Maximum " + max + " ms\t" + "Minimun " + min + " ms\t");
                ntm++;
            }
        } catch (Exception e) {
        }
    }

    public static ClassLoader initClassLoader(String server, int port, String worker) {
        String[] NEEDED_JARS = new String[] { "phsGui.jar", "data.jar", "astro.jar", "uplink_util.jar" };
        ClassLoader classLoader = null;
        String jarTempPath = server + "_" + port + "_" + worker;
        File jarTempDir = new File(jarTempPath);
        if (!jarTempDir.exists()) {
            if (!jarTempDir.mkdir()) {
                System.out.println(jarTempPath + " cannot be created.");
            }
        } else {
            System.out.println(jarTempPath + " exists.");
        }
        for (int i = 0; i < NEEDED_JARS.length; i++) {
            String jarName = NEEDED_JARS[i];
            File jarTempFile = new File(jarTempPath + "/" + jarName);
            if (!jarTempFile.exists()) {
                NetUtilities.downloadUpdate(server, port, worker, jarName, jarTempDir.getAbsolutePath());
            }
        }
        classLoader = new JarClassLoader(jarTempDir.getAbsolutePath(), NEEDED_JARS);
        return classLoader;
    }

    private static Vector processRequest(Vector vIn, String server, int port, String worker, String cmd, String className, ClassLoader classLoader) throws Exception {
        byte[] objByteArray;
        String theURL = "http://" + server + ":" + port + "/" + worker + cmd;
        URL aURL = new URL(theURL);
        URLConnection con = aURL.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        if (className == null) {
            con.setRequestProperty("Content-Type", "text/plain");
        } else {
            con.setRequestProperty("Content-Type", "java-internal/" + className);
        }
        if (vIn != null) {
            ObjectOutputStream objOS = new ObjectOutputStream(new BufferedOutputStream(con.getOutputStream()));
            objOS.writeObject(vIn);
            objOS.flush();
            objOS.close();
        }
        int numberBytes;
        Vector byteVector = new Vector();
        BufferedInputStream objIS = new BufferedInputStream(con.getInputStream());
        try {
            numberBytes = objIS.available();
            byte[] buffer = new byte[numberBytes];
            int readed = 0;
            while ((readed = objIS.read(buffer)) != -1) {
                for (int i = 0; i < readed; i++) {
                    byteVector.add(buffer[i]);
                }
            }
            objIS.close();
        } catch (IOException ioe) {
            objIS.close();
            throw ioe;
        } catch (Exception cnfe) {
            objIS.close();
            throw cnfe;
        }
        objByteArray = new byte[byteVector.size()];
        for (int i = 0; i < objByteArray.length; i++) {
            Byte element = (Byte) byteVector.get(i);
            objByteArray[i] = element.byteValue();
        }
        Vector out = (Vector) DynamicDeserializer.deserialize(server, port, worker, classLoader, objByteArray);
        return out;
    }

    private static Object createObservationRequest(ClassLoader classLoader, String obsModeClassName, String title, double ra, double dec, Hashtable<String, Object> pset) throws Exception {
        Class hreqClass = Class.forName("herschel.phs.gui.data.HerschelRequest", true, classLoader);
        Class obsModeClass = Class.forName(obsModeClassName, true, classLoader);
        Class positionJ2000Class = Class.forName("edu.caltech.ipac.target.PositionJ2000", true, classLoader);
        Class targetFixedSingleClass = Class.forName("edu.caltech.ipac.target.TargetFixedSingle", true, classLoader);
        Object request = hreqClass.newInstance();
        Object modeOut = obsModeClass.newInstance();
        Class[] positionArgsClass = new Class[] { double.class, double.class };
        Object[] positionArgs = new Object[] { ra, dec };
        Constructor createPositionJ2000 = positionJ2000Class.getConstructor(positionArgsClass);
        Object positionJ2000 = createPositionJ2000.newInstance(positionArgs);
        Class[] targetArgsClass = new Class[] { String.class, positionJ2000Class };
        Object[] targetArgs = new Object[] { "Dummy position", positionJ2000 };
        Constructor createTargetFixedSingle = targetFixedSingleClass.getConstructor(targetArgsClass);
        Object targetFixedSingle = createTargetFixedSingle.newInstance(targetArgs);
        Enumeration<String> keys = pset.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Object value = pset.get(key);
            Method setValue = null;
            if (value.getClass() == String.class || value.getClass() == Boolean.class) {
                setValue = obsModeClass.getMethod("setValue", new Class[] { key.getClass(), value.getClass() });
            } else {
                setValue = obsModeClass.getMethod("setValue", new Class[] { key.getClass(), Number.class });
            }
            setValue.invoke(modeOut, new Object[] { key, value });
        }
        Method setReqType = hreqClass.getMethod("setReqType", Class.forName("edu.caltech.ipac.data.InstrumentMode", false, classLoader));
        setReqType.invoke(request, modeOut);
        Method setTarget = hreqClass.getMethod("setTarget", Class.forName("edu.caltech.ipac.target.Target", false, classLoader));
        setTarget.invoke(request, targetFixedSingle);
        invoke(request, "setTitle", new Object[] { title }, new Class[] { String.class });
        return request;
    }

    private static void invoke(Object obj, String methodName, Object[] args, Class[] argsClass) throws Exception {
        Class objClass = obj.getClass();
        Method method = objClass.getMethod(methodName, argsClass);
        method.invoke(obj, args);
    }

    private static Object createProposal(ClassLoader classLoader, Vector obs) throws Exception {
        Class propClass = Class.forName("herschel.phs.gui.data.proposal.PhsProposal", true, classLoader);
        Object proposal = propClass.newInstance();
        invoke(proposal, "setTitle", new Object[] { "proposal name" }, new Class[] { String.class });
        invoke(proposal, "setAbstractText", new Object[] { "Proposal abstract text" }, new Class[] { String.class });
        invoke(proposal, "setCategory", new Object[] { "SDP" }, new Class[] { String.class });
        Class[] phsScienceCategoryArgsClass = new Class[] { String.class, String.class, String.class };
        Object[] phsScienceCategoryArgs = new Object[] { "Nearby galaxies", null, null };
        Class phsScienceCategoryClass = Class.forName("herschel.phs.gui.data.proposal.PhsScienceCategory", true, classLoader);
        Constructor createPhsScienceCategory = phsScienceCategoryClass.getConstructor(phsScienceCategoryArgsClass);
        Object phsScienceCategory = createPhsScienceCategory.newInstance(phsScienceCategoryArgs);
        invoke(proposal, "setScienceCategory", new Object[] { phsScienceCategory }, new Class[] { phsScienceCategoryClass });
        invoke(proposal, "setObservationRequests", new Object[] { obs }, new Class[] { obs.getClass() });
        Class[] phsProgramDetailsArgsClass = new Class[] { String.class, String.class, int.class, int.class, boolean.class, boolean.class };
        Object[] phsProgramDetailsArgs = new Object[] { "SDP", "SDP", 0, 1, false, true };
        Class phsProgramDetailsClass = Class.forName("herschel.phs.gui.data.proposal.PhsProgrammeDetails", true, classLoader);
        Constructor createPhsProgramDetails = phsProgramDetailsClass.getConstructor(phsProgramDetailsArgsClass);
        Object phsProgramDetails = createPhsProgramDetails.newInstance(phsProgramDetailsArgs);
        invoke(proposal, "setProgramDetails", new Object[] { phsProgramDetails }, new Class[] { phsProgramDetailsClass });
        return proposal;
    }

    private static Object createUser(ClassLoader classLoader, String username, String password) throws Exception {
        Class phsUserClass = Class.forName("herschel.phs.gui.data.proposal.PhsUser", true, classLoader);
        Constructor createPhsUser = phsUserClass.getConstructor(new Class[] { String.class, String.class });
        Object phsUser = createPhsUser.newInstance(new Object[] { username, password });
        return phsUser;
    }

    /**
     * Stores raw data into object
     *
     * @param entities Array of Strings - each String corresponding to the name of a raw file or directory to be added to the Zip archive.
     */
    public static byte[] pack(String filename) throws IOException {
        byte[] data = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(out);
        zout.setLevel(0);
        System.out.println("ZippingFile Zipping " + filename);
        try {
            String tmp = (new File(filename)).getName();
            System.out.println("Storing the following file name" + tmp);
            ZipEntry ze = new ZipEntry(tmp);
            zout.putNextEntry(ze);
            FileInputStream fin = new FileInputStream(filename);
            copy(fin, zout);
            zout.closeEntry();
            fin.close();
        } catch (IndexOutOfBoundsException e) {
            System.out.println("FileNotProcessed Problems processing " + filename);
        }
        zout.close();
        data = out.toByteArray();
        out.close();
        return data;
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        synchronized (in) {
            synchronized (out) {
                byte[] buffer = new byte[256];
                while (true) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    public static void main(String[] args) {
        String server = "localhost";
        int port = 8080;
        String worker = "herschel_phs_test";
        ClassLoader cl = Action.initClassLoader(server, port, worker);
        try {
            String className = "herschel.phs.gui.data.proposal.PhsUser";
            double ra = 12.3;
            double dec = 23.3;
            Hashtable<String, Object> pset = new Hashtable<String, Object>();
            pset.put("source", "small");
            pset.put("nRepetitions", (Number) 2L);
            pset.put("choppingAvoidOn", false);
            String obsModeClassName = "herschel.phs.gui.data.SpirePhoto";
            Vector obs = new Vector();
            for (int i = 0; i < 200; i++) {
                Object request = createObservationRequest(cl, obsModeClassName, "P" + i, ra, dec, pset);
                obs.add(request);
            }
            Vector vIn = new Vector();
            vIn.add(Action.createUser(cl, "randres", "BRABOJO"));
            vIn.add(Action.createProposal(cl, obs));
            vIn.add(pack("/home/randres/Desktop/certify.pdf"));
            Vector out = processRequest(vIn, server, port, worker, SUBMIT_CMD, className, cl);
            String newId = (String) out.elementAt(1);
            System.out.println(newId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

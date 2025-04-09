package phsperformance.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import phsperformance.util.CustomObjectInputStream;
import phsperformance.util.JarClassLoader;
import phsperformance.util.NetUtilities;
import phsperformance.util.data.RequestInfo;

public class PhsSerialization {

    private static final String TIME_ESTIMATION_CMD = "/HerschelApi/HerschelApi?cmd=TimeEst";

    private static final String SUBMIT_CMD = "/HerschelApi/HerschelApi?cmd=ProposalSubmit";

    private static final InputStream MY_PDF = PhsSerialization.class.getResourceAsStream("resources/mypdf.pdf");

    private static final String VISIBILITY_CMD = "/HerschelApi/HerschelApi?cmd=ShadowVisiblityReq";

    private static final String OVERLAY_CMD = "/HerschelApi/HerschelApi?cmd=AorFootprint";

    private static final String UPDATE_CMD = "/HerschelApi/HerschelApi?cmd=ProposalResubmit";

    private static final String RETRIEVE_PROP_IDS_CMD = "/HerschelApi/HerschelApi?cmd=ProposalGetIds";

    public static Vector doTimeEst(Vector vIn, String server, int port, String worker, ClassLoader classLoader) throws Exception {
        return processRequest(vIn, server, port, worker, TIME_ESTIMATION_CMD, "herschel.phs.gui.data.HerschelRequest", classLoader);
    }

    public static Vector doSubmit(Object user, Object proposal, String server, int port, String worker, ClassLoader classLoader) throws Exception {
        Vector vIn = new Vector();
        vIn.add(user);
        vIn.add(proposal);
        vIn.add(packPdf());
        return processRequest(vIn, server, port, worker, SUBMIT_CMD, user.getClass().getCanonicalName(), classLoader);
    }

    public static Vector doUpdate(Object user, Object proposal, Boolean force, String server, int port, String worker, ClassLoader classLoader) throws Exception {
        Vector vIn = new Vector();
        vIn.add(user);
        vIn.add(proposal);
        vIn.add(packPdf());
        vIn.add(force);
        return processRequest(vIn, server, port, worker, UPDATE_CMD, user.getClass().getCanonicalName(), classLoader);
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
        Vector out = (Vector) deserialize(server, port, worker, classLoader, objByteArray);
        return out;
    }

    public static Object createObservationRequest(ClassLoader classLoader, RequestInfo reqInfo, double ra, double dec) throws Exception {
        return createObservationRequest(classLoader, reqInfo.getObservingMode(), reqInfo.getTitle(), ra, dec, null, reqInfo.getParameterMap());
    }

    public static Object createObservationRequest(ClassLoader classLoader, RequestInfo reqInfo, double ra, double dec, Date visibleDate) throws Exception {
        return createObservationRequest(classLoader, reqInfo.getObservingMode(), reqInfo.getTitle(), ra, dec, visibleDate, reqInfo.getParameterMap());
    }

    public static Object createObservationRequest(ClassLoader classLoader, String obsModeClassName, String title, double ra, double dec, Date visibleDate, Map pset) throws Exception {
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
        if (visibleDate != null) {
            Class herschelTargetUtilClass = Class.forName("herschel.phs.gui.target.HerschelTargetUtil", true, classLoader);
            Class targetClass = Class.forName("edu.caltech.ipac.target.Target", true, classLoader);
            Class[] setVisibleDateArgsClass = new Class[] { targetClass, Date.class };
            Object[] setVisibleDateArgs = new Object[] { targetFixedSingle, visibleDate };
            Method setVisibleDate = herschelTargetUtilClass.getMethod("setVisibleDate", setVisibleDateArgsClass);
            setVisibleDate.invoke(targetFixedSingle, setVisibleDateArgs);
        }
        for (Object keyObj : pset.keySet()) {
            String key = keyObj.toString();
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

    public static Object createProposal(ClassLoader classLoader, String title, String proposalId, String programme, Vector obs) throws Exception {
        Class propClass = Class.forName("herschel.phs.gui.data.proposal.PhsProposal", true, classLoader);
        Object proposal = propClass.newInstance();
        invoke(proposal, "setTitle", new Object[] { title }, new Class[] { String.class });
        invoke(proposal, "setAbstractText", new Object[] { "Proposal abstract text" }, new Class[] { String.class });
        invoke(proposal, "setCategory", new Object[] { programme }, new Class[] { String.class });
        Class[] phsScienceCategoryArgsClass = new Class[] { String.class, String.class, String.class };
        Object[] phsScienceCategoryArgs = new Object[] { "Nearby galaxies", null, null };
        Class phsScienceCategoryClass = Class.forName("herschel.phs.gui.data.proposal.PhsScienceCategory", true, classLoader);
        Constructor createPhsScienceCategory = phsScienceCategoryClass.getConstructor(phsScienceCategoryArgsClass);
        Object phsScienceCategory = createPhsScienceCategory.newInstance(phsScienceCategoryArgs);
        invoke(proposal, "setScienceCategory", new Object[] { phsScienceCategory }, new Class[] { phsScienceCategoryClass });
        invoke(proposal, "setObservationRequests", new Object[] { obs }, new Class[] { obs.getClass() });
        if (proposalId != null) {
            invoke(proposal, "setProposalId", new Object[] { proposalId }, new Class[] { proposalId.getClass() });
        }
        Class[] phsProgramDetailsArgsClass = new Class[] { String.class, String.class, int.class, int.class, boolean.class, boolean.class };
        Object[] phsProgramDetailsArgs = new Object[] { programme, programme, 1, 0, false, true };
        Class phsProgramDetailsClass = Class.forName("herschel.phs.gui.data.proposal.PhsProgrammeDetails", true, classLoader);
        Constructor createPhsProgramDetails = phsProgramDetailsClass.getConstructor(phsProgramDetailsArgsClass);
        Object phsProgramDetails = createPhsProgramDetails.newInstance(phsProgramDetailsArgs);
        invoke(proposal, "setProgramDetails", new Object[] { phsProgramDetails }, new Class[] { phsProgramDetailsClass });
        return proposal;
    }

    public static Object createUser(ClassLoader classLoader, String username, String password) throws Exception {
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
    public static byte[] packPdf() throws IOException {
        byte[] data = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(out);
        zout.setLevel(0);
        try {
            String filename = "my_pdf.pdf";
            ZipEntry ze = new ZipEntry(filename);
            zout.putNextEntry(ze);
            copy(MY_PDF, zout);
            zout.closeEntry();
        } catch (IndexOutOfBoundsException e) {
            System.out.println("FileNotProcessed Problems processing ");
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

    public static Object deserialize(String server, long port, String worker, ClassLoader jarCl, byte[] objectByte) {
        ObjectInputStream objIS = null;
        Object out = null;
        try {
            objIS = new CustomObjectInputStream(new ByteArrayInputStream(objectByte), jarCl);
            out = objIS.readObject();
        } catch (IOException ioe) {
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } finally {
            try {
                objIS.close();
            } catch (Exception e) {
            }
        }
        if (out == null) {
            System.out.println("Non recuperable object comming from " + server + ":" + port + "/" + worker);
        }
        return out;
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

    public static Vector doVisibility(Object request, String server, int port, String worker, ClassLoader classLoader) throws Exception {
        Vector<Object> vIn = new Vector<Object>();
        vIn.add(request);
        vIn.add(null);
        return processRequest(vIn, server, port, worker, VISIBILITY_CMD, "herschel.phs.gui.data.HerschelRequest", classLoader);
    }

    public static Vector doOverlay(Object request, String server, int port, String worker, ClassLoader classLoader) throws Exception {
        Vector<Object> vIn = new Vector<Object>();
        vIn.add(request);
        return processRequest(vIn, server, port, worker, OVERLAY_CMD, "herschel.phs.gui.data.HerschelRequest", classLoader);
    }

    public static Vector retrieveProposalIds(Object user, String server, int port, String worker, ClassLoader classLoader) throws Exception {
        Vector vIn = new Vector();
        vIn.add(user);
        return processRequest(vIn, server, port, worker, RETRIEVE_PROP_IDS_CMD, user.getClass().getCanonicalName(), classLoader);
    }
}

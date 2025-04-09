package visad.data.dods;

import java.lang.reflect.*;
import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.data.in.*;

/**
 * Provides support for accessing the DODS form of data from VisAD.
 *
 * <P>Instances are mutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class DODSForm extends Form implements FormFileInformer {

    /**
     * The suffix in the path-component of a URL specification that identifies
     * a dataset specification as being a DODS dataset specification.  It 
     * doesn't have a leading period.
     */
    public static final String SUFFIX = "dods";

    private static final String periodSuffix = "." + SUFFIX;

    private static final DODSForm instance = new DODSForm();

    private static final String sourceMessage = "DODS data-import capability is not available -- " + "probably because the DODS package wasn't available when " + "this package was compiled.  If you want DODS data-import " + "capability, then you'll have to first obtain the DODS " + "package (see " + "<http://www.unidata.ucar.edu/packages/dods/index.html>) and " + "then recompile this package.";

    private static final String contactMessage = ".  This exception should not have occurred.  Contact VisAD support.";

    /**
     * Constructs from nothing.
     */
    protected DODSForm() {
        super("DODS");
    }

    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static DODSForm dodsForm() {
        return instance;
    }

    /**
     * Throws an exception.
     *
     * @param id		An identifier.
     * @param data		A VisAD data object.
     * @param replace		Whether or not to replace an existing object.
     * @throws UnimplementedException	Always.
     */
    public void save(String id, Data data, boolean replace) throws BadFormException, IOException, RemoteException, VisADException {
        throw new UnimplementedException(getClass().getName() + ".save(String,Data,boolean): " + "Can't save data to a DODS server");
    }

    /**
     * Throws an exception.
     *
     * @param id		An identifier.
     * @param data		A VisAD data object.
     * @param replace		Whether or not to replace an existing object.
     * @throws BadFormException	Always.
     */
    public void add(String id, Data data, boolean replace) throws BadFormException {
        throw new BadFormException(getClass().getName() + ".add(String,Data,boolean): " + "Can't add data to a DODS server");
    }

    /**
     * Opens an existing DODS dataset.
     *
     * @param id		The URL for a DODS dataset.  The path component
     *				should have a {@link #SUFFIX} suffix.
     * @return			The VisAD data object corresponding to the 
     *				specified DODS dataset.  Might be 
     *				<code>null</code>.
     * @throws BadFormException	The DODS dataset is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl open(String id) throws BadFormException, IOException, VisADException {
        String header = getClass().getName() + ".open(String): ";
        DataImpl data;
        try {
            Class sourceClass = Class.forName(getClass().getPackage().getName() + ".DODSSource");
            DataInputStream source = (DataInputStream) sourceClass.getConstructor(new Class[0]).newInstance(new Object[0]);
            sourceClass.getMethod("open", new Class[] { String.class }).invoke(source, new Object[] { id });
            data = new Consolidator(new TimeFactorer(source)).readData();
        } catch (ClassNotFoundException e) {
            throw new VisADException(header + e + ".  " + sourceMessage);
        } catch (NoSuchMethodException e) {
            throw new VisADException(header + e + contactMessage);
        } catch (SecurityException e) {
            throw new VisADException(header + e + contactMessage);
        } catch (InstantiationException e) {
            throw new VisADException(header + e + contactMessage);
        } catch (IllegalAccessException e) {
            throw new VisADException(header + e + contactMessage);
        } catch (IllegalArgumentException e) {
            throw new VisADException(header + e + contactMessage);
        } catch (InvocationTargetException e) {
            throw new VisADException(e.getTargetException().getMessage());
        }
        return data;
    }

    /**
     * Opens an existing data object.
     *
     * @param url		The URL for a DODS dataset.  The path component
     *				should have a {@link #SUFFIX} suffix.
     * @return			The VisAD data object corresponding to the 
     *				DODS dataset.
     * @throws BadFormException	The DODS dataset is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl open(URL url) throws BadFormException, VisADException, IOException {
        return open(url.toString());
    }

    /**
     * Returns <code>null</code>.
     *
     * @param data		A VisAD data object.
     * @return			<code>null</code>.
     */
    public FormNode getForms(Data data) {
        return null;
    }

    /**
     * Indicates if a dataset specification is consistent with a DODS dataset
     * specification.
     *
     * @param spec		A dataset specification.  NB: Not a URL.
     * @return			<code>true</code> if and only if the dataset
     *				specification is consistent with a DODS dataset
     *				specification.
     */
    public boolean isThisType(String spec) {
        int i = spec.lastIndexOf('?');
        if (i != -1) spec = spec.substring(0, i);
        return spec.toLowerCase().endsWith(periodSuffix);
    }

    /**
     * Does nothing.  Because the initial block of data in a DODS dataset can't
     * be obtained from a DODS server, this routine does nothing and always
     * returns false.
     *
     * @param block		A block of data.
     * @return			<code>false</code> always.
     */
    public boolean isThisType(byte[] block) {
        return false;
    }

    /**
     * Returns the path-component suffixes that identifies a dataset
     * specification as being a DODS dataset specification.  The suffixes don't
     * have a leading period.  The returned array can be safely modified.
     *
     * @return			A freshly-allocated array with the relevant 
     *				suffixes.
     */
    public String[] getDefaultSuffixes() {
        return new String[] { SUFFIX };
    }
}

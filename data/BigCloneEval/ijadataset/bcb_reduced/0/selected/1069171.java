package gnu.java.rmi.activation;

import gnu.java.rmi.server.ActivatableServerRef;
import gnu.java.rmi.server.UnicastServer;
import java.lang.reflect.Constructor;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationException;
import java.rmi.activation.ActivationGroup;
import java.rmi.activation.ActivationGroupID;
import java.rmi.activation.ActivationID;
import java.rmi.activation.UnknownObjectException;

/**
 * The default activation group class. This activation group assumes that
 * all classes are accessible via current thread context class loader.
 * The remote class loading is not supported for security reasons. The 
 * activation always occurs in the current jre.
 * 
 * @author Audrius Meskauskas (audriusa@Bioinformatics.org)
 */
public class DefaultActivationGroup extends ActivationGroup {

    /**
   * Use the serialVersionUID for interoperability.
   */
    private static final long serialVersionUID = 1;

    /**
   * Used during the group creation (required constructor).
   */
    static final Class[] cConstructorTypes = new Class[] { ActivationID.class, MarshalledObject.class };

    /**
   * Create the new default activation group.
   * 
   * @param id the group activation id.
   * @param data may contain the group initialization data (unused and can be
   *          null)
   * @throws RemoteException if the super constructor does
   */
    public DefaultActivationGroup(ActivationGroupID id, MarshalledObject data) throws RemoteException {
        super(id);
    }

    /**
   * May be overridden and used as a hook. This method is called each time
   * the new object is instantiated.
   */
    public void activeObject(ActivationID id, Remote obj) throws ActivationException, UnknownObjectException, RemoteException {
    }

    /**
   * Create the new instance of the object, using the class name and location
   * information, stored in the passed descriptor. The method expects the object
   * class to have the two parameter constructor, the first parameter being the
   * {@link ActivationID} and the second the {@link MarshalledObject}.
   * 
   * @param id the object activation id
   * @param desc the activation descriptor, providing the information, necessary
   *          to create and activate the object
   * @return the marshalled object, containing the exported stub of the created
   *         object
   * @throws ActivationException if the activation fails due any reason
   */
    public MarshalledObject newInstance(ActivationID id, ActivationDesc desc) throws ActivationException, RemoteException {
        try {
            if (ActivationSystemTransient.debug) System.out.println("Instantiating " + desc.getClassName());
            Remote object;
            Class objectClass;
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            objectClass = loader.loadClass(desc.getClassName());
            Constructor constructor = objectClass.getConstructor(cConstructorTypes);
            object = (Remote) constructor.newInstance(new Object[] { id, desc.getData() });
            ActivatableServerRef ref = UnicastServer.getActivatableRef(id);
            Remote stub = ref.exportObject(object);
            MarshalledObject marsh = new MarshalledObject(stub);
            activeObject(id, marsh);
            activeObject(id, stub);
            return marsh;
        } catch (Exception e) {
            ActivationException acex = new ActivationException("Unable to activate " + desc.getClassName() + " from " + desc.getLocation(), e);
            throw acex;
        }
    }
}

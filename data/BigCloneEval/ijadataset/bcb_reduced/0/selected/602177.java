package distrit.server;

import distrit.*;
import java.rmi.*;
import java.rmi.server.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/** <p>This wrapper simplifies the process of making your own server.  It hides all the ugly
 * RMI calls and allows you to concentrate on implementing the main ITServer methods.
 * It takes an InteractiveTaskServer class through the command line, loads it, and does
 * the necesary RMI binding. </p>
 * <p>SingleServerWrapper offers the possibility of forcing RMI traffic to go through a
 * specified port.  This is NOT the port of the RMI Registry which is at 1099 by default.  RMI
 * uses another port to handle connections with clients.  This is the port which can be 
 * configured here. If ommited, the default Java behaviour is to pick a random port each time.
 * </p>
 * <p>A SingleServerWrapper can be constructed through the command line passing the 
 * class name of the InteractiveTaskServer to load followed by the name by which it should 
 * be bound in the RMI Registry followed by Strings to be passed to the 
 * constructor of this class inside an ArrayList.  A '-p' argument followed by an integer will
 * define the handling port for RMI to use.</p>
 * <p>Alternately, a SingleServerWrapper Object can be created using the constructors
 * directly and calling the bindServer method to bind it to the RMI Registry. </p.
 * @author  mmg20
 */
public class SingleServerWrapper implements InteractiveTaskServer {

    static final int DEFAULT_TRAFFIC_PORT = 0;

    protected InteractiveTaskServer inITS;

    protected String bindingName;

    protected int trafficPort = DEFAULT_TRAFFIC_PORT;

    /** Creates a new instance of SingleServerWrapper 
      * @param inITS InteractiveTaskServer object we wan't to bind using this wrapper.
     * @param bindingName Name under which to bind this server into the RMI Registry.
     */
    public SingleServerWrapper(InteractiveTaskServer inITS, String bindingName) throws RemoteException {
        this.inITS = inITS;
        this.bindingName = bindingName;
    }

    /** Creates a new instance of SingleServerWrapper 
      * @param inITS InteractiveTaskServer object we wan't to bind using this wrapper.
     * @param bindingName Name under which to bind this server into the RMI Registry.
     * @param trafficPort Port to use for all RMI traffic.  This is different to the RMI Registry port which is 1099 by default.
     */
    public SingleServerWrapper(InteractiveTaskServer inITS, String bindingName, int trafficPort) throws RemoteException {
        this(inITS, bindingName);
        this.trafficPort = trafficPort;
    }

    /**
     * @param args the command line arguments.  The first argument should be the class name of 
     * your interactive task server class to be wrapping around.  This class should either have a constructor
     * with no arguments or in the case that extra command line arguments have been supplied, a constructor
     * with a single paramater of type ArrayList which will include String objects representing the extra command line arguments.
     *.  The second argument should be
     * the RMI binding name you wish to use, which is the name clients will refer to this server by.
     * A '-p' argument followed by an integer will define the handling port for RMI to use.
     */
    public static void main(String[] args) {
        int trafficPort = DEFAULT_TRAFFIC_PORT;
        if (args.length < 2) {
            String narrator = "Syntax is 'SingleServerWrapper ServerClass ServerBindingName [-p Port] [Params]' where ";
            narrator += "ServerClass is the class name of your interactive task server class to ";
            narrator += "be wrapping around (must have no argument constructor).";
            narrator += "\nServerBindingName is the RMI binding name you wish ";
            narrator += "to use, which is the name clients will refer to this server by.";
            narrator += "\nPort is the port where all RMI traffic should go through.";
            narrator += "\nParams are extra command line arguments to supply to the ServerClass constructor.";
            narrator += "\nEg. java -Djava.rmi.server.codebase=http://139.184.166.27:2011/ -Djava.rmi.server.hostname=139.184.166.27 -Djava.security.policy=java.policy distrit.server.SingleServerWrapper package.MyITSServer KingKong bog jog -p 1098";
            narrator += "\nThe example will construct server class package.MyITSServer with an ArrayList containing Strings bog and jog.  It will then bind it as KingKong and use port 1098 for all RMI traffic.";
            System.out.println("" + narrator);
            System.exit(0);
        }
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            Class inITSClass = classLoader.loadClass(args[0]);
            ArrayList extraArgs = new ArrayList();
            for (int al = 2; al < args.length; al++) {
                if (args[al].equals("-p")) {
                    trafficPort = Integer.parseInt(args[++al]);
                } else {
                    extraArgs.add(args[al]);
                }
            }
            InteractiveTaskServer inITS;
            if (extraArgs.size() == 0) {
                inITS = (InteractiveTaskServer) inITSClass.newInstance();
            } else {
                Class[] parameterTypes = { extraArgs.getClass() };
                Constructor inITSConstructor = inITSClass.getConstructor(parameterTypes);
                Object[] parameters = { extraArgs };
                inITS = (InteractiveTaskServer) inITSConstructor.newInstance(parameters);
            }
            System.out.println("Succesfully loaded InteractiveTaskServer " + inITS);
            try {
                SingleServerWrapper ssw = new SingleServerWrapper(inITS, args[1], trafficPort);
                ssw.bindServer();
            } catch (RemoteException rem) {
                handleException(rem);
            } catch (java.net.MalformedURLException e) {
                handleException(e);
            } catch (Exception e) {
                System.out.println("HANDLING GENERIC EXCEPTION!!!! WHAT IS IT????");
                handleException(e);
            }
        } catch (ClassNotFoundException cnfe) {
            handleException(cnfe);
        } catch (InstantiationException ine) {
            handleException(ine);
        } catch (IllegalAccessException iae) {
            handleException(iae);
        } catch (NoSuchMethodException e) {
            handleException(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            handleException(e);
        }
    }

    protected static void handleException(Exception e) {
        System.err.println("Exception handled by SingleServerWrapper: " + e.getMessage());
        e.printStackTrace();
    }

    protected void bindServer() throws RemoteException, java.net.MalformedURLException, java.io.IOException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        RMISocketFactory.setSocketFactory(new FixedPortRMISocketFactory(trafficPort));
        String name = "//localhost";
        name += "/" + bindingName;
        UnicastRemoteObject.exportObject(this);
        Naming.rebind(name, this);
        System.out.println("InteractiveServer bound as " + bindingName);
    }

    /** Called by clients the first time they connect.
     * @param initialParameters Initial parameters from client local configuration.
     * The current client implementation, see
     * InteractiveTaskClient, provides the ip address of
     * the client and the command line paramaters used to
     * launch the client.  These are packaged in a Vector.
     * @return The ID of this client will from now on used for
     * interaction with the server.
     */
    public Object getID(Object initialParameters) throws RemoteException {
        return inITS.getID(initialParameters);
    }

    /** This will provide the client with the InteractiveTask
     * it should run.
     * @param id The ID provided by the getID method by which the
     * client will always refer to itself.
     * @return The task this client should run.  This could be
     * exactly the same as the one all other clients are
     * running or tailored to this particular client.
     */
    public InteractiveTask getTask(Object id) throws RemoteException {
        return inITS.getTask(id);
    }

    /** Called by clients when they wish to interact.
     * @param ID the client's ID
     * @param clientTaskOutput the outputs of the client obtained by task.get( null ) on the client task
     * @return whatever should be sent into the client through task.set( )
     */
    public Object interact(Object ID, Object clientTaskOutput) throws RemoteException {
        return inITS.interact(ID, clientTaskOutput);
    }
}

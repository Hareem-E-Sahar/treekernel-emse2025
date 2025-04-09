package cb.recommender.testers;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Properties;
import com.uplexis.idealize.base.exceptions.IdealizeConfigurationException;
import com.uplexis.idealize.base.exceptions.IdealizeUnavailableResourceException;
import com.uplexis.idealize.base.facade.RemoteFacade;
import com.uplexis.idealize.base.loader.IdealizeClassLoader;
import com.uplexis.idealize.base.loader.PropertiesLoader;
import com.uplexis.idealize.hotspots.instantiator.InstantiatorWorker;

/**
 * This class is responsible for reading the properties file and decide which
 * other classes will be instantiated. 
 *  
 * @author Alex Amorim Dutra
 */
public class Tester {

    private final String KEY_INSTANTIATOR = "instantiator";

    protected Properties classes = null;

    /**
	 * Create a new AbstractTester.
	 */
    public Tester(String class_properties_file) {
        this.classes = new PropertiesLoader().load(class_properties_file);
    }

    /**
	 * Create a new TesterBatch
	 * 
	 * @param classes
	 *            Properties file with classes to instantiate.
	 */
    public Tester(Properties classes) {
        this.classes = classes;
    }

    /**
	 * Instantiate the necessary classes to be used by the system.
	 * 
	 * @return IdealizeFacade with the facade to the whole system
	 * @throws IdealizeConfigurationException
	 *             thrown when the configuration file of either, hibernate or
	 *             classes contains errors.
	 * @throws IdealizeUnavailableResourceException
	 *             thrown when resources such as databases, file system or
	 *             network are unavailable.
	 * @throws RemoteException
	 * @throws NoSuchMethodException
	 */
    public final RemoteFacade instantiate() throws IdealizeConfigurationException, IdealizeUnavailableResourceException, RemoteException {
        IdealizeClassLoader loader = new IdealizeClassLoader();
        InstantiatorWorker worker = null;
        try {
            worker = (InstantiatorWorker) loader.loadClass(classes.getProperty(this.KEY_INSTANTIATOR)).getConstructor().newInstance();
        } catch (IllegalArgumentException e) {
            throw new IdealizeConfigurationException("Instantiator.instantiate: Could not load instantiator worker: " + classes.getProperty(this.KEY_INSTANTIATOR), e);
        } catch (SecurityException e) {
            throw new IdealizeConfigurationException("Instantiator.instantiate: Could not load instantiator worker: " + classes.getProperty(this.KEY_INSTANTIATOR), e);
        } catch (InstantiationException e) {
            throw new IdealizeConfigurationException("Instantiator.instantiate: Could not load instantiator worker: " + classes.getProperty(this.KEY_INSTANTIATOR), e);
        } catch (IllegalAccessException e) {
            throw new IdealizeConfigurationException("Instantiator.instantiate: Could not load instantiator worker: " + classes.getProperty(this.KEY_INSTANTIATOR), e);
        } catch (InvocationTargetException e) {
            throw new IdealizeConfigurationException("Instantiator.instantiate: Could not load instantiator worker: " + classes.getProperty(this.KEY_INSTANTIATOR), e);
        } catch (ClassNotFoundException e) {
            throw new IdealizeConfigurationException("Instantiator.instantiate: Could not load instantiator worker: " + classes.getProperty(this.KEY_INSTANTIATOR), e);
        } catch (NoSuchMethodException e) {
            throw new IdealizeConfigurationException("Instantiator.instantiate: Could not load instantiator worker: " + classes.getProperty(this.KEY_INSTANTIATOR), e);
        } catch (NullPointerException e) {
            throw new IdealizeConfigurationException("Instantiator.instantiate: Could not load instantiator worker: " + classes.getProperty(this.KEY_INSTANTIATOR), e);
        }
        return worker.doWork(classes, loader);
    }
}

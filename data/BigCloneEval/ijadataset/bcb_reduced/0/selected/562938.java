package cb.recommender.batch.instantiator;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import org.apache.mahout.cf.taste.common.TasteException;
import cb.recommender.base.constants.CBBatchConstants;
import cb.recommender.base.datamodel.DataModelFactory;
import cb.recommender.base.recommender.IdealizeCBAbstractRecommender;
import cb.recommender.base.recommender.factory.RecommenderFactory;
import cb.recommender.batch.storable.CBStorableRecommender;
import com.uplexis.idealize.base.cache.Cache;
import com.uplexis.idealize.base.exceptions.IdealizeConfigurationException;
import com.uplexis.idealize.base.exceptions.IdealizeCoreException;
import com.uplexis.idealize.base.exceptions.IdealizeInputException;
import com.uplexis.idealize.base.exceptions.IdealizeUnavailableResourceException;
import com.uplexis.idealize.base.facade.RemoteFacade;
import com.uplexis.idealize.base.facade.RemoteIdealizeRecommenderFacade;
import com.uplexis.idealize.base.facade.impl.IdealizeRecommenderFacade;
import com.uplexis.idealize.base.loader.IdealizeClassLoader;
import com.uplexis.idealize.base.loggers.IdealizeLogger;
import com.uplexis.idealize.hotspots.controller.recommender.Controller;
import com.uplexis.idealize.hotspots.datamodel.IdealizeDataModel;
import com.uplexis.idealize.hotspots.input.interpreter.InputInterpreter;
import com.uplexis.idealize.hotspots.instantiator.AbstractInstantiatorWorker;
import com.uplexis.idealize.hotspots.output.RecommendationSerializer;
import com.uplexis.idealize.hotspots.processor.recommender.BatchProcessor;
import com.uplexis.idealize.hotspots.recommender.IdealizeRecommender;

/**
 * Loads the necessary components for use in this sector (batch), for
 * recommendations based on content. Puts the values configured on Archiving of
 * properties for components and for the values of the class constants. After
 * loading the components and the constants class instantiates the facade to be
 * used
 * 
 * @author Alex Amorim Dutra
 * 
 */
public class CBBatchInstantiatorWorker extends AbstractInstantiatorWorker {

    private final String canonicalName = this.getClass().getCanonicalName();

    protected final String KEY_ALLOW_REMOTE_COMPONENTS = "allow_remote_components";

    /**
	 * configures the parameters of the RMI. This is necessary for the remote
	 * objects can be accessed according to the time set in the parameters.
	 */
    private void setTimeOutRmi() {
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "1209600000");
        System.setProperty("sun.rmi.transport.tcp.readTimeout", "1209600000");
        System.setProperty("sun.rmi.transport.connectionTimeout", "1209600000");
    }

    @Override
    public RemoteFacade doWork(Properties classes, IdealizeClassLoader loader) throws IdealizeConfigurationException, IdealizeUnavailableResourceException, RemoteException {
        boolean useLogging = this.loadBoolean(classes, this.KEY_USE_LOGGING);
        if (useLogging) {
            String warnPath = classes.getProperty(this.KEY_LOG_WARN_PATH);
            String errorPath = classes.getProperty(this.KEY_LOG_ERROR_PATH);
            String fatalPath = classes.getProperty(this.KEY_LOG_FATAL_PATH);
            IdealizeLogger.init(useLogging, warnPath, errorPath, fatalPath);
        } else IdealizeLogger.init(useLogging, null, null, null);
        this.setTimeOutRmi();
        CBBatchConstants.FIELD_SEPARATOR_FOR_NETWORK = classes.getProperty(this.KEY_NETWORK_SEPARATOR);
        CBBatchConstants.DATA_STORAGE_PATH = classes.getProperty(this.KEY_DATA_STORAGE_PATH);
        CBBatchConstants.ALLOW_REMOTE_COMPONENTS = Boolean.parseBoolean(classes.getProperty(this.KEY_ALLOW_REMOTE_COMPONENTS));
        CBBatchConstants.CB_CONFIGURATION_FILE = classes.getProperty(this.KEY_CB_CONFIGURATION_FILE);
        boolean allowedSeparator = false;
        for (int i = CBBatchConstants.ALLOWED_SEPARATORS.length; --i >= 0; ) if (CBBatchConstants.FIELD_SEPARATOR_FOR_NETWORK.equals(CBBatchConstants.ALLOWED_SEPARATORS[i])) {
            allowedSeparator = true;
            break;
        }
        if (!allowedSeparator) throw new IdealizeConfigurationException(this.canonicalName + ".instantiate(): network separator not allowed: " + CBBatchConstants.FIELD_SEPARATOR_FOR_NETWORK);
        CBBatchConstants.USING_META_RECOMMENDER = Boolean.getBoolean(classes.getProperty(this.KEY_USING_META_RECOMMENDER));
        CBBatchConstants.STATIC_RECOMMENDER = this.loadInteger(classes, this.canonicalName + ": invalid static recommender: ", this.KEY_STATIC_RECOMMENDER);
        IdealizeDataModel dataModel = null;
        BatchProcessor batchProcessor = null;
        IdealizeRecommender recommender = null;
        Controller controller = this.loadController(classes, loader);
        boolean cache = false;
        if (dataModel == null) {
            dataModel = DataModelFactory.createDataModel(Integer.parseInt(classes.getProperty(this.KEY_DATA_MODEL)), cache, CBBatchConstants.DATA_STORAGE_PATH);
        }
        try {
            recommender = (IdealizeCBAbstractRecommender) RecommenderFactory.getRecommenderBuilder(CBBatchConstants.STATIC_RECOMMENDER, dataModel);
        } catch (TasteException e) {
            try {
                throw new IdealizeCoreException(this.canonicalName + ".doWok(): could not create recommender: " + e.getMessage(), e);
            } catch (IdealizeCoreException e1) {
                e1.printStackTrace();
            }
        }
        CBStorableRecommender data = new CBStorableRecommender((IdealizeCBAbstractRecommender) recommender);
        Cache.getInstance().setData(data, false);
        try {
            if (batchProcessor == null) {
                batchProcessor = this.loadBatchProcessor(classes, loader);
                controller.setBatchProcessor(batchProcessor);
                batchProcessor.process(true);
            } else batchProcessor.process(false);
        } catch (IdealizeInputException e) {
            throw new IdealizeConfigurationException(this.canonicalName + ".doWork: problems with input for batch processor: " + e.getMessage(), e);
        } catch (IdealizeCoreException e) {
            throw new IdealizeConfigurationException(this.canonicalName + ".doWork: problems while executing batch processing: " + e.getMessage(), e);
        } catch (RemoteException e) {
            throw new IdealizeConfigurationException(this.canonicalName + ".doWork: problems while executing batch processing: " + e.getMessage(), e);
        }
        InputInterpreter interpreter = this.loadInputInterpreter(classes, loader, CBBatchConstants.FIELD_SEPARATOR_FOR_NETWORK);
        RecommendationSerializer serializer = this.loadRecommendationSerializer(classes, loader);
        InputInterpreter feedbackInterpreter = this.loadFeedbackInterpreter(classes, loader, CBBatchConstants.FIELD_SEPARATOR_FOR_NETWORK);
        IdealizeRecommenderFacade facade = new IdealizeRecommenderFacade();
        facade.setController(controller);
        facade.setInputInterpreter(interpreter);
        facade.setSerializer(serializer);
        facade.setFeedbackInputInterpreter(feedbackInterpreter);
        if (CBBatchConstants.ALLOW_REMOTE_COMPONENTS) this.registerRemoteFacade(facade);
        return facade;
    }

    /**
	 * Makes the facade remotely available
	 * 
	 * @param remoteFacade
	 *            RemoteIdealizeRecommenderFacade instance
	 * @throws IdealizeUnavailableResourceException
	 *             thrown if facade could not be made remotely available
	 */
    private final void registerRemoteFacade(RemoteIdealizeRecommenderFacade remoteFacade) throws IdealizeUnavailableResourceException {
        try {
            RemoteIdealizeRecommenderFacade stub;
            stub = (RemoteIdealizeRecommenderFacade) UnicastRemoteObject.exportObject(remoteFacade, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("RemoteBatchFacadeCB", stub);
        } catch (RemoteException e) {
            throw new IdealizeUnavailableResourceException(remoteFacade.getClass().getCanonicalName() + ": could not make facade remotely available: " + e.getMessage(), e);
        }
    }

    /**
	 * Loads the specified InputInterpreter
	 * 
	 * @param classes
	 *            Properties file with classes to be loaded
	 * @param loader
	 *            IdealizeClassLoader with class loader implementation
	 * @param separatorForNetwork
	 *            separator fields inut facade
	 * @return InputInterpreter instance
	 * @throws IdealizeConfigurationException
	 *             thrown if problems of any nature occur
	 */
    public final InputInterpreter loadInputInterpreter(Properties classes, IdealizeClassLoader loader, String separatorForNetwork) throws IdealizeConfigurationException {
        InputInterpreter interpreter = null;
        try {
            interpreter = (InputInterpreter) loader.loadClass(classes.getProperty(this.KEY_INTERPRETER)).getConstructors()[0].newInstance(separatorForNetwork);
        } catch (IllegalArgumentException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadInputInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_INTERPRETER), e);
        } catch (SecurityException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadInputInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_INTERPRETER), e);
        } catch (InstantiationException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadInputInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_INTERPRETER), e);
        } catch (IllegalAccessException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadInputInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_INTERPRETER), e);
        } catch (InvocationTargetException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadInputInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_INTERPRETER), e);
        } catch (ClassNotFoundException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadInputInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_INTERPRETER), e);
        }
        return interpreter;
    }

    /**
	 * Loads the specified InputInterpreter for feedback
	 * 
	 * @param classes
	 *            Properties file with classes to be loaded
	 * @param loader
	 *            IdealizeClassLoader with class loader implementation
	 * @param separatorForNetwork
	 *            separator fields input facade
	 * @return InputInterpreter instance
	 * @throws IdealizeConfigurationException
	 *             thrown if problems of any nature occur
	 */
    public final InputInterpreter loadFeedbackInterpreter(Properties classes, IdealizeClassLoader loader, String separatorForNetwork) throws IdealizeConfigurationException {
        InputInterpreter feedbackInterpreter;
        try {
            feedbackInterpreter = (InputInterpreter) loader.loadClass(classes.getProperty(this.KEY_FEEDBACK_INTERPRETER)).getConstructors()[0].newInstance(separatorForNetwork);
        } catch (IllegalArgumentException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadFeedbackInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_FEEDBACK_INTERPRETER), e);
        } catch (SecurityException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadFeedbackInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_FEEDBACK_INTERPRETER), e);
        } catch (InstantiationException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadFeedbackInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_FEEDBACK_INTERPRETER), e);
        } catch (IllegalAccessException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadFeedbackInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_FEEDBACK_INTERPRETER), e);
        } catch (InvocationTargetException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadFeedbackInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_FEEDBACK_INTERPRETER), e);
        } catch (ClassNotFoundException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadFeedbackInterpreter: Could not load input interpreter: " + classes.getProperty(this.KEY_FEEDBACK_INTERPRETER), e);
        }
        return feedbackInterpreter;
    }
}

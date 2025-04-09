package cf.e_commerce.batch.instantiator;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import cf.e_commerce.base.constants.Constants;
import cf.e_commerce.base.datamodel.NetflixCFDataModel;
import cf.e_commerce.base.datamodel.loader.RatingsStrategyInMemory;
import cf.e_commerce.batch.storable.CFStorableDataModel;
import cf.e_commerce.batch.storable.CFStorablePreferences;
import cf.e_commerce.batch.storable.CFStorableRecommender;
import com.uplexis.idealize.base.cache.Cache;
import com.uplexis.idealize.base.cache.CacheObserver;
import com.uplexis.idealize.base.datamodel.DataModelStrategyContext;
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
import com.uplexis.idealize.hotspots.processor.RemoteBatchProcessor;
import com.uplexis.idealize.hotspots.processor.recommender.BatchProcessor;
import com.uplexis.idealize.hotspots.recommender.IdealizeCFAbstractRecommender;
import com.uplexis.idealize.hotspots.storable.BaseStorable;

/**
 * Performs the instantiation tasks for the batch sector.
 * 
 * @author Alex Amorim Dutra
 * 
 */
public class CFBatchInstantiatorUseThreads extends AbstractInstantiatorWorker {

    private final String canonicalName = this.getClass().getCanonicalName();

    private final String KEY_RECOMMENDER_RECREATION_RATE = "recommender_recreation_rate";

    private final String KEY_NUMBER_THREADS = "number_threads";

    private final String KEY_ALLOW_REMOTE_COMPONENTS = "allow_remote_components";

    private final String KEY_COMPLETE_MOST_POPULAR = "complete_most_popular";

    private final String KEY_RECOMMENDER_AUX = "recommender_aux";

    /**
	 * Loads all common framework components.
	 */
    @SuppressWarnings("unchecked")
    @Override
    public RemoteFacade doWork(Properties classes, IdealizeClassLoader loader) throws IdealizeConfigurationException, IdealizeUnavailableResourceException {
        boolean useLogging = this.loadBoolean(classes, this.KEY_USE_LOGGING);
        if (useLogging) {
            String warnPath = classes.getProperty(this.KEY_LOG_WARN_PATH);
            String errorPath = classes.getProperty(this.KEY_LOG_ERROR_PATH);
            String fatalPath = classes.getProperty(this.KEY_LOG_FATAL_PATH);
            IdealizeLogger.init(useLogging, warnPath, errorPath, fatalPath);
        } else IdealizeLogger.init(useLogging, null, null, null);
        Constants.NUMBERS_THREADS = this.loadInteger(classes, "Could not load number threads", this.KEY_NUMBER_THREADS);
        Constants.FIELD_SEPARATOR_FOR_NETWORK = classes.getProperty(this.KEY_NETWORK_SEPARATOR);
        boolean allowedSeparator = false;
        for (int i = Constants.ALLOWED_SEPARATORS.length; --i >= 0; ) if (Constants.FIELD_SEPARATOR_FOR_NETWORK.equals(Constants.ALLOWED_SEPARATORS[i])) {
            allowedSeparator = true;
            break;
        }
        Constants.COMPLETE_MOST_POPULAR = Boolean.parseBoolean(classes.getProperty(this.KEY_COMPLETE_MOST_POPULAR));
        Constants.RECOMMENDER_RECREATION_RATE = this.loadInteger(classes, "Could not load recommender recreation rate", this.KEY_RECOMMENDER_RECREATION_RATE);
        if (!allowedSeparator) throw new IdealizeConfigurationException("Instantiator: instantiate(): network separator not allowed: " + Constants.FIELD_SEPARATOR_FOR_NETWORK);
        Constants.RECOMMENDER_AUX = this.loadInteger(classes, ": invalid aux recommender: ", this.KEY_RECOMMENDER_AUX);
        Constants.STATIC_RECOMMENDER = this.loadInteger(classes, ": invalid static recommender: ", this.KEY_STATIC_RECOMMENDER);
        Constants.MAX_USERS = this.loadInteger(classes, ": invalid number of users to be loaded: ", this.KEY_MAX_USERS);
        Constants.CACHED_RECOMMENDATIONS = this.loadInteger(classes, ": invalid cached recommendations amount: ", this.KEY_CACHED_RECOMMENDATIONS);
        Constants.ALLOW_REMOTE_COMPONENTS = Boolean.parseBoolean(classes.getProperty(this.KEY_ALLOW_REMOTE_COMPONENTS));
        IdealizeDataModel dataModel = null;
        BatchProcessor batchProcessor = null;
        Controller controller = this.loadController(classes, loader);
        boolean cache = Boolean.parseBoolean(classes.getProperty(this.KEY_DATA_MODEL_CACHE_USAGE));
        if (cache) {
            Constants.SERIALIZE_RATE = this.loadInteger(classes, ": invalid serialize rate: ", this.KEY_SERIALIZE_RATE);
            if (Constants.SERIALIZE_RATE <= 0) throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ": serialize rate out of bounds: " + Constants.SERIALIZE_RATE);
        }
        if (CacheObserver.getInstance().getCachedClass() != null) {
            BaseStorable storable = this.loadBaseStorable(CacheObserver.getInstance().getCachedClass(), loader);
            storable.restore();
            Cache.getInstance().setData(storable, false);
            controller.prospectData(storable);
            if (storable instanceof CFStorableDataModel) dataModel = (IdealizeDataModel) controller.getData(); else if (storable instanceof CFStorablePreferences) dataModel = new DataModelStrategyContext(new RatingsStrategyInMemory(true)).executeStrategy(); else if (storable instanceof CFStorableRecommender) {
                dataModel = (IdealizeDataModel) ((IdealizeCFAbstractRecommender) controller.getDataList().get(0)).getDataModel();
                Cache.getInstance().setData(new CFStorableRecommender((IdealizeCFAbstractRecommender) controller.getDataList().get(0), (IdealizeCFAbstractRecommender) controller.getDataList().get(1)), false);
                batchProcessor = this.loadBatchProcessor(classes, loader);
                controller.setBatchProcessor(batchProcessor);
            }
        }
        if (dataModel == null) {
            dataModel = new DataModelStrategyContext(new RatingsStrategyInMemory(true)).executeStrategy();
            try {
                Cache.getInstance().setData(new CFStorableDataModel((NetflixCFDataModel) dataModel), true);
            } catch (IdealizeInputException e) {
                throw new IdealizeConfigurationException(this.canonicalName + ".doWork: could not cache data model: " + e.getMessage(), e);
            }
        } else try {
            Cache.getInstance().setData(new CFStorableDataModel((NetflixCFDataModel) dataModel), false);
        } catch (IdealizeInputException e1) {
            throw new IdealizeConfigurationException(this.canonicalName + ".doWork: could not cache data model: " + e1.getMessage(), e1);
        }
        try {
            if (batchProcessor == null) {
                batchProcessor = this.loadBatchProcessor(classes, loader);
                controller.setBatchProcessor(batchProcessor);
                batchProcessor.process(true);
            } else batchProcessor.process(false);
        } catch (IdealizeInputException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".doWork: problems with input for batch processor: " + e.getMessage(), e);
        } catch (IdealizeCoreException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".doWork: problems while executing batch processing: " + e.getMessage(), e);
        } catch (RemoteException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".doWork: problems while executing batch processing: " + e.getMessage(), e);
        }
        if (Constants.ALLOW_REMOTE_COMPONENTS) this.registerRemoteBatchProcessor(batchProcessor);
        InputInterpreter interpreter = this.loadInputInterpreter(classes, loader);
        RecommendationSerializer serializer = this.loadRecommendationSerializer(classes, loader);
        InputInterpreter feedbackInterpreter = this.loadFeedbackInterpreter(classes, loader);
        IdealizeRecommenderFacade facade = new IdealizeRecommenderFacade();
        facade.setController(controller);
        facade.setInputInterpreter(interpreter);
        facade.setSerializer(serializer);
        facade.setFeedbackInputInterpreter(feedbackInterpreter);
        if (Constants.ALLOW_REMOTE_COMPONENTS) this.registerRemoteFacade(facade);
        return facade;
    }

    /**
	 * Registers batch processor for remote access
	 * 
	 * @param remoteProcessor
	 *            RemoteBatchProcessor instance to be registered
	 * @throws IdealizeUnavailableResourceException
	 *             throw if processor cannot be registered
	 */
    private final void registerRemoteBatchProcessor(RemoteBatchProcessor remoteProcessor) throws IdealizeUnavailableResourceException {
        try {
            RemoteBatchProcessor stub = (RemoteBatchProcessor) UnicastRemoteObject.exportObject(remoteProcessor, 0);
            Registry registry = LocateRegistry.getRegistry(0);
            registry.rebind(RemoteBatchProcessor.class.getSimpleName(), stub);
        } catch (RemoteException e) {
            throw new IdealizeUnavailableResourceException(remoteProcessor.getClass().getCanonicalName() + ": register batch processor for remote access: " + e.getMessage(), e);
        }
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
            registry.rebind(RemoteFacade.class.getSimpleName(), stub);
        } catch (RemoteException e) {
            throw new IdealizeUnavailableResourceException(remoteFacade.getClass().getCanonicalName() + ": could not make facade remotely available: " + e.getMessage(), e);
        }
    }

    /**
	 * Loads the specified BaseStorable
	 * 
	 * @param classes
	 *            Properties file with classes to be loaded
	 * @param loader
	 *            IdealizeClassLoader with class loader implementation
	 * @return BaseStorable instance
	 * @throws IdealizeConfigurationException
	 *             thrown if problems of any nature occur
	 */
    @SuppressWarnings("unchecked")
    private final BaseStorable loadBaseStorable(String className, IdealizeClassLoader loader) throws IdealizeConfigurationException {
        BaseStorable storable = null;
        try {
            storable = (BaseStorable) loader.loadClass(className).getConstructor().newInstance();
        } catch (IllegalArgumentException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadBaseStorable: Could not load storable: " + className, e);
        } catch (SecurityException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadBaseStorable: Could not load storable: " + className, e);
        } catch (InstantiationException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadBaseStorable: Could not load storable: " + className, e);
        } catch (IllegalAccessException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadBaseStorable: Could not load storable: " + className, e);
        } catch (InvocationTargetException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadBaseStorable: Could not load storable: " + className, e);
        } catch (NoSuchMethodException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadBaseStorable: Could not load storable: " + className, e);
        } catch (ClassNotFoundException e) {
            throw new IdealizeConfigurationException(this.getClass().getCanonicalName() + ".loadBaseStorable: Could not load storable: " + className, e);
        }
        return storable;
    }
}

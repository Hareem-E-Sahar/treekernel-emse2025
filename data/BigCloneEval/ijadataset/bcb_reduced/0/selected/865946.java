package org.n52.sos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.servlet.UnavailableException;
import org.apache.log4j.Logger;
import org.n52.sos.decode.IHttpGetRequestDecoder;
import org.n52.sos.decode.IHttpPostRequestDecoder;
import org.n52.sos.ds.IConfigDAO;
import org.n52.sos.ds.IDAOFactory;
import org.n52.sos.encode.IGMLEncoder;
import org.n52.sos.encode.IOMEncoder;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport.ExceptionLevel;

/**
 * Singleton class reads the configFile and builds the RequestOperator and DAO; configures the logger.
 * 
 * @author Christoph Stasch
 * 
 */
public class SosConfigurator {

    /** common SOS properties from configFile */
    private Properties props;

    /** properties for DAO implementation */
    private Properties daoProps;

    /** base path for configuration files */
    private String basepath;

    /**
     * Implementation of the DAOFactory, used to build the DAOs for the request listeners
     */
    private IDAOFactory factory;

    /**
     * Implementation of IOMEncoder; class is loaded from conf.sos.omEncoder property defined in config.properties
     */
    private IOMEncoder omEncoder;

    /**
     * Implementation of IGMLEncoder; class is loaded from conf.sos.gmlEncoder property defined in config.properties
     */
    private IGMLEncoder gmlEncoder;

    /** skeleton file for capabilities document */
    private File capabilitiesSkeleton;

    /** directory of sensor descriptions in SensorML format */
    private File sensorDir;

    /** propertyname of listeners */
    private final String LISTENERS = "LISTENERS";

    /** propertyname of sekletonfile */
    private final String SKELETON_FILE = "SKELETONFILE";

    /** propertyname of sensor directory */
    private final String SENSOR_DIR = "SENSORDIR";

    /** propertyname of lease for getResulte operation */
    private final String LEASE = "LEASE";

    /** propertyname of supportsQuality */
    private final String SUPPORTSQUALITY = "SUPPORTSQUALITY";

    /** propertyname of supportsQuality */
    private final String EASTINGFIRST = "EASTINGFIRST";

    private final String FOI_ENCODED_IN_OBSERVATION = "FOI_ENCODED_IN_OBSERVATION";

    /** propertyname for decimal separator */
    private final String DECIMAL_SEPARATOR = "DECIMALSEPARATOR";

    /** propertyname of logging directory */
    private final String TOKEN_SEPERATOR = "TOKENSEPERATOR";

    /** propertyname of logging directory */
    private final String TUPLE_SEPERATOR = "TUPLESEPERATOR";

    /** propertyname of logging directory */
    private final String NO_DATA_VALUE = "NODATAVALUE";

    /** propertyname of DAOFACTORY property */
    private final String DAO_FACTORY = "DAOFactory";

    /** propertyname of GMLENCODER property*/
    private final String GMLENCODER = "GMLENCODER";

    /** propertyname of OMENCODER property */
    private final String OMENCODER = "OMENCODER";

    /** propertyname of GETREQUESTDECODER property*/
    private final String GETREQUESTDECODER = "GETREQUESTDECODER";

    /** propertyname of POSTREQUESTDECODER property */
    private final String POSTREQUESTDECODER = "POSTREQUESTDECODER";

    /** propertyname of logging directory */
    private final String GML_DATE_FORMAT = "GMLDATEFORMAT";

    /** propertyname of character encoding */
    private final String CHARACTER_ENCODING = "CHARACTERENCODING";

    /** logger */
    private static Logger log = Logger.getLogger(SosConfigurator.class);

    /** token seperator for result element */
    private String tokenSeperator;

    /** tuple seperator for result element */
    private String tupleSeperator;

    /** decimal separator for result element */
    private String decimalSeparator;

    /** tuple seperator for result element */
    private String noDataValue;

    /** character encoding for responses */
    private String characterEncoding;

    /** lease for getResult template in minutes */
    private int lease;

    /** boolean indicates, whether SOS supports quality information in observations */
    private boolean supportsQuality = true;

    /** boolean indicates the order of x and y components of coordinates */
    private boolean eastingFirst = false;

    /**
     * boolean indicates, whether SOS encodes the complete FOI-instance within the Observation instance or
     * just the FOI id
     */
    private boolean foiEncodedInObservation = true;

    /** date format of gml */
    private String gmlDateFormat;

    /** instance attribut, due to the singleton pattern */
    private static SosConfigurator instance = null;

    /** decoder for decoding httpPost requests */
    private IHttpPostRequestDecoder httpPostDecoder;

    /** decoder for decoding httpGet requests */
    private IHttpGetRequestDecoder httpGetDecoder;

    /**
     * private constructor due to the singelton pattern.
     * 
     * @param configis
     *        InputStream of the configfile
     * @param dbconfigis
     *        InputStream of the dbconfigfile
     * @throws UnavailableException
     *         if the configFile could not be loaded
     * @throws OwsExceptionReport
     *         if the
     */
    private SosConfigurator(InputStream configis, InputStream daoConfigIS, String basepath) throws OwsExceptionReport, UnavailableException {
        this.basepath = basepath;
        try {
            props = loadProperties(configis);
            daoProps = loadProperties(daoConfigIS);
            log.info("\n******\nConfig File loaded successfully!\n******\n");
        } catch (IOException ioe) {
            log.fatal("error while loading config file", ioe);
            throw new UnavailableException(ioe.getMessage());
        }
    }

    /**
     * Initialize this class. Since this initialization is not done in the 
     * constructor, dependent classes can use the SosConfigurator already when 
     * called from here. 
     */
    private void initialize() throws OwsExceptionReport {
        String leaseString = props.getProperty(LEASE);
        if (leaseString == null || leaseString.equals("")) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(ExceptionCode.NoApplicableCode, null, "No lease is defined in the config file! Please set the lease property on an integer value!");
            log.fatal("No lease is defined in the config file! Please set the lease property on an integer value!", se);
            throw se;
        }
        if (leaseString != null) {
            this.lease = new Integer(leaseString).intValue();
        } else {
            this.lease = 600;
        }
        String characterEnodingString = props.getProperty(CHARACTER_ENCODING);
        if (characterEnodingString == null) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(ExceptionCode.NoApplicableCode, null, "No characterEnoding is defined in the config file!!");
            log.fatal("No characterEnoding is defined in the config file!!");
            throw se;
        }
        this.characterEncoding = characterEnodingString;
        String supportsQualityString = props.getProperty(SUPPORTSQUALITY);
        if (supportsQualityString == null || (!supportsQualityString.equalsIgnoreCase("true") && !supportsQualityString.equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(ExceptionCode.NoApplicableCode, null, "No supportsQuality is defined in the config file or the value :" + supportsQualityString + " is wrong!");
            log.fatal("No supportsQuality is defined in the config file or the value '" + supportsQualityString + "' is wrong!", se);
            throw se;
        }
        this.supportsQuality = Boolean.parseBoolean(supportsQualityString);
        String eastingFirstString = props.getProperty(EASTINGFIRST);
        if (eastingFirstString == null || (!eastingFirstString.equalsIgnoreCase("true") && !eastingFirstString.equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            String excMsg = "No eastingFirst is defined in the config file or the value '" + supportsQualityString + "' is wrong!";
            se.addCodedException(ExceptionCode.NoApplicableCode, null, excMsg);
            log.fatal(excMsg, se);
            throw se;
        }
        this.eastingFirst = Boolean.parseBoolean(eastingFirstString);
        String foiEncodedInObservationString = props.getProperty(FOI_ENCODED_IN_OBSERVATION);
        if (foiEncodedInObservationString == null || (!foiEncodedInObservationString.equalsIgnoreCase("true") && !foiEncodedInObservationString.equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(ExceptionCode.NoApplicableCode, null, "No 'foiEncodedInObservation' is defined in the config file or the value '" + supportsQualityString + "' is wrong!");
            log.fatal("No eastingFirst is defined in the config file or the value :" + eastingFirstString + " is wrong!", se);
            throw se;
        }
        this.foiEncodedInObservation = Boolean.parseBoolean(foiEncodedInObservationString);
        String skelFile = props.getProperty(SKELETON_FILE);
        this.capabilitiesSkeleton = new File(skelFile);
        if (!this.capabilitiesSkeleton.exists()) {
            skelFile = this.getBasePath() + props.getProperty(SKELETON_FILE);
            this.capabilitiesSkeleton = new File(skelFile);
        }
        log.info("\n******\nCapabilities Skeleton File loaded successfully from :" + skelFile + " !\n******\n");
        this.sensorDir = new File(props.getProperty(SENSOR_DIR));
        if (!this.sensorDir.exists()) {
            this.sensorDir = new File(this.getBasePath() + props.getProperty(SENSOR_DIR));
        }
        log.info("\n******\nSensor directory file created successfully!\n******\n");
        setLease(new Integer(props.getProperty(LEASE)).intValue());
        setTokenSeperator(props.getProperty(TOKEN_SEPERATOR));
        setTupleSeperator(props.getProperty(TUPLE_SEPERATOR));
        setDecimalSeparator(props.getProperty(DECIMAL_SEPARATOR));
        setGmlDateFormat(props.getProperty(GML_DATE_FORMAT));
        setNoDataValue(props.getProperty(NO_DATA_VALUE));
        log.info("\n******\n dssos.config file loaded successfully!!\n******\n");
        initializeDAOFactory(daoProps);
        initializeOMEncoder(props);
        initializeGMLEncoder(props);
        initializeHttpGetRequestDecoder(props);
        initializeHttpPostRequestDecoder(props);
        intializeCapabilitiesCache();
    }

    /**
     * Eventually cleanup everything created by the constructor
     */
    public void cleanup() {
        if (factory != null) {
            factory.cleanup();
        }
    }

    /**
     * @return Returns an instance of the SosConfigurator. This method is used to implement the singelton
     *         pattern
     * 
     * @throws OwsExceptionReport
     * 
     * if no DAOFactory Implementation class is defined in the ConfigFile or if one or more RequestListeners,
     * defined in the configFile, could not be loaded
     * 
     * @throws UnavailableException
     *         if the configFile could not be loaded
     * @throws OwsExceptionReport
     * 
     */
    public static synchronized SosConfigurator getInstance(InputStream configis, InputStream dbconfigis, String basepath) throws UnavailableException, OwsExceptionReport {
        if (instance == null) {
            instance = new SosConfigurator(configis, dbconfigis, basepath);
            instance.initialize();
        }
        return instance;
    }

    /**
     * @return Returns the instance of the SosConfigurator. Null will be returned if the parameterized
     *         getInstance method was not invoked before. Usually this will be done in the SOS.
     */
    public static synchronized SosConfigurator getInstance() {
        return instance;
    }

    /**
     * reads the requestListeners from the configFile and returns a RequestOperator containing the
     * requestListeners
     * 
     * @return RequestOperators with requestListeners
     * @throws OwsExceptionReport
     *         if initialization of a RequestListener failed
     */
    @SuppressWarnings("unchecked")
    public RequestOperator buildRequestOperator() throws OwsExceptionReport {
        RequestOperator ro = new RequestOperator();
        ArrayList<String> listeners = loadListeners();
        Iterator<String> iter = listeners.iterator();
        while (iter.hasNext()) {
            String classname = iter.next();
            try {
                Class listenerClass = Class.forName(classname);
                Class[] constrArgs = {};
                Object[] args = {};
                Constructor<ISosRequestListener> constructor = listenerClass.getConstructor(constrArgs);
                ro.addRequestListener(constructor.newInstance(args));
            } catch (ClassNotFoundException cnfe) {
                log.fatal("Error while loading RequestListeners, required class could not be loaded: " + cnfe.toString());
                throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
            } catch (SecurityException se) {
                log.fatal("Error while loading RequestListeners");
                throw new OwsExceptionReport(se.getMessage(), se.getCause());
            } catch (NoSuchMethodException nsme) {
                log.fatal("Error while loading RequestListeners," + " no required constructor available: " + nsme.toString());
                throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
            } catch (IllegalArgumentException iae) {
                log.fatal("Error while loading RequestListeners, " + "parameters for the constructor are illegal: " + iae.toString());
                throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
            } catch (InstantiationException ie) {
                log.fatal("The instatiation of a RequestListener failed: " + ie.toString());
                throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
            } catch (IllegalAccessException iace) {
                log.fatal("The instatiation of a RequestListener failed: " + iace.toString());
                throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
            } catch (InvocationTargetException ite) {
                log.fatal("The instatiation of a RequestListener failed: " + ite.toString());
                throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
            }
        }
        log.info("\n******\nRequestOperator built successfully!\n******\n");
        return ro;
    }

    /**
     * intializes the CapabilitiesCache
     * 
     * @throws OwsExceptionReport
     *         if initializing the CapabilitiesCache failed
     */
    private void intializeCapabilitiesCache() throws OwsExceptionReport {
        IConfigDAO configDAO = factory.getConfigDAO();
        CapabilitiesCache.getInstance(configDAO);
        log.info("\n******\nRequestOperator built successfully!\n******\n");
    }

    /**
     * loads and instantiates the DAOFactory class
     * 
     * @throws OwsExceptionReport
     *         if initializing the DAOFactory Implementation class failed
     */
    @SuppressWarnings("unchecked")
    private void initializeDAOFactory(Properties daoProps) throws OwsExceptionReport {
        try {
            String daoName = props.getProperty(DAO_FACTORY);
            if (daoName == null) {
                log.fatal("No DAOFactory Implementation is set in the configFile!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode, "SosConfigurator.initializeDAOFactory()", "No DAOFactory Implementation is set in the configFile!");
                throw se;
            }
            Class daoFactoryClass = Class.forName(daoName);
            Class[] constrArgs = { Properties.class };
            Object[] args = { daoProps };
            Constructor<IDAOFactory> constructor = daoFactoryClass.getConstructor(constrArgs);
            this.factory = constructor.newInstance(args);
            log.info("\n******\n" + daoName + " loaded successfully!\n******\n");
        } catch (ClassNotFoundException cnfe) {
            log.fatal("Error while loading DAOFactory, required class could not be loaded: " + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        } catch (SecurityException se) {
            log.fatal("Error while loading DAOFactory: " + se.toString());
            throw new OwsExceptionReport(se.getMessage(), se.getCause());
        } catch (NoSuchMethodException nsme) {
            log.fatal("Error while loading DAOFactory, no required constructor available: " + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        } catch (IllegalArgumentException iae) {
            log.fatal("Error while loading DAOFactory, parameters for the constructor are illegal: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        } catch (InstantiationException ie) {
            log.fatal("The instatiation of a DAOFactory failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iace) {
            log.fatal("The instatiation of a DAOFactory failed: " + iace.toString());
            throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
        } catch (InvocationTargetException ite) {
            log.fatal("the instatiation of a DAOFactory failed: " + ite.toString() + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
    }

    /**
     * loads and instantiates the implementation of IOMEncoder
     * 
     * @param sosProps
     *          properties created from sos.config file
     * @throws OwsExceptionReport
     *         if initializing the IOMEncoder Implementation class failed
     */
    @SuppressWarnings("unchecked")
    private void initializeOMEncoder(Properties sosProps) throws OwsExceptionReport {
        String className = sosProps.getProperty(OMENCODER);
        try {
            if (className == null) {
                log.fatal("No OMEncoder Implementation is set in the configFile!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode, "SosConfigurator.initializeOMEncoder()", "No OMEncoder Implementation is set in the configFile!");
                throw se;
            }
            Class omEncoderClass = Class.forName(className);
            Class[] constrArgs = {};
            Object[] args = {};
            Constructor<IOMEncoder> constructor = omEncoderClass.getConstructor();
            this.omEncoder = constructor.newInstance();
            log.info("\n******\n" + className + " loaded successfully!\n******\n");
        } catch (ClassNotFoundException cnfe) {
            log.fatal("Error while loading OMEncoder, required class could not be loaded: " + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        } catch (SecurityException se) {
            log.fatal("Error while loading OMEncoder: " + se.toString());
            throw new OwsExceptionReport(se.getMessage(), se.getCause());
        } catch (NoSuchMethodException nsme) {
            log.fatal("Error while loading OMEncoder, no required constructor available: " + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        } catch (IllegalArgumentException iae) {
            log.fatal("Error while loading OMEncoder, parameters for the constructor are illegal: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        } catch (InstantiationException ie) {
            log.fatal("The instatiation of a OMEncoder failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iace) {
            log.fatal("The instatiation of an OMEncoder failed: " + iace.toString());
            throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
        } catch (InvocationTargetException ite) {
            log.fatal("the instatiation of an OMEncoder failed: " + ite.toString() + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
    }

    /**
     * loads and instantiates the implementation of GMLEncoder
     * 
     *  @param sosProps
     *          properties created from sos.config file
     * @throws OwsExceptionReport
     *         if initializing the GMLEncoder Implementation class failed
     */
    @SuppressWarnings("unchecked")
    private void initializeGMLEncoder(Properties sosProps) throws OwsExceptionReport {
        String className = sosProps.getProperty(GMLENCODER);
        try {
            if (className == null) {
                log.fatal("No GMLEncoder Implementation is set in the configFile!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode, "SosConfigurator.initializeGMLEncoder()", "No GMLEncoder Implementation is set in the configFile!");
                throw se;
            }
            Class gmlEncoderClass = Class.forName(className);
            Class[] constrArgs = {};
            Object[] args = {};
            Constructor<IGMLEncoder> constructor = gmlEncoderClass.getConstructor();
            this.gmlEncoder = constructor.newInstance();
            log.info("\n******\n" + className + " loaded successfully!\n******\n");
        } catch (ClassNotFoundException cnfe) {
            log.fatal("Error while loading GMLEncoder, required class could not be loaded: " + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        } catch (SecurityException se) {
            log.fatal("Error while loading GMLEncoder: " + se.toString());
            throw new OwsExceptionReport(se.getMessage(), se.getCause());
        } catch (NoSuchMethodException nsme) {
            log.fatal("Error while loading GMLEncoder, no required constructor available: " + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        } catch (IllegalArgumentException iae) {
            log.fatal("Error while loading GMLEncoder, parameters for the constructor are illegal: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        } catch (InstantiationException ie) {
            log.fatal("The instatiation of GMLEncoder failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iace) {
            log.fatal("The instatiation of GMLEncoder failed: " + iace.toString());
            throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
        } catch (InvocationTargetException ite) {
            log.fatal("the instatiation of GMLEncoder failed: " + ite.toString() + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
    }

    /**
     * loads and instantiates the implementation of IHttpPostRequestDecoder
     * 
     * @param sosProps
     *          properties created from sos.config file
     * @throws OwsExceptionReport
     *         if initializing the IHttpPostRequestDecoder Implementation class failed
     */
    @SuppressWarnings("unchecked")
    private void initializeHttpPostRequestDecoder(Properties sosProps) throws OwsExceptionReport {
        String className = sosProps.getProperty(POSTREQUESTDECODER);
        try {
            if (className == null) {
                log.fatal("No postRequestDecoder Implementation is set in the configFile!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode, "SosConfigurator.initializeHttpPostRequestDecoder()", "No postRequestDecoder Implementation is set in the configFile!");
                throw se;
            }
            Class httpPostRequestDecoderClass = Class.forName(className);
            Constructor<IHttpPostRequestDecoder> constructor = httpPostRequestDecoderClass.getConstructor();
            this.httpPostDecoder = constructor.newInstance();
            log.info("\n******\n" + className + " loaded successfully!\n******\n");
        } catch (ClassNotFoundException cnfe) {
            log.fatal("Error while loading postRequestDecoder, required class could not be loaded: " + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        } catch (SecurityException se) {
            log.fatal("Error while loading postRequestDecoder: " + se.toString());
            throw new OwsExceptionReport(se.getMessage(), se.getCause());
        } catch (NoSuchMethodException nsme) {
            log.fatal("Error while loading postRequestDecoder, no required constructor available: " + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        } catch (IllegalArgumentException iae) {
            log.fatal("Error while loading postRequestDecoder, parameters for the constructor are illegal: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        } catch (InstantiationException ie) {
            log.fatal("The instatiation of a postRequestDecoder failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iace) {
            log.fatal("The instatiation of an postRequestDecoder failed: " + iace.toString());
            throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
        } catch (InvocationTargetException ite) {
            log.fatal("the instatiation of an postRequestDecoder failed: " + ite.toString() + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
    }

    /**
     * loads and instantiates the implementation of IHttpGetRequestDecoder
     * 
     * @param sosProps
     *          properties created from sos.config file
     * @throws OwsExceptionReport
     *         if initializing the IHttpGetRequestDecoder Implementation class failed
     */
    @SuppressWarnings("unchecked")
    private void initializeHttpGetRequestDecoder(Properties sosProps) throws OwsExceptionReport {
        String className = sosProps.getProperty(GETREQUESTDECODER);
        try {
            if (className == null) {
                log.fatal("No getRequestDecoder Implementation is set in the configFile!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode, "SosConfigurator.initializeHttpGetRequestDecoder()", "No getRequestDecoder Implementation is set in the configFile!");
                throw se;
            }
            Class httpGetRequestDecoderClass = Class.forName(className);
            Constructor<IHttpGetRequestDecoder> constructor = httpGetRequestDecoderClass.getConstructor();
            this.httpGetDecoder = constructor.newInstance();
            log.info("\n******\n" + className + " loaded successfully!\n******\n");
        } catch (ClassNotFoundException cnfe) {
            log.fatal("Error while loading getRequestDecoder, required class could not be loaded: " + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        } catch (SecurityException se) {
            log.fatal("Error while loading getRequestDecoder: " + se.toString());
            throw new OwsExceptionReport(se.getMessage(), se.getCause());
        } catch (NoSuchMethodException nsme) {
            log.fatal("Error while loading getRequestDecoder, no required constructor available: " + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        } catch (IllegalArgumentException iae) {
            log.fatal("Error while loading getRequestDecoder, parameters for the constructor are illegal: " + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        } catch (InstantiationException ie) {
            log.fatal("The instatiation of a getRequestDecoder failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iace) {
            log.fatal("The instatiation of an getRequestDecoder failed: " + iace.toString());
            throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
        } catch (InvocationTargetException ite) {
            log.fatal("the instatiation of an getRequestDecoder failed: " + ite.toString() + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
    }

    /**
     * 
     * @return ArrayList with names of the RequestListeners defined in the ConfigFile
     * 
     * @throws OwsExceptionReport
     *         if no RequestListeners are defined in the configFile
     */
    private ArrayList<String> loadListeners() throws OwsExceptionReport {
        ArrayList<String> listeners = new ArrayList<String>();
        String listenersList = props.getProperty(LISTENERS);
        if (listenersList == null) {
            log.fatal("No RequestListeners are defined in the ConfigFile!");
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.NoApplicableCode, "SosConfigurator.loadListeners()", "No request Listeners are defined in the configFile!");
            throw se;
        }
        StringTokenizer tokenizer = new StringTokenizer(listenersList, ",");
        while (tokenizer.hasMoreTokens()) {
            listeners.add(tokenizer.nextToken());
        }
        return listeners;
    }

    /**
     * method (re-)loads the configFile
     * 
     * @param is
     *        InputStream containing the configFile
     * 
     * @throws UnavailableException
     *         if the configFile could not be loaded
     * @throws IOException
     */
    public Properties loadProperties(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);
        return properties;
    }

    /**
     * 
     * @return Returns the capabilities skeleton file
     */
    public File getCapabilitiesSkeleton() {
        return capabilitiesSkeleton;
    }

    /**
     * 
     * @return Returns the capabilities skeleton file
     */
    public File getSensorDir() {
        return sensorDir;
    }

    /**
     * @return Returns the DAOFactory.
     */
    public IDAOFactory getFactory() {
        return factory;
    }

    /**
     * @param factory
     *        The DAOFactory to set.
     */
    public void setFactory(IDAOFactory factory) {
        this.factory = factory;
    }

    /**
     * @return Returns the lease for the getResult template (in minutes).
     */
    public int getLease() {
        return lease;
    }

    /**
     * @param lease
     *        The lease to set.
     */
    public void setLease(int lease) {
        this.lease = lease;
    }

    /**
     * @return Returns the tokenSeperator.
     */
    public String getTokenSeperator() {
        return tokenSeperator;
    }

    /**
     * @param tokenSeperator
     *        The tokenSeperator to set.
     */
    public void setTokenSeperator(String tokenSeperator) {
        this.tokenSeperator = tokenSeperator;
    }

    /**
     * @return Returns the tupleSeperator.
     */
    public String getTupleSeperator() {
        return tupleSeperator;
    }

    /**
     * @param tupleSeperator
     *        The tupleSeperator to set.
     */
    public void setTupleSeperator(String tupleSeperator) {
        this.tupleSeperator = tupleSeperator;
    }

    /**
     * Returns decimal separator
     */
    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    /**
     * Sets the decimal separator
     * 
     * @param decimalSeparator
     *        decimal separator
     */
    public void setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    /**
     * @return Returns the gmlDateFormat.
     */
    public String getGmlDateFormat() {
        return gmlDateFormat;
    }

    /**
     * @param gmlDateFormat
     *        The gmlDateFormat to set.
     */
    public void setGmlDateFormat(String gmlDateFormat) {
        this.gmlDateFormat = gmlDateFormat;
    }

    /**
     * @return Returns the noDataValue.
     */
    public String getNoDataValue() {
        return noDataValue;
    }

    /**
     * @param noDataValue
     *        The noDataValue to set.
     */
    public void setNoDataValue(String noDataValue) {
        this.noDataValue = noDataValue;
    }

    /**
     * 
     * @return Returns the decoder for decoding Http-POST requests
     */
    public IHttpPostRequestDecoder getHttpPostDecoder() {
        return this.httpPostDecoder;
    }

    /**
     * 
     * @return Returns the decoder for decoding Http-GET requests
     */
    public IHttpGetRequestDecoder getHttpGetDecoder() {
        return httpGetDecoder;
    }

    /**
     * @return the supportsQuality
     */
    public boolean isSupportsQuality() {
        return supportsQuality;
    }

    /**
     * @param supportsQuality
     *        the supportsQuality to set
     */
    public void setSupportsQuality(boolean supportsQuality) {
        this.supportsQuality = supportsQuality;
    }

    /**
     * @return the eastingFirst
     */
    public boolean isEastingFirst() {
        return eastingFirst;
    }

    /**
     * @param eastingFirst
     *        the eastingFirst to set
     */
    public void setEastingFirst(boolean eastingFirst) {
        this.eastingFirst = eastingFirst;
    }

    public boolean isFoiEncodedInObservation() {
        return foiEncodedInObservation;
    }

    /**
     * @return the characterEncoding
     */
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * @return the gmlEncoder
     */
    public IGMLEncoder getGmlEncoder() {
        return gmlEncoder;
    }

    /**
     * @return the omEncoder
     */
    public IOMEncoder getOmEncoder() {
        return omEncoder;
    }

    /**
     * @return the base path for configuration files
     */
    public String getBasePath() {
        return basepath;
    }
}

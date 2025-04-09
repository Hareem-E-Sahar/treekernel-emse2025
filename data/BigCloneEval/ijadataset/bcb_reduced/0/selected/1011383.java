package pierre.api;

import pierre.util.ClassLoaderUtility;
import pierre.system.*;
import pierre.reports.*;
import pierre.model.*;
import pierre.db.*;
import pierre.io.PierreFileReader;
import pierre.plugins.PostProcessingService;
import pierre.configurationTool2.*;
import pedro.system.*;
import pedro.soa.security.*;
import pedro.desktopDeployment.Workspace;
import pedro.desktopDeployment.WorkspaceFileFinder;
import pedro.mda.config.*;
import pedro.mda.schema.Startup;
import pedro.mda.model.*;
import pedro.soa.ontology.sources.*;
import pedro.soa.ontology.views.*;
import pedro.soa.security.*;
import pedro.util.DisplayNameList;
import java.io.*;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.net.MalformedURLException;
import java.util.*;
import java.lang.reflect.Constructor;
import java.util.PropertyResourceBundle;

public class PierreService {

    private PedroFormContext pedroFormContext;

    private BrowserConfigurationModel browserConfigurationModel;

    private String[] queryFeatureNames;

    private HashMap queryFeatureFromName;

    private QueryFeatureManager queryFeatureManager;

    private DataRepository dataRepository;

    private SecurityService securityService;

    private Workspace workSpace;

    private RecordModelFactory recordModelFactory;

    private PostProcessingService[] postProcessingServices;

    private Boolean testMode;

    private boolean includeSecurity;

    private boolean enableClassLoader;

    public PierreService() {
        try {
            String modelDirectoryName = BrowserServiceResources.getMessage("pierreService.modelDirectory");
            String serviceConfigurationFileName = BrowserServiceResources.getMessage("pierreService.serviceFile");
            String dependencyLibraryDirectoryName = BrowserServiceResources.getMessage("pierreService.libraryDirectory");
            String startupDirectory = ".";
            init(startupDirectory, modelDirectoryName, serviceConfigurationFileName, dependencyLibraryDirectoryName, true);
        } catch (Exception err) {
            err.printStackTrace(System.out);
        }
    }

    public PierreService(PedroFormContext pedroFormContext) throws Exception {
        this.pedroFormContext = pedroFormContext;
        pedroFormContext.setApplicationProperty(PierreApplicationContext.PIERRE_SERVICE, this);
        this.browserConfigurationModel = (BrowserConfigurationModel) pedroFormContext.getApplicationProperty(PierreApplicationContext.BROWSER_CONFIGURATION_MODEL);
        User user = (User) pedroFormContext.getApplicationProperty(PedroApplicationContext.USER);
        SecurityService securityService = (SecurityService) pedroFormContext.getApplicationProperty(PedroApplicationContext.SECURITY_SERVICE);
        queryFeatureManager = new QueryFeatureManager(securityService, user);
        queryFeatureManager.setRootCategory(browserConfigurationModel.getRootQueryFeatureCategory());
        recordModelFactory = (RecordModelFactory) pedroFormContext.getApplicationProperty(PedroApplicationContext.RECORD_MODEL_FACTORY);
        this.testMode = (Boolean) pedroFormContext.getApplicationProperty(PierreApplicationContext.TEST_MODE);
        enableClassLoader = true;
        pedroFormContext.setApplicationProperty(PedroApplicationContext.ENABLE_CLASS_LOADER, new Boolean(enableClassLoader));
        initialiseDatabaseRepository();
        initialiseSecurityService();
        SuperUser superUser = new SuperUser();
        establishPostProcessingServices(superUser);
    }

    public PierreService(File propertiesFile, boolean enableClassLoader) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(propertiesFile);
        PropertyResourceBundle workSpaceResourceBundle = new PropertyResourceBundle(fileInputStream);
        String modelDirectoryName = workSpaceResourceBundle.getString("modelDirectory");
        String serviceConfigurationFilename = workSpaceResourceBundle.getString("serviceFile");
        String dependencyLibraryDirectoryName = workSpaceResourceBundle.getString("libraryDirectory");
        String startupDirectory = deriveStartupDirectory(propertiesFile);
        init(startupDirectory, modelDirectoryName, serviceConfigurationFilename, dependencyLibraryDirectoryName, enableClassLoader);
    }

    private String deriveStartupDirectory(File propertiesFile) {
        String path = propertiesFile.getAbsolutePath();
        int lastSlashIndex = path.lastIndexOf(File.separator);
        String workingDirectoryPath = path.substring(0, lastSlashIndex);
        return workingDirectoryPath;
    }

    private void init(String startupDirectory, String modelDirectoryName, String serviceConfigurationFileName, String dependencyLibraryDirectoryName, boolean enableClassLoader) throws Exception {
        this.enableClassLoader = enableClassLoader;
        checkEmptyProperty(modelDirectoryName);
        File modelDirectory = new File(modelDirectoryName);
        checkNonExistentFile(modelDirectory);
        checkDirectoryStatus(modelDirectory);
        checkEmptyProperty(serviceConfigurationFileName);
        File serviceConfigurationFile = new File(serviceConfigurationFileName);
        checkNonExistentFile(serviceConfigurationFile);
        checkEmptyProperty(dependencyLibraryDirectoryName);
        if (dependencyLibraryDirectoryName != null) {
            if (dependencyLibraryDirectoryName.equals("") == false) {
                File dependencyLibrary = new File(dependencyLibraryDirectoryName);
                checkNonExistentFile(dependencyLibrary);
                checkDirectoryStatus(dependencyLibrary);
            }
        }
        try {
            PierreStartupUtility pierreStartupUtility = new PierreStartupUtility();
            pedroFormContext = pierreStartupUtility.createFormContext(startupDirectory, modelDirectoryName, serviceConfigurationFile, enableClassLoader);
            PedroUIFactory pedroUIFactory = (PedroUIFactory) pedroFormContext.getApplicationProperty(PedroApplicationContext.USER_INTERFACE_FACTORY);
            if (pedroUIFactory == null) {
                System.out.println("PierreService init ui factory NULL!");
            } else {
                System.out.println("PierreService init ui factory not null!");
            }
            queryFeatureManager = pierreStartupUtility.getQueryFeatureManager();
            browserConfigurationModel = (BrowserConfigurationModel) pedroFormContext.getApplicationProperty(PierreApplicationContext.BROWSER_CONFIGURATION_MODEL);
            recordModelFactory = (RecordModelFactory) pedroFormContext.getApplicationProperty(PedroApplicationContext.RECORD_MODEL_FACTORY);
            initialiseDatabaseRepository();
            initialiseSecurityService();
            SuperUser superUser = new SuperUser();
            establishPostProcessingServices(superUser);
            pedroFormContext.setApplicationProperty(PierreApplicationContext.PIERRE_SERVICE, this);
        } catch (Exception err) {
            PedroException exception = new PedroException(err.toString());
            err.printStackTrace(System.out);
            throw exception;
        }
        getTomcatContextHelpDirectory();
    }

    private void initialiseDatabaseRepository() throws Exception {
        DatabaseModel databaseModel = browserConfigurationModel.getDatabaseModel();
        String databaseClassName = databaseModel.getDatabaseClassName();
        if (databaseClassName.equals("pierre.db.DummyDataRepository") == true) {
            DummyDataRepository dummyDataRepository = new DummyDataRepository();
            BrowseModel browseModel = browserConfigurationModel.getBrowseModel();
            ArrayList browseAttributes = browseModel.getBrowseAttributes();
            dummyDataRepository.setBrowseAttributes(browseAttributes);
            dataRepository = (DataRepository) dummyDataRepository;
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            Class repositoryClass = null;
            if (enableClassLoader == true) {
                ClassLoaderUtility classLoaderUtility = new ClassLoaderUtility(classLoader);
                URL[] urls = databaseModel.getDependencies();
                classLoaderUtility.addURLs(databaseModel.getDependencies());
                repositoryClass = classLoaderUtility.findClass(databaseClassName);
            } else {
                repositoryClass = Class.forName(databaseClassName);
            }
            Constructor constructor = repositoryClass.getConstructor(new Class[0]);
            dataRepository = (DataRepository) constructor.newInstance(new Object[0]);
        }
        dataRepository.setParameters(databaseModel.getParameters());
    }

    private void initialiseSecurityService() throws Exception {
        securityService = dataRepository.getSecurityService();
        if (securityService == null) {
            includeSecurity = false;
            DummySecurityService dummySecurityService = new DummySecurityService();
            dummySecurityService.setWorkSpace(workSpace);
            securityService = (SecurityService) dummySecurityService;
        } else {
            includeSecurity = true;
        }
    }

    /**
	* Security aspects
	*/
    private void establishPostProcessingServices(User user) throws Exception {
        if (includePlugins(user) == false) {
            return;
        }
        PluginsModel pluginsModel = browserConfigurationModel.getPluginsModel();
        ArrayList pluginDescriptions = pluginsModel.getPluginDescriptions();
        int numberOfPluginDescriptions = pluginDescriptions.size();
        ArrayList plugins = new ArrayList();
        for (int i = 0; i < numberOfPluginDescriptions; i++) {
            PluginDescription pluginDescription = (PluginDescription) pluginDescriptions.get(i);
            String pluginClassName = pluginDescription.getPluginDescriptionClassName();
            Class postProcessingServiceClass = null;
            if (testMode.booleanValue() == false) {
                postProcessingServiceClass = Class.forName(pluginClassName);
            } else {
                ClassLoader classLoader = getClass().getClassLoader();
                ClassLoaderUtility classLoaderUtility = new ClassLoaderUtility(classLoader);
                classLoaderUtility.addURLs(pluginDescription.getDependencies());
                postProcessingServiceClass = classLoaderUtility.findClass(pluginClassName);
            }
            Constructor constructor = postProcessingServiceClass.getConstructor(new Class[0]);
            PostProcessingService service = (PostProcessingService) constructor.newInstance(new Object[0]);
            String serviceName = service.getName();
            if (securityService.canExecute(serviceName, user) == true) {
                plugins.add(service);
            }
        }
        postProcessingServices = (PostProcessingService[]) plugins.toArray(new PostProcessingService[0]);
    }

    /**
	* @return the string that represents the start of a URL that considers
	* the name and port of the web server
	*/
    public String getTomcatServerInformation() {
        return dataRepository.getTomcatServerInformation();
    }

    /**
	* @return the directory that contains context sensitive help files used with
	* the web application deployment
	*/
    public String getTomcatContextHelpDirectory() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(dataRepository.getTomcatWebApplicationPath());
        buffer.append("/models/");
        File modelDirectory = (File) pedroFormContext.getApplicationProperty(PedroApplicationContext.MODEL_DIRECTORY);
        buffer.append(modelDirectory.getName());
        buffer.append("/doc/");
        return buffer.toString();
    }

    /**
	* This is used by the command line service to help display the report
	* file formats users can specify in a command.  in future this should
	* be decprecated in favour of trying to get report formats supported
	* by a given type of query feature.  
	*
	* @return the list of report file formats supported by the data
	* repository.
	*/
    public ReportFileFormat[] getSupportedReportFileFormats() {
        return dataRepository.getSupportedReportFileFormats();
    }

    /**
   * returns the report that shows the high level summaries of data sets
   * managed by the data repository
   * @param deploymentForm the kind of deployment requesting the report.  This
   * could be used to help render the report for different kinds of displays.
   * @param user the user who is using the deployment that interacts with the service.
   * @return a report showing summaries of data sets maintained by the data
   * repository.
   */
    public Report getBrowseReport(DeploymentForm deploymentForm, User user) {
        return dataRepository.getBrowseReport(pedroFormContext, deploymentForm, user);
    }

    /**
	* this method is used for supplying field operator possibilities to the
	* advanced search features of the deployments.  This might change depending
	* on the nature of the repository.  For example, a repository implemented
	* as a relational database may include operators like "LIKE" which may not be
	* supported by other data management technologies.
	* @param dataFieldModel the query field, represented by Pedro's DataFieldModel
	* data structure
	* @param user the user of the deployment that is interacting with the service.
	* @return a colection of field operators
	*/
    public String[] getQueryFieldOperatorTypes(DataFieldModel dataFieldModel, User user) {
        RecordModelUtility recordModelUtility = new RecordModelUtility();
        FieldDataType type = recordModelUtility.getType(dataFieldModel);
        FieldOperatorProvider fieldOperatorProvider = dataRepository.getFieldOperatorProvider(user);
        return fieldOperatorProvider.getFieldOperators(type);
    }

    /**
	* returns a field operator provider to the deployment.  The provider is responsible
	* for supplying a list of valid field operators (eg: =, !=, >, <) for features
	* such as advanced search.  This provider may change depending on the user.  for
	* example, the repository designer may decide that certain users shouldn't have
	* access to certain operators
	* @param user the user of the deployment that is interacting with the service
	* @return a provider of field operators.
	*/
    public FieldOperatorProvider getFieldOperatorProvider(User user) {
        FieldOperatorProvider fieldOperatorProvider = dataRepository.getFieldOperatorProvider(user);
        return fieldOperatorProvider;
    }

    /**
	* returns the default operator that associated query fields with a value.  
	* In most cases, this should be "=" or "contains" but it depends on the
	* way the data repository has been implemented.
	* @param dataFieldModel the query field represented by the Pedro DataFieldModel
	* @param user the user of the deployment that interacts with the service.
	* @return string representing an operator (eg: "=")
	*/
    public String getDefaultQueryFieldOperatorType(DataFieldModel dataFieldModel, User user) {
        RecordModelUtility recordModelUtility = new RecordModelUtility();
        FieldDataType type = recordModelUtility.getType(dataFieldModel);
        FieldOperatorProvider fieldOperatorProvider = dataRepository.getFieldOperatorProvider(user);
        return fieldOperatorProvider.getDefaultFieldOperator(type);
    }

    /**
	* returns a collection of default field operators.  This is used
	* in the advanced search feature of the web application.  Unlike the 
	* standalone GUI application, selecting the field name in the advanced search
	* can't automatically cause the operator list to change.  Therefore, the advanced
	* search of the web application presents a static list of field operators,
	* some of which may not be relevant to certain fields.  for example, ">" could
	* be listed as an operator but not be relevant to a text-based field.
	*
	* @param user the user of the deployment that interacts with the service
	* @return an array of default field operators. 
	*/
    public String[] getAllDefaultFieldOperators(User user) {
        FieldOperatorProvider fieldOperatorProvider = dataRepository.getFieldOperatorProvider(user);
        return fieldOperatorProvider.getAllDefaultFieldOperators();
    }

    /**
	* not entirely sure how this method is different from getAllDefaultFieldOperators(...)
	* @param user the user of the deployment that interacts with the service
	* @return an array of field operators
	*/
    public String[] getAllFieldOperators(User user) {
        FieldOperatorProvider fieldOperatorProvider = dataRepository.getFieldOperatorProvider(user);
        return fieldOperatorProvider.getAllFieldOperators();
    }

    /**
	* returns a query feature manager for a given user.  The query 
	* feature manager is responsible for providing the simple searches
	* that appear in the deployments.  
	* @param user the user of the deployment that interacts with the service
	* @return a query feature manager.
	*/
    public QueryFeatureManager getQueryFeatureManager(User user) {
        QueryFeatureManager userSpecificQueryFeatureManager = new QueryFeatureManager(securityService, user);
        CannedQueryFeatureCategory rootCategory = queryFeatureManager.getRootCategory();
        CannedQueryFeatureCategory clonedRootCategory = (CannedQueryFeatureCategory) rootCategory.clone();
        userSpecificQueryFeatureManager.setRootCategory(clonedRootCategory);
        return userSpecificQueryFeatureManager;
    }

    /**
	* returns a report that describes properties of the XML schema that
	* drives the data repository service
	* @param pedroFormContext a general collection of objects that refer to 
	* different parts of the software system.
	* @return report showing schema information
	*/
    public Report getSchemaInformation(PedroFormContext pedroFormContext) {
        return dataRepository.getSchemaInformation(pedroFormContext);
    }

    /**
	* returns a list of databases for a given user.  This is used in expert
	* searches when the user has the option of specifying a database for
	* some native query
	* @param user the user of the deployment that interacts with the service
	* @return names of databases managed by the repository.  Usually, a 
	* data repository manages a single database.
	*/
    public String[] getDataBases(User user) {
        return dataRepository.getDataBases(user);
    }

    /**
	* returns the language supported by a given database.  This is used to 
	* provide options in the expert search.
	* @param dataBase managed by the data repository
	* @param user the user of the deployment that interacts with the service
	* @return the language for a given database (eg: "SQL", "XQuery" etc)
	*/
    public String getLanguageForDataBase(String dataBase, User user) {
        return dataRepository.getLanguageForDataBase(dataBase, user);
    }

    /**
	* @return the basic Pierre data structure used to specify a 
	* data dissemination service
	*/
    public BrowserConfigurationModel getBrowserConfigurationModel() {
        return browserConfigurationModel;
    }

    /**
	* @return a general collection of objects that represent different
	* parts of the software system
	*/
    public PedroFormContext getPedroFormContext() {
        return pedroFormContext;
    }

    /**
	* @return the title of the service.  This is displayed as the title for
	* different application deployments
	*/
    public String getTitle() {
        GeneralInformationModel generalInformationModel = browserConfigurationModel.getGeneralInformationModel();
        return generalInformationModel.getTitle();
    }

    /**
	* gets a collection of data entry options that are appropriate for a
	* given user accessing a different query field.
	*
	* @param queryField the field of some query field.
	* @param user the user of the deployment that interacts with the service
	* @return a collection of data entry options associated with the 
	* query field.
	*/
    public DataEntryOption[] getDataEntryOptions(QueryField queryField, User user) {
        ArrayList dataEntryOptions = new ArrayList();
        SchemaBasedValues schemaBasedValues = getSchemaBasedValues(queryField, user);
        if (schemaBasedValues != null) {
            dataEntryOptions.add(schemaBasedValues);
        } else {
            if (queryField.supportsFreeText() == true) {
                FreeTextValue freeTextValue = new FreeTextValue();
                dataEntryOptions.add(freeTextValue);
            }
            OntologyServiceValues ontologyServiceValues = getOntologyServiceValues(queryField);
            if (ontologyServiceValues != null) {
                dataEntryOptions.add(ontologyServiceValues);
            }
        }
        ExistingValues existingValues = getExistingValues(queryField, user);
        if (existingValues != null) {
            dataEntryOptions.add(existingValues);
        }
        DataEntryOption[] results = (DataEntryOption[]) dataEntryOptions.toArray(new DataEntryOption[0]);
        return results;
    }

    /**
	* gets the ExistingValues data entry option for a query field. 
	* @param queryField a field of some query
	* @param user the user of the deployment that interacts with the service
	* @return an existing values object if appropriate.  If the field
	* isn't associated with this option, null is returned.
	*/
    public ExistingValues getExistingValues(QueryField queryField, User user) {
        if (queryField.supportsExistingValues() == true) {
            CannedQuery cannedQuery = queryField.getContainingQuery();
            String recordClassName = cannedQuery.getRecordClassName();
            String[] values = dataRepository.getExistingValues(recordClassName, queryField.getName(), cannedQuery.getSchemaContext(), user);
            ExistingValues existingValues = new ExistingValues(values);
            return existingValues;
        } else {
            return null;
        }
    }

    /**
	* gets the SchemaBasedValues data entry option for a query field.  
	* @param queryField a field of some query
	* @param user the user of the deployment that interacts with the service
	* @return a schema based values object if appropriate.  If the field
	* isn't associated with this option, null is returned.
	*/
    public SchemaBasedValues getSchemaBasedValues(QueryField queryField, User user) {
        EditFieldModel editFieldModel = queryField.getEditFieldModel();
        if (editFieldModel instanceof GroupFieldModel) {
            GroupFieldModel groupFieldModel = (GroupFieldModel) editFieldModel;
            String[] choices = groupFieldModel.getChoices();
            SchemaBasedValues schemaBasedValues = new SchemaBasedValues(choices);
            return schemaBasedValues;
        } else {
            return null;
        }
    }

    /**
	* gets the OntologyServiceValues data entry option for a query field.  
	* @param queryField a field of some query
	* @param user the user of the deployment that interacts with the service
	* @return an ontology service values object if appropriate.  If the field
	* isn't associated with this option, null is returned.
	*/
    public OntologyServiceValues getOntologyServiceValues(QueryField queryField) {
        if (queryField.supportsOntologyServices()) {
            CannedQueryObject cannedQueryObject = queryField.getContainingQuery();
            String recordClassName = cannedQueryObject.getRecordClassName();
            SchemaConceptProperties schemaConceptProperties = (SchemaConceptProperties) pedroFormContext.getApplicationProperty(PedroApplicationContext.SCHEMA_CONCEPT_PROPERTIES);
            EditFieldConfiguration configurationRecord = schemaConceptProperties.getEditFieldConfiguration(recordClassName, queryField.getName());
            OntologyServiceConfiguration[] ontologyServiceConfigurations = configurationRecord.getOntologyServiceConfigurations();
            OntologyService[] ontologyServices = createOntologyServices(ontologyServiceConfigurations);
            OntologyContext ontologyContext = queryField.getOntologyContext();
            ontologyContext.setCallingField(queryField.getName());
            OntologyServiceValues ontologyServiceValues = new OntologyServiceValues(pedroFormContext, ontologyServices);
            return ontologyServiceValues;
        } else {
            return null;
        }
    }

    private OntologyService[] createOntologyServices(OntologyServiceConfiguration[] ontologyServiceConfigurations) {
        if (ontologyServiceConfigurations.length == 0) {
            return (new OntologyService[0]);
        }
        ArrayList services = new ArrayList();
        OntologyServiceFactory ontologyServiceFactory = (OntologyServiceFactory) pedroFormContext.getApplicationProperty(PedroApplicationContext.ONTOLOGY_SERVICE_FACTORY);
        for (int i = 0; i < ontologyServiceConfigurations.length; i++) {
            OntologyService ontologyService = ontologyServiceFactory.createService(ontologyServiceConfigurations[i]);
            services.add(ontologyService);
        }
        OntologyService[] results = (OntologyService[]) services.toArray(new OntologyService[0]);
        return results;
    }

    /**
	* gets the OntologyServiceValues data entry option for a query field.  
	* @return a list of browse attributes.  This method is only used by Pierre's
	* test application.  The dummy data repository uses the browse attributes
	* to generate a dummy browse report.
	*/
    public ArrayList getBrowseAttributes() {
        BrowseModel browseModel = browserConfigurationModel.getBrowseModel();
        return browseModel.getBrowseAttributes();
    }

    /**
	* determines whether a given user will have access to the browse feature
	* @param user the user of the deployment that interacts with the service
	* @return true if the browse feature should appear in the deployment.  Otherwise
	* the feature is not displayed.
	*/
    public boolean includeBrowse(User user) {
        String featureCode = BrowserServiceResources.getMessage("general.browse.title");
        if (securityService.canExecute(featureCode, user) == false) {
            return false;
        }
        IncludeServiceAspects includeServiceAspects = browserConfigurationModel.getIncludeServiceAspects();
        return includeServiceAspects.includeBrowse();
    }

    /**
	* method used to determine whether a simple search should appear
	* in a deployment. The decision can be influenced by what user
	* is trying to use the service.
	* @param user the current user of a deployment.
	* @return true if the simple search should be included in the deployment;
	* otherwise don't display the feature.
	*/
    public boolean includeSimpleSearch(User user) {
        String featureCode = BrowserServiceResources.getMessage("general.simpleSearch.title");
        if (securityService.canExecute(featureCode, user) == false) {
            return false;
        }
        IncludeServiceAspects includeServiceAspects = browserConfigurationModel.getIncludeServiceAspects();
        return includeServiceAspects.includeSimpleSearch();
    }

    /**
	* method used to determine whether an advanced search should appear
	* in a deployment. The decision can be influenced by what user
	* is trying to use the service.
	* @param user the current user of a deployment.
	* @return true if the advanced search should be included in the deployment;
	* otherwise don't display the feature.
	*/
    public boolean includeAdvancedSearch(User user) {
        String featureCode = BrowserServiceResources.getMessage("general.advancedSearch.title");
        if (securityService.canExecute(featureCode, user) == false) {
            return false;
        }
        IncludeServiceAspects includeServiceAspects = browserConfigurationModel.getIncludeServiceAspects();
        return includeServiceAspects.includeAdvancedSearch();
    }

    /**
	* method used to determine whether an expert search should appear
	* in a deployment. The decision can be influenced by what user
	* is trying to use the service.
	* @param user the current user of a deployment.
	* @return true if the expert search should be included in the deployment;
	* otherwise don't display the feature.
	*/
    public boolean includeExpertSearch(User user) {
        String featureCode = BrowserServiceResources.getMessage("general.expertSearch.title");
        if (securityService.canExecute(featureCode, user) == false) {
            return false;
        }
        IncludeServiceAspects includeServiceAspects = browserConfigurationModel.getIncludeServiceAspects();
        return includeServiceAspects.includeExpertSearch();
    }

    /**
	* method used to determine whether a deployment should include
	* plugins
	* @param user the current user of a deployment.
	* @return true if the plugins should be included in the deployment;
	* otherwise don't display the feature.
	*/
    public boolean includePlugins(User user) {
        String featureCode = BrowserServiceResources.getMessage("general.plugins.title");
        if (securityService.canExecute(featureCode, user) == false) {
            return false;
        }
        IncludeServiceAspects includeServiceAspects = browserConfigurationModel.getIncludeServiceAspects();
        return includeServiceAspects.includePlugins();
    }

    /**
	* method used to determine whether the service uses a
	* security service.
	* @return true if the service supports a security service
	*/
    public boolean includeSecurity() {
        return includeSecurity;
    }

    /**
	* a utility method for extracting a list of queries for a given
	* query feature.
	* @param queryFeature the query feature used to extract queries
	* @param user a user of a deployment
	* @return a list of queries associated with the query feature
	*/
    public DisplayNameList getQueryList(QueryFeature queryFeature, User user) {
        CannedQuery[] queries = queryFeature.getQueries();
        DisplayNameList list = new DisplayNameList(queries);
        return list;
    }

    /**
	* a utility method for extracting a list of meta query fields for a given
	* canned query.
	* @param cannedQuery used to extract a list of meta query fields
	* @return a list of meta query fields
	*/
    public DisplayNameList getMetaQueryFieldList(CannedQuery cannedQuery) {
        ArrayList metaQueryFields = cannedQuery.getMetaQueryFields();
        DisplayNameList list = new DisplayNameList(metaQueryFields);
        return list;
    }

    /**
	* a utility method for extracting a list of query fields for a given
	* canned query.  It has the ability to render list display names
	* in a way that indicates the fields are required.  This is used in 
	* the text-based menu-driven deployment to indicate required fields.  
	* The other deployments have other ways of indicating the required/
	* optional status.  For example, the standalone GUI application and
	* the web application render the field names in bold print
	*
	* @param cannedQuery used to extract a list of query fields
	* @param indicateRequiredFields determines whether the list should
	* try to render required fields (displayed in cap letters) or not 
	* (all fields displayed normally)
	*
	* @param user user of a deployment using the service.
	* @return a list of query fields associated with the canned query
	*/
    public DisplayNameList getQueryFieldList(CannedQuery cannedQuery, boolean indicateRequiredFields, User user) {
        ArrayList queryFields = cannedQuery.getQueryFields();
        if (indicateRequiredFields == false) {
            DisplayNameList list = new DisplayNameList(queryFields);
            return list;
        } else {
            DisplayNameList list = new DisplayNameList();
            int numberOfQueryFields = queryFields.size();
            for (int i = 0; i < numberOfQueryFields; i++) {
                QueryField currentQueryField = (QueryField) queryFields.get(i);
                String displayName = currentQueryField.getName();
                if (currentQueryField.isRequired() == true) {
                    displayName = displayName.toUpperCase();
                }
                list.addItem(displayName, currentQueryField);
            }
            return list;
        }
    }

    /**
	* determines whether the data repository recognises a given user
	* @param user a user of a deployment that interacts with the service
	*/
    public boolean authenticateUser(User user) {
        return dataRepository.authenticateUser(user);
    }

    /**
	* 
	*/
    public PostProcessingService[] getPostProcessingServices() {
        return postProcessingServices;
    }

    private URL getURLFromFileName(String fileName, String fileDescription) throws MalformedURLException, PedroException {
        URL url = null;
        if ((fileName.startsWith("http:") == true) || (fileName.startsWith("ftp:") == true) || (fileName.startsWith("file:") == true)) {
            url = new URL(fileName);
        } else {
            File file = new File(fileName);
            if (file.exists() == false) {
                String errorMessage = BrowserServiceResources.getMessage("pierreService.api.nonExistentFile", file.getAbsolutePath());
                PedroException exception = new PedroException(errorMessage);
                throw exception;
            } else {
                url = file.toURL();
            }
        }
        return url;
    }

    /**
	* the execute method that is used by deployments that are 
	* submitting an expert search to the data repository
	*
	* @param queryLanguage the query language used to express
	* the expert query
	* @param freeTextQuery query typed by the end-user
	* (eg: an SQL query or an XQuery query)
	* @param deploymentForm describes the kind of deployment
	* that is using the service
	* @param user a user of a deployment that interacts with the service
	* @return report containing the results.
	*/
    public Report execute(String queryLanguage, String freeTextQuery, DeploymentForm deploymentForm, User user) {
        return (dataRepository.execute(queryLanguage, freeTextQuery, deploymentForm, user));
    }

    /**
	* the execute method that is used by deployments that are 
	* submitting a simple search to the data repository
	*
	* @param queryFeature the query feature object submitted by
	* the deployment.
	* @param deploymentForm describes the kind of deployment
	* that is using the service
	* @param user a user of a deployment that interacts with the service
	* @return report containing the results.
	*/
    public Report execute(QueryFeature queryFeature, DeploymentForm deploymentForm, User user) {
        return (dataRepository.execute(queryFeature, deploymentForm, user));
    }

    /**
	* the execute method that is used by deployments to submit a follow
	* on query represented by a report link.
	*
	* @param linkObject the link object containing parameters that
	* can be used to create the follow-on query.
	* @param sourceReport the report that contained the link that
	* is currently being executed.  This can be used by the data
	* repository to add some context to a follow-on query.
	* @param deploymentForm describes the kind of deployment
	* that is using the service
	* @param user a user of a deployment that interacts with the service
	* @return report containing the results.
	*/
    public Report execute(LinkObject linkObject, Report sourceReport, DeploymentForm deploymentForm, User user) {
        return (dataRepository.execute(linkObject, sourceReport, deploymentForm, user));
    }

    /**
	* saves the report to a file
	* @param file the file used to store the report
	* @param report a result report.
	*/
    public void saveResultsToFile(File file, Report report) throws Exception {
        String content = report.getPage();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(content);
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    /**
	* used to determine whether a feature can publish a record
	* or field for a given user.
	* @param recordClassName a record class defined in the XML schema.
	* @param fieldName a field class defined in the XML schema
	* @param user a user of a deployment that interacts with the service
	* @return true if a record or field can be made available in the
	* deployment; otherwise false.
	*/
    public boolean allowRecordStructure(String recordClassName, String fieldName, User user) {
        return securityService.canRead(recordClassName, fieldName, user);
    }

    private void printError(PedroException err) {
        System.out.println("ERROR:" + err.getMessage());
    }

    private void printError(String errorMessage) {
        System.out.println("ERROR:" + errorMessage);
    }

    private void checkEmptyProperty(String property) throws PedroException {
        if (property == null) {
            String errorMessage = BrowserServiceResources.getMessage("pierreService.api.nonExistentProperties", "pierreService.api.modelDirectory");
            PedroException exception = new PedroException(errorMessage);
            throw exception;
        }
    }

    private void checkNonExistentFile(File file) throws PedroException {
        if (file.exists() == false) {
            String errorMessage = BrowserServiceResources.getMessage("pierreService.api.nonExistentFile", file.getAbsolutePath());
            PedroException exception = new PedroException(errorMessage);
            throw exception;
        }
    }

    private void checkDirectoryStatus(File file) throws PedroException {
        if (file.isDirectory() == false) {
            String errorMessage = BrowserServiceResources.getMessage("pierre.api.nonExistentFile", file.getAbsolutePath());
            PedroException exception = new PedroException(errorMessage);
            throw exception;
        }
    }
}

package an.xacml.adapter.jaxb;

import static an.xml.XMLParserWrapper.getNodeXMLText;
import static an.xml.XMLParserWrapper.parse;
import static an.xml.XMLParserWrapper.verifySchemaFile;
import static deprecateed.an.xacml.adapter.file.XACMLParser.dumpPolicy;
import static deprecateed.an.xacml.adapter.file.XACMLParser.getPolicyDefaultSchema;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import org.w3c.dom.Element;
import an.config.ConfigElement;
import an.config.ConfigurationException;
import an.log.LogFactory;
import an.log.Logger;
import an.xacml.PolicySyntaxException;
import an.xacml.XACMLElement;
import an.xacml.engine.CacheManager;
import an.xacml.engine.DataStore;
import an.xacml.engine.DataStoreHelper;
import an.xacml.engine.PDP;
import an.xml.XMLGeneralException;
import deprecated.an.xacml.policy.AbstractPolicy;
import deprecateed.an.xacml.adapter.DataAdapter;
import deprecateed.an.xacml.adapter.DataAdapterException;

/**
 * The default implementation of DataStore, if "an.xacml.engine.DataStore"
 * is not configured, this defult DataStore will be used.
 */
public class JAXBDataStore implements DataStore {

    private String path;

    private String pattern;

    private Logger logger;

    private Map<URI, File> policyFilesByID = new Hashtable<URI, File>();

    public static final String ATTRIBUTE_POLICY_ID = "PolicyId";

    public static final String ATTRIBUTE_POLICYSET_ID = "PolicySetId";

    public static final String POLICY_FILE_PREFIX = "policy_";

    public static final String POLICY_FILE_SUFFIX = ".xml";

    public static final String ATTR_POLICY_PATH = "path";

    static final String ATTR_FILENAME_PATTERN = "pattern";

    public JAXBDataStore(ConfigElement config) {
        logger = LogFactory.getLogger();
        loadConfigurations(config);
    }

    protected void loadConfigurations(ConfigElement config) {
        path = (String) config.getAttributeValueByName(ATTR_POLICY_PATH);
        pattern = (String) config.getAttributeValueByName(ATTR_FILENAME_PATTERN);
    }

    /**
     * Load all policies from a specific path, return an Iterator of DataAdapter, caller may call 
     * DataAdapter.getEngineElement method to get the corresponding engine element which is going to be evaluated.
     * @throws ConfigurationException 
     */
    public DataAdapter[] load() throws DataAdapterException {
        try {
            policyFilesByID.clear();
            Vector<DataAdapter> adapters = new Vector<DataAdapter>();
            File dir = new File(path);
            if (dir.isDirectory()) {
                File[] policyFiles = dir.listFiles(new FilenameFilter() {

                    public boolean accept(File dir, String name) {
                        if (pattern == null || pattern.length() == 0) {
                            return true;
                        } else {
                            return name.matches(pattern);
                        }
                    }
                });
                System.out.println("Loading policies from '" + dir.toString() + "' ...");
                PDP currentPDP = DataStoreHelper.getPDP(this);
                String defaultSchema = verifySchemaFile(getPolicyDefaultSchema());
                InputStream in = null;
                int actualPolicyNum = 0;
                boolean warn = false;
                boolean error = false;
                System.gc();
                long memBegin = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long begin = System.currentTimeMillis();
                for (int i = 0; i < policyFiles.length; i++) {
                    try {
                        in = new FileInputStream(policyFiles[i]);
                        Element root = parse(in, defaultSchema);
                        URI policyId = getPolicyOrPolicySetId(root);
                        if (policyFilesByID.get(policyId) != null) {
                            throw new DataAdapterException("The policy loaded from '" + policyFiles[i] + "' with ID<" + policyId + "> already exists.");
                        }
                        policyFilesByID.put(policyId, policyFiles[i]);
                        DataAdapter da = createPolicyDataAdapterFromXMLElement(root);
                        ((AbstractPolicy) da.getEngineElement()).setOwnerPDP(currentPDP);
                        adapters.add(da);
                        actualPolicyNum++;
                        if (actualPolicyNum % 10 == 0) {
                            System.out.print(".");
                        }
                        if (actualPolicyNum % 1000 == 0 && i < policyFiles.length - 1) {
                            System.out.println();
                        }
                        if (logger.isDebugEnabled()) {
                            try {
                                XACMLElement engineElem = da.getEngineElement();
                                Constructor<?> cons = da.getClass().getConstructor(XACMLElement.class);
                                DataAdapter daGenFromEngineElem = (DataAdapter) cons.newInstance(engineElem);
                                logger.debug("Dump policy loaded from '" + policyFiles[i] + "': " + getNodeXMLText((Element) daGenFromEngineElem.getDataStoreObject()));
                            } catch (Exception debugEx) {
                                logger.debug("Dump policy failed due to: ", debugEx);
                            }
                        }
                    } catch (Exception e) {
                        if (e instanceof XMLGeneralException || e instanceof PolicySyntaxException) {
                            logger.warn("Could not load file '" + policyFiles[i] + "' since it should not be a valid policy file.", e);
                            warn = true;
                        } else if (e instanceof DataAdapterException) {
                            throw e;
                        } else {
                            logger.error("Error occurs when parsing policy file : " + policyFiles[i], e);
                            error = true;
                        }
                    } finally {
                        try {
                            in.close();
                        } catch (Exception ex) {
                        }
                    }
                }
                if (actualPolicyNum > 0) {
                    long end = System.currentTimeMillis();
                    System.gc();
                    long memEnd = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    System.out.println("\n" + actualPolicyNum + " policies loaded. " + "Time elapsed " + (end - begin) / 1000 + " second. " + "Memory used " + (memEnd - memBegin) / 1024 / 1024 + " MB.");
                    if (error || warn) {
                        System.out.println("There are " + (error ? "errors" : "warnings") + " occur while loading policies, please check log file for details.");
                    }
                }
            }
            return adapters.toArray(new DataAdapter[0]);
        } catch (DataAdapterException daEx) {
            throw daEx;
        } catch (Exception ex) {
            throw new DataAdapterException("Failed to get default policy schema from configuration.", ex);
        }
    }

    /**
     * Get policy id if parsed Element is a Policy, or get policySet id if parsed Element is a PolicySet.
     * There should not have multiple policies or policySets in a single file.
     * @param element
     * @return
     * @throws URISyntaxException
     * @throws PolicySyntaxException 
     */
    private URI getPolicyOrPolicySetId(Element element) throws URISyntaxException, PolicySyntaxException {
        String id = element.getAttribute(ATTRIBUTE_POLICY_ID);
        if (id == null || "".equals(id)) {
            id = element.getAttribute(ATTRIBUTE_POLICYSET_ID);
        }
        if (id == null || "".equals(id)) {
            throw new PolicySyntaxException("The element '" + element.getLocalName() + "' doesn't include PolicyId or PolicySetId attribute.");
        }
        return new URI(id);
    }

    /**
     * Save all engine elements that current in Cache to underlying data store
     */
    public void save() throws DataAdapterException {
        File policyFile = null;
        try {
            String schema = getPolicyDefaultSchema();
            schema = verifySchemaFile(schema);
            PDP currentPDP = DataStoreHelper.getPDP(this);
            CacheManager cacheMgr = CacheManager.getInstance(currentPDP);
            AbstractPolicy[] allPolicies = cacheMgr.getAllPolicies();
            for (int i = 0; i < allPolicies.length; i++) {
                AbstractPolicy policy = allPolicies[i];
                policyFile = policyFilesByID.get(policy.getId());
                if (policyFile == null) {
                    policyFile = new File(POLICY_FILE_PREFIX + policy.getId() + POLICY_FILE_SUFFIX);
                }
                dumpPolicy(policy, new FileOutputStream(policyFile));
            }
        } catch (Exception e) {
            throw new DataAdapterException("Error occurs when saving policy file" + (policyFile == null ? "." : " : '" + policyFile.getName() + "'"), e);
        }
    }

    /**
     * Only update some of policies in data store. The new added policies are included in "toBeUpdated".
     * "toBeDeleted" can only include IDs of policies.
     */
    public void update(DataAdapter[] toBeUpdated, DataAdapter[] toBeDeleted) throws DataAdapterException {
    }

    public void shutdown() {
        policyFilesByID.clear();
    }
}

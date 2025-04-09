package WebServices;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.User;
import javax.security.auth.x500.X500PrivateCredential;
import org.apache.commons.io.IOUtils;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.infomodel.AssociationImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import org.freebxml.omar.client.xml.registry.infomodel.SlotImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.client.xml.registry.util.SecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Server
 */
public class CCRepositoryProxyImpl {

    protected final String SLOT_UNIQUE_ID = "Unique ID";

    protected final String SLOT_ENTRY_TYPE = "Entry Type";

    protected final String SLOT_OBJECT_CLASS_TERM_QUALIFIER = "Object Class Term Qualifier";

    protected final String SLOT_OBJECT_CLASS_TERM = "Object Class Term";

    protected final String SLOT_PROPERTY_TERM_QUALIFIER = "Property Term Qualifier";

    protected final String SLOT_PROPERTY_TERM = "Property Term";

    protected final String SLOT_ASSOCIATED_OBJECT_CLASS_TERM_QUALIFIER = "Associated Object Class Term Qualifier";

    protected final String SLOT_ASSOCIATED_OBJECT_CLASS_TERM = "Associated Object Class Term";

    protected final String SLOT_MIN_OCCURENCE = "Min Occurence";

    protected final String SLOT_MAX_OCCURENCE = "Max Occurence";

    protected final String SLOT_BUSINESS_TERMS = "Business Terms";

    protected final String SLOT_BUSINESS_PROCESS_ROLE = "Business Process Role Context Value";

    protected final String SLOT_OFFICIAL_CONSTRAINTS = "Official Constraints Context Value";

    protected final String SLOT_BUSINESS_PROCESS = "Business Process Context Value";

    protected final String SLOT_PRODUCT = "Product Context Value";

    protected final String SLOT_SUPPORTING_ROLE = "Supporting Role Context Value";

    protected final String SLOT_GEOPOLITICAL = "Geopolitical Context Value";

    protected final String SLOT_SYSTEM_CAPABILITIES = "System Capabilities Context Value";

    protected final String SLOT_INDUSTRY = "Industry Context Value";

    protected final String SLOT_CORE_COMPONENT_TYPE = "Core Component Type";

    protected final String SLOT_REPRESENTATION_TERM = "Representation Term";

    protected final String SLOT_QUALIFIED_DATA_TYPE_UNIQUE_ID = "Qualified Data Type Unique ID";

    protected final String SLOT_DATA_TYPE_QUALIFIER = "Data Type Qualifier";

    protected final String SLOT_DATA_TYPE = "Data Type";

    protected final String SLOT_ENUMERATION = "Enumeration";

    protected final String SLOT_PRIMITIVE_TYPE = "Primitive Type";

    protected final String TAG_BUSINESS_INFORMATION_ENTITY = "BusinessInformationEntity";

    protected final String TAG_CORE_COMPONENT = "CoreComponent";

    protected final String TAG_QUALIFIED_DATA_TYPE_UNIQUE_ID = "QualifiedDataTypeUniqueID";

    protected final String TAG_DATA_TYPE = "DataType";

    protected final String TAG_UNIQUE_ID = "UniqueID";

    protected final String TAG_ENTRY_TYPE = "EntryType";

    protected final String TAG_DEN = "DictionaryEntryName";

    protected final String TAG_DEFINITION = "Definition";

    protected final String TAG_OBJECT_CLASS_TERM_QUALIFIER = "ObjectClassTermQualifier";

    protected final String TAG_OBJECT_CLASS_TERM = "ObjectClassTerm";

    protected final String TAG_PROPERTY_TERM_QUALIFIER = "PropertyTermQualifier";

    protected final String TAG_PROPERTY_TERM = "PropertyTerm";

    protected final String TAG_ASSOCIATED_OBJECT_CLASS_TERM_QUALIFIER = "AssociatedObjectClassTermQualifier";

    protected final String TAG_ASSOCIATED_OBJECT_CLASS_TERM = "AssociatedObjectClassTerm";

    protected final String TAG_MIN_OCCURENCE = "MinOccurence";

    protected final String TAG_MAX_OCCURENCE = "MaxOccurence";

    protected final String TAG_BUSINESS_TERMS = "BusinessTerms";

    protected final String TAG_BUSINESS_PROCESS_ROLE = "RoleContext";

    protected final String TAG_OFFICIAL_CONSTRAINTS = "OfficialConstraintsContext";

    protected final String TAG_BUSINESS_PROCESS = "BusinessProcessContext";

    protected final String TAG_PRODUCT = "ProductContext";

    protected final String TAG_SUPPORTING_ROLE = "SupportingRoleContext";

    protected final String TAG_GEOPOLITICAL = "GeopoliticalContext";

    protected final String TAG_SYSTEM_CAPABILITIES = "SystemConstraintsContext";

    protected final String TAG_INDUSTRY = "IndustryContext";

    protected final String TAG_CORE_COMPONENT_TYPE = "CoreComponentType";

    protected final String TAG_REPRESENTATION_TERM = "RepresentationTerm";

    protected final String TAG_DATA_TYPE_QUALIFIER = "DataTypeQualifier";

    protected final String TAG_ENUMERATION = "Enumeration";

    protected final String TAG_PRIMITIVE_TYPE = "PrimitiveType";

    private String xmlString;

    private Connection connection;

    private RegistryService registryService;

    private BusinessLifeCycleManager lcm;

    private BusinessQueryManager bqm;

    private DeclarativeQueryManager dqm;

    private String[] results;

    private DocumentBuilderFactory dbfac;

    private DocumentBuilder docBuilder;

    private Document doc;

    private Element root;

    private String USER_NAME;

    private String PASS;

    protected final String ASSOC_TYPE_EXTENDS = "urn:oasis:names:tc:ebxml-regrep:AssociationType:Extends";

    protected final String ASSOC_TYPE_CONTAINS = "urn:oasis:names:tc:ebxml-regrep:AssociationType:Contains";

    protected final String ASSOC_TYPE_RELATEDTO = "urn:oasis:names:tc:ebxml-regrep:AssociationType:RelatedTo";

    protected final String filePath = "C:\\Deneme\\CCRepositoryProxyDist\\sil.xml";

    protected final String jarPath = "\"C:\\Documents and Settings\\Server\\Belgelerim\\NetBeansProjects\\CCRepositoryProxy\\dist\\CCRepositoryProxy.jar\"";

    protected final String iSurfPath = "C:\\ApacheSoftwareFoundation\\Tomcat5.0\\webapps\\ROOT\\iSurf\\generated";

    protected final String savePath = "C:\\ApacheSoftwareFoundation\\Tomcat5.0\\webapps\\ROOT\\iSurf\\";

    private String init(String userName, String pass, boolean setCredentials) {
        try {
            Properties properties = new Properties();
            properties.setProperty("javax.xml.registry.queryManagerURL", "http://localhost:8080/omar/registry/soap");
            ConnectionFactory cf = JAXRUtility.getConnectionFactory();
            cf.setProperties(properties);
            connection = cf.createConnection();
            connection.setSynchronous(true);
            if (setCredentials) {
                try {
                    SecurityUtil securityUtil = SecurityUtil.getInstance();
                    HashSet credentials = new HashSet();
                    X500PrivateCredential x500 = securityUtil.aliasToX500PrivateCredential(userName, pass);
                    credentials.add(securityUtil.aliasToX500PrivateCredential(userName, pass));
                    connection.setCredentials(credentials);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return "login failed";
                }
            }
            registryService = connection.getRegistryService();
            lcm = registryService.getBusinessLifeCycleManager();
            if (!setCredentials) {
                bqm = registryService.getBusinessQueryManager();
                dqm = registryService.getDeclarativeQueryManager();
            }
            return "login succesful";
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Web service operation
     */
    public String login(String username, String pass) {
        return init(username, pass, true);
    }

    /**
     * Web service operation
     */
    public String saveXSD(String messageName, String messageContent, String cac, String cbc) throws InterruptedException {
        String dirName = null;
        try {
            init(null, null, false);
            messageName = messageName.replace('.', ' ').replaceAll(" ", "");
            String target = savePath + messageName + ".zip";
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));
            messageContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + messageContent;
            cbc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + cbc;
            cac = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + cac;
            addFileToZip(out, "common/UBL-CommonAggregateComponents-2.0.xsd", cac);
            addFileToZip(out, "common/UBL-CommonBasicComponents-2.0.xsd", cbc);
            addExistingFileToZip(out, "common/CodeList_CurrencyCode_ISO_7_04.xsd", "C:\\ApacheSoftwareFoundation\\Tomcat5.0\\webapps\\ROOT\\iSurf\\generated\\common\\CodeList_CurrencyCode_ISO_7_04.xsd");
            addExistingFileToZip(out, "common/CodeList_LanguageCode_ISO_7_04.xsd", "C:\\ApacheSoftwareFoundation\\Tomcat5.0\\webapps\\ROOT\\iSurf\\generated\\common\\CodeList_LanguageCode_ISO_7_04.xsd");
            addExistingFileToZip(out, "common/CodeList_MIMEMediaTypeCode_IANA_7_04.xsd", "C:\\ApacheSoftwareFoundation\\Tomcat5.0\\webapps\\ROOT\\iSurf\\generated\\common\\CodeList_MIMEMediaTypeCode_IANA_7_04.xsd");
            addExistingFileToZip(out, "common/CodeList_UnitCode_UNECE_7_04.xsd", "C:\\ApacheSoftwareFoundation\\Tomcat5.0\\webapps\\ROOT\\iSurf\\generated\\common\\CodeList_UnitCode_UNECE_7_04.xsd");
            addExistingFileToZip(out, "common/UBL-QualifiedDatatypes-2.0.xsd", "C:\\ApacheSoftwareFoundation\\Tomcat5.0\\webapps\\ROOT\\iSurf\\generated\\common\\UBL-QualifiedDatatypes-2.0.xsd");
            addExistingFileToZip(out, "common/UnqualifiedDataTypeSchemaModule-2.0.xsd", "C:\\ApacheSoftwareFoundation\\Tomcat5.0\\webapps\\ROOT\\iSurf\\generated\\common\\UnqualifiedDataTypeSchemaModule-2.0.xsd");
            addFileToZip(out, "maindoc/" + messageName + ".xsd", messageContent);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return "failed";
        }
        System.out.println("bitti");
        return "http://144.122.230.120:8080/iSurf/" + messageName + ".zip";
    }

    private void addExistingFileToZip(ZipOutputStream out, String entryName, String filePath) {
        if (filePath != null) {
            byte[] buf = new byte[1024];
            try {
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                out.putNextEntry(new ZipEntry(entryName));
                int data;
                while ((data = br.read()) != -1) {
                    out.write(data);
                }
                out.closeEntry();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addFileToZip(ZipOutputStream out, String fn, String content) {
        if (fn != null) {
            byte[] buf = new byte[1024];
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
                out.putNextEntry(new ZipEntry(fn));
                int len;
                while ((len = bais.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                bais.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Web service operation
     */
    public String isDuplicate(String[] toBeChecked) {
        init(null, null, false);
        for (int i = 0; i < toBeChecked.length; i++) {
            if (getEObyName(toBeChecked[i]) != null) {
                return toBeChecked[i];
            }
        }
        return "no";
    }

    /**
     * Web service operation
     */
    public void removeUIDSlot(String userName, String pass) {
        try {
            init(userName, pass, true);
            String qString = "SELECT user_ FROM user_ WHERE personname_firstname='" + userName + "'";
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] UserList = dqm.executeQuery(qry).getCollection().toArray();
            System.out.println(UserList.length);
            if (UserList.length == 1) {
                User user = (User) UserList[0];
                if (user.getSlot("UID Iterator") == null) {
                    return;
                }
                user.removeSlot("UID Iterator");
                Collection cllc = new ArrayList();
                cllc.add(user);
                lcm.saveObjects(cllc);
            }
        } catch (InvalidRequestException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Web service operation
     */
    public String getUIDIterator(String userName, String pass, int entityCount) throws RemoteException {
        try {
            init(userName, pass, false);
            String qString = "SELECT user_ FROM user_ WHERE user_.personname_firstname='" + userName + "'";
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] UserList = dqm.executeQuery(qry).getCollection().toArray();
            System.out.println(UserList.length);
            if (UserList.length == 1) {
                User user = (User) UserList[0];
                Slot uidSlot = null;
                uidSlot = user.getSlot("UID Iterator");
                String value = (String) uidSlot.getValues().toArray()[0];
                String[] cmd = new String[7];
                cmd[0] = "java";
                cmd[1] = "-jar";
                cmd[2] = jarPath;
                cmd[3] = "3";
                cmd[4] = userName;
                cmd[5] = pass;
                cmd[6] = String.valueOf(entityCount);
                Process p = null;
                try {
                    p = Runtime.getRuntime().exec(cmd);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    System.out.println("Process result: " + p.waitFor());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return value;
            }
        } catch (InvalidRequestException ex) {
            ex.printStackTrace();
        } catch (JAXRException ex) {
            ex.printStackTrace();
        }
        System.out.println("Succes");
        return "Succes";
    }

    /**
     * Web service operation
     */
    public String[] queryASCCByKeyword(String keyword) {
        try {
            init(null, null, false);
            String entryType = "ASCCP";
            String qString = "SELECT DISTINCT eo.* FROM Extrinsicobject eo WHERE ";
            qString += "id IN (SELECT parent FROM NAME_ nm WHERE upper(nm.value) LIKE upper('%" + keyword + "%')) AND ";
            qString += "id IN (SELECT parent FROM SLOT sl1 WHERE sl1.name_='Entry Type' AND sl1.value='" + entryType + "')";
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] EOlist = dqm.executeQuery(qry).getCollection().toArray();
            System.out.println(EOlist.length);
            results = new String[EOlist.length];
            for (int i = 0; i != EOlist.length; i++) {
                results[i] = parseToXML((ExtrinsicObject) EOlist[i]);
            }
        } catch (InvalidRequestException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    /**
     * Web service operation
     */
    public String[] queryBCCByKeyword(String keyword) {
        try {
            init(null, null, false);
            String entryType = "BCCP";
            String qString = "SELECT DISTINCT eo.* FROM Extrinsicobject eo WHERE ";
            qString += "id IN (SELECT parent FROM NAME_ nm WHERE upper(nm.value) LIKE upper('%" + keyword + "%')) AND ";
            qString += "id IN (SELECT parent FROM SLOT sl1 WHERE sl1.name_='Entry Type' AND sl1.value='" + entryType + "')";
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] EOlist = dqm.executeQuery(qry).getCollection().toArray();
            System.out.println(EOlist.length);
            results = new String[EOlist.length];
            for (int i = 0; i != EOlist.length; i++) {
                results[i] = parseToXML((ExtrinsicObject) EOlist[i]);
            }
        } catch (InvalidRequestException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    /**
     * Web service operation
     */
    public String[] queryAllACC() {
        try {
            init(null, null, false);
            String qString = "SELECT eo.* FROM Extrinsicobject eo WHERE ";
            qString += "id IN (SELECT sl1.parent FROM SLOT sl1 WHERE sl1.value='ACC' AND sl1.name='" + SLOT_ENTRY_TYPE + "' )";
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] EOlist = dqm.executeQuery(qry).getCollection().toArray();
            System.out.println(EOlist.length);
            results = new String[EOlist.length];
            for (int i = 0; i != EOlist.length; i++) {
                results[i] = parseToXML((ExtrinsicObject) EOlist[i]);
            }
        } catch (InvalidRequestException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    /**
     * Web service operation
     */
    public String[] queryACCByNameDescriptionAndBusinessTerms(String name, String[] desc, String objectClassTerm, String[] businessTerms) {
        try {
            init(null, null, false);
            String entryType = "ACC";
            name = "%" + name + "%";
            desc = appendLike(desc);
            objectClassTerm = "%" + objectClassTerm + "%";
            businessTerms = appendLike(businessTerms);
            boolean businessTermsFlag = false;
            boolean descriptionFlag = false;
            if (businessTerms.length > 0) {
                businessTermsFlag = true;
            }
            if (desc.length > 0) {
                descriptionFlag = true;
            }
            String qString = "SELECT eo.* FROM Extrinsicobject eo WHERE ";
            qString += "id IN (SELECT nm.parent FROM NAME_ nm WHERE upper(nm.value) LIKE upper('" + name + "')) AND ";
            qString += "id IN (SELECT sl1.parent FROM SLOT sl1 WHERE sl1.value='" + entryType + "' AND sl1.name='" + SLOT_ENTRY_TYPE + "' ) AND ";
            if (!objectClassTerm.contentEquals("%null%")) {
                qString += "id IN (SELECT sl1.parent FROM SLOT sl1 WHERE upper(sl1.value) LIKE upper('" + objectClassTerm + "') AND sl1.name='" + SLOT_OBJECT_CLASS_TERM + "' ) AND ";
            }
            if (qString.endsWith("AND ")) {
                qString = qString.substring(0, qString.length() - 4);
                qString += " ";
            }
            if (descriptionFlag) {
                qString += "AND (";
                for (int i = 0; i != desc.length; i++) {
                    qString += " id IN (SELECT descr.parent FROM DESCRIPTION descr WHERE upper(descr.value) LIKE upper('" + desc[i] + "')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (businessTermsFlag) {
                qString += " AND (";
                for (int i = 0; i != businessTerms.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Business Terms' AND upper(slot1.value) LIKE upper('" + businessTerms[i] + "')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            System.out.println(qString);
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] EOlist = dqm.executeQuery(qry).getCollection().toArray();
            System.out.println(EOlist.length);
            results = new String[EOlist.length];
            for (int i = 0; i != EOlist.length; i++) {
                results[i] = parseToXML((ExtrinsicObject) EOlist[i]);
            }
        } catch (InvalidRequestException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    /**
     * Web service operation
     */
    public String[] queryBMT(String[] officialContext, String[] businessProcessContext, String[] systemConstraintsContext, String[] productContext, String[] industryContext, String[] geopoliticalContext, String[] supportingRoleContext, String[] roleContext) throws RemoteException {
        try {
            init(null, null, false);
            officialContext = appendLike(officialContext);
            businessProcessContext = appendLike(businessProcessContext);
            systemConstraintsContext = appendLike(systemConstraintsContext);
            productContext = appendLike(productContext);
            industryContext = appendLike(industryContext);
            geopoliticalContext = appendLike(geopoliticalContext);
            supportingRoleContext = appendLike(supportingRoleContext);
            roleContext = appendLike(roleContext);
            boolean officialContexFlag = false;
            boolean businessProcessContexFlag = false;
            boolean systemConstraintsContexFlag = false;
            boolean productContexFlag = false;
            boolean industryContexFlag = false;
            boolean geopoliticalContexFlag = false;
            boolean supportingRoleContexFlag = false;
            boolean roleContexFlag = false;
            if (officialContext.length > 0) {
                officialContexFlag = true;
            }
            if (businessProcessContext.length > 0) {
                businessProcessContexFlag = true;
            }
            if (systemConstraintsContext.length > 0) {
                systemConstraintsContexFlag = true;
            }
            if (productContext.length > 0) {
                productContexFlag = true;
            }
            if (industryContext.length > 0) {
                industryContexFlag = true;
            }
            if (geopoliticalContext.length > 0) {
                geopoliticalContexFlag = true;
            }
            if (supportingRoleContext.length > 0) {
                supportingRoleContexFlag = true;
            }
            if (roleContext.length > 0) {
                roleContexFlag = true;
            }
            if (systemConstraintsContext.length > 0) {
                systemConstraintsContexFlag = true;
            }
            String qString = "SELECT eo.* FROM EXTRINSICOBJECT eo WHERE id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Entry Type' AND slot1.value='BMT' )";
            if (officialContexFlag) {
                qString += " AND (";
                for (int i = 0; i != officialContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Official Constraints Context Value' AND ((upper(slot1.value) LIKE upper('" + officialContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (businessProcessContexFlag) {
                qString += " AND (";
                System.out.println("Contexts array length: " + businessProcessContext.length);
                for (int i = 0; i != businessProcessContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Business Process Context Value' AND ((upper(slot1.value) LIKE upper('" + businessProcessContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (productContexFlag) {
                qString += " AND (";
                for (int i = 0; i != productContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Product Context Value' AND ((upper(slot1.value) LIKE upper('" + productContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (industryContexFlag) {
                qString += " AND (";
                for (int i = 0; i != industryContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Industry Context Value' AND ((upper(slot1.value) LIKE upper('" + industryContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (geopoliticalContexFlag) {
                qString += " AND (";
                for (int i = 0; i != geopoliticalContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Geopolitical Context Value' AND ((upper(slot1.value) LIKE upper('" + geopoliticalContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (roleContexFlag) {
                qString += " AND (";
                for (int i = 0; i != roleContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Business Process Role Context Value' AND ((upper(slot1.value) LIKE upper('" + roleContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (supportingRoleContexFlag) {
                qString += " AND (";
                for (int i = 0; i != supportingRoleContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Supporting Role Context Value' AND ((upper(slot1.value) LIKE upper('" + supportingRoleContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (systemConstraintsContexFlag) {
                qString += " AND (";
                for (int i = 0; i != systemConstraintsContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='System Capabilities Context Value' AND ((upper(slot1.value) LIKE upper('" + systemConstraintsContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            System.out.println(qString);
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] EOlist = dqm.executeQuery(qry).getCollection().toArray();
            System.out.println(EOlist.length);
            if (EOlist.length > 50) {
                results = new String[1];
                results[0] = "Too many results";
                return results;
            }
            results = new String[EOlist.length];
            for (int i = 0; i != EOlist.length; i++) {
                results[i] = parseToXML((ExtrinsicObject) EOlist[i]);
            }
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    /**
     * Web service operation
     */
    public String[] queryBIEByNameDescriptionAndContext(String name, String[] desc, String objectClassTermQualifier, String objectClassTerm, String[] businessTerms, String[] officialContext, String[] businessProcessContext, String[] systemConstraintsContext, String[] productContext, String[] industryContext, String[] geopoliticalContext, String[] supportingRoleContext, String[] roleContext) {
        try {
            init(null, null, false);
            String entryType = "ABIE";
            name = "%" + name + "%";
            desc = appendLike(desc);
            objectClassTermQualifier = "%" + objectClassTermQualifier + "%";
            businessTerms = appendLike(businessTerms);
            officialContext = appendLike(officialContext);
            businessProcessContext = appendLike(businessProcessContext);
            systemConstraintsContext = appendLike(systemConstraintsContext);
            productContext = appendLike(productContext);
            industryContext = appendLike(industryContext);
            geopoliticalContext = appendLike(geopoliticalContext);
            supportingRoleContext = appendLike(supportingRoleContext);
            roleContext = appendLike(roleContext);
            boolean objectClassTermFlag = false;
            boolean descriptionFlag = false;
            boolean businessTermsFlag = false;
            boolean officialContexFlag = false;
            boolean businessProcessContexFlag = false;
            boolean systemConstraintsContexFlag = false;
            boolean productContexFlag = false;
            boolean industryContexFlag = false;
            boolean geopoliticalContexFlag = false;
            boolean supportingRoleContexFlag = false;
            boolean roleContexFlag = false;
            if (!objectClassTerm.contentEquals("")) {
                objectClassTermFlag = true;
            }
            if (desc.length > 0) {
                descriptionFlag = true;
            }
            if (businessTerms.length > 0) {
                businessTermsFlag = true;
            }
            if (officialContext.length > 0) {
                officialContexFlag = true;
            }
            if (businessProcessContext.length > 0) {
                businessProcessContexFlag = true;
            }
            if (systemConstraintsContext.length > 0) {
                systemConstraintsContexFlag = true;
            }
            if (productContext.length > 0) {
                productContexFlag = true;
            }
            if (industryContext.length > 0) {
                industryContexFlag = true;
            }
            if (geopoliticalContext.length > 0) {
                geopoliticalContexFlag = true;
            }
            if (supportingRoleContext.length > 0) {
                supportingRoleContexFlag = true;
            }
            if (roleContext.length > 0) {
                roleContexFlag = true;
            }
            if (systemConstraintsContext.length > 0) {
                systemConstraintsContexFlag = true;
            }
            String qString = "SELECT eo.* FROM EXTRINSICOBJECT eo WHERE";
            qString += " id IN (SELECT nm.parent FROM NAME_ nm WHERE upper(nm.value) LIKE upper('" + name + "')) ";
            if (descriptionFlag) {
                qString += "AND (";
                for (int i = 0; i != desc.length; i++) {
                    qString += " id IN (SELECT descr.parent FROM DESCRIPTION descr WHERE upper(descr.value) LIKE upper('" + desc[i] + "')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            qString += " AND id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Entry Type' AND slot1.value='" + entryType + "')";
            qString += " AND id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Object Class Term Qualifier' AND upper(slot1.value) LIKE upper('" + objectClassTermQualifier + "'))";
            if (objectClassTermFlag) {
                qString += " AND id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Object Class Term' AND slot1.value='" + objectClassTerm + "')";
            }
            if (businessTermsFlag) {
                qString += " AND (";
                for (int i = 0; i != businessTerms.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Business Terms' AND upper(slot1.value) LIKE upper('" + businessTerms[i] + "')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (officialContexFlag) {
                qString += " AND (";
                for (int i = 0; i != officialContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Official Constraints Context Value' AND ((upper(slot1.value) LIKE upper('" + officialContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (businessProcessContexFlag) {
                qString += " AND (";
                for (int i = 0; i != businessProcessContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Business Process Context Value' AND ((upper(slot1.value) LIKE upper('" + businessProcessContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (productContexFlag) {
                qString += " AND (";
                for (int i = 0; i != productContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Product Context Value' AND ((upper(slot1.value) LIKE upper('" + productContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (industryContexFlag) {
                qString += " AND (";
                for (int i = 0; i != industryContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Industry Context Value' AND ((upper(slot1.value) LIKE upper('" + industryContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (geopoliticalContexFlag) {
                qString += " AND (";
                for (int i = 0; i != geopoliticalContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Geopolitical Context Value' AND ((upper(slot1.value) LIKE upper('" + geopoliticalContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (roleContexFlag) {
                qString += " AND (";
                for (int i = 0; i != businessProcessContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Business Process Role Context Value' AND ((upper(slot1.value) LIKE upper('" + roleContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (supportingRoleContexFlag) {
                qString += " AND (";
                for (int i = 0; i != supportingRoleContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Supporting Role Context Value' AND ((upper(slot1.value) LIKE upper('" + supportingRoleContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            if (systemConstraintsContexFlag) {
                qString += " AND (";
                for (int i = 0; i != systemConstraintsContext.length; i++) {
                    qString += " id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='System Capabilities Context Value' AND ((upper(slot1.value) LIKE upper('" + systemConstraintsContext[i] + "')) OR slot1.value='In All Contexts')) OR";
                }
                qString = qString.substring(0, qString.length() - 2);
                qString += ")";
            }
            System.out.println(qString);
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] EOlist = dqm.executeQuery(qry).getCollection().toArray();
            System.out.println(EOlist.length);
            if (EOlist.length > 50) {
                results = new String[1];
                results[0] = "Too many results";
                return results;
            }
            results = new String[EOlist.length];
            for (int i = 0; i != EOlist.length; i++) {
                results[i] = parseToXML((ExtrinsicObject) EOlist[i]);
            }
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    /**
     * Web service operation
     */
    public String queryByID(String ID, String entryType) {
        try {
            init(null, null, false);
            String qString = "SELECT DISTINCT eo.* FROM EXTRINSICOBJECT eo WHERE eo.lid='" + ID + "'";
            qString += " AND id IN (SELECT slot1.parent FROM SLOT slot1 WHERE slot1.name_='Entry Type' AND slot1.value='" + entryType + "')";
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] EOlist = dqm.executeQuery(qry).getCollection().toArray();
            System.out.println(EOlist.length);
            if (EOlist.length > 0) {
                return parseToXML((ExtrinsicObject) EOlist[0]);
            } else {
                return null;
            }
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Web service operation
     */
    public void saveACC2Registry(String str, String userName, String pass) {
        writeToFile(str);
        String[] cmd = new String[6];
        cmd[0] = "java";
        cmd[1] = "-jar";
        cmd[2] = jarPath;
        cmd[3] = "0";
        cmd[4] = userName;
        cmd[5] = pass;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            System.out.println("Process result: " + p.waitFor());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Web service operation
     */
    public void saveBMT2Registry(String str, String userName, String pass) throws RemoteException {
        writeToFile(str);
        String[] cmd = new String[6];
        cmd[0] = "java";
        cmd[1] = "-jar";
        cmd[2] = jarPath;
        cmd[3] = "2";
        cmd[4] = userName;
        cmd[5] = pass;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            System.out.println("Process result: " + p.waitFor());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Web service operation
     */
    public void saveABIE2Registry(String str, String userName, String pass) {
        try {
            xmlString = str;
            dbfac = DocumentBuilderFactory.newInstance();
            docBuilder = dbfac.newDocumentBuilder();
            doc = docBuilder.parse(new InputSource(new StringReader(xmlString)));
            root = doc.getDocumentElement();
            Node iter = root.getFirstChild();
            System.out.println("Entering: Save ABIE");
            while (iter != null) {
                if (!iter.hasChildNodes()) {
                    iter = iter.getNextSibling();
                    continue;
                }
                String type = ((Element) iter).getElementsByTagName(TAG_ENTRY_TYPE).item(0).getTextContent();
                if (type.contentEquals("ABIE")) {
                    System.out.println("Saving: ABIE");
                    String DEN = DEN = iter.getChildNodes().item(5).getTextContent();
                    String newXMLString;
                    int index = xmlString.indexOf(DEN);
                    index = xmlString.indexOf("ABIE", index);
                    if (index != -1) {
                        newXMLString = xmlString.substring(0, index);
                        newXMLString = newXMLString.substring(0, newXMLString.lastIndexOf("<BusinessInformationEntity>"));
                        newXMLString += "</root>";
                    } else {
                        newXMLString = xmlString;
                    }
                    String fileString = newXMLString;
                    writeToFile(fileString);
                    String[] cmd = new String[6];
                    cmd[0] = "java";
                    cmd[1] = "-jar";
                    cmd[2] = jarPath;
                    cmd[3] = "1";
                    cmd[4] = userName;
                    cmd[5] = pass;
                    Process p = null;
                    try {
                        p = Runtime.getRuntime().exec(cmd);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    try {
                        System.out.println("Process result: " + p.waitFor());
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("Saved: ABIE");
                }
                iter = iter.getNextSibling();
            }
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    private String[] appendLike(String[] strList) {
        for (int i = 0; i != strList.length; i++) {
            strList[i] = "%" + strList[i] + "%";
        }
        return strList;
    }

    private String parseToXML(ExtrinsicObject eo) {
        try {
            DataHandler dh = eo.getRepositoryItem();
            if (dh == null) {
                return "null";
            }
            InputStream is = dh.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            return IOUtils.toString(br);
        } catch (IOException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void saveBMT(Element btmn) {
        try {
            String uniqueID;
            String dictionaryEntryName;
            String definition;
            String objectClassTermQualifier;
            String objectClassTerm;
            ArrayList ABIES = new ArrayList();
            String businessProcessContextValue;
            String productContextValue;
            String industryContextValue;
            String geopoliticalContextValue;
            String officialConstraintsContextValue;
            String businessProcessRoleContextValue;
            String supportingRoleContextValue;
            String systemCapabilitiesContextValue;
            NodeList nl;
            uniqueID = btmn.getElementsByTagName(TAG_UNIQUE_ID).item(0).getTextContent();
            dictionaryEntryName = btmn.getElementsByTagName(TAG_DEN).item(0).getTextContent();
            definition = btmn.getElementsByTagName(TAG_DEFINITION).item(0).getTextContent();
            objectClassTermQualifier = btmn.getElementsByTagName(TAG_OBJECT_CLASS_TERM_QUALIFIER).item(0).getTextContent();
            objectClassTerm = btmn.getElementsByTagName(TAG_OBJECT_CLASS_TERM).item(0).getTextContent();
            nl = ((Element) (btmn.getElementsByTagName("ABIES").item(0))).getElementsByTagName("ABIE");
            for (int i = 0; i < nl.getLength(); i++) {
                ABIES.add(nl.item(i).getTextContent());
            }
            businessProcessContextValue = btmn.getElementsByTagName(TAG_BUSINESS_PROCESS).item(0).getTextContent();
            productContextValue = btmn.getElementsByTagName(TAG_PRODUCT).item(0).getTextContent();
            industryContextValue = btmn.getElementsByTagName(TAG_INDUSTRY).item(0).getTextContent();
            geopoliticalContextValue = btmn.getElementsByTagName(TAG_GEOPOLITICAL).item(0).getTextContent();
            officialConstraintsContextValue = btmn.getElementsByTagName(TAG_OFFICIAL_CONSTRAINTS).item(0).getTextContent();
            businessProcessRoleContextValue = btmn.getElementsByTagName(TAG_BUSINESS_PROCESS_ROLE).item(0).getTextContent();
            supportingRoleContextValue = btmn.getElementsByTagName(TAG_SUPPORTING_ROLE).item(0).getTextContent();
            systemCapabilitiesContextValue = btmn.getElementsByTagName(TAG_SYSTEM_CAPABILITIES).item(0).getTextContent();
            ExtrinsicObject eo = generateExtrinsicObject(dictionaryEntryName, definition, uniqueID);
            for (int i = 0; i < ABIES.size(); i++) {
                eo.addAssociation(generateAssociation(eo, "Contains", getEObyName((String) ABIES.get(i))));
            }
            eo.addSlot(generateSlot(SLOT_ENTRY_TYPE, "BMT"));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM_QUALIFIER, objectClassTermQualifier));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM, objectClassTerm));
            eo.addSlot(generateSlot(SLOT_BUSINESS_PROCESS, businessProcessContextValue));
            eo.addSlot(generateSlot(SLOT_BUSINESS_PROCESS_ROLE, businessProcessRoleContextValue));
            eo.addSlot(generateSlot(SLOT_GEOPOLITICAL, geopoliticalContextValue));
            eo.addSlot(generateSlot(SLOT_INDUSTRY, industryContextValue));
            eo.addSlot(generateSlot(SLOT_OFFICIAL_CONSTRAINTS, officialConstraintsContextValue));
            eo.addSlot(generateSlot(SLOT_PRODUCT, productContextValue));
            eo.addSlot(generateSlot(SLOT_SUPPORTING_ROLE, supportingRoleContextValue));
            eo.addSlot(generateSlot(SLOT_SYSTEM_CAPABILITIES, systemCapabilitiesContextValue));
            ((ExtrinsicObjectImpl) eo).setLid(uniqueID);
            String fileString = xmlString;
            String fileName = dictionaryEntryName.concat(".xml");
            File file = new File(fileName);
            try {
                (new DataOutputStream(new FileOutputStream(file))).writeBytes(fileString);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            addRepositoryItem(eo, fileName);
            addExtrinsicObject(eo);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void saveABIE(Node nd) throws IOException {
        try {
            String uniqueID;
            String entryType;
            String DEN;
            String definition;
            String objectClassTermQualifier = null;
            String objectClassTerm;
            String businessTerms = null;
            String businessProcess = null;
            String product = null;
            String industry = null;
            String geopolitical = null;
            String officialConstraints = null;
            String roleContext = null;
            String supportingRole = null;
            String systemConstraints = null;
            NodeList nl;
            nl = nd.getChildNodes();
            uniqueID = nl.item(1).getTextContent();
            entryType = nl.item(3).getTextContent();
            DEN = nl.item(5).getTextContent();
            if (getEObyName(DEN) != null) {
                return;
            }
            definition = nl.item(7).getTextContent();
            objectClassTermQualifier = nl.item(9).getTextContent();
            objectClassTerm = nl.item(11).getTextContent();
            businessTerms = nl.item(13).getTextContent();
            businessProcess = nl.item(15).getTextContent();
            product = nl.item(17).getTextContent();
            industry = nl.item(19).getTextContent();
            geopolitical = nl.item(21).getTextContent();
            officialConstraints = nl.item(23).getTextContent();
            roleContext = nl.item(25).getTextContent();
            supportingRole = nl.item(27).getTextContent();
            systemConstraints = nl.item(29).getTextContent();
            ExtrinsicObject eo = generateExtrinsicObject(DEN, definition, uniqueID);
            eo.addSlot(generateSlot(SLOT_UNIQUE_ID, uniqueID));
            eo.addSlot(generateSlot(SLOT_ENTRY_TYPE, entryType));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM_QUALIFIER, objectClassTermQualifier));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM, objectClassTerm));
            eo.addSlot(generateSlot(SLOT_BUSINESS_TERMS, businessTerms));
            eo.addSlot(generateSlot(SLOT_BUSINESS_PROCESS, businessProcess));
            eo.addSlot(generateSlot(SLOT_PRODUCT, product));
            eo.addSlot(generateSlot(SLOT_INDUSTRY, industry));
            eo.addSlot(generateSlot(SLOT_GEOPOLITICAL, geopolitical));
            eo.addSlot(generateSlot(SLOT_OFFICIAL_CONSTRAINTS, officialConstraints));
            eo.addSlot(generateSlot(SLOT_BUSINESS_PROCESS_ROLE, roleContext));
            eo.addSlot(generateSlot(SLOT_SUPPORTING_ROLE, supportingRole));
            eo.addSlot(generateSlot(SLOT_SYSTEM_CAPABILITIES, systemConstraints));
            nd = nd.getNextSibling().getNextSibling();
            String accName = objectClassTerm.concat(". Details");
            ExtrinsicObject accObject = getEObyName(accName);
            eo.addAssociation(generateAssociation(eo, "Extends", accObject));
            while (nd.getChildNodes().item(3).getTextContent().contentEquals("BBIE")) {
                ExtrinsicObject targetObject = saveBBIE(nd);
                eo.addAssociation(generateAssociation(eo, "Contains", targetObject));
                if (nd.getNextSibling().isEqualNode(nd.getParentNode().getLastChild())) {
                    break;
                }
                nd = nd.getNextSibling().getNextSibling();
            }
            if (nd != null) {
                while (nd.getChildNodes().item(3).getTextContent().contentEquals("ASBIE")) {
                    ExtrinsicObject targetObject = saveASBIE(nd);
                    eo.addAssociation(generateAssociation(eo, "Contains", targetObject));
                    if (nd.getNextSibling().isEqualNode(nd.getParentNode().getLastChild())) {
                        break;
                    }
                    nd = nd.getNextSibling().getNextSibling();
                }
            }
            ((ExtrinsicObjectImpl) eo).setLid(uniqueID);
            String fileString = xmlString;
            String fileName = DEN.concat(".xml");
            File file = new File(fileName);
            try {
                (new DataOutputStream(new FileOutputStream(file))).writeBytes(fileString);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            addRepositoryItem(eo, fileName);
            addExtrinsicObject(eo);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ExtrinsicObject saveASBIE(Node nd) {
        try {
            String uniqueID;
            String entryType;
            String DEN;
            String definition;
            String objectClassTermQualifier = null;
            String objectClassTerm;
            String propertyTermQualifier;
            String propertyTerm;
            String associatedObjectQualifier;
            String associatedObject;
            String businessTerms = null;
            String min;
            String max;
            String businessProcess = null;
            String product = null;
            String industry = null;
            String geopolitical = null;
            String officialConstraints = null;
            String roleContext = null;
            String supportingRole = null;
            String systemConstraints = null;
            NodeList nl;
            nl = nd.getChildNodes();
            uniqueID = nl.item(1).getTextContent();
            entryType = nl.item(3).getTextContent();
            DEN = nl.item(5).getTextContent();
            definition = nl.item(7).getTextContent();
            objectClassTermQualifier = nl.item(9).getTextContent();
            objectClassTerm = nl.item(11).getTextContent();
            propertyTermQualifier = nl.item(13).getTextContent();
            propertyTerm = nl.item(15).getTextContent();
            associatedObjectQualifier = nl.item(17).getTextContent();
            associatedObject = nl.item(19).getTextContent();
            businessTerms = nl.item(21).getTextContent();
            min = nl.item(23).getTextContent();
            max = nl.item(25).getTextContent();
            businessProcess = nl.item(27).getTextContent();
            product = nl.item(29).getTextContent();
            industry = nl.item(31).getTextContent();
            geopolitical = nl.item(33).getTextContent();
            officialConstraints = nl.item(35).getTextContent();
            roleContext = nl.item(37).getTextContent();
            supportingRole = nl.item(39).getTextContent();
            systemConstraints = nl.item(41).getTextContent();
            ExtrinsicObject eo = generateExtrinsicObject(DEN, definition, uniqueID);
            eo.addSlot(generateSlot(SLOT_UNIQUE_ID, uniqueID));
            eo.addSlot(generateSlot(SLOT_ENTRY_TYPE, entryType));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM_QUALIFIER, objectClassTermQualifier));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM, objectClassTerm));
            eo.addSlot(generateSlot(SLOT_PROPERTY_TERM_QUALIFIER, propertyTermQualifier));
            eo.addSlot(generateSlot(SLOT_PROPERTY_TERM, propertyTerm));
            eo.addSlot(generateSlot(SLOT_ASSOCIATED_OBJECT_CLASS_TERM_QUALIFIER, associatedObjectQualifier));
            eo.addSlot(generateSlot(SLOT_ASSOCIATED_OBJECT_CLASS_TERM, associatedObject));
            eo.addSlot(generateSlot(SLOT_BUSINESS_TERMS, businessTerms));
            eo.addSlot(generateSlot(SLOT_MIN_OCCURENCE, min));
            eo.addSlot(generateSlot(SLOT_MAX_OCCURENCE, max));
            eo.addSlot(generateSlot(SLOT_BUSINESS_PROCESS, businessProcess));
            eo.addSlot(generateSlot(SLOT_PRODUCT, product));
            eo.addSlot(generateSlot(SLOT_INDUSTRY, industry));
            eo.addSlot(generateSlot(SLOT_GEOPOLITICAL, geopolitical));
            eo.addSlot(generateSlot(SLOT_OFFICIAL_CONSTRAINTS, officialConstraints));
            eo.addSlot(generateSlot(SLOT_BUSINESS_PROCESS_ROLE, roleContext));
            eo.addSlot(generateSlot(SLOT_SUPPORTING_ROLE, supportingRole));
            eo.addSlot(generateSlot(SLOT_SYSTEM_CAPABILITIES, systemConstraints));
            String asccName = objectClassTerm.concat(". ").concat(propertyTerm).concat(". ").concat(associatedObject);
            ExtrinsicObject targetObject = getEObyName(asccName);
            eo.addAssociation(generateAssociation(eo, ASSOC_TYPE_EXTENDS, targetObject));
            String abieName = "";
            if (!associatedObjectQualifier.contentEquals("")) {
                abieName = abieName.concat(associatedObjectQualifier).concat("_ ");
            }
            abieName = abieName.concat(associatedObject).concat(". Details");
            targetObject = getEObyName(abieName);
            eo.addAssociation(generateAssociation(eo, "RelatedTo", targetObject));
            ((ExtrinsicObjectImpl) eo).setLid(uniqueID);
            addExtrinsicObject(eo);
            return eo;
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private ExtrinsicObject saveBBIE(Node nd) {
        try {
            String uniqueID;
            String entryType;
            String DEN;
            String definition;
            String objectClassTermQualifier = null;
            String objectClassTerm;
            String propertyTermQualifier;
            String propertyTerm;
            String representationTerm;
            String dataTypeQualifier;
            String dataType;
            String qualifiedDataTypeUniqueID;
            String businessTerms = null;
            String min;
            String max;
            String businessProcess = null;
            String product = null;
            String industry = null;
            String geopolitical = null;
            String officialConstraints = null;
            String roleContext = null;
            String supportingRole = null;
            String systemConstraints = null;
            NodeList nl;
            nl = nd.getChildNodes();
            uniqueID = nl.item(1).getTextContent();
            entryType = nl.item(3).getTextContent();
            DEN = nl.item(5).getTextContent();
            definition = nl.item(7).getTextContent();
            objectClassTermQualifier = nl.item(9).getTextContent();
            objectClassTerm = nl.item(11).getTextContent();
            propertyTermQualifier = nl.item(13).getTextContent();
            propertyTerm = nl.item(15).getTextContent();
            representationTerm = nl.item(17).getTextContent();
            dataTypeQualifier = nl.item(19).getTextContent();
            dataType = nl.item(21).getTextContent();
            qualifiedDataTypeUniqueID = nl.item(23).getTextContent();
            businessTerms = nl.item(25).getTextContent();
            min = nl.item(27).getTextContent();
            max = nl.item(29).getTextContent();
            businessProcess = nl.item(31).getTextContent();
            product = nl.item(33).getTextContent();
            industry = nl.item(35).getTextContent();
            geopolitical = nl.item(37).getTextContent();
            officialConstraints = nl.item(39).getTextContent();
            roleContext = nl.item(41).getTextContent();
            supportingRole = nl.item(43).getTextContent();
            systemConstraints = nl.item(45).getTextContent();
            ExtrinsicObject eo = generateExtrinsicObject(DEN, definition, uniqueID);
            eo.addSlot(generateSlot(SLOT_UNIQUE_ID, uniqueID));
            eo.addSlot(generateSlot(SLOT_ENTRY_TYPE, entryType));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM_QUALIFIER, objectClassTermQualifier));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM, objectClassTerm));
            eo.addSlot(generateSlot(SLOT_PROPERTY_TERM_QUALIFIER, propertyTermQualifier));
            eo.addSlot(generateSlot(SLOT_PROPERTY_TERM, propertyTerm));
            eo.addSlot(generateSlot(SLOT_REPRESENTATION_TERM, representationTerm));
            eo.addSlot(generateSlot(SLOT_DATA_TYPE_QUALIFIER, dataTypeQualifier));
            eo.addSlot(generateSlot(SLOT_DATA_TYPE, dataType));
            eo.addSlot(generateSlot(SLOT_QUALIFIED_DATA_TYPE_UNIQUE_ID, qualifiedDataTypeUniqueID));
            eo.addSlot(generateSlot(SLOT_BUSINESS_TERMS, businessTerms));
            eo.addSlot(generateSlot(SLOT_MIN_OCCURENCE, min));
            eo.addSlot(generateSlot(SLOT_MAX_OCCURENCE, max));
            eo.addSlot(generateSlot(SLOT_BUSINESS_PROCESS, businessProcess));
            eo.addSlot(generateSlot(SLOT_PRODUCT, product));
            eo.addSlot(generateSlot(SLOT_INDUSTRY, industry));
            eo.addSlot(generateSlot(SLOT_GEOPOLITICAL, geopolitical));
            eo.addSlot(generateSlot(SLOT_OFFICIAL_CONSTRAINTS, officialConstraints));
            eo.addSlot(generateSlot(SLOT_BUSINESS_PROCESS_ROLE, roleContext));
            eo.addSlot(generateSlot(SLOT_SUPPORTING_ROLE, supportingRole));
            eo.addSlot(generateSlot(SLOT_SYSTEM_CAPABILITIES, systemConstraints));
            String bccName = objectClassTerm.concat(". ").concat(propertyTerm).concat(". ").concat(representationTerm);
            ExtrinsicObject targetObject = getEObyName(bccName);
            eo.addAssociation(generateAssociation(eo, "Extends", targetObject));
            String abieName = "";
            if (!dataTypeQualifier.contentEquals("")) {
                abieName = abieName.concat(dataTypeQualifier).concat("_ ");
            }
            abieName = abieName.concat(dataType);
            targetObject = getEObyName(abieName);
            eo.addAssociation(generateAssociation(eo, "Extends", targetObject));
            ((ExtrinsicObjectImpl) eo).setLid(uniqueID);
            addExtrinsicObject(eo);
            return eo;
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void saveACC(Node nd) {
        try {
            String uniqueID;
            String entryType;
            String DEN;
            String definition;
            String objectClassTerm;
            String businessTerms = null;
            NodeList nl;
            nl = nd.getChildNodes();
            uniqueID = nl.item(1).getTextContent();
            entryType = nl.item(3).getTextContent();
            DEN = nl.item(5).getTextContent();
            definition = nl.item(7).getTextContent();
            objectClassTerm = nl.item(9).getTextContent();
            businessTerms = nl.item(11).getTextContent();
            ExtrinsicObject eo = generateExtrinsicObject(DEN, definition, uniqueID);
            eo.addSlot(generateSlot(SLOT_UNIQUE_ID, uniqueID));
            eo.addSlot(generateSlot(SLOT_ENTRY_TYPE, entryType));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM, objectClassTerm));
            eo.addSlot(generateSlot(SLOT_BUSINESS_TERMS, businessTerms));
            nd = nd.getNextSibling().getNextSibling();
            while (nd.getChildNodes().item(3).getTextContent().contentEquals("BCC")) {
                ExtrinsicObject targetObject = saveBCC(nd);
                eo.addAssociation(generateAssociation(eo, "Contains", targetObject));
                if (nd.getNextSibling().isEqualNode(nd.getParentNode().getLastChild())) {
                    break;
                }
                nd = nd.getNextSibling().getNextSibling();
            }
            if (nd != null) {
                while (nd.getChildNodes().item(1).getTextContent().contentEquals("ASCC")) {
                    ExtrinsicObject targetObject = saveASCC(nd);
                    eo.addAssociation(generateAssociation(eo, "Contains", targetObject));
                    if (nd.getNextSibling().isEqualNode(nd.getParentNode().getLastChild())) {
                        break;
                    }
                    nd = nd.getNextSibling();
                }
            }
            ((ExtrinsicObjectImpl) eo).setLid(uniqueID);
            String fileString = xmlString;
            String fileName = DEN.concat(".xml");
            File file = new File("xmlFiles\\".concat(fileName));
            try {
                (new DataOutputStream(new FileOutputStream(file))).writeBytes(fileString);
            } catch (IOException ex) {
                Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            addRepositoryItem(eo, fileName);
            addExtrinsicObject(eo);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ExtrinsicObject saveASCC(Node nd) {
        try {
            String uniqueID;
            String entryType;
            String DEN;
            String definition;
            String objectClassTerm;
            String propertyTerm;
            String businessTerms = null;
            String associated;
            String min;
            String max;
            ExtrinsicObject eo = null;
            NodeList nl;
            nl = nd.getChildNodes();
            uniqueID = nl.item(1).getTextContent();
            entryType = nl.item(3).getTextContent();
            DEN = nl.item(5).getTextContent();
            definition = nl.item(7).getTextContent();
            objectClassTerm = nl.item(9).getTextContent();
            propertyTerm = nl.item(11).getTextContent();
            associated = nl.item(13).getTextContent();
            businessTerms = nl.item(15).getTextContent();
            min = nl.item(17).getTextContent();
            max = nl.item(19).getTextContent();
            eo = generateExtrinsicObject(DEN, definition, uniqueID);
            eo.addSlot(generateSlot(SLOT_UNIQUE_ID, uniqueID));
            eo.addSlot(generateSlot(SLOT_ENTRY_TYPE, entryType));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM, objectClassTerm));
            eo.addSlot(generateSlot(SLOT_ASSOCIATED_OBJECT_CLASS_TERM, associated));
            eo.addSlot(generateSlot(SLOT_PROPERTY_TERM, propertyTerm));
            eo.addSlot(generateSlot(SLOT_BUSINESS_TERMS, businessTerms));
            eo.addSlot(generateSlot(SLOT_MIN_OCCURENCE, min));
            eo.addSlot(generateSlot(SLOT_MAX_OCCURENCE, max));
            String targetObjectName = associated.concat(". Details");
            ExtrinsicObject targetObject = getEObyName(targetObjectName);
            eo.addAssociation(generateAssociation(eo, "RelatedTo", targetObject));
            ((ExtrinsicObjectImpl) eo).setLid(uniqueID);
            addExtrinsicObject(eo);
            return eo;
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private ExtrinsicObject saveBCC(Node nd) {
        try {
            String uniqueID;
            String entryType;
            String DEN;
            String definition;
            String objectClassTerm;
            String propertyTerm;
            String businessTerms = null;
            String representation;
            String min;
            String max;
            ExtrinsicObject eo = null;
            NodeList nl;
            nl = nd.getChildNodes();
            uniqueID = nl.item(1).getTextContent();
            entryType = nl.item(3).getTextContent();
            DEN = nl.item(5).getTextContent();
            definition = nl.item(7).getTextContent();
            objectClassTerm = nl.item(9).getTextContent();
            propertyTerm = nl.item(11).getTextContent();
            representation = nl.item(13).getTextContent();
            businessTerms = nl.item(15).getTextContent();
            min = nl.item(17).getTextContent();
            max = nl.item(19).getTextContent();
            eo = generateExtrinsicObject(DEN, definition, uniqueID);
            eo.addSlot(generateSlot(SLOT_UNIQUE_ID, uniqueID));
            eo.addSlot(generateSlot(SLOT_ENTRY_TYPE, entryType));
            eo.addSlot(generateSlot(SLOT_OBJECT_CLASS_TERM, objectClassTerm));
            eo.addSlot(generateSlot(SLOT_ASSOCIATED_OBJECT_CLASS_TERM, representation));
            eo.addSlot(generateSlot(SLOT_PROPERTY_TERM, propertyTerm));
            eo.addSlot(generateSlot(SLOT_REPRESENTATION_TERM, representation));
            eo.addSlot(generateSlot(SLOT_BUSINESS_TERMS, businessTerms));
            eo.addSlot(generateSlot(SLOT_MIN_OCCURENCE, min));
            eo.addSlot(generateSlot(SLOT_MAX_OCCURENCE, max));
            String targetObjectName = representation.concat(". Type");
            ExtrinsicObject targetObject = getEObyName(targetObjectName);
            eo.addAssociation(generateAssociation(eo, "Extends", targetObject));
            ((ExtrinsicObjectImpl) eo).setLid(uniqueID);
            addExtrinsicObject(eo);
            return eo;
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Slot generateSlot(String slotName, String slotValue) {
        SlotImpl slot = null;
        try {
            slot = new SlotImpl((LifeCycleManagerImpl) lcm);
            Collection cllc = new ArrayList();
            cllc.add(slotValue);
            slot.setValues(cllc);
            slot.setName(slotName);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return slot;
    }

    private ExtrinsicObject generateExtrinsicObject(String name, String description, String uniqueIdentifier) {
        ExtrinsicObject eo = null;
        try {
            eo = lcm.createExtrinsicObject(null);
            eo.getName().setValue(name);
            eo.getDescription().setValue(description);
            if (uniqueIdentifier != null) {
                ((ExtrinsicObjectImpl) eo).setLid(uniqueIdentifier);
            }
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return eo;
    }

    private Association generateAssociation(ExtrinsicObject source, String type, ExtrinsicObject target) {
        AssociationImpl as = null;
        String id = null;
        try {
            if (type.contentEquals("Extends")) {
                id = ASSOC_TYPE_EXTENDS;
            } else if (type.contentEquals("Contains")) {
                id = ASSOC_TYPE_CONTAINS;
            } else if (type.contentEquals("RelatedTo")) {
                id = ASSOC_TYPE_RELATEDTO;
            } else {
                System.out.println("No Suitable Association Type Found.. Aborting");
                System.exit(1);
            }
            Concept cp = (Concept) bqm.getRegistryObject(id);
            as = new AssociationImpl((LifeCycleManagerImpl) lcm);
            as.setAssociationType(cp);
            as.setSourceObject(source);
            as.setTargetObject(target);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return as;
    }

    private void addExtrinsicObject(ExtrinsicObject eo) {
        try {
            Collection cllc = new ArrayList();
            cllc.add(eo);
            connection.close();
            init(USER_NAME, PASS, true);
            lcm.saveObjects(cllc);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ExtrinsicObject getEObyName(String targetObjectName) {
        try {
            String qString = "SELECT eo.* FROM Extrinsicobject eo, NAME_ nm WHERE nm.value='" + targetObjectName + "' " + "AND eo.id=nm.parent";
            Query qry = dqm.createQuery(Query.QUERY_TYPE_SQL, qString);
            Object[] EOlist = dqm.executeQuery(qry).getCollection().toArray();
            if (EOlist.length == 0) {
                return null;
            }
            return (ExtrinsicObject) EOlist[0];
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void saveXMLFile(Document doc, String fileName) {
        FileOutputStream outputStream = null;
        try {
            OutputFormat outputFormat = new OutputFormat(doc);
            outputFormat.setIndenting(true);
            outputFormat.setLineWidth(0);
            outputFormat.setOmitComments(false);
            outputFormat.setOmitDocumentType(false);
            outputFormat.setOmitXMLDeclaration(false);
            outputFormat.setEncoding("UTF-8");
            outputStream = new FileOutputStream("xmlFiles\\" + fileName);
            XMLSerializer serializer = new XMLSerializer(outputStream, outputFormat);
            serializer.asDOMSerializer();
            serializer.serialize(doc.getDocumentElement());
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void addRepositoryItem(ExtrinsicObject eo, String fileName) {
        try {
            File repositoryItemFile = new File("xmlFiles\\" + fileName);
            FileDataSource repositoryItemFDS = new FileDataSource(repositoryItemFile);
            DataHandler repositoryItemDH = new DataHandler(repositoryItemFDS);
            eo.setRepositoryItem(repositoryItemDH);
        } catch (JAXRException ex) {
            Logger.getLogger(CCRepositoryProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeToFile(String str) {
        BufferedWriter das = null;
        try {
            File file = new File(filePath);
            das = new BufferedWriter(new FileWriter(file));
            das.write(str);
            das.flush();
            das.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                das.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

package onepoint.project.modules.backup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import onepoint.log.XLog;
import onepoint.log.XLogFactory;
import onepoint.persistence.OpBroker;
import onepoint.persistence.OpField;
import onepoint.persistence.OpMember;
import onepoint.persistence.OpPrototype;
import onepoint.persistence.OpQuery;
import onepoint.persistence.OpRelationship;
import onepoint.persistence.OpSiteObject;
import onepoint.persistence.OpSiteObjectIfc;
import onepoint.persistence.OpTransaction;
import onepoint.persistence.OpType;
import onepoint.persistence.OpTypeManager;
import onepoint.persistence.OpUserType;
import onepoint.persistence.OpUserType.FieldDescription;
import onepoint.persistence.hibernate.OpHibernateSource;
import onepoint.project.OpInitializerFactory;
import onepoint.project.OpProjectSession;
import onepoint.project.modules.custom_attribute.OpCustomValuePage;
import onepoint.project.modules.custom_attribute.OpCustomizable;
import onepoint.project.modules.repository.OpRepositoryService;
import onepoint.project.modules.site_management.OpSite;
import onepoint.project.modules.site_management.OpSiteManager;
import onepoint.project.modules.user.OpGroup;
import onepoint.project.modules.user.OpPermission;
import onepoint.project.modules.user.OpPermissionable;
import onepoint.project.modules.user.OpUser;
import onepoint.project.modules.user.OpUserService;
import onepoint.project.util.OpEnvironmentManager;
import onepoint.project.util.OpGraph;
import onepoint.project.util.OpProjectCalendar;
import onepoint.service.XSizeInputStream;
import onepoint.service.server.XServiceException;
import onepoint.util.Pair;
import onepoint.util.Triple;
import onepoint.util.XEnvironmentManager;
import onepoint.xml.XDocumentWriter;

/**
 * Class that backs-up and restores the application repository.
 *
 * @author gmesaric
 * @author horia.chiorean
 */
public class OpBackupManager {

    public static final String DATA_XML = "data.xml";

    /**
	 * The current project version number.
	 */
    public static final int CURRENT_VERSION_NUMBER = 1;

    /**
	 * Constants that represent node and attribute names that will be written to the xml backup.
	 */
    public static final String OPP_BACKUP = "opp-backup";

    public static final String VERSION = "version";

    public static final String SET_TODAY = "set-today";

    public static final String OFFSET_DAYS = "offset-days";

    public static final String SCHEMA_VERSION = "schema-version";

    public static final String PROTOTYPES = "prototypes";

    public static final String PROTOTYPE = "prototype";

    public static final String NAME = "name";

    public static final String FIELD = "field";

    public static final String TYPE = "type";

    public static final String RELATIONSHIP = "relationship";

    public static final String COMPOSITE = "composite";

    public static final String OBJECTS = "objects";

    public static final String O = "object";

    public static final String ID = "id";

    public static final String SYSTEM = "system";

    public static final String P = "property";

    public static final String R = "reference";

    public static final String NULL = "null";

    public static final String TRUE = "true";

    public static final String FALSE = "false";

    /**
	 * This class's logger
	 */
    private static final XLog logger = XLogFactory.getLogger(OpBackupManager.class);

    /**
	 * The size of a page, when performing backup.
	 */
    private static final int BACKUP_PAGE_SIZE = 1000;

    /**
	 * The maximum number of operations done per a transaction.
	 */
    public static final int MAX_DELETES_PER_TRANSACTION = 300;

    /**
	 * The name of the directory where binary files (contents) are stored
	 */
    private static final String BINARY_DIR_NAME_SUFFIX = "-files";

    /**
	 * Date format used for importing/exporting date values.
	 */
    static SimpleDateFormat DATE_FORMAT = null;

    static SimpleDateFormat TIMESTAMP_FORMAT = null;

    /**
	 * The map of prototypes (their load order is important).
	 */
    private static Map<String, OpPrototype> prototypes = new LinkedHashMap<String, OpPrototype>();

    /**
	 * A map of [systemObjectName, systemObjectQuery] used for retrieving system objects.
	 */
    private static Map<String, String> systemObjectIdQueries = new HashMap<String, String>();

    /**
	 * The class instance
	 */
    private static OpBackupManager backupManager = null;

    /**
	 * "/" string
	 */
    private static final String SLASH_STRING = "/";

    private static final Integer DEFAULT_EDGE_CLASS = new Integer(0);

    private static final String WHERE_STR = " where ";

    private static final String AND_STR = " and ";

    /**
	 * There should be only 1 instance of this class.
	 */
    private OpBackupManager() {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        DATE_FORMAT.setTimeZone(OpProjectCalendar.GMT_TIMEZONE);
        TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TIMESTAMP_FORMAT.setTimeZone(OpProjectCalendar.GMT_TIMEZONE);
    }

    /**
	 * Returns a backup manager instance.
	 *
	 * @return a <code>OpBackupManager</code> instance.
	 */
    public static OpBackupManager getBackupManager() {
        if (backupManager == null) {
            backupManager = new OpBackupManager();
        }
        return backupManager;
    }

    /**
	 * Initializes the backup manager by registering all the prototypes in correct order.
	 */
    public void initializeBackupManager() {
        OpGraph graph = new OpGraph();
        for (OpPrototype prototype : OpTypeManager.getPrototypes()) {
            if (prototype.isInterface()) {
                continue;
            }
            graph.addNode(prototype);
            OpGraph.Entry prototypeNode = graph.getNode(prototype);
            List dependencies = prototype.getSubsequentBackupDependencies();
            Iterator iter = dependencies.iterator();
            while (iter.hasNext()) {
                OpPrototype dependency = (OpPrototype) iter.next();
                OpGraph.Entry dependencyNode = graph.getNode(dependency);
                if (dependencyNode == null) {
                    dependencyNode = graph.addNode(dependency);
                }
                if (((OpPrototype) dependencyNode.getElem()).getInstanceClass() == OpSiteObject.class) {
                    graph.removeEdge(prototypeNode, dependencyNode);
                }
                graph.addEdge(dependencyNode, prototypeNode, DEFAULT_EDGE_CLASS);
            }
        }
        Iterator iter = graph.getTopologicOrder().iterator();
        while (iter.hasNext()) {
            OpGraph.Entry node = (OpGraph.Entry) iter.next();
            if (!((OpPrototype) node.getElem()).subTypes().hasNext()) {
                addPrototype((OpPrototype) node.getElem());
            }
        }
    }

    public Map<String, OpPrototype> getPrototypes() {
        return prototypes;
    }

    /**
	 * Adds a prototype to the map of prototypes.
	 *
	 * @param prototype a <code>OpPrototype</code> object.
	 */
    public static void addPrototype(OpPrototype prototype) {
        logger.info("Backup manager registering: " + prototype.getName());
        prototypes.put(prototype.getName(), prototype);
    }

    /**
	 * Checks whether the manager has registed the given prototype or not.
	 *
	 * @param prototype a <code>OpPrototype</code> representing the prototype to register.
	 * @return a <code>boolean</code> indicating whether the prototype has been registered or not.
	 */
    public static boolean hasRegistered(OpPrototype prototype) {
        return prototypes.get(prototype.getName()) != null;
    }

    /**
	 * Adds a system object name together with a query that can obtain the system object to the backup manager.
	 *
	 * @param name  a <code>String</code> representing the name of the system object.
	 * @param query a <code>String</code> representing a query which together with the name obtains the sytem object.
	 */
    public static void addSystemObjectIDQuery(String name, String query) {
        systemObjectIdQueries.put(name, query);
    }

    /**
	 * Returns a query for the given system object name.
	 *
	 * @param name a <code>String</code> representing the name of the system object.
	 * @return a <code>String</code> representing a query for the system object's name.
	 */
    public static String getSystemObjectIDQuery(String name) {
        return systemObjectIdQueries.get(name);
    }

    /**
	 * Creates a map of [object_name, object_id] for all the system objects that were registered by the manager.
	 *
	 * @param broker a <code>OpBroker</code> that executes the query agains a db.
	 * @return a <code>Map</code> of [<code>String</code>, <code>Long</code>] pairs representing [name, id] pairs.
	 */
    public static Map<Long, String> querySystemObjectIdMap(OpBroker broker) {
        Map<Long, String> systemObjectIdMap = new HashMap<Long, String>();
        for (String name : systemObjectIdQueries.keySet()) {
            String queryString = systemObjectIdQueries.get(name);
            logger.info("Query:" + queryString);
            OpQuery query = broker.newQuery(queryString);
            Iterator it = broker.forceIterate(query);
            if (!it.hasNext()) {
                logger.warn("System object id not found after query:" + queryString);
                continue;
            }
            Long id = (Long) it.next();
            systemObjectIdMap.put(id, name);
        }
        return systemObjectIdMap;
    }

    /**
	 * Return a backup member for the given field.
	 *
	 * @param field a <code>OpField</code> instance, representing a field.
	 * @return a <code>XBackupMemeber</code> instance.
	 */
    private OpBackupMember getMemberForField(OpField field, List<Field> prefix, List<String> namePrefix) {
        OpBackupMember backupMember = new OpBackupMember();
        backupMember.setNames(namePrefix, field.getName());
        backupMember.typeId = field.getTypeID();
        backupMember.relationship = false;
        backupMember.ordered = field.getOrdered();
        backupMember.recursive = false;
        return backupMember;
    }

    /**
	 * Return a backup member for the given field.
	 *
	 * @param relationship a <code>XRelationShip</code> instance, representing a relationship.
	 * @return a <code>XBackupMemeber</code> instance.
	 */
    private OpBackupMember getMemberForRelationShip(OpRelationship relationship, List<Field> prefix, List<String> namePrefix) {
        OpBackupMember backupMember = new OpBackupMember();
        backupMember.setNames(namePrefix, relationship.getName());
        backupMember.typeId = relationship.getTypeID();
        backupMember.relationship = true;
        backupMember.ordered = false;
        backupMember.recursive = relationship.getRecursive();
        OpRelationship backRelationShip = relationship.getBackRelationship();
        backupMember.backRelationshipName = backRelationShip != null ? backRelationShip.getName() : null;
        return backupMember;
    }

    /**
	 * Exports all the prototypes this manager has loaded on the given document writer.
	 *
	 * @param writer a <code>XDocumentWriter</code> that writes out xml files.
	 * @return a <code>List</code> of <code>BackupMember[]</code> representing all the members which have been backed up
	 *         for each prototype.
	 * @throws IOException if any of the prototypes could not be exported.
	 */
    private Map<String, OpBackupMember[]> exportPrototypes(XDocumentWriter writer) throws IOException {
        Map<String, OpBackupMember[]> allMembers = new HashMap<String, OpBackupMember[]>();
        writer.writeStartElement(PROTOTYPES, null, false);
        for (Map.Entry<String, OpPrototype> pe : prototypes.entrySet()) {
            OpPrototype prototype = pe.getValue();
            if (!isBackupablePrototype(prototype)) {
                logger.debug("Prototype " + prototype.getName() + " not backed up...");
                continue;
            }
            appendXMLPrototypeStart(writer, prototype);
            List<OpBackupMember> backupMembers = new LinkedList<OpBackupMember>();
            Stack<Field> prefix = new Stack<Field>();
            Stack<String> namePrefix = new Stack<String>();
            for (OpMember member : prototype.getMembers()) {
                setupBackupStructuresForMember(writer, prototype, backupMembers, prefix, namePrefix, member);
            }
            allMembers.put(pe.getKey(), backupMembers.toArray(new OpBackupMember[backupMembers.size()]));
            appendXMLPrototypeEnd(writer);
        }
        writer.writeEndElement(PROTOTYPES);
        return allMembers;
    }

    private boolean isBackupablePrototype(OpPrototype prototype) {
        return OpSiteObjectIfc.class.isAssignableFrom(prototype.getInstanceClass());
    }

    private void setupBackupStructuresForMember(XDocumentWriter writer, OpPrototype prototype, List<OpBackupMember> backupMembers, Stack<Field> prefix, Stack<String> namePrefix, OpMember member) throws IOException {
        Field field = prepareField(prototype, member);
        if (field == null) {
            logger.error("No accessor found for member: " + member.getName() + "  of prototype:" + prototype.getName());
            return;
        }
        setupBackupStructureForMemberAndField(prototype, writer, backupMembers, prefix, namePrefix, member, field);
    }

    private void setupBackupStructuresForMember(OpPrototype prototype, XDocumentWriter writer, OpUserType c, List<OpBackupMember> backupMembers, Stack<Field> prefix, Stack<String> namePrefix, OpMember member) throws IOException {
        Field field = prepareField(c, member);
        if (field == null) {
            logger.error("No accessor found for member: " + member.getName() + "  of composite:" + c.getName());
            return;
        }
        setupBackupStructureForMemberAndField(prototype, writer, backupMembers, prefix, namePrefix, member, field);
    }

    private void setupBackupStructureForMemberAndField(OpPrototype prototype, XDocumentWriter writer, List<OpBackupMember> backupMembers, Stack<Field> prefix, Stack<String> namePrefix, OpMember member, Field field) throws IOException {
        if (member instanceof OpField) {
            OpField f = (OpField) member;
            String typeString = OpBackupTypeManager.getTypeString(f.getTypeID());
            if (typeString != null) {
                OpBackupMember backupMember = this.getMemberForField(f, prefix, namePrefix);
                backupMember.setPath(prefix, field);
                backupMember.setNames(namePrefix, f.getName());
                backupMembers.add(backupMember);
                appendXMLFieldMemberDescription(writer, OpMember.namesToString(backupMember.getNames()), typeString);
            }
        } else if (member instanceof OpRelationship) {
            OpRelationship rel = (OpRelationship) member;
            if (!rel.getInverse() && !rel.isTransient()) {
                OpPrototype targetPrototype = OpTypeManager.getPrototypeByID(rel.getTypeID());
                if (targetPrototype != null && isBackupablePrototype(targetPrototype)) {
                    OpBackupMember backupMember = this.getMemberForRelationShip(rel, prefix, namePrefix);
                    backupMember.setPath(prefix, field);
                    backupMember.setNames(namePrefix, rel.getName());
                    backupMembers.add(backupMember);
                    appendXMLRelationMember(writer, OpMember.namesToString(backupMember.getNames()), targetPrototype);
                }
            }
        } else if (member instanceof OpUserType) {
            OpUserType c = (OpUserType) member;
            prefix.push(field);
            namePrefix.push(c.getName());
            Iterator<FieldDescription> fit = c.getFieldIterator();
            while (fit.hasNext()) {
                FieldDescription f = fit.next();
                setupBackupStructuresForMember(prototype, writer, c, backupMembers, prefix, namePrefix, f.getMember());
            }
            namePrefix.pop();
            prefix.pop();
        }
    }

    private void appendXMLCompositeEnd(XDocumentWriter writer) throws IOException {
        writer.writeEndElement(COMPOSITE);
    }

    private void appendXMLCompositeStart(XDocumentWriter writer, OpUserType c) throws IOException {
        Map<String, String> prototypeAttributes = new HashMap<String, String>();
        prototypeAttributes.put(NAME, c.getName());
        prototypeAttributes.put(TYPE, c.getTypeName());
        writer.writeStartElement(COMPOSITE, prototypeAttributes, false);
    }

    private void appendXMLPrototypeEnd(XDocumentWriter writer) throws IOException {
        writer.writeEndElement(PROTOTYPE);
    }

    private void appendXMLPrototypeStart(XDocumentWriter writer, OpPrototype prototype) throws IOException {
        Map<String, String> prototypeAttributes = new HashMap<String, String>();
        prototypeAttributes.put(NAME, prototype.getName());
        writer.writeStartElement(PROTOTYPE, prototypeAttributes, false);
    }

    private void appendXMLRelationMember(XDocumentWriter writer, String name, OpPrototype targetPrototype) throws IOException {
        Map<String, String> relationAttributes = new HashMap<String, String>();
        relationAttributes.put(NAME, name);
        relationAttributes.put(TYPE, targetPrototype.getName());
        writer.writeStartElement(RELATIONSHIP, relationAttributes, true);
    }

    private void appendXMLFieldMemberDescription(XDocumentWriter writer, String name, String typeString) throws IOException {
        Map<String, String> memberAttributes = new HashMap<String, String>();
        memberAttributes.put(NAME, name);
        memberAttributes.put(TYPE, typeString);
        writer.writeStartElement(FIELD, memberAttributes, true);
    }

    private Field prepareField(OpPrototype prototype, OpMember member) {
        Field field = OpPrototype.getAccessibleField(prototype.getInstanceClass(), member.getName());
        if (field != null) {
            field.setAccessible(true);
        }
        return field;
    }

    private Field prepareField(OpUserType c, OpMember member) {
        Field field = OpPrototype.getAccessibleField(c.getObjectClass(), member.getName());
        if (field != null) {
            field.setAccessible(true);
        }
        return field;
    }

    /**
	 * Writes the given field (backup member) to the given writer, including its value.
	 *
	 * @param member a <code>OpBackupMember</code> representing a field of an entity.
	 * @param writer a <code>XDocumentWriter</code> that writes out the field with its value.
	 * @param value  a <code>Object</code> representing the value of the field.
	 * @throws IOException if the field cannot be written.
	 */
    private void writeFieldMember(OpSiteObjectIfc object, OpBackupMember member, XDocumentWriter writer, Object value, List<Triple<String, OpBackupMember, String>> toExport) throws IOException {
        writer.writeStartElement(P, null, false);
        if (value != null) {
            switch(member.typeId) {
                case OpType.BOOLEAN:
                    if (((Boolean) value).booleanValue()) {
                        writer.writeContent(TRUE);
                    } else {
                        writer.writeContent(FALSE);
                    }
                    break;
                case OpType.INTEGER:
                    writer.writeContent(((Integer) value).toString());
                    break;
                case OpType.LONG:
                    writer.writeContent(((Long) value).toString());
                    break;
                case OpType.STRING:
                    writer.writeContent(new StringBuffer("<![CDATA[").append((String) value).append("]]>").toString());
                    break;
                case OpType.TEXT:
                    writer.writeContent(new StringBuffer("<![CDATA[").append((String) value).append("]]>").toString());
                    break;
                case OpType.DATE:
                    writer.writeContent(DATE_FORMAT.format((Date) value));
                    break;
                case OpType.CONTENT:
                    if (value == null) {
                        writer.writeContent(NULL);
                    } else {
                        String fileName = binaryFilePath(object.getId(), member.getNames());
                        String filePath = "files" + fileName;
                        toExport.add(new Triple<String, OpBackupMember, String>(object.locator(), member, filePath));
                        writer.writeContent(filePath);
                    }
                    break;
                case OpType.BYTE:
                    writer.writeContent(((Byte) value).toString());
                    break;
                case OpType.DOUBLE:
                    writer.writeContent(((Double) value).toString());
                    break;
                case OpType.TIMESTAMP:
                    writer.writeContent(TIMESTAMP_FORMAT.format((Timestamp) value));
                    break;
                default:
                    logger.error("ERROR: Unsupported type ID " + member.typeId + " for " + OpTypeManager.getPrototypeForObject(object).getName() + "." + OpMember.namesToString(member.getNames()));
            }
        }
        writer.writeEndElement(P);
    }

    /**
	 * Writes the value of a relationship member.
	 *
	 * @param writer a <code>XDocumentWriter</code> that will write the output.
	 * @param value  a <code>Object</code> representing the value of the relation ship.
	 */
    private void writeRelationshipMember(XDocumentWriter writer, Object value) throws IOException {
        writer.writeStartElement(R, null, false);
        if (value != null) {
            writer.writeContent(String.valueOf(((OpSiteObjectIfc) value).getId()));
        }
        writer.writeEndElement(R);
    }

    /**
	 * @param orderedBy   a <code>String</code> representing the name of the member field after which to order by.
	 * @param recursiveBy a <code>String</code> representing the name of the member relationship which is recursive.
	 * @param objects     a <code>List</code> of <code>OpObject</code> representing child objects in case of a recursive backup.
	 * @see OpBackupManager#exportObjects(onepoint.project.OpProjectSession,onepoint.xml.XDocumentWriter,String,OpBackupMember[],java.util.Map)
	 */
    private void exportSubObjects(OpProjectSession session, OpBroker broker, XDocumentWriter writer, OpPrototype prototype, OpBackupMember[] members, String orderedBy, String recursiveBy, List objects, Map systemIdMap, List<Triple<String, OpBackupMember, String>> toExport) throws IOException {
        int pageSize = BACKUP_PAGE_SIZE;
        int startIndex = 0;
        Iterator result = null;
        if (objects != null) {
            result = objects.iterator();
        } else {
            int count = getObjectsToBackupCount(prototype, recursiveBy, broker);
            logger.info("Backing up " + count + " " + prototype.getName());
            if (count > pageSize) {
                while (startIndex < count) {
                    logger.info("Backing up objects between " + startIndex + AND_STR + (startIndex + pageSize));
                    OpBroker pagingBroker = session.newBroker();
                    try {
                        result = getObjectsToBackup(prototype, recursiveBy, orderedBy, pagingBroker, startIndex, pageSize);
                        startIndex += pageSize;
                        pageSize = (startIndex + pageSize) < count ? pageSize : count - startIndex;
                        List childObjects = exportIteratedObjects(result, systemIdMap, writer, members, recursiveBy, toExport);
                        if ((recursiveBy != null) && (childObjects.size() > 0)) {
                            exportSubObjects(session, pagingBroker, writer, prototype, members, orderedBy, recursiveBy, childObjects, systemIdMap, toExport);
                        }
                    } finally {
                        pagingBroker.closeAndEvict();
                    }
                }
                return;
            } else {
                result = getObjectsToBackup(prototype, recursiveBy, orderedBy, broker, 0, count);
            }
        }
        List childObjects = exportIteratedObjects(result, systemIdMap, writer, members, recursiveBy, toExport);
        if ((recursiveBy != null) && (childObjects.size() > 0)) {
            exportSubObjects(session, broker, writer, prototype, members, orderedBy, recursiveBy, childObjects, systemIdMap, toExport);
        }
    }

    private List exportIteratedObjects(Iterator result, Map systemIdMap, XDocumentWriter writer, OpBackupMember[] members, String recursiveBy, List<Triple<String, OpBackupMember, String>> toExport) throws IOException {
        List childObjects = (recursiveBy != null) ? new ArrayList() : null;
        Map<String, String> attributes = new HashMap<String, String>();
        while (result.hasNext()) {
            OpSiteObject object = (OpSiteObject) result.next();
            object = OpHibernateSource.initializeObject(OpSiteObject.class, object);
            Long id = new Long(object.getId());
            attributes.put(ID, String.valueOf(object.getId()));
            String systemObjectName = (String) systemIdMap.get(id);
            if (systemObjectName != null) {
                attributes.put(SYSTEM, systemObjectName);
            }
            writer.writeStartElement(O, attributes, false);
            attributes.clear();
            OpBackupMember member = null;
            for (int i = 0; i < members.length; i++) {
                member = members[i];
                if (member == null) {
                    continue;
                }
                Object value = OpBackupLoader.getValueFromObject(object, member);
                if (member.relationship) {
                    this.writeRelationshipMember(writer, value);
                    if (member.backRelationshipName != null && recursiveBy != null && recursiveBy.equals(OpMember.namesToString(member.getNames()))) {
                        Field backRelationField = OpPrototype.getAccessibleField(object.getClass(), member.backRelationshipName);
                        if (backRelationField != null) {
                            Object backRelationshipValue = OpPrototype.getFieldValue(object, backRelationField);
                            if (backRelationshipValue != null && (backRelationshipValue instanceof Collection)) {
                                childObjects.addAll((Collection) backRelationshipValue);
                            }
                        }
                    }
                } else {
                    this.writeFieldMember(object, member, writer, value, toExport);
                }
            }
            writer.writeEndElement(O);
        }
        return childObjects;
    }

    /**
	 * Gets a list of objects to back-up.
	 *
	 * @param prototype a <code>String</code> representing the name of a prototype.
	 * @param recursiveBy   a <code>String</code> indicating the name of the recursive relationship.
	 * @param orderedBy     a <code>String</code> representing the name of the field after which to order the objects.
	 * @param broker        a <code>OpBroker</code> used for performing business operations.
	 * @return an <code>Iterator</code> over <code>OpObjects</code>.
	 */
    private Iterator getObjectsToBackup(OpPrototype prototype, String recursiveBy, String orderedBy, OpBroker broker, int startIndex, int count) {
        StringBuffer queryBuffer = new StringBuffer("select xobject from ");
        queryBuffer.append(prototype.getInstanceClass().getSimpleName());
        queryBuffer.append(" as xobject");
        if (recursiveBy != null) {
            queryBuffer.append(" where xobject.");
            queryBuffer.append(recursiveBy);
            queryBuffer.append(" is null");
        }
        queryBuffer.append(" order by xobject.");
        if (orderedBy != null) {
            queryBuffer.append(orderedBy);
            queryBuffer.append(", xobject.");
        }
        queryBuffer.append("id");
        logger.debug("***QUERY: " + queryBuffer);
        OpQuery query = broker.newQuery(queryBuffer.toString());
        query.setFirstResult(startIndex);
        query.setMaxResults(count);
        query.setFetchSize(count);
        return broker.forceIterate(query);
    }

    /**
	 * Gets the number of objects to back-up from the db.
	 *
	 * @param prototype a <code>String</code> representing the name of a prototype.
	 * @param broker        a <code>OpBroker</code> used for performing business operations.
	 * @return an <code>Iterator</code> over <code>OpObjects</code>.
	 */
    private int getObjectsToBackupCount(OpPrototype prototype, String recursiveBy, OpBroker broker) {
        StringBuffer queryBuffer = new StringBuffer("select count(xobject.id) from ");
        queryBuffer.append(prototype.getInstanceClass().getSimpleName());
        queryBuffer.append(" as xobject");
        if (recursiveBy != null) {
            queryBuffer.append(" where xobject.");
            queryBuffer.append(recursiveBy);
            queryBuffer.append(" is null");
        }
        logger.debug("***QUERY: " + queryBuffer);
        OpQuery query = broker.newQuery(queryBuffer.toString());
        return ((Number) broker.forceIterate(query).next()).intValue();
    }

    /**
	 * For the given proptotype and members of the prototype exports all current db values.
	 *
	 * @param session       a <code>OpProjectSession</code> used for creating brokers that perform db operations.
	 * @param broker
	 * @param writer        a <code>XDocumentWriter</code> that is used to output xml.
	 * @param prototype a <code>String</code> representing the name of the prototype.
	 * @param members       a <code>OpBackupMember[]</code> representing the members to be exported for the given prototype.
	 * @param systemIdMap   a <code>Map</code> of <code>[String,Long]</code> representing the system objects.
	 * @throws IOException if anything fails.
	 */
    private void exportObjects(OpProjectSession session, OpBroker broker, XDocumentWriter writer, OpPrototype prototype, OpBackupMember[] members, Map systemIdMap, List<Triple<String, OpBackupMember, String>> toExport) throws IOException {
        if (members == null) {
            return;
        }
        logger.info("Backing up ******** " + prototype.getName() + " ********");
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(TYPE, prototype.getName());
        writer.writeStartElement(OBJECTS, attributes, false);
        attributes.clear();
        String orderedBy = null;
        for (int i = 0; i < members.length; i++) {
            if (members[i] != null) {
                String memberName = OpMember.namesToString(members[i].getNames());
                logger.info("Backing up member " + memberName);
                if (!members[i].relationship && members[i].ordered && orderedBy == null) {
                    orderedBy = memberName;
                }
                if (orderedBy != null) {
                    break;
                }
            }
        }
        exportSubObjects(session, broker, writer, prototype, members, orderedBy, null, null, systemIdMap, toExport);
        writer.writeEndElement(OBJECTS);
    }

    /**
	 * Backs up an existent repository under an xml file with the given path.
	 *
	 * @param session an <code>OpProjectSession</code> representing an application session.
	 * @param path    a <code>String</code> representing the path to the file where the backup should be generated.
	 * @throws IOException              if the operation fails and the backup cannot be written.
	 * @throws IllegalArgumentException if the given path is invalid.
	 */
    public final void backupRepository(OpProjectSession session, String path) throws IOException {
        if (!isPathAFile(path, false)) {
            throw new IllegalArgumentException("The given path to backup the repository to is not a file");
        }
        logger.info("backing up to " + path);
        this.backupRepository(session, new File(path));
    }

    /**
	 * Checks whether the given path points to an existent file or not.
	 *
	 * @param path        a <code>String</code> representing a path.
	 * @param shouldExist a <code>boolean</code> indicating whether an existence check should be done or not.
	 * @return <code>true</code> if the given path points to an existent file.
	 */
    private boolean isPathAFile(String path, boolean shouldExist) {
        String formattedPath = XEnvironmentManager.convertPathToSlash(path);
        File filePath = new File(formattedPath);
        if (shouldExist && !filePath.exists()) {
            logger.info("Given path should be an existing file, but isn't");
            return false;
        }
        if (filePath.isDirectory()) {
            logger.info("Given path is a directory");
            return false;
        }
        return true;
    }

    /**
	 * From the given path to a file, extracts the parent directory of the file.
	 *
	 * @param filePath a <code>String</code> representing the path to an existent file.
	 * @return a <code>String</code> representing the parent directory of the given file.
	 * @throws IllegalArgumentException if the given string does not point to an exiting file.
	 */
    private String getParentDirectory(String filePath) {
        filePath = XEnvironmentManager.convertPathToSlash(filePath);
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("The given path argument does not point to an existent file");
        }
        return XEnvironmentManager.convertPathToSlash(file.getParent());
    }

    /**
	 * @see OpBackupManager#backupRepository(onepoint.project.OpProjectSession,String)
	 */
    private void backupRepository(OpProjectSession session, File zipFile) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Starting repository backup....");
        OpBroker broker = session.newBroker();
        try {
            FileOutputStream fileOut = new FileOutputStream(zipFile);
            ZipOutputStream zipOut = new ZipOutputStream(fileOut);
            zipOut.putNextEntry(new ZipEntry(DATA_XML));
            XDocumentWriter writer = new XDocumentWriter(zipOut);
            List<Triple<String, OpBackupMember, String>> toExport = new LinkedList<Triple<String, OpBackupMember, String>>();
            writeXMLFile(session, broker, writer, toExport);
            writer.flush();
            zipOut.closeEntry();
            for (Triple<String, OpBackupMember, String> entry : toExport) {
                OpSiteObjectIfc object = broker.getObject(entry.getFirst());
                OpBackupMember member = entry.getSecond();
                String path = entry.getThird();
                Object value = OpBackupLoader.getValueFromObject(object, member);
                zipOut.putNextEntry(new ZipEntry(path));
                if (value instanceof XSizeInputStream) {
                    XSizeInputStream in = (XSizeInputStream) value;
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        zipOut.write(buf, 0, len);
                    }
                    in.close();
                } else {
                    zipOut.write((byte[]) value);
                }
                zipOut.closeEntry();
            }
            zipOut.flush();
            zipOut.close();
            long elapsedTimeSecs = (System.currentTimeMillis() - startTime) / 1000;
            logger.info("Repository backup completed in " + elapsedTimeSecs + " seconds");
        } finally {
            broker.closeAndEvict();
        }
    }

    private void writeXMLFile(OpProjectSession session, OpBroker broker, XDocumentWriter writer, List<Triple<String, OpBackupMember, String>> toExport) throws IOException {
        writer.writeHeader1_0();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(VERSION, String.valueOf(CURRENT_VERSION_NUMBER));
        int dataVersion = session.getSite().getDataVersion();
        attributes.put(SCHEMA_VERSION, String.valueOf(dataVersion));
        logger.info("Schema Version is: " + dataVersion);
        writer.writeStartElement(OPP_BACKUP, attributes, false);
        attributes.clear();
        Map<String, OpBackupMember[]> allMembers = exportPrototypes(writer);
        Map systemIdMap = querySystemObjectIdMap(broker);
        if (systemIdMap != null) {
            for (Map.Entry<String, OpPrototype> pe : prototypes.entrySet()) {
                exportObjects(session, broker, writer, pe.getValue(), allMembers.get(pe.getKey()), systemIdMap, toExport);
            }
        }
        writer.writeEndElement(OPP_BACKUP);
    }

    /**
	 * Restores a repository from the given path.
	 *
	 * @param session a <code>OpProjectSession</code> representing an application session.
	 * @param path    a <code>String</code> representing a path to the backup file.
	 * @throws IOException              if the repository cannot be restored.
	 * @throws SQLException
	 * @throws IllegalArgumentException if the given path does not point to an existing file.
	 */
    public final boolean restoreRepository(OpProjectSession session, String path) throws IOException, SQLException {
        if (!isPathAFile(path, true)) {
            throw new IllegalArgumentException("The given path does not point to an exiting file");
        }
        boolean revalidationRequired = false;
        if (!OpEnvironmentManager.getInstance().isOnDemand()) {
            OpInitializerFactory.getInstance().getInitializer().resetRepository();
            OpSiteManager.setSiteStatus(session.getSite(), OpSite.STATUS_LOCKED);
        } else {
            OpSiteManager.setSiteStatus(session.getSite(), OpSite.STATUS_LOCKED);
            removeAllObjects(session);
        }
        String parentDirectory = null;
        ZipFile zipFile = null;
        if (path.endsWith(OpRepositoryService.ZIP_FILE_EXTENSION)) {
            zipFile = new ZipFile(path);
            revalidationRequired = restoreRepository(session, zipFile);
        } else {
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
            parentDirectory = getParentDirectory(path);
            revalidationRequired = restoreRepository(session, input, parentDirectory);
            input.close();
        }
        postProcessRepository(session);
        OpSiteManager.setSiteStatus(session.getSite(), OpSite.STATUS_OK);
        return revalidationRequired;
    }

    /**
	 * do whatever is required to finalize the import (correct whatever might be wrong with our data)
	 * @param session
	 */
    public void postProcessRepository(OpProjectSession session) {
        OpBroker broker = session.newBroker();
        try {
            OpUserService.createAdministrator(session, broker);
            OpUserService.createEveryone(session, broker);
            session.clearSession();
            String[] persistentClassesWithPermissions = { "OpResource", "OpResourcePool", "OpProjectNode", "OpCustomer", "OpDocumentNode", "OpFolder", "OpReport" };
            for (int i = 0; i < persistentClassesWithPermissions.length; i++) {
                fixPermissionsAfterImport(session, broker, persistentClassesWithPermissions[i]);
            }
        } finally {
            broker.closeAndEvict();
        }
    }

    private void fixPermissionsAfterImport(OpProjectSession session, OpBroker broker, String className) {
        OpTransaction t = null;
        OpUser admin = session.administrator(broker);
        OpGroup everyone = session.everyone(broker);
        if (admin == null) {
            logger.warn("No Administrator user defined, no permissions created...");
        }
        if (everyone == null) {
            logger.warn("No group Everyone defined, no permissions created...");
        }
        try {
            t = broker.newTransaction();
            String qString = "" + "select x from " + className + " as x where not exists (select p from OpPermission as p where p.Object.id = x.id )";
            OpQuery q = broker.newQuery(qString);
            Iterator<OpSiteObject> oit = broker.iterate(q);
            while (oit.hasNext()) {
                OpPermissionable o = (OpPermissionable) oit.next();
                if (o.getPermissions() == null || o.getPermissions().isEmpty()) {
                    logger.info("fixing object without permissions: " + o.locator());
                    if (admin != null) {
                        broker.makePersistent(new OpPermission(o, admin, OpPermission.ADMINISTRATOR));
                    }
                    if (everyone != null) {
                        broker.makePersistent(new OpPermission(o, everyone, OpPermission.OBSERVER));
                    }
                }
            }
            t.commit();
        } finally {
            if (t != null) {
                t.rollbackIfNecessary();
            }
        }
    }

    /**
	 * Removes all the objects from the db.
	 *
	 * @param session a <code>OpProjectSession</code> the server session.
	 * @throws SQLException
	 */
    public void removeAllObjects(OpProjectSession session) throws SQLException {
        logger.info("Removing all objects from the db ");
        OpGraph graph = new OpGraph();
        LinkedHashSet<OpPrototype> allPrototypes = new LinkedHashSet<OpPrototype>();
        for (OpPrototype prototype : prototypes.values()) {
            allPrototypes.add(prototype);
            OpPrototype superType = prototype.getSuperType();
            while (superType != null) {
                if (!allPrototypes.add(superType)) {
                    break;
                }
                superType = superType.getSuperType();
            }
        }
        for (OpPrototype prototype : allPrototypes) {
            OpGraph.Entry node = graph.addNode(prototype);
            OpPrototype superType = prototype.getSuperType();
            if (superType != null) {
                OpGraph.Entry superNode = graph.addNode(superType);
                graph.addEdge(node, superNode, DEFAULT_EDGE_CLASS);
            }
            for (OpPrototype dependent : prototype.getDeleteDependencies()) {
                if (dependent != node.getElem()) {
                    OpGraph.Entry dependentNode = graph.addNode(dependent);
                    graph.addEdge(dependentNode, node, DEFAULT_EDGE_CLASS);
                }
            }
            for (OpPrototype dependent : prototype.getNonInverseNonRecursiveDependencies()) {
                if (dependent != node.getElem()) {
                    OpGraph.Entry dependentNode = graph.addNode(dependent);
                    graph.addEdge(node, dependentNode, DEFAULT_EDGE_CLASS);
                }
            }
        }
        List dependencies = graph.getTopologicOrder();
        OpBroker broker = session.newBroker();
        try {
            long start = System.currentTimeMillis();
            logger.info("Clearing db");
            OpTransaction transaction = broker.newTransaction();
            Iterator i = dependencies.iterator();
            while (i.hasNext()) {
                OpGraph.Entry delete = (OpGraph.Entry) i.next();
                logger.info("unlinking objects of type: " + ((OpPrototype) delete.getElem()).getName());
                unlinkObjectsWithPrototype((OpPrototype) delete.getElem(), session, broker, transaction);
            }
            i = dependencies.iterator();
            while (i.hasNext()) {
                OpGraph.Entry delete = (OpGraph.Entry) i.next();
                logger.info("deleting objects of type: " + ((OpPrototype) delete.getElem()).getName());
                removeObjectsWithPrototype((OpPrototype) delete.getElem(), session, broker, transaction);
            }
            transaction.commit();
            logger.info("Clearing db lasted: " + ((System.currentTimeMillis() - start) / 1000) + " seconds");
        } finally {
            broker.closeAndEvict();
        }
    }

    private void unlinkObjectsWithPrototype(OpPrototype prototype, OpProjectSession session, OpBroker broker, OpTransaction transaction) {
        List<OpRelationship> relations = prototype.getRelationships();
        for (OpRelationship relationship : relations) {
            if (!relationship.getInverse() && (!OpTypeManager.getPrototypeByID(relationship.getTypeID()).isInterface())) {
                OpRelationship backRelationship = relationship.getBackRelationship();
                String cascadeMode = backRelationship != null ? backRelationship.getCascadeMode() : null;
                if (!OpRelationship.CASCADE_ALL.equals(cascadeMode) && !OpRelationship.CASCADE_DELETE.equals(cascadeMode)) {
                    OpPrototype relPt = OpTypeManager.getPrototypeByID(relationship.getTypeID());
                    if (relPt != null && isBackupablePrototype(relPt)) {
                        String query = "update " + prototype.getInstanceClass().getName() + " obj set obj." + relationship.getName() + " = null";
                        OpQuery objectsQuery = broker.newQuery(query);
                        broker.execute(objectsQuery);
                    }
                }
            }
        }
    }

    /**
	 * Removes all the objects with the given prototype name from the db.
	 * This takes into account the recursive relationship order. So it is guaranteed that
	 * if object A references B (e.g. as its parent) A is deleted prior to B.
	 *
	 * @param prototype prototype to delete.
	 * @param session   a <code>OpProjectSession</code> the server session.
	 * @param transaction
	 */
    private void removeObjectsWithPrototype(OpPrototype prototype, OpProjectSession session, OpBroker broker, OpTransaction transaction) {
        if (prototype.isInterface() || !isBackupablePrototype(prototype)) {
            return;
        }
        String query = "delete " + prototype.getInstanceClass().getName();
        OpQuery objectsQuery = broker.newQuery(query);
        broker.execute(objectsQuery);
    }

    /**
	 * Get the identifiers of the objects for a given prototype.
	 *
	 * @param session       session to use
	 * @param prototypeName prototype for which to get the ids
	 * @param recursiveRelationshipCondition
	 * @return a <code>List<Long></code> of object identifiers
	 */
    private List<Long> getObjectsWithPrototype(OpProjectSession session, OpBroker broker, OpPrototype prototype, String recursiveRelationshipCondition, Byte outlineLevel) {
        String whereClause = recursiveRelationshipCondition == null || recursiveRelationshipCondition.length() == 0 ? "" : recursiveRelationshipCondition;
        String queryString = "select obj.id from " + prototype.getInstanceClass().getName() + " obj" + whereClause;
        OpQuery query = broker.newQuery(queryString);
        if (outlineLevel != null) {
            query.setByte("outlineLevel", outlineLevel);
        }
        logger.info("before getObjectsWithPrototype() query: " + queryString + (outlineLevel == null ? "" : ", outlineLevel : " + outlineLevel));
        long start = System.currentTimeMillis();
        List<Long> oids = broker.list(query, Long.class);
        logger.info("after getObjectsWithPrototype() query: " + queryString + (outlineLevel == null ? "" : ", outlineLevel : " + outlineLevel) + ", size: " + oids.size() + ", lasted: " + (System.currentTimeMillis() - start));
        return oids;
    }

    /**
	 * @see OpBackupManager#restoreRepository(onepoint.project.OpProjectSession,String)
	 */
    private boolean restoreRepository(OpProjectSession session, InputStream input, String workingDirectory) {
        long start = System.currentTimeMillis();
        logger.info("Restoring repository...");
        OpBackupLoader backupLoader = new OpBackupLoader();
        backupLoader.loadBackup(session, input, workingDirectory);
        long elapsedTimeSecs = (System.currentTimeMillis() - start) / 1000;
        updateCustomValuePages(session);
        logger.info("Repository restore completed in " + elapsedTimeSecs + " seconds");
        return backupLoader.isRevalidateRequired();
    }

    /**
    * @throws IOException
	 * @see OpBackupManager#restoreRepository(onepoint.project.OpProjectSession,String)
    */
    private boolean restoreRepository(OpProjectSession session, ZipFile zipFile) throws IOException {
        long start = System.currentTimeMillis();
        logger.info("Restoring repository...");
        OpBackupLoader backupLoader = new OpBackupLoader();
        backupLoader.loadBackup(session, zipFile);
        long elapsedTimeSecs = (System.currentTimeMillis() - start) / 1000;
        updateCustomValuePages(session);
        logger.info("Repository restore completed in " + elapsedTimeSecs + " seconds");
        return backupLoader.isRevalidateRequired();
    }

    private void updateCustomValuePages(OpProjectSession session) {
        OpBroker broker = session.newBroker();
        try {
            OpTransaction t = broker.newTransaction();
            int cnt = 0;
            List<Pair<OpCustomValuePage, OpCustomizable>> cvL = new LinkedList<Pair<OpCustomValuePage, OpCustomizable>>();
            OpQuery query = broker.newQuery("select cvp, cvp.Object from OpCustomValuePage as cvp where cvp.Sequence = 0");
            Iterator iter = broker.iterate(query);
            while (iter.hasNext()) {
                Object[] objs = (Object[]) iter.next();
                OpCustomValuePage page = (OpCustomValuePage) objs[0];
                OpCustomizable obj = (OpCustomizable) objs[1];
                cvL.add(new Pair<OpCustomValuePage, OpCustomizable>(page, obj));
            }
            t.commit();
            t = broker.newTransaction();
            for (Pair<OpCustomValuePage, OpCustomizable> cve : cvL) {
                if (cve.getSecond() != null && cve.getSecond().getId() != 0) {
                    cve.getSecond().setCustomValuePage(cve.getFirst());
                    cnt++;
                    if (cnt % 1000 == 0) {
                        t.commit();
                        t = broker.newTransaction();
                    }
                }
            }
            t.commit();
        } finally {
            broker.close();
        }
    }

    /**
	 * Generates a string that represents a path to the file that will be written with the binary content.
	 *
	 * @param id               a <code>long</code> representing the id of the object that has the content.
	 * @param backupMemberName a <code>String</code> representing the name of the object's property which has the content.
	 * @return a <code>String</code> representing the name of the binary file.
	 */
    private String binaryFilePath(long id, List<String> backupMemberName) {
        StringBuffer pathBuffer = new StringBuffer("/object-");
        pathBuffer.append(id);
        pathBuffer.append('-');
        pathBuffer.append(System.currentTimeMillis());
        pathBuffer.append('-');
        pathBuffer.append(OpMember.namesToString(backupMemberName));
        pathBuffer.append(".bin");
        return pathBuffer.toString();
    }

    /**
	 * Reads the contents of a binary file from the given path.
	 *
	 * @param path a <code>String</code> representing the path to a binary file.
	 * @return a <code>XSizeInputStream</code> with the file's content.
	 */
    static XSizeInputStream readBinaryFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            logger.error("Missing file to restore: " + path);
            throw new XServiceException("Missing file to restore: " + path);
        }
        return new XSizeInputStream(file, file.length());
    }

    /**
	 * Registers a prototype with the backup manager, taking into account the prototype's dependencies.
	 *
	 * @param superPrototype           a <code>OpPrototype</code> representing OpObject's prototype.
	 * @param startPoint               a <code>OpPrototype</code> representing a start point in the back-up registration process.
	 * @param lastPrototypesToRegister a <code>List</code> which acts as an acumulator and will contain at the end a list
	 *                                 of prototypes which will be registered at the end of all the others.
	 */
    private void registerPrototypeForBackup(OpPrototype superPrototype, OpPrototype startPoint, List<OpPrototype> lastPrototypesToRegister) {
        List dependencies = startPoint.getSubsequentBackupDependencies();
        logger.debug("start point is: " + startPoint.getName());
        for (Object dependency1 : dependencies) {
            OpPrototype dependency = (OpPrototype) dependency1;
            if (dependency.getID() == superPrototype.getID()) {
                if (!lastPrototypesToRegister.contains(startPoint)) {
                    lastPrototypesToRegister.add(startPoint);
                }
            } else if (!OpBackupManager.hasRegistered(dependency)) {
                logger.debug("dependency is: " + dependency.getName());
                registerPrototypeForBackup(superPrototype, dependency, lastPrototypesToRegister);
            }
        }
        if (!startPoint.subTypes().hasNext() && !OpBackupManager.hasRegistered(startPoint)) {
            if (!lastPrototypesToRegister.contains(startPoint)) {
                addPrototype(startPoint);
            }
        }
    }

    public static void setValueInObject(Object object, List<Field> path, Object value) {
        Iterator<Field> fit = path.iterator();
        Object target = object;
        while (fit.hasNext() && target != null) {
            Field f = fit.next();
            if (!fit.hasNext()) {
                try {
                    f.set(target, value);
                } catch (IllegalArgumentException e) {
                    logger.error("Could not restore property value for " + f.getName() + " in class " + target.getClass().getName(), e);
                } catch (IllegalAccessException e) {
                    logger.error("Could not restore property value for " + f.getName() + " in class " + target.getClass().getName(), e);
                }
            } else {
                target = OpPrototype.getFieldValue(target, f);
            }
        }
    }

    public static Object getValueFromObject(Object object, List<Field> path) {
        Iterator<Field> fit = path.iterator();
        Object result = object;
        while (fit.hasNext() && result != null) {
            Field f = fit.next();
            result = OpPrototype.getFieldValue(result, f);
        }
        return result;
    }

    public static Class getParentClassByPath(Class object, List<Field> path) {
        Iterator<Field> fit = path.iterator();
        Class result = object;
        while (fit.hasNext() && result != null) {
            Field f = fit.next();
            if (fit.hasNext()) {
                result = f.getType();
            }
        }
        return result;
    }
}

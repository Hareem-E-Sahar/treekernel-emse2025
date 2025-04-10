package org.eclipse.mylyn.internal.tasks.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.tasks.core.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.TaskExternalizationException;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskListFactory;
import org.eclipse.mylyn.tasks.core.TaskList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Mik Kersten
 * @author Ken Sueda
 * @author Rob Elves
 * @author Jevgeni Holodkov
 * 
 * TODO: move to core?
 */
public class TaskListWriter {

    private static final String TRANSFORM_PROPERTY_VERSION = "version";

    private static final String XML_VERSION = "1.0";

    public static final String ATTRIBUTE_VERSION = "Version";

    public static final String ELEMENT_TASK_LIST = "TaskList";

    private static final String VALUE_VERSION = "1.0.1";

    private static final String VALUE_VERSION_1_0_0 = "1.0.0";

    private static final String FILE_SUFFIX_SAVE = "save.xml";

    private List<AbstractTaskListFactory> externalizers;

    private DelegatingTaskExternalizer delagatingExternalizer;

    private List<Node> orphanedTaskNodes = new ArrayList<Node>();

    private List<Node> orphanedQueryNodes = new ArrayList<Node>();

    private String readVersion = "";

    private boolean hasCaughtException = false;

    public TaskListWriter() {
        this.delagatingExternalizer = new DelegatingTaskExternalizer();
    }

    public void setDelegateExternalizers(List<AbstractTaskListFactory> externalizers) {
        this.externalizers = externalizers;
        this.delagatingExternalizer.setFactories(externalizers);
    }

    public void writeTaskList(TaskList taskList, File outFile) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.newDocument();
        } catch (ParserConfigurationException e) {
            StatusHandler.log(e, "could not create document");
            return;
        }
        Element root = doc.createElement(ELEMENT_TASK_LIST);
        root.setAttribute(ATTRIBUTE_VERSION, VALUE_VERSION);
        for (AbstractTaskContainer category : taskList.getCategories()) {
            delagatingExternalizer.createCategoryElement(category, doc, root);
        }
        for (AbstractRepositoryQuery query : taskList.getQueries()) {
            try {
                delagatingExternalizer.createQueryElement(query, doc, root);
            } catch (Throwable t) {
                StatusHandler.fail(t, "Did not externalize: " + query.getSummary(), true);
            }
        }
        for (AbstractTask task : taskList.getAllTasks()) {
            delagatingExternalizer.createTaskElement(task, doc, root);
        }
        for (Node orphanedTaskNode : orphanedTaskNodes) {
            Node tempNode = doc.importNode(orphanedTaskNode, true);
            if (tempNode != null) {
                root.appendChild(tempNode);
            }
        }
        for (Node orphanedQueryNode : orphanedQueryNodes) {
            Node tempNode = doc.importNode(orphanedQueryNode, true);
            if (tempNode != null) {
                root.appendChild(tempNode);
            }
        }
        doc.appendChild(root);
        writeDOMtoFile(doc, outFile);
        return;
    }

    /**
	 * Writes an XML file from a DOM.
	 * 
	 * doc - the document to write file - the file to be written to
	 */
    private void writeDOMtoFile(Document doc, File file) {
        try {
            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry zipEntry = new ZipEntry(ITasksUiConstants.OLD_TASK_LIST_FILE);
            outputStream.putNextEntry(zipEntry);
            outputStream.setMethod(ZipOutputStream.DEFLATED);
            writeDOMtoStream(doc, outputStream);
            outputStream.flush();
            outputStream.closeEntry();
            outputStream.close();
        } catch (Exception fnfe) {
            StatusHandler.log(fnfe, "TaskList could not be found");
        }
    }

    /**
	 * Writes the provided XML document out to the specified output stream.
	 * 
	 * doc - the document to be written outputStream - the stream to which the document is to be written
	 */
    private void writeDOMtoStream(Document doc, OutputStream outputStream) {
        Source source = new DOMSource(doc);
        Result result = new StreamResult(outputStream);
        Transformer xformer = null;
        try {
            xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(TRANSFORM_PROPERTY_VERSION, XML_VERSION);
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (TransformerException e1) {
            e1.printStackTrace();
        }
    }

    /**
	 * TODO: fix this old mess
	 */
    public void readTaskList(TaskList taskList, File inFile, TaskDataManager taskDataManager) {
        hasCaughtException = false;
        Map<AbstractTask, NodeList> tasksWithSubtasks = new HashMap<AbstractTask, NodeList>();
        orphanedTaskNodes.clear();
        orphanedQueryNodes.clear();
        try {
            if (!inFile.exists()) return;
            Document doc = openAsDOM(inFile);
            if (doc == null) {
                handleException(inFile, null, new TaskExternalizationException("TaskList was not well formed XML"));
                return;
            }
            Element root = doc.getDocumentElement();
            readVersion = root.getAttribute(ATTRIBUTE_VERSION);
            if (readVersion.equals(VALUE_VERSION_1_0_0)) {
                StatusHandler.log("version: " + readVersion + " not supported", this);
            } else {
                NodeList list = root.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node child = list.item(i);
                    try {
                        if (child.getNodeName().endsWith(DelegatingTaskExternalizer.KEY_CATEGORY)) {
                            delagatingExternalizer.readCategory(child, taskList);
                        }
                    } catch (Exception e) {
                        handleException(inFile, child, e);
                    }
                }
                for (int i = 0; i < list.getLength(); i++) {
                    Node child = list.item(i);
                    try {
                        if (!child.getNodeName().endsWith(DelegatingTaskExternalizer.KEY_CATEGORY) && !child.getNodeName().endsWith(AbstractTaskListFactory.KEY_QUERY)) {
                            AbstractTask task = delagatingExternalizer.readTask(child, taskList, null, null);
                            if (task == null) {
                                orphanedTaskNodes.add(child);
                            } else {
                                if (child.getChildNodes() != null && child.getChildNodes().getLength() > 0) {
                                    tasksWithSubtasks.put(task, child.getChildNodes());
                                }
                            }
                        }
                    } catch (Exception e) {
                        handleException(inFile, child, e);
                    }
                }
                for (AbstractTask task : tasksWithSubtasks.keySet()) {
                    NodeList nodes = tasksWithSubtasks.get(task);
                    delagatingExternalizer.readSubTasks(task, nodes, taskList);
                }
                for (int i = 0; i < list.getLength(); i++) {
                    Node child = list.item(i);
                    try {
                        if (child.getNodeName().endsWith(AbstractTaskListFactory.KEY_QUERY)) {
                            readQuery(taskList, child);
                        }
                    } catch (Exception e) {
                        handleException(inFile, child, e);
                    }
                }
                int largest = taskList.findLargestTaskId();
                taskList.setLastLocalTaskId(largest);
            }
        } catch (Exception e) {
            handleException(inFile, null, e);
        }
        if (hasCaughtException) {
            writeTaskList(taskList, inFile);
        }
    }

    /** 
	 * Reads the Query from the specified Node. If taskList is not null, then also adds this query to the TaskList
	 */
    private AbstractRepositoryQuery readQuery(TaskList taskList, Node child) {
        AbstractRepositoryQuery query = null;
        for (AbstractTaskListFactory externalizer : externalizers) {
            Set<String> queryTagNames = externalizer.getQueryElementNames();
            if (queryTagNames != null && queryTagNames.contains(child.getNodeName())) {
                Element childElement = (Element) child;
                String repositoryUrl = childElement.getAttribute(DelegatingTaskExternalizer.KEY_REPOSITORY_URL);
                String queryString = childElement.getAttribute(AbstractTaskListFactory.KEY_QUERY_STRING);
                if (queryString.length() == 0) {
                    queryString = childElement.getAttribute(AbstractTaskListFactory.KEY_QUERY);
                }
                String label = childElement.getAttribute(DelegatingTaskExternalizer.KEY_NAME);
                if (label.length() == 0) {
                    label = childElement.getAttribute(DelegatingTaskExternalizer.KEY_LABEL);
                }
                query = externalizer.createQuery(repositoryUrl, queryString, label, childElement);
                if (query != null) {
                    if (childElement.getAttribute(DelegatingTaskExternalizer.KEY_LAST_REFRESH) != null && !childElement.getAttribute(DelegatingTaskExternalizer.KEY_LAST_REFRESH).equals("")) {
                        query.setLastSynchronizedStamp(childElement.getAttribute(DelegatingTaskExternalizer.KEY_LAST_REFRESH));
                    }
                }
                if (taskList != null) {
                    if (query != null) {
                        taskList.internalAddQuery(query);
                    }
                    NodeList queryChildren = child.getChildNodes();
                    for (int ii = 0; ii < queryChildren.getLength(); ii++) {
                        Node queryNode = queryChildren.item(ii);
                        try {
                            delagatingExternalizer.readQueryHit((Element) queryNode, taskList, query);
                        } catch (TaskExternalizationException e) {
                            hasCaughtException = true;
                        }
                    }
                }
                break;
            }
        }
        if (query == null) {
            orphanedQueryNodes.add(child);
        }
        return query;
    }

    /**
	 * Opens the specified XML file and parses it into a DOM Document.
	 * 
	 * Filename - the name of the file to open Return - the Document built from the XML file Throws - XMLException if
	 * the file cannot be parsed as XML - IOException if the file cannot be opened
	 */
    private Document openAsDOM(File inputFile) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            inputFile.renameTo(new File(inputFile.getName() + FILE_SUFFIX_SAVE));
            IOException ioe = new IOException("Failed to load XML file");
            ioe.initCause(pce);
            throw ioe;
        }
        try {
            InputStream inputStream = null;
            if (inputFile.getName().endsWith(ITasksUiConstants.FILE_EXTENSION)) {
                inputStream = new ZipInputStream(new FileInputStream(inputFile));
                ((ZipInputStream) inputStream).getNextEntry();
            } else {
                inputStream = new FileInputStream(inputFile);
            }
            document = builder.parse(inputStream);
        } catch (SAXException se) {
            MessageDialog.openWarning(null, "Mylyn task list corrupt", "Unable to read the Mylyn task list. Please restore from previous backup via File > Import > Mylyn Task Data");
        }
        return document;
    }

    private void handleException(File inFile, Node child, Exception e) {
        hasCaughtException = true;
        String name = inFile.getAbsolutePath();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yy-MM-dd-ss");
        name = name.substring(0, name.lastIndexOf('.')) + "-failed-" + sdf.format(date) + ".zip";
        File save = new File(name);
        if (save.exists()) {
            if (!save.delete()) {
                StatusHandler.log("Unable to delete old backup tasklist file", this);
                return;
            }
        }
        if (!copy(inFile, save)) {
            inFile.renameTo(new File(name));
        }
        if (child == null) {
            StatusHandler.log(e, ITasksUiConstants.MESSAGE_RESTORE);
        } else {
            e.printStackTrace();
            StatusHandler.log(e, "Tasks may have been lost from " + child.getNodeName());
        }
    }

    private boolean copy(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    public void setDelegatingExternalizer(DelegatingTaskExternalizer delagatingExternalizer) {
        this.delagatingExternalizer = delagatingExternalizer;
    }

    public List<AbstractTaskListFactory> getExternalizers() {
        return externalizers;
    }

    public void writeQueries(List<AbstractRepositoryQuery> queries, File outFile) {
        Document doc = createQueryDocument(queries);
        if (doc != null) {
            writeDOMtoFile(doc, outFile);
        }
        return;
    }

    /**
	 * @return null if it was not possible to create the Query document.
	 */
    public Document createQueryDocument(List<AbstractRepositoryQuery> queries) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.newDocument();
        } catch (ParserConfigurationException e) {
            StatusHandler.log(e, "could not create document");
            return null;
        }
        Element root = doc.createElement(ELEMENT_TASK_LIST);
        root.setAttribute(ATTRIBUTE_VERSION, VALUE_VERSION);
        for (AbstractRepositoryQuery query : queries) {
            try {
                delagatingExternalizer.createQueryElement(query, doc, root);
            } catch (Throwable t) {
                StatusHandler.fail(t, "Did not externalize: " + query.getSummary(), true);
                return null;
            }
        }
        doc.appendChild(root);
        return doc;
    }

    public List<AbstractRepositoryQuery> readQueries(File inFile) {
        List<AbstractRepositoryQuery> queries = new ArrayList<AbstractRepositoryQuery>();
        try {
            if (!inFile.exists()) return queries;
            Document doc = openAsDOM(inFile);
            if (doc == null) {
                handleException(inFile, null, new TaskExternalizationException("TaskList was not well formed XML"));
                return queries;
            }
            queries = readQueryDocument(doc);
        } catch (Exception e) {
            handleException(inFile, null, e);
        }
        return queries;
    }

    /**
	 * @param Query document to read.
	 */
    public List<AbstractRepositoryQuery> readQueryDocument(Document doc) {
        List<AbstractRepositoryQuery> queries = new ArrayList<AbstractRepositoryQuery>();
        Element root = doc.getDocumentElement();
        readVersion = root.getAttribute(ATTRIBUTE_VERSION);
        if (!readVersion.equals(VALUE_VERSION_1_0_0)) {
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node child = list.item(i);
                try {
                    if (child.getNodeName().endsWith(AbstractTaskListFactory.KEY_QUERY)) {
                        AbstractRepositoryQuery query = readQuery(null, child);
                        if (query != null) {
                            queries.add(query);
                        }
                    }
                } catch (Exception e) {
                    StatusHandler.log(e, "Tasks may have been lost from " + child.getNodeName());
                }
            }
        } else {
            StatusHandler.log("version: " + readVersion + " not supported", this);
        }
        return queries;
    }
}

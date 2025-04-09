package ru.adv.web.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import ru.adv.logger.TLogger;
import ru.adv.util.XmlUtils;

/**
 * Special class for analyze log files
 * for interrupt tasks and restoring mail
 * sending process from last working point
 * 
 * @author Tsarev Oleg
 *
 */
public class MailRestorer {

    private TLogger logger = new TLogger(MailRestorer.class);

    /**
	 * Static prefix for restored xml files 
	 */
    private static final String RESTORE_PREFIX = "_restored_";

    /**
	 * Special variables
	 */
    private MailService service;

    private File logDirectory;

    private String lastConfig;

    private Integer lastObject = 0;

    private Integer lastRecord = 0;

    private List<Element> configs;

    private String taskId;

    private String templateHref;

    private String query;

    /**
	 * Constructor with {@link MailService} object
	 * and logging directory as parameter  
	 * 
	 * @param service
	 * @param logDirectory
	 */
    public MailRestorer(MailService service, String logDirectory) {
        setService(service);
        this.logDirectory = new File(logDirectory);
    }

    /**
	 * Main method that read all xml files
	 * check interrupted sending
	 * 
	 */
    public void run() {
        if (logDirectory.exists() && logDirectory.isDirectory()) {
            File[] taskReports = logDirectory.listFiles(new PrefixExtentionFilter(MailLogger.TASK_FILE_PRAFIX, MailLogger.XML_EXTANTION));
            for (int i = 0, len = taskReports.length; i < len; i++) {
                if (!taskReports[i].getName().contains(RESTORE_PREFIX)) {
                    if (checkTaskFromLog(taskReports[i])) {
                        MailTask mailTask = new MailTask();
                        mailTask.setMailService(getService());
                        mailTask.setConfigElements(getConfigs());
                        mailTask.setConfigName(MailTask.ALL_CONFIGS_IDENTIFICATOR);
                        mailTask.setTemplateHref(getTemplate());
                        mailTask.setTaskId(getTaskId());
                        HashMap<String, Object> restorateMap = new HashMap<String, Object>();
                        restorateMap.put(MailTask.LAST_CONFIG, getLastConfig());
                        restorateMap.put(MailTask.LAST_OBJECT, getLastObject());
                        restorateMap.put(MailTask.LAST_RECORD, getLastRecord());
                        restorateMap.put(MailTask.QUERY, getQuery());
                        mailTask.setRestoreMap(restorateMap);
                        try {
                            mailTask.start();
                        } catch (MailException e) {
                            logger.info("MailRestorer: Cannot start mail sending task - " + e.getMessage());
                        }
                        taskReports[i].delete();
                    }
                }
            }
        }
    }

    /**
	 * Checker for log files by restoring xml file.
	 * Read all files and save last successed row,
	 * object and configuration structure.
	 * 
	 * 
	 * @param xml file
	 * @return true - if last success object founded,
	 *         false - otherwise
	 */
    private boolean checkTaskFromLog(File file) {
        Document document = loadDocument(file);
        if (document != null) {
            Element root = document.getDocumentElement();
            if (root.getTagName().equals(MailLogger.TASK_NODE)) {
                String taskId = readTaskId(root);
                String fileName = file.getName();
                String withoutExtention = fileName.substring(0, fileName.lastIndexOf("."));
                Integer realFilenameLength = withoutExtention.length();
                String date = withoutExtention.substring(realFilenameLength - 6, realFilenameLength);
                List<Element> configs = XmlUtils.findAllElements(root, MailTask.CONFIG_NAME, true);
                for (Element config : configs) {
                    if (isNotSending(config)) {
                        setLastConfig(config.getAttribute(MailTask.CONFIG_NAME_ATTRIBUTE));
                        if (isInterrupt(config)) {
                            readLogByTask(taskId + date, config.getAttribute(MailTask.CONFIG_NAME_ATTRIBUTE));
                        }
                        setTemplate(XmlUtils.findFirstElement(root, MailLogger.TEMPLATE_NODE).getAttribute(MailLogger.TEMPLATE_HREF));
                        setQuery(XmlUtils.findFirstElement(root, MailLogger.QUERY_NODE).getTextContent());
                        setConfigs(configs);
                        setTaskId(taskId + date + RESTORE_PREFIX);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
	 * Special method for low level reading
	 * log file
	 * 
	 * @param task file identificator
	 * @param configuration name
	 */
    private void readLogByTask(String taskFileId, String configName) {
        File log = new File(logDirectory.getPath() + File.separator + taskFileId + "_" + configName + MailLogger.LOG_EXTANTION);
        if (log.exists()) {
            String last = getLastString(log);
            if (last != null) {
                parseString(last);
            }
            log.delete();
        }
    }

    /**
	 * Special method that return last string
	 * from file
	 * 
	 * @param log file
	 * @return
	 */
    private String getLastString(File log) {
        String result = null;
        try {
            BufferedReader stream = new BufferedReader(new FileReader(log));
            String next;
            do {
                next = stream.readLine();
                if (next != null) {
                    result = next;
                }
            } while (next != null);
            stream.close();
            return result;
        } catch (FileNotFoundException e) {
            logger.info("MailRestorer: Cannot find file...");
        } catch (IOException e) {
            logger.info("MailRestorer: Cannot read file...");
        }
        return null;
    }

    /**
	 * Special parser for last string
	 * to get object and record numbers
	 * 
	 * @param last string
	 */
    private void parseString(String last) {
        StringTokenizer entities = new StringTokenizer(last, MailLogger.PART_SEPARATOR);
        while (entities.hasMoreElements()) {
            String part = (String) entities.nextElement();
            String[] map = part.split(MailLogger.MAP_SEPARATOR);
            if (map.length == 2) {
                if (map[0].equals(MailLogger.OBJECT_STRING)) {
                    setLastObject(Integer.parseInt(map[1]));
                } else if (map[0].equals(MailLogger.ELEMENT_STRING)) {
                    setLastRecord(Integer.parseInt(map[1]));
                }
            }
        }
    }

    /**
	 * Simple method to read task
	 * identificator from xml structure
	 * 
	 * @param root element
	 * @return string identificator
	 */
    private String readTaskId(Element root) {
        if (root.hasAttribute(MailLogger.TASK_ID)) {
            return root.getAttribute(MailLogger.TASK_ID);
        }
        return "";
    }

    /**
	 * Special property method to detect
	 * when sending is not end or start
	 * 
	 * @param config
	 * @return true - not sending success,
	 *         false - otherwise
	 */
    private boolean isNotSending(Element config) {
        if (isInterrupt(config) || !config.hasAttribute(MailLogger.CONFIG_STATUS_ATTRIBUTE)) {
            return true;
        }
        return false;
    }

    /**
	 * Special property method to detect
	 * when sending was interrupt
	 * 
	 * @param config
	 * @return true - sending interrupt,
	 * 		   false - otherwise
	 */
    private boolean isInterrupt(Element config) {
        if (config.hasAttribute(MailLogger.CONFIG_STATUS_ATTRIBUTE) && config.getAttribute(MailLogger.CONFIG_STATUS_ATTRIBUTE).equals(MailLogger.INTERRUPT_STATUS)) {
            return true;
        }
        return false;
    }

    /**
	 * Simple loader to parse restoring
	 * xml files
	 * 
	 * @param file
	 * @return document
	 */
    private Document loadDocument(File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(file);
        } catch (ParserConfigurationException e) {
            logger.info("MailRestorer: Cannot parse xml file...");
        } catch (SAXException e) {
            logger.info("MailRestorer: Cannot parse xml file...");
        } catch (IOException e) {
            logger.info("MailRestorer: Cannot read file...");
        }
        return null;
    }

    /**
	 * Setter for {@link MailService} object
	 * 
	 * @param service
	 */
    private void setService(MailService service) {
        this.service = service;
    }

    /**
	 * Getter for {@link MailService} object
	 * 
	 * @return service
	 */
    private MailService getService() {
        return service;
    }

    /**
	 * Setter for last configuration
	 * name from logs
	 * 
	 * @param  configuration name
	 */
    private void setLastConfig(String lastConfig) {
        this.lastConfig = lastConfig;
    }

    /**
	 * Getter for last configuration
	 * name from logs
	 * 
	 * @return  configuration name
	 */
    private String getLastConfig() {
        return lastConfig;
    }

    /**
	 * Setter for last object
	 * number from logs
	 * 
	 * @param  object number
	 */
    private void setLastObject(Integer lastObject) {
        this.lastObject = lastObject;
    }

    /**
	 * Getter for last object
	 * number from logs
	 * 
	 * @return  object number
	 */
    private Integer getLastObject() {
        return lastObject;
    }

    /**
	 * Setter for last record
	 * number from logs
	 * 
	 * @param  record number
	 */
    private void setLastRecord(Integer lastRecord) {
        this.lastRecord = lastRecord;
    }

    /**
	 * Getter for last record
	 * number from logs
	 * 
	 * @return  record number
	 */
    private Integer getLastRecord() {
        return lastRecord;
    }

    /**
	 * Setter for configuration
	 * elements
	 * 
	 * @param  list of elements
	 */
    private void setConfigs(List<Element> configs) {
        this.configs = configs;
    }

    /**
	 * Getter for configuration
	 * elements
	 * 
	 * @return  list of elements
	 */
    private List<Element> getConfigs() {
        return configs;
    }

    /**
	 * Setter task identificator
	 * 
	 * @param  task identificator stirng
	 */
    private void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
	 * Getter task identificator
	 * 
	 * @return  task identificator stirng
	 */
    private String getTaskId() {
        return taskId;
    }

    /**
	 * Setter template href
	 * 
	 * @param template href string
	 */
    private void setTemplate(String templateHref) {
        this.templateHref = templateHref;
    }

    /**
	 * Getter template href
	 * 
	 * @return template href string
	 */
    private String getTemplate() {
        return templateHref;
    }

    /**
	 * Setter for query
	 * {@see MailSender}
	 * 
	 * @param query string
	 */
    private void setQuery(String query) {
        if (query != null) {
            this.query = query;
        } else {
            this.query = "";
        }
    }

    /**
	 * Getter for query
	 * {@see MailSender}
	 * 
	 * @return query string
	 */
    private String getQuery() {
        return query;
    }

    /**
	 * Simple util class for filtering
	 * files by prefix and postfix (extantion)
 	 * 
	 * @author Tsarev Oleg
	 *
	 */
    class PrefixExtentionFilter implements FilenameFilter {

        private String prefix;

        private String extention;

        public PrefixExtentionFilter(String prefix, String extention) {
            this.prefix = prefix;
            this.extention = extention;
        }

        public boolean accept(File directory, String name) {
            return (name.startsWith(prefix) && name.endsWith(extention) && directory.canRead());
        }
    }
}

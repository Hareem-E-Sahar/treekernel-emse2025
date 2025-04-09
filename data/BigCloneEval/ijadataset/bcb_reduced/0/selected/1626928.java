package com.strangebreeds.therefromhere.engine;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.strangebreeds.therefromhere.XMLCreator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import com.strangebreeds.therefromhere.StatusBean;
import com.strangebreeds.therefromhere.PageType;

public class WikiPageEngine {

    private Logger logger = Logger.getLogger(WikiPageEngine.class);

    private DataAccess dataAccess = new DataAccess();

    private StatusBean statusBean = new StatusBean();

    private long rowNum = 0;

    private String fromPage;

    private String toPage;

    private String strategy;

    private String emailAddress;

    private Integer jobID;

    String workDirectory;

    Database myDatabase = null;

    Database myClassDb = null;

    Database pagesSeenDatabase = null;

    Environment myDbEnvironment = null;

    StoredClassCatalog classCatalog = null;

    EntryBinding dataBinding = null;

    {
        org.apache.log4j.PropertyConfigurator.configure("log4j.properties");
        logger.setLevel(Level.DEBUG);
    }

    public WikiPageEngine(String fromPage, String toPage, String strategy, String emailAddress, Integer jobID) {
        this.fromPage = fromPage;
        this.toPage = toPage;
        this.strategy = strategy;
        this.emailAddress = emailAddress;
        this.jobID = jobID;
        setupDB();
        runJob();
        closeDB();
        destroyWorkDirectory(new File(workDirectory));
    }

    private void createWorkDirectory() {
        workDirectory = Configuration.getBaseWorkDirectory();
        workDirectory += "/";
        workDirectory += fromPage;
        workDirectory += "-";
        workDirectory += toPage;
        workDirectory += "-";
        workDirectory += jobID;
        boolean success = (new File(workDirectory)).mkdir();
        if (success) {
            logger.debug("Created the " + workDirectory + " directory for job ID " + jobID);
        } else {
            logger.error("Could not create the directory " + workDirectory + "!");
        }
    }

    private boolean destroyWorkDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    destroyWorkDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    private void setupDB() {
        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            envConfig.setLockTimeout(0);
            envConfig.setLocking(false);
            createWorkDirectory();
            myDbEnvironment = new Environment(new File(workDirectory), envConfig);
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            myDatabase = myDbEnvironment.openDatabase(null, "wikiDatabase" + jobID.toString(), dbConfig);
            dbConfig.setSortedDuplicates(false);
            myClassDb = myDbEnvironment.openDatabase(null, "classDb" + jobID.toString(), dbConfig);
            classCatalog = new StoredClassCatalog(myClassDb);
            dataBinding = new SerialBinding(classCatalog, Page.class);
            DatabaseConfig pagesSeenConfig = new DatabaseConfig();
            pagesSeenConfig.setAllowCreate(true);
            pagesSeenDatabase = myDbEnvironment.openDatabase(null, "wikiPagesSeenDatabase" + jobID.toString(), pagesSeenConfig);
        } catch (DatabaseException dbe) {
            logger.error("Got DatabaseException: " + dbe.toString(), dbe);
        }
    }

    private void closeDB() {
        try {
            if (pagesSeenDatabase != null) {
                pagesSeenDatabase.close();
            }
            if (myClassDb != null) {
                myClassDb.close();
            }
            if (myDatabase != null) {
                myDatabase.close();
            }
            if (myDbEnvironment != null) {
                myDbEnvironment.cleanLog();
                myDbEnvironment.close();
            }
        } catch (DatabaseException dbe) {
            logger.error("Got DatabaseException: " + dbe.toString(), dbe);
        }
    }

    private void runJob() {
        Page topPage = new Page(fromPage, true);
        addToWorkDB(topPage);
        Page finalPage = null;
        boolean isDone = false;
        while (isDone == false) {
            Page workPage = getFromWorkDB();
            statusBean.sendPageName(jobID, PageType.WORK_PAGE, workPage.getPageName());
            if (workPage.getPageName().compareToIgnoreCase(toPage) == 0) {
                finalPage = workPage;
                isDone = true;
            } else {
                List<String> children = dataAccess.getChildPages(workPage.getPageName());
                for (String child : children) {
                    statusBean.sendPageName(jobID, PageType.CHILD_PAGE, child);
                    if (hasPageBeenSeen(child) == false) {
                        if (child.compareToIgnoreCase(toPage) == 0) {
                            List<String> ancestors = workPage.getAncestors();
                            if (ancestors.size() > 0) {
                                if (ancestors.get(ancestors.size() - 1).compareTo(workPage.getPageName()) != 0) ancestors.add(workPage.getPageName());
                            }
                            Page finalChildPage = new Page(child, ancestors);
                            finalPage = finalChildPage;
                            isDone = true;
                            break;
                        } else {
                            List<String> ancestors = null;
                            if (workPage.isTop()) {
                                ancestors = new ArrayList<String>();
                                ancestors.add(workPage.getPageName());
                            } else {
                                ancestors = workPage.getAncestors();
                                if (ancestors.size() > 0) {
                                    if (ancestors.get(ancestors.size() - 1).compareTo(workPage.getPageName()) != 0) ancestors.add(workPage.getPageName());
                                }
                            }
                            Page childPage = new Page(child, ancestors);
                            addToWorkDB(childPage);
                            addToPageSeenDB(child);
                        }
                    }
                }
            }
        }
        showLineage(finalPage);
    }

    private void showLineage(Page finalPage) {
        logger.debug("--------F I N I S H E D----------");
        XMLCreator xmlResponse = new XMLCreator();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Element receiptElement = xmlResponse.createXMLElement("receipt");
        Element requestElement = xmlResponse.createXMLElement("request");
        Element fromElement = xmlResponse.createXMLElement("from");
        fromElement.appendChild(xmlResponse.createXMLTextNode(fromPage));
        requestElement.appendChild(fromElement);
        Element toElement = xmlResponse.createXMLElement("to");
        toElement.appendChild(xmlResponse.createXMLTextNode(toPage));
        requestElement.appendChild(toElement);
        Element strategyElement = xmlResponse.createXMLElement("strategy");
        strategyElement.appendChild(xmlResponse.createXMLTextNode(strategy));
        requestElement.appendChild(strategyElement);
        Element emailElement = xmlResponse.createXMLElement("email_address");
        emailElement.appendChild(xmlResponse.createXMLTextNode(emailAddress));
        requestElement.appendChild(emailElement);
        receiptElement.appendChild(requestElement);
        Element jobElement = xmlResponse.createXMLElement("submitted_job");
        jobElement.setAttribute("id", jobID.toString());
        receiptElement.appendChild(jobElement);
        Element resultsElement = xmlResponse.createXMLElement("results");
        resultsElement.setAttribute("finish_time", df.format(new Date()));
        resultsElement.setAttribute("status", "--------F I N I S H E D----------");
        Element ancestorsElement = xmlResponse.createXMLElement("ancestors");
        List<String> ancestors = finalPage.getAncestors();
        for (String ancestor : ancestors) {
            Element ancestorElement = xmlResponse.createXMLElement("ancestor");
            ancestorElement.appendChild(xmlResponse.createXMLTextNode(ancestor));
            ancestorsElement.appendChild(ancestorElement);
            logger.debug("ancestor->" + ancestor);
        }
        Element ancestorElement = xmlResponse.createXMLElement("ancestor");
        ancestorElement.appendChild(xmlResponse.createXMLTextNode(finalPage.getPageName()));
        ancestorsElement.appendChild(ancestorElement);
        logger.debug("final-> " + finalPage.getPageName());
        resultsElement.appendChild(ancestorsElement);
        receiptElement.appendChild(resultsElement);
        xmlResponse.addNode(receiptElement);
        EmailResults email = new EmailResults();
        email.setEmailAddress(emailAddress);
        email.setSubject("There From Here Job " + jobID + " finished");
        email.setBody(xmlResponse.getDocAsXMLString(true));
        if (email.send() == false) logger.error("Did not send the email to " + emailAddress);
    }

    private Page getFromWorkDB() {
        Page workPage = null;
        try {
            Cursor myCursor = myDatabase.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            if (myCursor.getFirst(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                workPage = (Page) dataBinding.entryToObject(foundData);
                myCursor.delete();
            }
            myCursor.close();
        } catch (DatabaseException dbe) {
            logger.error("Got DatabaseException: " + dbe.toString(), dbe);
        }
        return workPage;
    }

    private void addToWorkDB(Page newPage) {
        try {
            DatabaseEntry theKey = new DatabaseEntry(Long.toString(++rowNum).getBytes("UTF-8"));
            DatabaseEntry theData = new DatabaseEntry();
            dataBinding.objectToEntry(newPage, theData);
            myDatabase.put(null, theKey, theData);
        } catch (DatabaseException dbe) {
            logger.error("Got DatabaseException: " + dbe.toString(), dbe);
        } catch (UnsupportedEncodingException usee) {
            logger.error("Got UnsupportedEncodingException: " + usee.toString(), usee);
        }
    }

    private void addToPageSeenDB(String name) {
        try {
            String aKey = name;
            String aData = name;
            DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes("UTF-8"));
            DatabaseEntry theData = new DatabaseEntry(aData.getBytes("UTF-8"));
            pagesSeenDatabase.put(null, theKey, theData);
        } catch (DatabaseException dbe) {
            logger.error("Got DatabaseException: " + dbe.toString(), dbe);
        } catch (UnsupportedEncodingException usee) {
            logger.error("Got UnsupportedEncodingException: " + usee.toString(), usee);
        }
    }

    private boolean hasPageBeenSeen(String pageName) {
        boolean wasFound = false;
        try {
            DatabaseEntry theKey = new DatabaseEntry(pageName.getBytes("UTF-8"));
            DatabaseEntry theData = new DatabaseEntry();
            if (pagesSeenDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                wasFound = true;
            }
        } catch (DatabaseException dbe) {
            logger.error("Got DatabaseException: " + dbe.toString(), dbe);
        } catch (UnsupportedEncodingException usee) {
            logger.error("Got UnsupportedEncodingException: " + usee.toString(), usee);
        }
        return wasFound;
    }
}

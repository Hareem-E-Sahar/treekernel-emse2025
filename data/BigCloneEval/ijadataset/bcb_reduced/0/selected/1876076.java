package com.pallas.unicore.container;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.unicore.User;
import org.unicore.Vsite;
import org.unicore.ajo.AbstractJob;
import org.unicore.ajo.Portfolio;
import org.unicore.sets.PortfolioEnumeration;
import org.unicore.sets.PortfolioSet;
import org.unicore.upl.ConsignJob;
import org.unicore.upl.ConsignJobReply;
import org.unicore.upl.Reply;
import org.unicore.upl.UnicoreResponse;
import com.pallas.unicore.client.Client;
import com.pallas.unicore.client.util.PluginManager;
import com.pallas.unicore.client.util.PluginObjectInputStream;
import com.pallas.unicore.connection.Connection;
import com.pallas.unicore.container.errorspec.ErrorSet;
import com.pallas.unicore.container.errorspec.UError;
import com.pallas.unicore.extensions.Usite;
import com.pallas.unicore.resourcemanager.ResourceManager;
import com.pallas.unicore.resourcemanager.ResourceTray;
import com.pallas.unicore.security.JobConverter;
import com.pallas.unicore.threadpool.IObserver;
import com.pallas.unicore.utility.PortfolioExaminer;
import com.pallas.unicore.utility.UserMessages;
import com.pallas.unicore.utility.Version;

/**
 * Container class for a job group. Each JobContainer may hold TaskContainer or
 * sub-group JobContainers. The JobContainer holds information about the Vsite
 * for the (sub)Job, the dependencies, the site specific security objects and
 * the combined errors of all tasks.
 * 
 * @author Thomas Kentemich
 * @author Ralf Ratering
 * @author Kirsten Foerster
 * @version $Id: JobContainer.java,v 1.1 2004/05/25 14:58:50 rmenday Exp $
 */
public class JobContainer extends GroupContainer implements Serializable, Cloneable, IObserver, Runnable, Comparable {

    private static String[][] persistentXMLFields = { { "USITE", "usite" }, { "VSITE", "vsite" }, { "USER", "user" }, { "JOBFILENAME", "filename" }, { "RESOURCES", "resourceTray" } };

    private static ResourceBundle res = ResourceBundle.getBundle("com.pallas.unicore.container.ResourceStrings");

    static final long serialVersionUID = 5667208652861020186L;

    /**
	 * Decode the job group from an byte[].
	 */
    public static JobContainer decode(byte[] raw) {
        JobContainer jc = null;
        PluginManager pManager = Client.getPluginManager();
        if (raw != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(raw);
                ZipInputStream zipIn = new ZipInputStream(bis);
                zipIn.getNextEntry();
                PluginObjectInputStream ois = new PluginObjectInputStream(zipIn, pManager);
                jc = (JobContainer) ois.readObject();
                ois.close();
            } catch (ClassNotFoundException e) {
                logger.log(Level.WARNING, "Plugin not available.", e);
            } catch (InvalidClassException e) {
                logger.log(Level.WARNING, "Job contains incompatible class.", e);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not decode job.", e);
            }
        }
        return jc;
    }

    /**
	 * The main program for the JobContainer class
	 * 
	 * @param args
	 *            The command line arguments
	 */
    public static void main(String[] args) {
        JobContainer cont = new JobContainer();
        cont.setName("TestJob");
        JobContainer clone = (JobContainer) cont.clone();
        logger.info("CONT: " + cont);
        logger.info("CLONE: " + clone);
    }

    private String filename;

    private transient ConsignJobReply jobReply;

    private transient String lastUpdateUPLDirectory = null;

    private transient int otherSubmissionErrors;

    private transient PortfolioSet pfSet = new PortfolioSet();

    private ResourceTray resourceTray = new ResourceTray("Resources");

    private transient Date savedTime;

    private transient byte[] sso;

    private Date submissionTime;

    private transient int taskId;

    private User user;

    private Usite usite = null;

    private Vsite vsite;

    /**
	 * Abstract methods
	 */
    public void buildActionGroup() {
        AbstractJob abstractJob = (AbstractJob) actionGroup;
        abstractJob.setVsite(getVsite());
        abstractJob.setEndorser(getUser());
        pfSet = new PortfolioSet();
        buildActionGroup(abstractJob);
        actionGroup.setPropagateSuccessfulIfNotSuccessful(getIgnoreFailure());
        Vector tasks = getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            ActionContainer ac = (ActionContainer) tasks.elementAt(i);
            if (ac.getActionGroup() instanceof AbstractJob) {
                AbstractJob job = (AbstractJob) ac.getActionGroup();
                PortfolioSet subSet = job.getStreamed();
                if (subSet != null) {
                    PortfolioEnumeration subSetEnum = job.getStreamed().elements();
                    while (subSetEnum.hasMoreElements()) {
                        Portfolio next = subSetEnum.nextElement();
                        pfSet.add(next);
                    }
                }
            }
        }
        PortfolioSet localPfset = collectLocalNspacePortfolios();
        if (localPfset != null) {
            PortfolioEnumeration subSetEnum = localPfset.elements();
            while (subSetEnum.hasMoreElements()) {
                Portfolio next = subSetEnum.nextElement();
                pfSet.add(next);
            }
        }
        abstractJob.setStreamed(pfSet);
    }

    /**
	 * Self check the correctness of the values relevant for this container
	 */
    public ErrorSet checkContents() {
        ErrorSet err = super.checkContents();
        if (usite == null) {
            err.add(new UError(id, "No Usite selected."));
        } else {
            boolean foundUsite = false;
            Vector knownUsites = ResourceManager.getUsites();
            for (int i = 0; i < knownUsites.size(); i++) {
                if (knownUsites.elementAt(i).equals(usite)) {
                    foundUsite = true;
                    usite = (Usite) knownUsites.elementAt(i);
                    break;
                }
            }
            if (!foundUsite) {
                usite = null;
                err.add(new UError(id, res.getString("NO_USITE")));
            } else {
                boolean foundVsite = false;
                Vector knownVsites = ResourceManager.getVsites(usite);
                for (int i = 0; i < knownVsites.size(); i++) {
                    Vsite knownVsite = (Vsite) knownVsites.elementAt(i);
                    if ((vsite != null) && vsite.equals(knownVsite)) {
                        foundVsite = true;
                        vsite = knownVsite;
                        break;
                    }
                }
                if (!foundVsite) {
                    vsite = null;
                } else if (!ResourceManager.isVsiteAvailable(vsite)) {
                    err.add(new UError(id, ResourceManager.getVsiteErrorMessage(vsite)));
                }
                if (vsite == null || vsite.getAddress() == null || vsite.getName() == null) {
                    err.add(new UError(id, res.getString("NO_VSITE_ENTRY")));
                }
            }
        }
        if (getTasks().isEmpty()) {
            err.add(new UError(id, res.getString("EMPTY_GROUP")));
        }
        if (err.containsError()) {
            updateIcon(Color.red);
        } else {
            updateIcon(Color.green);
        }
        setErrors(err);
        selectJPAIcon();
        return err;
    }

    /**
	 * Clonable Interface. Copy all tasks and dependencies and outcomes and
	 * return cloned JobContainer
	 * 
	 * @return JobContainer clone
	 */
    public Object clone() {
        JobContainer clone = (JobContainer) super.clone();
        clone.setFilename(null);
        AbstractJob abstractJob = new AbstractJob(getName(), null, null, getVsite(), this.getUser());
        clone.setIdentifier(abstractJob.getAJOId());
        clone.actionGroup = abstractJob;
        return clone;
    }

    /**
	 * Compares two submissionDates for ordering. If a submission date is null,
	 * use current date.
	 * 
	 * @param object
	 *            object to compare to
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
    public int compareTo(Object object) {
        Date date1 = submissionTime;
        Date date2 = ((JobContainer) object).getSubmissionTime();
        if (date1 == null) {
            date1 = new Date();
        }
        if (date2 == null) {
            date2 = new Date();
        }
        return date1.compareTo(date2);
    }

    /**
	 * Encode the job to a byte[].
	 * 
	 * @return serialized job
	 */
    public byte[] encode() {
        byte[] result = null;
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(bao);
            zipOut.putNextEntry(new ZipEntry("root-Job"));
            zipOut.setLevel(9);
            ObjectOutputStream oos = new ObjectOutputStream(zipOut);
            oos.writeObject(this);
            oos.flush();
            bao.flush();
            oos.close();
            result = bao.toByteArray();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Encoding job " + this + " failed.", ex);
        }
        return result;
    }

    public void generateNewAbstractJob() {
        logger.info("Building new AJO for: " + getName());
        AbstractJob abstractJob = new AbstractJob(getName());
        setIdentifier(abstractJob.getAJOId());
        actionGroup = abstractJob;
        Vector tasks = getAllTasks();
        for (int i = 0; i < tasks.size(); i++) {
            ActionContainer task = (ActionContainer) tasks.elementAt(i);
            if (task instanceof JobContainer) {
                ((JobContainer) task).generateNewAbstractJob();
            }
        }
    }

    /**
	 * Get the filename associated with this container
	 */
    public final String getFilename() {
        return filename;
    }

    /**
	 * Gets the LastUpdateUPLDirectory of the JobContainer object
	 */
    public String getLastUpdateUPLDirectory() {
        return this.lastUpdateUPLDirectory;
    }

    public Vector getNspaceExportTasks() {
        Vector exportTasks = new Vector();
        Vector allTasks = getAllTasks();
        for (int i = 0; i < allTasks.size(); i++) {
            ActionContainer task = (ActionContainer) allTasks.elementAt(i);
            if (task instanceof TaskContainer) {
                TaskContainer exportContainer = (TaskContainer) task;
                if (exportContainer.hasNspaceExports()) {
                    exportTasks.add(exportContainer);
                }
            }
        }
        return exportTasks;
    }

    public Vector getNspaceImportTasks() {
        Vector importTasks = new Vector();
        Vector allTasks = getAllTasks();
        for (int i = 0; i < allTasks.size(); i++) {
            ActionContainer task = (ActionContainer) allTasks.elementAt(i);
            if (task instanceof JobContainer) {
                Vector jobImports = ((JobContainer) task).getNspaceImportTasks();
                for (int j = 0; j < jobImports.size(); j++) {
                    TaskContainer importTask = (TaskContainer) jobImports.elementAt(j);
                    importTasks.add(importTask);
                }
            } else if (task instanceof TaskContainer) {
                TaskContainer importContainer = (TaskContainer) task;
                if (importContainer.hasNspaceImports()) {
                    importTasks.add(importContainer);
                }
            }
        }
        return importTasks;
    }

    /**
	 * This routine returns errors other than provided by the NJS, for example
	 * when streaming file. Return 0 if successfull;
	 * 
	 * @return The error code
	 */
    public int getOtherSubmissionErrors() {
        return otherSubmissionErrors;
    }

    /**
	 * Gets the ResourceTray
	 */
    public ResourceTray getResourceTray() {
        if (resourceTray == null) {
            resourceTray = new ResourceTray("Resources");
        }
        return this.resourceTray;
    }

    /**
	 * Get a time stamp when this container was saved for the last time.
	 * 
	 * @return System date of last save time
	 */
    public final Date getSavedTime() {
        return savedTime;
    }

    /**
	 * Get the site specific security object
	 * 
	 * @return The SSO value
	 */
    public final byte[] getSSO() {
        return sso;
    }

    /**
	 * Query the state of the container. This gets updated by the threads
	 * actually submitting the job
	 * 
	 * @return The State value
	 */
    public final int getState() {
        return containerState;
    }

    public final ConsignJobReply getSubmissionResult() {
        return jobReply;
    }

    /**
	 * Get a time stamp when this container was saved for the last time.
	 * 
	 * @return System date of last save time
	 */
    public Date getSubmissionTime() {
        return submissionTime;
    }

    /**
	 * Gets the TaskId
	 */
    public int getTaskId() {
        return taskId;
    }

    /**
	 * Return the User.
	 */
    public User getUser() {
        return user;
    }

    /**
	 * Return the Usite we are running on.
	 */
    public final Usite getUsite() {
        return usite;
    }

    /**
	 * Return the Vsite, this job group is running on.
	 */
    public final Vsite getVsite() {
        return vsite;
    }

    public void observableUpdate(Object theObserved, Object changeCode) {
        this.setState(ActionContainer.STATE_SUBMITTED);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        String currentDotVersion = Version.dotVersion();
        String containerDotVersion = getContainerVersion().substring(0, currentDotVersion.length());
        if (getContainerVersion().startsWith("4") || getContainerVersion().startsWith("5") || getContainerVersion().startsWith("1")) {
            logger.info("Reading JobContainer " + id + ", version: " + getContainerVersion() + " using default deserialization.");
            stream.defaultReadObject();
            updateContainerVersion();
            return;
        }
        if (containerDotVersion.compareTo("3.6") < 0) {
            logger.info("Converting JobContainer " + id + ", version: " + getContainerVersion() + " to version 3.6");
            updateName(this);
            if (resourceTray == null) {
                resourceTray = new ResourceTray("Resources");
            }
        }
        if (containerDotVersion.compareTo("4") < 0) {
            logger.info("Converting JobContainer " + id + ", version: " + getContainerVersion() + " to version 4.0");
            try {
                ObjectInputStream.GetField getField = stream.readFields();
                ObjectStreamField streamFields[] = getField.getObjectStreamClass().getFields();
                Field[] fields = getClass().getDeclaredFields();
                for (int i = 0; i < streamFields.length; i++) {
                    String name = streamFields[i].getName();
                    if (name.equals("tasks")) {
                        setTasks((Vector) getField.get(name, new Vector()));
                    } else if (name.equals("deps")) {
                        deps = (DependencyContainer[]) getField.get(name, null);
                    } else {
                        for (int j = 0; j < fields.length; j++) {
                            if (name.equals(fields[j].getName())) {
                                try {
                                    fields[j].set(this, getField.get(name, null));
                                } catch (Exception e) {
                                    logger.log(Level.WARNING, "Reading job " + this + " failed.", e);
                                }
                            }
                        }
                    }
                }
            } catch (InvalidClassException e) {
                logger.log(Level.SEVERE, "Could not read fields from serialized class", e);
            }
        }
        updateContainerVersion();
    }

    public AbstractJob prepareJob() {
        generateNewAbstractJob();
        buildActionGroup();
        setSubmissionTime(new Date());
        byte[] extraInfo = encode();
        actionGroup.setExtraInformation(extraInfo);
        AbstractJob abstractJob = (AbstractJob) actionGroup;
        logger.info("Submit job to: " + vsite.getName() + " user: " + user.getCertificate().getSubjectDN() + " xlogin: [" + user.getXlogin() + "] project: [" + user.getProject() + "]");
        logger.info("AJO DUMP:" + dumpActionGroup(abstractJob));
        if (System.getProperty("com.pallas.unicore.saveajo") != null) {
            ResourceManager.writeObjectToFile(abstractJob, "last.ajo");
        }
        PortfolioExaminer.test(abstractJob);
        return abstractJob;
    }

    public Vector prepareFilesToStream(ConsignJob cj) {
        Vector importFromNspaceTasks = getNspaceImportTasks();
        if (importFromNspaceTasks.size() > 0) {
            cj.setStreamed(true);
        }
        return importFromNspaceTasks;
    }

    /**
	 * submit job
	 */
    public void run() {
        try {
            setState(STATE_SUBMITTING);
            ConsignJob cj = JobConverter.convert(prepareJob(), true, true);
            Vector importFromNspaceTasks = prepareFilesToStream(cj);
            otherSubmissionErrors = 0;
            Connection connection = ResourceManager.getConnectionManager().lookupConnection(vsite, ResourceManager.getUser(vsite));
            Reply reply = connection.sendMessage(cj, importFromNspaceTasks);
            int returnCode = reply.getLastEntry().getReturnCode();
            if (reply instanceof ConsignJobReply && (returnCode == 0)) {
                jobReply = (ConsignJobReply) reply;
                logger.info("Reply " + jobReply + " result " + returnCode);
            } else {
                UnicoreResponse resp = reply.getLastEntry();
                UserMessages.error(res.getString("SUBMIT_FAILED"), resp.getComment());
            }
        } catch (Exception e) {
            UserMessages.warning("Submission of job " + getName() + " failed.", e.getMessage());
            logger.log(Level.SEVERE, "Submission of job " + getName() + " failed.", e);
            otherSubmissionErrors = 1;
            setState(STATE_UNKNOWN);
        }
        setState(STATE_SUBMITTED);
    }

    /**
	 * Method selects icon from ResourceManager
	 */
    public void selectJPAIcon() {
        if (containerState == STATE_SUBMITTING) {
            getStatusIcon().setImage(ResourceManager.getImage(ResourceManager.WAIT));
            return;
        }
        selectIcon();
    }

    /**
	 * Set a filename associated with a task or ajob
	 */
    public final void setFilename(String s) {
        this.filename = s;
    }

    /**
	 * Sets the LastUpdateUPLDirectory
	 */
    public void setLastUpdateUPLDirectory(String lastUpdateUPLDirectory) {
        this.lastUpdateUPLDirectory = lastUpdateUPLDirectory;
    }

    public void setResourceTray(ResourceTray resourceTray) {
        this.resourceTray = resourceTray;
    }

    /**
	 * Set a time stamp when this container was saved.
	 */
    public final void setSavedTime(Date savedTime) {
        this.savedTime = savedTime;
        setModifiedTime(savedTime);
    }

    /**
	 * Set the site specific security object
	 * 
	 * @param sso
	 *            The new SSO value
	 */
    public final void setSSO(byte[] sso) {
        this.sso = sso;
    }

    /**
	 * Set the internal state of the container. This is used to control job
	 * submission
	 */
    public final void setState(int state) {
        containerState = state;
        if (containerState == STATE_UPDATING || containerState == STATE_SUBMITTING) {
            getStatusIcon().setImage(ResourceManager.getImage(ResourceManager.WAIT));
        } else {
            getStatusIcon().setImage(ResourceManager.getImage(ResourceManager.JOBGROUP));
            setStatus(getStatus());
        }
    }

    /**
	 * Set a time stamp when this container was submitted.
	 */
    public void setSubmissionTime(Date submissionTime) {
        this.submissionTime = submissionTime;
    }

    /**
	 * Sets the TaskId attribute of the JobContainer object
	 */
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    /**
	 * Set the User.
	 */
    public void setUser(User user) {
        this.user = user;
    }

    /**
	 * Set the Usite.
	 */
    public final void setUsite(Usite us) {
        usite = us;
    }

    /**
	 * Set the Vsite for this job group. And update resources for its tasks.
	 */
    public final void setVsite(Vsite vs) {
        this.vsite = vs;
    }

    /**
	 * Get String representation for this object
	 */
    public String toString() {
        if (getSubmissionTime() == null) {
            return super.toString();
        } else {
            return (super.toString() + " [" + ResourceManager.getCompleteFormatter().format(getSubmissionTime()) + "]");
        }
    }
}

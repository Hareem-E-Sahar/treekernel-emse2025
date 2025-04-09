package org.grobid.service.util;

import org.grobid.service.exceptions.GROBIDServiceException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.service.GROBIDJobExtended;
import grobid.service.exchange.NotificationListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This object works as communicator to the persistence layer for Job objects. It reads, stores and deletes
 * Job objects.
 * @author FloZi
 *
 */
public class GROBIDJobManager implements NotificationListener {

    private static volatile GROBIDJobManager grobidJobMgr = null;

    /**
	 * Returns an instance of the GROBIDJobManager object. If non already exists, one will be created.
	 * @return
	 */
    public static synchronized GROBIDJobManager createGROBIDJobMgr() {
        String jobFolder = System.getProperty(GrobidProperties.PROP_SERVICE_JOBFOLDER_PATH);
        if ((jobFolder == null) || (jobFolder.equals(""))) {
            throw new GROBIDServiceException("Cannot create a job manager, because the job folder is not set." + " Please set system property '" + GrobidProperties.PROP_SERVICE_JOBFOLDER_PATH + "' first. A job folder is necessary, because this is the physical place, where the job objects " + "can be stored.");
        }
        GROBIDJobManager.setJobsFolder(new File(jobFolder));
        if (grobidJobMgr == null) {
            grobidJobMgr = new GROBIDJobManager();
        }
        return (grobidJobMgr);
    }

    private static Log logger = LogFactory.getLog(GROBIDJobManager.class);

    /**
	 * The ending of a xml file contained a representation of job object.
	 */
    public static final String JOB_FILE_ENDING = ".grobidJob";

    private GROBIDJobManager() {
        this.init();
    }

    private volatile GROBIDJobPool jobPool = null;

    public GROBIDJobPool getGROBIDJobPool() {
        if (jobPool == null) {
            synchronized (this) {
                if (jobPool == null) jobPool = new GROBIDJobPool();
            }
        }
        return (jobPool);
    }

    /**
	 * Folder to store the grobid jobs
	 */
    private static volatile File jobsFolder = null;

    /**
	 * Returns the folder to where the jobs can be stored.
	 * @return
	 */
    public static File getJobsFolder() {
        return jobsFolder;
    }

    /**
	 * Sets the folder to where the jobs can be stored.
	 * @param jobsFolder
	 */
    public static synchronized void setJobsFolder(File jobsFolder) {
        GROBIDJobManager.jobsFolder = jobsFolder;
    }

    /**
	 * Initializes this object:
	 * <ul>
	 * <li>checks if environment variable is set.<li/>
	 * </ul>
	 */
    private void init() {
        this.getJobs();
    }

    /**
	 * Creates a new job and adds it to the internal list of jobs.
	 * This job will be not stored, To persist this object call saveJob(Job);
	 * @return Returns the created job.
	 */
    public synchronized GROBIDJobExtended createJob() {
        logger.debug(">> " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        GROBIDJobExtended job = new GROBIDJobExtended();
        String jobId = null;
        while ((jobId == null) || (this.hasJob(jobId))) {
            jobId = KeyGen.getKey();
        }
        File jobFolder = new File(jobsFolder.getAbsolutePath() + "/" + jobId);
        if (!jobFolder.mkdirs()) throw new GROBIDServiceException("Cannot create folder for GROBID jobs.");
        logger.debug(jobFolder);
        job.setJobId(jobId);
        job.setJobPath(jobFolder);
        String jobPW = KeyGen.getKey();
        job.setJobPW(jobPW);
        this.getGROBIDJobPool().add(job);
        job.getNotificationListener().add(this);
        logger.debug("<< " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "()_" + job.getJobId());
        return (job);
    }

    /**
	 * Returns if a job with the given jobId already exists.
	 * @param jobId identifier of job to be searched for
	 * @return true, if no job exists.
	 */
    public Boolean hasJob(String jobId) {
        Boolean retVal = false;
        if (jobId == null) return (false);
        if (this.getGROBIDJobPool().get(jobId) != null) {
            retVal = true;
        } else {
            synchronized (this) {
                for (String fileStr : jobsFolder.list()) {
                    if (jobId.equals(fileStr)) {
                        File file = new File(jobsFolder + "/" + fileStr);
                        if (file.isDirectory()) {
                            return (true);
                        }
                    }
                }
            }
        }
        return (retVal);
    }

    /**
	 * Returns a job corresponding to the given jobId, if one exists and the password
	 * is correct.
	 * @param jobId identifier of job to be searched for
	 * @param jobPW password identifier of job to be searched for
	 * @return job object corresponding to jobID and jobPW, if non exists null would be returned
	 */
    public GROBIDJobExtended getJob(String jobId, String jobPW) {
        GROBIDJobExtended retVal = null;
        GROBIDJobExtended job = null;
        {
            job = this.getGROBIDJobPool().get(jobId);
        }
        if (job == null) {
            job = this.loadJob(jobId);
            this.getGROBIDJobPool().add(job);
        }
        if (job != null) {
            if (job.getJobPW() != null) {
                if (job.getJobPW().equalsIgnoreCase(jobPW)) retVal = job;
            }
        }
        return (retVal);
    }

    /**
	 * Returns a list of all currently listed jobs. 
	 * @return a list of all currently listed jobs.
	 */
    public synchronized List<GROBIDJobExtended> getJobs() {
        Vector<GROBIDJobExtended> retVal = null;
        if (jobsFolder == null) throw new GROBIDServiceException("Cannot load jobs, because the jobs folder is not set.");
        if (jobsFolder.list().length > 0) {
            for (String fileStr : jobsFolder.list()) {
                File jobFolder = new File(jobsFolder.getAbsoluteFile() + "/" + fileStr);
                if (jobFolder.isDirectory()) {
                    GROBIDJobExtended job = this.loadJob(fileStr);
                    if (job != null) {
                        if (retVal == null) retVal = new Vector<GROBIDJobExtended>();
                        retVal.add(job);
                    }
                }
            }
        }
        return (retVal);
    }

    /**
	 * Stores the given job.
	 * @param job to store
	 * @return true, if job was successfully stored
	 */
    public synchronized Boolean saveJob(GROBIDJobExtended job) {
        logger.debug(">> " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + job.getJobId() + ")");
        Boolean retVal = false;
        if (job != null) {
            for (String fileStr : jobsFolder.list()) {
                File jobFolder = new File(jobsFolder.getAbsolutePath() + "/" + fileStr);
                if ((fileStr.equalsIgnoreCase(job.getJobId())) && (jobFolder.isDirectory())) {
                    FileOutputStream out = null;
                    try {
                        File jobFile = new File(jobFolder.getAbsolutePath() + "/" + job.getJobId() + JOB_FILE_ENDING);
                        JAXBContext jc = JAXBContext.newInstance(GROBIDJobExtended.class);
                        Marshaller marshaller = jc.createMarshaller();
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(true));
                        out = new FileOutputStream(jobFile);
                        marshaller.marshal(job, out);
                        retVal = true;
                        out.flush();
                        logger.debug("job stored: " + job);
                    } catch (JAXBException e) {
                        throw new GROBIDServiceException("An unexpected exception ocurs while storing job '" + job.getJobId() + "'.", e);
                    } catch (FileNotFoundException e) {
                        throw new GROBIDServiceException("An unexpected exception ocurs while storing job '" + job.getJobId() + "'.", e);
                    } catch (IOException e) {
                        throw new GROBIDServiceException("An unexpected exception ocurs while storing job '" + job.getJobId() + "'.", e);
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                logger.error(e);
                            }
                        }
                    }
                    break;
                }
            }
        }
        logger.debug("<< " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + job.getJobId() + ")");
        return (retVal);
    }

    /**
	 * Stores the given job and removes it from the internal pool.
	 * @param job to store
	 * @return true, if job was successfully stored
	 */
    public synchronized Boolean flushJob(GROBIDJobExtended job) {
        logger.debug(">> " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + job.getJobId() + ")");
        Boolean retVal = false;
        this.removeJob(job);
        logger.debug("<< " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + job.getJobId() + ")");
        return (retVal);
    }

    /**
	 * Removes the given job from persistence layer and from pool.
	 * @param job job to remove 
	 * @return true, if job was successfully removed
	 */
    public synchronized Boolean removeJob(GROBIDJobExtended job) {
        logger.debug(">> " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + job.getJobId() + ")");
        Boolean retVal = false;
        if (job != null) {
            {
                retVal = this.getGROBIDJobPool().delete(job.getJobId());
            }
            if ((job.getJobId() != null) && (job.getJobPW() != null) && (job.getJobPath() != null)) {
                boolean filesRemoved = this.removeFile(job.getJobPath());
                retVal = retVal && filesRemoved;
                if (retVal) {
                    logger.debug("job removed: " + job);
                } else {
                    logger.error("job cannot be removed, because of an unknown cause: " + job);
                }
            }
        }
        logger.debug("<< " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + job.getJobId() + ")");
        return (retVal);
    }

    /**
	 * Removes the given file. If the given file is a directory, all
	 * its contents will be removed recursively. 
	 * @return
	 */
    private synchronized Boolean removeFile(File file) {
        Boolean retVal = false;
        if ((file != null) && (file.exists())) {
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    removeFile(subFile);
                }
            }
            retVal = file.delete();
        }
        return (retVal);
    }

    /**
	 * Removes all artifacts of processings. If another processing is still running, it will be interrupted 
	 * and removed.
	 * @return true, if clean up was successful.
	 */
    public synchronized Boolean cleanUp() {
        Boolean retVal = true;
        if (getJobsFolder() != null) {
            Boolean removed = null;
            for (String folderName : getJobsFolder().list()) {
                File folder = new File(jobsFolder.getAbsolutePath() + "/" + folderName);
                removed = this.removeFile(folder);
                retVal = removed && retVal;
            }
        }
        return (retVal);
    }

    /**
	 * Loads a job identified by the given jobId.
	 * @param jobId of the job to be load
	 * @return GROBIDJob object representing the job corresponding to the given jobId
	 */
    private synchronized GROBIDJobExtended loadJob(String jobId) {
        logger.debug(">> " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + jobId + ")");
        GROBIDJobExtended retVal = null;
        if ((jobId != null) && (!jobId.equals(""))) {
            for (String fileStr : getJobsFolder().list()) {
                File jobFolder = new File(jobsFolder.getAbsolutePath() + "/" + fileStr);
                if ((fileStr.equalsIgnoreCase(jobId)) && (jobFolder.isDirectory())) {
                    File jobFile = new File(jobFolder.getAbsolutePath() + "/" + jobId + JOB_FILE_ENDING);
                    if (jobFile.exists()) {
                        JAXBContext jc;
                        try {
                            jc = JAXBContext.newInstance(GROBIDJobExtended.class);
                            Unmarshaller unmarshaller = jc.createUnmarshaller();
                            retVal = (GROBIDJobExtended) unmarshaller.unmarshal(jobFile);
                            logger.debug("job loaded: " + retVal);
                        } catch (JAXBException e) {
                            throw new GROBIDServiceException("Cannot unmarshall GROBIDJob for file '" + jobFile.getAbsolutePath() + "'.", e);
                        }
                    }
                }
            }
        }
        logger.debug("<< " + this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "(" + jobId + ")");
        return (retVal);
    }

    /**
	 * Stores all jobs in job pool.
	 */
    @Override
    protected void finalize() throws Throwable {
        for (GROBIDJobExtended job : Collections.synchronizedCollection(this.getGROBIDJobPool().getAll())) {
            this.flushJob(job);
        }
    }

    /**
	 * Receives notification when changing the GROBIDJob objects. When a change of its values via SET is 
	 * done, the JobManager stores the job again, in case of it is not already in the pool. When the Job is
	 * in the pool nothing will happen
	 */
    public void notify(Object notifier, NOTIFICATION_TYPE type) {
        if (notifier != null) {
            if (NOTIFICATION_TYPE.SET.equals(type)) {
                if (notifier instanceof GROBIDJobExtended) {
                    GROBIDJobExtended job = (GROBIDJobExtended) notifier;
                    if (this.getGROBIDJobPool().get(job.getJobId()) == null) {
                        this.saveJob(job);
                    } else {
                        this.saveJob(job);
                    }
                }
            }
        }
    }
}

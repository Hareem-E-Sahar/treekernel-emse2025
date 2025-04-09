package au.gov.naa.digipres.dpr.task.step;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import au.gov.naa.digipres.dpr.core.Constants;
import au.gov.naa.digipres.dpr.core.manifest.ManifestEntry;
import au.gov.naa.digipres.dpr.core.manifest.ManifestEntry.BadManifestEntryException;
import au.gov.naa.digipres.dpr.dao.DataAccessManager;
import au.gov.naa.digipres.dpr.dao.TransferJobDAO;
import au.gov.naa.digipres.dpr.model.job.JobStatus;
import au.gov.naa.digipres.dpr.model.transferjob.DataObject;
import au.gov.naa.digipres.dpr.model.transferjob.QFTransferJobProcessingRecord;
import au.gov.naa.digipres.dpr.model.transferjob.TransferJob;
import au.gov.naa.digipres.dpr.model.user.User;
import au.gov.naa.digipres.dpr.task.JobProcessingTask;
import au.gov.naa.digipres.dpr.task.step.ProcessingErrorHandler.ProcessingErrorAction;
import au.gov.naa.digipres.dpr.util.FileUtils;
import au.gov.naa.digipres.dpr.util.carrier.CarrierNotFoundException;

/**
 * <p>This step will process the manifest file for a transfer job. At the completion of this step,
 * the data objects will be added to the transfer job, and each data object will have their
 * file name, carrier id, item number, and checksum data recorded. A new QF Data Object Processing record
 * will be created for each data object. The input carrier will be prepared for the transfer job,
 * by having the required folders created. The manifest file will be copied to the input carrier,
 * in the check folder.</p>
 * 
 * <p>It is possible that a failure may occur during I/O to the carrier device, to prevent this, before 
 * that part of the step is undertaken, the step will have it's status set to in progress, and this
 * will be committed by the persistence layer. On completion of the IO, the parsing of the manifest file
 * will take place, with regular flushes as the objects are saved.</p>
 * 
 * <p>In the case of an error during parsing of the manifest, the step will abort all processing
 * and throw a processing exception. At the completion of processing the step will be in one of two
 * possible states; either processing was successful and the step is passed, or the processing failed,
 * and the step is aborted and returned to the un-started state.</p>
 * 
 * <p>This step must also handle the case of a transfer job being restarted in quarantine, either due to
 * a failure on the QF or major problem in PF. In this case, we can skip parsing the manifest file, but
 * carrier preparation must still take place, and arguably, the manifest verified to match the data objects
 * to ensure the correct manifest is copied..</p>
 * 
 * @author andy
 *
 */
public class ReadManifestStep extends Step {

    public static final String STEP_NAME = "Read Manifest Step";

    public static final String MANIFEST_ENTRY_ERROR_NUMBER = "Entry Number";

    public static final String MANIFEST_ENTRY_ERROR_MESSAGE = "Error Message";

    private String manifestFileName = "";

    private String carrierLocation;

    private String carrierId;

    private TransferJob transferJob;

    private QFTransferJobProcessingRecord qfRecord;

    private String carrierLocationProperty;

    private String carrierIdProperty;

    private String manifestFileNameProperty;

    private DataAccessManager dataAccessManager;

    TransferJobDAO transferJobDAO;

    /**
	 * Constructor
	 * @param currentUser
	 * @param task
	 * @throws IllegalStepStateException 
	 * @throws ProcessingException 
	 */
    public ReadManifestStep(User currentUser, JobProcessingTask task, QFTransferJobProcessingRecord qfRecord) {
        super(currentUser, task);
        transferJob = task.getJobEncapsulator().getTransferJob();
        dataAccessManager = task.getDPRClient().getDataAccessManager();
        transferJobDAO = dataAccessManager.getTransferJobDAO(task);
        this.qfRecord = qfRecord;
        if (qfRecord.getCarrierDeviceId() != null && qfRecord.getCarrierDeviceLocation() != null) {
            File checkFile = new File(qfRecord.getCarrierDeviceLocation());
            if (!checkFile.exists() || !checkFile.isDirectory()) {
                properties.put(StepProperties.OUTPUT_CARRIER_ID_PROPERTY_NAME, qfRecord.getCarrierDeviceId());
                properties.put(StepProperties.OUTPUT_CARRIER_LOCATION_PROPERTY_NAME, qfRecord.getCarrierDeviceLocation());
            }
        }
    }

    @Override
    protected void abort() {
        logger.fine("Starting abort");
        dataAccessManager.rollbackTransaction();
        logger.finer("Removing folders from carrier");
        if (qfRecord.getCarrierDeviceLocation() != null) {
            String newJobDirName = transferJob.getTransferJobNumber().getPath();
            File newJobDir = new File(qfRecord.getCarrierDeviceLocation(), newJobDirName);
            FileUtils.deleteDirAndContents(newJobDir);
        }
        logger.finer("Resetting transfer job");
        dataAccessManager.beginTransaction();
        transferJobDAO.deleteDataObjects(transferJob);
        qfRecord.setManifestReadStatus(Constants.UNSTARTED_STATE);
        transferJob.setNumDataObjects(0);
        transferJobDAO.saveTransferJob(transferJob);
        dataAccessManager.commitTransaction();
        logger.fine("Abort complete");
    }

    @Override
    public void failStep() {
        dataAccessManager.commitTransaction();
        dataAccessManager.beginTransaction();
        qfRecord.setManifestReadStatus(Constants.FAILED_STATE);
        transferJob.setJobStatus(JobStatus.QF_INITIALISED_FROM_MANIFEST_FAILED);
        stopProcessing();
        transferJobDAO.saveTransferJob(transferJob);
        dataAccessManager.commitTransaction();
    }

    @Override
    public StepResults doProcessing(ProcessingErrorHandler processingErrorHandler) throws StepException {
        fireStepProcessingBeginningEvent(getDescription());
        logger.fine("Starting processing in " + this.getClass().getName());
        verifyStepState();
        if (Constants.PASSED_STATE.equals(qfRecord.getManifestReadStatus())) {
            throw new ProcessingException("Can not process step that has already passed successfully!");
        }
        if (Constants.FAILED_STATE.equals(qfRecord.getManifestReadStatus())) {
            throw new ProcessingException("Can not process step that has already passed successfully!");
        }
        if (manifestFileNameProperty != null && manifestFileNameProperty.length() != 0) {
            manifestFileName = manifestFileNameProperty;
        } else {
            manifestFileName = (String) properties.get(StepProperties.MANIFEST_FILE_NAME_PROPERTY_NAME);
        }
        if (manifestFileName == null || manifestFileName.length() == 0) {
            logger.severe("Manifest file name not set.");
            throw new ProcessingException("Manifest file name property has not been set!");
        }
        File manifestFile = new File(manifestFileName);
        if (!manifestFile.exists()) {
            String msg = "The location specified by the manifest file name (" + manifestFileName + ") does not exist.";
            logger.severe(msg);
            throw new ProcessingException(msg);
        }
        if (!manifestFile.isFile()) {
            String msg = "The location specified by the manifest file name (" + manifestFileName + ") is not a file.";
            logger.severe(msg);
            throw new ProcessingException(msg);
        }
        if (!manifestFile.canRead()) {
            String msg = "The file specified by the manifest file name (" + manifestFileName + ") can not be read.";
            logger.severe(msg);
            throw new ProcessingException(msg);
        }
        logger.fine("Manifest file name: " + manifestFileName);
        if (carrierLocationProperty != null && carrierLocationProperty.length() != 0) {
            carrierLocation = carrierLocationProperty;
        } else {
            carrierLocation = properties.getProperty(StepProperties.OUTPUT_CARRIER_LOCATION_PROPERTY_NAME);
        }
        File outputCarrier = new File(carrierLocation);
        if (!outputCarrier.exists() || !outputCarrier.canRead() || !outputCarrier.canWrite() || !outputCarrier.isDirectory()) {
            throw new CarrierNotFoundException();
        }
        if (!carrierLocation.equals(qfRecord.getCarrierDeviceLocation())) {
            dataAccessManager.beginTransaction();
            if (properties.get(StepProperties.OUTPUT_CARRIER_ID_PROPERTY_NAME) != null) {
                qfRecord.setCarrierDeviceId((String) properties.get(StepProperties.OUTPUT_CARRIER_ID_PROPERTY_NAME));
            }
            qfRecord.setCarrierDeviceLocation(carrierLocation);
            transferJobDAO.saveTransferJob(transferJob);
            dataAccessManager.commitTransaction();
        }
        if (carrierIdProperty != null && carrierIdProperty.length() != 0) {
            carrierId = carrierIdProperty;
        } else {
            carrierId = properties.getProperty(StepProperties.OUTPUT_CARRIER_ID_PROPERTY_NAME);
        }
        if (carrierId == null || carrierId.length() == 0) {
            String msg = "Carrier ID has not been set.";
            logger.severe(msg);
            throw new StepPropertiesValidationException(StepProperties.OUTPUT_CARRIER_ID_PROPERTY_NAME, msg);
        }
        if (!carrierId.equals(qfRecord.getCarrierDeviceId())) {
            dataAccessManager.beginTransaction();
            qfRecord.setCarrierDeviceId(carrierId);
            dataAccessManager.commitTransaction();
        }
        StepResults results = new StepResults();
        dataAccessManager.beginTransaction();
        qfRecord.setManifestReadStatus(Constants.IN_PROGRESS_STATE);
        transferJobDAO.saveTransferJob(transferJob);
        dataAccessManager.commitTransaction();
        try {
            String newJobDirName = transferJob.getTransferJobNumber().getPath();
            File newJobDir = new File(carrierLocation, newJobDirName);
            if (!newJobDir.exists() && !newJobDir.mkdirs()) {
                logger.severe("Could not create transfer job base directory");
                throw new ProcessingException("Could not create transfer job base directory");
            }
            File checkDir = new File(newJobDir, Constants.CHECK_DIR_NAME);
            if (!checkDir.exists() && !checkDir.mkdir()) {
                logger.severe("Could not create transfer job check directory");
                throw new ProcessingException("Could not create transfer job check directory");
            }
            File dataDir = new File(newJobDir, Constants.QF_DATA_DIR_NAME);
            if (!dataDir.exists() && !dataDir.mkdir()) {
                logger.severe("Could not create transfer job data directory");
                throw new ProcessingException("Could not create transfer job data directory");
            }
            File manifestOnCarrier = new File(checkDir, Constants.MANIFEST_FILENAME);
            try {
                FileUtils.fileCopy(manifestFile, manifestOnCarrier);
            } catch (IOException e) {
                logger.severe("Could not copy manifest file to carrier.");
                throw new ProcessingException("Could not copy manifest file to carrier", e);
            }
            String newManifestFileChecksum;
            try {
                newManifestFileChecksum = FileUtils.getChecksum(manifestFile, Constants.DEFAULT_CHECKSUM_ALGORITHM);
            } catch (IOException e) {
                logger.severe("Could not get checksum of manifest file on carrier.");
                throw new ProcessingException("Could not get checksum of manifest file on carrier.", e);
            }
            if (transferJob.getNumDataObjects() == 0) {
                List<Map<String, Object>> warnings;
                try {
                    dataAccessManager.beginTransaction();
                    warnings = processManifestFile(manifestFile);
                } catch (IOException e) {
                    logger.severe("Exception processing manifest file - IO error.");
                    throw new ProcessingException("Error processing manifest file.", e);
                }
                if (warnings.size() != 0) {
                    results.getErrorData().addAll(warnings);
                    results.setErrorCount(warnings.size());
                    results.setErrorsStored(warnings.size());
                    results.setErrorOccurred(true);
                }
            } else {
                if (!newManifestFileChecksum.equals(transferJob.getManifestFileChecksum())) {
                    logger.severe("Manifest file is corrupt of has been modified since previous processing attempt.");
                    throw new ProcessingException("Manifest File is corrupt or has been modified since previous processing attempt.");
                }
            }
            if (!results.isErrorOccurred()) {
                qfRecord.setManifestReadStatus(Constants.PASSED_STATE);
                qfRecord.setManifestReadBy(currentUser);
                qfRecord.setManifestReadDate(new Date());
                transferJob.setManifestFileChecksum(newManifestFileChecksum);
                transferJob.setManifestFileChecksumAlgorithm(Constants.DEFAULT_CHECKSUM_ALGORITHM);
                transferJob.setJobStatus(JobStatus.QF_INITIALISED_FROM_MANIFEST_PASSED);
                transferJobDAO.saveTransferJob(transferJob);
                dataAccessManager.commitTransaction();
                logger.fine("Processing Completed successfully");
                results.setResultsMessage(transferJob.getNumDataObjects() + " data objects successfully loaded from manifest.");
            } else {
                results.setResultsMessage(results.getErrorCount() + " errors when loading manifest file.");
                ProcessingErrorAction actionToTake = processingErrorHandler.determineAction(getStepName(), results);
                switch(actionToTake) {
                    case RESET:
                        logger.warning("Errors detected during processing, attempting to abort step.");
                        results.setStepReset(true);
                        abort();
                        break;
                    case STOP_PROCESSING:
                        logger.warning("Errors detected during processing, saving state and then stopping processing.");
                        failStep();
                        break;
                    case SAVE:
                        throw new IllegalStateException("This step cannot be saved in a state of error.");
                }
            }
        } catch (StepException stepEx) {
            abort();
            throw stepEx;
        }
        fireCompletedStepProcessingEvent(results);
        return results;
    }

    @Override
    public String getStepName() {
        return STEP_NAME;
    }

    @Override
    public String getStepStatus() {
        return qfRecord.getManifestReadStatus();
    }

    private static final String COMMENT_STRING = ";";

    private List<Map<String, Object>> processManifestFile(File manifestFile) throws IOException, FileNotFoundException, ProcessingException {
        logger.fine("Starting to parse manifest file");
        InputStream inputStream;
        inputStream = new BufferedInputStream(new FileInputStream(manifestFile));
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        int lineNumber = 0;
        int numDataObjects = 0;
        int warningsCount = 0;
        List<Map<String, Object>> warnings = new ArrayList<Map<String, Object>>();
        int lineCount = getManifestFileLineCount(manifestFile);
        fireItemProcessingBeginEvent(lineCount);
        String line = null;
        ManifestEntry currentManifestEntry;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith(COMMENT_STRING) && lineNumber != 0 && !line.trim().equals("")) {
                try {
                    currentManifestEntry = processLine(line);
                    logger.finest("New data object entry:" + line);
                    DataObject dataObject = new DataObject(currentManifestEntry, transferJob);
                    transferJobDAO.saveDataObject(dataObject);
                    numDataObjects++;
                    fireItemProcessEvent("Added " + dataObject.getFileName());
                } catch (BadManifestEntryException e) {
                    logger.fine("Bad manifest entry: " + e.getMessage());
                    Map<String, Object> errorDetails = new LinkedHashMap<String, Object>();
                    errorDetails.put(MANIFEST_ENTRY_ERROR_NUMBER, new Integer(lineNumber));
                    errorDetails.put(MANIFEST_ENTRY_ERROR_MESSAGE, e.getMessage());
                    warnings.add(errorDetails);
                    if (++warningsCount > Constants.MAX_ERROR_COUNT_IN_MANIFEST) {
                        logger.severe("Exceeded maximum manifest file error count (" + Constants.MAX_ERROR_COUNT_IN_MANIFEST + ").");
                        throw new ProcessingException("Exceeded maximum manifest file error count (" + Constants.MAX_ERROR_COUNT_IN_MANIFEST + ").");
                    }
                    fireItemProcessEvent("Bad manifest entry!");
                }
            }
            if (++lineNumber > 50 && numDataObjects == 0) {
                logger.severe("Read 50 lines and no valid manifest entries were found.");
                throw new ProcessingException("Read 50 lines and no valid manifest entries were found.");
            }
        }
        fireItemProcessingCompleteEvent();
        transferJob.setNumDataObjects(numDataObjects);
        reader.close();
        logger.fine("Finished parsing manifest file, found " + numDataObjects + " data objects.");
        return warnings;
    }

    /**
	 * Return the number of lines in the given manifest file
	 * @param manifestFile
	 * @return the number of lines in the given manifest file
	 * @throws IOException if the file cannot be found or read
	 */
    private static int getManifestFileLineCount(File manifestFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(manifestFile));
        String line = reader.readLine();
        int lineCount = 0;
        while (line != null) {
            lineCount++;
            line = reader.readLine();
        }
        return lineCount;
    }

    /**
	 * Process a single line to create a manifest entry.
	 * @param the line to process.
	 * @return the filled ManifestEntry object, or throw a {@link BadManifestEntryException} if the line does not contain a valid
	 * manifest entry.
	 * @throws BadManifestEntryException if the line does not contain a valid manifest entry.
	 */
    private static ManifestEntry processLine(String line) throws BadManifestEntryException {
        String[] splitLine = line.split("\t");
        if (splitLine.length < 5) {
            throw new BadManifestEntryException("Not enough fields to create a manifest entry in line: \"" + line + "\".");
        }
        String mediaId = splitLine[0];
        String fileName = splitLine[1];
        String checksum = splitLine[2];
        String algorithm = splitLine[3];
        String item = splitLine[4];
        ManifestEntry manifestEntry = new ManifestEntry(mediaId, fileName, item, checksum, algorithm);
        return manifestEntry;
    }

    @Override
    public Set<String> getRequiredPropertyNames() {
        Set<String> requiredPropertyNames = new LinkedHashSet<String>();
        requiredPropertyNames.add(StepProperties.OUTPUT_CARRIER_LOCATION_PROPERTY_NAME);
        requiredPropertyNames.add(StepProperties.OUTPUT_CARRIER_ID_PROPERTY_NAME);
        requiredPropertyNames.add(StepProperties.MANIFEST_FILE_NAME_PROPERTY_NAME);
        return requiredPropertyNames;
    }

    /**
	 * @return the carrierLocationProperty
	 */
    public String getCarrierLocationProperty() {
        return carrierLocationProperty;
    }

    /**
	 * @param carrierLocationProperty the carrierLocationProperty to set
	 */
    public void setCarrierLocationProperty(String carrierLocationProperty) {
        this.carrierLocationProperty = carrierLocationProperty;
    }

    /**
	 * @return the carrierIdProperty
	 */
    public String getCarrierIdProperty() {
        return carrierIdProperty;
    }

    /**
	 * @param carrierIdProperty the carrierIdProperty to set
	 */
    public void setCarrierIdProperty(String carrierIdProperty) {
        this.carrierIdProperty = carrierIdProperty;
    }

    /**
	 * @return the manifestFileNameProperty
	 */
    public String getManifestFileNameProperty() {
        return manifestFileNameProperty;
    }

    /**
	 * @param manifestFileNameProperty the manifestFileNameProperty to set
	 */
    public void setManifestFileNameProperty(String manifestFileNameProperty) {
        this.manifestFileNameProperty = manifestFileNameProperty;
    }

    @Override
    public String getDescription() {
        return "Load data objects from a manifest file";
    }
}

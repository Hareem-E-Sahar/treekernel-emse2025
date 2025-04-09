package au.com.cahaya.hubung.photoelements;

import java.io.File;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import au.com.cahaya.asas.io.FileUtil;
import au.com.cahaya.asas.io.filter.MetaDataFileSetFilter;
import au.com.cahaya.asas.util.cli.ActionSetOption;
import au.com.cahaya.asas.util.cli.DestinationFileOption;
import au.com.cahaya.asas.util.cli.DummyRunOption;
import au.com.cahaya.asas.util.cli.HelpOption;
import au.com.cahaya.asas.util.cli.RecursiveOption;
import au.com.cahaya.asas.util.cli.SourceFileOption;
import au.com.cahaya.hubung.file.model.FileDetailModel;
import au.com.cahaya.hubung.file.util.cli.EnableFileDetailOption;
import au.com.cahaya.hubung.photoelements.model.PhotoElementsImage;
import au.com.cahaya.hubung.photoelements.model.RefactorConfig;

/**
 * The focus of this class is to refactor the pictures directory structure
 * and cleanup duplicates.
 *
 * @author Mathew Pole
 * @since January 2008
 * @version $Revision$
 */
public class RefactorAndCleanup {

    /** The private logger for this class */
    private Logger myLog = LoggerFactory.getLogger(RefactorAndCleanup.class);

    private MetaDataFileSetFilter myMetaDataFilter = new MetaDataFileSetFilter(true);

    private RefactorConfig myConfig;

    /** If true make permanent changes, otherwise skip operations that would update database or filesystem */
    private boolean myMakeChanges = false;

    /** Should we recurse through any directories that are found */
    private boolean myRecursive = false;

    /** Enable check of file details database table? */
    private boolean myEnableFileDetail = false;

    /**
   *
   */
    public RefactorAndCleanup() {
    }

    /**
   *
   */
    protected boolean initialise(File source, File destination) {
        myLog.debug("initialise()");
        if (!(source.exists() && source.canRead())) {
            myLog.error("initialise() - issue with " + source + " exists() = " + source.exists() + ", canRead() = " + source.canRead());
            return false;
        } else {
            if (!source.isDirectory()) {
                source = source.getParentFile();
            }
            myConfig = new RefactorConfig(source, destination);
            myMetaDataFilter.setInverseFlag(true);
            return true;
        }
    }

    /**
   *
   */
    protected boolean initialiseFileDetail() {
        myLog.debug("initialiseFileDetail()");
        File from = new File("C:\\Documents and Settings\\All Users\\Documents\\My Pictures");
        File to = from;
        if (initialise(from, to)) {
            myConfig.addMapping(new File("paulyn/paulyn_drawing"), new File("Pole_MathewPaulyn_Scanned/paulyn_drawing"));
            myConfig.addMapping(new File("Pole_IanLynette/2005"), new File("Pole_IanLynette/2006"));
            return true;
        } else {
            return false;
        }
    }

    /**
   * @return the makeChanges
   */
    public boolean isMakeChanges() {
        return myMakeChanges;
    }

    /**
   * @param makeChanges set the makeChanges flag
   */
    protected void setMakeChanges(boolean makeChanges) {
        this.myMakeChanges = makeChanges;
    }

    /**
   * @return the enableFileDetail
   */
    public boolean isEnableFileDetail() {
        return myEnableFileDetail;
    }

    /**
   * @param enableFileDetail set the enableFileDetail flag
   */
    protected void setEnableFileDetail(boolean enableFileDetail) {
        this.myEnableFileDetail = enableFileDetail;
    }

    /**
   * @return the isRecursive
   */
    public boolean isRecursive() {
        return myRecursive;
    }

    /**
   * @param isRecursive set the recursive flag
   */
    protected void setRecursive(boolean isRecursive) {
        myLog.info("setRecursive(" + isRecursive + ")");
        myRecursive = isRecursive;
    }

    /**
   * @throws IOException
   *
   */
    public boolean refactor(EntityManagerFactory photoElementsEmf, EntityManagerFactory cahayaEmf) throws IOException {
        myLog.debug("refactor () - enter");
        return refactor(myConfig.getFromBasePath(), photoElementsEmf, cahayaEmf);
    }

    /**
   * @throws IOException
   *
   */
    private boolean refactor(File file, EntityManagerFactory photoElementsEmf, EntityManagerFactory cahayaEmf) throws IOException {
        myLog.debug("refactor (" + file + ")");
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles(myMetaDataFilter);
                for (int i = 0; i < files.length; i++) {
                    if (isRecursive() || !files[i].isDirectory()) {
                        boolean result = refactor(files[i], photoElementsEmf, cahayaEmf);
                        if (!result) {
                            return false;
                        }
                    }
                }
                if (myMakeChanges) {
                    files = file.listFiles(myMetaDataFilter);
                    if (files.length == 0) {
                        myLog.info("deleting " + file);
                        FileUtil.deleteDirectory(file);
                    } else {
                        myLog.info("length = " + files.length + " for " + file);
                    }
                }
            } else {
                boolean result = process(file, photoElementsEmf, cahayaEmf);
                if (!result) {
                    return false;
                }
            }
            return true;
        } else {
            myLog.error(file + " does not exist");
            return false;
        }
    }

    /**
   * Algorithm
   * <ol>
   * <li>check for duplicate - determine preferred location (base on camera exif or bluefoot location)</li>
   * <li>refactor name - update photo elements database</li>
   * </ol>
   * @throws IOException
   *
   */
    private boolean process(File source, EntityManagerFactory photoElementsEmf, EntityManagerFactory cahayaEmf) throws IOException {
        myLog.debug("process(" + source + ") - enter");
        EntityManager peEm = photoElementsEmf.createEntityManager();
        EntityTransaction peTx = peEm.getTransaction();
        peTx.begin();
        Query selectQuery = peEm.createQuery("select imt from PhotoElementsImage imt where imt.myMediaFullPath = :mediaFullPath");
        selectQuery.setParameter("mediaFullPath", source.toString());
        PhotoElementsImage pei = null;
        try {
            pei = (PhotoElementsImage) (selectQuery.getSingleResult());
        } catch (EntityNotFoundException exc) {
            myLog.debug("process() - no match for " + source.toString());
        } catch (NoResultException exc) {
            myLog.debug("process() - no match for " + source.toString());
        } catch (NonUniqueResultException exc) {
            myLog.debug("process() - no match for " + source.toString());
        }
        String md5sum = FileUtil.calcDigest(source, "MD5");
        FileDetailModel fde = null;
        if (myEnableFileDetail) {
            EntityManager cEm = cahayaEmf.createEntityManager();
            EntityTransaction cTx = cEm.getTransaction();
            cTx.begin();
            selectQuery = cEm.createQuery("select fde from FileDetailModel fde where fde.myMd5Sum = :md5sum");
            selectQuery.setParameter("md5sum", md5sum);
            try {
                fde = (FileDetailModel) (selectQuery.getSingleResult());
            } catch (EntityNotFoundException exc) {
                myLog.debug("process() - no file detail for " + md5sum);
            } catch (NoResultException exc) {
                myLog.debug("process() - no file detail for " + md5sum);
            } catch (NonUniqueResultException exc) {
                myLog.debug("process() - no file detail for " + md5sum);
            }
            cTx.commit();
            cEm.close();
        }
        File destination = null;
        if (fde != null) {
            destination = myConfig.bluefootTranslate(fde.getPath(), fde.getName());
        } else {
            destination = myConfig.mapTranslate(source);
        }
        myLog.debug("process() - destination = " + destination);
        if (moveFile(source, md5sum, pei != null, destination)) {
            if (myMakeChanges && pei != null) {
                pei.setMediaFullPath(destination.getPath());
                peEm.persist(pei);
            }
        }
        peTx.commit();
        peEm.close();
        return true;
    }

    /**
   *
   * @param sourceInPhotoElements
   */
    private boolean moveFile(File source, String sourceMd5Sum, boolean sourceInPhotoElements, File destination) {
        myLog.debug("moveFile(" + source + ", " + destination + ") - enter");
        File destinationParent = destination.getParentFile();
        if (!destinationParent.exists()) {
            destinationParent.mkdirs();
        } else if (!destinationParent.isDirectory()) {
            myLog.warn("destination directory " + destinationParent + " exists and is not a directory");
            return false;
        }
        if (source.equals(destination)) {
            myLog.warn("source " + source + "\n same as destination " + destination);
            return false;
        }
        if (destination.exists()) {
            if (destination.isFile() && destination.length() == source.length()) {
                String md5sum = null;
                try {
                    md5sum = FileUtil.calcDigest(destination, "MD5");
                } catch (IOException exc) {
                    myLog.error("moveFile", exc);
                    return false;
                }
                if (sourceMd5Sum.equals(md5sum)) {
                    if (source.lastModified() < destination.lastModified()) {
                        myLog.info("" + source + "\n  replacing " + destination);
                        if (myMakeChanges) {
                            destination.delete();
                            source.renameTo(destination);
                        }
                    } else {
                        myLog.info("deleting duplicate " + source + "\n  of " + destination);
                        if (myMakeChanges) {
                            source.delete();
                        }
                    }
                    return true;
                } else if (sourceInPhotoElements && source.getName().equals(destination.getName())) {
                    myLog.info("deleting duplicate " + source + "\n  of " + destination);
                    if (myMakeChanges) {
                        source.delete();
                    }
                    return true;
                } else {
                    myLog.warn("different md5sum " + source + "\n  and " + destination);
                    return false;
                }
            } else {
                myLog.warn("different length " + source + "\n " + destination + " already exists,\n but file is different length " + source.length() + " != " + destination.length());
                return false;
            }
        } else {
            myLog.info("  moving " + source + "\n      to " + destination);
            if (myMakeChanges) {
                source.renameTo(destination);
            }
            return true;
        }
    }

    /**
   * @param args
   */
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(new HelpOption());
        ActionSetOption actionOption = new ActionSetOption();
        actionOption.addPossibleValue("refactor", true);
        options.addOption(actionOption);
        options.addOption(new SourceFileOption());
        options.addOption(new DestinationFileOption());
        options.addOption(new RecursiveOption());
        options.addOption(new DummyRunOption());
        options.addOption(new EnableFileDetailOption());
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(HelpOption.cValue)) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp(PhotoElementsInfo.class.toString(), options);
            } else {
                boolean optionsOkay = true;
                File sourceFile = null;
                if (line.hasOption(SourceFileOption.cValue)) {
                    sourceFile = new File(line.getOptionValue(SourceFileOption.cValue));
                    if (!sourceFile.canRead()) {
                        optionsOkay = false;
                        System.out.println("unable to read " + sourceFile);
                    }
                }
                File destinationFile = null;
                if (line.hasOption(DestinationFileOption.cValue)) {
                    destinationFile = new File(line.getOptionValue(DestinationFileOption.cValue));
                }
                if (optionsOkay) {
                    EntityManagerFactory photoElementsEmf = Persistence.createEntityManagerFactory("photoelements");
                    EntityManagerFactory cahayaEmf = Persistence.createEntityManagerFactory("cahaya");
                    RefactorAndCleanup rac = new RefactorAndCleanup();
                    if (rac.initialise(sourceFile, destinationFile)) {
                        rac.setRecursive(line.hasOption(RecursiveOption.cValue));
                        rac.setMakeChanges(!line.hasOption(DummyRunOption.cValue));
                        rac.setEnableFileDetail(line.hasOption(EnableFileDetailOption.cValue));
                        try {
                            rac.refactor(sourceFile, photoElementsEmf, cahayaEmf);
                        } catch (IOException exc) {
                            Logger log = LoggerFactory.getLogger(RefactorAndCleanup.class);
                            log.error("error refactoring", exc);
                        }
                    }
                    photoElementsEmf.close();
                    cahayaEmf.close();
                } else {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp(RefactorAndCleanup.class.toString(), options);
                }
            }
        } catch (ParseException exc) {
            System.out.println("Unexpected exception: " + exc.getMessage());
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp(RefactorAndCleanup.class.toString(), options);
        }
    }
}

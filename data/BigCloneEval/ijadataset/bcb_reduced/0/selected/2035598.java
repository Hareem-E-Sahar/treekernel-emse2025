package edu.harvard.iq.safe.saasystem.web.auditschema;

import edu.harvard.iq.safe.saasystem.util.ConfigFile;
import edu.harvard.iq.safe.saasystem.util.SAASConfigurationRegistryBean;
import edu.harvard.iq.safe.saasystem.versioning.*;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.*;
import java.io.*;
import java.util.*;
import javax.faces.application.FacesMessage;
import javax.faces.context.*;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 *
 * @author Akio Sone
 */
@ManagedBean(name = "auditSchemaInstList")
@RequestScoped
public class AuditSchemaInstancesListManagedBean implements Serializable {

    static final Logger logger = Logger.getLogger(AuditSchemaInstancesListManagedBean.class.getName());

    @EJB
    ConfigFile configFile;

    @EJB
    SAASSubversionServiceBean saasSubversionServiceBean;

    String auditSchemaInstanceDirName;

    String instanceFileNamePattern;

    String bkInstanceFileNamePattern;

    List<AuditSchemaInstanceFile> currentAuditSchemaInstance = new ArrayList<AuditSchemaInstanceFile>();

    @EJB
    SAASConfigurationRegistryBean saasConfigurationRegistry;

    static String SAAS_DEFAULT_TIME_ZONE = "America/New_York";

    String timeZone = null;

    String timestampPattern = null;

    static final String SAAS_TIME_STAMP_PATTERN_FOR_WEB_PAGE = "yyyy-MM-dd'T'HH:mm:ss zz";

    String fileLocationForViewPage;

    public String getFileLocationForViewPage() {
        return fileLocationForViewPage;
    }

    public void setFileLocationForViewPage(String fileLocationForViewPage) {
        this.fileLocationForViewPage = fileLocationForViewPage;
    }

    AuditSchemaInstanceFile auditSchemaInstanceForViewPage;

    public AuditSchemaInstanceFile getAuditSchemaInstanceForViewPage() {
        return auditSchemaInstanceForViewPage;
    }

    public void setAuditSchemaInstanceForViewPage(AuditSchemaInstanceFile auditSchemaInstanceForViewPage) {
        this.auditSchemaInstanceForViewPage = auditSchemaInstanceForViewPage;
    }

    public List<AuditSchemaInstanceFile> getCurrentAuditSchemaInstance() {
        return currentAuditSchemaInstance;
    }

    public void setCurrentAuditSchemaInstance(List<AuditSchemaInstanceFile> currentAuditSchemaInstance) {
        this.currentAuditSchemaInstance = currentAuditSchemaInstance;
    }

    List<AuditSchemaInstanceFile> auditSchemaInstancesList = new ArrayList<AuditSchemaInstanceFile>();

    public List<AuditSchemaInstanceFile> getAuditSchemaInstancesList() {
        return auditSchemaInstancesList;
    }

    public void setAuditSchemaInstancesList(List<AuditSchemaInstanceFile> auditSchemaInstancesList) {
        this.auditSchemaInstancesList = auditSchemaInstancesList;
    }

    List<SubversionLogEntry> archivedAuditSchemaInstancesList = new ArrayList<SubversionLogEntry>();

    public List<SubversionLogEntry> getArchivedAuditSchemaInstancesList() {
        Collections.sort(archivedAuditSchemaInstancesList);
        Collections.reverse(archivedAuditSchemaInstancesList);
        return archivedAuditSchemaInstancesList;
    }

    public void setArchivedAuditSchemaInstancesList(List<SubversionLogEntry> archivedAuditSchemaInstancesList) {
        this.archivedAuditSchemaInstancesList = archivedAuditSchemaInstancesList;
    }

    /** Creates a new instance of AuditSchemaInstancesListManagedBean */
    public AuditSchemaInstancesListManagedBean() {
    }

    @PostConstruct
    public void initialize() {
        if (StringUtils.isNotBlank(saasConfigurationRegistry.getSaasConfigProperties().getProperty("saas.timezone"))) {
            timeZone = saasConfigurationRegistry.getSaasConfigProperties().getProperty("saas.timezone");
        } else {
            logger.info("the user-defined time-zone is not available; use the defaul");
            timeZone = SAAS_DEFAULT_TIME_ZONE;
        }
        logger.info("time zone=" + timeZone);
        if (auditSchemaInstanceDirName == null) {
            auditSchemaInstanceDirName = configFile.getAuditSchemaFileDir();
        }
        File auditSchemaInstanceDir = new File(auditSchemaInstanceDirName);
        instanceFileNamePattern = configFile.getAuditSchemaFileName();
        bkInstanceFileNamePattern = instanceFileNamePattern + ".bkup_";
        FilenameFilter bkIstanceFileFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(bkInstanceFileNamePattern);
            }
        };
        File[] instanceFiles = auditSchemaInstanceDir.listFiles(bkIstanceFileFilter);
        for (File f : instanceFiles) {
            auditSchemaInstancesList.add(new AuditSchemaInstanceFile(f.getName(), f.lastModified(), f.getAbsolutePath()));
        }
        FilenameFilter instanceFileFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.equals(instanceFileNamePattern);
            }
        };
        File[] currentInstanceFile = auditSchemaInstanceDir.listFiles(instanceFileFilter);
        for (File f : currentInstanceFile) {
            currentAuditSchemaInstance.add(new AuditSchemaInstanceFile(f.getName(), f.lastModified(), f.getAbsolutePath()));
        }
        archivedAuditSchemaInstancesList = saasSubversionServiceBean.getLog(configFile.getInputFilesSvnPath(), configFile.getAuditSchemaFileName());
        timestampPattern = configFile.getTimestampPattern();
        if (StringUtils.isBlank(timestampPattern)) {
            timestampPattern = SAAS_TIME_STAMP_PATTERN_FOR_WEB_PAGE;
        } else {
            if (!timestampPattern.endsWith(" zz")) {
                timestampPattern += " zz";
            }
        }
    }

    public String gotoHomePage() {
        logger.info("go to Home Page");
        return "/index.xhtml";
    }

    public String prepareCreate() {
        logger.info("go to create a new audit schema instance");
        return "CreateAuditSchema.xhtml";
    }

    public String prepareEditPage() {
        return "EditAuditSchema.xhtml";
    }

    public String prepareViewPage(AuditSchemaInstanceFile instFile) {
        logger.info("requested audit schema name=" + instFile.getFileName());
        auditSchemaInstanceForViewPage = instFile;
        return "ViewAuditSchema.xhtml";
    }

    public String deleteAuditSchemaInstance(AuditSchemaInstanceFile fileToBeDeleted) {
        logger.info("requested audit schema name to be delted=" + fileToBeDeleted.getFileName());
        auditSchemaInstancesList.remove(fileToBeDeleted);
        deleteAuditSchemaInstanceFile(fileToBeDeleted.fileName);
        return null;
    }

    void deleteAuditSchemaInstanceFile(String fileName) {
        String toBeDeleted = configFile.getAuditSchemaFileDir() + File.separator + fileName;
        boolean isDeleted = new File(toBeDeleted).delete();
        if (isDeleted) {
            logger.info("The requested audit schema instance was deleted:" + fileName);
        } else {
            logger.warning("deleting the requested audit schema instance failed:" + fileName);
        }
    }

    public String generateAuditReport() {
        return "/auditreport/ListAuditReports?faces-redirect=true";
    }

    public void downloadRequestedArchivedSchemaInstance() {
        FacesContext cntxt = FacesContext.getCurrentInstance();
        HttpServletResponse res = (HttpServletResponse) cntxt.getExternalContext().getResponse();
        long revisionNumber = -2;
        Map<String, String> params = cntxt.getExternalContext().getRequestParameterMap();
        revisionNumber = Long.parseLong(params.get("archivedAuditSchemaInstanceFileRevisionNumber"));
        logger.info("param: archivedAuditSchemaInstanceFileRevisionNumber=" + revisionNumber);
        String filePath = configFile.getInputFilesSvnPath() + "/" + configFile.getAuditSchemaFileName();
        logger.info("archived schema instance file: svn file path=" + filePath);
        if (revisionNumber <= 0) {
            logger.warning("revision Number is not positive abort the downloading");
            return;
        }
        OutputStream out = null;
        FileInputStream in = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String fileNameWithRevisionNumber = configFile.getAuditSchemaFileNameBase() + ".revision_" + revisionNumber + ".xml";
        String locallySavedFilePath = configFile.getAuditSchemaFileDir() + "/" + fileNameWithRevisionNumber;
        try {
            File locallSavedCopy = new File(locallySavedFilePath);
            if (locallSavedCopy.exists() && locallSavedCopy.length() > 0) {
                in = new FileInputStream(locallSavedCopy);
                res.setContentType("application/xml");
                res.setHeader("content-disposition", "attachment; filename=" + fileNameWithRevisionNumber);
                out = res.getOutputStream();
                byte[] buf = new byte[1024];
                int count = 0;
                while ((count = in.read(buf)) >= 0) {
                    out.write(buf, 0, count);
                }
            } else {
                baos = saasSubversionServiceBean.checkoutByRevisionNumber(filePath, revisionNumber);
                res.setContentType("application/xml");
                res.setHeader("content-disposition", "attachment; filename=" + fileNameWithRevisionNumber);
                out = res.getOutputStream();
                out.write(baos.toByteArray());
            }
            logger.info("finish downloading the requested arhived audit schema instance file");
        } catch (IOException e) {
            logger.warning("failed to return the requested audit schema instance file");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    logger.warning("failed to close the input stream for the requested archived file");
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    logger.warning("failed to close the ouput stream for the requested archived file");
                }
            }
        }
        cntxt.responseComplete();
    }

    public void downloadCurrentSchemaInstance() {
        FacesContext cntxt = FacesContext.getCurrentInstance();
        HttpServletResponse res = (HttpServletResponse) cntxt.getExternalContext().getResponse();
        long revisionNumber = -2;
        String filePath = configFile.getInputFilesSvnPath() + "/" + configFile.getAuditSchemaFileName();
        logger.info("archived schema instance file: svn file path=" + filePath);
        revisionNumber = saasSubversionServiceBean.getLastestRevisionNumber(filePath);
        logger.info("Latest Revision Number=" + revisionNumber);
        if (revisionNumber <= 0) {
            logger.warning("revision Number is not positive abort the downloading");
            return;
        }
        OutputStream out = null;
        FileInputStream in = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String fileNameWithRevisionNumber = configFile.getAuditSchemaFileNameBase() + ".revision_" + revisionNumber + ".xml";
        String locallySavedFilePath = configFile.getAuditSchemaFileDir() + "/" + fileNameWithRevisionNumber;
        try {
            File locallSavedCopy = new File(locallySavedFilePath);
            if (locallSavedCopy.exists() && locallSavedCopy.length() > 0) {
                logger.info("locally saved current audit schema instance is available");
                in = new FileInputStream(locallSavedCopy);
                res.setContentType("application/xml");
                res.setHeader("content-disposition", "attachment; filename=" + fileNameWithRevisionNumber);
                out = res.getOutputStream();
                byte[] buf = new byte[1024];
                int count = 0;
                while ((count = in.read(buf)) >= 0) {
                    out.write(buf, 0, count);
                }
            } else {
                logger.info("locally saved current audit schema instance is not available");
                logger.info("get a copy of the instance from the subversion repository");
                baos = saasSubversionServiceBean.checkoutByRevisionNumber(filePath, revisionNumber);
                res.setContentType("application/xml");
                res.setHeader("content-disposition", "attachment; filename=" + fileNameWithRevisionNumber);
                out = res.getOutputStream();
                out.write(baos.toByteArray());
            }
            logger.info("finish downloading the current audit schema instance file");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Download Request: ", "Completed"));
        } catch (IOException e) {
            logger.warning("failed to return the requested audit schema instance file");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Download Request Failed: ", "Reason: IOException occurred"));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    logger.warning("failed to close the input stream for the requested archived file");
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    logger.warning("failed to close the ouput stream for the requested archived file");
                }
            }
        }
        cntxt.responseComplete();
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getTimestampPattern() {
        return timestampPattern;
    }
}

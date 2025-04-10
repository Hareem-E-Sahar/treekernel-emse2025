package uk.ac.ebi.intact.services.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.UploadedFile;
import org.apache.myfaces.trinidad.model.DefaultBoundedRangeModel;
import org.apache.myfaces.trinidad.component.core.input.CoreInputText;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.event.PollEvent;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.faces.controller.BaseController;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 * This is the managed bean that contains the model of the information show to the user. From this bean,
 * all the information shown is handled. It creates the reports, etc.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller("psiValidatorBean")
@Scope("conversation.access")
@ViewController(viewIds = "/start.xhtml")
public class PsiValidatorBean extends BaseController {

    private static List<String> PROGRESS_STEPS;

    static {
        PROGRESS_STEPS = new ArrayList<String>();
        PROGRESS_STEPS.add("Uploading data to be validated");
        PROGRESS_STEPS.add("Configuring the validator");
        PROGRESS_STEPS.add("Running XML validation");
        PROGRESS_STEPS.add("Running controlled vocabulary mapping checks");
        PROGRESS_STEPS.add("Running semantic validation");
        PROGRESS_STEPS.add("Building validation report");
    }

    public static final String URL_PARAM = "url";

    public static final String MODEL_PARAM = "model";

    /**
     * Logging is an essential part of an application
     */
    private static final Log log = LogFactory.getLog(PsiValidatorBean.class);

    /**
     * If true, a local file is selected to be uploaded
     */
    private boolean uploadLocalFile;

    /**
     * The type of validation to be performed: syntax, cv, MIMIX, IMEx.
     */
    private ValidationScope validationScope;

    /**
     * Data model to be validated. Default value is PSI-MI (Note: this should be reflected in the tabbedPanel)
     */
    private DataModel model = DataModel.PSI_MI;

    /**
     * The file to upload
     */
    private UploadedFile psiFile;

    /**
     * The URL to upload
     */
    private String psiUrl;

    /**
     * dData model of the validation progress.
     */
    protected volatile DefaultBoundedRangeModel progressModel;

    /**
     * If we are viewing a report, this is the report viewed
     */
    private PsiReport currentPsiReport;

    /**
     * Constructor
     */
    public PsiValidatorBean() {
        this.uploadLocalFile = true;
    }

    /**
     * This is a valueChangeEvent. When the selection of File/URL is changed, this event is fired.
     *
     * @param vce needed in valueChangeEvent methods. From it we get the new value
     */
    public void uploadTypeChanged(ValueChangeEvent vce) {
        String type = (String) vce.getNewValue();
        uploadLocalFile = type.equals("local");
        if (log.isDebugEnabled()) log.debug("Upload type changed, is local file? " + uploadLocalFile);
    }

    /**
     * This is a valueChangeEvent. When the selection of File/URL is changed, this event is fired.
     *
     * @param vce needed in valueChangeEvent methods. From it we get the new value
     */
    public void validationScopeChangedMI(ValueChangeEvent vce) {
        String type = (String) vce.getNewValue();
        validationScope = ValidationScope.forName(type);
        if (log.isDebugEnabled()) log.debug("MI Validation scope changed to '" + validationScope + "'");
    }

    public void validationScopeChangedPAR(ValueChangeEvent vce) {
        String type = (String) vce.getNewValue();
        validationScope = ValidationScope.forName(type);
        if (log.isDebugEnabled()) log.debug("PAR Validation scope changed to '" + validationScope + "'");
    }

    public void validationModelChangedMI(DisclosureEvent event) {
        if (event.isExpanded()) {
            model = DataModel.PSI_MI;
            validationScope = ValidationScope.MIMIX;
            if (log.isDebugEnabled()) log.debug("Data model set to '" + model + "'");
        }
    }

    public void validationModelChangedPAR(DisclosureEvent event) {
        if (event.isExpanded()) {
            model = DataModel.PSI_PAR;
            validationScope = ValidationScope.CV_ONLY;
            if (log.isDebugEnabled()) log.debug("Data model set to '" + model + "'");
        }
    }

    public List<String> getProgressSteps() {
        return PROGRESS_STEPS;
    }

    public DefaultBoundedRangeModel getProgressModel() {
        return progressModel;
    }

    @PreRenderView
    public void initialParams() {
        FacesContext context = FacesContext.getCurrentInstance();
        String urlParam = context.getExternalContext().getRequestParameterMap().get(URL_PARAM);
        String modelParam = context.getExternalContext().getRequestParameterMap().get(MODEL_PARAM);
        if (urlParam != null && modelParam != null) {
            if (log.isInfoEnabled()) {
                log.info("User submitted a request with data specified in the URL: " + urlParam);
            }
            if (modelParam.equalsIgnoreCase("PAR") || modelParam.equalsIgnoreCase("PSI-PAR")) {
                model = DataModel.PSI_PAR;
            } else if (modelParam.equalsIgnoreCase("MI") || modelParam.equalsIgnoreCase("PSI-MI")) {
                model = DataModel.PSI_MI;
            } else {
                String msg = "You have tried to validate a file via URL, however the data model you have specified '" + modelParam + "' was not recognized. Please use one of the following 'MI', 'PSI-MI' or " + "'PAR', 'PSI-PAR'";
                FacesMessage message = new FacesMessage(msg);
                context.addMessage(null, message);
                return;
            }
            uploadLocalFile = false;
            psiUrl = urlParam;
            try {
                initializeProgressModel();
                uploadFromUrl();
            } catch (IOException e) {
                final String msg = "Failed to upload PSI data from given URL";
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null);
                context.addMessage("inputUrl", message);
            }
        } else if (urlParam != null || modelParam != null) {
            String msg = "You have tried to validate a file via URL, however you haven't provided all required " + "parameters. Please specify 'url' and 'model' ('PSI-MI' or 'PSI-PAR').";
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null);
            context.addMessage(null, message);
        }
    }

    public void onPsiFileUpload(ValueChangeEvent event) {
        System.out.println("PsiValidatorBean.psiFile: uploadLocalFile=" + uploadLocalFile);
        if (uploadLocalFile) {
            psiFile = (UploadedFile) event.getNewValue();
            if (psiFile != null) {
                FacesContext context = FacesContext.getCurrentInstance();
                FacesMessage message = new FacesMessage("Successfully uploaded file " + psiFile.getFilename() + " (" + psiFile.getLength() + " bytes)");
                context.addMessage(event.getComponent().getClientId(context), message);
            }
        }
    }

    /**
     * Validates the data entered by the user upon pressing the validate button.
     *
     * @param event
     */
    public void validate(ActionEvent event) {
        initializeProgressModel();
        try {
            if (uploadLocalFile) {
                uploadFromLocalFile();
            } else {
                uploadFromUrl();
            }
        } catch (Throwable t) {
            final String msg = "Failed to upload from " + (uploadLocalFile ? "local file" : "URL");
            log.error(msg, t);
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(msg);
            context.addMessage(event.getComponent().getClientId(context), message);
        }
    }

    private void initializeProgressModel() {
        progressModel = new DefaultBoundedRangeModel(-1, 5);
        progressModel.setValue(0);
    }

    /**
     * Reads the local file.
     *
     * @throws IOException if something has gone wrong with the file
     */
    private void uploadFromLocalFile() throws IOException {
        if (psiFile == null) {
            throw new IllegalStateException("Failed to upload the file");
        }
        if (log.isInfoEnabled()) {
            log.info("Uploading local file: " + psiFile.getFilename());
        }
        File f = storeAsTemporaryFile(psiFile.getInputStream());
        psiFile.dispose();
        PsiReportBuilder builder = new PsiReportBuilder(psiFile.getFilename(), f, model, validationScope, progressModel);
        log.warn("About to start building the PSI report");
        this.currentPsiReport = builder.createPsiReport();
        if (log.isWarnEnabled()) {
            log.warn("After uploading a local file the report was " + (this.currentPsiReport == null ? "not present" : "present"));
        }
    }

    /**
     * Store the content of the given input stream into a temporary file and return its descriptor.
     *
     * @param is the input stream to store.
     * @return a File descriptor describing a temporary file storing the content of the given input stream.
     * @throws IOException if an IO error occur.
     */
    private File storeAsTemporaryFile(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("You must give a non null InputStream");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        File tempDirectory = new File(System.getProperty("java.io.tmpdir", "tmp"));
        if (!tempDirectory.exists()) {
            if (!tempDirectory.mkdirs()) {
                throw new IOException("Cannot create temp directory: " + tempDirectory.getAbsolutePath());
            }
        }
        long id = System.currentTimeMillis();
        File tempFile = File.createTempFile("validator." + id, ".xml", tempDirectory);
        tempFile.deleteOnExit();
        log.info("The file is temporary store as: " + tempFile.getAbsolutePath());
        BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
        String line;
        while ((line = in.readLine()) != null) {
            out.write(line);
        }
        in.close();
        out.flush();
        out.close();
        return tempFile;
    }

    /**
     * Reads the file from a URL, so it can read locally and remotely
     *
     * @throws IOException if something goes wrong with the file or the connection
     */
    private void uploadFromUrl() throws IOException {
        if (log.isInfoEnabled()) {
            log.info("Uploading Url: " + psiUrl);
        }
        try {
            URL url = new URL(psiUrl);
            File f = storeAsTemporaryFile(url.openStream());
            String name = psiUrl.substring(psiUrl.lastIndexOf("/") + 1, psiUrl.length());
            PsiReportBuilder builder = new PsiReportBuilder(name, url, f, model, validationScope, progressModel);
            log.warn("About to start building the PSI report");
            this.currentPsiReport = builder.createPsiReport();
            log.warn("After uploading a URL the report was " + (this.currentPsiReport == null ? "not present" : "present"));
        } catch (Throwable e) {
            currentPsiReport = null;
            final String msg = "The given URL wasn't valid";
            log.error(msg, e);
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null);
            context.addMessage(null, message);
        }
    }

    /**
     * This method is a "validator" method. It has the arguments that JSF specifies for this kind of methods.
     * The objective is to validate the URL provided by the user, whether it is in the correct form
     * or the place where it points it does exist
     *
     * @param context    The JSF FacesContext
     * @param toValidate The UIComponent to validate (this is a UIInput component), the controller of the text box
     * @param value      The value provided in the text box by the user
     */
    public void validateUrlFormat(FacesContext context, UIComponent toValidate, Object value) {
        if (log.isDebugEnabled()) {
            log.debug("Validating URL: " + value);
        }
        currentPsiReport = null;
        URL url = null;
        CoreInputText inputCompToValidate = (CoreInputText) toValidate;
        String toValidateClientId = inputCompToValidate.getClientId(context);
        try {
            url = new URL((String) value);
        } catch (MalformedURLException e) {
            log.warn("Invalid URL given by the user: " + value, e);
            inputCompToValidate.setValid(false);
            context.addMessage(toValidateClientId, new FacesMessage("The given URL was not valid."));
            return;
        }
        try {
            url.openStream();
        } catch (Throwable e) {
            log.error("Error while validating the URL.", e);
            inputCompToValidate.setValid(false);
            context.addMessage(toValidateClientId, new FacesMessage("Could not read URL content."));
        }
    }

    public boolean isUploadLocalFile() {
        return uploadLocalFile;
    }

    public void setUploadLocalFile(boolean uploadLocalFile) {
        this.uploadLocalFile = uploadLocalFile;
    }

    public UploadedFile getPsiFile() {
        return psiFile;
    }

    public void setPsiFile(UploadedFile psiFile) {
        this.psiFile = psiFile;
    }

    public String getPsiUrl() {
        return psiUrl;
    }

    public void setPsiUrl(String psiUrl) {
        this.psiUrl = psiUrl;
    }

    public PsiReport getCurrentPsiReport() {
        return currentPsiReport;
    }

    public void setCurrentPsiReport(PsiReport currentPsiReport) {
        this.currentPsiReport = currentPsiReport;
    }
}

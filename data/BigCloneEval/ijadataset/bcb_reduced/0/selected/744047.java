package fr.insa.rennes.pelias.pexecutor;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.HiddenField;
import com.sun.webui.jsf.component.PasswordField;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.TextField;
import fr.insa.rennes.pelias.framework.*;
import fr.insa.rennes.pelias.pexecutor.providers.BatchParam;
import fr.insa.rennes.pelias.pexecutor.providers.FilesOutputs;
import fr.insa.rennes.pelias.pexecutor.providers.UserParam;
import fr.insa.rennes.pelias.pexecutor.providers.statisicProvider;
import fr.insa.rennes.pelias.platform.IRepository;
import fr.insa.rennes.pelias.platform.PObjectNotFoundException;
import fr.insa.rennes.pelias.platform.PObjectReference;
import fr.insa.rennes.pelias.platform.PSxSObjectReference;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 *
 * @version Viewexecution.java
 * @version Created on 17 févr. 2009, 16:32:22
 * @author 2bo
 */
public class Viewexecution extends AbstractPageBean {

    /**
     * @return the attachmentComplete
     */
    public static String getAttachmentComplete() {
        return attachmentComplete;
    }

    /**
     * @param aAttachmentComplete the attachmentComplete to set
     */
    public static void setAttachmentComplete(String aAttachmentComplete) {
        attachmentComplete = aAttachmentComplete;
    }

    /**
     * @return the attachmentLaunch
     */
    public static String getAttachmentLaunch() {
        return attachmentLaunch;
    }

    /**
     * @return the attachmentLaunchAsInt
     */
    public static String getAttachmentLaunchAsInt() {
        return attachmentLaunchAsInt;
    }

    /**
     * @param aAttachmentLaunch the attachmentLaunch to set
     */
    public static void setAttachmentLaunch(String aAttachmentLaunch) {
        attachmentLaunch = aAttachmentLaunch;
    }

    /**
     * @return the attachmentEvents
     */
    public static String getAttachmentEvents() {
        return attachmentEvents;
    }

    /**
     * @param aAttachmentEvents the attachmentEvents to set
     */
    public static void setAttachmentEvents(String aAttachmentEvents) {
        attachmentEvents = aAttachmentEvents;
    }

    /**
     * @return the attachmentSubmissionPerformed
     */
    public static String getAttachmentSubmissionPerformed() {
        return attachmentSubmissionPerformed;
    }

    /**
     * @param aAttachmentSubmissionPerformed the attachmentSubmissionPerformed to set
     */
    public static void setAttachmentSubmissionPerformed(String aAttachmentSubmissionPerformed) {
        attachmentSubmissionPerformed = aAttachmentSubmissionPerformed;
    }

    /**
     * @return the attachmentSubmissionProgress
     */
    public static String getAttachmentSubmissionProgress() {
        return attachmentSubmissionProgress;
    }

    /**
     * @param aAttachmentSubmissionProgress the attachmentSubmissionProgress to set
     */
    public static void setAttachmentSubmissionProgress(String aAttachmentSubmissionProgress) {
        attachmentSubmissionProgress = aAttachmentSubmissionProgress;
    }

    /**
     * @return the attachmentBatches
     */
    public static String getAttachmentBatches() {
        return attachmentBatches;
    }

    /**
     * @param aAttachmentBatches the attachmentBatches to set
     */
    public static void setAttachmentBatches(String aAttachmentBatches) {
        attachmentBatches = aAttachmentBatches;
    }

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }

    private Execution currentExecution;

    private BatchParam[] batchParams;

    private UserParam[] userParams;

    private FilesOutputs[] outputPath;

    private ExecutionEvent[] errors;

    private HiddenField hiddenFieldUUID = new HiddenField();

    private String lastError;

    private ExecutionProcessor executionProcessor;

    private statisicProvider stats;

    private String clusterName;

    private String chainName;

    private String progress;

    private String accessBatches;

    private boolean passwordLocked;

    private boolean complete;

    private static String attachmentComplete = "94DC46AB-3F39-4942-8924-7AFA147D1C1E";

    private static String attachmentLaunch = "54766FFC-3934-4321-A050-EA28E41450DA";

    private static String attachmentLaunchAsInt = "EF0FFDE2-2707-442b-8480-7E7B7BA3D3BE";

    private static String attachmentEvents = "B5C3BE8B-D4A1-47ae-BE3D-B9BF452D740A";

    private static String attachmentSubmissionPerformed = "EE3D97B1-FE31-4225-8D62-13CBA32156D5";

    private static String attachmentSubmissionProgress = "CB0434D6-5B10-4ec2-BD27-DC2C57221C9F";

    private static String attachmentBatches = "B2514087-13D4-42c9-ADE8-97E68B6CAC76";

    public HiddenField getHiddenFieldUUID() {
        return hiddenFieldUUID;
    }

    public void setHiddenFieldUUID(HiddenField hf) {
        this.hiddenFieldUUID = hf;
    }

    private Button buttonDerivate = new Button();

    public Button getButtonDerivate() {
        return buttonDerivate;
    }

    public void setButtonDerivate(Button b) {
        this.buttonDerivate = b;
    }

    private Button buttonStart = new Button();

    public Button getButtonStart() {
        return buttonStart;
    }

    public void setButtonStart(Button b) {
        this.buttonStart = b;
    }

    private Button buttonPause = new Button();

    public Button getButtonPause() {
        return buttonPause;
    }

    public void setButtonPause(Button b) {
        this.buttonPause = b;
    }

    private Button buttonStop = new Button();

    public Button getButtonStop() {
        return buttonStop;
    }

    public void setButtonStop(Button b) {
        this.buttonStop = b;
    }

    private Button buttonResume = new Button();

    public Button getButtonResume() {
        return buttonResume;
    }

    public void setButtonResume(Button b) {
        this.buttonResume = b;
    }

    private StaticText staticTextStatus = new StaticText();

    public StaticText getStaticTextStatus() {
        return staticTextStatus;
    }

    public void setStaticTextStatus(StaticText st) {
        this.staticTextStatus = st;
    }

    private TextField textFieldSSHLogin = new TextField();

    public TextField getTextFieldSSHLogin() {
        return textFieldSSHLogin;
    }

    public void setTextFieldSSHLogin(TextField tf) {
        this.textFieldSSHLogin = tf;
    }

    private PasswordField passwordField1 = new PasswordField();

    public PasswordField getPasswordField1() {
        return passwordField1;
    }

    public void setPasswordField1(PasswordField pf) {
        this.passwordField1 = pf;
    }

    private StaticText staticTextDisplayBatches = new StaticText();

    public StaticText getStaticTextDisplayBatches() {
        return staticTextDisplayBatches;
    }

    public void setStaticTextDisplayBatches(StaticText st) {
        this.staticTextDisplayBatches = st;
    }

    private StaticText staticTextLaunch = new StaticText();

    public StaticText getStaticTextLaunch() {
        return staticTextLaunch;
    }

    public void setStaticTextLaunch(StaticText st) {
        this.staticTextLaunch = st;
    }

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public Viewexecution() {
    }

    /**
     * <p>Callback method that is called whenever a page is navigated to,
     * either directly via a URL, or indirectly via page navigation.
     * Customize this method to acquire resources that will be needed
     * for event handlers and lifecycle methods, whether or not this
     * page is performing post back processing.</p>
     * 
     * <p>Note that, if the current request is a postback, the property
     * values of the components do <strong>not</strong> represent any
     * values submitted with this request.  Instead, they represent the
     * property values that were saved for this view when it was rendered.</p>
     */
    @Override
    public void init() {
        super.init();
        Map<String, String> res = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String uuid = res.get("id");
        loadPage(uuid);
        try {
            _init();
        } catch (Exception e) {
            log("Viewexecution Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e : new FacesException(e);
        }
    }

    /**
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    @Override
    public void preprocess() {
        if (hiddenFieldUUID.getValue() != null) {
            loadPage((String) hiddenFieldUUID.getValue());
        }
    }

    /**
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a postback and then navigated to a different page).  Customize
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     */
    @Override
    public void prerender() {
    }

    /**
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    @Override
    public void destroy() {
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     *
     * @return reference to the scoped data bean
     */
    protected SessionBean1 getSessionBean1() {
        return (SessionBean1) getBean("SessionBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     *
     * @return reference to the scoped data bean
     */
    protected ApplicationBean1 getApplicationBean1() {
        return (ApplicationBean1) getBean("ApplicationBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     *
     * @return reference to the scoped data bean
     */
    protected RequestBean1 getRequestBean1() {
        return (RequestBean1) getBean("RequestBean1");
    }

    /**
     * @return the currentExecution
     */
    public Execution getCurrentExecution() {
        return currentExecution;
    }

    /**
     * @param currentExecution the currentExecution to set
     */
    public void setCurrentExecution(Execution currentExecution) {
        this.currentExecution = currentExecution;
    }

    public void loadPage(String uuid) {
        try {
            if (uuid == null) {
                throw new IllegalArgumentException();
            }
            IRepository<Execution> executionRepository = ApplicationBean1.getExecutionIRepository();
            setCurrentExecution(executionRepository.getObject(UUID.fromString(uuid)));
            if (currentExecution == null) {
                throw new IllegalArgumentException();
            }
            hiddenFieldUUID.setValue(uuid);
            try {
                clusterName = currentExecution.getClusterReference().resolve(ApplicationBean1.getClusterIRepository(), false, false).getLabel();
            } catch (NullPointerException e) {
            }
            Chain currentChain = currentExecution.getChainReference().resolve(ApplicationBean1.getChainISxSRepository(), false, false, false);
            if (currentChain != null) {
                chainName = currentChain.getLabel();
                List<ChainInput> inputs = currentChain.getInputs();
                int size = inputs.size();
                HashMap<Integer, String> userParameters = currentExecution.getUserParameters();
                HashMap<Integer, PObjectReference> batchParameters = currentExecution.getBatchReferences();
                LinkedList<UserParam> userParamsList = new LinkedList<UserParam>();
                LinkedList<BatchParam> batchParamsList = new LinkedList<BatchParam>();
                for (int i = 0; i < size; i++) {
                    ChainInput chainIn = inputs.get(i);
                    if (chainIn.getType() == InputType.UserParameter) {
                        UserParam userP = new UserParam(chainIn.getName(), userParameters.get(i));
                        userParamsList.add(userP);
                    }
                    if (chainIn.getType() == InputType.Batch) {
                        BatchParam batchP = new BatchParam(chainIn.getName(), batchParameters.get(i).resolve(ApplicationBean1.getBatchIRepository(), false, false).getLabel());
                        batchParamsList.add(batchP);
                    }
                }
                executionProcessor = ApplicationBean1.getExecutionProcess(currentExecution.getSelfReference());
                if (executionProcessor != null) {
                    stats = new statisicProvider(executionProcessor);
                    Map<ChainOutput, String> pathes = executionProcessor.getOutputFolders();
                    Set keyvalues = pathes.entrySet();
                    outputPath = new FilesOutputs[keyvalues.size()];
                    Iterator it = keyvalues.iterator();
                    int j = 0;
                    while (it.hasNext()) {
                        Entry entry = (Entry) it.next();
                        outputPath[j] = new FilesOutputs(((ChainOutput) entry.getKey()).getName(), (String) entry.getValue());
                        j++;
                    }
                    List<ExecutionEvent> error = executionProcessor.getEvents();
                    errors = new ExecutionEvent[error.size()];
                    int i = 0;
                    for (ExecutionEvent e : error) {
                        errors[i] = e;
                        i++;
                    }
                    String idSansTiret = currentExecution.getId().toString().replaceAll("-", "");
                    accessBatches = "openPopup('SelectBatch','Batch" + idSansTiret + "','?id=" + currentExecution.getId() + "','height=200, width=250')";
                }
                userParams = new UserParam[userParamsList.size()];
                for (int i = 0; i < userParamsList.size(); i++) {
                    userParams[i] = userParamsList.get(i);
                }
                batchParams = new BatchParam[batchParamsList.size()];
                for (int i = 0; i < batchParamsList.size(); i++) {
                    batchParams[i] = batchParamsList.get(i);
                }
            }
            staticTextStatus.setValue("Non lancée");
            buttonDerivate.setDisabled(false);
            buttonStart.setDisabled(false);
            passwordLocked = true;
            staticTextDisplayBatches.setRendered(false);
            if (executionProcessor != null) {
                if (executionProcessor.isStarted()) {
                    staticTextStatus.setValue("En cours...");
                    buttonStop.setDisabled(false);
                    buttonResume.setDisabled(true);
                    buttonPause.setDisabled(false);
                    buttonStart.setDisabled(true);
                    passwordLocked = false;
                    staticTextDisplayBatches.setRendered(true);
                    if (executionProcessor.isPaused()) {
                        staticTextStatus.setValue("En pause");
                        buttonResume.setDisabled(false);
                        buttonPause.setDisabled(true);
                    }
                } else {
                    buttonPause.setDisabled(true);
                    buttonStop.setDisabled(true);
                    buttonResume.setDisabled(true);
                }
            } else {
                buttonPause.setDisabled(true);
                buttonStop.setDisabled(true);
                buttonResume.setDisabled(true);
            }
        } catch (Exception e) {
            currentExecution = new Execution();
            buttonStart.setDisabled(true);
            passwordLocked = false;
            staticTextDisplayBatches.setRendered(true);
            buttonPause.setDisabled(true);
            buttonStop.setDisabled(true);
            buttonResume.setDisabled(true);
            buttonDerivate.setDisabled(true);
        }
        try {
            String complet = ApplicationBean1.getExecutionIRepository().getObjectAttachment(currentExecution.getId(), UUID.fromString(getAttachmentComplete()));
            String launch = ApplicationBean1.getExecutionIRepository().getObjectAttachment(currentExecution.getId(), UUID.fromString(getAttachmentLaunch()));
            String progression = ApplicationBean1.getExecutionIRepository().getObjectAttachment(currentExecution.getId(), UUID.fromString(getAttachmentSubmissionProgress()));
            String performed = ApplicationBean1.getExecutionIRepository().getObjectAttachment(currentExecution.getId(), UUID.fromString(getAttachmentSubmissionPerformed()));
            if ((launch != null) && (complet != null) && (performed != null) && (progression != null)) {
                if (complet.equals("1")) {
                    String idSansTiret = currentExecution.getId().toString().replaceAll("-", "");
                    accessBatches = "openPopup('SelectBatch','Batch" + idSansTiret + "','?id=" + currentExecution.getId() + "','height=200, width=250')";
                    stats = new statisicProvider(null);
                    stats.setSubmissionPerformed(Integer.decode(performed));
                    stats.setSubmissionProgress(progression);
                    String attachment = ApplicationBean1.getExecutionIRepository().getObjectAttachment(currentExecution.getId(), UUID.fromString(getAttachmentEvents()));
                    if (attachment != null) {
                        ByteArrayInputStream ss = new ByteArrayInputStream(attachment.getBytes());
                        XMLDecoder d = new XMLDecoder(ss);
                        errors = (ExecutionEvent[]) d.readObject();
                    }
                    staticTextLaunch.setValue(launch);
                    staticTextStatus.setValue("Execution terminée");
                    buttonStart.setDisabled(true);
                    passwordLocked = false;
                    staticTextDisplayBatches.setRendered(true);
                    buttonPause.setDisabled(true);
                    buttonStop.setDisabled(true);
                    buttonResume.setDisabled(true);
                }
            }
        } catch (PObjectNotFoundException ex) {
        }
    }

    public String buttonDerivate_action() {
        currentExecution = ApplicationBean1.getExecutionIRepository().getObject(UUID.fromString((String) hiddenFieldUUID.getValue()));
        currentExecution.setId(UUID.randomUUID());
        ApplicationBean1.getExecutionIRepository().putObject(currentExecution, true);
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("Createexecution.jsp?id=" + currentExecution.getId());
        } catch (IOException ex) {
            Logger.getLogger(Viewbatch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * @return the lastError
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * @param lastError the lastError to set
     */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * @return the batchParams
     */
    public BatchParam[] getBatchParams() {
        return batchParams;
    }

    /**
     * @param batchParams the batchParams to set
     */
    public void setBatchParams(BatchParam[] batchParams) {
        this.batchParams = batchParams;
    }

    /**
     * @return the userParams
     */
    public UserParam[] getUserParams() {
        return userParams;
    }

    /**
     * @param userParams the userParams to set
     */
    public void setUserParams(UserParam[] userParams) {
        this.userParams = userParams;
    }

    /**
     * @return the executionProcessor
     */
    public ExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    /**
     * @param executionProcessor the executionProcessor to set
     */
    public void setExecutionProcessor(ExecutionProcessor executionProcessor) {
        this.executionProcessor = executionProcessor;
    }

    /**
     * @return the stats
     */
    public statisicProvider getStats() {
        return stats;
    }

    /**
     * @param stats the stats to set
     */
    public void setStats(statisicProvider stats) {
        this.stats = stats;
    }

    public static void launchExecution(Execution execution, String login, String pass) {
        ExecutionProcessor ep = new ExecutionProcessor(execution, login, pass);
        ep.startExecution(ApplicationBean1.getMapRepository());
        ApplicationBean1.setExecutionProcess(execution.getSelfReference(), ep);
        ep.registerEventListener(new ExecutionProcessor.EventListener() {

            public void notifyEvent(ExecutionProcessor executionProcessor, ExecutionEvent e) {
                ExecutionProcessor ep = ApplicationBean1.getExecutionProcess(e.getSource());
                if (ep.isFinished()) {
                    Execution exe = e.getSource().resolve(ApplicationBean1.getExecutionIRepository(), false, false);
                    try {
                        ApplicationBean1.getExecutionIRepository().putObjectAttachment(exe.getId(), UUID.fromString(getAttachmentComplete()), "1", false);
                        ApplicationBean1.getExecutionIRepository().putObjectAttachment(exe.getId(), UUID.fromString(getAttachmentSubmissionPerformed()), "" + ep.getCurrentSubmissionNumber(), false);
                        ApplicationBean1.getExecutionIRepository().putObjectAttachment(exe.getId(), UUID.fromString(getAttachmentSubmissionProgress()), ep.getCurrentSubmissionNumber() + " sur " + ep.getTotalSubmissionNumber(), false);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        XMLEncoder encoder = new XMLEncoder(outputStream);
                        encoder.setPersistenceDelegate(ExecutionEventLevel.class, new EnumPersistenceDelegate());
                        encoder.setPersistenceDelegate(UUID.class, new UUIDPersistenceDelegate());
                        encoder.setPersistenceDelegate(ExecutionEvent.class, new DefaultPersistenceDelegate(new String[] { "level", "date", "message", "source" }));
                        encoder.setPersistenceDelegate(PObjectReference.class, new DefaultPersistenceDelegate(new String[] { "referencedClass", "id" }));
                        encoder.setPersistenceDelegate(PSxSObjectReference.class, new DefaultPersistenceDelegate(new String[] { "referencedClass", "id", "version" }));
                        ExecutionEvent[] events = new ExecutionEvent[ep.getEvents().size()];
                        int i = 0;
                        for (ExecutionEvent ee : ep.getEvents()) {
                            events[i] = ee;
                            i++;
                        }
                        encoder.writeObject(events);
                        encoder.close();
                        ApplicationBean1.getExecutionIRepository().putObjectAttachment(exe.getId(), UUID.fromString(getAttachmentEvents()), outputStream.toString(), false);
                        outputStream = new ByteArrayOutputStream();
                        encoder = new XMLEncoder(outputStream);
                        encoder.setPersistenceDelegate(PObjectReference.class, new DefaultPersistenceDelegate(new String[] { "referencedClass", "id" }));
                        encoder.setPersistenceDelegate(PSxSObjectReference.class, new DefaultPersistenceDelegate(new String[] { "referencedClass", "id", "version" }));
                        encoder.writeObject(ep.getInputFiles());
                        encoder.close();
                        ApplicationBean1.getExecutionIRepository().putObjectAttachment(exe.getId(), UUID.fromString(getAttachmentBatches()), outputStream.toString(), false);
                        ApplicationBean1.deleteExecutionProcessor(e.getSource());
                    } catch (PObjectNotFoundException ex) {
                        Logger.getLogger(Viewexecution.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    public String buttonStart_action() {
        launchExecution(currentExecution, (String) textFieldSSHLogin.getValue(), (String) passwordField1.getPassword());
        try {
            ApplicationBean1.getExecutionIRepository().putObjectAttachment(currentExecution.getId(), UUID.fromString(getAttachmentLaunch()), "" + ApplicationBean1.getExecutionProcess(currentExecution.getSelfReference()).getStartTime(), false);
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            ApplicationBean1.getExecutionIRepository().putObjectAttachment(currentExecution.getId(), UUID.fromString(getAttachmentLaunchAsInt()), format.format(ApplicationBean1.getExecutionProcess(currentExecution.getSelfReference()).getStartTime()), false);
        } catch (PObjectNotFoundException ex) {
            Logger.getLogger(Viewexecution.class.getName()).log(Level.SEVERE, null, ex);
        }
        currentExecution.setLancee(true);
        envoyerMail(currentExecution.getMail(), currentExecution.getLabel(), currentExecution.getAuthor());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Viewexecution.class.getName()).log(Level.SEVERE, null, ex);
        }
        loadPage(currentExecution.getId().toString());
        return null;
    }

    private void envoyerMail(String message_dest, String label, String nom) {
        String message_objet = "Fin de l'execution " + label;
        String message_corps = "Bonjour,\n\n" + "L'exécution " + label + " créée par " + nom + " s'est terminée.\n\n" + "-----------------------------------------------------------------------------\n" + "Ce message a été généré automatiquement. Veuillez ne pas y répondre, merci.\n" + "La Team Pelias III";
        Message mesg;
        Properties props = new Properties();
        props.put("mail.smtp.host", "mailhost.insa-rennes.fr");
        Session session;
        session = Session.getDefaultInstance(props, null);
        session.setDebug(true);
        try {
            mesg = new MimeMessage(session);
            mesg.setFrom(new InternetAddress("pelias@insa-rennes.fr"));
            InternetAddress toAddress = new InternetAddress(message_dest);
            mesg.addRecipient(Message.RecipientType.TO, toAddress);
            mesg.setSubject(message_objet);
            mesg.setText(message_corps);
            Transport.send(mesg);
        } catch (MessagingException ex) {
            while ((ex = (MessagingException) ex.getNextException()) != null) {
                ex.printStackTrace();
            }
        }
    }

    static class EnumPersistenceDelegate extends PersistenceDelegate {

        protected boolean mutateTo(Object oldInstance, Object newInstance) {
            return oldInstance == newInstance;
        }

        protected Expression instantiate(Object oldInstance, Encoder en) {
            Enum e = (Enum) oldInstance;
            return new Expression(e, e.getClass(), "valueOf", new Object[] { e.name() });
        }
    }

    static class UUIDPersistenceDelegate extends PersistenceDelegate {

        protected boolean mutateTo(Object oldInstance, Object newInstance) {
            return oldInstance.equals(newInstance);
        }

        protected Expression instantiate(Object oldInstance, Encoder en) {
            UUID e = (UUID) oldInstance;
            return new Expression(e, e.getClass(), "fromString", new Object[] { e.toString() });
        }
    }

    public String buttonPause_action() {
        ApplicationBean1.pauseExecution(currentExecution.getSelfReference());
        loadPage(currentExecution.getId().toString());
        return null;
    }

    public String buttonStop_action() {
        ApplicationBean1.killExecution(currentExecution.getSelfReference());
        loadPage(currentExecution.getId().toString());
        return null;
    }

    /**
     * @return the outputPath
     */
    public FilesOutputs[] getOutputPath() {
        return outputPath;
    }

    /**
     * @param outputPath the outputPath to set
     */
    public void setOutputPath(FilesOutputs[] outputPath) {
        this.outputPath = outputPath;
    }

    public String buttonResume_action() {
        ApplicationBean1.resumeExecution(currentExecution.getSelfReference());
        loadPage(currentExecution.getId().toString());
        return null;
    }

    /**
     * @return the clusterName
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * @param clusterName the clusterName to set
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * @return the chainName
     */
    public String getChainName() {
        return chainName;
    }

    /**
     * @param chainName the chainName to set
     */
    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    /**
     * @return the progress
     */
    public String getProgress() {
        return progress;
    }

    /**
     * @param progress the progress to set
     */
    public void setProgress(String progress) {
        this.progress = progress;
    }

    /**
     * @return the errors
     */
    public ExecutionEvent[] getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(ExecutionEvent[] errors) {
        this.errors = errors;
    }

    /**
     * @return the accessBatches
     */
    public String getAccessBatches() {
        return accessBatches;
    }

    /**
     * @param accessBatches the accessBatches to set
     */
    public void setAccessBatches(String accessBatches) {
        this.accessBatches = accessBatches;
    }

    /**
     * @return the passwordLocked
     */
    public boolean isPasswordLocked() {
        return passwordLocked;
    }

    /**
     * @param passwordLocked the passwordLocked to set
     */
    public void setPasswordLocked(boolean passwordLocked) {
        this.passwordLocked = passwordLocked;
    }

    /**
     * @return the complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * @param complete the complete to set
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}

package org.hardtokenmgmt.ui;

import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.TokenException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.SwingUtilities;
import org.hardtokenmgmt.core.token.IToken;
import org.hardtokenmgmt.core.token.OperationNotSupportedException;
import org.hardtokenmgmt.core.token.SyncronizedToken;
import org.hardtokenmgmt.core.token.TokenManager;
import org.hardtokenmgmt.core.ui.BaseController;
import org.hardtokenmgmt.core.ui.BaseView;
import org.hardtokenmgmt.core.ui.UIHelper;
import org.hardtokenmgmt.core.util.CertUtils;

/**
 * 
 * Controller used to diplay and error and the possibility to
 * mail the error the designated administrator
 * 
 * <p>Controller Memory Settings:
 * <p>
 *  See separate CC constants.
 * 
 * @author Philip Vendil 2007 feb 16
 *
 * @version $Id$
 */
public class ErrorController extends BaseController {

    String callingController = null;

    String message = null;

    private boolean closeOnBack = false;

    /**
	 * Property defining the error message that should be used, must be a String,
	 * if not defined will the simple name of the exception be used.
	 */
    public static final String CCERRORMSG = "CCERRORMSG";

    /**
	 * Property defining the exception to report, this is a required setting for the
	 * controller to function properly. Must be a Throwable
	 */
    public static final String CCERROREXCEPTION = "CCERROREXCEPTION";

    /**
	 * Property defining the controller (string) that should be switched to when the back button
	 * is pressed, if property isn't set is the callingController used.
	 */
    public static final String CCBACKCONTROLLER = "CCBACKCONTROLLER";

    public ErrorController() {
        super(new ErrorView());
        init();
    }

    /**
	 * Alternative constructor.
	 * 
	 * @param alternateErrorView must implement the IErrorView interface.
	 * @param closeOnBack if the current view should be closed or not
	 */
    public ErrorController(BaseView alternateErrorView, boolean closeOnBack) {
        super(alternateErrorView);
        this.closeOnBack = closeOnBack;
        init();
    }

    private void init() {
        getErrorView().getBackButton().addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (closeOnBack) {
                    getErrorView().close();
                } else {
                    String backController = (String) getControllerMemory().getData(CCBACKCONTROLLER);
                    getControllerMemory().putData(CCBACKCONTROLLER, null);
                    if (backController == null) {
                        switchControlTo(callingController);
                    } else {
                        switchControlTo(backController);
                    }
                }
            }
        });
        getErrorView().getReportErrorButton().addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendMailAction();
            }
        });
    }

    private IErrorView getErrorView() {
        return (IErrorView) getView();
    }

    /**
	 * Method called by the Main applet when it's time for this
	 * controller to take control
	 * 
	 * @see org.hardtokenmgmt.core.ui.IController#getControl(String)
	 */
    public void getControl(String callingController) {
        this.callingController = callingController;
        Throwable exception = (Throwable) getControllerMemory().getData(CCERROREXCEPTION);
        String tmpErrorMsg = (String) getControllerMemory().getData(CCERRORMSG);
        if (tmpErrorMsg == null && exception != null) {
            if (exception.getClass().getSimpleName().equals("AuthorizationDeniedException_Exception")) {
                tmpErrorMsg = UIHelper.getText("error.revoked");
            } else {
                tmpErrorMsg = exception.getClass().getSimpleName();
            }
        }
        final String errorMsg = tmpErrorMsg;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                getErrorView().getErrorNameLabel().setText(errorMsg);
            }
        });
        String stackTraceString = "";
        if (exception != null) {
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            stackTraceString = sw.toString();
        }
        String adminSubjectDN = UIHelper.getText("error.noadminfound");
        if (getAdministratorSettings().getAdminCert() != null) {
            adminSubjectDN = CertUtils.getSubjectDN(getAdministratorSettings().getAdminCert());
        }
        String host = null;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            host = addr.getHostName();
        } catch (UnknownHostException e) {
            error("Error fetching hostname", e);
        }
        IToken processableToken = getProcessableToken();
        String cardReader = UIHelper.getText("error.notokeninserted");
        String tokenSN = UIHelper.getText("error.notokeninserted");
        String tokenImpl = UIHelper.getText("error.notokeninserted");
        if (processableToken != null) {
            if (processableToken instanceof SyncronizedToken) {
                tokenImpl = ((SyncronizedToken) processableToken).getWrappedToken().getClass().getSimpleName();
            }
            try {
                tokenSN = processableToken.getHardTokenSN();
                cardReader = getSlotDescription(processableToken);
            } catch (TokenException e) {
            } catch (OperationNotSupportedException e) {
            }
        }
        message = getControllerSetting("systemoperatormessage") + "\n\n " + UIHelper.getText("error.errormsg") + " " + errorMsg + "\n\n" + UIHelper.getText("error.administrator") + " " + adminSubjectDN + "\n" + UIHelper.getText("error.hostname") + " " + host + "\n\n" + UIHelper.getText("error.tokentype") + " " + tokenImpl + "\n" + UIHelper.getText("error.tokensn") + " " + tokenSN + "\n" + UIHelper.getText("error.cardreader") + " " + cardReader + "\n\n" + UIHelper.getText("error.stacktrace") + "\n" + stackTraceString;
        if (isSettingTrue("alwayssendreport")) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    getErrorView().getYouCanReportLabel().setText(UIHelper.getText("error.areportissent"));
                    getErrorView().getReportErrorButton().setVisible(false);
                }
            });
            sendMailAction();
        } else {
            if (!isSettingTrue("promptforreportsending")) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        getErrorView().getYouCanReportLabel().setVisible(false);
                        getErrorView().getReportErrorButton().setVisible(false);
                    }
                });
            }
        }
        getControllerMemory().putData(CCERROREXCEPTION, null);
        getControllerMemory().putData(CCERRORMSG, null);
    }

    /**
	 * Returns the slot description of the currently inserted card. 
	 * @param processableToken the current processableToken, cannot be null.
	 * @return the slot description of the currently inserted card.
	 */
    private String getSlotDescription(IToken processableToken) throws OperationNotSupportedException, TokenException {
        String retval = "";
        long slotId = processableToken.getSlotId(processableToken.getSupportedPINTypes()[0]);
        Collection<Slot> slots = getTokenManager().getSlots(TokenManager.SLOTTYPE_PROCESSABLECARDS);
        for (Slot slot : slots) {
            if (slot.getSlotID() == slotId) {
                if (slot.getSlotInfo() != null) {
                    retval = slot.getSlotInfo().getSlotDescription();
                }
            }
        }
        return retval;
    }

    /**
     * Method called by the main applet to check that
     * the administrator is authorized to this controller
     * 
     * @see org.hardtokenmgmt.core.ui.IController#isAuthorizedToController(X509Certificate)
     */
    public boolean isAuthorizedToController(X509Certificate admin) {
        return true;
    }

    private void sendMailAction() {
        try {
            getErrorView().getStatusLabel().setText(UIHelper.getText("error.sendingmail"));
            sendMail();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    getErrorView().getStatusLabel().setText(UIHelper.getText("error.reportsent"));
                    getErrorView().getReportErrorButton().setVisible(false);
                }
            });
        } catch (Exception e1) {
            error("Error sending error report", e1);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    getErrorView().getStatusLabel().setText(UIHelper.getText("error.errorsendingmail"));
                }
            });
        }
    }

    private void sendMail() throws Exception {
        String to = getControllerSetting("systemoperatormail").trim();
        String from = getControllerSetting("mailfromaddress");
        String host = getControllerSetting("smtphost");
        String filename = System.getProperty("user.home") + "/" + ".hardtokenmgmt0_0.log";
        String subject = getControllerSetting("systemoperatorsubject");
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        if (getControllerSetting("smtpusername") != null) {
            props.put("mail.smtp.user", getControllerSetting("smtpusername"));
            props.put("mail.smtp.auth", "true");
        }
        Session session = Session.getInstance(props, null);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        InternetAddress[] address = { new InternetAddress(to) };
        msg.setRecipients(Message.RecipientType.TO, address);
        msg.setSubject(subject);
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(message);
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);
        File attachFile = new File(filename);
        if (attachFile.exists()) {
            MimeBodyPart mbp2 = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(filename);
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setFileName(fds.getName());
            mp.addBodyPart(mbp2);
        }
        msg.setContent(mp);
        msg.setSentDate(new Date());
        if (getControllerSetting("smtpusername") == null) {
            Transport.send(msg);
        } else {
            Transport t = session.getTransport();
            try {
                t.connect(host, getControllerSetting("smtpusername"), getControllerSetting("smtppassword"));
                t.sendMessage(msg, msg.getAllRecipients());
            } finally {
                t.close();
            }
        }
    }

    /***
	 * Checks a specific conroller setting if it is set to TRUE
	 */
    private boolean isSettingTrue(String key) {
        if (getControllerSetting(key) == null) {
            return false;
        }
        return ((String) getControllerSetting(key)).trim().equalsIgnoreCase("true");
    }
}

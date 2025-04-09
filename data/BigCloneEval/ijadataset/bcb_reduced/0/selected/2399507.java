package org.powerfolder.workflow.model.script.v1.returnable;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import org.powerfolder.PFException;
import org.powerfolder.ValueAndClass;
import org.powerfolder.ValueAndClassFactory;
import org.powerfolder.utils.misc.MiscHelper;
import org.powerfolder.workflow.model.script.InitializeScriptTagContext;
import org.powerfolder.workflow.model.script.MultiStaticOrDynamicScriptTagConstraint;
import org.powerfolder.workflow.model.script.ReturnableScriptTag;
import org.powerfolder.workflow.model.script.ReturnClassContext;
import org.powerfolder.workflow.model.script.ReturnValueAndClassContext;
import org.powerfolder.workflow.model.script.StaticOrDynamicScriptTagConstraint;
import org.powerfolder.workflow.model.script.StudioScriptTagInstanceInformationContext;
import org.powerfolder.workflow.model.script.StudioScriptTagTypeInformationContext;
import org.powerfolder.workflow.model.script.ScriptTagCharacteristic;
import org.powerfolder.workflow.model.script.ScriptTagCharacteristicFactory;
import org.powerfolder.workflow.model.script.ScriptTagHelper;
import org.powerfolder.workflow.model.script.ScriptTagInformationContext;
import org.powerfolder.workflow.model.script.ScriptTagInitializer;
import org.powerfolder.workflow.model.script.ScriptTagInitializerFactory;
import org.powerfolder.workflow.model.script.WorkflowComponentsForReturnableScriptTag;

public class SendSimpleEmailWithAttachmentScriptTag implements ReturnableScriptTag {

    private ScriptTagCharacteristic toTc = null;

    private ScriptTagCharacteristic fromTc = null;

    private ScriptTagCharacteristic hostTc = null;

    private ScriptTagCharacteristic ccTc = null;

    private ScriptTagCharacteristic subjectTc = null;

    private ScriptTagCharacteristic messageTc = null;

    private ScriptTagCharacteristic attachmentTc = null;

    private static final String TO_TC = "to";

    private static final String TO_TITLE = "To";

    private static final String TO_SHORT_DESCRIPTION = "Comma seperated list of senders of the email.";

    private static final String FROM_TC = "from";

    private static final String FROM_TITLE = "From";

    private static final String FROM_SHORT_DESCRIPTION = "Comma seperated list of direct receivers of the email.";

    private static final String HOST_TC = "host";

    private static final String HOST_TITLE = "Host";

    private static final String HOST_SHORT_DESCRIPTION = "Internet or IP address of the SMTP server sending the email.";

    private static final String CC_TC = "cc";

    private static final String CC_TITLE = "CC";

    private static final String CC_SHORT_DESCRIPTION = "Comma seperated list of adjunct receivers of the email.";

    private static final String SUBJECT_TC = "subject";

    private static final String SUBJECT_TITLE = "Subject";

    private static final String SUBJECT_SHORT_DESCRIPTION = "Subject of the email.";

    private static final String MESSAGE_TC = "message";

    private static final String MESSAGE_TITLE = "Message";

    private static final String MESSAGE_SHORT_DESCRIPTION = "Text message that will accompany the email.";

    private static final String ATTACHMENT_TC = "attachment";

    private static final String ATTACHMENT_TITLE = "Attachments";

    private static final String ATTACHMENT_SHORT_DESCRIPTION = "Files attached to the message.";

    public SendSimpleEmailWithAttachmentScriptTag() {
        this.toTc = ScriptTagCharacteristicFactory.newInstance();
        this.fromTc = ScriptTagCharacteristicFactory.newInstance();
        this.hostTc = ScriptTagCharacteristicFactory.newInstance();
        this.ccTc = ScriptTagCharacteristicFactory.newInstance();
        this.subjectTc = ScriptTagCharacteristicFactory.newInstance();
        this.messageTc = ScriptTagCharacteristicFactory.newInstance();
        this.attachmentTc = ScriptTagCharacteristicFactory.newInstance();
    }

    public void initializeScriptTag(InitializeScriptTagContext inItc) {
        ScriptTagInitializer ti = ScriptTagInitializerFactory.newInstance(inItc);
        StaticOrDynamicScriptTagConstraint sodtc1 = StaticOrDynamicScriptTagConstraint.newInstance(this.TO_TC, this.toTc, ti);
        sodtc1.setTitle(this.TO_TITLE);
        sodtc1.setShortDescription(this.TO_SHORT_DESCRIPTION);
        sodtc1.setRegEx(MiscHelper.REG_EX_ANY_STRING);
        sodtc1.setReturnClassRestrictions(String.class);
        sodtc1.setDefaultValueAsString("you@company.com,your.boss@company.com");
        StaticOrDynamicScriptTagConstraint sodtc2 = StaticOrDynamicScriptTagConstraint.newInstance(this.FROM_TC, this.fromTc, ti);
        sodtc2.setTitle(this.FROM_TITLE);
        sodtc2.setShortDescription(this.FROM_SHORT_DESCRIPTION);
        sodtc2.setRegEx(MiscHelper.REG_EX_ANY_STRING);
        sodtc2.setReturnClassRestrictions(String.class);
        sodtc2.setDefaultValueAsString("coworker@company.com,your.boss@company.com");
        StaticOrDynamicScriptTagConstraint sodtc3 = StaticOrDynamicScriptTagConstraint.newInstance(this.CC_TC, this.ccTc, ti);
        sodtc3.setTitle(this.CC_TITLE);
        sodtc3.setShortDescription(this.CC_SHORT_DESCRIPTION);
        sodtc3.setRegEx(MiscHelper.REG_EX_ANY_STRING);
        sodtc3.setReturnClassRestrictions(String.class);
        sodtc3.setDefaultValueAsString("your.department@company.com");
        StaticOrDynamicScriptTagConstraint sodtc4 = StaticOrDynamicScriptTagConstraint.newInstance(this.SUBJECT_TC, this.subjectTc, ti);
        sodtc4.setTitle(this.SUBJECT_TITLE);
        sodtc4.setShortDescription(this.SUBJECT_SHORT_DESCRIPTION);
        sodtc4.setRegEx(MiscHelper.REG_EX_ANY_STRING);
        sodtc4.setReturnClassRestrictions(String.class);
        sodtc4.setDefaultValueAsString("SubjectHere");
        StaticOrDynamicScriptTagConstraint sodtc5 = StaticOrDynamicScriptTagConstraint.newInstance(this.HOST_TC, this.hostTc, ti);
        sodtc5.setTitle(this.HOST_TITLE);
        sodtc5.setShortDescription(this.HOST_SHORT_DESCRIPTION);
        sodtc5.setRegEx(MiscHelper.REG_EX_ANY_STRING);
        sodtc5.setReturnClassRestrictions(String.class);
        sodtc5.setDefaultValueAsString("127.0.0.1");
        MultiStaticOrDynamicScriptTagConstraint msodtc1 = MultiStaticOrDynamicScriptTagConstraint.newInstance(this.MESSAGE_TC, this.messageTc, ti);
        msodtc1.setDefaultLength(1);
        msodtc1.setMinimumLength(0);
        msodtc1.setLengthUnbounded(true);
        msodtc1.setTitle(this.MESSAGE_TITLE);
        msodtc1.setShortDescription(this.MESSAGE_SHORT_DESCRIPTION);
        msodtc1.setRegEx(MiscHelper.REG_EX_ANY_STRING);
        msodtc1.setReturnClassRestrictions(String.class);
        msodtc1.setDefaultValueAsString("MessageTextHere");
        msodtc1.setMultiLine(true);
        MultiStaticOrDynamicScriptTagConstraint msodtc2 = MultiStaticOrDynamicScriptTagConstraint.newInstance(this.ATTACHMENT_TC, this.attachmentTc, ti);
        msodtc2.setDefaultLength(0);
        msodtc2.setMinimumLength(0);
        msodtc2.setLengthUnbounded(true);
        msodtc2.setTitle(this.ATTACHMENT_TITLE);
        msodtc2.setShortDescription(this.ATTACHMENT_SHORT_DESCRIPTION);
        msodtc2.setRegEx(MiscHelper.REG_EX_ANY_STRING);
        msodtc2.setReturnClassRestrictions(String.class);
        msodtc2.setDefaultValueAsString("FullPathAndFileNameOfAttachment");
        ti.initialize();
    }

    public void getScriptTagInformation(ScriptTagInformationContext inTic) {
        if (inTic instanceof StudioScriptTagInstanceInformationContext) {
            StudioScriptTagInstanceInformationContext stiic = (StudioScriptTagInstanceInformationContext) inTic;
            stiic.setScriptTagInstanceTitle("Send Simple Email With Attachment");
            stiic.setScriptTagInstanceDescription("Sends an email with an attachment.");
        } else if (inTic instanceof StudioScriptTagTypeInformationContext) {
            StudioScriptTagTypeInformationContext sttic = (StudioScriptTagTypeInformationContext) inTic;
            sttic.setScriptTagTypeTitle("Send Simple Email With Attachment");
            sttic.setScriptTagTypeDescription("Sends an email with an attachment.");
        }
    }

    public ValueAndClass returnValueAndClass(ReturnValueAndClassContext inRvacc) throws PFException {
        WorkflowComponentsForReturnableScriptTag wcfrt = ScriptTagHelper.createWorkflowComponentsForReturnableScriptTag(inRvacc);
        try {
            InternetAddress to[] = InternetAddress.parse(this.toTc.getValueAsString(), false);
            InternetAddress cc[] = InternetAddress.parse(this.ccTc.getValueAsString(), false);
            InternetAddress from[] = InternetAddress.parse(this.fromTc.getValueAsString(), false);
            Properties props = new Properties();
            props.put("mail.smtp.host", this.hostTc.getValueAsString());
            Session session = Session.getInstance(props, null);
            MimeMessage message = new MimeMessage(session);
            message.setSentDate(new Date());
            message.setSubject(this.subjectTc.getValueAsString());
            message.setRecipients(RecipientType.TO, to);
            message.setRecipients(RecipientType.CC, cc);
            message.addFrom(from);
            String messageText = "";
            for (int i = 0; i < this.messageTc.getValueLength(); i++) {
                messageText = messageText + this.messageTc.getValueAsString(i);
            }
            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(messageText);
            multipart.addBodyPart(messageBodyPart);
            for (int i = 0; i < this.attachmentTc.getValueLength(); i++) {
                String fileName = this.attachmentTc.getValueAsString(i);
                File file = new File(fileName);
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(fileName);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(file.getName());
                multipart.addBodyPart(messageBodyPart);
            }
            message.setContent(multipart);
            Transport.send(message);
            return ValueAndClassFactory.newValueAndClass(null, Void.TYPE);
        } catch (AddressException ae) {
            throw new PFException(ae);
        } catch (MessagingException me) {
            throw new PFException(me);
        }
    }

    public Class getReturnClass(ReturnClassContext inRcc) {
        return Void.TYPE;
    }
}

package angop.mail;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMail {

    private static final Logger log = LoggerFactory.getLogger(XMail.class);

    private Collection emails = new ArrayList();

    public Message.RecipientType recipientType = Message.RecipientType.TO;

    private String subject = null;

    private String message = null;

    private String sender = null;

    private String SMTPHost = null;

    private String template = null;

    Map content = new HashMap();

    public Collection getEmails() {
        return emails;
    }

    public String getMessage() {
        return message;
    }

    public Message.RecipientType getRecipientType() {
        return recipientType;
    }

    public String getSender() {
        return sender;
    }

    public String getSMTPHost() {
        return SMTPHost;
    }

    public String getSubject() {
        return subject;
    }

    /**
	 * Adiciona um e-mail ou v�rios e-mails separados por virgula.
	 * 
	 * @param email
	 *            String
	 */
    public void setEmails(String emails) {
        StringTokenizer tokenAction = new StringTokenizer(emails, ",", false);
        while (tokenAction.hasMoreTokens()) {
            String token = tokenAction.nextToken();
            this.emails.add(token);
        }
    }

    /**
	 * Define o tipo de destinat�rio
	 * 
	 * @param recipientType
	 *            RecipientType Utlizar os t�pos: Message.RecipientType.BCC,
	 *            Message.RecipientType.CC, Message.RecipientType.TO
	 */
    public void setRecipientType(Message.RecipientType recipientType) {
        this.recipientType = recipientType;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSMTPHost(String SMTPHost) {
        this.SMTPHost = SMTPHost;
    }

    /**
	 * Cria o o corpo da mensagem a partir de um template XSL
	 */
    public String setMessage() {
        log.debug("TEMPLATE: " + this.template);
        String xslFile = this.template;
        log.debug("CONTENT: " + this.content);
        Map fields = this.content;
        String result = "";
        Document XMLdoc = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            docBuilder = docBuilderFactory.newDocumentBuilder();
            XMLdoc = docBuilder.newDocument();
            Element eleMessage = XMLdoc.createElement("message");
            if (fields != null) {
                for (Iterator iter = fields.keySet().iterator(); iter.hasNext(); ) {
                    String field = (String) iter.next();
                    if (fields.get(field) != null) {
                        String value = fields.get(field).toString();
                        Element eleField = XMLdoc.createElement(field);
                        eleField.appendChild(XMLdoc.createCDATASection(value));
                        eleMessage.appendChild(eleField);
                    }
                }
            }
            XMLdoc.appendChild(eleMessage);
            TransformerFactory factory = TransformerFactory.newInstance();
            File file = new File(xslFile);
            Transformer XSLDoc = factory.newTransformer(new StreamSource(file));
            XSLDoc.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            StringWriter sw = new StringWriter();
            StreamResult resultsw = new StreamResult(sw);
            XSLDoc.transform(new DOMSource(XMLdoc), resultsw);
            resultsw.setWriter(sw);
            result = sw.toString();
        } catch (TransformerException ex) {
            log.error("Ocorreu uma exce��o de transforma��o", ex);
        } catch (ParserConfigurationException ex) {
            log.error("Ocorreu uma exce��o de parsing", ex);
        } catch (TransformerFactoryConfigurationError ex) {
            log.error("Ocorreu uma exce��o de configura��o.", ex);
        } catch (Exception ex) {
            log.error("Ocorreu uma exce��o inesperada.", ex);
        }
        return result;
    }

    /**
	 * Envia o e-mail para os destinat�rios. O e-mail � enviado no formato HTML.
	 */
    public void send() throws Exception {
        try {
            log.debug("#################################################");
            this.message = this.setMessage();
            Collection cEmails = new ArrayList();
            log.debug("TAM EMAILS: " + this.emails.size());
            if (this.emails.size() > 0) {
                Iterator iMails = this.emails.iterator();
                while (iMails.hasNext()) {
                    String email = (String) iMails.next();
                    log.debug("EMAIL: " + email);
                    if (!cEmails.contains(email)) {
                        cEmails.add(email);
                    }
                }
            }
            log.debug("TAM CEMAIL: " + cEmails.size());
            if (cEmails.size() > 0) {
                InternetAddress toAddress[] = new InternetAddress[cEmails.size()];
                String emails = "";
                Iterator iEmails = cEmails.iterator();
                int i = 0;
                while (iEmails.hasNext()) {
                    String sEmail = (String) iEmails.next();
                    try {
                        log.debug("SEMAIL: " + sEmail);
                        toAddress[i] = new InternetAddress(sEmail);
                        emails += "[" + sEmail + "]";
                        i++;
                    } catch (AddressException ex) {
                        log.warn("O e-mail [" + sEmail + "] n�o � v�lido.");
                    }
                }
                log.debug("TAM TOADRESS: " + toAddress.length);
                for (int j = 0; toAddress.length > j; j++) {
                    log.debug("Envio de e-mail para [" + cEmails.size() + "] usu�rios. " + emails);
                    InternetAddress fromAddress = new InternetAddress(this.sender);
                    Properties props = new Properties();
                    log.debug("SMTP: " + this.SMTPHost);
                    props.put("mail.smtp.host", this.SMTPHost);
                    MimeMessage email = new MimeMessage(Session.getInstance(props));
                    log.debug("EMAIL: " + email);
                    log.debug("FROMADRESS: " + fromAddress);
                    email.setFrom(fromAddress);
                    log.debug("j " + j);
                    log.debug("Adress: " + toAddress[j]);
                    email.setRecipient(this.recipientType, toAddress[j]);
                    log.debug("SUBJECT: " + this.subject);
                    email.setSubject(this.subject);
                    log.debug("MESSAGE: " + this.message);
                    email.setContent(this.message, "text/html");
                    Transport.send(email);
                }
            }
        } catch (MessagingException ex1) {
            log.error("Falha inesperada ao tentar enviar e-mail [" + this.subject + "]: " + ex1.getNextException(), ex1);
            log.error("Falha inesperada: " + ex1.getNextException().getMessage(), ex1.getNextException());
        }
        log.debug("#################################################");
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Map getContent() {
        return content;
    }

    public void setContent(Map content) {
        this.content = content;
    }
}

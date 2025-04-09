package mailto.sender;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import mailto.xdaplugin.XdaPluginDialog;

public class MailSender {

    private static InputStream JIRA_USERS = MailSender.class.getResourceAsStream("/mailto/sender/resource/jira_users.xml");

    private static final String MAIL_SMTP_HOST_PROP = "mail.smtp.host";

    private static final String TO_TAG = "@TO";

    private static final String SUBJECT_TAG = "@SUBJECT";

    private static final String BODY_TAG = "@BODY";

    private static final String END_BODY_TAG = "@END_BODY";

    private static final String CC_TAG = "@CC";

    private static final String TEST_TAG = "@TEST";

    private static final String TEST_MAIL_ACCOUNT = "randres@sciops.esa.int";

    private static final String JIRA_TO_TAG = "@JTO";

    private static final String LDAP_TO_TAG = "@LDAPTO";

    private Session session;

    private static HashMap<String, String> jiraUsers = null;

    public MailSender() {
        if (jiraUsers == null) {
            loadJiraUsers();
        }
    }

    private void loadJiraUsers() {
        jiraUsers = new HashMap<String, String>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document xmlDoc = factory.newDocumentBuilder().parse(JIRA_USERS);
            XPath xpath = XPathFactory.newInstance().newXPath();
            String expression = "/users/user";
            NodeList nodes = (NodeList) xpath.evaluate(expression, xmlDoc.getDocumentElement(), XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node item = nodes.item(i);
                NamedNodeMap atts = item.getAttributes();
                jiraUsers.put(atts.getNamedItem("username").getNodeValue(), atts.getNamedItem("email").getNodeValue());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void setupSession(String server) {
        Properties props = new Properties();
        props.put(MAIL_SMTP_HOST_PROP, server);
        session = Session.getDefaultInstance(props, null);
    }

    private void sendMail(String subject, String message, List<String> to, List<String> cc) throws AddressException, MessagingException {
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(TEST_MAIL_ACCOUNT));
        for (String toAddress : to) {
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
        }
        for (String ccAddress : cc) {
            msg.addRecipient(Message.RecipientType.CC, new InternetAddress(ccAddress));
        }
        msg.setSubject(subject);
        msg.setText(message);
        Transport.send(msg);
    }

    public void send(String m_content, String server) {
        setupSession(server);
        sendContent(m_content);
    }

    private void sendContent(String m_content) {
        try {
            LineNumberReader reader = new LineNumberReader(new StringReader(m_content));
            String line = null;
            Vector<String> to = new Vector<String>();
            Vector<String> cc = new Vector<String>();
            String subject = null;
            StringBuffer body = new StringBuffer();
            boolean insideBody = false;
            String test = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(TO_TAG)) {
                    to.add(line.substring(TO_TAG.length()).trim());
                } else if (line.startsWith(JIRA_TO_TAG)) {
                    String mail = jiraUsers.get(line.substring(JIRA_TO_TAG.length()).trim());
                    if (mail != null) {
                        to.add(mail);
                    }
                } else if (line.startsWith(LDAP_TO_TAG)) {
                    String ldapUser = line.substring(LDAP_TO_TAG.length()).trim();
                    String mail = jiraUsers.get(ldapUser);
                    if (mail != null) {
                        to.add(mail);
                    }
                } else if (line.startsWith(CC_TAG)) {
                    cc.add(line.substring(CC_TAG.length()).trim());
                } else if (line.startsWith(SUBJECT_TAG)) {
                    subject = line.substring(SUBJECT_TAG.length());
                } else if (line.startsWith(BODY_TAG)) {
                    insideBody = true;
                } else if (line.startsWith(TEST_TAG)) {
                    test = line.substring(TEST_TAG.length()).trim();
                } else if (line.startsWith(END_BODY_TAG)) {
                    if (subject == null) {
                        subject = "[Empty Subject]";
                    }
                    if (test != null) {
                        cc = new Vector<String>();
                        to = new Vector<String>();
                        if (test.length() == 0) {
                            test = TEST_MAIL_ACCOUNT;
                        }
                        to.add(test);
                    }
                    if (to.size() > 0 && body.length() > 0) {
                        sendMail(subject, body.toString(), to, cc);
                    } else {
                        StringBuffer recipients = new StringBuffer();
                        for (String recipient : to) {
                            recipients.append(recipient + " ");
                        }
                        System.out.println(String.format("Cannot send '%s' to %s ", subject, recipients.toString()));
                    }
                    to = new Vector<String>();
                    cc = new Vector<String>();
                    subject = null;
                    body = new StringBuffer();
                    insideBody = false;
                    test = null;
                } else if (insideBody) {
                    body.append(line + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

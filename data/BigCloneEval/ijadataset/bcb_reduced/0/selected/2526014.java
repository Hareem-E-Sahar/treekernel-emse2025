package org.grassfield.common.service;

import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.log4j.Logger;

/**
 * The Class GenericMailerService.
 */
public class GenericMailerService implements IMailerService {

    Logger logger = Logger.getLogger(GenericMailerService.class);

    private String to;

    private String cc;

    private String bcc;

    private String from;

    private String host;

    private Properties props;

    private String title;

    private String message;

    private String footer;

    private DataSource dataSource;

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String getCc() {
        return cc;
    }

    @Override
    public void setCc(String cc) {
        this.cc = cc;
    }

    @Override
    public String getBcc() {
        return bcc;
    }

    @Override
    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Properties getProps() {
        return props;
    }

    @Override
    public void setProps(Properties props) {
        this.props = props;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    /**
	 * Gets the footer.
	 * 
	 * @return the footer
	 */
    public String getFooter() {
        return footer;
    }

    /**
	 * Sets the footer.
	 * 
	 * @param footer
	 *            the new footer
	 */
    public void setFooter(String footer) {
        this.footer = footer;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void sendMail() {
        try {
            Session session = Session.getInstance(props);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] addresses = getAddresses(this.to);
            msg.setRecipients(Message.RecipientType.TO, addresses);
            InternetAddress[] ccAddresses = getAddresses(this.cc);
            msg.setRecipients(Message.RecipientType.CC, ccAddresses);
            InternetAddress[] bccAddresses = getAddresses(this.bcc);
            msg.setRecipients(Message.RecipientType.BCC, bccAddresses);
            msg.setSubject(this.getTitle());
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(this.getMessage() + this.getFooter(), "text/html");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            if (this.getDataSource() != null) {
                bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(this.getDataSource()));
                bodyPart.setFileName(this.getDataSource().getName());
                multipart.addBodyPart(bodyPart);
            }
            msg.setContent(multipart);
            Transport.send(msg);
        } catch (AddressException e) {
            logger.error("Address is wrong", e);
        } catch (MessagingException e) {
            logger.error("Error while sending the mail", e);
        }
    }

    /**
	 * Gets the addresses.
	 * 
	 * @param csv
	 *            the csv
	 * @return the addresses
	 * @throws AddressException
	 *             the address exception
	 */
    private InternetAddress[] getAddresses(String csv) throws AddressException {
        if (csv == null) return new InternetAddress[] {};
        String[] tos = csv.split(",");
        InternetAddress[] addresses = stringAryToInetAddressAry(tos);
        return addresses;
    }

    /**
	 * String ary to inet address ary.
	 * 
	 * @param tos
	 *            the tos
	 * @return the internet address[]
	 * @throws AddressException
	 *             the address exception
	 */
    private InternetAddress[] stringAryToInetAddressAry(String[] tos) throws AddressException {
        InternetAddress[] addresses = new InternetAddress[tos.length];
        int i = 0;
        for (String add : tos) {
            addresses[i] = new InternetAddress(add);
            i++;
        }
        return addresses;
    }
}

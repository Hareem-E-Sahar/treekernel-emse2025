package com.volantis.devrep.repository.api.devices.unknowndevices;

import com.volantis.devrep.repository.api.devices.logging.UnknownDevicesLogger;
import com.volantis.devrep.repository.api.devices.logging.Entry;
import com.volantis.devrep.repository.api.devices.logging.HeadersEntry;
import com.volantis.devrep.repository.api.devices.logging.Header;
import com.volantis.devrep.repository.api.devices.logging.EntryIterator;
import com.volantis.devrep.localization.LocalizationFactory;
import com.volantis.xml.xml.serialize.OutputFormat;
import com.volantis.xml.xml.serialize.XMLSerializer;
import com.volantis.synergetics.log.LogDispatcher;
import com.volantis.synergetics.localization.ExceptionLocalizer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * E-mail notifier thread that periodically converts and sends log entries about
 * unknown/abstract devices.
 */
public class EmailNotifierThread extends Thread {

    /**
     * Used for logging
     */
    private static final LogDispatcher LOGGER = LocalizationFactory.createLogger(EmailNotifierThread.class);

    /**
     * Used to retrieve localized exception messages.
     */
    private static final ExceptionLocalizer EXCEPTION_LOCALIZER = LocalizationFactory.createExceptionLocalizer(EmailNotifierThread.class);

    /**
     * The format for the output XML.
     */
    private static final OutputFormat OUTPUT_FORMAT = new OutputFormat("xml", "UTF-8", true);

    private static final String NAMESPACE_UNKNOWN_DEVICES = "http://www.volantis.com/xmlns/2006/07/mcs/unknown-devices";

    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

    private static final String ELEMENT_NAME_ENTRIES = "entries";

    private static final String ELEMENT_NAME_ENTRY = "entry";

    private static final String ELEMENT_NAME_RESOLVED_NAME = "resolved-name";

    private static final String ELEMENT_NAME_DEVICE_TYPE = "device-type";

    private static final String ELEMENT_NAME_HEADERS = "headers";

    private static final String ELEMENT_NAME_HEADER = "header";

    private static final String ELEMENT_NAME_HEADER_NAME = "name";

    private static final String ELEMENT_NAME_HEADER_VALUE = "value";

    /**
     * The configuration for sending e-mails.
     */
    private final EmailNotifierConfig config;

    /**
     * The date and time for the next scheduled e-mail sending event.
     */
    private Calendar next;

    /**
     * The logger to access the log entries.
     */
    private final UnknownDevicesLogger logger;

    /**
     * Creates an e-mail sending thread.
     *
     * @param config the configuration object
     * @param logger the logger to access the log entries
     */
    public EmailNotifierThread(final EmailNotifierConfig config, final UnknownDevicesLogger logger) {
        this.config = config;
        this.logger = logger;
        readNextTime();
        checkTime();
    }

    /**
     * Checks if it is time to send the e-mail.
     */
    private void checkTime() {
        if (next.before(new GregorianCalendar())) {
            sendEmail();
        }
    }

    /**
     * Send the e-mail with the available log entries.
     */
    private void sendEmail() {
        final File tempFile;
        try {
            tempFile = File.createTempFile("email_notifier", "tmp");
        } catch (IOException e) {
            throw new EmailNotifierException(EXCEPTION_LOCALIZER.format("cannot-create-temp-file"), e);
        }
        final int count = convertLogToXML(tempFile);
        if (count > 0) {
            final Message message = createEmail(tempFile);
            sendEmail(message);
        }
        try {
            logger.deleteEntries(count, new GregorianCalendar());
        } catch (IOException e) {
            throw new EmailNotifierException(EXCEPTION_LOCALIZER.format("cannot-delete-log-entries"), e);
        }
        tempFile.delete();
        updateNextTime();
    }

    /**
     * Sends the specified e-mail message.
     * @param message the message to send
     * @return true if the e-mail was sent successfully
     */
    private boolean sendEmail(final Message message) {
        boolean succeeded = false;
        for (int i = 0; i < 3 && !succeeded; i++) {
            try {
                Transport.send(message);
                succeeded = true;
            } catch (MessagingException e) {
                LOGGER.error(EXCEPTION_LOCALIZER.format("unable-to-send-email"), e);
            }
        }
        return succeeded;
    }

    /**
     * Creates an e-mail message adding the specified file as attachment
     * @param file the file to be sent
     * @return the created e-mail message
     */
    private Message createEmail(final File file) {
        try {
            final Properties props = new Properties();
            props.put("mail.smtp.host", config.getSmtpHost());
            props.put("mail.smtp.port", Integer.toString(config.getSmtpPort()));
            final Session session = Session.getDefaultInstance(props, new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getSmtpUserName(), config.getSmtpPassword());
                }
            });
            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getFromAddress(), config.getFromName()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(config.getToAddress(), config.getToName()));
            message.setSubject(config.getSubject());
            message.setSentDate(new Date());
            final MimeMultipart multipart = new MimeMultipart();
            message.setContent(multipart);
            final BodyPart attachment = new MimeBodyPart();
            attachment.setFileName("unknown-devices.zip");
            final DataSource ds = new URLDataSource(file.toURL());
            attachment.setDataHandler(new DataHandler(ds));
            attachment.setHeader("Content-ID", "");
            multipart.addBodyPart(attachment);
            return message;
        } catch (Exception e) {
            LOGGER.warn("unable-to-create-email", e);
            return null;
        }
    }

    /**
     * Converts the available log entries to XML. The XML is stored in the
     * specified file.
     *
     * @param file the destination of the created XML
     * @return the number of log entries processed
     */
    private int convertLogToXML(final File file) {
        final EntryIterator iter = logger.getEntries();
        int entryCount = 0;
        if (iter.hasNext()) {
            ZipOutputStream os = null;
            try {
                os = new ZipOutputStream(new FileOutputStream(file));
                os.setLevel(9);
                os.putNextEntry(new ZipEntry("logs.xml"));
                final XMLSerializer serializer = new XMLSerializer(os, OUTPUT_FORMAT);
                serializer.startDocument();
                serializer.setNamespaces(true);
                serializer.startPrefixMapping("", NAMESPACE_UNKNOWN_DEVICES);
                serializer.startElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_ENTRIES, ELEMENT_NAME_ENTRIES, EMPTY_ATTRIBUTES);
                while (iter.hasNext()) {
                    final Entry entry = (Entry) iter.next();
                    serializer.startElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_ENTRY, ELEMENT_NAME_ENTRY, EMPTY_ATTRIBUTES);
                    final String name = entry.getResolvedName();
                    serializer.startElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_RESOLVED_NAME, ELEMENT_NAME_RESOLVED_NAME, EMPTY_ATTRIBUTES);
                    serializer.characters(name.toCharArray(), 0, name.length());
                    serializer.endElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_RESOLVED_NAME, ELEMENT_NAME_RESOLVED_NAME);
                    final String type = entry.getDeviceType();
                    serializer.startElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_DEVICE_TYPE, ELEMENT_NAME_DEVICE_TYPE, EMPTY_ATTRIBUTES);
                    serializer.characters(type.toCharArray(), 0, type.length());
                    serializer.endElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_DEVICE_TYPE, ELEMENT_NAME_DEVICE_TYPE);
                    if (entry instanceof HeadersEntry) {
                        serializeHeadersEntry(serializer, (HeadersEntry) entry);
                    } else {
                        serializeSimpleEntry(serializer, entry);
                    }
                    serializer.endElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_ENTRY, ELEMENT_NAME_ENTRY);
                    entryCount++;
                }
                serializer.endElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_ENTRIES, ELEMENT_NAME_ENTRIES);
                serializer.endPrefixMapping("");
                serializer.endDocument();
            } catch (Exception e) {
                throw new EmailNotifierException(EXCEPTION_LOCALIZER.format("cannot-convert-log-entries"), e);
            } finally {
                iter.close();
                if (os != null) {
                    try {
                        os.closeEntry();
                        os.close();
                    } catch (IOException e) {
                        throw new EmailNotifierException(EXCEPTION_LOCALIZER.format("cannot-close-output-stream"), e);
                    }
                }
            }
        } else {
            iter.close();
        }
        return entryCount;
    }

    /**
     * Serializes out a single simple (as opposed to headers) entry.
     *
     * @param serializer the serializer to be used
     * @param entry the entry to be serialized
     * @throws SAXException if an error occurrs during serialization
     */
    private void serializeSimpleEntry(final XMLSerializer serializer, final Entry entry) throws SAXException {
        final String query = entry.getQuery();
        final String value = entry.getValue();
        serializer.startElement(NAMESPACE_UNKNOWN_DEVICES, query, query, EMPTY_ATTRIBUTES);
        serializer.characters(value.toCharArray(), 0, value.length());
        serializer.endElement(NAMESPACE_UNKNOWN_DEVICES, query, query);
    }

    /**
     * Serializes out a single headers entry.
     *
     * @param serializer the serializer to be used
     * @param entry the entry to be serialized
     * @throws SAXException if an error occurrs during serialization
     */
    private void serializeHeadersEntry(final XMLSerializer serializer, final HeadersEntry entry) throws SAXException {
        serializer.startElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_HEADERS, ELEMENT_NAME_HEADERS, EMPTY_ATTRIBUTES);
        for (Iterator iter = entry.getHeaders(); iter.hasNext(); ) {
            final Header header = (Header) iter.next();
            serializer.startElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_HEADER, ELEMENT_NAME_HEADER, EMPTY_ATTRIBUTES);
            serializer.startElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_HEADER_NAME, ELEMENT_NAME_HEADER_NAME, EMPTY_ATTRIBUTES);
            final String name = header.getName();
            serializer.characters(name.toCharArray(), 0, name.length());
            serializer.endElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_HEADER_NAME, ELEMENT_NAME_HEADER_NAME);
            serializer.startElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_HEADER_VALUE, ELEMENT_NAME_HEADER_VALUE, EMPTY_ATTRIBUTES);
            final String value = header.getValue();
            serializer.characters(value.toCharArray(), 0, value.length());
            serializer.endElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_HEADER_VALUE, ELEMENT_NAME_HEADER_VALUE);
            serializer.endElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_HEADER, ELEMENT_NAME_HEADER);
        }
        serializer.endElement(NAMESPACE_UNKNOWN_DEVICES, ELEMENT_NAME_HEADERS, ELEMENT_NAME_HEADERS);
    }

    /**
     * Reads the timestamp
     */
    private void readNextTime() {
        try {
            next = logger.getTimestamp();
        } catch (IOException e) {
            throw new EmailNotifierException(EXCEPTION_LOCALIZER.format("error-reading-timestamp"), e);
        }
    }

    /**
     * Updates the time of the next e-mail sending event.
     */
    private void updateNextTime() {
        next = new GregorianCalendar();
        config.getPeriod().addTo(next);
    }

    public void run() {
        while (true) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
            }
            checkTime();
        }
    }
}

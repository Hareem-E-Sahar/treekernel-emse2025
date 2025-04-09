package de.mogwai.common.business.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import de.mogwai.common.business.service.EMailException;
import de.mogwai.common.business.service.MailAttachment;
import de.mogwai.common.business.service.PGPKeyInfo;
import de.mogwai.common.business.service.PGPMailService;
import de.mogwai.common.utils.GenericComparator;
import de.mogwai.common.utils.StreamPrinterThread;

/**
 * Implementierung des GPG Mail Services.
 * 
 * @author $Author: mirkosertic $
 * @version $Date: 2008-06-17 14:26:35 $
 */
public class GPGMailServiceImpl extends MailServiceImpl implements PGPMailService {

    protected static final int RETURN_OK = 0;

    protected static final String LINE_SEPARATOR = "\n";

    private String gpgPath;

    private String passphrase;

    /**
     * Gibt den Wert des Attributs <code>gpgPath</code> zur�ck.
     * 
     * @return Wert des Attributs gpgPath.
     */
    public String getGpgPath() {
        return gpgPath;
    }

    /**
     * Setzt den Wert des Attributs <code>gpgPath</code>.
     * 
     * @param gpgPath
     *            Wert f�r das Attribut gpgPath.
     */
    public void setGpgPath(String gpgPath) {
        this.gpgPath = gpgPath;
    }

    /**
     * Gibt den Wert des Attributs <code>passphrase</code> zur�ck.
     * 
     * @return Wert des Attributs passphrase.
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Setzt den Wert des Attributs <code>passphrase</code>.
     * 
     * @param passphrase
     *            Wert f�r das Attribut passphrase.
     */
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * Ermittlung der Passphrase f�r einen Benutzer.
     * 
     * @param aUID
     *            die UID
     * @return die Passphrase
     */
    public String getPassphraseFor(String aUID) {
        return passphrase;
    }

    protected File pgpCrypt(byte[] aData, String[] aRecipients, boolean aUseASCII, boolean aSign, String aUserID) throws IOException, MessagingException, InterruptedException, EMailException {
        File theTempFile = File.createTempFile("AMIS", ".tmp");
        try {
            FileOutputStream theStream = new FileOutputStream(theTempFile);
            theStream.write(aData);
            theStream.close();
            return pgpCrypt(theTempFile, aRecipients, aUseASCII, aSign, aUserID);
        } finally {
            theTempFile.delete();
        }
    }

    protected File pgpCrypt(BodyPart aPart, String[] aRecipients, boolean aUseASCII, boolean aSign, String aUserID) throws IOException, MessagingException, InterruptedException, EMailException {
        File theTempFile = File.createTempFile("AMIS", ".tmp");
        try {
            FileOutputStream theStream = new FileOutputStream(theTempFile);
            aPart.writeTo(theStream);
            theStream.close();
            return pgpCrypt(theTempFile, aRecipients, aUseASCII, aSign, aUserID);
        } finally {
            theTempFile.delete();
        }
    }

    protected File pgpCrypt(File aInputFile, String[] aRecipients, boolean aUseASCII, boolean aSign, String aUserID) throws IOException, MessagingException, InterruptedException, EMailException {
        String thePassphrase = null;
        File theDestinationFile = File.createTempFile("AMIS", ".tmp");
        theDestinationFile.createNewFile();
        String theParams = " --yes";
        theParams += " --batch";
        theParams += " --verbose";
        if (aUseASCII) {
            theParams += " --armor";
        }
        for (String theRecipient : aRecipients) {
            theParams += " --recipient " + theRecipient;
        }
        theParams += " --output " + theDestinationFile.getAbsolutePath();
        if (!aSign) {
            theParams += " --encrypt " + aInputFile.getAbsolutePath();
        } else {
            theParams += " -se ";
            if (aUserID != null) {
                theParams += " -u ";
                theParams += aUserID;
                thePassphrase = getPassphraseFor(aUserID);
                if (thePassphrase != null) {
                    theParams += " --passphrase-fd 0 ";
                }
            }
            theParams += aInputFile.getAbsolutePath();
        }
        executePGP(theParams, thePassphrase);
        return theDestinationFile;
    }

    protected List<String> executePGP(String aParams, String aPassphrase) throws IOException, InterruptedException, EMailException {
        String theCommand = getGpgPath();
        theCommand += " " + aParams.trim();
        logger.logDebug("Ausf�hrung GPG : " + theCommand);
        Process theProcess = Runtime.getRuntime().exec(theCommand);
        if (aPassphrase != null) {
            OutputStreamWriter theOutputWriter = new OutputStreamWriter(theProcess.getOutputStream());
            theOutputWriter.write(aPassphrase);
            theOutputWriter.write('\n');
            theOutputWriter.flush();
        }
        StreamPrinterThread theInputStreamPrinterThread = new StreamPrinterThread(theProcess.getInputStream());
        StreamPrinterThread theErrorStreamPrinterThread = new StreamPrinterThread(theProcess.getErrorStream());
        theInputStreamPrinterThread.start();
        theErrorStreamPrinterThread.start();
        theProcess.waitFor();
        theInputStreamPrinterThread.join();
        theErrorStreamPrinterThread.join();
        theProcess.getOutputStream().close();
        theProcess.getInputStream().close();
        theProcess.getErrorStream().close();
        int theExitValue = theProcess.exitValue();
        logger.logDebug("GPG ausgef�hrt, Exit - Code ist " + theExitValue);
        List<String> theResult = theInputStreamPrinterThread.getBuffer();
        if (theResult.size() > 0) {
            StringBuilder theBuffer = new StringBuilder();
            for (String aLine : theResult) {
                theBuffer.append(aLine).append(LINE_SEPARATOR);
            }
            logger.logDebug("GPG sagte " + theBuffer);
        }
        switch(theExitValue) {
            case RETURN_OK:
                break;
            default:
                String theMessage = theErrorStreamPrinterThread.getText();
                throw new EMailException("Aufruf von PGP ist fehlgeschlagen! Exit Code: " + theExitValue + " - " + theMessage);
        }
        return theResult;
    }

    /**
     * {@inheritDoc}
     */
    public void sendTextMessageEncrypted(String aText, String aFrom, String[] aRecipients, String aSubject) throws EMailException {
        sendTextMessageWithAttachmentsEncrypted(aText, aFrom, aRecipients, aSubject, null);
    }

    /**
     * {@inheritDoc}
     */
    public void sendTextMessageWithAttachmentsEncrypted(String aText, String aFrom, String[] aRecipients, String aSubject, List<MailAttachment> aAttachments) throws EMailException {
        try {
            MimeMessage theMessage = prepareFor(aFrom, aRecipients, aSubject);
            MimeBodyPart theBodyPart = new MimeBodyPart();
            theBodyPart.setText(aText);
            File theDestinationFile = null;
            try {
                theDestinationFile = pgpCrypt(theBodyPart, aRecipients, true, false, null);
                PGPMimeMultiPart theMultiPartMessage = PGPMimeMultiPart.createInstance();
                StringBuilder theBuilder = new StringBuilder();
                BufferedReader theReader = new BufferedReader(new FileReader(theDestinationFile));
                while (theReader.ready()) {
                    String theLine = theReader.readLine();
                    if (theLine != null) {
                        theBuilder.append(theLine + LINE_SEPARATOR);
                    }
                }
                theReader.close();
                theBodyPart = new MimeBodyPart();
                theBodyPart.setText(theBuilder.toString().trim());
                theMultiPartMessage.addBodyPart(theBodyPart);
                if (aAttachments != null) {
                    for (MailAttachment theAttachment : aAttachments) {
                        File theAttachmentFile = null;
                        try {
                            theAttachmentFile = pgpCrypt(theAttachment.getData(), aRecipients, false, false, null);
                            theBodyPart = new MimeBodyPart();
                            theBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(new FileInputStream(theAttachmentFile), theAttachment.getContentType())));
                            theBodyPart.setFileName(theAttachment.getFileName() + ".gpg");
                            theMultiPartMessage.addBodyPart(theBodyPart);
                        } finally {
                            if (theAttachmentFile != null) {
                                theAttachmentFile.delete();
                            }
                        }
                    }
                }
                theMessage.setContent(theMultiPartMessage);
                Transport.send(theMessage);
            } finally {
                if (theDestinationFile != null) {
                    theDestinationFile.delete();
                }
            }
        } catch (EMailException e) {
            throw e;
        } catch (Exception e) {
            throw new EMailException("Fehler beim Senden der Mail", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendTextMessageEncryptedAndSigned(String aText, String aFrom, String[] aRecipients, String aSubject) throws EMailException {
        sendTextMessageWithAttachmentsEncryptedAndSigned(aText, aFrom, aRecipients, aSubject, null);
    }

    /**
     * {@inheritDoc}
     */
    public void sendTextMessageWithAttachmentsEncryptedAndSigned(String aText, String aFrom, String[] aRecipients, String aSubject, List<MailAttachment> aAttachments) throws EMailException {
        try {
            MimeMessage theMessage = prepareFor(aFrom, aRecipients, aSubject);
            MimeBodyPart theBodyPart = new MimeBodyPart();
            theBodyPart.setText(aText);
            File theDestinationFile = null;
            try {
                theDestinationFile = pgpCrypt(theBodyPart, aRecipients, true, true, aFrom);
                PGPMimeMultiPart theMultiPartMessage = PGPMimeMultiPart.createInstance();
                StringBuilder theBuilder = new StringBuilder();
                BufferedReader theReader = new BufferedReader(new FileReader(theDestinationFile));
                while (theReader.ready()) {
                    String theLine = theReader.readLine();
                    if (theLine != null) {
                        theBuilder.append(theLine + LINE_SEPARATOR);
                    }
                }
                theReader.close();
                theBodyPart = new MimeBodyPart();
                theBodyPart.setText(theBuilder.toString().trim());
                theMultiPartMessage.addBodyPart(theBodyPart);
                if (aAttachments != null) {
                    for (MailAttachment theAttachment : aAttachments) {
                        File theAttachmentFile = null;
                        try {
                            theAttachmentFile = pgpCrypt(theAttachment.getData(), aRecipients, false, true, aFrom);
                            theBodyPart = new MimeBodyPart();
                            theBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(new FileInputStream(theAttachmentFile), theAttachment.getContentType())));
                            theBodyPart.setFileName(theAttachment.getFileName() + ".gpg");
                            theMultiPartMessage.addBodyPart(theBodyPart);
                        } finally {
                            if (theAttachmentFile != null) {
                                theAttachmentFile.delete();
                            }
                        }
                    }
                }
                theMessage.setContent(theMultiPartMessage);
                Transport.send(theMessage);
            } finally {
                if (theDestinationFile != null) {
                    theDestinationFile.delete();
                }
            }
        } catch (EMailException e) {
            throw e;
        } catch (Exception e) {
            throw new EMailException("Fehler beim Senden der Mail", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addPublicKey(Reader aReader) throws EMailException {
        File theDestinationFile = null;
        try {
            theDestinationFile = File.createTempFile("AMIS", ".tmp");
            FileWriter theWriter = new FileWriter(theDestinationFile);
            BufferedReader theReader = new BufferedReader(aReader);
            while (theReader.ready()) {
                String theLine = theReader.readLine();
                if (theLine != null) {
                    theWriter.write(theLine + LINE_SEPARATOR);
                } else {
                    break;
                }
            }
            theWriter.close();
            String theParams = " --batch --import " + theDestinationFile.getAbsolutePath();
            executePGP(theParams, null);
        } catch (EMailException e) {
            throw e;
        } catch (Exception e) {
            throw new EMailException("Fehler beim Hinzuf�gen des Schl�ssels", e);
        } finally {
            if (theDestinationFile != null) {
                theDestinationFile.delete();
            }
        }
    }

    protected PGPKeyInfo getKeyInfoFromLine(String aLine) {
        aLine = aLine.substring(3).trim();
        int theP = aLine.indexOf(" ");
        String theKeyID = aLine.substring(0, theP);
        int theP2 = aLine.indexOf(" ", theP + 1);
        String theDate = aLine.substring(theP + 1, theP2);
        int theP3 = aLine.indexOf("<", theP2 + 1);
        String theName = aLine.substring(theP2 + 1, theP3).trim();
        int theP4 = aLine.indexOf(">", theP3 + 1);
        String theUID = aLine.substring(theP3 + 1, theP4).trim();
        return new PGPKeyInfo(theKeyID, theUID, theDate, theName);
    }

    /**
     * {@inheritDoc}
     */
    public List<PGPKeyInfo> getKnownKeys() throws EMailException {
        try {
            List<PGPKeyInfo> theResult = new Vector<PGPKeyInfo>();
            String theParams = " --list-keys ";
            List<String> theLines = executePGP(theParams, null);
            String theKeyLine = null;
            for (String aLine : theLines) {
                if (aLine.startsWith("pub")) {
                    if (theKeyLine == null) {
                        theKeyLine = aLine;
                    } else {
                        theResult.add(getKeyInfoFromLine(theKeyLine));
                        theKeyLine = aLine;
                    }
                }
                if (aLine.startsWith("uid")) {
                    aLine = aLine.substring(3).trim();
                    theKeyLine += " " + aLine;
                    theResult.add(getKeyInfoFromLine(theKeyLine));
                    theKeyLine = null;
                }
            }
            if (theKeyLine != null) {
                theResult.add(getKeyInfoFromLine(theKeyLine));
            }
            Collections.sort(theResult, new GenericComparator("uid"));
            return theResult;
        } catch (EMailException e) {
            throw e;
        } catch (Exception e) {
            throw new EMailException("Fehler beim Auslesen der �ffentlichen Schl�ssel", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeKeyFor(String aUID) throws EMailException {
        try {
            String theParams = " --batch --yes --delete-key " + aUID;
            executePGP(theParams, null);
        } catch (EMailException e) {
            throw e;
        } catch (Exception e) {
            throw new EMailException("Fehler beim L�schen des Schl�ssels f�r " + aUID, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void exportKey(String aUID, Writer aWriter) throws EMailException {
        try {
            String theParams = " --batch --armor --export " + aUID;
            List<String> theResult = executePGP(theParams, null);
            for (String theLine : theResult) {
                aWriter.write(theLine + LINE_SEPARATOR);
            }
        } catch (EMailException e) {
            throw e;
        } catch (Exception e) {
            throw new EMailException("Fehler beim Hinzuf�gen des Schl�ssels", e);
        }
    }
}

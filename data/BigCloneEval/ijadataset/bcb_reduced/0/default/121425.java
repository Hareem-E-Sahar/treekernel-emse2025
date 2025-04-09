import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.*;
import java.util.Properties;
import java.util.Vector;
import java.util.Date;
import java.io.IOException;

/**
 * All the functionality for handeling mails is here
 * This server needs a POP3 account somewhere
 * and a SMTP server for sending mails
 * Please look at INSTALL
 */
public class MRMail implements MRConnectionHandler {

    /**
     * The Postoffice mailprotocol. Please use the
     * secure variant: pop3s
     */
    final String PROTOCOL = "pop3s";

    /**
     * The POP3 host, where the account is
     */
    final String HOST = "mypop3host";

    /**
     * The username
     */
    final String USER = "mypop3username";

    /**
     * The Password
     */
    final String PASSWORD = "mypop3password";

    /**
     * The smtp server for sending mails
     */
    final String MAILHOST = "mysmtpserver";

    /**
     * Address for sending emails (FROM:)
     */
    final String FROM = "mymailaddress";

    /**
     * Subject of the mails going out
     */
    final String SUBJECT = "Your Mailrank results";

    /**
     * flushes the mails after processing them
     * Set this true (recommended) or the mails
     * will be processed again and again
     */
    final boolean FLUSH = false;

    /**
     * if true, more output - useful for debugging
     */
    final boolean DEBUG = false;

    static final String MBOX = "INBOX";

    static final String MAILER = "Mailrankserver";

    /**
     * Runs popmail() and sleeps for minutes given
     * @param minutes the interval between two "pops"
     */
    public void run(int minutes) {
        while (true) {
            if (DEBUG) System.out.println("Go to work");
            popmail();
            if (DEBUG) System.out.println("Go to sleep");
            try {
                Thread.sleep(minutes * 1000 * 60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Does all the work
     * gets a connection to the POP3 server,
     * receives the mails, processes them and
     * sends out answer mails
     */
    public void popmail() {
        Properties myProp = System.getProperties();
        Session mySession = Session.getInstance(myProp, null);
        mySession.setDebug(DEBUG);
        Store myStore = null;
        try {
            myStore = mySession.getStore(PROTOCOL);
        } catch (NoSuchProviderException e) {
            System.out.println(e.getMessage());
            System.out.println("Sorry, no support for protocol: " + PROTOCOL);
        }
        try {
            myStore.connect(HOST, USER, PASSWORD);
            Folder myFolder = myStore.getFolder(MBOX);
            myFolder.open(Folder.READ_WRITE);
            if (myFolder.getMessageCount() > 0) {
                if (DEBUG) System.out.println("Getting the messages.");
                Message[] myMessages = myFolder.getMessages();
                for (int i = 0; i < myMessages.length; i++) {
                    if (DEBUG) System.out.println("This Message with Subject: " + myMessages[i].getSubject());
                    if (myMessages[i].isMimeType("text/plain")) {
                        if (DEBUG) {
                            System.out.println("Plaintext-mail received, OK");
                            try {
                                if (DEBUG) System.out.println((String) myMessages[i].getContent());
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                                System.out.println("Can't get the Message. Don't know why.");
                            }
                        }
                        try {
                            InternetAddress mySender = (InternetAddress) (myMessages[i].getFrom()[0]);
                            if (DEBUG) System.out.println("Processing mail from address: " + mySender.getAddress());
                            this.parseContent((String) myMessages[i].getContent(), mySender);
                            myMessages[i].setFlag(Flags.Flag.DELETED, FLUSH);
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                            System.out.println("Can't get the Message. Don't know why.");
                        }
                    } else {
                        System.out.println("Plaintext-mail support only");
                    }
                }
                myFolder.close(true);
                myStore.close();
            }
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
            System.out.println("There's a problem with the messages. Sorry.");
        }
    }

    /**
     * Takes a line from the mailbody, makes a MRData object
     * with the help of the MRDataParser, queries the DB and
     * sends the answer back to the sender of the mail
     * @param content a line from the body of a mail
     * @param mySender the mailaddress to send the answer mail to
     */
    private void parseContent(String content, InternetAddress mySender) {
        Vector result = new Vector();
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (DEBUG) System.out.println("Line" + i + " : " + lines[i]);
            MRDataParser myParser = new MRDataParser();
            MRData myData = myParser.parse(lines[i].trim());
            if (myData.getCommand() != null) {
                if (myData.getCommand().equals("setValues")) {
                    send(myData);
                } else {
                    result.add(send(myData));
                }
            }
        }
        sendMail(result, mySender);
    }

    /**
     * Takes a vector of vectors of MRData objects
     * (every query results in a vector of MRData objects)
     * and the mailaddress to send the answer mail to
     * @param myData a vector of vectors of MRData objects
     * @param mySender the mailaddress to send the answer mail to
     */
    private void sendMail(Vector myData, InternetAddress mySender) {
        if (!myData.isEmpty()) {
            Properties myProp = System.getProperties();
            Session mySession = Session.getInstance(myProp, null);
            mySession.setDebug(DEBUG);
            myProp.put("mail.smtp.host", MAILHOST);
            Message myMessage = new MimeMessage(mySession);
            try {
                myMessage.setFrom(new InternetAddress(FROM));
                myMessage.setRecipient(Message.RecipientType.TO, mySender);
                myMessage.setHeader("X-Mailer", MAILER);
                myMessage.setSubject(SUBJECT);
                myMessage.setSentDate(new Date());
            } catch (MessagingException e) {
                System.out.println(e.getMessage());
                System.out.println("Check from, recipient, mail, subject and date. There's a problem.");
            }
            String myMessageBody = "";
            for (int i = 0; i < myData.size(); i++) {
                Vector myVec = ((Vector) myData.get(i));
                for (int j = 0; j < myVec.size(); j++) {
                    MRData data = ((MRData) myVec.get(j));
                    myMessageBody = myMessageBody.concat(data.getUser() + ":" + data.getAddress() + ":" + data.getScore() + ":" + data.getCount() + "\n");
                }
            }
            if (DEBUG) System.out.println("MRMAIL BodyText: " + myMessageBody);
            if (myMessageBody != null) {
                try {
                    myMessage.setText(myMessageBody);
                } catch (MessagingException e) {
                    System.out.println(e.getMessage());
                    System.out.println("The Messagebody is not valid");
                }
                try {
                    Transport.send(myMessage);
                } catch (MessagingException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Can't send the mail.");
                }
            }
        } else {
            if (DEBUG) System.out.println("Result is empty. Maybe only setValues? Not sending an email");
        }
    }

    /**
     * Takes a MRData object and asks the DB. Gets back a vector
     * of MRData objects
     * @param myData a MRData object
     * @return a Vector of MRData objects
     */
    public Vector send(MRData myData) {
        if (DEBUG) System.out.println("Asking the DB");
        MRServerChannel chann = new MRServerChannel(myData);
        return chann.doWork();
    }
}

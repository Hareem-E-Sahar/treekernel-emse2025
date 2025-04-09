import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ServerSocketFactory;

public final class ReceiveMails extends Thread {

    private final Store store;

    private final Folder folder;

    private volatile boolean isRunning;

    public ReceiveMails(final Store store, final Folder folder) {
        setPriority(Thread.NORM_PRIORITY + 3);
        this.store = store;
        this.folder = folder;
        isRunning = true;
        start();
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void run() {
        while (isRunning) {
            try {
                messagesAdded(folder.getMessages());
                Thread.sleep(3);
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void close() throws MessagingException {
        folder.close(true);
        store.close();
    }

    static final Pattern subjPattern = Pattern.compile("perform number (\\d+) time (\\d+)");

    static final Pattern fromPattern = Pattern.compile("([A-Za-z]+)\\.([0-9a-f]+)@performanceanalysis.de");

    static final ReceiveMails receive() {
        final Session session = PerformanceAnalysis.session;
        final Store store;
        final Folder folder;
        try {
            store = session.getStore("imap");
            store.connect();
            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);
            return new ReceiveMails(store, folder);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        ReceiveMails rm = receive();
        Runtime.getRuntime().addShutdownHook(new PerformanceAnalysis.WriterHook(new File("C:/test.csv")));
        try {
            ServerSocket listener = ServerSocketFactory.getDefault().createServerSocket(2354);
            Socket socket = listener.accept();
            Scanner scan = new Scanner(socket.getInputStream());
            PrintWriter printer = new PrintWriter(socket.getOutputStream());
            printer.println("Please press \"w\" to send a report!");
            printer.println("Please press \"q\" to finish the test (wait some time please)!");
            printer.flush();
            char input;
            ScannerLoop: do {
                input = scan.next().trim().charAt(0);
                switch(input) {
                    case 'w':
                        printer.println("Preparing report for sending ...");
                        printer.flush();
                        MimeMessage msg = new MimeMessage(PerformanceAnalysis.session);
                        MimeMultipart mmp = new MimeMultipart();
                        MimeBodyPart mbp = new MimeBodyPart();
                        msg.setFrom(new InternetAddress("testresult@performanceanalysis.de"));
                        long time = System.currentTimeMillis();
                        msg.setSubject("Test from " + time);
                        msg.addRecipient(Message.RecipientType.TO, new InternetAddress("simon.jarke@i-u.de"));
                        msg.addRecipient(Message.RecipientType.TO, new InternetAddress("dennis.baumgart@i-u.de"));
                        CharArrayWriter writer = new CharArrayWriter(20 * 1024);
                        try {
                            PerformanceAnalysis.results.toCSVWriter(writer);
                            mbp.setFileName("csv-report-" + time + ".csv");
                            mbp.setContent(writer.toString(), "text/plain");
                            mmp.addBodyPart(mbp);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        msg.setContent(mmp);
                        Transport.send(msg);
                        printer.println("Report send out...");
                        printer.flush();
                        break;
                    case 'q':
                        break ScannerLoop;
                    default:
                        printer.println("... wrong input! Please press again");
                        printer.flush();
                }
            } while (true);
            System.out.println("Quitting ...");
            rm.setRunning(false);
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
    }

    private final void messagesAdded(final Message[] msg) {
        String id;
        int roundtrip;
        int msgid;
        TestType testType;
        try {
            for (int i = 0; i < msg.length; i++) {
                if (msg[i].isExpunged()) continue;
                final String from = msg[i].getFrom()[0].toString();
                final String subject = msg[i].getSubject();
                final Matcher fromMatcher = fromPattern.matcher(from);
                final Matcher subjMatcher = subjPattern.matcher(subject);
                if (subjMatcher.matches() && fromMatcher.matches()) {
                    roundtrip = (int) (System.currentTimeMillis() - Long.parseLong(subjMatcher.group(2)));
                    testType = Enum.valueOf(TestType.class, fromMatcher.group(1));
                    id = fromMatcher.group(2);
                    msgid = Integer.parseInt(subjMatcher.group(1));
                    msg[i].setFlag(Flags.Flag.DELETED, true);
                    folder.expunge();
                    PerformanceAnalysis.results.add(testType, id, msgid, roundtrip);
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

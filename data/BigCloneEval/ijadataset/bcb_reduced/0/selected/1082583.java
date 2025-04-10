package com.fatronik.codius.examples.javamail;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.*;
import javax.activation.*;

public class msgshow {

    static String protocol;

    static String host = null;

    static String user = null;

    static String password = null;

    static String mbox = null;

    static String url = null;

    static int port = -1;

    static boolean verbose = false;

    static boolean debug = false;

    static boolean showStructure = false;

    static boolean showMessage = false;

    static boolean showAlert = false;

    static boolean saveAttachments = false;

    static int attnum = 1;

    public static void main(String argv[]) {
        int msgnum = -1;
        int optind;
        InputStream msgStream = System.in;
        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-T")) {
                protocol = argv[++optind];
            } else if (argv[optind].equals("-H")) {
                host = argv[++optind];
            } else if (argv[optind].equals("-U")) {
                user = argv[++optind];
            } else if (argv[optind].equals("-P")) {
                password = argv[++optind];
            } else if (argv[optind].equals("-v")) {
                verbose = true;
            } else if (argv[optind].equals("-D")) {
                debug = true;
            } else if (argv[optind].equals("-f")) {
                mbox = argv[++optind];
            } else if (argv[optind].equals("-L")) {
                url = argv[++optind];
            } else if (argv[optind].equals("-p")) {
                port = Integer.parseInt(argv[++optind]);
            } else if (argv[optind].equals("-s")) {
                showStructure = true;
            } else if (argv[optind].equals("-S")) {
                saveAttachments = true;
            } else if (argv[optind].equals("-m")) {
                showMessage = true;
            } else if (argv[optind].equals("-a")) {
                showAlert = true;
            } else if (argv[optind].equals("--")) {
                optind++;
                break;
            } else if (argv[optind].startsWith("-")) {
                System.out.println("Usage: msgshow [-L url] [-T protocol] [-H host] [-p port] [-U user]");
                System.out.println("\t[-P password] [-f mailbox] [msgnum] [-v] [-D] [-s] [-S] [-a]");
                System.out.println("or msgshow -m [-v] [-D] [-s] [-S] [-f msg-file]");
                System.exit(1);
            } else {
                break;
            }
        }
        try {
            if (optind < argv.length) msgnum = Integer.parseInt(argv[optind]);
            Properties props = System.getProperties();
            Session session = Session.getInstance(props, null);
            session.setDebug(debug);
            if (showMessage) {
                MimeMessage msg;
                if (mbox != null) msg = new MimeMessage(session, new BufferedInputStream(new FileInputStream(mbox))); else msg = new MimeMessage(session, msgStream);
                dumpPart(msg);
                System.exit(0);
            }
            Store store = null;
            if (url != null) {
                URLName urln = new URLName(url);
                store = session.getStore(urln);
                if (showAlert) {
                    store.addStoreListener(new StoreListener() {

                        public void notification(StoreEvent e) {
                            String s;
                            if (e.getMessageType() == StoreEvent.ALERT) s = "ALERT: "; else s = "NOTICE: ";
                            System.out.println(s + e.getMessage());
                        }
                    });
                }
                store.connect();
            } else {
                if (protocol != null) store = session.getStore(protocol); else store = session.getStore();
                if (host != null || user != null || password != null) store.connect(host, port, user, password); else store.connect();
            }
            Folder folder = store.getDefaultFolder();
            if (folder == null) {
                System.out.println("No default folder");
                System.exit(1);
            }
            if (mbox == null) mbox = "INBOX";
            folder = folder.getFolder(mbox);
            if (folder == null) {
                System.out.println("Invalid folder");
                System.exit(1);
            }
            try {
                folder.open(Folder.READ_WRITE);
            } catch (MessagingException ex) {
                folder.open(Folder.READ_ONLY);
            }
            int totalMessages = folder.getMessageCount();
            if (totalMessages == 0) {
                System.out.println("Empty folder");
                folder.close(false);
                store.close();
                System.exit(1);
            }
            if (verbose) {
                int newMessages = folder.getNewMessageCount();
                System.out.println("Total messages = " + totalMessages);
                System.out.println("New messages = " + newMessages);
                System.out.println("-------------------------------");
            }
            if (msgnum == -1) {
                Message[] msgs = folder.getMessages();
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.FLAGS);
                fp.add("X-Mailer");
                folder.fetch(msgs, fp);
                for (int i = 0; i < msgs.length; i++) {
                    System.out.println("--------------------------");
                    System.out.println("MESSAGE #" + (i + 1) + ":");
                    dumpEnvelope(msgs[i]);
                }
            } else {
                System.out.println("Getting message number: " + msgnum);
                Message m = null;
                try {
                    m = folder.getMessage(msgnum);
                    dumpPart(m);
                } catch (IndexOutOfBoundsException iex) {
                    System.out.println("Message number out of range");
                }
            }
            folder.close(false);
            store.close();
        } catch (Exception ex) {
            System.out.println("Oops, got exception! " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public static void dumpPart(Part p) throws Exception {
        if (p instanceof Message) dumpEnvelope((Message) p);
        String ct = p.getContentType();
        try {
            pr("CONTENT-TYPE: " + (new ContentType(ct)).toString());
        } catch (ParseException pex) {
            pr("BAD CONTENT-TYPE: " + ct);
        }
        String filename = p.getFileName();
        if (filename != null) pr("FILENAME: " + filename);
        if (p.isMimeType("text/plain")) {
            pr("This is plain text");
            pr("---------------------------");
            if (!showStructure && !saveAttachments) System.out.println((String) p.getContent());
        } else if (p.isMimeType("multipart/*")) {
            pr("This is a Multipart");
            pr("---------------------------");
            Multipart mp = (Multipart) p.getContent();
            level++;
            int count = mp.getCount();
            for (int i = 0; i < count; i++) dumpPart(mp.getBodyPart(i));
            level--;
        } else if (p.isMimeType("message/rfc822")) {
            pr("This is a Nested Message");
            pr("---------------------------");
            level++;
            dumpPart((Part) p.getContent());
            level--;
        } else {
            if (!showStructure && !saveAttachments) {
                Object o = p.getContent();
                if (o instanceof String) {
                    pr("This is a string");
                    pr("---------------------------");
                    System.out.println((String) o);
                } else if (o instanceof InputStream) {
                    pr("This is just an input stream");
                    pr("---------------------------");
                    InputStream is = (InputStream) o;
                    int c;
                    while ((c = is.read()) != -1) System.out.write(c);
                } else {
                    pr("This is an unknown type");
                    pr("---------------------------");
                    pr(o.toString());
                }
            } else {
                pr("---------------------------");
            }
        }
        if (saveAttachments && level != 0 && !p.isMimeType("multipart/*")) {
            String disp = p.getDisposition();
            if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
                if (filename == null) filename = "Attachment" + attnum++;
                pr("Saving attachment to file " + filename);
                try {
                    File f = new File(filename);
                    if (f.exists()) throw new IOException("file exists");
                    ((MimeBodyPart) p).saveFile(f);
                } catch (IOException ex) {
                    pr("Failed to save attachment: " + ex);
                }
                pr("---------------------------");
            }
        }
    }

    public static void dumpEnvelope(Message m) throws Exception {
        pr("This is the message envelope");
        pr("---------------------------");
        Address[] a;
        if ((a = m.getFrom()) != null) {
            for (int j = 0; j < a.length; j++) pr("FROM: " + a[j].toString());
        }
        if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++) {
                pr("TO: " + a[j].toString());
                InternetAddress ia = (InternetAddress) a[j];
                if (ia.isGroup()) {
                    InternetAddress[] aa = ia.getGroup(false);
                    for (int k = 0; k < aa.length; k++) pr(" GROUP: " + aa[k].toString());
                }
            }
        }
        pr("SUBJECT: " + m.getSubject());
        Date d = m.getSentDate();
        pr("SendDate: " + (d != null ? d.toString() : "UNKNOWN"));
        Flags flags = m.getFlags();
        StringBuffer sb = new StringBuffer();
        Flags.Flag[] sf = flags.getSystemFlags();
        boolean first = true;
        for (int i = 0; i < sf.length; i++) {
            String s;
            Flags.Flag f = sf[i];
            if (f == Flags.Flag.ANSWERED) s = "\\Answered"; else if (f == Flags.Flag.DELETED) s = "\\Deleted"; else if (f == Flags.Flag.DRAFT) s = "\\Draft"; else if (f == Flags.Flag.FLAGGED) s = "\\Flagged"; else if (f == Flags.Flag.RECENT) s = "\\Recent"; else if (f == Flags.Flag.SEEN) s = "\\Seen"; else continue;
            if (first) first = false; else sb.append(' ');
            sb.append(s);
        }
        String[] uf = flags.getUserFlags();
        for (int i = 0; i < uf.length; i++) {
            if (first) first = false; else sb.append(' ');
            sb.append(uf[i]);
        }
        pr("FLAGS: " + sb.toString());
        String[] hdrs = m.getHeader("X-Mailer");
        if (hdrs != null) pr("X-Mailer: " + hdrs[0]); else pr("X-Mailer NOT available");
    }

    static String indentStr = " ";

    static int level = 0;

    /**
	 * Print a, possibly indented, string.
	 */
    public static void pr(String s) {
        if (showStructure) System.out.print(indentStr.substring(0, level * 2));
        System.out.println(s);
    }
}

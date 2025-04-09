import java.util.*;
import java.io.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.mail.*;
import javax.mail.internet.*;

class MessageViewer extends JWindowFrame {

    private String STD_VIEWER = "Standard Viewer";

    private String RAW_VIEWER = "Raw Viewer";

    public MessageViewer(final MimeMessage msg, final JMenu windowMenu) {
        super(windowMenu, "<NONE>", true, true, true, true);
        final CardLayout cards = new CardLayout();
        setLayout(cards);
        try {
            JComponent firstCard = processPart(msg, this);
            add(firstCard, STD_VIEWER);
        } catch (Exception ex) {
            LogFrame.log(ex);
        }
        try {
            String raw;
            if (msg.getSize() > 1024 * 1024) {
                raw = "Message is too big!";
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                msg.writeTo(baos);
                raw = baos.toString();
            }
            JTextArea jta = new JTextArea(raw);
            jta.setFont(Options.monoFont);
            jta.setEditable(false);
            JScrollPane secondCard = new JScrollPane(jta);
            add(secondCard, RAW_VIEWER);
        } catch (Exception ex) {
            LogFrame.log(ex);
        }
        JMenuBar jmb = new JMenuBar();
        setJMenuBar(jmb);
        JMenu mMessage = new JMenu("Message");
        jmb.add(mMessage);
        final JCheckBoxMenuItem mRaw = new JCheckBoxMenuItem("Raw view");
        mMessage.add(mRaw);
        mRaw.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (mRaw.getState()) cards.show(getContentPane(), RAW_VIEWER); else cards.show(getContentPane(), STD_VIEWER);
            }
        });
        JMenuItem mReply = new JMenuItem("Reply");
        mMessage.add(mReply);
        mReply.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JInternalFrame jif = new Composer(msg, windowMenu);
                jif.setSize(640, 480);
                jif.show();
                getDesktopPane().add(jif);
                try {
                    jif.setSelected(true);
                } catch (Exception ex) {
                    LogFrame.log(ex);
                }
            }
        });
        JMenuItem mForward = new JMenuItem("Forward");
        mMessage.add(mForward);
        mForward.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JInternalFrame jif = new Forward(msg, windowMenu);
                jif.pack();
                jif.show();
                getDesktopPane().add(jif);
                try {
                    jif.setSelected(true);
                } catch (Exception ex) {
                    LogFrame.log(ex);
                }
            }
        });
        JMenuItem mSaveAs = new JMenuItem("Save As...");
        mMessage.add(mSaveAs);
        mSaveAs.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Options.saveFileChooser.setSelectedFile(null);
                int returnVal = Options.saveFileChooser.showSaveDialog(MessageViewer.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = Options.saveFileChooser.getSelectedFile();
                        OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                        msg.writeTo(os);
                        os.close();
                    } catch (Exception ex) {
                        LogFrame.log(ex);
                    }
                }
            }
        });
    }

    private static String[] processAddresses(Address[] plain) {
        String[] addresses;
        if (plain == null) addresses = new String[0]; else {
            addresses = new String[plain.length];
            for (int i = 0; i < plain.length; i++) {
                InternetAddress ia = (InternetAddress) plain[i];
                addresses[i] = ia.toUnicodeString();
            }
        }
        return addresses;
    }

    private static JComponent processMimeBodyPart(final MimeBodyPart mbp, final JInternalFrame parent) throws MessagingException, IOException {
        NumberFormat format = NumberFormat.getIntegerInstance();
        String disposition = mbp.getDisposition();
        String fileName = mbp.getFileName();
        String contentType = mbp.getContentType();
        JComponent jc;
        if (fileName == null && !(disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT))) {
            StringBuffer sb = new StringBuffer();
            BufferedReader in = new BufferedReader(new InputStreamReader(mbp.getInputStream()));
            char[] buf = new char[4096];
            int nch;
            while ((nch = in.read(buf)) != -1) {
                sb.append(new String(buf, 0, nch));
            }
            jc = processString(sb.toString());
        } else {
            jc = new JPanel(new GridLayout(7, 2));
            jc.add(new JLabel("Disposition:"));
            jc.add(new JLabel(disposition, JLabel.RIGHT));
            jc.add(new JLabel("FileName:"));
            JButton jb;
            if (fileName == null) jb = new JButton("Save"); else {
                fileName = MimeUtility.decodeText(fileName);
                jb = new JButton(fileName);
            }
            jb.addActionListener(new SaveStreamAction(fileName, mbp.getInputStream(), parent));
            jc.add(jb);
            jc.add(new JLabel("Description:"));
            jc.add(new JLabel(mbp.getDescription(), JLabel.RIGHT));
            jc.add(new JLabel("Size:"));
            int size = mbp.getSize();
            jc.add(new JLabel(new StringBuffer(format.format(size)).append(" bytes").toString(), JLabel.RIGHT));
            jc.add(new JLabel("ContentID:"));
            jc.add(new JLabel(mbp.getContentID(), JLabel.RIGHT));
            jc.add(new JLabel("Encoding:"));
            jc.add(new JLabel(mbp.getEncoding(), JLabel.RIGHT));
            jc.add(new JLabel("Raw:"));
            jb = new JButton("Save as raw");
            jb.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Options.saveFileChooser.setSelectedFile(null);
                    int returnVal = Options.saveFileChooser.showSaveDialog(parent);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = Options.saveFileChooser.getSelectedFile();
                        try {
                            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                            mbp.writeTo(os);
                            os.close();
                        } catch (Exception ex) {
                            LogFrame.log(ex);
                        }
                    }
                }
            });
            jc.add(jb);
        }
        return jc;
    }

    private static JComponent processMimeMultipart(MimeMultipart mmp, JInternalFrame parent) throws MessagingException, IOException {
        JTabbedPane jtp = new JTabbedPane();
        for (int i = 0; i < mmp.getCount(); i++) {
            Part bp = mmp.getBodyPart(i);
            String contentType = bp.getContentType();
            JComponent jc = processPart(bp, parent);
            jtp.addTab(contentType, jc);
        }
        jtp.setSelectedIndex(0);
        return jtp;
    }

    private static JComponent processMimeMessage(MimeMessage msg, JInternalFrame parent) {
        JPanel upPanel = new JPanel();
        upPanel.setLayout(new BoxLayout(upPanel, BoxLayout.Y_AXIS));
        JPanel jpt;
        JLabel jlt;
        try {
            String[] addresses = processAddresses(msg.getFrom());
            if (addresses.length > 0) {
                jpt = new JPanel(new GridLayout(1, 2));
                jlt = new JLabel("From:");
                jpt.add(jlt);
                JList list = new JList(addresses);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                jpt.add(new JScrollPane(list));
                upPanel.add(jpt);
            }
        } catch (Exception ex) {
            LogFrame.log(ex);
        }
        try {
            String[] addresses = processAddresses(msg.getRecipients(Message.RecipientType.TO));
            if (addresses.length > 0) {
                jpt = new JPanel(new GridLayout(1, 2));
                jlt = new JLabel("To:");
                jpt.add(jlt);
                JList list = new JList(addresses);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                jpt.add(new JScrollPane(list));
                upPanel.add(jpt);
            }
        } catch (Exception ex) {
            LogFrame.log(ex);
        }
        try {
            String[] addresses = processAddresses(msg.getRecipients(Message.RecipientType.CC));
            if (addresses.length > 0) {
                jpt = new JPanel(new GridLayout(1, 2));
                jlt = new JLabel("CC:");
                jpt.add(jlt);
                JList list = new JList(addresses);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                jpt.add(new JScrollPane(list));
                upPanel.add(jpt);
            }
        } catch (Exception ex) {
            LogFrame.log(ex);
        }
        try {
            String[] addresses = processAddresses(msg.getRecipients(Message.RecipientType.BCC));
            if (addresses.length > 0) {
                jpt = new JPanel(new GridLayout(1, 2));
                jlt = new JLabel("BCC:");
                jpt.add(jlt);
                JList list = new JList(addresses);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                jpt.add(new JScrollPane(list));
                upPanel.add(jpt);
            }
        } catch (Exception ex) {
            LogFrame.log(ex);
        }
        try {
            jpt = new JPanel(new GridLayout(1, 2));
            jlt = new JLabel("Subject:");
            jpt.add(jlt);
            String sbj = msg.getSubject();
            jlt = new JLabel(sbj);
            jpt.add(jlt);
            upPanel.add(jpt);
            parent.setTitle(sbj);
        } catch (Exception ex) {
            LogFrame.log(ex);
        }
        try {
            Date data = msg.getSentDate();
            if (data != null) {
                String ds = data.toString();
                jpt = new JPanel(new GridLayout(1, 2));
                jlt = new JLabel("Date:");
                jpt.add(jlt);
                jlt = new JLabel(ds);
                jpt.add(jlt);
                upPanel.add(jpt);
            }
        } catch (Exception ex) {
            LogFrame.log(ex);
        }
        JComponent downPanel = null;
        try {
            Object cont = msg.getContent();
            downPanel = processPart(cont, parent);
        } catch (Exception ex) {
            LogFrame.log(ex);
        }
        return new JSplitPane(JSplitPane.VERTICAL_SPLIT, upPanel, downPanel);
    }

    private static JComponent processString(String str) {
        JTextArea jta = new JTextArea(str);
        jta.setEditable(false);
        return new JScrollPane(jta);
    }

    public static JComponent processPart(Object obj, JInternalFrame parent) throws MessagingException, IOException {
        if (obj instanceof MimeMessage) {
            MimeMessage msg = (MimeMessage) obj;
            return processMimeMessage(msg, parent);
        }
        if (obj instanceof MimeMultipart) {
            MimeMultipart mmp = (MimeMultipart) obj;
            return processMimeMultipart(mmp, parent);
        }
        if (obj instanceof MimeBodyPart) {
            MimeBodyPart mbp = (MimeBodyPart) obj;
            if (mbp.isMimeType("message/rfc822")) {
                MimeMessage msg = new MimeMessage(Options.session, mbp.getInputStream());
                return processMimeMessage(msg, parent);
            }
            if (mbp.isMimeType("multipart/alternative")) {
                MimeMultipart mmp = new MimeMultipart(new MimePartDataSource(mbp));
                return processMimeMultipart(mmp, parent);
            }
            return processMimeBodyPart(mbp, parent);
        }
        if (obj instanceof String) {
            String str = (String) obj;
            return processString(str);
        }
        return new JLabel(obj.getClass().getName());
    }
}

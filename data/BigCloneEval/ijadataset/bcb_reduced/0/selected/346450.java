package com.patientis.upgrade.diagnostic;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import com.patientis.ejb.system.EmailHandler;
import com.patientis.ejb.system.IEmail;
import com.patientis.framework.controls.ISButton;
import com.patientis.framework.controls.ISButtonPanel;
import com.patientis.framework.controls.ISLabel;
import com.patientis.framework.controls.ISPanel;
import com.patientis.framework.controls.ISTextArea;
import com.patientis.framework.controls.ISButtonPanel.ButtonPanelConfig;
import com.patientis.framework.controls.forms.ISFrame;
import com.patientis.framework.controls.listeners.ISActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author gcaulton
 *
 */
public class Display {

    /**
	 * 
	 * @param frame
	 * @param msg
	 * @param title
	 */
    public static void message(ISFrame frame, String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
	 * 
	 */
    public static void email(final String subject, final String textContents, final String emailAddress) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "25");
        Session session = Session.getInstance(props, new EmailAuth("posdiagnostic@gmail.com", "patientos"));
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("posdiagnostic@gmail.com"));
        InternetAddress[] address = { new InternetAddress(emailAddress) };
        msg.setRecipients(Message.RecipientType.TO, address);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setText(textContents);
        Transport.send(msg);
    }

    /**
	 * 
	 * @param component
	 * @param usefullframe
	 */
    public static String getInput(JFrame frame, String title, String header, String label, String defaultText) {
        final List<String> response = new ArrayList<String>();
        final JDialog dialog = new JDialog(frame, true);
        dialog.setSize(480, 320);
        dialog.setLocation(200, 200);
        final ISTextArea textarea = new ISTextArea();
        textarea.setText(defaultText);
        dialog.setTitle(title);
        ISButtonPanel buttons = new ISButtonPanel(ButtonPanelConfig.OKCANCEL);
        buttons.getCmdButton0().addActionListener(new ISActionListener() {

            @Override
            public void actionExecuted(ActionEvent e) throws Exception {
                response.clear();
                dialog.setVisible(false);
            }
        });
        buttons.getCmdButton1().addActionListener(new ISActionListener() {

            @Override
            public void actionExecuted(ActionEvent e) throws Exception {
                response.add(textarea.getText());
                dialog.setVisible(false);
            }
        });
        ISPanel panel = new ISPanel(new BorderLayout());
        panel.add(new ISLabel(header), BorderLayout.NORTH);
        panel.add(new javax.swing.JScrollPane(textarea), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
        if (response.size() > 0) {
            return response.get(0);
        } else {
            return null;
        }
    }
}

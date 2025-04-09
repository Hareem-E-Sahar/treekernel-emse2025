package org.xsocket.server;

import java.util.Properties;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.xsocket.TestUtil;

/**
*
* @author grro@xsocket.org
*/
public final class RunnableSmtpJavaMailClient {

    private Session session = null;

    private String data = null;

    public static void main(String... args) throws Exception {
        if (args.length != 4) {
            System.out.println("usage org.xsocket.server.RunnableSmtpJavaMailClient <host> <port> <datasize> <repeats>");
            return;
        }
        new RunnableSmtpJavaMailClient().launch(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    }

    public void launch(String host, String port, int datasize, int repeats) throws Exception {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        session = Session.getInstance(props, null);
        data = "hello\n\n" + new String(TestUtil.generatedByteArray(datasize));
        for (int i = 0; i < 5; i++) {
            send();
        }
        long elapsed = 0;
        for (int i = 0; i < repeats; i++) {
            try {
                elapsed += send();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {
                }
            }
        }
        System.out.println("elapsed average" + (elapsed / repeats));
    }

    private long send() throws Exception {
        MimeMessage msg = new MimeMessage(session);
        msg.setSender(new InternetAddress("test@socket.org"));
        msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress("test2@socket.org"));
        msg.setText(data);
        long start = System.currentTimeMillis();
        Transport.send(msg);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(elapsed);
        return elapsed;
    }
}

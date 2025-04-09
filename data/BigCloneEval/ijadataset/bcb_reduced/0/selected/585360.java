package cc;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class mh extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String s = "OK";
        try {
            send_mail("qdone@rogers.com", "test", "test");
        } catch (Exception e) {
            s = e.toString();
        }
        PrintWriter out = resp.getWriter();
        out.write(s);
        out.flush();
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String s = "OK";
        String s1 = req.getParameter("a1");
        String s2 = req.getParameter("a2");
        String s3 = req.getParameter("a3");
        try {
            send_mail(s1, s2, s3);
        } catch (Exception e) {
            s = e.toString();
        }
        PrintWriter out = resp.getWriter();
        out.write(s);
        out.flush();
        out.close();
    }

    public void send_mail(String s1, String s2, String s3) throws Exception {
        String[] tt = s1.split(",");
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String sdt = "";
        TimeZone tz = TimeZone.getTimeZone("America/Montreal");
        SimpleDateFormat parser = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss z");
        Date d = Calendar.getInstance(TimeZone.getTimeZone("EST")).getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss z'('Z')'");
        formatter.setTimeZone(tz);
        sdt = formatter.format(d);
        s2 = s2 + " " + sdt;
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("ymdata@gmail.com", "UFOS Web"));
        for (int i = 0; i < tt.length; i++) msg.addRecipient(Message.RecipientType.TO, new InternetAddress(tt[i], tt[i]));
        msg.setSubject(s2);
        msg.setHeader("Content-type:", "text/html;charset=ISO-8859-1");
        msg.setText(s3);
        Transport.send(msg);
    }

    public static String rfu(String url) {
        StringBuffer s = new StringBuffer();
        try {
            URL u = new URL(url);
            InputStream in = u.openConnection().getInputStream();
            for (int ch = in.read(); ch > 0; ch = in.read()) {
                s.append((char) ch);
            }
            in.close();
        } catch (IOException e) {
            return e.toString();
        }
        return s.toString();
    }
}

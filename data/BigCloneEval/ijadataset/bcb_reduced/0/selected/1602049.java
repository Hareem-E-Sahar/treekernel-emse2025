package audictiv.server.mainBar.login.services;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import audictiv.client.mainBar.login.classes.LoginInfo;
import audictiv.client.mainBar.login.classes.LoginStatus;
import audictiv.client.mainBar.login.services.LoginService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {

    public LoginStatus checkLogin(LoginInfo loginInfo) {
        try {
            String login = loginInfo.getLogin();
            String password = loginInfo.getPassword();
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url = "jdbc:mysql://localhost";
            String db_user = "root";
            String db_password = null;
            Connection connection = DriverManager.getConnection(url, db_user, db_password);
            System.out.println("Connected !");
            String query1 = "SELECT * FROM a296665_audictiv.MEMBER WHERE CLOGIN='" + login + "'";
            Statement stmt1 = connection.createStatement();
            ResultSet result1 = stmt1.executeQuery(query1);
            if (result1.next()) {
                if (result1.getString("CLOGIN").equals(login)) {
                    String query2 = "SELECT MD5('" + password + "')";
                    Statement stmt2 = connection.createStatement();
                    ResultSet result2 = stmt2.executeQuery(query2);
                    System.out.println(query2);
                    if (result2.next()) {
                        if (result1.getString("CPASSWORD").equals(result2.getString(1))) {
                            return LoginStatus.LOGIN_OK;
                        } else return LoginStatus.WRONG_PASSWORD;
                    }
                }
            } else return LoginStatus.WRONG_LOGIN;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return LoginStatus.CONNECTION_FAILED;
    }

    @Override
    public int sendCode(String to) {
        Random random = new Random();
        int randomInt = random.nextInt(999999);
        System.out.println(randomInt);
        Properties props = System.getProperties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress("kryo.afh@gmail.com", "Audictiv"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Audictiv - Forgot password - Code");
            message.setContent("Here is the code: " + randomInt, "text/html");
            Transport.send(message);
            return randomInt;
        } catch (AddressException e3) {
            e3.printStackTrace();
            return -1;
        } catch (MessagingException e3) {
            e3.printStackTrace();
            return -1;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return -1;
        }
    }
}

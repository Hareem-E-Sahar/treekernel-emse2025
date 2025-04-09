import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.RequestOptions;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.creare.algorithms.Algorithm;
import com.creare.algorithms.server.AlgorithmImpl;
import com.creare.algorithms.server.AlgorithmSlave;
import com.creare.algorithms.server.StatusTracker;

/**
  * Algorithm which monitors a status channel, and issues an alert email 
  *  when it drops below a specified value.
  *
  * @since 2004/11/18
  * @author WHF
  */
public class AircraftAlert extends AlgorithmImpl {

    protected AlgorithmSlave getSlave() {
        return slave;
    }

    protected void initProperties(Map props, Map info) {
        super.mapAll(props, propNames, propDefaults);
        super.mapAll(info, propNames, propInfo);
        List l;
        l = (List) props.get(INPUT_CHANNEL_NAMES_PROP);
        l.clear();
        l.addAll(Arrays.asList(iChanNames));
        l = (List) props.get(INPUT_CHANNELS_PROP);
        l.addAll(Arrays.asList(iChanDefaults));
        l = (List) info.get(INPUT_CHANNELS_PROP);
        l.clear();
        l.addAll(Arrays.asList(iChanInfo));
    }

    /**
	  * @throws A RuntimeException if the properties were invalid.
	  */
    protected boolean validateProperties(Map props, Map info, Set used) {
        takeoffThreshold = Double.parseDouble(props.get(TAKEOFF_THRESHOLD_PROP).toString());
        used.add(TAKEOFF_THRESHOLD_PROP);
        landingThreshold = Double.parseDouble(props.get(LANDING_THRESHOLD_PROP).toString());
        used.add(LANDING_THRESHOLD_PROP);
        minTimeBetweenTakeoffs = Double.parseDouble(props.get(MIN_TIME_BETWEEN_TAKEOFFS_PROP).toString());
        used.add(MIN_TIME_BETWEEN_TAKEOFFS_PROP);
        try {
            email = InternetAddress.parse(props.get(EMAIL_PROP).toString(), false);
        } catch (AddressException ae) {
            throw new RuntimeException("Error setting destination address.", ae);
        }
        used.add(EMAIL_PROP);
        used.add(MAILSERVER_PROP);
        String fromEmailStr = props.get(FROM_EMAIL_PROP).toString();
        if (fromEmailStr.length() == 0) fromEmail = null; else {
            try {
                fromEmail = new InternetAddress(fromEmailStr);
            } catch (AddressException ae) {
                throw new RuntimeException("Error setting sender address.", ae);
            }
        }
        used.add(FROM_EMAIL_PROP);
        emailAccountUsername = props.get(EMAIL_USERNAME_PROP).toString();
        used.add(EMAIL_USERNAME_PROP);
        emailAccountPassword = props.get(EMAIL_PASSWD_PROP).toString();
        used.add(EMAIL_PASSWD_PROP);
        return true;
    }

    private void sendAlert(String subjectText, String msgText) throws MessagingException {
        System.getProperties().put("mail.smtp.host", getPropHandle().get(MAILSERVER_PROP).toString());
        if ((emailAccountUsername != null) && (!emailAccountUsername.equals(""))) {
            System.getProperties().put("mail.smtp.auth", "true");
        }
        Session session = Session.getInstance(System.getProperties(), null);
        Message msg = new MimeMessage(session);
        if (fromEmail != null) {
            msg.setFrom(fromEmail);
        } else {
            msg.setFrom();
        }
        msg.setRecipients(Message.RecipientType.TO, email);
        msg.setSubject(subjectText);
        Date now = new Date();
        String localhost;
        try {
            localhost = java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException uhe) {
            uhe.printStackTrace();
            localhost = "unknown";
        }
        msg.setText(msgText);
        msg.setHeader("X-Mailer", this.getClass().getName());
        msg.setSentDate(now);
        if ((emailAccountUsername == null) || (emailAccountUsername.equals(""))) {
            Transport.send(msg);
        } else {
            System.err.println("Using email username/password");
            Transport transport = session.getTransport("smtp");
            transport.connect(getPropHandle().get(MAILSERVER_PROP).toString(), emailAccountUsername, emailAccountPassword);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
        }
    }

    private class AlertSlave extends AlgorithmSlaveImpl {

        AlertSlave() {
        }

        protected void init() throws SAPIException {
            emailFailed = tookOffFlag = landedFlag = false;
            tookOffTime = 0;
        }

        /** 
		  * Returns true when the outputMap should be flushed.
		  */
        protected boolean process(ChannelMap inputMapI, ChannelMap synchMapI, ChannelMap outputMapI) throws SAPIException {
            boolean successR = false;
            String chan = ((List) getPropHandle().get(INPUT_CHANNELS_PROP)).get(0).toString();
            int iIdx = inputMapI.GetIndex(chan), sIdx = synchMapI.GetIndex(getInputStatusMap().get(chan).toString());
            if (iIdx == -1) return false;
            double[] times = inputMapI.GetTimes(iIdx);
            double[] data = null;
            StatusTracker stat = null;
            if (sIdx < 0) {
                stat = new StatusTracker(latchedStatus);
                stat.add(times[0], STATUS_GOOD);
            }
            switch(inputMapI.GetType(iIdx)) {
                case ChannelMap.TYPE_INT16:
                    short[] sData = inputMapI.GetDataAsInt16(iIdx);
                    data = new double[times.length];
                    for (int idx1 = 0; idx1 < sData.length; ++idx1) {
                        data[idx1] = (double) sData[idx1];
                    }
                    break;
                case ChannelMap.TYPE_INT32:
                    int[] iData = inputMapI.GetDataAsInt32(iIdx);
                    data = new double[times.length];
                    for (int idx1 = 0; idx1 < iData.length; ++idx1) {
                        data[idx1] = (double) iData[idx1];
                    }
                    break;
                case ChannelMap.TYPE_FLOAT32:
                    float[] fData = inputMapI.GetDataAsFloat32(iIdx);
                    data = new double[times.length];
                    for (int idx1 = 0; idx1 < fData.length; ++idx1) {
                        data[idx1] = (double) fData[idx1];
                    }
                    break;
                case ChannelMap.TYPE_FLOAT64:
                    data = inputMapI.GetDataAsFloat64(iIdx);
                    break;
            }
            int dataPoints = times.length;
            long currTime = System.currentTimeMillis();
            for (int point = 0; point < dataPoints; ++point) {
                if ((data[point] > takeoffThreshold) && ((tookOffFlag == false) || ((landedFlag == true) && ((currTime - tookOffTime) > (minTimeBetweenTakeoffs * 1000))))) {
                    tookOffTime = currTime;
                    tookOffFlag = true;
                    landedFlag = false;
                    String msg = "Aircraft ascended to altitude " + java.lang.Math.round(data[point]) + " (above threshold " + takeoffThreshold + ")" + " at " + com.rbnb.api.Time.since1970(times[point]) + "\n\n" + chan;
                    System.err.println("\n\n" + msg);
                    try {
                        sendAlert("Aircraft ascent notice!", msg);
                    } catch (MessagingException me) {
                        me.printStackTrace();
                    }
                }
                if ((data[point] < landingThreshold) && (tookOffFlag == true) && (landedFlag == false)) {
                    landedFlag = true;
                    String msg = "Aircraft descended to altitude " + java.lang.Math.round(data[point]) + " (below threshold " + landingThreshold + ")" + " at " + com.rbnb.api.Time.since1970(times[point]) + "\n\n" + chan;
                    System.err.println("\n\n" + msg);
                    try {
                        sendAlert("Aircraft descent notice!", msg);
                    } catch (MessagingException me) {
                        me.printStackTrace();
                    }
                }
            }
            if (sIdx != -1) {
                outputMapI.PutTimeRef(synchMapI, sIdx);
                outputMapI.PutDataAsInt8(getOutputStatusIndex(), synchMapI.GetDataAsInt8(sIdx));
            } else if (stat != null) {
                outputMapI.PutTimes(stat.getTimeArray());
                outputMapI.PutDataAsInt8(getOutputStatusIndex(), stat.getStatusArray());
            }
            successR = true;
            return (successR);
        }

        protected void cleanup() {
        }
    }

    private boolean tookOffFlag = false, landedFlag = false, emailFailed;

    private long tookOffTime = 0;

    private double takeoffThreshold, landingThreshold, minTimeBetweenTakeoffs;

    private String emailAccountUsername, emailAccountPassword;

    private InternetAddress[] email;

    private InternetAddress fromEmail;

    private final AlertSlave slave = new AlertSlave();

    private static final String TAKEOFF_THRESHOLD_PROP = "Takeoff altitude threshold", LANDING_THRESHOLD_PROP = "Landing altitude threshold", MIN_TIME_BETWEEN_TAKEOFFS_PROP = "Min time between takeoffs", EMAIL_PROP = "Contact Email", MAILSERVER_PROP = "Mail Server", FROM_EMAIL_PROP = "Alert Sender Address", EMAIL_USERNAME_PROP = "Email account username", EMAIL_PASSWD_PROP = "Email account password";

    private static final String[] propNames = { TAKEOFF_THRESHOLD_PROP, LANDING_THRESHOLD_PROP, MIN_TIME_BETWEEN_TAKEOFFS_PROP, EMAIL_PROP, MAILSERVER_PROP, FROM_EMAIL_PROP, EMAIL_USERNAME_PROP, EMAIL_PASSWD_PROP }, propDefaults = { "1500", "1000", "600", "", "", "", "", "" }, propInfo = { "Threshold above which takeoff email notice is sent.", "Threshold below which landing email notice is sent.", "Minimum time between takeoffs (seconds)", "Email address(es) of the individual(s) to alert.", "Hostname of the SMTP server to issue the message.", "Email address to use in the 'From' field of the message; " + "<user>@<domain> is used if empty.", "Email account username", "Email account password" }, iChanNames = { "AltitudeInput" }, iChanInfo = { "Alititude of aircraft." }, iChanDefaults = { "" }, oChanNames = {}, oChanInfo = {}, oChanDefaults = {};

    private byte latchedStatus = STATUS_STARTUP;
}

package vorm.gvn;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.InputSource;

public class GoogleVoiceNotifier {

    private static int currentMajorVersionNumber = 1;

    private static int currentMinorVersionNumber = 3;

    private static String currentSubMinorVersionCharacter = "";

    private static String versionRegex = "Current Version=(\\d+)\\.(\\d+)(\\w?)";

    private static Image noMsgImage = Toolkit.getDefaultToolkit().getImage("images/google-voice.png");

    private static Image newMsgImage = Toolkit.getDefaultToolkit().getImage("images/google-voice-new-msg.png");

    private static Timer timer = new Timer();

    private static Timer updateTimer = new Timer();

    private static HttpClient client = new HttpClient();

    private static HttpState state = new HttpState();

    private static TrayIcon trayIcon;

    private static OptionsGUI optGUI = new OptionsGUI();

    private static Options curOptions;

    private static String auth = "";

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        System.out.println(LogFactory.FACTORY_PROPERTIES);
        URL imgURL = GoogleVoiceNotifier.class.getResource("images/google-voice.png");
        noMsgImage = Toolkit.getDefaultToolkit().getImage(imgURL);
        imgURL = GoogleVoiceNotifier.class.getResource("images/google-voice-new-msg.png");
        newMsgImage = Toolkit.getDefaultToolkit().getImage(imgURL);
        setupSystrayIcon();
        try {
            Options o = new Options();
            String passwd = getPassword(o);
            if (passwd == null) {
                throw new Exception("User entered a null password");
            }
            o.setPassword(passwd);
            saveOptions(o);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            optGUI.displayOptions(Options.getDefaultOptions());
        }
    }

    private static String getPassword(Options o) {
        char[] password = null;
        JLabel label = new JLabel("Please enter the password for the google voice account: " + o.getUsername());
        JPasswordField pass = new JPasswordField();
        Object[] array = { label, pass };
        pass.requestFocusInWindow();
        int res = JOptionPane.showConfirmDialog(null, array, "Password for " + o.getUsername(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            password = pass.getPassword();
        } else {
            return null;
        }
        return new String(password);
    }

    private static int newMessages() {
        int newMessages = 0;
        JSONTokener tokener = new JSONTokener(getInbox());
        JSONObject object = null;
        try {
            object = new JSONObject(tokener);
            JSONObject messages = object.getJSONObject("messages");
            Iterator<?> keys = messages.keys();
            while (keys.hasNext()) {
                Object id = keys.next();
                JSONObject message = messages.getJSONObject(id.toString());
                if (!message.getBoolean("isRead")) {
                    newMessages++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newMessages;
    }

    private static void login(Options options) {
        PostMethod method = new PostMethod("https://www.google.com/accounts/ClientLogin");
        method.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        method.setRequestHeader("User-Agent", "google-voice-notifier");
        state = new HttpState();
        method.addParameter("accountType", "HOSTED_OR_GOOGLE");
        method.addParameter("Email", options.getUsername());
        method.addParameter("Passwd", options.getPassword());
        method.addParameter("service", "grandcentral");
        method.addParameter("source", "google-voice-notifier");
        try {
            int statusCode = client.executeMethod(client.getHostConfiguration(), method, state);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Auth=")) {
                        auth = line.split("=", 2)[1].trim();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
    }

    private static String getInbox() {
        String out = "";
        String request = "https://www.google.com/voice/inbox/recent/inbox";
        try {
            request += "?auth=" + URLEncoder.encode(auth, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        GetMethod method = new GetMethod(request);
        try {
            int statusCode = client.executeMethod(client.getHostConfiguration(), method, state);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }
            XPath xpath = XPathFactory.newInstance().newXPath();
            InputSource isource = new InputSource(method.getResponseBodyAsStream());
            out = xpath.evaluate("/response/json", isource);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        return out;
    }

    private static void checkAndDisplayMessages() {
        if (curOptions == null) return;
        int n;
        if ((n = newMessages()) > 0) {
            trayIcon.setImage(newMsgImage);
            trayIcon.displayMessage("New Messages!", "You have " + n + " new messages!", TrayIcon.MessageType.INFO);
        } else {
            trayIcon.setImage(noMsgImage);
        }
    }

    private static void checkForUpdates() {
        if (curOptions.isCheckForUpdates()) {
            GetMethod method = new GetMethod("http://code.google.com/p/google-voice-notifier/");
            try {
                client.executeMethod(method);
                String body = method.getResponseBodyAsString();
                Pattern p = Pattern.compile(versionRegex);
                Matcher m = p.matcher(body);
                if (m.find()) {
                    if ((Integer.parseInt(m.group(1)) > currentMajorVersionNumber) || (Integer.parseInt(m.group(2)) > currentMinorVersionNumber) || (m.group(3).compareTo(currentSubMinorVersionCharacter) > 0)) {
                        JOptionPane.showMessageDialog(null, "There is a new Version!\nGoto http://code.google.com/p/google-voice-notifier/ to get the new version!");
                    }
                }
            } catch (HttpException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                method.releaseConnection();
            }
        }
    }

    public static void saveOptions(Options newOpts) {
        curOptions = newOpts;
        if (curOptions.getProxyType() != null) {
            switch(curOptions.getProxyType()) {
                case HTTP:
                    client.getHostConfiguration().setProxy(curOptions.getProxyHost(), curOptions.getProxyPort());
                    break;
                case SOCKS:
                    System.setProperty("socksProxyHost", curOptions.getProxyHost());
                    System.setProperty("socksProxyPort", Integer.toString(curOptions.getProxyPort()));
                    break;
                default:
                    break;
            }
        }
        login(newOpts);
        setupTimers(newOpts);
    }

    private static void setupTimers(Options opts) {
        TimerTask task = new TimerTask() {

            public void run() {
                checkAndDisplayMessages();
            }
        };
        TimerTask checkForUpdates = new TimerTask() {

            public void run() {
                checkForUpdates();
            }
        };
        timer.cancel();
        timer.purge();
        timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, opts.getDelay());
        updateTimer.cancel();
        updateTimer.purge();
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(checkForUpdates, 0, 1000 * 60 * 60 * 24);
    }

    private static void setupSystrayIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            ActionListener exitListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
            };
            ActionListener optionsListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    optGUI.displayOptions(curOptions);
                }
            };
            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            MenuItem optionsItem = new MenuItem("Options");
            optionsItem.addActionListener(optionsListener);
            popup.add(optionsItem);
            popup.add(defaultItem);
            trayIcon = new TrayIcon(noMsgImage, "Google Voice Notifier", popup);
            ActionListener actionListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    checkAndDisplayMessages();
                    if (Desktop.isDesktopSupported()) {
                        try {
                            URI uri = new URI("https://www.google.com/voice/");
                            Desktop.getDesktop().browse(uri);
                        } catch (URISyntaxException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            };
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
            try {
                tray.add(trayIcon);
                if (curOptions != null) {
                    login(curOptions);
                    setupTimers(curOptions);
                    checkForUpdates();
                }
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        } else {
        }
    }
}

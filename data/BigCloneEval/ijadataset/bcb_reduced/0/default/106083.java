import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import com.bluestone.extension.paros.ProxyServer;
import com.bluestone.scripts.ActionScript;
import com.bluestone.scripts.Command;
import com.bluestone.scripts.TestCase;
import com.bluestone.scripts.TestSuite;

/**
 * CallFuntionAction can execute a function.
 * @author <a href="mailto:bluesotne.master@gmail.com">daniel.q</a>
 */
public class RecordDataTool extends Thread implements ProxyListener {

    public static void main(String[] args) {
        try {
            String file = "recorddata_scripts.xml";
            File input = new File(file);
            JAXBContext context = JAXBContext.newInstance(TestSuite.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            TestSuite testsuite = (TestSuite) unmarshaller.unmarshal(input);
            List<TestCase> testcases = testsuite.getTestcases();
            for (TestCase testcase : testcases) {
                List<Command> commands = testcase.getCommands();
                for (Command command : commands) {
                    List<ActionScript> actions = command.getActions();
                    for (ActionScript action : actions) {
                        if (!new RecordDataTool().execute(action)) {
                            break;
                        }
                        ;
                    }
                }
            }
        } catch (Exception me) {
            me.printStackTrace();
        }
    }

    ActionScript action = null;

    String outputfolder = "";

    String recordfile = "";

    StringBuffer records = new StringBuffer();

    long activetime = System.currentTimeMillis();

    int recordId = 0;

    /**
	 * This action can execute a query.
	 * @return false 
	 */
    public boolean execute(ActionScript action) {
        this.action = action;
        ProxyServer proxyServer = ProxyServer.getProxyServer();
        proxyServer.run(action);
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        outputfolder = action.getPara("outputfolder");
        if (outputfolder == null || outputfolder.length() == 0) {
            outputfolder = ".\\record";
        }
        outputfolder = outputfolder + "\\" + date + "\\";
        File folder = new File(outputfolder);
        deleteDirectory(folder);
        mkdirs(outputfolder);
        recordfile = outputfolder + "record.txt";
        boolean result = false;
        try {
            ProxyServer.getProxyServer().addListener(this);
            String maxwaitingtime = action.getPara("maxwaitingtime");
            if (maxwaitingtime == null) {
                maxwaitingtime = "1";
            }
            long waitingtime = 60 * 1000 * Integer.parseInt(maxwaitingtime);
            while (true) {
                this.sleep(1000);
                if (System.currentTimeMillis() - activetime > waitingtime) {
                    break;
                }
            }
            result = true;
        } catch (Exception me) {
            me.printStackTrace();
        }
        return result;
    }

    private String getRecordId() {
        recordId++;
        return "" + recordId;
    }

    public void onHttpRequestSend(HttpMessage msg) {
        String proxysite = action.getPara("proxysite");
        if (proxysite != null && proxysite.length() > 0) {
            String[] filters = proxysite.split(",");
            String requestUrl = msg.getRequestHeader().getURI().toString();
            Pattern p = null;
            boolean isMatch = false;
            for (int i = 0; i < filters.length; i++) {
                p = Pattern.compile(filters[i], Pattern.CASE_INSENSITIVE);
                if (p.matcher(requestUrl).find()) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                return;
            }
        }
        activetime = System.currentTimeMillis();
    }

    public void onHttpResponseReceive(HttpMessage msg) {
        if (msg == null) {
            return;
        }
        String proxysite = action.getPara("proxysite");
        if (proxysite != null && proxysite.length() > 0) {
            String[] filters = proxysite.split(",");
            String requestUrl = msg.getRequestHeader().getURI().toString();
            Pattern p = null;
            boolean isMatch = false;
            for (int i = 0; i < filters.length; i++) {
                p = Pattern.compile(filters[i], Pattern.CASE_INSENSITIVE);
                if (p.matcher(requestUrl).find()) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                return;
            }
        }
        String recordUrl = action.getPara("recordurl");
        if (recordUrl != null && recordUrl.length() > 0) {
            String[] filters = recordUrl.split(",");
            String requestUrl = msg.getRequestHeader().getURI().toString();
            if (requestUrl.indexOf("?") > 0) {
                requestUrl = requestUrl.substring(0, requestUrl.indexOf("?"));
            }
            Pattern p = null;
            boolean isMatch = false;
            for (int i = 0; i < filters.length; i++) {
                if (requestUrl.endsWith(filters[i])) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                return;
            }
        }
        String contentType = msg.getResponseHeader().getHeader(HttpHeader.CONTENT_TYPE).toLowerCase();
        String recordcontenttype = action.getPara("recordcontenttype");
        if (recordcontenttype != null && recordcontenttype.length() > 0) {
            String[] filters = recordcontenttype.split(",");
            Pattern p = null;
            boolean isMatch = false;
            for (int i = 0; i < filters.length; i++) {
                if (contentType.indexOf(filters[i]) > -1) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                return;
            }
        }
        activetime = System.currentTimeMillis();
        String requestURL = msg.getRequestHeader().getURI().toString();
        String id = getRecordId();
        records.append("" + id).append("\r\n");
        Util.saveHttpMessage(msg, outputfolder, "" + id);
        Util.createFile(recordfile, records.toString().getBytes());
    }

    private synchronized boolean mkdirs(String path) {
        boolean flag = true;
        File file = new File(path);
        if (!file.exists()) {
            flag = file.mkdirs();
        }
        return flag;
    }

    private void copyDirectory(File sourceLocation, File targetLocation) {
        try {
            if (sourceLocation.isDirectory()) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdir();
                }
                String[] children = sourceLocation.list();
                for (int i = 0; i < children.length; i++) {
                    copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
                }
            } else {
                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (Exception me) {
            me.printStackTrace();
        }
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}

package com.neurogrid.gui.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.torque.Torque;
import org.apache.torque.util.Criteria;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.servlet.VelocityServlet;
import zoop.Bobbot;
import com.neurogrid.database.SearchCriteria;
import com.neurogrid.gui.http.action.AddBookmarkCommand;
import com.neurogrid.gui.http.action.AddNodeCommand;
import com.neurogrid.gui.http.action.AddTripleCommand;
import com.neurogrid.gui.http.action.AddTriplePageCommand;
import com.neurogrid.gui.http.action.AdvancedSearchCommand;
import com.neurogrid.gui.http.action.AdvancedSearchPageCommand;
import com.neurogrid.gui.http.action.CombineCommand;
import com.neurogrid.gui.http.action.Command;
import com.neurogrid.gui.http.action.CreateUserCommand;
import com.neurogrid.gui.http.action.FeedbackCommand;
import com.neurogrid.gui.http.action.FetchCommand;
import com.neurogrid.gui.http.action.ListCCCommand;
import com.neurogrid.gui.http.action.ListCommand;
import com.neurogrid.gui.http.action.ListEventCommand;
import com.neurogrid.gui.http.action.ListUriDescCommand;
import com.neurogrid.gui.http.action.ListUserCommand;
import com.neurogrid.gui.http.action.LoadBookmarkCommand;
import com.neurogrid.gui.http.action.LoadLegacyCommand;
import com.neurogrid.gui.http.action.LoginCommand;
import com.neurogrid.gui.http.action.NodeFeedbackCommand;
import com.neurogrid.gui.http.action.SearchCCCommand;
import com.neurogrid.gui.http.action.SearchCommand;
import com.neurogrid.gui.http.action.SearchEventCommand;
import com.neurogrid.gui.http.action.SearchEventPageCommand;
import com.neurogrid.gui.http.action.SearchNeuroGridCommand;
import com.neurogrid.gui.http.action.SearchPageCommand;
import com.neurogrid.gui.http.action.SearchUriDescCommand;
import com.neurogrid.gui.http.action.TestCommand;
import com.neurogrid.om.Event;
import com.neurogrid.om.EventType;
import com.neurogrid.om.Keyword;
import com.neurogrid.om.NgUser;
import com.neurogrid.om.NodeDesc;
import com.neurogrid.om.Predicate;
import com.neurogrid.om.Uri;
import com.neurogrid.om.UriDesc;
import com.neurogrid.om.UriTriple;
import com.neurogrid.prime.NeuroGridSearch;
import com.neurogrid.prime.UpdateSuggestions;
import com.neurogrid.tristero.TristeroSearch;

/**
 * Main entry point into the forum application.
 * All requests are made to this servlet.
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.2 $
 * $Id: ControllerServlet.java,v 1.2 2003/07/03 05:38:52 samjoseph Exp $
 */
public class ControllerServlet extends VelocityServlet {

    private static String ERR_MSG_TAG = "forumdemo_current_error_msg";

    public static final String ADD_BOOKMARK = "add_bookmark";

    private static PrivateKey o_priv = null;

    public static PrivateKey getPrivateKey() {
        return o_priv;
    }

    private static PublicKey o_pub = null;

    public static PublicKey getPublicKey() {
        return o_pub;
    }

    public static String o_load_bookmark_call = "neurogrid?action=load_bookmark";

    public static final String DEFAULT_ENCODING = "Shift_JIS";

    public static String o_overall_encoding = DEFAULT_ENCODING;

    private static String o_port = "8080";

    private static String o_server = "localhost";

    public static String o_hard_local_url_head = "http://localhost:";

    public static String o_hard_local_url = "http://localhost:8080";

    public static String o_query_format = null;

    private static String o_query_format_tail = "<QUERY>action=search_neurogrid&user=all&subject=&predicate=&object=</QUERY>" + "<APPEND>&Submit=Search</APPEND>" + "<TYPE>GET</TYPE>" + "<CLASS>NEUROGRID</CLASS>";

    public static String o_neurogrid_path = null;

    public static String o_proxy_path = null;

    public static String o_broker_path = null;

    public static String o_tunnel_path = null;

    public static String o_file_upload_path = null;

    public static String o_image_path = null;

    private static Category o_cat = Category.getInstance(ControllerServlet.class.getName());

    /**
     * initialize the logging system
     *
     * @param p_conf      configuration filename
     */
    public static void init(String p_conf) {
        BasicConfigurator.configure();
        PropertyConfigurator.configure(p_conf);
        o_cat.info("ControllerServlet Initialized");
    }

    /**
     *  lets override the loadConfiguration() so we can do some 
     *  fancier setup of the template path
     */
    protected Properties loadConfiguration(ServletConfig config) throws IOException, FileNotFoundException {
        String x_vel_props_file = config.getInitParameter(INIT_PROPS_KEY);
        String x_log4j_props_file = config.getInitParameter("log4j");
        String x_torque_props_file = config.getInitParameter("torque");
        String x_bot_props_file = config.getInitParameter("bot");
        String x_keystore_props_file = config.getInitParameter("keystore");
        String x_db_props_file = config.getInitParameter("db");
        if (x_vel_props_file != null) {
            String x_vel_props_real_path = getServletContext().getRealPath(x_vel_props_file);
            if (x_vel_props_real_path != null) {
                x_vel_props_file = x_vel_props_real_path;
            }
        }
        Properties x_vel_props = new Properties();
        x_vel_props.load(new FileInputStream(x_vel_props_file));
        String path = x_vel_props.getProperty("file.resource.loader.path");
        if (path != null) {
            path = getServletContext().getRealPath(path);
            x_vel_props.setProperty("file.resource.loader.path", path);
        }
        path = x_vel_props.getProperty("runtime.log");
        if (path != null) {
            path = getServletContext().getRealPath(path);
            x_vel_props.setProperty("runtime.log", path);
        }
        if (x_log4j_props_file != null) {
            String x_log4j_props_real_path = getServletContext().getRealPath(x_log4j_props_file);
            if (x_log4j_props_real_path != null) {
                x_log4j_props_file = x_log4j_props_real_path;
            }
        }
        if (x_torque_props_file != null) {
            String x_torque_props_real_path = getServletContext().getRealPath(x_torque_props_file);
            if (x_torque_props_real_path != null) {
                x_torque_props_file = x_torque_props_real_path;
            }
        }
        if (x_bot_props_file != null) {
            String x_bot_props_real_path = getServletContext().getRealPath(x_bot_props_file);
            if (x_bot_props_real_path != null) {
                x_bot_props_file = x_bot_props_real_path;
            }
        }
        if (x_keystore_props_file != null) {
            String x_keystore_props_real_path = getServletContext().getRealPath(x_keystore_props_file);
            if (x_keystore_props_real_path != null) {
                x_keystore_props_file = x_keystore_props_real_path;
            }
        }
        if (x_db_props_file != null) {
            String x_db_props_real_path = getServletContext().getRealPath(x_db_props_file);
            if (x_db_props_real_path != null) {
                x_db_props_file = x_db_props_real_path;
            }
        }
        StringBuffer x_buf = new StringBuffer(256);
        try {
            Configuration c = (Configuration) new PropertiesConfiguration(x_torque_props_file);
            String x_db_url = (String) (c.getProperty("torque.database.neurogrid.url"));
            String x_true_db_url = null;
            if (x_db_url.indexOf("db.conf") != -1) {
                x_buf.append(":jdbc:mckoi:local://").append(x_db_props_file);
                x_buf.append("/neurogrid?boot_or_create=true");
                x_true_db_url = x_buf.toString();
                c.setProperty("torque.database.neurogrid.url", x_true_db_url);
                System.out.println(c.getProperty("torque.database.neurogrid.url"));
            }
            Torque.init(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ControllerServlet.init(x_log4j_props_file);
        String x_irc_host = x_vel_props.getProperty("irc.host");
        String x_irc_port = x_vel_props.getProperty("irc.port");
        String x_irc_interface = x_vel_props.getProperty("irc.interface");
        String x_irc_nick = x_vel_props.getProperty("irc.nick");
        String x_irc_channel = x_vel_props.getProperty("irc.channel");
        o_overall_encoding = x_vel_props.getProperty("overall.encoding");
        if (o_overall_encoding == null) {
            o_overall_encoding = DEFAULT_ENCODING;
        }
        String x_brokers_contact = x_vel_props.getProperty("brokers.contact");
        o_port = x_vel_props.getProperty("servlet.port");
        o_query_format = x_vel_props.getProperty("servlet.query_format");
        o_neurogrid_path = config.getInitParameter("neurogrid");
        o_proxy_path = config.getInitParameter("proxy");
        o_broker_path = config.getInitParameter("broker");
        o_tunnel_path = config.getInitParameter("tunnel");
        o_file_upload_path = config.getInitParameter("file_upload");
        o_image_path = config.getInitParameter("image");
        String x_port = config.getInitParameter("port");
        o_server = config.getInitParameter("server");
        o_load_bookmark_call = getServletName() + "?action=load_bookmark";
        if (x_port != null) o_port = x_port;
        o_cat.error("neurogrid: " + o_neurogrid_path);
        o_cat.error("proxy: " + o_proxy_path);
        o_cat.error("broker: " + o_broker_path);
        o_cat.error("tunnel: " + o_tunnel_path);
        o_cat.error("file_upload: " + o_file_upload_path);
        o_cat.error("image: " + o_image_path);
        o_cat.error("port: " + o_port);
        o_cat.error("server: " + o_server);
        o_cat.error("o_load_bookmark_call: " + o_load_bookmark_call);
        x_buf.delete(0, x_buf.length());
        x_buf.append("<BASE>").append(o_neurogrid_path.substring(1, o_neurogrid_path.length()));
        x_buf.append("?</BASE>").append(o_query_format_tail);
        o_query_format = x_buf.toString();
        o_cat.error("query_format: " + o_query_format);
        x_buf.delete(0, x_buf.length());
        x_buf.append("http://").append(o_server).append(":").append(o_port);
        o_hard_local_url = x_buf.toString();
        o_cat.error("hard_local_url: " + o_hard_local_url);
        Uri.init(x_log4j_props_file);
        NgUser.init(x_log4j_props_file);
        EventType.init(x_log4j_props_file);
        Predicate.init(x_log4j_props_file);
        Keyword.init(x_log4j_props_file);
        UriTriple.init(x_log4j_props_file);
        o_cat.debug("trying to init event ...");
        Event.init(x_log4j_props_file);
        UriDesc.init(x_log4j_props_file);
        NodeDesc.init(x_log4j_props_file);
        LoginCommand.init(x_log4j_props_file);
        TristeroSearch.init(x_log4j_props_file);
        NeuroGridSearch.init(x_log4j_props_file);
        FeedbackCommand.init(x_log4j_props_file);
        NodeFeedbackCommand.init(x_log4j_props_file);
        UpdateSuggestions.init(x_log4j_props_file);
        try {
            AddBookmarkCommand.init(x_log4j_props_file);
            AddBookmarkCommand.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AddNodeCommand.init(x_log4j_props_file);
            AddNodeCommand.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (x_irc_interface != null && x_irc_interface.equals("true")) {
            try {
                String[] argv = new String[6];
                argv[0] = x_irc_host;
                argv[1] = x_irc_port;
                argv[2] = x_irc_nick;
                argv[3] = x_bot_props_file;
                argv[4] = x_irc_channel;
                argv[5] = x_log4j_props_file;
                Thread x_bobbot = new Thread(new BobbotThread(argv));
                x_bobbot.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            initIdentity(x_keystore_props_file);
            if (x_brokers_contact.equals("true")) {
                Thread x_contact_brokers = new Thread(new ContactBrokers(this));
                x_contact_brokers.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return x_vel_props;
    }

    /**
     *  intialise this nodes identity
     * 
     */
    private void initIdentity(String p_file) throws Exception {
        File x_ks_file = new File(p_file);
        if (x_ks_file.exists() == true) {
            FileInputStream x_fis = new FileInputStream(x_ks_file);
            if (x_fis == null) System.err.println("arg");
            ObjectInputStream s = new ObjectInputStream(x_fis);
            o_priv = (PrivateKey) (s.readObject());
            o_pub = (PublicKey) (s.readObject());
            s.close();
        } else {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            KeyPair pair = keyGen.generateKeyPair();
            o_priv = pair.getPrivate();
            o_pub = pair.getPublic();
            FileOutputStream x_fos = new FileOutputStream(x_ks_file);
            if (x_fos == null) System.err.println("arg");
            ObjectOutputStream s = new ObjectOutputStream(x_fos);
            s.writeObject(o_priv);
            s.writeObject(o_pub);
            s.flush();
            s.close();
        }
    }

    /**
     *  run bobbot
     * 
     */
    public class BobbotThread implements Runnable {

        String[] o_args;

        public BobbotThread(String[] p_args) {
            o_args = p_args;
        }

        public void run() {
            try {
                new Bobbot().realmain(o_args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  create brokers list - sign it and then contact the brokers
     * 
     */
    public class ContactBrokers implements Runnable {

        private ControllerServlet o_controller = null;

        public ContactBrokers(ControllerServlet p_controller) {
            o_controller = p_controller;
        }

        public void run() {
            try {
                Criteria.Criterion x_token1 = TristeroSearch.search("", Criteria.EQUAL, "IS", Criteria.EQUAL, "BROKER", Criteria.EQUAL, null);
                List x_results = TristeroSearch.fetch(x_token1, new SearchCriteria(0, 10));
                String[] x_brokers = new String[x_results.size()];
                StringBuffer x_buf = new StringBuffer();
                for (int i = 0; i < x_brokers.length; i++) {
                    x_brokers[i] = ((UriTriple) (x_results.get(i))).getUri().getUri();
                    x_buf.append(x_brokers[i]).append("\n");
                }
                String o_broker = x_buf.toString();
                Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
                dsa.initSign(o_priv);
                byte[] buffer = o_broker.getBytes();
                dsa.update(buffer, 0, buffer.length);
                byte[] realSig = dsa.sign();
                byte[] key = o_pub.getEncoded();
                byte[] port = o_port.getBytes();
                byte[] query_format = o_query_format.getBytes();
                List x_messages = new Vector(5);
                x_messages.add(key);
                x_messages.add(realSig);
                x_messages.add(buffer);
                x_messages.add(port);
                x_messages.add(query_format);
                for (int i = 0; i < x_brokers.length; i++) {
                    try {
                        if (x_brokers[i].indexOf("localhost") != -1) Transport.getTransport().firewallPost(x_brokers[i], x_messages, o_controller);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *   Handles all requests 
     *
     *  @param request  HttpServletRequest object containing client request
     *  @param response HttpServletResponse object for the response
     */
    public void doFirewallRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Context context = createContext(request, response);
            setContentType(request, response);
            Template template = handleRequest(request, response, context);
            if (template == null) {
                return;
            }
            mergeTemplate(template, context, response);
            requestCleanup(request, response, context);
        } catch (Exception e) {
            error(request, response, e);
        }
    }

    public Template intermediateProcess(String p_template_name) throws Exception, ResourceNotFoundException {
        if (p_template_name == null) {
            System.err.println("Null Template: assuming feedback redirection or bookmark loading ...");
            return null;
        }
        Template template = getTemplate(p_template_name, o_overall_encoding);
        template.setEncoding(o_overall_encoding);
        o_cat.debug("Template: " + template.getName());
        return template;
    }

    /**
     * VelocityServlet handles most of the Servlet issues.
     * By extending it, you need to just implement the handleRequest method.
     * @param the Context created in VelocityServlet.
     * @return the template
     */
    public Template handleRequest(Context ctx) {
        HttpServletRequest req = (HttpServletRequest) ctx.get(VelocityServlet.REQUEST);
        HttpServletResponse resp = (HttpServletResponse) ctx.get(VelocityServlet.RESPONSE);
        resp.setContentType("text/html");
        Template template = null;
        String templateName = null;
        HttpSession sess = req.getSession();
        sess.setAttribute(ERR_MSG_TAG, "all ok");
        try {
            templateName = processRequest(req, resp, ctx);
            template = intermediateProcess(templateName);
        } catch (ResourceNotFoundException rnfe) {
            String err = "ForumDemo -> ControllerServlet.handleRequest() : Cannot find template " + templateName;
            sess.setAttribute(ERR_MSG_TAG, err);
            System.err.println(err);
        } catch (ParseErrorException pee) {
            String err = "ForumDemo -> ControllerServlet.handleRequest() : Syntax error in template " + templateName + ":" + pee;
            sess.setAttribute(ERR_MSG_TAG, err);
            System.err.println(err);
        } catch (Exception e) {
            String err = "Error handling the request: " + e;
            sess.setAttribute(ERR_MSG_TAG, err);
            System.err.println(err);
        }
        return template;
    }

    /**
     * Process the request and execute the command.
     * Uses a command pattern
     * @param the request
     * @param the response 
     * @param the context
     * @return the name of the template to use
     */
    public String processRequest(HttpServletRequest req, HttpServletResponse resp, Context context) throws Exception {
        Cookie x_cookie = findLatestCookie("NG0001", req.getCookies());
        if (x_cookie != null) context.put("login_name", x_cookie.getValue()); else context.put("login_name", NgUser.GUEST);
        context.put("FORM_ACTION", o_neurogrid_path);
        context.put("IMAGE", o_image_path);
        context.put("FILE_UPLOAD_ACTION", o_file_upload_path);
        Command c = null;
        String template = null;
        String name = req.getParameter("action");
        if (name == null || name.equals("")) name = req.getParameter("ACTION");
        o_cat.debug("req.getPathInfo(): " + req.getPathInfo());
        o_cat.debug("req.getPathTranslated(): " + req.getPathTranslated());
        o_cat.debug("req.getQueryString(): " + req.getQueryString());
        o_cat.debug("req.getServletPath(): " + req.getServletPath());
        if (name != null) o_cat.debug("action: " + name);
        if (name == null || name.length() == 0) {
            String x_protocol = req.getParameter("protocol");
            String x_sub_type = req.getParameter("sub_type");
            String x_url = req.getParameter("url");
            if (x_protocol != null && x_protocol.equals("NG_ALPHA") && x_sub_type != null && x_sub_type.equals("feedback")) {
                if (x_url != null) name = "feedback"; else name = "node_feedback";
            } else {
                String x_page = req.getParameter("page");
                if (x_page == null || x_page.length() == 0) {
                    name = "search_neurogrid";
                } else return x_page;
            }
        }
        if (name.equalsIgnoreCase("login")) {
            c = new LoginCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("list")) {
            c = new ListCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("list_event")) {
            c = new ListEventCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("search_event")) {
            c = new SearchEventCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("search_event_page")) {
            c = new SearchEventPageCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("search")) {
            c = new SearchCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("advanced_search")) {
            c = new AdvancedSearchCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("search_neurogrid")) {
            c = new SearchNeuroGridCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("search_uri_desc")) {
            c = new SearchUriDescCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("list_uri_desc")) {
            c = new ListUriDescCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("feedback")) {
            c = new FeedbackCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("node_feedback")) {
            c = new NodeFeedbackCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("add_triple_page")) {
            c = new AddTriplePageCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("search_page")) {
            c = new SearchPageCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("advanced_search_page")) {
            c = new AdvancedSearchPageCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("combine")) {
            c = new CombineCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("fetch")) {
            c = new FetchCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("create_user")) {
            c = new CreateUserCommand(req, resp);
            template = c.exec(context);
            c = new ListUserCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("list_user")) {
            c = new ListUserCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("add_triple")) {
            c = new AddTripleCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase(ADD_BOOKMARK)) {
            c = new AddBookmarkCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("load_bookmark")) {
            c = new LoadBookmarkCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("load_legacy")) {
            c = new LoadLegacyCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("test")) {
            c = new TestCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("add_node")) {
            c = new AddNodeCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("search_cc")) {
            c = new SearchCCCommand(req, resp);
            template = c.exec(context);
        } else if (name.equalsIgnoreCase("list_cc")) {
            c = new ListCCCommand(req, resp);
            template = c.exec(context);
        }
        return template;
    }

    /**
     *  Override the method from VelocityServlet to produce an intelligent 
     *  message to the browser
     */
    protected void error(HttpServletRequest request, HttpServletResponse response, Exception cause) throws ServletException, IOException {
        HttpSession sess = request.getSession();
        String err = (String) sess.getAttribute(ERR_MSG_TAG);
        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>ForumDemo : Error processing the request</h2>");
        html.append("<br><br>There was a problem in the request.");
        html.append("<br><br>The relevant error is :<br>");
        html.append(err);
        html.append("<br><br><br>");
        html.append("The error occurred at :<br><br>");
        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw));
        html.append(sw.toString());
        html.append("</body>");
        html.append("</html>");
        response.getOutputStream().print(html.toString());
    }

    /**
   * Search through a list of cookies looking for one with a particular value.
   *
   * @param p_name        The name of the cookie to look for
   * @param p_cookies     The cookies to look through
   *
   * @returns Cookie    the found cookie or null
   */
    protected Cookie findCookie(String p_name, Cookie[] p_cookies) {
        try {
            for (int i = 0; i < p_cookies.length; i++) {
                if (p_name.equalsIgnoreCase(p_cookies[i].getName())) {
                    return p_cookies[i];
                }
            }
        } catch (Exception e) {
            System.err.println("cookie: " + e.getMessage());
        }
        return null;
    }

    /**
   * Search through a list of cookies looking for one with a particular value, and
   * if there are multiple copies, get the latest one (assuming that we have placed
   * a date value in the cookies comment field during creation)
   *
   * @param p_name        The name of the cookie to look for
   * @param p_cookies     The cookies to look through
   *
   * @returns Cookie    the found cookie or null
   */
    protected Cookie findLatestCookie(String p_name, Cookie[] p_cookies) {
        try {
            Vector x_matching_cookies = new Vector();
            for (int i = 0; i < p_cookies.length; i++) {
                if (p_name.equalsIgnoreCase(p_cookies[i].getName())) {
                    x_matching_cookies.addElement(p_cookies[i]);
                }
            }
            int x_no_matches = x_matching_cookies.size();
            if (x_no_matches == 0) return null; else if (x_no_matches == 1) return (Cookie) (x_matching_cookies.firstElement()); else {
                Cookie x_cookie = null;
                java.util.Date x_created = null;
                java.util.Date x_most_recent = null;
                Cookie x_most_recent_cookie = null;
                for (int j = 0; j < x_no_matches; j++) {
                    x_cookie = (Cookie) (x_matching_cookies.elementAt(j));
                    try {
                        x_created = new java.util.Date(x_cookie.getComment());
                    } catch (Exception e) {
                        x_created = new java.util.Date(System.currentTimeMillis() - 1000000);
                    }
                    if (x_most_recent == null || x_created.after(x_most_recent)) {
                        x_most_recent = x_created;
                        x_most_recent_cookie = x_cookie;
                    }
                }
                return x_most_recent_cookie;
            }
        } catch (Exception e) {
            System.err.println("cookie: " + e.getMessage());
        }
        return null;
    }
}

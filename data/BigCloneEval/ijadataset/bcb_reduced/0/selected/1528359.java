package net.sf.warpcore.cms.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import net.sf.warpcore.ejb.UniquePK;
import net.sf.wedgetarian.util.StageLevel;
import net.sf.warpcore.domperignon.common.*;
import net.sf.warpcore.cms.value.ContentVO;
import net.sf.warpcore.domperignon.api.RepositorySessionFactory;
import net.sf.warpcore.cms.entity.property.PropertyDomain;
import net.sf.warpcore.cms.webfrontend.*;
import net.sf.warpcore.cms.webfrontend.git.*;
import net.sf.wedgetarian.util.MessageBundle;
import java.util.Date;
import java.text.SimpleDateFormat;
import net.sf.warpcore.cms.servlets.stage.*;

public class PublishServlet extends HttpServlet {

    /** Logging **/
    private boolean verbose = true;

    private String deployRoot;

    private UniquePK file;

    private static final String BLACK = "#000000";

    private static final String RED = "#ff0000";

    private static final String GREEN = "#00ff00";

    private static final String YELLOW = "#ffff00";

    private static final String ORANGE = "#ff9900";

    private ServletHelper helper;

    public void init() throws ServletException {
        deployRoot = getServletContext().getInitParameter("net.sf.warpcore.cms.stage.deploy-root");
        helper = new ServletHelper();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Git git = Git.getCurrent(request.getSession());
        Locale locale = request.getLocale();
        MessageBundle messageBundle = new MessageBundle("net.sf.warpcore.cms/servlets/PublishServletMessages");
        System.out.println("net.sf.warpcore.cms.stage.deploy-root = " + deployRoot);
        try {
            PrintWriter out = helper.getPrintWriter(response);
            try {
                String id = request.getParameter("id");
                GitElement element = git.getComponentReader("publish").getElement(id);
                String path = ((String) element.getAttribute("path"));
                String repositoryGroupName = (String) element.getAttribute("repGroupName");
                DOMPerignonTreeNode node = ((DOMPerignonTreeNode) element.getAttribute("node"));
                file = node.getRepositoryNode().getId();
                if (Stage.getStageLock().startUpdate()) {
                    try {
                        String root = deployRoot;
                        if (repositoryGroupName.equals("WEBLIB")) {
                            root += "/WEB-INF";
                        }
                        publish(response, repositoryGroupName, path, root);
                    } finally {
                        Stage.getStageLock().endUpdate();
                    }
                } else {
                    helper.header(out, messageBundle, locale);
                    out.println(messageBundle.getMessage("stage_is_locked", locale));
                    helper.footer(out);
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } catch (IOException IOE) {
            ;
        }
    }

    public void publish(HttpServletResponse response, String repositoryGroupName, String path, String root) throws IOException {
        log("######### Starting Publisher #########");
        log("From " + repositoryGroupName + "/" + path + " to " + root);
        ServletOutputStream out = response.getOutputStream();
        try {
            printHeader(out);
            RepositorySession session = null;
            SimpleDeployer deployer = new SimpleDeployer();
            try {
                print(out, "--------- Publishing Started ---------");
                publishObject(session, file, deployer, root, repositoryGroupName, out);
                print(out, "--------- Publishing Finished ---------");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            printFooter(out);
        } catch (RepositoryException exception) {
            print(out, "Something is wrong with the path '" + repositoryGroupName + ":" + path + "'", true);
            print(out, "Aborting !!!", RED, true);
            System.out.println("Repository error occured " + exception.getMessage());
        } catch (IOException exception) {
            System.out.println("######### Publishing Canceled #########");
            throw exception;
        } catch (Exception exception) {
            System.out.println("Unknown error occured");
        }
    }

    private void publishObject(RepositorySession session, UniquePK node, SimpleDeployer deployer, String root, String repositoryGroupName, ServletOutputStream out) throws IOException {
        if (node.isDirectory()) {
            UniquePK[] children = node.listChildren();
            for (int xy = 0; xy < children.length; xy++) {
                publishObject(session, children[xy], deployer, root, repositoryGroupName, out);
            }
        } else {
            try {
                String name = file.getName();
                if (!deployRoot.endsWith("/")) {
                    deployRoot += "/";
                }
                deployer.copy(file.getPath(), deployRoot + name);
                print(out, "publishing ... ", false);
                print(out, "successful", GREEN, true);
            } catch (Exception ex) {
                ex.printStackTrace();
                print(out, "failed", RED, true);
                print(out, "Reason: Content is null");
            }
        }
    }

    private void print(ServletOutputStream out, String message) throws IOException {
        print(out, message, BLACK);
    }

    private void print(ServletOutputStream out, String message, boolean br) throws IOException {
        print(out, message, BLACK, br);
    }

    private void print(ServletOutputStream out, String message, String color) throws IOException {
        print(out, message, color, true);
    }

    private void print(ServletOutputStream out, String message, String color, boolean br) throws IOException {
        out.print("<font face=\"Helvetica,Arial\" size=\"-1\" color=\"" + color + "\"><b>" + message + "</b></font>");
        if (br) {
            out.println("<br>");
        }
        out.flush();
    }

    private void printHeader(ServletOutputStream out) throws IOException {
        out.println("<html>                                                         ");
        out.println("  <head>                                                       ");
        out.println("    <script language=\"JavaScript\">                           ");
        out.println("      <!--                                                     ");
        out.println("        pixPos = 0;                                            ");
        out.println("        function doScroll() {                                  ");
        out.println("          window.scrollTo(0,pixPos);                           ");
        out.println("          pixPos+=100;                                         ");
        out.println("        }                                                      ");
        out.println("      //-->                                                    ");
        out.println("    </script>                                                  ");
        out.println("  </head>                                                      ");
        out.println("  <body bgcolor=\"#ffffff\">                                   ");
        out.flush();
        out.println("    <script language=\"JavaScript\">                           ");
        out.println("      <!--                                                     ");
        out.println("        doIt = window.setInterval('doScroll()', 1000);         ");
        out.println("      //-->                                                    ");
        out.println("    </script>                                                  ");
        out.println("    <br>                                                       ");
        out.flush();
    }

    private void printFooter(ServletOutputStream out) throws IOException {
        out.println("<script language=\"JavaScript\">                               ");
        out.println("  <!--                                                         ");
        out.println("    window.clearInterval(doIt);                                ");
        out.println("    doScroll();                                                ");
        out.flush();
        out.println("    doScroll();                                                ");
        out.flush();
        out.println("    doScroll();                                                ");
        out.flush();
        out.println("    doScroll();                                                ");
        out.flush();
        out.println("  //-->                                                        ");
        out.println("</script>                                                      ");
        out.println("<br>                                                           ");
        out.println("</body>                                                        ");
        out.println("</html>                                                        ");
        out.flush();
    }
}

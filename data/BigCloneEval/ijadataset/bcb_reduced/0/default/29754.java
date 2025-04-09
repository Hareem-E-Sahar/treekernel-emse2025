import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ePICE extends HttpServlet {

    Connection connectDB = null;

    public String showDateTime(Locale currentLocale) {
        Date today;
        String result;
        DateFormat formatter;
        today = new Date();
        int[] styles = { DateFormat.DEFAULT, DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL };
        formatter = DateFormat.getDateTimeInstance(styles[2], styles[2], currentLocale);
        result = formatter.format(today);
        return (result);
    }

    public void Control(String Action, String UserSystemID, HttpServletRequest request, HttpSession session) throws IllegalStateException, IOException {
        if (Action == "new") {
            try {
                if (session.isNew()) ;
                session.setAttribute("UserSystemID", UserSystemID);
            } catch (IllegalStateException ISE) {
                System.out.println("An attempt was made to access session data with a 'new'" + "request after the session was invalidated.");
            }
        } else if (Action == "kill") {
            try {
                session.invalidate();
            } catch (IllegalStateException ISE) {
                System.out.println("An attempt was made to access session data with a 'kill'" + "request after the session was invalidated.");
            }
        }
    }

    private String PathDB;

    private String UsernameDB;

    private String PasswordDB;

    public void init() throws ServletException {
        PathDB = getInitParameter("PathDB");
        UsernameDB = getInitParameter("UserDB");
        PasswordDB = getInitParameter("PassDB");
        String jdbcDriver = "org.gjt.mm.mysql.Driver";
        String dbURL = "jdbc:mysql://" + PathDB + "?user=" + UsernameDB + "&password=" + PasswordDB;
        try {
            Class.forName(jdbcDriver).newInstance();
            connectDB = DriverManager.getConnection(dbURL);
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("JDBC driver not found: " + jdbcDriver);
        } catch (SQLException e) {
            throw new UnavailableException("Unable to connect to: " + dbURL);
        } catch (Exception e) {
            throw new UnavailableException("Error: " + e);
        }
    }

    /**
	doGet() method called in response to GET request.
	Displays HTML pages that have no form/action section.
	*/
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String Page_request = request.getParameter("page_request");
        byte outputbody = 1;
        String UserSystemID = null;
        String SelectedCourse = null;
        String CurrentTerm = null;
        try {
            Statement sqlstmt = connectDB.createStatement();
            ResultSet termset = sqlstmt.executeQuery("select Term from CurrentTerm");
            while (termset.next()) {
                CurrentTerm = termset.getString("Term");
            }
            session.setAttribute("CurrentTerm", CurrentTerm);
            out.println("<html>");
            if (Page_request == null) {
                out.println("<script src=\"js/navmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                out.println("</script>");
                HtmlSection1(out);
                out.println("<img src=\"graphics/publogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                out.println("<span class=\"title\">About</span>&nbsp<span class=\"epicetitle\">ePICE</span>");
                HtmlSection2(out);
                doGetHtmlSection3(out);
                out.println("<h1 class=\"ah12\">");
                out.println("<span class=\"ahyo12\">ePICE</span> is an environmental tool for");
                out.println("Instructors and Students to communicate outside of the traditional lecture hall");
                out.println("and office-hours settings. Assignments, materials, notes, and student");
                out.println("submissions can be obtained and posted online--providing 24/7 availability and");
                out.println("accountability to both students and instructors.");
                out.println("</h1>");
                out.println("<h1 class=\"ah16\">");
                out.println("Use <span class=\"ahyo16\">ePICE</i></span> to streamline and maximize the");
                out.println("educational experience !");
                out.println("</h1>");
                out.println("<h1 class=\"ah12\">");
                out.println("Note: <span class=\"ahyo12\">ePICE</span> has specific web browser functional requirements!");
                out.println("<br>");
                out.println("Please read the <a class=\"ah10\" href=\"ePICE?page_request=faqPage\">FAQs</a> for more information!");
                out.println("</h1>");
                HtmlClose1(out, UserSystemID, session);
                gotomenu(out);
                HtmlClose2(out);
            } else {
                byte CourseCount = 0;
                byte IncrementCount = 1;
                if (Page_request.equals("loginPage")) {
                    out.println("<script src=\"js/ePICEChk.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    out.println("<script src=\"js/navmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    HtmlSection1(out);
                    out.println("<img src=\"graphics/publogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                    out.println("<span class=\"title\">Login</span>");
                    HtmlSection2(out);
                    doGetHtmlSection3(out);
                    out.println("<h1 class=\"ahb14\">");
                    out.println("<div align=\"center\">");
                    out.println("<p>");
                    out.println("Please login:");
                    out.println("<form action=\"ePICE\" name=\"LoginForm\" method=\"post\" onsubmit=\"return CheckSubmittedValues(\'loginPage\')\">");
                    out.println("<input type=\"hidden\" name=\"page_request\" value=\"logginginPage\">");
                    out.println("<h1 class=\"ahb10\">");
                    out.println("E-mail:<br><input type=\"text\" name=\"Email\">");
                    out.println("<script language=\"JavaScript\">");
                    out.println("document.LoginForm.Email.focus();");
                    out.println("</script>");
                    out.println("<p>");
                    out.println("Password:<br><input type=\"password\" name=\"Passwd\">");
                    out.println("<p>");
                    out.println("<input type=\"submit\" value=\"Login\">&nbsp&nbsp<input type=\"reset\" Value=\"Reset\">");
                    out.println("</h1>");
                    out.println("</form>");
                    out.println("<br>");
                    out.println("<h1 class=\"ah12\">");
                    out.println("A registered account is required.  Please <a class=\"ah12\" href=\"ePICE?page_request=registerstudentPage\">Register</a> first if you need an account.");
                    out.println("</div>");
                    out.println("</h1>");
                    HtmlClose1(out, UserSystemID, session);
                    gotomenu(out);
                    HtmlClose2(out);
                }
                if (Page_request.equals("sysreqPage")) {
                    out.println("<script src=\"js/navmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    HtmlSection1(out);
                    out.println("<img src=\"graphics/publogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                    out.println("<span class=\"title\">System Requirements</span>");
                    HtmlSection2(out);
                    out.println("<span class=\"ahb14\">Minimum system requirements:</span>");
                    doGetHtmlSection4(out);
                    out.println("<h1 class=\"ah14\">");
                    out.println("<p>");
                    out.println("- Pentium (tm) 120 MHz");
                    out.println("<p>");
                    out.println("- 800x600 or 1024x768 resolution screen resolution.");
                    out.println("<p>");
                    out.println("- Popup windows enabled.");
                    out.println("<p>");
                    out.println("- Javascript support enabled.");
                    out.println("</h1>");
                    out.println("<p>");
                    out.println("<span class=\"ahb12\">Tested web browsers:</span>");
                    out.println("<br>");
                    out.println("&nbsp&nbsp&nbsp<span class=\"ahb12\">1. </span><span class=\"ah14\">Mozilla (tm) 1.2.1 </span><span class=\"ahb12\">(</span><span class=\"ah14\">a.k.a., Netscape (tm) 5.0</span><span class=\"ahb12\">)</span>");
                    out.println("<br>");
                    out.println("&nbsp&nbsp&nbsp<span class=\"ahb12\">2. </span><span class=\"ah14\">Internet Explorer (tm) 5.0</span>");
                    out.println("<p>");
                    out.println("<h1 class=\"ahb12\">");
                    out.println("Please update or upgrade your system if required.");
                    out.println("</h1>");
                    HtmlClose1(out, UserSystemID, session);
                    gotomenu(out);
                    HtmlClose2(out);
                }
                if (Page_request.equals("faqPage")) {
                    out.println("<script src=\"js/navmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    HtmlSection1(out);
                    out.println("<img src=\"graphics/publogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                    out.println("<span class=\"title\">FAQs</span>");
                    HtmlSection2(out);
                    out.println("<span class=\"ahb12\">My system is slower than a Pentium (tm) 120 MHz.  Will ePICE still run?</span><br>");
                    doGetHtmlSection4(out);
                    out.println("<h1 class=\"ah10\">");
                    out.println("The <span class=\"ahyo10\">ePICE</span> web app was tested on a broad range of systems with certain browsers; a P120 with 32 MB RAM was the oldest machine in the lab. No attempt was made to test any system below a P120, so performance on anything slower may or may not be acceptable.");
                    doGetHtmlSection5(out);
                    out.println("<span class=\"ahb12\">Can I use a screen resolution other than 800x600 or 1024x768?</span><br>");
                    doGetHtmlSection4(out);
                    out.println("<h1 class=\"ah10\">");
                    out.println("The <span class=\"ahyo12\">ePICE</span> web app was designed using the 800x600 screen resolution. &nbsp Acceptable viewing is possible at 1024x768.&nbsp&nbsp");
                    out.println("Using a screen resolution below 800x600 or above 1024x768 may be less than optimal.");
                    doGetHtmlSection5(out);
                    out.println("<span class=\"ahb12\">Besides IE (v5.5 and up) and Mozilla (tm) (v1.2.1 and up), are other browsers supported?</span><br>");
                    doGetHtmlSection4(out);
                    out.println("<h1 class=\"ah10\">");
                    out.println("Depends. <span class=\"ahyo12\">ePICE</span> was evaluated and viewed in the lab using IE and Mozilla (tm) because these two products were consider to be the most commonly available in one variation or another. Most ISP's (to the best of our knowledge) do not restrict you to a specific browser, so it is possible to have multiple browsers installed on your system.  If the one you are using refuses to display ePICE correctly, download and install one of the tested browsers.");
                    doGetHtmlSection5(out);
                    out.println("<span class=\"ahb12\">Do I have to enable my browser's Javascript support?</span><br>");
                    doGetHtmlSection4(out);
                    out.println("<h1 class=\"ah10\">");
                    out.println("Yes. <span class=\"ahyo12\">ePICE</span> requires Javascript support.");
                    doGetHtmlSection5(out);
                    out.println("<span class=\"ahb12\">Do I have to enable my popup windows on my browser?</span><br>");
                    doGetHtmlSection4(out);
                    out.println("<h1 class=\"ah10\">");
                    out.println("Yes. <span class=\"ahyo12\">ePICE</span> requires popup window support.");
                    out.println("</h1>");
                    HtmlClose1(out, UserSystemID, session);
                    gotomenu(out);
                    HtmlClose2(out);
                }
                if (Page_request.equals("contactPage")) {
                    out.println("<script src=\"js/navmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    HtmlSection1(out);
                    out.println("<img src=\"graphics/publogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                    out.println("<span class=\"title\">Contact</span>&nbsp<span class=\"epicetitle\">ePICE</span>");
                    HtmlSection2(out);
                    out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                    out.println("<tr>");
                    out.println("<td valign=\"top\" bgcolor=\"000099\">");
                    out.println("<h1 class=\"ah12\"><span class=\"ahyo12\">ePICE</span> is very interested in any questions or comments you may have. &nbsp Please use the E-mail form below to");
                    out.println("contact us. &nbsp If a response is required, we will reply within 48 hours. &nbsp Thank you.");
                    out.println("</h1>");
                    out.println("<p>");
                    out.println("<!--");
                    out.println("<script src=\"js/formaction.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>ePICEEMail\" method=\"post\">");
                    out.println("-->");
                    out.println("<form action=\"ePICEEMail\" method=\"post\">");
                    out.println("<script src=\"js/onetextarea.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>name=\"Contacttext\"></textarea>");
                    out.println("<p>");
                    out.println("<input type=\"reset\" Value=\" Reset \">&nbsp&nbsp&nbsp<input type=\"Submit\" Value=\"Send E-mail\">");
                    out.println("</form>bbb skd");
                    sqlstmt.close();
                    HtmlClose1(out, UserSystemID, session);
                    gotomenu(out);
                    out.println("<p>");
                    out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/help.html')\">Help Link Here</a>");
                    HtmlClose2(out);
                }
                if (Page_request.equals("logginginPage")) {
                    String email = request.getParameter("Email");
                    String passwd = request.getParameter("Passwd");
                    String CheckPasswd = null;
                    String CheckUser = null;
                    String UserID = null;
                    String UserFName = null;
                    String UserLName = null;
                    String UserType = null;
                    String UserLastLogin = null;
                    String UserEmail = null;
                    String SessionDeptID = null;
                    ResultSet loginset = sqlstmt.executeQuery("select UserSystemID,UserEmail,UserPasswd,UserFName,UserLName,UserType from User where UserEmail=\'" + email + "\' and UserPasswd=\'" + passwd + "\'");
                    if (loginset.first() == false) {
                        out.println("<script src=\"js/navmenu4.js\" language=\"javascript\" type=\"text/javascript\">");
                    } else {
                        UserSystemID = loginset.getString("UserSystemID");
                        UserFName = loginset.getString("UserFName");
                        UserLName = loginset.getString("UserLName");
                        UserType = loginset.getString("UserType");
                        UserEmail = loginset.getString("UserEmail");
                        Control("new", UserSystemID, request, session);
                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        session.setAttribute("UserType", UserType);
                        if (UserType == null) {
                            out.println("<script src=\"js/navmenu1.js\" language=\"javascript\" type=\"text/javascript\">");
                        } else {
                            if (UserType.equals("Instructor")) {
                                out.println("<script src=\"js/navmenu3.js\" language=\"javascript\" type=\"text/javascript\">");
                            } else {
                                if (UserType.equals("sysadmin") || UserType.equals("AdvUser")) {
                                    out.println("<script src=\"js/sysnavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                                } else {
                                    if (UserType.startsWith("D")) {
                                        SessionDeptID = UserType.substring(1);
                                    } else {
                                        SessionDeptID = UserType;
                                    }
                                    session.setAttribute("DeptSystemID", SessionDeptID);
                                    out.println("<script src=\"js/adminnavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                                }
                            }
                        }
                    }
                    out.println("</script>");
                    out.println("<script src=\"js/checkloginform.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    HtmlSection1(out);
                    if (UserSystemID == null) {
                        out.println("<img src=\"graphics/publogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                    } else {
                        out.println("<img src=\"graphics/inlogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                    }
                    out.println("<span class=\"title\">Login Status</span>");
                    HtmlSection2(out);
                    out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" width=\"100%\" height=\"100%\">");
                    out.println("<tr>");
                    out.println("<span class=\"ahb16\">Status of your Login:</span><p>");
                    if (UserSystemID == null) {
                        out.println("<span class=\"ahyo18\">Failed!</span><p>");
                        out.println("<span class=\"ahyo14\">Submitted E-mail &#38 Password Combination does not exist in ePICE system.</span><p>");
                        out.println("<span class=\"ahb14\">Click the brower's </span>");
                        out.println("<span class=\"ah14\">\"Back\"</span> ");
                        out.println("<span class=\"ahb14\">button or any related link on this page and try again if you believe you reached this page due to erroneous login information.</span><p><br><br>");
                        out.println("<span class=\"ahyo12\">If you know that your login data is correct, but there has been a long hiatus since your last login, your account may have been deactivated.  Please contact your ePICE system administrator for reactivation.</span><p>");
                    } else {
                        out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<span class=\"ahb14\">Welcome,</span><p>");
                        out.println("<span class=\"ahy12\">&nbsp&nbsp " + UserFName + "</span><span class=\"ahb14\">!</span><p>");
                        if (SessionDeptID != null) {
                            out.println("<span class=\"ahb14\">Your Department is:</span><br><br>");
                            ResultSet deptset = sqlstmt.executeQuery("select DeptName from Department where DeptSystemID=\'" + (String) session.getAttribute("DeptSystemID") + "\'");
                            while (deptset.next()) {
                                session.setAttribute("DeptName", deptset.getString("DeptName"));
                                out.println("<span class=\"ahy12\">&nbsp&nbsp " + (String) session.getAttribute("DeptName") + "</span><p>");
                            }
                        }
                        out.println("<span class=\"ahb14\">Login System Time:</span><p>");
                        out.println("<span class=\"ahy12\">&nbsp&nbsp " + showDateTime(new Locale("en", "US")) + "</span><p><p>");
                        sqlstmt.executeUpdate("update User set UserLastLogin=CURRENT_TIMESTAMP where UserSystemID=\'" + UserSystemID + "\'");
                        out.println("</td>");
                        VerticalDivBar(out);
                        out.println("<td valign=\"top\" width=\"*\">");
                        if (UserType == null) {
                            out.println("<span class=\"ahb12\">If you are a first time user, you will need to select your courses using the Course Selection link in the navigation panel.</span><p>");
                            out.println("<span class=\"ahb12\">If you are a returning user, and you have already selected your courses, please use the navigation links in the navigation panel on the right side of the browser window to access them.</span><p>");
                        } else {
                            if (UserType.equals("Instructor")) {
                                out.println("<span class=\"ahb12\">The active sections of your instruction roster should be listed as navigation links on the right side of the browser window.</span><p>");
                                out.println("<span class=\"ahb12\">If there are no section links displayed, please contact the appropriate department ePICE administrator to update your instruction roster.</span><p>");
                            } else {
                                if (UserType.equals("sysadmin")) {
                                    out.println("<span class=\"ahb12\">You may now create or administer Departments for your institution.</span><p>");
                                    out.println("<span class=\"ahb12\">You may also create or administer Advanced Users to assist you with the administration of the ePICE system for your institution.</span><p>");
                                } else {
                                    out.println("<span class=\"ahb12\">You may now create or administer Courses, Course Sections, and Instructors.</span><p>");
                                    out.println("<span class=\"ahb12\">You may also create or administer Department Users to assist you with the administration of the ePICE system for your department.</span><p>");
                                }
                            }
                        }
                    }
                    HtmlClose1(out, UserSystemID, session);
                    gotomenu(out);
                    out.println("<p>");
                    if ((UserType != null) && UserType.equals("Instructor")) {
                        popcurrentinst(UserSystemID, sqlstmt, out, CourseCount, IncrementCount, session);
                    }
                    if ((UserSystemID != null) && (UserType == null)) {
                        session.setAttribute("UserFName", UserFName);
                        session.setAttribute("UserLName", UserLName);
                        session.setAttribute("UserEmail", UserEmail);
                        popcurrentcourses(UserSystemID, sqlstmt, out, CourseCount, IncrementCount, session);
                    }
                    out.println("<p>");
                    out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/loginstatushelp.html')\">Login Status Help</a>");
                    HtmlClose2(out);
                }
                if (Page_request.equals("logoutPage")) {
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<meta http-equiv=\"refresh\" content=\"3; url=index.html\">");
                    out.println("<title>ePICE Logout</title>");
                    out.println("<link rel=stylesheet href=\"css/epice1.css\" type=\"text/css\">");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">");
                    out.println("<tr>");
                    out.println("<td valign=\"top\" width=\"*\">");
                    out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">");
                    out.println("<tr>");
                    out.println("<td valign=\"middle\" height=\"68\" width=\"*\">");
                    out.println("<img src=\"graphics/publogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                    out.println("<span class=\"title\">Logout</span>");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td valign=\"middle\" align=\"center\" bgcolor=\"000099\" colspan=\"2\">");
                    out.println("<span class=\"ahb18\">You are now logged out.<P>Thanks for using <span class=\"ahyo18\">ePICE</span>!</span>");
                    HtmlClose2(out);
                    out.println("</table>");
                    out.println("</td>");
                    out.println("</tr>");
                    try {
                        session.invalidate();
                    } catch (IllegalStateException ISE) {
                        System.out.println("An attempt was made to access session data with a 'kill'" + "request after the session was invalidated.");
                    }
                }
                if (Page_request.equals("registerstudentPage")) {
                    out.println("<html>");
                    out.println("<script src=\"js/ePICEChk.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    out.println("<script src=\"js/navmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    out.println("<head>");
                    out.println("<title>ePICE Account Registration</title>");
                    out.println("<link rel=stylesheet href=\"css/epice1.css\" type=\"text/css\">");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" width=\"100%\" height=\"100%\">");
                    out.println("<tr>");
                    out.println("<td valign=\"top\" width=\"*\">");
                    out.println("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" width=\"100%\" height=\"100%\">");
                    out.println("<tr>");
                    out.println("<td valign=\"middle\" height=\"68\" colspan=\"2\">");
                    out.println("<img src=\"graphics/publogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                    out.println("<span class=\"title\">Account Registration</span>");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td valign=\"middle\" height=\"30\" colspan=\"2\" bgcolor=\"000099\" >");
                    out.println("<h1 class=\"ah12\">");
                    out.println("<span class=\"ahb12\">Please submit your information below. &nbsp Any special conditions are specified in notes to the right of the textboxes.&nbsp All fields are required.</span><p>");
                    out.println("</h1>");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td valign=\"top\" bgcolor=\"000099\" width=\"25%\">");
                    out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"2\" width=\"100%\" height=\"100%\">");
                    out.println("<form action=\"ePICE?page_request=registerstudentPage\" method=\"post\" name=\"RegForm\" onsubmit=\"return CheckSubmittedValues(\'registerstudentPage\')\">");
                    out.println("<tr>");
                    out.println("<td valign=\"middle\" height=\"36\" align=\"right\" width=\"25%\">");
                    out.println("<span class=\"ah10\">&nbsp First Name:</span>");
                    out.println("</td>");
                    out.println("<td valign=\"middle\" height=\"36\">");
                    out.println("<input type=\"text\" name=\"Fname\" maxlength=\"32\">");
                    out.println("<script language=\"JavaScript\">");
                    out.println("document.RegForm.Fname.focus();");
                    out.println("</script>");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td valign=\"middle\" height=\"36\" align=\"right\" width=\"25%\">");
                    out.println("<span class=\"ah10\">&nbsp Last Name:</span>");
                    out.println("</td valign=\"middle\" height=\"36\">");
                    out.println("<td>");
                    out.println("<input type=\"text\" name=\"Lname\" maxlength=\"32\">");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td valign=\"middle\" height=\"36\" align=\"right\" width=\"25%\">");
                    out.println("<span class=\"ah10\">&nbsp E-mail:</span>");
                    out.println("</td>");
                    out.println("<td valign=\"middle\" height=\"36\">");
                    out.println("<input type=\"text\" name=\"Email\" maxlength=\"127\">");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td valign=\"middle\" height=\"36\" align=\"right\" width=\"25%\">");
                    out.println("<span class=\"ah10\">&nbsp Password:</span>");
                    out.println("</td>");
                    out.println("<td valign=\"middle\" height=\"36\">");
                    out.println("<input type=\"text\" name=\"Passwd\" maxlength=\"32\"><span class=\"ah10\">&nbsp&nbsp(8 characters min)</span>");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td valign=\"middle\" height=\"36\" align=\"right\" width=\"25%\">");
                    out.println("<span class=\"ah10\">&nbsp Re-enter Password:</span>");
                    out.println("</td>");
                    out.println("<td valign=\"middle\" height=\"36\">");
                    out.println("<input type=\"text\" name=\"Passwd2\" maxlength=\"32\"><span class=\"ah10\">&nbsp&nbsp(Passwords must match)</span>");
                    out.println("</td>");
                    out.println("<tr>");
                    out.println("</tr>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td height=\"36\" width=\"25%\">");
                    out.println("</td>");
                    out.println("<td valign=\"bottom\" height=\"36\" width=\"*\">");
                    out.println("<input type=\"reset\" value=\" Reset \">&nbsp&nbsp&nbsp");
                    out.println("<input type=\"submit\" name=\"submit\" value=\"Submit\">");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("</form>");
                    out.println("</table>");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("</table>");
                    out.println("</td>");
                    out.println("<td valign=\"top\" width=\"160\" bgcolor=\"FFFFFF\">");
                    out.println("<span class=\"ahdb10\">Go To:</span><br>");
                    out.println("<script language=\"javascript\" type=\"text/javascript\">");
                    out.println("showMenu();");
                    out.println("</script>");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("</table>");
                    out.println("</body>");
                    out.println("</html>");
                }
                if (Page_request.equals("epicetemplate")) {
                    out.println("<script src=\"js/navmenu3.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    HtmlSection1(out);
                    out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                    out.println("<span class=\"epicetitle\">ePICE</span> <span class=\"title\">Insert Title Here</span>");
                    HtmlSection2(out);
                    out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                    out.println("<tr>");
                    out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                    out.println("</script>");
                    out.println("</td>");
                    VerticalDivBar(out);
                    out.println("<td valign=\"top\" width=\"*\">");
                    sqlstmt.close();
                    HtmlClose1(out, UserSystemID, session);
                    gotomenu(out);
                    out.println("<p>");
                    out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/help.html')\">Help Link Here</a>");
                    HtmlClose2(out);
                }
            }
            sqlstmt.close();
            if (outputbody > 0) {
                out.println("</body>");
            }
            out.println("</html>");
        } catch (Exception e) {
            sendErrorToClient(out, e);
            log("Error in doGet() method.", e);
        } finally {
            try {
                out.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
	doPost() method called in response to POST request.
	*/
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String Page_request = request.getParameter("page_request");
        String DeptSystemID = null;
        String UserSystemID = (String) session.getAttribute("UserSystemID");
        String CourseSystemID = request.getParameter("CourseSystemID");
        String SectionSystemID = request.getParameter("SectionSystemID");
        String SelectedCourse = (String) session.getAttribute("SelectedCourse");
        byte CourseCount = 0;
        byte IncrementCount = 1;
        int MAX_SIZE = 102400;
        String successMessage;
        String process = null;
        try {
            Statement sqlstmt = connectDB.createStatement();
            if (Page_request.equals("logginginPage")) {
                if (request.getContentLength() > 8 * 1024) {
                    out.println("<head>");
                    out.println("<title>Too big</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Error - content length &gt;8k not allowed</h1>");
                } else {
                    doGet(request, response);
                }
            }
            out.println("<html>");
            if (Page_request.equals("logoutPage")) {
                if (request.getContentLength() > 8 * 1024) {
                    response.setContentType("text/html");
                    out.println("<head>");
                    out.println("<title>Too big</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>Error - content length &gt;8k not allowed</h1>");
                } else {
                    doGet(request, response);
                }
            }
            if (Page_request.equals("registerstudentPage")) {
                String fname = request.getParameter("Fname");
                String lname = request.getParameter("Lname");
                String email = request.getParameter("Email");
                String passwd = request.getParameter("Passwd");
                String verifyquest = request.getParameter("Verifyquest");
                String countersign = request.getParameter("Countersign");
                String studentid = request.getParameter("Studentid");
                out.println("<script src=\"js/navmenu1.js\" language=\"javascript\" type=\"text/javascript\">");
                out.println("</script>");
                HtmlSection1(out);
                out.println("<img src=\"graphics/publogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                out.println("<span class=\"title\">You are Registered!</span>");
                HtmlSection2(out);
                out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                out.println("<tr>");
                out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                out.println("</script>");
                out.println("<span class=\"ahb12\">First Name:</span> <span class=\"ah12\">" + fname + "</span><br>");
                out.println("<span class=\"ahb12\">Last Name:</span> <span class=\"ah12\">" + lname + "</span><br>");
                out.println("<span class=\"ahb12\">E-mail:</span> <span class=\"ah12\">" + email + "</span><br>");
                out.println("<p>");
                String register1sql = "insert into User (UserEmail,UserPasswd,UserFName,UserLName,UserRegTimeStamp) values ('" + email + "','" + passwd + "','" + fname + "','" + lname + "',CURRENT_TIMESTAMP)";
                String register2sql = "select UserSystemID from User where UserEmail='" + email + "'";
                sqlstmt.executeUpdate(register1sql);
                ResultSet sysidset = sqlstmt.executeQuery(register2sql);
                String sysidVar = null;
                while (sysidset.next()) {
                    sysidVar = sysidset.getString("UserSystemID");
                }
                UserSystemID = sysidVar;
                String UserFName = fname;
                String UserLName = lname;
                String UserEMail = email;
                Control("new", UserSystemID, request, session);
                session.setAttribute("UserFName", UserFName);
                out.println("<span class=\"ah14\">" + fname + "</span><span class=\"ahb14\">, you are now registered and logged in!</span><p>");
                out.println("</td>");
                VerticalDivBar(out);
                out.println("<td valign=\"top\" width=\"*\">");
                HtmlClose1(out, UserSystemID, session);
                gotomenu(out);
                out.println("<p>");
                out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/help.html')\">Help Link Here</a>");
                HtmlClose2(out);
            }
            out.println("</body></html>");
        } catch (Exception e) {
            sendErrorToClient(out, e);
            log("Error in doPost() method.", e);
        } finally {
            try {
                out.close();
            } catch (Exception ignored) {
            }
        }
    }

    private synchronized void HtmlSection1(PrintWriter out) {
        out.println("<head>");
        out.println("<title>ePICE Web Application</title>");
        out.println("<link rel=stylesheet href=\"css/epice1.css\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">");
        out.println("<tr>");
        out.println("<td valign=\"top\" width=\"*\">");
        out.println("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" width=\"100%\" height=\"100%\">");
        out.println("<tr>");
        out.println("<td valign=\"middle\" height=\"68\" width=\"*\">");
    }

    private synchronized void HtmlSection2(PrintWriter out) {
        out.println("</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td valign=\"top\" bgcolor=\"000099\" colspan=\"2\">");
    }

    private synchronized void doGetHtmlSection3(PrintWriter out) {
        out.println("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" width=\"100%\" height=\"100%\">");
        out.println("<tr>");
        out.println("<td valign=\"top\" bgcolor=\"000099\" colspan=\"2\">");
    }

    private synchronized void doGetHtmlSection4(PrintWriter out) {
        out.println("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" width=\"*\" height=\"*\">");
        out.println("<tr>");
        out.println("<td width=\"25\">");
        out.println("</td>");
        out.println("<td valign=\"middle\" width=\"*\">");
    }

    private synchronized void doGetHtmlSection5(PrintWriter out) {
        out.println("</h1>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<p>");
    }

    private synchronized void doGetHtmlSection6(PrintWriter out) {
        out.println("</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td valign=\"middle\" align=\"right\" width=\"25%\">");
    }

    private synchronized void VerticalDivBar(PrintWriter out) {
        out.println("<td valign=\"top\" bgcolor=\"000099\" width=2 height=\"100%\">");
        out.println("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\"width=\"100%\" height=\"100%\">");
        out.println("<td valign=\"top\" bgcolor=\"000099\" width=0 height=\"100%\">");
        out.println("</td>");
        out.println("</table>");
        out.println("</td>");
    }

    private synchronized void HtmlClose1(PrintWriter out, String UserSystemID, HttpSession session) {
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("</td>");
        out.println("<td valign=\"top\" width=\"160\" bgcolor=\"FFFFFF\">");
        out.println("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\"width=\"100%\" height=\"100%\">");
        if ((String) session.getAttribute("UserSystemID") != null) {
            out.println("<tr>");
            out.println("<td valign=\"top\" bgcolor=\"000099\" height=\"68\">");
            if ((String) session.getAttribute("CurrentTerm") != null) {
                out.println("<div align=\"right\"><span class=\"ah12\">" + (String) session.getAttribute("CurrentTerm") + "</span></div>");
            } else {
                out.println("<span class=\"ah12\"><br></span>");
            }
            if ((String) session.getAttribute("SelectedCourseName") != null) {
                out.println("<div align=\"right\"><span class=\"ahb12\">" + (String) session.getAttribute("SelectedCourseName") + "</span></div>");
            } else {
                out.println("<span class=\"ah12\"><br></span>");
            }
            out.println("<div align=\"right\"><a class=\"ahb12\" href=\"ePICE?page_request=logoutPage\"><span class=\"ahyo12\">ePICE</span> Log Out</a></div>");
            out.println("</td>");
            out.println("</tr>");
            out.println("<tr>");
        }
        out.println("<td valign=\"top\" bgcolor=\"FFFFFF\">");
    }

    private synchronized void HtmlClose2(PrintWriter out) {
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
    }

    private synchronized void popcurrentcourses(String UserSystemID, Statement sqlstmt, PrintWriter out, byte CourseCount, byte IncrementCount, HttpSession session) {
        String getcoursesql = "select Course.CoursePrefix,Course.CourseNum,Section.SectionSystemID,Section.SectionNum from Course,Section,Enrolls,CurrentTerm where Enrolls.UserSystemID='" + UserSystemID + "' and Section.SectionSystemID=Enrolls.SectionSystemID and Course.CourseSystemID=Section.CourseSystemID and Enrolls.EnrollStatus='y' and Section.Term = CurrentTerm.Term order by CoursePrefix,CourseNum,SectionNum";
        try {
            ResultSet courseset = sqlstmt.executeQuery(getcoursesql);
            out.println("<span class=\"ahdb10\">Current Courses:</span><br>");
            if (courseset.first() == false) {
                out.println("&nbsp&nbsp<span class=\"ahdb10\">None</span>");
            } else {
                String CurrentSectionVar = null;
                String CurrentCoursePrefix = null;
                String CurrentCourseNum = null;
                String CurrentSectionNum = null;
                courseset.beforeFirst();
                while (courseset.next()) {
                    CurrentSectionVar = courseset.getString("SectionSystemID");
                    CurrentCoursePrefix = courseset.getString("CoursePrefix");
                    CurrentCourseNum = courseset.getString("CourseNum");
                    CurrentSectionNum = courseset.getString("SectionNum");
                    out.println("&nbsp&nbsp<a class=\"ah10\" target=\"_top\" href=\"ePICE1?page_request=announcementsPage&SelectedCourse=" + CurrentSectionVar + "\">" + CurrentCoursePrefix + " " + CurrentCourseNum + " s." + CurrentSectionNum + "</a><br>");
                    CourseCount += 1;
                    if (CourseCount == IncrementCount) {
                        session.setAttribute("Course" + CourseCount, CurrentSectionVar);
                        session.setAttribute("Course" + CourseCount + "N", CurrentCoursePrefix + " " + CurrentCourseNum + " s." + CurrentSectionNum);
                        IncrementCount += 1;
                    }
                }
                String StrCourseCount = String.valueOf(CourseCount);
                session.setAttribute("CourseCount", StrCourseCount);
            }
        } catch (Exception e) {
            log("Error in doPost() method.", e);
        }
    }

    private synchronized void CourseCountMethod(HttpSession session, byte CourseCount, byte IncrementCount, String CurrentCourseVar) {
        if (CourseCount == IncrementCount) {
            session.setAttribute("Course" + CourseCount, CurrentCourseVar);
            IncrementCount += 1;
        }
    }

    private synchronized void popcurrentinst(String UserSystemID, Statement sqlstmt, PrintWriter out, byte CourseCount, byte IncrementCount, HttpSession session) {
        String getinstsql = "select Course.CoursePrefix,Course.CourseNum,Section.SectionSystemID,Section.SectionNum from Course,Section,CurrentTerm where Section.UserSystemID='" + UserSystemID + "' and Course.CourseSystemID = Section.CourseSystemID and Section.Term = CurrentTerm.Term order by CoursePrefix,CourseNum,SectionNum";
        try {
            ResultSet instset = sqlstmt.executeQuery(getinstsql);
            out.println("<span class=\"ahdb10\">Current Instruction Roster:</span><br>");
            if (instset.first() == false) {
                out.println("&nbsp&nbsp<span class=\"ahdb10\">None</span>");
            } else {
                String CurrentSectionVar = null;
                String CurrentCoursePrefix = null;
                String CurrentCourseNum = null;
                String CurrentSectionNum = null;
                instset.beforeFirst();
                while (instset.next()) {
                    CurrentSectionVar = instset.getString("SectionSystemID");
                    CurrentCoursePrefix = instset.getString("CoursePrefix");
                    CurrentCourseNum = instset.getString("CourseNum");
                    CurrentSectionNum = instset.getString("SectionNum");
                    out.println("&nbsp&nbsp<a class=\"ah10\" href=\"ePICE2?page_request=currenttermPage&SelectedCourse=" + CurrentSectionVar + "\">" + CurrentCoursePrefix + " " + CurrentCourseNum + " s." + CurrentSectionNum + "</a><br>");
                    CourseCount += 1;
                    if (CourseCount == IncrementCount) {
                        session.setAttribute("Course" + CourseCount, CurrentSectionVar);
                        session.setAttribute("Course" + CourseCount + "N", CurrentCoursePrefix + " " + CurrentCourseNum + " s." + CurrentSectionNum);
                        IncrementCount += 1;
                    }
                }
                String StrCourseCount = String.valueOf(CourseCount);
                session.setAttribute("CourseCount", StrCourseCount);
            }
        } catch (Exception e) {
            log("Error in doPost() method.", e);
        }
    }

    private synchronized void gotomenu(PrintWriter out) {
        out.println("<span class=\"ahdb10\">Go To:</span><br>");
        out.println("<script language=\"javascript\" type=\"text/javascript\">");
        out.println("showMenu();");
        out.println("</script>");
    }

    private synchronized void InsertColSpacing1(PrintWriter out) {
        out.println("</td>");
        out.println("<td >");
    }

    private void sendErrorToClient(PrintWriter out, Exception e) {
        StringWriter stringError = new StringWriter();
        PrintWriter printError = new PrintWriter(stringError);
        e.printStackTrace(printError);
        String stackTrace = stringError.toString();
        out.println("<html><title>Error</title><body>");
        out.println("<h1>Servlet Error</h1><h4>Error</h4>" + e + "<H4>Stack Trace</H4>" + stackTrace + "</body></html>");
        System.out.println("Servlet error: " + stackTrace);
    }
}

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

public class ePICE2 extends HttpServlet {

    Connection connectDB = null;

    public String FileName = null, FilePath = null;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try {
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
        } catch (Exception E) {
            out.println("Unable to load driver.");
            E.printStackTrace();
        }
        try {
            String UserSystemID = (String) session.getAttribute("UserSystemID");
            if (UserSystemID != null) {
                String PathDB = getInitParameter("PathDB");
                String UserDB = getInitParameter("UserDB");
                String PassDB = getInitParameter("PassDB");
                String Page_request = request.getParameter("page_request");
                String dbURL = "jdbc:mysql://" + PathDB + "?user=" + UserDB + "&password=" + PassDB;
                connectDB = DriverManager.getConnection(dbURL);
                byte outputbody = 1;
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
                    byte CourseCount = 0;
                    byte IncrementCount = 1;
                    if (Page_request.equals("collectassignmentsPage")) {
                        SelectedCourse = (String) session.getAttribute("SelectedCourse");
                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        HtmlSection1(out);
                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                        out.println("<span class=\"title\">Collect Assignments</span>");
                        HtmlSection2(out);
                        out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                        out.println("<tr>");
                        out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        String sql = "SELECT AssignmentSystemID,AssignmentName FROM Assignment " + "WHERE SectionSystemID=" + SelectedCourse + " ORDER BY AssignmentPostTime";
                        ResultSet resultset = sqlstmt.executeQuery(sql);
                        if (resultset.first() == false) {
                            out.println("<span class=\"ahyo14\">No assignments have been given so no student submissions are available for collection.</span>");
                        } else {
                            out.println("<span class=\"ahb14\">Select Assignment for collection:</span>");
                            out.println("<br><br>");
                            out.println("<form method=\"post\" action=\"ePICE2?page_request=collectassignmentsPage\">");
                            out.println("<select name=\"AssignmentSystemID\">");
                            String AssignmentSystemID = null;
                            String AssignmentName = null;
                            resultset.beforeFirst();
                            while (resultset.next()) {
                                AssignmentSystemID = resultset.getString("AssignmentSystemID");
                                AssignmentName = resultset.getString("AssignmentName");
                                out.println("<option value=\"" + AssignmentSystemID + "\">" + AssignmentName + "</option>");
                            }
                            resultset.close();
                            sqlstmt.close();
                            out.println("</select>");
                            session.setAttribute("process", "collect");
                            out.println("<input type=\"submit\" NAME=\"Go\" value=\"Go\"> ");
                            out.println("</form>");
                        }
                        out.println("</td>");
                        VerticalDivBar(out);
                        out.println("<td valign=\"top\" width=\"*\">");
                        sqlstmt.close();
                        HtmlClose1(out, session);
                        gotomenu(out);
                        out.println("<p>");
                        out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/collectassignmenthelp.html')\">Collect Assignments Help</a>");
                        HtmlClose2(out);
                    }
                    if (Page_request.equals("currenttermPage")) {
                        SelectedCourse = request.getParameter("SelectedCourse");
                        session.setAttribute("SelectedCourse", SelectedCourse);
                        String StrCourseCount = (String) session.getAttribute("CourseCount");
                        if (StrCourseCount == null) {
                            StrCourseCount = "0";
                        }
                        byte IncrementStep = 1;
                        Integer SessionCourseCount = Integer.valueOf(StrCourseCount);
                        if (SessionCourseCount.intValue() > 0) {
                            while (SessionCourseCount.intValue() >= IncrementStep) {
                                if (SelectedCourse.equals((String) session.getAttribute("Course" + IncrementStep))) {
                                    session.setAttribute("SelectedCourseName", (String) session.getAttribute("Course" + IncrementStep + "N"));
                                }
                                IncrementStep += 1;
                            }
                        }
                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/antextwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/textwin400200.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        HtmlSection1(out);
                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                        out.println("<span class=\"title\">Current Term</span>");
                        HtmlSection2(out);
                        doGetHtmlSection3(out);
                        out.println("<span class=\"ahb14\">Announcement(s) on file:</span><p>");
                        String getannouncesql = "select AnnounceHeader,AnnounceText,AnnounceTimeStamp from Announcement where SectionSystemID='" + SelectedCourse + "' order by AnnounceTimeStamp desc";
                        ResultSet announceset = sqlstmt.executeQuery(getannouncesql);
                        if (announceset.first() == false) {
                            out.println("&nbsp&nbsp <span class=\"ahyo14\">There are no announcements posted.</span>");
                        } else {
                            String AnheaderVar = null;
                            String AntextVar = null;
                            String AntstampVar = null;
                            announceset.beforeFirst();
                            while (announceset.next()) {
                                AnheaderVar = announceset.getString("AnnounceHeader");
                                AntextVar = announceset.getString("AnnounceText");
                                AntstampVar = announceset.getString("AnnounceTimeStamp");
                                out.println("<span class=\"ahyo12\">" + AntstampVar + "</span><br>");
                                out.println("&nbsp&nbsp <a class=\"ah12\" href=\"javascript:text_win_400_200('" + AntextVar + "')\">");
                                out.println("<span class=\"ah12\">" + AnheaderVar + "</span></a><br><br>");
                            }
                        }
                        sqlstmt.close();
                        HtmlClose1(out, session);
                        gotomenu(out);
                        currentinst(out, session);
                        out.println("<p>&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/currenttermhelp.html')\">Current Term Help</a>");
                        HtmlClose2(out);
                    }
                    if (Page_request.equals("postannouncementPage")) {
                        SelectedCourse = (String) session.getAttribute("SelectedCourse");
                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/textwin400200.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        HtmlSection1(out);
                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                        out.println("<span class=\"title\">Post Announcement</span>");
                        HtmlSection2(out);
                        out.println("<form action=\"ePICE2?page_request=postannouncementPage\" method=\"post\" name=\"AnnForm\" onsubmit=\"return CheckSubmittedValues(\'postannouncementPage\')\">");
                        out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                        out.println("<tr>");
                        out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<span class=\"ahb14\">Announcement(s) on file:</span><p>");
                        String getannouncesql = "select AnnounceHeader,AnnounceText,AnnounceTimeStamp from Announcement where SectionSystemID = '" + SelectedCourse + "' order by AnnounceTimeStamp desc";
                        ResultSet announceset = sqlstmt.executeQuery(getannouncesql);
                        if (announceset.first() == false) {
                            out.println("&nbsp&nbsp<span class=\"ahyo14\">There are no announcements posted.</span>");
                        } else {
                            String AnheaderVar = null;
                            String AntextVar = null;
                            String AntstampVar = null;
                            announceset.beforeFirst();
                            while (announceset.next()) {
                                AnheaderVar = announceset.getString("AnnounceHeader");
                                AntextVar = announceset.getString("AnnounceText");
                                AntextVar.replaceAll("'", "\"'");
                                AntstampVar = announceset.getString("AnnounceTimeStamp");
                                out.println("<span class=\"ahyo12\">" + AntstampVar + "</span><br>");
                                out.println("&nbsp&nbsp <a class=\"ah12\" href=\"javascript:text_win_400_200('" + AntextVar + "')\">");
                                out.println("<span class=\"ah12\">" + AnheaderVar + "</span></a><br><br>");
                            }
                        }
                        sqlstmt.close();
                        out.println("</td>");
                        VerticalDivBar(out);
                        out.println("<td valign=\"top\" width=\"*\">");
                        out.println("<span class=\"ahb14\">Post New Announcement</span>");
                        out.println("<p>");
                        out.println("<span class=\"ah10\">Announcement Header:</span><br>");
                        out.println("<script src=\"js/128dyntextbox.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script language=\"JavaScript\">");
                        out.println("document.AnnForm.sourceheader.focus();");
                        out.println("</script>");
                        out.println("<p>");
                        out.println("<span class=\"ah10\">Announcement Text:</span><br>");
                        out.println("<script src=\"js/dyntextarea.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script> name=\"Antext\"></textarea>");
                        out.println("<p>");
                        out.println("<input type=\"reset\" Value=\" Reset \">&nbsp&nbsp&nbsp<input type=\"Submit\" Value=\"Submit\">");
                        out.println("</form>");
                        HtmlClose1(out, session);
                        gotomenu(out);
                        out.println("<p>");
                        currentinst(out, session);
                        out.println("<p>");
                        out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postannouncehelp.html')\">Post Announcement Help</a>");
                        HtmlClose2(out);
                    }
                    if (Page_request.equals("postassignmentPage")) {
                        SelectedCourse = (String) session.getAttribute("SelectedCourse");
                        out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        HtmlSection1(out);
                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                        out.println("<span class=\"title\">Post Assignment</span>");
                        HtmlSection2(out);
                        doGetHtmlSection3(out);
                        out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<span class=\"ahb14\">Assignment(s) on file:</span><p>");
                        String getassignmentsql = "select AssignmentName,AssignmentPostTime from Assignment where SectionSystemID = '" + SelectedCourse + "' order by AssignmentPostTime";
                        ResultSet assignmentset = sqlstmt.executeQuery(getassignmentsql);
                        if (assignmentset.first() == false) {
                            out.println("&nbsp&nbsp<span class=\"ahyo14\">There are no assignments posted.</span>");
                        } else {
                            String AnameVar = null;
                            String AposttimeVar = null;
                            assignmentset.beforeFirst();
                            while (assignmentset.next()) {
                                AnameVar = assignmentset.getString("AssignmentName");
                                AposttimeVar = assignmentset.getString("AssignmentPostTime");
                                out.println("<span class=\"ahyo12\">" + AposttimeVar + "</span><br><span class=\"ah12\">&nbsp&nbsp " + AnameVar + "</span><br><br>");
                            }
                        }
                        sqlstmt.close();
                        out.println("</td>");
                        VerticalDivBar(out);
                        out.println("<td valign=\"top\" width=\"*\">");
                        out.println("<form action=\"ePICE2?page_request=postassignmentPage\" name=\"postassignmentForm\" method=\"post\" onsubmit=\"return CheckSubmittedValues(\'postassignmentPage\')\">");
                        out.println("<span class=\"ahb14\">Post New Assignment</span>");
                        out.println("<p>");
                        out.println("<span class=\"ah10\">Assignment Name: (must be unique)</span>");
                        out.println("<script src=\"js/128dyntextbox.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<input type=\"hidden\" name=\"process\" value=\"getSourceHeader\"><p>");
                        out.println("<input type=\"reset\" value=\" Reset \">&nbsp&nbsp&nbsp<input type=\"Submit\" Value=\"Submit\">");
                        out.println("</form>");
                        out.println("<script language=\"JavaScript\">");
                        out.println("document.postassignmentForm.sourceheader.focus();");
                        out.println("</script>");
                        HtmlClose1(out, session);
                        gotomenu(out);
                        currentinst(out, session);
                        out.println("<p>&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postassignmenthelp.html')\">Post Assignment Help</a>");
                        HtmlClose2(out);
                    }
                    if (Page_request.equals("postmaterialPage")) {
                        SelectedCourse = (String) session.getAttribute("SelectedCourse");
                        out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        HtmlSection1(out);
                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                        out.println("<span class=\"title\">Post Material</span>");
                        HtmlSection2(out);
                        doGetHtmlSection3(out);
                        out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<span class=\"ahb14\">Material(s) on file:</span><p>");
                        String getmaterialsql = "select MaterialName,MaterialPostTime from Material where SectionSystemID = '" + SelectedCourse + "' order by MaterialPostTime";
                        ResultSet materialset = sqlstmt.executeQuery(getmaterialsql);
                        if (materialset.first() == false) {
                            out.println("&nbsp&nbsp<span class=\"ahyo14\">There are no materials posted.</span>");
                        } else {
                            String MnameVar = null;
                            String MposttimeVar = null;
                            materialset.beforeFirst();
                            while (materialset.next()) {
                                MnameVar = materialset.getString("MaterialName");
                                MposttimeVar = materialset.getString("MaterialPostTime");
                                out.println("<span class=\"ahyo12\">" + MposttimeVar + "</span><br><span class=\"ah12\">&nbsp&nbsp " + MnameVar + "</span><br><br>");
                            }
                        }
                        sqlstmt.close();
                        out.println("</td>");
                        VerticalDivBar(out);
                        out.println("<td valign=\"top\" width=\"*\">");
                        out.println("<form action=\"ePICE2?page_request=postmaterialPage\" name=\"postmaterialForm\" method=\"post\" onsubmit=\"return CheckSubmittedValues(\'postmaterialPage\')\">");
                        out.println("<span class=\"ahb14\">Post New Material</span>");
                        out.println("<p>");
                        out.println("<span class=\"ah10\">Material Name: (must be unique)</span>");
                        out.println("<script src=\"js/128dyntextbox.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script><p>");
                        out.println("<input type=\"hidden\" name=\"process\" value=\"getSourceHeader\">");
                        out.println("<input type=\"reset\" value=\" Reset \">&nbsp&nbsp&nbsp<input type=\"Submit\" Value=\"Submit\">");
                        out.println("</form>");
                        out.println("<script language=\"JavaScript\">");
                        out.println("document.postmaterialForm.sourceheader.focus();");
                        out.println("</script>");
                        HtmlClose1(out, session);
                        gotomenu(out);
                        currentinst(out, session);
                        out.println("<p>&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postmaterialhelp.html')\">Post Material Help</a>");
                        HtmlClose2(out);
                    }
                    if (Page_request.equals("postscoresPage")) {
                        out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        HtmlSection1(out);
                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp<span class=\"title\">");
                        out.println(" Post Scores");
                        out.println("</span>");
                        out.println("</td>");
                        out.println("</tr>");
                        out.println("<tr>");
                        out.println("<td valign=\"top\" bgcolor=\"000099\">");
                        out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                        out.println("<tr>");
                        out.println("<span class=\"ahb14\">Select the Assignment for Posting Scores:</span><p>");
                        ResultSet assignmentset = sqlstmt.executeQuery("select AssignmentName,AssignmentSystemID from Assignment where SectionSystemID=\'" + (String) session.getAttribute("SelectedCourse") + "\' order by AssignmentName");
                        if (assignmentset.first() == false) {
                            out.println("<span class=\'ahyo14\'>No assignments have been posted for this section.</span><p>");
                            out.println("<span class=\'ahb12\'>You will need to have at least one assignment with at least one student submission before you can post any score.</span><p>");
                            out.println("<span class=\'ahb12\'>Please navigate to the ePICE Post Assignment page or select another ePICE function.</span><p>");
                        } else {
                            String CurrentAssignmentSystemID = null;
                            String CurrentAssignmentName = null;
                            assignmentset.beforeFirst();
                            out.println("<form action=\"ePICE2?page_request=postscoresPage\" method=\"post\" name=\"PostScoresForm\">");
                            out.println("<span class=\"ahb10\">Available Assignments:</span><br>");
                            out.println("<select name=\"assignmentchoice\" size=\"15\">");
                            out.println("<option selected>&lt select assignment &gt");
                            while (assignmentset.next()) {
                                CurrentAssignmentName = assignmentset.getString("AssignmentName");
                                CurrentAssignmentSystemID = assignmentset.getString("AssignmentSystemID");
                                out.println("<option value=\"" + CurrentAssignmentSystemID + "\">" + CurrentAssignmentName);
                            }
                            out.println("</select>");
                            out.println("<input type=\"hidden\" name=\"process\" value=\"enterscores\">");
                            out.println("<input type=\"submit\" value=\"Get Scores\">");
                            out.println("</form>");
                        }
                        HtmlClose1(out, session);
                        gotomenu(out);
                        out.println("<p>");
                        currentinst(out, session);
                        out.println("<p>");
                        if (Page_request.equals("PostScores")) {
                            out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postscoreshelp.html')\">Post Scores Roster Help</a>");
                        }
                        sqlstmt.close();
                        out.println("</td>");
                        out.println("</tr>");
                        out.println("</table>");
                    }
                    if (Page_request.equals("replyquestionPage")) {
                        String questid = request.getParameter("Questid");
                        SelectedCourse = (String) session.getAttribute("SelectedCourse");
                        out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        HtmlSection1(out);
                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                        out.println("<span class=\"title\">Reply to Questions</span>");
                        HtmlSection2(out);
                        out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                        out.println("<tr>");
                        out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<span class=\"ahb16\">Question(s) on file:</span><p>");
                        String getquestionsql = "select QuestionSystemID,QuestionHeader,QuestionTimeStamp,PostFlag from Question where SectionSystemID = '" + SelectedCourse + "' order by QuestionTimeStamp desc";
                        ResultSet questionset = sqlstmt.executeQuery(getquestionsql);
                        if (questionset.first() == false) {
                            out.println("&nbsp&nbsp<span class=\"ahyo14\">There are no questions submitted.</span>");
                        } else {
                            String QuestsystemidVar = null;
                            String QuestheaderVar = null;
                            String QuesttstampVar = null;
                            int QuestreplyVar = 0;
                            questionset.beforeFirst();
                            while (questionset.next()) {
                                QuestsystemidVar = questionset.getString("QuestionSystemID");
                                QuestheaderVar = questionset.getString("QuestionHeader");
                                QuesttstampVar = questionset.getString("QuestionTimeStamp");
                                QuestreplyVar = questionset.getInt("PostFlag");
                                out.println("<span class=\"ahyo12\">" + QuesttstampVar + "</span><br>");
                                out.println("&nbsp&nbsp <a class=\"ah12\" href=\"ePICE2?page_request=replyquestionPage&Questid=" + QuestsystemidVar + "\"");
                                out.println("<span class=\"ah12\">" + QuestheaderVar + "</span>");
                                if (QuestreplyVar == 1) {
                                    out.println("<br>(Reply Posted)");
                                }
                                out.println("</a><br><br>");
                            }
                        }
                        out.println("</td>");
                        VerticalDivBar(out);
                        out.println("<td valign=\"top\" width=\"*\">");
                        if (questid != null) {
                            String getreplysql = "select QuestionHeader,QuestionTimeStamp,QuestionText,ReplyText,ReplyTimeStamp from Question where QuestionSystemID=" + questid;
                            ResultSet replyset = sqlstmt.executeQuery(getreplysql);
                            String QuestheaderVar = null;
                            String QuesttstampVar = null;
                            String QuesttextVar = null;
                            String LasttextVar = null;
                            String ReplytstampVar = null;
                            String Replyoutput = null;
                            while (replyset.next()) {
                                QuestheaderVar = replyset.getString("QuestionHeader");
                                QuesttstampVar = replyset.getString("QuestionTimeStamp");
                                QuesttextVar = replyset.getString("QuestionText");
                                LasttextVar = replyset.getString("ReplyText");
                                ReplytstampVar = replyset.getString("ReplyTimeStamp");
                                out.println("<span class=\"ahyo14\">Question:</span><br>");
                                out.println("<span class=\"ah10\">Submitted: </span><span class=\"ahy10\">" + QuesttstampVar + "</span><br>");
                                out.println("<span class=\"ah10\">Header: </span><span class=\"ahy10\">" + QuestheaderVar + "</span>");
                                out.println("<h1 class=\"ah12\">" + QuesttextVar + "</h1><p>");
                                out.println("<span class=\"ahyo14\">Reply:</span><br>");
                                if (ReplytstampVar != null) {
                                    out.println("<span class=\"ah10\">Posted or Appended: </span><span class=\"ahy10\">" + ReplytstampVar + "</span>");
                                    out.println("<h1 class=\"ah12\">" + LasttextVar + "</h1>");
                                } else {
                                    out.println("<p><span class=\"ah12\">-NONE-</span>");
                                }
                            }
                            out.println("<form action=\"ePICE2?page_request=replyquestionPage\" method=\"post\" name=\"ReplyQuestForm\" onsubmit=\"return CheckSubmittedValues(\'replyquestionPage\')\">");
                            out.println("<p><span class=\"ahyo14\">Post or Append Reply:</span><br>");
                            out.println("<span class=\"ah10\">Reply Text:</span><br>");
                            out.println("<script src=\"js/dyntextarea.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script> name=\"Replytext\"></textarea>");
                            out.println("<script language=\"JavaScript\">");
                            out.println("document.ReplyQuestForm.Replytext.focus();");
                            out.println("</script>");
                            out.println("<input name=\"Questid\" type=\"hidden\" value='" + questid + "'>");
                            out.println("<input name=\"Lastreplytext\" type=\"hidden\" value='" + LasttextVar + "'>");
                            out.println("<p>");
                            out.println("<input type=\"reset\" value=\" Reset \">&nbsp&nbsp&nbsp<input type=\"submit\" name=\"submit\" value=\"Submit\">");
                            out.println("</form>");
                        }
                        sqlstmt.close();
                        HtmlClose1(out, session);
                        gotomenu(out);
                        out.println("<p>");
                        currentinst(out, session);
                        out.println("<p>");
                        out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/replyquestionhelp.html')\">Reply to Question Help</a>");
                        HtmlClose2(out);
                    }
                    if (Page_request.equals("retrievequestionPage")) {
                        String questid = request.getParameter("Questid");
                        out.println("<head>");
                        out.println("<title>ePICE Retrieved Question</title>");
                        out.println("<link rel=stylesheet href=\"css/epice1.css\" type=\"text/css\">");
                        out.println("</head>");
                        out.println("<body>");
                        if (questid != null) {
                            String getreplysql = "select QuestionHeader,QuestionTimeStamp,QuestionText,ReplyText,ReplyTimeStamp from Question where QuestionSystemID=" + questid;
                            ResultSet replyset = sqlstmt.executeQuery(getreplysql);
                            String QuestheaderVar = null;
                            String QuesttstampVar = null;
                            String QuesttextVar = null;
                            String ReplytextVar = null;
                            String ReplytstampVar = null;
                            String Replyoutput = null;
                            while (replyset.next()) {
                                QuestheaderVar = replyset.getString("QuestionHeader");
                                QuesttstampVar = replyset.getString("QuestionTimeStamp");
                                QuesttextVar = replyset.getString("QuestionText");
                                ReplytextVar = replyset.getString("ReplyText");
                                ReplytstampVar = replyset.getString("ReplyTimeStamp");
                                out.println("<span class=\"ahyo12\">Question:</span><br>");
                                out.println("<span class=\"ah10\">Submitted: </span><span class=\"ahy10\">" + QuesttstampVar + "</span><br>");
                                out.println("<span class=\"ah10\">Header: </span><span class=\"ahy10\">" + QuestheaderVar + "</span><br>");
                                out.println("<h1 class=\"ah12\">" + QuesttextVar + "</h1><p>");
                                out.println("<span class=\"ahyo12\">Reply:</span><br>");
                                if (ReplytstampVar != null) {
                                    out.println("<span class=\"ah10\">Posted or Appended: </span><span class=\"ahy10\">" + ReplytstampVar + "</span>");
                                    out.println("<h1 class=\"ah12\">" + ReplytextVar + "</h1>");
                                } else {
                                    out.println("<p><span class=\"ah12\">-NONE-</span>");
                                }
                                out.println("<p><span class=\"ahyo12\">**End**</span><br>");
                            }
                        }
                    }
                    if (Page_request.equals("AdminClassRoster")) {
                        out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                        out.println("</script>");
                        HtmlSection1(out);
                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp<span class=\"title\">");
                        out.println(" Administer Class Roster");
                        out.println("</span>");
                        out.println("</td>");
                        out.println("</tr>");
                        out.println("<tr>");
                        out.println("<td valign=\"top\" bgcolor=\"000099\">");
                        out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                        out.println("<tr>");
                        out.println("<span class=\"ahb16\">Administer Full Enrollment Roster</span><p>");
                        out.println("<span class=\"ahb12\">Please select the current enrollment status of each student:</span><p>");
                        ResultSet courserosterset = sqlstmt.executeQuery("select User.UserSystemID,User.UserFname,User.UserLname,User.UserEmail,Enrolls.EnrollStatus from User,Enrolls where User.UserSystemID=Enrolls.UserSystemID and Enrolls.SectionSystemID=" + (String) session.getAttribute("SelectedCourse") + " order by UserLname,UserFname");
                        if (courserosterset.first() == false) {
                            out.println("<span class=\'ahyo14\'>There are no students enrolled in this section.  Please check back later.</span>");
                        } else {
                            String UserFname = null;
                            String UserLname = null;
                            String UserEmail = null;
                            String EnrollStatus = null;
                            courserosterset.beforeFirst();
                            out.println("<form action=\"ePICE2?page_request=AdminClassRoster\" method=\"post\" name=\"AdminClassRosterForm\">");
                            out.println("<span class=\'ahy10\'>Active &nbsp&nbsp&nbsp Drop</span><br>");
                            while (courserosterset.next()) {
                                UserSystemID = courserosterset.getString("UserSystemID");
                                UserFname = courserosterset.getString("UserFname");
                                UserLname = courserosterset.getString("UserLname");
                                UserEmail = courserosterset.getString("UserEmail");
                                EnrollStatus = courserosterset.getString("EnrollStatus");
                                if (EnrollStatus.equals("y")) {
                                    out.println("&nbsp&nbsp <input type=\"radio\" name=\"" + UserSystemID + "\" value=\"y\" checked> ");
                                    out.println("&nbsp&nbsp <input type=\"radio\" name=\"" + UserSystemID + "\" value=\"n\">");
                                } else {
                                    out.println("&nbsp&nbsp <input type=\"radio\" name=\"" + UserSystemID + "\" value=\"y\"> ");
                                    out.println("&nbsp&nbsp <input type=\"radio\" name=\"" + UserSystemID + "\" value=\"n\" checked>");
                                }
                                out.println("<span class=\'ah12\'>" + UserLname + ", " + UserFname + "</span>&nbsp&nbsp<span class=\'ahb12\'>" + UserEmail + "</span><br>");
                            }
                            out.println("<p>&nbsp&nbsp&nbsp<input type=\"submit\" name=\"submit\" value=\"Submit\">");
                            out.println("</form>");
                        }
                        HtmlClose1(out, session);
                        gotomenu(out);
                        out.println("<p>");
                        currentinst(out, session);
                        out.println("<p>");
                        out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/rosteradminhelp.html')\">Admin Class Roster Help</a>");
                        out.println("</td>");
                        out.println("</tr>");
                        out.println("</table>");
                    }
                    sqlstmt.close();
                    if (outputbody > 0) {
                        out.println("</body>");
                    }
                    out.println("</html>");
                } catch (Exception e) {
                    sendErrorToClient(out, e);
                    log("Error in doGet() method.", e);
                }
            } else {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Automatic System Log Out</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>ePICE detected an extended period of inactivity.<p>You were logged out automatically as a security precaution.<p>If you wish to continue using ePICE, please return to the ePICE hpme page and login.<p>Thank you for your understanding.</h1>");
                out.println("</body>");
                out.println("</html>");
            }
        } catch (SQLException E) {
            System.out.println("SQLException: " + E.getMessage());
            System.out.println("SQLState: " + E.getSQLState());
            System.out.println("VendorError(298):" + E.getErrorCode());
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(true);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        long MAXLENGTH = 2048 * 100;
        if (request.getContentLength() > 2048000) {
            fileTooBig(out, session);
        } else {
            try {
                Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            } catch (Exception E) {
                out.println("Unable to load driver.");
                E.printStackTrace();
            }
            try {
                String UserSystemID = (String) session.getAttribute("UserSystemID");
                if (UserSystemID != null) {
                    String PathDB = getInitParameter("PathDB");
                    String UserDB = getInitParameter("UserDB");
                    String PassDB = getInitParameter("PassDB");
                    String Page_request = request.getParameter("page_request");
                    String CurrentTerm = (String) session.getAttribute("CurrentTerm");
                    String EnrollStatus = null;
                    String NewEnrollStatus = null;
                    String DeptSystemID = null;
                    String CourseSystemID = request.getParameter("CourseSystemID");
                    String SectionSystemID = request.getParameter("SectionSystemID");
                    String SelectedCourse = (String) session.getAttribute("SelectedCourse");
                    String Score = null;
                    String NewScore = null;
                    String AssignmentSystemID = null;
                    String AssignmentName = null;
                    String SubmissionScore = null;
                    String process = request.getParameter("process");
                    byte CourseCount = 0;
                    byte IncrementCount = 1;
                    int MAX_SIZE = 102400;
                    String successMessage;
                    try {
                        Statement sqlstmt = connectDB.createStatement();
                        out.println("<html>");
                        if (Page_request.equals("collectassignmentsPage")) {
                            String SubmissionSystemID = "";
                            String UserLName = "";
                            String UserFName = "";
                            String UserEmail = "";
                            String SubmissionName = "";
                            String itemLocation = "";
                            String FileName = "";
                            AssignmentSystemID = request.getParameter("AssignmentSystemID");
                            SelectedCourse = (String) session.getAttribute("SelectedCourse");
                            try {
                                out.println("<head>");
                                out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                HtmlSection1(out);
                                out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp<span class=\"title\">Collect Assignment</span>");
                                HtmlSection2(out);
                                out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                                out.println("<tr>");
                                out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                out.println("<span class=\"ahb16\">You chose this assignment to collect ...</span><p>");
                                String sql = "SELECT AssignmentName from Assignment WHERE AssignmentSystemID='" + AssignmentSystemID + "'";
                                ResultSet asmtName = sqlstmt.executeQuery(sql);
                                asmtName.beforeFirst();
                                while (asmtName.next()) {
                                    AssignmentName = asmtName.getString("AssignmentName");
                                    out.println("<span class=\"ahb14\">" + AssignmentName + "</span><br>");
                                }
                                out.println("</td>");
                                VerticalDivBar(out);
                                out.println("<td valign=\"top\" width=\"*\">");
                                out.println("<span class=\"ahb12\">Select the student submission you wish to download.");
                                out.println("<p>");
                                sql = "SELECT s.SubmissionSystemID,s.SubmissionName,s.SubmissionFilePath,s.SubmissionFileName," + "u.UserLName,u.UserFName,u.UserEmail" + " FROM Submission AS s,User as u" + " WHERE s.AssignmentSystemID=" + AssignmentSystemID + " AND SectionSystemID=" + SelectedCourse + " AND s.UserSystemID=u.UserSystemID";
                                System.out.println("1580:sql=" + sql);
                                ResultSet submittedset = sqlstmt.executeQuery(sql);
                                submittedset.beforeFirst();
                                while (submittedset.next()) {
                                    SubmissionSystemID = submittedset.getString("SubmissionSystemID");
                                    SubmissionName = submittedset.getString("SubmissionName");
                                    itemLocation = submittedset.getString("SubmissionFilePath");
                                    FileName = submittedset.getString("SubmissionFileName");
                                    UserLName = submittedset.getString("UserLName");
                                    UserFName = submittedset.getString("UserFName");
                                    UserEmail = submittedset.getString("UserEmail");
                                    out.println("<span class=\"ah12\">" + "<A class=\"ah12\" HREF=\"/ePICE/ePICE5" + "?itemLocation=" + itemLocation + "&FileName=" + FileName + "\">" + UserLName + ", " + UserFName + "  " + UserEmail + "</A>" + "</span></a><br><br>");
                                }
                                sqlstmt.close();
                                HtmlClose1(out, session);
                                gotomenu(out);
                                out.println("<p>");
                                currentcourses(out, session);
                                out.println("<p>");
                                out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/collectassignmenthelp.html')\">Collect Assignments Help</a>");
                                HtmlClose2(out);
                                out.println("</body></html>");
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
                        if (Page_request.equals("postannouncementPage")) {
                            String anheader = request.getParameter("sourceheader");
                            String antext = request.getParameter("Antext");
                            try {
                                out.println("<head>");
                                out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                HtmlSection1(out);
                                out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp<span class=\"title\">Announcement Posted !</span>");
                                HtmlSection2(out);
                                out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                                out.println("<tr>");
                                out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                out.println("<span class=\"ahb16\">Your submitted announcement...</span><p>");
                                out.println("&nbsp&nbsp<span class=\"ahb12\">Announcement Header:</span><h1 class=\"ahy12\">&nbsp&nbsp&nbsp&nbsp " + anheader + "</h1><p>");
                                if (antext.equals("")) {
                                    antext = "-No announcement text was submitted by the instructor-";
                                }
                                out.println("&nbsp&nbsp<span class=\"ahb12\">Announcement Text:</span><h1 class=\"ahy12\">&nbsp&nbsp&nbsp&nbsp " + antext + "</h1><p>");
                                String postannouncesql = "insert into Announcement (SectionSystemID,UserSystemID,AnnounceHeader,AnnounceText,AnnounceTimeStamp) values ('" + SelectedCourse + "','" + UserSystemID + "','" + anheader + "','" + antext + "',CURRENT_TIMESTAMP)";
                                sqlstmt.executeUpdate(postannouncesql);
                                out.println("<span class=\"ahb16\">...has posted successfully!</span><p>");
                                out.println("</td>");
                                VerticalDivBar(out);
                                out.println("<td valign=\"top\" width=\"*\">");
                                out.println("<span class=\"ahb12\">Please use the navigation links on the right side of the browser window to select your next ePICE function.</span>");
                                sqlstmt.close();
                                HtmlClose1(out, session);
                                gotomenu(out);
                                out.println("<p>");
                                currentcourses(out, session);
                                out.println("<p>");
                                out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postannouncehelp.html')\">Post Announcement Help</a>");
                                HtmlClose2(out);
                                out.println("</body></html>");
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
                        if (Page_request.equals("postassignmentPage")) {
                            process = request.getParameter("process");
                            ResultSet assignmentset = sqlstmt.executeQuery("SELECT * FROM Assignment " + "WHERE SectionSystemID='" + (String) session.getAttribute("SelectedCourse") + "' " + "AND AssignmentName='" + request.getParameter("sourceheader") + "'");
                            if (assignmentset.first() == true) {
                                process = "duplicate";
                            }
                            if (process != null) {
                                String sourceheader = request.getParameter("sourceheader");
                                session.setAttribute("sourceheader", sourceheader);
                                SelectedCourse = (String) session.getAttribute("SelectedCourse");
                                out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                HtmlSection1(out);
                                out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                                out.println("<span class=\"title\">Post Assignment</span>");
                                HtmlSection2(out);
                                doGetHtmlSection3(out);
                                out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                out.println("<span class=\"ahb14\">Post New Assignment</span>");
                                out.println("<p>");
                                if (process != "duplicate") {
                                    out.println("<span class=\"ahb12\">You are posting <span class=\"ahy12\">" + sourceheader + "</span>. Please select the corresponding file for <span class=\"ahy12\">" + sourceheader + "</span> in the adjacent column.</span><p>");
                                    out.println("<span class=\"ahb12\">If the file you selected exceeds the system's current file size limit, you will need to contact your ePICE system administrator.</span>");
                                } else {
                                    out.println("<h1 class=\"ahb12\"><span class=\"ahyo12\">Duplicate Assignment Name detected! Post Assignment request aborted.</span><p>Click the \"<span class=\"ahy12\">Back</span>\" button and enter a unique Assignment name.</h1>");
                                }
                                out.println("</td>");
                                VerticalDivBar(out);
                                out.println("<td valign=\"top\" width=\"*\">");
                                if (process != "duplicate") {
                                    out.println("<form enctype=\"multipart/form-data\" action=\"ePICE2?page_request=postassignmentPage\" name=\"postassignmentForm\" method=\"post\" onsubmit=\"return CheckSubmittedValues(\'postassignmentPage2\')\">");
                                    out.println("<span class=\"ahb12\">Select the corresponding assignment file:</span>");
                                    out.println("<p>");
                                    out.println("<span class=\"ah10\">Assignment File Path:</span><br>");
                                    out.println("<script src=\"js/filepathbox.js\" language=\"javascript\" type=\"text/javascript\">");
                                    out.println("</script>");
                                    out.println(" name=\"sourcepath\" maxlength=\"255\">");
                                    out.println("<p>");
                                    out.println("<input type=\"reset\" value=\" Reset \">&nbsp&nbsp&nbsp<input type=\"Submit\" Value=\"Submit\">");
                                    out.println("</form>");
                                } else {
                                    out.println("<span class=\"ahb12\">If you require a different ePICE function, please use the navigation links on the right side of the browser window.</span>");
                                }
                                HtmlClose1(out, session);
                                gotomenu(out);
                                currentinst(out, session);
                                out.println("<p>&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postassignmenthelp.html')\">Post Assignment Help</a>");
                                HtmlClose2(out);
                            } else {
                                successMessage = request.getParameter("SuccessMessage");
                                if (successMessage == null) {
                                    successMessage = "File upload complete!";
                                }
                                if (uploadFile(request, response, session, "assignment") == 0) {
                                    try {
                                        String AssignmentFileName = FileName;
                                        String AssignmentFilePath = FilePath;
                                        String sourceheader = (String) session.getAttribute("sourceheader");
                                        SelectedCourse = (String) session.getAttribute("SelectedCourse");
                                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                                        out.println("</script>");
                                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                                        out.println("</script>");
                                        HtmlSection1(out);
                                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                                        out.println("<span class=\"title\">Posting Assignment!</span>");
                                        HtmlSection2(out);
                                        out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                                        out.println("<tr>");
                                        out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                                        out.println("</script>");
                                        out.println("<span class=\"ahb16\">Your submitted assignment...</span><p>");
                                        out.println("&nbsp&nbsp<span class=\"ah14\">Assignment Name:</span><h1 class=\"ahy14\">&nbsp&nbsp&nbsp&nbsp " + sourceheader + "</h1><p>");
                                        out.println("&nbsp&nbsp<span class=\"ah14\">Assignment File Name:</span><h1 class=\"ahy14\">&nbsp&nbsp&nbsp&nbsp " + AssignmentFileName + "</h1><p>");
                                        String postassignmentsql = "insert into Assignment " + "(SectionSystemID,UserSystemID,AssignmentName,AssignmentPostTime,AssignmentFilePath,AssignmentFileName)" + "values ('" + SelectedCourse + "','" + UserSystemID + "','" + sourceheader + "',CURRENT_TIMESTAMP,'" + AssignmentFilePath + "','" + AssignmentFileName + "')";
                                        sqlstmt.executeUpdate(postassignmentsql);
                                        out.println("<span class=\"ahb16\">...has posted successfully!</span><p>");
                                        out.println("</td>");
                                        VerticalDivBar(out);
                                        out.println("<td valign=\"top\" width=\"*\">");
                                        out.println("<span class=\"ahb12\">Please use the navigation links on the right side of the browser window to select your next ePICE function.</span><p>");
                                        sqlstmt.close();
                                        HtmlClose1(out, session);
                                        gotomenu(out);
                                        currentinst(out, session);
                                        out.println("<p>&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postassignmenthelp.html')\">Post Assignment Help</a>");
                                        HtmlClose2(out);
                                    } catch (Exception e) {
                                        try {
                                            System.out.println("Error in doPost: " + e);
                                            out.println("An error has occurred.");
                                            out.println("Error description: " + e);
                                        } catch (Exception f) {
                                        }
                                    }
                                }
                            }
                        }
                        if (Page_request.equals("postmaterialPage")) {
                            process = request.getParameter("process");
                            ResultSet materialset = sqlstmt.executeQuery("SELECT * FROM Material " + "WHERE SectionSystemID='" + (String) session.getAttribute("SelectedCourse") + "' " + "AND MaterialName='" + request.getParameter("sourceheader") + "'");
                            if (materialset.first() == true) {
                                process = "duplicate";
                            }
                            if (process != null) {
                                String sourceheader = request.getParameter("sourceheader");
                                session.setAttribute("sourceheader", sourceheader);
                                SelectedCourse = (String) session.getAttribute("SelectedCourse");
                                out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                HtmlSection1(out);
                                out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                                out.println("<span class=\"title\">Post Material</span>");
                                HtmlSection2(out);
                                doGetHtmlSection3(out);
                                out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                                out.println("<span class=\"ahb14\">Post New Material</span>");
                                out.println("<p>");
                                if (process != "duplicate") {
                                    out.println("<span class=\"ahb12\">You are posting <span class=\"ahy12\">" + sourceheader + "</span>. Please select the corresponding file for <span class=\"ahy12\">" + sourceheader + "</span> in the adjacent column.</span><p>");
                                    out.println("<span class=\"ahb12\">If the file you selected exceeds the system's current file size limit, you will need to contact your ePICE system administrator.</span>");
                                } else {
                                    out.println("<h1 class=\"ahb12\"><span class=\"ahyo12\">Duplicate Material Name detected! Post Material request aborted.</span><p>Click the \"<span class=\"ahy12\">Back</span>\" button and enter a unique Material name.</h1>");
                                }
                                out.println("</td>");
                                VerticalDivBar(out);
                                out.println("<td valign=\"top\" width=\"*\">");
                                if (process != "duplicate") {
                                    out.println("<form enctype=\"multipart/form-data\" action=\"ePICE2?page_request=postmaterialPage\" name=\"postmaterialForm\" method=\"post\" onsubmit=\"return CheckSubmittedValues(\'postmaterialPage2\')\">");
                                    out.println("<span class=\"ahb14\">Select the corresponding material file:</span>");
                                    out.println("<p>");
                                    out.println("<span class=\"ah10\">Material File Path:</span><br>");
                                    out.println("<script src=\"js/filepathbox.js\" language=\"javascript\" type=\"text/javascript\">");
                                    out.println("</script>");
                                    out.println(" name=\"sourcepath\" maxlength=\"255\">");
                                    out.println("<p>");
                                    out.println("<input type=\"reset\" value=\" Reset \">&nbsp&nbsp&nbsp<input type=\"Submit\" Value=\"Submit\">");
                                    out.println("</form>");
                                } else {
                                    out.println("<span class=\"ahb12\">If you require a different ePICE function, please use the navigation links on the right side of the browser window.</span>");
                                }
                                HtmlClose1(out, session);
                                gotomenu(out);
                                currentinst(out, session);
                                out.println("<p>&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postmaterialhelp.html')\">Post Material Help</a>");
                                HtmlClose2(out);
                            } else {
                                successMessage = request.getParameter("SuccessMessage");
                                if (successMessage == null) {
                                    successMessage = "File upload complete!";
                                }
                                if (uploadFile(request, response, session, "material") == 0) {
                                    try {
                                        String MaterialFileName = FileName;
                                        String MaterialFilePath = FilePath;
                                        String sourceheader = (String) session.getAttribute("sourceheader");
                                        String MaterialSystemID = null;
                                        SelectedCourse = (String) session.getAttribute("SelectedCourse");
                                        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                                        out.println("</script>");
                                        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                                        out.println("</script>");
                                        HtmlSection1(out);
                                        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                                        out.println("<span class=\"title\">Posting Material!</span>");
                                        HtmlSection2(out);
                                        out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                                        out.println("<tr>");
                                        out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                                        out.println("</script>");
                                        out.println("<span class=\"ahb16\">Your submitted material...</span><p>");
                                        out.println("&nbsp&nbsp<span class=\"ah14\">Material Name:</span><h1 class=\"ahy14\">&nbsp&nbsp&nbsp&nbsp " + sourceheader + "</h1><p>");
                                        out.println("&nbsp&nbsp<span class=\"ah14\">Material Header:</span><h1 class=\"ahy14\">&nbsp&nbsp&nbsp&nbsp " + MaterialFileName + "</h1><p>");
                                        String postmaterialsql = "insert into Material " + "(SectionSystemID,UserSystemID,MaterialName,MaterialPostTime,MaterialFilePath,MaterialFileName)" + "values ('" + SelectedCourse + "','" + UserSystemID + "','" + sourceheader + "',CURRENT_TIMESTAMP,'" + MaterialFilePath + "','" + MaterialFileName + "')";
                                        sqlstmt.executeUpdate(postmaterialsql);
                                        ResultSet getmaterialIDset = sqlstmt.executeQuery("select MaterialSystemID from Material where SectionSystemID = '" + SelectedCourse + "' and MaterialName = '" + sourceheader + "'");
                                        out.println("<span class=\"ahb16\">...has posted successfully!</span><p>");
                                        out.println("</td>");
                                        VerticalDivBar(out);
                                        out.println("<td valign=\"top\" width=\"*\">");
                                        out.println("<span class=\"ahb12\">Please use the navigation links on the right side of the browser window to select your next ePICE function.</span><p>");
                                        sqlstmt.close();
                                        HtmlClose1(out, session);
                                        gotomenu(out);
                                        currentinst(out, session);
                                        out.println("<p>&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postmaterialHelp')\">Post Material Help</a>");
                                        HtmlClose2(out);
                                    } catch (Exception e) {
                                        try {
                                            System.out.println("Error in doPost: " + e);
                                            out.println("An error has occurred.");
                                            out.println("Error description: " + e);
                                        } catch (Exception f) {
                                        }
                                    }
                                }
                            }
                        }
                        if (Page_request.equals("postscoresPage")) {
                            out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script>");
                            out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script>");
                            out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script>");
                            HtmlSection1(out);
                            out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                            out.println("<span class=\"title\">Post Scores</span>");
                            out.println("</span>");
                            out.println("</td>");
                            out.println("</tr>");
                            out.println("<tr>");
                            out.println("<td valign=\"top\" bgcolor=\"000099\">");
                            out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                            out.println("<tr>");
                            if (process.equals("update")) {
                                out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                                out.println("</script>");
                            }
                            if (process.equals("enterscores")) {
                                AssignmentSystemID = request.getParameter("assignmentchoice");
                                boolean test = AssignmentSystemID.startsWith("<");
                                if (test == false) {
                                    ResultSet assignmentnameset = sqlstmt.executeQuery("select AssignmentName from Assignment where AssignmentSystemID=\'" + AssignmentSystemID + "\'");
                                    while (assignmentnameset.next()) {
                                        AssignmentName = assignmentnameset.getString("AssignmentName");
                                    }
                                    out.println("<td colspan=\"3\" valign=\"top\" height=\"10\" align=\"left\" width=\"*\">");
                                    out.println("<span class=\"ahb16\">Scores of Current Enrollment Roster for </span><span class=\"ah16\">" + AssignmentName + "</span><span class=\"ahb16\">:</span><p>");
                                    out.println("<span class=\"ahb12\">Please update scores as required.</span><p>");
                                    out.println("</td>");
                                    out.println("</tr>");
                                    out.println("<tr>");
                                    ResultSet scoreset = sqlstmt.executeQuery("select Submission.SubmissionScore,User.UserSystemID,User.UserFName,User.UserLName,User.UserEmail from Submission,User where Submission.AssignmentSystemID=\'" + AssignmentSystemID + "\' and Submission.UserSystemID=User.UserSystemID order by UserLname,UserFname");
                                    if (scoreset.first() == false) {
                                        out.println("<td colspan=\"3\" valign=\"top\" height=\"10\" align=\"center\" width=\"*\">");
                                        out.println("<span class=\'ahyo14\'>There are no assignment submissions for this section.  Please check back later.</span>");
                                        out.println("</td>");
                                    } else {
                                        out.println("<td valign=\"top\" height=\"20\" align=\"right\" width=\"40%\">");
                                        out.println("<span class=\'ahyo14\'>Student</span><br>");
                                        out.println("<span class=\'ahyo14\'>Name</span>");
                                        out.println("</td>");
                                        out.println("<td  valign=\"top\" height=\"20\" align=\"center\" width=\"20%\">");
                                        out.println("<span class=\'ahyo14\'>Unofficial</span><br>");
                                        out.println("<span class=\'ahyo14\'>Score</span>");
                                        out.println("</td>");
                                        out.println("<td  valign=\"top\" height=\"20\" align=\"left\" width=\"*\">");
                                        out.println("<span class=\'ahyo14\'>Student</span><br>");
                                        out.println("<span class=\'ahyo14\'>E-mail Address</span>");
                                        out.println("</td>");
                                        out.println("</tr>");
                                        out.println("<tr>");
                                        String UserFName = null;
                                        String UserLName = null;
                                        String UserEmail = null;
                                        scoreset.beforeFirst();
                                        out.println("<form action=\"ePICE2?page_request=postscoresPage\" method=\"post\" name=\"PostScoresForm\">");
                                        while (scoreset.next()) {
                                            UserSystemID = scoreset.getString("UserSystemID");
                                            UserFName = scoreset.getString("UserFName");
                                            UserLName = scoreset.getString("UserLName");
                                            UserEmail = scoreset.getString("UserEmail");
                                            SubmissionScore = scoreset.getString("SubmissionScore");
                                            out.println("<td valign=\"middle\" height=\"10\" align=\"right\" width=\"40%\">");
                                            out.println("<span class=\'ahb12\'>" + UserLName + ", " + UserFName + "</span>");
                                            out.println("</td>");
                                            out.println("<td valign=\"middle\" height=\"10\" align=\"center\" width=\"20*\">");
                                            out.println("<span class=\"ah12\"><input type=\"text\" name=\"" + UserSystemID + "\" size=\"3\" maxlength=\"3\" align=\"left\" value=\"" + SubmissionScore + "\"></span>");
                                            out.println("</td>");
                                            out.println("<td  valign=\"middle\" height=\"20\" align=\"left\" width=\"*\">");
                                            out.println("<a class=\'ah12\' href=\"mailto:" + UserEmail + "\">" + UserEmail + "</span>");
                                            out.println("</td>");
                                            out.println("</tr>");
                                            out.println("<tr>");
                                        }
                                        out.println("<td colspan=\"3\" valign=\"middle\" height=\"*\" align=\"center\" width=\"*\">");
                                        out.println("<input type=\"hidden\" name=\"process\" value=\"update\">");
                                        out.println("<input type=\"hidden\" name=\"AssignmentSystemID\" value=\"" + AssignmentSystemID + "\">");
                                        out.println("<input type=\"hidden\" name=\"AssignmentName\" value=\"" + AssignmentName + "\">");
                                        out.println("<p>&nbsp&nbsp&nbsp<input type=\"submit\" name=\"submit\" value=\"Submit\">");
                                        out.println("</form>");
                                    }
                                } else {
                                    out.println("<h1 class=\"ahyo14\">");
                                    out.println("A valid Assignment selection must be chosen in order to facilitate any changes you want to make.<p>");
                                    out.println("Please go back to the Post Scores Page and select the desired Assignment for score recording.");
                                }
                            }
                            if (process.equals("update")) {
                                AssignmentSystemID = request.getParameter("AssignmentSystemID");
                                AssignmentName = request.getParameter("AssignmentName");
                                ResultSet scoreset = sqlstmt.executeQuery("select UserSystemID,SubmissionScore from Submission where AssignmentSystemID=\'" + AssignmentSystemID + "\' order by UserSystemID");
                                if (scoreset.first() == false) {
                                    out.println("<span class=\'ahyo14\'>There are no students enrolled in this section.  Please check back later.</span>");
                                } else {
                                    out.println("<span class=\'ahb14\'>Updating student scores for</span> <span class=\'ah14\'>" + AssignmentName + "</span><span class=\'ahb14\'>...</span><p>");
                                    scoreset.beforeFirst();
                                    while (scoreset.next()) {
                                        UserSystemID = scoreset.getString("UserSystemID");
                                        SubmissionScore = scoreset.getString("SubmissionScore");
                                        NewScore = request.getParameter(UserSystemID);
                                        if (SubmissionScore != NewScore) {
                                            sqlstmt.executeUpdate("update Submission set SubmissionScore=\'" + NewScore + "\' where UserSystemID=\'" + UserSystemID + "\' and AssignmentSystemID=\'" + AssignmentSystemID + "\'");
                                        }
                                    }
                                }
                                out.println("</td>");
                                VerticalDivBar(out);
                                out.println("<td valign=\"top\">");
                                out.println("<span class=\"ahb12\">The assignment scores has been updated with your changes. ");
                                out.println("<p>");
                                out.println("<span class=\"ahb12\">Please use the navigation links on the right side of the browser window to select your next ePICE function.</span>");
                            }
                            HtmlClose1(out, session);
                            gotomenu(out);
                            out.println("<p>");
                            currentinst(out, session);
                            out.println("<p>");
                            if (Page_request.equals("PostScores")) {
                                out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/postscoreshelp.html')\">Post Scores Help</a>");
                            }
                            sqlstmt.close();
                            out.println("</td>");
                            out.println("</tr>");
                            out.println("</table>");
                        }
                        if (Page_request.equals("replyquestionPage")) {
                            String questid = request.getParameter("Questid");
                            String replytext = request.getParameter("Replytext");
                            out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script>");
                            HtmlSection1(out);
                            out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
                            out.println("<span class=\"title\">Reply Posted !</span>");
                            HtmlSection2(out);
                            out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                            out.println("<tr>");
                            out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script>");
                            out.println("<span class=\"ahb14\">Your reply...</span>");
                            out.println("<p>");
                            out.println("<span class=\"ah12\">" + replytext + "</span><br>");
                            String ReplytextVar = null;
                            String checkreplysql = "select ReplyText from Question where QuestionSystemID=" + questid;
                            ResultSet checkreplyset = sqlstmt.executeQuery(checkreplysql);
                            while (checkreplyset.next()) {
                                ReplytextVar = checkreplyset.getString("ReplyText");
                            }
                            if (ReplytextVar == null) {
                                ReplytextVar = replytext;
                                String postreplysql = "update Question set PostFlag=1,ReplyText='" + ReplytextVar + "',ReplyTimeStamp=NOW() where QuestionSystemID=" + questid;
                                sqlstmt.executeUpdate(postreplysql);
                            } else {
                                checkreplyset.beforeFirst();
                                while (checkreplyset.next()) {
                                    ReplytextVar = checkreplyset.getString("ReplyText");
                                }
                                ReplytextVar += "<p>" + replytext;
                                String postreplysql = "update Question set ReplyText='" + ReplytextVar + "',ReplyTimeStamp=NOW() where QuestionSystemID=" + questid;
                                sqlstmt.executeUpdate(postreplysql);
                                out.println("<p><span class=\"ahb14\">...has been successfully posted into the ePICE database.</span>");
                            }
                            out.println("</td>");
                            VerticalDivBar(out);
                            out.println("<td valign=\"top\" width=\"*\">");
                            out.println("<span class=\"ahb12\">Your reply will now appear on the course's question page.  Your students will be able to view its content when they access the link to the Submit Question Page.");
                            out.println("<p>");
                            out.println("<span class=\"ahb12\">You will be automatically returned to the ePICE Reply Question  Web Page momentarily. If you require a different ePICE function, please use the navigation links on the right side of the browser window.</span>");
                            sqlstmt.close();
                            HtmlClose1(out, session);
                            gotomenu(out);
                            currentinst(out, session);
                            HtmlClose2(out);
                        }
                        if (Page_request.equals("AdminClassRoster")) {
                            out.println("<script src=\"js/ePICE2Chk.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script>");
                            out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script>");
                            out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script>");
                            HtmlSection1(out);
                            out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp<span class=\"title\">");
                            out.println(" Class Roster Administration");
                            out.println("</span>");
                            out.println("</td>");
                            out.println("</tr>");
                            out.println("<tr>");
                            out.println("<td valign=\"top\" bgcolor=\"000099\">");
                            out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
                            out.println("<tr>");
                            out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
                            out.println("</script>");
                            ResultSet courserosterset = sqlstmt.executeQuery("select UserSystemID,EnrollStatus from Enrolls where SectionSystemID=" + (String) session.getAttribute("SelectedCourse") + " order by UserSystemID");
                            if (courserosterset.first() == false) {
                                out.println("<span class=\'ahyo14\'>There are no students enrolled in this section.  Please check back later.</span>");
                            } else {
                                out.println("<span class=\'ahb14\'>Updating Enrollment Roster for student with enrollment status changes...</span><p>");
                                courserosterset.beforeFirst();
                                while (courserosterset.next()) {
                                    UserSystemID = courserosterset.getString("UserSystemID");
                                    EnrollStatus = courserosterset.getString("EnrollStatus");
                                    NewEnrollStatus = request.getParameter(UserSystemID);
                                    if (EnrollStatus != NewEnrollStatus) {
                                        sqlstmt.executeUpdate("update Enrolls set EnrollStatus='" + NewEnrollStatus + "' where UserSystemID='" + UserSystemID + "' and SectionSystemID='" + (String) session.getAttribute("SelectedCourse") + "'");
                                    }
                                }
                            }
                            out.println("</td>");
                            VerticalDivBar(out);
                            out.println("<td valign=\"top\">");
                            out.println("<h1 class=\"ahb12\">The section enrollment roster has been updated with your changes. You will see the new updates when you return to the initial Enrollment Roster Administration Page.");
                            out.println("<p>");
                            out.println("Please use the navigation links on the right side of the browser window to select your next ePICE function.</h1>");
                            HtmlClose1(out, session);
                            gotomenu(out);
                            out.println("<p>");
                            currentinst(out, session);
                            out.println("<p>");
                            out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/createsectionhelp.html')\">Admin Roster Help</a>");
                            sqlstmt.close();
                            out.println("</td>");
                            out.println("</tr>");
                            out.println("</table>");
                        }
                        if (Page_request.equals("uploadexceptionPage")) {
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
                            HtmlClose1(out, session);
                            gotomenu(out);
                            out.println("<p>");
                            out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/help.html')\">Help Link Here</a>");
                            HtmlClose2(out);
                        }
                        out.println("</body></html>");
                    } catch (SQLException E) {
                        System.out.println("SQLException: " + E.getMessage());
                        System.out.println("SQLState: " + E.getSQLState());
                        System.out.println("VendorError(292):" + E.getErrorCode());
                    }
                } else {
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>Automatic System Log Out</title>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h1>ePICE detected an extended period of inactivity.<p>You were logged out automatically as a security precaution.<p>If you wish to continue using ePICE, please return to the ePICE hpme page and login.<p>Thank you for your understanding.</h1>");
                    out.println("</body>");
                    out.println("</html>");
                }
            } catch (Exception e) {
                sendErrorToClient(out, e);
                log("Error in doPost() method.", e);
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
        out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"*\" height=\"*\">");
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

    private synchronized void HtmlClose1(PrintWriter out, HttpSession session) {
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("</td>");
        out.println("<td valign=\"top\" width=\"160\" bgcolor=\"FFFFFF\">");
        out.println("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\"width=\"100%\" height=\"100%\">");
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
        String getcoursesql = "select Course.CoursePrefix,Course.CourseNum,Section.SectionSystemID,Section.SectionNum from Course,Section,Enrolls  where Enrolls.UserSystemID='" + UserSystemID + "' and Section.SectionSystemID = Enrolls.SectionSystemID and Course.CourseSystemID = Section.CourseSystemID order by CoursePrefix,CourseNum,SectionNum";
        try {
            ResultSet courseset = sqlstmt.executeQuery(getcoursesql);
            out.println("<span class=\"ahdb10\">Current Courses:</span><br>");
            if (courseset.first() == false) {
                out.println("&nbsp&nbsp<span class=\"ahb10\">None</span>");
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
                    out.println("&nbsp&nbsp<a class=\"ah10\" target=\"_top\" href=\"ePICE?page_request=announcementsPage&SelectedCourse=" + CurrentSectionVar + "\">" + CurrentCoursePrefix + " " + CurrentCourseNum + " s." + CurrentSectionNum + "</a><br>");
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

    private synchronized void currentcourses(PrintWriter out, HttpSession session) {
        out.println("<p><span class=\"ahdb10\">Current Courses:</span><br>");
        String StrCourseCount = (String) session.getAttribute("CourseCount");
        if (StrCourseCount == null) {
            StrCourseCount = "0";
        }
        byte IncrementStep = 1;
        Integer CourseCount = Integer.valueOf(StrCourseCount);
        if (CourseCount.intValue() == 0) {
            out.println("&nbsp&nbsp<span class=\"ahdb10\">None</span><p>");
        } else {
            while (CourseCount.intValue() >= IncrementStep) {
                out.println("&nbsp&nbsp<a class=\"ah10\" target=\"_top\" href=\"ePICE?page_request=announcementsPage&SelectedCourse=" + (String) session.getAttribute("Course" + IncrementStep) + "\">" + (String) session.getAttribute("Course" + IncrementStep + "N") + "</a><br>");
                IncrementStep += 1;
            }
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
                out.println("&nbsp&nbsp<span class=\"ahb10\">None</span>");
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

    private synchronized void currentinst(PrintWriter out, HttpSession session) {
        out.println("<p><span class=\"ahdb10\">Current Instruction Roster:</span><br>");
        String StrCourseCount = (String) session.getAttribute("CourseCount");
        if (StrCourseCount == null) {
            StrCourseCount = "0";
        }
        byte IncrementStep = 1;
        Integer CourseCount = Integer.valueOf(StrCourseCount);
        if (CourseCount.intValue() == 0) {
            out.println("&nbsp&nbsp<span class=\"ahb10\">None</span><p>");
        } else {
            while (CourseCount.intValue() >= IncrementStep) {
                out.println("&nbsp&nbsp<a class=\"ah10\" target=\"_top\" href=\"ePICE2?page_request=currenttermPage&SelectedCourse=" + (String) session.getAttribute("Course" + IncrementStep) + "\">" + (String) session.getAttribute("Course" + IncrementStep + "N") + "</a><br>");
                IncrementStep += 1;
            }
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

    private synchronized void sectionSelection(PrintWriter out, String CourseSystemID) {
        try {
            out.println("<title>Choose Section Number</title>");
            out.println("</head>");
            out.println("<body bgcolor=#000099 text=#FFFFFF>");
            out.println("<font size=\"4\" face=\"Arial,Helvetica\">");
            out.println("<font color=#FFFF00><i>Select<br>Section Number</i></font></font>");
            out.println("<br><br>");
            out.println("<font size=\"2\" face=\"Arial,Helvetica\">Select the course section number you wish to enroll:</font>");
            out.println("<br><br>");
            out.println("<form method=\"post\" target=\"_top\" action=\"ePICE?page_request=registercoursePage\">");
            out.println("<select name=\"SectionSystemID\">");
            String sectsql = "select SectionSystemID,SectionNum from Section where CourseSystemID=\'" + CourseSystemID + "\' order by SectionNum";
            Statement sqlstmt = connectDB.createStatement();
            ResultSet sectrsltset = sqlstmt.executeQuery(sectsql);
            String SectionNum = null;
            String SectionSystemID = null;
            while (sectrsltset.next()) {
                SectionSystemID = sectrsltset.getString("SectionSystemID");
                SectionNum = sectrsltset.getString("SectionNum");
                out.println("<option value=\'" + SectionSystemID + "\'>" + SectionNum + "</option>");
            }
            sectrsltset.close();
            sqlstmt.close();
            out.println("</select>");
            out.println("<input name=\'SectionSystemID\' type=\'hidden\' value=\'" + SectionSystemID + "\'>");
        } catch (Exception e) {
            log("Error in doPost() method.", e);
        }
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

    /** This the same as the boolean version except it returns a 0, -1, or 1 for 'OK', upload problem, and file
	 * collision, respectively. class FileCollisionException is created to support this routine.
	 */
    protected int uploadFile(HttpServletRequest request, HttpServletResponse response, HttpSession session, String folder) throws ServletException, java.io.IOException, FileCollisionException {
        String DRIVER, URL, USER, PASS, fieldName, fieldData, fileName, fileFieldName, Page_requestuest, returnAddress;
        String prodName, prodDetail, prodImgURI, prodImgPreview, SelectedCourse, rootPath, sourceheader;
        int Normal = 0;
        int Problem = -1;
        int Collision = 1;
        Long UploadSizeLimit = Long.valueOf(getInitParameter("UploadSizeLimit"));
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        Connection con = null;
        rootPath = getInitParameter("RootPath");
        if (rootPath == null) {
            rootPath = "/tmp/ePICE/";
        }
        String tempFolder = rootPath + "temp/";
        SelectedCourse = (String) session.getAttribute("SelectedCourse");
        folder = rootPath + folder + "/" + SelectedCourse;
        sourceheader = (String) session.getAttribute("sourceheader");
        try {
            DiskFileUpload upload = new DiskFileUpload();
            upload.setSizeMax(UploadSizeLimit.longValue());
            upload.setSizeThreshold(4096);
            upload.setRepositoryPath(tempFolder);
            List items = upload.parseRequest(request);
            Iterator iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField()) {
                } else if (item.getName().equals("")) {
                } else {
                    fileName = item.getName();
                    fileFieldName = item.getFieldName();
                    StringTokenizer tokenizer = new StringTokenizer(fileName, "\\, :, /");
                    int amount = tokenizer.countTokens();
                    for (int i = 0; i < amount - 1; i++) {
                        tokenizer.nextToken();
                    }
                    String currentFile = tokenizer.nextToken();
                    String saveName = "";
                    File fileDir = new File(folder);
                    if (!fileDir.exists()) {
                        fileDir.mkdirs();
                    }
                    File tempDir = new File(tempFolder);
                    if (!tempDir.exists()) {
                        tempDir.mkdirs();
                    }
                    File diskFile = new File(folder, currentFile);
                    FileName = currentFile;
                    FilePath = folder + "/" + FileName;
                    if (diskFile.exists()) {
                        throw new FileCollisionException(out, currentFile, session);
                    } else {
                        item.write(diskFile);
                    }
                    item.delete();
                }
            }
        } catch (FileUploadException fue) {
            fue.printStackTrace();
            out.println("There was and error when reading and writing the file to the server.");
            return (Problem);
        } catch (IllegalStateException ise) {
            System.out.println("IllegalStateException: 143: " + ise);
            return (Problem);
        } catch (Exception e) {
            e.printStackTrace();
            return (Problem);
        }
        return (Normal);
    }

    private void fileTooBig(PrintWriter out, HttpSession session) {
        out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
        out.println("</script>");
        out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
        out.println("</script>");
        HtmlSection1(out);
        out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
        out.println("<span class=\"title\">File Size Exceeds Limit</span>");
        HtmlSection2(out);
        out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
        out.println("<tr>");
        out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
        out.println("</script>");
        out.println("<h1 class=\"ah16\">The file you have attempted to upload exceeds the limits set by the System Administrator. You will need to contact your System Administrator or instructor for directions on submitting your file.<p>Click the '<span class=\"ahy16\">Back</span>' button on your browser to return to the previous page.</h1>");
        HtmlClose1(out, session);
        gotomenu(out);
        out.println("<p>");
        HtmlClose2(out);
    }

    public class FileCollisionException extends Exception {

        public FileCollisionException(PrintWriter out, String s, HttpSession session) {
            super(s);
            out.println("<script src=\"js/instructornavmenu.js\" language=\"javascript\" type=\"text/javascript\">");
            out.println("</script>");
            out.println("<script src=\"js/helpwin.js\" language=\"javascript\" type=\"text/javascript\">");
            out.println("</script>");
            HtmlSection1(out);
            out.println("<img src=\"graphics/syslogo.jpg\" alt=\"Public\" align=\"left\">&nbsp");
            out.println("<span class=\"title\">File Upload Error</span>");
            HtmlSection2(out);
            out.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"1\"width=\"100%\" height=\"100%\">");
            out.println("<tr>");
            out.println("<script src=\"js/screenarea.js\" language=\"javascript\" type=\"text/javascript\">");
            out.println("</script>");
            out.println("<span class=\"ahyo16\">You have attempted to upload a file which has already been uploaded for this context!</span><p><h1 class=\"ah14\">Please choose another file to upload or rename your file to try again.<p>Filename: <span class=\"ahy14\">" + s + "</span></h1>");
            HtmlClose1(out, session);
            gotomenu(out);
            out.println("<p>");
            out.println("&nbsp&nbsp<a class=\"ah10\" href=\"javascript:help_win('help/fileuploaderrorhelp.html')\">File Upload Error Help</a>");
            HtmlClose2(out);
        }
    }
}

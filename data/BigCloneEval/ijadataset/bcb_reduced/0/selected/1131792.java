package fll.web.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import net.mtu.eggplant.util.sql.SQLFunctions;
import net.mtu.eggplant.xml.NodelistElementCollectionAdapter;
import net.mtu.eggplant.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import fll.Team;
import fll.db.Queries;
import fll.web.ApplicationAttributes;
import fll.web.BaseFLLServlet;
import fll.web.SessionAttributes;

/**
 * Commit the changes made by editTeam.jsp.
 * 
 */
@WebServlet("/admin/subjective-data.fll")
public class DownloadSubjectiveData extends BaseFLLServlet {

    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response, final ServletContext application, final HttpSession session) throws IOException, ServletException {
        final DataSource datasource = SessionAttributes.getDataSource(session);
        try {
            final Connection connection = datasource.getConnection();
            final Document challengeDocument = ApplicationAttributes.getChallengeDocument(application);
            if (Queries.isJudgesProperlyAssigned(connection, challengeDocument)) {
                response.reset();
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "filename=subjective-data.fll");
                writeSubjectiveScores(connection, challengeDocument, response.getOutputStream());
            } else {
                response.reset();
                response.setContentType("text/plain");
                final ServletOutputStream os = response.getOutputStream();
                os.println("Judges are not properly assigned, please go back to the administration page and assign judges");
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
   * Create a document to hold subject scores for the tournament described in
   * challengeDocument.
   * 
   * @param challengeDocument describes the tournament
   * @param teams the teams for this tournament
   * @param connection the database connection used to retrieve the judge
   *          information
   * @param currentTournament the tournament to generate the document for, used
   *          for deciding which set of judges to use
   * @return the document
   */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = { "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING" }, justification = "Category determines table name")
    public static Document createSubjectiveScoresDocument(final Document challengeDocument, final Collection<Team> teams, final Connection connection, final int currentTournament) throws SQLException {
        ResultSet rs = null;
        ResultSet rs2 = null;
        PreparedStatement prep = null;
        PreparedStatement prep2 = null;
        try {
            prep = connection.prepareStatement("SELECT id, event_division FROM Judges WHERE category = ? AND Tournament = ?");
            prep.setInt(2, currentTournament);
            final Document document = XMLUtils.DOCUMENT_BUILDER.newDocument();
            final Element top = document.createElement("scores");
            document.appendChild(top);
            for (final Element categoryDescription : new NodelistElementCollectionAdapter(challengeDocument.getDocumentElement().getElementsByTagName("subjectiveCategory"))) {
                final String categoryName = categoryDescription.getAttribute("name");
                final Element categoryElement = document.createElement(categoryName);
                top.appendChild(categoryElement);
                prep.setString(1, categoryName);
                rs = prep.executeQuery();
                while (rs.next()) {
                    final String judge = rs.getString(1);
                    final String division = rs.getString(2);
                    for (final Team team : teams) {
                        final String teamDiv = Queries.getEventDivision(connection, team.getTeamNumber());
                        if (Judges.ALL_DIVISIONS.equals(division) || division.equals(teamDiv)) {
                            final Element scoreElement = document.createElement("score");
                            categoryElement.appendChild(scoreElement);
                            scoreElement.setAttribute("teamName", team.getTeamName());
                            scoreElement.setAttribute("teamNumber", String.valueOf(team.getTeamNumber()));
                            scoreElement.setAttribute("division", teamDiv);
                            scoreElement.setAttribute("organization", team.getOrganization());
                            scoreElement.setAttribute("judge", judge);
                            scoreElement.setAttribute("NoShow", "false");
                            prep2 = connection.prepareStatement("SELECT * FROM " + categoryName + " WHERE TeamNumber = ? AND Tournament = ? AND Judge = ?");
                            prep2.setInt(1, team.getTeamNumber());
                            prep2.setInt(2, currentTournament);
                            prep2.setString(3, judge);
                            rs2 = prep2.executeQuery();
                            if (rs2.next()) {
                                for (final Element goalDescription : new NodelistElementCollectionAdapter(categoryDescription.getElementsByTagName("goal"))) {
                                    final String goalName = goalDescription.getAttribute("name");
                                    final String value = rs2.getString(goalName);
                                    if (!rs2.wasNull()) {
                                        scoreElement.setAttribute(goalName, value);
                                    }
                                }
                                scoreElement.setAttribute("NoShow", rs2.getString("NoShow"));
                            }
                        }
                    }
                }
            }
            return document;
        } finally {
            SQLFunctions.close(rs);
            SQLFunctions.close(rs2);
            SQLFunctions.close(prep);
            SQLFunctions.close(prep2);
        }
    }

    /**
   * Write out the subjective scores data for the current tournament.
   * 
   * @param stream where to write the scores file
   * @throws IOException
   */
    public static void writeSubjectiveScores(final Connection connection, final Document challengeDocument, final OutputStream stream) throws IOException, SQLException {
        final Map<Integer, Team> tournamentTeams = Queries.getTournamentTeams(connection);
        final int tournament = Queries.getCurrentTournament(connection);
        final ZipOutputStream zipOut = new ZipOutputStream(stream);
        final Charset charset = Charset.forName("UTF-8");
        final Writer writer = new OutputStreamWriter(zipOut, charset);
        zipOut.putNextEntry(new ZipEntry("challenge.xml"));
        XMLUtils.writeXML(challengeDocument, writer, "UTF-8");
        zipOut.closeEntry();
        zipOut.putNextEntry(new ZipEntry("score.xml"));
        final Document scoreDocument = DownloadSubjectiveData.createSubjectiveScoresDocument(challengeDocument, tournamentTeams.values(), connection, tournament);
        XMLUtils.writeXML(scoreDocument, writer, "UTF-8");
        zipOut.closeEntry();
        zipOut.close();
    }
}

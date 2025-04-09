package ch.articlefox.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ch.articlefox.ArticleFoxConstants;
import ch.articlefox.Phases;
import ch.articlefox.SessionConstants;
import ch.articlefox.db.TArticles;
import ch.articlefox.db.TCategories;
import ch.articlefox.db.TTexts;
import ch.articlefox.db.TUsers;
import ch.articlefox.init.ArticleFoxConfig;
import ch.articlefox.localization.Dictionaries;
import ch.articlefox.localization.Dictionary;
import ch.articlefox.utils.AttachmentInfo;
import ch.articlefox.utils.AttachmentInfoComparator;
import ch.articlefox.utils.AttachmentsAndComments;
import ch.articlefox.utils.DateUtils;
import ch.articlefox.utils.IOUtils;
import ch.articlefox.utils.StringUtils;
import ch.articlefox.utils.servlet.ServletResponseTools;
import com.orelias.infoaccess.InfoAccess;
import com.orelias.infoaccess.InfoBean;

/**
 * A servlet to generate dynamic content.
 * 
 * @author Lukas Blunschi
 */
public class GeneratedServlet extends HttpServlet {

    public static final String P_TYPE = "type";

    public static final String P_ID = "id";

    private static final long serialVersionUID = 1;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long begin = System.nanoTime();
        Integer userId = (Integer) req.getSession().getAttribute(SessionConstants.A_USERID);
        if (userId == null) {
            StringBuffer msg = new StringBuffer();
            msg.append("Please login before downloading generated content.");
            ServletResponseTools.streamStringBuffer(msg, null, "text/html", "utf-8", 0, req, resp);
            return;
        }
        ServletContext ctx = req.getSession().getServletContext();
        InfoAccess infoaccess = (InfoAccess) ctx.getAttribute(ArticleFoxConstants.A_INFOACCESS);
        ArticleFoxConfig config = (ArticleFoxConfig) ctx.getAttribute(ArticleFoxConstants.A_CONFIG);
        Dictionary dict = Dictionaries.getDictionaryFromSession(req);
        String uploadPath = (String) ctx.getAttribute(ArticleFoxConstants.A_UPLOADPATH);
        String type = req.getParameter(P_TYPE);
        String idStr = req.getParameter(P_ID);
        Integer id = null;
        if (idStr != null) {
            try {
                id = Integer.parseInt(idStr);
            } catch (Exception e) {
                id = null;
            }
        }
        StringBuffer buf = null;
        File zipFile = null;
        if (type == null || idStr == null) {
            buf = new StringBuffer("Not all required parameters given!");
        } else {
            if (type.equals(TTexts.TBL_NAME)) {
                InfoBean text = infoaccess.get(type, id, true);
                buf = getArticleText(text);
            } else if (type.equals(TArticles.TBL_NAME)) {
                InfoBean article = infoaccess.get(type, id, true);
                zipFile = getArticleZip(infoaccess, config, dict, uploadPath, article);
            } else if (type.equals(TUsers.TBL_NAME)) {
                Collection<InfoBean> users = infoaccess.query(TUsers.TBL_NAME, null, TUsers.F_USERNAME, true);
                buf = getUserList(users, dict);
            } else {
                buf = new StringBuffer("You are not allowed to get this type!");
            }
        }
        InfoBean userLoggedIn = infoaccess.get(TUsers.TBL_NAME, userId, true);
        String username = userLoggedIn.getString(TUsers.F_USERNAME);
        double calcTime = (System.nanoTime() - begin) / 1E6;
        if (buf == null) {
            ServletResponseTools.streamFile(zipFile, username, req, resp, -1);
        } else {
            ServletResponseTools.streamStringBuffer(buf, username, "text/plain", "utf-8", calcTime, req, resp);
        }
    }

    /**
	 * Get text representation of given text.
	 * 
	 * @param text
	 * @return
	 */
    private StringBuffer getArticleText(InfoBean text) {
        StringBuffer buf = new StringBuffer();
        buf.append("Title:\n");
        buf.append(StringUtils.replaceConfiguredChars(text.getString(TTexts.F_TITLE))).append("\n\n");
        buf.append("Lead:\n");
        buf.append(StringUtils.replaceConfiguredChars(text.getString(TTexts.F_LEAD))).append("\n\n");
        buf.append("Text:\n");
        buf.append(StringUtils.replaceConfiguredChars(text.getString(TTexts.F_TEXT))).append("\n\n");
        buf.append("Attachments:\n");
        buf.append(text.getString(TTexts.F_ATTACHMENTS)).append("\n");
        return buf;
    }

    /**
	 * Get CSV (comma separated values) list of all users.
	 * 
	 * @param users
	 * @param dict
	 * @return
	 */
    private StringBuffer getUserList(Collection<InfoBean> users, Dictionary dict) {
        StringBuffer buf = new StringBuffer();
        buf.append(dict.username()).append(";");
        buf.append(dict.isAdmin()).append(";");
        buf.append(dict.isEditor()).append(";");
        buf.append(dict.nickname()).append(";");
        buf.append(dict.firstname()).append(";");
        buf.append(dict.lastname()).append(";");
        buf.append(dict.language()).append(";");
        buf.append(dict.address()).append(";");
        buf.append(dict.email()).append(";");
        buf.append(dict.mobile()).append(";\n");
        for (InfoBean user : users) {
            Integer language = (Integer) user.getProperty(TUsers.F_LANGUAGE);
            Boolean isAdmin = (Boolean) user.getProperty(TUsers.F_ISADMIN);
            Boolean isEditor = (Boolean) user.getProperty(TUsers.F_ISEDITOR);
            buf.append(user.getString(TUsers.F_USERNAME)).append(";");
            buf.append(isAdmin ? "1" : "").append(";");
            buf.append(isEditor ? "1" : "").append(";");
            buf.append(user.getString(TUsers.F_NICKNAME)).append(";");
            buf.append(user.getString(TUsers.F_FIRSTNAME)).append(";");
            buf.append(user.getString(TUsers.F_LASTNAME)).append(";");
            buf.append(dict.getLanguageName(language)).append(";");
            buf.append(user.getString(TUsers.F_ADDRESS)).append(";");
            buf.append(user.getString(TUsers.F_EMAIL)).append(";");
            buf.append(user.getString(TUsers.F_MOBILE)).append(";\n");
        }
        return buf;
    }

    /**
	 * Get zip file containing given text and attachments.
	 * 
	 * @param infoaccess
	 * @param config
	 * @param dict
	 * @param uploadPath
	 * @param article
	 * @return
	 * @throws IOException
	 */
    private File getArticleZip(InfoAccess infoaccess, ArticleFoxConfig config, Dictionary dict, String uploadPath, InfoBean article) throws IOException {
        boolean prefixComments = config.isDownloadPrefixCommentsWithLanguage();
        boolean generateSeparate = config.isDownloadGenerateSeparateTexts();
        boolean generateCombined = config.isDownloadGenerateCombinedText();
        boolean utf8 = config.isDownloadGenerateUTF8();
        boolean utf16 = config.isDownloadGenerateUTF16();
        Date articleDate = (Date) article.getProperty(TArticles.F_DATE);
        String dateStr = DateUtils.dateFormatter.format(articleDate);
        String categoryName = article.getOneRelation(TArticles.F_CATEGORY).getString(TCategories.F_NAME);
        String downloadName = dateStr + "_" + categoryName + "_" + article.getString(TArticles.F_NAME);
        downloadName = downloadName.replaceAll("\\s", "-");
        downloadName = StringUtils.removeSpecialChars(downloadName);
        new File(ArticleFoxConstants.TMP_DIR_PATH).mkdirs();
        File file = new File(ArticleFoxConstants.TMP_DIR_PATH + downloadName + "_" + new Date().getTime() + ".zip");
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(file));
        final String topDirPath = downloadName + "/";
        zipOut.putNextEntry(new ZipEntry(topDirPath));
        Map<Integer, InfoBean> latestOfTask = new HashMap<Integer, InfoBean>();
        String filter = TTexts.F_ARTICLE + "=" + article.getId();
        Collection<InfoBean> coll = infoaccess.query(TTexts.TBL_NAME, filter, null, true);
        for (InfoBean text : coll) {
            Integer curPhase = (Integer) text.getProperty(TTexts.F_PHASE);
            Integer curLanguage = (Integer) text.getProperty(TTexts.F_LANGUAGE);
            if (curPhase >= Phases.CORRECT) {
                latestOfTask.put(curLanguage, text);
            }
        }
        final String uploadDirPath = uploadPath + article.getId() + "/";
        Map<String, AttachmentInfo> attachmentInfos = new HashMap<String, AttachmentInfo>();
        for (InfoBean text : latestOfTask.values()) {
            Integer curPhase = (Integer) text.getProperty(TTexts.F_PHASE);
            Integer curLanguage = (Integer) text.getProperty(TTexts.F_LANGUAGE);
            String title = text.getString(TTexts.F_TITLE);
            String lead = text.getString(TTexts.F_LEAD);
            String textStr = text.getString(TTexts.F_TEXT);
            if (title.trim().length() > 0 || lead.trim().length() > 0 || textStr.trim().length() > 0) {
                String shortTaskName = dict.getShortTaskName(curPhase, curLanguage);
                String taskDirPath = topDirPath + shortTaskName + "/";
                zipOut.putNextEntry(new ZipEntry(taskDirPath));
                if (generateSeparate) {
                    if (title.trim().length() > 0) {
                        title = StringUtils.replaceConfiguredChars(title);
                        createTextFile(zipOut, title, taskDirPath + "1_title", utf8, utf16);
                    }
                    if (lead.trim().length() > 0) {
                        lead = StringUtils.replaceConfiguredChars(lead);
                        createTextFile(zipOut, lead, taskDirPath + "2_lead", utf8, utf16);
                    }
                    if (textStr.trim().length() > 0) {
                        textStr = StringUtils.replaceConfiguredChars(textStr);
                        createTextFile(zipOut, textStr, taskDirPath + "3_text", utf8, utf16);
                    }
                }
                if (generateCombined) {
                    StringBuffer buf = new StringBuffer();
                    if (title.trim().length() > 0) {
                        title = StringUtils.replaceConfiguredChars(title);
                        buf.append(dict.title()).append(":\n");
                        buf.append(title);
                        buf.append("\n\n");
                    }
                    if (lead.trim().length() > 0) {
                        lead = StringUtils.replaceConfiguredChars(lead);
                        buf.append(dict.lead()).append(":\n");
                        buf.append(lead);
                        buf.append("\n\n");
                    }
                    if (textStr.trim().length() > 0) {
                        textStr = StringUtils.replaceConfiguredChars(textStr);
                        buf.append(dict.text()).append(":\n");
                        buf.append(textStr);
                        buf.append("\n\n");
                    }
                    String all = buf.toString();
                    createTextFile(zipOut, all, taskDirPath + "9_all-combined", utf8, utf16);
                }
            }
            String attachmentsStr = text.getString(TTexts.F_ATTACHMENTS);
            HashMap<String, String> attachmentMap = AttachmentsAndComments.getAttachmentMap(attachmentsStr);
            List<String> comments = new ArrayList<String>(attachmentMap.values());
            Collections.sort(comments);
            for (Map.Entry<String, String> attEntry : attachmentMap.entrySet()) {
                String filename = attEntry.getKey();
                String comment = attEntry.getValue();
                int priority = 0;
                for (priority = 0; priority < comments.size(); priority++) {
                    if (comment.equals(comments.get(priority))) {
                        break;
                    }
                }
                priority++;
                int pos = comment.indexOf(":");
                if (pos != -1) {
                    try {
                        Integer.parseInt(comment.substring(0, pos));
                        comment = comment.substring(pos + 1).trim();
                    } catch (NumberFormatException nfe) {
                    }
                }
                if (prefixComments) {
                    String language = dict.getLanguageName(curLanguage);
                    comment = language + ":\n" + comment;
                }
                AttachmentInfo info = attachmentInfos.get(filename);
                if (info == null) {
                    info = new AttachmentInfo(filename);
                    attachmentInfos.put(filename, info);
                }
                info.addComment(priority, comment);
            }
        }
        if (attachmentInfos.size() > 0) {
            String attachmentsDirPath = topDirPath + "attachments/";
            List<AttachmentInfo> infos = new ArrayList<AttachmentInfo>(attachmentInfos.values());
            Collections.sort(infos, new AttachmentInfoComparator());
            for (AttachmentInfo info : infos) {
                String filename = info.getFilename();
                String comments = info.getComments();
                int priority = info.getAvgPriority();
                String priorityStr = priority < 10 ? "0" + priority + "_" : String.valueOf(priority) + "_";
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                FileInputStream bytesIn = new FileInputStream(uploadDirPath + filename);
                IOUtils.pipe(bytesIn, bytesOut);
                bytesIn.close();
                bytesOut.close();
                byte[] bytes = bytesOut.toByteArray();
                zipOut.putNextEntry(new ZipEntry(attachmentsDirPath + priorityStr + filename));
                zipOut.write(bytes, 0, bytes.length);
                createTextFile(zipOut, comments, attachmentsDirPath + priorityStr + filename + "_comment", utf8, utf16);
            }
        }
        zipOut.close();
        return file;
    }

    private void createTextFile(ZipOutputStream zipOut, String str, String basename, boolean utf8, boolean utf16) throws IOException {
        byte[] bytes = null;
        if (utf8) {
            bytes = str.getBytes("utf-8");
            zipOut.putNextEntry(new ZipEntry(basename + ".txt"));
            zipOut.write(bytes, 0, bytes.length);
        }
        if (utf16) {
            bytes = str.getBytes("utf-16");
            zipOut.putNextEntry(new ZipEntry(basename + ".utf-16.txt"));
            zipOut.write(bytes, 0, bytes.length);
        }
    }
}

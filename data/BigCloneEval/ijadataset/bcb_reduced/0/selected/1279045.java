package com.sitescape.team.web.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import com.sitescape.team.domain.Attachment;
import com.sitescape.team.domain.DefinableEntity;
import com.sitescape.team.domain.Description;
import com.sitescape.team.domain.FileAttachment;
import com.sitescape.team.domain.EntityIdentifier.EntityType;
import com.sitescape.team.module.definition.DefinitionUtils;
import com.sitescape.team.portletadapter.AdaptedPortletURL;
import com.sitescape.team.portletadapter.MultipartFileSupport;
import com.sitescape.team.repository.RepositoryUtil;
import com.sitescape.team.util.FileUploadItem;
import com.sitescape.team.util.SZoneConfig;
import com.sitescape.team.util.SimpleMultipartFile;
import com.sitescape.team.util.TempFileUtil;
import com.sitescape.team.web.WebKeys;
import com.sitescape.util.Validator;

public class WebHelper {

    protected static Log logger = LogFactory.getLog(WebHelper.class);

    public static boolean isUserLoggedIn(HttpServletRequest request) {
        try {
            getRequiredUserName(request);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static boolean isUserLoggedIn(PortletRequest request) {
        try {
            getRequiredUserName(request);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static String getRequiredUserName(HttpServletRequest request) throws IllegalStateException {
        String username = request.getRemoteUser();
        if (username != null) return username;
        HttpSession ses = request.getSession(false);
        if (ses == null) throw new IllegalStateException("No user session");
        if (ses.getAttribute(WebKeys.USER_NAME) == null) throw new IllegalStateException("No user name in the session");
        return (String) ses.getAttribute(WebKeys.USER_NAME);
    }

    public static String getRequiredUserName(PortletRequest request) throws IllegalStateException {
        String username = request.getRemoteUser();
        if (username != null) return username;
        PortletSession ses = request.getPortletSession(false);
        if (ses == null) throw new IllegalStateException("No user session");
        if (ses.getAttribute(WebKeys.USER_NAME, PortletSession.APPLICATION_SCOPE) == null) throw new IllegalStateException("No user name in the session");
        return (String) ses.getAttribute(WebKeys.USER_NAME, PortletSession.APPLICATION_SCOPE);
    }

    public static String getRequiredZoneName(HttpServletRequest request) throws IllegalStateException {
        String zoneName = null;
        HttpSession ses = request.getSession(false);
        if (ses != null) zoneName = (String) ses.getAttribute(WebKeys.ZONE_NAME);
        if (zoneName == null) zoneName = SZoneConfig.getDefaultZoneName();
        return zoneName;
    }

    public static String getRequiredZoneName(PortletRequest request) throws IllegalStateException {
        String zoneName = null;
        PortletSession ses = request.getPortletSession(false);
        if (ses != null) zoneName = (String) ses.getAttribute(WebKeys.ZONE_NAME, PortletSession.APPLICATION_SCOPE);
        if (zoneName == null) zoneName = SZoneConfig.getDefaultZoneName();
        return zoneName;
    }

    public static PortletSession getRequiredPortletSession(PortletRequest request) throws IllegalStateException {
        String username = request.getRemoteUser();
        PortletSession ses = request.getPortletSession(false);
        if (ses != null) {
            if (ses.getAttribute(WebKeys.USER_NAME, PortletSession.APPLICATION_SCOPE) == null) {
                if (username != null) {
                    ses.setAttribute(WebKeys.USER_NAME, username, PortletSession.APPLICATION_SCOPE);
                } else {
                    throw new IllegalStateException("No valid session - Illegal request sequence.");
                }
            }
        } else {
            if (username != null) {
                ses = request.getPortletSession();
                ses.setAttribute(WebKeys.USER_NAME, username, PortletSession.APPLICATION_SCOPE);
            } else {
                throw new IllegalStateException("No valid session - Illegal request sequence");
            }
        }
        return ses;
    }

    public static HttpSession getRequiredSession(HttpServletRequest request) throws IllegalStateException {
        String username = request.getRemoteUser();
        HttpSession ses = request.getSession(false);
        if (ses != null) {
            if (ses.getAttribute(WebKeys.USER_NAME) == null) {
                if (username != null) {
                    ses.setAttribute(WebKeys.USER_NAME, username);
                } else {
                    throw new IllegalStateException("No valid session - Illegal request sequence.");
                }
            }
        } else {
            if (username != null) {
                ses = request.getSession();
                ses.setAttribute(WebKeys.USER_NAME, username);
            } else {
                throw new IllegalStateException("No valid session - Illegal request sequence");
            }
        }
        return ses;
    }

    /**
	 * Returns a handle on the uploaded file. This handle is guaranteed to be
	 * valid only during the current server session. In other words, the handle
	 * is not persistent and will be lost once the server shuts down.
	 * It returns <code>null</code> if there is no uploaded file.
	 * 
	 * @param request
	 * @return
	 */
    public static String getFileHandleOnUploadedFile(ActionRequest request) throws IOException {
        Map fileMap = null;
        if (request instanceof MultipartFileSupport) fileMap = ((MultipartFileSupport) request).getFileMap();
        if (fileMap == null || fileMap.size() == 0) return null;
        MultipartFile file = (MultipartFile) fileMap.values().iterator().next();
        String fileName = file.getOriginalFilename();
        String prefix = String.valueOf(fileName.length()) + "-" + fileName + "_";
        File destFile = TempFileUtil.createTempFile(prefix);
        file.transferTo(destFile);
        return destFile.getName();
    }

    /**
	 * Wraps the file handle in a MultipartFile datastructure. This is used
	 * primarily to put the data in an argument format compatible with some
	 * of the module methods. 
	 * 
	 * @param fileHandle
	 * @return
	 * @throws IOException
	 */
    public static MultipartFile wrapFileHandleInMultipartFile(String fileHandle) throws IOException {
        int idx = fileHandle.indexOf("-");
        int fileNameLength = Integer.parseInt(fileHandle.substring(0, idx));
        String fileName = fileHandle.substring(idx + 1, idx + 1 + fileNameLength);
        File file = new File(TempFileUtil.getTempFileDir(), fileHandle);
        SimpleMultipartFile mf = new SimpleMultipartFile(fileName, file, true);
        return mf;
    }

    /**
	 * Returns the file name of the uploaded file
	 * 
	 * @param fileHandle
	 * @return fileName
	 */
    public static String getFileName(String fileHandle) {
        int idx = fileHandle.indexOf("-");
        if (idx < 0) return null;
        int fileNameLength = Integer.parseInt(fileHandle.substring(0, idx));
        return fileHandle.substring(idx + 1, idx + 1 + fileNameLength);
    }

    public static void readFileHandleContent(String fileHandle, OutputStream out) throws IOException {
        File file = new File(TempFileUtil.getTempFileDir(), fileHandle);
        FileCopyUtils.copy(new BufferedInputStream(new FileInputStream(file)), out);
    }

    /**
	 * Clean up resources associated with the file handle. Must be called
	 * when the application is done with the file handle.
	 * 
	 * @param fileHandle
	 */
    public static void releaseFileHandle(String fileHandle) {
        File file = new File(TempFileUtil.getTempFileDir(), fileHandle);
        file.delete();
    }

    /**
	 * Parse a description looking for uploaded file references
	 * 
	 * @param Description
	 * @param File
	 * @return 
	 */
    public static void scanDescriptionForUploadFiles(Description description, List fileData) {
        String fileHandle = "";
        Pattern pattern = Pattern.compile("(<img [^>]*src=\"https?://[^>]*viewType=ss_viewUploadFile[^>]*>)");
        Matcher m = pattern.matcher(description.getText());
        int loopDetector = 0;
        while (m.find()) {
            if (loopDetector > 2000) {
                logger.error("Error processing markup: " + description.getText());
                break;
            }
            String img = m.group(0);
            Pattern p2 = Pattern.compile("fileId=([^\\&\"]*)");
            Matcher m2 = p2.matcher(img);
            if (m2.find() && m2.groupCount() >= 1) fileHandle = m2.group(1);
            if (!fileHandle.equals("")) {
                MultipartFile myFile = null;
                try {
                    myFile = WebHelper.wrapFileHandleInMultipartFile(fileHandle);
                } catch (IOException e) {
                    return;
                }
                if (myFile != null) {
                    String fileName = myFile.getOriginalFilename();
                    if (fileName.equals("")) return;
                    String repositoryName = RepositoryUtil.getDefaultRepositoryName();
                    FileUploadItem fui = new FileUploadItem(FileUploadItem.TYPE_ATTACHMENT, null, myFile, repositoryName);
                    fileData.add(fui);
                }
            }
            Pattern p3 = Pattern.compile("src *= *\"([^\"]*)\"");
            Matcher m3 = p3.matcher(img);
            if (m3.find() && m3.groupCount() >= 1) {
                img = m3.replaceFirst("src=\"{{attachmentUrl: " + WebHelper.getFileName(fileHandle) + "}}\"");
                description.setText(m.replaceFirst(img));
                m = pattern.matcher(description.getText());
            }
        }
    }

    public static void scanDescriptionForAttachmentFileUrls(Description description) {
        Pattern pattern = Pattern.compile("(<img [^>]*src=\"https?://[^>]*viewType=ss_viewAttachmentFile[^>]*>)");
        Matcher m = pattern.matcher(description.getText());
        int loopDetector = 0;
        while (m.find()) {
            if (loopDetector > 2000) {
                logger.error("Error processing markup: " + description.getText());
                break;
            }
            String fileId = "";
            String img = m.group(0);
            Pattern p2 = Pattern.compile("fileId=([^\\&\"]*)");
            Matcher m2 = p2.matcher(img);
            if (m2.find() && m2.groupCount() >= 1) fileId = m2.group(1).trim();
            String binderId = "";
            img = m.group(0);
            Pattern p3 = Pattern.compile("binderId=([^\\&\"]*)");
            Matcher m3 = p3.matcher(img);
            if (m3.find() && m3.groupCount() >= 1) binderId = m3.group(1).trim();
            String entryId = "";
            img = m.group(0);
            Pattern p4 = Pattern.compile("entryId=([^\\&\"]*)");
            Matcher m4 = p4.matcher(img);
            if (m4.find() && m4.groupCount() >= 1) entryId = m4.group(1).trim();
            if (!fileId.equals("")) {
                Pattern p1 = Pattern.compile("src *= *\"([^\"]*)\"");
                Matcher m1 = p1.matcher(img);
                if (m1.find() && m1.groupCount() >= 1) {
                    img = m1.replaceFirst("src=\"{{attachmentFileId: fileId=" + fileId + "&amp;binderId=" + binderId + "&amp;entryId=" + entryId + "}}\"");
                    description.setText(m.replaceFirst(img));
                    m = pattern.matcher(description.getText());
                }
            }
        }
    }

    public static String markupStringReplacement(RenderRequest req, RenderResponse res, HttpServletRequest httpReq, HttpServletResponse httpRes, DefinableEntity entity, String inputString, String type) {
        Long binderId = null;
        Long entryId = null;
        if (entity != null) {
            String entityType = entity.getEntityIdentifier().getEntityType().name();
            if (entityType.equals(EntityType.workspace.name()) || entityType.equals(EntityType.folder.name()) || entityType.equals(EntityType.profiles.name())) {
                binderId = entity.getId();
            } else if (entityType.equals(EntityType.folderEntry.name())) {
                binderId = entity.getParentBinder().getId();
                entryId = entity.getId();
            }
        }
        return markupStringReplacement(req, res, httpReq, httpRes, entity, inputString, type, binderId, entryId);
    }

    public static String markupStringReplacement(RenderRequest req, RenderResponse res, HttpServletRequest httpReq, HttpServletResponse httpRes, DefinableEntity entity, String inputString, String type, Long binderId, Long entryId) {
        String outputString = new String(inputString);
        outputString = outputString.replaceAll("%20", " ");
        outputString = outputString.replaceAll("%7B", "{");
        outputString = outputString.replaceAll("%7D", "}");
        int loopDetector;
        try {
            if (httpReq != null && binderId != null) {
                Pattern p1 = Pattern.compile("(\\{\\{attachmentUrl: ([^}]*)\\}\\})");
                Matcher m1 = p1.matcher(outputString);
                loopDetector = 0;
                while (m1.find()) {
                    if (loopDetector > 2000) {
                        logger.error("Error processing markup: " + inputString);
                        return outputString;
                    }
                    loopDetector++;
                    String url = m1.group(2);
                    String webUrl = WebUrlUtil.getServletRootURL(httpReq) + WebKeys.SERVLET_VIEW_FILE + "?";
                    if (entity != null) {
                        FileAttachment fa = entity.getFileAttachment(url.trim());
                        if (fa != null) {
                            webUrl += WebKeys.URL_FILE_ID + "=" + fa.getId().toString() + "&amp;";
                        } else {
                            webUrl += WebKeys.URL_FILE_TITLE + "=" + url.trim() + "&amp;";
                        }
                    } else {
                        webUrl += WebKeys.URL_FILE_TITLE + "=" + url.trim() + "&amp;";
                    }
                    webUrl += WebKeys.URL_FILE_VIEW_TYPE + "=" + WebKeys.FILE_VIEW_TYPE_ATTACHMENT_FILE + "&amp;";
                    webUrl += WebKeys.URL_BINDER_ID + "=" + binderId.toString() + "&amp;";
                    if (entryId != null) {
                        webUrl += WebKeys.URL_ENTRY_ID + "=" + entryId.toString() + "&amp;";
                    }
                    outputString = m1.replaceFirst(webUrl);
                }
            }
            if (type.equals(WebKeys.MARKUP_VIEW) || type.equals(WebKeys.MARKUP_FORM)) {
                Pattern p2 = Pattern.compile("(\\{\\{attachmentFileId: ([^}]*)\\}\\})");
                Matcher m2 = p2.matcher(outputString);
                loopDetector = 0;
                while (m2.find()) {
                    if (loopDetector > 2000) {
                        logger.error("Error processing markup: " + inputString);
                        return outputString;
                    }
                    loopDetector++;
                    String fileIds = m2.group(2).trim();
                    String webUrl = WebUrlUtil.getServletRootURL(httpReq) + WebKeys.SERVLET_VIEW_FILE + "?";
                    webUrl += WebKeys.URL_FILE_VIEW_TYPE + "=" + WebKeys.FILE_VIEW_TYPE_ATTACHMENT_FILE + "&amp;";
                    webUrl += fileIds;
                    outputString = m2.replaceFirst(webUrl);
                    m2 = p2.matcher(outputString);
                }
            }
            if (type.equals(WebKeys.MARKUP_VIEW)) {
                Pattern p2 = Pattern.compile("(\\{\\{titleUrl: ([^\\}]*)\\}\\})");
                Matcher m2 = p2.matcher(outputString);
                loopDetector = 0;
                while (m2.find()) {
                    if (loopDetector > 2000) {
                        logger.error("Error processing markup: " + inputString);
                        return outputString;
                    }
                    loopDetector++;
                    String urlParts = m2.group(2).trim();
                    String s_binderId = "";
                    Pattern p3 = Pattern.compile("binderId=([^ ]*)");
                    Matcher m3 = p3.matcher(urlParts);
                    if (m3.find() && m3.groupCount() >= 1) s_binderId = m3.group(1).trim();
                    String normalizedTitle = "";
                    Pattern p4 = Pattern.compile("title=([^ ]*)");
                    Matcher m4 = p4.matcher(urlParts);
                    if (m4.find() && m4.groupCount() >= 1) normalizedTitle = m4.group(1).trim();
                    String title = "";
                    Pattern p5 = Pattern.compile("text=(.*)$");
                    Matcher m5 = p5.matcher(urlParts);
                    if (m5.find() && m5.groupCount() >= 1) title = m5.group(1).trim();
                    String titleLink = "";
                    String action = WebKeys.ACTION_VIEW_FOLDER_ENTRY;
                    Map params = new HashMap();
                    params.put(WebKeys.URL_BINDER_ID, new String[] { s_binderId });
                    params.put(WebKeys.URL_NORMALIZED_TITLE, new String[] { normalizedTitle });
                    String webUrl = getPortletUrl(req, res, httpReq, httpRes, action, true, params);
                    titleLink = "<a href=\"" + webUrl + "\" ";
                    titleLink += "onClick=\"if (self.ss_openTitleUrl) return self.ss_openTitleUrl(this);\">";
                    titleLink += "<span class=\"ss_title_link\">";
                    titleLink += title + "</span></a>";
                    titleLink = titleLink.replaceAll("&", "&amp;");
                    outputString = outputString.substring(0, m2.start(0)) + titleLink + outputString.substring(m2.end(), outputString.length());
                    m2 = p2.matcher(outputString);
                }
            }
            if (binderId != null && (type.equals(WebKeys.MARKUP_VIEW) || type.equals(WebKeys.MARKUP_FILE))) {
                String action = WebKeys.ACTION_VIEW_FOLDER_ENTRY;
                Pattern p3 = Pattern.compile("(\\[\\[([^\\]]*)\\]\\])");
                Matcher m3 = p3.matcher(outputString);
                loopDetector = 0;
                while (m3.find()) {
                    if (loopDetector > 2000) {
                        logger.error("Error processing markup: " + inputString);
                        return outputString;
                    }
                    loopDetector++;
                    String title = m3.group(2).trim();
                    String normalizedTitle = getNormalizedTitle(title);
                    if (!normalizedTitle.equals("")) {
                        String titleLink = "";
                        if (type.equals(WebKeys.MARKUP_VIEW)) {
                            Map params = new HashMap();
                            params.put(WebKeys.URL_BINDER_ID, binderId.toString());
                            params.put(WebKeys.URL_NORMALIZED_TITLE, normalizedTitle);
                            String webUrl = getPortletUrl(req, res, httpReq, httpRes, action, true, params);
                            titleLink = "<a href=\"" + webUrl + "\" ";
                            titleLink += "onClick=\"if (self.ss_openTitleUrl) return self.ss_openTitleUrl(this);\">";
                            titleLink += "<span class=\"ss_title_link\">";
                            titleLink += title + "</span></a>";
                        } else {
                            titleLink = "{{titleUrl: " + WebKeys.URL_BINDER_ID + "=" + binderId.toString();
                            titleLink += " " + WebKeys.URL_NORMALIZED_TITLE + "=" + normalizedTitle;
                            titleLink += " text=" + title + "}}";
                        }
                        outputString = outputString.substring(0, m3.start(0)) + titleLink + outputString.substring(m3.end(), outputString.length());
                        m3 = p3.matcher(outputString);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error processing markup: " + inputString);
            return inputString;
        }
        return outputString;
    }

    public static String getNormalizedTitle(String title) {
        Pattern p1 = Pattern.compile("(\\&[^;]*;)");
        Matcher m1 = p1.matcher(title);
        int loopDetector = 0;
        while (m1.find()) {
            if (loopDetector > 2000) {
                logger.error("Error processing markup: " + title);
                break;
            }
            title = m1.replaceFirst(" ");
            m1 = p1.matcher(title);
        }
        String normalTitle = title.replaceAll("[\\P{L}&&\\P{N}]", " ");
        normalTitle = normalTitle.replaceAll(" ++", "_");
        normalTitle = normalTitle.toLowerCase();
        return normalTitle;
    }

    public static String getPortletUrl(RenderRequest req, RenderResponse res, HttpServletRequest httpReq, HttpServletResponse httpRes, String action, boolean actionUrl, Map params) {
        return getPortletUrl(req, res, httpReq, httpRes, action, actionUrl, params, false, "ss_forum");
    }

    public static String getPortletUrl(RenderRequest req, RenderResponse res, HttpServletRequest httpReq, HttpServletResponse httpRes, String action, boolean actionUrl, Map params, boolean forceAdapter, String portletName) {
        if (forceAdapter) {
            if (!Validator.isNull(action)) {
                params.put("action", new String[] { action });
            }
            AdaptedPortletURL adapterUrl = new AdaptedPortletURL(req, portletName, actionUrl);
            Iterator it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry) it.next();
                adapterUrl.setParameter((String) me.getKey(), ((String[]) me.getValue())[0]);
            }
            return adapterUrl.toString();
        } else if (req == null || res == null) {
            if (!Validator.isNull(action)) {
                params.put("action", new String[] { action });
            }
            AdaptedPortletURL adapterUrl = new AdaptedPortletURL(httpReq, portletName, actionUrl);
            Iterator it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry) it.next();
                adapterUrl.setParameter((String) me.getKey(), ((String[]) me.getValue())[0]);
            }
            return adapterUrl.toString();
        } else {
            PortletURL portletURL = null;
            if (actionUrl) {
                portletURL = res.createActionURL();
            } else {
                portletURL = res.createRenderURL();
            }
            try {
                portletURL.setWindowState(new WindowState(WindowState.MAXIMIZED.toString()));
            } catch (Exception e) {
            }
            Iterator it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry) it.next();
                portletURL.setParameter((String) me.getKey(), (String) me.getValue());
            }
            if (!Validator.isNull(action)) {
                portletURL.setParameter("action", new String[] { action });
            }
            return portletURL.toString();
        }
    }
}

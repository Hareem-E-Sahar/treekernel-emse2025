package vqwiki.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import vqwiki.Change;
import vqwiki.ChangeLog;
import vqwiki.Environment;
import vqwiki.PseudoTopicHandler;
import vqwiki.SearchEngine;
import vqwiki.SearchResultEntry;
import vqwiki.Topic;
import vqwiki.WikiBase;
import vqwiki.servlets.beans.SitemapLineBean;
import vqwiki.utils.Utilities;
import TemplateEngine.Template;

public class ExportHTMLThread extends LongOperationThread {

    private String imageDir;

    private Exception exception;

    private File tempFile;

    private String tempdir;

    private String virtualWiki;

    private ServletContext ctx;

    private static final Logger logger = Logger.getLogger(ExportHTMLThread.class);

    public ExportHTMLThread(String virtualWiki, String tempdir, String imageDir, ServletContext ctx) {
        this.virtualWiki = virtualWiki;
        this.tempdir = tempdir;
        this.imageDir = imageDir;
        this.ctx = ctx;
    }

    /**
     * Do the long lasting operation
     */
    protected void onRun() {
        Environment en = Environment.getInstance();
        exception = null;
        BufferedOutputStream fos = null;
        try {
            tempFile = File.createTempFile("htmlexport", "zip", new File(tempdir));
            fos = new BufferedOutputStream(new FileOutputStream(tempFile));
            ZipOutputStream zipout = new ZipOutputStream(fos);
            zipout.setMethod(ZipOutputStream.DEFLATED);
            addAllTopics(en, zipout, 0, 80);
            addAllSpecialPages(en, zipout, 80, 10);
            addAllUploadedFiles(en, zipout, 90, 5);
            addAllImages(en, zipout, 95, 5);
            zipout.close();
            logger.debug("Closing zip and sending to user");
        } catch (Exception e) {
            logger.fatal("Exception", e);
            exception = e;
        }
        progress = PROGRESS_DONE;
    }

    /**
    *
    */
    private void addAllTopics(Environment en, ZipOutputStream zipout, int progressStart, int progressLength) throws Exception, IOException {
        HashMap containingTopics = new HashMap();
        if (virtualWiki == null || virtualWiki.length() < 1) {
            virtualWiki = WikiBase.DEFAULT_VWIKI;
        }
        SearchEngine sedb = WikiBase.getInstance().getSearchEngineInstance();
        Collection all = sedb.getAllTopicNames(virtualWiki);
        String defaultTopic = en.getDefaultTopic();
        if (defaultTopic == null || defaultTopic.length() < 2) {
            defaultTopic = "StartingPoints";
        }
        Template tpl;
        logger.debug("Logging Wiki " + virtualWiki + " starting at " + defaultTopic);
        List ignoreTheseTopicsList = new ArrayList();
        ignoreTheseTopicsList.add("WikiSearch");
        ignoreTheseTopicsList.add("RecentChanges");
        ignoreTheseTopicsList.add("WikiSiteMap");
        ignoreTheseTopicsList.add("WikiSiteMapIE");
        ignoreTheseTopicsList.add("WikiSiteMapNS");
        Iterator allIterator = all.iterator();
        int count = 0;
        while (allIterator.hasNext()) {
            progress = Math.min(progressStart + (int) ((double) count * (double) progressLength / (double) all.size()), 99);
            count++;
            String topicname = (String) allIterator.next();
            try {
                addTopicToZip(en, zipout, containingTopics, sedb, defaultTopic, ignoreTheseTopicsList, topicname);
            } catch (Exception e) {
                logger.fatal("Exception while adding a topic ", e);
            }
        }
        logger.debug("Done adding all topics.");
        List sitemapLines = new ArrayList();
        Vector visitedPages = new Vector();
        List startingList = new ArrayList(1);
        startingList.add(SitemapThread.LAST_IN_LIST);
        parsePages(defaultTopic, containingTopics, startingList, "1", sitemapLines, visitedPages);
        StringBuffer ieView = new StringBuffer();
        StringBuffer nsView = new StringBuffer();
        Vector childNodes = new Vector();
        for (Iterator lineIterator = sitemapLines.iterator(); lineIterator.hasNext(); ) {
            SitemapLineBean line = (SitemapLineBean) lineIterator.next();
            if (childNodes.size() > 0) {
                String myGroup = line.getGroup();
                String lastNode = (String) childNodes.get(childNodes.size() - 1);
                while (myGroup.length() <= (lastNode.length() + 1) && childNodes.size() > 0) {
                    ieView.append("</div><!-- " + lastNode + "-->");
                    childNodes.remove(childNodes.size() - 1);
                    if (childNodes.size() > 0) {
                        lastNode = (String) childNodes.get(childNodes.size() - 1);
                    }
                }
            }
            ieView.append("<div id=\"node_" + line.getGroup() + "_Parent\" class=\"parent\">");
            for (Iterator levelsIterator = line.getLevels().iterator(); levelsIterator.hasNext(); ) {
                String level = (String) levelsIterator.next();
                if (line.isHasChildren()) {
                    if ("x".equalsIgnoreCase(level)) {
                        ieView.append("<a href=\"#\" onClick=\"expandIt('node_" + line.getGroup() + "_'); return false;\"><img src=\"images/x-.png\" widht=\"30\" height=\"30\" align=\"top\"  name=\"imEx\" border=\"0\"></a>");
                    } else if ("e".equalsIgnoreCase(level)) {
                        ieView.append("<a href=\"#\" onClick=\"expandItE('node_" + line.getGroup() + "_'); return false;\"><img src=\"images/e-.png\" widht=\"30\" height=\"30\" align=\"top\"  name=\"imEx\" border=\"0\"></a>");
                    } else {
                        ieView.append("<img src=\"images/" + level + ".png\" widht=\"30\" height=\"30\" align=\"top\">");
                    }
                } else {
                    ieView.append("<img src=\"images/" + level + ".png\" widht=\"30\" height=\"30\" align=\"top\">");
                }
            }
            ieView.append("<a href=\"" + safename(line.getTopic()) + ".html\">" + line.getTopic() + "</a></div>\n");
            if (line.isHasChildren()) {
                ieView.append("<div id=\"node_" + line.getGroup() + "_Child\" class=\"child\">");
                childNodes.add(line.getGroup());
            }
        }
        for (int i = childNodes.size() - 1; i >= 0; i--) {
            ieView.append("</div><!-- " + (String) childNodes.get(i) + "-->");
        }
        nsView.append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n");
        for (Iterator lineIterator = sitemapLines.iterator(); lineIterator.hasNext(); ) {
            SitemapLineBean line = (SitemapLineBean) lineIterator.next();
            nsView.append("<tr><td height=\"30\" valign=\"top\">");
            for (Iterator levelsIterator = line.getLevels().iterator(); levelsIterator.hasNext(); ) {
                String level = (String) levelsIterator.next();
                nsView.append("<img src=\"images/" + level + ".png\" widht=\"30\" height=\"30\" align=\"top\">");
            }
            nsView.append("<a href=\"" + safename(line.getTopic()) + ".html\">" + line.getTopic() + "</a></td></tr>\n");
        }
        nsView.append("</table>\n");
        tpl = getTemplateFilledWithContent("sitemap");
        tpl.setFieldGlobal("TOPICNAME", "WikiSiteMap");
        ZipEntry entry = new ZipEntry("WikiSiteMap.html");
        StringReader strin = new StringReader(Utilities.replaceString(tpl.getContent(), "@@NSVIEW@@", nsView.toString()));
        zipout.putNextEntry(entry);
        int read;
        while ((read = strin.read()) != -1) {
            zipout.write(read);
        }
        zipout.closeEntry();
        zipout.flush();
        tpl = getTemplateFilledWithContent("sitemap_ie");
        tpl.setFieldGlobal("TOPICNAME", "WikiSiteMap");
        entry = new ZipEntry("WikiSiteMapIE.html");
        strin = new StringReader(Utilities.replaceString(tpl.getContent(), "@@IEVIEW@@", ieView.toString()));
        zipout.putNextEntry(entry);
        while ((read = strin.read()) != -1) {
            zipout.write(read);
        }
        zipout.closeEntry();
        zipout.flush();
        tpl = getTemplateFilledWithContent("sitemap_ns");
        tpl.setFieldGlobal("TOPICNAME", "WikiSiteMap");
        entry = new ZipEntry("WikiSiteMapNS.html");
        strin = new StringReader(Utilities.replaceString(tpl.getContent(), "@@NSVIEW@@", nsView.toString()));
        zipout.putNextEntry(entry);
        while ((read = strin.read()) != -1) {
            zipout.write(read);
        }
        zipout.closeEntry();
        zipout.flush();
    }

    /**
    * Add a single topic to the Zip stream
    *
    * @param en The current environment
    * @param zipout The Zip to add the topic to
    * @param containingTopics List of all containing topic
    * @param sedb The search engine
    * @param defaultTopic The default topics
    * @param ignoreTheseTopicsList Ignore these topics
    * @param topicname The name of this topic
    * @throws Exception
    * @throws IOException
    */
    private void addTopicToZip(Environment en, ZipOutputStream zipout, HashMap containingTopics, SearchEngine sedb, String defaultTopic, List ignoreTheseTopicsList, String topicname) throws Exception, IOException {
        WikiBase wb = WikiBase.getInstance();
        Template tpl;
        tpl = new Template(ctx.getRealPath("/WEB-INF/classes/export2html/mastertemplate.tpl"));
        tpl.setFieldGlobal("VERSION", Environment.WIKI_VERSION);
        StringBuffer oneline = new StringBuffer();
        if (!ignoreTheseTopicsList.contains(topicname)) {
            oneline.append(topicname);
            tpl.setFieldGlobal("TOPICNAME", topicname);
            Topic topicObject = new Topic(topicname);
            logger.debug("Adding topic " + topicname);
            String author = null;
            java.util.Date lastRevisionDate = null;
            if (Environment.getInstance().isVersioningOn()) {
                lastRevisionDate = topicObject.getMostRecentRevisionDate(virtualWiki);
                author = topicObject.getMostRecentAuthor(virtualWiki);
                if (author != null || lastRevisionDate != null) {
                    tpl.setField("SHOWVERSIONING1", "-->");
                    if (author != null) tpl.setField("AUTHOR", author);
                    if (lastRevisionDate != null) tpl.setField("MODIFYDATE", Utilities.formatDate(lastRevisionDate));
                    tpl.setField("SHOWVERSIONING2", "<!--");
                }
            }
            StringBuffer content = new StringBuffer();
            content.append(wb.readCooked(virtualWiki, topicname, en.getStringSetting(Environment.PROPERTY_FORMAT_LEXER), en.getStringSetting(Environment.PROPERTY_LAYOUT_LEXER), "vqwiki.lex.LinkLexExportHTMLWrapper", true));
            String redirect = "redirect:";
            if (content.toString().startsWith(redirect)) {
                StringBuffer link = new StringBuffer(content.toString().substring(redirect.length()).trim());
                while (link.toString().indexOf("<") != -1) {
                    int startpos = link.toString().indexOf("<");
                    int endpos = link.toString().indexOf(">");
                    if (endpos == -1) {
                        endpos = link.length();
                    } else {
                        endpos++;
                    }
                    link.delete(startpos, endpos);
                }
                link = new StringBuffer(safename(link.toString().trim()));
                link = link.append(".html");
                String nl = System.getProperty("line.separator");
                tpl.setFieldGlobal("REDIRECT", "<script>" + nl + "location.replace(\"" + link.toString() + "\");" + nl + "</script>" + nl + "<meta http-equiv=\"refresh\" content=\"1; " + link.toString() + "\">" + nl);
            } else {
                tpl.setFieldGlobal("REDIRECT", "");
            }
            Collection searchresult = sedb.find(virtualWiki, topicname, false);
            if (searchresult != null && searchresult.size() > 0) {
                Iterator it = searchresult.iterator();
                String divider = "";
                StringBuffer backlinks = new StringBuffer();
                for (; it.hasNext(); ) {
                    SearchResultEntry result = (SearchResultEntry) it.next();
                    if (!result.getTopic().equals(topicname)) {
                        backlinks.append(divider);
                        backlinks.append("<a href=\"");
                        backlinks.append(safename(result.getTopic()));
                        backlinks.append(".html\">");
                        backlinks.append(result.getTopic());
                        backlinks.append("</a>");
                        divider = " | ";
                        List l = (List) containingTopics.get(result.getTopic());
                        if (l == null) {
                            l = new ArrayList();
                        }
                        if (!l.contains(topicname)) {
                            l.add(topicname);
                        }
                        containingTopics.put(result.getTopic(), l);
                    }
                }
                if (backlinks.length() > 0) {
                    ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
                    content.append("<br /><br /><span class=\"backlinks\">");
                    content.append(topicname);
                    content.append(" ");
                    content.append(messages.getString("topic.ismentionedon"));
                    content.append(" ");
                    content.append(backlinks.toString());
                    content.append("</span>");
                }
            }
            tpl.setFieldGlobal("CONTENTS", content.toString());
            ZipEntry entry = new ZipEntry(safename(topicname) + ".html");
            StringReader in = new StringReader(tpl.getContent());
            zipout.putNextEntry(entry);
            int read = 0;
            while ((read = in.read()) != -1) {
                zipout.write(read);
            }
            zipout.closeEntry();
            zipout.flush();
            if (topicname.equals(defaultTopic)) {
                entry = new ZipEntry("index.html");
                in = new StringReader(tpl.getContent());
                zipout.putNextEntry(entry);
                read = 0;
                while ((read = in.read()) != -1) {
                    zipout.write(read);
                }
                zipout.closeEntry();
                zipout.flush();
            }
        }
    }

    /**
    * Create a safe name of this topic for the file system.
    *
    * @param topic
    *            The original topic name
    * @return The safe topic name
    */
    private String safename(String topic) {
        return Utilities.encodeSafeExportFileName(topic);
    }

    /**
    * Parse the pages starting with startTopic. The results are stored in the
    * list sitemapLines. This functions is called recursivly, but the list is
    * filled in the correct order.
    *
    * @param currentWiki
    *            name of the wiki to refer to
    * @param startTopic
    *            Start with this page
    * @param level
    *            A list indicating the images to use to represent certain
    *            levels
    * @param group
    *            The group, we are representing
    * @param sitemapLines
    *            A list of all lines, which results in the sitemap
    * @param visitedPages
    *            A vector of all pages, which already have been visited
    * @param endString
    *            Beyond this text we do not search for links
    */
    private void parsePages(String topic, HashMap wiki, List levelsIn, String group, List sitemapLines, Vector visitedPages) {
        try {
            List result = new ArrayList();
            List levels = new ArrayList(levelsIn.size());
            for (int i = 0; i < levelsIn.size(); i++) {
                if ((i + 1) < levelsIn.size()) {
                    if (SitemapThread.MORE_TO_COME.equals((String) levelsIn.get(i))) {
                        levels.add(SitemapThread.HORIZ_LINE);
                    } else if (SitemapThread.LAST_IN_LIST.equals((String) levelsIn.get(i))) {
                        levels.add(SitemapThread.NOTHING);
                    } else {
                        levels.add(levelsIn.get(i));
                    }
                } else {
                    levels.add(levelsIn.get(i));
                }
            }
            List l = (List) wiki.get(topic);
            if (l == null) {
                l = new ArrayList();
            }
            for (Iterator listIterator = l.iterator(); listIterator.hasNext(); ) {
                String link = (String) listIterator.next();
                if (link.indexOf('&') > -1) {
                    link = link.substring(0, link.indexOf('&'));
                }
                if (link.length() > 3 && !link.startsWith("topic=") && !link.startsWith("action=") && !visitedPages.contains(link) && !PseudoTopicHandler.getInstance().isPseudoTopic(link)) {
                    result.add(link);
                    visitedPages.add(link);
                }
            }
            SitemapLineBean slb = new SitemapLineBean();
            slb.setTopic(topic);
            slb.setLevels(new ArrayList(levels));
            slb.setGroup(group);
            slb.setHasChildren(result.size() > 0);
            sitemapLines.add(slb);
            for (int i = 0; i < result.size(); i++) {
                String link = (String) result.get(i);
                String newGroup = group + "_" + String.valueOf(i);
                boolean isLast = ((i + 1) == result.size());
                if (isLast) {
                    levels.add(SitemapThread.LAST_IN_LIST);
                } else {
                    levels.add(SitemapThread.MORE_TO_COME);
                }
                parsePages(link, wiki, levels, newGroup, sitemapLines, visitedPages);
                levels.remove(levels.size() - 1);
            }
        } catch (Exception e) {
            logger.fatal("Exception", e);
        }
    }

    /**
    *
    */
    private void addAllSpecialPages(Environment en, ZipOutputStream zipout, int progressStart, int progressLength) throws Exception, IOException {
        if (virtualWiki == null || virtualWiki.length() < 1) {
            virtualWiki = WikiBase.DEFAULT_VWIKI;
        }
        ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
        Template tpl;
        int count = 0;
        int numberOfSpecialPages = 7;
        int bytesRead = 0;
        byte[] byteArray = new byte[4096];
        progress = Math.min(progressStart + (int) ((double) count * (double) progressLength / (double) numberOfSpecialPages), 99);
        count++;
        ZipEntry entry = new ZipEntry("vqwiki.css");
        zipout.putNextEntry(entry);
        StringReader readin = new StringReader(WikiBase.getInstance().readRaw(virtualWiki, "StyleSheet"));
        int read = 0;
        while ((read = readin.read()) != -1) {
            zipout.write(read);
        }
        zipout.closeEntry();
        zipout.flush();
        progress = Math.min(progressStart + (int) ((double) count * (double) progressLength / (double) numberOfSpecialPages), 99);
        count++;
        tpl = getTemplateFilledWithContent("search");
        tpl.setFieldGlobal("TOPICNAME", "WikiSearch");
        entry = new ZipEntry("WikiSearch.html");
        StringReader strin = new StringReader(tpl.getContent());
        zipout.putNextEntry(entry);
        while ((bytesRead = strin.read()) != -1) {
            zipout.write(bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        progress = Math.min(progressStart + (int) ((double) count * (double) progressLength / (double) numberOfSpecialPages), 99);
        count++;
        entry = new ZipEntry("applets/vqapplets.jar");
        zipout.putNextEntry(entry);
        InputStream in = new BufferedInputStream(new FileInputStream(ctx.getRealPath("/WEB-INF/classes/export2html/vqapplets.jar")));
        while (in.available() > 0) {
            bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
            zipout.write(byteArray, 0, bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        entry = new ZipEntry("applets/log4j.jar");
        zipout.putNextEntry(entry);
        in = new BufferedInputStream(new FileInputStream(ctx.getRealPath("/WEB-INF/lib/log4j-1.2.12.jar")));
        while (in.available() > 0) {
            bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
            zipout.write(byteArray, 0, bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        entry = new ZipEntry("applets/lucene-1.2a.jar");
        zipout.putNextEntry(entry);
        in = new BufferedInputStream(new FileInputStream(ctx.getRealPath("/WEB-INF/lib/lucene-1.2a.jar")));
        while (in.available() > 0) {
            bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
            zipout.write(byteArray, 0, bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        entry = new ZipEntry("applets/commons-httpclient-2.0.jar");
        zipout.putNextEntry(entry);
        in = new BufferedInputStream(new FileInputStream(ctx.getRealPath("/WEB-INF/lib/commons-httpclient-2.0.jar")));
        while (in.available() > 0) {
            bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
            zipout.write(byteArray, 0, bytesRead);
        }
        zipout.closeEntry();
        zipout.flush();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            JarOutputStream indexjar = new JarOutputStream(bos);
            JarEntry jarEntry;
            File searchDir = new File(WikiBase.getInstance().getSearchEngineInstance().getSearchIndexPath(virtualWiki));
            String files[] = searchDir.list();
            StringBuffer listOfAllFiles = new StringBuffer();
            for (int i = 0; i < files.length; i++) {
                if (listOfAllFiles.length() > 0) {
                    listOfAllFiles.append(",");
                }
                listOfAllFiles.append(files[i]);
                jarEntry = new JarEntry("lucene/index/" + files[i]);
                indexjar.putNextEntry(jarEntry);
                in = new FileInputStream(new File(searchDir, files[i]));
                while (in.available() > 0) {
                    bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
                    indexjar.write(byteArray, 0, bytesRead);
                }
                indexjar.closeEntry();
            }
            indexjar.flush();
            jarEntry = new JarEntry("lucene/index.dir");
            strin = new StringReader(listOfAllFiles.toString());
            indexjar.putNextEntry(jarEntry);
            while ((bytesRead = strin.read()) != -1) {
                indexjar.write(bytesRead);
            }
            indexjar.closeEntry();
            indexjar.flush();
            indexjar.close();
            entry = new ZipEntry("applets/index.jar");
            zipout.putNextEntry(entry);
            zipout.write(bos.toByteArray());
            zipout.closeEntry();
            zipout.flush();
            bos.reset();
        } catch (Exception e) {
            logger.debug("Exception while adding lucene index: ", e);
        }
        progress = Math.min(progressStart + (int) ((double) count * (double) progressLength / (double) numberOfSpecialPages), 99);
        count++;
        tpl = new Template(ctx.getRealPath("/WEB-INF/classes/export2html/mastertemplate.tpl"));
        tpl.setFieldGlobal("VERSION", Environment.WIKI_VERSION);
        StringBuffer content = new StringBuffer();
        content.append("<table><tr><th>" + messages.getString("common.date") + "</th><th>" + messages.getString("common.topic") + "</th><th>" + messages.getString("common.user") + "</th></tr>\n");
        Collection all = null;
        try {
            Calendar cal = Calendar.getInstance();
            ChangeLog cl = WikiBase.getInstance().getChangeLogInstance();
            int n = Environment.getInstance().getIntSetting(Environment.PROPERTY_RECENT_CHANGES_DAYS);
            if (n == 0) {
                n = 5;
            }
            all = new ArrayList();
            for (int i = 0; i < n; i++) {
                Collection col = cl.getChanges(virtualWiki, cal.getTime());
                if (col != null) {
                    all.addAll(col);
                }
                cal.add(Calendar.DATE, -1);
            }
        } catch (Exception e) {
            ;
        }
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        for (Iterator iter = all.iterator(); iter.hasNext(); ) {
            Change change = (Change) iter.next();
            content.append("<tr><td class=\"recent\">" + df.format(change.getTime()) + "</td><td class=\"recent\"><a href=\"" + safename(change.getTopic()) + ".html\">" + change.getTopic() + "</a></td><td class=\"recent\">" + change.getUser() + "</td></tr>");
        }
        content.append("</table>\n");
        tpl.setFieldGlobal("TOPICNAME", "RecentChanges");
        tpl.setFieldGlobal("VERSION", Environment.WIKI_VERSION);
        tpl.setFieldGlobal("CONTENTS", content.toString());
        entry = new ZipEntry("RecentChanges.html");
        strin = new StringReader(tpl.getContent());
        zipout.putNextEntry(entry);
        while ((read = strin.read()) != -1) {
            zipout.write(read);
        }
        zipout.closeEntry();
        zipout.flush();
        logger.debug("Done adding all special topics.");
    }

    /**
    *
    */
    private void addAllImages(Environment en, ZipOutputStream zipout, int progressStart, int progressLength) throws IOException {
        String[] files = new File(imageDir).list();
        int bytesRead = 0;
        byte byteArray[] = new byte[4096];
        FileInputStream in = null;
        for (int i = 0; i < files.length; i++) {
            progress = Math.min(progressStart + (int) ((double) i * (double) progressLength / (double) files.length), 99);
            File fileToHandle = new File(imageDir, files[i]);
            if (fileToHandle.isFile() && fileToHandle.canRead()) {
                try {
                    logger.debug("Adding image file " + files[i]);
                    ZipEntry entry = new ZipEntry("images/" + files[i]);
                    zipout.putNextEntry(entry);
                    in = new FileInputStream(fileToHandle);
                    while (in.available() > 0) {
                        bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
                        zipout.write(byteArray, 0, bytesRead);
                    }
                } catch (FileNotFoundException e) {
                    ;
                } catch (IOException e) {
                    ;
                } finally {
                    try {
                        zipout.closeEntry();
                    } catch (IOException e1) {
                        ;
                    }
                    try {
                        zipout.flush();
                    } catch (IOException e1) {
                        ;
                    }
                    try {
                        if (in != null) {
                            in.close();
                            in = null;
                        }
                    } catch (IOException e1) {
                        ;
                    }
                }
            }
        }
    }

    /**
    *
    */
    private void addAllUploadedFiles(Environment en, ZipOutputStream zipout, int progressStart, int progressLength) throws IOException, FileNotFoundException {
        File uploadPath = en.uploadPath(virtualWiki, "");
        String[] files = uploadPath.list();
        int bytesRead = 0;
        byte byteArray[] = new byte[4096];
        for (int i = 0; i < files.length; i++) {
            progress = Math.min(progressStart + (int) ((double) i * (double) progressLength / (double) files.length), 99);
            logger.debug("Adding uploaded file " + files[i]);
            ZipEntry entry = new ZipEntry(safename(files[i]));
            try {
                FileInputStream in = new FileInputStream(en.uploadPath(virtualWiki, files[i]));
                zipout.putNextEntry(entry);
                while (in.available() > 0) {
                    bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
                    zipout.write(byteArray, 0, bytesRead);
                }
                zipout.closeEntry();
                zipout.flush();
            } catch (FileNotFoundException e) {
                logger.warn("Could not open file!", e);
            } catch (IOException e) {
                logger.warn("IOException!", e);
                try {
                    zipout.closeEntry();
                    zipout.flush();
                } catch (IOException e1) {
                    ;
                }
            }
        }
    }

    /**
    *
    */
    private Template getTemplateFilledWithContent(String contentName) throws Exception {
        Template tpl = new Template(ctx.getRealPath("/WEB-INF/classes/export2html/mastertemplate.tpl"));
        tpl.setFieldGlobal("VERSION", Environment.WIKI_VERSION);
        StringBuffer content = readFile("/WEB-INF/classes/export2html/" + contentName + ".content");
        tpl.setFieldGlobal("CONTENTS", content.toString());
        return tpl;
    }

    /**
    * Read a file from a resource inside the classpath
    *
    * @param filename
    *            The file to read
    * @return The content of the file as StringBuffer
    */
    private StringBuffer readFile(String filename) {
        return Utilities.readFile(new File(ctx.getRealPath(filename)));
    }

    /**
    * We are done. Go to result page.
    *
    * @see vqwiki.servlets.LongLastingOperationServlet#dispatchDone(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
    protected void dispatchDone(HttpServletRequest request, HttpServletResponse response, LongLastingOperationServlet servlet) {
        if (exception != null) {
            servlet.error(request, response, new ServletException(exception.getMessage(), exception));
            return;
        }
        try {
            response.setContentType("application/zip");
            response.setHeader("Expires", "0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Keep-Alive", "timeout=15, max=100");
            response.setHeader("Connection", "Keep-Alive");
            response.setHeader("Content-Disposition", "attachment" + ";filename=HTMLExportOf" + virtualWiki + ".zip;");
            FileInputStream in = new FileInputStream(tempFile);
            response.setContentLength((int) tempFile.length());
            OutputStream out = response.getOutputStream();
            int bytesRead = 0;
            byte byteArray[] = new byte[4096];
            while (in.available() > 0) {
                bytesRead = in.read(byteArray, 0, Math.min(4096, in.available()));
                out.write(byteArray, 0, bytesRead);
            }
            out.flush();
            out.close();
            tempFile.delete();
        } catch (Exception e) {
            logger.fatal("Exception", e);
            servlet.error(request, response, new ServletException(e.getMessage(), e));
        }
    }
}

package org.jzonic.yawiki.export;

import gnu.regexp.RE;
import gnu.regexp.REMatch;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.velocity.VelocityContext;
import org.jconfig.Configuration;
import org.jconfig.ConfigurationManager;
import org.jzonic.core.Command;
import org.jzonic.core.CommandException;
import org.jzonic.core.WebContext;
import org.jzonic.jlo.LogManager;
import org.jzonic.jlo.Logger;
import org.jzonic.yawiki.commands.FormatHelper;
import org.jzonic.yawiki.menu.MenuDBManager;
import org.jzonic.yawiki.renderer.DefaultWikiRenderContext;
import org.jzonic.yawiki.renderer.WikiRenderer;
import org.jzonic.yawiki.renderer.WikiRendererFactory;
import org.jzonic.yawiki.repository.Domain;
import org.jzonic.yawiki.repository.PageInfo;
import org.jzonic.yawiki.repository.WikiEngine;
import org.jzonic.yawiki.util.VelocityTransformer;

/**
 * This is the base yawiki command. All commands should extend this command.
 * This command sets the theme defined in the configuration as yawiki_theme
 * in the yawiki category.
 *
 * @author  Andreas Mecky andreasmecky@yahoo.de
 */
public class ExportCommand implements Command {

    private static final Configuration cm = ConfigurationManager.getConfiguration("jZonic");

    private static final Logger logger = LogManager.getLogger("org.jzonic.yawiki.export");

    public ExportCommand() {
    }

    public String execute(WebContext webContext) throws CommandException {
        try {
            String template = webContext.getRequestParameter("template");
            Domain domain = (Domain) webContext.getSessionParameter("DOMAIN");
            String dir = webContext.getRequestParameter("directory");
            if (dir == null || dir.length() == 0) {
                dir = System.getProperty("java.io.tmpdir");
            }
            if (!dir.endsWith(File.separator)) {
                dir += File.separator;
            }
            String css = webContext.getRequestParameter("cssfile", null);
            String pageName = domain.getStartPage();
            WikiEngine we = new WikiEngine();
            MenuDBManager manager = new MenuDBManager(domain);
            List mTopics = manager.getMenuTopics();
            Vector all = we.getAllEntries(domain);
            logger.info("starting to export domain:" + domain.getName() + " using template:" + template);
            for (int i = 0; i < all.size(); i++) {
                PageInfo info = (PageInfo) all.get(i);
                if (info != null) {
                    DefaultWikiRenderContext dctx = new DefaultWikiRenderContext(domain);
                    WikiRenderer renderer = WikiRendererFactory.getRenderer();
                    String content = renderer.render(pageName, dctx);
                    if (pageName.equals(info.getPageName())) {
                        saveFile("index", content, info, mTopics, dir, template);
                        saveFile(info.getPageName(), content, info, mTopics, dir, template);
                    } else {
                        saveFile(info.getPageName(), content, info, mTopics, dir, template);
                    }
                }
            }
            if (css != null) {
                File newCssFile = new File(dir + css);
                File cssFile = new File(cm.getProperty("base_path") + "export" + File.separator + css);
                copy(cssFile, newCssFile);
            }
            return null;
        } catch (Exception e) {
            logger.fatal("execute", e);
            throw new CommandException("Error while exporting the domain:" + e.getMessage());
        }
    }

    private void copy(File src, File dst) throws IOException {
        logger.info("copy src:" + src.getAbsolutePath() + " dest:" + dst.getAbsolutePath());
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private String[] listFiles(String dirName) {
        File dir = new File(dirName);
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".html");
            }
        };
        return dir.list(filter);
    }

    private void buildZipFile(String dirName) {
        String[] filenames = listFiles(dirName);
        byte[] buf = new byte[1024];
        try {
            String outFilename = dirName + "/outfile.zip";
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
            for (int i = 0; i < filenames.length; i++) {
                File file = new File(dirName + "/" + filenames[i]);
                if (file.exists()) {
                    FileInputStream in = new FileInputStream(dirName + "/" + filenames[i]);
                    out.putNextEntry(new ZipEntry(filenames[i]));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(String pageName, String content, PageInfo info, List menuTopics, String dir, String template) {
        try {
            String fname = dir + "/" + pageName + ".html";
            logger.info("saving file:" + fname);
            FileWriter fw = new FileWriter(fname);
            VelocityContext ctx = new VelocityContext();
            ctx.put("content", content);
            ctx.put("info", info);
            ctx.put("FormatHelper", new FormatHelper());
            ctx.put("menutopics", menuTopics);
            String tmp = VelocityTransformer.transform("export/" + template, ctx);
            tmp = replaceUrls(tmp);
            fw.write(tmp);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String replaceUrls(String line) {
        String uri = cm.getProperty("redirect_prefix");
        String expr = "wiki.jz\\?page=([A-Za-z0-9\\s:&=\\-/.]*)";
        int offset = 0;
        try {
            RE re = new RE(expr);
            REMatch[] matches = re.getAllMatches(line);
            for (int i = 0; i < matches.length; i++) {
                String tmp = matches[i].toString();
                String pName = tmp.substring(tmp.indexOf("=") + 1);
                int start = matches[i].getStartIndex() + offset;
                int end = matches[i].getEndIndex() + offset;
                String firstPart = line.substring(0, start);
                String secondPart = line.substring(end);
                String tag = pName + ".html";
                line = firstPart + tag + secondPart;
                offset += tag.length() - matches[i].toString().length();
            }
            return line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }
}

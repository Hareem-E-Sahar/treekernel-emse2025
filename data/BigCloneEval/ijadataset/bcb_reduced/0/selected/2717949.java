package org.one.stone.soup.wiki.migration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.one.stone.soup.entity.KeyValuePair;
import org.one.stone.soup.file.FileHelper;
import org.one.stone.soup.stringhelper.FileNameHelper;
import org.one.stone.soup.stringhelper.StringArrayHelper;
import org.one.stone.soup.wiki.WikiNameHelper;
import org.one.stone.soup.wiki.controller.WikiControllerInterface;

public class WikiMigrator {

    private String wikiRoot;

    private String webRoot;

    private String newRoot;

    private String matchName;

    public static void main(String args[]) {
        if (args.length == 3) {
            new WikiMigrator().migrate(args[0], args[1], args[2], null);
        } else if (args.length == 4) {
            new WikiMigrator().migrate(args[0], args[1], args[2], args[3]);
        } else {
            System.out.println("Usage: WikiMigrator wikiRoot webRoot newRoot [matchPages]");
        }
    }

    public void migrate(String wikiRoot, String webRoot, String newRoot, String matchName) {
        this.wikiRoot = wikiRoot;
        this.webRoot = webRoot;
        this.newRoot = newRoot;
        this.matchName = matchName;
        System.out.println("Migrating " + wikiRoot + " with " + webRoot + " to " + newRoot);
        File webRootDir = new File(webRoot);
        File[] pages = webRootDir.listFiles();
        for (int loop = 0; loop < pages.length; loop++) {
            if (pages[loop].isDirectory()) {
                continue;
            }
            String ext = FileNameHelper.getExt(pages[loop].getName());
            if (ext.equals("html")) {
                String pageName = pages[loop].getName();
                pageName = pageName.substring(0, pageName.length() - 5);
                if (pageName.indexOf("-printable") == -1) {
                    migratePage(pageName);
                }
            }
        }
        System.out.println("Migration Complete");
    }

    private void migratePage(String pageName) {
        if (matchName != null) {
            if (pageName.matches(matchName) == false) {
                return;
            }
        }
        String newPageName = pageName;
        if (pageName.indexOf("_blogentry_") != -1) {
            newPageName = pageName.substring(0, pageName.indexOf("_blogentry_")) + "/blog/";
            try {
                String dateString = pageName.substring(pageName.indexOf("_blogentry_") + 11);
                Date entryDate = null;
                if (dateString.length() == 8) {
                    entryDate = new SimpleDateFormat("ddMMyy_h").parse(dateString);
                } else {
                    entryDate = new SimpleDateFormat("ddMMyyyy_h").parse(dateString);
                }
                String entryName = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(entryDate);
                newPageName = newPageName + entryName + "/";
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        System.out.println("Migrating " + pageName + " to " + WikiNameHelper.titleToWikiName(newPageName));
        File htmlFile = new File(webRoot + "/" + pageName + ".html");
        File newHtmlFile = new File(newRoot + "/" + WikiNameHelper.titleToWikiName(newPageName) + "/page.html");
        File wikiFile = new File(wikiRoot + "/" + pageName + ".txt");
        File newWikiFile = new File(newRoot + "/" + WikiNameHelper.titleToWikiName(newPageName) + "/page.wiki");
        File wikiPropertyFile = new File(wikiRoot + "/" + pageName + ".properties");
        File newWikiPropertyFile = new File(newRoot + "/" + WikiNameHelper.titleToWikiName(newPageName) + "/" + WikiControllerInterface.OWNER_FILE);
        if (htmlFile.exists()) {
            FileHelper.copy(htmlFile, newHtmlFile);
            newHtmlFile.setLastModified(htmlFile.lastModified());
        }
        if (wikiFile.exists()) {
            FileHelper.copy(wikiFile, newWikiFile);
            newWikiFile.setLastModified(wikiFile.lastModified());
        }
        if (wikiPropertyFile.exists()) {
            try {
                String data = FileHelper.readFile(wikiPropertyFile.getAbsolutePath());
                String[] lines = StringArrayHelper.parseFields(data, '\n');
                String author = KeyValuePair.parseKeyValuePair(lines[2], "=").getValue();
                FileHelper.buildFile(newWikiPropertyFile.getAbsolutePath(), author);
                newWikiPropertyFile.setLastModified(wikiPropertyFile.lastModified());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        migratePageAttachments(pageName, newPageName);
    }

    private void migratePageAttachments(String pageName, String newPageName) {
        File wikiAttachementsFolder = new File(wikiRoot + "/" + pageName + "-att");
        if (wikiAttachementsFolder.exists()) {
            File[] wikiAttachments = wikiAttachementsFolder.listFiles();
            for (int loop = 0; loop < wikiAttachments.length; loop++) {
                try {
                    String fileName = wikiAttachments[loop].getName();
                    fileName = fileName.substring(0, fileName.length() - 4);
                    String extension = FileNameHelper.getExt(fileName);
                    String historyFile = wikiAttachments[loop].getAbsolutePath() + "/attachment.properties";
                    String[] history = StringArrayHelper.parseFields(FileHelper.readFile(historyFile), '\n');
                    String lastEntry = history[history.length - 1];
                    lastEntry = KeyValuePair.parseKeyValuePair(lastEntry, ".").key;
                    String sourceFileName = lastEntry + "." + extension;
                    File sourceFile = new File(wikiAttachments[loop].getAbsolutePath() + "/" + sourceFileName);
                    File targetFile = new File(newRoot + "/" + WikiNameHelper.titleToWikiName(newPageName) + "/" + fileName);
                    FileHelper.copy(sourceFile, targetFile);
                    targetFile.setLastModified(sourceFile.lastModified());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

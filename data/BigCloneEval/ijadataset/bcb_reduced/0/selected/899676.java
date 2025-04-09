package m2s.utils;

import java.io.*;
import java.util.Vector;
import m2s.Moodle.*;
import m2s.Scorm.*;

/**
 *
 * @author v3r5_u5
 */
public class Scorm {

    ImsManifest imsManifest;

    Lom lom;

    private String path;

    private String courseName;

    private Moodle moodle;

    public void makeDirs() {
        new File(path + courseName + "/images").mkdirs();
        new File(path + courseName + "/css").mkdirs();
        new File(path + "/images").mkdirs();
    }

    public void makeChapters() {
        Vector chapters = moodle.getSections();
        for (int i = 0; i < chapters.size(); i++) {
            new File(path + courseName + "/Chapter_" + i).mkdirs();
            Section sect = (Section) chapters.get(i);
            Vector mods = sect.getMods();
            Lom chapterLom = new Lom(path + courseName + "/Chapter_" + i + ".xml");
            chapterLom.createEmptyDoc();
            try {
                chapterLom.saveXML();
            } catch (IOException ex) {
                System.out.println("!!!" + ex.getLocalizedMessage());
            }
            imsManifest.mainAsset.getFiles().add(courseName + "/Chapter_" + i + ".xml");
            OrgItem chOrgItem = new OrgItem();
            chOrgItem.setIdentifier("S_" + i);
            chOrgItem.setTitle(sect.getSummary());
            for (int j = 0; j < mods.size(); j++) {
                Integer modId = (Integer) mods.get(j);
                Mod mod = moodle.getMod(modId);
                makeHtml(path + courseName + "/Chapter_" + i + "/sco_" + j + ".html", mod);
                makeLom(path + courseName + "/Chapter_" + i + "/sco_" + j + ".xml", mod);
                imsManifest.addRes(courseName + "/Chapter_" + i + "/sco_", i, j);
                OrgItem modOrgItem = new OrgItem();
                modOrgItem.setIdentifier("S_" + i + "_" + j);
                modOrgItem.setTitle(mod.getTitle());
                modOrgItem.setIdentifierref("RS_" + i + "_" + j);
                chOrgItem.addItem(modOrgItem);
            }
            imsManifest.orgItem.addItem(chOrgItem);
        }
    }

    public void makeHtml(String path, Mod mod) {
        String head = "<html><head><title>";
        String body = "</title></head><body>";
        String tail = "</body></html>";
        String data = head + mod.getTitle() + body + mod.getData() + tail;
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(data.getBytes());
            fos.close();
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    public void makeLom(String path, Mod mod) {
        Lom modLom = new Lom(path);
        modLom.createEmptyDoc();
        try {
            modLom.saveXML();
        } catch (IOException ex) {
            System.out.println("!!!" + ex.getLocalizedMessage());
        }
    }

    public void copyRes(String from, String to) {
        File dir = new File(from);
        File list[];
        list = dir.listFiles();
        for (int i = 0; i < list.length; i++) {
            if (!list[i].isDirectory()) {
                try {
                    copyFile(from + list[i].getName(), to + list[i].getName());
                } catch (Exception ex) {
                    System.out.println("Can't copy file: " + from + list[i].getName());
                    System.out.println(ex.getLocalizedMessage());
                }
            } else {
                new File(to + list[i].getName()).mkdirs();
                copyRes(from + list[i].getName() + "/", to + list[i].getName() + "/");
            }
        }
    }

    public void copyFile(String from, String to) throws Exception {
        File inputFile = new File(from);
        File outputFile = new File(to);
        FileInputStream in = new FileInputStream(inputFile);
        FileOutputStream out = new FileOutputStream(outputFile);
        int c;
        while ((c = in.read()) != -1) {
            out.write(c);
        }
        in.close();
        out.close();
    }

    public Scorm(String path, String courseName) {
        imsManifest = new ImsManifest(path);
        lom = new Lom(path + "/" + courseName + ".xml");
        this.courseName = courseName;
        imsManifest.createEmptyDoc();
        lom.createEmptyDoc();
        this.path = path;
    }

    public void convertFromMoodle(Moodle moodle) {
        this.moodle = moodle;
        makeDirs();
        copyRes(moodle.getPath() + "course_files/", getPath() + courseName + "/");
        copyRes("template/", getPath());
        imsManifest.createAsset(path);
        makeChapters();
    }

    public void save() {
        try {
            imsManifest.save();
            lom.saveXML();
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    public String getPath() {
        return path;
    }
}

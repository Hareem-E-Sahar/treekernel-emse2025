package net.sourceforge.jhelpdev.action;

import java.awt.Cursor;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import net.sourceforge.jhelpdev.ConfigHolder;
import net.sourceforge.jhelpdev.JHelpDevFrame;

/**
 * Action to export everything and creating the search database
 * as well. It invokes actions from other action objects.
 * @author <a href="mailto:mk@mk-home.de">Markus Kraetzig</a>
 */
public final class CreateAllAction extends AbstractAction {

    private static String tmpDir = null;

    private static final com.sun.java.help.search.Indexer indexer = new com.sun.java.help.search.Indexer();

    /**
	 * CreateMapAction constructor comment.
	 */
    public CreateAllAction() {
        super();
        putValue(NAME, "Create All");
        URL url = getClass().getResource("/images/create_all.gif");
        if (url != null) putValue(SMALL_ICON, new javax.swing.ImageIcon(url));
    }

    /**
	 * CreateMapAction constructor comment.
	 * @param name java.lang.String
	 */
    public CreateAllAction(String name) {
        super(name);
    }

    /**
	 * CreateMapAction constructor comment.
	 * @param name java.lang.String
	 * @param icon javax.swing.Icon
	 */
    public CreateAllAction(String name, javax.swing.Icon icon) {
        super(name, icon);
    }

    /**
	 * Exports all and creates search database.
	 */
    public void actionPerformed(java.awt.event.ActionEvent arg1) {
        doIt();
    }

    /**
	 * Generates th search database by indexing all files within
	 * project directory and all files in subdirectories except 
	 * the directory JavaHelpSearch</code>.
	 * It makes use of <code>com.sun.java.help.search.Indexer</code> and
	 * invokes it as an extra process by calling <code>java</code>.
	 */
    public static void createSearchDataBase() {
        String proDir = ConfigHolder.CONF.getProjectDir() + File.separator;
        ArrayList<String> argList = new ArrayList<String>();
        argList.add("-db");
        argList.add(getTmpDir() + File.separator + "JavaHelpSearch");
        argList.add("-sourcepath");
        argList.add(proDir);
        String[] fileList = CreateMapAction.getMappedFiles();
        for (int i = 0; i < fileList.length; i++) argList.add(fileList[i]);
        try {
            String[] strsArgCompile = new String[argList.size()];
            strsArgCompile = argList.toArray(new String[0]);
            JHelpDevFrame.getAJHelpDevToolFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            indexer.compile(strsArgCompile);
            File newDir = new File(ConfigHolder.CONF.getProjectDir() + File.separator + "JavaHelpSearch");
            if (!newDir.isDirectory()) newDir.mkdir();
            String[] filesInSearchDir = new File(getTmpDir() + File.separator + "JavaHelpSearch").list();
            for (int i = 0; i < filesInSearchDir.length; i++) {
                HelperMethods.copyFileTo(newDir + "/" + filesInSearchDir[i], getTmpDir() + File.separator + "JavaHelpSearch/" + filesInSearchDir[i]);
            }
            System.out.println("Search database created with com.sun.java.help.search.Indexer.");
        } catch (Exception se) {
            System.out.println("Creation of search database failed.");
            se.printStackTrace();
            return;
        } finally {
            JHelpDevFrame.getAJHelpDevToolFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
	 * @see AbstractHelperAction
	 */
    public static void doIt() {
        ExportHsAction.doIt();
        ExportIndexAction.doIt();
        ExportTOCAction.doIt();
        createSearchDataBase();
    }

    /**
	 * Gets the directory defined in <code>java.io.tmpdir</code>. 
	 * 
	 * @return the temporary directory
	 */
    public static String getTmpDir() {
        if (tmpDir == null) {
            tmpDir = System.getProperty("java.io.tmpdir");
            System.out.println("Using temporary directory " + tmpDir + ".");
        }
        if (tmpDir == null || !new File(tmpDir).isDirectory()) {
            throw new RuntimeException("Property java.io.tmpdir could not be found or is not a directory.");
        }
        return tmpDir;
    }
}

package com.senn.magic.svn;

import java.io.File;
import com.senn.magic.util.FileUtils;

/**
 * Class to remove the <code>.svn</code>-structure from a folder (incl. subfolders).<br />
 * Can be run from commandline using the following command:<br />
 * <code>java -cp senn-magic-{version}.jar com.senn.magic.svn.SvnRemover</code>
 * 
 * @author Bart Thierens
 * 
 * <br>
 * 
 * Last modification: 02/06/2010
 *
 * @since 3.3
 */
public class SvnRemover {

    /**
	 * Main entrypoint for when the class is run from commandline (standalone).
	 * 
	 * @param args
	 * 		requires one parameter: location <br />
	 * 		<code>java -cp senn-magic.jar com.senn.magic.svn.SvnRemover C:/temp</code>
	 */
    public static void main(String[] args) {
        if ((args.length != 1) && "".equals(args[0])) throw new IllegalArgumentException("Location of folder must be provided");
        System.out.println("Starting removal of Svn items inside " + args[0]);
        if (remove(args[0])) System.out.println("Removal of Svn items successful!"); else System.out.println("Removal of Svn items failed!");
    }

    /**
	 * Removes the <code>.svn</code> structures from a folder and it's subfolders.
	 * 
	 * @param path
	 * 		the path to a directory on the filesystem
	 * 
	 * @return <code>boolean</code> - <code>true</code> if it succeeded, <code>false</code> if it didn't
	 */
    public static boolean remove(String path) {
        return remove(new File(path));
    }

    /**
	 * Removes the <code>.svn</code> structures from a folder and it's subfolders.
	 * 
	 * @param folder
	 * 		a {@link File} object representing a directory on the filesystem
	 * 
	 * @return <code>boolean</code> - <code>true</code> if it succeeded, <code>false</code> if it didn't
	 */
    public static boolean remove(File folder) {
        boolean bDelete = true;
        boolean bRecursive = true;
        for (File child : folder.listFiles()) {
            if (SvnChecker.isSvnFolder(child)) bDelete = FileUtils.deleteDir(child); else if (child.isDirectory()) bRecursive = remove(child);
        }
        return (bDelete && bRecursive);
    }
}

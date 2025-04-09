package org.m4eclipse.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.m4eclipse.M4EclipsePlugin;

/**
 * Utility methods
 */
public class Util {

    /**
	 * Helper method which creates a folder and, recursively, all its parent
	 * folders.
	 * 
	 * @param folder
	 *            The folder to create.
	 * 
	 * @throws CoreException
	 *             if creating the given <code>folder</code> or any of its
	 *             parents fails.
	 */
    public static void createFolder(IFolder folder) throws CoreException {
        if (!folder.exists()) {
            IContainer parent = folder.getParent();
            if (parent instanceof IFolder) {
                createFolder((IFolder) parent);
            }
            folder.create(false, true, null);
        }
    }

    /**
	 * Substitute any variable
	 */
    public static String substituteVar(String s) {
        if (s == null) {
            return s;
        }
        try {
            return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(s);
        } catch (CoreException e) {
            M4EclipsePlugin.log(e);
            return null;
        }
    }

    public static Model clone(Model model) {
        StringWriter buf = new StringWriter();
        try {
            new MavenXpp3Writer().write(buf, model);
            return new MavenXpp3Reader().read(new StringReader(buf.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] tab = dir.listFiles();
            if (tab != null) {
                for (int i = 0, max = tab.length; i < max; i++) {
                    if (tab[i].isDirectory()) deleteDir(tab[i]); else tab[i].delete();
                }
            }
        }
        dir.delete();
    }
}

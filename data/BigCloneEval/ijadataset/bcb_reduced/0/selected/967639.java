package jolie.xtext.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import jolie.xtext.ui.navigation.JolieHyperlinkHelper;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.editor.XtextSourceViewerConfiguration;
import org.eclipse.xtext.ui.editor.hyperlinking.IHyperlinkHelper;
import org.osgi.framework.Bundle;
import com.google.inject.Inject;
import org.eclipse.osgi.framework.internal.core.*;

/**
 * Use this class to register components to be used within the IDE.
 */
public class JolieUiModule extends jolie.xtext.ui.AbstractJolieUiModule {

    public JolieUiModule(AbstractUIPlugin plugin) {
        super(plugin);
        IProgressMonitor progressMonitor = new NullProgressMonitor();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject("JolieIncludedLibraries");
        try {
            if (!project.exists()) {
                project.create(progressMonitor);
                project.open(progressMonitor);
                Bundle bundle = Platform.getBundle("jolie.xtext.ui");
                URL entry = Platform.getBundle("jolie.xtext.ui").getEntry("JolieIncludedLibraries");
                URI uri;
                try {
                    URLConnection connection = entry.openConnection();
                    System.out.println("connection " + connection.toString());
                    URL fileURL = ((BundleURLConnection) connection).getFileURL();
                    System.out.println(fileURL.toString());
                    uri = new URI(fileURL.toString());
                    String path = new File(uri).getAbsolutePath();
                    File to = new File(uri);
                    File from = new File(project.getLocationURI());
                    System.out.println(from.length());
                    try {
                        copyDirectory(to, from);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    IProjectDescription description = project.getDescription();
                    description.setNatureIds(new String[] { XtextProjectHelper.NATURE_ID });
                    project.setDescription(description, null);
                    project.refreshLocal(1, progressMonitor);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Class<? extends IHyperlinkHelper> bindIHyperlinkHelper() {
        return JolieHyperlinkHelper.class;
    }

    public Class<? extends XtextSourceViewerConfiguration> bindXtextSourceViewerConfiguration() {
        return JolieSorceViewerConfiguration.class;
    }

    public void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        System.out.println(sourceLocation.exists());
        System.out.println(sourceLocation.list());
        System.out.println("Provo a copiare...");
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                System.out.println("Creo la cartella di destinazione perch� non esiste...");
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                System.out.println("Copio...");
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            System.out.println(" Copy the bits from instream to outstream");
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    static void log() {
    }
}

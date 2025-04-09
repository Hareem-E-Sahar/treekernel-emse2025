package org.codehaus.mojo.tomcatctx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * <p>NexOpen Framework</p>
 * 
 * <p></p>
 * 
 * @author Francesc Xavier Magdaleno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractTomcatContextMojo extends AbstractMojo {

    /**
	 * File location of Tomcat
	 * 
	 * @parameter
	 * @required
	 */
    protected String tomcatHome;

    /**
	 * <p>tomcat engine name</p>
	 * 
	 * @parameter default-value = "Catalina"
	 */
    protected String engineName;

    /**
	 * <p>tomcat host name</p>
	 * 
	 * @parameter default-value = "localhost"
	 */
    protected String hostName;

    /**
	 * @throws MojoExecutionException
	 */
    protected void checkConfig() throws MojoExecutionException {
        if (tomcatHome == null || tomcatHome.equals("ENV")) {
            tomcatHome = System.getenv("CATALINA_HOME");
        }
        if (tomcatHome == null || tomcatHome.length() == 0) {
            throw new MojoExecutionException("Neither CATALINA_HOME parameter nor the tomcatHome configuration parameter is set!");
        }
        return;
    }

    /**
	 * @param srcDir
	 * @param dstDir
	 * @throws IOException
	 */
    protected void copy(final File srcDir, final File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            final String children[] = srcDir.list();
            for (int i = 0; i < children.length; i++) {
                copy(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            copyFile(srcDir, dstDir);
        }
    }

    /**
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
    protected void copyFile(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            byte buf[] = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}

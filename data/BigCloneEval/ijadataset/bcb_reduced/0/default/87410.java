import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.apache.xml.serialize.*;
import org.apache.tools.ant.*;

/**
 * Task used to Update Distribution JNLP Files with Correct CodeBase etc
 */
public class UpdateDistJNLP extends org.apache.tools.ant.Task {

    String version;

    String siteDir;

    String localDir;

    DocumentBuilder builder;

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDistDir(String site_dir) {
        this.siteDir = site_dir;
    }

    public void setLocalDir(String local_dir) {
        this.localDir = local_dir;
    }

    public void execute() throws BuildException {
        File versionDir = new File(getProject().getBaseDir(), localDir);
        if (!versionDir.exists() || !versionDir.isDirectory()) {
            System.out.println("Dir: " + versionDir.toString() + " does not Exist. Cannot Update Dist JNLP Files!");
        }
        File files[] = versionDir.listFiles();
        try {
            DocumentBuilderFactory defaultFactory = DocumentBuilderFactory.newInstance();
            defaultFactory.setValidating(false);
            defaultFactory.setNamespaceAware(true);
            builder = defaultFactory.newDocumentBuilder();
        } catch (Throwable _e) {
            _e.printStackTrace();
            throw new BuildException("Error Creating XML Parser");
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) continue;
            if (files[i].getName().endsWith(".jnlp")) {
                fixCodeBase(files[i]);
            }
        }
    }

    private void fixCodeBase(File jnlpFile) {
        try {
            Document doc = builder.parse(jnlpFile);
            Element root = doc.getDocumentElement();
            String codebase = root.getAttribute("codebase");
            String href = root.getAttribute("href");
            if (codebase == null || codebase.equals("")) root.setAttribute("codebase", siteDir);
            if (href == null || href.equals("")) root.setAttribute("href", jnlpFile.getName());
            FileOutputStream out = new FileOutputStream(jnlpFile);
            XMLSerializer _s = new XMLSerializer(out, new OutputFormat(doc, "UTF-8", true));
            _s.serialize(doc);
            System.out.println("Updated:" + jnlpFile.toString());
        } catch (Exception exp) {
            System.out.println("Error Updating File:" + jnlpFile.toString() + ": " + exp.getMessage());
        }
    }
}

package org.signserver.anttasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.zip.ZipEntry;
import org.signserver.common.clusterclassloader.MARFileParser;

/**
 * TODO
 * 
 * 
 * @author Philip Vendil 28 jun 2008
 * @version $Id: MarAntTask.java 1875 2011-09-29 14:50:48Z netmackan $
 */
public class MarAntTask extends Task {

    private String version = "1";

    private String modulename = null;

    private String description = null;

    private String destfile = null;

    private boolean verbose = false;

    private List<PartAntTask> parts = new ArrayList<PartAntTask>();

    /**
     * Method called by ant
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        try {
            Integer.parseInt(version);
        } catch (NumberFormatException e) {
            throw new BuildException("Error: 'version' property must only contain digits");
        }
        if (destfile == null) {
            throw new BuildException("Error: 'destfile' property must be set.");
        }
        if (modulename == null) {
            modulename = new File(destfile).getName().toUpperCase();
            if (modulename.endsWith(".MAR")) {
                modulename = modulename.substring(0, modulename.length() - 4);
            }
        } else {
            modulename = modulename.toUpperCase();
        }
        if (parts.size() == 0) {
            throw new BuildException("Error: at least on 'part' task must exists in a mar configuration.");
        }
        String partsString = parts.get(0).getName();
        for (int i = 1; i < parts.size(); i++) {
            partsString += "," + parts.get(i).getName();
        }
        if (verbose) {
            System.out.println("Generating Module Archive : " + modulename);
            System.out.println("  Version                 : " + version);
            System.out.println("  Default Description     : " + (description == null ? "" : description));
            System.out.println("  Destination             : " + destfile);
            System.out.println("  Parts                   : " + partsString);
            System.out.println("");
        }
        Properties desc = new Properties();
        desc.setProperty(MARFileParser.MARDESCRIPTOR_VERSION, version);
        if (description != null) {
            desc.setProperty(MARFileParser.MARDESCRIPTOR_DEFAULTDESCRIPTION, description);
        }
        desc.setProperty(MARFileParser.MARDESCRIPTOR_MODULENAME, modulename);
        desc.setProperty(MARFileParser.MARDESCRIPTOR_PARTS, partsString);
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destfile));
            out.putNextEntry(new ZipEntry(MARFileParser.MARDESCRIPTOR_PATH));
            desc.store(out, "MAR Descriptor generated by ANT task.");
            out.closeEntry();
            for (PartAntTask part : parts) {
                zipFilesInPart(out, part);
            }
            out.close();
        } catch (IOException e) {
            throw new BuildException("Error: IO Exception when generated MAR file : " + e.getMessage());
        }
        super.execute();
    }

    private void zipFilesInPart(ZipOutputStream out, PartAntTask part) throws IOException {
        for (FileSet fs : part.getFileSets()) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] includedFiles = ds.getIncludedFiles();
            for (int i = 0; i < includedFiles.length; i++) {
                String filename = includedFiles[i].replace('\\', '/');
                filename = filename.substring(filename.lastIndexOf("/") + 1);
                writeFileToZip(out, ds.getBasedir() + "/" + includedFiles[i], part.getName() + "/" + filename);
            }
        }
    }

    private void writeFileToZip(ZipOutputStream out, String sourceFilename, String vPath) throws IOException {
        FileInputStream in = new FileInputStream(sourceFilename);
        out.putNextEntry(new ZipEntry(vPath));
        int len;
        byte[] buf = new byte[1024];
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.closeEntry();
        in.close();
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the moduleName
     */
    public String getModulename() {
        return modulename;
    }

    /**
     * @param modulename the moduleName to set
     */
    public void setModulename(String modulename) {
        this.modulename = modulename;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @return the destfile
     */
    public String getDestfile() {
        return destfile;
    }

    /**
     * @param destfile the destfile to set
     */
    public void setDestfile(String destfile) {
        this.destfile = destfile;
    }

    public void addConfiguredPart(PartAntTask part) {
        parts.add(part);
    }
}

package com.izforge.izpack.compiler;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.Info;
import com.izforge.izpack.Pack;

/**
 *  Standard packager.
 *
 * @author     Julien Ponge
 */
public class StdPackager extends Packager {

    /**  The zipped output stream. */
    protected JarOutputStream outJar;

    /**
   *  The constructor.
   *
   * @param  outputFilename  The output filename.
   * @param  plistener       The packager listener.
   * @exception  Exception   Description of the Exception
   */
    public StdPackager(String outputFilename, PackagerListener plistener) throws Exception {
        packs = new ArrayList();
        langpacks = new ArrayList();
        setPackagerListener(plistener);
        sendStart();
        FileOutputStream outFile = new FileOutputStream(outputFilename);
        outJar = new JarOutputStream(outFile);
        outJar.setLevel(9);
        sendMsg("Copying the skeleton installer ...");
        writeSkeletonInstaller(outJar);
    }

    /**
   *  Adds a pack (the compiler sends the merged data).
   *
   * @param  packNumber     The pack number.
   * @param  name           The pack name.
   * @param  required       Is the pack required ?
   * @param  osConstraints  The target operation system(s) of this pack.
   * @param  description    The pack description.
   * @return                Description of the Return Value
   * @exception  Exception  Description of the Exception
   */
    public OutputStream addPack(int packNumber, String name, String id, List osConstraints, boolean required, String description, boolean preselected) throws Exception {
        sendMsg("Adding pack #" + packNumber + " : " + name + " ...");
        Pack pack = new Pack(name, id, description, osConstraints, required, preselected);
        packs.add(packNumber, pack);
        String entryName = "packs/pack" + packNumber;
        ZipEntry entry = new ZipEntry(entryName);
        outJar.putNextEntry(entry);
        return outJar;
    }

    /**
   *  Sets the GUI preferences.
   *
   * @param  prefs          The new gUIPrefs value
   * @exception  Exception  Description of the Exception
   */
    public void setGUIPrefs(GUIPrefs prefs) throws Exception {
        sendMsg("Setting the GUI preferences ...");
        outJar.putNextEntry(new ZipEntry("GUIPrefs"));
        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
        objOut.writeObject(prefs);
        objOut.flush();
        outJar.closeEntry();
    }

    /**
   *  Adds a panel.
   *
   * @param  classFilename  The class filename.
   * @param  input          The stream to get the file data from.
   * @exception  Exception  Description of the Exception
   */
    public void addPanelClass(String classFilename, InputStream input) throws Exception {
        sendMsg("Adding the (sub)classes for " + classFilename + " ...");
        outJar.putNextEntry(new ZipEntry("com/izforge/izpack/panels/" + classFilename));
        copyStream(input, outJar);
        outJar.closeEntry();
    }

    /**
   *  Sets the panels order.
   *
   * @param  order          The ordered list of the panels.
   * @exception  Exception  Description of the Exception
   */
    public void setPanelsOrder(ArrayList order) throws Exception {
        sendMsg("Setting the panels order ...");
        outJar.putNextEntry(new ZipEntry("panelsOrder"));
        DataOutputStream datOut = new DataOutputStream(outJar);
        int size = order.size();
        datOut.writeInt(size);
        for (int i = 0; i < size; i++) datOut.writeUTF((String) order.get(i));
        datOut.flush();
        outJar.closeEntry();
    }

    /**
   *  Sets the informations related to this installation.
   *
   * @param  info           The info section.
   * @exception  Exception  Description of the Exception
   */
    public void setInfo(Info info) throws Exception {
        sendMsg("Setting the installer informations ...");
        outJar.putNextEntry(new ZipEntry("info"));
        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
        objOut.writeObject(info);
        objOut.flush();
        outJar.closeEntry();
    }

    /**
   *  Adds Variable Declaration.
   *
   * @param  varDef         The variables definitions.
   * @exception  Exception  Description of the Exception
   */
    public void setVariables(Properties varDef) throws Exception {
        sendMsg("Setting  the variables ...");
        outJar.putNextEntry(new ZipEntry("vars"));
        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
        objOut.writeObject(varDef);
        objOut.flush();
        outJar.closeEntry();
    }

    /**
   *  Adds a resource.
   *
   * @param  resId          The resource Id.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
    public void addResource(String resId, InputStream input) throws Exception {
        sendMsg("Adding resource : " + resId + " ...");
        outJar.putNextEntry(new ZipEntry("res/" + resId));
        copyStream(input, outJar);
        outJar.closeEntry();
    }

    /**
   *  Adds a native library.
   *
   * @param  name           The native library name.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
    public void addNativeLibrary(String name, InputStream input) throws Exception {
        sendMsg("Adding native library : " + name + " ...");
        outJar.putNextEntry(new ZipEntry("native/" + name));
        copyStream(input, outJar);
        outJar.closeEntry();
        input.close();
    }

    /**
   *  Adds a language pack.
   *
   * @param  iso3           The ISO3 code.
   * @param  input          The stream to get the data from.
   * @exception  Exception  Description of the Exception
   */
    public void addLangPack(String iso3, InputStream input) throws Exception {
        sendMsg("Adding langpack : " + iso3 + " ...");
        langpacks.add(iso3);
        outJar.putNextEntry(new ZipEntry("langpacks/" + iso3 + ".xml"));
        copyStream(input, outJar);
        outJar.closeEntry();
        input.close();
    }

    /**
   *  Adds a jar file content to the installer.
   *
   * @param  file           The jar filename.
   * @exception  Exception  Description of the Exception
   */
    public void addJarContent(String file) throws Exception {
        sendMsg("Adding a jar file content ...");
        JarFile jar = new JarFile(file);
        Enumeration entries = jar.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zentry = (ZipEntry) entries.nextElement();
            try {
                InputStream zin = jar.getInputStream(zentry);
                outJar.putNextEntry(new ZipEntry(zentry.getName()));
                copyStream(zin, outJar);
                outJar.closeEntry();
                zin.close();
            } catch (ZipException zerr) {
            }
        }
    }

    /**
   *  Tells the packager to finish the job (misc writings, cleanups, closings ,
   *  ...).
   *
   * @exception  Exception  Description of the Exception
   */
    public void finish() throws Exception {
        DataOutputStream datOut;
        ObjectOutputStream objOut;
        int size;
        int i;
        sendMsg("Finishing the enpacking ...");
        outJar.putNextEntry(new ZipEntry("kind"));
        datOut = new DataOutputStream(outJar);
        datOut.writeUTF("standard");
        datOut.flush();
        outJar.closeEntry();
        outJar.putNextEntry(new ZipEntry("packs.info"));
        objOut = new ObjectOutputStream(outJar);
        size = packs.size();
        objOut.writeInt(size);
        for (i = 0; i < size; i++) objOut.writeObject(packs.get(i));
        objOut.flush();
        outJar.closeEntry();
        outJar.putNextEntry(new ZipEntry("langpacks.info"));
        datOut = new DataOutputStream(outJar);
        size = langpacks.size();
        datOut.writeInt(size);
        for (i = 0; i < size; i++) datOut.writeUTF((String) langpacks.get(i));
        datOut.flush();
        outJar.closeEntry();
        outJar.flush();
        outJar.close();
        sendStop();
    }
}

package com.bnpparibas.frmk.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * <P>La classe <strong>BuildNumberIncrement</strong> permet d'incr�menter le num�ro de
 * version d'un jar.
 * <P>Ce num�ro de version est stock� dans le fichier manifest du jar.<BR>
 * Si le manifest n'existe pas, il est cr�e.<BR>
 * Si le manifest ne contient pas de num�ro de version, cette t�che en cr�e un.
 * <P>L'attribut non obligatoire <code>newMajorRelease</code> permet de remplacer
 * le num�ro de version majeure par celui sp�cifi�.
 * <P>L'attribut non obligatoire <code>newMinorRelease</code> permet de remplacer
 * le num�ro de version mineure par celui sp�cifi�. Dans ce cas, ce num�ro de build
 * n'est pas incr�ment�.
 *
 * @author Framework JAVA BP2S
 * @version 1.0 Cr�ation de la classe
 * @version 1.1 La tache modifie d�sormais le manifest et non un fichier version.properties
 * @version 1.2 - 05/12/2002 - Fr�d�ric SOIGNEUX - Retire l'appel au VersionManager. Il faut
 *                que le package ant ne d�pende plus des autres packages.
 *                Ajout des m�thodes getNewMajorRelease() et getNewMinorRelease().
 */
public class BuildNumberIncrement extends Task {

    /** DOCUMENT ME! */
    public static final String DEFAULT_VERSION_JARNAME = "version.jar";

    /** DOCUMENT ME! */
    public static final String DEFAULT_VERSION_JARDIR = "/WEB-INF/lib/";

    /** DOCUMENT ME! */
    public static final String ENVTYPE_HEADER_STRING = "Specification-Type: ";

    /** DOCUMENT ME! */
    public static final String VERSION_HEADER_STRING = "Manifest-Version: ";

    /** DOCUMENT ME! */
    public static final String BUILD_HEADER_STRING = "Implementation-Version: \"Build (";

    /**
    * Variable utilis�e pour stocker le type d'environnement (DEV, TEST, PROD)
    */
    private String m_envType = null;

    /**
    * Emplacement du fichier jar
    */
    private String m_jarPath = null;

    /**
    * Nouveau num�ro de version majeure � placer dans le fichier manifest
    */
    private String m_newMajorRelease = null;

    /**
    * Nouveau num�ro de version mineure � placer dans le fichier manifest
    */
    private String m_newMinorRelease = null;

    /**
    * Variable utilis�e pour stocker le nouveau num�ro de version
    */
    private String m_newVersion = null;

    /**
    * Creates a new BuildNumberIncrement object.
    */
    public BuildNumberIncrement() {
    }

    /**
    * The method executing the task
    * @throws BuildException exception
    */
    public void execute() throws BuildException {
        boolean manifestFound = false;
        ZipFile zipFile = null;
        ZipInputStream zipInputStream = null;
        ZipOutputStream zipFileOutputStream = null;
        FileInputStream fileInputStream = null;
        File fileIn = new File(m_jarPath);
        if (!fileIn.exists()) {
            throw new BuildException("File " + m_jarPath + " does not exists.");
        }
        File fileOut = new File(m_jarPath + ".new");
        try {
            zipFile = new ZipFile(fileIn);
            fileInputStream = new FileInputStream(fileIn);
            zipInputStream = new ZipInputStream(fileInputStream);
            FileOutputStream fileOutputStream = new FileOutputStream(fileOut);
            zipFileOutputStream = new ZipOutputStream(fileOutputStream);
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                } else {
                    InputStream in = zipFile.getInputStream(entry);
                    byte[] content = readInputStream(in);
                    if (entry.getName().endsWith("manifest.mf")) {
                        manifestFound = true;
                        String contenu = incrementVersionInManifest(content);
                        zipFileOutputStream.putNextEntry(entry);
                        zipFileOutputStream.write(contenu.getBytes());
                        zipFileOutputStream.flush();
                    } else {
                        zipFileOutputStream.putNextEntry(entry);
                        zipFileOutputStream.write(content);
                        zipFileOutputStream.flush();
                    }
                }
                entry = zipInputStream.getNextEntry();
            }
            if (!manifestFound) {
                ZipEntry newEntry = new ZipEntry("/meta-inf/manifest.mf");
                zipFileOutputStream.putNextEntry(newEntry);
                if (m_newMajorRelease == null) {
                    m_newMajorRelease = "1.0";
                }
                if (m_newMinorRelease == null) {
                    m_newMinorRelease = "1";
                }
                String content = "Manifest-Version: " + m_newMajorRelease + "\nImplementation-Version: \"Build (" + m_newMinorRelease + ")\"";
                if ((m_envType != null) && (m_envType.length() > 0)) {
                    content += (ENVTYPE_HEADER_STRING + m_envType + "\n");
                }
                zipFileOutputStream.write(content.getBytes());
                zipFileOutputStream.flush();
                m_newVersion = m_newMajorRelease + "." + m_newMinorRelease;
            }
        } catch (Exception e) {
            throw new BuildException(e.getMessage(), e);
        } finally {
            try {
                if (zipFileOutputStream != null) {
                    zipFileOutputStream.close();
                }
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException e) {
            }
        }
        fileIn.delete();
        fileOut.renameTo(fileIn);
        System.out.println("Version increased in jar " + m_jarPath + " to " + m_newVersion);
    }

    /**
    * DOCUMENT ME!
    *
    * @param _content DOCUMENT ME!
    *
    * @return DOCUMENT ME!
    */
    protected String incrementVersionInManifest(byte[] _content) {
        String contenu = new String(_content);
        String numVersionMaj = null;
        int indexManifestStart = contenu.indexOf(VERSION_HEADER_STRING);
        int indexManifestEnd = -1;
        if (indexManifestStart != -1) {
            indexManifestEnd = contenu.indexOf("\n", indexManifestStart);
        }
        if (m_newMajorRelease != null) {
            numVersionMaj = m_newMajorRelease;
            if (indexManifestEnd != -1) {
                contenu = contenu.substring(0, indexManifestStart).trim() + VERSION_HEADER_STRING + m_newMajorRelease + "\n" + contenu.substring(indexManifestEnd).trim() + "\n";
            } else if (indexManifestStart != -1) {
                contenu = contenu.substring(0, indexManifestStart).trim() + VERSION_HEADER_STRING + m_newMajorRelease;
            } else {
                contenu = VERSION_HEADER_STRING + m_newMajorRelease + "\n" + contenu.trim() + "\n";
            }
        } else {
            numVersionMaj = "";
            if (indexManifestEnd != -1) {
                numVersionMaj = contenu.substring(indexManifestStart + VERSION_HEADER_STRING.length(), indexManifestEnd);
            } else if (indexManifestStart != -1) {
                numVersionMaj = contenu.substring(indexManifestStart + VERSION_HEADER_STRING.length());
            }
        }
        int indexMin = contenu.indexOf(BUILD_HEADER_STRING);
        if (indexMin != -1) {
            int indexMax = contenu.indexOf(")", indexMin + BUILD_HEADER_STRING.length());
            int numeroBuild = -1;
            if ((m_newMinorRelease == null) || (m_newMinorRelease.length() == 0)) {
                String sNumeroBuild;
                if (indexMax != -1) {
                    sNumeroBuild = contenu.substring(indexMin + BUILD_HEADER_STRING.length(), indexMax);
                } else {
                    sNumeroBuild = contenu.substring(indexMin + BUILD_HEADER_STRING.length());
                }
                numeroBuild = Integer.parseInt(sNumeroBuild);
                numeroBuild++;
            } else {
                numeroBuild = Integer.parseInt(m_newMinorRelease);
            }
            m_newVersion = numVersionMaj + "." + numeroBuild;
            if (indexMax != -1) {
                contenu = contenu.substring(0, indexMin).trim() + "\n" + BUILD_HEADER_STRING + numeroBuild + contenu.substring(indexMax).trim();
            } else {
                contenu = contenu.substring(0, indexMin).trim() + "\n" + BUILD_HEADER_STRING + numeroBuild;
            }
        } else {
            if (m_newMinorRelease == null) {
                m_newMinorRelease = "1";
            }
            contenu = contenu.trim() + "\n" + BUILD_HEADER_STRING + m_newMinorRelease + ")\"";
            m_newVersion = numVersionMaj + "." + m_newMinorRelease;
        }
        contenu += "\n";
        int indexEnvType = contenu.indexOf(ENVTYPE_HEADER_STRING);
        int indexMaxEnvType = -1;
        if (indexEnvType != -1) {
            indexMaxEnvType = contenu.indexOf("\n", indexEnvType);
        }
        if (indexEnvType != -1) {
            String tempStr = contenu.substring(0, indexEnvType).trim();
            if ((m_envType != null) && (m_envType.length() > 0) && (!m_envType.startsWith("${"))) {
                tempStr += ("\n" + ENVTYPE_HEADER_STRING + m_envType);
            }
            if (indexMaxEnvType != -1) {
                tempStr += ("\n" + contenu.substring(indexMaxEnvType).trim());
            }
            contenu = tempStr.trim() + "\n";
        } else {
            if ((m_envType != null) && (m_envType.length() > 0) && (!m_envType.startsWith("${"))) {
                contenu += (ENVTYPE_HEADER_STRING + m_envType + "\n");
            }
        }
        return contenu;
    }

    /**
    * Cette m�thode lit un flux d'entr�e et renvoie le contenu.
    * @param _stream InputStream
    * @return byte[]
    * @throws IOException exception
    */
    protected byte[] readInputStream(InputStream _stream) throws IOException {
        int maxLength = _stream.available();
        byte[] content = new byte[maxLength];
        int offset = 0;
        int length = 0;
        while (offset < maxLength) {
            length = _stream.read(content, offset, maxLength - offset);
            offset += length;
        }
        return content;
    }

    /**
    * DOCUMENT ME!
    *
    * @param _jarPath DOCUMENT ME!
    */
    public void setJarPath(String _jarPath) {
        m_jarPath = _jarPath;
    }

    /**
    * DOCUMENT ME!
    *
    * @param _newMajorRelease DOCUMENT ME!
    */
    public void setNewMajorRelease(String _newMajorRelease) {
        m_newMajorRelease = _newMajorRelease;
    }

    /**
    * DOCUMENT ME!
    *
    * @param _newMinorRelease DOCUMENT ME!
    */
    public void setNewMinorRelease(String _newMinorRelease) {
        m_newMinorRelease = _newMinorRelease;
    }

    /**
    * DOCUMENT ME!
    *
    * @param _envType DOCUMENT ME!
    */
    public void setEnvType(String _envType) {
        m_envType = _envType;
    }

    /**
    * DOCUMENT ME!
    *
    * @return DOCUMENT ME!
    */
    public String getNewMajorRelease() {
        return m_newMajorRelease;
    }

    /**
    * DOCUMENT ME!
    *
    * @return DOCUMENT ME!
    */
    public String getNewMinorRelease() {
        return m_newMinorRelease;
    }
}

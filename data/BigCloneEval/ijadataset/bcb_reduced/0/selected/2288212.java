package br.gov.demoiselle.eclipse.util.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import br.gov.demoiselle.eclipse.util.utility.classwriter.ClassHelper;
import br.gov.demoiselle.eclipse.util.utility.classwriter.RoundTrip;

/**
 * Utility class for file manipulation on Editing Project
 * @author SERPRO/CETEC/CTCTA
 *
 */
public class FileUtil implements CoreConstants {

    public static final int BUFFER = 2 * 8192;

    /**
	 * Copy all files from a directory to another, if the directory doesn't exists, it will be created.
	 * @param File from directory
	 * @param File to directory
	 */
    public static void copyDirectory(File source, File to) throws IOException {
        if (source.isDirectory()) {
            if (!to.exists()) {
                to.mkdir();
            }
            String[] children = source.list();
            for (int i = 0; i < children.length; i++) {
                File newSource = new File(source, children[i]);
                File newTo = new File(to, children[i]);
                copyDirectory(newSource, newTo);
            }
        } else {
            try {
                copyFile(source, to);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("It can't copy the file: " + source.getName());
            }
        }
    }

    /**
	 * Copy all files from a directory to another, if the directory doesn't exists, it will be created.
	 * @param String from directory
	 * @param String to directory
	 */
    public static void copyFile(String source, String to) throws Exception {
        File sourceFile = new File(source);
        File toFile = new File(to);
        copyFile(sourceFile, toFile);
    }

    /**
	 * Copy all files from a directory to another, if the directory doesn't exists, it will be created.
	 * @param File from directory
	 * @param File to directory
	 */
    public static void copyFile(File source, File to) throws Exception {
        FileInputStream fin = new FileInputStream(source);
        File path = new File(to.getAbsolutePath().substring(0, to.getAbsolutePath().lastIndexOf(File.separator)));
        path.mkdirs();
        String pathTo = to.getAbsolutePath();
        if (!to.delete()) {
            System.out.println("Remove file failed");
        }
        FileOutputStream fout = new FileOutputStream(pathTo);
        try {
            byte[] buf = new byte[BUFFER];
            int i = 0;
            while ((i = fin.read(buf)) != -1) {
                fout.write(buf, 0, i);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (fin != null) fin.close();
            if (fout != null) fout.close();
        }
    }

    /**
	 * Find the class file and formats and organize all code of it
	 * 
	 * @param String path of class
	 * @return String contents the format code of the class
	 */
    public static String formatClass(String path) {
        String result = null;
        try {
            Path realPath = new Path(path);
            IFile file = EclipseUtil.getSelectedProject().getFile(realPath);
            result = FormatterCode.formatter(file);
            Charset charSet = Charset.forName(EclipseUtil.getSelectedProject().getDefaultCharset());
            if (charSet != null) {
                CharBuffer c = charSet.decode(ByteBuffer.wrap(result.getBytes()));
                result = c.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
	 *  Search for files on informed path and package name, with extension filter 
	 * @param path
	 * @param packageName
	 * @param extensionFilter
	 * @return
	 */
    public static List<String> findFiles(File path, String packageName, String extensionFilter) {
        List<String> result = new ArrayList<String>();
        String[] files = path.list();
        for (int i = 0; i < files.length; i++) {
            String string = files[i];
            File file = new File(path.getAbsolutePath() + "/" + string);
            if (file.isFile()) {
                String nameFile = file.getName();
                int fileSize = nameFile.length();
                if (fileSize > extensionFilter.length()) {
                    String extension = nameFile.substring(fileSize - extensionFilter.length(), fileSize);
                    if (extension.compareToIgnoreCase(extensionFilter) == 0) {
                        if ((packageName != null) && (!packageName.equals(""))) {
                            nameFile = packageName + "/" + nameFile;
                        }
                        result.add(nameFile);
                    }
                }
            } else {
                if ((packageName != null) && (!packageName.equals(""))) {
                    result.addAll(findFiles(file, packageName.concat("/").concat(file.getName()), extensionFilter));
                } else {
                    result.addAll(findFiles(file, packageName, extensionFilter));
                }
            }
        }
        return result;
    }

    /**
	 * Search for Java files for informed package, on source location 
	 * @param packageName
	 * @return
	 */
    public static List<String> findJavaFiles(String packageName) {
        File packageFile = new File(EclipseUtil.getSourceLocation() + "/" + packageName.replace(".", "/"));
        if (packageFile.exists()) {
            return findFiles(packageFile, packageName, EXTENSION_JAVA);
        } else {
            return null;
        }
    }

    /**
	 * Search for Java files for informed package, on test location 
	 * @param packageName
	 * @return
	 */
    public static List<String> findJavaTestFiles(String packageName) {
        File packageFile = null;
        if (packageName != null && packageName.trim().length() > 0) {
            packageFile = new File(EclipseUtil.getTestSourceLocation() + "/" + packageName.replace(".", "/"));
        } else {
            packageFile = new File(EclipseUtil.getTestSourceLocation());
        }
        if (packageFile.exists()) {
            return findFiles(packageFile, packageName, EXTENSION_JAVA);
        } else {
            return null;
        }
    }

    /**
	 * Search for HBM (Hibernate) files for informed package 
	 * @param packageName
	 * @return
	 */
    public static List<String> findHBMFiles(String packageName) {
        File packageFile = new File(EclipseUtil.getResourceLocation() + "/" + packageName);
        if (packageFile.exists()) {
            return findFiles(packageFile, packageName, EXTENSION_HBM);
        } else {
            return null;
        }
    }

    /**
	 * Search for JSP Files
	 * @param shell
	 * @param title
	 * @return
	 */
    public static String findJSPFile(Shell shell, String title) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        String context = EclipseUtil.getContextLocation();
        dialog.setText(title);
        dialog.setFilterPath(context);
        String fileName = dialog.open();
        if (fileName != null) {
            fileName = fileName.substring(context.length()).replace("\\", "/");
            return fileName;
        }
        return null;
    }

    /**
	 * Search for JSP Files
	 * @param shell
	 * @param title
	 * @return
	 */
    public static String findJSPDirectory(Shell shell, String title, String msg) {
        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
        String context = EclipseUtil.getContextLocation();
        dialog.setText(title);
        dialog.setFilterPath(context);
        dialog.setMessage(msg);
        String fileName = dialog.open();
        if (fileName != null) {
            fileName = fileName.substring(context.length()).replace("\\", "/");
            return fileName;
        }
        return null;
    }

    /**
	 * Remove informed class on source location.
	 * @param name
	 * @return
	 * @throws CoreException 
	 * @see EclipseUtil.getSourceLocation()
	 */
    public static boolean removeClass(String name) throws CoreException {
        String path = EclipseUtil.getSourceLocation() + "/" + name.replace(".", "/") + EXTENSION_JAVA;
        return deleteFile(path);
    }

    /**
	 * Remove informed class on tests source location. 
	 * @param name
	 * @return
	 * @throws CoreException 
	 * @see EclipseUtil.getTestSourceLocation()
	 */
    public static boolean removeTestClass(String name) throws CoreException {
        String path = EclipseUtil.getTestSourceLocation() + "/" + name.replace(".", "/") + EXTENSION_JAVA;
        return deleteFile(path);
    }

    public static boolean deleteFile(String pathFile) throws CoreException {
        File file = new File(pathFile);
        if (file.exists()) {
            if (file.delete()) {
                EclipseUtil.updateProject();
                return true;
            }
        }
        return false;
    }

    /**
	 * Checks if a informed name is a valid JSP file.
	 * @param name
	 * @return
	 */
    public static boolean validNameOfJSPFile(String name) {
        if (name.length() < 5) {
            return false;
        } else if (!name.substring(name.length() - 4, name.length()).toLowerCase().equals(".jsp")) {
            return false;
        } else {
            return true;
        }
    }

    /**
	 * Cria o arquivo .java e o formata no projeto.
	 * 
	 * @author Robson S. Ximenes 13/11/2007
	 * @param classe
	 * @throws Exception
	 */
    public static void writeClassFile(String path, ClassHelper classe, boolean roundTrip, boolean isInterface) {
        try {
            FileUtil.createFolder(path);
            String className = classe.getPackageName() + "." + classe.getName();
            String arquivo = path + classe.getName() + EXTENSION_JAVA;
            if (roundTrip) {
                classe = RoundTrip.merge(arquivo, classe, isInterface);
            }
            String content = classe.toString();
            FileOutputStream file = new FileOutputStream(arquivo);
            String encode = EclipseUtil.getSelectedProject().getDefaultCharset();
            Writer fw = new OutputStreamWriter(file, encode);
            fw.write(content);
            fw.close();
            EclipseUtil.updateProject();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            String format = formatClass(PATH_SRC_JAVA + File.separator + className.replace(".", File.separator) + EXTENSION_JAVA);
            if (format != null) {
                FileOutputStream fileFormat = new FileOutputStream(arquivo);
                fw = new OutputStreamWriter(fileFormat, encode);
                fw.write(format);
                fw.close();
            }
            EclipseUtil.updateProject();
            EclipseUtil.buildFullCurrentProject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }

    /**
 * Create the .java file and format on project
*
 * @author Robson S. Ximenes 13/11/2007
 * @author CETEC/CTCTA 2009
 * @param path
 * @param classe
 * @param roundTrip
 * @param isInterface
 * @param isTest
 */
    public static void writeClassFile(String path, ClassHelper classe, boolean roundTrip, boolean isInterface, boolean isTest) {
        try {
            FileUtil.createFolder(path);
            String className = classe.getPackageName() + "." + classe.getName();
            String arquivo = path + classe.getName() + EXTENSION_JAVA;
            if (roundTrip) {
                classe = RoundTrip.merge(arquivo, classe, isInterface);
            }
            String content = classe.toString();
            FileOutputStream file = new FileOutputStream(arquivo);
            String encode = EclipseUtil.getSelectedProject().getDefaultCharset();
            Writer fw = new OutputStreamWriter(file, encode);
            fw.write(content);
            fw.close();
            EclipseUtil.updateProject();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            String format = null;
            if (isTest) {
                format = formatClass(PATH_SRC_TEST_JAVA + File.separator + className.replace(".", File.separator) + EXTENSION_JAVA);
            } else {
                format = formatClass(PATH_SRC_JAVA + File.separator + className.replace(".", File.separator) + EXTENSION_JAVA);
            }
            if (format != null) {
                FileOutputStream fileFormat = new FileOutputStream(arquivo);
                fw = new OutputStreamWriter(fileFormat, encode);
                fw.write(format);
                fw.close();
            }
            EclipseUtil.updateProject();
            EclipseUtil.buildFullCurrentProject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }

    /**
	 *  Write content on file
	 * @param path
	 * @param content
	 */
    public static void writeFile(String path, String content) {
        File file = new File(path);
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.close();
        } catch (Exception e) {
        }
    }

    /**
	 * Read a informed file
	 * @param fileName
	 * @return content of file
	 */
    public static String readFile(String fileName) {
        StringBuffer content = new StringBuffer();
        try {
            File fileExists = new File(fileName);
            if (fileExists.exists()) {
                FileReader file = new FileReader(fileName);
                BufferedReader input = new BufferedReader(file);
                try {
                    String line = null;
                    while ((line = input.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                } finally {
                    input.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return content.toString();
    }

    /**
	 * @param file
	 * @return Name of file without directory name
	 */
    public static String extractFileName(String file) {
        if (file != null) {
            int index = file.lastIndexOf("/");
            if (index >= 0) {
                return file.substring(index + 1);
            }
        }
        return "";
    }

    /**
	 * Create a folder after informed path
	 * @param path
	 */
    public static void createFolder(String path) {
        File pathFile = new File(path);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
    }

    /**
	 * 
	 * @param path
	 * @param key
	 * @return
	 */
    public static String readAttributesFromTxtClass(String path, String key) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            String str;
            while ((str = in.readLine()) != null) {
                if (str.indexOf(key + EQUAL) >= 0) {
                    return str.substring(str.indexOf(EQUAL) + 1, str.length());
                }
            }
            in.close();
            return "";
        } catch (IOException e) {
        }
        return "";
    }

    /**
	 * 
	 * @param pathFile
	 * @return true if file exists
	 * @throws CoreException
	 */
    public static boolean hasFile(String pathFile) throws CoreException {
        File file = new File(pathFile);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
	 * 
	 * @param javaClass
	 * @return true if the javaClass exists
	 * @throws CoreException
	 */
    public static boolean hasJavaFile(String javaClass) throws CoreException {
        String path = EclipseUtil.getSourceLocation() + "/" + javaClass.replace(".", "/") + EXTENSION_JAVA;
        return hasFile(path);
    }

    public static File[] listFilesAsArray(File directory, FilenameFilter filter, boolean recurse) {
        Collection<File> files = listFiles(directory, filter, recurse);
        File[] arr = new File[files.size()];
        return files.toArray(arr);
    }

    public static Collection<File> listFiles(File directory, FilenameFilter filter, boolean recurse) {
        Vector<File> files = new Vector<File>();
        File[] entries = directory.listFiles();
        if (entries != null) {
            for (File entry : entries) {
                if (filter == null || filter.accept(directory, entry.getName())) {
                    files.add(entry);
                }
                if (recurse && entry.isDirectory()) {
                    files.addAll(listFiles(entry, filter, recurse));
                }
            }
        }
        return files;
    }
}

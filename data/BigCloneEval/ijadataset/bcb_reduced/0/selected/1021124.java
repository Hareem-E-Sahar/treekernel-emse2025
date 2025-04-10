package ptolemy.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 A collection of utilities for manipulating files
 These utilities do not depend on any other ptolemy.* packages.

 @author Christopher Brooks
 @version $Id: FileUtilities.java,v 1.54.4.3 2008/03/25 22:32:41 cxh Exp $
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class FileUtilities {

    /** Instances of this class cannot be created.
     */
    private FileUtilities() {
    }

    /** Copy sourceURL to destinationFile without doing any byte conversion.
     *  @param sourceURL The source URL
     *  @param destinationFile The destination File.
     *  @return true if the file was copied, false if the file was not
     *  copied because the sourceURL and the destinationFile refer to the
     *  same file.
     *  @exception IOException If the source file does not exist.
     */
    public static boolean binaryCopyURLToFile(URL sourceURL, File destinationFile) throws IOException {
        URL destinationURL = destinationFile.getCanonicalFile().toURI().toURL();
        if (sourceURL.sameFile(destinationURL)) {
            return false;
        }
        File sourceFile = new File(sourceURL.getFile());
        if ((sourceFile.getPath().indexOf("!/") == -1) && (sourceFile.getPath().indexOf("!\\") == -1)) {
            try {
                if (sourceFile.getCanonicalFile().toURI().toURL().sameFile(destinationURL)) {
                    return false;
                }
            } catch (IOException ex) {
                IOException ioException = new IOException("Cannot find canonical file name of '" + sourceFile + "'");
                ioException.initCause(ex);
                throw ioException;
            }
        }
        _binaryCopyStream(sourceURL.openStream(), destinationFile);
        return true;
    }

    /** Extract a jar file into a directory.  This is a trivial
     *  implementation of the <code>jar -xf</code> command.
     *  @param jarFileName The name of the jar file to extract
     *  @param directoryName The name of the directory.  If this argument
     *  is null, then the files are extracted in the current directory.
     *  @exception IOException If the jar file cannot be opened, or
     *  if there are problems extracting the contents of the jar file
     */
    public static void extractJarFile(String jarFileName, String directoryName) throws IOException {
        JarFile jarFile = new JarFile(jarFileName);
        Enumeration entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) entries.nextElement();
            File destinationFile = new File(directoryName, jarEntry.getName());
            if (jarEntry.isDirectory()) {
                if (!destinationFile.isDirectory() && !destinationFile.mkdirs()) {
                    throw new IOException("Warning, failed to create " + "directory for \"" + destinationFile + "\".");
                }
            } else {
                _binaryCopyStream(jarFile.getInputStream(jarEntry), destinationFile);
            }
        }
    }

    /** Extract the contents of a jar file.
     *  @param args An array of arguments.  The first argument
     *  names the jar file to be extracted.  The first argument
     *  is required.  The second argument names the directory in
     *  which to extract the files from the jar file.  The second
     *  argument is optional.
     */
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java -classpath $PTII " + "ptolemy.util.FileUtilities jarFile [directory]\n" + "where jarFile is the name of the jar file\n" + "and directory is the optional directory in which to " + "extract.");
            StringUtilities.exit(2);
        }
        String jarFileName = args[0];
        String directoryName = null;
        if (args.length >= 2) {
            directoryName = args[1];
        }
        try {
            extractJarFile(jarFileName, directoryName);
        } catch (Throwable throwable) {
            System.err.println("Failed to extract \"" + jarFileName + "\"");
            throwable.printStackTrace();
            StringUtilities.exit(3);
        }
    }

    /** Given a file name or URL, construct a java.io.File object that
     *  refers to the file name or URL.  This method
     *  first attempts to directly use the file name to construct the
     *  File. If the resulting File is a relative pathname, then
     *  it is resolved relative to the specified base URI, if
     *  there is one.  If there is no such base URI, then it simply
     *  returns the relative File object.  See the java.io.File
     *  documentation for a details about relative and absolute pathnames.
     *
     *  <p>
     *  The file need not exist for this method to succeed.  Thus,
     *  this method can be used to determine whether a file with a given
     *  name exists, prior to calling openForWriting(), for example.
     *
     *  <p>This method is similar to
     *  {@link #nameToURL(String, URI, ClassLoader)}
     *  except that in this method, the file or URL must be readable.
     *  Usually, this method is use for write a file and
     *  {@link #nameToURL(String, URI, ClassLoader)} is used for reading.
     *
     *  @param name The file name or URL.
     *  @param base The base for relative URLs.
     *  @return A File, or null if the filename argument is null or
     *   an empty string.
     *  @see #nameToURL(String, URI, ClassLoader)
     */
    public static File nameToFile(String name, URI base) {
        if ((name == null) || name.trim().equals("")) {
            return null;
        }
        File file = new File(name);
        if (!file.isAbsolute()) {
            if (base != null) {
                URI newURI = base.resolve(name);
                String urlString = newURI.getPath();
                file = new File(StringUtilities.substitute(urlString, "%20", " "));
            }
        }
        return file;
    }

    /** Given a file or URL name, return as a URL.  If the file name
     *  is relative, then it is interpreted as being relative to the
     *  specified base directory. If the name begins with
     *  "xxxxxxCLASSPATHxxxxxx" or "$CLASSPATH" then search for the
     *  file relative to the classpath.
     *
     *  <p>Note that "xxxxxxCLASSPATHxxxxxx" is the value of the
     *  globally defined constant $CLASSPATH available in the Ptolemy
     *  II expression language.
     *
     *  <p>If no file is found, then throw an exception.
     *
     *  <p>This method is similar to {@link #nameToFile(String, URI)}
     *  except that in this method, the file or URL must be readable.
     *  Usually, this method is use for reading a file and
     *  is used for writing {@link #nameToFile(String, URI)}.
     *
     *  @param name The name of a file or URL.
     *  @param baseDirectory The base directory for relative file names,
     *   or null to specify none.
     *  @param classLoader The class loader to use to locate system
     *   resources, or null to use the system class loader that was used
     *   to load this class.
     *  @return A URL, or null if the name is null or the empty string.
     *  @exception IOException If the file cannot be read, or
     *   if the file cannot be represented as a URL (e.g. System.in), or
     *   the name specification cannot be parsed.
     *  @see #nameToFile(String, URI)
     */
    public static URL nameToURL(String name, URI baseDirectory, ClassLoader classLoader) throws IOException {
        if ((name == null) || name.trim().equals("")) {
            return null;
        }
        if (name.startsWith(_CLASSPATH_VALUE) || name.startsWith("$CLASSPATH")) {
            String classpathKey;
            if (name.startsWith(_CLASSPATH_VALUE)) {
                classpathKey = _CLASSPATH_VALUE;
            } else {
                classpathKey = "$CLASSPATH";
            }
            String trimmedName = name.substring(classpathKey.length() + 1);
            if (classLoader == null) {
                String referenceClassName = "ptolemy.util.FileUtilities";
                try {
                    Class referenceClass = Class.forName(referenceClassName);
                    classLoader = referenceClass.getClassLoader();
                } catch (Exception ex) {
                    IOException ioException = new IOException("Cannot look up class \"" + referenceClassName + "\" or get its ClassLoader.");
                    ioException.initCause(ex);
                    throw ioException;
                }
            }
            URL result = classLoader.getResource(trimmedName);
            if (result == null) {
                throw new IOException("Cannot find file '" + trimmedName + "' in classpath");
            }
            return result;
        }
        File file = new File(name);
        if (file.isAbsolute()) {
            if (!file.canRead()) {
                file = new File(StringUtilities.substitute(name, "%20", " "));
                URL possibleJarURL = null;
                if (!file.canRead()) {
                    possibleJarURL = ClassUtilities.jarURLEntryResource(name);
                    if (possibleJarURL != null) {
                        file = new File(possibleJarURL.getFile());
                    }
                }
                if (!file.canRead()) {
                    throw new IOException("Cannot read file '" + name + "' or '" + StringUtilities.substitute(name, "%20", " ") + "'" + ((possibleJarURL == null) ? "" : (" or '" + possibleJarURL.getFile() + "")));
                }
            }
            return file.toURI().toURL();
        } else {
            if (baseDirectory != null) {
                URI newURI;
                try {
                    newURI = baseDirectory.resolve(name);
                } catch (Exception ex) {
                    String name2 = StringUtilities.substitute(name, "%20", " ");
                    try {
                        newURI = baseDirectory.resolve(name2);
                        name = name2;
                    } catch (Exception ex2) {
                        IOException io = new IOException("Problem with URI format in '" + name + "'. " + "and '" + name2 + "' " + "This can happen if the file name " + "is not absolute" + "and is not present relative to the " + "directory in which the specified model " + "was read (which was '" + baseDirectory + "')");
                        io.initCause(ex2);
                        throw io;
                    }
                }
                String urlString = newURI.toString();
                try {
                    if ((newURI.getScheme() != null) && (newURI.getAuthority() == null)) {
                        urlString = urlString.substring(0, 6) + "//" + urlString.substring(6);
                    }
                    return new URL(urlString);
                } catch (Exception ex3) {
                    try {
                        return new URL(baseDirectory.toURL(), urlString);
                    } catch (Exception ex4) {
                        try {
                            return new URL(baseDirectory.toURL(), newURI.toString());
                        } catch (Exception ex5) {
                        }
                        IOException io = new IOException("Problem with URI format in '" + urlString + "'. " + "This can happen if the '" + urlString + "' is not absolute" + " and is not present relative to the directory" + " in which the specified model was read" + " (which was '" + baseDirectory + "')");
                        io.initCause(ex3);
                        throw io;
                    }
                }
            }
            URL url = new URL(name);
            try {
                String fixedURLAsString = url.toString().replaceFirst("(https?:)//?", "$1//");
                url = new URL(fixedURLAsString);
            } catch (Exception e) {
            }
            return url;
        }
    }

    /** Open the specified file for reading. If the specified name is
     *  "System.in", then a reader from standard in is returned. If
     *  the name begins with "$CLASSPATH" or "xxxxxxCLASSPATHxxxxxx",
     *  then the name is passed to {@link #nameToURL(String, URI, ClassLoader)}
     *  If the file name is not absolute, the it is assumed to be relative to
     *  the specified base URI.
     *  @see #nameToURL(String, URI, ClassLoader)
     *  @param name File name.
     *  @param base The base URI for relative references.
     *  @param classLoader The class loader to use to locate system
     *   resources, or null to use the system class loader that was used
     *   to load this class.
     *  @return If the name is null or the empty string,
     *  then null is returned, otherwise a buffered reader is returned.

     *  @exception IOException If the file cannot be opened.
     */
    public static BufferedReader openForReading(String name, URI base, ClassLoader classLoader) throws IOException {
        if ((name == null) || name.trim().equals("")) {
            return null;
        }
        if (name.trim().equals("System.in")) {
            if (STD_IN == null) {
                STD_IN = new BufferedReader(new InputStreamReader(System.in));
            }
            return STD_IN;
        }
        URL url = nameToURL(name, base, classLoader);
        if (url == null) {
            throw new IOException("Could not convert \"" + name + "\" with base \"" + base + "\" to a URL.");
        }
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(url.openStream());
        } catch (IOException ex) {
            try {
                URL possibleJarURL = ClassUtilities.jarURLEntryResource(url.toString());
                if (possibleJarURL != null) {
                    inputStreamReader = new InputStreamReader(possibleJarURL.openStream());
                }
                return new BufferedReader(inputStreamReader);
            } catch (Exception ex2) {
                try {
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                } catch (IOException ex3) {
                }
                IOException ioException = new IOException("Failed to open \"" + url + "\".");
                ioException.initCause(ex);
                throw ioException;
            }
        }
        return new BufferedReader(inputStreamReader);
    }

    /** Open the specified file for writing or appending. If the
     *  specified name is "System.out", then a writer to standard out
     *  is returned; otherwise, pass the name and base to {@link
     *  #nameToFile(String, URI)} and create a file writer.  If the
     *  file does not exist, then create it.  If the file name is not
     *  absolute, the it is assumed to be relative to the specified
     *  base directory.  If permitted, this method will return a
     *  Writer that will simply overwrite the contents of the file. It
     *  is up to the user of this method to check whether this is OK
     *  (by first calling {@link #nameToFile(String, URI)} and calling
     *  exists() on the returned value).
     *
     *  @param name File name.
     *  @param base The base URI for relative references.
     *  @param append If true, then append to the file rather than
     *   overwriting.
     *  @return If the name is null or the empty string,
     *  then null is returned, otherwise a writer is returned.
     *  @exception IOException If the file cannot be opened
     *   or created.
     */
    public static Writer openForWriting(String name, URI base, boolean append) throws IOException {
        if ((name == null) || name.trim().equals("")) {
            return null;
        }
        if (name.trim().equals("System.out")) {
            if (STD_OUT == null) {
                STD_OUT = new PrintWriter(System.out);
            }
            return STD_OUT;
        }
        File file = nameToFile(name, base);
        return new FileWriter(file, append);
    }

    /** Standard in as a reader, which will be non-null
     *  only after a call to openForReading("System.in").
     */
    public static BufferedReader STD_IN = null;

    /** Standard out as a writer, which will be non-null
     *  only after a call to openForWriting("System.out").
     */
    public static PrintWriter STD_OUT = null;

    /** Copy files safely.  If there are problems, the streams are
     *  close appropriately.
     *  @param inputStream The input stream.
     *  @param destinationFile The destination File.
     *  @exception IOException If the input stream cannot be created
     *  or read, or * if there is a problem writing to the destination
     *  file.
     */
    private static void _binaryCopyStream(InputStream inputStream, File destinationFile) throws IOException {
        BufferedInputStream input = null;
        try {
            input = new BufferedInputStream(inputStream);
            BufferedOutputStream output = null;
            try {
                File parent = destinationFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    if (!parent.mkdirs()) {
                        throw new IOException("Failed to create directories " + "for \"" + parent + "\".");
                    }
                }
                output = new BufferedOutputStream(new FileOutputStream(destinationFile));
                int c;
                while ((c = input.read()) != -1) {
                    output.write(c);
                }
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                }
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        }
    }

    /** Tag value used by this class and registered as a parser
     *  constant for the identifier "CLASSPATH" to indicate searching
     *  in the classpath.  This is a hack, but it deals with the fact
     *  that Java is not symmetric in how it deals with getting files
     *  from the classpath (using getResource) and getting files from
     *  the file system.
     */
    private static String _CLASSPATH_VALUE = "xxxxxxCLASSPATHxxxxxx";
}

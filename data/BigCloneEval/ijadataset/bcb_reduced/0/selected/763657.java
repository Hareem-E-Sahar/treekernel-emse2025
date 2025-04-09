package supersync.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.jdesktop.application.ResourceMap;
import supersync.sync.prefs.PasswordManager;
import supersync.sync.prefs.SystemSetup;

/** This is an abstract class representing a file system.
 *
 * @author Brandon Drake
 */
public abstract class AbstractFileSystem {

    protected static final String CIPHER_SALT = "3Jq2mz0HV";

    private static final ResourceMap resMap = org.jdesktop.application.Application.getInstance(supersync.SynchronizerApp.class).getContext().getResourceMap(AbstractFileSystem.class);

    protected boolean loggedIn = false;

    protected final SystemSetup systemSetup;

    protected AbstractFileSystem(SystemSetup l_systemSetup) {
        this.systemSetup = l_systemSetup;
    }

    /** Adds the child path to the relative path.  The child path (normally a file name).  The child path should not start with the folder separator char.
     */
    public static String addToRelativePath(String l_relativePath, String l_child, char folderSeparatorChar) {
        return l_relativePath + (l_relativePath.endsWith(String.valueOf(folderSeparatorChar)) ? "" : folderSeparatorChar) + l_child;
    }

    @Override
    public abstract boolean equals(Object o);

    /** Gets the absolute path from the relative path and base file location.
     */
    public String getAbsolutePath(String l_fileLocation, String l_relativeTo) {
        String pathSeparator = String.valueOf(this.getFolderSeparatorChar());
        String relativePath = l_fileLocation;
        if (relativePath.startsWith(".")) {
            relativePath = relativePath.substring(1);
        }
        if (relativePath.startsWith(pathSeparator) || relativePath.startsWith("/") || relativePath.startsWith("\\")) {
            relativePath = relativePath.substring(1);
        }
        String path = l_relativeTo;
        if (false == relativePath.isEmpty() && false == l_relativeTo.endsWith(pathSeparator)) {
            path += pathSeparator + relativePath;
        }
        return path;
    }

    /** Gets a cipher to use for encryption/decryption.
     */
    protected Cipher getCryptCipher(int l_cryptMode) throws IOException {
        if (false == this.loggedIn && false == this.login()) {
            throw new IOException(resMap.getString("message.userCanceledLogin.text"));
        }
        String password = this.systemSetup.getPassword();
        Cipher cipher = null;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), CIPHER_SALT.getBytes(), 1024, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(l_cryptMode, secret);
        } catch (Exception ex) {
            throw new IOException(resMap.getString("message.couldNotInitializeEncryptor.text"));
        }
        return cipher;
    }

    /** Gets the default directory for the file system.  This function may return null if you are not logged in but otherwise will always return a file.
     */
    public abstract AbstractFile getDefaultDirectory();

    /** Gets the file at the specified location.  This should not throw an error unless the specified file name is invalid.
     */
    public abstract AbstractFile getFile(String l_fileLocation);

    /** Gets a relative file location.
     */
    public AbstractFile getFile(String l_fileLocation, String l_relativeTo) {
        return getFile(getAbsolutePath(l_fileLocation, l_relativeTo));
    }

    public abstract char getFolderSeparatorChar();

    /** Returns true if you are logged in to the file system.
     */
    public boolean getLoggedIn() {
        return this.loggedIn;
    }

    /** Gets the roots of the file system.  Note that some file systems such as Windows will have multiple roots.
     */
    public abstract AbstractFile[] getRoots();

    /** Determines if the file is a relative path location.
     */
    public boolean isRelativePath(String l_fileLocation) {
        return l_fileLocation.startsWith("./") || l_fileLocation.startsWith(".\\");
    }

    /** Logs in to the file system.  Returns true if successfully logged in or false if the user canceled the login.
     *
     * If a password is required, the user will be prompted.
     */
    public boolean login() throws IOException {
        return login(null);
    }

    /** Logs in to the file system.  Returns true if successfully logged in or false if the user canceled the login.
     *
     * This will try to use the password manager to decrypt any passwords if necessary.  If that fails the user will be prompted for their password.
     *
     * @param l_passwordManager: A password manager that can be used to decrypt passwords used for login.  If this is null, a password manager will not be used.
     */
    public boolean login(PasswordManager l_passwordManager) throws IOException {
        if (this.systemSetup.getEncrypt() && null == this.systemSetup.getPassword(l_passwordManager)) {
            if (null == this.systemSetup.getPasswordFromUser(resMap.getString("message.enterPasswordForSystem.text", this.systemSetup.getName()))) {
                return false;
            }
        }
        this.loggedIn = true;
        return true;
    }

    /** Logs out of the file system.
     */
    public void logout() throws IOException {
        this.loggedIn = false;
    }

    /** This function is used to wrap an input stream in an encryption stream and/or compression stream if that is required by the system settings.
     */
    public InputStream wrapInputStream(InputStream l_stream) throws IOException {
        InputStream result = l_stream;
        if (this.systemSetup.getEncrypt()) {
            result = this.wrapInDecryptorStream(result);
        }
        if (this.systemSetup.getCompress()) {
            result = wrapInDecompressorStream(result);
        }
        return result;
    }

    /** This function is used to wrap an output stream in a compression stream.
     */
    public static OutputStream wrapInCompressorStream(OutputStream l_outStream, String l_fileName) throws IOException {
        java.util.zip.ZipOutputStream result = new java.util.zip.ZipOutputStream(l_outStream);
        result.putNextEntry(new java.util.zip.ZipEntry(l_fileName));
        return result;
    }

    /** This function is used to wrap an input stream in a decompression stream.
     */
    public static InputStream wrapInDecompressorStream(InputStream l_inStream) throws IOException {
        java.util.zip.ZipInputStream result = new java.util.zip.ZipInputStream(l_inStream);
        result.getNextEntry();
        return result;
    }

    /** This function is used to wrap an input stream in a decryption stream.
     */
    public InputStream wrapInDecryptorStream(InputStream l_inStream) throws IOException {
        return new javax.crypto.CipherInputStream(l_inStream, getCryptCipher(Cipher.DECRYPT_MODE));
    }

    /** This function is used to wrap an input stream in an encryption stream.
     */
    public OutputStream wrapInEncryptorStream(OutputStream l_outStream) throws IOException {
        return new javax.crypto.CipherOutputStream(l_outStream, getCryptCipher(Cipher.ENCRYPT_MODE));
    }

    /** Wraps the output stream in an encryption and/or encryption stream if needed based on the system setup.
     */
    public OutputStream wrapOutputStream(OutputStream l_out, String l_fileName) throws IOException {
        OutputStream result = l_out;
        if (this.systemSetup.getEncrypt()) {
            result = this.wrapInEncryptorStream(result);
        }
        if (this.systemSetup.getCompress()) {
            result = wrapInCompressorStream(result, l_fileName);
        }
        return result;
    }
}

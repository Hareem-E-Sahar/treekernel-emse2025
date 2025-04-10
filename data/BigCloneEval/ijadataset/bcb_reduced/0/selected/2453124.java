package decodepcode.svn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Logger;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import decodepcode.ContainerProcessor;
import decodepcode.PToolsObjectToFileMapper;
import decodepcode.PeopleCodeParser;
import decodepcode.ProjectReader;
import decodepcode.SQLobject;

/**
 * 
 * Uses the SVNKit library to commit .pcode and .sql files to Subversion.
 *
 */
public class SubversionSubmitter {

    static Logger logger = Logger.getLogger(SubversionSubmitter.class.getName());

    private static void addDirPath(SVNRepository repository, String dirPath) throws SVNException {
        logger.fine("addDirPath: " + dirPath);
        if (dirPath.endsWith("/")) dirPath = dirPath.substring(0, dirPath.length() - 1);
        if (!(dirPath.startsWith("/trunk") || dirPath.startsWith("/tags") || dirPath.startsWith("/branches"))) throw new IllegalArgumentException("Expected absolute path (/trunk, /tags or /branches); got " + dirPath);
        SVNNodeKind nodeKind = repository.checkPath(dirPath, -1);
        if (nodeKind == SVNNodeKind.DIR) {
            return;
        } else if (nodeKind == SVNNodeKind.FILE) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "Entry at URL ''{0}'' is a file while directory was expected", dirPath);
            throw new SVNException(err);
        }
        int lastSlash = dirPath.lastIndexOf("/");
        if (lastSlash < 1) return;
        String parentDir = dirPath.substring(0, lastSlash);
        addDirPath(repository, parentDir);
        ISVNEditor editor = repository.getCommitEditor("create path '" + dirPath + "'", null);
        editor.openRoot(-1);
        String curPath = "";
        String[] dirs = dirPath.split("/");
        for (int i = 0; i < dirs.length - 1; i++) if (dirs[i] != null && dirs[i].length() > 0) {
            curPath += "/" + dirs[i];
            logger.fine("Opening " + curPath);
            editor.openDir(curPath, -1);
        }
        logger.fine("Now calling editor.addDir(" + dirPath + ", ...)");
        editor.addDir(dirPath, null, -1);
        for (int i = 0; i < dirs.length; i++) if (dirs[i] != null && dirs[i].length() > 0) editor.closeDir();
        editor.closeEdit();
    }

    private static void addFile(SVNRepository repository, String filePath, String commitStr, byte[] data) throws SVNException {
        int lastSlash = filePath.lastIndexOf("/");
        if (lastSlash < 1) throw new IllegalArgumentException("Expected file name with directory path, got " + filePath);
        String dirPath = filePath.substring(0, lastSlash);
        addDirPath(repository, dirPath);
        SVNNodeKind nodeKind = repository.checkPath(filePath, -1);
        if (nodeKind == SVNNodeKind.DIR) throw new SVNException(SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "Entry at URL ''{0}'' is a directory while a file was expected", filePath));
        boolean doesNotExist = nodeKind == SVNNodeKind.NONE;
        ISVNEditor editor;
        if (doesNotExist) {
            editor = repository.getCommitEditor(commitStr, null);
            editor.openRoot(-1);
            String[] dirs = dirPath.split("/");
            String curPath = "";
            for (int i = 0; i < dirs.length; i++) if (dirs[i] != null && dirs[i].length() > 0) {
                curPath += "/" + dirs[i];
                editor.openDir(curPath, -1);
            }
            logger.info("Creating file " + filePath);
            editor.addFile(filePath, null, -1);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SVNProperties fileProperties = new SVNProperties();
            repository.getFile(filePath, -1, fileProperties, baos);
            String remoteChecksum = fileProperties.getStringValue("svn:entry:checksum");
            try {
                MessageDigest complete = MessageDigest.getInstance("MD5");
                complete.update(data, 0, data.length);
                byte[] checkSumBytes = complete.digest();
                String checkSum = "";
                for (int i = 0; i < checkSumBytes.length; i++) checkSum += Integer.toString((checkSumBytes[i] & 0xff) + 0x100, 16).substring(1);
                logger.fine("Remote MD5: " + remoteChecksum);
                logger.fine("New    MD5: " + checkSum);
                if (remoteChecksum.equals(checkSum)) {
                    logger.info("New text identical to remote SVN file - not committing " + filePath);
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
            }
            editor = repository.getCommitEditor(commitStr, null);
            editor.openRoot(-1);
            logger.info("Updating file " + filePath);
            String[] dirs = dirPath.split("/");
            String curPath = "";
            for (int i = 0; i < dirs.length; i++) if (dirs[i] != null && dirs[i].length() > 0) {
                curPath += "/" + dirs[i];
                editor.openDir(curPath, -1);
            }
            editor.openFile(filePath, -1);
        }
        editor.applyTextDelta(filePath, null);
        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
        String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(data), editor, true);
        editor.closeFile(filePath, checksum);
        editor.closeDir();
        editor.closeEdit();
    }

    static interface AuthManagerMapper {

        ISVNAuthenticationManager getAuthManager(String userName);
    }

    static class FixedAuthManagerMapper implements AuthManagerMapper {

        HashMap<String, ISVNAuthenticationManager> map = new HashMap<String, ISVNAuthenticationManager>();

        ISVNAuthenticationManager defaultCredentials;

        public ISVNAuthenticationManager getAuthManager(String userName) {
            if (userName == null) return defaultCredentials;
            ISVNAuthenticationManager m = map.get(userName.trim());
            return m == null ? defaultCredentials : m;
        }

        void addCredentials(String pToolUserName, String svnUserName, String svnPassword) {
            ISVNAuthenticationManager m = SVNWCUtil.createDefaultAuthenticationManager(svnUserName.trim(), svnPassword);
            if (map.size() == 0) defaultCredentials = m;
            if (pToolUserName != null) map.put(pToolUserName.trim(), m);
        }
    }

    public static class SubversionContainerProcessor extends ContainerProcessor {

        SVNRepository repository;

        String basePath;

        PToolsObjectToFileMapper mapper;

        PeopleCodeParser parser = new PeopleCodeParser();

        AuthManagerMapper authMapper;

        SubversionContainerProcessor(SVNURL url, String _basePath, PToolsObjectToFileMapper _mapper, AuthManagerMapper _authMapper) throws SVNException {
            repository = SVNRepositoryFactory.create(url);
            basePath = _basePath;
            mapper = _mapper;
            authMapper = _authMapper;
            System.out.println("Submitting PeopleCode and SQL definitions to " + url + basePath);
        }

        public void process(decodepcode.PeopleCodeObject c) throws IOException {
            StringWriter w = new StringWriter();
            if (c.hasPlainPeopleCode()) w.write(c.getPeopleCodeText()); else {
                parser.parse(((decodepcode.PeopleCodeContainer) c), w);
            }
            String path = basePath + mapper.getPath(c, "pcode");
            try {
                ISVNAuthenticationManager user = authMapper.getAuthManager(c.getLastChangedBy());
                if (user != null) {
                    logger.info("setting mapped AuthManager for user " + c.getLastChangedBy());
                    repository.setAuthenticationManager(user);
                }
                addFile(repository, path, "Saved at " + ProjectReader.df2.format(c.getLastChangedDtTm()) + " by " + c.getLastChangedBy() + (c.getSource() == null ? "" : " (source: " + c.getSource() + ")"), w.toString().getBytes());
            } catch (SVNException se) {
                IOException e = new IOException("Error submitting pcode to Subversion");
                e.initCause(se);
                throw e;
            }
        }

        public void processSQL(SQLobject sql) throws IOException {
            String path = basePath + mapper.getPathForSQL(sql, "sql");
            try {
                ISVNAuthenticationManager user = authMapper.getAuthManager(sql.getLastChangedBy());
                if (user != null) {
                    logger.info("Setting mapped AuthManager for user " + sql.getLastChangedBy());
                    repository.setAuthenticationManager(user);
                }
                if (sql.getLastChangedDtTm() != null && sql.getLastChangedBy() != null) addFile(repository, path, "Saved at " + ProjectReader.df2.format(sql.getLastChangedDtTm()) + " by " + sql.getLastChangedBy(), sql.getSql().getBytes());
            } catch (SVNException se) {
                IOException e = new IOException("Error submitting pcode to Subversion");
                e.initCause(se);
                throw e;
            }
        }

        @Override
        public void aboutToProcess() {
            System.out.println("Submitting to SVN, base path = " + basePath);
        }
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        try {
            ISVNAuthenticationManager m = SVNWCUtil.createDefaultAuthenticationManager("harry", "secret");
            SVNURL url = SVNURL.parseURIEncoded("svn://192.168.1.5/project1");
            SVNRepository repository = SVNRepositoryFactory.create(url);
            repository.setAuthenticationManager(m);
            addDirPath(repository, "/trunk/x/y/z");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setUpSVNKit() {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
    }

    static {
        setUpSVNKit();
    }
}

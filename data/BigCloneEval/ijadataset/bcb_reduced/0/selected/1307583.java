package securus.action.remoteaccess;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.commons.lang.mutable.MutableInt;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.io.SVNRepository;
import securus.FacesSupport;
import securus.UserFormException;
import securus.entity.User;
import securus.services.FileSendingService;
import securus.services.SVNService;
import securus.services.SecurusException;
import securus.services.StatusMessage;
import securus.services.FileSendingService.FileSystemItem;

/**
 * @author e.dorofeev
 */
@Name("utils")
@Scope(ScopeType.CONVERSATION)
public class UtilBean implements Serializable {

    private static final long serialVersionUID = 4924177977835224177L;

    @In(required = true)
    private EntityManager entityManager;

    @In(required = true)
    private FileSendingService fileSendingService;

    public static class ClientFileSystemItem extends FileSystemItem {

        private final FileSystemNode node;

        ClientFileSystemItem(FileSystemNode node) {
            super(node.getName(), node.isFolder());
            this.node = node;
        }

        @Override
        public InputStream getContent() {
            try {
                return node.getContent();
            } catch (Exception e) {
                throw new SecurusException(e);
            }
        }
    }

    public List<StatusMessage> sendToEmail(List<FileSystemNode> nodes, String to, String cc, String bcc, String subject, String body) throws UserFormException, IOException {
        final MutableInt size = new MutableInt(0);
        List<FileSystemItem> files = createFileList(null, nodes, null, size);
        if (files.isEmpty()) {
            throw new UserFormException("No files selected");
        }
        return fileSendingService.sendToEmail(files, to, cc, bcc, subject, body);
    }

    private static final int MAX_EMAIL_SIZE = 10 * 1024 * 1024;

    private List<FileSystemItem> createFileList(String dir, List<FileSystemNode> nodes, List<FileSystemItem> list, MutableInt size) throws UserFormException {
        if (list == null) {
            list = new ArrayList<FileSystemItem>();
        }
        for (FileSystemNode node : nodes) {
            String fileName = node.getFileName();
            if (node.isFolder()) {
                createFileList(fileName, node.getFileNodes(), list, size);
            } else {
                String relativePath = (dir == null) ? fileName : (dir + "/" + fileName);
                size.add(node.getDirEntry().getSize());
                FileSystemItem item = new ClientFileSystemItem(node);
                item.setFile(relativePath);
                item.setFolder(node.isFolder());
                list.add(item);
            }
            if (size.intValue() >= MAX_EMAIL_SIZE) {
                throw new UserFormException("sendBtn", "too_heavy_to_send");
            }
        }
        return list;
    }

    public void deleteFiles(FileSystemNode dir, List<FileSystemNode> nodes) throws MalformedURLException, SVNClientException {
        SVNUrl[] urls = new SVNUrl[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            deleteShare(nodes.get(i));
            urls[i] = new SVNUrl(nodes.get(i).getPath());
        }
        dir.getSvnAdapter().remove(urls, SVNRevision.HEAD.toString());
    }

    private void deleteShare(FileSystemNode node) {
        Query q = entityManager.createQuery("delete from SharedFile t where " + "device=:device and path like :path");
        q.setParameter("device", node.getDevice());
        String path = node.getFile().replace("_", "\\_").replace("%", "\\%");
        if (node.isFolder()) {
            q.setParameter("path", path + "%");
        } else {
            q.setParameter("path", path);
        }
        q.executeUpdate();
    }

    private User findUserByEmail(String email) {
        Query q = entityManager.createQuery("from User u where u.email = :email");
        q.setParameter("email", email);
        try {
            return (User) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean checkSharedEmail(String email, List<ShareAction.User> users, List<FileSystemNode> selectedNodes, String key) throws UserFormException {
        for (ShareAction.User u : users) {
            if (u.getEmail().equals(email)) {
                throw new UserFormException(key, "address_already_in_list");
            }
        }
        if (!FacesSupport.validateEmail(email)) {
            throw new UserFormException(key, "incorrect_email_format");
        }
        String host = email.substring(email.indexOf('@') + 1);
        try {
            InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new UserFormException(key, "unknown_host");
        }
        for (FileSystemNode n : selectedNodes) {
            for (SharedTo st : n.getSharedTo()) {
                if (st.getEmail().equals(email)) {
                    throw new UserFormException(key, "duplicate_share");
                }
            }
        }
        return findUserByEmail(email) != null;
    }

    /**
	 * Achieves the list of files recursively.
	 * 
	 * @param files
	 * @param out
	 * @throws Exception
	 */
    public static void archieveFiles(List<FileSystemNode> files, ZipOutputStream out, String pathFromRoot) throws Exception {
        byte[] buffer = new byte[4096];
        for (FileSystemNode file : files) {
            if (file.isFolder()) {
                archieveFiles(file.getFileNodes(), out, pathFromRoot + file.getName() + "/");
                continue;
            }
            out.putNextEntry(new ZipEntry(pathFromRoot + file.getName()));
            InputStream in = new BufferedInputStream(file.getContent());
            for (int length = 0; (length = in.read(buffer)) > 0; ) {
                out.write(buffer, 0, length);
            }
            out.closeEntry();
            in.close();
        }
    }

    /**
	 * Returns the map of most recent deleted item paths and corresponding
	 * revisions
	 * 
	 * @param user
	 *            - the owner of the deleted items
	 * @param path
	 *            - the path to be searched for deleted items
	 * @return
	 */
    @SuppressWarnings("unchecked")
    public static synchronized Map<String, Long> getDeletedItems(User user, String path) {
        Map<String, Long> deletedItems = new HashMap<String, Long>();
        try {
            SVNRepository repository = SVNService.getRepository(user, path);
            Collection<SVNLogEntry> entries = repository.log(new String[0], null, 0, -1, true, true);
            for (SVNLogEntry svnLogEntry : entries) {
                Set<Entry<String, SVNLogEntryPath>> changedPathsEntrySet = svnLogEntry.getChangedPaths().entrySet();
                for (Entry<String, SVNLogEntryPath> e : changedPathsEntrySet) {
                    SVNLogEntryPath svnLogEntryPath = e.getValue();
                    String itemPath = svnLogEntryPath.getPath();
                    char itemAction = svnLogEntryPath.getType();
                    if (itemAction == 'D') {
                        long itemRevision = svnLogEntry.getRevision() - 1;
                        Long deletedRevision = deletedItems.get(itemPath);
                        if (deletedRevision == null) {
                            deletedItems.put(itemPath, itemRevision);
                        } else {
                            if (deletedRevision.longValue() < itemRevision) {
                                deletedItems.put(itemPath, Long.valueOf(itemRevision));
                            }
                        }
                    }
                }
            }
        } catch (SVNException e) {
            e.printStackTrace();
        }
        return deletedItems;
    }

    @SuppressWarnings("unchecked")
    public static synchronized Map<String, Long> getDeletedFolderDeletedItems(User user, String path, long revision) {
        Map<String, Long> deletedItems = new HashMap<String, Long>();
        try {
            SVNRepository repository = SVNService.getRepository(user, path);
            Collection<SVNLogEntry> entries = repository.log(new String[0], null, 0, revision, true, true);
            for (SVNLogEntry svnLogEntry : entries) {
                Set<Entry<String, SVNLogEntryPath>> changedPathsEntrySet = svnLogEntry.getChangedPaths().entrySet();
                for (Entry<String, SVNLogEntryPath> e : changedPathsEntrySet) {
                    SVNLogEntryPath svnLogEntryPath = e.getValue();
                    String itemPath = svnLogEntryPath.getPath();
                    char itemAction = svnLogEntryPath.getType();
                    long itemRevision = svnLogEntry.getRevision();
                    if (itemAction == 'D') {
                        itemRevision -= 1;
                    }
                    Long deletedRevision = deletedItems.get(itemPath);
                    if (deletedRevision == null) {
                        deletedItems.put(itemPath, itemRevision);
                    } else {
                        if (deletedRevision.longValue() < itemRevision) {
                            deletedItems.put(itemPath, Long.valueOf(itemRevision));
                        }
                    }
                }
            }
        } catch (SVNException e) {
            throw new IllegalStateException(e);
        }
        return deletedItems;
    }
}

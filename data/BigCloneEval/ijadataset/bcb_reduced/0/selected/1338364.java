package de.spotnik.gpl.provider.maildir;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.FolderEvent;
import javax.mail.internet.MimeMessage;

/**
 * The folder class implementing a Maildir-format mailbox.
 * 
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class MaildirFolder extends Folder {

    /**
     * Singleton instance of filter.
     */
    static final FilenameFilter filter = new MaildirFilter();

    static final String INBOX = "INBOX";

    /**
     * The maildir base directory.
     */
    File maildir;

    /**
     * The maildir <code>tmp</code> directory.
     */
    File tmpdir;

    /**
     * The maildir <code>new</code> directory.
     */
    MaildirTuple newdir;

    /**
     * The maildir <code>cur</code> directory.
     */
    MaildirTuple curdir;

    int type;

    boolean inbox;

    static Flags permanentFlags = new Flags();

    static long deliveryCount = 0;

    /**
     * Constructor.
     * 
     * @param store
     * @param filename
     * @param root
     * @param inbox
     */
    protected MaildirFolder(Store store, String filename, boolean root, boolean inbox) {
        super(store);
        this.maildir = new File(filename);
        this.tmpdir = new File(this.maildir, "tmp");
        this.newdir = new MaildirTuple(new File(this.maildir, "new"));
        this.curdir = new MaildirTuple(new File(this.maildir, "cur"));
        this.mode = -1;
        this.type = root ? HOLDS_FOLDERS : HOLDS_MESSAGES;
        this.inbox = inbox;
    }

    /**
     * Constructor.
     * 
     * @param store
     * @param filename
     */
    protected MaildirFolder(Store store, String filename) {
        this(store, filename, false, false);
    }

    /**
     * Returns the name of this folder.
     */
    @Override
    public String getName() {
        if (this.inbox) {
            return INBOX;
        }
        return this.maildir.getName();
    }

    /**
     * Returns the full name of this folder.
     */
    @Override
    public String getFullName() {
        if (this.inbox) {
            return INBOX;
        }
        return this.maildir.getPath();
    }

    /**
     * Return a URLName representing this folder.
     */
    @Override
    public URLName getURLName() throws MessagingException {
        URLName url = super.getURLName();
        return new URLName(url.getProtocol(), null, -1, url.getFile(), null, null);
    }

    /**
     * Returns the type of this folder.
     * 
     * @exception MessagingException if a messaging error occurred
     */
    @SuppressWarnings("unused")
    @Override
    public int getType() throws MessagingException {
        return this.type;
    }

    /**
     * Indicates whether this folder exists.
     * 
     * @exception MessagingException if a messaging error occurred
     */
    @SuppressWarnings("unused")
    @Override
    public boolean exists() throws MessagingException {
        return this.maildir.exists();
    }

    /**
     * Indicates whether this folder contains new messages.
     * 
     * @exception MessagingException if a messaging error occurred
     */
    @Override
    public boolean hasNewMessages() throws MessagingException {
        return getNewMessageCount() > 0;
    }

    /**
     * Opens this folder. If the folder is opened for writing, a lock must be
     * acquired on the mbox. If this fails a MessagingException is thrown.
     * 
     * @exception MessagingException if a messaging error occurred
     */
    @Override
    public void open(int mode) throws MessagingException {
        if (this.mode != -1) {
            throw new IllegalStateException("Folder is open");
        }
        if (!this.maildir.exists() || !this.maildir.canRead()) {
            throw new FolderNotFoundException(this);
        }
        boolean success = true;
        if (!this.tmpdir.exists()) {
            success = success && this.tmpdir.mkdirs();
        }
        if (!this.newdir.dir.exists()) {
            success = success && this.newdir.dir.mkdirs();
        }
        if (!this.curdir.dir.exists()) {
            success = success && this.curdir.dir.mkdirs();
        }
        if (!success) {
            throw new MessagingException("Unable to create directories");
        }
        if (mode == READ_WRITE) {
            if (!this.maildir.canWrite()) {
                throw new MessagingException("Folder is read-only");
            }
        }
        this.mode = mode;
        notifyConnectionListeners(ConnectionEvent.OPENED);
    }

    /**
     * Closes this folder.
     * 
     * @param expunge if the folder is to be expunged before it is closed
     * @exception MessagingException if a messaging error occurred
     */
    @Override
    public void close(boolean expunge) throws MessagingException {
        if (this.mode == -1) {
            throw new IllegalStateException("Folder is closed");
        }
        if (expunge) {
            expunge();
        }
        this.mode = -1;
        notifyConnectionListeners(ConnectionEvent.CLOSED);
    }

    /**
     * Expunges this folder. This deletes all the messages marked as deleted.
     * 
     * @exception MessagingException if a messaging error occurred
     */
    @Override
    public Message[] expunge() throws MessagingException {
        if (this.mode == -1) {
            throw new IllegalStateException("Folder is closed");
        }
        if (!exists()) {
            throw new FolderNotFoundException(this);
        }
        if (this.mode == Folder.READ_ONLY) {
            throw new IllegalWriteException();
        }
        Message[] expunged;
        synchronized (this) {
            List elist = new ArrayList();
            try {
                if (this.newdir.messages != null) {
                    int len = this.newdir.messages.length;
                    for (int i = 0; i < len; i++) {
                        MaildirMessage message = this.newdir.messages[i];
                        if (message.getFlags().contains(Flags.Flag.DELETED)) {
                            message.file.delete();
                            elist.add(message);
                        }
                    }
                }
                if (this.curdir.messages != null) {
                    int len = this.curdir.messages.length;
                    for (int i = 0; i < len; i++) {
                        MaildirMessage message = this.curdir.messages[i];
                        if (message.getFlags().contains(Flags.Flag.DELETED)) {
                            message.file.delete();
                            elist.add(message);
                        }
                    }
                }
            } catch (SecurityException e) {
                throw new IllegalWriteException(e.getMessage());
            }
            expunged = new Message[elist.size()];
            elist.toArray(expunged);
        }
        if (expunged.length > 0) {
            notifyMessageRemovedListeners(true, expunged);
        }
        return expunged;
    }

    /**
     * Indicates whether this folder is open.
     */
    @Override
    public boolean isOpen() {
        return this.mode != -1;
    }

    /**
     * Returns the permanent flags for this folder.
     */
    @Override
    public Flags getPermanentFlags() {
        return permanentFlags;
    }

    /**
     * Returns the number of messages in this folder.
     * 
     * @exception MessagingException if a messaging error occurred
     */
    @Override
    public synchronized int getMessageCount() throws MessagingException {
        statDir(this.curdir);
        statDir(this.newdir);
        return this.curdir.messages.length + this.newdir.messages.length;
    }

    /**
     * Returns the number of new messages in this folder.
     * 
     * @exception MessagingException if a messaging error occurred
     */
    @Override
    public synchronized int getNewMessageCount() throws MessagingException {
        statDir(this.newdir);
        return this.newdir.messages.length;
    }

    /**
     * Returns the specified message number from this folder.
     * 
     * @exception MessagingException if a messaging error occurred
     */
    @Override
    public synchronized Message getMessage(int msgnum) throws MessagingException {
        statDir(this.curdir);
        statDir(this.newdir);
        int clen = this.curdir.messages.length;
        int alen = clen + this.newdir.messages.length;
        int index = msgnum - 1;
        if (index < 0 || index >= alen) {
            throw new MessagingException("No such message: " + msgnum);
        }
        if (index < clen) {
            return this.curdir.messages[index];
        } else {
            return this.newdir.messages[index - clen];
        }
    }

    /**
     * Returns the messages in this folder.
     * 
     * @exception MessagingException if a messaging error occurred
     */
    @Override
    public synchronized Message[] getMessages() throws MessagingException {
        statDir(this.curdir);
        statDir(this.newdir);
        int clen = this.curdir.messages.length;
        int nlen = this.newdir.messages.length;
        int alen = clen + nlen;
        Message[] m = new Message[alen];
        System.arraycopy(this.curdir.messages, 0, m, 0, clen);
        System.arraycopy(this.newdir.messages, 0, m, clen, nlen);
        return m;
    }

    /**
     * Check the specified directory for messages, repopulating its
     * <code>messages</code> member if necessary, and updating its timestamp.
     */
    void statDir(MaildirTuple dir) throws MessagingException {
        long timestamp = dir.dir.lastModified();
        if (timestamp == dir.timestamp) {
            return;
        }
        File[] files = dir.dir.listFiles(filter);
        int mlen = files.length;
        dir.messages = new MaildirMessage[mlen];
        for (int i = 0; i < mlen; i++) {
            File file = files[i];
            String uniq = file.getName();
            String info = null;
            int ci = uniq.indexOf(':');
            if (ci != -1) {
                info = uniq.substring(ci + 1);
                uniq = uniq.substring(0, ci);
            }
            dir.messages[i] = new MaildirMessage(this, file, uniq, info, i + 1);
        }
        dir.timestamp = timestamp;
    }

    /**
     * Move the specified message between new and cur, depending on whether it
     * has been seen or not.
     */
    void setSeen(MaildirMessage message, boolean seen) throws MessagingException {
        File src = message.file;
        File dst = null;
        if (seen) {
            String dstname = new StringBuffer(message.uniq).append(':').append(message.getInfo()).toString();
            dst = new File(this.curdir.dir, dstname);
        } else {
            dst = new File(this.newdir.dir, message.uniq);
        }
        if (!src.renameTo(dst)) {
            throw new MessagingException("Unable to move message");
        }
    }

    /**
     * Appends messages to this folder. Only MimeMessages within the array will
     * be appended, as we don't know how to retrieve internet content for other
     * kinds.
     * 
     * @param m an array of messages to be appended
     */
    @Override
    public synchronized void appendMessages(Message[] m) throws MessagingException {
        MaildirMessage[] n;
        synchronized (this) {
            statDir(this.newdir);
            statDir(this.curdir);
            int nlen = 0;
            int clen = 0;
            if (this.newdir.messages != null) nlen = this.newdir.messages.length;
            if (this.curdir.messages != null) clen = this.curdir.messages.length;
            List appended = new ArrayList(m.length);
            for (Message element : m) {
                if (element instanceof MimeMessage) {
                    MimeMessage src = (MimeMessage) element;
                    Flags flags = src.getFlags();
                    boolean seen = flags.contains(Flags.Flag.SEEN);
                    int count = seen ? ++clen : ++nlen;
                    try {
                        String uniq = createUniq();
                        String tmpname = uniq;
                        String info = null;
                        if (seen) {
                            info = MaildirMessage.getInfo(flags);
                            tmpname = new StringBuffer(uniq).append('_').append(info).toString();
                        }
                        File tmpfile = new File(this.tmpdir, tmpname);
                        long time = System.currentTimeMillis();
                        long timeout = time + 86400000L;
                        while (time < timeout) {
                            if (!tmpfile.exists()) {
                                break;
                            }
                            try {
                                wait(2000);
                            } catch (InterruptedException e) {
                            }
                            time = System.currentTimeMillis();
                        }
                        if (!tmpfile.createNewFile()) {
                            throw new MessagingException("Temporary file already exists");
                        }
                        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpfile));
                        src.writeTo(out);
                        out.close();
                        File file = new File(seen ? this.curdir.dir : this.newdir.dir, tmpname);
                        tmpfile.renameTo(file);
                        tmpfile.delete();
                        MaildirMessage dst = new MaildirMessage(this, file, uniq, info, count);
                        appended.add(dst);
                    } catch (IOException e) {
                        throw new MessagingException(e.getMessage(), e);
                    } catch (SecurityException e) {
                        throw new IllegalWriteException(e.getMessage());
                    }
                }
            }
            n = new MaildirMessage[appended.size()];
            if (n.length > 0) {
                appended.toArray(n);
            }
        }
        if (n.length > 0) {
            notifyMessageAddedListeners(n);
        }
    }

    /**
     * Create a unique filename.
     */
    @SuppressWarnings("unused")
    static String createUniq() throws MessagingException, IOException {
        long time = System.currentTimeMillis() / 1000L;
        long pid = 0;
        File urandom = new File("/dev/urandom");
        if (urandom.exists() && urandom.canRead()) {
            byte[] bytes = new byte[8];
            InputStream in = new FileInputStream(urandom);
            int offset = 0;
            while (offset < bytes.length) {
                offset += in.read(bytes, offset, bytes.length - offset);
            }
            in.close();
            for (int i = 0; i < bytes.length; i++) {
                pid |= bytes[i] * (long) Math.pow(i, 2);
            }
        } else {
            pid += ++deliveryCount;
        }
        String host = InetAddress.getLocalHost().getHostName();
        return new StringBuffer().append(time).append('.').append(pid).append('.').append(host).toString();
    }

    /**
     * Returns the parent folder.
     */
    @Override
    public Folder getParent() throws MessagingException {
        return this.store.getFolder(this.maildir.getParent());
    }

    /**
     * Returns the subfolders of this folder.
     */
    @Override
    public Folder[] list() throws MessagingException {
        if (this.type != HOLDS_FOLDERS) {
            throw new MessagingException("This folder can't contain subfolders");
        }
        try {
            String[] files = this.maildir.list();
            Folder[] folders = new Folder[files.length];
            for (int i = 0; i < files.length; i++) {
                folders[i] = this.store.getFolder(this.maildir.getAbsolutePath() + File.separator + files[i]);
            }
            return folders;
        } catch (SecurityException e) {
            throw new MessagingException("Access denied", e);
        }
    }

    /**
     * Returns the subfolders of this folder matching the specified pattern.
     */
    @Override
    public Folder[] list(String pattern) throws MessagingException {
        if (this.type != HOLDS_FOLDERS) {
            throw new MessagingException("This folder can't contain subfolders");
        }
        try {
            String[] files = this.maildir.list(new MaildirListFilter(pattern));
            Folder[] folders = new Folder[files.length];
            for (int i = 0; i < files.length; i++) {
                folders[i] = this.store.getFolder(this.maildir.getAbsolutePath() + File.separator + files[i]);
            }
            return folders;
        } catch (SecurityException e) {
            throw new MessagingException("Access denied", e);
        }
    }

    /**
     * Returns the separator character.
     */
    @SuppressWarnings("unused")
    @Override
    public char getSeparator() throws MessagingException {
        return File.separatorChar;
    }

    /**
     * Creates this folder in the store.
     */
    @Override
    public boolean create(int type) throws MessagingException {
        if (this.maildir.exists()) {
            throw new MessagingException("Folder already exists");
        }
        switch(type) {
            case HOLDS_FOLDERS:
                try {
                    if (!this.maildir.mkdirs()) {
                        return false;
                    }
                    this.type = type;
                    notifyFolderListeners(FolderEvent.CREATED);
                    return true;
                } catch (SecurityException e) {
                    throw new MessagingException("Access denied", e);
                }
            case HOLDS_MESSAGES:
                try {
                    boolean success = true;
                    synchronized (this) {
                        success = success && this.maildir.mkdirs();
                        success = success && this.tmpdir.mkdirs();
                        success = success && this.newdir.dir.mkdirs();
                        success = success && this.curdir.dir.mkdirs();
                    }
                    if (!success) {
                        return false;
                    }
                    this.type = type;
                    notifyFolderListeners(FolderEvent.CREATED);
                    return true;
                } catch (SecurityException e) {
                    throw new MessagingException("Access denied", e);
                }
        }
        return false;
    }

    /**
     * Deletes this folder.
     */
    @Override
    public boolean delete(boolean recurse) throws MessagingException {
        if (recurse) {
            try {
                if (this.type == HOLDS_FOLDERS) {
                    Folder[] folders = list();
                    for (Folder element : folders) {
                        if (!element.delete(recurse)) {
                            return false;
                        }
                    }
                }
                if (!delete(this.maildir)) {
                    return false;
                }
                notifyFolderListeners(FolderEvent.DELETED);
                return true;
            } catch (SecurityException e) {
                throw new MessagingException("Access denied", e);
            }
        } else {
            try {
                if (this.type == HOLDS_FOLDERS) {
                    Folder[] folders = list();
                    if (folders.length > 0) {
                        return false;
                    }
                }
                if (!delete(this.maildir)) {
                    return false;
                }
                notifyFolderListeners(FolderEvent.DELETED);
                return true;
            } catch (SecurityException e) {
                throw new MessagingException("Access denied", e);
            }
        }
    }

    /**
     * Depth-first file/directory delete.
     */
    boolean delete(File file) throws SecurityException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File element : files) {
                if (!delete(element)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    /**
     * Renames this folder.
     */
    @Override
    public boolean renameTo(Folder folder) throws MessagingException {
        try {
            String filename = folder.getFullName();
            if (filename != null) {
                if (!this.maildir.renameTo(new File(filename))) {
                    return false;
                }
                notifyFolderRenamedListeners(folder);
                return true;
            } else {
                throw new MessagingException("Illegal filename: null");
            }
        } catch (SecurityException e) {
            throw new MessagingException("Access denied", e);
        }
    }

    /**
     * Returns the subfolder of this folder with the specified name.
     */
    @Override
    public Folder getFolder(String filename) throws MessagingException {
        if (INBOX.equalsIgnoreCase(filename)) {
            try {
                return this.store.getFolder(INBOX);
            } catch (MessagingException e) {
            }
        }
        return this.store.getFolder(this.maildir.getAbsolutePath() + File.separator + filename);
    }

    /**
     * Filename filter that rejects dotfiles.
     */
    static class MaildirFilter implements FilenameFilter {

        @SuppressWarnings("unused")
        public boolean accept(File dir, String name) {
            return name.length() > 0 && name.charAt(0) != 0x2e;
        }
    }

    /**
     * Structure holding the details for a maildir subdirectory.
     */
    static class MaildirTuple {

        File dir;

        long timestamp = 0L;

        MaildirMessage[] messages = null;

        MaildirTuple(File dir) {
            this.dir = dir;
        }
    }

    /**
     * Filename filter for listing subfolders.
     */
    class MaildirListFilter implements FilenameFilter {

        String pattern;

        int asteriskIndex, percentIndex;

        MaildirListFilter(String pattern) {
            this.pattern = pattern;
            this.asteriskIndex = pattern.indexOf('*');
            this.percentIndex = pattern.indexOf('%');
        }

        public boolean accept(File directory, String name) {
            if (this.asteriskIndex > -1) {
                String start = this.pattern.substring(0, this.asteriskIndex);
                String end = this.pattern.substring(this.asteriskIndex + 1, this.pattern.length());
                return name.startsWith(start) && name.endsWith(end);
            } else if (this.percentIndex > -1) {
                String start = this.pattern.substring(0, this.percentIndex);
                String end = this.pattern.substring(this.percentIndex + 1, this.pattern.length());
                return directory.equals(MaildirFolder.this.maildir) && name.startsWith(start) && name.endsWith(end);
            }
            return name.equals(this.pattern);
        }
    }
}

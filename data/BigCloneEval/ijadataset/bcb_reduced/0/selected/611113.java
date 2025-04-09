package net.ukrpost.storage.maildir;

import net.ukrpost.utils.NewlineOutputStream;
import net.ukrpost.utils.QuotaAwareOutputStream;
import net.ukrpost.utils.QuotaExceededException;
import org.apache.log4j.Logger;
import javax.mail.*;
import javax.mail.event.FolderEvent;
import javax.mail.event.MessageChangedEvent;
import java.io.*;
import java.util.*;

/**
 * <b>Approach to external deliveries.</b>
 * <p/>
 * External deliveries are only detected in methods that are used to poll maildir:
 * <ul>
 * <li>getMessageCount</li>
 * <li>getNewMessageCount</li>
 * <li>getDeletedMessageCount</li>
 * <li>getUnreadMessageCount</li>
 * </ul>
 * Any other methods expect internal state to be equal to external state.
 * For folder to be aware of externaly delivered messages you should either close and reopen
 * MaildirFolder (slow on large Maildirs) or perform continious polling using methods specified above.
 */
public class MaildirFolder extends Folder implements UIDFolder {

    private static Logger log = Logger.getLogger(MaildirFolder.class);

    private static Flags supportedFlags = new Flags();

    static {
        supportedFlags.add(Flags.Flag.ANSWERED);
        supportedFlags.add(Flags.Flag.DELETED);
        supportedFlags.add(Flags.Flag.DRAFT);
        supportedFlags.add(Flags.Flag.FLAGGED);
        supportedFlags.add(Flags.Flag.RECENT);
        supportedFlags.add(Flags.Flag.SEEN);
    }

    private static final Message[] emptyMessageArray = new Message[] {};

    private File rootdir;

    private File dir;

    private File curd;

    private File newd;

    private File tmpd;

    long curLastModified = -1, newLastModified = -1;

    private String str;

    private String root;

    private boolean isdefault = false;

    private boolean isopen = false;

    private int unreadMessages = 0;

    private int recentMessages = 0;

    private int deletedMessages = 0;

    private ArrayList messages = null;

    private TreeMap uniqToMessageMap = new TreeMap();

    private Properties uids = new Properties();

    private int lastUid = 0;

    private long uidValidity = System.currentTimeMillis();

    public MaildirFolder(String s, MaildirStore store) {
        super(store);
        root = getStore().getURLName().getFile();
        if (!root.endsWith(File.separator)) {
            root += File.separator;
        }
        str = s.replace(File.separatorChar, '.');
        str = BASE64MailboxEncoder.encode(str);
        if (str.equals(".")) {
            isdefault = true;
        }
        if (str.toUpperCase().equals("INBOX")) {
            str = ".";
            isdefault = false;
        }
        if (!str.startsWith(".")) {
            str = "." + str;
        }
        rootdir = new File(root);
        dir = new File(root + str);
        curd = new File(dir, "cur");
        newd = new File(dir, "new");
        tmpd = new File(dir, "tmp");
    }

    private void updatemsgs() throws MessagingException {
        updatemsgs(true, false);
    }

    /**
     * Checks whether underlying folders "cur", "new" and "tmp" were modified.
     * Checks lastModified values for the folders and decides whether messages were addded or removed.
     *
     * @return modification state
     */
    private boolean isFoldersModified(LastModified lm) {
        long curLm = getCurDir().lastModified();
        long newLm = getNewDir().lastModified();
        if (curLm == lm.curLm && newLm == lm.newLm) {
            return false;
        }
        lm.curLm = curLm;
        lm.newLm = newLm;
        if (lm == updateLm) {
            newCountLm = new LastModified(updateLm);
            unreadCountLm = new LastModified(updateLm);
            deletedCountLm = new LastModified(updateLm);
            messageCountLm = new LastModified(updateLm);
        }
        return true;
    }

    /**
     * Updates message maps and counters: recentMessages, deletedMessages, unreadMessages based on flags set in MaildirFilename.
     */
    private MaildirMessage addMessage(MaildirFilename mfn) throws MessagingException {
        if (!mfn.getFlag(Flags.Flag.SEEN)) unreadMessages++;
        if (mfn.getFlag(Flags.Flag.DELETED)) deletedMessages++;
        if (!uids.containsKey(mfn.getUniq())) uids.setProperty(mfn.getUniq(), Integer.toString(lastUid++));
        if (!uniqToMessageMap.containsKey(mfn.getUniq())) {
            final MaildirMessage mm = new MaildirMessage(this, mfn.getFile(), mfn, -1);
            uniqToMessageMap.put(mfn.getUniq(), mm);
            return mm;
        } else {
            return (MaildirMessage) uniqToMessageMap.get(mfn.getUniq());
        }
    }

    LastModified updateLm = new LastModified();

    private void updatemsgs(boolean doNotify) throws MessagingException {
        updatemsgs(doNotify, false);
    }

    private void updatemsgs(boolean doNotify, boolean forceUpdate) throws MessagingException {
        if (!forceUpdate && (!isFoldersModified(updateLm))) return;
        synchronized (this) {
            if (!isOpen()) {
                return;
            }
            if (isdefault || !exists()) {
                return;
            }
            unreadMessages = deletedMessages = recentMessages = 0;
            ArrayList oldMessages = null;
            deletedMessages = recentMessages = totalMessages = unreadMessages = 0;
            if (doNotify) {
                oldMessages = new ArrayList(uniqToMessageMap.values());
            }
            MaildirFilename newMfns[] = MaildirUtils.listMfns(newd);
            MaildirFilename curMfns[] = MaildirUtils.listMfns(curd);
            if (messages == null) {
                messages = new ArrayList(newMfns.length + curMfns.length);
            }
            for (int i = 0; i < newMfns.length; i++) {
                final MaildirFilename mfn = newMfns[i];
                File target = new File(curd, mfn.toString());
                if (mfn.getFile().renameTo(target)) mfn.setFile(target);
                mfn.setFlag(Flags.Flag.RECENT);
                recentMessages++;
                addMessage(mfn);
            }
            for (int i = 0; i < curMfns.length; i++) {
                addMessage(curMfns[i]);
            }
            final Collection newMessages = uniqToMessageMap.values();
            if (doNotify) {
                log.debug("old messages: " + oldMessages);
                log.debug("new messages: " + newMessages);
                final Collection removedMessages = collectionsSubtract(oldMessages, newMessages);
                log.debug("removedMessages: " + removedMessages);
                final Collection addedMessages = collectionsSubtract(newMessages, oldMessages);
                log.debug("addedMessages: " + addedMessages);
                final Message[] added = (Message[]) addedMessages.toArray(emptyMessageArray);
                final Message[] removed = (Message[]) removedMessages.toArray(emptyMessageArray);
                if (removedMessages.size() > 0) {
                    notifyMessageRemovedListeners(true, removed);
                }
                if (addedMessages.size() > 0) {
                    notifyMessageAddedListeners(added);
                }
            }
            messages = new ArrayList(newMessages);
            final Iterator it = messages.iterator();
            for (int i = 1; it.hasNext(); i++) {
                final MaildirMessage m = (MaildirMessage) it.next();
                m.setMsgNum(i);
            }
            isFoldersModified(updateLm);
        }
    }

    LastModified unreadCountLm = new LastModified();

    public int getUnreadMessageCount() throws MessagingException {
        if (isOpen()) {
            updatemsgs();
            return unreadMessages;
        }
        if (!isFoldersModified(unreadCountLm)) return unreadMessages;
        int unreadNew = MaildirUtils.getFlaggedCount(newd, Flags.Flag.SEEN, false);
        int unreadCur = MaildirUtils.getFlaggedCount(curd, Flags.Flag.SEEN, false);
        unreadMessages = unreadNew + unreadCur;
        return unreadMessages;
    }

    LastModified newCountLm = new LastModified();

    public int getNewMessageCount() throws MessagingException {
        if (isOpen()) {
            updatemsgs();
            return recentMessages;
        }
        if (!isFoldersModified(newCountLm)) return recentMessages;
        String[] newf = getNewDir().list();
        recentMessages = newf == null ? 0 : newf.length;
        return recentMessages;
    }

    LastModified deletedCountLm = new LastModified();

    public int getDeletedMessageCount() throws MessagingException {
        log.debug("getDeletedMessageCount", new Exception());
        if (isOpen()) {
            updatemsgs();
            return deletedMessages;
        }
        if (!isFoldersModified(deletedCountLm)) return deletedMessages;
        int deletedNew = MaildirUtils.getFlaggedCount(newd, Flags.Flag.DELETED, true);
        int deletedCur = MaildirUtils.getFlaggedCount(curd, Flags.Flag.DELETED, true);
        deletedMessages = deletedNew + deletedCur;
        return deletedMessages;
    }

    int totalMessages = 0;

    LastModified messageCountLm = new LastModified();

    public int getMessageCount() throws MessagingException {
        if (isOpen()) {
            updatemsgs();
            return messages.size();
        }
        if (!isFoldersModified(messageCountLm)) return totalMessages;
        String[] curf = getCurDir().list();
        String[] newf = getNewDir().list();
        totalMessages = 0;
        if (curf != null) {
            totalMessages += curf.length;
        }
        if (newf != null) {
            totalMessages += newf.length;
        }
        return totalMessages;
    }

    class LastModified implements Cloneable {

        long curLm = -1;

        long newLm = -1;

        public LastModified() {
        }

        public LastModified(LastModified lm) {
            curLm = lm.curLm;
            newLm = lm.newLm;
        }

        public String toString() {
            return "cur: " + curLm + " new: " + newLm;
        }
    }

    public boolean hasNewMessages() throws MessagingException {
        return (getNewMessageCount() > 0);
    }

    private boolean doAutoCreateDir() {
        return new Boolean(getMaildirStore().getSessionProperty("mail.store.maildir.autocreatedir")).booleanValue();
    }

    private boolean checkMessageSizeBeforeAppend() {
        return new Boolean(getMaildirStore().getSessionProperty("mail.store.maildir.checkmessagesizebeforeappend")).booleanValue();
    }

    private boolean noQuotaEnforcement() {
        return new Boolean(getMaildirStore().getSessionProperty("mail.store.maildir.noquota")).booleanValue();
    }

    private final File newUniqFilename(MaildirFilename mfn, File dir, boolean useUniqOnly) throws MessagingException {
        File target = new File(dir, useUniqOnly ? mfn.getUniq() : mfn.toString());
        int attempt = 0;
        for (attempt = 0; attempt < 100 && target.exists(); attempt++) {
            int dc = mfn.getDeliveryCounter();
            mfn.setDeliveryCounter(dc + 1);
            target = new File(dir, useUniqOnly ? mfn.getUniq() : mfn.toString());
            if (target.exists()) sleep(1500);
        }
        log.debug("newUniqFilename: " + target);
        if (attempt >= 100) {
            throw new MessagingException("cannot deliver message after 100 attempts");
        }
        return target;
    }

    private final OutputStream getTmpFileOutputStream(File tmpFilename, MaildirQuota quota) throws IOException {
        OutputStream os = new NewlineOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFilename), 4096));
        if (quota == null || noQuotaEnforcement() || quota.getResourceLimit("STORAGE") == 0) return os;
        int sizeLimit = (int) quota.getResourceLimit("STORAGE");
        int mailboxSize = (int) quota.getResourceUsage("STORAGE");
        return new QuotaAwareOutputStream(os, sizeLimit - mailboxSize);
    }

    private final void checkBeforeAppend(Message m, MaildirQuota quota) throws MessagingException, QuotaExceededException {
        if (quota == null || !checkMessageSizeBeforeAppend()) return;
        int messageSize = m.getSize();
        int sizeLimit = (int) quota.getResourceLimit("STORAGE");
        int mailboxSize = (int) quota.getResourceUsage("STORAGE");
        if ((messageSize != -1) && ((mailboxSize + messageSize) > sizeLimit)) {
            throw new QuotaExceededException("message (" + messageSize + "bytes) does not fit into mailbox");
        }
    }

    public void appendMessages(Message[] msgs) throws MessagingException {
        if (doAutoCreateDir() && !isOpen() && !exists()) {
            create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);
        }
        MaildirQuota quota = getMaildirStore().getQuota("")[0];
        int mailboxSize = (int) quota.getResourceUsage("STORAGE");
        int sizeLimit = (int) quota.getResourceLimit("STORAGE");
        ArrayList addedMessages = new ArrayList(msgs.length);
        if (!noQuotaEnforcement() && sizeLimit > 0 && mailboxSize > sizeLimit) throw new MessagingException("quota exceeded", new QuotaExceededException("mailbox is full"));
        log.debug("mailboxSize: " + mailboxSize + " sizeLimit: " + sizeLimit);
        try {
            int timestamp = 0;
            for (int i = 0; i < msgs.length; i++) {
                checkBeforeAppend(msgs[i], quota);
                final MaildirFilename mfn = new MaildirFilename();
                if (mfn.getTimestamp() == timestamp) {
                    mfn.setDeliveryCounter((int) System.currentTimeMillis() % 1000);
                }
                File tmptarget = null;
                OutputStream output = null;
                try {
                    tmptarget = newUniqFilename(mfn, getTmpDir(), true);
                    output = getTmpFileOutputStream(tmptarget, quota);
                    msgs[i].writeTo(output);
                } catch (QuotaExceededException qeex) {
                    tmptarget.delete();
                    throw qeex;
                } catch (IOException ioex) {
                    log.error("unrecoverable io error: " + ioex.toString());
                    tmptarget.delete();
                    throw new MessagingException("unrecoverable io error", ioex);
                } finally {
                    streamClose(output);
                }
                mfn.setSize(tmptarget.length());
                boolean deliverToNew = (!isOpen() || msgs[i].isSet(Flags.Flag.RECENT));
                File target = (deliverToNew) ? newUniqFilename(mfn, getNewDir(), false) : newUniqFilename(mfn, getCurDir(), false);
                log.debug("rename '" + tmptarget + "' -> '" + target + "'");
                if (doAutoCreateDir() && !target.getParentFile().exists()) {
                    target.getParentFile().mkdirs();
                }
                boolean movedFromTmpToNew = tmptarget.renameTo(target);
                if (!movedFromTmpToNew) {
                    log.error("cannot rename " + tmptarget + " to " + target);
                    tmptarget.delete();
                    target.delete();
                    throw new MessagingException("cant rename " + tmptarget + " to " + target);
                }
                mfn.setFlags(msgs[i].getFlags());
                quota.setResourceUsage("MESSAGE", quota.getResourceUsage("MESSAGES") + 1);
                quota.setResourceUsage("STORAGE", mailboxSize + target.length());
                getMaildirStore().setQuota(quota);
                timestamp = mfn.getTimestamp();
                mfn.setFile(target);
                final MaildirMessage mdm = addMessage(mfn);
                if (isOpen()) {
                    mdm.setMsgNum(messages.size() + 1);
                    messages.add(mdm);
                }
                addedMessages.add(mdm);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MessagingException("cant append message", ex);
        } finally {
            if (addedMessages.size() > 0) notifyMessageAddedListeners((Message[]) addedMessages.toArray(emptyMessageArray));
        }
    }

    /**
     * Unlike Folder objects, repeated calls to getMessage with the same message
     * number will return the same Message object, as long as no messages in this
     * folder have been expunged.
     */
    public Message getMessage(int msgnum) throws MessagingException {
        log.debug("msgnum: " + msgnum);
        return getMessages(new int[] { msgnum })[0];
    }

    public Message[] getMessages(int[] msgs) throws MessagingException {
        if (!isOpen()) {
            throw new IllegalStateException("folder closed");
        }
        if (!exists()) {
            throw new FolderNotFoundException(this);
        }
        if (isdefault) {
            throw new MessagingException("no messages under root folder allowed");
        }
        final ArrayList outmsgs = new ArrayList(msgs.length);
        for (int i = 0; i < msgs.length; i++) {
            if (messages.size() < msgs[i] || (msgs[i] <= 0)) {
                throw new IndexOutOfBoundsException("message " + msgs[i] + " not available");
            }
            final MaildirMessage mdm = (MaildirMessage) messages.get(msgs[i] - 1);
            outmsgs.add(mdm);
        }
        return (Message[]) outmsgs.toArray(emptyMessageArray);
    }

    public Message[] getMessages() throws MessagingException {
        return messages == null ? emptyMessageArray : (Message[]) messages.toArray(emptyMessageArray);
    }

    public boolean isOpen() {
        return isopen;
    }

    public synchronized void close(boolean expunge) throws MessagingException {
        if (expunge) {
            expunge();
        }
        if (getMode() != Folder.READ_ONLY) {
            int msgsize = messages.size();
            for (int i = 0; i < msgsize; i++) {
                final MaildirMessage mdm = (MaildirMessage) messages.get(i);
                final MaildirFilename mfn = mdm.getMaildirFilename();
                final File file = mfn.getFile();
                if (!file.getName().equals(mfn.toString()) || (mdm.isSet(Flags.Flag.RECENT) && file.getParentFile().getName().equals("new"))) file.renameTo(new File(getCurDir(), mfn.toString()));
            }
        }
        isopen = false;
        messages = null;
        recentMessages = deletedMessages = unreadMessages = 0;
        updateLm = new LastModified();
        newCountLm = new LastModified();
        unreadCountLm = new LastModified();
        deletedCountLm = new LastModified();
        messageCountLm = new LastModified();
        uniqToMessageMap = new TreeMap();
        saveUids();
    }

    public synchronized void open(int mode) throws MessagingException {
        log.debug("open");
        if (isopen) {
            return;
        }
        if (doAutoCreateDir()) {
            create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);
        }
        if (!exists()) {
            throw new FolderNotFoundException(this, "folder '" + getName() + "' not found");
        }
        this.mode = mode;
        if (isdefault) {
            return;
        }
        loadUids();
        isopen = true;
        updatemsgs(false);
        final Enumeration keys = uids.propertyNames();
        while (keys.hasMoreElements()) {
            String uniq = (String) keys.nextElement();
            if (!uniqToMessageMap.containsKey(uniq)) {
                uids.remove(uniq);
                log.debug("removed stale uniq from uidvalidity: " + uniq);
            }
        }
    }

    private void loadUids() {
        final File uidF = getUIDVFile();
        if (uidF.exists()) {
            InputStream uidIns = null;
            try {
                uidIns = new FileInputStream(uidF);
                uids.load(uidIns);
            } catch (IOException e) {
                log.error("error loading uids from .uidvalidity: " + e);
            } finally {
                streamClose(uidIns);
            }
            String uidVStr = uids.getProperty("uidvalidity");
            String lastUidStr = uids.getProperty("lastuid");
            try {
                uidValidity = Long.parseLong(uidVStr);
                lastUid = Integer.parseInt(lastUidStr);
            } catch (NumberFormatException nfex) {
                uids.clear();
            }
            uids.remove("uidvalidity");
            uids.remove("lastuid");
        }
    }

    private void saveUids() {
        if (mode != Folder.READ_ONLY && getDir().canWrite()) {
            File uidF = getUIDVFile();
            if (!uidF.exists()) {
                try {
                    uidF.createNewFile();
                } catch (IOException e) {
                    log.error("cannot create .uidvalidity: " + e);
                }
            }
            uids.setProperty("lastuid", String.valueOf(lastUid));
            uids.setProperty("uidvalidity", String.valueOf(uidValidity));
            OutputStream uidout = null;
            try {
                uidout = new FileOutputStream(uidF);
                uids.store(uidout, null);
            } catch (IOException ioex) {
                log.error("cannot save uids to .uidvalidity: " + ioex);
            } finally {
                streamClose(uidout);
            }
        }
        uids = new Properties();
    }

    protected MaildirStore getMaildirStore() {
        return (MaildirStore) getStore();
    }

    public boolean renameTo(Folder f) throws MessagingException {
        log.debug("TRACE: '" + getFullName() + "' renameTo('" + f.getFullName() + "')");
        if (!(f instanceof MaildirFolder) || (f.getStore() != super.store)) {
            throw new MessagingException("cant rename across stores");
        }
        boolean result = dir.renameTo(((MaildirFolder) f).getDir());
        if (result) {
            notifyFolderRenamedListeners(f);
        }
        return result;
    }

    public boolean delete(boolean recurse) throws MessagingException {
        if (isdefault || str.equals(".")) {
            throw new MessagingException("cant delete root and INBOX folder");
        }
        if (!exists()) {
            return false;
        }
        if (!recurse) {
            return false;
        }
        String[] list = rootdir.list();
        boolean result = true;
        for (int i = 0; i < list.length; i++) if (list[i].startsWith(str)) {
            String path = root + list[i] + File.separatorChar;
            result = result & rmdir(new File(path));
        }
        if (result) {
            notifyFolderListeners(FolderEvent.DELETED);
        }
        return false;
    }

    public Folder getFolder(String name) throws MessagingException {
        String folderfullname = ".";
        name = name.replace(File.separatorChar, '.');
        if (name.startsWith(".")) {
            folderfullname = name;
        } else if (name.equals("INBOX")) {
            folderfullname = "INBOX";
        } else {
            if (str.endsWith(".")) {
                folderfullname = str + name;
            } else {
                folderfullname = str + "." + name;
            }
        }
        return new MaildirFolder(folderfullname, (MaildirStore) super.store);
    }

    public boolean create(int type) throws MessagingException {
        log.debug("create (" + getFullName() + ")");
        if (exists()) {
            return false;
        }
        log.debug("request to create folder: " + dir);
        log.debug("creating folder: " + dir.getAbsolutePath());
        dir.mkdirs();
        curd.mkdir();
        newd.mkdir();
        tmpd.mkdir();
        boolean result = exists();
        if (result) {
            notifyFolderListeners(FolderEvent.CREATED);
        }
        return result;
    }

    public int getType() {
        if (str.equals(".")) {
            if (isdefault) {
                return Folder.HOLDS_FOLDERS;
            } else {
                return Folder.HOLDS_MESSAGES;
            }
        }
        return (Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);
    }

    public char getSeparator() {
        return '.';
    }

    public Folder[] list(String pattern) throws MessagingException {
        log.debug("pattern: " + pattern);
        if (pattern == null) {
            pattern = "%";
        }
        int firstStar = pattern.indexOf('*');
        int firstPercent = pattern.indexOf('%');
        if (((firstStar > -1) && (pattern.indexOf('*', firstStar + 1) > -1)) || ((firstPercent > -1) && (pattern.indexOf('%', firstPercent + 1) > -1)) || ((firstStar > -1) && (firstPercent > -1))) {
            throw new MessagingException("list pattern not supported");
        }
        ArrayList folders = new ArrayList(3);
        if (!exists()) {
            return new Folder[0];
        }
        if (str.equals(".") && !isdefault) {
            return new Folder[0];
        }
        File[] matchingFiles;
        matchingFiles = rootdir.listFiles(new MaildirFileFilter(pattern));
        String rootPath = rootdir.getAbsolutePath();
        for (int i = 0; i < matchingFiles.length; i++) {
            String fileName = matchingFiles[i].getAbsolutePath();
            if (fileName.startsWith(rootPath)) {
                fileName = fileName.substring(rootPath.length());
            }
            if (fileName.startsWith(File.separator)) {
                fileName = fileName.substring(File.separator.length());
            }
            fileName.replace(File.separatorChar, getSeparator());
            fileName = BASE64MailboxDecoder.decode(fileName);
            folders.add(new MaildirFolder(fileName, (MaildirStore) store));
        }
        if (isdefault) {
            boolean includeInbox = true;
            int wildcardLocation = Math.max(firstStar, firstPercent);
            String inbox = "INBOX";
            if (wildcardLocation == -1) {
                includeInbox = pattern.equals(inbox);
            } else {
                if ((wildcardLocation > 0) && (!inbox.startsWith(pattern.substring(0, wildcardLocation)))) {
                    includeInbox = false;
                } else {
                    if ((wildcardLocation < (pattern.length() - 1)) && (!inbox.endsWith(pattern.substring(wildcardLocation + 1, pattern.length() - 1)))) {
                        includeInbox = false;
                    }
                }
            }
            if (includeInbox) {
                folders.add(new MaildirFolder("INBOX", (MaildirStore) store));
            }
        }
        log.debug("folders.size: " + folders.size());
        return (Folder[]) (folders.toArray(new Folder[] {}));
    }

    public boolean exists() throws MessagingException {
        boolean direxists = false;
        if (isdefault) {
            direxists = dir.isDirectory();
        } else {
            direxists = curd.isDirectory() && newd.isDirectory() && tmpd.isDirectory();
        }
        log.debug("exists?: " + direxists);
        return direxists;
    }

    public Folder getParent() throws MessagingException {
        if (dir.equals(rootdir)) {
            throw new MessagingException("already at rootdir cant getParent");
        }
        if (!hasParent()) {
            return new MaildirFolder(".", (MaildirStore) store);
        }
        int lastdot = str.lastIndexOf(".");
        String parentstr = "";
        if (lastdot > 0) {
            parentstr = str.substring(0, lastdot);
            return new MaildirFolder(parentstr, (MaildirStore) store);
        }
        return null;
    }

    public String getFullName() {
        String out = "";
        if (isdefault) {
            return out;
        }
        if (str.equals(".")) {
            out = "INBOX";
        } else {
            if (hasParent()) {
                out = str;
            } else {
                out = str.substring(1);
            }
        }
        out = BASE64MailboxDecoder.decode(out);
        return out;
    }

    private boolean hasParent() {
        String tmpparent = root + str.substring(0, str.lastIndexOf("."));
        boolean result = (!tmpparent.equals(root)) && new File(tmpparent).isDirectory();
        log.debug("checking for parent of " + str);
        log.debug("possible parent: " + tmpparent);
        log.debug("hasparent?: " + result);
        return result;
    }

    public String getName() {
        String out = "";
        if (isdefault) {
            out = "";
        }
        if (str.equals(".")) {
            out = "INBOX";
        } else {
            if (hasParent()) {
                out = str.substring(str.lastIndexOf(".") + 1);
            } else {
                out = str.substring(1);
            }
        }
        out = BASE64MailboxDecoder.decode(out);
        return out;
    }

    public Message[] expunge() throws MessagingException {
        if (!isOpen()) throw new FolderClosedException(this);
        if (messages == null) throw new RuntimeException("internal error: messages == null");
        final List removedMessagesList = new ArrayList();
        boolean forceUpdate = false;
        int msgsSize = messages.size();
        for (int i = msgsSize - 1; i >= 0; i--) {
            MaildirMessage mdm = (MaildirMessage) messages.get(i);
            if (mdm.isSet(Flags.Flag.DELETED)) {
                String uniq = mdm.getMaildirFilename().getUniq();
                uids.remove(uniq);
                uniqToMessageMap.remove(uniq);
                log.debug("uniq2message: " + uniqToMessageMap.toString());
                messages.remove(mdm);
                boolean result = mdm.getFile().delete();
                log.debug("removing " + mdm.getFile() + ": " + result);
                removedMessagesList.add(mdm);
                forceUpdate = true;
            }
        }
        updatemsgs(true, forceUpdate);
        Message[] removedMessages = (Message[]) removedMessagesList.toArray(emptyMessageArray);
        notifyMessageRemovedListeners(true, removedMessages);
        return removedMessages;
    }

    /**
     * Exposes notifyMessageChangedListeners to package members.
     */
    void localNotifyMessageChangedListeners(int eventType, int eventDetails, MaildirMessage changedMessage) throws MessagingException {
        if (eventType == MessageChangedEvent.FLAGS_CHANGED) {
            if ((eventDetails & FlagChangedEvent.ISSET) != 0) {
                if ((eventDetails & FlagChangedEvent.DELETED) != 0) deletedMessages++; else if ((eventDetails & FlagChangedEvent.RECENT) != 0) recentMessages++; else if ((eventDetails & FlagChangedEvent.SEEN) != 0) unreadMessages--;
            } else {
                if ((eventDetails & FlagChangedEvent.DELETED) != 0) deletedMessages--; else if ((eventDetails & FlagChangedEvent.RECENT) != 0) recentMessages--; else if ((eventDetails & FlagChangedEvent.SEEN) != 0) unreadMessages++;
            }
        }
        notifyMessageChangedListeners(eventType, changedMessage);
    }

    public Flags getPermanentFlags() {
        return supportedFlags;
    }

    private File uidVFile = null;

    private File getUIDVFile() {
        if (uidVFile == null) uidVFile = new File(getDir(), ".uidvalidity");
        return uidVFile;
    }

    public long getUIDValidity() throws MessagingException {
        return uidValidity;
    }

    public Message getMessageByUID(long uid) throws MessagingException {
        return getMessagesByUID(new long[] { uid })[0];
    }

    public Message[] getMessagesByUID(long start, long end) throws MessagingException {
        if (end == LASTUID) end = lastUid;
        if (end < start) throw new IndexOutOfBoundsException("end cannot be lesser than start");
        ArrayList messages = new ArrayList();
        for (long i = start; i <= end; i++) {
            messages.add(getMessagesByUID(new long[] { i }));
        }
        return (Message[]) messages.toArray(new Message[] {});
    }

    /**
     * Method is slow on multiple messages.
     */
    public Message[] getMessagesByUID(long[] uidArray) throws MessagingException {
        uids.remove("lastuid");
        uids.remove("uidvalidity");
        long sortedUidArray[] = new long[uidArray.length];
        System.arraycopy(uidArray, 0, sortedUidArray, 0, uidArray.length);
        Arrays.sort(sortedUidArray);
        Message msgs[] = new Message[uidArray.length];
        final Enumeration uniqs = uids.propertyNames();
        for (int i = 0; uniqs.hasMoreElements(); i++) {
            final String uniq = (String) uniqs.nextElement();
            int uid = 0;
            try {
                uid = Integer.parseInt(uids.getProperty(uniq));
            } catch (NumberFormatException nfex) {
            }
            if (Arrays.binarySearch(sortedUidArray, uid) >= 0) {
                msgs[i] = (Message) uniqToMessageMap.get(uniq);
            }
        }
        return msgs;
    }

    public long getUID(Message message) throws MessagingException {
        if (!(message instanceof MaildirMessage)) throw new NoSuchElementException("message does not belong to this folder");
        MaildirMessage mdm = (MaildirMessage) message;
        String uidStr = uids.getProperty(mdm.getMaildirFilename().getUniq());
        if (uidStr == null) throw new NoSuchElementException("message does not belong to this folder");
        int uid = 0;
        try {
            uid = Integer.parseInt(uidStr);
        } catch (NumberFormatException nfex) {
            throw new NoSuchElementException("message does not belong to this folder: " + nfex.getMessage());
        }
        return uid;
    }

    protected File getDir() {
        return dir;
    }

    protected File getCurDir() {
        return curd;
    }

    protected File getTmpDir() {
        return tmpd;
    }

    protected File getNewDir() {
        return newd;
    }

    private static final Collection collectionsSubtract(final Collection a, final Collection b) {
        final ArrayList list = new ArrayList(a);
        final Iterator it = b.iterator();
        while (it.hasNext()) {
            list.remove(it.next());
        }
        return list;
    }

    /**
     * Finds only matching valid maildir directories.
     */
    class MaildirFileFilter implements FileFilter {

        String pattern;

        /**
         * Creates a new MaildirFileFilter to match the given pattern.
         */
        public MaildirFileFilter(String pPattern) {
            if (pPattern == null) {
                pPattern = "%";
            }
            if (str.endsWith(".")) {
                pattern = str + pPattern;
            } else {
                pattern = str + "." + pPattern;
            }
        }

        /**
         * Tests whether or not the specified abstract pathname should be
         * included in a pathname list.
         */
        public boolean accept(File f) {
            if (!(f.isDirectory() && (new File(f, "cur")).isDirectory() && (new File(f, "new")).isDirectory() && (new File(f, "tmp")).isDirectory())) {
                return false;
            }
            String fileName = f.getName();
            fileName = BASE64MailboxDecoder.decode(fileName);
            boolean noRecurse = false;
            int wildcard = pattern.indexOf('*');
            if (wildcard < 0) {
                wildcard = pattern.indexOf('%');
                noRecurse = true;
            }
            if (wildcard < 0) {
                return fileName.equals(pattern);
            }
            if (wildcard > 0) {
                if (!fileName.startsWith(pattern.substring(0, wildcard))) {
                    return false;
                }
            }
            if (wildcard != (pattern.length() - 1)) {
                if (!fileName.endsWith(pattern.substring(wildcard + 1))) {
                    return false;
                }
            }
            if (noRecurse) {
                if (fileName.substring(wildcard, fileName.length() - (pattern.length() - wildcard) + 1).indexOf(getSeparator()) > -1) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final void streamClose(OutputStream outs) {
        if (outs != null) try {
            outs.close();
        } catch (Exception ex) {
        }
    }

    private static final void streamClose(InputStream ins) {
        if (ins != null) try {
            ins.close();
        } catch (Exception ex) {
        }
    }

    private static final void sleep(long usec) {
        try {
            Thread.sleep(usec);
        } catch (Exception ex) {
        }
    }

    private static final boolean rmdir(File d) {
        log.debug("TRACE: rmdir(" + d + ")");
        if (!d.exists()) return false;
        if (d.isFile()) {
            return d.delete();
        }
        File[] list = d.listFiles();
        if (list.length == 0) {
            return d.delete();
        }
        for (int i = 0; i < list.length; i++) if (list[i].isDirectory()) {
            return rmdir(list[i]);
        } else {
            list[i].delete();
        }
        return false;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }
}

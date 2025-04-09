package securus.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import securus.entity.User;

/**
 * Functionality for file sending via mail
 * 
 * @author m.kanel
 * 
 */
@Name("fileSendingService")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class FileSendingService {

    @In(required = false)
    private User currentUser;

    @In(required = true)
    private MailServices mailServices;

    @Logger
    private Log log;

    public abstract static class FileSystemItem extends FileItem {

        public FileSystemItem(String path, boolean isFolder) {
            super(path, isFolder);
        }

        public abstract InputStream getContent();
    }

    ;

    public List<StatusMessage> sendToEmail(List<FileSystemItem> files, String to, String cc, String bcc, String subject, String body) throws IOException {
        List<StatusMessage> sms = new ArrayList<StatusMessage>();
        String[] tos = splitAddress("emailTo", sms, to);
        if (cc != null) {
            splitAddress("emailCc", sms, cc);
        }
        if (bcc != null) {
            splitAddress("emailBcc", sms, bcc);
        }
        if (!sms.isEmpty()) {
            return sms;
        }
        List<? extends Attachment> attachments;
        if (files.size() > 1) {
            attachments = createZipAttachment(files, containsFolder(files));
        } else {
            attachments = createFileAttachments(files);
        }
        try {
            mailServices.sendFiles(currentUser, tos, cc, bcc, subject, body, attachments.toArray(new Attachment[0]));
        } finally {
            for (Attachment a : attachments) {
                try {
                    a.close();
                } catch (IOException e) {
                    log.warn("Failed to close attachment " + a, e);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Attachment> createFileAttachments(List<FileSystemItem> items) throws MalformedURLException {
        List<Attachment> attachments = new ArrayList<Attachment>();
        for (FileSystemItem node : items) {
            String path = node.getFile();
            InputStream in = node.getContent();
            attachments.add(new Attachment(in, path));
        }
        return attachments;
    }

    private List<? extends Attachment> createZipAttachment(List<FileSystemItem> nodes, boolean withFolders) throws IOException {
        File tmp = File.createTempFile("securus", null);
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(tmp));
        try {
            for (FileSystemItem node : nodes) {
                String path;
                if (withFolders) {
                    path = node.getFile();
                } else {
                    path = node.getFile();
                }
                zip.putNextEntry(new ZipEntry(path));
                InputStream in = new BufferedInputStream(node.getContent());
                int b;
                while ((b = in.read()) != -1) {
                    zip.write(b);
                }
                zip.closeEntry();
            }
        } finally {
            zip.close();
        }
        return Arrays.asList(new ZipAttachment(tmp));
    }

    private boolean containsFolder(List<FileSystemItem> nodes) {
        for (FileSystemItem node : nodes) {
            if (node.isFolder()) {
                return true;
            }
        }
        return false;
    }

    private static String[] splitAddress(String key, List<StatusMessage> sms, String addr) {
        String[] addrs = splitAddress(addr);
        if (key.equals("emailTo") && addrs.length == 0) {
            sms.add(new StatusMessage(key, "missing_email_address"));
        } else {
            checkEmails(key, sms, Arrays.asList(addrs));
        }
        return addrs;
    }

    private static String[] splitAddress(String addr) {
        addr = addr.trim();
        if (addr.isEmpty()) {
            return new String[0];
        }
        return addr.split(" *; *");
    }

    private static boolean checkEmails(String key, List<StatusMessage> sms, List<String> emails) {
        if (key.equals("emailTo") && emails.isEmpty()) {
            sms.add(new StatusMessage("missing_email_address"));
        } else {
            for (String email : emails) {
                Pattern pattern = ApplicationContext.getEmailPattern();
                if (!pattern.matcher(email).matches()) {
                    sms.add(new StatusMessage(key, "incorrect_email_address", email + "."));
                } else {
                    String host = email.substring(email.indexOf('@') + 1);
                    try {
                        InetAddress.getByName(host);
                    } catch (UnknownHostException e) {
                        sms.add(new StatusMessage(key, "unknown_host_2", email + "."));
                    }
                }
            }
        }
        return sms.isEmpty();
    }
}

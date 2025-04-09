package net.sf.vfsjfilechooser.accessories.bookmarks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class to save bookmarks
 *
 * @author Yves Zoundi <yveszoundi at users dot sf dot net>
 * @author Stan Love
 * @author Alex Arana <alex at arana.net.au>
 * @version 0.0.2
 */
final class BookmarksWriter {

    private Writer writer;

    public BookmarksWriter() {
    }

    private void startAttribute(String name, String value) throws IOException {
        writer.write(" ");
        writer.write(name);
        writer.write(" =");
        writer.write("\"");
        writer.write(value);
        writer.write("\"");
    }

    private void startTag(String name) throws IOException {
        writer.write("<" + name + ">");
    }

    private void startNewLine() throws IOException {
        writer.write("\n");
    }

    private void endTag(String tagName) throws IOException {
        writer.write("</" + tagName + ">");
    }

    private void writeData(List<TitledURLEntry> entries) throws java.io.IOException {
        startTag("entries");
        Iterator<TitledURLEntry> it = entries.iterator();
        while (it.hasNext()) {
            TitledURLEntry entry = it.next();
            if ((entry == null) || ((entry.getTitle() == null) || (entry.getTitle().length() == 0))) {
                it.remove();
            }
            startNewLine();
            writer.write("<entry");
            startAttribute("title", entry.getTitle());
            startAttribute("url", entry.getURL());
            if (entry instanceof FTPURLEntry) {
                FTPURLEntry ftpEntry = (FTPURLEntry) entry;
                startAttribute("passiveFtp", String.valueOf(ftpEntry.isPassiveFtp()));
            }
            writer.write("/>");
        }
        startNewLine();
        endTag("entries");
    }

    public void writeToFile(List<TitledURLEntry> entries, File bookmarksFile) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if ((entries == null) || (bookmarksFile == null)) {
            throw new NullPointerException();
        }
        String write_type = "b1";
        if (write_type.equals("")) {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bookmarksFile), "UTF-8"));
            writeData(entries);
            writer.flush();
            writer.close();
        } else if (write_type.equals("b1")) {
            writer = (new StringWriter());
            writeData(entries);
            byte[] out = writer.toString().getBytes();
            byte[] raw = new byte[16];
            raw[0] = (byte) 1;
            raw[2] = (byte) 23;
            raw[3] = (byte) 24;
            raw[4] = (byte) 2;
            raw[5] = (byte) 99;
            raw[6] = (byte) 200;
            raw[7] = (byte) 202;
            raw[8] = (byte) 209;
            raw[9] = (byte) 199;
            raw[10] = (byte) 181;
            raw[11] = (byte) 255;
            raw[12] = (byte) 33;
            raw[13] = (byte) 210;
            raw[14] = (byte) 214;
            raw[15] = (byte) 216;
            SecretKeySpec skeyspec = new SecretKeySpec(raw, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
            byte[] encrypted = cipher.doFinal(out);
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bookmarksFile), "UTF-8"));
            writer2.write("b1");
            writer2.write(Util.byteArraytoHexString(encrypted));
            writer2.flush();
            writer2.close();
        } else {
            System.out.println("FATAL ERROR -- BookmarksWriter.java  unknown write style");
            System.exit(10);
        }
    }
}

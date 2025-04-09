import static junit.framework.Assert.fail;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import de.unkrig.commons.io.FileUtil;
import de.unkrig.patch.Main;

public class PatchTest {

    private static final File UNPATCHED = new File("files/unpatched");

    private static final File PATCHED = new File("files/patched");

    @Test
    public void testFiles() throws IOException {
        Files expected = new Files(new Object[] { "dir1", new Object[] { "dir2", new Object[] { "file1", "line1\nline2\nline3\n", "file.zip", new Object[] { "dir1/dir2/file1", "line1\nline2\nline3\n", "dir3/dir4/file2", "line1\nline2\nline3\n" } } } });
        expected.save(UNPATCHED);
        Files actual = new Files(UNPATCHED);
        this.assertNoDiff(expected, actual);
    }

    @Test
    public void testNop() throws Exception {
        if (UNPATCHED.exists()) FileUtil.delete(UNPATCHED);
        new Files(new Object[] { "dir1", new Object[] { "dir2", new Object[] { "file1", "line1\nline2\nline3\n", "file.zip", new Object[] { "dir1/dir2/file1", "line1\nline2\nline3\n", "dir3/dir4/file2", "line1\nline2\nline3\n" } } } }).save(UNPATCHED);
        if (PATCHED.exists()) FileUtil.delete(PATCHED);
        Main.main(new String[] { "-zip", "-debug", UNPATCHED.getPath(), PATCHED.getPath() });
        Files expected = new Files(new Object[] { "dir1", new Object[] { "dir2", new Object[] { "file1", "line1\nline2\nline3\n", "file.zip", new Object[] { "dir1/dir2/file1", "line1\nline2\nline3\n", "dir3/dir4/file2", "line1\nline2\nline3\n" } } } });
        Files actual = new Files(PATCHED);
        this.assertNoDiff(expected, actual);
    }

    @Test
    public void testSubstitute() throws Exception {
        if (UNPATCHED.exists()) FileUtil.delete(UNPATCHED);
        new Files(new Object[] { "dir1", new Object[] { "dir2", new Object[] { "file1", "line1\nline2\nline3\n", "file.zip", new Object[] { "dir1/dir2/file1", "line1\nline2\nline3\n", "dir3/dir4/file2", "line1\nline2\nline3\n" } } } }).save(UNPATCHED);
        if (PATCHED.exists()) FileUtil.delete(PATCHED);
        Main.main(new String[] { "-zip", "-substitute", "**file1", "line2", "foo", "-substitute", "***file1", "line3", "bar", UNPATCHED.getPath(), PATCHED.getPath() });
        Files expected = new Files(new Object[] { "dir1", new Object[] { "dir2", new Object[] { "file1", "line1\nfoo\nbar\n", "file.zip", new Object[] { "dir1/dir2/file1", "line1\nline2\nbar\n", "dir3/dir4/file2", "line1\nline2\nline3\n" } } } });
        Files actual = new Files(PATCHED);
        this.assertNoDiff(expected, actual);
    }

    @Test
    public void testRename() throws Exception {
        if (UNPATCHED.exists()) FileUtil.delete(UNPATCHED);
        new Files(new Object[] { "dir1", new Object[] { "dir2", new Object[] { "file1", "line1\nline2\nline3\n", "file.zip", new Object[] { "dir1/dir2/file1", "line1\nline2\nline3\n", "dir3/dir4/file2", "line1\nline2\nline3\n" } } } }).save(UNPATCHED);
        if (PATCHED.exists()) FileUtil.delete(PATCHED);
        Main.main(new String[] { "-zip", "-debug", "-rename", "(***)/dir2/file1=$1/file11", "-rename", "(**)/file11=$1/dir33/file33", "-rename", "(**!**)/file11=$1/file22", UNPATCHED.getPath(), PATCHED.getPath() });
        Files expected = new Files(new Object[] { "dir1", new Object[] { "dir2", new Object[] { "file.zip", new Object[] { "dir1/file22", "line1\nline2\nline3\n", "dir3/dir4/file2", "line1\nline2\nline3\n" } }, "dir33", new Object[] { "file33", "line1\nline2\nline3\n" } } });
        Files actual = new Files(PATCHED);
        this.assertNoDiff(expected, actual);
    }

    private void assertNoDiff(Files expected, Files actual) {
        String diff = expected.diff(actual);
        if (diff != null) fail(diff);
    }

    static class Files {

        private final Object desc;

        public Files(Object[] desc) {
            this.desc = desc;
        }

        public Files(File file) throws IOException {
            this.desc = this.load(file);
        }

        private Object load(File file) throws IOException {
            return (file.isDirectory() ? loadDir(file) : file.getName().endsWith(".zip") ? loadZipFile(file) : loadFile(file));
        }

        public void save(File file) throws IOException {
            this.save(file, this.desc);
        }

        /**
         * @return {@code null} if {@code this} and {@code that} are equal, or a human-readable text that describes the
         *         first difference between {@code this} and {@code that}
         */
        public String diff(Files that) {
            return diff("", this.desc, that.desc);
        }

        private static String diff(String name, Object desc1, Object desc2) {
            if (desc1 instanceof String) {
                if (desc2 instanceof String) {
                    if (desc1.equals(desc2)) {
                        return null;
                    }
                    return (name + ": Contents should be '" + ((String) desc1).replace('\n', '|') + "', not '" + ((String) desc2).replace('\n', '|') + "'");
                }
                return name + " should be a plain member / ZIP entry";
            }
            if (desc2 instanceof String) {
                return name + " should be a directory or ZIP file";
            }
            name += name.endsWith(".zip") ? '!' : '/';
            SortedMap<String, Object> entries1 = new TreeMap<String, Object>();
            {
                Object[] oa = (Object[]) desc1;
                for (int i = 0; i < oa.length; ) {
                    entries1.put((String) oa[i++], oa[i++]);
                }
            }
            SortedMap<String, Object> entries2 = new TreeMap<String, Object>();
            {
                Object[] oa = (Object[]) desc2;
                for (int i = 0; i < oa.length; ) {
                    entries2.put((String) oa[i++], oa[i++]);
                }
            }
            for (Iterator<Entry<String, Object>> it1 = entries1.entrySet().iterator(), it2 = entries2.entrySet().iterator(); ; ) {
                if (it1.hasNext()) {
                    Entry<String, Object> entry1 = it1.next();
                    if (it2.hasNext()) {
                        Entry<String, Object> entry2 = it2.next();
                        String name1 = entry1.getKey();
                        String name2 = entry2.getKey();
                        int cmp = name1.compareTo(name2);
                        if (cmp < 0) return name + name1 + " missing";
                        if (cmp > 0) return "Unexpected " + name + name2;
                        String diff = diff(name + name1, entry1.getValue(), entry2.getValue());
                        if (diff != null) return diff;
                    } else {
                        return name + entry1.getKey() + " missing";
                    }
                } else {
                    if (it2.hasNext()) {
                        return "Unexpected " + name + it2.next().getKey();
                    } else {
                        break;
                    }
                }
            }
            return null;
        }

        private void save(File file, Object desc) throws IOException {
            if (desc instanceof String) {
                String contents = (String) desc;
                OutputStream os = new FileOutputStream(file);
                try {
                    this.saveContents(contents, os);
                    os.close();
                } finally {
                    try {
                        os.close();
                    } catch (Exception e) {
                    }
                }
                return;
            } else if (file.getName().endsWith(".zip")) {
                this.saveZipFile((Object[]) desc, file);
            } else {
                this.saveDir((Object[]) desc, file);
            }
        }

        /**
         * @param desc Pairs of member name and {@link String} (contents of plain file) or {@code Object[]}
         *             (subdirectory or ZIP file)
         */
        private void saveDir(Object[] desc, File dir) throws IOException {
            dir.mkdirs();
            for (int i = 0; i < desc.length; ) {
                this.save(new File(dir, (String) desc[i++]), desc[i++]);
            }
        }

        private void saveZipFile(Object[] entries, File file) throws FileNotFoundException, IOException {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
            try {
                this.saveZipEntries(entries, zos);
                zos.close();
            } finally {
                try {
                    zos.close();
                } catch (Exception e) {
                }
            }
        }

        private void saveContents(String contents, OutputStream os) {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
            for (int from = 0; from != contents.length(); ) {
                int to = contents.indexOf('\n', from);
                pw.println(contents.substring(from, to));
                from = to + 1;
            }
            pw.flush();
        }

        /**
         * @param entries Pairs of name and {@link String} (plain ZIP entry contents) or {@code Object[]} (nested ZIP
         *                file entries)
         */
        private void saveZipEntries(Object[] entries, ZipOutputStream zos) throws IOException {
            for (int i = 0; i < entries.length; ) {
                String entryName = (String) entries[i++];
                Object entry = entries[i++];
                zos.putNextEntry(new ZipEntry(entryName));
                if (entry instanceof String) {
                    this.saveContents((String) entry, zos);
                } else {
                    this.saveZipEntries((Object[]) entry, zos);
                }
            }
        }

        /**
         * @return Pairs of member name and {@link String} (contents of plain file) or {@code Object[]} (subdirectory
         *         or ZIP file)
         */
        private static Object[] loadDir(File dir) throws IOException {
            File[] members = dir.listFiles();
            Object[] oa = new Object[2 * members.length];
            for (int i = 0; i < members.length; i++) {
                File member = members[i];
                oa[2 * i] = member.getName();
                oa[2 * i + 1] = (member.isDirectory() ? loadDir(member) : member.getName().endsWith(".zip") ? loadZipFile(member) : loadFile(member));
            }
            return oa;
        }

        /**
         * @return The files's contents; lines separated with '\n'
         */
        private static String loadFile(File file) throws IOException {
            Reader r = new FileReader(file);
            try {
                String text = loadReader(r);
                r.close();
                return text;
            } finally {
                try {
                    r.close();
                } catch (Exception e) {
                }
            }
        }

        /**
         * @return The {@link Reader}'s contents; lines separated with '\n'
         */
        private static String loadReader(Reader r) throws IOException {
            BufferedReader br = r instanceof BufferedReader ? (BufferedReader) r : new BufferedReader(r);
            StringBuilder sb = new StringBuilder();
            for (; ; ) {
                String line = br.readLine();
                if (line == null) break;
                sb.append(line).append('\n');
            }
            return sb.toString();
        }

        /**
         * @return Pairs of entry name and {@link String} (plain entry) or {@code Object[]} (nested ZIP entry)
         */
        private static Object loadZipFile(File file) throws IOException {
            ZipFile zipFile = new ZipFile(file);
            try {
                List<Object> l = new ArrayList<Object>();
                for (ZipEntry zipEntry : Collections.list(zipFile.entries())) {
                    l.add(zipEntry.getName());
                    InputStream is = zipFile.getInputStream(zipEntry);
                    try {
                        l.add(loadZipEntry(zipEntry, is));
                        is.close();
                    } finally {
                        try {
                            is.close();
                        } catch (Exception e) {
                        }
                    }
                }
                zipFile.close();
                return l.toArray();
            } finally {
                try {
                    zipFile.close();
                } catch (Exception e) {
                }
            }
        }

        /**
         * @return {@link String} (plain entry) or {@code Object[]} (nested ZIP entry)
         */
        private static Object loadZipEntry(ZipEntry zipEntry, InputStream is) throws IOException {
            return (zipEntry.getName().endsWith(".zip") ? loadZipInputStream(new ZipInputStream(is)) : loadReader(new InputStreamReader(is)));
        }

        /**
         * @return Pairs of entry name and {@link String} (plain entry) or {@code Object[]} (nested ZIP entry)
         */
        private static Object[] loadZipInputStream(ZipInputStream zipInputStream) throws IOException {
            List<Object> l = new ArrayList<Object>();
            for (; ; ) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (zipEntry == null) break;
                l.add(zipEntry.getName());
                l.add(loadZipEntry(zipEntry, zipInputStream));
            }
            return l.toArray();
        }
    }
}

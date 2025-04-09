package hup.instrument;

import java.io.*;
import java.util.*;
import java.util.zip.*;

class ClassFileDB extends Vector {

    int index = 0;

    public ClassFileDB(String conf_file) {
        parseConfigFile(conf_file);
    }

    public ClassFileDB(InputStream in, OutputStream out) {
        try {
            addElement(new ClassStreamEntry(in, out));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public ClassFileDB(String cmd, String in, String out) {
        try {
            if (cmd.equals("jar")) {
                addElement(new JarFileEntry(in, out));
            }
            if (cmd.equals("class")) {
                addElement(new ClassFileEntry(in, out));
            }
            if (cmd.equals("dir")) {
                if (in.equals(out)) {
                    throw new IOException("Input and output directories are equal: " + in);
                }
                dirCommandProcessor(in, out);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void parseConfigFile(String conf_file) {
        try {
            File f = new File(conf_file);
            BufferedReader in = new BufferedReader(new FileReader(f));
            String line;
            while ((line = in.readLine()) != null) {
                if (new StringTokenizer(line).countTokens() == 0) {
                    continue;
                }
                if (new StringTokenizer(line).nextToken().startsWith("#")) {
                    continue;
                }
                if (new StringTokenizer(line).countTokens() < 3) {
                    throw new Exception("Wrong configuration file format: " + line);
                }
                StringTokenizer st = new StringTokenizer(line);
                String cmd = st.nextToken();
                if (cmd.equals("jar")) {
                    addElement(new JarFileEntry(st.nextToken(), st.nextToken()));
                }
                if (cmd.equals("class")) {
                    addElement(new ClassFileEntry(st.nextToken(), st.nextToken()));
                }
                if (cmd.equals("dir")) {
                    String in_dir = st.nextToken();
                    String out_dir = st.nextToken();
                    if (in_dir.equals(out_dir)) {
                        throw new IOException("Input and output directories are equal: " + line);
                    }
                    dirCommandProcessor(in_dir, out_dir);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return;
    }

    public ClassFile getNextClassFile() {
        if (size() < index + 1) return null;
        FileEntry cur_entry = (FileEntry) elementAt(index);
        if (cur_entry.hasMoreClassFiles() == false) {
            cur_entry.closeFileEntry();
            if (size() < (++index + 1)) return null;
            cur_entry = (FileEntry) elementAt(index);
        }
        cur_entry.activateNextClassFile();
        return cur_entry;
    }

    public void reset() {
        close();
        index = 0;
    }

    public void close() {
        if (size() < index + 1) return;
        ((FileEntry) elementAt(index)).closeFileEntry();
    }

    private void dirCommandProcessor(String in_dir, String out_dir) {
        String list[] = new File(in_dir).list();
        new File(out_dir).mkdirs();
        for (int i = 0; i < list.length; i++) {
            String in = new String(in_dir + "/" + list[i]);
            String out = new String(out_dir + "/" + list[i]);
            if (new File(in).isDirectory()) {
                dirCommandProcessor(in, out);
            } else if (in.endsWith(".class")) {
                addElement(new ClassFileEntry(in, out));
            } else if (in.endsWith(".jar")) {
                addElement(new JarFileEntry(in, out));
            }
        }
    }

    private abstract class FileEntry extends ClassFile {

        protected String in, out;

        abstract boolean hasMoreClassFiles();

        abstract void activateNextClassFile();

        abstract void closeFileEntry();
    }

    private class JarFileEntry extends FileEntry {

        ZipFile zip_file;

        Enumeration zip_file_en = null;

        ZipEntry cur_zip_entry;

        MyZipOutputStream my_zip_os;

        class MyZipOutputStream extends ZipOutputStream {

            MyZipOutputStream(OutputStream os) {
                super(os);
            }

            public void close() {
            }

            public void myClose() throws IOException {
                super.close();
            }
        }

        JarFileEntry(String in, String out) {
            super.in = in;
            super.out = out;
            File in_f = new File(in);
            File out_f = new File(out);
            try {
                if (in.equals(out)) {
                    throw new IOException("Input and output files are equal");
                }
                if (!in_f.exists() || !in_f.isFile() || !in_f.canRead()) {
                    throw new IOException("Failed to open input file");
                }
                if (out_f.exists() && !out_f.isFile()) {
                    throw new IOException("Failed to open output file");
                }
                String parent = out_f.getParent();
                if (parent != null) {
                    new File(parent).mkdirs();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage() + " (in - " + in + ", out - " + out + ")");
                System.exit(1);
            }
        }

        boolean hasMoreClassFiles() {
            if (zip_file_en == null) return true;
            if (!zip_file_en.hasMoreElements()) return false;
            Enumeration tmp_zip_file_en = zip_file.entries();
            ZipEntry tmp_zip_entry;
            do {
                tmp_zip_entry = (ZipEntry) tmp_zip_file_en.nextElement();
            } while (!tmp_zip_entry.getName().equals(cur_zip_entry.getName()));
            do {
                tmp_zip_entry = (ZipEntry) tmp_zip_file_en.nextElement();
                if (tmp_zip_entry.getName().endsWith(".class")) break;
                try {
                    cur_zip_entry = (ZipEntry) zip_file_en.nextElement();
                    final int BUF_LEN = 1024;
                    byte b[] = new byte[BUF_LEN];
                    int len;
                    cur_zip_entry.setCompressedSize(-1);
                    my_zip_os.putNextEntry(cur_zip_entry);
                    InputStream is = zip_file.getInputStream(cur_zip_entry);
                    while ((len = is.read(b, 0, BUF_LEN)) > 0) {
                        my_zip_os.write(b, 0, len);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            } while (tmp_zip_file_en.hasMoreElements() && !tmp_zip_entry.getName().endsWith(".class"));
            if (tmp_zip_entry.getName().endsWith(".class")) return true;
            return false;
        }

        void activateNextClassFile() {
            try {
                if (zip_file_en == null) {
                    zip_file = new ZipFile(new File(super.in));
                    zip_file_en = zip_file.entries();
                    my_zip_os = new MyZipOutputStream((OutputStream) (new FileOutputStream(new File(super.out))));
                    my_zip_os.setLevel(0);
                }
                do {
                    cur_zip_entry = (ZipEntry) zip_file_en.nextElement();
                    if (!cur_zip_entry.getName().endsWith(".class")) {
                        final int BUF_LEN = 1024;
                        byte b[] = new byte[BUF_LEN];
                        int len;
                        cur_zip_entry.setCompressedSize(-1);
                        my_zip_os.putNextEntry(cur_zip_entry);
                        InputStream is = zip_file.getInputStream(cur_zip_entry);
                        while ((len = is.read(b, 0, BUF_LEN)) > 0) {
                            my_zip_os.write(b, 0, len);
                        }
                    } else {
                        ZipEntry new_zip_entry = new ZipEntry(cur_zip_entry.getName());
                        new_zip_entry.setComment(cur_zip_entry.getComment());
                        new_zip_entry.setExtra(cur_zip_entry.getExtra());
                        my_zip_os.putNextEntry(new_zip_entry);
                    }
                } while (zip_file_en.hasMoreElements() && !cur_zip_entry.getName().endsWith(".class"));
            } catch (Exception e) {
                System.out.println(e.getMessage() + " (in - " + super.in + ", out - " + super.out + ")");
                System.exit(1);
            }
        }

        void closeFileEntry() {
            try {
                if (zip_file_en != null) {
                    my_zip_os.closeEntry();
                    my_zip_os.myClose();
                    zip_file.close();
                }
            } catch (IOException e) {
                System.out.println("Failed to close files (in - " + super.in + ", out - " + super.out + ")");
                System.exit(1);
            }
        }

        InputStream getInputStream() throws IOException {
            if (zip_file_en == null) return null;
            return zip_file.getInputStream(cur_zip_entry);
        }

        OutputStream getOutputStream() throws IOException {
            if (zip_file_en == null) return null;
            return (OutputStream) my_zip_os;
        }

        String getClassName() {
            return new File(super.in).getName();
        }
    }

    private class ClassFileEntry extends FileEntry {

        static final int num_entries = 1;

        int entries_counter = 0;

        FileInputStream file_is = null;

        FileOutputStream file_os = null;

        ClassFileEntry(String in, String out) {
            super.in = in;
            super.out = out;
            File in_f = new File(in);
            File out_f = new File(out);
            try {
                if (in.equals(out)) {
                    throw new IOException("Input and output files are equal");
                }
                if (!in_f.exists() || !in_f.isFile() || !in_f.canRead()) {
                    throw new IOException("Failed to open input file");
                }
                if (out_f.exists() && !out_f.isFile()) {
                    throw new IOException("Failed to open output file");
                }
                String parent = out_f.getParent();
                if (parent != null) {
                    new File(parent).mkdirs();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage() + " (in - " + in + ", out - " + out + ")");
                System.exit(1);
            }
        }

        boolean hasMoreClassFiles() {
            return (entries_counter < num_entries);
        }

        void activateNextClassFile() {
            entries_counter++;
        }

        void closeFileEntry() {
            try {
                if (file_is != null) file_is.close();
                if (file_os != null) file_os.close();
            } catch (IOException e) {
                System.out.println("Failed to close files (in - " + super.in + ", out - " + super.out + ")");
                System.exit(1);
            }
        }

        InputStream getInputStream() throws IOException {
            file_is = new FileInputStream(new File(super.in));
            return (InputStream) file_is;
        }

        OutputStream getOutputStream() throws IOException {
            file_os = new FileOutputStream(new File(super.out));
            return (OutputStream) file_os;
        }

        String getClassName() {
            return new File(super.in).getName();
        }
    }

    private class ClassStreamEntry extends FileEntry {

        static final int num_entries = 1;

        int entries_counter = 0;

        InputStream is = null;

        OutputStream os = null;

        ClassStreamEntry(InputStream in, OutputStream out) {
            super.in = null;
            super.out = null;
            is = in;
            os = out;
        }

        boolean hasMoreClassFiles() {
            return (entries_counter < num_entries);
        }

        void activateNextClassFile() {
            entries_counter++;
        }

        void closeFileEntry() {
        }

        InputStream getInputStream() throws IOException {
            return is;
        }

        OutputStream getOutputStream() throws IOException {
            return os;
        }

        String getClassName() {
            return null;
        }
    }
}

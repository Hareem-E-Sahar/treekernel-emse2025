package net.sourceforge.jsxe.help;

import java.io.*;
import java.net.*;
import java.util.zip.*;
import java.util.*;
import net.sourceforge.jsxe.util.Log;

class HelpIndex {

    public HelpIndex() {
        words = new HashMap();
        files = new ArrayList();
        ignoreWord("a");
        ignoreWord("an");
        ignoreWord("and");
        ignoreWord("are");
        ignoreWord("as");
        ignoreWord("be");
        ignoreWord("by");
        ignoreWord("can");
        ignoreWord("do");
        ignoreWord("for");
        ignoreWord("from");
        ignoreWord("how");
        ignoreWord("i");
        ignoreWord("if");
        ignoreWord("in");
        ignoreWord("is");
        ignoreWord("it");
        ignoreWord("not");
        ignoreWord("of");
        ignoreWord("on");
        ignoreWord("or");
        ignoreWord("s");
        ignoreWord("that");
        ignoreWord("the");
        ignoreWord("this");
        ignoreWord("to");
        ignoreWord("will");
        ignoreWord("with");
        ignoreWord("you");
    }

    /**
     * Indexes all available help, including the jEdit user's guide, FAQ,]
     * and plugin documentation.
     */
    public void indexEditorHelp() {
        try {
            String home = jsXe.getInstallDirectory();
            if (home != null) {
                indexDirectory(MiscUtilities.constructPath(home, "doc", "users-guide"));
                indexDirectory(MiscUtilities.constructPath(home, "doc", "FAQ"));
                indexDirectory(MiscUtilities.constructPath(home, "doc", "news42"));
            }
        } catch (Throwable e) {
            Log.log(Log.ERROR, this, "Error indexing editor help");
            Log.log(Log.ERROR, this, e);
        }
        PluginJAR[] jars = jEdit.getPluginJARs();
        for (int i = 0; i < jars.length; i++) {
            try {
                indexJAR(jars[i].getZipFile());
            } catch (Throwable e) {
                Log.log(Log.ERROR, this, "Error indexing JAR: " + jars[i].getPath());
                Log.log(Log.ERROR, this, e);
            }
        }
        Log.log(Log.DEBUG, this, "Indexed " + words.size() + " words");
    }

    /**
     * Indexes all HTML and text files in the specified directory.
     * @param dir The directory
     */
    public void indexDirectory(String dir) throws Exception {
        File directory = new File(dir);
        ArrayList exts = new ArrayList();
        exts.add("html");
        exts.add("txt");
        File[] files = directory.listFiles(new CustomFileFilter(exts, "Help Files"));
        for (int i = 0; i < files.length; i++) {
            indexURL(files[i]);
        }
    }

    /**
     * Indexes all HTML and text files in the specified JAR file.
     * @param jar The JAR file
     */
    public void indexJAR(ZipFile jar) throws Exception {
        Enumeration e = jar.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            String name = entry.getName();
            String lname = name.toLowerCase();
            if (lname.endsWith(".html")) {
                String url = "jeditresource:/" + MiscUtilities.getFileName(jar.getName()) + "!/" + name;
                Log.log(Log.DEBUG, this, url);
                indexStream(jar.getInputStream(entry), url);
            }
        }
    }

    /**
     * Reads the specified HTML file and adds all words defined therein to the
     * index.
     * @param url The HTML file's URL
     */
    public void indexURL(String url) throws Exception {
        InputStream _in;
        if (MiscUtilities.isURL(url)) _in = new URL(url).openStream(); else {
            _in = new FileInputStream(url);
            url = "file:" + url;
        }
        indexStream(_in, url);
    }

    public Word lookupWord(String word) {
        Object o = words.get(word);
        if (o == IGNORE) return null; else return (Word) o;
    }

    public HelpFile getFile(int index) {
        return (HelpFile) files.get(index);
    }

    private static Object IGNORE = new Object();

    private HashMap words;

    private ArrayList files;

    private void ignoreWord(String word) {
        words.put(word, IGNORE);
    }

    /**
     * Reads the specified HTML file and adds all words defined therein to the
     * index.
     * @param _in The input stream
     * @param file The file
     */
    private void indexStream(InputStream _in, String fileName) throws Exception {
        HelpFile file = new HelpFile(fileName);
        files.add(file);
        int index = files.size() - 1;
        StringBuffer titleText = new StringBuffer();
        BufferedReader in = new BufferedReader(new InputStreamReader(_in));
        try {
            StringBuffer word = new StringBuffer();
            boolean insideTag = false;
            boolean insideEntity = false;
            boolean title = false;
            int c;
            while ((c = in.read()) != -1) {
                char ch = (char) c;
                if (insideTag) {
                    if (ch == '>') {
                        if (word.toString().equals("title")) title = true;
                        insideTag = false;
                        word.setLength(0);
                    } else word.append(ch);
                } else if (insideEntity) {
                    if (ch == ';') insideEntity = false;
                } else if (ch == '<') {
                    if (title) title = false;
                    if (word.length() != 0) {
                        addWord(word.toString(), index, title);
                        word.setLength(0);
                    }
                    insideTag = true;
                } else if (ch == '&') insideEntity = true; else if (title) titleText.append(ch); else if (!Character.isLetterOrDigit(ch)) {
                    if (word.length() != 0) {
                        addWord(word.toString(), index, title);
                        word.setLength(0);
                    }
                } else word.append(ch);
            }
        } finally {
            in.close();
        }
        if (titleText.length() == 0) file.title = fileName; else file.title = titleText.toString();
    }

    private void addWord(String word, int file, boolean title) {
        word = word.toLowerCase();
        Object o = words.get(word);
        if (o == IGNORE) return;
        if (o == null) words.put(word, new Word(word, file, title)); else ((Word) o).addOccurrence(file, title);
    }

    static class Word {

        static final int TITLE_OCCUR = 10;

        String word;

        int occurCount = 0;

        Occurrence[] occurrences;

        Word(String word, int file, boolean title) {
            this.word = word;
            occurrences = new Occurrence[5];
            addOccurrence(file, title);
        }

        void addOccurrence(int file, boolean title) {
            for (int i = 0; i < occurCount; i++) {
                if (occurrences[i].file == file) {
                    occurrences[i].count += (title ? TITLE_OCCUR : 1);
                    return;
                }
            }
            if (occurCount >= occurrences.length) {
                Occurrence[] newOccur = new Occurrence[occurrences.length * 2];
                System.arraycopy(occurrences, 0, newOccur, 0, occurCount);
                occurrences = newOccur;
            }
            occurrences[occurCount++] = new Occurrence(file, title);
        }

        static class Occurrence {

            int file;

            int count;

            Occurrence(int file, boolean title) {
                this.file = file;
                this.count = (title ? TITLE_OCCUR : 1);
            }
        }
    }

    static class HelpFile {

        String file;

        String title;

        HelpFile(String file) {
            this.file = file;
        }

        public String toString() {
            return title;
        }

        public boolean equals(Object o) {
            if (o instanceof HelpFile) return ((HelpFile) o).file.equals(file); else return false;
        }
    }
}

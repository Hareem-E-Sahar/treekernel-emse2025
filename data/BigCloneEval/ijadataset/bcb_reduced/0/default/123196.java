import java.net.URLConnection;
import java.net.URL;
import java.net.Socket;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.Locale;

/**
 *  <i>Input</i>. This class provides methods for reading strings
 *  and numbers from standard input, file input, URL, and socket.
 *  <p>
 *  The Locale used is: language = English, country = US. This is consistent
 *  with the formatting conventions with Java floating-point literals,
 *  command-line arguments (via <tt>Double.parseDouble()</tt>)
 *  and standard output (via <tt>System.out.print()</tt>). It ensures that
 *  standard input works the number formatting used in the textbook.
 *  <p>
 *  For additional documentation, see <a href="http://www.cs.princeton.edu/introcs/31datatype">Section 3.1</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne.
 */
public final class In {

    private Scanner scanner;

    private String charsetName = "ISO-8859-1";

    private Locale usLocale = new Locale("en", "US");

    /**
     * Create an input stream for standard input.
     */
    public In() {
        scanner = new Scanner(System.in, charsetName);
        scanner.useLocale(usLocale);
    }

    /**
     * Create an input stream from a socket.
     */
    public In(Socket socket) {
        try {
            InputStream is = socket.getInputStream();
            scanner = new Scanner(is, charsetName);
            scanner.useLocale(usLocale);
        } catch (IOException ioe) {
            System.err.println("Could not open " + socket);
        }
    }

    /**
     * Create an input stream from a URL.
     */
    public In(URL url) {
        try {
            URLConnection site = url.openConnection();
            InputStream is = site.getInputStream();
            scanner = new Scanner(is, charsetName);
            scanner.useLocale(usLocale);
        } catch (IOException ioe) {
            System.err.println("Could not open " + url);
        }
    }

    /**
     * Create an input stream from a file or web page.
     */
    public In(String s) {
        try {
            File file = new File(s);
            if (file.exists()) {
                scanner = new Scanner(file, charsetName);
                scanner.useLocale(usLocale);
                return;
            }
            URL url = getClass().getResource(s);
            if (url == null) {
                url = new URL(s);
            }
            URLConnection site = url.openConnection();
            InputStream is = site.getInputStream();
            scanner = new Scanner(is, charsetName);
            scanner.useLocale(usLocale);
        } catch (IOException ioe) {
            System.err.println("Could not open " + s);
        }
    }

    /**
     * Does the input stream exist?
     */
    public boolean exists() {
        return scanner != null;
    }

    /**
     * Is the input stream empty?
     */
    public boolean isEmpty() {
        return !scanner.hasNext();
    }

    /**
     * Read and return the next line.
     */
    public String readLine() {
        String line = null;
        try {
            line = scanner.nextLine();
        } catch (Exception e) {
        }
        return line;
    }

    /**
     * Read and return the next character.
     */
    public char readChar() {
        String s = scanner.findWithinHorizon("(?s).", 1);
        return s.charAt(0);
    }

    /**
     * Read and return the remainder of the input as a string.
     */
    public String readAll() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.useDelimiter("\\A").next();
    }

    /**
     * Return the next string from the input stream.
     */
    public String readString() {
        return scanner.next();
    }

    /**
     * Return the next int from the input stream.
     */
    public int readInt() {
        return scanner.nextInt();
    }

    /**
     * Return the next double from the input stream.
     */
    public double readDouble() {
        return scanner.nextDouble();
    }

    /**
     * Return the next float from the input stream.
     */
    public double readFloat() {
        return scanner.nextFloat();
    }

    /**
     * Return the next long from the input stream.
     */
    public long readLong() {
        return scanner.nextLong();
    }

    /**
     * Return the next byte from the input stream.
     */
    public byte readByte() {
        return scanner.nextByte();
    }

    /**
     * Return the next boolean from the input stream, allowing "true" or "1"
     * for true and "false" or "0" for false.
     */
    public boolean readBoolean() {
        String s = readString();
        if (s.equalsIgnoreCase("true")) return true;
        if (s.equalsIgnoreCase("false")) return false;
        if (s.equals("1")) return true;
        if (s.equals("0")) return false;
        throw new java.util.InputMismatchException();
    }

    /**
     * Close the input stream.
     */
    public void close() {
        scanner.close();
    }

    /**
     * Test client.
     */
    public static void main(String[] args) {
        In in;
        String urlName = "http://www.cs.princeton.edu/IntroCS/stdlib/InTest.txt";
        System.out.println("readAll() from URL " + urlName);
        System.out.println("---------------------------------------------------------------------------");
        try {
            in = new In(urlName);
            System.out.println(in.readAll());
        } catch (Exception e) {
        }
        System.out.println();
        System.out.println("readLine() from URL " + urlName);
        System.out.println("---------------------------------------------------------------------------");
        try {
            in = new In(urlName);
            while (!in.isEmpty()) {
                String s = in.readLine();
                System.out.println(s);
            }
        } catch (Exception e) {
        }
        System.out.println();
        System.out.println("readString() from URL " + urlName);
        System.out.println("---------------------------------------------------------------------------");
        try {
            in = new In(urlName);
            while (!in.isEmpty()) {
                String s = in.readString();
                System.out.println(s);
            }
        } catch (Exception e) {
        }
        System.out.println();
        System.out.println("readLine() from current directory");
        System.out.println("---------------------------------------------------------------------------");
        try {
            in = new In("./InTest.txt");
            while (!in.isEmpty()) {
                String s = in.readLine();
                System.out.println(s);
            }
        } catch (Exception e) {
        }
        System.out.println();
        System.out.println("readLine() from relative path");
        System.out.println("---------------------------------------------------------------------------");
        try {
            in = new In("../stdlib/InTest.txt");
            while (!in.isEmpty()) {
                String s = in.readLine();
                System.out.println(s);
            }
        } catch (Exception e) {
        }
        System.out.println();
        System.out.println("readChar() from file");
        System.out.println("---------------------------------------------------------------------------");
        try {
            in = new In("InTest.txt");
            while (!in.isEmpty()) {
                char c = in.readChar();
                System.out.print(c);
            }
        } catch (Exception e) {
        }
        System.out.println();
        System.out.println();
        System.out.println("readLine() from absolute OS X / Linux path");
        System.out.println("---------------------------------------------------------------------------");
        in = new In("/n/fs/csweb/introcs/stdlib/InTest.txt");
        try {
            while (!in.isEmpty()) {
                String s = in.readLine();
                System.out.println(s);
            }
        } catch (Exception e) {
        }
        System.out.println();
        System.out.println("readLine() from absolute Windows path");
        System.out.println("---------------------------------------------------------------------------");
        try {
            in = new In("G:\\www\\introcs\\stdlib\\InTest.txt");
            while (!in.isEmpty()) {
                String s = in.readLine();
                System.out.println(s);
            }
            System.out.println();
        } catch (Exception e) {
        }
        System.out.println();
    }
}

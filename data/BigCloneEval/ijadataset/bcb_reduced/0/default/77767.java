import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Unistrok {

    static final int MAXMATCHES = 5;

    BufferedReader reader;

    BufferedWriter writer;

    ArrayList<Character> characters;

    public class Character {

        protected int codepoint;

        protected int filters;

        public Vector<Integer> strokes;

        public Character() {
            strokes = new Vector<Integer>();
        }

        public void addStroke(int i) {
            strokes.add(new Integer(i));
        }

        public String toString() {
            String toReturn = Integer.toHexString(codepoint) + " " + (char) codepoint + " | ";
            for (Integer stroke : strokes) {
                toReturn += " " + stroke.toString();
            }
            if (filters != 0) {
                toReturn += " | " + filters;
            }
            return toReturn;
        }
    }

    public Unistrok() {
        this.characters = new ArrayList<Character>(1000);
    }

    public Unistrok(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        this.characters = loadFile(reader);
        reader.close();
    }

    public void saveFile(String filename) throws IOException {
    }

    /**
     * Returns the unistrok file unsorted.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public ArrayList<Character> loadFile(BufferedReader file) throws IOException {
        String line;
        ArrayList<Character> characters = new ArrayList<Character>(1000);
        line = file.readLine();
        while ((line = file.readLine()) != null) {
            Character currentCharacter = new Character();
            if (line.length() == 0) continue;
            if (line.charAt(0) == '#') continue;
            int pipe;
            String unicode = line.substring(0, line.indexOf(' '));
            line = line.substring(line.indexOf(" ") + 1);
            if (line.indexOf(" ") < 0) {
                continue;
            }
            line = line.substring(line.indexOf(" "));
            pipe = line.indexOf('|');
            if (pipe == -1) {
                continue;
            }
            currentCharacter.codepoint = Integer.parseInt(unicode, 16);
            line = line.substring(pipe + 1);
            String tokline, argline;
            int tokindex = line.indexOf('|');
            if (tokindex != -1) {
                tokline = line.substring(0, tokindex);
                argline = line.substring(tokindex + 1);
            } else {
                argline = null;
                tokline = line;
            }
            StringTokenizer st = new StringTokenizer(tokline);
            WhileLoop: while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                for (int i = 0; i < tok.length(); i++) {
                    switch(tok.charAt(i)) {
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            currentCharacter.addStroke(tok.charAt(i) - '0');
                            break;
                        case 'b':
                            currentCharacter.addStroke(62);
                            break;
                        case 'c':
                            currentCharacter.addStroke(26);
                            break;
                        case 'x':
                            currentCharacter.addStroke(21);
                            break;
                        case 'y':
                            currentCharacter.addStroke(23);
                            break;
                        case '|':
                            break WhileLoop;
                        default:
                            System.out.println("unknown symbol in kanji database: " + tok.charAt(i));
                            System.out.println(line);
                            continue;
                    }
                }
            }
            characters.add(currentCharacter);
        }
        return characters;
    }
}

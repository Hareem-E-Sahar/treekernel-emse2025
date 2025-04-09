import java.util.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author ljbuesch
 */
public class UniDict {

    Vector<Character> Characters;

    String cedictFilename;

    String unistrokFilename;

    String unihanFilename;

    /** Creates a new instance of UniDict */
    public UniDict() {
        Characters = new Vector<Character>(10000);
    }

    /**
     *Our XML format
     */
    public UniDict(String xmlFile) {
    }

    /**
     * This builds the XML structure for all of the characters.
     * @return
     */
    public org.w3c.dom.Document buildXML() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        org.w3c.dom.Document doc;
        try {
            db = dbf.newDocumentBuilder();
            DOMImplementation impl = db.getDOMImplementation();
            doc = impl.createDocument(null, "HanziRecognizer", null);
        } catch (Exception e) {
            return null;
        }
        Element root = doc.getDocumentElement();
        Element characters = doc.createElement("Characters");
        root.appendChild(characters);
        for (Character currentCharacter : this.Characters) {
            Element character = doc.createElement("Character");
            Element traditional = doc.createElement("Traditional");
            Element simplified = doc.createElement("Simplified");
            Element t_codepoint = doc.createElement("Codepoint");
            Element t_radical = doc.createElement("Radical");
            Element s_codepoint = doc.createElement("Codepoint");
            Element s_radical = doc.createElement("Radical");
            Element pronunciation = doc.createElement("Pronunciation");
            Element mandarin = doc.createElement("Mandarin");
            Element cantonese = doc.createElement("Cantonese");
            Element definitions = doc.createElement("Definitions");
            Element strokes = doc.createElement("Strokes");
            character.appendChild(traditional);
            character.appendChild(simplified);
            character.appendChild(pronunciation);
            character.appendChild(definitions);
            character.appendChild(strokes);
            if (currentCharacter.traditional != 0) {
                t_codepoint.appendChild(doc.createTextNode(String.valueOf(currentCharacter.traditional)));
            }
            if (currentCharacter.simplified != 0) {
                s_codepoint.appendChild(doc.createTextNode(String.valueOf(currentCharacter.simplified)));
            }
            traditional.appendChild(t_codepoint);
            traditional.appendChild(t_radical);
            simplified.appendChild(s_codepoint);
            simplified.appendChild(s_radical);
            if (currentCharacter.mandarin != "") {
                mandarin.appendChild(doc.createTextNode(currentCharacter.mandarin));
            }
            if (currentCharacter.cantonese != "") {
                cantonese.appendChild(doc.createTextNode(currentCharacter.cantonese));
            }
            pronunciation.appendChild(cantonese);
            pronunciation.appendChild(mandarin);
            characters.appendChild(character);
        }
        return doc;
    }

    /**
     *Load with the three files
     */
    public UniDict(String cedict_file, String unistrok_file, String unihan_file) {
        Characters = new Vector<Character>(10000);
        cedictFilename = cedict_file;
        unistrokFilename = unistrok_file;
        unihanFilename = unihan_file;
        try {
            loadCEDict(cedict_file);
            loadUnistrok(unistrok_file);
            loadUnihan(unihan_file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void loadCEDict(String filename) throws IOException {
        cedictFilename = filename;
        FileInputStream fis = new FileInputStream(cedictFilename);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("#")) {
                continue;
            }
            Character c = new Character();
            StringTokenizer st = new StringTokenizer(line);
            String trad = st.nextToken();
            String simp = st.nextToken();
            String pron = line.substring(line.indexOf('[') + 1, line.lastIndexOf(']'));
            String def = line.substring(line.indexOf('/') + 1, line.lastIndexOf('/'));
            if (trad.length() == 1) {
                c.traditional = (char) trad.codePointAt(0);
            } else {
                continue;
            }
            if (simp.length() == 1) {
                c.simplified = (char) simp.codePointAt(0);
            } else {
                continue;
            }
            c.mandarin = pron;
            c.definition = def;
            c = this.getCharacter(c);
        }
    }

    public Character getCharacter(Character c) {
        int index;
        index = Characters.indexOf(c);
        if (index == -1) {
            Characters.add(c);
            return c;
        } else {
            return Characters.get(index);
        }
    }

    public void loadUnistrok(String filename) throws IOException {
        unistrokFilename = filename;
        BufferedReader reader = new BufferedReader(new FileReader(unistrokFilename));
        String line;
        boolean simplified = false;
        while ((line = reader.readLine()) != null) {
            Character currentCharacter = new Character();
            if (line.length() == 0) {
                continue;
            }
            if (line.charAt(0) == '#') {
                if (line.toLowerCase().contains("traditional")) {
                    simplified = false;
                } else if (line.toLowerCase().contains("simplified")) {
                    simplified = true;
                }
                continue;
            }
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
            if (simplified) {
                currentCharacter.simplified = (char) Integer.parseInt(unicode, 16);
            } else {
                currentCharacter.traditional = (char) Integer.parseInt(unicode, 16);
            }
            currentCharacter = this.getCharacter(currentCharacter);
            if (currentCharacter.strokes.size() > 0 && (currentCharacter.simplified == currentCharacter.traditional)) {
                continue;
            }
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
                            char c = tok.charAt(i);
                            currentCharacter.addStroke(c - '0', Double.MAX_VALUE);
                            break;
                        case 'b':
                            currentCharacter.addStroke(62, Double.MAX_VALUE);
                            break;
                        case 'c':
                            currentCharacter.addStroke(26, Double.MAX_VALUE);
                            break;
                        case 'x':
                            currentCharacter.addStroke(21, Double.MAX_VALUE);
                            break;
                        case 'y':
                            currentCharacter.addStroke(23, Double.MAX_VALUE);
                            break;
                        case '|':
                            break WhileLoop;
                        default:
                            continue;
                    }
                }
            }
        }
        reader.close();
    }

    public void writeUnistrok(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (Character character : Characters) {
            writer.newLine();
        }
        writer.close();
    }

    public void loadUnihan(String filename) throws IOException {
        unihanFilename = filename;
        FileInputStream fis = new FileInputStream(unihanFilename);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        String line;
        String unicode = "";
        Character unihan_character = new Character();
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            Character database_character = new Character();
            if (unicode.compareTo(line.substring(2, line.indexOf(9))) != 0 && unihan_character.mandarin != null) {
                if (unihan_character.simplified == 0 && unihan_character.traditional == 0) {
                    unihan_character.simplified = (char) Integer.parseInt(unicode, 16);
                    unihan_character.traditional = (char) Integer.parseInt(unicode, 16);
                }
                database_character = this.getCharacter(unihan_character);
                database_character.cantonese = unihan_character.cantonese;
                database_character.hdz = unihan_character.hdz;
                database_character.radical = unihan_character.radical;
                database_character.totalStrokes = unihan_character.totalStrokes;
                if (database_character.definition == null) {
                    database_character.definition = unihan_character.definition;
                }
                unihan_character = new Character();
            }
            unicode = line.substring(2, line.indexOf(9));
            String tag_name = line.substring(line.indexOf(9) + 1, line.lastIndexOf(9));
            String line_value = line.substring(line.lastIndexOf(9) + 1);
            if (tag_name.compareTo("kMandarin") == 0) {
                unihan_character.mandarin = line_value.toLowerCase();
            } else if (tag_name.compareTo("kCantonese") == 0) {
                unihan_character.cantonese = line_value.toLowerCase();
            } else if (tag_name.compareTo("kTotalStrokes") == 0) {
                unihan_character.totalStrokes = Integer.parseInt(line_value);
            } else if (tag_name.compareTo("kIRGHanyuDaZidian") == 0) {
                unihan_character.hdz = line_value;
            } else if (tag_name.compareTo("kRSUnicode") == 0) {
                unihan_character.radical = line_value;
            } else if (tag_name.compareTo("kDefinition") == 0) {
                unihan_character.definition = line_value;
            } else if (tag_name.compareTo("kSimplifiedVariant") == 0) {
                if (line_value.contains(" ")) {
                    unihan_character.simplified = (char) Integer.parseInt(line_value.substring(2, line_value.indexOf(' ')), 16);
                } else {
                    unihan_character.simplified = (char) Integer.parseInt(line_value.substring(2), 16);
                }
            } else if (tag_name.compareTo("kTraditionalVariant") == 0) {
                if (line_value.contains(" ")) {
                    unihan_character.traditional = (char) Integer.parseInt(line_value.substring(2, line_value.indexOf(' ')), 16);
                } else {
                    unihan_character.traditional = (char) Integer.parseInt(line_value.substring(2), 16);
                }
            }
        }
        reader.close();
    }

    public void loadUniDict(String filename) throws IOException {
    }

    public void writeUniDict(String filename) throws IOException {
    }

    String setPinyinUnicode(String syllable) {
        if (syllable.contains(":")) {
            syllable = syllable.replaceFirst("u", "ü");
        }
        if (syllable.indexOf('A') != -1) {
            int tone = new Integer(syllable.charAt(syllable.length() - 1) - '0');
            switch(tone) {
                case 1:
                    syllable = syllable.replaceFirst("A", "Ā");
                    break;
                case 2:
                    syllable = syllable.replaceFirst("A", "Á");
                    break;
                case 3:
                    syllable = syllable.replaceFirst("A", "Ǎ");
                    break;
                case 4:
                    syllable = syllable.replaceFirst("A", "À");
                    break;
                default:
                    break;
            }
        } else if (syllable.indexOf('a') != -1) {
            int tone = new Integer(syllable.charAt(syllable.length() - 1) - '0');
            switch(tone) {
                case 1:
                    syllable = syllable.replaceFirst("a", "ā");
                    break;
                case 2:
                    syllable = syllable.replaceFirst("a", "á");
                    break;
                case 3:
                    syllable = syllable.replaceFirst("a", "ǎ");
                    break;
                case 4:
                    syllable = syllable.replaceFirst("a", "à");
                    break;
                default:
                    break;
            }
        } else if (syllable.indexOf('E') != -1) {
            int tone = new Integer(syllable.charAt(syllable.length() - 1) - '0');
            switch(tone) {
                case 1:
                    syllable = syllable.replaceFirst("E", "Ē");
                    break;
                case 2:
                    syllable = syllable.replaceFirst("E", "É");
                    break;
                case 3:
                    syllable = syllable.replaceFirst("E", "Ě");
                    break;
                case 4:
                    syllable = syllable.replaceFirst("E", "È");
                    break;
                case 5:
                    break;
                default:
                    break;
            }
        } else if (syllable.indexOf('e') != -1) {
            int tone = new Integer(syllable.charAt(syllable.length() - 1) - '0');
            switch(tone) {
                case 1:
                    syllable = syllable.replaceFirst("e", "ē");
                    break;
                case 2:
                    syllable = syllable.replaceFirst("e", "é");
                    break;
                case 3:
                    syllable = syllable.replaceFirst("e", "ě");
                    break;
                case 4:
                    syllable = syllable.replaceFirst("e", "è");
                    break;
                default:
                    break;
            }
        } else if (syllable.indexOf("ou") != -1) {
            int tone = new Integer(syllable.charAt(syllable.length() - 1) - '0');
            switch(tone) {
                case 1:
                    syllable = syllable.replaceFirst("o", "ō");
                    break;
                case 2:
                    syllable = syllable.replaceFirst("o", "ó");
                    break;
                case 3:
                    syllable = syllable.replaceFirst("o", "ǒ");
                    break;
                case 4:
                    syllable = syllable.replaceFirst("o", "ò");
                    break;
                default:
                    break;
            }
        } else {
            int tone = new Integer(syllable.charAt(syllable.length() - 1) - '0');
            int location_of_o = syllable.indexOf('o');
            int location_of_i = syllable.indexOf('i');
            int location_of_u = syllable.indexOf('u');
            int location_of_v = syllable.indexOf(':');
            if (location_of_o > location_of_i && location_of_o > location_of_u) {
                switch(tone) {
                    case 1:
                        syllable = syllable.replaceFirst("o", "ō");
                        break;
                    case 2:
                        syllable = syllable.replaceFirst("o", "ó");
                        break;
                    case 3:
                        syllable = syllable.replaceFirst("o", "ǒ");
                        break;
                    case 4:
                        syllable = syllable.replaceFirst("o", "ò");
                        break;
                    default:
                        break;
                }
            } else if (location_of_i > location_of_o && location_of_i > location_of_u) {
                switch(tone) {
                    case 1:
                        syllable = syllable.replaceFirst("i", "ī");
                        break;
                    case 2:
                        syllable = syllable.replaceFirst("i", "í");
                        break;
                    case 3:
                        syllable = syllable.replaceFirst("i", "ǐ");
                        break;
                    case 4:
                        syllable = syllable.replaceFirst("i", "ì");
                        break;
                    default:
                        break;
                }
            } else if (location_of_u > location_of_o && location_of_u > location_of_i && location_of_v != -1) {
                switch(tone) {
                    case 1:
                        syllable = syllable.replaceFirst("ü", "ǖ");
                        break;
                    case 2:
                        syllable = syllable.replaceFirst("ü", "ǘ");
                        break;
                    case 3:
                        syllable = syllable.replaceFirst("ü", "ǚ");
                        break;
                    case 4:
                        syllable = syllable.replaceFirst("ü", "ǜ");
                        break;
                    default:
                        break;
                }
            } else if (location_of_u > location_of_o && location_of_u > location_of_i) {
                switch(tone) {
                    case 1:
                        syllable = syllable.replaceFirst("u", "ū");
                        break;
                    case 2:
                        syllable = syllable.replaceFirst("u", "ú");
                        break;
                    case 3:
                        syllable = syllable.replaceFirst("u", "ǔ");
                        break;
                    case 4:
                        syllable = syllable.replaceFirst("u", "ù");
                        break;
                    default:
                        break;
                }
            } else if (location_of_u == -1 && location_of_i == -1 && location_of_o == -1 && syllable.indexOf('O') != -1) {
                switch(tone) {
                    case 1:
                        syllable = syllable.replaceFirst("O", "Ō");
                        break;
                    case 2:
                        syllable = syllable.replaceFirst("O", "Ó");
                        break;
                    case 3:
                        syllable = syllable.replaceFirst("O", "Ǒ");
                        break;
                    case 4:
                        syllable = syllable.replaceFirst("O", "Ò");
                        break;
                    default:
                        break;
                }
            }
        }
        if (syllable.endsWith("1") || syllable.endsWith("2") || syllable.endsWith("3") || syllable.endsWith("4") || syllable.endsWith("5")) {
            syllable = syllable.substring(0, syllable.length() - 1);
        }
        if (syllable.contains(":")) {
            syllable = syllable.substring(0, syllable.length() - 1);
        }
        return syllable;
    }

    public Vector<String> getCompoudCharacterWords(Vector<Character> characters) {
        Vector<String> dictionary_entries = new Vector<String>();
        BufferedReader reader;
        String line;
        String hanzi_sequence = "";
        for (Character next_character : characters) {
            hanzi_sequence += next_character.traditional;
        }
        try {
            FileInputStream fis = new FileInputStream(cedictFilename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(isr);
            while ((line = reader.readLine()) != null) {
                String line_hanzi = line.substring(0, line.indexOf('['));
                if (line_hanzi.contains(hanzi_sequence)) {
                    String formatted_line = "";
                    formatted_line += line.substring(0, line.indexOf('['));
                    formatted_line += line.substring(line.indexOf('[') + 1, line.indexOf(']')) + " ";
                    formatted_line += line.substring(line.indexOf('/') + 1, line.lastIndexOf('/'));
                    dictionary_entries.add(formatted_line);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return dictionary_entries;
    }

    public Vector<Character> findCharacterByPinyin(String pinyin) {
        Vector<Character> character_matches = new Vector<Character>();
        if (Characters.isEmpty()) {
            System.out.println("Characters not defined yet!!!");
        }
        if (pinyin.contains("v")) {
            pinyin = pinyin.replaceFirst("v", "u") + ":";
        }
        for (Character current_char : Characters) {
            if (current_char.mandarin != null) {
                if (!current_char.mandarin.isEmpty() && !pinyin.isEmpty() && current_char.mandarin.equalsIgnoreCase(pinyin)) {
                    character_matches.add(current_char);
                }
            }
        }
        if (!(pinyin.endsWith("1") || pinyin.endsWith("2") || pinyin.endsWith("3") || pinyin.endsWith("4") || pinyin.endsWith("5"))) {
            character_matches.addAll(findCharacterByPinyin(pinyin + "5"));
            character_matches.addAll(findCharacterByPinyin(pinyin + "1"));
            character_matches.addAll(findCharacterByPinyin(pinyin + "2"));
            character_matches.addAll(findCharacterByPinyin(pinyin + "3"));
            character_matches.addAll(findCharacterByPinyin(pinyin + "4"));
        }
        return character_matches;
    }

    public Vector<Character> findCharacterByDefinition(String user_definition) {
        Vector<Character> character_matches = new Vector<Character>();
        user_definition = user_definition.trim();
        for (Character current_char : Characters) {
            if (current_char.definition != null) {
                StringTokenizer st = new StringTokenizer(current_char.definition, "/;,");
                String subdefinition;
                while (st.hasMoreTokens()) {
                    subdefinition = st.nextToken();
                    if (subdefinition.contains("(")) {
                        int index_of_paren = subdefinition.indexOf("(");
                        subdefinition = subdefinition.substring(0, index_of_paren);
                    }
                    subdefinition = subdefinition.trim();
                    if (subdefinition.equalsIgnoreCase(user_definition)) {
                        character_matches.add(current_char);
                    }
                }
            }
        }
        return character_matches;
    }

    public Vector<String> findUnidictEntryByDefinition(String definition) {
        Vector<String> dictionary_entries = new Vector<String>();
        BufferedReader reader;
        String line;
        try {
            FileInputStream fis = new FileInputStream(cedictFilename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(isr);
            while ((line = reader.readLine()) != null) {
                String line_definition = line.substring(line.indexOf('/') + 1, line.lastIndexOf('/'));
                if (line_definition.contains(definition)) {
                    String formatted_line = "";
                    formatted_line += line.substring(0, line.indexOf('['));
                    formatted_line += line.substring(line.indexOf('[') + 1, line.indexOf(']')) + " ";
                    formatted_line += line.substring(line.indexOf('/') + 1, line.lastIndexOf('/'));
                    dictionary_entries.add(formatted_line);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return dictionary_entries;
    }
}

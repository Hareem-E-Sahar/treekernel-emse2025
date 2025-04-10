package diet.server.conversationhistory;

import java.io.Serializable;
import java.util.Vector;
import diet.server.conversationhistory.turn.Turn;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * This stores all words (and parts of speech) used by participants. It stores separately whether participant
 * was the origin of the recipient.
 * 
 * @author user
 */
public class PartOfSpeechLexicon implements Serializable {

    private Vector lexicon = new Vector();

    /** Creates a new instance of PartOfSpeechLexicon */
    public PartOfSpeechLexicon() {
    }

    public void addWordsToLexicon(Turn t, Vector taggedWords, Conversant sender, Vector recipients) {
        Vector wordsAsLexicalEntries = new Vector();
        for (int i = 0; i < taggedWords.size(); i++) {
            TaggedWord tw = (TaggedWord) taggedWords.elementAt(i);
            LexiconEntry le = this.binarySearchForWordAndPartOfSpeechAndUpdate(tw);
            le.addNewOccurrenceOfWord(t, sender, recipients);
            wordsAsLexicalEntries.addElement(le);
        }
        t.setWordsAsLexicalEntries(wordsAsLexicalEntries);
    }

    public void addStringsToLexicon(Turn t, Vector untaggedWords, Conversant sender, Vector recipients) {
        Vector wordsAsLexicalEntries = new Vector();
        for (int i = 0; i < untaggedWords.size(); i++) {
            String s = (String) untaggedWords.elementAt(i);
            LexiconEntry le = this.binarySearchForWordAndPartOfSpeechAndUpdate(s);
            le.addNewOccurrenceOfWord(t, sender, recipients);
            wordsAsLexicalEntries.addElement(le);
        }
        t.setWordsAsLexicalEntries(wordsAsLexicalEntries);
    }

    public void searchAndUpdate(TaggedWord tw) {
        LexiconEntry le = this.binarySearchForWordAndPartOfSpeechAndUpdate(tw);
        for (int i = 0; i < lexicon.size(); i++) {
            LexiconEntry le2 = (LexiconEntry) lexicon.elementAt(i);
        }
    }

    public void printLexicon() {
    }

    public void printLexiconDummy() {
        for (int i = 0; i < lexicon.size(); i++) {
            LexiconEntry le2 = (LexiconEntry) lexicon.elementAt(i);
            System.out.println(i + ": " + le2.getWPT());
        }
    }

    private LexiconEntry binarySearchForWordAndPartOfSpeechAndUpdate(String s) {
        TaggedWord tw = new TaggedWord(s);
        tw.setTag("NON");
        return binarySearchForWordAndPartOfSpeechAndUpdate(tw);
    }

    private LexiconEntry binarySearchForWordAndPartOfSpeechAndUpdate(TaggedWord tw) {
        int lowestPossibleLoc = 0;
        int highestPossibleLoc = lexicon.size() - 1;
        int middle = (lowestPossibleLoc + highestPossibleLoc) / 2;
        while (highestPossibleLoc >= lowestPossibleLoc) {
            middle = (lowestPossibleLoc + highestPossibleLoc) / 2;
            LexiconEntry lxe = (LexiconEntry) lexicon.elementAt(middle);
            int comparator = (tw.word() + tw.tag()).compareToIgnoreCase(lxe.getWord() + lxe.getPartOfSpeech());
            if (comparator == 0) {
                return lxe;
            } else if (comparator < 0) {
                highestPossibleLoc = middle - 1;
            } else {
                lowestPossibleLoc = middle + 1;
            }
        }
        LexiconEntry le = new LexiconEntry(tw.word(), tw.tag());
        lexicon.insertElementAt(le, lowestPossibleLoc);
        return le;
    }

    public Vector getLexicon() {
        return lexicon;
    }
}

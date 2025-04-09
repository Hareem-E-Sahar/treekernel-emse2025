package diet.server.conversationhistory;

import java.io.Serializable;
import java.util.Vector;
import diet.server.conversationhistory.turn.Turn;

/**
 * This is the representation of all the participant. It is 
 * @author user
 */
public class Conversant implements Serializable {

    private String username;

    private Vector turnsProduced = new Vector();

    private Vector turnsReceived = new Vector();

    private Vector wordsUsed = new Vector();

    private Vector wordsReceived = new Vector();

    public Vector getWordsReceived() {
        return wordsReceived;
    }

    public Vector getWordsUsed() {
        return wordsUsed;
    }

    public Conversant(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void addTurnProduced(Turn t) {
        try {
            turnsProduced.addElement(t);
            Vector lexicalEntries = t.getWordsAsLexicalEntries();
            for (int i = 0; i < lexicalEntries.size(); i++) {
                LexiconEntry lxe = (LexiconEntry) lexicalEntries.elementAt(i);
                binarySearchForWordAndPartOfSpeechOccurrenceCounterAndUpdateSender(t, lxe);
            }
        } catch (Exception e) {
            System.err.println("Error in addTurnProduced of participant " + this.wordsReceived + " message: " + t.getTextString());
        }
    }

    public void addTurnReceived(Turn t) {
        try {
            turnsReceived.addElement(t);
            Vector lexicalEntries = t.getWordsAsLexicalEntries();
            for (int i = 0; i < lexicalEntries.size(); i++) {
                LexiconEntry lxe = (LexiconEntry) lexicalEntries.elementAt(i);
                binarySearchForWordAndPartOfSpeechOccurrenceCounterAndUpdateRecipient(t, lxe);
            }
        } catch (Exception e) {
            System.err.println("Error in addTurnProduced of participant " + this.wordsReceived + " message: " + t.getTextString());
        }
    }

    private void binarySearchForWordAndPartOfSpeechOccurrenceCounterAndUpdateSender(Turn t, LexiconEntry lx1) {
        int lowestPossibleLoc = 0;
        int highestPossibleLoc = wordsUsed.size() - 1;
        int middle = (lowestPossibleLoc + highestPossibleLoc) / 2;
        while (highestPossibleLoc >= lowestPossibleLoc) {
            middle = (lowestPossibleLoc + highestPossibleLoc) / 2;
            WordPartOfSpeechOccurrenceCounterSender wposocc = (WordPartOfSpeechOccurrenceCounterSender) wordsUsed.elementAt(middle);
            LexiconEntry lxe = wposocc.getLexEntry();
            int comparator = (lx1.getWord() + lx1.getPartOfSpeech()).compareToIgnoreCase(lxe.getWord() + lxe.getPartOfSpeech());
            if (comparator == 0) {
                wposocc.update(t);
                return;
            } else if (comparator < 0) {
                highestPossibleLoc = middle - 1;
            } else {
                lowestPossibleLoc = middle + 1;
            }
        }
        WordPartOfSpeechOccurrenceCounterSender wposoccc = new WordPartOfSpeechOccurrenceCounterSender(t, lx1);
        wordsUsed.insertElementAt(wposoccc, lowestPossibleLoc);
    }

    private void binarySearchForWordAndPartOfSpeechOccurrenceCounterAndUpdateRecipient(Turn t, LexiconEntry lx1) {
        int lowestPossibleLoc = 0;
        int highestPossibleLoc = wordsReceived.size() - 1;
        int middle = (lowestPossibleLoc + highestPossibleLoc) / 2;
        while (highestPossibleLoc >= lowestPossibleLoc) {
            middle = (lowestPossibleLoc + highestPossibleLoc) / 2;
            WordPartOfSpeechOccurrenceCounterRecipient wposoccr = (WordPartOfSpeechOccurrenceCounterRecipient) wordsReceived.elementAt(middle);
            LexiconEntry lxe = wposoccr.getLexEntry();
            int comparator = (lx1.getWord() + lx1.getPartOfSpeech()).compareToIgnoreCase(lxe.getWord() + lxe.getPartOfSpeech());
            if (comparator == 0) {
                wposoccr.update(t);
                return;
            } else if (comparator < 0) {
                highestPossibleLoc = middle - 1;
            } else {
                lowestPossibleLoc = middle + 1;
            }
        }
        WordPartOfSpeechOccurrenceCounterRecipient wposoccr = new WordPartOfSpeechOccurrenceCounterRecipient(t, lx1, this);
        wordsReceived.insertElementAt(wposoccr, lowestPossibleLoc);
    }

    public Vector getTurnsProduced() {
        return this.turnsProduced;
    }

    public Vector getTurnsReceived() {
        return this.turnsReceived;
    }
}

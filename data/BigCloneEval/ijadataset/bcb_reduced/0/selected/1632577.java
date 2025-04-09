package tdomhan.addict.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;
import com.ibm.icu.text.CollationElementIterator;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

/**
 * contains the translation of a word in another one.
 * @author tobi
 *
 */
public class Translation implements Comparable<Translation>, Serializable {

    private static final long serialVersionUID = 685441859533705478L;

    /**
	 * possible genders of the word
	 * @author tobi
	 *
	 */
    public enum Gender {

        UNKOWN, MASCULINE, FEMININE, NEUTER, COMMON
    }

    ;

    public enum TranslationDirection {

        FIRST_SECOND_LANGUAGE, SECOND_FIRST_LANGUAGE
    }

    ;

    public Translation() {
        this.firstLanguage = "";
        this.firstLangGender = Gender.UNKOWN;
        this.secondLanguage = "";
        this.secondLangGender = Gender.UNKOWN;
        this.meaning = "";
    }

    public Translation(String firstLanguage, Gender firstLangGender, String secondLanguage, Gender secondLangGender, String meaning, WordType type) {
        super();
        this.firstLanguage = firstLanguage;
        this.firstLangGender = firstLangGender;
        this.secondLanguage = secondLanguage;
        this.secondLangGender = secondLangGender;
        this.meaning = meaning;
        this.type = type;
    }

    public Translation(Translation translation) {
        this.firstLanguage = translation.getFirstLanguage();
        this.firstLangGender = translation.getFirstLangGender();
        this.secondLanguage = translation.getSecondLanguage();
        this.secondLangGender = translation.getSecondLangGender();
        this.meaning = translation.getMeaning();
        this.type = translation.getType();
    }

    volatile String firstLanguage;

    int firstLanguageIdx = -1;

    Gender firstLangGender;

    volatile String secondLanguage;

    int secondLanguageIdx = -1;

    Gender secondLangGender;

    String meaning;

    WordType type = WordType.UNKOWN;

    static TranslationDirection translationDirection = TranslationDirection.FIRST_SECOND_LANGUAGE;

    static Collator firstLangColl = null;

    static Collator secondLangColl = null;

    /**
	 * all unique first language words
	 */
    static Vector<String> uniqueFirstLangWords;

    /**
	 * all unique second language words
	 */
    static Vector<String> uniqueSecondLangWords;

    /**
	 * index at which a word from uniqueFirstLangWords appears in
	 * the translations array the first time
	 */
    static int[] firstLangAppearIdx;

    /**
	 * index at which a word from uniqueSecondLangWords appears in
	 * the translations array the first time
	 */
    static int[] secondLangAppearIdx;

    /**
	 * get the index of the first appearance of the given string in the list of translations
	 * @param x
	 * @return
	 */
    public static int search(String x) {
        RuleBasedCollator coll;
        Vector<String> uniqueWords;
        int[] appearIdx;
        if (translationDirection == TranslationDirection.FIRST_SECOND_LANGUAGE) {
            coll = (RuleBasedCollator) firstLangColl;
            uniqueWords = uniqueFirstLangWords;
            appearIdx = firstLangAppearIdx;
        } else {
            coll = (RuleBasedCollator) secondLangColl;
            uniqueWords = uniqueSecondLangWords;
            appearIdx = secondLangAppearIdx;
        }
        int low = 0;
        int high = uniqueWords.size() - 1;
        int mid;
        int tries = 0;
        int beginsWithIdx = -1;
        while (low <= high) {
            tries++;
            mid = (low + high) / 2;
            String midTranslationText = uniqueWords.get(mid);
            int compResult = coll.compare(midTranslationText, x);
            if (compResult < 0) {
                low = mid + 1;
            } else if (compResult > 0) {
                CollationElementIterator transTextIt = coll.getCollationElementIterator(midTranslationText);
                CollationElementIterator searchTextIt = coll.getCollationElementIterator(x);
                int transTextElem = transTextIt.next();
                int searchTextElem = searchTextIt.next();
                boolean beginsWith = false;
                while (transTextElem != CollationElementIterator.NULLORDER && searchTextElem != CollationElementIterator.NULLORDER) {
                    if (transTextElem != searchTextElem) {
                        beginsWith = false;
                        break;
                    } else {
                        beginsWith = true;
                    }
                    transTextElem = transTextIt.next();
                    searchTextElem = searchTextIt.next();
                }
                if (beginsWith) beginsWithIdx = mid;
                high = mid - 1;
            } else {
                System.out.println("lookup took " + tries + " tries.");
                return (mid > 0 && mid < appearIdx.length) ? appearIdx[mid] : -1;
            }
        }
        return (beginsWithIdx > 0 && beginsWithIdx < appearIdx.length) ? appearIdx[beginsWithIdx] : -1;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (type == WordType.VERB) {
            str.append("to ");
        }
        str.append(getFirstLanguage());
        if (type == WordType.ADJECTIVE) {
            str.append(" {adj.}");
        } else if (type == WordType.ADVERB) {
            str.append(" {adv.}");
        }
        str.append(" | ");
        str.append(getSecondLanguage());
        str.append(" | ");
        str.append(meaning);
        str.append("\n");
        return str.toString();
    }

    /**
	 * WARNING: be careful changing the comparing logic! other classes rely on a certain one!
	 */
    @Override
    public int compareTo(Translation o) {
        String source;
        String destination;
        Collator coll;
        if (translationDirection == TranslationDirection.FIRST_SECOND_LANGUAGE) {
            source = getFirstLanguage();
            destination = o.getFirstLanguage();
            coll = firstLangColl;
        } else {
            source = getSecondLanguage();
            destination = o.getSecondLanguage();
            coll = secondLangColl;
        }
        if (coll != null) {
            if (!coll.equals(source, destination)) {
                return coll.compare(source, destination);
            } else {
                if (!type.equals(o.type)) {
                    return type.compareTo(o.type);
                } else {
                    return meaning.compareTo(o.meaning);
                }
            }
        } else if (!source.equals(destination)) {
            return source.compareTo(destination);
        } else {
            if (!type.equals(o.type)) {
                return type.compareTo(o.type);
            } else {
                return meaning.compareTo(o.meaning);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        Collator coll;
        if (translationDirection == TranslationDirection.FIRST_SECOND_LANGUAGE) {
            coll = firstLangColl;
        } else {
            coll = secondLangColl;
        }
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Translation other = (Translation) obj;
        if (firstLangGender != other.firstLangGender) return false;
        String firstLanguage = getFirstLanguage();
        String secondLanguage = getSecondLanguage();
        if (firstLanguage == null) {
            if (other.getFirstLanguage() != null) return false;
        } else if (!coll.equals(getFirstLanguage(), other.getFirstLanguage())) return false;
        if (meaning == null) {
            if (other.meaning != null) return false;
        } else if (!coll.equals(meaning, other.meaning)) return false;
        if (secondLangGender != other.secondLangGender) return false;
        if (secondLanguage == null) {
            if (other.getSecondLanguage() != null) return false;
        } else if (!coll.equals(getSecondLanguage(), other.getSecondLanguage())) return false;
        if (type != other.type) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firstLangGender == null) ? 0 : firstLangGender.hashCode());
        result = prime * result + ((getFirstLanguage() == null) ? 0 : getFirstLanguage().hashCode());
        result = prime * result + ((meaning == null) ? 0 : meaning.hashCode());
        result = prime * result + ((secondLangGender == null) ? 0 : secondLangGender.hashCode());
        result = prime * result + ((getSecondLanguage() == null) ? 0 : getSecondLanguage().hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
	 * replace the translations string with a reference to the unique words of each language.
	 */
    public void createStaticWordReference() {
        if ((firstLanguageIdx = uniqueFirstLangWords.indexOf(firstLanguage)) != -1) {
            firstLanguage = null;
        } else {
            throw new RuntimeException();
        }
        if ((secondLanguageIdx = uniqueSecondLangWords.indexOf(secondLanguage)) != -1) {
            secondLanguage = null;
        } else {
            throw new RuntimeException();
        }
    }

    /**
	 * @param firstLangColl the firstLangColl to set
	 */
    public static void setFirstLangCollator(Collator firstLangColl) {
        Translation.firstLangColl = firstLangColl;
    }

    public void writeTo(BufferedWriter out) throws IOException {
        out.write(Integer.toString(firstLanguageIdx));
        out.newLine();
        out.write(Integer.toString(secondLanguageIdx));
        out.newLine();
        out.write(Integer.toString(firstLangGender.ordinal()));
        out.newLine();
        out.write(Integer.toString(secondLangGender.ordinal()));
        out.newLine();
        out.write(Integer.toString(type.ordinal()));
        out.newLine();
        out.write(meaning);
        out.newLine();
    }

    public void readFrom(BufferedReader in) throws IOException {
        firstLanguageIdx = Integer.parseInt(in.readLine());
        secondLanguageIdx = Integer.parseInt(in.readLine());
        firstLangGender = Gender.values()[Integer.parseInt(in.readLine())];
        secondLangGender = Gender.values()[Integer.parseInt(in.readLine())];
        type = WordType.values()[Integer.parseInt(in.readLine())];
        meaning = in.readLine();
    }

    /**
	 * @return the translationDirection
	 */
    public static TranslationDirection getTranslationDirection() {
        return translationDirection;
    }

    /**
	 * @param translationDirection the translationDirection to set
	 */
    public static void setTranslationDirection(TranslationDirection translationDirection) {
        Translation.translationDirection = translationDirection;
    }

    /**
	 * @param secondLangColl the secondLangColl to set
	 */
    public static void setSecondLangCollator(Collator secondLangColl) {
        Translation.secondLangColl = secondLangColl;
    }

    /**
	 * @return the firstLanguage
	 */
    public String getFirstLanguage() {
        return (firstLanguageIdx != -1) ? uniqueFirstLangWords.get(firstLanguageIdx) : firstLanguage;
    }

    /**
	 * 
	 * @return the source language string depending on the translation direction.
	 */
    public String getSourceLanguage() {
        if (translationDirection.equals(TranslationDirection.FIRST_SECOND_LANGUAGE)) {
            return getFirstLanguage();
        } else if (translationDirection.equals(TranslationDirection.SECOND_FIRST_LANGUAGE)) {
            return getSecondLanguage();
        } else {
            throw new RuntimeException();
        }
    }

    /**
	 * 
	 * @return the destination language string depending on the translation direction.
	 */
    public String getDestinationLanguage() {
        if (translationDirection.equals(TranslationDirection.FIRST_SECOND_LANGUAGE)) {
            return getSecondLanguage();
        } else if (translationDirection.equals(TranslationDirection.SECOND_FIRST_LANGUAGE)) {
            return getFirstLanguage();
        } else {
            throw new RuntimeException();
        }
    }

    /**
	 * @param firstLanguage the firstLanguage to set
	 */
    public void setFirstLanguage(String firstLanguage) {
        this.firstLanguage = firstLanguage;
    }

    /**
	 * @return the firstLangGender
	 */
    public Gender getFirstLangGender() {
        return firstLangGender;
    }

    /**
	 * @param firstLangGender the firstLangGender to set
	 */
    public void setFirstLangGender(Gender firstLangGender) {
        this.firstLangGender = firstLangGender;
    }

    /**
	 * @return the secondLanguage
	 */
    public String getSecondLanguage() {
        return (secondLanguageIdx != -1) ? uniqueSecondLangWords.get(secondLanguageIdx) : secondLanguage;
    }

    /**
	 * @param secondLanguage the secondLanguage to set
	 */
    public void setSecondLanguage(String secondLanguage) {
        this.secondLanguage = secondLanguage;
    }

    /**
	 * @return the secondLangGender
	 */
    public Gender getSecondLangGender() {
        return secondLangGender;
    }

    /**
	 * @param secondLangGender the secondLangGender to set
	 */
    public void setSecondLangGender(Gender secondLangGender) {
        this.secondLangGender = secondLangGender;
    }

    /**
	 * @return the meaning
	 */
    public String getMeaning() {
        return meaning;
    }

    /**
	 * @param meaning the meaning to set
	 */
    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    /**
	 * @return the type
	 */
    public WordType getType() {
        return type;
    }

    /**
	 * @param type the type to set
	 */
    public void setType(WordType type) {
        this.type = type;
    }

    /**
	 * @return the uniqueFirstLangWords
	 */
    public static Vector<String> getUniqueFirstLangWords() {
        return uniqueFirstLangWords;
    }

    /**
	 * @param uniqueFirstLangWords the uniqueFirstLangWords to set
	 */
    public static void setUniqueFirstLangWords(Vector<String> uniqueFirstLangWords) {
        Translation.uniqueFirstLangWords = uniqueFirstLangWords;
    }

    /**
	 * @return the uniqueSecondLangWords
	 */
    public static Vector<String> getUniqueSecondLangWords() {
        return uniqueSecondLangWords;
    }

    /**
	 * @param uniqueSecondLangWords the uniqueSecondLangWords to set
	 */
    public static void setUniqueSecondLangWords(Vector<String> uniqueSecondLangWords) {
        Translation.uniqueSecondLangWords = uniqueSecondLangWords;
    }

    /**
	 * @param firstLanguageIdx the firstLanguageIdx to set
	 */
    public void setFirstLanguageIdx(int firstLanguageIdx) {
        this.firstLanguageIdx = firstLanguageIdx;
    }

    /**
	 * @param secondLanguageIdx the secondLanguageIdx to set
	 */
    public void setSecondLanguageIdx(int secondLanguageIdx) {
        this.secondLanguageIdx = secondLanguageIdx;
    }

    /**
	 * @param firstLangAppearIdx the firstLangAppearIdx to set
	 */
    public static void setFirstLangAppearIdx(int[] firstLangAppearIdx) {
        Translation.firstLangAppearIdx = firstLangAppearIdx;
    }

    /**
	 * @param secondLangAppearIdx the secondLangAppearIdx to set
	 */
    public static void setSecondLangAppearIdx(int[] secondLangAppearIdx) {
        Translation.secondLangAppearIdx = secondLangAppearIdx;
    }
}

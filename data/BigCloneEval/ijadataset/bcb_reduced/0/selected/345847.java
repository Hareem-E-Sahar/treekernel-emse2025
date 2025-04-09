package spacefaring.util;

/**
 * StringManipulator class offers functions
 * to manipulate Strings in several ways and
 * analyse them.
 * 
 * @author astrometric
 * @version 0.0.0x9 - 30.09.2005 - changed 17.01.2006
 */
public class StringManipulator {

    private static final char CR = '\r', LF = '\n', HT = '\t', FF = '\f', SP = ' ';

    private static final String CRLF = "\r\n";

    public static void toUpper(char[] chararr) {
        int len = chararr.length;
        for (int i = 0; i < len; i++) {
            chararr[i] = Character.toUpperCase(chararr[i]);
        }
    }

    public static void toLower(char[] chararr) {
        int len = chararr.length;
        for (int i = 0; i < len; i++) {
            chararr[i] = Character.toLowerCase(chararr[i]);
        }
    }

    public static void toInverted(char[] chararr) {
        int len = chararr.length;
        char uchar;
        for (int i = 0; i < len; i++) {
            uchar = Character.toUpperCase(chararr[i]);
            if (uchar == chararr[i]) {
                chararr[i] = Character.toLowerCase(chararr[i]);
            } else {
                chararr[i] = uchar;
            }
        }
    }

    public static void toProper(char[] chararr) {
        int len = chararr.length;
        char lastchar, thischar;
        lastchar = chararr[0];
        chararr[0] = Character.toUpperCase(lastchar);
        for (int i = 1; i < len; i++) {
            thischar = chararr[i];
            if ((!Character.isLetter(lastchar)) && Character.isLetter(thischar)) {
                chararr[i] = Character.toUpperCase(thischar);
            } else {
                chararr[i] = Character.toLowerCase(thischar);
            }
            lastchar = thischar;
        }
    }

    /**
     * Cleans a String from bad line termination formating.
     *
     * @see makeFeedCarrageString( char[] )
     */
    public static String makeFeedCarrageString(String str) {
        return makeFeedCarrageString(str.toCharArray());
    }

    /**
     * This function "cleans" a String from "bad" line
     * termination formating. It basically converts all
     * the single carrage returns and line feeds of a
     * given String to the combo "\r\n" so that the text
     * will be readable on most platforms.
     * 
     * @param  chararr   A String that is to be cleand 
     *                   from bad line termination formting.
     * @return           A String with better line
     *                   termination formating. Example:
     *                   "hello \n nasty" ->
     *                   "hello \r\n nasty" 
     */
    public static String makeFeedCarrageString(char[] chararr) {
        int crcount = getNumberOfCharsInString(chararr, CR);
        int lfcount = getNumberOfCharsInString(chararr, LF);
        boolean interm = false;
        int nontermcount = 0;
        StringBuilder strbuff = new StringBuilder(chararr.length + crcount + lfcount);
        if (chararr.length == 0) {
            return "";
        }
        if (chararr.length == 1) {
            if (isLineTerminatorChar(chararr[0])) {
                return CRLF;
            } else {
                return new String(chararr);
            }
        }
        for (int i = 0; i < (chararr.length - 1); i++) {
            if (isLineTerminatorChar(chararr[i])) {
                if (interm == false) {
                    strbuff.append(chararr, i - nontermcount, nontermcount);
                    nontermcount = 0;
                }
                if (isLineTerminatorChar(chararr[i + 1])) {
                    if (chararr[i] != chararr[i + 1]) {
                        i++;
                    }
                }
                strbuff.append(CRLF);
                interm = true;
            } else {
                interm = false;
                nontermcount++;
            }
        }
        if (isLineTerminatorChar(chararr[chararr.length - 1])) {
            strbuff.append(CRLF);
        } else {
            if (nontermcount > 0) {
                nontermcount++;
                strbuff.append(chararr, chararr.length - nontermcount, nontermcount);
            } else {
                strbuff.append(chararr[chararr.length - 1]);
            }
        }
        return strbuff.toString();
    }

    /**
     * This function reverses the character order of a String.
     * Basicly it writes the String backwards.
     * 
     * @param  str    A String that is to be reversed.
     * @return        A reversed String.
     */
    public static String reverseString(String str) {
        return new String(reverseString(str.toCharArray()));
    }

    /**
     * This function reverses the character order of a char
     * array. Basicly it writes the chars in the array backwards.
     * 
     * @param  chararr A String that is to be reversed.
     * @return         A reversed String.
     */
    public static char[] reverseString(char[] chararr) {
        int strlen = chararr.length;
        char[] newbuff = new char[strlen];
        int i;
        for (i = 0; i < (strlen - 1); i++) {
            if (isLineTerminatorChar(chararr[i]) && isLineTerminatorChar(chararr[i + 1]) && chararr[i] != chararr[i + 1]) {
                newbuff[strlen - i - 2] = chararr[i];
                newbuff[strlen - i - 1] = chararr[i + 1];
                i++;
            } else {
                newbuff[strlen - i - 1] = chararr[i];
            }
        }
        if (i == strlen - 1) {
            newbuff[0] = chararr[strlen - 1];
        }
        return newbuff;
    }

    public static void reverseCharsSimple(char[] chararr, int fromindex, int toindex) {
        int halfway = Math.round((fromindex + toindex) / 2) + 1;
        int i_op;
        char t;
        for (int i = fromindex; i < halfway; i++) {
            i_op = toindex - i + fromindex;
            t = chararr[i_op];
            chararr[i_op] = chararr[i];
            chararr[i] = t;
        }
    }

    /**
     * This method separated a String with the use of a 
     * space character (' '). In the return String a 
     * space has been placed between all the chars of
     * the original String.
     * 
     * @param  str         A String that is to be separated.
     * @return             The separated String. Example:
     *                     "expand" -> "e x p a n d"
     * @see separateStringEx(String,char)
     * @see inspandString(String)
     */
    public static String separateString(String str) {
        return new String(separateStringEx(str.toCharArray(), ' '));
    }

    /**
     * This method separates the characters in a String by 
     * placing the separation char between each character.
     * 
     * @param  str         A String that is to be separated.
     * @param  separation   The separation char to be used.
     * @return             The separated String. Example with ' ':
     *                     "expand" -> "e x p a n d"
     * @see separateString(String)
     */
    public static char[] separateStringEx(char[] chararr, char separation) {
        if (chararr == null) {
            return null;
        }
        int newlen = chararr.length * 2 - 1;
        char newstr[] = new char[newlen];
        int pos;
        newstr[0] = chararr[0];
        for (int i = 1; i < newlen; i += 2) {
            pos = (i + 1) / 2;
            newstr[i] = separation;
            newstr[i + 1] = chararr[pos];
        }
        return newstr;
    }

    public static char[] separateStringBeginning(char[] chararr, char separation, int charcount) {
        if (chararr == null || charcount < 1) {
            return null;
        }
        if (charcount > chararr.length) {
            charcount = chararr.length;
        }
        int newlen = charcount * 2 - 1;
        char newstr[] = new char[newlen];
        int pos;
        newstr[0] = chararr[0];
        for (int i = 1; i < newlen; i += 2) {
            pos = (i + 1) / 2;
            newstr[i] = separation;
            newstr[i + 1] = chararr[pos];
        }
        return newstr;
    }

    /**
     * This method creates a String object made up only of the characters
     * passed through the parameter.
     * Example: makeCharRepetition('a',5) returns "aaaaa"
     * 
     * @param  c       the character that is to be repeated
     * @param  repeat  number of repetition times
     * @return         a String containing only character of c
     */
    public static String makeCharRepetition(char c, int repeat) {
        char[] charrep = new char[repeat];
        for (int i = 0; i < repeat; i++) charrep[i] = c;
        return new String(charrep);
    }

    /**
     * This method adds a certain amount of Zeros to a String
     * Example: addPrefixZeros( "23", 5 ) returns "00023"
     *
     * @param  str   a String that is to be prefixed.
     * @param  fixlength   a number to what str is to be prefixed to
     * @return       a String with preceeding Zeros ('0')
     */
    public static String addPrefixZeros(String str, int fixlength) {
        if (str.length() < fixlength) {
            return makeCharRepetition('0', fixlength - str.length()) + str;
        } else return str;
    }

    /**
     * This method inverts a String so that all characters
     * that were upper case turn into lowercase and vice versa.
     * 
     * @param  chararr   an String that is to be modified.
     * @return       the inverted String - example: 
     *               " as DDS blD" -> " AS dds BLd"
     * @see invertStringCase(char[])
     */
    public static String invertStringCase(String str) {
        return invertStringCase(str.toCharArray());
    }

    /**
     * This method inverts a char array so that all characters
     * that were upper case turn into lowercase and vice versa.
     * 
     * @param  chararr   an array of chars that is to be modified
     * @return       the inverted String - example: 
     *               " as DDS blD" -> " AS dds BLd"
     * @see invertStringCase(String)
     */
    public static String invertStringCase(char[] chararr) {
        int i;
        int len = chararr.length;
        char uchar;
        char[] newstr = new char[len];
        for (i = 0; i < len; i++) {
            uchar = Character.toUpperCase(chararr[i]);
            if (uchar == chararr[i]) {
                newstr[i] = Character.toLowerCase(chararr[i]);
            } else {
                newstr[i] = uchar;
            }
        }
        return new String(newstr);
    }

    /**
     * This method modifies the words in a String so that all
     * start with a capital letter.
     * 
     * @param  str   a String that is to be modified
     * @return       the new modified String - example: 
     *               "make me proper" -> "Make Me Proper"
     * @see toProperCase(char[])
     */
    public static String toProperCase(String str) {
        return toProperCase(str.toCharArray());
    }

    /**
     * This method modifies the words in a char array so that
     * all start with a capital letter.
     * 
     * @param  str   a char array that is to be modified.
     * @return       the new modified char array - example: 
     *               "make me proper" -> "Make Me Proper"
     * @see toProperCase(String)
     */
    public static String toProperCase(char[] chararr) {
        int len = chararr.length;
        char[] newstr = new char[len];
        char lastchar, thischar;
        if (len == 0) {
            return new String("");
        }
        lastchar = chararr[0];
        newstr[0] = Character.toUpperCase(lastchar);
        for (int i = 1; i < len; i++) {
            thischar = chararr[i];
            if ((!Character.isLetter(lastchar)) && Character.isLetter(thischar)) {
                newstr[i] = Character.toUpperCase(thischar);
            } else {
                newstr[i] = Character.toLowerCase(thischar);
            }
            lastchar = thischar;
        }
        return new String(newstr);
    }

    public static int getFirstIndexOfChar(char[] chararr, char c) {
        int charindex = -1;
        int strlen = chararr.length;
        for (int i = 0; i < strlen; i++) {
            if (chararr[i] == c) {
                charindex = i;
                break;
            }
        }
        return charindex;
    }

    /**
     * This function counts the time a specific char 
     * shows up in a specified String.
     *
     * @param  str   a String in which caller wants the amount
     *               of a specific character.
     * @param  ofchar   the character to be counted in str
     * @return       number of chars that equal ofchar in str
     */
    public static int getNumberOfCharsInString(String str, char ofchar) {
        return getNumberOfCharsInString(str.toCharArray(), ofchar);
    }

    /**
     * This function counts the time a specific char 
     * shows up in a specified char buffer.
     *
     * @param  chararray   a char buffer in which caller wants
     *                     the amount of a specific character.
     * @param  ofchar  the character to be counted in chararray
     * @return       number of chars that equal ofchar in chararray
     */
    public static int getNumberOfCharsInString(char[] chararray, char ofchar) {
        int strlen = chararray.length;
        int charcount = 0;
        for (int i = 0; i < strlen; i++) {
            if (chararray[i] == ofchar) {
                charcount++;
            }
        }
        return charcount;
    }

    /**    
    * Splits a String into a Vector of lines.
    * @see LinesToString(char[][])
    */
    public static char[][] StringToLines(char[] chararr) {
        char[][] lines;
        int strlen = chararr.length;
        boolean interm = false;
        int startofline = 0;
        int i, linescount;
        if (strlen == 0) {
            lines = new char[1][];
            lines[0] = null;
            return lines;
        }
        linescount = getNumberOfCompressedLines(chararr);
        lines = new char[linescount][];
        linescount = 0;
        for (i = 0; i < strlen; i++) {
            if (isLineTerminatorChar(chararr[i])) {
                if (!interm) {
                    interm = true;
                }
            } else {
                if (interm) {
                    lines[linescount] = new char[i - startofline];
                    System.arraycopy(chararr, startofline, lines[linescount], 0, i - startofline);
                    linescount++;
                    startofline = i;
                    interm = false;
                }
            }
        }
        if (linescount < lines.length) {
            lines[linescount] = new char[strlen - startofline];
            System.arraycopy(chararr, startofline, lines[linescount], 0, strlen - startofline);
        }
        return lines;
    }

    /**    
    * Combines the "lines" in a String[] to one String.
    * 
    * @param  lines  the array of Strings that are to be merged
    * @return        the String that contains all the lines
    * @see    StringToLines(String)
    */
    public static char[] LinesToString(char[][] lines) {
        int curllen;
        int lcount;
        int newstrlen = 0;
        char[] chararr;
        int i, curcharindex = 0;
        if (lines == null) {
            return null;
        }
        lcount = lines.length;
        for (i = 0; i < lcount; i++) {
            if (lines[i] != null) {
                newstrlen += lines[i].length;
            }
        }
        if (newstrlen == 0) {
            return null;
        }
        chararr = new char[newstrlen];
        for (i = 0; i < lcount; i++) {
            if (lines[i] != null) {
                curllen = lines[i].length;
                System.arraycopy(lines[i], 0, chararr, curcharindex, curllen);
                curcharindex += curllen;
            }
        }
        return chararr;
    }

    /**    
    * Counts the number of "compressed lines" in a char array.
    * 
    * @param  charbuf  the array of chars that are to be checked
    * @return        number of compressed the lines in string
    */
    public static int getNumberOfCompressedLines(char[] charbuf) {
        int linescount = 0;
        int strlen = charbuf.length;
        if (strlen == 0) {
            return 0;
        }
        for (int i = 0; i < (strlen - 1); i++) {
            if (!isLineTerminatorChar(charbuf[i]) && isLineTerminatorChar(charbuf[i + 1])) {
                linescount++;
            }
        }
        if (isLineTerminatorChar(charbuf[0])) {
            linescount++;
        }
        if (!isLineTerminatorChar(charbuf[strlen - 1])) {
            linescount++;
        }
        if (linescount == 0) {
            linescount++;
        }
        return linescount;
    }

    /**    
    * Counts the number of words in a String.
    * 
    * @param  str  the String that is to be checked
    * @return        number of words in the String
    * @see getNumberOfWords(char[])
    */
    public static int getNumberOfWords(String str) {
        return getNumberOfWords(str.toCharArray());
    }

    /**    
    * Counts the number of words in a char array.
    * 
    * @param  chararr  the char array that is to be checked
    * @return        number of words in the char array
    * @see getNumberOfWords(String)
    */
    public static int getNumberOfWords(char[] chararr) {
        int wordcount = 0;
        int strlen = chararr.length;
        if (strlen == 0) {
            return 0;
        }
        for (int i = 0; i < (strlen - 1); i++) {
            if (!Character.isLetter(chararr[i]) && Character.isLetter(chararr[i + 1])) {
                wordcount++;
            }
        }
        if (!isBlankishChar(chararr[0])) {
            wordcount++;
        }
        return wordcount;
    }

    /**
     * This method checks a String if all its characters are part
     * of the English alphabet, i.e. from [a-z] or from [A-Z].
     * 
     * @param  str   A String that is to be check.
     * @return       true if all characters are alphabetic.
     *               false if one or more characters are not alphabetic,
     *               or if str.lenth == 0.
     */
    public static boolean isAlphabetic(String str) {
        int i;
        int strlen = str.length();
        char curchar;
        boolean nonalphabetic = false;
        if (strlen == 0) return false;
        for (i = 0; i < strlen; i++) {
            curchar = str.charAt(i);
            if (curchar < 'A') {
                nonalphabetic = true;
                break;
            } else if (curchar > 'z') {
                nonalphabetic = true;
                break;
            } else if (curchar > 'Z' && curchar < 'a') {
                nonalphabetic = true;
                break;
            }
        }
        return !nonalphabetic;
    }

    /**
     * This method checks a String if all its characters are numertic.
     * 
     * @param  str  the String that is to be check
     * @return      true if all characters are numeric - false if one 
     *              or more characters is not numeric, or if 
     *              String.lenth() == 0.
     * @see isNumeric(char[])
     */
    public static boolean isNumeric(String str) {
        return isNumeric(str.toCharArray());
    }

    /**
     * This method checks a char array if all its characters are numertic.
     * 
     * @param  chararr  the char array that is to be check
     * @return          true if all characters are numeric - false if
     *                  one or more characters is not numeric, or if 
     *                  chararr.lenth == 0.
     * @see isNumeric(String)
     */
    public static boolean isNumeric(char[] chararr) {
        int strlen = chararr.length;
        boolean nonnumeric = false;
        if (strlen == 0) return false;
        for (int i = 0; i < strlen; i++) {
            if (!Character.isDigit(chararr[i])) {
                nonnumeric = true;
                break;
            }
        }
        return !nonnumeric;
    }

    /**    
     * Tells caller whether the char c a "blankish" char is.
     * A blankish chars are string characters that are usually
     * not represented by a graphical symbol when used in a 
     * text. These are the following:
     * - space (' ')
     * - horizontal tab ('\t')           
     * - form feed ('\f')
     * - line feed ('\n')
     * - carrage return ('\r')
     *
     * @param c  the character to be checkt
     * @return   returns true is c is blankish, otherwise false
     */
    public static boolean isBlankishChar(char c) {
        if (c == SP || c == LF || c == CR || c == HT || c == FF) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tells caller whether the char c a line terminator.
     * Line terminators are:
     * - line feed ('\n')
     * - carrage return ('\r')
     * 
     * @return   true if c is a line terminator, otherwise
     *           false.
     */
    public static boolean isLineTerminatorChar(char c) {
        if (c == LF || c == CR) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tells caller whether the char c a whitespace.
     * Whitespaces are:
     * - space (' ')
     * - horizontal tab ('\t')           
     * - form feed ('\f')
     * 
     * @return   true if c is a whitespace, otherwise
     *           false.
     */
    public static boolean isWhitespaceChar(char c) {
        if (c == SP || c == HT || c == FF) {
            return true;
        } else {
            return false;
        }
    }
}

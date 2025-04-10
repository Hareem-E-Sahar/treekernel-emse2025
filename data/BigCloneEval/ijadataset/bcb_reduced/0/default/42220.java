import java.io.*;
import java.util.*;

/**
 Class for simple console input.
 A class designed primarily for simple keyboard input of the
 form one input value per line. If the user enters an improper
 input, i.e., an input of the wrong type or a blank line, then
 the user is prompted to reenter the input and given a brief
 explanation of what is required. Also includes some additional
 methods to input single numbers, words, and characters, without
 going to the next line.
*/
public class SavitchIn {

    /**
     Reads a line of text and returns that line as a String
     value. The end of a line must be indicated either by a
     new-line character '\n' or by a carriage return '\r'
     followed by a new-line character '\n'. (Almost all systems
     do this automatically. So you need not worry about this
     detail.) Neither the '\n', nor the '\r' if present, are
     part of the string returned. This will read the rest of a
     line if the line is already partially read.
    */
    public static String readLine() {
        char nextChar;
        String result = "";
        boolean done = false;
        while (!done) {
            nextChar = readChar();
            if (nextChar == '\n') done = true; else if (nextChar == '\r') {
            } else result = result + nextChar;
        }
        return result;
    }

    /**
     Reads the first string of nonwhitespace characters on
     a line and returns that string. The rest of the line
     is discarded. If the line contains only whitespace,
     the user is asked to reenter the line.
    */
    public static String readLineWord() {
        String inputString = null, result = null;
        boolean done = false;
        while (!done) {
            inputString = readLine();
            StringTokenizer wordSource = new StringTokenizer(inputString);
            if (wordSource.hasMoreTokens()) {
                result = wordSource.nextToken();
                done = true;
            } else {
                System.out.println("Your input is not correct. Your input must");
                System.out.println("contain at least one nonwhitespace character.");
                System.out.println("Please try again. Enter input:");
            }
        }
        return result;
    }

    /**
     Precondition: The user has entered a number of type int
     on a line by itself, except that there may be
     whitespace before and/or after the number.
     Action: Reads and returns the number as a value of type
     int. The rest of the line is discarded. If the input is
     not entered correctly, then in most cases, the user will
     be asked to reenter the input. In particular, this
     applies to incorrect number formats and blank lines.
    */
    public static int readLineInt() {
        String inputString = null;
        int number = -9999;
        boolean done = false;
        while (!done) {
            try {
                inputString = readLine();
                inputString = inputString.trim();
                number = Integer.parseInt(inputString);
                done = true;
            } catch (NumberFormatException e) {
                System.out.println("Your input number is not correct.");
                System.out.println("Your input number must be");
                System.out.println("a whole number written as an");
                System.out.println("ordinary numeral, such as 42");
                System.out.println("Minus signs are OK," + "but do not use a plus sign.");
                System.out.println("Please try again.");
                System.out.println("Enter a whole number:");
            }
        }
        return number;
    }

    /**
     Precondition: The user has entered a number of type long
     on a line by itself, except that there may be whitespace
     before and/or after the number.
     Action: Reads and returns the number as a value of type
     long. The rest of the line is discarded. If the input is
     not entered correctly, then in most cases, the user will
     be asked to reenter the input. In particular, this
     applies to incorrect number formats and blank lines.
    */
    public static long readLineLong() {
        String inputString = null;
        long number = -9999;
        boolean done = false;
        while (!done) {
            try {
                inputString = readLine();
                inputString = inputString.trim();
                number = Long.parseLong(inputString);
                done = true;
            } catch (NumberFormatException e) {
                System.out.println("Your input number is not correct.");
                System.out.println("Your input number must be");
                System.out.println("a whole number written as an");
                System.out.println("ordinary numeral, such as 42");
                System.out.println("Minus signs are OK," + "but do not use a plus sign.");
                System.out.println("Please try again.");
                System.out.println("Enter a whole number:");
            }
        }
        return number;
    }

    /**
     Precondition: The user has entered a number of type
     double on a line by itself, except that there may be
     whitespace before and/or after the number.
     Action: Reads and returns the number as a value of type
     double. The rest of the line is discarded. If the input
     is not entered correctly, then in most cases, the user
     will be asked to reenter the input. In particular, this
     applies to incorrect number formats and blank lines.
    */
    public static double readLineDouble() {
        String inputString = null;
        double number = -9999;
        boolean done = false;
        while (!done) {
            try {
                inputString = readLine();
                inputString = inputString.trim();
                number = Double.parseDouble(inputString);
                done = true;
            } catch (NumberFormatException e) {
                System.out.println("Your input number is not correct.");
                System.out.println("Your input number must be");
                System.out.println("an ordinary number either with");
                System.out.println("or without a decimal point,");
                System.out.println("such as 42 or 9.99");
                System.out.println("Please try again.");
                System.out.println("Enter the number:");
            }
        }
        return number;
    }

    /**
     Precondition: The user has entered a number of type float
     on a line by itself, except that there may be whitespace
     before and/or after the number.
     Action: Reads and returns the number as a value of type
     float. The rest of the line is discarded. If the input is
     not entered correctly, then in most cases, the user will
     be asked to reenter the input. In particular,
     this applies to incorrect number formats and blank lines.
    */
    public static float readLineFloat() {
        String inputString = null;
        float number = -9999;
        boolean done = false;
        while (!done) {
            try {
                inputString = readLine();
                inputString = inputString.trim();
                number = Float.parseFloat(inputString);
                done = true;
            } catch (NumberFormatException e) {
                System.out.println("Your input number is not correct.");
                System.out.println("Your input number must be");
                System.out.println("an ordinary number either with");
                System.out.println("or without a decimal point,");
                System.out.println("such as 42 or 9.99");
                System.out.println("Please try again.");
                System.out.println("Enter the number:");
            }
        }
        return number;
    }

    /**
     Reads the first nonwhitespace character on a line and
     returns that character. The rest of the line is
     discarded. If the line contains only whitespace, the
     user is asked to reenter the line.
    */
    public static char readLineNonwhiteChar() {
        boolean done = false;
        String inputString = null;
        char nonWhite = ' ';
        while (!done) {
            inputString = readLine();
            inputString = inputString.trim();
            if (inputString.length() == 0) {
                System.out.println("Your input is not correct.");
                System.out.println("Your input must contain at");
                System.out.println("least one nonwhitespace character.");
                System.out.println("Please try again.");
                System.out.println("Enter input:");
            } else {
                nonWhite = (inputString.charAt(0));
                done = true;
            }
        }
        return nonWhite;
    }

    /**
     Input should consist of a single word on a line, possibly
     surrounded by whitespace. The line is read and discarded.
     If the input word is "true" or "t", then true is returned.
     If the input word is "false" or "f", then false is
     returned. Uppercase and lowercase letters are considered
     equal. If the user enters anything else (e.g., multiple
     words or different words), the user is asked
     to reenter the input.
    */
    public static boolean readLineBoolean() {
        boolean done = false;
        String inputString = null;
        boolean result = false;
        while (!done) {
            inputString = readLine();
            inputString = inputString.trim();
            if (inputString.equalsIgnoreCase("true") || inputString.equalsIgnoreCase("t")) {
                result = true;
                done = true;
            } else if (inputString.equalsIgnoreCase("false") || inputString.equalsIgnoreCase("f")) {
                result = false;
                done = true;
            } else {
                System.out.println("Your input is not correct.");
                System.out.println("Your input must be");
                System.out.println("one of the following:");
                System.out.println("the word true,");
                System.out.println("the word false,");
                System.out.println("the letter T,");
                System.out.println("or the letter F.");
                System.out.println("You may use either upper-");
                System.out.println("or lowercase letters.");
                System.out.println("Please try again.");
                System.out.println("Enter input:");
            }
        }
        return result;
    }

    /**
     Reads the next input character and returns that character.
     The next read takes place on the same line where this
     one left off.
    */
    public static char readChar() {
        int charAsInt = -1;
        try {
            charAsInt = System.in.read();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Fatal error. Ending program.");
            System.exit(0);
        }
        return (char) charAsInt;
    }

    /**
     Reads the next nonwhitespace input character and returns
     that character. The next read takes place immediately
     after the character read.
    */
    public static char readNonwhiteChar() {
        char next;
        next = readChar();
        while (Character.isWhitespace(next)) next = readChar();
        return next;
    }

    /**
     Precondition: The next input in the stream consists of
     an int value, possibly preceded by whitespace, but
     definitely followed by whitespace.
     Action: Reads the first string of nonwhitespace characters
     and returns the int value it represents. Discards the
     first whitespace character after the word. The next read
     takes place immediately after the discarded whitespace.
     In particular, if the word is at the end of a line, the
     next read will take place starting on the next line.
     If the next word does not represent an int value,
     a NumberFormatException is thrown.
    */
    public static int readInt() throws NumberFormatException {
        String inputString = null;
        inputString = readWord();
        return Integer.parseInt(inputString);
    }

    /**
     Precondition: The next input consists of a long value,
     possibly preceded by whitespace, but definitely
     followed by whitespace.
     Action: Reads the first string of nonwhitespace characters
     and returns the long value it represents. Discards the
     first whitespace character after the string read. The
     next read takes place immediately after the discarded
     whitespace. In particular, if the string read is at the
     end of a line, the next read will take place starting on
     the next line. If the next word does not represent a long
     value, a NumberFormatException is thrown.
    */
    public static long readLong() throws NumberFormatException {
        String inputString = null;
        inputString = readWord();
        return Long.parseLong(inputString);
    }

    /**
     Precondition: The next input consists of a double value,
     possibly preceded by whitespace, but definitely
     followed by whitespace.
     Action: Reads the first string of nonwhitespace characters
     and returns the double value it represents. Discards the
     first whitespace character after the string read. The
     next read takes place immediately after the discarded
     whitespace. In particular, if the string read is at the
     end of a line, the next read will take place starting on
     the next line. If the next word does not represent a
     double value, a NumberFormatException is thrown.
    */
    public static double readDouble() throws NumberFormatException {
        String inputString = null;
        inputString = readWord();
        return Double.parseDouble(inputString);
    }

    /**
     Precondition: The next input consists of a float value,
     possibly preceded by whitespace, but definitely
     followed by whitespace.
     Action: Reads the first string of nonwhitespace characters
     and returns the float value it represents. Discards the
     first whitespace character after the string read. The
     next read takes place immediately after the discarded
     whitespace. In particular, if the string read is at the
     end of a line, the next read will take place starting on
     the next line. If the next word does not represent
     a float value, a NumberFormatException is thrown.
    */
    public static float readFloat() throws NumberFormatException {
        String inputString = null;
        inputString = readWord();
        return Float.parseFloat(inputString);
    }

    /**
     Reads the first string of nonwhitespace characters and
     returns that string. Discards the first whitespace
     character after the string read. The next read takes
     place immediately after the discarded whitespace. In
     particular, if the string read is at the end of a line,
     the next read will take place starting on the next line.
     Note that if it receives blank lines, it will wait until
     it gets a nonwhitespace character.
    */
    public static String readWord() {
        String result = "";
        char next;
        next = readChar();
        while (Character.isWhitespace(next)) next = readChar();
        while (!(Character.isWhitespace(next))) {
            result = result + next;
            next = readChar();
        }
        if (next == '\r') {
            next = readChar();
            if (next != '\n') {
                System.out.println("Fatal error in method " + "readWord of the class SavitchIn.");
                System.exit(1);
            }
        }
        return result;
    }

    /**
     Precondition: The user has entered a number of type byte
     on a line by itself, except that there may be whitespace
     before and/or after the number.
     Action: Reads and returns the number as a value of type
     byte. The rest of the line is discarded. If the input is
     not entered correctly, then in most cases, the user will
     be asked to reenter the input. In particular, this applies
     to incorrect number formats and blank lines.
    */
    public static byte readLineByte() {
        String inputString = null;
        byte number = -123;
        boolean done = false;
        while (!done) {
            try {
                inputString = readLine();
                inputString = inputString.trim();
                number = Byte.parseByte(inputString);
                done = true;
            } catch (NumberFormatException e) {
                System.out.println("Your input number is not correct.");
                System.out.println("Your input number must be a");
                System.out.println("whole number in the range");
                System.out.println("-128 to 127, written as");
                System.out.println("an ordinary numeral, such as 42.");
                System.out.println("Minus signs are OK," + "but do not use a plus sign.");
                System.out.println("Please try again.");
                System.out.println("Enter a whole number:");
            }
        }
        return number;
    }

    /**
     Precondition: The user has entered a number of type short
     on a line by itself, except that there may be whitespace
     before and/or after the number.
     Action: Reads and returns the number as a value of type
     short. The rest of the line is discarded. If the input is
     not entered correctly, then in most cases, the user will
     be asked to reenter the input. In particular, this applies
     to incorrect number formats and blank lines.
    */
    public static short readLineShort() {
        String inputString = null;
        short number = -9999;
        boolean done = false;
        while (!done) {
            try {
                inputString = readLine();
                inputString = inputString.trim();
                number = Short.parseShort(inputString);
                done = true;
            } catch (NumberFormatException e) {
                System.out.println("Your input number is not correct.");
                System.out.println("Your input number must be a");
                System.out.println("whole number in the range");
                System.out.println("-32768 to 32767, written as");
                System.out.println("an ordinary numeral, such as 42.");
                System.out.println("Minus signs are OK," + "but do not use a plus sign.");
                System.out.println("Please try again.");
                System.out.println("Enter a whole number:");
            }
        }
        return number;
    }

    public static byte readByte() throws NumberFormatException {
        String inputString = null;
        inputString = readWord();
        return Byte.parseByte(inputString);
    }

    public static short readShort() throws NumberFormatException {
        String inputString = null;
        inputString = readWord();
        return Short.parseShort(inputString);
    }

    /**
     Reads the first byte in the input stream and returns that
     byte as an int. The next read takes place where this one
     left off. This read is the same as System.in.read( ),
     except that it catches IOExceptions.
    */
    public static int read() {
        int result = -1;
        try {
            result = System.in.read();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Fatal error. Ending program.");
            System.exit(0);
        }
        return result;
    }
}

package org.argouml.util;

/**
 * Base class for custom separators.
 *
 * <p>It can be instantiated directly, and then works like a separator in the
 * delimiter string. For that purpose you should use the delimiter string
 * in MyTokenizer, unless your token is wider than 32 characters.
 *
 * <p>You can also subclass this class to provide for more intricate recogition
 * of the tokens. It is known that this class has been subclassed to recognize
 * quoted strings, and balanced parentheses.
 *
 * <p>You should have this mental image of the tokenizing process:<nl>
 * <li>Reset is called.
 * <li>For each character, c, in the sequence being tokenized:<ul>
 *     <li>addChar(c) is called for each separator in the tokenizer.
 *     <li>if addChar returns true, break.</ul>
 * <li>hasFreePart is checked to see if something follows. If true:<ul>
 *     <li>endChar(c) is called for each following character.
 *     <li>if/when endChar returns true, break.</ul>
 * <li>tokenLength is checked to see how far back in the sequence the token
 *     begun. If there are characters before that but after the last token,
 *     then they are made a token and this token is saved and returned next.
 * </nl>
 *
 * @author Michael Stockman
 * @since 0.11.2
 * @see MyTokenizer
 */
public class CustomSeparator {

    private char pattern[];

    private char match[];

    /**
	 * This constructor is only availible to subclasses of this class.
	 * If you use it you should also override {@link #addChar addChar}
	 * to recognize when your separator should that control. If you don't,
	 * then you may block all other separators.
	 */
    protected CustomSeparator() {
        match = pattern = new char[0];
    }

    /**
	 * This constructor creates a new custom separator that matches the
	 * character start. Unless you override {@link #addChar addChar}, the
	 * default behaviour is to return false in addChar until start is
	 * encountered and then hasFreePart returns false.
	 *
	 * @param start The start character.
	 */
    public CustomSeparator(char start) {
        pattern = new char[1];
        pattern[0] = start;
        match = new char[pattern.length];
    }

    /**
	 * This constructor creates a new custom separator that matches the
	 * string start. Unless you override {@link #addChar addChar}, the
	 * default behaviour is to return false in addChar until start is
	 * encountered and then hasFreePart returns false.
	 *
	 * @param start The start String.
	 */
    public CustomSeparator(String start) {
        pattern = start.toCharArray();
        match = new char[pattern.length];
    }

    /**
	 * Called to reset the separator before staring to look for a new
	 * token.
	 */
    public void reset() {
        int i;
        for (i = 0; i < match.length; i++) match[i] = 0;
    }

    /**
	 * Returns the length of the matched token. It is not required to be
	 * meaningful unless addChar has returned true and hasFreePart
	 * returned false or endChar returned true.
	 *
	 * @return The length of the matched token.
	 */
    public int tokenLength() {
        return pattern.length;
    }

    /**
	 * Called to allow you to decide if you want to capure control of
	 * the matching process. If you return true, then
	 * {@link #hasFreePart hasFreePart} will be checked to see if you
	 * expect more things to follow.
	 *
	 * <p>The default behaviour is to return false until the character
	 * or String given as parameter to the constructor has been matched.
	 *
	 * @param c The next character in the sequence being tokenized.
	 * @return true to gain control of the matching, false to continue
	 * matching.
	 */
    public boolean addChar(char c) {
        int i;
        for (i = 0; i < match.length - 1; i++) match[i] = match[i + 1];
        match[match.length - 1] = c;
        for (i = 0; i < match.length; i++) if (match[i] != pattern[i]) return false;
        return true;
    }

    /**
	 * Called to check if more characters are expected to follow after
	 * addChar has returned true. If true, then any following characters
	 * will be fed to endChar until endChar returns true.
	 *
	 * <p>The default behaviour is to return false.
	 *
	 * @return true to continue feeding characters to endChar or false.
	 */
    public boolean hasFreePart() {
        return false;
    }

    /**
	 * Called to check if more characters are expected in the free part of
	 * the token.
	 *
	 * @param c The next character in the sequence being tokenized.
	 * @return true to indicate that the token is complete, or false to
	 * continue feeding characters through endChar.
	 */
    public boolean endChar(char c) {
        return true;
    }
}

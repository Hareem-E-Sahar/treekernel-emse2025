package org.pdfclown.documents.contents.composition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.pdfclown.documents.contents.fonts.Font;

/**
  Text fitter.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.3
  @version 0.1.2, 01/20/12
*/
final class TextFitter {

    private final Font font;

    private final double fontSize;

    private final boolean hyphenation;

    private final char hyphenationCharacter;

    private final String text;

    private double width;

    private int beginIndex = 0;

    private int endIndex = -1;

    private String fittedText;

    private double fittedWidth;

    TextFitter(String text, double width, Font font, double fontSize, boolean hyphenation, char hyphenationCharacter) {
        this.text = text;
        this.width = width;
        this.font = font;
        this.fontSize = fontSize;
        this.hyphenation = hyphenation;
        this.hyphenationCharacter = hyphenationCharacter;
    }

    /**
    Fits the text inside the specified width.

    @param unspacedFitting Whether fitting of unspaced text is allowed.
    @return Whether the operation was successful.
  */
    public boolean fit(boolean unspacedFitting) {
        return fit(endIndex + 1, width, unspacedFitting);
    }

    /**
    Fits the text inside the specified width.

    @param index Beginning index, inclusive.
    @param width Available width.
    @param unspacedFitting Whether fitting of unspaced text is allowed.
    @return Whether the operation was successful.
    @version 0.0.4
  */
    public boolean fit(int index, double width, boolean unspacedFitting) {
        beginIndex = index;
        this.width = width;
        fittedText = null;
        fittedWidth = 0;
        String hyphen = "";
        fitting: {
            Pattern pattern = Pattern.compile("(\\s*)(\\S*)");
            Matcher matcher = pattern.matcher(text);
            matcher.region(beginIndex, text.length());
            while (matcher.find()) {
                for (int spaceIndex = matcher.start(1), spaceEnd = matcher.end(1); spaceIndex < spaceEnd; spaceIndex++) {
                    switch(text.charAt(spaceIndex)) {
                        case '\n':
                        case '\r':
                            index = spaceIndex;
                            break fitting;
                    }
                }
                int wordEndIndex = matcher.end(0);
                double wordWidth = font.getWidth(matcher.group(0), fontSize);
                fittedWidth += wordWidth;
                if (fittedWidth > width) {
                    fittedWidth -= wordWidth;
                    wordEndIndex = index;
                    if (!hyphenation && (wordEndIndex > beginIndex || !unspacedFitting || text.charAt(beginIndex) == ' ')) break fitting;
                    hyphenating: while (true) {
                        char textChar = text.charAt(wordEndIndex);
                        wordWidth = font.getWidth(textChar, fontSize);
                        wordEndIndex++;
                        fittedWidth += wordWidth;
                        if (fittedWidth > width) {
                            fittedWidth -= wordWidth;
                            wordEndIndex--;
                            if (hyphenation) {
                                if (wordEndIndex > index + 4) {
                                    wordEndIndex--;
                                    index = wordEndIndex;
                                    textChar = text.charAt(wordEndIndex);
                                    fittedWidth -= font.getWidth(textChar, fontSize);
                                    textChar = hyphenationCharacter;
                                    fittedWidth += font.getWidth(textChar, fontSize);
                                    hyphen = String.valueOf(textChar);
                                } else {
                                    while (wordEndIndex > index) {
                                        wordEndIndex--;
                                        textChar = text.charAt(wordEndIndex);
                                        fittedWidth -= font.getWidth(textChar, fontSize);
                                    }
                                }
                            } else {
                                index = wordEndIndex;
                            }
                            break hyphenating;
                        }
                    }
                    break fitting;
                }
                index = wordEndIndex;
            }
        }
        fittedText = text.substring(beginIndex, index) + hyphen;
        endIndex = index;
        return (fittedWidth > 0);
    }

    /**
    Gets the begin index of the fitted text inside the available text.
  */
    public int getBeginIndex() {
        return beginIndex;
    }

    /**
    Gets the end index of the fitted text inside the available text.
  */
    public int getEndIndex() {
        return endIndex;
    }

    /**
    Gets the fitted text.
  */
    public String getFittedText() {
        return fittedText;
    }

    /**
    Gets the fitted text's width.
  */
    public double getFittedWidth() {
        return fittedWidth;
    }

    /**
    Gets the font used to fit the text.
  */
    public Font getFont() {
        return font;
    }

    /**
    Gets the size of the font used to fit the text.
  */
    public double getFontSize() {
        return fontSize;
    }

    /**
    Gets the character shown at the end of the line before a hyphenation break.
  */
    public char getHyphenationCharacter() {
        return hyphenationCharacter;
    }

    /**
    Gets the available text.
  */
    public String getText() {
        return text;
    }

    /**
    Gets the available width.
  */
    public double getWidth() {
        return width;
    }

    /**
    Gets whether the hyphenation algorithm has to be applied.
  */
    public boolean isHyphenation() {
        return hyphenation;
    }
}

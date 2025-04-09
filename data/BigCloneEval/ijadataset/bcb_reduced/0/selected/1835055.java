package gui;

import gui.channels.Channel;
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import whisper.Config;
import cc.slx.java.string.StringHelper;
import cc.slx.java.time.SimpleDate;

/**
 * This class is responsible for maintaining the available text styles used throughout the program. It
 * provides helper functions to use the styles.
 * 
 * @author Thomas Pedley.
 */
public class TextStyle {

    /** Identifier for the "regular" style of text. */
    private static final String STYLE_REGULAR = "regular";

    /** Identifier for the "action" style of text. */
    private static final String STYLE_ACTION = "action";

    /** Identifier for the "error" style of text. */
    private static final String STYLE_ERROR = "error";

    /** Identifier for the "system" style of text. */
    private static final String STYLE_SYSTEM = "system";

    /** Identifier for the "remote chat" style of text (message from a third party). */
    private static final String STYLE_CHATREMOTE = "remote";

    /** Identifier for the "remote chat from a friend" style of text (message from a third party). */
    private static final String STYLE_CHATREMOTEFRIEND = "remoteFriend";

    /** Identifier for the "local chat" style of text (message from the user). */
    private static final String STYLE_CHATLOCAL = "local";

    /** Identifier for the "informational" style of text. */
    private static final String STYLE_INFORMATIONAL = "informational";

    /** Identifier for the "offline" style of text. */
    private static final String STYLE_OFFLINE = "offline";

    /** Identifier for the "object" style of text. */
    private static final String STYLE_OBJECT = "object";

    /** Identifier for the "faint" style of text. */
    private static final String STYLE_FAINT = "faint";

    /** Identifier for the "faint" style of text. */
    private static final String STYLE_ACTION_FAINT = "faintAction";

    /** Identifier for a URL. */
    public static final Integer IDENTIFIER_URL = new Integer(0);

    /**
	 * Set the styles in a channel's {@link StyledDocument}. This is used to initialise the {@link StyledDocument}
	 * so that it is aware of the available styles. This only needs to be called once per {@link StyledDocument}.
	 * 
	 * @param channel The channel to initialise.
	 */
    public static void setStyles(Channel channel) {
        StyledDocument styledDocument = channel.getStyledDocument();
        Style regular = styledDocument.addStyle(STYLE_REGULAR, StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
        StyleConstants.setFontFamily(regular, "SansSerif");
        styledDocument.addStyle(STYLE_ACTION, regular);
        Style error = styledDocument.addStyle(STYLE_ERROR, regular);
        StyleConstants.setForeground(error, Color.red);
        StyleConstants.setBold(error, true);
        Style system = styledDocument.addStyle(STYLE_SYSTEM, regular);
        StyleConstants.setForeground(system, new Color(170, 0, 170));
        StyleConstants.setBold(system, true);
        Style chatRemote = styledDocument.addStyle(STYLE_CHATREMOTE, regular);
        StyleConstants.setForeground(chatRemote, new Color(230, 150, 0));
        StyleConstants.setBold(chatRemote, true);
        Style chatRemoteFriend = styledDocument.addStyle(STYLE_CHATREMOTEFRIEND, regular);
        StyleConstants.setForeground(chatRemoteFriend, Color.green);
        StyleConstants.setBold(chatRemoteFriend, true);
        Style chatLocal = styledDocument.addStyle(STYLE_CHATLOCAL, regular);
        StyleConstants.setForeground(chatLocal, Color.BLUE);
        StyleConstants.setBold(chatLocal, true);
        Style informational = styledDocument.addStyle(STYLE_INFORMATIONAL, regular);
        StyleConstants.setForeground(informational, Color.DARK_GRAY);
        Style offline = styledDocument.addStyle(STYLE_OFFLINE, regular);
        StyleConstants.setForeground(offline, Color.DARK_GRAY);
        StyleConstants.setItalic(offline, true);
        Style object = styledDocument.addStyle(STYLE_OBJECT, regular);
        StyleConstants.setForeground(object, new Color(0, 127, 0));
        StyleConstants.setBold(object, true);
        Style faint = styledDocument.addStyle(STYLE_FAINT, regular);
        StyleConstants.setForeground(faint, new Color(50, 50, 50));
        Style faintAction = styledDocument.addStyle(STYLE_ACTION_FAINT, regular);
        StyleConstants.setForeground(faintAction, new Color(50, 50, 50));
    }

    /**
	 * Add text using the informational formatting.
	 * 
	 * @param text The text to add.
	 * @param channel The {@link Channel} to add the text to.
	 */
    public static void addInformationalText(String text, Channel channel) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText(text + StringHelper.nl, sd, sd.getStyle(STYLE_INFORMATIONAL));
    }

    /**
	 * Add text using the error formatting.
	 * 
	 * @param text The text to add.
	 * @param channel The {@link Channel} to add the text to.
	 */
    public static void addErrorText(String text, Channel channel) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText(text + StringHelper.nl, sd, sd.getStyle(STYLE_ERROR));
    }

    /**
	 * Add text using the system formatting.
	 * 
	 * @param text The text to add.
	 * @param name The name of the message originator (if any).
	 * @param channel The {@link Channel} to add the text to.
	 */
    public static void addSystemText(String text, String name, Channel channel) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        if (name != null && name.trim().length() > 0) {
            addText(name + ": ", sd, sd.getStyle(STYLE_SYSTEM));
        }
        addText(text + StringHelper.nl, sd, sd.getStyle(STYLE_REGULAR));
    }

    /**
	 * Add text using the remote chat formatting.
	 * 
	 * @param text The text to add.
	 * @param name The name of the message originator.
	 * @param channel The {@link Channel} to add the text to.
	 * @param faint True if the text is barely audible.
	 * @param friend True if the text is from a friend, otherwise false.
	 */
    public static void addRemoteChatText(String text, String name, Channel channel, boolean faint, boolean friend) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText("<" + name + "> ", sd, friend ? sd.getStyle(STYLE_CHATREMOTEFRIEND) : sd.getStyle(STYLE_CHATREMOTE));
        addText(text.trim() + StringHelper.nl, sd, faint ? sd.getStyle(STYLE_FAINT) : sd.getStyle(STYLE_REGULAR));
    }

    /**
	 * Add text using the local chat formatting.
	 * 
	 * @param text The text to add.
	 * @param name The name of the message originator.
	 * @param channel The {@link Channel} to add the text to.
	 */
    public static void addLocalChatText(String text, String name, Channel channel) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText("<" + name + "> ", sd, sd.getStyle(STYLE_CHATLOCAL));
        addText(text.trim() + StringHelper.nl, sd, sd.getStyle(STYLE_REGULAR));
    }

    /**
	 * Add text using the remote action formatting.
	 * 
	 * @param text The text to add.
	 * @param name The name of the message originator.
	 * @param channel The {@link Channel} to add the text to.
	 * @param faint True if the text is barely audible.
	 * @param friend True if the text is from a friend, otherwise false.
	 */
    public static void addRemoteActionText(String text, String name, Channel channel, boolean faint, boolean friend) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText("* " + name + " ", sd, friend ? sd.getStyle(STYLE_CHATREMOTEFRIEND) : sd.getStyle(STYLE_CHATREMOTE));
        addText(text.trim() + StringHelper.nl, sd, faint ? sd.getStyle(STYLE_ACTION_FAINT) : sd.getStyle(STYLE_ACTION));
    }

    /**
	 * Add text using the local action formatting.
	 * 
	 * @param text The text to add.
	 * @param name The name of the message originator.
	 * @param channel The {@link Channel} to add the text to.
	 */
    public static void addLocalActionText(String text, String name, Channel channel) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText("* " + name + " ", sd, sd.getStyle(STYLE_CHATLOCAL));
        addText(text.trim() + StringHelper.nl, sd, sd.getStyle(STYLE_ACTION));
    }

    /**
	 * Add text using the offline chat formatting.
	 * 
	 * @param text The text to add.
	 * @param name The name of the message originator.
	 * @param channel The {@link Channel} to add the text to.
	 * @param friend True if the text is from a friend, otherwise false.
	 */
    public static void addOfflineChatText(String text, String name, Channel channel, boolean friend) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText("<" + name + "> ", sd, friend ? sd.getStyle(STYLE_CHATREMOTEFRIEND) : sd.getStyle(STYLE_CHATREMOTE));
        addText(text.trim() + StringHelper.nl, sd, sd.getStyle(STYLE_OFFLINE));
    }

    /**
	 * Add text using the offline chat formatting.
	 * 
	 * @param text The text to add.
	 * @param name The name of the message originator.
	 * @param channel The {@link Channel} to add the text to.
	 * @param friend True if the text is from a friend, otherwise false.
	 */
    public static void addOfflineActionText(String text, String name, Channel channel, boolean friend) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText("* " + name + " ", sd, friend ? sd.getStyle(STYLE_CHATREMOTEFRIEND) : sd.getStyle(STYLE_CHATREMOTE));
        addText(text.trim() + StringHelper.nl, sd, sd.getStyle(STYLE_OFFLINE));
    }

    /**
	 * Add text using the object chat formatting.
	 * 
	 * @param text The text to add.
	 * @param name The name of the message originator.
	 * @param channel The {@link Channel} to add the text to.
	 * @param faint True if the text is barely audible.
	 */
    public static void addObjectText(String text, String name, Channel channel, boolean faint) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText(name + ": ", sd, sd.getStyle(STYLE_OBJECT));
        addText(text.trim() + StringHelper.nl, sd, faint ? sd.getStyle(STYLE_FAINT) : sd.getStyle(STYLE_REGULAR));
    }

    /**
	 * Add text using the object action formatting.
	 * 
	 * @param text The text to add.
	 * @param name The name of the message originator.
	 * @param channel The {@link Channel} to add the text to.
	 * @param faint True if the text is barely audible.
	 */
    public static void addObjectActionText(String text, String name, Channel channel, boolean faint) {
        StyledDocument sd = channel.getStyledDocument();
        addTimestamp(channel);
        addText("* " + name + " ", sd, sd.getStyle(STYLE_OBJECT));
        addText(text.trim() + StringHelper.nl, sd, faint ? sd.getStyle(STYLE_ACTION_FAINT) : sd.getStyle(STYLE_ACTION));
    }

    /**
	 * Add a timestamp to the {@link StyledDocument} (only if the configuration requires it).
	 * 
	 * @param channel The {@link Channel} to add the text to.
	 */
    private static void addTimestamp(Channel channel) {
        StyledDocument sd = channel.getStyledDocument();
        if (Config.getConfig().timestamp) {
            SimpleDate date = new SimpleDate();
            addText("[" + date.getDate(SimpleDate.DATE_FORMAT_TIME_SECONDS) + "] ", sd, sd.getStyle(STYLE_INFORMATIONAL));
        }
    }

    /**
	 * Add text to a {@link StyledDocument} with the given style. Any URLs will automatically
	 * be highlighted and made clickable.
	 * 
	 * @param text The text to add.
	 * @param sd The document to add to.
	 * @param style The style to use.
	 */
    private static void addText(String text, StyledDocument sd, Style style) {
        Pattern URL = Pattern.compile(StringHelper.URLRegex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = URL.matcher(text);
        try {
            sd.insertString(sd.getLength(), text, style);
        } catch (BadLocationException e) {
        }
        Style urlStyle;
        while (matcher.find()) {
            urlStyle = sd.addStyle("link" + matcher.start(), null);
            urlStyle.addAttribute(IDENTIFIER_URL, matcher.group());
            StyleConstants.setForeground(urlStyle, Color.BLUE);
            StyleConstants.setBold(urlStyle, true);
            StyleConstants.setUnderline(urlStyle, true);
            sd.setCharacterAttributes(sd.getLength() - text.length() + matcher.start(), matcher.end() - matcher.start(), urlStyle, true);
        }
    }
}

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class javaxword extends java.applet.Applet implements ActionListener, ListSelectionListener {

    static final int kAppWidth = 605;

    static final int kAppHeight = 525;

    static final int kBlockWidth = 25;

    static final int kBlockHeight = 25;

    static final int kBlocksWide = 15;

    static final int kBlocksHigh = 15;

    static final int kButtonWidth = 100;

    static final int kButtonHeight = 25;

    static final int kUp = 0;

    static final int kDown = 1;

    static final int kAcross = 0;

    static final int kPaddingTop = 40;

    static final int kPaddingLeft = 5;

    static final int kPaddingClues = 15;

    static final int kQuestionAreaHeight = 40;

    int gDirection = kAcross;

    int gCurX = 0;

    int gCurY = 0;

    int clipLeft = -1;

    int clipTop = -1;

    int clipWidth = -1;

    int clipHeight = -1;

    int gBlockMinY = 0;

    int gBlockMaxY = 0;

    int gBlockMinX = 0;

    int gBlockMaxX = 0;

    int gOldBlockMinY = 0;

    int gOldBlockMaxY = 0;

    int gOldBlockMinX = 0;

    int gOldBlockMaxX = 0;

    Button RevealAll;

    Button RevealWord;

    Button CheckAll;

    Button CheckWord;

    JList AcrossList;

    JList DownList;

    private JTextField AcrossText;

    private JTextField DownText;

    private JTextField titleText;

    private JTextField authorText;

    private JTextArea copyrightText;

    String letters[] = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

    static final String author = "  Alex Boisvert  ";

    static final String title = "Langdon Calling";

    static final String copyright = "© 2009 Alex Boisvert";

    static final int layout[][] = { { 1, 2, 3, -1, 4, 5, 6, 7, 8, 9, -1, 10, 11, 12, 13 }, { 14, 00, 00, -1, 15, 00, 00, 00, 00, 00, -1, 16, 00, 00, 00 }, { 17, 00, 00, -1, 18, 00, 00, 00, 00, 00, 19, 00, 00, 00, 00 }, { 20, 00, 00, 21, 00, 00, -1, 22, 00, 00, 00, 00, -1, -1, -1 }, { 23, 00, 00, 00, 00, 00, 24, 00, 00, -1, 25, 00, 26, 27, 28 }, { -1, -1, -1, 29, 00, 00, 00, 00, -1, 30, 00, 00, 00, 00, 00 }, { 31, 32, 33, 00, 00, -1, 34, 00, 35, 00, 00, -1, 36, 00, 00 }, { 37, 00, 00, 00, -1, 38, 00, 00, 00, 00, -1, 39, 00, 00, 00 }, { 40, 00, 00, -1, 41, 00, 00, 00, 00, -1, 42, 00, 00, 00, 00 }, { 43, 00, 00, 44, 00, 00, -1, 45, 00, 46, 00, 00, -1, -1, -1 }, { 47, 00, 00, 00, 00, -1, 48, 00, 00, 00, 00, 00, 49, 50, 51 }, { -1, -1, -1, 52, 00, 53, 00, 00, -1, 54, 00, 00, 00, 00, 00 }, { 55, 56, 57, 00, 00, 00, 00, 00, 58, 00, 00, -1, 59, 00, 00 }, { 60, 00, 00, 00, -1, 61, 00, 00, 00, 00, 00, -1, 62, 00, 00 }, { 63, 00, 00, 00, -1, 64, 00, 00, 00, 00, 00, -1, 65, 00, 00 } };

    static final String answers[][] = { { "L", "A", "T", ".", "L", "O", "C", "A", "L", "E", ".", "F", "O", "A", "M" }, { "A", "P", "E", ".", "I", "R", "O", "N", "E", "R", ".", "I", "A", "G", "O" }, { "M", "A", "N", ".", "T", "A", "N", "G", "E", "L", "O", "T", "R", "E", "E" }, { "B", "R", "O", "L", "I", "N", ".", "E", "R", "E", "C", "T", ".", ".", "." }, { "S", "T", "R", "A", "N", "G", "E", "L", "Y", ".", "C", "E", "A", "S", "E" }, { ".", ".", ".", "S", "T", "E", "M", "S", ".", "B", "U", "D", "D", "H", "A" }, { "W", "E", "D", "T", "O", ".", "B", "A", "K", "E", "R", ".", "L", "E", "S" }, { "H", "A", "U", "S", ".", "R", "E", "N", "E", "E", ".", "S", "A", "L", "E" }, { "O", "T", "B", ".", "P", "A", "D", "D", "Y", ".", "C", "H", "I", "L", "D" }, { "M", "E", "A", "S", "L", "Y", ".", "D", "E", "E", "R", "E", ".", ".", "." }, { "E", "R", "I", "C", "A", ".", "D", "E", "S", "D", "E", "M", "O", "N", "A" }, { ".", ".", ".", "H", "I", "M", "O", "M", ".", "S", "A", "P", "P", "E", "D" }, { "C", "L", "A", "U", "D", "E", "M", "O", "N", "E", "T", ".", "T", "W", "O" }, { "O", "I", "L", "S", ".", "S", "E", "N", "I", "L", "E", ".", "E", "E", "R" }, { "B", "E", "S", "S", ".", "A", "S", "S", "E", "S", "S", ".", "D", "R", "E" } };

    static final String gQuestionsAcross[] = { "", "Back muscle, briefly", "", "", "Place", "", "", "", "", "", "It's seen on the rapids ... or on the rabid", "", "", "", "You're a \"great\" one", "One with pressing work?", "Venetian of fiction", "\"Holy cow!\"", "Certain hybrid manufacturer?", "", "Bush portrayer", "", "Throw up", "\"It's odd, but ...\"", "", "Cut it out", "", "", "", "Undesirable dime bag findings", "Spiritual teacher known as the \"sage of the Shakyas\"", "Joined with", "", "", "Loafer?", "", "Moore with a famous epitaph", "Chalet in the Austrian Alps?", "First name meaning \"born again\"", "Charity event, maybe", "Gambling initials", "Rice field", "Little person", "Laughably few", "", "Big name in farm equipment", "", "\"Jealousy is all the fun you think they had\" quipster Jong", "\"My noble father, I do perceive here a divided duty\" speaker", "", "", "", "Words mouthed at football games", "", "Dead tired", "Painter buried in the Giverny church cemetery", "", "", "", "First prime number", "Lubricates, in a way", "Losing it", "Poetic contraction", "Gershwin title woman", "Get a status update on", "\"The Chronic\" rapper", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" };

    static final String AcrossVector[] = { "<HTML>1. Back muscle, briefly</HTML>", "<HTML>4. Place</HTML>", "<HTML>10. It's seen on the rapids<p>... or on the rabid</HTML>", "<HTML>14. You're a \"great\" one</HTML>", "<HTML>15. One with pressing work?</HTML>", "<HTML>16. Venetian of fiction</HTML>", "<HTML>17. \"Holy cow!\"</HTML>", "<HTML>18. Certain hybrid<p>manufacturer?</HTML>", "<HTML>20. Bush portrayer</HTML>", "<HTML>22. Throw up</HTML>", "<HTML>23. \"It's odd, but ...\"</HTML>", "<HTML>25. Cut it out</HTML>", "<HTML>29. Undesirable dime bag<p>findings</HTML>", "<HTML>30. Spiritual teacher known<p>as the \"sage of the<p>Shakyas\"</HTML>", "<HTML>31. Joined with</HTML>", "<HTML>34. Loafer?</HTML>", "<HTML>36. Moore with a famous<p>epitaph</HTML>", "<HTML>37. Chalet in the Austrian<p>Alps?</HTML>", "<HTML>38. First name meaning<p>\"born again\"</HTML>", "<HTML>39. Charity event, maybe</HTML>", "<HTML>40. Gambling initials</HTML>", "<HTML>41. Rice field</HTML>", "<HTML>42. Little person</HTML>", "<HTML>43. Laughably few</HTML>", "<HTML>45. Big name in farm<p>equipment</HTML>", "<HTML>47. \"Jealousy is all the<p>fun you think they had\"<p>quipster Jong</HTML>", "<HTML>48. \"My noble father, I do<p>perceive here a divided<p>duty\" speaker</HTML>", "<HTML>52. Words mouthed at<p>football games</HTML>", "<HTML>54. Dead tired</HTML>", "<HTML>55. Painter buried in the<p>Giverny church cemetery</HTML>", "<HTML>59. First prime number</HTML>", "<HTML>60. Lubricates, in a way</HTML>", "<HTML>61. Losing it</HTML>", "<HTML>62. Poetic contraction</HTML>", "<HTML>63. Gershwin title woman</HTML>", "<HTML>64. Get a status update on</HTML>", "<HTML>65. \"The Chronic\" rapper</HTML>" };

    static final String gQuestionsDown[] = { "", "Symbols of peace", "Not together", "José Carreras, e.g.", "Yelled at", "Big name in crossword blogging", "Cheat", "Dan Brown thriller ... or supernatural beings hiding in 18-, 23-, 48-, and 55-Across", "Skeptical", "First name in crime fiction", "Like some baseball caps", "Canoeist's need", "\"Just a number\", supposedly", "Brother of 39-Down", "", "", "", "", "", "Happen", "", "Goes on and on and ...", "", "", "Add to your site, as a YouTube video", "", "First name in '50s politics", "Dole (out)", "Helped, as with a transition", "", "Honey maker", "Words of feigned denial", "One at a restaurant", "City featuring the Palm Islands", "", "He lost to Obama in the 2004 Illinois Senate race", "", "", "Narrow beam of light", "Brother of 13-Down", "", "Sock pattern", "Forges", "", "Go downhill, in a way", "", "Car bombs?", "", "Stadium toppers", "Chose", "Like Twitter, vis-à-vis Blogger", "Love to death", "", "Sedona sight", "", "Corn holder", "Misstate the truth", "Unser and Bundy", "Never, to Nietzsche", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" };

    static final String DownVector[] = { "<HTML>1. Symbols of peace</HTML>", "<HTML>2. Not together</HTML>", "<HTML>3. José Carreras, e.g.</HTML>", "<HTML>4. Yelled at</HTML>", "<HTML>5. Big name in crossword<p>blogging</HTML>", "<HTML>6. Cheat</HTML>", "<HTML>7. Dan Brown thriller ...<p>or supernatural beings<p>hiding in 18-, 23-, 48-,<p>and 55-Across</HTML>", "<HTML>8. Skeptical</HTML>", "<HTML>9. First name in crime<p>fiction</HTML>", "<HTML>10. Like some baseball caps</HTML>", "<HTML>11. Canoeist's need</HTML>", "<HTML>12. \"Just a number\",<p>supposedly</HTML>", "<HTML>13. Brother of 39-Down</HTML>", "<HTML>19. Happen</HTML>", "<HTML>21. Goes on and on and ...</HTML>", "<HTML>24. Add to your site, as a<p>YouTube video</HTML>", "<HTML>26. First name in '50s<p>politics</HTML>", "<HTML>27. Dole (out)</HTML>", "<HTML>28. Helped, as with a<p>transition</HTML>", "<HTML>30. Honey maker</HTML>", "<HTML>31. Words of feigned denial</HTML>", "<HTML>32. One at a restaurant</HTML>", "<HTML>33. City featuring the Palm<p>Islands</HTML>", "<HTML>35. He lost to Obama in the<p>2004 Illinois Senate race</HTML>", "<HTML>38. Narrow beam of light</HTML>", "<HTML>39. Brother of 13-Down</HTML>", "<HTML>41. Sock pattern</HTML>", "<HTML>42. Forges</HTML>", "<HTML>44. Go downhill, in a way</HTML>", "<HTML>46. Car bombs?</HTML>", "<HTML>48. Stadium toppers</HTML>", "<HTML>49. Chose</HTML>", "<HTML>50. Like Twitter,<p>vis-à-vis Blogger</HTML>", "<HTML>51. Love to death</HTML>", "<HTML>53. Sedona sight</HTML>", "<HTML>55. Corn holder</HTML>", "<HTML>56. Misstate the truth</HTML>", "<HTML>57. Unser and Bundy</HTML>", "<HTML>58. Never, to Nietzsche</HTML>" };

    String gGuesses[][] = new String[kBlocksWide][kBlocksHigh];

    int ColorMatrix[][] = new int[kBlocksWide][kBlocksHigh];

    int NumberCorrectAnswers = 0;

    int NumberWhiteSquares = 0;

    boolean gUpdateActiveAreaFlag = false;

    boolean gChangedActiveAreaFlag = false;

    Font buttonFont = null;

    Font tileFont = null;

    public void init() {
        int viewWidth;
        int left, top;
        setLayout(new BorderLayout());
        Panel southPanel = new Panel();
        FlowLayout leftLayout = new FlowLayout(FlowLayout.LEFT);
        southPanel.setLayout(leftLayout);
        RevealAll = new Button("Reveal All");
        RevealAll.addActionListener(this);
        RevealWord = new Button("Reveal Word");
        RevealWord.addActionListener(this);
        CheckAll = new Button("Check All");
        CheckAll.addActionListener(this);
        CheckWord = new Button("Check Word");
        CheckWord.addActionListener(this);
        Font cpFont = new Font("Helvetica", Font.PLAIN, 12);
        String cpString = "   " + copyright + "\n    Created at alexboisvert.com";
        copyrightText = new JTextArea(cpString);
        copyrightText.setEditable(false);
        copyrightText.setBackground(Color.WHITE);
        copyrightText.setBorder(null);
        copyrightText.setFont(cpFont);
        southPanel.add(CheckWord);
        southPanel.add(CheckAll);
        southPanel.add(RevealWord);
        southPanel.add(RevealAll);
        southPanel.add(copyrightText);
        add(southPanel, BorderLayout.SOUTH);
        Panel eastPanel = new Panel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.PAGE_AXIS));
        AcrossList = new JList(AcrossVector);
        AcrossList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AcrossList.setFixedCellWidth(200);
        AcrossList.setSelectedIndex(0);
        AcrossList.setVisibleRowCount(18);
        AcrossList.addListSelectionListener(this);
        JScrollPane pane = new JScrollPane(AcrossList);
        DownList = new JList(DownVector);
        DownList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DownList.setFixedCellWidth(200);
        DownList.setVisibleRowCount(18);
        DownList.addListSelectionListener(this);
        JScrollPane pane2 = new JScrollPane(DownList);
        AcrossText = new JTextField("Across");
        DownText = new JTextField("Down");
        Font helFontBold = new Font("Helvetica", Font.BOLD, 14);
        Font helFont = new Font("Helvetica", Font.PLAIN, 14);
        AcrossText.setFont(helFontBold);
        AcrossText.setEditable(false);
        AcrossText.setBackground(Color.WHITE);
        AcrossText.setBorder(null);
        DownText.setFont(helFontBold);
        DownText.setEditable(false);
        DownText.setBackground(Color.WHITE);
        DownText.setBorder(null);
        eastPanel.add(AcrossText);
        eastPanel.add(pane);
        eastPanel.add(DownText);
        eastPanel.add(pane2);
        add(eastPanel, BorderLayout.EAST);
        Panel northPanel = new Panel();
        northPanel.setLayout(leftLayout);
        titleText = new JTextField(title);
        authorText = new JTextField(author);
        titleText.setFont(helFontBold);
        titleText.setEditable(false);
        titleText.setBackground(Color.WHITE);
        titleText.setBorder(null);
        authorText.setEditable(false);
        authorText.setFont(helFont);
        authorText.setBackground(Color.WHITE);
        authorText.setBorder(null);
        northPanel.add(titleText);
        northPanel.add(authorText);
        add(northPanel, BorderLayout.NORTH);
        NewGame();
        buttonFont = new java.awt.Font("Courier", Font.PLAIN, 12);
        tileFont = new java.awt.Font("Helvetica", Font.PLAIN, 36);
    }

    public void NewGame() {
        for (int j = 0; j < kBlocksHigh; j++) {
            for (int i = 0; i < kBlocksWide; i++) {
                gGuesses[i][j] = "";
                if (layout[j][i] != -1) {
                    NumberWhiteSquares++;
                }
            }
        }
        gOldBlockMinY = 0;
        gOldBlockMaxY = 0;
        gOldBlockMinX = 0;
        gOldBlockMaxX = 0;
        gDirection = kAcross;
        gCurX = 0;
        gCurY = 0;
        SetActiveBlock(gCurX, gCurY, gDirection);
    }

    public void paint(Graphics g) {
        int left = 0;
        int right = kAppWidth - 1;
        int top = 0;
        int bottom = kAppHeight - 1;
        int tempLeft = 0;
        int tempRight = 0;
        int tempTop = 0;
        int viewWidth;
        int viewHeight;
        int buttonWidth = 0;
        int buttonHeight = 0;
        int buttonLeft = 0;
        int buttonTop = 0;
        int tileLeft;
        int tileTop;
        g.setColor(getForeground());
        Font f = new java.awt.Font("Helvetica", 0, 12);
        g.setFont(f);
        Font numFont = new java.awt.Font("Helvetica", 0, 9);
        Font answerFont = new java.awt.Font("Helvetica", Font.BOLD, 16);
        Font questionFont = new java.awt.Font("Helvetica", 0, 18);
        Font questionFont18 = new java.awt.Font("Helvetica", 0, 14);
        FontMetrics answerFontMetrics = g.getFontMetrics(answerFont);
        FontMetrics questionFontMetrics = g.getFontMetrics(questionFont);
        FontMetrics questionFont18Metrics = g.getFontMetrics(questionFont18);
        viewWidth = kBlocksWide * kBlockWidth;
        viewHeight = kBlocksHigh * kBlockHeight;
        top = kPaddingTop;
        left = kPaddingLeft;
        g.setColor(Color.white);
        g.fill3DRect(left, top, viewWidth, kQuestionAreaHeight, false);
        g.setFont(f);
        String s = new String(String.valueOf(layout[gBlockMinY][gBlockMinX]));
        s = s.concat(" - ");
        if (gDirection == kAcross) s = s.concat("across"); else s = s.concat("down");
        g.drawString(s, left + 5, top + 12);
        g.setFont(questionFont);
        if (gDirection == kAcross) {
            Font userFont = questionFont;
            if (questionFontMetrics.stringWidth(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]]) > viewWidth - 4) {
                userFont = questionFont18;
                g.setFont(questionFont18);
            }
            g.drawString(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]], left + 5, (top + kQuestionAreaHeight) - 8);
        } else {
            Font userFont = questionFont;
            if (questionFontMetrics.stringWidth(gQuestionsDown[layout[gBlockMinY][gBlockMinX]]) > viewWidth - 4) {
                userFont = questionFont18;
                g.setFont(questionFont18);
            }
            g.drawString(gQuestionsDown[layout[gBlockMinY][gBlockMinX]], left + 5, (top + kQuestionAreaHeight) - 8);
        }
        left = kPaddingLeft;
        top = (kPaddingTop + kPaddingClues) + kQuestionAreaHeight;
        for (int j = 0; j < kBlocksHigh; j++) {
            for (int i = 0; i < kBlocksWide; i++) {
                tempLeft = left + (i * kBlockWidth);
                tempTop = top + (j * kBlockHeight);
                if (InActiveBlock(i, j)) {
                    if (i == gCurX && j == gCurY) g.setColor(Color.cyan); else g.setColor(Color.yellow);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                } else {
                    g.setColor(Color.white);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                }
                g.setColor(Color.black);
                g.drawRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                if (layout[j][i] == -1) {
                    g.setColor(Color.black);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                } else if (layout[j][i] != 0) {
                    String numStr = String.valueOf(layout[j][i]);
                    g.setFont(numFont);
                    g.drawString(numStr, tempLeft + 4, tempTop + 10);
                }
                if (layout[j][i] != -1) {
                    if (gGuesses[i][j].length() != 0) {
                        int sWidth = 0;
                        if (ColorMatrix[i][j] == 1) g.setColor(Color.red); else if (ColorMatrix[i][j] == 2) g.setColor(Color.blue); else g.setColor(Color.black);
                        sWidth = answerFontMetrics.stringWidth(gGuesses[i][j]);
                        g.setFont(answerFont);
                        g.drawString(gGuesses[i][j], tempLeft + ((kBlockWidth / 2) - (sWidth / 2) + 3), (tempTop + kBlockHeight) - 3);
                    }
                }
            }
        }
    }

    void PaintWord(Graphics g, int minX, int maxX, int minY, int maxY) {
        int viewWidth = kBlocksWide * kBlockWidth;
        int viewHeight = kBlocksHigh * kBlockHeight;
        int left = kPaddingLeft;
        int top = (kPaddingTop + kPaddingClues) + kQuestionAreaHeight;
        int tempLeft = 0;
        int tempRight = 0;
        int tempTop = 0;
        left += (minX * kBlockWidth);
        top += (minY * kBlockHeight);
        Font f = new java.awt.Font("Helvetica", 0, 12);
        g.setFont(f);
        Font numFont = new java.awt.Font("Helvetica", 0, 9);
        Font answerFont = new java.awt.Font("Helvetica", Font.BOLD, 16);
        FontMetrics answerFontMetrics = g.getFontMetrics(answerFont);
        viewWidth = kBlocksWide * kBlockWidth;
        viewHeight = kBlocksHigh * kBlockHeight;
        left = kPaddingLeft;
        top = (kPaddingTop + kPaddingClues) + kQuestionAreaHeight;
        for (int j = minY; j <= maxY; j++) {
            for (int i = minX; i <= maxX; i++) {
                tempLeft = left + (i * kBlockWidth);
                tempTop = top + (j * kBlockHeight);
                if (InActiveBlock(i, j)) {
                    if (i == gCurX && j == gCurY) g.setColor(Color.cyan); else g.setColor(Color.yellow);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                } else {
                    g.setColor(Color.white);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                }
                g.setColor(Color.black);
                g.drawRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                if (layout[j][i] == -1) {
                    g.setColor(Color.black);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                } else if (layout[j][i] != 0) {
                    String numStr = String.valueOf(layout[j][i]);
                    g.setFont(numFont);
                    g.drawString(numStr, tempLeft + 4, tempTop + 10);
                }
                if (layout[j][i] != -1) {
                    if (gGuesses[i][j].length() != 0) {
                        int sWidth = 0;
                        if (ColorMatrix[i][j] == 1) g.setColor(Color.red); else if (ColorMatrix[i][j] == 2) g.setColor(Color.blue); else g.setColor(Color.black);
                        sWidth = answerFontMetrics.stringWidth(gGuesses[i][j]);
                        g.setFont(answerFont);
                        g.drawString(gGuesses[i][j], tempLeft + ((kBlockWidth / 2) - (sWidth / 2) + 3), (tempTop + kBlockHeight) - 3);
                    }
                }
            }
        }
    }

    void PaintQuestionArea(Graphics g) {
        Font f = new java.awt.Font("Helvetica", 0, 12);
        Font questionFont = new java.awt.Font("Helvetica", 0, 18);
        Font questionFont18 = new java.awt.Font("Helvetica", 0, 14);
        FontMetrics questionFontMetrics = g.getFontMetrics(questionFont);
        int viewWidth = kBlocksWide * kBlockWidth;
        int viewHeight = kBlocksHigh * kBlockHeight;
        int top = kPaddingTop;
        int left = kPaddingLeft;
        g.setColor(Color.white);
        g.fill3DRect(left, top, viewWidth, kQuestionAreaHeight, false);
        g.setFont(f);
        String s = new String(String.valueOf(layout[gBlockMinY][gBlockMinX]));
        s = s.concat(" - ");
        if (gDirection == kAcross) s = s.concat("across"); else s = s.concat("down");
        g.drawString(s, left + 5, top + 12);
        g.setFont(questionFont);
        int fontSize = 24;
        if (gDirection == kAcross) {
            Font userFont = questionFont;
            if (questionFontMetrics.stringWidth(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]]) > viewWidth - 4) {
                userFont = questionFont18;
                g.setFont(questionFont18);
            }
            g.drawString(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]], left + 5, (top + kQuestionAreaHeight) - 8);
        } else {
            Font userFont = questionFont;
            if (questionFontMetrics.stringWidth(gQuestionsDown[layout[gBlockMinY][gBlockMinX]]) > viewWidth - 4) {
                userFont = questionFont18;
                g.setFont(questionFont18);
            }
            g.drawString(gQuestionsDown[layout[gBlockMinY][gBlockMinX]], left + 5, (top + kQuestionAreaHeight) - 8);
        }
    }

    private boolean InActiveBlock(int x, int y) {
        if (x < gBlockMinX) return (false);
        if (x > gBlockMaxX) return (false);
        if (y < gBlockMinY) return (false);
        if (y > gBlockMaxY) return (false);
        return (true);
    }

    private void SetActiveBlock(int x, int y, int direction) {
        int tempx;
        int tempy;
        gOldBlockMinY = gBlockMinY;
        gOldBlockMaxY = gBlockMaxY;
        gOldBlockMinX = gBlockMinX;
        gOldBlockMaxX = gBlockMaxX;
        if (direction == kAcross) {
            gBlockMinY = y;
            gBlockMaxY = y;
            tempx = x;
            while (tempx > 0 && layout[y][tempx] != -1) {
                tempx--;
            }
            if (tempx > 0) gBlockMinX = tempx + 1; else {
                if (layout[y][0] == -1) gBlockMinX = 1; else gBlockMinX = 0;
            }
            tempx = x;
            while (tempx < kBlocksWide && layout[y][tempx] != -1) {
                tempx++;
            }
            gBlockMaxX = tempx - 1;
        } else {
            gBlockMinX = x;
            gBlockMaxX = x;
            tempy = y;
            while (tempy > 0 && layout[tempy][x] != -1) {
                tempy--;
            }
            if (tempy > 0) gBlockMinY = tempy + 1; else {
                if (layout[0][x] == -1) gBlockMinY = 1; else gBlockMinY = 0;
            }
            tempy = y;
            while (tempy < kBlocksHigh && layout[tempy][x] != -1) {
                tempy++;
            }
            gBlockMaxY = tempy - 1;
        }
    }

    public void update(Graphics g) {
        if (clipLeft != -1 && clipTop != -1) {
            clipLeft = clipTop = clipWidth = clipHeight = -1;
        }
        if (gChangedActiveAreaFlag == false && gUpdateActiveAreaFlag == false) {
            paint(g);
            return;
        }
        if (gChangedActiveAreaFlag == true) {
            PaintQuestionArea(g);
            PaintWord(g, gOldBlockMinX, gOldBlockMaxX, gOldBlockMinY, gOldBlockMaxY);
            PaintWord(g, gBlockMinX, gBlockMaxX, gBlockMinY, gBlockMaxY);
            gChangedActiveAreaFlag = false;
            return;
        }
        if (gUpdateActiveAreaFlag == true) {
            gUpdateActiveAreaFlag = false;
            PaintWord(g, gBlockMinX, gBlockMaxX, gBlockMinY, gBlockMaxY);
            return;
        }
    }

    private void beep() {
        play(getCodeBase(), "nope.au");
    }

    public boolean mouseDown(java.awt.Event evt, int x, int y) {
        int viewWidth = kBlocksWide * kBlockWidth;
        int left = kPaddingLeft;
        int top = (kPaddingTop + kPaddingClues) + kQuestionAreaHeight;
        requestFocus();
        if (x < left) return false;
        if (y < top) return false;
        int j = y - top;
        j /= kBlockHeight;
        int i = x - left;
        i /= kBlockWidth;
        if (i >= 0 && i < kBlocksWide && j >= 0 && j < kBlocksHigh) {
            if ((i == gCurX && j == gCurY)) {
                if ((gDirection == kAcross && j == 0 && layout[j + 1][i] != -1) || (gDirection == kDown && i == 0 && layout[j][i + 1] != -1) || (gDirection == kAcross && (layout[j - 1][i] != -1 || layout[j + 1][i] != -1)) || (gDirection == kDown && (layout[j][i - 1] != -1 || layout[j][i + 1] != -1))) {
                    ChangeDirection();
                    gChangedActiveAreaFlag = true;
                    repaint();
                    ListsUpdate();
                }
            } else {
                if (layout[j][i] != -1) {
                    if (InActiveBlock(i, j)) {
                        gCurX = i;
                        gCurY = j;
                        gUpdateActiveAreaFlag = true;
                        repaint();
                    } else {
                        gCurX = i;
                        gCurY = j;
                        if ((gDirection == kAcross && (i == 0 || layout[j][i - 1] == -1) && (i == kBlocksWide || layout[j][i + 1] == -1))) {
                            gDirection = kDown;
                        }
                        if ((gDirection == kDown && (j == 0 || layout[j - 1][i] == -1) && (j == kBlocksHigh || layout[j + 1][i] == -1))) {
                            gDirection = kAcross;
                        }
                        SetActiveBlock(i, j, gDirection);
                        gChangedActiveAreaFlag = true;
                        repaint();
                    }
                    ListsUpdate();
                    return true;
                }
            }
        }
        return true;
    }

    public boolean mouseUp(java.awt.Event evt, int x, int y) {
        requestFocus();
        return true;
    }

    public boolean mouseDrag(java.awt.Event evt, int x, int y) {
        requestFocus();
        return true;
    }

    public boolean mouseExit(java.awt.Event evt) {
        return true;
    }

    public boolean mouseEnter(java.awt.Event evt) {
        requestFocus();
        return true;
    }

    public boolean mouseMove(java.awt.Event evt, int x, int y) {
        requestFocus();
        return true;
    }

    public boolean keyDown(java.awt.Event evt, int key) {
        int tempx;
        int tempy;
        boolean tempWIC = false;
        if ((key >= 'A' && key <= 'Z') || (key >= 'a' && key <= 'z')) {
            char charArray[] = new char[1];
            charArray[0] = (char) key;
            tempWIC = WasItCorrect(gCurX, gCurY);
            gGuesses[gCurX][gCurY] = new String(charArray);
            gGuesses[gCurX][gCurY] = gGuesses[gCurX][gCurY].toUpperCase();
            if (WasItCorrect(gCurX, gCurY) == true && tempWIC == false) {
                NumberCorrectAnswers++;
            }
            if (WasItCorrect(gCurX, gCurY) == false && tempWIC == true) {
                NumberCorrectAnswers--;
            }
            if (gDirection == kAcross) {
                if (gCurX < kBlocksWide - 1 && layout[gCurY][gCurX + 1] != -1) {
                    gCurX++;
                    ListsUpdate();
                }
            } else {
                if (gCurY < kBlocksHigh - 1 && layout[gCurY + 1][gCurX] != -1) {
                    gCurY++;
                    ListsUpdate();
                }
            }
            gUpdateActiveAreaFlag = true;
            repaint();
            if (NumberCorrectAnswers == NumberWhiteSquares) {
                JOptionPane.showMessageDialog(null, "The puzzle has been successfully completed.", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
                NumberWhiteSquares++;
                repaint();
            }
            return true;
        }
        switch((char) key) {
            case Event.RIGHT:
                if (gDirection == kAcross) {
                    if (gCurX < kBlocksWide - 1) {
                        tempx = gCurX + 1;
                        tempy = gCurY;
                        while (layout[gCurY][tempx] == -1) {
                            ++tempx;
                            if (tempx == kBlocksWide) {
                                tempx = gCurX;
                            }
                        }
                        gCurX = tempx;
                        gCurY = tempy;
                        if (InActiveBlock(tempx, tempy)) {
                            gUpdateActiveAreaFlag = true;
                            repaint();
                        } else {
                            SetActiveBlock(tempx, tempy, gDirection);
                            gChangedActiveAreaFlag = true;
                            repaint();
                        }
                        ListsUpdate();
                    }
                } else {
                    if ((gCurX == 0 || layout[gCurY][gCurX - 1] == -1) && (gCurX == kBlocksWide - 1 || layout[gCurY][gCurX + 1] == -1)) {
                        if (gCurX < kBlocksWide - 1) {
                            tempx = gCurX + 1;
                            tempy = gCurY;
                            while (layout[gCurY][tempx] == -1) {
                                ++tempx;
                                if (tempx == kBlocksWide) {
                                    tempx = gCurX;
                                }
                            }
                            gCurX = tempx;
                            gCurY = tempy;
                            if (InActiveBlock(tempx, tempy)) {
                                gUpdateActiveAreaFlag = true;
                                repaint();
                            } else {
                                SetActiveBlock(tempx, tempy, gDirection);
                                gChangedActiveAreaFlag = true;
                                repaint();
                            }
                            ListsUpdate();
                        }
                    } else {
                        ChangeDirection();
                        if (gGuesses[gCurX][gCurY] != "" && gCurX < kBlocksWide - 1 && layout[gCurY][gCurX + 1] != -1) {
                            gCurX++;
                        }
                        gChangedActiveAreaFlag = true;
                        repaint();
                        ListsUpdate();
                    }
                }
                break;
            case Event.LEFT:
                if (gDirection == kAcross) {
                    if (gCurX != 0) {
                        tempx = gCurX - 1;
                        tempy = gCurY;
                        while (layout[gCurY][tempx] == -1) {
                            --tempx;
                            if (tempx == -1) {
                                tempx = gCurX;
                            }
                        }
                        gCurX = tempx;
                        gCurY = tempy;
                        if (InActiveBlock(tempx, tempy)) {
                            gUpdateActiveAreaFlag = true;
                            repaint();
                        } else {
                            SetActiveBlock(tempx, tempy, gDirection);
                            gChangedActiveAreaFlag = true;
                            repaint();
                        }
                        ListsUpdate();
                    }
                } else {
                    if ((gCurX == 0 || layout[gCurY][gCurX - 1] == -1) && (gCurX == kBlocksWide - 1 || layout[gCurY][gCurX + 1] == -1)) {
                        if (gCurX != 0) {
                            tempx = gCurX - 1;
                            tempy = gCurY;
                            while (layout[gCurY][tempx] == -1) {
                                --tempx;
                                if (tempx == -1) {
                                    tempx = gCurX;
                                }
                            }
                            gCurX = tempx;
                            gCurY = tempy;
                            if (InActiveBlock(tempx, tempy)) {
                                gUpdateActiveAreaFlag = true;
                                repaint();
                            } else {
                                SetActiveBlock(tempx, tempy, gDirection);
                                gChangedActiveAreaFlag = true;
                                repaint();
                            }
                            ListsUpdate();
                        }
                    } else {
                        ChangeDirection();
                        if (gGuesses[gCurX][gCurY] != "" && gCurX < kBlocksWide - 1 && layout[gCurY][gCurX + 1] != -1) {
                            gCurX--;
                        }
                        gChangedActiveAreaFlag = true;
                        repaint();
                        ListsUpdate();
                    }
                }
                break;
            case Event.UP:
                if (gDirection == kDown) {
                    if (gCurY != 0) {
                        tempx = gCurX;
                        tempy = gCurY - 1;
                        while (layout[tempy][gCurX] == -1) {
                            --tempy;
                            if (tempy == -1) {
                                tempy = gCurY;
                            }
                        }
                        gCurX = tempx;
                        gCurY = tempy;
                        if (InActiveBlock(tempx, tempy)) {
                            gUpdateActiveAreaFlag = true;
                            repaint();
                        } else {
                            SetActiveBlock(tempx, tempy, gDirection);
                            gChangedActiveAreaFlag = true;
                            repaint();
                        }
                        ListsUpdate();
                    }
                } else {
                    if ((gCurY == 0 || layout[gCurY - 1][gCurX] == -1) && (gCurY == kBlocksHigh - 1 || layout[gCurY + 1][gCurX] == -1)) {
                        if (gCurY != 0) {
                            tempx = gCurX;
                            tempy = gCurY - 1;
                            while (layout[tempy][gCurX] == -1) {
                                --tempy;
                                if (tempy == -1) {
                                    tempy = gCurY;
                                }
                            }
                            gCurX = tempx;
                            gCurY = tempy;
                            if (InActiveBlock(tempx, tempy)) {
                                gUpdateActiveAreaFlag = true;
                                repaint();
                            } else {
                                SetActiveBlock(tempx, tempy, gDirection);
                                gChangedActiveAreaFlag = true;
                                repaint();
                            }
                            ListsUpdate();
                        }
                    } else {
                        ChangeDirection();
                        if (gGuesses[gCurX][gCurY] != "" && gCurX < kBlocksWide - 1 && layout[gCurY][gCurX + 1] != -1) {
                            gCurY--;
                        }
                        gChangedActiveAreaFlag = true;
                        repaint();
                        ListsUpdate();
                    }
                }
                break;
            case Event.DOWN:
                if (gDirection == kDown) {
                    if (gCurY < kBlocksHigh - 1) {
                        tempx = gCurX;
                        tempy = gCurY + 1;
                        while (layout[tempy][gCurX] == -1) {
                            ++tempy;
                            if (tempy == kBlocksHigh) {
                                tempy = gCurY;
                            }
                        }
                        gCurX = tempx;
                        gCurY = tempy;
                        if (InActiveBlock(tempx, tempy)) {
                            gUpdateActiveAreaFlag = true;
                            repaint();
                        } else {
                            SetActiveBlock(tempx, tempy, gDirection);
                            gChangedActiveAreaFlag = true;
                            repaint();
                        }
                        ListsUpdate();
                    }
                } else {
                    if ((gCurY == 0 || layout[gCurY - 1][gCurX] == -1) && (gCurY == kBlocksHigh - 1 || layout[gCurY + 1][gCurX] == -1)) {
                        if (gCurY < kBlocksHigh - 1) {
                            tempx = gCurX;
                            tempy = gCurY + 1;
                            while (layout[tempy][gCurX] == -1) {
                                ++tempy;
                                if (tempy == kBlocksHigh) {
                                    tempy = gCurY;
                                }
                            }
                            gCurX = tempx;
                            gCurY = tempy;
                            if (InActiveBlock(tempx, tempy)) {
                                gUpdateActiveAreaFlag = true;
                                repaint();
                            } else {
                                SetActiveBlock(tempx, tempy, gDirection);
                                gChangedActiveAreaFlag = true;
                                repaint();
                            }
                            ListsUpdate();
                        }
                    } else {
                        ChangeDirection();
                        if (gGuesses[gCurX][gCurY] != "" && gCurY < kBlocksHigh - 1 && layout[gCurY + 1][gCurX] != -1) {
                            gCurY++;
                        }
                        gChangedActiveAreaFlag = true;
                        repaint();
                        ListsUpdate();
                    }
                }
                break;
            case ' ':
                if (WasItCorrect(gCurX, gCurY) == true) {
                    NumberCorrectAnswers--;
                }
                gGuesses[gCurX][gCurY] = "";
                if (gDirection == kAcross) {
                    if (gCurX < kBlocksWide - 1 && layout[gCurY][gCurX + 1] != -1) {
                        gCurX++;
                    }
                } else {
                    if (gCurY < kBlocksHigh - 1 && layout[gCurY + 1][gCurX] != -1) {
                        gCurY++;
                    }
                }
                gUpdateActiveAreaFlag = true;
                ListsUpdate();
                repaint();
                break;
            case 0x08:
                if (gGuesses[gCurX][gCurY] != "") {
                    if (WasItCorrect(gCurX, gCurY) == true) {
                        NumberCorrectAnswers--;
                    }
                    gGuesses[gCurX][gCurY] = "";
                    gUpdateActiveAreaFlag = true;
                    repaint();
                } else {
                    if (gDirection == kAcross) {
                        if (gCurX != 0 && layout[gCurY][gCurX - 1] != -1) {
                            gCurX--;
                            if (WasItCorrect(gCurX, gCurY) == true) {
                                NumberCorrectAnswers--;
                            }
                            gGuesses[gCurX][gCurY] = "";
                            gUpdateActiveAreaFlag = true;
                            repaint();
                            ListsUpdate();
                        }
                    } else {
                        if (gCurY != 0 && layout[gCurY - 1][gCurX] != -1) {
                            gCurY--;
                            if (WasItCorrect(gCurX, gCurY) == true) {
                                NumberCorrectAnswers--;
                            }
                            gGuesses[gCurX][gCurY] = "";
                            gUpdateActiveAreaFlag = true;
                            repaint();
                            ListsUpdate();
                        }
                    }
                }
                break;
            case Event.DELETE:
                if (gGuesses[gCurX][gCurY] != "") {
                    if (WasItCorrect(gCurX, gCurY) == true) {
                        NumberCorrectAnswers--;
                    }
                    gGuesses[gCurX][gCurY] = "";
                    gUpdateActiveAreaFlag = true;
                    repaint();
                }
                break;
            case 0x03:
                EnterPressed();
                break;
            case 0x0A:
                EnterPressed();
                break;
            default:
                beep();
                break;
        }
        return true;
    }

    int random(int max) {
        return (int) Math.floor(Math.random() * max);
    }

    boolean PtInRect(int x, int y, int left, int top, int right, int bottom) {
        if (x < left) return (false);
        if (x > right) return (false);
        if (y < top) return (false);
        if (y > bottom) return (false);
        return (true);
    }

    void ChangeDirection() {
        if (gDirection == kDown) {
            gDirection = kUp;
        } else gDirection = kDown;
        SetActiveBlock(gCurX, gCurY, gDirection);
    }

    public boolean WasItCorrect(int i, int j) {
        if (gGuesses[i][j].equalsIgnoreCase(answers[j][i]) == true) {
            return true;
        } else {
            return false;
        }
    }

    public void Reveal(int x1, int x2, int y1, int y2) {
        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                if (WasItCorrect(i, j) == false) {
                    ColorMatrix[i][j] = 1;
                    gGuesses[i][j] = answers[j][i];
                    NumberCorrectAnswers++;
                }
            }
        }
    }

    public void Check(int x1, int x2, int y1, int y2) {
        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                if (gGuesses[i][j] != "" && WasItCorrect(i, j) == false) {
                    gGuesses[i][j] = "%";
                    if (ColorMatrix[i][j] != 1) {
                        ColorMatrix[i][j] = 2;
                    }
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == RevealAll) {
            Reveal(0, kBlocksWide - 1, 0, kBlocksHigh - 1);
            repaint();
        }
        if (e.getSource() == RevealWord) {
            Reveal(gBlockMinX, gBlockMaxX, gBlockMinY, gBlockMaxY);
            repaint();
        }
        if (e.getSource() == CheckWord) {
            Check(gBlockMinX, gBlockMaxX, gBlockMinY, gBlockMaxY);
            repaint();
        }
        if (e.getSource() == CheckAll) {
            Check(0, kBlocksWide - 1, 0, kBlocksHigh - 1);
            repaint();
        }
    }

    public void valueChanged(ListSelectionEvent evt) {
        JList ListSource = (JList) evt.getSource();
        if (ListSource.getSelectedIndex() != -1) {
            int AcrossArray[][] = { { 0, 0 }, { 4, 0 }, { 11, 0 }, { 0, 1 }, { 4, 1 }, { 11, 1 }, { 0, 2 }, { 4, 2 }, { 0, 3 }, { 7, 3 }, { 0, 4 }, { 10, 4 }, { 3, 5 }, { 9, 5 }, { 0, 6 }, { 6, 6 }, { 12, 6 }, { 0, 7 }, { 5, 7 }, { 11, 7 }, { 0, 8 }, { 4, 8 }, { 10, 8 }, { 0, 9 }, { 7, 9 }, { 0, 10 }, { 6, 10 }, { 3, 11 }, { 9, 11 }, { 0, 12 }, { 12, 12 }, { 0, 13 }, { 5, 13 }, { 12, 13 }, { 0, 14 }, { 5, 14 }, { 12, 14 } };
            int DownArray[][] = { { 0, 0 }, { 1, 0 }, { 2, 0 }, { 4, 0 }, { 5, 0 }, { 6, 0 }, { 7, 0 }, { 8, 0 }, { 9, 0 }, { 11, 0 }, { 12, 0 }, { 13, 0 }, { 14, 0 }, { 10, 2 }, { 3, 3 }, { 6, 4 }, { 12, 4 }, { 13, 4 }, { 14, 4 }, { 9, 5 }, { 0, 6 }, { 1, 6 }, { 2, 6 }, { 8, 6 }, { 5, 7 }, { 11, 7 }, { 4, 8 }, { 10, 8 }, { 3, 9 }, { 9, 9 }, { 6, 10 }, { 12, 10 }, { 13, 10 }, { 14, 10 }, { 5, 11 }, { 0, 12 }, { 1, 12 }, { 2, 12 }, { 8, 12 } };
            if (ListSource == AcrossList) {
                int CurIndex = AcrossList.getSelectedIndex();
                int i = AcrossArray[CurIndex][0];
                int j = AcrossArray[CurIndex][1];
                if (InActiveBlock(i, j) == false || gDirection == kDown) {
                    gCurX = i;
                    gCurY = j;
                    gDirection = kAcross;
                    SetActiveBlock(i, j, gDirection);
                    gChangedActiveAreaFlag = true;
                    repaint();
                    requestFocus();
                }
                DownList.getSelectionModel().clearSelection();
            }
            if (ListSource == DownList) {
                int CurIndex = DownList.getSelectedIndex();
                int i = DownArray[CurIndex][0];
                int j = DownArray[CurIndex][1];
                if (InActiveBlock(i, j) == false || gDirection == kAcross) {
                    gCurX = i;
                    gCurY = j;
                    gDirection = kDown;
                    SetActiveBlock(i, j, gDirection);
                    gChangedActiveAreaFlag = true;
                    repaint();
                    requestFocus();
                }
                AcrossList.getSelectionModel().clearSelection();
            }
        }
    }

    public void EnterPressed() {
        if (gDirection == kAcross) {
            int enterArray[][] = { { 0, 0 }, { 4, 0 }, { 0, 0 }, { 0, 0 }, { 11, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 1 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 4, 1 }, { 11, 1 }, { 0, 2 }, { 4, 2 }, { 0, 3 }, { 0, 0 }, { 7, 3 }, { 0, 0 }, { 0, 4 }, { 10, 4 }, { 0, 0 }, { 3, 5 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 9, 5 }, { 0, 6 }, { 6, 6 }, { 0, 0 }, { 0, 0 }, { 12, 6 }, { 0, 0 }, { 0, 7 }, { 5, 7 }, { 11, 7 }, { 0, 8 }, { 4, 8 }, { 10, 8 }, { 0, 9 }, { 7, 9 }, { 0, 0 }, { 0, 10 }, { 0, 0 }, { 6, 10 }, { 3, 11 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 9, 11 }, { 0, 0 }, { 0, 12 }, { 12, 12 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 13 }, { 5, 13 }, { 12, 13 }, { 0, 14 }, { 5, 14 }, { 12, 14 }, { 0, 0 } };
            int curNum = layout[gBlockMinY][gBlockMinX];
            gCurX = enterArray[curNum][0];
            gCurY = enterArray[curNum][1];
        } else {
            int enterArray[][] = { { 0, 0 }, { 1, 0 }, { 2, 0 }, { 4, 0 }, { 5, 0 }, { 6, 0 }, { 7, 0 }, { 8, 0 }, { 9, 0 }, { 11, 0 }, { 12, 0 }, { 13, 0 }, { 14, 0 }, { 10, 2 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 3, 3 }, { 0, 0 }, { 6, 4 }, { 0, 0 }, { 0, 0 }, { 12, 4 }, { 0, 0 }, { 13, 4 }, { 14, 4 }, { 9, 5 }, { 0, 0 }, { 0, 6 }, { 1, 6 }, { 2, 6 }, { 8, 6 }, { 0, 0 }, { 5, 7 }, { 0, 0 }, { 0, 0 }, { 11, 7 }, { 4, 8 }, { 0, 0 }, { 10, 8 }, { 3, 9 }, { 0, 0 }, { 9, 9 }, { 0, 0 }, { 6, 10 }, { 0, 0 }, { 12, 10 }, { 13, 10 }, { 14, 10 }, { 5, 11 }, { 0, 0 }, { 0, 12 }, { 0, 0 }, { 1, 12 }, { 2, 12 }, { 8, 12 }, { 0, 0 } };
            int curNum = layout[gBlockMinY][gBlockMinX];
            gCurX = enterArray[curNum][0];
            gCurY = enterArray[curNum][1];
        }
        SetActiveBlock(gCurX, gCurY, gDirection);
        gChangedActiveAreaFlag = true;
        repaint();
        ListsUpdate();
    }

    public void ListsUpdate() {
        int AcrossIndices[] = { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 4, 5, 6, 7, 0, 8, 0, 9, 10, 0, 11, 0, 0, 0, 12, 13, 14, 0, 0, 15, 0, 16, 17, 18, 19, 20, 21, 22, 23, 0, 24, 0, 25, 26, 0, 0, 0, 27, 0, 28, 29, 0, 0, 0, 30, 31, 32, 33, 34, 35, 36 };
        int DownIndices[] = { 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 15, 0, 16, 17, 18, 0, 19, 20, 21, 22, 0, 23, 0, 0, 24, 25, 0, 26, 27, 0, 28, 0, 29, 0, 30, 31, 32, 33, 0, 34, 0, 35, 36, 37, 38 };
        int lonum, cluevalue;
        if (gDirection == kAcross) {
            lonum = layout[gBlockMinY][gBlockMinX];
            cluevalue = AcrossIndices[lonum];
            AcrossList.setSelectedIndex(cluevalue);
            AcrossList.ensureIndexIsVisible(cluevalue);
            DownList.getSelectionModel().clearSelection();
        } else {
            lonum = layout[gBlockMinY][gBlockMinX];
            cluevalue = DownIndices[lonum];
            DownList.setSelectedIndex(cluevalue);
            DownList.ensureIndexIsVisible(cluevalue);
            AcrossList.getSelectionModel().clearSelection();
        }
    }
}

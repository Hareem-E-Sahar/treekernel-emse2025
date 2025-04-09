package quizgame.mainscreen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.LinkedList;
import javax.swing.JPanel;
import quizgame.common.InGameCategory;
import quizgame.common.InGameQuestion;

/**
 * A panel for showing the main screen
 * @author rheo
 */
public class MainScreenPanel extends JPanel {

    private InGameCategory[] categories;

    private boolean isInitialized = false;

    private String textFont = "Sans-Serif";

    private boolean fontSizeIsKnown = false;

    private Font categoryFont = null;

    private Font showQuestionFont;

    private double boardAnimationCounter = 0;

    private double questionAnimationCounter = 0;

    private double framesPerSecond = 30;

    private boolean boardVisible = false;

    private double categoryStartTime = 0;

    private double questionStartTime = 800;

    private double categoryTime = 150;

    private double questionTime = 100;

    private double categoryGrowTime = 400;

    private double questionGrowTime = 200;

    private int numberOfStepsBetweenQuestions = 3;

    private double totalPaintTime;

    private int categoryPadding = 8;

    private int categoryCornerRadius = 16;

    private double categoryHeightRatio = 0.6;

    private int categoryWidth;

    private int categoryHeight;

    private int questionPadding = 24;

    private int maxNumberOfQuestions;

    private int totalQuestionHeight;

    private int questionHeight;

    private int questionWidth;

    private int questionStartX;

    private double questionPaintTime = 400;

    private double showQuestionSizeRatio = 0.75;

    private int showQuestionPadding = 120;

    private double showQuestionFontRatio = 0.07;

    private int showQuestionWidth;

    private int showQuestionHeight;

    private int showQuestionStartX;

    private int showQuestionStartY;

    private int showQuestionCornerRadius = 24;

    private Color categoryGradientStartColor = new Color(0, 150, 255);

    private Color categoryGradientEndColor = Color.BLACK;

    private Color categoryHighlightGradientStartColor = new Color(150, 255, 0);

    private Color categoryHighlightGradientEndColor = Color.BLACK;

    private Color questionGradientStartColor = new Color(0, 150, 255);

    private Color questionGradientEndColor = questionGradientStartColor.darker();

    private Color showQuestionGradientStartColor = new Color(0, 150, 255, 200);

    private Color showQuestionGradientEndColor = new Color(0, 0, 0, 200);

    private GradientPaint categoryGradient;

    private GradientPaint categoryHighlightGradient;

    private GradientPaint[] questionGradient;

    private GradientPaint[] usedQuestionGradient;

    private GradientPaint showQuestionGradient;

    private Image backdrop = Toolkit.getDefaultToolkit().createImage(new File("img/backdrop.jpg").getAbsolutePath());

    private Image scaledBackdrop;

    private Image logo = Toolkit.getDefaultToolkit().createImage(new File("img/ironquiz.png").getAbsolutePath());

    private Image scaledLogo;

    private String currentQuestion;

    private int currentCategoryIndex;

    private int currentQuestionIndex;

    private Thread updateThread;

    /**
     * Creates a new instance of MainScreenPanel
     * @param dimension dimension of the drawing area
     */
    public MainScreenPanel(Dimension dimension) {
        Runnable updateRunnable;
        addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                resized();
            }
        });
        setPreferredSize(dimension);
        setVisible(true);
        repaint();
        updateRunnable = new Runnable() {

            public synchronized void run() {
                long lastTime = System.nanoTime() / 1000000;
                for (; ; ) {
                    boolean updated = false;
                    long startTime = System.nanoTime() / 1000000;
                    long frameTime = startTime - lastTime;
                    if (boardVisible) {
                        if (boardAnimationCounter < totalPaintTime) {
                            boardAnimationCounter = Math.min(totalPaintTime, boardAnimationCounter + frameTime);
                            updated = true;
                        }
                    } else {
                        if (boardAnimationCounter > 0) {
                            boardAnimationCounter = Math.max(0, boardAnimationCounter - frameTime);
                            updated = true;
                        }
                    }
                    if (currentQuestion != null) {
                        if (questionAnimationCounter < questionPaintTime) {
                            questionAnimationCounter = Math.min(questionPaintTime, questionAnimationCounter + frameTime);
                            updated = true;
                        }
                    } else {
                        if (questionAnimationCounter > 0) {
                            questionAnimationCounter = Math.max(0, questionAnimationCounter - frameTime);
                            updated = true;
                        }
                    }
                    if (!updated) {
                        try {
                            Thread.sleep(Long.MAX_VALUE);
                        } catch (InterruptedException ex) {
                        }
                        lastTime = System.nanoTime() / 1000000;
                        continue;
                    }
                    repaint();
                    for (; ; ) {
                        long sleepTime = (int) (startTime + 1000 / framesPerSecond) - System.nanoTime() / 1000000;
                        if (sleepTime <= 0) {
                            break;
                        }
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                        }
                    }
                    lastTime = startTime;
                }
            }
        };
        updateThread = new Thread(updateRunnable);
        repaint();
        updateThread.start();
    }

    public void setCategories(InGameCategory[] categories) {
        currentQuestion = null;
        if (categories == null || categories.length < 1) {
            boardVisible = false;
        } else {
            boardAnimationCounter = 0;
            this.categories = categories;
            initializeBoard();
            boardVisible = true;
        }
        updateThread.interrupt();
    }

    public void setQuestionUsedUp(int categoryIndex, int questionIndex, boolean usedUp) {
        if (boardVisible) {
            try {
                categories[categoryIndex].questions[questionIndex].usedUp = usedUp;
            } catch (ArrayIndexOutOfBoundsException ex) {
                return;
            }
            repaint();
        }
    }

    public void setCurrentQuestion(int categoryIndex, int questionIndex, String question) {
        if (boardVisible && question != null) {
            categories[categoryIndex].questions[questionIndex].usedUp = true;
            currentQuestion = question;
            currentCategoryIndex = categoryIndex;
            currentQuestionIndex = questionIndex;
            questionAnimationCounter = 0;
            updateThread.interrupt();
        }
    }

    public void setNoCurrentQuestion() {
        currentQuestion = null;
        updateThread.interrupt();
    }

    public void resized() {
        showQuestionWidth = (int) (getWidth() * showQuestionSizeRatio);
        showQuestionHeight = (int) (getHeight() * showQuestionSizeRatio);
        showQuestionStartX = (getWidth() - showQuestionWidth) / 2;
        showQuestionStartY = (getHeight() - showQuestionHeight) / 2;
        showQuestionFont = new Font(textFont, Font.BOLD, (int) (getHeight() * showQuestionFontRatio));
        showQuestionGradient = new GradientPaint(0, showQuestionStartY, showQuestionGradientStartColor, 0, showQuestionStartY + showQuestionHeight, showQuestionGradientEndColor);
        if (backdrop.getHeight(this) > 0 && backdrop.getWidth(this) > 0) {
            if (getWidth() * backdrop.getHeight(this) > getHeight() * backdrop.getWidth(this)) {
                scaledBackdrop = backdrop.getScaledInstance(getWidth(), backdrop.getHeight(this) * getWidth() / backdrop.getWidth(this), Image.SCALE_FAST);
            } else {
                scaledBackdrop = backdrop.getScaledInstance(backdrop.getWidth(this) * getHeight() / backdrop.getHeight(this), getHeight(), Image.SCALE_FAST);
            }
        }
        if (logo.getHeight(this) > 0 && logo.getWidth(this) > 0) {
            double scale = (double) (getWidth() + getHeight()) / 2800;
            scaledLogo = logo.getScaledInstance((int) (scale * logo.getWidth(this)), (int) (scale * logo.getHeight(this)), Image.SCALE_SMOOTH);
        }
        repaint();
    }

    public void initializeBoard() {
        categoryFont = null;
        categoryWidth = (getWidth() - categoryPadding * (categories.length + 1)) / categories.length;
        categoryHeight = (int) (categoryWidth * categoryHeightRatio);
        maxNumberOfQuestions = 0;
        for (InGameCategory category : categories) {
            maxNumberOfQuestions = Math.max(maxNumberOfQuestions, category.questions.length);
        }
        if (maxNumberOfQuestions == 0) {
            return;
        }
        totalPaintTime = questionStartTime + questionTime * maxNumberOfQuestions * categories.length + questionGrowTime;
        totalQuestionHeight = getHeight() - (categoryHeight + 2 * categoryPadding);
        questionHeight = (totalQuestionHeight - (questionPadding * (maxNumberOfQuestions + 1))) / maxNumberOfQuestions;
        questionWidth = questionHeight;
        questionStartX = categoryHeight + 2 * categoryPadding + questionPadding;
        categoryGradient = new GradientPaint(0, categoryPadding, categoryGradientStartColor, 0, categoryPadding + categoryHeight, categoryGradientEndColor);
        categoryHighlightGradient = new GradientPaint(0, categoryPadding, categoryHighlightGradientStartColor, 0, categoryPadding + categoryHeight, categoryHighlightGradientEndColor);
        questionGradient = new GradientPaint[maxNumberOfQuestions];
        for (int i = 0; i < maxNumberOfQuestions; i++) {
            questionGradient[i] = new GradientPaint(0, questionStartX + i * (questionHeight + questionPadding), questionGradientStartColor, 0, questionStartX + i * (questionHeight + questionPadding) + questionHeight, questionGradientEndColor);
        }
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        double questionSizeFactor = Math.min(1, questionAnimationCounter / questionPaintTime);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (scaledBackdrop != null) {
            g.drawImage(scaledBackdrop, (getWidth() - scaledBackdrop.getWidth(this)) / 2, (getHeight() - scaledBackdrop.getHeight(this)) / 2, this);
        }
        if (scaledLogo != null && boardAnimationCounter == 0) {
            g.drawImage(scaledLogo, (getWidth() - scaledLogo.getWidth(this)) / 2, (getHeight() - scaledLogo.getHeight(this)) / 2, this);
        }
        if (categories == null) {
            return;
        }
        if (categoryFont == null) {
            int maxWidth = 0;
            g.setFont(new Font(textFont, Font.BOLD, 100));
            for (InGameCategory category : categories) {
                maxWidth = Math.max(maxWidth, g.getFontMetrics().stringWidth(category.name));
            }
            categoryFont = new Font(textFont, Font.BOLD, Math.min(80 * (categoryWidth - categoryPadding) / maxWidth, 80 * (categoryHeight - categoryPadding) / g.getFontMetrics().getHeight()));
        }
        g.setFont(categoryFont);
        int i = 0;
        for (InGameCategory category : categories) {
            boolean isCurrentCategory = category == categories[currentCategoryIndex];
            int x = categoryPadding + i * (categoryPadding + categoryWidth);
            int y = categoryPadding;
            double categoryAnimationCounter = boardAnimationCounter - categoryTime * i - categoryStartTime;
            if (categoryAnimationCounter > 0) {
                double sizeFactor = Math.min(1, categoryAnimationCounter / categoryGrowTime);
                g.setPaint(categoryGradient);
                if (isCurrentCategory) {
                    int sR = (int) (categoryGradientStartColor.getRed() * (1 - questionSizeFactor) + categoryHighlightGradientStartColor.getRed() * questionSizeFactor);
                    int sG = (int) (categoryGradientStartColor.getGreen() * (1 - questionSizeFactor) + categoryHighlightGradientStartColor.getGreen() * questionSizeFactor);
                    int sB = (int) (categoryGradientStartColor.getBlue() * (1 - questionSizeFactor) + categoryHighlightGradientStartColor.getBlue() * questionSizeFactor);
                    int eR = (int) (categoryGradientEndColor.getRed() * (1 - questionSizeFactor) + categoryHighlightGradientEndColor.getRed() * questionSizeFactor);
                    int eG = (int) (categoryGradientEndColor.getGreen() * (1 - questionSizeFactor) + categoryHighlightGradientEndColor.getGreen() * questionSizeFactor);
                    int eB = (int) (categoryGradientEndColor.getBlue() * (1 - questionSizeFactor) + categoryHighlightGradientEndColor.getBlue() * questionSizeFactor);
                    g.setPaint(new GradientPaint(0, categoryPadding, new Color(sR, sG, sB), 0, categoryPadding + categoryHeight, new Color(eR, eG, eB)));
                }
                g.fillRoundRect(x, y, (int) (categoryWidth * sizeFactor), (int) (categoryHeight * sizeFactor), categoryCornerRadius, categoryCornerRadius);
                g.setColor(Color.BLACK);
                g.drawRoundRect(x, y, (int) (categoryWidth * sizeFactor), (int) (categoryHeight * sizeFactor), categoryCornerRadius, categoryCornerRadius);
                if (sizeFactor >= 1) {
                    int fontWidth = g.getFontMetrics().stringWidth(category.name);
                    int fontHeight = g.getFontMetrics().getHeight();
                    int xOffset = (categoryWidth - fontWidth) / 2;
                    int yOffset = (categoryHeight - fontHeight) / 2;
                    g.setColor(Color.BLACK);
                    g.drawString(category.name, x + xOffset + 1, y + categoryHeight - yOffset + 1);
                    g.setColor(Color.WHITE);
                    g.drawString(category.name, x + xOffset, y + categoryHeight - yOffset);
                }
            }
            int j = 0;
            for (InGameQuestion question : category.questions) {
                double animationCounter = boardAnimationCounter - ((i * numberOfStepsBetweenQuestions + j) * questionTime + questionStartTime);
                if (animationCounter > 0 && !question.usedUp) {
                    int qx = x + (categoryWidth - questionWidth) / 2;
                    int qy = questionStartX + j * (questionPadding + questionHeight);
                    double sizeFactor = Math.min(1, animationCounter / questionGrowTime);
                    GradientPaint tmpPaint = questionGradient[j];
                    g.setPaint(tmpPaint);
                    g.fillOval(qx, qy, (int) (questionWidth * sizeFactor), (int) (questionHeight * sizeFactor));
                    g.setColor(Color.BLACK);
                    g.drawOval(qx, qy, (int) (questionWidth * sizeFactor), (int) (questionHeight * sizeFactor));
                    if (sizeFactor >= 1) {
                        int fontWidth = g.getFontMetrics().stringWidth("" + question.score);
                        int fontHeight = g.getFontMetrics().getHeight();
                        int xOffset = (questionWidth - fontWidth) / 2;
                        int yOffset = (int) (((questionHeight - .65 * fontHeight) / 2));
                        g.setColor(Color.BLACK);
                        g.drawString("" + question.score, qx + xOffset + 1, qy + questionHeight - yOffset + 1);
                        g.setColor(Color.WHITE);
                        g.drawString("" + question.score, qx + xOffset, qy + questionHeight - yOffset);
                    }
                }
                j++;
            }
            i++;
        }
        if (questionAnimationCounter > 0) {
            g.setPaint(showQuestionGradient);
            g.fillRoundRect(showQuestionStartX, showQuestionStartY, (int) (showQuestionWidth * questionSizeFactor), (int) (showQuestionHeight * questionSizeFactor), showQuestionCornerRadius, showQuestionCornerRadius);
            g.setColor(Color.BLACK);
            g.drawRoundRect(showQuestionStartX, showQuestionStartY, (int) (showQuestionWidth * questionSizeFactor), (int) (showQuestionHeight * questionSizeFactor), showQuestionCornerRadius, showQuestionCornerRadius);
            if (questionSizeFactor >= 1 && currentQuestion != null) {
                g.setFont(showQuestionFont);
                LinkedList<String> tmpList = fold(currentQuestion, (int) (currentQuestion.length() * (showQuestionSizeRatio * getWidth() - showQuestionPadding) / g.getFontMetrics().stringWidth(currentQuestion)));
                int xOffset = (int) (getWidth() * showQuestionSizeRatio - showQuestionPadding) / 2;
                int fontHeight = g.getFontMetrics().getHeight();
                for (int j = 0; j < tmpList.size(); j++) {
                    g.setColor(Color.BLACK);
                    g.drawString(tmpList.get(j), getWidth() / 2 - xOffset, getHeight() / 2 + j * fontHeight);
                    g.setColor(Color.WHITE);
                    g.drawString(tmpList.get(j), getWidth() / 2 - xOffset - 1, getHeight() / 2 + j * fontHeight - 1);
                }
            }
        }
    }

    /**
     * Splits a given string into a list of substrings preferrably as long as
     * possible, but never longer than the given length. The string is
     * preferrably split at whitespace characters, which are not included in the
     * substrings.
     *
     * @param string the string to split
     * @param length the maximum length of the substrings
     * @returns the list of substrings
     */
    private LinkedList<String> fold(String string, int length) {
        LinkedList<String> result = new LinkedList<String>();
        for (int i = 0; i < string.length(); ) {
            if (string.charAt(i) > ' ') {
                int start = i;
                int end;
                i += length;
                if (i < string.length()) {
                    while (i > start && string.charAt(i) > ' ') {
                        i--;
                    }
                    if (i <= start) {
                        i += length;
                    }
                } else {
                    i = string.length();
                }
                end = i - 1;
                while (string.charAt(end) <= ' ') {
                    end--;
                }
                result.add(string.substring(start, end + 1));
            } else {
                i++;
            }
        }
        return result;
    }
}

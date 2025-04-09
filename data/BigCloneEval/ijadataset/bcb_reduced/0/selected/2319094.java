package impl.game.grid;

import java.awt.*;

/**
 * Date: 17.04.2008
 * Time: 16:09:03
 *
 * @author Denis DIR Rozhnev
 */
public class Player extends Point {

    public static Color COLOR = Color.BLACK;

    public static Color COLOR_2 = Color.WHITE;

    /** ���� ����� ����������� Player-�� */
    public static Color COLOR_BG = Color.WHITE;

    public static int SIZE = 2;

    public static int DELTA_X = -4;

    public static int DELTA_Y = 4;

    private static int scoreChangeId = 0;

    private static int maxScore;

    private static int minScore;

    public Color color = COLOR;

    /** ���� ����� Player-� */
    public Color colorBg = COLOR_BG;

    protected String name;

    private int scoreId = 0;

    private int score;

    public Player(int x, int y, String name) {
        super(x, y);
        if (name == null) this.name = ""; else this.name = name;
    }

    public void draw(Graphics g) {
        final int S2 = (SIZE + 1) / 2;
        final int x0 = x - S2;
        final int y0 = y - S2;
        if (scoreId != scoreChangeId) {
            if (maxScore == minScore) {
                colorBg = COLOR_BG;
                color = COLOR;
            } else {
                int c = (int) ((long) (score - minScore) * 255 / (maxScore - minScore));
                if (c < 0) {
                    c = 0;
                } else if (c > 255) {
                    c = 255;
                }
                colorBg = new Color(c, c, c);
                color = c > 127 ? COLOR : COLOR_2;
            }
            scoreId = scoreChangeId;
        }
        g.setColor(colorBg);
        g.fillOval(x0, y0, SIZE, SIZE);
        g.setColor(color);
        g.drawOval(x0, y0, SIZE, SIZE);
        final int off = SIZE % 2;
        if (isInit()) {
            g.drawString(name.substring(0, 1), x + DELTA_X - off, y + DELTA_Y - off);
        }
    }

    public boolean contains(Point p) {
        return distance(p) <= SIZE;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInit() {
        return !this.name.isEmpty();
    }

    public void setScore(int score) {
        this.score = score;
        scoreId--;
    }

    public static void setScoreRange(int min, int max) {
        minScore = min;
        maxScore = max;
        scoreChangeId++;
    }
}

package backgammon_client;

import java.awt.*;
import backgammon.*;
import java.awt.geom.*;

/**
 *
 * @author Justin
 */
public class BPoints extends javax.swing.JPanel {

    public class InvalidPlayerSelectionException extends RuntimeException {
    }

    private final int left = 0;

    private final int top = 0;

    private final int boardHeight = 550;

    private final int boardWidth = 600;

    private final int bottom = top + boardHeight;

    private final int pointWidth = boardWidth / 13;

    private final int pointHeight = pointWidth * 5;

    private Color color1 = Color.BLUE;

    private Color color2 = Color.GRAY;

    private Color barColor = Color.PINK;

    private Color player1Color = Color.BLACK;

    private Color player2Color = Color.WHITE;

    public BPoints() {
    }

    @Override
    public void paint(Graphics g) {
        for (int i = 0; i < 13; i++) {
            if (i != 6) {
                int leftBase = pointWidth * i;
                int rightBase = pointWidth * (i + 1);
                int middle = (leftBase + rightBase) / 2;
                Polygon tTri = new Polygon();
                tTri.addPoint(leftBase, top);
                tTri.addPoint(rightBase, top);
                tTri.addPoint(middle, top + pointHeight);
                Polygon bTri = new Polygon();
                bTri.addPoint(leftBase, bottom);
                bTri.addPoint(rightBase, bottom);
                bTri.addPoint(middle, bottom - pointHeight);
                if (i % 2 == 0) {
                    g.setColor(color1);
                    g.fillPolygon(tTri);
                    g.drawPolygon(tTri);
                    g.setColor(color2);
                    g.fillPolygon(bTri);
                    g.drawPolygon(bTri);
                } else if (i % 2 == 1) {
                    g.setColor(color2);
                    g.fillPolygon(tTri);
                    g.drawPolygon(tTri);
                    g.setColor(color1);
                    g.fillPolygon(bTri);
                    g.drawPolygon(bTri);
                }
            } else {
                int rectX = (left + pointWidth * i);
                int rectY = (top);
                g.setColor(barColor);
                g.fillRect(rectX, rectY, pointWidth, boardHeight);
                g.drawRect(rectX, rectY, pointWidth, boardHeight);
            }
        }
    }

    public void paintCheckers(BoardState state, String playerstr) {
        Graphics g = this.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        int[][] curCheckers = state.getCheckers();
        int x = 0;
        int y = 0;
        int diam = pointWidth;
        if (playerstr.equals("player1")) {
            for (int i = 1; i < 26; i++) {
                for (int j = 0; j < 2; j++) {
                    int locCount = curCheckers[j][i];
                    if (locCount != 0) {
                        if (i < 7) {
                            x = boardWidth - (pointWidth * i);
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = num * diam;
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                        } else if (6 < i & i < 13) {
                            x = boardWidth - (pointWidth * (i + 1));
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = num * diam;
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                        } else if (12 < i & i < 19) {
                            x = boardWidth - (pointWidth * (26 - i));
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = diam * num;
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                            g2d.fillOval(x, y, diam, diam);
                        } else if (18 < i & i < 25) {
                            x = boardWidth - (pointWidth * (26 - i));
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = diam * num;
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                        } else if (i == 25) {
                            x = pointWidth * 7;
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = diam * num;
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                        }
                    }
                }
            }
        } else if (playerstr.equals("player2")) {
            for (int i = 1; i < 26; i++) {
                for (int j = 0; j < 2; j++) {
                    int locCount = curCheckers[j][i];
                    if (locCount != 0) {
                        if (i < 7) {
                            x = boardWidth - (pointWidth * i);
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = num * diam;
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                        } else if (6 < i & i < 13) {
                            x = boardWidth - (pointWidth * (i + 1));
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = num * diam;
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                        } else if (12 < i & i < 19) {
                            x = boardWidth - (pointWidth * (26 - i));
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = diam * num;
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                            g2d.fillOval(x, y, diam, diam);
                        } else if (18 < i & i < 25) {
                            x = boardWidth - (pointWidth * (26 - i));
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = diam * num;
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                        } else if (i == 25) {
                            x = pointWidth * 7;
                            for (int num = 0; num < locCount; num++) {
                                if (num < 6) {
                                    if (j == 0) {
                                        y = diam * num;
                                        g2d.setColor(player1Color);
                                    } else {
                                        y = bottom - ((num + 1) * diam);
                                        g2d.setColor(player2Color);
                                    }
                                    g2d.fillOval(x, y, diam, diam);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package game.rubkscube;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

/**
 * @author Josef Jelinek
 * @version 3.5b
 */
public final class AnimCube extends Applet implements Runnable, MouseListener, MouseMotionListener, KeyListener {

    private final Hashtable<String, String> config = new Hashtable<String, String>();

    private Color bgColor;

    private Color bgColor2;

    private Color hlColor;

    private Color textColor;

    private Color buttonBgColor;

    private final Color[] colors = new Color[24];

    private final int[][] cube = new int[6][9];

    private final int[][] initialCube = new int[6][9];

    private static final double[][] faceNormals = { { 0.0, -1.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, -1.0 }, { 0.0, 0.0, 1.0 }, { -1.0, 0.0, 0.0 }, { 1.0, 0.0, 0.0 } };

    private static final double[][] cornerCoords = { { -1.0, -1.0, -1.0 }, { 1.0, -1.0, -1.0 }, { 1.0, -1.0, 1.0 }, { -1.0, -1.0, 1.0 }, { -1.0, 1.0, -1.0 }, { 1.0, 1.0, -1.0 }, { 1.0, 1.0, 1.0 }, { -1.0, 1.0, 1.0 } };

    private static final int[][] faceCorners = { { 0, 1, 2, 3 }, { 4, 7, 6, 5 }, { 0, 4, 5, 1 }, { 2, 6, 7, 3 }, { 0, 3, 7, 4 }, { 1, 5, 6, 2 } };

    private static final int[][] oppositeCorners = { { 0, 3, 2, 1 }, { 0, 3, 2, 1 }, { 3, 2, 1, 0 }, { 3, 2, 1, 0 }, { 0, 3, 2, 1 }, { 0, 3, 2, 1 } };

    private static final int[][] adjacentFaces = { { 2, 5, 3, 4 }, { 4, 3, 5, 2 }, { 4, 1, 5, 0 }, { 5, 1, 4, 0 }, { 0, 3, 1, 2 }, { 2, 1, 3, 0 } };

    private int twistedLayer;

    private int twistedMode;

    private static final int[] faceTwistDirs = { 1, 1, -1, -1, -1, -1 };

    private final double[] eye = { 0.0, 0.0, -1.0 };

    private final double[] eyeX = { 1.0, 0.0, 0.0 };

    private final double[] eyeY = new double[3];

    private final double[] initialEye = new double[3];

    private final double[] initialEyeX = new double[3];

    private final double[] initialEyeY = new double[3];

    private double currentAngle;

    private double originalAngle;

    private int speed;

    private int doubleSpeed;

    private boolean natural = true;

    private boolean toTwist;

    private boolean interrupted;

    private boolean restarted;

    private boolean mirrored;

    private boolean editable;

    private boolean twisting;

    private boolean spinning;

    private boolean animating;

    private boolean dragging;

    private boolean demo;

    private int persp;

    private double scale;

    private int align;

    private boolean hint;

    private double faceShift;

    private int[][] move;

    private int[][] demoMove;

    private int curMove;

    private int movePos;

    private int moveDir;

    private boolean moveOne;

    private boolean moveAnimated;

    private int metric;

    private String[] infoText;

    private int curInfoText;

    private int buttonBar;

    private int buttonHeight;

    private boolean drawButtons = true;

    private boolean pushed;

    private int buttonPressed = -1;

    private int progressHeight = 6;

    private int textHeight;

    private int moveText;

    private boolean outlined = true;

    private static final int[] posFaceTransform = { 3, 2, 0, 5, 1, 4 };

    private static final int[][] posFaceletTransform = { { 6, 3, 0, 7, 4, 1, 8, 5, 2 }, { 2, 5, 8, 1, 4, 7, 0, 3, 6 }, { 0, 1, 2, 3, 4, 5, 6, 7, 8 }, { 0, 1, 2, 3, 4, 5, 6, 7, 8 }, { 6, 3, 0, 7, 4, 1, 8, 5, 2 }, { 0, 1, 2, 3, 4, 5, 6, 7, 8 } };

    private final int[] hex = new int[6];

    public void init() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        colors[0] = new Color(255, 128, 64);
        colors[1] = new Color(255, 0, 0);
        colors[2] = new Color(0, 255, 0);
        colors[3] = new Color(0, 0, 255);
        colors[4] = new Color(153, 153, 153);
        colors[5] = new Color(170, 170, 68);
        colors[6] = new Color(187, 119, 68);
        colors[7] = new Color(153, 68, 68);
        colors[8] = new Color(68, 119, 68);
        colors[9] = new Color(0, 68, 119);
        colors[10] = new Color(255, 255, 255);
        colors[11] = new Color(255, 255, 0);
        colors[12] = new Color(255, 96, 32);
        colors[13] = new Color(208, 0, 0);
        colors[14] = new Color(0, 144, 0);
        colors[15] = new Color(32, 64, 208);
        colors[16] = new Color(176, 176, 176);
        colors[17] = new Color(80, 80, 80);
        colors[18] = new Color(255, 0, 255);
        colors[19] = new Color(0, 255, 255);
        colors[20] = new Color(255, 160, 192);
        colors[21] = new Color(32, 255, 16);
        colors[22] = new Color(0, 0, 0);
        colors[23] = new Color(128, 128, 128);
        animThread = new Thread(this, "Cube Animator");
        animThread.start();
        String param = getParameter("config");
        if (param != null) {
            try {
                URL url = new URL(getDocumentBase(), param);
                InputStream input = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line = reader.readLine();
                while (line != null) {
                    int pos = line.indexOf('=');
                    if (pos > 0) {
                        String key = line.substring(0, pos).trim();
                        String value = line.substring(pos + 1).trim();
                        config.put(key, value);
                    }
                    line = reader.readLine();
                }
                reader.close();
            } catch (MalformedURLException ex) {
                System.err.println("Malformed URL: " + param + ": " + ex);
            } catch (IOException ex) {
                System.err.println("Input error: " + param + ": " + ex);
            }
        }
        param = getParameter("bgcolor");
        if (param != null && param.length() == 6) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 16; j++) {
                    if (Character.toLowerCase(param.charAt(i)) == "0123456789abcdef".charAt(j)) {
                        hex[i] = j;
                        break;
                    }
                }
            }
            bgColor = new Color(hex[0] * 16 + hex[1], hex[2] * 16 + hex[3], hex[4] * 16 + hex[5]);
        } else bgColor = Color.gray;
        param = getParameter("butbgcolor");
        if (param != null && param.length() == 6) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 16; j++) {
                    if (Character.toLowerCase(param.charAt(i)) == "0123456789abcdef".charAt(j)) {
                        hex[i] = j;
                        break;
                    }
                }
            }
            buttonBgColor = new Color(hex[0] * 16 + hex[1], hex[2] * 16 + hex[3], hex[4] * 16 + hex[5]);
        } else buttonBgColor = bgColor;
        param = getParameter("colors");
        if (param != null) {
            for (int k = 0; k < 10 && k < param.length() / 6; k++) {
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 16; j++) {
                        if (Character.toLowerCase(param.charAt(k * 6 + i)) == "0123456789abcdef".charAt(j)) {
                            hex[i] = j;
                            break;
                        }
                    }
                }
                colors[k] = new Color(hex[0] * 16 + hex[1], hex[2] * 16 + hex[3], hex[4] * 16 + hex[5]);
            }
        }
        for (int i = 0; i < 6; i++) for (int j = 0; j < 9; j++) cube[i][j] = i + 10;
        String initialPosition = "lluu";
        param = getParameter("colorscheme");
        if (param != null && param.length() == 6) {
            for (int i = 0; i < 6; i++) {
                int color = 23;
                for (int j = 0; j < 23; j++) {
                    if (Character.toLowerCase(param.charAt(i)) == "0123456789wyorgbldmcpnk".charAt(j)) {
                        color = j;
                        break;
                    }
                }
                for (int j = 0; j < 9; j++) cube[i][j] = color;
            }
        }
        param = getParameter("pos");
        if (param != null && param.length() == 54) {
            initialPosition = "uuuuff";
            if (bgColor == Color.gray) bgColor = Color.white;
            for (int i = 0; i < 6; i++) {
                int ti = posFaceTransform[i];
                for (int j = 0; j < 9; j++) {
                    int tj = posFaceletTransform[i][j];
                    cube[ti][tj] = 23;
                    for (int k = 0; k < 14; k++) {
                        if (param.charAt(i * 9 + j) == "DFECABdfecabgh".charAt(k)) {
                            cube[ti][tj] = k + 4;
                            break;
                        }
                    }
                }
            }
        }
        param = getParameter("facelets");
        if (param != null && param.length() == 54) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 9; j++) {
                    cube[i][j] = 23;
                    for (int k = 0; k < 23; k++) {
                        if (Character.toLowerCase(param.charAt(i * 9 + j)) == "0123456789wyorgbldmcpnk".charAt(k)) {
                            cube[i][j] = k;
                            break;
                        }
                    }
                }
            }
        }
        param = getParameter("move");
        move = (param == null ? new int[0][0] : getMove(param, true));
        movePos = 0;
        curInfoText = -1;
        param = getParameter("initmove");
        if (param != null) {
            int[][] initialMove = param.equals("#") ? move : getMove(param, false);
            if (initialMove.length > 0) doMove(cube, initialMove[0], 0, initialMove[0].length, false);
        }
        param = getParameter("initrevmove");
        if (param != null) {
            int[][] initialReversedMove = param.equals("#") ? move : getMove(param, false);
            if (initialReversedMove.length > 0) doMove(cube, initialReversedMove[0], 0, initialReversedMove[0].length, true);
        }
        param = getParameter("demo");
        if (param != null) {
            demoMove = param.equals("#") ? move : getMove(param, true);
            if (demoMove.length > 0 && demoMove[0].length > 0) demo = true;
        }
        param = getParameter("position");
        vNorm(vMul(eyeY, eye, eyeX));
        if (param == null) param = initialPosition;
        double pi12 = Math.PI / 12;
        for (int i = 0; i < param.length(); i++) {
            double angle = pi12;
            switch(Character.toLowerCase(param.charAt(i))) {
                case 'd':
                    angle = -angle;
                case 'u':
                    vRotY(eye, angle);
                    vRotY(eyeX, angle);
                    break;
                case 'f':
                    angle = -angle;
                case 'b':
                    vRotZ(eye, angle);
                    vRotZ(eyeX, angle);
                    break;
                case 'l':
                    angle = -angle;
                case 'r':
                    vRotX(eye, angle);
                    vRotX(eyeX, angle);
                    break;
            }
        }
        vNorm(vMul(eyeY, eye, eyeX));
        speed = 0;
        doubleSpeed = 0;
        param = getParameter("speed");
        if (param != null) for (int i = 0; i < param.length(); i++) if (param.charAt(i) >= '0' && param.charAt(i) <= '9') speed = speed * 10 + (int) param.charAt(i) - '0';
        param = getParameter("doublespeed");
        if (param != null) for (int i = 0; i < param.length(); i++) if (param.charAt(i) >= '0' && param.charAt(i) <= '9') doubleSpeed = doubleSpeed * 10 + (int) param.charAt(i) - '0';
        if (speed == 0) speed = 10;
        if (doubleSpeed == 0) doubleSpeed = speed * 3 / 2;
        persp = 0;
        param = getParameter("perspective");
        if (param == null) persp = 2; else for (int i = 0; i < param.length(); i++) if (param.charAt(i) >= '0' && param.charAt(i) <= '9') persp = persp * 10 + (int) param.charAt(i) - '0';
        int intscale = 0;
        param = getParameter("scale");
        if (param != null) for (int i = 0; i < param.length(); i++) if (param.charAt(i) >= '0' && param.charAt(i) <= '9') intscale = intscale * 10 + (int) param.charAt(i) - '0';
        scale = 1.0 / (1.0 + intscale / 10.0);
        hint = false;
        param = getParameter("hint");
        if (param != null) {
            hint = true;
            faceShift = 0.0;
            for (int i = 0; i < param.length(); i++) if (param.charAt(i) >= '0' && param.charAt(i) <= '9') faceShift = faceShift * 10 + (int) param.charAt(i) - '0';
            if (faceShift < 1.0) hint = false; else faceShift /= 10.0;
        }
        buttonBar = 1;
        buttonHeight = 13;
        progressHeight = move.length == 0 ? 0 : 6;
        param = getParameter("buttonbar");
        if ("0".equals(param)) {
            buttonBar = 0;
            buttonHeight = 0;
            progressHeight = 0;
        } else if ("1".equals(param)) buttonBar = 1; else if ("2".equals(param) || move.length == 0) {
            buttonBar = 2;
            progressHeight = 0;
        }
        param = getParameter("edit");
        if ("0".equals(param)) editable = false; else editable = true;
        param = getParameter("movetext");
        if ("1".equals(param)) moveText = 1; else if ("2".equals(param)) moveText = 2; else if ("3".equals(param)) moveText = 3; else if ("4".equals(param)) moveText = 4; else moveText = 0;
        param = getParameter("fonttype");
        if (param == null || "1".equals(param)) outlined = true; else outlined = false;
        metric = 0;
        param = getParameter("metric");
        if (param != null) {
            if ("1".equals(param)) metric = 1; else if ("2".equals(param)) metric = 2; else if ("3".equals(param)) metric = 3;
        }
        align = 1;
        param = getParameter("align");
        if (param != null) {
            if ("0".equals(param)) align = 0; else if ("1".equals(param)) align = 1; else if ("2".equals(param)) align = 2;
        }
        for (int i = 0; i < 6; i++) for (int j = 0; j < 9; j++) initialCube[i][j] = cube[i][j];
        for (int i = 0; i < 3; i++) {
            initialEye[i] = eye[i];
            initialEyeX[i] = eyeX[i];
            initialEyeY[i] = eyeY[i];
        }
        int red = bgColor.getRed();
        int green = bgColor.getGreen();
        int blue = bgColor.getBlue();
        int average = (red * 299 + green * 587 + blue * 114) / 1000;
        if (average < 128) {
            textColor = Color.white;
            hlColor = bgColor.brighter();
            hlColor = new Color(hlColor.getBlue(), hlColor.getRed(), hlColor.getGreen());
        } else {
            textColor = Color.black;
            hlColor = bgColor.darker();
            hlColor = new Color(hlColor.getBlue(), hlColor.getRed(), hlColor.getGreen());
        }
        bgColor2 = new Color(red / 2, green / 2, blue / 2);
        curInfoText = -1;
        if (demo) startAnimation(-1);
    }

    public String getParameter(String name) {
        String parameter = super.getParameter(name);
        if (parameter == null) return (String) config.get(name);
        return parameter;
    }

    private static final int[] moveModes = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2 };

    private static final int[] moveCodes = { 0, 1, 2, 3, 4, 5, 1, 2, 4, 5, 2, 0, 5, 2, 0, 0, 1, 2, 3, 4, 5 };

    private int[][] getMove(String sequence, boolean info) {
        if (info) {
            int inum = 0;
            int pos = sequence.indexOf('{');
            while (pos != -1) {
                inum++;
                pos = sequence.indexOf('{', pos + 1);
            }
            if (infoText == null) {
                curInfoText = 0;
                infoText = new String[inum];
            } else {
                String[] infoText2 = new String[infoText.length + inum];
                for (int i = 0; i < infoText.length; i++) infoText2[i] = infoText[i];
                curInfoText = infoText.length;
                infoText = infoText2;
            }
        }
        int num = 1;
        int pos = sequence.indexOf(';');
        while (pos != -1) {
            num++;
            pos = sequence.indexOf(';', pos + 1);
        }
        int[][] move = new int[num][];
        int lastPos = 0;
        pos = sequence.indexOf(';');
        num = 0;
        while (pos != -1) {
            move[num++] = getMovePart(sequence.substring(lastPos, pos), info);
            lastPos = pos + 1;
            pos = sequence.indexOf(';', lastPos);
        }
        move[num] = getMovePart(sequence.substring(lastPos), info);
        return move;
    }

    private static final char[] modeChar = { 'm', 't', 'c', 's', 'a' };

    private int[] getMovePart(String sequence, boolean info) {
        int length = 0;
        int[] move = new int[sequence.length()];
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '.') {
                move[length] = -1;
                length++;
            } else if (sequence.charAt(i) == '{') {
                i++;
                String s = "";
                while (i < sequence.length()) {
                    if (sequence.charAt(i) == '}') break;
                    if (info) s += sequence.charAt(i);
                    i++;
                }
                if (info) {
                    infoText[curInfoText] = s;
                    move[length] = 1000 + curInfoText;
                    curInfoText++;
                    length++;
                }
            } else {
                for (int j = 0; j < 21; j++) {
                    if (sequence.charAt(i) == "UDFBLRESMXYZxyzudfblr".charAt(j)) {
                        i++;
                        int mode = moveModes[j];
                        move[length] = moveCodes[j] * 24;
                        if (i < sequence.length()) {
                            if (moveModes[j] == 0) {
                                for (int k = 0; k < modeChar.length; k++) {
                                    if (sequence.charAt(i) == modeChar[k]) {
                                        mode = k + 1;
                                        i++;
                                        break;
                                    }
                                }
                            }
                        }
                        move[length] += mode * 4;
                        if (i < sequence.length()) {
                            if (sequence.charAt(i) == '1') i++; else if (sequence.charAt(i) == '\'' || sequence.charAt(i) == '3') {
                                move[length] += 2;
                                i++;
                            } else if (sequence.charAt(i) == '2') {
                                i++;
                                if (i < sequence.length() && sequence.charAt(i) == '\'') {
                                    move[length] += 3;
                                    i++;
                                } else move[length] += 1;
                            }
                        }
                        length++;
                        i--;
                        break;
                    }
                }
            }
        }
        int[] returnMove = new int[length];
        for (int i = 0; i < length; i++) returnMove[i] = move[i];
        return returnMove;
    }

    private String moveText(int[] move, int start, int end) {
        if (start >= move.length) return "";
        String s = "";
        for (int i = start; i < end; i++) s += turnText(move, i);
        return s;
    }

    private static final String[][][] turnSymbol = { { { "U", "D", "F", "B", "L", "R" }, { "Um", "Dm", "Fm", "Bm", "Lm", "Rm" }, { "Ut", "Dt", "Ft", "Bt", "Lt", "Rt" }, { "Uc", "Dc", "Fc", "Bc", "Lc", "Rc" }, { "Us", "Ds", "Fs", "Bs", "Ls", "Rs" }, { "Ua", "Da", "Fa", "Ba", "La", "Ra" } }, { { "U", "D", "F", "B", "L", "R" }, { "~E", "E", "S", "~S", "M", "~M" }, { "u", "d", "f", "b", "l", "r" }, { "Z", "~Z", "Y", "~Y", "~X", "X" }, { "Us", "Ds", "Fs", "Bs", "Ls", "Rs" }, { "Ua", "Da", "Fa", "Ba", "La", "Ra" } }, { { "U", "D", "F", "B", "L", "R" }, { "~E", "E", "S", "~S", "M", "~M" }, { "u", "d", "f", "b", "l", "r" }, { "Y", "~Y", "Z", "~Z", "~X", "X" }, { "Us", "Ds", "Fs", "Bs", "Ls", "Rs" }, { "Ua", "Da", "Fa", "Ba", "La", "Ra" } }, { { "U", "D", "F", "B", "L", "R" }, { "u", "d", "f", "b", "l", "r" }, { "Uu", "Dd", "Ff", "Bb", "Ll", "Rr" }, { "QU", "QD", "QF", "QB", "QL", "QR" }, { "UD'", "DU'", "FB'", "BF'", "LR'", "RL'" }, { "UD", "DU", "FB", "BF", "LR", "RL" } } };

    private static final String[] modifierStrings = { "", "2", "'", "2'" };

    private String turnText(int[] move, int pos) {
        if (pos >= move.length) return "";
        if (move[pos] >= 1000) return "";
        if (move[pos] == -1) return ".";
        String s = turnSymbol[moveText - 1][move[pos] / 4 % 6][move[pos] / 24];
        if (s.charAt(0) == '~') return s.substring(1) + modifierStrings[(move[pos] + 2) % 4];
        return s + modifierStrings[move[pos] % 4];
    }

    private static final String[] metricChar = { "", "q", "f", "s" };

    private static int realMoveLength(int[] move) {
        int length = 0;
        for (int i = 0; i < move.length; i++) if (move[i] < 1000) length++;
        return length;
    }

    private static int realMovePos(int[] move, int pos) {
        int rpos = 0;
        for (int i = 0; i < pos; i++) if (move[i] < 1000) rpos++;
        return rpos;
    }

    private static int arrayMovePos(int[] move, int realPos) {
        int pos = 0;
        int rpos = 0;
        while (true) {
            while (pos < move.length && move[pos] >= 1000) pos++;
            if (rpos == realPos) break;
            if (pos < move.length) {
                rpos++;
                pos++;
            }
        }
        return pos;
    }

    private int moveLength(int[] move, int end) {
        int length = 0;
        for (int i = 0; i < move.length && (i < end || end < 0); i++) length += turnLength(move[i]);
        return length;
    }

    private int turnLength(int turn) {
        if (turn < 0 || turn >= 1000) return 0;
        int modifier = turn % 4;
        int mode = turn / 4 % 6;
        int n = 1;
        switch(metric) {
            case 1:
                if (modifier == 1 || modifier == 3) n *= 2;
            case 2:
                if (mode == 1 || mode == 4 || mode == 5) n *= 2;
            case 3:
                if (mode == 3) n = 0;
        }
        return n;
    }

    private void initInfoText(int[] move) {
        if (move.length > 0 && move[0] >= 1000) curInfoText = move[0] - 1000; else curInfoText = -1;
    }

    private void doMove(int[][] cube, int[] move, int start, int length, boolean reversed) {
        int position = reversed ? start + length : start;
        while (true) {
            if (reversed) {
                if (position <= start) break;
                position--;
            }
            if (move[position] >= 1000) {
                curInfoText = reversed ? -1 : move[position] - 1000;
            } else if (move[position] >= 0) {
                int modifier = move[position] % 4 + 1;
                int mode = move[position] / 4 % 6;
                if (modifier == 4) modifier = 2;
                if (reversed) modifier = 4 - modifier;
                twistLayers(cube, move[position] / 24, modifier, mode);
            }
            if (!reversed) {
                position++;
                if (position >= start + length) break;
            }
        }
    }

    private Thread animThread = null;

    private void startAnimation(int mode) {
        synchronized (animThread) {
            stopAnimation();
            if (!demo && (move.length == 0 || move[curMove].length == 0)) return;
            if (demo && (demoMove.length == 0 || demoMove[0].length == 0)) return;
            moveDir = 1;
            moveOne = false;
            moveAnimated = true;
            switch(mode) {
                case 0:
                    break;
                case 1:
                    moveDir = -1;
                    break;
                case 2:
                    moveOne = true;
                    break;
                case 3:
                    moveDir = -1;
                    moveOne = true;
                    break;
                case 4:
                    moveAnimated = false;
                    break;
            }
            animThread.notify();
        }
    }

    public void stopAnimation() {
        synchronized (animThread) {
            restarted = true;
            animThread.notify();
            try {
                animThread.wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
            restarted = false;
        }
    }

    public void run() {
        synchronized (animThread) {
            interrupted = false;
            do {
                if (restarted) {
                    animThread.notify();
                }
                try {
                    animThread.wait();
                } catch (InterruptedException e) {
                    break;
                }
                if (restarted) continue;
                boolean restart = false;
                animating = true;
                drawButtons = true;
                int[] mv = demo ? demoMove[0] : move[curMove];
                if (moveDir > 0) {
                    if (movePos >= mv.length) {
                        movePos = 0;
                        initInfoText(mv);
                    }
                } else {
                    curInfoText = -1;
                    if (movePos == 0) movePos = mv.length;
                }
                while (true) {
                    if (moveDir < 0) {
                        if (movePos == 0) break;
                        movePos--;
                    }
                    if (mv[movePos] == -1) {
                        repaint();
                        if (!moveOne) sleep(33 * speed);
                    } else if (mv[movePos] >= 1000) {
                        curInfoText = moveDir > 0 ? mv[movePos] - 1000 : -1;
                    } else {
                        int num = mv[movePos] % 4 + 1;
                        int mode = mv[movePos] / 4 % 6;
                        boolean clockwise = num < 3;
                        if (num == 4) num = 2;
                        if (moveDir < 0) {
                            clockwise = !clockwise;
                            num = 4 - num;
                        }
                        spin(mv[movePos] / 24, num, mode, clockwise, moveAnimated);
                        if (moveOne) restart = true;
                    }
                    if (moveDir > 0) {
                        movePos++;
                        if (movePos < mv.length && mv[movePos] >= 1000) {
                            curInfoText = mv[movePos] - 1000;
                            movePos++;
                        }
                        if (movePos == mv.length) {
                            if (!demo) break;
                            movePos = 0;
                            initInfoText(mv);
                            for (int i = 0; i < 6; i++) for (int j = 0; j < 9; j++) cube[i][j] = initialCube[i][j];
                        }
                    } else curInfoText = -1;
                    if (interrupted || restarted || restart) break;
                }
                animating = false;
                drawButtons = true;
                repaint();
                if (demo) {
                    clear();
                    demo = false;
                }
            } while (!interrupted);
        }
    }

    private void sleep(int time) {
        synchronized (animThread) {
            try {
                animThread.wait(time);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
    }

    private void clear() {
        synchronized (animThread) {
            movePos = 0;
            if (move.length > 0) initInfoText(move[curMove]);
            natural = true;
            mirrored = false;
            for (int i = 0; i < 6; i++) for (int j = 0; j < 9; j++) cube[i][j] = initialCube[i][j];
            for (int i = 0; i < 3; i++) {
                eye[i] = initialEye[i];
                eyeX[i] = initialEyeX[i];
                eyeY[i] = initialEyeY[i];
            }
        }
    }

    private void spin(int layer, int num, int mode, boolean clockwise, boolean animated) {
        twisting = false;
        natural = true;
        spinning = true;
        originalAngle = 0;
        if (faceTwistDirs[layer] > 0) clockwise = !clockwise;
        if (animated) {
            double phit = Math.PI / 2;
            double phis = clockwise ? 1.0 : -1.0;
            int turnTime = 67 * speed;
            if (num == 2) {
                phit = Math.PI;
                turnTime = 67 * doubleSpeed;
            }
            twisting = true;
            twistedLayer = layer;
            twistedMode = mode;
            splitCube(layer);
            long sTime = System.currentTimeMillis();
            long lTime = sTime;
            double d = phis * phit / turnTime;
            for (currentAngle = 0; currentAngle * phis < phit; currentAngle = d * (lTime - sTime)) {
                repaint();
                sleep(25);
                if (interrupted || restarted) break;
                lTime = System.currentTimeMillis();
            }
        }
        currentAngle = 0;
        twisting = false;
        natural = true;
        twistLayers(cube, layer, num, mode);
        spinning = false;
        if (animated) repaint();
    }

    private static final int[][][] cubeBlocks = { { { 0, 3 }, { 0, 3 } }, { { 0, 3 }, { 0, 3 } }, { { 0, 3 }, { 0, 3 } }, { { 0, 3 }, { 0, 3 } }, { { 0, 3 }, { 0, 3 } }, { { 0, 3 }, { 0, 3 } } };

    private final int[][][] topBlocks = new int[6][][];

    private final int[][][] midBlocks = new int[6][][];

    private final int[][][] botBlocks = new int[6][][];

    private static final int[][][] topBlockTable = { { { 0, 0 }, { 0, 0 } }, { { 0, 3 }, { 0, 3 } }, { { 0, 3 }, { 0, 1 } }, { { 0, 1 }, { 0, 3 } }, { { 0, 3 }, { 2, 3 } }, { { 2, 3 }, { 0, 3 } } };

    private static final int[][][] midBlockTable = { { { 0, 0 }, { 0, 0 } }, { { 0, 3 }, { 1, 2 } }, { { 1, 2 }, { 0, 3 } } };

    private static final int[][] topBlockFaceDim = { { 1, 0, 3, 3, 2, 3 }, { 0, 1, 5, 5, 4, 5 }, { 2, 3, 1, 0, 3, 2 }, { 4, 5, 0, 1, 5, 4 }, { 3, 2, 2, 4, 1, 0 }, { 5, 4, 4, 2, 0, 1 } };

    private static final int[][] midBlockFaceDim = { { 0, 0, 2, 2, 1, 2 }, { 0, 0, 2, 2, 1, 2 }, { 1, 2, 0, 0, 2, 1 }, { 1, 2, 0, 0, 2, 1 }, { 2, 1, 1, 1, 0, 0 }, { 2, 1, 1, 1, 0, 0 } };

    private static final int[][] botBlockFaceDim = { { 0, 1, 5, 5, 4, 5 }, { 1, 0, 3, 3, 2, 3 }, { 4, 5, 0, 1, 5, 4 }, { 2, 3, 1, 0, 3, 2 }, { 5, 4, 4, 2, 0, 1 }, { 3, 2, 2, 4, 1, 0 } };

    private void splitCube(int layer) {
        for (int i = 0; i < 6; i++) {
            topBlocks[i] = topBlockTable[topBlockFaceDim[layer][i]];
            botBlocks[i] = topBlockTable[botBlockFaceDim[layer][i]];
            midBlocks[i] = midBlockTable[midBlockFaceDim[layer][i]];
        }
        natural = false;
    }

    private void twistLayers(int[][] cube, int layer, int num, int mode) {
        switch(mode) {
            case 3:
                twistLayer(cube, layer ^ 1, num, false);
            case 2:
                twistLayer(cube, layer, 4 - num, false);
            case 1:
                twistLayer(cube, layer, 4 - num, true);
                break;
            case 5:
                twistLayer(cube, layer ^ 1, 4 - num, false);
                twistLayer(cube, layer, 4 - num, false);
                break;
            case 4:
                twistLayer(cube, layer ^ 1, num, false);
            default:
                twistLayer(cube, layer, 4 - num, false);
        }
    }

    private static final int[] cycleOrder = { 0, 1, 2, 5, 8, 7, 6, 3 };

    private static final int[] cycleFactors = { 1, 3, -1, -3, 1, 3, -1, -3 };

    private static final int[] cycleOffsets = { 0, 2, 8, 6, 3, 1, 5, 7 };

    private static final int[][] cycleLayerSides = { { 3, 3, 3, 0 }, { 2, 1, 1, 1 }, { 3, 3, 0, 0 }, { 2, 1, 1, 2 }, { 3, 2, 0, 0 }, { 2, 2, 0, 1 } };

    private static final int[][] cycleCenters = { { 7, 7, 7, 4 }, { 6, 5, 5, 5 }, { 7, 7, 4, 4 }, { 6, 5, 5, 6 }, { 7, 6, 4, 4 }, { 6, 6, 4, 5 } };

    private final int[] twistBuffer = new int[12];

    private void twistLayer(int[][] cube, int layer, int num, boolean middle) {
        if (!middle) {
            for (int i = 0; i < 8; i++) twistBuffer[(i + num * 2) % 8] = cube[layer][cycleOrder[i]];
            for (int i = 0; i < 8; i++) cube[layer][cycleOrder[i]] = twistBuffer[i];
        }
        int k = num * 3;
        for (int i = 0; i < 4; i++) {
            int n = adjacentFaces[layer][i];
            int c = middle ? cycleCenters[layer][i] : cycleLayerSides[layer][i];
            int factor = cycleFactors[c];
            int offset = cycleOffsets[c];
            for (int j = 0; j < 3; j++) {
                twistBuffer[k % 12] = cube[n][j * factor + offset];
                k++;
            }
        }
        k = 0;
        for (int i = 0; i < 4; i++) {
            int n = adjacentFaces[layer][i];
            int c = middle ? cycleCenters[layer][i] : cycleLayerSides[layer][i];
            int factor = cycleFactors[c];
            int offset = cycleOffsets[c];
            int j = 0;
            while (j < 3) {
                cube[n][j * factor + offset] = twistBuffer[k];
                j++;
                k++;
            }
        }
    }

    private Graphics graphics = null;

    private Image image = null;

    private int width;

    private int height;

    private int lastX;

    private int lastY;

    private int lastDragX;

    private int lastDragY;

    private int dragAreas;

    private final int[][] dragCornersX = new int[18][4];

    private final int[][] dragCornersY = new int[18][4];

    private final double[] dragDirsX = new double[18];

    private final double[] dragDirsY = new double[18];

    private static final int[][][] dragBlocks = { { { 0, 0 }, { 3, 0 }, { 3, 1 }, { 0, 1 } }, { { 3, 0 }, { 3, 3 }, { 2, 3 }, { 2, 0 } }, { { 3, 3 }, { 0, 3 }, { 0, 2 }, { 3, 2 } }, { { 0, 3 }, { 0, 0 }, { 1, 0 }, { 1, 3 } }, { { 0, 1 }, { 3, 1 }, { 3, 2 }, { 0, 2 } }, { { 2, 0 }, { 2, 3 }, { 1, 3 }, { 1, 0 } } };

    private static final int[][] areaDirs = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 }, { 1, 0 }, { 0, 1 } };

    private static final int[][] twistDirs = { { 1, 1, 1, 1, 1, -1 }, { 1, 1, 1, 1, 1, -1 }, { 1, -1, 1, -1, 1, 1 }, { 1, -1, 1, -1, -1, 1 }, { -1, 1, -1, 1, -1, -1 }, { 1, -1, 1, -1, 1, 1 } };

    private int[] dragLayers = new int[18];

    private int[] dragModes = new int[18];

    private double dragX;

    private double dragY;

    private static final int[][][] rotCos = { { { 1, 0, 0 }, { 0, 0, 0 }, { 0, 0, 1 } }, { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } }, { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } } };

    private static final int[][][] rotSin = { { { 0, 0, 1 }, { 0, 0, 0 }, { -1, 0, 0 } }, { { 0, 1, 0 }, { -1, 0, 0 }, { 0, 0, 0 } }, { { 0, 0, 0 }, { 0, 0, 1 }, { 0, -1, 0 } } };

    private static final int[][][] rotVec = { { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } }, { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 1 } }, { { 1, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } } };

    private static final int[] rotSign = { 1, -1, 1, -1, 1, -1 };

    private final double[] tempEye = new double[3];

    private final double[] tempEyeX = new double[3];

    private final double[] tempEyeY = new double[3];

    private final double[] tempEye2 = new double[3];

    private final double[] tempEyeX2 = new double[3];

    private final double[] tempEyeY2 = new double[3];

    private final double[] perspEye = new double[3];

    private final double[] perspEyeI = new double[3];

    private final double[] perspNormal = new double[3];

    private final double[][] eyeArray = new double[3][];

    private final double[][] eyeArrayX = new double[3][];

    private final double[][] eyeArrayY = new double[3][];

    private final int[][] eyeOrder = { { 1, 0, 0 }, { 0, 1, 0 }, { 1, 1, 0 }, { 1, 1, 1 }, { 1, 0, 1 }, { 1, 0, 2 } };

    private final int[][][][] blockArray = new int[3][][][];

    private final int[][] blockMode = { { 0, 2, 2 }, { 2, 1, 2 }, { 2, 2, 2 }, { 2, 2, 2 }, { 2, 2, 2 }, { 2, 2, 2 } };

    private final int[][] drawOrder = { { 0, 1, 2 }, { 2, 1, 0 }, { 0, 2, 1 } };

    public void paint(Graphics g) {
        Dimension size = getSize();
        if (image == null || size.width != width || size.height - buttonHeight != height) {
            width = size.width;
            height = size.height;
            image = createImage(width, height);
            graphics = image.getGraphics();
            textHeight = graphics.getFontMetrics().getHeight() - graphics.getFontMetrics().getLeading();
            if (buttonBar == 1) height -= buttonHeight;
            drawButtons = true;
        }
        graphics.setColor(bgColor);
        graphics.setClip(0, 0, width, height);
        graphics.fillRect(0, 0, width, height);
        synchronized (animThread) {
            dragAreas = 0;
            if (natural) fixBlock(eye, eyeX, eyeY, cubeBlocks, 3); else {
                double cosA = Math.cos(originalAngle + currentAngle);
                double sinA = Math.sin(originalAngle + currentAngle) * rotSign[twistedLayer];
                for (int i = 0; i < 3; i++) {
                    tempEye[i] = 0;
                    tempEyeX[i] = 0;
                    for (int j = 0; j < 3; j++) {
                        int axis = twistedLayer / 2;
                        tempEye[i] += eye[j] * (rotVec[axis][i][j] + rotCos[axis][i][j] * cosA + rotSin[axis][i][j] * sinA);
                        tempEyeX[i] += eyeX[j] * (rotVec[axis][i][j] + rotCos[axis][i][j] * cosA + rotSin[axis][i][j] * sinA);
                    }
                }
                vMul(tempEyeY, tempEye, tempEyeX);
                double cosB = Math.cos(originalAngle - currentAngle);
                double sinB = Math.sin(originalAngle - currentAngle) * rotSign[twistedLayer];
                for (int i = 0; i < 3; i++) {
                    tempEye2[i] = 0;
                    tempEyeX2[i] = 0;
                    for (int j = 0; j < 3; j++) {
                        int axis = twistedLayer / 2;
                        tempEye2[i] += eye[j] * (rotVec[axis][i][j] + rotCos[axis][i][j] * cosB + rotSin[axis][i][j] * sinB);
                        tempEyeX2[i] += eyeX[j] * (rotVec[axis][i][j] + rotCos[axis][i][j] * cosB + rotSin[axis][i][j] * sinB);
                    }
                }
                vMul(tempEyeY2, tempEye2, tempEyeX2);
                eyeArray[0] = eye;
                eyeArrayX[0] = eyeX;
                eyeArrayY[0] = eyeY;
                eyeArray[1] = tempEye;
                eyeArrayX[1] = tempEyeX;
                eyeArrayY[1] = tempEyeY;
                eyeArray[2] = tempEye2;
                eyeArrayX[2] = tempEyeX2;
                eyeArrayY[2] = tempEyeY2;
                blockArray[0] = topBlocks;
                blockArray[1] = midBlocks;
                blockArray[2] = botBlocks;
                vSub(vScale(vCopy(perspEye, eye), 5.0 + persp), vScale(vCopy(perspNormal, faceNormals[twistedLayer]), 1.0 / 3.0));
                vSub(vScale(vCopy(perspEyeI, eye), 5.0 + persp), vScale(vCopy(perspNormal, faceNormals[twistedLayer ^ 1]), 1.0 / 3.0));
                double topProd = vProd(perspEye, faceNormals[twistedLayer]);
                double botProd = vProd(perspEyeI, faceNormals[twistedLayer ^ 1]);
                int orderMode;
                if (topProd < 0 && botProd > 0) orderMode = 0; else if (topProd > 0 && botProd < 0) orderMode = 1; else orderMode = 2;
                fixBlock(eyeArray[eyeOrder[twistedMode][drawOrder[orderMode][0]]], eyeArrayX[eyeOrder[twistedMode][drawOrder[orderMode][0]]], eyeArrayY[eyeOrder[twistedMode][drawOrder[orderMode][0]]], blockArray[drawOrder[orderMode][0]], blockMode[twistedMode][drawOrder[orderMode][0]]);
                fixBlock(eyeArray[eyeOrder[twistedMode][drawOrder[orderMode][1]]], eyeArrayX[eyeOrder[twistedMode][drawOrder[orderMode][1]]], eyeArrayY[eyeOrder[twistedMode][drawOrder[orderMode][1]]], blockArray[drawOrder[orderMode][1]], blockMode[twistedMode][drawOrder[orderMode][1]]);
                fixBlock(eyeArray[eyeOrder[twistedMode][drawOrder[orderMode][2]]], eyeArrayX[eyeOrder[twistedMode][drawOrder[orderMode][2]]], eyeArrayY[eyeOrder[twistedMode][drawOrder[orderMode][2]]], blockArray[drawOrder[orderMode][2]], blockMode[twistedMode][drawOrder[orderMode][2]]);
            }
            if (!pushed && !animating) buttonPressed = -1;
            if (!demo && move.length > 0) {
                if (move[curMove].length > 0) {
                    graphics.setColor(Color.black);
                    graphics.drawRect(0, height - progressHeight, width - 1, progressHeight - 1);
                    graphics.setColor(textColor);
                    int progress = (width - 2) * realMovePos(move[curMove], movePos) / realMoveLength(move[curMove]);
                    graphics.fillRect(1, height - progressHeight + 1, progress, progressHeight - 2);
                    graphics.setColor(bgColor.darker());
                    graphics.fillRect(1 + progress, height - progressHeight + 1, width - 2 - progress, progressHeight - 2);
                    String s = "" + moveLength(move[curMove], movePos) + "/" + moveLength(move[curMove], -1) + metricChar[metric];
                    int w = graphics.getFontMetrics().stringWidth(s);
                    int x = width - w - 2;
                    int y = height - progressHeight - 2;
                    if (moveText > 0 && textHeight > 0) {
                        drawString(graphics, s, x, y - textHeight);
                        drawMoveText(graphics, y);
                    } else drawString(graphics, s, x, y);
                }
                if (move.length > 1) {
                    graphics.setClip(0, 0, width, height);
                    int b = graphics.getFontMetrics().getDescent();
                    int y = textHeight - b;
                    String s = "" + (curMove + 1) + "/" + move.length;
                    int w = graphics.getFontMetrics().stringWidth(s);
                    int x = width - w - buttonHeight - 2;
                    drawString(graphics, s, x, y);
                    graphics.setColor(buttonBgColor);
                    graphics.fill3DRect(width - buttonHeight, 0, buttonHeight, buttonHeight, buttonPressed != 7);
                    drawButton(graphics, 7, width - buttonHeight / 2, buttonHeight / 2);
                }
            }
            if (curInfoText >= 0) {
                graphics.setClip(0, 0, width, height);
                int b = graphics.getFontMetrics().getDescent();
                int y = textHeight - b;
                drawString(graphics, infoText[curInfoText], 0, y);
            }
            if (drawButtons && buttonBar != 0) drawButtons(graphics);
        }
        g.drawImage(image, 0, 0, this);
    }

    public void update(Graphics g) {
        paint(g);
    }

    private final int[] fillX = new int[4];

    private final int[] fillY = new int[4];

    private final double[] coordsX = new double[8];

    private final double[] coordsY = new double[8];

    private final double[][] cooX = new double[6][4];

    private final double[][] cooY = new double[6][4];

    private static final double[][] border = { { 0.10, 0.10 }, { 0.90, 0.10 }, { 0.90, 0.90 }, { 0.10, 0.90 } };

    private static final int[][] factors = { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 1, 0 } };

    private final double[] tempNormal = new double[3];

    private void fixBlock(double[] eye, double[] eyeX, double[] eyeY, int[][][] blocks, int mode) {
        for (int i = 0; i < 8; i++) {
            double min = width < height ? width : height - progressHeight;
            double x = min / 3.7 * vProd(cornerCoords[i], eyeX) * scale;
            double y = min / 3.7 * vProd(cornerCoords[i], eyeY) * scale;
            double z = min / (5.0 + persp) * vProd(cornerCoords[i], eye) * scale;
            x = x / (1 - z / min);
            y = y / (1 - z / min);
            coordsX[i] = width / 2.0 + x;
            if (align == 0) coordsY[i] = (height - progressHeight) / 2.0 * scale - y; else if (align == 2) coordsY[i] = height - progressHeight - (height - progressHeight) / 2.0 * scale - y; else coordsY[i] = (height - progressHeight) / 2.0 - y;
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                cooX[i][j] = coordsX[faceCorners[i][j]];
                cooY[i][j] = coordsY[faceCorners[i][j]];
            }
        }
        if (hint) {
            for (int i = 0; i < 6; i++) {
                vSub(vScale(vCopy(perspEye, eye), 5.0 + persp), faceNormals[i]);
                if (vProd(perspEye, faceNormals[i]) < 0) {
                    vScale(vCopy(tempNormal, faceNormals[i]), faceShift);
                    double min = width < height ? width : height - progressHeight;
                    double x = min / 3.7 * vProd(tempNormal, eyeX);
                    double y = min / 3.7 * vProd(tempNormal, eyeY);
                    double z = min / (5.0 + persp) * vProd(tempNormal, eye);
                    x = x / (1 - z / min);
                    y = y / (1 - z / min);
                    int sideW = blocks[i][0][1] - blocks[i][0][0];
                    int sideH = blocks[i][1][1] - blocks[i][1][0];
                    if (sideW > 0 && sideH > 0) {
                        for (int n = 0, p = blocks[i][1][0]; n < sideH; n++, p++) {
                            for (int o = 0, q = blocks[i][0][0]; o < sideW; o++, q++) {
                                for (int j = 0; j < 4; j++) {
                                    getCorners(i, j, fillX, fillY, q + border[j][0], p + border[j][1], mirrored);
                                    fillX[j] += mirrored ? -x : x;
                                    fillY[j] -= y;
                                }
                                graphics.setColor(colors[cube[i][p * 3 + q]]);
                                graphics.fillPolygon(fillX, fillY, 4);
                                graphics.setColor(colors[cube[i][p * 3 + q]].darker());
                                graphics.drawPolygon(fillX, fillY, 4);
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            int sideW = blocks[i][0][1] - blocks[i][0][0];
            int sideH = blocks[i][1][1] - blocks[i][1][0];
            if (sideW > 0 && sideH > 0) {
                for (int j = 0; j < 4; j++) getCorners(i, j, fillX, fillY, blocks[i][0][factors[j][0]], blocks[i][1][factors[j][1]], mirrored);
                if (sideW == 3 && sideH == 3) graphics.setColor(bgColor2); else graphics.setColor(Color.black);
                graphics.drawPolygon(fillX, fillY, 4);
            }
        }
        for (int i = 0; i < 6; i++) {
            int sideW = blocks[i][0][1] - blocks[i][0][0];
            int sideH = blocks[i][1][1] - blocks[i][1][0];
            if (sideW <= 0 || sideH <= 0) {
                for (int j = 0; j < 4; j++) {
                    int k = oppositeCorners[i][j];
                    fillX[j] = (int) (cooX[i][j] + (cooX[i ^ 1][k] - cooX[i][j]) * 2.0 / 3.0);
                    fillY[j] = (int) (cooY[i][j] + (cooY[i ^ 1][k] - cooY[i][j]) * 2.0 / 3.0);
                    if (mirrored) fillX[j] = width - fillX[j];
                }
                graphics.setColor(Color.black);
                graphics.fillPolygon(fillX, fillY, 4);
            } else {
                for (int j = 0; j < 4; j++) getCorners(i, j, fillX, fillY, blocks[i][0][factors[j][0]], blocks[i][1][factors[j][1]], mirrored);
                graphics.setColor(Color.black);
                graphics.fillPolygon(fillX, fillY, 4);
            }
        }
        for (int i = 0; i < 6; i++) {
            vSub(vScale(vCopy(perspEye, eye), 5.0 + persp), faceNormals[i]);
            if (vProd(perspEye, faceNormals[i]) > 0) {
                int sideW = blocks[i][0][1] - blocks[i][0][0];
                int sideH = blocks[i][1][1] - blocks[i][1][0];
                if (sideW > 0 && sideH > 0) {
                    for (int n = 0, p = blocks[i][1][0]; n < sideH; n++, p++) {
                        for (int o = 0, q = blocks[i][0][0]; o < sideW; o++, q++) {
                            for (int j = 0; j < 4; j++) getCorners(i, j, fillX, fillY, q + border[j][0], p + border[j][1], mirrored);
                            graphics.setColor(colors[cube[i][p * 3 + q]].darker());
                            graphics.drawPolygon(fillX, fillY, 4);
                            graphics.setColor(colors[cube[i][p * 3 + q]]);
                            graphics.fillPolygon(fillX, fillY, 4);
                        }
                    }
                }
                if (!editable || animating) continue;
                double dxh = (cooX[i][1] - cooX[i][0] + cooX[i][2] - cooX[i][3]) / 6.0;
                double dyh = (cooX[i][3] - cooX[i][0] + cooX[i][2] - cooX[i][1]) / 6.0;
                double dxv = (cooY[i][1] - cooY[i][0] + cooY[i][2] - cooY[i][3]) / 6.0;
                double dyv = (cooY[i][3] - cooY[i][0] + cooY[i][2] - cooY[i][1]) / 6.0;
                if (mode == 3) {
                    for (int j = 0; j < 6; j++) {
                        for (int k = 0; k < 4; k++) getCorners(i, k, dragCornersX[dragAreas], dragCornersY[dragAreas], dragBlocks[j][k][0], dragBlocks[j][k][1], false);
                        dragDirsX[dragAreas] = (dxh * areaDirs[j][0] + dxv * areaDirs[j][1]) * twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh * areaDirs[j][0] + dyv * areaDirs[j][1]) * twistDirs[i][j];
                        dragLayers[dragAreas] = adjacentFaces[i][j % 4];
                        if (j >= 4) dragLayers[dragAreas] &= ~1;
                        dragModes[dragAreas] = j / 4;
                        dragAreas++;
                        if (dragAreas == 18) break;
                    }
                } else if (mode == 0) {
                    if (i != twistedLayer && sideW > 0 && sideH > 0) {
                        int j = sideW == 3 ? (blocks[i][1][0] == 0 ? 0 : 2) : (blocks[i][0][0] == 0 ? 3 : 1);
                        for (int k = 0; k < 4; k++) getCorners(i, k, dragCornersX[dragAreas], dragCornersY[dragAreas], dragBlocks[j][k][0], dragBlocks[j][k][1], false);
                        dragDirsX[dragAreas] = (dxh * areaDirs[j][0] + dxv * areaDirs[j][1]) * twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh * areaDirs[j][0] + dyv * areaDirs[j][1]) * twistDirs[i][j];
                        dragLayers[dragAreas] = twistedLayer;
                        dragModes[dragAreas] = 0;
                        dragAreas++;
                    }
                } else if (mode == 1) {
                    if (i != twistedLayer && sideW > 0 && sideH > 0) {
                        int j = sideW == 3 ? 4 : 5;
                        for (int k = 0; k < 4; k++) getCorners(i, k, dragCornersX[dragAreas], dragCornersY[dragAreas], dragBlocks[j][k][0], dragBlocks[j][k][1], false);
                        dragDirsX[dragAreas] = (dxh * areaDirs[j][0] + dxv * areaDirs[j][1]) * twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh * areaDirs[j][0] + dyv * areaDirs[j][1]) * twistDirs[i][j];
                        dragLayers[dragAreas] = twistedLayer;
                        dragModes[dragAreas] = 1;
                        dragAreas++;
                    }
                }
            }
        }
    }

    private void getCorners(int face, int corner, int[] cornersX, int[] cornersY, double factor1, double factor2, boolean mirror) {
        factor1 /= 3.0;
        factor2 /= 3.0;
        double x1 = cooX[face][0] + (cooX[face][1] - cooX[face][0]) * factor1;
        double y1 = cooY[face][0] + (cooY[face][1] - cooY[face][0]) * factor1;
        double x2 = cooX[face][3] + (cooX[face][2] - cooX[face][3]) * factor1;
        double y2 = cooY[face][3] + (cooY[face][2] - cooY[face][3]) * factor1;
        cornersX[corner] = (int) (0.5 + x1 + (x2 - x1) * factor2);
        cornersY[corner] = (int) (0.5 + y1 + (y2 - y1) * factor2);
        if (mirror) cornersX[corner] = width - cornersX[corner];
    }

    private void drawButtons(Graphics g) {
        if (buttonBar == 2) {
            g.setColor(buttonBgColor);
            g.fill3DRect(0, height - buttonHeight, buttonHeight, buttonHeight, buttonPressed != 0);
            drawButton(g, 0, buttonHeight / 2, height - (buttonHeight + 1) / 2);
            return;
        }
        if (buttonBar == 1) {
            g.setClip(0, height, width, buttonHeight);
            int buttonX = 0;
            for (int i = 0; i < 7; i++) {
                int buttonWidth = (width - buttonX) / (7 - i);
                g.setColor(buttonBgColor);
                g.fill3DRect(buttonX, height, buttonWidth, buttonHeight, buttonPressed != i);
                drawButton(g, i, buttonX + buttonWidth / 2, height + buttonHeight / 2);
                buttonX += buttonWidth;
            }
            drawButtons = false;
            return;
        }
    }

    private void drawButton(Graphics g, int i, int x, int y) {
        g.setColor(Color.white);
        switch(i) {
            case 0:
                drawRect(g, x - 4, y - 3, 3, 7);
                drawArrow(g, x + 3, y, -1);
                break;
            case 1:
                drawRect(g, x + 2, y - 3, 3, 7);
                drawArrow(g, x, y, -1);
                break;
            case 2:
                drawArrow(g, x + 2, y, -1);
                break;
            case 3:
                if (animating) drawRect(g, x - 3, y - 3, 7, 7); else {
                    drawRect(g, x - 3, y - 2, 7, 5);
                    drawRect(g, x - 1, y - 4, 3, 9);
                }
                break;
            case 4:
                drawArrow(g, x - 2, y, 1);
                break;
            case 5:
                drawRect(g, x - 4, y - 3, 3, 7);
                drawArrow(g, x, y, 1);
                break;
            case 6:
                drawRect(g, x + 1, y - 3, 3, 7);
                drawArrow(g, x - 4, y, 1);
                break;
            case 7:
                drawArrow(g, x - 2, y, 1);
                break;
        }
    }

    private static void drawArrow(Graphics g, int x, int y, int dir) {
        g.setColor(Color.black);
        g.drawLine(x, y - 3, x, y + 3);
        x += dir;
        for (int i = 0; i >= -3 && i <= 3; i += dir) {
            int j = 3 - i * dir;
            g.drawLine(x + i, y + j, x + i, y - j);
        }
        g.setColor(Color.white);
        for (int i = 0; i >= -1 && i <= 1; i += dir) {
            int j = 1 - i * dir;
            g.drawLine(x + i, y + j, x + i, y - j);
        }
    }

    private static void drawRect(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.black);
        g.drawRect(x, y, width - 1, height - 1);
        g.setColor(Color.white);
        g.fillRect(x + 1, y + 1, width - 2, height - 2);
    }

    private static final int[] textOffset = { 1, 1, -1, -1, -1, 1, 1, -1, -1, 0, 1, 0, 0, 1, 0, -1 };

    private void drawString(Graphics g, String s, int x, int y) {
        if (outlined) {
            g.setColor(Color.black);
            for (int i = 0; i < textOffset.length; i += 2) g.drawString(s, x + textOffset[i], y + textOffset[i + 1]);
            g.setColor(Color.white);
        } else g.setColor(textColor);
        g.drawString(s, x, y);
    }

    private void drawMoveText(Graphics g, int y) {
        g.setClip(0, height - progressHeight - textHeight, width, textHeight);
        g.setColor(Color.black);
        int pos = movePos == 0 ? arrayMovePos(move[curMove], movePos) : movePos;
        String s1 = moveText(move[curMove], 0, pos);
        String s2 = turnText(move[curMove], pos);
        String s3 = moveText(move[curMove], pos + 1, move[curMove].length);
        int w1 = g.getFontMetrics().stringWidth(s1);
        int w2 = g.getFontMetrics().stringWidth(s2);
        int w3 = g.getFontMetrics().stringWidth(s3);
        int x = 1;
        if (x + w1 + w2 + w3 > width) {
            x = Math.min(1, width / 2 - w1 - w2 / 2);
            x = Math.max(x, width - w1 - w2 - w3 - 2);
        }
        if (w2 > 0) {
            g.setColor(hlColor);
            g.fillRect(x + w1 - 1, height - progressHeight - textHeight, w2 + 2, textHeight);
        }
        if (w1 > 0) drawString(g, s1, x, y);
        if (w2 > 0) drawString(g, s2, x + w1, y);
        if (w3 > 0) drawString(g, s3, x + w1 + w2, y);
    }

    private int selectButton(int x, int y) {
        if (buttonBar == 0) return -1;
        if (move.length > 1 && x >= width - buttonHeight && x < width && y >= 0 && y < buttonHeight) return 7;
        if (buttonBar == 2) {
            if (x >= 0 && x < buttonHeight && y >= height - buttonHeight && y < height) return 0;
            return -1;
        }
        if (y < height) return -1;
        int buttonX = 0;
        for (int i = 0; i < 7; i++) {
            int buttonWidth = (width - buttonX) / (7 - i);
            if (x >= buttonX && x < buttonX + buttonWidth && y >= height && y < height + buttonHeight) return i;
            buttonX += buttonWidth;
        }
        return -1;
    }

    private static final int[] buttonAction = { -1, 3, 1, -1, 0, 2, 4, -1 };

    public void mousePressed(MouseEvent e) {
        lastDragX = lastX = e.getX();
        lastDragY = lastY = e.getY();
        toTwist = false;
        buttonPressed = selectButton(lastX, lastY);
        if (buttonPressed >= 0) {
            pushed = true;
            if (buttonPressed == 3) {
                if (!animating) mirrored = !mirrored; else stopAnimation();
            } else if (buttonPressed == 0) {
                stopAnimation();
                clear();
            } else if (buttonPressed == 7) {
                stopAnimation();
                clear();
                curMove = curMove < move.length - 1 ? curMove + 1 : 0;
            } else startAnimation(buttonAction[buttonPressed]);
            drawButtons = true;
            repaint();
        } else if (progressHeight > 0 && move.length > 0 && move[curMove].length > 0 && lastY >= height - progressHeight && lastY < height) {
            stopAnimation();
            int len = realMoveLength(move[curMove]);
            int pos = ((lastX - 1) * len * 2 / (width - 2) + 1) / 2;
            pos = Math.max(0, Math.min(len, pos));
            if (pos > 0) pos = arrayMovePos(move[curMove], pos);
            if (pos > movePos) doMove(cube, move[curMove], movePos, pos - movePos, false);
            if (pos < movePos) doMove(cube, move[curMove], pos, movePos - pos, true);
            movePos = pos;
            dragging = true;
            repaint();
        } else {
            if (mirrored) lastDragX = lastX = width - lastX;
            if (editable && !animating && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && (e.getModifiers() & InputEvent.SHIFT_MASK) == 0) toTwist = true;
        }
    }

    public void mouseReleased(MouseEvent e) {
        dragging = false;
        if (pushed) {
            pushed = false;
            drawButtons = true;
            repaint();
        } else if (twisting && !spinning) {
            twisting = false;
            originalAngle += currentAngle;
            currentAngle = 0.0;
            double angle = originalAngle;
            while (angle < 0.0) angle += 32.0 * Math.PI;
            int num = (int) (angle * 8.0 / Math.PI) % 16;
            if (num % 4 == 0 || num % 4 == 3) {
                num = (num + 1) / 4;
                if (faceTwistDirs[twistedLayer] > 0) num = (4 - num) % 4;
                originalAngle = 0;
                natural = true;
                twistLayers(cube, twistedLayer, num, twistedMode);
            }
            repaint();
        }
    }

    private final double[] eyeD = new double[3];

    public void mouseDragged(MouseEvent e) {
        if (pushed) return;
        if (dragging) {
            stopAnimation();
            int len = realMoveLength(move[curMove]);
            int pos = ((e.getX() - 1) * len * 2 / (width - 2) + 1) / 2;
            pos = Math.max(0, Math.min(len, pos));
            if (pos > 0) pos = arrayMovePos(move[curMove], pos);
            if (pos > movePos) doMove(cube, move[curMove], movePos, pos - movePos, false);
            if (pos < movePos) doMove(cube, move[curMove], pos, movePos - pos, true);
            movePos = pos;
            repaint();
            return;
        }
        int x = mirrored ? width - e.getX() : e.getX();
        int y = e.getY();
        int dx = x - lastX;
        int dy = y - lastY;
        if (editable && toTwist && !twisting && !animating) {
            lastDragX = x;
            lastDragY = y;
            for (int i = 0; i < dragAreas; i++) {
                double d1 = dragCornersX[i][0];
                double x1 = dragCornersX[i][1] - d1;
                double y1 = dragCornersX[i][3] - d1;
                double d2 = dragCornersY[i][0];
                double x2 = dragCornersY[i][1] - d2;
                double y2 = dragCornersY[i][3] - d2;
                double a = (y2 * (lastX - d1) - y1 * (lastY - d2)) / (x1 * y2 - y1 * x2);
                double b = (-x2 * (lastX - d1) + x1 * (lastY - d2)) / (x1 * y2 - y1 * x2);
                if (a > 0 && a < 1 && b > 0 && b < 1) {
                    if (dx * dx + dy * dy < 144) return;
                    dragX = dragDirsX[i];
                    dragY = dragDirsY[i];
                    double d = Math.abs(dragX * dx + dragY * dy) / Math.sqrt((dragX * dragX + dragY * dragY) * (dx * dx + dy * dy));
                    if (d > 0.75) {
                        twisting = true;
                        twistedLayer = dragLayers[i];
                        twistedMode = dragModes[i];
                        break;
                    }
                }
            }
            toTwist = false;
            lastX = lastDragX;
            lastY = lastDragY;
        }
        dx = x - lastX;
        dy = y - lastY;
        if (!twisting || animating) {
            vNorm(vAdd(eye, vScale(vCopy(eyeD, eyeX), dx * -0.016)));
            vNorm(vMul(eyeX, eyeY, eye));
            vNorm(vAdd(eye, vScale(vCopy(eyeD, eyeY), dy * 0.016)));
            vNorm(vMul(eyeY, eye, eyeX));
            lastX = x;
            lastY = y;
        } else {
            if (natural) splitCube(twistedLayer);
            currentAngle = 0.03 * (dragX * dx + dragY * dy) / Math.sqrt(dragX * dragX + dragY * dragY);
        }
        repaint();
    }

    private static final String[] buttonDescriptions = { "Clear to the initial state", "Show the previous step", "Play backward", "Stop", "Play", "Show the next step", "Go to the end", "Next sequence" };

    private String buttonDescription = "";

    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        String description = "Drag the cube with a mouse";
        if (x >= 0 && x < width) {
            if (y >= height && y < height + buttonHeight || y >= 0 && y < buttonHeight) {
                buttonPressed = selectButton(x, y);
                if (buttonPressed >= 0) description = buttonDescriptions[buttonPressed];
                if (buttonPressed == 3 && !animating) description = "Mirror the cube view";
            } else if (progressHeight > 0 && move.length > 0 && move[curMove].length > 0 && y >= height - progressHeight && y < height) {
                description = "Current progress";
            }
        }
        if (description != buttonDescription) {
            buttonDescription = description;
            showStatus(description);
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private static double[] vCopy(double[] vector, double[] srcVec) {
        vector[0] = srcVec[0];
        vector[1] = srcVec[1];
        vector[2] = srcVec[2];
        return vector;
    }

    private static double[] vNorm(double[] vector) {
        double length = Math.sqrt(vProd(vector, vector));
        vector[0] /= length;
        vector[1] /= length;
        vector[2] /= length;
        return vector;
    }

    private static double[] vScale(double[] vector, double value) {
        vector[0] *= value;
        vector[1] *= value;
        vector[2] *= value;
        return vector;
    }

    private static double vProd(double[] vec1, double[] vec2) {
        return vec1[0] * vec2[0] + vec1[1] * vec2[1] + vec1[2] * vec2[2];
    }

    private static double[] vAdd(double[] vector, double[] srcVec) {
        vector[0] += srcVec[0];
        vector[1] += srcVec[1];
        vector[2] += srcVec[2];
        return vector;
    }

    private static double[] vSub(double[] vector, double[] srcVec) {
        vector[0] -= srcVec[0];
        vector[1] -= srcVec[1];
        vector[2] -= srcVec[2];
        return vector;
    }

    private static double[] vMul(double[] vector, double[] vec1, double[] vec2) {
        vector[0] = vec1[1] * vec2[2] - vec1[2] * vec2[1];
        vector[1] = vec1[2] * vec2[0] - vec1[0] * vec2[2];
        vector[2] = vec1[0] * vec2[1] - vec1[1] * vec2[0];
        return vector;
    }

    private static double[] vRotX(double[] vector, double angle) {
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        double y = vector[1] * cosA - vector[2] * sinA;
        double z = vector[1] * sinA + vector[2] * cosA;
        vector[1] = y;
        vector[2] = z;
        return vector;
    }

    private static double[] vRotY(double[] vector, double angle) {
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        double x = vector[0] * cosA - vector[2] * sinA;
        double z = vector[0] * sinA + vector[2] * cosA;
        vector[0] = x;
        vector[2] = z;
        return vector;
    }

    private static double[] vRotZ(double[] vector, double angle) {
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        double x = vector[0] * cosA - vector[1] * sinA;
        double y = vector[0] * sinA + vector[1] * cosA;
        vector[0] = x;
        vector[1] = y;
        return vector;
    }

    public void keyPressed(KeyEvent e) {
        char c = e.getKeyChar();
        System.out.println("key character = '" + c + "'");
        int location = e.getKeyLocation();
        if (location == KeyEvent.KEY_LOCATION_LEFT) {
            System.out.println("Key LEFT");
            startAnimation(3);
        } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
            System.out.println("Key RIGHT");
            startAnimation(2);
        }
        drawButtons = true;
        dragging = false;
        pushed = false;
        drawButtons = true;
        repaint();
    }

    public void keyReleased(KeyEvent arg0) {
    }

    public void keyTyped(KeyEvent arg0) {
    }
}

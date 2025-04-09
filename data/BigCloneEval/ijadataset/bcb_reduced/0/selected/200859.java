package net.sf.cb2xml.sablecc.lexer;

import java.io.*;
import java.util.*;
import net.sf.cb2xml.sablecc.node.*;

public class Lexer {

    protected Token token;

    protected State state = State.INITIAL;

    private PushbackReader in;

    private int line;

    private int pos;

    private boolean cr;

    private boolean eof;

    private final StringBuffer text = new StringBuffer();

    protected void filter() throws LexerException, IOException {
    }

    public Lexer(PushbackReader in) {
        this.in = in;
        if (gotoTable == null) {
            try {
                DataInputStream s = new DataInputStream(new BufferedInputStream(Lexer.class.getResourceAsStream("lexer.dat")));
                int length = s.readInt();
                gotoTable = new int[length][][][];
                for (int i = 0; i < gotoTable.length; i++) {
                    length = s.readInt();
                    gotoTable[i] = new int[length][][];
                    for (int j = 0; j < gotoTable[i].length; j++) {
                        length = s.readInt();
                        gotoTable[i][j] = new int[length][3];
                        for (int k = 0; k < gotoTable[i][j].length; k++) {
                            for (int l = 0; l < 3; l++) {
                                gotoTable[i][j][k][l] = s.readInt();
                            }
                        }
                    }
                }
                length = s.readInt();
                accept = new int[length][];
                for (int i = 0; i < accept.length; i++) {
                    length = s.readInt();
                    accept[i] = new int[length];
                    for (int j = 0; j < accept[i].length; j++) {
                        accept[i][j] = s.readInt();
                    }
                }
                s.close();
            } catch (Exception e) {
                throw new RuntimeException("The file \"lexer.dat\" is either missing or corrupted.");
            }
        }
    }

    public Token peek() throws LexerException, IOException {
        while (token == null) {
            token = getToken();
            filter();
        }
        return token;
    }

    public Token next() throws LexerException, IOException {
        while (token == null) {
            token = getToken();
            filter();
        }
        Token result = token;
        token = null;
        return result;
    }

    protected Token getToken() throws IOException, LexerException {
        int dfa_state = 0;
        int start_pos = pos;
        int start_line = line;
        int accept_state = -1;
        int accept_token = -1;
        int accept_length = -1;
        int accept_pos = -1;
        int accept_line = -1;
        int[][][] gotoTable = this.gotoTable[state.id()];
        int[] accept = this.accept[state.id()];
        text.setLength(0);
        while (true) {
            int c = getChar();
            if (c != -1) {
                switch(c) {
                    case 10:
                        if (cr) {
                            cr = false;
                        } else {
                            line++;
                            pos = 0;
                        }
                        break;
                    case 13:
                        line++;
                        pos = 0;
                        cr = true;
                        break;
                    default:
                        pos++;
                        cr = false;
                        break;
                }
                ;
                text.append((char) c);
                do {
                    int oldState = (dfa_state < -1) ? (-2 - dfa_state) : dfa_state;
                    dfa_state = -1;
                    int[][] tmp1 = gotoTable[oldState];
                    int low = 0;
                    int high = tmp1.length - 1;
                    while (low <= high) {
                        int middle = (low + high) / 2;
                        int[] tmp2 = tmp1[middle];
                        if (c < tmp2[0]) {
                            high = middle - 1;
                        } else if (c > tmp2[1]) {
                            low = middle + 1;
                        } else {
                            dfa_state = tmp2[2];
                            break;
                        }
                    }
                } while (dfa_state < -1);
            } else {
                dfa_state = -1;
            }
            if (dfa_state >= 0) {
                if (accept[dfa_state] != -1) {
                    accept_state = dfa_state;
                    accept_token = accept[dfa_state];
                    accept_length = text.length();
                    accept_pos = pos;
                    accept_line = line;
                }
            } else {
                if (accept_state != -1) {
                    switch(accept_token) {
                        case 0:
                            {
                                Token token = new0(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 1:
                            {
                                Token token = new1(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 2:
                            {
                                Token token = new2(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 3:
                            {
                                Token token = new3(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 4:
                            {
                                Token token = new4(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 5:
                            {
                                Token token = new5(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 6:
                            {
                                Token token = new6(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 7:
                            {
                                Token token = new7(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 8:
                            {
                                Token token = new8(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 9:
                            {
                                Token token = new9(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 10:
                            {
                                Token token = new10(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 11:
                            {
                                Token token = new11(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 12:
                            {
                                Token token = new12(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 13:
                            {
                                Token token = new13(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 14:
                            {
                                Token token = new14(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 15:
                            {
                                Token token = new15(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 16:
                            {
                                Token token = new16(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 17:
                            {
                                Token token = new17(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 18:
                            {
                                Token token = new18(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 19:
                            {
                                Token token = new19(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 20:
                            {
                                Token token = new20(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 21:
                            {
                                Token token = new21(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 22:
                            {
                                Token token = new22(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 23:
                            {
                                Token token = new23(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 24:
                            {
                                Token token = new24(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 25:
                            {
                                Token token = new25(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 26:
                            {
                                Token token = new26(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 27:
                            {
                                Token token = new27(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 28:
                            {
                                Token token = new28(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 29:
                            {
                                Token token = new29(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 30:
                            {
                                Token token = new30(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 31:
                            {
                                Token token = new31(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 32:
                            {
                                Token token = new32(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 33:
                            {
                                Token token = new33(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 34:
                            {
                                Token token = new34(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 35:
                            {
                                Token token = new35(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 36:
                            {
                                Token token = new36(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 37:
                            {
                                Token token = new37(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 38:
                            {
                                Token token = new38(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 39:
                            {
                                Token token = new39(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 40:
                            {
                                Token token = new40(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 41:
                            {
                                Token token = new41(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 42:
                            {
                                Token token = new42(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 43:
                            {
                                Token token = new43(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 44:
                            {
                                Token token = new44(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 45:
                            {
                                Token token = new45(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 46:
                            {
                                Token token = new46(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 47:
                            {
                                Token token = new47(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 48:
                            {
                                Token token = new48(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 49:
                            {
                                Token token = new49(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 50:
                            {
                                Token token = new50(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 51:
                            {
                                Token token = new51(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 52:
                            {
                                Token token = new52(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 53:
                            {
                                Token token = new53(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 54:
                            {
                                Token token = new54(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 55:
                            {
                                Token token = new55(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 56:
                            {
                                Token token = new56(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 57:
                            {
                                Token token = new57(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 58:
                            {
                                Token token = new58(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 59:
                            {
                                Token token = new59(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 60:
                            {
                                Token token = new60(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 61:
                            {
                                Token token = new61(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 62:
                            {
                                Token token = new62(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 63:
                            {
                                Token token = new63(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 64:
                            {
                                Token token = new64(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 65:
                            {
                                Token token = new65(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 66:
                            {
                                Token token = new66(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 67:
                            {
                                Token token = new67(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 68:
                            {
                                Token token = new68(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 69:
                            {
                                Token token = new69(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 70:
                            {
                                Token token = new70(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 71:
                            {
                                Token token = new71(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 72:
                            {
                                Token token = new72(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 73:
                            {
                                Token token = new73(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 74:
                            {
                                Token token = new74(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 75:
                            {
                                Token token = new75(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 76:
                            {
                                Token token = new76(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 77:
                            {
                                Token token = new77(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 78:
                            {
                                Token token = new78(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 79:
                            {
                                Token token = new79(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 80:
                            {
                                Token token = new80(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                    }
                } else {
                    if (text.length() > 0) {
                        throw new LexerException("[" + (start_line + 1) + "," + (start_pos + 1) + "]" + " Unknown token: " + text);
                    } else {
                        EOF token = new EOF(start_line + 1, start_pos + 1);
                        return token;
                    }
                }
            }
        }
    }

    Token new0(String text, int line, int pos) {
        return new TWhiteSpace(text, line, pos);
    }

    Token new1(String text, int line, int pos) {
        return new TComment(text, line, pos);
    }

    Token new2(int line, int pos) {
        return new TDot(line, pos);
    }

    Token new3(int line, int pos) {
        return new TComma(line, pos);
    }

    Token new4(int line, int pos) {
        return new TSlash(line, pos);
    }

    Token new5(int line, int pos) {
        return new TPlus(line, pos);
    }

    Token new6(int line, int pos) {
        return new TMinus(line, pos);
    }

    Token new7(int line, int pos) {
        return new TStar(line, pos);
    }

    Token new8(int line, int pos) {
        return new TDollar(line, pos);
    }

    Token new9(int line, int pos) {
        return new TLparen(line, pos);
    }

    Token new10(int line, int pos) {
        return new TRparen(line, pos);
    }

    Token new11(String text, int line, int pos) {
        return new TNumber88(text, line, pos);
    }

    Token new12(String text, int line, int pos) {
        return new TNumberNot88(text, line, pos);
    }

    Token new13(String text, int line, int pos) {
        return new TAlphanumericLiteral(text, line, pos);
    }

    Token new14(String text, int line, int pos) {
        return new TNumericLiteral(text, line, pos);
    }

    Token new15(String text, int line, int pos) {
        return new TDotZee(text, line, pos);
    }

    Token new16(String text, int line, int pos) {
        return new TDotMinus(text, line, pos);
    }

    Token new17(String text, int line, int pos) {
        return new TDotPlus(text, line, pos);
    }

    Token new18(String text, int line, int pos) {
        return new TAll(text, line, pos);
    }

    Token new19(String text, int line, int pos) {
        return new TAre(text, line, pos);
    }

    Token new20(String text, int line, int pos) {
        return new TAscending(text, line, pos);
    }

    Token new21(String text, int line, int pos) {
        return new TBinary(text, line, pos);
    }

    Token new22(String text, int line, int pos) {
        return new TBlank(text, line, pos);
    }

    Token new23(String text, int line, int pos) {
        return new TBy(text, line, pos);
    }

    Token new24(String text, int line, int pos) {
        return new TCharacter(text, line, pos);
    }

    Token new25(String text, int line, int pos) {
        return new TComp(text, line, pos);
    }

    Token new26(String text, int line, int pos) {
        return new TComp1(text, line, pos);
    }

    Token new27(String text, int line, int pos) {
        return new TComp2(text, line, pos);
    }

    Token new28(String text, int line, int pos) {
        return new TComp3(text, line, pos);
    }

    Token new29(String text, int line, int pos) {
        return new TComp4(text, line, pos);
    }

    Token new30(String text, int line, int pos) {
        return new TComp5(text, line, pos);
    }

    Token new31(String text, int line, int pos) {
        return new TComp6(text, line, pos);
    }

    Token new32(String text, int line, int pos) {
        return new TDate(text, line, pos);
    }

    Token new33(String text, int line, int pos) {
        return new TDepending(text, line, pos);
    }

    Token new34(String text, int line, int pos) {
        return new TDescending(text, line, pos);
    }

    Token new35(String text, int line, int pos) {
        return new TDisplay(text, line, pos);
    }

    Token new36(String text, int line, int pos) {
        return new TDisplay1(text, line, pos);
    }

    Token new37(String text, int line, int pos) {
        return new TExternal(text, line, pos);
    }

    Token new38(String text, int line, int pos) {
        return new TFiller(text, line, pos);
    }

    Token new39(String text, int line, int pos) {
        return new TFormat(text, line, pos);
    }

    Token new40(String text, int line, int pos) {
        return new TFunctionPointer(text, line, pos);
    }

    Token new41(String text, int line, int pos) {
        return new TGlobal(text, line, pos);
    }

    Token new42(String text, int line, int pos) {
        return new THighValues(text, line, pos);
    }

    Token new43(String text, int line, int pos) {
        return new TIndex(text, line, pos);
    }

    Token new44(String text, int line, int pos) {
        return new TIndexed(text, line, pos);
    }

    Token new45(String text, int line, int pos) {
        return new TIs(text, line, pos);
    }

    Token new46(String text, int line, int pos) {
        return new TJustified(text, line, pos);
    }

    Token new47(String text, int line, int pos) {
        return new TKey(text, line, pos);
    }

    Token new48(String text, int line, int pos) {
        return new TLeading(text, line, pos);
    }

    Token new49(String text, int line, int pos) {
        return new TLeft(text, line, pos);
    }

    Token new50(String text, int line, int pos) {
        return new TLowValues(text, line, pos);
    }

    Token new51(String text, int line, int pos) {
        return new TNational(text, line, pos);
    }

    Token new52(String text, int line, int pos) {
        return new TNative(text, line, pos);
    }

    Token new53(String text, int line, int pos) {
        return new TNulls(text, line, pos);
    }

    Token new54(String text, int line, int pos) {
        return new TObject(text, line, pos);
    }

    Token new55(String text, int line, int pos) {
        return new TOccurs(text, line, pos);
    }

    Token new56(String text, int line, int pos) {
        return new TOn(text, line, pos);
    }

    Token new57(String text, int line, int pos) {
        return new TPackedDecimal(text, line, pos);
    }

    Token new58(String text, int line, int pos) {
        return new TPicture(text, line, pos);
    }

    Token new59(String text, int line, int pos) {
        return new TPointer(text, line, pos);
    }

    Token new60(String text, int line, int pos) {
        return new TProcedurePointer(text, line, pos);
    }

    Token new61(String text, int line, int pos) {
        return new TQuotes(text, line, pos);
    }

    Token new62(String text, int line, int pos) {
        return new TRedefines(text, line, pos);
    }

    Token new63(String text, int line, int pos) {
        return new TReference(text, line, pos);
    }

    Token new64(String text, int line, int pos) {
        return new TRenames(text, line, pos);
    }

    Token new65(String text, int line, int pos) {
        return new TRight(text, line, pos);
    }

    Token new66(String text, int line, int pos) {
        return new TSeparate(text, line, pos);
    }

    Token new67(String text, int line, int pos) {
        return new TSign(text, line, pos);
    }

    Token new68(String text, int line, int pos) {
        return new TSpaces(text, line, pos);
    }

    Token new69(String text, int line, int pos) {
        return new TSynchronized(text, line, pos);
    }

    Token new70(String text, int line, int pos) {
        return new TThrough(text, line, pos);
    }

    Token new71(String text, int line, int pos) {
        return new TTimes(text, line, pos);
    }

    Token new72(String text, int line, int pos) {
        return new TTo(text, line, pos);
    }

    Token new73(String text, int line, int pos) {
        return new TTrailing(text, line, pos);
    }

    Token new74(String text, int line, int pos) {
        return new TUsage(text, line, pos);
    }

    Token new75(String text, int line, int pos) {
        return new TValue(text, line, pos);
    }

    Token new76(String text, int line, int pos) {
        return new TValues(text, line, pos);
    }

    Token new77(String text, int line, int pos) {
        return new TWhen(text, line, pos);
    }

    Token new78(String text, int line, int pos) {
        return new TZeros(text, line, pos);
    }

    Token new79(String text, int line, int pos) {
        return new TDataName(text, line, pos);
    }

    Token new80(String text, int line, int pos) {
        return new TUnknown(text, line, pos);
    }

    private int getChar() throws IOException {
        if (eof) {
            return -1;
        }
        int result = in.read();
        if (result == -1) {
            eof = true;
        }
        return result;
    }

    private void pushBack(int acceptLength) throws IOException {
        int length = text.length();
        for (int i = length - 1; i >= acceptLength; i--) {
            eof = false;
            in.unread(text.charAt(i));
        }
    }

    protected void unread(Token token) throws IOException {
        String text = token.getText();
        int length = text.length();
        for (int i = length - 1; i >= 0; i--) {
            eof = false;
            in.unread(text.charAt(i));
        }
        pos = token.getPos() - 1;
        line = token.getLine() - 1;
    }

    private String getText(int acceptLength) {
        StringBuffer s = new StringBuffer(acceptLength);
        for (int i = 0; i < acceptLength; i++) {
            s.append(text.charAt(i));
        }
        return s.toString();
    }

    private static int[][][][] gotoTable;

    private static int[][] accept;

    public static class State {

        public static final State INITIAL = new State(0);

        private int id;

        private State(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }
    }
}

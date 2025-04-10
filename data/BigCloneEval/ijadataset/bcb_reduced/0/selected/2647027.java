package iwork.patchpanel.lexer;

import java.io.*;
import java.util.*;
import iwork.patchpanel.node.*;

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
                DataInputStream s = new DataInputStream(new BufferedInputStream(Lexer.class.getResourceAsStream("/iwork/patchpanel/lexer/lexer.dat")));
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
                                Token token = new1(start_line + 1, start_pos + 1);
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
                                Token token = new11(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 12:
                            {
                                Token token = new12(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 13:
                            {
                                Token token = new13(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 14:
                            {
                                Token token = new14(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 15:
                            {
                                Token token = new15(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 16:
                            {
                                Token token = new16(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 17:
                            {
                                Token token = new17(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 18:
                            {
                                Token token = new18(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 19:
                            {
                                Token token = new19(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 20:
                            {
                                Token token = new20(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 21:
                            {
                                Token token = new21(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 22:
                            {
                                Token token = new22(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 23:
                            {
                                Token token = new23(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 24:
                            {
                                Token token = new24(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 25:
                            {
                                Token token = new25(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 26:
                            {
                                Token token = new26(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 27:
                            {
                                Token token = new27(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 28:
                            {
                                Token token = new28(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 29:
                            {
                                Token token = new29(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 30:
                            {
                                Token token = new30(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 31:
                            {
                                Token token = new31(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 32:
                            {
                                Token token = new32(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 33:
                            {
                                Token token = new33(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 34:
                            {
                                Token token = new34(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 35:
                            {
                                Token token = new35(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 36:
                            {
                                Token token = new36(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 37:
                            {
                                Token token = new37(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 38:
                            {
                                Token token = new38(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 39:
                            {
                                Token token = new39(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 40:
                            {
                                Token token = new40(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 41:
                            {
                                Token token = new41(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 42:
                            {
                                Token token = new42(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 43:
                            {
                                Token token = new43(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 44:
                            {
                                Token token = new44(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                pos = accept_pos;
                                line = accept_line;
                                return token;
                            }
                        case 45:
                            {
                                Token token = new45(start_line + 1, start_pos + 1);
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

    Token new1(int line, int pos) {
        return new TAssign(line, pos);
    }

    Token new2(int line, int pos) {
        return new TLt(line, pos);
    }

    Token new3(int line, int pos) {
        return new TGt(line, pos);
    }

    Token new4(int line, int pos) {
        return new TEq(line, pos);
    }

    Token new5(int line, int pos) {
        return new TLteq(line, pos);
    }

    Token new6(int line, int pos) {
        return new TGteq(line, pos);
    }

    Token new7(int line, int pos) {
        return new TNeq(line, pos);
    }

    Token new8(int line, int pos) {
        return new TComplement(line, pos);
    }

    Token new9(int line, int pos) {
        return new TAnd(line, pos);
    }

    Token new10(int line, int pos) {
        return new TOr(line, pos);
    }

    Token new11(int line, int pos) {
        return new TPlus(line, pos);
    }

    Token new12(int line, int pos) {
        return new TMinus(line, pos);
    }

    Token new13(int line, int pos) {
        return new TStar(line, pos);
    }

    Token new14(int line, int pos) {
        return new TDiv(line, pos);
    }

    Token new15(int line, int pos) {
        return new TColon(line, pos);
    }

    Token new16(int line, int pos) {
        return new TTernary(line, pos);
    }

    Token new17(int line, int pos) {
        return new TComma(line, pos);
    }

    Token new18(int line, int pos) {
        return new TMax(line, pos);
    }

    Token new19(int line, int pos) {
        return new TMin(line, pos);
    }

    Token new20(int line, int pos) {
        return new TPower(line, pos);
    }

    Token new21(int line, int pos) {
        return new TMod(line, pos);
    }

    Token new22(int line, int pos) {
        return new TCos(line, pos);
    }

    Token new23(int line, int pos) {
        return new TSin(line, pos);
    }

    Token new24(int line, int pos) {
        return new TTan(line, pos);
    }

    Token new25(int line, int pos) {
        return new TAcos(line, pos);
    }

    Token new26(int line, int pos) {
        return new TAsin(line, pos);
    }

    Token new27(int line, int pos) {
        return new TAtan(line, pos);
    }

    Token new28(int line, int pos) {
        return new TSqrt(line, pos);
    }

    Token new29(int line, int pos) {
        return new TExp(line, pos);
    }

    Token new30(int line, int pos) {
        return new TLog(line, pos);
    }

    Token new31(int line, int pos) {
        return new TFloor(line, pos);
    }

    Token new32(int line, int pos) {
        return new TCeil(line, pos);
    }

    Token new33(int line, int pos) {
        return new TAbs(line, pos);
    }

    Token new34(int line, int pos) {
        return new TBoolean(line, pos);
    }

    Token new35(int line, int pos) {
        return new TString(line, pos);
    }

    Token new36(int line, int pos) {
        return new TByte(line, pos);
    }

    Token new37(int line, int pos) {
        return new TInt(line, pos);
    }

    Token new38(int line, int pos) {
        return new TDouble(line, pos);
    }

    Token new39(int line, int pos) {
        return new TFloat(line, pos);
    }

    Token new40(int line, int pos) {
        return new TLong(line, pos);
    }

    Token new41(int line, int pos) {
        return new TShort(line, pos);
    }

    Token new42(int line, int pos) {
        return new TTrue(line, pos);
    }

    Token new43(int line, int pos) {
        return new TFalse(line, pos);
    }

    Token new44(int line, int pos) {
        return new TLParenthese(line, pos);
    }

    Token new45(int line, int pos) {
        return new TRParenthese(line, pos);
    }

    Token new46(String text, int line, int pos) {
        return new TIntegerLiteral(text, line, pos);
    }

    Token new47(String text, int line, int pos) {
        return new TFloatingPointLiteral(text, line, pos);
    }

    Token new48(String text, int line, int pos) {
        return new TStringLiteral(text, line, pos);
    }

    Token new49(String text, int line, int pos) {
        return new TIdentifier(text, line, pos);
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

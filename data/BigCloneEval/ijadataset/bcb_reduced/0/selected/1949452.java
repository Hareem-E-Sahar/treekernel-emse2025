package net.sourceforge.jdefprog.mcl.generated.lexer;

import java.io.*;
import net.sourceforge.jdefprog.mcl.generated.node.*;

@SuppressWarnings("nls")
public class Lexer {

    protected Token token;

    protected State state = State.INITIAL_STATE;

    private PushbackReader in;

    private int line;

    private int pos;

    private boolean cr;

    private boolean eof;

    private final StringBuffer text = new StringBuffer();

    @SuppressWarnings("unused")
    protected void filter() throws LexerException, IOException {
    }

    public Lexer(@SuppressWarnings("hiding") PushbackReader in) {
        this.in = in;
    }

    public Token peek() throws LexerException, IOException {
        while (this.token == null) {
            this.token = getToken();
            filter();
        }
        return this.token;
    }

    public Token next() throws LexerException, IOException {
        while (this.token == null) {
            this.token = getToken();
            filter();
        }
        Token result = this.token;
        this.token = null;
        return result;
    }

    protected Token getToken() throws IOException, LexerException {
        int dfa_state = 0;
        int start_pos = this.pos;
        int start_line = this.line;
        int accept_state = -1;
        int accept_token = -1;
        int accept_length = -1;
        int accept_pos = -1;
        int accept_line = -1;
        @SuppressWarnings("hiding") int[][][] gotoTable = Lexer.gotoTable[this.state.id()];
        @SuppressWarnings("hiding") int[] accept = Lexer.accept[this.state.id()];
        this.text.setLength(0);
        while (true) {
            int c = getChar();
            if (c != -1) {
                switch(c) {
                    case 10:
                        if (this.cr) {
                            this.cr = false;
                        } else {
                            this.line++;
                            this.pos = 0;
                        }
                        break;
                    case 13:
                        this.line++;
                        this.pos = 0;
                        this.cr = true;
                        break;
                    default:
                        this.pos++;
                        this.cr = false;
                        break;
                }
                this.text.append((char) c);
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
                    accept_length = this.text.length();
                    accept_pos = this.pos;
                    accept_line = this.line;
                }
            } else {
                if (accept_state != -1) {
                    switch(accept_token) {
                        case 0:
                            {
                                @SuppressWarnings("hiding") Token token = new0(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 1:
                            {
                                @SuppressWarnings("hiding") Token token = new1(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 2:
                            {
                                @SuppressWarnings("hiding") Token token = new2(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 3:
                            {
                                @SuppressWarnings("hiding") Token token = new3(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 4:
                            {
                                @SuppressWarnings("hiding") Token token = new4(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 5:
                            {
                                @SuppressWarnings("hiding") Token token = new5(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 6:
                            {
                                @SuppressWarnings("hiding") Token token = new6(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 7:
                            {
                                @SuppressWarnings("hiding") Token token = new7(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 8:
                            {
                                @SuppressWarnings("hiding") Token token = new8(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 9:
                            {
                                @SuppressWarnings("hiding") Token token = new9(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 10:
                            {
                                @SuppressWarnings("hiding") Token token = new10(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 11:
                            {
                                @SuppressWarnings("hiding") Token token = new11(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 12:
                            {
                                @SuppressWarnings("hiding") Token token = new12(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 13:
                            {
                                @SuppressWarnings("hiding") Token token = new13(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 14:
                            {
                                @SuppressWarnings("hiding") Token token = new14(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 15:
                            {
                                @SuppressWarnings("hiding") Token token = new15(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 16:
                            {
                                @SuppressWarnings("hiding") Token token = new16(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 17:
                            {
                                @SuppressWarnings("hiding") Token token = new17(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 18:
                            {
                                @SuppressWarnings("hiding") Token token = new18(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 19:
                            {
                                @SuppressWarnings("hiding") Token token = new19(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 20:
                            {
                                @SuppressWarnings("hiding") Token token = new20(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 21:
                            {
                                @SuppressWarnings("hiding") Token token = new21(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                        case 22:
                            {
                                @SuppressWarnings("hiding") Token token = new22(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.INITIAL_STATE;
                                        break;
                                }
                                return token;
                            }
                    }
                } else {
                    if (this.text.length() > 0) {
                        throw new LexerException("[" + (start_line + 1) + "," + (start_pos + 1) + "]" + " Unknown token: " + this.text);
                    }
                    @SuppressWarnings("hiding") EOF token = new EOF(start_line + 1, start_pos + 1);
                    return token;
                }
            }
        }
    }

    Token new0(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNewLine(text, line, pos);
    }

    Token new1(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBlank(text, line, pos);
    }

    Token new2(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TOpenParen(line, pos);
    }

    Token new3(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCloseParen(line, pos);
    }

    Token new4(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDot(line, pos);
    }

    Token new5(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TComma(line, pos);
    }

    Token new6(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSemi(line, pos);
    }

    Token new7(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIntConst(text, line, pos);
    }

    Token new8(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLongConst(text, line, pos);
    }

    Token new9(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAop(text, line, pos);
    }

    Token new10(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TMop(text, line, pos);
    }

    Token new11(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEqualop(text, line, pos);
    }

    Token new12(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TRelop(text, line, pos);
    }

    Token new13(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLogicop(text, line, pos);
    }

    Token new14(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNegation(line, pos);
    }

    Token new15(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TThis(line, pos);
    }

    Token new16(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TKwNull(line, pos);
    }

    Token new17(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TResult(line, pos);
    }

    Token new18(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParam(text, line, pos);
    }

    Token new19(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TOld(line, pos);
    }

    Token new20(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPrimitiveType(text, line, pos);
    }

    Token new21(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBooleanConst(text, line, pos);
    }

    Token new22(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIdent(text, line, pos);
    }

    private int getChar() throws IOException {
        if (this.eof) {
            return -1;
        }
        int result = this.in.read();
        if (result == -1) {
            this.eof = true;
        }
        return result;
    }

    private void pushBack(int acceptLength) throws IOException {
        int length = this.text.length();
        for (int i = length - 1; i >= acceptLength; i--) {
            this.eof = false;
            this.in.unread(this.text.charAt(i));
        }
    }

    protected void unread(@SuppressWarnings("hiding") Token token) throws IOException {
        @SuppressWarnings("hiding") String text = token.getText();
        int length = text.length();
        for (int i = length - 1; i >= 0; i--) {
            this.eof = false;
            this.in.unread(text.charAt(i));
        }
        this.pos = token.getPos() - 1;
        this.line = token.getLine() - 1;
    }

    private String getText(int acceptLength) {
        StringBuffer s = new StringBuffer(acceptLength);
        for (int i = 0; i < acceptLength; i++) {
            s.append(this.text.charAt(i));
        }
        return s.toString();
    }

    private static int[][][][] gotoTable = { { { { 9, 9, 1 }, { 10, 10, 2 }, { 13, 13, 3 }, { 32, 32, 4 }, { 33, 33, 5 }, { 35, 35, 6 }, { 38, 38, 7 }, { 40, 40, 8 }, { 41, 41, 9 }, { 42, 42, 10 }, { 43, 43, 11 }, { 44, 44, 12 }, { 45, 45, 13 }, { 46, 46, 14 }, { 47, 47, 15 }, { 48, 57, 16 }, { 59, 59, 17 }, { 60, 60, 18 }, { 61, 61, 19 }, { 62, 62, 20 }, { 65, 90, 21 }, { 97, 97, 22 }, { 98, 98, 23 }, { 99, 99, 24 }, { 100, 100, 25 }, { 101, 101, 22 }, { 102, 102, 26 }, { 103, 104, 22 }, { 105, 105, 27 }, { 106, 107, 22 }, { 108, 108, 28 }, { 109, 109, 22 }, { 110, 110, 29 }, { 111, 114, 22 }, { 115, 115, 30 }, { 116, 116, 31 }, { 117, 122, 22 }, { 124, 124, 32 } }, { { 9, 9, 1 }, { 32, 32, 4 } }, {}, { { 10, 10, 33 } }, { { 9, 32, -3 } }, { { 61, 61, 34 } }, { { 111, 111, 35 }, { 112, 112, 36 }, { 114, 114, 37 } }, { { 38, 38, 38 } }, {}, {}, {}, {}, {}, { { 62, 62, 39 } }, {}, {}, { { 48, 57, 16 }, { 76, 76, 40 }, { 108, 108, 41 } }, {}, { { 61, 61, 42 } }, { { 61, 61, 43 } }, { { 61, 61, 44 } }, { { 48, 57, 45 }, { 65, 90, 46 }, { 95, 95, 47 }, { 97, 122, 48 } }, { { 48, 122, -23 } }, { { 48, 95, -23 }, { 97, 120, 48 }, { 121, 121, 49 }, { 122, 122, 48 } }, { { 48, 95, -23 }, { 97, 103, 48 }, { 104, 104, 50 }, { 105, 122, 48 } }, { { 48, 95, -23 }, { 97, 110, 48 }, { 111, 111, 51 }, { 112, 122, 48 } }, { { 48, 95, -23 }, { 97, 97, 52 }, { 98, 107, 48 }, { 108, 108, 53 }, { 109, 122, 48 } }, { { 48, 95, -23 }, { 97, 109, 48 }, { 110, 110, 54 }, { 111, 122, 48 } }, { { 48, 110, -27 }, { 111, 111, 55 }, { 112, 122, 48 } }, { { 48, 95, -23 }, { 97, 116, 48 }, { 117, 117, 56 }, { 118, 122, 48 } }, { { 48, 103, -26 }, { 104, 104, 57 }, { 105, 122, 48 } }, { { 48, 103, -26 }, { 104, 104, 58 }, { 105, 113, 48 }, { 114, 114, 59 }, { 115, 122, 48 } }, { { 124, 124, 60 } }, {}, {}, { { 108, 108, 61 } }, { { 97, 97, 62 } }, { { 101, 101, 63 } }, {}, {}, {}, {}, {}, {}, {}, { { 48, 122, -23 } }, { { 48, 122, -23 } }, { { 48, 57, 64 }, { 65, 90, 65 }, { 97, 122, 66 } }, { { 48, 122, -23 } }, { { 48, 95, -23 }, { 97, 115, 48 }, { 116, 116, 67 }, { 117, 122, 48 } }, { { 48, 95, -23 }, { 97, 97, 68 }, { 98, 122, 48 } }, { { 48, 116, -31 }, { 117, 117, 69 }, { 118, 122, 48 } }, { { 48, 95, -23 }, { 97, 107, 48 }, { 108, 108, 70 }, { 109, 122, 48 } }, { { 48, 110, -27 }, { 111, 111, 71 }, { 112, 122, 48 } }, { { 48, 115, -51 }, { 116, 116, 72 }, { 117, 122, 48 } }, { { 48, 109, -29 }, { 110, 110, 73 }, { 111, 122, 48 } }, { { 48, 107, -54 }, { 108, 108, 74 }, { 109, 122, 48 } }, { { 48, 110, -27 }, { 111, 111, 75 }, { 112, 122, 48 } }, { { 48, 95, -23 }, { 97, 104, 48 }, { 105, 105, 76 }, { 106, 122, 48 } }, { { 48, 116, -31 }, { 117, 117, 77 }, { 118, 122, 48 } }, {}, { { 100, 100, 78 } }, { { 114, 114, 79 } }, { { 115, 115, 80 } }, { { 48, 122, -23 } }, { { 48, 122, -23 } }, { { 48, 122, -23 } }, { { 48, 95, -23 }, { 97, 100, 48 }, { 101, 101, 81 }, { 102, 122, 48 } }, { { 48, 95, -23 }, { 97, 113, 48 }, { 114, 114, 82 }, { 115, 122, 48 } }, { { 48, 95, -23 }, { 97, 97, 48 }, { 98, 98, 83 }, { 99, 122, 48 } }, { { 48, 95, -23 }, { 97, 114, 48 }, { 115, 115, 84 }, { 116, 122, 48 } }, { { 48, 95, -23 }, { 97, 97, 85 }, { 98, 122, 48 } }, { { 48, 122, -23 } }, { { 48, 95, -23 }, { 97, 102, 48 }, { 103, 103, 86 }, { 104, 122, 48 } }, { { 48, 107, -54 }, { 108, 108, 87 }, { 109, 122, 48 } }, { { 48, 113, -70 }, { 114, 114, 88 }, { 115, 122, 48 } }, { { 48, 114, -72 }, { 115, 115, 89 }, { 116, 122, 48 } }, { { 48, 100, -69 }, { 101, 101, 90 }, { 102, 122, 48 } }, {}, { { 97, 97, 91 } }, { { 117, 117, 92 } }, { { 48, 122, -23 } }, { { 48, 122, -23 } }, { { 48, 107, -54 }, { 108, 108, 93 }, { 109, 122, 48 } }, { { 48, 100, -69 }, { 101, 101, 94 }, { 102, 122, 48 } }, { { 48, 115, -51 }, { 116, 116, 95 }, { 117, 122, 48 } }, { { 48, 122, -23 } }, { { 48, 122, -23 } }, { { 48, 115, -51 }, { 116, 116, 96 }, { 117, 122, 48 } }, { { 48, 122, -23 } }, { { 48, 122, -23 } }, { { 109, 109, 97 } }, { { 108, 108, 98 } }, { { 48, 100, -69 }, { 101, 101, 99 }, { 102, 122, 48 } }, { { 48, 122, -23 } }, { { 48, 122, -23 } }, { { 48, 122, -23 } }, { { 49, 57, 100 } }, { { 116, 116, 101 } }, { { 48, 122, -23 } }, { { 48, 57, 102 } }, {}, { { 48, 57, 102 } } } };

    private static int[][] accept = { { -1, 1, 0, 0, 1, 14, -1, -1, 2, 3, 10, 9, 5, 9, 4, 10, 7, 6, 12, -1, 12, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, -1, 0, 11, -1, -1, -1, 13, 13, 8, 8, 12, 11, 12, 22, 22, -1, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 13, -1, -1, -1, 22, 22, 22, 22, 22, 22, 22, 22, 20, 22, 22, 22, 22, 22, 19, -1, -1, 20, 20, 22, 22, 22, 20, 16, 22, 15, 21, -1, -1, 22, 21, 20, 20, -1, -1, 20, 18, 17, 18 } };

    public static class State {

        public static final State INITIAL_STATE = new State(0);

        private int id;

        private State(@SuppressWarnings("hiding") int id) {
            this.id = id;
        }

        public int id() {
            return this.id;
        }
    }
}

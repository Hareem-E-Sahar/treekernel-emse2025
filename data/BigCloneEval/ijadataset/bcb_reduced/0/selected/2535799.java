package org.sablecc.sablecc.intermediate.syntax3.lexer;

import java.io.*;
import org.sablecc.sablecc.intermediate.syntax3.node.*;

@SuppressWarnings("nls")
public class Lexer {

    protected Token token;

    protected State state = State.INITIAL;

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
                                @SuppressWarnings("hiding") Token token = new0(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 1:
                            {
                                @SuppressWarnings("hiding") Token token = new1(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 2:
                            {
                                @SuppressWarnings("hiding") Token token = new2(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 3:
                            {
                                @SuppressWarnings("hiding") Token token = new3(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 4:
                            {
                                @SuppressWarnings("hiding") Token token = new4(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 5:
                            {
                                @SuppressWarnings("hiding") Token token = new5(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 6:
                            {
                                @SuppressWarnings("hiding") Token token = new6(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 7:
                            {
                                @SuppressWarnings("hiding") Token token = new7(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 8:
                            {
                                @SuppressWarnings("hiding") Token token = new8(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 9:
                            {
                                @SuppressWarnings("hiding") Token token = new9(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 10:
                            {
                                @SuppressWarnings("hiding") Token token = new10(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 11:
                            {
                                @SuppressWarnings("hiding") Token token = new11(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 12:
                            {
                                @SuppressWarnings("hiding") Token token = new12(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 13:
                            {
                                @SuppressWarnings("hiding") Token token = new13(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 14:
                            {
                                @SuppressWarnings("hiding") Token token = new14(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 15:
                            {
                                @SuppressWarnings("hiding") Token token = new15(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 16:
                            {
                                @SuppressWarnings("hiding") Token token = new16(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 17:
                            {
                                @SuppressWarnings("hiding") Token token = new17(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 18:
                            {
                                @SuppressWarnings("hiding") Token token = new18(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 19:
                            {
                                @SuppressWarnings("hiding") Token token = new19(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 20:
                            {
                                @SuppressWarnings("hiding") Token token = new20(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 21:
                            {
                                @SuppressWarnings("hiding") Token token = new21(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 22:
                            {
                                @SuppressWarnings("hiding") Token token = new22(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 23:
                            {
                                @SuppressWarnings("hiding") Token token = new23(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 24:
                            {
                                @SuppressWarnings("hiding") Token token = new24(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 25:
                            {
                                @SuppressWarnings("hiding") Token token = new25(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 26:
                            {
                                @SuppressWarnings("hiding") Token token = new26(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 27:
                            {
                                @SuppressWarnings("hiding") Token token = new27(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 28:
                            {
                                @SuppressWarnings("hiding") Token token = new28(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 29:
                            {
                                @SuppressWarnings("hiding") Token token = new29(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 30:
                            {
                                @SuppressWarnings("hiding") Token token = new30(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 31:
                            {
                                @SuppressWarnings("hiding") Token token = new31(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 32:
                            {
                                @SuppressWarnings("hiding") Token token = new32(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 33:
                            {
                                @SuppressWarnings("hiding") Token token = new33(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 34:
                            {
                                @SuppressWarnings("hiding") Token token = new34(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 35:
                            {
                                @SuppressWarnings("hiding") Token token = new35(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 36:
                            {
                                @SuppressWarnings("hiding") Token token = new36(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 37:
                            {
                                @SuppressWarnings("hiding") Token token = new37(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 38:
                            {
                                @SuppressWarnings("hiding") Token token = new38(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 39:
                            {
                                @SuppressWarnings("hiding") Token token = new39(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 40:
                            {
                                @SuppressWarnings("hiding") Token token = new40(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 41:
                            {
                                @SuppressWarnings("hiding") Token token = new41(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 42:
                            {
                                @SuppressWarnings("hiding") Token token = new42(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 43:
                            {
                                @SuppressWarnings("hiding") Token token = new43(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 44:
                            {
                                @SuppressWarnings("hiding") Token token = new44(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 45:
                            {
                                @SuppressWarnings("hiding") Token token = new45(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 46:
                            {
                                @SuppressWarnings("hiding") Token token = new46(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 47:
                            {
                                @SuppressWarnings("hiding") Token token = new47(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 48:
                            {
                                @SuppressWarnings("hiding") Token token = new48(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 49:
                            {
                                @SuppressWarnings("hiding") Token token = new49(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 50:
                            {
                                @SuppressWarnings("hiding") Token token = new50(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 51:
                            {
                                @SuppressWarnings("hiding") Token token = new51(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 52:
                            {
                                @SuppressWarnings("hiding") Token token = new52(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 53:
                            {
                                @SuppressWarnings("hiding") Token token = new53(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 54:
                            {
                                @SuppressWarnings("hiding") Token token = new54(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 55:
                            {
                                @SuppressWarnings("hiding") Token token = new55(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 56:
                            {
                                @SuppressWarnings("hiding") Token token = new56(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 57:
                            {
                                @SuppressWarnings("hiding") Token token = new57(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 58:
                            {
                                @SuppressWarnings("hiding") Token token = new58(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 59:
                            {
                                @SuppressWarnings("hiding") Token token = new59(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 60:
                            {
                                @SuppressWarnings("hiding") Token token = new60(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 61:
                            {
                                @SuppressWarnings("hiding") Token token = new61(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 62:
                            {
                                @SuppressWarnings("hiding") Token token = new62(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 63:
                            {
                                @SuppressWarnings("hiding") Token token = new63(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 64:
                            {
                                @SuppressWarnings("hiding") Token token = new64(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 65:
                            {
                                @SuppressWarnings("hiding") Token token = new65(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 66:
                            {
                                @SuppressWarnings("hiding") Token token = new66(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 67:
                            {
                                @SuppressWarnings("hiding") Token token = new67(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 68:
                            {
                                @SuppressWarnings("hiding") Token token = new68(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 69:
                            {
                                @SuppressWarnings("hiding") Token token = new69(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 70:
                            {
                                @SuppressWarnings("hiding") Token token = new70(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 71:
                            {
                                @SuppressWarnings("hiding") Token token = new71(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 72:
                            {
                                @SuppressWarnings("hiding") Token token = new72(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 73:
                            {
                                @SuppressWarnings("hiding") Token token = new73(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 74:
                            {
                                @SuppressWarnings("hiding") Token token = new74(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 75:
                            {
                                @SuppressWarnings("hiding") Token token = new75(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 76:
                            {
                                @SuppressWarnings("hiding") Token token = new76(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 77:
                            {
                                @SuppressWarnings("hiding") Token token = new77(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 78:
                            {
                                @SuppressWarnings("hiding") Token token = new78(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 79:
                            {
                                @SuppressWarnings("hiding") Token token = new79(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 80:
                            {
                                @SuppressWarnings("hiding") Token token = new80(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 81:
                            {
                                @SuppressWarnings("hiding") Token token = new81(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 82:
                            {
                                @SuppressWarnings("hiding") Token token = new82(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 83:
                            {
                                @SuppressWarnings("hiding") Token token = new83(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 84:
                            {
                                @SuppressWarnings("hiding") Token token = new84(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
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

    Token new0(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAcceptKeyword(line, pos);
    }

    Token new1(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAlphabetKeyword(line, pos);
    }

    Token new2(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAlternativeKeyword(line, pos);
    }

    Token new3(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TArgumentNameKeyword(line, pos);
    }

    Token new4(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAtLeastTypeKeyword(line, pos);
    }

    Token new5(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAtLeastValueKeyword(line, pos);
    }

    Token new6(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBackCountKeyword(line, pos);
    }

    Token new7(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TChoiceKeyword(line, pos);
    }

    Token new8(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TConditionKeyword(line, pos);
    }

    Token new9(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TContextNameKeyword(line, pos);
    }

    Token new10(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDepthKeyword(line, pos);
    }

    Token new11(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TElementKeyword(line, pos);
    }

    Token new12(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEndExpectedKeyword(line, pos);
    }

    Token new13(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TExpectedSelectionKeyword(line, pos);
    }

    Token new14(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFalseKeyword(line, pos);
    }

    Token new15(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFromKeyword(line, pos);
    }

    Token new16(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TGroupKeyword(line, pos);
    }

    Token new17(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TGroupsKeyword(line, pos);
    }

    Token new18(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIgnoreKeyword(line, pos);
    }

    Token new19(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIntervalKeyword(line, pos);
    }

    Token new20(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIntervalTypeKeyword(line, pos);
    }

    Token new21(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIntervalValueKeyword(line, pos);
    }

    Token new22(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TInvestigatorNameKeyword(line, pos);
    }

    Token new23(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TInvestigatorsKeyword(line, pos);
    }

    Token new24(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLanguageKeyword(line, pos);
    }

    Token new25(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLexerKeyword(line, pos);
    }

    Token new26(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLexerAutomatonKeyword(line, pos);
    }

    Token new27(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLexerStateKeyword(line, pos);
    }

    Token new28(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLexerTransitionKeyword(line, pos);
    }

    Token new29(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TListReferenceValueKeyword(line, pos);
    }

    Token new30(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLookDepthKeyword(line, pos);
    }

    Token new31(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLookSymbolKeyword(line, pos);
    }

    Token new32(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TMarkerKeyword(line, pos);
    }

    Token new33(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNameKeyword(line, pos);
    }

    Token new34(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNewValueKeyword(line, pos);
    }

    Token new35(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNullValueKeyword(line, pos);
    }

    Token new36(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNumberTypeKeyword(line, pos);
    }

    Token new37(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNumberValueKeyword(line, pos);
    }

    Token new38(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TOneKeyword(line, pos);
    }

    Token new39(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParseKeyword(line, pos);
    }

    Token new40(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParseAlternativeKeyword(line, pos);
    }

    Token new41(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParseAlternativeNameKeyword(line, pos);
    }

    Token new42(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParseElementKeyword(line, pos);
    }

    Token new43(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParseElementNameKeyword(line, pos);
    }

    Token new44(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParseProductionKeyword(line, pos);
    }

    Token new45(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParseProductionNameKeyword(line, pos);
    }

    Token new46(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParserKeyword(line, pos);
    }

    Token new47(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParserInvestigatorKeyword(line, pos);
    }

    Token new48(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParserSelectorKeyword(line, pos);
    }

    Token new49(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParserStateKeyword(line, pos);
    }

    Token new50(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TProductionKeyword(line, pos);
    }

    Token new51(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TProductionTransitionKeyword(line, pos);
    }

    Token new52(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPublicKeyword(line, pos);
    }

    Token new53(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TReduceActionKeyword(line, pos);
    }

    Token new54(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TReferenceValueKeyword(line, pos);
    }

    Token new55(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TRejectKeyword(line, pos);
    }

    Token new56(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TRetainedElementKeyword(line, pos);
    }

    Token new57(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TRetainedElementNameKeyword(line, pos);
    }

    Token new58(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TShiftActionKeyword(line, pos);
    }

    Token new59(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStartNameKeyword(line, pos);
    }

    Token new60(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSelectionActionKeyword(line, pos);
    }

    Token new61(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSelectorNameKeyword(line, pos);
    }

    Token new62(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSeparatorKeyword(line, pos);
    }

    Token new63(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStartKeyword(line, pos);
    }

    Token new64(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSymbolKeyword(line, pos);
    }

    Token new65(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSymbolNameKeyword(line, pos);
    }

    Token new66(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTargetNameKeyword(line, pos);
    }

    Token new67(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TToKeyword(line, pos);
    }

    Token new68(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTokenKeyword(line, pos);
    }

    Token new69(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTokenActionKeyword(line, pos);
    }

    Token new70(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTokenNameKeyword(line, pos);
    }

    Token new71(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTokenTransitionKeyword(line, pos);
    }

    Token new72(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTokensKeyword(line, pos);
    }

    Token new73(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTransformationKeyword(line, pos);
    }

    Token new74(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTreeActionKeyword(line, pos);
    }

    Token new75(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTrueKeyword(line, pos);
    }

    Token new76(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TZeroOrOneKeyword(line, pos);
    }

    Token new77(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAssign(line, pos);
    }

    Token new78(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSemicolon(line, pos);
    }

    Token new79(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLBrace(line, pos);
    }

    Token new80(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TRBrace(line, pos);
    }

    Token new81(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TString(text, line, pos);
    }

    Token new82(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNumber(text, line, pos);
    }

    Token new83(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBlank(text, line, pos);
    }

    Token new84(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TInvalidCharacter(text, line, pos);
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

    private static int[][][][] gotoTable;

    private static int[][] accept;

    public static class State {

        public static final State INITIAL = new State(0);

        private int id;

        private State(@SuppressWarnings("hiding") int id) {
            this.id = id;
        }

        public int id() {
            return this.id;
        }
    }

    static {
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

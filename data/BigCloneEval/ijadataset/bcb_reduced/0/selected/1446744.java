package ru.amse.baltijsky.javascheme.importer.sablecc.java15.lexer;

import ru.amse.baltijsky.javascheme.importer.sablecc.java15.node.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PushbackReader;

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
                                @SuppressWarnings("hiding") Token token = new0(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 1:
                            {
                                @SuppressWarnings("hiding") Token token = new1(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 2:
                            {
                                @SuppressWarnings("hiding") Token token = new2(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 3:
                            {
                                @SuppressWarnings("hiding") Token token = new3(getText(accept_length), start_line + 1, start_pos + 1);
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
                                @SuppressWarnings("hiding") Token token = new81(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 82:
                            {
                                @SuppressWarnings("hiding") Token token = new82(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 83:
                            {
                                @SuppressWarnings("hiding") Token token = new83(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 84:
                            {
                                @SuppressWarnings("hiding") Token token = new84(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 85:
                            {
                                @SuppressWarnings("hiding") Token token = new85(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 86:
                            {
                                @SuppressWarnings("hiding") Token token = new86(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 87:
                            {
                                @SuppressWarnings("hiding") Token token = new87(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 88:
                            {
                                @SuppressWarnings("hiding") Token token = new88(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 89:
                            {
                                @SuppressWarnings("hiding") Token token = new89(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 90:
                            {
                                @SuppressWarnings("hiding") Token token = new90(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 91:
                            {
                                @SuppressWarnings("hiding") Token token = new91(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 92:
                            {
                                @SuppressWarnings("hiding") Token token = new92(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 93:
                            {
                                @SuppressWarnings("hiding") Token token = new93(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 94:
                            {
                                @SuppressWarnings("hiding") Token token = new94(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 95:
                            {
                                @SuppressWarnings("hiding") Token token = new95(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 96:
                            {
                                @SuppressWarnings("hiding") Token token = new96(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 97:
                            {
                                @SuppressWarnings("hiding") Token token = new97(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 98:
                            {
                                @SuppressWarnings("hiding") Token token = new98(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 99:
                            {
                                @SuppressWarnings("hiding") Token token = new99(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 100:
                            {
                                @SuppressWarnings("hiding") Token token = new100(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 101:
                            {
                                @SuppressWarnings("hiding") Token token = new101(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 102:
                            {
                                @SuppressWarnings("hiding") Token token = new102(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 103:
                            {
                                @SuppressWarnings("hiding") Token token = new103(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 104:
                            {
                                @SuppressWarnings("hiding") Token token = new104(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 105:
                            {
                                @SuppressWarnings("hiding") Token token = new105(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 106:
                            {
                                @SuppressWarnings("hiding") Token token = new106(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 107:
                            {
                                @SuppressWarnings("hiding") Token token = new107(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                return token;
                            }
                        case 108:
                            {
                                @SuppressWarnings("hiding") Token token = new108(getText(accept_length), start_line + 1, start_pos + 1);
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

    Token new0(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TWhiteSpace(text, line, pos);
    }

    Token new1(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTraditionalComment(text, line, pos);
    }

    Token new2(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDocumentationComment(text, line, pos);
    }

    Token new3(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEndOfLineComment(text, line, pos);
    }

    Token new4(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAssert(line, pos);
    }

    Token new5(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAbstract(line, pos);
    }

    Token new6(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBoolean(line, pos);
    }

    Token new7(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBreak(line, pos);
    }

    Token new8(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TByte(line, pos);
    }

    Token new9(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCase(line, pos);
    }

    Token new10(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCatch(line, pos);
    }

    Token new11(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TChar(line, pos);
    }

    Token new12(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TClazz(line, pos);
    }

    Token new13(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TConst(line, pos);
    }

    Token new14(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TContinue(line, pos);
    }

    Token new15(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDefault(line, pos);
    }

    Token new16(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDo(line, pos);
    }

    Token new17(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDouble(line, pos);
    }

    Token new18(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TElse(line, pos);
    }

    Token new19(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEnum(line, pos);
    }

    Token new20(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TExtends(line, pos);
    }

    Token new21(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFinal(line, pos);
    }

    Token new22(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFinally(line, pos);
    }

    Token new23(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFloat(line, pos);
    }

    Token new24(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFor(line, pos);
    }

    Token new25(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TGoto(line, pos);
    }

    Token new26(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIf(line, pos);
    }

    Token new27(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TImplements(line, pos);
    }

    Token new28(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TImport(line, pos);
    }

    Token new29(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TInstanceof(line, pos);
    }

    Token new30(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TInt(line, pos);
    }

    Token new31(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TInterface(line, pos);
    }

    Token new32(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLong(line, pos);
    }

    Token new33(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNative(line, pos);
    }

    Token new34(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNew(line, pos);
    }

    Token new35(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPackage(line, pos);
    }

    Token new36(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPrivate(line, pos);
    }

    Token new37(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TProtected(line, pos);
    }

    Token new38(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPublic(line, pos);
    }

    Token new39(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TReturn(line, pos);
    }

    Token new40(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TShort(line, pos);
    }

    Token new41(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStatic(line, pos);
    }

    Token new42(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStrictfp(line, pos);
    }

    Token new43(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSuper(line, pos);
    }

    Token new44(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSwitch(line, pos);
    }

    Token new45(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSynchronized(line, pos);
    }

    Token new46(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TThis(line, pos);
    }

    Token new47(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TThrow(line, pos);
    }

    Token new48(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TThrows(line, pos);
    }

    Token new49(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTransient(line, pos);
    }

    Token new50(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTry(line, pos);
    }

    Token new51(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TVoid(line, pos);
    }

    Token new52(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TVolatile(line, pos);
    }

    Token new53(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TWhile(line, pos);
    }

    Token new54(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLPar(line, pos);
    }

    Token new55(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TRPar(line, pos);
    }

    Token new56(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLBrc(line, pos);
    }

    Token new57(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TRBrc(line, pos);
    }

    Token new58(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLBrk(line, pos);
    }

    Token new59(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TRBrk(line, pos);
    }

    Token new60(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSemicolon(line, pos);
    }

    Token new61(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TComma(line, pos);
    }

    Token new62(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDot(line, pos);
    }

    Token new63(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAssign(line, pos);
    }

    Token new64(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLt(line, pos);
    }

    Token new65(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TGt(line, pos);
    }

    Token new66(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNot(line, pos);
    }

    Token new67(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TComplement(line, pos);
    }

    Token new68(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TQuestion(line, pos);
    }

    Token new69(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TColon(line, pos);
    }

    Token new70(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEllipsis(line, pos);
    }

    Token new71(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAt(line, pos);
    }

    Token new72(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEq(line, pos);
    }

    Token new73(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLtEq(line, pos);
    }

    Token new74(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TGtEq(line, pos);
    }

    Token new75(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNeq(line, pos);
    }

    Token new76(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAndAnd(line, pos);
    }

    Token new77(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TOrOr(line, pos);
    }

    Token new78(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPlusPlus(line, pos);
    }

    Token new79(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TMinusMinus(line, pos);
    }

    Token new80(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPlus(line, pos);
    }

    Token new81(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TMinus(line, pos);
    }

    Token new82(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStar(line, pos);
    }

    Token new83(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDiv(line, pos);
    }

    Token new84(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAnd(line, pos);
    }

    Token new85(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TOr(line, pos);
    }

    Token new86(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TXor(line, pos);
    }

    Token new87(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TMod(line, pos);
    }

    Token new88(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TShl(line, pos);
    }

    Token new89(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSshr(line, pos);
    }

    Token new90(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TUshr(line, pos);
    }

    Token new91(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPlusAssign(line, pos);
    }

    Token new92(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TMinusAssign(line, pos);
    }

    Token new93(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStarAssign(line, pos);
    }

    Token new94(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDivAssign(line, pos);
    }

    Token new95(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAndAssign(line, pos);
    }

    Token new96(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TOrAssign(line, pos);
    }

    Token new97(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TXorAssign(line, pos);
    }

    Token new98(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TModAssign(line, pos);
    }

    Token new99(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TShlAssign(line, pos);
    }

    Token new100(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSshrAssign(line, pos);
    }

    Token new101(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TUshrAssign(line, pos);
    }

    Token new102(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFloatingPointLiteral(text, line, pos);
    }

    Token new103(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCharacterLiteral(text, line, pos);
    }

    Token new104(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStringLiteral(text, line, pos);
    }

    Token new105(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBooleanLiteral(text, line, pos);
    }

    Token new106(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNullLiteral(text, line, pos);
    }

    Token new107(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIntegerLiteral(text, line, pos);
    }

    Token new108(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIdentifier(text, line, pos);
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

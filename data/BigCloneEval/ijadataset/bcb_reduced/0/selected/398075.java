package com.google.clearsilver.jsilver.syntax.lexer;

import java.io.*;
import com.google.clearsilver.jsilver.syntax.node.*;

@SuppressWarnings("nls")
public class Lexer {

    protected Token token;

    protected State state = State.CONTENT;

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
                                        state = State.CONTENT;
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
                                    case 3:
                                        state = State.COMMENT;
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
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 7:
                            {
                                @SuppressWarnings("hiding") Token token = new7(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 9:
                            {
                                @SuppressWarnings("hiding") Token token = new9(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 10:
                            {
                                @SuppressWarnings("hiding") Token token = new10(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 11:
                            {
                                @SuppressWarnings("hiding") Token token = new11(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 12:
                            {
                                @SuppressWarnings("hiding") Token token = new12(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 13:
                            {
                                @SuppressWarnings("hiding") Token token = new13(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 18:
                            {
                                @SuppressWarnings("hiding") Token token = new18(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
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
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 20:
                            {
                                @SuppressWarnings("hiding") Token token = new20(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 21:
                            {
                                @SuppressWarnings("hiding") Token token = new21(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 22:
                            {
                                @SuppressWarnings("hiding") Token token = new22(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 23:
                            {
                                @SuppressWarnings("hiding") Token token = new23(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 24:
                            {
                                @SuppressWarnings("hiding") Token token = new24(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 25:
                            {
                                @SuppressWarnings("hiding") Token token = new25(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 26:
                            {
                                @SuppressWarnings("hiding") Token token = new26(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 27:
                            {
                                @SuppressWarnings("hiding") Token token = new27(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 28:
                            {
                                @SuppressWarnings("hiding") Token token = new28(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 29:
                            {
                                @SuppressWarnings("hiding") Token token = new29(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 30:
                            {
                                @SuppressWarnings("hiding") Token token = new30(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 31:
                            {
                                @SuppressWarnings("hiding") Token token = new31(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 32:
                            {
                                @SuppressWarnings("hiding") Token token = new32(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 33:
                            {
                                @SuppressWarnings("hiding") Token token = new33(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 34:
                            {
                                @SuppressWarnings("hiding") Token token = new34(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 35:
                            {
                                @SuppressWarnings("hiding") Token token = new35(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 36:
                            {
                                @SuppressWarnings("hiding") Token token = new36(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 37:
                            {
                                @SuppressWarnings("hiding") Token token = new37(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 38:
                            {
                                @SuppressWarnings("hiding") Token token = new38(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 39:
                            {
                                @SuppressWarnings("hiding") Token token = new39(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 40:
                            {
                                @SuppressWarnings("hiding") Token token = new40(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 41:
                            {
                                @SuppressWarnings("hiding") Token token = new41(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 42:
                            {
                                @SuppressWarnings("hiding") Token token = new42(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 43:
                            {
                                @SuppressWarnings("hiding") Token token = new43(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 44:
                            {
                                @SuppressWarnings("hiding") Token token = new44(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 45:
                            {
                                @SuppressWarnings("hiding") Token token = new45(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 46:
                            {
                                @SuppressWarnings("hiding") Token token = new46(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 47:
                            {
                                @SuppressWarnings("hiding") Token token = new47(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 48:
                            {
                                @SuppressWarnings("hiding") Token token = new48(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 49:
                            {
                                @SuppressWarnings("hiding") Token token = new49(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 50:
                            {
                                @SuppressWarnings("hiding") Token token = new50(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 51:
                            {
                                @SuppressWarnings("hiding") Token token = new51(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.ARGS;
                                        break;
                                    case 1:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 52:
                            {
                                @SuppressWarnings("hiding") Token token = new52(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.COMMAND;
                                        break;
                                }
                                return token;
                            }
                        case 53:
                            {
                                @SuppressWarnings("hiding") Token token = new53(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.COMMENT;
                                        break;
                                }
                                return token;
                            }
                        case 54:
                            {
                                @SuppressWarnings("hiding") Token token = new54(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 55:
                            {
                                @SuppressWarnings("hiding") Token token = new55(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 1:
                                        state = State.ARGS;
                                        break;
                                }
                                return token;
                            }
                        case 56:
                            {
                                @SuppressWarnings("hiding") Token token = new56(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 2:
                                        state = State.CONTENT;
                                        break;
                                    case 1:
                                        state = State.CONTENT;
                                        break;
                                    case 3:
                                        state = State.CONTENT;
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
        return new TData(text, line, pos);
    }

    Token new1(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TComment(text, line, pos);
    }

    Token new2(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TVar(line, pos);
    }

    Token new3(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLvar(line, pos);
    }

    Token new4(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEvar(line, pos);
    }

    Token new5(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TUvar(line, pos);
    }

    Token new6(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSet(line, pos);
    }

    Token new7(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIf(line, pos);
    }

    Token new8(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TElseIf(text, line, pos);
    }

    Token new9(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TElse(line, pos);
    }

    Token new10(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TWith(line, pos);
    }

    Token new11(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEscape(line, pos);
    }

    Token new12(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAutoescape(line, pos);
    }

    Token new13(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLoop(line, pos);
    }

    Token new14(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEach(line, pos);
    }

    Token new15(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAlt(line, pos);
    }

    Token new16(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TName(line, pos);
    }

    Token new17(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDef(line, pos);
    }

    Token new18(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCall(line, pos);
    }

    Token new19(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TInclude(line, pos);
    }

    Token new20(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLinclude(line, pos);
    }

    Token new21(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TContentType(line, pos);
    }

    Token new22(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TInline(line, pos);
    }

    Token new23(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TComma(line, pos);
    }

    Token new24(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBang(line, pos);
    }

    Token new25(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAssignment(line, pos);
    }

    Token new26(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TEq(line, pos);
    }

    Token new27(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNe(line, pos);
    }

    Token new28(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLt(line, pos);
    }

    Token new29(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TGt(line, pos);
    }

    Token new30(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLte(line, pos);
    }

    Token new31(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TGte(line, pos);
    }

    Token new32(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TAnd(line, pos);
    }

    Token new33(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TOr(line, pos);
    }

    Token new34(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TString(text, line, pos);
    }

    Token new35(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new THash(line, pos);
    }

    Token new36(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPlus(line, pos);
    }

    Token new37(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TMinus(line, pos);
    }

    Token new38(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStar(line, pos);
    }

    Token new39(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TPercent(line, pos);
    }

    Token new40(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBracketOpen(line, pos);
    }

    Token new41(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBracketClose(line, pos);
    }

    Token new42(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParenOpen(line, pos);
    }

    Token new43(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TParenClose(line, pos);
    }

    Token new44(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDot(line, pos);
    }

    Token new45(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDollar(line, pos);
    }

    Token new46(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TQuestion(line, pos);
    }

    Token new47(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDecNumber(text, line, pos);
    }

    Token new48(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new THexNumber(text, line, pos);
    }

    Token new49(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TWord(text, line, pos);
    }

    Token new50(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TArgWhitespace(text, line, pos);
    }

    Token new51(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSlash(line, pos);
    }

    Token new52(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCsOpen(text, line, pos);
    }

    Token new53(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCommentStart(line, pos);
    }

    Token new54(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCommandDelimiter(text, line, pos);
    }

    Token new55(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new THardDelimiter(text, line, pos);
    }

    Token new56(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCsClose(text, line, pos);
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

        public static final State CONTENT = new State(0);

        public static final State COMMAND = new State(1);

        public static final State ARGS = new State(2);

        public static final State COMMENT = new State(3);

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

package galoot.lexer;

import java.io.*;
import galoot.node.*;

@SuppressWarnings("nls")
public class Lexer {

    protected Token token;

    protected State state = State.DEFAULT;

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
                                switch(state.id()) {
                                    case 14:
                                        state = State.LOAD_VAR;
                                        break;
                                    case 10:
                                        state = State.SET_VAR;
                                        break;
                                    case 7:
                                        state = State.WITH_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 1:
                            {
                                @SuppressWarnings("hiding") Token token = new1(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 17:
                                        state = State.IF_VAR;
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
                                    case 17:
                                        state = State.IF_VAR;
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
                                    case 17:
                                        state = State.IF_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 4:
                            {
                                @SuppressWarnings("hiding") Token token = new4(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.DEFAULT;
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
                                        state = State.VAR;
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
                                        state = State.DEFAULT;
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
                                    case 23:
                                        state = State.FOR_VAR;
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
                                        state = State.BLOCK_TAG;
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
                                        state = State.LOAD_VAR;
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
                                        state = State.INCLUDE_TAG;
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
                                        state = State.FIRSTOF_VAR;
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
                                        state = State.FOR_TAG;
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
                                        state = State.IF_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 14:
                            {
                                @SuppressWarnings("hiding") Token token = new14(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.IFEQ_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 15:
                            {
                                @SuppressWarnings("hiding") Token token = new15(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.FILTER_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 16:
                            {
                                @SuppressWarnings("hiding") Token token = new16(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.WITH_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 17:
                            {
                                @SuppressWarnings("hiding") Token token = new17(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.SET_VAR;
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
                                        state = State.NOW_TAG;
                                        break;
                                }
                                return token;
                            }
                        case 19:
                            {
                                @SuppressWarnings("hiding") Token token = new19(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.EXTENDS_TAG;
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
                                        state = State.TEMPLATE_TAG;
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
                                        state = State.DEFAULT;
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
                                        state = State.DEFAULT;
                                        break;
                                }
                                return token;
                            }
                        case 23:
                            {
                                @SuppressWarnings("hiding") Token token = new23(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.DEFAULT;
                                        break;
                                }
                                return token;
                            }
                        case 24:
                            {
                                @SuppressWarnings("hiding") Token token = new24(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.COMMENT_BLOCK;
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
                                    case 27:
                                        state = State.DEFAULT;
                                        break;
                                    case 28:
                                        state = State.DEFAULT;
                                        break;
                                    case 4:
                                        state = State.DEFAULT;
                                        break;
                                    case 14:
                                        state = State.DEFAULT;
                                        break;
                                    case 13:
                                        state = State.DEFAULT;
                                        break;
                                    case 34:
                                        state = State.DEFAULT;
                                        break;
                                    case 33:
                                        state = State.DEFAULT;
                                        break;
                                    case 1:
                                        state = State.DEFAULT;
                                        break;
                                }
                                return token;
                            }
                        case 26:
                            {
                                @SuppressWarnings("hiding") Token token = new26(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 3:
                                        state = State.DEFAULT;
                                        break;
                                    case 31:
                                        state = State.DEFAULT;
                                        break;
                                    case 24:
                                        state = State.DEFAULT;
                                        break;
                                    case 20:
                                        state = State.DEFAULT;
                                        break;
                                    case 17:
                                        state = State.DEFAULT;
                                        break;
                                    case 10:
                                        state = State.DEFAULT;
                                        break;
                                    case 7:
                                        state = State.DEFAULT;
                                        break;
                                }
                                return token;
                            }
                        case 27:
                            {
                                @SuppressWarnings("hiding") Token token = new27(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.DEFAULT;
                                        break;
                                }
                                return token;
                            }
                        case 28:
                            {
                                @SuppressWarnings("hiding") Token token = new28(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.DEFAULT;
                                        break;
                                }
                                return token;
                            }
                        case 29:
                            {
                                @SuppressWarnings("hiding") Token token = new29(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.DEFAULT;
                                        break;
                                }
                                return token;
                            }
                        case 30:
                            {
                                @SuppressWarnings("hiding") Token token = new30(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.DEFAULT;
                                        break;
                                }
                                return token;
                            }
                        case 31:
                            {
                                @SuppressWarnings("hiding") Token token = new31(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 5:
                                        state = State.DEFAULT;
                                        break;
                                }
                                return token;
                            }
                        case 32:
                            {
                                @SuppressWarnings("hiding") Token token = new32(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 5:
                                        state = State.COMMENT_BLOCK;
                                        break;
                                }
                                return token;
                            }
                        case 33:
                            {
                                @SuppressWarnings("hiding") Token token = new33(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 27:
                                        state = State.EXTENDS_TAG;
                                        break;
                                    case 6:
                                        state = State.VAR;
                                        break;
                                    case 32:
                                        state = State.FILTER_VAR;
                                        break;
                                    case 30:
                                        state = State.FIRSTOF_VAR;
                                        break;
                                    case 28:
                                        state = State.FIRSTOF_VAR;
                                        break;
                                    case 20:
                                        state = State.IFEQ_VAR;
                                        break;
                                    case 4:
                                        state = State.INCLUDE_TAG;
                                        break;
                                    case 16:
                                        state = State.LOAD_VAR;
                                        break;
                                    case 13:
                                        state = State.LOAD_AS;
                                        break;
                                    case 34:
                                        state = State.NOW_TAG;
                                        break;
                                    case 12:
                                        state = State.SET_VAR;
                                        break;
                                    case 9:
                                        state = State.WITH_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 34:
                            {
                                @SuppressWarnings("hiding") Token token = new34(start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 28:
                                        state = State.FIRSTOF_MEMBER;
                                        break;
                                    case 24:
                                        state = State.FOR_MEMBER;
                                        break;
                                    case 20:
                                        state = State.IFEQ_MEMBER;
                                        break;
                                    case 17:
                                        state = State.IF_MEMBER;
                                        break;
                                    case 13:
                                        state = State.LOAD_MEMBER;
                                        break;
                                    case 10:
                                        state = State.SET_MEMBER;
                                        break;
                                    case 1:
                                        state = State.VAR_MEMBER;
                                        break;
                                    case 7:
                                        state = State.WITH_MEMBER;
                                        break;
                                }
                                return token;
                            }
                        case 35:
                            {
                                @SuppressWarnings("hiding") Token token = new35(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 3:
                                        state = State.BLOCK_TAG;
                                        break;
                                    case 31:
                                        state = State.FILTER_VAR;
                                        break;
                                    case 28:
                                        state = State.FIRSTOF_VAR;
                                        break;
                                    case 23:
                                        state = State.FOR_TAG;
                                        break;
                                    case 24:
                                        state = State.FOR_VAR;
                                        break;
                                    case 20:
                                        state = State.IFEQ_VAR;
                                        break;
                                    case 17:
                                        state = State.IF_VAR;
                                        break;
                                    case 4:
                                        state = State.INCLUDE_TAG;
                                        break;
                                    case 13:
                                        state = State.LOAD_VAR;
                                        break;
                                    case 10:
                                        state = State.SET_VAR;
                                        break;
                                    case 33:
                                        state = State.TEMPLATE_TAG;
                                        break;
                                    case 1:
                                        state = State.VAR;
                                        break;
                                    case 7:
                                        state = State.WITH_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 36:
                            {
                                @SuppressWarnings("hiding") Token token = new36(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 29:
                                        state = State.FIRSTOF_VAR;
                                        break;
                                    case 25:
                                        state = State.FOR_VAR;
                                        break;
                                    case 21:
                                        state = State.IFEQ_VAR;
                                        break;
                                    case 18:
                                        state = State.IF_VAR;
                                        break;
                                    case 4:
                                        state = State.INCLUDE_TAG;
                                        break;
                                    case 15:
                                        state = State.LOAD_VAR;
                                        break;
                                    case 11:
                                        state = State.SET_VAR;
                                        break;
                                    case 2:
                                        state = State.VAR;
                                        break;
                                    case 8:
                                        state = State.WITH_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 37:
                            {
                                @SuppressWarnings("hiding") Token token = new37(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 3:
                                        state = State.BLOCK_TAG;
                                        break;
                                    case 27:
                                        state = State.EXTENDS_TAG;
                                        break;
                                    case 6:
                                        state = State.FILTER;
                                        break;
                                    case 31:
                                        state = State.FILTER_VAR;
                                        break;
                                    case 28:
                                        state = State.FIRSTOF_VAR;
                                        break;
                                    case 23:
                                        state = State.FOR_TAG;
                                        break;
                                    case 24:
                                        state = State.FOR_VAR;
                                        break;
                                    case 20:
                                        state = State.IFEQ_VAR;
                                        break;
                                    case 19:
                                        state = State.IF_FILTER;
                                        break;
                                    case 17:
                                        state = State.IF_VAR;
                                        break;
                                    case 4:
                                        state = State.INCLUDE_TAG;
                                        break;
                                    case 14:
                                        state = State.LOAD_AS;
                                        break;
                                    case 16:
                                        state = State.LOAD_FILTER;
                                        break;
                                    case 13:
                                        state = State.LOAD_VAR;
                                        break;
                                    case 34:
                                        state = State.NOW_TAG;
                                        break;
                                    case 12:
                                        state = State.SET_FILTER;
                                        break;
                                    case 10:
                                        state = State.SET_VAR;
                                        break;
                                    case 33:
                                        state = State.TEMPLATE_TAG;
                                        break;
                                    case 1:
                                        state = State.VAR;
                                        break;
                                    case 9:
                                        state = State.WITH_FILTER;
                                        break;
                                    case 7:
                                        state = State.WITH_VAR;
                                        break;
                                }
                                return token;
                            }
                        case 38:
                            {
                                @SuppressWarnings("hiding") Token token = new38(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 0:
                                        state = State.DEFAULT;
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
                                    case 31:
                                        state = State.FILTER_VAR;
                                        break;
                                    case 28:
                                        state = State.FIRSTOF_VAR;
                                        break;
                                    case 24:
                                        state = State.FOR_VAR;
                                        break;
                                    case 20:
                                        state = State.IFEQ_VAR;
                                        break;
                                    case 17:
                                        state = State.IF_VAR;
                                        break;
                                    case 13:
                                        state = State.LOAD_VAR;
                                        break;
                                    case 10:
                                        state = State.SET_VAR;
                                        break;
                                    case 1:
                                        state = State.VAR;
                                        break;
                                    case 7:
                                        state = State.WITH_VAR;
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
                                    case 28:
                                        state = State.FIRSTOF_VAR;
                                        break;
                                    case 24:
                                        state = State.FOR_VAR;
                                        break;
                                    case 20:
                                        state = State.IFEQ_VAR;
                                        break;
                                    case 17:
                                        state = State.IF_VAR;
                                        break;
                                    case 13:
                                        state = State.LOAD_VAR;
                                        break;
                                    case 1:
                                        state = State.VAR;
                                        break;
                                    case 7:
                                        state = State.WITH_VAR;
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
                                    case 31:
                                        state = State.FILTER_FILTER;
                                        break;
                                    case 28:
                                        state = State.FIRSTOF_FILTER;
                                        break;
                                    case 24:
                                        state = State.FOR_FILTER;
                                        break;
                                    case 20:
                                        state = State.IFEQ_FILTER;
                                        break;
                                    case 17:
                                        state = State.IF_FILTER;
                                        break;
                                    case 13:
                                        state = State.LOAD_FILTER;
                                        break;
                                    case 10:
                                        state = State.SET_FILTER;
                                        break;
                                    case 1:
                                        state = State.FILTER;
                                        break;
                                    case 7:
                                        state = State.WITH_FILTER;
                                        break;
                                }
                                return token;
                            }
                        case 42:
                            {
                                @SuppressWarnings("hiding") Token token = new42(getText(accept_length), start_line + 1, start_pos + 1);
                                pushBack(accept_length);
                                this.pos = accept_pos;
                                this.line = accept_line;
                                switch(state.id()) {
                                    case 6:
                                        state = State.VAR;
                                        break;
                                    case 32:
                                        state = State.FILTER_VAR;
                                        break;
                                    case 30:
                                        state = State.FIRSTOF_VAR;
                                        break;
                                    case 26:
                                        state = State.FOR_VAR;
                                        break;
                                    case 22:
                                        state = State.IFEQ_VAR;
                                        break;
                                    case 19:
                                        state = State.IF_VAR;
                                        break;
                                    case 16:
                                        state = State.LOAD_VAR;
                                        break;
                                    case 12:
                                        state = State.SET_VAR;
                                        break;
                                    case 9:
                                        state = State.WITH_VAR;
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

    Token new0(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TKwAs(line, pos);
    }

    Token new1(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TKwNot(line, pos);
    }

    Token new2(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TKwAnd(line, pos);
    }

    Token new3(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TKwOr(line, pos);
    }

    Token new4(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSimpleComment(text, line, pos);
    }

    Token new5(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TVarStart(line, pos);
    }

    Token new6(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TVarEnd(line, pos);
    }

    Token new7(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TKwIn(line, pos);
    }

    Token new8(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBlockStart(text, line, pos);
    }

    Token new9(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TLoadStart(text, line, pos);
    }

    Token new10(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIncludeStart(text, line, pos);
    }

    Token new11(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFirstofStart(text, line, pos);
    }

    Token new12(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TForStart(text, line, pos);
    }

    Token new13(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIfStart(text, line, pos);
    }

    Token new14(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIfeqStart(text, line, pos);
    }

    Token new15(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFilterStart(text, line, pos);
    }

    Token new16(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TWithStart(text, line, pos);
    }

    Token new17(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TSetStart(text, line, pos);
    }

    Token new18(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TNowStart(text, line, pos);
    }

    Token new19(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TExtendsStart(text, line, pos);
    }

    Token new20(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTemplatetag(text, line, pos);
    }

    Token new21(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TElseTag(text, line, pos);
    }

    Token new22(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIfEnd(text, line, pos);
    }

    Token new23(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TIfeqEnd(text, line, pos);
    }

    Token new24(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCommentStart(text, line, pos);
    }

    Token new25(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTagEnd(line, pos);
    }

    Token new26(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTagEndEol(text, line, pos);
    }

    Token new27(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TForEnd(text, line, pos);
    }

    Token new28(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TWithEnd(text, line, pos);
    }

    Token new29(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TFilterEnd(text, line, pos);
    }

    Token new30(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TBlockEnd(text, line, pos);
    }

    Token new31(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCommentEnd(text, line, pos);
    }

    Token new32(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TCommentText(text, line, pos);
    }

    Token new33(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStringLiteral(text, line, pos);
    }

    Token new34(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TDot(line, pos);
    }

    Token new35(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TId(text, line, pos);
    }

    Token new36(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TMember(text, line, pos);
    }

    Token new37(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TStripWs(text, line, pos);
    }

    Token new38(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TTextChar(text, line, pos);
    }

    Token new39(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TVertPipe(line, pos);
    }

    Token new40(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TComma(line, pos);
    }

    Token new41(@SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TColon(line, pos);
    }

    Token new42(@SuppressWarnings("hiding") String text, @SuppressWarnings("hiding") int line, @SuppressWarnings("hiding") int pos) {
        return new TUnquoted(text, line, pos);
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

        public static final State DEFAULT = new State(0);

        public static final State VAR = new State(1);

        public static final State VAR_MEMBER = new State(2);

        public static final State BLOCK_TAG = new State(3);

        public static final State INCLUDE_TAG = new State(4);

        public static final State COMMENT_BLOCK = new State(5);

        public static final State FILTER = new State(6);

        public static final State WITH_VAR = new State(7);

        public static final State WITH_MEMBER = new State(8);

        public static final State WITH_FILTER = new State(9);

        public static final State SET_VAR = new State(10);

        public static final State SET_MEMBER = new State(11);

        public static final State SET_FILTER = new State(12);

        public static final State LOAD_VAR = new State(13);

        public static final State LOAD_AS = new State(14);

        public static final State LOAD_MEMBER = new State(15);

        public static final State LOAD_FILTER = new State(16);

        public static final State IF_VAR = new State(17);

        public static final State IF_MEMBER = new State(18);

        public static final State IF_FILTER = new State(19);

        public static final State IFEQ_VAR = new State(20);

        public static final State IFEQ_MEMBER = new State(21);

        public static final State IFEQ_FILTER = new State(22);

        public static final State FOR_TAG = new State(23);

        public static final State FOR_VAR = new State(24);

        public static final State FOR_MEMBER = new State(25);

        public static final State FOR_FILTER = new State(26);

        public static final State EXTENDS_TAG = new State(27);

        public static final State FIRSTOF_VAR = new State(28);

        public static final State FIRSTOF_MEMBER = new State(29);

        public static final State FIRSTOF_FILTER = new State(30);

        public static final State FILTER_VAR = new State(31);

        public static final State FILTER_FILTER = new State(32);

        public static final State TEMPLATE_TAG = new State(33);

        public static final State NOW_TAG = new State(34);

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

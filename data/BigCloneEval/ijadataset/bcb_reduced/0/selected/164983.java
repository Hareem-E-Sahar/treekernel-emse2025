package net.sourceforge.htmlunit.corejs.javascript;

import java.io.*;

/**
 * This class implements the JavaScript scanner.
 *
 * It is based on the C source files jsscan.c and jsscan.h
 * in the jsref package.
 *
 * @see net.sourceforge.htmlunit.corejs.javascript.Parser
 *
 * @author Mike McCabe
 * @author Brendan Eich
 */
class TokenStream {

    private static final int EOF_CHAR = -1;

    private static final char BYTE_ORDER_MARK = '﻿';

    TokenStream(Parser parser, Reader sourceReader, String sourceString, int lineno) {
        this.parser = parser;
        this.lineno = lineno;
        if (sourceReader != null) {
            if (sourceString != null) Kit.codeBug();
            this.sourceReader = sourceReader;
            this.sourceBuffer = new char[512];
            this.sourceEnd = 0;
        } else {
            if (sourceString == null) Kit.codeBug();
            this.sourceString = sourceString;
            this.sourceEnd = sourceString.length();
        }
        this.sourceCursor = this.cursor = 0;
    }

    String tokenToString(int token) {
        if (Token.printTrees) {
            String name = Token.name(token);
            switch(token) {
                case Token.STRING:
                case Token.REGEXP:
                case Token.NAME:
                    return name + " `" + this.string + "'";
                case Token.NUMBER:
                    return "NUMBER " + this.number;
            }
            return name;
        }
        return "";
    }

    static boolean isKeyword(String s) {
        return Token.EOF != stringToKeyword(s);
    }

    private static int stringToKeyword(String name) {
        final int Id_break = Token.BREAK, Id_case = Token.CASE, Id_continue = Token.CONTINUE, Id_default = Token.DEFAULT, Id_delete = Token.DELPROP, Id_do = Token.DO, Id_else = Token.ELSE, Id_export = Token.RESERVED, Id_false = Token.FALSE, Id_for = Token.FOR, Id_function = Token.FUNCTION, Id_if = Token.IF, Id_in = Token.IN, Id_let = Token.LET, Id_new = Token.NEW, Id_null = Token.NULL, Id_return = Token.RETURN, Id_switch = Token.SWITCH, Id_this = Token.THIS, Id_true = Token.TRUE, Id_typeof = Token.TYPEOF, Id_var = Token.VAR, Id_void = Token.VOID, Id_while = Token.WHILE, Id_with = Token.WITH, Id_yield = Token.YIELD, Id_abstract = Token.RESERVED, Id_boolean = Token.RESERVED, Id_byte = Token.RESERVED, Id_catch = Token.CATCH, Id_char = Token.RESERVED, Id_class = Token.RESERVED, Id_const = Token.CONST, Id_debugger = Token.DEBUGGER, Id_double = Token.RESERVED, Id_enum = Token.RESERVED, Id_extends = Token.RESERVED, Id_final = Token.RESERVED, Id_finally = Token.FINALLY, Id_float = Token.RESERVED, Id_goto = Token.RESERVED, Id_implements = Token.RESERVED, Id_import = Token.RESERVED, Id_instanceof = Token.INSTANCEOF, Id_int = Token.RESERVED, Id_interface = Token.RESERVED, Id_long = Token.RESERVED, Id_native = Token.RESERVED, Id_package = Token.RESERVED, Id_private = Token.RESERVED, Id_protected = Token.RESERVED, Id_public = Token.RESERVED, Id_short = Token.RESERVED, Id_static = Token.RESERVED, Id_super = Token.RESERVED, Id_synchronized = Token.RESERVED, Id_throw = Token.THROW, Id_throws = Token.RESERVED, Id_transient = Token.RESERVED, Id_try = Token.TRY, Id_volatile = Token.RESERVED;
        int id;
        String s = name;
        L0: {
            id = 0;
            String X = null;
            int c;
            L: switch(s.length()) {
                case 2:
                    c = s.charAt(1);
                    if (c == 'f') {
                        if (s.charAt(0) == 'i') {
                            id = Id_if;
                            break L0;
                        }
                    } else if (c == 'n') {
                        if (s.charAt(0) == 'i') {
                            id = Id_in;
                            break L0;
                        }
                    } else if (c == 'o') {
                        if (s.charAt(0) == 'd') {
                            id = Id_do;
                            break L0;
                        }
                    }
                    break L;
                case 3:
                    switch(s.charAt(0)) {
                        case 'f':
                            if (s.charAt(2) == 'r' && s.charAt(1) == 'o') {
                                id = Id_for;
                                break L0;
                            }
                            break L;
                        case 'i':
                            if (s.charAt(2) == 't' && s.charAt(1) == 'n') {
                                id = Id_int;
                                break L0;
                            }
                            break L;
                        case 'l':
                            if (s.charAt(2) == 't' && s.charAt(1) == 'e') {
                                id = Id_let;
                                break L0;
                            }
                            break L;
                        case 'n':
                            if (s.charAt(2) == 'w' && s.charAt(1) == 'e') {
                                id = Id_new;
                                break L0;
                            }
                            break L;
                        case 't':
                            if (s.charAt(2) == 'y' && s.charAt(1) == 'r') {
                                id = Id_try;
                                break L0;
                            }
                            break L;
                        case 'v':
                            if (s.charAt(2) == 'r' && s.charAt(1) == 'a') {
                                id = Id_var;
                                break L0;
                            }
                            break L;
                    }
                    break L;
                case 4:
                    switch(s.charAt(0)) {
                        case 'b':
                            X = "byte";
                            id = Id_byte;
                            break L;
                        case 'c':
                            c = s.charAt(3);
                            if (c == 'e') {
                                if (s.charAt(2) == 's' && s.charAt(1) == 'a') {
                                    id = Id_case;
                                    break L0;
                                }
                            } else if (c == 'r') {
                                if (s.charAt(2) == 'a' && s.charAt(1) == 'h') {
                                    id = Id_char;
                                    break L0;
                                }
                            }
                            break L;
                        case 'e':
                            c = s.charAt(3);
                            if (c == 'e') {
                                if (s.charAt(2) == 's' && s.charAt(1) == 'l') {
                                    id = Id_else;
                                    break L0;
                                }
                            } else if (c == 'm') {
                                if (s.charAt(2) == 'u' && s.charAt(1) == 'n') {
                                    id = Id_enum;
                                    break L0;
                                }
                            }
                            break L;
                        case 'g':
                            X = "goto";
                            id = Id_goto;
                            break L;
                        case 'l':
                            X = "long";
                            id = Id_long;
                            break L;
                        case 'n':
                            X = "null";
                            id = Id_null;
                            break L;
                        case 't':
                            c = s.charAt(3);
                            if (c == 'e') {
                                if (s.charAt(2) == 'u' && s.charAt(1) == 'r') {
                                    id = Id_true;
                                    break L0;
                                }
                            } else if (c == 's') {
                                if (s.charAt(2) == 'i' && s.charAt(1) == 'h') {
                                    id = Id_this;
                                    break L0;
                                }
                            }
                            break L;
                        case 'v':
                            X = "void";
                            id = Id_void;
                            break L;
                        case 'w':
                            X = "with";
                            id = Id_with;
                            break L;
                    }
                    break L;
                case 5:
                    switch(s.charAt(2)) {
                        case 'a':
                            X = "class";
                            id = Id_class;
                            break L;
                        case 'e':
                            c = s.charAt(0);
                            if (c == 'b') {
                                X = "break";
                                id = Id_break;
                            } else if (c == 'y') {
                                X = "yield";
                                id = Id_yield;
                            }
                            break L;
                        case 'i':
                            X = "while";
                            id = Id_while;
                            break L;
                        case 'l':
                            X = "false";
                            id = Id_false;
                            break L;
                        case 'n':
                            c = s.charAt(0);
                            if (c == 'c') {
                                X = "const";
                                id = Id_const;
                            } else if (c == 'f') {
                                X = "final";
                                id = Id_final;
                            }
                            break L;
                        case 'o':
                            c = s.charAt(0);
                            if (c == 'f') {
                                X = "float";
                                id = Id_float;
                            } else if (c == 's') {
                                X = "short";
                                id = Id_short;
                            }
                            break L;
                        case 'p':
                            X = "super";
                            id = Id_super;
                            break L;
                        case 'r':
                            X = "throw";
                            id = Id_throw;
                            break L;
                        case 't':
                            X = "catch";
                            id = Id_catch;
                            break L;
                    }
                    break L;
                case 6:
                    switch(s.charAt(1)) {
                        case 'a':
                            X = "native";
                            id = Id_native;
                            break L;
                        case 'e':
                            c = s.charAt(0);
                            if (c == 'd') {
                                X = "delete";
                                id = Id_delete;
                            } else if (c == 'r') {
                                X = "return";
                                id = Id_return;
                            }
                            break L;
                        case 'h':
                            X = "throws";
                            id = Id_throws;
                            break L;
                        case 'm':
                            X = "import";
                            id = Id_import;
                            break L;
                        case 'o':
                            X = "double";
                            id = Id_double;
                            break L;
                        case 't':
                            X = "static";
                            id = Id_static;
                            break L;
                        case 'u':
                            X = "public";
                            id = Id_public;
                            break L;
                        case 'w':
                            X = "switch";
                            id = Id_switch;
                            break L;
                        case 'x':
                            X = "export";
                            id = Id_export;
                            break L;
                        case 'y':
                            X = "typeof";
                            id = Id_typeof;
                            break L;
                    }
                    break L;
                case 7:
                    switch(s.charAt(1)) {
                        case 'a':
                            X = "package";
                            id = Id_package;
                            break L;
                        case 'e':
                            X = "default";
                            id = Id_default;
                            break L;
                        case 'i':
                            X = "finally";
                            id = Id_finally;
                            break L;
                        case 'o':
                            X = "boolean";
                            id = Id_boolean;
                            break L;
                        case 'r':
                            X = "private";
                            id = Id_private;
                            break L;
                        case 'x':
                            X = "extends";
                            id = Id_extends;
                            break L;
                    }
                    break L;
                case 8:
                    switch(s.charAt(0)) {
                        case 'a':
                            X = "abstract";
                            id = Id_abstract;
                            break L;
                        case 'c':
                            X = "continue";
                            id = Id_continue;
                            break L;
                        case 'd':
                            X = "debugger";
                            id = Id_debugger;
                            break L;
                        case 'f':
                            X = "function";
                            id = Id_function;
                            break L;
                        case 'v':
                            X = "volatile";
                            id = Id_volatile;
                            break L;
                    }
                    break L;
                case 9:
                    c = s.charAt(0);
                    if (c == 'i') {
                        X = "interface";
                        id = Id_interface;
                    } else if (c == 'p') {
                        X = "protected";
                        id = Id_protected;
                    } else if (c == 't') {
                        X = "transient";
                        id = Id_transient;
                    }
                    break L;
                case 10:
                    c = s.charAt(1);
                    if (c == 'm') {
                        X = "implements";
                        id = Id_implements;
                    } else if (c == 'n') {
                        X = "instanceof";
                        id = Id_instanceof;
                    }
                    break L;
                case 12:
                    X = "synchronized";
                    id = Id_synchronized;
                    break L;
            }
            if (X != null && X != s && !X.equals(s)) id = 0;
        }
        if (id == 0) {
            return Token.EOF;
        }
        return id & 0xff;
    }

    final String getSourceString() {
        return sourceString;
    }

    final int getLineno() {
        return lineno;
    }

    final String getString() {
        return string;
    }

    final char getQuoteChar() {
        return (char) quoteChar;
    }

    final double getNumber() {
        return number;
    }

    final boolean isNumberOctal() {
        return isOctal;
    }

    final boolean eof() {
        return hitEOF;
    }

    final int getToken() throws IOException {
        int c;
        retry: for (; ; ) {
            for (; ; ) {
                c = getChar();
                if (c == EOF_CHAR) {
                    tokenBeg = cursor - 1;
                    tokenEnd = cursor;
                    return Token.EOF;
                } else if (c == '\n') {
                    dirtyLine = false;
                    tokenBeg = cursor - 1;
                    tokenEnd = cursor;
                    return Token.EOL;
                } else if (!isJSSpace(c)) {
                    if (c != '-') {
                        dirtyLine = true;
                    }
                    break;
                }
            }
            tokenBeg = cursor - 1;
            tokenEnd = cursor;
            if (c == '@') return Token.XMLATTR;
            boolean identifierStart;
            boolean isUnicodeEscapeStart = false;
            if (c == '\\') {
                c = getChar();
                if (c == 'u') {
                    identifierStart = true;
                    isUnicodeEscapeStart = true;
                    stringBufferTop = 0;
                } else {
                    identifierStart = false;
                    ungetChar(c);
                    c = '\\';
                }
            } else {
                identifierStart = Character.isJavaIdentifierStart((char) c);
                if (identifierStart) {
                    stringBufferTop = 0;
                    addToString(c);
                }
            }
            if (identifierStart) {
                boolean containsEscape = isUnicodeEscapeStart;
                for (; ; ) {
                    if (isUnicodeEscapeStart) {
                        int escapeVal = 0;
                        for (int i = 0; i != 4; ++i) {
                            c = getChar();
                            escapeVal = Kit.xDigitToInt(c, escapeVal);
                            if (escapeVal < 0) {
                                break;
                            }
                        }
                        if (escapeVal < 0) {
                            parser.addError("msg.invalid.escape");
                            return Token.ERROR;
                        }
                        addToString(escapeVal);
                        isUnicodeEscapeStart = false;
                    } else {
                        c = getChar();
                        if (c == '\\') {
                            c = getChar();
                            if (c == 'u') {
                                isUnicodeEscapeStart = true;
                                containsEscape = true;
                            } else {
                                parser.addError("msg.illegal.character");
                                return Token.ERROR;
                            }
                        } else {
                            if (c == EOF_CHAR || c == BYTE_ORDER_MARK || !Character.isJavaIdentifierPart((char) c)) {
                                break;
                            }
                            addToString(c);
                        }
                    }
                }
                ungetChar(c);
                String str = getStringFromBuffer();
                if (!containsEscape) {
                    int result = stringToKeyword(str);
                    if (result != Token.EOF) {
                        if ((result == Token.LET || result == Token.YIELD) && parser.compilerEnv.getLanguageVersion() < Context.VERSION_1_7) {
                            string = result == Token.LET ? "let" : "yield";
                            result = Token.NAME;
                        }
                        if (result != Token.RESERVED) {
                            return result;
                        } else if (!parser.compilerEnv.isReservedKeywordAsIdentifier()) {
                            return result;
                        }
                    }
                }
                this.string = (String) allStrings.intern(str);
                return Token.NAME;
            }
            if (isDigit(c) || (c == '.' && isDigit(peekChar()))) {
                isOctal = false;
                stringBufferTop = 0;
                int base = 10;
                if (c == '0') {
                    c = getChar();
                    if (c == 'x' || c == 'X') {
                        base = 16;
                        c = getChar();
                    } else if (isDigit(c)) {
                        base = 8;
                        isOctal = true;
                    } else {
                        addToString('0');
                    }
                }
                if (base == 16) {
                    while (0 <= Kit.xDigitToInt(c, 0)) {
                        addToString(c);
                        c = getChar();
                    }
                } else {
                    while ('0' <= c && c <= '9') {
                        if (base == 8 && c >= '8') {
                            parser.addWarning("msg.bad.octal.literal", c == '8' ? "8" : "9");
                            base = 10;
                        }
                        addToString(c);
                        c = getChar();
                    }
                }
                boolean isInteger = true;
                if (base == 10 && (c == '.' || c == 'e' || c == 'E')) {
                    isInteger = false;
                    if (c == '.') {
                        do {
                            addToString(c);
                            c = getChar();
                        } while (isDigit(c));
                    }
                    if (c == 'e' || c == 'E') {
                        addToString(c);
                        c = getChar();
                        if (c == '+' || c == '-') {
                            addToString(c);
                            c = getChar();
                        }
                        if (!isDigit(c)) {
                            parser.addError("msg.missing.exponent");
                            return Token.ERROR;
                        }
                        do {
                            addToString(c);
                            c = getChar();
                        } while (isDigit(c));
                    }
                }
                ungetChar(c);
                String numString = getStringFromBuffer();
                this.string = numString;
                double dval;
                if (base == 10 && !isInteger) {
                    try {
                        dval = Double.valueOf(numString).doubleValue();
                    } catch (NumberFormatException ex) {
                        parser.addError("msg.caught.nfe");
                        return Token.ERROR;
                    }
                } else {
                    dval = ScriptRuntime.stringToNumber(numString, 0, base);
                }
                this.number = dval;
                return Token.NUMBER;
            }
            if (c == '"' || c == '\'') {
                quoteChar = c;
                stringBufferTop = 0;
                c = getChar();
                strLoop: while (c != quoteChar) {
                    if (c == '\n' || c == EOF_CHAR) {
                        ungetChar(c);
                        tokenEnd = cursor;
                        parser.addError("msg.unterminated.string.lit");
                        return Token.ERROR;
                    }
                    if (c == '\\') {
                        int escapeVal;
                        c = getChar();
                        switch(c) {
                            case 'b':
                                c = '\b';
                                break;
                            case 'f':
                                c = '\f';
                                break;
                            case 'n':
                                c = '\n';
                                break;
                            case 'r':
                                c = '\r';
                                break;
                            case 't':
                                c = '\t';
                                break;
                            case 'v':
                                c = 0xb;
                                break;
                            case 'u':
                                int escapeStart = stringBufferTop;
                                addToString('u');
                                escapeVal = 0;
                                for (int i = 0; i != 4; ++i) {
                                    c = getChar();
                                    escapeVal = Kit.xDigitToInt(c, escapeVal);
                                    if (escapeVal < 0) {
                                        continue strLoop;
                                    }
                                    addToString(c);
                                }
                                stringBufferTop = escapeStart;
                                c = escapeVal;
                                break;
                            case 'x':
                                c = getChar();
                                escapeVal = Kit.xDigitToInt(c, 0);
                                if (escapeVal < 0) {
                                    addToString('x');
                                    continue strLoop;
                                } else {
                                    int c1 = c;
                                    c = getChar();
                                    escapeVal = Kit.xDigitToInt(c, escapeVal);
                                    if (escapeVal < 0) {
                                        addToString('x');
                                        addToString(c1);
                                        continue strLoop;
                                    } else {
                                        c = escapeVal;
                                    }
                                }
                                break;
                            case '\n':
                                c = getChar();
                                continue strLoop;
                            default:
                                if ('0' <= c && c < '8') {
                                    int val = c - '0';
                                    c = getChar();
                                    if ('0' <= c && c < '8') {
                                        val = 8 * val + c - '0';
                                        c = getChar();
                                        if ('0' <= c && c < '8' && val <= 037) {
                                            val = 8 * val + c - '0';
                                            c = getChar();
                                        }
                                    }
                                    ungetChar(c);
                                    c = val;
                                }
                        }
                    }
                    addToString(c);
                    c = getChar();
                }
                String str = getStringFromBuffer();
                this.string = (String) allStrings.intern(str);
                return Token.STRING;
            }
            switch(c) {
                case ';':
                    return Token.SEMI;
                case '[':
                    return Token.LB;
                case ']':
                    return Token.RB;
                case '{':
                    return Token.LC;
                case '}':
                    return Token.RC;
                case '(':
                    return Token.LP;
                case ')':
                    return Token.RP;
                case ',':
                    return Token.COMMA;
                case '?':
                    return Token.HOOK;
                case ':':
                    if (matchChar(':')) {
                        return Token.COLONCOLON;
                    } else {
                        return Token.COLON;
                    }
                case '.':
                    if (matchChar('.')) {
                        return Token.DOTDOT;
                    } else if (matchChar('(')) {
                        return Token.DOTQUERY;
                    } else {
                        return Token.DOT;
                    }
                case '|':
                    if (matchChar('|')) {
                        return Token.OR;
                    } else if (matchChar('=')) {
                        return Token.ASSIGN_BITOR;
                    } else {
                        return Token.BITOR;
                    }
                case '^':
                    if (matchChar('=')) {
                        return Token.ASSIGN_BITXOR;
                    } else {
                        return Token.BITXOR;
                    }
                case '&':
                    if (matchChar('&')) {
                        return Token.AND;
                    } else if (matchChar('=')) {
                        return Token.ASSIGN_BITAND;
                    } else {
                        return Token.BITAND;
                    }
                case '=':
                    if (matchChar('=')) {
                        if (matchChar('=')) {
                            return Token.SHEQ;
                        } else {
                            return Token.EQ;
                        }
                    } else {
                        return Token.ASSIGN;
                    }
                case '!':
                    if (matchChar('=')) {
                        if (matchChar('=')) {
                            return Token.SHNE;
                        } else {
                            return Token.NE;
                        }
                    } else {
                        return Token.NOT;
                    }
                case '<':
                    if (matchChar('!')) {
                        if (matchChar('-')) {
                            if (matchChar('-')) {
                                skipLine();
                                continue retry;
                            }
                            ungetCharIgnoreLineEnd('-');
                        }
                        ungetCharIgnoreLineEnd('!');
                    }
                    if (matchChar('<')) {
                        if (matchChar('=')) {
                            return Token.ASSIGN_LSH;
                        } else {
                            return Token.LSH;
                        }
                    } else {
                        if (matchChar('=')) {
                            return Token.LE;
                        } else {
                            return Token.LT;
                        }
                    }
                case '>':
                    if (matchChar('>')) {
                        if (matchChar('>')) {
                            if (matchChar('=')) {
                                return Token.ASSIGN_URSH;
                            } else {
                                return Token.URSH;
                            }
                        } else {
                            if (matchChar('=')) {
                                return Token.ASSIGN_RSH;
                            } else {
                                return Token.RSH;
                            }
                        }
                    } else {
                        if (matchChar('=')) {
                            return Token.GE;
                        } else {
                            return Token.GT;
                        }
                    }
                case '*':
                    if (matchChar('=')) {
                        return Token.ASSIGN_MUL;
                    } else {
                        return Token.MUL;
                    }
                case '/':
                    markCommentStart();
                    if (matchChar('/')) {
                        tokenBeg = cursor - 2;
                        skipLine();
                        commentType = Token.CommentType.LINE;
                        return Token.COMMENT;
                    }
                    if (matchChar('*')) {
                        boolean lookForSlash = false;
                        tokenBeg = cursor - 2;
                        if (matchChar('*')) {
                            lookForSlash = true;
                            commentType = Token.CommentType.JSDOC;
                        } else {
                            commentType = Token.CommentType.BLOCK_COMMENT;
                        }
                        for (; ; ) {
                            c = getChar();
                            if (c == EOF_CHAR) {
                                tokenEnd = cursor - 1;
                                parser.addError("msg.unterminated.comment");
                                return Token.COMMENT;
                            } else if (c == '*') {
                                lookForSlash = true;
                            } else if (c == '/') {
                                if (lookForSlash) {
                                    tokenEnd = cursor;
                                    return Token.COMMENT;
                                }
                            } else {
                                lookForSlash = false;
                                tokenEnd = cursor;
                            }
                        }
                    }
                    if (matchChar('=')) {
                        return Token.ASSIGN_DIV;
                    } else {
                        return Token.DIV;
                    }
                case '%':
                    if (matchChar('=')) {
                        return Token.ASSIGN_MOD;
                    } else {
                        return Token.MOD;
                    }
                case '~':
                    return Token.BITNOT;
                case '+':
                    if (matchChar('=')) {
                        return Token.ASSIGN_ADD;
                    } else if (matchChar('+')) {
                        return Token.INC;
                    } else {
                        return Token.ADD;
                    }
                case '-':
                    if (matchChar('=')) {
                        c = Token.ASSIGN_SUB;
                    } else if (matchChar('-')) {
                        if (!dirtyLine) {
                            if (matchChar('>')) {
                                markCommentStart("--");
                                skipLine();
                                commentType = Token.CommentType.HTML;
                                return Token.COMMENT;
                            }
                        }
                        c = Token.DEC;
                    } else {
                        c = Token.SUB;
                    }
                    dirtyLine = true;
                    return c;
                default:
                    parser.addError("msg.illegal.character");
                    return Token.ERROR;
            }
        }
    }

    private static boolean isAlpha(int c) {
        if (c <= 'Z') {
            return 'A' <= c;
        } else {
            return 'a' <= c && c <= 'z';
        }
    }

    static boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    static boolean isJSSpace(int c) {
        if (c <= 127) {
            return c == 0x20 || c == 0x9 || c == 0xC || c == 0xB;
        } else {
            return c == 0xA0 || c == BYTE_ORDER_MARK || Character.getType((char) c) == Character.SPACE_SEPARATOR;
        }
    }

    private static boolean isJSFormatChar(int c) {
        return c > 127 && Character.getType((char) c) == Character.FORMAT;
    }

    /**
     * Parser calls the method when it gets / or /= in literal context.
     */
    void readRegExp(int startToken) throws IOException {
        int start = tokenBeg;
        stringBufferTop = 0;
        if (startToken == Token.ASSIGN_DIV) {
            addToString('=');
        } else {
            if (startToken != Token.DIV) Kit.codeBug();
        }
        boolean inCharSet = false;
        int c;
        while ((c = getChar()) != '/' || inCharSet) {
            if (c == '\n' || c == EOF_CHAR) {
                ungetChar(c);
                tokenEnd = cursor - 1;
                this.string = new String(stringBuffer, 0, stringBufferTop);
                parser.reportError("msg.unterminated.re.lit");
                return;
            }
            if (c == '\\') {
                addToString(c);
                c = getChar();
            } else if (c == '[') {
                inCharSet = true;
            } else if (c == ']') {
                inCharSet = false;
            }
            addToString(c);
        }
        int reEnd = stringBufferTop;
        while (true) {
            if (matchChar('g')) addToString('g'); else if (matchChar('i')) addToString('i'); else if (matchChar('m')) addToString('m'); else if (matchChar('y')) addToString('y'); else break;
        }
        tokenEnd = start + stringBufferTop + 2;
        if (isAlpha(peekChar())) {
            parser.reportError("msg.invalid.re.flag");
        }
        this.string = new String(stringBuffer, 0, reEnd);
        this.regExpFlags = new String(stringBuffer, reEnd, stringBufferTop - reEnd);
    }

    String readAndClearRegExpFlags() {
        String flags = this.regExpFlags;
        this.regExpFlags = null;
        return flags;
    }

    boolean isXMLAttribute() {
        return xmlIsAttribute;
    }

    int getFirstXMLToken() throws IOException {
        xmlOpenTagsCount = 0;
        xmlIsAttribute = false;
        xmlIsTagContent = false;
        if (!canUngetChar()) return Token.ERROR;
        ungetChar('<');
        return getNextXMLToken();
    }

    int getNextXMLToken() throws IOException {
        tokenBeg = cursor;
        stringBufferTop = 0;
        for (int c = getChar(); c != EOF_CHAR; c = getChar()) {
            if (xmlIsTagContent) {
                switch(c) {
                    case '>':
                        addToString(c);
                        xmlIsTagContent = false;
                        xmlIsAttribute = false;
                        break;
                    case '/':
                        addToString(c);
                        if (peekChar() == '>') {
                            c = getChar();
                            addToString(c);
                            xmlIsTagContent = false;
                            xmlOpenTagsCount--;
                        }
                        break;
                    case '{':
                        ungetChar(c);
                        this.string = getStringFromBuffer();
                        return Token.XML;
                    case '\'':
                    case '"':
                        addToString(c);
                        if (!readQuotedString(c)) return Token.ERROR;
                        break;
                    case '=':
                        addToString(c);
                        xmlIsAttribute = true;
                        break;
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        addToString(c);
                        break;
                    default:
                        addToString(c);
                        xmlIsAttribute = false;
                        break;
                }
                if (!xmlIsTagContent && xmlOpenTagsCount == 0) {
                    this.string = getStringFromBuffer();
                    return Token.XMLEND;
                }
            } else {
                switch(c) {
                    case '<':
                        addToString(c);
                        c = peekChar();
                        switch(c) {
                            case '!':
                                c = getChar();
                                addToString(c);
                                c = peekChar();
                                switch(c) {
                                    case '-':
                                        c = getChar();
                                        addToString(c);
                                        c = getChar();
                                        if (c == '-') {
                                            addToString(c);
                                            if (!readXmlComment()) return Token.ERROR;
                                        } else {
                                            stringBufferTop = 0;
                                            this.string = null;
                                            parser.addError("msg.XML.bad.form");
                                            return Token.ERROR;
                                        }
                                        break;
                                    case '[':
                                        c = getChar();
                                        addToString(c);
                                        if (getChar() == 'C' && getChar() == 'D' && getChar() == 'A' && getChar() == 'T' && getChar() == 'A' && getChar() == '[') {
                                            addToString('C');
                                            addToString('D');
                                            addToString('A');
                                            addToString('T');
                                            addToString('A');
                                            addToString('[');
                                            if (!readCDATA()) return Token.ERROR;
                                        } else {
                                            stringBufferTop = 0;
                                            this.string = null;
                                            parser.addError("msg.XML.bad.form");
                                            return Token.ERROR;
                                        }
                                        break;
                                    default:
                                        if (!readEntity()) return Token.ERROR;
                                        break;
                                }
                                break;
                            case '?':
                                c = getChar();
                                addToString(c);
                                if (!readPI()) return Token.ERROR;
                                break;
                            case '/':
                                c = getChar();
                                addToString(c);
                                if (xmlOpenTagsCount == 0) {
                                    stringBufferTop = 0;
                                    this.string = null;
                                    parser.addError("msg.XML.bad.form");
                                    return Token.ERROR;
                                }
                                xmlIsTagContent = true;
                                xmlOpenTagsCount--;
                                break;
                            default:
                                xmlIsTagContent = true;
                                xmlOpenTagsCount++;
                                break;
                        }
                        break;
                    case '{':
                        ungetChar(c);
                        this.string = getStringFromBuffer();
                        return Token.XML;
                    default:
                        addToString(c);
                        break;
                }
            }
        }
        tokenEnd = cursor;
        stringBufferTop = 0;
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return Token.ERROR;
    }

    /**
     *
     */
    private boolean readQuotedString(int quote) throws IOException {
        for (int c = getChar(); c != EOF_CHAR; c = getChar()) {
            addToString(c);
            if (c == quote) return true;
        }
        stringBufferTop = 0;
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    /**
     *
     */
    private boolean readXmlComment() throws IOException {
        for (int c = getChar(); c != EOF_CHAR; ) {
            addToString(c);
            if (c == '-' && peekChar() == '-') {
                c = getChar();
                addToString(c);
                if (peekChar() == '>') {
                    c = getChar();
                    addToString(c);
                    return true;
                } else {
                    continue;
                }
            }
            c = getChar();
        }
        stringBufferTop = 0;
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    /**
     *
     */
    private boolean readCDATA() throws IOException {
        for (int c = getChar(); c != EOF_CHAR; ) {
            addToString(c);
            if (c == ']' && peekChar() == ']') {
                c = getChar();
                addToString(c);
                if (peekChar() == '>') {
                    c = getChar();
                    addToString(c);
                    return true;
                } else {
                    continue;
                }
            }
            c = getChar();
        }
        stringBufferTop = 0;
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    /**
     *
     */
    private boolean readEntity() throws IOException {
        int declTags = 1;
        for (int c = getChar(); c != EOF_CHAR; c = getChar()) {
            addToString(c);
            switch(c) {
                case '<':
                    declTags++;
                    break;
                case '>':
                    declTags--;
                    if (declTags == 0) return true;
                    break;
            }
        }
        stringBufferTop = 0;
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    /**
     *
     */
    private boolean readPI() throws IOException {
        for (int c = getChar(); c != EOF_CHAR; c = getChar()) {
            addToString(c);
            if (c == '?' && peekChar() == '>') {
                c = getChar();
                addToString(c);
                return true;
            }
        }
        stringBufferTop = 0;
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    private String getStringFromBuffer() {
        tokenEnd = cursor;
        return new String(stringBuffer, 0, stringBufferTop);
    }

    private void addToString(int c) {
        int N = stringBufferTop;
        if (N == stringBuffer.length) {
            char[] tmp = new char[stringBuffer.length * 2];
            System.arraycopy(stringBuffer, 0, tmp, 0, N);
            stringBuffer = tmp;
        }
        stringBuffer[N] = (char) c;
        stringBufferTop = N + 1;
    }

    private boolean canUngetChar() {
        return ungetCursor == 0 || ungetBuffer[ungetCursor - 1] != '\n';
    }

    private void ungetChar(int c) {
        if (ungetCursor != 0 && ungetBuffer[ungetCursor - 1] == '\n') Kit.codeBug();
        ungetBuffer[ungetCursor++] = c;
        cursor--;
    }

    private boolean matchChar(int test) throws IOException {
        int c = getCharIgnoreLineEnd();
        if (c == test) {
            tokenEnd = cursor;
            return true;
        } else {
            ungetCharIgnoreLineEnd(c);
            return false;
        }
    }

    private int peekChar() throws IOException {
        int c = getChar();
        ungetChar(c);
        return c;
    }

    private int getChar() throws IOException {
        if (ungetCursor != 0) {
            cursor++;
            return ungetBuffer[--ungetCursor];
        }
        for (; ; ) {
            int c;
            if (sourceString != null) {
                if (sourceCursor == sourceEnd) {
                    hitEOF = true;
                    return EOF_CHAR;
                }
                cursor++;
                c = sourceString.charAt(sourceCursor++);
            } else {
                if (sourceCursor == sourceEnd) {
                    if (!fillSourceBuffer()) {
                        hitEOF = true;
                        return EOF_CHAR;
                    }
                }
                cursor++;
                c = sourceBuffer[sourceCursor++];
            }
            if (lineEndChar >= 0) {
                if (lineEndChar == '\r' && c == '\n') {
                    lineEndChar = '\n';
                    continue;
                }
                lineEndChar = -1;
                lineStart = sourceCursor - 1;
                lineno++;
            }
            if (c <= 127) {
                if (c == '\n' || c == '\r') {
                    lineEndChar = c;
                    c = '\n';
                }
            } else {
                if (c == BYTE_ORDER_MARK) return c;
                if (isJSFormatChar(c)) {
                    continue;
                }
                if (ScriptRuntime.isJSLineTerminator(c)) {
                    lineEndChar = c;
                    c = '\n';
                }
            }
            return c;
        }
    }

    private int getCharIgnoreLineEnd() throws IOException {
        if (ungetCursor != 0) {
            cursor++;
            return ungetBuffer[--ungetCursor];
        }
        for (; ; ) {
            int c;
            if (sourceString != null) {
                if (sourceCursor == sourceEnd) {
                    hitEOF = true;
                    return EOF_CHAR;
                }
                cursor++;
                c = sourceString.charAt(sourceCursor++);
            } else {
                if (sourceCursor == sourceEnd) {
                    if (!fillSourceBuffer()) {
                        hitEOF = true;
                        return EOF_CHAR;
                    }
                }
                cursor++;
                c = sourceBuffer[sourceCursor++];
            }
            if (c <= 127) {
                if (c == '\n' || c == '\r') {
                    lineEndChar = c;
                    c = '\n';
                }
            } else {
                if (c == BYTE_ORDER_MARK) return c;
                if (isJSFormatChar(c)) {
                    continue;
                }
                if (ScriptRuntime.isJSLineTerminator(c)) {
                    lineEndChar = c;
                    c = '\n';
                }
            }
            return c;
        }
    }

    private void ungetCharIgnoreLineEnd(int c) {
        ungetBuffer[ungetCursor++] = c;
        cursor--;
    }

    private void skipLine() throws IOException {
        int c;
        while ((c = getChar()) != EOF_CHAR && c != '\n') {
        }
        ungetChar(c);
        tokenEnd = cursor;
    }

    /**
     * Returns the offset into the current line.
     */
    final int getOffset() {
        int n = sourceCursor - lineStart;
        if (lineEndChar >= 0) {
            --n;
        }
        return n;
    }

    final String getLine() {
        if (sourceString != null) {
            int lineEnd = sourceCursor;
            if (lineEndChar >= 0) {
                --lineEnd;
            } else {
                for (; lineEnd != sourceEnd; ++lineEnd) {
                    int c = sourceString.charAt(lineEnd);
                    if (ScriptRuntime.isJSLineTerminator(c)) {
                        break;
                    }
                }
            }
            return sourceString.substring(lineStart, lineEnd);
        } else {
            int lineLength = sourceCursor - lineStart;
            if (lineEndChar >= 0) {
                --lineLength;
            } else {
                for (; ; ++lineLength) {
                    int i = lineStart + lineLength;
                    if (i == sourceEnd) {
                        try {
                            if (!fillSourceBuffer()) {
                                break;
                            }
                        } catch (IOException ioe) {
                            break;
                        }
                        i = lineStart + lineLength;
                    }
                    int c = sourceBuffer[i];
                    if (ScriptRuntime.isJSLineTerminator(c)) {
                        break;
                    }
                }
            }
            return new String(sourceBuffer, lineStart, lineLength);
        }
    }

    private boolean fillSourceBuffer() throws IOException {
        if (sourceString != null) Kit.codeBug();
        if (sourceEnd == sourceBuffer.length) {
            if (lineStart != 0 && !isMarkingComment()) {
                System.arraycopy(sourceBuffer, lineStart, sourceBuffer, 0, sourceEnd - lineStart);
                sourceEnd -= lineStart;
                sourceCursor -= lineStart;
                lineStart = 0;
            } else {
                char[] tmp = new char[sourceBuffer.length * 2];
                System.arraycopy(sourceBuffer, 0, tmp, 0, sourceEnd);
                sourceBuffer = tmp;
            }
        }
        int n = sourceReader.read(sourceBuffer, sourceEnd, sourceBuffer.length - sourceEnd);
        if (n < 0) {
            return false;
        }
        sourceEnd += n;
        return true;
    }

    /**
     * Return the current position of the scanner cursor.
     */
    public int getCursor() {
        return cursor;
    }

    /**
     * Return the absolute source offset of the last scanned token.
     */
    public int getTokenBeg() {
        return tokenBeg;
    }

    /**
     * Return the absolute source end-offset of the last scanned token.
     */
    public int getTokenEnd() {
        return tokenEnd;
    }

    /**
     * Return tokenEnd - tokenBeg
     */
    public int getTokenLength() {
        return tokenEnd - tokenBeg;
    }

    /**
     * Return the type of the last scanned comment.
     * @return type of last scanned comment, or 0 if none have been scanned.
     */
    public Token.CommentType getCommentType() {
        return commentType;
    }

    private void markCommentStart() {
        markCommentStart("");
    }

    private void markCommentStart(String prefix) {
        if (parser.compilerEnv.isRecordingComments() && sourceReader != null) {
            commentPrefix = prefix;
            commentCursor = sourceCursor - 1;
        }
    }

    private boolean isMarkingComment() {
        return commentCursor != -1;
    }

    final String getAndResetCurrentComment() {
        if (sourceString != null) {
            if (isMarkingComment()) Kit.codeBug();
            return sourceString.substring(tokenBeg, tokenEnd);
        } else {
            if (!isMarkingComment()) Kit.codeBug();
            StringBuilder comment = new StringBuilder(commentPrefix);
            comment.append(sourceBuffer, commentCursor, getTokenLength() - commentPrefix.length());
            commentCursor = -1;
            return comment.toString();
        }
    }

    private boolean dirtyLine;

    String regExpFlags;

    private String string = "";

    private double number;

    private boolean isOctal;

    private int quoteChar;

    private char[] stringBuffer = new char[128];

    private int stringBufferTop;

    private ObjToIntMap allStrings = new ObjToIntMap(50);

    private final int[] ungetBuffer = new int[3];

    private int ungetCursor;

    private boolean hitEOF = false;

    private int lineStart = 0;

    private int lineEndChar = -1;

    int lineno;

    private String sourceString;

    private Reader sourceReader;

    private char[] sourceBuffer;

    private int sourceEnd;

    int sourceCursor;

    int cursor;

    int tokenBeg;

    int tokenEnd;

    Token.CommentType commentType;

    private boolean xmlIsAttribute;

    private boolean xmlIsTagContent;

    private int xmlOpenTagsCount;

    private Parser parser;

    private String commentPrefix = "";

    private int commentCursor = -1;
}

package iwork.patchpanel.manager.script.parser;

import iwork.patchpanel.manager.script.lexer.*;
import iwork.patchpanel.manager.script.node.*;
import iwork.patchpanel.manager.script.analysis.*;
import java.util.*;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

public class Parser {

    public final Analysis ignoredTokens = new AnalysisAdapter();

    protected Node node;

    private final Lexer lexer;

    private final ListIterator stack = new LinkedList().listIterator();

    private int last_shift;

    private int last_pos;

    private int last_line;

    private Token last_token;

    private final TokenIndex converter = new TokenIndex();

    private final int[] action = new int[2];

    private static final int SHIFT = 0;

    private static final int REDUCE = 1;

    private static final int ACCEPT = 2;

    private static final int ERROR = 3;

    protected void filter() throws ParserException, LexerException, IOException {
    }

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        if (actionTable == null) {
            try {
                DataInputStream s = new DataInputStream(new BufferedInputStream(Parser.class.getResourceAsStream("parser.dat")));
                int length = s.readInt();
                actionTable = new int[length][][];
                for (int i = 0; i < actionTable.length; i++) {
                    length = s.readInt();
                    actionTable[i] = new int[length][3];
                    for (int j = 0; j < actionTable[i].length; j++) {
                        for (int k = 0; k < 3; k++) {
                            actionTable[i][j][k] = s.readInt();
                        }
                    }
                }
                length = s.readInt();
                gotoTable = new int[length][][];
                for (int i = 0; i < gotoTable.length; i++) {
                    length = s.readInt();
                    gotoTable[i] = new int[length][2];
                    for (int j = 0; j < gotoTable[i].length; j++) {
                        for (int k = 0; k < 2; k++) {
                            gotoTable[i][j][k] = s.readInt();
                        }
                    }
                }
                length = s.readInt();
                errorMessages = new String[length];
                for (int i = 0; i < errorMessages.length; i++) {
                    length = s.readInt();
                    StringBuffer buffer = new StringBuffer();
                    for (int j = 0; j < length; j++) {
                        buffer.append(s.readChar());
                    }
                    errorMessages[i] = buffer.toString();
                }
                length = s.readInt();
                errors = new int[length];
                for (int i = 0; i < errors.length; i++) {
                    errors[i] = s.readInt();
                }
                s.close();
            } catch (Exception e) {
                throw new RuntimeException("The file \"parser.dat\" is either missing or corrupted.");
            }
        }
    }

    private int goTo(int index) {
        int state = state();
        int low = 1;
        int high = gotoTable[index].length - 1;
        int value = gotoTable[index][0][1];
        while (low <= high) {
            int middle = (low + high) / 2;
            if (state < gotoTable[index][middle][0]) {
                high = middle - 1;
            } else if (state > gotoTable[index][middle][0]) {
                low = middle + 1;
            } else {
                value = gotoTable[index][middle][1];
                break;
            }
        }
        return value;
    }

    private void push(int state, Node node, boolean filter) throws ParserException, LexerException, IOException {
        this.node = node;
        if (filter) {
            filter();
        }
        if (!stack.hasNext()) {
            stack.add(new State(state, this.node));
            return;
        }
        State s = (State) stack.next();
        s.state = state;
        s.node = this.node;
    }

    private int state() {
        State s = (State) stack.previous();
        stack.next();
        return s.state;
    }

    private Node pop() {
        return (Node) ((State) stack.previous()).node;
    }

    private int index(Switchable token) {
        converter.index = -1;
        token.apply(converter);
        return converter.index;
    }

    public Start parse() throws ParserException, LexerException, IOException {
        push(0, null, false);
        List ign = null;
        while (true) {
            while (index(lexer.peek()) == -1) {
                if (ign == null) {
                    ign = new TypedLinkedList(NodeCast.instance);
                }
                ign.add(lexer.next());
            }
            if (ign != null) {
                ignoredTokens.setIn(lexer.peek(), ign);
                ign = null;
            }
            last_pos = lexer.peek().getPos();
            last_line = lexer.peek().getLine();
            last_token = lexer.peek();
            int index = index(lexer.peek());
            action[0] = actionTable[state()][0][1];
            action[1] = actionTable[state()][0][2];
            int low = 1;
            int high = actionTable[state()].length - 1;
            while (low <= high) {
                int middle = (low + high) / 2;
                if (index < actionTable[state()][middle][0]) {
                    high = middle - 1;
                } else if (index > actionTable[state()][middle][0]) {
                    low = middle + 1;
                } else {
                    action[0] = actionTable[state()][middle][1];
                    action[1] = actionTable[state()][middle][2];
                    break;
                }
            }
            switch(action[0]) {
                case SHIFT:
                    push(action[1], lexer.next(), true);
                    last_shift = action[1];
                    break;
                case REDUCE:
                    switch(action[1]) {
                        case 0:
                            {
                                Node node = new0();
                                push(goTo(0), node, true);
                            }
                            break;
                        case 1:
                            {
                                Node node = new1();
                                push(goTo(0), node, true);
                            }
                            break;
                        case 2:
                            {
                                Node node = new2();
                                push(goTo(61), node, false);
                            }
                            break;
                        case 3:
                            {
                                Node node = new3();
                                push(goTo(61), node, false);
                            }
                            break;
                        case 4:
                            {
                                Node node = new4();
                                push(goTo(1), node, true);
                            }
                            break;
                        case 5:
                            {
                                Node node = new5();
                                push(goTo(2), node, true);
                            }
                            break;
                        case 6:
                            {
                                Node node = new6();
                                push(goTo(2), node, true);
                            }
                            break;
                        case 7:
                            {
                                Node node = new7();
                                push(goTo(3), node, true);
                            }
                            break;
                        case 8:
                            {
                                Node node = new8();
                                push(goTo(3), node, true);
                            }
                            break;
                        case 9:
                            {
                                Node node = new9();
                                push(goTo(3), node, true);
                            }
                            break;
                        case 10:
                            {
                                Node node = new10();
                                push(goTo(4), node, true);
                            }
                            break;
                        case 11:
                            {
                                Node node = new11();
                                push(goTo(5), node, true);
                            }
                            break;
                        case 12:
                            {
                                Node node = new12();
                                push(goTo(5), node, true);
                            }
                            break;
                        case 13:
                            {
                                Node node = new13();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 14:
                            {
                                Node node = new14();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 15:
                            {
                                Node node = new15();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 16:
                            {
                                Node node = new16();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 17:
                            {
                                Node node = new17();
                                push(goTo(62), node, false);
                            }
                            break;
                        case 18:
                            {
                                Node node = new18();
                                push(goTo(62), node, false);
                            }
                            break;
                        case 19:
                            {
                                Node node = new19();
                                push(goTo(8), node, true);
                            }
                            break;
                        case 20:
                            {
                                Node node = new20();
                                push(goTo(9), node, true);
                            }
                            break;
                        case 21:
                            {
                                Node node = new21();
                                push(goTo(9), node, true);
                            }
                            break;
                        case 22:
                            {
                                Node node = new22();
                                push(goTo(10), node, true);
                            }
                            break;
                        case 23:
                            {
                                Node node = new23();
                                push(goTo(11), node, true);
                            }
                            break;
                        case 24:
                            {
                                Node node = new24();
                                push(goTo(11), node, true);
                            }
                            break;
                        case 25:
                            {
                                Node node = new25();
                                push(goTo(12), node, true);
                            }
                            break;
                        case 26:
                            {
                                Node node = new26();
                                push(goTo(12), node, true);
                            }
                            break;
                        case 27:
                            {
                                Node node = new27();
                                push(goTo(13), node, true);
                            }
                            break;
                        case 28:
                            {
                                Node node = new28();
                                push(goTo(14), node, true);
                            }
                            break;
                        case 29:
                            {
                                Node node = new29();
                                push(goTo(14), node, true);
                            }
                            break;
                        case 30:
                            {
                                Node node = new30();
                                push(goTo(14), node, true);
                            }
                            break;
                        case 31:
                            {
                                Node node = new31();
                                push(goTo(15), node, true);
                            }
                            break;
                        case 32:
                            {
                                Node node = new32();
                                push(goTo(15), node, true);
                            }
                            break;
                        case 33:
                            {
                                Node node = new33();
                                push(goTo(15), node, true);
                            }
                            break;
                        case 34:
                            {
                                Node node = new34();
                                push(goTo(16), node, true);
                            }
                            break;
                        case 35:
                            {
                                Node node = new35();
                                push(goTo(16), node, true);
                            }
                            break;
                        case 36:
                            {
                                Node node = new36();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 37:
                            {
                                Node node = new37();
                                push(goTo(18), node, true);
                            }
                            break;
                        case 38:
                            {
                                Node node = new38();
                                push(goTo(18), node, true);
                            }
                            break;
                        case 39:
                            {
                                Node node = new39();
                                push(goTo(19), node, true);
                            }
                            break;
                        case 40:
                            {
                                Node node = new40();
                                push(goTo(20), node, true);
                            }
                            break;
                        case 41:
                            {
                                Node node = new41();
                                push(goTo(20), node, true);
                            }
                            break;
                        case 42:
                            {
                                Node node = new42();
                                push(goTo(63), node, false);
                            }
                            break;
                        case 43:
                            {
                                Node node = new43();
                                push(goTo(63), node, false);
                            }
                            break;
                        case 44:
                            {
                                Node node = new44();
                                push(goTo(21), node, true);
                            }
                            break;
                        case 45:
                            {
                                Node node = new45();
                                push(goTo(21), node, true);
                            }
                            break;
                        case 46:
                            {
                                Node node = new46();
                                push(goTo(21), node, true);
                            }
                            break;
                        case 47:
                            {
                                Node node = new47();
                                push(goTo(22), node, true);
                            }
                            break;
                        case 48:
                            {
                                Node node = new48();
                                push(goTo(23), node, true);
                            }
                            break;
                        case 49:
                            {
                                Node node = new49();
                                push(goTo(23), node, true);
                            }
                            break;
                        case 50:
                            {
                                Node node = new50();
                                push(goTo(24), node, true);
                            }
                            break;
                        case 51:
                            {
                                Node node = new51();
                                push(goTo(24), node, true);
                            }
                            break;
                        case 52:
                            {
                                Node node = new52();
                                push(goTo(64), node, false);
                            }
                            break;
                        case 53:
                            {
                                Node node = new53();
                                push(goTo(64), node, false);
                            }
                            break;
                        case 54:
                            {
                                Node node = new54();
                                push(goTo(25), node, true);
                            }
                            break;
                        case 55:
                            {
                                Node node = new55();
                                push(goTo(25), node, true);
                            }
                            break;
                        case 56:
                            {
                                Node node = new56();
                                push(goTo(25), node, true);
                            }
                            break;
                        case 57:
                            {
                                Node node = new57();
                                push(goTo(25), node, true);
                            }
                            break;
                        case 58:
                            {
                                Node node = new58();
                                push(goTo(25), node, true);
                            }
                            break;
                        case 59:
                            {
                                Node node = new59();
                                push(goTo(25), node, true);
                            }
                            break;
                        case 60:
                            {
                                Node node = new60();
                                push(goTo(25), node, true);
                            }
                            break;
                        case 61:
                            {
                                Node node = new61();
                                push(goTo(25), node, true);
                            }
                            break;
                        case 62:
                            {
                                Node node = new62();
                                push(goTo(26), node, true);
                            }
                            break;
                        case 63:
                            {
                                Node node = new63();
                                push(goTo(27), node, true);
                            }
                            break;
                        case 64:
                            {
                                Node node = new64();
                                push(goTo(28), node, true);
                            }
                            break;
                        case 65:
                            {
                                Node node = new65();
                                push(goTo(29), node, true);
                            }
                            break;
                        case 66:
                            {
                                Node node = new66();
                                push(goTo(29), node, true);
                            }
                            break;
                        case 67:
                            {
                                Node node = new67();
                                push(goTo(30), node, true);
                            }
                            break;
                        case 68:
                            {
                                Node node = new68();
                                push(goTo(31), node, true);
                            }
                            break;
                        case 69:
                            {
                                Node node = new69();
                                push(goTo(32), node, true);
                            }
                            break;
                        case 70:
                            {
                                Node node = new70();
                                push(goTo(32), node, true);
                            }
                            break;
                        case 71:
                            {
                                Node node = new71();
                                push(goTo(65), node, false);
                            }
                            break;
                        case 72:
                            {
                                Node node = new72();
                                push(goTo(65), node, false);
                            }
                            break;
                        case 73:
                            {
                                Node node = new73();
                                push(goTo(32), node, true);
                            }
                            break;
                        case 74:
                            {
                                Node node = new74();
                                push(goTo(66), node, false);
                            }
                            break;
                        case 75:
                            {
                                Node node = new75();
                                push(goTo(66), node, false);
                            }
                            break;
                        case 76:
                            {
                                Node node = new76();
                                push(goTo(32), node, true);
                            }
                            break;
                        case 77:
                            {
                                Node node = new77();
                                push(goTo(33), node, true);
                            }
                            break;
                        case 78:
                            {
                                Node node = new78();
                                push(goTo(34), node, true);
                            }
                            break;
                        case 79:
                            {
                                Node node = new79();
                                push(goTo(34), node, true);
                            }
                            break;
                        case 80:
                            {
                                Node node = new80();
                                push(goTo(35), node, true);
                            }
                            break;
                        case 81:
                            {
                                Node node = new81();
                                push(goTo(35), node, true);
                            }
                            break;
                        case 82:
                            {
                                Node node = new82();
                                push(goTo(36), node, true);
                            }
                            break;
                        case 83:
                            {
                                Node node = new83();
                                push(goTo(36), node, true);
                            }
                            break;
                        case 84:
                            {
                                Node node = new84();
                                push(goTo(37), node, true);
                            }
                            break;
                        case 85:
                            {
                                Node node = new85();
                                push(goTo(38), node, true);
                            }
                            break;
                        case 86:
                            {
                                Node node = new86();
                                push(goTo(39), node, true);
                            }
                            break;
                        case 87:
                            {
                                Node node = new87();
                                push(goTo(39), node, true);
                            }
                            break;
                        case 88:
                            {
                                Node node = new88();
                                push(goTo(40), node, true);
                            }
                            break;
                        case 89:
                            {
                                Node node = new89();
                                push(goTo(41), node, true);
                            }
                            break;
                        case 90:
                            {
                                Node node = new90();
                                push(goTo(42), node, true);
                            }
                            break;
                        case 91:
                            {
                                Node node = new91();
                                push(goTo(42), node, true);
                            }
                            break;
                        case 92:
                            {
                                Node node = new92();
                                push(goTo(43), node, true);
                            }
                            break;
                        case 93:
                            {
                                Node node = new93();
                                push(goTo(43), node, true);
                            }
                            break;
                        case 94:
                            {
                                Node node = new94();
                                push(goTo(44), node, true);
                            }
                            break;
                        case 95:
                            {
                                Node node = new95();
                                push(goTo(44), node, true);
                            }
                            break;
                        case 96:
                            {
                                Node node = new96();
                                push(goTo(45), node, true);
                            }
                            break;
                        case 97:
                            {
                                Node node = new97();
                                push(goTo(46), node, true);
                            }
                            break;
                        case 98:
                            {
                                Node node = new98();
                                push(goTo(46), node, true);
                            }
                            break;
                        case 99:
                            {
                                Node node = new99();
                                push(goTo(46), node, true);
                            }
                            break;
                        case 100:
                            {
                                Node node = new100();
                                push(goTo(46), node, true);
                            }
                            break;
                        case 101:
                            {
                                Node node = new101();
                                push(goTo(47), node, true);
                            }
                            break;
                        case 102:
                            {
                                Node node = new102();
                                push(goTo(47), node, true);
                            }
                            break;
                        case 103:
                            {
                                Node node = new103();
                                push(goTo(48), node, true);
                            }
                            break;
                        case 104:
                            {
                                Node node = new104();
                                push(goTo(48), node, true);
                            }
                            break;
                        case 105:
                            {
                                Node node = new105();
                                push(goTo(48), node, true);
                            }
                            break;
                        case 106:
                            {
                                Node node = new106();
                                push(goTo(48), node, true);
                            }
                            break;
                        case 107:
                            {
                                Node node = new107();
                                push(goTo(49), node, true);
                            }
                            break;
                        case 108:
                            {
                                Node node = new108();
                                push(goTo(50), node, true);
                            }
                            break;
                        case 109:
                            {
                                Node node = new109();
                                push(goTo(51), node, true);
                            }
                            break;
                        case 110:
                            {
                                Node node = new110();
                                push(goTo(51), node, true);
                            }
                            break;
                        case 111:
                            {
                                Node node = new111();
                                push(goTo(52), node, true);
                            }
                            break;
                        case 112:
                            {
                                Node node = new112();
                                push(goTo(52), node, true);
                            }
                            break;
                        case 113:
                            {
                                Node node = new113();
                                push(goTo(52), node, true);
                            }
                            break;
                        case 114:
                            {
                                Node node = new114();
                                push(goTo(52), node, true);
                            }
                            break;
                        case 115:
                            {
                                Node node = new115();
                                push(goTo(53), node, true);
                            }
                            break;
                        case 116:
                            {
                                Node node = new116();
                                push(goTo(53), node, true);
                            }
                            break;
                        case 117:
                            {
                                Node node = new117();
                                push(goTo(53), node, true);
                            }
                            break;
                        case 118:
                            {
                                Node node = new118();
                                push(goTo(53), node, true);
                            }
                            break;
                        case 119:
                            {
                                Node node = new119();
                                push(goTo(54), node, true);
                            }
                            break;
                        case 120:
                            {
                                Node node = new120();
                                push(goTo(54), node, true);
                            }
                            break;
                        case 121:
                            {
                                Node node = new121();
                                push(goTo(54), node, true);
                            }
                            break;
                        case 122:
                            {
                                Node node = new122();
                                push(goTo(54), node, true);
                            }
                            break;
                        case 123:
                            {
                                Node node = new123();
                                push(goTo(55), node, true);
                            }
                            break;
                        case 124:
                            {
                                Node node = new124();
                                push(goTo(56), node, true);
                            }
                            break;
                        case 125:
                            {
                                Node node = new125();
                                push(goTo(57), node, true);
                            }
                            break;
                        case 126:
                            {
                                Node node = new126();
                                push(goTo(57), node, true);
                            }
                            break;
                        case 127:
                            {
                                Node node = new127();
                                push(goTo(58), node, true);
                            }
                            break;
                        case 128:
                            {
                                Node node = new128();
                                push(goTo(58), node, true);
                            }
                            break;
                        case 129:
                            {
                                Node node = new129();
                                push(goTo(58), node, true);
                            }
                            break;
                        case 130:
                            {
                                Node node = new130();
                                push(goTo(59), node, true);
                            }
                            break;
                        case 131:
                            {
                                Node node = new131();
                                push(goTo(59), node, true);
                            }
                            break;
                        case 132:
                            {
                                Node node = new132();
                                push(goTo(60), node, true);
                            }
                            break;
                        case 133:
                            {
                                Node node = new133();
                                push(goTo(60), node, true);
                            }
                            break;
                        case 134:
                            {
                                Node node = new134();
                                push(goTo(60), node, true);
                            }
                            break;
                    }
                    break;
                case ACCEPT:
                    {
                        EOF node2 = (EOF) lexer.next();
                        PScript node1 = (PScript) pop();
                        Start node = new Start(node1, node2);
                        return node;
                    }
                case ERROR:
                    throw new ParserException(last_token, "[" + last_line + "," + last_pos + "] " + errorMessages[errors[action[1]]]);
            }
        }
    }

    Node new0() {
        XPBodyLine node2 = null;
        PHeadLine node1 = (PHeadLine) pop();
        AScript node = new AScript(node1, node2);
        return node;
    }

    Node new1() {
        XPBodyLine node2 = (XPBodyLine) pop();
        PHeadLine node1 = (PHeadLine) pop();
        AScript node = new AScript(node1, node2);
        return node;
    }

    Node new2() {
        PBodyLine node2 = (PBodyLine) pop();
        XPBodyLine node1 = (XPBodyLine) pop();
        X1PBodyLine node = new X1PBodyLine(node1, node2);
        return node;
    }

    Node new3() {
        PBodyLine node1 = (PBodyLine) pop();
        X2PBodyLine node = new X2PBodyLine(node1);
        return node;
    }

    Node new4() {
        TSemicolon node3 = (TSemicolon) pop();
        PGroupname node2 = (PGroupname) pop();
        TGroup node1 = (TGroup) pop();
        AHeadLine node = new AHeadLine(node1, node2, node3);
        return node;
    }

    Node new5() {
        PNameWithoutSpace node1 = (PNameWithoutSpace) pop();
        ANospaceGroupname node = new ANospaceGroupname(node1);
        return node;
    }

    Node new6() {
        PNameWithSpace node1 = (PNameWithSpace) pop();
        AWithspaceGroupname node = new AWithspaceGroupname(node1);
        return node;
    }

    Node new7() {
        PEventDefLine node1 = (PEventDefLine) pop();
        AEventBodyLine node = new AEventBodyLine(node1);
        return node;
    }

    Node new8() {
        PStateDefLine node1 = (PStateDefLine) pop();
        AStateBodyLine node = new AStateBodyLine(node1);
        return node;
    }

    Node new9() {
        PVarDefLine node1 = (PVarDefLine) pop();
        AVarBodyLine node = new AVarBodyLine(node1);
        return node;
    }

    Node new10() {
        PEventBlock node5 = (PEventBlock) pop();
        PTypeName node4 = (PTypeName) pop();
        TType node3 = (TType) pop();
        PEventName node2 = (PEventName) pop();
        TEvent node1 = (TEvent) pop();
        AEventDefLine node = new AEventDefLine(node1, node2, node3, node4, node5);
        return node;
    }

    Node new11() {
        PNameWithoutSpace node1 = (PNameWithoutSpace) pop();
        ANospaceEventName node = new ANospaceEventName(node1);
        return node;
    }

    Node new12() {
        PNameWithSpace node1 = (PNameWithSpace) pop();
        AWithspaceEventName node = new AWithspaceEventName(node1);
        return node;
    }

    Node new13() {
        PNameWithoutSpace node1 = (PNameWithoutSpace) pop();
        ANospaceTypeName node = new ANospaceTypeName(node1);
        return node;
    }

    Node new14() {
        PNameWithSpace node1 = (PNameWithSpace) pop();
        AWithspaceTypeName node = new AWithspaceTypeName(node1);
        return node;
    }

    Node new15() {
        TRBrace node3 = (TRBrace) pop();
        XPFieldDefLine node2 = null;
        TLBrace node1 = (TLBrace) pop();
        AEventBlock node = new AEventBlock(node1, node2, node3);
        return node;
    }

    Node new16() {
        TRBrace node3 = (TRBrace) pop();
        XPFieldDefLine node2 = (XPFieldDefLine) pop();
        TLBrace node1 = (TLBrace) pop();
        AEventBlock node = new AEventBlock(node1, node2, node3);
        return node;
    }

    Node new17() {
        PFieldDefLine node2 = (PFieldDefLine) pop();
        XPFieldDefLine node1 = (XPFieldDefLine) pop();
        X1PFieldDefLine node = new X1PFieldDefLine(node1, node2);
        return node;
    }

    Node new18() {
        PFieldDefLine node1 = (PFieldDefLine) pop();
        X2PFieldDefLine node = new X2PFieldDefLine(node1);
        return node;
    }

    Node new19() {
        TSemicolon node4 = (TSemicolon) pop();
        PRhs node3 = (PRhs) pop();
        TAssign node2 = (TAssign) pop();
        PLhs node1 = (PLhs) pop();
        AFieldDefLine node = new AFieldDefLine(node1, node2, node3, node4);
        return node;
    }

    Node new20() {
        PFieldName node2 = (PFieldName) pop();
        PType node1 = null;
        ALhs node = new ALhs(node1, node2);
        return node;
    }

    Node new21() {
        PFieldName node2 = (PFieldName) pop();
        PType node1 = (PType) pop();
        ALhs node = new ALhs(node1, node2);
        return node;
    }

    Node new22() {
        PValExpressionWithTemplateVal node1 = (PValExpressionWithTemplateVal) pop();
        ARhs node = new ARhs(node1);
        return node;
    }

    Node new23() {
        PNameWithoutSpace node1 = (PNameWithoutSpace) pop();
        ANospaceFieldName node = new ANospaceFieldName(node1);
        return node;
    }

    Node new24() {
        PNameWithSpace node1 = (PNameWithSpace) pop();
        AWithspaceFieldName node = new AWithspaceFieldName(node1);
        return node;
    }

    Node new25() {
        PFieldTemplateDef node2 = null;
        PValExpression node1 = (PValExpression) pop();
        AValExpressionWithTemplateVal node = new AValExpressionWithTemplateVal(node1, node2);
        return node;
    }

    Node new26() {
        PFieldTemplateDef node2 = (PFieldTemplateDef) pop();
        PValExpression node1 = (PValExpression) pop();
        AValExpressionWithTemplateVal node = new AValExpressionWithTemplateVal(node1, node2);
        return node;
    }

    Node new27() {
        PFieldTemplateType node2 = (PFieldTemplateType) pop();
        TComma node1 = (TComma) pop();
        AFieldTemplateDef node = new AFieldTemplateDef(node1, node2);
        return node;
    }

    Node new28() {
        TFormal node1 = (TFormal) pop();
        AFormalFieldTemplateType node = new AFormalFieldTemplateType(node1);
        return node;
    }

    Node new29() {
        TVirtual node1 = (TVirtual) pop();
        AVirtualFieldTemplateType node = new AVirtualFieldTemplateType(node1);
        return node;
    }

    Node new30() {
        TActual node1 = (TActual) pop();
        AActualFieldTemplateType node = new AActualFieldTemplateType(node1);
        return node;
    }

    Node new31() {
        PNameWithoutSpace node1 = (PNameWithoutSpace) pop();
        ANospaceValExpression node = new ANospaceValExpression(node1);
        return node;
    }

    Node new32() {
        PNameWithSpace node1 = (PNameWithSpace) pop();
        AWithspaceValExpression node = new AWithspaceValExpression(node1);
        return node;
    }

    Node new33() {
        PConstantExpression node1 = (PConstantExpression) pop();
        AConstantExpValExpression node = new AConstantExpValExpression(node1);
        return node;
    }

    Node new34() {
        TSemicolon node4 = (TSemicolon) pop();
        PRhsVar node3 = (PRhsVar) pop();
        TAssign node2 = (TAssign) pop();
        PLhs node1 = (PLhs) pop();
        AVarVarDefLine node = new AVarVarDefLine(node1, node2, node3, node4);
        return node;
    }

    Node new35() {
        TSemicolon node3 = (TSemicolon) pop();
        PTimerName node2 = (PTimerName) pop();
        TTimer node1 = (TTimer) pop();
        ATimerVarDefLine node = new ATimerVarDefLine(node1, node2, node3);
        return node;
    }

    Node new36() {
        PValExpression node1 = (PValExpression) pop();
        ARhsVar node = new ARhsVar(node1);
        return node;
    }

    Node new37() {
        PStateBlock node4 = (PStateBlock) pop();
        PStateName node3 = (PStateName) pop();
        TState node2 = (TState) pop();
        TInitial node1 = null;
        AStateDefLine node = new AStateDefLine(node1, node2, node3, node4);
        return node;
    }

    Node new38() {
        PStateBlock node4 = (PStateBlock) pop();
        PStateName node3 = (PStateName) pop();
        TState node2 = (TState) pop();
        TInitial node1 = (TInitial) pop();
        AStateDefLine node = new AStateDefLine(node1, node2, node3, node4);
        return node;
    }

    Node new39() {
        PNameWithoutSpace node1 = (PNameWithoutSpace) pop();
        AStateName node = new AStateName(node1);
        return node;
    }

    Node new40() {
        TRBrace node3 = (TRBrace) pop();
        XPInnerStateLine node2 = null;
        TLBrace node1 = (TLBrace) pop();
        AStateBlock node = new AStateBlock(node1, node2, node3);
        return node;
    }

    Node new41() {
        TRBrace node3 = (TRBrace) pop();
        XPInnerStateLine node2 = (XPInnerStateLine) pop();
        TLBrace node1 = (TLBrace) pop();
        AStateBlock node = new AStateBlock(node1, node2, node3);
        return node;
    }

    Node new42() {
        PInnerStateLine node2 = (PInnerStateLine) pop();
        XPInnerStateLine node1 = (XPInnerStateLine) pop();
        X1PInnerStateLine node = new X1PInnerStateLine(node1, node2);
        return node;
    }

    Node new43() {
        PInnerStateLine node1 = (PInnerStateLine) pop();
        X2PInnerStateLine node = new X2PInnerStateLine(node1);
        return node;
    }

    Node new44() {
        PVarDefLine node1 = (PVarDefLine) pop();
        AVarInnerStateLine node = new AVarInnerStateLine(node1);
        return node;
    }

    Node new45() {
        PEventDefLine node1 = (PEventDefLine) pop();
        AEventInnerStateLine node = new AEventInnerStateLine(node1);
        return node;
    }

    Node new46() {
        PActionDefLine node1 = (PActionDefLine) pop();
        AActionInnerStateLine node = new AActionInnerStateLine(node1);
        return node;
    }

    Node new47() {
        PActionBlock node3 = (PActionBlock) pop();
        PActionType node2 = (PActionType) pop();
        TOn node1 = (TOn) pop();
        AActionDefLine node = new AActionDefLine(node1, node2, node3);
        return node;
    }

    Node new48() {
        PNameWithoutSpace node1 = (PNameWithoutSpace) pop();
        AUserDefActionType node = new AUserDefActionType(node1);
        return node;
    }

    Node new49() {
        TEnter node1 = (TEnter) pop();
        AEnterActionType node = new AEnterActionType(node1);
        return node;
    }

    Node new50() {
        TRBrace node3 = (TRBrace) pop();
        XPActionLine node2 = null;
        TLBrace node1 = (TLBrace) pop();
        AActionBlock node = new AActionBlock(node1, node2, node3);
        return node;
    }

    Node new51() {
        TRBrace node3 = (TRBrace) pop();
        XPActionLine node2 = (XPActionLine) pop();
        TLBrace node1 = (TLBrace) pop();
        AActionBlock node = new AActionBlock(node1, node2, node3);
        return node;
    }

    Node new52() {
        PActionLine node2 = (PActionLine) pop();
        XPActionLine node1 = (XPActionLine) pop();
        X1PActionLine node = new X1PActionLine(node1, node2);
        return node;
    }

    Node new53() {
        PActionLine node1 = (PActionLine) pop();
        X2PActionLine node = new X2PActionLine(node1);
        return node;
    }

    Node new54() {
        PSendStatement node1 = (PSendStatement) pop();
        ASendStmtActionLine node = new ASendStmtActionLine(node1);
        return node;
    }

    Node new55() {
        PGotoStatement node1 = (PGotoStatement) pop();
        AGotoStmtActionLine node = new AGotoStmtActionLine(node1);
        return node;
    }

    Node new56() {
        PCancelStatement node1 = (PCancelStatement) pop();
        ACancelStmtActionLine node = new ACancelStmtActionLine(node1);
        return node;
    }

    Node new57() {
        PSetStatement node1 = (PSetStatement) pop();
        ASetStmtActionLine node = new ASetStmtActionLine(node1);
        return node;
    }

    Node new58() {
        PSwitchStatement node1 = (PSwitchStatement) pop();
        ASwitchStmtActionLine node = new ASwitchStmtActionLine(node1);
        return node;
    }

    Node new59() {
        PIfElseStatement node1 = (PIfElseStatement) pop();
        AIfElseStmtActionLine node = new AIfElseStmtActionLine(node1);
        return node;
    }

    Node new60() {
        PCompoundStatement node1 = (PCompoundStatement) pop();
        ACompoundActionLine node = new ACompoundActionLine(node1);
        return node;
    }

    Node new61() {
        PBasicStatement node1 = (PBasicStatement) pop();
        ABasicStmtActionLine node = new ABasicStmtActionLine(node1);
        return node;
    }

    Node new62() {
        TSemicolon node3 = (TSemicolon) pop();
        PEventName node2 = (PEventName) pop();
        TSend node1 = (TSend) pop();
        ASendStatement node = new ASendStatement(node1, node2, node3);
        return node;
    }

    Node new63() {
        TSemicolon node3 = (TSemicolon) pop();
        PStateName node2 = (PStateName) pop();
        TGoto node1 = (TGoto) pop();
        AGotoStatement node = new AGotoStatement(node1, node2, node3);
        return node;
    }

    Node new64() {
        TSemicolon node3 = (TSemicolon) pop();
        PTimerName node2 = (PTimerName) pop();
        TCancel node1 = (TCancel) pop();
        ACancelStatement node = new ACancelStatement(node1, node2, node3);
        return node;
    }

    Node new65() {
        TSemicolon node4 = (TSemicolon) pop();
        TIntegerLiteral node3 = (TIntegerLiteral) pop();
        PTimerName node2 = (PTimerName) pop();
        TSet node1 = (TSet) pop();
        ALiteralSetStatement node = new ALiteralSetStatement(node1, node2, node3, node4);
        return node;
    }

    Node new66() {
        TSemicolon node4 = (TSemicolon) pop();
        PFieldName node3 = (PFieldName) pop();
        PTimerName node2 = (PTimerName) pop();
        TSet node1 = (TSet) pop();
        AVariableSetStatement node = new AVariableSetStatement(node1, node2, node3, node4);
        return node;
    }

    Node new67() {
        PNameWithoutSpace node1 = (PNameWithoutSpace) pop();
        ATimerName node = new ATimerName(node1);
        return node;
    }

    Node new68() {
        PSwitchBlock node6 = (PSwitchBlock) pop();
        TRParenthese node5 = (TRParenthese) pop();
        PExpression node4 = (PExpression) pop();
        TLParenthese node3 = (TLParenthese) pop();
        PType node2 = (PType) pop();
        TSwitch node1 = (TSwitch) pop();
        ASwitchStatement node = new ASwitchStatement(node1, node2, node3, node4, node5, node6);
        return node;
    }

    Node new69() {
        TRBrace node4 = (TRBrace) pop();
        XPSwitchLabel node3 = null;
        XPSwitchBlockStatementGroup node2 = null;
        TLBrace node1 = (TLBrace) pop();
        ASwitchBlock node = new ASwitchBlock(node1, node2, node3, node4);
        return node;
    }

    Node new70() {
        TRBrace node4 = (TRBrace) pop();
        XPSwitchLabel node3 = null;
        XPSwitchBlockStatementGroup node2 = (XPSwitchBlockStatementGroup) pop();
        TLBrace node1 = (TLBrace) pop();
        ASwitchBlock node = new ASwitchBlock(node1, node2, node3, node4);
        return node;
    }

    Node new71() {
        PSwitchBlockStatementGroup node2 = (PSwitchBlockStatementGroup) pop();
        XPSwitchBlockStatementGroup node1 = (XPSwitchBlockStatementGroup) pop();
        X1PSwitchBlockStatementGroup node = new X1PSwitchBlockStatementGroup(node1, node2);
        return node;
    }

    Node new72() {
        PSwitchBlockStatementGroup node1 = (PSwitchBlockStatementGroup) pop();
        X2PSwitchBlockStatementGroup node = new X2PSwitchBlockStatementGroup(node1);
        return node;
    }

    Node new73() {
        TRBrace node4 = (TRBrace) pop();
        XPSwitchLabel node3 = (XPSwitchLabel) pop();
        XPSwitchBlockStatementGroup node2 = null;
        TLBrace node1 = (TLBrace) pop();
        ASwitchBlock node = new ASwitchBlock(node1, node2, node3, node4);
        return node;
    }

    Node new74() {
        PSwitchLabel node2 = (PSwitchLabel) pop();
        XPSwitchLabel node1 = (XPSwitchLabel) pop();
        X1PSwitchLabel node = new X1PSwitchLabel(node1, node2);
        return node;
    }

    Node new75() {
        PSwitchLabel node1 = (PSwitchLabel) pop();
        X2PSwitchLabel node = new X2PSwitchLabel(node1);
        return node;
    }

    Node new76() {
        TRBrace node4 = (TRBrace) pop();
        XPSwitchLabel node3 = (XPSwitchLabel) pop();
        XPSwitchBlockStatementGroup node2 = (XPSwitchBlockStatementGroup) pop();
        TLBrace node1 = (TLBrace) pop();
        ASwitchBlock node = new ASwitchBlock(node1, node2, node3, node4);
        return node;
    }

    Node new77() {
        TRBrace node4 = (TRBrace) pop();
        XPActionLine node3 = (XPActionLine) pop();
        TLBrace node2 = (TLBrace) pop();
        XPSwitchLabel node1 = (XPSwitchLabel) pop();
        ASwitchBlockStatementGroup node = new ASwitchBlockStatementGroup(node1, node2, node3, node4);
        return node;
    }

    Node new78() {
        TColon node3 = (TColon) pop();
        PCaseExpression node2 = (PCaseExpression) pop();
        TCase node1 = (TCase) pop();
        ACaseExpSwitchLabel node = new ACaseExpSwitchLabel(node1, node2, node3);
        return node;
    }

    Node new79() {
        TColon node2 = (TColon) pop();
        TDefault node1 = (TDefault) pop();
        ADefaultSwitchLabel node = new ADefaultSwitchLabel(node1, node2);
        return node;
    }

    Node new80() {
        PConstantExpression node1 = (PConstantExpression) pop();
        AConstExpCaseExpression node = new AConstExpCaseExpression(node1);
        return node;
    }

    Node new81() {
        TStringLiteral node1 = (TStringLiteral) pop();
        AStrExpCaseExpression node = new AStrExpCaseExpression(node1);
        return node;
    }

    Node new82() {
        PElsePart node4 = null;
        PThenBlock node3 = (PThenBlock) pop();
        PCondition node2 = (PCondition) pop();
        TIf node1 = (TIf) pop();
        AIfElseStatement node = new AIfElseStatement(node1, node2, node3, node4);
        return node;
    }

    Node new83() {
        PElsePart node4 = (PElsePart) pop();
        PThenBlock node3 = (PThenBlock) pop();
        PCondition node2 = (PCondition) pop();
        TIf node1 = (TIf) pop();
        AIfElseStatement node = new AIfElseStatement(node1, node2, node3, node4);
        return node;
    }

    Node new84() {
        TRParenthese node3 = (TRParenthese) pop();
        PConditionalExpression node2 = (PConditionalExpression) pop();
        TLParenthese node1 = (TLParenthese) pop();
        ACondition node = new ACondition(node1, node2, node3);
        return node;
    }

    Node new85() {
        PIfOrElsePart node2 = (PIfOrElsePart) pop();
        TElse node1 = (TElse) pop();
        AElsePart node = new AElsePart(node1, node2);
        return node;
    }

    Node new86() {
        PIfElseStatement node1 = (PIfElseStatement) pop();
        AIfIfOrElsePart node = new AIfIfOrElsePart(node1);
        return node;
    }

    Node new87() {
        PElseBlock node1 = (PElseBlock) pop();
        AElseIfOrElsePart node = new AElseIfOrElsePart(node1);
        return node;
    }

    Node new88() {
        PCompoundStatement node1 = (PCompoundStatement) pop();
        AElseBlock node = new AElseBlock(node1);
        return node;
    }

    Node new89() {
        PCompoundStatement node1 = (PCompoundStatement) pop();
        AThenBlock node = new AThenBlock(node1);
        return node;
    }

    Node new90() {
        TRBrace node3 = (TRBrace) pop();
        XPActionLine node2 = null;
        TLBrace node1 = (TLBrace) pop();
        ACompoundStatement node = new ACompoundStatement(node1, node2, node3);
        return node;
    }

    Node new91() {
        TRBrace node3 = (TRBrace) pop();
        XPActionLine node2 = (XPActionLine) pop();
        TLBrace node1 = (TLBrace) pop();
        ACompoundStatement node = new ACompoundStatement(node1, node2, node3);
        return node;
    }

    Node new92() {
        TStringLiteral node1 = (TStringLiteral) pop();
        AVarExpConditionalExpression node = new AVarExpConditionalExpression(node1);
        return node;
    }

    Node new93() {
        PConstantExpression node1 = (PConstantExpression) pop();
        AConstExpConditionalExpression node = new AConstExpConditionalExpression(node1);
        return node;
    }

    Node new94() {
        TStringLiteral node1 = (TStringLiteral) pop();
        AVarExpExpression node = new AVarExpExpression(node1);
        return node;
    }

    Node new95() {
        PConstantExpression node1 = (PConstantExpression) pop();
        AConstExpExpression node = new AConstExpExpression(node1);
        return node;
    }

    Node new96() {
        PVarDefLine node1 = (PVarDefLine) pop();
        ABasicStatement node = new ABasicStatement(node1);
        return node;
    }

    Node new97() {
        PIdentifier node1 = (PIdentifier) pop();
        AVarVarName node = new AVarVarName(node1);
        return node;
    }

    Node new98() {
        PIdentifier node3 = (PIdentifier) pop();
        TDot node2 = (TDot) pop();
        TIn node1 = (TIn) pop();
        AInVarName node = new AInVarName(node1, node2, node3);
        return node;
    }

    Node new99() {
        PIdentifier node3 = (PIdentifier) pop();
        TDot node2 = (TDot) pop();
        TOut node1 = (TOut) pop();
        AOutVarName node = new AOutVarName(node1, node2, node3);
        return node;
    }

    Node new100() {
        PIdentifier node3 = (PIdentifier) pop();
        TDot node2 = (TDot) pop();
        TGlobal node1 = (TGlobal) pop();
        AGlobalVarName node = new AGlobalVarName(node1, node2, node3);
        return node;
    }

    Node new101() {
        PNameWithoutSpace node1 = (PNameWithoutSpace) pop();
        ANospaceIdentifier node = new ANospaceIdentifier(node1);
        return node;
    }

    Node new102() {
        PNameWithSpace node1 = (PNameWithSpace) pop();
        AWithspaceIdentifier node = new AWithspaceIdentifier(node1);
        return node;
    }

    Node new103() {
        PRange node1 = (PRange) pop();
        ARangeConstantExpression node = new ARangeConstantExpression(node1);
        return node;
    }

    Node new104() {
        PNumericalLiteral node1 = (PNumericalLiteral) pop();
        ANumericConstantExpression node = new ANumericConstantExpression(node1);
        return node;
    }

    Node new105() {
        PBooleanLiteral node1 = (PBooleanLiteral) pop();
        ABooleanConstantExpression node = new ABooleanConstantExpression(node1);
        return node;
    }

    Node new106() {
        TCharacterLiteral node1 = (TCharacterLiteral) pop();
        ACharacterConstantExpression node = new ACharacterConstantExpression(node1);
        return node;
    }

    Node new107() {
        TStringNormal node1 = (TStringNormal) pop();
        ANameWithoutSpace node = new ANameWithoutSpace(node1);
        return node;
    }

    Node new108() {
        TStringLiteral node1 = (TStringLiteral) pop();
        ANameWithSpace node = new ANameWithSpace(node1);
        return node;
    }

    Node new109() {
        TTrue node1 = (TTrue) pop();
        ATrueBooleanLiteral node = new ATrueBooleanLiteral(node1);
        return node;
    }

    Node new110() {
        TFalse node1 = (TFalse) pop();
        AFalseBooleanLiteral node = new AFalseBooleanLiteral(node1);
        return node;
    }

    Node new111() {
        TIntegerLiteral node2 = (TIntegerLiteral) pop();
        TMinus node1 = null;
        AIntegerLiteralNumericalLiteral node = new AIntegerLiteralNumericalLiteral(node1, node2);
        return node;
    }

    Node new112() {
        TIntegerLiteral node2 = (TIntegerLiteral) pop();
        TMinus node1 = (TMinus) pop();
        AIntegerLiteralNumericalLiteral node = new AIntegerLiteralNumericalLiteral(node1, node2);
        return node;
    }

    Node new113() {
        TFloatingPointLiteral node2 = (TFloatingPointLiteral) pop();
        TMinus node1 = null;
        AFloatingPointLiteralNumericalLiteral node = new AFloatingPointLiteralNumericalLiteral(node1, node2);
        return node;
    }

    Node new114() {
        TFloatingPointLiteral node2 = (TFloatingPointLiteral) pop();
        TMinus node1 = (TMinus) pop();
        AFloatingPointLiteralNumericalLiteral node = new AFloatingPointLiteralNumericalLiteral(node1, node2);
        return node;
    }

    Node new115() {
        PNumericalLiteral node1 = (PNumericalLiteral) pop();
        ANumericalLiteralLiteral node = new ANumericalLiteralLiteral(node1);
        return node;
    }

    Node new116() {
        PBooleanLiteral node1 = (PBooleanLiteral) pop();
        ABooleanLiteralLiteral node = new ABooleanLiteralLiteral(node1);
        return node;
    }

    Node new117() {
        TCharacterLiteral node1 = (TCharacterLiteral) pop();
        ACharacterLiteralLiteral node = new ACharacterLiteralLiteral(node1);
        return node;
    }

    Node new118() {
        TStringLiteral node1 = (TStringLiteral) pop();
        AStringLiteralLiteral node = new AStringLiteralLiteral(node1);
        return node;
    }

    Node new119() {
        TRParenthese node5 = (TRParenthese) pop();
        PUpperbound node4 = (PUpperbound) pop();
        TComma node3 = (TComma) pop();
        PLowerbound node2 = (PLowerbound) pop();
        TLParenthese node1 = (TLParenthese) pop();
        AExclExclRange node = new AExclExclRange(node1, node2, node3, node4, node5);
        return node;
    }

    Node new120() {
        TRBracket node5 = (TRBracket) pop();
        PUpperbound node4 = (PUpperbound) pop();
        TComma node3 = (TComma) pop();
        PLowerbound node2 = (PLowerbound) pop();
        TLParenthese node1 = (TLParenthese) pop();
        AExclInclRange node = new AExclInclRange(node1, node2, node3, node4, node5);
        return node;
    }

    Node new121() {
        TRParenthese node5 = (TRParenthese) pop();
        PUpperbound node4 = (PUpperbound) pop();
        TComma node3 = (TComma) pop();
        PLowerbound node2 = (PLowerbound) pop();
        TLBracket node1 = (TLBracket) pop();
        AInclExclRange node = new AInclExclRange(node1, node2, node3, node4, node5);
        return node;
    }

    Node new122() {
        TRBracket node5 = (TRBracket) pop();
        PUpperbound node4 = (PUpperbound) pop();
        TComma node3 = (TComma) pop();
        PLowerbound node2 = (PLowerbound) pop();
        TLBracket node1 = (TLBracket) pop();
        AInclInclRange node = new AInclInclRange(node1, node2, node3, node4, node5);
        return node;
    }

    Node new123() {
        PNumericalLiteral node1 = (PNumericalLiteral) pop();
        ALowerbound node = new ALowerbound(node1);
        return node;
    }

    Node new124() {
        PNumericalLiteral node1 = (PNumericalLiteral) pop();
        AUpperbound node = new AUpperbound(node1);
        return node;
    }

    Node new125() {
        TFloat node1 = (TFloat) pop();
        AFloatFloatingPointType node = new AFloatFloatingPointType(node1);
        return node;
    }

    Node new126() {
        TDouble node1 = (TDouble) pop();
        ADoubleFloatingPointType node = new ADoubleFloatingPointType(node1);
        return node;
    }

    Node new127() {
        TInt node1 = (TInt) pop();
        AIntIntegralType node = new AIntIntegralType(node1);
        return node;
    }

    Node new128() {
        TLong node1 = (TLong) pop();
        ALongIntegralType node = new ALongIntegralType(node1);
        return node;
    }

    Node new129() {
        TChar node1 = (TChar) pop();
        ACharIntegralType node = new ACharIntegralType(node1);
        return node;
    }

    Node new130() {
        PIntegralType node1 = (PIntegralType) pop();
        AIntegralTypeNumericType node = new AIntegralTypeNumericType(node1);
        return node;
    }

    Node new131() {
        PFloatingPointType node1 = (PFloatingPointType) pop();
        AFloatingPointTypeNumericType node = new AFloatingPointTypeNumericType(node1);
        return node;
    }

    Node new132() {
        PNumericType node1 = (PNumericType) pop();
        ANumericTypeType node = new ANumericTypeType(node1);
        return node;
    }

    Node new133() {
        TBoolean node1 = (TBoolean) pop();
        ABooleanType node = new ABooleanType(node1);
        return node;
    }

    Node new134() {
        TString node1 = (TString) pop();
        AStringType node = new AStringType(node1);
        return node;
    }

    private static int[][][] actionTable;

    private static int[][][] gotoTable;

    private static String[] errorMessages;

    private static int[] errors;
}

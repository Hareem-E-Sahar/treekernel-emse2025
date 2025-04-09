package saf.parser;

import saf.lexer.*;
import saf.node.*;
import saf.analysis.*;
import java.util.*;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

@SuppressWarnings("nls")
public class Parser {

    public final Analysis ignoredTokens = new AnalysisAdapter();

    protected ArrayList nodeList;

    private final Lexer lexer;

    private final ListIterator stack = new LinkedList().listIterator();

    private int last_pos;

    private int last_line;

    private Token last_token;

    private final TokenIndex converter = new TokenIndex();

    private final int[] action = new int[2];

    private static final int SHIFT = 0;

    private static final int REDUCE = 1;

    private static final int ACCEPT = 2;

    private static final int ERROR = 3;

    public Parser(@SuppressWarnings("hiding") Lexer lexer) {
        this.lexer = lexer;
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private void push(int numstate, ArrayList listNode) throws ParserException, LexerException, IOException {
        this.nodeList = listNode;
        if (!this.stack.hasNext()) {
            this.stack.add(new State(numstate, this.nodeList));
            return;
        }
        State s = (State) this.stack.next();
        s.state = numstate;
        s.nodes = this.nodeList;
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

    private int state() {
        State s = (State) this.stack.previous();
        this.stack.next();
        return s.state;
    }

    private ArrayList pop() {
        return ((State) this.stack.previous()).nodes;
    }

    private int index(Switchable token) {
        this.converter.index = -1;
        token.apply(this.converter);
        return this.converter.index;
    }

    @SuppressWarnings("unchecked")
    public Start parse() throws ParserException, LexerException, IOException {
        push(0, null);
        List<Node> ign = null;
        while (true) {
            while (index(this.lexer.peek()) == -1) {
                if (ign == null) {
                    ign = new LinkedList<Node>();
                }
                ign.add(this.lexer.next());
            }
            if (ign != null) {
                this.ignoredTokens.setIn(this.lexer.peek(), ign);
                ign = null;
            }
            this.last_pos = this.lexer.peek().getPos();
            this.last_line = this.lexer.peek().getLine();
            this.last_token = this.lexer.peek();
            int index = index(this.lexer.peek());
            this.action[0] = Parser.actionTable[state()][0][1];
            this.action[1] = Parser.actionTable[state()][0][2];
            int low = 1;
            int high = Parser.actionTable[state()].length - 1;
            while (low <= high) {
                int middle = (low + high) / 2;
                if (index < Parser.actionTable[state()][middle][0]) {
                    high = middle - 1;
                } else if (index > Parser.actionTable[state()][middle][0]) {
                    low = middle + 1;
                } else {
                    this.action[0] = Parser.actionTable[state()][middle][1];
                    this.action[1] = Parser.actionTable[state()][middle][2];
                    break;
                }
            }
            switch(this.action[0]) {
                case SHIFT:
                    {
                        ArrayList list = new ArrayList();
                        list.add(this.lexer.next());
                        push(this.action[1], list);
                    }
                    break;
                case REDUCE:
                    switch(this.action[1]) {
                        case 0:
                            {
                                ArrayList list = new0();
                                push(goTo(0), list);
                            }
                            break;
                        case 1:
                            {
                                ArrayList list = new1();
                                push(goTo(1), list);
                            }
                            break;
                        case 2:
                            {
                                ArrayList list = new2();
                                push(goTo(2), list);
                            }
                            break;
                        case 3:
                            {
                                ArrayList list = new3();
                                push(goTo(3), list);
                            }
                            break;
                        case 4:
                            {
                                ArrayList list = new4();
                                push(goTo(4), list);
                            }
                            break;
                        case 5:
                            {
                                ArrayList list = new5();
                                push(goTo(5), list);
                            }
                            break;
                        case 6:
                            {
                                ArrayList list = new6();
                                push(goTo(6), list);
                            }
                            break;
                        case 7:
                            {
                                ArrayList list = new7();
                                push(goTo(7), list);
                            }
                            break;
                        case 8:
                            {
                                ArrayList list = new8();
                                push(goTo(7), list);
                            }
                            break;
                        case 9:
                            {
                                ArrayList list = new9();
                                push(goTo(8), list);
                            }
                            break;
                        case 10:
                            {
                                ArrayList list = new10();
                                push(goTo(8), list);
                            }
                            break;
                        case 11:
                            {
                                ArrayList list = new11();
                                push(goTo(9), list);
                            }
                            break;
                        case 12:
                            {
                                ArrayList list = new12();
                                push(goTo(9), list);
                            }
                            break;
                    }
                    break;
                case ACCEPT:
                    {
                        EOF node2 = (EOF) this.lexer.next();
                        PProgram node1 = (PProgram) pop().get(0);
                        Start node = new Start(node1, node2);
                        return node;
                    }
                case ERROR:
                    throw new ParserException(this.last_token, "[" + this.last_line + "," + this.last_pos + "] " + Parser.errorMessages[Parser.errors[this.action[1]]]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    ArrayList new0() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgram pprogramNode1;
        {
            PProgramHeader pprogramheaderNode2;
            PProgramPersonality pprogrampersonalityNode3;
            PProgramBehaviour pprogrambehaviourNode6;
            PProgramFooter pprogramfooterNode7;
            pprogramheaderNode2 = (PProgramHeader) nodeArrayList1.get(0);
            {
                LinkedList listNode5 = new LinkedList();
                {
                    LinkedList listNode4 = new LinkedList();
                    listNode4 = (LinkedList) nodeArrayList2.get(0);
                    if (listNode4 != null) {
                        listNode5.addAll(listNode4);
                    }
                }
                pprogrampersonalityNode3 = new AProgramPersonality(listNode5);
            }
            pprogrambehaviourNode6 = (PProgramBehaviour) nodeArrayList3.get(0);
            pprogramfooterNode7 = (PProgramFooter) nodeArrayList4.get(0);
            pprogramNode1 = new AProgram(pprogramheaderNode2, pprogrampersonalityNode3, pprogrambehaviourNode6, pprogramfooterNode7);
        }
        nodeList.add(pprogramNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new1() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgramHeader pprogramheaderNode1;
        {
            LinkedList listNode3 = new LinkedList();
            TLeftCurly tleftcurlyNode4;
            {
                LinkedList listNode2 = new LinkedList();
                listNode2 = (LinkedList) nodeArrayList1.get(0);
                if (listNode2 != null) {
                    listNode3.addAll(listNode2);
                }
            }
            tleftcurlyNode4 = (TLeftCurly) nodeArrayList2.get(0);
            pprogramheaderNode1 = new AProgramHeader(listNode3, tleftcurlyNode4);
        }
        nodeList.add(pprogramheaderNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new2() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgramFooter pprogramfooterNode1;
        {
            TRightCurly trightcurlyNode2;
            trightcurlyNode2 = (TRightCurly) nodeArrayList1.get(0);
            pprogramfooterNode1 = new AProgramFooter(trightcurlyNode2);
        }
        nodeList.add(pprogramfooterNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new3() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgramCharacteristic pprogramcharacteristicNode1;
        {
            LinkedList listNode3 = new LinkedList();
            TEquals tequalsNode4;
            TDigit tdigitNode5;
            {
                LinkedList listNode2 = new LinkedList();
                listNode2 = (LinkedList) nodeArrayList1.get(0);
                if (listNode2 != null) {
                    listNode3.addAll(listNode2);
                }
            }
            tequalsNode4 = (TEquals) nodeArrayList2.get(0);
            tdigitNode5 = (TDigit) nodeArrayList3.get(0);
            pprogramcharacteristicNode1 = new AProgramCharacteristic(listNode3, tequalsNode4, tdigitNode5);
        }
        nodeList.add(pprogramcharacteristicNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new4() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgramBehaviour pprogrambehaviourNode1;
        {
            LinkedList listNode3 = new LinkedList();
            {
                LinkedList listNode2 = new LinkedList();
                listNode2 = (LinkedList) nodeArrayList1.get(0);
                if (listNode2 != null) {
                    listNode3.addAll(listNode2);
                }
            }
            pprogrambehaviourNode1 = new AProgramBehaviour(listNode3);
        }
        nodeList.add(pprogrambehaviourNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new5() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList5 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgramRule pprogramruleNode1;
        {
            LinkedList listNode3 = new LinkedList();
            TLeftBracket tleftbracketNode4;
            PProgramAction pprogramactionNode5;
            PProgramAction pprogramactionNode6;
            TRightBracket trightbracketNode7;
            {
                LinkedList listNode2 = new LinkedList();
                listNode2 = (LinkedList) nodeArrayList1.get(0);
                if (listNode2 != null) {
                    listNode3.addAll(listNode2);
                }
            }
            tleftbracketNode4 = (TLeftBracket) nodeArrayList2.get(0);
            pprogramactionNode5 = (PProgramAction) nodeArrayList3.get(0);
            pprogramactionNode6 = (PProgramAction) nodeArrayList4.get(0);
            trightbracketNode7 = (TRightBracket) nodeArrayList5.get(0);
            pprogramruleNode1 = new AProgramRule(listNode3, tleftbracketNode4, pprogramactionNode5, pprogramactionNode6, trightbracketNode7);
        }
        nodeList.add(pprogramruleNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new6() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgramAction pprogramactionNode1;
        {
            LinkedList listNode3 = new LinkedList();
            TLeftParen tleftparenNode4;
            LinkedList listNode6 = new LinkedList();
            TRightParen trightparenNode7;
            {
                LinkedList listNode2 = new LinkedList();
                listNode2 = (LinkedList) nodeArrayList1.get(0);
                if (listNode2 != null) {
                    listNode3.addAll(listNode2);
                }
            }
            tleftparenNode4 = (TLeftParen) nodeArrayList2.get(0);
            {
                LinkedList listNode5 = new LinkedList();
                listNode5 = (LinkedList) nodeArrayList3.get(0);
                if (listNode5 != null) {
                    listNode6.addAll(listNode5);
                }
            }
            trightparenNode7 = (TRightParen) nodeArrayList4.get(0);
            pprogramactionNode1 = new AProgramAction(listNode3, tleftparenNode4, listNode6, trightparenNode7);
        }
        nodeList.add(pprogramactionNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new7() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode2 = new LinkedList();
        {
            TLetter tletterNode1;
            tletterNode1 = (TLetter) nodeArrayList1.get(0);
            if (tletterNode1 != null) {
                listNode2.add(tletterNode1);
            }
        }
        nodeList.add(listNode2);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new8() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode3 = new LinkedList();
        {
            LinkedList listNode1 = new LinkedList();
            TLetter tletterNode2;
            listNode1 = (LinkedList) nodeArrayList1.get(0);
            tletterNode2 = (TLetter) nodeArrayList2.get(0);
            if (listNode1 != null) {
                listNode3.addAll(listNode1);
            }
            if (tletterNode2 != null) {
                listNode3.add(tletterNode2);
            }
        }
        nodeList.add(listNode3);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new9() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode2 = new LinkedList();
        {
            PProgramCharacteristic pprogramcharacteristicNode1;
            pprogramcharacteristicNode1 = (PProgramCharacteristic) nodeArrayList1.get(0);
            if (pprogramcharacteristicNode1 != null) {
                listNode2.add(pprogramcharacteristicNode1);
            }
        }
        nodeList.add(listNode2);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new10() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode3 = new LinkedList();
        {
            LinkedList listNode1 = new LinkedList();
            PProgramCharacteristic pprogramcharacteristicNode2;
            listNode1 = (LinkedList) nodeArrayList1.get(0);
            pprogramcharacteristicNode2 = (PProgramCharacteristic) nodeArrayList2.get(0);
            if (listNode1 != null) {
                listNode3.addAll(listNode1);
            }
            if (pprogramcharacteristicNode2 != null) {
                listNode3.add(pprogramcharacteristicNode2);
            }
        }
        nodeList.add(listNode3);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new11() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode2 = new LinkedList();
        {
            PProgramRule pprogramruleNode1;
            pprogramruleNode1 = (PProgramRule) nodeArrayList1.get(0);
            if (pprogramruleNode1 != null) {
                listNode2.add(pprogramruleNode1);
            }
        }
        nodeList.add(listNode2);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new12() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode3 = new LinkedList();
        {
            LinkedList listNode1 = new LinkedList();
            PProgramRule pprogramruleNode2;
            listNode1 = (LinkedList) nodeArrayList1.get(0);
            pprogramruleNode2 = (PProgramRule) nodeArrayList2.get(0);
            if (listNode1 != null) {
                listNode3.addAll(listNode1);
            }
            if (pprogramruleNode2 != null) {
                listNode3.add(pprogramruleNode2);
            }
        }
        nodeList.add(listNode3);
        return nodeList;
    }

    private static int[][][] actionTable;

    private static int[][][] gotoTable;

    private static String[] errorMessages;

    private static int[] errors;

    static {
        try {
            DataInputStream s = new DataInputStream(new BufferedInputStream(Parser.class.getResourceAsStream("parser.dat")));
            int length = s.readInt();
            Parser.actionTable = new int[length][][];
            for (int i = 0; i < Parser.actionTable.length; i++) {
                length = s.readInt();
                Parser.actionTable[i] = new int[length][3];
                for (int j = 0; j < Parser.actionTable[i].length; j++) {
                    for (int k = 0; k < 3; k++) {
                        Parser.actionTable[i][j][k] = s.readInt();
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

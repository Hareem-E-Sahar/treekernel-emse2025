package jpdl.patterns.parser.parser;

import jpdl.patterns.parser.lexer.*;
import jpdl.patterns.parser.node.*;
import jpdl.patterns.parser.analysis.*;
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
                                push(goTo(1), list);
                            }
                            break;
                        case 3:
                            {
                                ArrayList list = new3();
                                push(goTo(2), list);
                            }
                            break;
                        case 4:
                            {
                                ArrayList list = new4();
                                push(goTo(2), list);
                            }
                            break;
                        case 5:
                            {
                                ArrayList list = new5();
                                push(goTo(3), list);
                            }
                            break;
                        case 6:
                            {
                                ArrayList list = new6();
                                push(goTo(3), list);
                            }
                            break;
                        case 7:
                            {
                                ArrayList list = new7();
                                push(goTo(3), list);
                            }
                            break;
                        case 8:
                            {
                                ArrayList list = new8();
                                push(goTo(3), list);
                            }
                            break;
                    }
                    break;
                case ACCEPT:
                    {
                        EOF node2 = (EOF) this.lexer.next();
                        PPattern node1 = (PPattern) pop().get(0);
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
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPattern ppatternNode1;
        ppatternNode1 = (PPattern) nodeArrayList2.get(0);
        nodeList.add(ppatternNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new1() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PArgItem pargitemNode1;
        {
            TPathPattern tpathpatternNode2;
            tpathpatternNode2 = (TPathPattern) nodeArrayList1.get(0);
            pargitemNode1 = new APathArgItem(tpathpatternNode2);
        }
        nodeList.add(pargitemNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new2() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PArgItem pargitemNode1;
        {
            TVararg tvarargNode2;
            tvarargNode2 = (TVararg) nodeArrayList1.get(0);
            pargitemNode1 = new AVarargArgItem(tvarargNode2);
        }
        nodeList.add(pargitemNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new3() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode2 = new LinkedList();
        {
            PArgItem pargitemNode1;
            pargitemNode1 = (PArgItem) nodeArrayList1.get(0);
            if (pargitemNode1 != null) {
                listNode2.add(pargitemNode1);
            }
        }
        nodeList.add(listNode2);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new4() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode3 = new LinkedList();
        {
            LinkedList listNode1 = new LinkedList();
            PArgItem pargitemNode2;
            listNode1 = (LinkedList) nodeArrayList1.get(0);
            pargitemNode2 = (PArgItem) nodeArrayList3.get(0);
            if (listNode1 != null) {
                listNode3.addAll(listNode1);
            }
            if (pargitemNode2 != null) {
                listNode3.add(pargitemNode2);
            }
        }
        nodeList.add(listNode3);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new5() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPattern ppatternNode1;
        {
            TPathPattern tpathpatternNode2;
            tpathpatternNode2 = (TPathPattern) nodeArrayList1.get(0);
            ppatternNode1 = new AClassOrPkgPattern(tpathpatternNode2);
        }
        nodeList.add(ppatternNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new6() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPattern ppatternNode1;
        {
            TPathPattern tpathpatternNode2;
            TPathPattern tpathpatternNode3;
            tpathpatternNode2 = (TPathPattern) nodeArrayList1.get(0);
            tpathpatternNode3 = (TPathPattern) nodeArrayList2.get(0);
            ppatternNode1 = new AFieldPattern(tpathpatternNode2, tpathpatternNode3);
        }
        nodeList.add(ppatternNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new7() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPattern ppatternNode1;
        {
            TPathPattern tpathpatternNode2;
            TPathPattern tpathpatternNode3;
            LinkedList listNode4 = new LinkedList();
            tpathpatternNode2 = (TPathPattern) nodeArrayList1.get(0);
            tpathpatternNode3 = (TPathPattern) nodeArrayList2.get(0);
            {
            }
            ppatternNode1 = new AMethodPattern(tpathpatternNode2, tpathpatternNode3, listNode4);
        }
        nodeList.add(ppatternNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new8() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList5 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPattern ppatternNode1;
        {
            TPathPattern tpathpatternNode2;
            TPathPattern tpathpatternNode3;
            LinkedList listNode5 = new LinkedList();
            tpathpatternNode2 = (TPathPattern) nodeArrayList1.get(0);
            tpathpatternNode3 = (TPathPattern) nodeArrayList2.get(0);
            {
                LinkedList listNode4 = new LinkedList();
                listNode4 = (LinkedList) nodeArrayList4.get(0);
                if (listNode4 != null) {
                    listNode5.addAll(listNode4);
                }
            }
            ppatternNode1 = new AMethodPattern(tpathpatternNode2, tpathpatternNode3, listNode5);
        }
        nodeList.add(ppatternNode1);
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

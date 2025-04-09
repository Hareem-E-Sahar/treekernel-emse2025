package jpdl.parser.parser;

import jpdl.parser.lexer.*;
import jpdl.parser.node.*;
import jpdl.parser.analysis.*;
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
                                push(goTo(0), list);
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
                                push(goTo(1), list);
                            }
                            break;
                        case 4:
                            {
                                ArrayList list = new4();
                                push(goTo(1), list);
                            }
                            break;
                        case 5:
                            {
                                ArrayList list = new5();
                                push(goTo(1), list);
                            }
                            break;
                        case 6:
                            {
                                ArrayList list = new6();
                                push(goTo(1), list);
                            }
                            break;
                        case 7:
                            {
                                ArrayList list = new7();
                                push(goTo(1), list);
                            }
                            break;
                        case 8:
                            {
                                ArrayList list = new8();
                                push(goTo(2), list);
                            }
                            break;
                        case 9:
                            {
                                ArrayList list = new9();
                                push(goTo(2), list);
                            }
                            break;
                        case 10:
                            {
                                ArrayList list = new10();
                                push(goTo(3), list);
                            }
                            break;
                        case 11:
                            {
                                ArrayList list = new11();
                                push(goTo(3), list);
                            }
                            break;
                        case 12:
                            {
                                ArrayList list = new12();
                                push(goTo(4), list);
                            }
                            break;
                        case 13:
                            {
                                ArrayList list = new13();
                                push(goTo(4), list);
                            }
                            break;
                        case 14:
                            {
                                ArrayList list = new14();
                                push(goTo(5), list);
                            }
                            break;
                        case 15:
                            {
                                ArrayList list = new15();
                                push(goTo(5), list);
                            }
                            break;
                        case 16:
                            {
                                ArrayList list = new16();
                                push(goTo(5), list);
                            }
                            break;
                        case 17:
                            {
                                ArrayList list = new17();
                                push(goTo(5), list);
                            }
                            break;
                        case 18:
                            {
                                ArrayList list = new18();
                                push(goTo(5), list);
                            }
                            break;
                        case 19:
                            {
                                ArrayList list = new19();
                                push(goTo(6), list);
                            }
                            break;
                        case 20:
                            {
                                ArrayList list = new20();
                                push(goTo(6), list);
                            }
                            break;
                        case 21:
                            {
                                ArrayList list = new21();
                                push(goTo(7), list);
                            }
                            break;
                        case 22:
                            {
                                ArrayList list = new22();
                                push(goTo(7), list);
                            }
                            break;
                        case 23:
                            {
                                ArrayList list = new23();
                                push(goTo(8), list);
                            }
                            break;
                        case 24:
                            {
                                ArrayList list = new24();
                                push(goTo(9), list);
                            }
                            break;
                        case 25:
                            {
                                ArrayList list = new25();
                                push(goTo(10), list);
                            }
                            break;
                        case 26:
                            {
                                ArrayList list = new26();
                                push(goTo(11), list);
                            }
                            break;
                        case 27:
                            {
                                ArrayList list = new27();
                                push(goTo(11), list);
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
        PProgram pprogramNode1;
        {
            LinkedList listNode2 = new LinkedList();
            {
            }
            pprogramNode1 = new AProgram(listNode2);
        }
        nodeList.add(pprogramNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new1() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgram pprogramNode1;
        {
            LinkedList listNode3 = new LinkedList();
            {
                LinkedList listNode2 = new LinkedList();
                listNode2 = (LinkedList) nodeArrayList1.get(0);
                if (listNode2 != null) {
                    listNode3.addAll(listNode2);
                }
            }
            pprogramNode1 = new AProgram(listNode3);
        }
        nodeList.add(pprogramNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new2() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStm pstmNode1;
        {
            TIdent tidentNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            tidentNode2 = (TIdent) nodeArrayList2.get(0);
            pstmNode1 = new ADerivativeStm(tidentNode2, null);
        }
        nodeList.add(pstmNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new3() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStm pstmNode1;
        {
            TIdent tidentNode2;
            TStringLiteral tstringliteralNode3;
            tidentNode2 = (TIdent) nodeArrayList2.get(0);
            tstringliteralNode3 = (TStringLiteral) nodeArrayList3.get(0);
            pstmNode1 = new ADerivativeStm(tidentNode2, tstringliteralNode3);
        }
        nodeList.add(pstmNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new4() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStm pstmNode1;
        {
            PQuery pqueryNode2;
            pqueryNode2 = (PQuery) nodeArrayList1.get(0);
            pstmNode1 = new AQueryStm(pqueryNode2);
        }
        nodeList.add(pstmNode1);
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
        PStm pstmNode1;
        {
            TIdent tidentNode2;
            PPointcut ppointcutNode3;
            tidentNode2 = (TIdent) nodeArrayList2.get(0);
            ppointcutNode3 = (PPointcut) nodeArrayList4.get(0);
            pstmNode1 = new ADefinePredicateStm(tidentNode2, ppointcutNode3);
        }
        nodeList.add(pstmNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new6() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList9 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList8 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList7 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList6 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList5 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStm pstmNode1;
        {
            TIdent tidentNode2;
            PRelation prelationNode3;
            tidentNode2 = (TIdent) nodeArrayList2.get(0);
            prelationNode3 = (PRelation) nodeArrayList6.get(0);
            pstmNode1 = new ADefineRelationStm(tidentNode2, prelationNode3);
        }
        nodeList.add(pstmNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new7() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStm pstmNode1;
        {
            pstmNode1 = new AEndOfCmdStm();
        }
        nodeList.add(pstmNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new8() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PQuery pqueryNode1;
        {
            PPointcut ppointcutNode2;
            TStringLiteral tstringliteralNode3;
            ppointcutNode2 = (PPointcut) nodeArrayList1.get(0);
            tstringliteralNode3 = (TStringLiteral) nodeArrayList3.get(0);
            pqueryNode1 = new AQuery(ppointcutNode2, tstringliteralNode3);
        }
        nodeList.add(pqueryNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new9() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PQuery pqueryNode1;
        {
            PPointcut ppointcutNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            ppointcutNode2 = (PPointcut) nodeArrayList1.get(0);
            pqueryNode1 = new AQuery(ppointcutNode2, null);
        }
        nodeList.add(pqueryNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new10() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PRelation prelationNode1;
        {
            PBinaryPrim pbinaryprimNode2;
            PRelation prelationNode3;
            pbinaryprimNode2 = (PBinaryPrim) nodeArrayList1.get(0);
            prelationNode3 = (PRelation) nodeArrayList3.get(0);
            prelationNode1 = new ACompositeRelation(pbinaryprimNode2, prelationNode3);
        }
        nodeList.add(prelationNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new11() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PRelation prelationNode1;
        {
            PBinaryPrim pbinaryprimNode2;
            pbinaryprimNode2 = (PBinaryPrim) nodeArrayList1.get(0);
            prelationNode1 = new ASingleRelation(pbinaryprimNode2);
        }
        nodeList.add(prelationNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new12() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPointcut ppointcutNode1;
        {
            PUnaryPrim punaryprimNode2;
            punaryprimNode2 = (PUnaryPrim) nodeArrayList1.get(0);
            ppointcutNode1 = new APredicatePointcut(punaryprimNode2);
        }
        nodeList.add(ppointcutNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new13() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPointcut ppointcutNode1;
        {
            TPattern tpatternNode2;
            tpatternNode2 = (TPattern) nodeArrayList1.get(0);
            ppointcutNode1 = new APatternPointcut(tpatternNode2);
        }
        nodeList.add(ppointcutNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new14() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPointcut ppointcutNode1;
        ppointcutNode1 = (PPointcut) nodeArrayList1.get(0);
        nodeList.add(ppointcutNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new15() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPointcut ppointcutNode1;
        {
            PUnaryOparator punaryoparatorNode2;
            PPointcut ppointcutNode3;
            punaryoparatorNode2 = (PUnaryOparator) nodeArrayList1.get(0);
            ppointcutNode3 = (PPointcut) nodeArrayList2.get(0);
            ppointcutNode1 = new AUnaryOpPointcut(punaryoparatorNode2, ppointcutNode3);
        }
        nodeList.add(ppointcutNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new16() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPointcut ppointcutNode1;
        ppointcutNode1 = (PPointcut) nodeArrayList2.get(0);
        nodeList.add(ppointcutNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new17() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPointcut ppointcutNode1;
        {
            TQualif tqualifNode2;
            PRelation prelationNode3;
            PPointcut ppointcutNode4;
            tqualifNode2 = (TQualif) nodeArrayList1.get(0);
            prelationNode3 = (PRelation) nodeArrayList2.get(0);
            ppointcutNode4 = (PPointcut) nodeArrayList4.get(0);
            ppointcutNode1 = new AQualifPointcut(tqualifNode2, prelationNode3, ppointcutNode4);
        }
        nodeList.add(ppointcutNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new18() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPointcut ppointcutNode1;
        {
            PRelation prelationNode2;
            PPointcut ppointcutNode3;
            prelationNode2 = (PRelation) nodeArrayList1.get(0);
            ppointcutNode3 = (PPointcut) nodeArrayList3.get(0);
            ppointcutNode1 = new ARelationPointcut(prelationNode2, ppointcutNode3);
        }
        nodeList.add(ppointcutNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new19() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPointcut ppointcutNode1;
        ppointcutNode1 = (PPointcut) nodeArrayList1.get(0);
        nodeList.add(ppointcutNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new20() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPointcut ppointcutNode1;
        {
            PBinaryOperator pbinaryoperatorNode2;
            PPointcut ppointcutNode3;
            PPointcut ppointcutNode4;
            pbinaryoperatorNode2 = (PBinaryOperator) nodeArrayList2.get(0);
            ppointcutNode3 = (PPointcut) nodeArrayList1.get(0);
            ppointcutNode4 = (PPointcut) nodeArrayList3.get(0);
            ppointcutNode1 = new ABinaryOpPointcut(pbinaryoperatorNode2, ppointcutNode3, ppointcutNode4);
        }
        nodeList.add(ppointcutNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new21() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PBinaryOperator pbinaryoperatorNode1;
        {
            TAnd tandNode2;
            tandNode2 = (TAnd) nodeArrayList1.get(0);
            pbinaryoperatorNode1 = new AAndBinaryOperator(tandNode2);
        }
        nodeList.add(pbinaryoperatorNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new22() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PBinaryOperator pbinaryoperatorNode1;
        {
            TOr torNode2;
            torNode2 = (TOr) nodeArrayList1.get(0);
            pbinaryoperatorNode1 = new AOrBinaryOperator(torNode2);
        }
        nodeList.add(pbinaryoperatorNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new23() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PUnaryOparator punaryoparatorNode1;
        {
            TNot tnotNode2;
            tnotNode2 = (TNot) nodeArrayList1.get(0);
            punaryoparatorNode1 = new ANotUnaryOparator(tnotNode2);
        }
        nodeList.add(punaryoparatorNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new24() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PUnaryPrim punaryprimNode1;
        {
            TIdent tidentNode2;
            tidentNode2 = (TIdent) nodeArrayList1.get(0);
            punaryprimNode1 = new AUnaryPrim(tidentNode2);
        }
        nodeList.add(punaryprimNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new25() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PBinaryPrim pbinaryprimNode1;
        {
            TIdent tidentNode2;
            tidentNode2 = (TIdent) nodeArrayList1.get(0);
            pbinaryprimNode1 = new ABinaryPrim(tidentNode2);
        }
        nodeList.add(pbinaryprimNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new26() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode2 = new LinkedList();
        {
            PStm pstmNode1;
            pstmNode1 = (PStm) nodeArrayList1.get(0);
            if (pstmNode1 != null) {
                listNode2.add(pstmNode1);
            }
        }
        nodeList.add(listNode2);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new27() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode3 = new LinkedList();
        {
            LinkedList listNode1 = new LinkedList();
            PStm pstmNode2;
            listNode1 = (LinkedList) nodeArrayList1.get(0);
            pstmNode2 = (PStm) nodeArrayList2.get(0);
            if (listNode1 != null) {
                listNode3.addAll(listNode1);
            }
            if (pstmNode2 != null) {
                listNode3.add(pstmNode2);
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

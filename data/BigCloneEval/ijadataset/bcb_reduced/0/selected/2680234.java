package it.enricod.jcontextfree.engine.sablecc.parser;

import it.enricod.jcontextfree.engine.sablecc.lexer.*;
import it.enricod.jcontextfree.engine.sablecc.node.*;
import it.enricod.jcontextfree.engine.sablecc.analysis.*;
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

    protected void filter() throws ParserException, LexerException, IOException {
    }

    private void push(int numstate, ArrayList listNode, boolean hidden) throws ParserException, LexerException, IOException {
        this.nodeList = listNode;
        if (!hidden) {
            filter();
        }
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
        push(0, null, true);
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
                        push(this.action[1], list, false);
                    }
                    break;
                case REDUCE:
                    switch(this.action[1]) {
                        case 0:
                            {
                                ArrayList list = new0();
                                push(goTo(0), list, false);
                            }
                            break;
                        case 1:
                            {
                                ArrayList list = new1();
                                push(goTo(1), list, false);
                            }
                            break;
                        case 2:
                            {
                                ArrayList list = new2();
                                push(goTo(2), list, false);
                            }
                            break;
                        case 3:
                            {
                                ArrayList list = new3();
                                push(goTo(3), list, false);
                            }
                            break;
                        case 4:
                            {
                                ArrayList list = new4();
                                push(goTo(4), list, false);
                            }
                            break;
                        case 5:
                            {
                                ArrayList list = new5();
                                push(goTo(5), list, false);
                            }
                            break;
                        case 6:
                            {
                                ArrayList list = new6();
                                push(goTo(6), list, false);
                            }
                            break;
                        case 7:
                            {
                                ArrayList list = new7();
                                push(goTo(7), list, false);
                            }
                            break;
                        case 8:
                            {
                                ArrayList list = new8();
                                push(goTo(8), list, false);
                            }
                            break;
                        case 9:
                            {
                                ArrayList list = new9();
                                push(goTo(9), list, false);
                            }
                            break;
                        case 10:
                            {
                                ArrayList list = new10();
                                push(goTo(9), list, false);
                            }
                            break;
                        case 11:
                            {
                                ArrayList list = new11();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 12:
                            {
                                ArrayList list = new12();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 13:
                            {
                                ArrayList list = new13();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 14:
                            {
                                ArrayList list = new14();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 15:
                            {
                                ArrayList list = new15();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 16:
                            {
                                ArrayList list = new16();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 17:
                            {
                                ArrayList list = new17();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 18:
                            {
                                ArrayList list = new18();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 19:
                            {
                                ArrayList list = new19();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 20:
                            {
                                ArrayList list = new20();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 21:
                            {
                                ArrayList list = new21();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 22:
                            {
                                ArrayList list = new22();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 23:
                            {
                                ArrayList list = new23();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 24:
                            {
                                ArrayList list = new24();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 25:
                            {
                                ArrayList list = new25();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 26:
                            {
                                ArrayList list = new26();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 27:
                            {
                                ArrayList list = new27();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 28:
                            {
                                ArrayList list = new28();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 29:
                            {
                                ArrayList list = new29();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 30:
                            {
                                ArrayList list = new30();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 31:
                            {
                                ArrayList list = new31();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 32:
                            {
                                ArrayList list = new32();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 33:
                            {
                                ArrayList list = new33();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 34:
                            {
                                ArrayList list = new34();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 35:
                            {
                                ArrayList list = new35();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 36:
                            {
                                ArrayList list = new36();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 37:
                            {
                                ArrayList list = new37();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 38:
                            {
                                ArrayList list = new38();
                                push(goTo(10), list, false);
                            }
                            break;
                        case 39:
                            {
                                ArrayList list = new39();
                                push(goTo(11), list, true);
                            }
                            break;
                        case 40:
                            {
                                ArrayList list = new40();
                                push(goTo(11), list, true);
                            }
                            break;
                        case 41:
                            {
                                ArrayList list = new41();
                                push(goTo(12), list, true);
                            }
                            break;
                        case 42:
                            {
                                ArrayList list = new42();
                                push(goTo(12), list, true);
                            }
                            break;
                        case 43:
                            {
                                ArrayList list = new43();
                                push(goTo(13), list, true);
                            }
                            break;
                        case 44:
                            {
                                ArrayList list = new44();
                                push(goTo(13), list, true);
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
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgram pprogramNode1;
        {
            PProgramHeading pprogramheadingNode2;
            pprogramheadingNode2 = (PProgramHeading) nodeArrayList1.get(0);
            pprogramNode1 = new AProgram(pprogramheadingNode2);
        }
        nodeList.add(pprogramNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new1() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgramHeading pprogramheadingNode1;
        {
            PProgramStartshape pprogramstartshapeNode2;
            PRules prulesNode3;
            pprogramstartshapeNode2 = (PProgramStartshape) nodeArrayList1.get(0);
            prulesNode3 = (PRules) nodeArrayList2.get(0);
            pprogramheadingNode1 = new AProgramHeading(pprogramstartshapeNode2, prulesNode3);
        }
        nodeList.add(pprogramheadingNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new2() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PProgramStartshape pprogramstartshapeNode1;
        {
            TStartshape tstartshapeNode2;
            TIdentifier tidentifierNode3;
            tstartshapeNode2 = (TStartshape) nodeArrayList1.get(0);
            tidentifierNode3 = (TIdentifier) nodeArrayList2.get(0);
            pprogramstartshapeNode1 = new AProgramStartshape(tstartshapeNode2, tidentifierNode3);
        }
        nodeList.add(pprogramstartshapeNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new3() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PRules prulesNode1;
        {
            LinkedList listNode3 = new LinkedList();
            {
                LinkedList listNode2 = new LinkedList();
                listNode2 = (LinkedList) nodeArrayList1.get(0);
                if (listNode2 != null) {
                    listNode3.addAll(listNode2);
                }
            }
            prulesNode1 = new ARules(listNode3);
        }
        nodeList.add(prulesNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new4() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PRuleDef pruledefNode1;
        {
            PRuleDecl pruledeclNode2;
            PRuleBody prulebodyNode3;
            PRuleDeclEnd pruledeclendNode4;
            pruledeclNode2 = (PRuleDecl) nodeArrayList1.get(0);
            prulebodyNode3 = (PRuleBody) nodeArrayList2.get(0);
            pruledeclendNode4 = (PRuleDeclEnd) nodeArrayList3.get(0);
            pruledefNode1 = new ARuleDef(pruledeclNode2, prulebodyNode3, pruledeclendNode4);
        }
        nodeList.add(pruledefNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new5() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PRuleDecl pruledeclNode1;
        {
            TRule truleNode2;
            TIdentifier tidentifierNode3;
            TLBrc tlbrcNode4;
            truleNode2 = (TRule) nodeArrayList1.get(0);
            tidentifierNode3 = (TIdentifier) nodeArrayList2.get(0);
            tlbrcNode4 = (TLBrc) nodeArrayList3.get(0);
            pruledeclNode1 = new ARuleDecl(truleNode2, tidentifierNode3, tlbrcNode4);
        }
        nodeList.add(pruledeclNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new6() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PRuleBody prulebodyNode1;
        {
            LinkedList listNode3 = new LinkedList();
            {
                LinkedList listNode2 = new LinkedList();
                listNode2 = (LinkedList) nodeArrayList1.get(0);
                if (listNode2 != null) {
                    listNode3.addAll(listNode2);
                }
            }
            prulebodyNode1 = new ARuleBody(listNode3);
        }
        nodeList.add(prulebodyNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new7() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PShapeDecl pshapedeclNode1;
        {
            TIdentifier tidentifierNode2;
            TLBrc tlbrcNode3;
            PShapeVarValues pshapevarvaluesNode4;
            TRBrc trbrcNode5;
            tidentifierNode2 = (TIdentifier) nodeArrayList1.get(0);
            tlbrcNode3 = (TLBrc) nodeArrayList2.get(0);
            pshapevarvaluesNode4 = (PShapeVarValues) nodeArrayList3.get(0);
            trbrcNode5 = (TRBrc) nodeArrayList4.get(0);
            pshapedeclNode1 = new AShapeDecl(tidentifierNode2, tlbrcNode3, pshapevarvaluesNode4, trbrcNode5);
        }
        nodeList.add(pshapedeclNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new8() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PRuleDeclEnd pruledeclendNode1;
        {
            TRBrc trbrcNode2;
            trbrcNode2 = (TRBrc) nodeArrayList1.get(0);
            pruledeclendNode1 = new ARuleDeclEnd(trbrcNode2);
        }
        nodeList.add(pruledeclendNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new9() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        PShapeVarValues pshapevarvaluesNode1;
        {
            LinkedList listNode2 = new LinkedList();
            {
            }
            pshapevarvaluesNode1 = new AShapeVarValues(listNode2);
        }
        nodeList.add(pshapevarvaluesNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new10() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PShapeVarValues pshapevarvaluesNode1;
        {
            LinkedList listNode3 = new LinkedList();
            {
                LinkedList listNode2 = new LinkedList();
                listNode2 = (LinkedList) nodeArrayList1.get(0);
                if (listNode2 != null) {
                    listNode3.addAll(listNode2);
                }
            }
            pshapevarvaluesNode1 = new AShapeVarValues(listNode3);
        }
        nodeList.add(pshapevarvaluesNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new11() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TX txNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            txNode2 = (TX) nodeArrayList1.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new AXFloatingPointValueSingleValue(txNode2, null, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new12() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TX txNode2;
            TMinus tminusNode3;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            txNode2 = (TX) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new AXFloatingPointValueSingleValue(txNode2, tminusNode3, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new13() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TX txNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TIntegerLiteral tintegerliteralNode4;
            txNode2 = (TX) nodeArrayList1.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new AXIntegerValueSingleValue(txNode2, null, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new14() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TX txNode2;
            TMinus tminusNode3;
            TIntegerLiteral tintegerliteralNode4;
            txNode2 = (TX) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new AXIntegerValueSingleValue(txNode2, tminusNode3, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new15() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TY tyNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tyNode2 = (TY) nodeArrayList1.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new AYFloatingPointValueSingleValue(tyNode2, null, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new16() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TY tyNode2;
            TMinus tminusNode3;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tyNode2 = (TY) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new AYFloatingPointValueSingleValue(tyNode2, tminusNode3, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new17() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TY tyNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TIntegerLiteral tintegerliteralNode4;
            tyNode2 = (TY) nodeArrayList1.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new AYIntegerValueSingleValue(tyNode2, null, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new18() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TY tyNode2;
            TMinus tminusNode3;
            TIntegerLiteral tintegerliteralNode4;
            tyNode2 = (TY) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new AYIntegerValueSingleValue(tyNode2, tminusNode3, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new19() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TZ tzNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tzNode2 = (TZ) nodeArrayList1.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new AZFloatingPointValueSingleValue(tzNode2, null, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new20() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TZ tzNode2;
            TMinus tminusNode3;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tzNode2 = (TZ) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new AZFloatingPointValueSingleValue(tzNode2, tminusNode3, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new21() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TZ tzNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TIntegerLiteral tintegerliteralNode4;
            tzNode2 = (TZ) nodeArrayList1.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new AZIntegerValueSingleValue(tzNode2, null, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new22() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TZ tzNode2;
            TMinus tminusNode3;
            TIntegerLiteral tintegerliteralNode4;
            tzNode2 = (TZ) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new AZIntegerValueSingleValue(tzNode2, tminusNode3, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new23() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TR trNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            trNode2 = (TR) nodeArrayList1.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new ARFloatingPointValueSingleValue(trNode2, null, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new24() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TR trNode2;
            TMinus tminusNode3;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            trNode2 = (TR) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new ARFloatingPointValueSingleValue(trNode2, tminusNode3, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new25() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TR trNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TIntegerLiteral tintegerliteralNode4;
            trNode2 = (TR) nodeArrayList1.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new ARIntegerValueSingleValue(trNode2, null, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new26() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TR trNode2;
            TMinus tminusNode3;
            TIntegerLiteral tintegerliteralNode4;
            trNode2 = (TR) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new ARIntegerValueSingleValue(trNode2, tminusNode3, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new27() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TS tsNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tsNode2 = (TS) nodeArrayList1.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new ASFloatingPointValueSingleValue(tsNode2, null, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new28() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TS tsNode2;
            TMinus tminusNode3;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tsNode2 = (TS) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new ASFloatingPointValueSingleValue(tsNode2, tminusNode3, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new29() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TS tsNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TIntegerLiteral tintegerliteralNode4;
            tsNode2 = (TS) nodeArrayList1.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new ASIntegerValueSingleValue(tsNode2, null, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new30() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TS tsNode2;
            TMinus tminusNode3;
            TIntegerLiteral tintegerliteralNode4;
            tsNode2 = (TS) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new ASIntegerValueSingleValue(tsNode2, tminusNode3, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new31() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TF tfNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tfNode2 = (TF) nodeArrayList1.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new AFFloatingPointValueSingleValue(tfNode2, null, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new32() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TF tfNode2;
            TMinus tminusNode3;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tfNode2 = (TF) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new AFFloatingPointValueSingleValue(tfNode2, tminusNode3, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new33() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TF tfNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TIntegerLiteral tintegerliteralNode4;
            tfNode2 = (TF) nodeArrayList1.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new AFIntegerValueSingleValue(tfNode2, null, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new34() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TF tfNode2;
            TMinus tminusNode3;
            TIntegerLiteral tintegerliteralNode4;
            tfNode2 = (TF) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new AFIntegerValueSingleValue(tfNode2, tminusNode3, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new35() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TB tbNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tbNode2 = (TB) nodeArrayList1.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new ABFloatingPointValueSingleValue(tbNode2, null, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new36() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TB tbNode2;
            TMinus tminusNode3;
            TFloatingPointLiteral tfloatingpointliteralNode4;
            tbNode2 = (TB) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tfloatingpointliteralNode4 = (TFloatingPointLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new ABFloatingPointValueSingleValue(tbNode2, tminusNode3, tfloatingpointliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new37() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TB tbNode2;
            @SuppressWarnings("unused") Object nullNode3 = null;
            TIntegerLiteral tintegerliteralNode4;
            tbNode2 = (TB) nodeArrayList1.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList2.get(0);
            psinglevalueNode1 = new ABIntegerValueSingleValue(tbNode2, null, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new38() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PSingleValue psinglevalueNode1;
        {
            TB tbNode2;
            TMinus tminusNode3;
            TIntegerLiteral tintegerliteralNode4;
            tbNode2 = (TB) nodeArrayList1.get(0);
            tminusNode3 = (TMinus) nodeArrayList2.get(0);
            tintegerliteralNode4 = (TIntegerLiteral) nodeArrayList3.get(0);
            psinglevalueNode1 = new ABIntegerValueSingleValue(tbNode2, tminusNode3, tintegerliteralNode4);
        }
        nodeList.add(psinglevalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new39() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode2 = new LinkedList();
        {
            PRuleDef pruledefNode1;
            pruledefNode1 = (PRuleDef) nodeArrayList1.get(0);
            if (pruledefNode1 != null) {
                listNode2.add(pruledefNode1);
            }
        }
        nodeList.add(listNode2);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new40() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode3 = new LinkedList();
        {
            LinkedList listNode1 = new LinkedList();
            PRuleDef pruledefNode2;
            listNode1 = (LinkedList) nodeArrayList1.get(0);
            pruledefNode2 = (PRuleDef) nodeArrayList2.get(0);
            if (listNode1 != null) {
                listNode3.addAll(listNode1);
            }
            if (pruledefNode2 != null) {
                listNode3.add(pruledefNode2);
            }
        }
        nodeList.add(listNode3);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new41() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode2 = new LinkedList();
        {
            PShapeDecl pshapedeclNode1;
            pshapedeclNode1 = (PShapeDecl) nodeArrayList1.get(0);
            if (pshapedeclNode1 != null) {
                listNode2.add(pshapedeclNode1);
            }
        }
        nodeList.add(listNode2);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new42() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode3 = new LinkedList();
        {
            LinkedList listNode1 = new LinkedList();
            PShapeDecl pshapedeclNode2;
            listNode1 = (LinkedList) nodeArrayList1.get(0);
            pshapedeclNode2 = (PShapeDecl) nodeArrayList2.get(0);
            if (listNode1 != null) {
                listNode3.addAll(listNode1);
            }
            if (pshapedeclNode2 != null) {
                listNode3.add(pshapedeclNode2);
            }
        }
        nodeList.add(listNode3);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new43() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode2 = new LinkedList();
        {
            PSingleValue psinglevalueNode1;
            psinglevalueNode1 = (PSingleValue) nodeArrayList1.get(0);
            if (psinglevalueNode1 != null) {
                listNode2.add(psinglevalueNode1);
            }
        }
        nodeList.add(listNode2);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new44() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        LinkedList listNode3 = new LinkedList();
        {
            LinkedList listNode1 = new LinkedList();
            PSingleValue psinglevalueNode2;
            listNode1 = (LinkedList) nodeArrayList1.get(0);
            psinglevalueNode2 = (PSingleValue) nodeArrayList2.get(0);
            if (listNode1 != null) {
                listNode3.addAll(listNode1);
            }
            if (psinglevalueNode2 != null) {
                listNode3.add(psinglevalueNode2);
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

package net.sourceforge.jdefprog.mcl.generated.parser;

import net.sourceforge.jdefprog.mcl.generated.lexer.*;
import net.sourceforge.jdefprog.mcl.generated.node.*;
import net.sourceforge.jdefprog.mcl.generated.analysis.*;
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
                                push(goTo(4), list, false);
                            }
                            break;
                        case 6:
                            {
                                ArrayList list = new6();
                                push(goTo(5), list, false);
                            }
                            break;
                        case 7:
                            {
                                ArrayList list = new7();
                                push(goTo(5), list, false);
                            }
                            break;
                        case 8:
                            {
                                ArrayList list = new8();
                                push(goTo(5), list, false);
                            }
                            break;
                        case 9:
                            {
                                ArrayList list = new9();
                                push(goTo(6), list, false);
                            }
                            break;
                        case 10:
                            {
                                ArrayList list = new10();
                                push(goTo(6), list, false);
                            }
                            break;
                        case 11:
                            {
                                ArrayList list = new11();
                                push(goTo(7), list, false);
                            }
                            break;
                        case 12:
                            {
                                ArrayList list = new12();
                                push(goTo(7), list, false);
                            }
                            break;
                        case 13:
                            {
                                ArrayList list = new13();
                                push(goTo(8), list, false);
                            }
                            break;
                        case 14:
                            {
                                ArrayList list = new14();
                                push(goTo(8), list, false);
                            }
                            break;
                        case 15:
                            {
                                ArrayList list = new15();
                                push(goTo(9), list, false);
                            }
                            break;
                        case 16:
                            {
                                ArrayList list = new16();
                                push(goTo(9), list, false);
                            }
                            break;
                        case 17:
                            {
                                ArrayList list = new17();
                                push(goTo(9), list, false);
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
                                push(goTo(11), list, false);
                            }
                            break;
                        case 25:
                            {
                                ArrayList list = new25();
                                push(goTo(11), list, false);
                            }
                            break;
                        case 26:
                            {
                                ArrayList list = new26();
                                push(goTo(11), list, false);
                            }
                            break;
                        case 27:
                            {
                                ArrayList list = new27();
                                push(goTo(11), list, false);
                            }
                            break;
                        case 28:
                            {
                                ArrayList list = new28();
                                push(goTo(11), list, false);
                            }
                            break;
                        case 29:
                            {
                                ArrayList list = new29();
                                push(goTo(12), list, false);
                            }
                            break;
                        case 30:
                            {
                                ArrayList list = new30();
                                push(goTo(12), list, false);
                            }
                            break;
                        case 31:
                            {
                                ArrayList list = new31();
                                push(goTo(12), list, false);
                            }
                            break;
                        case 32:
                            {
                                ArrayList list = new32();
                                push(goTo(12), list, false);
                            }
                            break;
                        case 33:
                            {
                                ArrayList list = new33();
                                push(goTo(13), list, false);
                            }
                            break;
                        case 34:
                            {
                                ArrayList list = new34();
                                push(goTo(13), list, false);
                            }
                            break;
                        case 35:
                            {
                                ArrayList list = new35();
                                push(goTo(14), list, false);
                            }
                            break;
                        case 36:
                            {
                                ArrayList list = new36();
                                push(goTo(14), list, false);
                            }
                            break;
                        case 37:
                            {
                                ArrayList list = new37();
                                push(goTo(15), list, false);
                            }
                            break;
                        case 38:
                            {
                                ArrayList list = new38();
                                push(goTo(15), list, false);
                            }
                            break;
                        case 39:
                            {
                                ArrayList list = new39();
                                push(goTo(16), list, false);
                            }
                            break;
                        case 40:
                            {
                                ArrayList list = new40();
                                push(goTo(16), list, false);
                            }
                            break;
                        case 41:
                            {
                                ArrayList list = new41();
                                push(goTo(17), list, false);
                            }
                            break;
                    }
                    break;
                case ACCEPT:
                    {
                        EOF node2 = (EOF) this.lexer.next();
                        PConstraint node1 = (PConstraint) pop().get(0);
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
        PConstraint pconstraintNode1;
        {
            PValue pvalueNode2;
            pvalueNode2 = (PValue) nodeArrayList1.get(0);
            pconstraintNode1 = new AConstraint(pvalueNode2);
        }
        nodeList.add(pconstraintNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new1() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PValue pvalueNode1;
        {
            PLevelEight pleveleightNode2;
            pleveleightNode2 = (PLevelEight) nodeArrayList1.get(0);
            pvalueNode1 = new AUpValue(pleveleightNode2);
        }
        nodeList.add(pvalueNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new2() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelEight pleveleightNode1;
        {
            PLevelSeven plevelsevenNode2;
            plevelsevenNode2 = (PLevelSeven) nodeArrayList1.get(0);
            pleveleightNode1 = new AUpLevelEight(plevelsevenNode2);
        }
        nodeList.add(pleveleightNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new3() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelSeven plevelsevenNode1;
        {
            PLevelSix plevelsixNode2;
            plevelsixNode2 = (PLevelSix) nodeArrayList1.get(0);
            plevelsevenNode1 = new AUpLevelSeven(plevelsixNode2);
        }
        nodeList.add(plevelsevenNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new4() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelSix plevelsixNode1;
        {
            PLevelFive plevelfiveNode2;
            plevelfiveNode2 = (PLevelFive) nodeArrayList1.get(0);
            plevelsixNode1 = new AUpLevelSix(plevelfiveNode2);
        }
        nodeList.add(plevelsixNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new5() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelSix plevelsixNode1;
        {
            PLevelSix plevelsixNode2;
            TLogicop tlogicopNode3;
            PLevelFive plevelfiveNode4;
            plevelsixNode2 = (PLevelSix) nodeArrayList1.get(0);
            tlogicopNode3 = (TLogicop) nodeArrayList2.get(0);
            plevelfiveNode4 = (PLevelFive) nodeArrayList3.get(0);
            plevelsixNode1 = new ALogicexprLevelSix(plevelsixNode2, tlogicopNode3, plevelfiveNode4);
        }
        nodeList.add(plevelsixNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new6() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelFive plevelfiveNode1;
        {
            PLevelFour plevelfourNode2;
            plevelfourNode2 = (PLevelFour) nodeArrayList1.get(0);
            plevelfiveNode1 = new AUpLevelFive(plevelfourNode2);
        }
        nodeList.add(plevelfiveNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new7() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelFive plevelfiveNode1;
        {
            PLevelFive plevelfiveNode2;
            TEqualop tequalopNode3;
            PLevelFour plevelfourNode4;
            plevelfiveNode2 = (PLevelFive) nodeArrayList1.get(0);
            tequalopNode3 = (TEqualop) nodeArrayList2.get(0);
            plevelfourNode4 = (PLevelFour) nodeArrayList3.get(0);
            plevelfiveNode1 = new AEqualexprLevelFive(plevelfiveNode2, tequalopNode3, plevelfourNode4);
        }
        nodeList.add(plevelfiveNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new8() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelFive plevelfiveNode1;
        {
            PLevelFive plevelfiveNode2;
            TRelop trelopNode3;
            PLevelFour plevelfourNode4;
            plevelfiveNode2 = (PLevelFive) nodeArrayList1.get(0);
            trelopNode3 = (TRelop) nodeArrayList2.get(0);
            plevelfourNode4 = (PLevelFour) nodeArrayList3.get(0);
            plevelfiveNode1 = new ARelexprLevelFive(plevelfiveNode2, trelopNode3, plevelfourNode4);
        }
        nodeList.add(plevelfiveNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new9() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelFour plevelfourNode1;
        {
            PLevelThree plevelthreeNode2;
            plevelthreeNode2 = (PLevelThree) nodeArrayList1.get(0);
            plevelfourNode1 = new AUpLevelFour(plevelthreeNode2);
        }
        nodeList.add(plevelfourNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new10() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelFour plevelfourNode1;
        {
            PLevelFour plevelfourNode2;
            TAop taopNode3;
            PLevelThree plevelthreeNode4;
            plevelfourNode2 = (PLevelFour) nodeArrayList1.get(0);
            taopNode3 = (TAop) nodeArrayList2.get(0);
            plevelthreeNode4 = (PLevelThree) nodeArrayList3.get(0);
            plevelfourNode1 = new ASumLevelFour(plevelfourNode2, taopNode3, plevelthreeNode4);
        }
        nodeList.add(plevelfourNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new11() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelThree plevelthreeNode1;
        {
            PLevelTwo pleveltwoNode2;
            pleveltwoNode2 = (PLevelTwo) nodeArrayList1.get(0);
            plevelthreeNode1 = new AUpLevelThree(pleveltwoNode2);
        }
        nodeList.add(plevelthreeNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new12() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelThree plevelthreeNode1;
        {
            PLevelThree plevelthreeNode2;
            TMop tmopNode3;
            PLevelTwo pleveltwoNode4;
            plevelthreeNode2 = (PLevelThree) nodeArrayList1.get(0);
            tmopNode3 = (TMop) nodeArrayList2.get(0);
            pleveltwoNode4 = (PLevelTwo) nodeArrayList3.get(0);
            plevelthreeNode1 = new AMultiplicationLevelThree(plevelthreeNode2, tmopNode3, pleveltwoNode4);
        }
        nodeList.add(plevelthreeNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new13() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelTwo pleveltwoNode1;
        {
            PLevelOne pleveloneNode2;
            pleveloneNode2 = (PLevelOne) nodeArrayList1.get(0);
            pleveltwoNode1 = new AUpLevelTwo(pleveloneNode2);
        }
        nodeList.add(pleveltwoNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new14() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelTwo pleveltwoNode1;
        {
            TNegation tnegationNode2;
            PLevelTwo pleveltwoNode3;
            tnegationNode2 = (TNegation) nodeArrayList1.get(0);
            pleveltwoNode3 = (PLevelTwo) nodeArrayList2.get(0);
            pleveltwoNode1 = new ANegationLevelTwo(tnegationNode2, pleveltwoNode3);
        }
        nodeList.add(pleveltwoNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new15() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelOne pleveloneNode1;
        {
            PPrimary pprimaryNode2;
            pprimaryNode2 = (PPrimary) nodeArrayList1.get(0);
            pleveloneNode1 = new AUpLevelOne(pprimaryNode2);
        }
        nodeList.add(pleveloneNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new16() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList6 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList5 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelOne pleveloneNode1;
        {
            PLevelOne pleveloneNode2;
            TDot tdotNode3;
            TIdent tidentNode4;
            TOpenParen topenparenNode5;
            PParamsList pparamslistNode6;
            TCloseParen tcloseparenNode7;
            pleveloneNode2 = (PLevelOne) nodeArrayList1.get(0);
            tdotNode3 = (TDot) nodeArrayList2.get(0);
            tidentNode4 = (TIdent) nodeArrayList3.get(0);
            topenparenNode5 = (TOpenParen) nodeArrayList4.get(0);
            pparamslistNode6 = (PParamsList) nodeArrayList5.get(0);
            tcloseparenNode7 = (TCloseParen) nodeArrayList6.get(0);
            pleveloneNode1 = new ACallLevelOne(pleveloneNode2, tdotNode3, tidentNode4, topenparenNode5, pparamslistNode6, tcloseparenNode7);
        }
        nodeList.add(pleveloneNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new17() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PLevelOne pleveloneNode1;
        {
            PLevelOne pleveloneNode2;
            TDot tdotNode3;
            TIdent tidentNode4;
            pleveloneNode2 = (PLevelOne) nodeArrayList1.get(0);
            tdotNode3 = (TDot) nodeArrayList2.get(0);
            tidentNode4 = (TIdent) nodeArrayList3.get(0);
            pleveloneNode1 = new AAccessLevelOne(pleveloneNode2, tdotNode3, tidentNode4);
        }
        nodeList.add(pleveloneNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new18() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPrimary pprimaryNode1;
        {
            PReference preferenceNode2;
            preferenceNode2 = (PReference) nodeArrayList1.get(0);
            pprimaryNode1 = new AReferencePrimary(preferenceNode2);
        }
        nodeList.add(pprimaryNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new19() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPrimary pprimaryNode1;
        {
            TIdent tidentNode2;
            TOpenParen topenparenNode3;
            PParamsList pparamslistNode4;
            TCloseParen tcloseparenNode5;
            tidentNode2 = (TIdent) nodeArrayList1.get(0);
            topenparenNode3 = (TOpenParen) nodeArrayList2.get(0);
            pparamslistNode4 = (PParamsList) nodeArrayList3.get(0);
            tcloseparenNode5 = (TCloseParen) nodeArrayList4.get(0);
            pprimaryNode1 = new AFCallPrimary(tidentNode2, topenparenNode3, pparamslistNode4, tcloseparenNode5);
        }
        nodeList.add(pprimaryNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new20() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPrimary pprimaryNode1;
        {
            PNumberConst pnumberconstNode2;
            pnumberconstNode2 = (PNumberConst) nodeArrayList1.get(0);
            pprimaryNode1 = new AFNumberConstPrimary(pnumberconstNode2);
        }
        nodeList.add(pprimaryNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new21() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPrimary pprimaryNode1;
        {
            TBooleanConst tbooleanconstNode2;
            tbooleanconstNode2 = (TBooleanConst) nodeArrayList1.get(0);
            pprimaryNode1 = new AFBoolConstPrimary(tbooleanconstNode2);
        }
        nodeList.add(pprimaryNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new22() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList4 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPrimary pprimaryNode1;
        {
            TOld toldNode2;
            TOpenParen topenparenNode3;
            PValue pvalueNode4;
            TCloseParen tcloseparenNode5;
            toldNode2 = (TOld) nodeArrayList1.get(0);
            topenparenNode3 = (TOpenParen) nodeArrayList2.get(0);
            pvalueNode4 = (PValue) nodeArrayList3.get(0);
            tcloseparenNode5 = (TCloseParen) nodeArrayList4.get(0);
            pprimaryNode1 = new AOldPrimary(toldNode2, topenparenNode3, pvalueNode4, tcloseparenNode5);
        }
        nodeList.add(pprimaryNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new23() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PPrimary pprimaryNode1;
        {
            TOpenParen topenparenNode2;
            PValue pvalueNode3;
            TCloseParen tcloseparenNode4;
            topenparenNode2 = (TOpenParen) nodeArrayList1.get(0);
            pvalueNode3 = (PValue) nodeArrayList2.get(0);
            tcloseparenNode4 = (TCloseParen) nodeArrayList3.get(0);
            pprimaryNode1 = new AParenthesesPrimary(topenparenNode2, pvalueNode3, tcloseparenNode4);
        }
        nodeList.add(pprimaryNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new24() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PReference preferenceNode1;
        {
            TIdent tidentNode2;
            tidentNode2 = (TIdent) nodeArrayList1.get(0);
            preferenceNode1 = new AFIdentReference(tidentNode2);
        }
        nodeList.add(preferenceNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new25() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PReference preferenceNode1;
        {
            TParam tparamNode2;
            tparamNode2 = (TParam) nodeArrayList1.get(0);
            preferenceNode1 = new AFParamReference(tparamNode2);
        }
        nodeList.add(preferenceNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new26() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PReference preferenceNode1;
        {
            TResult tresultNode2;
            tresultNode2 = (TResult) nodeArrayList1.get(0);
            preferenceNode1 = new AFResultReference(tresultNode2);
        }
        nodeList.add(preferenceNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new27() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PReference preferenceNode1;
        {
            TThis tthisNode2;
            tthisNode2 = (TThis) nodeArrayList1.get(0);
            preferenceNode1 = new AFThisReference(tthisNode2);
        }
        nodeList.add(preferenceNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new28() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PReference preferenceNode1;
        {
            TKwNull tkwnullNode2;
            tkwnullNode2 = (TKwNull) nodeArrayList1.get(0);
            preferenceNode1 = new AFNullReference(tkwnullNode2);
        }
        nodeList.add(preferenceNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new29() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PNumberConst pnumberconstNode1;
        {
            TIntConst tintconstNode2;
            tintconstNode2 = (TIntConst) nodeArrayList1.get(0);
            pnumberconstNode1 = new AFIntNumberConst(tintconstNode2);
        }
        nodeList.add(pnumberconstNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new30() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PNumberConst pnumberconstNode1;
        {
            TLongConst tlongconstNode2;
            tlongconstNode2 = (TLongConst) nodeArrayList1.get(0);
            pnumberconstNode1 = new AFLongNumberConst(tlongconstNode2);
        }
        nodeList.add(pnumberconstNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new31() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PNumberConst pnumberconstNode1;
        {
            TAop taopNode2;
            TIntConst tintconstNode3;
            taopNode2 = (TAop) nodeArrayList1.get(0);
            tintconstNode3 = (TIntConst) nodeArrayList2.get(0);
            pnumberconstNode1 = new AFSignedIntNumberConst(taopNode2, tintconstNode3);
        }
        nodeList.add(pnumberconstNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new32() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PNumberConst pnumberconstNode1;
        {
            TAop taopNode2;
            TLongConst tlongconstNode3;
            taopNode2 = (TAop) nodeArrayList1.get(0);
            tlongconstNode3 = (TLongConst) nodeArrayList2.get(0);
            pnumberconstNode1 = new AFSignedLongNumberConst(taopNode2, tlongconstNode3);
        }
        nodeList.add(pnumberconstNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new33() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PCast pcastNode1;
        {
            TOpenParen topenparenNode2;
            TPrimitiveType tprimitivetypeNode3;
            TCloseParen tcloseparenNode4;
            topenparenNode2 = (TOpenParen) nodeArrayList1.get(0);
            tprimitivetypeNode3 = (TPrimitiveType) nodeArrayList2.get(0);
            tcloseparenNode4 = (TCloseParen) nodeArrayList3.get(0);
            pcastNode1 = new APrimCast(topenparenNode2, tprimitivetypeNode3, tcloseparenNode4);
        }
        nodeList.add(pcastNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new34() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PCast pcastNode1;
        {
            TOpenParen topenparenNode2;
            PComplexIdent pcomplexidentNode3;
            TCloseParen tcloseparenNode4;
            topenparenNode2 = (TOpenParen) nodeArrayList1.get(0);
            pcomplexidentNode3 = (PComplexIdent) nodeArrayList2.get(0);
            tcloseparenNode4 = (TCloseParen) nodeArrayList3.get(0);
            pcastNode1 = new AGenericCast(topenparenNode2, pcomplexidentNode3, tcloseparenNode4);
        }
        nodeList.add(pcastNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new35() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PComplexIdent pcomplexidentNode1;
        {
            TIdent tidentNode2;
            tidentNode2 = (TIdent) nodeArrayList1.get(0);
            pcomplexidentNode1 = new ASimpleComplexIdent(tidentNode2);
        }
        nodeList.add(pcomplexidentNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new36() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PComplexIdent pcomplexidentNode1;
        {
            PComplexIdent pcomplexidentNode2;
            TDot tdotNode3;
            TIdent tidentNode4;
            pcomplexidentNode2 = (PComplexIdent) nodeArrayList1.get(0);
            tdotNode3 = (TDot) nodeArrayList2.get(0);
            tidentNode4 = (TIdent) nodeArrayList3.get(0);
            pcomplexidentNode1 = new ACombinedComplexIdent(pcomplexidentNode2, tdotNode3, tidentNode4);
        }
        nodeList.add(pcomplexidentNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new37() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        PParamsList pparamslistNode1;
        {
            pparamslistNode1 = new AVoidParamsList();
        }
        nodeList.add(pparamslistNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new38() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PParamsList pparamslistNode1;
        {
            PNotVoidParamsList pnotvoidparamslistNode2;
            pnotvoidparamslistNode2 = (PNotVoidParamsList) nodeArrayList1.get(0);
            pparamslistNode1 = new ANotVoidParamsList(pnotvoidparamslistNode2);
        }
        nodeList.add(pparamslistNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new39() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PNotVoidParamsList pnotvoidparamslistNode1;
        {
            PParamValue pparamvalueNode2;
            pparamvalueNode2 = (PParamValue) nodeArrayList1.get(0);
            pnotvoidparamslistNode1 = new ASimpleNotVoidParamsList(pparamvalueNode2);
        }
        nodeList.add(pnotvoidparamslistNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new40() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PNotVoidParamsList pnotvoidparamslistNode1;
        {
            PNotVoidParamsList pnotvoidparamslistNode2;
            TComma tcommaNode3;
            PParamValue pparamvalueNode4;
            pnotvoidparamslistNode2 = (PNotVoidParamsList) nodeArrayList1.get(0);
            tcommaNode3 = (TComma) nodeArrayList2.get(0);
            pparamvalueNode4 = (PParamValue) nodeArrayList3.get(0);
            pnotvoidparamslistNode1 = new ACombinedNotVoidParamsList(pnotvoidparamslistNode2, tcommaNode3, pparamvalueNode4);
        }
        nodeList.add(pnotvoidparamslistNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new41() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PParamValue pparamvalueNode1;
        {
            PValue pvalueNode2;
            pvalueNode2 = (PValue) nodeArrayList1.get(0);
            pparamvalueNode1 = new AParamValue(pvalueNode2);
        }
        nodeList.add(pparamvalueNode1);
        return nodeList;
    }

    private static int[][][] actionTable = { { { -1, ERROR, 0 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, ERROR, 1 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, REDUCE, 29 } }, { { -1, REDUCE, 30 } }, { { -1, ERROR, 4 }, { 5, SHIFT, 27 }, { 6, SHIFT, 28 } }, { { -1, ERROR, 5 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, REDUCE, 27 } }, { { -1, REDUCE, 28 } }, { { -1, REDUCE, 26 } }, { { -1, REDUCE, 25 } }, { { -1, ERROR, 10 }, { 0, SHIFT, 30 } }, { { -1, REDUCE, 21 } }, { { -1, REDUCE, 24 }, { 0, SHIFT, 31 } }, { { -1, ERROR, 13 }, { 21, ACCEPT, -1 } }, { { -1, REDUCE, 0 } }, { { -1, REDUCE, 1 } }, { { -1, REDUCE, 2 } }, { { -1, REDUCE, 3 }, { 11, SHIFT, 32 } }, { { -1, REDUCE, 4 }, { 9, SHIFT, 33 }, { 10, SHIFT, 34 } }, { { -1, REDUCE, 6 }, { 7, SHIFT, 35 } }, { { -1, REDUCE, 9 }, { 8, SHIFT, 36 } }, { { -1, REDUCE, 11 } }, { { -1, REDUCE, 13 }, { 2, SHIFT, 37 } }, { { -1, REDUCE, 15 } }, { { -1, REDUCE, 18 } }, { { -1, REDUCE, 20 } }, { { -1, ERROR, 26 }, { 1, SHIFT, 38 } }, { { -1, REDUCE, 31 } }, { { -1, REDUCE, 32 } }, { { -1, REDUCE, 14 } }, { { -1, ERROR, 30 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, REDUCE, 37 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, ERROR, 32 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, ERROR, 33 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, ERROR, 34 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, ERROR, 35 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, ERROR, 36 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, ERROR, 37 }, { 20, SHIFT, 49 } }, { { -1, REDUCE, 23 } }, { { -1, ERROR, 39 }, { 1, SHIFT, 50 } }, { { -1, REDUCE, 41 } }, { { -1, ERROR, 41 }, { 1, SHIFT, 51 } }, { { -1, REDUCE, 38 }, { 3, SHIFT, 52 } }, { { -1, REDUCE, 39 } }, { { -1, REDUCE, 5 }, { 9, SHIFT, 33 }, { 10, SHIFT, 34 } }, { { -1, REDUCE, 7 }, { 7, SHIFT, 35 } }, { { -1, REDUCE, 8 }, { 7, SHIFT, 35 } }, { { -1, REDUCE, 10 }, { 8, SHIFT, 36 } }, { { -1, REDUCE, 12 } }, { { -1, REDUCE, 17 }, { 0, SHIFT, 53 } }, { { -1, REDUCE, 22 } }, { { -1, REDUCE, 19 } }, { { -1, ERROR, 52 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, REDUCE, 37 }, { 0, SHIFT, 1 }, { 5, SHIFT, 2 }, { 6, SHIFT, 3 }, { 7, SHIFT, 4 }, { 12, SHIFT, 5 }, { 13, SHIFT, 6 }, { 14, SHIFT, 7 }, { 15, SHIFT, 8 }, { 16, SHIFT, 9 }, { 17, SHIFT, 10 }, { 19, SHIFT, 11 }, { 20, SHIFT, 12 } }, { { -1, REDUCE, 40 } }, { { -1, ERROR, 55 }, { 1, SHIFT, 56 } }, { { -1, REDUCE, 16 } } };

    private static int[][][] gotoTable = { { { -1, 13 } }, { { -1, 40 }, { 0, 14 }, { 1, 26 }, { 30, 39 } }, { { -1, 15 } }, { { -1, 16 } }, { { -1, 17 } }, { { -1, 18 }, { 32, 44 } }, { { -1, 19 }, { 33, 45 }, { 34, 46 } }, { { -1, 20 }, { 35, 47 } }, { { -1, 21 }, { 5, 29 }, { 36, 48 } }, { { -1, 22 } }, { { -1, 23 } }, { { -1, 24 } }, { { -1, 25 } }, { { -1, -1 } }, { { -1, -1 } }, { { -1, 41 }, { 53, 55 } }, { { -1, 42 } }, { { -1, 43 }, { 52, 54 } } };

    private static String[] errorMessages = { "expecting: '(', int const, long const, aop, '!', 'this', 'null', '#result', param, '#old', boolean const, ident", "expecting: ')', '.', ',', aop, mop, equalop, relop, logicop, EOF", "expecting: int const, long const", "expecting: '('", "expecting: '(', ')', '.', ',', aop, mop, equalop, relop, logicop, EOF", "expecting: EOF", "expecting: ')', ',', EOF", "expecting: ')', ',', logicop, EOF", "expecting: ')', ',', equalop, relop, logicop, EOF", "expecting: ')', ',', aop, equalop, relop, logicop, EOF", "expecting: ')', ',', aop, mop, equalop, relop, logicop, EOF", "expecting: ')'", "expecting: '(', ')', int const, long const, aop, '!', 'this', 'null', '#result', param, '#old', boolean const, ident", "expecting: ident", "expecting: ')', ','" };

    private static int[] errors = { 0, 0, 1, 1, 2, 0, 1, 1, 1, 1, 3, 1, 4, 5, 5, 6, 6, 7, 8, 9, 10, 10, 1, 1, 1, 1, 11, 1, 1, 10, 0, 12, 0, 0, 0, 0, 0, 13, 1, 11, 14, 11, 14, 14, 8, 9, 9, 10, 10, 4, 1, 1, 0, 12, 14, 11, 1 };
}

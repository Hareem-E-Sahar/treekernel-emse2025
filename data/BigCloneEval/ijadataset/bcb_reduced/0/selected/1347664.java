package smile.parser;

import smile.lexer.*;
import smile.node.*;
import smile.analysis.*;
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
                                push(goTo(0), list, false);
                            }
                            break;
                        case 2:
                            {
                                ArrayList list = new2();
                                push(goTo(1), list, false);
                            }
                            break;
                        case 3:
                            {
                                ArrayList list = new3();
                                push(goTo(1), list, false);
                            }
                            break;
                        case 4:
                            {
                                ArrayList list = new4();
                                push(goTo(1), list, false);
                            }
                            break;
                        case 5:
                            {
                                ArrayList list = new5();
                                push(goTo(1), list, false);
                            }
                            break;
                        case 6:
                            {
                                ArrayList list = new6();
                                push(goTo(1), list, false);
                            }
                            break;
                        case 7:
                            {
                                ArrayList list = new7();
                                push(goTo(1), list, false);
                            }
                            break;
                        case 8:
                            {
                                ArrayList list = new8();
                                push(goTo(1), list, false);
                            }
                            break;
                        case 9:
                            {
                                ArrayList list = new9();
                                push(goTo(1), list, false);
                            }
                            break;
                        case 10:
                            {
                                ArrayList list = new10();
                                push(goTo(1), list, false);
                            }
                            break;
                    }
                    break;
                case ACCEPT:
                    {
                        EOF node2 = (EOF) this.lexer.next();
                        PStmtlist node1 = (PStmtlist) pop().get(0);
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
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmtlist pstmtlistNode1;
        {
            PStmt pstmtNode2;
            PStmtlist pstmtlistNode3;
            pstmtNode2 = (PStmt) nodeArrayList1.get(0);
            pstmtlistNode3 = (PStmtlist) nodeArrayList2.get(0);
            pstmtlistNode1 = new AStmtlist(pstmtNode2, pstmtlistNode3);
        }
        nodeList.add(pstmtlistNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new1() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        PStmtlist pstmtlistNode1;
        {
            pstmtlistNode1 = new AEmptyStmtlist();
        }
        nodeList.add(pstmtlistNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new2() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmt pstmtNode1;
        {
            TNextindex tnextindexNode2;
            tnextindexNode2 = (TNextindex) nodeArrayList1.get(0);
            pstmtNode1 = new ANextindexStmt(tnextindexNode2);
        }
        nodeList.add(pstmtNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new3() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmt pstmtNode1;
        {
            TPrevindex tprevindexNode2;
            tprevindexNode2 = (TPrevindex) nodeArrayList1.get(0);
            pstmtNode1 = new APrevindexStmt(tprevindexNode2);
        }
        nodeList.add(pstmtNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new4() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmt pstmtNode1;
        {
            TInc tincNode2;
            tincNode2 = (TInc) nodeArrayList1.get(0);
            pstmtNode1 = new AIncStmt(tincNode2);
        }
        nodeList.add(pstmtNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new5() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmt pstmtNode1;
        {
            TDec tdecNode2;
            tdecNode2 = (TDec) nodeArrayList1.get(0);
            pstmtNode1 = new ADecStmt(tdecNode2);
        }
        nodeList.add(pstmtNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new6() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmt pstmtNode1;
        {
            TPrint tprintNode2;
            tprintNode2 = (TPrint) nodeArrayList1.get(0);
            pstmtNode1 = new APrintStmt(tprintNode2);
        }
        nodeList.add(pstmtNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new7() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmt pstmtNode1;
        {
            TPrintchar tprintcharNode2;
            tprintcharNode2 = (TPrintchar) nodeArrayList1.get(0);
            pstmtNode1 = new APrintcharStmt(tprintcharNode2);
        }
        nodeList.add(pstmtNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new8() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmt pstmtNode1;
        {
            TWhileint twhileintNode2;
            PStmtlist pstmtlistNode3;
            TWhileend twhileendNode4;
            twhileintNode2 = (TWhileint) nodeArrayList1.get(0);
            pstmtlistNode3 = (PStmtlist) nodeArrayList2.get(0);
            twhileendNode4 = (TWhileend) nodeArrayList3.get(0);
            pstmtNode1 = new AWhileStmt(twhileintNode2, pstmtlistNode3, twhileendNode4);
        }
        nodeList.add(pstmtNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new9() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList3 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList2 = pop();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmt pstmtNode1;
        {
            TIf tifNode2;
            PStmtlist pstmtlistNode3;
            TEndif tendifNode4;
            tifNode2 = (TIf) nodeArrayList1.get(0);
            pstmtlistNode3 = (PStmtlist) nodeArrayList2.get(0);
            tendifNode4 = (TEndif) nodeArrayList3.get(0);
            pstmtNode1 = new AIfStmt(tifNode2, pstmtlistNode3, tendifNode4);
        }
        nodeList.add(pstmtNode1);
        return nodeList;
    }

    @SuppressWarnings("unchecked")
    ArrayList new10() {
        @SuppressWarnings("hiding") ArrayList nodeList = new ArrayList();
        @SuppressWarnings("unused") ArrayList nodeArrayList1 = pop();
        PStmt pstmtNode1;
        {
            TInputchar tinputcharNode2;
            tinputcharNode2 = (TInputchar) nodeArrayList1.get(0);
            pstmtNode1 = new AInputcharStmt(tinputcharNode2);
        }
        nodeList.add(pstmtNode1);
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

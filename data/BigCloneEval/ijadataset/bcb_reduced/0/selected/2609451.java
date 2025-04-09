package iwork.patchpanel.parser;

import iwork.patchpanel.lexer.*;
import iwork.patchpanel.node.*;
import iwork.patchpanel.analysis.*;
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
                DataInputStream s = new DataInputStream(new BufferedInputStream(Parser.class.getResourceAsStream("/iwork/patchpanel/parser/parser.dat")));
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
                                push(goTo(1), node, true);
                            }
                            break;
                        case 2:
                            {
                                Node node = new2();
                                push(goTo(2), node, true);
                            }
                            break;
                        case 3:
                            {
                                Node node = new3();
                                push(goTo(2), node, true);
                            }
                            break;
                        case 4:
                            {
                                Node node = new4();
                                push(goTo(3), node, true);
                            }
                            break;
                        case 5:
                            {
                                Node node = new5();
                                push(goTo(3), node, true);
                            }
                            break;
                        case 6:
                            {
                                Node node = new6();
                                push(goTo(3), node, true);
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
                                push(goTo(4), node, true);
                            }
                            break;
                        case 9:
                            {
                                Node node = new9();
                                push(goTo(4), node, true);
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
                                push(goTo(5), node, true);
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
                                push(goTo(6), node, true);
                            }
                            break;
                        case 16:
                            {
                                Node node = new16();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 17:
                            {
                                Node node = new17();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 18:
                            {
                                Node node = new18();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 19:
                            {
                                Node node = new19();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 20:
                            {
                                Node node = new20();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 21:
                            {
                                Node node = new21();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 22:
                            {
                                Node node = new22();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 23:
                            {
                                Node node = new23();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 24:
                            {
                                Node node = new24();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 25:
                            {
                                Node node = new25();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 26:
                            {
                                Node node = new26();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 27:
                            {
                                Node node = new27();
                                push(goTo(6), node, true);
                            }
                            break;
                        case 28:
                            {
                                Node node = new28();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 29:
                            {
                                Node node = new29();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 30:
                            {
                                Node node = new30();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 31:
                            {
                                Node node = new31();
                                push(goTo(8), node, true);
                            }
                            break;
                        case 32:
                            {
                                Node node = new32();
                                push(goTo(8), node, true);
                            }
                            break;
                        case 33:
                            {
                                Node node = new33();
                                push(goTo(8), node, true);
                            }
                            break;
                        case 34:
                            {
                                Node node = new34();
                                push(goTo(8), node, true);
                            }
                            break;
                        case 35:
                            {
                                Node node = new35();
                                push(goTo(8), node, true);
                            }
                            break;
                        case 36:
                            {
                                Node node = new36();
                                push(goTo(9), node, true);
                            }
                            break;
                        case 37:
                            {
                                Node node = new37();
                                push(goTo(9), node, true);
                            }
                            break;
                        case 38:
                            {
                                Node node = new38();
                                push(goTo(9), node, true);
                            }
                            break;
                        case 39:
                            {
                                Node node = new39();
                                push(goTo(10), node, true);
                            }
                            break;
                        case 40:
                            {
                                Node node = new40();
                                push(goTo(10), node, true);
                            }
                            break;
                        case 41:
                            {
                                Node node = new41();
                                push(goTo(10), node, true);
                            }
                            break;
                        case 42:
                            {
                                Node node = new42();
                                push(goTo(10), node, true);
                            }
                            break;
                        case 43:
                            {
                                Node node = new43();
                                push(goTo(10), node, true);
                            }
                            break;
                        case 44:
                            {
                                Node node = new44();
                                push(goTo(11), node, true);
                            }
                            break;
                        case 45:
                            {
                                Node node = new45();
                                push(goTo(11), node, true);
                            }
                            break;
                        case 46:
                            {
                                Node node = new46();
                                push(goTo(11), node, true);
                            }
                            break;
                        case 47:
                            {
                                Node node = new47();
                                push(goTo(12), node, true);
                            }
                            break;
                        case 48:
                            {
                                Node node = new48();
                                push(goTo(12), node, true);
                            }
                            break;
                        case 49:
                            {
                                Node node = new49();
                                push(goTo(13), node, true);
                            }
                            break;
                        case 50:
                            {
                                Node node = new50();
                                push(goTo(13), node, true);
                            }
                            break;
                        case 51:
                            {
                                Node node = new51();
                                push(goTo(14), node, true);
                            }
                            break;
                        case 52:
                            {
                                Node node = new52();
                                push(goTo(14), node, true);
                            }
                            break;
                        case 53:
                            {
                                Node node = new53();
                                push(goTo(15), node, true);
                            }
                            break;
                        case 54:
                            {
                                Node node = new54();
                                push(goTo(16), node, true);
                            }
                            break;
                        case 55:
                            {
                                Node node = new55();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 56:
                            {
                                Node node = new56();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 57:
                            {
                                Node node = new57();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 58:
                            {
                                Node node = new58();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 59:
                            {
                                Node node = new59();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 60:
                            {
                                Node node = new60();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 61:
                            {
                                Node node = new61();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 62:
                            {
                                Node node = new62();
                                push(goTo(17), node, true);
                            }
                            break;
                    }
                    break;
                case ACCEPT:
                    {
                        EOF node2 = (EOF) lexer.next();
                        PEqExpression node1 = (PEqExpression) pop();
                        Start node = new Start(node1, node2);
                        return node;
                    }
                case ERROR:
                    throw new ParserException(last_token, "[" + last_line + "," + last_pos + "] " + errorMessages[errors[action[1]]]);
            }
        }
    }

    Node new0() {
        PExpression node5 = (PExpression) pop();
        TRParenthese node4 = (TRParenthese) pop();
        PType node3 = (PType) pop();
        TLParenthese node2 = (TLParenthese) pop();
        TAssign node1 = (TAssign) pop();
        AEqExpression node = new AEqExpression(node1, node2, node3, node4, node5);
        return node;
    }

    Node new1() {
        PTernaryExpression node1 = (PTernaryExpression) pop();
        AExpression node = new AExpression(node1);
        return node;
    }

    Node new2() {
        TTrue node1 = (TTrue) pop();
        ATrueBooleanLiteral node = new ATrueBooleanLiteral(node1);
        return node;
    }

    Node new3() {
        TFalse node1 = (TFalse) pop();
        AFalseBooleanLiteral node = new AFalseBooleanLiteral(node1);
        return node;
    }

    Node new4() {
        TIntegerLiteral node2 = (TIntegerLiteral) pop();
        TMinus node1 = null;
        AIntegerLiteralNumericalLiteral node = new AIntegerLiteralNumericalLiteral(node1, node2);
        return node;
    }

    Node new5() {
        TIntegerLiteral node2 = (TIntegerLiteral) pop();
        TMinus node1 = (TMinus) pop();
        AIntegerLiteralNumericalLiteral node = new AIntegerLiteralNumericalLiteral(node1, node2);
        return node;
    }

    Node new6() {
        TFloatingPointLiteral node2 = (TFloatingPointLiteral) pop();
        TMinus node1 = null;
        AFloatingPointLiteralNumericalLiteral node = new AFloatingPointLiteralNumericalLiteral(node1, node2);
        return node;
    }

    Node new7() {
        TFloatingPointLiteral node2 = (TFloatingPointLiteral) pop();
        TMinus node1 = (TMinus) pop();
        AFloatingPointLiteralNumericalLiteral node = new AFloatingPointLiteralNumericalLiteral(node1, node2);
        return node;
    }

    Node new8() {
        PNumericalLiteral node1 = (PNumericalLiteral) pop();
        ANumericalLiteralLiteral node = new ANumericalLiteralLiteral(node1);
        return node;
    }

    Node new9() {
        PBooleanLiteral node1 = (PBooleanLiteral) pop();
        ABooleanLiteralLiteral node = new ABooleanLiteralLiteral(node1);
        return node;
    }

    Node new10() {
        TStringLiteral node1 = (TStringLiteral) pop();
        AStringLiteralLiteral node = new AStringLiteralLiteral(node1);
        return node;
    }

    Node new11() {
        PLiteral node1 = (PLiteral) pop();
        ALiteralPrimary node = new ALiteralPrimary(node1);
        return node;
    }

    Node new12() {
        TRParenthese node3 = (TRParenthese) pop();
        PExpression node2 = (PExpression) pop();
        TLParenthese node1 = (TLParenthese) pop();
        AParenthesePrimary node = new AParenthesePrimary(node1, node2, node3);
        return node;
    }

    Node new13() {
        TIdentifier node1 = (TIdentifier) pop();
        AIdentifierPrimary node = new AIdentifierPrimary(node1);
        return node;
    }

    Node new14() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TComplement node1 = (TComplement) pop();
        ACompletmentUnaryExpression node = new ACompletmentUnaryExpression(node1, node2);
        return node;
    }

    Node new15() {
        PPrimary node1 = (PPrimary) pop();
        APrimaryUnaryExpression node = new APrimaryUnaryExpression(node1);
        return node;
    }

    Node new16() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TCos node1 = (TCos) pop();
        ACosUnaryExpression node = new ACosUnaryExpression(node1, node2);
        return node;
    }

    Node new17() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TSin node1 = (TSin) pop();
        ASinUnaryExpression node = new ASinUnaryExpression(node1, node2);
        return node;
    }

    Node new18() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TTan node1 = (TTan) pop();
        ATanUnaryExpression node = new ATanUnaryExpression(node1, node2);
        return node;
    }

    Node new19() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TAcos node1 = (TAcos) pop();
        AAcosUnaryExpression node = new AAcosUnaryExpression(node1, node2);
        return node;
    }

    Node new20() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TAsin node1 = (TAsin) pop();
        AAsinUnaryExpression node = new AAsinUnaryExpression(node1, node2);
        return node;
    }

    Node new21() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TAtan node1 = (TAtan) pop();
        AAtanUnaryExpression node = new AAtanUnaryExpression(node1, node2);
        return node;
    }

    Node new22() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TSqrt node1 = (TSqrt) pop();
        ASqrtUnaryExpression node = new ASqrtUnaryExpression(node1, node2);
        return node;
    }

    Node new23() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TExp node1 = (TExp) pop();
        AExpUnaryExpression node = new AExpUnaryExpression(node1, node2);
        return node;
    }

    Node new24() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TLog node1 = (TLog) pop();
        ALogUnaryExpression node = new ALogUnaryExpression(node1, node2);
        return node;
    }

    Node new25() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TFloor node1 = (TFloor) pop();
        AFloorUnaryExpression node = new AFloorUnaryExpression(node1, node2);
        return node;
    }

    Node new26() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TCeil node1 = (TCeil) pop();
        ACeilUnaryExpression node = new ACeilUnaryExpression(node1, node2);
        return node;
    }

    Node new27() {
        PUnaryExpression node2 = (PUnaryExpression) pop();
        TAbs node1 = (TAbs) pop();
        AAbsUnaryExpression node = new AAbsUnaryExpression(node1, node2);
        return node;
    }

    Node new28() {
        PUnaryExpression node1 = (PUnaryExpression) pop();
        AUnaryExpressionComparativeExpression node = new AUnaryExpressionComparativeExpression(node1);
        return node;
    }

    Node new29() {
        TRParenthese node6 = (TRParenthese) pop();
        PUnaryExpression node5 = (PUnaryExpression) pop();
        TComma node4 = (TComma) pop();
        PComparativeExpression node3 = (PComparativeExpression) pop();
        TLParenthese node2 = (TLParenthese) pop();
        TMin node1 = (TMin) pop();
        AMinComparativeExpression node = new AMinComparativeExpression(node1, node2, node3, node4, node5, node6);
        return node;
    }

    Node new30() {
        TRParenthese node6 = (TRParenthese) pop();
        PUnaryExpression node5 = (PUnaryExpression) pop();
        TComma node4 = (TComma) pop();
        PComparativeExpression node3 = (PComparativeExpression) pop();
        TLParenthese node2 = (TLParenthese) pop();
        TMax node1 = (TMax) pop();
        AMaxComparativeExpression node = new AMaxComparativeExpression(node1, node2, node3, node4, node5, node6);
        return node;
    }

    Node new31() {
        PComparativeExpression node1 = (PComparativeExpression) pop();
        AUnaryExpressionMultiplicativeExpression node = new AUnaryExpressionMultiplicativeExpression(node1);
        return node;
    }

    Node new32() {
        PUnaryExpression node3 = (PUnaryExpression) pop();
        TStar node2 = (TStar) pop();
        PMultiplicativeExpression node1 = (PMultiplicativeExpression) pop();
        AMulMultiplicativeExpression node = new AMulMultiplicativeExpression(node1, node2, node3);
        return node;
    }

    Node new33() {
        PUnaryExpression node3 = (PUnaryExpression) pop();
        TDiv node2 = (TDiv) pop();
        PMultiplicativeExpression node1 = (PMultiplicativeExpression) pop();
        ADivMultiplicativeExpression node = new ADivMultiplicativeExpression(node1, node2, node3);
        return node;
    }

    Node new34() {
        PUnaryExpression node3 = (PUnaryExpression) pop();
        TMod node2 = (TMod) pop();
        PMultiplicativeExpression node1 = (PMultiplicativeExpression) pop();
        AModMultiplicativeExpression node = new AModMultiplicativeExpression(node1, node2, node3);
        return node;
    }

    Node new35() {
        PUnaryExpression node3 = (PUnaryExpression) pop();
        TPower node2 = (TPower) pop();
        PMultiplicativeExpression node1 = (PMultiplicativeExpression) pop();
        APowerMultiplicativeExpression node = new APowerMultiplicativeExpression(node1, node2, node3);
        return node;
    }

    Node new36() {
        PMultiplicativeExpression node1 = (PMultiplicativeExpression) pop();
        AMultiplicativeExpressionAdditiveExpression node = new AMultiplicativeExpressionAdditiveExpression(node1);
        return node;
    }

    Node new37() {
        PMultiplicativeExpression node3 = (PMultiplicativeExpression) pop();
        TPlus node2 = (TPlus) pop();
        PAdditiveExpression node1 = (PAdditiveExpression) pop();
        APlusAdditiveExpression node = new APlusAdditiveExpression(node1, node2, node3);
        return node;
    }

    Node new38() {
        PMultiplicativeExpression node3 = (PMultiplicativeExpression) pop();
        TMinus node2 = (TMinus) pop();
        PAdditiveExpression node1 = (PAdditiveExpression) pop();
        AMinusAdditiveExpression node = new AMinusAdditiveExpression(node1, node2, node3);
        return node;
    }

    Node new39() {
        PAdditiveExpression node1 = (PAdditiveExpression) pop();
        AAdditiveExpressionRelationalExpression node = new AAdditiveExpressionRelationalExpression(node1);
        return node;
    }

    Node new40() {
        PAdditiveExpression node3 = (PAdditiveExpression) pop();
        TLt node2 = (TLt) pop();
        PRelationalExpression node1 = (PRelationalExpression) pop();
        ALtRelationalExpression node = new ALtRelationalExpression(node1, node2, node3);
        return node;
    }

    Node new41() {
        PAdditiveExpression node3 = (PAdditiveExpression) pop();
        TGt node2 = (TGt) pop();
        PRelationalExpression node1 = (PRelationalExpression) pop();
        AGtRelationalExpression node = new AGtRelationalExpression(node1, node2, node3);
        return node;
    }

    Node new42() {
        PAdditiveExpression node3 = (PAdditiveExpression) pop();
        TLteq node2 = (TLteq) pop();
        PRelationalExpression node1 = (PRelationalExpression) pop();
        ALteqRelationalExpression node = new ALteqRelationalExpression(node1, node2, node3);
        return node;
    }

    Node new43() {
        PAdditiveExpression node3 = (PAdditiveExpression) pop();
        TGteq node2 = (TGteq) pop();
        PRelationalExpression node1 = (PRelationalExpression) pop();
        AGteqRelationalExpression node = new AGteqRelationalExpression(node1, node2, node3);
        return node;
    }

    Node new44() {
        PRelationalExpression node1 = (PRelationalExpression) pop();
        ARelationalExpressionEqualityExpression node = new ARelationalExpressionEqualityExpression(node1);
        return node;
    }

    Node new45() {
        PRelationalExpression node3 = (PRelationalExpression) pop();
        TEq node2 = (TEq) pop();
        PEqualityExpression node1 = (PEqualityExpression) pop();
        ATermEqEqualityExpression node = new ATermEqEqualityExpression(node1, node2, node3);
        return node;
    }

    Node new46() {
        PRelationalExpression node3 = (PRelationalExpression) pop();
        TNeq node2 = (TNeq) pop();
        PEqualityExpression node1 = (PEqualityExpression) pop();
        ATermNeqEqualityExpression node = new ATermNeqEqualityExpression(node1, node2, node3);
        return node;
    }

    Node new47() {
        PEqualityExpression node1 = (PEqualityExpression) pop();
        AEqualityExpressionConditionalAndExpression node = new AEqualityExpressionConditionalAndExpression(node1);
        return node;
    }

    Node new48() {
        PEqualityExpression node3 = (PEqualityExpression) pop();
        TAnd node2 = (TAnd) pop();
        PConditionalAndExpression node1 = (PConditionalAndExpression) pop();
        AConditionalAndExpressionConditionalAndExpression node = new AConditionalAndExpressionConditionalAndExpression(node1, node2, node3);
        return node;
    }

    Node new49() {
        PConditionalAndExpression node1 = (PConditionalAndExpression) pop();
        AConditionalAndExpressionConditionalOrExpression node = new AConditionalAndExpressionConditionalOrExpression(node1);
        return node;
    }

    Node new50() {
        PConditionalAndExpression node3 = (PConditionalAndExpression) pop();
        TOr node2 = (TOr) pop();
        PConditionalOrExpression node1 = (PConditionalOrExpression) pop();
        AConditionalOrExpressionConditionalOrExpression node = new AConditionalOrExpressionConditionalOrExpression(node1, node2, node3);
        return node;
    }

    Node new51() {
        PConditionalOrExpression node1 = (PConditionalOrExpression) pop();
        AConditionalOrExpressionTernaryExpression node = new AConditionalOrExpressionTernaryExpression(node1);
        return node;
    }

    Node new52() {
        PTernaryCaseFalseExpression node5 = (PTernaryCaseFalseExpression) pop();
        TColon node4 = (TColon) pop();
        PTernaryCaseTrueExpression node3 = (PTernaryCaseTrueExpression) pop();
        TTernary node2 = (TTernary) pop();
        PTernaryExpression node1 = (PTernaryExpression) pop();
        ATernaryExpressionTernaryExpression node = new ATernaryExpressionTernaryExpression(node1, node2, node3, node4, node5);
        return node;
    }

    Node new53() {
        PConditionalOrExpression node1 = (PConditionalOrExpression) pop();
        ATernaryCaseTrueExpression node = new ATernaryCaseTrueExpression(node1);
        return node;
    }

    Node new54() {
        PConditionalOrExpression node1 = (PConditionalOrExpression) pop();
        ATernaryCaseFalseExpression node = new ATernaryCaseFalseExpression(node1);
        return node;
    }

    Node new55() {
        TString node1 = (TString) pop();
        AStringType node = new AStringType(node1);
        return node;
    }

    Node new56() {
        TBoolean node1 = (TBoolean) pop();
        ABooleanType node = new ABooleanType(node1);
        return node;
    }

    Node new57() {
        TByte node1 = (TByte) pop();
        AByteType node = new AByteType(node1);
        return node;
    }

    Node new58() {
        TInt node1 = (TInt) pop();
        AIntType node = new AIntType(node1);
        return node;
    }

    Node new59() {
        TDouble node1 = (TDouble) pop();
        ADoubleType node = new ADoubleType(node1);
        return node;
    }

    Node new60() {
        TFloat node1 = (TFloat) pop();
        AFloatType node = new AFloatType(node1);
        return node;
    }

    Node new61() {
        TLong node1 = (TLong) pop();
        ALongType node = new ALongType(node1);
        return node;
    }

    Node new62() {
        TShort node1 = (TShort) pop();
        AShortType node = new AShortType(node1);
        return node;
    }

    private static int[][][] actionTable;

    private static int[][][] gotoTable;

    private static String[] errorMessages;

    private static int[] errors;
}

package net.sf.dsaman.model.arithmetic.parser;

import net.sf.dsaman.model.arithmetic.lexer.*;
import net.sf.dsaman.model.arithmetic.node.*;
import net.sf.dsaman.model.arithmetic.analysis.*;
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
                                push(goTo(1), node, true);
                            }
                            break;
                        case 3:
                            {
                                Node node = new3();
                                push(goTo(1), node, true);
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
                                push(goTo(2), node, true);
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
                                push(goTo(3), node, true);
                            }
                            break;
                        case 11:
                            {
                                Node node = new11();
                                push(goTo(4), node, true);
                            }
                            break;
                        case 12:
                            {
                                Node node = new12();
                                push(goTo(4), node, true);
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
                                push(goTo(5), node, true);
                            }
                            break;
                        case 15:
                            {
                                Node node = new15();
                                push(goTo(5), node, true);
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
                                push(goTo(7), node, true);
                            }
                            break;
                        case 19:
                            {
                                Node node = new19();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 20:
                            {
                                Node node = new20();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 21:
                            {
                                Node node = new21();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 22:
                            {
                                Node node = new22();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 23:
                            {
                                Node node = new23();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 24:
                            {
                                Node node = new24();
                                push(goTo(7), node, true);
                            }
                            break;
                        case 25:
                            {
                                Node node = new25();
                                push(goTo(8), node, true);
                            }
                            break;
                        case 26:
                            {
                                Node node = new26();
                                push(goTo(8), node, true);
                            }
                            break;
                        case 27:
                            {
                                Node node = new27();
                                push(goTo(19), node, false);
                            }
                            break;
                        case 28:
                            {
                                Node node = new28();
                                push(goTo(19), node, false);
                            }
                            break;
                        case 29:
                            {
                                Node node = new29();
                                push(goTo(9), node, true);
                            }
                            break;
                        case 30:
                            {
                                Node node = new30();
                                push(goTo(10), node, true);
                            }
                            break;
                        case 31:
                            {
                                Node node = new31();
                                push(goTo(10), node, true);
                            }
                            break;
                        case 32:
                            {
                                Node node = new32();
                                push(goTo(11), node, true);
                            }
                            break;
                        case 33:
                            {
                                Node node = new33();
                                push(goTo(11), node, true);
                            }
                            break;
                        case 34:
                            {
                                Node node = new34();
                                push(goTo(12), node, true);
                            }
                            break;
                        case 35:
                            {
                                Node node = new35();
                                push(goTo(12), node, true);
                            }
                            break;
                        case 36:
                            {
                                Node node = new36();
                                push(goTo(12), node, true);
                            }
                            break;
                        case 37:
                            {
                                Node node = new37();
                                push(goTo(12), node, true);
                            }
                            break;
                        case 38:
                            {
                                Node node = new38();
                                push(goTo(12), node, true);
                            }
                            break;
                        case 39:
                            {
                                Node node = new39();
                                push(goTo(13), node, true);
                            }
                            break;
                        case 40:
                            {
                                Node node = new40();
                                push(goTo(14), node, true);
                            }
                            break;
                        case 41:
                            {
                                Node node = new41();
                                push(goTo(15), node, true);
                            }
                            break;
                        case 42:
                            {
                                Node node = new42();
                                push(goTo(15), node, true);
                            }
                            break;
                        case 43:
                            {
                                Node node = new43();
                                push(goTo(16), node, true);
                            }
                            break;
                        case 44:
                            {
                                Node node = new44();
                                push(goTo(16), node, true);
                            }
                            break;
                        case 45:
                            {
                                Node node = new45();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 46:
                            {
                                Node node = new46();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 47:
                            {
                                Node node = new47();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 48:
                            {
                                Node node = new48();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 49:
                            {
                                Node node = new49();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 50:
                            {
                                Node node = new50();
                                push(goTo(17), node, true);
                            }
                            break;
                        case 51:
                            {
                                Node node = new51();
                                push(goTo(18), node, true);
                            }
                            break;
                        case 52:
                            {
                                Node node = new52();
                                push(goTo(18), node, true);
                            }
                            break;
                    }
                    break;
                case ACCEPT:
                    {
                        EOF node2 = (EOF) lexer.next();
                        PExpression node1 = (PExpression) pop();
                        Start node = new Start(node1, node2);
                        return node;
                    }
                case ERROR:
                    throw new ParserException(last_token, "[" + last_line + "," + last_pos + "] " + errorMessages[errors[action[1]]]);
            }
        }
    }

    Node new0() {
        PArithmeticExpression node1 = (PArithmeticExpression) pop();
        AArithmeticExpressionExpression node = new AArithmeticExpressionExpression(node1);
        return node;
    }

    Node new1() {
        PBooleanExpression node1 = (PBooleanExpression) pop();
        ABooleanExpressionExpression node = new ABooleanExpressionExpression(node1);
        return node;
    }

    Node new2() {
        PTerm node1 = (PTerm) pop();
        ATermArithmeticExpression node = new ATermArithmeticExpression(node1);
        return node;
    }

    Node new3() {
        PTerm node3 = (PTerm) pop();
        TOpPlus node2 = (TOpPlus) pop();
        PArithmeticExpression node1 = (PArithmeticExpression) pop();
        AAdditionArithmeticExpression node = new AAdditionArithmeticExpression(node1, node2, node3);
        return node;
    }

    Node new4() {
        PTerm node3 = (PTerm) pop();
        TOpMinus node2 = (TOpMinus) pop();
        PArithmeticExpression node1 = (PArithmeticExpression) pop();
        ASubstractionArithmeticExpression node = new ASubstractionArithmeticExpression(node1, node2, node3);
        return node;
    }

    Node new5() {
        PFactor node1 = (PFactor) pop();
        AFactorTerm node = new AFactorTerm(node1);
        return node;
    }

    Node new6() {
        PFactor node3 = (PFactor) pop();
        TOpMult node2 = (TOpMult) pop();
        PTerm node1 = (PTerm) pop();
        AMultiplicationTerm node = new AMultiplicationTerm(node1, node2, node3);
        return node;
    }

    Node new7() {
        PFactor node3 = (PFactor) pop();
        TOpDiv node2 = (TOpDiv) pop();
        PTerm node1 = (PTerm) pop();
        ADivisionTerm node = new ADivisionTerm(node1, node2, node3);
        return node;
    }

    Node new8() {
        PExponent node2 = (PExponent) pop();
        PSign node1 = null;
        AExponentFactor node = new AExponentFactor(node1, node2);
        return node;
    }

    Node new9() {
        PExponent node2 = (PExponent) pop();
        PSign node1 = (PSign) pop();
        AExponentFactor node = new AExponentFactor(node1, node2);
        return node;
    }

    Node new10() {
        PExponent node3 = (PExponent) pop();
        TOpExp node2 = (TOpExp) pop();
        PFactor node1 = (PFactor) pop();
        AExponentiationFactor node = new AExponentiationFactor(node1, node2, node3);
        return node;
    }

    Node new11() {
        TOpPlus node1 = (TOpPlus) pop();
        ASignPlusSign node = new ASignPlusSign(node1);
        return node;
    }

    Node new12() {
        TOpMinus node1 = (TOpMinus) pop();
        ASignMinusSign node = new ASignMinusSign(node1);
        return node;
    }

    Node new13() {
        PAtomicValueNumber node1 = (PAtomicValueNumber) pop();
        AAtomicValueNumberExponent node = new AAtomicValueNumberExponent(node1);
        return node;
    }

    Node new14() {
        TRPar node3 = (TRPar) pop();
        PArithmeticExpression node2 = (PArithmeticExpression) pop();
        TLPar node1 = (TLPar) pop();
        ABracketedArithmeticExpressionExponent node = new ABracketedArithmeticExpressionExponent(node1, node2, node3);
        return node;
    }

    Node new15() {
        PArithmeticFunction node1 = (PArithmeticFunction) pop();
        AArithmeticFunctionExponent node = new AArithmeticFunctionExponent(node1);
        return node;
    }

    Node new16() {
        TIdentifierPath node1 = (TIdentifierPath) pop();
        AIdentifierOfTypeNumericAtomicValueNumber node = new AIdentifierOfTypeNumericAtomicValueNumber(node1);
        return node;
    }

    Node new17() {
        TFloatConstant node1 = (TFloatConstant) pop();
        ANumberConstantAtomicValueNumber node = new ANumberConstantAtomicValueNumber(node1);
        return node;
    }

    Node new18() {
        TRPar node7 = (TRPar) pop();
        PArithmeticExpression node6 = (PArithmeticExpression) pop();
        TColon node5 = (TColon) pop();
        PArithmeticExpression node4 = (PArithmeticExpression) pop();
        TQuestionMark node3 = (TQuestionMark) pop();
        PBooleanExpression node2 = (PBooleanExpression) pop();
        TLPar node1 = (TLPar) pop();
        AConditionalExpressionArithmeticFunction node = new AConditionalExpressionArithmeticFunction(node1, node2, node3, node4, node5, node6, node7);
        return node;
    }

    Node new19() {
        TRPar node4 = (TRPar) pop();
        PArithmeticExpression node3 = (PArithmeticExpression) pop();
        TLPar node2 = (TLPar) pop();
        TFuncSqrt node1 = (TFuncSqrt) pop();
        AFunctionSquareRootArithmeticFunction node = new AFunctionSquareRootArithmeticFunction(node1, node2, node3, node4);
        return node;
    }

    Node new20() {
        TRPar node4 = (TRPar) pop();
        PArithmeticExpression node3 = (PArithmeticExpression) pop();
        TLPar node2 = (TLPar) pop();
        TFuncRound node1 = (TFuncRound) pop();
        AFunctionRoundArithmeticFunction node = new AFunctionRoundArithmeticFunction(node1, node2, node3, node4);
        return node;
    }

    Node new21() {
        TRPar node4 = (TRPar) pop();
        PArithmeticExpression node3 = (PArithmeticExpression) pop();
        TLPar node2 = (TLPar) pop();
        TFuncCeil node1 = (TFuncCeil) pop();
        AFunctionCeilArithmeticFunction node = new AFunctionCeilArithmeticFunction(node1, node2, node3, node4);
        return node;
    }

    Node new22() {
        TRPar node4 = (TRPar) pop();
        PArithmeticExpression node3 = (PArithmeticExpression) pop();
        TLPar node2 = (TLPar) pop();
        TFuncFloor node1 = (TFuncFloor) pop();
        AFunctionFloorArithmeticFunction node = new AFunctionFloorArithmeticFunction(node1, node2, node3, node4);
        return node;
    }

    Node new23() {
        TRPar node4 = (TRPar) pop();
        PArithmeticParameterList node3 = (PArithmeticParameterList) pop();
        TLPar node2 = (TLPar) pop();
        TFuncMin node1 = (TFuncMin) pop();
        AFunctionMinArithmeticFunction node = new AFunctionMinArithmeticFunction(node1, node2, node3, node4);
        return node;
    }

    Node new24() {
        TRPar node4 = (TRPar) pop();
        PArithmeticParameterList node3 = (PArithmeticParameterList) pop();
        TLPar node2 = (TLPar) pop();
        TFuncMax node1 = (TFuncMax) pop();
        AFunctionMaxArithmeticFunction node = new AFunctionMaxArithmeticFunction(node1, node2, node3, node4);
        return node;
    }

    Node new25() {
        XPPreCommaWithArithmeticExpression node2 = null;
        PArithmeticExpression node1 = (PArithmeticExpression) pop();
        AArithmeticParameterList node = new AArithmeticParameterList(node1, node2);
        return node;
    }

    Node new26() {
        XPPreCommaWithArithmeticExpression node2 = (XPPreCommaWithArithmeticExpression) pop();
        PArithmeticExpression node1 = (PArithmeticExpression) pop();
        AArithmeticParameterList node = new AArithmeticParameterList(node1, node2);
        return node;
    }

    Node new27() {
        PPreCommaWithArithmeticExpression node2 = (PPreCommaWithArithmeticExpression) pop();
        XPPreCommaWithArithmeticExpression node1 = (XPPreCommaWithArithmeticExpression) pop();
        X1PPreCommaWithArithmeticExpression node = new X1PPreCommaWithArithmeticExpression(node1, node2);
        return node;
    }

    Node new28() {
        PPreCommaWithArithmeticExpression node1 = (PPreCommaWithArithmeticExpression) pop();
        X2PPreCommaWithArithmeticExpression node = new X2PPreCommaWithArithmeticExpression(node1);
        return node;
    }

    Node new29() {
        PArithmeticExpression node2 = (PArithmeticExpression) pop();
        TComma node1 = (TComma) pop();
        APreCommaWithArithmeticExpression node = new APreCommaWithArithmeticExpression(node1, node2);
        return node;
    }

    Node new30() {
        PConjunction node1 = (PConjunction) pop();
        ABooleanConjunctionTermBooleanExpression node = new ABooleanConjunctionTermBooleanExpression(node1);
        return node;
    }

    Node new31() {
        PConjunction node3 = (PConjunction) pop();
        TBoolOpOr node2 = (TBoolOpOr) pop();
        PBooleanExpression node1 = (PBooleanExpression) pop();
        ADisjunctionBooleanExpression node = new ADisjunctionBooleanExpression(node1, node2, node3);
        return node;
    }

    Node new32() {
        PBooleanLitteral node1 = (PBooleanLitteral) pop();
        ABooleanLitteralConjunction node = new ABooleanLitteralConjunction(node1);
        return node;
    }

    Node new33() {
        PBooleanLitteral node3 = (PBooleanLitteral) pop();
        TBoolOpAnd node2 = (TBoolOpAnd) pop();
        PConjunction node1 = (PConjunction) pop();
        AConjunctionConjunction node = new AConjunctionConjunction(node1, node2, node3);
        return node;
    }

    Node new34() {
        PAtomicValueBool node1 = (PAtomicValueBool) pop();
        AAtomicValueBoolBooleanLitteral node = new AAtomicValueBoolBooleanLitteral(node1);
        return node;
    }

    Node new35() {
        TRPar node3 = (TRPar) pop();
        PBooleanExpression node2 = (PBooleanExpression) pop();
        TLPar node1 = (TLPar) pop();
        ABracketedBooleanExpressionBooleanLitteral node = new ABracketedBooleanExpressionBooleanLitteral(node1, node2, node3);
        return node;
    }

    Node new36() {
        PComparativeExpression node1 = (PComparativeExpression) pop();
        AComparativeExpressionBooleanLitteral node = new AComparativeExpressionBooleanLitteral(node1);
        return node;
    }

    Node new37() {
        PBooleanLitteral node2 = (PBooleanLitteral) pop();
        TBoolOpNot node1 = (TBoolOpNot) pop();
        ANegationBooleanLitteral node = new ANegationBooleanLitteral(node1, node2);
        return node;
    }

    Node new38() {
        PBooleanFunction node1 = (PBooleanFunction) pop();
        ABooleanFunctionBooleanLitteral node = new ABooleanFunctionBooleanLitteral(node1);
        return node;
    }

    Node new39() {
        TRPar node4 = (TRPar) pop();
        PStringExpression node3 = (PStringExpression) pop();
        TLPar node2 = (TLPar) pop();
        TFuncHas node1 = (TFuncHas) pop();
        AFunctionHasBooleanFunction node = new AFunctionHasBooleanFunction(node1, node2, node3, node4);
        return node;
    }

    Node new40() {
        TBoolConstant node1 = (TBoolConstant) pop();
        AAtomicValueBool node = new AAtomicValueBool(node1);
        return node;
    }

    Node new41() {
        PArithmeticExpression node3 = (PArithmeticExpression) pop();
        PRelOp node2 = (PRelOp) pop();
        PArithmeticExpression node1 = (PArithmeticExpression) pop();
        AArithmeticCompareComparativeExpression node = new AArithmeticCompareComparativeExpression(node1, node2, node3);
        return node;
    }

    Node new42() {
        PStringExpression node3 = (PStringExpression) pop();
        PEqOp node2 = (PEqOp) pop();
        PStringExpression node1 = (PStringExpression) pop();
        AStringCompareComparativeExpression node = new AStringCompareComparativeExpression(node1, node2, node3);
        return node;
    }

    Node new43() {
        TStringLitteral node1 = (TStringLitteral) pop();
        AStringLitteralConstantStringExpression node = new AStringLitteralConstantStringExpression(node1);
        return node;
    }

    Node new44() {
        TIdentifierPath node1 = (TIdentifierPath) pop();
        AIdentifierOfTypeStringStringExpression node = new AIdentifierOfTypeStringStringExpression(node1);
        return node;
    }

    Node new45() {
        TRelOpLt node1 = (TRelOpLt) pop();
        ARelOpLtRelOp node = new ARelOpLtRelOp(node1);
        return node;
    }

    Node new46() {
        TRelOpLe node1 = (TRelOpLe) pop();
        ARelOpLeRelOp node = new ARelOpLeRelOp(node1);
        return node;
    }

    Node new47() {
        TRelOpGt node1 = (TRelOpGt) pop();
        ARelOpGtRelOp node = new ARelOpGtRelOp(node1);
        return node;
    }

    Node new48() {
        TRelOpGe node1 = (TRelOpGe) pop();
        ARelOpGeRelOp node = new ARelOpGeRelOp(node1);
        return node;
    }

    Node new49() {
        TRelOpEq node1 = (TRelOpEq) pop();
        ARelOpEqRelOp node = new ARelOpEqRelOp(node1);
        return node;
    }

    Node new50() {
        TRelOpNe node1 = (TRelOpNe) pop();
        ARelOpNeRelOp node = new ARelOpNeRelOp(node1);
        return node;
    }

    Node new51() {
        TStrRelOpEq node1 = (TStrRelOpEq) pop();
        AStrRelOpEqEqOp node = new AStrRelOpEqEqOp(node1);
        return node;
    }

    Node new52() {
        TStrRelOpNe node1 = (TStrRelOpNe) pop();
        AStrRelOpNeEqOp node = new AStrRelOpNeEqOp(node1);
        return node;
    }

    private static int[][][] actionTable;

    private static int[][][] gotoTable;

    private static String[] errorMessages;

    private static int[] errors;
}

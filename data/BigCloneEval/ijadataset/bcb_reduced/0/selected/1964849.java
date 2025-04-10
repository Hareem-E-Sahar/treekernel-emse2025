package org.dllearner.parser;

import org.dllearner.core.owl.*;
import java.io.*;
import java.net.URL;

@SuppressWarnings("all")
public class KBParser implements KBParserConstants {

    public static String internalNamespace = "http://localhost/foo#";

    public static String getInternalURI(String name) {
        if (name.startsWith("http://")) return name; else return internalNamespace + name;
    }

    public static Description parseConcept(String string) throws ParseException {
        String eq = "tmp = " + string + ".";
        KBParser parser = new KBParser(new StringReader(eq));
        EquivalentClassesAxiom eqAxiom = parser.TBoxEquiv();
        return eqAxiom.getConcept2();
    }

    public static Description parseConcept(String string, String namespace) throws ParseException {
        internalNamespace = namespace;
        return parseConcept(string);
    }

    public static KB parseKBFile(String content) throws ParseException {
        KBParser parser = new KBParser(new StringReader(content));
        return parser.KB();
    }

    public static KB parseKBFile(URL url) throws IOException, ParseException {
        KBParser parser = new KBParser(url.openStream());
        return parser.KB();
    }

    public static KB parseKBFile(File file) throws FileNotFoundException, ParseException {
        KBParser parser = new KBParser(new FileInputStream(file));
        return parser.KB();
    }

    public final KB KB() throws ParseException {
        ClassAssertionAxiom conceptAssertion;
        ObjectPropertyAssertion roleAssertion;
        PropertyAxiom rBoxAxiom;
        EquivalentClassesAxiom equality;
        SubClassAxiom inclusion;
        KB kb = new KB();
        label_1: while (true) {
            switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case ID:
                case TOP:
                case BOTTOM:
                case EXISTS:
                case ALL:
                case NOT:
                case GE:
                case LE:
                case STRING:
                case 22:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                    ;
                    break;
                default:
                    jj_la1[0] = jj_gen;
                    break label_1;
            }
            if (jj_2_1(2147483647)) {
                conceptAssertion = ABoxConcept();
                kb.addABoxAxiom(conceptAssertion);
            } else if (jj_2_2(2147483647)) {
                roleAssertion = ABoxRole();
                kb.addABoxAxiom(roleAssertion);
            } else {
                switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                    case 28:
                        rBoxAxiom = Transitive();
                        kb.addRBoxAxiom(rBoxAxiom);
                        break;
                    case 29:
                        rBoxAxiom = Functional();
                        kb.addRBoxAxiom(rBoxAxiom);
                        break;
                    case 30:
                        rBoxAxiom = Symmetric();
                        kb.addRBoxAxiom(rBoxAxiom);
                        break;
                    case 31:
                        rBoxAxiom = Inverse();
                        kb.addRBoxAxiom(rBoxAxiom);
                        break;
                    case 32:
                        rBoxAxiom = Subrole();
                        kb.addRBoxAxiom(rBoxAxiom);
                        break;
                    case 34:
                    case 35:
                    case 36:
                        rBoxAxiom = ObjectPropertyDomainAxiom();
                        kb.addRBoxAxiom(rBoxAxiom);
                        break;
                    case 37:
                    case 38:
                        rBoxAxiom = DatatypePropertyDomainAxiom();
                        kb.addRBoxAxiom(rBoxAxiom);
                        break;
                    case 39:
                    case 40:
                    case 41:
                        rBoxAxiom = ObjectPropertyRangeAxiom();
                        kb.addRBoxAxiom(rBoxAxiom);
                        break;
                    case 42:
                    case 43:
                        rBoxAxiom = DatatypePropertyRangeAxiom();
                        kb.addRBoxAxiom(rBoxAxiom);
                        break;
                    default:
                        jj_la1[1] = jj_gen;
                        if (jj_2_3(2147483647)) {
                            equality = TBoxEquiv();
                            kb.addTBoxAxiom(equality);
                        } else if (jj_2_4(2147483647)) {
                            inclusion = TBoxSub();
                            kb.addTBoxAxiom(inclusion);
                        } else {
                            jj_consume_token(-1);
                            throw new ParseException();
                        }
                }
            }
        }
        jj_consume_token(0);
        {
            if (true) return kb;
        }
        throw new Error("Missing return statement in function");
    }

    public final ClassAssertionAxiom ABoxConcept() throws ParseException {
        Description c;
        Individual i;
        c = Concept();
        jj_consume_token(22);
        i = Individual();
        jj_consume_token(23);
        jj_consume_token(COMMAND_END);
        {
            if (true) return new ClassAssertionAxiom(c, i);
        }
        throw new Error("Missing return statement in function");
    }

    public final ObjectPropertyAssertion ABoxRole() throws ParseException {
        boolean isNegated = false;
        ObjectProperty ar;
        Individual i1, i2;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case NOT:
                Not();
                isNegated = true;
                break;
            default:
                jj_la1[2] = jj_gen;
                ;
        }
        ar = ObjectProperty();
        jj_consume_token(22);
        i1 = Individual();
        jj_consume_token(24);
        i2 = Individual();
        jj_consume_token(23);
        jj_consume_token(COMMAND_END);
        if (isNegated) {
            if (true) throw new Error("negated role assertions not supported yet");
        } else {
            if (true) return new ObjectPropertyAssertion(ar, i1, i2);
        }
        throw new Error("Missing return statement in function");
    }

    public final TransitiveObjectPropertyAxiom Transitive() throws ParseException {
        ObjectProperty ar;
        jj_consume_token(28);
        jj_consume_token(22);
        ar = ObjectProperty();
        jj_consume_token(23);
        jj_consume_token(COMMAND_END);
        {
            if (true) return new TransitiveObjectPropertyAxiom(ar);
        }
        throw new Error("Missing return statement in function");
    }

    public final FunctionalObjectPropertyAxiom Functional() throws ParseException {
        ObjectProperty ar;
        jj_consume_token(29);
        jj_consume_token(22);
        ar = ObjectProperty();
        jj_consume_token(23);
        jj_consume_token(COMMAND_END);
        {
            if (true) return new FunctionalObjectPropertyAxiom(ar);
        }
        throw new Error("Missing return statement in function");
    }

    public final SymmetricObjectPropertyAxiom Symmetric() throws ParseException {
        ObjectProperty ar;
        jj_consume_token(30);
        jj_consume_token(22);
        ar = ObjectProperty();
        jj_consume_token(23);
        jj_consume_token(COMMAND_END);
        {
            if (true) return new SymmetricObjectPropertyAxiom(ar);
        }
        throw new Error("Missing return statement in function");
    }

    public final InverseObjectPropertyAxiom Inverse() throws ParseException {
        ObjectProperty ar1, ar2;
        jj_consume_token(31);
        jj_consume_token(22);
        ar1 = ObjectProperty();
        jj_consume_token(24);
        ar2 = ObjectProperty();
        jj_consume_token(23);
        jj_consume_token(COMMAND_END);
        {
            if (true) return new InverseObjectPropertyAxiom(ar1, ar2);
        }
        throw new Error("Missing return statement in function");
    }

    public final SubObjectPropertyAxiom Subrole() throws ParseException {
        ObjectProperty ar1, ar2;
        jj_consume_token(32);
        jj_consume_token(22);
        ar1 = ObjectProperty();
        jj_consume_token(24);
        ar2 = ObjectProperty();
        jj_consume_token(23);
        jj_consume_token(COMMAND_END);
        {
            if (true) return new SubObjectPropertyAxiom(ar1, ar2);
        }
        throw new Error("Missing return statement in function");
    }

    public final EquivalentClassesAxiom TBoxEquiv() throws ParseException {
        Description c1, c2;
        c1 = Concept();
        jj_consume_token(25);
        c2 = Concept();
        jj_consume_token(COMMAND_END);
        {
            if (true) return new EquivalentClassesAxiom(c1, c2);
        }
        throw new Error("Missing return statement in function");
    }

    public final SubClassAxiom TBoxSub() throws ParseException {
        Description c1, c2;
        c1 = Concept();
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case 26:
                jj_consume_token(26);
                break;
            case 27:
                jj_consume_token(27);
                break;
            case 33:
                jj_consume_token(33);
                break;
            default:
                jj_la1[3] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        c2 = Concept();
        jj_consume_token(COMMAND_END);
        {
            if (true) return new SubClassAxiom(c1, c2);
        }
        throw new Error("Missing return statement in function");
    }

    public final ObjectPropertyDomainAxiom ObjectPropertyDomainAxiom() throws ParseException {
        ObjectProperty op;
        Description domain;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case 34:
                jj_consume_token(34);
                break;
            case 35:
                jj_consume_token(35);
                break;
            case 36:
                jj_consume_token(36);
                break;
            default:
                jj_la1[4] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        jj_consume_token(22);
        op = ObjectProperty();
        jj_consume_token(23);
        jj_consume_token(25);
        domain = Concept();
        jj_consume_token(COMMAND_END);
        {
            if (true) return new ObjectPropertyDomainAxiom(op, domain);
        }
        throw new Error("Missing return statement in function");
    }

    public final DatatypePropertyDomainAxiom DatatypePropertyDomainAxiom() throws ParseException {
        DatatypeProperty op;
        Description domain;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case 37:
                jj_consume_token(37);
                break;
            case 38:
                jj_consume_token(38);
                break;
            default:
                jj_la1[5] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        jj_consume_token(22);
        op = DatatypeProperty();
        jj_consume_token(23);
        jj_consume_token(25);
        domain = Concept();
        jj_consume_token(COMMAND_END);
        {
            if (true) return new DatatypePropertyDomainAxiom(op, domain);
        }
        throw new Error("Missing return statement in function");
    }

    public final ObjectPropertyRangeAxiom ObjectPropertyRangeAxiom() throws ParseException {
        ObjectProperty op;
        Description range;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case 39:
                jj_consume_token(39);
                break;
            case 40:
                jj_consume_token(40);
                break;
            case 41:
                jj_consume_token(41);
                break;
            default:
                jj_la1[6] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        jj_consume_token(22);
        op = ObjectProperty();
        jj_consume_token(23);
        jj_consume_token(25);
        range = Concept();
        jj_consume_token(COMMAND_END);
        {
            if (true) return new ObjectPropertyRangeAxiom(op, range);
        }
        throw new Error("Missing return statement in function");
    }

    public final DatatypePropertyRangeAxiom DatatypePropertyRangeAxiom() throws ParseException {
        DatatypeProperty op;
        DataRange range;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case 42:
                jj_consume_token(42);
                break;
            case 43:
                jj_consume_token(43);
                break;
            default:
                jj_la1[7] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        jj_consume_token(22);
        op = DatatypeProperty();
        jj_consume_token(23);
        jj_consume_token(25);
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case 44:
                jj_consume_token(44);
                range = OWL2Datatype.DOUBLE.getDatatype();
                break;
            case 45:
                jj_consume_token(45);
                range = OWL2Datatype.BOOLEAN.getDatatype();
                break;
            case 46:
                jj_consume_token(46);
                range = OWL2Datatype.INT.getDatatype();
                break;
            default:
                jj_la1[8] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        jj_consume_token(COMMAND_END);
        {
            if (true) return new DatatypePropertyRangeAxiom(op, range);
        }
        throw new Error("Missing return statement in function");
    }

    public final Description Concept() throws ParseException {
        Description c, c1, c2;
        NamedClass ac;
        ObjectProperty ar;
        DatatypeProperty dp;
        ObjectProperty op;
        String s;
        Individual ind;
        int i;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case TOP:
                Top();
                {
                    if (true) return new Thing();
                }
                break;
            case BOTTOM:
                Bottom();
                {
                    if (true) return new Nothing();
                }
                break;
            default:
                jj_la1[9] = jj_gen;
                if (jj_2_5(2)) {
                    ac = AtomicConcept();
                    {
                        if (true) return ac;
                    }
                } else if (jj_2_6(2147483647)) {
                    jj_consume_token(22);
                    c1 = Concept();
                    And();
                    c2 = Concept();
                    jj_consume_token(23);
                    {
                        if (true) return new Intersection(c1, c2);
                    }
                } else if (jj_2_7(2147483647)) {
                    jj_consume_token(22);
                    c1 = Concept();
                    Or();
                    c2 = Concept();
                    jj_consume_token(23);
                    {
                        if (true) return new Union(c1, c2);
                    }
                } else {
                    switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                        case EXISTS:
                            Exists();
                            ar = ObjectProperty();
                            jj_consume_token(COMMAND_END);
                            c = Concept();
                            {
                                if (true) return new ObjectSomeRestriction(ar, c);
                            }
                            break;
                        case ALL:
                            All();
                            ar = ObjectProperty();
                            jj_consume_token(COMMAND_END);
                            c = Concept();
                            {
                                if (true) return new ObjectAllRestriction(ar, c);
                            }
                            break;
                        case NOT:
                            Not();
                            c = Concept();
                            {
                                if (true) return new Negation(c);
                            }
                            break;
                        case GE:
                            GE();
                            i = Integer();
                            ar = ObjectProperty();
                            jj_consume_token(COMMAND_END);
                            c = Concept();
                            {
                                if (true) return new ObjectMinCardinalityRestriction(i, ar, c);
                            }
                            break;
                        case LE:
                            LE();
                            i = Integer();
                            ar = ObjectProperty();
                            jj_consume_token(COMMAND_END);
                            c = Concept();
                            {
                                if (true) return new ObjectMaxCardinalityRestriction(i, ar, c);
                            }
                            break;
                        default:
                            jj_la1[10] = jj_gen;
                            if (jj_2_8(4)) {
                                jj_consume_token(22);
                                dp = DatatypeProperty();
                                jj_consume_token(47);
                                jj_consume_token(48);
                                jj_consume_token(23);
                                {
                                    if (true) return new BooleanValueRestriction(dp, true);
                                }
                            } else if (jj_2_9(4)) {
                                jj_consume_token(22);
                                dp = DatatypeProperty();
                                jj_consume_token(47);
                                jj_consume_token(49);
                                jj_consume_token(23);
                                {
                                    if (true) return new BooleanValueRestriction(dp, false);
                                }
                            } else if (jj_2_10(4)) {
                                jj_consume_token(22);
                                op = ObjectProperty();
                                jj_consume_token(50);
                                ind = Individual();
                                jj_consume_token(23);
                                {
                                    if (true) return new ObjectValueRestriction(op, ind);
                                }
                            } else {
                                switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                                    case 22:
                                        jj_consume_token(22);
                                        dp = DatatypeProperty();
                                        jj_consume_token(51);
                                        s = String();
                                        jj_consume_token(23);
                                        {
                                            if (true) return new StringValueRestriction(dp, s);
                                        }
                                        break;
                                    default:
                                        jj_la1[11] = jj_gen;
                                        jj_consume_token(-1);
                                        throw new ParseException();
                                }
                            }
                    }
                }
        }
        throw new Error("Missing return statement in function");
    }

    public final void Or() throws ParseException {
        jj_consume_token(OR);
    }

    public final void And() throws ParseException {
        jj_consume_token(AND);
    }

    public final void Top() throws ParseException {
        jj_consume_token(TOP);
    }

    public final void Bottom() throws ParseException {
        jj_consume_token(BOTTOM);
    }

    public final void Exists() throws ParseException {
        jj_consume_token(EXISTS);
    }

    public final void All() throws ParseException {
        jj_consume_token(ALL);
    }

    public final void Not() throws ParseException {
        jj_consume_token(NOT);
    }

    public final void GE() throws ParseException {
        jj_consume_token(GE);
    }

    public final void LE() throws ParseException {
        jj_consume_token(LE);
    }

    public final NamedClass AtomicConcept() throws ParseException {
        String name;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case ID:
                name = Id();
                break;
            case STRING:
                name = String();
                break;
            default:
                jj_la1[12] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if (true) return new NamedClass(getInternalURI(name));
        }
        throw new Error("Missing return statement in function");
    }

    public final DatatypeProperty DatatypeProperty() throws ParseException {
        String name;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case ID:
                name = Id();
                break;
            case STRING:
                name = String();
                break;
            default:
                jj_la1[13] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if (true) return new DatatypeProperty(getInternalURI(name));
        }
        throw new Error("Missing return statement in function");
    }

    public final ObjectProperty ObjectProperty() throws ParseException {
        String name;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case ID:
                name = Id();
                break;
            case STRING:
                name = String();
                break;
            default:
                jj_la1[14] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if (true) return new ObjectProperty(getInternalURI(name));
        }
        throw new Error("Missing return statement in function");
    }

    public final Individual Individual() throws ParseException {
        String name;
        switch((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case ID:
                name = Id();
                break;
            case STRING:
                name = String();
                break;
            default:
                jj_la1[15] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if (true) return new Individual(getInternalURI(name));
        }
        throw new Error("Missing return statement in function");
    }

    public final String Id() throws ParseException {
        Token t;
        t = jj_consume_token(ID);
        {
            if (true) return t.image;
        }
        throw new Error("Missing return statement in function");
    }

    public final double Double() throws ParseException {
        Token t;
        t = jj_consume_token(DOUBLE);
        {
            if (true) return new Double(t.image);
        }
        throw new Error("Missing return statement in function");
    }

    public final int Integer() throws ParseException {
        Token t;
        t = jj_consume_token(NUMBER);
        {
            if (true) return new Integer(t.image);
        }
        throw new Error("Missing return statement in function");
    }

    public final String String() throws ParseException {
        Token t;
        String s;
        t = jj_consume_token(STRING);
        s = t.image;
        s = s.substring(1, s.length() - 1);
        {
            if (true) return s;
        }
        throw new Error("Missing return statement in function");
    }

    private boolean jj_2_1(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_1();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(0, xla);
        }
    }

    private boolean jj_2_2(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_2();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(1, xla);
        }
    }

    private boolean jj_2_3(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_3();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(2, xla);
        }
    }

    private boolean jj_2_4(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_4();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(3, xla);
        }
    }

    private boolean jj_2_5(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_5();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(4, xla);
        }
    }

    private boolean jj_2_6(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_6();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(5, xla);
        }
    }

    private boolean jj_2_7(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_7();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(6, xla);
        }
    }

    private boolean jj_2_8(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_8();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(7, xla);
        }
    }

    private boolean jj_2_9(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_9();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(8, xla);
        }
    }

    private boolean jj_2_10(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_10();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(9, xla);
        }
    }

    private boolean jj_3_6() {
        if (jj_scan_token(22)) return true;
        if (jj_3R_2()) return true;
        if (jj_scan_token(14)) return true;
        return false;
    }

    private boolean jj_3R_12() {
        if (jj_scan_token(17)) return true;
        if (jj_3R_4()) return true;
        if (jj_scan_token(COMMAND_END)) return true;
        if (jj_3R_2()) return true;
        return false;
    }

    private boolean jj_3R_11() {
        if (jj_scan_token(16)) return true;
        if (jj_3R_4()) return true;
        if (jj_scan_token(COMMAND_END)) return true;
        if (jj_3R_2()) return true;
        return false;
    }

    private boolean jj_3R_10() {
        if (jj_scan_token(22)) return true;
        if (jj_3R_2()) return true;
        if (jj_scan_token(15)) return true;
        if (jj_3R_2()) return true;
        if (jj_scan_token(23)) return true;
        return false;
    }

    private boolean jj_3R_9() {
        if (jj_scan_token(22)) return true;
        if (jj_3R_2()) return true;
        if (jj_scan_token(14)) return true;
        if (jj_3R_2()) return true;
        if (jj_scan_token(23)) return true;
        return false;
    }

    private boolean jj_3_4() {
        if (jj_3R_2()) return true;
        Token xsp;
        xsp = jj_scanpos;
        if (jj_scan_token(26)) {
            jj_scanpos = xsp;
            if (jj_scan_token(27)) return true;
        }
        return false;
    }

    private boolean jj_3R_18() {
        if (jj_3R_26()) return true;
        return false;
    }

    private boolean jj_3_3() {
        if (jj_3R_2()) return true;
        if (jj_scan_token(25)) return true;
        return false;
    }

    private boolean jj_3_5() {
        if (jj_3R_5()) return true;
        return false;
    }

    private boolean jj_3R_8() {
        if (jj_scan_token(13)) return true;
        return false;
    }

    private boolean jj_3R_7() {
        if (jj_scan_token(12)) return true;
        return false;
    }

    private boolean jj_3R_2() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_7()) {
            jj_scanpos = xsp;
            if (jj_3R_8()) {
                jj_scanpos = xsp;
                if (jj_3_5()) {
                    jj_scanpos = xsp;
                    if (jj_3R_9()) {
                        jj_scanpos = xsp;
                        if (jj_3R_10()) {
                            jj_scanpos = xsp;
                            if (jj_3R_11()) {
                                jj_scanpos = xsp;
                                if (jj_3R_12()) {
                                    jj_scanpos = xsp;
                                    if (jj_3R_13()) {
                                        jj_scanpos = xsp;
                                        if (jj_3R_14()) {
                                            jj_scanpos = xsp;
                                            if (jj_3R_15()) {
                                                jj_scanpos = xsp;
                                                if (jj_3_8()) {
                                                    jj_scanpos = xsp;
                                                    if (jj_3_9()) {
                                                        jj_scanpos = xsp;
                                                        if (jj_3_10()) {
                                                            jj_scanpos = xsp;
                                                            if (jj_3R_16()) return true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean jj_3R_27() {
        if (jj_scan_token(ID)) return true;
        return false;
    }

    private boolean jj_3R_20() {
        if (jj_3R_26()) return true;
        return false;
    }

    private boolean jj_3R_17() {
        if (jj_3R_27()) return true;
        return false;
    }

    private boolean jj_3R_3() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_17()) {
            jj_scanpos = xsp;
            if (jj_3R_18()) return true;
        }
        return false;
    }

    private boolean jj_3R_24() {
        if (jj_3R_26()) return true;
        return false;
    }

    private boolean jj_3_2() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_scan_token(18)) jj_scanpos = xsp;
        if (jj_3R_4()) return true;
        if (jj_scan_token(22)) return true;
        if (jj_3R_3()) return true;
        if (jj_scan_token(24)) return true;
        return false;
    }

    private boolean jj_3R_19() {
        if (jj_3R_27()) return true;
        return false;
    }

    private boolean jj_3R_4() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_19()) {
            jj_scanpos = xsp;
            if (jj_3R_20()) return true;
        }
        return false;
    }

    private boolean jj_3_1() {
        if (jj_3R_2()) return true;
        if (jj_scan_token(22)) return true;
        if (jj_3R_3()) return true;
        if (jj_scan_token(23)) return true;
        if (jj_scan_token(COMMAND_END)) return true;
        return false;
    }

    private boolean jj_3R_22() {
        if (jj_3R_26()) return true;
        return false;
    }

    private boolean jj_3R_23() {
        if (jj_3R_27()) return true;
        return false;
    }

    private boolean jj_3R_6() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_23()) {
            jj_scanpos = xsp;
            if (jj_3R_24()) return true;
        }
        return false;
    }

    private boolean jj_3R_21() {
        if (jj_3R_27()) return true;
        return false;
    }

    private boolean jj_3R_5() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_21()) {
            jj_scanpos = xsp;
            if (jj_3R_22()) return true;
        }
        return false;
    }

    private boolean jj_3R_26() {
        if (jj_scan_token(STRING)) return true;
        return false;
    }

    private boolean jj_3R_16() {
        if (jj_scan_token(22)) return true;
        if (jj_3R_6()) return true;
        if (jj_scan_token(51)) return true;
        if (jj_3R_26()) return true;
        if (jj_scan_token(23)) return true;
        return false;
    }

    private boolean jj_3_10() {
        if (jj_scan_token(22)) return true;
        if (jj_3R_4()) return true;
        if (jj_scan_token(50)) return true;
        if (jj_3R_3()) return true;
        if (jj_scan_token(23)) return true;
        return false;
    }

    private boolean jj_3_9() {
        if (jj_scan_token(22)) return true;
        if (jj_3R_6()) return true;
        if (jj_scan_token(47)) return true;
        if (jj_scan_token(49)) return true;
        if (jj_scan_token(23)) return true;
        return false;
    }

    private boolean jj_3_8() {
        if (jj_scan_token(22)) return true;
        if (jj_3R_6()) return true;
        if (jj_scan_token(47)) return true;
        if (jj_scan_token(48)) return true;
        if (jj_scan_token(23)) return true;
        return false;
    }

    private boolean jj_3R_15() {
        if (jj_scan_token(20)) return true;
        if (jj_3R_25()) return true;
        if (jj_3R_4()) return true;
        if (jj_scan_token(COMMAND_END)) return true;
        if (jj_3R_2()) return true;
        return false;
    }

    private boolean jj_3R_14() {
        if (jj_scan_token(19)) return true;
        if (jj_3R_25()) return true;
        if (jj_3R_4()) return true;
        if (jj_scan_token(COMMAND_END)) return true;
        if (jj_3R_2()) return true;
        return false;
    }

    private boolean jj_3R_25() {
        if (jj_scan_token(NUMBER)) return true;
        return false;
    }

    private boolean jj_3_7() {
        if (jj_scan_token(22)) return true;
        if (jj_3R_2()) return true;
        if (jj_scan_token(15)) return true;
        return false;
    }

    private boolean jj_3R_13() {
        if (jj_scan_token(18)) return true;
        if (jj_3R_2()) return true;
        return false;
    }

    /** Generated Token Manager. */
    public KBParserTokenManager token_source;

    SimpleCharStream jj_input_stream;

    /** Current token. */
    public Token token;

    /** Next token. */
    public Token jj_nt;

    private int jj_ntk;

    private Token jj_scanpos, jj_lastpos;

    private int jj_la;

    private int jj_gen;

    private final int[] jj_la1 = new int[16];

    private static int[] jj_la1_0;

    private static int[] jj_la1_1;

    static {
        jj_la1_init_0();
        jj_la1_init_1();
    }

    private static void jj_la1_init_0() {
        jj_la1_0 = new int[] { 0xf07f3200, 0xf0000000, 0x40000, 0xc000000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x3000, 0x1f0000, 0x400000, 0x200200, 0x200200, 0x200200, 0x200200 };
    }

    private static void jj_la1_init_1() {
        jj_la1_1 = new int[] { 0xffd, 0xffd, 0x0, 0x2, 0x1c, 0x60, 0x380, 0xc00, 0x7000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 };
    }

    private final JJCalls[] jj_2_rtns = new JJCalls[10];

    private boolean jj_rescan = false;

    private int jj_gc = 0;

    /** Constructor with InputStream. */
    public KBParser(java.io.InputStream stream) {
        this(stream, null);
    }

    /** Constructor with InputStream and supplied encoding */
    public KBParser(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source = new KBParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 16; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Reinitialise. */
    public void ReInit(java.io.InputStream stream) {
        ReInit(stream, null);
    }

    /** Reinitialise. */
    public void ReInit(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream.ReInit(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 16; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Constructor. */
    public KBParser(java.io.Reader stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new KBParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 16; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Reinitialise. */
    public void ReInit(java.io.Reader stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 16; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Constructor with generated Token Manager. */
    public KBParser(KBParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 16; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /** Reinitialise. */
    public void ReInit(KBParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 16; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null) token = token.next; else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            if (++jj_gc > 100) {
                jj_gc = 0;
                for (int i = 0; i < jj_2_rtns.length; i++) {
                    JJCalls c = jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < jj_gen) c.first = null;
                        c = c.next;
                    }
                }
            }
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    private static final class LookaheadSuccess extends java.lang.Error {
    }

    private final LookaheadSuccess jj_ls = new LookaheadSuccess();

    private boolean jj_scan_token(int kind) {
        if (jj_scanpos == jj_lastpos) {
            jj_la--;
            if (jj_scanpos.next == null) {
                jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
            } else {
                jj_lastpos = jj_scanpos = jj_scanpos.next;
            }
        } else {
            jj_scanpos = jj_scanpos.next;
        }
        if (jj_rescan) {
            int i = 0;
            Token tok = token;
            while (tok != null && tok != jj_scanpos) {
                i++;
                tok = tok.next;
            }
            if (tok != null) jj_add_error_token(kind, i);
        }
        if (jj_scanpos.kind != kind) return true;
        if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
        return false;
    }

    /** Get the next Token. */
    public final Token getNextToken() {
        if (token.next != null) token = token.next; else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    /** Get the specific Token. */
    public final Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) t = t.next; else t = t.next = token_source.getNextToken();
        }
        return t;
    }

    private int jj_ntk() {
        if ((jj_nt = token.next) == null) return (jj_ntk = (token.next = token_source.getNextToken()).kind); else return (jj_ntk = jj_nt.kind);
    }

    private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();

    private int[] jj_expentry;

    private int jj_kind = -1;

    private int[] jj_lasttokens = new int[100];

    private int jj_endpos;

    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100) return;
        if (pos == jj_endpos + 1) {
            jj_lasttokens[jj_endpos++] = kind;
        } else if (jj_endpos != 0) {
            jj_expentry = new int[jj_endpos];
            for (int i = 0; i < jj_endpos; i++) {
                jj_expentry[i] = jj_lasttokens[i];
            }
            jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext(); ) {
                int[] oldentry = (int[]) (it.next());
                if (oldentry.length == jj_expentry.length) {
                    for (int i = 0; i < jj_expentry.length; i++) {
                        if (oldentry[i] != jj_expentry[i]) {
                            continue jj_entries_loop;
                        }
                    }
                    jj_expentries.add(jj_expentry);
                    break jj_entries_loop;
                }
            }
            if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
        }
    }

    /** Generate ParseException. */
    public ParseException generateParseException() {
        jj_expentries.clear();
        boolean[] la1tokens = new boolean[52];
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 16; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((jj_la1_1[i] & (1 << j)) != 0) {
                        la1tokens[32 + j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 52; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.add(jj_expentry);
            }
        }
        jj_endpos = 0;
        jj_rescan_token();
        jj_add_error_token(0, 0);
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = jj_expentries.get(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    /** Enable tracing. */
    public final void enable_tracing() {
    }

    /** Disable tracing. */
    public final void disable_tracing() {
    }

    private void jj_rescan_token() {
        jj_rescan = true;
        for (int i = 0; i < 10; i++) {
            try {
                JJCalls p = jj_2_rtns[i];
                do {
                    if (p.gen > jj_gen) {
                        jj_la = p.arg;
                        jj_lastpos = jj_scanpos = p.first;
                        switch(i) {
                            case 0:
                                jj_3_1();
                                break;
                            case 1:
                                jj_3_2();
                                break;
                            case 2:
                                jj_3_3();
                                break;
                            case 3:
                                jj_3_4();
                                break;
                            case 4:
                                jj_3_5();
                                break;
                            case 5:
                                jj_3_6();
                                break;
                            case 6:
                                jj_3_7();
                                break;
                            case 7:
                                jj_3_8();
                                break;
                            case 8:
                                jj_3_9();
                                break;
                            case 9:
                                jj_3_10();
                                break;
                        }
                    }
                    p = p.next;
                } while (p != null);
            } catch (LookaheadSuccess ls) {
            }
        }
        jj_rescan = false;
    }

    private void jj_save(int index, int xla) {
        JJCalls p = jj_2_rtns[index];
        while (p.gen > jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }
        p.gen = jj_gen + xla - jj_la;
        p.first = token;
        p.arg = xla;
    }

    static final class JJCalls {

        int gen;

        Token first;

        int arg;

        JJCalls next;
    }
}

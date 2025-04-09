import java.math.BigInteger;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Evaluator implements Kitsch.yyTree.Visitor {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Evaluator infile");
            System.exit(1);
        }
        try {
            java.io.InputStream is = new java.io.FileInputStream(args[0]);
            Kitsch.yyInput scanner = new Kitsch.yyLex(is);
            java.lang.Object parser = scanner.getClass().getEnclosingClass().newInstance();
            java.lang.Object tree = parser.getClass().getMethod("yyparse", new java.lang.Class[] { Kitsch.yyInput.class, java.lang.Object[].class }).invoke(parser, scanner, new java.lang.Object[] { new Kitsch.yyTree() });
            FuncVisitor fv = new FuncVisitor();
            Map<FuncVisitor.Function, Kitsch.yyTree.FuncDecl> funcsToNodes = (Map) fv.visit((Kitsch.yyTree.Prgm) tree);
            TypeChecker checker = new TypeChecker(funcsToNodes);
            checker.visit((Kitsch.yyTree.Prgm) tree);
            Evaluator eval = new Evaluator(funcsToNodes);
            eval.visit((Kitsch.yyTree.Prgm) tree);
        } catch (java.io.FileNotFoundException ex) {
            System.err.println(args[0] + ": File does not exist.");
            System.exit(1);
        } catch (java.lang.InstantiationException e) {
            System.err.println("cannot create parser [" + e + "]");
            System.exit(1);
        } catch (java.lang.IllegalAccessException e) {
            System.err.println("cannot create parser [" + e + "]");
            System.exit(1);
        } catch (java.lang.NoSuchMethodException e) {
            System.err.println("cannot create parser [" + e + "]");
            System.exit(1);
        } catch (java.lang.reflect.InvocationTargetException e) {
            System.err.println("parse error [" + e + "]");
            System.exit(1);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    private Scope globalScope;

    private Scope currentScope;

    private Scanner input;

    private Map<FuncVisitor.Function, Kitsch.yyTree.FuncDecl> funcsToNodes;

    private OperatorRegistry registry;

    public class Data {

        public int type;

        public Object value;

        public Data(int type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    public class Scope {

        private Scope outerscope;

        public Map<String, Data> state;

        public Scope(Scope outerscope) {
            this.outerscope = outerscope;
            state = new HashMap<String, Data>();
        }

        public void addToScope(String ident, int type, Object value) {
            if (getValue(ident) != null) {
                System.err.println("Fatal: ident " + ident + " is already in scope.");
                System.exit(1);
            }
            state.put(ident, new Data(type, value));
        }

        public void changeValue(String ident, Object value) {
            if (getValue(ident) == null) {
                System.err.println("Fatal: ident " + ident + " does not exist.");
                System.exit(1);
            }
            if (state.get(ident) == null) {
                outerscope.changeValue(ident, value);
            } else {
                state.put(ident, new Data(state.get(ident).type, value));
            }
        }

        public Object getValue(String ident) {
            Data d = state.get(ident);
            if (d != null) {
                return d.value;
            }
            Object data;
            if (d == null && outerscope != null) {
                data = outerscope.getValue(ident);
            } else {
                data = null;
            }
            if (data == null) {
                return null;
            } else {
                return data;
            }
        }

        public Data getData(String ident) {
            Data data = state.get(ident);
            if (data == null && outerscope != null) {
                data = outerscope.getData(ident);
            }
            return data;
        }

        public int dumpMem() {
            int indent;
            if (outerscope != null) {
                indent = outerscope.dumpMem();
            } else {
                indent = 0;
            }
            String indentString = "";
            for (int x = 0; x < indent; x++) {
                indentString += "\t";
            }
            System.out.println(indentString + "Start scope: ");
            for (String ident : state.keySet()) {
                System.out.println(indentString + ident + " = " + state.get(ident).value);
            }
            return indent + 1;
        }
    }

    public Evaluator(Map<FuncVisitor.Function, Kitsch.yyTree.FuncDecl> funcsToNodes) {
        globalScope = new Scope(null);
        currentScope = globalScope;
        input = new Scanner(System.in);
        input.useDelimiter(System.getProperty("line.separator"));
        this.funcsToNodes = funcsToNodes;
        registry = new OperatorRegistry();
    }

    private FuncVisitor.Function lookupFunc(String fname) {
        for (FuncVisitor.Function f : funcsToNodes.keySet()) {
            if (f.name.equals(fname)) {
                return f;
            }
        }
        System.err.println("Error: Function " + fname + " does not exist.");
        System.exit(-1);
        return null;
    }

    /**
     * To evaluate a program node, we evaluate each subnode in the program.
     */
    public java.lang.Object visit(Kitsch.yyTree.Prgm node) {
        Kitsch.yyTree.Visit elem;
        currentScope = new Scope(globalScope);
        for (int x = 0; x < node.size(); x++) {
            elem = (Kitsch.yyTree.Visit) node.get(x);
            if (elem != null) {
                elem.visit(this);
            }
        }
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.top_stmt node) {
        Kitsch.yyTree.Visit elem;
        for (int x = 0; x < node.size(); x++) {
            elem = (Kitsch.yyTree.Visit) node.get(x);
            if (elem != null && !(elem instanceof Kitsch.yyTree.FuncDecl)) {
                elem.visit(this);
            }
        }
        return null;
    }

    /**
     * To evaluate a statement node, we evaluate each subnode.
     * There should really only be one, but pj2 wants to put it in a list anyway.
     */
    public java.lang.Object visit(Kitsch.yyTree.Stmt node) {
        Kitsch.yyTree.Visit elem;
        for (int x = 0; x < node.size(); x++) {
            elem = (Kitsch.yyTree.Visit) node.get(x);
            elem.visit(this);
        }
        return null;
    }

    /**
     * An if statement has a single if block, an arbitrary number of elif blocks,
     * and possibly an else block.
     */
    public java.lang.Object visit(Kitsch.yyTree.IfStmt node) {
        int pos = 0;
        Boolean eval = (Boolean) ((Kitsch.yyTree.Visit) node.get(pos++)).visit(this);
        while (!eval && pos < node.size()) {
            if (node.get(pos) instanceof Kitsch.yyTree.ElifBlock) {
                eval = (Boolean) ((Kitsch.yyTree.Visit) node.get(pos++)).visit(this);
            } else if (node.get(pos) != null) {
                ((Kitsch.yyTree.Visit) node.get(pos++)).visit(this);
            } else {
                pos++;
            }
        }
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.IfBlock node) {
        Boolean eval = (Boolean) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        if (eval) {
            for (int x = 1; x < node.size(); x++) {
                ((Kitsch.yyTree.Visit) node.get(x)).visit(this);
            }
        }
        return eval;
    }

    public java.lang.Object visit(Kitsch.yyTree.ElifBlock node) {
        Boolean eval = (Boolean) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        if (eval) {
            for (int x = 1; x < node.size(); x++) {
                ((Kitsch.yyTree.Visit) node.get(x)).visit(this);
            }
        }
        return eval;
    }

    public java.lang.Object visit(Kitsch.yyTree.ElseBlock node) {
        for (int x = 0; x < node.size(); x++) {
            ((Kitsch.yyTree.Visit) node.get(x)).visit(this);
        }
        return null;
    }

    /**
     * A while is made up of two elements:
     * 0 - the comparison
     * 1 - the body of the while
     */
    public java.lang.Object visit(Kitsch.yyTree.WhileStmt node) {
        Kitsch.yyTree.Expr expr = (Kitsch.yyTree.Expr) node.get(0);
        while ((Boolean) expr.visit(this) == true) {
            Kitsch.yyTree.Visit elem;
            for (int x = 1; x < node.size(); x++) {
                elem = (Kitsch.yyTree.Visit) node.get(x);
                if (elem != null) {
                    elem.visit(this);
                }
            }
        }
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.GlbDecl node) {
        globalScope.addToScope((String) node.get(1), 0, 0);
        return null;
    }

    /**
     * Assignment has two elements, the identifier and the value.
     */
    public java.lang.Object visit(Kitsch.yyTree.Assign node) {
        int type = node.getType();
        String ident = (String) ((Kitsch.yyTree.IdCls) node.get(0)).get(0);
        Data data = currentScope.getData(ident);
        if (data == null) {
            currentScope.addToScope(ident, node.getType(), new ArrayList());
            data = currentScope.getData(ident);
        }
        if (node.get(1) != null) {
            BigInteger index = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
            Object value = ((Kitsch.yyTree.Visit) node.get(2)).visit(this);
            List list = (List) data.value;
            while (list.size() <= index.intValue()) {
                switch(type) {
                    case OperatorRegistry.INT:
                        list.add(0);
                        break;
                    case OperatorRegistry.BOOL:
                        list.add(false);
                        break;
                    case OperatorRegistry.STRING:
                        list.add("");
                        break;
                    default:
                        list.add(new ArrayList());
                        break;
                }
            }
            list.set(index.intValue(), value);
        } else {
            if (type == OperatorRegistry.INT) {
                BigInteger value = (BigInteger) ((Kitsch.yyTree.Visit) node.get(2)).visit(this);
                currentScope.changeValue(ident, value);
            } else if (type == OperatorRegistry.BOOL) {
                Boolean value = (Boolean) ((Kitsch.yyTree.Visit) node.get(2)).visit(this);
                currentScope.changeValue(ident, value);
            } else if (type == OperatorRegistry.STRING) {
                String value = (String) ((Kitsch.yyTree.Visit) node.get(2)).visit(this);
                currentScope.changeValue(ident, value);
            } else if (OperatorRegistry.isArrayType(type)) {
                List value = (List) ((Kitsch.yyTree.Visit) node.get(2)).visit(this);
                currentScope.changeValue(ident, value);
            } else {
                System.out.println("FATAL: Encountered unknown object type in assignment of '" + ident + "'");
                System.exit(1);
            }
            data = currentScope.getData(ident);
            if (data.type != node.getType()) {
                data.type = node.getType();
            }
        }
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.FuncDecl node) {
        Kitsch.yyTree.Visit elem;
        for (int x = 2; x < node.size() - 1; x++) {
            elem = (Kitsch.yyTree.Visit) node.get(x);
            if (elem != null) {
                elem.visit(this);
            }
        }
        elem = (Kitsch.yyTree.Visit) node.get(node.size() - 1);
        if (elem == null) {
            return 0;
        } else {
            return elem.visit(this);
        }
    }

    public java.lang.Object visit(Kitsch.yyTree.Params node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.DefPar node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.RetCls node) {
        return ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
    }

    public java.lang.Object visit(Kitsch.yyTree.Print node) {
        int type = node.getType();
        java.lang.Object obj = ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        if (type == OperatorRegistry.BOOL) {
            System.out.println((Boolean) obj ? "True" : "False");
        } else if (type == OperatorRegistry.BOOL_ARRAY) {
            String s = "[";
            for (Boolean b : (List<Boolean>) obj) {
                s += (b ? "True" : "False") + ", ";
            }
            s = s.substring(0, s.length() - 2) + "]";
            System.out.println(s);
        } else if (type == OperatorRegistry.STR_ARRAY) {
            String s = "[";
            for (String str : (List<String>) obj) {
                s += "'" + str + "'" + ", ";
            }
            s = s.substring(0, s.length() - 2) + "]";
            System.out.println(s);
        } else {
            System.out.println(obj);
        }
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Read node) {
        int type = node.getType();
        String ident = (String) ((Kitsch.yyTree.IdCls) node.get(0)).get(0);
        Data data = currentScope.getData(ident);
        if (data == null) {
            currentScope.addToScope(ident, OperatorRegistry.STRING, "");
        }
        if (type == OperatorRegistry.INT && input.hasNextBigInteger()) {
            currentScope.changeValue(ident, input.nextBigInteger());
        } else if (type == OperatorRegistry.BOOL && input.hasNextBoolean()) {
            currentScope.changeValue(ident, input.nextBoolean());
        } else if (type == OperatorRegistry.STRING && input.hasNext()) {
            currentScope.changeValue(ident, input.next());
        } else {
            System.err.println("Fatal: Input is not an integer.");
        }
        return null;
    }

    /**
     * The next set of nodes all do the same thing - eval the LHS and RHS,
     * then use the given operator to compare them.
     * There are much more elegant ways to do this, but eh.
     */
    public java.lang.Object visit(Kitsch.yyTree.Lt node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.compareTo(rhs) < 0;
    }

    public java.lang.Object visit(Kitsch.yyTree.Lte node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.compareTo(rhs) <= 0;
    }

    public java.lang.Object visit(Kitsch.yyTree.Gt node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.compareTo(rhs) > 0;
    }

    public java.lang.Object visit(Kitsch.yyTree.Gte node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.compareTo(rhs) >= 0;
    }

    public java.lang.Object visit(Kitsch.yyTree.Eq node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.compareTo(rhs) == 0;
    }

    public java.lang.Object visit(Kitsch.yyTree.Neq node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.compareTo(rhs) != 0;
    }

    public java.lang.Object visit(Kitsch.yyTree.LgAnd node) {
        Boolean lhs = (Boolean) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        Boolean rhs = (Boolean) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs && rhs;
    }

    public java.lang.Object visit(Kitsch.yyTree.LgOr node) {
        Boolean lhs = (Boolean) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        Boolean rhs = (Boolean) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs || rhs;
    }

    public java.lang.Object visit(Kitsch.yyTree.LgNot node) {
        Boolean lhs = (Boolean) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        return !lhs;
    }

    /**
     * Expr is just a container, pass the visit call onwards.
     */
    public java.lang.Object visit(Kitsch.yyTree.Expr node) {
        Kitsch.yyTree.Visit elem = (Kitsch.yyTree.Visit) node.get(0);
        return elem.visit(this);
    }

    public java.lang.Object visit(Kitsch.yyTree.Add node) {
        if (node.getType() == OperatorRegistry.INT) {
            BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
            BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
            return lhs.add(rhs);
        } else if (node.getType() == OperatorRegistry.STRING) {
            return (String) ((Kitsch.yyTree.Visit) node.get(0)).visit(this) + (String) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        } else if (node.getType() == OperatorRegistry.INT_ARRAY) {
            List lhs = (List) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
            List rhs = (List) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
            List combined = new ArrayList<BigInteger>(lhs);
            combined.addAll(rhs);
            return combined;
        } else if (node.getType() == OperatorRegistry.STR_ARRAY) {
            List lhs = (List) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
            List rhs = (List) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
            List combined = new ArrayList<String>(lhs);
            combined.addAll(rhs);
            return combined;
        } else if (node.getType() == OperatorRegistry.BOOL_ARRAY) {
            List lhs = (List) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
            List rhs = (List) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
            List combined = new ArrayList<Boolean>(lhs);
            combined.addAll(rhs);
            return combined;
        } else {
            System.err.println("Unsupported type for '+' operation.");
            System.exit(1);
            return 1;
        }
    }

    public java.lang.Object visit(Kitsch.yyTree.Sub node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.subtract(rhs);
    }

    public java.lang.Object visit(Kitsch.yyTree.Mul node) {
        int left_type = ((Kitsch.yyTree.Visit) node.get(0)).getType();
        int right_type = ((Kitsch.yyTree.Visit) node.get(1)).getType();
        if (left_type == OperatorRegistry.STRING) {
            String lhs = (String) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
            BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
            String newstr = "";
            for (int i = rhs.intValue(); i > 0; i--) {
                newstr += lhs;
            }
            return newstr;
        } else if (right_type == OperatorRegistry.STRING) {
            String rhs = (String) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
            BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
            String newstr = "";
            for (int i = lhs.intValue(); i > 0; i--) {
                newstr += rhs;
            }
            return newstr;
        } else {
            BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
            BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
            return lhs.multiply(rhs);
        }
    }

    public java.lang.Object visit(Kitsch.yyTree.Div node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        if (rhs.compareTo(new BigInteger("0")) == 0) {
            System.err.println("Fatal: divide by 0.");
            System.exit(1);
        }
        return lhs.divide(rhs);
    }

    public java.lang.Object visit(Kitsch.yyTree.Mod node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.mod(rhs);
    }

    public java.lang.Object visit(Kitsch.yyTree.Exp node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.pow(rhs.intValue());
    }

    public java.lang.Object visit(Kitsch.yyTree.BwAnd node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.and(rhs);
    }

    public java.lang.Object visit(Kitsch.yyTree.BwOr node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.or(rhs);
    }

    public java.lang.Object visit(Kitsch.yyTree.BwXor node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        BigInteger rhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return lhs.xor(rhs);
    }

    public java.lang.Object visit(Kitsch.yyTree.BwNot node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        return lhs.not();
    }

    public java.lang.Object visit(Kitsch.yyTree.Call node) {
        FuncVisitor.Function f = lookupFunc((String) node.get(0));
        if (f.name.equals("len")) {
            List value = (List) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
            return new BigInteger("" + value.size());
        }
        Scope oldScope = currentScope;
        Scope tempScope;
        currentScope = new Scope(globalScope);
        int paramIndex = 0;
        for (int x = 1; x < node.size(); x++) {
            if (node.get(x) != null) {
                FuncVisitor.Parameter p = f.params.get(paramIndex);
                tempScope = currentScope;
                currentScope = oldScope;
                Object value = ((Kitsch.yyTree.Visit) node.get(x)).visit(this);
                currentScope = tempScope;
                currentScope.addToScope(p.name, p.type, value);
                paramIndex++;
            }
        }
        for (int x = paramIndex; x < f.params.size(); x++) {
            FuncVisitor.Parameter p = f.params.get(x);
            Object defaultValue = p.defaultValue;
            switch(p.type) {
                case OperatorRegistry.INT:
                    defaultValue = new BigInteger((String) defaultValue);
                    break;
                case OperatorRegistry.BOOL:
                    defaultValue = new Boolean((String) defaultValue);
                    break;
                case OperatorRegistry.STRING:
                    defaultValue = (String) defaultValue;
                    defaultValue = ((String) defaultValue).substring(1);
                    defaultValue = ((String) defaultValue).substring(0, ((String) defaultValue).length() - 1);
                    break;
                default:
                    System.err.println("Error: Unknown type in parameter " + p.name);
                    System.exit(-1);
                    break;
            }
            currentScope.addToScope(p.name, p.type, defaultValue);
        }
        Object returnVal = funcsToNodes.get(f).visit(this);
        currentScope = oldScope;
        return returnVal;
    }

    public java.lang.Object visit(Kitsch.yyTree.ArrayLit node) {
        Kitsch.yyTree.Visit elem;
        List list = new ArrayList();
        for (int x = 0; x < node.size(); x++) {
            elem = (Kitsch.yyTree.Visit) node.get(x);
            if (elem != null) {
                list.add(elem.visit(this));
            }
        }
        return list;
    }

    public java.lang.Object visit(Kitsch.yyTree.Parens node) {
        return ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
    }

    public java.lang.Object visit(Kitsch.yyTree.Cast node) {
        Object rhs = (Object) ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        int rhs_type = ((Kitsch.yyTree.Visit) node.get(1)).getType();
        Object value = null;
        try {
            switch(node.getType()) {
                case OperatorRegistry.INT:
                    if (rhs_type == OperatorRegistry.STRING) {
                        value = new BigInteger(rhs.toString());
                    } else if (rhs_type == OperatorRegistry.BOOL) {
                        value = (Boolean) rhs ? new BigInteger("1") : new BigInteger("0");
                    } else {
                        value = (BigInteger) rhs;
                    }
                    break;
                case OperatorRegistry.STRING:
                    value = rhs.toString();
                    break;
                case OperatorRegistry.BOOL:
                    if (rhs_type == OperatorRegistry.STRING) {
                        if (((String) rhs).length() > 0) {
                            value = new Boolean(true);
                        } else {
                            value = new Boolean(false);
                        }
                    } else if (rhs_type == OperatorRegistry.INT) {
                        value = rhs.toString() == "0" ? false : true;
                    } else {
                        value = (Boolean) rhs;
                    }
                    break;
                default:
                    System.err.println("Error: Unknown type in cast.");
                    System.exit(1);
            }
        } catch (ClassCastException e) {
            System.err.println("Error: Invalid cast from " + registry.typeToString(((Kitsch.yyTree.Visit) node.get(1)).getType()) + " to " + registry.typeToString(node.getType()));
            System.exit(1);
        }
        return value;
    }

    public java.lang.Object visit(Kitsch.yyTree.UNeg node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        return lhs.negate();
    }

    public java.lang.Object visit(Kitsch.yyTree.UPos node) {
        BigInteger lhs = (BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        return lhs;
    }

    public java.lang.Object visit(Kitsch.yyTree.NumCls node) {
        return new BigInteger((String) node.get(0));
    }

    public java.lang.Object visit(Kitsch.yyTree.IdCls node) {
        String ident = (String) node.get(0);
        int type = node.getType();
        if (type == OperatorRegistry.INT) {
            BigInteger value = (BigInteger) currentScope.getValue(ident);
            if (value == null) {
                System.err.println("Fatal: " + ident + " is not in scope.");
                System.exit(1);
            }
            return value;
        } else if (type == OperatorRegistry.BOOL) {
            Boolean value = (Boolean) currentScope.getValue(ident);
            if (value == null) {
                System.err.println("Fatal: " + ident + " is not in scope.");
                System.exit(1);
            }
            return value;
        } else if (type == OperatorRegistry.STRING) {
            String value = (String) currentScope.getValue(ident);
            if (value == null) {
                System.err.println("Fatal: " + ident + " is not in scope.");
                System.exit(1);
            }
            return value;
        } else if (OperatorRegistry.isArrayType(type)) {
            List value = (List) currentScope.getValue(ident);
            if (value == null) {
                System.err.println("Fatal: " + ident + " is not in scope.");
                System.exit(1);
            }
            return value;
        } else {
            System.err.println("FATAL: Encountered unknown object type");
            System.exit(1);
        }
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.TypeCls node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.StrCls node) {
        String s = (String) node.get(0);
        s = s.substring(1);
        s = s.substring(0, s.length() - 1);
        return s;
    }

    public java.lang.Object visit(Kitsch.yyTree.BoolCls node) {
        return new Boolean((String) node.get(0));
    }

    public java.lang.Object visit(Kitsch.yyTree.EolCls node) {
        return null;
    }

    public class Range {

        public int start_index;

        public int end_index;

        public Range(int start, int end) {
            this.start_index = start;
            this.end_index = end;
        }

        public Range(java.math.BigInteger num) {
            this.start_index = num.intValue();
            this.end_index = num.intValue() + 1;
        }

        public Range() {
            this.start_index = 0;
            this.end_index = 0;
        }
    }

    public java.lang.Object visit(Kitsch.yyTree.Range node) {
        Range range = new Range();
        if (node.get(0) == null && node.get(1) == null) {
            range.start_index = 0;
            range.end_index = 0;
        } else if (node.get(0) == null) {
            range.start_index = 0;
            range.end_index = ((BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this)).intValue();
        } else if (node.get(1) == null) {
            range.start_index = ((BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this)).intValue();
            range.end_index = 0;
        } else {
            range.start_index = ((BigInteger) ((Kitsch.yyTree.Visit) node.get(0)).visit(this)).intValue();
            range.end_index = ((BigInteger) ((Kitsch.yyTree.Visit) node.get(1)).visit(this)).intValue();
        }
        return range;
    }

    public java.lang.Object visit(Kitsch.yyTree.SubStr node) {
        Object obj = ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        Range range;
        Object rhs = ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        if (rhs instanceof Range) {
            range = (Range) rhs;
        } else {
            range = new Range((BigInteger) rhs);
        }
        int len = 0;
        if (obj instanceof String) {
            len = ((String) obj).length();
        } else {
            len = ((List) obj).size();
        }
        while (range.start_index < 0) {
            range.start_index += len;
        }
        while (range.end_index < 0) {
            range.end_index += len;
        }
        if (obj instanceof String) {
            String s = (String) obj;
            if (range.start_index == 0 && range.end_index == 0) {
                return s;
            } else if (range.end_index == 0) {
                return s.substring(range.start_index);
            } else {
                return s.substring(range.start_index, range.end_index);
            }
        } else {
            List l = (List) obj;
            if (range.start_index == 0 && range.end_index == 0) {
            } else if (range.end_index == 0) {
                l = l.subList(range.start_index, l.size());
            } else {
                l = l.subList(range.start_index, range.end_index);
            }
            return (l.size() == 1 ? l.get(0) : l);
        }
    }

    /** hook for unknown classes. */
    public java.lang.Object visit(Kitsch.yyTree.Visit node) {
        System.out.println("Unrecognized node:" + node);
        System.out.println("Node class: " + node.getClass());
        return null;
    }
}

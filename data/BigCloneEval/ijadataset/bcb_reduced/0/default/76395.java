import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class FuncVisitor implements Kitsch.yyTree.Visitor {

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
            List<Function> funcs = (List<Function>) fv.visit((Kitsch.yyTree.Prgm) tree);
            System.out.println(funcs);
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
        }
    }

    public class Function {

        public String name;

        public int returnType;

        public int startLoc;

        public List<Parameter> params;

        public Function(String name) {
            this.name = name;
            returnType = 0;
            params = new ArrayList<Parameter>();
            startLoc = -1;
        }

        public Parameter lookupParameter(String pname) {
            for (Parameter p : params) {
                if (p.name.equals(pname)) {
                    return p;
                }
            }
            return null;
        }

        public String toString() {
            return name + " returns " + returnType + " with params:\n" + params + "\n";
        }
    }

    public class Parameter {

        public String name;

        public int type;

        public Object defaultValue;

        public Parameter(String name) {
            this.name = name;
            type = 0;
            defaultValue = null;
        }

        public String toString() {
            return name + " has type " + type + " and defaults to " + defaultValue;
        }
    }

    private Map<Function, Kitsch.yyTree.FuncDecl> funcsToNodes;

    private Function currentFunc;

    private boolean error;

    public FuncVisitor() {
        funcsToNodes = new HashMap<Function, Kitsch.yyTree.FuncDecl>();
    }

    public java.lang.Object visit(Kitsch.yyTree.Prgm node) {
        Kitsch.yyTree.Visit elem;
        for (int x = 0; x < node.size(); x++) {
            elem = (Kitsch.yyTree.Visit) node.get(x);
            if (elem != null) {
                elem.visit(this);
            }
        }
        if (error) {
            System.exit(1);
        }
        Function len = new Function("len");
        len.returnType = OperatorRegistry.INT;
        Parameter array = new Parameter("array");
        array.type = OperatorRegistry.VOID_ARRAY;
        len.params.add(array);
        funcsToNodes.put(len, null);
        return funcsToNodes;
    }

    public java.lang.Object visit(Kitsch.yyTree.top_stmt node) {
        Kitsch.yyTree.Visit elem;
        for (int x = 0; x < node.size(); x++) {
            elem = (Kitsch.yyTree.Visit) node.get(x);
            if (elem instanceof Kitsch.yyTree.FuncDecl) {
                elem.visit(this);
            }
        }
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Stmt node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.IfStmt node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.IfBlock node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.ElifBlock node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.ElseBlock node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.WhileStmt node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.GlbDecl node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Assign node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.FuncDecl node) {
        String name = (String) node.get(0);
        Function f = new Function(name);
        funcsToNodes.put(f, node);
        currentFunc = f;
        ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Params node) {
        Kitsch.yyTree.Visit elem;
        boolean defaultSeen = false;
        for (int x = 0; x < node.size(); x++) {
            elem = (Kitsch.yyTree.Visit) node.get(x);
            if (elem instanceof Kitsch.yyTree.DefPar) {
                defaultSeen = true;
            } else if (elem instanceof Kitsch.yyTree.IdCls && defaultSeen) {
                System.err.println("Error: default parameters for function " + currentFunc.name + " before all non-default parameters.");
                error = true;
            }
            if (elem != null) {
                Parameter p = (Parameter) elem.visit(this);
                currentFunc.params.add(p);
            }
        }
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.DefPar node) {
        Parameter p = (Parameter) ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        Object dv = ((Kitsch.yyTree.Visit) node.get(1)).visit(this);
        if (dv == null) {
            System.err.println("Error: Unsupported operation in default value " + "assignment for function " + currentFunc.name);
            error = true;
        } else {
            p.defaultValue = dv;
        }
        return p;
    }

    public java.lang.Object visit(Kitsch.yyTree.RetCls node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Print node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Read node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Lt node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Lte node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Gt node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Gte node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Eq node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Neq node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.LgAnd node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.LgOr node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.LgNot node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Expr node) {
        if (node.size() > 1) {
            return null;
        } else {
            return ((Kitsch.yyTree.Visit) node.get(0)).visit(this);
        }
    }

    public java.lang.Object visit(Kitsch.yyTree.Add node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Sub node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Mul node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Div node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Mod node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Exp node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.BwAnd node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.BwOr node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.BwXor node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.BwNot node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Call node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.ArrayLit node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Parens node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Cast node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.UNeg node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.UPos node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.NumCls node) {
        return node.get(0);
    }

    public java.lang.Object visit(Kitsch.yyTree.BoolCls node) {
        return node.get(0);
    }

    public java.lang.Object visit(Kitsch.yyTree.StrCls node) {
        return node.get(0);
    }

    public java.lang.Object visit(Kitsch.yyTree.TypeCls node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.IdCls node) {
        Parameter p = new Parameter((String) node.get(0));
        return p;
    }

    public java.lang.Object visit(Kitsch.yyTree.EolCls node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.SubStr node) {
        return null;
    }

    public java.lang.Object visit(Kitsch.yyTree.Range node) {
        return null;
    }

    /** hook for unknown classes. */
    public java.lang.Object visit(Kitsch.yyTree.Visit node) {
        return null;
    }
}

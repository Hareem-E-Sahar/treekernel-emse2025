import java.io.*;
import java.util.*;

/**
 * Creates an OO world.
 */
abstract class AbstractOOSink extends FileSink {

    protected final String tab;

    AbstractOOSink(File outdir, String tab) {
        super(outdir);
        this.tab = tab;
    }

    AbstractOOSink(File outdir) {
        this(outdir, "  ");
    }

    abstract String typeHeader(World.Type t);

    abstract String typeFooter(World.Type t);

    abstract boolean wantsPublic();

    abstract String getExtends();

    abstract String formatMember(World.Decl d);

    abstract String constructorStart(String name);

    abstract String methodStart(World.Decl d);

    abstract boolean useParamType();

    abstract String thiz(String name);

    abstract String var(String name);

    abstract void createMethodBody(PrintStream out, World.Type t, World.Decl d);

    abstract String lineComment();

    abstract String comment(String s);

    abstract String openBrace();

    abstract String closeBrace();

    /**
   * @return access to methods, defaults to <tt>thiz(name)</tt>
   */
    String methodThiz(String name) {
        return thiz(name);
    }

    /**
   * @return <tt>true</tt> if we have member variables, defaults to <tt>true</tt>
   */
    boolean wantMembers() {
        return true;
    }

    /** 
   * @return first member to constructor -- e.g. <tt>self</tt> in Python,
   *         <tt>null</tt> by default
   */
    String firstMemberToConstructor() {
        return null;
    }

    /** 
   * @return <tt>true</tt> if you want commas, <tt>true</tt> by default 
   */
    boolean wantsSemis() {
        return true;
    }

    /**
   * @return end of the class, defaults to {@link #closeBrace()}
   */
    String endClass() {
        return closeBrace();
    }

    /**
   * @return the class heading, defaults to <tt>"class " + t.getName()</tt>
   * @param t the type in question
   */
    String classHeading(World.Type t) {
        return "class " + t.getName();
    }

    /**
   * @return whether we enclose the type in braces, defaults to <tt>true</tt>
   */
    boolean bracesEncloseType() {
        return true;
    }

    /**
   * @return <tt>true</tt> if we have params, defaults to <tt>true</tt>
   */
    boolean wantsParams() {
        return true;
    }

    /**
   * @return the start of the constructor, defaults to <tt>null</tt>
   */
    String constructorStart(List<World.Decl> decls) {
        return null;
    }

    /**
   * @return the end of the constructor, defaults to <tt>null</tt>
   */
    String constructorEnd() {
        return null;
    }

    /**
   * @return init code for a variable, defaults to 
   *         <tt>thiz(d.getName()) + " = " + var(d.getName())</tt>
   */
    String varInit(World.Decl d) {
        return thiz(d.getName()) + " = " + var(d.getName());
    }

    /**
   * @return code in the front of methods, defaults to <tt>null</tt>
   */
    String methodBegin(World.Type t, World.Decl d) {
        return null;
    }

    /**
   * @return code in the end of methods, defaults to <tt>null</tt>
   */
    String methodEnd(World.Type t, World.Decl d) {
        return null;
    }

    final void output(World.Type t, PrintStream out) throws Exception {
        nn(out, typeHeader(t));
        out.println(lineComment() + " *** DO NOT EDIT: Generated on " + new Date());
        if (wantsPublic()) out.print("public ");
        nn(out, classHeading(t));
        String ex = getExtends();
        if (ex != null) {
            if (t.getSupertype() != null) out.print(" " + ex + " " + t.getSupertype());
        }
        out.println(bracesEncloseType() ? " " + openBrace() : "");
        if (wantMembers()) {
            for (World.Decl d : t.getDecls()) {
                if (isImmutable(d)) {
                    out.print(tab + formatMember(d));
                    semi(out);
                    out.println();
                }
            }
        }
        out.print(tab);
        if (wantsPublic()) out.print("public ");
        boolean first = true;
        nn(out, constructorStart(t.getName()));
        if (wantsParams()) {
            out.print("(");
            String self = firstMemberToConstructor();
            if (self != null) {
                out.print(self + ",");
                first = false;
            }
            for (World.Decl d : t.getDecls()) {
                if (!isAttribute(d)) continue;
                if (!first) out.print(",");
                first = false;
                if (useParamType()) out.print(d.getType() + " ");
                out.print(var(d.getName()));
            }
            out.print(") ");
        }
        out.println(openBrace());
        nn(out, constructorStart(t.getDecls()));
        for (World.Decl d : t.getDecls()) {
            if (!isAttribute(d)) continue;
            out.print(tab + tab);
            nn(out, varInit(d));
            semi(out);
            out.println();
        }
        nn(out, constructorEnd());
        out.println(tab + closeBrace());
        for (World.Decl d : t.getDecls()) {
            if (isSynthetic(d)) {
                out.print(tab);
                if (wantsPublic()) out.print("public ");
                out.print(nonNull(methodStart(d)));
                out.print(" get" + Util.capitilize(d.getName()) + "(");
                if (d.getParams() != null) {
                    first = true;
                    for (World.Param p : d.getParams()) {
                        if (p.getType() != null) {
                            if (!first) out.print(",");
                            first = false;
                            if (useParamType()) out.print(d.getType() + " ");
                            out.print(p.getName());
                        }
                    }
                }
                out.println(") " + openBrace());
                nn(out, methodBegin(t, d));
                createMethodBody(out, t, d);
                nn(out, methodEnd(t, d));
                out.println();
                out.println(tab + closeBrace());
            } else {
                out.print(tab);
                if (wantsPublic()) out.print("public ");
                out.print(nonNull(methodStart(d)));
                out.print(getGetMethodName(d) + "() " + openBrace());
                nn(out, methodBegin(t, d));
                out.print("return " + thiz(d.getName()));
                semi(out);
                out.print(closeBrace());
                out.println();
                if (isMutable(d)) {
                    out.print(tab);
                    if (wantsPublic()) out.print("public ");
                    out.print(nonNull(methodStart(d)));
                    out.print(getSetMethodName(d) + "(");
                    if (useParamType()) out.print(d.getType() + " ");
                    out.print(d.getName() + ") " + openBrace() + "return " + thiz(d.getName()) + " = " + d.getName());
                    semi(out);
                    out.print(closeBrace());
                }
            }
        }
        nn(out, endClass());
        nn(out, typeFooter(t));
        out.println();
        out.flush();
    }

    private void nn(PrintStream out, String s) {
        out.print(nonNull(s));
    }

    private void semi(PrintStream out) {
        if (wantsSemis()) out.print(";");
    }

    private String nonNull(String s) {
        return s == null ? "" : s + " ";
    }

    protected final String getInvokeMethod(World.Type t, List<World.Param> params, World.Param p) {
        World.Decl d = getDecl(t, p.getName());
        return getInvokeMethod(t, params, d, p.getName());
    }

    protected final String getInvokeMethod(World.Type t, List<World.Param> params, World.Decl d, String name) {
        StringBuffer s = new StringBuffer(methodThiz(getGetMethodName(name)));
        s.append("(");
        boolean first = true;
        for (World.Param p : d.getParams()) {
            if (!first) s.append(",");
            first = false;
            if (p.getType() == null) {
            } else {
                s.append(p.getName());
            }
        }
        s.append(")");
        return s.toString();
    }

    protected final World.Decl getDecl(World.Type t, String name) {
        for (World.Decl d : t.getDecls()) {
            if (d.getName().equals(name)) return d;
        }
        return null;
    }

    protected final String getGetMethodName(World.Decl d) {
        return getGetMethodName(d.getName());
    }

    protected final String getGetMethodName(String name) {
        return "get" + Util.capitilize(name);
    }

    protected final String getSetMethodName(World.Decl d) {
        return "set" + Util.capitilize(d.getName());
    }
}

package cn.webwheel.database.engine;

import cn.webwheel.compiler.JavaC;
import cn.webwheel.database.*;
import cn.webwheel.database.annotations.Param;
import cn.webwheel.database.annotations.Select;
import cn.webwheel.database.annotations.Update;
import cn.webwheel.el.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Maker {

    static final String SPSign = ":";

    private static int seq;

    private Class<?> cls;

    private StringBuilder src;

    private IPoolFactory factory;

    private Map<UserType, String> userTypeMap = new HashMap<UserType, String>();

    public Maker(Class<?> cls, IPoolFactory factory) {
        this.factory = factory;
        if (!Modifier.isAbstract(cls.getModifiers()) || !Modifier.isPublic(cls.getModifiers())) {
            throw new IllegalArgumentException("class should be public abstract class or public interface: " + cls);
        }
        if (!cls.isInterface()) {
            try {
                cls.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(cls + " must have default constructor");
            }
        }
        this.cls = cls;
    }

    private void code(String s) {
        src.append(s);
    }

    private static class MethodHelper {

        Method method;

        private MethodHelper(Method method) {
            this.method = method;
        }

        @Override
        public int hashCode() {
            return method.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MethodHelper)) return false;
            Method m = ((MethodHelper) obj).method;
            if (!method.equals(m)) return false;
            return Arrays.equals(m.getParameterTypes(), method.getParameterTypes());
        }
    }

    private Set<MethodHelper> getAllAbstractMethods(Class<?> cls, Set<MethodHelper> set) {
        loop: for (Method method : cls.getDeclaredMethods()) {
            if (!Modifier.isAbstract(method.getModifiers())) continue;
            for (MethodHelper mh : set) {
                if (method.getName().equals(mh.method.getName()) && Arrays.equals(method.getParameterTypes(), mh.method.getParameterTypes())) {
                    continue loop;
                }
            }
            set.add(new MethodHelper(method));
        }
        for (Class<?> ic : cls.getInterfaces()) {
            getAllAbstractMethods(ic, set);
        }
        cls = cls.getSuperclass();
        if (cls != null && cls != Object.class) {
            getAllAbstractMethods(cls, set);
        }
        return set;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends IPoolObject> create() throws Exception {
        if (src != null) throw new IllegalStateException();
        src = new StringBuilder();
        String name = "SqlUtils_" + cls.getSimpleName() + "_" + seq++;
        code("public class " + name + " ");
        if (Modifier.isInterface(cls.getModifiers())) {
            code("implements " + cls.getCanonicalName() + ", " + IPoolObject.class.getName() + " {");
        } else {
            code("extends " + cls.getCanonicalName() + " implements " + IPoolObject.class.getName() + " {");
        }
        code("private " + IPoolImpl.class.getName() + " _pool;");
        code("public void setIPoolImpl(" + IPoolImpl.class.getName() + " _pool) {this._pool = _pool;}");
        for (MethodHelper methodHelper : getAllAbstractMethods(cls, new HashSet<MethodHelper>())) {
            Method method = methodHelper.method;
            Class<?>[] ets = method.getExceptionTypes();
            if (ets == null || ets.length != 1 || ets[0] != Exception.class) {
                throw new IException("there must be one and only one java.lang.Exception declared on " + method);
            }
            code("public " + method.getReturnType().getCanonicalName() + " " + method.getName() + "(");
            Class<?>[] pts = method.getParameterTypes();
            for (int i = 0; i < pts.length; i++) {
                code(pts[i].getCanonicalName() + " $" + (i + 1) + ",");
            }
            if (pts.length > 0) {
                src.setCharAt(src.length() - 1, ')');
            } else {
                code(")");
            }
            code("throws java.lang.Exception {");
            String retVar;
            if (method.getAnnotation(Select.class) != null) {
                prepareStatement(method, method.getAnnotation(Select.class).value());
                retVar = getResultSet(method);
            } else if (method.getAnnotation(Update.class) != null) {
                prepareStatement(method, method.getAnnotation(Update.class).value());
                if (method.getReturnType() == Void.TYPE) {
                    code("_stmt.executeUpdate();");
                    retVar = null;
                } else if (method.getReturnType() == Integer.TYPE) {
                    retVar = "_r" + seq++;
                    code("int " + retVar + " = _stmt.executeUpdate();");
                } else {
                    throw new IException("Update method can only have a return type of int or void: " + method);
                }
            } else {
                throw new IException("there's no Select or Update annotation on " + method);
            }
            complete(retVar);
            code("}");
        }
        for (Map.Entry<UserType, String> entry : userTypeMap.entrySet()) {
            code("public static " + UserType.class.getCanonicalName() + " " + entry.getValue() + ";");
        }
        code("}");
        Class cls = JavaC.getInst().compile(name, src.toString());
        for (Map.Entry<UserType, String> entry : userTypeMap.entrySet()) {
            cls.getField(entry.getValue()).set(null, entry.getKey());
        }
        return cls;
    }

    private void complete(String retVar) {
        if (retVar != null) code("return " + retVar + ";");
        code("} finally {");
        code("if(_rs!=null) try {_rs.close();} catch(java.sql.SQLException e){}");
        code("if(_stmt!=null) try{_stmt.close();} catch(java.sql.SQLException e){}");
        code("_pool.closeConnection(_con);");
        code("}");
    }

    private void prepareStatement(Method method, String sql) {
        if (sql.startsWith(SPSign)) {
            if (factory.sqlProvider != null) {
                String s = factory.sqlProvider.sql(method, sql.substring(SPSign.length()));
                if (s != null) {
                    sql = s;
                }
            }
        }
        GenericClass[] pts;
        {
            Type[] types = method.getGenericParameterTypes();
            pts = new GenericClass[types.length];
            for (int i = 0; i < types.length; i++) {
                pts[i] = new GenericClass(types[i]);
            }
        }
        if (pts.length > 0 && PreparedStatementProvider.class.isAssignableFrom(pts[0].toClass())) {
            prepareStatementWithProvider(sql);
            return;
        }
        if (sql.startsWith(SPSign)) {
            throw new IException("can not find sql from sql provider: " + sql.substring(SPSign.length()) + " in " + method);
        }
        Stack<Var> varStack = new Stack<Var>();
        Stack<VarContext> varContextStack = new Stack<VarContext>();
        VarContextImpl vci = new VarContextImpl();
        for (int i = 1; i <= pts.length; i++) {
            VarImpl var = new VarImpl();
            var.type = pts[i - 1];
            var.var = "$" + i;
            vci.map.put("arg" + i, var);
        }
        {
            VarImpl var = new VarImpl();
            var.type = new GenericClass(cls);
            var.var = "this";
            vci.map.put("this", var);
            if (pts.length == 0) {
                varStack.push(var);
            }
        }
        varContextStack.push(vci);
        if (pts.length == 1) {
            VarImpl var = new VarImpl();
            var.type = pts[0];
            var.var = "$1";
            varStack.push(var);
        }
        String[] pns = findParamNames(method);
        for (int i = 0; i < pns.length; i++) {
            if (pns[i] == null) continue;
            VarImpl var = new VarImpl();
            var.type = pts[i];
            var.var = "$" + (i + 1);
            vci.map.put(pns[i], var);
        }
        StringBuilder sb = new StringBuilder();
        int end = 0;
        Pattern pat = Pattern.compile("#.+?#|\\?\\d*|\\$.+?\\$");
        Matcher matcher = pat.matcher(sql);
        List<Expression> params = new ArrayList<Expression>();
        List<UserTypeInfo> utis = new ArrayList<UserTypeInfo>();
        int qidx = 1;
        while (matcher.find()) {
            if (end != matcher.start()) {
                sb.append(Utils.strSrc(sql.substring(end, matcher.start())));
            }
            end = matcher.end();
            String s = matcher.group();
            Expression exp = null;
            UserTypeInfo uti = null;
            try {
                if (s.startsWith("#")) {
                    s = s.substring(1, s.length() - 1);
                    int idx = s.lastIndexOf(':');
                    if (idx != -1) {
                        uti = factory.userTypeInfoMap.get(s.substring(idx + 1));
                        if (uti != null) {
                            s = s.substring(0, idx);
                        }
                    }
                    exp = new ExpParser(varStack, varContextStack).parse(s);
                } else if (s.startsWith("$")) {
                    sb.append("\" + ");
                    sb.append(new ExpParser(varStack, varContextStack).parse(s = s.substring(1, s.length() - 1)).getEvalString());
                    sb.append(" + \"");
                } else {
                    int qi;
                    if (s.length() > 1) {
                        qi = Integer.parseInt(s.substring(1));
                    } else {
                        qi = qidx++;
                    }
                    if (qi > pts.length) {
                        throw new IException("there are too few parameters in " + method);
                    }
                    exp = new ExpParser(varStack, varContextStack).parse("arg" + qi);
                }
            } catch (ParseException e) {
                throw new IException("can not parse expression \"" + s + "\" in " + method);
            }
            if (exp != null) {
                if (uti == null) {
                    if (!Utils.isBasicType(exp.getReturnType().toClass())) {
                        for (Map.Entry<String, UserTypeInfo> entry : factory.userTypeInfoMap.entrySet()) {
                            if (entry.getValue().bCls.equals(exp.getReturnType().toClass())) {
                                uti = entry.getValue();
                                break;
                            }
                        }
                        if (uti == null) {
                            throw new IException("parameter \"" + s + "\" is not basic type in " + method);
                        }
                    }
                } else {
                    if (!exp.getReturnType().toClass().isAssignableFrom(uti.bCls)) {
                        throw new IException("user type " + uti.userType.getClass() + " is not compatible with " + s + " in " + method);
                    }
                }
                params.add(exp);
                utis.add(uti);
                sb.append('?');
            }
        }
        if (end != sql.length()) {
            sb.append(Utils.strSrc(sql.substring(end)));
        }
        sql = sb.toString();
        code("java.sql.Connection _con = _pool.getConnection();");
        code("java.sql.PreparedStatement _stmt = null;");
        code("java.sql.ResultSet _rs = null;");
        code("try {");
        code("_stmt = _con.prepareStatement(\"" + sql + "\");");
        for (int i = 0; i < params.size(); i++) {
            Expression exp = params.get(i);
            UserTypeInfo uti = utis.get(i);
            if (uti == null) {
                Utils.setStmtParam(exp.getReturnType().toClass(), src, i + 1, exp.getEvalString());
            } else {
                String utv = userTypeMap.get(uti.userType);
                if (utv == null) userTypeMap.put(uti.userType, utv = "_ut" + seq++);
                Utils.setStmtParam(uti.dCls, src, i + 1, "(" + uti.dCls.getCanonicalName() + ")" + utv + ".beanToDB(" + exp.getEvalString() + ")");
            }
        }
    }

    private String[] findParamNames(Method method) {
        Annotation[][] as = method.getParameterAnnotations();
        String[] list = new String[as.length];
        Param param = method.getAnnotation(Param.class);
        if (param != null) {
            for (int i = 0; i < param.value().length; i++) {
                list[i] = param.value()[i];
            }
        }
        for (int i = 0; i < as.length; i++) {
            Annotation[] a = as[i];
            for (Annotation an : a) {
                if (an instanceof Param) {
                    list[i] = ((Param) an).value()[0];
                    break;
                }
            }
        }
        return list;
    }

    private void prepareStatementWithProvider(String sql) {
        code("java.sql.Connection _con = _pool.getConnection();");
        code("java.sql.PreparedStatement _stmt = null;");
        code("java.sql.ResultSet _rs = null;");
        code("try {");
        code("_stmt = $1.get(_con, \"" + Utils.strSrc(sql) + "\");");
    }

    private String getResultSet(Method method) {
        if (method.getGenericReturnType() == Void.TYPE) {
            Class<?>[] pts = method.getParameterTypes();
            int ptlen = pts.length;
            if (ptlen > 0 && ResultSetParser.class.isAssignableFrom(pts[ptlen - 1])) {
                code("$" + ptlen + ".parse(_rs = _stmt.executeQuery());");
                return null;
            }
        }
        code("_rs = _stmt.executeQuery();");
        Select select = method.getAnnotation(Select.class);
        if (method.getReturnType() == boolean.class && select.result().isEmpty() && select.value().trim().toLowerCase().startsWith("select 1 from ")) {
            return "_rs.next()";
        }
        ResultDesc resultDesc = new ResultDesc(cls, method, factory);
        Class type = method.getReturnType();
        if (type == Void.TYPE) {
            if (resultDesc.isFlat()) {
                code("if(!_rs.next()) throw new " + NoResultException.class.getName() + "();");
                getFlatResult(resultDesc, "this");
            } else {
                getRichResult(resultDesc, 0);
            }
            return null;
        } else if (type == List.class) {
            code("java.util.List _l0 = new java.util.ArrayList();");
            if (!resultDesc.hasSubResults()) {
                code("while(_rs.next()) {");
                code(resultDesc.getUserTypeInfo().bCls.getCanonicalName() + " _b0;");
                getBasicResult(resultDesc, "1", "_b0");
                code("_l0.add(_b0);");
                code("}");
            } else if (resultDesc.isFlat()) {
                code("while(_rs.next()) {");
                FlatResult fr = getFlatResult(resultDesc, null);
                code("_l0.add(" + fr.fvar + ");");
                code("}");
            } else {
                code(resultDesc.getUserTypeInfo().bCls.getCanonicalName() + " _b0 = null;");
                getRichResult(resultDesc, 1);
            }
            return "_l0";
        } else {
            if (!resultDesc.hasSubResults()) {
                code("if(!_rs.next()) throw new " + NoResultException.class.getName() + "();");
                code(resultDesc.getUserTypeInfo().bCls.getCanonicalName() + " _b0;");
                getBasicResult(resultDesc, "1", "_b0");
            } else if (resultDesc.isFlat()) {
                code("if(!_rs.next()) return null;");
                return getFlatResult(resultDesc, null).fvar;
            } else {
                code(resultDesc.getUserTypeInfo().bCls.getCanonicalName() + " _b0 = null;");
                getRichResult(resultDesc, 2);
            }
            return "_b0";
        }
    }

    private void getBasicResult(ResultDesc resultDesc, String idx, String var) {
        if (resultDesc.getUserTypeInfo().userType != null) {
            String utv = userTypeMap.get(resultDesc.getUserTypeInfo().userType);
            if (utv == null) userTypeMap.put(resultDesc.getUserTypeInfo().userType, utv = "_ut" + seq++);
            String bv = "_br" + seq++;
            code(resultDesc.getUserTypeInfo().dCls.getCanonicalName() + " " + bv + ";");
            Utils.getBasicResult(resultDesc.getUserTypeInfo().dCls, src, idx, bv);
            code(var + " = (" + resultDesc.getUserTypeInfo().bCls.getCanonicalName() + ")" + utv + ".DBToBean(" + bv + ");");
        } else {
            Utils.getBasicResult(resultDesc.getUserTypeInfo().bCls, src, idx, var);
        }
    }

    private String getPoolInst(Class cls) {
        return "((" + cls.getCanonicalName() + ")_pool.get(" + cls.getCanonicalName() + ".class))";
    }

    private static class FlatResult {

        ResultDesc resultDesc;

        String var;

        String fvar;

        private FlatResult(ResultDesc resultDesc, String var, String fvar) {
            this.resultDesc = resultDesc;
            this.var = var;
            this.fvar = fvar;
        }
    }

    private FlatResult getFlatResult(ResultDesc resultDesc, String var) {
        ResultDesc rt = null;
        if (var == null) {
            var = "_v" + seq++;
            code(resultDesc.getUserTypeInfo().bCls.getCanonicalName() + " " + var + " = " + getPoolInst(resultDesc.getUserTypeInfo().bCls) + ";");
        }
        String bvar = null;
        if (resultDesc.isOuterJoin()) {
            bvar = "_b" + seq++;
            code("boolean " + bvar + " = true;");
        }
        for (Map.Entry<MethodField, ResultDesc> entry : resultDesc.getSubResults().entrySet()) {
            MethodField mf = entry.getKey();
            ResultDesc rd = entry.getValue();
            if (mf.getType() == List.class) {
                code(var + "." + mf.set("_tlist = new java.util.ArrayList()"));
                rt = rd;
            } else if (rd.hasSubResults()) {
                FlatResult fr = getFlatResult(rd, null);
                if (fr.resultDesc != null) rt = fr.resultDesc;
                code(var + "." + mf.set(fr.fvar));
                if (resultDesc.isOuterJoin()) {
                    code("if(" + bvar + " && " + fr.fvar + "!=null) " + bvar + " = false;");
                }
            } else {
                String v = "_v" + seq++;
                code(rd.getUserTypeInfo().bCls.getCanonicalName() + " " + v + ";");
                getBasicResult(rd, rd.getColumnName(), v);
                code(var + "." + mf.set(v));
                if (resultDesc.isOuterJoin()) {
                    code("if(" + bvar + " && !_rs.wasNull()) " + bvar + " = false;");
                }
            }
        }
        String fvar = var;
        if (resultDesc.isOuterJoin()) {
            code("if(" + bvar + ") " + var + " = null;");
        }
        return new FlatResult(rt, var, fvar);
    }

    private void getRichResult(ResultDesc resultDesc, int which) {
        {
            ResultDesc rd = resultDesc;
            int dp = 1;
            while ((rd = rd.getListResultDesc()) != null) {
                code("java.util.List _l" + dp + " = null;");
                code("java.lang.Object _b" + dp + " = null;");
                dp++;
            }
        }
        code("java.util.List _tlist = null;");
        if (which == 0) code("boolean _first = true;");
        code("while(_rs.next()) {");
        switch(which) {
            case 0:
                code("if(_first) {");
                code("_first = false;");
                FlatResult fr = getFlatResult(resultDesc, "this");
                code("_l1" + " = _tlist;");
                code("}");
                getRichResultDeep(fr.resultDesc, 1);
                break;
            case 1:
                getRichResultDeep(resultDesc, 0);
                break;
            case 2:
                code("if(_b0==null) {");
                fr = getFlatResult(resultDesc, null);
                code("_b0 = " + fr.fvar + ";");
                code("_l1" + " = _tlist;");
                code("}");
                getRichResultDeep(fr.resultDesc, 1);
                break;
            default:
                throw new IException("impossible");
        }
        code("}");
        if (which == 0) code("if(_first) throw new " + NoResultException.class.getName() + "();");
    }

    private void getRichResultDeep(ResultDesc resultDesc, int deep) {
        FlatResult fr = getFlatResult(resultDesc, null);
        if (fr.resultDesc == null) {
            if (resultDesc.isOuterJoin()) code("if(" + fr.var + "!=null) ");
            code("_l" + deep + ".add(" + fr.fvar + ");");
            return;
        }
        if (resultDesc.isOuterJoin()) {
            code("if(" + fr.var + "==null) {");
            code("_b" + deep + " = null;");
            code("continue;");
            code("}");
        }
        code("if(!" + fr.var + ".equals(_b" + deep + ")) {");
        code("_l" + deep + ".add(" + fr.fvar + ");");
        code("_b" + deep + " = " + fr.var + ";");
        code("_l" + (deep + 1) + " = _tlist;");
        code("_b" + (deep + 1) + " = null;");
        code("}");
        getRichResultDeep(fr.resultDesc, deep + 1);
    }

    private static class VarContextImpl implements VarContext {

        Map<String, Var> map = new HashMap<String, Var>();

        public Var getVar(String name) {
            return map.get(name);
        }
    }

    private static class VarImpl implements Var {

        GenericClass type;

        String var;

        public GenericClass getType() {
            return type;
        }

        public String getVar() {
            return var;
        }
    }
}

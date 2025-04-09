package org.sss.module.hibernate.compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sss.module.IModuleManager;
import org.sss.module.eibs.DataType;
import org.sss.module.eibs.Datafield;
import org.sss.module.eibs.Module;
import org.sss.module.eibs.ModuleRef;
import org.sss.module.eibs.Rule;
import org.sss.module.eibs.RuleType;
import org.sss.module.eibs.compile.CompileException;
import org.sss.util.ContainerUtils;

/**
 * Hibernate名称处理工具类
 * @author Jason.Hoo (latest modification by $Author: hujianxin78728 $)
 * @version $Revision: 710 $ $Date: 2012-04-22 05:13:08 -0400 (Sun, 22 Apr 2012) $
 */
public class NameUtils {

    static final Log log = LogFactory.getLog(NameUtils.class);

    public static String proxyTypeName(String name) {
        if ("IModule".equals(name)) return "ProxyModule<IModule>";
        return "Proxy" + typeName(name);
    }

    public static String listTypeName(String name) {
        return "Elst" + typeName(name);
    }

    public static String eibsTypeName(String name) {
        return "Eibs" + typeName(name);
    }

    public static String typeName(String name) {
        if (IModuleManager.MODULE_OBJECT.equalsIgnoreCase(name)) return "IModule";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String varyName(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static String typeName(Datafield datafield) {
        DataType type = datafield.getDatatype();
        switch(type.getValue()) {
            case DataType.TEXT:
            case DataType.BLOCK:
            case DataType.CONTROL:
                return "String";
            case DataType.NUMERIC:
                if (datafield.getLineOrDecimalSize() > 0) return "BigDecimal";
                return "Integer";
            case DataType.DATE:
                return "Date";
            case DataType.STREAM:
                return "IStream";
            default:
                return "Object";
        }
    }

    public static String ruleName(Rule rule) {
        StringBuffer sb = new StringBuffer(rule.getType().getName());
        if (RuleType.EVENT_LITERAL.equals(rule.getType())) sb.append(rule.getEventType().getValue());
        return sb.append(rule.getOrder()).append("_").append(rule.getName().replace('\\', '_').replaceAll("\\[[\\w]*\\]", "")).toString();
    }

    public static String fieldName(IModuleManager manager, String path) {
        String fieldName = fieldName(manager, "", path);
        if (fieldName.charAt(0) == '.') return fieldName.substring(1);
        return fieldName;
    }

    public static String fieldName(IModuleManager manager, String prefix, String path) {
        StringBuffer sb = new StringBuffer();
        convertPath(manager, null, sb, prefix, path);
        return sb.toString();
    }

    /**
   * 对于表达式进行替换
   */
    public static void convertExpression(IModuleManager manager, Module module, StringBuffer sb, String expression, String dataType) {
        Pattern pattern = Pattern.compile("\\$[\\w\\\\\\[\\]]*[\\s*\\.]?");
        Matcher matcher = pattern.matcher(expression);
        int offset = 0;
        while (matcher.find()) {
            String path = expression.substring(matcher.start() + 1, matcher.end());
            if (!path.endsWith(".")) {
                sb.append(expression.substring(offset, matcher.start()));
                convertPath(manager, module, sb, path + ".getValue(");
                sb.append(")");
                offset = matcher.end();
            }
        }
        if (offset == 0 && "BigDecimal".equals(dataType)) sb.append("new BigDecimal(").append(expression).append(")"); else sb.append(expression.substring(offset));
    }

    public static void convertPath(IModuleManager manager, Module module, StringBuffer sb, String path) {
        convertPath(manager, module, sb, null, path);
    }

    static final List<String> roots = new ArrayList<String>();

    static {
        roots.add("sysmod");
        roots.add("trnmod");
        roots.add("btnmod");
        roots.add("rmkmod");
        roots.add("bimenu");
        roots.add("mtabut");
    }

    /**
   * 对于Path[.[sg]etValue(]进行替换
   */
    public static void convertPath(IModuleManager manager, Module module, StringBuffer sb, String prefix, String path) {
        HibernateEntityCompile.text = path;
        if ("\\".equals(path)) {
            sb.append("ctx.getRoot()");
            return;
        }
        if (path.startsWith("\\")) {
            path = path.substring(1);
            int index = path.indexOf('\\');
            if (index < 0) index = path.indexOf('.');
            String subPath;
            if (index < 0) {
                subPath = path;
                path = "";
            } else {
                subPath = path.substring(0, index);
                path = path.substring(index + 1);
            }
            String[] subPaths = subPath.split("\\[|\\]");
            if (!roots.contains(subPaths[0])) throw new CompileException("Cann't use root module as '" + subPaths[0] + "'");
            ModuleRef moduleRef = (ModuleRef) manager.getEibsObject(null, subPaths[0]);
            if (moduleRef == null) throw new CompileException("Root module '" + subPaths[0] + "' not found.");
            if (moduleRef.isList()) {
                sb.append("((").append(listTypeName(moduleRef.getType())).append(")ctx.getRoot().get(\"").append(subPaths[0]).append("\"))");
                if (subPaths.length > 1 && !ContainerUtils.isEmpty(subPaths[1])) sb.append(".get(").append(subPaths[1]).append(")");
            } else sb.append("((").append(typeName(moduleRef.getType())).append(")ctx.getRoot().get(\"").append(subPaths[0]).append("\"))");
        } else if (module != null) sb.append(typeName(module.getName())).append(".this"); else if (prefix != null) sb.append(prefix);
        if (ContainerUtils.isEmpty(path)) return;
        for (String subPath : path.split("\\\\")) appendTypeName(manager, module, sb, subPath);
    }

    private static void appendTypeName(IModuleManager manager, Module module, StringBuffer sb, String path) {
        if (path.endsWith(".getValue(")) sb.append(".getEibs").append(typeName(path.substring(0, path.length() - 10))).append("("); else if (path.endsWith(".setValue(")) sb.append(".setEibs").append(typeName(path.substring(0, path.length() - 10))).append("("); else {
            String[] paths = path.split("\\[|\\]");
            sb.append(".get").append(typeName(paths[0])).append("()");
            if (paths.length > 1 && !ContainerUtils.isEmpty(paths[1])) sb.append(".get(").append(paths[1]).append(")");
        }
    }

    private static HashMap<String, String[]> convertMap = new HashMap<String, String[]>();

    static {
        put("Platform.setLocale(", "ctx.setLocale(");
        put("Platform.getLocale(", "ctx.getLocale(");
        put("Platform.errorCode(", "ctx.getError(");
        put("Platform.chain(", "ctx.getGui().chain(");
        put("Platform.returnToCaller(", "ctx.getGui().chain(CALLER");
        put("Platform.popup(", "ctx.getGui().popup(");
        put("Platform.popupOnly(", "ctx.getGui().popupOnly(");
        put("Platform.closePanel(", "ctx.getGui().close(");
        put("Platform.error(", "ctx.getGui().error(");
        put("Platform.message(", "ctx.getGui().message(");
        put("Platform.prompt(", "ctx.getGui().prompt(");
        put("Platform.setRequired(", "ctx.getGui().setRequired(");
        put("Platform.setNotRequired(", "ctx.getGui().setNotRequired(");
        put("Platform.isRequired(", "ctx.getGui().isRequired(");
        put("Platform.enable(", "ctx.getGui().enable(");
        put("Platform.disable(", "ctx.getGui().disable(");
        put("Platform.isEnabled(", "ctx.getGui().isEnabled(");
        put("Platform.visible(", "ctx.getGui().visible(");
        put("Platform.invisible(", "ctx.getGui().invisible(");
        put("Platform.isVisible(", "ctx.getGui().isVisible(");
        put("Platform.resetModified(", "ctx.getGui().resetModified(");
        put("Platform.setModified(", "ctx.getGui().setModified(");
        put("Platform.isModified(", "ctx.getGui().isModified(");
        put("Platform.setValues(", "ctx.getGui().setValues(");
        put("Platform.getSelectedRowIndex(", "ctx.getGui().getSelectedRowIndex(");
        put("Platform.setSelectedRowIndex(", "ctx.getGui().setSelectedRowIndex(");
        put("Platform.saveDisplay(", "ctx.getGui().saveDisplay(");
        put("Platform.showDisplay(", "ctx.getGui().showDisplay(");
        put("Platform.getRootPath(", "ctx.getGui().getRootPath(");
        put("Platform.putCodetable(", "ctx.getGui().putCodetable(");
        put("Platform.getCodetable(", "ctx.getGui().getCodetable(");
        put("Platform.getCodetableLabel(", "ctx.getGui().getCodetableLabel(");
        put("Platform.getI18nString(", "ctx.getGui().getI18nValue(");
        put("Platform.getI18nValue(", "ctx.getGui().getI18nValue(");
        put("Platform.setDescription(", "ctx.getGui().setDescription(");
        put("Platform.encryptPassword(", "ctx.getGui().getPasswordFilter().encode(");
        put("Platform.isDebugMode(", "ctx.getGui().isDebugMode(");
        put("Platform.isBackground(", "ctx.getGui().isBackground(");
        put("Platform.logout(", "ctx.getGui().logout(");
        put("Platform.getLoginData(", "ctx.getAuth().getValue(");
        put("Platform.getLoginUser(", "ctx.getAuth().getLoginUser(");
        put("Platform.checkAll(", "ctx.getSession().checkAll(");
        put("Platform.postEventRule(", "ctx.getSession().postEventRule(");
        put("Platform.postDefaultRule(", "ctx.getSession().postDefaultRule(");
        put("Platform.getModule(", "ctx.getSession().getBaseObject(", "(IModule)");
        put("Platform.getDatafield(", "ctx.getSession().getBaseObject(", "(IDatafield)");
        put("Platform.getBaseObject(", "ctx.getSession().getBaseObject(");
        put("Platform.getTransName(", "ctx.getSession().getTransName(");
        put("Platform.saveData(", "ctx.getSession().saveData(");
        put("Platform.loadData(", "ctx.getSession().loadData(");
        put("Platform.getPath(", "ctx.getSession().getPath(");
        put("Platform.getModuleType(", "ctx.getSession().getModuleType(");
        put("Platform.restoreData(", "ctx.getSession().restoreData(");
        put("Platform.storeData(", "ctx.getSession().storeData(");
        put("Platform.dbBegin(", "ctx.getSupport().begin(");
        put("Platform.dbCommit(", "ctx.getSupport().commit(");
        put("Platform.dbRollback(", "ctx.getSupport().rollback(");
        put("Platform.dbCounter(", "ctx.getSupport().count(");
        put("Platform.dbRead(", "ctx.getSupport().get(");
        put("Platform.dbReadset(", "ctx.getSupport().find(");
        put("Platform.dbDelete(", "ctx.getSupport().delete(");
        put("Platform.dbInsert(", "ctx.getSupport().save(");
        put("Platform.dbUpdate(", "ctx.getSupport().update(");
        put("Platform.dbExecuteSQL(", "ctx.getSupport().execute(");
        put("Platform.dbFetchFields(", "ctx.getSupport().fetch(");
        put("Platform.dbCloseCursor(", "ctx.getSupport().close(");
        put("Platform.dbLock(", "ctx.getSupport().lock(");
        put("Platform.dbUnlock(", "ctx.getSupport().unlock(");
        put("Platform.parseInt(", "ContainerUtils.parseInt(");
        put("Platform.parseDecimal(", "ContainerUtils.parseDecimal(");
        put("Platform.parseDate(", "ContainerUtils.parseDate(");
        put("Platform.diff(", "ContainerUtils.diff(");
        put("Platform.today(", "ContainerUtils.today(");
        put("Platform.format(", "ContainerUtils.format(");
        put("Platform.isEmpty(", "ContainerUtils.isEmpty(");
        put("Platform.getPropertyValue(", "ContainerUtils.getPropertyValue(");
        put("Platform.setPropertyValue(", "ContainerUtils.setPropertyValue(");
        put("Platform.fileCopy(", "ContainerUtils.fileCopy(");
        put("Platform.fileDelete(", "ContainerUtils.fileDelete(");
        put("Platform.fileExists(", "ContainerUtils.fileExists(");
        put("Platform.streamLoad(", "ContainerUtils.streamLoad(");
        put("Platform.streamClear(", "ContainerUtils.streamClear(");
        put("Platform.streamSave(", "ContainerUtils.streamSave(");
        put("Platform.invoke(", "ContainerUtils.invoke(");
        put("Platform.getProperty(", "ContainerUtils.getProperty(");
        put("Platform.setProperty(", "ContainerUtils.setProperty(");
        put("Platform.catPath(", "ContainerUtils.catPath(");
        put("Platform.listFiles(", "ContainerUtils.listFiles(");
        put("Platform.clear(", "ContainerUtils.clear(");
        put("Platform.sqlDate(", "ContainerUtils.sqlDate(");
        put("Platform.add(", "ContainerUtils.add(");
        put("Platform.loadExcel(", "ContainerUtils.loadExcel(");
        put("Platform.unloadExcel(", "ContainerUtils.unloadExcel(");
    }

    public static void put(String key, String... arguments) {
        convertMap.put(key, arguments);
    }

    public static void convertMethodName(StringBuffer sb, String text, boolean root) {
        HibernateEntityCompile.text = text;
        String[] values = convertMap.get(text);
        if (values == null) sb.append(text); else {
            if (values.length > 1 && values[1] != null) sb.append(values[1]);
            sb.append(values[0]);
            if (values.length > 2 && values[2] != null) sb.append(values[2]);
        }
    }
}

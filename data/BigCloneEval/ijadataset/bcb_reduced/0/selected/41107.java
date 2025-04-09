package org.sss.module.hibernate.compile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Entity;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.sss.common.impl.AbstractModule;
import org.sss.common.impl.AbstractModuleList;
import org.sss.common.impl.ConstantDatafield;
import org.sss.common.impl.Constants;
import org.sss.common.impl.DatafieldImpl;
import org.sss.common.impl.ProxyDatafield;
import org.sss.common.impl.I18nImpl;
import org.sss.common.impl.PanelImpl;
import org.sss.common.impl.ProxyModule;
import org.sss.common.impl.ProxyModuleList;
import org.sss.common.impl.RuleUtils;
import org.sss.common.impl.StreamImpl;
import org.sss.common.model.CodetableItem;
import org.sss.common.model.Event;
import org.sss.common.model.IBaseObject;
import org.sss.common.model.ICallback;
import org.sss.common.model.IContext;
import org.sss.common.model.IDatafield;
import org.sss.common.model.IEventRule;
import org.sss.common.model.IModule;
import org.sss.common.model.IModuleList;
import org.sss.common.model.IModuleRule;
import org.sss.common.model.IPanel;
import org.sss.common.model.IParent;
import org.sss.common.model.IResult;
import org.sss.common.model.IRule;
import org.sss.common.model.IState;
import org.sss.common.model.IStream;
import org.sss.common.model.LockInfo;
import org.sss.module.IModuleManager;
import org.sss.module.eibs.Argument;
import org.sss.module.eibs.DataType;
import org.sss.module.eibs.Datafield;
import org.sss.module.eibs.EventType;
import org.sss.module.eibs.Module;
import org.sss.module.eibs.ModuleRef;
import org.sss.module.eibs.Panel;
import org.sss.module.eibs.Rule;
import org.sss.module.eibs.RuleType;
import org.sss.module.eibs.compile.CompileException;
import org.sss.module.eibs.util.CodetextUtils;
import org.sss.module.eibs.util.ModuleConstant;
import org.sss.module.eibs.util.ModulePathUtil;
import org.sss.module.exception.CheckerException;
import org.sss.module.hibernate.HibernateModule;
import org.sss.module.hibernate.HibernateModuleList;
import org.sss.module.hibernate.HibernateModuleRoot;
import org.sss.module.hibernate.HibernateProxyModule;
import org.sss.module.hibernate.HibernateProxyModuleList;
import org.sss.util.ContainerUtils;
import static org.sss.module.hibernate.compile.NameUtils.*;

/**
 * Hibernate编译类
 * @author Jason.Hoo (latest modification by $Author: hujianxin $)
 * @version $Revision: 708 $ $Date: 2012-04-10 06:05:39 -0400 (Tue, 10 Apr 2012) $
 */
public class HibernateEntityCompile {

    static final Log log = LogFactory.getLog(HibernateEntityCompile.class);

    static Module transaction;

    static Module module;

    static Rule rule;

    static String text;

    public static void compile(HibernateCompileContext ctx) throws CompileException {
        try {
            HibernateEntityCompile.transaction = ctx.manager.getModule();
            compileModule(ctx, ctx.manager.getModule(), true);
        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();
            sb.append("transaction:\t").append(transaction.getName());
            if (module != null) sb.append("\r\nmodule:\t").append(module.getName());
            if (rule != null) {
                sb.append("\r\nrule:\t").append(rule.getType().getName()).append(" ").append(rule.getName()).append("(order ").append(rule.getOrder()).append(")");
            }
            if (text != null) sb.append("\r\nlast codetext:").append(text);
            if (e instanceof CompileException) throw new CompileException(sb.append("\r\n\r\n").append(e.getMessage()).toString());
            throw new CompileException(sb.append("\r\n\r\n").append(e.getMessage()).toString(), e);
        }
    }

    private static void compileEibsModule(ClassPrint print, Module module, String typeName) throws IOException {
        print.printImport(Entity.class);
        print.printImport(Table.class);
        print.printImport(Column.class);
        print.printImport(Index.class);
        print.printImport(Id.class);
        print.printImport(GeneratedValue.class);
        print.printImport(GenericGenerator.class);
        print.printImport(Transient.class);
        print.printImport(BigDecimal.class);
        print.printImport(Date.class);
        print.printImport(IStream.class);
        String tableAnnotation = MessageFormat.format("javax.persistence.Table(name=\"{0}\")", module.getTable());
        StringBuffer tableExAnnotation = new StringBuffer();
        if (!module.getIndexes().isEmpty()) {
            tableExAnnotation.append("Table(appliesTo=\"").append(module.getTable()).append("\",indexes={");
            String connIndex = "";
            for (org.sss.module.eibs.Index index : (List<org.sss.module.eibs.Index>) module.getIndexes()) {
                tableExAnnotation.append(connIndex).append("\r\t@Index(name=\"").append(index.getName()).append("\",columnNames={");
                String connColumnName = "";
                for (String columnName : (List<String>) index.getFields()) tableExAnnotation.append(connColumnName).append("\"").append(columnName).append("\"");
                tableExAnnotation.append("})");
            }
            tableExAnnotation.append("})");
        }
        print.printClassBegin(new String[] { "javax.persistence.Entity", "Entity(dynamicInsert=true,dynamicUpdate=true)", tableAnnotation, tableExAnnotation.toString() }, typeName, null, new Class[] { Serializable.class });
        for (Datafield field : (List<Datafield>) module.getDatafields()) {
            String fieldType = typeName(field);
            String varyName = varyName(field.getName());
            print.printFormat("protected {0} {1};", fieldType, varyName);
        }
        for (Datafield field : (List<Datafield>) module.getDatafields()) {
            StringBuffer attribute = new StringBuffer();
            String fieldType = typeName(field);
            String varyName = varyName(field.getName());
            if ("inr".equals(field.getName()) || field.isUnique()) {
                if (!field.isUnique()) field.setUnique(true);
                attribute.append(",unique=true,nullable=false");
                print.println("@Id");
                print.println("@GeneratedValue(generator=\"idGenerator\")");
                print.println("@GenericGenerator(name =\"idGenerator\",strategy=\"assigned\")");
            }
            if (!field.isTransient() && !"Object".equals(fieldType)) {
                String column = field.getColumn();
                if (ContainerUtils.isEmpty(column)) {
                    column = field.getName();
                    field.setColumn(column);
                }
                if (!field.isNullable()) attribute.append(",nullable=").append(field.isNullable());
                if (DataType.NUMERIC_LITERAL.equals(field.getDatatype())) attribute.append(",precision=").append(field.getLength()).append(",scale=").append(field.getLineOrDecimalSize()); else if (DataType.BLOCK_LITERAL.equals(field.getDatatype()) || DataType.TEXT_LITERAL.equals(field.getDatatype())) {
                    int line = field.getLineOrDecimalSize();
                    int size = (field.getLength() + 1) * (line > 0 ? line : 1) - 1;
                    if ("inr".equals(field.getName()) || size == 1) attribute.append(",columnDefinition=\"char(").append(field.getLength()).append(")\""); else attribute.append(",length=").append(size);
                }
                print.printFormat("@Column(name=\"{0}\"{1})", column, attribute);
            } else print.println("@Transient");
            print.printGetMethodBegin(fieldType, field.getName());
            print.printFormat("return {0};", varyName);
            print.printMethodEnd();
            print.printSetMethodBegin(fieldType, field.getName());
            print.printFormat("{0}=value;", varyName);
            print.printMethodEnd();
        }
        print.printClassEnd();
        print.close();
    }

    private static void compileProxyModule(ClassPrint print, Module module, String className) throws IOException {
        boolean flag = getHibernateFlag(module);
        String moduleClassName = typeName(module.getName());
        print.printImport(IParent.class);
        print.printImport(IPanel.class);
        print.printImport(BigDecimal.class);
        print.printImport(Date.class);
        print.printImport(IStream.class);
        print.printImport(IDatafield.class);
        print.printImport(IModule.class);
        print.printClassBegin(className, flag ? HibernateProxyModule.class : ProxyModule.class, moduleClassName);
        print.printFormat("public {0}(IParent parent)", className);
        print.printBegin();
        print.println("super(parent);");
        print.printEnd();
        for (ModuleRef child : (List<ModuleRef>) module.getModules()) {
            String typeName = child.isList() ? listTypeName(child.getType()) : typeName(child.getType());
            print.printGetMethodBegin(typeName, child.getName());
            if (child.isArgument()) {
                print.printFormat("return host.{0}.getHost();", varyName(child.getName()));
                print.printMethodEnd();
                print.printSetMethodBegin(typeName, child.getName());
                print.printFormat("host.{0}.setHost(value);", varyName(child.getName()));
            } else print.printFormat("return host.{0};", varyName(child.getName()));
            print.printMethodEnd();
        }
        for (Datafield field : (List<Datafield>) module.getDatafields()) {
            String varyName = varyName(field.getName());
            String fieldType = typeName(field);
            String eibsName = eibsTypeName(field.getName());
            String type = String.format("IDatafield<%s>", typeName(field));
            print.printGetMethodBegin(type, field.getName());
            print.printFormat("return host.{0};", varyName);
            print.printMethodEnd();
            if (field.isArgument()) {
                print.printSetMethodBegin(type, field.getName());
                print.printFormat("host.{0}.setHost(value);", varyName);
                print.printMethodEnd();
            }
            print.printGetMethodBegin(fieldType, eibsName);
            print.printFormat("return host.{0}.getValue();", varyName);
            print.printMethodEnd();
            print.printSetMethodBegin(fieldType, eibsName);
            print.printFormat("host.{0}.setValue(value);", varyName);
            print.printMethodEnd();
        }
        for (Panel panel : (List<Panel>) module.getPanels()) {
            if (panel.isVisible() && !panel.isTransient()) {
                print.printGetMethodBegin("IPanel", panel.getName());
                print.printFormat("return host.{0};", varyName(panel.getName()));
                print.printMethodEnd();
            }
        }
        print.printClassEnd();
        print.close();
    }

    private static void compileProxyModuleList(ClassPrint print, Module module, String className) throws IOException {
        boolean flag = getHibernateFlag(module);
        String moduleClassName = typeName(module.getName());
        String eibsListTypeName = listTypeName(module.getName());
        print.printImport(IParent.class);
        print.printImport(IPanel.class);
        print.printImport(IModuleList.class);
        print.printImport(BigDecimal.class);
        print.printImport(Date.class);
        print.printImport(IStream.class);
        print.printImport(IDatafield.class);
        String exts = moduleClassName + "," + eibsListTypeName;
        print.printClassBegin(className, flag ? HibernateProxyModuleList.class : ProxyModuleList.class, exts);
        print.printFormat("public {0}(IParent parent)", className);
        print.printBegin();
        print.println("super(parent);");
        print.printEnd();
        for (Datafield field : (List<Datafield>) module.getDatafields()) {
            String varyName = varyName(field.getName());
            String typeName = typeName(varyName);
            String fieldType = typeName(field);
            String eibsName = eibsTypeName(field.getName());
            print.printGetMethodBegin(String.format("IDatafield<%s>", fieldType), field.getName());
            print.printFormat("return host.get{0}();", typeName);
            print.printMethodEnd();
            print.printGetMethodBegin(fieldType, eibsName);
            print.printFormat("return host.get{0}();", eibsName);
            print.printMethodEnd();
            print.printSetMethodBegin(fieldType, eibsName);
            print.printFormatEx("host.set{0}(value);", eibsName);
            print.printMethodEnd();
        }
        for (ModuleRef child : (List<ModuleRef>) module.getModules()) {
            String typeName = child.isList() ? listTypeName(child.getType()) : typeName(child.getType());
            print.printGetMethodBegin(typeName, child.getName());
            if (child.isArgument()) print.printFormat("return host.get{0}().getHost();", typeName(child.getName())); else print.printFormat("return host.get{0}();", typeName(child.getName()));
            print.printMethodEnd();
        }
        for (Panel panel : (List<Panel>) module.getPanels()) {
            if (panel.isVisible() && !panel.isTransient()) {
                print.printGetMethodBegin("IPanel", panel.getName());
                print.printFormat("return host.get{0}();", typeName(panel.getName()));
                print.printMethodEnd();
            }
        }
        print.printClassEnd();
        print.close();
    }

    private static void compileEibsList(ClassPrint print, Module module, String className) throws IOException {
        boolean flag = getHibernateFlag(module);
        String moduleClassName = typeName(module.getName());
        String eibsTypeName = eibsTypeName(moduleClassName);
        print.printImport(IParent.class);
        print.printImport(IPanel.class);
        print.printImport(IModuleRule.class);
        print.printImport(List.class);
        print.printImport(ArrayList.class);
        print.printImport(Event.class);
        print.printImport(IResult.class);
        print.printImport(BigDecimal.class);
        print.printImport(Date.class);
        print.printImport(IStream.class);
        print.printImport(IDatafield.class);
        print.printClassBegin(className, flag ? HibernateModuleList.class : AbstractModuleList.class, moduleClassName);
        print.printFormat("public {0}(IParent parent,int pageSize)", className);
        print.printBegin();
        print.println("super(parent,pageSize);");
        print.printEnd();
        print.println("@Override");
        print.printFormat("public {0} add(IResult... results)", moduleClassName);
        print.printBegin();
        print.printFormat("{0} module=new {0}(this);", moduleClassName);
        print.printFormat("add(module);", className);
        print.println("module.setValues(results);");
        print.println("return module;");
        print.printMethodEnd();
        print.printMethodBegin("Override", "getModuleClass", Class.class);
        print.printFormat("return {0}.class;", moduleClassName);
        print.printMethodEnd();
        if (flag) {
            print.printMethodBegin("Override", "getHibernateClass", Class.class);
            print.printFormat("return {0}.class;", eibsTypeName);
            print.printMethodEnd();
            print.printMethodBegin("Override", "setHibernateList", null, List.class, "list");
            print.println("setSize(list.size());");
            print.println("for(int i=0;i<list.size();i++)");
            print.printlnEx("get(i).setHibernateObject(list.get(i),true);");
            print.printMethodEnd();
        }
        String listRuleClassName = String.format("IModuleRule<%s>", moduleClassName);
        print.printFormat("private transient List<{0}> rules=new ArrayList<{0}>();", listRuleClassName);
        print.printMethodBegin(new String[] {}, "protected", "addRule", null, new String[] { listRuleClassName }, new String[] { "rule" });
        print.println("rules.add(rule);");
        print.printMethodEnd();
        print.printMethodBegin("Override", "add", boolean.class, moduleClassName, "module");
        print.printFormat("for({0} rule:rules)", listRuleClassName);
        print.printlnEx("rule.invoke(module);");
        print.println("return super.add(module);");
        print.printMethodEnd();
        for (Datafield field : (List<Datafield>) module.getDatafields()) {
            String varyName = varyName(field.getName());
            String fieldType = typeName(field);
            String eibsName = eibsTypeName(field.getName());
            print.printGetMethodBegin(String.format("IDatafield<%s>", fieldType), field.getName());
            print.printFormat("return module==null?null:module.{0};", varyName);
            print.printMethodEnd();
            print.printGetMethodBegin(fieldType, eibsName);
            print.printFormat("return module==null?null:module.{0}.getValue();", varyName);
            print.printMethodEnd();
            print.printSetMethodBegin(fieldType, eibsName);
            print.println("if(module!=null)");
            print.printFormatEx("module.{0}.setValue(value);", varyName);
            print.printMethodEnd();
        }
        for (ModuleRef child : (List<ModuleRef>) module.getModules()) {
            String typeName = child.isList() ? listTypeName(child.getType()) : typeName(child.getType());
            print.printGetMethodBegin(typeName, child.getName());
            if (child.isArgument()) print.printFormat("return module==null?null:moudle.{0}.getHost();", varyName(child.getName())); else print.printFormat("return module==null?null:moudle.{0};", varyName(child.getName()));
            print.printMethodEnd();
        }
        for (Panel panel : (List<Panel>) module.getPanels()) {
            if (panel.isVisible() && !panel.isTransient()) {
                print.printGetMethodBegin("IPanel", panel.getName());
                print.printFormat("return module==null?null:module.{0};", varyName(panel.getName()));
                print.printMethodEnd();
            }
        }
        print.printClassEnd();
        print.close();
    }

    private static void compileModule(HibernateCompileContext ctx, Module module, boolean root) throws IOException, CompileException, CheckerException {
        String className = module.getName();
        boolean flag = getHibernateFlag(module, root);
        if (IModuleManager.MODULE_OBJECT.equalsIgnoreCase(className) || ctx.doneList.contains(className)) return;
        ctx.doneList.add(className);
        ctx.manager.check(module);
        ClassPrint print = ctx.createPrint(className);
        print.printStaticImport(Constants.class);
        print.printImport(Log.class);
        print.printImport(LogFactory.class);
        print.printImport(List.class);
        print.printImport(ArrayList.class);
        print.printImport(Map.class);
        print.printImport(HashMap.class);
        print.printImport(Pattern.class);
        print.printImport(Matcher.class);
        print.printImport(Date.class);
        print.printImport(BigDecimal.class);
        print.printImport(IStream.class);
        print.printImport(StreamImpl.class);
        print.printImport(Serializable.class);
        print.printImport(LockInfo.class);
        print.printImport(IBaseObject.class);
        print.printImport(IDatafield.class);
        print.printImport(DatafieldImpl.class);
        print.printImport(ConstantDatafield.class);
        print.printImport(ProxyDatafield.class);
        print.printImport(IParent.class);
        print.printImport(ProxyModule.class);
        print.printImport(ProxyModuleList.class);
        print.printImport(IModule.class);
        print.printImport(IModuleList.class);
        print.printImport(IPanel.class);
        print.printImport(PanelImpl.class);
        print.printImport(IRule.class);
        print.printImport(Event.class);
        print.printImport(org.sss.common.model.EventType.class);
        print.printImport(IEventRule.class);
        print.printImport(IModuleRule.class);
        print.printImport(CodetableItem.class);
        print.printImport(IContext.class);
        print.printImport(I18nImpl.class);
        print.printImport(ICallback.class);
        print.printImport(IState.class);
        print.printImport(IResult.class);
        print.printImport(org.sss.common.model.Argument.class);
        print.printImport(ContainerUtils.class);
        print.printImport(RuleUtils.class);
        print.printClassBegin(className, root ? HibernateModuleRoot.class : flag ? HibernateModule.class : AbstractModule.class);
        print.printFormat("static final Log log = LogFactory.getLog({0}.class);", typeName(className));
        String format;
        for (ModuleRef child : (List<ModuleRef>) module.getModules()) {
            if (!child.isArgument()) {
                if (child.isList()) format = "{0}final protected {1} {2} = new {1}(this,{3});"; else format = "{0}final protected {1} {2} = new {1}(this);";
            } else format = "{0}final protected {1} {2} = new {1}(this);";
            String typeName = typeName(child.getType());
            String argument = "";
            if (child.isList()) {
                typeName = listTypeName(typeName);
                argument = String.valueOf(child.getPageSize());
                if (!ctx.doneList.contains(typeName)) {
                    ctx.doneList.add(typeName);
                    compileEibsList(ctx.createPrint(typeName), ctx.manager.getModule(child), typeName);
                }
            }
            if (child.isArgument()) {
                typeName = proxyTypeName(typeName);
                if (!IModuleManager.MODULE_OBJECT.equalsIgnoreCase(child.getType()) && !ctx.doneList.contains(typeName)) {
                    ctx.doneList.add(typeName);
                    if (child.isList()) compileProxyModuleList(ctx.createPrint(typeName), ctx.manager.getModule(child), typeName); else compileProxyModule(ctx.createPrint(typeName), ctx.manager.getModule(child), typeName);
                }
            }
            print.printFormat(format, "", typeName, varyName(child.getName()), argument);
        }
        for (Datafield field : (List<Datafield>) module.getDatafields()) {
            if (!field.isArgument()) {
                String typeName = typeName(field);
                if ("IStream".equals(typeName)) format = "final protected IDatafield<{0}> {1} = new DatafieldImpl<{0}>(this,IStream.class,new StreamImpl());"; else if ("BigDecimal".equals(typeName)) format = "final protected IDatafield<{0}> {1} = new DatafieldImpl<{0}>(this,BigDecimal.class,IDatafield.BZERO);"; else if ("Integer".equals(typeName)) format = "final protected IDatafield<{0}> {1} = new DatafieldImpl<{0}>(this,Integer.class,IDatafield.ZERO);"; else format = "final protected IDatafield<{0}> {1} = new DatafieldImpl<{0}>(this,{0}.class,null);";
            } else format = "final protected ProxyDatafield<{0}> {1}=new ProxyDatafield<{0}>(this);";
            print.printFormat(format, typeName(field), varyName(field.getName()));
        }
        for (Panel panel : (List<Panel>) module.getPanels()) {
            if (panel.isVisible() && !panel.isTransient()) print.printFormat("final transient protected IPanel {0} = new PanelImpl(this,\"{0}\");", varyName(panel.getName()));
        }
        print.println();
        if (root) {
            print.printFormat("public {0}(HibernateModuleRoot oldEntity,String name,boolean newFlag)", typeName(className));
            print.printBegin();
            print.println("super(oldEntity,name,newFlag);");
            print.println("addChild();");
            print.printMethodEnd();
        } else {
            print.printFormat("public {0}(IParent parent)", typeName(className));
            print.printBegin();
            print.println("super(parent);");
            print.printMethodEnd();
        }
        String eibsTypeName = eibsTypeName(className);
        if (flag) {
            ctx.addMappingContent(eibsTypeName);
            compileEibsModule(ctx.createPrint(eibsTypeName), module, eibsTypeName);
            print.printFormat("protected transient {0} hibernateObject;", eibsTypeName);
            print.printMethodBegin("Override", "setHibernateObject", null, new Class[] { Object.class, boolean.class }, new String[] { "object", "update" });
            print.printFormat("hibernateObject=({0})object;", eibsTypeName);
            print.println("if(update&&object!=null)");
            print.printBegin();
            for (Datafield field : (List<Datafield>) module.getDatafields()) print.printFormat("{0}.setValue(hibernateObject.{0});", field.getName());
            print.printEnd();
            print.printMethodEnd();
            print.printMethodBegin("Override", "getHibernateClass", Class.class);
            print.printFormat("return {0}.class;", eibsTypeName);
            print.printMethodEnd();
            print.printMethodBegin("Override", "getHibernateObject", Object.class);
            print.println("if(hibernateObject!=null)");
            print.printBegin();
            for (Datafield field : (List<Datafield>) module.getDatafields()) print.printFormat("hibernateObject.{0}={0}.getValue();", field.getName());
            print.printEnd();
            print.println("return hibernateObject;");
            print.printMethodEnd();
            print.printMethodBegin("Override", "newHibernateObject", null);
            print.printFormat("hibernateObject=new {0}();", eibsTypeName);
            print.printMethodEnd();
            print.printMethodBegin("Override", "getIdentifier", Serializable.class);
            print.println("return inr.getValue();");
            print.printMethodEnd();
        }
        print.printMethodBegin("Override", "copyValue", null, IModule.class, "module");
        if (module.getDatafields().size() > 0) print.printFormat("{0} eibsObject=({0})module;", typeName(module.getName()));
        for (Datafield field : (List<Datafield>) module.getDatafields()) print.printFormat("{0}.setValue(eibsObject.{0}.getValue());", field.getName());
        if (flag) print.print("hibernateObject=eibsObject.hibernateObject;");
        print.printMethodEnd();
        print.printMethodBegin("Override", "copyValues", null, IParent.class, "parent");
        print.println("copyValue((IModule)parent);");
        if (module.getModules().size() > 0) print.printFormat("{0} eibsObject=({0})parent;", typeName(module.getName()));
        for (ModuleRef moduleRef : (List<ModuleRef>) module.getModules()) print.printFormat("{0}.copyValues(eibsObject.{0});", moduleRef.getName());
        print.printMethodEnd();
        print.printMethodBegin("Override", "clear", null);
        for (Datafield field : (List<Datafield>) module.getDatafields()) print.printFormat("{0}.clear();", field.getName());
        for (ModuleRef moduleRef : (List<ModuleRef>) module.getModules()) print.printFormat("{0}.clear();", moduleRef.getName());
        print.printMethodEnd();
        print.printMethodBegin("Override", "clone", IModule.class);
        if (root) print.println("throw new UnsupportedOperationException();"); else {
            print.printFormat("{0} module=new {0}(null);", typeName(className));
            print.println("module.copyValues(this);");
            print.println("return module;");
        }
        print.printMethodEnd();
        print.printMethodBegin("Override", "addChild", null);
        for (ModuleRef child : (List<ModuleRef>) module.getModules()) {
            print.printFormat("put(\"{0}\",{0});", varyName(child.getName()));
            if (!child.isArgument()) {
                for (Argument argument : (List<Argument>) child.getArguments()) print.printSetArgument(ctx, child, argument);
                if (child.isList()) for (int i = 0; i < child.getInitSize(); i++) print.printFormat("{0}.add(new {1}({0}));", varyName(child.getName()), typeName(child.getType()));
                print.printFormat("{0}.addChild();", varyName(child.getName()));
            }
        }
        for (Datafield field : (List<Datafield>) module.getDatafields()) print.printFormat("put(\"{0}\",{0});", varyName(field.getName()));
        if (root) print.println("super.addChild();");
        print.printMethodEnd();
        print.printMethodBegin("Override", "init", null, IContext.class, "ctx");
        for (Rule rule : (List<Rule>) module.getDefaultRules()) for (String path : dependPaths(ctx.manager, module, rule)) printRule(ctx, module, print, path, rule);
        for (Rule rule : (List<Rule>) module.getEventRules()) printRule(ctx, module, print, rule.getName(), rule);
        for (Rule rule : (List<Rule>) module.getCheckRules()) printRule(ctx, module, print, rule.getName(), rule);
        for (Rule rule : (List<Rule>) module.getInitRules()) printRule(ctx, module, print, rule.getName(), rule);
        print.println("super.init(ctx);");
        print.printMethodEnd();
        for (ModuleRef child : (List<ModuleRef>) module.getModules()) {
            String typeName = child.isList() ? listTypeName(child.getType()) : typeName(child.getType());
            print.printGetMethodBegin(typeName, child.getName());
            if (child.isArgument()) {
                print.printFormat("return {0}.getHost();", varyName(child.getName()));
                print.printMethodEnd();
                print.printSetMethodBegin(typeName, child.getName());
                print.printFormat("{0}.setHost(value);", varyName(child.getName()));
            } else print.printFormat("return {0};", varyName(child.getName()));
            print.printMethodEnd();
        }
        for (Datafield field : (List<Datafield>) module.getDatafields()) {
            String varyName = varyName(field.getName());
            String fieldType = typeName(field);
            String eibsName = eibsTypeName(field.getName());
            String type = String.format("IDatafield<%s>", typeName(field));
            print.printGetMethodBegin(type, field.getName());
            print.printFormat("return {0};", varyName);
            print.printMethodEnd();
            if (field.isArgument()) {
                print.printSetMethodBegin(type, field.getName());
                print.printFormat("{0}.setHost(value);", varyName);
                print.printMethodEnd();
            }
            print.printGetMethodBegin(fieldType, eibsName);
            print.printFormat("return {0}.getValue();", varyName);
            print.printMethodEnd();
            print.printSetMethodBegin(fieldType, eibsName);
            print.printFormat("{0}.setValue(value);", varyName);
            print.printMethodEnd();
        }
        for (Panel panel : (List<Panel>) module.getPanels()) {
            if (panel.isVisible() && !panel.isTransient()) {
                print.printGetMethodBegin("IPanel", panel.getName());
                print.printFormat("return {0};", varyName(panel.getName()));
                print.printMethodEnd();
            }
        }
        compileRuleEx(ctx, print, module, module.getInitRules(), root);
        compileRuleEx(ctx, print, module, module.getDefaultRules(), root);
        compileRuleEx(ctx, print, module, module.getCheckRules(), root);
        compileRuleEx(ctx, print, module, module.getEventRules(), root);
        compileRule(ctx, print, module, module.getGlobalMethods(), false);
        compileRule(ctx, print, module, module.getLocalMethods(), root);
        print.printClassEnd();
        print.close();
        for (ModuleRef child : (List<ModuleRef>) module.getModules()) compileModule(ctx, ctx.manager.getModule(child.getType()), false);
    }

    private static final HashMap<RuleType, String> ruleTypes = new HashMap<RuleType, String>();

    private static final HashMap<EventType, String> eventTypes = new HashMap<EventType, String>();

    static {
        ruleTypes.put(RuleType.INIT_LITERAL, "DefaultRule");
        ruleTypes.put(RuleType.DEFAULT_LITERAL, "DefaultRule");
        ruleTypes.put(RuleType.CHECK_LITERAL, "CheckRule");
        ruleTypes.put(RuleType.EVENT_LITERAL, "EventRule");
        eventTypes.put(EventType.ON_EVENT_LITERAL, "DEFAULT");
        eventTypes.put(EventType.ON_CHANGE_LITERAL, "ON_CHANGE");
        eventTypes.put(EventType.ON_CLICK_LITERAL, "ON_CLICK");
        eventTypes.put(EventType.ON_DBL_CLICK_LITERAL, "ON_DBLCLICK");
        eventTypes.put(EventType.ON_ROW_INSERT_LITERAL, "ON_ROW_INSERT");
        eventTypes.put(EventType.ON_ROW_REMOVE_LITERAL, "ON_ROW_REMOVE");
        eventTypes.put(EventType.ON_STREAM_DOWNLOAD_LITERAL, "ON_STREAM_DOWNLOAD");
        eventTypes.put(EventType.ON_STREAM_UPLOAD_LITERAL, "ON_STREAM_UPLOAD");
        eventTypes.put(EventType.ON_PANEL_SHOW_LITERAL, "ON_PANEL_SHOW");
        eventTypes.put(EventType.ON_PANEL_CLOSE_LITERAL, "ON_PANEL_CLOSE");
    }

    private static void printRule(HibernateCompileContext ctx, Module module, ClassPrint print, String rulePath, Rule rule) {
        HibernateEntityCompile.module = module;
        HibernateEntityCompile.rule = rule;
        int index = rulePath.indexOf('[');
        IModuleManager manager = ctx.manager;
        if (index > 0) {
            String subPath = rulePath.substring(rulePath.indexOf("]") + 2);
            rulePath = rulePath.substring(0, index);
            ModuleRef moduleRef = (ModuleRef) ctx.manager.getEibsObject(module, rulePath);
            print.printFormat("{0}.addRule(new IModuleRule<{1}>(){2}", fieldName(manager, rulePath), typeName(moduleRef.getType()), "{");
            print.incIndex();
            print.printFormat("public void invoke({0} module)", typeName(moduleRef.getType()));
            print.printBegin();
            if (!ContainerUtils.isEmpty(subPath)) print.printFormat("module.add{1}({2});", ruleTypes.get(rule.getType()), ruleName(rule)); else print.printFormat("{0}.add{1}({2});", fieldName(manager, "module", subPath), ruleTypes.get(rule.getType()), ruleName(rule));
            print.printEnd();
            print.decIndex();
            print.println("});");
        } else print.printFormat("{0}.add{1}({2});", fieldName(manager, rulePath), ruleTypes.get(rule.getType()), ruleName(rule));
    }

    private static boolean getHibernateFlag(Module module) {
        return getHibernateFlag(module, false);
    }

    private static boolean getHibernateFlag(Module module, boolean root) {
        return module.getModules().size() == 0 && module.getDatafields().size() > 0 && !module.isTransient() && !root;
    }

    private static void compileRule(HibernateCompileContext ctx, ClassPrint print, Module module, List<Rule> rules, boolean root) throws FileNotFoundException, IOException, CheckerException {
        ClassPrint _print = null;
        for (Rule rule : rules) {
            if (_print == null) _print = RuleType.GLOBAL_LITERAL.equals(rule.getType()) ? ctx.gPrint : print;
            String codeText = ModulePathUtil.getCodeText(ctx.manager, module, rule);
            if (ModuleConstant.ignoreWarning) _print.println("@SuppressWarnings(value = \"unchecked\")");
            _print.println(MessageFormat.format("public final {0}", codeText.substring(0, codeText.indexOf(")") + 1)));
            _print.printBegin();
            _print.println("IContext ctx=RuleUtils.ctx.get();");
            compileRuleText(ctx, _print, module, rule, codeText, root, false);
            _print.printEnd();
        }
    }

    private static void compileRuleEx(HibernateCompileContext ctx, ClassPrint print, Module module, List<Rule> rules, boolean root) throws FileNotFoundException, IOException, CheckerException {
        for (Rule rule : rules) {
            boolean flag = RuleType.EVENT_LITERAL.equals(rule.getType());
            print.println(MessageFormat.format("private final transient {0} {1}=new {0}()", flag ? "IEventRule" : "IRule", ruleName(rule)));
            print.printBegin();
            print.printMethodBegin("getOrder", int.class);
            print.println(String.format("return %d;", rule.getOrder()));
            print.printMethodEnd();
            if (flag) {
                print.printMethodBegin("getType", EventType.class);
                String eventType = eventTypes.get(rule.getEventType());
                print.println(String.format("return EventType.%s;", eventType == null ? "DEFAULT" : eventType));
                print.printMethodEnd();
            }
            if (ModuleConstant.ignoreWarning) print.println("@SuppressWarnings(value = \"unchecked\")");
            if (flag) print.printMethodBegin("invoke", boolean.class, new Class[] { IContext.class, Event.class }, new String[] { "ctx", "evt" }); else print.printMethodBegin("invoke", boolean.class, IContext.class, "ctx");
            String codeText = ModulePathUtil.getCodeText(ctx.manager, module, rule);
            compileRuleText(ctx, print, module, rule, codeText, root, true);
            print.println("return true;");
            print.printMethodEnd();
            print.printEndEx();
        }
    }

    private static List<String> dependPaths(IModuleManager manager, Module module, Rule rule) {
        String codeText = ModulePathUtil.getCodeText(manager, module, rule);
        int beginIndex = codeText.indexOf("{");
        int endIndex = codeText.lastIndexOf("}");
        codeText = codeText.substring(beginIndex + 1, endIndex);
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\$[\\w\\\\\\[\\]]*");
        Matcher matcher = pattern.matcher(codeText);
        while (matcher.find()) {
            String path = codeText.substring(matcher.start() + 1, matcher.end());
            if (!rule.getName().equals(path) && !list.contains(path)) list.add(path);
        }
        return list;
    }

    private static void compileRuleText(HibernateCompileContext ctx, ClassPrint print, Module module, Rule rule, String codeText, boolean root, boolean replaceReturn) throws FileNotFoundException, IOException, CheckerException {
        HibernateEntityCompile.module = module;
        HibernateEntityCompile.rule = rule;
        int beginIndex = codeText.indexOf("{");
        int endIndex = codeText.lastIndexOf("}");
        codeText = codeText.substring(beginIndex + 1, endIndex);
        if (replaceReturn) codeText = codeText.replaceAll("return\\s*;", "return true;");
        Pattern pattern = Pattern.compile("\\s*(/\\*[^\\*/]*\\*/|//[^\r\n]*[\r\n])");
        Matcher matcher = pattern.matcher(codeText);
        int offset = 0;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(codeText.substring(offset, matcher.start()));
            offset = matcher.end();
        }
        sb.append(codeText.substring(offset));
        codeText = sb.toString();
        pattern = Pattern.compile("#CT[0-9]{6}");
        matcher = pattern.matcher(codeText);
        offset = 0;
        sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(codeText.substring(offset, matcher.start()));
            sb.append("Platform.getI18nString(\"").append(module.getName()).append("\",\"").append(codeText.substring(matcher.start() + 1, matcher.end())).append("\")");
            offset = matcher.end();
        }
        sb.append(codeText.substring(offset));
        codeText = sb.toString();
        CodetextUtils.checkEx(ctx.manager, module, codeText, rule);
        pattern = Pattern.compile("new ICallback\\([\\w]*\\)\\.invoke\\(\\)");
        matcher = pattern.matcher(codeText);
        offset = 0;
        sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(codeText.substring(offset, matcher.start()));
            sb.append("new ICallback(){public Object invoke(Object... arguments){return ").append(codeText.substring(matcher.start() + 14, matcher.end() - 10)).append("(arguments);}}");
            offset = matcher.end();
        }
        sb.append(codeText.substring(offset));
        codeText = sb.toString();
        pattern = Pattern.compile("Platform.exitEvent\\([\\s]*\\)");
        matcher = pattern.matcher(codeText);
        offset = 0;
        sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(codeText.substring(offset, matcher.start()));
            sb.append("return false");
            offset = matcher.end();
        }
        sb.append(codeText.substring(offset));
        codeText = sb.toString();
        pattern = Pattern.compile("Platform.[\\w]*\\(");
        matcher = pattern.matcher(codeText);
        offset = 0;
        sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(codeText.substring(offset, matcher.start()));
            convertMethodName(sb, codeText.substring(matcher.start(), matcher.end()), root);
            offset = matcher.end();
        }
        sb.append(codeText.substring(offset));
        codeText = sb.toString();
        pattern = Pattern.compile("\\$[\\w\\\\\\[\\]]*\\s*=[^\\;]*\\;");
        matcher = pattern.matcher(codeText);
        offset = 0;
        sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(codeText.substring(offset, matcher.start()));
            String body = codeText.substring(matcher.start() + 1, matcher.end());
            String[] values = body.split("=");
            String path = values[0].trim();
            convertPath(ctx.manager, module, sb, path + ".setValue(");
            String dataType = typeName((Datafield) ctx.manager.getEibsObject(module, path));
            convertExpression(ctx.manager, module, sb, values[1].substring(0, values[1].length() - 1), dataType);
            sb.append(");");
            offset = matcher.end();
        }
        sb.append(codeText.substring(offset));
        codeText = sb.toString();
        pattern = Pattern.compile("\\$[\\w\\\\\\[\\]]*(\\.[s|g]etValue\\()?");
        matcher = pattern.matcher(codeText);
        offset = 0;
        sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(codeText.substring(offset, matcher.start()));
            convertPath(ctx.manager, module, sb, codeText.substring(matcher.start() + 1, matcher.end()));
            offset = matcher.end();
        }
        sb.append(codeText.substring(offset));
        codeText = sb.toString();
        codeText = codeText.replaceFirst("^\\s*", "");
        boolean flag = true;
        for (String line : codeText.split("[\r\n]+")) {
            print.println(line);
            if (flag) {
                print.decIndex();
                flag = false;
            }
        }
        print.incIndex();
    }
}

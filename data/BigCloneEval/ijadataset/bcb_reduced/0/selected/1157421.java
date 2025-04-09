package net.sourceforge.ondex.xten.workflow.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JOptionPane;
import net.sourceforge.ondex.AbstractONDEXPlugin;
import net.sourceforge.ondex.export.AbstractONDEXExport;
import net.sourceforge.ondex.export.ExportArguments;
import net.sourceforge.ondex.filter.AbstractONDEXFilter;
import net.sourceforge.ondex.filter.FilterArguments;
import net.sourceforge.ondex.mapping.AbstractONDEXMapping;
import net.sourceforge.ondex.mapping.MappingArguments;
import net.sourceforge.ondex.parser.AbstractONDEXParser;
import net.sourceforge.ondex.parser.ParserArguments;
import net.sourceforge.ondex.statistics.AbstractONDEXStatistics;
import net.sourceforge.ondex.statistics.StatisticsArguments;
import net.sourceforge.ondex.transformer.AbstractONDEXTransformer;
import net.sourceforge.ondex.transformer.TransformerArguments;
import net.sourceforge.ondex.workflow.args.ArgumentDefinition;
import net.sourceforge.ondex.workflow.args.BooleanArgumentDefinition;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author lysenkoa
 *
 */
@SuppressWarnings("unchecked")
public class ConfigUpdater {

    private ConfigUpdater() {
    }

    public static void updateConfig(String configFile, Map<String, List<ListItem>> sortedLists, Map<String, ComponentTemplate> internalToTemplate) {
        SAXBuilder builder = new SAXBuilder();
        File toUpdate = new File(configFile);
        try {
            Document doc = builder.build(toUpdate);
            Element root = doc.getRootElement();
            Map<String, Element> internalToEl = new HashMap<String, Element>();
            Map<String, Map<String, Element>> argNameToEl = new HashMap<String, Map<String, Element>>();
            Map<String, Element> inputDefs = new HashMap<String, Element>();
            Element componentDef = root.getChild("ComponentDefinitions");
            for (Object o : componentDef.getChildren()) {
                Element e = (Element) o;
                internalToEl.put(e.getAttributeValue("id"), e);
                Map<String, Element> args = new HashMap<String, Element>();
                inputDefs.put(e.getAttributeValue("id"), e.getChild("InputDefinitions"));
                for (Object o1 : e.getChild("InputDefinitions").getChildren()) {
                    Element e1 = (Element) o1;
                    String intName = e1.getChildText("InternalName");
                    if (intName != null && !intName.equals("") && !e1.getAttributeValue("typeParser").equals("standart") && !intName.equals("datadir") && !intName.equals("importfile") && !intName.equals("exportfile")) {
                        args.put(intName, e1);
                    }
                }
                argNameToEl.put(e.getAttributeValue("id"), args);
            }
            for (Entry<String, List<ListItem>> ent : sortedLists.entrySet()) {
                List<ListItem> toRemove = new LinkedList<ListItem>();
                if (!ent.getKey().equals("Function")) {
                    for (ListItem item : ent.getValue()) {
                        Map<String, Element> current = argNameToEl.get(item.getInternalName());
                        ComponentTemplate ct = internalToTemplate.get(item.getInternalName());
                        AbstractONDEXPlugin p = getPlugin(ct.getCls());
                        if (p == null) {
                            internalToEl.get(item.getInternalName()).detach();
                            internalToEl.remove(item.getInternalName());
                            toRemove.add(item);
                            System.out.println("Configuration changed - " + ct.getCls() + " is no longer avalalbe.");
                            continue;
                        }
                        Map<String, Object[]> defs = getDefinitions(p);
                        for (Entry<String, Object[]> def : defs.entrySet()) {
                            Element newEl = getNewElement((ArgumentDefinition) def.getValue()[1], (String) def.getValue()[0]);
                            if (!current.containsKey(def.getKey())) {
                                inputDefs.get(item.getInternalName()).addContent(newEl);
                                System.out.println("Configuration changed - new option " + def.getKey() + " added to " + item.toString() + ".");
                            } else {
                                Element argDef = current.get(def.getKey());
                                argDef.getChild("Required").setText(newEl.getChildText("Required"));
                                if (newEl.getChild("DefaultValue") != null) {
                                    Element defaultVal = argDef.getChild("DefaultValue");
                                    if (defaultVal == null) {
                                        defaultVal = new Element("DefaultValue");
                                        argDef.addContent(defaultVal);
                                    }
                                    defaultVal.setText(newEl.getChildText("DefaultValue"));
                                }
                                String type = newEl.getChild("Visualisation").getChildText("Type");
                                if (!argDef.getChild("Visualisation").getChildText("Type").equals(type)) {
                                    argDef.getChild("Visualisation").getChild("Type").setText(type);
                                    argDef.getChild("Visualisation").getChild("ContentHint").setText(newEl.getChild("Visualisation").getChildText("ContentHint"));
                                }
                            }
                        }
                        for (Entry<String, Element> els : current.entrySet()) {
                            if (!defs.containsKey(els.getKey())) {
                                els.getValue().detach();
                                System.out.println("Configuration changed - option " + els.getKey() + " in " + item.toString() + " is no longer available.");
                            }
                        }
                    }
                    for (ListItem item : toRemove) ent.getValue().remove(item);
                }
            }
            Element cl = new Element("ComponentList");
            for (Entry<String, List<ListItem>> ent : sortedLists.entrySet()) {
                Element sublist = new Element("Sublist");
                sublist.setAttribute("name", firstUpper(ent.getKey()));
                for (ListItem item : ent.getValue()) {
                    Element one = new Element("Component");
                    String id = item.getInternalName();
                    String name = item.toString();
                    one.setAttribute("id", id);
                    one.setAttribute("name", name);
                    sublist.addContent(one);
                }
                cl.addContent(sublist);
            }
            root.getChild("ComponentList").detach();
            Element cd = root.getChild("ComponentDefinitions");
            cd.detach();
            root.addContent(cl);
            root.addContent(cd);
            Format f = Format.getPrettyFormat();
            f.setTextMode(Format.TextMode.PRESERVE);
            XMLOutputter outputter = new XMLOutputter(f);
            toUpdate.delete();
            safeCreateNewFile(toUpdate);
            try {
                FileOutputStream fos = new FileOutputStream(toUpdate);
                outputter.output(doc, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void safeCreateNewFile(File toUpdate) {
        boolean stateSuccess = false;
        int counter = 0;
        while (stateSuccess == false) {
            try {
                toUpdate.createNewFile();
                stateSuccess = true;
            } catch (IOException e) {
                if (counter > 30) {
                    stateSuccess = true;
                    toUpdate = new File(toUpdate.getAbsolutePath() + ".bak");
                    try {
                        toUpdate.createNewFile();
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Could not update configuration: Access to file was denied. Recovery copy saved as " + toUpdate.getName() + ".", "Error", JOptionPane.ERROR_MESSAGE);
                        e1.printStackTrace();
                    }
                    break;
                }
                counter++;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    public static Element constructPluginInputs(AbstractONDEXPlugin p) {
        Element idef = new Element("InputDefinitions");
        Map<String, Object[]> defs = getDefinitions(p);
        for (Entry<String, Object[]> def : defs.entrySet()) {
            Element newEl = getNewElement((ArgumentDefinition) def.getValue()[1], (String) def.getValue()[0]);
            idef.addContent(newEl);
        }
        return idef;
    }

    public static void addDefaultPluginInputs(AbstractONDEXPlugin p, Element idef) {
        Element arg = makeInputArgDef("1", "standartArgument", null, "", " net.sourceforge.ondex.core.security.Session", "", "\\session", "true", "", null, "handle", "true");
        idef.addContent(arg);
        arg = makeInputArgDef("2", "standartArgument", null, "Input graph", "net.sourceforge.ondex.core.AbstractONDEXGraph", "Graph that will be operated on by this plugin.", null, "true", "field", null, "handle", "false");
        idef.addContent(arg);
        if (AbstractONDEXFilter.class.isAssignableFrom(p.getClass())) {
            arg = makeInputArgDef("3", "standartArgument", null, "Optional output graph", "net.sourceforge.ondex.core.AbstractONDEXGraph", "Use this graph for the output, leave the original unmodified.", null, "false", "field", null, "handle", "false");
            idef.addContent(arg);
        } else if (AbstractONDEXParser.class.isAssignableFrom(p.getClass())) {
            arg = makeInputArgDef("0", "pluginArgument", "datadir", "Data folder", "java.lang.String", "Folder with data to import.", null, "true", "field", null, "browse_folder", "false");
            idef.addContent(arg);
            arg = makeInputArgDef("0", "pluginArgument", "importfile", "Input File", "java.lang.String", "Name and location of the output file. Either dir or file are compulsory.", null, "false", "field", null, "browse_file", "false");
            idef.addContent(arg);
        } else if (AbstractONDEXExport.class.isAssignableFrom(p.getClass())) {
            arg = makeInputArgDef("0", "pluginArgument", "exportfile", "Output File", "java.lang.String", "Name and location of the output file.", null, "true", "field", null, "browse_file", "false");
            idef.addContent(arg);
        }
    }

    public static Element constructPluginOutputs(AbstractONDEXPlugin p) {
        Element outputdef = new Element("OutputDefinitions");
        if (AbstractONDEXFilter.class.isAssignableFrom(p.getClass()) || AbstractONDEXMapping.class.isAssignableFrom(p.getClass()) || AbstractONDEXParser.class.isAssignableFrom(p.getClass()) || AbstractONDEXTransformer.class.isAssignableFrom(p.getClass())) {
            Element arg = makeOutputArgDef("0", "standart", "Output graph", "net.sourceforge.ondex.core.AbstractONDEXGraph", "Output graph modifed by the plugin", "field", null, "handle");
            outputdef.addContent(arg);
        }
        return outputdef;
    }

    private static Element getNewElement(ArgumentDefinition<?> ad, String argType) {
        String guitype;
        if (ad.isAllowedMultipleInstances()) {
            guitype = "list";
        } else if (BooleanArgumentDefinition.class.isAssignableFrom(ad.getClass())) {
            guitype = "checkbox";
        } else {
            guitype = "field";
        }
        String defaultValue = null;
        if (ad.getDefaultValue() != null) defaultValue = ad.getDefaultValue().toString();
        return makeInputArgDef("0", "pluginArgument", ad.getName(), firstUpper(ad.getName()), argType, ad.getDescription(), defaultValue, String.valueOf(ad.isRequiredArgument()), guitype, null, null, "false");
    }

    private static Map<String, Object[]> getDefinitions(AbstractONDEXPlugin p) {
        Map<String, Object[]> defs = new HashMap<String, Object[]>();
        String argType = null;
        if (AbstractONDEXParser.class.isAssignableFrom(p.getClass())) {
            argType = ParserArguments.class.getCanonicalName();
        } else if (AbstractONDEXExport.class.isAssignableFrom(p.getClass())) {
            argType = ExportArguments.class.getCanonicalName();
        } else if (AbstractONDEXMapping.class.isAssignableFrom(p.getClass())) {
            argType = MappingArguments.class.getCanonicalName();
        } else if (AbstractONDEXStatistics.class.isAssignableFrom(p.getClass())) {
            argType = StatisticsArguments.class.getCanonicalName();
        } else if (AbstractONDEXTransformer.class.isAssignableFrom(p.getClass())) {
            argType = TransformerArguments.class.getCanonicalName();
        } else if (AbstractONDEXFilter.class.isAssignableFrom(p.getClass())) {
            argType = FilterArguments.class.getCanonicalName();
        }
        ArgumentDefinition<?>[] ads = p.getArgumentDefinitions();
        if (ads != null) {
            for (ArgumentDefinition<?> ad : ads) {
                defs.put(ad.getName(), new Object[] { argType, ad });
            }
        }
        return defs;
    }

    public static AbstractONDEXPlugin getPlugin(String str) {
        AbstractONDEXPlugin p = null;
        try {
            Class<?> cls = Class.forName(str);
            p = (AbstractONDEXPlugin) cls.getConstructor(new Class<?>[] {}).newInstance();
        } catch (Exception e) {
        }
        return p;
    }

    public static String firstUpper(String in) {
        return in.substring(0, 1).toUpperCase() + in.substring(1);
    }

    public static Element makeInputArgDef(String key, String parser, String tag, String name, String strClass, String desc, String defVal, String required, String guitype, String guisize, String guihint, String isHidden) {
        Element arg = new Element("ArgDef");
        arg.setAttribute("key", key);
        arg.setAttribute("internalUse", isHidden);
        arg.setAttribute("typeParser", parser);
        addElement(arg, "Name", name);
        if (tag != null) {
            addElement(arg, "InternalName", tag);
        }
        addElement(arg, "Class", strClass);
        addElement(arg, "Description", desc);
        if (defVal != null) {
            addElement(arg, "DefaultValue", defVal);
        }
        addElement(arg, "Required", required);
        Element gui = new Element("Visualisation");
        addElement(gui, "Type", guitype);
        addElement(gui, "Size", guisize);
        addElement(gui, "ContentHint", guihint);
        arg.addContent(gui);
        return arg;
    }

    public static Element makeOutputArgDef(String key, String parser, String name, String strClass, String desc, String guitype, String guisize, String guihint) {
        Element arg = new Element("ArgDef");
        arg.setAttribute("key", key);
        arg.setAttribute("typeParser", parser);
        addElement(arg, "Name", name);
        addElement(arg, "Class", strClass);
        addElement(arg, "Required", "true");
        addElement(arg, "Description", desc);
        Element gui = new Element("Visualisation");
        addElement(gui, "Type", guitype);
        addElement(gui, "Size", guisize);
        addElement(gui, "ContentHint", guihint);
        arg.addContent(gui);
        return arg;
    }

    public static Element makeComponentElment(String cls, String description, String name, String interanlName) {
        Element comp = new Element("Component");
        comp.setAttribute("id", interanlName);
        Element n = new Element("Name");
        n.setText(name);
        comp.addContent(n);
        Element definition = new Element("Definition");
        Element type = new Element("Type");
        type.setText("plugin");
        Element elCalss = new Element("Class");
        elCalss.setText(cls);
        definition.addContent(type);
        definition.addContent(elCalss);
        comp.addContent(definition);
        Element d = new Element("Description");
        if (description != null) {
            d.setText(description);
        }
        comp.addContent(d);
        return comp;
    }

    public static Element makeFunctionElment(String cls, String name, String method, String signature, String description, String returns) {
        String[] temp = cls.split("\\.");
        String id = temp[temp.length - 1];
        Element comp = new Element("Component");
        comp.setAttribute("id", id.toLowerCase());
        Element n = new Element("Name");
        n.setText(name);
        comp.addContent(n);
        Element definition = new Element("Definition");
        Element type = new Element("Type");
        type.setText("function");
        Element elCalss = new Element("Class");
        elCalss.setText(cls);
        Element elMeth = new Element("Method");
        elMeth.setText(method);
        Element elSig = new Element("MethodArgs");
        elSig.setText(signature);
        definition.addContent(type);
        definition.addContent(elCalss);
        definition.addContent(elMeth);
        definition.addContent(elSig);
        comp.addContent(definition);
        Element d = new Element("Description");
        if (description != null) {
            d.setText(description);
        }
        comp.addContent(d);
        Element in = new Element("InputDefinitions");
        comp.addContent(in);
        Element out = new Element("OutputDefinitions");
        comp.addContent(out);
        String[] argSigs = signature.split(", *?");
        for (int i = 0; i < argSigs.length; i++) {
            in.addContent(makeFunctionArg(argSigs[i], i));
        }
        if (returns != null) {
            String[] argOuts = returns.split(", *?");
            for (int i = 0; i < argOuts.length; i++) {
                out.addContent(makeFunctionArg(argOuts[i], i));
            }
        }
        return comp;
    }

    private static Element makeFunctionArg(String cls, int pos) {
        String strPos = ((Integer) pos).toString();
        return makeInputArgDef(strPos, "standart", "", "arg" + strPos, cls, "", "", "true", "field", "", "", "false");
    }

    public static String safeGetElementText(Element parent, String childName) {
        Element e = parent.getChild(childName);
        if (e == null) return null;
        return e.getText();
    }

    public static Element findElementWithAttVlaue(Element parent, String attrName, String attrValue) {
        List<Element> children = parent.getChildren();
        for (Element e : children) {
            if (e.getAttributeValue(attrName) != null && e.getAttributeValue(attrName).equals(attrValue)) {
                return e;
            }
        }
        return null;
    }

    public static Element makeListEntryElement(String id, String name) {
        Element comp = new Element("Component");
        comp.setAttribute("id", id);
        comp.setAttribute("name", name);
        return comp;
    }

    public static Element addElement(Element parent, String name, String text) {
        Element e = new Element(name);
        if (text != null) {
            e.setText(text);
        }
        parent.addContent(e);
        return e;
    }
}

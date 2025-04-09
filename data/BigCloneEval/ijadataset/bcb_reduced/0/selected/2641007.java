package org.vexi.widgetdoc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ibex.js.Constants;
import org.ibex.js.JS;
import org.ibex.js.JSExn;
import org.ibex.js.JSU;
import org.ibex.js.Parser;
import org.ibex.js.Parser.GlobalsChecker;
import org.ibex.util.Basket;
import org.ibex.util.Tree;
import org.ibex.util.Vec;
import freemarker.template.SimpleCollection;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 *  Encapsulates a template file => 1+ template applications
 */
public class TFile {

    static Set<String> staticGlobals = new HashSet<String>();

    static {
        staticGlobals.add("vexi");
        staticGlobals.add("static");
    }

    String name;

    public TElement templates;

    SortedSet<String> properties;

    public TFile(String name) {
        this.name = name;
        Package.add(this);
    }

    public String getName() {
        return name;
    }

    public SortedSet<String> getProperties() {
        properties = new TreeSet<String>();
        TElement t = templates;
        while (t != null) {
            properties.addAll(t.properties);
            t = t.preapply;
        }
        return properties;
    }

    public boolean hasProperty(String property) {
        return templates.hasProperty(property);
    }

    public static TemplateMethodModel templateLinkMethod(final String packagename, final boolean qualify) {
        return new TemplateMethodModel() {

            public Object exec(List arguments) throws TemplateModelException {
                String arg = "" + arguments.get(0);
                String fullName = (packagename == null ? "" : packagename + ".") + arg;
                String filename;
                if (fullName.equals("ui:box")) {
                    filename = "_ui_box.html";
                } else {
                    filename = Main.templateFilename(fullName);
                }
                String displayName = qualify ? fullName : Package.unqualify(fullName);
                return "<a href=\"../templates/" + filename + "\" target=\"infoFrame\" >" + displayName + "</a>";
            }
        };
    }

    Static staticPart;

    public void createStatic(Prefixes staticPrefixes) {
        staticPart = new Static(staticPrefixes);
    }

    public Static getStatic() {
        return staticPart;
    }

    public class Static {

        JS staticObject;

        Prefixes uriPrefixes;

        Set<String> properties;

        public Static(Prefixes staticPrefixes) {
            staticObject = new JS.Obj();
            properties = new HashSet<String>();
            uriPrefixes = staticPrefixes;
        }

        public GlobalsChecker getStaticChecker() {
            return new GlobalsChecker() {

                public boolean acceptGlobal(String name) {
                    return hasProperty(name) || staticGlobals.contains(name) || staticPart.uriPrefixes.isPrefix(name);
                }

                public void decideDeclareGlobal(JS parent, String token) {
                    if (JSU.S("static").equals(parent)) properties.add(token);
                }
            };
        }

        public String getDescription(String property) {
            return "TODO doc for static properties";
        }

        public Set getPreapplies() {
            return new HashSet();
        }

        public Iterator getProperties() {
            return properties.iterator();
        }

        public boolean hasProperty(String property) {
            return properties.contains(property) || "static".equals(property) || "vexi".equals(property);
        }
    }

    /**
	 *  Encapsulates a template node 
	 *  REMARK - modified from org.vexi.core.Template
	 *  REMARK - even though it is implementing PropertiesDoc,
	 *  this is only from a logical point of view (in terms of generation)
	 *  really the properties are documented in the TemplateFile all together.
	 *  This retains the order. */
    public class TElement extends CodeBlock implements Constants {

        TElement preapply = null;

        TFile principal = null;

        SortedSet<String> properties = new TreeSet<String>();

        Map<String, String> idTemplates = new HashMap<String, String>();

        public TElement getPreapply() {
            return preapply;
        }

        public TFile getPrincipal() {
            return principal;
        }

        public SortedSet<String> getProperties() {
            return properties;
        }

        public boolean hasProperty(String property) {
            if (properties.contains(property)) return true;
            if (preapply != null && preapply.hasProperty(property)) return true;
            if (principal != null && principal.hasProperty(property)) return true;
            if (principal == null && BoxDoc.get().hasProperty(property)) return true;
            return false;
        }

        public String getDescription(String property) {
            return "TODO - doc for custom property";
        }

        public GlobalsChecker getPIChecker() {
            return new GlobalsChecker() {

                public boolean acceptGlobal(String name) {
                    return hasProperty(name) || staticGlobals.contains(name) || "thisbox".equals(name) || uriPrefixes.isPrefix(name) || name.startsWith("$");
                }

                public void decideDeclareGlobal(JS parent, String token) {
                    if (JSU.S("thisbox").equals(parent)) properties.add(token);
                }
            };
        }

        String id = null;

        JS[] keys;

        JS[] vals;

        Prefixes uriPrefixes;

        Vec children = new Vec();

        JS script = null;

        TElement parent = null;

        JS ff;

        Parser.GlobalsChecker pisParserParam = null;

        TElement(TElement t, int startLine) {
            preapply = t;
            this.ff = t.ff;
            this.startLine = startLine;
            templates = this;
        }

        TElement(JS ff, int startLine) {
            this.ff = ff;
            this.startLine = startLine;
            templates = this;
        }

        TElement(JS ff, int startLine, TElement parent) {
            this.ff = ff;
            this.startLine = startLine;
            this.parent = parent;
        }

        private class RedirectTarget implements TemplateHashModel {

            String id;

            Set<String> props = new HashSet<String>();

            RedirectTarget(String id) {
                this.id = id;
            }

            public TemplateModel get(String key) throws TemplateModelException {
                if ("name".equals(key)) {
                    return new SimpleScalar(idTemplates.get(id));
                } else if ("props".equals(key)) {
                    return new SimpleCollection(props);
                }
                return null;
            }

            public boolean isEmpty() throws TemplateModelException {
                return false;
            }

            public void add(String prop) {
                props.add(prop);
            }
        }

        private TemplateCollectionModel redirectTargets = null;

        public TemplateCollectionModel getRedirectTargets() {
            if (this.redirectTargets == null) {
                HashMap<String, RedirectTarget> redirectTargets = new HashMap<String, RedirectTarget>();
                if (content != null && !"vexi.util.redirect".equals(name)) {
                    String code = content.toString();
                    Pattern p = Pattern.compile("addRedirect(.*)", Pattern.MULTILINE);
                    Matcher m = p.matcher(code);
                    HashMap targets = new HashMap();
                    while (m.find()) {
                        int end = code.indexOf(")", m.start() + 1);
                        String addRedirect = code.substring(m.start(), end + 1);
                        Pattern pid = Pattern.compile("\\$[a-zA-Z]*");
                        Matcher mid = pid.matcher(addRedirect);
                        if (!mid.find()) continue;
                        String id = mid.group(0).substring(1);
                        RedirectTarget target = redirectTargets.get(id);
                        if (target == null) {
                            target = new RedirectTarget(id);
                            redirectTargets.put(id, target);
                        }
                        Pattern pprop = Pattern.compile("\"[^\"]*\"", Pattern.MULTILINE);
                        Matcher mprop = pprop.matcher(addRedirect);
                        while (mprop.find()) {
                            String prop = addRedirect.substring(mprop.start() + 1, mprop.end() - 1);
                            target.add(prop);
                        }
                    }
                }
                this.redirectTargets = new SimpleCollection(redirectTargets.values());
            }
            return this.redirectTargets;
        }

        public boolean isEmpty() throws TemplateModelException {
            return false;
        }

        public void putId(String id, String template) {
            if (parent != null) parent.putId(id, template); else idTemplates.put(id, template);
        }
    }

    static class Prefixes {

        static final Object NULL_PLACEHOLDER = new Object();

        private Basket.Hash map = new Basket.Hash();

        Prefixes parent;

        JS vexi;

        Prefixes(JS vexi, Prefixes parent, Tree.Element e) {
            this.parent = parent;
            this.vexi = vexi;
            Tree.Prefixes prefixes = e.getPrefixes();
            for (int i = 0; i < prefixes.pfxSize(); i++) {
                String key = prefixes.getPrefixKey(i);
                String val = prefixes.getPrefixVal(i);
                if (val.equals("vexi://ui")) continue;
                if (val.equals("vexi://meta")) continue;
                if (val.length() > 0 && val.charAt(0) == '.') val = val.substring(1);
                map.put(JSU.S(key), val);
            }
        }

        public boolean isPrefix(String key) {
            try {
                return get(JSU.S(key), false) != null;
            } catch (JSExn e) {
                e.printStackTrace();
                return false;
            }
        }

        public Object get(JS key, boolean resolve) throws JSExn {
            Object r = map.get(key);
            if (r == NULL_PLACEHOLDER) return null;
            if (resolve && r instanceof String) {
            }
            if (r == null && parent != null) {
                r = parent.get(key, resolve);
                if (r == null) {
                    if ("".equals(JSU.toString(key))) r = vexi.get(key); else map.put(key, NULL_PLACEHOLDER);
                }
            }
            return r;
        }
    }
}

class CodeBlock {

    StringBuffer content = null;

    int content_start = 0;

    int startLine = -1;
}

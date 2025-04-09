package org.garret.ptl.template;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import org.garret.ptl.startup.Configuration;
import org.garret.ptl.startup.IStaticResource;
import org.garret.ptl.util.SystemException;

/**
 * A compiled PTL template that can be parametrised with a set of variables and subcomponents,
 * and rendered into an XHTML snippet.
 *
 * @author Andrey Subbotin
 */
class Template {

    private static final String JS_FILE = "/js/ptl.js";

    private final List<String> imports = new ArrayList<String>();

    private final HashMap<String, Integer> nameToIndex = new HashMap<String, Integer>();

    private final Map<String, Template> nameToRepeatable = new HashMap<String, Template>();

    private Integer repeatableParentIndex = null;

    private final List<INode> nodes = new ArrayList<INode>();

    private Stack<Group> groupStack = new Stack<Group>();

    private final String name;

    public Template(String name) {
        this.name = name;
    }

    Integer declareVariable(String name) {
        Integer ndx = nameToIndex.get(name);
        if (ndx == null) {
            ndx = nameToIndex.size();
            nameToIndex.put(name, ndx);
        }
        return ndx;
    }

    private void addNode(INode node) {
        if (groupStack.isEmpty()) nodes.add(node); else groupStack.peek().buffer.add(node);
    }

    private void declareVariablesInExpression(String id) {
        int end = 0;
        for (int ndx = id.indexOf("${"); ndx >= 0; ndx = id.indexOf("${", end)) {
            end = id.indexOf('}', ndx);
            String var = id.substring(ndx + 2, end);
            declareVariable(var);
            end++;
        }
    }

    private String evalExpression(Template.Instance instance, String id) {
        StringBuffer buf = new StringBuffer();
        int end = 0;
        for (int ndx = id.indexOf("${"); ndx >= 0; ndx = id.indexOf("${", end)) {
            buf.append(id.substring(end, ndx));
            end = id.indexOf('}', ndx);
            String var = id.substring(ndx + 2, end);
            buf.append(PtlExpressionBuilder.build(Template.this, var).execute(instance).toString());
            end++;
        }
        if (end < id.length()) {
            buf.append(id.substring(end));
        }
        return buf.toString();
    }

    void addImport(String filename) {
        imports.add(filename);
    }

    void addText(String text) {
        addNode(new StringNode(text));
    }

    void addHeaderPlaceholder() {
        addNode(new HeaderPlaceholderNode());
    }

    void addExpression(String name) {
        addNode(new ExpressionNode(PtlExpressionBuilder.build(this, name)));
    }

    void addTag(String name, String id, String value, String[] attrNames, String[] attrValues) {
        addNode(new TagNode(id, name, value, attrNames, attrValues));
    }

    void addComponent(String id) {
        addNode(new ComponentNode(id));
    }

    void addInclude(String filename) {
        addNode(new IncludeNode(filename));
    }

    void openConditional(String expr, boolean ifTrue) {
        PtlExpression expression = (expr != null) ? PtlExpressionBuilder.build(this, expr) : null;
        groupStack.add(new Group(expression, ifTrue));
    }

    void closeConditional() {
        Group c = groupStack.pop();
        addNode(new ConditionalNode(c.expression, c.ifTrue, new ArrayList<INode>(c.buffer)));
    }

    void openUpdateable(String eventName, String htmlId) {
        groupStack.add(new Group(eventName, htmlId));
    }

    void closeUpdateable() {
        Group c = groupStack.pop();
        addNode(new UpdateableNode(c.eventName, c.htmlId, new ArrayList<INode>(c.buffer)));
    }

    void openChoose() {
        groupStack.add(new Group(null, null));
    }

    void closeChoose() {
        Group c = groupStack.pop();
        addNode(new ChooseNode(new ArrayList<INode>(c.buffer)));
    }

    void openComment() {
        groupStack.add(new Group(null, null));
    }

    void closeComment() {
        Group c = groupStack.pop();
        addNode(new CommentNode(new ArrayList<INode>(c.buffer)));
    }

    void addRepeatable(String id, Template subnode, boolean insertable, boolean updateable, String separator) {
        if (nameToRepeatable.get(id) != null) throw new IllegalStateException("Repeatable with the same ID declared twice: " + id + " at " + name);
        subnode.repeatableParentIndex = nameToRepeatable.size();
        nameToRepeatable.put(id, subnode);
        addNode(new RepeatableNode(id, subnode, insertable, updateable, separator));
    }

    private String nameForIndex(Integer index) {
        for (String name : nameToIndex.keySet()) if (nameToIndex.get(name).equals(index)) return name;
        return "";
    }

    Collection<String> imports() {
        return imports;
    }

    Instance newInstance(IPageContext context, Instance parent, Map<String, ITag> tags, Map<String, IComponent> components) {
        return new Instance(context, parent, tags, components);
    }

    void write(Instance instance, Writer writer) throws IOException {
        for (INode node : nodes) node.print(instance, writer);
    }

    void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException {
        for (INode node : nodes) node.writeAjaxUpdateables(instance, event, results);
    }

    String writeAjaxRepeatableElement(Instance instance, String repeatableId, int index) throws IOException {
        for (INode node : nodes) {
            String result = node.writeAjaxRepeatable(instance, repeatableId, index);
            if (result != null) return result;
        }
        return null;
    }

    private interface INode {

        void print(Instance instance, Writer writer) throws IOException;

        void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException;

        String writeAjaxRepeatable(Instance instance, String repeatableId, int index) throws IOException;
    }

    private class StringNode implements INode {

        private final String text;

        StringNode(String text) {
            this.text = text;
        }

        public void print(Instance instance, Writer writer) throws IOException {
            writer.append(text);
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException {
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) {
            return null;
        }
    }

    private class HeaderPlaceholderNode implements INode {

        public void print(Instance instance, Writer writer) throws IOException {
            for (String ref : instance.imports()) {
                IStaticResource resource = Configuration.resourceForPath(ref);
                if (resource == null) throw new IllegalStateException("Unknown ptl:import resource: " + ref + " at " + name);
                long timestamp = resource.lastModified();
                if (ref.endsWith(".js")) {
                    writer.append("<script type=\"text/javascript\" src=\"").append(ref).append("?ts=").append(Long.toString(timestamp)).append("\"></script>\n");
                } else {
                    writer.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(ref).append("?ts=").append(Long.toString(timestamp)).append("\"></link>\n");
                }
            }
            if (Configuration.getAttribute("ptl-js-insert", true)) {
                writer.append("<script type=\"text/javascript\" src=\"").append(JS_FILE).append("?ts=").append(Long.toString(Configuration.resourceForPath(JS_FILE).lastModified())).append("\"></script>\n");
            }
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException {
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) {
            return null;
        }
    }

    private class ExpressionNode implements INode {

        private final PtlExpression expression;

        ExpressionNode(PtlExpression expr) {
            this.expression = expr;
        }

        public void print(Instance instance, Writer writer) throws IOException {
            String value = expression.execute(instance).toString();
            int idx = -1;
            if (expression instanceof PtlExpression.VarEx) idx = ((PtlExpression.VarEx) expression).idx;
            Integer mode = idx < 0 ? 0 : instance.mode(idx);
            boolean brMode = idx < 0 || (mode & ITemplateInstance.MODE_BR) == 0 && (mode & ITemplateInstance.MODE_NOBR) == 0 ? instance.brMode : (mode & ITemplateInstance.MODE_BR) != 0;
            boolean escMode = idx < 0 || (mode & ITemplateInstance.MODE_ESCAPE) == 0 && (mode & ITemplateInstance.MODE_UNESCAPE) == 0 ? instance.escapeTextMode : (mode & ITemplateInstance.MODE_ESCAPE) != 0;
            if (brMode) {
                if (escMode) {
                    StringWriter ww = new StringWriter();
                    ww.write(PtlPage.escapeText(value));
                    value = ww.toString();
                }
                writer.append(value.trim().replace("\n", "<br/>"));
            } else {
                if (escMode) {
                    writer.write(PtlPage.escapeText(value));
                } else {
                    writer.append(value);
                }
            }
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException {
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) {
            return null;
        }
    }

    private class ConditionalNode implements INode {

        private final PtlExpression expression;

        private final boolean ifTrue;

        private final List<INode> nodes;

        ConditionalNode(PtlExpression expression, boolean ifTrue, List<INode> nodes) {
            this.expression = expression;
            this.ifTrue = ifTrue;
            this.nodes = nodes;
        }

        public void print(Instance instance, Writer writer) throws IOException {
            if (evalCondition(instance)) {
                for (INode node : nodes) node.print(instance, writer);
            }
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException {
            if (evalCondition(instance)) {
                for (INode node : nodes) node.writeAjaxUpdateables(instance, event, results);
            }
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) throws IOException {
            if (evalCondition(instance)) for (INode node : nodes) {
                String result = node.writeAjaxRepeatable(instance, repeatableId, index);
                if (result != null) return result;
            }
            return null;
        }

        private boolean evalCondition(Instance instance) {
            if (expression == null) return true;
            Object value = expression.execute(instance);
            return (!ifTrue ^ ("true".equals(value) || Boolean.TRUE.equals(value)));
        }
    }

    private class ChooseNode implements INode {

        private final List<INode> nodes;

        public ChooseNode(List<INode> nodes) {
            this.nodes = nodes;
        }

        public void print(Instance instance, Writer writer) throws IOException {
            for (INode node : nodes) {
                if (node instanceof ConditionalNode) {
                    if (((ConditionalNode) node).evalCondition(instance)) {
                        node.print(instance, writer);
                        break;
                    }
                } else {
                    node.print(instance, writer);
                }
            }
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) throws IOException {
            for (INode node : nodes) {
                if (node instanceof ConditionalNode) {
                    if (((ConditionalNode) node).evalCondition(instance)) {
                        return node.writeAjaxRepeatable(instance, repeatableId, index);
                    }
                }
            }
            return null;
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException {
            for (INode node : nodes) {
                if (node instanceof ConditionalNode) {
                    if (((ConditionalNode) node).evalCondition(instance)) {
                        node.writeAjaxUpdateables(instance, event, results);
                        break;
                    }
                } else {
                    node.writeAjaxUpdateables(instance, event, results);
                }
            }
        }
    }

    private class CommentNode implements INode {

        private final List<INode> nodes;

        public CommentNode(List<INode> nodes) {
            this.nodes = nodes;
        }

        public void print(Instance instance, Writer writer) throws IOException {
            writer.append("<!--");
            for (INode node : nodes) {
                node.print(instance, writer);
            }
            writer.append("-->");
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) throws IOException {
            return null;
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException {
            for (INode node : nodes) {
                node.writeAjaxUpdateables(instance, event, results);
            }
        }
    }

    private class UpdateableNode implements INode {

        private final String eventName;

        private final String htmlId;

        private final List<INode> nodes;

        UpdateableNode(String eventName, String htmlId, List<INode> nodes) {
            this.eventName = eventName;
            this.htmlId = htmlId;
            this.nodes = nodes;
            declareVariablesInExpression(eventName);
        }

        public void print(Instance instance, Writer writer) throws IOException {
            for (INode node : nodes) node.print(instance, writer);
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException {
            String evalEventName = evalExpression(instance, this.eventName);
            if (evalEventName.equals(event)) {
                StringWriter writer = new StringWriter();
                for (INode subnode : nodes) subnode.print(instance, writer);
                results.put(prepareHtmlId(instance), writer.toString());
            } else {
                for (INode node : nodes) node.writeAjaxUpdateables(instance, event, results);
            }
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) throws IOException {
            for (INode node : nodes) {
                String result = node.writeAjaxRepeatable(instance, repeatableId, index);
                if (result != null) return result;
            }
            return null;
        }

        private String prepareHtmlId(Instance instance) {
            StringBuffer sb = new StringBuffer();
            int p = 0, q = 0;
            while (true) {
                p = htmlId.indexOf("${", q);
                if (p >= 0) sb.append(htmlId.substring(q, p)); else {
                    sb.append(htmlId.substring(q));
                    break;
                }
                q = htmlId.indexOf("}", p);
                String var = htmlId.substring(p + 2, q);
                sb.append(instance.variable(nameToIndex.get(var)));
                if (++q >= htmlId.length()) break;
            }
            return sb.toString();
        }
    }

    private class RepeatableNode implements INode {

        private final String id;

        private final Template template;

        private final boolean insertable;

        private final boolean updateable;

        private final String separator;

        RepeatableNode(String id, Template template, boolean insertable, boolean updateable, String separator) {
            this.id = id;
            this.template = template;
            this.insertable = insertable;
            this.updateable = updateable;
            if (separator == null || separator.trim().length() == 0) separator = "div";
            this.separator = separator;
        }

        public void print(Instance instance, Writer writer) throws IOException {
            String uniId = instance.repeatableUid(template, id);
            if (insertable || updateable) writer.append("<" + separator + " id='ptl$").append(uniId).append("$B' style='display:none'></" + separator + ">");
            int counter = 0;
            for (Instance sub : instance.repeatables(template)) {
                template.write(sub, writer);
                if (updateable) writer.append("<" + separator + " id='ptl$").append(uniId).append("$" + counter).append("' style='display:none'></" + separator + ">");
                counter++;
            }
            if (insertable || updateable) writer.append("<" + separator + " id='ptl$").append(uniId).append("$E' style='display:none'></" + separator + ">");
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) throws IOException {
            for (Instance sub : instance.repeatables(template)) {
                for (INode node : template.nodes) node.writeAjaxUpdateables(sub, event, results);
            }
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) throws IOException {
            String uniId = instance.repeatableUid(template, id);
            int counter = 0;
            for (Instance sub : instance.repeatables(template)) {
                if (repeatableId.equals(id) && index == counter) {
                    if (!insertable && !updateable) throw new IllegalStateException("<repeatable> must set insertable='true' or updateable='true' to be refreshed fron ajax at " + name);
                    StringWriter writer = new StringWriter();
                    template.write(sub, writer);
                    if (updateable) writer.append("<" + separator + " id='ptl$").append(uniId).append("$" + counter).append("' style='display:none'></" + separator + ">");
                    return writer.toString();
                } else {
                    String result = template.writeAjaxRepeatableElement(sub, repeatableId, index);
                    if (result != null) return result;
                }
                counter++;
            }
            return null;
        }
    }

    private class ComponentNode implements INode {

        private final String id;

        ComponentNode(String id) {
            this.id = id;
            declareVariablesInExpression(id);
        }

        public void print(Instance instance, Writer writer) throws IOException {
            String evalId = evalExpression(instance, id);
            IComponent c = instance.components.get(evalId);
            if (c == null) throw new SystemException("Component '" + evalId + "' is undefined");
            if (c != TemplateComponent.EMPTY) c.renderHtml(instance.context, writer);
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) {
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) {
            return null;
        }
    }

    private class IncludeNode implements INode {

        private final String filename;

        IncludeNode(String filename) {
            this.filename = filename;
            declareVariablesInExpression(filename);
        }

        public void print(Instance instance, Writer writer) throws IOException {
            IncludeComponent c = new IncludeComponent(instance.context, evalExpression(instance, filename));
            c.renderHtml(instance.context, writer);
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) {
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) {
            return null;
        }
    }

    private class TagNode implements INode {

        private final String id;

        private final String tagName;

        private final String tagValue;

        private final String[] attrNames;

        private final String[] attrValues;

        TagNode(String tagId, String tagName, String tagValue, String[] attrNames, String[] attrValues) {
            this.id = tagId;
            this.tagName = tagName;
            this.tagValue = tagValue;
            this.attrNames = attrNames;
            this.attrValues = attrValues;
            declareVariablesInExpression(tagId);
        }

        public void print(Instance instance, Writer writer) throws IOException {
            String evalId = evalExpression(instance, id);
            ITag tag = instance.tags.get(evalId);
            if (tag == null) throw new IllegalStateException("Tag '" + evalId + "' not declared in template " + name);
            TagAttributes ta = new TagAttributes();
            tag.renderHtml(instance.context, ta);
            if (ta.html() != null) {
                writer.append(ta.html());
            } else {
                writer.append('<').append(tagName);
                for (int i = 0; i < attrNames.length; i++) {
                    String newValue = ta.getAndForget(attrNames[i]);
                    if (newValue == null) newValue = attrValues[i];
                    printAttribute(instance, attrNames[i], newValue, writer);
                }
                for (int i = 0; i < ta.numberOfAttributes(); i++) printAttribute(instance, ta.name(i), ta.value(i), writer);
                String value = ta.value();
                if (value == null) value = tagValue;
                if (value == null || value.length() == 0) {
                    if (TemplateCompilerStream.isForceOpenTag(tagName)) {
                        writer.append(">").append("</").append(tagName).append(">");
                    } else {
                        writer.append("/>");
                    }
                } else {
                    writer.append(">").append(value);
                    writer.append("</").append(tagName).append(">");
                }
            }
        }

        public void printAttribute(Instance instance, String name, String value, Writer writer) throws IOException {
            writer.append(' ').append(name).append("=\"");
            for (int ndx = value.indexOf("${"); ndx >= 0; ndx = value.indexOf("${")) {
                String before = value.substring(0, ndx);
                writer.append(before);
                int end = value.indexOf('}', ndx);
                String var = value.substring(ndx + 2, end);
                value = value.substring(end + 1);
                if (var.equals("SPACE")) {
                    writer.write(" ");
                } else {
                    writer.append(PtlExpressionBuilder.build(Template.this, var).execute(instance).toString());
                }
            }
            writer.write(value);
            writer.append('\"');
        }

        public void writeAjaxUpdateables(Instance instance, String event, Map<String, String> results) {
        }

        public String writeAjaxRepeatable(Instance instance, String repeatableId, int index) {
            return null;
        }
    }

    class Instance implements ITemplateInstance {

        private final IPageContext context;

        private final Map<String, ITag> tags = new HashMap<String, ITag>();

        private final Map<String, IComponent> components = new HashMap<String, IComponent>();

        private final Instance parent;

        private Object[] values = new Object[nameToIndex.size()];

        private boolean[] deeps = new boolean[nameToIndex.size()];

        private Integer[] modes = new Integer[nameToIndex.size()];

        private final List<Instance>[] repeatables = new List[nameToRepeatable.size()];

        private final String[] repeatableUids = new String[nameToRepeatable.size()];

        private final List<String> imports = new ArrayList<String>();

        private boolean escapeTextMode = true;

        private boolean brMode = false;

        Instance(IPageContext context, Instance parent, Map<String, ITag> tags, Map<String, IComponent> components) {
            this.context = context;
            this.parent = parent;
            this.tags.putAll(tags);
            this.components.putAll(components);
        }

        public void setEscapeTextMode(boolean on) {
            this.escapeTextMode = on;
        }

        public void setBrMode(boolean on) {
            this.brMode = on;
        }

        public void set(String name, Object value) {
            set(name, value, false);
        }

        private <T> T[] increaseArray(T[] a, int len) {
            T[] tmp = a;
            if (a.length > len) {
                return a;
            }
            int newLen = a.length * 2;
            if (newLen < len) {
                newLen = len;
            }
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), newLen);
            System.arraycopy(tmp, 0, a, 0, tmp.length);
            return a;
        }

        private boolean[] increaseArray(boolean[] a, int len) {
            boolean[] tmp = a;
            if (a.length > len) {
                return a;
            }
            int newLen = a.length * 2;
            if (newLen < len) {
                newLen = len;
            }
            a = new boolean[newLen];
            System.arraycopy(tmp, 0, a, 0, tmp.length);
            return a;
        }

        public void set(String name, Object value, boolean deep) {
            set(name, value, deep, MODE_NONE);
        }

        public void set(String name, Object value, boolean deep, int mode) {
            if (value == null) value = "";
            Integer index = nameToIndex.get(name);
            if (index == null) {
                if (!deep) throw new IllegalStateException("Undefined variable set:" + name + ", available:" + nameToIndex.keySet() + " at " + Template.this.name); else {
                    declareVariable(name);
                    values = increaseArray(values, values.length + 1);
                    deeps = increaseArray(deeps, deeps.length + 1);
                    modes = increaseArray(modes, modes.length + 1);
                    index = nameToIndex.get(name);
                }
            }
            if (index >= values.length) {
                values = increaseArray(values, index + 1);
                deeps = increaseArray(deeps, index + 1);
                modes = increaseArray(modes, index + 1);
            }
            values[index] = value;
            deeps[index] = deep;
            modes[index] = mode;
        }

        public void setComponent(String key, IComponent value) {
            components.put(key, value);
        }

        public boolean hasComponent(String key) {
            return hasComponent(nodes, key);
        }

        private boolean hasComponent(List<INode> nodes, String key) {
            if (nodes == null) return false;
            for (INode x : nodes) {
                if (x instanceof ComponentNode && ((ComponentNode) x).id.equals(key)) return true;
                if (x instanceof ConditionalNode && hasComponent(((ConditionalNode) x).nodes, key)) return true;
            }
            return false;
        }

        public void setTag(String key, ITag value) {
            tags.put(key, value);
        }

        public void setCollectionUniqueId(String name, String uniqueId) {
            Template subnode = nameToRepeatable.get(name);
            repeatableUids[subnode.repeatableParentIndex] = uniqueId;
        }

        public ITemplateInstance createCollectionsElement(String... names) {
            ITemplateInstance[] arr = new ITemplateInstance[names.length];
            for (int i = 0; i < names.length; i++) arr[i] = createCollectionElement(names[i]);
            return new MultiInstance(arr);
        }

        public ITemplateInstance createCollectionElement(String name) {
            Template subnode = nameToRepeatable.get(name);
            if (subnode == null) throw new IllegalStateException("Undefined repeatable accessed:" + name + ", available:" + nameToRepeatable.keySet() + " at " + Template.this.name);
            if (repeatables[subnode.repeatableParentIndex] == null) repeatables[subnode.repeatableParentIndex] = new ArrayList<Instance>();
            Instance instance = subnode.newInstance(context, this, tags, components);
            repeatables[subnode.repeatableParentIndex].add(instance);
            return instance;
        }

        Integer nameToIndex(String name) {
            return nameToIndex.get(name);
        }

        int mode(int index) {
            if (modes[index] == null) {
                String name = nameForIndex(index);
                Instance p = parent;
                while (p != null) {
                    Integer x = p.nameToIndex(name);
                    if (x != null && x >= 0 && p.deeps[x]) return p.modes[x];
                    p = p.parent;
                }
                throw new IllegalStateException("Template parameter not defined: " + nameForIndex(index) + " at " + Template.this.name);
            }
            return modes[index];
        }

        Object variable(int index) {
            if (values[index] == null) {
                String name = nameForIndex(index);
                Instance p = parent;
                while (p != null) {
                    Integer x = p.nameToIndex(name);
                    if (x != null && x >= 0 && p.deeps[x]) return p.values[x];
                    p = p.parent;
                }
                throw new IllegalStateException("Template parameter not defined: " + nameForIndex(index) + " at " + Template.this.name);
            }
            return values[index];
        }

        String repeatableUid(Template template, String def) {
            String uid = repeatableUids[template.repeatableParentIndex];
            return (uid != null) ? uid : def;
        }

        Iterable<Instance> repeatables(Template template) {
            if (repeatables[template.repeatableParentIndex] == null) return Collections.emptyList();
            return repeatables[template.repeatableParentIndex];
        }

        void setImports(Collection<String> imports) {
            this.imports.addAll(imports);
        }

        Iterable<String> imports() {
            return imports;
        }
    }

    static class MultiInstance implements ITemplateInstance {

        final ITemplateInstance[] arr;

        MultiInstance(ITemplateInstance[] arr) {
            this.arr = arr;
        }

        @Override
        public ITemplateInstance createCollectionElement(String name) {
            ITemplateInstance[] newArr = new ITemplateInstance[arr.length];
            for (int i = 0; i < arr.length; i++) newArr[i] = arr[i].createCollectionElement(name);
            return new MultiInstance(newArr);
        }

        @Override
        public boolean hasComponent(String key) {
            return arr[0].hasComponent(key);
        }

        @Override
        public void set(String key, Object value) {
            for (ITemplateInstance x : arr) x.set(key, value);
        }

        @Override
        public void set(String key, Object value, boolean deep) {
            for (ITemplateInstance x : arr) x.set(key, value, deep);
        }

        @Override
        public void set(String key, Object value, boolean deep, int mode) {
            for (ITemplateInstance x : arr) x.set(key, value, deep, mode);
        }

        @Override
        public void setBrMode(boolean on) {
            for (ITemplateInstance x : arr) x.setBrMode(on);
        }

        @Override
        public void setCollectionUniqueId(String name, String uniqueId) {
            for (ITemplateInstance x : arr) x.setCollectionUniqueId(name, uniqueId);
        }

        @Override
        public void setComponent(String key, IComponent value) {
            for (ITemplateInstance x : arr) x.setComponent(key, value);
        }

        @Override
        public void setEscapeTextMode(boolean on) {
            for (ITemplateInstance x : arr) x.setEscapeTextMode(on);
        }

        @Override
        public void setTag(String key, ITag value) {
            for (ITemplateInstance x : arr) x.setTag(key, value);
        }

        @Override
        public ITemplateInstance createCollectionsElement(String... names) {
            ITemplateInstance[] newArr = new ITemplateInstance[arr.length];
            for (int i = 0; i < arr.length; i++) newArr[i] = arr[i].createCollectionsElement(names);
            return new MultiInstance(newArr);
        }
    }

    private static class Group {

        private final PtlExpression expression;

        private final boolean ifTrue;

        private final String eventName;

        private final String htmlId;

        private List<INode> buffer = new ArrayList<INode>();

        public Group(PtlExpression expr, boolean ifTrue) {
            this.expression = expr;
            this.ifTrue = ifTrue;
            this.eventName = null;
            this.htmlId = null;
        }

        Group(String eventName, String htmlId) {
            this.expression = null;
            this.ifTrue = false;
            this.eventName = eventName;
            this.htmlId = htmlId;
        }
    }

    public String getName() {
        return name;
    }
}

package com.setec.xml;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import su.jet.chedecp.icminterface.operations.storage.model.Changeable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLNode implements Changeable, Comparable {

    protected static Map extenders = new HashMap();

    protected String id;

    protected Map attributes;

    protected Map children = new LinkedHashMap();

    protected XMLNode parent = null;

    private String xmlHeader;

    private boolean isEmpty = false;

    public XMLNode() {
    }

    public XMLNode(String content) {
        load(content, null);
    }

    public XMLNode(String content, XMLNode parent) {
        load(content, parent);
    }

    protected void load(String content, XMLNode parent) {
        this.parent = parent;
        content = normalizeXML(content);
        xmlHeader = findFirst(content, "<\\?.*\\?>");
        if (xmlHeader == null) {
            xmlHeader = "";
        }
        content = content.replaceAll("<\\?.*?>", "");
        id = findFirst(content, "^<[a-zA-Z0-9_:]+").replaceAll("^<", "").trim();
        String attributeContent = "";
        String internalContent = "";
        if (findFirst(content, "^<" + id + "[^<]*/>") != null) {
            attributeContent = findFirst(content, "^<" + id + "[^<]*/>").replaceAll("^<" + id, "").replaceAll("/>$", "").trim();
        } else {
            attributeContent = findFirst(content, "^<" + id + "[^<^>]*>").replaceAll("^<" + id, "").replaceAll(">$", "").trim();
            internalContent = content.replaceAll("^<" + id + "[^<^>]*>", "").replaceAll("</" + id + ">$", "");
        }
        attributes = parseAttributes(attributeContent);
        String parsedContent = internalContent;
        while (parsedContent.length() > 0) {
            String internalNodeName = findFirst(parsedContent, "^<[a-zA-Z0-9_:]+").replaceAll("^<", "").trim();
            String internalNodeBlock = findFirst(parsedContent, "^<" + internalNodeName + "[^<]*/>");
            if (internalNodeBlock == null) {
                int balance = 0;
                int fromIndex = 0;
                while (true) {
                    boolean isClosingNode = false;
                    int indexOfNodeName = parsedContent.indexOf(internalNodeName, fromIndex);
                    int indexOfOpeningBracket = parsedContent.lastIndexOf('<', indexOfNodeName);
                    int indexOfClosingBracket = indexOfNodeName;
                    while (true) {
                        indexOfClosingBracket = parsedContent.indexOf('>', indexOfClosingBracket);
                        if ((parsedContent.length() == indexOfClosingBracket + 1) || parsedContent.charAt(indexOfClosingBracket + 1) == '<') {
                            break;
                        }
                        indexOfClosingBracket++;
                    }
                    String nodeBody = parsedContent.substring(indexOfOpeningBracket, indexOfClosingBracket + 1);
                    if (findFirst(nodeBody, "<" + internalNodeName + "[^>]*/>") == null) {
                        if (findFirst(nodeBody, "</[^>]*>") != null) {
                            balance--;
                            isClosingNode = true;
                        } else {
                            balance++;
                        }
                    }
                    if (balance == 0 && isClosingNode) {
                        internalNodeBlock = parsedContent.substring(0, indexOfClosingBracket + 1);
                        break;
                    }
                    fromIndex = indexOfClosingBracket + 1;
                }
            }
            Class childClass = (Class) extenders.get(id + "." + internalNodeName);
            if (childClass == null) {
                childClass = (Class) extenders.get(internalNodeName);
            }
            if (childClass == null) {
                childClass = XMLNode.class;
            }
            XMLNode child = null;
            try {
                child = (XMLNode) childClass.getConstructor(new Class[] { String.class, XMLNode.class }).newInstance(new Object[] { internalNodeBlock, this });
            } catch (NoSuchMethodException e) {
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (children.get(child.getId()) == null) {
                children.put(child.getId(), new ArrayList());
            }
            ((List) children.get(child.getId())).add(child);
            parsedContent = parsedContent.substring(internalNodeBlock.length());
        }
    }

    public String getId() {
        return id;
    }

    public Map getAttributes() {
        return attributes;
    }

    public Map getChildren() {
        return children;
    }

    public XMLNode getParent() {
        return parent;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public String getHeader() {
        return xmlHeader;
    }

    public String getName() {
        if (getAttribute("name") != null) {
            return getAttribute("name");
        } else if (getAttribute("ref") != null) {
            return getAttribute("ref");
        } else {
            String attrName = (String) CollectionUtils.find(attributes.keySet(), new Predicate() {

                public boolean evaluate(Object o) {
                    if (((String) o).indexOf("Name") >= 0 || ((String) o).indexOf("name") >= 0) {
                        return true;
                    }
                    return false;
                }
            });
            if (attrName != null) {
                return getNormalAttribute(attrName);
            }
        }
        return id;
    }

    /**
     * Путь к узлу дерева в виде /root/branch1/subbranch1
     * @return
     */
    public String getFullPath() {
        List path = new ArrayList();
        path.add(id);
        XMLNode node = this;
        while (node.getParent() != null) {
            node = node.getParent();
            path.add(node.id);
        }
        Collections.reverse(path);
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < path.size(); i++) {
            result.append("/").append(path.get(i));
        }
        return result.toString();
    }

    /**
     * Глубина узла в дереве (количество элементов пути к данному узлу минус единица)
     * @return
     */
    public int getDepth() {
        int result = 0;
        XMLNode node = this;
        while (node.getParent() != null) {
            node = node.getParent();
            result++;
        }
        return result;
    }

    /**
     *
     * @param name
     * @param index
     * @return
     */
    public XMLNode getChildNode(String name, int index) {
        return (XMLNode) ((List) children.get(name)).get(index);
    }

    public int getChildNumber(String name) {
        if (children.get(name) == null) {
            return 0;
        }
        return ((List) children.get(name)).size();
    }

    public List getChildrenList() {
        List result = new ArrayList();
        for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
            result.addAll((List) children.get(it.next()));
        }
        return result;
    }

    public String getAttribute(String name) {
        return (String) attributes.get(name);
    }

    public String getNormalAttribute(String name) {
        if (!attributes.containsKey(name)) {
            return "";
        }
        return (String) attributes.get(name);
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public List getSiblings(String id) {
        List result = (List) children.get(id);
        if (result == null) {
            return new ArrayList();
        } else {
            return result;
        }
    }

    public List getSiblingsAttributes(String id, String attributeName) {
        List result = new ArrayList();
        for (Iterator it = getSiblings(id).iterator(); it.hasNext(); ) {
            XMLNode node = (XMLNode) it.next();
            if (node.getAttribute(attributeName) != null) {
                result.add(node.getAttribute(attributeName));
            }
        }
        return result;
    }

    public List getSiblingTreeAttributes(String id, String attributeName) {
        List result = new ArrayList();
        for (Iterator it = getSiblings(id).iterator(); it.hasNext(); ) {
            XMLNode node = (XMLNode) it.next();
            if (node.getAttribute(attributeName) != null) {
                result.add(node.getAttribute(attributeName));
            }
        }
        for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            for (Iterator jt = ((List) children.get(key)).iterator(); jt.hasNext(); ) {
                result.addAll(((XMLNode) jt.next()).getSiblingTreeAttributes(id, attributeName));
            }
        }
        return result;
    }

    public List getSiblingsTree(String id) {
        List result = new ArrayList();
        for (Iterator it = getSiblings(id).iterator(); it.hasNext(); ) {
            XMLNode node = (XMLNode) it.next();
            result.add(node);
        }
        for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            for (Iterator jt = ((List) children.get(key)).iterator(); jt.hasNext(); ) {
                result.addAll(((XMLNode) jt.next()).getSiblingsTree(id));
            }
        }
        return result;
    }

    public static List getAttributes(List nodes, String attributeName) {
        List result = new ArrayList();
        for (Iterator it = nodes.iterator(); it.hasNext(); ) {
            XMLNode node = (XMLNode) it.next();
            result.add(node.getAttribute(attributeName));
        }
        return result;
    }

    public void clearHeader() {
        xmlHeader = "";
    }

    public void addNodes(String id, List nodes) {
        if (children.get(id) == null) {
            children.put(id, new ArrayList(nodes));
        } else {
            ((List) children.get(id)).addAll(nodes);
        }
    }

    public void addNode(String id, XMLNode node) {
        if (children.get(id) == null) {
            children.put(id, new ArrayList());
        } else {
            ((List) children.get(id)).add(node);
        }
    }

    public void overwriteSiblings(String id, List newSiblings) {
        if (children.containsKey(id)) {
            ((List) children.get(id)).clear();
            ((List) children.get(id)).addAll(newSiblings);
        } else {
            children.put(id, new ArrayList(newSiblings));
        }
    }

    public XMLNode removeSiblings(String id) {
        if (children.containsKey(id)) {
            children.remove(id);
        }
        return this;
    }

    public XMLNode removeAllSiblingsExcept(String id) {
        for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            if (!key.equals(id)) {
                it.remove();
            }
        }
        return this;
    }

    public void removeTreeSiblings(String id) {
        removeSiblings(id);
        for (Iterator it = getChildrenList().iterator(); it.hasNext(); ) {
            XMLNode child = (XMLNode) it.next();
            child.removeTreeSiblings(id);
        }
    }

    /**
     * ICM все равно тримает атрибуты при импорте, поэтому данная операция не влияет на сохранность атрибутов при импорте.
     */
    public void trimTreeAttributes() {
        Map newAttributes = new LinkedHashMap();
        for (Iterator it = attributes.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            newAttributes.put(key, ((String) attributes.get(key)).trim());
        }
        attributes = newAttributes;
        for (Iterator it = getChildrenList().iterator(); it.hasNext(); ) {
            XMLNode child = (XMLNode) it.next();
            child.trimTreeAttributes();
        }
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        if (xmlHeader != null) {
            result.append(xmlHeader);
        }
        result.append("<").append(id);
        if (!isEmpty && attributes != null && attributes.keySet().size() > 0) {
            result.append(" ");
            for (Iterator it = attributes.keySet().iterator(); it.hasNext(); ) {
                String attributeName = (String) it.next();
                String attributeValue = (String) attributes.get(attributeName);
                result.append(attributeName).append("=\"").append(attributeValue).append("\"");
                if (it.hasNext()) {
                    result.append(" ");
                }
            }
        }
        if (children != null && children.keySet().size() > 0) {
            result.append(">");
            for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
                String childNodeName = (String) it.next();
                List childrenList = (List) children.get(childNodeName);
                for (Iterator jt = childrenList.iterator(); jt.hasNext(); ) {
                    result.append("\n").append(jt.next());
                }
            }
            result.append("</").append(id).append(">\n");
        } else {
            result.append("/>\n");
        }
        return result.toString();
    }

    /**
     * Проверка что объект, описанный блоком XML поменялся, не поменялся, или является другим объектом.
     * Объект является другим, если его ID или ключевой параметр или empty таг отличается от сравниваемого.
     * Объект является поменявшимся, если он не другой, и атрибуты или дети различаются.
     * Объект является не поменявшимся, если все кроме header совпадает.
     *
     * Важно: при перегрузке XMLNode расширяющим классом, представлящим модель какого-либо объекта, описываемого блоком XML
     * необходимо также проверять изменение ключевого параметра расширяющего объекта. Ключевой параметр это свойство объекта модели, которое
     * уникальным образом идентифицирует группу объекта, в которой он может менятся (например Type.name) (@see Type#checkChanged(Changeable))
     * @param changeable
     * @return
     */
    public short checkChanged(Changeable changeable) {
        if (changeable instanceof XMLNode) {
            if (!((XMLNode) changeable).getId().equals(id)) {
                return Changeable.ANOTHER;
            }
            if (isEmpty != ((XMLNode) changeable).isEmpty) {
                return Changeable.CHANGED;
            } else if (!attributesEquals(((XMLNode) changeable).attributes)) {
                return Changeable.CHANGED;
            } else if (!childrenEquals(((XMLNode) changeable).children)) {
                return Changeable.CHANGED;
            } else {
                return Changeable.NOT_CHANGED;
            }
        }
        return Changeable.ANOTHER;
    }

    public boolean attributesEquals(Map compareAttributes) {
        if (!attributes.keySet().equals(compareAttributes.keySet())) {
            return false;
        }
        for (Iterator it = attributes.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            if (!attributes.get(key).equals(compareAttributes.get(key))) {
                return false;
            }
        }
        return true;
    }

    public boolean childrenEquals(Map compareChildren) {
        if (!children.keySet().equals(compareChildren.keySet())) {
            return false;
        }
        for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            List list1 = (List) children.get(key);
            List list2 = (List) compareChildren.get(key);
            if (list1.size() != list2.size()) {
                return false;
            }
            for (int i = 0; i < list1.size(); i++) {
                if (!list1.get(i).toString().equals(list2.get(i).toString())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XMLNode xmlNode = (XMLNode) o;
        if (isEmpty != xmlNode.isEmpty) return false;
        if (id != null ? !id.equals(xmlNode.id) : xmlNode.id != null) return false;
        if (xmlHeader != null ? !xmlHeader.equals(xmlNode.xmlHeader) : xmlNode.xmlHeader != null) return false;
        if (!this.toString().equals(o.toString())) return false;
        return true;
    }

    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (xmlHeader != null ? xmlHeader.hashCode() : 0);
        result = 31 * result + (isEmpty ? 1 : 0);
        result = 31 * result + super.hashCode();
        return result;
    }

    public Object clone() {
        XMLNode result = null;
        try {
            result = (XMLNode) this.getClass().newInstance();
            result.xmlHeader = xmlHeader;
            result.id = id;
            result.attributes = new LinkedHashMap(attributes);
            result.children = new LinkedHashMap();
            for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                List resultChildren = new ArrayList();
                result.children.put(key, resultChildren);
                for (Iterator jt = ((List) children.get(key)).iterator(); jt.hasNext(); ) {
                    XMLNode child = (XMLNode) jt.next();
                    XMLNode childClone = (XMLNode) child.clone();
                    childClone.parent = result;
                    resultChildren.add(childClone);
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Сортирует суб-ноды по id
     */
    public XMLNode sortNodes() {
        List keys = new ArrayList(children.keySet());
        Map sortedChildren = new LinkedHashMap();
        for (int i = 0; i < keys.size(); i++) {
            List childrenForKey = (List) children.get(keys.get(i));
            Collections.sort(childrenForKey, new Comparator() {

                public int compare(Object o1, Object o2) {
                    XMLNode node1 = (XMLNode) o1;
                    XMLNode node2 = (XMLNode) o2;
                    node1.sortAttributes();
                    node2.sortAttributes();
                    return o2.toString().compareTo(o1.toString());
                }
            });
            sortedChildren.put(keys.get(i), childrenForKey);
        }
        children = sortedChildren;
        for (Iterator it = getChildrenList().iterator(); it.hasNext(); ) {
            XMLNode child = (XMLNode) it.next();
            child.sortNodes();
        }
        return this;
    }

    private void sortAttributes() {
        List keys = new ArrayList(attributes.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            if (key.equals("name") && i != 0) {
                String buff = (String) keys.get(0);
                keys.set(0, keys.get(i));
                keys.set(i, buff);
            }
        }
        Map newAttributes = new LinkedHashMap();
        for (int i = 0; i < keys.size(); i++) {
            newAttributes.put(keys.get(i), attributes.get(keys.get(i)));
        }
        attributes = newAttributes;
    }

    /**
     *
     * @param substracted
     * @return
     */
    public XMLNode subtractNodes(XMLNode substracted) {
        XMLNode result = (XMLNode) this.clone();
        result.substract(substracted);
        result.purgeEmptyBranches();
        return result;
    }

    /**
     * Убирает все ветки данной ноды, которые имеют похожие ветки в отнимаемой ноде. Похожие ветки -
     * значит имеющие одинаковые атрибуты и названия ветки. Удаленные ветки заменяются пустой нодой с id="empty".
     * @param substracted
     * @return
     */
    private void substract(XMLNode substracted) {
        if (!this.getFullPath().equals(substracted.getFullPath())) {
            throw new IllegalStateException();
        }
        if (this.getId().equals(substracted.getId())) {
            if (this.getAttributes().equals(substracted.getAttributes())) {
                this.isEmpty = true;
            }
        }
        List thisChildren = getChildrenList();
        List substractedChildren = substracted.getChildrenList();
        List[] intersectionResults = nodesIntersection(thisChildren, substractedChildren);
        for (int i = 0; i < intersectionResults[1].size(); i++) {
            XMLNode[] commonNodesPair = (XMLNode[]) intersectionResults[1].get(i);
            commonNodesPair[0].substract(commonNodesPair[1]);
        }
    }

    private List[] nodesIntersection(List nodesList1, List nodesList2) {
        List uniqueForList1 = new ArrayList();
        List common = new ArrayList();
        List uniqueForList2 = new ArrayList(nodesList2);
        for (int i = 0; i < nodesList1.size(); i++) {
            XMLNode node1 = (XMLNode) nodesList1.get(i);
            boolean foundCommon = false;
            for (int j = 0; j < nodesList2.size(); j++) {
                XMLNode node2 = (XMLNode) nodesList2.get(j);
                if (node1.getFullPath().equals(node2.getFullPath())) {
                    if (node1.getAttributes().equals(node2.getAttributes())) {
                        common.add(new XMLNode[] { node1, node2 });
                        uniqueForList2.remove(node2);
                        foundCommon = true;
                        break;
                    }
                }
            }
            if (!foundCommon) {
                uniqueForList1.add(node1);
            }
        }
        return new List[] { uniqueForList1, common, uniqueForList2 };
    }

    /**
     * Находит пустые концы дерева и для каждого запускает удаление от конца до непустых нод.
     */
    private void purgeEmptyBranches() {
        List emptyEndsCollector = new ArrayList();
        findEmptyEnds(emptyEndsCollector);
        for (Iterator it = emptyEndsCollector.iterator(); it.hasNext(); ) {
            XMLNode emptyEndNode = (XMLNode) it.next();
            emptyEndNode.deleteEmptyEnds();
        }
    }

    /**
     * Находит все пустые суб-ноды и заносит их в emptyEndsCollector
     * @param emptyEndsCollector
     */
    private void findEmptyEnds(final List emptyEndsCollector) {
        for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            List childNodes = (List) children.get(key);
            for (int i = 0; i < childNodes.size(); i++) {
                XMLNode childNode = (XMLNode) childNodes.get(i);
                if (childNode.isEmpty && childNode.children.size() == 0) {
                    emptyEndsCollector.add(childNode);
                } else {
                    childNode.findEmptyEnds(emptyEndsCollector);
                }
            }
        }
    }

    /**
     * Удаляет пустые концы, при этом если все концы родителя конца пустые, то удаляет и родителя.
     */
    private void deleteEmptyEnds() {
        if (getParent() == null) {
            return;
        }
        getParent().deleteEmptyChildren();
        if (getParent().children.size() == 0) {
            if (getParent().getParent() != null) {
                getParent().getParent().deleteEmptyEnds();
            }
        }
    }

    /**
     * Удаляет пустые суб-ноды данной ноды.
     */
    private void deleteEmptyChildren() {
        for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            List childNodeList = (List) children.get(key);
            for (Iterator jt = childNodeList.iterator(); jt.hasNext(); ) {
                XMLNode childNode = (XMLNode) jt.next();
                if (childNode.isEmpty && childNode.children.size() == 0) {
                    jt.remove();
                }
            }
            if (childNodeList.size() == 0) {
                children.remove(key);
            }
        }
    }

    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }

    /**
     * Static XML utilites
     */
    private static String findFirst(String text, String regexp) {
        Matcher matcher = Pattern.compile(regexp, Pattern.DOTALL).matcher(text);
        while (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private static String normalizeXML(String xml) {
        xml = xml.trim();
        xml = xml.replaceAll("\n|\r", " ");
        xml = xml.replaceAll(" +", " ");
        xml = xml.replaceAll("> *<", "><");
        return xml;
    }

    private static Map parseAttributes(String attributesContent) {
        Map result = new LinkedHashMap();
        if (attributesContent != null && attributesContent.length() > 0) {
            List attributes = findAll(attributesContent, "[a-zA-Z0-9_:]+=\"[^\"]*\"");
            for (int i = 0; i < attributes.size(); i++) {
                String attribute = (String) attributes.get(i);
                int valueStart = attribute.indexOf("=");
                String name = attribute.substring(0, valueStart);
                String value = attribute.substring(valueStart + 1).replaceAll("^ *\"|\" *$", "");
                result.put(name, value);
            }
        }
        return result;
    }

    private static List findAll(String text, String regExp) {
        List result = new ArrayList();
        Matcher matcher = Pattern.compile(regExp, Pattern.DOTALL).matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
}

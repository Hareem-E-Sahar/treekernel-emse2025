package ee.ioc.cs.vsle.table;

import java.io.*;
import java.util.*;
import javax.swing.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import ee.ioc.cs.vsle.common.xml.*;
import ee.ioc.cs.vsle.editor.*;
import ee.ioc.cs.vsle.table.exception.*;
import ee.ioc.cs.vsle.util.*;

/** 
 * Class provides methods for parsing table xml as well as saving tables, 
 * parse() method returns a map of tables by their ids.
 * 
 * @author pavelg
 */
public class TableXmlProcessor extends AbstractXmlProcessor {

    private static final String TBL_ELEM_ENTRY = "entry";

    private static final String TBL_ELEM_RULE = "rule";

    private static final String TBL_ELEM_ROOT = "table";

    private static final String TBL_ELEM_VAR = "var";

    private static final String TBL_ELEM_DATA = "data";

    private static final String TBL_ELEM_VRULES = "vrules";

    private static final String TBL_ELEM_HRULES = "hrules";

    private static final String TBL_ELEM_OUTPUT = "output";

    private static final String TBL_ELEM_INPUT = "input";

    private static final String TBL_ELEM_DEFAULT = "default";

    private static final String TBL_ELEM_VALUE = "value";

    private static final String TBL_ELEM_CELL = "cell";

    private static final String TBL_ELEM_ROW = "row";

    private static final String TBL_ATTR_ID = "id";

    private static final String TBL_ATTR_TYPE = "type";

    private static final String TBL_ATTR_VALUE = "value";

    private static final String TBL_ATTR_COND = "cond";

    private static final String TBL_ATTR_VAR = "var";

    private static final String TBL_ATTR_ALIAS_ID = "alias_id";

    private static final String TBL_ATTR_ALIAS_TYPE = "alias_type";

    private static final String TBL_ATTR_DEFAULT = "default";

    private static final String TBL_ATTR_KIND = "kind";

    private static final String TBL_ATTR_VALUE_ALIAS = "alias";

    /**
     * @param tableFile
     */
    public TableXmlProcessor(File tableFile) {
        super(tableFile, "cocovila", "tables", RuntimeProperties.TABLE_SCHEMA);
    }

    /**
     * Saves the table in xml format
     * 
     * @param table
     */
    public void save(Table table, boolean askOnOverwrite) {
        try {
            Document document;
            if (!xmlFile.exists()) {
                xmlFile.createNewFile();
            }
            if (xmlFile.length() == 0) {
                document = createNewDocument();
            } else {
                try {
                    document = getDocument();
                } catch (Exception e) {
                    if (JOptionPane.showConfirmDialog(null, "Unable to parse table content, overwrite " + xmlFile.getName() + "?", "Error reading table file", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        collector.clearMessages();
                        document = createNewDocument();
                    } else {
                        return;
                    }
                }
            }
            Element root = document.getDocumentElement();
            NodeList tables = root.getElementsByTagNameNS(XML_NS_URI, TBL_ELEM_ROOT);
            Node existingTableNode = null;
            for (int i = 0; i < tables.getLength(); i++) {
                if (table.getTableId().equals(tables.item(i).getAttributes().getNamedItem(TBL_ATTR_ID).getNodeValue())) {
                    if (askOnOverwrite && JOptionPane.showConfirmDialog(null, "Overwrite existing table \"" + table.getTableId() + "\" in " + xmlFile.getName() + "?", "Overwrite table?", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                        return;
                    }
                    existingTableNode = tables.item(i);
                    break;
                }
            }
            Node newTableNode = createTableNode(table, document);
            if (existingTableNode != null) {
                root.replaceChild(newTableNode, existingTableNode);
            } else {
                root.appendChild(newTableNode);
            }
            validateDocument(document);
            if (!collector.hasProblems()) {
                writeDocument(document, new FileOutputStream(xmlFile));
            }
        } catch (Exception e) {
            e.printStackTrace();
            collector.collectDiagnostic(e.getClass().getName() + " " + e.getMessage(), true);
        } finally {
            checkProblems("Error saving table file " + xmlFile.getName());
        }
    }

    /**
     * Creates table node
     * 
     * @param table
     * @param document
     * @return
     */
    private Node createTableNode(Table table, Document document) {
        Element tableNode = document.createElementNS(XML_NS_URI, TBL_ELEM_ROOT);
        tableNode.setAttribute(TBL_ATTR_ID, table.getTableId());
        Element inputNode = document.createElementNS(XML_NS_URI, TBL_ELEM_INPUT);
        tableNode.appendChild(inputNode);
        for (InputTableField input : table.getInputFields()) {
            Element inputEl = createVarElement(input, document);
            inputNode.appendChild(inputEl);
            if (input.getQuestion() != null) {
                Element questionEl = document.createElementNS(XML_NS_URI, "question");
                questionEl.setTextContent(input.getQuestion());
                inputEl.appendChild(questionEl);
            }
            if (!input.getConstraints().isEmpty()) {
                Element constraintsEl = document.createElementNS(XML_NS_URI, "constraints");
                for (TableFieldConstraint constr : input.getConstraints()) {
                    if (constr instanceof TableFieldConstraint.List) {
                        TableFieldConstraint.List list = (TableFieldConstraint.List) constr;
                        Element listEl = document.createElementNS(XML_NS_URI, "list");
                        for (Object obj : list.getValueList()) {
                            Element elementEl = document.createElementNS(XML_NS_URI, "element");
                            elementEl.setAttribute(TBL_ATTR_VALUE, obj.toString());
                            listEl.appendChild(elementEl);
                        }
                        constraintsEl.appendChild(listEl);
                    } else if (constr instanceof TableFieldConstraint.Range) {
                        TableFieldConstraint.Range range = (TableFieldConstraint.Range) constr;
                        Element rangeEl = document.createElementNS(XML_NS_URI, "range");
                        if (range.getMin() != null) rangeEl.setAttribute("min", range.getMin().toString());
                        if (range.getMax() != null) rangeEl.setAttribute("max", range.getMax().toString());
                        constraintsEl.appendChild(rangeEl);
                    }
                }
                inputEl.appendChild(constraintsEl);
            }
        }
        Element outputNode = document.createElementNS(XML_NS_URI, TBL_ELEM_OUTPUT);
        tableNode.appendChild(outputNode);
        boolean isAliasOutput = table.isAliasOutput();
        if (isAliasOutput) {
            TableField alias = table.getAliasOutput();
            outputNode.setAttribute(TBL_ATTR_KIND, TBL_ATTR_VALUE_ALIAS);
            outputNode.setAttribute(TBL_ATTR_ALIAS_ID, alias.getId());
            outputNode.setAttribute(TBL_ATTR_ALIAS_TYPE, alias.getType());
        }
        for (TableField output : table.getOutputFields()) {
            outputNode.appendChild(createVarElement(output, document));
        }
        Element hRulesNode = document.createElementNS(XML_NS_URI, TBL_ELEM_HRULES);
        createRuleNodesAndAppend(table.getHRules(), document, hRulesNode);
        tableNode.appendChild(hRulesNode);
        Element vRulesNode = document.createElementNS(XML_NS_URI, TBL_ELEM_VRULES);
        createRuleNodesAndAppend(table.getVRules(), document, vRulesNode);
        tableNode.appendChild(vRulesNode);
        tableNode.appendChild(createDataNode(table, document));
        return tableNode;
    }

    /**
     * Creates var node
     * 
     * @param field
     * @param doc
     * @return
     */
    private Element createVarElement(TableField field, Document doc) {
        Element var = doc.createElementNS(XML_NS_URI, TBL_ELEM_VAR);
        var.setAttribute(TBL_ATTR_ID, field.getId());
        var.setAttribute(TBL_ATTR_TYPE, field.getType());
        if (field.getDefaultValue() != null) var.setAttribute(TBL_ATTR_DEFAULT, field.getDefaultValue().toString());
        return var;
    }

    /**
     * Creates and appends rule nodes
     * 
     * @param rules
     * @param doc
     * @param rulesNode
     */
    private void createRuleNodesAndAppend(List<Rule> rules, Document doc, Node rulesNode) {
        for (Rule rule : rules) {
            Element ruleNode = doc.createElementNS(XML_NS_URI, TBL_ELEM_RULE);
            ruleNode.setAttribute(TBL_ATTR_COND, rule.getConditionString());
            ruleNode.setAttribute(TBL_ATTR_VALUE, TypeUtil.toTokenString(rule.getValue()));
            ruleNode.setAttribute(TBL_ATTR_VAR, rule.getField().getId());
            for (Integer entry : rule.getEntries()) {
                Element entryNode = doc.createElementNS(XML_NS_URI, TBL_ELEM_ENTRY);
                entryNode.setAttribute(TBL_ATTR_ID, entry.toString());
                ruleNode.appendChild(entryNode);
            }
            rulesNode.appendChild(ruleNode);
        }
    }

    /**
     * Creates a node for the data table
     * 
     * @param tbl
     * @param doc
     * @return
     */
    private Node createDataNode(Table tbl, Document doc) {
        Element dataNode = doc.createElementNS(XML_NS_URI, TBL_ELEM_DATA);
        List<Integer> rowIds = tbl.getOrderedRowIds();
        List<Integer> colIds = tbl.getOrderedColumnIds();
        for (int i = 0, rowCount = rowIds.size(); i < rowCount; i++) {
            Element rowNode = doc.createElementNS(XML_NS_URI, TBL_ELEM_ROW);
            rowNode.setAttribute(TBL_ATTR_ID, rowIds.get(i).toString());
            for (int j = 0, colCount = colIds.size(); j < colCount; j++) {
                Element cellNode = doc.createElementNS(XML_NS_URI, TBL_ELEM_CELL);
                cellNode.setAttribute(TBL_ATTR_ID, colIds.get(j).toString());
                Map<TableField, Object> values = tbl.getCellValuesAt(i, j);
                for (TableField output : tbl.getOutputFields()) {
                    Object value = values.get(output);
                    if (value != null) {
                        Element valueEl = doc.createElementNS(XML_NS_URI, TBL_ELEM_VALUE);
                        valueEl.setAttribute(TBL_ATTR_VAR, output.getId());
                        valueEl.setTextContent(value.toString());
                        cellNode.appendChild(valueEl);
                    }
                }
                rowNode.appendChild(cellNode);
            }
            dataNode.appendChild(rowNode);
        }
        return dataNode;
    }

    /**
     * Parses the table xml from a given file
     * 
     * @param tableFile absolute path to table.xml
     * @return Map<table id, table>
     */
    @Override
    public Map<String, Table> parse() {
        Map<String, Table> tables = new LinkedHashMap<String, Table>();
        try {
            Document document = getDocument();
            Element root = document.getDocumentElement();
            NodeList list = root.getElementsByTagNameNS(XML_NS_URI, TBL_ELEM_ROOT);
            for (int i = 0; i < list.getLength(); i++) {
                Table t = createTable((Element) list.item(i));
                tables.put(t.getTableId(), t);
            }
        } catch (Exception e) {
            e.printStackTrace();
            collector.collectDiagnostic(e.getMessage(), true);
        } finally {
            checkProblems("Error parsing table file " + xmlFile.getName());
        }
        return tables;
    }

    /**
     * Iterates through nodes and parses each element using corresponding methods
     *  
     * @param tableRoot Element
     * @return Table object
     * @throws TableException
     */
    private Table createTable(Element tableRoot) throws TableException {
        Table table = new Table(tableRoot.getAttribute(TBL_ATTR_ID));
        NodeList nodes = tableRoot.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
            Element node = (Element) nodes.item(i);
            if (TBL_ELEM_INPUT.equals(node.getNodeName())) {
                Collection<InputTableField> coll = parseInputVariables(node.getElementsByTagName(TBL_ELEM_VAR));
                table.addInputFields(coll);
            } else if (TBL_ELEM_OUTPUT.equals(node.getNodeName())) {
                if (node.hasAttribute(TBL_ATTR_KIND) && node.getAttribute(TBL_ATTR_KIND).equals(TBL_ATTR_VALUE_ALIAS)) {
                    if (!node.hasAttribute(TBL_ATTR_ALIAS_ID)) throw new TableException("Id of an alias output must be specified");
                    if (!node.hasAttribute(TBL_ATTR_ALIAS_TYPE)) throw new TableException("Type of an alias output must be specified");
                    TableField alias = new TableField(node.getAttribute(TBL_ATTR_ALIAS_ID), node.getAttribute(TBL_ATTR_ALIAS_TYPE));
                    table.setAliasOutput(alias);
                }
                TableFieldList<TableField> outputs = parseOuputVariables(node.getElementsByTagName(TBL_ELEM_VAR));
                if (!table.isAliasOutput() && outputs.size() != 1) throw new TableException("The table must have single output");
                table.addOutputFields(outputs);
            } else if (TBL_ELEM_HRULES.equals(node.getNodeName())) {
                table.addHRules(parseRules(node.getElementsByTagName(TBL_ELEM_RULE), table));
            } else if (TBL_ELEM_VRULES.equals(node.getNodeName())) {
                table.addVRules(parseRules(node.getElementsByTagName(TBL_ELEM_RULE), table));
            } else if (TBL_ELEM_DATA.equals(node.getNodeName())) {
                parseData(node.getElementsByTagName(TBL_ELEM_ROW), table);
            } else if (TBL_ELEM_DEFAULT.equals(node.getNodeName())) {
                if (!table.isAliasOutput()) table.getOutputField().setDefaultValueFromString(node.getTextContent());
            }
        }
        return table;
    }

    /**
     * Parses variables (inputs and outputs)
     * 
     * @param inputsList
     * @return
     */
    private TableFieldList<InputTableField> parseInputVariables(NodeList inputsList) {
        TableFieldList<InputTableField> vars = new TableFieldList<InputTableField>();
        for (int i = 0; i < inputsList.getLength(); i++) {
            Element var = (Element) inputsList.item(i);
            InputTableField tf = new InputTableField(var.getAttribute(TBL_ATTR_ID), var.getAttribute(TBL_ATTR_TYPE));
            NodeList entries = var.getElementsByTagName("question");
            if (entries.getLength() == 1) {
                Element question = (Element) entries.item(0);
                tf.setQuestion(question.getTextContent());
            }
            entries = var.getElementsByTagName("constraints");
            if (entries.getLength() == 1) {
                Element constraints = (Element) entries.item(0);
                NodeList nodes = constraints.getChildNodes();
                List<TableFieldConstraint> constrList = new ArrayList<TableFieldConstraint>();
                for (int j = 0; j < nodes.getLength(); j++) {
                    System.out.println(nodes.item(j).getNodeName() + " -- " + nodes.item(j).getNodeType());
                    if (nodes.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
                    Element constrEl = (Element) nodes.item(j);
                    if ("list".equals(constrEl.getNodeName())) {
                        TableFieldConstraint.List list = new TableFieldConstraint.List();
                        NodeList elNodes = constrEl.getElementsByTagName("element");
                        StringBuilder sb = new StringBuilder();
                        for (int k = 0; k < elNodes.getLength(); k++) {
                            if (k > 0) sb.append("%%");
                            String value = ((Element) elNodes.item(k)).getAttribute("value");
                            sb.append(value);
                        }
                        list.setValuesFromString(tf.getType(), sb.toString());
                        constrList.add(list);
                    } else if ("range".equals(constrEl.getNodeName())) {
                        TableFieldConstraint.Range range = new TableFieldConstraint.Range();
                        range.setValuesFromString(tf.getType(), constrEl.getAttribute("min"), constrEl.getAttribute("max"));
                        constrList.add(range);
                    }
                }
                tf.setConstraints(constrList);
            }
            vars.add(tf);
        }
        return vars;
    }

    private TableFieldList<TableField> parseOuputVariables(NodeList list) {
        TableFieldList<TableField> vars = new TableFieldList<TableField>();
        for (int i = 0; i < list.getLength(); i++) {
            Element var = (Element) list.item(i);
            TableField tf = new TableField(var.getAttribute(TBL_ATTR_ID), var.getAttribute(TBL_ATTR_TYPE));
            if (var.hasAttribute(TBL_ELEM_DEFAULT)) {
                tf.setDefaultValueFromString(var.getAttribute(TBL_ELEM_DEFAULT));
            }
            vars.add(tf);
        }
        return vars;
    }

    /**
     * Parses rules (horizontal and vertical)
     * 
     * @param ruleNodes
     * @param table
     * @return
     * @throws TableException
     */
    private Set<Rule> parseRules(NodeList ruleNodes, Table table) throws TableException {
        Set<Rule> rules = new LinkedHashSet<Rule>();
        for (int j = 0; j < ruleNodes.getLength(); j++) {
            Element ruleNode = (Element) ruleNodes.item(j);
            Rule rule;
            try {
                rule = Rule.createRule(table.getInput(ruleNode.getAttribute(TBL_ATTR_VAR)), ruleNode.getAttribute(TBL_ATTR_COND), ruleNode.getAttribute(TBL_ATTR_VALUE));
            } catch (Exception e) {
                throw new TableException(e);
            }
            rules.add(rule);
            NodeList entries = ruleNode.getElementsByTagName(TBL_ELEM_ENTRY);
            for (int k = 0; k < entries.getLength(); k++) {
                Element entry = (Element) entries.item(k);
                rule.addEntry(Integer.parseInt(entry.getAttribute(TBL_ATTR_ID)));
            }
        }
        return rules;
    }

    /**
     * Parses data table
     * 
     * @param dataNodes
     * @param table
     * @throws TableException
     */
    private void parseData(NodeList dataNodes, Table table) throws TableException {
        for (int j = 0; j < dataNodes.getLength(); j++) {
            Element row = (Element) dataNodes.item(j);
            int rowId = Integer.parseInt(row.getAttribute(TBL_ATTR_ID));
            NodeList cells = row.getElementsByTagName(TBL_ELEM_CELL);
            for (int k = 0; k < cells.getLength(); k++) {
                Element cell = (Element) cells.item(k);
                int colId = Integer.parseInt(cell.getAttribute(TBL_ATTR_ID));
                Map<String, String> valueMap = new HashMap<String, String>();
                NodeList valueNodes = cell.getElementsByTagName(TBL_ELEM_VALUE);
                if (valueNodes.getLength() == 0) {
                    String val = cell.getTextContent();
                    if (val != null && val.length() > 0) {
                        valueMap.put(table.getOutputField().getId(), val);
                    }
                } else {
                    for (int i = 0; i < valueNodes.getLength(); i++) {
                        Element valueNode = (Element) valueNodes.item(i);
                        if (valueNode.getChildNodes().getLength() != 0) valueMap.put(valueNode.getAttribute(TBL_ATTR_VAR), valueNode.getTextContent());
                    }
                }
                table.addDataCell(rowId, colId, valueMap);
            }
        }
    }

    @Override
    protected void reportError(String message) {
        throw new TableException(message);
    }

    @Override
    protected EntityResolver getEntityResolver() {
        return null;
    }
}

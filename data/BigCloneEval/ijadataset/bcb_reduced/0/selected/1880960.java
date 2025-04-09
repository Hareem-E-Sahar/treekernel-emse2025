package org.omwg.mediation.parser.alignment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.omwg.mediation.language.objectmodel.api.AttributeCondition;
import org.omwg.mediation.language.objectmodel.api.ClassCondition;
import org.omwg.mediation.language.objectmodel.api.ComplexExpression;
import org.omwg.mediation.language.objectmodel.api.Expression;
import org.omwg.mediation.language.objectmodel.api.Id;
import org.omwg.mediation.language.objectmodel.api.MappingDoc;
import org.omwg.mediation.language.objectmodel.api.MappingRule;
import org.omwg.mediation.language.objectmodel.api.comparators.BinaryComparator;
import org.omwg.mediation.language.objectmodel.api.comparators.Comparator;
import org.omwg.mediation.language.objectmodel.api.comparators.Equal;
import org.omwg.mediation.language.objectmodel.api.comparators.UnaryComprator;
import org.omwg.mediation.language.objectmodel.api.datatypes.AtomicType;
import org.omwg.mediation.language.objectmodel.impl.AnnotationImpl;
import org.omwg.mediation.language.objectmodel.impl.AttributeExpr;
import org.omwg.mediation.language.objectmodel.impl.AttributeOccurenceCondition;
import org.omwg.mediation.language.objectmodel.impl.AttributeTypeCondition;
import org.omwg.mediation.language.objectmodel.impl.AttributeValueCondition;
import org.omwg.mediation.language.objectmodel.impl.ClassExpr;
import org.omwg.mediation.language.objectmodel.impl.ComplexAttributeExpression;
import org.omwg.mediation.language.objectmodel.impl.ComplexExpressionImpl;
import org.omwg.mediation.language.objectmodel.impl.IRI;
import org.omwg.mediation.language.objectmodel.impl.InstanceExpr;
import org.omwg.mediation.language.objectmodel.impl.MappingDocument;
import org.omwg.mediation.language.objectmodel.impl.MappingRuleImpl;
import org.omwg.mediation.language.objectmodel.impl.NamespaceDeclarationImpl;
import org.omwg.mediation.language.objectmodel.impl.TypeCondition;
import org.omwg.mediation.language.objectmodel.impl.ValueCondition;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NewParser {

    private MappingDoc doc;

    private final DocumentBuilder BUILDER;

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    private final Map<Class, Constructor> EXPR_CONSTRUCTORCACHE = new HashMap<Class, Constructor>();

    private static final String RULEIDPREFIX = "MappingRule_";

    public NewParser() throws ParserConfigurationException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setValidating(false);
        fac.setNamespaceAware(false);
        BUILDER = fac.newDocumentBuilder();
        resetDocument();
    }

    public MappingDoc parse(final Document dom) throws XPathExpressionException {
        final Node root = dom.getDocumentElement();
        final Node alignment = (Node) XPATH.evaluate("//Alignment", dom, XPathConstants.NODE);
        final NodeList cells;
        Node tempNode = null;
        NamedNodeMap attrs = null;
        attrs = root.getAttributes();
        for (int iCounter = 0; iCounter < attrs.getLength(); iCounter++) {
            tempNode = attrs.item(iCounter);
            if (tempNode.getNodeName().equals("xmlns")) {
                doc.addAnnotation(new NamespaceDeclarationImpl(new IRI(tempNode.getNodeValue()), ":"));
            } else if (tempNode.getNodeName().startsWith("xmlns")) {
                doc.addAnnotation(new NamespaceDeclarationImpl(new IRI(tempNode.getNodeValue()), tempNode.getNodeName().substring(6) + ":"));
            }
        }
        final String sId = (String) XPATH.evaluate("@id", alignment, XPathConstants.STRING);
        if ((sId != null) && (!sId.equals(""))) {
            doc.setId(new IRI(sId));
        } else {
            throw new IllegalArgumentException("A id for the alignment must be given");
        }
        final String sLevel = (String) XPATH.evaluate("level/text()", alignment, XPathConstants.STRING);
        final String sType = (String) XPATH.evaluate("type/text()", alignment, XPathConstants.STRING);
        final String sSourceOnto = (String) XPATH.evaluate("onto1/text()", alignment, XPathConstants.STRING);
        final String sTargetOnto = (String) XPATH.evaluate("onto2/text()", alignment, XPathConstants.STRING);
        final String sDate = (String) XPATH.evaluate("date/text()", alignment, XPathConstants.STRING);
        final String sDescription = (String) XPATH.evaluate("description/text()", alignment, XPathConstants.STRING);
        final String sCreator = (String) XPATH.evaluate("creator/text()", alignment, XPathConstants.STRING);
        if ((sTargetOnto != null) && (!sTargetOnto.equals(""))) {
            doc.setTarget(new IRI(sTargetOnto));
        } else {
            throw new IllegalArgumentException("A target ontology must be given");
        }
        if ((sSourceOnto != null) && (!sSourceOnto.equals(""))) {
            doc.setSource(new IRI(sSourceOnto));
        } else {
            throw new IllegalArgumentException("A source ontology must be given");
        }
        if ((sType != null) && (!sType.equals(""))) {
            doc.setType(sType);
        }
        if ((sLevel != null) && (!sLevel.equals(""))) {
            doc.setLevel(sLevel);
        }
        if ((sCreator != null) && (!sCreator.equals(""))) {
            doc.addAnnotation(new AnnotationImpl(new IRI(Namespace.DUBLIN_CORE.getShortCut() + ":creator"), sCreator));
        }
        if ((sDate != null) && (!sDate.equals(""))) {
            doc.addAnnotation(new AnnotationImpl(new IRI(Namespace.DUBLIN_CORE.getShortCut() + ":date"), sDate));
        }
        if ((sDescription != null) && (!sDescription.equals(""))) {
            doc.addAnnotation(new AnnotationImpl(new IRI(Namespace.DUBLIN_CORE.getShortCut() + ":description"), sDescription));
        }
        cells = (NodeList) XPATH.evaluate("map/Cell", alignment, XPathConstants.NODESET);
        for (int iCellCount = 0; iCellCount < cells.getLength(); iCellCount++) {
            tempNode = cells.item(iCellCount);
            final MappingRule rule;
            final String sSource = (String) XPATH.evaluate("entity1/@resource", tempNode, XPathConstants.STRING);
            final String sTarget = (String) XPATH.evaluate("entity2/@resource", tempNode, XPathConstants.STRING);
            final float fMeasure = Float.parseFloat((String) XPATH.evaluate("measure/text()", tempNode, XPathConstants.STRING));
            final String sRelation = (String) XPATH.evaluate("relation/text()", tempNode, XPathConstants.STRING);
            final RuleType rel = RuleType.getMapByName(sRelation);
            final String sLabel = (String) XPATH.evaluate("label/text()", tempNode, XPathConstants.STRING);
            final String ruleId = (String) XPATH.evaluate("@id", tempNode, XPathConstants.STRING);
            rule = new MappingRuleImpl(rel);
            if ((ruleId != null) && (!ruleId.equals(""))) {
                rule.setId(new IRI(ruleId));
            } else {
                rule.setId(new IRI(RULEIDPREFIX + iCellCount));
            }
            if (sSource.equals("")) {
                rule.setSource(getComplexRules((Node) XPATH.evaluate("entity1", tempNode, XPathConstants.NODE)));
            } else {
                rule.setSource(getExpressionForClass(rel.getSourceClass(), sSource));
            }
            if (sTarget.equals("")) {
                rule.setTarget(getComplexRules((Node) XPATH.evaluate("entity2", tempNode, XPathConstants.NODE)));
            } else {
                rule.setTarget(getExpressionForClass(rel.getTargetClass(), sTarget));
            }
            if ((sLabel != null) && !sLabel.equals("")) {
                rule.addAnnotation(new AnnotationImpl(new IRI(Namespace.DUBLIN_CORE.getShortCut() + ":description"), sLabel));
            }
            rule.setMeasure(fMeasure);
            doc.addRule(rule);
        }
        return doc;
    }

    protected Expression getComplexRules(final Node node) throws XPathExpressionException {
        final Node root = (Node) XPATH.evaluate("*", node, XPathConstants.NODE);
        final Expression expr;
        if (root.getNodeName().endsWith("Class")) {
            expr = getClassExpression(root);
        } else if (root.getNodeName().endsWith("Attribute")) {
            expr = getAttributeExpression(root);
        } else if (root.getNodeName().endsWith("Relation")) {
            throw new IllegalArgumentException("asdf");
        } else if (root.getNodeName().endsWith("Instance")) {
            expr = new InstanceExpr(new IRI((String) XPATH.evaluate("@about", root, XPathConstants.STRING)));
        } else {
            throw new IllegalArgumentException("Couldn't find a matching parsing method for the node: " + node.getNodeName());
        }
        return expr;
    }

    protected Expression getAttributeExpression(final Node node) throws XPathExpressionException {
        final Expression expr;
        final String sExId = (String) XPATH.evaluate("@about", node, XPathConstants.STRING);
        if ((sExId != null) && (sExId.equals(""))) {
            expr = getAttributeConstruct(node);
        } else {
            AttributeExpr ae = new AttributeExpr(new IRI(sExId));
            ae.setTransformationURI(getAttributeTransformation(node));
            expr = ae;
        }
        if (!expr.isComplexExpression()) {
            expr.addCondition(getAttributeConditions(node));
        }
        return expr;
    }

    protected List<AttributeCondition> getAttributeConditions(final Node node) throws XPathExpressionException {
        final List<AttributeCondition> conditions = new ArrayList<AttributeCondition>();
        NodeList tempList = (NodeList) XPATH.evaluate("valueCondition", node, XPathConstants.NODESET);
        for (int iConditionCount = 0; iConditionCount < tempList.getLength(); iConditionCount++) {
            Restriction res = Restriction.parseRestrictionNode((Node) XPATH.evaluate("Restriction", tempList.item(iConditionCount), XPathConstants.NODE));
            AttributeCondition cond = new ValueCondition(res.getComparator());
            conditions.add(cond);
        }
        tempList = (NodeList) XPATH.evaluate("typeCondition", node, XPathConstants.NODESET);
        for (int iConditionCount = 0; iConditionCount < tempList.getLength(); iConditionCount++) {
            Restriction res = Restriction.parseRestrictionNode((Node) XPATH.evaluate("Restriction", tempList.item(iConditionCount), XPathConstants.NODE));
            AttributeCondition cond = new TypeCondition((Equal) res.getComparator());
            conditions.add(cond);
        }
        return conditions;
    }

    protected Expression getAttributeConstruct(final Node node) throws XPathExpressionException {
        List<Expression> expressions = new ArrayList<Expression>();
        NodeList list = (NodeList) XPATH.evaluate("*", node, XPathConstants.NODESET);
        final Operator op;
        Operator tmp = null;
        try {
            tmp = Operator.getOperatorByString(node.getNodeName());
        } finally {
            op = tmp;
        }
        for (int iCounter = 0; iCounter < list.getLength(); iCounter++) {
            expressions.add(getAttributeExpression(list.item(iCounter)));
        }
        if (((op == Operator.AND) || (op == Operator.OR)) && (expressions.size() < 2)) {
            throw new IllegalArgumentException("In a 'or' or 'and' AttributeConstruct must " + "be more than 2 AttributeExpressions");
        } else if ((op == Operator.NOT) && (expressions.size() < 1)) {
            throw new IllegalArgumentException("In a 'not' AttributeConstruct " + "must be more than 1 AttributeExpressions");
        }
        if (expressions.size() == 1 && (op == null)) {
            AttributeExpr ae = (AttributeExpr) expressions.get(0);
            ae.setTransformationURI(getAttributeTransformation(node));
            return ae;
        }
        ComplexAttributeExpression cae = new ComplexAttributeExpression(op);
        cae.addSubexpression(expressions);
        cae.setTransformationURI(getAttributeTransformation(node));
        return cae;
    }

    protected Id getAttributeTransformation(final Node node) throws XPathExpressionException {
        if (node == null) {
            return null;
        }
        final String sUri = (String) XPATH.evaluate("transf/@resource | service/@resource", node, XPathConstants.STRING);
        if ((sUri != null) && (!sUri.equals(""))) {
            return new IRI(sUri);
        }
        return null;
    }

    protected Expression getClassExpression(final Node node) throws XPathExpressionException {
        final Expression expr;
        final String sExId = (String) XPATH.evaluate("@about", node, XPathConstants.STRING);
        if ((sExId != null) && (sExId.equals(""))) {
            expr = getClassConstuct(node);
        } else {
            expr = new ClassExpr(new IRI(sExId));
        }
        if (!expr.isComplexExpression()) {
            expr.addCondition(getClassConditions(node));
        }
        return expr;
    }

    @SuppressWarnings("unchecked")
    protected List<ClassCondition> getClassConditions(final Node node) throws XPathExpressionException {
        final List<ClassCondition> conditions = new ArrayList<ClassCondition>();
        NodeList tempList = (NodeList) XPATH.evaluate("attributeValueCondition", node, XPathConstants.NODESET);
        for (int iConditionCount = 0; iConditionCount < tempList.getLength(); iConditionCount++) {
            Restriction res = Restriction.parseRestrictionNode((Node) XPATH.evaluate("Restriction", tempList.item(iConditionCount), XPathConstants.NODE));
            ClassCondition cond = new AttributeValueCondition(new IRI(res.getProperty()), res.getComparator());
            conditions.add(cond);
        }
        tempList = (NodeList) XPATH.evaluate("attributeTypeCondition", node, XPathConstants.NODESET);
        for (int iConditionCount = 0; iConditionCount < tempList.getLength(); iConditionCount++) {
            Restriction res = Restriction.parseRestrictionNode((Node) XPATH.evaluate("Restriction", tempList.item(iConditionCount), XPathConstants.NODE));
            ClassCondition cond = new AttributeTypeCondition(new IRI(res.getProperty()), (Equal) res.getComparator());
            conditions.add(cond);
        }
        tempList = (NodeList) XPATH.evaluate("attributeOccurenceCondition", node, XPathConstants.NODESET);
        for (int iConditionCount = 0; iConditionCount < tempList.getLength(); iConditionCount++) {
            Restriction res = Restriction.parseRestrictionNode((Node) XPATH.evaluate("Restriction", tempList.item(iConditionCount), XPathConstants.NODE));
            ClassCondition cond = new AttributeOccurenceCondition(new IRI(res.getProperty()), (BinaryComparator) res.getComparator());
            conditions.add(cond);
        }
        return conditions;
    }

    protected Expression getClassConstuct(final Node node) throws XPathExpressionException {
        List<Expression> expressions = new ArrayList<Expression>();
        NodeList list = (NodeList) XPATH.evaluate("*", node, XPathConstants.NODESET);
        final Operator op;
        Operator tmp = null;
        try {
            tmp = Operator.getOperatorByString(node.getNodeName());
        } catch (Exception e) {
        } finally {
            op = tmp;
        }
        for (int iCounter = 0; iCounter < list.getLength(); iCounter++) {
            expressions.add(getClassExpression(list.item(iCounter)));
        }
        if (((op == Operator.AND) || (op == Operator.OR)) && (expressions.size() < 2)) {
            throw new IllegalArgumentException("In a 'or' or 'and' ClassConstruct must be more than 2 ClassExpressions");
        } else if ((op == Operator.NOT) && (expressions.size() < 1)) {
            throw new IllegalArgumentException("In a 'not' ClassConstruct must be more than 1 ClassExpressions");
        }
        if (expressions.size() == 1 && (op == null)) {
            return expressions.get(0);
        }
        ComplexExpression ce = new ComplexExpressionImpl(op, ExpressionType.CLASS);
        ce.addSubexpression(expressions);
        return ce;
    }

    public void parse(final File file) throws SAXException, IOException, XPathExpressionException {
        parse(BUILDER.parse(file));
    }

    public void parse(final String file) throws XPathExpressionException, SAXException, IOException {
        parse(new File(file));
    }

    /**
	 * This is a helpermethod for getting Expressions (AttributeExpr, ClassExpr,
	 * InstanceExpr, RelationExpr) for the given class and iri string. The given
	 * clazz must have an constructor which takes a single Id object, otherwise
	 * an IllegalArgumentException will be thrown. The constructors will be
	 * cached in a HashMap.
	 * 
	 * @param clazz
	 *            should extend the abstract id class
	 * @param sId
	 *            the id of the IRI of the Expression object
	 * @return the constructed Expression
	 * @throws IllegalArgumentException
	 *             if there is an error while creating the object, or gathering
	 *             the constructor
	 */
    protected Expression getExpressionForClass(final Class clazz, final String sId) throws IllegalArgumentException {
        Constructor constr = EXPR_CONSTRUCTORCACHE.get(clazz);
        try {
            if (constr != null) {
                Object[] obj = { new IRI(sId) };
                return (Expression) constr.newInstance(obj);
            } else {
                Class[] params = { Id.class };
                constr = clazz.getConstructor(params);
                if (constr != null) {
                    EXPR_CONSTRUCTORCACHE.put(clazz, constr);
                    return getExpressionForClass(clazz, sId);
                } else {
                    throw new IllegalArgumentException("Can't find a Constructor for the class: " + clazz.getName());
                }
            }
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
	 * Determines which comperator class from the ComperatorMapping should be
	 * returned for the given text (representation) and the given argument
	 * types. For checking against the parameter types of a binary comperator
	 * class the clazzes[0] and clazzes[1] will be taken for the
	 * setFirstArgument and setSecondArgument methods. For checking against a
	 * UnaryComperator only clazzes[0] will be recognized.
	 * 
	 * @param sText
	 *            the representation of the comperator
	 * @param clazzes
	 *            the parameter types
	 * @return the class representing the comperator
	 * @throws IllegalArgumentException
	 *             if no comerator was found.
	 */
    protected static ComparatorMapping getComparatorMapping(final String sText, final Class... clazzes) {
        for (ComparatorMapping mapping : ComparatorMapping.values()) {
            if (mapping.getPattern().matcher(sText).matches()) {
                Class comp = mapping.getClazz();
                if (getAllInterfaces(comp).contains(BinaryComparator.class)) {
                    try {
                        if (clazzes[0] != null) {
                            comp.getMethod("setFirstArgument", clazzes[0]);
                        }
                        if (clazzes[1] != null) {
                            comp.getMethod("setSecondArgument", clazzes[1]);
                        }
                        return mapping;
                    } catch (SecurityException e) {
                    } catch (NoSuchMethodException e) {
                    }
                } else if (getAllInterfaces(comp).contains(UnaryComprator.class)) {
                    try {
                        if (clazzes[0] != null) {
                            comp.getMethod("setArgument", clazzes[0]);
                        }
                        return mapping;
                    } catch (SecurityException e) {
                    } catch (NoSuchMethodException e) {
                    }
                }
            }
        }
        throw new IllegalArgumentException("No ComperatorClass found for " + sText);
    }

    protected static List<Class> getAllInterfaces(final Class source) {
        List<Class> list = new ArrayList<Class>();
        if (source != null) {
            getAllInterfaces(source, list);
        }
        return list;
    }

    protected static List<Class> getAllInterfaces(final Class source, final List<Class> list) {
        if (source != null) {
            for (Class clazz : source.getInterfaces()) {
                list.add(clazz);
                getAllInterfaces(clazz, list);
            }
        }
        return list;
    }

    public MappingDoc getDocument() {
        return doc;
    }

    public void resetDocument() {
        doc = new MappingDocument();
    }

    private static class Restriction {

        private final String property;

        private final Comparator comparator;

        private Restriction(final String property, final Comparator comparator) {
            this.property = property;
            this.comparator = comparator;
        }

        @SuppressWarnings("unchecked")
        public static Restriction parseRestrictionNode(final Node node) throws XPathExpressionException {
            String sProp = parsePath((Node) XPATH.evaluate("property", node, XPathConstants.NODE));
            String sValue = parsePath((Node) XPATH.evaluate("value", node, XPathConstants.NODE));
            if ((sValue == null) || sValue.equals("")) {
                sValue = (String) XPATH.evaluate("value/text()", node, XPathConstants.STRING);
            }
            final DatatypeMapping datatypeMapping;
            final String sDatatype = (String) XPATH.evaluate("value/@datatype", node, XPathConstants.STRING);
            if ((sDatatype != null) && !sDatatype.equals("")) {
                datatypeMapping = DatatypeMapping.determineDataType(sDatatype);
            } else {
                datatypeMapping = null;
            }
            ComparatorMapping comparatorMapping = null;
            final String sComparator = (String) XPATH.evaluate("comparator/@resource", node, XPathConstants.STRING);
            if ((sComparator != null) && (!sComparator.equals("")) && (datatypeMapping != null)) {
                for (Class clazz : getAllInterfaces(datatypeMapping.getClazz())) {
                    try {
                        comparatorMapping = getComparatorMapping(sComparator, null, clazz);
                        break;
                    } catch (IllegalArgumentException e) {
                    }
                }
                if (comparatorMapping == null) {
                    throw new IllegalArgumentException("Can't find a ComparatorMapping for " + sComparator + " and " + datatypeMapping);
                }
            } else if ((sComparator != null) && (!sComparator.equals(""))) {
                comparatorMapping = ComparatorMapping.getComperatorByString(sComparator);
            } else {
                throw new IllegalArgumentException("A comperator is needed for every restriction");
            }
            Comparator comp;
            if (UnaryComprator.class.isAssignableFrom(comparatorMapping.getClazz())) {
                try {
                    comp = (Comparator) comparatorMapping.getClazz().newInstance();
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            } else if (BinaryComparator.class.isAssignableFrom(comparatorMapping.getClazz())) {
                try {
                    BinaryComparator bc = (BinaryComparator) comparatorMapping.getClazz().newInstance();
                    AtomicType at = (AtomicType) datatypeMapping.getClazz().newInstance();
                    at.parse(sValue);
                    bc.setSecondArgument(at);
                    comp = bc;
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                throw new IllegalArgumentException("Comparator type unknown (must be Unary or Binary): " + comparatorMapping.getClazz().getName());
            }
            return new Restriction(sProp, comp);
        }

        protected static String parsePath(final Node node) throws XPathExpressionException {
            String sVal = (String) XPATH.evaluate("Attribute/@about", node, XPathConstants.STRING);
            if ((sVal == null) || (sVal.equals(""))) {
                sVal = (String) XPATH.evaluate("Path/@resource", node, XPathConstants.STRING);
            } else if ((sVal == null) || (sVal.equals(""))) {
                sVal = "";
            }
            return sVal;
        }

        public String toString() {
            return this.getClass().getName() + "(property=" + getProperty() + ",comparator=" + getComparator() + ")";
        }

        public Comparator getComparator() {
            return comparator;
        }

        public String getProperty() {
            return property;
        }
    }
}

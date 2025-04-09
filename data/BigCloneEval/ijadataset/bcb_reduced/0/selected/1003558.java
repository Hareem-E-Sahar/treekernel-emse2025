package com.divosa.eformulieren.toolkit.renderer;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;
import com.divosa.eformulieren.core.cache.ToolkitCache;
import com.divosa.eformulieren.core.service.FormService;
import com.divosa.eformulieren.core.service.WidgetService;
import com.divosa.eformulieren.core.spring.SpringApplicationContext;
import com.divosa.eformulieren.domain.domeinobject.Scheme;
import com.divosa.eformulieren.domain.domeinobject.Widget;
import com.divosa.eformulieren.domain.domeinobject.WidgetAttribute;
import com.divosa.eformulieren.domain.domeinobject.WidgetStruct;
import com.divosa.eformulieren.domain.domeinobject.WidgetType;
import com.divosa.eformulieren.domain.util.XPath;
import com.divosa.eformulieren.toolkit.exception.ToolkitRendererException;
import com.divosa.eformulieren.util.StringUtil;
import com.divosa.eformulieren.util.helper.FileHelper;
import com.divosa.security.exception.AuthenticationException;
import com.divosa.security.exception.ObjectNotFoundException;
import com.divosa.security.service.SecurityService;

public abstract class ViewRenderer {

    protected final Logger LOGGER = Logger.getLogger(this.getClass());

    protected static final String XHTML_PREFIX = "xhtml:";

    protected static final String OPEN_START_TAG = "<";

    protected static final String CLOSE_TAG = ">";

    protected static final String CLOSE_TAG_NO_FILL = "/>";

    protected static final String OPEN_END_TAG = "</";

    protected static final String TAG_HTML = "html";

    protected static final String TAG_HEAD = "head";

    protected static final String TAG_BODY = "body";

    protected static final String TAG_TITLE = "title";

    protected static final String TAG_TABLE = "table";

    protected static final String TAG_TR = "tr";

    protected static final String TAG_TD = "td";

    protected static final String TAG_DIV = "div";

    protected static final String TAG_P = "p";

    protected static final String TAG_PANE = "pane";

    /**
     * Implement this method to return the right Scheme object.
     * @return
     */
    protected abstract Scheme getScheme();

    private WidgetService widgetService;

    private FormService formService;

    private SecurityService securityService;

    protected WidgetService getWidgetService() {
        if (widgetService == null) {
            widgetService = (WidgetService) SpringApplicationContext.getBean("widgetService");
        }
        return widgetService;
    }

    protected FormService getFormService() {
        if (formService == null) {
            formService = (FormService) SpringApplicationContext.getBean("formService");
        }
        return formService;
    }

    protected SecurityService getSecurityService() {
        if (securityService == null) {
            securityService = (SecurityService) SpringApplicationContext.getBean("securityService");
        }
        return securityService;
    }

    /**
     * Replace the specified placeholder with the specified file (filename) in the specified source.
     * 
     * @param placeholder the placeholder to be replaced
     * @param filename the name of the file to be inserted in the place of the placeholder
     * @param source the source to replace the placeholder in
     * @throws ToolkitRendererException the exception thrown if anything went wrong
     */
    protected void replacePlaceholderWithStringFromFile(String placeholder, String filename, StringBuilder source) throws ToolkitRendererException {
        String string = loadStringFromFileOnClasspath(filename);
        replace(placeholder, string, source);
    }

    protected String loadStringFromFileOnClasspath(String filename) throws ToolkitRendererException {
        String string = null;
        try {
            string = FileHelper.loadStringFromFileOnClasspath(filename);
        } catch (FileNotFoundException e) {
            String message = "Error loading file " + filename + " from the classpath";
            LOGGER.error(message + ": " + e.getMessage());
            throw new ToolkitRendererException(message + ": " + e.getMessage(), e);
        }
        return string;
    }

    /**
     * Replace the specified oldStr with the specified newStr in the specified sb.
     * 
     * @param oldStr the string to be replaced
     * @param newStr the string to insert
     * @param sb the source
     */
    protected void replace(String oldStr, String newStr, StringBuilder sb) {
        if (oldStr != null && newStr != null) {
            while (sb.indexOf(oldStr) > -1) {
                sb.replace(sb.indexOf(oldStr), sb.indexOf(oldStr) + oldStr.length(), newStr);
            }
        }
    }

    /**
     * Get the object model, thus the complete form or the upper WidgetStruct having a child Widget of type WidgetType.FORM.
     * The object model is cached in ToolkitCache.
     * 
     * @return the object model, thus the complete form
     * @throws AuthenticationException
     */
    protected WidgetStruct getObjectModel() throws ToolkitRendererException, AuthenticationException {
        WidgetStruct objectModel = null;
        WidgetStruct activeWidgetStruct = ToolkitCache.getActiveWidgetStruct(getCacheKey());
        if (activeWidgetStruct != null) {
            objectModel = activeWidgetStruct;
        } else {
            throw new ToolkitRendererException("No object model set. Class is " + this.getClass().getSimpleName() + "!");
        }
        return objectModel;
    }

    /**
     * Get the key for caching. This key is a concatenation of the id of the logged in user and the session id.
     * 
     * @return the key for caching
     * @throws AuthenticationException
     */
    protected String getCacheKey() throws AuthenticationException {
        Long id = getSecurityService().getLoggedInApplicationUser().getUser().getId();
        String sessionId = getSecurityService().getSessionIdOfLoggedInApplicationUser();
        String key = new StringBuilder().append(id).append("_").append(sessionId).toString();
        return key;
    }

    /**
     * Get the String representation of the object model, thus the complete form or the upper WidgetStruct having a child
     * Widget of type WidgetType.FORM. The object model should be cached and if not found, a ToolkitRendererException is
     * thrown.
     * 
     * @return the object model, thus the complete form
     * @throws AuthenticationException
     */
    protected String getObjectModelAsString() throws ToolkitRendererException, AuthenticationException {
        String objectModelAsString = null;
        String activeWidgetStructAsString = (String) ToolkitCache.getActiveWidgetStructAsString(getCacheKey());
        if (activeWidgetStructAsString != null) {
            objectModelAsString = activeWidgetStructAsString;
        } else {
            String message = "No Document representation for the object model set. Class is " + this.getClass().getSimpleName() + "!";
            LOGGER.error(message);
            throw new ToolkitRendererException(message);
        }
        return objectModelAsString;
    }

    /**
     * @param objectModel the objectModel to set
     */
    protected StringBuilder getStartTag(String prefix, String tag, String[] attributes, String[] values) {
        StringBuilder sb = new StringBuilder(OPEN_START_TAG);
        Set<WidgetAttribute> widgetAttributesFormFrame = new HashSet<WidgetAttribute>();
        for (int i = 0; i < attributes.length; i++) {
            WidgetAttribute wa = new WidgetAttribute(attributes[i], values[i]);
            widgetAttributesFormFrame.add(wa);
        }
        sb.append(getTag(prefix, tag, widgetAttributesFormFrame));
        sb.append(CLOSE_TAG);
        return sb;
    }

    protected StringBuilder getStartTag(String prefix, String tag, Set<WidgetAttribute> widgetAttributes) {
        StringBuilder sb = new StringBuilder(OPEN_START_TAG);
        sb.append(getTag(prefix, tag, widgetAttributes));
        sb.append(CLOSE_TAG);
        return sb;
    }

    protected StringBuilder getStartTagNoFill(String prefix, String tag, Set<WidgetAttribute> widgetAttributes) {
        StringBuilder sb = new StringBuilder(OPEN_START_TAG);
        sb.append(getTag(prefix, tag, widgetAttributes));
        sb.append(CLOSE_TAG_NO_FILL);
        return sb;
    }

    protected StringBuilder getEndTag(String prefix, String tag) {
        StringBuilder sb = new StringBuilder(OPEN_END_TAG);
        sb.append(getTag(prefix, tag, null));
        sb.append(CLOSE_TAG);
        return sb;
    }

    protected StringBuilder getTag(String prefix, String tag, Set<WidgetAttribute> widgetAttributes) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
        }
        sb.append(tag);
        if (widgetAttributes != null) {
            for (WidgetAttribute widgetAttribute : widgetAttributes) {
                sb.append(" ").append(widgetAttribute.getName()).append("=\"").append(widgetAttribute.getValue()).append("\"");
            }
        }
        return sb;
    }

    /**
     * Get the first WidgetStruct with the specified widgetType and a child Widget that contains the WidgetAttribute objects
     * that correspond with the WidgetAttribute objects as delivered in the specified list when looking from parent
     * WidgetStruct to parent WidgetStruct.
     * 
     * @param widgetStruct the WidgetStruct to search the parent WidgetStruct for
     * @param widgetType the WidgetType of the parent WidgetStruct to search for
     * @param childWidgetAttributes the WidgetAttribute objects that must be present in the parent WidgetStruct to search for
     * @return the parent (first containing) WidgetStruct with the specified WidgetType and the specified WidgetAttribute
     * objects for the specified WidgetStruct or null if no parent WidgetStruct is found
     */
    protected WidgetStruct getContainingParentWidgetStruct(WidgetStruct widgetStruct, WidgetType widgetType, List<WidgetAttribute> childWidgetAttributes) {
        boolean parentWidgetStructFound = true;
        WidgetStruct parentWidgetStruct = null;
        parentWidgetStruct = getContainingParentWidgetStruct(widgetStruct, widgetType);
        if (parentWidgetStruct != null) {
            Set<WidgetAttribute> childWidgetWidgetAttributes = parentWidgetStruct.getChildWidget().getWidgetAttributes();
            if (childWidgetAttributes != null && !childWidgetAttributes.isEmpty()) {
                for (WidgetAttribute childWidgetAttribute : childWidgetAttributes) {
                    if (!childWidgetWidgetAttributes.contains(childWidgetAttribute)) {
                        parentWidgetStructFound = false;
                        break;
                    }
                }
            }
        } else {
            return parentWidgetStruct;
        }
        if (parentWidgetStructFound) {
            return parentWidgetStruct;
        } else {
            return getContainingParentWidgetStruct(parentWidgetStruct, widgetType, childWidgetAttributes);
        }
    }

    /**
     * Get the first WidgetStruct with a child Widget with the specified widgetType when looking from parent WidgetStruct to
     * parent WidgetStruct.
     * 
     * @param widgetStruct the WidgetStruct to search the parent WidgetStruct for
     * @param widgetType the WidgetType of the parent WidgetStruct to search for
     * @return the parent (first containing) WidgetStruct with the specified WidgetType for the specified WidgetStruct or
     * null if no parent WidgetStruct is found
     */
    protected WidgetStruct getContainingParentWidgetStruct(WidgetStruct widgetStruct, WidgetType widgetType) {
        WidgetStruct containingTab = null;
        if (widgetStruct != null && widgetStruct.getChildWidget() != null) {
            if (!widgetStruct.getChildWidget().getWidgetType().getName().equals(widgetType.getName())) {
                containingTab = getContainingParentWidgetStruct(widgetStruct.getParentWidgetStruct(), widgetType);
            } else {
                containingTab = widgetStruct;
            }
        }
        return containingTab;
    }

    protected WidgetStruct getContainingRepeatableBlock(WidgetStruct currentWidgetStruct) throws ToolkitRendererException, AuthenticationException {
        WidgetStruct repeatableWidgetStruct = null;
        WidgetStruct containingBlockWidgetStruct = getContainingParentWidgetStruct(currentWidgetStruct.getParentWidgetStruct(), WidgetType.BLOCK);
        if (containingBlockWidgetStruct != null) {
            LOGGER.info("containingBlockWidgetStruct = " + containingBlockWidgetStruct.getChildWidget().getDescription());
        }
        if (containingBlockWidgetStruct != null) {
            Widget containingBlockWidget = containingBlockWidgetStruct.getChildWidget();
            if (containingBlockWidget.hasWidgetAttribute("repeatable") && containingBlockWidget.getWidgetAttribute("repeatable").getValue() != null && !containingBlockWidget.getWidgetAttribute("repeatable").getValue().isEmpty()) {
                repeatableWidgetStruct = containingBlockWidgetStruct;
            } else {
                repeatableWidgetStruct = getContainingRepeatableBlock(containingBlockWidgetStruct.getParentWidgetStruct());
            }
        }
        return repeatableWidgetStruct;
    }

    /**
     * Get a list of child WidgetStruct objects within the specified WidgetStruct with a childWidget of the specified
     * WidgetType.
     * 
     * @param widgetType the WidgetType
     * @return the list of WidgetStruct objects
     */
    protected List<WidgetStruct> getChildWidgetStructsByWidgetType(WidgetStruct widgetStruct, WidgetType widgetType) {
        List<WidgetStruct> widgetStructs = new ArrayList<WidgetStruct>();
        for (WidgetStruct child : widgetStruct.getChildWidgetStructs()) {
            if (child.getChildWidget().getWidgetType().getName().equals(widgetType.getName())) {
                widgetStructs.add(child);
            }
        }
        return widgetStructs;
    }

    /**
     * Get a list of child WidgetStruct objects within the specified WidgetStruct with a childWidget of the specified
     * WidgetType and a WidgetAttribute with the specified widgetAttributeName and that contains the specified
     * widgetAttributeValue.
     * 
     * @param widgetType the WidgetType
     * @return the list of WidgetStruct objects
     */
    protected List<WidgetStruct> getChildWidgetStructsByWidgetTypeAndWidgetAttributeValue(WidgetStruct widgetStruct, WidgetType widgetType, String widgetAttributeName, String widgetAttributeValue) {
        List<WidgetStruct> result = new ArrayList<WidgetStruct>();
        List<WidgetStruct> widgList = getChildWidgetStructsByWidgetType(widgetStruct, widgetType);
        for (WidgetStruct widgetStruct2 : widgList) {
            WidgetAttribute wa = widgetStruct2.getChildWidget().getWidgetAttribute(widgetAttributeName);
            if (wa != null && wa.getValue() != null && wa.getValue().contains(widgetAttributeValue)) {
                result.add(widgetStruct2);
            }
        }
        return result;
    }

    /**
     * Get a list of child WidgetStruct objects within the specified WidgetStruct with the specified WidgetAttribute with the
     * name and value specified in the WidgetAttribute. If the WidgetAttribute (on name) is not present in the child,
     * inclusion depends on the parameter includeChildIfWidgetAttributeNotPresent
     * 
     * @param widgetStruct the parent WidgetStruct
     * @param widgetAttribute the WidgetAttribute to search for
     * @param includeChildIfWidgetAttributeNotPresent boolean to indicate whether to in- or exclude WidgetStructs if the
     * specified WidgetAttribute is not present
     * @return the list of WidgetStruct objects to include
     */
    protected List<WidgetStruct> getChildWidgetStructsByWidgetAttribute(WidgetStruct widgetStruct, WidgetAttribute widgetAttribute, boolean includeChildIfWidgetAttributeNotPresent) {
        List<WidgetStruct> result = new ArrayList<WidgetStruct>();
        List<WidgetStruct> widgList = widgetStruct.getChildWidgetStructs();
        for (WidgetStruct widgetStruct2 : widgList) {
            WidgetAttribute wa = widgetStruct2.getChildWidget().getWidgetAttribute(widgetAttribute.getName());
            if (wa == null) {
                if (includeChildIfWidgetAttributeNotPresent) {
                    result.add(widgetStruct2);
                }
            } else if (wa.getValue() != null && wa.getValue().equals(widgetAttribute.getValue())) {
                result.add(widgetStruct2);
            }
        }
        return result;
    }

    /**
     * Get all WidgetStructs with the specified WidgetType and the specified attributename and -value in the specified
     * WidgetStruct and check deep (the complete tree, recursively).
     * 
     * @param widgetStruct
     * @param widgetType
     * @param widgetAttributeName
     * @param widgetAttributeValue
     * @return
     */
    protected List<WidgetStruct> getDeepChildWidgetStructs(WidgetStruct widgetStruct, WidgetType widgetType, String widgetAttributeName, String widgetAttributeValue) {
        List<WidgetStruct> widgetStructs = new ArrayList<WidgetStruct>();
        List<WidgetStruct> widgetStructsWT = new ArrayList<WidgetStruct>();
        getDeepChildWidgetStructs(widgetStructsWT, widgetStruct, widgetType);
        for (WidgetStruct widgetStruct2 : widgetStructsWT) {
            WidgetAttribute wa = widgetStruct2.getChildWidget().getWidgetAttribute(widgetAttributeName);
            if (wa != null) {
                String value = wa.getValue();
                if (value != null && (value.endsWith(widgetAttributeValue) || value.startsWith(widgetAttributeValue))) {
                    widgetStructs.add(widgetStruct2);
                }
            }
        }
        return widgetStructs;
    }

    /**
     * Get all WidgetStructs with the specified WidgetType in the specified WidgetStruct and check deep (the complete tree,
     * recursively).
     * 
     * @param widgetStructs
     * @param widgetStruct
     * @param widgetType
     */
    protected void getDeepChildWidgetStructs(List<WidgetStruct> widgetStructs, WidgetStruct widgetStruct, WidgetType widgetType) {
        widgetStructs.addAll(getChildWidgetStructsByWidgetType(widgetStruct, widgetType));
        for (WidgetStruct child : widgetStruct.getChildWidgetStructs()) {
            getDeepChildWidgetStructs(widgetStructs, child, widgetType);
        }
    }

    /**
     * Replace all occurrences of syntax '<sign><numeric-value>' with the reference to the widget with the id <numeric-value>
     * and concatenate.
     * 
     * @param widgetAttribute the widgetAttribute with the value to check for occurrences of <sign><numeric-value>
     * @param sign the indicator to prefix an id of a referenced widget with
     * @throws AuthenticationException
     */
    protected String replaceAllSignesWithReferencesToWidgetsAndConcat(WidgetAttribute widgetAttribute, String sign, Widget form, Integer version) throws ToolkitRendererException, AuthenticationException {
        String returnValue = null;
        if (widgetAttribute != null && widgetAttribute.getValue() != null && !"".equals(widgetAttribute.getValue()) && widgetAttribute.getValue().contains(sign)) {
            boolean descriptionReferenceFound = false;
            String source = "concat('" + widgetAttribute.getValue() + "')";
            String result = source;
            while (source.indexOf(sign) > -1) {
                String descriptionReference = null;
                if (source.contains(sign + "[")) {
                    String partLocalTemp = source;
                    descriptionReference = partLocalTemp.substring(partLocalTemp.indexOf("[") + 1, partLocalTemp.indexOf("]"));
                } else {
                    descriptionReference = StringUtil.getStringBetween(source, sign, "([a-zA-Z0-9_]*)");
                }
                if (descriptionReference != null && !"".equals(descriptionReference)) {
                    descriptionReferenceFound = true;
                    Widget widget;
                    try {
                        widget = (Widget) getWidgetService().getWidgetByDescriptionFormAndVersion(descriptionReference, form, version);
                        String reference = getReferenceValue(widget).getValue();
                        if (widget.hasWidgetAttribute("xsdType") && !widget.getWidgetAttribute("xsdType").getValue().isEmpty() && widget.getWidgetAttribute("xsdType").getValue().equals("xforms:date")) {
                            reference = new StringBuilder("if (").append(reference).append(" != '') then (format-date(xs:date(").append(reference).append("),'[D]/[M]/[Y]', 'en', (), ())) else (string(''))").toString();
                        }
                        StringBuffer sb = new StringBuffer(reference);
                        if (source.contains(sign + "[")) {
                            result = result.replaceFirst(sign + "\\[" + descriptionReference + "\\]", "'," + sb.toString() + ",'");
                        } else {
                            result = result.replaceFirst(sign + descriptionReference, "'," + sb.toString() + ",'");
                        }
                    } catch (ObjectNotFoundException e) {
                        LOGGER.warn(e.getMessage());
                        if (source.contains(sign + "[")) {
                            result = result.replaceFirst(sign + "\\[" + descriptionReference + "\\]", "','" + sign + "\\[" + descriptionReference + "\\]" + "','");
                        } else {
                            result = result.replaceFirst(sign + descriptionReference, "','" + sign + descriptionReference + "','");
                        }
                    }
                }
                source = source.replaceFirst(sign, "");
            }
            if (!descriptionReferenceFound) {
                result = "string('" + widgetAttribute.getValue() + "')";
            }
            returnValue = result;
        }
        return returnValue;
    }

    /**
     * Update the xpaths that include relative references (including DotDots (..)) to other widgets.
     * 
     * @param container the WidgetStruct the xpaths are declared on
     * @param xpaths a Set of XPath objects declared on the container
     * @throws ToolkitRendererException
     * @throws AuthenticationException
     */
    protected void updateRelativeReferencesInXPaths(WidgetStruct container, Set<XPath> xpaths) throws ToolkitRendererException, AuthenticationException {
        for (XPath xpath : xpaths) {
            String updatedRule = updateRelativeReferencesInString(container, xpath.getRule());
            xpath.setRule(updatedRule);
        }
    }

    /**
     * Add XPath bindings. This method finds the highest operand indicator and then groups the different relevances (on
     * operand) per indicator. Then, for each group of relevances, the method addXPathBindings() is called to add the actual
     * binding string.
     * 
     * @param xpaths the set of xpaths to add bindings for
     * @param bindingString the binding string to append the bindings to
     * @throws ObjectNotFoundException the Exception thrown if the object cannot be found
     * @throws ToolkitRendererException the exception thrown if anything went wrong. If an exception was caught, this
     * exception will be accessible (wrapped) inside this ToolkitRendererException
     */
    protected void addXPathBindings(Set<XPath> xpaths, StringBuilder bindingString) throws ObjectNotFoundException, ToolkitRendererException, AuthenticationException {
        boolean orRelevancesFound = false;
        int highestOrRelevancesFound = 0;
        boolean andRelevancesFound = false;
        int highestAndRelevancesFound = 0;
        boolean xorRelevancesFound = false;
        int highestXorRelevancesFound = 0;
        boolean aroRelevancesFound = false;
        int highestAroRelevancesFound = 0;
        highestOrRelevancesFound = findHighestXPathOperantIndicator(xpaths, "or");
        highestAndRelevancesFound = findHighestXPathOperantIndicator(xpaths, "and");
        highestXorRelevancesFound = findHighestXPathOperantIndicator(xpaths, "xor");
        highestAroRelevancesFound = findHighestXPathOperantIndicator(xpaths, "aro");
        int highestOperantIndicator = highestOrRelevancesFound > highestAndRelevancesFound ? highestOrRelevancesFound : highestAndRelevancesFound;
        highestOperantIndicator = highestOperantIndicator > highestXorRelevancesFound ? highestOperantIndicator : highestXorRelevancesFound;
        highestOperantIndicator = highestOperantIndicator > highestAroRelevancesFound ? highestOperantIndicator : highestAroRelevancesFound;
        for (int i = 0; i <= highestOperantIndicator; i++) {
            String operantIndicator = "";
            if (i > 0) {
                operantIndicator += i;
                bindingString.append(" or ");
            }
            bindingString.append(" (");
            Set<XPath> orRelevances = filterXPathsOnOperant(xpaths, "or" + operantIndicator);
            Set<XPath> andRelevances = filterXPathsOnOperant(xpaths, "and" + operantIndicator);
            Set<XPath> xorRelevances = filterXPathsOnOperant(xpaths, "xor" + operantIndicator);
            Set<XPath> aroRelevances = filterXPathsOnOperant(xpaths, "aro" + operantIndicator);
            orRelevancesFound = orRelevances.size() > 0 ? true : false;
            andRelevancesFound = andRelevances.size() > 0 ? true : false;
            xorRelevancesFound = xorRelevances.size() > 0 ? true : false;
            aroRelevancesFound = aroRelevances.size() > 0 ? true : false;
            if (orRelevances != null && !orRelevances.isEmpty()) {
                if (andRelevancesFound || xorRelevancesFound || aroRelevancesFound) {
                    bindingString.append("(");
                }
                addXPathBindings(orRelevances, bindingString, "or");
                if (andRelevancesFound || xorRelevancesFound || aroRelevancesFound) {
                    bindingString.append(")");
                }
            }
            if (andRelevances != null && !andRelevances.isEmpty()) {
                if (orRelevancesFound || xorRelevancesFound || aroRelevancesFound) {
                    bindingString.append(" and (");
                }
                addXPathBindings(andRelevances, bindingString, "and");
                if (orRelevancesFound || xorRelevancesFound || aroRelevancesFound) {
                    bindingString.append(")");
                }
            }
            if (xorRelevances != null && !xorRelevances.isEmpty()) {
                if (orRelevancesFound || andRelevancesFound || aroRelevancesFound) {
                    bindingString.append(" and (");
                }
                addXPathBindings(xorRelevances, bindingString, "or");
                if (orRelevancesFound || andRelevancesFound || aroRelevancesFound) {
                    bindingString.append(")");
                }
            }
            if (aroRelevances != null && !aroRelevances.isEmpty()) {
                if (orRelevancesFound || andRelevancesFound || xorRelevancesFound) {
                    bindingString.append(" and (");
                }
                addXPathBindings(aroRelevances, bindingString, "and");
                if (orRelevancesFound || andRelevancesFound || xorRelevancesFound) {
                    bindingString.append(")");
                }
            }
            bindingString.append(")");
        }
    }

    private int findHighestXPathOperantIndicator(Set<XPath> xpaths, String operant) {
        int result = 0;
        for (XPath xpath : xpaths) {
            if (xpath.getOperant().startsWith(operant) && xpath.getOperant().length() > operant.length()) {
                try {
                    Integer newValue = new Integer(xpath.getOperant().substring(operant.length()));
                    if (newValue > result) {
                        result = newValue;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error("NumberFormatException: " + e.getMessage());
                }
            }
        }
        return result;
    }

    private void addXPathBindings(Set<XPath> xpaths, StringBuilder bindingString, String operant) throws ObjectNotFoundException, ToolkitRendererException, AuthenticationException {
        int i = 0;
        for (XPath xpath : xpaths) {
            if (i > 0) {
                bindingString.append(operant + " (");
            } else {
                bindingString.append("(");
            }
            i++;
            String xpathOperant = xpath.getOperant();
            if ("or".equals(xpathOperant) || "aro".equals(xpathOperant)) {
                bindingString.append(replaceSignsInStringWithWidgetPaths(xpath.getRule(), "@", true));
            } else {
                bindingString.append(replaceSignsInStringWithWidgetPaths(xpath.getRule(), "@", false));
            }
            bindingString.append(")");
        }
    }

    private Set<XPath> filterXPathsOnOperant(Set<XPath> xpaths, String operant) {
        Set<XPath> theXPaths = new HashSet<XPath>();
        for (XPath xpath : xpaths) {
            if (xpath.getOperant().equals(operant)) {
                theXPaths.add(xpath);
            }
        }
        return theXPaths;
    }

    private String updateRelativeReferencesInString(WidgetStruct container, String string) throws ToolkitRendererException, AuthenticationException {
        String widgetPath = null;
        if (container.getChildWidget().getPrefilleddataWidgetAttribute() != null) {
            Widget tmpWidget = new Widget();
            tmpWidget.setDescription(container.getChildWidget().getDescription());
            tmpWidget.setWidgetType(container.getChildWidget().getWidgetType());
            widgetPath = getWidgetPath(tmpWidget);
        } else {
            widgetPath = getWidgetPath(container.getChildWidget());
        }
        List<String> list = new ArrayList<String>();
        list.add(".");
        list.add("/");
        list.add("*");
        list.add("[");
        list.add("]");
        list.add("0");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");
        Map<String, String> hits = new HashMap<String, String>();
        boolean xpathContainsRelativeReference = false;
        xpathContainsRelativeReference = false;
        String a = string;
        if (string.contains(" ..")) {
            a = a.replaceAll("[ ]\\.\\.", " " + widgetPath + "/..");
            xpathContainsRelativeReference = true;
        }
        if (string.startsWith("..")) {
            a = a.replaceFirst("\\.\\.", " " + widgetPath + "/..");
            xpathContainsRelativeReference = true;
        }
        if (xpathContainsRelativeReference) {
            String z = a;
            int i = 0;
            while (z.contains(widgetPath)) {
                int c = z.indexOf(widgetPath);
                int b = z.indexOf(widgetPath) + widgetPath.length();
                String aa = z.substring(c, b + StringUtil.firstCharPositionNotInList(z.substring(b), list));
                hits.put("XXX" + (++i), aa);
                z = z.replace(aa, "XXX" + i);
            }
            for (String hit : hits.keySet()) {
                String str = hits.get(hit);
                String referencedWidgetPath = getWidgetModelPath(str.substring(str.indexOf("widgetTree") + "widgetTree".length()));
                Node referencedNode = getWidget(referencedWidgetPath.substring(referencedWidgetPath.indexOf("widgetTree") + "widgetTree".length()));
                String referencesDescription = referencedNode.valueOf("@description");
                z = z.replaceFirst(hit, "@" + referencesDescription);
            }
            a = z;
        }
        return a;
    }

    private String getXsdXpathString(Widget widget) throws ObjectNotFoundException, ToolkitRendererException, NumberFormatException, AuthenticationException {
        StringBuilder result = null;
        StringBuilder sb = new StringBuilder(getAbsoluteReferenceAttribute(widget).getValue());
        WidgetAttribute xsdType = widget.getWidgetAttribute("xsdType");
        if (xsdType != null && xsdType.getValue() != null && !"".equals(xsdType.getValue())) {
            String newXsdTypeValue = xsdType.getValue();
            if (xsdType.getValue().startsWith("xforms:")) {
                newXsdTypeValue = xsdType.getValue().replace("xforms:", "xs:");
            }
            result = new StringBuilder("(").append(sb.toString()).append(" castable as ").append(newXsdTypeValue).append(")");
        }
        return result == null ? null : result.toString();
    }

    /**
     * Replace references to widgets in the specified source string with the actual paths (in the model) to those widgets.
     * The references to the widgets in the source string have the syntax <sign><id> in which the sign is specified as one of
     * the argument to this method and the id should be the id of the widget to reference. The widget is searched for in the
     * database and, if found, the path in the model to that widget is found calling method getWidgetPath(widget). If found,
     * the <sign><id> will be replaced by the path found. If we also have to account for recursive relevancy, also passed in
     * as argument, exf:relevant(path) is appended to the result.
     * 
     * @param source the source string in which references to widgets are included
     * @param sign the sign that prepends the id of the widget to reference
     * @param recursive an indicator for specifying whether the relation to the widget should be recursive
     * @return the result string, with the references to the referenced widgets included
     * @throws ObjectNotFoundException
     * @throws AuthenticationException
     * @throws NumberFormatException
     * @throws ToolkitRendererException the exception thrown if anything went wrong. If an exception was caught, this
     * exception will be wrapped inside this ToolkitRendererException
     */
    public String replaceSignsInStringWithWidgetPaths(String source, String sign, boolean recursive) throws ToolkitRendererException, NumberFormatException, AuthenticationException, ObjectNotFoundException {
        String result = source;
        List<String> list = new ArrayList<String>();
        list.add("(");
        list.add(")");
        List<String> parts = findAllParts(source, sign);
        if (parts != null && !parts.isEmpty()) {
            for (String part : parts) {
                String resultLocal = part;
                String partLocal = part;
                Set<String> recursivePaths = new HashSet<String>();
                Set<Widget> widgets = new HashSet<Widget>();
                while (partLocal.indexOf(sign) > -1) {
                    String descriptionReference = null;
                    if (partLocal.contains(sign + "[")) {
                        String partLocalTemp = partLocal;
                        descriptionReference = partLocalTemp.substring(partLocalTemp.indexOf("[") + 1, partLocalTemp.indexOf("]"));
                    } else {
                        descriptionReference = StringUtil.getStringBetween(partLocal, sign, "([a-zA-Z0-9_]*)");
                    }
                    Widget widget = getWidgetService().getWidgetByDescriptionFormAndVersion(descriptionReference, getObjectModel().getFormWidget(), getObjectModel().getVersion());
                    if (widget.getWidgetType() != null && (widget.getWidgetType().getName().equals(WidgetType.BLOCK.getName()) || widget.getWidgetType().getName().equals(WidgetType.TAB.getName()))) {
                        StringBuilder strB = new StringBuilder();
                        constructConstraintsString(widget, strB);
                        if (partLocal.contains(sign + "[")) {
                            resultLocal = resultLocal.replaceAll(sign + "\\[" + descriptionReference + "\\]", new StringBuilder("(").append(strB).append(")").toString());
                            partLocal = partLocal.replaceAll(sign + "\\[" + descriptionReference + "\\]", "");
                        } else {
                            resultLocal = resultLocal.replaceAll(sign + descriptionReference, new StringBuilder("(").append(strB).append(")").toString());
                            partLocal = partLocal.replaceAll(sign + descriptionReference, "");
                        }
                        continue;
                    }
                    String widgetPath = getWidgetPath(widget);
                    widgets.add(widget);
                    if (recursive) {
                        if (widgetPath.contains("prefilleddata")) {
                            Widget tmpWidget = new Widget();
                            tmpWidget.setDescription(widget.getDescription());
                            tmpWidget.setWidgetType(widget.getWidgetType());
                            String tmpWidgetPath = getWidgetPath(tmpWidget);
                            recursivePaths.add(tmpWidgetPath);
                        } else {
                            recursivePaths.add(widgetPath);
                        }
                    }
                    WidgetAttribute xsdType = widget.getWidgetAttribute("xsdType");
                    if (xsdType != null && xsdType.getValue() != null && !"".equals(xsdType.getValue()) && "xforms:date".equals(xsdType.getValue())) {
                        widgetPath = new StringBuilder("xs:date(").append(widgetPath).append(")").toString();
                    }
                    if (partLocal.contains(sign + "[")) {
                        resultLocal = resultLocal.replaceFirst(sign + "\\[" + descriptionReference + "\\]", widgetPath);
                    } else {
                        resultLocal = resultLocal.replaceFirst(sign + descriptionReference, widgetPath);
                    }
                    partLocal = partLocal.replaceFirst(sign, "");
                }
                String leadingChars = null;
                int a = StringUtil.firstCharPositionNotInList(resultLocal, list);
                if (a > 0) {
                    leadingChars = resultLocal.substring(0, a);
                    resultLocal = resultLocal.substring(a);
                }
                StringBuilder sb = new StringBuilder();
                if (leadingChars != null) {
                    sb.append(leadingChars);
                }
                sb.append("(");
                for (Widget widget : widgets) {
                    String xsdTypeXPathString = getXsdXpathString(widget);
                    if (xsdTypeXPathString != null) {
                        sb.append(xsdTypeXPathString);
                        sb.append(" and ");
                    }
                }
                if (recursive && !recursivePaths.isEmpty()) {
                    for (String string : recursivePaths) {
                        sb.append("(exf:relevant(");
                        sb.append(string);
                        sb.append(")) and ");
                    }
                }
                sb.append("(").append(resultLocal).append(")");
                sb.append(")");
                resultLocal = sb.toString();
                result = result.replace(part, resultLocal);
            }
        }
        return result;
    }

    private void constructConstraintsString(Widget containerWidget, StringBuilder sb) throws ToolkitRendererException, AuthenticationException, NumberFormatException, ObjectNotFoundException {
        WidgetStruct containerWidgetStruct = findWidgetStructForChildWidget(getObjectModel(), containerWidget);
        constructConstraintsString(containerWidgetStruct, sb);
    }

    private void constructConstraintsString(WidgetStruct containerWidgetStruct, StringBuilder sb) throws ToolkitRendererException, AuthenticationException, NumberFormatException, ObjectNotFoundException {
        for (WidgetStruct ws : containerWidgetStruct.getChildWidgetStructs()) {
            Widget w = ws.getChildWidget();
            if (w.getWidgetType().getName().equals(WidgetType.BLOCK.getName()) || w.getWidgetType().getName().equals(WidgetType.TAB.getName())) {
                if (!w.getWidgetAttribute("relevance").getValue().isEmpty()) {
                    if (sb.toString().endsWith(")")) {
                        sb.append(" and ");
                    }
                    String initialValue = w.getWidgetAttribute("relevance").getValue();
                    String constraintValue = replaceDotsButNotDotDotsInString(w, initialValue);
                    constraintValue = updateRelativeReferencesInString(ws, constraintValue);
                    sb.append("( if ( ").append(replaceSignsInStringWithWidgetPaths(constraintValue, "@", true)).append(") then (");
                }
                constructConstraintsString(ws, sb);
                if (!w.getWidgetAttribute("relevance").getValue().isEmpty()) {
                    sb.append(") else (true()) )");
                }
            } else {
                if (w != null && (w.hasWidgetAttribute("constraint") || w.hasWidgetAttribute("mandatory"))) {
                    if (w.hasWidgetAttribute("constraint") && w.getWidgetAttribute("constraint").getValue() != null && !w.getWidgetAttribute("constraint").getValue().isEmpty()) {
                        if (sb.toString().endsWith(")")) {
                            sb.append(" and ");
                        }
                        String initialValue = w.getWidgetAttribute("constraint").getValue();
                        String constraintValue = replaceDotsButNotDotDotsInString(w, initialValue);
                        constraintValue = updateRelativeReferencesInString(ws, constraintValue);
                        sb.append("(");
                        sb.append(replaceSignsInStringWithWidgetPaths(constraintValue, "@", true));
                        sb.append(")");
                    } else if (w.hasWidgetAttribute("mandatory") && w.getWidgetAttribute("mandatory").getValue() != null && !w.getWidgetAttribute("mandatory").getValue().equals("false()")) {
                        if (sb.toString().endsWith(")")) {
                            sb.append(" and ");
                        }
                        String initialValue = w.getWidgetAttribute("mandatory").getValue().equals("true()") ? ((w.hasWidgetAttribute("xsdType") && w.getWidgetAttribute("xsdType") != null && w.getWidgetAttribute("xsdType").getValue().equals("xforms:date")) ? "boolean(string(.))" : ". != ''") : new StringBuilder("(not(").append(w.getWidgetAttribute("mandatory").getValue()).append(") or (. != ''))").toString();
                        String constraintValue = replaceDotsButNotDotDotsInString(w, initialValue);
                        constraintValue = updateRelativeReferencesInString(ws, constraintValue);
                        sb.append("(");
                        sb.append(replaceSignsInStringWithWidgetPaths(constraintValue, "@", true));
                        sb.append(")");
                    } else {
                        if (!sb.toString().endsWith(")")) {
                            sb.append("true()");
                        }
                    }
                } else {
                    if (!sb.toString().endsWith(")")) {
                        sb.append("true()");
                    }
                }
            }
        }
    }

    protected void replaceDotsButNotDotDotsInXPaths(Set<XPath> xpaths, Widget widget) {
        for (XPath path : xpaths) {
            String initialValue = path.getRule();
            path.setRule(replaceDotsButNotDotDotsInString(widget, initialValue));
        }
    }

    private String replaceDotsButNotDotDotsInString(Widget w, String initialValue) {
        String resultValue = initialValue;
        if (initialValue.contains("..")) {
            resultValue = resultValue.replaceAll("\\.\\.", "XXXXX");
        }
        Pattern decimalPattern = Pattern.compile("[0-9]\\.[0-9]");
        Matcher m = decimalPattern.matcher(resultValue);
        int count = 0;
        while (m.find()) {
            resultValue = resultValue.replace(resultValue.substring(m.start(), m.end()), resultValue.substring(m.start(), m.end()).replace(".", "~"));
        }
        resultValue = resultValue.replaceAll("\\.", "@" + w.getDescription());
        if (initialValue.contains("..")) {
            resultValue = resultValue.replaceAll("XXXXX", "..");
        }
        if (resultValue.contains("~")) {
            resultValue = resultValue.replaceAll("~", ".");
        }
        return resultValue;
    }

    private WidgetStruct findWidgetStructForChildWidget(WidgetStruct widgetStruct, Widget childWidget) throws ToolkitRendererException, AuthenticationException {
        WidgetStruct widgetStr = null;
        if (!widgetStruct.getChildWidget().getId().equals(childWidget.getId())) {
            for (WidgetStruct ws : widgetStruct.getChildWidgetStructs()) {
                widgetStr = findWidgetStructForChildWidget(ws, childWidget);
                if (widgetStr != null) {
                    break;
                }
            }
        } else {
            widgetStr = widgetStruct;
        }
        return widgetStr;
    }

    public List<String> findAllParts(String source, String sign) {
        List<String> parts = new ArrayList<String>();
        String sourceTmp = source.replaceAll(" or ", "|");
        sourceTmp = sourceTmp.replaceAll(" and ", "&");
        StringTokenizer andTokens = new StringTokenizer(sourceTmp, "&");
        while (andTokens.hasMoreElements()) {
            String andToken = andTokens.nextToken();
            StringTokenizer orTokens = new StringTokenizer(andToken, "|");
            while (orTokens.hasMoreElements()) {
                String orToken = orTokens.nextToken();
                parts.add(orToken);
            }
        }
        return parts;
    }

    public String removeLeadingCharacter(String source, List<String> strings) {
        String firstChar = Character.toString(source.charAt(0));
        if (strings.contains(firstChar)) {
            source = removeLeadingCharacter(source.substring(1), strings);
        }
        return source;
    }

    /**
     * Get the path to the value of a Widget in the xforms model. The widget can be bound to prefilling or not. This method
     * first checks whether the Widget is of a type that can be prefilled or not. If not, or if the WidgetAttribute that
     * holds the value for prefilling is null or contains an empty string, the path in the model is derived by calling
     * getWidgetModelPath(id). If the widget is bound to prefilling, the path in the model is derived by calling
     * getWidgetPrefilleddataPath(WidgetAttribute). The path to the value of the widget is then constructed based on the path
     * found for the widget. This method returns a WidgetAttribute that has the name 'ref'. The value of that WidgetAttribute
     * is the path constructed.
     * 
     * @param widget the Widget to find the path in the model for
     * @return the path to the Widget in the xforms model
     * @throws ToolkitRendererException the exception thrown if anything went wrong. If an exception was caught, this
     * exception will be accessible (wrapped) inside this ToolkitRendererException
     * @throws AuthenticationException
     */
    protected WidgetAttribute getAbsoluteReferenceAttribute(Widget widget) throws ToolkitRendererException, AuthenticationException {
        WidgetAttribute ref = new WidgetAttribute();
        ref.setName("ref");
        if (WidgetType.INPUT_TEXT.getName().equals(widget.getWidgetType().getName()) || WidgetType.TEXTAREA.getName().equals(widget.getWidgetType().getName()) || WidgetType.SELECT_ONE.getName().equals(widget.getWidgetType().getName()) || WidgetType.SELECT_MULTIPLE.getName().equals(widget.getWidgetType().getName())) {
            ref.setValue(getWidgetModelPathById(widget) + "/value");
        } else if (WidgetType.OUTPUT_TEXT.getName().equals(widget.getWidgetType().getName())) {
            ref.setValue(getWidgetModelPathById(widget) + "/@modelValue");
        } else {
            ref.setValue(getWidgetModelPathById(widget));
        }
        return ref;
    }

    protected WidgetAttribute getRelativeReferenceAttribute(Widget widget) throws ToolkitRendererException, AuthenticationException {
        WidgetAttribute ref = getAbsoluteReferenceAttribute(widget);
        String value = ref.getValue();
        boolean isUpperWidget = false;
        String strippedValue = value.substring(value.indexOf("widget[1]/widget") + "widget[1]/widget".length());
        strippedValue = strippedValue.replaceFirst("widget", "DUMMY");
        if (!strippedValue.contains("widget")) {
            isUpperWidget = true;
        }
        if (!isUpperWidget) {
            int lastWidgetIndex = value.lastIndexOf("widget");
            String relativeValue = value.substring(lastWidgetIndex);
            ref.setValue(relativeValue);
        }
        return ref;
    }

    /**
     * Get the path to a Widget in the xforms model. The widget can be bound to prefilling or not. This method first checks
     * whether the Widget is of a type that can be prefilled or not. If not, or if the WidgetAttribute that holds the value
     * for prefilling is null or cantains an empty string, the path is derived by calling getWidgetModelPath(id). If the
     * widget is bound to prefilling, the path is derived by calling getWidgetPrefilleddataPath(WidgetAttribute).
     * 
     * @param widget the Widget to find the path in the model for
     * @return the path to the Widget in the xforms model
     * @throws ToolkitRendererException the exception thrown if anything went wrong. If an exception was caught, this
     * exception will be accessible (wrapped) inside this ToolkitRendererException
     * @throws AuthenticationException
     */
    protected String getWidgetPath(Widget widget) throws ToolkitRendererException, AuthenticationException {
        String result = null;
        result = getWidgetModelPathByDescription(widget.getDescription());
        return result;
    }

    protected WidgetAttribute getReferenceValue(Widget widget) throws ToolkitRendererException, AuthenticationException {
        if ((!WidgetType.SELECT_ONE.getName().equals(widget.getWidgetType().getName()) && !WidgetType.SELECT_MULTIPLE.getName().equals(widget.getWidgetType().getName()))) {
            return getAbsoluteReferenceAttribute(widget);
        } else {
            WidgetAttribute ref = new WidgetAttribute();
            ref.setName("ref");
            WidgetAttribute dkd = widget.getWidgetAttribute("dkd");
            String widgetPath = null;
            widgetPath = getWidgetModelPathByDescription(widget.getDescription());
            if (WidgetType.SELECT_ONE.getName().equals(widget.getWidgetType().getName())) {
                ref.setValue(widgetPath + "/widget[@selectValue = " + widgetPath + "/value]/@selectLabel");
            } else if (WidgetType.SELECT_MULTIPLE.getName().equals(widget.getWidgetType().getName())) {
                int amountOfSelectItems = getChildCountByDescription(widget.getDescription(), WidgetType.SELECT_ITEM);
                StringBuilder val = new StringBuilder();
                if (amountOfSelectItems > 0) {
                    val.append("concat(");
                    for (int i = 1; i <= amountOfSelectItems; i++) {
                        if (i > 1) {
                            val.append(", ' ',");
                        }
                        val.append("if(contains(").append(widgetPath + "/value,").append(widgetPath + "/widget[" + i + "]/@selectValue)) then ");
                        val.append(widgetPath + "/widget[" + i + "]/@selectLabel");
                        val.append(" else string('')");
                    }
                    val.append(")");
                }
                ref.setValue(val.toString());
            }
            return ref;
        }
    }

    protected List<Node> getWidgets(String xpath) throws ToolkitRendererException, AuthenticationException {
        Document widgetTree;
        try {
            widgetTree = DocumentHelper.parseText(getObjectModelAsString());
        } catch (DocumentException e1) {
            String message = "A DocumentException occurred during parsing of the object model: [" + getObjectModelAsString() + "]";
            LOGGER.error(message);
            throw new ToolkitRendererException(message, e1);
        }
        List<Node> nodes = null;
        try {
            Dom4jXPath expression = new Dom4jXPath(xpath);
            nodes = (List<Node>) expression.selectNodes(widgetTree);
        } catch (JaxenException e) {
            String message = "A JaxenException occurred in getWidgetPath(xpath) for xpath: [" + xpath + "]";
            LOGGER.error(message);
            throw new ToolkitRendererException(message, e);
        }
        if (nodes == null || nodes.isEmpty()) {
            String message = "No nodes found based on the xpath: [" + xpath + "]";
            LOGGER.warn(message);
            throw new ToolkitRendererException(message);
        }
        return nodes;
    }

    protected Node getWidget(String xpath) throws ToolkitRendererException, AuthenticationException {
        Document widgetTree;
        try {
            widgetTree = DocumentHelper.parseText(getObjectModelAsString());
        } catch (DocumentException e1) {
            String message = "A DocumentException occurred during parsing of the object model: [" + getObjectModelAsString() + "]";
            LOGGER.error(message);
            throw new ToolkitRendererException(message, e1);
        }
        Node node;
        try {
            Dom4jXPath expression = new Dom4jXPath(xpath);
            node = (Node) expression.selectSingleNode(widgetTree);
        } catch (JaxenException e) {
            String message = "A JaxenException occurred in getWidgetPath(xpath) for xpath: [" + xpath + "]";
            LOGGER.error(message);
            throw new ToolkitRendererException(message, e);
        }
        if (node == null) {
            String message = "No node found based on the xpath: [" + xpath + "]";
            LOGGER.error(message);
            throw new ToolkitRendererException(message);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("found node: " + node.getUniquePath() + ";" + node.asXML());
        }
        return node;
    }

    /**
     * Get the path to a Widget in the model. This implicitly means, that the Widget is not supposed to be bound to
     * prefilling and the value is stored directly in the model. This method accepts an xpath expression to search the model
     * with, collects the object model and evaluates the specified xpath expression on the object model. If a node is found,
     * the unique path is extracted and re-formed to a path that orbeon/xforms understands/expects. If not found or a
     * JaxenException occurs, a ToolkitRendererException is thrown.
     * 
     * @param xpath the xpath to search the object model with
     * @return the path to the Widget in the model
     * @throws ToolkitRendererException the exception thrown if anything went wrong. If an exception was caught, this
     * exception will be accessible (wrapped) inside this ToolkitRendererException
     * @throws AuthenticationException
     */
    protected String getWidgetModelPath(String xpath) throws ToolkitRendererException, AuthenticationException {
        String widgetPath = null;
        StringBuffer path = new StringBuffer();
        Node node = getWidget(xpath);
        String nodePath = node.getUniquePath();
        while (nodePath.contains("widget/")) {
            nodePath = nodePath.replaceFirst("widget/", "widget[1]/");
        }
        if (nodePath.indexOf("widgetTree") > -1) {
            nodePath = nodePath.substring(nodePath.indexOf("widgetTree") + "widgetTree".length());
        }
        path = path.append("instance('form-instance')/formcontent/widgetTree").append(nodePath);
        widgetPath = path.toString();
        return widgetPath;
    }

    /**
     * Get the path to a Widget in the model. This implicitly means, that the Widget is not bound to prefilling and the value
     * is stored directly in the model. This method first checks whether the path to this Widget in the active WidgetStruct
     * (form) for the logged in user is already stored in cache. If it is, the path is taken from cache. If it is not, an
     * xpath expression with the id of the Widget as passed into this method is constructed and getWidgetModelPath(xpath) is
     * called to derive the actual path. If the path is found, it is stored in cache.
     * 
     * @param id the id of the Widget to find the path in the model for
     * @return the path to the Widget in the model
     * @throws ToolkitRendererException the exception thrown if anything went wrong. If an exception was caught, this
     * exception will be accessible (wrapped) inside this ToolkitRendererException
     * @throws AuthenticationException
     */
    protected String getWidgetModelPathByDescription(String description) throws ToolkitRendererException, AuthenticationException {
        String xpath = null;
        if (description != null) {
            String key = getCacheKey();
            xpath = ToolkitCache.getActiveWidgetStructWidgetPathByDescription(key, description);
            if (xpath == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ViewRenderer.getWidgetModelPath: widgetPath for key: " + key + " and widget: " + description + " not found in cache");
                }
                String xpathStr = "//widget[@description='" + description + "']";
                xpath = getWidgetModelPath(xpathStr);
                ToolkitCache.updateActiveWidgetStructWidgetPathByDescription(key, description, xpath);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ViewRenderer.getWidgetModelPath: widgetPath for key: " + key + " and widget: " + description + " found in cache: " + xpath);
                }
            }
        }
        return xpath;
    }

    protected String getWidgetModelPathByAttribute(WidgetAttribute wa) throws ToolkitRendererException, AuthenticationException {
        String xpath = null;
        String xpathStr = "//widget[@" + wa.getName() + "='" + wa.getValue() + "']";
        xpath = getWidgetModelPath(xpathStr);
        return xpath;
    }

    private String getWidgetModelPathById(Widget widget) throws ToolkitRendererException, AuthenticationException {
        Long id = widget.getId();
        String xpath = null;
        if (id != null) {
            String key = getCacheKey();
            xpath = ToolkitCache.getActiveWidgetStructWidgetPathById(key, id);
            if (xpath == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ViewRenderer.getWidgetModelPath: widgetPath for key: " + key + " and widget: " + id + " not found in cache");
                }
                String xpathStr = "//widget[@value=" + id + "]";
                xpath = getWidgetModelPath(xpathStr);
                ToolkitCache.updateActiveWidgetStructWidgetPathById(key, id, xpath);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ViewRenderer.getWidgetModelPath: widgetPath for key: " + key + " and widget: " + id + " found in cache: " + xpath);
                }
            }
        }
        return xpath;
    }

    /**
     * Get the amount of children with the specified WidgetType of the Widget in the object model with the specified id. This
     * method constructs an xpath expression with the id passed in and then calls getChildCount(xpath, widgetType) to derive
     * the actual child count.
     * 
     * @param id the id used to find the Widget in the object model to count the children for
     * @return the amount of children with the specified WidgetType of the Widget with the specified id in the object model
     * @throws ToolkitRendererException the exception thrown if anything went wrong. If an exception was caught, this
     * exception will be accessible (wrapped) inside this ToolkitRendererException
     * @throws AuthenticationException
     */
    private int getChildCountByDescription(String description, WidgetType widgetType) throws ToolkitRendererException, AuthenticationException {
        int amountOfChildren = 0;
        if (description != null) {
            String xpathToWidget = "//widget[@description='" + description + "']";
            amountOfChildren = getChildCount(xpathToWidget, widgetType);
        }
        return amountOfChildren;
    }

    /**
     * Get the amount of children with the specified WidgetType of the Widget in the object model with the specified
     * WidgetAttribute. This method constructs an xpath expression with the name and value of the WidgetAttribute passed in
     * and then calls getChildCount(xpath, widgetType) to derive the actual child count.
     * 
     * @param widgetAttribute the WidgetAttribute used to find the Widget in the object model to count the children for
     * @return the amount of children with the specified WidgetType of the Widget with the specified id in the object model
     * @throws ToolkitRendererException the exception thrown if anything went wrong. If an exception was caught, this
     * exception will be accessible (wrapped) inside this ToolkitRendererException
     * @throws AuthenticationException
     */
    private int getChildCountByWidgetAttribute(WidgetAttribute widgetAttribute, WidgetType widgetType) throws ToolkitRendererException, AuthenticationException {
        int amountOfChildren = 0;
        if (widgetAttribute != null && widgetAttribute.getValue() != null && !"".equals(widgetAttribute.getValue())) {
            String xpathToWidget = "//widget[@" + widgetAttribute.getName() + "='" + widgetAttribute.getValue() + "']";
            amountOfChildren = getChildCount(xpathToWidget, widgetType);
        }
        return amountOfChildren;
    }

    /**
     * Get the amount of children with the specified WidgetType of a Widget in the object model. This method accepts an xpath
     * expression to search the model with, collects the object model and evaluates the specified xpath expression on the
     * object model. If a node is found, the amount of children with the specified WidgetType is derived. If not found or a
     * JaxenException occurs, a ToolkitRendererException is thrown.
     * 
     * @param xpath the xpath to search the object model with
     * @return the amount of children of the widget found with the xpath in the object model
     * @throws ToolkitRendererException the exception thrown if anything went wrong. If an exception was caught, this
     * exception will be accessible (wrapped) inside this ToolkitRendererException
     * @throws AuthenticationException
     */
    private int getChildCount(String xpath, WidgetType widgetType) throws ToolkitRendererException, AuthenticationException {
        int amountOfChildren = 0;
        Document widgetTree = null;
        try {
            widgetTree = DocumentHelper.parseText(getObjectModelAsString());
        } catch (DocumentException e1) {
            String message = "A DocumentException occurred during parsing of the object model: [" + getObjectModelAsString() + "]";
            LOGGER.error(message);
            throw new ToolkitRendererException(message, e1);
        }
        List<Node> nodes;
        try {
            Dom4jXPath expression = new Dom4jXPath(xpath);
            Node node = (Node) expression.selectSingleNode(widgetTree);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("found node: " + node.getUniquePath() + ";" + node.asXML());
            }
            String xpathToChildWidgets = "./widget[@widgetType='" + widgetType.getName() + "']";
            expression = new Dom4jXPath(xpathToChildWidgets);
            nodes = expression.selectNodes(node);
        } catch (JaxenException e) {
            String message = "A JaxenException occurred in getChildCount(xpath, widgetType) for xpath '" + xpath + "' and widgetType '" + widgetType.getName();
            LOGGER.error(message);
            throw new ToolkitRendererException(message, e);
        }
        amountOfChildren = nodes.size();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("found " + nodes.size() + " child nodes");
        }
        return amountOfChildren;
    }
}

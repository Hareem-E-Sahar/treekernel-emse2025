package com.xmultra.processor.nitf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.xmultra.exception.ConfigException;
import com.xmultra.util.InitMapHolder;

/**
 * Parses the urgency from a doc. Configured with the "Urgency" element's child
 * "ParsedUrgencyRange" elements. The "ParsedUrgencyRange" range elements have a
 * single child "ParsedUrgencyErrorActions" which provide directions when an error
 * is encountered in the parsing of the Urgency out of the document. See 
 * "xmultra_system_operator's_guide.doc" for setup details.
 *
 * @author Wayne W. Weber
 * @version $Revision: #1 $
 * @since 1.4
 */
class Nxf_Urgency extends NitfXformer {

    /** Holds the data from the parsed out "ParsedUrgencyRange" elements. */
    private List parsedRangeList = new ArrayList();

    private int minValue = 0;

    private int maxValue = 0;

    private Element urgencyEl = null;

    /**
     * Initializes the object, parsing out attributes in the "Urgency" element and its
     * child "ParsedUrgencyRange" elements.
     *
     * @param e                   The configuration element associated with a particular Xformer.
     *
     * @param nitfProcessorConfig Holds the NitfProcessorConfig data & methods.
     *
     * @param imh                 Holds references to utility and log objects.
     *
     * @param nitfXformerUtils    Has utility methods shared by Xformers.
     *
     * @return True if initialization is successful.
     */
    boolean init(Element urgencyEl, NitfProcessorConfig nitfProcessorConfig, InitMapHolder imh, NitfXformerUtils nitfXformerUtils) {
        this.urgencyEl = urgencyEl;
        super.init(urgencyEl, nitfProcessorConfig, imh, nitfXformerUtils);
        ParsedRange parsedRangeUtil = new ParsedRange();
        try {
            this.minValue = parsedRangeUtil.parseValue(urgencyEl, NitfProcessorConfig.MIN_VALUE);
            this.maxValue = parsedRangeUtil.parseValue(urgencyEl, NitfProcessorConfig.MAX_VALUE);
        } catch (ConfigException e) {
            return false;
        }
        if (minValue >= maxValue) {
            errEntry.setAppContext("ParsedRange.init()");
            errEntry.setDocInfo(nitfProcessorConfig.getNitfProcessorConfigFilename());
            errEntry.setAppMessage("The '" + NitfProcessorConfig.MIN_VALUE + "' attribute " + "must be less than the '" + NitfProcessorConfig.MAX_VALUE + "' attribute in the '" + urgencyEl.getLocalName() + "' element but it is not: '" + minValue + "' is not less than '" + maxValue + "'.");
            logger.logError(errEntry);
            return false;
        }
        NodeList parsedUrgencyRangeNodeList = urgencyEl.getChildNodes();
        for (int i = 0; i < parsedUrgencyRangeNodeList.getLength(); i++) {
            Node parsedUrgencyRangeNode = parsedUrgencyRangeNodeList.item(i);
            if (parsedUrgencyRangeNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element parsedUrgencyRangeEl = (Element) parsedUrgencyRangeNode;
            ParsedRange parsedRange = new ParsedRange();
            if (!parsedRange.init(parsedUrgencyRangeEl)) {
                return false;
            }
            parsedRangeList.add(parsedRange);
        }
        return true;
    }

    /**
     * Applies a transform or process to an Nitf document.
     *
     */
    boolean xform(NitfDoc nitfDoc) {
        int parsedRangeListSize = parsedRangeList.size();
        String docStr = nitfDoc.getDoc();
        String valueStr = null;
        boolean success = true;
        int weightedValue = 0;
        int totalWeightedValue = 0;
        for (int i = 0; i < parsedRangeListSize; i++) {
            ParsedRange parsedRange = (ParsedRange) parsedRangeList.get(i);
            boolean foundMatch = strings.matches(parsedRange.identifyingPattern, docStr, parsedRange.ignoreCase);
            valueStr = strings.getGroup(parsedRange.groupInPattern);
            if (!foundMatch || valueStr == null) {
                String errorMsg = "Error calculating Urgency. Could not find a match in group '" + parsedRange.groupInPattern + "' of the following regex in the '" + NitfProcessorConfig.IDENTIFYING_PATTERN + "' of a '" + NitfProcessorConfig.PARSED_URGENCY_RANGE + "' element: " + parsedRange.identifyingPattern + ". Using value derived from '" + NitfProcessorConfig.ERROR_VALUE + "' attribute of the '" + NitfProcessorConfig.PARSED_URGENCY_ERROR_ACTIONS + "' element: " + parsedRange.errorValue + ".";
                if (!executeErrorAction(nitfDoc, parsedRange.parsedUrgencyErrorActionsEl, NitfProcessorConfig.NO_PATTERN_MATCH_ERROR_ACTION, errorMsg)) {
                    success = false;
                }
                valueStr = "" + parsedRange.errorValue;
            } else {
                Object lookedUpValueStrObj = null;
                if (parsedRange.valueMap != null) {
                    String valueMapKey = valueStr;
                    if (parsedRange.ignoreCase) {
                        valueMapKey = valueMapKey.toUpperCase();
                    }
                    lookedUpValueStrObj = parsedRange.valueMap.get(valueMapKey);
                    if (lookedUpValueStrObj == null) {
                        String errorMsg = "Parsed out urgency value '" + valueStr + "' could not be converted to a number because it was " + "not found in the following value in the' " + NitfProcessorConfig.VALUE_MAP + "' attribute of the '" + NitfProcessorConfig.PARSED_URGENCY_RANGE + "' element: " + parsedRange.valueMapStr + ". Using value derived from '" + NitfProcessorConfig.ERROR_VALUE + "' attribute of the '" + NitfProcessorConfig.PARSED_URGENCY_ERROR_ACTIONS + "' element: " + parsedRange.errorValue + ".";
                        if (!executeErrorAction(nitfDoc, parsedRange.parsedUrgencyErrorActionsEl, NitfProcessorConfig.PARSED_VALUE_FORMAT_ERROR_ACTION, errorMsg)) {
                            success = false;
                        }
                        lookedUpValueStrObj = "" + parsedRange.errorValue;
                    }
                    valueStr = (String) lookedUpValueStrObj;
                }
            }
            int value = 0;
            try {
                value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                String errorMsg = "'" + valueStr + "' parsed from doc with '" + parsedRange.identifyingPattern + "' regex in '" + NitfProcessorConfig.IDENTIFYING_PATTERN + "' attribute of the '" + NitfProcessorConfig.PARSED_URGENCY_RANGE + "' element is not a number.";
                if (!executeErrorAction(nitfDoc, parsedRange.parsedUrgencyErrorActionsEl, NitfProcessorConfig.PARSED_VALUE_FORMAT_ERROR_ACTION, errorMsg)) {
                    success = false;
                }
            }
            if (value > parsedRange.maxValue) {
                value = parsedRange.valueOutsideMaxValue;
            } else if (value < parsedRange.minValue) {
                value = parsedRange.valueOutsideMinValue;
            }
            value = value - parsedRange.baseValue;
            if (parsedRange.inversionBaseValue > 0) {
                value = parsedRange.inversionBaseValue - value;
            }
            weightedValue = (int) (parsedRange.weight * value);
            totalWeightedValue += weightedValue;
        }
        if (totalWeightedValue > this.maxValue) {
            String errorMsg = "The calculated Urgency (Rank) of '" + totalWeightedValue + "' is greater than the maximum allowed '" + this.maxValue + "' as specified in the '" + NitfProcessorConfig.MAX_VALUE + "' attribute in the '" + urgencyEl.getLocalName() + "' element. Using MaxValue: " + this.maxValue;
            if (!executeErrorAction(nitfDoc, urgencyEl, NitfProcessorConfig.OUTSIDE_VALUE_ACTION, errorMsg)) {
                success = false;
            }
            totalWeightedValue = this.maxValue;
        } else if (totalWeightedValue < this.minValue) {
            String errorMsg = "The calculated Urgency (Rank) of '" + totalWeightedValue + "' is less than the minimum allowed '" + this.minValue + "' as specified in the '" + NitfProcessorConfig.MIN_VALUE + "' attribute in the '" + urgencyEl.getLocalName() + "' element. Using MinValue: " + this.minValue;
            if (!executeErrorAction(nitfDoc, urgencyEl, NitfProcessorConfig.OUTSIDE_VALUE_ACTION, errorMsg)) {
                success = false;
            }
            totalWeightedValue = this.minValue;
        }
        String urgencyElStr = "<urgency ed-urg=\"" + totalWeightedValue + "\"/>";
        xformerUtils.insertChildOfDocdata(nitfDoc, "urgency", urgencyElStr);
        return success;
    }

    /**
     * Gets the "ErrorAction" attribute out of the passed in element. Can
     * insert "NotReadyToPublish" and "DocInfo" meta tags, depending on the
     * value in the "ErrorAction" attribute.
     *
     * @param nitfDoc       The holder of the incoming Nitf document and its attributes.
     *
     * @param elementWithErrorActionAttr
     *                      The element which has an ErrorAction attribute.
     *
     * @param errorAttrName The name of the ErrorAction attribute.
     *
     * @param errorMsg      The message to insert on DocInfo meta tags and in logs.
     *
     * @return True if errors should be ignored.
     */
    private boolean executeErrorAction(NitfDoc nitfDoc, Element elementWithErrorActionAttr, String errorAttrName, String errorMsg) {
        String errorActionAttr = elementWithErrorActionAttr.getAttribute(errorAttrName);
        if (errorActionAttr.equals(NitfProcessorConfig.THROW_AWAY)) {
            nitfDoc.getDocStatus().put(NitfDocStatus.THROW_AWAY, errorMsg);
            return false;
        } else if (errorActionAttr.equals(NitfProcessorConfig.BAD_LOCATION)) {
            nitfDoc.getDocStatus().put(NitfDocStatus.PROCESSING_ERROR_MOVE_TO_BAD, errorMsg);
            xformerUtils.insertMetaTagInNitfDoc(nitfDoc, NitfDoc.DOC_INFO, errorMsg);
            return false;
        } else if (errorActionAttr.equals(NitfProcessorConfig.NOT_READY_TO_PUBLISH)) {
            nitfDoc.getDocStatus().put(NitfDocStatus.NOT_READY_TO_PUBLISH, errorMsg);
            xformerUtils.insertMetaTagInNitfDoc(nitfDoc, NitfDoc.READY_TO_PUBLISH, NitfDoc.FALSE);
            xformerUtils.insertMetaTagInNitfDoc(nitfDoc, NitfDoc.DOC_INFO, errorMsg);
            return false;
        } else if (errorActionAttr.equals(NitfProcessorConfig.DOC_INFO_MESSAGE)) {
            nitfDoc.getDocStatus().put(NitfDocStatus.DOC_INFO_WARNING_MESSAGE, errorMsg);
            xformerUtils.insertMetaTagInNitfDoc(nitfDoc, NitfDoc.DOC_INFO, errorMsg);
            return false;
        }
        return true;
    }

    /**
     * Examines the 'ErrorValue' attribute of the passed in element to
     * see which value should be returned.
     *
     * @param minValue                  If the passed in attribute is set to
     *                                  "MinValue" then this value returned.
     *
     * @param maxValue                  If the passed in attribute is set to
     *                                  "MaxValue" then this value returned.
     *
     * @param customErrorValue          If the passed in attribute is set to
     *                                  "CustomErrorValue" then this value returned.
     *
     * @param elementWithErrorValueAttr The element that has the ErrorValue attribute.
     *
     * @return The value selected by the ErrorValue attribute.
     */
    private int deriveErrorValue(int minValue, int maxValue, int customErrorValue, Element elementWithErrorValueAttr) {
        int errorValue = 0;
        String errorValueAttr = elementWithErrorValueAttr.getAttribute(NitfProcessorConfig.ERROR_VALUE);
        if (errorValueAttr.equals(NitfProcessorConfig.MIN_VALUE)) {
            errorValue = minValue;
        } else if (errorValueAttr.equals(NitfProcessorConfig.MAX_VALUE)) {
            errorValue = maxValue;
        } else if (errorValueAttr.equals(NitfProcessorConfig.MIN_MAX_VALUE_MIDPOINT)) {
            errorValue = (minValue + maxValue) / 2;
        } else if (errorValueAttr.equals(NitfProcessorConfig.CUSTOM_ERROR_VALUE)) {
            errorValue = customErrorValue;
        } else {
            errorValue = 0;
        }
        return errorValue;
    }

    /**
     * A holding class for the data found in attributes of the "ParsedUrgencyRange"
     * config file element.
     */
    private class ParsedRange {

        String identifyingPattern = null;

        int groupInPattern = 0;

        boolean ignoreCase = true;

        int minValue = 0;

        int maxValue = 0;

        int valueOutsideMinValue = minValue;

        int valueOutsideMaxValue = maxValue;

        int errorValue = 0;

        int baseValue = 0;

        int inversionBaseValue = 0;

        float weight = 0;

        String valueMapStr = null;

        Element parsedUrgencyErrorActionsEl = null;

        HashMap valueMap = null;

        /**
         * Loads and validates all the data in the attributes of the "ParsedUrgencyRange"
         * element and stores it in this object's attributes.
         *
         * @param parsedUrgencyRangeEl A "ParsedUrgencyRange" element in the config file.
         *
         * @return boolean True if no errors found in attributes.
         */
        boolean init(Element parsedUrgencyRangeEl) {
            this.identifyingPattern = parsedUrgencyRangeEl.getAttribute(NitfProcessorConfig.IDENTIFYING_PATTERN);
            if (!xformerUtils.isRegexValid(this.identifyingPattern, NitfProcessorConfig.PARSED_URGENCY_RANGE, NitfProcessorConfig.IDENTIFYING_PATTERN)) {
                return false;
            }
            Element parsedUrgencyErrorActionsEl = (Element) xmlParseUtils.getChildNode(parsedUrgencyRangeEl, NitfProcessorConfig.PARSED_URGENCY_ERROR_ACTIONS);
            this.parsedUrgencyErrorActionsEl = parsedUrgencyErrorActionsEl;
            String groupInPatternStr = parsedUrgencyRangeEl.getAttribute(NitfProcessorConfig.GROUP_IN_PATTERN);
            this.groupInPattern = Integer.parseInt(groupInPatternStr);
            String ignoreCaseStr = parsedUrgencyRangeEl.getAttribute(NitfProcessorConfig.IGNORE_CASE);
            if ("No".equalsIgnoreCase(ignoreCaseStr)) {
                ignoreCase = false;
            } else {
                ignoreCase = true;
            }
            int customErrorValue = 0;
            try {
                this.minValue = parseValue(parsedUrgencyRangeEl, NitfProcessorConfig.MIN_VALUE);
                this.maxValue = parseValue(parsedUrgencyRangeEl, NitfProcessorConfig.MAX_VALUE);
                this.valueOutsideMinValue = parseValue(parsedUrgencyRangeEl, NitfProcessorConfig.VALUE_OUTSIDE_MIN_VALUE);
                this.valueOutsideMaxValue = parseValue(parsedUrgencyRangeEl, NitfProcessorConfig.VALUE_OUTSIDE_MAX_VALUE);
                this.baseValue = parseValue(parsedUrgencyRangeEl, NitfProcessorConfig.BASE_VALUE);
                this.inversionBaseValue = parseValue(parsedUrgencyRangeEl, NitfProcessorConfig.INVERSION_BASE_VALUE);
                customErrorValue = parseValue(parsedUrgencyErrorActionsEl, NitfProcessorConfig.CUSTOM_ERROR_VALUE);
                String valueMapStr = parsedUrgencyRangeEl.getAttribute(NitfProcessorConfig.VALUE_MAP);
                this.valueMap = parseValueMap(valueMapStr);
            } catch (ConfigException e) {
                return false;
            }
            if (minValue >= maxValue) {
                errEntry.setAppContext("ParsedRange.init()");
                errEntry.setDocInfo(nitfProcessorConfig.getNitfProcessorConfigFilename());
                errEntry.setAppMessage("The '" + NitfProcessorConfig.MIN_VALUE + "' attribute " + "must be less than the '" + NitfProcessorConfig.MAX_VALUE + "' attribute in the '" + parsedUrgencyRangeEl.getLocalName() + "' elements but it is not: '" + minValue + "' is not less than '" + maxValue + "'.");
                logger.logError(errEntry);
                return false;
            }
            this.errorValue = deriveErrorValue(minValue, maxValue, customErrorValue, parsedUrgencyErrorActionsEl);
            String weightStr = parsedUrgencyRangeEl.getAttribute(NitfProcessorConfig.WEIGHT);
            if (!xformerUtils.isNumberValid(weightStr, true, parsedUrgencyRangeEl.getLocalName(), NitfProcessorConfig.WEIGHT)) {
                return false;
            }
            this.weight = Float.parseFloat(weightStr);
            return true;
        }

        /**
         * Parse and validate attributes in the "ParsedUrgencyRange" element that
         * end with "Value". They should either integer.
         *
         * @param parsedUrgencyRangeEl The "ParsedUrgencyRange" element.
         *
         * @param attrName             The name of the "Value" attribute in the above element.
         *
         * @return The number representing the value.
         * @exception Exception If 'attrName' is not a valid integer.
         */
        int parseValue(Element element, String attrName) throws ConfigException {
            String valueStr = element.getAttribute(attrName);
            int noWeightValue = 0;
            if (attrName.equals(NitfProcessorConfig.VALUE_OUTSIDE_MAX_VALUE)) {
                if (valueStr.equals(NitfProcessorConfig.MAX_VALUE)) {
                    return this.maxValue;
                } else {
                    return noWeightValue;
                }
            }
            if (attrName.equals(NitfProcessorConfig.VALUE_OUTSIDE_MIN_VALUE)) {
                if (valueStr.equals(NitfProcessorConfig.MIN_VALUE)) {
                    return this.minValue;
                } else {
                    return noWeightValue;
                }
            }
            if (!xformerUtils.isNumberValid(valueStr, false, element.getLocalName(), attrName)) {
                throw new ConfigException();
            }
            return Integer.parseInt(valueStr);
        }

        /**
         * Validates the 'ValueMap' attribute of the 'ParsedUrgencyRange' element
         * and converts it to a HashMap.
         *
         * @param valueMapStr The String to convert to a HashMap.
         *
         * @return The HashMap converted from the input string.
         * @exception ConfigException
         */
        HashMap parseValueMap(String valueMapStr) throws ConfigException {
            this.valueMapStr = valueMapStr;
            if (valueMapStr == null || valueMapStr.equals("")) {
                return null;
            }
            if (!strings.matches("^( *[^=, ]+ *= *\\d+ *,? *)+$", valueMapStr)) {
                errEntry.setAppContext("ParsedRange.parseValueMap()");
                errEntry.setDocInfo(nitfProcessorConfig.getNitfProcessorConfigFilename());
                errEntry.setAppMessage("'" + valueMapStr + "' is not a valid number in '" + NitfProcessorConfig.PARSED_URGENCY_RANGE + "' element, '" + NitfProcessorConfig.VALUE_MAP + "' attribute. " + "It must have 'alpha=number,' syntax as shown in the following: \n" + "<ParsedUrgencyRange ... ValueMap=\"A=1,B=2,C=3,D=4,E=5,F=6,G=7,H=9\" ... ");
                logger.logError(errEntry);
                throw new ConfigException();
            }
            HashMap valueMap = new HashMap();
            StringTokenizer st = new StringTokenizer(valueMapStr, ",");
            while (st.hasMoreTokens()) {
                String nameValueStr = (String) st.nextElement();
                if (strings.matches(" *([^= ]+) *= *(\\d+) *", nameValueStr)) {
                    String name = strings.getGroup(1);
                    if (this.ignoreCase) {
                        name = name.toUpperCase();
                    }
                    String value = strings.getGroup(2);
                    valueMap.put(name, value);
                }
            }
            return valueMap;
        }
    }
}

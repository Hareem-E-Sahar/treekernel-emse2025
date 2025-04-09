package org.bungeni.editor.toolbar.conditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bungeni.editor.toolbar.conditions.operators.baseOperator;
import org.bungeni.ooo.OOComponentHelper;
import org.bungeni.ooo.utils.CommonExceptionUtils;

/**
 *
 * @author Administrator
 */
public class BungeniToolbarConditionProcessor {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BungeniToolbarConditionProcessor.class.getName());

    protected BungeniToolbarConditionOperator matchedCondition = null;

    protected String conditionValue;

    protected String[] individualConditions;

    protected static final HashMap<String, BungeniToolbarConditionOperator> operators = BungeniToolbarConditionOperatorFactory.getObjects();

    /** Creates a new instance of BungeniToolbarConditionProcessor */
    public BungeniToolbarConditionProcessor(String conditionVal) {
        this.conditionValue = conditionVal;
        processOperators(conditionVal);
    }

    class groupBoundary {

        int start;

        int end;

        groupBoundary(int s, int e) {
            start = s;
            end = e;
        }
    }

    private static String GROUP_BEGIN = "{";

    private static String GROUP_END = "}";

    /**
     * 
     * match bracket groups ... find th
     * @param conditionValue
     */
    protected void processGroups(String conditionValue) {
        ArrayList<groupBoundary> foundGroups = new ArrayList<groupBoundary>();
        if (conditionValue.contains(GROUP_BEGIN) && conditionValue.contains(GROUP_END)) {
            Pattern pGroup = Pattern.compile("\\{(.+?)\\}");
            Matcher m = pGroup.matcher(conditionValue);
            while (m.find()) {
                int ns = m.start();
                int ne = m.end();
                foundGroups.add(new groupBoundary(ns, ne));
            }
            processFoundGroups(foundGroups, conditionValue);
        } else {
            processOperators(conditionValue);
        }
    }

    private void processFoundGroups(ArrayList<groupBoundary> foundGroups, String conditionVal) {
    }

    /**
     * processes conditions of the type :
     * cursorInSection:clause([0-9]*) :and: textSelected:true
     * currently grouping of conditions is not supported.
     * 23 July 2008 - to be added grouping of conditions  e.g.
     * {cursorInSection:clause([0-9]*) :or: cursorInSection:article([0-9]*)} :and: textSelected:true
     * 
     * @param fullConditionValue
     */
    protected void processOperators(String fullConditionValue) {
        log.debug("1. processOperators: " + fullConditionValue);
        java.util.Iterator<String> keys = operators.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            BungeniToolbarConditionOperator condition = operators.get(key);
            if (fullConditionValue.indexOf(condition.getCondition()) != -1) {
                matchedCondition = condition;
                individualConditions = fullConditionValue.split(condition.getCondition());
                return;
            }
        }
    }

    /**
     * This map caches condition operator objects.  The evaluate is repeatedly called by the editor based on the 
     * cursor position, whicn means these objects are repeatedly created and garbage collected.
     * By caching the objects we simply reload an already created object from memory and return that.
     */
    private static HashMap<String, IBungeniToolbarConditionOperator> toolbarConditionOperatorMap = new HashMap<String, IBungeniToolbarConditionOperator>();

    private static boolean conditionProcessorClassExists(String className) {
        if (toolbarConditionOperatorMap.containsKey(className)) {
            return true;
        } else {
            return false;
        }
    }

    private static void setConditionOperator(String className, IBungeniToolbarConditionOperator operatorObj) {
        toolbarConditionOperatorMap.put(className, operatorObj);
    }

    private static IBungeniToolbarConditionOperator getConditionOperator(String className) {
        return toolbarConditionOperatorMap.get(className);
    }

    private boolean evaluateWithOperator(OOComponentHelper ooDocument) {
        boolean bResult = false;
        try {
            String conditionProcessorClass = matchedCondition.getConditionProcessorClass();
            if (conditionProcessorClassExists(conditionProcessorClass)) {
                IBungeniToolbarConditionOperator selectedOperator = getConditionOperator(conditionProcessorClass);
                selectedOperator.setOperatingCondition(matchedCondition, individualConditions);
                bResult = selectedOperator.result(ooDocument);
            } else {
                IBungeniToolbarConditionOperator selectedOperator;
                Class processorClassRef;
                processorClassRef = Class.forName(conditionProcessorClass);
                selectedOperator = (IBungeniToolbarConditionOperator) processorClassRef.newInstance();
                setConditionOperator(conditionProcessorClass, selectedOperator);
                selectedOperator.setOperatingCondition(matchedCondition, individualConditions);
                bResult = selectedOperator.result(ooDocument);
            }
        } catch (InstantiationException ex) {
            log.error("evaluateWithOperator: " + ex.getMessage());
            log.error("evaluateWithOperator : " + this.conditionValue);
            log.error("evaluateWithOperator: " + CommonExceptionUtils.getStackTrace(ex));
        } catch (IllegalAccessException ex) {
            log.error("evaluateWithOperator: " + ex.getMessage());
            log.error("evaluateWithOperator : " + this.conditionValue);
            log.error("evaluateWithOperator: " + CommonExceptionUtils.getStackTrace(ex));
        } catch (ClassNotFoundException ex) {
            log.error("evaluateWithOperator: " + ex.getMessage());
            log.error("evaluateWithOperator : " + this.conditionValue);
            log.error("evaluateWithOperator: " + CommonExceptionUtils.getStackTrace(ex));
        } finally {
            return bResult;
        }
    }

    private static HashMap<String, IBungeniToolbarCondition> toolbarConditionMap = new HashMap<String, IBungeniToolbarCondition>();

    private static boolean conditionExists(String cClass) {
        if (toolbarConditionMap.containsKey(cClass)) {
            return true;
        } else {
            return false;
        }
    }

    private static void setCondition(String conditionName, IBungeniToolbarCondition conditionObj) {
        toolbarConditionMap.put(conditionName, conditionObj);
    }

    private static IBungeniToolbarCondition getCondition(String conditionName) {
        return toolbarConditionMap.get(conditionName);
    }

    private boolean evaluateWithoutOperator(OOComponentHelper ooDocument) {
        boolean bResult = false;
        try {
            BungeniToolbarCondition toolbarCond = new BungeniToolbarCondition(conditionValue);
            String conditionClass = toolbarCond.getConditionClass();
            if (conditionExists(conditionClass)) {
                IBungeniToolbarCondition iCondition = getCondition(conditionClass);
                bResult = iCondition.processCondition(ooDocument, toolbarCond);
            } else {
                IBungeniToolbarCondition iCondition = baseOperator.getConditionObject(conditionClass);
                setCondition(conditionClass, iCondition);
                bResult = iCondition.processCondition(ooDocument, toolbarCond);
            }
        } catch (Exception ex) {
            log.error("evaluateWithoutOperator: " + ex.getMessage());
            log.error("evaluateWithoutOperator : " + this.conditionValue);
            log.error("evaluateWithoutOperator: " + CommonExceptionUtils.getStackTrace(ex));
        } finally {
            return bResult;
        }
    }

    public boolean evaluate(OOComponentHelper ooDocument) {
        boolean bResult = false;
        if (matchedCondition == null) {
            bResult = evaluateWithoutOperator(ooDocument);
        } else {
            bResult = evaluateWithOperator(ooDocument);
        }
        return bResult;
    }
}

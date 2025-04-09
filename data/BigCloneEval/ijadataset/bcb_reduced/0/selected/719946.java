package com.daffodilwoods.daffodildb.utils.parser;

import java.util.ArrayList;
import java.util.Arrays;
import com.daffodilwoods.database.utility.P;
import com.daffodilwoods.daffodildb.utils.DBStack;

/**
    * FOR NONRECURSIVE RULES BEST OPTION IS TO BE USED.
    * FOR COMPARABLERULES BINARY IS TO BE USED.
    * FOR RECURSIVE RULES BEST OPTION IS TO BE USED.
    */
public class OrProductionRulesWithHashMap extends ProductionRulesWithHashMap {

    /**
    * Array For Comparable Rules
    */
    Object[] comparableRules;

    /**
    * Array For Recursive Rules
    */
    Object[] recursiveObjects;

    /**
    * Array For NonRecursive Rules
    */
    Object[] nonRecursiveObjects;

    Object[] nonRecWithBest;

    Object[] nonRecWithoutBest;

    OrProductionRulesWithHashMap(ClassLoader classLoader0) {
        super(classLoader0);
    }

    /**
    * ParsePart Method of OrProductionRules
    * Calling parseForToken as without Best Option
    * Calling parse of query as With Best Option
    */
    public Object parsePart(ParseElements pe) throws com.daffodilwoods.database.resource.DException {
        if (pe.bestOptionFlag) {
            if (pe.hashMap.containsKey(nameOfRule)) return withBestOption(pe); else return withoutBestOptionForSQL(pe);
        }
        return withoutBestOptionForToken(pe);
    }

    private Object withoutBestOptionForToken(ParseElements pe) throws com.daffodilwoods.database.resource.DException {
        Object value = null;
        int ruleSize = nonRecursiveObjects == null ? -1 : nonRecursiveObjects.length;
        for (int i = 0; i < ruleSize; i++) {
            value = ((ProductionRules) nonRecursiveObjects[i]).parse(pe);
            if (!(value instanceof ParseException)) {
                if (!recursiveflag) return value;
                pe.recursiveObject = value;
                pe.recursionState = nameOfRule;
                i = -1;
            }
        }
        return value == null ? pe.tokenFlag ? applyBinary(pe) : pe.parseException : value;
    }

    /**
   * This Function is For parsing withoutBest Rules first then WithBest Rules.
   */
    private Object withoutBestOptionForSQL(ParseElements pe) throws com.daffodilwoods.database.resource.DException {
        Object object1 = pe.recursiveObject;
        String recState = pe.recursionState;
        Object asdf = null;
        Object o = comparableRules == null ? null : applyBinary(pe);
        if (o != null) {
            if (!(o instanceof ParseException)) return o;
        }
        int ruleSize = nonRecursiveObjects == null ? -1 : nonRecursiveObjects.length - 1;
        for (int i = ruleSize; i >= 0; i--) {
            ProductionRules object = (ProductionRules) nonRecursiveObjects[i];
            Object value = object.parse(pe);
            if (!(value instanceof ParseException)) {
                asdf = value;
                if (!recursiveflag) return asdf;
                object1 = asdf;
                recState = nameOfRule;
            }
            pe.recursiveObject = object1;
            pe.recursionState = recState;
        }
        if (asdf == null) return pe.parseException; else return parseForRecusriveRules(asdf, pe);
    }

    /**
   * Method for getting RecursiveObjects for nameOfRule passed as an argument.
   * If all of childs of thie rule is recursive then rule returns itself to it's parent.
   * otherwise, this rule returns all of it's childs as recursive to it's parent.
   * If any of it's childs is not Recursive then it returns null to its parent.
   * Stack OccuredRules passed is used to avoid recursion.
   */
    public Object getRecursiveObject(String nameOfRule, DBStack occuredRules) {
        if (rules == null || rules.length == 0) return null;
        int size = rules.length - 1;
        ArrayList list = new ArrayList();
        for (int i = 0; i < rules.length; ++i) {
            ProductionRules production = (ProductionRules) rules[i];
            if (!list.contains(production) && !production.ruleKey.equalsIgnoreCase(nameOfRule) && !this.ruleKey.equalsIgnoreCase(production.ruleKey) && !occuredRules.contains(production.ruleKey)) {
                occuredRules.push(production.ruleKey);
                Object[] object = (Object[]) production.getRecursiveObject(nameOfRule, occuredRules);
                if (object != null) {
                    for (int k = 0; k < object.length; ++k) if (!list.contains(object[k])) list.add(object[k]);
                }
            }
        }
        if (!list.isEmpty()) {
            if (Arrays.equals(list.toArray(), rules)) return new Object[] { this };
            return list.toArray();
        }
        return null;
    }

    /**
   * Method for getting NonRecursiveObjects for nameOfRule passed as an argument.
   * If Rule is recursive for itself then this rule returns itself to it's parent.
   * otherwise, this rule returns all of it's childs as recursive to it's parent.
   * If any of it's childs is not nonRecursive then it returns null to its parent.
   * Stack OccuredRules passed is used to avoid recursion.
   */
    public Object getNonRecursiveObject(String nameOfRule, DBStack occuredRules) {
        if (rules == null || rules.length == 0) return null;
        int size = rules.length;
        ArrayList list = new ArrayList();
        for (int i = 0; i < size; ++i) {
            ProductionRules production = (ProductionRules) rules[i];
            if (!list.contains(production) && !production.ruleKey.equalsIgnoreCase(nameOfRule) && !this.ruleKey.equalsIgnoreCase(production.ruleKey) && !occuredRules.contains(production.ruleKey)) {
                occuredRules.push(production.ruleKey);
                Object[] object = (Object[]) production.getNonRecursiveObject(nameOfRule, occuredRules);
                if (object != null) {
                    for (int k = 0; k < object.length; ++k) if (!list.contains(object[k])) list.add(object[k]);
                }
            }
        }
        if (!list.isEmpty()) {
            DBStack a = new DBStack();
            a.push(this.ruleKey);
            Object arr[] = (Object[]) getRecursiveObject(this.ruleKey, a);
            if (arr != null) return new Object[] { this };
            return list.toArray();
        }
        return null;
    }

    /**
   * Method for Applying Binary to Rules.
   * Make a call for method specialHandling.
   * This special provision is made for Rules other Than OrProductionRules Whose first rule
   * is OrProductionRule and it fails.Then parse of its left and right rules are called.
   */
    private Object applyBinary(ParseElements pe) throws com.daffodilwoods.database.resource.DException {
        if (comparableRules == null) return pe.parseException;
        int low = 0, high = comparableRules.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            ProductionRules object = (ProductionRules) comparableRules[mid];
            Object obj = object.parse(pe);
            if (obj instanceof ParseException) {
                ParseException parseException = (ParseException) obj;
                if (parseException.returnType == 2) {
                    Object o = specialHandling(mid - 1, mid + 1, pe, low, high);
                    if (!(o instanceof ParseException)) return o;
                    parseException = (ParseException) o;
                }
                if (parseException.returnType < 0) high = mid - 1; else if (parseException.returnType > 0) low = mid + 1; else return obj;
            } else {
                if (!object.recursiveflag) return obj; else {
                    pe.recursiveObject = obj;
                    pe.recursionState = nameOfRule;
                    return parseForRecusriveRules(obj, pe);
                }
            }
        }
        return pe.parseException;
    }

    private Object specialHandling(int below, int above, ParseElements pe, int low, int high) throws com.daffodilwoods.database.resource.DException {
        ProductionRules object = null;
        if (below >= 0) {
            object = (ProductionRules) comparableRules[below];
            Object obj = object.parse(pe);
            if (!(obj instanceof ParseException)) return obj;
        }
        if (above < comparableRules.length) {
            object = (ProductionRules) comparableRules[above];
            Object obj = object.parse(pe);
            if (!(obj instanceof ParseException)) return obj;
        }
        return parseForRestOfComparableRules(pe, low, high);
    }

    private Object parseForRestOfComparableRules(ParseElements pe, int low, int high) throws com.daffodilwoods.database.resource.DException {
        for (int i = low; i < high; ++i) {
            ProductionRules object = (ProductionRules) comparableRules[i];
            Object obj = object.parse(pe);
            if (!(obj instanceof ParseException)) if (!object.recursiveflag) return obj; else {
                pe.recursiveObject = obj;
                pe.recursionState = nameOfRule;
                return parseForRecusriveRules(obj, pe);
            }
        }
        return pe.parseException;
    }

    /**
   * ApplyBinary Method is used to have Best Rule among all the orRules.
   */
    private Object applyBinary(ParseElements pe, Best best) throws com.daffodilwoods.database.resource.DException {
        if (comparableRules == null) return pe.parseException;
        int low = 0, high = comparableRules.length - 1;
        int position = pe.position;
        while (low <= high) {
            int mid = (low + high) / 2;
            ProductionRules object = (ProductionRules) comparableRules[mid];
            Object obj = object.parse(pe);
            if (obj instanceof ParseException) {
                ParseException parseException = (ParseException) obj;
                if (!pe.tokenFlag && parseException.returnType == 2) {
                    specialHandling(mid - 1, mid + 1, pe, best, position);
                    return best.sobject;
                }
                if (parseException.returnType < 0) high = mid - 1; else if (parseException.returnType > 0) low = mid + 1; else return obj;
            } else {
                if (position != pe.position) best.update(obj, pe.position);
                if (!object.recursiveflag) {
                    specialHandling(mid - 1, mid + 1, pe, best, position);
                    return best.sobject;
                }
                pe.recursiveObject = best.sobject;
                pe.recursionState = nameOfRule;
                return parseForRecusriveRules(best, pe);
            }
        }
        return pe.parseException;
    }

    private void specialHandling(int below, int above, ParseElements pe, Best best, int position) throws com.daffodilwoods.database.resource.DException {
        ProductionRules object = null;
        pe.position = position;
        if (below >= 0) {
            object = (ProductionRules) comparableRules[below];
            Object obj = object.parse(pe);
            if (!(obj instanceof ParseException)) {
                if (position != pe.position) best.update(obj, pe.position);
            }
        }
        if (above < comparableRules.length) {
            object = (ProductionRules) comparableRules[above];
            pe.position = position;
            Object obj = object.parse(pe);
            if (!(obj instanceof ParseException)) {
                if (position != pe.position) best.update(obj, pe.position);
            }
        }
    }

    /**
    * Method for calling parse of NonRecursive Rules
    * NonRecursive Rules are categorised in to two categories.
    *    a) Rules which are Comparable.
    *    b) Rules which are Non Comparable.
    * At First, parse of Non Comparable Rule is called with Best Option.
    * Then Parse of Comparable Rule is called with Binary.
    * Thereafter parse Of Recusrive Rules are called wityh Best Option.
    */
    private Object withBestOption(ParseElements pe) throws com.daffodilwoods.database.resource.DException {
        Object object1 = pe.recursiveObject;
        String recState = pe.recursionState;
        Best best = new Best();
        int position = pe.position;
        int ruleSize = nonRecursiveObjects == null ? -1 : nonRecursiveObjects.length - 1;
        for (int i = ruleSize; i >= 0; i--) {
            ProductionRules object = (ProductionRules) nonRecursiveObjects[i];
            Object value = object.parse(pe);
            if (position != pe.position && !(value instanceof ParseException)) {
                best.update(value, pe.position);
                pe.position = position;
            }
            pe.recursiveObject = object1;
            pe.recursionState = recState;
        }
        applyBinary(pe, best);
        if (best.sposition > position) {
            pe.position = best.sposition;
            if (!recursiveflag) return best.sobject;
            position = best.sposition;
            pe.recursiveObject = best.sobject;
            pe.recursionState = nameOfRule;
            return parseForRecusriveRules(best, pe);
        }
        return pe.parseException;
    }

    /**
    * Method For Calling Parse Of Recursive Rules if any.
    */
    private Object parseForRecusriveRules(Object asdf, ParseElements pe) throws com.daffodilwoods.database.resource.DException {
        Best best = new Best();
        best.update(asdf, pe.position);
        while (true) {
            int position = pe.position;
            int prvPosition = best.sposition;
            String st = pe.recursionState;
            Object asq = pe.recursiveObject;
            int ruleSize = recursiveObjects == null ? -1 : recursiveObjects.length - 1;
            for (int i = ruleSize; i >= 0; i--) {
                ProductionRules object = (ProductionRules) recursiveObjects[i];
                Object value = object.parse(pe);
                if (position != pe.position && !(value instanceof ParseException)) {
                    best.update(value, pe.position);
                    pe.position = position;
                }
                pe.recursiveObject = asq;
                pe.recursionState = st;
            }
            if (best.sposition > prvPosition) {
                pe.position = best.sposition;
                asdf = best.sobject;
                if (!recursiveflag) {
                    pe.recursiveObject = null;
                    pe.recursionState = null;
                    return asdf;
                }
                pe.recursiveObject = asdf;
                pe.recursionState = nameOfRule;
            } else {
                if (asdf == null) {
                    pe.recursiveObject = null;
                    pe.recursionState = null;
                    return pe.parseException;
                } else {
                    pe.recursiveObject = null;
                    pe.recursionState = null;
                    return asdf;
                }
            }
        }
    }

    /**
    * Method For Calling Parse Of Recursive Rules if any.
    */
    private Object parseForRecusriveRules(Best best, ParseElements pe) throws com.daffodilwoods.database.resource.DException {
        Object asdf = best.sobject;
        while (true) {
            int position = pe.position;
            int prvPosition = best.sposition;
            String st = pe.recursionState;
            Object asq = pe.recursiveObject;
            int ruleSize = recursiveObjects == null ? -1 : recursiveObjects.length - 1;
            for (int i = ruleSize; i >= 0; i--) {
                ProductionRules object = (ProductionRules) recursiveObjects[i];
                Object value = object.parse(pe);
                if (position != pe.position && !(value instanceof ParseException)) {
                    best.update(value, pe.position);
                    pe.position = position;
                }
                pe.recursiveObject = asq;
                pe.recursionState = st;
            }
            if (best.sposition > prvPosition) {
                pe.position = best.sposition;
                asdf = best.sobject;
                if (!recursiveflag) {
                    pe.recursiveObject = null;
                    pe.recursionState = null;
                    return asdf;
                }
                pe.recursiveObject = asdf;
                pe.recursionState = nameOfRule;
            } else {
                if (asdf == null) {
                    pe.recursiveObject = null;
                    pe.recursionState = null;
                    return pe.parseException;
                } else {
                    pe.recursiveObject = null;
                    pe.recursionState = null;
                    return asdf;
                }
            }
        }
    }

    public String toString() {
        return nameOfRule;
    }

    /**
    * Best Class is used to implementing Best Option.
    * It Holds the object whose parsePosition is best among all or Rules.
    */
    public static class Best {

        Object sobject;

        int sposition = -1;

        void update(Object object, int position) {
            if (position >= sposition) {
                sobject = object;
                sposition = position;
            }
        }

        public String toString() {
            StringBuffer str = new StringBuffer("[Best ");
            str.append(sposition).append("--").append(sobject).append("]");
            return str.toString();
        }
    }
}

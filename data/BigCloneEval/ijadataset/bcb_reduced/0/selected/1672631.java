package com.ibm.icu.text;

import com.ibm.icu.impl.UCharacterProperty;
import com.ibm.icu.impl.Utility;
import java.text.*;
import java.util.Vector;

/**
 * A collection of rules used by a RuleBasedNumberFormat to format and
 * parse numbers.  It is the responsibility of a RuleSet to select an
 * appropriate rule for formatting a particular number and dispatch
 * control to it, and to arbitrate between different rules when parsing
 * a number.
 * $RCSfile: NFRuleSet.java,v $ $Revision: 1.7 $ $Date: 2002/07/31 17:37:08 $
 */
final class NFRuleSet {

    /**
     * Puts a copyright in the .class file
     */
    private static final String copyrightNotice = "Copyright ©1997-1998 IBM Corp.  All rights reserved.";

    /**
     * The rule set's name
     */
    private String name;

    /**
     * The rule set's regular rules
     */
    private NFRule[] rules;

    /**
     * The rule set's negative-number rule
     */
    private NFRule negativeNumberRule = null;

    /**
     * The rule set's fraction rules: element 0 is the proper fraction
     * (0.x) rule, element 1 is the improper fraction (x.x) rule, and
     * element 2 is the master (x.0) rule.
     */
    private NFRule[] fractionRules = new NFRule[3];

    /**
     * True if the rule set is a fraction rule set.  A fraction rule set
     * is a rule set that is used to format the fractional part of a
     * number.  It is called from a >> substitution in another rule set's
     * fraction rule, and is only called upon to format values between
     * 0 and 1.  A fraction rule set has different rule-selection
     * behavior than a regular rule set.
     */
    private boolean isFractionRuleSet = false;

    public NFRuleSet(String[] descriptions, int index) throws IllegalArgumentException {
        String description = descriptions[index];
        if (description.charAt(0) == '%') {
            int pos = description.indexOf(':');
            if (pos == -1) {
                throw new IllegalArgumentException("Rule set name doesn't end in colon");
            } else {
                name = description.substring(0, pos);
                while (pos < description.length() && UCharacterProperty.isRuleWhiteSpace(description.charAt(++pos))) {
                }
                description = description.substring(pos);
                descriptions[index] = description;
            }
        } else {
            name = "%default";
        }
        if (description.length() == 0) {
            throw new IllegalArgumentException("Empty rule set description");
        }
    }

    /**
     * Construct the subordinate data structures used by this object.
     * This function is called by the RuleBasedNumberFormat constructor
     * after all the rule sets have been created to actually parse
     * the description and build rules from it.  Since any rule set
     * can refer to any other rule set, we have to have created all of
     * them before we can create anything else.
     * @param description The textual description of this rule set
     * @param owner The formatter that owns this rule set
     */
    public void parseRules(String description, RuleBasedNumberFormat owner) {
        Vector ruleDescriptions = new Vector();
        int oldP = 0;
        int p = description.indexOf(';');
        while (oldP != -1) {
            if (p != -1) {
                ruleDescriptions.addElement(description.substring(oldP, p));
                oldP = p + 1;
            } else {
                if (oldP < description.length()) {
                    ruleDescriptions.addElement(description.substring(oldP));
                }
                oldP = p;
            }
            p = description.indexOf(';', p + 1);
        }
        Vector tempRules = new Vector();
        NFRule predecessor = null;
        for (int i = 0; i < ruleDescriptions.size(); i++) {
            Object temp = NFRule.makeRules((String) ruleDescriptions.elementAt(i), this, predecessor, owner);
            if (temp instanceof NFRule) {
                tempRules.addElement(temp);
                predecessor = (NFRule) temp;
            } else if (temp instanceof NFRule[]) {
                NFRule[] rulesToAdd = (NFRule[]) temp;
                for (int j = 0; j < rulesToAdd.length; j++) {
                    tempRules.addElement(rulesToAdd[j]);
                    predecessor = rulesToAdd[j];
                }
            }
        }
        ruleDescriptions = null;
        long defaultBaseValue = 0;
        int i = 0;
        while (i < tempRules.size()) {
            NFRule rule = (NFRule) tempRules.elementAt(i);
            switch((int) rule.getBaseValue()) {
                case 0:
                    rule.setBaseValue(defaultBaseValue);
                    if (!isFractionRuleSet) {
                        ++defaultBaseValue;
                    }
                    ++i;
                    break;
                case NFRule.NEGATIVE_NUMBER_RULE:
                    negativeNumberRule = rule;
                    tempRules.removeElementAt(i);
                    break;
                case NFRule.IMPROPER_FRACTION_RULE:
                    fractionRules[0] = rule;
                    tempRules.removeElementAt(i);
                    break;
                case NFRule.PROPER_FRACTION_RULE:
                    fractionRules[1] = rule;
                    tempRules.removeElementAt(i);
                    break;
                case NFRule.MASTER_RULE:
                    fractionRules[2] = rule;
                    tempRules.removeElementAt(i);
                    break;
                default:
                    if (rule.getBaseValue() < defaultBaseValue) {
                        throw new IllegalArgumentException("Rules are not in order");
                    }
                    defaultBaseValue = rule.getBaseValue();
                    if (!isFractionRuleSet) {
                        ++defaultBaseValue;
                    }
                    ++i;
                    break;
            }
        }
        rules = new NFRule[tempRules.size()];
        tempRules.copyInto((Object[]) rules);
    }

    /**
     * Flags this rule set as a fraction rule set.  This function is
     * called during the construction process once we know this rule
     * set is a fraction rule set.  We don't know a rule set is a
     * fraction rule set until we see it used somewhere.  This function
     * is not ad must not be called at any time other than during
     * construction of a RuleBasedNumberFormat.
     */
    public void makeIntoFractionRuleSet() {
        isFractionRuleSet = true;
    }

    /**
     * Compares two rule sets for equality.
     * @param that The other rule set
     * @return true if the two rule sets are functionally equivalent.
     */
    public boolean equals(Object that) {
        if (!(that instanceof NFRuleSet)) {
            return false;
        } else {
            NFRuleSet that2 = (NFRuleSet) that;
            if (!name.equals(that2.name) || !Utility.objectEquals(negativeNumberRule, that2.negativeNumberRule) || !Utility.objectEquals(fractionRules[0], that2.fractionRules[0]) || !Utility.objectEquals(fractionRules[1], that2.fractionRules[1]) || !Utility.objectEquals(fractionRules[2], that2.fractionRules[2]) || rules.length != that2.rules.length || isFractionRuleSet != that2.isFractionRuleSet) {
                return false;
            }
            for (int i = 0; i < rules.length; i++) {
                if (!rules[i].equals(that2.rules[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Builds a textual representation of a rule set.
     * @return A textual representation of a rule set.  This won't
     * necessarily be the same description that the rule set was
     * constructed with, but it will produce the same results.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(name + ":\n");
        for (int i = 0; i < rules.length; i++) {
            result.append("    " + rules[i].toString() + "\n");
        }
        if (negativeNumberRule != null) {
            result.append("    " + negativeNumberRule.toString() + "\n");
        }
        if (fractionRules[0] != null) {
            result.append("    " + fractionRules[0].toString() + "\n");
        }
        if (fractionRules[1] != null) {
            result.append("    " + fractionRules[1].toString() + "\n");
        }
        if (fractionRules[2] != null) {
            result.append("    " + fractionRules[2].toString() + "\n");
        }
        return result.toString();
    }

    /**
     * Says whether this rule set is a fraction rule set.
     * @return true if this rule is a fraction rule set; false if it isn't
     */
    public boolean isFractionSet() {
        return isFractionRuleSet;
    }

    public String getName() {
        return name;
    }

    /**
     * Formats a long.  Selects an appropriate rule and dispatches
     * control to it.
     * @param number The number being formatted
     * @param toInsertInto The string where the result is to be placed
     * @param pos The position in toInsertInto where the result of
     * this operation is to be inserted
     */
    public void format(long number, StringBuffer toInsertInto, int pos) {
        NFRule applicableRule = findNormalRule(number);
        applicableRule.doFormat(number, toInsertInto, pos);
    }

    /**
     * Formats a double.  Selects an appropriate rule and dispatches
     * control to it.
     * @param number The number being formatted
     * @param toInsertInto The string where the result is to be placed
     * @param pos The position in toInsertInto where the result of
     * this operation is to be inserted
     */
    public void format(double number, StringBuffer toInsertInto, int pos) {
        NFRule applicableRule = findRule(number);
        applicableRule.doFormat(number, toInsertInto, pos);
    }

    /**
     * Selects an apropriate rule for formatting the number.
     * @param number The number being formatted.
     * @return The rule that should be used to format it
     */
    private NFRule findRule(double number) {
        if (isFractionRuleSet) {
            return findFractionRuleSetRule(number);
        }
        if (number < 0) {
            if (negativeNumberRule != null) {
                return negativeNumberRule;
            } else {
                number = -number;
            }
        }
        if (number != Math.floor(number)) {
            if (number < 1 && fractionRules[1] != null) {
                return fractionRules[1];
            } else if (fractionRules[0] != null) {
                return fractionRules[0];
            }
        }
        if (fractionRules[2] != null) {
            return fractionRules[2];
        } else {
            return findNormalRule((long) Math.round(number));
        }
    }

    /**
     * If the value passed to findRule() is a positive integer, findRule()
     * uses this function to select the appropriate rule.  The result will
     * generally be the rule with the highest base value less than or equal
     * to the number.  There is one exception to this: If that rule has
     * two substitutions and a base value that is not an even multiple of
     * its divisor, and the number itself IS an even multiple of the rule's
     * divisor, then the result will be the rule that preceded the original
     * result in the rule list.  (This behavior is known as the "rollback
     * rule", and is used to handle optional text: a rule with optional
     * text is represented internally as two rules, and the rollback rule
     * selects appropriate between them.  This avoids things like "two
     * hundred zero".)
     * @param number The number being formatted
     * @return The rule to use to format this number
     */
    private NFRule findNormalRule(long number) {
        if (isFractionRuleSet) {
            return findFractionRuleSetRule(number);
        }
        if (number < 0) {
            if (negativeNumberRule != null) {
                return negativeNumberRule;
            } else {
                number = -number;
            }
        }
        int lo = 0;
        int hi = rules.length;
        if (hi > 0) {
            while (lo < hi) {
                int mid = (lo + hi) / 2;
                if (rules[mid].getBaseValue() == number) {
                    return rules[mid];
                } else if (rules[mid].getBaseValue() > number) {
                    hi = mid;
                } else {
                    lo = mid + 1;
                }
            }
            NFRule result = rules[hi - 1];
            if (result.shouldRollBack(number)) {
                result = rules[hi - 2];
            }
            return result;
        }
        return fractionRules[2];
    }

    /**
     * If this rule is a fraction rule set, this function is used by
     * findRule() to select the most appropriate rule for formatting
     * the number.  Basically, the base value of each rule in the rule
     * set is treated as the denominator of a fraction.  Whichever
     * denominator can produce the fraction closest in value to the
     * number passed in is the result.  If there's a tie, the earlier
     * one in the list wins.  (If there are two rules in a row with the
     * same base value, the first one is used when the numerator of the
     * fraction would be 1, and the second rule is used the rest of the
     * time.
     * @param number The number being formatted (which will always be
     * a number between 0 and 1)
     * @return The rule to use to format this number
     */
    private NFRule findFractionRuleSetRule(double number) {
        long leastCommonMultiple = rules[0].getBaseValue();
        for (int i = 1; i < rules.length; i++) {
            leastCommonMultiple = lcm(leastCommonMultiple, rules[i].getBaseValue());
        }
        long numerator = (long) (Math.round(number * leastCommonMultiple));
        long tempDifference;
        long difference = Long.MAX_VALUE;
        int winner = 0;
        for (int i = 0; i < rules.length; i++) {
            tempDifference = numerator * rules[i].getBaseValue() % leastCommonMultiple;
            if (leastCommonMultiple - tempDifference < tempDifference) {
                tempDifference = leastCommonMultiple - tempDifference;
            }
            if (tempDifference < difference) {
                difference = tempDifference;
                winner = i;
                if (difference == 0) {
                    break;
                }
            }
        }
        if (winner + 1 < rules.length && rules[winner + 1].getBaseValue() == rules[winner].getBaseValue()) {
            if (Math.round(number * rules[winner].getBaseValue()) < 1 || Math.round(number * rules[winner].getBaseValue()) >= 2) {
                ++winner;
            }
        }
        return rules[winner];
    }

    /**
     * Calculates the least common multiple of x and y.
     */
    private static long lcm(long x, long y) {
        long x1 = x;
        long y1 = y;
        int p2 = 0;
        while ((x1 & 1) == 0 && (y1 & 1) == 0) {
            ++p2;
            x1 >>= 1;
            y1 >>= 1;
        }
        long t;
        if ((x1 & 1) == 1) {
            t = -y1;
        } else {
            t = x1;
        }
        while (t != 0) {
            while ((t & 1) == 0) {
                t >>= 1;
            }
            if (t > 0) {
                x1 = t;
            } else {
                y1 = -t;
            }
            t = x1 - y1;
        }
        long gcd = x1 << p2;
        return x / gcd * y;
    }

    /**
     * Parses a string.  Matches the string to be parsed against each
     * of its rules (with a base value less than upperBound) and returns
     * the value produced by the rule that matched the most charcters
     * in the source string.
     * @param text The string to parse
     * @param parsePosition The initial position is ignored and assumed
     * to be 0.  On exit, this object has been updated to point to the
     * first character position this rule set didn't consume.
     * @param upperBound Limits the rules that can be allowed to match.
     * Only rules whose base values are strictly less than upperBound
     * are considered.
     * @return The numerical result of parsing this string.  This will
     * be the matching rule's base value, composed appropriately with
     * the results of matching any of its substitutions.  The object
     * will be an instance of Long if it's an integral value; otherwise,
     * it will be an instance of Double.  This function always returns
     * a valid object: If nothing matched the input string at all,
     * this function returns new Long(0), and the parse position is
     * left unchanged.
     */
    public Number parse(String text, ParsePosition parsePosition, double upperBound) {
        ParsePosition highWaterMark = new ParsePosition(0);
        Number result = new Long(0);
        Number tempResult = null;
        if (text.length() == 0) {
            return result;
        }
        if (negativeNumberRule != null) {
            tempResult = negativeNumberRule.doParse(text, parsePosition, false, upperBound);
            if (parsePosition.getIndex() > highWaterMark.getIndex()) {
                result = tempResult;
                highWaterMark.setIndex(parsePosition.getIndex());
            }
            parsePosition.setIndex(0);
        }
        for (int i = 0; i < 3; i++) {
            if (fractionRules[i] != null) {
                tempResult = fractionRules[i].doParse(text, parsePosition, false, upperBound);
                if (parsePosition.getIndex() > highWaterMark.getIndex()) {
                    result = tempResult;
                    highWaterMark.setIndex(parsePosition.getIndex());
                }
                parsePosition.setIndex(0);
            }
        }
        for (int i = rules.length - 1; i >= 0 && highWaterMark.getIndex() < text.length(); i--) {
            if (!isFractionRuleSet && rules[i].getBaseValue() >= upperBound) {
                continue;
            }
            tempResult = rules[i].doParse(text, parsePosition, isFractionRuleSet, upperBound);
            if (parsePosition.getIndex() > highWaterMark.getIndex()) {
                result = tempResult;
                highWaterMark.setIndex(parsePosition.getIndex());
            }
            parsePosition.setIndex(0);
        }
        parsePosition.setIndex(highWaterMark.getIndex());
        return result;
    }
}

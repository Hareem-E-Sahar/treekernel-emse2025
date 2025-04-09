package net.sf.yavtags.rules;

import net.sf.yavtags.*;
import net.sf.yavtags.util.Misc;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DateLe extends YavRule {

    private String DATE_FORMAT;

    public DateLe(String rule, List<YavRule> yavRules, YavConfig yavConfig) throws ParseException {
        super(rule, yavRules, yavConfig);
        super.i18nKeys[0] = "DATE_LE_MSG";
        this.DATE_FORMAT = yavConfig.getDATE_FORMAT();
    }

    @Override
    public YavError checkError(String value) {
        if (!Misc.isEmptyOrNull(value)) {
            YavError comparingError = new YavError(this.getFieldName(), "Comparing date must be a date in " + DATE_FORMAT + " format.");
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            String dateStr = getProcessedValue();
            YavRule dateRule;
            try {
                dateRule = YavRule.parseRule("xxx" + yavConfig.getRULE_SEP() + "date", null, null, yavConfig);
            } catch (ParseException ex) {
                return comparingError;
            }
            if (dateRule.checkError(dateStr) != null) {
                return comparingError;
            }
            java.util.Date comparingDate;
            try {
                comparingDate = sdf.parse(dateStr);
            } catch (ParseException ex) {
                return comparingError;
            }
            YavError dateError = new YavError(this.getFieldName(), this.getFieldLabel() + " must be a date in " + DATE_FORMAT + " format.");
            if (dateRule.checkError(value) != null) {
                return dateError;
            }
            java.util.Date valueDate;
            try {
                valueDate = sdf.parse(value);
            } catch (ParseException ex) {
                return dateError;
            }
            Calendar c1 = Calendar.getInstance();
            c1.setTime(valueDate);
            Calendar c2 = Calendar.getInstance();
            c2.setTime(comparingDate);
            c1.set(Calendar.HOUR, 0);
            c1.set(Calendar.MINUTE, 0);
            c1.set(Calendar.SECOND, 0);
            c1.set(Calendar.MILLISECOND, 0);
            c2.set(Calendar.HOUR, 0);
            c2.set(Calendar.MINUTE, 0);
            c2.set(Calendar.SECOND, 0);
            c2.set(Calendar.MILLISECOND, 0);
            if (c1.compareTo(c2) > 0) {
                return new YavError(this.getFieldName(), this.getFieldLabel() + " must be before or equal " + sdf.format(comparingDate) + ".");
            }
        }
        return null;
    }

    /** If this rule has a second part after rule type. */
    @Override
    protected boolean hasRuleSuffix() {
        return true;
    }

    /** If this rule has a final help message. */
    @Override
    protected boolean hasRuleMessage() {
        return true;
    }
}

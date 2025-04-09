package com.googlecode.cannedbeans.generator.core.strategies.impl;

import com.googlecode.cannedbeans.generator.core.exceptions.MissingPropertyException;
import com.googlecode.cannedbeans.generator.core.exceptions.UnsupportedConstraintException;
import com.googlecode.cannedbeans.generator.core.strategies.GenerationStrategy;
import com.googlecode.cannedbeans.generator.model.Constraint;
import com.googlecode.cannedbeans.generator.model.ConstraintType;
import org.apache.commons.lang.RandomStringUtils;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Kim
 */
public class StringGenerationStrategy implements GenerationStrategy<String> {

    private static Set<ConstraintType> supportedConstraintTypes;

    private Random random;

    static {
        supportedConstraintTypes = EnumSet.of(ConstraintType.NOTNULL, ConstraintType.LENGTH);
    }

    public StringGenerationStrategy() {
        random = new Random();
    }

    public String generateValidValue(Map<ConstraintType, Constraint> constraints) throws UnsupportedConstraintException, MissingPropertyException {
        for (ConstraintType constraintType : constraints.keySet()) {
            if (!supportedConstraintTypes.contains(constraintType)) {
                throw new UnsupportedConstraintException(this, constraintType);
            }
        }
        String value = null;
        if (constraints.containsKey(ConstraintType.LENGTH)) {
            Integer max = (Integer) constraints.get(ConstraintType.LENGTH).getProperty("max");
            Integer min = (Integer) constraints.get(ConstraintType.LENGTH).getProperty("min");
            if (max == null && min == null) {
                throw new MissingPropertyException(constraints.get(ConstraintType.LENGTH), "min", "Expected at least one of properties [min, max] for Constraint of type LENGTH.");
            }
            min = (min == null ? 0 : min);
            max = (max == null ? Integer.MAX_VALUE : max);
            int length = min + random.nextInt(max - min);
            value = RandomStringUtils.random(length);
            return value;
        } else if (constraints.containsKey(ConstraintType.NOTNULL)) {
            return "notnull";
        }
        return value;
    }

    public String generateInvalidValue(Map<ConstraintType, Constraint> constraints, Constraint constraintToViolate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String generateLimitingValue(Map<ConstraintType, Constraint> constraints, Constraint limitingConstraint) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<ConstraintType> getSupportedConstraintTypes() {
        return supportedConstraintTypes;
    }
}

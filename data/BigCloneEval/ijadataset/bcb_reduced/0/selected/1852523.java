package com.rapidminer.operator.text;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.FirstInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * This operator segments a text based on a starting and ending regular
 * expression.
 * 
 * @author Sebastian Land
 */
public class TextSegmenter extends OperatorChain {

    private static final String PARAMETER_START_REGEX = "start_regex";

    private static final String PARAMETER_END_REGEX = "end_regex";

    public TextSegmenter(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        TextObject text = getInput(TextObject.class);
        IOContainer input = getInput();
        Pattern startPattern = Pattern.compile(getParameterAsString(PARAMETER_START_REGEX));
        Pattern endPattern = Pattern.compile(getParameterAsString(PARAMETER_END_REGEX));
        Matcher startMatcher = startPattern.matcher(text.getText());
        Matcher endMatcher = endPattern.matcher(text.getText());
        int start = 0;
        while (startMatcher.find(start)) {
            if (endMatcher.find(startMatcher.end())) {
                TextObject segment = new TextObject(text.getText().substring(startMatcher.start(), endMatcher.end()));
                Iterator<Operator> childIterator = super.getOperators();
                IOContainer childInput = input.append(segment);
                while (childIterator.hasNext()) {
                    try {
                        childInput = childIterator.next().apply(childInput);
                    } catch (ConcurrentModificationException e) {
                        if (isDebugMode()) e.printStackTrace();
                        throw new UserError(this, 923);
                    }
                }
                start = endMatcher.end();
            } else {
                break;
            }
        }
        return input.getIOObjects();
    }

    public InnerOperatorCondition getInnerOperatorCondition() {
        return new FirstInnerOperatorCondition(new Class[] { TextObject.class });
    }

    public int getMaxNumberOfInnerOperators() {
        return Integer.MAX_VALUE;
    }

    public int getMinNumberOfInnerOperators() {
        return 1;
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> parameters = super.getParameterTypes();
        parameters.add(new ParameterTypeString(PARAMETER_START_REGEX, "This regular expression specifies the start of segments."));
        parameters.add(new ParameterTypeString(PARAMETER_END_REGEX, "This regular expression specifies the end of segments."));
        return parameters;
    }

    public Class<?>[] getInputClasses() {
        return new Class[] { TextObject.class };
    }

    public Class<?>[] getOutputClasses() {
        return new Class[0];
    }
}

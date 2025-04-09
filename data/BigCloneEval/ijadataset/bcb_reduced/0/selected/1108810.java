package uk.ac.ed.rapid.jobdata;

import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ed.rapid.exception.RapidException;
import uk.ac.ed.rapid.value.Value;

/**
 *
 * @author jos
 * Helper class to substitute variables of the form $(VARIABLE). Use through the VariableResolver class
 */
public class VariableAnalysis {

    private String input = null;

    private List<VariableLocation> variableLocations = null;

    private JobData jobData = null;

    private StaticTable staticTable = null;

    protected VariableAnalysis(JobData jobData, StaticTable staticTable) {
        this.jobData = jobData;
        this.staticTable = staticTable;
    }

    protected VariableAnalysis(JobData jobData) {
        this.jobData = jobData;
    }

    protected void setVariableData(JobData jobData, StaticTable staticTable) {
        this.jobData = jobData;
        this.staticTable = staticTable;
    }

    protected void setVariableData(JobData jobData) {
        this.jobData = jobData;
    }

    protected synchronized void setInput(String input) {
        this.input = input;
        this.analyse();
    }

    protected int getNumberOfVariables() {
        return this.variableLocations.size();
    }

    protected List<Value> getVariables() throws RapidException {
        List<Value> result = new Vector<Value>();
        for (VariableLocation location : this.variableLocations) result.add(this.jobData.getVariable(location.getName()));
        return result;
    }

    protected Collection<String> getVariableNames() {
        List<String> result = new Vector<String>();
        for (VariableLocation location : this.variableLocations) result.add(location.getName());
        return result;
    }

    /**
	 * Replace all occurrences of $(VARIABLE) by a variable
	 * @param subJob index in the variable
	 * @return 
	 * @throws RapidException
	 */
    protected String getOutput(int subJob) throws RapidException {
        StringBuffer result = new StringBuffer();
        Value resolvedVariable = null;
        int start = 0;
        for (VariableLocation location : this.variableLocations) {
            String variableName = location.getName();
            result.append(this.input.substring(start, location.start));
            if (this.staticTable != null && this.staticTable.exists(variableName)) resolvedVariable = staticTable.getStatic(variableName); else resolvedVariable = jobData.getVariable(variableName);
            result.append(resolvedVariable.get(subJob));
            start = location.getEnd();
        }
        result.append(this.input.substring(start, this.input.length()));
        return result.toString().replace("$$", "$");
    }

    protected String getOutput() throws RapidException {
        return this.getOutput(0);
    }

    /**
	 * find all patterns of $(VARIABLE), register the location (start-end in the string) and the name 
	 */
    protected void analyse() {
        Pattern varPattern = Pattern.compile("(\\$)+\\([^ \\)]*\\)");
        Matcher varMatch = varPattern.matcher(this.input);
        this.variableLocations = new Vector<VariableLocation>();
        while (varMatch.find()) {
            int start = varMatch.start();
            String var = varMatch.group();
            while (var.startsWith("$$")) {
                var = var.substring(2);
                start = start + 2;
            }
            if (var.startsWith("$(")) this.variableLocations.add(new VariableLocation(var.substring(2, var.length() - 1), start, varMatch.end()));
        }
    }

    private class VariableLocation {

        String name;

        int start;

        int end;

        public VariableLocation(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        public String getName() {
            return this.name;
        }

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }
    }
}

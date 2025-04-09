public class Test {    public String getChannelName(String timeVarName) {
        return timeVars.containsKey(timeVarName) ? timeVars.get(timeVarName) : "";
    }
}
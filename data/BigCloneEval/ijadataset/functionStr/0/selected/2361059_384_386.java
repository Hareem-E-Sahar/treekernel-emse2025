public class Test {    public String getVariable(String varName) throws Exception {
        return TegsoftPBX.getVariable(getChannel(), varName);
    }
}
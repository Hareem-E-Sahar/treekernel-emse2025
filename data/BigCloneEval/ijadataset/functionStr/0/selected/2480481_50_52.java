public class Test {    public void process(String inScriptName) throws GeneratorException {
        writeScript(inScriptName, readScript(inScriptName));
    }
}
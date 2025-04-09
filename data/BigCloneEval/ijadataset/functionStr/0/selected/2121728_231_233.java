public class Test {    public boolean isAlsoReadwriteField(final String name) {
        return this.readwriteParameterNames.contains(name);
    }
}
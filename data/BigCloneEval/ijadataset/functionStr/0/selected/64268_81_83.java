public class Test {    protected Object createTest() throws Exception {
        return getTestClass().getConstructor().newInstance();
    }
}
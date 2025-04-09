public class Test {    @Override
    public String dumpPipeline() {
        return getClass().getName() + ": " + reader + "->" + writer;
    }
}
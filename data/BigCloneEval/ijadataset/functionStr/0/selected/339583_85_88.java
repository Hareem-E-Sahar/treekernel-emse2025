public class Test {    @Override
    public Method getJavaMember() {
        return reader == null ? writer.getJavaMember() : reader.getJavaMember();
    }
}
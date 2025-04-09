public class Test {    private Value[] createRecord(PrintWriter writer, DataPage s) {
        return createRecord(writer, s, s.readInt());
    }
}
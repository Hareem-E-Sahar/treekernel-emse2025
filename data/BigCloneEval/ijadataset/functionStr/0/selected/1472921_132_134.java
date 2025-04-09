public class Test {    private void writeObjectClassID(ByteArrayBuffer reader, int id) {
        reader.writeInt(-id);
    }
}
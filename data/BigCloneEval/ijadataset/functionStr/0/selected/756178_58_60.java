public class Test {    protected void writeResponse(Serializable responceObject) throws IOException {
        ConnectionManager.writeToSocket(responceObject, getChannel());
    }
}
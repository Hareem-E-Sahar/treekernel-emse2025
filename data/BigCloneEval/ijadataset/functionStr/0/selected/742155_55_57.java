public class Test {    public int availableForPut() {
        return dataSize - (writePosition - readPosition);
    }
}
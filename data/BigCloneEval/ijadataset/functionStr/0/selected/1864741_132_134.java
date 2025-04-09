public class Test {    public AbstractFormat getFormat(boolean readable) {
        return readable ? writeFormat : displayFormat;
    }
}
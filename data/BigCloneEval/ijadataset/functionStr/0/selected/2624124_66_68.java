public class Test {    public static void writeDefaultStylesheet(OutputStream cssOutputStream) {
        writeStylesheet(readBuiltinInlineCSSProperties(), cssOutputStream);
    }
}
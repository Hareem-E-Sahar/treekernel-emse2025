public class Test {    public static String readAll() {
        if (!scanner.hasNextLine()) return null;
        return scanner.useDelimiter("\\A").next();
    }
}
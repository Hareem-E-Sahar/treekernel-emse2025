public class Test {    public String getTitle() {
        return JSONStringValueOrNull((JSONString) getChannel().get(Keys.TITLE));
    }
}
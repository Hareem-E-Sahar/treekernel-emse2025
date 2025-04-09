public class Test {    private JSONArray getItems() {
        return (JSONArray) getChannel().get("item");
    }
}
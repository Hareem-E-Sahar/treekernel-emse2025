public class Test {    public List<String> getChannelIds() {
        return (endpoints != null && endpoints.size() != 0) ? new ArrayList<String>(endpoints.keySet()) : null;
    }
}
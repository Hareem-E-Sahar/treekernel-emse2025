public class Test {    public Collection getChannelUsers() {
        return Collections.unmodifiableCollection(users.values());
    }
}
public class Test {    public Channel getChannelById(int id) {
        return root.getChannelByIdRecursive(id);
    }
}
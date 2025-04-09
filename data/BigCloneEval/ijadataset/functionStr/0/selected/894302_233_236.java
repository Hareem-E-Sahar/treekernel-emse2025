public class Test {    public Vector getMembers() {
        this.assertStarted();
        return getChannel().getView().getMembers();
    }
}
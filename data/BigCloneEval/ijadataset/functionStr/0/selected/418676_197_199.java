public class Test {    public String getBoardInstanceIdentifierForMenu() {
        return this.commChannel.getChannelName() + "/" + this.getBoardIdentifier() + "(" + this.getAddress() + ")";
    }
}
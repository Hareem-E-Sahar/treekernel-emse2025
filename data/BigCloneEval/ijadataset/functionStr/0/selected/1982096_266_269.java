public class Test {    public Vector getChannels() {
        Vector registeredChannels = (Vector) this.attrMgr.getAttribute("channels");
        return registeredChannels;
    }
}
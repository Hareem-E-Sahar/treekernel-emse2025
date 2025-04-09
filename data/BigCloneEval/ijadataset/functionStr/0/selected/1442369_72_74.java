public class Test {    public IChannelKey getChannelSpecificationKey() {
        return new LocalChannelKey(driver + url + user + password);
    }
}
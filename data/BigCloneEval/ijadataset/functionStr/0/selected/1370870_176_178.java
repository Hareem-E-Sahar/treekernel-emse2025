public class Test {    public String getChannelName() throws Exception {
        return SageApi.StringApi("GetChannelName", new Object[] { sageAiring });
    }
}
public class Test {    protected IChannelService getChannelService() {
        return XtotoApplication.get().getCometdService();
    }
}
public class Test {    public IGenericChannelTemplate createTemplate() throws XAwareException {
        return this.m_channelSpecification.getChannelTemplate();
    }
}
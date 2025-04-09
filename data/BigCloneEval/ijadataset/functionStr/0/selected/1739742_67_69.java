public class Test {    public Queue(QueueAdmissionsController anAdmissionsController) {
        this(anAdmissionsController, ChannelFactory.instance().getChannel());
    }
}
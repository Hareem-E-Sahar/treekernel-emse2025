public class Test {    public Queue<Message> getChannel(String queueName) {
        return queues.get(queueName);
    }
}
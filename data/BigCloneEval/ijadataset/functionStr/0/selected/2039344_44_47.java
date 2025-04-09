public class Test {            @Override
            public void sendMessage(Message message, Address[] recipients) throws MessagingException {
                Transport.send(message, recipients);
            }
}
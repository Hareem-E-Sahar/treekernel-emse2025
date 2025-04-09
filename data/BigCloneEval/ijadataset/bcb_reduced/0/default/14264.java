import java.lang.reflect.Constructor;
import edu.ucla.cs.typecast.event.EventManager;
import edu.ucla.cs.typecast.event.EventSource;

public class NewsPublisher {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            Class newsClass = Class.forName(args[0]);
            String content = args[1];
            Constructor constructor = newsClass.getConstructor(new Class[] { String.class });
            Object news = constructor.newInstance(new Object[] { content });
            EventSource eventSource = EventManager.getInstance().getEventSource(newsClass);
            eventSource.publishEvent(news);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

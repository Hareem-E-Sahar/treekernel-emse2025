package xesloganalyzer;

import com.thoughtworks.xstream.XStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;
import org.deckfour.xes.util.XTimer;
import org.deckfour.xes.xstream.XesXStreamPersistency;

/**
 *
 * @author XY
 */
public class XESLogAnalyzer {

    public static final Random random = new Random();

    public static XFactory factory = XFactoryRegistry.instance().currentDefault();

    public static final char CHARACTERS[] = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };

    public static long NUM_TRACES = 0;

    public static long NUM_EVENTS = 0;

    public static long NUM_ATTRIBUTES = 0;

    public static String generateString(int minLength, int maxLength) {
        StringBuilder sb = new StringBuilder();
        int length = minLength + random.nextInt(maxLength - minLength);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS[random.nextInt(CHARACTERS.length)]);
        }
        return sb.toString();
    }

    public static XAttribute generateAttribute() {
        String key = generateString(4, 15);
        XAttribute attribute;
        int typeIndex = random.nextInt(5);
        switch(typeIndex) {
            case 0:
                attribute = factory.createAttributeBoolean(key, random.nextBoolean(), null);
                break;
            case 1:
                attribute = factory.createAttributeContinuous(key, random.nextDouble(), null);
                break;
            case 2:
                attribute = factory.createAttributeDiscrete(key, random.nextLong(), null);
                break;
            case 3:
                attribute = factory.createAttributeLiteral(key, generateString(10, 30), null);
                break;
            default:
                attribute = factory.createAttributeTimestamp(key, random.nextLong(), null);
                break;
        }
        addAttributes(attribute, 0.05, 1, 5);
        NUM_ATTRIBUTES++;
        return attribute;
    }

    public static void addAttributes(XAttributable attributable, double chance, int minNr, int maxNr) {
        if (random.nextDouble() < chance) {
            XAttribute childAttr;
            for (int i = random.nextInt(maxNr - minNr) + minNr; i > 0; i--) {
                childAttr = generateAttribute();
                attributable.getAttributes().put(childAttr.getKey(), childAttr);
            }
        }
    }

    public static XEvent createEvent() {
        XEvent event = factory.createEvent();
        XAttribute childAttr;
        childAttr = factory.createAttributeLiteral("name", "name1", null);
        event.getAttributes().put(childAttr.getKey(), childAttr);
        childAttr = factory.createAttributeTimestamp("time", NUM_TRACES, null);
        event.getAttributes().put(childAttr.getKey(), childAttr);
        NUM_EVENTS++;
        return event;
    }

    public static XTrace createTrace(int minLength, int maxLength) {
        XTrace trace = factory.createTrace();
        addAttributes(trace, 0.9, 3, 50);
        int length = minLength + random.nextInt(maxLength - minLength);
        for (int i = 0; i < length; i++) {
            trace.add(createEvent());
        }
        NUM_TRACES++;
        return trace;
    }

    public static XLog createLog(int minTraces, int maxTraces, int minTraceLength, int maxTraceLength) {
        XLog log = factory.createLog();
        addAttributes(log, 0.9, 3, 50);
        int length = minTraces + random.nextInt(maxTraces - minTraces);
        for (int i = 0; i < length; i++) {
            log.add(createTrace(minTraceLength, maxTraceLength));
        }
        return log;
    }

    public static void walkLog(XLog log) {
        walkAttributes(log);
        for (XTrace trace : log) {
            walkTrace(trace);
        }
    }

    public static void walkTrace(XTrace trace) {
        walkAttributes(trace);
        for (XEvent event : trace) {
            walkAttributes(event);
        }
    }

    public static void walkAttributes(XAttributable attributable) {
        XAttributeMap attributeMap = attributable.getAttributes();
        for (XAttribute attribute : attributeMap.values()) {
            String key = attribute.getKey();
            String value = attribute.toString();
            key.trim();
            value.trim();
            walkAttributes(attribute);
        }
    }

    public static void runTest() {
        Thread testRunner = new Thread() {

            @Override
            public void run() {
                XTimer timer = new XTimer();
                XLog log = createLog(2, 3, 10, 20);
                timer.stop();
                System.out.println("Created log:");
                System.out.println("  Traces: " + NUM_TRACES);
                System.out.println("  Events: " + NUM_EVENTS);
                System.out.println("  Attributes: " + NUM_ATTRIBUTES);
                System.out.println("Duration: " + timer.getDurationString());
                timer.start();
                walkLog(log);
                timer.stop();
                System.out.println("Walked log.");
                System.out.println("Duration: " + timer.getDurationString());
                File sFile = new File("XESLOG.xml");
                if (sFile.exists()) {
                    sFile.delete();
                }
                try {
                    sFile.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                XStream xstream = new XStream();
                XesXStreamPersistency.register(xstream);
                try {
                    System.out.println("Serializing log with XStream");
                    timer.start();
                    OutputStream oStream = new BufferedOutputStream(new FileOutputStream(sFile));
                    xstream.toXML(log, oStream);
                    timer.stop();
                    System.out.println("Duration: " + timer.getDurationString());
                    System.out.println("Deserializing log with XStream");
                    timer.start();
                    XLog log2 = (XLog) xstream.fromXML(new BufferedInputStream(new FileInputStream(sFile)));
                    System.out.println("Duration: " + timer.getDurationString());
                    timer.start();
                    walkLog(log2);
                    timer.stop();
                    System.out.println("Walked deserialized log.");
                    System.out.println("Duration: " + timer.getDurationString());
                    XLogInfo info = null;
                    info = XLogInfoFactory.createLogInfo(log2);
                    System.out.println("pocet trace :" + info.getNumberOfTraces());
                    System.out.println("pocet events :" + info.getNumberOfEvents());
                    for (int i = 0; i < info.getNumberOfTraces(); i++) {
                        XTrace trace = log2.get(i);
                        System.out.println(trace.size());
                        for (int j = 0; j < trace.size(); j++) {
                            Object key = new Object();
                            key = "name";
                            System.out.println("  " + trace.get(j).getAttributes().get(key).toString());
                            key = "time";
                            System.out.println("  " + trace.get(j).getAttributes().get(key).toString());
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        testRunner.start();
    }
}

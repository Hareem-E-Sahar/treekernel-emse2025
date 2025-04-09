import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class XMLScheduleLoader extends org.xml.sax.helpers.DefaultHandler implements ScheduleLoader {

    private String _uri;

    private XMLReader _parser;

    private Schedule _schedule;

    private Megatron _overlord;

    private HashMap<String, Block> _blockptr;

    private LinkedHashMap<String, LinkedList<Pair<String, String>>> _unresLayout;

    private StringBuffer _accumulator;

    private Block _scheduleTentative;

    private String _startDate;

    private Block _block;

    public XMLScheduleLoader(String uri, Megatron overlord) throws Exception {
        _uri = uri;
        _overlord = overlord;
        _parser = XMLReaderFactory.createXMLReader();
        _parser.setFeature("http://xml.org/sax/features/validation", false);
        _parser.setContentHandler(this);
        _parser.setErrorHandler(this);
    }

    public void changeURI(String uri) {
        _schedule = null;
        _uri = uri;
    }

    public Schedule produceSchedule() throws Exception {
        _unresLayout = new LinkedHashMap<String, LinkedList<Pair<String, String>>>();
        _schedule = new Schedule(_overlord);
        _parser.parse(new InputSource(new BufferedReader(new FileReader(_uri))));
        _schedule.setStartDate(_startDate);
        return _schedule;
    }

    private class Block {

        public String _name;

        public int _multiplier;

        public LinkedList<Pair<String, Object>> _layout;

        public Block(String n, int mul, LinkedList<Pair<String, Object>> lay) {
            _name = n;
            _multiplier = mul;
            _layout = lay;
        }
    }

    private class Pair<K, V> {

        public K _key;

        public V _current;

        public Pair(K k, V v) {
            _key = k;
            _current = v;
        }
    }

    private TrackGenerator createModuleInvocation(String str) {
        String[] segments;
        segments = str.split("[(,)] *");
        try {
            Object[] objs = new Object[2];
            objs[0] = _overlord;
            objs[1] = segments;
            return (TrackGenerator) Class.forName(segments[0]).getConstructor(Class.forName("Megatron"), Class.forName("[Ljava.lang.String;")).newInstance(objs);
        } catch (ClassNotFoundException e) {
            (Megatron.getLog()).log("XMLScheduleLoader: module " + segments[0] + " cannot be found (check paths, including CLASSPATH) (" + e + ")\n");
        } catch (IllegalAccessException e) {
            (Megatron.getLog()).log("XMLScheduleLoader: Module " + segments[0] + " wtf? (" + e + ")\n");
        } catch (NoSuchMethodException e) {
            (Megatron.getLog()).log("XMLScheduleLoader: Module " + segments[0] + " appears to have a private constructor, try to getInstance...");
            try {
                Object[] objs = new Object[2];
                objs[0] = _overlord;
                objs[1] = segments;
                return (TrackGenerator) Class.forName(segments[0]).getMethod("getInstance", new Class[] { Megatron.class, String[].class }).invoke(null, objs);
            } catch (ClassNotFoundException f) {
                (Megatron.getLog()).log("XMLScheduleLoader: this should never happen");
            } catch (NoSuchMethodException f) {
                (Megatron.getLog()).log("XMLScheduleLoader: Could not access constructor or invoke getInstance on " + segments[0] + ".  Giving up.\n");
            } catch (SecurityException f) {
                (Megatron.getLog()).log("XMLScheduleLoader: SECURITY ALERT HAS FUCKED YOU!");
            } catch (IllegalAccessException f) {
                f.printStackTrace();
            } catch (IllegalArgumentException f) {
                (Megatron.getLog()).error("XMLScheduleLoader: you sent the params wrong to getInstance, koder");
            } catch (java.lang.reflect.InvocationTargetException f) {
                (Megatron.getLog()).log("XMLScheduleLoader: getInstance() of the " + segments[0] + " vommitted.  Abandoning all hope");
            } catch (Exception f) {
                f.printStackTrace();
                (Megatron.getLog()).log("XMLScheduleLoader: something went wrong" + f.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            (Megatron.getLog()).error("XMLScheduleLoader: A queer error has been encountered\n");
        }
        return null;
    }

    public void startDocument() {
        _accumulator = new StringBuffer();
        _blockptr = new HashMap<String, Block>();
    }

    public void characters(char[] buffer, int start, int length) {
        _accumulator.append(buffer, start, length);
    }

    public void startElement(String namespaceURL, String localname, String qname, Attributes attrs) {
        _accumulator.setLength(0);
        if (localname.equals("Schedule")) _startDate = attrs.getValue("start"); else if (localname.equals("Block")) {
            _block = new Block(null, -1, new LinkedList<Pair<String, Object>>());
            if (attrs.getValue("master") != null) _scheduleTentative = _block;
        } else if (localname.equals("Layout")) {
            _unresLayout.put(_block._name, new LinkedList<Pair<String, String>>());
            _block._multiplier = (new Integer(attrs.getValue("multiplier"))).intValue();
        } else if (localname.equals("Entry")) {
            String dur = attrs.getValue("duration");
            if (dur == null) dur = "0";
            String deadlined = attrs.getValue("exact");
            if (deadlined == null) deadlined = "0";
            (_unresLayout.get(_block._name)).push(new Pair<String, String>(dur + ":" + deadlined, null));
        } else if (localname.equals("Name")) ; else (Megatron.getLog()).log("XMLScheduleLoader: Unknown tag " + localname + "\n");
    }

    public void endElement(String namespaceURL, String localname, String qname) {
        if (localname == "Entry") {
            Pair<String, String> p = (_unresLayout.get(_block._name)).pop();
            p._current = _accumulator.toString();
            (_unresLayout.get(_block._name)).push(p);
        } else if (localname == "Layout") ; else if (localname == "Block") _blockptr.put(_block._name, _block); else if (localname == "Name") _block._name = _accumulator.toString(); else if (localname == "Schedule") {
            Iterator<Block> blocks;
            blocks = (_blockptr).values().iterator();
            while (blocks.hasNext()) {
                Block b = blocks.next();
                Iterator<Pair<String, String>> urLayouts = _unresLayout.get(b._name).iterator();
                while (urLayouts.hasNext()) {
                    Pair<String, String> urLayout = urLayouts.next();
                    b._layout.push(new Pair<String, Object>(urLayout._key, (urLayout._current.endsWith("/") ? RandomStack.getInstance(urLayout._current) : (urLayout._current.startsWith("*") ? createModuleInvocation(urLayout._current.substring(1)) : _blockptr.get(urLayout._current)))));
                }
            }
        } else (Megatron.getLog()).log("XMLScheduleLoader: Unknown end tag " + localname + "\n");
    }

    public void endDocument() {
        (Megatron.getLog()).debug("Start date: " + _startDate);
        flattenSchedule(_scheduleTentative);
    }

    private void flattenSchedule(Block start) {
        Block node = start;
        Iterator<Pair<String, Object>> layouts = node._layout.iterator();
        while (layouts.hasNext()) {
            Pair<String, Object> entry = layouts.next();
            String[] durkey = entry._key.split(":");
            int keyval = (new Integer(durkey[0])).intValue();
            int deadline = (new Integer(durkey[1])).intValue();
            if (keyval < 1) _schedule.link(new Stax((TrackGenerator) entry._current, keyval * node._multiplier, deadline)); else {
                for (int i = keyval * node._multiplier; i > 0; ) {
                    if (entry._current instanceof Block) {
                        flattenSchedule((Block) entry._current);
                        i -= node._multiplier;
                    } else {
                        _schedule.link(new Stax((TrackGenerator) entry._current, keyval * node._multiplier, 0));
                        i -= keyval * node._multiplier;
                    }
                }
            }
        }
    }

    public void warning(SAXParseException exception) {
        (Megatron.getLog()).error("WARNING: line " + exception.getLineNumber() + ": " + exception.getMessage());
    }

    public void error(SAXParseException exception) {
        (Megatron.getLog()).error("ERROR: line " + exception.getLineNumber() + ": " + exception.getMessage());
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        (Megatron.getLog()).error("FATAL: line " + exception.getLineNumber() + ": " + exception.getMessage());
        throw (exception);
    }
}

;

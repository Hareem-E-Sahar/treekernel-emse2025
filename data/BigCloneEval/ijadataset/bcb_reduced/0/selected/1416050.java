package src.utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Removes content between e.g. two XHTML tags and inserts it again later
 *
 * @author Simon Eugster
 */
public class Placeholder {

    /** Start tag/whatever */
    private String patternStart = "";

    /** End tag/whatever */
    private String patternEnd = "";

    /** Temporarily removed content */
    public ArrayList<String> content;

    /** Generates a new Placeholder which will temporarily remove the content between patternStart and patternEnd. */
    public Placeholder(String patternStart, String patternEnd) {
        this.patternStart = patternStart;
        this.patternEnd = patternEnd;
    }

    /**
	 * Removes the content between the specified place holders.
	 * Considers nesting!
	 * @param input
	 * @return The input with removed contents
	 */
    public StringBuffer removeContent(StringBuffer input) {
        content = new ArrayList<String>();
        StringBuffer out;
        Pattern po = Pattern.compile(patternStart);
        Pattern pc = Pattern.compile(patternEnd);
        Matcher mo = po.matcher(input);
        Matcher mc = pc.matcher(input);
        TreeSet<Position> map = new TreeSet<Placeholder.Position>(new Comparator<Position>() {

            public int compare(Position o1, Position o2) {
                return o1._start - o2._start;
            }

            ;
        });
        while (mo.find()) {
            map.add(new Position(mo.start(), mo.end(), Position.Type.opening));
        }
        while (mc.find()) {
            map.add(new Position(mc.start(), mc.end(), Position.Type.closing));
        }
        int depth = 0;
        Position prev = null;
        for (Position p : map) {
            depth += p._type.depthDelta;
            p._depth = depth;
            prev = p;
        }
        boolean stop;
        boolean updatePrev;
        do {
            stop = true;
            prev = null;
            for (Position p : map) {
                updatePrev = !p._ignore;
                if (prev != null && !p._ignore) {
                    if (prev._type == Position.Type.opening && p._type == Position.Type.closing) {
                        if (p._depth > 0) {
                            p._ignore = true;
                            prev._ignore = true;
                            stop = false;
                        }
                    }
                }
                if (updatePrev) {
                    prev = p;
                } else {
                }
            }
        } while (!stop);
        if (map.size() > 0) {
            int last = 0;
            out = new StringBuffer();
            prev = null;
            for (Position p : map) {
                if (prev != null && !p._ignore) {
                    assert !prev._ignore;
                    if (prev._type == Position.Type.opening && p._type == Position.Type.closing) {
                        content.add(input.substring(prev._end, p._start));
                        out.append(input.substring(last, prev._end));
                        last = p._start;
                        p = null;
                    }
                }
                if (p == null || !p._ignore) {
                    prev = p;
                }
            }
            out.append(input.substring(last));
        } else {
            out = input;
        }
        return out;
    }

    /**
	 * Re-inserts the removed content
	 * @param input Input
	 * @return The input with inserted contents
	 */
    public StringBuffer insertContent(StringBuffer input) {
        StringBuffer out = new StringBuffer();
        Pattern p = Pattern.compile("(?si)(" + patternStart + ")(" + patternEnd + ")");
        Matcher m = p.matcher(input);
        int last = 0;
        int counter = 0;
        while (m.find()) {
            out.append(input.substring(last, m.start()));
            out.append(m.group(1));
            try {
                out.append(content.get(counter));
            } catch (IndexOutOfBoundsException e) {
            }
            out.append(m.group(2));
            counter++;
            last = m.end();
        }
        out.append(input.substring(last));
        return out;
    }

    /**
	 * Sets the parts between which content will be removed.
	 * @param patternStart
	 * @param patternEnd
	 */
    public void setParts(String patternStart, String patternEnd) {
        if (patternStart != null) this.patternStart = patternStart;
        if (patternEnd != null) this.patternEnd = patternEnd;
    }

    private static class Position {

        enum Type {

            opening("opening", +1), closing("closing", -1);

            @SuppressWarnings("unused")
            public final String name;

            public final int depthDelta;

            Type(String name, int delta) {
                this.name = name;
                this.depthDelta = delta;
            }
        }

        public final int _start;

        public final int _end;

        public final Type _type;

        public int _depth = 0;

        /** If this is a nested element, then it can be ignored (will be removed with the parent element). */
        public boolean _ignore = false;

        public Position(int start, int end, Type type) {
            _start = start;
            _end = end;
            _type = type;
        }
    }
}

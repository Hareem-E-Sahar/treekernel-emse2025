package org.workflow4j.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default workflow context object.
 *
 * @author oscar
 */
public class DefaultContext extends HashMap implements IContext {

    /**
     * Serial version UID.
     * 
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public DefaultContext() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @param source DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String replaceVariables(String source) {
        Log log = LogFactory.getLog(DefaultContext.class);
        ArrayList list = new ArrayList();
        Iterator iter = keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            Object value = get(key);
            log.debug(key + ": " + value);
            if ((key != null) && (value != null) && value instanceof String) {
                ReplaceStruct replace = new ReplaceStruct("${" + key + "}", (String) value);
                list.add(replace);
            }
        }
        String result = replace(source, list);
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param source DOCUMENT ME!
     * @param replaceStructList DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private String replace(String source, List replaceStructList) {
        int listSize = replaceStructList.size();
        if (listSize == 0) {
            return source;
        }
        ReplaceStruct which = null;
        int smallestIndex = -1;
        for (int i = listSize - 1; i >= 0; i--) {
            ReplaceStruct replace = (ReplaceStruct) replaceStructList.get(i);
            int index = source.indexOf(replace.from);
            if (index == -1) {
                replaceStructList.remove(i);
            } else {
                if ((smallestIndex == -1) || (index < smallestIndex)) {
                    smallestIndex = index;
                    which = replace;
                }
                replace.index = index;
            }
        }
        if ((listSize = replaceStructList.size()) == 0) {
            return source;
        }
        StringBuffer strBuf = new StringBuffer(source.length());
        strBuf.append(source.substring(0, smallestIndex));
        strBuf.append(which.to);
        int start = smallestIndex + which.from.length();
        while (listSize != 0) {
            smallestIndex = -1;
            which = null;
            for (int i = listSize - 1; i >= 0; i--) {
                ReplaceStruct replace = (ReplaceStruct) replaceStructList.get(i);
                if (replace.index < start) {
                    int index = source.indexOf(replace.from, start);
                    if (index == -1) {
                        replaceStructList.remove(i);
                    } else {
                        if ((smallestIndex == -1) || (index < smallestIndex)) {
                            smallestIndex = index;
                            which = replace;
                        }
                        replace.index = index;
                    }
                } else {
                    int index = replace.index;
                    if ((smallestIndex == -1) || (index < smallestIndex)) {
                        smallestIndex = index;
                        which = replace;
                    }
                }
            }
            if ((listSize = replaceStructList.size()) == 0) {
                strBuf.append(source.substring(start));
                return strBuf.toString();
            }
            strBuf.append(source.substring(start, smallestIndex));
            strBuf.append(which.to);
            start = smallestIndex + which.from.length();
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     */
    private class ReplaceStruct {

        /**
         * Creates a new ReplaceStruct object.
         *
         * @param from DOCUMENT ME!
         * @param to DOCUMENT ME!
         */
        public ReplaceStruct(final String from, final String to) {
            this.from = from;
            this.to = to;
        }

        /**
         * @param from The from to set.
         */
        public void setFrom(final String from) {
            this.from = from;
        }

        /**
         * @return Returns the from.
         */
        public String getFrom() {
            return from;
        }

        /**
         * @param index The index to set.
         */
        public void setIndex(final int index) {
            this.index = index;
        }

        /**
         * @return Returns the index.
         */
        public int getIndex() {
            return index;
        }

        /**
         * @param to The to to set.
         */
        public void setTo(final String to) {
            this.to = to;
        }

        /**
         * @return Returns the to.
         */
        public String getTo() {
            return to;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String toString() {
            StringBuffer strBuf = new StringBuffer();
            strBuf.append("===== ReplaceStruct =====");
            strBuf.append("\n\t index = ").append(index);
            strBuf.append("\n\t from = ").append(from);
            strBuf.append("\n\t to = ").append(to);
            return strBuf.toString();
        }

        /**
         * DOCUMENT ME!
         */
        private String from;

        /**
         * DOCUMENT ME!
         */
        private String to;

        /**
         * DOCUMENT ME!
         */
        private int index = -1;
    }
}

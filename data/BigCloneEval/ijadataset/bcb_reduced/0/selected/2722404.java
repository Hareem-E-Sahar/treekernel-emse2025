package de.fu_berlin.inf.gmanda.qda.TagComma;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.fu_berlin.inf.gmanda.qda.AbstractCode;
import de.fu_berlin.inf.gmanda.qda.AbstractCodedString;
import de.fu_berlin.inf.gmanda.qda.Code;
import de.fu_berlin.inf.gmanda.qda.CodedString;
import de.fu_berlin.inf.gmanda.util.CStringUtils;
import de.fu_berlin.inf.gmanda.util.IterableUnroller;
import de.fu_berlin.inf.gmanda.util.CStringUtils.JoinConverter;
import de.fu_berlin.inf.gmanda.util.CStringUtils.StringConverter;

public class TagCommaFactory {

    public static class TagCommaString extends AbstractCodedString {

        public static String parseTag(String code) {
            int index = code.indexOf('=');
            if (index != -1 && index + 1 < code.length()) {
                return code.substring(0, index);
            } else {
                return code;
            }
        }

        public static String parseValue(String code) {
            int index = code.indexOf('=');
            if (index != -1 && index + 1 < code.length()) {
                return code.substring(index + 1);
            } else {
                return null;
            }
        }

        public class TagCodedCode extends AbstractCode {

            public TagCodedCode(String code) {
                super(parseTag(code));
                value = parseValue(code);
            }

            String value;

            public String getValue() {
                return value;
            }

            public String toString(boolean withValue, boolean whiteSpace) {
                StringBuilder sb = new StringBuilder();
                if (whiteSpace) {
                    sb.append(tag);
                } else {
                    sb.append(tag.trim());
                }
                if (withValue && hasValue()) {
                    sb.append('=');
                    if (whiteSpace) sb.append(value); else sb.append(value.trim());
                }
                return sb.toString();
            }

            public String format(int indent, int width) {
                int hasNewLinesAtBeginning = 0;
                StringTokenizer st = new StringTokenizer(tag, " \t\n\r\f", true);
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (!" \t\n\r\f".contains(token)) {
                        break;
                    }
                    if ("\r\n\f".contains(token)) {
                        hasNewLinesAtBeginning++;
                    }
                }
                String oldValue = toString(true, false);
                StringBuilder sb = new StringBuilder();
                sb.append(CStringUtils.join(tagLevels, "."));
                if (hasValue()) {
                    sb.append('=');
                    String value = this.value.trim().replaceAll("\\n *", "\n");
                    List<String> lines = new LinkedList<String>();
                    for (String s : value.split("\\n\\n")) {
                        lines.add(s.replaceAll("\\s+", " "));
                    }
                    value = CStringUtils.join(lines, "\n");
                    if (value.length() > 80 - sb.length() || value.contains("\n")) {
                        sb.append("\n");
                        lines.clear();
                        for (String s : value.split("\n")) {
                            StringBuffer total = new StringBuffer();
                            StringBuffer sb2 = new StringBuffer();
                            sb2.append(CStringUtils.spaces(indent));
                            for (String s2 : s.split("\\s+")) {
                                if (sb2.length() + s2.length() < 80) sb2.append(" ").append(s2); else {
                                    total.append(sb2.toString()).append('\n');
                                    sb2 = new StringBuffer(CStringUtils.spaces(indent));
                                    sb2.append(s2);
                                }
                            }
                            total.append(sb2);
                            lines.add(total.toString());
                        }
                        sb.append(CStringUtils.join(lines, "\n\n"));
                    } else {
                        sb.append(value);
                    }
                }
                if (oldValue != null) assert sb.toString().replaceAll("\\s", "").equals(oldValue.replaceAll("\\s", "")) : sb.toString() + " \n\n" + oldValue;
                if (hasNewLinesAtBeginning >= 2) {
                    sb.insert(0, '\n');
                }
                return sb.toString();
            }

            public List<Code> getProperties() {
                if (value == null) {
                    return Collections.emptyList();
                }
                String value = org.apache.commons.lang.StringUtils.strip(this.value, "\" \n\r\f\t");
                Pattern p = Pattern.compile("(memo|date|desc|summary|milestone|def|value|vdef|quote|ref|title|cause)\\s*:\\s*");
                Matcher m = p.matcher(value);
                List<Code> result = new LinkedList<Code>();
                String lastKey = "desc";
                StringBuilder currentValue = new StringBuilder();
                int pos = 0;
                while (m.find()) {
                    int start = m.start();
                    String key = m.group(1);
                    if (pos < start) {
                        currentValue.append(value.substring(pos, start));
                    }
                    pos = m.end();
                    if (currentValue.length() > 0) {
                        result.add(new TagCodedCode(lastKey + "=\"" + org.apache.commons.lang.StringUtils.strip(currentValue.toString(), ", \n\r\f\t") + "\""));
                        currentValue = new StringBuilder();
                    }
                    lastKey = key;
                }
                if (pos < value.length()) {
                    currentValue.append(value.substring(pos));
                }
                if (currentValue.length() > 0) {
                    result.add(new TagCodedCode(lastKey + "=\"" + org.apache.commons.lang.StringUtils.strip(currentValue.toString(), ", \n\r\f\t") + "\""));
                }
                return result;
            }

            public Code renameTag(String fromRename, String toRename) {
                throw new UnsupportedOperationException("TagCommaSyntax is no longer supported except for reading");
            }
        }

        List<List<Code>> codes = new LinkedList<List<Code>>();

        public TagCommaString(String stringOfcodes) {
            for (String segments : CStringUtils.split(stringOfcodes, ';', '\"')) {
                List<Code> segmentList = new LinkedList<Code>();
                for (String code : CStringUtils.split(segments, ',', '\"')) {
                    if (code.trim().length() > 0) segmentList.add(new TagCodedCode(code));
                }
                if (segmentList.size() > 0) codes.add(segmentList);
            }
        }

        public String format() {
            return CStringUtils.join(codes, ";\n\n", new JoinConverter<Code>(",\n", new StringConverter<Code>() {

                public String toString(Code t) {
                    return t.format(2, 80);
                }
            }));
        }

        public Code parse(String s) {
            return new TagCodedCode(s);
        }

        public Iterable<Code> getAllCodes() {
            return new IterableUnroller<Code>(codes);
        }

        public String toString() {
            return CStringUtils.join(codes, ";", new JoinConverter<Code>(",", new StringConverter<Code>() {

                public String toString(Code t) {
                    return t.toString(true, true);
                }
            }));
        }

        /**
		 * Add the given code to the last segment of this coded string
		 * 
		 * @param s
		 */
        public void add(String s) {
            s = s.trim();
            if (s.length() == 0) return;
            if (codes.size() == 0) {
                codes.add(new LinkedList<Code>());
            }
            codes.get(codes.size() - 1).add(new TagCodedCode(s));
        }

        public void remove(String s) {
            Iterator<List<Code>> lit = codes.iterator();
            while (lit.hasNext()) {
                List<Code> list = lit.next();
                Iterator<Code> it = list.iterator();
                while (it.hasNext()) {
                    Code code = it.next();
                    if (code.getTag().equals(s)) it.remove();
                }
                if (list.size() == 0) lit.remove();
            }
        }

        public CodedString rename(String from, String to) {
            throw new UnsupportedOperationException("TagComma is deprecated and this operation is no longer supported");
        }
    }
}

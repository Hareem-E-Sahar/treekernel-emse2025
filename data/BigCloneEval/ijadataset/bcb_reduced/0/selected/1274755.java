package org.digitall.apps.jpgadmin.sqleditor;

import java.awt.Color;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class SQLDocument extends DefaultStyledDocument {

    private Element rootElement;

    private HashMap<String, Color> keywords;

    private MutableAttributeSet style;

    private Color commentColor = Color.gray;

    private Pattern singleLineCommentDelimter = Pattern.compile("--");

    private Pattern multiLineCommentDelimiterStart = Pattern.compile("/\\*");

    private Pattern multiLineCommentDelimiterEnd = Pattern.compile("\\*/");

    private Color stringColor = Color.red;

    private char stringDelimiter = '\"';

    private Color quoteColor = Color.red.darker();

    private char quoteDelimiter = '\'';

    private Color reservedColor = new Color(0, 0, 240);

    private Color keywordColor = new Color(20, 25, 200);

    private Color sqlFunctionColor = new Color(149, 196, 248);

    private Color dataTypesColor = new Color(51, 102, 153);

    private Color sqlColor = new Color(229, 124, 10);

    public SQLDocument() {
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        rootElement = getDefaultRootElement();
        keywords = new HashMap<String, Color>();
        keywords.put("boolean", dataTypesColor);
        keywords.put("bytea", dataTypesColor);
        keywords.put("char", dataTypesColor);
        keywords.put("name", dataTypesColor);
        keywords.put("bigint", dataTypesColor);
        keywords.put("smallint", dataTypesColor);
        keywords.put("int2vector", dataTypesColor);
        keywords.put("integer", dataTypesColor);
        keywords.put("regproc", dataTypesColor);
        keywords.put("text", dataTypesColor);
        keywords.put("oid", dataTypesColor);
        keywords.put("tid", dataTypesColor);
        keywords.put("xid", dataTypesColor);
        keywords.put("cid", dataTypesColor);
        keywords.put("oidvector", dataTypesColor);
        keywords.put("pg_type", dataTypesColor);
        keywords.put("pg_attribute", dataTypesColor);
        keywords.put("pg_proc", dataTypesColor);
        keywords.put("pg_class", dataTypesColor);
        keywords.put("smgr", dataTypesColor);
        keywords.put("point", dataTypesColor);
        keywords.put("lseg", dataTypesColor);
        keywords.put("path", dataTypesColor);
        keywords.put("box", dataTypesColor);
        keywords.put("polygon", dataTypesColor);
        keywords.put("line", dataTypesColor);
        keywords.put("cidr", dataTypesColor);
        keywords.put("real", dataTypesColor);
        keywords.put("double precision", dataTypesColor);
        keywords.put("abstime", dataTypesColor);
        keywords.put("reltime", dataTypesColor);
        keywords.put("tinterval", dataTypesColor);
        keywords.put("unknown", dataTypesColor);
        keywords.put("circle", dataTypesColor);
        keywords.put("money", dataTypesColor);
        keywords.put("macaddr", dataTypesColor);
        keywords.put("inet", dataTypesColor);
        keywords.put("aclitem", dataTypesColor);
        keywords.put("character", dataTypesColor);
        keywords.put("character varying", dataTypesColor);
        keywords.put("date", dataTypesColor);
        keywords.put("time without time zone", dataTypesColor);
        keywords.put("timestamp without time zone", dataTypesColor);
        keywords.put("timestamp with time zone", dataTypesColor);
        keywords.put("interval", dataTypesColor);
        keywords.put("time with time zone", dataTypesColor);
        keywords.put("bit", dataTypesColor);
        keywords.put("bit varying", dataTypesColor);
        keywords.put("numeric", dataTypesColor);
        keywords.put("refcursor", dataTypesColor);
        keywords.put("regprocedure", dataTypesColor);
        keywords.put("regoper", dataTypesColor);
        keywords.put("regoperator", dataTypesColor);
        keywords.put("regclass", dataTypesColor);
        keywords.put("regtype", dataTypesColor);
        keywords.put("record", dataTypesColor);
        keywords.put("cstring", dataTypesColor);
        keywords.put("any", dataTypesColor);
        keywords.put("anyarray", dataTypesColor);
        keywords.put("void", dataTypesColor);
        keywords.put("trigger", dataTypesColor);
        keywords.put("language_handler", dataTypesColor);
        keywords.put("internal", dataTypesColor);
        keywords.put("opaque", dataTypesColor);
        keywords.put("anyelement", dataTypesColor);
        keywords.put("AND", sqlColor);
        keywords.put("BODY", sqlColor);
        keywords.put("BY", sqlColor);
        keywords.put("CASE", sqlColor);
        keywords.put("DELETE", sqlColor);
        keywords.put("DISTINCT", sqlColor);
        keywords.put("DROP", sqlColor);
        keywords.put("EXECUTE", sqlColor);
        keywords.put("FROM", sqlColor);
        keywords.put("GROUP", sqlColor);
        keywords.put("HAVING", sqlColor);
        keywords.put("IN", sqlColor);
        keywords.put("INNER", sqlColor);
        keywords.put("INSERT", sqlColor);
        keywords.put("INTO", sqlColor);
        keywords.put("IS", sqlColor);
        keywords.put("JOIN", sqlColor);
        keywords.put("LEFT", sqlColor);
        keywords.put("LIMIT", sqlColor);
        keywords.put("NOT", sqlColor);
        keywords.put("NULL", sqlColor);
        keywords.put("OFFSET", sqlColor);
        keywords.put("ON", sqlColor);
        keywords.put("OR", sqlColor);
        keywords.put("ORDER", sqlColor);
        keywords.put("OUTER", sqlColor);
        keywords.put("PERFORM", sqlColor);
        keywords.put("RIGHT", sqlColor);
        keywords.put("SET", sqlColor);
        keywords.put("SELECT", sqlColor);
        keywords.put("UNION", sqlColor);
        keywords.put("UPDATE", sqlColor);
        keywords.put("USING", sqlColor);
        keywords.put("VALUES", sqlColor);
        keywords.put("WHEN", sqlColor);
        keywords.put("WHERE", sqlColor);
        keywords.put("BETWEEN", sqlColor);
        keywords.put("AVG", sqlFunctionColor);
        keywords.put("COUNT", sqlFunctionColor);
        keywords.put("CURRENT_DATE", sqlFunctionColor);
        keywords.put("CURRENT_TIME", sqlFunctionColor);
        keywords.put("CURRENT_TIMESTAMP", sqlFunctionColor);
        keywords.put("CURRENT_USER", sqlFunctionColor);
        keywords.put("ISNULL", sqlFunctionColor);
        keywords.put("MAX", sqlFunctionColor);
        keywords.put("MIN", sqlFunctionColor);
        keywords.put("SESSION_USER", sqlFunctionColor);
        keywords.put("SUM", sqlFunctionColor);
        keywords.put("USER", sqlFunctionColor);
        keywords.put("ALL", reservedColor);
        keywords.put("ANALYSE", reservedColor);
        keywords.put("ANALYZE", reservedColor);
        keywords.put("ANY", reservedColor);
        keywords.put("AS", reservedColor);
        keywords.put("ASC", reservedColor);
        keywords.put("BEGIN", keywordColor);
        keywords.put("BOTH", reservedColor);
        keywords.put("CAST", reservedColor);
        keywords.put("CHECK", reservedColor);
        keywords.put("COLLATE", reservedColor);
        keywords.put("COLUMN", reservedColor);
        keywords.put("CONSTRAINT", reservedColor);
        keywords.put("CREATE", reservedColor);
        keywords.put("DECLARE", reservedColor);
        keywords.put("DEFAULT", reservedColor);
        keywords.put("DEFERRABLE", reservedColor);
        keywords.put("DESC", reservedColor);
        keywords.put("DO", reservedColor);
        keywords.put("ELSE", reservedColor);
        keywords.put("ELSEIF", reservedColor);
        keywords.put("END", keywordColor);
        keywords.put("EXCEPT", reservedColor);
        keywords.put("FALSE", reservedColor);
        keywords.put("FOR", reservedColor);
        keywords.put("FOREIGN", reservedColor);
        keywords.put("GRANT", reservedColor);
        keywords.put("IF", reservedColor);
        keywords.put("INITIALLY", reservedColor);
        keywords.put("INTERSECT", reservedColor);
        keywords.put("LEADING", reservedColor);
        keywords.put("LOCALTIME", reservedColor);
        keywords.put("LOCALTIMESTAMP", reservedColor);
        keywords.put("NEW", reservedColor);
        keywords.put("OFF", reservedColor);
        keywords.put("OLD", reservedColor);
        keywords.put("ONLY", reservedColor);
        keywords.put("OPEN", reservedColor);
        keywords.put("PLACING", reservedColor);
        keywords.put("PRIMARY", reservedColor);
        keywords.put("REFERENCES", reservedColor);
        keywords.put("RETURN", reservedColor);
        keywords.put("SOME", reservedColor);
        keywords.put("TABLE", reservedColor);
        keywords.put("THEN", reservedColor);
        keywords.put("TO", reservedColor);
        keywords.put("TRAILING", reservedColor);
        keywords.put("TRUE", reservedColor);
        keywords.put("UNIQUE", reservedColor);
        keywords.put("LOOP", reservedColor);
        style = new SimpleAttributeSet();
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        super.insertString(offset, str, attr);
        processChangedLines(offset, str.length());
    }

    public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        processChangedLines(offset, length);
    }

    public void processChangedLines(int offset, int length) throws BadLocationException {
        String text = getText(0, getLength());
        highlightString(Color.black, 0, getLength(), true, false, false);
        Set<String> keyw = keywords.keySet();
        for (String keyword : keyw) {
            Color col = keywords.get(keyword);
            Pattern p = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            while (m.find()) {
                highlightString(col, m.start(), keyword.length(), true, true, false);
            }
        }
        int mlsStart = text.indexOf(stringDelimiter);
        while (mlsStart > -1) {
            int mlsEnd = text.indexOf(stringDelimiter, mlsStart + 1);
            mlsEnd = (mlsEnd < 0 ? text.length() - 1 : mlsEnd);
            highlightString(stringColor, mlsStart, (mlsEnd - (mlsStart - 1)), true, false, false);
            mlsStart = text.indexOf(stringDelimiter, mlsEnd + 1);
        }
        int mlqStart = text.indexOf(quoteDelimiter);
        while (mlqStart > -1) {
            int mlqEnd = text.indexOf(quoteDelimiter, mlqStart + 1);
            mlqEnd = (mlqEnd < 0 ? text.length() - 1 : mlqEnd);
            highlightString(quoteColor, mlqStart, (mlqEnd - (mlqStart - 1)), true, false, false);
            mlqStart = text.indexOf(quoteDelimiter, mlqEnd + 1);
        }
        Matcher mlcStart = multiLineCommentDelimiterStart.matcher(text);
        Matcher mlcEnd = multiLineCommentDelimiterEnd.matcher(text);
        while (mlcStart.find()) {
            if (mlcEnd.find(mlcStart.end())) highlightString(commentColor, mlcStart.start(), (mlcEnd.end() - mlcStart.start()), true, false, true); else highlightString(commentColor, mlcStart.start(), getLength(), true, false, true);
        }
        Matcher slc = singleLineCommentDelimter.matcher(text);
        while (slc.find()) {
            int line = rootElement.getElementIndex(slc.start());
            int endOffset = rootElement.getElement(line).getEndOffset() - 1;
            highlightString(commentColor, slc.start(), (endOffset - slc.start()), true, false, true);
        }
    }

    public void highlightString(Color col, int begin, int length, boolean flag, boolean bold, boolean italic) {
        StyleConstants.setForeground(style, col);
        StyleConstants.setBold(style, bold);
        StyleConstants.setItalic(style, italic);
        setCharacterAttributes(begin, length, style, flag);
    }

    public void addSQLFunctionKeyword(String _keyword) {
        keywords.put(_keyword, sqlFunctionColor);
    }
}

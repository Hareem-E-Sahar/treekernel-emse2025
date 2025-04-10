package org.databene.html;

import org.databene.commons.CollectionUtil;
import org.databene.commons.IOUtil;
import org.databene.commons.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.text.ParseException;
import java.util.Set;
import java.util.Stack;
import java.util.Map;

/**
 * Provides utility methods for converting HTML to XML.<br/>
 * <br/>
 * Created: 25.01.2007 17:10:37
 * @author Volker Bergmann
 */
public class HTML2XML {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTML2XML.class);

    private static final Set<String> COMMON_CODES = CollectionUtil.toSet("lt", "gt", "amp");

    public static String convert(String html) throws ParseException {
        Reader reader = new StringReader(html);
        StringWriter writer = new StringWriter();
        try {
            ConversionContext context = new ConversionContext(reader, writer, "UTF-8");
            convert(context);
            return writer.getBuffer().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtil.close(reader);
        }
    }

    public static void convert(Reader reader, OutputStream out, String encoding) throws ParseException, UnsupportedEncodingException {
        Writer writer = new OutputStreamWriter(out, encoding);
        try {
            ConversionContext context = new ConversionContext(reader, writer, encoding);
            convert(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtil.close(reader);
            IOUtil.close(writer);
        }
    }

    private static void convert(ConversionContext context) throws IOException, ParseException {
        int token;
        while ((token = context.tokenizer.nextToken()) != HTMLTokenizer.END) {
            switch(token) {
                case HTMLTokenizer.START_TAG:
                case HTMLTokenizer.CLOSED_TAG:
                    ensureXmlHeader(context);
                    if ("script".equalsIgnoreCase(context.tokenizer.name())) continue;
                    String lcTagName = context.tokenizer.name().toLowerCase();
                    if (!"html".equals(lcTagName) && !context.rootCreated) ensureRootElement(context); else if ("html".equals(lcTagName) && context.rootCreated) {
                        LOGGER.warn("Malformed HTML document: misplaced <HTML> element");
                        break;
                    } else {
                        if (context.path.size() > 0) {
                            String lastTagName = context.path.peek();
                            if (HTMLUtil.isEmptyTag(lastTagName) && !context.tokenizer.name().equals(lastTagName)) {
                                context.writer.write("</" + lastTagName + '>');
                                context.path.pop();
                            }
                        }
                    }
                    context.rootCreated = true;
                    if (token == HTMLTokenizer.CLOSED_TAG) {
                        writeEmptyTag(context.writer, context.tokenizer);
                    } else {
                        writeStartTag(context.writer, context.tokenizer);
                        context.path.push(context.tokenizer.name());
                    }
                    break;
                case (HTMLTokenizer.END_TAG):
                    if ("script".equalsIgnoreCase(context.tokenizer.name())) continue;
                    boolean done = false;
                    if (contains(context.path, context.tokenizer.name())) {
                        do {
                            String pathTagName = context.path.pop();
                            context.writer.write("</" + pathTagName + '>');
                            if (pathTagName.equals(context.tokenizer.name())) done = true;
                        } while (!done);
                    }
                    if ("html".equalsIgnoreCase(context.tokenizer.name())) return;
                    break;
                case HTMLTokenizer.TEXT:
                    ensureXmlHeader(context);
                    String text = context.tokenizer.text();
                    if (text != null && text.trim().length() > 0) ensureRootElement(context);
                    writeText(context.writer, text);
                    break;
                case HTMLTokenizer.COMMENT:
                    ensureRootElement(context);
                    String comment = context.tokenizer.text();
                    int s = comment.indexOf("<!--") + "<!--".length();
                    int e = comment.lastIndexOf("-->");
                    comment = "<!--" + comment.substring(s, e).replace("--", "- ") + "-->";
                    writeXml(context.writer, comment);
                    break;
                case HTMLTokenizer.DOCUMENT_TYPE:
                    break;
                case HTMLTokenizer.PROCESSING_INSTRUCTION:
                    String piText = context.tokenizer.text();
                    writeXml(context.writer, piText);
                    if (piText.startsWith("<?xml")) context.xmlHeaderCreated = true;
                    break;
                case HTMLTokenizer.SCRIPT:
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported token type: " + token);
            }
        }
        while (context.path.size() > 0) {
            String tagName = context.path.pop();
            context.writer.write("</" + tagName + '>');
        }
    }

    private static void ensureXmlHeader(ConversionContext context) throws IOException {
        if (!context.xmlHeaderCreated) {
            context.writer.write("<?xml " + "version=\"1.0\" " + "encoding=\"" + context.encoding + "\"?>" + SystemInfo.getLineSeparator());
            context.xmlHeaderCreated = true;
        }
    }

    private static void ensureRootElement(ConversionContext context) throws IOException {
        ensureXmlHeader(context);
        if (!context.rootCreated && !"html".equals(context.tokenizer.name())) {
            writeStartTag(context.writer, "html");
            context.path.push("html");
            context.rootCreated = true;
        }
    }

    private static boolean contains(Stack<String> path, String name) {
        for (String tagName : path) if (tagName.equals(name)) return true;
        return false;
    }

    private static void writeEmptyTag(Writer writer, HTMLTokenizer tokenizer) throws IOException {
        writer.write('<' + tokenizer.name());
        writeAttributes(writer, tokenizer);
        writer.write("/>");
    }

    private static void writeStartTag(Writer writer, HTMLTokenizer tokenizer) throws IOException {
        writer.write('<' + tokenizer.name());
        writeAttributes(writer, tokenizer);
        writer.write('>');
    }

    private static void writeStartTag(Writer writer, String name) throws IOException {
        writer.write('<' + name + '>');
    }

    private static void writeAttributes(Writer writer, HTMLTokenizer tokenizer) throws IOException {
        for (Map.Entry<String, String> entry : tokenizer.attributes().entrySet()) {
            String value = entry.getValue();
            char quote = '"';
            if (value == null) value = ""; else if (value.contains("\"")) quote = '\'';
            writer.write(' ');
            writer.write(entry.getKey());
            writer.write('=');
            writer.write(quote);
            writeText(writer, value);
            writer.write(quote);
        }
    }

    private static void writeXml(Writer writer, String s) throws IOException {
        s = resolveEntities(writer, s);
        writer.write(s);
    }

    private static void writeText(Writer writer, String s) throws IOException {
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        s = resolveEntities(writer, s);
        writer.write(s);
    }

    private static String resolveEntities(Writer writer, String s) throws IOException {
        int i;
        while ((i = s.indexOf('&')) >= 0) {
            HTMLEntity entity = HTMLEntity.getEntity(s, i);
            if (entity != null) {
                writer.write(s.substring(0, i + 1));
                if (COMMON_CODES.contains(entity.htmlCode)) writer.write(entity.htmlCode + ';'); else writer.write("#" + entity.xmlCode + ";");
                s = s.substring(s.indexOf(';', i) + 1);
            } else {
                writer.write(s.substring(0, i));
                writer.write("&amp;");
                s = s.substring(i + 1);
            }
        }
        return s;
    }

    private static class ConversionContext {

        public String encoding;

        Writer writer;

        HTMLTokenizer tokenizer;

        Stack<String> path;

        boolean xmlHeaderCreated;

        boolean rootCreated;

        ConversionContext(Reader reader, Writer writer, String encoding) {
            this.tokenizer = new DefaultHTMLTokenizer(reader);
            this.path = new Stack<String>();
            this.xmlHeaderCreated = false;
            this.rootCreated = false;
            this.writer = writer;
            this.encoding = encoding;
        }
    }
}

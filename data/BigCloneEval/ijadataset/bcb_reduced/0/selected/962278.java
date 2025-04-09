package takatuka.codegen.natcompile;

import java.util.regex.*;
import java.util.logging.*;
import java.io.*;
import java.util.*;

public class Parser {

    static class ParseError extends RuntimeException {

        ParseError() {
            super("undefined");
        }

        ParseError(String msg) {
            super(msg);
        }
    }

    ;

    static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static final String idPattern = "[A-Za-z_][A-Za-z0-9_.]*";

    public static final String typePattern = String.format("%s\\s*(?:\\[\\s*\\])?", idPattern);

    private static Pattern methodStart = Pattern.compile(String.format("\\s*native\\s*(static)?\\s+(%1$s)\\s+(%1$s)\\s*\\((.*?)\\)\\s*\\{", idPattern));

    private static Pattern methodParam = Pattern.compile(String.format("\\s*(%2$s)\\s+(%1$s)\\s*,?", idPattern, typePattern));

    private static Pattern methodReturn = Pattern.compile("[;\\}\\{:\\s]\\s*(return)(.*?)\\s*;");

    private static Pattern objectAccess = Pattern.compile(String.format("%s(JNI_GETFIELD)\\s*\\(\\s*(%s)\\s*,\\s*(%s)\\s*,\\s*(%s)\\s*\\)", "[^A-Za-z0-9_.]", idPattern, idPattern, idPattern));

    private static Pattern staticAccess = Pattern.compile(String.format("%s(JNI_GETSTATIC)\\s*\\(\\s*(%s)\\s*,\\s*(%s)\\s*\\)", "[^A-Za-z0-9_.]", idPattern, idPattern));

    private List<Method.Param> getParams(String pstr) throws Types.ParseError {
        Vector<Method.Param> result = new Vector<Method.Param>();
        Matcher m = methodParam.matcher(pstr);
        while (m.find()) {
            result.add(new Method.Param(Types.parse(m.group(1)), m.group(2)));
            log.info(String.format("found param: %s %s", result.lastElement().type.javaName, result.lastElement().name));
        }
        return result;
    }

    private void buildMethod(Method result, String body) {
        Matcher stat = Pattern.compile(String.format("(%s)|(%s)|(%s)", methodReturn.toString(), objectAccess.toString(), staticAccess.toString())).matcher(body);
        int currentPos = 0;
        while (stat.find()) {
            Matcher returns = methodReturn.matcher(stat.group());
            Matcher oAccess = objectAccess.matcher(stat.group());
            Matcher sAccess = staticAccess.matcher(stat.group());
            returns.find();
            oAccess.find();
            sAccess.find();
            if (returns.matches()) {
                result.append(new UnchangedCode(body.substring(currentPos, stat.start() + returns.start(1))));
                result.append(new Method.Return(returns.group(2)));
            } else if (oAccess.matches()) {
                result.append(new UnchangedCode(body.substring(currentPos, stat.start() + oAccess.start(1))));
                result.append(new Method.ObjectAccess(oAccess.group(2), oAccess.group(3), oAccess.group(4)));
            } else {
                result.append(new UnchangedCode(body.substring(currentPos, stat.start() + sAccess.start(1))));
                result.append(new Method.StaticAccess(sAccess.group(2), sAccess.group(3)));
            }
            currentPos = stat.end();
        }
        result.append(new UnchangedCode(body.substring(currentPos, body.length())));
    }

    private Method parseMethod(Matcher header, String body) throws Types.ParseError {
        Method result;
        if (header.group(1) != null) {
            result = new StaticMethod(header.group(3), Types.parse(header.group(2)), getParams(header.group(4)));
        } else {
            result = new VirtualMethod(header.group(3), Types.parse(header.group(2)), getParams(header.group(4)));
        }
        buildMethod(result, body);
        return result;
    }

    private int findMethodEnd(String in, int start) throws ParseError {
        BufferedReader instream = new BufferedReader(new StringReader(in));
        int c;
        int depth = 1;
        try {
            instream.skip(start);
            for (c = instream.read(); c != -1; c = instream.read(), start++) {
                if (c == '}') {
                    depth--;
                }
                if (c == '{') {
                    depth++;
                }
                if (depth == 0) {
                    break;
                }
            }
            if (c == -1) {
                throw new ParseError();
            }
        } catch (IOException e) {
            throw new ParseError();
        }
        return start;
    }

    private ClassImplementation initClass(StringBuffer b) {
        String tmp = b.toString();
        Matcher m;
        ClassImplementation ci;
        try {
            m = Pattern.compile("\\s*class\\s+(\\S+)\\s*;").matcher(tmp);
            m.find();
            ci = new ClassImplementation(m.group(1));
            b.delete(0, m.end());
        } catch (Exception e) {
            throw new ParseError("error parsing class specification:" + e.getMessage());
        }
        return ci;
    }

    public ClassImplementation parse(String in) throws ParseError, Types.ParseError {
        StringBuffer buf = new StringBuffer(in);
        ClassImplementation result = initClass(buf);
        in = buf.toString();
        Matcher m = methodStart.matcher(in);
        int currentPos = 0;
        while (m.find()) {
            result.append(new UnchangedCode(in.substring(currentPos, m.start())));
            currentPos = findMethodEnd(in, m.end());
            result.append(parseMethod(m, in.substring(m.end(), currentPos)));
        }
        result.append(new UnchangedCode(in.substring(currentPos, in.length())));
        return result;
    }

    public Parser() {
    }
}

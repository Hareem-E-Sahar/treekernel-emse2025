package fr.ninauve.jnoob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XslJniGenerator {

    private String replaceLabel(final String inStr) {
        final StringBuffer sb = new StringBuffer();
        int start = 0;
        final Pattern pattern = Pattern.compile("[$][{][^}]+[}]");
        final Matcher matcher = pattern.matcher(inStr);
        while (matcher.find()) {
            sb.append(inStr.substring(start, matcher.start()));
            if ("${SP}".equals(matcher.group())) {
                sb.append("<xsl:text> </xsl:text>");
            } else if ("${CR}".equals(matcher.group())) {
                sb.append("<xsl:text>\n</xsl:text>");
            } else if ("${TAB}".equals(matcher.group())) {
                sb.append("<xsl:text>\t</xsl:text>");
            } else if (matcher.group().startsWith("${cType(")) {
                final String balise = "<xsl:value-of select=\"java:fr.ninauve.jnoob.ReflectUtils.getCType(XXXX)\"/>";
                sb.append(balise.replace("XXXX", matcher.group().substring("${cType(".length(), matcher.group().length() - 2)));
            } else if (matcher.group().startsWith("${condensedType(")) {
                final String balise = "<xsl:value-of select=\"java:fr.ninauve.jnoob.ReflectUtils.getJniParam(XXXX)\"/>";
                sb.append(balise.replace("XXXX", matcher.group().substring("${condensedType(".length(), matcher.group().length() - 2)));
            } else if (matcher.group().startsWith("${jniCallMethod(")) {
                final String balise = "<xsl:value-of select=\"java:fr.ninauve.jnoob.ReflectUtils.getJniCallMethod(XXXX)\"/>";
                sb.append(balise.replace("XXXX", matcher.group().substring("${jniCallMethod(".length(), matcher.group().length() - 2)));
            } else if (matcher.group().startsWith("${lookupType(")) {
                final String balise = "<xsl:value-of select=\"java:fr.ninauve.jnoob.ReflectUtils.getLookupType(XXXX)\"/>";
                sb.append(balise.replace("XXXX", matcher.group().substring("${lookupType(".length(), matcher.group().length() - 2)));
            } else {
                final String balise = "<xsl:value-of select=\"XXXX\"/>";
                sb.append(balise.replace("XXXX", matcher.group().substring(2, matcher.group().length() - 1)));
            }
            start = matcher.end();
        }
        if (start <= inStr.length() - 1) sb.append(inStr.substring(start));
        return sb.toString();
    }

    private void generate_xsl(final InputStream inStream, final File outFile) throws Exception {
        final BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        final PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
        String line = null;
        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        out.println("");
        out.println("<xsl:stylesheet version=\"1.0\"");
        out.println("\txmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"");
        out.println("\txmlns:java=\"http://xml.apache.org/xalan/java\">");
        out.println("");
        out.println("<xsl:output");
        out.println("\tmethod=\"text\"");
        out.println("\tindent=\"no\"");
        out.println("\tencoding=\"UTF-8\" />");
        out.println("");
        while ((line = in.readLine()) != null) {
            String temp = line.trim().replaceAll(">\\s+<", "><");
            temp = replaceLabel(temp);
            out.print(temp);
        }
        out.flush();
        out.close();
    }

    private static void print_args(final Class[] params, final PrintWriter out) {
        int i = 0;
        for (Class param : params) {
            out.print("\t\t<arg name=\"arg");
            out.print(i);
            out.print("\" type=\"");
            out.print(param.getName());
            out.println("\" />");
            i++;
        }
    }

    public void generate_xml(final Class aClass, final File dir) throws Exception {
        dir.mkdirs();
        final File xml = new File(dir, aClass.getSimpleName() + ".xml");
        final PrintWriter out = new PrintWriter(xml);
        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        out.println("<class simple-name=\"" + aClass.getSimpleName() + "\" name=\"" + aClass.getName() + "\">");
        {
            int i = 0;
            for (Constructor constructor : aClass.getConstructors()) {
                out.print("\t<constructor ");
                out.print("name=\"create_");
                out.print(i);
                out.println("\">");
                print_args(constructor.getParameterTypes(), out);
                out.println("\t</constructor>");
                i++;
            }
        }
        final Map<String, Set<Method>> methodsByName = new LinkedHashMap<String, Set<Method>>();
        for (Method method : aClass.getMethods()) {
            if (Object.class.equals(method.getDeclaringClass())) continue;
            Set<Method> methods = methodsByName.get(method.getName());
            if (methods == null) {
                methods = new LinkedHashSet<Method>();
            }
            methods.add(method);
            methodsByName.put(method.getName(), methods);
        }
        for (Set<Method> methods : methodsByName.values()) {
            if (methods.size() == 1) {
                final Method method = methods.iterator().next();
                printMethod(out, method, method.getName());
            } else {
                int i = 0;
                for (Method method : methods) {
                    printMethod(out, method, method.getName() + "_" + i);
                    i++;
                }
            }
        }
        for (Field field : aClass.getFields()) {
            out.print("<field name=\"");
            out.print(field.getName());
            out.print("\" type=\"");
            out.print(field.getType().getName());
            out.print("\" />\n");
        }
        out.println("</class>");
        out.flush();
        out.close();
    }

    private static void printMethod(final PrintWriter out, final Method method, final String cName) {
        final String element;
        if (Modifier.isStatic(method.getModifiers())) element = "static"; else element = "method";
        out.printf("\t<%s name=\"" + cName + "\" jname=\"" + method.getName() + "\" result=\"" + method.getReturnType().getName() + "\" >\n", element);
        print_args(method.getParameterTypes(), out);
        out.printf("</%s>\n", element);
    }

    private static InputStream getResource(final String path) throws IOException {
        return XslJniGenerator.class.getClassLoader().getResource(path).openStream();
    }

    public void generate(final Class aClass, final File dir) throws Exception {
        System.out.println("generating class -> " + aClass.getSimpleName());
        final File xml = new File(dir, aClass.getSimpleName() + ".xml");
        dir.mkdirs();
        generate_xsl(getResource("templates/jni_c.xsl.in"), new File(dir, "jni_c.xsl"));
        generate_xsl(getResource("templates/jni_h.xsl.in"), new File(dir, "jni_h.xsl"));
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource(new File(dir, "jni_h.xsl")));
        transformer.transform(new StreamSource(xml), new StreamResult(new FileOutputStream(new File(dir, "J_" + aClass.getSimpleName() + "_Class.h"))));
        transformer = tFactory.newTransformer(new StreamSource(new File(dir, "jni_c.xsl")));
        transformer.transform(new StreamSource(xml), new StreamResult(new FileOutputStream(new File(dir, "J_" + aClass.getSimpleName() + "_Class.c"))));
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) throws Exception {
        final List<String> argsList = Arrays.asList(args);
        XslJniGenerator generator = new XslJniGenerator();
        File outDir = new File(".");
        if (argsList.contains("-out")) {
            outDir = new File(argsList.get(argsList.indexOf("-out") + 1));
        }
        if (argsList.contains("-xml")) {
            if (!argsList.contains("-class")) {
                System.out.println("xml action requires -class option");
                return;
            }
            final String className = argsList.get(argsList.indexOf("-class") + 1);
            final Class classObj = Class.forName(className);
            generator.generate_xml(classObj, outDir);
        }
        if (argsList.contains("-native")) {
            if (!argsList.contains("-class")) {
                System.out.println("native action requires -class option");
                return;
            }
            final String className = argsList.get(argsList.indexOf("-class") + 1);
            final Class classObj = Class.forName(className);
            generator.generate(classObj, outDir);
        }
    }
}

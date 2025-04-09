package org.wfp.rita.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wfp.rita.base.RitaException;
import org.wfp.rita.exception.RitaInternalError;
import org.wfp.rita.util.JettyLoader;
import org.wfp.rita.web.controller.ExecutableJarServlet.XmlTraverser.Node;

/**
 * This servlet handles /rita-executable.jar and dynamically assembles the
 * Local Instance JAR file from the running code. This is less stable and
 * maintainable than the one produced by the 
 * <code>mvn assembly:assembly</code> goal, but more convenient for rapid
 * development.
 * 
 * <p>When using this in a development environment, particularly while
 * developing under Eclipse, please note that it will use the current
 * contents of your <code>web/WEB-INF/lib</code> directory, which is
 * not the same as the libraries used by Eclipse, or the JettyLoader
 * running under Eclipse. Therefore, you might build a JAR file that
 * doesn't work, if some dependencies have changed since your
 * <code>lib</code> directory was populated, or you might never have
 * populated it.
 * 
 * <p>The easiest way to update your <code>lib</code> directory is to run
 * <code>mvn war:inplace</code>, before you download an executable JAR for
 * the first time, and whenever you modify the <code>pom.xml</code> file.
 * @author chris
 *
 */
public class ExecutableJarServlet extends HttpServlet {

    private static URL webDirUrl;

    private static Logger LOG = LoggerFactory.getLogger(ExecutableJarServlet.class);

    static class XmlTraverser {

        static class Node {

            private String name;

            private List<Node> children = new ArrayList<Node>();

            public Node(String name) {
                this.name = name;
            }

            void append(Node child) {
                children.add(child);
            }

            public String name() {
                return name;
            }

            public List<Node> children() {
                return new ArrayList<Node>(children);
            }

            public Node firstChild(String name) {
                for (Node child : children) {
                    if (child.name.equals(name)) {
                        return child;
                    }
                }
                return null;
            }

            public Node forceChild(String name) {
                Node child = firstChild(name);
                if (child == null) {
                    throw new AssertionFailedError("Expected to find <" + name + "> within " + toString());
                }
                return child;
            }

            public String text() {
                if (children.size() != 1) {
                    throw new AssertionFailedError("Expected to find only " + "one node within " + toString());
                }
                Node child = children.get(0);
                if (!(child instanceof TextNode)) {
                    throw new AssertionFailedError("Expected to find only " + "text within " + toString());
                }
                return child.name();
            }

            public String toString() {
                StringBuilder str = new StringBuilder();
                str.append("<").append(name).append(">");
                for (Node child : children) {
                    str.append(child.toString());
                }
                str.append("</").append(name).append(">\n");
                return str.toString();
            }
        }

        static class TextNode extends Node {

            public TextNode(String text) {
                super(text);
            }

            public String toString() {
                return '"' + name() + '"';
            }

            void append(Node child) {
                throw new IllegalArgumentException("Cannot add children " + "to a text node");
            }
        }

        public static Node parse(InputStream input, String expectedRootNode) throws XMLStreamException {
            Stack<Node> stack = new Stack<Node>();
            Node current = null, root = null;
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(input);
            for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
                if (event == XMLStreamConstants.START_ELEMENT) {
                    Node newNode = new Node(parser.getLocalName());
                    if (current == null) {
                        current = newNode;
                        root = newNode;
                        if (!root.name().equals(expectedRootNode)) {
                            throw new ComparisonFailure("Wrong root node", expectedRootNode, root.name());
                        }
                    } else {
                        current.append(newNode);
                    }
                    stack.push(newNode);
                    current = newNode;
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if (!current.name().equals(parser.getLocalName())) {
                        throw new ComparisonFailure("End element", current.name(), parser.getLocalName());
                    }
                    Node popped = stack.pop();
                    if (!popped.equals(current)) {
                        throw new ComparisonFailure("Top of stack", current.name(), popped.name());
                    }
                    if (stack.isEmpty()) {
                        current = null;
                    } else {
                        current = stack.peek();
                    }
                } else if (event == XMLStreamConstants.CHARACTERS) {
                    String text = parser.getText();
                    if (text.matches("\\s+")) {
                    } else {
                        current.append(new TextNode(text));
                    }
                }
            }
            if (!stack.isEmpty()) {
                throw new AssertionFailedError("Expected empty stack at " + "end of document but still had: " + stack);
            }
            return root;
        }
    }

    private List<String> parseExcludes(Node excludes, boolean convertPackagesToJars) throws RitaException, XMLStreamException {
        if (excludes == null) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<String>();
        for (Node exclude : excludes.children()) {
            if (!exclude.name().equals("exclude") && !exclude.name().equals("include")) {
                continue;
            }
            String pattern = exclude.text();
            if (convertPackagesToJars) {
                pattern = pattern.replaceFirst(".*:", "");
            } else if (pattern.endsWith("/")) {
                pattern += "*";
            }
            results.add(pattern);
        }
        return results;
    }

    private void addToJar(JarOutputStream jos, String name, long size, long time, InputStream is) throws IOException {
        ZipEntry newEntry = new ZipEntry(name);
        if (size != -1) {
            newEntry.setSize(size);
        }
        newEntry.setTime(time);
        jos.putNextEntry(newEntry);
        byte buffer[] = new byte[1024];
        int copied;
        do {
            copied = is.read(buffer);
            if (copied > 0) {
                jos.write(buffer, 0, copied);
            }
        } while (copied > 0);
        jos.closeEntry();
    }

    private void addFilesToJar(JarOutputStream jos, File source, String targetName, String baseName, List<String> excludes, Map<String, String> contents) throws IOException, RitaException {
        if (!targetName.startsWith(baseName)) {
            throw new RitaInternalError(targetName + " is not under " + baseName);
        }
        if (contents.containsKey(targetName) && !source.isDirectory()) {
            LOG.warn("Skipping duplicate file " + targetName + " from " + source.getName() + ", already found " + "in " + contents.get(targetName));
            return;
        }
        if (!contents.containsKey(targetName)) {
            contents.put(targetName, source.getPath());
            if (source.isDirectory()) {
                if (targetName.length() > 0) {
                    jos.putNextEntry(new ZipEntry(targetName));
                }
            } else {
                FileInputStream in = new FileInputStream(source);
                addToJar(jos, targetName, source.length(), source.lastModified(), in);
                in.close();
                return;
            }
        }
        for (File subFile : source.listFiles()) {
            String potentialName = targetName + subFile.getName();
            if (subFile.isDirectory()) {
                potentialName += "/";
            }
            boolean isExcluded = false;
            if (potentialName.indexOf(baseName) != 0) {
                throw new RitaInternalError(potentialName + " is not under " + baseName);
            }
            String nameUnderBasename = potentialName.substring(baseName.length());
            for (String exclude : excludes) {
                if (FilenameUtils.wildcardMatch(nameUnderBasename, exclude)) {
                    LOG.debug("Skipping excluded file " + nameUnderBasename + " from " + source.getName() + " which matches " + exclude);
                    isExcluded = true;
                    break;
                }
            }
            if (isExcluded) {
            } else {
                addFilesToJar(jos, subFile, potentialName, baseName, excludes, contents);
            }
        }
    }

    private void addJarContentsToJar(JarOutputStream jos, File inputJar, Map<String, String> alreadyAddedFiles, List<String> excludedFiles) throws IOException, ServletException {
        LOG.debug("Unpacking JAR " + inputJar.getName());
        JarInputStream jis = new JarInputStream(new FileInputStream(inputJar));
        for (JarEntry e = jis.getNextJarEntry(); e != null; e = jis.getNextJarEntry()) {
            String entryName = e.getName();
            boolean isExcluded = false;
            for (String pattern : excludedFiles) {
                if (FilenameUtils.wildcardMatch(entryName, pattern)) {
                    LOG.debug("Skipping excluded file " + entryName + " from " + inputJar.getName() + " which matches " + pattern);
                    isExcluded = true;
                    break;
                }
            }
            if (isExcluded) continue;
            String previousJarContainingName = alreadyAddedFiles.get(entryName);
            if (previousJarContainingName != null) {
                if (!e.isDirectory()) {
                    LOG.warn("Skipping duplicate file " + entryName + " from " + inputJar.getName() + ", already found " + "in " + previousJarContainingName);
                }
                continue;
            }
            alreadyAddedFiles.put(entryName, inputJar.getName());
            try {
                addToJar(jos, entryName, e.getSize(), e.getTime(), jis);
                jis.closeEntry();
            } catch (Exception ex) {
                throw new ServletException("Failed to copy " + entryName + " from " + inputJar, ex);
            }
        }
    }

    private String replace(String input, String match, String replacement) {
        for (int index = input.indexOf(match); index != -1; index = input.indexOf(match)) {
            StringBuilder output = new StringBuilder();
            output.append(input.substring(0, index));
            output.append(replacement);
            if (input.length() > index + match.length()) {
                output.append(input.substring(index + match.length()));
            }
            input = output.toString();
        }
        return input;
    }

    public static boolean hasThrownException = false;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("throwException") != null) {
            hasThrownException = true;
            throw new ServletException("test");
        }
        URLClassLoader loader = (URLClassLoader) getClass().getClassLoader();
        List<String> globalExcludedJars = new ArrayList<String>();
        try {
            if (webDirUrl == null) {
                webDirUrl = JettyLoader.getWebDirUrlBySearch();
            }
            if (webDirUrl == null) {
                throw new ServletException(new RitaInternalError("Cannot (yet) " + "serve executable JARs from a local instance"));
            }
            Manifest manifest = new Manifest(new FileInputStream(webDirUrl.getPath() + "META-INF/MANIFEST.MF"));
            Attributes attr = manifest.getMainAttributes();
            attr.put(new Attributes.Name("Implementation-Build"), new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            attr.put(new Attributes.Name("Created-By"), "ExecutableJarServlet");
            InputStream pomFile = getClass().getResourceAsStream("/pom.xml");
            Node project = XmlTraverser.parse(pomFile, "project");
            Node plugins = project.forceChild("build").forceChild("pluginManagement").forceChild("plugins");
            Node assemblyPlugin = null;
            for (Node plugin : plugins.children()) {
                if (plugin.forceChild("artifactId").text().equals("maven-assembly-plugin")) {
                    assemblyPlugin = plugin;
                }
            }
            Node mainClass = assemblyPlugin.forceChild("configuration").forceChild("archive").forceChild("manifest").forceChild("mainClass");
            attr.put(new Attributes.Name("Main-Class"), mainClass.text());
            JarOutputStream jos = new JarOutputStream(resp.getOutputStream(), manifest);
            InputStream configData = getClass().getResourceAsStream("/executable-jar.xml");
            Node assembly = XmlTraverser.parse(configData, "assembly");
            Node depSets = assembly.forceChild("dependencySets");
            Node deps = project.forceChild("dependencies");
            Collection<String> ignoredScopes = Arrays.asList("test");
            for (Node child : deps.children()) {
                Node scope = child.firstChild("scope");
                if (scope != null && ignoredScopes.contains(scope.text())) {
                    globalExcludedJars.add(child.forceChild("artifactId").text() + "-");
                }
            }
            Map<String, String> contents = new HashMap<String, String>();
            URL[] classpathUrls = loader.getURLs();
            for (Node depSet : depSets.children()) {
                if (!depSet.name().equals("dependencySet")) continue;
                List<String> excludedFiles = new ArrayList<String>();
                List<String> excludedJars = new ArrayList<String>();
                List<String> includedJars = new ArrayList<String>();
                String scope = null;
                for (Node child : depSet.children()) {
                    if (child.name().equals("unpackOptions")) {
                        Node excludes = child.firstChild("excludes");
                        excludedFiles = parseExcludes(excludes, false);
                    } else if (child.name().equals("excludes")) {
                        excludedJars = parseExcludes(child, true);
                    } else if (child.name().equals("includes")) {
                        includedJars = parseExcludes(child, true);
                    } else if (child.name().equals("scope")) {
                        scope = child.text();
                    }
                }
                if (scope != null) {
                    continue;
                }
                for (URL u : classpathUrls) {
                    String path = u.getPath();
                    if (!path.endsWith(".jar")) continue;
                    boolean isExcluded = false;
                    File jarFile = new File(path);
                    if (includedJars.size() > 0) {
                        isExcluded = true;
                        for (String includedJarPrefix : includedJars) {
                            if (jarFile.getName().toLowerCase().startsWith(includedJarPrefix.toLowerCase())) {
                                LOG.debug("Including JAR: " + jarFile.getName());
                                isExcluded = false;
                                break;
                            }
                        }
                    }
                    for (String excludedJarPrefix : excludedJars) {
                        if (jarFile.getName().toLowerCase().startsWith(excludedJarPrefix.toLowerCase())) {
                            LOG.debug("Excluding JAR: " + jarFile.getName());
                            isExcluded = true;
                            break;
                        }
                    }
                    if (!isExcluded) {
                        addJarContentsToJar(jos, jarFile, contents, excludedFiles);
                    }
                }
            }
            Node fileSets = assembly.forceChild("fileSets");
            for (Node fileSet : fileSets.children()) {
                String sourceDir = fileSet.forceChild("directory").text();
                sourceDir = replace(sourceDir, "${basedir}", "..");
                sourceDir = replace(sourceDir, "${project.build.outputDirectory}", "WEB-INF/classes");
                String targetDir = fileSet.forceChild("outputDirectory").text();
                targetDir = replace(targetDir, ".", "");
                if (targetDir.length() > 0 && !targetDir.endsWith("/")) {
                    targetDir += "/";
                }
                Node excludes = fileSet.firstChild("excludes");
                List<String> excludePatterns = Collections.emptyList();
                if (excludes != null) {
                    excludePatterns = parseExcludes(excludes, false);
                }
                addFilesToJar(jos, new File(webDirUrl.getPath(), sourceDir), targetDir, targetDir, excludePatterns, contents);
            }
            jos.finish();
            jos.close();
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}

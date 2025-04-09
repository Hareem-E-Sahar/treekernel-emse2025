package ru.ipo.problemsapi;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.ipo.problemsapi.zip.ZipProblem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: ilya
 * Date: 20.08.2010
 * Time: 13:54:01
 */
public class TestProblems {

    private static File problem;

    @BeforeClass
    public static void createProblem() throws IOException, ParserConfigurationException, TransformerException {
        problem = File.createTempFile("___prb___", ".problem");
        System.out.println("created temporary problem " + problem);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(problem));
        PrintStream ps = new PrintStream(zos);
        zos.putNextEntry(new ZipEntry("MANIFEST"));
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("resources");
        doc.appendChild(root);
        Element alias = doc.createElement("alias");
        alias.setAttribute("path", "STATEMENT");
        alias.setAttribute("target", "statement.txt");
        root.appendChild(alias);
        Element contents1 = doc.createElement("contents");
        contents1.setAttribute("path", "ANSWER");
        contents1.setTextContent("42");
        root.appendChild(contents1);
        Element contents2 = doc.createElement("contents");
        contents2.setAttribute("path", "STATEMENT EXT");
        contents2.setTextContent("TXT");
        root.appendChild(contents2);
        Element teacher = doc.createElement("teacher");
        teacher.setAttribute("path", "ANSWER");
        root.appendChild(teacher);
        Source source = new DOMSource(doc);
        Result result = new StreamResult(zos);
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
        zos.putNextEntry(new ZipEntry("statement.txt"));
        ps.println("���� ����� ������������ ����� � ����?");
        zos.closeEntry();
        zos.close();
    }

    @AfterClass
    public static void removeProblem() throws IOException {
        if (!problem.delete()) throw new IOException("failed to delete problem file: " + problem);
        System.out.println("deleted temporary problem " + problem);
    }

    @Test
    public void testGetString() throws IOException {
        Problem pr = new ZipProblem(problem);
        assertEquals("42", pr.getString("ANSWER"));
        assertEquals("TXT", pr.getString("STATEMENT EXT"));
        try {
            pr.getString("STATEMENT");
            throw new AssertionError("IOException needed");
        } catch (IOException ignored) {
        }
    }

    @Test
    public void testTeacher() throws IOException {
        Problem pr = new ZipProblem(problem);
        assertTrue(pr.isTeacher("ANSWER"));
        assertFalse(pr.isTeacher("STATEMENT"));
    }

    @Test
    public void testAlias() throws IOException {
        Problem pr = new ZipProblem(problem);
        InputStream is1 = pr.getReadStream("STATEMENT");
        InputStream is2 = pr.getReadStream("statement.txt");
        compareStreams(is1, is2);
        is1.close();
        is2.close();
    }

    private void compareStreams(InputStream is1, InputStream is2) throws IOException {
        int b1, b2;
        do {
            b1 = is1.read();
            b2 = is2.read();
            assertEquals(b1, b2);
        } while (b1 != -1);
    }
}

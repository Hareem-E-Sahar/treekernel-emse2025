package org.knowledgemanager.exporter;

import org.knowledgemanager.model.Graph;
import org.knowledgemanager.model.nonpersistent.GraphImp;
import org.knowledgemanager.model.Node;
import org.knowledgemanager.model.ImageContent;
import org.knowledgemanager.persistence.GraphWriter;
import org.knowledgemanager.util.FileUtility;
import org.log4j.BasicConfigurator;
import org.log4j.Category;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class KnowledgeExporter {

    static Category cat = Category.getInstance(KnowledgeExporter.class.getName());

    File outputFile;

    File graphFile;

    String graphName;

    public KnowledgeExporter(File outputFileVal, File graphFileVal) {
        outputFile = outputFileVal;
        graphFile = graphFileVal;
        graphName = graphFile.getName().substring(0, graphFile.getName().indexOf(".xml"));
    }

    /**returns the file in zip format,
     * which contains:
     * The graph file itself, the display informations of the graph
     * all files, that are referenced in the graph (which then are referenced relatively)
     *
     * display informations are assumed to be saved under the pattern
     * <directory of graph file>/<graphFileName>[<name user assigned>.]<displayname>.xml
     * directory structure:
     *   <graphFileName>.xml
     *   <graphFileName>.[<userAssignedName>.]<displayName>.layout
     *   <graphFileName>.files/file,file,file
     *
     */
    public void export() {
        try {
            class MyFilter implements FileFilter {

                public boolean accept(File file) {
                    return (file.getName().startsWith(graphName) && file.getName().endsWith("display"));
                }
            }
            ;
            cat.debug("graphFile: " + graphFile);
            File parentDir = graphFile.getParentFile();
            if (parentDir == null) parentDir = new File(".");
            File[] displayFiles = parentDir.listFiles(new MyFilter());
            Graph graph = (new GraphWriter()).readXML(new GraphImp(GraphImp.EMPTY_GRAPH), new FileInputStream(graphFile));
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
            for (int i = 0; i < displayFiles.length; i++) {
                File file = displayFiles[i];
                out.putNextEntry(new ZipEntry(file.getName()));
                byte[] arr = new byte[(int) file.length()];
                (new FileInputStream(file)).read(arr, 0, (int) file.length());
                out.write(arr, 0, (int) file.length());
                out.closeEntry();
            }
            out.putNextEntry(new ZipEntry(graphName + ".files/"));
            relativateFiles(graphName, graph, out);
            out.putNextEntry(new ZipEntry(graphName + ".xml"));
            (new GraphWriter()).writeXML(graph, out);
            out.closeEntry();
            out.flush();
            out.close();
        } catch (IOException exc) {
            exc.printStackTrace(System.err);
        }
    }

    public void relativateFiles(String graphName, Graph graph, ZipOutputStream out) {
        Node[] roots = graph.getRoots();
        for (int i = 0; i < roots.length; i++) {
            relativateFiles(graphName, roots[i], out);
        }
    }

    public void relativateFiles(String graphName, Node node, ZipOutputStream out) {
        if (node.getContent() instanceof ImageContent) {
            try {
                ImageContent content = (ImageContent) node.getContent();
                File file = content.getFile();
                String newFileName = graphName + ".files/" + file.getName();
                out.putNextEntry(new ZipEntry(newFileName));
                content.setFile(new File(newFileName));
                byte[] arr = new byte[(int) file.length()];
                (new FileInputStream(file)).read(arr, 0, (int) file.length());
                out.write(arr, 0, (int) file.length());
                out.closeEntry();
            } catch (IOException exc) {
            }
        }
        for (int i = 0; i < node.getChildren().size(); i++) {
            relativateFiles(graphName, (Node) node.getChildren().get(i), out);
        }
    }

    public static void main(String[] argv) {
        BasicConfigurator.configure();
        (new KnowledgeExporter(new File("test.zip"), new File("ooswe.xml"))).export();
    }
}

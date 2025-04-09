package th.co.edge.jseq.argouml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.w3c.dom.Document;
import ru.novosoft.uml.model_management.MModel;
import ru.novosoft.uml.xmi.IncompleteXMIException;
import ru.novosoft.uml.xmi.XMIWriter;
import th.co.edge.jseq.Diagram;
import th.co.edge.jseq.util.XMLUtil;

/**
 * An <code>ArgoUMLDiagram</code> is a <code>Diagram</code> that can be used
 * to generate sequence diagrams in .zargo format, readable by the tool <a
 * href="http://argouml.tigris.org/" target="new">ArgoUML</a>.
 *
 * <p>
 * The current version has only been verified to work with version 0.12 of
 * ArgoUML, and it is a known problem that it does not work with version 0.24 (<a
 * href=https://sourceforge.net/tracker/index.php?func=detail&aid=2046485&group_id=230519&atid=1080391"
 * target="new">SourceForge issue 2046485</a>).
 */
public class ArgoUMLDiagram implements Diagram {

    private Document argo;

    private MModel model;

    private List<SequenceDiagram> diagrams;

    /**
     * Creates a new <code>ArgoUMLDiagram</code>.
     *
     * @param argo
     *            an XML <code>Document</code> defining the contents of this
     *            file
     * @param model
     *            an <code>MModel</code> representation of an XMI document
     *            defining the parts of this UML model
     * @param diagrams
     *            a list of <code>SequenceDiagram</code>s, one per root
     *            activation
     */
    public ArgoUMLDiagram(Document argo, MModel model, List<SequenceDiagram> diagrams) {
        this.argo = argo;
        this.model = model;
        this.diagrams = diagrams;
    }

    /**
     * Writes this <code>ArgoUMLDiagram</code> to file in .zargo format, that
     * is, as a zipped set of XML model files.
     *
     * @param file
     *            the <code>File</code> to write to
     *
     * @throws IOException
     *             if the diagram could not be saved
     */
    public void save(File file) throws IOException {
        Writer writer = null;
        try {
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
            writer = new OutputStreamWriter(zip);
            writeXML(argo, "Untitled.argo", zip, writer);
            writeModel(model, "Untitled.xmi", zip, writer);
            int n = 0;
            for (SequenceDiagram diagram : diagrams) {
                String name = "SequenceDiagram" + (n++) + ".pgml";
                writeXML(diagram.getDocument(), name, zip, writer);
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }

    private void writeXML(Document xml, String name, ZipOutputStream zip, Writer writer) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        writer.write(XMLUtil.toString(xml));
        writer.flush();
        zip.closeEntry();
    }

    private void writeModel(MModel mModel, String name, ZipOutputStream zip, Writer writer) throws IOException {
        StringWriter stringWriter = new StringWriter();
        XMIWriter xmiWriter = new XMIWriter(mModel, stringWriter);
        try {
            xmiWriter.gen();
        } catch (IncompleteXMIException e) {
            e.printStackTrace();
            throw new IOException("Writing XMI failed: " + e.getMessage());
        }
        zip.putNextEntry(new ZipEntry(name));
        writer.write(stringWriter.toString());
        writer.flush();
        zip.closeEntry();
    }
}

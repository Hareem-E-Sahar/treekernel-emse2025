package uk.ac.rothamsted.ovtk.IO;

import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import uk.ac.rothamsted.ovtk.Console.Console;
import uk.ac.rothamsted.ovtk.GUI.MainFrame;
import uk.ac.rothamsted.ovtk.Graph.VisualONDEXGraph;
import uk.ac.rothamsted.ovtk.Util.CustomFileFilter;
import uk.ac.rothamsted.ovtk.Util.StopWatch;
import backend.core.AbstractConcept;
import backend.core.AbstractRelation;
import backend.core.memory.MemoryONDEXIterator;
import backend.core.security.Session;

/**
 * Writes the contained VisualONDEXGraph as XML to a file.
 * Options for only visible part export and visual attributes are available.
 * 
 * @author taubertj
 *
 */
public class ExportAsOXL extends Thread {

    private MainFrame mainFrame;

    private String filename;

    private boolean visible = false;

    private boolean visual = false;

    private boolean packed = false;

    /**
	 * Constructor for the actual mainframe of the OVTK
	 * 
	 * @param mainFrame - OVTK MainFrame
	 */
    public ExportAsOXL(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
	 * Set filtering for visible concepts/relations.
	 * 
	 * @param visible - boolean
	 */
    public void setOnlyVisible(boolean visible) {
        this.visible = visible;
    }

    /**
	 * Export also visual attributes of concepts/relations.
	 * 
	 * @param visual - boolean
	 */
    public void setExportVisual(boolean visual) {
        this.visual = visual;
    }

    /**
	 * Compress xml output as a zip file.
	 * 
	 * @param packed - boolean
	 */
    public void setPacked(boolean packed) {
        this.packed = packed;
    }

    /**
	 * Start export by displaying a filechooser.
	 * 
	 */
    @Override
    public void run() {
        JPanel options = new JPanel(new GridLayout(3, 1));
        JCheckBox visibleCheckBox = new JCheckBox("only visible", true);
        options.add(visibleCheckBox);
        JCheckBox visualCheckBox = new JCheckBox("visual attributes", false);
        options.add(visualCheckBox);
        JCheckBox packedCheckBox = new JCheckBox("compress output", true);
        options.add(packedCheckBox);
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        chooser.setAccessory(options);
        CustomFileFilter filter = new CustomFileFilter(new String[] { "xml", "zip" }, new String[] { "Backend XML File", "Compressed Backend XML File" });
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(mainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            filename = chooser.getSelectedFile().getAbsolutePath();
            if (filename.endsWith(".zip")) {
                filename = filename.substring(0, filename.length() - 4);
            }
            if (!filename.endsWith(".xml")) {
                filename = filename + ".xml";
            }
            visible = visibleCheckBox.isSelected();
            visual = visualCheckBox.isSelected();
            packed = packedCheckBox.isSelected();
            process(filename);
        } else {
            return;
        }
    }

    /**
	 * Process and output ONDEXGraph.
	 *
	 */
    public void process(String filename) {
        StopWatch watch = new StopWatch();
        watch.tic();
        Console.println(Console.TARGET_MAIN, "Saving file " + filename + ".");
        Console.startProgress("Saving file.");
        Session s = mainFrame.getSession();
        VisualONDEXGraph vog = mainFrame.getVisualONDEXGraph();
        VisualGDSBuilder vgb = new VisualGDSBuilder(s, vog, visible, visual);
        try {
            System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
            BufferedWriter writer;
            if (packed) {
                String zipname = filename + ".zip";
                ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipname));
                File file = new File(filename);
                zip.putNextEntry(new ZipEntry(file.getName()));
                writer = new BufferedWriter(new OutputStreamWriter(zip));
            } else {
                writer = new BufferedWriter(new FileWriter(filename));
            }
            XMLOutputFactory2 xmlof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
            xmlof.configureForSpeed();
            XMLStreamWriter2 xmlw = xmlof.createXMLStreamWriter(writer, "UTF-8");
            backend.exchange.xml.export.ondex.Export builder = new backend.exchange.xml.export.ondex.Export(s);
            builder.buildDocument(xmlw, new MemoryONDEXIterator<AbstractConcept>(vgb.getConcepts()), new MemoryONDEXIterator<AbstractRelation>(vgb.getRelations()));
            xmlw.flush();
            xmlw.close();
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            Console.printmsg(Console.TARGET_MAIN, ioe);
            Console.stopProgress();
        } catch (XMLStreamException xmlse) {
            Console.printmsg(Console.TARGET_MAIN, xmlse);
            Console.stopProgress();
        } catch (URISyntaxException urise) {
            Console.printmsg(Console.TARGET_MAIN, urise);
            Console.stopProgress();
        }
        double time = watch.toc();
        Console.println(Console.TARGET_MAIN, "Saving finished. - " + time + " s");
        Console.stopProgress();
    }
}

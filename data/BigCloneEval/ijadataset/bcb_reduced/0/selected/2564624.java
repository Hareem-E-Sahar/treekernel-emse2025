package uk.ac.rothamsted.ovtk.IO;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import uk.ac.rothamsted.ovtk.Console.Console;
import uk.ac.rothamsted.ovtk.GUI.MainFrame;
import uk.ac.rothamsted.ovtk.Graph.Concept;
import uk.ac.rothamsted.ovtk.Graph.Concept_Acc;
import uk.ac.rothamsted.ovtk.Graph.GeneralGraphData;
import uk.ac.rothamsted.ovtk.Graph.ONDEXGraph;
import uk.ac.rothamsted.ovtk.Graph.Relation;
import uk.ac.rothamsted.ovtk.Util.CustomFileFilter;

/**
 * @author taubertj
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ExportAsBeans extends Thread implements ActionListener {

    private JDesktopPane desktop;

    private JInternalFrame dialog;

    private MainFrame mainFrame;

    private boolean packed = true;

    private boolean visible = false;

    private JTextField textField;

    public ExportAsBeans(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.desktop = mainFrame.getDesktopPane();
    }

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();
        if (command == "abort") {
            dialog.setVisible(false);
            desktop.remove(dialog);
            dialog.dispose();
        } else if (command == "ok") {
            final String filename = textField.getText();
            if (filename.length() == 0) JOptionPane.showMessageDialog(dialog, "No filename given.", "Filename error", JOptionPane.ERROR_MESSAGE); else {
                Thread t = new Thread() {

                    public void run() {
                        process(filename);
                    }
                };
                t.start();
                dialog.setVisible(false);
                desktop.remove(dialog);
                dialog.dispose();
            }
        } else if (command == "packed") {
            JCheckBox checkBox = (JCheckBox) ae.getSource();
            packed = checkBox.isSelected();
        } else if (command == "visible") {
            JCheckBox checkBox = (JCheckBox) ae.getSource();
            visible = checkBox.isSelected();
        } else if (command == "chooser") {
            JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
            CustomFileFilter filter = new CustomFileFilter("oxl", "OVTk Java Beans XML Graph Files");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showSaveDialog(dialog);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String filename = chooser.getSelectedFile().getAbsolutePath();
                if (filename.lastIndexOf(".oxl") < 0 || filename.lastIndexOf(".oxl") < (filename.length() - 4)) {
                    filename = filename + ".oxl";
                }
                textField.setText(filename);
                textField.repaint();
            }
        }
    }

    public void setPacked(boolean packed) {
        this.packed = packed;
    }

    public void setOnlyVisible(boolean visible) {
        this.visible = visible;
    }

    public void process(String filename) {
        double start = System.currentTimeMillis();
        Console.println(0, "Saving file " + filename + ".");
        Console.startProgress("Saving file.");
        ONDEXGraph graph = mainFrame.getONDEXGraph();
        if (visible) graph = processVisible(graph);
        try {
            XMLEncoder encoder;
            if (!packed) {
                encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(filename)));
            } else {
                File file = new File(filename);
                ZipEntry zipEntry = new ZipEntry(file.getName());
                ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
                zipOutputStream.putNextEntry(zipEntry);
                encoder = new XMLEncoder(new BufferedOutputStream(zipOutputStream));
            }
            encoder.setPersistenceDelegate(Object2ObjectOpenHashMap.class, encoder.getPersistenceDelegate(Map.class));
            encoder.setPersistenceDelegate(Int2ObjectOpenHashMap.class, encoder.getPersistenceDelegate(Map.class));
            encoder.writeObject(graph);
            encoder.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        Console.println(0, "Saving finished. - " + (System.currentTimeMillis() - start) / 1000 + " s");
        Console.stopProgress();
    }

    public void run() {
        dialog = new JInternalFrame("Java Beans XML Export");
        dialog.setLayout(new GridLayout(4, 2));
        dialog.setSize(350, 110);
        JLabel label = new JLabel("Filename");
        dialog.add(label);
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BorderLayout());
        textField = new JTextField();
        filePanel.add(textField, BorderLayout.CENTER);
        JButton chooserButton = new JButton("...");
        chooserButton.setActionCommand("chooser");
        chooserButton.addActionListener(this);
        filePanel.add(chooserButton, BorderLayout.EAST);
        dialog.add(filePanel);
        label = new JLabel("packed?");
        dialog.add(label);
        JCheckBox packedCheckBox = new JCheckBox();
        packedCheckBox.setSelected(packed);
        packedCheckBox.setActionCommand("packed");
        packedCheckBox.addActionListener(this);
        dialog.add(packedCheckBox);
        label = new JLabel("just visible concepts");
        dialog.add(label);
        JCheckBox visibleCheckBox = new JCheckBox();
        visibleCheckBox.setSelected(visible);
        visibleCheckBox.setActionCommand("visible");
        visibleCheckBox.addActionListener(this);
        dialog.add(visibleCheckBox);
        JButton okButton = new JButton("OK");
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        dialog.add(okButton);
        JButton abortButton = new JButton("Abort");
        abortButton.setActionCommand("abort");
        abortButton.addActionListener(this);
        dialog.add(abortButton);
        dialog.setVisible(true);
        desktop.add(dialog);
        dialog.toFront();
    }

    private ONDEXGraph processVisible(ONDEXGraph o) {
        ONDEXGraph n = new ONDEXGraph();
        Int2ObjectOpenHashMap cs = o.getConceptIDs();
        Int2ObjectOpenHashMap ncs = new Int2ObjectOpenHashMap(cs.size(), 0.85f);
        Iterator it = cs.values().iterator();
        while (it.hasNext()) {
            Concept c = (Concept) it.next();
            if (c.isVisible()) {
                Concept nc = new Concept();
                nc.setColor(c.getColor());
                nc.setConcept_names(c.getConcept_names());
                nc.setDescription(c.getDescription());
                nc.setElement_of(c.getElement_of());
                nc.setGzipurl(c.getGzipurl());
                nc.setIcon(c.getIcon());
                nc.setId(c.getId());
                nc.setId_int(c.getId_int());
                nc.setLabel(c.getLabel());
                nc.setOf_type_FK(c.getOf_type_FK());
                nc.setProperties(c.getProperties());
                nc.setSequences(c.getSequences());
                nc.setShape(c.getShape());
                nc.setSize(c.getSize());
                nc.setStructure(c.getStructure());
                nc.setTaxid(c.getTaxid());
                nc.setUbiquitous(c.isUbiquitous());
                nc.setVisible(c.isVisible());
                nc.setX(c.getX());
                nc.setY(c.getY());
                ncs.put(nc.getId_int(), nc);
            }
        }
        n.setConceptIDs(ncs);
        GeneralGraphData ggdo = o.getGraphData();
        GeneralGraphData ggdn = new GeneralGraphData();
        Object2ObjectOpenHashMap accs = ggdo.getConcept_accs();
        Object2ObjectOpenHashMap naccs = new Object2ObjectOpenHashMap(accs.size(), 0.85f);
        it = accs.values().iterator();
        while (it.hasNext()) {
            Vector v = (Vector) it.next();
            Vector nv = new Vector();
            for (int i = 0; i < v.size(); i++) {
                Concept_Acc acc = (Concept_Acc) v.get(i);
                if (acc.getFor_concept().isVisible()) {
                    Concept for_concept = n.getConcept(acc.getFor_concept().getId_int());
                    Concept_Acc nacc = new Concept_Acc();
                    nacc.setAmbiguous(acc.isAmbiguous());
                    nacc.setConcept_accession(acc.getConcept_accession());
                    nacc.setElement_of(acc.getElement_of());
                    nacc.setFor_concept(for_concept);
                    for_concept.getConcept_accs().add(nacc);
                    nv.add(nacc);
                }
            }
            if (nv.size() > 0) naccs.put(((Concept_Acc) nv.get(0)).getConcept_accession(), nv);
        }
        ggdn.setConcept_accs(naccs);
        ggdn.setConcept_classes(ggdo.getConcept_classes());
        ggdn.setCvs(ggdo.getCvs());
        ggdn.setFormat_types(ggdo.getFormat_types());
        ggdn.setMapping_methods(ggdo.getMapping_methods());
        ggdn.setProperty_types(ggdo.getProperty_types());
        ggdn.setRelation_types(ggdo.getRelation_types());
        ggdn.setSequence_types(ggdo.getSequence_types());
        n.setGraphData(ggdn);
        Int2ObjectOpenHashMap rs = o.getRelationIDs();
        Int2ObjectOpenHashMap nrs = new Int2ObjectOpenHashMap(rs.size(), 0.85f);
        it = rs.values().iterator();
        while (it.hasNext()) {
            Relation r = (Relation) it.next();
            if (r.isVisible() && r.getFrom_concept().isVisible() && r.getTo_concept().isVisible()) {
                Relation nr = new Relation();
                nr.setColor(r.getColor());
                nr.setEvidence(r.getEvidence());
                nr.setFrom_concept(n.getConcept(r.getFrom_concept().getId_int()));
                nr.setFrom_element_of(r.getFrom_element_of());
                nr.setFrom_name(r.getFrom_name());
                nr.setId_int(r.getId_int());
                nr.setLabel(r.getLabel());
                nr.setOf_type(r.getOf_type());
                nr.setProperties(r.getProperties());
                nr.setTo_concept(n.getConcept(r.getTo_concept().getId_int()));
                nr.setTo_element_of(r.getTo_element_of());
                nr.setTo_name(r.getTo_name());
                nr.setVisible(r.isVisible());
                nr.getFrom_concept().getOutgoing_relations().add(nr);
                nr.getTo_concept().getIncoming_relations().add(nr);
                nrs.put(nr.getId_int(), nr);
            }
        }
        n.setRelationIDs(nrs);
        return n;
    }
}

package net.sourceforge.circuitsmith.cam;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sourceforge.circuitsmith.layers.EdaLayer;
import net.sourceforge.circuitsmith.layers.EdaLayerList;
import net.sourceforge.circuitsmith.panes.EdaDrawingPane;

/**
 * @author Kenneth MacCallum
 *
 */
public class CamFileWriter {

    private EdaDrawingPane pane;

    private CamOutput camOutput;

    private File file;

    public CamFileWriter(File f, EdaDrawingPane pane, CamOutput camOutput) {
        this.pane = pane;
        this.camOutput = camOutput;
        this.file = f;
    }

    public void generateGerberFiles() {
        if (camOutput.getZipped() == CamOutput.Zipped.NOT_ZIPPED) {
            System.out.println("GerberGenerator.generateGerberFiles can't do non-zipped gerbers yet");
        }
        for (CamOutputTableItem coti : camOutput.getCamFiles()) {
            CamFile camFile = null;
            if (coti instanceof CamFile) {
                camFile = (CamFile) coti;
                EdaLayerList list = new EdaLayerList();
                list.add(pane.getEdaLayerList().getEdaLayer("Background"));
                for (String layer : camFile.getLayerNames()) {
                    EdaLayer l = pane.getEdaLayerList().getEdaLayer(layer);
                    if (l != null) {
                        list.add(l);
                    } else {
                        System.out.println("GerberGenerator.generageGerberFiles: can't find layer \"" + l + "\"");
                    }
                }
                if (camFile instanceof GerberFile) {
                    CamGraphics gg = new GerberGraphics(pane, (GerberFile) camFile);
                    AffineTransform at = AffineTransform.getScaleInstance(CamGraphics.TEMP_SCALE, CamGraphics.TEMP_SCALE);
                    pane.getDrawing().draw(gg, at, list, EdaDrawingPane.LayerDrawingMode.NORMAL);
                } else if (camFile instanceof DrillFile) {
                    CamGraphics gg = new DrillGraphics(pane, (DrillFile) camFile);
                    AffineTransform at = AffineTransform.getScaleInstance(CamGraphics.TEMP_SCALE, CamGraphics.TEMP_SCALE);
                    pane.getDrawing().draw(gg, at, list, EdaDrawingPane.LayerDrawingMode.NORMAL);
                }
            }
        }
    }

    public void writeGerberFiles() {
        boolean zipped = false;
        File f;
        File subDir;
        if (camOutput.getZipped() == CamOutput.Zipped.ZIPPED) {
            zipped = true;
        }
        try {
            if (zipped) {
                f = file;
                f.createNewFile();
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f));
                zos.setMethod(ZipOutputStream.DEFLATED);
                zos.setLevel(Deflater.DEFAULT_COMPRESSION);
                OutputStreamWriter osw;
                osw = new OutputStreamWriter(zos);
                for (CamOutputTableItem coti : camOutput.getCamFiles()) {
                    if (coti instanceof CamFile) {
                        CamFile cf = (CamFile) coti;
                        zos.putNextEntry(new ZipEntry(cf.getFileName()));
                        cf.writeCamFile(osw);
                        osw.flush();
                        zos.closeEntry();
                        zos.closeEntry();
                    } else if (coti instanceof ReadmeFile) {
                        ReadmeFile rf = (ReadmeFile) coti;
                        zos.putNextEntry(new ZipEntry(rf.getFileName()));
                        osw.write(rf.getText());
                        osw.flush();
                        zos.closeEntry();
                    }
                }
                zos.close();
            } else {
                subDir = file;
                subDir.mkdir();
                for (CamOutputTableItem coti : camOutput.getCamFiles()) {
                    f = new File(subDir, coti.getFileName());
                    f.createNewFile();
                    Writer w = new FileWriter(f);
                    if (coti instanceof CamFile) {
                        CamFile cf = (CamFile) coti;
                        cf.writeCamFile(w);
                    } else if (coti instanceof ReadmeFile) {
                        ReadmeFile rf = (ReadmeFile) coti;
                        w.write(rf.getText());
                    }
                    w.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

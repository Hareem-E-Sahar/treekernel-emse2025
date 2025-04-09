package org.weasis.dicom.explorer;

import java.awt.Desktop;
import java.io.File;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.weasis.core.api.gui.util.AbstractProperties;
import org.weasis.core.api.media.MimeInspector;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.editor.MimeSystemAppViewer;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.dicom.codec.DicomEncapDocSeries;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomVideoSeries;
import org.weasis.dicom.codec.FileExtractor;

public class MimeSystemAppFactory implements SeriesViewerFactory {

    public static final String NAME = "default system application";

    public static final Icon ICON = new ImageIcon(MimeInspector.class.getResource("/icon/16x16/apps-system.png"));

    public static final MimeSystemAppViewer mimeSystemViewer = new MimeSystemAppViewer() {

        @Override
        public String getPluginName() {
            return Messages.getString("MimeSystemAppViewer.app");
        }

        @Override
        public void addSeries(MediaSeries series) {
            if (series instanceof DicomVideoSeries || series instanceof DicomEncapDocSeries) {
                if (AbstractProperties.OPERATING_SYSTEM.startsWith("linux")) {
                    FileExtractor extractor = (FileExtractor) series;
                    startAssociatedProgramFromLinux(extractor.getExtractFile());
                } else if (AbstractProperties.OPERATING_SYSTEM.startsWith("win")) {
                    FileExtractor extractor = (FileExtractor) series;
                    File file = extractor.getExtractFile();
                    startAssociatedProgramFromWinCMD(file.getAbsolutePath());
                } else if (Desktop.isDesktopSupported()) {
                    final Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        FileExtractor extractor = (FileExtractor) series;
                        startAssociatedProgramFromDesktop(desktop, extractor.getExtractFile());
                    }
                }
            }
        }
    };

    public MimeSystemAppFactory() {
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean canReadMimeType(String mimeType) {
        return DicomMediaIO.SERIES_VIDEO_MIMETYPE.equals(mimeType) || DicomMediaIO.SERIES_ENCAP_DOC_MIMETYPE.equals(mimeType);
    }

    @Override
    public boolean isViewerCreatedByThisFactory(SeriesViewer viewer) {
        return false;
    }

    @Override
    public SeriesViewer createSeriesViewer(Hashtable<String, Object> properties) {
        return mimeSystemViewer;
    }

    @Override
    public int getLevel() {
        return 100;
    }
}

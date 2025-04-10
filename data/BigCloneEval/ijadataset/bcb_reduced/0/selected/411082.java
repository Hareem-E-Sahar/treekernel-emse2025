package org.weasis.dicom.explorer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AbstractProperties;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.util.WtoolBar;
import org.weasis.dicom.codec.DicomEncapDocSeries;
import org.weasis.dicom.codec.DicomVideoSeries;
import org.weasis.dicom.codec.FileExtractor;

public class MimeSystemAppViewer implements SeriesViewer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MimeSystemAppViewer.class);

    @Override
    public String getPluginName() {
        return Messages.getString("MimeSystemAppViewer.app");
    }

    @Override
    public void close() {
    }

    @Override
    public List<MediaSeries> getOpenSeries() {
        return null;
    }

    @Override
    public void addSeries(MediaSeries series) {
        if (series instanceof DicomVideoSeries || series instanceof DicomEncapDocSeries) {
            if (AbstractProperties.OPERATING_SYSTEM.startsWith("linux")) {
                FileExtractor extractor = (FileExtractor) series;
                File file = extractor.getExtractFile();
                if (file != null) {
                    try {
                        String cmd = String.format("xdg-open %s", file.getAbsolutePath());
                        Runtime.getRuntime().exec(cmd);
                    } catch (IOException e1) {
                        LOGGER.error("Cannot open {} with the default system application", file.getName());
                    }
                }
            } else if (AbstractProperties.OPERATING_SYSTEM.startsWith("win")) {
                FileExtractor extractor = (FileExtractor) series;
                File file = extractor.getExtractFile();
                startAssociatedProgram(file.getAbsolutePath());
            } else if (Desktop.isDesktopSupported()) {
                final Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    FileExtractor extractor = (FileExtractor) series;
                    File file = extractor.getExtractFile();
                    if (file != null) {
                        try {
                            desktop.open(file);
                        } catch (IOException e1) {
                            LOGGER.error("Cannot open {} with the default system application", file.getName());
                        }
                    }
                }
            }
        }
    }

    public static void startAssociatedProgram(String file) {
        try {
            Runtime.getRuntime().exec("cmd /c \"" + file + '"');
        } catch (IOException e) {
            LOGGER.error("Cannot open {} with the default system application", file);
            e.printStackTrace();
        }
    }

    @Override
    public void removeSeries(MediaSeries sequence) {
    }

    @Override
    public JMenu fillSelectedPluginMenu(JMenu menu) {
        return null;
    }

    @Override
    public WtoolBar[] getToolBar() {
        return null;
    }

    @Override
    public WtoolBar getStatusBar() {
        return null;
    }

    @Override
    public PluginTool[] getToolPanel() {
        return null;
    }

    @Override
    public void setSelected(boolean selected) {
    }

    @Override
    public MediaSeriesGroup getGroupID() {
        return null;
    }
}

package ca.sqlpower.architect.swingui.olap.action;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import ca.sqlpower.architect.olap.MondrianXMLWriter;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.action.ProgressAction;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.MonitorableImpl;

public class ExportSchemaAction extends ProgressAction {

    private static final Logger logger = Logger.getLogger(ExportSchemaAction.class);

    private static final String FILE_KEY = "FILE_KEY";

    private ArchitectSwingSession session;

    private Schema schema;

    private RecentMenu recent;

    public ExportSchemaAction(ArchitectSwingSession session, Schema schema) {
        super(session, "Export Schema...", "Export Schema to xml", OSUtils.SCHEMA_EXPORT_ICON);
        this.session = session;
        this.schema = schema;
        this.recent = session.getRecentMenu();
    }

    @Override
    public void cleanUp(MonitorableImpl monitor) {
    }

    @Override
    public void doStuff(MonitorableImpl monitor, Map<String, Object> properties) {
        File f = (File) properties.get(FILE_KEY);
        try {
            MondrianXMLWriter.exportXML(f, schema);
        } catch (IOException e) {
            logger.error("Failed to save " + f.getName() + " for schema: " + schema);
            ASUtils.showExceptionDialog(session, "Could not save xml schema file.", e);
        }
    }

    @Override
    public String getButtonText() {
        return "Run in Background";
    }

    @Override
    public String getDialogMessage() {
        return "Creating XML";
    }

    @Override
    public boolean setup(MonitorableImpl monitor, Map<String, Object> properties) {
        monitor.setStarted(true);
        JFileChooser chooser = new JFileChooser(recent.getMostRecentFile());
        chooser.addChoosableFileFilter(SPSUtils.XML_FILE_FILTER);
        File file = null;
        while (true) {
            int response = chooser.showSaveDialog(session.getArchitectFrame());
            if (response != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            file = chooser.getSelectedFile();
            String fileName = file.getName();
            if (!fileName.endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            if (file.exists()) {
                response = JOptionPane.showConfirmDialog(null, "The file " + file.getPath() + " already exists. Do you want to overwrite it?", "File Exists", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    break;
                }
            } else {
                break;
            }
        }
        logger.debug("Saving to file: " + file.getName() + "(" + file.getPath() + ")");
        recent.putRecentFileName(file.getAbsolutePath());
        properties.put(FILE_KEY, file);
        return true;
    }
}

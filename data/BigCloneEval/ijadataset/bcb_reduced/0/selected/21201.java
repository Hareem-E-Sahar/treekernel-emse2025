package net.sf.amemailchecker.command.impl.app;

import net.sf.amemailchecker.app.ApplicationContext;
import net.sf.amemailchecker.app.exception.ExceptionCode;
import net.sf.amemailchecker.app.model.Settings;
import net.sf.amemailchecker.command.ICommand;
import net.sf.amemailchecker.command.exception.CommandExecutionException;
import net.sf.amemailchecker.command.impl.BaseStatusCommand;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportSettingsCommand extends BaseStatusCommand<Void> implements ICommand<Void> {

    private static final Logger logger = Logger.getLogger(ExportSettingsCommand.class.getName());

    private File file;

    public ExportSettingsCommand(File file) {
        this.file = file;
    }

    @Override
    public Void execute() throws CommandExecutionException {
        try {
            File outputFile = new File(file, "AmeMailCheckerSettings-" + System.currentTimeMillis() + ".zip");
            int BUFFER = 2048;
            BufferedInputStream origin;
            FileOutputStream destination = new FileOutputStream(outputFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(destination));
            out.setLevel(ZipOutputStream.STORED);
            byte data[] = new byte[BUFFER];
            String[] paths = new String[3];
            Settings settings = ApplicationContext.getInstance().getSettings();
            paths[0] = settings.getUserDataFilePath();
            paths[1] = settings.getUserKeyFilePath();
            paths[2] = settings.getUserPreferencesFilePath();
            for (int i = 0; i < paths.length; i++) {
                File file = new File(paths[i]);
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(file.getName());
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new CommandExecutionException(ExceptionCode.UNABLE_SAVE_FILE, e.getMessage(), e);
        }
        return null;
    }
}

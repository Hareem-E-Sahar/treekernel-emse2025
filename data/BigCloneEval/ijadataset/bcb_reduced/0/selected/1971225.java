package com.leclercb.taskunifier.gui.components.export_data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.leclercb.taskunifier.api.models.ContactFactory;
import com.leclercb.taskunifier.api.models.ContextFactory;
import com.leclercb.taskunifier.api.models.FolderFactory;
import com.leclercb.taskunifier.api.models.GoalFactory;
import com.leclercb.taskunifier.api.models.LocationFactory;
import com.leclercb.taskunifier.api.models.NoteFactory;
import com.leclercb.taskunifier.api.models.TaskFactory;
import com.leclercb.taskunifier.gui.translations.Translations;

public class ExportModelsDialog extends AbstractExportDialog {

    private static ExportModelsDialog INSTANCE;

    public static ExportModelsDialog getInstance() {
        if (INSTANCE == null) INSTANCE = new ExportModelsDialog();
        return INSTANCE;
    }

    private ExportModelsDialog() {
        super(Translations.getString("action.export_models"), "zip", Translations.getString("general.zip_files"), "export.models.file_name");
    }

    @Override
    protected void exportToFile(String file) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ContactFactory.getInstance().encodeToXML(output);
            this.writeIntoZip(zos, "contacts.xml", new ByteArrayInputStream(output.toByteArray()));
        }
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ContextFactory.getInstance().encodeToXML(output);
            this.writeIntoZip(zos, "contexts.xml", new ByteArrayInputStream(output.toByteArray()));
        }
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            FolderFactory.getInstance().encodeToXML(output);
            this.writeIntoZip(zos, "folders.xml", new ByteArrayInputStream(output.toByteArray()));
        }
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            GoalFactory.getInstance().encodeToXML(output);
            this.writeIntoZip(zos, "goals.xml", new ByteArrayInputStream(output.toByteArray()));
        }
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            LocationFactory.getInstance().encodeToXML(output);
            this.writeIntoZip(zos, "locations.xml", new ByteArrayInputStream(output.toByteArray()));
        }
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NoteFactory.getInstance().encodeToXML(output);
            this.writeIntoZip(zos, "notes.xml", new ByteArrayInputStream(output.toByteArray()));
        }
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            TaskFactory.getInstance().encodeToXML(output);
            this.writeIntoZip(zos, "tasks.xml", new ByteArrayInputStream(output.toByteArray()));
        }
        zos.close();
    }

    private void writeIntoZip(ZipOutputStream output, String name, InputStream input) throws Exception {
        output.putNextEntry(new ZipEntry(name));
        int size = 0;
        byte[] buffer = new byte[1024];
        while ((size = input.read(buffer, 0, buffer.length)) > 0) {
            output.write(buffer, 0, size);
        }
        output.closeEntry();
        input.close();
    }
}

package br.com.gonow.gtt.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import br.com.gonow.gtt.model.File;
import br.com.gonow.gtt.model.Language;
import br.com.gonow.gtt.model.Project;
import br.com.gonow.gtt.persistence.EntryPersistence;
import br.com.gonow.gtt.persistence.FilePersistence;
import br.com.gonow.gtt.persistence.LanguagePersistence;
import br.com.gonow.gtt.persistence.TranslationPersistence;
import br.com.gonow.gtt.service.DownloadService;
import br.com.gonow.gtt.service.ProjectDao;
import br.com.gonow.gtt.service.TranslationToolService;

@Repository
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private TranslationToolService translationToolService;

    public void dowloadFile(final File file, final String locale, final OutputStream outputStream) throws IOException {
        final LanguagePersistence pLanguage = projectDao.getLanguageByLocale(locale);
        final Long fileId = file.getId();
        final List<EntryPersistence> list = projectDao.getEntriesByFile(fileId);
        writeString(outputStream, "# File " + file.getFilename() + " generated for " + (("".equals(locale)) ? "default" : locale) + " locale.\n");
        writeString(outputStream, "# using GWT Translation Tool (http://translation.gonow.com.br/)\n");
        writeString(outputStream, "\n");
        for (final EntryPersistence fileEntryPersistence : list) {
            processEntry(fileEntryPersistence, pLanguage, outputStream);
        }
    }

    @Override
    public void dowloadProjectFile(final Project project, final OutputStream outputStream) throws IOException {
        final Long projectId = project.getId();
        final List<FilePersistence> list = projectDao.getFilesByProject(projectId);
        final ZipOutputStream zos = new ZipOutputStream(outputStream);
        try {
            final List<Language> languages = translationToolService.getLanguagesByProject(project);
            for (final Language language : languages) {
                for (final FilePersistence pFile : list) {
                    final String[] s = pFile.getFilename().split("[.]");
                    final StringBuilder filename = new StringBuilder();
                    for (int i = 0; i < s.length; i++) {
                        if (i == 0) {
                            filename.append(s[i]);
                        } else if (i == s.length - 1) {
                            filename.append("_").append(language.getLocale()).append(".").append(s[i]);
                        } else {
                            filename.append("/").append(s[i]);
                        }
                    }
                    final ZipEntry ze = new ZipEntry(filename.toString());
                    zos.putNextEntry(ze);
                    dowloadFile(pFile.toFile(), language.getLocale(), zos);
                    zos.closeEntry();
                }
            }
        } finally {
            zos.close();
        }
    }

    private void processEntry(final EntryPersistence entry, final LanguagePersistence language, final OutputStream outputStream) throws IOException {
        TranslationPersistence translation = null;
        if (language != null) {
            translation = projectDao.getTranslationByEntryAndLanguage(entry.getId(), language.getId());
        }
        if (entry.getDescription() != null) {
            writeString(outputStream, "# Description: " + entry.getDescription() + "\n");
        }
        if (entry.getMeaning() != null) {
            writeString(outputStream, "# Meaning: " + entry.getMeaning() + "\n");
        }
        if (entry.getParameters() != null) {
            writeString(outputStream, "# " + entry.getParameters() + "\n");
        }
        if (translation == null) {
            writeString(outputStream, entry.getKey() + "=" + entry.getContent() + "\n");
        } else {
            writeString(outputStream, entry.getKey() + "=" + translation.getTranslation() + "\n");
        }
        writeString(outputStream, "\n");
    }

    private void writeString(final OutputStream outputStream, final String string) throws IOException {
        outputStream.write(string.getBytes("UTF-8"));
    }
}

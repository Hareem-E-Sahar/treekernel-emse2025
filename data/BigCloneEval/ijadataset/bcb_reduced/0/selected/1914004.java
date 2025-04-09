package de.bwb.ekp.commons;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.faces.application.FacesMessage;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import de.bwb.ekp.entities.Ausschreibung;
import de.bwb.ekp.entities.Dokument;
import de.bwb.ekp.interceptors.MeasureCalls;

/**
 * 
 * 
 * @author Dorian Gloski Copyright akquinet AG, 2007
 */
@MeasureCalls
@Name("zipWriter")
@AutoCreate
public class ZipWriter {

    @In
    private FacesMessages facesMessages;

    public byte[] createDokumentenZipFile(final Ausschreibung ausschreibung) throws IOException {
        final List<ZipEintrag> zipList = new ArrayList<ZipEintrag>();
        final Set<Dokument> dokumente = ausschreibung.getAlleDokumente();
        zipList.add(this.generateInhaltsverzeichnis(ausschreibung, dokumente));
        for (Dokument dokument : dokumente) {
            zipList.add(new ZipEintrag(dokument.getDatenInByte(), dokument.getDokumentName()));
        }
        return this.createZipFile(zipList);
    }

    public ZipEintrag generateInhaltsverzeichnis(final Ausschreibung ausschreibung, final Collection<Dokument> dokumente) {
        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("datum", new Date());
        root.put("dokumente", dokumente);
        root.put("ausschreibung", ausschreibung);
        final String templateName = "dateienvergabeunterlagen.ftl";
        final byte[] inhalt = FreemarkerUtil.generate(root, templateName);
        return new ZipEintrag(inhalt, "dateienvergabeunterlagen.txt");
    }

    public byte[] createZipFile(final Collection<ZipEintrag> zipEintraege) throws IOException {
        final ByteArrayOutputStream dest = new ByteArrayOutputStream();
        final ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(dest));
        try {
            final List<String> dokumentNamen = new ArrayList<String>();
            for (final ZipEintrag zipEintrag : zipEintraege) {
                final String dokumentName = zipEintrag.getFilename();
                if (dokumentNamen.contains(dokumentName)) {
                    this.facesMessages.add(FacesMessage.SEVERITY_WARN, "Das Dokument " + dokumentName + " ist doppelt und wird nicht ber�cksichtigt");
                    continue;
                }
                final byte[] bytes = zipEintrag.getDaten();
                if (bytes == null) {
                    this.facesMessages.add(FacesMessage.SEVERITY_WARN, "Das Dokument " + dokumentName + " ist leer und wird nicht ber�cksichtigt");
                    continue;
                }
                dokumentNamen.add(dokumentName);
                final ZipEntry entry = new ZipEntry(dokumentName);
                zipStream.putNextEntry(entry);
                zipStream.write(bytes, 0, bytes.length);
            }
        } finally {
            zipStream.close();
            dest.close();
        }
        return dest.toByteArray();
    }
}

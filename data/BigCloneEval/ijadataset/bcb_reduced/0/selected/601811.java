package org.ziggurat.fenix.common.modelo.cilindros;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author manuel
 */
public class Exporter {

    private String path;

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    /** Creates a new instance of Exporter */
    public Exporter(String path) {
        this.path = path;
    }

    /**
     * Hace la exportacion a un archivo txt con el formato requerido
     * por ENARGAS. El archivo lo escribe en el path especificado en
     * el constructor de la clase.
     * El archivo generado tiene extension TXT.
     * 
     * @param tecnicos
     * @param revisiones
     * @param postFix Para definir un postfijo luego del nombre del archivo, por ejemplo se puede agregar la fecha.
     * 
     */
    public void exportToENARGASTxt(List tecnicos, List revisiones, String postFix) {
        try {
            String fileName = System.getProperty("java.io.tmpdir") + File.separator + "REVTMP.TXT";
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            for (Iterator ir = revisiones.iterator(); ir.hasNext(); ) {
                Revision rev = (Revision) ir.next();
                out.write(this.getENARGASString(rev));
                out.write("\r\n");
            }
            out.close();
            byte[] buf = new byte[1024];
            FileInputStream in = new FileInputStream(fileName);
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(path + File.separator + "REV" + postFix + ".zip"));
            zip.putNextEntry(new ZipEntry("REV.TXT"));
            int len;
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
            zip.closeEntry();
            in.close();
            zip.close();
        } catch (IOException e) {
            System.out.println("Error al crear el archivo, exportacion.");
        }
    }

    /**
     * Genera un string apropiado para la exportacion a ENARGAS
     * de una revision.
     * @param revision
     * @return String con la revision de acuerdo a la especificacion de ENARGAS
     */
    private String getENARGASString(Revision revision) {
        String retVal = new String();
        retVal += StringUtils.substring(revision.getCodigoCRPC(), 0, 4).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getMatricula(), 0, 10).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getCodigoPEC(), 0, 4).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getCodigoTaller(), 0, 7).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getCuitTaller(), 0, 13).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getApeYnomUsuario(), 0, 35).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getTipoDoc(), 0, 4).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getNroDoc(), 0, 13).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getDomicilio(), 0, 50).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getLocalidad(), 0, 35).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getProvincia(), 0, 30).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getCp(), 0, 4).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getTelefono(), 0, 10).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getDominio(), 0, 10).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getCodigoHomologado(), 0, 4).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getNumeroSerie(), 0, 25).toUpperCase();
        retVal += ";";
        retVal += String.valueOf(revision.getMesFabricacion().intValue() + 1);
        retVal += ";";
        retVal += StringUtils.substring(String.valueOf(revision.getAnoFabricacion().intValue()), 2, 4);
        retVal += ";";
        retVal += StringUtils.substring(revision.getMaterial(), 0, 15).toUpperCase();
        retVal += ";";
        retVal += StringUtils.substring(revision.getCapacidad(), 0, 4).toUpperCase();
        retVal += ";";
        retVal += revision.getNumeroRevision();
        retVal += ";";
        retVal += revision.getResultadoRevision();
        retVal += ";";
        if (revision.isGlobos()) ;
        retVal += "1";
        retVal += ";";
        if (revision.isAbolladuras()) retVal += "1";
        retVal += ";";
        if (revision.isAboEstrias()) retVal += "1";
        retVal += ";";
        if (revision.isFisuras()) retVal += "1";
        retVal += ";";
        if (revision.isLaminado()) retVal += "1";
        retVal += ";";
        if (revision.isPinchaduras()) retVal += "1";
        retVal += ";";
        if (revision.isRosca()) retVal += "1";
        retVal += ";";
        if (revision.isDesgasteLocalizado()) retVal += "1";
        retVal += ";";
        if (revision.isCorrosion()) retVal += "1";
        retVal += ";";
        if (revision.isOvalado()) retVal += "1";
        retVal += ";";
        if (revision.isDeformacionMarcado()) retVal += "1";
        retVal += ";";
        if (revision.isExpansionVolu()) retVal += "1";
        retVal += ";";
        if (revision.isPerdidaMasa()) retVal += "1";
        retVal += ";";
        if (revision.isFuegoCalor()) retVal += "1";
        retVal += ";";
        retVal += StringUtils.upperCase(StringUtils.substring(StringUtils.replace(revision.getOtrosDefectos(), "\n", " "), 0, 67));
        retVal += ";";
        retVal += "";
        retVal += ";";
        retVal += formatter.format(revision.getFechaRevision());
        retVal += ";";
        retVal += formatter.format(revision.getFechaVencimiento());
        retVal += ";";
        retVal += StringUtils.substring(revision.getTipoOperacion(), 0, 1).toUpperCase();
        retVal += ";";
        retVal += formatter.format(revision.getFechaModificacion());
        retVal += ";";
        retVal += formatter.format(new Date());
        return retVal;
    }
}

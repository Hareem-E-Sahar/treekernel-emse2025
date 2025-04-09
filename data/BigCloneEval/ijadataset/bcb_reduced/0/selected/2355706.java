package net.sf.ideoreport.reportgenerator.helpers;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.sf.ideoreport.api.datastructure.containers.data.DataContainer;
import net.sf.ideoreport.reportgenerator.ReportGenerator;
import net.sf.ideoreport.reportgenerator.config.IReportConfig;
import net.sf.ideoreport.reportgenerator.exception.ReportFactoryException;

/**
 * Classe utilitaire d�di�e � la g�n�ration de rapports.
 *
 * @author jroyer (Last modification by $Author$ on $Date$)
 * @version $Revision$
 */
public final class ReportFactoryUtils {

    /** Logger for this class. */
    private static final Log logger = LogFactory.getLog(ReportFactoryUtils.class);

    /**
	 * Constructor par d�faut priv�.
	 */
    private ReportFactoryUtils() {
    }

    /**
	 * G�n�re un rapport ou un ensemble de rapport et envoie le r�sultat sur un flux de sortie, � partir de donn�es r�cup�r�es par la Report Factory.
	 * @param pOut le flux de sortie sur lequel doit �tre envoy� le r�sultat
	 * @param pReportFactory le g�n�rateur de rapport utilis�
	 * @param pReportParams la param�tres du rapport
	 * @throws ReportFactoryException si une erreuir survient lors de la g�n�ration du ou des rapports
	 */
    public static void writeReportsToOutputStream(OutputStream pOut, ReportGenerator pReportFactory, ReportFactoryParameters pReportParams) throws ReportFactoryException {
        writeReportsToOutputStream(pOut, pReportFactory, pReportParams, null);
    }

    /**
	 * G�n�re un rapport ou un ensemble de rapport et envoie le r�sultat sur un flux de sortie, � partir de donn�es stock�es dans un Data Container.
	 * @param pOut le flux de sortie sur lequel doit �tre envoy� le r�sultat
	 * @param pReportFactory le g�n�rateur de rapport utilis�
	 * @param pReportParams la param�tres du rapport
	 * @param pDataContainer le container de donn�es utilis� pour la g�n�ration du rapport, ou <code>null</code> pour laisser la Report Factory acc�der aux donn�es par elle-m�me
	 * @throws ReportFactoryException si une erreuir survient lors de la g�n�ration du ou des rapports
	 */
    public static void writeReportsToOutputStream(OutputStream pOut, ReportGenerator pReportFactory, ReportFactoryParameters pReportParams, DataContainer pDataContainer) throws ReportFactoryException {
        try {
            OutputStream vOut = new BufferedOutputStream(pOut);
            if (pReportParams.isZip()) {
                vOut = new ZipOutputStream(pOut);
            }
            for (int vIndex = 0; vIndex < pReportParams.getReportIDs().length; vIndex++) {
                String vIdReport = pReportParams.getReportIDs()[vIndex];
                IReportConfig vReportConfig = pReportFactory.getConfiguration().getReport(vIdReport);
                if (vReportConfig == null) {
                    String vConfigurationName = pReportParams.getConfigurationName();
                    throw new ReportFactoryException("Unable to find configuration for report [" + vIdReport + "] in configuration [" + vConfigurationName + "]");
                }
                String vFilename = pReportParams.getFilenames()[vIndex];
                if (vFilename == null || vFilename.equals("report-" + vIdReport)) {
                    vFilename = StringUtils.defaultString(vReportConfig.getDefaultFileName(), vIdReport);
                    if (pReportParams.isDateSuffixe()) {
                        StringBuffer vNewFilename = new StringBuffer();
                        vNewFilename.append(vFilename.substring(0, vFilename.lastIndexOf(".")));
                        vNewFilename.append("_").append((new Date()).getTime());
                        vNewFilename.append(vFilename.lastIndexOf("."));
                        vFilename = vNewFilename.toString();
                    }
                }
                if (pReportParams.isZip()) {
                    ZipEntry vZipEntry = new ZipEntry(vFilename);
                    ((ZipOutputStream) vOut).putNextEntry(vZipEntry);
                }
                if (pDataContainer != null) {
                    pReportFactory.process(vIdReport, pDataContainer, pReportParams.getParameterValues(), vOut);
                } else {
                    pReportFactory.process(vIdReport, pReportParams.getParameterValues(), vOut);
                }
                if (pReportParams.isZip()) {
                    ((ZipOutputStream) vOut).closeEntry();
                    if (logger.isDebugEnabled()) {
                        logger.debug("closing zip entry [" + vFilename + "]...");
                    }
                }
            }
            if (pReportParams.isZip()) {
                ((ZipOutputStream) vOut).finish();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReportFactoryException(e.getMessage(), e);
        }
    }
}

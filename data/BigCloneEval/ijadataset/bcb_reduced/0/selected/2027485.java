package org.gvsig.remotesensing.principalcomponents;

import org.gvsig.fmap.raster.layers.FLyrRasterSE;
import org.gvsig.raster.RasterProcess;
import org.gvsig.raster.buffer.BufferFactory;
import org.gvsig.raster.buffer.RasterBuffer;
import org.gvsig.raster.buffer.RasterBufferInvalidException;
import org.gvsig.raster.dataset.IRasterDataSource;
import org.gvsig.raster.grid.Grid;
import org.gvsig.raster.grid.GridException;
import org.gvsig.raster.util.RasterToolsUtil;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import com.iver.andami.PluginServices;

/**
 *PCStatisticsProcess es la clase que implementa el proceso c�lculo de estad�sticas avanzado
 *para el an�lisis de componentes principales. Se calcula la matriz de varianza-covarianza, 
 *los atovalores y autovectrores.
 *
 *@parms
 *<LI>FLyrRasterSE "inputRasterLayer": Capa raster de entrada</LI>
 *<LI>boolean[] "selectedBands": Bandas del raster original que se tienen en cuenta para la transformaci�n</LI>
 *
 *@result
 *<LI>PCStatistics: Estad�sticas reusltantes del ACP.</LI>
 *
 *@author Alejandro Mu�oz Sanchez (alejandro.munoz@uclm.es)
 *@author Diego Guerrero Sevilla (diego.guerrero@uclm.es)
 *@version 19/10/2007 
 */
public class PCStatisticsProcess extends RasterProcess {

    private Grid inputGrid = null;

    private double autovalors[] = null;

    private Matrix coVarMatrix = null;

    private Matrix autoVectorMatrix = null;

    private int percent = 0;

    private boolean cancel = false;

    private boolean selectedBands[] = null;

    private FLyrRasterSE inputRasterLayer = null;

    private PCStatistics reusltStatistics = null;

    /**
	 * Constructor
	 */
    public PCStatisticsProcess() {
    }

    /**
	 * @return array con los autovalores
	 */
    public Object getResult() {
        if (reusltStatistics == null) reusltStatistics = new PCStatistics(autoVectorMatrix, autovalors, coVarMatrix);
        return reusltStatistics;
    }

    public double[] getAutoValors() {
        return autovalors;
    }

    /**
	 * @return Matriz de autovectores
	 */
    public Matrix getAutoVectorMatrix() {
        return autoVectorMatrix;
    }

    /**
	 * @return Matriz varianza-covarianza
	 */
    public Matrix getcoVarMatrix() {
        return coVarMatrix;
    }

    /**
	 * C�lculo de la matriz varianza covarianza de las bandas de un Grid. 
	 * @throws InterruptedException 
	 */
    private double[][] covarianceOptimize() throws InterruptedException {
        double dSum = 0;
        int iValues = 0;
        buildGrid();
        double[][] coV = new double[inputGrid.getRasterBuf().getBandCount()][inputGrid.getRasterBuf().getBandCount()];
        double cancelMatrix[][] = new double[][] { { 0 } };
        double valorBandai = 0, valorBandaj = 0;
        int bandCount = inputGrid.getRasterBuf().getBandCount();
        if (inputGrid.getRasterBuf().getDataType() == RasterBuffer.TYPE_BYTE) {
            for (int i = 0; i < bandCount; i++) {
                for (int j = i; j < bandCount; j++) {
                    if (cancel) return cancelMatrix;
                    iValues = 0;
                    dSum = 0;
                    for (int k = 0; k < inputGrid.getNX(); k++) {
                        for (int l = 0; l < inputGrid.getNY(); l++) {
                            try {
                                inputGrid.setBandToOperate(i);
                                valorBandai = inputGrid.getCellValueAsByte(k, l) - inputGrid.getMeanValue();
                                inputGrid.setBandToOperate(j);
                                valorBandaj = inputGrid.getCellValueAsByte(k, l) - inputGrid.getMeanValue();
                            } catch (GridException e) {
                                RasterToolsUtil.messageBoxError(PluginServices.getText(this, "grid_error"), this, e);
                            }
                            dSum += valorBandai * valorBandaj;
                            iValues++;
                        }
                    }
                    if (iValues > 1) coV[i][j] = dSum / (double) (iValues); else coV[i][j] = inputGrid.getNoDataValue();
                }
                if (bandCount > 1) percent = (i + 1) * 100 / (bandCount - 1); else percent = (i + 1) * 100 / (1);
            }
        }
        if (inputGrid.getRasterBuf().getDataType() == RasterBuffer.TYPE_SHORT) {
            for (int i = 0; i < bandCount; i++) {
                for (int j = i; j < bandCount; j++) {
                    if (cancel) return cancelMatrix;
                    iValues = 0;
                    dSum = 0;
                    for (int k = 0; k < inputGrid.getNX(); k++) {
                        for (int l = 0; l < inputGrid.getNY(); l++) {
                            try {
                                inputGrid.setBandToOperate(i);
                                valorBandai = inputGrid.getCellValueAsShort(k, l) - inputGrid.getMeanValue();
                                inputGrid.setBandToOperate(j);
                                valorBandaj = inputGrid.getCellValueAsShort(k, l) - inputGrid.getMeanValue();
                            } catch (GridException e) {
                                RasterToolsUtil.messageBoxError(PluginServices.getText(this, "grid_error"), this, e);
                            }
                            dSum += valorBandai * valorBandaj;
                            iValues++;
                        }
                    }
                    if (iValues > 1) coV[i][j] = dSum / (double) (iValues); else coV[i][j] = inputGrid.getNoDataValue();
                }
                if (bandCount > 1) percent = (i + 1) * 100 / (bandCount - 1); else percent = (i + 1) * 100 / (1);
            }
        }
        if (inputGrid.getRasterBuf().getDataType() == RasterBuffer.TYPE_INT) {
            for (int i = 0; i < bandCount; i++) {
                for (int j = i; j < bandCount; j++) {
                    if (cancel) return cancelMatrix;
                    iValues = 0;
                    dSum = 0;
                    for (int k = 0; k < inputGrid.getNX(); k++) {
                        for (int l = 0; l < inputGrid.getNY(); l++) {
                            try {
                                inputGrid.setBandToOperate(i);
                                valorBandai = inputGrid.getCellValueAsInt(k, l) - inputGrid.getMeanValue();
                                inputGrid.setBandToOperate(j);
                                valorBandaj = inputGrid.getCellValueAsInt(k, l) - inputGrid.getMeanValue();
                            } catch (GridException e) {
                                RasterToolsUtil.messageBoxError(PluginServices.getText(this, "grid_error"), this, e);
                            }
                            dSum += valorBandai * valorBandaj;
                            iValues++;
                        }
                    }
                    if (iValues > 1) coV[i][j] = dSum / (double) (iValues); else coV[i][j] = inputGrid.getNoDataValue();
                }
                if (bandCount > 1) percent = (i + 1) * 100 / (bandCount - 1); else percent = (i + 1) * 100 / (1);
            }
        }
        if (inputGrid.getRasterBuf().getDataType() == RasterBuffer.TYPE_FLOAT) {
            for (int i = 0; i < bandCount; i++) {
                for (int j = i; j < bandCount; j++) {
                    if (cancel) return cancelMatrix;
                    iValues = 0;
                    dSum = 0;
                    for (int k = 0; k < inputGrid.getNX(); k++) {
                        for (int l = 0; l < inputGrid.getNY(); l++) {
                            try {
                                inputGrid.setBandToOperate(i);
                                valorBandai = inputGrid.getCellValueAsFloat(k, l) - inputGrid.getMeanValue();
                                inputGrid.setBandToOperate(j);
                                valorBandaj = inputGrid.getCellValueAsFloat(k, l) - inputGrid.getMeanValue();
                            } catch (GridException e) {
                                RasterToolsUtil.messageBoxError(PluginServices.getText(this, "grid_error"), this, e);
                            }
                            dSum += valorBandai * valorBandaj;
                            iValues++;
                        }
                    }
                    if (iValues > 1) coV[i][j] = dSum / (double) (iValues); else coV[i][j] = inputGrid.getNoDataValue();
                }
                if (bandCount > 1) percent = (i + 1) * 100 / (bandCount - 1); else percent = (i + 1) * 100 / (1);
            }
        }
        if (inputGrid.getRasterBuf().getDataType() == RasterBuffer.TYPE_DOUBLE) {
            for (int i = 0; i < bandCount; i++) {
                for (int j = i; j < bandCount; j++) {
                    if (cancel) return cancelMatrix;
                    iValues = 0;
                    dSum = 0;
                    for (int k = 0; k < inputGrid.getNX(); k++) {
                        for (int l = 0; l < inputGrid.getNY(); l++) {
                            try {
                                inputGrid.setBandToOperate(i);
                                valorBandai = inputGrid.getCellValueAsDouble(k, l) - inputGrid.getMeanValue();
                                inputGrid.setBandToOperate(j);
                                valorBandaj = inputGrid.getCellValueAsDouble(k, l) - inputGrid.getMeanValue();
                            } catch (GridException e) {
                                RasterToolsUtil.messageBoxError(PluginServices.getText(this, "grid_error"), this, e);
                            }
                            dSum += valorBandai * valorBandaj;
                            iValues++;
                        }
                    }
                    if (iValues > 1) coV[i][j] = dSum / (double) (iValues); else coV[i][j] = inputGrid.getNoDataValue();
                }
                if (bandCount > 1) percent = (i + 1) * 100 / (bandCount - 1); else percent = (i + 1) * 100 / (1);
            }
        }
        for (int i = 0; i < bandCount; i++) {
            for (int j = 0; j < bandCount; j++) {
                if (j < i) coV[i][j] = coV[j][i];
            }
        }
        return coV;
    }

    /**
	 * Construye el grid con las bandas seleccionadas
	 */
    private void buildGrid() {
        IRasterDataSource dsetCopy = null;
        dsetCopy = inputRasterLayer.getDataSource().newDataset();
        BufferFactory bufferFactory = new BufferFactory(dsetCopy);
        if (!RasterBuffer.loadInMemory(dsetCopy)) bufferFactory.setReadOnly(true);
        int longitud = 0;
        for (int i = 0; i < selectedBands.length; i++) if (selectedBands[i]) longitud++;
        int bands[] = new int[longitud];
        int j = 0;
        for (int i = 0; i < selectedBands.length; i++) if (selectedBands[i]) {
            bands[j] = i;
            j++;
        }
        try {
            inputGrid = new Grid(bufferFactory, bands);
        } catch (RasterBufferInvalidException e) {
            e.printStackTrace();
        }
    }

    public String getLabel() {
        return PluginServices.getText(this, "procesando");
    }

    public String getLog() {
        return PluginServices.getText(this, "calculando_estadisticas") + "...";
    }

    public int getPercent() {
        return percent;
    }

    public String getTitle() {
        return PluginServices.getText(this, "principal_components");
    }

    /**
	 * @return grid 
	 */
    public Grid getInputGrid() {
        return inputGrid;
    }

    /**
	 * @return raster de entrada
	 * */
    public FLyrRasterSE getRasterLayer() {
        return inputRasterLayer;
    }

    public void init() {
        selectedBands = (boolean[]) getParam("selectedBands");
        inputRasterLayer = getLayerParam("inputRasterLayer");
    }

    /**
	 *  Proceso de calculo de estadisticas para Principal Component
	 * */
    public void process() throws InterruptedException {
        double coVar[][] = covarianceOptimize();
        coVarMatrix = new Matrix(coVar);
        EigenvalueDecomposition eigenvalueDecomp = new EigenvalueDecomposition(coVarMatrix);
        autoVectorMatrix = eigenvalueDecomp.getV();
        autovalors = eigenvalueDecomp.getRealEigenvalues();
        if (!cancel) if (incrementableTask != null) incrementableTask.processFinalize();
        if (externalActions != null) externalActions.end(getResult());
    }

    public void interrupted() {
    }
}

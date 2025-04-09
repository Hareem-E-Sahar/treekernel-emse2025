package com.iver.cit.gvsig.fmap.rendering;

import java.util.ArrayList;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.values.DoubleValue;
import com.hardcode.gdbms.engine.values.FloatValue;
import com.hardcode.gdbms.engine.values.IntValue;
import com.hardcode.gdbms.engine.values.LongValue;
import com.hardcode.gdbms.engine.values.NullValue;
import com.hardcode.gdbms.engine.values.ShortValue;
import com.hardcode.gdbms.engine.values.Value;

/**
 * Calcula los intervalos naturales.
 *
 * @author Vicente Caballero Navarro
 */
public class NaturalIntervalGenerator {

    protected DataSource ds;

    protected String msFieldName;

    protected int miNumIntervalosSolicitados;

    protected int miNumIntervalosGenerados;

    protected double[] mdaValoresRuptura;

    protected double[] mdaValInit;

    /**
	 * Crea un nuevo IntervalGenerator.
	 *
	 * @param layer AlphanumericData
	 * @param field Nombre del campo.
	 * @param numIntervals N�mero de intervalos.
	 */
    public NaturalIntervalGenerator(DataSource recordSet, String field, int numIntervals) {
        ds = recordSet;
        msFieldName = field;
        miNumIntervalosSolicitados = numIntervals;
    }

    /**
	 * Esta funci�n busca en el vector de datos la posici�n que le corresponde
	 * al valor almacenado en vdValor y devuelve dicha posici�n en
	 * vdValorAInsertar. Para hallar la posici�n se realiza una b�squeda
	 * binaria. Si se trata de un elemento que ya est� en el vector devolvemos
	 * el �ndice que le corresponde en rlIndiceCorrespondiente y false en
	 * rbNuevoElemento. Si se trata de un nuevo elemento que hay que
	 * insertar... devolvemos el �ndice en el que ir�a y True en
	 * rbNuevoElemento En caso de que ocurra alg�n error devuelve false
	 *
	 * @param rVectorDatos ArrayList con los datos.
	 * @param vdValorAInsertar Valor a insertar.
	 * @param rlIndiceCorrespondiente �ndice.
	 * @param rbNuevoElemento True si es un nuevo elemento.
	 *
	 * @return True si ha conseguido correctamente la posici�n en el vector.
	 */
    protected boolean mbObtenerPosicionEnVector(ArrayList rVectorDatos, double vdValorAInsertar, int[] rlIndiceCorrespondiente, boolean[] rbNuevoElemento) {
        int llIndiceIzq;
        int llIndiceDer;
        int llMedio;
        double ldValorComparacion;
        rbNuevoElemento[0] = false;
        rlIndiceCorrespondiente[0] = -1;
        if (rVectorDatos.size() == 1) {
            if (((udtDatosEstudio) rVectorDatos.get(0)).Coincidencias == 0) {
                rlIndiceCorrespondiente[0] = 0;
                rbNuevoElemento[0] = false;
                return true;
            }
        }
        llIndiceIzq = 0;
        llIndiceDer = rVectorDatos.size() - 1;
        llMedio = (llIndiceIzq + llIndiceDer) / 2;
        while (llIndiceIzq <= llIndiceDer) {
            ldValorComparacion = ((udtDatosEstudio) rVectorDatos.get(llMedio)).Valor;
            if (vdValorAInsertar > ldValorComparacion) {
                llIndiceIzq = llMedio + 1;
                llMedio = (llIndiceIzq + llIndiceDer) / 2;
            } else if (vdValorAInsertar < ldValorComparacion) {
                llIndiceDer = llMedio - 1;
                llMedio = (llIndiceIzq + llIndiceDer) / 2;
            } else if (vdValorAInsertar == ldValorComparacion) {
                rlIndiceCorrespondiente[0] = llMedio;
                rbNuevoElemento[0] = false;
                return true;
            }
        }
        rbNuevoElemento[0] = true;
        ldValorComparacion = ((udtDatosEstudio) rVectorDatos.get(llMedio)).Valor;
        if (vdValorAInsertar > ldValorComparacion) {
            rlIndiceCorrespondiente[0] = llMedio + 1;
        } else {
            rlIndiceCorrespondiente[0] = llMedio;
        }
        return true;
    }

    /**
	 * M�todo para generar los intervalos.
	 *
	 * @return true si se han generado correctamente.
	 *
	 * @throws com.iver.cit.gvsig.fmap.DriverException
	 * @throws DriverException
	 */
    public boolean generarIntervalos() throws ReadDriverException {
        ArrayList lVectorDatos;
        double[] ldMediaTotal = new double[1];
        double[] ldSDAM = new double[1];
        int[] llaIndicesRupturas;
        double[] ldUltimaGVF = new double[1];
        double[] ldNuevaGVF = new double[1];
        int i;
        int liNumClasesReales;
        int llNumElementosPorClase;
        lVectorDatos = new ArrayList();
        lVectorDatos.add(new udtDatosEstudio());
        if (!mbObtenerDatos(lVectorDatos, ldMediaTotal)) {
            return false;
        }
        System.out.println("Analizando datos ...");
        ldSDAM[0] = mbGetSumSquaredDeviationArrayMean(lVectorDatos, ldMediaTotal[0]);
        if (lVectorDatos.isEmpty()) {
            mdaValoresRuptura[0] = ((udtDatosEstudio) lVectorDatos.get(0)).Valor;
            mdaValInit[0] = ((udtDatosEstudio) lVectorDatos.get(0)).Valor;
            miNumIntervalosGenerados = 2;
            return true;
        }
        if (miNumIntervalosSolicitados > (lVectorDatos.size())) {
            liNumClasesReales = lVectorDatos.size() + 1;
        } else {
            liNumClasesReales = miNumIntervalosSolicitados;
        }
        llaIndicesRupturas = new int[liNumClasesReales - 1];
        llNumElementosPorClase = (lVectorDatos.size()) / liNumClasesReales;
        for (i = 0; i < llaIndicesRupturas.length; i++) {
            if (i == 0) {
                llaIndicesRupturas[i] = llNumElementosPorClase - 1;
            } else {
                llaIndicesRupturas[i] = llaIndicesRupturas[i - 1] + llNumElementosPorClase;
            }
        }
        udtDatosClase[] ldaSDCM_Parciales = new udtDatosClase[llaIndicesRupturas.length + 1];
        udtDatosClase[] ldaSDCM_Validos = new udtDatosClase[llaIndicesRupturas.length + 1];
        if (llaIndicesRupturas.length == 0) {
            return true;
        }
        if (!mbCalcularGVF(lVectorDatos, ldaSDCM_Parciales, llaIndicesRupturas, ldSDAM[0], ldUltimaGVF, -1, false)) {
            miNumIntervalosSolicitados = lVectorDatos.size();
            generarIntervalos();
            return false;
        }
        ldaSDCM_Validos = getArray(ldaSDCM_Parciales);
        boolean lbMoverADerecha;
        boolean lbMoverAIzquierda;
        boolean lbIntentarDesplazamiento;
        int llIndiceRupturaOriginal;
        long k;
        double ldGVFentrepasadas;
        ldGVFentrepasadas = ldUltimaGVF[0];
        for (k = 1; k <= 100; k++) {
            for (i = 0; i < (llaIndicesRupturas.length); i++) {
                lbMoverADerecha = false;
                lbMoverAIzquierda = false;
                llIndiceRupturaOriginal = llaIndicesRupturas[i];
                ldaSDCM_Validos = getArray(ldaSDCM_Parciales);
                lbIntentarDesplazamiento = false;
                if (i == (llaIndicesRupturas.length - 1)) {
                    if ((llaIndicesRupturas[i] + 1) < lVectorDatos.size()) {
                        lbIntentarDesplazamiento = true;
                    }
                } else {
                    if ((llaIndicesRupturas[i] + 1) < llaIndicesRupturas[i + 1]) {
                        lbIntentarDesplazamiento = true;
                    }
                }
                if (lbIntentarDesplazamiento) {
                    llaIndicesRupturas[i] = llaIndicesRupturas[i] + 1;
                    if (!mbCalcularGVF(lVectorDatos, ldaSDCM_Parciales, llaIndicesRupturas, ldSDAM[0], ldNuevaGVF, i, false)) {
                        return false;
                    }
                    if (ldNuevaGVF[0] > ldUltimaGVF[0]) {
                        lbMoverADerecha = true;
                        ldaSDCM_Validos = getArray(ldaSDCM_Parciales);
                    } else {
                        llaIndicesRupturas[i] = llIndiceRupturaOriginal;
                        ldaSDCM_Parciales = getArray(ldaSDCM_Validos);
                    }
                }
                lbIntentarDesplazamiento = false;
                if (!lbMoverADerecha) {
                    if (i == 0) {
                        if ((llaIndicesRupturas[i] - 1) >= 0) {
                            lbIntentarDesplazamiento = true;
                        }
                    } else {
                        if ((llaIndicesRupturas[i] - 1) > llaIndicesRupturas[i - 1]) {
                            lbIntentarDesplazamiento = true;
                        }
                    }
                }
                if (lbIntentarDesplazamiento) {
                    llaIndicesRupturas[i] = llaIndicesRupturas[i] - 1;
                    if (!mbCalcularGVF(lVectorDatos, ldaSDCM_Parciales, llaIndicesRupturas, ldSDAM[0], ldNuevaGVF, i, true)) {
                        return false;
                    }
                    if (ldNuevaGVF[0] > ldUltimaGVF[0]) {
                        lbMoverAIzquierda = true;
                        ldaSDCM_Validos = getArray(ldaSDCM_Parciales);
                    } else {
                        llaIndicesRupturas[i] = llIndiceRupturaOriginal;
                        ldaSDCM_Parciales = getArray(ldaSDCM_Validos);
                    }
                }
                lbIntentarDesplazamiento = false;
                if (lbMoverAIzquierda || lbMoverADerecha) {
                    ldUltimaGVF[0] = ldNuevaGVF[0];
                    boolean exit = false;
                    while (!exit) {
                        llIndiceRupturaOriginal = llaIndicesRupturas[i];
                        if (lbMoverADerecha) {
                            if (i == (llaIndicesRupturas.length - 1)) {
                                if ((llaIndicesRupturas[i] + 1) >= lVectorDatos.size()) {
                                    exit = true;
                                }
                            } else {
                                if ((llaIndicesRupturas[i] + 1) >= llaIndicesRupturas[i + 1]) {
                                    exit = true;
                                }
                            }
                            llaIndicesRupturas[i] = llaIndicesRupturas[i] + 1;
                        } else {
                            if (i == 0) {
                                if ((llaIndicesRupturas[i] - 1) < 0) {
                                    exit = true;
                                }
                            } else {
                                if ((llaIndicesRupturas[i] - 1) <= llaIndicesRupturas[i - 1]) {
                                    exit = true;
                                }
                            }
                            llaIndicesRupturas[i] = llaIndicesRupturas[i] - 1;
                        }
                        if (!mbCalcularGVF(lVectorDatos, ldaSDCM_Parciales, llaIndicesRupturas, ldSDAM[0], ldNuevaGVF, i, lbMoverAIzquierda)) {
                            return false;
                        }
                        if (ldNuevaGVF[0] < ldUltimaGVF[0]) {
                            llaIndicesRupturas[i] = llIndiceRupturaOriginal;
                            ldaSDCM_Parciales = getArray(ldaSDCM_Validos);
                            exit = true;
                        } else {
                            ldUltimaGVF[0] = ldNuevaGVF[0];
                            ldaSDCM_Validos = getArray(ldaSDCM_Parciales);
                        }
                    }
                }
            }
            if (ldUltimaGVF[0] <= ldGVFentrepasadas) {
                i = 101;
            }
            ldGVFentrepasadas = ldUltimaGVF[0];
        }
        mdaValoresRuptura = new double[llaIndicesRupturas.length];
        mdaValInit = new double[llaIndicesRupturas.length];
        for (i = 0; i < mdaValoresRuptura.length; i++) {
            if (llaIndicesRupturas[i] == -1) llaIndicesRupturas[i] = 1;
            if (llaIndicesRupturas[i] > lVectorDatos.size() - 1) llaIndicesRupturas[i] = lVectorDatos.size() - 1;
            mdaValoresRuptura[i] = ((udtDatosEstudio) lVectorDatos.get(llaIndicesRupturas[i])).Valor;
            if ((llaIndicesRupturas[i] + 1) < lVectorDatos.size()) {
                mdaValInit[i] = ((udtDatosEstudio) lVectorDatos.get(llaIndicesRupturas[i] + 1)).Valor;
            } else {
                mdaValInit[i] = ((udtDatosEstudio) lVectorDatos.get(llaIndicesRupturas[i])).Valor;
            }
        }
        miNumIntervalosGenerados = mdaValoresRuptura.length + 2;
        ldaSDCM_Validos = null;
        ldaSDCM_Parciales = null;
        lVectorDatos = null;
        return true;
    }

    /**
	 * DOCUMENT ME!
	 *
	 * @param array DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
    private udtDatosClase[] getArray(udtDatosClase[] array) {
        udtDatosClase[] aux = new udtDatosClase[array.length];
        for (int i = 0; i < array.length; i++) {
            aux[i] = new udtDatosClase();
            aux[i].Media = array[i].Media;
            aux[i].NumElementos = array[i].NumElementos;
            aux[i].SDCM = array[i].SDCM;
            aux[i].SumaCuadradoTotal = array[i].SumaCuadradoTotal;
            aux[i].SumaTotal = array[i].SumaTotal;
        }
        return aux;
    }

    /**
	 * Devuelve la "SDAM" de un conjunto de datos que vienen almacenados en el
	 * vector rVectorDatos
	 *
	 * @param rVectorDatos Datos
	 * @param vdMedia Media
	 *
	 * @return suma de las desviaciones t�picas del total de los datos respecto
	 * 		   de la media total
	 */
    private double mbGetSumSquaredDeviationArrayMean(ArrayList rVectorDatos, double vdMedia) {
        int i;
        double rdSDAM = 0;
        for (i = 0; i < rVectorDatos.size(); i++) {
            rdSDAM = rdSDAM + (Math.pow((((udtDatosEstudio) rVectorDatos.get(i)).Valor - vdMedia), 2) * ((udtDatosEstudio) rVectorDatos.get(i)).Coincidencias);
        }
        return rdSDAM;
    }

    /**
	 * Esta funci�n obtiene los datos en los que queremos hallar las rupturas
	 * naturales. Los datos se devuelven ordenados en un vector. Tambi�n
	 * devuelve la media total
	 *
	 * @param rVectorDatos Datos
	 * @param rdMediaTotal Media total
	 *
	 * @return True si se ha calculado correctamente.
	 *
	 * @throws com.iver.cit.gvsig.fmap.DriverException
	 * @throws DriverException
	 */
    protected boolean mbObtenerDatos(ArrayList rVectorDatos, double[] rdMediaTotal) throws ReadDriverException {
        double ldValor;
        int i;
        long llRecordCount;
        int[] llIndice = new int[1];
        boolean[] lbNuevoElemento = new boolean[1];
        llRecordCount = ds.getRowCount();
        if (!gbExisteCampoEnRegistro(ds, msFieldName)) {
            if (msFieldName == "") {
                System.out.println("No se ha establecido el nombre del campo origen!");
            } else {
                System.out.println("El campo '" + msFieldName + "' no pertence a la capa!");
            }
            return false;
        }
        for (i = 0; i < llRecordCount; i++) {
            try {
                ldValor = getValue(ds.getFieldValue(i, ds.getFieldIndexByName(msFieldName)));
            } catch (Exception e) {
                llRecordCount--;
                continue;
            }
            rdMediaTotal[0] = rdMediaTotal[0] + ldValor;
            if (!mbObtenerPosicionEnVector(rVectorDatos, ldValor, llIndice, lbNuevoElemento)) {
                return false;
            }
            if (!lbNuevoElemento[0]) {
                if ((llIndice[0] < 0) || (llIndice[0] > rVectorDatos.size())) {
                    System.out.println("�ndice incorrecto!");
                    return false;
                }
                ((udtDatosEstudio) rVectorDatos.get(llIndice[0])).Valor = ldValor;
                ((udtDatosEstudio) rVectorDatos.get(llIndice[0])).Coincidencias = ((udtDatosEstudio) rVectorDatos.get(llIndice[0])).Coincidencias + 1;
            } else {
                udtDatosEstudio udt = new udtDatosEstudio();
                udt.Valor = ldValor;
                udt.Coincidencias = 1;
                rVectorDatos.add(llIndice[0], udt);
            }
        }
        rdMediaTotal[0] = rdMediaTotal[0] / llRecordCount;
        return true;
    }

    /**
	 * Devuelve true si existe el campo en el registro.
	 *
	 * @param recordset Recordset.
	 * @param msFieldName2 Nombre del campo.
	 *
	 * @return True si existe el campo.
	 * @throws ReadDriverException
	 *
	 */
    private boolean gbExisteCampoEnRegistro(DataSource recordset, String msFieldName2) throws ReadDriverException {
        if (recordset.getFieldIndexByName(msFieldName2) == -1) {
            return false;
        }
        return true;
    }

    /**
	 * Esta funci�n s�lo calcula las SDCM de las clases adyacentes al �ndice de
	 * ruptura actual. Si el �ndice de ruptura actual es -1 entonces las
	 * calcula todas! . En este caso no importa el valor de
	 * vbDesplazAIzquierda
	 *
	 * @param rVectorDatos Datos
	 * @param raClases Calses que representan a los intervalos.
	 * @param rlaIndicesRuptura �ndices de ruptura.
	 * @param vdSDAM Desviaci�n standard.
	 * @param rdGVF Desviaci�n standard de los intervalos.
	 * @param vlIndiceRupturaActual �ndice de ruptura actual.
	 * @param vbDesplazAIzquierda Desplazamiento a la izquierda.
	 *
	 * @return True si se ha calculado correctamente.
	 */
    private boolean mbCalcularGVF(ArrayList rVectorDatos, udtDatosClase[] raClases, int[] rlaIndicesRuptura, double vdSDAM, double[] rdGVF, int vlIndiceRupturaActual, boolean vbDesplazAIzquierda) {
        double ldSDCM_aux;
        int i;
        if (vlIndiceRupturaActual == -1) {
            for (i = 0; i < rlaIndicesRuptura.length; i++) {
                if (i == 0) {
                    if (!mbGetDatosClase(rVectorDatos, 0, rlaIndicesRuptura[i], raClases, i)) {
                        return false;
                    }
                } else {
                    if (!mbGetDatosClase(rVectorDatos, rlaIndicesRuptura[i - 1] + 1, rlaIndicesRuptura[i], raClases, i)) {
                        return false;
                    }
                }
            }
            if (!mbGetDatosClase(rVectorDatos, rlaIndicesRuptura[rlaIndicesRuptura.length - 1] + 1, rVectorDatos.size() - 1, raClases, raClases.length - 1)) {
                return false;
            }
        } else {
            i = vlIndiceRupturaActual;
            if (vbDesplazAIzquierda) {
                if (!mbRecalcularDatosClase(raClases, i, rVectorDatos, rlaIndicesRuptura[i] + 1, vdSDAM, false)) {
                    return false;
                }
                if (!mbRecalcularDatosClase(raClases, i + 1, rVectorDatos, rlaIndicesRuptura[i] + 1, vdSDAM, true)) {
                    return false;
                }
            } else {
                if (!mbRecalcularDatosClase(raClases, i, rVectorDatos, rlaIndicesRuptura[i], vdSDAM, true)) {
                    return false;
                }
                if (!mbRecalcularDatosClase(raClases, i + 1, rVectorDatos, rlaIndicesRuptura[i], vdSDAM, false)) {
                    return false;
                }
            }
        }
        ldSDCM_aux = 0;
        for (i = 0; i < raClases.length; i++) {
            ldSDCM_aux = ldSDCM_aux + raClases[i].SDCM;
        }
        rdGVF[0] = (vdSDAM - ldSDCM_aux) / vdSDAM;
        return true;
    }

    /**
	 * Devuelve la "SDCM" de un conjunto de datos que vienen almacenados en el
	 * vector rVectorDatos y que est�n delimitados por vlLimiteInf y
	 * vlLimiteInf
	 *
	 * @param rVectorDatos Datos
	 * @param vlLimiteInf L�mite inferior.
	 * @param vlLimiteSup L�mite superior.
	 * @param rClase Calses que representan a los intervalos.
	 * @param numClas N�mero de calses.
	 *
	 * @return True si se ha calculado correctamente.
	 */
    private boolean mbGetDatosClase(ArrayList rVectorDatos, int vlLimiteInf, int vlLimiteSup, udtDatosClase[] rClase, int numClas) {
        int i;
        if (vlLimiteInf < 0) {
            return false;
        }
        if (vlLimiteSup > rVectorDatos.size()) {
            return false;
        }
        if (vlLimiteSup < vlLimiteInf) {
            return false;
        }
        rClase[numClas] = new udtDatosClase();
        for (i = vlLimiteInf; i < (vlLimiteSup + 1); i++) {
            rClase[numClas].NumElementos = rClase[numClas].NumElementos + ((udtDatosEstudio) rVectorDatos.get(i)).Coincidencias;
            rClase[numClas].SumaTotal = rClase[numClas].SumaTotal + (((udtDatosEstudio) rVectorDatos.get(i)).Valor * ((udtDatosEstudio) rVectorDatos.get(i)).Coincidencias);
            rClase[numClas].SumaCuadradoTotal = rClase[numClas].SumaCuadradoTotal + (Math.pow(((udtDatosEstudio) rVectorDatos.get(i)).Valor * ((udtDatosEstudio) rVectorDatos.get(i)).Coincidencias, 2));
        }
        rClase[numClas].Media = rClase[numClas].SumaTotal / rClase[numClas].NumElementos;
        rClase[numClas].SDCM = (rClase[numClas].SumaCuadradoTotal) - (2 * rClase[numClas].Media * rClase[numClas].SumaTotal) + (rClase[numClas].NumElementos * Math.pow(rClase[numClas].Media, 2));
        return true;
    }

    /**
	 * Recalcula los datos de las clases.
	 *
	 * @param rClase Clases.
	 * @param i �adir �adir �adir �adir indica si a la clase se le a�ade un
	 * 		  elemento (True) o se le quita (False)
	 * @param rVectorDatos Datos.
	 * @param vlIndiceElemento es el �ndice del elemento que se le va a�adir o
	 * 		  a quitar a la clase
	 * @param vdSDAM desviaci�n standard.
	 * @param vbA �adir DOCUMENT ME!
	 *
	 * @return True si se ha calculado correctamente.
	 */
    private boolean mbRecalcularDatosClase(udtDatosClase[] rClase, int i, ArrayList rVectorDatos, int vlIndiceElemento, double vdSDAM, boolean vbAnyadir) {
        double ldValor;
        long llNumCoincidencias;
        try {
            if (vlIndiceElemento > rVectorDatos.size() - 1) return true;
            ldValor = ((udtDatosEstudio) rVectorDatos.get(vlIndiceElemento)).Valor;
            llNumCoincidencias = ((udtDatosEstudio) rVectorDatos.get(vlIndiceElemento)).Coincidencias;
            if (vbAnyadir) {
                rClase[i].SumaTotal = rClase[i].SumaTotal + (ldValor * llNumCoincidencias);
                rClase[i].SumaCuadradoTotal = rClase[i].SumaCuadradoTotal + (Math.pow((ldValor * llNumCoincidencias), 2));
                rClase[i].NumElementos = rClase[i].NumElementos + llNumCoincidencias;
            } else {
                rClase[i].SumaTotal = rClase[i].SumaTotal - (ldValor * llNumCoincidencias);
                rClase[i].SumaCuadradoTotal = rClase[i].SumaCuadradoTotal - (Math.pow((ldValor * llNumCoincidencias), 2));
                rClase[i].NumElementos = rClase[i].NumElementos - llNumCoincidencias;
            }
            if (rClase[i].NumElementos <= 0) rClase[i].NumElementos = 1;
            rClase[i].Media = rClase[i].SumaTotal / rClase[i].NumElementos;
            rClase[i].SDCM = (rClase[i].SumaCuadradoTotal) - (2 * rClase[i].Media * rClase[i].SumaTotal) + (rClase[i].NumElementos * Math.pow(rClase[i].Media, 2));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
	 * Devuelve el valor de ruptura seg�n el �ndice que se le pasa como
	 * par�metro.
	 *
	 * @param viIndice �nidice del valor de ruptura.
	 *
	 * @return Valor de ruptura.
	 */
    public double getValorRuptura(int viIndice) {
        return mdaValoresRuptura[viIndice];
    }

    /**
	 * Devuelve el valor inicial de cada intervalo
	 *
	 * @param index �ndice del intervalo
	 *
	 * @return valor del intervalo.
	 */
    public double getValInit(int index) {
        return mdaValInit[index];
    }

    /**
	 * Devuelve el n�mero de intervalos que se pueden representar, no tiene
	 * porque coincidir con el n�mero de intervalos que se piden.
	 *
	 * @return N�mero de intervalos calculados.
	 */
    public int getNumIntervals() {
        return miNumIntervalosGenerados;
    }

    /**
	 * Clase para contener los atributos Valor y coincidencias.
	 *
	 * @author Vicente Caballero Navarro
	 */
    public class udtDatosEstudio {

        public double Valor;

        public long Coincidencias;
    }

    /**
	 * Clase para contener los atributos: N�mero de Elementos. Media.
	 * SumaTotal. SumaCuadradoTotal. Desviaci�n standard.
	 *
	 * @author Vicente Caballero Navarro
	 */
    public class udtDatosClase {

        public long NumElementos;

        public double Media;

        public double SumaTotal;

        public double SumaCuadradoTotal;

        public double SDCM;
    }

    /**
	 * Devuelve el valor en un double del Value que se pasa como par�metro.
	 *
	 * @param value Value.
	 *
	 * @return valor.
	 * @throws Exception
	 */
    protected double getValue(Value value) throws Exception {
        if (value instanceof IntValue) {
            return ((IntValue) value).intValue();
        } else if (value instanceof LongValue) {
            return ((LongValue) value).longValue();
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).floatValue();
        } else if (value instanceof ShortValue) {
            return ((ShortValue) value).shortValue();
        } else if (value instanceof NullValue) {
            throw new Exception();
        }
        return ((DoubleValue) value).doubleValue();
    }
}

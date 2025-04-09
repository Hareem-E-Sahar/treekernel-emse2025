package EGC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import kmin_kmax.*;

/**
 * @author lgms
 *
 */
public class DataPointNetwork {

    private int ncol, nrow;

    private DataPoint[] Net;

    private double[][] Distances;

    private DataPointMetric DPM;

    private useless MM;

    private int K;

    private double threshold = -1;

    private double averagePayoff = -1;

    public boolean Load(String fileName) {
        ArrayList<double[]> dataList = new ArrayList<double[]>();
        File file = new File(fileName);
        String[] tempTable;
        double[] tempList;
        int rows = 0;
        try {
            FileReader fr = new FileReader(file);
            BufferedReader input = new BufferedReader(fr);
            String line;
            System.out.println("Data from: \"" + fileName + "\" are importing...");
            int tableLength = 0;
            while ((line = input.readLine()) != null) {
                rows++;
                tempTable = line.split("\t");
                tableLength = tempTable.length;
                tempList = new double[tableLength];
                String tempText = "Linha " + rows + ": ";
                for (int i = 0; i < tableLength; i++) {
                    tempList[i] = Double.valueOf(tempTable[i]);
                    if (i > 0) {
                        tempText = tempText + "," + Double.valueOf(tempTable[i]);
                    } else {
                        tempText = tempText + Double.valueOf(tempTable[i]);
                    }
                }
                dataList.add(tempList);
            }
            this.ncol = tableLength;
            this.nrow = rows;
            fr.close();
            System.out.println(rows + " rows was imported");
            Net = new DataPoint[nrow];
            DataPoint tmp;
            for (int i = 0; i < nrow; i++) {
                Net[i] = new DataPoint(dataList.get(i), i);
            }
            return true;
        } catch (IOException e) {
            System.out.println("File can not be read!. Error: " + e);
            return false;
        }
    }

    public boolean InitializeDistances() {
        Distances = new double[nrow][nrow];
        for (int i = 0; i < nrow; i++) {
            for (int j = 0; j < i; j++) {
                Distances[i][j] = DPM.Dist(Net[i], Net[j], ncol);
                Distances[j][i] = Distances[i][j];
            }
            Distances[i][i] = DPM.Dist(Net[i], Net[i], ncol);
        }
        return true;
    }

    public double[] GetDistancesRow(int i) {
        double[] tmp = new double[nrow];
        for (int j = 0; j < nrow; j++) tmp[j] = Distances[i][j];
        return tmp;
    }

    public void InitializePreferencesEtDistances() {
        for (int i = 0; i < nrow; i++) {
            DataPoint[] tmp = Net[i].getNeighbourhood();
            for (int j = 0; j < tmp.length; j++) {
                Net[i].setConnectionPreference(tmp[j], 1 / (double) K);
                Net[i].setConnectionDistance(tmp[j], Distances[i][tmp[j].Index()]);
            }
        }
        if (threshold < 0) {
            double averagePayoff = 0;
            for (int i = 0; i < nrow; i++) averagePayoff += Net[i].Payoff();
            threshold = averagePayoff / (double) nrow;
            System.out.println("Using average payoff as threshold - " + threshold);
        }
    }

    public boolean InitializeGammaZero() {
        double[] tmp;
        int[] indices;
        for (int i = 0; i < nrow; i++) {
            tmp = GetDistancesRow(i);
            indices = MM.getIndMinK(tmp, K);
            for (int j = 0; j < K; j++) {
                Net[i].addOutConn(Net[indices[j]]);
            }
        }
        return true;
    }

    public DataPoint[] GammaPlus(DataPoint P) {
        DataPoint[] Hood = P.getNeighbourhood();
        ArrayList<DataPoint> tmp = new ArrayList<DataPoint>(0);
        for (int i = 0; i < Hood.length; i++) {
            if (Hood[i].Payoff() > threshold) tmp.add(Hood[i]);
        }
        if (tmp.size() > 0) {
            DataPoint[] RetHood = new DataPoint[tmp.size()];
            RetHood = tmp.toArray(RetHood);
            return RetHood;
        } else {
            return null;
        }
    }

    public DataPoint[] UniteHoods(DataPoint P) {
        DataPoint[] Hood = GammaPlus(P);
        if (Hood != null) {
            ArrayList<DataPoint> tmp = new ArrayList<DataPoint>(0);
            for (int i = 0; i < Hood.length; i++) {
                tmp.add(Hood[i]);
                tmp.addAll(Hood[i].getNeighbourhoodAsList());
            }
            ArrayList<DataPoint> TMP = new ArrayList<DataPoint>(0);
            for (int i = 0; i < tmp.size(); i++) {
                if (!TMP.contains(tmp.get(i))) {
                    TMP.add(tmp.get(i));
                }
            }
            DataPoint[] RetHood = new DataPoint[TMP.size()];
            RetHood = TMP.toArray(RetHood);
            return RetHood;
        } else {
            return null;
        }
    }

    public DataPoint[] Gamma(DataPoint P) {
        DataPoint[] Hood = UniteHoods(P);
        if (Hood != null) {
            double[] Pays = new double[Hood.length];
            for (int i = 0; i < Hood.length; i++) {
                Pays[i] = Hood[i].Payoff();
            }
            int[] Indices = this.MM.getIndMaxK(Pays, K);
            DataPoint[] RetHood = new DataPoint[K];
            for (int i = 0; i < K; i++) {
                RetHood[i] = Hood[Indices[i]];
            }
            return RetHood;
        } else {
            return null;
        }
    }

    public ArrayList<DataPoint> GammaAsList(DataPoint P) {
        DataPoint[] Hood = UniteHoods(P);
        if (Hood != null) {
            double[] Pays = new double[Hood.length];
            for (int i = 0; i < Hood.length; i++) {
                Pays[i] = Hood[i].Payoff();
            }
            int[] Indices = this.MM.getIndMaxK(Pays, K);
            ArrayList<DataPoint> RetHood = new ArrayList<DataPoint>(Indices.length);
            for (int i = 0; i < Indices.length; i++) {
                RetHood.add(Hood[Indices[i]]);
            }
            return RetHood;
        } else {
            return null;
        }
    }

    public int UpdateHood(DataPoint P) {
        ArrayList<DataPoint> NewHood = GammaAsList(P);
        if (NewHood != null) {
            DataPoint[] OldHood = P.getNeighbourhood();
            double sumPrefs = 0;
            int NumRem = 0;
            for (int i = 0; i < OldHood.length; i++) {
                if (!NewHood.contains(OldHood[i])) {
                    sumPrefs += P.getConnectionPreference(P);
                    P.remOutConn(P);
                    NumRem++;
                }
            }
            int NumNew = NewHood.size() - P.numOutConn();
            double NewPref = sumPrefs / (double) NumNew;
            for (int i = 0; i < NewHood.size(); i++) {
                if (P.isNeighbour(P) != -1) {
                    P.addOutConn(NewHood.get(i));
                    P.setConnectionPreference(NewHood.get(i), NewPref);
                }
            }
            return (NumRem + NumNew);
        } else {
            return -1;
        }
    }

    public int Update() {
        int j = 0;
        for (int i = 0; i < nrow; i++) {
            j = +UpdateHood(Net[i]);
        }
        return j;
    }

    public DataPointNetwork(DataPointMetric M, useless kmm, int k, double t) {
        DPM = M;
        MM = kmm;
        K = k;
        threshold = t;
    }

    public DataPointNetwork(DataPointMetric M, useless kmm, int k) {
        DPM = M;
        MM = kmm;
        K = k;
    }

    public void OutNetInfo() {
        for (int i = 0; i < nrow; i++) {
            double[] tmp = Net[i].getPosition();
            String tmp2 = "" + tmp[0];
            for (int j = 1; j < tmp.length; j++) tmp2 = tmp2 + ", " + tmp[j];
            System.out.println("Ponto " + i + ": " + tmp2);
            DataPoint[] tmp3 = Net[i].getNeighbourhood();
            if (tmp3.length > 0) {
                tmp2 = "[�ndice " + tmp3[0].Index() + ", Prefer�ncia " + Net[i].getConnectionPreference(tmp3[0]) + ", Dist�ncia " + Distances[i][tmp3[0].Index()] + "]";
                for (int j = 1; j < tmp3.length; j++) {
                    tmp2 = tmp2 + ", [�ndice " + tmp3[j].Index() + ", Prefer�ncia " + Net[i].getConnectionPreference(tmp3[j]) + ", Dist�ncia " + Distances[i][tmp3[j].Index()] + "]";
                }
                System.out.println("Conex�es: " + tmp2);
            } else {
                System.out.println("Isolado.");
            }
            System.out.println("Payoff: " + Net[i].Payoff());
        }
    }

    public ArrayList<ArrayList<DataPoint>> ListPathsWithoutRepeating() {
        ArrayList<ArrayList<DataPoint>> Paths = new ArrayList<ArrayList<DataPoint>>(0);
        ArrayList<DataPoint> ForbiddenArray = new ArrayList<DataPoint>(0);
        for (int i = 0; i < nrow; i++) {
            ArrayList<DataPoint> tmp = new ArrayList<DataPoint>(0);
            tmp.add(Net[i]);
            tmp.addAll(Net[i].getPathsStarted(ForbiddenArray));
            if (tmp.size() > 1) {
                System.out.println(i + "-th path.");
                for (int j = 0; j < tmp.size(); j++) {
                    System.out.println(tmp.get(j).Index());
                }
                Paths.add(i, tmp);
            } else if (ForbiddenArray.contains(Net[i])) {
                System.out.println("Already counted: " + i);
                Paths.add(i, new ArrayList<DataPoint>(0));
            } else {
                Paths.add(i, tmp);
                System.out.println("Isolated point: " + i);
            }
            ForbiddenArray.addAll(tmp);
        }
        return Paths;
    }

    public void OutClustersOut() {
        ArrayList<ArrayList<DataPoint>> Paths = ListPathsWithoutRepeating();
    }
}

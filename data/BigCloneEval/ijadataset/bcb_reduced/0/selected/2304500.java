package clustering.framework;

import java.util.*;
import java.io.*;

/**
 * @author Tudor.Ionescu@supelec.fr

DistanceMatrixComputer

The distance matrix computer class uses a compressor and a distance metric to compute an n x n distance matrix, where n represents the number of files to be clustered.

Constructor:

public DistanceMatrixComputer(ICompressor comp, IDistanceMetric dmetric);

The constructor requires a compressor object � comp � and a distance metric object � dmetric � which are used to compute the distance matrix.

Public method(s):

public double [][] ComputeMatrix(String [] filesList) throws Exception;

The filesList parameter denotes a list of the files to be clustered. The method returns an n x n double matrix (where n is the length of the list of files) which is computed using the compressor and the distance metric which were specified in the constructor. The method takes each file from the list, compresses it and then concatenates its original version with all other files and compresses the concatenated version. Then it uses the distance metric object to compute the distances between all the files, taken two by two.

 */
public class DistanceMatrixComputer {

    public double[][] fDistanceMatrix;

    boolean bComputed = false;

    ICompressor comp;

    IDistanceMetric dmetric;

    Hashtable htCSize;

    public DistanceMatrixComputer(ICompressor comp, IDistanceMetric dmetric) {
        this.comp = comp;
        this.dmetric = dmetric;
    }

    public double[][] ComputeMatrix(String[] filesList) throws Exception {
        int n = filesList.length;
        fDistanceMatrix = new double[n][n];
        htCSize = new Hashtable();
        Random rand = new Random(100000);
        ArrayList alTempFiles = new ArrayList();
        for (int i = 0; i < n; i++) {
            File f1 = new File(filesList[i]);
            FileInputStream fis = new FileInputStream(f1);
            byte[] file_data = new byte[(int) f1.length()];
            fis.read(file_data);
            fis.close();
            File fCompX = comp.Compress(f1);
            alTempFiles.add(fCompX);
            htCSize.put("c:" + i, fCompX);
            for (int j = i; j < n; j++) {
                File f2 = new File(filesList[j]);
                fis = new FileInputStream(f2);
                byte[] file_data2 = new byte[(int) f2.length()];
                fis.read(file_data2);
                fis.close();
                String two_files_name = rand.nextInt() + "_two_files.dat";
                FileOutputStream fos = new FileOutputStream(two_files_name);
                fos.write(file_data);
                fos.write(file_data2);
                fos.close();
                File two_files = new File(two_files_name);
                File fCompXY = comp.Compress(two_files);
                alTempFiles.add(fCompXY);
                htCSize.put("c:" + i + ":" + j, fCompXY);
                alTempFiles.add(two_files);
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                File c_x = (File) htCSize.get("c:" + i);
                File c_y = (File) htCSize.get("c:" + j);
                File c_xy = (File) htCSize.get("c:" + i + ":" + j);
                fDistanceMatrix[i][j] = dmetric.Compute(c_x, c_y, c_xy);
                fDistanceMatrix[j][i] = fDistanceMatrix[i][j];
            }
        }
        bComputed = true;
        for (int i = 0; i < alTempFiles.size(); i++) {
            File f = (File) alTempFiles.get(i);
            f.delete();
        }
        return fDistanceMatrix;
    }

    public boolean getComputed() {
        return bComputed;
    }
}

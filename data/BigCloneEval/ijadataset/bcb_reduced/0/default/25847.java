import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.StreamTokenizer;
import java.util.Vector;
import java.util.Random;

public class Idata {

    public int nChan;

    public int nPoints;

    int nMaxSubspace = 50;

    double[][] electrodeins;

    double insmax = 0.0F;

    double insfac = 1.0F;

    Random random = new Random();

    double[][] electrodesub;

    int[] subspacei;

    int nsubspacei = 0;

    int mdi;

    double[][] subspacescalars;

    int nv = 0;

    double sscamax = 0.0F;

    double sscafac = 1.0F;

    double[][] SVsqss;

    double[][] Vsqss;

    double[] Lf;

    double[][] elecsubins;

    double[][] elecdiff;

    double[] waveletsh;

    double[] waveletsh_i;

    double[] wchann;

    int wshcen = 0;

    int wshwidth = 0;

    double[] probe;

    double[] bestmatch;

    double[] npall;

    Idata(Iraw iraw, Ipos ipos) {
        nChan = iraw.dNChan;
        nPoints = iraw.dNPoints;
        InitArrays();
        ClearAllSignals();
        MakeSubspaceVex();
    }

    void ClearAllSignals() {
        for (int i = 0; i < nPoints; i++) for (int j = 0; j < nChan; j++) electrodeins[i][j] = 0.0;
        SetElectodeIntensitiesScale();
    }

    void BuildWavelet(double wavelength, double decay, int lnpoints) {
        wshcen = (lnpoints + 1) / 2;
        wshwidth = wshcen;
        double dsum = 0;
        for (int i = wshcen - wshwidth; i < wshcen + wshwidth + 1; i++) {
            double t = i - wshcen;
            double wval = t * Math.PI * 2 / wavelength;
            double cw = Math.cos(wval);
            double sw = Math.sin(wval);
            double tdec = t * decay;
            double dfac = Math.exp(-tdec * tdec);
            waveletsh[i] = dfac * cw;
            waveletsh_i[i] = dfac * sw;
            dsum += dfac;
        }
        if (dsum != 0.0) {
            for (int i = wshcen - wshwidth; i < wshcen + wshwidth + 1; i++) {
                waveletsh[i] /= dsum;
                waveletsh_i[i] /= dsum;
            }
        }
    }

    void ConvolveChannelWavelet(int j) {
        for (int i = 0; i < nPoints; i++) {
            double wr = 0.0;
            double wi = 0.0;
            boolean bTruncated = false;
            for (int k = -wshwidth; k < wshwidth + 1; k++) {
                int i0 = i + k;
                if ((i0 >= 0) && (i0 < nPoints)) {
                    wr += waveletsh[k + wshcen] * electrodeins[i0][j];
                    wi += waveletsh_i[k + wshcen] * electrodeins[i0][j];
                } else bTruncated = true;
            }
            wchann[i] = wr;
            if (bTruncated) wchann[i] = 0.0;
        }
        for (int i = 0; i < nPoints; i++) electrodeins[i][j] = wchann[i];
    }

    void ApplyWavelet(double wavewidth) {
        BuildWavelet(11.0, 0.1, 50);
        for (int j = 0; j < nChan; j++) ConvolveChannelWavelet(j);
        SetElectodeIntensitiesScale();
    }

    void CopyElectodeIntensities(double[][][] rdata, int t0, int t1) {
        double mini = 0.0F;
        double maxi = 0.0F;
        for (int t = t0; t < t1; t++) {
            for (int i = 0; i < nPoints; i++) for (int j = 0; j < nChan; j++) electrodeins[i][j] += rdata[t][i][j];
        }
        SetElectodeIntensitiesScale();
    }

    void InitArrays() {
        electrodeins = new double[nPoints][];
        subspacescalars = new double[nPoints][];
        elecsubins = new double[nPoints][];
        elecdiff = new double[nPoints][];
        for (int i = 0; i < nPoints; i++) {
            electrodeins[i] = new double[nChan];
            subspacescalars[i] = new double[nMaxSubspace];
            elecsubins[i] = new double[nChan];
            elecdiff[i] = new double[nChan];
        }
        electrodesub = new double[nMaxSubspace][];
        subspacei = new int[nMaxSubspace];
        SVsqss = new double[nMaxSubspace][];
        Vsqss = new double[nMaxSubspace][];
        Lf = new double[nMaxSubspace];
        for (int i = 0; i < nMaxSubspace; i++) {
            SVsqss[i] = new double[nMaxSubspace];
            Vsqss[i] = new double[nMaxSubspace];
            electrodesub[i] = new double[nChan];
        }
        waveletsh = new double[nPoints];
        waveletsh_i = new double[nPoints];
        wchann = new double[nPoints];
        probe = new double[nChan];
        bestmatch = new double[nChan];
        npall = new double[nChan];
        nsubspacei = 0;
        nv = 0;
    }

    void SetElectodeIntensitiesScale() {
        double mini = 0.0F;
        double maxi = 0.0F;
        for (int i = 0; i < nPoints; i++) for (int j = 0; j < nChan; j++) {
            double val = electrodeins[i][j];
            mini = Math.min(mini, val);
            maxi = Math.max(maxi, val);
        }
        System.out.println("Intensities with range " + mini + " to " + maxi);
        insmax = Math.max(-mini, maxi);
        insfac = (insfac != 0.0 ? 1.0 / insmax : 1.0);
    }

    void AddFakeSignals(Ipos ipos) {
        AddFakeSignal(128, 11.0, 20.0, 20.0, 500.0, 35.0, ipos);
        AddFakeSignal(86, 77.7, 300.0, 16.0, 300, 1000, ipos);
        AddFakeSignal(55, 37.7, 100.0, 20.0, 300, 1000, ipos);
        SetElectodeIntensitiesScale();
    }

    void AddFakeSignal(int p, double wavelength, double ampl, double phas, double pos, double width, Ipos ipos) {
        ipos.MakeProbe(probe, p, 2);
        for (int i = 0; i < nPoints; i++) {
            double ffac = Math.sin((i + phas) * Math.PI * 2 / wavelength) * ampl;
            if (Math.abs(i - pos) > width) ffac = 0.0;
            for (int j = 0; j < nChan; j++) electrodeins[i][j] += probe[j] * ffac;
        }
    }

    void AddWaveletView(int p) {
        BuildWavelet(11.0, 0.1, 50);
        for (int i = 0; i < nPoints; i++) {
            int wi = i - nPoints / 2 + wshcen;
            if ((wi >= wshcen - wshwidth) && (wi <= wshcen + wshwidth)) electrodeins[i][p] += waveletsh[wi];
        }
        SetElectodeIntensitiesScale();
    }

    void AddNoiseSignal(double sd) {
        for (int i = 0; i < nPoints; i++) for (int j = 0; j < nChan; j++) electrodeins[i][j] += (random.nextGaussian() * 2 - 1) * sd;
        SetElectodeIntensitiesScale();
    }

    void CopySubspaceIns(Ibrainpatterns ibrainpatterns, boolean bselsubonly) {
        if (bselsubonly) {
            nsubspacei = ibrainpatterns.nselbrainpatterns;
            nv = nsubspacei;
            for (int i = 0; i < nv; i++) {
                double[] lelectrodeins = ibrainpatterns.GetBrainMap(i + ibrainpatterns.nchosenbrainpatterns).telectrodeins;
                for (int j = 0; j < nChan; j++) electrodesub[i][j] = lelectrodeins[j];
            }
        } else {
            int nbm = ibrainpatterns.nchosenbrainpatterns;
            nsubspacei = ibrainpatterns.nselbrainpatterns;
            nv = nbm + nsubspacei;
            for (int i = 0; i < nv; i++) {
                double[] lelectrodeins = ibrainpatterns.GetBrainMap(i).telectrodeins;
                for (int j = 0; j < nChan; j++) electrodesub[i][j] = lelectrodeins[j];
            }
        }
    }

    double[] GetDisplElectrodeIns(int i, int vectracetype) {
        switch(vectracetype) {
            case 0:
                return electrodeins[i];
            case 1:
                return elecsubins[i];
            case 2:
                return elecdiff[i];
            default:
                System.out.println("Error");
        }
        return null;
    }

    double MatchNonzeroChannels(double[] bestmatch, double[] probe, int nvlo, boolean bnewsymmat) {
        if (bnewsymmat) Imath.BuildSqSymmBasisMatrix(SVsqss, nv, electrodesub, nChan);
        boolean bInvSucc = Imath.FindSubspaceScalars(null, bestmatch, Lf, Vsqss, SVsqss, nv, electrodesub, probe, nChan);
        Imath.MultSubspaceScalars(null, bestmatch, Lf, nvlo, nv, electrodesub, probe, nChan);
        int probesupport = 0;
        double diffsq = 0.0;
        for (int d = 0; d < nChan; d++) {
            if (probe[d] != 0.0) {
                double ddi = probe[d] - bestmatch[d];
                diffsq += ddi * ddi;
                probesupport++;
            }
        }
        if (!bInvSucc || (probesupport == 0)) {
            System.out.println("singular matrix.");
            return -1.0F;
        }
        return Math.sqrt(diffsq / probesupport);
    }

    int MakeSubspaceVex() {
        System.out.println("subspace on " + nv);
        Imath.BuildSqSymmBasisMatrix(SVsqss, nv, electrodesub, nChan);
        mdi = -1;
        sscamax = 0.0;
        for (int i = 0; i < nPoints; i++) {
            Imath.FindSubspaceScalars(elecdiff[i], elecsubins[i], subspacescalars[i], Vsqss, SVsqss, nv, electrodesub, electrodeins[i], nChan);
            subspacescalars[i][nv] = Imath.MultSubspaceScalars(elecdiff[i], elecsubins[i], subspacescalars[i], 0, nv, electrodesub, electrodeins[i], nChan);
            if ((subspacescalars[i][nv] != -1.0F) && ((mdi == -1) || (subspacescalars[i][nv] > subspacescalars[mdi][nv]))) mdi = i;
            for (int j = 0; j < nv; j++) sscamax = Math.max(sscamax, Math.abs(subspacescalars[i][j]));
        }
        subspacei[nsubspacei] = mdi;
        System.out.println("Dev " + subspacescalars[mdi][nv] + "  next index " + mdi);
        sscafac = (sscamax != 0.0 ? 1.0 / sscamax : 1.0);
        return mdi;
    }

    Idata(String fname) {
        nChan = 129;
        nPoints = 1000;
        InitArrays();
        ReadElectrodeIntensities(fname);
        MakeSubspaceVex();
    }

    void ReadElectrodeIntensities(String fname) {
        try {
            FileReader fis = new FileReader(fname);
            BufferedReader br = new BufferedReader(fis);
            StreamTokenizer stoken = new StreamTokenizer(br);
            stoken.resetSyntax();
            stoken.wordChars('0', '9');
            stoken.wordChars('e', 'e');
            stoken.wordChars('.', '.');
            stoken.wordChars('-', '-');
            stoken.wordChars('+', '+');
            stoken.whitespaceChars(' ', ' ');
            stoken.whitespaceChars('\n', '\n');
            stoken.whitespaceChars('\r', '\r');
            double fv[] = new double[nPoints];
            int ifv = 0;
            double mini = 0.0F;
            double maxi = 0.0F;
            int ts = 0;
            int nd = 0;
            while (stoken.nextToken() == StreamTokenizer.TT_WORD) {
                double val = (double) Double.valueOf(stoken.sval).doubleValue();
                electrodeins[ts][nd] = val;
                mini = Math.min(mini, val);
                maxi = Math.max(maxi, val);
                ts++;
                if (ts == nPoints) {
                    nd++;
                    ts = 0;
                }
            }
            int nre = (nd * nPoints + ts);
            System.out.println("Read " + nre + " intensities with range " + mini + " to " + maxi);
            double isca = 1.0F / 17.0F;
            for (int i = 0; i < nPoints; i++) for (int j = 0; j < nChan; j++) electrodeins[i][j] = Math.max(-1.0F, Math.min(1.0F, electrodeins[i][j] * isca));
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}

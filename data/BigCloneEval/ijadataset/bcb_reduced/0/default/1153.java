import java.io.*;

public class Spectrum {

    double[][][] spectrum;

    double[] Epow_s = new double[4];

    double rho;

    double[][][][] A_ro, B_ro, C_ro, D_ro, location, eigen_ro, spectrum_r;

    double[][][] eigen_location;

    double[][] Epow_r, angl_spectrum;

    double[] aa_r;

    Spectrum(int N_spectrum, int N_a_rotation, int ch_case, int[] N_lm, int N_lmtot, int N_a, int fitpower, int fiteigen) {
        spectrum = new double[4][2][N_spectrum];
        spectrum_r = new double[3][N_a_rotation][2][N_spectrum];
        Epow_r = new double[3][N_a_rotation];
        if (ch_case == 3) {
            A_ro = new double[3][N_a_rotation][][];
            B_ro = new double[3][N_a_rotation][][];
            C_ro = new double[3][N_a_rotation][][];
            D_ro = new double[3][N_a_rotation][][];
            location = new double[3][N_a_rotation][][];
            eigen_ro = new double[3][][][];
            eigen_location = new double[3][N_lmtot][5];
        }
        if (ch_case == 3) {
            for (int i = 0; i < 3; i++) {
                eigen_ro[i] = new double[N_lm[i]][5][fiteigen];
                eigen_location[i] = new double[N_lm[i]][5];
                location[i] = new double[N_a_rotation][N_lm[i]][3];
                for (int j = 0; j < N_a_rotation; j++) {
                    A_ro[i][j] = new double[N_lm[i]][2];
                    B_ro[i][j] = new double[N_lm[i]][fitpower];
                    C_ro[i][j] = new double[N_lm[i]][fitpower];
                    D_ro[i][j] = new double[N_lm[i]][2];
                }
            }
        }
        aa_r = new double[N_a_rotation];
        angl_spectrum = new double[2][N_a];
        for (int i = 0; i < N_a; i++) angl_spectrum[0][i] = -1. + 2. / (N_a + 1.) * (i + 1.);
    }

    public void calculateRotatingSpectrum(double ap1, int N_a_rotation1, int N_spectrum1) {
        for (int t = 0; t < 3; t++) {
            int A_index;
            double Epow_s1;
            A_index = findpd(ap1, N_a_rotation1, aa_r);
            if (A_index > N_a_rotation1 - 2) A_index = N_a_rotation1 - 2;
            Epow_s[t] = ((ap1 - aa_r[A_index]) * Epow_r[t][A_index + 1] + (aa_r[A_index + 1] - ap1) * Epow_r[t][A_index]) / (aa_r[A_index + 1] - aa_r[A_index]);
            for (int k = 0; k < N_spectrum1; k++) {
                spectrum[t][0][k] = spectrum_r[t][A_index][0][k];
                spectrum[t][1][k] = ((aa_r[A_index + 1] - ap1) * spectrum_r[t][A_index][1][k] + (ap1 - aa_r[A_index]) * spectrum_r[t][A_index + 1][1][k]) / (aa_r[A_index + 1] - aa_r[A_index]);
            }
        }
    }

    public int findpd(double Y, int N, double[] X) {
        int JLX = -1;
        int JU = N + 1;
        while (JU - JLX > 1) {
            int JM = (JU + JLX) / 2;
            if (Y > X[JM]) JLX = JM; else JU = JM;
        }
        if (JLX < 0) JLX = 0;
        return JLX;
    }

    public void readParameters(int ch_case, double end_spectrum, int N_spectrum, int dim, int dims, double Bt, int on_gravity, int N_a_rotation, int fiteigen, int[] N_lm) throws IOException {
        if (ch_case == 1) spectrum_norotating(end_spectrum, N_spectrum, dim, dims);
        if (ch_case == 2) readSpectrumNorotatingTension(end_spectrum, N_spectrum, dim, dims, Bt);
        Epow_s[3] *= on_gravity;
        if (ch_case == 3) read_parameter_rotating(N_a_rotation, end_spectrum, N_spectrum, dim, N_lm, fiteigen);
        if (ch_case == 4) set_parameter_lisa(dim, N_spectrum, end_spectrum);
    }

    public void spectrum_norotating(double end_spectrum, int N_spectrum1, int dim1, int dims1) throws IOException {
        int power = 8;
        for (int t = 0; t < 4; t++) {
            double[] A = new double[2];
            double[] B = new double[power];
            double[] C = new double[power];
            double[] D = new double[2];
            double[] location1 = new double[3];
            String filename;
            if (t == 0) filename = "spectrum/nonrotation/scalar/scalar_" + dim1 + "_" + dims1 + ".txt"; else if (t == 1) filename = "spectrum/nonrotation/spinor/spinor_" + dim1 + ".txt"; else if (t == 3) filename = "spectrum/nonrotation/graviton/graviton_" + dim1 + ".txt"; else filename = "spectrum/nonrotation/vector/gauge_" + dim1 + "_" + dims1 + ".txt";
            File f = new File(filename);
            BufferedReader r = new BufferedReader(new FileReader(f));
            String l = r.readLine();
            String[] as = l.split(",");
            for (int i = 0; i < location1.length; i++) location1[i] = Double.parseDouble(as[i]);
            l = r.readLine();
            as = l.split(",");
            for (int i = 0; i < A.length; i++) A[i] = Double.parseDouble(as[i]);
            l = r.readLine();
            as = l.split(",");
            for (int i = 0; i < B.length; i++) B[i] = Double.parseDouble(as[i]);
            l = r.readLine();
            as = l.split(",");
            for (int i = 0; i < C.length; i++) C[i] = Double.parseDouble(as[i]);
            l = r.readLine();
            as = l.split(",");
            for (int i = 0; i < D.length; i++) D[i] = Double.parseDouble(as[i]);
            for (int i = 0; i < N_spectrum1; i++) {
                spectrum[t][0][i] = (i + 1) * end_spectrum / N_spectrum1;
                if (spectrum[t][0][i] < location1[0]) spectrum[t][1][i] = part1(A, spectrum[t][0][i]) / spectrum[t][0][i]; else if (spectrum[t][0][i] < location1[1]) spectrum[t][1][i] = part23(B, spectrum[t][0][i] - location1[1], power) / spectrum[t][0][i]; else if (spectrum[t][0][i] < location1[2]) spectrum[t][1][i] = part23(C, spectrum[t][0][i] - location1[2], power) / spectrum[t][0][i]; else spectrum[t][1][i] = part4(D, spectrum[t][0][i]) / spectrum[t][0][i];
            }
            double aa = spectrum[t][1][0];
            double aa1;
            spectrum[t][1][0] = 0.;
            for (int i = 1; i < N_spectrum1; i++) {
                aa1 = spectrum[t][1][i];
                spectrum[t][1][i] = (aa + aa1) / 2. + spectrum[t][1][i - 1];
                aa = aa1;
            }
            aa1 = spectrum[t][1][N_spectrum1 - 1] * (spectrum[t][0][1] - spectrum[t][0][0]);
            for (int i = 0; i < N_spectrum1; i++) spectrum[t][1][i] /= spectrum[t][1][N_spectrum1 - 1];
            Epow_s[t] = aa1;
        }
    }

    public void readSpectrumNorotatingTension(double end_spectrum, int N_spectrum1, int dim, int dims, double Bt1) throws IOException {
        int power = 8;
        for (int t = 0; t < 4; t++) {
            double[] A = new double[2];
            double[] B = new double[power];
            double[] C = new double[power];
            double[] D = new double[2];
            double[] A1 = new double[2];
            double[] B1 = new double[power];
            double[] C1 = new double[power];
            double[] D1 = new double[2];
            double[] location1 = new double[3];
            double[] location2 = new double[3];
            if (((dims == 0) && (t < 3)) || (t == 1)) {
                String filename;
                if (t == 0) filename = "spectrum/nonrotation/scalar/scalar_" + dim + "_" + dims + ".txt"; else if (t == 1) filename = "spectrum/nonrotation/spinor/spinor_" + dim + ".txt"; else filename = "spectrum/nonrotation/vector/gauge_" + dim + "_" + dims + ".txt";
                File f = new File(filename);
                BufferedReader r = new BufferedReader(new FileReader(f));
                String l = r.readLine();
                String[] as = l.split(",");
                for (int i = 0; i < location2.length; i++) location2[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < A.length; i++) A[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < B.length; i++) B[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < C.length; i++) C[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < D.length; i++) D[i] = Double.parseDouble(as[i]);
                for (int i = 0; i < N_spectrum1; i++) {
                    spectrum[t][0][i] = (i + 1) * end_spectrum / N_spectrum1;
                    if (spectrum[t][0][i] < location2[0]) spectrum[t][1][i] = part1(A, spectrum[t][0][i]) / spectrum[t][0][i]; else if (spectrum[t][0][i] < location2[1]) spectrum[t][1][i] = part23(B, spectrum[t][0][i] - location2[1], power) / spectrum[t][0][i]; else if (spectrum[t][0][i] < location2[2]) spectrum[t][1][i] = part23(C, spectrum[t][0][i] - location2[2], power) / spectrum[t][0][i]; else spectrum[t][1][i] = part4(D, spectrum[t][0][i]) / spectrum[t][0][i];
                }
                double aa = spectrum[t][1][0];
                double aa1;
                spectrum[t][1][0] = 0.;
                for (int i = 1; i < N_spectrum1; i++) {
                    aa1 = spectrum[t][1][i];
                    spectrum[t][1][i] = (aa + aa1) / 2. + spectrum[t][1][i - 1];
                    aa = aa1;
                }
                aa1 = spectrum[t][1][N_spectrum1 - 1] * (spectrum[t][0][1] - spectrum[t][0][0]);
                for (int i = 0; i < N_spectrum1; i++) spectrum[t][1][i] /= spectrum[t][1][N_spectrum1 - 1];
                Epow_s[t] = aa1;
            } else if (((dims == 2) && (t != 1)) || (t == 3)) {
                int Bc1, Bc2;
                if (Bt1 < 0.4) {
                    Bc1 = 0;
                    Bc2 = 4;
                } else if (Bt1 < 0.6) {
                    Bc1 = 4;
                    Bc2 = 6;
                } else if (Bt1 < 0.8) {
                    Bc1 = 6;
                    Bc2 = 8;
                } else if (Bt1 < 0.9) {
                    Bc1 = 8;
                    Bc2 = 9;
                } else {
                    Bc1 = 9;
                    Bc2 = 10;
                }
                String filename;
                if (t == 0) filename = "spectrum/tension/scalar/scalar_t_" + Bc1 + ".txt"; else if (t == 3) filename = "spectrum/tension/graviton/graviton_t_" + Bc1 + ".txt"; else filename = "spectrum/tension/vector/gauge_t_" + Bc1 + ".txt";
                File f = new File(filename);
                BufferedReader r = new BufferedReader(new FileReader(f));
                String l = r.readLine();
                String[] as = l.split(",");
                for (int i = 0; i < location2.length; i++) location2[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < A.length; i++) A[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < B.length; i++) B[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < C.length; i++) C[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < D.length; i++) D[i] = Double.parseDouble(as[i]);
                if (t == 0) filename = "spectrum/tension/scalar/scalar_t_" + Bc2 + ".txt"; else if (t == 3) filename = "spectrum/tension/graviton/graviton_t_" + Bc2 + ".txt"; else filename = "spectrum/tension/vector/gauge_t_" + Bc2 + ".txt";
                File g = new File(filename);
                r = new BufferedReader(new FileReader(g));
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < location1.length; i++) location1[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < A1.length; i++) A1[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < B1.length; i++) B1[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < C1.length; i++) C1[i] = Double.parseDouble(as[i]);
                l = r.readLine();
                as = l.split(",");
                for (int i = 0; i < D1.length; i++) D1[i] = Double.parseDouble(as[i]);
                for (int i = 0; i < N_spectrum1; i++) {
                    spectrum[t][0][i] = (i + 1) * end_spectrum / N_spectrum1;
                    double aa = (Bc2 - Bt1 * 10.) / ((double) (Bc2 - Bc1));
                    double aa1 = (10 * Bt1 - Bc1) / ((double) (Bc2 - Bc1));
                    if (spectrum[t][0][i] < location2[0]) spectrum[t][1][i] = part1(A, spectrum[t][0][i]) * aa / spectrum[t][0][i]; else if (spectrum[t][0][i] < location2[1]) spectrum[t][1][i] = part23(B, spectrum[t][0][i] - location2[1], power) * aa / spectrum[t][0][i]; else if (spectrum[t][0][i] < location2[2]) spectrum[t][1][i] = part23(C, spectrum[t][0][i] - location2[2], power) * aa / spectrum[t][0][i]; else spectrum[t][1][i] = part4(D, spectrum[t][0][i]) * aa / spectrum[t][0][i];
                    if (spectrum[t][0][i] < location1[0]) spectrum[t][1][i] += part1(A1, spectrum[t][0][i]) * aa1 / spectrum[t][0][i]; else if (spectrum[t][0][i] < location1[1]) spectrum[t][1][i] += part23(B1, spectrum[t][0][i] - location1[1], power) * aa1 / spectrum[t][0][i]; else if (spectrum[t][0][i] < location1[2]) spectrum[t][1][i] += part23(C1, spectrum[t][0][i] - location1[2], power) * aa1 / spectrum[t][0][i]; else spectrum[t][1][i] += part4(D1, spectrum[t][0][i]) * aa1 / spectrum[t][0][i];
                }
                double aa = spectrum[t][1][0];
                double aa1;
                spectrum[t][1][0] = 0.;
                for (int i = 1; i < N_spectrum1; i++) {
                    aa1 = spectrum[t][1][i];
                    spectrum[t][1][i] = (aa + aa1) / 2. + spectrum[t][1][i - 1];
                    aa = aa1;
                }
                aa1 = spectrum[t][1][N_spectrum1 - 1] * (spectrum[t][0][1] - spectrum[t][0][0]);
                for (int i = 0; i < N_spectrum1; i++) spectrum[t][1][i] /= spectrum[t][1][N_spectrum1 - 1];
                Epow_s[t] = aa1;
            }
        }
    }

    public void read_parameter_rotating(int N_a_rotation1, double end_spectrum, int N_spectrum1, int dim1, int[] N_lm, int fiteigen) throws IOException {
        int power = 9;
        for (int t = 0; t < 3; t++) {
            String filename;
            filename = "spectrum/rotation/n" + dim1 + ".txt";
            File f = new File(filename);
            BufferedReader r = new BufferedReader(new FileReader(f));
            for (int i = 0; i < N_a_rotation1; i++) {
                String l = r.readLine();
                aa_r[i] = Double.parseDouble(l);
            }
            r.close();
            for (int i = 0; i < N_a_rotation1; i++) {
                if (t == 0) filename = "spectrum/rotation/scalar/n" + dim1 + "/scalar_" + aa_r[i] + "_" + dim1 + ".txt"; else if (t == 1) filename = "spectrum/rotation/spinor/n" + dim1 + "/spinor_" + aa_r[i] + "_" + dim1 + ".txt"; else filename = "spectrum/rotation/gauge/n" + dim1 + "/vector_" + aa_r[i] + "_" + dim1 + ".txt";
                for (int k = 0; k < N_spectrum1; k++) {
                    spectrum_r[t][i][0][k] = (k + 1) * end_spectrum / N_spectrum1;
                    spectrum_r[t][i][1][k] = 0.;
                }
                double omega = aa_r[i] / (1 + aa_r[i] * aa_r[i]);
                double T = ((dim1 + 1) + (dim1 - 1) * aa_r[i] * aa_r[i]) / 4. / Math.PI / 2 / 2. / (1. + aa_r[i] * aa_r[i]);
                File g = new File(filename);
                r = new BufferedReader(new FileReader(g));
                for (int j = 0; j < N_lm[t]; j++) {
                    String l = r.readLine();
                    String[] as = l.split(",");
                    double m = Double.parseDouble(as[1]);
                    m = Double.parseDouble(as[2]);
                    l = r.readLine();
                    as = l.split(",");
                    for (int k = 0; k < location[t][i][j].length; k++) location[t][i][j][k] = Double.parseDouble(as[k]);
                    l = r.readLine();
                    as = l.split(",");
                    for (int k = 0; k < 2; k++) A_ro[t][i][j][k] = Double.parseDouble(as[k]);
                    l = r.readLine();
                    as = l.split(",");
                    for (int k = 0; k < power; k++) B_ro[t][i][j][k] = Double.parseDouble(as[k]);
                    l = r.readLine();
                    as = l.split(",");
                    for (int k = 0; k < power; k++) C_ro[t][i][j][k] = Double.parseDouble(as[k]);
                    l = r.readLine();
                    as = l.split(",");
                    for (int k = 0; k < 1; k++) D_ro[t][i][j][k] = Double.parseDouble(as[k]);
                    for (int k = 0; k < N_spectrum1; k++) {
                        if (spectrum_r[t][i][0][k] < location[t][i][j][0]) spectrum_r[t][i][1][k] += part1((A_ro[t][i][j]), spectrum_r[t][i][0][k]) / spectrum_r[t][i][0][k]; else if (spectrum_r[t][i][0][k] < location[t][i][j][1]) spectrum_r[t][i][1][k] += part23((B_ro[t][i][j]), spectrum_r[t][i][0][k] - location[t][i][j][1], power) / spectrum_r[t][i][0][k]; else if (spectrum_r[t][i][0][k] < location[t][i][j][2]) spectrum_r[t][i][1][k] += part23((C_ro[t][i][j]), spectrum_r[t][i][0][k] - location[t][i][j][2], power) / spectrum_r[t][i][0][k]; else spectrum_r[t][i][1][k] += part4_rotation((D_ro[t][i][j][0]), spectrum_r[t][i][0][k], omega, T, m, t) / spectrum_r[t][i][0][k];
                    }
                }
                r.close();
                double aa = spectrum_r[t][i][1][0];
                double aa1;
                spectrum_r[t][i][1][0] = 0.;
                for (int k = 1; k < N_spectrum1; k++) {
                    aa1 = spectrum_r[t][i][1][k];
                    spectrum_r[t][i][1][k] = (aa + aa1) / 2. + spectrum_r[t][i][1][k - 1];
                    aa = aa1;
                }
                Epow_r[t][i] = spectrum_r[t][i][1][N_spectrum1 - 1] * (spectrum_r[t][i][0][1] - spectrum_r[t][i][0][0]);
                for (int k = 0; k < N_spectrum1; k++) spectrum_r[t][i][1][k] /= spectrum_r[t][i][1][N_spectrum1 - 1];
            }
            if (t == 0) filename = "spectrum/rotation/angular/scalar/scalar-angle-" + dim1 + ".txt"; else if (t == 1) filename = "spectrum/rotation/angular/spinor/spinor-angle-" + dim1 + ".txt"; else filename = "spectrum/rotation/angular/gauge/vector-angle-" + dim1 + ".txt";
            f = new File(filename);
            r = new BufferedReader(new FileReader(f));
            for (int j = 0; j < N_lm[t]; j++) {
                r.readLine();
                String l = r.readLine();
                String[] as = l.split(",");
                for (int i = 0; i < 5; i++) eigen_location[t][j][i] = Double.parseDouble(as[i]);
                for (int i = 0; i < 5; i++) {
                    l = r.readLine();
                    as = l.split(",");
                    for (int k = 0; k < fiteigen; k++) eigen_ro[t][j][i][k] = Double.parseDouble(as[k]);
                }
            }
            r.close();
        }
    }

    public void set_parameter_lisa(int dim1, int N_spectrum1, double end_spectrum) throws IOException {
        double[] Gamma_i = new double[3], phi_i = new double[3], f_i = new double[] { 1., 7. / 8., 1. }, g_i = new double[] { 1., 3. / 4., 1. }, c_i = new double[] { 1, 90, 27 };
        double T_lisa, lisa1, lisa2, lisa1_1, lisa2_1, delta_lisa;
        int power = 8;
        double[] location1 = new double[3];
        double[] A = new double[2];
        double[] B = new double[power];
        double[] C = new double[power];
        double[] D = new double[2];
        T_lisa = (1. + dim1) / 4 / Math.PI;
        for (int i = 0; i < 3; i++) {
            String filename;
            if (i == 0) filename = "spectrum/nonrotation/scalar/scalar_" + dim1 + "_0.txt"; else if (i == 1) filename = "spectrum/nonrotation/spinor/spinor_" + dim1 + ".txt"; else filename = "spectrum/nonrotation/vector/gauge_" + dim1 + "_0.txt";
            File f = new File(filename);
            BufferedReader r = new BufferedReader(new FileReader(f));
            String l = r.readLine();
            String[] as = l.split(",");
            for (int j = 0; j < 3; j++) location1[j] = Double.parseDouble(as[j]);
            l = r.readLine();
            as = l.split(",");
            for (int j = 0; j < 2; j++) A[j] = Double.parseDouble(as[j]);
            l = r.readLine();
            as = l.split(",");
            for (int j = 0; j < power; j++) B[j] = Double.parseDouble(as[j]);
            l = r.readLine();
            as = l.split(",");
            for (int j = 0; j < power; j++) C[j] = Double.parseDouble(as[j]);
            l = r.readLine();
            as = l.split(",");
            for (int j = 0; j < 2; j++) D[j] = Double.parseDouble(as[j]);
            r.close();
            phi_i[i] = 0;
            Gamma_i[i] = 0;
            lisa1 = 0;
            lisa2 = 0;
            for (int j = 0; j < N_spectrum1; j++) {
                double aa = (j + 1) * end_spectrum / N_spectrum1;
                double aa1;
                if (aa < location1[0]) aa1 = part1(A, aa); else if (aa < location1[1]) aa1 = part23(B, aa - location1[1], power); else if (aa < location1[2]) aa1 = part23(C, aa - location1[2], power); else aa1 = part4(D, aa);
                if (i == 0 || i == 2) {
                    lisa1_1 = aa * aa / (Math.exp(aa / T_lisa) - 1);
                    lisa2_1 = aa * aa * aa / (Math.exp(aa / T_lisa) - 1);
                } else {
                    lisa1_1 = aa * aa / (Math.exp(aa / T_lisa) + 1);
                    lisa2_1 = aa * aa * aa / (Math.exp(aa / T_lisa) + 1);
                }
                if (j == 0 || j == N_spectrum1 - 1) {
                    phi_i[i] += aa1 / 2.;
                    Gamma_i[i] += aa1 / aa / 2.;
                    lisa1 += lisa1_1 / 2.;
                    lisa2 += lisa2_1 / 2.;
                } else {
                    phi_i[i] += aa1;
                    Gamma_i[i] += aa1 / aa;
                    lisa1 += lisa1_1;
                    lisa2 += lisa2_1;
                }
            }
            phi_i[i] /= lisa2;
            Gamma_i[i] /= lisa1;
        }
        lisa1 = 0;
        lisa2 = 0;
        for (int i = 0; i < 3; i++) {
            lisa1 += c_i[i] * g_i[i] * Gamma_i[i];
            lisa2 += c_i[i] * f_i[i] * phi_i[i];
        }
        rho = lisa1 / lisa2 * 1.20206 * 2 / (Math.PI * Math.PI * Math.PI * Math.PI / 90. * 6.);
    }

    public static double part1(double[] A, double x) {
        return A[0] * Math.pow(x, A[1]);
    }

    public static double part23(double[] B, double x, int n) {
        double a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            a += B[i] * b;
            b *= x;
        }
        return a;
    }

    public static double root_part23(double[] B, double x, int n, double r_max) {
        double a = r_max / 2;
        for (int j = 0; j < 8; j++) {
            double b = 1;
            double c = B[0] - x;
            double d = 0;
            for (int i = 1; i < n; i++) {
                d += i * B[i] * b;
                b *= a;
                c += B[i] * b;
            }
            a = a - c / d;
        }
        return a;
    }

    public static double part4(double[] D, double x) {
        return D[0] / (1 + Math.exp(D[1] * x));
    }

    public static double part4_rotation(double D, double x, double omega, double T, double m, int i) {
        if (i == 0 || i == 2) return D * x / (Math.exp((x - m * omega) / T) - 1.); else return D * x / (Math.exp((x - m * omega) / T) + 1.);
    }

    public static double part1_4(double[] A, double[] B, double[] C, double[] D, double[] location1, double aa, int power, double omega, double T, double m, int type) {
        double aa1;
        if (aa < location1[0]) aa1 = part1(A, aa); else if (aa < location1[1]) aa1 = part23(B, aa - location1[1], power); else if (aa < location1[2]) aa1 = part23(C, aa - location1[2], power); else aa1 = part4_rotation(D[0], aa, omega, T, m, type);
        return aa1;
    }
}

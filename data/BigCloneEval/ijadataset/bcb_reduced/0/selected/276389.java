package pogvue.datamodel;

import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class Pwm {

    private double[] lods = null;

    private double[] pwm;

    private String name;

    private ChrRegion region;

    public Pwm(double[] pwm, String name) {
        this.pwm = pwm;
        this.name = name;
    }

    public void setChrRegion(ChrRegion r) {
        this.region = r;
    }

    public ChrRegion getChrRegion() {
        return region;
    }

    public void setPwm(double[] pwm) {
        this.pwm = pwm;
    }

    public double[] getPwm() {
        return pwm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void print(double pwm[]) {
        int j = 0;
        while (j < pwm.length / 4) {
            int i = 0;
            System.out.print("A T C G ");
            while (i < 4) {
                System.out.print(pwm[j * 4 + i] + " ");
                i++;
            }
            System.out.println();
            j++;
        }
    }

    public static void printPwmLine(double pwm[]) {
        int i = 0;
        while (i < pwm.length / 4) {
            System.out.println((int) (100 * pwm[i * 4]) / 10 + "\t" + (int) (100 * pwm[i * 4 + 1]) / 10 + "\t" + (int) (100 * pwm[i * 4 + 2]) / 10 + "\t" + (int) (100 * pwm[i * 4 + 3]) / 10);
            i++;
        }
    }

    public static void printLogo(double pwm[]) {
        int i = 0;
        String[] bases = new String[4];
        bases[0] = "A";
        bases[1] = "T";
        bases[2] = "C";
        bases[3] = "G";
        while (i < pwm.length / 4) {
            double inf = 0;
            int j = 0;
            while (j < 4) {
                if (pwm[i * 4 + j] > 0) {
                    inf += pwm[i * 4 + j] * Math.log(pwm[i * 4 + j]) / Math.log(2);
                }
                j++;
            }
            inf = 2 + inf;
            j = 0;
            System.out.print("Base " + i + "\t");
            while (j < 4) {
                int len = (int) (pwm[i * 4 + j] * 20 * inf);
                int k = 0;
                while (k < len) {
                    System.out.print(bases[j]);
                    k++;
                }
                if (k > 1) {
                    System.out.print(" ");
                }
                j++;
            }
            System.out.println();
            i++;
        }
    }

    public double getInformationContent() {
        int i = 0;
        double inf = 0;
        while (i < pwm.length / 4) {
            int j = 0;
            while (j < 4) {
                if (pwm[i * 4 + j] > 0) {
                    inf += pwm[i * 4 + j] * Math.log(pwm[i * 4 + j]) / Math.log(2);
                }
                j++;
            }
            inf = 2 + inf;
            i++;
        }
        return inf;
    }

    public double[] getLogOdds() {
        lods = new double[pwm.length];
        int i = 0;
        while (i < pwm.length / 4) {
            double inf = 0;
            int j = 0;
            while (j < 4) {
                if (pwm[i * 4 + j] > 0) {
                    inf += pwm[i * 4 + j] * Math.log(pwm[i * 4 + j]) / Math.log(2);
                }
                j++;
            }
            inf = (2 + inf) / 2;
            j = 0;
            while (j < 4) {
                if (pwm[i * 4 + j] > 0) {
                    lods[i * 4 + j] = (Math.log(pwm[i * 4 + j] / 0.24) / Math.log(2)) * inf;
                } else {
                    lods[i * 4 + j] = (Math.log(.0001) / Math.log(2)) * inf;
                }
                j++;
            }
            i++;
        }
        return lods;
    }

    public double scoreLogOdds(double[] pwm2) {
        double[] lods = getLogOdds();
        double score = 0;
        int i = 0;
        while (i < lods.length) {
            score += pwm2[i] * lods[i];
            i++;
        }
        return score;
    }

    public double getInfContent() {
        int i = 0;
        double totinf = 0;
        while (i < pwm.length / 4) {
            double inf = 0;
            int j = 0;
            while (j < 4) {
                if (pwm[i * 4 + j] > 0) {
                    inf += pwm[i * 4 + j] * Math.log(pwm[i * 4 + j]) / Math.log(2);
                }
                j++;
            }
            totinf += 2 + inf;
            i++;
        }
        return totinf;
    }

    public String getConsensus() {
        StringBuffer cons = new StringBuffer();
        int i = 0;
        while (i < pwm.length / 4) {
            double maxval = pwm[i * 4];
            String maxch = "A";
            if (pwm[i * 4 + 1] > maxval) {
                maxval = pwm[i * 4 + 1];
                maxch = "T";
            } else if (pwm[i * 4 + 2] > maxval) {
                maxval = pwm[i * 4 + 2];
                maxch = "C";
            } else if (pwm[i * 4 + 3] > maxval) {
                maxval = pwm[i * 4 + 3];
                maxch = "G";
            }
            cons.append(maxch);
            i++;
        }
        return cons.toString();
    }

    public double[] getRevPwm() {
        int i = 0;
        double[] revmat = new double[pwm.length];
        int len = pwm.length / 4;
        while (i < len) {
            revmat[(len - i - 1) * 4 + 0] = pwm[i * 4 + 1];
            revmat[(len - i - 1) * 4 + 1] = pwm[i * 4 + 0];
            revmat[(len - i - 1) * 4 + 2] = pwm[i * 4 + 3];
            revmat[(len - i - 1) * 4 + 3] = pwm[i * 4 + 2];
            i++;
        }
        return revmat;
    }
}

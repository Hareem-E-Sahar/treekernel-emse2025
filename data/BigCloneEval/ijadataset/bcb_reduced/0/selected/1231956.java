package br.uece.paa.sa.jssp.alg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import br.uece.paa.sa.jssp.ui.JsspMain;

/**
 * 
 * @author Mario Alves de Moraes Neto
 * @author Tales Paiva Nogueira
 */
public class JsspSa extends Thread {

    BufferedReader br;

    static short n, m;

    short[][] tempo, ordem;

    int[][] result;

    Integer otimo;

    short[] s, sInicial;

    int nsucc, seq;

    XYSeries series1 = new XYSeries("Makespan");

    Random r = new Random();

    private short[] res;

    private boolean mostrarGrafico;

    int L, M, P;

    double alfa = 0.9;

    JsspMain ui;

    private long tempoGasto;

    JProgressBar prog;

    short[] confSequencial;

    private double tempInicial;

    private double temperatura;

    public JsspSa(File file, boolean mostraGraf, int maxSuc, int maxIter, int maxPert, double a, JsspMain ui) {
        try {
            mostrarGrafico = mostraGraf;
            L = maxSuc;
            M = maxIter;
            P = maxPert;
            alfa = a;
            this.ui = ui;
            prog = ui.getProgressBar();
            prog.setIndeterminate(true);
            this.ui.getTxtMelhorSolucao().setText("");
            this.ui.getTxtSolucaoInicial().setText("");
            this.ui.getTimeElapsedLabel().setText("Tempo gasto: ");
            this.ui.getLblTempIni().setText("Temperatura inicial: ");
            this.ui.getLblTempFim().setText("Temperatura final: ");
            br = new BufferedReader(new FileReader(file));
            String line = new String();
            line = br.readLine();
            String[] temp = line.split(" +");
            n = Short.parseShort(temp[0]);
            m = Short.parseShort(temp[1]);
            tempo = new short[n][m];
            ordem = new short[n][m];
            confSequencial = new short[n];
            short i = 0, j = 0;
            while (line != null) {
                line = line.trim();
                temp = line.split(" +");
                if (temp.length <= 2) {
                    line = br.readLine();
                    continue;
                }
                for (short k = 0; k < temp.length; k++) {
                    ordem[i][j] = Short.parseShort(temp[k]);
                    k++;
                    tempo[i][j] = Short.parseShort(temp[k]);
                    j++;
                }
                confSequencial[i] = i;
                i++;
                j = 0;
                line = br.readLine();
            }
        } catch (FileNotFoundException fnf) {
            JOptionPane.showMessageDialog(null, "Verifique se o arquivo selecionado existe.\n" + fnf.getMessage(), "Arquivo nao encontrado", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Verifique se o arquivo estï¿½ no formato correto.\n" + nfe.getMessage(), "Erro de leitura", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostraGrafico() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        JFreeChart chart = ChartFactory.createXYLineChart(null, "Numero de iteracoes", "Makespan", dataset, PlotOrientation.VERTICAL, false, true, false);
        NumberAxis numberAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);
        numberAxis.setAutoRange(true);
        ChartFrame frame = new ChartFrame("Evolucao", chart);
        frame.pack();
        frame.setVisible(true);
    }

    private void aceita(short[] configuracao2, int f) {
        s = configuracao2.clone();
        nsucc++;
    }

    /**
	 * Metodo qye calcula a temperatua inicial
	 * @param conf uma configuracao inicial qualquer
	 * @param nTent numero de tentativas
	 * @return a temperatura inicial
	 */
    private double calcTempInicial(short[] conf, int nTent, Random r) {
        int prevObj = 0;
        int k = 0;
        int soma = 0;
        int obj = 0;
        while (nTent > 0) {
            obj = funcaoObjetivo3(conf);
            if (otimo == null || obj < otimo.intValue()) {
                otimo = obj;
                s = conf.clone();
            }
            if (obj > prevObj) {
                k++;
                soma += obj;
            }
            prevObj = obj;
            conf = perturba(conf, r);
            nTent--;
        }
        int media;
        if (k > 0) media = soma / k; else media = 1000;
        return 4.48 * media;
    }

    /**
	 * Realiza uma perturbacao na configuracao passada como parametro atraves da
	 * troca de dois elementos aleatoriamente escolhidos
	 * 
	 * @param c
	 *            o array a ser modificado
	 * @return o mesmo array, com dois elementos trocados
	 */
    private short[] perturba(short[] src, Random r) {
        if (res == null) res = new short[src.length];
        res = src.clone();
        int index1 = r.nextInt(res.length);
        int index2 = r.nextInt(res.length);
        if (index1 != index2) {
            short aux = res[index1];
            res[index1] = res[index2];
            res[index2] = aux;
        }
        return res;
    }

    /**
	 * Metodo que calcula a funcao objetivo para o JSSP, que e' o tempo
	 * em que o ultimo job termina na ultima maquina
	 * 
	 * @param sequencia
	 *            a sequencia dos jobs
	 * @return o tempo de execucao do ultimo job
	 */
    private int funcaoObjetivo(short[] sequencia) {
        result = new int[n][m];
        for (int i = 0; i < n; i++) {
            short k = sequencia[i];
            for (int j = 0; j < m; j++) {
                if (i == 0 && j == 0) {
                    result[i][j] = tempo[k][j];
                } else if (j == 0 && i > 0) {
                    result[i][j] = tempo[k][j] + result[i - 1][j];
                } else if (i == 0 && j > 0) {
                    result[i][j] = tempo[k][j] + result[i][j - 1];
                } else {
                    if (result[i - 1][j] >= result[i][j - 1]) result[i][j] = tempo[k][j] + result[i - 1][j]; else result[i][j] = tempo[k][j] + result[i][j - 1];
                }
            }
        }
        return result[n - 1][m - 1];
    }

    /**
	 * Metodo que calcula a funcao objetivo para o JSSP, que e' o tempo
	 * em que o ultimo job termina na ultima maquina
	 * 
	 * @param sequencia
	 *            a sequencia dos jobs
	 * @return o tempo de execucao do ultimo job
	 */
    private int funcaoObjetivo2(short[] sequencia) {
        result = new int[n][m];
        int[][] inicio = new int[n][m];
        short o;
        int ind = 0;
        int oAnt = 0;
        for (int i = 0; i < sequencia.length; i++) {
            short k = sequencia[i];
            for (int j = 0; j < m; j++) {
                o = ordem[k][j];
                if (i == 0 && j == 0) {
                    result[i][o] = tempo[k][o];
                    inicio[i][o] = result[i][o] - tempo[k][o];
                } else if (i == 0 && j > 0) {
                    result[i][o] = tempo[k][o] + result[i][ordem[k][j - 1]];
                    inicio[i][o] = result[i][o] - tempo[k][o];
                } else if (j == 0) {
                    for (ind = i - 1; ind >= 0; ind--) {
                        if (!hasZero(inicio, o, i) && tempo[k][o] <= inicio[ind][o]) {
                            result[i][o] = tempo[k][o];
                            inicio[i][o] = 0;
                        } else if (result[i][o] < result[ind][o] + tempo[k][o]) {
                            result[i][o] = result[ind][o] + tempo[k][o];
                            inicio[i][o] = result[ind][o];
                        }
                    }
                } else {
                    oAnt = ordem[k][j - 1];
                    for (ind = i - 1; ind >= 0; ind--) {
                        if (result[i][oAnt] > result[ind][o]) {
                            result[i][o] = result[i][oAnt] + tempo[k][o];
                            inicio[i][o] = result[i][oAnt];
                        } else if (result[i][oAnt] + tempo[k][o] < inicio[ind][o]) {
                            result[i][o] = result[i][oAnt] + tempo[k][o];
                            inicio[i][o] = result[i][oAnt];
                        } else if (ind >= 1 && inicio[ind][o] > result[ind - 1][o] && inicio[ind][o] - result[ind - 1][o] >= tempo[k][o] && result[ind - 1][o] > result[i][oAnt]) {
                            result[i][o] = result[ind - 1][o] + tempo[k][o];
                            inicio[i][o] = result[i][o] - tempo[k][o];
                        } else if (result[i][o] < result[ind][o] + tempo[k][o]) {
                            result[i][o] = result[ind][o] + tempo[k][o];
                            inicio[i][o] = result[ind][o];
                            break;
                        }
                    }
                }
            }
        }
        int makespan = Integer.MIN_VALUE;
        for (int p = 0; p < n; p++) {
            for (int q = 0; q < m; q++) {
                if (result[p][q] > makespan) makespan = result[p][q];
            }
        }
        if (makespan == Integer.MIN_VALUE) System.err.println("makespan == Integer.MIN_VALUE!");
        return makespan;
    }

    /**
	 * Metodo que informa se uma dada coluna em uma matriz possui
	 * o valor zero em alguma posicao
	 * @param matriz matriz onde o zero sera buscado 
	 * @param index o indice da coluna onde o zero sera buscado
	 * @param currI o indice de limite de linha de busca
	 * @return true se a coluna <index> na matriz <matriz> tem
	 * 			zero em alguma posicao limitada por <currI>, 
	 * 			false, caso contrario 
	 */
    private boolean hasZero(int[][] matriz, int index, int currI) {
        Integer[] res = new Integer[matriz.length];
        for (int i = 0; i < currI; i++) for (int j = 0; j < matriz.length; j++) if (index == j) res[i] = matriz[i][j];
        for (int i = 0; i < res.length; i++) if (res[i] != null && res[i] == 0) return true;
        return false;
    }

    @SuppressWarnings("static-access")
    public int funcaoObjetivo3(short[] sequencia) {
        short[][] mTempoSeq = new short[this.n][this.m];
        short[][] mOrdemSeq = new short[this.n][this.m];
        for (int i = 0; i < this.n; i++) {
            mTempoSeq[i] = tempo[sequencia[i]];
            mOrdemSeq[i] = ordem[sequencia[i]];
        }
        int[] ultimaMaq = new int[this.m];
        int[] ultimoJob = new int[this.n];
        int maqAtual;
        int tempoOpAtual;
        for (int opAtual = 0; opAtual < this.m; opAtual++) for (int jobAtual = 0; jobAtual < this.n; jobAtual++) {
            maqAtual = mOrdemSeq[jobAtual][opAtual];
            tempoOpAtual = mTempoSeq[jobAtual][maqAtual];
            if (ultimoJob[jobAtual] <= ultimaMaq[maqAtual]) {
                ultimaMaq[maqAtual] += tempoOpAtual;
                ultimoJob[jobAtual] = ultimaMaq[maqAtual];
            } else {
                ultimoJob[jobAtual] += tempoOpAtual;
                ultimaMaq[maqAtual] = ultimoJob[jobAtual];
            }
        }
        int tempo = 0;
        for (int m = 0; m < this.m; m++) if (tempo < ultimaMaq[m]) tempo = ultimaMaq[m];
        return tempo;
    }

    /**
	 * Gera uma configuracao inicial consistindo em um array com o tamanho do
	 * numero de tarefas que representa uma dada sequencia de tarefas. Nenhum
	 * elemento do array eh repetido.
	 * 
	 * @param n
	 *            o tamanho do array a ser gerado
	 * @return um array de tamanho n com uma sequencia aleatoria de numeros
	 */
    private short[] geraConfInicial(int n, short[] array) {
        int swap;
        short aux;
        Random random = new Random();
        for (int pos = n - 1; pos > 0; --pos) {
            swap = random.nextInt(pos + 1);
            aux = array[pos];
            array[pos] = array[swap];
            array[swap] = aux;
        }
        return array;
    }

    public String printArray(short[] a) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < a.length; i++) {
            s.append(a[i] + " ");
        }
        return s.toString();
    }

    @Override
    public void run() {
        short[] configuracao2;
        sInicial = geraConfInicial(n, confSequencial);
        temperatura = calcTempInicial(sInicial, 100, r);
        tempInicial = temperatura;
        int f1, f2;
        BigDecimal delta, expo, t;
        double metropolis = 0, rand = 0;
        int j = 1, i = 0;
        tempoGasto = System.currentTimeMillis();
        do {
            i = 1;
            nsucc = 0;
            do {
                if (Thread.interrupted()) {
                    mostrarResultado();
                    return;
                }
                f1 = funcaoObjetivo3(s);
                configuracao2 = perturba(s, r);
                f2 = funcaoObjetivo3(configuracao2);
                delta = new BigDecimal(f2 - f1);
                if (delta.doubleValue() < 0) {
                    aceita(configuracao2, f2);
                } else if (delta.doubleValue() > 0) {
                    t = new BigDecimal(temperatura);
                    expo = delta.negate().divide(t, 16, BigDecimal.ROUND_DOWN);
                    metropolis = Math.exp(expo.doubleValue());
                    rand = r.nextDouble();
                    if (metropolis > rand) aceita(configuracao2, f2);
                }
                i++;
            } while (nsucc < L && i < P);
            temperatura = temperatura * alfa;
            series1.add(j, f1);
            j++;
        } while (j < M);
        tempoGasto = System.currentTimeMillis() - tempoGasto;
        mostrarResultado();
    }

    private void mostrarResultado() {
        JTextField melhor = ui.getTxtMelhorSolucao();
        JTextField inicial = ui.getTxtSolucaoInicial();
        JLabel time = ui.getTimeElapsedLabel();
        JLabel ti = ui.getLblTempIni();
        JLabel tf = ui.getLblTempFim();
        inicial.setText(funcaoObjetivo3(sInicial) + " (" + printArray(sInicial) + ")");
        melhor.setText(funcaoObjetivo3(s) + " (" + printArray(s) + ")");
        time.setText("Tempo gasto: " + (float) tempoGasto / 1000 + "s");
        ti.setText("Temperatura inicial: " + tempInicial);
        tf.setText("Temperatura final: " + temperatura);
        prog.setIndeterminate(false);
        if (mostrarGrafico) mostraGrafico();
        ui.getRunButton().setEnabled(true);
        ui.getStopButton().setEnabled(false);
    }
}

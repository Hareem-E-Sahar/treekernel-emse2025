package motor3d;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import javax.imageio.ImageIO;
import utils.EsPotencia2;

public class MapaAlturas implements Serializable {

    private static final long serialVersionUID = 1;

    private int tamX;

    private int tamZ;

    private int numPuntos;

    private double ladoCuadrado;

    private double minY;

    private double rangoY;

    private int tamX2;

    public double[] alturas;

    public void inicializar(int tamX, int tamZ) {
        this.tamX = tamX;
        this.tamZ = tamZ;
        this.numPuntos = tamX * tamZ;
        this.tamX2 = tamX * tamX;
        alturas = new double[numPuntos];
    }

    public void aplanar(double altura) {
        for (int p = 0; p < numPuntos; p++) {
            alturas[p] = altura;
        }
    }

    public void sumarConstante(double valor) {
        for (int p = 0; p < numPuntos; p++) {
            alturas[p] += valor;
        }
    }

    public void multiplicarConstante(double valor) {
        for (int p = 0; p < numPuntos; p++) {
            alturas[p] *= valor;
        }
    }

    public int getTamX() {
        return tamX;
    }

    public int getTamZ() {
        return tamZ;
    }

    public int getNumPuntos() {
        return numPuntos;
    }

    public double getLadoCuadrado() {
        return ladoCuadrado;
    }

    public double getAnchoX() {
        return (tamX - 1) * ladoCuadrado;
    }

    public double getAnchoZ() {
        return (tamZ - 1) * ladoCuadrado;
    }

    public double getMinY() {
        return minY;
    }

    public double getRangoY() {
        return rangoY;
    }

    public void setParametros(double ladoCuadrado, double minY, double rangoY) {
        this.ladoCuadrado = ladoCuadrado;
        this.minY = minY;
        this.rangoY = rangoY;
    }

    public void cargarDeImagen(String ruta) {
        BufferedImage imgFuente = null;
        try {
            imgFuente = ImageIO.read(new File(ruta));
        } catch (IOException e) {
            log.Log.log("Error: IOException while loading heightmap image " + ruta);
            return;
        }
        int width = Math.min(imgFuente.getWidth(), tamX);
        int height = Math.min(imgFuente.getHeight(), tamZ);
        int p = 0;
        for (int j = 0; j < width; j++) {
            for (int i = 0; i < height; i++) {
                int rgb = imgFuente.getRGB(i, j);
                alturas[p] = (rgb & 0xFF) / 255.0d;
                p++;
            }
            p += tamX - width;
        }
    }

    public void randomizar(Random random) {
        for (int p = 0; p < numPuntos; p++) {
            alturas[p] = random.nextDouble();
        }
    }

    public void suavizar(MapaAlturas destino, boolean bordeContinuo) {
        if (destino.tamX != this.tamX || destino.tamZ != this.tamZ) {
            log.Log.log("Error smoothing heightMap: dimensions doesn't coincide");
            return;
        }
        if (!bordeContinuo) {
            int pThis = 0;
            int pDest = 0;
            for (int j = 0; j < tamZ; j++) {
                for (int i = 0; i < tamX; i++) {
                    double valorAcum = alturas[pThis];
                    int n = 1;
                    if (i > 0) {
                        valorAcum += alturas[pThis - 1];
                        n++;
                    }
                    if (i < tamX - 1) {
                        valorAcum += alturas[pThis + 1];
                        n++;
                    }
                    if (j > 0) {
                        valorAcum += alturas[pThis - tamX];
                        n++;
                    }
                    if (j < tamZ - 1) {
                        valorAcum += alturas[pThis + tamX];
                        n++;
                    }
                    destino.alturas[pDest] = valorAcum / n;
                    pThis++;
                    pDest++;
                }
            }
        } else {
            int pThis = 0;
            int pDest = 0;
            for (int j = 0; j < tamZ; j++) {
                for (int i = 0; i < tamX; i++) {
                    destino.alturas[pDest] = (alturas[(pThis - 1 + tamX) % tamX] + alturas[(pThis + 1) % tamX] + alturas[(pThis - tamX + tamX2) % tamX2] + alturas[(pThis + tamX) % tamX2]) / 4.0d;
                    pThis++;
                    pDest++;
                }
            }
        }
    }

    public void fractalizar(double cteRango, double cteVariacion, boolean bordesAplanados, Random random) {
        if (!EsPotencia2.esPotencia2(this.tamX - 1) || !EsPotencia2.esPotencia2(this.tamZ - 1)) {
            log.Log.log("Error fractalizing heightMap: (dimension -1) is not power of two");
            return;
        }
        fractalizarRectangulo(0, 0, tamX - 1, tamZ - 1, cteRango, cteVariacion, bordesAplanados, random);
    }

    private void fractalizarRectangulo(int x0, int z0, int x2, int z2, double cteRango, double cteVariacion, boolean bordesAplanados, Random random) {
        if (x0 + 1 < x2 && z0 + 1 < z2) {
            int x1 = (x0 + x2) / 2;
            int z1 = (z0 + z2) / 2;
            double h01 = 0.5d * (this.alturas[x0 + z0 * tamX] + this.alturas[x0 + z2 * tamX]);
            double h10 = 0.5d * (this.alturas[x0 + z0 * tamX] + this.alturas[x2 + z0 * tamX]);
            double h12 = 0.5d * (this.alturas[x0 + z2 * tamX] + this.alturas[x2 + z2 * tamX]);
            double h21 = 0.5d * (this.alturas[x2 + z0 * tamX] + this.alturas[x2 + z2 * tamX]);
            double h11 = 0.25d * (h01 + h10 + h12 + h21);
            if (x0 == 0 && !bordesAplanados) {
                this.alturas[x0 + z1 * tamX] += h01 + cteRango * (random.nextDouble() - 0.5d);
            }
            if (z0 == 0 && !bordesAplanados) {
                this.alturas[x1 + z0 * tamX] += h10 + cteRango * (random.nextDouble() - 0.5d);
            }
            if (!(x2 == tamX - 1 && bordesAplanados)) {
                this.alturas[x2 + z1 * tamX] += h21 + cteRango * (random.nextDouble() - 0.5d);
            }
            if (!(z2 == tamZ - 1 && bordesAplanados)) {
                this.alturas[x1 + z2 * tamX] += h12 + cteRango * (random.nextDouble() - 0.5d);
            }
            this.alturas[x1 + z1 * tamX] += h11 + cteRango * (random.nextDouble() - 0.5d);
            fractalizarRectangulo(x0, z0, x1, z1, cteRango * cteVariacion, cteVariacion, bordesAplanados, random);
            fractalizarRectangulo(x1, z0, x2, z1, cteRango * cteVariacion, cteVariacion, bordesAplanados, random);
            fractalizarRectangulo(x0, z1, x1, z2, cteRango * cteVariacion, cteVariacion, bordesAplanados, random);
            fractalizarRectangulo(x1, z1, x2, z2, cteRango * cteVariacion, cteVariacion, bordesAplanados, random);
        }
    }
}

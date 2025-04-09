package juego.genSS;

import java.awt.image.*;
import javax.imageio.*;
import espacio.Astro;
import espacio.VisualAstro;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;
import java.util.Random;
import motor3d.Vector3d;
import motor3d.Geometria;
import utils.FuncionPorTramosVectorial;

public class ProcesadorTexturas {

    private FuncionPorTramosVectorial funcionColoresPlanetasTerraqueos;

    public static final int COLOR_OCEANOS = 0x0A0A33;

    public static final int COLOR_CASQUETES_POLARES = 0xFFFFFF;

    public static final double NIVEL_TERRAQUEO_SELVA = 0.0d;

    public static final double NIVEL_TERRAQUEO_TIERRA = 0.85d;

    public static final double NIVEL_TERRAQUEO_HIELO = 1.0d;

    public static final Vector3d COLOR_TERRAQUEO_SELVA = colorIntAVector3d(0x2E550C);

    public static final Vector3d COLOR_TERRAQUEO_TIERRA = colorIntAVector3d(0xE8BE92);

    public static final Vector3d COLOR_TERRAQUEO_HIELO = new Vector3d(1.0d, 1.0d, 1.0d);

    public static final Vector<Vector3d[]> COLORES_GIGANTES_GAS;

    static {
        COLORES_GIGANTES_GAS = new Vector<Vector3d[]>();
        COLORES_GIGANTES_GAS.add(new Vector3d[] { colorIntAVector3d(0x99a0a4), colorIntAVector3d(0xa0a1a7), colorIntAVector3d(0x8c8484), colorIntAVector3d(0x877d7f), colorIntAVector3d(0x979597), colorIntAVector3d(0xa8a8ab), colorIntAVector3d(0xb5bfc4), colorIntAVector3d(0xacb3bb), colorIntAVector3d(0xb4bbbe), colorIntAVector3d(0xb2bccd), colorIntAVector3d(0xbccdde), colorIntAVector3d(0xbbcad6), colorIntAVector3d(0xaea99f), colorIntAVector3d(0xa79c8c), colorIntAVector3d(0xc0b29b), colorIntAVector3d(0xbcbab5), colorIntAVector3d(0xb9b4a6), colorIntAVector3d(0xbdb4a4), colorIntAVector3d(0xada698), colorIntAVector3d(0xc7d0c7), colorIntAVector3d(0xb5c5cf), colorIntAVector3d(0xa0adbd), colorIntAVector3d(0xbecbd7), colorIntAVector3d(0xc4ccc5), colorIntAVector3d(0xc5d0c9), colorIntAVector3d(0xa6aeb0), colorIntAVector3d(0x858582), colorIntAVector3d(0x827169), colorIntAVector3d(0x827169), colorIntAVector3d(0x8c6f5d), colorIntAVector3d(0xa38a7e), colorIntAVector3d(0x9e826d), colorIntAVector3d(0xa17c5a), colorIntAVector3d(0xb3a78c), colorIntAVector3d(0xbab7a0), colorIntAVector3d(0xbabbb0), colorIntAVector3d(0xb7c5ce), colorIntAVector3d(0xaca89a), colorIntAVector3d(0xbea876), colorIntAVector3d(0xba9b64), colorIntAVector3d(0xbb9c6c), colorIntAVector3d(0xb8aa89), colorIntAVector3d(0xbbaf8e) });
        COLORES_GIGANTES_GAS.add(new Vector3d[] { colorIntAVector3d(0x85807c), colorIntAVector3d(0x958d80), colorIntAVector3d(0xa09174), colorIntAVector3d(0xa6967c), colorIntAVector3d(0xa79b81), colorIntAVector3d(0xaa9b84), colorIntAVector3d(0xa69180), colorIntAVector3d(0xa4927e), colorIntAVector3d(0x9c8978), colorIntAVector3d(0xa18e7f), colorIntAVector3d(0xa08a72), colorIntAVector3d(0xa89378), colorIntAVector3d(0xaa9f7f), colorIntAVector3d(0xa49979), colorIntAVector3d(0x9b9680) });
    }

    ;

    public static final Vector<Vector3d[]> COLORES_GIGANTES_HIELO;

    static {
        COLORES_GIGANTES_HIELO = new Vector<Vector3d[]>();
        COLORES_GIGANTES_HIELO.add(new Vector3d[] { colorIntAVector3d(0x56747e), colorIntAVector3d(0x56747e), colorIntAVector3d(0x9ed7e2), colorIntAVector3d(0xbeebf0), colorIntAVector3d(0x7fe0f0), colorIntAVector3d(0x5bd9ef), colorIntAVector3d(0x50d9ec), colorIntAVector3d(0x34b8d1), colorIntAVector3d(0xfbf7eb), colorIntAVector3d(0x2d9fba) });
    }

    ;

    public static final Vector3d[] COLORES_ANILLOS = { colorIntAVector3d(0x32312f), colorIntAVector3d(0x1a1819), colorIntAVector3d(0x544f4b), colorIntAVector3d(0x4b4340), colorIntAVector3d(0x564e4b), colorIntAVector3d(0x796c64), colorIntAVector3d(0x554e48), colorIntAVector3d(0x83766d), colorIntAVector3d(0x000000), colorIntAVector3d(0x887b72), colorIntAVector3d(0x61574d), colorIntAVector3d(0xccb39d), colorIntAVector3d(0x5e5141), colorIntAVector3d(0xccb193), colorIntAVector3d(0x544b3c), colorIntAVector3d(0x9a8f7d), colorIntAVector3d(0x000000), colorIntAVector3d(0x514946), colorIntAVector3d(0x2b2726), colorIntAVector3d(0x9c9288), colorIntAVector3d(0xccbaa2), colorIntAVector3d(0x695d4f), colorIntAVector3d(0x91806c), colorIntAVector3d(0x000000) };

    public static final Vector3d[] COLORES_SOLIDO_ARIDO = { colorIntAVector3d(0x807152), colorIntAVector3d(0x907155), colorIntAVector3d(0x806f51), colorIntAVector3d(0xaf8658), colorIntAVector3d(0xaf8356), colorIntAVector3d(0xefa25c), colorIntAVector3d(0xe49553), colorIntAVector3d(0xfecc6b) };

    private int histogramaNiveles[];

    private static Vector3d vectorTemp = new Vector3d();

    private static Vector3d vectorTemp2 = new Vector3d();

    private static Vector3d vectorTemp3 = new Vector3d();

    public ProcesadorTexturas() {
        histogramaNiveles = new int[256];
        funcionColoresPlanetasTerraqueos = new FuncionPorTramosVectorial();
        funcionColoresPlanetasTerraqueos.nuevoPunto(NIVEL_TERRAQUEO_SELVA, COLOR_TERRAQUEO_SELVA);
        funcionColoresPlanetasTerraqueos.nuevoPunto(NIVEL_TERRAQUEO_TIERRA, COLOR_TERRAQUEO_TIERRA);
        funcionColoresPlanetasTerraqueos.nuevoPunto(NIVEL_TERRAQUEO_HIELO, COLOR_TERRAQUEO_HIELO);
    }

    public void procesarTexturaTerraqueo(String pathTextura, DescriptorAstro descriptorAstro) {
        BufferedImage imgFuente = null;
        try {
            imgFuente = ImageIO.read(new File(pathTextura));
        } catch (IOException e) {
            log.Log.log("Error: IOException while loading earth-like texture " + pathTextura);
            return;
        }
        int width = imgFuente.getWidth();
        int height = imgFuente.getHeight();
        BufferedImage imgResult = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int h = 0; h < 256; h++) {
            histogramaNiveles[h] = 0;
        }
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int nivel = imgFuente.getRGB(i, j);
                nivel &= 0x000000FF;
                histogramaNiveles[nivel]++;
            }
        }
        for (int h = 1; h < 256; h++) {
            histogramaNiveles[h] += histogramaNiveles[h - 1];
        }
        int hBuscado = (int) (width * height * descriptorAstro.fraccionOceanos);
        int nivelDelMar = 0;
        while (nivelDelMar < 256 && histogramaNiveles[nivelDelMar] < hBuscado) {
            nivelDelMar++;
        }
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int nivel = imgFuente.getRGB(i, j);
                int colorFinal = nivel;
                nivel &= 0x000000FF;
                boolean oceano = false;
                if (nivel < nivelDelMar) {
                    colorFinal = COLOR_OCEANOS;
                    oceano = true;
                } else if (nivel == 255) {
                    colorFinal = COLOR_CASQUETES_POLARES;
                } else {
                    double x = (nivel - nivelDelMar) / (255.0d - nivelDelMar);
                    funcionColoresPlanetasTerraqueos.getValor(x, vectorTemp);
                    colorFinal = colorVector3dAInt(vectorTemp);
                }
                imgResult.setRGB(i, j, colorFinal);
            }
        }
        copiarPixelsBordeIzquierdoAlDerecho(imgResult);
        File fichResult = new File(pathTextura);
        try {
            ImageIO.write(imgResult, "PNG", fichResult);
        } catch (IOException e) {
            log.Log.log("Error: IOException while writing earth-like texture " + pathTextura);
        }
    }

    public void procesarTexturaGiganteGasOHielo(String pathTextura, DescriptorAstro descriptorAstro, boolean giganteGasNoHielo, Random random) {
        BufferedImage imgFuente = null;
        try {
            imgFuente = ImageIO.read(new File(pathTextura));
        } catch (IOException e) {
            log.Log.log("Error: IOException while loading gas giant texture " + pathTextura);
            return;
        }
        int width = imgFuente.getWidth();
        int height = imgFuente.getHeight();
        BufferedImage imgResult = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        FuncionPorTramosVectorial funcionColores = giganteGasNoHielo ? creaFuncionColoresBandas(50, 70, COLORES_GIGANTES_GAS.elementAt(random.nextInt(COLORES_GIGANTES_GAS.size())), null, random) : creaFuncionColoresBandas(50, 70, COLORES_GIGANTES_HIELO.elementAt(random.nextInt(COLORES_GIGANTES_HIELO.size())), null, random);
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int nivel = imgFuente.getRGB(i, j) & 0x000000FF;
                double x = nivel / 255.0d;
                funcionColores.getValor(x, vectorTemp);
                int colorFinal = colorVector3dAInt(vectorTemp);
                imgResult.setRGB(i, j, colorFinal);
            }
        }
        copiarPixelsBordeIzquierdoAlDerecho(imgResult);
        File fichResult = new File(pathTextura);
        try {
            ImageIO.write(imgResult, "PNG", fichResult);
        } catch (IOException e) {
            log.Log.log("Error: IOException while writing gas giant texture " + pathTextura);
        }
    }

    public void procesarTexturasSolidoRocoso(String pathTextura, String pathTexturaBump, DescriptorAstro descriptorAstro) {
        BufferedImage imgFuente = null;
        try {
            imgFuente = ImageIO.read(new File(pathTextura));
        } catch (IOException e) {
            log.Log.log("Error: IOException while loading rocky planet texture " + pathTextura);
            return;
        }
        int width = imgFuente.getWidth();
        int height = imgFuente.getHeight();
        BufferedImage imgResult = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage imgBump = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        generarTexturaBump(imgFuente, imgBump, -1);
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int nivel = imgFuente.getRGB(i, j) & 0x000000FF;
                double valor = nivel / 255.0d;
                vectorTemp.set(valor);
                imgResult.setRGB(i, j, colorVector3dAInt(vectorTemp));
            }
        }
        copiarPixelsBordeIzquierdoAlDerecho(imgResult);
        File fichResult = new File(pathTextura);
        try {
            ImageIO.write(imgResult, "PNG", fichResult);
        } catch (IOException e) {
            log.Log.log("Error: IOException while writing rocky planet texture " + pathTextura);
        }
        File fichBump = new File(pathTexturaBump);
        try {
            ImageIO.write(imgBump, "PNG", fichBump);
        } catch (IOException e) {
            log.Log.log("Error: IOException while writing rocky planet bump texture " + pathTexturaBump);
        }
    }

    public void procesarTexturaSolidoArido(String pathTextura, String pathTexturaBump, DescriptorAstro descriptorAstro, Random random) {
        BufferedImage imgFuente = null;
        try {
            imgFuente = ImageIO.read(new File(pathTextura));
        } catch (IOException e) {
            log.Log.log("Error: IOException while loading arid planet texture " + pathTextura);
            return;
        }
        int width = imgFuente.getWidth();
        int height = imgFuente.getHeight();
        BufferedImage imgResult = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage imgBump = null;
        if (pathTexturaBump != null) {
            imgBump = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            generarTexturaBump(imgFuente, imgBump, 255);
        }
        generarTexturaBump(imgFuente, imgBump, 255);
        FuncionPorTramosVectorial funcionColoresPlanetasAridos = new FuncionPorTramosVectorial();
        int nc = COLORES_SOLIDO_ARIDO.length;
        double d = 0.0d;
        for (int i = 0; i < nc; i++) {
            funcionColoresPlanetasAridos.nuevoPunto(d, COLORES_SOLIDO_ARIDO[i]);
            d += 0.4d + random.nextDouble();
        }
        funcionColoresPlanetasAridos.normalizarX();
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int nivel = imgFuente.getRGB(i, j) & 0x000000FF;
                if (nivel == 255) {
                    imgResult.setRGB(i, j, 0xffffff);
                } else {
                    double x = nivel / 255.0d;
                    funcionColoresPlanetasAridos.getValor(x, vectorTemp);
                    int colorFinal = colorVector3dAInt(vectorTemp);
                    imgResult.setRGB(i, j, colorFinal);
                }
            }
        }
        copiarPixelsBordeIzquierdoAlDerecho(imgResult);
        File fichResult = new File(pathTextura);
        try {
            ImageIO.write(imgResult, "PNG", fichResult);
        } catch (IOException e) {
            log.Log.log("Error: IOException while writing arid planet texture " + pathTextura);
        }
        if (pathTexturaBump != null) {
            File fichBump = new File(pathTexturaBump);
            try {
                ImageIO.write(imgBump, "PNG", fichBump);
            } catch (IOException e) {
                log.Log.log("Error: IOException while writing arid planet bump texture " + pathTexturaBump);
            }
        }
    }

    public void generarTexturaBump(BufferedImage imagenAlturas, BufferedImage imagenBump, int nivelIgnorar) {
        int width = imagenAlturas.getWidth();
        int height = imagenAlturas.getHeight();
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int nivel = imagenAlturas.getRGB(i, j) & 0x000000FF;
                int difX = 128;
                int difY = 128;
                if (nivelIgnorar < 0 || nivel != nivelIgnorar) {
                    int nivelXMas1 = imagenAlturas.getRGB((i + 1) % width, j) & 0x000000FF;
                    if (nivelIgnorar < 0 || nivelXMas1 != nivelIgnorar) {
                        difX = nivelXMas1 - nivel + 128;
                        if (difX < 0) {
                            difX = 0;
                        }
                        if (difX > 255) {
                            difX = 255;
                        }
                        int nivelYMas1 = nivel;
                        if (j < height - 1) {
                            nivelYMas1 = imagenAlturas.getRGB(i, j + 1) & 0x000000FF;
                        }
                        if (nivelIgnorar < 0 || nivelYMas1 != nivelIgnorar) {
                            difY = nivelYMas1 - nivel + 128;
                            if (difY < 0) {
                                difY = 0;
                            }
                            if (difY > 255) {
                                difY = 255;
                            }
                        }
                    }
                }
                imagenBump.setRGB(i, j, colorARGB(0, difX, difY, 0));
            }
        }
        copiarPixelsBordeIzquierdoAlDerecho(imagenBump);
    }

    public void generarTexturaAnillos(String pathTextura, int resolucion, DescriptorAstro descriptorAstro, Random random) {
        BufferedImage imgResult = new BufferedImage(resolucion, resolucion, BufferedImage.TYPE_INT_ARGB);
        int width = imgResult.getWidth();
        int height = imgResult.getHeight();
        FuncionPorTramosVectorial funcionColores = creaFuncionColoresBandas(50, 70, COLORES_ANILLOS, new Vector3d(), random);
        double ancho2 = resolucion / 2.0d;
        double limInterior = descriptorAstro.radioInteriorAnillos;
        double limExterior = descriptorAstro.radioExteriorAnillos;
        double rangoAnillos = limExterior - limInterior;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                double x = (i - ancho2);
                double y = (j - ancho2);
                double r = limExterior * Math.sqrt(x * x + y * y) / ancho2;
                double a = (r - limInterior) / rangoAnillos;
                funcionColores.getValor(a, vectorTemp);
                double alfa = (1.0d / 3.0d) * (vectorTemp.x + vectorTemp.y + vectorTemp.z);
                int colorFinal = colorVector3dAInt(vectorTemp, alfa);
                imgResult.setRGB(i, j, colorFinal);
            }
        }
        copiarPixelsBordeIzquierdoAlDerecho(imgResult);
        File fichResult = new File(pathTextura);
        try {
            ImageIO.write(imgResult, "PNG", fichResult);
        } catch (IOException e) {
            log.Log.log("Error: IOException while writing ring texture " + pathTextura);
        }
    }

    private static Vector3d rayo = new Vector3d();

    private static Vector3d origenRayo = new Vector3d();

    private static Vector3d incremSample = new Vector3d();

    public void generarTexturaDifusionAtmosferica(String pathTexturaAtmosfera, Astro astro) {
        int tam = 128;
        double tamd = (double) tam;
        int numPixeles = tam * tam;
        int numBytes = numPixeles * 4 * 4;
        ByteBuffer bufer = ByteBuffer.allocateDirect(numBytes);
        bufer.order(ByteOrder.nativeOrder());
        double filaAnterior[][] = new double[tam][4];
        VisualAstro visualAstro = (VisualAstro) astro.getObjeto3d();
        double radioPlaneta = visualAstro.radioAstro;
        double radioAtmosfera = radioPlaneta + visualAstro.alturaAtmosfera;
        double rangoAtmosfera = radioAtmosfera - radioPlaneta;
        double escalaAltura = 1.0d / rangoAtmosfera;
        double raleyghScaleHeight = 0.25d;
        double mieScaleHeight = 0.1d;
        double raleyghScaleHeightInv = 1.0d / raleyghScaleHeight;
        double mieScaleHeightInv = 1.0d / mieScaleHeight;
        int numSamples = 10;
        for (int anguloi = 0; anguloi < tam; anguloi++) {
            double cos = 1.0d - (anguloi + anguloi) / tamd;
            double angulo = Math.acos(cos);
            rayo.set(Math.sin(angulo), cos, 0.0d);
            for (int alturai = 0; alturai < tam; alturai++) {
                double altura = 1.0e-8d + radioPlaneta + rangoAtmosfera * ((double) alturai) / tamd;
                origenRayo.set(0.0d, altura, 0.0d);
                double rayleighDensityRatio;
                double mieDensityRatio;
                if (!Geometria.interseccionRayoEsfera(origenRayo, rayo, Vector3d.vectorNulo, radioPlaneta, vectorTemp)) {
                    rayleighDensityRatio = Math.exp(-(altura - radioPlaneta) * escalaAltura * raleyghScaleHeightInv);
                    mieDensityRatio = Math.exp(-(altura - radioPlaneta) * escalaAltura * mieScaleHeightInv);
                } else {
                    rayleighDensityRatio = filaAnterior[alturai][0] * 0.75d;
                    mieDensityRatio = filaAnterior[alturai][2] * 0.75d;
                }
                double b = 2.0d * origenRayo.y * rayo.y;
                double c = origenRayo.y * origenRayo.y - radioAtmosfera * radioAtmosfera;
                double d = b * b - 4.0d * c;
                if (d < 0.0) {
                    log.Log.log("Error: no intersection with atmosphere while calculating atmospheric scattering texture");
                    d = 0.0;
                }
                double recorridoRayo = 0.5d * (-b + Math.sqrt(d));
                double longitudSample = recorridoRayo / numSamples;
                double escalaSample = longitudSample * escalaAltura;
                incremSample.prodEscVector(longitudSample, rayo);
                origenRayo.increm(0.5d * incremSample.x, 0.5d * incremSample.y, 0.5d * incremSample.z);
                double rayleighDepth = 0.0d;
                double mieDepth = 0.0d;
                for (int sample = 0; sample < numSamples; sample++) {
                    double radioSample = origenRayo.modulo();
                    double alturaSample = Math.max(0.0d, (radioSample - radioPlaneta) * escalaAltura);
                    rayleighDepth += Math.exp(-alturaSample * raleyghScaleHeightInv);
                    mieDepth += Math.exp(-alturaSample * mieScaleHeightInv);
                    origenRayo.increm(incremSample);
                }
                rayleighDepth *= escalaSample;
                mieDepth *= escalaSample;
                bufer.putFloat((float) rayleighDensityRatio);
                bufer.putFloat((float) mieDensityRatio);
                bufer.putFloat((float) rayleighDepth);
                bufer.putFloat((float) mieDepth);
                filaAnterior[anguloi][0] = rayleighDensityRatio;
                filaAnterior[anguloi][1] = mieDensityRatio;
                filaAnterior[anguloi][2] = rayleighDepth;
                filaAnterior[anguloi][3] = mieDepth;
            }
        }
        bufer.rewind();
        try {
            FileOutputStream fos = new FileOutputStream(pathTexturaAtmosfera);
            try {
                for (int i = 0; i < numBytes; i++) {
                    fos.write(bufer.get());
                }
            } catch (IOException e) {
                log.Log.log("Error: IOException while writing atmospheric scattering texture " + pathTexturaAtmosfera);
            }
        } catch (FileNotFoundException e) {
            log.Log.log("Error: FileNotFoundException while writing atmospheric scattering texture " + pathTexturaAtmosfera);
        }
    }

    private void copiarPixelsBordeIzquierdoAlDerecho(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        for (int j = 0; j < height; j++) {
            img.setRGB(0, j, img.getRGB(width - 1, j));
        }
    }

    private FuncionPorTramosVectorial creaFuncionColoresBandas(int minColores, int maxColores, Vector3d[] colores, Vector3d colorExtremos, Random random) {
        FuncionPorTramosVectorial funcionColores = new FuncionPorTramosVectorial();
        int numColores = minColores + random.nextInt(maxColores - minColores);
        if (numColores > 0) {
            if (colorExtremos != null) {
                funcionColores.nuevoPunto(0.0d, colorExtremos);
            } else {
                funcionColores.nuevoPunto(0.0d, colorBanda(colores, random));
            }
            if (numColores > 1) {
                funcionColores.nuevoPunto(1.0d, colorBanda(colores, random));
                if (numColores > 2) {
                    double lat = 0.0d;
                    double dLat = 1.0d / (numColores - 2);
                    for (int i = 2; i < numColores - 1; i++) {
                        lat += dLat + random.nextDouble() * 4.0d;
                        funcionColores.nuevoPunto(lat, colorBanda(colores, random));
                    }
                    lat += dLat + random.nextDouble() * 4.0d;
                    if (colorExtremos != null) {
                        funcionColores.nuevoPunto(lat, colorExtremos);
                    } else {
                        funcionColores.nuevoPunto(lat, colorBanda(colores, random));
                    }
                }
            }
            funcionColores.normalizarX();
        }
        return funcionColores;
    }

    private Vector3d colorBanda(Vector3d[] colores, Random random) {
        return colores[random.nextInt(colores.length)];
    }

    public static int colorVector3dAInt(Vector3d color) {
        return (((int) (255.0d * color.x)) << 16) | (((int) (255.0d * color.y)) << 8) | ((int) (255.0d * color.z));
    }

    public static int colorVector3dAInt(Vector3d color, double alfa) {
        return (((int) (255.0d * alfa)) << 24) | (((int) (255.0d * color.x)) << 16) | (((int) (255.0d * color.y)) << 8) | ((int) (255.0d * color.z));
    }

    public static int colorARGB(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | (b);
    }

    public static void colorIntAVector3d(int rgb, Vector3d result) {
        result.set(((rgb & 0x00FF0000) >> 16) / 255.0d, ((rgb & 0x0000FF00) >> 8) / 255.0d, (rgb & 0x000000FF) / 255.0d);
    }

    public static Vector3d colorIntAVector3d(int rgb) {
        return new Vector3d(((rgb & 0x00FF0000) >> 16) / 255.0d, ((rgb & 0x0000FF00) >> 8) / 255.0d, (rgb & 0x000000FF) / 255.0d);
    }
}

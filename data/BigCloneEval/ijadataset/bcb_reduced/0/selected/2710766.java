package br.ufrj.nce.linkit;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.lang.Math;

public class Rel {

    int x;

    int y;

    int rxMeioTam;

    int ryMeioTam;

    int xOrigem;

    int yOrigem;

    int xDestino;

    int yDestino;

    int xOrigemOrig;

    int yOrigemOrig;

    int xDestinoOrig;

    int yDestinoOrig;

    boolean bDirecaoMesma;

    boolean bAcordado;

    boolean bContribui;

    Var varo;

    Var vard;

    double valor;

    public static final double valorMinimo = 0.0000000000000001;

    public static final double valorInfinito = 6;

    double forca;

    public static final double efeitoNormal = 1;

    public static final double efeitoFraco = 0.5;

    public static final double efeitoForte = 2;

    public static final double deltaT = 0.001;

    int tipoRel;

    public static final int relacionamentoTaxa = 1;

    public static final int relacionamentoProp = 2;

    int estado;

    public static final int normal = 0;

    public static final int arrastado = 1;

    public static final int novo = 2;

    Color corGraf;

    int red;

    int green;

    int blue;

    public Rel() {
        rxMeioTam = 10;
        ryMeioTam = 10;
        bDirecaoMesma = true;
        bAcordado = true;
        bContribui = false;
        forca = efeitoNormal;
        tipoRel = relacionamentoProp;
        valor = 0;
        estado = novo;
        corGraf = Color.black;
        red = 0;
        green = 0;
        blue = 0;
    }

    public void desenhaRel(Graphics2D g2, String estadoMod) {
        testaCorArrasto(g2, estadoMod);
        g2.drawLine(xOrigem, yOrigem, x, y);
        g2.drawLine(x, y, xDestino, yDestino);
        formaSimbolo(g2, estadoMod);
        formaSeta(g2);
    }

    public void formaSimbolo(Graphics2D g2, String estadoMod) {
        g2.setPaint(Color.white);
        if (tipoRel == relacionamentoTaxa) {
            g2.fillOval(x - rxMeioTam, y - ryMeioTam, rxMeioTam * 2, ryMeioTam * 2);
            testaCorArrasto(g2, estadoMod);
            if (forca == efeitoFraco) g2.setStroke(new BasicStroke(1));
            if (forca == efeitoNormal) g2.setStroke(new BasicStroke(2));
            if (forca == efeitoForte) g2.setStroke(new BasicStroke(3));
            g2.drawOval(x - rxMeioTam, y - ryMeioTam, rxMeioTam * 2, ryMeioTam * 2);
        } else {
            g2.fillRect(x - rxMeioTam, y - ryMeioTam, rxMeioTam * 2, ryMeioTam * 2);
            testaCorArrasto(g2, estadoMod);
            if (forca == efeitoFraco) g2.setStroke(new BasicStroke(1));
            if (forca == efeitoNormal) g2.setStroke(new BasicStroke(2));
            if (forca == efeitoForte) g2.setStroke(new BasicStroke(3));
            g2.drawRect(x - rxMeioTam, y - ryMeioTam, rxMeioTam * 2, ryMeioTam * 2);
        }
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(x - 2, y + 6, x - 2, y - 6);
        g2.drawLine(x - 2, y - 6, x - 2 - 4, y - 6 + 4);
        if (bDirecaoMesma) {
            g2.drawLine(x + 1, y + 6, x + 1, y - 6);
            g2.drawLine(x + 1, y - 6, x + 1 + 4, y - 6 + 4);
        } else {
            g2.drawLine(x + 1, y - 6, x + 1, y + 6);
            g2.drawLine(x + 1, y + 6, x + 1 + 4, y + 6 - 4);
        }
    }

    public void formaSeta(Graphics2D g2) {
        Ponto ponto = new Ponto();
        ponto = ponto.pontoExtremidadeSeta(Math.PI / 9, xDestino, yDestino, x, y);
        g2.drawLine(ponto.x, ponto.y, xDestino, yDestino);
        ponto = ponto.pontoExtremidadeSeta(-(Math.PI / 9), xDestino, yDestino, x, y);
        g2.drawLine(xDestino, yDestino, ponto.x, ponto.y);
    }

    public void testaCorArrasto(Graphics2D g2, String estadoMod) {
        if (((estado == arrastado) || (varo.estado == varo.eArrastarCaixa) || (vard.estado == vard.eArrastarCaixa)) && estadoMod.equals("eArrastando")) {
            g2.setColor(new Color(200, 200, 200));
        } else g2.setColor(corGraf);
    }

    public void relacionar(int clipTop, int clipBot, int clipLeft, int clipRig) {
        if (estado == novo) {
            x = (xOrigemOrig + xDestinoOrig) / 2;
            y = (yOrigemOrig + yDestinoOrig) / 2;
        }
        calcularLimites();
        if (estado == novo) {
            x = (xOrigem + xDestino) / 2;
            y = (yOrigem + yDestino) / 2;
            corrigePosicao(clipTop, clipBot, clipLeft, clipRig);
        }
    }

    public void corrigePosicao(int clipTop, int clipBot, int clipLeft, int clipRig) {
        if ((y - ryMeioTam) < clipTop) {
            y = clipTop + ryMeioTam;
        }
        if ((y + ryMeioTam) > clipBot) {
            y = clipBot - ryMeioTam;
        }
        if ((x - rxMeioTam) < clipLeft) {
            x = clipLeft + rxMeioTam;
        }
        if ((x + rxMeioTam) > clipRig) {
            x = clipRig - rxMeioTam;
        }
    }

    public void calcularLimites() {
        calcularOrigem();
        calcularDestino();
    }

    public void calcularOrigem() {
        double a1, b1, a2, b2;
        a1 = (double) (varo.yMeioTam + varo.yMeioTam) / (varo.xMeioTam + varo.xMeioTam);
        b1 = (double) (yOrigemOrig - varo.yMeioTam) - a1 * (xOrigemOrig - varo.xMeioTam);
        a2 = -a1;
        b2 = (double) (yOrigemOrig - varo.yMeioTam) - a2 * (xOrigemOrig + varo.xMeioTam);
        int v1 = (int) (a1 * x + b1);
        int v2 = (int) (a2 * x + b2);
        if ((y < (int) (a1 * x + b1)) && (y < (int) (a2 * x + b2))) {
            xOrigem = xOrigemOrig;
            yOrigem = yOrigemOrig - varo.yMeioTam - varo.yNome;
        }
        if ((y < (int) (a1 * x + b1)) && (y >= (int) (a2 * x + b2))) {
            xOrigem = xOrigemOrig + varo.xMeioTam;
            yOrigem = yOrigemOrig;
        }
        if ((y >= (int) (a1 * x + b1)) && (y < (int) (a2 * x + b2))) {
            xOrigem = xOrigemOrig - varo.xMeioTam;
            yOrigem = yOrigemOrig;
        }
        if ((y >= (int) (a1 * x + b1)) && (y >= (int) (a2 * x + b2))) {
            xOrigem = xOrigemOrig;
            yOrigem = yOrigemOrig + varo.yMeioTam;
        }
    }

    public void calcularDestino() {
        double a1, b1, a2, b2;
        a1 = (double) (vard.yMeioTam + vard.yMeioTam) / (vard.xMeioTam + vard.xMeioTam);
        b1 = (double) (yDestinoOrig - vard.yMeioTam) - a1 * (xDestinoOrig - vard.xMeioTam);
        a2 = -a1;
        b2 = (double) (yDestinoOrig - vard.yMeioTam) - a2 * (xDestinoOrig + vard.xMeioTam);
        if ((y < (a1 * x + b1)) && (y < (a2 * x + b2))) {
            xDestino = xDestinoOrig;
            yDestino = yDestinoOrig - vard.yMeioTam - vard.yNome;
        }
        if ((y < (a1 * x + b1)) && (y >= (a2 * x + b2))) {
            xDestino = xDestinoOrig + vard.xMeioTam;
            yDestino = yDestinoOrig;
        }
        if ((y >= (a1 * x + b1)) && (y < (a2 * x + b2))) {
            xDestino = xDestinoOrig - vard.xMeioTam;
            yDestino = yDestinoOrig;
        }
        if ((y >= (a1 * x + b1)) && (y >= (a2 * x + b2))) {
            xDestino = xDestinoOrig;
            yDestino = yDestinoOrig + vard.yMeioTam;
        }
    }

    public void calcularValor(Var varo, Var vard) {
        bContribui = bAcordado && varo.bAcordada;
        if (varo.tipo == "VarLigaDesliga") {
            bContribui = (bContribui && varo.bLigada());
        }
        if (bContribui) {
            valor = forca * varo.getValor();
            if (!bDirecaoMesma) {
                if (vard.tipoCombinacao == 1 | vard.tipoCombinacao == 3) {
                    valor = -valor;
                }
                if (vard.tipoCombinacao == 2) {
                    if (valor < 0 & (valor > -valorMinimo)) {
                        valor = -valorInfinito;
                    } else {
                        if ((valor >= 0) & (valor < valorMinimo)) {
                            valor = valorInfinito;
                        } else {
                            valor = 1 / valor;
                        }
                    }
                }
            }
        }
    }

    public void atualizarRelOrig(Var vart) {
        varo = vart;
        xOrigemOrig = varo.x;
        yOrigemOrig = varo.y;
        xOrigem = varo.x;
        yOrigem = varo.y;
    }

    public void atualizarRelDest(Var vart) {
        vard = vart;
        xDestinoOrig = vard.x;
        yDestinoOrig = vard.y;
        xDestino = vard.x;
        yDestino = vard.y;
    }

    public void setPosicao(int posX, int posY, int clipTop, int clipBot, int clipLeft, int clipRig) {
        x = posX;
        y = posY;
        corrigePosicao(clipTop, clipBot, clipLeft, clipRig);
    }

    public void setaAtributos(String linha, int numLinRel) {
        if (numLinRel == 1) {
            x = Integer.parseInt(linha);
        }
        if (numLinRel == 2) {
            y = Integer.parseInt(linha);
        }
        if (numLinRel == 3) {
            xOrigem = Integer.parseInt(linha);
        }
        if (numLinRel == 4) {
            yOrigem = Integer.parseInt(linha);
        }
        if (numLinRel == 5) {
            xDestino = Integer.parseInt(linha);
        }
        if (numLinRel == 6) {
            yDestino = Integer.parseInt(linha);
        }
        if (numLinRel == 7) {
            xOrigemOrig = Integer.parseInt(linha);
        }
        if (numLinRel == 8) {
            yOrigemOrig = Integer.parseInt(linha);
        }
        if (numLinRel == 9) {
            xDestinoOrig = Integer.parseInt(linha);
        }
        if (numLinRel == 10) {
            yDestinoOrig = Integer.parseInt(linha);
        }
        if (numLinRel == 11) {
            tipoRel = Integer.parseInt(linha);
        }
        if (numLinRel == 12 && linha.equals("DIRECAO: MESMA")) {
            bDirecaoMesma = true;
        }
        if (numLinRel == 12 && linha.equals("DIRECAO: OPOSTA")) {
            bDirecaoMesma = false;
        }
        if (numLinRel == 13 && linha.equals("ACORDADO")) {
            bAcordado = true;
        }
        if (numLinRel == 13 && linha.equals("DORMINDO")) {
            bAcordado = false;
        }
        if (numLinRel == 14) {
            forca = Double.parseDouble(linha);
        }
        if (numLinRel == 15) {
            red = Integer.parseInt(linha);
        }
        if (numLinRel == 16) {
            green = Integer.parseInt(linha);
        }
        if (numLinRel == 17) {
            blue = Integer.parseInt(linha);
            corGraf = new Color(red, green, blue);
        }
    }
}

package com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.swing.JProgressBar;

/**
 *
 * @author manchini
 */
public class DiretorioUtils {

    private static int qtdArqLido = 0;

    public static void main(String[] args) {
    }

    public static void copyDirectory(File srcDir, File dstDir, Integer totalArq, JProgressBar jProgressBar) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            String[] children = srcDir.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]), totalArq, jProgressBar);
            }
        } else {
            copyFile(srcDir, dstDir);
            qtdArqLido++;
            jProgressBar.setValue(qtdArqLido * 100 / totalArq);
            jProgressBar.setString("Atualizando Sistema " + (qtdArqLido * 100 / totalArq) + "%");
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static int calculaQuantidadeArquivos(ArrayList<File> diretorio) {
        int count = 0;
        for (int i = 0; i < diretorio.size(); i++) {
            count += calculaQuantidadeArquivosDir(diretorio.get(i));
        }
        return count;
    }

    private static int calculaQuantidadeArquivosDir(File diretorio) {
        File[] arquivos = diretorio.listFiles();
        int cont = arquivos.length;
        for (int i = 0; i < arquivos.length; i++) {
            if (arquivos[i].isDirectory()) {
                cont += calculaQuantidadeArquivosDir(arquivos[i]);
            }
        }
        return cont;
    }

    public static int calculaQuantidadeArquivos(File diretorio) {
        if (!diretorio.isDirectory()) {
            return 1;
        }
        int cont = 0;
        File[] arquivos = diretorio.listFiles();
        for (int i = 0; i < arquivos.length; i++) {
            if (arquivos[i].isDirectory()) {
                cont += calculaQuantidadeArquivos(arquivos[i]);
            } else {
                cont++;
            }
        }
        return cont;
    }
}

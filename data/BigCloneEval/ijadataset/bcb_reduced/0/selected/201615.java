package exercicio33;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Compactador implements Runnable {

    private boolean running = true;

    private Buffer buf;

    private String name;

    public Compactador(Buffer buffer) {
        this.buf = buffer;
    }

    public Compactador(Buffer buf2, String nome) {
        this(buf2);
        name = nome;
    }

    public void run() {
        String nomeArquivo;
        while (running) {
            while (buf.getQuantidadeArquivos() <= 0 && running) {
                try {
                    System.out.println("Buffer vazio, aguardando...");
                    synchronized (this) {
                        wait(2000);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("N�o conseguiu fazer o wait.");
                }
            }
            nomeArquivo = buf.getProximoArquivo();
            if (nomeArquivo != null) {
                System.out.println("Compactando arquivo.");
                compactar(nomeArquivo);
            }
        }
    }

    public static String exibeEmHoras(long l) {
        String segundos, minutos, horas, milis, stringTime = "" + l;
        segundos = "" + (l / 1000);
        minutos = "" + (l / 1000) / 60;
        horas = "" + ((l / 1000) / 60) / 60;
        milis = ((l < 1000) ? stringTime : stringTime.substring(stringTime.length() - 3, stringTime.length()));
        return horas + ":" + minutos + ":" + segundos + "." + milis;
    }

    private void compactar(String nomeArquivo) {
        File arquivo, novoArquivo;
        FileInputStream fis;
        ZipOutputStream zos;
        byte[] dadosArquivo = new byte[2048];
        arquivo = new File(nomeArquivo);
        if (arquivo.exists()) {
            try {
                fis = new FileInputStream(arquivo);
                novoArquivo = new File(nomeArquivo + ".zip");
                if (novoArquivo.createNewFile()) {
                    novoArquivo.delete();
                    novoArquivo.createNewFile();
                }
                System.out.println(novoArquivo.getName() + "....");
                zos = new ZipOutputStream(new FileOutputStream(novoArquivo));
                zos.putNextEntry(new ZipEntry(arquivo.getName()));
                while (fis.read(dadosArquivo) > 0) {
                    zos.write(dadosArquivo);
                    zos.flush();
                }
                fis.close();
                zos.close();
                System.out.println("OK!");
            } catch (FileNotFoundException e) {
                System.out.println("ERRO!");
                System.err.println("Erro ao compactar ou ler arquivo " + nomeArquivo + ".");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("ERRO!");
                System.err.println("Erro ao escrever ou ler dados do arquivo " + nomeArquivo + ".");
                e.printStackTrace();
            }
        } else {
            System.err.println("Arquivo " + nomeArquivo + " n�o existe.");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}

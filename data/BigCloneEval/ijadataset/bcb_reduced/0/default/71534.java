import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Compactador implements Runnable {

    private String nomeArquivo;

    public Compactador(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public void run() {
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
                System.out.print(novoArquivo.getName() + "....");
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

    public static String exibeEmHoras(long l) {
        String segundos, minutos, horas, milis, stringTime = "" + l;
        segundos = "" + (l / 1000);
        minutos = "" + (l / 1000) / 60;
        horas = "" + ((l / 1000) / 60) / 60;
        milis = ((l < 1000) ? stringTime : stringTime.substring(stringTime.length() - 3, stringTime.length()));
        return horas + ":" + minutos + ":" + segundos + "." + milis;
    }
}

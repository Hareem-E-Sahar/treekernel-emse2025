package agex.yahooFinance;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.Vector;
import agex.to.DayTransactionsPaper;
import utils.Dates;
import utils.TextParser;

/**
 * Classe que faz parsing de arquivos no formato disponibilizado
 *  pelo Yahoo Finance (finance.yahoo.com)
 *  Formato tipo 1:
 *  Date,Open,High,Low,Close,Volume,Adj Close
 *  1-Sep-06,45.21,46.87,45.20,46.72,1323000,46.72
 *  
 *  
 */
public class YFParserFormat1 implements Parser {

    private Vector<DayTransactionsPaper> negociacoes = new Vector<DayTransactionsPaper>();

    public Vector getNegociacoesDiaTO() {
        return negociacoes;
    }

    int moeda;

    /**
	 * 
	 */
    public Vector<DayTransactionsPaper> parse(String idPapel, String fileName, int moeda) throws Exception {
        LineNumberReader in = new LineNumberReader(new FileReader(fileName));
        this.moeda = moeda;
        String aux;
        in.readLine();
        do {
            aux = in.readLine();
            if (aux != null) negociacoes.add(parseDia(idPapel, aux));
        } while (aux != null);
        return negociacoes;
    }

    /***
        * Campos separados por virgulas na seguinte ordem:
        *  data (dd-MM-YY), open, high, low, close, volume, adj. close(nao utilizado)
        */
    private DayTransactionsPaper parseDia(String idPapel, String reg) {
        double aber, fech, med, min, max;
        int tit;
        if (reg == null) return null;
        int start = 0;
        int end = reg.indexOf(",", start);
        Date data = Dates.ParseDateDDMMMYY(reg.substring(start, end));
        start = end + 1;
        end = reg.indexOf(",", start);
        aber = Double.parseDouble(reg.substring(start, end));
        start = end + 1;
        end = reg.indexOf(",", start);
        max = Double.parseDouble(reg.substring(start, end));
        start = end + 1;
        end = reg.indexOf(",", start);
        min = Double.parseDouble(reg.substring(start, end));
        med = (max + min) / 2;
        start = end + 1;
        end = reg.indexOf(",", start);
        fech = Double.parseDouble(reg.substring(start, end));
        start = end + 1;
        end = reg.indexOf(",", start);
        tit = TextParser.getInteger(reg.substring(start, end));
        return new DayTransactionsPaper(idPapel, data, aber, fech, max, med, min, tit, moeda);
    }

    public static void main(String[] args) throws Exception {
        YFParserFormat1 par = new YFParserFormat1();
        DayTransactionsPaper neg = par.parseDia("AAPL", "1-Sep-06,45.21,46.87,45.20,46.72,1323000,46.72");
        System.out.println("N=" + neg);
    }
}

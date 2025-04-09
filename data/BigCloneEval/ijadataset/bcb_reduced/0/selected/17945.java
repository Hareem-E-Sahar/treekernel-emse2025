package agex.yahooFinance;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import agex.to.DayTransactionsPaper;
import utils.Dates;
import utils.TextParser;

/**
 * Classe que faz parsing de arquivos no formato disponibilizado
 *  pelo Yahoo Finance (finance.yahoo.com)
 *  Especificacao de formato tipo 2;
 *  Date,Open,High,Low,Close,Volume,Adj Close
*   2008-08-28,175.28,176.25,172.75,173.74,15394500,173.74

 *  
 */
public class YFParserFormat2 implements Parser {

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

    public Date parseDateFormat2(String date) {
        if (date == null || date.length() < 8) return null;
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, day, 0, 0, 0);
        return cal.getTime();
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
        Date data = parseDateFormat2(reg.substring(start, end));
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
        YFParserFormat2 par = new YFParserFormat2();
        DayTransactionsPaper neg = par.parseDia("AAPL", "2008-08-28,175.28,176.25,172.75,173.74,15394500,173.74");
        System.out.println("Dados=" + neg);
    }
}

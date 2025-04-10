package org.alfredlibrary.utilitarios.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.alfredlibrary.AlfredException;

/**
 * Utilit�rio para arquivos do tipo CSV.
 * 
 * @author Marlon Silva Carvalho
 * @since 04/06/2009
 */
public final class CSVReader {

    private CSVReader() {
        throw new AssertionError();
    }

    /**
	 * L� um arquivo CSV de um local e interpreta.
	 * Retorna um Map onde o cabe�alho forma as chaves do Map.
	 * 
	 * @param u URL do arquivo CSV.
	 * @return Mapa.
	 */
    public static Collection<Map<String, String>> interpretar(String u) {
        URL url;
        Collection<Map<String, String>> c = new ArrayList<Map<String, String>>();
        try {
            url = new URL(u);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            boolean primeiraLinha = true;
            String[] keys = null;
            while ((line = rd.readLine()) != null) {
                if (line.trim().charAt(0) == '#') continue;
                if (line.trim().split(",").length == 0) continue;
                if (primeiraLinha) {
                    primeiraLinha = false;
                    CSV csv = new CSV();
                    csv.split(line);
                    keys = new String[csv.getNField() - 1];
                    for (int i = 0; i < csv.getNField() - 1; i++) keys[i] = csv.getField(i);
                } else {
                    CSV csv = new CSV();
                    Map<String, String> retorno = new HashMap<String, String>();
                    csv.split(line);
                    for (int i = 0; i < csv.getNField() - 1; i++) {
                        retorno.put(keys[i], csv.getField(i));
                    }
                    c.add(retorno);
                }
            }
            rd.close();
            return c;
        } catch (MalformedURLException e) {
            throw new AlfredException("N�o foi poss�vel obter contato com o site " + u, e);
        } catch (IOException e) {
            throw new AlfredException("N�o foi poss�vel obter contato com o site " + u, e);
        }
    }
}

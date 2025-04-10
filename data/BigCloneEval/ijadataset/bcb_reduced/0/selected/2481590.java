package com.esri.gpt.catalog.lucene;

import com.esri.gpt.framework.util.Val;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import org.apache.lucene.queryParser.ParseException;

/**
 * Standard network parser adaptor.
 * <p/>
 * Provides mechanizm to delegate query parsing to the servlet on the network.
 */
public class StandardNetworkParserAdaptor implements IParserProxy {

    /** base url */
    private String baseUrl = "";

    /**
   * Gets base URL.
   * @return base URL
   */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
   * Sets base URL.
   * @param baseUrl base URL
   */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = Val.chkStr(baseUrl);
    }

    /**
   * Initializes adaptor.
   * <p/>
   * Reads <i>baseUrl</i> property from the properties.
   * @param properties properties
   */
    public void init(Properties properties) {
        setBaseUrl(properties.getProperty("baseUrl", ""));
    }

    public String parse(String queryText) throws ParseException {
        try {
            StringBuilder sb = new StringBuilder();
            queryText = Val.chkStr(queryText);
            if (queryText.length() > 0) {
                URL url = new URL(getUrl(queryText));
                InputStream in = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (sb.length() > 0) {
                        sb.append("\r\n");
                    }
                    sb.append(line);
                }
            }
            return sb.toString();
        } catch (IOException ex) {
            throw new ParseException("Ontology parser is unable to parse term: \"" + queryText + "\" due to internal error: " + ex.getMessage());
        }
    }

    /**
   * Gets connection URL.
   * @param term term to search
   * @return connection URL
   */
    private String getUrl(String term) {
        try {
            term = Val.chkStr(term);
            term = URLEncoder.encode(term, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return baseUrl + term;
    }
}

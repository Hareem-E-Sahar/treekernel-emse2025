package nz.org.venice.quote;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.MalformedURLException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import nz.org.venice.prefs.PreferencesManager;
import nz.org.venice.util.Find;
import nz.org.venice.util.Locale;
import nz.org.venice.util.Report;
import nz.org.venice.util.TradingDate;

/**
 * Import end-of-day quotes from float.com.au into Venice.
 *
 * @author Andrew Leppard
 * @see ImportQuoteModule
 */
public class FloatEODQuoteImport {

    private static final String DAY = "_DAY_";

    private static final String MONTH = "_MONTH_";

    private static final String YEAR = "_YEAR_";

    private static final String URL_PATTERN = ("http://float.com.au/download/_YEAR__MONTH__DAY_.txt");

    private FloatEODQuoteImport() {
        assert false;
    }

    public static List importDate(Report report, TradingDate date) throws ImportExportException {
        List quotes = new ArrayList();
        String urlString = constructURL(date);
        EODQuoteFilter filter = new MetaStockQuoteFilter();
        PreferencesManager.ProxyPreferences proxyPreferences = PreferencesManager.getProxySettings();
        try {
            URL url = new URL(urlString);
            InputStreamReader input = new InputStreamReader(url.openStream());
            BufferedReader bufferedInput = new BufferedReader(input);
            String line = null;
            do {
                line = bufferedInput.readLine();
                if (line != null) {
                    try {
                        EODQuote quote = filter.toEODQuote(line);
                        quotes.add(quote);
                        verify(report, quote);
                    } catch (QuoteFormatException e) {
                        report.addError(Locale.getString("DFLOAT_DISPLAY_URL") + ":" + date + ":" + Locale.getString("ERROR") + ": " + e.getMessage());
                    }
                }
            } while (line != null);
            bufferedInput.close();
        } catch (BindException e) {
            throw new ImportExportException(Locale.getString("UNABLE_TO_CONNECT_ERROR", e.getMessage()));
        } catch (ConnectException e) {
            throw new ImportExportException(Locale.getString("UNABLE_TO_CONNECT_ERROR", e.getMessage()));
        } catch (UnknownHostException e) {
            throw new ImportExportException(Locale.getString("UNKNOWN_HOST_ERROR", e.getMessage()));
        } catch (NoRouteToHostException e) {
            throw new ImportExportException(Locale.getString("DESTINATION_UNREACHABLE_ERROR", e.getMessage()));
        } catch (MalformedURLException e) {
            throw new ImportExportException(Locale.getString("INVALID_PROXY_ERROR", proxyPreferences.host, proxyPreferences.port));
        } catch (FileNotFoundException e) {
            report.addError(Locale.getString("FLOAT_DISPLAY_URL") + ":" + date + ":" + Locale.getString("ERROR") + ": " + Locale.getString("NO_QUOTES_FOUND"));
        } catch (IOException e) {
            throw new ImportExportException(Locale.getString("ERROR_DOWNLOADING_QUOTES"));
        }
        return quotes;
    }

    /**
     * Construct the URL necessary to retrieve all the quotes for the given date from
     * float.com.au.
     *
     * @param date the date to retrieve
     * @return URL string
     */
    private static String constructURL(TradingDate date) {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        format.setMinimumIntegerDigits(2);
        String urlString = URL_PATTERN;
        urlString = Find.replace(urlString, DAY, format.format(date.getDay()));
        urlString = Find.replace(urlString, MONTH, format.format(date.getMonth()));
        format.setMinimumIntegerDigits(4);
        urlString = Find.replace(urlString, YEAR, format.format(date.getYear()));
        return urlString;
    }

    /**
     * Verify the quote is valid. Log any problems to the report and try to clean
     * it up the best we can.
     *
     * @param report the report
     * @param quote the quote
     */
    private static void verify(Report report, EODQuote quote) {
        try {
            quote.verify();
        } catch (QuoteFormatException e) {
            List messages = e.getMessages();
            for (Iterator iterator = messages.iterator(); iterator.hasNext(); ) {
                String message = (String) iterator.next();
                report.addWarning(Locale.getString("FLOAT_DISPLAY_URL") + ":" + quote.getSymbol() + ":" + quote.getDate() + ":" + Locale.getString("WARNING") + ": " + message);
            }
        }
    }
}

package net.sipvip.server.services.domain.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import org.json.JSONException;
import com.google.inject.Inject;
import com.sin.createcrcontext.CreateCrContext;
import com.sin.createcrcontext.CreateCrContextImpl;
import net.sipvip.server.services.domain.inte.CrwService;

public class CrwServiceImpl implements CrwService {

    private static final Logger log = Logger.getLogger(CrwServiceImpl.class.getName());

    private CreateCrContext createCrContext;

    private String outstr;

    @Inject
    public CrwServiceImpl(CreateCrContext createCrContext) {
        this.createCrContext = createCrContext;
    }

    @Override
    public String getOutStr(String urlstrMain, String urlStrRC, String User_Agent, String locale, String themes, String domain, String pathinfo) throws JSONException {
        HttpURLConnection connection = null;
        BufferedReader rd = null;
        StringBuilder sb = null;
        try {
            URL url = new URL(urlstrMain);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(20000);
            connection.setRequestProperty("User_Agent", "Robot/Spider");
            connection.setRequestProperty("locale", "fi_FI");
            connection.setRequestProperty("themes", "porno");
            connection.setRequestProperty("domain", domain);
            connection.setRequestProperty("pathinfo", pathinfo);
            rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF8"));
            sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            outstr = createCrContext.makeFrom(domain, pathinfo, sb.toString());
        } catch (IOException e) {
            log.severe(e.getMessage());
        } finally {
            connection.disconnect();
            rd = null;
            sb = null;
        }
        return outstr;
    }
}

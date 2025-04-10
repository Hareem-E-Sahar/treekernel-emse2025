package org.dllearner.algorithm.tbsl.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBpediaSpotlightNER implements NER {

    private static final String SERVICE_URL = "http://spotlight.dbpedia.org/rest/annotate?";

    private static final double CONFIDENCE = 0.4;

    private static final int SUPPORT = 20;

    public DBpediaSpotlightNER() {
    }

    @Override
    public List<String> getNamedEntitites(String sentence) {
        List<String> namedEntities = new ArrayList<String>();
        try {
            URL url = new URL(SERVICE_URL + "text=" + URLEncoder.encode(sentence, "UTF-8") + "&confidence=" + CONFIDENCE + "&support=" + SUPPORT);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("accept", "application/json");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            JSONObject json = new JSONObject(sb.toString());
            if (!json.isNull("Resources")) {
                JSONArray array = json.getJSONArray("Resources");
                JSONObject entityObject;
                for (int i = 0; i < array.length(); i++) {
                    entityObject = array.getJSONObject(i);
                    System.out.println("Entity: " + entityObject.getString("@surfaceForm"));
                    System.out.println("DBpedia URI: " + entityObject.getString("@URI"));
                    System.out.println("Types: " + entityObject.getString("@types"));
                    namedEntities.add(entityObject.getString("@surfaceForm"));
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return namedEntities;
    }
}

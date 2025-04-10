package info.clockworksapple.android.barsearch.common;

import info.clockworksapple.android.barsearch.relax.result.bar.Shop;
import info.clockworksapple.android.barsearch.relax.result.bar.Shops;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;
import android.content.res.Resources;

/**
 * @author ibmpck62u
 * 
 */
public class BarNaviUtil {

    private Resources resources;

    /**
     * @param resources
     */
    public BarNaviUtil(Resources resources) {
        this.resources = resources;
    }

    public Shop[] getShopByStation(String prefecture, String station, String barType) {
        HttpClient httpclient = null;
        Shop[] shop = null;
        try {
            httpclient = new DefaultHttpClient();
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("key", "d229542ed774c5df5e4b239faa80fbb2e2fbb0578f95a5154a2b3b75a6bf51ea"));
            qparams.add(new BasicNameValuePair("pattern", "0"));
            qparams.add(new BasicNameValuePair("pref", getPrefsNum(prefecture)));
            qparams.add(new BasicNameValuePair("access", station + resources.getString(info.clockworksapple.android.barsearch.R.string.str_station)));
            qparams.add(new BasicNameValuePair("count", "50"));
            qparams.add(new BasicNameValuePair("url", "www.clockworksapple.info"));
            if (barType.equals("1")) {
                qparams.add(new BasicNameValuePair("type", "1"));
                qparams.add(new BasicNameValuePair("type", "2"));
                qparams.add(new BasicNameValuePair("type", "3"));
                qparams.add(new BasicNameValuePair("type", "4"));
                qparams.add(new BasicNameValuePair("type", "5"));
                qparams.add(new BasicNameValuePair("type", "6"));
                qparams.add(new BasicNameValuePair("type", "7"));
                qparams.add(new BasicNameValuePair("type", "8"));
                qparams.add(new BasicNameValuePair("type", "10"));
                qparams.add(new BasicNameValuePair("type", "16"));
                qparams.add(new BasicNameValuePair("type", "17"));
            } else if (barType.equals("2")) {
                qparams.add(new BasicNameValuePair("type", "9"));
                qparams.add(new BasicNameValuePair("type", "11"));
            } else if (barType.equals("3")) {
                qparams.add(new BasicNameValuePair("type", "12"));
                qparams.add(new BasicNameValuePair("type", "13"));
                qparams.add(new BasicNameValuePair("type", "14"));
                qparams.add(new BasicNameValuePair("type", "15"));
            } else if (barType.equals("4")) {
                qparams.add(new BasicNameValuePair("type", "18"));
            } else if (barType.equals("0")) {
                qparams.add(new BasicNameValuePair("type", "0"));
            }
            URI uri = URIUtils.createURI("http", "webapi.suntory.co.jp", -1, "/barnavi/v2/shops", URLEncodedUtils.format(qparams, "UTF-8"), null);
            HttpGet httpget = new HttpGet(uri);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                buf.append(str);
                buf.append(System.getProperty("line.separator"));
            }
            reader.close();
            Shops shops = new Shops(buf.toString());
            shop = shops.getShop();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (ClientProtocolException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return shop;
    }

    /**
     * 
     * @param latitude
     * @param longitude
     * @param barType
     * @return
     */
    public Shop[] getShopByLocation(double latitude, double longitude, String barType) {
        Shop[] shop = null;
        HttpClient httpclient = null;
        try {
            httpclient = new DefaultHttpClient();
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("key", "d229542ed774c5df5e4b239faa80fbb2e2fbb0578f95a5154a2b3b75a6bf51ea"));
            qparams.add(new BasicNameValuePair("pattern", "1"));
            qparams.add(new BasicNameValuePair("lat", String.valueOf(latitude)));
            qparams.add(new BasicNameValuePair("lng", String.valueOf(longitude)));
            qparams.add(new BasicNameValuePair("range", "500"));
            qparams.add(new BasicNameValuePair("datum", "world"));
            qparams.add(new BasicNameValuePair("count", "50"));
            qparams.add(new BasicNameValuePair("url", "www.clockworksapple.info"));
            if (barType.equals("1")) {
                qparams.add(new BasicNameValuePair("type", "1"));
                qparams.add(new BasicNameValuePair("type", "2"));
                qparams.add(new BasicNameValuePair("type", "3"));
                qparams.add(new BasicNameValuePair("type", "4"));
                qparams.add(new BasicNameValuePair("type", "5"));
                qparams.add(new BasicNameValuePair("type", "6"));
                qparams.add(new BasicNameValuePair("type", "7"));
                qparams.add(new BasicNameValuePair("type", "8"));
                qparams.add(new BasicNameValuePair("type", "10"));
                qparams.add(new BasicNameValuePair("type", "16"));
                qparams.add(new BasicNameValuePair("type", "17"));
            } else if (barType.equals("2")) {
                qparams.add(new BasicNameValuePair("type", "9"));
                qparams.add(new BasicNameValuePair("type", "11"));
            } else if (barType.equals("3")) {
                qparams.add(new BasicNameValuePair("type", "12"));
                qparams.add(new BasicNameValuePair("type", "13"));
                qparams.add(new BasicNameValuePair("type", "14"));
                qparams.add(new BasicNameValuePair("type", "15"));
            } else if (barType.equals("4")) {
                qparams.add(new BasicNameValuePair("type", "18"));
            } else if (barType.equals("0")) {
                qparams.add(new BasicNameValuePair("type", "0"));
            }
            URI uri = URIUtils.createURI("http", "webapi.suntory.co.jp", -1, "/barnavi/v2/shops", URLEncodedUtils.format(qparams, "UTF-8"), null);
            HttpGet httpget = new HttpGet(uri);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                buf.append(str);
                buf.append(System.getProperty("line.separator"));
            }
            reader.close();
            Shops shops = new Shops(buf.toString());
            shop = shops.getShop();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (ClientProtocolException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return shop;
    }

    /**
     * @param prefecture
     * @return
     */
    private String getPrefsNum(String prefecture) {
        String prefNum = null;
        String[] prefs = resources.getStringArray(info.clockworksapple.android.barsearch.R.array.req_param_prefecture);
        for (int i = 0; i < prefs.length; i++) {
            if (prefs[i].indexOf(prefecture) != -1) {
                prefNum = prefs[i].split(":")[0];
                break;
            }
        }
        return prefNum;
    }
}

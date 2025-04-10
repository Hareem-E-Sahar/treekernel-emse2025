package net.sf.entDownloader.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.entDownloader.core.events.Broadcaster;
import net.sf.entDownloader.core.events.DownloadedBytesEvent;

/**
 * Gère les connexions HTTP et permet des utilisations avancées tels que
 * l'obtention des sources HTML
 * et le téléchargement de fichiers.
 * 
 */
public class Browser {

    private Map<String, String> argv;

    private List<HttpCookie> cookies;

    private Map<String, List<String>> headerFields = null;

    private String url;

    private String encodedParam;

    private Method method;

    private boolean followRedirects = true;

    private int responseCode = -1;

    private Proxy proxy = Proxy.NO_PROXY;

    public enum Method {

        GET, POST
    }

    /**
	 * Constructeur par défaut de la classe Browser
	 */
    public Browser() {
        this.argv = new HashMap<String, String>(8);
        url = "";
        encodedParam = "";
        method = Method.GET;
    }

    /**
	 * Constructeur de la classe Browser
	 * 
	 * @param url
	 *            Adresse de la page web
	 */
    public Browser(String url) {
        this();
        setUrl(url);
    }

    /**
	 * Ajoute un argument à la requête. Si l'argument a déjà été défini,
	 * l'ancienne valeur sera écrasé.
	 * 
	 * @param name Nom du champ de l'argument.
	 * @param value Valeur de l'argument.
	 */
    public void setParam(String name, String value) {
        argv.put(name, value);
    }

    /**
	 * Supprime tous les arguments de requête précédemment définis
	 */
    public void clearParam() {
        argv.clear();
    }

    protected void encodeParam() throws UnsupportedEncodingException {
        int argc = 0;
        encodedParam = "";
        if (!argv.isEmpty()) {
            if (method == Method.GET) {
                encodedParam = "?";
            }
            for (Map.Entry<String, String> e : argv.entrySet()) {
                if (argc > 0) {
                    encodedParam += "&";
                }
                encodedParam += URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
                ++argc;
            }
        }
    }

    /**
	 * Effectue la requête précédemment configuré et retourne le texte renvoyé
	 * par le serveur (code HTML par exemple).
	 * 
	 * @return Le texte renvoyé par le serveur (code HTML ou XML par exemple).
	 * @throws IOException La connexion a échoué.
	 */
    public String getPage() throws IOException {
        BufferedReader reader = null;
        String response = "";
        try {
            reader = new BufferedReader(new InputStreamReader(performRequest()));
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                response += ligne;
            }
        } catch (ConnectException e1) {
            UnknownHostException ex = new UnknownHostException();
            ex.initCause(e1);
            throw new UnknownHostException();
        } catch (UnknownHostException e) {
            throw e;
        } catch (NoRouteToHostException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return response;
    }

    /**
	 * Télécharge le fichier défini par {@link #setUrl(String)} et l'enregistre
	 * à l'emplacement désigné par <code>destinationPath</code>.
	 * 
	 * @param destinationPath Le chemin où le fichier sera enregistrer
	 * @throws FileNotFoundException Voir le constructeur de
	 *             java.io.FileOutputStream
	 */
    public void downloadFile(String destinationPath) throws FileNotFoundException {
        InputStream reader = null;
        FileOutputStream writeFile = null;
        try {
            File fpath = new File(destinationPath).getCanonicalFile();
            destinationPath = fpath.getPath();
            File dpath = new File(fpath.getParent());
            dpath.mkdirs();
            reader = performRequest();
            writeFile = new FileOutputStream(destinationPath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = reader.read(buffer)) > 0) {
                writeFile.write(buffer, 0, read);
                Broadcaster.fireDownloadedBytes(new DownloadedBytesEvent(read));
            }
            writeFile.flush();
        } catch (FileNotFoundException e1) {
            throw e1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
            try {
                writeFile.close();
            } catch (Exception e) {
            }
        }
    }

    /**
	 * Effectue la requête précédemment configuré et retourne un InputStream
	 * permettant de lire la réponse renvoyée par le serveur.
	 * 
	 * @return Un InputStream permettant de lire la réponse renvoyée par le
	 *         serveur.
	 */
    private InputStream performRequest() throws UnsupportedEncodingException, MalformedURLException, IOException {
        OutputStreamWriter writer = null;
        encodeParam();
        URL url;
        if (method == Method.POST) {
            url = new URL(this.url);
        } else {
            url = new URL(this.url + encodedParam);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
        conn.setInstanceFollowRedirects(followRedirects);
        conn.setDoOutput(true);
        setupCookie(conn);
        try {
            if (method == Method.POST) {
                writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(encodedParam);
                writer.flush();
            }
            setCookies(conn.getHeaderField("Set-Cookie"));
            responseCode = conn.getResponseCode();
            headerFields = conn.getHeaderFields();
            return conn.getInputStream();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
	 * Retourne l'url courante de l'objet.
	 */
    public String getUrl() {
        return url;
    }

    /**
	 * Définit l'url cible de la requête.
	 * 
	 * @param url URL à définir
	 */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
	 * Retourne la méthode HTTP actuellement utilisée.
	 * 
	 */
    public Method getMethod() {
        return method;
    }

    /**
	 * Configure la méthode HTTP à utiliser (POST ou GET).
	 * 
	 * @param method Méthode HTTP à utiliser.
	 */
    public void setMethod(Method method) {
        if (method == Method.POST || method == Method.GET) {
            this.method = method;
        } else throw new IllegalArgumentException();
    }

    /**
	 * Retourne l'état actuel du suivi des redirections.
	 * 
	 * @return True si le suivi des redirections est activé, false sinon.
	 */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
	 * Active ou désactive le suivi des redirections HTTP (code 3xx)
	 * 
	 * @param followRedirects
	 *            Nouvelle valeur
	 */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
	 * Installe les cookies actuellement définis sur la connexion indiquée.
	 * 
	 * @param urlConnection La connexion sur laquelle on souhaite définir les
	 *            cookies.
	 */
    private void setupCookie(URLConnection urlConnection) {
        if (cookies != null && !cookies.isEmpty()) {
            String cookieString = "";
            boolean isFirst = true;
            for (HttpCookie cookie : cookies) {
                if (!isFirst) {
                    cookieString += "; ";
                }
                cookieString += cookie.toString();
                isFirst = false;
            }
            urlConnection.setRequestProperty("Cookie", cookieString);
        }
    }

    /**
	 * Retourne la valeur du champ de cookie portant le nom
	 * <code>fieldname</code>, ou null si ce champ n'existe pas.
	 * 
	 * @param name Le nom du champ de cookie souhaité
	 * @return La valeur du champ de cookie demandé, ou null si le champ n'est
	 *         pas défini.
	 */
    public String getCookieValueByName(String name) {
        HttpCookie cookie = getCookieByName(name);
        return cookie != null ? cookie.getValue() : null;
    }

    /**
	 * Retourne le cookie portant le nom indiqué.
	 * 
	 * @param name Le nom du cookie recherché.
	 * @return Le cookie portant le nom indiqué, ou null si ce dernier n'existe
	 *         pas.
	 */
    public HttpCookie getCookieByName(String name) {
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals(name)) return cookie;
        }
        return null;
    }

    /**
	 * Ajoute ou redéfinit la valeur du champ de cookie spécifié
	 * 
	 * @param name Le nom du champ de cookie à définir
	 * @param value La valeur du champ de cookie <i>fieldname</i>.
	 */
    public void setCookieField(String name, String value) {
        HttpCookie cookie = getCookieByName(name);
        if (cookie == null) {
            cookie = new HttpCookie(name, value);
            cookies.add(cookie);
        } else {
            cookie.setValue(value);
        }
    }

    /**
	 * Supprime tous les cookies actuellement défini
	 */
    public void delCookie() {
        cookies.clear();
    }

    /**
	 * Supprime le champ de cookie spécifié
	 * 
	 * @param name Le champ à supprimer
	 */
    public void delCookie(String name) {
        HttpCookie cookie = getCookieByName(name);
        if (cookie != null) {
            cookies.remove(cookie);
        }
    }

    /**
	 * Définit les cookies envoyés dans les requêtes suivantes. Les précédents
	 * cookies sont écrasés
	 * 
	 * @param cookie Cookies à définir
	 */
    public void setCookies(String cookie) {
        if (cookie == null) return;
        cookies = HttpCookie.parse(cookie);
    }

    /**
	 * Obtient le code de statut du message de réponse HTTP
	 * 
	 * @return Le code de statut HTTP, ou -1 si aucun code ne peut être discerné
	 *         de la réponse (la réponse n'est pas valide) ou si aucune requête
	 *         n'a été effectué.
	 * @see HttpURLConnection#getResponseCode()
	 */
    public int getResponseCode() {
        return responseCode;
    }

    /**
	 * Retourne une Map contenant l'ensemble des entêtes de la réponse HTTP, ou
	 * null si aucune requête n'a été effectué.
	 * 
	 * @see URLConnection#getHeaderFields()
	 */
    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    /**
	 * Retourne la valeur du champ d'entête portant le nom désigné, ou null s'il
	 * n'y a pas ce champ dans la réponse.
	 * 
	 * @throws IllegalStateException Si aucune requête n'a été effectué.
	 * @see URLConnection#getHeaderField(String)
	 */
    public String getHeaderField(String name) {
        if (headerFields == null) throw new IllegalStateException("No request has been made.");
        List<String> values = headerFields.get(name);
        if (values == null) return null;
        String value = "";
        for (int i = 0; i < values.size() - 1; i++) {
            value += values.get(i) + ", ";
        }
        value += values.get(values.size() - 1);
        return value;
    }

    /**
	 * Installe un proxy HTTP à utiliser pour la connexion à Internet.
	 * 
	 * @param host Le nom d'hôte ou l'adresse du proxy.
	 * @param port Le port du proxy.
	 */
    public void setHttpProxy(String host, int port) {
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    /**
	 * Installe un proxy HTTP à utiliser pour la connexion à Internet.
	 * 
	 * @param proxy L'instance de java.net.Proxy à utiliser.
	 * @see java.net.Proxy
	 */
    public void setHttpProxy(Proxy proxy) {
        if (proxy == null) {
            proxy = Proxy.NO_PROXY;
        }
        this.proxy = proxy;
    }

    /**
	 * Retourne le proxy HTTP utilisé pour la connexion à Internet.
	 * 
	 * @return Le proxy HTTP utilisé pour la connexion à Internet.
	 */
    public Proxy getProxy() {
        return proxy;
    }

    /**
	 * Supprime la configuration de proxy précédemment installé.
	 */
    public void removeHttpProxy() {
        setHttpProxy(null);
    }
}

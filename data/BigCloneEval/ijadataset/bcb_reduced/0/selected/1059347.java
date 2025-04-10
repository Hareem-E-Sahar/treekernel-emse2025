package cz.softinel.retra.core.joke.blo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ApplicationObjectSupport;
import cz.softinel.retra.core.joke.Joke;
import cz.softinel.retra.core.joke.JokeFinderException;
import cz.softinel.uaf.webproxycache.WebProxyCache;

public class MiraJokeGenerator extends ApplicationObjectSupport implements JokeGenerator {

    private Log logger = LogFactory.getLog(this.getClass());

    private static List<Joke> jokes = null;

    private static Date nextLoad = null;

    public synchronized List<Joke> getJokesForDate(Date date) {
        if (jokes == null || (nextLoad == null || (new Date()).after(nextLoad))) {
            prepareJokes(date);
        }
        return jokes;
    }

    private synchronized void prepareJokes(Date date) {
        jokes = new ArrayList<Joke>();
        Joke garfield = getGarfieldJokeForDate(date);
        if (garfield != null) {
            jokes.add(garfield);
        }
        Joke dilbert = getDilbertJokeForDate(date);
        if (dilbert != null) {
            jokes.add(dilbert);
        }
        Joke snoopy = getSnoopyJokeForDate(date);
        if (snoopy != null) {
            jokes.add(snoopy);
        }
        Joke ben = getBenJokeForDate(date);
        if (ben != null) {
            jokes.add(ben);
        }
        Joke pcAndPixel = getPcAndPixelJokeForDate(date);
        if (pcAndPixel != null) {
            jokes.add(pcAndPixel);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        nextLoad = calendar.getTime();
    }

    private Joke getGarfieldJokeForDate(Date date) {
        String targetURL = "http://www.gocomics.com/garfield/";
        String baseURL = "http://www.gocomics.com/printable/garfield/";
        String imageDefinition = "src=\"http://cdn.svcs.c2.uclick.com";
        String labelKey = "garfield.label";
        int imagePathStart = 5;
        Joke joke = getComicsJoke(date, targetURL, baseURL, imageDefinition, labelKey, imagePathStart);
        return joke;
    }

    private Joke getDilbertJokeForDate(Date date) {
        Joke joke = null;
        String title = getMessageSourceAccessor().getMessage("dilbert.label");
        try {
            Joke dilbert = new Joke(getDilbertHtmlJokeForDate(date, title), title);
            jokes.add(dilbert);
        } catch (JokeFinderException e) {
        }
        return joke;
    }

    private String getDilbertHtmlJokeForDate(Date date, String title) throws JokeFinderException {
        String targetURL = "http://dilbert.com";
        String baseURL = "http://dilbert.com/strips/";
        String imageDefinition = "src=\"/dyn/str_strip";
        int imagePathStart = 5;
        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"http://www.dilbert.com\" target=\"_blank\">");
        sb.append("<img src=\"");
        sb.append(getProxyUrl(getDilbertJoke(date, targetURL, baseURL, imageDefinition, imagePathStart)));
        sb.append("\" alt=\"");
        sb.append(title);
        sb.append("\" title=\"");
        sb.append(title);
        sb.append("\" />");
        sb.append("</a>");
        return sb.toString();
    }

    private Joke getSnoopyJokeForDate(Date date) {
        String targetURL = "http://www.gocomics.com/peanuts/";
        String baseURL = "http://www.gocomics.com/printable/peanuts/";
        String imageDefinition = "src=\"http://cdn.svcs.c2.uclick.com";
        String labelKey = "snoopy.label";
        int imagePathStart = 5;
        Joke joke = getComicsJoke(date, targetURL, baseURL, imageDefinition, labelKey, imagePathStart);
        return joke;
    }

    private Joke getBenJokeForDate(Date date) {
        String targetURL = "http://www.gocomics.com/ben/";
        String baseURL = "http://www.gocomics.com/printable/ben/";
        String imageDefinition = "src=\"http://cdn.svcs.c2.uclick.com";
        String labelKey = "ben.label";
        int imagePathStart = 5;
        Joke joke = getComicsJoke(date, targetURL, baseURL, imageDefinition, labelKey, imagePathStart);
        return joke;
    }

    private Joke getPcAndPixelJokeForDate(Date date) {
        String targetURL = "http://www.gocomics.com/pcandpixel/";
        String baseURL = "http://www.gocomics.com/printable/pcandpixel/";
        String imageDefinition = "src=\"http://cdn.svcs.c2.uclick.com";
        String labelKey = "pcAndPixel.label";
        int imagePathStart = 5;
        Joke joke = getComicsJoke(date, targetURL, baseURL, imageDefinition, labelKey, imagePathStart);
        return joke;
    }

    private Joke getComicsJoke(Date date, String targetURL, String baseURL, String imageDefinition, String labelKey, int imagePathStart) {
        Joke joke = null;
        try {
            joke = new Joke();
            String label = getMessageSourceAccessor().getMessage(labelKey);
            StringBuffer sb = new StringBuffer();
            sb.append("<a href=\"");
            sb.append(targetURL);
            sb.append("\" target=\"_blank\">");
            sb.append("<img src=\"");
            sb.append(getProxyUrl(getImageUrlForComicsJoke(date, baseURL, imageDefinition, imagePathStart)));
            sb.append("\" alt=\"");
            sb.append(label);
            sb.append("\" title=\"");
            sb.append(label);
            sb.append("\" />");
            sb.append("</a>");
            String jokeHtmlCode = sb.toString();
            joke.setHtml(jokeHtmlCode);
            joke.setLabel(label);
        } catch (JokeFinderException e) {
            logger.warn("Couldn't find joke.", e);
            joke = null;
        }
        return joke;
    }

    private String getImageUrlForComicsJoke(Date date, String baseURL, String imageDefinition, int imagePathStart) throws JokeFinderException {
        String imageUrl = null;
        try {
            imageUrl = getBaseImageComicsJoke(date, baseURL, imageDefinition, imagePathStart);
            URL url = new URL(imageUrl);
            url.openStream();
        } catch (JokeFinderException e) {
            logger.warn("Couldn't find base URL.", e);
            imageUrl = null;
        } catch (IOException e) {
            logger.warn("Couldn't find or open image base URL.", e);
            imageUrl = null;
        }
        if (imageUrl == null) {
            throw new JokeFinderException("Couldn't find joke.");
        }
        return imageUrl;
    }

    private String getBaseImageComicsJoke(Date date, String baseURL, String imageDefinition, int imagePathStart) throws JokeFinderException {
        StringBuffer sb = new StringBuffer();
        URL comicsPageURL = null;
        InputStreamReader inr = null;
        try {
            sb.append(baseURL);
            sb.append(new SimpleDateFormat("yyyy/MM/dd").format(date));
            sb.append("/");
            comicsPageURL = new URL(sb.toString());
            inr = new InputStreamReader(comicsPageURL.openStream());
        } catch (Exception e) {
            logger.warn("Couldn't find base URL.");
            throw new JokeFinderException(e);
        }
        BufferedReader in = new BufferedReader(inr);
        String inputLine;
        int start = 0;
        boolean lineImageFound = false;
        try {
            while ((inputLine = in.readLine()) != null) {
                start = inputLine.indexOf(imageDefinition);
                if (start > -1) {
                    lineImageFound = true;
                    break;
                }
            }
            in.close();
        } catch (IOException e) {
            throw new JokeFinderException(e);
        }
        if (lineImageFound) {
            String lineWithImg = inputLine.substring(start + imagePathStart);
            String replaced = lineWithImg.replaceAll("(.*)(\" />).*", "$1");
            sb = new StringBuffer("");
            sb.append(replaced);
        } else {
            throw new JokeFinderException("Img for joke couldn't be found.");
        }
        return sb.toString();
    }

    private String getDilbertJoke(Date date, String targetURL, String baseURL, String imageDefinition, int imagePathStart) throws JokeFinderException {
        StringBuffer sb = new StringBuffer();
        URL comicsPageURL = null;
        InputStreamReader inr = null;
        try {
            sb.append(baseURL);
            sb.append(new SimpleDateFormat("yyyy-MM-dd").format(date));
            sb.append("/");
            comicsPageURL = new URL(sb.toString());
            inr = new InputStreamReader(comicsPageURL.openStream());
        } catch (Exception e) {
            logger.warn("Couldn't find base URL.");
            throw new JokeFinderException(e);
        }
        BufferedReader in = new BufferedReader(inr);
        String inputLine;
        int start = 0;
        boolean first = true;
        boolean lineImageFound = false;
        try {
            while ((inputLine = in.readLine()) != null) {
                start = inputLine.indexOf(imageDefinition);
                if (start > -1) {
                    if (first) {
                        first = false;
                    } else {
                        lineImageFound = true;
                        break;
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            throw new JokeFinderException(e);
        }
        if (lineImageFound) {
            String lineWithImg = inputLine.substring(start + imagePathStart);
            String replaced = lineWithImg.replaceAll("(.*)(\\.(gif|jpg|jpeg|png)).*", "$1$2");
            sb = new StringBuffer(targetURL);
            sb.append(replaced);
        } else {
            throw new JokeFinderException("Img for joke couldn't be found.");
        }
        return sb.toString();
    }

    private String getProxyUrl(String url) {
        WebProxyCache webProxyCache = WebProxyCache.getInstance();
        return webProxyCache.getProxyUrl(url);
    }
}

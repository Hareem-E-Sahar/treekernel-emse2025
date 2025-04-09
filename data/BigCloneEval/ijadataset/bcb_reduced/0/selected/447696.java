package com.moviejukebox.plugin;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;
import com.moviejukebox.model.Movie;
import com.moviejukebox.tools.HTMLTools;
import com.moviejukebox.tools.PropertiesUtil;

public class KinopoiskPlugin extends ImdbPlugin {

    public static String KINOPOISK_PLUGIN_ID = "kinopoisk";

    int preferredPlotLength = Integer.parseInt(PropertiesUtil.getProperty("kinopoisk.plot.maxlength", "400"));

    String preferredRating = PropertiesUtil.getProperty("kinopoisk.rating", "imdb");

    protected TheTvDBPlugin tvdb;

    public KinopoiskPlugin() {
        super();
        preferredCountry = PropertiesUtil.getProperty("imdb.preferredCountry", "Russia");
        tvdb = new TheTvDBPlugin();
    }

    @Override
    public boolean scan(Movie mediaFile) {
        boolean retval = true;
        String kinopoiskId = mediaFile.getId(KINOPOISK_PLUGIN_ID);
        if (kinopoiskId == null || kinopoiskId.equalsIgnoreCase(Movie.UNKNOWN)) {
            if (!mediaFile.isTVShow()) super.scan(mediaFile); else tvdb.scan(mediaFile);
            kinopoiskId = getKinopoiskId(mediaFile.getTitle(), mediaFile.getYear(), mediaFile.getSeason());
            mediaFile.setId(KINOPOISK_PLUGIN_ID, kinopoiskId);
        } else {
            mediaFile.setTitle(Movie.UNKNOWN);
        }
        if (kinopoiskId != null && !kinopoiskId.equalsIgnoreCase(Movie.UNKNOWN)) {
            retval = updateKinopoiskMediaInfo(mediaFile, kinopoiskId);
        }
        return retval;
    }

    @Override
    public void scanNFO(String nfo, Movie movie) {
        logger.finest("Scanning NFO for Kinopoisk Id");
        int beginIndex = nfo.indexOf("kinopoisk.ru/level/1/film/");
        if (beginIndex != -1) {
            StringTokenizer st = new StringTokenizer(nfo.substring(beginIndex + 26), "/");
            movie.setId(KinopoiskPlugin.KINOPOISK_PLUGIN_ID, st.nextToken());
            logger.finer("Kinopoisk Id found in nfo = " + movie.getId(KinopoiskPlugin.KINOPOISK_PLUGIN_ID));
        } else {
            logger.finer("No Kinopoisk Id found in nfo !");
        }
        super.scanNFO(nfo, movie);
    }

    /**
     * Retrieve Kinopoisk matching the specified movie name and year. 
     * This routine is base on a Google request.
     */
    private String getKinopoiskId(String movieName, String year, int season) {
        try {
            String sb = "+site:www.kinopoisk.ru/level/1/film/";
            sb = sb + " " + movieName;
            if (season != -1) {
                sb = sb + " +сериал";
            } else {
                if (year != null && !year.equalsIgnoreCase(Movie.UNKNOWN)) sb = sb + " +год +" + year;
            }
            sb = "http://www.google.ru/search?hl=ru&q=" + URLEncoder.encode(sb, "UTF-8");
            String xml = webBrowser.request(sb);
            int beginIndex = xml.indexOf("<a href=\"http://www.kinopoisk.ru/level/1/film/");
            if (beginIndex == -1) return Movie.UNKNOWN;
            beginIndex = xml.indexOf("kinopoisk.ru/level/1/film/", beginIndex);
            if (beginIndex == -1) return Movie.UNKNOWN;
            StringTokenizer st = new StringTokenizer(xml.substring(beginIndex + 26), "/\"");
            String kinopoiskId = st.nextToken();
            if (kinopoiskId != "") {
                try {
                    Integer.parseInt(kinopoiskId);
                } catch (Exception ignore) {
                    return Movie.UNKNOWN;
                }
                return kinopoiskId;
            } else {
                return Movie.UNKNOWN;
            }
        } catch (Exception e) {
            logger.severe("Failed retreiving Kinopoisk Id for movie : " + movieName);
            logger.severe("Error : " + e.getMessage());
            return Movie.UNKNOWN;
        }
    }

    /**
     * Scan Kinopoisk html page for the specified movie
     */
    private boolean updateKinopoiskMediaInfo(Movie movie, String kinopoiskId) {
        try {
            String originalTitle = movie.getTitle();
            String newTitle = originalTitle;
            String xml = webBrowser.request("http://www.kinopoisk.ru/level/1/film/" + kinopoiskId);
            xml = xml.replace((CharSequence) "&#133;", (CharSequence) "&hellip;");
            xml = xml.replace((CharSequence) "&#151;", (CharSequence) "&mdash;");
            if (!movie.isOverrideTitle()) {
                newTitle = HTMLTools.extractTag(xml, "class=\"moviename-big\">", 0, "<>");
                if (!newTitle.equals(Movie.UNKNOWN)) {
                    int i = newTitle.indexOf("(сериал");
                    if (i >= 0) {
                        newTitle = newTitle.substring(0, i);
                        movie.setMovieType(Movie.TYPE_TVSHOW);
                    }
                    newTitle = newTitle.replace(' ', ' ').trim();
                    if (movie.getSeason() != -1) newTitle = newTitle + ", сезон " + String.valueOf(movie.getSeason());
                    String s = HTMLTools.extractTag(xml, "<span style=\"font-size:13px;color:#666\">", 0, "><");
                    if (!s.equals(Movie.UNKNOWN) && !s.equalsIgnoreCase("/span")) {
                        originalTitle = s;
                        newTitle = newTitle + " / " + originalTitle;
                    } else originalTitle = newTitle;
                }
            }
            String plot = HTMLTools.extractTag(xml, "<td colspan=3 style=\"padding:10px;padding-left:20px;\" class=\"news\">", 0, "<>");
            if (plot.equals(Movie.UNKNOWN)) plot = movie.getPlot();
            if (plot.length() > preferredPlotLength) plot = plot.substring(0, preferredPlotLength) + "...";
            movie.setPlot(plot);
            LinkedList<String> newGenres = new LinkedList<String>();
            for (String genre : HTMLTools.extractTags(xml, ">жанр</td>", "</td>", "<a href=\"/level/10", "</a>")) {
                genre = genre.substring(0, 1).toUpperCase() + genre.substring(1, genre.length());
                if (genre.equalsIgnoreCase("мультфильм")) newGenres.addFirst(genre); else newGenres.add(genre);
            }
            if (newGenres.size() > 0) {
                int maxGenres = 9;
                try {
                    maxGenres = Integer.parseInt(PropertiesUtil.getProperty("genres.max", "9"));
                } catch (Exception ignore) {
                }
                while (newGenres.size() > maxGenres) newGenres.removeLast();
                movie.setGenres(newGenres);
            }
            for (String director : HTMLTools.extractTags(xml, ">режиссер</td>", "</td>", "<a href=\"/level/4", "</a>")) {
                movie.setDirector(director);
                break;
            }
            Collection<String> newCast = new ArrayList<String>();
            for (String actor : HTMLTools.extractTags(xml, ">В главных ролях:", "</table>", "<a href=\"/level/4", "</a>")) {
                newCast.add(actor);
            }
            if (newCast.size() > 0) movie.setCast(newCast);
            for (String country : HTMLTools.extractTags(xml, ">страна</td>", "</td>", "<a href=\"/level/10", "</a>")) {
                movie.setCountry(country);
                break;
            }
            if (movie.getYear().equals(Movie.UNKNOWN)) {
                for (String year : HTMLTools.extractTags(xml, ">год</td>", "</td>", "<a href=\"/level/10", "</a>")) {
                    movie.setYear(year);
                    break;
                }
            }
            int kinopoiskRating = -1;
            String rating = HTMLTools.extractTag(xml, "<a href=\"/level/83/film/" + kinopoiskId + "/\" class=\"continue\">", 0, "<");
            if (!rating.equals(Movie.UNKNOWN)) {
                try {
                    kinopoiskRating = (int) (Float.parseFloat(rating) * 10);
                } catch (Exception ignore) {
                }
            }
            int imdbRating = movie.getRating();
            if (imdbRating == -1) {
                rating = HTMLTools.extractTag(xml, ">IMDB:", 0, "<(");
                if (!rating.equals(Movie.UNKNOWN)) {
                    try {
                        imdbRating = (int) (Float.parseFloat(rating) * 10);
                    } catch (Exception ignore) {
                    }
                }
            }
            int r = kinopoiskRating;
            if (imdbRating != -1) {
                if (preferredRating.equals("imdb")) r = imdbRating; else if (preferredRating.equals("average")) r = (kinopoiskRating + imdbRating) / 2;
            }
            movie.setRating(r);
            if (movie.getPosterURL() == null || movie.getPosterURL().equalsIgnoreCase(Movie.UNKNOWN)) {
                movie.setTitle(originalTitle);
                movie.setPosterURL(getPosterURL(movie, ""));
            }
            if (movie.getRuntime().equals(Movie.UNKNOWN)) {
                movie.setRuntime(getPreferredValue(HTMLTools.extractTags(xml, ">время</td>", "</td>", "<td", "</td>")));
            }
            movie.setTitle(newTitle);
        } catch (Exception e) {
            logger.severe("Failed retreiving movie data from Kinopoisk : " + kinopoiskId);
            e.printStackTrace();
        }
        return true;
    }
}

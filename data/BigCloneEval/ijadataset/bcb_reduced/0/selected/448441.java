package net.sf.sageplugins.webserver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.http.HttpServletRequest;
import net.sf.sageplugins.sageutils.SageApi;
import Acme.Serve.Serve;

/**
 * @author Owner
 *
 * 
 *
 */
public class Search {

    protected static String charset = null;

    public static final Long BEGINNING_OF_TIME = new Long(0);

    public static final Long END_OF_TIME = new Long(Long.MAX_VALUE);

    public static final Long NOW = null;

    public static final String SEARCH_TYPE_AIRINGS = "Airings";

    public static final String SEARCH_TYPE_MEDIA_FILES = "MediaFiles";

    public static final String SEARCH_TYPE_TV_FILES = "TVFiles";

    private String searchString = "";

    private boolean exactTitle = false;

    private String favoriteId = null;

    private String searchType = SEARCH_TYPE_AIRINGS;

    private boolean video = true;

    private boolean dvd = true;

    private boolean music = false;

    private boolean picture = false;

    private String[] categories = null;

    private String[] channels = null;

    private String[] fields = null;

    private String filename = null;

    private boolean regex = false;

    private boolean casesensitive = false;

    private String watched = null;

    private String dontlike = null;

    private String favorite = null;

    private String firstRuns = null;

    private String hdtv = null;

    private String archived = null;

    private String manrec = null;

    private String autodelete = null;

    private String partials = null;

    private String timeRange = null;

    private String sort1 = null;

    private String sort2 = null;

    private Long starttime = NOW;

    private Long endtime = END_OF_TIME;

    static {
        try {
            charset = SageApi.GetProperty("nielm/webserver/charset", "UTF-8");
        } catch (InvocationTargetException e) {
            Serve.extLog("Error getting charset:" + e.toString());
            e.printStackTrace(System.out);
            charset = "UTF-8";
        }
    }

    public Search() {
    }

    /**
     * @return the archived
     */
    public String getArchived() {
        if (archived == null) {
            return "any";
        } else {
            return archived;
        }
    }

    public String getManRec() {
        if (manrec == null) {
            return "any";
        } else {
            return manrec;
        }
    }

    public void setManRec(String manrec) {
        this.manrec = manrec;
    }

    public void setArchived(String archived) {
        this.archived = archived;
    }

    /**
     * @return the autodelete
     */
    public String getAutodelete() {
        if (autodelete == null) {
            return "any";
        } else {
            return autodelete;
        }
    }

    /**
     * @param autodelete the autodelete to set
     */
    public void setAutodelete(String autodelete) {
        this.autodelete = autodelete;
    }

    /**
     * @return if filename search is case sensitive
     */
    public boolean isCaseSensitive() {
        return casesensitive;
    }

    /**
     * @param casesensitive filename search is case sensitive
     */
    public void setCaseSensitive(boolean casesensitive) {
        this.casesensitive = casesensitive;
    }

    /**
     * @return the categories
     */
    public String[] getCategories() {
        return categories;
    }

    /**
     * @param categories the categories to set
     */
    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public String getSearchName() {
        StringBuffer searchName = new StringBuffer();
        if (searchString != null) {
            if (searchString.length() == 0) {
                searchName.append("All ");
            }
            if (isSearchTypeAirings() || isSearchTypeMediaFiles() || isSearchTypeTVFiles()) {
                if (isSearchTypeAirings()) {
                    searchName.append("Airings ");
                } else if (isSearchTypeMediaFiles()) {
                    searchName.append("Imported ");
                    boolean addcomma = false;
                    if (isVideo()) {
                        searchName.append("Videos ");
                        addcomma = true;
                    }
                    if (isDVD()) {
                        if (addcomma) searchName.append(", ");
                        searchName.append("DVDs");
                        addcomma = true;
                    }
                    if (isMusic()) {
                        if (addcomma) searchName.append(", ");
                        searchName.append("Music");
                        addcomma = true;
                    }
                    if (isPicture()) {
                        if (addcomma) searchName.append(", ");
                        searchName.append("Pictures ");
                        addcomma = true;
                    }
                } else if (isSearchTypeTVFiles()) {
                    searchName.append("Recordings ");
                }
                searchName.append("matching \"");
                searchName.append(searchString);
                searchName.append('"');
            } else {
                searchName.append("Unknown search type");
            }
        } else if (favoriteId != null) {
            Favorite favorite = null;
            try {
                favorite = new Favorite(Integer.parseInt(getFavoriteId()));
                if (favorite != null) {
                    if (isSearchTypeTVFiles()) {
                        searchName.append("Recordings of Favorite: \"");
                    } else {
                        searchName.append("Airings of Favorite: \"");
                    }
                    searchName.append(favorite.getDescription());
                    searchName.append('"');
                }
            } catch (Exception e) {
                searchName.append("Favorite ");
            }
        } else {
            searchName.append("Unknown search type");
        }
        return searchName.toString();
    }

    /**
     * @return the channels
     */
    public String[] getChannels() {
        return channels;
    }

    /**
     * @param channels the channels to set
     */
    public void setChannels(String[] channels) {
        this.channels = channels;
    }

    /**
     * @return the dontlike
     */
    public String getDontlike() {
        if (dontlike == null) {
            return "any";
        } else {
            return dontlike;
        }
    }

    /**
     * @param dontlike the dontlike to set
     */
    public void setDontlike(String dontlike) {
        this.dontlike = dontlike;
    }

    /**
     * @return if dvd is the imported media file type
     */
    public boolean isDVD() {
        return dvd;
    }

    /**
     * @param dvd search for imported dvds
     */
    public void setDVD(boolean dvd) {
        this.dvd = dvd;
    }

    /**
     * @return the endtime
     */
    public Long getEndtime() {
        if (endtime == NOW) {
            return new Long(new Date().getTime());
        }
        return endtime;
    }

    /**
     * @param endtime the endtime to set
     */
    public void setEndtime(Long endtime) {
        this.endtime = endtime;
    }

    /**
     * @return if search string is matched exactly
     */
    public boolean isExactTitle() {
        return exactTitle;
    }

    /**
     * @param exactTitle match search string exactly
     */
    public void setExactTitle(boolean exactTitle) {
        this.exactTitle = exactTitle;
    }

    /**
     * @return the favorite
     */
    public String getFavorite() {
        if (favorite == null) {
            return "any";
        } else {
            return favorite;
        }
    }

    /**
     * @param favorite the favorite to set
     */
    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }

    /**
     * @return the favoriteId
     */
    public String getFavoriteId() {
        return favoriteId;
    }

    /**
     * @param favoriteId the favoriteId to set
     */
    public void setFavoriteId(String favoriteId) {
        this.favoriteId = favoriteId;
    }

    /**
     * @return the fields
     */
    public String[] getFields() {
        return fields;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(String[] fields) {
        this.fields = fields;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the firstRuns
     */
    public String getFirstRuns() {
        if (firstRuns == null) {
            return "any";
        } else {
            return firstRuns;
        }
    }

    /**
     * @param hdtv the hdtv to set
     */
    public void setHDTV(String hdtv) {
        this.hdtv = hdtv;
    }

    /**
     * @return the hdtv
     */
    public String getHDTV() {
        if (hdtv == null) {
            return "any";
        } else {
            return hdtv;
        }
    }

    /**
     * @param firstRuns the firstRuns to set
     */
    public void setFirstRuns(String firstRuns) {
        this.firstRuns = firstRuns;
    }

    /**
     * @return if music is the imported media file type
     */
    public boolean isMusic() {
        return music;
    }

    /**
     * @param music search for imported music
     */
    public void setMusic(boolean music) {
        this.music = music;
    }

    /**
     * @return the partials
     */
    public String getPartials() {
        if (partials == null) {
            return "none";
        } else {
            return partials;
        }
    }

    /**
     * @param partials the partials to set
     */
    public void setPartials(String partials) {
        this.partials = partials;
    }

    /**
     * @return if picture is the imported media file type
     */
    public boolean isPicture() {
        return picture;
    }

    /**
     * @param picture search for imported pictures
     */
    public void setPicture(boolean picture) {
        this.picture = picture;
    }

    /**
     * @param req the http request with properties.
     * Convenience method for calling code.
     */
    public void setProperties(HttpServletRequest req) {
        setArchived(req.getParameter("archived"));
        setAutodelete(req.getParameter("autodelete"));
        setCaseSensitive("on".equals(req.getParameter("casesensitive")));
        setCategories(req.getParameterValues("Categories"));
        setChannels(req.getParameterValues("Channels"));
        setDontlike(req.getParameter("dontlike"));
        setDVD("on".equals(req.getParameter("DVD")));
        setExactTitle("on".equals(req.getParameter("ExactTitle")));
        setFavorite(req.getParameter("favorite"));
        setFavoriteId(req.getParameter("FavoriteId"));
        setFields(req.getParameterValues("search_fields"));
        setFilename(req.getParameter("filename"));
        setFirstRuns(req.getParameter("firstruns"));
        setHDTV(req.getParameter("hdtv"));
        setManRec(req.getParameter("manrec"));
        setMusic("on".equals(req.getParameter("Music")));
        setPartials(req.getParameter("partials"));
        setPicture("on".equals(req.getParameter("Picture")));
        setRegex("on".equals(req.getParameter("regex")));
        setSearchString(req.getParameter("SearchString"));
        setSearchType(req.getParameter("searchType"));
        setSort1(req.getParameter("sort1"));
        setSort2(req.getParameter("sort2"));
        setTimeRange(req.getParameter("TimeRange"));
        setVideo("on".equals(req.getParameter("Video")));
        setWatched(req.getParameter("watched"));
    }

    /**
     * @return if regex is used for filename
     */
    public boolean isRegex() {
        return regex;
    }

    /**
     * @param regex use regex for filename
     */
    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    /**
     * @return the searchString
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * @param searchString the searchString to set
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * @return the searchType
     */
    public String getSearchType() {
        if (searchType == null) {
            return SEARCH_TYPE_AIRINGS;
        } else {
            return searchType;
        }
    }

    /**
     * @param searchType the searchType to set
     */
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    /**
     * @returns if the search type is Airings
     */
    public boolean isSearchTypeAirings() {
        return getSearchType().equalsIgnoreCase(SEARCH_TYPE_AIRINGS);
    }

    /**
     * @returns if the search type is Media Files
     */
    public boolean isSearchTypeMediaFiles() {
        return getSearchType().equalsIgnoreCase(SEARCH_TYPE_MEDIA_FILES);
    }

    /**
     * @returns if the search type is TV Files
     */
    public boolean isSearchTypeTVFiles() {
        return getSearchType().equalsIgnoreCase(SEARCH_TYPE_TV_FILES);
    }

    /**
     * @return the sort1
     */
    public String getSort1() {
        if (sort1 == null) {
            return "airdate_asc";
        } else {
            return sort1;
        }
    }

    /**
     * @param sort1 the sort1 to set
     */
    public void setSort1(String sort1) {
        this.sort1 = sort1;
    }

    /**
     * @return the sort2
     */
    public String getSort2() {
        if (sort2 == null) {
            return "none";
        } else {
            return sort2;
        }
    }

    /**
     * @param sort2 the sort2 to set
     */
    public void setSort2(String sort2) {
        this.sort2 = sort2;
    }

    /**
     * @return the starttime
     */
    public Long getStarttime() {
        if (starttime == NOW) {
            return new Long(new Date().getTime());
        }
        return starttime;
    }

    /**
     * @param starttime the starttime to set
     */
    public void setStarttime(Long starttime) {
        this.starttime = starttime;
    }

    /**
     * @return if time is filtered
     */
    public boolean isTimeFiltered() {
        return (!isSearchTypeMediaFiles() && !isSearchTypeTVFiles() && ((starttime != BEGINNING_OF_TIME) || (endtime != END_OF_TIME)));
    }

    /**
     * @return the timeRange
     */
    public String getTimeRange() {
        if (timeRange == null) {
            return "0";
        } else {
            return timeRange;
        }
    }

    /**
     * @param timeRange the timeRange to set
     */
    public void setTimeRange(String timeRange) {
        if (timeRange != null) {
            if (timeRange.equals("0")) {
                setStarttime(NOW);
                setEndtime(END_OF_TIME);
            } else if (timeRange.equals("-1")) {
                setStarttime(BEGINNING_OF_TIME);
                setEndtime(END_OF_TIME);
            } else if (timeRange.equals("-999")) {
                setEndtime(NOW);
                setStarttime(BEGINNING_OF_TIME);
            } else {
                try {
                    long timeRangeLong = Long.parseLong(timeRange);
                    if (timeRangeLong > 0) {
                        setStarttime(NOW);
                        setEndtime(new Long(getStarttime().longValue() + timeRangeLong * 60 * 60 * 1000));
                    }
                } catch (Exception e) {
                }
            }
        } else {
            setStarttime(NOW);
            setEndtime(END_OF_TIME);
        }
        this.timeRange = timeRange;
    }

    /**
     * @return if video is the imported media file type
     */
    public boolean isVideo() {
        return video;
    }

    /**
     * @param video search for imported videos
     */
    public void setVideo(boolean video) {
        this.video = video;
    }

    /**
     * @return the watched
     */
    public String getWatched() {
        if (watched == null) {
            return "any";
        } else {
            return watched;
        }
    }

    /**
     * @param watched the watched to set
     */
    public void setWatched(String watched) {
        this.watched = watched;
    }

    public Object doSearch() throws Exception {
        Object searchResults = null;
        if (searchString != null) {
            if (searchString != null && searchString.length() > 0) {
                if (isSearchTypeAirings() || isSearchTypeMediaFiles() || isSearchTypeTVFiles()) {
                    if (isExactTitle()) {
                        searchResults = SageApi.Api("SearchByTitle", new Object[] { searchString });
                    } else {
                        String[] fields = getFields();
                        List<String> fields_l = null;
                        if (fields != null) fields_l = Arrays.asList(fields); else fields_l = new Vector<String>();
                        searchResults = SageApi.Api("SearchSelectedFields", new Object[] { searchString, Boolean.FALSE, new Boolean(fields == null || fields_l.contains("title") || fields_l.contains("**ALL**")), new Boolean(fields_l.contains("episode") || fields_l.contains("**ALL**")), new Boolean(fields_l.contains("desc") || fields_l.contains("**ALL**")), new Boolean(fields_l.contains("people") || fields_l.contains("**ALL**")), new Boolean(fields_l.contains("category") || fields_l.contains("**ALL**")), new Boolean(fields_l.contains("rated") || fields_l.contains("**ALL**")), new Boolean(fields_l.contains("extrated") || fields_l.contains("**ALL**")), new Boolean(fields_l.contains("year") || fields_l.contains("**ALL**")), new Boolean(fields_l.contains("misc") || fields_l.contains("**ALL**")) });
                    }
                    if (isSearchTypeMediaFiles() || isSearchTypeTVFiles()) {
                        searchResults = SageApi.Api("FilterByMethod", new Object[] { searchResults, "GetMediaFileForAiring", null, Boolean.FALSE });
                        int numres = SageApi.Size(searchResults);
                        Object newSearchResults = null;
                        for (int i = 0; i < numres; i++) newSearchResults = SageApi.Api("DataUnion", new Object[] { newSearchResults, SageApi.Api("GetMediaFileForAiring", SageApi.GetElement(searchResults, i)) });
                        boolean tvFiles = isSearchTypeTVFiles();
                        searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { newSearchResults, "IsTVFile", new Boolean(tvFiles) });
                        if (tvFiles) {
                            searchResults = filterTvFiles(searchResults);
                        }
                        if (isSearchTypeMediaFiles()) {
                            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, (isVideo() ? "IsVideoFile" : "IsAiringObject") + (isDVD() ? "|IsDVD" : "|IsAiringObject") + (isMusic() ? "|IsMusicFile" : "|IsAiringObject") + (isPicture() ? "|IsPictureFile" : "|IsAiringObject"), Boolean.TRUE });
                        }
                        searchResults = filterShowList(searchResults);
                    } else {
                        searchResults = filterShowList(searchResults);
                    }
                } else {
                    searchResults = null;
                }
            } else {
                if (isSearchTypeAirings()) {
                    searchResults = SageApi.Api("GetAiringsOnViewableChannelsAtTime", new Object[] { getStarttime(), getEndtime(), Boolean.FALSE });
                } else if (isSearchTypeMediaFiles()) {
                    searchResults = SageApi.Api("GetMediaFiles");
                    searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsTVFile", Boolean.FALSE });
                    searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsLibraryFile", Boolean.TRUE });
                    searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, (isVideo() ? "IsVideoFile" : "IsAiringObject") + (isDVD() ? "|IsDVD" : "|IsAiringObject") + (isMusic() ? "|IsMusicFile" : "|IsAiringObject") + (isPicture() ? "|IsPictureFile" : "|IsAiringObject"), Boolean.TRUE });
                } else if (isSearchTypeTVFiles()) {
                    searchResults = SageApi.Api("GetMediaFiles");
                    searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsTVFile", Boolean.TRUE });
                    searchResults = filterTvFiles(searchResults);
                } else {
                    searchResults = null;
                }
                searchResults = filterShowList(searchResults);
            }
            if (searchResults != null) {
                searchResults = filterFilenames(searchResults);
            }
        } else if (favoriteId != null) {
            Favorite favorite = new Favorite(Integer.parseInt(getFavoriteId()));
            if (isSearchTypeTVFiles()) {
                searchResults = favorite.getSageAirings();
                searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsTVFile", Boolean.TRUE });
                searchResults = filterShowList(searchResults);
            } else {
                searchResults = favorite.getSageAirings();
                searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsMusicFile", Boolean.FALSE });
                searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsVideoFile", Boolean.FALSE });
                searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsDVD", Boolean.FALSE });
                searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsPictureFile", Boolean.FALSE });
                Object searchResults2 = favorite.getSageAirings();
                searchResults2 = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults2, "IsTVFile", Boolean.TRUE });
                searchResults = SageApi.Api("DataUnion", new Object[] { searchResults, searchResults2 });
                searchResults = filterShowList(searchResults);
            }
        }
        searchResults = sort(searchResults);
        return searchResults;
    }

    private Object filterShowList(Object searchResults) throws Exception {
        if (manrec != null && !manrec.equals("any") && SageApi.Size(searchResults) > 0) {
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsManualRecord", new Boolean(manrec.equalsIgnoreCase("set")) });
        }
        if (watched != null && !watched.equals("any") && SageApi.Size(searchResults) > 0) {
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsWatched", new Boolean(watched.equalsIgnoreCase("set")) });
        }
        if (dontlike != null && !dontlike.equals("any") && SageApi.Size(searchResults) > 0) {
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsDontLike", new Boolean(dontlike.equalsIgnoreCase("set")) });
        }
        if (favorite != null && !favorite.equals("any") && SageApi.Size(searchResults) > 0) {
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsFavorite", new Boolean(favorite.equalsIgnoreCase("set")) });
        }
        if (firstRuns != null && !firstRuns.equals("any") && SageApi.Size(searchResults) > 0) {
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsShowFirstRun", new Boolean(firstRuns.equalsIgnoreCase("set")) });
        }
        if (hdtv != null && !hdtv.equals("any") && SageApi.Size(searchResults) > 0) {
            try {
                searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsAiringHDTV", new Boolean(hdtv.equalsIgnoreCase("set")) });
            } catch (InvocationTargetException e) {
            }
        }
        if (isTimeFiltered() && SageApi.Size(searchResults) > 0) {
            searchResults = SageApi.Api("FilterByRange", new Object[] { searchResults, "GetAiringEndTime", getStarttime(), getEndtime(), Boolean.TRUE });
        }
        if (getCategories() != null && getCategories().length > 0 && SageApi.Size(searchResults) > 0) {
            List<String> categories_l = Arrays.asList(getCategories());
            if (categories_l.contains("**Any**")) {
                if (getCategories().length > 1) setCategories(new String[] { "**Any**" });
            } else {
                Object allFiltered = null;
                for (Iterator<String> it = categories_l.iterator(); it.hasNext(); ) {
                    Object filtered = SageApi.Api("DataUnion", new Object[] { searchResults });
                    String category = URLDecoder.decode((String) it.next(), charset);
                    filtered = SageApi.Api("FilterByMethod", new Object[] { filtered, "GetShowCategory", category, Boolean.TRUE });
                    allFiltered = SageApi.Api("DataUnion", new Object[] { allFiltered, filtered });
                }
                searchResults = allFiltered;
            }
        }
        if (getChannels() != null && getChannels().length > 0 && SageApi.Size(searchResults) > 0) {
            List<String> channels_l = Arrays.asList(getChannels());
            if (channels_l.contains("**Any**")) {
                if (getChannels().length > 1) setChannels(new String[] { "**Any**" });
            } else {
                Object allFiltered = null;
                for (Iterator<String> it = channels_l.iterator(); it.hasNext(); ) {
                    String channel = (String) it.next();
                    try {
                        Integer chID = new Integer(channel);
                        Object channelObj = SageApi.Api("GetChannelForStationID", new Object[] { chID });
                        if (channelObj != null) {
                            Object filtered = SageApi.Api("DataUnion", new Object[] { searchResults });
                            filtered = SageApi.Api("FilterByMethod", new Object[] { filtered, "GetChannel", channelObj, Boolean.TRUE });
                            allFiltered = SageApi.Api("DataUnion", new Object[] { allFiltered, filtered });
                        }
                    } catch (Exception e) {
                        Serve.extLog("error filtering by channel in search: " + e.toString());
                        e.printStackTrace(System.out);
                    }
                }
                searchResults = allFiltered;
            }
        }
        return searchResults;
    }

    private Object filterTvFiles(Object searchResults) throws InvocationTargetException {
        if (archived != null && !archived.equals("any") && SageApi.Size(searchResults) > 0) {
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsLibraryFile", new Boolean(archived.equalsIgnoreCase("set")) });
        }
        if (manrec != null && !manrec.equals("any") && SageApi.Size(searchResults) > 0) {
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsManualRecord", new Boolean(manrec.equalsIgnoreCase("set")) });
        }
        if (autodelete != null && !autodelete.equals("any") && SageApi.Size(searchResults) > 0) {
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsFileCurrentlyRecording", Boolean.FALSE });
            if (autodelete.equals("set")) {
                searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsManualRecord", Boolean.FALSE });
                searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsLibraryFile", Boolean.FALSE });
            }
            int numres = SageApi.Size(searchResults);
            Object newSearchResults = null;
            for (int i = 0; i < numres; i++) {
                Object sageAiring = SageApi.GetElement(searchResults, i);
                Object sageFavorite = SageApi.Api("GetFavoriteForAiring", new Object[] { sageAiring });
                if (autodelete.equals("set")) {
                    if ((sageFavorite == null) || (SageApi.booleanApi("IsAutoDelete", new Object[] { sageFavorite }))) {
                        newSearchResults = SageApi.Api("DataUnion", new Object[] { newSearchResults, sageAiring });
                    }
                } else if (autodelete.equals("cleared")) {
                    if (((sageFavorite != null) && (!SageApi.booleanApi("IsAutoDelete", new Object[] { sageFavorite }))) || (SageApi.booleanApi("IsManualRecord", new Object[] { sageAiring })) || (SageApi.booleanApi("IsLibraryFile", new Object[] { sageAiring }))) {
                        newSearchResults = SageApi.Api("DataUnion", new Object[] { newSearchResults, sageAiring });
                    }
                }
            }
            searchResults = newSearchResults;
        }
        String partials = getPartials();
        if (partials.equals("none")) searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsCompleteRecording", Boolean.TRUE }); else if (partials.equals("only")) {
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsCompleteRecording", Boolean.FALSE });
            searchResults = SageApi.Api("FilterByBoolMethod", new Object[] { searchResults, "IsManualRecord", Boolean.FALSE });
        }
        return searchResults;
    }

    private Object filterFilenames(Object searchResults) throws InvocationTargetException {
        if (filename != null && filename.length() > 0 && SageApi.Size(searchResults) > 0) {
            List<Object> newSearchResults = new ArrayList<Object>();
            String regex = isRegex() ? filename : Wildcard.toRegex(filename);
            Pattern pattern = null;
            try {
                pattern = Pattern.compile(regex, isCaseSensitive() ? 0 : (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
            } catch (PatternSyntaxException e) {
                return newSearchResults;
            }
            int numres = SageApi.Size(searchResults);
            for (int i = 0; i < numres; i++) {
                Object sageAiring = SageApi.GetElement(searchResults, i);
                File files[] = (File[]) SageApi.Api("GetSegmentFiles", new Object[] { sageAiring });
                if (files != null) {
                    for (int j = 0; j < files.length; j++) {
                        Matcher matcher = pattern.matcher(files[j].getAbsolutePath());
                        if (matcher.matches()) {
                            newSearchResults.add(sageAiring);
                        }
                    }
                }
            }
            searchResults = newSearchResults;
        }
        return searchResults;
    }

    private Object sort(Object searchResults) throws InvocationTargetException {
        if (searchResults != null) {
            int numprogs = SageApi.Size(searchResults);
            if (numprogs > 0) {
                String sort = getSort2();
                Boolean SortOrder = Boolean.FALSE;
                if (sort != null) {
                    if (sort.endsWith("_desc")) SortOrder = Boolean.TRUE;
                    if (sort.startsWith("airdate_")) {
                        searchResults = SageApi.Api("Sort", new Object[] { searchResults, SortOrder, "GetAiringStartTime" });
                    } else if (sort.startsWith("origairdate_")) {
                        searchResults = SageApi.Api("Sort", new Object[] { searchResults, SortOrder, "GetOriginalAiringDate" });
                    } else if (sort.startsWith("title_")) {
                        searchResults = SageApi.Api("Sort", new Object[] { searchResults, SortOrder, "GetAiringTitle" });
                    } else if (sort.startsWith("episode_")) {
                        searchResults = SageApi.Api("Sort", new Object[] { searchResults, SortOrder, "GetShowEpisode" });
                    }
                }
                sort = getSort1();
                if (sort == null) sort = "airdate_asc";
                SortOrder = Boolean.FALSE;
                if (sort.endsWith("_desc")) SortOrder = Boolean.TRUE;
                if (sort.startsWith("airdate_")) {
                    searchResults = SageApi.Api("Sort", new Object[] { searchResults, SortOrder, "GetAiringStartTime" });
                } else if (sort.startsWith("origairdate_")) {
                    searchResults = SageApi.Api("Sort", new Object[] { searchResults, SortOrder, "GetOriginalAiringDate" });
                } else if (sort.startsWith("title_")) {
                    searchResults = SageApi.Api("Sort", new Object[] { searchResults, SortOrder, "GetAiringTitle" });
                } else if (sort.startsWith("episode_")) {
                    searchResults = SageApi.Api("Sort", new Object[] { searchResults, SortOrder, "GetShowEpisode" });
                } else if (sort.startsWith("people_")) {
                    searchResults = SageApi.Api("Sort", new Object[] { searchResults, SortOrder, "GetPeopleInShow" });
                }
            }
        }
        return searchResults;
    }
}

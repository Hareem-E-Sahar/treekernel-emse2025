package edu.miami.cs.research.apg.generator.search;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Scanner;
import com.sleepycat.je.DatabaseException;
import edu.miami.cs.research.apg.agregator.itunesextractor.MusicLibraryAnalyzer;
import edu.miami.cs.research.apg.agregator.ontology.SongTrack;
import edu.miami.cs.research.apg.generator.search.ga.PlaylistBasicGeneticAlgorithm;
import edu.miami.cs.research.apg.generator.search.ga.Playlist_1st_GeneticAlgorithmVariant;
import edu.miami.cs.research.apg.generator.search.ga.Playlist_2nd_GeneticAlgorithmVariant;
import edu.miami.cs.research.apg.generator.search.ga.Playlist_3rd_GeneticAlgorithmVariant;
import edu.miami.cs.research.apg.generator.search.ga.Playlist_4th_GeneticAlgorithmVariant;
import edu.miami.cs.research.apg.generator.search.local.PlaylistLocalBeamGeneticSearch;
import edu.miami.cs.research.apg.generator.search.local.PlaylistLocalBeamSearch;
import edu.miami.cs.research.apg.generator.search.local.PlaylistStochasticBeamGeneticSearch;
import edu.miami.cs.research.apg.generator.search.local.PlaylistStochasticBeamSearch;
import edu.miami.cs.research.apg.generator.search.representations.Playlist;
import edu.miami.cs.research.apg.generator.search.representations.PlaylistToM3UConverter;
import edu.miami.cs.research.apg.generator.search.representations.RandomPlaylistCriteriaGenerator;
import edu.miami.cs.research.apg.generator.search.representations.criteria.PlaylistCriteria;
import edu.miami.cs.research.apg.generator.search.test.PlaylistSearchTestFrame;
import edu.miami.cs.research.apg.storage.DatabaseUpdateManager;
import edu.miami.cs.research.apg.storage.DatabaseUpdateManager.XMLUpdateManager;
import edu.miami.cs.research.apg.storage.SongTrackEntityManager;

/**
 * @author Darrius Serrant
 *
 */
public class SearchAlgorithmRunner {

    public Hashtable<String, Class<?>> searchAlgorithmHash;

    private boolean testMode;

    private boolean createM3UFile;

    private boolean preprocessXMLFile;

    private boolean createMusicAnalysisReport;

    private boolean createSearchAlgorithmReport;

    private ArrayList<Class<?>> searchAlgorithmsToRun;

    private ArrayList<String> runnerOptions;

    private SongTrackEntityManager stdbm;

    private XMLUpdateManager updateManager;

    public SearchAlgorithmRunner(ArrayList<Class<?>> searchAlgorithms, ArrayList<String> runOptions) {
        initializeSearchAlgorithmHash();
        if ((searchAlgorithms == null) || searchAlgorithms.size() == 0) {
            throw new IllegalArgumentException("User must specify a list of valid search algorithms. ");
        } else {
            searchAlgorithmsToRun = searchAlgorithms;
            runnerOptions = runOptions;
            parseRunnerOptions();
        }
        testMode = createM3UFile = preprocessXMLFile = createMusicAnalysisReport = createSearchAlgorithmReport = false;
        try {
            updateManager = new DatabaseUpdateManager().getXmlManager();
        } catch (DatabaseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseRunnerOptions() {
        for (String option : runnerOptions) {
            if (option.equals("-xml")) {
                preprocessXMLFile = true;
            } else if (option.equals("-mlar")) {
                createMusicAnalysisReport = true;
            } else if (option.equals("-apr")) {
                createSearchAlgorithmReport = true;
            } else if (option.equals("-m3u")) {
                createM3UFile = true;
            } else if (option.equals("-test")) {
                testMode = true;
            }
        }
    }

    private void initializeSearchAlgorithmHash() {
        searchAlgorithmHash = new Hashtable<String, Class<?>>();
        searchAlgorithmHash.put("PlaylistLocalBeamSearch.class", PlaylistLocalBeamSearch.class);
        searchAlgorithmHash.put("PlaylistLocalBeamGeneticSearch.class", PlaylistLocalBeamGeneticSearch.class);
        searchAlgorithmHash.put("PlaylistStochasticBeamSearch.class", PlaylistStochasticBeamSearch.class);
        searchAlgorithmHash.put("PlaylistStochasticBeamGeneticSearch.class", PlaylistStochasticBeamGeneticSearch.class);
        searchAlgorithmHash.put("PlaylistBasicGeneticAlgorithm.class", PlaylistBasicGeneticAlgorithm.class);
        searchAlgorithmHash.put("Playlist_1st_GeneticAlgorithmVariant.class", Playlist_1st_GeneticAlgorithmVariant.class);
        searchAlgorithmHash.put("Playlist_2nd_GeneticAlgorithmVariant.class", Playlist_2nd_GeneticAlgorithmVariant.class);
        searchAlgorithmHash.put("Playlist_3rd_GeneticAlgorithmVariant.class", Playlist_3rd_GeneticAlgorithmVariant.class);
        searchAlgorithmHash.put("Playlist_4th_GeneticAlgorithmVariant.class", Playlist_4th_GeneticAlgorithmVariant.class);
    }

    public void executeSearchAlgorithms() {
        MusicLibraryAnalyzer analyzer;
        ArrayList<SongTrack> musicTitles2 = new ArrayList<SongTrack>();
        if (preprocessXMLFile) {
            updateManager.updateSongTrackEntityStore();
            if (createMusicAnalysisReport) {
                updateManager.createMusicAnalyzerReport();
            }
            updateManager.serializeMusicLibrary();
            musicTitles2.addAll(updateManager.getMusicTitles());
            analyzer = updateManager.getMusicLibraryAnalyzer();
        } else {
            analyzer = updateManager.deserializePreviousAnalysisFile();
            if (createMusicAnalysisReport) {
                updateManager.createMusicAnalyzerReport();
            }
            try {
                musicTitles2.addAll(updateManager.getMusicTitles());
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.out.println(" Commencing test procedures on implemented SearchAlgorithms...");
        ArrayList<Playlist> bestPlaylists = runSearchAlgorithms(analyzer, musicTitles2, 0.0);
        System.out.println(" Testing complete.");
        if (createM3UFile) {
            for (Playlist p : bestPlaylists) {
                try {
                    PlaylistToM3UConverter.createM3UFile(p, stdbm, retrievePropertyValue("apg.itunes.reports.playlists.location"));
                } catch (Exception e) {
                    reportError(e, false);
                }
            }
            System.out.println(" Generated playlists outputted to preconfigured directory location. ");
        }
        System.out.println(" Saving data and closing out database connections...");
        serializeMusicLibraryAnalyzer(analyzer);
        stdbm.closeDatabase();
    }

    private void serializeMusicLibraryAnalyzer(MusicLibraryAnalyzer analyzer) {
        try {
            FileOutputStream fileOut = new FileOutputStream("employee.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(analyzer);
            out.close();
            fileOut.close();
        } catch (Exception exception) {
            reportError(exception, true);
        }
    }

    private String retrievePropertyValue(String property) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(SongTrackEntityManager.PROPERTIES_FILE_LOCATION));
        } catch (Exception e) {
            reportError(e, true);
        }
        return properties.getProperty(property);
    }

    private ArrayList<Playlist> runSearchAlgorithms(MusicLibraryAnalyzer analyzer, ArrayList<SongTrack> musicTitles2, double targetCost) {
        ArrayList<Playlist> pLists = null;
        try {
            if (stdbm == null) {
                stdbm = new SongTrackEntityManager();
            }
            PlaylistCriteria criteria = new PlaylistCriteria();
            if (testMode) {
                System.out.println(" Formulating playlist search criteria...");
                criteria = getSearchCriteria(analyzer);
            } else {
            }
            System.out.println(" Initializing search algorithms...");
            ArrayList<PlaylistSearchAlgorithm> algorithms = initializeSearchAlgorithms(criteria, musicTitles2, stdbm, targetCost);
            System.out.println(" Establishing a PlaylistSearchTestFrame...");
            PlaylistSearchTestFrame frame = new PlaylistSearchTestFrame(algorithms);
            musicTitles2 = null;
            System.out.println(" Testing search algorithms...");
            System.gc();
            pLists = frame.runTest(createSearchAlgorithmReport, retrievePropertyValue("apg.itunes.reports.algorithmanalysis.location"));
        } catch (Exception e) {
            reportError(e, true);
        }
        return pLists;
    }

    private PlaylistCriteria getSearchCriteria(MusicLibraryAnalyzer analyzer) {
        RandomPlaylistCriteriaGenerator generator = new RandomPlaylistCriteriaGenerator(analyzer);
        PlaylistCriteria criteria = new PlaylistCriteria();
        try {
            criteria = generator.generatePlaylistCriteria(8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(" Playlist criteria created.");
        return criteria;
    }

    private ArrayList<PlaylistSearchAlgorithm> initializeSearchAlgorithms(PlaylistCriteria criteria, ArrayList<SongTrack> musicTitles, SongTrackEntityManager stdbm2, double targetCost) {
        ArrayList<PlaylistSearchAlgorithm> algorithms = new ArrayList<PlaylistSearchAlgorithm>();
        try {
            for (Class<?> a : searchAlgorithmsToRun) {
                Constructor<?> algorithmConstructor = a.getConstructor(PlaylistCriteria.class, ArrayList.class, SongTrackEntityManager.class, Double.class);
                algorithms.add((PlaylistSearchAlgorithm) algorithmConstructor.newInstance(criteria, musicTitles, stdbm, targetCost));
            }
        } catch (Exception exception) {
            reportError(exception, true);
        }
        return algorithms;
    }

    private void reportError(Exception e, boolean b) {
        System.out.print(" An error has occured. Type 'q' for more details. ");
        Scanner inputScanner = new Scanner(System.in);
        String input = inputScanner.nextLine();
        if (input.equals("q")) {
            e.printStackTrace();
        }
        if (b) {
            if (stdbm != null) {
                stdbm.closeDatabase();
            }
            System.exit(1);
        }
    }
}

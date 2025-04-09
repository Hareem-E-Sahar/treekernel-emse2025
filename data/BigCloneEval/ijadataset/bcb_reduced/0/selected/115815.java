package thesis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import sun.management.MXBeanSupport;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.EuclideanDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 * Dit is een singleton klasse om te voorkomen dat andere developers
 * meerdere gegevensbank objecten aanmaken.
 * 
 * @author Kristof Claes & Wouter Debecker
 *
 */
public class Gegevensbank {

    private static Gegevensbank ref;

    private static Analyzer analyzer;

    private static IndexModifier indexModifier;

    private Gegevensbank() {
    }

    /**
	 * Initialiseer het Gegevensbank object.
	 * 
	 * @return
	 */
    public static Gegevensbank getGegevensbank() {
        if (ref == null) ref = new Gegevensbank();
        return ref;
    }

    public ArrayList zoekChunkMetChunksVanBronnenInResultaatOpgenomen(ArrayList chunk) {
        ArrayList resultaat = new ArrayList();
        ArrayList bronnen = new ArrayList<String>();
        ArrayList chunks_van_verdachte_bronnen = new ArrayList();
        String chunkString = "";
        for (int i = 0; i < Settings.getCHUNKGROOTTE(); i++) {
            chunkString += chunk.get(i) + " ";
        }
        chunkString = chunkString.trim();
        try {
            IndexSearcher indexSearcher = new IndexSearcher(Settings.getIndexdir());
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser qparser = new QueryParser("chunk", analyzer);
            String chunkQuery = "chunk:\"" + chunkString + "\"";
            Query query = qparser.parse(chunkQuery);
            Hits hits = indexSearcher.search(query);
            String[] chunkvalues;
            String path;
            for (int i = 0; i < hits.length(); i++) {
                path = hits.doc(i).get("path");
                bronnen.add(path);
                chunkvalues = hits.doc(i).getValues("chunk");
                chunks_van_verdachte_bronnen.add(chunkvalues);
            }
            indexSearcher.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        resultaat.add(bronnen);
        resultaat.add(chunks_van_verdachte_bronnen);
        return resultaat;
    }

    /**
	 * Hierin dus controleren of de chunk in de gegevensbank zit,
	 * indien niet: null teruggeven.
	 * Indien wel, strings terugggeven die de files aanduiden waarin de chunk voorkomt.
	 * Het is mogelijk dat eenzelfde string meerdere keren in hetzelfde document voorkomt.
	 * @param chunk
	 * @return
	 */
    public ArrayList zoekChunk(ArrayList chunk) {
        ArrayList resultaat = new ArrayList();
        ArrayList bronnen = new ArrayList<String>();
        ArrayList chunks_van_verdachte_bronnen = new ArrayList();
        if (chunk == null) {
            System.out.println("Meegegeven chunk is null.");
        }
        String chunkString = "";
        for (int i = 0; i < Settings.getCHUNKGROOTTE(); i++) {
            chunkString += chunk.get(i) + " ";
        }
        chunkString = chunkString.trim();
        try {
            IndexSearcher indexSearcher = new IndexSearcher(Settings.getIndexdir());
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser qparser = new QueryParser("chunk", analyzer);
            String chunkQuery = "chunk:\"" + chunkString + "\"";
            Query query = qparser.parse(chunkQuery);
            Hits hits = indexSearcher.search(query);
            String path;
            for (int i = 0; i < hits.length(); i++) {
                path = hits.doc(i).get("path");
                bronnen.add(path);
            }
            indexSearcher.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        resultaat.add(bronnen);
        resultaat.add(chunks_van_verdachte_bronnen);
        return resultaat;
    }

    /**
	 * Controleren of er in de gegevensbank een bestand bestaat dat de gegeven string letterlijk bevat.
	 * Gewijzigde voorkomens (woord veranderd etc) zullen met deze methode dus niet gevonden worden.
	 * @param inhoud
	 * 	      De te zoeken tekst.
	 * @return
	 * 		  Een lijst van strings die de bestandspaden van de gevonden bronnen bevat.
	 */
    public String[] zoekIdentiekVoorkomen(String inhoud) {
        String[] retString = null;
        if (inhoud == null) {
            System.out.println("Meegegeven inhoud is null.");
        }
        try {
            IndexSearcher indexSearcher = new IndexSearcher(Settings.getIndexdir());
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser qparser = new QueryParser("fullcontent", analyzer);
            String inhoudQuery = "fullcontent:\"" + inhoud + "\"";
            Query query = qparser.parse(inhoudQuery);
            Hits hits = indexSearcher.search(query);
            retString = new String[hits.length()];
            String path;
            for (int i = 0; i < hits.length(); i++) {
                path = hits.doc(i).get("path");
                retString[i] = path;
            }
            indexSearcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return retString;
    }

    /**
	 * Controleren of er in de gegevensbank een bestand bestaat dat als inhoud een tekst heeft die in "zekere mate"
	 * op de gegeven string gelijkt.
	 * De mate van overeenkomst wordt berekend aan de hand van een similarity measure, berekend met het
	 * SimMetrics pakket.
	 * @param inhoud
	 * 		  De te controleren tekst.
	 * @return
	 * 		  Een ArrayList die als eerste element de bronnen bevat en
	 *		  als tweede element de overeenkomstige matching percentages en
	 *		  als derde element het totale matching percentage.
	 */
    public ArrayList zoekGewijzigdVoorkomen(String inhoud, ArrayList verdachtebronnen) {
        ArrayList result = new ArrayList();
        ArrayList bronnen = new ArrayList();
        ArrayList percentages = new ArrayList();
        float totalematching = 0;
        try {
            IndexSearcher indexSearcher = new IndexSearcher(Settings.getIndexdir());
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser qparser = new QueryParser("validforsimilaritymeasurecomparison", analyzer);
            String inhoudQuery = "validforsimilaritymeasurecomparison:\"" + "yes" + "\"";
            Query query = qparser.parse(inhoudQuery);
            Hits hits = indexSearcher.search(query);
            String[] bronnen_inhoud = new String[hits.length()];
            String[] bronnen_paden = new String[hits.length()];
            AbstractStringMetric metric = new CosineSimilarity();
            float[] matchingwaarden;
            for (int i = 0; i < hits.length(); i++) {
                bronnen_inhoud[i] = hits.doc(i).get("fullcontent");
                bronnen_paden[i] = hits.doc(i).get("path");
            }
            int nbdoc = bronnen_inhoud.length;
            System.out.println("Aantal in batch-compare: " + nbdoc);
            System.out.println("STARTING BATCH COMPARE.");
            matchingwaarden = metric.batchCompareSet(bronnen_inhoud, inhoud);
            System.out.println("compare gedaan.");
            float matchingpercentage_i;
            for (int i = 0; i < matchingwaarden.length; i++) {
                System.out.println("i: " + i);
                matchingpercentage_i = matchingwaarden[i] * 100;
                if (matchingpercentage_i > Settings.getMINIMUM_MATCHING_PERCENTAGE_TO_SHOW_UP_IN_RESULTS()) {
                    bronnen.add(bronnen_paden[i]);
                    percentages.add(matchingpercentage_i);
                }
            }
            totalematching = maximumVanRij(matchingwaarden) * 100;
            indexSearcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result = getGesorteerdeVersieVanBronnenEnPercentages(bronnen, percentages);
        result.add(totalematching);
        return result;
    }

    public ArrayList zoekDocumentenMetGemeenschappelijkeMinstVoorkomendeWoordenMetVoorbereidendeFilter(String minst_voorkomende_woorden_string) {
        ArrayList result = new ArrayList();
        ArrayList bronnen = new ArrayList();
        ArrayList percentages = new ArrayList();
        float totalematching = 0;
        String[] minstvoorkomendewoorden = minst_voorkomende_woorden_string.split(" ");
        System.out.println("Testdocument bevat: " + minstvoorkomendewoorden.length + " minst voorkomende woorden.");
        int max_nb_woorden_in_filterset = Settings.getNB_WORDS_IN_WOORDFREQUENTIE_FILTER();
        int nb_woorden_in_filterset = Math.min(max_nb_woorden_in_filterset, minstvoorkomendewoorden.length);
        String[] filterset = new String[nb_woorden_in_filterset];
        for (int i = 0; i < nb_woorden_in_filterset && i < minstvoorkomendewoorden.length; i++) {
            filterset[i] = minstvoorkomendewoorden[i];
        }
        try {
            IndexSearcher indexSearcher = new IndexSearcher(Settings.getIndexdir());
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser qparser = new QueryParser("fullcontent", analyzer);
            String inhoudQuery = "fullcontent:\"" + filterset[0] + "\"";
            if (filterset.length > 1) {
                for (int i = 1; i < filterset.length; i++) inhoudQuery = inhoudQuery.concat(" OR fullcontent:\"" + filterset[i] + "\"");
            }
            Query query = qparser.parse(inhoudQuery);
            Hits hits = indexSearcher.search(query);
            String[] bronnen_woordfrequentiestring = new String[hits.length()];
            String[] bronnen_paden = new String[hits.length()];
            float[] matchingwaarden = new float[hits.length()];
            for (int i = 0; i < hits.length(); i++) {
                bronnen_woordfrequentiestring[i] = hits.doc(i).get("woordfrequentiestring");
                bronnen_paden[i] = hits.doc(i).get("path");
            }
            for (int i = 0; i < bronnen_woordfrequentiestring.length; i++) {
                String currentwfs = bronnen_woordfrequentiestring[i];
                String[] currentwoorden = currentwfs.split(" ");
                int aantal_gemeenschappelijke_woorden = 0;
                for (int j = 0; j < minstvoorkomendewoorden.length; j++) {
                    if (bevatElement(currentwoorden, minstvoorkomendewoorden[j])) {
                        aantal_gemeenschappelijke_woorden++;
                    }
                }
                matchingwaarden[i] = aantal_gemeenschappelijke_woorden / (float) minstvoorkomendewoorden.length;
            }
            indexSearcher.close();
            float matchingpercentage_i;
            for (int i = 0; i < matchingwaarden.length; i++) {
                matchingpercentage_i = matchingwaarden[i] * 100;
                if (matchingpercentage_i >= Settings.getMINIMUM_MATCHING_PERCENTAGE_TO_SHOW_UP_IN_RESULTS()) {
                    percentages.add(matchingpercentage_i);
                    bronnen.add(bronnen_paden[i]);
                }
            }
            totalematching = maximumVanRij(matchingwaarden) * 100;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result = getGesorteerdeVersieVanBronnenEnPercentages(bronnen, percentages);
        result.add(totalematching);
        return result;
    }

    public ArrayList zoekDocumentenMetGemeenschappelijkeMinstVoorkomendeWoorden_OUD_paarsgewijze_vergelijking(String minst_voorkomende_woorden_string) {
        ArrayList result = new ArrayList();
        ArrayList bronnen = new ArrayList();
        ArrayList percentages = new ArrayList();
        float totalematching = 0;
        try {
            IndexSearcher indexSearcher = new IndexSearcher(Settings.getIndexdir());
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser qparser = new QueryParser("validforsimilaritymeasurecomparison", analyzer);
            String inhoudQuery = "validforsimilaritymeasurecomparison:\"" + "yes" + "\"";
            Query query = qparser.parse(inhoudQuery);
            Hits hits = indexSearcher.search(query);
            String[] bronnen_woordfrequentiestring = new String[hits.length()];
            String[] bronnen_paden = new String[hits.length()];
            float[] matchingwaarden = new float[hits.length()];
            String[] minstvoorkomendewoorden = minst_voorkomende_woorden_string.split(" ");
            for (int i = 0; i < hits.length(); i++) {
                bronnen_woordfrequentiestring[i] = hits.doc(i).get("woordfrequentiestring");
                bronnen_paden[i] = hits.doc(i).get("path");
            }
            for (int i = 0; i < bronnen_woordfrequentiestring.length; i++) {
                String currentwfs = bronnen_woordfrequentiestring[i];
                String[] currentwoorden = currentwfs.split(" ");
                int aantal_gemeenschappelijke_woorden = 0;
                for (int j = 0; j < minstvoorkomendewoorden.length; j++) {
                    if (bevatElement(currentwoorden, minstvoorkomendewoorden[j])) {
                        aantal_gemeenschappelijke_woorden++;
                    }
                }
                matchingwaarden[i] = aantal_gemeenschappelijke_woorden / (float) minstvoorkomendewoorden.length;
            }
            float matchingpercentage_i;
            for (int i = 0; i < matchingwaarden.length; i++) {
                matchingpercentage_i = matchingwaarden[i] * 100;
                if (matchingpercentage_i >= Settings.getMINIMUM_MATCHING_PERCENTAGE_TO_SHOW_UP_IN_RESULTS()) {
                    percentages.add(matchingpercentage_i);
                    bronnen.add(bronnen_paden[i]);
                }
            }
            totalematching = maximumVanRij(matchingwaarden) * 100;
            indexSearcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result = getGesorteerdeVersieVanBronnenEnPercentages(bronnen, percentages);
        result.add(totalematching);
        return result;
    }

    /**
	 * Controleren of er in de gegevensbank een bestand bestaat dat als woordfrequentiestring een string heeft die in 
	 * "zekere mate" op de gegeven string lijkt.
	 * De mate van overeenkomst wordt berekend aan de hand van een similarity measure, berekend met het
	 * SimMetrics pakket.
	 * @param woordfrequentiestring
	 * 		  De te vergelijken woordfrequentiestring.
	 * @return
	 * 		  Een ArrayList die als eerste element de bronnen bevat en
	 *		  als tweede element de overeenkomstige matching percentages en
	 *		  als derde element het totale matching percentage.
	 */
    public ArrayList zoekWoordfrequentiestring(String woordfrequentiestring, ArrayList verdachtebronnen) {
        ArrayList result = new ArrayList();
        ArrayList bronnen = new ArrayList();
        ArrayList percentages = new ArrayList();
        float totalematching = 0;
        try {
            IndexSearcher indexSearcher = new IndexSearcher(Settings.getIndexdir());
            Analyzer analyzer = new StandardAnalyzer();
            if (verdachtebronnen == null || verdachtebronnen.size() == 0) {
                QueryParser qparser = new QueryParser("validforsimilaritymeasurecomparison", analyzer);
                String inhoudQuery = "validforsimilaritymeasurecomparison:\"" + "yes" + "\"";
                Query query = qparser.parse(inhoudQuery);
                Hits hits = indexSearcher.search(query);
                String[] bronnen_woordfrequentiestring = new String[hits.length()];
                String[] bronnen_paden = new String[hits.length()];
                AbstractStringMetric metric = new Levenshtein();
                float[] matchingwaarden;
                for (int i = 0; i < hits.length(); i++) {
                    bronnen_woordfrequentiestring[i] = hits.doc(i).get("woordfrequentiestring");
                    bronnen_paden[i] = hits.doc(i).get("path");
                }
                matchingwaarden = metric.batchCompareSet(bronnen_woordfrequentiestring, woordfrequentiestring);
                float matchingpercentage_i;
                for (int i = 0; i < matchingwaarden.length; i++) {
                    matchingpercentage_i = matchingwaarden[i] * 100;
                    if (matchingpercentage_i > Settings.getMINIMUM_MATCHING_PERCENTAGE_TO_SHOW_UP_IN_RESULTS()) {
                        percentages.add(matchingpercentage_i);
                        bronnen.add(bronnen_paden[i]);
                    }
                }
                totalematching = maximumVanRij(matchingwaarden) * 100;
            } else {
            }
            indexSearcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result = getGesorteerdeVersieVanBronnenEnPercentages(bronnen, percentages);
        result.add(totalematching);
        return result;
    }

    private ArrayList getGesorteerdeVersieVanBronnenEnPercentages(ArrayList bronnen_unsorted, ArrayList percentages_unsorted) {
        ArrayList resultaat = new ArrayList();
        ArrayList bronnen = new ArrayList();
        ArrayList percentages = new ArrayList();
        int aantalgesorteerd = 0;
        float vorigmaximum = 101;
        float maximum = 0;
        while (aantalgesorteerd < bronnen_unsorted.size()) {
            maximum = 0;
            for (int i = 0; i < percentages_unsorted.size(); i++) {
                float percentage = (Float) (percentages_unsorted.get(i));
                if (percentage > maximum && percentage < vorigmaximum) {
                    maximum = percentage;
                }
            }
            for (int i = 0; i < percentages_unsorted.size(); i++) {
                float percentage = (Float) (percentages_unsorted.get(i));
                if (percentage == maximum) {
                    bronnen.add(bronnen_unsorted.get(i));
                    percentages.add(percentages_unsorted.get(i));
                    aantalgesorteerd++;
                }
            }
            vorigmaximum = maximum;
        }
        resultaat.add(bronnen);
        resultaat.add(percentages);
        return resultaat;
    }

    private boolean bevatElement(String[] rij, String element) {
        for (int i = 0; i < rij.length; i++) {
            if (rij[i].equalsIgnoreCase(element)) return true;
        }
        return false;
    }

    private float maximumVanRij(float[] rij) {
        float result = 0;
        for (int i = 0; i < rij.length; i++) {
            if (Float.toString(rij[i]).equals("NaN")) {
            } else {
                result = Math.max(result, rij[i]);
            }
        }
        return result;
    }

    /**
	 * Voegt de documenten in de meegegeven directory recursief (dus 
	 * ook de files van de subdirectories) toe aan de database.
	 * 
	 * @param 	locatie		De locatie (pad) van het bestand of directory
	 * @param 	deep		Boolean die op true wordt gezet als er dieper dan 1
	 * 						nieuw wordt ingelezen, zodat niet meer dan 1 lucene
	 * 						analyzer en indexModifier wordt gecreëerd.
	 * @return		True indien toevoegen geslaagd, false anders.
	 * @throws IOException
	 */
    public boolean voegDocumentenToe(File locatie, boolean deep) throws IOException {
        if (!locatie.exists()) {
            return false;
        }
        File[] documentFiles;
        if (locatie.isDirectory()) {
            documentFiles = locatie.listFiles();
            System.out.println("DIRECTORY");
        } else {
            documentFiles = new File[1];
            documentFiles[0] = locatie;
            System.out.println("FILE");
        }
        if (documentFiles == null) return false;
        Document document = new Document();
        DTDocument dtdocument;
        File currentfile;
        Inlezer inlezer = new Inlezer();
        if (!deep) {
            analyzer = new StandardAnalyzer();
            indexModifier = new IndexModifier(Settings.getIndexdir(), analyzer, false);
        }
        for (int i = 0; i < documentFiles.length; i++) {
            currentfile = documentFiles[i];
            if (currentfile.isDirectory()) {
                System.out.println(documentFiles[i].getPath() + "  (DIRECTORY) ... verder uitdiepen=>");
                voegDocumentenToe(documentFiles[i], true);
            } else if (currentfile.isFile()) {
                String documentpath = currentfile.getPath();
                dtdocument = inlezer.leesBestandIn(documentpath);
                document = DTDocument.dtdocument2document(dtdocument);
                document.add(new Field("id", "1", Field.Store.YES, Field.Index.UN_TOKENIZED));
                Term term = new Term("path", documentpath);
                indexModifier.deleteDocuments(term);
                indexModifier.addDocument(document);
                indexModifier.optimize();
            }
        }
        if (!deep) {
            indexModifier.optimize();
            indexModifier.close();
        }
        return true;
    }

    public boolean verwijderDocumenten(String pad) {
        try {
            analyzer = new StandardAnalyzer();
            indexModifier = new IndexModifier(Settings.getIndexdir(), analyzer, false);
            String documentpath = pad;
            Term term = new Term("path", documentpath);
            int nb1 = indexModifier.docCount();
            System.out.println("aantal doc voor delete: " + nb1);
            indexModifier.deleteDocuments(term);
            int nb2 = indexModifier.docCount();
            System.out.println("aantal doc na delete: " + nb2);
            indexModifier.optimize();
            indexModifier.optimize();
            indexModifier.close();
            if (nb1 == nb2) return false;
            return true;
        } catch (IOException exc) {
            return false;
        }
    }

    public int getNbDocumentsInDatabase() {
        try {
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriter indexWriter = new IndexWriter(Settings.getIndexdir(), analyzer, false);
            int nbdocs = indexWriter.docCount();
            indexWriter.close();
            return nbdocs;
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return -1;
    }

    public boolean resetGegevensbank() {
        System.out.println("Aantal: " + getNbDocumentsInDatabase());
        try {
            analyzer = new StandardAnalyzer();
            IndexWriter iw = new IndexWriter(Settings.getIndexdir(), analyzer, true);
            iw.close();
        } catch (IOException exc) {
            exc.printStackTrace();
            return false;
        }
        System.out.println("Nieuw aantal: " + getNbDocumentsInDatabase());
        return true;
    }

    public static IndexModifier getIndexModifier() {
        return indexModifier;
    }

    private static void setIndexModifier(IndexModifier iw) {
        Gegevensbank.indexModifier = iw;
    }
}

package news_rack.search;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import news_rack.database.NewsItem;
import news_rack.archiver.Source;
import news_rack.news_filter.Concept;
import news_rack.news_filter.RuleTerm;

public class Search {

    /**
	 * Adds a news item to the index.  The news item object
	 * has all necessary information like URL, local copy name,
	 * feed id and date of the news item
	 *    n.GetReader() will get you a reader object to read the contents of the news item
	 *    n.GetFeedId() will get you the unique feed id that this news item belongs to 
	 *    Look at NewsItem's API to know about the rest
	 * @param n news item to add to the search index
	 */
    public void AddDocument(NewsItem n) {
        LuceneInterface _lf = new LuceneInterface();
        _lf.AddNewsItemToIndex(n);
    }

    private Set GetMatches(String query) {
        LuceneInterface lf = new LuceneInterface();
        SearchFiles sf = new SearchFiles();
        return sf.SearchIndexedFiles(lf._indexFile.getAbsolutePath(), query);
    }

    /**
	 * Return a set of matching documents that contain the keyword k
	 * @param k the keyword to match
	 **/
    public Set GetMatchingDocuments(String k) {
        return GetMatches("contents: ( " + k + " )");
    }

    public Set GetMatchingDocuments(String k, Source s) {
        return GetMatches("source:" + s + " AND contents: ( " + k + " )");
    }

    public Set GetMatchingDocuments(String k, Source s, Date d) {
        return GetMatches("source:" + s + " AND date: " + d + " AND contents: ( " + k + " )");
    }

    private static void AppendKeywords(StringBuffer qb, Iterator ks) {
        qb.append("contents: ( ");
        if (ks.hasNext()) {
            qb.append(ks.next().toString());
            while (ks.hasNext()) {
                qb.append(" OR " + ks.next().toString());
            }
        }
        qb.append(" )");
    }

    /**
	 * Return a set of matching documents that contain any of the
	 * keywords in the list "ks"
	 **/
    public Set GetMatchingDocuments(Iterator ks) {
        StringBuffer qb = new StringBuffer();
        AppendKeywords(qb, ks);
        return GetMatches(qb.toString());
    }

    public Set GetMatchingDocuments(Iterator ks, Source s) {
        StringBuffer qb = "source:" + s + " AND ";
        AppendKeywords(qb, ks);
        return GetMatches(qb.toString());
    }

    public Set GetMatchingDocuments(Iterator ks, Source s, Date d) {
        StringBuffer qb = "source:" + s + " AND date: " + d + " AND ";
        AppendKeywords(qb, ks);
        return GetMatches(qb.toString());
    }

    /**
	 * Return a set of matching documents that contain the
	 * concept "c"
	 **/
    public Set GetMatchingDocuments(Concept c) {
        return GetMatchingDocuments(c.GetKeywords());
    }

    public Set GetMatchingDocuments(Concept c, Source s) {
        return GetMatchingDocuments(c.GetKeywords(), s);
    }

    public Set GetMatchingDocuments(Concept c, Source s, Date d) {
        return GetMatchingDocuments(c.GetKeywords(), s, d);
    }

    /**
	public Set GetMatchingDocuments(RuleTerm r)
	{
	}

	public Set GetMatchingDocuments(RuleTerm r, Source s)
	{
	}

	public Set GetMatchingDocuments(RuleTerm r, Source s, Date d)
	{
	}
**/
    public void createTempIndex(File rootDocDir) {
        LuceneInterface _lf = new LuceneInterface();
        System.out.println("index dir ==>" + _lf._indexFile.getAbsolutePath());
        if (rootDocDir.isDirectory()) {
            String[] files = rootDocDir.list();
            Arrays.sort(files);
            for (int i = 0; i < files.length; i++) {
                String date = files[i].toString();
                File dateDir = new File(rootDocDir, files[i].toString());
                String[] dateFiles = dateDir.list();
                Arrays.sort(dateFiles);
                for (int j = 0; j < dateFiles.length; j++) {
                    NewsItem ni = new NewsItem();
                    File docFile = new File(dateDir, dateFiles[j]);
                    ni.SetLocalPath(docFile.getAbsolutePath());
                    ni.SetDate(date);
                    ni.SetSource(ni._sourceId);
                    ni.SetURL("www.rediff.com");
                    System.out.println(" patth ==>" + ni.GetLocalCopyPath());
                    System.out.println(" date ==>" + ni.GetDateString());
                    System.out.println(" source ==>" + ni.GetSourceId());
                    _lf.AddNewsItemToIndex(ni);
                }
            }
        }
    }

    public void DefineIndex(String indexDirPathString) {
        LuceneInterface _lf = new LuceneInterface();
        File indexDirPath = new File(indexDirPathString);
        if (indexDirPath.canRead()) {
            if (indexDirPath.isDirectory()) {
                _lf.DefineIndex(indexDirPath + "/index");
            }
        }
    }

    public void CreateIndex(String indexDirPathString) {
        LuceneInterface _lf = new LuceneInterface();
        File indexDirPath = new File(indexDirPathString);
        if (indexDirPath.canRead()) {
            if (indexDirPath.isDirectory()) {
                _lf.CreateIndex(indexDirPath + "/index");
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("provide query");
            System.exit(0);
        }
        String source = new String();
        String date = new String();
        String query = new String();
        for (int i = 0; i < args.length; i++) {
            if ("-source".equals(args[i])) {
                source = args[i + 1];
                i++;
            } else if ("-date".equals(args[i])) {
                date = args[i + 1];
                i++;
            } else if ("-query".equals(args[i])) {
                query = args[i + 1];
                i++;
            }
        }
        try {
            LuceneInterface _lf = new LuceneInterface();
            GlobalConstants _gc = new GlobalConstants();
            Search srch = new Search();
            _gc.Init();
            File indexDirPath = new File("/home/mtech/newsrack/test");
            if (indexDirPath.canRead()) {
                if (indexDirPath.isDirectory()) {
                    File rootDocDir = new File("/home/mtech/newsrack/test/filtered");
                }
            }
            SearchFiles sF = new SearchFiles();
            List keywords = new ArrayList();
            keywords.add("india");
            keywords.add("mumbai");
            keywords.add("narmada");
            keywords.add("bangalore");
            keywords.add("cricket");
            keywords.add("science");
            keywords.add("prime");
            keywords.add("terrorist");
            Set resultSet;
            if (source.equals("") && date.equals("")) resultSet = srch.GetMatchingDocuments(query); else if (date.equals("")) resultSet = srch.GetMatchingDocuments(query, source); else resultSet = srch.GetMatchingDocuments(query, source, date);
            SearchResult result = new SearchResult();
            if (!resultSet.isEmpty()) {
                Iterator itr = resultSet.iterator();
                while (itr.hasNext()) {
                    result = (SearchResult) itr.next();
                    System.out.println("path ==> " + result._localPath);
                    System.out.println("date ==> " + result._date);
                    System.out.println("title ==> " + result._title);
                    System.out.println("summary ==> " + result._summary);
                }
            }
        } catch (Exception e) {
        }
    }
}

package bioevent.semanticsimilarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import pitt.search.semanticvectors.BuildIndex;
import pitt.search.semanticvectors.CompoundVectorBuilder;
import pitt.search.semanticvectors.Flags;
import pitt.search.semanticvectors.LuceneUtils;
import pitt.search.semanticvectors.VectorStoreReaderLucene;
import pitt.search.semanticvectors.VectorUtils;
import bioevent.core.Configuration;
import bioevent.core.Util;

public class SemanticVector {

    String luceneIndexPath;

    Directory dir;

    VectorStoreReaderLucene vecReader = null;

    LuceneUtils luceneUtils = null;

    public static void main(String[] args) throws Exception {
        String trainingRoot = args[0];
        SemanticVector sv = new SemanticVector();
        sv.createIndex(trainingRoot);
    }

    public SemanticVector() {
        luceneIndexPath = Configuration.getValue("LuceneIndexFile");
        try {
            dir = FSDirectory.open(new File(luceneIndexPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            vecReader = new VectorStoreReaderLucene(Flags.queryvectorfile);
        } catch (IOException e) {
            Util.log("Failed to open vector store from file: " + Flags.queryvectorfile, 3);
        }
        Util.log("Opened query vector store from file: " + Flags.queryvectorfile, 2);
        try {
            luceneUtils = new LuceneUtils(luceneIndexPath);
        } catch (IOException e) {
            Util.log("Couldn't open Lucene index at " + luceneIndexPath, 3);
        }
        if (luceneUtils == null) {
            Util.log("No Lucene index for query term weighting, " + "so all query terms will have same weight.", 2);
        }
    }

    public void createIndex(String docsPath) {
        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + luceneIndexPath + "'...");
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
            iwc.setOpenMode(OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);
            writer.close();
            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");
            Util.log("Building Semantic Vector Index...", 2);
            Flags.maxnonalphabetchars = 2;
            BuildIndex.main(new String[] { luceneIndexPath });
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    private void indexDocs(IndexWriter writer, File file) throws IOException {
        if (!file.canRead()) {
            Util.log("Error in indexing, File unreadable:" + file.getPath(), 3);
            return;
        }
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    indexDocs(writer, new File(file, files[i]));
                }
            }
        } else {
            if (!file.getName().endsWith(".txt")) return;
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException fnfe) {
                Util.log("Error in indexing:" + fnfe.getMessage(), 3);
                return;
            }
            try {
                Document doc = new Document();
                Field pathField = new Field("path", file.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
                pathField.setOmitTermFreqAndPositions(true);
                doc.add(pathField);
                NumericField modifiedField = new NumericField("modified");
                modifiedField.setLongValue(file.lastModified());
                doc.add(modifiedField);
                doc.add(new Field("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));
                if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                    System.out.println("adding " + file);
                    writer.addDocument(doc);
                } else {
                    System.out.println("updating " + file);
                    writer.updateDocument(new Term("path", file.getPath()), doc);
                }
            } catch (Exception e) {
                Util.log("Error in indexing:" + e.getMessage(), 3);
            } finally {
                fis.close();
            }
        }
    }

    public double getSimilarity(String term1, String term2) {
        float similarity = 0;
        term1 = term1.toLowerCase();
        term2 = term2.toLowerCase();
        String term1_unique = "";
        String term2_unique = "";
        for (String cterm : term1.split(" ")) {
            boolean is_unique = true;
            for (String cterm2 : term2.split(" ")) if (cterm2.equals(cterm)) {
                is_unique = false;
                break;
            }
            if (is_unique && !Util.isStopWord(cterm)) term1_unique += cterm + " ";
        }
        for (String cterm : term2.split(" ")) {
            boolean is_unique = true;
            for (String cterm2 : term1.split(" ")) if (cterm2.equals(cterm)) {
                is_unique = false;
                break;
            }
            if (is_unique && !Util.isStopWord(cterm)) term2_unique += cterm + " ";
        }
        if (term2_unique.equals("") || term1_unique.equals("")) return 0;
        try {
            float[] vec1 = VectorUtils.getNormalizedVector(CompoundVectorBuilder.getQueryVectorFromString(vecReader, luceneUtils, term1_unique));
            float[] vec2 = VectorUtils.getNormalizedVector(CompoundVectorBuilder.getQueryVectorFromString(vecReader, luceneUtils, term2_unique));
            similarity = VectorUtils.scalarProduct(vec1, vec2);
            similarity = (similarity + 1) / 2;
            if (similarity == 0.5) similarity = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return similarity;
    }
}

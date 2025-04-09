package org.solrmarc.marc;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.marc4j.*;
import org.marc4j.marc.Record;
import org.solrmarc.solr.SolrServerProxy;
import org.solrmarc.tools.Utils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * Reindex marc records stored in an index
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class SolrReIndexer extends MarcImporter {

    private String queryForRecordsToUpdate;

    protected String solrFieldContainingEncodedMarcRecord;

    protected boolean doUpdate = true;

    protected MarcWriter output = null;

    protected SolrServer solrServer = null;

    private boolean getIdsOnly = false;

    static Logger logger = Logger.getLogger(SolrReIndexer.class.getName());

    /**
     * Constructor
     * @param properties path to properties files
     * @param args additional arguments
     * @throws IOException
     */
    public SolrReIndexer() {
    }

    @Override
    public int handleAll() {
        verbose = false;
        output = new MarcStreamWriter(System.out, "UTF8", true);
        if (solrFieldContainingEncodedMarcRecord == null) solrFieldContainingEncodedMarcRecord = "marc_display";
        if (getIdsOnly) {
            readAllMatchingIds(queryForRecordsToUpdate);
        } else {
            readAllMatchingDocs(queryForRecordsToUpdate);
        }
        output.close();
        return 0;
    }

    @Override
    protected void loadLocalProperties() {
        super.loadLocalProperties();
        String up = Utils.getProperty(configProps, "solr.do_update");
        doUpdate = (up == null) ? true : Boolean.parseBoolean(up);
    }

    @Override
    protected void processAdditionalArgs() {
        int argOffset = 0;
        solrFieldContainingEncodedMarcRecord = Utils.getProperty(configProps, "solr.fieldname");
        queryForRecordsToUpdate = Utils.getProperty(configProps, "solr.query");
        if (addnlArgs.length > 0 && addnlArgs[0].equals("-id")) {
            getIdsOnly = true;
            argOffset = 1;
        }
        if (queryForRecordsToUpdate == null && addnlArgs.length > argOffset) {
            queryForRecordsToUpdate = addnlArgs[argOffset];
        }
        if (solrFieldContainingEncodedMarcRecord == null && addnlArgs.length > argOffset + 1) {
            solrFieldContainingEncodedMarcRecord = addnlArgs[argOffset + 1];
        }
    }

    @Override
    public void loadIndexer(String indexerName, String indexerProps) {
        super.loadIndexer(indexerName, indexerProps);
        solrServer = ((SolrServerProxy) solrProxy).getSolrServer();
    }

    /**
     * Read matching records from the index
     * @param queryForRecordsToUpdate
     */
    public void readAllMatchingIds(String queryForRecordsToUpdate) {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryForRecordsToUpdate);
        query.setQueryType("standard");
        query.setFacet(false);
        query.setRows(1000);
        query.setFields("id");
        int totalHits = -1;
        int totalProcessed = 0;
        try {
            do {
                query.setStart(totalProcessed);
                QueryResponse response = solrServer.query(query);
                SolrDocumentList sdl = response.getResults();
                if (totalHits == -1) totalHits = (int) sdl.getNumFound();
                for (SolrDocument doc : sdl) {
                    String id = doc.getFieldValue("id").toString();
                    totalProcessed++;
                    if (output != null && id != null) {
                        System.out.println(id);
                        System.out.flush();
                    }
                }
            } while (totalProcessed < totalHits);
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read matching records from the index
     * @param queryForRecordsToUpdate
     */
    public void readAllMatchingDocs(String queryForRecordsToUpdate) {
        String queryparts[] = queryForRecordsToUpdate.split(":");
        if (queryparts.length != 2) {
            logger.error("Error query must be of the form    field:term");
            System.out.println("Error: query must be of the form    field:term  " + queryForRecordsToUpdate);
            return;
        }
        SolrQuery query = new SolrQuery();
        query.setQuery(queryForRecordsToUpdate);
        query.setQueryType("standard");
        query.setFacet(false);
        query.setRows(1000);
        int totalHits = -1;
        int totalProcessed = 0;
        try {
            do {
                query.setStart(totalProcessed);
                QueryResponse response = solrServer.query(query);
                SolrDocumentList sdl = response.getResults();
                if (totalHits == -1) totalHits = (int) sdl.getNumFound();
                for (SolrDocument doc : sdl) {
                    totalProcessed++;
                    Record record = getRecordFromDocument(doc);
                    if (output != null && record != null) {
                        output.write(record);
                        System.out.flush();
                    }
                }
            } while (totalProcessed < totalHits);
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read and index a Solr document
     * @param field Solr field
     * @param term Term string to index
     * @param update flag to update the record 
     * @return Map of the fields
     */
    public Map<String, Object> readAndIndexDoc(String field, String term, boolean update) {
        SolrQuery query = new SolrQuery();
        query.setQuery(field + ":" + term);
        query.setQueryType("standard");
        query.setFacet(false);
        try {
            QueryResponse response = solrServer.query(query);
            SolrDocumentList sdl = response.getResults();
            for (SolrDocument doc : sdl) {
                Record record = getRecordFromDocument(doc);
                if (record != null) {
                    Map<String, Object> docMap = indexer.map(record, errors);
                    addExtraInfoFromDocToMap(doc, docMap);
                    if (update && docMap != null && docMap.size() != 0) {
                        update(docMap);
                    } else {
                        return (docMap);
                    }
                }
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return (null);
    }

    /**
     * Add information from a document to a map.
     * @param doc
     * @param map
     */
    protected void addExtraInfoFromDocToMap(SolrDocument doc, Map<String, Object> docMap) {
        addExtraInfoFromDocToMap(doc, docMap, "fund_code_facet");
        addExtraInfoFromDocToMap(doc, docMap, "date_received_facet");
        addExtraInfoFromDocToMap(doc, docMap, "marc_error");
    }

    /**
     * Add extra information from a Solr Document to a map
     * @param doc Solr Document to pull information from
     * @param map Map to add information to
     * @param keyVal Value to add
     */
    protected void addExtraInfoFromDocToMap(SolrDocument doc, Map<String, Object> map, String keyVal) {
        Collection<Object> fieldVals = null;
        fieldVals = doc.getFieldValues(keyVal);
        if (fieldVals != null && fieldVals.size() > 0) {
            for (Object fieldValObj : fieldVals) {
                String fieldVal = fieldValObj.toString();
                addToMap(map, keyVal, fieldVal);
            }
        }
    }

    /**
     * Retrieve the marc information from the Solr document
     * @param doc SolrDocument from the index
     * @return marc4j Record
     * @throws IOException
     */
    public Record getRecordFromDocument(SolrDocument doc) {
        String field = null;
        field = doc.getFirstValue(solrFieldContainingEncodedMarcRecord).toString();
        if (field == null || field.length() == 0) {
            logger.warn("field: " + solrFieldContainingEncodedMarcRecord + " not found in solr document");
            return (null);
        }
        String marcRecordStr = field;
        if (marcRecordStr.startsWith("<?xml version")) {
            return (getRecordFromXMLString(marcRecordStr));
        } else if (marcRecordStr.startsWith("{")) {
            return (getRecordFromJSONString(marcRecordStr));
        } else {
            return (getRecordFromRawMarc(marcRecordStr));
        }
    }

    /**
     * Extract the marc record from JSON String
     * @param marcRecordStr
     * @return
     */
    private Record getRecordFromJSONString(String marcRecordStr) {
        MarcJsonReader reader;
        int tries = 0;
        boolean tryAgain = false;
        do {
            try {
                tries++;
                tryAgain = false;
                reader = new MarcJsonReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext()) {
                    Record record = reader.next();
                    if (verbose) {
                        System.out.println(record.toString());
                    }
                    return (record);
                }
            } catch (MarcException me) {
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } while (tryAgain);
        return (null);
    }

    /**
     * Extract the marc record from binary marc
     * @param marcRecordStr
     * @return
     */
    private Record getRecordFromRawMarc(String marcRecordStr) {
        MarcStreamReader reader;
        int tries = 0;
        boolean tryAgain = false;
        do {
            try {
                tries++;
                tryAgain = false;
                reader = new MarcStreamReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext()) {
                    Record record = reader.next();
                    if (verbose) {
                        System.out.println(record.toString());
                    }
                    return (record);
                }
            } catch (MarcException me) {
                if (tries == 1) {
                    tryAgain = true;
                    marcRecordStr = normalizeUnicode(marcRecordStr);
                } else {
                    me.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } while (tryAgain);
        return (null);
    }

    private String normalizeUnicode(String string) {
        Pattern pattern = Pattern.compile("(\\\\u([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]))|(#(29|30|31);)");
        Matcher matcher = pattern.matcher(string);
        StringBuffer result = new StringBuffer();
        int prevEnd = 0;
        while (matcher.find()) {
            result.append(string.substring(prevEnd, matcher.start()));
            result.append(getChar(matcher.group()));
            prevEnd = matcher.end();
        }
        result.append(string.substring(prevEnd));
        string = result.toString();
        return (string);
    }

    private String getChar(String charCodePoint) {
        int charNum;
        if (charCodePoint.startsWith("\\u")) {
            charNum = Integer.parseInt(charCodePoint.substring(1), 16);
        } else {
            charNum = Integer.parseInt(charCodePoint.substring(1, 3));
        }
        String result = "" + ((char) charNum);
        return (result);
    }

    static BufferedWriter errOut = null;

    /**
     * Extract marc record from MarcXML
     * @param marcRecordStr MarcXML string
     * @return marc4j Record
     */
    public Record getRecordFromXMLString(String marcRecordStr) {
        MarcXmlReader reader;
        boolean tryAgain = false;
        do {
            try {
                tryAgain = false;
                reader = new MarcXmlReader(new ByteArrayInputStream(marcRecordStr.getBytes("UTF8")));
                if (reader.hasNext()) {
                    Record record = reader.next();
                    if (verbose) {
                        System.out.println(record.toString());
                        System.out.flush();
                    }
                    return (record);
                }
            } catch (MarcException me) {
                if (doUpdate == false && errOut == null) {
                    try {
                        errOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("badRecs.xml"))));
                        errOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\">");
                    } catch (FileNotFoundException e) {
                        logger.error(e.getMessage());
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
                if (doUpdate == false && errOut != null) {
                    String trimmed = marcRecordStr.substring(marcRecordStr.indexOf("<record>"));
                    trimmed = trimmed.replaceFirst("</collection>", "");
                    trimmed = trimmed.replaceAll("><", ">\n<");
                    try {
                        errOut.write(trimmed);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
                if (marcRecordStr.contains("<subfield code=\"&#31;\">")) {
                    marcRecordStr = marcRecordStr.replaceAll("<subfield code=\"&#31;\">(.)", "<subfield code=\"$1\">");
                    tryAgain = true;
                } else if (extractLeader(marcRecordStr).contains("&#")) {
                    String leader = extractLeader(marcRecordStr).replaceAll("&#[0-9]+;", "0");
                    marcRecordStr = marcRecordStr.replaceAll("<leader>[^<]*</leader>", leader);
                    tryAgain = true;
                } else {
                    me.printStackTrace();
                    if (verbose) {
                        logger.info("The bad record is: " + marcRecordStr);
                        logger.error("The bad record is: " + marcRecordStr);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage());
            }
        } while (tryAgain);
        return (null);
    }

    /**
     * Extract the leader from the marc record string
     * @param marcRecordStr marc record as a String
     * @return Leader leader string for the marc record
     */
    private String extractLeader(String marcRecordStr) {
        final String leadertag1 = "<leader>";
        final String leadertag2 = "</leader>";
        String leader = null;
        try {
            leader = marcRecordStr.substring(marcRecordStr.indexOf(leadertag1), marcRecordStr.indexOf(leadertag2) + leadertag2.length());
        } catch (IndexOutOfBoundsException e) {
        }
        return leader;
    }

    /**
     * Add a key value pair to a map
     */
    protected void addToMap(Map<String, Object> map, String key, String value) {
        if (map.containsKey(key)) {
            Object prevValue = map.get(key);
            if (prevValue instanceof String) {
                if (!prevValue.equals(value)) {
                    Set<String> result = new LinkedHashSet<String>();
                    result.add((String) prevValue);
                    result.add((String) value);
                    map.put(key, result);
                }
            } else if (prevValue instanceof Collection) {
                Iterator<String> valIter = ((Collection) prevValue).iterator();
                boolean addit = true;
                while (valIter.hasNext()) {
                    String collVal = valIter.next();
                    if (collVal.equals(value)) addit = false;
                }
                if (addit) {
                    ((Collection) prevValue).add(value);
                    map.put(key, prevValue);
                }
            }
        } else {
            map.put(key, value);
        }
    }

    /**
     * Update a document in the Solr index
     * @param map Values of the "new" marc record
     */
    public void update(Map<String, Object> map) {
        try {
            String docStr = solrProxy.addDoc(map, verbose, true);
            if (verbose) {
                logger.info(docStr);
            }
        } catch (IOException ioe) {
            logger.error("Couldn't add document: " + ioe.getMessage());
        }
    }

    public static void main(String[] args) {
        String newArgs[] = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = "NONE";
        SolrReIndexer reader = null;
        reader = new SolrReIndexer();
        reader.init(newArgs);
        reader.handleAll();
        reader.finish();
        System.exit(0);
    }
}

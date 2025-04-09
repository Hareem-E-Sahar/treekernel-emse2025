package com.zagile.zslayer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelSource;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.zagile.log4j.ZLog4j;

/**
 * 
 * @author ediaz
 * 
 */
public class DatabaseManager extends AbstractZSLayer implements RDFErrorHandler {

    private static final String ontologyFile = "_";

    private static final int BUFFER = 2048;

    private static final String outputFile = "." + File.separator + "ontos.zip";

    public static final String MAIN_INFERRED_MODEL = "MAIN_INFER_MODEL";

    private Store store = null;

    private final Logger logger = ZLog4j.getLogger(LOG_FILE_NAME, DatabaseManager.class);

    private SDBConnection connection;

    protected DatabaseManager(boolean create) {
        try {
            Properties settings = new Properties();
            settings.load(new FileInputStream(getSettingsFilePath()));
            OntDocumentManager.getInstance().setProcessImports(false);
            JDBC.loadDriver(settings.getProperty("com.zagile.zslayer.db.driver"));
            createConnection(settings.getProperty("com.zagile.zslayer.db.jdbc_url"), settings.getProperty("com.zagile.zslayer.db.user"), settings.getProperty("com.zagile.zslayer.db.password"), settings.getProperty("com.zagile.zslayer.db.layout"), settings.getProperty("com.zagile.zslayer.db.db_type"));
            if (create) {
                store.getTableFormatter().truncate();
                logger.info("Database cleaned");
            }
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private void createConnection(String jdbc_url, String user, String password, String layout, String db_type) {
        StoreDesc storeDescription = new StoreDesc(layout, db_type);
        connection = new SDBConnection(jdbc_url, user, password);
        store = SDBFactory.connectStore(connection, storeDescription);
    }

    private void shutdown() {
        if (store.getDatabaseType().equals(DatabaseType.HSQLDB)) {
            try {
                Statement statement = connection.getSqlConnection().createStatement();
                statement.execute("SHUTDOWN COMPACT");
                logger.info("Shutdown HSQLDB");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void readOntologies(Set<String> filesNames) {
        checkDatabase();
        String filename = null;
        ModelMaker subModelMaker = ModelFactory.createMemModelMaker();
        File ontologyFile = null;
        File[] files = null;
        Iterator<String> fileNamesIterator = filesNames.iterator();
        while (fileNamesIterator.hasNext()) {
            filename = fileNamesIterator.next();
            ontologyFile = new File(filename);
            if (ontologyFile.isFile()) {
                addFile(ontologyFile, subModelMaker);
            } else if (ontologyFile.isDirectory()) {
                logger.warn("Filename is a directory, looking for file childs ...");
                files = ontologyFile.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        addFile(file, subModelMaker);
                    }
                }
            }
        }
        shutdown();
    }

    private void checkDatabase() {
        if (store.getConnection().getTableNames().isEmpty()) {
            store.getTableFormatter().create();
        }
    }

    private void addFile(File ontologyFile, ModelSource subModelMaker) {
        Model subModel = null;
        String fileName = ontologyFile.getAbsolutePath();
        try {
            subModel = subModelMaker.createFreshModel().read(new BufferedReader(new FileReader(ontologyFile)), null);
            logger.info("Adding: \"" + fileName);
        } catch (FileNotFoundException e) {
            logger.error("Ignoring \"" + fileName + "\"", e);
            return;
        } catch (Exception e) {
            logger.error("Ignoring \"" + fileName + "\"", e);
            return;
        }
        String ontologyModelName = (String) subModel.getNsPrefixMap().get("");
        if (ontologyModelName == null) {
            logger.error("No default namespace for \"" + fileName + "\". Ignoring it");
            return;
        }
        ontologyModelName = ontologyModelName.substring(0, ontologyModelName.length() - 1);
        logger.info("Main Ontology: " + ontologyModelName);
        Model mainModel = SDBFactory.connectNamedModel(store, ontologyModelName);
        if (mainModel.size() > 0) {
            logger.warn("Model \"" + ontologyModelName + "\" exists, updating it");
            mainModel.removeAll();
            mainModel = SDBFactory.connectNamedModel(store, ontologyModelName);
        } else {
            logger.info("Model \"" + ontologyModelName + "\" doesn't exist, creating it");
        }
        if (mainModel.supportsTransactions()) {
            mainModel.begin();
        }
        mainModel.add(subModel);
        if (mainModel.supportsTransactions()) {
            mainModel.commit();
        }
        mainModel.close();
        logger.info("DONE\n");
    }

    public void removeModels(Set<String> modelsName) {
        Dataset dataset = SDBFactory.connectDataset(store);
        Iterator<?> modelNameIterator = dataset.listNames();
        if (!modelNameIterator.hasNext()) {
            logger.info("No models to remove");
            return;
        }
        if (modelsName.size() == 0) {
            logger.warn("This will cleaninig up the entire database. Are you sure you want to continue? [y/N]");
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String confirmation = inputReader.readLine();
                if (confirmation != null && confirmation.toLowerCase().equals("y")) {
                    store.getTableFormatter().truncate();
                    logger.info("Done");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.error("Aborted");
            return;
        }
        String modelName = null;
        String fileName = null;
        int index;
        while (modelNameIterator.hasNext()) {
            modelName = (String) modelNameIterator.next();
            index = modelName.lastIndexOf("/");
            fileName = modelName;
            if (index != -1) {
                fileName = modelName.substring(index + 1, modelName.length());
            }
            if (modelsName.contains(fileName)) {
                SDBFactory.connectNamedModel(store, modelName).removeAll();
                logger.info("Model \"" + modelName + "\" removed");
            }
        }
        shutdown();
    }

    public void getOWLFile(Set<String> modelsNames, boolean simpleDump) throws IOException {
        Dataset dataset = SDBFactory.connectDataset(store);
        Iterator<?> modelNameIterator = dataset.listNames();
        if (!modelNameIterator.hasNext()) {
            logger.info("No models to dump");
            return;
        }
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputFile));
        zipOut.setLevel(9);
        String modelName = null;
        String fileName = null;
        OntModel ontModel = null;
        int index;
        RDFWriter writer = null;
        boolean entryAdded;
        boolean extractSpecificModels = modelsNames.size() > 0;
        while (modelNameIterator.hasNext()) {
            modelName = (String) modelNameIterator.next();
            if (modelName.equals(MAIN_INFERRED_MODEL)) {
                continue;
            }
            index = modelName.lastIndexOf("/");
            fileName = modelName;
            if (index != -1) {
                fileName = modelName.substring(index + 1, modelName.length());
            }
            if (extractSpecificModels && !modelsNames.contains(fileName)) {
                continue;
            }
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, SDBFactory.connectNamedModel(store, modelName));
            logger.info("Saving model \"" + modelName + "\"");
            entryAdded = false;
            try {
                writer = ontModel.getWriter(simpleDump ? "RDF/XML" : "RDF/XML-ABBREV");
                ontModel.setNsPrefix("", modelName + "#");
                logger.info("Base Namespace: " + modelName);
                writer.setProperty("xmlbase", modelName);
                writer.setProperty("showXmlDeclaration", "true");
                writer.setProperty("tab", "2");
                zipOut.putNextEntry(new ZipEntry(fileName));
                entryAdded = true;
                writer.write(ontModel, zipOut, null);
                ontModel.close();
                writer.setErrorHandler(this);
                zipOut.closeEntry();
                entryAdded = false;
            } catch (IOException e) {
                if (entryAdded) {
                    zipOut.close();
                }
                e.printStackTrace();
            }
            logger.info("DONE");
        }
        zipOut.close();
        shutdown();
    }

    public void readDefaultOntologies() throws IOException {
        checkDatabase();
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ontologyFile)));
        ZipEntry entry = null;
        String filename = null;
        ModelMaker subModelMaker = ModelFactory.createMemModelMaker();
        BufferedOutputStream outputFile = null;
        String[] fileNameParts = null;
        int count = 0;
        byte data[] = null;
        File tmpFile = null;
        if (zipInputStream != null) {
            while ((entry = zipInputStream.getNextEntry()) != null) {
                filename = entry.getName();
                if (filename.endsWith(".owl") && !filename.startsWith("test")) {
                    logger.info("Adding: \"" + filename);
                    try {
                        fileNameParts = filename.split("\\.");
                        tmpFile = File.createTempFile(fileNameParts[0], fileNameParts[1]);
                        outputFile = new BufferedOutputStream(new FileOutputStream(tmpFile), BUFFER);
                        data = new byte[BUFFER];
                        while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
                            outputFile.write(data, 0, count);
                        }
                        outputFile.flush();
                        outputFile.close();
                        addFile(tmpFile, subModelMaker);
                        tmpFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            zipInputStream.close();
        }
        shutdown();
    }

    public void error(Exception e) {
        logger.error("ERROR", e);
    }

    public void fatalError(Exception e) {
        logger.error("FATAL ERROR", e);
    }

    public void warning(Exception e) {
        logger.warn("WARNING", e);
    }

    public static void main(String[] arguments) throws IOException {
        DatabaseManager databaseManager = null;
        if (arguments.length == 0) {
            System.out.println("Usage:");
            System.out.println("SCRIPT create OWL_FILE_NAME_1 DIRECTORY_NAME_2 ...");
            System.out.println("\tCreates or replaces the model(s) corresponding to each owl file\n");
            System.out.println("java -cp JDBC_DRIVER_PATH:zUtils.jar com.zagile.zslayer.main.Main dump all");
            System.out.println("\tExtract all the models on the database and put them on file ontos.zip\n");
            System.out.println("SCRIPT dump MODEL_ALIAS_1 MODEL_ALIAS_2 ...");
            System.out.println("\tExtract the specified model(s) on the database and put them on file ontos.zip\n");
            System.out.println("SCRIPT remove all");
            System.out.println("\tRemove all the models on the database asking for confirmation before doing anything\n");
            System.out.println("SCRIPT remove MODEL_ALIAS_1 MODEL_ALIAS_2 ...");
            System.out.println("\tRemove the specified model(s) on the database\n");
            System.out.println("\nWhere SCRIPT is: dbManager.sh OR dbManager.bat");
        } else {
            String command = arguments[0].toLowerCase();
            Set<String> names = new HashSet<String>();
            databaseManager = new DatabaseManager(false);
            if (command.equals("dump")) {
                if (parseParameters(arguments, names, "dump")) {
                    databaseManager.getOWLFile(names, false);
                }
            } else if (command.equals("simpledump")) {
                if (parseParameters(arguments, names, "simpledump")) {
                    databaseManager.getOWLFile(names, true);
                }
            } else if (command.equals("create")) {
                int index = arguments.length;
                if (index == 2 && arguments[1].toLowerCase().equals("default")) {
                    databaseManager.readDefaultOntologies();
                    return;
                }
                while (--index >= 1) {
                    names.add(arguments[index]);
                }
                if (names.size() == 0) {
                    System.err.println("No file names specified");
                } else {
                    databaseManager.readOntologies(names);
                }
            } else if (command.equals("remove")) {
                if (parseParameters(arguments, names, "remove")) {
                    databaseManager.removeModels(names);
                }
            } else {
                System.err.println("Unknow command: " + command);
            }
        }
    }

    private static boolean parseParameters(String[] arguments, Set<String> names, String command) {
        if (arguments.length > 1) {
            if (!arguments[1].toLowerCase().equals("all")) {
                int index = arguments.length;
                String modelName = null;
                while (--index >= 1) {
                    modelName = arguments[index];
                    if (!modelName.endsWith(".owl")) {
                        System.err.println("Invalid model name syntax \"" + modelName + "\". Ignoring parameter");
                        continue;
                    }
                    names.add(modelName);
                }
            }
            return true;
        } else {
            System.err.println("No " + command + " parameter specified.");
        }
        return false;
    }
}

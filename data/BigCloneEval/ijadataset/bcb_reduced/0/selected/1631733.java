package com.zagile.zslayer.db;

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
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.impl.Driver_HSQL;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelSource;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author ediaz
 */
public class DBHandler implements RDFErrorHandler {

    private static final String ontologyFile = "_";

    private static final int BUFFER = 2048;

    private static final String outputFile = "." + File.separator + "ontos.zip";

    private ModelMaker modelMaker = null;

    private IDBConnection connection = null;

    public DBHandler(boolean create) {
        try {
            Properties dbProps = new Properties();
            dbProps.load(new FileInputStream("ZSLayerConfig.properties"));
            try {
                Class.forName(dbProps.getProperty("com.zagile.zslayer.db.driver"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            connection = new DBConnection(dbProps.getProperty("com.zagile.zslayer.db.jdbc_url"), dbProps.getProperty("com.zagile.zslayer.db.user"), dbProps.getProperty("com.zagile.zslayer.db.password"), dbProps.getProperty("com.zagile.zslayer.db.db_type"));
            if (connection == null) {
                return;
            }
            if (create) {
                connection.cleanDB();
                System.out.println("Database cleaned");
            }
            OntDocumentManager.getInstance().setProcessImports(false);
            modelMaker = ModelFactory.createModelRDBMaker(connection);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void readOntologies(Set<String> filesNames) {
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
                System.out.println("Filename is a directory, looking for file childs ...");
                files = ontologyFile.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        addFile(file, subModelMaker);
                    }
                }
            }
        }
        checkDatabase();
    }

    private void checkDatabase() {
        if (connection.getDriver().getClass().equals(Driver_HSQL.class)) {
            ((Driver_HSQL) connection.getDriver()).shutdown();
        }
    }

    private void addFile(File ontologyFile, ModelSource subModelMaker) {
        Model subModel = null;
        String defaultName = null;
        Model model = null;
        String fileName = ontologyFile.getAbsolutePath();
        try {
            subModel = subModelMaker.createFreshModel().read(new BufferedReader(new FileReader(ontologyFile)), null);
            System.out.println("Adding: \"" + fileName);
        } catch (FileNotFoundException e) {
            System.err.println("Ignoring \"" + fileName + "\"");
            e.printStackTrace();
            return;
        } catch (Exception e) {
            System.err.println("Ignoring \"" + fileName + "\"");
            e.printStackTrace();
            return;
        }
        defaultName = (String) subModel.getNsPrefixMap().get("");
        if (defaultName == null) {
            System.err.println("No default namespace for \"" + fileName + "\". Ignoring it");
            return;
        }
        defaultName = defaultName.substring(0, defaultName.length() - 1);
        System.out.println("Main Ontology: " + defaultName);
        if (modelMaker.hasModel(defaultName)) {
            System.out.println("Model \"" + defaultName + "\" exists, updating it");
            modelMaker.removeModel(defaultName);
        } else {
            System.out.println("Model \"" + defaultName + "\" doesn't exist, creating it");
        }
        model = modelMaker.createModel(defaultName);
        model.begin();
        model.add(subModel);
        model.commit();
        model.close();
        System.out.println("DONE\n");
    }

    public void removeModels(Set<String> modelsName) {
        ExtendedIterator modelNameIterator = connection.getAllModelNames();
        if (!modelNameIterator.hasNext()) {
            System.out.println("No models to remove");
            return;
        }
        if (modelsName.size() == 0) {
            System.out.println("This will cleaninig up the entire database. Are you sure you want to continue? [y/N]");
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String confirmation = inputReader.readLine();
                if (confirmation != null && confirmation.toLowerCase().equals("y")) {
                    connection.cleanDB();
                    System.out.println("Done");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Aborted");
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
                modelMaker.removeModel(modelName);
                System.out.println("Model \"" + modelName + "\" removed");
            }
        }
        modelNameIterator.close();
        checkDatabase();
    }

    public void getOWLFile(Set<String> modelsNames) throws IOException {
        ExtendedIterator modelNameIterator = connection.getAllModelNames();
        if (!modelNameIterator.hasNext()) {
            System.out.println("No models to dump");
            return;
        }
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputFile));
        zipOut.setLevel(9);
        String modelName = null;
        String fileName = null;
        OntModel ontModel = null;
        int index;
        RDFWriter writer = null;
        boolean extractSpecificModels = modelsNames.size() > 0;
        while (modelNameIterator.hasNext()) {
            modelName = (String) modelNameIterator.next();
            index = modelName.lastIndexOf("/");
            fileName = modelName;
            if (index != -1) {
                fileName = modelName.substring(index + 1, modelName.length());
            }
            if (extractSpecificModels && !modelsNames.contains(fileName)) {
                continue;
            }
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, modelMaker.openModel(modelName));
            System.out.println("Saving model \"" + modelName + "\"");
            try {
                writer = ontModel.getWriter("RDF/XML");
                ontModel.setNsPrefix("", modelName + "#");
                System.out.println("Base Namespace: " + modelName);
                writer.setProperty("xmlbase", modelName);
                writer.setProperty("showXmlDeclaration", "true");
                writer.setProperty("tab", "2");
                zipOut.putNextEntry(new ZipEntry(fileName));
                writer.write(ontModel, zipOut, null);
                ontModel.close();
                writer.setErrorHandler(this);
                zipOut.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("DONE");
        }
        zipOut.close();
    }

    public void readDefaultOntologies() throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ontologyFile)));
        ZipEntry entry = null;
        String filename = null;
        ModelMaker subModelMaker = ModelFactory.createMemModelMaker();
        BufferedOutputStream outputFile = null;
        String[] fileNameParts = null;
        int count = 0;
        byte data[] = null;
        File tmpFile = null;
        try {
            if (zipInputStream != null) {
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    filename = entry.getName();
                    if (filename.endsWith(".owl") && !filename.startsWith("test")) {
                        System.out.println("Adding: \"" + filename);
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
        } finally {
            checkDatabase();
        }
    }

    public void error(Exception exception) {
        System.err.println("[ERROR]: " + exception.getLocalizedMessage());
        exception.printStackTrace();
    }

    public void fatalError(Exception exception) {
        System.err.println("[FATAL-ERROR]: " + exception.getLocalizedMessage());
        exception.printStackTrace();
    }

    public void warning(Exception exception) {
        System.err.println("[WARNING]: " + exception.getLocalizedMessage());
        exception.printStackTrace();
    }
}

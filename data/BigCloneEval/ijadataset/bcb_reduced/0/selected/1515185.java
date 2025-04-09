package fr.crnan.videso3d.radio;

import java.io.File;
import java.io.FileFilter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.formats.xml.PolygonDeserializer;
import fr.crnan.videso3d.formats.xml.SaxonFactory;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * Gestion formatage des données, mise à jour, etc...
 * @author mickael papail
 * @version 0.2
* */
public class RadioDataManager extends FileParser {

    /**
	 * Nombre de fichiers gérés
	 */
    private int numberFiles = 0;

    private int currentProgress = 0;

    private String name = "radio";

    private Connection conn;

    private String airspaceType;

    private String directoryPath;

    private File directory;

    private String outputXmlFilePath;

    private RadioDirectoryReader radioDirectoryReader = new RadioDirectoryReader(airspaceType);

    private File[] rootDirs;

    private ArrayList<Airspace> airspaces;

    public RadioDataManager() {
    }

    /**
	 * @param string
	 * @param airspaceType : (nom générique pour radioCovname="radioCov"))
	 */
    public RadioDataManager(String path) {
        super(path);
        this.directoryPath = path;
        directory = new File(directoryPath);
        radioDirectoryReader = new RadioDirectoryReader(directoryPath);
        outputXmlFilePath = directoryPath + "/radioOutput.xml";
        rootDirs = directory.listFiles(new FileFilter() {

            public boolean accept(File file) {
                return (file.isDirectory());
            }
        });
    }

    public static boolean containsRadioDatas(Collection<File> files) {
        Iterator<File> iterator = files.iterator();
        boolean found = false;
        while (iterator.hasNext() && !found) {
            String name = iterator.next().getName();
            found = name.equalsIgnoreCase("radioCoverageXSL.xsl");
        }
        return found;
    }

    public ArrayList<Airspace> getAirspaces() {
        return this.airspaces;
    }

    /**
	 **/
    public ArrayList<Airspace> loadData() {
        boolean xmlUpToDate = true;
        try {
            if (rootDirs.length != 0) {
                for (int i = 0; i < rootDirs.length; i++) {
                    if (!(radioDirectoryReader.xmlFileUpToDate(rootDirs[i]))) {
                        xmlUpToDate = false;
                    }
                }
                if (!xmlUpToDate) {
                    radioDirectoryReader.scanDirectoriesList(directory);
                    this.computeXSL();
                }
            }
            if (new File(outputXmlFilePath).exists()) {
                System.out.println("Debut de désérialisation");
                PolygonDeserializer polygonDeserializer = new PolygonDeserializer();
                this.airspaces = polygonDeserializer.Deserialize(outputXmlFilePath);
                for (Airspace airspace : airspaces) {
                    ((DatabaseVidesoObject) airspace).setDatabaseType(Type.RadioCov);
                }
                System.out.println("Fin de désérialisation");
                return this.airspaces;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 
	 * 1) Parsing de l'arborecence,
	 * 2) génération du xml dans tous les reps (Vérifier si radioCoverage.xml existe (cf pattern)), 
	 * 3) concaténation de toutes les données et écriture du fichier de sortie dans le root directory.
	**/
    public void computeXSL() {
        if (directory.isDirectory()) {
            File[] dirs = directory.listFiles(new FileFilter() {

                public boolean accept(File file) {
                    return ((file.isDirectory()));
                }
            });
            numberFiles = dirs.length;
            this.setProgress(0);
            String XSL = directoryPath + File.separator + "radioCoverageXSL.xsl";
            String XML_out = directory.getAbsolutePath() + File.separator + "radioOutput.xml";
            XmlFile.writeIntoFile(XML_out, "<list>" + "\n");
            for (int i = 0; i < dirs.length; i++) {
                String XML_in = dirs[i].getAbsolutePath() + File.separator + "radioCoverage.xml";
                String XML_temp_out = dirs[i].getAbsolutePath() + File.separator + "tempRadioOutput.xml";
                try {
                    SaxonFactory.SaxonJob(XSL, XML_in, XML_temp_out);
                    this.setProgress(currentProgress++);
                    XmlFile.copy(XML_temp_out, XML_out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            XmlFile.writeIntoFile(XML_out, "</list>" + "\n");
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean scanForUpdate() {
        boolean test = false;
        return test;
    }

    @Override
    public Integer doInBackground() {
        try {
            this.conn = DatabaseManager.selectDB(Type.RadioCov, this.name);
            this.conn.setAutoCommit(false);
            if (!DatabaseManager.databaseExists(Type.RadioCov, this.name)) {
                System.out.println("(Radio.java) / La base de données n'existe pas" + "");
                DatabaseManager.createRadioCov(this.name, this.path);
                DatabaseManager.insertRadioCov(this.name, this.path);
                try {
                    this.conn.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("La base de données existe");
                DatabaseManager.insertRadioCov(this.name, this.path);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void done() {
        if (this.isCancelled()) {
            try {
                DatabaseManager.deleteDatabase(this.name, Type.RadioCov);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            firePropertyChange("done", true, false);
        } else {
            firePropertyChange("done", false, true);
        }
    }

    @Override
    public int numberFiles() {
        return this.numberFiles;
    }

    @Override
    public void getFromFiles() {
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Type getType() {
        return Type.RadioCov;
    }
}

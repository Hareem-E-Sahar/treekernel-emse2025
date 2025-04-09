package de.tobiasmaasland.voctrain;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.graph.CycleStrategy;
import org.simpleframework.xml.load.Persister;
import de.tobiasmaasland.voctrain.business.data.RecursiveVocable;
import de.tobiasmaasland.voctrain.business.data.RecursiveVocabulary;
import de.tobiasmaasland.voctrain.business.data.Vocable;
import de.tobiasmaasland.voctrain.business.data.Vocabulary;

/**
 * Converts xml files from old scheme to new scheme.
 * 
 * @author Tobias Maasland
 *
 */
@SuppressWarnings("deprecation")
public final class ConvertVocabulary {

    private static Logger log = Logger.getLogger(ConvertVocabulary.class);

    private ConvertVocabulary() {
    }

    /**
	 * Absolutely temporary solution for creating vocabulary files.
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        if (args.length > 0) {
            ConvertVocabulary convertVocabulary = new ConvertVocabulary();
            convertVocabulary.convertIt(args[0]);
        } else {
            log.error("No filename given. Quitting...");
        }
    }

    private void convertIt(String readFilename) {
        Serializer serializer = new Persister(new CycleStrategy());
        Vocabulary vocabulary = null;
        try {
            log.debug("Trying to read " + readFilename);
            File file = new File(readFilename);
            RecursiveVocabulary recursiveVocabulary = serializer.read(RecursiveVocabulary.class, file);
            vocabulary = new Vocabulary(recursiveVocabulary.getLanguage1(), recursiveVocabulary.getLanguage2());
            Vector<Vocable> theVocs = new Vector<Vocable>();
            vocabulary.setVocables(theVocs);
            vocabulary.setShortDescription(recursiveVocabulary.getShortDescription());
            vocabulary.setDescription(recursiveVocabulary.getDescription());
            vocabulary.setCreationDate(recursiveVocabulary.getCreationDate());
            vocabulary.setChangeDate(recursiveVocabulary.getChangeDate());
            log.debug("Processing " + recursiveVocabulary.getVocables().size() + " items.");
            for (int i = 0; i < recursiveVocabulary.getVocables().size(); i = i + 2) {
                RecursiveVocable recursiveVocable = recursiveVocabulary.getVocables().get(i);
                Vocable vocable = new Vocable(recursiveVocable.getName(), recursiveVocable.getReverseVocable().getName());
                vocable.setFolderFront(recursiveVocable.getFolder());
                vocable.setFolderBack(recursiveVocable.getReverseVocable().getFolder());
                vocable.setLastChangeDateFront(recursiveVocable.getLastChangeDate());
                vocable.setLastChangeDateBack(recursiveVocable.getReverseVocable().getLastChangeDate());
                vocable.setCreationDate(recursiveVocable.getCreationDate());
                vocabulary.getVocables().add(vocable);
            }
        } catch (Exception e) {
            log.error("", e);
            return;
        }
        serializer = new Persister();
        try {
            File vocs = new File(readFilename + ".conv");
            log.debug("Starting to save...");
            if (1 == 0) {
                ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(vocs));
                zipOutput.setLevel(9);
                zipOutput.putNextEntry(new ZipEntry(readFilename));
                serializer.write(vocabulary, zipOutput);
                zipOutput.closeEntry();
                zipOutput.close();
            }
            serializer.write(vocabulary, vocs);
            log.info("Saved " + readFilename + ".conv successfully");
        } catch (Exception e1) {
            log.error("", e1);
        }
    }
}

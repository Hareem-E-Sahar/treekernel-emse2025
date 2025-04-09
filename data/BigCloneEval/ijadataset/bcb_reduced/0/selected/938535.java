package sk.tuke.ess.editor.base.document;

import sk.tuke.ess.editor.base.components.logger.Logger;
import sk.tuke.ess.editor.base.settings.SettingsManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Správca dokumentov.
 *
 * @author Ján Čabala
 */
public class DocumentManager {

    /**
     * Odkaz na aktívny dokument
     */
    Document activeDocument;

    /**
     * Zoznam dokumentov
     */
    List<Document> documentsList;

    public void loadDocuments() {
        SettingsManager.load(new DocumentManagerSettings(this));
    }

    /**
     * Pridať nový dokument
     *
     * @param doc dokument ktorý bude pridaný
     */
    public void addDocument(Document doc) {
        documentsList.add(doc);
        SettingsManager.update(new DocumentManagerSettings(this));
    }

    public void removeDocument(Document doc) {
        documentsList.remove(doc);
        if (activeDocument != null && activeDocument.equals(doc)) {
            activeDocument = null;
        }
        SettingsManager.update(new DocumentManagerSettings(this));
    }

    /**
     * Uložiť všetky otvorené dokumenty
     */
    public void saveAll() {
        for (Document document : documentsList) {
            save(document);
        }
    }

    public void save(Document document) {
        try {
            document.save();
        } catch (DocumentException e) {
            Logger.getLogger().addError("Chyba pri ukladaní dokumentu. %s", e.getMessage());
            Logger.getLogger().addException(e, "Nepodarilo sa uložiť dokument <b>%s</b>", document.getPath());
        }
    }

    /**
     * Nastaviť aktívny dokument
     *
     * @param doc dokument, ktorý sa stane aktívnym
     */
    public void setActiveDocument(Document doc) {
        this.activeDocument = doc;
        Logger.getLogger().addDebug("Active document = <b>%s</b>", doc.getName());
        SettingsManager.update(new DocumentManagerSettings(this));
    }

    /**
     * Vracia aktívny dokument
     *
     * @return aktívny dokument
     */
    public Document getActiveDocument() {
        return activeDocument;
    }

    public int getActiveDocumentIndex() {
        return activeDocument == null ? -1 : documentsList.indexOf(activeDocument);
    }

    public List<Document> getUnsavedDocuments() {
        List<Document> unsavedDocumentList = new ArrayList<Document>();
        for (Document document : documentsList) {
            if (document instanceof DocumentWithHistorySupport) {
                if (!((DocumentWithHistorySupport) document).isSaved()) unsavedDocumentList.add(document);
            }
        }
        return unsavedDocumentList;
    }

    public boolean hasUnsavedDocuments() {
        return getUnsavedDocuments().size() > 0;
    }

    public Document[] getAllDocuments() {
        return documentsList.toArray(new Document[documentsList.size()]);
    }

    public boolean isDocumentOpened(String path) {
        return getDocument(path) != null;
    }

    public Document getDocument(String path) {
        for (Document document : documentsList) {
            if (document.getPath().equals(path)) return document;
        }
        return null;
    }

    public <T extends Document> T addDocument(Class<T> documentClass, String path) throws DocumentException {
        try {
            T document = (T) documentClass.getConstructor().newInstance();
            document.open(path);
            addDocument(document);
            return document;
        } catch (Exception e) {
            throw new DocumentException(String.format("Nepodarilo sa nájsť požadovaný konštruktor pre dokument typu %s, cesta: %s, error: %s", documentClass.getName(), path, e));
        }
    }
}

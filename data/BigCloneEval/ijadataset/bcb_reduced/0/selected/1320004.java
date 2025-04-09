package fr.insa.rennes.pelias.platform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Repr�sente un d�pot d'objet sur le syst�me de fichier.
 * @author Guillaume Ray
 */
public class FileSystemRepository<T extends PObject> implements IRepository<T> {

    /**
	 * Repr�sente un objet d�pos�. 
	 * @author Guillaume Ray
	 */
    @XmlRootElement
    public static class RepositoryElement<T> {

        private T object;

        /**
		 * Initialise une nouvelle instance par d�faut de RepositoryElement.
		 */
        public RepositoryElement() {
            this(null);
        }

        /**
		 * Initialise une nouvelle instance par d�faut de RepositoryElement sur l'objet sp�cifi�.
		 * @param object Objet � d�poser.
		 */
        public RepositoryElement(T object) {
            this.object = object;
        }

        /**
		 * D�finit l'objet � d�poser.
		 * @param object Objet � d�poser.
		 */
        public void setObject(T object) {
            this.object = object;
        }

        /**
		 * Obtient l'objet � d�poser.
		 * @return Objet � d�poser.
		 */
        public T getObject() {
            return object;
        }
    }

    /**
	 * Repr�sente un filtre sur une extension de fichier.
	 * @author Guillaume Ray
	 */
    public class FileExtensionFilter implements FilenameFilter {

        private String extension;

        /**
		 * Initialise une nouvelle instance de FileExtensionFilter � partir de l'extension sp�cifi�e.
		 * @param extension Extension du filtre.
		 */
        public FileExtensionFilter(String extension) {
            this.extension = extension;
        }

        /**
		 * Obtien l'extension du filtre.
		 * @return Extension du filtre.
		 */
        public String getExtension() {
            return extension;
        }

        /**
		 * D�finit l'extension du filtre.
		 * @param extension Extension du filtre.
		 */
        public void setExtension(String extension) {
            this.extension = extension;
        }

        /**
		 * Indique si le fichier sp�ficifi� respecte le filtre.
		 */
        public boolean accept(File dir, String name) {
            if (extension == null) return true;
            return name.endsWith("." + extension);
        }
    }

    private String location;

    private FileExtensionFilter metaFilter;

    private FileExtensionFilter labelFilter;

    private LinkedList<IRepository<?>> associates;

    private Class<?> objectClass;

    private JAXBContext context;

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private static JAXBContext referencesContext;

    private static Marshaller referencesMarshaller;

    private static Unmarshaller referencesUnmarshaller;

    private final void ensureContext() {
        if (context != null) return;
        try {
            context = JAXBContext.newInstance(RepositoryElement.class, objectClass);
            marshaller = context.createMarshaller();
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static final void ensureReferencesContext() {
        if (referencesContext != null) return;
        try {
            referencesContext = JAXBContext.newInstance(FileSystemRepository.RepositoryElement.class, PSxSObjectReference.class, Object[].class);
            referencesMarshaller = referencesContext.createMarshaller();
            referencesUnmarshaller = referencesContext.createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Initialise une nouvelle instance de FileSystemRepository dans le dossier sp�cifi�.
	 * @param location Dossier du d�p�t.
	 * @param objectClass Classe des objets r�f�renc�s.
	 * @throws NullPointerException objectClass doit �tre non null.
	 */
    public FileSystemRepository(String location, Class<?> objectClass) {
        if (objectClass == null) throw new NullPointerException("objectClass");
        setLocation(location);
        metaFilter = new FileExtensionFilter("meta");
        labelFilter = new FileExtensionFilter("label");
        associates = new LinkedList<IRepository<?>>();
        associates.add(this);
        this.objectClass = objectClass;
    }

    /**
	 * Obtient le filtre pour les attachements.
	 * @return Filtre pour les attachements.
	 */
    protected FileExtensionFilter getMetaFilter() {
        return metaFilter;
    }

    /**
	 * Obtient le filtre pour l'�tiquette.
	 * @return Filtre pour l'�tiquette.
	 */
    protected FileExtensionFilter getLabelFilter() {
        return labelFilter;
    }

    /**
	 * Obtient la classe des objets r�f�renc�s.
	 * @return Classe des objets r�f�renc�s.
	 */
    public Class<?> getObjectClass() {
        return objectClass;
    }

    /**
	 * Obtient les d�p�ts associ�s.
	 * @return D�p�ts associ�s.
	 */
    public LinkedList<IRepository<?>> getAssociates() {
        return associates;
    }

    /**
	 * Obtient l'emplacement du d�p�t.
	 * @return Emplacement du d�p�t.
	 */
    public String getLocation() {
        return location;
    }

    /**
	 * D�finit l'emplacement du d�p�t.
	 * @param location Emplacement du d�p�t.
	 */
    public void setLocation(String location) {
        if (location == null) location = "";
        this.location = location;
    }

    /**
	 * Obtient le fichier racine du d�p�t.
	 * @return Fichier racine du d�p�t.
	 */
    public File getLocationFile() {
        File result = new File(getLocation());
        if (!result.exists()) {
            result.mkdirs();
        }
        if (!result.isDirectory()) {
            throw new IllegalStateException("Le dossier du d�p�t n'existe pas et ne peut �tre cr��.");
        }
        return result;
    }

    /**
	 * Obtient le dossier de travail l'objet sp�cifi�.
	 * @param id Identifiant de l'objet.
	 * @return Dossier de travai de l'objet sp�cifi�.
	 */
    protected File getIdFolder(UUID id) {
        return new File(getLocationFile(), id.toString());
    }

    /**
	 * Obtient une r�f�rence sur l'objet sp�cifi�.
	 * @param object Objet sur lequel obtenir un r�f�rence.
	 * @return R�f�rence sur l'objet sp�cifi�.
	 */
    protected PObjectReference getReference(T object) {
        return object.getSelfReference();
    }

    protected boolean objectAttachmentExists(File idFolder, UUID attachment) throws PObjectNotFoundException {
        if ((idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        return new File(idFolder, attachment.toString() + ".meta").exists();
    }

    /** 
	 * @see fr.insa.rennes.pelias.platform.IRepository#objectAttachmentExists(java.util.UUID, java.util.UUID)
	 */
    public synchronized boolean objectAttachmentExists(UUID id, UUID attachment) throws PObjectNotFoundException {
        return objectAttachmentExists(getIdFolder(id), attachment);
    }

    protected void clearObjectAttachments(File idFolder) throws PObjectNotFoundException {
        if ((idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        for (File metaFile : idFolder.listFiles(getMetaFilter())) {
            metaFile.delete();
        }
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#clearObjectAttachments(java.util.UUID)
	 */
    public synchronized void clearObjectAttachments(UUID id) throws PObjectNotFoundException {
        clearObjectAttachments(getIdFolder(id));
    }

    protected List<UUID> enumerateObjectAttachments(File idFolder) throws PObjectNotFoundException {
        if ((idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        ArrayList<UUID> result = new ArrayList<UUID>();
        for (File file : idFolder.listFiles(getMetaFilter())) {
            result.add(UUID.fromString(file.getName().substring(0, 36)));
        }
        return result;
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#enumerateObjectAttachments(java.util.UUID)
	 */
    public synchronized List<UUID> enumerateObjectAttachments(UUID id) throws PObjectNotFoundException {
        return enumerateObjectAttachments(getIdFolder(id));
    }

    protected boolean putObjectAttachment(File idFolder, UUID attachment, String value, boolean replace) throws PObjectNotFoundException {
        if (value == null) {
            if (replace) return removeObjectAttachment(idFolder, attachment); else return objectAttachmentExists(idFolder, attachment);
        }
        if ((idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        File metaFile = new File(idFolder, attachment.toString() + ".meta");
        boolean result = metaFile.exists();
        if (result && !replace) return true;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(metaFile, false));
            writer.write(value);
            writer.close();
        } catch (Exception e) {
        }
        return result;
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#putObjectAttachment(java.util.UUID, java.util.UUID, java.lang.String, boolean)
	 */
    public synchronized boolean putObjectAttachment(UUID id, UUID attachment, String value, boolean replace) throws PObjectNotFoundException {
        return putObjectAttachment(getIdFolder(id), attachment, value, replace);
    }

    protected String getObjectAttachment(File idFolder, UUID attachment) throws PObjectNotFoundException {
        if ((idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        File metaFile = new File(idFolder, attachment.toString() + ".meta");
        if (!metaFile.exists()) return null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(metaFile));
            StringBuilder contents = new StringBuilder();
            String line = reader.readLine();
            if (line != null) {
                contents.append(line);
                while ((line = reader.readLine()) != null) {
                    contents.append(System.getProperty("line.separator"));
                    contents.append(line);
                }
                reader.close();
            }
            return contents.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#getObjectAttachment(java.util.UUID, java.util.UUID)
	 */
    public synchronized String getObjectAttachment(UUID id, UUID attachment) throws PObjectNotFoundException {
        return getObjectAttachment(getIdFolder(id), attachment);
    }

    protected boolean removeObjectAttachment(File idFolder, UUID attachment) throws PObjectNotFoundException {
        if ((idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        File metaFile = new File(idFolder, attachment.toString() + ".meta");
        boolean result = metaFile.exists();
        metaFile.delete();
        return result;
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#removeObjectAttachment(java.util.UUID, java.util.UUID)
	 */
    public synchronized boolean removeObjectAttachment(UUID id, UUID attachment) throws PObjectNotFoundException {
        return removeObjectAttachment(getIdFolder(id), attachment);
    }

    protected void unBindObject(File idFolder, PObjectReference consumer) {
        try {
            for (PObjectReference dependency : getObjectDeclaredDependencies(idFolder)) {
                for (IRepository<?> repository : getAssociates()) {
                    if ((repository instanceof ISxSRepository<?>) && (dependency instanceof PSxSObjectReference)) {
                        ISxSRepository<?> sxsRepository = (ISxSRepository<?>) repository;
                        PSxSObjectReference sxsDependency = (PSxSObjectReference) dependency;
                        sxsRepository.unregisterObjectConsumer(dependency.getId(), sxsDependency.getVersion(), false, consumer);
                    } else {
                        repository.unregisterObjectConsumer(dependency.getId(), consumer);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    protected void bindObject(File idFolder, PObjectReference consumer) {
        try {
            for (PObjectReference dependency : getObjectDeclaredDependencies(idFolder)) {
                for (IRepository<?> repository : getAssociates()) {
                    if ((repository instanceof ISxSRepository<?>) && (dependency instanceof PSxSObjectReference)) {
                        ISxSRepository<?> sxsRepository = (ISxSRepository<?>) repository;
                        PSxSObjectReference sxsDependency = (PSxSObjectReference) dependency;
                        sxsRepository.registerObjectConsumer(dependency.getId(), sxsDependency.getVersion(), false, consumer);
                    } else {
                        repository.registerObjectConsumer(dependency.getId(), consumer);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setObjectDeclaredDependencies(File idFolder, T object) throws FileNotFoundException, PObjectNotFoundException {
        if ((object == null) || (idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        File metaFile = new File(idFolder, "object.dep");
        if (metaFile.exists()) {
            metaFile.delete();
        }
        ensureReferencesContext();
        RepositoryElement<Object[]> rootObject = new RepositoryElement<Object[]>(object.getDeclaredDependencies(true).toArray());
        try {
            referencesMarshaller.marshal(rootObject, metaFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setObjectRegisteredConsumers(File idFolder, List<PObjectReference> consumers) throws FileNotFoundException, PObjectNotFoundException {
        if ((consumers == null) || (idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        File metaFile = new File(idFolder, "object.rco");
        if (metaFile.exists()) {
            metaFile.delete();
        }
        ensureReferencesContext();
        RepositoryElement<Object[]> rootObject = new RepositoryElement<Object[]>(consumers.toArray());
        try {
            referencesMarshaller.marshal(rootObject, metaFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    protected List<PObjectReference> getObjectRegisteredConsumers(File idFolder) throws PObjectNotFoundException, FileNotFoundException {
        if ((idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        File metaFile = new File(idFolder, "object.rco");
        if (metaFile.exists()) {
            ensureReferencesContext();
            try {
                RepositoryElement<Object[]> rootObject = (RepositoryElement<Object[]>) referencesUnmarshaller.unmarshal(metaFile);
                ArrayList<PObjectReference> result = new ArrayList<PObjectReference>();
                for (int i = 0; i < rootObject.object.length; i++) {
                    result.add((PObjectReference) rootObject.object[i]);
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<PObjectReference>();
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#getObjectRegisteredConsumers(java.util.UUID)
	 */
    public synchronized List<PObjectReference> getObjectRegisteredConsumers(UUID id) throws PObjectNotFoundException {
        try {
            return getObjectRegisteredConsumers(getIdFolder(id));
        } catch (PObjectNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected List<PObjectReference> getObjectDeclaredDependencies(File idFolder) throws PObjectNotFoundException, FileNotFoundException {
        if ((idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        File metaFile = new File(idFolder, "object.dep");
        if (metaFile.exists()) {
            ensureReferencesContext();
            try {
                RepositoryElement<Object[]> rootObject = (RepositoryElement<Object[]>) referencesUnmarshaller.unmarshal(metaFile);
                ArrayList<PObjectReference> result = new ArrayList<PObjectReference>();
                for (int i = 0; i < rootObject.object.length; i++) {
                    result.add((PObjectReference) rootObject.object[i]);
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<PObjectReference>();
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#getObjectDeclaredDependencies(java.util.UUID)
	 */
    public synchronized List<PObjectReference> getObjectDeclaredDependencies(UUID id) throws PObjectNotFoundException {
        try {
            return getObjectDeclaredDependencies(getIdFolder(id));
        } catch (PObjectNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    protected String getObjectLabel(File idFolder) throws PObjectNotFoundException {
        if ((idFolder == null) || (!idFolder.exists())) throw new PObjectNotFoundException();
        for (File file : idFolder.listFiles(getLabelFilter())) {
            try {
                return URLDecoder.decode(file.getName().substring(0, file.getName().lastIndexOf('.')), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return file.getName().substring(0, file.getName().lastIndexOf('.'));
            }
        }
        return "";
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#getObjectLabel(java.util.UUID)
	 */
    public synchronized String getObjectLabel(UUID id) throws PObjectNotFoundException {
        return getObjectLabel(getIdFolder(id));
    }

    protected boolean objectExists(File idFolder) {
        return idFolder.exists() && idFolder.isDirectory();
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#objectExists(java.util.UUID)
	 */
    public synchronized boolean objectExists(UUID id) {
        File idFolder = getIdFolder(id);
        return objectExists(idFolder) && (new File(idFolder, "object.xml")).exists();
    }

    protected boolean putObject(File idFolder, T object, boolean replace) throws FileNotFoundException {
        if (object == null) {
            if (replace) return removeObject(idFolder, getReference(object)); else return objectExists(idFolder);
        }
        if (!idFolder.exists()) idFolder.mkdirs();
        File objectFile = new File(idFolder, "object.xml");
        boolean result = objectFile.exists();
        if (result) {
            if (replace) {
                unBindObject(idFolder, getReference(object));
                objectFile.delete();
            } else {
                return true;
            }
        }
        ensureContext();
        RepositoryElement<T> rootObject = new RepositoryElement<T>(object);
        try {
            marshaller.marshal(rootObject, objectFile);
            setObjectDeclaredDependencies(idFolder, object);
            bindObject(idFolder, getReference(object));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#putObject(fr.insa.rennes.pelias.platform.PObject, boolean)
	 */
    public synchronized boolean putObject(T object, boolean replace) {
        File idFolder = getIdFolder(object.getId());
        boolean result = true;
        try {
            result = putObject(idFolder, object, replace);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!result || replace) {
            for (File file : idFolder.listFiles(getLabelFilter())) {
                file.delete();
            }
            if (object.getLabel() != "") {
                try {
                    File labelFile = new File(idFolder, URLEncoder.encode(object.getLabel(), "UTF-8") + ".label");
                    labelFile.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    protected boolean removeObject(File idFolder, PObjectReference reference) {
        if (idFolder == null) return false;
        File objectFile = new File(idFolder, "object.xml");
        if (!objectFile.exists()) return false;
        unBindObject(idFolder, reference);
        delete(idFolder);
        return true;
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#removeObject(java.util.UUID)
	 */
    public synchronized boolean removeObject(UUID id) {
        return removeObject(getIdFolder(id), new PObjectReference(getObjectClass(), id));
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#enumerateObjects()
	 */
    public synchronized List<PObjectReference> enumerateObjects() {
        LinkedList<PObjectReference> result = new LinkedList<PObjectReference>();
        for (File file : getLocationFile().listFiles()) {
            if (!file.isDirectory()) continue;
            try {
                PObjectReference reference = new PObjectReference(getObjectClass(), UUID.fromString(file.getName()));
                reference.setLabel(getObjectLabel(file));
                result.add(reference);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected T getObject(File idFolder) throws FileNotFoundException {
        if ((idFolder == null) || (!idFolder.exists())) return null;
        ensureContext();
        try {
            RepositoryElement<T> result = (RepositoryElement<T>) unmarshaller.unmarshal(new File(idFolder, "object.xml"));
            return result.object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#getObject(java.util.UUID)
	 */
    public synchronized T getObject(UUID id) {
        try {
            File idFolder = getIdFolder(id);
            T object = getObject(idFolder);
            if (object != null) object.setLabel(getObjectLabel(idFolder));
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#clearObjects()
	 */
    public synchronized void clearObjects() {
        for (File file : getLocationFile().listFiles()) {
            delete(file);
        }
    }

    protected void delete(File file) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                delete(childFile);
            }
        }
        file.delete();
    }

    protected boolean registerObjectConsumer(File idFolder, PObjectReference consumer) {
        try {
            List<PObjectReference> consumers = getObjectRegisteredConsumers(idFolder);
            if (!consumers.contains(consumer)) {
                consumers.add(consumer);
                setObjectRegisteredConsumers(idFolder, consumers);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#registerObjectConsumer(java.util.UUID,fr.insa.rennes.pelias.platform.PObjectReference)
	 */
    public synchronized boolean registerObjectConsumer(UUID id, PObjectReference consumer) {
        return registerObjectConsumer(getIdFolder(id), consumer);
    }

    protected boolean unregisterObjectConsumer(File idFolder, PObjectReference consumer) {
        try {
            List<PObjectReference> consumers = getObjectRegisteredConsumers(idFolder);
            if (consumers.remove(consumer)) {
                setObjectRegisteredConsumers(idFolder, consumers);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
	 * @see fr.insa.rennes.pelias.platform.IRepository#unregisterObjectConsumer(java.util.UUID,fr.insa.rennes.pelias.platform.PObjectReference)
	 */
    public synchronized boolean unregisterObjectConsumer(UUID id, PObjectReference consumer) {
        return unregisterObjectConsumer(getIdFolder(id), consumer);
    }
}

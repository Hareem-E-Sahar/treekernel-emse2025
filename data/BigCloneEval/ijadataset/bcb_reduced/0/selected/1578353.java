package main.configuration;

import javax.naming.SizeLimitExceededException;
import main.ReplacementPolicy;
import main.allocationpolicy.*;
import main.process.LogicReference;
import main.replacementpolicy.*;
import main.exception.configurationexception.CannotUseThisMethodException;
import main.memory.*;
import java.io.*;
import java.security.AccessController;
import java.util.*;
import java.lang.reflect.*;
import java.lang.Math;

/**
 * Classe di supporto per la ricerca dei file.
 * @author Luca Piergiovanni
 * @version 1.0
 */
class Filter implements FilenameFilter {

    /**
	 * Estensione del file da ricercare.
	 */
    private String extension_;

    /**
	 * Costruttore di Filter.
	 * @param s, estensione da ricercare.
	 */
    Filter(final String extension) {
        extension_ = extension;
    }

    /**
	 * Implementazione del metodo richiesto dall'interfaccia.
	 * @param dir , directory del file da ricercare.
	 * @param name , nome del file da ricercare.
	 * @return boolean , true se esiste.
	 *
	 */
    public boolean accept(final File dir, final String name) {
        if (name.endsWith(extension_)) {
            return true;
        }
        return false;
    }
}

class SimpleClassLoader extends ClassLoader {

    private String[] dirs;

    private MemoryController.MemoryType memoryType_;

    public SimpleClassLoader(String path) {
        dirs = path.split(System.getProperty("path.separator"));
    }

    public SimpleClassLoader(String path, ClassLoader parent) {
        super(parent);
        dirs = path.split(System.getProperty("path.separator"));
    }

    public void extendClasspath(String path) {
        String[] exDirs = path.split(System.getProperty("path.separator"));
        String[] newDirs = new String[dirs.length + exDirs.length];
        System.arraycopy(dirs, 0, newDirs, 0, dirs.length);
        System.arraycopy(exDirs, 0, newDirs, dirs.length, exDirs.length);
        dirs = newDirs;
    }

    public synchronized Class findClass(String name, MemoryController.MemoryType memoryType) throws ClassNotFoundException, NoClassDefFoundError {
        for (int i = 0; i < dirs.length; i++) {
            byte[] buf = getClassData(dirs[i], name);
            if (buf != null) {
                if (memoryType == MemoryController.MemoryType.PAGED) if (findLoadedClass("main.replacementpolicy." + name) != null) return this.findLoadedClass("main.replacementpolicy." + name); else {
                    Class c = defineClass("main.replacementpolicy." + name, buf, 0, buf.length);
                    return c;
                } else if (findLoadedClass("main.allocationpolicy." + name) != null) return this.findLoadedClass("main.allocationpolicy." + name); else return defineClass("main.allocationpolicy." + name, buf, 0, buf.length);
            }
        }
        throw new ClassNotFoundException();
    }

    protected byte[] getClassData(String directory, String name) {
        String classFile = directory + "/" + name.replace('.', '/') + ".class";
        int classSize = (new Long((new File(classFile)).length())).intValue();
        byte[] buf = new byte[classSize];
        try {
            FileInputStream filein = new FileInputStream(classFile);
            classSize = filein.read(buf);
            filein.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return buf;
    }
}

/**
 * Classe di configurazione della "MemoryUnit", ossia del MemoryModel,
 * MemoryController e AllocationPolicy<p>
 * Il flusso di creazione segue questa linea<p>
 *   1. Creazione del MemoryModel, tramite createMemoryModel() (eventualmente
 *   settaggio dei parametri ad essa concernenti);<p>
 *   2. Settaggio della politica di allocazione (in caso di memoria segmentata),
 *   ed eventuali impostazioni dei parametri;<p>
 *   3. Creazione dell'unita' tramite createConfiguratedMemory().<p>
 * @author Luca Piergiovanni
 * @version 1.6
 *
 */
public class MemoryConfiguration {

    /**
	 * Flag indicante se il sistema di memoria e' configurato
	 */
    private boolean configuratedMemory_ = false;

    /**
	 * capacita della RAM in Byte.
	 */
    private int memoryCapacity_ = 32;

    /**
	 * Dimensione della pagina in Byte memoria paginata.
	 */
    private int pageSize_ = 2;

    /**
	 * Riferimento all'oggetto osservatore del model.
	 */
    private Observer observerView_;

    /**
	 * Riferimento alla politica di allocazione.
	 */
    private AllocationPolicy allocationPolicy_;

    /**
	 * Nome della politica di allocazione.
	 */
    private String allocationPolicyName_ = "FirstFit";

    /**
	 * Riferimento alla politica di rimpiazzo.
	 */
    private ReplacementPolicy replacementPolicy_ = new Fifo();

    /**
	 * Nome della politica di rimpiazzo.
	 */
    private String replacementPolicyName_ = "Fifo";

    /**
	 * Campo per il tipo di memoria.
	 */
    private MemoryController.MemoryType memoryType_ = MemoryController.MemoryType.PAGED;

    /**
	 * percorso di ricerca della politica di allocazione.
	 */
    private String allocationPolicyPath_;

    /**
	 * percorso di ricerca della politica di rimpiazzo.
	 */
    private String replacementPolicyPath_;

    /**
	 * estensione dei file da ricercare per le politiche di allocazione.
	 */
    private String extension_ = ".class";

    /**
	 * Riferimento al MemoryModel.
	 */
    private MemoryModel memoryModel_;

    /**
	 * Riferimento al MemoryController.
	 */
    private MemoryController memoryController_;

    /**
	 * Riferimento alle informazioni dei metodi delle politiche di
	 * allocazione/rimpiazzo.
	 */
    private MethodsData MethodParameter_;

    /**
	 *
	 */
    private Object[] insertedMethodParameter;

    /**
	 * Riferimento alla immagine di memoria di settaggio
	 * iniziale.
	 */
    private MemoryImage memoryImage_;

    /**
	 * Parametro usato per sapere se la configurazione
	 * e' minimalmente configurata
	 */
    private boolean properlyConfigured_ = false;

    /**
	 *
	 */
    private boolean isModified_ = false;

    /**
	 * Class loader ridefinito per caricare nuove politiche
	 */
    private SimpleClassLoader simpleClassLoader = new SimpleClassLoader(".");

    ;

    /**
	 * Costruttore della class MemoryConfiguration.
	 * @param observer  , parametro corrispondente alla osservatore del
	 * model, secondo il pattern Observer.
	 */
    public MemoryConfiguration(final Observer observer) {
        observerView_ = observer;
        simpleClassLoader.extendClasspath("policies/allocation");
        simpleClassLoader.extendClasspath("policies/replacement");
        try {
            Class memoryAreaInfo = this.simpleClassLoader.findClass("MemoryAreaInformation", this.memoryType_);
            try {
                java.lang.reflect.Member[] member = memoryAreaInfo.getDeclaredMethods();
                for (int i = 0; i < member.length; i++) {
                    ((Method) member[i]).setAccessible(true);
                }
                member = memoryAreaInfo.getDeclaredConstructors();
                for (int i = 0; i < member.length; i++) {
                    ((Constructor<Object>) member[i]).setAccessible(true);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (ClassNotFoundException e1) {
            System.out.println("QUA NEL COSTRUTTORE");
            e1.printStackTrace();
        }
        this.allocationPolicyPath_ = "policies" + System.getProperties().getProperty("file.separator") + "allocation";
        this.replacementPolicyPath_ = "policies" + System.getProperties().getProperty("file.separator") + "replacement";
    }

    /**
	 * Metodo ausiliario privato che incrementa la dimensione di
	 * un array di Object passato come parametro di 1 elemento,
	 * e lo ritorna.
	 */
    private Object[] incrementArray(Object[] array) {
        Object[] tmp;
        if (array == null) {
            tmp = new Object[1];
        } else {
            tmp = new Object[array.length + 1];
            for (int i = 0; i < array.length; i++) {
                tmp[i] = array[i];
            }
        }
        return tmp;
    }

    /**
	 * Ritorna l'elenco dei nomi delle politiche esistenti, a seconda
	 * del tipo memoria selezionata.
	 * <p>
	 * Inline.
	 * @return String[] elenco dei nomi delle politiche disponibili
	 * @throws FileNotFoundException lanciata in caso non vengano
	 * trovati i file ricercati
	 */
    public final String[] getPolicyName() throws FileNotFoundException {
        Class cerca = null;
        File f;
        if (this.memoryType_ == MemoryController.MemoryType.PAGED) {
            f = new File(this.replacementPolicyPath_);
        } else {
            f = new File(this.allocationPolicyPath_);
        }
        if (!f.exists()) {
            throw new FileNotFoundException("File .class " + "not found in " + f.getAbsolutePath());
        }
        FilenameFilter pathFilter = new Filter(extension_);
        String[] files = f.list(pathFilter);
        Vector<String> a = new Vector<String>();
        for (int i = 0; i < files.length; i++) {
            a.add(files[i]);
        }
        Collections.sort(a);
        files = new String[a.size()];
        a.toArray(files);
        Vector<String> tmpFiles = new Vector<String>();
        int k = 0;
        for (int i = 0; i < files.length; i++) {
            Constructor<Object> c;
            Object x;
            try {
                String tmp = files[i].substring(0, files[i].lastIndexOf("."));
                if (this.memoryType_ == MemoryController.MemoryType.PAGED) {
                    cerca = simpleClassLoader.findClass(tmp, this.memoryType_);
                    if (cerca.getConstructors().length != 0) {
                        c = (Constructor<Object>) cerca.getConstructors()[0];
                        if (c.getParameterTypes().length == 0) {
                            x = c.newInstance();
                        } else {
                            x = new Object();
                        }
                        if (x instanceof ReplacementPolicy) {
                            tmpFiles.add(tmp);
                            k++;
                        }
                    }
                } else {
                    cerca = simpleClassLoader.findClass(tmp, this.memoryType_);
                    if (cerca.getConstructors().length != 0) {
                        c = (Constructor<Object>) cerca.getConstructors()[0];
                        if (c.getParameterTypes().length != 0) {
                            x = c.newInstance(new MemoryModel(100));
                        } else {
                            x = new Object();
                        }
                        if (x instanceof AllocationPolicy) {
                            tmpFiles.add(tmp);
                            k++;
                        }
                    }
                }
            } catch (NoClassDefFoundError e) {
                System.out.println("QUA NEL METODO");
            } catch (Exception e) {
            }
        }
        if (tmpFiles.size() <= 0) {
            throw new FileNotFoundException("Nessuna politica trovata");
        }
        files = new String[tmpFiles.size()];
        return tmpFiles.toArray(files);
    }

    /**
	 * Seleziona la politica di allocazione/rimpiazzo da usare nella simulazione.
	 * Il caricamento delle Policy avviene a run-time.
	 * <p>
	 * Inline.
	 * @param PolicyName , rappresenta il nome della politica
	 * da usare.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
    public final void setPolicy(final String PolicyName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoClassDefFoundError {
        if (memoryModel_ == null) {
            throw new InstantiationException("E' necessario chiamare createMemoryModel() prima di usare questo metodo");
        }
        Constructor<Object> c;
        if (((this.memoryType_ == MemoryController.MemoryType.PAGED) && !(PolicyName.equals(this.replacementPolicyName_))) || ((this.memoryType_ == MemoryController.MemoryType.SEGMENTED) && !(PolicyName.equals(this.allocationPolicyName_)))) {
            if (this.memoryType_ == MemoryController.MemoryType.PAGED) {
                this.replacementPolicyName_ = PolicyName;
                try {
                    Class cerca = simpleClassLoader.findClass(PolicyName, this.memoryType_);
                    c = (Constructor<Object>) cerca.getConstructors()[0];
                    this.replacementPolicy_ = (ReplacementPolicy) c.newInstance();
                } catch (NoClassDefFoundError e) {
                    throw e;
                }
            } else {
                this.allocationPolicyName_ = PolicyName;
                Class cerca = simpleClassLoader.findClass(PolicyName, this.memoryType_);
                c = (Constructor<Object>) cerca.getConstructors()[0];
                this.allocationPolicy_ = (AllocationPolicy) c.newInstance(memoryModel_);
            }
            if (this.properlyConfigured_ == true) {
                System.out.println("QUA DENTRO CI 6??");
                this.isModified_ = true;
            }
            this.properlyConfigured_ = false;
            this.insertedMethodParameter = new Object[this.getPolicySetMethodName().length];
        }
    }

    /**
	 * Ritorna l'elenco dei metodi di settaggio della politica
	 * selezionata.
	 * <p>
	 * Inline.
	 * @return ritorna i nomi dei metodi di settaggio della
	 * politica scelta.
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 */
    public final String[] getPolicySetMethodName() throws InvocationTargetException, ClassNotFoundException {
        if (replacementPolicy_ == null || (this.memoryType_ == MemoryController.MemoryType.SEGMENTED && allocationPolicy_ == null)) {
            throw new ClassNotFoundException("Politica non ancora selezionata");
        }
        java.lang.reflect.Method[] m;
        if (this.memoryType_ == MemoryController.MemoryType.PAGED) {
            Class cerca = simpleClassLoader.findClass(replacementPolicyName_, this.memoryType_);
            m = cerca.getMethods();
        } else {
            Class cerca = simpleClassLoader.findClass(allocationPolicyName_, this.memoryType_);
            m = cerca.getMethods();
        }
        Vector<String> ret = new Vector<String>();
        for (int i = 0; i < m.length; i++) {
            if ((m[i].getName()).startsWith("set")) {
                ret.add((m[i].getName()).substring(3));
            }
        }
        String[] tmp = new String[ret.size()];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = ret.get(i);
        }
        return tmp;
    }

    /**
	 * Metodo che ritorna un MethodsData, in modo tale da poter
	 * invocare tutti i metodi di settaggio della politica di allocazione.
	 * <p>
	 * Inline.
	 * @return AllocationMethodsData, classe rappresentante tutte le
	 * informazioni utili dei metodi di settaggio della politica di allocazione
	 * @throws ClassNotFoundException
	 */
    public final MethodsData getPolicyMethodsInformation() throws ClassNotFoundException {
        if (replacementPolicy_ == null && (this.memoryType_ == MemoryController.MemoryType.SEGMENTED && allocationPolicy_ == null)) {
            throw new ClassNotFoundException("Politica non ancora selezionata");
        }
        Method[] theMethods;
        if (this.memoryType_ == MemoryController.MemoryType.PAGED) {
            Class cerca = simpleClassLoader.findClass(replacementPolicyName_, this.memoryType_);
            theMethods = cerca.getMethods();
        } else {
            Class cerca = simpleClassLoader.findClass(allocationPolicyName_, this.memoryType_);
            theMethods = cerca.getMethods();
        }
        Vector<String> methodName = new Vector<String>();
        Vector<Class<Object>> returnType = new Vector<Class<Object>>();
        Vector<Class<Object>> parameterType = new Vector<Class<Object>>();
        Vector<Class<Object>> exceptionType = new Vector<Class<Object>>();
        Vector<Method> methodList = new Vector<Method>();
        for (int i = 0; i < theMethods.length; i++) {
            if ((theMethods[i].getName()).startsWith("set")) {
                methodList.add(theMethods[i]);
                methodName.add(theMethods[i].getName());
                if (theMethods[i].getParameterTypes().length != 1 || theMethods[i].getExceptionTypes().length != 1) {
                    throw new IllegalArgumentException("Politica non valida");
                }
                parameterType.add((Class<Object>) (theMethods[i].getParameterTypes())[0]);
                returnType.add((Class<Object>) theMethods[i].getReturnType());
                exceptionType.add((Class<Object>) theMethods[i].getExceptionTypes()[0]);
            }
        }
        MethodParameter_ = new MethodsData(methodList, methodName, returnType, parameterType, exceptionType);
        return MethodParameter_;
    }

    /**
	 * Metodo in grado di invocare dinamicamente i metodi di settaggio delle
	 * politiche di allocazione. Si aspetta un parametro Method che rappresenta
	 * il metodo da invocare e un parametro da passare al metodo.
	 * <p>
	 * Inline.
	 * @param method , oggetto rappresentante il metodo sulla quale fare
	 * l'invocazione.
	 * @param parameters , parametri passati al metodo method.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
    public final void invokePolicyMethod(final Method method, final Object parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, CannotUseThisMethodException {
        if (replacementPolicy_ == null && (this.memoryType_ == MemoryController.MemoryType.SEGMENTED && allocationPolicy_ == null)) {
            throw new ClassNotFoundException("Politica non ancora selezionata");
        }
        try {
            if (this.memoryType_ == MemoryController.MemoryType.PAGED) {
                method.invoke(this.replacementPolicy_, parameters);
            } else {
                method.invoke(this.allocationPolicy_, parameters);
            }
            for (int i = 0; i < this.MethodParameter_.getNumberOfSetMethod(); i++) {
                if (this.MethodParameter_.getMethod(i) == method) {
                    this.insertedMethodParameter[i] = parameters;
                }
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Parametro " + "di tipo " + parameters.getClass().getName() + " incorretto per il metodo <<" + method.toGenericString() + ">>");
        }
        if (this.properlyConfigured_ == true) {
            this.isModified_ = true;
        }
        this.properlyConfigured_ = false;
    }

    /**
	 * Metodo incaricato di settare la dimensione della memoria.
	 * @param memoryCapacity dimensione della memoria in byte
	 * @throws IllegalArgumentException , lanciata in caso di parametro
	 * non sia corretto.
	 */
    public final void setMemoryCapacity(final int memoryCapacity) throws IllegalArgumentException {
        if (memoryCapacity != this.memoryCapacity_) {
            double c = (Math.log(memoryCapacity) / Math.log(2));
            int x = (int) c;
            int x1 = (int) Math.ceil(c);
            if (x != x1) {
                throw new IllegalArgumentException(memoryCapacity + " non" + " e' una NOTA potenza di 2");
            }
            memoryCapacity_ = memoryCapacity;
            if (this.properlyConfigured_ == true) {
                this.isModified_ = true;
            }
            this.properlyConfigured_ = false;
        }
    }

    /**
	 * Setta la dimensione delle pagine della memoria paginata.
	 * @param pageSize dimensione della pagina in byte
	 * @throws IllegalArgumentException , lanciata in caso di parametro
	 * non sia corretto.
	 */
    public final void setPageSize(final int pageSize) throws IllegalArgumentException {
        System.out.println("page passato alla fun: " + pageSize + " page mio: " + this.pageSize_);
        if (pageSize != this.pageSize_) {
            double c = (Math.log(pageSize) / Math.log(2));
            int x = (int) c;
            int x1 = (int) Math.ceil(c);
            if (x != x1) {
                throw new IllegalArgumentException(pageSize + " non" + " e' una NOTA potenza di 2");
            }
            pageSize_ = pageSize;
            if (this.properlyConfigured_ == true) {
                this.isModified_ = true;
            }
            this.properlyConfigured_ = false;
        }
    }

    /**
	 * Seleziona il tipo di memoria da usare nella simulazione.
	 * @param MemoryController.memoryType indica il tipo di memoria da
	 *  usare.
	 */
    public final void setMemoryType(final MemoryController.MemoryType memoryType) {
        if (memoryType != this.memoryType_) {
            memoryType_ = memoryType;
            if (this.properlyConfigured_ == true) {
                this.isModified_ = true;
            }
            this.properlyConfigured_ = false;
        }
    }

    /**
	 * Metodo incaricato di creare il MemoryModel.
	 * @throws IllegalArgumentException , lanciata in caso di parametro
	 * non sia corretto.
	 * @throws SizeLimitExceededException , in caso di memoria eccessivamente
	 * grande.
	 */
    public final void createMemoryModel() throws IllegalArgumentException, SizeLimitExceededException {
        this.memoryModel_ = new MemoryModel(this.memoryCapacity_);
        this.memoryModel_.addObserver(observerView_);
        if (this.properlyConfigured_ == false) {
            this.isModified_ = true;
        }
    }

    /**
	 * Metodo incaricato di creare tutta la MemoryUnit funzionante necessaria
	 * all'applicazione.
	 * <p>
	 * PRE REQUISITO
	 * Chiamata di createMemoryModel()
	 * @throws IllegalArgumentException
	 * @throws SizeLimitExceededException
	 * @throws InstantiationException lanciata se si richiama il metodo prima di
	 * creare il MemoryModel.
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
    public final void createConfiguratedMemory() throws IllegalArgumentException, SizeLimitExceededException, InstantiationException, CannotUseThisMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        if (memoryModel_ == null) {
            throw new CannotUseThisMethodException("E' necessario chiamare " + "createMemoryModel() prima di invocare il metodo createConfiguratedMemory)");
        }
        if (this.memoryType_ == MemoryController.MemoryType.PAGED && (this.memoryCapacity_ / this.pageSize_) > 16) throw new IllegalArgumentException("Stai cercando di creare una simulazione che possa indirizzare piu' " + "di 16 pagine.");
        memoryController_ = new MemoryController(memoryModel_, memoryType_);
        memoryController_.setFrameSize(pageSize_);
        if (this.memoryType_ == MemoryController.MemoryType.PAGED && this.replacementPolicy_ == null) {
            this.setPolicy("Fifo");
            allocationPolicy_ = new FirstFit(this.memoryModel_);
        }
        if (this.memoryType_ == MemoryController.MemoryType.SEGMENTED && this.allocationPolicy_ == null) {
            this.allocationPolicy_ = new FirstFit(this.memoryModel_);
        }
        memoryController_.setAllocationPolicy(allocationPolicy_);
        if (!this.properlyConfigured_) {
            this.properlyConfigured_ = true;
        } else this.isModified_ = false;
        System.out.println(isModified_ + " <--------------- createConfMem isModified");
    }

    /**
	 * Metodo di aggiunta della memory area per la configurazione iniziale
	 * della ram.
	 * <p>
	 * PRE REQUISITO
	 * Chiamata di createConfiguratedMemory().
	 * @param memoryArea l'area di memoria da inserire inizialmente
	 * all'interno della ram.
	 * @exception CannotUseThisMethodException lanciata se non e'
	 * stata ancora create la memory unit.
	 * @exception IllegalArgumentException lanciata se la memoria
	 * area non rispetta i criteri di coerenza.
	 */
    public final void addMemoryArea(final MemoryArea memoryArea) throws CannotUseThisMethodException, IllegalArgumentException {
        if (!this.configuratedMemory_) {
            throw new CannotUseThisMethodException("E' necessario chiamare " + "createMemoryModel() prima di invocare il metodo addMemoryArea()");
        }
        if (this.memoryImage_ == null) {
            this.memoryImage_ = new MemoryImage(this.replacementPolicy_, this.memoryCapacity_);
        }
        this.memoryImage_.add(memoryArea);
        this.properlyConfigured_ = false;
    }

    /**
	 * Rimuove l'area di memoria inserita
	 *<p>
	 * PRE REQUISITO
	 * Chiamata di createConfiguratedMemory().
	 * @param memoryArea area di memoria da rimuovere
	 * @return true se la rimozione e' riuscita, false altrimenti.
	 * @throws CannotUseThisMethodException lanciata se non e'
	 * stata ancora create la memory unit.
	 */
    public final boolean removeMemoryArea(final MemoryArea memoryArea) throws CannotUseThisMethodException {
        if (!this.configuratedMemory_) {
            throw new CannotUseThisMethodException("E' necessario chiamare " + "createMemoryModel() prima di invocare il metodo removeMemoryArea()");
        }
        boolean check = this.memoryImage_.remove(memoryArea);
        if (check && this.memoryImage_.isEmpty()) {
            this.memoryImage_ = null;
        }
        if (check) {
            this.properlyConfigured_ = false;
        }
        return check;
    }

    /**
	 * Metodo per il settaggio dello stato iniziale della memoria.
	 * @throws CannotUseThisMethodException
	 */
    public final void createInitialMemoryConfiguration() throws CannotUseThisMethodException {
        if (!this.configuratedMemory_) {
            throw new CannotUseThisMethodException("E' necessario chiamare " + "createMemoryModel() prima di invocare il metodo createInitialMemoryConfiguration()");
        }
        if (this.memoryImage_ == null) {
            throw new CannotUseThisMethodException("Nessuna MemoryArea " + "inserita");
        }
        this.memoryController_.setModelInitialState(this.memoryImage_);
    }

    /**
	 * Ritorna un riferimento alla memoryType, in modo
	 * da permettere a ProcessConfiguration di riconoscere il
	 * tipo di memoria in uso.
	 * @return , tipo di memoria in uso.
	 */
    public MemoryController.MemoryType getMemoryType() {
        return this.memoryType_;
    }

    /**
	 * Ritorna la dimensione delle pagine
	 * @return , dimensione delle pagine.
	 */
    public int getPageFrameSize() {
        return this.pageSize_;
    }

    /**
	 * Metodo che ritorna la dimensione della memoria.
	 * @return dimensione della memoria.
	 */
    public int getMemoryCapacity() {
        return this.memoryCapacity_;
    }

    /**
	 * Permette a ProcessConfiguration di sapere
	 * se la MemoryUnit e' correttamente configurata.
	 * @return , true se configurato cor
	 * rettamente, false altrimenti.
	 */
    boolean isConfigured() {
        return this.properlyConfigured_;
    }

    /**
	 * Ritorna un riferimento al memoryController
	 * correttamente configurato.
	 * @return Riferimento al memoryController se correttamente
	 * configurato.
	 * @throws CannotUseThisMethodException , se la MemoryUnit non e'
	 * ancora configurata.
	 */
    MemoryController getMemoryController() throws CannotUseThisMethodException {
        if (!this.isConfigured()) {
            throw new CannotUseThisMethodException("MemoryUnit non ancora " + "correttamente configurata");
        }
        return this.memoryController_;
    }

    /**
	 * Ritorna un riferimento alla ReplacementPolicy, utile
	 * alla logicUnit
	 * @return riferimento alla ReplacementPolicy settata
	 * @throws CannotUseThisMethodException , se la memoryUnit non e'
	 * ancora configurata
	 */
    ReplacementPolicy getReplacementPolicy() throws CannotUseThisMethodException {
        if (!this.isConfigured()) {
            throw new CannotUseThisMethodException("MemoryUnit non ancora " + "correttamente configurata");
        }
        return this.replacementPolicy_;
    }

    /**
	 * Metodo che ritorna il nome della politica selezionata
	 * @return il nome della politica selezionata.
	 */
    public final String getSelectedPolicyName() {
        if (this.memoryType_ == MemoryController.MemoryType.PAGED) {
            return this.replacementPolicyName_;
        } else {
            return this.allocationPolicyName_;
        }
    }

    /**
	 * metodo che ritorna i parametri passati alla politica
	 * di allocazione, se esistenti. Altrimenti ritorna
	 * null
	 * @return Object[]
	 */
    Object[] getInsertedMethodParameters() {
        return this.insertedMethodParameter;
    }

    /**
	 * Metodo che indica se sono avvenute modifiche
	 * nella configurazione.
	 * @return true se sono avvenute modifiche, false altrimenti
	 */
    public boolean isModified() {
        return this.isModified_;
    }
}

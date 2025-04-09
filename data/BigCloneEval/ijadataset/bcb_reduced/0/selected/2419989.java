package dryven.persistence.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import dryven.NotImplementedException;
import dryven.codegen.asm.ClassBuilder;

public class DryvenPersistenceUnitInfo implements PersistenceUnitInfo {

    private List<String> _managedClassNames;

    private String _providerClassName;

    private DataSource _nonJtaDataSource;

    private String _persistenceUnitName;

    private ClassLoader _tempClassLoader;

    private ClassLoader _entityClassLoader;

    private List<URL> _emptyURLList;

    private List<String> _emptyStringList;

    public DryvenPersistenceUnitInfo(List<String> managedClassNames, String providerClassName, DataSource nonJtaDataSource, String persistenceUnitName, ClassLoader tempClassLoader, ClassLoader entityClassLoader) {
        super();
        _managedClassNames = managedClassNames;
        _providerClassName = providerClassName;
        _nonJtaDataSource = nonJtaDataSource;
        _persistenceUnitName = persistenceUnitName;
        _tempClassLoader = tempClassLoader;
        _entityClassLoader = entityClassLoader;
        _emptyStringList = new ArrayList<String>(0);
        _emptyURLList = new ArrayList<URL>(0);
    }

    @Override
    public void addTransformer(ClassTransformer arg0) {
        throw new NotImplementedException();
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return true;
    }

    @Override
    public ClassLoader getClassLoader() {
        return _entityClassLoader;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return _emptyURLList;
    }

    @Override
    public DataSource getJtaDataSource() {
        return null;
    }

    @Override
    public List<String> getManagedClassNames() {
        return _managedClassNames;
    }

    @Override
    public List<String> getMappingFileNames() {
        return _emptyStringList;
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return _tempClassLoader;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return _nonJtaDataSource;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return _providerClassName;
    }

    @Override
    public String getPersistenceUnitName() {
        return _persistenceUnitName;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        try {
            return new URL("mock", "mock", 80, "mock", new MemoryURLHandler());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Properties getProperties() {
        return new Properties();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return null;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.NONE;
    }

    @Override
    public ValidationMode getValidationMode() {
        return ValidationMode.NONE;
    }

    public static void main(String[] args) {
        try {
            URL u = new URL("mock", "mock", 80, "mock", new MemoryURLHandler());
            InputStream is = u.openStream();
            System.out.println(is.getClass().getName());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MemoryURLHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new MemoryURLConnection(u);
    }
}

class MemoryURLConnection extends URLConnection {

    protected MemoryURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public InputStream getInputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(0);
        ZipOutputStream jos = new ZipOutputStream(os);
        String className = "Mock";
        ClassBuilder b = new ClassBuilder(className);
        byte[] classData = b.finish();
        ZipEntry e = new ZipEntry(className + ".class");
        jos.putNextEntry(e);
        jos.write(classData);
        jos.closeEntry();
        jos.finish();
        jos.close();
        byte[] data = os.toByteArray();
        return new ByteArrayInputStream(data);
    }
}

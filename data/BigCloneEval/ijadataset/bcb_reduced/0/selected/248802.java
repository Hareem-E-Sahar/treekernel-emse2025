package org.nmc.pachyderm.foundation;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import ca.ucalgary.apollo.core.*;
import ca.ucalgary.apollo.data.*;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;

public abstract class PXBundle {

    private static final Class[] DefaultConstructorArguments = new Class[] { URL.class, boolean.class };

    private static NSDictionary _protocolHandlers;

    private static NSDictionary _bundleFormats;

    private static final String BundleFormatsFile = "PXBundleFormats.plist";

    private NSDictionary _formatDefinition = NSDictionary.EmptyDictionary;

    private static final String TemplatesPathKey = "TemplatesPath";

    private static final String ResourcesPathKey = "ResourcesPath";

    private static final String DataPathKey = "DataPath";

    public static final String Dev1Format = "Dev1";

    public static final String Dev2Format = "Dev2";

    private static String _defaultBundleFormat;

    private static final String BundleFormatPropertyKey = "PXBundleFormat";

    static {
        _protocolHandlers = new NSDictionary(new Object[] { PXLocalBundle.class }, new String[] { "file" });
        NSBundle pxf = NSBundle.bundleForName("PXFoundation");
        String resourcePath = pxf.resourcePathForLocalizedResourceNamed(BundleFormatsFile, null);
        URL pathURL = pxf.pathURLForResourcePath(resourcePath);
        _bundleFormats = (NSDictionary) NSPropertyListSerialization.propertyListWithPathURL(pathURL);
        _defaultBundleFormat = (String) CXProperties.getProperty(BundleFormatPropertyKey, Dev1Format);
    }

    public PXBundle(URL url, boolean createIfNeeded) {
        super();
    }

    protected void prepareBundleForFormat(String format) {
        NSDictionary definition = (NSDictionary) _bundleFormats.objectForKey(format);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown bundle format: " + format + ". Either this bundle is corrupt or is made with a newer version of Pachyderm.");
        }
        _formatDefinition = definition;
    }

    public static String defaultBundleFormat() {
        return _defaultBundleFormat;
    }

    public static PXBundle bundleWithURL(URL url) {
        return bundleWithURL(url, false);
    }

    public static PXBundle bundleWithURL(URL url, boolean createIfNeeded) {
        if (url == null) {
            return null;
        }
        String protocol = url.getProtocol();
        Class handler = _handlerForProtocol(protocol);
        if (handler == null) {
            throw new IllegalArgumentException("PXBundle does not support bundles with protocol '" + protocol + "'.");
        }
        try {
            Constructor constructor = handler.getConstructor(DefaultConstructorArguments);
            PXBundle bundle = (PXBundle) constructor.newInstance(new Object[] { url, new Boolean(createIfNeeded) });
            return bundle;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PXBundle _bundleWithPath(String path) {
        try {
            URL newUrl = new URL("file://" + path);
            if (newUrl != null) {
            } else {
            }
            return bundleWithURL(newUrl, true);
        } catch (MalformedURLException murle) {
            System.out.println("PXBundle._bundleWithPath(file://" + path + ") throws a MalformedURLException: \n");
            murle.printStackTrace();
            return null;
        }
    }

    private static Class _handlerForProtocol(String protocol) {
        return (Class) _protocolHandlers.objectForKey(protocol);
    }

    protected class ResourceReference {

        private String _identifier;

        private CXManagedObject _object;

        private PXReferenceCountArray _contextReferences = new PXReferenceCountArray();

        private String _baseFileName = null;

        ResourceReference(String identifier, CXManagedObject object) {
            _identifier = identifier;
            _object = object;
            URL objectURL = object.url();
            System.out.println("PXBundle.ResourceReference(): object = " + object + "\n");
            System.out.println("PXBundle.ResourceReference(): objectURL = " + objectURL + "\n");
            if (objectURL != null) {
                String objectPath = objectURL.getPath();
                String objectFileName = NSPathUtilities.lastPathComponent(objectPath);
                objectFileName = PXUtility.stripNonAlphaNumerics(NSPathUtilities.stringByDeletingPathExtension(objectFileName));
                _baseFileName = objectFileName;
            }
        }

        String identifier() {
            return _identifier;
        }

        void addContext(NSDictionary context) {
            _contextReferences.addObject(context);
        }

        void removeContext(NSDictionary context) {
            _contextReferences.removeObject(context);
        }

        public String toString() {
            return ("Reference for " + _identifier + "\ncontexts: " + _contextReferences);
        }

        protected NSArray _uniqueContexts() {
            return _contextReferences.allObjects();
        }

        int indexOfContext(NSDictionary context) {
            return _contextReferences.indexOfObject(context);
        }

        String baseFileName() {
            return _baseFileName;
        }

        String fileNameForContext(NSDictionary context) {
            String kind = (String) context.objectForKey(MD.Kind);
            String extension;
            if ("com.macromedia.flash.swf".equals(kind)) {
                extension = "swf";
            } else {
                extension = UTType.preferredTagWithClass(kind, UTType.FilenameExtensionTagClass);
                if (extension == null) {
                    extension = NSPathUtilities.pathExtension(_object.url().getPath());
                }
            }
            String _width = (String) context.valueForKey(MD.PixelWidth);
            String _height = (String) context.valueForKey(MD.PixelHeight);
            String dimensions = "";
            if ((_width != null) && (_height != null)) {
                dimensions = "-" + _width + "x" + _height;
            }
            String objectFileName = baseFileName() + dimensions;
            objectFileName = NSPathUtilities.stringByAppendingPathExtension(objectFileName, extension);
            return objectFileName;
        }
    }

    public abstract String resourcePathForObject(CXManagedObject object, NSDictionary context);

    public abstract NSArray resources();

    protected abstract NSArray _registeredResourceIdentifiers();

    protected abstract NSArray _registeredContextsForResourceIdentifier(String identifier);

    public abstract void includeObjectInContext(CXManagedObject object, NSDictionary context);

    public abstract void removeObjectInContext(CXManagedObject object, NSDictionary context);

    public void writeDataWithName(NSData data, String name) {
        writeDataWithNameToPath(data, name, "/");
    }

    public abstract void writeDataWithNameToPath(NSData data, String name, String path);

    public void writeStreamWithName(InputStream is, String name) {
        writeStreamWithNameToPath(is, name, "/");
    }

    public abstract void writeStreamWithNameToPath(InputStream is, String name, String path);

    public boolean isReadOnly() {
        return true;
    }

    public abstract URL bundleURL();

    public abstract NSDictionary infoDictionary();

    public Object objectForInfoDictionaryKey(String key) {
        return infoDictionary().objectForKey(key);
    }

    public String templatesPath() {
        return (String) _formatValueForKey(TemplatesPathKey, "");
    }

    public String resourcesPath() {
        return (String) _formatValueForKey(ResourcesPathKey, "");
    }

    public String dataPath() {
        return (String) _formatValueForKey(DataPathKey, "");
    }

    private Object _formatValueForKey(String key, Object defaultValue) {
        Object value = _formatDefinition.objectForKey(key);
        if (value == null) {
            value = defaultValue;
        } else if (value == NSKeyValueCoding.NullValue) {
            value = null;
        }
        return value;
    }

    public NSArray localizations() {
        return NSArray.EmptyArray;
    }
}

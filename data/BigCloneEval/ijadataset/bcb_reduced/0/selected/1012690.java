package ch.jester.common.utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * XML Serialisations Helper Klasse
 *
 */
public class ObjectXMLSerializer {

    JAXBContext mContext;

    private Class<?>[] mclz;

    public ObjectXMLSerializer() {
    }

    /**
	 * Den Kontext für JAXB definieren.
	 * @param pClasses
	 * @throws JAXBException
	 */
    public void prepareContext(Class<?>... pClasses) throws JAXBException {
        mclz = Arrays.copyOf(pClasses, pClasses.length + 1);
        mclz[pClasses.length] = XMLList.class;
        mContext = JAXBContext.newInstance(mclz);
    }

    /**
	 * Einen Writer erzeugen
	 * @param pFileName 
	 * @return aSerializationWriter
	 * @throws FileNotFoundException
	 */
    public SerializationWriter createWriter(String pFileName) throws FileNotFoundException {
        return new SerializationWriter(new ZipOutputStream(new FileOutputStream(pFileName)), mContext);
    }

    /**
	 * Einen ZipReader erzeugen
	 * @param pFileName
	 * @return aZipSerialisationReader
	 * @throws FileNotFoundException
	 */
    public ZipSerializationReader createReader(String pFileName) throws FileNotFoundException {
        return new ZipSerializationReader(new ZipInputStream(new FileInputStream(pFileName)), mContext);
    }

    /**
	 * Einen SerialisationReader erzeugen.
	 * @param inStream
	 * @return aSerializationReader
	 * @throws FileNotFoundException
	 */
    public SerializationReader createReader(InputStream inStream) throws FileNotFoundException {
        return new SerializationReader(inStream, mContext);
    }

    /**
	 * Reader Klasse
	 *
	 */
    public static class SerializationReader {

        InputStream mzis;

        ZipEntry entry;

        JAXBContext mContext;

        public SerializationReader(InputStream pzis, JAXBContext pContext) {
            mzis = pzis;
            mContext = pContext;
        }

        /**
		 * Lesen des Contents
		 * @return Liste mit deserialisierten Elementen oder null
		 * @throws IOException
		 */
        @SuppressWarnings("rawtypes")
        public List read() throws IOException {
            try {
                XMLList<?> pt = (XMLList<?>) mContext.createUnmarshaller().unmarshal(mzis);
                return pt.getList();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void close() throws IOException {
            mzis.close();
        }
    }

    /**
 * Zip Reader
 *
 */
    public static class ZipSerializationReader {

        ZipInputStream mzis;

        ZipEntry entry;

        JAXBContext mContext;

        public ZipSerializationReader(ZipInputStream pzis, JAXBContext pContext) {
            mzis = pzis;
            mContext = pContext;
        }

        /**
		 * Lesen des ZipEntry
		 * @param pEntriy
		 * @return Liste mit Elementen oder null
		 * @throws IOException
		 */
        @SuppressWarnings("rawtypes")
        public List read(String pEntriy) throws IOException {
            ZipEntry ze;
            while ((ze = mzis.getNextEntry()) != null) {
                if (ze.getName().equals(pEntriy)) {
                    break;
                }
            }
            try {
                XMLList<?> pt = (XMLList<?>) mContext.createUnmarshaller().unmarshal(ZipUtility.deflate(mzis, ze));
                return pt.getList();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
		 * Schliessen von den Zips
		 * @throws IOException
		 */
        public void close() throws IOException {
            mzis.closeEntry();
            mzis.close();
        }
    }

    /**
 * Writer
 *
 */
    public static class SerializationWriter {

        ZipOutputStream mzos;

        ZipEntry entry;

        JAXBContext mContext;

        public SerializationWriter(ZipOutputStream pzos, JAXBContext pContext) {
            mzos = pzos;
            mContext = pContext;
        }

        /**
		 * erzeugt neuen ZipEntry
		 * @param pString
		 * @throws IOException
		 */
        public void newEntry(String pString) throws IOException {
            if (entry != null) {
                mzos.closeEntry();
            }
            mzos.putNextEntry(entry = new ZipEntry(pString));
        }

        /**
		 * Beenden und alles schliessen
		 * @throws IOException
		 */
        public void close() throws IOException {
            mzos.flush();
            mzos.closeEntry();
            mzos.close();
        }

        /**
		 * Schreiben der Element Liste
		 * @param plist
		 */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public void write(List plist) {
            try {
                XMLList list = new XMLList();
                list.setList(plist);
                mContext.createMarshaller().marshal(list, mzos);
                mzos.flush();
            } catch (JAXBException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
		 * JAXB Context vorbereiten.
		 * @param pClz
		 * @throws JAXBException
		 */
        public void prepareContext(Class<?>... pClz) throws JAXBException {
            Class<?>[] clz = Arrays.copyOf(pClz, pClz.length + 1);
            clz[pClz.length] = XMLList.class;
            mContext = JAXBContext.newInstance(clz);
        }
    }
}

/**
 * Wrapper Element für Serialisation
 *
 * @param <T>
 */
@XmlRootElement(name = "Root")
class XMLList<T> extends ArrayList<T> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8807328009384763155L;

    protected List<T> item;

    @XmlElementWrapper(name = "List")
    @XmlElement(name = "Object")
    public List<T> getList() {
        return this.item;
    }

    public void setList(List<T> p) {
        this.item = p;
    }
}

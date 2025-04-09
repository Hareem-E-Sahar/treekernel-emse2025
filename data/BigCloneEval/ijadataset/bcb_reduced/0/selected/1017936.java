package br.ufmg.lcc.pcollecta.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import br.ufmg.lcc.arangi.commons.BasicException;
import br.ufmg.lcc.arangi.commons.MessageFactory;
import br.ufmg.lcc.pcollecta.dto.SerializableData;

public class SerializationXML {

    protected static Logger log = Logger.getLogger(SerializationXML.class);

    public interface Default {

        int BEGIN = 0;

        int QUANTITY = 10;
    }

    public interface Type {

        int INTEGER = 1;

        int TEXT = 2;

        int DATE = 3;

        int DECIMAL = 4;
    }

    public interface Lit {

        String INTEGER = "integer";

        String TEXT = "text";

        String DATE = "date";

        String DECIMAL = "decimal";
    }

    public interface CXML {

        int INDEFINITE = 0;

        int HARVEST = 1;

        int ID = 2;

        int DATE = 3;

        int SQL = 4;

        int HEADER = 5;

        int NAME = 6;

        int QUANTITY = 7;

        int DATA = 8;

        int LINE = 9;

        int COLUMN = 10;
    }

    public static final String ENCODING = "ISO-8859-1";

    private FileOutputStream fos = null;

    private ZipOutputStream zipOutputStream = null;

    private ZipFile zip;

    private XMLStreamReader xmlReader;

    private XMLStreamWriter xmlWriter;

    private int[] types = null;

    private int count = 0;

    private SerializableData serializableData;

    private int currentCXML = 0;

    private Object[] line;

    private int columnIndex = 0;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private DecimalFormat decimalFormat = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.ENGLISH));

    private DecimalFormat decHash = new DecimalFormat("#.0000000000", new DecimalFormatSymbols(Locale.ENGLISH));

    private boolean currentValueAndNull = false;

    private StringBuffer readData = new StringBuffer();

    private long beginRegister = 0;

    private long registerNumber = 0;

    private ComputeHash computeHash;

    /**
     * 
     * 
     * @param folder
     * @param dir
     * @throws BasicException
     */
    public void prepareWrite(SerializableData data, String folder) throws BasicException {
        String fileId = data.getHarvestId().toString();
        if (data.getSummaryId() != null) {
            fileId += "_" + data.getSummaryId().toString();
        }
        if (folder == null || folder.trim().equals("")) {
            throw this.errorHandling("Diret�rio Imagem de Dados n�o foi definido.", "msgErroNaoExisteDiretorioImagem", new String[] {});
        }
        try {
            fos = new FileOutputStream(new File(folder.trim(), "pdump." + fileId + ".data.xml.zip"));
        } catch (FileNotFoundException e) {
            throw this.errorHandling("Erro ao abrir arquivo de dados xml para escrita", "msgErroCriarArquivoDadosXML", e);
        }
        zipOutputStream = new ZipOutputStream(fos);
        zipOutputStream.setLevel(Deflater.DEFAULT_COMPRESSION);
        try {
            zipOutputStream.putNextEntry(new ZipEntry("pdump." + fileId + ".data.xml"));
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(zipOutputStream, ENCODING);
        } catch (Exception e) {
            throw this.errorHandling("Erro ao criar entrada em arquivo compactado", "msgErroCriarEntradaZipArquivoDadosXML", e);
        }
        XMLStreamWriter osw = null;
        try {
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(zipOutputStream, ENCODING);
        } catch (Exception e) {
            throw this.errorHandling("Erro ao criar entrada em arquivo compactado", "msgErroCriarEntradaZipArquivoDadosXML", e);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        if (!data.getTypes().isEmpty()) {
            types = new int[data.getTypes().size()];
            for (int c = 0; c < data.getTypes().size(); c++) {
                String tipo = data.getTypes().get(c);
                if (tipo.equals(Lit.INTEGER)) {
                    types[c] = Type.INTEGER;
                } else if (tipo.equals(Lit.DECIMAL)) {
                    types[c] = Type.DECIMAL;
                } else if (tipo.equals(Lit.DATE)) {
                    types[c] = Type.DATE;
                } else {
                    types[c] = Type.TEXT;
                }
            }
        }
        try {
            xmlWriter.writeStartDocument(ENCODING, "1.0");
            xmlWriter.writeStartElement("harvest");
            xmlWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xmlWriter.writeAttribute("xsi:noNamespaceSchemaLocation", "http://www.pCollecta.lcc.ufmg.br/dtd/pCollectaDataDump.xsd");
            xmlWriter.writeStartElement("id");
            xmlWriter.writeCharacters(fileId);
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("date");
            xmlWriter.writeCharacters(dateFormat.format(data.getHarvestDate()));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("sql");
            xmlWriter.writeCData(data.getQuery());
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("header");
            for (int c = 0; c < data.getHeader().size(); c++) {
                xmlWriter.writeStartElement("name");
                xmlWriter.writeAttribute("type", data.getTypes().get(c));
                xmlWriter.writeCharacters(data.getHeader().get(c));
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("quantity");
            xmlWriter.writeCharacters("" + data.getQuantity());
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("data");
        } catch (XMLStreamException e) {
            throw this.errorHandling("Erro ao escrever dados em arquivo compactado", "msgErroEscreverArquivoDadosXML", e);
        }
        computeHash = new ComputeHash();
    }

    /**
	 * 
	 * @param dados
	 * @throws BasicException
	 */
    public void writeFileLine(SerializableData data) throws BasicException {
        Date valorData = null;
        String coluna = "";
        List<String> dadosHash = new ArrayList<String>();
        for (int l = 0; l < data.getData().size(); l++) {
            Object[] line = data.getData().get(l);
            int num = l + count;
            String str = null;
            try {
                xmlWriter.writeStartElement("line");
                xmlWriter.writeAttribute("num", "" + num);
                for (int c = 0; c < line.length; c++) {
                    Object valor = line[c];
                    int tipo = types[c];
                    xmlWriter.writeStartElement("col");
                    xmlWriter.writeAttribute("num", "" + c);
                    if (valor == null) {
                        xmlWriter.writeAttribute("nulo", "true");
                    } else {
                        coluna += ">";
                        switch(tipo) {
                            case Type.TEXT:
                                str = cutOutInvalidCharacters(valor.toString());
                                xmlWriter.writeCData(str);
                                dadosHash.add(str);
                                break;
                            case Type.INTEGER:
                                xmlWriter.writeCharacters(valor.toString());
                                dadosHash.add(valor.toString());
                                break;
                            case Type.DATE:
                                valorData = (java.util.Date) valor;
                                String strData = dateFormat.format(valorData);
                                xmlWriter.writeCharacters(strData);
                                dadosHash.add(strData);
                                break;
                            case Type.DECIMAL:
                                coluna += decimalFormat.format((Number) valor) + "</col>";
                                xmlWriter.writeCharacters(decimalFormat.format((Number) valor));
                                dadosHash.add(decHash.format((Number) valor));
                        }
                    }
                    xmlWriter.writeEndElement();
                }
                xmlWriter.writeEndElement();
            } catch (Exception e) {
                throw this.errorHandling("Erro ao escrever dados em arquivo compactado", "msgErroEscreverArquivoDadosXML", e);
            }
            computeHash.adicionaLinha(dadosHash);
        }
        count += data.getData().size();
    }

    /**
	 * 
	 * @param str
	 * @return
	 */
    private String cutOutInvalidCharacters(String str) {
        char[] charray = str.toCharArray();
        StringBuffer sb = new StringBuffer();
        int j = 0;
        for (int i = 0; i < charray.length; i++) {
            char ch = charray[i];
            j = ch;
            if (ch == '\t' || ch == '\n' || j >= 32) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
	 * 
	 * @throws BasicException
	 */
    public void finalizeWrite() throws BasicException {
        computeHash.finalizeDataInput();
        try {
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("hash");
            xmlWriter.writeCharacters(computeHash.getHash());
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
            xmlWriter.writeEndDocument();
            xmlWriter.flush();
        } catch (Exception e) {
            throw this.errorHandling("Erro ao escrever dados em arquivo compactado", "msgErroEscreverArquivoDadosXML", e);
        }
        try {
            zipOutputStream.closeEntry();
        } catch (IOException e1) {
            throw this.errorHandling("Erro ao fechar entrada em arquivo de dados compactado", "msgErroFecharEntradaZipArquivoDadosXML", e1);
        }
        try {
            zipOutputStream.close();
        } catch (IOException e1) {
            throw this.errorHandling("Erro ao fechar arquivo de dados xml de escrita", "msgErroFecharArquivoEscritaDadosXML", e1);
        }
    }

    public boolean hasFile(SerializableData data, String dir) throws BasicException {
        if (dir == null) {
            dir = "";
        }
        serializableData = data;
        String fileId = data.getHarvestId().toString();
        if (data.getSummaryId() != null) {
            fileId += "_" + data.getSummaryId().toString();
        }
        try {
            File file = new File(dir.trim(), "pdump." + fileId + ".data.xml.zip");
            return file.exists();
        } catch (Exception e) {
            throw this.errorHandling("Erro ao verificar existencia de arquivo de dados", "msgErroVerificarExistenciaArquivoDadosXML", e);
        }
    }

    public void prepareRead(SerializableData data, String folder) throws BasicException {
        if (StringUtils.isEmpty(folder)) {
            throw this.errorHandling("Diretorio Imagem de Dados nao foi definido.", "msgErroNaoExisteDiretorioImagem", new String[] {});
        }
        serializableData = data;
        String fileId = data.getHarvestId().toString();
        if (data.getSummaryId() != null) {
            fileId += "_" + data.getSummaryId().toString();
        }
        try {
            zip = new ZipFile(new File(folder.trim(), "pdump." + fileId + ".data.xml.zip"));
            Enumeration entries = zip.entries();
            ZipEntry zEntry = (ZipEntry) entries.nextElement();
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlReader = xmlInputFactory.createXMLStreamReader(new InputStreamReader(zip.getInputStream(zEntry), ENCODING));
        } catch (Exception e) {
            throw this.errorHandling("Erro ao abrir arquivo de dados xml de leitura", "msgErroAbrirArquivoLeituraDadosXML", e);
        }
        boolean hasMore = true;
        count = 0;
        try {
            while (hasMore && xmlReader.hasNext()) {
                xmlReader.next();
                if (xmlReader.getEventType() == XMLStreamReader.START_ELEMENT) {
                    this.startElement(xmlReader);
                } else if (xmlReader.getEventType() == XMLStreamReader.END_ELEMENT) {
                    this.endElement(xmlReader);
                    if (xmlReader.getLocalName().equals("quantity")) {
                        hasMore = false;
                    }
                } else if (xmlReader.getEventType() == XMLStreamReader.CHARACTERS) {
                    this.characters(xmlReader.getText());
                }
            }
        } catch (Exception e) {
            throw this.errorHandling("Erro ao ler dados de arquivo xml", "msgErroLerArquivoDadosXML", e);
        }
    }

    public void readSequentialRegisterFromFile(long numRegistros) throws BasicException {
        readRegisterFromFile(count, numRegistros);
    }

    public void readRegisterFromFile(long begRegister, long regNumber) throws BasicException {
        this.beginRegister = begRegister;
        this.registerNumber = regNumber;
        this.getSerializableData().getData().clear();
        boolean temMais = true;
        try {
            while (temMais && xmlReader.hasNext()) {
                xmlReader.next();
                if (xmlReader.getEventType() == XMLStreamReader.START_ELEMENT) {
                    this.startElement(xmlReader);
                } else if (xmlReader.getEventType() == XMLStreamReader.END_ELEMENT) {
                    if (xmlReader.getLocalName().equals("data")) {
                        temMais = false;
                    }
                    if (xmlReader.getLocalName().toLowerCase().equals("line")) {
                        if (count >= (beginRegister + registerNumber - 1)) {
                            temMais = false;
                        }
                    }
                    this.endElement(xmlReader);
                } else if (xmlReader.getEventType() == XMLStreamReader.CHARACTERS) {
                    this.characters(xmlReader.getText());
                }
            }
        } catch (Exception e) {
            throw this.errorHandling("Erro ao ler dados de arquivo xml", "msgErroLerArquivoDadosXML", e);
        }
    }

    public void finalizeRead() throws BasicException {
        try {
            zip.close();
        } catch (Exception e) {
            throw this.errorHandling("Erro ao fechar arquivo de dados xml de leitura", "msgErroFecharArquivoDadosLeituraXML", e);
        }
    }

    public void startElement(XMLStreamReader xmlReader) throws Exception {
        readData.delete(0, readData.length());
        String qName = xmlReader.getLocalName();
        if (qName.toLowerCase().equals("col")) {
            if (xmlReader.getAttributeValue(null, "nulo") == null) {
                currentValueAndNull = false;
            } else {
                currentValueAndNull = (xmlReader.getAttributeValue(null, "nulo")).equals("true");
            }
            currentCXML = CXML.COLUMN;
        } else if (qName.toLowerCase().equals("line")) {
            columnIndex = 0;
            if (count >= beginRegister) {
                line = new Object[serializableData.getHeader().size()];
            }
            currentCXML = CXML.LINE;
        } else if (qName.toLowerCase().equals("data")) {
            count = 0;
            currentCXML = CXML.DATA;
        } else if (qName.toLowerCase().equals("quantity")) {
            currentCXML = CXML.QUANTITY;
        } else if (qName.toLowerCase().equals("header")) {
            currentCXML = CXML.HEADER;
        } else if (qName.toLowerCase().equals("name")) {
            serializableData.getTypes().add(xmlReader.getAttributeValue(null, "type"));
            currentCXML = CXML.NAME;
        } else if (qName.toLowerCase().equals("sql")) {
            currentCXML = CXML.SQL;
        } else if (qName.toLowerCase().equals("date")) {
            currentCXML = CXML.DATE;
        } else if (qName.toLowerCase().equals("id")) {
            currentCXML = CXML.ID;
        } else if (qName.toLowerCase().equals("harvest")) {
            currentCXML = CXML.HARVEST;
            if (serializableData == null) {
                serializableData = new SerializableData();
            }
            serializableData.setHeader(new ArrayList());
            serializableData.setTypes(new ArrayList());
            serializableData.setData(new ArrayList());
        }
    }

    public void endElement(XMLStreamReader xmlReader) throws Exception {
        String qName = xmlReader.getLocalName().toLowerCase();
        currentCXML = CXML.INDEFINITE;
        if (qName.equals("col")) {
            if (count >= beginRegister && !currentValueAndNull) {
                line[columnIndex] = dataConvert(readData.toString(), types[columnIndex]);
            }
            columnIndex++;
        } else if (qName.equals("line")) {
            if (count >= beginRegister) {
                serializableData.getData().add(line);
            }
            count++;
        } else if (qName.equals("quantity")) {
            serializableData.setQuantity((Long) dataConvert(readData.toString(), Type.INTEGER));
        } else if (qName.equals("header")) {
            if (serializableData.getTypes().size() > 0) {
                types = new int[serializableData.getTypes().size()];
                for (int c = 0; c < serializableData.getTypes().size(); c++) {
                    String tipo = serializableData.getTypes().get(c);
                    if (tipo.equals(Lit.INTEGER)) {
                        types[c] = Type.INTEGER;
                    } else if (tipo.equals(Lit.DECIMAL)) {
                        types[c] = Type.DECIMAL;
                    } else if (tipo.equals(Lit.DATE)) {
                        types[c] = Type.DATE;
                    } else {
                        types[c] = Type.TEXT;
                    }
                }
            }
        } else if (qName.equals("name")) {
            serializableData.getHeader().add(readData.toString());
        } else if (qName.equals("sql")) {
            serializableData.setQuery(readData.toString());
        } else if (qName.equals("date")) {
            serializableData.setHarvestDate((Date) dataConvert(readData.toString(), Type.DATE));
        }
    }

    public void characters(String text) throws Exception {
        switch(currentCXML) {
            case CXML.COLUMN:
                if (!currentValueAndNull && count >= beginRegister) {
                    readData.append(text);
                }
                break;
            case CXML.QUANTITY:
            case CXML.NAME:
            case CXML.SQL:
            case CXML.DATE:
                readData.append(text);
        }
    }

    private Object dataConvert(String value, int type) throws Exception {
        if (value == null) {
            return null;
        }
        switch(type) {
            case Type.TEXT:
                return value;
            case Type.INTEGER:
                try {
                    return new Long(value);
                } catch (NumberFormatException e2) {
                    throw new Exception("Erro ao converter dado lido de XML para tipo Long. Valor: " + value);
                }
            case Type.DECIMAL:
                try {
                    return new Double((decimalFormat.parse(value)).doubleValue());
                } catch (NumberFormatException e1) {
                    throw new Exception("Erro ao converter dado lido de XML para tipo Double. Valor: " + value);
                }
            case Type.DATE:
                try {
                    return dateFormat.parse(value);
                } catch (ParseException e) {
                    throw new Exception("Erro ao converter dado lido de XML para tipo Date. Valor: " + value);
                }
        }
        return null;
    }

    public SerializableData getSerializableData() {
        return serializableData;
    }

    public void setSerializableData(SerializableData serializableData) {
        this.serializableData = serializableData;
    }

    /**
	 * Error handling for this class. It's a new BasicException object by a key
	 * and one exception fired.
	 * 
	 * @param rootMessage
	 * @param key
	 * @param e
	 * @return
	 */
    protected BasicException errorHandling(String rootMessage, String key, Exception e) {
        return errorHandling(rootMessage, key, e, "ApplicationResources");
    }

    /**
	 * Error handling for this class. It's a new BasicException object by a key
	 * and one exception fired.
	 * 
	 * @param rootMessage
	 * @param key
	 * @param e
	 * @param bundle
	 * @return
	 */
    protected BasicException errorHandling(String rootMessage, String key, Exception e, String bundle) {
        String message = MessageFactory.composeMessageFromException(e);
        log.error(rootMessage + ": " + message);
        return new BasicException(rootMessage + ": " + message, key, new String[] { message }, bundle);
    }

    /**
	 * Error handling for this class. It's a new BasicException object by a key and
	 * arguments supplied.
	 * @param rootMessage
	 * @param key
	 * @param args
	 * @return
	 */
    protected BasicException errorHandling(String rootMessage, String key, String[] args) {
        return this.errorHandling(rootMessage, key, args, "ApplicationResources");
    }

    /**
	 * Error handling for this class. It's a new BasicException object by a key and
	 * arguments supplied.
	 * @param rootMessage
	 * @param key
	 * @param args
	 * @param bundle
	 * @return
	 */
    protected BasicException errorHandling(String rootMessage, String key, String[] args, String bundle) {
        log.error(rootMessage);
        if (args == null) {
            args = new String[] {};
        }
        return new BasicException(rootMessage, key, args, bundle);
    }

    /**
     * Retrieve the calculated hash for the file with hexadecimal string
     * @return String hexadecimal with the hash
     */
    public String getHash() {
        return computeHash.getHash();
    }
}

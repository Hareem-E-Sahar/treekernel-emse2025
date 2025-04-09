package br.gov.mec.pingifesManager.comuns;

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
import org.apache.log4j.Logger;
import br.gov.mec.pingifesManager.dto.DadosSeriais;
import br.ufmg.lcc.arangi.commons.BasicException;
import br.ufmg.lcc.arangi.commons.MessageFactory;

public class SerializacaoXML {

    public static final int INTEIRO = 1;

    public static final int TEXTO = 2;

    public static final int DECIMAL = 3;

    public static final int DATA = 4;

    public static final String LIT_INTEIRO = "inteiro";

    public static final String LIT_TEXTO = "texto";

    public static final String LIT_DATA = "data";

    public static final String LIT_DECIMAL = "decimal";

    public static final int CXML_INDEFINDO = 0;

    public static final int CXML_COLETA = 1;

    public static final int CXML_ID = 2;

    public static final int CXML_DATA = 3;

    public static final int CXML_SQL = 4;

    public static final int CXML_CABECALHO = 5;

    public static final int CXML_NOME = 6;

    public static final int CXML_QUANTIDADE = 7;

    public static final int CXML_DADOS = 8;

    public static final int CXML_LINHA = 9;

    public static final int CXML_COL = 10;

    protected static Logger log = Logger.getLogger(SerializacaoXML.class);

    private FileOutputStream fos = null;

    private ZipOutputStream zipOutputStream = null;

    ZipFile zip;

    private XMLStreamReader xmlReader;

    private XMLStreamWriter xmlWriter;

    private int[] tipos = null;

    private int cont = 0;

    private DadosSeriais dadosSeriais;

    private int CXMLCorrente = 0;

    private Object[] linha;

    private int indiceColuna = 0;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private DecimalFormat decimalFormat = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.ENGLISH));

    private DecimalFormat decHash = new DecimalFormat("#.0000000000", new DecimalFormatSymbols(Locale.ENGLISH));

    private boolean valorCorrenteENulo = false;

    private StringBuffer dadoLido = new StringBuffer();

    private long registroInicial = 0;

    private long numeroRegistros = 0;

    private CalculaHash calculadorHash;

    public static final String ENCODING = "ISO-8859-1";

    public void preparaEscrita(DadosSeriais dados, String dir) throws BasicException {
        String idArquivo = dados.getIdColeta().toString();
        if (dados.getIdResumo() != null) {
            idArquivo += "_" + dados.getIdResumo().toString();
        }
        if (dir == null || dir.trim().equals("")) {
            throw BasicException.errorHandling("Diret�rio Imagem de Dados n�o foi definido.", "msgErroNaoExisteDiretorioImagem", new String[] {}, log);
        }
        try {
            fos = new FileOutputStream(new File(dir.trim(), "pdump." + idArquivo + ".data.xml.zip"));
        } catch (FileNotFoundException e) {
            throw BasicException.errorHandling("Erro ao abrir arquivo de dados xml para escrita", "msgErroCriarArquivoDadosXML", e, log);
        }
        zipOutputStream = new ZipOutputStream(fos);
        zipOutputStream.setLevel(Deflater.DEFAULT_COMPRESSION);
        try {
            zipOutputStream.putNextEntry(new ZipEntry("pdump." + idArquivo + ".data.xml"));
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(zipOutputStream, ENCODING);
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao criar entrada em arquivo compactado", "msgErroCriarEntradaZipArquivoDadosXML", e, log);
        }
        XMLStreamWriter osw = null;
        try {
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(zipOutputStream, ENCODING);
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao criar entrada em arquivo compactado", "msgErroCriarEntradaZipArquivoDadosXML", e, log);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        if (dados.getTipos().size() > 0) {
            tipos = new int[dados.getTipos().size()];
            for (int c = 0; c < dados.getTipos().size(); c++) {
                String tipo = dados.getTipos().get(c);
                if (tipo.equals(LIT_INTEIRO)) {
                    tipos[c] = INTEIRO;
                } else if (tipo.equals(LIT_DECIMAL)) {
                    tipos[c] = DECIMAL;
                } else if (tipo.equals(LIT_DATA)) {
                    tipos[c] = DATA;
                } else {
                    tipos[c] = TEXTO;
                }
            }
        }
        try {
            xmlWriter.writeStartDocument(ENCODING, "1.0");
            xmlWriter.writeStartElement("coleta");
            xmlWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xmlWriter.writeAttribute("xsi:noNamespaceSchemaLocation", "http://www.pingifes.lcc.ufmg.br/dtd/pingifes_dados_seriais.xsd");
            xmlWriter.writeStartElement("id");
            xmlWriter.writeCharacters(idArquivo);
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("data");
            xmlWriter.writeCharacters(dateFormat.format(dados.getDataColeta()));
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("sql");
            xmlWriter.writeCData(dados.getSql());
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("cabecalho");
            for (int c = 0; c < dados.getCabecalho().size(); c++) {
                xmlWriter.writeStartElement("nome");
                xmlWriter.writeAttribute("tipo", dados.getTipos().get(c));
                xmlWriter.writeCharacters(dados.getCabecalho().get(c));
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("quantidade");
            xmlWriter.writeCharacters("" + dados.getQuantidade());
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("dados");
        } catch (XMLStreamException e) {
            throw BasicException.errorHandling("Erro ao escrever dados em arquivo compactado", "msgErroEscreverArquivoDadosXML", e, log);
        }
        calculadorHash = new CalculaHash();
    }

    public void escreveLinhaArquivo(DadosSeriais dados) throws BasicException {
        Date valorData = null;
        String coluna = "";
        List<String> dadosHash = new ArrayList<String>();
        for (int l = 0; l < dados.getDados().size(); l++) {
            Object[] linha = dados.getDados().get(l);
            int num = l + cont;
            String str = null;
            try {
                xmlWriter.writeStartElement("linha");
                xmlWriter.writeAttribute("num", "" + num);
                for (int c = 0; c < linha.length; c++) {
                    Object valor = linha[c];
                    int tipo = tipos[c];
                    xmlWriter.writeStartElement("col");
                    xmlWriter.writeAttribute("num", "" + c);
                    if (valor == null) {
                        xmlWriter.writeAttribute("nulo", "true");
                    } else {
                        coluna += ">";
                        switch(tipo) {
                            case TEXTO:
                                str = expurgaCaracteresInvalidos(valor.toString());
                                xmlWriter.writeCData(str);
                                dadosHash.add(str);
                                break;
                            case INTEIRO:
                                xmlWriter.writeCharacters(valor.toString());
                                dadosHash.add(valor.toString());
                                break;
                            case DATA:
                                valorData = (java.util.Date) valor;
                                String strData = dateFormat.format(valorData);
                                xmlWriter.writeCharacters(strData);
                                dadosHash.add(strData);
                                break;
                            case DECIMAL:
                                coluna += decimalFormat.format((Number) valor) + "</col>";
                                xmlWriter.writeCharacters(decimalFormat.format((Number) valor));
                                dadosHash.add(decHash.format((Number) valor));
                        }
                    }
                    xmlWriter.writeEndElement();
                }
                xmlWriter.writeEndElement();
            } catch (Exception e) {
                throw BasicException.errorHandling("Erro ao escrever dados em arquivo compactado", "msgErroEscreverArquivoDadosXML", e, log);
            }
            calculadorHash.adicionaLinha(dadosHash);
        }
        cont += dados.getDados().size();
    }

    private String expurgaCaracteresInvalidos(String str) {
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

    public void finalizaEscrita() throws BasicException {
        calculadorHash.finalizaEntradaDados();
        try {
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("hash");
            xmlWriter.writeCharacters(calculadorHash.getHash());
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
            xmlWriter.writeEndDocument();
            xmlWriter.flush();
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao escrever dados em arquivo compactado", "msgErroEscreverArquivoDadosXML", e, log);
        }
        try {
            zipOutputStream.closeEntry();
        } catch (IOException e1) {
            throw BasicException.errorHandling("Erro ao fechar entrada em arquivo de dados compactado", "msgErroFecharEntradaZipArquivoDadosXML", e1, log);
        }
        try {
            zipOutputStream.close();
        } catch (IOException e1) {
            throw BasicException.errorHandling("Erro ao fechar arquivo de dados xml de escrita", "msgErroFecharArquivoEscritaDadosXML", e1, log);
        }
    }

    public boolean existeArquivo(DadosSeriais dados, String dir) throws BasicException {
        if (dir == null) {
            dir = "";
        }
        dadosSeriais = dados;
        String idArquivo = dados.getIdColeta().toString();
        if (dados.getIdResumo() != null) {
            idArquivo += "_" + dados.getIdResumo().toString();
        }
        try {
            File file = new File(dir.trim(), "pdump." + idArquivo + ".data.xml.zip");
            return file.exists();
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao verificar existencia de arquivo de dados", "msgErroVerificarExistenciaArquivoDadosXML", e, log);
        }
    }

    public void preparaLeitura(DadosSeriais dados, String dir) throws BasicException {
        if (dir == null || dir.trim().equals("")) {
            throw BasicException.errorHandling("Diretorio Imagem de Dados nao foi definido.", "msgErroNaoExisteDiretorioImagem", new String[] {}, log);
        }
        dadosSeriais = dados;
        String idArquivo = dados.getIdColeta().toString();
        if (dados.getIdResumo() != null) {
            idArquivo += "_" + dados.getIdResumo().toString();
        }
        try {
            zip = new ZipFile(new File(dir.trim(), "pdump." + idArquivo + ".data.xml.zip"));
            java.util.Enumeration entries = zip.entries();
            ZipEntry zEntry = (ZipEntry) entries.nextElement();
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlReader = xmlInputFactory.createXMLStreamReader(new InputStreamReader(zip.getInputStream(zEntry), ENCODING));
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao abrir arquivo de dados xml de leitura", "msgErroAbrirArquivoLeituraDadosXML", e, log);
        }
        boolean temMais = true;
        cont = 0;
        try {
            while (temMais && xmlReader.hasNext()) {
                xmlReader.next();
                if (xmlReader.getEventType() == XMLStreamReader.START_ELEMENT) {
                    this.startElement(xmlReader);
                } else if (xmlReader.getEventType() == XMLStreamReader.END_ELEMENT) {
                    this.endElement(xmlReader);
                    if (xmlReader.getLocalName().equals("quantidade")) {
                        temMais = false;
                    }
                } else if (xmlReader.getEventType() == XMLStreamReader.CHARACTERS) {
                    this.characters(xmlReader.getText());
                }
            }
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao ler dados de arquivo xml", "msgErroLerArquivoDadosXML", e, log);
        }
    }

    public void leRegistrosSequencialmenteDeArquivo(long numRegistros) throws BasicException {
        leRegistrosDeArquivo(cont, numRegistros);
    }

    public void leRegistrosDeArquivo(long regInicial, long numRegistros) throws BasicException {
        this.registroInicial = regInicial;
        this.numeroRegistros = numRegistros;
        this.getDadosSeriais().getDados().clear();
        boolean temMais = true;
        try {
            while (temMais && xmlReader.hasNext()) {
                xmlReader.next();
                if (xmlReader.getEventType() == XMLStreamReader.START_ELEMENT) {
                    this.startElement(xmlReader);
                } else if (xmlReader.getEventType() == XMLStreamReader.END_ELEMENT) {
                    if (xmlReader.getLocalName().equals("dados")) {
                        temMais = false;
                    }
                    if (xmlReader.getLocalName().toLowerCase().equals("linha")) {
                        if (cont >= (registroInicial + numeroRegistros - 1)) {
                            temMais = false;
                        }
                    }
                    this.endElement(xmlReader);
                } else if (xmlReader.getEventType() == XMLStreamReader.CHARACTERS) {
                    this.characters(xmlReader.getText());
                }
            }
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao ler dados de arquivo xml", "msgErroLerArquivoDadosXML", e, log);
        }
    }

    public void finalizaLeitura() throws BasicException {
        try {
            zip.close();
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao fechar arquivo de dados xml de leitura", "msgErroFecharArquivoDadosLeituraXML", e, log);
        }
    }

    public void startElement(XMLStreamReader xmlReader) throws Exception {
        dadoLido.delete(0, dadoLido.length());
        String qName = xmlReader.getLocalName();
        if (qName.toLowerCase().equals("col")) {
            if (xmlReader.getAttributeValue(null, "nulo") == null) {
                valorCorrenteENulo = false;
            } else {
                valorCorrenteENulo = (xmlReader.getAttributeValue(null, "nulo")).equals("true");
            }
            CXMLCorrente = CXML_COL;
        } else if (qName.toLowerCase().equals("linha")) {
            indiceColuna = 0;
            if (cont >= registroInicial) {
                linha = new Object[dadosSeriais.getCabecalho().size()];
            }
            CXMLCorrente = CXML_LINHA;
        } else if (qName.toLowerCase().equals("dados")) {
            cont = 0;
            CXMLCorrente = CXML_DADOS;
        } else if (qName.toLowerCase().equals("quantidade")) {
            CXMLCorrente = CXML_QUANTIDADE;
        } else if (qName.toLowerCase().equals("cabecalho")) {
            CXMLCorrente = CXML_CABECALHO;
        } else if (qName.toLowerCase().equals("nome")) {
            dadosSeriais.getTipos().add(xmlReader.getAttributeValue(null, "tipo"));
            CXMLCorrente = CXML_NOME;
        } else if (qName.toLowerCase().equals("sql")) {
            CXMLCorrente = CXML_SQL;
        } else if (qName.toLowerCase().equals("data")) {
            CXMLCorrente = CXML_DATA;
        } else if (qName.toLowerCase().equals("id")) {
            CXMLCorrente = CXML_ID;
        } else if (qName.toLowerCase().equals("coleta")) {
            CXMLCorrente = CXML_COLETA;
            if (dadosSeriais == null) {
                dadosSeriais = new DadosSeriais();
            }
            dadosSeriais.setCabecalho(new ArrayList());
            dadosSeriais.setTipos(new ArrayList());
            dadosSeriais.setDados(new ArrayList());
        }
    }

    public void endElement(XMLStreamReader xmlReader) throws Exception {
        String qName = xmlReader.getLocalName().toLowerCase();
        CXMLCorrente = CXML_INDEFINDO;
        if (qName.equals("col")) {
            if (cont >= registroInicial && !valorCorrenteENulo) {
                linha[indiceColuna] = converteDados(dadoLido.toString(), tipos[indiceColuna]);
            }
            indiceColuna++;
        } else if (qName.equals("linha")) {
            if (cont >= registroInicial) {
                dadosSeriais.getDados().add(linha);
            }
            cont++;
        } else if (qName.equals("quantidade")) {
            dadosSeriais.setQuantidade((Long) converteDados(dadoLido.toString(), INTEIRO));
        } else if (qName.equals("cabecalho")) {
            if (dadosSeriais.getTipos().size() > 0) {
                tipos = new int[dadosSeriais.getTipos().size()];
                for (int c = 0; c < dadosSeriais.getTipos().size(); c++) {
                    String tipo = dadosSeriais.getTipos().get(c);
                    if (tipo.equals(LIT_INTEIRO)) {
                        tipos[c] = INTEIRO;
                    } else if (tipo.equals(LIT_DECIMAL)) {
                        tipos[c] = DECIMAL;
                    } else if (tipo.equals(LIT_DATA)) {
                        tipos[c] = DATA;
                    } else {
                        tipos[c] = TEXTO;
                    }
                }
            }
        } else if (qName.equals("nome")) {
            dadosSeriais.getCabecalho().add(dadoLido.toString());
        } else if (qName.equals("sql")) {
            dadosSeriais.setSql(dadoLido.toString());
        } else if (qName.equals("data")) {
            dadosSeriais.setDataColeta((Date) converteDados(dadoLido.toString(), DATA));
        }
    }

    public void characters(String texto) throws Exception {
        switch(CXMLCorrente) {
            case CXML_COL:
                if (!valorCorrenteENulo && cont >= registroInicial) {
                    dadoLido.append(texto);
                }
                break;
            case CXML_QUANTIDADE:
            case CXML_NOME:
            case CXML_SQL:
            case CXML_DATA:
                dadoLido.append(texto);
        }
    }

    private Object converteDados(String valor, int tipo) throws Exception {
        if (valor == null) {
            return null;
        }
        switch(tipo) {
            case TEXTO:
                return valor;
            case INTEIRO:
                try {
                    return new Long(valor);
                } catch (NumberFormatException e2) {
                    throw new Exception("Erro ao converter dado lido de XML para tipo Long. Valor: " + valor);
                }
            case DECIMAL:
                try {
                    return new Double((decimalFormat.parse(valor)).doubleValue());
                } catch (NumberFormatException e1) {
                    throw new Exception("Erro ao converter dado lido de XML para tipo Double. Valor: " + valor);
                }
            case DATA:
                try {
                    return dateFormat.parse(valor);
                } catch (ParseException e) {
                    throw new Exception("Erro ao converter dado lido de XML para tipo Date. Valor: " + valor);
                }
        }
        return null;
    }

    public DadosSeriais getDadosSeriais() {
        return this.dadosSeriais;
    }

    public void setDadosSeriais(DadosSeriais dadosSeriais) {
        this.dadosSeriais = dadosSeriais;
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
        return BasicException.errorHandling(rootMessage, key, args, "ApplicationResources", log);
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
     * Recupera o hash calculado para o arquivo como uma string hexadecimal
     * @return String hexadecimal com o hash
     */
    public String getHash() {
        return calculadorHash.getHash();
    }
}

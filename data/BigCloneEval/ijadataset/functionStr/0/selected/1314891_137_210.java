public class Test {    public void prepareWrite(SerializableData data, String folder) throws BasicException {
        String fileId = data.getHarvestId().toString();
        if (data.getSummaryId() != null) {
            fileId += "_" + data.getSummaryId().toString();
        }
        if (folder == null || folder.trim().equals("")) {
            throw BasicException.errorHandling("Diret�rio Imagem de Dados n�o foi definido.", "msgErroNaoExisteDiretorioImagem", StringHelper.EMPTY_STRING_VECTOR, log);
        }
        try {
            fos = new FileOutputStream(new File(folder.trim(), "pdump." + fileId + ".data.xml.zip"));
        } catch (FileNotFoundException e) {
            throw BasicException.errorHandling("Erro ao abrir file de dados xml para escrita", "msgErroCriarArquivoDadosXML", e, log);
        }
        zipOutputStream = new ZipOutputStream(fos);
        zipOutputStream.setLevel(Deflater.DEFAULT_COMPRESSION);
        try {
            zipOutputStream.putNextEntry(new ZipEntry("pdump." + fileId + ".data.xml"));
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(zipOutputStream, ENCODING);
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao criar entrada em file compactado", "msgErroCriarEntradaZipArquivoDadosXML", e, log);
        }
        XMLStreamWriter osw = null;
        try {
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(zipOutputStream, ENCODING);
        } catch (Exception e) {
            throw BasicException.errorHandling("Erro ao criar entrada em file compactado", "msgErroCriarEntradaZipArquivoDadosXML", e, log);
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
            xmlWriter.writeAttribute("xsi:noNamespaceSchemaLocation", "http://www.pcollecta.lcc.ufmg.br/dtd/pCollectaDataDump.xsd");
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
            throw BasicException.errorHandling("Erro ao escrever dados em file compactado", "msgErroEscreverArquivoDadosXML", e, log);
        }
        computeHash = new ComputeHash();
    }
}
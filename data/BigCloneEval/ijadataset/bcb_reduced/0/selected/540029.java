package org.exist.xquery.modules.compression;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.exist.xmldb.EXistResource;
import org.exist.xmldb.LocalCollection;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.FunctionCall;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.functions.xmldb.XMLDBAbstractCollectionManipulator;
import org.exist.xquery.modules.ModuleUtils;
import org.exist.xquery.value.AnyURIValue;
import org.exist.xquery.value.Base64BinaryValueType;
import org.exist.xquery.value.BinaryValue;
import org.exist.xquery.value.BinaryValueFromInputStream;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.FunctionReference;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.StringValue;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

/**
 * @author Adam Retter <adam@exist-db.org>
 * @version 1.0
 */
public abstract class AbstractExtractFunction extends BasicFunction {

    private FunctionCall entryFilterFunction = null;

    protected Sequence filterParam = null;

    private FunctionCall entryDataFunction = null;

    protected Sequence storeParam = null;

    private Sequence contextSequence;

    public AbstractExtractFunction(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
        this.contextSequence = contextSequence;
        if (args[0].isEmpty()) return Sequence.EMPTY_SEQUENCE;
        if (!(args[1].itemAt(0) instanceof FunctionReference)) throw new XPathException("No entry-filter function provided.");
        FunctionReference entryFilterFunctionRef = (FunctionReference) args[1].itemAt(0);
        entryFilterFunction = entryFilterFunctionRef.getFunctionCall();
        FunctionSignature entryFilterFunctionSig = entryFilterFunction.getSignature();
        if (entryFilterFunctionSig.getArgumentCount() < 3) throw new XPathException("entry-filter function must take at least 3 arguments.");
        filterParam = args[2];
        if (!(args[3].itemAt(0) instanceof FunctionReference)) throw new XPathException("No entry-data function provided.");
        FunctionReference entryDataFunctionRef = (FunctionReference) args[3].itemAt(0);
        entryDataFunction = entryDataFunctionRef.getFunctionCall();
        FunctionSignature entryDataFunctionSig = entryDataFunction.getSignature();
        if (entryDataFunctionSig.getArgumentCount() < 3) throw new XPathException("entry-data function must take at least 3 arguments");
        storeParam = args[4];
        BinaryValue compressedData = ((BinaryValue) args[0].itemAt(0));
        try {
            return processCompressedData(compressedData);
        } catch (XMLDBException e) {
            throw new XPathException(e);
        }
    }

    /**
     * Processes a compressed archive
     *
     * @param compressedData the compressed data to extract
     * @return Sequence of results
     */
    protected abstract Sequence processCompressedData(BinaryValue compressedData) throws XPathException, XMLDBException;

    /**
     * Processes a compressed entry from an archive
     *
     * @param name The name of the entry
     * @param isDirectory true if the entry is a directory, false otherwise
     * @param is an InputStream for reading the uncompressed data of the entry
     * @param filterParam is an additional param for entry filtering function  
     * @param storeParam is an additional param for entry storing function
     * @throws XMLDBException 
     */
    protected Sequence processCompressedEntry(String name, boolean isDirectory, InputStream is, Sequence filterParam, Sequence storeParam) throws IOException, XPathException, XMLDBException {
        String dataType = isDirectory ? "folder" : "resource";
        Sequence filterParams[] = new Sequence[3];
        filterParams[0] = new StringValue(name);
        filterParams[1] = new StringValue(dataType);
        filterParams[2] = filterParam;
        Sequence entryFilterFunctionResult = entryFilterFunction.evalFunction(contextSequence, null, filterParams);
        if (BooleanValue.FALSE == entryFilterFunctionResult.itemAt(0)) {
            return Sequence.EMPTY_SEQUENCE;
        } else {
            Sequence entryDataFunctionResult;
            Sequence uncompressedData = Sequence.EMPTY_SEQUENCE;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int read = -1;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            byte[] entryData = baos.toByteArray();
            if (entryDataFunction.getSignature().getArgumentCount() == 3) {
                Sequence dataParams[] = new Sequence[3];
                System.arraycopy(filterParams, 0, dataParams, 0, 2);
                dataParams[2] = storeParam;
                entryDataFunctionResult = entryDataFunction.evalFunction(contextSequence, null, dataParams);
                String path = entryDataFunctionResult.itemAt(0).getStringValue();
                Collection root = new LocalCollection(context.getUser(), context.getBroker().getBrokerPool(), new AnyURIValue("/db").toXmldbURI(), context.getAccessContext());
                if (isDirectory) {
                    XMLDBAbstractCollectionManipulator.createCollection(root, path);
                } else {
                    Resource resource;
                    File file = new File(path);
                    name = file.getName();
                    path = file.getParent();
                    Collection target = (path == null) ? root : XMLDBAbstractCollectionManipulator.createCollection(root, path);
                    MimeType mime = MimeTable.getInstance().getContentTypeFor(name);
                    try {
                        NodeValue content = ModuleUtils.streamToXML(context, new ByteArrayInputStream(baos.toByteArray()));
                        resource = target.createResource(name, "XMLResource");
                        ContentHandler handler = ((XMLResource) resource).setContentAsSAX();
                        handler.startDocument();
                        content.toSAX(context.getBroker(), handler, null);
                        handler.endDocument();
                    } catch (SAXException e) {
                        resource = target.createResource(name, "BinaryResource");
                        resource.setContent(baos.toByteArray());
                    }
                    if (resource != null) {
                        if (mime != null) {
                            ((EXistResource) resource).setMimeType(mime.getName());
                        }
                        target.storeResource(resource);
                    }
                }
            } else {
                try {
                    uncompressedData = ModuleUtils.streamToXML(context, new ByteArrayInputStream(entryData));
                } catch (SAXException saxe) {
                    if (entryData.length > 0) uncompressedData = BinaryValueFromInputStream.getInstance(context, new Base64BinaryValueType(), new ByteArrayInputStream(entryData));
                }
                Sequence dataParams[] = new Sequence[4];
                System.arraycopy(filterParams, 0, dataParams, 0, 2);
                dataParams[2] = uncompressedData;
                dataParams[3] = storeParam;
                entryDataFunctionResult = entryDataFunction.evalFunction(contextSequence, null, dataParams);
            }
            return entryDataFunctionResult;
        }
    }
}

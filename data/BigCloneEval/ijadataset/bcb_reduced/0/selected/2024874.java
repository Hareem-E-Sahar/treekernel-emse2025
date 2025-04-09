package org.jgap.xml;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import junitx.util.PrivateAccessor;
import org.jgap.*;
import org.w3c.dom.*;

/**
 * The XMLManager performs marshalling of genetic entity instances
 * (such as Chromosomes and Genotypes) to XML representations of those
 * entities, as well as unmarshalling. All of the methods in this class are
 * static, so no construction is required (or allowed).
 *
 * @author Neil Rotstan
 * @author Klaus Meffert
 * @since 1.0
 */
public class XMLManager {

    /** String containing the CVS revision. Read out via reflection!*/
    private static final String CVS_REVISION = "$Revision: 1.21 $";

    /**
   * Constant representing the name of the genotype XML element tag.
   */
    private static final String GENOTYPE_TAG = "genotype";

    /**
   * Constant representing the name of the chromosome XML element tag.
   */
    private static final String CHROMOSOME_TAG = "chromosome";

    /**
   * Constant representing the name of the gene XML element tag.
   */
    private static final String GENES_TAG = "genes";

    /**
   * Constant representing the name of the gene XML element tag.
   */
    private static final String GENE_TAG = "gene";

    private static final String ALLELE_TAG = "allele";

    /**
   * Constant representing the name of the size XML attribute that is
   * added to genotype and chromosome elements to describe their size.
   */
    private static final String SIZE_ATTRIBUTE = "size";

    /**
   * Constant representing the fully-qualified name of the concrete
   * Gene class that was marshalled.
   */
    private static final String CLASS_ATTRIBUTE = "class";

    /**
   * Shared DocumentBuilder, which is used to create new DOM Document
   * instances.
   */
    private static final DocumentBuilder m_documentCreator;

    /**
   * Shared lock object used for synchronization purposes.
   */
    private static final Object m_lock = new Object();

    /**
   * @author Neil Rotstan
   * @since 1.0
   */
    static {
        try {
            m_documentCreator = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException parserError) {
            throw new RuntimeException("XMLManager: Unable to setup DocumentBuilder: " + parserError.getMessage());
        }
    }

    /**
   * Private constructor. All methods in this class are static, so no
   * construction is allowed.
   */
    private XMLManager() {
    }

    /**
   * Marshall a Chromosome instance to an XML Document representation,
   * including its contained Gene instances.
   *
   * @param a_subject the chromosome to represent as an XML document
   * @return a Document object representing the given Chromosome
   *
   * @author Neil Rotstan
   * @since 1.0
   * @deprecated use XMLDocumentBuilder instead
   */
    public static Document representChromosomeAsDocument(final IChromosome a_subject) {
        Document chromosomeDocument;
        synchronized (m_lock) {
            chromosomeDocument = m_documentCreator.newDocument();
        }
        Element chromosomeElement = representChromosomeAsElement(a_subject, chromosomeDocument);
        chromosomeDocument.appendChild(chromosomeElement);
        return chromosomeDocument;
    }

    /**
   * Marshall a Genotype to an XML Document representation, including its
   * population of Chromosome instances.
   *
   * @param a_subject the genotype to represent as an XML document
   * @return a Document object representing the given Genotype
   *
   * @author Neil Rotstan
   * @since 1.0
   * @deprecated use XMLDocumentBuilder instead
   */
    public static Document representGenotypeAsDocument(final Genotype a_subject) {
        Document genotypeDocument;
        synchronized (m_lock) {
            genotypeDocument = m_documentCreator.newDocument();
        }
        Element genotypeElement = representGenotypeAsElement(a_subject, genotypeDocument);
        genotypeDocument.appendChild(genotypeElement);
        return genotypeDocument;
    }

    /**
   * Marshall an array of Genes to an XML Element representation.
   *
   * @param a_geneValues the genes to represent as an XML element
   * @param a_xmlDocument a Document instance that will be used to create
   * the Element instance. Note that the element will NOT be added to the
   * document by this method
   * @return an Element object representing the given genes
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 1.0
   * @deprecated use XMLDocumentBuilder instead
   */
    public static Element representGenesAsElement(final Gene[] a_geneValues, final Document a_xmlDocument) {
        Element genesElement = a_xmlDocument.createElement(GENES_TAG);
        Element geneElement;
        for (int i = 0; i < a_geneValues.length; i++) {
            geneElement = a_xmlDocument.createElement(GENE_TAG);
            geneElement.setAttribute(CLASS_ATTRIBUTE, a_geneValues[i].getClass().getName());
            Element alleleRepresentation = representAlleleAsElement(a_geneValues[i], a_xmlDocument);
            geneElement.appendChild(alleleRepresentation);
            genesElement.appendChild(geneElement);
        }
        return genesElement;
    }

    /**
   *
   * @param a_gene Gene
   * @param a_xmlDocument Document
   * @return Element
   *
   * @author Klaus Meffert
   * @since 2.0
   */
    private static Element representAlleleAsElement(final Gene a_gene, final Document a_xmlDocument) {
        Element alleleElement = a_xmlDocument.createElement(ALLELE_TAG);
        alleleElement.setAttribute("class", a_gene.getClass().getName());
        alleleElement.setAttribute("value", a_gene.getPersistentRepresentation());
        return alleleElement;
    }

    /**
   * Marshall a Chromosome instance to an XML Element representation,
   * including its contained Genes as sub-elements. This may be useful in
   * scenarios where representation as an entire Document is undesirable,
   * such as when the representation of this Chromosome is to be combined
   * with other elements in a single Document.
   *
   * @param a_subject the chromosome to represent as an XML element
   * @param a_xmlDocument a Document instance that will be used to create
   * the Element instance. Note that the element will NOT be added to the
   * document by this method
   * @return an Element object representing the given Chromosome
   *
   * @author Neil Rotstan
   * @since 1.0
   * @deprecated use XMLDocumentBuilder instead
   */
    public static Element representChromosomeAsElement(final IChromosome a_subject, final Document a_xmlDocument) {
        Element chromosomeElement = a_xmlDocument.createElement(CHROMOSOME_TAG);
        chromosomeElement.setAttribute(SIZE_ATTRIBUTE, Integer.toString(a_subject.size()));
        Element genesElement = representGenesAsElement(a_subject.getGenes(), a_xmlDocument);
        chromosomeElement.appendChild(genesElement);
        return chromosomeElement;
    }

    /**
   * Marshall a Genotype instance into an XML Element representation,
   * including its population of Chromosome instances as sub-elements.
   * This may be useful in scenarios where representation as an
   * entire Document is undesirable, such as when the representation
   * of this Genotype is to be combined with other elements in a
   * single Document.
   *
   * @param a_subject the genotype to represent as an XML element
   * @param a_xmlDocument a Document instance that will be used to create
   * the Element instance. Note that the element will NOT be added to the
   * document by this method
   * @return an Element object representing the given Genotype
   *
   * @author Neil Rotstan
   * @since 1.0
   * @deprecated use XMLDocumentBuilder instead
   */
    public static Element representGenotypeAsElement(final Genotype a_subject, final Document a_xmlDocument) {
        Population population = a_subject.getPopulation();
        Element genotypeTag = a_xmlDocument.createElement(GENOTYPE_TAG);
        genotypeTag.setAttribute(SIZE_ATTRIBUTE, Integer.toString(population.size()));
        for (int i = 0; i < population.size(); i++) {
            Element chromosomeElement = representChromosomeAsElement(population.getChromosome(i), a_xmlDocument);
            genotypeTag.appendChild(chromosomeElement);
        }
        return genotypeTag;
    }

    /**
   * Unmarshall a Chromosome instance from a given XML Element
   * representation.
   *
   * @param a_activeConfiguration current Configuration object
   * @param a_xmlElement the XML Element representation of the Chromosome
   * @return a new Chromosome instance setup with the data from the XML Element
   * representation
   *
   * @throws ImproperXMLException if the given Element is improperly
   * structured or missing data
   * @throws UnsupportedRepresentationException if the actively configured
   * Gene implementation does not support the string representation of the
   * alleles used in the given XML document
   * @throws GeneCreationException if there is a problem creating or populating
   * a Gene instance
   *
   * @author Neil Rotstan
   * @since 1.0
   */
    public static Gene[] getGenesFromElement(Configuration a_activeConfiguration, Element a_xmlElement) throws ImproperXMLException, UnsupportedRepresentationException, GeneCreationException {
        if (a_xmlElement == null || !(a_xmlElement.getTagName().equals(GENES_TAG))) {
            throw new ImproperXMLException("Unable to build Chromosome instance from XML Element: " + "given Element is not a 'genes' element.");
        }
        List genes = Collections.synchronizedList(new ArrayList());
        NodeList geneElements = a_xmlElement.getElementsByTagName(GENE_TAG);
        if (geneElements == null) {
            throw new ImproperXMLException("Unable to build Gene instances from XML Element: " + "'" + GENE_TAG + "'" + " sub-elements not found.");
        }
        int numberOfGeneNodes = geneElements.getLength();
        for (int i = 0; i < numberOfGeneNodes; i++) {
            Element thisGeneElement = (Element) geneElements.item(i);
            thisGeneElement.normalize();
            String geneClassName = thisGeneElement.getAttribute(CLASS_ATTRIBUTE);
            Gene thisGeneObject;
            Class geneClass = null;
            try {
                geneClass = Class.forName(geneClassName);
                try {
                    Constructor constr = geneClass.getConstructor(new Class[] { Configuration.class });
                    thisGeneObject = (Gene) constr.newInstance(new Object[] { a_activeConfiguration });
                } catch (NoSuchMethodException nsme) {
                    Constructor constr = geneClass.getConstructor(new Class[] {});
                    thisGeneObject = (Gene) constr.newInstance(new Object[] {});
                    thisGeneObject = (Gene) PrivateAccessor.invoke(thisGeneObject, "newGeneInternal", new Class[] {}, new Object[] {});
                }
            } catch (Throwable e) {
                throw new GeneCreationException(geneClass, e);
            }
            NodeList children = thisGeneElement.getChildNodes();
            int childrenSize = children.getLength();
            String alleleRepresentation = null;
            for (int j = 0; j < childrenSize; j++) {
                Element alleleElem = (Element) children.item(j);
                if (alleleElem.getTagName().equals(ALLELE_TAG)) {
                    alleleRepresentation = alleleElem.getAttribute("value");
                }
                if (children.item(j).getNodeType() == Node.TEXT_NODE) {
                    alleleRepresentation = children.item(j).getNodeValue();
                    break;
                }
            }
            if (alleleRepresentation == null) {
                throw new ImproperXMLException("Unable to build Gene instance from XML Element: " + "value (allele) is missing representation.");
            }
            try {
                thisGeneObject.setValueFromPersistentRepresentation(alleleRepresentation);
            } catch (UnsupportedOperationException e) {
                throw new GeneCreationException("Unable to build Gene because it does not support the " + "setValueFromPersistentRepresentation() method.");
            }
            genes.add(thisGeneObject);
        }
        return (Gene[]) genes.toArray(new Gene[genes.size()]);
    }

    /**
   * Unmarshall a Chromosome instance from a given XML Element
   * representation.
   *
   * @param a_activeConfiguration the current active Configuration object
   * that is to be used during construction of the Chromosome
   * @param a_xmlElement the XML Element representation of the Chromosome
   * @return a new Chromosome instance setup with the data from the XML
   * Element representation
   *
   * @throws ImproperXMLException if the given Element is improperly
   * structured or missing data
   * @throws InvalidConfigurationException if the given Configuration is in
   * an inconsistent state
   * @throws UnsupportedRepresentationException if the actively configured
   * Gene implementation does not support the string representation of the
   * alleles used in the given XML document
   * @throws GeneCreationException if there is a problem creating or populating
   * a Gene instance
   *
   * @author Neil Rotstan
   * @since 1.0
   */
    public static Chromosome getChromosomeFromElement(Configuration a_activeConfiguration, Element a_xmlElement) throws ImproperXMLException, InvalidConfigurationException, UnsupportedRepresentationException, GeneCreationException {
        if (a_xmlElement == null || !(a_xmlElement.getTagName().equals(CHROMOSOME_TAG))) {
            throw new ImproperXMLException("Unable to build Chromosome instance from XML Element: " + "given Element is not a 'chromosome' element.");
        }
        Element genesElement = (Element) a_xmlElement.getElementsByTagName(GENES_TAG).item(0);
        if (genesElement == null) {
            throw new ImproperXMLException("Unable to build Chromosome instance from XML Element: " + "'genes' sub-element not found.");
        }
        Gene[] geneAlleles = getGenesFromElement(a_activeConfiguration, genesElement);
        return new Chromosome(a_activeConfiguration, geneAlleles);
    }

    /**
   * Unmarshall a Genotype instance from a given XML Element representation.
   * Its population of Chromosomes will be unmarshalled from the Chromosome
   * sub-elements.
   *
   * @param a_activeConfiguration the current active Configuration object
   * that is to be used during construction of the Genotype and Chromosome
   * instances
   * @param a_xmlElement the XML Element representation of the Genotype
   * @return a new Genotype instance, complete with a population of Chromosomes,
   * setup with the data from the XML Element representation
   *
   * @throws ImproperXMLException if the given Element is improperly structured
   * or missing data
   * @throws InvalidConfigurationException if the given Configuration is in an
   * inconsistent state
   * @throws UnsupportedRepresentationException if the actively configured
   * Gene implementation does not support the string representation of the
   * alleles used in the given XML document
   * @throws GeneCreationException if there is a problem creating or populating
   * a Gene instance
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 1.0
   */
    public static Genotype getGenotypeFromElement(Configuration a_activeConfiguration, Element a_xmlElement) throws ImproperXMLException, InvalidConfigurationException, UnsupportedRepresentationException, GeneCreationException {
        if (a_xmlElement == null || !(a_xmlElement.getTagName().equals(GENOTYPE_TAG))) {
            throw new ImproperXMLException("Unable to build Genotype instance from XML Element: " + "given Element is not a 'genotype' element.");
        }
        NodeList chromosomes = a_xmlElement.getElementsByTagName(CHROMOSOME_TAG);
        int numChromosomes = chromosomes.getLength();
        Population population = new Population(a_activeConfiguration, numChromosomes);
        for (int i = 0; i < numChromosomes; i++) {
            population.addChromosome(getChromosomeFromElement(a_activeConfiguration, (Element) chromosomes.item(i)));
        }
        return new Genotype(a_activeConfiguration, population);
    }

    /**
   * Unmarshall a Genotype instance from a given XML Document representation.
   * Its population of Chromosomes will be unmarshalled from the Chromosome
   * sub-elements.
   *
   * @param a_activeConfiguration the current active Configuration object that
   * is to be used during construction of the Genotype and Chromosome instances
   * @param a_xmlDocument the XML Document representation of the Genotype
   *
   * @return A new Genotype instance, complete with a population of Chromosomes,
   * setup with the data from the XML Document representation
   *
   * @throws ImproperXMLException if the given Document is improperly structured
   * or missing data
   * @throws InvalidConfigurationException if the given Configuration is in an
   * inconsistent state
   * @throws UnsupportedRepresentationException if the actively configured Gene
   * implementation does not support the string representation of the alleles
   * used in the given XML document
   * @throws GeneCreationException if there is a problem creating or populating
   * a Gene instance
   *
   * @author Neil Rotstan
   * @since 1.0
   */
    public static Genotype getGenotypeFromDocument(Configuration a_activeConfiguration, Document a_xmlDocument) throws ImproperXMLException, InvalidConfigurationException, UnsupportedRepresentationException, GeneCreationException {
        Element rootElement = a_xmlDocument.getDocumentElement();
        if (rootElement == null || !(rootElement.getTagName().equals(GENOTYPE_TAG))) {
            throw new ImproperXMLException("Unable to build Genotype from XML Document: " + "'genotype' element must be at root of document.");
        }
        return getGenotypeFromElement(a_activeConfiguration, rootElement);
    }

    /**
   * Unmarshall a Chromosome instance from a given XML Document
   * representation. Its genes will be unmarshalled from the gene
   * sub-elements.
   *
   * @param a_activeConfiguration the current active Configuration object that
   * is to be used during construction of the Chromosome instances
   * @param a_xmlDocument the XML Document representation of the Chromosome
   *
   * @return a new Chromosome instance setup with the data from the XML Document
   * representation
   *
   * @throws ImproperXMLException if the given Document is improperly structured
   * or missing data
   * @throws InvalidConfigurationException if the given Configuration is in an
   * inconsistent state
   * @throws UnsupportedRepresentationException if the actively configured Gene
   * implementation does not support the string representation of the alleles
   * used in the given XML document
   * @throws GeneCreationException if there is a problem creating or populating
   * a Gene instance
   *
   * @author Neil Rotstan
   * @since 1.0
   */
    public static Chromosome getChromosomeFromDocument(Configuration a_activeConfiguration, Document a_xmlDocument) throws ImproperXMLException, InvalidConfigurationException, UnsupportedRepresentationException, GeneCreationException {
        Element rootElement = a_xmlDocument.getDocumentElement();
        if (rootElement == null || !(rootElement.getTagName().equals(CHROMOSOME_TAG))) {
            throw new ImproperXMLException("Unable to build Chromosome instance from XML Document: " + "'chromosome' element must be at root of Document.");
        }
        return getChromosomeFromElement(a_activeConfiguration, rootElement);
    }

    /**
   * Reads in an XML file and returns a Document object.
   *
   * @param file the file to be read in
   * @throws IOException
   * @throws SAXException
   * @return Document
   *
   * @author Klaus Meffert
   * @since 2.0
   */
    public static Document readFile(File file) throws IOException, org.xml.sax.SAXException {
        return m_documentCreator.parse(file);
    }

    /**
   * Writes an XML file from a Document object.
   *
   * @param doc the Document object to be written to file
   * @param file the file to be written
   * @throws IOException
   *
   * @author Klaus Meffert
   * @since 2.0
   */
    public static void writeFile(Document doc, File file) throws IOException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tFactory.newTransformer();
        } catch (TransformerConfigurationException tex) {
            throw new IOException(tex.getMessage());
        }
        DOMSource source = new DOMSource(doc);
        FileOutputStream fos = new FileOutputStream(file);
        StreamResult result = new StreamResult(fos);
        try {
            transformer.transform(source, result);
            fos.close();
        } catch (TransformerException tex) {
            throw new IOException(tex.getMessage());
        }
    }
}

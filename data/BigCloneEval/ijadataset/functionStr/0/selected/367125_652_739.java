public class Test {    @Override
    public void start(ParserConnector connector, String source_dir) throws Throwable {
        BufferedReader reader = null;
        session = connector.getStatelessSession();
        session.beginTransaction();
        File[] files = new File(source_dir).listFiles(this);
        for (File file : files) {
            file_length += file.length();
        }
        writeInit();
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "dbxref.txt")));
        writeDbxref(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "db.txt")));
        writeDb(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "term.txt")));
        writeTerm(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "term2term.txt")));
        writeTerm2Term(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "relation_properties.txt")));
        writeRelationProperties(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "relation_composition.txt")));
        writeRelationComposition(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "term_definition.txt")));
        writeTermDefinition(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "term_synonym.txt")));
        writeTermSynonym(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "term_dbxref.txt")));
        writeTermDbxref(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "term_subset.txt")));
        writeTermSubset(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "term2term_metadata.txt")));
        writeTerm2TermMetadata(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "species.txt")));
        writeSpecies(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "gene_product.txt")));
        writeGeneProduct(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "gene_product_synonym.txt")));
        writeGeneProductSynonym(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "association.txt")));
        writeAssociation(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "association_qualifier.txt")));
        writeAssociationQualifier(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "association_species_qualifier.txt")));
        writeAssociationSpeciesQualifier(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "evidence.txt")));
        writeEvidence(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "evidence_dbxref.txt")));
        writeEvidenceDbxref(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "graph_path.txt")));
        writeGraphPath(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "gene_product_count.txt")));
        writeGeneProductCount(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "homolset.txt")));
        writeHomolset(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "gene_product_homolset.txt")));
        writeGeneProductHomolset(reader);
        session.getTransaction().commit();
        reader = new BufferedReader(new FileReader(new File(source_dir + File.separator + "source_audit.txt")));
        writeSourceAudit(reader);
        session.getTransaction().commit();
        if (session.getTransaction().isActive()) {
            session.getTransaction().commit();
        }
        session.close();
    }
}
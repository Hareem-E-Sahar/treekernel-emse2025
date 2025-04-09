package br.ucam.kuabaSubsystem.abstractTestCase;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.URI;
import br.ucam.kuabaSubsystem.kuabaModel.*;
import br.ucam.kuabaSubsystem.kuabaModel.impl.MyFactory;
import br.ucam.kuabaSubsystem.util.FileUtil;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.repository.impl.LocalFolderRepository;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLNames;
import edu.stanford.smi.protegex.owl.swrl.model.factory.SWRLJavaFactory;
import junit.framework.TestCase;

public class AbstractKuabaTestCase extends TestCase {

    public static String TEST_BASE_PACKAGE = "test/br/ucam/kuabaSubsystem/testBase/";

    /**
	 * factory: the factory object to instantiate the Kuaba model classes.  
	 */
    protected MyFactory factory;

    /**
	 * lr: the Local Repository that holds the KuabaOntology.owl. This attribute
	 * indicates the local path of the Kuaba Ontology file that is necessary to
	 * resolve import clauses on the header.txt file.
	 */
    protected LocalFolderRepository lr;

    /**
	 * br.ucam.kuabaSubsystem.testBase: this is the file that hold all the owl individuals
	 * specified on the files on the "Test/br.ucam.kuabaSubsystem.fixtures" path.
	 * This File represents the test base. It is cleared and filled before the 
	 * execution of any test method. This way, the execution of a test do not
	 * affect another test execution. 
	 */
    protected File testKuabaKnowlegeBase;

    /**
	 * br.ucam.kuabaSubsystem.fixtures: this attribute is the array of all br.ucam.kuabaSubsystem.fixtures files contained in
	 * the "Text/br.ucam.kuabaSubsystem.fixtures/" directory.  
	 */
    protected File[] fixtures;

    /**
	 * model: this represents the OWL model of a given OWL ontology.
	 * The JenaOwlModel class is part of Prot�g� framework. 
	 */
    protected JenaOWLModel model;

    /**
	 * the setUp() method is executed before any test method execution.
	 */
    @Override
    protected void setUp() throws Exception {
        File fixturesDirectory = new File("test/br/ucam/kuabaSubsystem/fixtures/");
        this.fixtures = fixturesDirectory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) return false;
                return true;
            }
        });
        testKuabaKnowlegeBase = new File("test/br/ucam/kuabaSubsystem/testBase/testKuabaKnowlegeBase.xml");
        FileUtil.clearFile(testKuabaKnowlegeBase);
        FileUtil.copyFile(new File("test/br/ucam/kuabaSubsystem/fixtures/headers/header.txt"), this.testKuabaKnowlegeBase);
        for (File fixture : this.fixtures) {
            FileUtil.copyFile(fixture, this.testKuabaKnowlegeBase);
        }
        FileUtil.copyFile(new File("test/br/ucam/kuabaSubsystem/fixtures/headers/EOF.txt"), this.testKuabaKnowlegeBase);
        File kuabaRepository = new File("kuabaOntology/");
        System.out.println("Path: " + kuabaRepository.getAbsolutePath());
        assert kuabaRepository.exists();
        this.lr = new LocalFolderRepository(kuabaRepository, true);
        System.out.println("lista vazia? " + lr.getOntologies().isEmpty());
        System.out.println("Cont�m KuabaOntology " + lr.contains(URI.create("http://www.tecweb.inf.puc-rio.br/DesignRationale/KuabaOntology.owl")));
        model = ProtegeOWL.createJenaOWLModel();
        model.getRepositoryManager().addProjectRepository(this.lr);
        System.out.println("existe: " + this.testKuabaKnowlegeBase.exists());
        model.load(new FileInputStream(testKuabaKnowlegeBase), "oi");
        this.factory = new MyFactory(model);
    }

    /**
	 * Executed after a test method execution.
	 */
    @Override
    protected void tearDown() throws Exception {
        this.model.dispose();
    }

    public void test() {
    }
}

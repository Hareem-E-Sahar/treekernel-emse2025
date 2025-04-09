package hephaestus.model.transformer;

import hephaestus.model.CardinalityConstants;
import hephaestus.model.TemplateManager;
import hephaestus.model.metamodel.Classe;
import hephaestus.model.metamodel.Method;
import hephaestus.model.metamodel.Package;
import hephaestus.model.metamodel.TypeMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.sun.org.apache.xpath.internal.XPathAPI;

public class CodeTransformer extends Transformer {

    @SuppressWarnings("unused")
    private Logger logger = Logger.getLogger(CodeTransformer.class);

    private TemplateManager templateManager;

    private String appPath;

    private static Properties structure = new Properties();

    private static final String STRUCTURE_PATH = "config/structure.properties";

    private String transformationType;

    public CodeTransformer(Map<String, String> initParams) {
        super(initParams);
    }

    @Override
    public void initialize(Map initParams) {
        try {
            structure.load(new FileInputStream(STRUCTURE_PATH));
            appPath = (String) initParams.get("appPath") + "/";
            transformationType = (String) initParams.get("transformationType");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void transform() throws Exception {
        if (model == null) {
            throw new Exception("Modelo inexistente.");
        }
        Collection<Package> packs = model.getPackages();
        for (Package pack : packs) {
            Collection<Classe> classes = pack.getClasses();
            for (Classe c : classes) {
                String stereotype = c.getStereotype();
                Template t = getTemplate(stereotype);
                Velocity.init();
                VelocityContext ctx = new VelocityContext();
                this.doMapTypes(c);
                ctx.put("packageName", pack.getName());
                ctx.put("classe", c);
                ctx.put("projectName", "test");
                ctx.put("card", new CardinalityConstants());
                ctx.put("classList", classes);
                ctx.put("atrList", c.getAttributes());
                ctx.put("methList", c.getMethods());
                ctx.put("relList", c.getRelationships());
                FileWriter f = createDirAndFile(appPath + "src/", pack, c);
                t.merge(ctx, f);
                f.close();
            }
        }
        resolveDependencies();
    }

    private void doMapTypes(Classe c) throws IOException, FileNotFoundException {
        Properties p = new Properties();
        p.load(new FileInputStream("templates/" + transformationType + "/type-mapping.properties"));
        TypeMapper mapper = new TypeMapper(p);
        mapper.transformTypes(c.getAttributes());
        mapper.transformTypes(c.getMethods());
        if (c.getMethods() != null) {
            for (Method m : (Collection<Method>) c.getMethods()) {
                mapper.transformTypes(m.getParameters());
            }
        }
    }

    /**
	 * 
	 * 
	 */
    private void resolveDependencies() {
        DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = b.newDocumentBuilder();
            Document conf = builder.parse(structure.getProperty("dependency_conf"));
            NodeList nodeLang = conf.getElementsByTagName("dependency");
            int i = 0;
            while (nodeLang.item(i) != null) {
                Node n = nodeLang.item(i);
                if (n.getParentNode().getAttributes().getNamedItem("name").getTextContent().equalsIgnoreCase(transformationType)) {
                    String path = null;
                    Node nodePath = n.getAttributes().getNamedItem("path");
                    Node nodePathList = n.getAttributes().getNamedItem("pathlist");
                    if (nodePath != null) {
                        path = n.getAttributes().getNamedItem("path").getTextContent();
                        String outPath = n.getAttributes().getNamedItem("outpath").getTextContent();
                        String fileName = n.getAttributes().getNamedItem("fileName").getTextContent();
                        copyFile(path, appPath + outPath, fileName);
                    } else if (nodePathList != null) {
                        String outPath = n.getAttributes().getNamedItem("outpath").getTextContent();
                        path = nodePathList.getTextContent();
                        copyDirectory(new File(path), new File(appPath + outPath));
                    }
                }
                i++;
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyDirectory(File srcPath, File dstPath) throws IOException {
        if (srcPath.isDirectory()) {
            if (!dstPath.exists()) {
                dstPath.mkdir();
            }
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
            }
        } else {
            if (!srcPath.exists()) {
                System.out.println("File or directory does not exist.");
                System.exit(0);
            } else {
                InputStream in = new FileInputStream(srcPath);
                OutputStream out = new FileOutputStream(dstPath);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        }
        System.out.println("Directory copied.");
    }

    protected void projectBinding() {
    }

    /**
	 * Copia arquivos fisicamente
	 * 
	 * @param inFile
	 *            Caminho completo do arquivo de origem
	 * @param outFile
	 *            Caminho completo do arquivo de destino
	 * @param fileName
	 * @return true se a c�pia do arquivo for realizada com sucesso
	 */
    public boolean copyFile(String inFile, String outFile, String fileName) {
        InputStream is = null;
        OutputStream os = null;
        byte[] buffer;
        boolean success = true;
        try {
            createDir(outFile);
            is = new FileInputStream(inFile + "/" + fileName);
            os = new FileOutputStream(outFile + "/" + fileName);
            buffer = new byte[is.available()];
            is.read(buffer);
            os.write(buffer);
        } catch (IOException e) {
            success = false;
        } catch (OutOfMemoryError e) {
            success = false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
            }
        }
        return success;
    }

    private FileWriter createDirAndFile(String appPath, Package pack, Classe c) throws IOException {
        File dir = new File(appPath + pack.getName().replaceAll("[.]", "/"));
        dir.mkdirs();
        FileWriter f = new FileWriter(appPath + pack.getName().replaceAll("[.]", "/") + "/" + c.getName() + model.getType());
        return f;
    }

    @SuppressWarnings("unused")
    private void createDir(String path) throws IOException {
        File dir = new File(path);
        dir.mkdirs();
    }

    private String createDir(String appPath, Package pack) throws IOException {
        File dir = new File(appPath + pack.getName().replaceAll("[.]", "/"));
        dir.mkdirs();
        return appPath + pack.getName().replaceAll("[.]", "/");
    }

    /**
	 * Recupera um template velocity referente ao esteri�tipo
	 * 
	 * @param stereotype
	 * @return
	 * @throws Exception
	 */
    private Template getTemplate(String stereotype) throws Exception {
        DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = b.newDocumentBuilder();
        XPathAPI xpathSelector = new XPathAPI();
        Document conf = builder.parse(structure.getProperty("template_conf"));
        NodeList nodeLang = conf.getElementsByTagName("template");
        int i = 0;
        while (nodeLang.item(i) != null) {
            Node n = nodeLang.item(i);
            if (n.getParentNode().getAttributes().getNamedItem("name").getTextContent().equalsIgnoreCase(transformationType)) {
                if (n.getAttributes().getNamedItem("source-stereotype").getTextContent().equalsIgnoreCase(stereotype) && n.getAttributes().getNamedItem("unique").getTextContent().equalsIgnoreCase("false")) {
                    String t = n.getAttributes().getNamedItem("path").getTextContent();
                    return Velocity.getTemplate(mountTemplatePath(t));
                }
            }
            i++;
        }
        throw new Exception("N�o existe template mapeado para o esteri�tipo : " + stereotype);
    }

    /**
	 * monta o path de um template
	 * 
	 * @param t
	 * @return
	 */
    private String mountTemplatePath(String t) {
        String templatePath = structure.getProperty("template_root") + "/" + transformationType + "/" + t;
        return templatePath;
    }

    /**
	 * @return Retorna o templateManager.
	 */
    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    /**
	 * @param templateManager
	 *            O templateManager a ser definido.
	 */
    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }
}

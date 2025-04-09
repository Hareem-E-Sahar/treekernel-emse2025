package skellib.model.kernel;

import java.io.FileWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import skellib.core.Source;
import skellib.core.Trainer;
import skellib.data.KernelMatrix;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderImpl;

public class ClusterKernelMatrixModel extends KernelMatrixModel {

    public ClusterKernelMatrixModel(KernelModel m) {
        super(m);
    }

    public ClusterKernelMatrixModel(Source TrainSet, Trainer T, KernelMatrix _matrix, Kernel k) {
        super(TrainSet, T, _matrix, k);
    }

    public ClusterKernelMatrixModel(Source TrainSet, Trainer T, Kernel k) {
        super(TrainSet, T, k);
    }

    public String newickClusterTree;

    public int[] medoids;

    public float[] clusterStdev;

    public int[] clusterSizes;

    /**
	 * THis functions returns the feature space distances calculated from the kernel matrix
	 * @return A two-dimensional array with the distances
	 */
    public double[][] getFSDistances() {
        int size = getMatrix().size();
        double[][] distances;
        try {
            distances = new double[size][size];
            for (int i = 0; i < size; i++) {
                double distii = get(i, i);
                for (int j = i; j < size; j++) {
                    distances[i][j] = Math.sqrt(distii + get(j, j) - 2 * get(i, j));
                    distances[j][i] = distances[i][j];
                }
            }
        } catch (OutOfMemoryError e) {
            distances = null;
            System.err.println("Not enough memory for distances!");
            System.gc();
        }
        return distances;
    }

    public void store(String target) {
        try {
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            DocumentBuilderFactoryImpl factory = (DocumentBuilderFactoryImpl) DocumentBuilderFactoryImpl.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());
            DocumentBuilderImpl builder = (DocumentBuilderImpl) factory.newDocumentBuilder();
            DocumentImpl document = (DocumentImpl) builder.newDocument();
            Element root = document.createElement("KernelModel");
            document.appendChild(root);
            Element settings = document.createElement("Settings");
            root.appendChild(settings);
            settings.setAttribute("Algorithm", this.trainer.getIdentifier());
            settings.setAttribute("Kernel", getIdentifier());
            if (source != null) settings.setAttribute("Source", this.source.getIdentifier());
            settings.setAttribute("HasKernelMatrix", "jep");
            Element model = document.createElement("Model");
            root.appendChild(model);
            if (alpha != null) {
                Element support = document.createElement("Classes");
                model.appendChild(support);
                for (int i = 0; i < alpha.length; i++) {
                    Element sv = document.createElement("Instance");
                    support.appendChild(sv);
                    sv.setAttribute("Cluster", "" + alpha[i]);
                }
            }
            if (this.newickClusterTree != null) {
                Element newick = document.createElement("NewickString");
                model.appendChild(newick);
                newick.appendChild(document.createTextNode(newickClusterTree));
            }
            if (this.medoids != null) {
                model.setAttribute("Clusters", "" + medoids.length);
                model.setAttribute("SSW", "" + this.b);
                Element centers = document.createElement("Medoids");
                model.appendChild(centers);
                for (int k = 0; k < medoids.length; k++) {
                    Element medoid = document.createElement("Medoid");
                    centers.appendChild(medoid);
                    medoid.setAttribute("Instance", "" + medoids[k]);
                    medoid.setAttribute("Cluster", "" + k);
                    if (source != null) {
                        medoid.setAttribute("Label", "" + source.getValue(medoids[k]));
                        medoid.appendChild(document.createTextNode(source.InstanceToString(medoids[k])));
                    }
                }
            }
            FileWriter w = new FileWriter(target);
            TransformerImpl transformer = (TransformerImpl) TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource ds = new DOMSource(document);
            StreamResult res = new StreamResult(w);
            transformer.transform(ds, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

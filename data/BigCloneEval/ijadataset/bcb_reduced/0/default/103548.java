import java.util.HashMap;
import java.util.Map;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.ClusterUtil;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.ClusterUtilFactory;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.handler.AbstractClusterHandler;
import edu.ucdavis.genomics.metabolomics.binbase.cluster.util.RocksClusterFactoryImpl;
import edu.ucdavis.genomics.metabolomics.util.config.XMLConfigurator;
import edu.ucdavis.genomics.metabolomics.util.status.ReportEvent;
import edu.ucdavis.genomics.metabolomics.util.status.ReportType;

public class YourOwnCalucation3 extends AbstractClusterHandler {

    public boolean startProcessing() throws Exception {
        if (getObject() instanceof Long) {
            getReport().report(getObject(), new ReportEvent("start", ""), new ReportType("calculate", ""));
            long result = fib((Long) getObject());
            getReport().report(getObject(), new ReportEvent("finished", ""), new ReportType("result", "the result for you current calculation", result));
        }
        return true;
    }

    public long fib(long n) {
        if (n <= 1) return n; else return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] args) throws Exception {
        XMLConfigurator.getInstance().getProperties();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("username", args[1]);
        properties.put("password", args[2]);
        properties.put("server", args[0]);
        ClusterUtil util = ClusterUtilFactory.newInstance(RocksClusterFactoryImpl.class.getName()).createUtil(properties);
        util.scheduleJob(new Long(35), YourOwnCalucation3.class.getName());
        util.scheduleJob(new Long(40), YourOwnCalucation3.class.getName());
        util.scheduleJob(new Long(45), YourOwnCalucation3.class.getName());
        util.scheduleJob(new Long(50), YourOwnCalucation3.class.getName());
        System.out.println(util.startNode());
        System.out.println(util.startNode());
        System.out.println(util.startNode());
        System.out.println(util.startNode());
        util.destroy();
    }
}

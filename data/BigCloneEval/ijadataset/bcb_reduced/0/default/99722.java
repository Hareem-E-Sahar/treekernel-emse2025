import org.w3c.dom.*;
import java.io.*;
import javax.xml.parsers.*;
import java.util.*;

public class gjp_workflowEngine {

    ListIterator workflow;

    ArrayList workflowList;

    gjp_coreAPI capi;

    Class stringArrayClass;

    ArrayList api;

    Class apiClass;

    public static void main(String args[]) {
        gjp_workflowEngine we = new gjp_workflowEngine();
    }

    gjp_workflowEngine() {
        init();
    }

    gjp_workflowEngine(HashMap context) {
        init();
        context.put("workflowList", workflowList);
    }

    private void init() {
        ArrayList workflowList = new ArrayList();
        Iterator workflow = workflowList.listIterator();
        api = new ArrayList();
        apiClass = api.getClass();
    }

    public void run(String workflowName) {
    }

    public void run() {
        boolean proceed = true;
        while (proceed && workflow.hasNext()) {
            api = (ArrayList) workflow.next();
            workflow.remove();
            Method meth = capiClass.getMethod(api.get(1), apiClass);
            proceed = (boolean) meth.invoke(capi, api);
        }
    }

    public void get(ArrayList workflowName) {
        workflowList.addAll(workflowName);
    }
}

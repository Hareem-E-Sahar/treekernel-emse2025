import java.io.*;
import org.mozilla.javascript.*;
import org.w3c.dom.*;
import java.util.*;
import org.apache.xalan.xslt.*;
import java.lang.reflect.*;

public class gjp_coreAPI {

    HashMap sessionContext;

    HashMap appContext;

    boolean cacheing;

    int state;

    Document rootDoc;

    HashMap templates;

    HashMap maps;

    HashMap rules;

    HashMap sql;

    HashMap scripts;

    HashMap workflows;

    gjp_transformer transformer;

    gjp_getXPath gxp;

    PrintWriter out;

    String templatePath;

    String rulePath;

    String scriptPath;

    String workflowPath;

    String mapPath;

    String sqlPath;

    Class thisClass;

    Class paramClass[] = new Class[1];

    ArrayList workflowList;

    ListIterator workflow;

    public static void main(String args[]) {
        boolean proceed = true;
        gjp_coreAPI capi = new gjp_coreAPI();
        ArrayList al = new ArrayList();
        al.add("save");
        al.add("xpath");
        al.add("map");
        Class testClass[] = new Class[1];
        testClass[0] = al.getClass();
        try {
            Object api[] = new Object[1];
            api[0] = al;
            System.out.println(al);
            String methodName = (String) al.get(0);
            System.out.println(methodName);
            Method meth = capi.getClass().getMethod(methodName, testClass);
            Object o = meth.invoke(capi, api);
            proceed = ((Boolean) o).booleanValue();
            System.out.println(proceed);
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        }
    }

    gjp_coreAPI() {
    }

    gjp_coreAPI(HashMap sessionContext) {
        HashMap appContext = (HashMap) sessionContext.get("appContext");
        rootDoc = (Document) appContext.get("rootDoc");
        templates = (HashMap) appContext.get("templates");
        maps = (HashMap) appContext.get("templates");
        rules = (HashMap) appContext.get("rules");
        sql = (HashMap) appContext.get("sql");
        scripts = (HashMap) appContext.get("scripts");
        workflows = (HashMap) appContext.get("workflows");
        transformer = (gjp_transformer) appContext.get("transformer");
        gxp = (gjp_getXPath) appContext.get("xpath");
        out = (PrintWriter) sessionContext.get("printWriter");
        Properties props = (Properties) appContext.get("props");
        templatePath = (String) props.get("templates");
        rulePath = (String) props.get("rules");
        scriptPath = (String) props.get("scripts");
        workflowPath = (String) props.get("workflows");
        mapPath = (String) props.get("maps");
        sqlPath = (String) props.get("sql");
        thisClass = this.getClass();
        ArrayList workflowList = new ArrayList();
        paramClass[0] = workflowList.getClass();
        workflow = workflowList.listIterator();
    }

    public boolean display(ArrayList al) {
        String templateName = (String) al.get(2);
        XSLTInputSource xis = null;
        if (cacheing) {
            xis = (XSLTInputSource) templates.get(templateName);
            if (xis == null) {
                xis = new XSLTInputSource(templatePath + templateName);
                templates.put(templateName, xis);
            }
        }
        out.print(transformer.transform(rootDoc, xis));
        return false;
    }

    public boolean displayNostop(ArrayList al) {
        String templateName = (String) al.get(2);
        XSLTInputSource xis = null;
        if (cacheing) {
            xis = (XSLTInputSource) templates.get(templateName);
            if (xis == null) {
                xis = new XSLTInputSource(templatePath + templateName);
                templates.put(templateName, xis);
            }
        }
        out.print(transformer.transform(rootDoc, xis));
        return true;
    }

    public boolean copy(ArrayList al) {
        return true;
    }

    public boolean move(ArrayList al) {
        return true;
    }

    public boolean remove(ArrayList al) {
        return true;
    }

    public boolean create(ArrayList al) {
        return true;
    }

    private void create(String mapName, String xpath) {
        Vector nodes = gxp.resolve(xpath);
        gjp_DOMWrapper dw = (gjp_DOMWrapper) nodes.get(1);
        Vector sqlMap = (Vector) maps.get(mapName);
        HashMap fieldMap = (HashMap) sqlMap.get(2);
        Set fieldSet = fieldMap.keySet();
        String nodeName;
        Iterator iter = fieldSet.iterator();
        while (iter.hasNext()) {
            nodeName = (String) iter.next();
            dw = dw.create(nodeName);
            dw.setValue(null);
        }
    }

    private void create(String nodeName, String xpath, String value) {
        Vector nodes = gxp.resolve(xpath);
        gjp_DOMWrapper dw = (gjp_DOMWrapper) nodes.get(1);
        dw = dw.create(nodeName);
        dw.setValue(value);
    }

    public boolean runScript(ArrayList al) {
        return true;
    }

    public boolean rule(ArrayList al) {
        return true;
    }

    public boolean workflow(ArrayList al) {
        return true;
    }

    public boolean truncate(ArrayList al) {
        return true;
    }

    public boolean qbe(ArrayList al) {
        return true;
    }

    private void qbe(String origin, String mapName, String destination) {
    }

    private void qbe(String xpath, String mapName) {
    }

    public boolean sql(ArrayList al) {
        return true;
    }

    public boolean rename(ArrayList al) {
        return true;
    }

    public boolean save(ArrayList al) {
        return true;
    }

    public boolean userInput(ArrayList al) {
        return false;
    }

    public boolean initialize(ArrayList al) {
        return true;
    }

    private void initialize(String nodename, String xpath, String Value) {
    }

    private void initialize(String mapname, String xpath) {
    }

    public boolean set(ArrayList al) {
        return true;
    }

    public void workflowEngine() {
        boolean proceed = true;
        try {
            while (proceed && workflow.hasNext()) {
                Object api[] = new Object[1];
                api[0] = workflow.next();
                ArrayList paramList = (ArrayList) api[0];
                workflow.remove();
                Method meth = thisClass.getMethod((String) paramList.get(0), paramClass);
                Object o = meth.invoke(this, api);
                proceed = ((Boolean) o).booleanValue();
            }
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        }
    }

    public void workflowEngine(String workflowName) {
        workflowEngine();
    }
}

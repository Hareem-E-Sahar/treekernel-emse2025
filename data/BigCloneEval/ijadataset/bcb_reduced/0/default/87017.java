import java.lang.reflect.Constructor;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * The testController handles all methods for creating a test Object and 
 * executing that object.
 * 
 */
public class TestController {

    /**
     * Takes the given ArrayList of HashMaps and creates objects of the type
     * iTest with the data from each HashMap in the ArrayList
     * @return ArrayList<iTest>: A ArrayList with iTest objects
     */
    public ArrayList<iTest> createTests(ArrayList<HashMap> testArray) {
        ArrayList<iTest> tests = new ArrayList();
        Iterator iter = testArray.iterator();
        while (iter.hasNext()) {
            HashMap testParams = (HashMap) iter.next();
            String className = (String) testParams.get("name");
            try {
                Class temp = Class.forName(className);
                Constructor con = temp.getConstructor(HashMap.class);
                Object o = con.newInstance(new Object[] { testParams });
                iTest test = (iTest) o;
                tests.add(test);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        return tests;
    }

    /**
     * Method to execute one test and store the result in a HashMap
     * This HashMap gets added to a ArrayList with possible unsent results
     * from previous tests
     * 
     * @param test : object of the type iTest
     * @param oldTestResults : ArrayList containing HashMaps with non-sent testresults 
     * @return ArrayList containing all unsent testresults + 1 new one
     */
    public ArrayList<HashMap> execTest(iTest test, ArrayList<HashMap> oldTestResults) {
        ArrayList<HashMap> testResults = oldTestResults;
        test.run();
        HashMap results = test.getResult();
        Set keys = results.keySet();
        Iterator resultsIt = keys.iterator();
        while (resultsIt.hasNext()) {
            String resultName = (String) resultsIt.next().toString();
            HashMap resultMap = new HashMap();
            String ip = getIp();
            String time = "" + System.currentTimeMillis() / 1000;
            resultMap.put("time", time);
            resultMap.put("ip", ip);
            resultMap.put("name", resultName);
            resultMap.put("target", test.getTarget());
            resultMap.put("result", results.get(resultName).toString());
            testResults.add(resultMap);
        }
        return testResults;
    }

    /**
     * 
     * @return String ip: the ipv4 address for this host
     */
    public String getIp() {
        String ip = "";
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface eth = (NetworkInterface) e.nextElement();
                if (!eth.isLoopback()) {
                    Enumeration addresses = eth.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress a = (InetAddress) addresses.nextElement();
                        if (a instanceof Inet4Address) {
                            ip += a.getHostAddress() + " ";
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return ip;
    }
}

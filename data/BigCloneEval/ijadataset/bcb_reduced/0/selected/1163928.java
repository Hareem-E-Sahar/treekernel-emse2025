package org.infoeng.ictp.client.sim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.logging.LoggingServiceWithPrefix;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UID;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.service.BlackboardService;
import org.infoeng.icws.documents.InformationCurrencySeries;
import org.infoeng.icws.documents.InformationCurrencyUnit;
import org.infoeng.icws.client.URLIssuanceClient;

public class GenerateInformationCurrencyPlugin extends ComponentPlugin {

    private BlackboardService myBBService;

    private CollectionSubscription myStringSub;

    private Random myRandom;

    private KeyPair myKeyPair;

    private int MAX_RFC = 3500;

    private int MIN_RFC = 2000;

    private String rfcPrefix = "http://www.ietf.org/rfc/rfc";

    private String cert_endpoint = "https://localhost:48443/icws/services/ICWS";

    private String storage_directory = "/home/jpb/tmp/ics";

    private String keystore_file = "/n/home/jpb/code/infoeng/trunk/icws-3/build/icws-run/localhost-keystore.jks";

    public void load() {
        super.load();
        System.out.println("loaded GenerateInformationCurrencyPlugin.");
    }

    protected void setupSubscriptions() {
        try {
            Iterator iter = getParameters().iterator();
            String uuid = "";
            if (iter.hasNext()) {
                uuid = (String) iter.next();
            } else {
                return;
            }
            myBBService = getBlackboardService();
            File uuidDir = new File(storage_directory + File.separator + uuid.trim());
            uuidDir.mkdir();
            Properties issuanceProps = new Properties();
            myRandom = new Random();
            int firstInt = -1;
            int secondInt = -1;
            while (true) {
                firstInt = myRandom.nextInt(MAX_RFC);
                secondInt = myRandom.nextInt(MAX_RFC);
                if ((firstInt > MIN_RFC) && (secondInt > MIN_RFC)) break;
            }
            String rfcUrlOne = "" + rfcPrefix + firstInt + ".txt";
            String rfcUrlTwo = "" + rfcPrefix + secondInt + ".txt";
            issuanceProps.setProperty("trustStore", keystore_file);
            issuanceProps.setProperty("certification_endpoint", cert_endpoint);
            System.setProperty("javax.net.ssl.trustStore", keystore_file);
            int x = 0;
            File[] tmpFileArray = uuidDir.listFiles();
            int numSeries = 0;
            for (x = 0; x < tmpFileArray.length; x++) {
                try {
                    InformationCurrencySeries ics = new InformationCurrencySeries(new FileInputStream(tmpFileArray[x]));
                    myBBService.publishAdd(ics);
                } catch (Exception e) {
                }
            }
            File keypairFile = new File(uuidDir.getCanonicalPath() + File.separator + "keypair.obj");
            myKeyPair = null;
            if (keypairFile.exists()) {
                myKeyPair = (KeyPair) new ObjectInputStream(new FileInputStream(keypairFile)).readObject();
            }
            if (myKeyPair == null) {
                try {
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                    kpg.initialize(2048);
                    myKeyPair = kpg.generateKeyPair();
                    FileOutputStream fos = new FileOutputStream(keypairFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(myKeyPair);
                    oos.flush();
                    fos.flush();
                } catch (Exception e) {
                }
            }
            if (tmpFileArray.length < 6) {
                try {
                    InformationCurrencySeries retICSOne = URLIssuanceClient.generateIC(new URL(rfcUrlOne), myKeyPair.getPrivate(), issuanceProps);
                    File tmpFileOne = File.createTempFile("ics-out-", ".xml", uuidDir);
                    retICSOne.toString(new FileOutputStream(tmpFileOne));
                    myBBService.publishAdd(retICSOne);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    InformationCurrencySeries retICSTwo = URLIssuanceClient.generateIC(new URL(rfcUrlTwo), myKeyPair.getPrivate(), issuanceProps);
                    File tmpFileTwo = File.createTempFile("ics-out-", ".xml", uuidDir);
                    retICSTwo.toString(new FileOutputStream(tmpFileTwo));
                    myBBService.publishAdd(retICSTwo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            myBBService.publishAdd(myKeyPair);
            System.out.println("created myKeyPair");
            myStringSub = (CollectionSubscription) myBBService.subscribe(myStringPredicate);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void execute() {
        for (Enumeration enu = myStringSub.elements(); enu.hasMoreElements(); ) {
            Object obj = enu.nextElement();
            System.out.println(" enumeration element: " + obj + "");
            System.out.println("agent identifier: " + getAgentIdentifier() + "");
        }
    }

    private UnaryPredicate myStringPredicate = new UnaryPredicate() {

        public boolean execute(Object o) {
            return o instanceof String;
        }
    };
}

package net.sf.easymol.comm.bluetooth.windows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import net.sf.easymol.core.Molecule;
import net.sf.easymol.io.xml.NewMoleculetoXML;
import net.sf.easymol.io.xml.NewXMLtoMolecule;
import org.jdom.output.XMLOutputter;

/**
 * @author Jacob Alvarez Benedicto Class to implement the first send of an image
 *         over a bluetooth connection. PFC phase 1.
 * 
 * --------------------------------------------------------------------------------------
 * IMPORANT NOTE: All the bluetooth service and device discovery has been
 * obtained from a Sun example that was included in the Nokia's Javadoc of its
 * Eclipse Plugin, and Benhui's BlueCove example. The reason to do this is to
 * test the possibity of sending and displaying an image over a bluetooth
 * connection without worrying about the things attained to the bluetooth net
 * environment. In the next version a new and original Bluetooth Net Package
 * will be made to complete, refine, and adjust Bluetooth device and service
 * discovery to the this service.
 * 
 * --------------------------------------------------------------------------------------
 */
public class ImageTester implements DiscoveryListener {

    /**
	 * The DiscoveryAgent for the local Bluetooth device.
	 */
    private DiscoveryAgent agent;

    /**
	 * The max number of service searches that can occur at any one time.
	 */
    private int maxServiceSearches = 0;

    /**
	 * The number of service searches that are presently in progress.
	 */
    private int serviceSearchCount;

    /**
	 * Keeps track of the transaction IDs returned from searchServices.
	 */
    private int transactionID[];

    /**
	 * The service record to a printer service that can print the message
	 * provided at the command line.
	 */
    private ServiceRecord record;

    /**
	 * Keeps track of the devices found during an inquiry.
	 */
    private Vector deviceList;

    private Molecule mol;

    /**
	 * Creates an imageTester object and prepares the object for device
	 * discovery and service searching.
	 * 
	 * @exception BluetoothStateException
	 *                if the Bluetooth system could not be initialized
	 */
    public ImageTester(Molecule mol) throws BluetoothStateException {
        System.out.println("Image Send Test v1.0\n\n");
        this.mol = mol;
        System.out.println(this.mol);
        LocalDevice local = LocalDevice.getLocalDevice();
        agent = local.getDiscoveryAgent();
        try {
            maxServiceSearches = 1;
        } catch (NumberFormatException e) {
            System.out.println("General Application Error");
            System.out.println("\tNumberFormatException: " + e.getMessage());
        }
        transactionID = new int[maxServiceSearches];
        for (int i = 0; i < maxServiceSearches; i++) {
            transactionID[i] = -1;
        }
        record = null;
        deviceList = new Vector();
        ImageTester client = this;
        ImageClient imClient;
        ServiceRecord imageServer = client.findImageServer();
        if (imageServer != null) {
            String conURL = imageServer.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            if (!conURL.contains(":14")) {
                conURL = conURL.replaceFirst(":[1]", ":14");
            }
            System.out.println("conURL: " + conURL);
            imClient = new ImageClient(conURL);
            try {
                imClient.sendImage(mol);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No Printer was found");
        }
    }

    public ImageTester() throws BluetoothStateException {
        System.out.println("Image Send Test v1.0\n\n");
        this.mol = mol;
        System.out.println(this.mol);
        LocalDevice local = LocalDevice.getLocalDevice();
        agent = local.getDiscoveryAgent();
        try {
            maxServiceSearches = 1;
        } catch (NumberFormatException e) {
            System.out.println("General Application Error");
            System.out.println("\tNumberFormatException: " + e.getMessage());
        }
        transactionID = new int[maxServiceSearches];
        for (int i = 0; i < maxServiceSearches; i++) {
            transactionID[i] = -1;
        }
        record = null;
        deviceList = new Vector();
    }

    /**
	 * Adds the transaction table with the transaction ID provided.
	 * 
	 * @param trans
	 *            the transaction ID to add to the table
	 */
    private void addToTransactionTable(int trans) {
        for (int i = 0; i < transactionID.length; i++) {
            if (transactionID[i] == -1) {
                transactionID[i] = trans;
                return;
            }
        }
    }

    /**
	 * Removes the transaction from the transaction ID table.
	 * 
	 * @param trans
	 *            the transaction ID to delete from the table
	 */
    private void removeFromTransactionTable(int trans) {
        for (int i = 0; i < transactionID.length; i++) {
            if (transactionID[i] == trans) {
                transactionID[i] = -1;
                return;
            }
        }
    }

    /**
	 * Completes a service search on each remote device in the list until all
	 * devices are searched or until a printer is found that this application
	 * can print to.
	 * 
	 * @param devList
	 *            the list of remote Bluetooth devices to search
	 * 
	 * @return true if a printer service is found; otherwise false if no printer
	 *         service was found on the devList provided
	 */
    private boolean searchServices(RemoteDevice[] devList) {
        UUID[] searchList = new UUID[1];
        searchList[0] = new UUID("0ECEF8B070D211DAA97C000FB07A0B78", false);
        for (int i = 0; i < devList.length; i++) {
            System.out.println("Length = " + devList.length);
            if (record != null) {
                System.out.println("Record is not null");
                return true;
            }
            try {
                System.out.println("Starting Service Search on " + devList[i].getBluetoothAddress());
                int trans = agent.searchServices(null, searchList, devList[i], this);
                System.out.println("Starting Service Search " + trans);
                addToTransactionTable(trans);
            } catch (BluetoothStateException e) {
                System.out.println("BluetoothStateException: " + e.getMessage());
            }
            synchronized (this) {
                serviceSearchCount++;
                System.out.println("maxServiceSearches = " + maxServiceSearches);
                System.out.println("serviceSearchCount = " + serviceSearchCount);
                if (serviceSearchCount == maxServiceSearches) {
                    System.out.println("Waiting");
                    try {
                        this.wait();
                    } catch (Exception e) {
                    }
                }
                System.out.println("Done Waiting " + serviceSearchCount);
            }
        }
        while (serviceSearchCount > 0) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (Exception e) {
                }
            }
        }
        if (record != null) {
            System.out.println("Record is not null");
            return true;
        } else {
            System.out.println("Record is null");
            return false;
        }
    }

    /**
	 * Finds the first printer that is available to print to.
	 * 
	 * @return the service record of the printer that was found; null if no
	 *         printer service was found
	 */
    public ServiceRecord findImageServer() {
        RemoteDevice[] devList;
        try {
            agent.startInquiry(DiscoveryAgent.GIAC, this);
            synchronized (this) {
                try {
                    this.wait();
                } catch (Exception e) {
                }
            }
        } catch (BluetoothStateException e) {
            System.out.println("Unable to find devices to search");
        }
        if (deviceList.size() > 0) {
            devList = new RemoteDevice[deviceList.size()];
            deviceList.copyInto(devList);
            if (searchServices(devList)) {
                return record;
            }
        }
        return null;
    }

    /**
	 * Called when a device was found during an inquiry. An inquiry searches for
	 * devices that are discoverable. The same device may be returned multiple
	 * times.
	 * 
	 * @see DiscoveryAgent#startInquiry
	 * 
	 * @param btDevice
	 *            the device that was found during the inquiry
	 * 
	 * @param cod
	 *            the service classes, major device class, and minor device
	 *            class of the remote device being returned
	 * 
	 */
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        System.out.println("Found device = " + btDevice.getBluetoothAddress());
        deviceList.addElement(btDevice);
    }

    /**
	 * The following method is called when a service search is completed or was
	 * terminated because of an error. Legal status values include:
	 * <code>SERVICE_SEARCH_COMPLETED</code>,
	 * <code>SERVICE_SEARCH_TERMINATED</code>,
	 * <code>SERVICE_SEARCH_ERROR</code>,
	 * <code>SERVICE_SEARCH_DEVICE_NOT_REACHABLE</code>, and
	 * <code>SERVICE_SEARCH_NO_RECORDS</code>.
	 * 
	 * @param transID
	 *            the transaction ID identifying the request which initiated the
	 *            service search
	 * 
	 * @param respCode
	 *            the response code which indicates the status of the
	 *            transaction; guaranteed to be one of the aforementioned only
	 * 
	 */
    public void serviceSearchCompleted(int transID, int respCode) {
        System.out.println("serviceSearchCompleted(" + transID + ", " + respCode + ")");
        removeFromTransactionTable(transID);
        serviceSearchCount--;
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
	 * Called when service(s) are found during a service search. This method
	 * provides the array of services that have been found.
	 * 
	 * @param transID
	 *            the transaction ID of the service search that is posting the
	 *            result
	 * 
	 * @param service
	 *            a list of services found during the search request
	 * 
	 * @see DiscoveryAgent#searchServices
	 */
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        if (record == null) {
            System.out.println("Found a service " + transID);
            System.out.println("Length of array = " + servRecord.length);
            if (servRecord[0] == null) {
                System.out.println("The service record is null");
            }
            record = servRecord[0];
            System.out.println("After this");
            if (record == null) {
                System.out.println("THe Seocnd try was null");
            }
            for (int i = 0; i < transactionID.length; i++) {
                if (transactionID[i] != -1) {
                }
            }
        }
    }

    /**
	 * Called when a device discovery transaction is completed. The
	 * <code>discType</code> will be <code>INQUIRY_COMPLETED</code> if the
	 * device discovery transactions ended normally, <code>INQUIRY_ERROR</code>
	 * if the device discovery transaction failed to complete normally,
	 * <code>INQUIRY_TERMINATED</code> if the device discovery transaction was
	 * canceled by calling <code>DiscoveryAgent.cancelInquiry()</code>.
	 * 
	 * @param discType
	 *            the type of request that was completed; one of
	 *            <code>INQUIRY_COMPLETED</code>, <code>INQUIRY_ERROR</code>
	 *            or <code>INQUIRY_TERMINATED</code>
	 */
    public void inquiryCompleted(int discType) {
        synchronized (this) {
            try {
                this.notifyAll();
            } catch (Exception e) {
            }
        }
    }

    /**
	 * Main method of the ImageTester
	 * 
	 * @param args -
	 *            The complete name of the path of the image that has to be sent
	 *            over the bluetooth connection.
	 */
    public static void main(String[] args) {
        ImageTester client = null;
        ImageClient imClient;
        try {
            client = new ImageTester(null);
        } catch (BluetoothStateException e) {
            System.out.println("Failed to start Bluetooth System");
            System.out.println("\tBluetoothStateException: " + e.getMessage());
        }
        ServiceRecord imageServer = client.findImageServer();
        if (imageServer != null) {
            String conURL = imageServer.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            if (!conURL.contains(":14")) {
                conURL = conURL.replaceFirst(":[1]", ":14");
            }
            System.out.println("conURL: " + conURL);
            imClient = new ImageClient(conURL);
            try {
                imClient.sendImage(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No Printer was found");
        }
    }
}

/**
 * The ImageClient will make a connection using the connection string provided
 * and send a message to the server to print the data sent.
 */
class ImageClient {

    /**
	 * Keeps the connection string in case the application would like to make
	 * multiple connections to a printer.
	 */
    private String serverConnectionString;

    /**
	 * Creates an ImageClient that will send an image to an ImageServer.
	 * 
	 * @param server
	 *            the connection string used to connect to the server
	 */
    public ImageClient(String server) {
        serverConnectionString = server;
    }

    /**
	 * Sends the data to the ImageServer to display it. This method will
	 * establish a connection to the server and send the bytes to the server.
	 * 
	 * @param data
	 *            the data to send to the printer
	 * 
	 * @return true if the data was printed; false if the data failed to be
	 *         printed
	 */
    public boolean sendImage(Molecule data) throws Exception {
        OutputStream os, os2 = null;
        InputStream is, is2 = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        StreamConnection con = null;
        GZIPOutputStream gzout = null;
        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bis = null;
        if (data != null) {
            System.out.println(data);
        } else {
            data = new NewXMLtoMolecule(new File("xml/VX.xml")).getMolecule();
        }
        try {
            con = (StreamConnection) Connector.open(serverConnectionString);
            os = con.openOutputStream();
            is = con.openInputStream();
            oos = new ObjectOutputStream(os);
            ois = new ObjectInputStream(is);
            bos = new ByteArrayOutputStream();
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(512, SecureRandom.getInstance("SHA1PRNG"));
            AlgorithmParameters params = paramGen.generateParameters();
            DHParameterSpec dhSpec = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);
            keyGen.initialize(dhSpec);
            KeyPair keypair = keyGen.generateKeyPair();
            System.out.println("KeyPair generated.");
            PrivateKey privateKey = keypair.getPrivate();
            PublicKey publicKey = keypair.getPublic();
            System.out.println(publicKey.getFormat());
            PublicKey rempub = null;
            byte[] ourpublicKeyBytes = publicKey.getEncoded();
            byte[] theirpublicKeyBytes = null;
            System.out.println("Our: " + toHexString(ourpublicKeyBytes));
            oos.writeObject(publicKey);
            Thread.sleep(1000);
            System.out.println("Our public was sent.");
            theirpublicKeyBytes = ((PublicKey) ois.readObject()).getEncoded();
            System.out.println("The: " + toHexString(theirpublicKeyBytes));
            System.out.println("Remote public key is read.");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(theirpublicKeyBytes);
            KeyFactory keyFact = KeyFactory.getInstance("DH");
            rempub = keyFact.generatePublic(x509KeySpec);
            DHParameterSpec paramspec = ((DHPublicKey) rempub).getParams();
            KeyAgreement ka = KeyAgreement.getInstance("DH");
            ka.init(privateKey);
            ka.doPhase(rempub, true);
            String algorithm = "TripleDES";
            SecretKey secretKey = ka.generateSecret(algorithm);
            System.out.println(toHexString(secretKey.getEncoded()));
            SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), algorithm);
            Cipher c = Cipher.getInstance(algorithm);
            c.init(Cipher.ENCRYPT_MODE, skeySpec);
            oos.flush();
            oos.close();
            os.flush();
            os.close();
            os2 = con.openOutputStream();
            oos = new ObjectOutputStream(os2);
            long ttim = System.currentTimeMillis();
            NewMoleculetoXML mxml = new NewMoleculetoXML(data);
            org.jdom.Document doc = mxml.getDocument();
            XMLOutputter out = new XMLOutputter();
            SealedObject so = new SealedObject(doc, c);
            oos.writeObject(so);
            oos.flush();
            System.out.println("Time taken:" + (System.currentTimeMillis() - ttim));
            oos.flush();
            oos.close();
            os2.flush();
            os2.close();
            con.close();
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        } catch (java.security.InvalidKeyException e) {
        } catch (java.security.spec.InvalidKeySpecException e) {
        } catch (java.security.InvalidAlgorithmParameterException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        System.out.println("The image was sent");
        return true;
    }

    private void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    private String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }
}

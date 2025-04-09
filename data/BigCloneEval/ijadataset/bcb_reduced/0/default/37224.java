import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ContentHandler;
import java.net.Socket;
import java.util.LinkedList;

public class XMLDB implements DBInterface {

    private Document doc;

    private XPath xpath;

    private ContentHandler ch;

    private SAXParser parser;

    private File file;

    /**
	* constructor
	* @param file the xml file containing user info
	*/
    public XMLDB(File f) {
        file = f;
        loadDB();
    }

    /**
	* Finds a user in the database
	* @param username the username of the user
	* @param password the plaintext (unencrypted) password
	* @return the user if the user exists, null otherwise
	*/
    public synchronized User getUser(String username, String password, Socket socket) throws DBException {
        try {
            Password enPassword = Password.getInstance();
            password = enPassword.encrypt(password);
            System.out.println("Login attempt:\nusername: " + username + "\npassword: " + password);
            NodeList node = null;
            String ss = "/users/user[@name='" + username + "']";
            node = (NodeList) xpath.compile(ss).evaluate(doc, XPathConstants.NODESET);
            if (node.item(0) != null) {
                System.out.println("User found. Authorizing...");
                if (password.equals(node.item(0).getAttributes().getNamedItem("password").getNodeValue())) {
                    System.out.println("password correct!");
                    String qID = node.item(0).getAttributes().getNamedItem("quest").getNodeValue();
                    int rID = Integer.parseInt(node.item(0).getAttributes().getNamedItem("room").getNodeValue());
                    LinkedList<Item> items = null;
                    if (node.item(0).hasChildNodes()) {
                        items = new LinkedList<Item>();
                        NodeList itemNodes;
                        itemNodes = node.item(0).getChildNodes();
                        for (int y = 0; y < itemNodes.getLength(); y++) {
                            if (!itemNodes.item(y).getNodeName().startsWith("#")) {
                                items.add(new Item(itemNodes.item(y).getAttributes().getNamedItem("name").getNodeValue()));
                            }
                        }
                    }
                    System.out.println("creating user...");
                    return new User(username, socket, qID, rID, items);
                } else {
                    System.out.println("password incorrect!");
                    return null;
                }
            } else {
                System.out.println("user doesnt exist!");
                return null;
            }
        } catch (XPathExpressionException e) {
            throw new DBException(e.getMessage());
        }
    }

    /**
	* Adds a  user to the xml database
	* @param username the username of the user
	* @param password the plaintext (unencrypted) password
	* @return true if the user was successfully added, false otherwise
	*/
    public synchronized boolean addUser(String username, String password, LinkedList<Item> items) throws DBException {
        try {
            System.out.println("Adding new user to database...");
            System.out.print("username:" + username);
            if (userExists(username)) {
                System.out.println("\nUsername taken");
                return false;
            }
            NodeList node = null;
            Password enPassword = Password.getInstance();
            password = enPassword.encrypt(password);
            System.out.println("  password:" + password);
            node = (NodeList) xpath.compile("/users/child::user").evaluate(doc, XPathConstants.NODESET);
            if (node != null) {
                try {
                    OutputStream fout = new FileOutputStream("../users.xml");
                    OutputStream bout = new BufferedOutputStream(fout);
                    OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");
                    out.write("<?xml version=\"1.0\" ");
                    out.write("encoding=\"ISO-8859-1\"?>\r\n");
                    out.write("<users>\r\n");
                    for (int x = 0; x < node.getLength(); x++) {
                        String name = node.item(x).getAttributes().getNamedItem("name").getNodeValue();
                        String pass = node.item(x).getAttributes().getNamedItem("password").getNodeValue();
                        String quest = node.item(x).getAttributes().getNamedItem("quest").getNodeValue();
                        String room = node.item(x).getAttributes().getNamedItem("room").getNodeValue();
                        out.write("\t<user name=\"" + name + "\" password=\"" + pass + "\" quest=\"" + quest + "\" room=\"" + room + "\">\r\n");
                        if (node.item(x).hasChildNodes()) {
                            NodeList itemNodes;
                            itemNodes = node.item(x).getChildNodes();
                            for (int y = 0; y < itemNodes.getLength(); y++) {
                                if (!itemNodes.item(y).getNodeName().startsWith("#")) {
                                    out.write("\t\t<item name=\"" + itemNodes.item(y).getAttributes().getNamedItem("name").getNodeValue() + "\"/>\r\n");
                                }
                            }
                        }
                        out.write("\t</user>\r\n");
                    }
                    out.write("\t<user name=\"" + username + "\" password=\"" + password + "\" quest=\"/\" room=\"-1\">\r\n\t</user>\r\n");
                    out.write("</users>\r\n");
                    out.flush();
                    out.close();
                    System.out.println("User added.");
                    loadDB();
                    return true;
                } catch (UnsupportedEncodingException e) {
                    System.out.println("This VM does not support the Latin-1 character set.");
                    return false;
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            } else {
                System.out.println("User was not added.");
                return false;
            }
        } catch (XPathExpressionException e) {
            throw new DBException(e.getMessage());
        }
    }

    public synchronized boolean updateUser(User u) throws DBException {
        try {
            System.out.println("Updating user database..." + u.getUsername());
            NodeList node = null;
            node = (NodeList) xpath.compile("/users/child::user").evaluate(doc, XPathConstants.NODESET);
            if (node != null) {
                try {
                    OutputStream fout = new FileOutputStream("../users.xml");
                    OutputStream bout = new BufferedOutputStream(fout);
                    OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");
                    out.write("<?xml version=\"1.0\" ");
                    out.write("encoding=\"ISO-8859-1\"?>\r\n");
                    out.write("<users>\r\n");
                    for (int x = 0; x < node.getLength(); x++) {
                        String name = node.item(x).getAttributes().getNamedItem("name").getNodeValue();
                        String pass = node.item(x).getAttributes().getNamedItem("password").getNodeValue();
                        String quest = node.item(x).getAttributes().getNamedItem("quest").getNodeValue();
                        String room = node.item(x).getAttributes().getNamedItem("room").getNodeValue();
                        if (!name.equals(u.getUsername())) {
                            out.write("\t<user name=\"" + name + "\" password=\"" + pass + "\" quest=\"" + quest + "\" room=\"" + room + "\">\r\n");
                            if (node.item(x).hasChildNodes()) {
                                NodeList itemNodes;
                                itemNodes = node.item(x).getChildNodes();
                                for (int y = 0; y < itemNodes.getLength(); y++) {
                                    if (!itemNodes.item(y).getNodeName().startsWith("#")) {
                                        out.write("\t\t<item name=\"" + itemNodes.item(y).getAttributes().getNamedItem("name").getNodeValue() + "\"/>\r\n");
                                    }
                                }
                            }
                            out.write("\t</user>\r\n");
                        } else {
                            out.write("\t<user name=\"" + name + "\" password=\"" + pass + "\" quest=\"" + u.getQuestID() + "\" room=\"" + u.getRoomID() + "\">\r\n");
                            for (Item currentI : u.getInventory()) {
                                out.write("\t\t<item name=\"" + currentI.getName() + "\"/>\r\n");
                            }
                            out.write("\t</user>\r\n");
                        }
                    }
                    out.write("</users>\r\n");
                    out.flush();
                    out.close();
                    System.out.println("Database updated.");
                    loadDB();
                    return true;
                } catch (UnsupportedEncodingException e) {
                    System.out.println("This VM does not support the Latin-1 character set.");
                    return false;
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            } else {
                System.out.println("Database update failed.");
                return false;
            }
        } catch (XPathExpressionException e) {
            throw new DBException(e.getMessage());
        }
    }

    /**
	* checks whether a user with given username exists in the database
	* @param username the username to look for
	* @return true if the username is found in the db, false otherwise
	*/
    private boolean userExists(String username) {
        try {
            NodeList node = null;
            String ss = "/users/user[@name='" + username + "']";
            node = (NodeList) xpath.compile(ss).evaluate(doc, XPathConstants.NODESET);
            if (node.item(0) != null) {
                return true;
            }
            return false;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadDB() {
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            xpath = XPathFactory.newInstance().newXPath();
            ch = new ContentHandler() {

                @Override
                public Object getContent(URLConnection urlc) throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace(System.err);
            System.exit(-1);
        } catch (SAXException saxe) {
            saxe.printStackTrace(System.err);
            System.exit(-1);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}

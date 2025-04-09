import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Traitement_Serveur extends Thread {

    private ObjetServeur serveur;

    private XPath xpath_msg;

    private Document base_msg;

    public Traitement_Serveur(ObjetServeur ser) {
        serveur = ser;
        DocumentBuilder db;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            base_msg = db.parse(new File("./bd_msg.xml"));
            xpath_msg = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sauvegardeBase() {
        DOMSource domSource = new DOMSource(base_msg);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String stringXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + writer.toString();
        ecrireFichier(new File("bd_msg.xml").getAbsolutePath(), stringXML);
    }

    public static void ecrireFichier(String path_dest, String src) {
        OutputStreamWriter osw = null;
        try {
            File fileE = new File(path_dest);
            if (!fileE.exists()) {
                fileE.createNewFile();
            }
            osw = new OutputStreamWriter(new FileOutputStream(fileE), "UTF-8");
            osw.write(src);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                osw.close();
            } catch (Throwable e) {
            }
        }
    }

    public void run() {
        Document doc;
        while (true) {
            try {
                boolean ok = true;
                doc = serveur.recuperer_Msg();
                if (doc == null) {
                    try {
                        for (int i = 0; i < 3; i++) {
                            if (serveur.isTermine()) {
                                sauvegardeBase();
                                this.stop();
                            }
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    int id = Integer.parseInt(doc.getDocumentElement().getAttribute("id_client"));
                    if (doc.getElementsByTagName("type").item(0).getTextContent().equals("2")) {
                        String res = serveur.executerRequete("//produit[@reference = '" + doc.getElementsByTagName("reference").item(0).getTextContent() + "']/quantite");
                        if (!res.equals("")) {
                            serveur.deposer_Msg(id, "V�rification Stock\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\nQuantite = " + res);
                        } else {
                            ok = false;
                            serveur.deposer_Msg(id, "V�rification Stock\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\n Ce produit n'existe pas dans la base");
                        }
                    } else if (doc.getElementsByTagName("type").item(0).getTextContent().equals("0")) {
                        int qte_msg = Integer.parseInt(doc.getElementsByTagName("quantite").item(0).getTextContent());
                        String res = serveur.executerRequete("//produit[@reference = '" + doc.getElementsByTagName("reference").item(0).getTextContent() + "']/quantite");
                        if (!res.equals("")) {
                            int qte_bd = Integer.parseInt(res);
                            Node n = serveur.recupererNode("//produit[@reference = '" + doc.getElementsByTagName("reference").item(0).getTextContent() + "']/quantite");
                            n.setTextContent(String.valueOf(qte_bd + qte_msg));
                            serveur.deposer_Msg(id, "Ajouter\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\nL'ajout est effectu�\nNouvelle quantit� : " + (qte_bd + qte_msg));
                        } else {
                            ok = false;
                            serveur.deposer_Msg(id, "Ajouter\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\n Ce produit n'existe pas dans la base");
                        }
                    } else if (doc.getElementsByTagName("type").item(0).getTextContent().equals("1")) {
                        int qte_msg = Integer.parseInt(doc.getElementsByTagName("quantite").item(0).getTextContent());
                        String res = serveur.executerRequete("//produit[@reference = '" + doc.getElementsByTagName("reference").item(0).getTextContent() + "']/quantite");
                        if (!res.equals("")) {
                            int qte_bd = Integer.parseInt(serveur.executerRequete("//produit[@reference = '" + doc.getElementsByTagName("reference").item(0).getTextContent() + "']/quantite"));
                            if (qte_bd - qte_msg >= 0) {
                                Node n = serveur.recupererNode("//produit[@reference = '" + doc.getElementsByTagName("reference").item(0).getTextContent() + "']/quantite");
                                n.setTextContent(String.valueOf(qte_bd - qte_msg));
                                serveur.deposer_Msg(id, "Retirer\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\nLa suppression est effectu�e\nNouvelle quantit� : " + (qte_bd - qte_msg));
                            } else {
                                ok = false;
                                serveur.deposer_Msg(id, "Retirer\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\nLa suppression a �chou� (quantit� disponible : " + qte_bd + ")");
                            }
                        } else {
                            ok = false;
                            serveur.deposer_Msg(id, "Retirer\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\n Ce produit n'existe pas dans la base");
                        }
                    } else if (doc.getElementsByTagName("type").item(0).getTextContent().equals("3")) {
                        String res = serveur.executerRequete("//produit[@reference = '" + doc.getElementsByTagName("reference").item(0).getTextContent() + "']/quantite");
                        if (res.equals("")) {
                            serveur.inserElement(doc.getElementsByTagName("reference").item(0).getTextContent());
                            serveur.deposer_Msg(id, "Cr�er\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\nLa cr�ation de cette r�f�rence a r�ussi");
                        } else {
                            ok = false;
                            serveur.deposer_Msg(id, "Cr�er\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\nLa cr�ation de cette r�f�rence a �chou�e, elle existe d�j�");
                        }
                    } else if (doc.getElementsByTagName("type").item(0).getTextContent().equals("4")) {
                        String res = serveur.executerRequete("//produit[@reference = '" + doc.getElementsByTagName("reference").item(0).getTextContent() + "']/@reference");
                        if (!res.equals("")) {
                            serveur.supprimerElement(doc.getElementsByTagName("reference").item(0).getTextContent(), id);
                        } else {
                            ok = false;
                            serveur.deposer_Msg(id, "Supprimer\nProduit (r�f : " + doc.getElementsByTagName("reference").item(0).getTextContent() + ")\nLa suppression de cette r�f�rence a �chou�e, elle n'existe pas");
                        }
                    }
                    if (ok) {
                        Node n = base_msg.getElementsByTagName("messages").item(0);
                        Element msg = base_msg.createElement("message");
                        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        Element da = base_msg.createElement("date");
                        da.setTextContent(format.format(new Date()));
                        msg.appendChild(da);
                        format = new SimpleDateFormat("HH:mm");
                        Element h = base_msg.createElement("heure");
                        h.setTextContent(format.format(new Date()));
                        msg.appendChild(h);
                        Element us = base_msg.createElement("user");
                        us.setTextContent(doc.getElementsByTagName("user").item(0).getTextContent());
                        msg.appendChild(us);
                        Element ty = base_msg.createElement("type");
                        ty.setTextContent(doc.getElementsByTagName("type").item(0).getTextContent());
                        msg.appendChild(ty);
                        Element re = base_msg.createElement("reference");
                        re.setTextContent(doc.getElementsByTagName("reference").item(0).getTextContent());
                        msg.appendChild(re);
                        Element qt = base_msg.createElement("quantite");
                        qt.setTextContent(doc.getElementsByTagName("quantite").item(0).getTextContent());
                        msg.appendChild(qt);
                        Element com = base_msg.createElement("commentaire");
                        com.setTextContent(doc.getElementsByTagName("commentaire").item(0).getTextContent());
                        msg.appendChild(com);
                        n.appendChild(msg);
                        System.out.println("coucou" + base_msg.getElementsByTagName("message").item(0) + "coucou");
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}

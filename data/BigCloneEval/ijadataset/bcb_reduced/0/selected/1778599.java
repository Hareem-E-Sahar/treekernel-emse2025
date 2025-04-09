package Utils;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Enumeration;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author nrovinskiy
 */
public class FileWriter {

    File fleWrite;

    Profile prfWrite;

    public FileWriter(String file, Profile profile) {
        fleWrite = new File(file);
        prfWrite = profile;
    }

    public FileWriter(File file, Profile profile) {
        fleWrite = file;
        prfWrite = profile;
    }

    public boolean write(char[] key) {
        try {
            fleWrite.delete();
            fleWrite.createNewFile();
            byte[] k1 = { (byte) 10, (byte) 35, (byte) 40, (byte) 44, (byte) 123, (byte) 37, (byte) 55, (byte) 41 };
            KeySpec keySpec = new PBEKeySpec(key, k1, 2);
            SecretKey scKey;
            scKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(k1, 2);
            Cipher cptFile = Cipher.getInstance(scKey.getAlgorithm());
            cptFile.init(Cipher.ENCRYPT_MODE, scKey, paramSpec);
            FileOutputStream fosOut = new FileOutputStream(fleWrite);
            CipherOutputStream cosOut = new CipherOutputStream(fosOut, cptFile);
            writeXML(cosOut);
            cosOut.flush();
            cosOut.close();
            fosOut.flush();
            fosOut.close();
            return true;
        } catch (IOException ex) {
            System.out.println("write");
            System.out.println(ex.getClass());
            return false;
        } catch (NoSuchAlgorithmException ex1) {
            return false;
        } catch (InvalidKeySpecException ex2) {
            System.out.println(ex2.getMessage());
            return false;
        } catch (NoSuchPaddingException ex3) {
            return false;
        } catch (InvalidKeyException ex4) {
            return false;
        } catch (InvalidAlgorithmParameterException ex5) {
            return false;
        }
    }

    private void writeXML(CipherOutputStream writer) {
        try {
            DocumentBuilderFactory dbfWriter = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbWriter = dbfWriter.newDocumentBuilder();
            Document docXML = dbWriter.newDocument();
            OutputFormat XMLFormat = new OutputFormat(docXML);
            XMLFormat.setIndenting(true);
            XMLSerializer XMLWriter = new XMLSerializer(writer, XMLFormat);
            ContentHandler chHolder = XMLWriter.asContentHandler();
            XMLWriter.serialize(docXML);
            chHolder.startDocument();
            Element elProfile = docXML.createElement("PROFILE");
            elProfile.setAttribute("NAME", prfWrite.getName());
            AttributesImpl atts = new AttributesImpl();
            atts.clear();
            atts.addAttribute("", "", "NAME", "CDATA", prfWrite.getName());
            chHolder.startElement("", "", "PROFILE", atts);
            atts.clear();
            chHolder.startElement("", "", "REASONS", atts);
            if (!prfWrite.getReasons().isEmpty()) {
                Enumeration<String> enReasons = prfWrite.getReasons().keys();
                while (enReasons.hasMoreElements()) {
                    String strReason = enReasons.nextElement();
                    atts.clear();
                    atts.addAttribute("", "", "NAME", "CDATA", strReason);
                    atts.addAttribute("", "", "GROUP", "CDATA", prfWrite.getReasons().get(strReason));
                    chHolder.startElement("", "", "REASON", atts);
                    chHolder.endElement("", "", "REASON");
                }
                enReasons = null;
            }
            chHolder.endElement("", "", "REASONS");
            atts.clear();
            chHolder.startElement("", "", "CARDS", atts);
            if (prfWrite.getCards() != null) {
                for (int i = 0; i < prfWrite.getCardNumber(); i++) {
                    Enumeration<Card> enCards = prfWrite.getCards();
                    while (enCards.hasMoreElements()) {
                        Card crdTemp = enCards.nextElement();
                        if (crdTemp.getOrdNumber() == i) {
                            atts.clear();
                            atts.addAttribute("", "", "NAME", "CDATA", crdTemp.getName());
                            atts.addAttribute("", "", "INIT", "CDATA", Double.toString(crdTemp.getInitialBalance()));
                            chHolder.startElement("", "", "CARD", atts);
                            Enumeration<Transaction> enTransaction = crdTemp.getAllTransactions();
                            while (enTransaction.hasMoreElements()) {
                                Transaction trTmp = enTransaction.nextElement();
                                atts.clear();
                                atts.addAttribute("", "", "ID", "CDATA", Integer.toString(trTmp.getID()));
                                atts.addAttribute("", "", "DATE", "CDATA", trTmp.getTransactionDate());
                                atts.addAttribute("", "", "AMOUNT", "CDATA", Double.toString(trTmp.amount));
                                atts.addAttribute("", "", "REASON", "CDATA", trTmp.strReason);
                                chHolder.startElement("", "", "TRANSACTION", atts);
                                chHolder.endElement("", "", "TRANSACTION");
                            }
                            chHolder.endElement("", "", "CARD");
                            break;
                        }
                    }
                    enCards = null;
                }
            }
            chHolder.endElement("", "", "CARDS");
            chHolder.endElement("", "", "PROFILE");
            chHolder.endDocument();
        } catch (ParserConfigurationException ex) {
            System.out.println("writeXML");
            System.out.println(ex.getMessage());
        } catch (Exception ex1) {
            System.out.println("writeXML");
            System.out.println(ex1.getMessage());
        }
    }
}

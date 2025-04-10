package org.apache.axis2.saaj;

import junit.framework.TestCase;
import org.apache.axis2.saaj.util.SAAJDataSource;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;

/**
 * 
 */
public class SOAPMessageTest extends TestCase {

    private SOAPMessage msg;

    protected void setUp() throws Exception {
        msg = MessageFactory.newInstance().createMessage();
    }

    public void testSaveRequired() {
        try {
            assertTrue("Save Required is False", msg.saveRequired());
        } catch (Exception e) {
            fail("Unexpected Exception : " + e);
        }
    }

    public void testSaveRequired2() {
        try {
            msg.saveChanges();
            assertFalse("Save Required is True", msg.saveRequired());
        } catch (Exception e) {
            fail("Unexpected Exception : " + e);
        }
    }

    public void testRemoveAttachements() {
        Iterator iterator = null;
        AttachmentPart ap1 = null;
        AttachmentPart ap2 = null;
        AttachmentPart ap3 = null;
        try {
            MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SOAPMessage msg = fac.createMessage();
            SOAPPart soapPart = msg.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody body = envelope.getBody();
            SOAPFault sf = body.addFault();
            InputStream in1 = new FileInputStream(new File(System.getProperty("basedir", ".") + "/target/test-resources" + File.separator + "attach.xml"));
            ap1 = msg.createAttachmentPart(in1, "text/xml");
            msg.addAttachmentPart(ap1);
            InputStream in2 = new FileInputStream(new File(System.getProperty("basedir", ".") + "/target/test-resources" + File.separator + "axis2.xml"));
            ap2 = msg.createAttachmentPart(in2, "text/xml");
            msg.addAttachmentPart(ap2);
            InputStream in3 = new FileInputStream(new File(System.getProperty("basedir", ".") + "/target/test-resources" + File.separator + "axis2.xml"));
            ap3 = msg.createAttachmentPart(in3, "text/plain");
            msg.addAttachmentPart(ap3);
            iterator = msg.getAttachments();
            int cnt = 0;
            while (iterator.hasNext()) {
                cnt++;
                iterator.next();
            }
            assertEquals(cnt, 3);
            MimeHeaders mhs = new MimeHeaders();
            mhs.addHeader("Content-Type", "text/xml");
            msg.removeAttachments(mhs);
            iterator = msg.getAttachments();
            cnt = 0;
            iterator = msg.getAttachments();
            while (iterator.hasNext()) {
                cnt++;
                iterator.next();
            }
            assertEquals(cnt, 1);
            iterator = msg.getAttachments();
            AttachmentPart ap = (AttachmentPart) iterator.next();
            String ctype = ap.getContentType();
            assertTrue(ctype.equals("text/plain"));
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    public void testGetContent() {
        try {
            MessageFactory fac = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SOAPMessage msg = fac.createMessage();
            SOAPPart soapPart = msg.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody body = envelope.getBody();
            AttachmentPart ap;
            InputStream inputStream = new FileInputStream(new File(System.getProperty("basedir", ".") + "/test-resources" + File.separator + "attach.xml"));
            ap = msg.createAttachmentPart(inputStream, "text/xml");
            DataHandler dh = new DataHandler(new SAAJDataSource(inputStream, 1000, "text/xml", true));
            StringBuffer sb1 = copyToBuffer(dh.getInputStream());
            assertNotNull(ap);
            try {
                Object o = ap.getContent();
                InputStream is = null;
                assertNotNull(o);
                if (o instanceof StreamSource) {
                    StreamSource ss = (StreamSource) o;
                    is = ss.getInputStream();
                } else {
                    fail("got object: " + o + ", expected object: javax.xml.transform.stream.StreamSource");
                }
            } catch (Exception e) {
                fail("attachment has no content - unexpected");
            }
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    private StringBuffer copyToBuffer(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        StringWriter stringWriter = new StringWriter();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String s;
            while ((s = br.readLine()) != null) stringWriter.write(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringWriter.getBuffer();
    }

    public void _testGetAttachmentsByHREF() {
        String NS_PREFIX = "mypre";
        String NS_URI = "http://myuri.org/";
        try {
            SOAPMessage msg = MessageFactory.newInstance().createMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope envelope = sp.getEnvelope();
            SOAPHeader hdr = envelope.getHeader();
            SOAPBody bdy = envelope.getBody();
            SOAPBodyElement sbe1 = bdy.addBodyElement(envelope.createName("Body1", NS_PREFIX, NS_URI));
            sbe1.addChildElement(envelope.createName("TheGifAttachment", NS_PREFIX, NS_URI));
            sbe1.setAttribute("href", "cid:THEGIF");
            SOAPBodyElement sbe2 = bdy.addBodyElement(envelope.createName("Body2", NS_PREFIX, NS_URI));
            sbe2.addChildElement(envelope.createName("TheXmlAttachment", NS_PREFIX, NS_URI));
            sbe2.setAttribute("href", "cid:THEXML");
            URL url1 = new URL("http://localhost:8080/SOAPMessage/attach.xml");
            URL url2 = new URL("http://localhost:8080/SOAPMessage/attach.gif");
            URL url3 = new URL("http://localhost:8080/SOAPMessage/attach.txt");
            URL url4 = new URL("http://localhost:8080/SOAPMessage/attach.html");
            URL url5 = new URL("http://localhost:8080/SOAPMessage/attach.jpeg");
            AttachmentPart ap1 = msg.createAttachmentPart(new DataHandler(url1));
            AttachmentPart ap2 = msg.createAttachmentPart(new DataHandler(url2));
            AttachmentPart ap3 = msg.createAttachmentPart(new DataHandler(url3));
            AttachmentPart ap4 = msg.createAttachmentPart(new DataHandler(url4));
            AttachmentPart ap5 = msg.createAttachmentPart(new DataHandler(url5));
            ap1.setContentType("text/xml");
            ap1.setContentId("<THEXML>");
            ap2.setContentType("image/gif");
            ap2.setContentId("<THEGIF>");
            ap3.setContentType("text/plain");
            ap3.setContentId("<THEPLAIN>");
            ap4.setContentType("text/html");
            ap4.setContentId("<THEHTML>");
            ap5.setContentType("image/jpeg");
            ap5.setContentId("<THEJPEG>");
            msg.addAttachmentPart(ap1);
            msg.addAttachmentPart(ap2);
            msg.addAttachmentPart(ap3);
            msg.addAttachmentPart(ap4);
            msg.addAttachmentPart(ap5);
            msg.saveChanges();
            AttachmentPart myap = msg.getAttachment(sbe1);
            if (myap == null) {
                fail("Returned null (unexpected)");
            } else if (!myap.getContentType().equals("image/gif")) {
                fail("Wrong attachment was returned: Got Content-Type of " + myap.getContentType() + ", Expected Content-Type of image/gif");
            }
            myap = msg.getAttachment(sbe2);
            if (myap == null) {
                fail("Returned null (unexpected)");
            } else if (!myap.getContentType().equals("text/xml")) {
                fail("Wrong attachment was returned: Got Content-Type of " + myap.getContentType() + ", Expected Content-Type of text/xml");
            }
            QName myqname = new QName("boo-hoo");
            SOAPElement myse = SOAPFactoryImpl.newInstance().createElement(myqname);
            myse.addTextNode("<theBooHooAttachment href=\"cid:boo-hoo\"/>");
            myap = msg.getAttachment(myse);
            assertNull(myap);
        } catch (Exception e) {
            fail("Error :" + e);
        }
    }

    public void _testGetAttachmentByHREF2() {
        String NS_PREFIX = "mypre";
        String NS_URI = "http://myuri.org/";
        try {
            MessageFactory fac = MessageFactory.newInstance();
            SOAPMessage msg = fac.createMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope envelope = sp.getEnvelope();
            SOAPHeader hdr = envelope.getHeader();
            SOAPBody bdy = envelope.getBody();
            SOAPBodyElement sbe1 = bdy.addBodyElement(envelope.createName("Body1", NS_PREFIX, NS_URI));
            sbe1.addChildElement(envelope.createName("TheGifAttachment", NS_PREFIX, NS_URI));
            SOAPBodyElement sbe2 = bdy.addBodyElement(envelope.createName("Body2", NS_PREFIX, NS_URI));
            sbe2.addChildElement(envelope.createName("TheXmlAttachment", NS_PREFIX, NS_URI));
            URL url1 = new URL("http://localhost:8080/SOAPMessage/attach.xml");
            URL url2 = new URL("http://localhost:8080/SOAPMessage/attach.gif");
            URL url3 = new URL("http://localhost:8080/SOAPMessage/attach.txt");
            URL url4 = new URL("http://localhost:8080/SOAPMessage/attach.html");
            URL url5 = new URL("http://localhost:8080/SOAPMessage/attach.jpeg");
            sbe1.setAttribute("href", url2.toString());
            sbe2.setAttribute("href", url1.toString());
            AttachmentPart ap1 = msg.createAttachmentPart(new DataHandler(url1));
            AttachmentPart ap2 = msg.createAttachmentPart(new DataHandler(url2));
            AttachmentPart ap3 = msg.createAttachmentPart(new DataHandler(url3));
            AttachmentPart ap4 = msg.createAttachmentPart(new DataHandler(url4));
            AttachmentPart ap5 = msg.createAttachmentPart(new DataHandler(url5));
            ap1.setContentType("text/xml");
            ap1.setContentId("<THEXML>");
            ap1.setContentLocation(url1.toString());
            ap2.setContentType("image/gif");
            ap2.setContentId("<THEGIF>");
            ap2.setContentLocation(url2.toString());
            ap3.setContentType("text/plain");
            ap3.setContentId("<THEPLAIN>");
            ap3.setContentLocation(url3.toString());
            ap4.setContentType("text/html");
            ap4.setContentId("<THEHTML>");
            ap4.setContentLocation(url4.toString());
            ap5.setContentType("image/jpeg");
            ap5.setContentId("<THEJPEG>");
            ap5.setContentLocation(url5.toString());
            msg.addAttachmentPart(ap1);
            msg.addAttachmentPart(ap2);
            msg.addAttachmentPart(ap3);
            msg.addAttachmentPart(ap4);
            msg.addAttachmentPart(ap5);
            msg.saveChanges();
            AttachmentPart myap = msg.getAttachment(sbe1);
            if (myap == null) {
                fail("Returned null (unexpected)");
            } else if (!myap.getContentType().equals("image/gif")) {
                fail("Wrong attachment was returned: Got Content-Type of " + myap.getContentType() + ", Expected Content-Type of image/gif");
            }
            myap = msg.getAttachment(sbe2);
            if (myap == null) {
                fail("Returned null (unexpected)");
            } else if (!myap.getContentType().equals("text/xml")) {
                fail("Wrong attachment was returned: Got Content-Type of " + myap.getContentType() + ", Expected Content-Type of text/xml");
            }
            QName myqname = new QName("boo-hoo");
            SOAPElement myse = SOAPFactory.newInstance().createElement(myqname);
            myse.addTextNode("<theBooHooAttachment href=\"boo-hoo\"/>");
            myap = msg.getAttachment(myse);
            assertNull(myap);
        } catch (Exception e) {
            fail("Exception: " + e);
        }
    }

    public void _testMessageCreation() {
        try {
            final String NS_PREFIX = "ns-prefix";
            final String NS_URI = "ns-uri";
            MessageFactory fac = MessageFactory.newInstance();
            SOAPMessage msg = fac.createMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope envelope = sp.getEnvelope();
            SOAPHeader hdr = envelope.getHeader();
            SOAPBody bdy = envelope.getBody();
            SOAPElement se = hdr.addHeaderElement(envelope.createName("Header1", NS_PREFIX, NS_URI)).addTextNode("This is Header1");
            SOAPHeaderElement she = (SOAPHeaderElement) se;
            she.setMustUnderstand(true);
            se = hdr.addHeaderElement(envelope.createName("Header2", NS_PREFIX, NS_URI)).addTextNode("This is Header2");
            she = (SOAPHeaderElement) se;
            she.setMustUnderstand(false);
            se = hdr.addHeaderElement(envelope.createName("Header3", NS_PREFIX, NS_URI)).addTextNode("This is Header3");
            she = (SOAPHeaderElement) se;
            she.setMustUnderstand(true);
            se = hdr.addHeaderElement(envelope.createName("Header4", NS_PREFIX, NS_URI)).addTextNode("This is Header4");
            she = (SOAPHeaderElement) se;
            she.setMustUnderstand(false);
            SOAPBodyElement sbe = bdy.addBodyElement(envelope.createName("Body1", NS_PREFIX, NS_URI));
            sbe.addChildElement(envelope.createName("Child1", NS_PREFIX, NS_URI)).addTextNode("This is Child1");
            sbe.addChildElement(envelope.createName("Child2", NS_PREFIX, NS_URI)).addTextNode("This is Child2");
            URL url1 = new URL("http://localhost:8080/SOAPMessage/attach.xml");
            AttachmentPart ap = msg.createAttachmentPart(new DataHandler(url1));
            ap.setContentType("text/xml");
            msg.addAttachmentPart(ap);
            msg.saveChanges();
            URL urlEndpoint = new URL("http://localhost:8080/ReceivingSOAP11Servlet");
            SOAPConnection con = new SOAPConnectionImpl();
            SOAPMessage replymsg = con.call(msg, urlEndpoint);
            if (!validateReplyMessage(replymsg, 1)) {
            } else {
            }
        } catch (Exception e) {
            System.err.println("SendSyncReqRespMsgTest2 Exception: " + e);
            e.printStackTrace(System.err);
        }
    }

    private boolean validateReplyMessage(SOAPMessage msg, int num) {
        try {
            boolean pass = true;
            SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
            boolean foundHeader1 = false;
            boolean foundHeader2 = false;
            boolean foundHeader3 = false;
            boolean foundHeader4 = false;
            Iterator i = envelope.getHeader().examineAllHeaderElements();
            while (i.hasNext()) {
                SOAPElement se = (SOAPElement) i.next();
                Name name = se.getElementName();
                String value = se.getValue();
                if (value == null || name == null) continue; else if (value.equals("This is Header1") && name.getLocalName().equals("Header1")) foundHeader1 = true; else if (value.equals("This is Header2") && name.getLocalName().equals("Header2")) foundHeader2 = true; else if (value.equals("This is Header3") && name.getLocalName().equals("Header3")) foundHeader3 = true; else if (value.equals("This is Header4") && name.getLocalName().equals("Header4")) foundHeader4 = true;
            }
            if (!foundHeader1 || !foundHeader2 || !foundHeader3 || !foundHeader4) {
                pass = false;
            } else {
            }
            boolean foundBody1 = false;
            boolean foundChild1 = false;
            boolean foundChild2 = false;
            SOAPBody bdy = envelope.getBody();
            i = bdy.getChildElements();
            while (i.hasNext()) {
                SOAPBodyElement sbe = (SOAPBodyElement) i.next();
                Name name = sbe.getElementName();
                if (name.getLocalName().equals("Body1")) foundBody1 = true;
                Iterator c = sbe.getChildElements();
                while (c.hasNext()) {
                    SOAPElement se = (SOAPElement) c.next();
                    name = se.getElementName();
                    String value = se.getValue();
                    if (value.equals("This is Child1") && name.getLocalName().equals("Child1")) foundChild1 = true; else if (value.equals("This is Child2") && name.getLocalName().equals("Child2")) foundChild2 = true;
                }
            }
            if (!foundBody1) {
                pass = false;
            } else if (!foundChild1 || !foundChild2) {
                pass = false;
            } else {
            }
            int count = msg.countAttachments();
            if (count == num) {
                i = msg.getAttachments();
                boolean gifFound = false;
                boolean xmlFound = false;
                boolean textFound = false;
                boolean htmlFound = false;
                boolean jpegFound = false;
                while (i.hasNext()) {
                    AttachmentPart a = (AttachmentPart) i.next();
                    String type = a.getContentType();
                    if (type.equals("image/gif")) gifFound = true; else if (type.equals("text/xml")) xmlFound = true; else if (type.equals("text/plain")) textFound = true; else if (type.equals("text/html")) htmlFound = true; else if (type.equals("image/jpeg")) jpegFound = true; else {
                        pass = false;
                    }
                }
                if (num > 0) {
                    pass = false;
                }
                return pass;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}

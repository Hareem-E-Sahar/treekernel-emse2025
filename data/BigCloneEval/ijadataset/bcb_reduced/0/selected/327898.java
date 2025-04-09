package com.ecs.etrade.uiinterfaces;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.empower.model.Customer;
import com.empower.model.Item;
import com.empower.model.Order;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.File;
import java.io.StringWriter;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AttachmentEmailer {

    VelocityEngine engine = new VelocityEngine();

    public AttachmentEmailer() throws Exception {
        configure(engine);
    }

    /**
	   * "Sends" (actually writes to System.out for demonstration
	   * purposes) a receipt e-mail for the specified order.
	   */
    public void sendReceipt(Customer cust) throws Exception {
        Template template = engine.getTemplate("email.vm");
        ArrayList lineItems = new ArrayList();
        lineItems.add(new Item("Java Development with Ant", 44.95f));
        lineItems.add(new Item("BEPL ", 41.37f));
        VelocityContext context = createContext();
        context.put("order", new Order(cust, lineItems));
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        writer.close();
        System.out.println("To: " + cust.getToAddress());
        System.out.println("Subject: " + context.get("subject"));
        System.out.println(writer.getBuffer());
        sendMail(writer, cust);
        System.out.println("After calling the writer...");
    }

    /**
	   * "Sends" (actually writes to System.out for demonstration
	   * purposes) a receipt e-mail for the specified order.
	   */
    public void sendMail(StringWriter data, Customer cust) throws Exception {
        String host = "mail.empowerconsultancy.in";
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.debug", "true");
        Session session = Session.getInstance(props);
        try {
            Message msg = new MimeMessage(session);
            MimeMessageHelper message = new MimeMessageHelper((MimeMessage) msg, true, "UTF-8");
            message.setFrom("suman.ravuri@empowerconsultancy.in");
            message.setTo(cust.getToAddress());
            if (null != cust.getCcAddress()) ;
            {
                message.setCc(new InternetAddress(cust.getCcAddress()));
            }
            if (null != cust.getBccAddress()) ;
            {
                message.setBcc(new InternetAddress(cust.getBccAddress()));
            }
            message.setSubject(cust.getSubject());
            message.setText(data.getBuffer().toString());
            String filePath = cust.getFilePath();
            String fileName;
            fileName = getFileName(filePath);
            message.addAttachment(fileName, new File(filePath));
            Transport.send(message.getMimeMessage());
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    private String getFileName(String filePath) {
        String fileName;
        fileName = null;
        StringTokenizer tokenizer = new StringTokenizer(filePath, "\\");
        int n = 0;
        while (tokenizer.hasMoreElements()) {
            fileName = (String) tokenizer.nextElement();
            System.out.println("" + ++n + ": " + fileName);
        }
        return fileName;
    }

    /**
	   * Configures the engine to use classpath to find templates
	   */
    private void configure(VelocityEngine engine) throws Exception {
        Properties props = new Properties();
        props.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
        props.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".class", ClasspathResourceLoader.class.getName());
        engine.init(props);
    }

    /**
	   * Creates a Velocity context and adds a formatter tool
	   * and store information.
	   */
    private VelocityContext createContext() {
        VelocityContext context = new VelocityContext();
        context.put("formatter", new Formatter());
        HashMap store = new HashMap();
        store.put("name", "SAI TARDERS NELLORE");
        store.put("url", "http://saitraders.net");
        context.put("store", store);
        return context;
    }
}

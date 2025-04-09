package ge.forms.etx.web.flex;

import ge.forms.etx.controllers.DatabaseService;
import ge.forms.etx.model.Country;
import ge.forms.etx.model.Inspection;
import ge.forms.etx.model.flex.CountrySkin;
import ge.forms.etx.model.flex.FaultSkin;
import ge.forms.etx.model.flex.InspectionSkin;
import ge.forms.etx.model.flex.SuccessSkin;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * General services servlet.
 * 
 * @author dimitri
 */
public class GeneralServlet extends HttpServlet {

    private static final long serialVersionUID = -3418899435269439858L;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    /**
	 * POST handling.
	 */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Element sendElement = null;
        Document doc = Utils.getDocument();
        DatabaseService service = Utils.getDatabaseService(getServletContext());
        try {
            String context = Utils.getString(req, "context");
            if ("sendmail".equals(context)) {
                sendElement = sendEmail(doc);
            } else if ("countries".equals(context)) {
                sendElement = doCountries(doc, service);
            } else if ("inspections".equals(context)) {
                sendElement = doInspections(doc, service);
            } else {
                Utils.generateUnknownContextException(context);
            }
        } catch (Exception ex) {
            sendElement = new FaultSkin().convert(doc, ex);
            Logger.getLogger(GeneralServlet.class.getName()).log(Level.WARNING, "Exception in GeneralServlet", ex);
        }
        OutputStream out = resp.getOutputStream();
        resp.setHeader("Content-Type", "text/xml; charset=UTF-8");
        Utils.writeElement(sendElement, out);
        out.flush();
        out.close();
    }

    /**
	 * Select countries list.
	 */
    private Element doCountries(Document doc, DatabaseService service) {
        List<Country> countries = service.getGeneralService().getCountries();
        Element root = doc.createElement("countries");
        CountrySkin skin = new CountrySkin();
        for (Country country : countries) root.appendChild(skin.convert(doc, country));
        return root;
    }

    /**
	 * Select inspections list.
	 */
    private Element doInspections(Document doc, DatabaseService service) {
        List<Inspection> inspections = service.getGeneralService().getInspections();
        Element root = doc.createElement("inspections");
        InspectionSkin skin = new InspectionSkin();
        for (Inspection inspection : inspections) root.appendChild(skin.convert(doc, inspection));
        return root;
    }

    private Element sendEmail(Document doc) {
        Properties config = new Properties();
        config.put("mail.host", "mail.forms.ge");
        config.put("mail.from", "info@forms.ge");
        config.put("mail.user", "info@forms.ge");
        config.put("mail.password", "erty2sami");
        Session session = Session.getDefaultInstance(config, null);
        MimeMessage message = new MimeMessage(session);
        try {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("dimakura@gmail.com"));
            message.setSubject("Test from Java");
            message.setText("This is a test message sent from java. Be happy!");
            Transport.send(message);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new SuccessSkin().convert(doc, "message sent");
    }
}

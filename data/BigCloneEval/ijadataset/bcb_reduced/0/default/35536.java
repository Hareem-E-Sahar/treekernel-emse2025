import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.Vector;
import java.util.Properties;
import javax.mail.*;
import javax.mail.Message.*;
import javax.mail.internet.*;
import javax.activation.*;

public class MailImageRenderer extends ImageRenderer {

    private String address;

    private Properties prop;

    public MailImageRenderer(Image background, Properties prop, String addr) {
        super(background);
        address = addr;
        this.prop = prop;
    }

    public MailImageRenderer(int width, int height, Properties prop, String addr) {
        super(width, height);
        address = addr;
        this.prop = prop;
    }

    private String renderToTempFile(Vector shapes) throws ImageRenderException {
        File f;
        Dimension size = getSize();
        BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        if (!render(shapes, g)) throw new ImageRenderException("Couldn't render image");
        try {
            f = File.createTempFile("whiteboard", ".png");
        } catch (IOException e) {
            throw new ImageRenderException("Couldn't create temporary file: " + e);
        }
        try {
            ImageIO.write(bi, "png", f);
        } catch (IOException e) {
            throw new ImageRenderException("Couldn't write image to temp file: " + e);
        }
        return f.getPath();
    }

    public boolean render(Vector shapes) throws ImageRenderException {
        String tempFile = renderToTempFile(shapes);
        Session session;
        MimeMessage msg;
        try {
            session = Session.getDefaultInstance(prop, null);
            msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(prop.getProperty("mail.from", "gh@mbl.is")));
            msg.addRecipient(RecipientType.TO, new InternetAddress(address));
            msg.setSubject(prop.getProperty("mail.subject", "Whiteboard image"));
            Multipart multipart = new MimeMultipart();
            BodyPart msgBody = new MimeBodyPart();
            msgBody.setText(prop.getProperty("mail.body", "This message contains your whiteboard image"));
            multipart.addBodyPart(msgBody);
            BodyPart msgImg = new MimeBodyPart();
            DataSource source = new FileDataSource(tempFile);
            msgImg.setDataHandler(new DataHandler(source));
            msgImg.setFileName("whiteboard.png");
            multipart.addBodyPart(msgImg);
            msg.setContent(multipart);
        } catch (MessagingException e) {
            throw new ImageRenderException("Couldn't initialize mail message: " + e);
        }
        try {
            Transport.send(msg);
        } catch (MessagingException e) {
            throw new ImageRenderException("Couldn't send message: " + e);
        }
        new File(tempFile).delete();
        return true;
    }
}

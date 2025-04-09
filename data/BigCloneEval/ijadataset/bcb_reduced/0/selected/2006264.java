package net.alinnistor.nk.visual.bricks;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.alinnistor.nk.domain.Context;
import net.alinnistor.nk.domain.User;
import net.alinnistor.nk.service.network.JVExecutable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:nad7ir@yahoo.com">Alin NISTOR</a>
 * @version 1.0 
 *  date: 28.10.2008 
 */
public class PrintScreen {

    private static final Log log = LogFactory.getLog(PrintScreen.class);

    private static int ORDER = 1;

    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static File takeAPictureAndSaveIt(int wait, String dir) {
        try {
            Thread.sleep(wait);
            boolean wasVisible = false;
            if (dir == null && Context.wind != null) {
                if (Context.wind.isVisible()) {
                    Context.wind.setVisible(false);
                    wasVisible = true;
                }
            }
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            String imagefile = "img" + ORDER++ + ".gif";
            if (dir != null) {
                imagefile = dir + "/snd" + ORDER + ".nk";
            }
            File file = new File(imagefile);
            ImageIO.write(image, "gif", file);
            if (dir == null && wasVisible && Context.wind != null) {
                Context.wind.setVisible(true);
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void takeSelectedPicture(ActionListener ctrl, User user) {
        SelectWind fsw = new SelectWind(ctrl, user);
        fsw.setVisible(true);
    }

    private static class SelectWind extends Window {

        private ImageArea imageArea;

        private Dimension dimScreenSize;

        private Rectangle rectScreenSize;

        private ActionListener ctrl;

        private User user;

        public SelectWind(final ActionListener ctrl, final User user) {
            super(new Frame());
            this.ctrl = ctrl;
            this.user = user;
            imageArea = new ImageArea();
            dimScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
            rectScreenSize = new Rectangle(dimScreenSize);
            initialize();
        }

        public void initialize() {
            BufferedImage biScreen = robot.createScreenCapture(rectScreenSize);
            imageArea.setImage(biScreen);
            add(imageArea);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setBounds(0, 0, screenSize.width, screenSize.height);
            imageArea.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    imageArea.crop();
                    dispose();
                    JPanel jptp = new JPanel() {

                        @Override
                        protected void paintComponent(Graphics g) {
                            g.drawImage(imageArea.getImage(), 0, 0, this);
                        }
                    };
                    BufferedImage bi = (BufferedImage) imageArea.getImage();
                    jptp.setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
                    String[] choices = { "Send Embedded", "Send", "Send/Save", "Save", "Delete" };
                    int response = JOptionPane.showOptionDialog(null, jptp, "The area you selected", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, "Send");
                    switch(response) {
                        case 0:
                            {
                                final File file = saveImageToFile(imageArea, "img" + ORDER++ + ".jpg");
                                ctrl.actionPerformed(new ActionEvent(new Object[] { user, file }, 77, Context.SEND_FILE_EMBEDDED));
                                break;
                            }
                        case 1:
                            {
                                final File file = saveImageToFile(imageArea, "img" + ORDER++ + ".jpg");
                                ctrl.actionPerformed(new ActionEvent(new Object[] { user, new File[] { file }, new JVExecutable() {

                                    @Override
                                    public void onFinish() {
                                        if (file != null) {
                                            file.delete();
                                            dialog.dispose();
                                        }
                                    }
                                } }, 77, Context.SEND_FILE));
                                break;
                            }
                        case 2:
                            {
                                final File file = saveImageToFile(imageArea, "img" + ORDER++ + ".jpg");
                                ctrl.actionPerformed(new ActionEvent(new Object[] { user, new File[] { file }, new JVExecutable() {

                                    @Override
                                    public void onFinish() {
                                        dialog.dispose();
                                    }
                                } }, 77, Context.SEND_FILE));
                                break;
                            }
                        case 3:
                            {
                                final File file = saveImageToFile(imageArea, "img" + ORDER++ + ".jpg");
                                break;
                            }
                        case 4:
                            {
                                final File file = saveImageToFile(imageArea, "img" + ORDER++ + ".jpg");
                                if (file != null) {
                                    file.delete();
                                }
                                break;
                            }
                        default:
                            JOptionPane.showMessageDialog(null, "Unexpected response " + response);
                    }
                }
            });
        }
    }

    private static File saveImageToFile(ImageArea imageArea, String imagefile) {
        final File file = new File(imagefile);
        ImageWriter writer = null;
        ImageOutputStream ios = null;
        try {
            Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
            if (!iter.hasNext()) {
                log.error("Unable to save image to jpeg file type.");
                return null;
            }
            writer = (ImageWriter) iter.next();
            ios = ImageIO.createImageOutputStream(file);
            writer.setOutput(ios);
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(0.95f);
            writer.write(null, new IIOImage((BufferedImage) imageArea.getImage(), null, null), iwp);
        } catch (IOException e2) {
            log.error("Unable to save image due to: " + e2);
        } finally {
            try {
                if (ios != null) {
                    ios.flush();
                    ios.close();
                }
                if (writer != null) {
                    writer.dispose();
                }
            } catch (IOException e2) {
                log.error("exception: " + e2);
            }
        }
        return file;
    }
}

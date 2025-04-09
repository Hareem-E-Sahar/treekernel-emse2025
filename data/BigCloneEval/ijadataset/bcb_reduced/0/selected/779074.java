package org.personalsmartspace.psw3p.pros;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.personalsmartspace.onm.api.pss3p.ICallbackListener;
import org.personalsmartspace.onm.api.pss3p.IMessageQueue;
import org.personalsmartspace.onm.api.pss3p.ONMException;
import org.personalsmartspace.onm.api.pss3p.ServiceMessage;
import org.personalsmartspace.psw3p.api.pros.IProjectorService;
import org.personalsmartspace.psw3p.projector.RoomProjectorService;
import org.personalsmartspace.sre.api.pss3p.IDigitalPersonalIdentifier;
import org.personalsmartspace.sre.api.pss3p.IServiceIdentifier;

/**
 * @author Perumal Kuppuudaiyar,email:pkudaiyar@users.sourceforge.net
 *
 */
public class ProjectorServiceImpl implements IProjectorService {

    /**
     * 
     */
    private IDigitalPersonalIdentifier consumerDPI;

    private IServiceIdentifier serviceId;

    private static ProjectorRemoteControl projRemCtrl;

    private IMessageQueue m_msgQue;

    public ProjectorServiceImpl() {
        System.out.println(" ProjectorServiceImpl startin now ");
    }

    public void showProjectorControl(IDigitalPersonalIdentifier consDPI, IServiceIdentifier servId) throws Exception {
        this.serviceId = servId;
        this.consumerDPI = consDPI;
        if (projRemCtrl == null) projRemCtrl = new ProjectorRemoteControl(this);
        projRemCtrl.createAndShow();
        if (this.serviceId != null) checkProjAccess(new PSWProjCallbackListener(projRemCtrl)); else projRemCtrl.updateAccesStatus("PSW Service not available");
    }

    public void hideProjectorControl() {
        if (projRemCtrl != null) {
            projRemCtrl.setVisible(false);
            projRemCtrl.dispose();
        }
    }

    public void shareScreen(double projW, double projH) {
        Robot robot;
        try {
            robot = new Robot();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            BufferedImage img = robot.createScreenCapture(screenRect);
            BufferedImage dst;
            double thisScrH = screenSize.height;
            double thisScrW = screenSize.width;
            double ratio = projW / projH;
            if (projW == thisScrW && projH == thisScrH) {
                dst = img;
            } else if ((ratio) == (thisScrH / thisScrW)) {
                double scaleW = projW / thisScrW;
                double scaleH = projH / thisScrH;
                BufferedImageOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scaleW, scaleH), new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
                dst = op.filter(img, null);
            } else {
                double h, w;
                if (Math.max(projW, projH) == projW) {
                    h = projW / ratio;
                    w = projW;
                } else {
                    w = projW * ratio;
                    h = projH;
                }
                double scaleW = w / thisScrW;
                double scaleH = h / thisScrH;
                BufferedImageOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scaleW, scaleH), new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
                dst = op.filter(img, null);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(dst, "gif", baos);
            } catch (IOException e) {
                throw new IllegalStateException(e.toString());
            }
            byte[] b = baos.toByteArray();
            baos.flush();
            new RoomProjectorService().displayScreen(b, "Perumal");
            sendScreenImage(b, new PSWProjCallbackListener(projRemCtrl));
        } catch (AWTException e) {
            System.out.println("AWTException while capturing screen");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception while capturing screen");
            e.printStackTrace();
        }
    }

    private void sendScreenImage(byte[] b, ICallbackListener listener) {
        ServiceMessage msg = new ServiceMessage("PSW Service Proxy", serviceId.toString(), serviceId.getOperatorId(), true, "sendScreenImage", true, new Object[] { this.consumerDPI.toUriString(), b }, new String[] { "java.lang.String", "java.lang.Byte" });
        try {
            m_msgQue.addServiceMessage(msg, listener);
        } catch (ONMException e) {
            e.printStackTrace();
        }
        checkProjAccess(new PSWProjCallbackListener(ProjectorServiceImpl.projRemCtrl));
    }

    public void stopSharing() {
        ServiceMessage msg = new ServiceMessage("PSW Service Proxy", serviceId.toString(), serviceId.getOperatorId(), true, "stopSharing", false, new String[] { consumerDPI.toUriString(), "Stop" }, new String[] { "java.lang.String", "java.lang.String" });
        try {
            m_msgQue.addServiceMessage(msg);
        } catch (ONMException e) {
            e.printStackTrace();
        }
        checkProjAccess(new PSWProjCallbackListener(ProjectorServiceImpl.projRemCtrl));
    }

    public void checkProjAccess(ICallbackListener listener) {
        ServiceMessage msg = new ServiceMessage("PSW Service Proxy", serviceId.toString(), serviceId.getOperatorId(), true, "checkProjAccess", true, new String[] { consumerDPI.toUriString() }, new String[] { "java.lang.String" });
        try {
            m_msgQue.addServiceMessage(msg, listener);
        } catch (ONMException e) {
            e.printStackTrace();
        }
    }
}

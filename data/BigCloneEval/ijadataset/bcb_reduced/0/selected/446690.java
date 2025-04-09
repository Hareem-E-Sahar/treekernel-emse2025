package unibg.overencrypt.client.managers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.apache.log4j.Logger;
import unibg.overencrypt.client.ClientConfiguration;
import unibg.overencrypt.client.LocalPrivateResource;
import unibg.overencrypt.client.view.IndefiniteProgressDialog;
import unibg.overencrypt.protocol.ClientPrimitives;
import unibg.overencrypt.protocol.OverEncryptRequest;
import unibg.overencrypt.protocol.ServerPrimitives;
import unibg.overencrypt.protocol.OverEncryptRequest.OverEncryptRequestType;
import unibg.overencrypt.utility.FileSystemUtils;

/**
 * Manage process to DH Key Pairs agreement:
 * 	public and private Diffie Hellman keys creation;
 *  save plain text PIN and userID in local private resource;
 *  wait for server response and prompt success or error.
 *
 * @author Flavio Giovarruscio & Riccardo Tribbia
 * @version 1.0
 */
public class DHKeyAgreementManager {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(DHKeyAgreementManager.class);

    /**
	 * Generate key pairs.
	 *
	 * @param path the actual path to communication with server throught .response / .request
	 * @param realm the server realm
	 * @param userId the logged user id
	 */
    public static void generateKeyPairs(String path, String realm, String userId) {
        JPasswordField pinField = new JPasswordField();
        JPasswordField pinConfirmField = new JPasswordField();
        Object[] obj = { "Create your private passphrase:", pinField, "and confirm this: ", pinConfirmField };
        boolean pinConfirmed = false;
        int times = 0;
        while (pinConfirmed == false && times < 3) {
            if (JOptionPane.showOptionDialog(null, obj, realm, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
                if (String.valueOf(pinField.getPassword()).equals(String.valueOf(pinConfirmField.getPassword()))) {
                    pinConfirmed = true;
                } else {
                    JOptionPane.showMessageDialog(null, "The 2 pass phrases must be the same!", "Error", JOptionPane.ERROR_MESSAGE);
                    times++;
                }
            } else {
                times++;
                JOptionPane.showMessageDialog(null, "Anonymous login is not yet developed", "Not allowed", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
        if (pinConfirmed) {
            JFrame frame = new JFrame();
            IndefiniteProgressDialog inst = new IndefiniteProgressDialog(frame, "Diffie Hellman Key Agreement", "Waiting for server response...", new DHKeyAgreementManager().new CancelListener(frame));
            inst.setVisible(true);
            String tempFilePath = ClientConfiguration.getLOCAL_PRIVATE_RESOURCES_PATH() + "/dhkeys.txt";
            dhKeyPairsCreation(tempFilePath, String.valueOf(pinField.getPassword()));
            LocalPrivateResource lpr = new LocalPrivateResource();
            String[] keys = { "pin", "userId" };
            String[] values = { String.valueOf(pinField.getPassword()), userId };
            try {
                lpr.saveLocal("UserInfo", keys, values);
            } catch (Exception e) {
                LOGGER.warn("Error during save info in local resources - " + e.getMessage());
            }
            String dhkeypairs = FileSystemUtils.readPrivateFile("dhkeys.txt");
            boolean requestDone = OverEncryptRequest.generateRequest(ClientPrimitives.OE_DHKEYPAIRS, OverEncryptRequestType.LOCK, userId, path, dhkeypairs, userId);
            if (requestDone) {
                String[] returnedValues = ResponseManager.retrieveCommunication(path, ServerPrimitives.OE_LOCK, "value");
                LOGGER.debug("returned values [0]: " + returnedValues[0]);
                if (!Boolean.parseBoolean(returnedValues[0])) {
                    JOptionPane.showMessageDialog(null, "The folder is locked. Please, retry later", "Locked", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
            OverEncryptRequest.generateRequest(ClientPrimitives.OE_DHKEYPAIRS, OverEncryptRequestType.CONTINUE, userId, path, dhkeypairs, userId);
            FileSystemUtils.deletePrivateFile("dhkeys.txt");
            String returned = ResponseManager.retrieveCommunication(path, ServerPrimitives.OE_SUCCESSFUL)[0];
            if (Boolean.parseBoolean(returned)) JOptionPane.showMessageDialog(null, "All operations completed successfully", "DH Key agreement completed", JOptionPane.INFORMATION_MESSAGE);
            OverEncryptRequest.generateRequest(ClientPrimitives.OE_DHKEYPAIRS, OverEncryptRequestType.UNLOCK, userId, path, userId, path, dhkeypairs, userId);
        } else if (times >= 3) {
            JOptionPane.showMessageDialog(null, "Three time error. Access denied", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * Diffie-Hellman key pairs creation.
	 */
    private static void dhKeyPairsCreation(String filePath, String phrase) {
        try {
            String[] command = { ClientConfiguration.getEXECUTABLES_PATH() + "/wpes2_linux", "dh", phrase, filePath };
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * The listener interface for receiving cancel events.
	 * The class that is interested in processing a cancel
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addCancelListener<code> method. When
	 * the cancel event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see CancelEvent
	 */
    private class CancelListener implements ActionListener {

        /** The frame. */
        private JFrame frame;

        /**
		 * Instantiates a new cancel listener.
		 *
		 * @param frame the frame
		 */
        public CancelListener(JFrame frame) {
            this.frame = frame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            frame.dispose();
            System.exit(-1);
        }
    }
}

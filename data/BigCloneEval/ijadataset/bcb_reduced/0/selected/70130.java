package unibg.overencrypt.client;

import java.io.IOException;
import org.apache.log4j.Logger;
import unibg.overencrypt.protocol.ClientPrimitives;
import unibg.overencrypt.protocol.Response;
import unibg.overencrypt.utility.FileSystemUtils;
import unibg.overencrypt.utility.SecurityAlgorithms;

public class AuthenticationClient {

    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(AuthenticationClient.class);

    private OverEncryptClient client = new OverEncryptClient();

    public Response checkPin(String path, String userId, String pin) {
        Response resp = client.sendAndWait(ClientPrimitives.OE_PASSPHRASE, path, SecurityAlgorithms.doubleMd5(pin), userId);
        LocalPrivateResource lpr = new LocalPrivateResource();
        try {
            lpr.set("UserInfo", "userId", userId);
            lpr.set("UserInfo", "pin", SecurityAlgorithms.encryptAES(pin));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    public Response generateKeyPairs(String path, String userId, String pin) {
        String tempFilePath = Configuration.LOCAL_PRIVATE_RESOURCES_PATH + "/dhkeys.txt";
        try {
            String[] command = { Configuration.EXECUTABLES_PATH + "/wpes2_linux", "dh", pin, tempFilePath };
            Runtime.getRuntime().exec(command);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        LocalPrivateResource lpr = new LocalPrivateResource();
        try {
            lpr.set("UserInfo", "userId", userId);
            lpr.set("UserInfo", "pin", SecurityAlgorithms.encryptAES(pin));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String dhkeypairs = FileSystemUtils.readPrivateFile("dhkeys.txt");
        FileSystemUtils.deletePrivateFile("dhkeys.txt");
        return client.sendAndWait(ClientPrimitives.OE_DHKEYPAIRS, path, dhkeypairs, userId);
    }

    public Response logout(String path) {
        String userId = null;
        LocalPrivateResource lpr = new LocalPrivateResource();
        try {
            userId = lpr.get("UserInfo", "userId");
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug(userId);
        client.send(ClientPrimitives.OE_LOGOUT, path, userId);
        Response response = client.readResponse(path);
        return response;
    }
}

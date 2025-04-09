package gnupg;

import java.io.*;
import utils.*;

public class GPGHandler {

    private String pathGPG;

    private String pathKeyringPublic;

    private String pathKeyringSecret;

    private String pathTempDir;

    public GPGHandler(String GPGpath, String pathKeyringPublic, String pathKeyringSecret, String pathTempDir) {
        setPathGPG(GPGpath);
        setPathKeyringPublic(pathKeyringPublic);
        setPathKeyringSecret(pathKeyringSecret);
        setPathTempDir(pathTempDir);
    }

    public void generateKeypair() throws IOException, InterruptedException {
        Rand.initialize();
        String tempKeyringPublic;
        String tempKeyringSecret;
        String rand;
        do {
            rand = Rand.getString(10, "ABCDEF0123456789");
            tempKeyringPublic = pathTempDir + "/" + rand + ".pub";
            tempKeyringSecret = pathTempDir + "/" + rand + ".sec";
        } while (FileOperation.exists(tempKeyringPublic) || FileOperation.exists(tempKeyringSecret));
        if (!FileOperation.exists(pathKeyringPublic)) FileOperation.create(pathKeyringPublic);
        if (!FileOperation.exists(pathKeyringSecret)) FileOperation.create(pathKeyringSecret);
        String name = "OPP Autokey [" + rand + "]";
        String keyInfo = "Key-Type: DSA\n" + "Key-Length: 1024\n" + "Subkey-Type: ELG-E\n" + "Subkey-Length: 2048\n" + "Expire-Date: 0\n" + "Name-Real: " + name + "\n" + "%pubring " + tempKeyringPublic + "\n" + "%secring " + tempKeyringSecret + "\n" + "%commit\n" + "%echo " + name + " created!\n";
        runGnuPG(pathGPG + " --batch --gen-key", keyInfo, Stream.STDOUT);
        String sPublicKeyCode = runGnuPG(pathGPG + " -a --no-default-keyring --keyring " + tempKeyringPublic + " --export", "", Stream.STDOUT);
        String sSecretKeyCode = runGnuPG(pathGPG + " -a --no-default-keyring --secret-keyring " + tempKeyringSecret + " --export-secret-keys", "", Stream.STDOUT);
        importKey(sPublicKeyCode);
        importKey(sSecretKeyCode);
        FileOperation.delete(tempKeyringPublic);
        FileOperation.delete(tempKeyringSecret);
    }

    public String importKey(String keyCode) throws IOException, InterruptedException {
        String keyInfo = runGnuPG(createExecuteCommand() + " --fingerprint --import", keyCode, Stream.STDERR);
        String keyID = keyInfo.split("\n")[0].split(": key ")[1].split(":")[0];
        runGnuPG(createExecuteCommand() + " --sign-key " + keyID, "", Stream.STDOUT);
        return keyID;
    }

    private String createExecuteCommand() {
        return pathGPG + " --no-default-keyring" + " --secret-keyring " + pathKeyringSecret + " --keyring " + pathKeyringPublic + " -a";
    }

    public void setPathTempDir(String pathTempDir) {
        this.pathTempDir = pathTempDir;
    }

    public String getPathTempDir() {
        return pathTempDir;
    }

    public void setPathGPG(String pathGPG) {
        this.pathGPG = pathGPG;
    }

    public String getPathGPG() {
        return pathGPG;
    }

    public void setPathKeyringPublic(String pathKeyringPublic) {
        this.pathKeyringPublic = pathKeyringPublic;
    }

    public void setPathKeyringSecret(String pathKeyringSecret) {
        this.pathKeyringSecret = pathKeyringSecret;
    }

    public String getPathKeyringPublic() {
        return pathKeyringPublic;
    }

    public String getPathKeyringSecret() {
        return pathKeyringSecret;
    }

    public String encrypt(String text, String recipientID) throws IOException, InterruptedException {
        return runGnuPG(createExecuteCommand() + " --batch --yes --always-trust -e -r " + recipientID, text, Stream.STDOUT);
    }

    public String decrypt(String text, String privateKeyId) throws IOException, InterruptedException {
        return runGnuPG(createExecuteCommand() + " -d --default-key " + privateKeyId, text, Stream.STDOUT);
    }

    public String encryptSign(String text, String recipientID, String privateKeyId) throws IOException, InterruptedException {
        return runGnuPG(createExecuteCommand() + " --batch --yes --always-trust -s -e -r " + recipientID + " --default-key " + privateKeyId, text, Stream.STDOUT);
    }

    public String sign(String text, String privateKeyId) throws IOException, InterruptedException {
        return runGnuPG(createExecuteCommand() + " -s --default-key " + privateKeyId, text, Stream.STDOUT);
    }

    private String runGnuPG(String cmd, String input, Stream stream) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        ProcessStreamReader in;
        if (stream == Stream.STDERR) in = new ProcessStreamReader(p.getErrorStream()); else in = new ProcessStreamReader(p.getInputStream());
        in.start();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        out.write(input);
        out.close();
        p.waitFor();
        in.join();
        return in.getString();
    }

    public GPGKey[] getPublicKeys() throws IOException, InterruptedException {
        String str = runGnuPG(createExecuteCommand() + " --list-public-keys --fingerprint", "", Stream.STDOUT);
        String[] keyInfo = str.split("\n\n");
        GPGKey[] keys = new GPGKey[keyInfo.length];
        for (int i = 0; i < keyInfo.length; ++i) {
            String[] keyDetail = keyInfo[i].split("\n");
            int iKeyid = 0;
            int iFingerprint = 1;
            int iUid = 2;
            if (i == 0) {
                iKeyid += 2;
                iFingerprint += 2;
                iUid += 2;
            }
            String sKeyId = keyDetail[iKeyid].split("/")[1].split(" ")[0];
            String sFingerprint = keyDetail[iFingerprint].split(" = ")[1].trim();
            String sUid = keyDetail[iUid].substring(3).trim();
            keys[i] = new GPGKey(sKeyId, sFingerprint, sUid, getPublicKeyCode(sKeyId));
        }
        return keys;
    }

    private String getPublicKeyCode(String keyId) throws IOException, InterruptedException {
        return runGnuPG(createExecuteCommand() + " --export " + keyId, "", Stream.STDOUT);
    }
}

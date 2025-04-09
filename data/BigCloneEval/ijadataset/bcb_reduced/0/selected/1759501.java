package server;

import java.io.File;
import java.io.LineNumberReader;

public final class DeleteGameCommand extends AbstractCommand {

    @Override
    public final void doIt(LineNumberReader in, String[] cmd, javax.mail.internet.MimeMessage msg) {
        beforeDoIt(msg);
        int result = BAD_RESULT;
        switch(cmd.length) {
            case 3:
                String pass = cmd[2];
                if (!OGSserver.getProperties().getProperty("Server.Password").equals(pass)) {
                    System.out.println("Bad server password - " + pass);
                    break;
                }
                String gName = cmd[1];
                File gDir = new File("games" + File.separator + gName);
                if (!gDir.exists()) {
                    System.out.println("File not found - " + gName);
                    break;
                }
                deleteDir(gDir);
                if (!gDir.delete()) {
                    System.err.println("Can't delete directory - " + gDir.toString());
                    break;
                }
                result = OK_RESULT;
                setGameMindAddress(answer);
                break;
            default:
                break;
        }
        afterDoIt(cmd, result);
    }

    private static void deleteDir(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) deleteDir(file);
            if (!file.delete()) System.err.println("Can't delete file (dir) - " + file.toString());
        }
    }
}

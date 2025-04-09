import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import java.io.*;

public class KonuskanEngine {

    private Syntax.VoiceType voiceDB;

    private Parser parser;

    public KonuskanEngine(String source, Syntax.VoiceType voiceDB) {
        this.voiceDB = voiceDB;
        parser = new Parser(source.toLowerCase());
        speak();
    }

    public void speak() {
        String osName = System.getProperty("os.name");
        String[] cmd = new String[3];
        parser.getFullText().read();
        if (osName.equals("Windows NT") || osName.equals("Windows XP")) {
            cmd[0] = "cmd.exe";
            cmd[1] = "/C";
            cmd[2] = "space\\mbrola.exe space\\" + voiceDB.getFileName() + " space\\temp.pho space\\temp.au";
        } else if (osName.equals("Windows 95")) {
            cmd[0] = "command.com";
            cmd[1] = "/C";
            cmd[2] = "space\\mbrola.exe space\\" + voiceDB.getFileName() + " space\\temp.pho space\\temp.au";
        } else if (osName.equals("Linux")) {
            cmd[0] = "/bin/sh";
            cmd[1] = "-c";
            cmd[2] = "space/mbrola-linux-i386 space/" + voiceDB.getFileName() + " space/temp.pho space/temp.au";
        } else {
            System.out.println("Unsupported OS");
            System.exit(1);
        }
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec(cmd);
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            proc.waitFor();
            InputStream in = new FileInputStream("space" + File.separator + "temp.au");
            AudioStream as = new AudioStream(in);
            AudioPlayer.player.start(as);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        String inputString = "";
        int voiceType = 0;
        if (args.length != 2) {
            System.out.println("Usage : KonuskanEngine \"text\" voiceType");
            System.out.println("\ttext : Input string which will be voiced");
            System.out.println("\tvoiceType : Type 1 for male and 2 or female voice");
            System.exit(1);
        }
        if (args[0].trim().length() <= 0) {
            System.out.println("Please enter an input text");
            System.exit(1);
        } else {
            inputString = args[0].trim();
        }
        try {
            if (Integer.parseInt(args[1].trim()) < 1 || Integer.parseInt(args[1].trim()) > 2) {
                System.out.println("Please enter voiceType properly");
                System.out.println("\tvoiceType : Type 1 for male and 2 or female voice");
                System.exit(1);
            } else {
                voiceType = Integer.parseInt(args[1].trim());
            }
        } catch (Exception e) {
            System.out.println("Please enter voiceType properly");
            System.out.println("\tvoiceType : Type 1 for male and 2 or female voice");
            System.exit(1);
        }
        switch(voiceType) {
            case 1:
                new KonuskanEngine(inputString, Syntax.VoiceType.Male);
                break;
            case 2:
                new KonuskanEngine(inputString, Syntax.VoiceType.Female);
                break;
            default:
                System.out.println("Invalid voiceType");
        }
    }

    class StreamGobbler extends Thread {

        InputStream is;

        String type;

        StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) System.out.println(type + ">" + line);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}

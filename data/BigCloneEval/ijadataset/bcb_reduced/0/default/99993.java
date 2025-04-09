import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class NoMuleThread extends Thread {

    private String url;

    private JTextField tUrl;

    private JLabel lStatus;

    private JButton bDownload;

    private String sOutputProfile;

    private String strDest = "";

    public void setUrl(JTextField tUrl) {
        this.url = tUrl.getText();
        this.tUrl = tUrl;
    }

    public void setStatusLabel(JLabel lStatus) {
        this.lStatus = lStatus;
    }

    public void setOutputProfile(String sOutputProfile) {
        this.sOutputProfile = sOutputProfile;
    }

    public void setButton(JButton bDownload) {
        this.bDownload = bDownload;
    }

    public void end() {
        if (!strDest.equals("")) {
            System.out.println("Deleting");
            File f = new File(strDest);
            f.delete();
        }
        bDownload.setText("Download");
        bDownload.setEnabled(true);
        lStatus.setText("Done");
        tUrl.setText("");
    }

    public void run() {
        boolean found = false;
        for (MediaProvider mediaProvider : NoMule.lMediaProvider) {
            if (mediaProvider.MatchURLPattern(url)) {
                if (!isInterrupted()) {
                    found = true;
                    lStatus.setText(mediaProvider.getName());
                    lStatus.repaint();
                }
                String sMediaUrl = "";
                if (!isInterrupted()) sMediaUrl = mediaProvider.getMediaURL(url);
                if (!isInterrupted()) {
                    for (int i = 0; i < 255; i++) {
                        strDest = Integer.toString(i) + ".flv";
                        File f = new File(strDest);
                        if (!f.exists()) break;
                    }
                }
                if (!isInterrupted()) {
                    HTTP.download(sMediaUrl, strDest, lStatus);
                    lStatus.setText("save");
                    lStatus.repaint();
                }
                JFileChooser jfc = null;
                int rval = 0;
                if (!isInterrupted()) {
                    jfc = new JFileChooser(url + " save as");
                    rval = jfc.showSaveDialog(null);
                }
                if (!isInterrupted()) {
                    if (rval == JFileChooser.APPROVE_OPTION) {
                        String strFileDest = jfc.getSelectedFile().getAbsolutePath();
                        strFileDest = strFileDest.replace("\\", "/");
                        System.out.println(strFileDest);
                        try {
                            RandomAccessFile in = new RandomAccessFile(sOutputProfile + ".op", "r");
                            String command = in.readLine();
                            in.close();
                            command = command.replaceAll("\\$I", strDest);
                            command = command.replaceAll("\\$O", strFileDest);
                            System.out.println("Converting using output profile");
                            System.out.println(command);
                            System.out.println("Running " + command);
                            Process p = Runtime.getRuntime().exec(command);
                            StreamEater seErr = new StreamEater(p.getErrorStream());
                            StreamEater seOut = new StreamEater(p.getInputStream());
                            seErr.start();
                            seOut.start();
                            p.waitFor();
                            System.out.println("done");
                            lStatus.setText("Done");
                            lStatus.repaint();
                        } catch (FileNotFoundException e) {
                            JOptionPane.showMessageDialog(null, "Outputprofile " + sOutputProfile + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                            System.out.println("Outputprofile " + sOutputProfile + " not found.");
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Error while reading outputprofile.", "Error", JOptionPane.ERROR_MESSAGE);
                            System.out.println("Error while reading outputprofile.");
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                end();
            }
        }
        if (!found) {
            end();
            JOptionPane.showMessageDialog(null, "Could not find service for url:" + url, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

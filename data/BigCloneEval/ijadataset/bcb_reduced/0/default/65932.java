import java.io.*;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

/**
 *
 * @author Thewinator
 */
public class RenderWorker extends SwingWorker<ImageIcon, Void> {

    private String flame, previewname;

    private Renderer control;

    private RenderQueuer invoker;

    private int type;

    private boolean pending;

    RenderWorker(String flame, Renderer control, RenderQueuer invoker, int type) {
        this.flame = flame;
        this.control = control;
        this.invoker = invoker;
        this.type = type;
        this.pending = true;
    }

    public boolean isPending() {
        return pending;
    }

    @Override
    protected ImageIcon doInBackground() throws Exception {
        this.pending = false;
        Settings.recursiveValidate();
        if (type == 0) {
            ImageIcon img = renderThumb(flame);
            invoker.recieveRender(img, type);
        } else {
            renderPreview(flame);
            invoker.recieveRenderFile(new File(previewname), type);
        }
        control.done(this);
        return null;
    }

    private void renderPreview(String flame) {
        java.util.Date today = new java.util.Date();
        previewname = Settings.previewFolder + today.getTime() + ".jpg";
        render(flame, previewname);
        new File(previewname).deleteOnExit();
    }

    private ImageIcon renderThumb(String flame) {
        java.util.Date today = new java.util.Date();
        String thumbname = Settings.previewFolder + today.getTime() + ".jpg";
        render(flame, thumbname);
        ImageIcon img = new ImageIcon(thumbname);
        Utils.delete(new File(thumbname));
        return img;
    }

    private void render(String flame, String filename) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(Settings.flam3Folder + "flam3-render.exe");
            Map<String, String> env = pb.environment();
            env.put("out", filename);
            env.put("format", "jpg");
            Process p = pb.start();
            OutputStream os = p.getOutputStream();
            BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            PrintWriter pw = new PrintWriter(os);
            pw.print(flame);
            pw.flush();
            pw.close();
            os.close();
            er.close();
            in.close();
            p.waitFor();
            p.destroy();
        } catch (InterruptedException ex) {
            Debugger.storeException(ex);
        } catch (IOException ex) {
            Debugger.storeException(ex);
        }
    }
}

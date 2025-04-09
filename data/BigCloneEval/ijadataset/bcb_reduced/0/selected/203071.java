package quizcards;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.zip.*;
import javax.swing.JComponent;

public class ImageSide extends Side implements ImageObserver {

    private static SoundImage left_pic = new SoundImage();

    private static SoundImage right_pic = new SoundImage();

    private static MediaTracker tracker = new MediaTracker(left_pic);

    public Image image;

    private String image_file = "";

    private SoundImage pic;

    private String sound_file = "";

    public ImageSide(boolean left_side) {
        pic = left_side ? left_pic : right_pic;
    }

    public JComponent get() {
        if (image == null) loadImage();
        pic.setImage(image, image_file);
        pic.setSound(sound_file);
        return pic;
    }

    public char getType() {
        return IMAGE;
    }

    public boolean imageUpdate(Image im, int flags, int x, int y, int width, int height) {
        if ((flags & (ImageObserver.FRAMEBITS | ImageObserver.ALLBITS)) > 0) return false;
        return true;
    }

    private void loadImage() {
        if (image_file.length() == 0) return;
        File f = new File(image_file);
        byte buf[] = new byte[(int) f.length()];
        try {
            new FileInputStream(f).read(buf);
        } catch (IOException e) {
            System.out.println(e);
        }
        image = QuizCards.t.createImage(buf);
        tracker.addImage(image, 1);
        try {
            tracker.waitForID(1);
        } catch (InterruptedException e2) {
        }
        tracker.removeImage(image);
    }

    public boolean playSound() {
        return pic.playSound();
    }

    public void read(int version) {
        String s;
        while (XML.readElement(element, contents)) {
            s = element.toString();
            if (s.equals("image_file")) image_file = contents.toString(); else sound_file = contents.toString();
        }
    }

    public void readFiles(ZipInputStream zis, DataInputStream dis) {
        if (image_file.length() == 0) return;
        try {
            zis.getNextEntry();
            int w = dis.readInt();
            int h = dis.readInt();
            int l = dis.readInt();
            int a[] = new int[l];
            for (int i = 0; i < l; i++) a[i] = dis.readInt();
            image = QuizCards.t.createImage(new MemoryImageSource(w, h, a, 0, w));
            tracker.addImage(image, 1);
            try {
                tracker.waitForID(1);
            } catch (InterruptedException e2) {
            }
            tracker.removeImage(image);
            zis.closeEntry();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public boolean set() {
        boolean changed = false;
        if (0 != image_file.compareTo(pic.imageFile)) {
            image_file = pic.imageFile;
            image = pic.image;
            changed = true;
        }
        if (0 != sound_file.compareTo(pic.soundFile)) {
            sound_file = pic.soundFile;
            changed = true;
        }
        return changed;
    }

    public void setEditMode(boolean edit_mode) {
        pic.setEditMode(edit_mode);
    }

    public void setFocusable(boolean focusable) {
        pic.setFocusable(focusable);
    }

    public void setSoundFlag(boolean sound_flag) {
        pic.setSoundFlag(sound_flag);
    }

    public void setVisible(boolean visible) {
        pic.setVisible(visible);
    }

    public void write() {
        XML.startElement("side", "type", "image");
        XML.writeElement("image_file", image_file);
        XML.writeElement("sound_file", sound_file);
    }

    public void writeFiles(ZipOutputStream zos, DataOutputStream dos) {
        if (image_file.length() == 0) return;
        try {
            if (image == null) loadImage();
            int w = image.getWidth(this);
            int h = image.getHeight(this);
            PixelGrabber grabber = new PixelGrabber(image, 0, 0, -1, -1, true);
            try {
                grabber.grabPixels();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            zos.putNextEntry(new ZipEntry(image_file));
            dos.writeInt(w);
            dos.writeInt(h);
            int a[] = (int[]) grabber.getPixels();
            dos.writeInt(a.length);
            for (int i = 0; i < a.length; i++) dos.writeInt(a[i]);
            zos.closeEntry();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

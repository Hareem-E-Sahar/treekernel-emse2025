package info.benjaminhill.clip2png;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Copyright (c) 2007, Benjamin Hill All rights reserved. Redistribution and use
 * in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: Redistributions of source
 * code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of the benjaminhill.info nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * @author Benjamin Hill
 */
public class Clip2Image extends Thread {

    public Clip2Image(final String format) {
        if (!ImageIO.getImageWritersByFormatName(format).hasNext()) {
            throw new IllegalArgumentException("Unable to write to image format: " + format);
        }
        this.format = format;
    }

    private static final String CLIPBOARD = "clipboard";

    private static final String DESKTOP = "Desktop";

    private static final String SCREEN = "screen";

    private static final String WINDOWS = "Windows";

    public boolean tryClipboard = true;

    protected BufferedImage bi;

    protected String format;

    /**
	 * If the clipboard contains an image, return the image as a BufferedImage
	 * 
	 * @return BufferedImage of the clipboard
	 */
    static BufferedImage getClipboardImage() {
        final Toolkit kit = Toolkit.getDefaultToolkit();
        final Clipboard clipboard = kit.getSystemClipboard();
        final Transferable clipData = clipboard.getContents(clipboard);
        if (clipData.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                return (BufferedImage) clipData.getTransferData(DataFlavor.imageFlavor);
            } catch (final UnsupportedFlavorException e) {
            } catch (final IOException e) {
            }
        }
        return null;
    }

    /**
	 * Return the screen image as a BufferedImage
	 * 
	 * @return BufferedImage of the screen
	 * @throws AWTException
	 * @throws IOException
	 *             Problem grabbing the image
	 */
    static BufferedImage getScreenImage() throws AWTException {
        final Toolkit kit = Toolkit.getDefaultToolkit();
        final Rectangle rec = new Rectangle(kit.getScreenSize());
        final Robot rob = new Robot();
        return rob.createScreenCapture(rec);
    }

    protected static File getFilePath(final String prefix, final String suffix) {
        final String fileName = prefix + "_" + System.currentTimeMillis() + "." + suffix;
        final String home = System.getProperty("user.home");
        final File destination;
        if (home == null || home.trim().length() < 1) {
            destination = new File(File.separator);
        } else if (System.getProperty("os.name").indexOf(Clip2Image.WINDOWS) > -1) {
            destination = new File(home.trim() + File.separator + Clip2Image.DESKTOP + File.separator + fileName);
        } else {
            destination = new File(home.trim());
        }
        return destination;
    }

    @Override
    public void run() {
        try {
            String source = null;
            if (this.tryClipboard) {
                source = Clip2Image.CLIPBOARD;
                this.bi = Clip2Image.getClipboardImage();
            }
            if (this.bi == null) {
                source = Clip2Image.SCREEN;
                this.bi = Clip2Image.getScreenImage();
            }
            final File destination = Clip2Image.getFilePath(source, this.format);
            ImageIO.write(this.bi, this.format, destination);
        } catch (final Throwable e) {
            JOptionPane.showMessageDialog(null, "Unable to capture clipboard or screen\n(" + e.getLocalizedMessage() + ")", "clip2png", JOptionPane.WARNING_MESSAGE);
        }
    }
}

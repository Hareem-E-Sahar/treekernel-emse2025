package org.eoti.rei.ndfcmd;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.*;

public class PrintCmd extends DetailCmd {

    protected File tmpFile;

    protected PrintWriter out;

    public String[] commands() {
        return new String[] { "print" };
    }

    public void detail(int houseNum) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            format("'print' is not supported on this platofrm.\n");
            return;
        }
        tmpFile = File.createTempFile("NDFBrowser", ".txt");
        tmpFile.deleteOnExit();
        out = new PrintWriter(tmpFile);
        super.detail(houseNum);
        out.flush();
        out.close();
        Desktop.getDesktop().print(tmpFile);
        tmpFile = null;
    }

    public void format(String fmt, Object... args) {
        if (out == null) super.format(fmt, args); else out.format(fmt, args);
    }

    public void displayHelp() {
        if (!Desktop.isDesktopSupported()) {
            format("'print' is not supported on this platofrm.\n");
            return;
        }
        format("print: list available houses and prompt for house to print\n");
        format("print houseNumber: print information for house\n");
    }
}

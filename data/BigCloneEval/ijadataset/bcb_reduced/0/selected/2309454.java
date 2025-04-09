package jm2pc.server.service;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.io.OutputStream;
import java.util.Date;
import com.jm2pc.Command;
import jm2pc.server.i18n.Messages;
import jm2pc.server.log.Loggable;
import jm2pc.server.utils.DateFormat;
import jm2pc.utils.Constants;

public class Screen implements Command {

    private Control control;

    private Rectangle screenSize;

    private int cliWidth;

    private int cliHeight;

    private Loggable log;

    private String dscClient;

    private DataFormat dataFormat;

    public Screen(Loggable log, String dscClient, Control control, DataFormat dataFormat, OutputStream out) {
        this.log = log;
        this.dscClient = dscClient;
        this.control = control;
        this.dataFormat = dataFormat;
        screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        cliWidth = dataFormat.getClienteWidth();
        cliHeight = dataFormat.getClienteHeight();
    }

    public Object execute(String args) throws Exception {
        String[] param = args.split(" ");
        String tipo = param[0];
        int tam = 0;
        if (tipo.equals(Constants.CMD_TELA_TP_INT)) {
            tam = printScreen();
        } else if (tipo.equals(Constants.CMD_TELA_TP_XY)) {
            int x = Integer.parseInt(param[1]);
            int y = Integer.parseInt(param[2]);
            int zoom = Integer.parseInt(param[3]);
            tam = printScreen(x, y, zoom);
        }
        log.logSucess(DateFormat.format(new Date()) + " -> " + dscClient + " " + Messages.getMessage("capturingScreen") + "... " + tam + " bytes");
        return null;
    }

    public String getName() {
        return Constants.CMD_TELA;
    }

    public String getVersion() {
        return "OEM";
    }

    public int printScreen() {
        Image img = control.createScreenCapture(screenSize);
        Image scaledImg = img.getScaledInstance(cliWidth, cliHeight, Image.SCALE_AREA_AVERAGING);
        return dataFormat.formatAndSend(scaledImg);
    }

    public int printScreen(int x, int y, int zoom) {
        Image img = control.createScreenCapture(new Rectangle(x, y, cliWidth * zoom, cliHeight * zoom));
        Image scaledImg = img.getScaledInstance(cliWidth, cliHeight, BufferedImage.SCALE_AREA_AVERAGING);
        return dataFormat.formatAndSend(scaledImg);
    }
}

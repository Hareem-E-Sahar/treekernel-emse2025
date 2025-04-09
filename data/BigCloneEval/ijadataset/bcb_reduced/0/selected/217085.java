package net.sf.groofy.player;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import net.sf.groofy.logger.GroofyLogger;
import org.eclipse.swt.widgets.Composite;
import com.google.code.jspot.Track;

public abstract class AbstractUiPlayer extends Composite implements IPlayer {

    public AbstractUiPlayer(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    public void playSpotifySong(Track track) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(track.getId()));
            } catch (IOException e) {
                GroofyLogger.getInstance().logException(e);
            } catch (URISyntaxException e) {
                GroofyLogger.getInstance().logException(e);
            }
        }
    }
}

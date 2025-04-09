package net.sf.groofy.listeners;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import net.sf.groofy.logger.GroofyLogger;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * @author agomez (abelgomez@users.sourceforge.net)
 *
 */
public class LinkSelectionListener implements SelectionListener {

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(e.text));
            } catch (IOException e1) {
                GroofyLogger.getInstance().logException(e1);
            } catch (URISyntaxException e1) {
                GroofyLogger.getInstance().logException(e1);
            }
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }
}

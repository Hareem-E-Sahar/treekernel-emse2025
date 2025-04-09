package de.sonivis.tool.mwapiconnector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import de.sonivis.tool.core.SONIVISCore;
import de.sonivis.tool.mediawikiconnector.MediaWikiActivator;

/**
 * @author Benedikt Meuthrath
 * @version $Revision$, $Date$
 */
public class EHandler extends AbstractHandler {

    private Clip clickClip;

    @Override
    public void dispose() {
        clickClip = null;
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            clickClip = AudioSystem.getClip();
            URL clipUrl = SONIVISCore.findFileInBundle(MediaWikiActivator.PLUGIN_ID, "doc/Ente.wav").toURI().toURL();
            AudioInputStream ais = AudioSystem.getAudioInputStream(clipUrl);
            clickClip.open(ais);
            clickClip.start();
        } catch (LineUnavailableException e) {
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (UnsupportedAudioFileException e) {
        }
        return null;
    }
}

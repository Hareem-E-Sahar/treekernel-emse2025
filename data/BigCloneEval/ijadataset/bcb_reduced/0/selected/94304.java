package com.peterhi.client.ui;

import java.io.IOException;
import java.util.ResourceBundle;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TestVoiceDialogEx_Output extends Composite {

    private static ResourceBundle bundle = Grabber.grabBundle(TestVoiceDialogEx_Output.class);

    private CLabel outputImage;

    private Composite composite;

    private Label inst;

    private Button play;

    private Clip clip;

    public TestVoiceDialogEx_Output(Composite parent, int style) {
        super(parent, style);
        setLayout(Util.gridLayout(5, 5, 5, 5, 2));
        outputImage = new CLabel(this, SWT.NONE);
        outputImage.setImage(new Image(getDisplay(), Grabber.grabResource("arts-audio-manager-128x128.png")));
        outputImage.setLayoutData(Util.gridData(GridData.FILL, GridData.FILL, false, true, -1, -1));
        composite = new Composite(this, SWT.NONE);
        composite.setLayout(Util.gridLayout(5, 5, 5, 5));
        composite.setLayoutData(Util.gridData());
        inst = new Label(composite, SWT.WRAP);
        inst.setText("Click on the button below and listen to a short clip. If you can hear it, click 'Next', otherwise click 'Back' and select a different device.");
        inst.setLayoutData(Util.gridData(GridData.FILL, GridData.FILL, true, false, -1, 80));
        play = new Button(composite, SWT.TOGGLE);
        play.setText("Play Clip");
        play.setLayoutData(Util.gridData(GridData.CENTER, GridData.CENTER, true, false, -1, 30));
        play.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (play.getSelection()) {
                    startPlay();
                } else {
                    endPlay();
                }
            }
        });
    }

    private void startPlay() {
        try {
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(Grabber.grabResource("test.wav")));
            clip.loop(-1);
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void endPlay() {
        if (clip != null) {
            clip.stop();
            clip.drain();
            clip.flush();
            clip.close();
            clip = null;
        }
    }
}

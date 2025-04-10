package ch.laoe.plugin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import ch.laoe.clip.AClip;
import ch.laoe.clip.AClipSelection;
import ch.laoe.clip.ALayerSelection;
import ch.laoe.clip.MMArray;
import ch.laoe.operation.AOPitchGenerator;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipEditor;
import ch.laoe.ui.GClipLayerChooser;
import ch.laoe.ui.GControlTextA;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.LProgressViewer;
import ch.laoe.ui.LWorker;
import ch.laoe.ui.Laoe;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.control.UiControlText;

/***********************************************************

This file is part of LAoE.

LAoE is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published
by the Free Software Foundation; either version 2 of the License,
or (at your option) any later version.

LAoE is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with LAoE; if not, write to the Free Software Foundation,
Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


Class:			GPPitchGenerator
Autor:			olivier g�umann, neuch�tel (switzerland)
JDK:				1.3

Desctription:	plugin for pitch generator.  

History:
Date:			Description:									Autor:
25.06.01		first draft										oli4

***********************************************************/
public class GPPitchGenerator extends GPluginFrame {

    public GPPitchGenerator(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    public String getName() {
        return "pitchGenerator";
    }

    private JButton createBaseSignalConst;

    private UiControlText constPitch;

    private JButton applyConst;

    private JButton createBaseSignalVar;

    private GClipLayerChooser layerChooser;

    private JButton applyVar;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel pConst = new JPanel();
        UiCartesianLayout clt = new UiCartesianLayout(pConst, 10, 5);
        clt.setPreferredCellSize(new Dimension(25, 35));
        pConst.setLayout(clt);
        clt.add(new JLabel(GLanguage.translate("pitch")), 0, 0, 4, 1);
        constPitch = new GControlTextA(10, true, true);
        constPitch.setDataRange(-100, 100);
        constPitch.setData(1);
        clt.add(constPitch, 4, 0, 6, 1);
        createBaseSignalConst = new JButton(GLanguage.translate("new"));
        clt.add(createBaseSignalConst, 1, 4, 4, 1);
        applyConst = new JButton(GLanguage.translate("apply"));
        clt.add(applyConst, 5, 4, 4, 1);
        tabbedPane.add(GLanguage.translate("constant"), pConst);
        JPanel pVar = new JPanel();
        UiCartesianLayout clv = new UiCartesianLayout(pVar, 10, 5);
        clv.setPreferredCellSize(new Dimension(25, 35));
        pVar.setLayout(clv);
        layerChooser = new GClipLayerChooser(Laoe.getInstance(), "pitchCurve");
        clv.add(layerChooser, 0, 0, 10, 3);
        createBaseSignalVar = new JButton(GLanguage.translate("new"));
        clv.add(createBaseSignalVar, 1, 4, 4, 1);
        applyVar = new JButton(GLanguage.translate("apply"));
        clv.add(applyVar, 5, 4, 4, 1);
        tabbedPane.add(GLanguage.translate("f(time)"), pVar);
        frame.getContentPane().add(tabbedPane);
        pack();
        eventDispatcher = new EventDispatcher();
        applyConst.addActionListener(new LWorker(eventDispatcher));
        applyVar.addActionListener(new LWorker(eventDispatcher));
        createBaseSignalConst.addActionListener(eventDispatcher);
        createBaseSignalVar.addActionListener(eventDispatcher);
    }

    public void reload() {
        super.reload();
        layerChooser.reload();
        constPitch.refresh();
    }

    private class EventDispatcher implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == applyConst) {
                LProgressViewer.getInstance().entrySubProgress(getName());
                Debug.println(1, "plugin " + getName() + " [apply const] clicked");
                onApplyConst();
                updateHistory(GLanguage.translate(getName()));
                LProgressViewer.getInstance().exitSubProgress();
                reloadFocussedClipEditor();
                autoCloseNow();
            } else if (e.getSource() == applyVar) {
                LProgressViewer.getInstance().entrySubProgress(getName());
                Debug.println(1, "plugin " + getName() + " [apply var] clicked");
                onApplyVar();
                updateHistory(GLanguage.translate(getName()));
                LProgressViewer.getInstance().exitSubProgress();
                reloadFocussedClipEditor();
                autoCloseNow();
            } else if (e.getSource() == createBaseSignalConst) {
                Debug.println(1, "plugin " + getName() + " [create base signal const] clicked");
                onCreateBaseSignal();
            } else if (e.getSource() == createBaseSignalVar) {
                Debug.println(1, "plugin " + getName() + " [create base signal var] clicked");
                onCreateBaseSignal();
            }
        }
    }

    private GClipEditor baseSignalClipEditor;

    private void onCreateBaseSignal() {
        AClip c = new AClip(1, 1, 1000);
        c.setName("<" + GLanguage.translate("pitchGeneratorBaseSignal") + ">");
        c.setSampleRate(getFocussedClip().getSampleRate());
        Laoe.getInstance().addClipFrame(c);
        baseSignalClipEditor = getFocussedClipEditor();
    }

    private void onApplyConst() {
        if (baseSignalClipEditor != null) {
            MMArray sig = baseSignalClipEditor.getClip().getLayer(0).getChannel(0).getSamples();
            ALayerSelection ls = getFocussedClip().getSelectedLayer().getSelection();
            ls.operateEachChannel(new AOPitchGenerator(sig, (float) constPitch.getData()));
        }
    }

    private void onApplyVar() {
        if (baseSignalClipEditor != null) {
            MMArray sig = baseSignalClipEditor.getClip().getLayer(0).getChannel(0).getSamples();
            AClipSelection cs = new AClipSelection(new AClip());
            cs.addLayerSelection(getFocussedClip().getSelectedLayer().getSelection());
            cs.addLayerSelection(layerChooser.getSelectedLayer().createSelection());
            cs.operateLayer0WithLayer1(new AOPitchGenerator(sig));
        }
    }
}

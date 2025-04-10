package ch.laoe.plugin;

import ch.laoe.clip.AChannel;
import ch.laoe.clip.AChannelSelection;
import ch.laoe.clip.ALayer;
import ch.laoe.ui.GLanguage;

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


Class:			GPSelectAfter
Autor:			olivier g�umann, neuch�tel (switzerland)
JDK:				1.3

Desctription:	plugin to select the area after the actual
					selection.

History:
Date:			Description:									Autor:
03.03.02		first draft										oli4

***********************************************************/
public class GPSelectAfter extends GPlugin {

    public GPSelectAfter(GPluginHandler ph) {
        super(ph);
    }

    public String getName() {
        return "selectAfter";
    }

    public void start() {
        super.start();
        ALayer l = getFocussedClip().getSelectedLayer();
        for (int i = 0; i < l.getNumberOfChannels(); i++) {
            AChannel ch = l.getChannel(i);
            AChannelSelection s = ch.getSelection();
            int o = s.getOffset() + s.getLength();
            ch.modifySelection(o, ch.getSampleLength() - o);
        }
        updateHistory(GLanguage.translate(getName()));
        repaintFocussedClipEditor();
    }
}

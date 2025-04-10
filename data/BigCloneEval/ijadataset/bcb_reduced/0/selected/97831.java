package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;
import ch.laoe.clip.MMArray;

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


Class:			AOConstGenerator
Autor:			olivier g�umann, neuch�tel (switzerland)
JDK:				1.3

Desctription:	generate a constant signal

History:
Date:			Description:									Autor:
28.04.01		first draft										oli4

***********************************************************/
public class AOConstGenerator extends AOperation {

    /**
   *	constructor
   */
    public AOConstGenerator(float offset, boolean add) {
        super();
        this.offset = offset;
        this.add = add;
    }

    private float offset;

    private boolean add;

    /**
	*	performs the segments generation
	*/
    public void operate(AChannelSelection ch1) {
        MMArray s1 = ch1.getChannel().getSamples();
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        ch1.getChannel().markChange();
        for (int i = o1; i < o1 + l1; i++) {
            if (add) {
                s1.set(i, ch1.mixIntensity(i, s1.get(i), s1.get(i) + offset));
            } else {
                s1.set(i, ch1.mixIntensity(i, s1.get(i), offset));
            }
        }
    }
}

package org.geometerplus.zlibrary.text.view;

import java.util.ArrayList;

final class ZLTextElementAreaVector extends ArrayList<ZLTextElementArea> {

    private static final long serialVersionUID = -7880472347947563506L;

    final ArrayList<ZLTextElementRegion> ElementRegions = new ArrayList<ZLTextElementRegion>();

    private ZLTextElementRegion myCurrentElementRegion;

    @Override
    public void clear() {
        ElementRegions.clear();
        myCurrentElementRegion = null;
        super.clear();
    }

    @Override
    public boolean add(ZLTextElementArea area) {
        final ZLTextHyperlink hyperlink = area.Style.Hyperlink;
        if (hyperlink.Id != null) {
            if (!(myCurrentElementRegion instanceof ZLTextHyperlinkRegion) || ((ZLTextHyperlinkRegion) myCurrentElementRegion).Hyperlink != hyperlink) {
                myCurrentElementRegion = new ZLTextHyperlinkRegion(hyperlink, this, size());
                ElementRegions.add(myCurrentElementRegion);
            } else {
                myCurrentElementRegion.extend();
            }
        } else if (area.Element instanceof ZLTextImageElement) {
            ElementRegions.add(new ZLTextImageRegion((ZLTextImageElement) area.Element, this, size()));
            myCurrentElementRegion = null;
        } else if (area.Element instanceof ZLTextWord && ((ZLTextWord) area.Element).isAWord()) {
            if (!(myCurrentElementRegion instanceof ZLTextWordRegion) || ((ZLTextWordRegion) myCurrentElementRegion).Word != area.Element) {
                myCurrentElementRegion = new ZLTextWordRegion((ZLTextWord) area.Element, this, size());
                ElementRegions.add(myCurrentElementRegion);
            } else {
                myCurrentElementRegion.extend();
            }
        } else {
            myCurrentElementRegion = null;
        }
        return super.add(area);
    }

    ZLTextElementArea binarySearch(int x, int y) {
        int left = 0;
        int right = size();
        while (left < right) {
            final int middle = (left + right) / 2;
            final ZLTextElementArea candidate = get(middle);
            if (candidate.YStart > y) {
                right = middle;
            } else if (candidate.YEnd < y) {
                left = middle + 1;
            } else if (candidate.XStart > x) {
                right = middle;
            } else if (candidate.XEnd < x) {
                left = middle + 1;
            } else {
                return candidate;
            }
        }
        return null;
    }
}

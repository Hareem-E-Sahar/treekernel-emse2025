package org.nakedobjects.viewer.skylark.special;

public class ScrollBar {

    private int maximum;

    private int minimum;

    private int scrollPosition = 0;

    private int visibleAmount;

    public ScrollBar() {
        super();
    }

    public void setPostion(final int position) {
        scrollPosition = Math.min(position, maximum);
        scrollPosition = Math.max(scrollPosition, minimum);
    }

    public void firstClick(int x, boolean alt) {
        if (alt) {
            setPostion(x - visibleAmount / 2);
        } else {
            if (x < scrollPosition) {
                setPostion(scrollPosition - visibleAmount);
            } else if (x > scrollPosition + visibleAmount) {
                setPostion(scrollPosition + visibleAmount);
            }
        }
    }

    public int getMaximum() {
        return maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getPosition() {
        return scrollPosition;
    }

    public int getVisibleAmount() {
        return visibleAmount;
    }

    public void limit() {
        if (scrollPosition > maximum) {
            scrollPosition = maximum;
        }
    }

    public void reset() {
        scrollPosition = 0;
    }

    public boolean isOnThumb(int pos) {
        return pos > scrollPosition && pos < scrollPosition + visibleAmount;
    }

    public void setSize(int viewportSize, int contentSize) {
        visibleAmount = contentSize == 0 ? 0 : (viewportSize * viewportSize / contentSize);
        maximum = viewportSize - visibleAmount;
    }

    public void secondClick(int y) {
        int midpoint = (maximum + visibleAmount) / 2;
        setPostion(y < midpoint ? minimum : maximum);
    }
}

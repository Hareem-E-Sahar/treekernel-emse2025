package fr.inria.zvtm.treemap;

/** 
 * This layout uses a static binary tree for
 * dividing map items. It is not great with regard to
 * aspect ratios or ordering, but it has excellent
 * stability properties.
 */
public class BinaryTreeLayout extends AbstractMapLayout {

    public void layout(Mappable[] items, Rect bounds, Insets insets) {
        layout(items, 0, items.length - 1, bounds, insets);
    }

    public void layout(Mappable[] items, int start, int end, Rect bounds, Insets insets) {
        layout(items, start, end, bounds, true, insets);
    }

    public void layout(Mappable[] items, int start, int end, Rect bounds, boolean vertical, Insets insets) {
        if (start > end) return;
        if (start == end) {
            items[start].setBounds(bounds);
            return;
        }
        int mid = (start + end) / 2;
        double total = sum(items, start, end);
        double first = sum(items, start, mid);
        double a = first / total;
        double x = bounds.x, y = bounds.y, w = bounds.w, h = bounds.h;
        if (vertical) {
            Rect b1 = new Rect(x, y, w * a, h);
            Rect b2 = new Rect(x + w * a, y, w * (1 - a), h);
            layout(items, start, mid, b1, !vertical, insets);
            layout(items, mid + 1, end, b2, !vertical, insets);
        } else {
            Rect b1 = new Rect(x, y, w, h * a);
            Rect b2 = new Rect(x, y + h * a, w, h * (1 - a));
            layout(items, start, mid, b1, !vertical, insets);
            layout(items, mid + 1, end, b2, !vertical, insets);
        }
    }

    private double normAspect(double a, double b) {
        return Math.max(a / b, b / a);
    }

    private double sum(Mappable[] items, int start, int end) {
        double s = 0;
        for (int i = start; i <= end; i++) s += items[i].getSize();
        return s;
    }

    private int findMax(Mappable[] items, int start, int end) {
        double m = 0;
        int n = -1;
        for (int i = start; i <= end; i++) {
            double s = items[i].getSize();
            if (s >= m) {
                m = s;
                n = i;
            }
        }
        return n;
    }

    public String getName() {
        return "Binary Tree";
    }

    public String getDescription() {
        return "Uses a static binary tree layout.";
    }
}

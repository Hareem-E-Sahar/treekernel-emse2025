public class Test {    public void layout(Mappable[] items, int start, int end, Rect bounds, boolean vertical, Insets insets) {
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
}
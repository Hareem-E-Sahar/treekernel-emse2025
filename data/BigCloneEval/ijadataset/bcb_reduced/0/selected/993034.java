package ups.MapParse;

public class PixelCache {

    private Pixel[] arrayCache;

    public int length = 0;

    private int MAX_COLORS = 1024;

    public PixelCache() {
        arrayCache = new Pixel[MAX_COLORS];
    }

    public Pixel getPixel(int pixelValue) {
        Pixel p = search(pixelValue);
        if (p == null) {
            p = new Pixel(pixelValue);
            insert(p);
        }
        return p;
    }

    private Pixel search(int pixVal) {
        int low = 0;
        int high = length - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (arrayCache[mid].intValue > pixVal) {
                high = mid - 1;
            } else if (arrayCache[mid].intValue < pixVal) {
                low = mid + 1;
            } else {
                return arrayCache[mid];
            }
        }
        return null;
    }

    private void insert(Pixel pix) {
        int i = length - 1;
        while (i >= 0 && arrayCache[i].intValue > pix.intValue) {
            arrayCache[i + 1] = arrayCache[i];
            i--;
        }
        arrayCache[i + 1] = pix;
        length++;
    }
}

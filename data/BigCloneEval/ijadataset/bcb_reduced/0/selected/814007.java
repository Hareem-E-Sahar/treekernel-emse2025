package image.chunks;

public class ViewChunkIterator {

    private ViewChunk chunk;

    private int counter;

    private int width;

    private int modulo;

    private int a, c;

    public ViewChunkIterator(ViewChunk chunk) {
        this.chunk = chunk;
        counter = -1;
        width = chunk.getWidth();
        modulo = chunk.getWidth() * chunk.getHeight() - 1;
        a = modulo + 1;
        c = getC();
    }

    public byte next() {
        counter++;
        return chunk.getByte(counter % width, counter / width);
    }

    public void writeToCurrent(byte info) {
        chunk.setByte(counter % width, counter / width, info);
    }

    public boolean HasNext() {
        if (counter + 1 > modulo) return false; else return true;
    }

    public byte generateNext() {
        if (counter <= 7) counter = 8;
        do {
            counter = nextLCNumber(counter);
        } while (counter <= 7);
        return chunk.getByte(counter % width, counter / width);
    }

    public void skip(int count) {
        counter += count;
    }

    private int nextLCNumber(int previos) {
        return ((a * previos + c) % modulo);
    }

    private int getC() {
        c = modulo / 20;
        while ((NOD(c, modulo) != 1)) {
            c--;
        }
        return c;
    }

    private int NOD(int x, int y) {
        if (y == 0) {
            return x;
        } else {
            return NOD(y, x % y);
        }
    }
}

public class Test {    public int searchULong(int startIndex, int startOffset, int endIndex, int endOffset, int length, int key) {
        int location = 0;
        int bottom = 0;
        int top = length;
        while (top != bottom) {
            location = (top + bottom) / 2;
            int locationStart = this.readULongAsInt(startIndex + location * startOffset);
            if (key < locationStart) {
                top = location;
            } else {
                int locationEnd = this.readULongAsInt(endIndex + location * endOffset);
                if (key <= locationEnd) {
                    return location;
                }
                bottom = location + 1;
            }
        }
        return -1;
    }
}
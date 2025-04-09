package lpg;

class Token {

    void resetInfoAndSetLocation(InputFileSymbol fileSymbol, int startLocation) {
        this.fileSymbol = fileSymbol;
        assert (startLocation >= 0 && startLocation <= 0x00FFFFFF);
        info = (((long) startLocation) << 8);
        symbol = null;
        endLocation = startLocation;
    }

    int startLocation() {
        return (int) (info >> 8);
    }

    int endLocation() {
        return endLocation;
    }

    void setEndLocation(int endLoc) {
        endLocation = endLoc;
    }

    void setKind(int kind) {
        assert (kind >= 0 && kind <= 0x0000003F);
        info = (info & 0xFFFFFFC0) | kind;
    }

    int kind() {
        return (int) (info & 0x0000003F);
    }

    String fileName() {
        return fileSymbol.name();
    }

    int line() {
        return findLine(fileSymbol.lineLocation(), startLocation());
    }

    int endLine() {
        return findLine(fileSymbol.lineLocation(), endLocation());
    }

    int column() {
        return findColumn(fileSymbol, startLocation());
    }

    int endColumn() {
        return findColumn(fileSymbol, endLocation());
    }

    InputFileSymbol fileSymbol() {
        return fileSymbol;
    }

    Symbol getSymbol() {
        return symbol;
    }

    void setSymbol(Symbol symbol) {
        this.symbol = symbol;
        if (symbol.location() == null) symbol.setLocation(this);
    }

    static int findLine(IntArrayList lineLocation, int location) {
        int lo = 0;
        int hi = lineLocation.size() - 1;
        if (hi == 0) return 0;
        do {
            int mid = lo + (hi - lo) / 2;
            if (lineLocation.get(mid) == location) return mid;
            if (lineLocation.get(mid) < location) lo = mid + 1; else hi = mid - 1;
        } while (lo < hi);
        return (lineLocation.get(lo) > location ? lo - 1 : lo);
    }

    static int findColumn(InputFileSymbol fileSymbol, int location) {
        if (fileSymbol.buffer() == null) fileSymbol.readInput();
        int index = findLine(fileSymbol.lineLocation(), location);
        return (index == 0 ? 0 : Tab.strlen(fileSymbol.buffer(), fileSymbol.lineLocation(index), location));
    }

    private long info;

    private Symbol symbol;

    private int rightBrace;

    private int endLocation;

    private InputFileSymbol fileSymbol;
}

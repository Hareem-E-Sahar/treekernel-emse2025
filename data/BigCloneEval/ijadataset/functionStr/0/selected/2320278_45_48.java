public class Test {    protected void update() {
        baseLevel = minLevel + (maxLevel - minLevel) / 2;
        fireModelChanged();
    }
}
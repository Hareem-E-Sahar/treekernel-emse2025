public class Test {        private void calcCurrentDrop() {
            currentDrop = minDropRate + RANDOM.nextInt(maxDropRate - minDropRate);
        }
}
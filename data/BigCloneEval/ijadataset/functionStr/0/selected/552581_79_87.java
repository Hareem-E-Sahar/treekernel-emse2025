public class Test {    public ThreadChoiceFromSet randomize() {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            ThreadInfo tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
        return this;
    }
}
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RunSorter {

    private static int maxArraySize = 200;

    private static boolean printArrays = false;

    public static void main(String[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InterruptedException {
        Sorter s = new Sorter();
        s.quickSort(new int[] { 0 });
        int arraySize = maxArraySize;
        if (args.length > 0) {
            arraySize = Integer.parseInt(args[0]);
        }
        int[] master = getMasterCopy(arraySize);
        run("quickSort", (int[]) master.clone(), s);
        run("insertionSort", (int[]) master.clone(), s);
        run("selectionSort", (int[]) master.clone(), s);
        run("bubbleSort", (int[]) master.clone(), s);
    }

    private static int[] getMasterCopy(int size) {
        System.out.println("loading random array of " + size + " elements");
        int[] master = new int[size];
        for (int i = 0; i < master.length; i++) {
            master[i] = (int) (Math.random() * 100);
        }
        return master;
    }

    private static void run(String alg, int[] array, Sorter s) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InterruptedException {
        Thread.sleep(3000);
        System.out.print("running " + alg + " ... ");
        if (printArrays) System.out.print(arrayToString(array, true));
        Method m = s.getClass().getMethod(alg, int[].class);
        long startTime = System.currentTimeMillis();
        m.invoke(s, array);
        double elapsed = ((double) (System.currentTimeMillis() - startTime) / 1000);
        System.out.println("took " + elapsed + " sec. ");
        if (printArrays) System.out.println(" -> " + arrayToString(array, true));
    }

    private static String arrayToString(Object array, boolean withBrackets) {
        StringBuffer sb = new StringBuffer();
        if (array == null) return "";
        if (array.getClass().isArray()) {
            int len = Array.getLength(array);
            if (withBrackets) sb.append("{");
            for (int j = 0; j < len; j++) {
                Object value = Array.get(array, j);
                if (value != null) {
                    sb.append(value.toString());
                } else {
                    sb.append("(null)");
                }
                if (j < len - 1) sb.append(',');
            }
            if (withBrackets) sb.append("}");
        } else {
            throw new IllegalArgumentException("not a array!");
        }
        return sb.toString();
    }
}

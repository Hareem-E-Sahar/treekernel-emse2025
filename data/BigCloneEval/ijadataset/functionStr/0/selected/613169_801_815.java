public class Test {    private static void mergeConfigurationArray(Configuration[] a, int fromIndex, int toIndex) {
        int i, j, k, middle, n;
        n = toIndex - fromIndex;
        Configuration[] b = new Configuration[n];
        k = 0;
        middle = (fromIndex + toIndex) / 2;
        for (i = fromIndex; i < middle; i++) b[k++] = a[i];
        for (j = toIndex - 1; j >= middle; j--) b[k++] = a[j];
        i = 0;
        j = n - 1;
        k = fromIndex;
        while (i <= j) {
            if (b[i].getPid().compareTo(b[j].getPid()) < 0) a[k++] = b[i++]; else a[k++] = b[j--];
        }
    }
}
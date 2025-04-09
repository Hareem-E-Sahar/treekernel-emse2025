import java.util.StringTokenizer;
import java.io.BufferedReader;

public class Heap {

    int[] heap;

    String[] comments;

    int lastUsedIndex;

    public Heap(int max) {
        heap = new int[max + 1];
        lastUsedIndex = 0;
    }

    public Heap(BufferedReader reader) throws Exception {
        reader.readLine();
        int hLength = lastUsedIndex = Integer.parseInt(reader.readLine().trim());
        heap = new int[hLength];
        comments = new String[hLength];
        reader.readLine();
        for (int i = 0; i < hLength; i++) {
            String nextLine = "";
            while (nextLine.length() == 0) nextLine = reader.readLine().trim();
            StringTokenizer strtok = new StringTokenizer(nextLine);
            if (strtok.countTokens() > 0) {
                strtok.nextToken();
                heap[i] = Integer.parseInt(strtok.nextToken());
                comments[i] = "";
                while (strtok.hasMoreTokens()) comments[i] = comments[i] + strtok.nextToken() + " ";
            }
        }
    }

    public int changeSize(int newMax) {
        if (newMax <= heap.length) return -1;
        int[] temp = new int[newMax];
        String[] ctemp = new String[newMax];
        for (int i = 0; i < heap.length; i++) {
            temp[i] = heap[i];
            ctemp[i] = comments[i];
        }
        heap = temp;
        comments = ctemp;
        return 1;
    }

    public int fetchPointer(int obj, int var) {
        return heap[(obj + 2) + var];
    }

    public void storePointer(int obj, int var, int val) {
        heap[(obj + 2) + var] = val;
    }

    public int fetchClassOf(int obj) {
        return heap[obj + 1];
    }

    public int fetchSizeOf(int obj) {
        return heap[obj];
    }

    public int fetchDictOf(int cla) {
        return heap[cla + 3];
    }

    public int addCompiledMethod(int cla, int method) {
        for (int i = fetchDictOf(cla); i < lastUsedIndex; i++) if (heap[i] == 0) {
            heap[i] = method;
            return i;
        }
        return -1;
    }

    public int createObject(int cla, int size) {
        if (lastUsedIndex + size > heap.length) return -1;
        heap[lastUsedIndex + 1] = size;
        heap[lastUsedIndex + 2] = cla;
        lastUsedIndex = lastUsedIndex + size;
        return lastUsedIndex - (size - 1);
    }

    public int createClass(int size, int meta, int sup, int dict, int inSpec, int nVars, int nMeths) {
        if (lastUsedIndex + size > heap.length) return -1;
        int temp = lastUsedIndex + 1;
        heap[temp] = size;
        heap[temp + 1] = meta;
        heap[temp + 2] = sup;
        heap[temp + 3] = dict;
        heap[temp + 4] = inSpec;
        heap[temp + 5] = nVars;
        heap[temp + 6] = nMeths;
        lastUsedIndex = lastUsedIndex + size;
        return temp;
    }

    public int createMetaClass(int size, int Meta, int supMeta, int dict, int inSpec, int nVars, int cMeths) {
        if (lastUsedIndex + size > heap.length) return -1;
        int temp = lastUsedIndex + 1;
        heap[temp] = size;
        heap[temp + 1] = Meta;
        heap[temp + 2] = supMeta;
        heap[temp + 3] = dict;
        heap[temp + 4] = inSpec;
        heap[temp + 5] = nVars;
        heap[temp + 6] = cMeths;
        lastUsedIndex = lastUsedIndex + size;
        return temp;
    }

    public int createCompiledMethod(CompiledMethod meth) {
        int temp = lastUsedIndex + 1;
        int[] method = meth.pack();
        if (lastUsedIndex + method.length > heap.length) changeSize((int) (heap.length * 1.5));
        System.arraycopy(method, 0, heap, temp, method.length);
        lastUsedIndex = temp + method.length - 1;
        return temp;
    }

    public int createCompiledMethod(int size, int cla, int nArgs, int nTVars, int nLits, int pIndex, int[] litFrame, int[] byteCodes) {
        if (lastUsedIndex + size > heap.length) return -1;
        int temp = lastUsedIndex + 1;
        heap[temp] = size;
        heap[temp + 1] = cla;
        heap[temp + 2] = nArgs;
        heap[temp + 3] = nTVars;
        heap[temp + 4] = nLits;
        heap[temp + 5] = pIndex;
        int fTemp = temp = 6;
        for (int i = 0; i < litFrame.length; i++) {
            heap[fTemp] = litFrame[i];
            fTemp++;
        }
        for (int i = 0; i < byteCodes.length; i++) {
            heap[fTemp] = byteCodes[i];
            fTemp++;
        }
        lastUsedIndex = fTemp - 1;
        return temp;
    }

    public void trimHeap() {
        int[] temp = new int[lastUsedIndex];
        String[] tcom = new String[lastUsedIndex];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = heap[i];
            tcom[i] = comments[i];
        }
        heap = temp;
        comments = tcom;
    }

    public String toString() {
        String output = "Existing heap length:\n" + heap.length + "\nHeap:\n";
        for (int i = 0; i < heap.length; i++) {
            output += String.valueOf(i + 1) + "\t" + String.valueOf(heap[i]) + "\t" + String.valueOf(comments[i]) + "\n";
        }
        return output;
    }
}

package cn.nkjobsearch.publicClass;

public class SearchAndSort {

    /**
	 * 二分查找
	 */
    public static int binarySearch(int[] a, int key) {
        int low = 0;
        int high = a.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int midVal = a[mid];
            if (midVal < key) low = mid + 1; else if (midVal > key) high = mid - 1; else return mid;
        }
        return -(low + 1);
    }

    /**
	 * 普通的线性查找，当int[]a无序的时候使用
	 */
    public static int normalSearch(int[] a, int key) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == key) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * 快速排序
	 * @param int [] pData 数据
	 * @param int [] pDataNum id号
	 * @param int left 左起排序索引号
	 * @param int right 右起排序索引号
	 * @param boolean asc 升序?降序
	 * */
    public static void QuickSort(int[] pData, int[] pDataNum, int left, int right, boolean asc) {
        int i, j;
        int iTemp;
        int middle, intTemp;
        i = left;
        j = right;
        middle = pData[(left + right) / 2];
        if (asc) {
            do {
                while ((pData[i] < middle) && (i < right)) i++;
                while ((pData[j] > middle) && (j > left)) j--;
                if (i <= j) {
                    intTemp = pData[i];
                    pData[i] = pData[j];
                    pData[j] = intTemp;
                    iTemp = pDataNum[i];
                    pDataNum[i] = pDataNum[j];
                    pDataNum[j] = iTemp;
                    i++;
                    j--;
                }
            } while (i <= j);
        } else {
            do {
                while ((pData[i] > middle) && (i < right)) i++;
                while ((pData[j] < middle) && (j > left)) j--;
                if (i <= j) {
                    intTemp = pData[i];
                    pData[i] = pData[j];
                    pData[j] = intTemp;
                    iTemp = pDataNum[i];
                    pDataNum[i] = pDataNum[j];
                    pDataNum[j] = iTemp;
                    i++;
                    j--;
                }
            } while (i <= j);
        }
        if (left < j) QuickSort(pData, pDataNum, left, j, asc);
        if (right > i) QuickSort(pData, pDataNum, i, right, asc);
    }

    /**
	 * 快速排序String
	 * */
    public static void QuickSort(String[] pData, int[] pDataNum, int left, int right) {
        int i, j;
        int iTemp;
        String middle, strTemp;
        i = left;
        j = right;
        middle = pData[(left + right) / 2];
        do {
            while ((pData[i].compareTo(middle) < 0) && (i < right)) i++;
            while ((pData[j].compareTo(middle)) > 0 && (j > left)) j--;
            if (i <= j) {
                strTemp = pData[i];
                pData[i] = pData[j];
                pData[j] = strTemp;
                iTemp = pDataNum[i];
                pDataNum[i] = pDataNum[j];
                pDataNum[j] = iTemp;
                i++;
                j--;
            }
        } while (i <= j);
        if (left < j) QuickSort(pData, pDataNum, left, j);
        if (right > i) QuickSort(pData, pDataNum, i, right);
    }

    /**
	 * @return 针对第indexOfData维度排序后的多位数组
	 * @param pData 待排序的多位数组
	 * @param indexOfData 待排序的维度
	 * @param 数组每一维的宽度
	 * @param int left 左起排序索引号
	 * @param int right 右起排序索引号
	 * @param boolean asc 升序?降序
	 * 适合多维数组的排序，示例如下:<br/>
	 	int[][] t = {{222,111,444},{20,10,40},{2,1,4},{22,11,44}};
		cn.nkjobsearch.publicClass.Print.print(t);
		cn.nkjobsearch.publicClass.SearchAndSort.QuickSort(t, 0, 3, 0, 3, false);
		cn.nkjobsearch.publicClass.Print.print(t);
	 * */
    public static void QuickSort(int[][] pData, int indexOfData, int dimensions, int left, int right, boolean asc) {
        int i, j;
        int middle;
        int temp;
        i = left;
        j = right;
        middle = pData[(left + right) / 2][indexOfData];
        if (asc) {
            do {
                while ((pData[i][indexOfData] < middle) && (i < right)) i++;
                while ((pData[j][indexOfData] > middle) && (j > left)) j--;
                if (i <= j) {
                    for (int k = 0; k < dimensions; k++) {
                        temp = pData[i][k];
                        pData[i][k] = pData[j][k];
                        pData[j][k] = temp;
                    }
                    i++;
                    j--;
                }
            } while (i <= j);
        } else {
            do {
                while ((pData[i][indexOfData] > middle) && (i < right)) i++;
                while ((pData[j][indexOfData] < middle) && (j > left)) j--;
                if (i <= j) {
                    for (int k = 0; k < dimensions; k++) {
                        temp = pData[i][k];
                        pData[i][k] = pData[j][k];
                        pData[j][k] = temp;
                    }
                    i++;
                    j--;
                }
            } while (i <= j);
        }
        if (left < j) QuickSort(pData, indexOfData, dimensions, left, j, asc);
        if (right > i) QuickSort(pData, indexOfData, dimensions, i, right, asc);
    }
}

package com.be.table;

public class Sorter {

    int[] fSortOrder;

    public int[] sortInt(int[] pArray, int[] pSortOrder, boolean up) {
        fSortOrder = pSortOrder;
        sort(pArray, 0, pArray.length - 1, up);
        return fSortOrder;
    }

    private void sort(int[] a, int lo0, int hi0, boolean up) {
        int lo = lo0;
        int hi = hi0;
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        sort(a, lo, mid, up);
        sort(a, mid + 1, hi, up);
        int end_lo = mid;
        int start_hi = mid + 1;
        while ((lo <= end_lo) && (start_hi <= hi)) {
            if (up) {
                if (compareInt(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) <= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            } else {
                if (compareInt(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) >= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            }
        }
    }

    private int compareInt(int a, int b) {
        if (a > b) {
            return 1;
        } else if (a < b) {
            return -1;
        } else {
            return 0;
        }
    }

    public int[] sortShort(short[] pArray, int[] pSortOrder, boolean up) {
        fSortOrder = pSortOrder;
        sort(pArray, 0, pArray.length - 1, up);
        return fSortOrder;
    }

    private void sort(short[] a, int lo0, int hi0, boolean up) {
        int lo = lo0;
        int hi = hi0;
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        sort(a, lo, mid, up);
        sort(a, mid + 1, hi, up);
        int end_lo = mid;
        int start_hi = mid + 1;
        while ((lo <= end_lo) && (start_hi <= hi)) {
            if (up) {
                if (compareShort(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) <= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            } else {
                if (compareShort(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) >= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            }
        }
    }

    public int[] sortLong(long[] pArray, int[] pSortOrder, boolean up) {
        fSortOrder = pSortOrder;
        sort(pArray, 0, pArray.length - 1, up);
        return fSortOrder;
    }

    private void sort(long[] a, int lo0, int hi0, boolean up) {
        int lo = lo0;
        int hi = hi0;
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        sort(a, lo, mid, up);
        sort(a, mid + 1, hi, up);
        int end_lo = mid;
        int start_hi = mid + 1;
        while ((lo <= end_lo) && (start_hi <= hi)) {
            if (up) {
                if (compareLong(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) <= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            } else {
                if (compareLong(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) >= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            }
        }
    }

    private int compareShort(short a, short b) {
        if (a > b) {
            return 1;
        } else if (a < b) {
            return -1;
        } else {
            return 0;
        }
    }

    private int compareLong(long a, long b) {
        if (a > b) {
            return 1;
        } else if (a < b) {
            return -1;
        } else {
            return 0;
        }
    }

    public int[] sortFloat(float[] pArray, int[] pSortOrder, boolean up) {
        fSortOrder = pSortOrder;
        sort(pArray, 0, pArray.length - 1, up);
        return fSortOrder;
    }

    private void sort(float[] a, int lo0, int hi0, boolean up) {
        int lo = lo0;
        int hi = hi0;
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        sort(a, lo, mid, up);
        sort(a, mid + 1, hi, up);
        int end_lo = mid;
        int start_hi = mid + 1;
        while ((lo <= end_lo) && (start_hi <= hi)) {
            if (up) {
                if (compareFloat(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) <= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            } else {
                if (compareFloat(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) > 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            }
        }
    }

    private int compareFloat(float a, float b) {
        if (a > b) {
            return 1;
        } else if (a < b) {
            return -1;
        } else {
            return 0;
        }
    }

    public int[] sortDouble(double[] pArray, int[] pSortOrder, boolean up) {
        fSortOrder = pSortOrder;
        sort(pArray, 0, pArray.length - 1, up);
        return fSortOrder;
    }

    private void sort(double[] a, int lo0, int hi0, boolean up) {
        int lo = lo0;
        int hi = hi0;
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        sort(a, lo, mid, up);
        sort(a, mid + 1, hi, up);
        int end_lo = mid;
        int start_hi = mid + 1;
        while ((lo <= end_lo) && (start_hi <= hi)) {
            if (up) {
                if (compareDouble(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) <= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            } else {
                if (compareDouble(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) >= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            }
        }
    }

    private int compareDouble(double a, double b) {
        if (a > b) {
            return 1;
        } else if (a < b) {
            return -1;
        } else {
            return 0;
        }
    }

    public int[] sortBoolean(boolean[] pArray, int[] pSortOrder, boolean up) {
        fSortOrder = pSortOrder;
        sort(pArray, 0, pArray.length - 1, up);
        return fSortOrder;
    }

    private void sort(boolean[] a, int lo0, int hi0, boolean up) {
        int lo = lo0;
        int hi = hi0;
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        sort(a, lo, mid, up);
        sort(a, mid + 1, hi, up);
        int end_lo = mid;
        int start_hi = mid + 1;
        while ((lo <= end_lo) && (start_hi <= hi)) {
            if (up) {
                if (compareBoolean(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) <= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            } else {
                if (compareBoolean(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) >= 0) {
                    lo++;
                } else {
                    int T = fSortOrder[start_hi];
                    for (int k = start_hi - 1; k >= lo; k--) {
                        fSortOrder[k + 1] = fSortOrder[k];
                    }
                    fSortOrder[lo] = T;
                    lo++;
                    end_lo++;
                    start_hi++;
                }
            }
        }
    }

    private int compareBoolean(boolean a, boolean b) {
        if (a == b) {
            return 0;
        } else if (a == true) {
            return 1;
        } else if (a == false) {
            return -1;
        }
        return 0;
    }
}

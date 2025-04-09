package org.lnicholls.galleon.util;

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.io.*;
import org.lnicholls.galleon.media.*;
import org.apache.log4j.Logger;

public class FileSorters {

    private static Logger log = Logger.getLogger(FileSorters.class.getName());

    private FileSorters() {
    }

    public static final class SortCollator {

        private static final Collator collator = Collator.getInstance();

        public SortCollator(Media Item) {
            mItem = Item;
            mTitle = Item.getTitle();
            mTitleCollationKey = collator.getCollationKey(mTitle);
            mExtensionCollationKey = collator.getCollationKey("");
            int pos = Item.getPath().lastIndexOf('.');
            if (pos > 0) {
                int suffixLength = Item.getPath().length() - pos;
                if ((suffixLength == ".xxx".length()) || (suffixLength == ".xxxx".length())) {
                    mExtensionCollationKey = collator.getCollationKey(Item.getPath().substring(pos));
                } else mExtensionCollationKey = collator.getCollationKey("");
            }
            for (int i = 0; i < mTitle.length(); i++) if (Character.isDigit(mTitle.charAt(i))) {
                mHasDigits = true;
                break;
            }
            mIsDirectory = (new File(mItem.getPath())).isDirectory();
            mDate = Item.getDateModified().getTime();
        }

        public final Media getItem() {
            return mItem;
        }

        private Media mItem;

        private String mTitle;

        private CollationKey mTitleCollationKey;

        private CollationKey mExtensionCollationKey;

        private boolean mHasDigits;

        private boolean mIsDirectory;

        private long mDate;
    }

    private static final String getIntegerSubstring(String s) {
        int i = 0;
        while ((i < s.length()) && Character.isDigit(s.charAt(i))) {
            ++i;
        }
        return s.substring(0, i);
    }

    static final class TitleComparator implements Comparator {

        public TitleComparator() {
        }

        public final int compare(Object o1, Object o2) {
            SortCollator f1 = (SortCollator) o1;
            SortCollator f2 = (SortCollator) o2;
            if (f1.mHasDigits && f2.mHasDigits) {
                String name1 = f1.mTitle;
                String name2 = f2.mTitle;
                int i1 = 0, i2 = 0;
                while (i1 < name1.length()) {
                    if (i2 >= name2.length()) {
                        return 1;
                    }
                    char c1 = name1.charAt(i1);
                    char c2 = name2.charAt(i2);
                    if (Character.isDigit(c1) && Character.isDigit(c2)) {
                        String number1 = getIntegerSubstring(name1.substring(i1));
                        String number2 = getIntegerSubstring(name2.substring(i2));
                        double double1 = 0, double2 = 0;
                        try {
                            double1 = Double.parseDouble(number1);
                            double2 = Double.parseDouble(number2);
                        } catch (NumberFormatException e) {
                            log.error("TitleCompare exception " + e + " name1=" + name1 + " name2=" + name2);
                        }
                        if (double1 < double2) {
                            return -1;
                        } else if (double1 > double2) {
                            return 1;
                        }
                        i1 += number1.length();
                        i2 += number2.length();
                    } else {
                        if (c1 < c2) {
                            return -1;
                        } else if (c1 > c2) {
                            return 1;
                        }
                        ++i1;
                        ++i2;
                    }
                }
                return (i2 >= name2.length()) ? 0 : -1;
            } else return f1.mTitleCollationKey.compareTo(f2.mTitleCollationKey);
        }
    }

    static final class TypeComparator implements Comparator {

        public TypeComparator() {
        }

        public final int compare(Object o1, Object o2) {
            SortCollator f1 = (SortCollator) o1;
            SortCollator f2 = (SortCollator) o2;
            boolean isDirectory1 = f1.mIsDirectory;
            boolean isDirectory2 = f2.mIsDirectory;
            if (!isDirectory1 && !isDirectory2) {
                return f1.mExtensionCollationKey.compareTo(f2.mExtensionCollationKey);
            } else if (!isDirectory1 && isDirectory2) return 1; else if (isDirectory1 && !isDirectory2) return -1; else return 0;
        }
    }

    static final class DateComparator implements Comparator {

        public DateComparator() {
        }

        public final int compare(Object o1, Object o2) {
            SortCollator f1 = (SortCollator) o1;
            SortCollator f2 = (SortCollator) o2;
            long date1 = f1.mDate;
            long date2 = f2.mDate;
            if (date1 == date2) return 0; else if (date1 < date2) return -1; else return 1;
        }
    }

    public static final Comparator titleComparator = new TitleComparator();

    public static final Comparator typeComparator = new TypeComparator();

    public static final Comparator dateComparator = new DateComparator();

    static final class ChainedComparator implements Comparator {

        private ArrayList comparators = new ArrayList();

        public final void addFilter(Comparator comparator) {
            comparators.add(comparator);
        }

        public final int compare(Object o1, Object o2) {
            for (Iterator i = comparators.iterator(); i.hasNext(); ) {
                Comparator comparator = (Comparator) i.next();
                int result = comparator.compare(o1, o2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

        public final int getSize() {
            return comparators.size();
        }

        public final Comparator getFirstComparator() {
            return (Comparator) comparators.get(0);
        }
    }

    public static void Sort(ArrayList arr, Comparator comparator) {
        QuickSort(arr, 0, arr.size() - 1, comparator);
    }

    private static void QuickSort(ArrayList arr, int lo, int hi, Comparator comparator) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        Object tmp;
        Object middle = arr.get(mid);
        if (comparator.compare(arr.get(lo), middle) > 0) {
            arr.set(mid, arr.get(lo));
            arr.set(lo, middle);
            middle = arr.get(mid);
        }
        if (comparator.compare(middle, arr.get(hi)) > 0) {
            arr.set(mid, arr.get(hi));
            arr.set(hi, middle);
            middle = arr.get(mid);
            if (comparator.compare(arr.get(lo), middle) > 0) {
                arr.set(mid, arr.get(lo));
                arr.set(lo, middle);
                middle = arr.get(mid);
            }
        }
        int left = lo + 1;
        int right = hi - 1;
        if (left >= right) return;
        for (; ; ) {
            while (comparator.compare(arr.get(right), middle) > 0) {
                right--;
            }
            while (left < right && comparator.compare(arr.get(left), middle) <= 0) {
                left++;
            }
            if (left < right) {
                tmp = arr.get(left);
                arr.set(left, arr.get(right));
                arr.set(right, tmp);
                right--;
            } else {
                break;
            }
        }
        QuickSort(arr, lo, left, comparator);
        QuickSort(arr, left + 1, hi, comparator);
    }
}

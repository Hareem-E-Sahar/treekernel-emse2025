package org.designerator.color.test;

import java.applet.*;
import java.awt.*;
import java.util.Random;
import java.util.Vector;
import java.lang.*;

public class SortDemo extends java.applet.Applet implements Runnable {

    Button randomize;

    Button randomize_color;

    Button reverse;

    Button save_state;

    Button restore_state;

    Button abort_sort;

    ListBarCanvas theListBar;

    TextField num_elements;

    Scrollbar slide_speed;

    Checkbox sort_by_value;

    Checkbox sort_by_color;

    Vector disable_components = new Vector();

    Vector sorter_algorithms = new Vector();

    public boolean do_abort_sort;

    public boolean sort_in_progress;

    Thread kicker;

    public void init() {
        setBackground(Color.lightGray);
        setLayout(new BorderLayout());
        theListBar = new ListBarCanvas(100, size());
        add("North", theListBar);
        Panel sorterPanel = new Panel();
        Panel controlPanel = new Panel();
        sorterPanel.setLayout(new GridLayout(SorterAlgorithm.NUM_SORTER_ALGORITHMS + 1, 1));
        for (int i = 0; i < SorterAlgorithm.NUM_SORTER_ALGORITHMS; i++) {
            SorterAlgorithm sorter = new SorterAlgorithm(this, theListBar, i);
            sorter_algorithms.addElement(sorter);
            sorterPanel.add(sorter);
        }
        controlPanel.setLayout(new GridLayout(10, 1));
        Panel numElemPanel = new Panel();
        numElemPanel.setLayout(new GridLayout(1, 2));
        numElemPanel.add(new Label("Elements", Label.CENTER));
        numElemPanel.add(num_elements = new TextField(String.valueOf(theListBar.GetSize())));
        Panel speedPanel = new Panel();
        speedPanel.setLayout(new GridLayout(1, 2));
        speedPanel.add(new Label("Speed", Label.CENTER));
        speedPanel.add(slide_speed = new Scrollbar(Scrollbar.HORIZONTAL, theListBar.wait_speed = 10, 1, 1, 10));
        controlPanel.add(speedPanel);
        controlPanel.add(numElemPanel);
        disable_components.addElement(randomize = new Button("Randomize"));
        disable_components.addElement(randomize_color = new Button("Randomize Color"));
        disable_components.addElement(reverse = new Button("Reverse"));
        disable_components.addElement(save_state = new Button("Save State"));
        disable_components.addElement(restore_state = new Button("Restore State"));
        disable_components.addElement(abort_sort = new Button("Abort Sorting"));
        disable_components.addElement(num_elements);
        abort_sort.disable();
        controlPanel.add(randomize);
        controlPanel.add(randomize_color);
        controlPanel.add(reverse);
        controlPanel.add(save_state);
        controlPanel.add(restore_state);
        CheckboxGroup myGroup = new CheckboxGroup();
        disable_components.addElement(sort_by_value = new Checkbox("Sort by Value", myGroup, true));
        disable_components.addElement(sort_by_color = new Checkbox("Sort by Color", myGroup, false));
        controlPanel.add(sort_by_value);
        controlPanel.add(sort_by_color);
        controlPanel.add(abort_sort);
        add("West", controlPanel);
        add("Center", sorterPanel);
        sort_in_progress = false;
    }

    public void SortingInProgress(boolean inProgress, int nAlgorithm) {
        if (inProgress) {
            for (int i = 0; i < disable_components.size(); i++) ((Component) disable_components.elementAt(i)).disable();
            abort_sort.enable();
        } else {
            for (int i = 0; i < disable_components.size(); i++) ((Component) disable_components.elementAt(i)).enable();
            abort_sort.disable();
        }
        for (int i = 0; i < sorter_algorithms.size(); i++) ((SorterAlgorithm) sorter_algorithms.elementAt(i)).SortingInProgress(inProgress, nAlgorithm);
        sort_in_progress = inProgress;
    }

    public boolean handleEvent(Event e) {
        if (e.id == Event.ACTION_EVENT) {
            if (e.target == randomize) theListBar.Randomize(); else if (e.target == randomize_color) theListBar.RandomizeColor(); else if (e.target == reverse) theListBar.Reverse(); else if (e.target == save_state) theListBar.SaveState(); else if (e.target == restore_state) theListBar.RestoreState(); else if (e.target == abort_sort) do_abort_sort = true; else if (e.target == num_elements) {
                String text = num_elements.getText();
                try {
                    int size = (int) java.lang.Integer.parseInt(text);
                    if (size > 0 && size <= 10000) theListBar.SetSize(size);
                } catch (java.lang.NumberFormatException exc) {
                    ;
                }
            } else if (e.target == sort_by_value || e.target == sort_by_color) {
                theListBar.m_nSortOrder = 0;
                if (sort_by_value.getState()) theListBar.m_nSortOrder = ListBarObject.SORT_VALUE; else if (sort_by_color.getState()) theListBar.m_nSortOrder = ListBarObject.SORT_COLOR;
            }
        }
        if (e.id == Event.SCROLL_ABSOLUTE || e.id == Event.SCROLL_BEGIN || e.id == Event.SCROLL_END || e.id == Event.SCROLL_LINE_DOWN || e.id == Event.SCROLL_LINE_UP || e.id == Event.SCROLL_PAGE_DOWN || e.id == Event.SCROLL_PAGE_UP) {
            if (e.target == slide_speed) {
                theListBar.wait_speed = slide_speed.getValue();
            }
        }
        return super.handleEvent(e);
    }

    public String getAppletInfo() {
        return "SortDemo Applet by H�kan T. Johansson, Sweden, 1997.";
    }

    public void start() {
        if (kicker == null) {
            kicker = new Thread(this);
            kicker.start();
        }
    }

    public void stop() {
        kicker.stop();
        kicker = null;
    }

    public void run() {
        for (; ; ) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}

class SortingRunner implements Runnable {

    int m_nAlgorithm;

    ListBarCanvas m_theList;

    SorterAlgorithm m_sorterAlgorithm;

    SortingRunner(ListBarCanvas list, SorterAlgorithm sorterAlgorithm, int nAlgorithm) {
        m_sorterAlgorithm = sorterAlgorithm;
        m_theList = list;
        m_nAlgorithm = nAlgorithm;
    }

    public void run() {
        switch(m_nAlgorithm) {
            case SorterAlgorithm.SORTER_INSERTATION:
                InsertationSort();
                break;
            case SorterAlgorithm.SORTER_SELECTION:
                SelectionSort();
                break;
            case SorterAlgorithm.SORTER_BUBBLE:
                BubbleSort();
                break;
            case SorterAlgorithm.SORTER_ALTBUBBLE:
                AlternatingBubbleSort();
                break;
            case SorterAlgorithm.SORTER_SHELL:
                ShellSort();
                break;
            case SorterAlgorithm.SORTER_MERGE:
                MergeSort();
                break;
            case SorterAlgorithm.SORTER_HEAP:
                HeapSort();
                break;
            case SorterAlgorithm.SORTER_QUICK:
                QuickSort();
                break;
            case SorterAlgorithm.SORTER_BOGO:
                AHundredBogosorts();
                break;
        }
        if (m_sorterAlgorithm.m_parent.do_abort_sort) {
            m_sorterAlgorithm.compLabel.setText("Comp: ---");
            m_sorterAlgorithm.swapLabel.setText("Swap: ---");
        } else {
            m_sorterAlgorithm.compLabel.setText("Comp: " + String.valueOf(m_theList.m_nCompareCount));
            m_sorterAlgorithm.swapLabel.setText("Swap: " + String.valueOf(m_theList.m_nSwapCount));
        }
        m_sorterAlgorithm.m_parent.SortingInProgress(false, m_nAlgorithm);
    }

    protected boolean IsSorted() {
        for (int i = 1; i < m_theList.GetSize(); i++) if (m_theList.GetElem(i - 1).GreaterThan(m_theList.GetElem(i), m_theList)) return false;
        return true;
    }

    protected void InsertationSort() {
        ListBarObject newElem;
        int index;
        for (int last = 1; last < m_theList.GetSize(); last++) {
            newElem = m_theList.GetElemCopy(last);
            index = last;
            while (index > 0 && newElem.LessThan(m_theList.GetElem(index - 1), m_theList) && !m_sorterAlgorithm.m_parent.do_abort_sort) {
                m_theList.CopyElem(index, index - 1);
                index--;
            }
            m_theList.PutElem(index, newElem);
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
        }
    }

    protected void SelectionSort() {
        int first = 0;
        int last = m_theList.GetSize() - 1;
        int smallpos;
        for (int slot = first; slot < last; slot++) {
            smallpos = slot;
            for (int cand = slot + 1; cand <= last; cand++) if (m_theList.GetElem(cand).LessThan(m_theList.GetElem(smallpos), m_theList)) {
                smallpos = cand;
            }
            m_theList.Swap(slot, smallpos);
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
        }
    }

    protected void BubbleSort() {
        int first = 0;
        boolean sorted = false;
        while (first < m_theList.GetSize() - 1) {
            sorted = true;
            for (int current = m_theList.GetSize() - 2; current >= first; current--) {
                if (m_theList.GetElem(current).GreaterThan(m_theList.GetElem(current + 1), m_theList)) {
                    m_theList.Swap(current, current + 1);
                    sorted = false;
                }
                if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
            }
            first++;
            if (sorted) return;
        }
    }

    protected void AlternatingBubbleSort() {
        int first = 0;
        int last = m_theList.GetSize() - 1;
        boolean sorted = false;
        for (; ; ) {
            if (sorted || first >= last) return;
            sorted = true;
            for (int current = last - 1; current >= first; current--) {
                if (m_theList.GetElem(current).GreaterThan(m_theList.GetElem(current + 1), m_theList)) {
                    m_theList.Swap(current, current + 1);
                    sorted = false;
                }
                if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
            }
            first++;
            if (sorted || first >= last) return;
            sorted = true;
            for (int current = first; current < last; current++) {
                if (m_theList.GetElem(current).GreaterThan(m_theList.GetElem(current + 1), m_theList)) {
                    m_theList.Swap(current, current + 1);
                    sorted = false;
                }
                if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
            }
            last--;
        }
    }

    protected void ShellSort() {
        int dist = m_theList.GetSize() / 2;
        while (dist > 0) {
            for (int begin = 0; begin < dist; begin++) {
                boolean finished = false;
                int rounds = 0;
                while (!finished) {
                    finished = true;
                    int check = begin;
                    int next = check + dist;
                    while (next < m_theList.GetSize() - rounds * dist) {
                        if (m_theList.GetElem(check).GreaterThan(m_theList.GetElem(next), m_theList)) {
                            m_theList.Swap(check, next);
                            finished = false;
                        }
                        if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
                        check = next;
                        next += dist;
                    }
                    rounds++;
                }
            }
            dist /= 2;
        }
    }

    void Merge(int nSize, int length, Vector temp) {
        temp.removeAllElements();
        int last = nSize - 1;
        int firstleft, lastleft, firstright, lastright;
        firstleft = 0;
        while (firstleft <= last) {
            lastleft = firstleft + length - 1;
            firstright = firstleft + length;
            lastright = firstright + length - 1;
            if (firstleft < last) {
                while (firstleft <= lastleft && firstright <= lastright && firstleft <= last && firstright <= last) {
                    if (m_theList.GetElem(firstright).LessThan(m_theList.GetElem(firstleft), m_theList)) temp.addElement(m_theList.GetElem(firstright++)); else temp.addElement(m_theList.GetElem(firstleft++));
                }
            }
            if (firstleft <= lastleft) {
                for (; firstleft <= lastleft && firstleft <= last; firstleft++) temp.addElement(m_theList.GetElem(firstleft));
            } else if (firstright <= lastright) {
                for (; firstright <= lastright && firstright <= last; firstright++) temp.addElement(m_theList.GetElem(firstright));
            }
            firstleft = lastright + 1;
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
        }
        for (int i = 0; i < nSize; i++) {
            m_theList.PutElem(i, (ListBarObject) temp.elementAt(i));
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
        }
    }

    protected void MergeSort() {
        if (m_theList.GetSize() < 2) return;
        Vector temp = new Vector();
        for (int length = 1; length < m_theList.GetSize(); length *= 2) {
            Merge(m_theList.GetSize(), length, temp);
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
        }
    }

    protected void AdjustHeap(int nFirst, int nLast) {
        boolean finished = false;
        int j = 2 * nFirst;
        ListBarObject cand = m_theList.GetElemCopy(nFirst - 1);
        while (j <= nLast && !finished && !m_sorterAlgorithm.m_parent.do_abort_sort) {
            if (j < nLast && m_theList.GetElem(j - 1).LessThan(m_theList.GetElem(j + 1 - 1), m_theList)) j++;
            if (m_theList.GetElem(j - 1).LessThan(cand, m_theList)) finished = true; else {
                m_theList.CopyElem(j / 2 - 1, j - 1);
                j *= 2;
            }
        }
        m_theList.PutElem(j / 2 - 1, cand);
    }

    protected void HeapSort() {
        int nFirst = 1;
        int nLast = m_theList.GetSize();
        for (int i = nLast / 2; i >= 1; i--) {
            AdjustHeap(i, nLast);
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
        }
        for (int i = nLast - 1; i >= 1; i--) {
            m_theList.Swap(i + 1 - 1, nFirst - 1);
            AdjustHeap(nFirst, i);
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
        }
    }

    void RecQuickSort(int nFirst, int nLast) {
        int pivot, front, back;
        if (nFirst < nLast) {
            if (nFirst < nLast - 5) {
                int center = (nFirst + nLast) / 2;
                if (m_theList.GetElem(center).GreaterThan(m_theList.GetElem(nFirst), m_theList)) m_theList.Swap(center, nFirst);
                if (m_theList.GetElem(center).GreaterThan(m_theList.GetElem(nLast), m_theList)) m_theList.Swap(center, nLast);
                if (m_theList.GetElem(nFirst).GreaterThan(m_theList.GetElem(nLast), m_theList)) m_theList.Swap(nFirst, nLast);
            }
            pivot = nFirst;
            front = nFirst;
            back = nLast + 1;
            for (; ; ) {
                do {
                    front++;
                } while (m_theList.GetElem(front).LessThan(m_theList.GetElem(pivot), m_theList) && front < nLast);
                do {
                    back--;
                } while (m_theList.GetElem(back).GreaterThan(m_theList.GetElem(pivot), m_theList));
                if (front < back) m_theList.Swap(front, back); else break;
                if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
            }
            m_theList.Swap(pivot, back);
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
            RecQuickSort(nFirst, back - 1);
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
            RecQuickSort(back + 1, nLast);
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
        }
    }

    protected void QuickSort() {
        RecQuickSort(0, m_theList.GetSize() - 1);
    }

    protected void AHundredBogosorts() {
        Random myRand = new Random();
        for (int nCnt = 0; nCnt < 100; nCnt++) {
            for (int i = 0; i < m_theList.GetSize(); i++) {
                int nSwap = myRand.nextInt();
                if (nSwap < 0) nSwap = -nSwap;
                nSwap %= m_theList.GetSize();
                m_theList.Swap(i, nSwap);
            }
            if (IsSorted()) return;
            if (m_sorterAlgorithm.m_parent.do_abort_sort) return;
        }
    }
}

class SorterAlgorithm extends Panel {

    static final int SORTER_BUBBLE = 0;

    static final int SORTER_ALTBUBBLE = SORTER_BUBBLE + 1;

    static final int SORTER_INSERTATION = SORTER_ALTBUBBLE + 1;

    static final int SORTER_SELECTION = SORTER_INSERTATION + 1;

    static final int SORTER_SHELL = SORTER_SELECTION + 1;

    static final int SORTER_HEAP = SORTER_SHELL + 1;

    static final int SORTER_MERGE = SORTER_HEAP + 1;

    static final int SORTER_QUICK = SORTER_MERGE + 1;

    static final int SORTER_BOGO = SORTER_QUICK + 1;

    static final int NUM_SORTER_ALGORITHMS = SORTER_BOGO + 1;

    ListBarCanvas m_theList;

    SortDemo m_parent;

    static String names[] = { "BubbleSort", "Alternating BubbleSort", "InsertationSort", "SelectionSort", "ShellSort", "HeapSort", "MergeSort", "QuickSort", "A Hundred Bogosorts" };

    protected int m_nAlgorithm;

    Button startButton;

    Label compLabel;

    Label swapLabel;

    SorterAlgorithm(SortDemo parent, ListBarCanvas list, int nAlgorithm) {
        m_nAlgorithm = nAlgorithm;
        m_theList = list;
        m_parent = parent;
        setLayout(new GridLayout(1, 3));
        startButton = new Button(names[m_nAlgorithm]);
        add(startButton);
        compLabel = new Label("Comp: ---", Label.CENTER);
        add(compLabel);
        swapLabel = new Label("Swap: ---", Label.CENTER);
        add(swapLabel);
    }

    public void SortingInProgress(boolean inProgress, int nAlgorithm) {
        if (inProgress) {
            startButton.disable();
            if (nAlgorithm == m_nAlgorithm) startButton.setLabel("* " + names[m_nAlgorithm] + " *");
        } else {
            startButton.enable();
            if (nAlgorithm == m_nAlgorithm) startButton.setLabel(names[m_nAlgorithm]);
        }
    }

    public boolean handleEvent(Event e) {
        if (e.id == Event.ACTION_EVENT) {
            if (e.target == startButton) {
                if (!m_parent.sort_in_progress) {
                    m_theList.m_nCompareCount = 0;
                    m_theList.m_nSwapCount = 0;
                    m_parent.SortingInProgress(true, m_nAlgorithm);
                    m_parent.do_abort_sort = false;
                    SortingRunner runner = new SortingRunner(m_theList, this, m_nAlgorithm);
                    Thread myThread = new Thread(runner);
                    myThread.start();
                }
            }
        }
        return super.handleEvent(e);
    }
}

class ListBarObject {

    static final int NUM_VALUES = 1000;

    static final int NUM_COLORS_SHADES = 3;

    static final int NUM_COLORS = ((NUM_COLORS_SHADES * NUM_COLORS_SHADES * NUM_COLORS_SHADES) - 1);

    static final int COLOR_MULTIPLY = 255 / (NUM_COLORS_SHADES - 1);

    static final int SORT_VALUE = 0x01;

    static final int SORT_COLOR = 0x02;

    protected int m_nValue = 0;

    protected int m_nColor = 0;

    public void Set(int nValue, int nColor) {
        m_nValue = nValue;
        m_nColor = nColor;
    }

    public int GetValue() {
        return m_nValue;
    }

    public int GetColor() {
        return m_nColor;
    }

    public void DrawBar(Graphics g, int x, int top, int bottom) {
        g.setColor(new Color(((m_nColor + 1) % NUM_COLORS_SHADES) * COLOR_MULTIPLY, (((m_nColor + 1) / NUM_COLORS_SHADES) % NUM_COLORS_SHADES) * COLOR_MULTIPLY, ((m_nColor + 1) / (NUM_COLORS_SHADES * NUM_COLORS_SHADES)) * COLOR_MULTIPLY));
        g.drawLine(x, (top * (m_nValue) + bottom * (NUM_VALUES - m_nValue)) / NUM_VALUES, x, bottom);
    }

    public boolean LessThan(ListBarObject other, ListBarCanvas list) {
        list.m_nCompareCount++;
        if (list.m_nSortOrder == SORT_VALUE) return (m_nValue < other.m_nValue);
        return (m_nColor < other.m_nColor);
    }

    public boolean GreaterThan(ListBarObject other, ListBarCanvas list) {
        list.m_nCompareCount++;
        if (list.m_nSortOrder == SORT_VALUE) return (m_nValue > other.m_nValue);
        return (m_nColor > other.m_nColor);
    }

    public boolean EqualTo(ListBarObject other, ListBarCanvas list) {
        list.m_nCompareCount++;
        return (m_nValue == other.m_nValue && m_nColor == other.m_nColor);
    }
}

class ListBarCanvas extends Canvas {

    public int m_nCompareCount = 0;

    public int m_nSwapCount = 0;

    public int m_nSortOrder = ListBarObject.SORT_VALUE;

    public int wait_speed;

    protected int m_nSize = 0;

    protected Vector elements = new Vector();

    protected Vector save_state_elem = new Vector();

    protected Dimension m_parentSize;

    ListBarCanvas(int size, Dimension parentSize) {
        m_parentSize = parentSize;
        m_nSize = 0;
        SetSize(size);
    }

    public int GetSize() {
        return m_nSize;
    }

    public void SetSize(int size) {
        if (m_nSize > size) {
            elements.removeAllElements();
            m_nSize = 0;
        }
        for (; m_nSize < size; m_nSize++) elements.addElement(new ListBarObject());
        Randomize();
        SaveState();
    }

    public void Randomize() {
        Random myRand = new Random();
        for (int i = 0; i < m_nSize; i++) {
            int value = myRand.nextInt();
            int color = myRand.nextInt();
            if (value < 0) value = -value;
            if (color < 0) color = -color;
            value %= ListBarObject.NUM_VALUES;
            color %= ListBarObject.NUM_COLORS;
            ((ListBarObject) elements.elementAt(i)).Set(value, color);
        }
        repaint();
    }

    public void RandomizeColor() {
        Random myRand = new Random();
        for (int i = 0; i < m_nSize; i++) {
            int value = myRand.nextInt();
            int color = myRand.nextInt();
            if (value < 0) value = -value;
            if (color < 0) color = -color;
            value %= ListBarObject.NUM_VALUES;
            color %= ListBarObject.NUM_COLORS;
            if (myRand.nextInt() % 8 != 0 && i > 0) {
                int begin = myRand.nextInt();
                if (begin < 0) begin = -begin;
                begin %= i;
                int search = begin;
                do {
                    if (((ListBarObject) elements.elementAt(search)).GetColor() == color) {
                        value = ((ListBarObject) elements.elementAt(search)).GetValue();
                        break;
                    }
                    if (++search == i) search = 0;
                } while (search != begin);
            }
            ((ListBarObject) elements.elementAt(i)).Set(value, color);
        }
        repaint();
    }

    Graphics grphUse;

    protected void RedrawBar(int i) {
        int canW = size().width;
        int canH = size().height;
        grphUse.setColor(Color.lightGray);
        grphUse.drawLine((canW * (i + 1)) / (m_nSize + 1), 1, (canW * (i + 1)) / (m_nSize + 1), canH - 2);
        ((ListBarObject) elements.elementAt(i)).DrawBar(grphUse, (canW * (i + 1)) / (m_nSize + 1), 1, canH - 2);
    }

    public void Reverse() {
        int first = 0;
        int last = m_nSize - 1;
        while (first < last) Swap(first++, last--);
    }

    public void Swap(int i, int j) {
        m_nSwapCount++;
        SleepWait();
        ListBarObject temp = (ListBarObject) elements.elementAt(i);
        elements.setElementAt(elements.elementAt(j), i);
        elements.setElementAt(temp, j);
        RedrawBar(i);
        RedrawBar(j);
    }

    public ListBarObject GetElem(int index) {
        return (ListBarObject) elements.elementAt(index);
    }

    public ListBarObject GetElemCopy(int index) {
        ListBarObject newobj = new ListBarObject();
        newobj.Set(((ListBarObject) elements.elementAt(index)).GetValue(), ((ListBarObject) elements.elementAt(index)).GetColor());
        return newobj;
    }

    public void PutElem(int index, ListBarObject obj) {
        m_nSwapCount++;
        SleepWait();
        elements.setElementAt(obj, index);
        RedrawBar(index);
    }

    public void SleepWait() {
        if (wait_speed != 10) {
            try {
                Thread.sleep((10 - wait_speed) * (10 - wait_speed) * 10);
            } catch (InterruptedException ex) {
            }
        }
    }

    public void CopyElem(int dest, int src) {
        m_nSwapCount++;
        SleepWait();
        ((ListBarObject) elements.elementAt(dest)).Set(((ListBarObject) elements.elementAt(src)).GetValue(), ((ListBarObject) elements.elementAt(src)).GetColor());
        RedrawBar(dest);
    }

    public void CopyVectors(Vector dest, Vector src) {
        if (!dest.isEmpty()) dest.removeAllElements();
        for (int i = 0; i < m_nSize; i++) {
            ListBarObject newobj = new ListBarObject();
            newobj.Set(((ListBarObject) src.elementAt(i)).GetValue(), ((ListBarObject) src.elementAt(i)).GetColor());
            dest.addElement(newobj);
        }
    }

    public void SaveState() {
        CopyVectors(save_state_elem, elements);
    }

    public void RestoreState() {
        CopyVectors(elements, save_state_elem);
        repaint();
    }

    public void paint(Graphics g) {
        int canW = size().width;
        int canH = size().height;
        g.setColor(Color.lightGray);
        g.draw3DRect(0, 0, canW - 1, canH - 1, true);
        int x, lastx = -100000;
        for (int i = 0; i < m_nSize; i++) {
            x = (canW * (i + 1)) / (m_nSize + 1);
            if (x != lastx) {
                ((ListBarObject) elements.elementAt(i)).DrawBar(g, x, 1, canH - 2);
                lastx = x;
            }
        }
        grphUse = getGraphics();
    }

    public Dimension preferredSize() {
        return new Dimension(500, m_parentSize.height - 200);
    }
}

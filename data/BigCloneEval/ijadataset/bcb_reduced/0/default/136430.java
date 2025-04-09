import jdsl.core.api.*;
import jdsl.core.ref.*;
import jdsl.core.algo.sorts.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SequenceFun extends Frame implements ActionListener, WindowListener {

    private void sort(Sequence seq) {
        SortObject sorter = new ArrayQuickSort();
        sorter.sort(seq, new ComparableComparator());
    }

    private void reverseSort(Sequence seq) {
        SortObject sorter = new ArrayQuickSort();
        sorter.sort(seq, new ComparatorReverser(new ComparableComparator()));
    }

    protected void permute(Sequence s) {
        Random rnd = new java.util.Random();
        for (int i = s.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            if (j < i) s.swapElements(s.atRank(i), s.atRank(j));
        }
    }

    Sequence seq;

    Label title = new Label("Test Sequences");

    TextField inField = new TextField("", 50);

    TextField outField = new TextField("", 50);

    Button echoBtn = new Button("Echo");

    Button sortBtn = new Button("Sort");

    Button reverseBtn = new Button("Reverse Sort");

    Button permuteBtn = new Button("Permute");

    Button quitBtn = new Button("Quit");

    public SequenceFun() {
        setUpWindow();
    }

    public void actionPerformed(ActionEvent e) {
        String in = inField.getText();
        seq = getWords(in);
        if (e.getSource() == sortBtn) sort(seq); else if (e.getSource() == reverseBtn) reverseSort(seq); else if (e.getSource() == permuteBtn) permute(seq); else if (e.getSource() == quitBtn) quit();
        outField.setText(conc(seq));
    }

    private Sequence getWords(String s) {
        Sequence ret = new ArraySequence();
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            ret.insertLast(st.nextToken());
        }
        return ret;
    }

    private String conc(Sequence s) {
        String ret = "";
        for (ObjectIterator i = seq.elements(); i.hasNext(); ) ret += (i.nextObject() + " ");
        return ret;
    }

    public static void main(String args[]) {
        SequenceFun s = new SequenceFun();
        s.show();
    }

    private void setUpWindow() {
        setTitle("Sort");
        title.setFont(new Font("Helvetica", Font.BOLD, 24));
        Panel titlePanel = new Panel();
        titlePanel.add(title);
        Panel inPanel = new Panel();
        inPanel.add(new Label("Enter a sentence"));
        inPanel.add(inField);
        outField.setEditable(false);
        Panel outPanel = new Panel();
        outPanel.add(new Label("Result"));
        outPanel.add(outField);
        Panel centerPanel = new Panel();
        centerPanel.add(inPanel);
        centerPanel.add(outPanel);
        Panel btnPanel = new Panel();
        btnPanel.add(echoBtn);
        btnPanel.add(sortBtn);
        btnPanel.add(reverseBtn);
        btnPanel.add(permuteBtn);
        btnPanel.add(quitBtn);
        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        addWindowListener(this);
        echoBtn.addActionListener(this);
        sortBtn.addActionListener(this);
        reverseBtn.addActionListener(this);
        permuteBtn.addActionListener(this);
        quitBtn.addActionListener(this);
        inField.addActionListener(this);
        inField.requestFocus();
        setSize(500, 250);
        setLocation(50, 50);
    }

    private void quit() {
        System.exit(0);
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
        quit();
    }

    public void windowClosing(WindowEvent e) {
        quit();
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }
}

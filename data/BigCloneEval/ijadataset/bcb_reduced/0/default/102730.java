import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import calculator.*;

public final class Gui extends JFrame {

    private static final long serialVersionUID = 1L;

    private final MatrixesModel model = new MatrixesModel();

    private final Calculator calculator = new Calculator();

    private final JFileChooser fileChooser = new JFileChooser();

    private final JList list = new JList(model);

    private final JSpinner tcSpinner;

    private final JPanel resultPanel = new JPanel();

    private final JLabel[] resultLabels = new JLabel[Method.count()];

    Gui() {
        super("Matrices Multiplication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        FileFilter filter = new FileNameExtensionFilter("Matrix (*.mat)", "mat");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(filter);
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(listPanel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(300, 300));
        listPanel.add(listScroller);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        add(buttonPanel);
        JButton loadButton = new JButton("Load Matrix");
        loadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showOpenDialog(getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    resetOutput();
                    try {
                        model.add(file.getName(), calculator.load(file));
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(getMainWindow(), "Unable to load matrix from file: " + file.getName());
                    }
                }
            }
        });
        buttonPanel.add(loadButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton genButton = new JButton("Generate Random Matrix");
        genButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String rowsS = JOptionPane.showInputDialog(getMainWindow(), "Number of matrix rows:", "5000");
                String colsS = JOptionPane.showInputDialog(getMainWindow(), "Number of matrix columns:", "5000");
                int rows, cols;
                try {
                    rows = Integer.parseInt(rowsS);
                    cols = Integer.parseInt(colsS);
                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(getMainWindow(), "Invalid matrix size: " + rowsS + " x " + colsS);
                    return;
                }
                Matrix matrix = calculator.genRandom(rows, cols);
                if (fileChooser.showSaveDialog(getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    resetOutput();
                    try {
                        calculator.save(file, matrix);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(getMainWindow(), "Unable to save generated matrix into file: " + file.getName());
                    }
                    model.add(file.getName(), matrix);
                }
            }
        });
        buttonPanel.add(genButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        final JButton removeButton = new JButton("Remove Matrix");
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list.getSelectedIndex();
                if (index >= 0) {
                    resetOutput();
                    model.remove(index);
                }
            }
        });
        removeButton.setEnabled(false);
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                removeButton.setEnabled(!list.getSelectionModel().isSelectionEmpty());
            }
        });
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JPanel tcPanel = new JPanel();
        add(tcPanel);
        SpinnerModel tcModel = new SpinnerNumberModel(calculator.getThreadCountInit(), calculator.getThreadCountMin(), calculator.getThreadCountMax(), 1);
        JLabel tcLabel = new JLabel("Thread Count:");
        tcPanel.add(tcLabel);
        tcSpinner = new JSpinner(tcModel);
        tcSpinner.setEditor(new JSpinner.NumberEditor(tcSpinner, "#"));
        tcLabel.setLabelFor(tcSpinner);
        tcPanel.add(tcSpinner);
        final JButton okButton = new JButton("Multiply");
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.getSize() < 2) return;
                resultPanel.setVisible(true);
                int threadCount = ((Integer) tcSpinner.getValue()).intValue();
                for (Method method : Method.values()) {
                    long time = 0;
                    Matrix m1 = model.getMatrixAt(0);
                    for (int i = 1; i < model.getSize(); i++) {
                        Matrix m2 = model.getMatrixAt(i);
                        m1 = calculator.multiply(m1, m2, threadCount, method);
                        time += calculator.getLastMultiplyingTime();
                    }
                    resultLabels[method.ordinal()].setText(time + " ms");
                }
            }
        });
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.setEnabled(false);
        model.addListDataListener(new ListDataListenerEx() {

            @Override
            public void dataEvent(ListDataEvent e) {
                okButton.setEnabled(model.getSize() >= 2);
            }
        });
        add(okButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultPanel.setVisible(false);
        add(resultPanel);
        JPanel resultLabelsPanel = new JPanel();
        resultLabelsPanel.setLayout(new GridLayout(resultLabels.length, 2));
        resultLabelsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (Method m : Method.values()) {
            JLabel nameLabel = new JLabel(m.name);
            resultLabelsPanel.add(nameLabel);
            JLabel timeLabel = new JLabel("", SwingConstants.TRAILING);
            nameLabel.setLabelFor(timeLabel);
            resultLabelsPanel.add(timeLabel);
            resultLabels[m.ordinal()] = timeLabel;
        }
        resultLabelsPanel.setMaximumSize(new Dimension(150, 100));
        resultPanel.add(resultLabelsPanel);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        new Gui();
    }

    private JFrame getMainWindow() {
        return this;
    }

    private void resetOutput() {
        resultPanel.setVisible(false);
    }
}

final class MatrixesModel extends AbstractListModel {

    private static final long serialVersionUID = 1L;

    private final List<String> names = new ArrayList<String>();

    private final List<Matrix> matrixes = new ArrayList<Matrix>();

    public void add(String name, Matrix matrix) {
        names.add(name);
        matrixes.add(matrix);
        int index = getSize() - 1;
        fireIntervalAdded(this, index, index);
        return;
    }

    @Override
    public String getElementAt(int index) {
        return names.get(index);
    }

    public Matrix getMatrixAt(int index) {
        return matrixes.get(index);
    }

    @Override
    public int getSize() {
        return names.size();
    }

    public void remove(int index) {
        names.remove(index);
        matrixes.remove(index);
        fireIntervalRemoved(this, index, index);
    }
}

abstract class ListDataListenerEx implements ListDataListener {

    public abstract void dataEvent(ListDataEvent e);

    @Override
    public void contentsChanged(ListDataEvent e) {
        dataEvent(e);
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        dataEvent(e);
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        dataEvent(e);
    }
}

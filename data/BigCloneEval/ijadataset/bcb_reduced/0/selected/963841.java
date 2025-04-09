package com.apdf.wof.jpb;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.apdf.wof.db.jeopardy.Question;

public class JeopardyBoardView extends JPanel {

    public static int BASIC_POINT = 100;

    private JTable boardTable;

    private JeopardyBoardModel jeopardyBoardModel;

    public JeopardyBoardView(JeopardyBoardModel jeopardyBoardModel) {
        super(new GridLayout(1, 0));
        setJeopardyBoardModel(jeopardyBoardModel);
        initComponents();
    }

    private void initComponents() {
        Object[][] data = null;
        Object[] columnName = null;
        if (boardTable == null) {
            boardTable = new JTable();
        }
        if (jeopardyBoardModel == null) {
            data = new Object[][] { { "100", "100", "100", "100", "100", "100" }, { "200", "200", "200", "200", "200", "100" } };
            columnName = new String[] { "Category 1", "Category 2", "Category 3", "Category 4", "Category 5", "Category 6" };
        } else {
            data = transform(jeopardyBoardModel.getQuestions());
            columnName = jeopardyBoardModel.getCategories();
        }
        if (data != null && columnName != null) {
            boardTable.setModel(new DefaultTableModel(data, columnName) {

                public Class getColumnClass(int c) {
                    return getValueAt(0, c).getClass();
                }
            });
            boardTable.setDefaultRenderer(Question.class, new QuestionRenderer(jeopardyBoardModel));
        }
        boardTable.setCellSelectionEnabled(true);
        boardTable.setPreferredScrollableViewportSize(new Dimension(this.getWidth(), this.getHeight()));
        boardTable.setFillsViewportHeight(true);
        boardTable.setRowHeight(30);
        add(new JScrollPane(boardTable));
    }

    private Object[][] transform(Object[][] src) {
        int col = src.length;
        int row = src[0].length;
        Object[][] dest = new Object[row][col];
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                dest[j][i] = src[i][j];
            }
        }
        return dest;
    }

    public JeopardyBoardModel getJeopardyBoardModel() {
        return jeopardyBoardModel;
    }

    public void setJeopardyBoardModel(JeopardyBoardModel jeopardyBoardModel) {
        this.jeopardyBoardModel = jeopardyBoardModel;
    }

    public JTable getBoardTable() {
        return boardTable;
    }

    public void setBoardTable(JTable boardTable) {
        this.boardTable = boardTable;
    }
}

class QuestionRenderer extends DefaultTableCellRenderer {

    JeopardyBoardModel jeopardyBoardModel;

    public QuestionRenderer(JeopardyBoardModel jpm) {
        super();
        this.jeopardyBoardModel = jpm;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String v = null;
        if (value instanceof Question) {
            Question question = (Question) value;
            int point = question.getCategory().getLevel() * question.getLevel() * JeopardyBoardView.BASIC_POINT;
            v = (value == null) ? "" : Integer.toString(point);
        }
        Component cell = new JButton(v);
        if (jeopardyBoardModel.getSelected()[column][row]) {
            cell = new JLabel("");
        }
        if (row == 0 || (row > 0 && jeopardyBoardModel.getSelected()[column][row - 1])) {
            if (isSelected && hasFocus) {
                cell = new JLabel("");
            }
        }
        return cell;
    }
}

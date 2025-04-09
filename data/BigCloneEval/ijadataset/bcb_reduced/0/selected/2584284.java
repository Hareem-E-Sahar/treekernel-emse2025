package seco.notebook.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;
import seco.notebook.NotebookUI;
import seco.notebook.SelectionManager;
import seco.notebook.view.CellHandleView.CustomButton;

public class InsertionPointView extends HidableComponentView {

    private CustomButton button = null;

    public InsertionPointView(Element element) {
        super(element);
    }

    protected Component createComponent() {
        if (button == null) {
            button = new CustomButton();
            button.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            Dimension dim = new Dimension((int) getPreferredSpan(View.X_AXIS), 3);
            button.setPreferredSize(dim);
            button.setBackground(Color.white);
        }
        return button;
    }

    public void setVisible(boolean _visible) {
        super.setVisible(_visible);
        if (button != null) button.setVisible(_visible);
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
        return getStartOffset();
    }

    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
        int next = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
        if (!isVisible()) return next;
        int start = getStartOffset();
        int end = getEndOffset();
        if (next < start || end < next) return next;
        if (next == start) return next;
        if (direction == WEST) return start;
        if (direction == EAST) {
            biasRet[0] = Position.Bias.Backward;
            if (pos == end && next == end && b == b.Backward) return end;
            return end;
        }
        int mid = (start + end) / 2;
        next = (next <= mid) ? start : end;
        return next;
    }

    class CustomButton extends JLabel implements SelectionManager.Selection {

        SelectionManager selectionManager;

        public CustomButton() {
            super();
            NotebookUI ui = (NotebookUI) getContainer();
            if (ui == null) return;
            selectionManager = ui.getSelectionManager();
            selectionManager.put(getElement(), this);
            putClientProperty("Plastic.is3D", Boolean.FALSE);
        }

        public void removeNotify() {
            selectionManager.remove(getElement());
            super.removeNotify();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (((NotebookUI) getContainer()).getCaretPosition() != getElement().getStartOffset() || !getContainer().isFocusOwner()) return;
            Rectangle bounds = this.getBounds();
            g.setColor(java.awt.Color.black);
            g.drawLine(bounds.x, bounds.y + 1, bounds.x + bounds.width, bounds.y + 1);
        }

        public void setSelected(boolean selected) {
            repaint();
        }
    }
}

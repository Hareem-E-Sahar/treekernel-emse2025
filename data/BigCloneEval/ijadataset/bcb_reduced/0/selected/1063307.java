package lug.gui.archetype.skills;

import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import lug.gui.AdvancedGridBagLayout;
import lug.gui.CachedImageLoader;
import lug.gui.ColorUtils;
import lug.serenity.npc.gui.controls.WeightingSlider;
import lug.serenity.npc.random.archetype.skills.ChildSkillGroup;
import lug.serenity.npc.random.archetype.skills.WeightedChildSkill;
import org.apache.commons.lang.StringUtils;

/**
 * @author Luggy
 *
 */
public abstract class ChildSkillGroupPanel extends JPanel implements DragGestureListener {

    private static final float DRAG_DARKEN_AMOUNT = 0.15f;

    private static final Dimension SLIDER_MIN_SIZE = new Dimension(120, 14);

    private static final Insets BUTTON_INSETS = new Insets(0, 1, 0, 1);

    private static final Insets TITLE_SLIDER_INSETS = new Insets(0, 4, 0, 1);

    private static final Dimension PARENT_SLIDER_MIN_SIZE = new Dimension(92, 14);

    private static final Icon DELETE_ICON = CachedImageLoader.getCachedIcon("images/delete.png");

    private static final Icon DELETE_ICON_CLICKED = CachedImageLoader.getCachedIcon("images/delete_clicked.png");

    public static final String LABEL_NAME_PREFIX = "label_";

    protected static final int SLIDER_MAX = 7;

    private String draggedSkill = null;

    public static final Color SELECTION_GRADIENT2 = new Color(0xb39457);

    public static final Color SELECTION_GRADIENT1 = new Color(0xe6e588);

    protected ChildSkillGroup dataModel;

    private JPanel titlePanel;

    protected HashMap<String, JLabel> labels;

    protected HashMap<String, WeightingSlider> biasSliders;

    protected WeightingSlider titleBiasSlider;

    protected ListSelectionModel selectionModel;

    private List<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();

    private Action deleteGroupAction = new AbstractAction("", DELETE_ICON) {

        public void actionPerformed(ActionEvent event) {
            performDelete(dataModel.getName());
        }
    };

    private JButton deleteGroupButton;

    private AdvancedGridBagLayout layout;

    /**
	 * Construct a child skil group panel
	 * @param group
	 */
    protected ChildSkillGroupPanel(ChildSkillGroup group) {
        super();
        selectionModel = new DefaultListSelectionModel();
        this.dataModel = group;
        dataModel.sortChildren();
        build();
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    performClickResponse(e);
                }
            }
        });
    }

    /**
	 * 
	 */
    private void build() {
        layout = new AdvancedGridBagLayout();
        setLayout(layout);
        buildTitlePanel();
        add(titlePanel, new GridBagConstraints(0, 0, 3, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        biasSliders = new HashMap<String, WeightingSlider>();
        labels = new HashMap<String, JLabel>();
        buildList();
        DragSource ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
    }

    /**
	 * 
	 */
    private void buildList() {
        int y = 1;
        for (WeightedChildSkill wcs : dataModel.getChildSkills()) {
            addSkillRow(wcs, y++);
        }
        calculatePreferredSize();
    }

    /**
	 * 
	 */
    private void calculatePreferredSize() {
        setPreferredSize(new Dimension(200, titlePanel.getPreferredSize().height + (18 * dataModel.getSize())));
    }

    /**
	 * Create a weighted child skill row.
	 * @param wcs child skill who's slider is being created.
	 * @param y y coordinate of the row
	 */
    private void addSkillRow(WeightedChildSkill wcs, int y) {
        String name = wcs.getSkillName();
        JLabel label = new JLabel(name);
        label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        label.setOpaque(false);
        label.setName(LABEL_NAME_PREFIX + name);
        add(label, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 2), 0, 0));
        WeightingSlider slider = new WeightingSlider(wcs.getWeighting());
        slider.setName("slider_" + name);
        slider.setPreferredSize(SLIDER_MIN_SIZE);
        add(slider, new GridBagConstraints(2, y, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
        slider.setOpaque(false);
        int value = wcs.getWeighting().getValue();
        slider.setValue(value);
        slider.setName("slider_" + (y));
        labels.put(name, label);
        biasSliders.put(name, slider);
    }

    /**
	 * Go through the current slider list and remove any sliders that do not
	 * exist in the datamodel and add the new ones.
	 */
    public void rebuild() {
        removeOldChildren();
        layout.compressGridY(this);
        addNewChildren();
        revalidate();
        calculatePreferredSize();
    }

    /**
	 * Remove any child skill sliders that no longer exist within the datamodel.
	 */
    private void removeOldChildren() {
        String[] childNames = biasSliders.keySet().toArray(new String[0]);
        List<String> removals = new ArrayList<String>();
        for (int i = 0; i < childNames.length; i++) {
            if (dataModel.getWeightedChildSkill(childNames[i]) == null) {
                remove(biasSliders.get(childNames[i]));
                biasSliders.remove(childNames[i]);
                remove(labels.get(childNames[i]));
                labels.remove(childNames[i]);
            }
        }
    }

    /**
	 * Add any child skills that exist in the data model that are not within the gui.
	 */
    private void addNewChildren() {
        List<String> additions = new ArrayList<String>();
        for (WeightedChildSkill wcs : dataModel.getChildSkills()) {
            if (!biasSliders.containsKey(wcs.getSkillName())) {
                additions.add(wcs.getSkillName());
            }
        }
        for (String name : additions) {
            int gridy = layout.firstEmptyRow(this);
            addSkillRow(dataModel.getWeightedChildSkill(name), gridy);
        }
    }

    public void insertSkill(Point p, WeightedChildSkill wcs) {
        dataModel.addSkill(wcs);
        dataModel.sortChildren();
        for (String str : labels.keySet()) {
            remove(labels.get(str));
        }
        for (String str : biasSliders.keySet()) {
            remove(biasSliders.get(str));
        }
        buildList();
    }

    /**
	 * 
	 */
    private void buildTitlePanel() {
        titlePanel = new JPanel();
        titlePanel.setLayout(new GridBagLayout());
        JLabel titleLabel = new JLabel(getGroupTitle());
        titleLabel.setForeground(getTitleTextColor());
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titlePanel.add(titleLabel, new GridBagConstraints(0, 0, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 2), 0, 0));
        titlePanel.setUI(new GradientPanelUI(titleTopColor(), titleBottomColor()));
        titlePanel.setPreferredSize(new Dimension(100, 36));
        titleBiasSlider = new WeightingSlider(dataModel.getWeighting());
        titleBiasSlider.setMinimumSize(PARENT_SLIDER_MIN_SIZE);
        titleBiasSlider.setOpaque(false);
        titleBiasSlider.setName("parentSlider");
        titlePanel.add(titleBiasSlider, new GridBagConstraints(0, 1, 1, 1, 0.4f, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, TITLE_SLIDER_INSETS, 0, 0));
        titleBiasSlider.setMinimumSize(WeightedChildSkillPanel.SLIDER_MIN_SIZE);
        titleBiasSlider.setPreferredSize(WeightedChildSkillPanel.SLIDER_MIN_SIZE);
        deleteGroupButton = new EmptyButton(deleteGroupAction);
        deleteGroupButton.setPressedIcon(DELETE_ICON_CLICKED);
        titlePanel.add(deleteGroupButton, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 8, 0, 1), 0, 0));
    }

    /**
	 * Determine if the title color should be black or white.
	 * @return
	 */
    private Color getTitleTextColor() {
        int greyTop = ColorUtils.getGreyValue(titleTopColor());
        int greyBottom = ColorUtils.getGreyValue(titleBottomColor());
        int greyAvg = (greyTop + greyBottom) / 2;
        return (greyAvg > 128 ? Color.black : Color.white);
    }

    /**
	 * @return Bottom gradient color for the title panel.
	 */
    protected abstract Color titleTopColor();

    /**
	 * @return Top gradient color for the title panel.
	 */
    protected abstract Color titleBottomColor();

    /**
	 * @return
	 */
    protected abstract String getGroupTitle();

    /**
	 * Respond to a left click within this GUI.
	 * @param e
	 */
    protected void performClickResponse(MouseEvent e) {
        int index = getLabelIndex(e.getPoint());
        if (e.isShiftDown() && !e.isControlDown()) {
            performShiftClick(index);
        } else if (e.isControlDown() && !e.isShiftDown()) {
            performControlClick(index);
        }
        if (!e.isShiftDown() && !e.isControlDown()) {
            performedPlainClick(index);
        }
        repaint();
    }

    /**
	 * Perform a select click with CTRL held, adding or removing the selected index. 
	 * @param index
	 */
    private void performControlClick(int index) {
        if (selectionModel.isSelectedIndex(index)) {
            selectionModel.removeIndexInterval(index, index);
        } else {
            selectionModel.addSelectionInterval(index, index);
        }
    }

    /**
	 * @param index
	 */
    private void performShiftClick(int index) {
        int leadIndex = selectionModel.getAnchorSelectionIndex();
        selectionModel.setSelectionInterval(leadIndex, index);
    }

    /**
	 * Perform a simple click select, with no shift or control modifiers.
	 * @param index
	 */
    private void performedPlainClick(int index) {
        selectionModel.setSelectionInterval(index, index);
    }

    /**
	 * @param point
	 * @return
	 */
    private int getLabelIndex(Point point) {
        if (point.y < labels.get(labels.keySet().toArray()[0]).getY()) {
            return -1;
        }
        int index = 0;
        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i) != null && labels.get(i).getY() + labels.get(i).getHeight() - 1 > point.y) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * Name of a the skill at the given point
	 * @param point
	 * @return
	 */
    private String getSkillNameAt(Point point) {
        for (String str : labels.keySet()) {
            Rectangle bounds = labels.get(str).getBounds();
            if (bounds.contains(new Point(bounds.x + 1, point.y))) {
                return str;
            }
        }
        return null;
    }

    /**
	 * The row index of the given skill name
	 * @param str
	 * @return the row number where str is the skill name or -1 if
	 * the skill name does not exists. 
	 */
    private int getSkillIndex(String str) {
        String[] skillNames = labels.keySet().toArray(new String[labels.size()]);
        for (int i = 0; i < skillNames.length; i++) {
            if (StringUtils.equalsIgnoreCase(skillNames[i], str)) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * Add a listener to be notified of selection events.
	 * @param listener
	 */
    public void addListSelectionListener(ListSelectionListener listener) {
        selectionModel.addListSelectionListener(listener);
    }

    /**
	 * Re,move a listener so that it is no longer notified of selection event.s
	 * @param listener
	 */
    public void removeListSelectionListener(ListSelectionListener listener) {
        selectionModel.removeListSelectionListener(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (labels == null || selectionModel == null || selectionModel.isSelectionEmpty()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        for (int i = 0; i < labels.size(); i++) {
            if (selectionModel.isSelectedIndex(i)) {
                JLabel selected = labels.get(i);
                int y1 = labels.get(i).getY();
                int y2 = y1 + labels.get(i).getHeight() - 1;
                GradientPaint gradient = new GradientPaint(new Point(0, y1), SELECTION_GRADIENT1, new Point(0, y2), SELECTION_GRADIENT2);
                Rectangle bounds = new Rectangle(0, y1, getWidth(), labels.get(i).getHeight());
                g2.setPaint(gradient);
                g2.fill(bounds);
            }
        }
        if (selectionModel.getLeadSelectionIndex() != -1) {
            JLabel anchor = labels.get(selectionModel.getLeadSelectionIndex());
            int y1 = anchor.getY();
            int y2 = y1 + anchor.getHeight() - 1;
            Rectangle bounds = new Rectangle(1, y1, getWidth() - 3, anchor.getHeight());
            g2.setColor(new Color(0, 0, 0, 128));
            g2.draw(bounds);
        }
        g2.dispose();
    }

    ListSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
	 * @return the dataModel
	 */
    public ChildSkillGroup getDataModel() {
        return dataModel;
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        Cursor cursor = null;
        if (dge.getDragAction() == DnDConstants.ACTION_MOVE) {
            cursor = DragSource.DefaultMoveDrop;
        }
        draggedSkill = getSkillNameAt(dge.getDragOrigin());
        if (draggedSkill != null) {
            SkillTransferable transferable = new SkillTransferable(dataModel.getWeightedChildSkill(draggedSkill));
            dge.startDrag(cursor, transferable);
        }
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (draggedSkill == null) {
            super.paint(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        Dimension size = super.getSize();
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, size.width, size.height);
        int y1 = labels.get(draggedSkill).getBounds().y;
        int h = labels.get(draggedSkill).getBounds().height - 1;
        super.paintBorder(g2);
        g2.setPaint(ColorUtils.darken(getBackground(), DRAG_DARKEN_AMOUNT));
        g2.fillRect(1, y1, size.width - 2, h);
        paintChildren(g2);
        g2.dispose();
    }

    /**
	 * Add a listener to be notified when the delete button is pressed.
	 */
    public void addDeleteListener(DeleteListener listener) {
        deleteListeners.add(listener);
    }

    /**
	 * Remove a listener to no longer be notified when the delete button is pressed.
	 */
    public void removeDeleteListener(DeleteListener listener) {
        deleteListeners.remove(listener);
    }

    private void performDelete(String deleteTarget) {
        for (DeleteListener dl : deleteListeners) {
            dl.delete(deleteTarget);
        }
    }
}

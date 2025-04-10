package sabokan;

import java.lang.reflect.Constructor;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import sabokan.game.Trace;
import sabokan.game.entities.Positionable;
import sabokan.game.entities.characters.Char;
import sabokan.game.entities.tiles.Tile;
import sabokan.game.enums.BoxEnum;
import sabokan.game.enums.CharacterEnum;
import sabokan.game.enums.ItemEnum;
import sabokan.game.enums.PlayerEnum;

/**
 * Container frame that renders LevelEditor
 * @author anaka
 */
public class LevelEditor extends javax.swing.JDialog {

    public LevelEditor(java.awt.Frame parent) {
        super(parent);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        mainPanel = new javax.swing.JSplitPane();
        previewPanel = new javax.swing.JScrollPane();
        editorPanel = new sabokan.game.EditorPanel();
        controlsPanel = new javax.swing.JScrollPane();
        controlsPanelContainer = new javax.swing.JPanel();
        tileComboBox = new javax.swing.JComboBox();
        setTileButton = new javax.swing.JButton();
        setPlayerButton = new javax.swing.JButton();
        addItemButton = new javax.swing.JButton();
        addCharButton = new javax.swing.JButton();
        playerComboBox = new javax.swing.JComboBox();
        itemComboBox = new javax.swing.JComboBox();
        characterComboBox = new javax.swing.JComboBox();
        resetButton = new javax.swing.JButton();
        addBoxButton = new javax.swing.JButton();
        boxComboBox = new javax.swing.JComboBox();
        saveButton = new javax.swing.JButton();
        characterTextField = new javax.swing.JTextField();
        loadButton = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form");
        mainPanel.setDividerLocation(500);
        mainPanel.setResizeWeight(0.5);
        mainPanel.setName("mainPanel");
        previewPanel.setName("previewPanel");
        editorPanel.setName("editorPanel");
        editorPanel.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editorPanelMouseClicked(evt);
            }
        });
        javax.swing.GroupLayout editorPanelLayout = new javax.swing.GroupLayout(editorPanel);
        editorPanel.setLayout(editorPanelLayout);
        editorPanelLayout.setHorizontalGroup(editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 540, Short.MAX_VALUE));
        editorPanelLayout.setVerticalGroup(editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 749, Short.MAX_VALUE));
        previewPanel.setViewportView(editorPanel);
        mainPanel.setLeftComponent(previewPanel);
        controlsPanel.setName("controlsPanel");
        controlsPanelContainer.setName("controlsPanelContainer");
        tileComboBox.setModel(new javax.swing.DefaultComboBoxModel(Tile.values()));
        tileComboBox.setName("tileComboBox");
        setTileButton.setText("Set Tile");
        setTileButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setTileButtonActionPerformed(evt);
            }
        });
        setPlayerButton.setText("Set Player");
        setPlayerButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setPlayerButtonActionPerformed(evt);
            }
        });
        addItemButton.setText("Add Item");
        addItemButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemButtonActionPerformed(evt);
            }
        });
        addCharButton.setText("Add Character");
        addCharButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCharButtonActionPerformed(evt);
            }
        });
        playerComboBox.setModel(new javax.swing.DefaultComboBoxModel(PlayerEnum.values()));
        playerComboBox.setName("playerComboBox");
        itemComboBox.setModel(new javax.swing.DefaultComboBoxModel(ItemEnum.values()));
        itemComboBox.setName("itemComboBox");
        characterComboBox.setModel(new javax.swing.DefaultComboBoxModel(CharacterEnum.values()));
        characterComboBox.setName("characterComboBox");
        resetButton.setText("Reset");
        resetButton.setName("resetButton");
        resetButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        addBoxButton.setText("Add Box");
        addBoxButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBoxButtonActionPerformed(evt);
            }
        });
        boxComboBox.setModel(new javax.swing.DefaultComboBoxModel(BoxEnum.values()));
        boxComboBox.setName("boxComboBox");
        saveButton.setText("Save");
        saveButton.setName("saveButton");
        saveButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        characterTextField.setColumns(5);
        characterTextField.setText("Dialog Text Here");
        characterTextField.setName("characterTextField");
        loadButton.setText("Load");
        loadButton.setName("loadButton");
        loadButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout controlsPanelContainerLayout = new javax.swing.GroupLayout(controlsPanelContainer);
        controlsPanelContainer.setLayout(controlsPanelContainerLayout);
        controlsPanelContainerLayout.setHorizontalGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(controlsPanelContainerLayout.createSequentialGroup().addContainerGap().addGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(controlsPanelContainerLayout.createSequentialGroup().addGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(addBoxButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(setPlayerButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(addItemButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(addCharButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(setTileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(boxComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(tileComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(playerComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(itemComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(characterComboBox, 0, 140, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(characterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(controlsPanelContainerLayout.createSequentialGroup().addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(loadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        controlsPanelContainerLayout.setVerticalGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(controlsPanelContainerLayout.createSequentialGroup().addContainerGap().addGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(setTileButton).addComponent(tileComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(setPlayerButton).addComponent(playerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(addItemButton).addComponent(itemComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(addCharButton).addComponent(characterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(characterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(addBoxButton).addComponent(boxComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(controlsPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(loadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap(547, Short.MAX_VALUE)));
        controlsPanel.setViewportView(controlsPanelContainer);
        mainPanel.setRightComponent(controlsPanel);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1035, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        pack();
    }

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {
        editorPanel.reset();
    }

    private void editorPanelMouseClicked(java.awt.event.MouseEvent evt) {
        editorPanel.setSelectedTile(evt);
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {
            editorPanel.saveLevel(chooser.getSelectedFile());
        }
    }

    private void addBoxButtonActionPerformed(java.awt.event.ActionEvent evt) {
        editorPanel.addBox(spawnEntity(((BoxEnum) boxComboBox.getSelectedItem()).getClazz()));
    }

    private void addCharButtonActionPerformed(java.awt.event.ActionEvent evt) {
        Char tmpChar = spawnEntity(((CharacterEnum) characterComboBox.getSelectedItem()).getClazz());
        tmpChar.setDialogText(characterTextField.getText() != null ? characterTextField.getText() : "");
        editorPanel.addCharacter(tmpChar);
    }

    private void addItemButtonActionPerformed(java.awt.event.ActionEvent evt) {
        editorPanel.addItem(spawnEntity(((ItemEnum) itemComboBox.getSelectedItem()).getClazz()));
    }

    private void setPlayerButtonActionPerformed(java.awt.event.ActionEvent evt) {
        editorPanel.setPlayer(spawnEntity(((PlayerEnum) playerComboBox.getSelectedItem()).getClazz()));
    }

    private void setTileButtonActionPerformed(java.awt.event.ActionEvent evt) {
        editorPanel.setTile((Tile) tileComboBox.getSelectedItem());
    }

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            editorPanel.loadLevel(chooser.getSelectedFile());
        }
    }

    /**
     * Given a class of Positionable, returns an instance of it
     * @param <T>
     * @param clazz
     * @return 
     */
    private <T extends Positionable> T spawnEntity(Class<T> clazz) {
        T object = null;
        try {
            Integer x = Integer.valueOf(editorPanel.getSelected().x);
            Integer y = Integer.valueOf(editorPanel.getSelected().y);
            Constructor constr = clazz.getConstructor(new Class[] { int.class, int.class });
            object = (T) constr.newInstance(new Object[] { x, y });
        } catch (Exception e) {
            Trace.error("I failed to instantiate " + clazz.getName());
            JOptionPane.showMessageDialog(this, "I failed to instantiate " + clazz.getName(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return object;
    }

    private javax.swing.JButton addBoxButton;

    private javax.swing.JButton addCharButton;

    private javax.swing.JButton addItemButton;

    private javax.swing.JComboBox boxComboBox;

    private javax.swing.JComboBox characterComboBox;

    private javax.swing.JTextField characterTextField;

    private javax.swing.JScrollPane controlsPanel;

    private javax.swing.JPanel controlsPanelContainer;

    private sabokan.game.EditorPanel editorPanel;

    private javax.swing.JComboBox itemComboBox;

    private javax.swing.JButton loadButton;

    private javax.swing.JSplitPane mainPanel;

    private javax.swing.JComboBox playerComboBox;

    private javax.swing.JScrollPane previewPanel;

    private javax.swing.JButton resetButton;

    private javax.swing.JButton saveButton;

    private javax.swing.JButton setPlayerButton;

    private javax.swing.JButton setTileButton;

    private javax.swing.JComboBox tileComboBox;
}

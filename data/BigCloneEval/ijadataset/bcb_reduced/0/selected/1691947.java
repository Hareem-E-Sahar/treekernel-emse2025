package com.jiexplorer.gui.table;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import com.jiexplorer.JIExplorer;
import com.jiexplorer.db.JIThumbnailCache;
import com.jiexplorer.db.JIThumbnailService;
import com.jiexplorer.gui.BatchRenameDialog;
import com.jiexplorer.gui.JIFileModel;
import com.jiexplorer.gui.StatusBarPanel;
import com.jiexplorer.gui.cattree.JICatTreeFrame;
import com.jiexplorer.gui.dnd.TransferActionListener;
import com.jiexplorer.gui.keyword.KeyWordsFrame;
import com.jiexplorer.gui.metadata.JIMetaDataFrame;
import com.jiexplorer.gui.preferences.JIPreferences;
import com.jiexplorer.gui.viewer.JIViewer;
import com.jiexplorer.util.DiskObject;
import com.jiexplorer.util.JIObservable;
import com.jiexplorer.util.JIObserver;
import com.jiexplorer.util.JIUtility;
import com.jiexplorer.util.OpenWith;
import com.jiexplorer.util.OrderedDiskObjectList;

public class JIFileTable extends JTable implements JIObservable {

    /**
	 *
	 */
    private static final long serialVersionUID = 7363111233553268973L;

    private final TransferActionListener actionListener;

    private final JMenuItem jMenuItemOpen;

    private final JMenuItem jMenuItemOpenWith;

    private final JMenuItem jMenuItemRefresh;

    private final JMenuItem jMenuItemRefreshIcon;

    private final JMenuItem jMenuItemRemoveFromDB;

    private final JMenuItem jMenuItemRemoveKeyWords;

    private final JMenuItem jMenuItemRemoveCategories;

    private final JMenuItem jMenuItemRefreshList;

    private final JMenuItem jMenuItemRename;

    private final JMenuItem jMenuItemBatchRename;

    private final JMenuItem jMenuItemDelete;

    private final JMenuItem jMenuItemKeyWords;

    private final JMenuItem jMenuItemCatagory;

    private final JMenuItem jMenuItemMetadata;

    private final JMenuItem jMenuItemCut;

    private final JMenuItem jMenuItemCopy;

    private final JMenuItem jMenuItemPaste;

    private final JPopupMenu jDesktopPopupMenu;

    private KeyWordsFrame keyWordsFrame;

    private JICatTreeFrame catagorysFrame;

    private DiskObject lastSelectedDiskObject = null;

    private final Vector<JIObserver> obs;

    private boolean changed;

    public JIFileTable() {
        this(null);
    }

    public JIFileTable(final OrderedDiskObjectList dObjList) {
        super();
        this.getColumnModel().addColumn(new TableColumn());
        this.getColumnModel().getColumn(0).setPreferredWidth(250);
        this.getColumnModel().getColumn(0).setMinWidth(250);
        this.getColumnModel().getColumn(0).setMaxWidth(500);
        this.getColumnModel().addColumn(new TableColumn());
        this.getColumnModel().getColumn(1).setPreferredWidth(70);
        this.getColumnModel().getColumn(1).setMinWidth(70);
        this.getColumnModel().getColumn(1).setMaxWidth(140);
        this.getColumnModel().addColumn(new TableColumn());
        this.getColumnModel().getColumn(2).setPreferredWidth(50);
        this.getColumnModel().getColumn(2).setMinWidth(50);
        this.getColumnModel().getColumn(2).setMaxWidth(100);
        this.getColumnModel().addColumn(new TableColumn());
        this.getColumnModel().getColumn(3).setPreferredWidth(120);
        this.getColumnModel().getColumn(3).setMinWidth(120);
        this.getColumnModel().getColumn(3).setMaxWidth(240);
        this.getColumnModel().addColumn(new TableColumn());
        this.getColumnModel().getColumn(4).setPreferredWidth(95);
        this.getColumnModel().getColumn(4).setMinWidth(95);
        this.getColumnModel().getColumn(4).setMaxWidth(180);
        this.getColumnModel().addColumn(new TableColumn());
        this.getColumnModel().getColumn(5).setPreferredWidth(1000);
        this.getColumnModel().getColumn(5).setMinWidth(0);
        this.getColumnModel().getColumn(5).setMaxWidth(2000);
        if (dObjList != null) {
            setModel(new JIFileTableModel(this, dObjList));
        } else {
            setModel(new JIFileTableModel(this));
        }
        final JIFileTableCellRenderer diskObjCellRenderer = new JIFileTableCellRenderer();
        diskObjCellRenderer.setMinimumSize(new Dimension(250, 40));
        diskObjCellRenderer.setBackground(new Color(235, 235, 235));
        diskObjCellRenderer.setForeground(Color.black);
        setDefaultRenderer(DiskObject.class, diskObjCellRenderer);
        this.obs = new Vector<JIObserver>();
        this.changed = false;
        this.actionListener = new TransferActionListener();
        this.jMenuItemOpen = new JMenuItem();
        this.jMenuItemOpenWith = new JMenu();
        this.jMenuItemRefresh = new JMenu();
        this.jMenuItemRefreshIcon = new JMenuItem();
        this.jMenuItemRemoveFromDB = new JMenuItem();
        this.jMenuItemRemoveKeyWords = new JMenuItem();
        this.jMenuItemRemoveCategories = new JMenuItem();
        this.jMenuItemRefreshList = new JMenuItem();
        this.jMenuItemRename = new JMenuItem();
        this.jMenuItemBatchRename = new JMenuItem();
        this.jMenuItemDelete = new JMenuItem();
        this.jMenuItemKeyWords = new JMenuItem();
        this.jMenuItemCatagory = new JMenuItem();
        this.jMenuItemMetadata = new JMenuItem();
        this.jMenuItemCut = new JMenuItem();
        this.jMenuItemCopy = new JMenuItem();
        this.jMenuItemPaste = new JMenuItem();
        this.jDesktopPopupMenu = new JPopupMenu();
        this.showHorizontalLines = false;
        this.showVerticalLines = false;
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(true);
        initActions();
        JIThumbnailCache.getInstance().invalidate();
    }

    private final void initOpenWith() {
        this.jMenuItemOpenWith.removeAll();
        this.jMenuItemOpenWith.setText("Open With");
        final Vector<OpenWith> openWithCmds = JIThumbnailService.getInstance().getOpenWith();
        for (final OpenWith openWith : openWithCmds) {
            final JMenuItem openItem = new JMenuItem();
            openItem.setText(openWith.getCommandName());
            openItem.setAction(new AbstractAction(openWith.getCommandName()) {

                /**
				 *
				 */
                private static final long serialVersionUID = -6218105533266200846L;

                public void actionPerformed(final ActionEvent e) {
                    final Thread runner = new Thread() {

                        @Override
                        public void run() {
                            Runnable runnable = new Runnable() {

                                public void run() {
                                    final Object[] list = getSelectedValues();
                                    final String[] args = new String[list.length];
                                    int cnt = 0;
                                    for (Object obj : list) {
                                        args[cnt++] = ((DiskObject) obj).getPath();
                                    }
                                    openWith.run(args);
                                }
                            };
                            SwingUtilities.invokeLater(runnable);
                        }
                    };
                    runner.start();
                }
            });
            this.jMenuItemOpenWith.add(openItem);
        }
    }

    protected final void initActions() {
        this.jMenuItemOpen.setText("Open");
        this.jMenuItemOpen.setAction(new AbstractAction("Open") {

            /**
			 *
			 */
            private static final long serialVersionUID = -4974543208624992733L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                final int index = getSelectedRow();
                                final OrderedDiskObjectList imageList = ((JIFileTableModel) getModel()).getDiskObjectList();
                                imageList.setCurrentIndex(index);
                                new JIViewer(imageList);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        initOpenWith();
        this.jMenuItemRefreshIcon.setText("Refresh Icon");
        this.jMenuItemRefreshIcon.setAction(new AbstractAction("Refresh Icon") {

            /**
			 *
			 */
            private static final long serialVersionUID = -4913568179844130255L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                DiskObject[] dObjs = getSelectedValues();
                                JIThumbnailService.getInstance().refreshThumbnails(dObjs);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        this.jMenuItemRemoveFromDB.setText("Remove From DB");
        this.jMenuItemRemoveFromDB.setAction(new AbstractAction("Remove From DB") {

            /**
			 *
			 */
            private static final long serialVersionUID = -5907397468048195552L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                DiskObject[] dObjs = getSelectedValues();
                                JIThumbnailService.getInstance().removeFromDBFor(dObjs);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        this.jMenuItemRemoveKeyWords.setText("Remove Key Words");
        this.jMenuItemRemoveKeyWords.setAction(new AbstractAction("Remove Key Words") {

            /**
			 *
			 */
            private static final long serialVersionUID = -3668850328834885142L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                DiskObject[] dObjs = getSelectedValues();
                                JIThumbnailService.getInstance().removeKeyWordsFor(dObjs);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        this.jMenuItemRemoveCategories.setText("Remove Categories");
        this.jMenuItemRemoveCategories.setAction(new AbstractAction("Remove Categories") {

            /**
			 *
			 */
            private static final long serialVersionUID = -5827674129277593801L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                DiskObject[] dObjs = getSelectedValues();
                                JIThumbnailService.getInstance().removeCategoriesFor(dObjs);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        this.jMenuItemRefreshList.setText("Refresh List");
        this.jMenuItemRefreshList.setAction(new AbstractAction("Refresh List") {

            /**
			 *
			 */
            private static final long serialVersionUID = 6973778660573038262L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                getFileListModel().reload();
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        this.jMenuItemRefreshList.setText("Refresh");
        this.jMenuItemRefreshList.setAction(new AbstractAction("Refresh") {

            /**
			 *
			 */
            private static final long serialVersionUID = 5054876186607504643L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                ((JIFileTableModel) JIFileTable.this.getModel()).reload();
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        this.jMenuItemKeyWords.setText("Key Words");
        this.jMenuItemKeyWords.setAction(new AbstractAction("Key Words") {

            /**
			 *
			 */
            private static final long serialVersionUID = -5989788886126220896L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                if ((JIFileTable.this.keyWordsFrame == null) || JIFileTable.this.keyWordsFrame.isDisplayable()) {
                                    JIFileTable.this.keyWordsFrame = new KeyWordsFrame();
                                }
                                JIFileTable.this.keyWordsFrame.loadListModels();
                                JIFileTable.this.keyWordsFrame.requestFocus();
                                JIFileTable.this.keyWordsFrame.toFront();
                                JIFileTable.this.keyWordsFrame.setVisible(true);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        this.jMenuItemCatagory.setText("Categories");
        this.jMenuItemCatagory.setAction(new AbstractAction("Categories") {

            /**
			 *
			 */
            private static final long serialVersionUID = 6683001344373925091L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                if ((JIFileTable.this.catagorysFrame != null) && JIFileTable.this.catagorysFrame.isDisplayable()) {
                                    JIFileTable.this.catagorysFrame.setTitle();
                                    JIFileTable.this.catagorysFrame.requestFocus();
                                    JIFileTable.this.catagorysFrame.toFront();
                                } else {
                                    JIFileTable.this.catagorysFrame = new JICatTreeFrame();
                                }
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        this.jMenuItemMetadata.setText("Metadata");
        this.jMenuItemMetadata.setAction(new AbstractAction("Metadata") {

            /**
			 *
			 */
            private static final long serialVersionUID = -8686986974748727491L;

            public void actionPerformed(final ActionEvent e) {
                final Thread runner = new Thread() {

                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {

                            public void run() {
                                if (JIFileTable.this.getSelectedRow() > -1) {
                                    new JIMetaDataFrame();
                                }
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                };
                runner.start();
            }
        });
        this.jMenuItemBatchRename.setText("Batch Rename");
        this.jMenuItemBatchRename.setAction(new AbstractAction("Batch Rename") {

            /**
			 *
			 */
            private static final long serialVersionUID = 3871344047915741026L;

            public void actionPerformed(final ActionEvent e) {
                if ((getSelectedValues() != null) && (getSelectedValues().length > 0)) {
                    final BatchRenameDialog brd = new BatchRenameDialog();
                    brd.setVisible(true);
                }
            }
        });
        this.jMenuItemCut.setText("Cut");
        this.jMenuItemCut.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
        this.jMenuItemCut.addActionListener(this.actionListener);
        this.jMenuItemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        this.jMenuItemCut.setMnemonic(KeyEvent.VK_U);
        this.jMenuItemCopy.setText("Copy");
        this.jMenuItemCopy.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
        this.jMenuItemCopy.addActionListener(this.actionListener);
        this.jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        this.jMenuItemCopy.setMnemonic(KeyEvent.VK_C);
        this.jMenuItemPaste.setText("Paste");
        this.jMenuItemPaste.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
        this.jMenuItemPaste.addActionListener(this.actionListener);
        this.jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        this.jMenuItemPaste.setMnemonic(KeyEvent.VK_P);
        addMouseListener(new PopupTrigger());
    }

    public final Object getSelectedValue() {
        if (this.getSelectedRow() > -1) {
            return ((JIFileTableModel) this.getModel()).dataVector.elementAt(0);
        }
        return null;
    }

    public JIFileModel getFileListModel() {
        return (JIFileTableModel) getModel();
    }

    public final Object getElementAt(final int row) {
        return this.getModel().getValueAt(row, 0);
    }

    public final DiskObject getDiskObjectAt(final int row) {
        return (DiskObject) this.getModel().getValueAt(row, 0);
    }

    public final DiskObject[] getSelectedValues() {
        final int[] index = this.getSelectedRows();
        final DiskObject[] values = new DiskObject[index.length];
        for (int i = 0; i < index.length; i++) {
            values[i] = this.getDiskObjectAt(index[i]);
        }
        return values;
    }

    public int getLastSelectedIndex() {
        final Object[] objs = getSelectedValues();
        final Object obj = getSelectedValue();
        int cnt = 0;
        for (final Object o : objs) {
            if (o.equals(obj)) {
                return cnt;
            }
            cnt++;
        }
        return -1;
    }

    @Override
    public final void valueChanged(final ListSelectionEvent e) {
        super.valueChanged(e);
        if (this.lastSelectedDiskObject == null) {
            if (getModel().getRowCount() > e.getLastIndex()) {
                this.lastSelectedDiskObject = (DiskObject) getModel().getValueAt(e.getLastIndex(), 0);
            } else {
                return;
            }
        }
        JIExplorer.instance().getContext().setSelectedDiskObjects(this.getSelectedValues(), this.lastSelectedDiskObject);
        this.lastSelectedDiskObject = null;
        setChanged();
        notifyObservers(JIObservable.SECTION_CHANGED);
        clearChanged();
    }

    private final void componentMouseClicked(final MouseEvent e) {
        final int index = this.rowAtPoint(e.getPoint());
        final Object sobject = getSelectedValue();
        if ((sobject == null) || !(sobject instanceof DiskObject)) {
            clearSelection();
            return;
        }
        final DiskObject selectedObject = (DiskObject) sobject;
        final File selectedFile = selectedObject.getFile();
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
            if (JIUtility.isSupportedImage(selectedObject.getSuffix())) {
                try {
                    final OrderedDiskObjectList imageList = ((JIFileTableModel) getModel()).getDiskObjectList();
                    imageList.setCurrentIndex(index);
                    new JIViewer(imageList);
                } catch (final Exception de) {
                    de.printStackTrace();
                    JOptionPane.showMessageDialog(this, de.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (selectedObject.getFile().isDirectory()) {
                JIExplorer.instance().getJDirTree().setSelectedDir(selectedObject);
            }
            e.consume();
        }
    }

    private final void componentMouseEvent(final MouseEvent e) {
        if ((e.getButton() == 3) && (e.getClickCount() == 1)) {
            try {
                final Object obj = getSelectedValue();
                if (obj == null) {
                    this.jDesktopPopupMenu.removeAll();
                    this.jDesktopPopupMenu.add(this.jMenuItemRefreshList);
                    this.jDesktopPopupMenu.addSeparator();
                    this.jDesktopPopupMenu.add(this.jMenuItemPaste);
                    if (Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
                        this.jMenuItemPaste.setEnabled(true);
                    } else {
                        this.jMenuItemPaste.setEnabled(false);
                    }
                    this.jDesktopPopupMenu.show(this, e.getX(), e.getY());
                } else {
                    final File selectedFile = ((DiskObject) getSelectedValue()).getFile();
                    if (!selectedFile.isDirectory()) {
                        this.jDesktopPopupMenu.removeAll();
                        this.jDesktopPopupMenu.add(this.jMenuItemOpen);
                        this.jDesktopPopupMenu.addSeparator();
                        if ((JIThumbnailService.getInstance().getOpenWith() != null) && (JIThumbnailService.getInstance().getOpenWith().size() > 0)) {
                            this.jDesktopPopupMenu.add(this.jMenuItemOpenWith);
                            this.jDesktopPopupMenu.addSeparator();
                        }
                        this.jDesktopPopupMenu.add(this.jMenuItemRefreshList);
                        this.jMenuItemRefresh.setText("Refresh Selected");
                        this.jMenuItemRefresh.add(this.jMenuItemRefreshIcon);
                        this.jMenuItemRefresh.add(this.jMenuItemRemoveFromDB);
                        this.jMenuItemRefresh.add(this.jMenuItemRemoveKeyWords);
                        this.jMenuItemRefresh.add(this.jMenuItemRemoveCategories);
                        this.jDesktopPopupMenu.add(this.jMenuItemRefresh);
                        this.jDesktopPopupMenu.addSeparator();
                        this.jDesktopPopupMenu.add(this.jMenuItemKeyWords);
                        this.jDesktopPopupMenu.add(this.jMenuItemCatagory);
                        this.jDesktopPopupMenu.add(this.jMenuItemMetadata);
                        this.jDesktopPopupMenu.addSeparator();
                        this.jDesktopPopupMenu.add(this.jMenuItemCut);
                        this.jDesktopPopupMenu.add(this.jMenuItemCopy);
                        this.jDesktopPopupMenu.add(this.jMenuItemPaste);
                        if (Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
                            this.jMenuItemPaste.setEnabled(true);
                        } else {
                            this.jMenuItemPaste.setEnabled(false);
                        }
                        this.jDesktopPopupMenu.show(this, e.getX(), e.getY());
                    } else {
                        this.jDesktopPopupMenu.removeAll();
                        this.jDesktopPopupMenu.add(this.jMenuItemOpen);
                        if ((JIPreferences.getInstance().getOpenWith() != null) && (JIPreferences.getInstance().getOpenWith().size() > 0)) {
                            this.jDesktopPopupMenu.add(this.jMenuItemOpenWith);
                            this.jDesktopPopupMenu.addSeparator();
                        }
                        this.jDesktopPopupMenu.add(this.jMenuItemRefreshList);
                        this.jDesktopPopupMenu.addSeparator();
                        this.jDesktopPopupMenu.add(this.jMenuItemCopy);
                        this.jDesktopPopupMenu.add(this.jMenuItemPaste);
                        if (Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
                            this.jMenuItemPaste.setEnabled(true);
                        } else {
                            this.jMenuItemPaste.setEnabled(false);
                        }
                        this.jDesktopPopupMenu.show(this, e.getX(), e.getY());
                    }
                }
            } catch (final Exception exp) {
                exp.printStackTrace();
            } finally {
                e.consume();
            }
        }
    }

    final class PopupTrigger extends MouseAdapter {

        @Override
        public void mousePressed(final MouseEvent e) {
            final int index = JIFileTable.this.rowAtPoint(e.getPoint());
            final DiskObject selectedObject = (DiskObject) JIFileTable.this.getModel().getValueAt(index, 0);
            if ((selectedObject != null) && (selectedObject instanceof DiskObject)) {
                JIFileTable.this.lastSelectedDiskObject = selectedObject;
            }
            final Thread runner = new Thread() {

                @Override
                public void run() {
                    Runnable runnable = new Runnable() {

                        public void run() {
                            componentMouseEvent(e);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
            };
            runner.start();
        }

        @Override
        public final void mouseClicked(final MouseEvent e) {
            final Thread runner = new Thread() {

                @Override
                public void run() {
                    Runnable runnable = new Runnable() {

                        public void run() {
                            componentMouseClicked(e);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
            };
            runner.start();
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            final Thread runner = new Thread() {

                @Override
                public void run() {
                    Runnable runnable = new Runnable() {

                        public void run() {
                            componentMouseEvent(e);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
            };
            runner.start();
        }
    }

    final class JIThumbnailKeyAdapter extends java.awt.event.KeyAdapter {

        /** Creates a new instance of JIThumbnailKeyListener */
        public JIThumbnailKeyAdapter() {
        }

        /** key event handlers */
        @Override
        public final void keyPressed(final KeyEvent e) {
            final Thread runner = new Thread() {

                @Override
                public void run() {
                    Runnable runnable = new Runnable() {

                        public void run() {
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
            };
            runner.start();
        }
    }

    /**
	 * Adds an observer to the set of observers for this object, provided
	 * that it is not the same as some observer already in the set.
	 * The order in which notifications will be delivered to multiple
	 * observers is not specified. See the class comment.
	 *
	 * @param   o   an observer to be added.
	 * @throws NullPointerException   if the parameter o is null.
	 */
    public synchronized void addObserver(final JIObserver o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!this.obs.contains(o)) {
            this.obs.addElement(o);
        }
    }

    /**
	 * Deletes an observer from the set of observers of this object.
	 * Passing <CODE>null</CODE> to this method will have no effect.
	 * @param   o   the observer to be deleted.
	 */
    public synchronized void deleteObserver(final JIObserver o) {
        this.obs.removeElement(o);
    }

    /**
	 * If this object has changed, as indicated by the
	 * <code>hasChanged</code> method, then notify all of its observers
	 * and then call the <code>clearChanged</code> method to
	 * indicate that this object has no longer changed.
	 * <p>
	 * Each observer has its <code>update</code> method called with two
	 * arguments: this observable object and <code>null</code>. In other
	 * words, this method is equivalent to:
	 * <blockquote><tt>
	 * notifyObservers(null)</tt></blockquote>
	 *
	 * @see     java.util.Observable#clearChanged()
	 * @see     java.util.Observable#hasChanged()
	 * @see     java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
    public void notifyObservers() {
        notifyObservers(null);
    }

    /**
	 * If this object has changed, as indicated by the
	 * <code>hasChanged</code> method, then notify all of its observers
	 * and then call the <code>clearChanged</code> method to indicate
	 * that this object has no longer changed.
	 * <p>
	 * Each observer has its <code>update</code> method called with two
	 * arguments: this observable object and the <code>arg</code> argument.
	 *
	 * @param   arg   any object.
	 * @see     java.util.Observable#clearChanged()
	 * @see     java.util.Observable#hasChanged()
	 * @see     java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
    public void notifyObservers(final Object arg) {
        Object[] arrLocal;
        synchronized (this) {
            if (!this.changed) {
                return;
            }
            arrLocal = this.obs.toArray();
            clearChanged();
        }
        for (int i = arrLocal.length - 1; i >= 0; i--) {
            ((JIObserver) arrLocal[i]).update(this, arg);
        }
    }

    public void notifyStatusBar(final Object arg) {
        Object[] arrLocal;
        synchronized (this) {
            arrLocal = this.obs.toArray();
            clearChanged();
        }
        for (int i = arrLocal.length - 1; i >= 0; i--) {
            if (arrLocal[i] instanceof StatusBarPanel) {
                ((JIObserver) arrLocal[i]).update(this, arg);
            }
        }
    }

    /**
	 * Clears the observer list so that this object no longer has any observers.
	 */
    public synchronized void deleteObservers() {
        this.obs.removeAllElements();
    }

    /**
	 * Marks this <tt>Observable</tt> object as having been changed; the
	 * <tt>hasChanged</tt> method will now return <tt>true</tt>.
	 */
    public synchronized void setChanged() {
        this.changed = true;
    }

    /**
	 * Indicates that this object has no longer changed, or that it has
	 * already notified all of its observers of its most recent change,
	 * so that the <tt>hasChanged</tt> method will now return <tt>false</tt>.
	 * This method is called automatically by the
	 * <code>notifyObservers</code> methods.
	 *
	 * @see     java.util.Observable#notifyObservers()
	 * @see     java.util.Observable#notifyObservers(java.lang.Object)
	 */
    public synchronized void clearChanged() {
        this.changed = false;
    }

    /**
	 * Tests if this object has changed.
	 *
	 * @return  <code>true</code> if and only if the <code>setChanged</code>
	 *          method has been called more recently than the
	 *          <code>clearChanged</code> method on this object;
	 *          <code>false</code> otherwise.
	 * @see     java.util.Observable#clearChanged()
	 * @see     java.util.Observable#setChanged()
	 */
    public synchronized boolean hasChanged() {
        return this.changed;
    }

    /**
	 * Returns the number of observers of this <tt>Observable</tt> object.
	 *
	 * @return  the number of observers of this object.
	 */
    public synchronized int countObservers() {
        return this.obs.size();
    }
}

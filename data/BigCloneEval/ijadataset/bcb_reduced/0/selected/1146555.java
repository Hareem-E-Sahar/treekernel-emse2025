package de.hpi.eworld.simulationstatistic;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import de.hpi.eworld.core.ui.MainWindow;
import de.hpi.eworld.core.ui.docking.DefaultLayoutDockingManager;
import de.hpi.eworld.core.ui.docking.DockArea;
import de.hpi.eworld.core.ui.docking.DockingManager;
import de.hpi.eworld.core.ui.docking.DockingView;
import de.hpi.eworld.core.ui.docking.MultipleDockGroup;
import de.hpi.eworld.networkview.GraphController;
import de.hpi.eworld.observer.NotificationType;
import de.hpi.eworld.observer.ObserverNotification;
import de.hpi.eworld.simulationstatistic.model.StatDataset;
import de.hpi.eworld.simulationstatistic.model.StatisticsDataManager;
import de.hpi.eworld.simulationstatistic.model.Value;

/**
 * Provides the user with options to control the generation and display of
 * statistical data of simulation runs.
 * 
 * @author Philipp Maschke
 */
public class SimulationStatistic extends MultipleDockGroup {

    private static final long serialVersionUID = -9026297614736087400L;

    public final Observable observable = new Observable();

    /**
	 * A Widget with options for the visualization of data in the map.
	 */
    private MapVisualizerWidget visualizeWidget;

    /**
	 * A Widget for managing the different statistical datasets
	 */
    private JPanel statDataWidget;

    /**
	 * A List Widget for displaying all currently stored statistical datasets.
	 */
    private JPopupMenu datasetsListPopup;

    private JList datasetsList;

    /**
	 * A tab widget for organizing the different display widgets into one dock
	 * widget
	 */
    private JPanel tabWidget;

    private MainWindow parentWindow = null;

    /**
	 * A check box for the user to specify, whether he wants statistical data to
	 * be automatically imported after simulation finish
	 */
    private JCheckBox statAutoImport;

    private Logger logger;

    private JPanel mainPanel;

    /**
	 * Triggers the visualization of the current data set on the map
	 */
    private JMenuItem showOnMapAction;

    private DefaultListModel listModel = null;

    private class ListValue {

        private String name;

        private String id;

        public ListValue(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }
    }

    /**
	 * The simstat dock constructor creates a new dock widget and places it on
	 * the left side of the main window.
	 * 
	 * @param parent
	 *            A reference to the parent widget
	 * @param title
	 *            The title for this dock widget
	 * @param menuItem 
	 */
    public SimulationStatistic(JCheckBoxMenuItem menuItem) {
        super(menuItem);
        mainPanel = new JPanel();
        this.logger = Logger.getLogger(this.getClass());
        StatisticsDataManager smm = StatisticsDataManager.getInstance();
        smm.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof ObserverNotification) {
                    final ObserverNotification notification = (ObserverNotification) arg;
                    final NotificationType type = notification.getType();
                    if (type.equals(NotificationType.displayInfoBox)) {
                        showInfoBox((String) notification.getObj1(), (String) notification.getObj2());
                    }
                }
            }
        });
        tabWidget = new JPanel();
        tabWidget.setMinimumSize(new Dimension(150, 200));
        tabWidget.setMaximumSize(new Dimension(235, 9999));
        statDataWidget = new JPanel();
        GridBagLayout dataLayout = new GridBagLayout();
        listModel = new DefaultListModel();
        datasetsList = new JList();
        datasetsListPopup = new JPopupMenu();
        datasetsListPopup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                displayDatasetItem((ListValue) e.getSource());
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        JMenuItem showPropertiesAction = new JMenuItem("Properties");
        showPropertiesAction.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                showProperties();
            }
        });
        datasetsListPopup.add(showPropertiesAction);
        JMenuItem showChartsAction = new JMenuItem("Show charts");
        showChartsAction.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                showCharts();
            }
        });
        datasetsListPopup.add(showChartsAction);
        showOnMapAction = new JMenuItem("Show on map");
        showOnMapAction.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                initShowOnMap();
            }
        });
        datasetsListPopup.add(showOnMapAction);
        JMenuItem showRawAction = new JMenuItem("Open data file");
        showRawAction.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                showRawData();
            }
        });
        datasetsListPopup.add(showRawAction);
        JMenuItem renameSetAction = new JMenuItem("Rename");
        renameSetAction.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                onRenameDataset();
            }
        });
        datasetsListPopup.add(renameSetAction);
        JMenuItem deleteSetAction = new JMenuItem("Remove");
        deleteSetAction.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                onDeleteDataset();
            }
        });
        datasetsListPopup.add(deleteSetAction);
        JMenuItem deleteAllAction = new JMenuItem("Remove all");
        deleteAllAction.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                onDeleteAllDatasets();
            }
        });
        datasetsListPopup.add(deleteAllAction);
        datasetsList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                checkBelongingToMap();
            }
        });
        statAutoImport = new JCheckBox("Automatically import data");
        statAutoImport.setToolTipText("Import of the SUMO dump data will be triggered automatically when the simulation finishes");
        statAutoImport.setSelected(true);
        final GridBagConstraints datasetsListConstraints = new GridBagConstraints();
        datasetsListConstraints.gridx = 0;
        datasetsListConstraints.gridy = 0;
        datasetsListConstraints.fill = GridBagConstraints.HORIZONTAL;
        dataLayout.setConstraints(datasetsList, datasetsListConstraints);
        mainPanel.add(datasetsList);
        final GridBagConstraints statAutoImportConstraints = new GridBagConstraints();
        statAutoImportConstraints.gridx = 1;
        statAutoImportConstraints.gridy = 0;
        statAutoImportConstraints.gridwidth = 1;
        statAutoImportConstraints.fill = GridBagConstraints.HORIZONTAL;
        dataLayout.setConstraints(statAutoImport, statAutoImportConstraints);
        mainPanel.add(statAutoImport);
        statDataWidget.setLayout(dataLayout);
        smm.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof ObserverNotification) {
                    final ObserverNotification notification = (ObserverNotification) arg;
                    final NotificationType type = notification.getType();
                    if (type.equals(NotificationType.datasetsChanged)) {
                        onDatasetsChanged();
                    }
                }
            }
        });
        smm.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof ObserverNotification) {
                    final ObserverNotification notification = (ObserverNotification) arg;
                    final NotificationType type = notification.getType();
                    if (type.equals(NotificationType.datasetsCleared)) {
                        onDatasetsCleared();
                    }
                }
            }
        });
    }

    /**
	 * Displays a short info about the statistical dataset represented by item
	 * 
	 * @param item
	 *            the listwidget item to display
	 */
    private void displayDatasetItem(ListValue value) {
        StatDataset dataset = getDataset(value);
        if (dataset != null) JOptionPane.showMessageDialog(parentWindow, dataset.toString(), "Info on statistical dataset", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
	 * Triggers visualization of the current dataset on the map(networkView)
	 */
    private void initShowOnMap() {
        StatDataset dataset = getDataset((ListValue) datasetsList.getSelectedValue());
        visualizeWidget.showOnMap(dataset, Value.SPEED);
    }

    /**
	 * 
	 * @param item
	 *            the listwidget item
	 * @return the dataset represented by the item
	 */
    private StatDataset getDataset(ListValue value) {
        return StatisticsDataManager.getInstance().getDataset(value.getId());
    }

    /**
	 * Is called whenever the datasets list is discovered to be empty. Disables
	 * context menu
	 * 
	 * @param isEmpty
	 */
    private void datasetListEmpty(boolean isEmpty) {
        if (isEmpty) {
        } else {
        }
    }

    /**
	 * SLOT that is called when the user activates the removeAll action
	 */
    private void onDeleteAllDatasets() {
        StatisticsDataManager.getInstance().clear();
    }

    /**
	 * SLOT that is called when the user activates the deleteDataset action
	 */
    private void onDeleteDataset() {
        StatDataset dataset = getDataset((ListValue) datasetsList.getSelectedValue());
        if (dataset != null) StatisticsDataManager.getInstance().removeDataset(dataset.getId()); else onDatasetsCleared();
    }

    /**
	 * SLOT that is called whenever the {@link StatisticsDataManager} signals a
	 * dataset change. Reloads the datasets list and calls
	 * {@link MapVisualizerWidget#datasetsChanged()}
	 * Dis-/Enables visualizer tab depending on existence of visualizable data
	 */
    @SuppressWarnings("unused")
    private void onDatasetsChanged() {
        logger.debug("Received 'datasets changed' signal");
        datasetsListPopup.removeAll();
        StatisticsDataManager smm = StatisticsDataManager.getInstance();
        ArrayList<String> list = new ArrayList<String>();
        if (smm.numDatasets() == 0) {
            datasetListEmpty(true);
        } else {
            datasetListEmpty(false);
            for (final StatDataset dataset : smm.getDatasets()) {
                ListValue itemToAdd = new ListValue(dataset.getDisplayText(), dataset.getId());
                listModel.addElement(itemToAdd);
            }
        }
        visualizeWidget.datasetsChanged();
        if (visualizeWidget.hasDisplayableData()) {
            visualizeWidget.setEnabled(true);
        } else {
            visualizeWidget.setEnabled(false);
        }
    }

    /**
	 * SLOT that is called whenever the {@link StatisticsDataManager} emits the
	 * datasetsCleared signal
	 */
    private void onDatasetsCleared() {
        datasetsListPopup.removeAll();
        datasetListEmpty(true);
        visualizeWidget.datasetsChanged();
        visualizeWidget.setEnabled(false);
    }

    /**
	 * SLOT Opens an {@link QInputDialog} and changes the display text of the
	 * current dataset
	 */
    private void onRenameDataset() {
        StatDataset dataset = getDataset((ListValue) datasetsList.getSelectedValue());
        if (dataset != null) {
            String newName = JOptionPane.showInputDialog(parentWindow, "Please enter new name for this data set", "Rename data set", JOptionPane.PLAIN_MESSAGE);
            if (!((newName == null) || (newName == ""))) dataset.setDisplayText(newName);
            onDatasetsChanged();
        } else onDatasetsCleared();
    }

    /**
	 * Sets the networkview and connects to some signals
	 * to react on map changes
	 * @param networkView
	 */
    public void setNetworkView(GraphController networkView) {
        visualizeWidget.setNetworkView(networkView);
        networkView.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof ObserverNotification) {
                    final ObserverNotification notification = (ObserverNotification) arg;
                    final NotificationType type = notification.getType();
                    if (type.equals(NotificationType.endBatchProcess)) {
                        onBatchEnded();
                    }
                }
            }
        });
        networkView.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof ObserverNotification) {
                    final ObserverNotification notification = (ObserverNotification) arg;
                    final NotificationType type = notification.getType();
                    if (type.equals(NotificationType.startBatchProcess)) {
                        visualizeWidget.stopShowOnMap();
                    }
                }
            }
        });
    }

    /**
	 * Workaround for the calling of {@link StatisticsDataManager#recheckDatasets()},
	 * whenever the map was changed with a batch job
	 */
    private void onBatchEnded() {
        StatisticsDataManager.getInstance().recheckDatasets();
    }

    /**
	 * Open the {@link ChartsDialog}
	 */
    protected void showCharts() {
        StatDataset dataset = getDataset((ListValue) datasetsList.getSelectedValue());
        GraphGeneratorGUI gg = new GraphGeneratorGUI(parentWindow, dataset);
        gg.setVisible(true);
    }

    /**
	 * Opens a system editor to display the file with the original data of the
	 * current dataset
	 */
    protected void showRawData() {
        StatDataset dataset = getDataset((ListValue) datasetsList.getSelectedValue());
        if (Desktop.isDesktopSupported() && dataset != null) {
            try {
                Desktop.getDesktop().open(dataset.getDataFile());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parentWindow, e.getMessage(), "File could not be opened", JOptionPane.WARNING_MESSAGE);
            }
        } else onDatasetsCleared();
    }

    /**
	 * removes this plugin from the main windows dockbar
	 */
    public void shutdown() {
        closeAll();
    }

    /**
	 * Initializes the Dock Widget
	 * 
	 * @param simulationDock
	 *            A reference to the main applications window which to dock to
	 */
    public void startUp(MainWindow parentWindow) {
        this.parentWindow = parentWindow;
        DockingManager dockingManager = DefaultLayoutDockingManager.getInstance();
        DockingView statisticDock = new SimulationStatisticDock(mainPanel);
        addView(statisticDock);
        dockingManager.addView(statisticDock, DockArea.EAST, false, 0);
        visualizeWidget = new MapVisualizerWidget(views.get(0));
        DockingView mapVisualizationDock = new MapVisualizationDock(visualizeWidget);
        addView(mapVisualizationDock);
        dockingManager.addView(mapVisualizationDock, DockArea.EAST, false, 1);
        visualizeWidget.setEnabled(false);
    }

    /**
	 * 
	 * @return
	 */
    public boolean isAutoImportRequested() {
        return statAutoImport.isSelected();
    }

    /**
	 * Slot for the {@link StatisticsDataManager}
	 * Shows an information QMessageBox with the supplied message
	 * and title
	 * @param message
	 *            the message to be displayed to the user
	 * @param title
	 * 			title for the message box
	 */
    private void showInfoBox(String title, String message) {
        JOptionPane.showMessageDialog(parentWindow, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
	 * Called whenever the currently selected data set changes.
	 * Checks whether the new data set belongs to the current map
	 * and de-/activates the "show on map" action accordingly
	 */
    private void checkBelongingToMap() {
        StatDataset dataset = getDataset((ListValue) datasetsList.getSelectedValue());
        if ((dataset != null) && (dataset.belongsToCurrentMap())) showOnMapAction.setEnabled(true); else showOnMapAction.setEnabled(false);
    }

    /**
	 * Opens a message box with some basic information about
	 * the currently selected dataset
	 */
    private void showProperties() {
        displayDatasetItem((ListValue) datasetsList.getSelectedValue());
    }
}

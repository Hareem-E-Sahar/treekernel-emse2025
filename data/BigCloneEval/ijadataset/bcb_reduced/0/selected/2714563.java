package org.ladybug.gui;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTable;
import org.ladybug.log.LogEngine;
import org.ladybug.utils.Gui;

/**
 * @author Aurelian Pop
 */
public class CopyOfLobby extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_TITLE = "Ladybug - Manage Your Development";

    private static final String HELP_URI = "http://lady-bug.sourceforge.net/help.html";

    private static final Dimension MINIMUM_WINDOW_DIMENSION = new Dimension(800, 600);

    private static final Dimension PREFERRED_WINDOW_DIMENSION = new Dimension(1024, 768);

    private static final Dimension MAXIMUM_WINDOW_DIMENSION = new Dimension(1920, 1200);

    private static final Dimension TABLE_INTERCELL_SPACE = new Dimension(20, 20);

    private static final int TABLE_ROW_HEIGHT = 128;

    private final LogEngine logger = LogEngine.getInstance();

    private final MenuBar menuBar = new MenuBar(this);

    private final ProductsTableModel productsTableModel;

    public CopyOfLobby() {
        super(DEFAULT_TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(MINIMUM_WINDOW_DIMENSION);
        setPreferredSize(PREFERRED_WINDOW_DIMENSION);
        setMaximumSize(MAXIMUM_WINDOW_DIMENSION);
        setSize(PREFERRED_WINDOW_DIMENSION);
        Gui.centerScreen(this);
        final Container contentPane = getContentPane();
        final JTable table = new JTable();
        final Table scrollPane = new Table(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPane.add(scrollPane);
        productsTableModel = new ProductsTableModel(table);
        setJMenuBar(menuBar);
        setUpTable(table);
        createData();
    }

    private void setUpTable(final JTable table) {
        table.setTableHeader(null);
        table.setShowGrid(false);
        table.setIntercellSpacing(TABLE_INTERCELL_SPACE);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setBackground(table.getParent().getBackground());
        table.setModel(productsTableModel);
        table.getColumnModel().getColumn(0).setCellRenderer(new ProductsCellRenderer());
        table.addMouseListener(new MouseAdapter() {

            private int selectedRow = -1;

            @Override
            public void mouseClicked(final MouseEvent e) {
                final JTable t = (JTable) e.getSource();
                if (selectedRow != t.getSelectedRow()) {
                    selectedRow = t.getSelectedRow();
                } else {
                    if (2 == e.getClickCount()) {
                        final Slider slider = new Slider();
                        final Container tableParent = table.getParent();
                        slider.slide(tableParent, -tableParent.getWidth(), 0d, 1000L, true);
                    }
                }
            }
        });
    }

    @Override
    public void actionPerformed(final ActionEvent ae) {
        final Object source = ae.getSource();
        if (menuBar.getNewItem() == source) {
            productsTableModel.add(String.valueOf(System.currentTimeMillis()), String.valueOf(System.currentTimeMillis()));
            return;
        }
        if (menuBar.getExitItem() == source) {
            System.exit(0);
            return;
        }
        if (menuBar.getHelpItem() == source) {
            if (Desktop.isDesktopSupported()) {
                final Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URI(HELP_URI));
                } catch (final IOException e) {
                    logger.error("Could not launch the default browser for your system", e);
                } catch (final URISyntaxException e) {
                    logger.error("Invalid URI " + HELP_URI, e);
                }
            } else {
                logger.inform("Unfortunately your system doesn't support Java SE 6 Desktop API");
            }
            return;
        }
    }

    @Deprecated
    private void createData() {
        for (int i = 0; i < 5; i++) {
            productsTableModel.add(String.valueOf(System.currentTimeMillis()), String.valueOf(System.currentTimeMillis()));
            try {
                Thread.sleep(20);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

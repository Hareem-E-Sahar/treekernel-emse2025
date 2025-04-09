package com.stakface.ocmd.gui;

import com.stakface.ocmd.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.util.*;
import javax.swing.*;

public class FilePaneWrapper extends JComponent implements ItemListener, FocusListener, OCmdListener {

    protected OCmd _ocmd;

    private Map<String, String> _paneTypes;

    private JComboBox _paneTypeChooser;

    private JScrollPane _scrollPane;

    private FilePane _pane;

    public FilePaneWrapper(OCmd ocmd) {
        _ocmd = ocmd;
        _paneTypes = FilePane.getFilePaneTypes(_ocmd);
        _paneTypeChooser = new JComboBox(new Vector<String>(_paneTypes.keySet()));
        _paneTypeChooser.setFocusable(false);
        setLayout(new BorderLayout());
        add(_paneTypeChooser, BorderLayout.NORTH);
        addFocusListener(this);
        _paneTypeChooser.addItemListener(this);
        _paneTypeChooser.setSelectedIndex(1);
    }

    public String getName() {
        return (_pane == null ? "Empty" : _pane.getName());
    }

    public FilePane getFilePane() {
        return _pane;
    }

    public void itemStateChanged(ItemEvent evt) {
        if (evt != null && evt.getStateChange() != ItemEvent.SELECTED) {
            return;
        }
        FilePane newPane = null;
        try {
            String newPaneClass = _paneTypes.get(_paneTypeChooser.getSelectedItem()).toString();
            newPane = (FilePane) Class.forName(newPaneClass).getConstructor(new Class[] { _ocmd.getClass() }).newInstance(new Object[] { _ocmd });
        } catch (NullPointerException npe) {
            _ocmd.logMessage(OCmd.LogType.LOG_PANE, "WARNING: selected pane not found");
            _ocmd.logThrowable(npe);
            return;
        } catch (Exception e) {
            _ocmd.logMessage(OCmd.LogType.LOG_PANE, "WARNING: could not load selected pane");
            _ocmd.logThrowable(e);
            return;
        }
        if (_scrollPane != null) {
            remove(_scrollPane);
            _ocmd.removeOCmdListener(_pane);
        }
        _ocmd.addOCmdListener(newPane);
        FilePane oldPane = _pane;
        _pane = newPane;
        _scrollPane = new JScrollPane(_pane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JComponent columnHeader = _pane.getColumnHeader();
        if (columnHeader != null) {
            _scrollPane.setColumnHeaderView(columnHeader);
        }
        JComponent rowHeader = _pane.getRowHeader();
        if (rowHeader != null) {
            _scrollPane.setRowHeaderView(rowHeader);
        }
        add(_scrollPane, BorderLayout.CENTER);
        _ocmd.fireOCmdEvent(this, OCmdEvent.EventType.ACTIVE_FILEPANE, oldPane, _pane);
    }

    public void focusGained(FocusEvent evt) {
        _pane.requestFocus();
    }

    public void focusLost(FocusEvent evt) {
    }

    public void ocmdEvent(OCmdEvent evt) {
    }
}

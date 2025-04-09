package raptor.swt.chess.movelist;

import raptor.util.RaptorLogger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.swt.RaptorTable;
import raptor.swt.RaptorTable.RaptorTableAdapter;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardMoveList;

/**
 * A move list that just shows a simple 2 column table. The table is traversable
 * by mouse and by keystrokes.
 */
public class TableMoveList implements ChessBoardMoveList {

    private static final RaptorLogger LOG = RaptorLogger.getLog(TableMoveList.class);

    protected ChessBoardController controller;

    protected boolean ignoreSelection;

    protected RaptorTable movesTable;

    protected long lastWheel;

    Listener mouseWheelListener = new Listener() {

        public void handleEvent(Event event) {
            switch(event.type) {
                case SWT.MouseWheel:
                    if (System.currentTimeMillis() - lastWheel > 100 && Raptor.getInstance().getPreferences().getBoolean(PreferenceKeys.BOARD_TRAVERSE_WITH_MOUSE_WHEEL)) {
                        getChessBoardController().userMouseWheeled(event.count);
                        lastWheel = System.currentTimeMillis();
                    }
                    break;
            }
        }
    };

    /**
	 * {@inheritDoc}
	 */
    public void clear() {
        movesTable.clearTable();
    }

    /**
	 * {@inheritDoc}
	 */
    public Composite create(Composite parent) {
        if (movesTable == null) {
            createControls(parent);
        }
        return movesTable;
    }

    /**
	 * {@inheritDoc}
	 */
    public void forceRedraw() {
        updateToGame();
        movesTable.setCursorEnd();
    }

    /**
	 * {@inheritDoc}
	 */
    public ChessBoardController getChessBoardController() {
        return controller;
    }

    /**
	 * {@inheritDoc}
	 */
    public Composite getControl() {
        return movesTable;
    }

    /**
	 * {@inheritDoc}
	 */
    public void select(int halfMoveIndex) {
        if (movesTable.isVisible()) {
            if (movesTable.getTable().getItemCount() == 0) {
                return;
            }
            ignoreSelection = true;
            if (halfMoveIndex > 0) {
                halfMoveIndex -= 1;
            }
            if (movesTable.getTable().getItemCount() == 0) {
                return;
            } else if (halfMoveIndex < 0) {
                halfMoveIndex = 0;
            } else if (halfMoveIndex / 2 > movesTable.getTable().getItemCount()) {
                movesTable.setCursorEnd();
                return;
            }
            int row = halfMoveIndex / 2;
            int column = halfMoveIndex % 2 == 0 ? 0 : 1;
            if (row > movesTable.getTable().getItemCount() - 1) {
                movesTable.setCursorEnd();
                return;
            }
            movesTable.select(row, column);
            ignoreSelection = false;
        }
    }

    /**
	 * {@inheritDoc}
	 */
    public void setController(ChessBoardController controller) {
        this.controller = controller;
    }

    /**
	 * {@inheritDoc}
	 */
    public void updateToGame() {
        if (movesTable.isVisible()) {
            long startTime = System.currentTimeMillis();
            Game game = controller.getGame();
            int moveListSize = game.getMoveList().getSize();
            if (moveListSize == 0) {
                movesTable.clearTable();
            } else {
                int numRows = (moveListSize + 1) / 2;
                String[][] data = new String[numRows][2];
                for (int i = 0; i < data.length; i++) {
                    data[i][0] = i + 1 + ") " + GameUtils.convertSanToUseUnicode(game.getMoveList().get(i * 2).toString(), true);
                    if (i * 2 + 1 >= moveListSize) {
                        data[i][1] = "";
                    } else {
                        data[i][1] = GameUtils.convertSanToUseUnicode(game.getMoveList().get(i * 2 + 1).toString(), true);
                    }
                }
                movesTable.refreshTable(data);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Updated to game in : " + (System.currentTimeMillis() - startTime));
                }
            }
        }
    }

    /**
	 * Appends the move at the specified half move number to the movesTable.
	 */
    protected void appendMove(Game game, int halfMoveNumber) {
        int currentRow = halfMoveNumber / 2;
        if (halfMoveNumber % 2 != 0) {
            movesTable.setText(currentRow, 1, GameUtils.convertSanToUseUnicode(game.getMoveList().get(halfMoveNumber).toString(), false));
        } else {
            int moveNumber = currentRow + 1;
            movesTable.appendRow(new String[] { String.valueOf(moveNumber) + ") " + GameUtils.convertSanToUseUnicode(game.getMoveList().get(halfMoveNumber).toString(), true), "" });
        }
    }

    /**
	 * Creates all of the controls.
	 */
    protected void createControls(Composite parent) {
        movesTable = new RaptorTable(parent, SWT.SINGLE | SWT.V_SCROLL, true, true);
        movesTable.addColumn("White", SWT.LEFT, 60, false, null);
        movesTable.addColumn("Black", SWT.LEFT, 40, false, null);
        movesTable.addRaptorTableListener(new RaptorTableAdapter() {

            @Override
            public void cursorMoved(int row, int column) {
                if (!ignoreSelection) {
                    controller.userSelectedMoveListMove(getCursorHalfMoveIndex());
                }
            }
        });
    }

    /**
	 * Returns the cursors current half move index.
	 * 
	 * @return
	 */
    protected int getCursorHalfMoveIndex() {
        return movesTable.getTable().getSelectionIndex() * 2 + movesTable.getTableCursor().getColumn() + 1;
    }
}

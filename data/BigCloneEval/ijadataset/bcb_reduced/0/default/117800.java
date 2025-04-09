import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.SwingUtilities;

public class GameBoard implements IBoard {

    private int _rows, _cols;

    private ArrayList _players;

    private IPlayer[][] _matrix;

    private PlayerStats[] _stats;

    private int _currPlayer;

    private int _orientation = 0;

    private int _moves;

    private int _passes;

    private Color _boardColor;

    private int _gamesLeft;

    private boolean _hypothetical;

    private Point _lastMove;

    public GameBoard(int rows, int cols) {
        _rows = rows;
        _cols = cols;
        _players = new ArrayList();
        _gamesLeft = 0;
        _hypothetical = false;
    }

    private GameBoard(GameBoard copy) {
        _rows = copy._rows;
        _cols = copy._cols;
        _matrix = new IPlayer[copy._rows][copy._cols];
        for (int r = 0; r < _rows; r++) {
            for (int c = 0; c < _cols; c++) {
                _matrix[r][c] = copy._matrix[r][c];
            }
        }
        _players = new ArrayList(copy._players.size());
        _players.addAll(copy._players);
        _currPlayer = copy._currPlayer;
        _passes = copy._passes;
        _stats = null;
        _hypothetical = true;
    }

    private IPlayer getPlayer(Color c) {
        for (int i = 0; i < _players.size(); i++) {
            IPlayer p = (IPlayer) _players.get(i);
            if (p.getColor().equals(c)) return p;
        }
        return null;
    }

    public void beginGame() {
        _matrix = new IPlayer[_rows][_cols];
        _passes = 0;
        _lastMove = null;
        if (_currPlayer == 0) _orientation = (_orientation + 1) % 2;
        int rMid = _rows / 2;
        int cMid = _cols / 2;
        IPlayer p = (IPlayer) _players.get(_orientation);
        _matrix[rMid][cMid] = p;
        _matrix[rMid - 1][cMid - 1] = p;
        p = (IPlayer) _players.get(_players.size() - 1 - _orientation);
        _matrix[rMid - 1][cMid] = p;
        _matrix[rMid][cMid - 1] = p;
        _moves = 4;
        beginTurn();
    }

    public void beginTurn() {
        int curr = _currPlayer;
        if (_stats != null) _stats[curr].startTurn();
        IPlayer player = ((IPlayer) _players.get(curr));
        player.beginTurn();
        if (!(player instanceof HumanPlayer) && curr == _currPlayer) {
            System.out.println("WARNING: Player '" + player.getName() + "' has not yet submitted a move.");
        }
    }

    public int getColCount() {
        return _cols;
    }

    public int getRowCount() {
        return _rows;
    }

    public boolean placePiece(IPlayer player, int row, int col) {
        IGui.get().redrawBoard();
        if (isGameOver() || player != _players.get(_currPlayer)) {
            return false;
        }
        if (row < 0 || row >= _rows || col < 0 || col >= _cols) throw new IllegalArgumentException();
        IPlayer p = _matrix[row][col];
        if (p != null) {
            System.out.println("  Denied!  That space is not free!");
            return false;
        }
        _matrix[row][col] = player;
        int flips = 0;
        if (walkVector(row, col, -1, 0, player, false) > 0) flips += walkVector(row, col, -1, 0, player, true);
        if (walkVector(row, col, -1, 1, player, false) > 0) flips += walkVector(row, col, -1, 1, player, true);
        if (walkVector(row, col, 0, 1, player, false) > 0) flips += walkVector(row, col, 0, 1, player, true);
        if (walkVector(row, col, 1, 1, player, false) > 0) flips += walkVector(row, col, 1, 1, player, true);
        if (walkVector(row, col, 1, 0, player, false) > 0) flips += walkVector(row, col, 1, 0, player, true);
        if (walkVector(row, col, 1, -1, player, false) > 0) flips += walkVector(row, col, 1, -1, player, true);
        if (walkVector(row, col, 0, -1, player, false) > 0) flips += walkVector(row, col, 0, -1, player, true);
        if (walkVector(row, col, -1, -1, player, false) > 0) flips += walkVector(row, col, -1, -1, player, true);
        if (flips == 0) {
            _matrix[row][col] = null;
            System.out.println("  Denied!  That move is not valid!");
            return false;
        }
        _moves++;
        _passes = 0;
        _lastMove = new Point(col, row);
        if (!_hypothetical) player.endTurn();
        nextTurn();
        return true;
    }

    public IBoard hypotheticallyPlacePiece(Color playerColor, int row, int col) {
        GameBoard b = new GameBoard(this);
        IPlayer player = getPlayer(playerColor);
        b.placePiece(player, row, col);
        return b;
    }

    public boolean pass(IPlayer player) {
        IGui.get().redrawBoard();
        if (isGameOver() || player != _players.get(_currPlayer)) {
            return false;
        }
        if (hasValidMove(player)) {
            return false;
        }
        _passes++;
        _lastMove = null;
        if (!_hypothetical) player.endTurn();
        nextTurn();
        return true;
    }

    public IBoard hypotheticallyPass(Color playerColor) {
        GameBoard b = new GameBoard(this);
        IPlayer player = getPlayer(playerColor);
        b.pass(player);
        return b;
    }

    public boolean isValidMove(Color p, int row, int col) {
        if (row < 0 || row >= _rows || col < 0 || col >= _cols || _matrix[row][col] != null) {
            return false;
        }
        int flips = 0;
        flips += Math.max(0, walkVector(row, col, -1, 0, p));
        flips += Math.max(0, walkVector(row, col, -1, 1, p));
        flips += Math.max(0, walkVector(row, col, 0, 1, p));
        flips += Math.max(0, walkVector(row, col, 1, 1, p));
        flips += Math.max(0, walkVector(row, col, 1, 0, p));
        flips += Math.max(0, walkVector(row, col, 1, -1, p));
        flips += Math.max(0, walkVector(row, col, 0, -1, p));
        flips += Math.max(0, walkVector(row, col, -1, -1, p));
        return flips > 0;
    }

    private int walkVector(int r0, int c0, int dR, int dC, Color player) {
        IPlayer p = getPlayer(player);
        if (p == null) return 0;
        return walkVector(r0, c0, dR, dC, p, false);
    }

    private int walkVector(int r0, int c0, int dR, int dC, IPlayer player, boolean flip) {
        int r = r0 + dR;
        int c = c0 + dC;
        int steps = 0;
        while (r >= 0 && r < _rows && c >= 0 && c < _cols) {
            IPlayer p = _matrix[r][c];
            if (p == null) break;
            if (p == player) return steps;
            if (flip) _matrix[r][c] = player;
            r += dR;
            c += dC;
            steps++;
        }
        return -1;
    }

    private boolean hasValidMove(IPlayer player) {
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColCount(); c++) {
                if (isValidMove(player.getColor(), r, c)) return true;
            }
        }
        return false;
    }

    private void nextTurn() {
        IGui.get().showDropIndicator(null, -1, -1);
        if (isGameOver()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    onGameOver(true);
                }
            });
            return;
        }
        if (_stats != null) _stats[_currPlayer].endTurn();
        _currPlayer = (_currPlayer + 1) % _players.size();
        if (!_hypothetical) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    beginTurn();
                }
            });
        }
    }

    public boolean isGameOver() {
        return _moves >= _rows * _cols || _passes >= _players.size();
    }

    public Color playerAt(int row, int col) {
        if (_matrix == null) return null;
        if (row < 0 || row >= _rows || col < 0 || col >= _cols) throw new IllegalArgumentException();
        IPlayer p = _matrix[row][col];
        if (p == null) return null;
        return p.getColor();
    }

    public int playerScore(Color p) {
        if (_matrix == null) return 0;
        int score = 0;
        for (int r = 0; r < _rows; r++) {
            for (int c = 0; c < _cols; c++) {
                if (_matrix[r][c] != null && _matrix[r][c].getColor() == p) score++;
            }
        }
        return score;
    }

    public void addPlayer(IPlayer p, Color color) {
        _players.add(p);
        p.joinBoard(this, color);
    }

    public Color getBoardColor() {
        if (_boardColor == null) _boardColor = new Color(0x66, 0x99, 0x33);
        if (_boardColor == null) {
            Iterator iter = getPlayers();
            float[] floats = new float[3];
            int hue = 0, lum = 0, sat = 0;
            while (iter.hasNext()) {
                Color pColor = ((IPlayerInfo) iter.next()).getColor();
                Color.RGBtoHSB(pColor.getRed(), pColor.getGreen(), pColor.getBlue(), floats);
                int h = (int) (floats[0] * 255.0);
                int l = (int) (floats[1] * 255.0);
                int s = (int) (floats[2] * 255.0);
                hue ^= h;
                lum = (lum + l) / 2;
                sat = (sat + s) / 2;
            }
            _boardColor = new Color(Color.HSBtoRGB(((float) hue) / 255.0f, ((float) lum) / 255.0f, ((float) sat) / 255.0f));
        }
        return _boardColor;
    }

    public Iterator getPlayers() {
        return new PlayerIterator();
    }

    private class PlayerIterator implements Iterator {

        /**
     * Counts from the index of the current player up to current + num-players.
     * That is, if it is currently player 2's turn out of 4 players, it
     * will count from 2 until 5.  Mod then makes the indeces valid.
     */
        private int _index;

        public PlayerIterator() {
            _index = _currPlayer;
        }

        public boolean hasNext() {
            return _index < _currPlayer + _players.size();
        }

        public Object next() {
            final IPlayer player = (IPlayer) _players.get(_index++ % _players.size());
            return new IPlayerInfo() {

                public String getName() {
                    return player.getName();
                }

                public Color getColor() {
                    return player.getColor();
                }
            };
        }

        public void remove() {
        }
    }

    public void beginTournament(int numGames) {
        _gamesLeft = numGames;
        onGameOver(false);
    }

    private void onGameOver(boolean addToStats) {
        if (!addToStats) {
            _stats = new PlayerStats[_players.size()];
            for (int i = 0; i < _players.size(); i++) {
                _stats[i] = new PlayerStats(((IPlayer) _players.get(i)).getName());
            }
        } else if (_stats != null) {
            int[] squares = new int[_players.size()];
            int most = -1;
            boolean tie = false;
            for (int i = 0; i < _players.size(); i++) {
                squares[i] = playerScore(((IPlayer) _players.get(i)).getColor());
                if (most < squares[i]) most = squares[i]; else if (most == squares[i]) tie = true;
            }
            for (int i = 0; i < _players.size(); i++) {
                _stats[i].addStats(squares[i], most, tie);
            }
        }
        if (_gamesLeft-- > 0) {
            _currPlayer = _gamesLeft % _players.size();
            beginGame();
        }
        IGui.get().redrawBoard();
    }

    public String getCurrentPlayerScore() {
        if (isGameOver()) return null;
        IPlayer plyr = (IPlayer) _players.get(_currPlayer);
        return plyr.getName() + "'s turn (" + playerScore(plyr.getColor()) + ").";
    }

    public Color getCurrentPlayerColor() {
        if (isGameOver()) return null;
        return ((IPlayer) _players.get(_currPlayer)).getColor();
    }

    public String getGameScore() {
        if (!isGameOver()) return null;
        String score = "";
        for (int i = 0; i < _players.size(); i++) {
            IPlayer plyr = (IPlayer) _players.get(i);
            if (i >= 0) score += "   ";
            score += plyr.getName() + ": " + playerScore(plyr.getColor()) + ".";
        }
        return score;
    }

    public String getTournamentScore() {
        if (_stats == null) return null;
        String score = "";
        for (int i = 0; i < _stats.length; i++) {
            if (i >= 0) score += "   ";
            score += _stats[i].toString();
        }
        return score;
    }

    public Point lastMove() {
        return _lastMove;
    }
}

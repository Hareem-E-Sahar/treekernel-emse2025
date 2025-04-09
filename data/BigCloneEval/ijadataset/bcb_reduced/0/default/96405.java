import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class JunesBoard extends Board {

    private int halfwayDistance;

    private Image playerImage;

    private JLabel centerImage;

    private Point playerImagePosition;

    private final Class[] squareConstructionArgs = { Dimension.class, BoardSquare.SquareIdentifier.class };

    private GUI gui;

    private Dimension squareSize;

    private BoardSquare.SquareIdentifier currentSquare;

    Vector<PlayerMovementMarker> playerMovementMarkers = new Vector<PlayerMovementMarker>(0, 1);

    private final long ANIMATIONTIMEOUT = 100;

    private boolean drawingMovementAnimation = false;

    public JunesBoard(GUI gui) {
        this.gui = gui;
        squareSize = getSquareSize();
        setPreferredSize(new Dimension(squareSize.width * 5, squareSize.height * 5));
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setSize(getPreferredSize());
        playerImage = createPlayerMarker();
        centerImage = createCenterImage();
        createSquares();
        halfwayDistance = squares.length / 2;
        for (int i = 0; i < squares.length; i++) {
            add(squares[i]);
        }
        add(centerImage);
        playerImagePosition = new Point();
        setPlayerImagePosition(BoardSquare.SquareIdentifier.GUILD_HOUSE);
    }

    private Dimension getSquareSize() {
        if (squareSize != null) {
            return squareSize;
        }
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        System.out.println("Maximum window bounds" + r);
        if (r.height < 900) {
            return new Dimension(100, 100);
        }
        return new Dimension(150, 150);
    }

    public Vector<BoardSquare.SquareIdentifier> getShortestRoute(BoardSquare.SquareIdentifier from, BoardSquare.SquareIdentifier to) {
        int distance = distanceBetween(from, to);
        Vector<BoardSquare.SquareIdentifier> route = new Vector<BoardSquare.SquareIdentifier>(distance, 1);
        MovingDirection direction = logicalMovementDirection(from, to);
        BoardSquare.SquareIdentifier nextSquare = from;
        route.add(from);
        while (nextSquare != to) {
            nextSquare = nextSquareFrom(nextSquare, direction);
            route.add(nextSquare);
        }
        return route;
    }

    private Vector<MovementTransition> routeAsTransitions(Vector<BoardSquare.SquareIdentifier> route) {
        if (route.size() < 2) {
            return null;
        }
        MovingDirection mDirection = logicalMovementDirection(route.elementAt(0), route.elementAt(1));
        Vector<MovementTransition> transitions = new Vector<MovementTransition>(route.size() - 1, 1);
        for (int i = 0; i < route.size() - 1; i++) {
            BoardSquare.SquareIdentifier from = route.elementAt(i);
            BoardSquare.SquareIdentifier to = route.elementAt(i + 1);
            AbsoluteDirection aDirection = absoluteMovementDirection(from, mDirection);
            transitions.add(new MovementTransition(from, to, aDirection));
        }
        return transitions;
    }

    public int distanceBetween(BoardSquare.SquareIdentifier from, BoardSquare.SquareIdentifier to) {
        return distanceBetween(from.ordinal(), to.ordinal());
    }

    private int distanceBetween(int from, int to) {
        int distance = 0;
        if (Math.abs(from - to) <= halfwayDistance) {
            distance = Math.abs(from - to);
        } else {
            distance = squares.length - Math.abs(from - to);
        }
        return distance;
    }

    private MovingDirection logicalMovementDirection(BoardSquare.SquareIdentifier from, BoardSquare.SquareIdentifier to) {
        return logicalMovementDirection(from.ordinal(), to.ordinal());
    }

    private MovingDirection logicalMovementDirection(int from, int to) {
        if ((from < to) && (to - from) <= halfwayDistance) {
            return MovingDirection.CLOCKWISE;
        }
        if ((from > to) && (from - to) > halfwayDistance) {
            return MovingDirection.CLOCKWISE;
        }
        return MovingDirection.COUNTERCLOCKWISE;
    }

    private AbsoluteDirection absoluteMovementDirection(BoardSquare.SquareIdentifier from, MovingDirection direction) {
        return absoluteMovementDirection(from.ordinal(), direction);
    }

    private AbsoluteDirection absoluteMovementDirection(int from, MovingDirection direction) {
        AbsoluteDirection result = AbsoluteDirection.right;
        if (direction == MovingDirection.CLOCKWISE) {
            if (from > 3) result = AbsoluteDirection.down;
            if (from > 7) result = AbsoluteDirection.left;
            if (from > 11) result = AbsoluteDirection.up;
        } else {
            if (from == 0) return AbsoluteDirection.down;
            result = AbsoluteDirection.left;
            if (from > 4) result = AbsoluteDirection.up;
            if (from > 8) result = AbsoluteDirection.right;
            if (from > 12) result = AbsoluteDirection.down;
        }
        return result;
    }

    private BoardSquare.SquareIdentifier nextSquareFrom(BoardSquare.SquareIdentifier from, MovingDirection direction) {
        int squareIndex = from.ordinal();
        squareIndex += squares.length;
        if (direction == MovingDirection.CLOCKWISE) {
            squareIndex++;
        } else {
            squareIndex--;
        }
        squareIndex = squareIndex % squares.length;
        return squares[squareIndex].getIdentifier();
    }

    public void movePlayerMarker(BoardSquare.SquareIdentifier toSquare) {
        if (toSquare != currentSquare) {
            clearPlayerMovementAnimation();
            preparePlayerMovementAnimationSequence(getShortestRoute(currentSquare, toSquare));
            drawPlayerMovementAnimationSequence();
        }
        setPlayerImagePosition(toSquare);
        repaint();
    }

    private void clearPlayerMovementAnimation() {
        playerMovementMarkers.clear();
        repaint();
    }

    private void preparePlayerMovementAnimationSequence(Vector<BoardSquare.SquareIdentifier> route) {
        if (route.isEmpty()) {
            return;
        }
        Vector<MovementTransition> transitions = routeAsTransitions(route);
        Iterator<MovementTransition> transitionIterator = transitions.iterator();
        while (transitionIterator.hasNext()) {
            mapPlayerMovementMarkers(transitionIterator.next());
        }
    }

    private void mapPlayerMovementMarkers(MovementTransition transition) {
        AbsoluteDirection direction = transition.getDirection();
        BoardSquare from = getBoardSquare(transition.getFrom());
        BoardSquare to = getBoardSquare(transition.getTo());
        Point centerPoint = new Point(squareSize.width / 2, squareSize.height / 2);
        Point centerPointFrom = new Point(from.getX() + centerPoint.x, from.getY() + centerPoint.y);
        Point centerPointTo = new Point(to.getX() + centerPoint.x, to.getY() + centerPoint.y);
        PlayerMovementMarker marker1 = new PlayerMovementMarker(centerPointFrom.x, centerPointFrom.y);
        ;
        PlayerMovementMarker marker2;
        PlayerMovementMarker marker3;
        if (direction == AbsoluteDirection.left || direction == AbsoluteDirection.right) {
            int pixelsBetweenStepsX = squareSize.width / 3;
            if (direction == AbsoluteDirection.left) {
                pixelsBetweenStepsX = -pixelsBetweenStepsX;
            }
            marker2 = new PlayerMovementMarker(centerPointFrom.x + pixelsBetweenStepsX, centerPointFrom.y);
            marker3 = new PlayerMovementMarker(centerPointTo.x - pixelsBetweenStepsX, centerPointTo.y);
        } else {
            int pixelsBetweenStepsY = squareSize.height / 3;
            if (direction == AbsoluteDirection.up) {
                pixelsBetweenStepsY = -pixelsBetweenStepsY;
            }
            marker2 = new PlayerMovementMarker(centerPointFrom.x, centerPointFrom.y + pixelsBetweenStepsY);
            marker3 = new PlayerMovementMarker(centerPointTo.x, centerPointTo.y - pixelsBetweenStepsY);
        }
        playerMovementMarkers.add(marker1);
        playerMovementMarkers.add(marker2);
        playerMovementMarkers.add(marker3);
    }

    private void drawPlayerMovementAnimationSequence() {
        drawingMovementAnimation = true;
        paintImmediately(0, 0, getWidth(), getHeight());
        Iterator<PlayerMovementMarker> markers = playerMovementMarkers.iterator();
        while (markers.hasNext()) {
            PlayerMovementMarker marker = markers.next();
            marker.setDraw(true);
            paintImmediately(marker.getX(), marker.getY(), marker.getImage().getWidth(), marker.getImage().getHeight());
            try {
                Thread.sleep(ANIMATIONTIMEOUT);
            } catch (InterruptedException e) {
            }
        }
        drawingMovementAnimation = false;
    }

    private void setPlayerImagePosition(BoardSquare.SquareIdentifier boardIndex) {
        currentSquare = boardIndex;
        BoardSquare s = getBoardSquare(boardIndex);
        playerImagePosition.x = s.getX() + (s.getWidth() / 2) - (playerImage.getWidth(this) / 2);
        playerImagePosition.y = s.getY() + (s.getHeight() / 2) - (playerImage.getHeight(this) / 2);
    }

    private void createSquares() {
        int x = squareSize.width;
        int y = squareSize.height;
        squares = new BoardSquare[16];
        squares[0] = createSquare(0, 0, BoardSquare.SquareIdentifier.PRIVATE_HOUSING);
        squares[1] = createSquare(x, 0, BoardSquare.SquareIdentifier.DORMITORY);
        squares[2] = createSquare(x * 2, 0, BoardSquare.SquareIdentifier.GUILD_HOUSE);
        squares[3] = createSquare(x * 3, 0, BoardSquare.SquareIdentifier.PAWN_SHOP);
        squares[4] = createSquare(x * 4, 0, BoardSquare.SquareIdentifier.BULIMIA);
        squares[5] = createSquare(x * 4, y, BoardSquare.SquareIdentifier.WOKKIPAJA);
        squares[6] = createSquare(x * 4, y * 2, BoardSquare.SquareIdentifier.STUDY_MATERIAL_SHOP);
        squares[7] = createSquare(x * 4, y * 3, BoardSquare.SquareIdentifier.BAR);
        squares[8] = createSquare(x * 4, y * 4, BoardSquare.SquareIdentifier.HOME_ELECTRONICS);
        squares[9] = createSquare(x * 3, y * 4, BoardSquare.SquareIdentifier.LECTUREHALL_TECHNICAL);
        squares[10] = createSquare(x * 2, y * 4, BoardSquare.SquareIdentifier.LECTUREHALL_HUMANIST);
        squares[11] = createSquare(x, y * 4, BoardSquare.SquareIdentifier.EMPLOYMENT_AGENCY);
        squares[12] = createSquare(0, y * 4, BoardSquare.SquareIdentifier.FACTORY);
        squares[13] = createSquare(0, y * 3, BoardSquare.SquareIdentifier.BANK);
        squares[14] = createSquare(0, y * 2, BoardSquare.SquareIdentifier.BIG_SHOP);
        squares[15] = createSquare(0, y, BoardSquare.SquareIdentifier.ALCOHOL_SHOP);
    }

    private Image createPlayerMarker() {
        BufferedImage unscaledImage = JunesGUI.getUtils().getBufferedImage("playerimage-01.png");
        if (unscaledImage == null) {
            return null;
        }
        int height = (int) (squareSize.height * 0.8);
        int width = (int) (((float) unscaledImage.getWidth() / (float) unscaledImage.getHeight()) * height);
        Image resultImage = unscaledImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return resultImage;
    }

    private JLabel createCenterImage() {
        BufferedImage unscaledImage = JunesGUI.getUtils().getBufferedImage("playerimage-01.png");
        if (unscaledImage == null) {
            return null;
        }
        Dimension availableSpace = new Dimension(squareSize.width * 3, squareSize.height * 3);
        int height = availableSpace.height;
        int width = (int) (((float) unscaledImage.getWidth() / (float) unscaledImage.getHeight()) * height);
        Image resultImage = unscaledImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(resultImage));
        int paddingy = (squareSize.height * 3 - height) / 2;
        int paddingx = (squareSize.width * 3 - width) / 2;
        label.setLocation(squareSize.width + paddingx, squareSize.height + paddingy);
        label.setSize(width, height);
        return label;
    }

    private BoardSquare createSquare(int x, int y, BoardSquare.SquareIdentifier identifier) {
        BoardSquare s;
        try {
            Constructor c = identifier.getSpecializedClass().getConstructor(squareConstructionArgs);
            s = (BoardSquare) c.newInstance(squareSize, identifier);
            s.createActionDialog();
            s.useBackgroundColor(JunesLogic.options.getUseBackgroundColor());
        } catch (Exception e) {
            s = new DefaultBoardSquare(squareSize, identifier);
        }
        s.setLocation(x, y);
        s.setSize(squareSize);
        gui.loadingProgress(1);
        return s;
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (acceptInput) {
            if (drawingMovementAnimation) {
                drawPlayerMovementMarkers(g);
            } else {
                g.drawImage(playerImage, playerImagePosition.x, playerImagePosition.y, this);
            }
        }
    }

    private void drawPlayerMovementMarkers(Graphics g) {
        Iterator<PlayerMovementMarker> markers = playerMovementMarkers.iterator();
        while (markers.hasNext()) {
            PlayerMovementMarker marker = markers.next();
            if (marker.getDraw()) {
                g.drawImage(marker.getImage(), marker.getX(), marker.getY(), this);
            }
        }
    }
}

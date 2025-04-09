package quizcards;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

public class Stack extends JPanel implements ActionListener, ItemListListener, UndoableEditListener {

    public static final int BROWSE = 0;

    public static final int QUIZ_LEFT = 1;

    public static final int QUIZ_LEFT_SOUND = 2;

    public static final int QUIZ_RIGHT = 3;

    public static final int QUIZ_RIGHT_SOUND = 4;

    public boolean compressed;

    public static JMenuItem done = new JMenuItem("Done");

    public String lFontName = "Default";

    public int lFontSize = 10;

    public String lName;

    public boolean lSoundFlag;

    public String rFontName = "Default";

    public int rFontSize = 10;

    public String rName;

    public boolean rSoundFlag;

    public final Sets sets = new Sets();

    public String title;

    private boolean add_edits = false;

    private JPanel button_panel = new JPanel();

    private JPanel card_panel = new JPanel();

    private Vector cards = new Vector();

    private JMenuItem create_card = new JMenuItem("New Card...");

    private int current_card;

    private int current_index;

    private boolean default_sets[];

    private JMenuItem defaults = new JMenuItem("Set New Card Defaults...");

    private JMenuItem delete_card = new JMenuItem("Delete Card");

    private boolean dirty = false;

    private JButton drop = new JButton("Drop");

    private JMenu edit_menu = new JMenu("Edit");

    private boolean edit_mode = false;

    private String filename;

    private JMenuItem find = new JMenuItem("Find...");

    private JMenuItem find_next = new JMenuItem("Find Next");

    private String find_text;

    private JButton first;

    private JMenuItem import_text_file = new JMenuItem("Import Text File...");

    private JButton keep = new JButton("Next");

    private JButton last;

    private JCheckBox learned = new JCheckBox("Learned");

    private JButton location = new JButton();

    private char left_type = Side.NUM_CARD_TYPES;

    private int mode;

    private JMenuItem move_bottom = new JMenuItem("Move Card to Bottom");

    private JMenuItem move_down = new JMenuItem("Move Card Down");

    private JMenuItem move_top = new JMenuItem("Move Card to Top");

    private JMenuItem move_up = new JMenuItem("Move Card Up");

    private JPanel navigator = new JPanel();

    private JButton next;

    private JButton prev;

    private JMenuItem properties = new JMenuItem("Card Properties...");

    private JPanel quiz_panel = new JPanel();

    private JMenuItem redo = new JMenuItem();

    private char right_type = Side.NUM_CARD_TYPES;

    private JMenu search_menu = new JMenu("Search");

    private Vector set_members = new Vector();

    private JButton show = new JButton("Show Answer");

    private JMenuItem undo = new JMenuItem();

    private UndoManager undo_manager = new UndoManager();

    public Stack() {
        create_card.setMnemonic('n');
        defaults.setMnemonic('s');
        delete_card.setMnemonic('d');
        done.setMnemonic('d');
        drop.setMnemonic('d');
        edit_menu.setMnemonic('e');
        find.setMnemonic('f');
        find_next.setMnemonic('n');
        import_text_file.setMnemonic('i');
        keep.setMnemonic('n');
        learned.setMnemonic('l');
        location.setMnemonic('c');
        move_bottom.setMnemonic('e');
        move_down.setMnemonic('v');
        move_top.setMnemonic('m');
        move_up.setMnemonic('o');
        properties.setMnemonic('p');
        redo.setMnemonic('r');
        search_menu.setMnemonic('e');
        show.setMnemonic('s');
        undo.setMnemonic('u');
        create_card.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
        done.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, java.awt.Event.CTRL_MASK));
        find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, java.awt.Event.CTRL_MASK));
        find_next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, java.awt.Event.CTRL_MASK));
        properties.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.Event.CTRL_MASK));
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, java.awt.Event.CTRL_MASK));
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.Event.CTRL_MASK));
        updateEditMenu();
        edit_menu.add(undo);
        edit_menu.add(redo);
        edit_menu.addSeparator();
        edit_menu.add(create_card);
        edit_menu.add(delete_card);
        edit_menu.addSeparator();
        edit_menu.add(move_top);
        edit_menu.add(move_up);
        edit_menu.add(move_down);
        edit_menu.add(move_bottom);
        edit_menu.addSeparator();
        edit_menu.add(properties);
        edit_menu.add(defaults);
        search_menu.add(find);
        search_menu.add(find_next);
        first = new JButton(new ImageIcon(QuizCards.t.getImage(ClassLoader.getSystemResource("first.gif"))));
        last = new JButton(new ImageIcon(QuizCards.t.getImage(ClassLoader.getSystemResource("last.gif"))));
        next = new JButton(new ImageIcon(QuizCards.t.getImage(ClassLoader.getSystemResource("next.gif"))));
        prev = new JButton(new ImageIcon(QuizCards.t.getImage(ClassLoader.getSystemResource("previous.gif"))));
        navigator.setLayout(new GridLayout(1, 5));
        navigator.add(first);
        navigator.add(prev);
        navigator.add(next);
        navigator.add(last);
        navigator.add(location);
        location.setFont(QuizCards.small_font);
        location.setMargin(new Insets(4, 0, 0, 0));
        quiz_panel.setLayout(new GridLayout(1, 3));
        quiz_panel.add(show);
        quiz_panel.add(keep);
        quiz_panel.add(drop);
        card_panel.setLayout(new GridLayout(1, 2, 5, 5));
        setLayout(new BorderLayout());
        add(button_panel, "South");
        add(card_panel, "Center");
        create_card.addActionListener(this);
        defaults.addActionListener(this);
        delete_card.addActionListener(this);
        drop.addActionListener(this);
        find.addActionListener(this);
        find_next.addActionListener(this);
        first.addActionListener(this);
        import_text_file.addActionListener(this);
        keep.addActionListener(this);
        last.addActionListener(this);
        learned.addActionListener(this);
        location.addActionListener(this);
        move_bottom.addActionListener(this);
        move_down.addActionListener(this);
        move_top.addActionListener(this);
        move_up.addActionListener(this);
        next.addActionListener(this);
        prev.addActionListener(this);
        properties.addActionListener(this);
        redo.addActionListener(this);
        show.addActionListener(this);
        undo.addActionListener(this);
        Side.setUndoableEditListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == create_card) {
            if (left_type == Side.NUM_CARD_TYPES) {
                CardDialog d = new CardDialog(-1, Side.TEXT, Side.TEXT, sets, default_sets);
                d.show();
                if (d.isOKPressed()) {
                    left_type = d.getLeftType();
                    right_type = d.getRightType();
                    default_sets = d.member;
                } else {
                    updatePosition();
                    return;
                }
            }
            Card card = new Card(left_type, right_type);
            insertCard(card, current_card + 1, default_sets);
            undo_manager.addEdit(new CreateCard(card, current_card, default_sets));
            updateEditMenu();
        } else if (source == defaults) {
            CardDialog d;
            if (left_type == Side.NUM_CARD_TYPES) d = new CardDialog(-1, Side.TEXT, Side.TEXT, sets, default_sets); else d = new CardDialog(-1, left_type, right_type, sets, default_sets);
            d.show();
            if (d.isOKPressed()) {
                left_type = d.getLeftType();
                right_type = d.getRightType();
                default_sets = d.member;
            }
        } else if (source == delete_card) {
            int cardNum = current_card;
            Card c = (Card) cards.elementAt(cardNum);
            boolean member[] = new boolean[sets.size()];
            deleteCard(current_card, member);
            undo_manager.addEdit(new DeleteCard(c, cardNum, member));
            updateEditMenu();
        } else if (source == drop) {
            int numLeft = goRandom(false);
            if (numLeft == 0) done.doClick(); else if (numLeft == 1) keep.setEnabled(false);
        } else if (source == find) find(); else if (source == find_next) if (find_text != null) find_next(); else find(); else if (source == first) goFirst(); else if (source == import_text_file) importFile(); else if (source == keep) goRandom(true); else if (source == last) goLast(); else if (source == learned) {
            ((Card) cards.elementAt(current_card)).learned = learned.isSelected();
            dirty = true;
        } else if (source == location) {
            GoToDialog d = new GoToDialog(current_index + 1, numCardsViewable());
            d.show();
            if (d.isOKPressed()) {
                current_index = d.getNumber() - 1;
                closeCard();
                current_card = ((Integer) set_members.elementAt(current_index)).intValue();
                openCard();
            }
        } else if (source == move_bottom) {
            undo_manager.addEdit(new MoveCard(current_card, cards.size() - 1));
            updateEditMenu();
            moveCard(current_card, cards.size() - 1);
        } else if (source == move_down) {
            undo_manager.addEdit(new MoveCard(current_card, current_card + 1));
            updateEditMenu();
            moveCard(current_card, current_card + 1);
        } else if (source == move_top) {
            undo_manager.addEdit(new MoveCard(current_card, 0));
            updateEditMenu();
            moveCard(current_card, 0);
        } else if (source == move_up) {
            undo_manager.addEdit(new MoveCard(current_card, current_card - 1));
            updateEditMenu();
            moveCard(current_card, current_card - 1);
        } else if (source == next) goNext(); else if (source == prev) goPrev(); else if (source == properties) {
            Card card = (Card) cards.elementAt(current_card);
            CardDialog d = new CardDialog(current_card, card.left_side.getType(), card.right_side.getType(), sets);
            d.show();
            if (d.isOKPressed()) {
                Side left_side = card.left_side, right_side = card.right_side;
                if (left_side.getType() != d.getLeftType()) left_side = Side.newSide(d.getLeftType(), true);
                if (right_side.getType() != d.getRightType()) right_side = Side.newSide(d.getRightType(), false);
                undo_manager.addEdit(new CardProperties(current_card, sets, card.left_side, card.right_side, left_side, right_side));
                updateEditMenu();
                setCardSides(current_card, left_side, right_side);
                sets.setCardSets(current_card, d.member);
                validate();
                repaint();
            }
        } else if (source == redo) {
            undo_manager.redo();
            updateEditMenu();
        } else if (source == show) {
            Card card = (Card) cards.elementAt(current_card);
            card.left_side.setVisible(true);
            card.right_side.setVisible(true);
            keep.requestFocus();
        } else if (source == undo) {
            undo_manager.undo();
            updateEditMenu();
        }
    }

    public void close() {
        if (current_card != -1) closeCard();
        undo_manager.discardAllEdits();
        if (dirty) write();
        JMenuBar menuBar = QuizCards.w.menuBar;
        if (edit_mode) menuBar.remove(1);
        if (mode == BROWSE) menuBar.remove(1);
        if (menuBar.getMenuCount() > 2) {
            menuBar.remove(3);
            menuBar.remove(2);
        }
    }

    private void closeCard() {
        if (edit_mode && current_card != -1) dirty |= ((Card) cards.elementAt(current_card)).set();
    }

    public void createItem(int position, String name) {
        sets.add(position, name);
    }

    public void deleteCard(int position, boolean member[]) {
        closeCard();
        current_card = position;
        if (cards.size() == 1) hideLastCard();
        cards.removeElementAt(position);
        set_members.removeElementAt(set_members.size() - 1);
        sets.deleteCard(position, member);
        if (cards.size() == 0) current_card = -1; else if (current_card > 0) current_card--;
        current_index = current_card;
        dirty = true;
        if (current_index != -1) openCard(); else updatePosition();
    }

    public void deleteCards(int position, int num_cards, boolean before, boolean member[]) {
        closeCard();
        if (position == -1 || cards.size() == num_cards) hideLastCard();
        if (position == -1) {
            current_card = -1;
            cards.removeAllElements();
            set_members.removeAllElements();
            for (int i = 0; i < num_cards; i++) sets.deleteCard(0, member);
        } else {
            current_card = position;
            if (!before) ++position;
            for (int i = 0; i < num_cards; i++) {
                cards.removeElementAt(position);
                set_members.removeElementAt(set_members.size() - 1);
            }
            for (int i = 0; i < num_cards; i++) sets.deleteCard(position, member);
        }
        current_index = current_card;
        dirty = true;
        if (current_index != -1) openCard(); else updatePosition();
    }

    public void deleteItem(int position) {
        sets.remove(position);
    }

    private void find() {
        String text = (String) JOptionPane.showInputDialog(QuizCards.w, "Search for (after this card):", "Find", JOptionPane.QUESTION_MESSAGE, null, null, find_text);
        if (text != null && text.length() > 0) {
            find_text = text.toLowerCase();
            find_next();
        }
    }

    private void find_next() {
        for (int i = current_index + 1; i < set_members.size(); i++) {
            int c = ((Integer) set_members.elementAt(i)).intValue();
            if (((Card) cards.elementAt(c)).find(find_text)) {
                closeCard();
                current_index = i;
                current_card = c;
                openCard();
                return;
            }
        }
        getToolkit().beep();
    }

    public void goFirst() {
        closeCard();
        current_index = 0;
        current_card = ((Integer) set_members.elementAt(current_index)).intValue();
        openCard();
    }

    public void goLast() {
        closeCard();
        current_index = set_members.size() - 1;
        current_card = ((Integer) set_members.elementAt(current_index)).intValue();
        openCard();
    }

    public void goNext() {
        closeCard();
        ++current_index;
        current_card = ((Integer) set_members.elementAt(current_index)).intValue();
        openCard();
    }

    public void goPrev() {
        closeCard();
        --current_index;
        current_card = ((Integer) set_members.elementAt(current_index)).intValue();
        openCard();
    }

    public int goRandom(boolean keep) {
        if (keep && set_members.size() <= 1) return set_members.size();
        closeCard();
        if (keep) {
            int i = current_index;
            while (i == current_index) current_index = (int) (Math.random() * set_members.size());
        } else {
            for (int i = 0; i < set_members.size(); i++) if (((Integer) set_members.elementAt(i)).intValue() == current_card) {
                set_members.removeElementAt(i);
                break;
            }
            if (set_members.size() == 0) return 0; else if (set_members.size() == 1) current_index = 0; else current_index = (int) (Math.random() * set_members.size());
        }
        current_card = ((Integer) set_members.elementAt(current_index)).intValue();
        openCard();
        return set_members.size();
    }

    public void goToCard(int position) {
        if (current_card != position) {
            closeCard();
            current_card = position;
            current_index = current_card;
            openCard();
        }
    }

    private void hideLastCard() {
        Card card = (Card) cards.elementAt(0);
        card.left_side.setVisible(false);
        card.right_side.setVisible(false);
        delete_card.setEnabled(false);
        properties.setEnabled(false);
    }

    public void importFile() {
        ImportDialog id = new ImportDialog(cards.size());
        if (id.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String delimeter = id.getDelimeter();
            String quote_char = id.getQuoteChar();
            boolean before = id.isBeforeSelected();
            try {
                char d = delimeter.equals("Tab") ? 9 : delimeter.charAt(0);
                BufferedReader r = new BufferedReader(new FileReader(id.getSelectedFile().getPath()));
                String s;
                Vector v = new Vector();
                while ((s = r.readLine()) != null) {
                    String left = null;
                    String right = null;
                    ;
                    if (quote_char.equals("None")) {
                        int i = s.indexOf(d);
                        if (i != -1) {
                            left = s.substring(0, i);
                            right = s.substring(i + 1);
                        } else left = s;
                    } else {
                        int i = s.indexOf(quote_char);
                        if (i != -1) {
                            int j = s.indexOf(quote_char, i + 1);
                            if (j != -1) {
                                left = s.substring(i + 1, j);
                                i = s.indexOf(quote_char, j + 1);
                                if (i != -1) {
                                    j = s.indexOf(quote_char, i + 1);
                                    if (j != -1) right = s.substring(i + 1, j);
                                }
                            }
                        }
                    }
                    Card card = new Card(Side.TEXT, Side.TEXT);
                    ((TextSide) card.left_side).setText(left);
                    ((TextSide) card.right_side).setText(right);
                    v.addElement(card);
                }
                Card[] c = new Card[v.size()];
                for (int i = 0; i < v.size(); i++) c[i] = (Card) v.elementAt(i);
                undo_manager.addEdit(new Import(c, current_card, before, default_sets));
                insertCards(c, current_card, before, default_sets);
                updateEditMenu();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public void insertCard(Card card, int position, boolean member[]) {
        closeCard();
        cards.insertElementAt(card, position);
        set_members.addElement(new Integer(set_members.size()));
        sets.insertCard(position, member);
        dirty = true;
        current_card = position;
        current_index = current_card;
        openCard();
    }

    public void insertCards(Card[] c, int position, boolean before, boolean member[]) {
        closeCard();
        if (position == -1) position = 0; else if (!before) ++position;
        for (int i = 0; i < c.length; i++) {
            cards.insertElementAt(c[i], position);
            set_members.addElement(new Integer(set_members.size()));
            sets.insertCard(position, member);
            ++position;
        }
        dirty = true;
        current_card = position - 1;
        current_index = current_card;
        openCard();
    }

    public boolean isFirstCard() {
        return current_index == 0;
    }

    public boolean isLastCard() {
        return current_index == set_members.size() - 1;
    }

    public void moveCard(int from, int to) {
        if (from == to) return;
        closeCard();
        Object o = cards.elementAt(from);
        cards.removeElementAt(from);
        cards.insertElementAt(o, to);
        sets.moveCard(from, to);
        dirty = true;
        current_card = to;
        current_index = current_card;
        openCard();
    }

    public int numCards() {
        return cards.size();
    }

    public int numCardsViewable() {
        return set_members.size();
    }

    public void open(int mode, boolean edit_mode) {
        this.mode = mode;
        this.edit_mode = edit_mode;
        left_type = Side.NUM_CARD_TYPES;
        default_sets = new boolean[sets.size()];
        JMenu fileMenu = QuizCards.w.fileMenu;
        fileMenu.removeAll();
        if (edit_mode) {
            fileMenu.add(import_text_file);
            fileMenu.addSeparator();
        }
        fileMenu.add(done);
        JMenuBar menuBar = QuizCards.w.menuBar;
        if (mode == BROWSE) menuBar.add(search_menu, 1);
        if (edit_mode) menuBar.add(edit_menu, 1);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(learned);
        if (edit_mode) setCurrentSet(-1, true, true);
        if (mode != BROWSE) {
            current_index = (int) (Math.random() * set_members.size());
            button_panel.removeAll();
            button_panel.setLayout(new BorderLayout());
            button_panel.add(quiz_panel);
            keep.setEnabled(numCardsViewable() > 1);
        } else {
            current_index = 0;
            button_panel.removeAll();
            button_panel.setLayout(new GridLayout(1, 5));
            button_panel.add(first);
            button_panel.add(prev);
            button_panel.add(next);
            button_panel.add(last);
            button_panel.add(location);
        }
        updateFonts();
        if (edit_mode && cards.size() == 0) {
            card_panel.setVisible(false);
            QuizCards.w.repaint();
            current_card = -1;
            current_index = -1;
            actionPerformed(new ActionEvent(create_card, 0, ""));
        } else current_card = ((Integer) set_members.elementAt(current_index)).intValue();
    }

    public void openCard() {
        if (current_card == -1) return;
        Card card = (Card) cards.elementAt(current_card);
        boolean soundPlayed = true;
        learned.setSelected(card.learned);
        if (edit_mode) if (cards.size() == 0) {
            card.left_side.setVisible(false);
            card.right_side.setVisible(false);
            updatePosition();
            delete_card.setEnabled(false);
            properties.setEnabled(false);
            return;
        } else {
            card_panel.setVisible(true);
            card.left_side.setVisible(true);
            card.right_side.setVisible(true);
            delete_card.setEnabled(true);
            properties.setEnabled(true);
        }
        if (mode == QUIZ_LEFT_SOUND || mode == QUIZ_RIGHT_SOUND) {
            card.left_side.setVisible(false);
            card.right_side.setVisible(false);
        } else if (mode == QUIZ_LEFT) {
            card.left_side.setVisible(true);
            card.right_side.setVisible(false);
        } else if (mode == QUIZ_RIGHT) {
            card.left_side.setVisible(false);
            card.right_side.setVisible(true);
        } else {
            card.left_side.setVisible(true);
            card.right_side.setVisible(true);
        }
        if (edit_mode) {
            card.left_side.setFocusable(true);
            card.right_side.setFocusable(true);
        } else {
            card.left_side.setFocusable(false);
            card.right_side.setFocusable(false);
        }
        add_edits = false;
        JComponent left = card.left_side.get();
        JComponent right = card.right_side.get();
        add_edits = true;
        card.left_side.setSoundFlag(lSoundFlag);
        card.right_side.setSoundFlag(rSoundFlag);
        card.left_side.setEditMode(edit_mode);
        card.right_side.setEditMode(edit_mode);
        card_panel.removeAll();
        card_panel.add(left);
        card_panel.add(right);
        card_panel.repaint();
        if (mode != BROWSE) show.requestFocus(); else {
            updatePosition();
            if (edit_mode) {
                updateEditMenu();
                if (card.left_side.getType() != Side.IMAGE) left.requestFocus(); else if (card.right_side.getType() != Side.IMAGE) right.requestFocus();
            }
        }
        if (mode == QUIZ_LEFT_SOUND) {
            if (!card.left_side.playSound()) {
                JOptionPane.showMessageDialog(null, "There is no sound set for " + lName + " on this card.", "No Sound", JOptionPane.WARNING_MESSAGE);
                show.doClick();
            }
        } else if (mode == QUIZ_RIGHT_SOUND) {
            if (!card.right_side.playSound()) {
                JOptionPane.showMessageDialog(null, "There is no sound set for " + rName + " on this card.", "No Sound", JOptionPane.WARNING_MESSAGE);
                show.doClick();
            }
        }
    }

    public boolean read(String filename) {
        StringBuffer attribute = new StringBuffer();
        BitSet b;
        Card c;
        StringBuffer contents = new StringBuffer();
        StringBuffer element = new StringBuffer();
        String s;
        StringBuffer value = new StringBuffer();
        int version;
        try {
            InputStream is = new FileInputStream(filename);
            Reader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            ZipInputStream zis = null;
            if (XML.startFile(r)) compressed = false; else {
                compressed = true;
                r.close();
                is = new FileInputStream(filename);
                zis = new ZipInputStream(is);
                zis.getNextEntry();
                r = new InputStreamReader(zis);
                if (!XML.startFile(r)) return false;
            }
            this.filename = filename;
            XML.openElement(element, attribute, value);
            version = Integer.parseInt(value.toString());
            XML.openElement(element, attribute, value);
            while (XML.readElement(element, contents)) {
                s = element.toString();
                if (s.equals("title")) title = contents.toString(); else if (s.equals("left_name")) lName = contents.toString(); else if (s.equals("right_name")) rName = contents.toString(); else if (s.equals("left_font")) lFontName = contents.toString(); else if (s.equals("right_font")) rFontName = contents.toString(); else if (s.equals("left_font_size")) lFontSize = Integer.parseInt(contents.toString()); else if (s.equals("right_font_size")) rFontSize = Integer.parseInt(contents.toString()); else if (s.equals("left_sound")) lSoundFlag = Boolean.valueOf(contents.toString()).booleanValue(); else rSoundFlag = Boolean.valueOf(contents.toString()).booleanValue();
            }
            if (sets.read(element)) XML.openElement(element, attribute, value);
            c = new Card();
            while (c.read(version, cards.size(), sets)) {
                cards.addElement(c);
                c = new Card();
            }
            c = null;
            XML.finishFile();
            if (compressed) {
                DataInputStream dis = new DataInputStream(zis);
                for (int i = 0; i < cards.size(); i++) {
                    ((Card) cards.elementAt(i)).readFiles(zis, dis);
                }
            }
            dirty = false;
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    public void renameItem(int oldPosition, int newPosition, String newName) {
        sets.rename(oldPosition, newPosition, newName);
    }

    public void setCardSides(int position, Side left_side, Side right_side) {
        closeCard();
        current_card = position;
        current_index = current_card;
        dirty = true;
        ((Card) cards.elementAt(position)).left_side = left_side;
        ((Card) cards.elementAt(position)).right_side = right_side;
        openCard();
    }

    public void setCurrentSet(int set, boolean view_unlearned_cards, boolean view_learned_cards) {
        CardSet s = set == -1 ? null : sets.getSet(set);
        set_members.removeAllElements();
        for (int i = 0; i < cards.size(); i++) {
            boolean learned = ((Card) cards.elementAt(i)).learned;
            if ((s == null || s.members.get(i)) && ((!learned && view_unlearned_cards) || (learned && view_learned_cards))) set_members.addElement(new Integer(i));
        }
    }

    public void setFilename(String filename) {
        this.filename = filename;
        dirty = true;
    }

    public void undoableEditHappened(UndoableEditEvent e) {
        if (add_edits) {
            undo_manager.addEdit(new GoToCard(current_card, e.getEdit()));
            updateEditMenu();
        }
    }

    private void updateEditMenu() {
        redo.setText(undo_manager.getRedoPresentationName());
        redo.setEnabled(undo_manager.canRedo());
        undo.setText(undo_manager.getUndoPresentationName());
        undo.setEnabled(undo_manager.canUndo());
    }

    public void updateFonts() {
        String fontName;
        Font left_font, right_font;
        if (lFontName.equals("Default")) fontName = QuizCards.w.getFont().getName(); else fontName = lFontName;
        left_font = new Font(fontName, Font.PLAIN, lFontSize);
        if (rFontName.equals("Default")) fontName = QuizCards.w.getFont().getName(); else fontName = rFontName;
        right_font = new Font(fontName, Font.PLAIN, rFontSize);
        Side.setFont(left_font, right_font);
    }

    private void updatePosition() {
        int num_cards = set_members.size();
        if (0 == num_cards || isFirstCard()) {
            first.setEnabled(false);
            first.transferFocus();
            move_top.setEnabled(false);
            move_up.setEnabled(false);
            prev.setEnabled(false);
            prev.transferFocus();
        } else {
            first.setEnabled(true);
            move_top.setEnabled(true);
            move_up.setEnabled(true);
            prev.setEnabled(true);
        }
        if (0 == num_cards || isLastCard()) {
            last.setEnabled(false);
            last.transferFocus();
            move_bottom.setEnabled(false);
            move_down.setEnabled(false);
            next.setEnabled(false);
            next.transferFocus();
        } else {
            last.setEnabled(true);
            move_bottom.setEnabled(true);
            move_down.setEnabled(true);
            next.setEnabled(true);
        }
        if (0 == num_cards) {
            location.setText("0 Cards");
            learned.setSelected(false);
        } else location.setText("Card " + (current_index + 1) + " of " + set_members.size());
        learned.setEnabled(num_cards > 0);
        location.setEnabled(num_cards > 0);
    }

    public void write() {
        int i;
        try {
            OutputStream os = new FileOutputStream(filename);
            ZipOutputStream zos = new ZipOutputStream(os);
            OutputStreamWriter out;
            if (compressed) {
                zos.putNextEntry(new ZipEntry("QuizCards XML"));
                out = new OutputStreamWriter(zos);
                XML.startFile(out);
            } else {
                out = new OutputStreamWriter(os, "UTF-8");
                XML.startFile(out);
            }
            XML.startElement("qcards:stack", "version", String.valueOf(QuizCards.version));
            XML.startElement("properties");
            XML.writeElement("title", title);
            XML.writeElement("left_name", lName);
            XML.writeElement("right_name", rName);
            XML.writeElement("left_font", lFontName);
            XML.writeElement("right_font", rFontName);
            XML.writeElement("left_font_size", Integer.toString(lFontSize));
            XML.writeElement("right_font_size", Integer.toString(rFontSize));
            XML.writeElement("left_sound", String.valueOf(lSoundFlag));
            XML.writeElement("right_sound", String.valueOf(rSoundFlag));
            XML.finishElement();
            sets.write();
            XML.startElement("cards");
            for (i = 0; i < cards.size(); i++) ((Card) cards.elementAt(i)).write(i, sets);
            XML.finishElement();
            XML.finishElement();
            XML.finishFile();
            out.flush();
            if (compressed) {
                DataOutputStream dos = new DataOutputStream(zos);
                zos.closeEntry();
                for (i = 0; i < cards.size(); i++) {
                    ((Card) cards.elementAt(i)).writeFiles(zos, dos);
                }
                dos.flush();
                zos.flush();
                zos.finish();
            }
            os.flush();
            dirty = false;
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

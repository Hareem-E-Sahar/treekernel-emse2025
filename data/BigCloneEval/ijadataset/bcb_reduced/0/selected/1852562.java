package sk.bur.viliam.notilo.view;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import sk.bur.viliam.notilo.Notilo;
import sk.bur.viliam.notilo.database.DatabaseException;
import sk.bur.viliam.notilo.model.Book;
import sk.bur.viliam.notilo.view.tool.AppAction;
import sk.bur.viliam.notilo.view.tool.MenuBuilder;
import sk.bur.viliam.notilo.view.tool.ToolBuilder;

/**
 * The main application dialog.
 * 
 * @author Viliam
 */
public class MainDialog extends JFrame {

    /**
	 * Serialization support.
	 */
    private static final long serialVersionUID = 1L;

    private static final String BOOK_FILE_EXTENSTION = "not";

    private static final String TEST_BOOK_PATH = Notilo.RELEASE ? "data.not" : "D:\\2010\\notilo\\_test\\test.not";

    BookEditor bookEditor;

    /**
	 * Creates a new application dialog.
	 */
    public MainDialog(String parameter) {
        setTitle(Notilo.TITLE);
        setIconImage(new ImageIcon("img/icon.png").getImage());
        setSize(800, 600);
        buildMenu();
        bookEditor = new BookEditor();
        add(bookEditor.getComponent());
        addWindowListener(new MyWindowListener());
        setVisible(true);
        if (null != parameter) {
            openBook(new File(parameter));
        }
    }

    private void buildMenu() {
        AppAction actionProgramAbout = new ActionProgramAbout();
        actionProgramAbout.setKey(KeyEvent.VK_A);
        actionProgramAbout.setIcon("img/menu_program_about.png");
        actionProgramAbout.setText("About", "Display short information about program");
        AppAction actionProgramQuit = new ActionProgramQuit();
        actionProgramQuit.setKey(KeyEvent.VK_Q);
        actionProgramQuit.setIcon("img/menu_program_quit.png");
        actionProgramQuit.setText("Quit", "Close this application");
        AppAction actionBookNew = new ActionBookNew();
        actionBookNew.setKey(KeyEvent.VK_N);
        actionBookNew.setIcon("img/menu_book_new.png");
        actionBookNew.setText("New", "Create a new note book");
        AppAction actionBookOpen = new ActionBookOpen();
        actionBookOpen.setKey(KeyEvent.VK_O);
        actionBookOpen.setIcon("img/menu_book_open.png");
        actionBookOpen.setText("Open", "Open existing note book");
        AppAction actionBookClose = new ActionBookClose();
        actionBookClose.setKey(KeyEvent.VK_C);
        actionBookClose.setIcon("img/menu_book_close.png");
        actionBookClose.setText("Close", "Close current note book");
        AppAction actionPageMoveUp = new ActionPageMoveUp();
        actionPageMoveUp.setKey(KeyEvent.VK_U);
        actionPageMoveUp.setIcon("img/menu_page_move_up.png");
        actionPageMoveUp.setText("Move Up");
        AppAction actionPageMoveDown = new ActionPageMoveDown();
        actionPageMoveDown.setKey(KeyEvent.VK_D);
        actionPageMoveDown.setIcon("img/menu_page_move_down.png");
        actionPageMoveDown.setText("Move Down");
        AppAction actionPageMoveInside = new ActionPageMoveInside();
        actionPageMoveInside.setKey(KeyEvent.VK_I);
        actionPageMoveInside.setIcon("img/menu_page_move_inside.png");
        actionPageMoveInside.setText("Move Inside");
        AppAction actionPageMoveOutside = new ActionPageMoveOutside();
        actionPageMoveOutside.setKey(KeyEvent.VK_O);
        actionPageMoveOutside.setIcon("img/menu_page_move_outside.png");
        actionPageMoveOutside.setText("Move Outside");
        AppAction actionPageAddSibling = new ActionPageAddSibling();
        actionPageAddSibling.setKey(KeyEvent.VK_S);
        actionPageAddSibling.setIcon("img/menu_page_add_sibling.png");
        actionPageAddSibling.setText("Add Sibling");
        AppAction actionPageAddChild = new ActionPageAddChild();
        actionPageAddChild.setKey(KeyEvent.VK_C);
        actionPageAddChild.setIcon("img/menu_page_add_child.png");
        actionPageAddChild.setText("Add Child");
        AppAction actionPageRemove = new ActionPageRemove();
        actionPageRemove.setKey(KeyEvent.VK_R);
        actionPageRemove.setIcon("img/menu_page_remove.png");
        actionPageRemove.setText("Remove");
        AppAction actionHelpHomepage = new ActionHelp("http://www.viliam.bur.sk/en/notilo.html");
        actionHelpHomepage.setKey(KeyEvent.VK_H);
        actionHelpHomepage.setIcon("img/menu_help.png");
        actionHelpHomepage.setText("Project Homepage");
        MenuBuilder menu = new MenuBuilder(this);
        menu.addBar();
        menu.addMenu("Program", KeyEvent.VK_P);
        menu.addItem(actionProgramAbout);
        menu.addItemSeparator();
        menu.addItem(actionProgramQuit);
        menu.addMenu("Book", KeyEvent.VK_D);
        menu.addItem(actionBookNew);
        menu.addItem(actionBookOpen);
        menu.addItem(actionBookClose);
        menu.addMenu("Page", KeyEvent.VK_N);
        menu.addItem(actionPageMoveUp);
        menu.addItem(actionPageMoveDown);
        menu.addItem(actionPageMoveInside);
        menu.addItem(actionPageMoveOutside);
        menu.addItemSeparator();
        menu.addItem(actionPageAddSibling);
        menu.addItem(actionPageAddChild);
        menu.addItemSeparator();
        menu.addItem(actionPageRemove);
        menu.addMenuSeparator();
        menu.addMenu("Help", KeyEvent.VK_H);
        menu.addItem(actionHelpHomepage);
        ToolBuilder tool = new ToolBuilder(this);
        tool.addBar();
        tool.addItem(actionBookNew);
        tool.addItem(actionBookOpen);
        tool.addItem(actionBookClose);
        tool.addSeparator();
        tool.addItem(actionPageAddSibling);
        tool.addItem(actionPageAddChild);
        tool.addSeparator();
        tool.addItem(actionPageRemove);
        tool.addSeparator();
        tool.addItem(actionPageMoveUp);
        tool.addItem(actionPageMoveDown);
        tool.addItem(actionPageMoveInside);
        tool.addItem(actionPageMoveOutside);
        tool.addSeparator();
        tool.addItem(actionProgramAbout);
        tool.addItem(actionProgramQuit);
    }

    public void openBook(File file) {
        try {
            Book book = Book.open(file);
            bookEditor.open(book);
        } catch (DatabaseException exception) {
            System.out.println(exception);
            JOptionPane.showMessageDialog(MainDialog.this, "Error opening book", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class ActionProgramAbout extends AppAction {

        @Override
        public void action() {
            JOptionPane.showMessageDialog(MainDialog.this, "Notilo personal information manager\n" + "Copyright 2009,2010 Viliam Bur");
        }
    }

    class ActionProgramQuit extends AppAction {

        @Override
        public void action() {
            dispose();
        }
    }

    class ActionBookNew extends AppAction {

        @Override
        public void action() {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Create new database");
            fc.addChoosableFileFilter(new FileNameExtensionFilter("DataNotes databases", BOOK_FILE_EXTENSTION));
            fc.setSelectedFile(new File(TEST_BOOK_PATH));
            if (JFileChooser.APPROVE_OPTION == fc.showDialog(MainDialog.this, "Create")) {
                if (fc.getSelectedFile().exists()) {
                    JOptionPane.showMessageDialog(MainDialog.this, "File already exists!");
                    return;
                }
                try {
                    Book book = Book.create(fc.getSelectedFile());
                    bookEditor.open(book);
                } catch (DatabaseException exception) {
                    System.out.println(exception);
                    JOptionPane.showMessageDialog(MainDialog.this, "Error creating database", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class ActionBookOpen extends AppAction {

        @Override
        public void action() {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Open existing database");
            fc.addChoosableFileFilter(new FileNameExtensionFilter("DataNotes databases", BOOK_FILE_EXTENSTION));
            fc.setSelectedFile(new File(TEST_BOOK_PATH));
            if (JFileChooser.APPROVE_OPTION == fc.showDialog(MainDialog.this, "Open")) {
                try {
                    Book book = Book.open(fc.getSelectedFile());
                    bookEditor.open(book);
                } catch (DatabaseException exception) {
                    System.out.println(exception);
                    JOptionPane.showMessageDialog(MainDialog.this, "Error opening database", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class ActionBookClose extends AppAction {

        @Override
        public void action() {
            try {
                bookEditor.close();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }

    class ActionPageAddChild extends AppAction {

        @Override
        public void action() {
            bookEditor.editAddChildPage();
        }
    }

    class ActionPageAddSibling extends AppAction {

        @Override
        public void action() {
            bookEditor.editAddSiblingPage();
        }
    }

    class ActionPageRemove extends AppAction {

        @Override
        public void action() {
            bookEditor.editRemovePage();
        }
    }

    class ActionPageMoveUp extends AppAction {

        @Override
        public void action() {
            bookEditor.editMovePageUp();
        }
    }

    class ActionPageMoveDown extends AppAction {

        @Override
        public void action() {
            bookEditor.editMovePageDown();
        }
    }

    class ActionPageMoveInside extends AppAction {

        @Override
        public void action() {
            bookEditor.editMovePageInside();
        }
    }

    class ActionPageMoveOutside extends AppAction {

        @Override
        public void action() {
            bookEditor.editMovePageOutside();
        }
    }

    class ActionHelp extends AppAction {

        private URI _uri;

        public ActionHelp(String uri) {
            try {
                _uri = new URI(uri);
            } catch (URISyntaxException e) {
            }
        }

        @Override
        public void action() {
            if ((null != _uri) && Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(_uri);
                        return;
                    } catch (IOException e) {
                    }
                }
            }
            JOptionPane.showMessageDialog(MainDialog.this, "Cannot open page " + _uri);
        }
    }

    @Deprecated
    class ActionNotImplemented extends AppAction {

        @Override
        public void action() {
        }
    }

    class MyWindowListener extends WindowAdapter {

        @Override
        public void windowClosed(WindowEvent windowEvent) {
            try {
                bookEditor.close();
            } catch (DatabaseException exception) {
            }
        }

        @Override
        public void windowClosing(WindowEvent windowEvent) {
            dispose();
        }
    }
}

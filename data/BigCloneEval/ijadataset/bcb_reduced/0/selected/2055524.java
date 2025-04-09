package vivace.view;

import vivace.helper.GUIHelper;
import vivace.model.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Observer;
import java.util.Observable;
import javax.swing.*;

public class Notation extends SelectionArea implements Observer {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7493781747855057523L;

    private Project model;

    NotationToolBar notationToolBar;

    /**
	 * Constructor notation
	 * Create a note in the classical notation using a FONT
	 */
    public Notation() {
        model = App.Project;
        App.addProjectObserver(this, App.Source.MODEL);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File("resources/Euterpe.ttf"));
            GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
            g.registerFont(font);
        } catch (IOException e) {
            GUIHelper.displayError(e);
        } catch (FontFormatException e) {
            e.printStackTrace();
        }
        setLayout(new BorderLayout());
        NoteSystem noteSystem = new NoteSystem(model);
        notationToolBar = new NotationToolBar();
        JScrollPane scrollPane = new JScrollPane(noteSystem);
        add(notationToolBar, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void update(Observable o, Object arg) {
        boolean toolBarVisible = App.UI.getTool() == UI.Tool.PENCIL;
        notationToolBar.setVisible(toolBarVisible);
    }
}

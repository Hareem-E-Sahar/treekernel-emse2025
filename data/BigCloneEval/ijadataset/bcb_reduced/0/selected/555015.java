package ui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URL;
import winecellar.util.*;

/**
 * Organizes the input form and detailed info for a single wine.
 * 
 * @author Dimitrij Pankratz, Anton Musichin
 * @version 1.91
 */
public class WinePanel extends JPanel {

    private static final long serialVersionUID = -5533102545942467250L;

    public static final String[] PRE_SETTED_COLOR = { "", "Wei�", "Rot", "Ros�", "sonstige" };

    public static final String[] PRE_SETTED_SIZE = { "", "0.375", "0.75", "1.0", "1.5" };

    public static final String[] PRE_SETTED_RATING = { "*", "**", "***", "****", "*****" };

    private JLabel labelName = new JLabel("Bezeichnung", JLabel.TRAILING);

    private JLabel labelEstate = new JLabel("Weingut", JLabel.TRAILING);

    private JLabel labelRegion = new JLabel("Region", JLabel.TRAILING);

    private JLabel labelCountry = new JLabel("Land", JLabel.TRAILING);

    private JLabel labelYear = new JLabel("Jahrgang", JLabel.TRAILING);

    private JLabel labelGrape = new JLabel("Rebsorte", JLabel.TRAILING);

    private JLabel labelColor = new JLabel("Farbe", JLabel.TRAILING);

    private JLabel labelSize = new JLabel("Flaschengr��e [l]", JLabel.TRAILING);

    private JLabel labelParkerPoints = new JLabel("Bew. nach Parker", JLabel.TRAILING);

    private JLabel labelRating = new JLabel("Pers. Bewertung", JLabel.TRAILING);

    private JLabel labelStocks = new JLabel("Weinbestand", JLabel.TRAILING);

    private JLabel labelPrice = new JLabel("Einkaufspreis [�]", JLabel.TRAILING);

    private JLabel labelMeals = new JLabel("Passende Speisen", JLabel.TRAILING);

    private JLabel labelUrl = new JLabel("Webseite", JLabel.TRAILING);

    private JTextField fieldName = new JTextField();

    private JTextField fieldEstate = new JTextField();

    private JTextField fieldRegion = new JTextField();

    private JTextField fieldCountry = new JTextField();

    private JTextField fieldYear = new JTextField();

    private JTextField fieldGrape = new JTextField();

    private JComboBox fieldColor = new JComboBox(PRE_SETTED_COLOR);

    private JComboBox fieldSize = new JComboBox(PRE_SETTED_SIZE);

    private JTextField fieldParkerPoints = new JTextField();

    private JComboBox fieldRating = new JComboBox(PRE_SETTED_RATING);

    private JTextField fieldStocks = new JTextField();

    private JTextField fieldPrice = new JTextField();

    private JList fieldMeals = new JList();

    private JTextField fieldUrl = new JTextField();

    private JButton buttonAddMeal = new JButton("Hinzuf�gen");

    private JButton buttonDeleteMeal = new JButton("Entfernen");

    private JButton buttonIncreaseStocks = new JButton("Kiste kaufen");

    private JButton buttonDecreaseStocks = new JButton("Flasche trinken");

    private JButton buttonBrowse = new JButton("Website besuchen");

    /**
	 * Class constructor.
	 */
    public WinePanel() {
        super();
        initialize();
    }

    /**
	 * Class constructor.
	 * 
	 * @param wine Wine to load the panel.
	 */
    public WinePanel(Wine wine) {
        super();
        initialize();
        setWine(wine);
    }

    /**
	 * Initializes WinePanel components.
	 */
    private void initialize() {
        Insets buttonInsets = new Insets(0, 0, 0, 0);
        buttonAddMeal.setMargin(buttonInsets);
        buttonDeleteMeal.setMargin(buttonInsets);
        buttonIncreaseStocks.setMargin(buttonInsets);
        buttonDecreaseStocks.setMargin(buttonInsets);
        buttonBrowse.setMargin(buttonInsets);
        Font font = new Font(null, Font.PLAIN, 11);
        labelName.setFont(font);
        labelEstate.setFont(font);
        labelRegion.setFont(font);
        labelCountry.setFont(font);
        labelYear.setFont(font);
        labelGrape.setFont(font);
        labelColor.setFont(font);
        labelSize.setFont(font);
        labelParkerPoints.setFont(font);
        labelRating.setFont(font);
        labelStocks.setFont(font);
        labelPrice.setFont(font);
        labelMeals.setFont(font);
        labelUrl.setFont(font);
        fieldName.setFont(font);
        fieldEstate.setFont(font);
        fieldRegion.setFont(font);
        fieldCountry.setFont(font);
        fieldYear.setFont(font);
        fieldGrape.setFont(font);
        fieldColor.setFont(font);
        fieldSize.setFont(font);
        fieldParkerPoints.setFont(font);
        fieldRating.setFont(font);
        fieldStocks.setFont(font);
        buttonIncreaseStocks.setFont(font);
        buttonDecreaseStocks.setFont(font);
        fieldPrice.setFont(font);
        fieldMeals.setFont(font);
        buttonAddMeal.setFont(font);
        buttonDeleteMeal.setFont(font);
        fieldUrl.setFont(font);
        buttonBrowse.setFont(font);
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        JScrollPane scPane = new JScrollPane(fieldMeals);
        final GroupLayout.Alignment LEADING = GroupLayout.Alignment.LEADING;
        hGroup.addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(labelEstate).addComponent(labelRegion).addComponent(labelCountry).addComponent(labelYear).addComponent(labelGrape).addComponent(labelColor).addComponent(labelSize).addComponent(labelParkerPoints).addComponent(labelRating).addComponent(labelStocks).addComponent(labelPrice).addComponent(labelMeals).addComponent(labelUrl));
        hGroup.addGroup(layout.createParallelGroup().addComponent(fieldName, LEADING, -1, 0, -1).addComponent(fieldEstate, LEADING, -1, 0, -1).addComponent(fieldRegion, LEADING, -1, 0, -1).addComponent(fieldCountry, LEADING, -1, 0, -1).addComponent(fieldYear, LEADING, -1, 0, -1).addComponent(fieldGrape, LEADING, -1, 0, -1).addComponent(fieldColor, LEADING, -1, 0, -1).addComponent(fieldSize, LEADING, -1, 0, -1).addComponent(fieldParkerPoints, LEADING, -1, 0, -1).addComponent(fieldRating, LEADING, -1, 0, -1).addComponent(fieldStocks, LEADING, -1, 0, -1).addComponent(scPane, LEADING, -1, 0, -1).addGroup(layout.createSequentialGroup().addComponent(buttonIncreaseStocks).addComponent(buttonDecreaseStocks)).addComponent(fieldPrice).addGroup(layout.createSequentialGroup().addComponent(buttonAddMeal).addComponent(buttonDeleteMeal)).addComponent(fieldUrl, LEADING, -1, 0, -1).addComponent(buttonBrowse, LEADING, -1, 0, -1));
        layout.setHorizontalGroup(hGroup);
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelName).addComponent(fieldName));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelEstate).addComponent(fieldEstate));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelRegion).addComponent(fieldRegion));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelCountry).addComponent(fieldCountry));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelYear).addComponent(fieldYear));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelGrape).addComponent(fieldGrape));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelColor).addComponent(fieldColor));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelSize).addComponent(fieldSize));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelParkerPoints).addComponent(fieldParkerPoints));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelRating).addComponent(fieldRating));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelStocks).addComponent(fieldStocks));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(buttonIncreaseStocks).addComponent(buttonDecreaseStocks));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelPrice).addComponent(fieldPrice));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelMeals).addComponent(scPane, -1, 0, -1));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(buttonAddMeal).addComponent(buttonDeleteMeal));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelUrl).addComponent(fieldUrl));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(buttonBrowse));
        layout.setVerticalGroup(vGroup);
        fieldColor.setEditable(true);
        fieldSize.setEditable(true);
        reset();
        buttonDecreaseStocks.setEnabled(false);
        buttonDeleteMeal.setEnabled(false);
        buttonBrowse.setEnabled(false);
        fieldStocks.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                stocksChanged();
            }

            public void insertUpdate(DocumentEvent e) {
                stocksChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                stocksChanged();
            }
        });
        buttonDecreaseStocks.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stocksDecrese();
                buttonDecreaseStocks.requestFocus();
            }
        });
        buttonIncreaseStocks.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stocksIncrese();
                buttonIncreaseStocks.requestFocus();
            }
        });
        ListSelectionModel sl = fieldMeals.getSelectionModel();
        sl.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                mealsSelectionChanged();
            }
        });
        buttonAddMeal.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mealsAdd();
                buttonAddMeal.requestFocus();
            }
        });
        buttonDeleteMeal.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mealsDelete();
                buttonDeleteMeal.requestFocus();
            }
        });
        buttonBrowse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                browse(fieldUrl.getText());
                buttonBrowse.requestFocus();
            }
        });
        fieldUrl.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                urlChanged();
            }

            public void insertUpdate(DocumentEvent e) {
                urlChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                urlChanged();
            }
        });
    }

    /**
	 * Sets the status bar manager and configures the hint messages.
	 * 
	 * @param sbHintManager Status bar hint manager.
	 */
    public void setStatusBarHintManager(StatusBarHintManager sbHintManager) {
        sbHintManager.addComponentHint(buttonAddMeal, "Zeigt ein Eingabefeld zum Einf�gen von Speisen.");
        sbHintManager.addComponentHint(buttonDeleteMeal, "L�scht die gew�hlte Speise.");
        sbHintManager.addComponentHint(buttonIncreaseStocks, "Eine Kiste mit 6 Flaschen einkaufen.");
        sbHintManager.addComponentHint(buttonDecreaseStocks, "Eine Flasche aus dem Weinkeller entfernen.");
        sbHintManager.addComponentHint(buttonBrowse, "Website des Weins besuchen.");
        sbHintManager.addComponentHint(fieldName, "Geben Sie hier eine Bezeichnung f�r den Wein ein.");
        sbHintManager.addComponentHint(fieldEstate, "Geben Sie das Weingut hier ein.");
        sbHintManager.addComponentHint(fieldRegion, "Geben Sie die Region ein.");
        sbHintManager.addComponentHint(fieldCountry, "Geben Sie das Land ein.");
        sbHintManager.addComponentHint(fieldYear, "Geben Sie das Jahr ein.");
        sbHintManager.addComponentHint(fieldGrape, "Geben Sie die Rebsorte ein.");
        sbHintManager.addComponentHint(fieldColor, "Geben Sie die Farbe ein.");
        sbHintManager.addComponentHint(fieldSize, "Geben Sie die Gr��e ein.");
        sbHintManager.addComponentHint(fieldParkerPoints, "Geben Sie die Parker Bewertung ein.");
        sbHintManager.addComponentHint(fieldRating, "Geben Sie die pers�nliche Bewertung ein.");
        sbHintManager.addComponentHint(fieldStocks, "Geben Sie den Bestand ein.");
        sbHintManager.addComponentHint(fieldPrice, "Geben Sie den Preis pro Flasche in � ein.");
        sbHintManager.addComponentHint(fieldUrl, "Geben Sie die Website ein.");
        sbHintManager.addComponentHint(fieldMeals, "W�hlen Sie eine Speise aus.");
    }

    /**
	 * Is called when URL text changed.
	 */
    private void urlChanged() {
        buttonBrowse.setEnabled(isValidUrl(fieldUrl.getText()));
    }

    /**
	 * Is called when selection of meals changed.
	 */
    private void mealsSelectionChanged() {
        Meals meals = getMeals();
        int selectedIndex = fieldMeals.getSelectedIndex();
        boolean inRange = selectedIndex >= 0 && selectedIndex < meals.size();
        buttonDeleteMeal.setEnabled(inRange);
    }

    /**
	 * Adds a meal to the meal list.
	 */
    private void mealsAdd() {
        String meal;
        meal = JOptionPane.showInputDialog(this, "Name der neuen Speise:", "Neue Speise", JOptionPane.QUESTION_MESSAGE);
        if (meal == null) {
            return;
        }
        if (meal.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sie haben keinen Namen eingegeben.", "Neue Speise", JOptionPane.ERROR_MESSAGE);
        } else if (getMeals() == null) {
            Meals meals = new Meals();
            meals.add(meal);
            fieldMeals.setListData(meals.toArray());
        } else if (getMeals().contains(meal)) {
            JOptionPane.showMessageDialog(this, "Diese Speise ist bereits vorhanden.", "Neue Speise", JOptionPane.ERROR_MESSAGE);
        } else {
            Meals meals = getMeals();
            meals.add(meal);
            fieldMeals.setListData(meals.toArray());
        }
    }

    /**
	 * Deletes selected meal from the list.
	 */
    private void mealsDelete() {
        Meals meals = getMeals();
        int selectedIndex = fieldMeals.getSelectedIndex();
        if (selectedIndex >= 0) {
            meals.remove(selectedIndex);
            fieldMeals.setListData(meals.toArray());
        }
    }

    /**
	 * Decreases the stocks of wine by 1 unit.
	 */
    private void stocksDecrese() {
        final int DECREASE_VALUE = 1;
        boolean isValidInt = isValidInteger(fieldStocks.getText());
        if (isValidInt) {
            int num = Integer.valueOf(fieldStocks.getText());
            num = Math.max(num - DECREASE_VALUE, 0);
            fieldStocks.setText(String.valueOf(num));
        }
    }

    /**
	 * Increases the stocks of wine by 6 units.
	 */
    private void stocksIncrese() {
        final int INCREASE_VALUE = 6;
        boolean isValidInt = isValidInteger(fieldStocks.getText());
        if (isValidInt) {
            int num = Integer.valueOf(fieldStocks.getText());
            num = Math.max(num + INCREASE_VALUE, 0);
            fieldStocks.setText(String.valueOf(num));
        } else if (fieldStocks.getText().isEmpty()) {
            fieldStocks.setText(String.valueOf(INCREASE_VALUE));
        }
    }

    /**
	 * Is called when stocks changed.
	 */
    private void stocksChanged() {
        boolean isValidInt = isValidInteger(fieldStocks.getText());
        if (isValidInt) {
            int num = Integer.valueOf(fieldStocks.getText());
            buttonDecreaseStocks.setEnabled(num > 0);
            buttonIncreaseStocks.setEnabled(num >= 0);
        } else if (fieldStocks.getText().isEmpty()) {
            buttonIncreaseStocks.setEnabled(true);
            buttonDecreaseStocks.setEnabled(false);
        } else {
            buttonIncreaseStocks.setEnabled(false);
            buttonDecreaseStocks.setEnabled(false);
        }
    }

    /**
	 * Resets the Wine form.
	 */
    private void reset() {
        fieldName.setText("");
        fieldEstate.setText("");
        fieldRegion.setText("");
        fieldCountry.setText("");
        fieldYear.setText("");
        fieldGrape.setText("");
        while (fieldColor.getItemCount() > PRE_SETTED_COLOR.length) {
            fieldColor.removeItemAt(0);
        }
        fieldColor.setSelectedIndex(0);
        while (fieldSize.getItemCount() > PRE_SETTED_SIZE.length) {
            fieldSize.removeItemAt(0);
        }
        fieldSize.setSelectedIndex(0);
        fieldParkerPoints.setText("");
        fieldRating.setSelectedIndex(0);
        fieldStocks.setText("");
        fieldPrice.setText("");
        fieldMeals.setListData(new Object[0]);
        fieldUrl.setText("");
    }

    /**
	 * Sets given Wine into the form.
	 * 
	 * @param wine Wine to set.
	 */
    public void setWine(Wine wine) {
        reset();
        if (wine == null) {
            return;
        }
        fieldName.setText(wine.name);
        fieldEstate.setText(wine.estate);
        fieldRegion.setText(wine.region);
        fieldCountry.setText(wine.country);
        if (wine.year != null) {
            fieldYear.setText(String.valueOf(wine.year));
        }
        fieldGrape.setText(wine.grape);
        if (wine.color != null) {
            fieldColor.insertItemAt(String.valueOf(wine.color), 0);
        }
        fieldColor.setSelectedIndex(0);
        if (wine.size != null) {
            fieldSize.insertItemAt(String.valueOf(wine.size), 0);
        }
        fieldSize.setSelectedIndex(0);
        if (wine.parker != null) {
            fieldParkerPoints.setText(String.valueOf(wine.parker));
        }
        if (wine.rating != null) {
            fieldRating.setSelectedIndex(wine.rating - 1);
        }
        if (wine.stocks != null) {
            fieldStocks.setText(String.valueOf(wine.stocks));
        }
        if (wine.price != null) {
            fieldPrice.setText(String.valueOf(wine.price));
        }
        if (wine.meals != null) {
            fieldMeals.setListData(wine.meals.toArray());
        }
        fieldUrl.setText(wine.url);
    }

    /**
	 * Returns a wine from the form data.
	 * 
	 * @return Wine, which is represented by the form.
	 * @throws WineNotWellFormedException if form is not valid.
	 */
    public Wine getWine() throws WineNotWellFormedException {
        if (!isValidWine()) {
            String message = getErrorMessage();
            throw new WineNotWellFormedException(message);
        }
        Wine wine = new Wine();
        if (!fieldName.getText().isEmpty()) {
            wine.name = fieldName.getText();
        }
        if (!fieldEstate.getText().isEmpty()) {
            wine.estate = fieldEstate.getText();
        }
        if (!fieldRegion.getText().isEmpty()) {
            wine.region = fieldRegion.getText();
        }
        if (!fieldCountry.getText().isEmpty()) {
            wine.country = fieldCountry.getText();
        }
        if (!fieldYear.getText().isEmpty()) {
            wine.year = Integer.valueOf(fieldYear.getText());
        }
        if (!fieldGrape.getText().isEmpty()) {
            wine.grape = fieldGrape.getText();
        }
        if (!fieldColor.getSelectedItem().toString().isEmpty()) {
            wine.color = fieldColor.getSelectedItem().toString();
        }
        if (!fieldSize.getSelectedItem().toString().isEmpty()) {
            wine.size = Double.valueOf(fieldSize.getSelectedItem().toString());
        }
        if (!fieldParkerPoints.getText().isEmpty()) {
            wine.parker = Integer.valueOf(fieldParkerPoints.getText());
        }
        wine.rating = fieldRating.getSelectedIndex() + 1;
        if (!fieldStocks.getText().isEmpty()) {
            int stocks = Integer.valueOf(fieldStocks.getText());
            if (stocks > 0) {
                wine.stocks = Integer.valueOf(fieldStocks.getText());
            }
        }
        if (!fieldPrice.getText().isEmpty()) {
            wine.price = Double.valueOf(fieldPrice.getText());
        }
        wine.meals = getMeals();
        wine.url = fieldUrl.getText();
        return wine;
    }

    /**
	 * Generates error message for WineNotWellFormedException
	 * 
	 * @return error message
	 */
    public String getErrorMessage() {
        final String ERROR_MSG = "Bitte f�llen Sie folgende Felder korrekt aus:";
        final String NO_ERROR_MSG = "Alle Felder sind korrekt ausgef�llt";
        final String[] ERROR_FIELDS = { "Bezeichnung (Pflichtfeld)", "Weingut (Pflichtfeld)", "Region (Pflichtfeld)", "Land (Pflichtfeld)", "Jahrgang (Pflichtfeld)", "Rebsorte (Pflichtfeld)", "Farbe (Pflichtfeld)", "Flaschengr��e (Pflichtfeld)", "Bewertung nach Parker (50-100)", "Pers�nliche Bewertung (1-5)", "Weinbestand", "Einkaufspreis", "Passende Speisen", "Webseite" };
        if (isValidWine()) {
            return NO_ERROR_MSG;
        } else {
            StringBuilder msg = new StringBuilder();
            boolean[] validField = validateFields();
            int upperBound = Math.min(ERROR_FIELDS.length, validField.length);
            msg.append(ERROR_MSG);
            for (int i = 0; i < upperBound; i++) {
                if (validField[i] == false) {
                    msg.append("\n�");
                    msg.append(ERROR_FIELDS[i]);
                }
            }
            return msg.toString();
        }
    }

    /**
	 * Returns all meals in the form.
	 * 
	 * @return Meals from the form.
	 */
    private Meals getMeals() {
        int count = fieldMeals.getModel().getSize();
        if (count <= 0) {
            return null;
        } else {
            Meals meals = new Meals();
            for (int i = 0; i < count; i++) {
                Object item = fieldMeals.getModel().getElementAt(i);
                meals.add((String) item);
            }
            return meals;
        }
    }

    /**
	 * Checks if given string represents a valid integer.
	 * 
	 * @param str String to check for.
	 * @return True if string represents an integer, otherwise false.
	 */
    private boolean isValidInteger(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
	 * Checks if given string represents a valid double.
	 * 
	 * @param str String to check for.
	 * @return True if string represents a double, otherwise false.
	 */
    private boolean isValidDouble(String str) {
        try {
            Double.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
	 * Validates all fields of the form.
	 * 
	 * @return An array of validation of every field.
	 */
    private boolean[] validateFields() {
        boolean[] validFields = new boolean[14];
        validFields[0] = !fieldName.getText().isEmpty();
        validFields[1] = !fieldEstate.getText().isEmpty();
        validFields[2] = !fieldRegion.getText().isEmpty();
        validFields[3] = !fieldCountry.getText().isEmpty();
        String yearStr = fieldYear.getText();
        if (isValidInteger(yearStr)) {
            int year = Integer.valueOf(yearStr);
            validFields[4] = year > 0 && year <= 9999 ? true : false;
        } else {
            validFields[4] = false;
        }
        validFields[5] = !fieldGrape.getText().isEmpty();
        validFields[6] = !((String) fieldColor.getSelectedItem()).isEmpty();
        String sizeStr = (String) fieldSize.getSelectedItem();
        if (isValidDouble(sizeStr)) {
            double size = Double.valueOf(sizeStr);
            validFields[7] = size >= 0 ? true : false;
        } else {
            validFields[7] = false;
        }
        String ppStr = fieldParkerPoints.getText();
        if (isValidInteger(ppStr)) {
            int pp = Integer.valueOf(ppStr);
            validFields[8] = pp >= 50 && pp <= 100 ? true : false;
        } else if (ppStr.isEmpty()) {
            validFields[8] = true;
        } else {
            validFields[8] = false;
        }
        validFields[9] = fieldRating.getSelectedIndex() >= 0 && fieldRating.getSelectedIndex() <= 4 ? true : false;
        String stocksStr = fieldStocks.getText();
        if (isValidInteger(stocksStr)) {
            int stocks = Integer.valueOf(stocksStr);
            validFields[10] = stocks >= 0 ? true : false;
        } else if (stocksStr.isEmpty()) {
            validFields[10] = true;
        } else {
            validFields[10] = false;
        }
        String priceStr = fieldPrice.getText();
        if (isValidDouble(priceStr)) {
            double price = Double.valueOf(priceStr);
            validFields[11] = price >= 0 ? true : false;
        } else if (priceStr.isEmpty()) {
            validFields[11] = true;
        } else {
            validFields[11] = false;
        }
        validFields[12] = true;
        validFields[13] = isValidUrl(fieldUrl.getText()) || fieldUrl.getText().isEmpty();
        return validFields;
    }

    /**
	 * Checks if form is valid.
	 * 
	 * @return True if form is valid, otherwise false.
	 */
    public boolean isValidWine() {
        boolean[] validFields = validateFields();
        for (int i = 0; i < validFields.length; i++) {
            if (validFields[i] == false) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Checks if the given string is a valid web URL.
	 * 
	 * @param url This string to check.
	 * @return True if given string represents web URL, otherwise false.
	 */
    private boolean isValidUrl(String url) {
        if (url.isEmpty()) {
            return false;
        } else {
            try {
                URL u = new URL(url);
                return u.getProtocol().equalsIgnoreCase("http") ? true : false;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
	 * Launches the default browser to call the URL.
	 * 
	 * @param url URL to call.
	 */
    private void browse(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Folgender Fehler ist beim �ffnen des Standart-" + "Browsers aufgetreten:\n" + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Ihre Plattform wird nicht unterst�tzt.", "Webseite besuchen", JOptionPane.ERROR_MESSAGE);
        }
    }
}

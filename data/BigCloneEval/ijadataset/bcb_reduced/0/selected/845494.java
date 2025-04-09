package com.calipso.reportgenerator.userinterface;

import com.calipso.reportgenerator.common.LanguageTraslator;
import com.calipso.reportgenerator.common.ReportSpec;
import com.calipso.reportgenerator.common.exception.InfoException;
import com.calipso.reportgenerator.common.ShowExceptionMessageDialog;
import com.calipso.reportgenerator.reportdefinitions.types.ReportDataType;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Genera la interfaz de usuario para el ingreso de Parametros
 */
public class UserParametersUI extends JDialog implements ActionListener {

    private Vector userParametersCollection;

    private HashMap params;

    private boolean isMapGenerated;

    private HashMap variablesNames;

    private JButton btAccept;

    private JButton btCancel;

    private int WIDTH = 90;

    private int HEIGHT = 26;

    /**
   * Inicializa una instancia de UserParametersUI
   * @param userParametersCollection coleccion necesaria para la creacion de la interfaz
   */
    public UserParametersUI(Frame owner, Vector userParametersCollection) {
        super(owner, true);
        this.userParametersCollection = userParametersCollection;
        this.variablesNames = new HashMap();
    }

    /**
   * Inicializa los componentes de la interfaz y los muestra
   */
    public void showUI() {
        getContentPane().add(createCenterPanel(), BorderLayout.CENTER);
        getContentPane().add(createSouthPanel(), BorderLayout.SOUTH);
        this.pack();
        setLocation(getDefaultLocation());
        this.setVisible(true);
    }

    private Point getDefaultLocation() {
        Point ownerLocation = getOwner().getLocation();
        Dimension ownerSize = getOwner().getSize();
        Dimension size = getSize();
        int x = ownerLocation.x + ownerSize.width / 2 - size.width / 2;
        int y = ownerLocation.y + ownerSize.height / 2 - size.height / 2;
        return new Point(x, y);
    }

    /**
   * Genera el Panel con los botones Aceptar o Cancelar
   * @return Panel que contiene los botones Aceptar o Cancelar
   */
    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        setTitle(LanguageTraslator.traslate("186"));
        btAccept = new JButton(LanguageTraslator.traslate("112"));
        btAccept.setSize(new Dimension(WIDTH, HEIGHT));
        btAccept.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        btAccept.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        btAccept.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        btAccept.addActionListener(this);
        btCancel = new JButton(LanguageTraslator.traslate("113"));
        btCancel.addActionListener(this);
        btCancel.setSize(new Dimension(WIDTH, HEIGHT));
        btCancel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        btCancel.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        btCancel.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        JPanel eastSouthPanel = new JPanel(new FlowLayout());
        eastSouthPanel.add(btAccept);
        eastSouthPanel.add(btCancel);
        southPanel.add(eastSouthPanel, BorderLayout.EAST);
        return southPanel;
    }

    /**
   * Panel que contiene los TextField y sus descripciones correspondientes para el
   * ingreso de Parametros por parte del usuario
   * @return Panel escencial para el ingreso de datos
   */
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridLayout(userParametersCollection.size(), 1));
        for (Enumeration enumeration = userParametersCollection.elements(); enumeration.hasMoreElements(); ) {
            UserParameterElement paramValueElement = (UserParameterElement) enumeration.nextElement();
            JLabel label = new JLabel(paramValueElement.getName());
            centerPanel.add(label, BorderLayout.CENTER);
            centerPanel.add(getValuesPanel(paramValueElement));
        }
        return centerPanel;
    }

    /**
   * Crea y devuelve un Panel segun la cantidad y elementos que hayan en la coleccion
   * de parametros de usuario
   * @param paramValueElement
   * @return Panel con los textfields necesarios para el ingreso de datos
   */
    private JPanel getValuesPanel(UserParameterElement paramValueElement) {
        JPanel pnlValues = new JPanel(new GridLayout(paramValueElement.getValues().size(), paramValueElement.getValues().size()));
        for (int i = 0; i < paramValueElement.getValues().size(); i++) {
            String currentKey = paramValueElement.getKeyAt(i);
            JLabel label = new JLabel(currentKey);
            pnlValues.add(label);
            UserParameterTextField textField = null;
            if (paramValueElement.getDimensionDataType() == ReportDataType.DATETIME_TYPE || paramValueElement.getDimensionDataType() == ReportDataType.DATE_TYPE) {
                String formatedDate = getDateFrom(paramValueElement.getValues().get(currentKey).toString());
                textField = new DateTextField(formatedDate);
            } else if (paramValueElement.getDimensionDataType() == ReportDataType.STRING_TYPE) {
                textField = new StringTextField(paramValueElement.getValues().get(currentKey).toString());
            }
            pnlValues.add(textField);
            initializeDictionary(paramValueElement.getFilterDefinitionName(), currentKey, textField);
        }
        return pnlValues;
    }

    private String getDateFrom(String dateString) {
        String returnVal = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = dateFormat.parse(dateString);
            DateFormat second = SimpleDateFormat.getDateInstance(DateFormat.SHORT, LanguageTraslator.getLocale());
            returnVal = second.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return returnVal;
    }

    /**
   * Genera un Map que contiene las instancias de los TextFields creados
   * @param name Nombre del Filtro
   * @param paramType posibles valores FROM, TO, QUANTITY, VALUE
   * @param textfield instancia de textfield
   */
    private void initializeDictionary(String name, String paramType, JTextField textfield) {
        if (paramType.equals(LanguageTraslator.traslate("146"))) {
            variablesNames.put(name + " FROM", textfield);
        } else if (paramType.equals(LanguageTraslator.traslate("147"))) {
            variablesNames.put(name + " TO", textfield);
        } else if (paramType.equals(LanguageTraslator.traslate("149"))) {
            variablesNames.put(name + " QUANTITY", textfield);
        } else if (paramType.equals(LanguageTraslator.traslate("150"))) {
            variablesNames.put(name + " VALUE", textfield);
        }
    }

    /**
   * Metodo que administra los eventos de la clase
   * @param ae Evento correspondiente
   */
    public void actionPerformed(ActionEvent ae) {
        isMapGenerated = false;
        if (ae.getSource() == btAccept) {
            Hashtable incorrectInputs = validateUserParameters();
            if (incorrectInputs.size() > 0) {
                showMessage(incorrectInputs);
                return;
            }
            try {
                params = getUserParameters();
            } catch (InfoException e) {
                ShowExceptionMessageDialog.initExceptionDialogMessage(LanguageTraslator.traslate("257"), e);
            }
            isMapGenerated = true;
            this.dispose();
        } else {
            dispose();
        }
    }

    /**
   * Muestra un Dialog en caso de que se hayan cometido errores en el ingreso
   * @param incorrectInputs Map que contiene los valores ingresados incorrectos
   */
    private void showMessage(Hashtable incorrectInputs) {
        String incorrectInputsString = "";
        for (Enumeration enumeration = incorrectInputs.keys(); enumeration.hasMoreElements(); ) {
            String currentKey = enumeration.nextElement().toString();
            incorrectInputsString = incorrectInputsString + currentKey + " > " + incorrectInputs.get(currentKey) + '\n';
        }
        JOptionPane.showMessageDialog(this, incorrectInputsString, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
   * Obtiene los parametros de usuario
   * @return Hashmap que contiene los parametros de usuario
   */
    private HashMap getUserParameters() throws InfoException {
        HashMap userParameters = new HashMap();
        Set keys = variablesNames.keySet();
        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            String currentKey = iterator.next().toString();
            String[] tokens = prepareTokens(currentKey);
            userParameters.put(tokens[0].toUpperCase() + tokens[1], ((UserParameterTextField) variablesNames.get(currentKey)).getFieldText());
        }
        return userParameters;
    }

    /**
   * Valida los parametros ingresados por el usuario
   * @return  Map que contiene los posibles valores incorrectos
   */
    private Hashtable validateUserParameters() {
        Hashtable incorrectMap = new Hashtable();
        Set keys = variablesNames.keySet();
        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            String currentKey = iterator.next().toString();
            String[] tokens = prepareTokens(currentKey);
            String returnedVal = validate(tokens);
            if (!returnedVal.equals("")) {
                String[] incorrectTokens = prepareTokens(returnedVal);
                incorrectMap.put(incorrectTokens[0], LanguageTraslator.traslate("152") + incorrectTokens[1] + ", " + incorrectTokens[2]);
            }
        }
        return incorrectMap;
    }

    /**
   * Valida por cada Filtro. Es decir, que el valor "TO" o "QUANTITY" sea menor al valor "FROM"
   * @param tokens
   * @return value
   */
    private String validate(String[] tokens) {
        String from = "0", to = "0";
        if (tokens[tokens.length - 1].equals("TO")) {
            to = ((JTextField) variablesNames.get(tokens[0] + " " + tokens[tokens.length - 1])).getText();
            from = ((JTextField) variablesNames.get(tokens[0] + " " + "FROM")).getText();
        } else if (tokens[tokens.length - 1].equals("QUANTITY")) {
            to = ((JTextField) variablesNames.get(tokens[0] + " " + tokens[tokens.length - 1])).getText();
            from = ((JTextField) variablesNames.get(tokens[0] + " " + "FROM")).getText();
        } else if (tokens[tokens.length - 1].equals("VALUE")) {
            to = ((JTextField) variablesNames.get(tokens[0] + " " + tokens[tokens.length - 1])).getText();
        } else {
            from = "-1";
        }
        if (from.compareTo(to) > 0) {
            return tokens[0] + " " + from + " " + to;
        }
        return "";
    }

    /**
   * A partir de un String devuelve los tokens
   * @param currentKey
   * @return Array que contiene los tokens
   */
    private String[] prepareTokens(String currentKey) {
        StringTokenizer stringTokenizer = new StringTokenizer(currentKey);
        String[] tokens = new String[stringTokenizer.countTokens()];
        for (int i = 0; stringTokenizer.hasMoreTokens(); i++) {
            tokens[i] = stringTokenizer.nextToken();
        }
        return tokens;
    }

    /**
   * Devuelve los parametros de usuario
   * @return Map que contiene los parametros de usuario
   */
    public HashMap getParams() {
        return params;
    }

    /**
   * Devuelve un boolean que determina si se ha generado o no el Map con los parametros de usuario
   * @return boolean que determina si se ha generado o no el Map con los
   * parametros de usuario
   */
    public boolean isGenerated() {
        return isMapGenerated;
    }

    /**
   * Crea la coleccion de los Filtros cuyo atributo visible = true y crea la interfaz
   * para el ingreso de los parametros de usuario.
   * En caso de que se haya generado el Map con dichos parametros devuelve el Map.
   * @param reportSpec Necesario para la creacion de la coleccion
   * @param params Map vacio que se llena luego con los nuevos parametros
   * @return booleano que determina si se desean editar los parametros
   */
    public static boolean editParams(Frame owner, ReportSpec reportSpec, Map params) {
        UserParametersCollection userParametersCollection = new UserParametersCollection(reportSpec);
        UserParametersUI userParametersUI = new UserParametersUI(owner, userParametersCollection.getUserParametersCollection());
        if (userParametersCollection.getUserParametersCollection().size() > 0) {
            userParametersUI.showUI();
            boolean result = userParametersUI.isGenerated();
            if (result) {
                params.putAll(userParametersUI.getParams());
            }
            return result;
        }
        return true;
    }
}

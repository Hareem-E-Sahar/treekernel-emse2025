import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.*;
import java.util.*;

/**
 * Diálogo para edição de tabela do banco de dados de índices de correção
 * monetária.
*/
public class EditWindow extends JDialog implements ActionListener, TableModelListener, ListSelectionListener {

    /**
   * Constrói o diálogo modal configurando o funcionamento da edição.
   *
   * @param owner Frame proprietária desse diálogo.
   * @param connection Conexão aberta ao banco de dados.
   * @param tableName Nome da tabela a editar.
   * @param dbProperties Propriedades específicas do DB.
  */
    public EditWindow(Frame owner, Connection connection, String tableName, Properties dbProperties) {
        super(owner, String.format("Editando %s.%S", dbProperties.getProperty("dbCatalogName"), tableName), true);
        this.connection = connection;
        this.tableName = tableName;
        this.dbProperties = dbProperties;
        useSQLite = dbProperties.getProperty("protocol").contains("sqlite");
        datum = new ArrayList<Object[]>();
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String q = String.format(dbProperties.getProperty("all"), tableName);
        Statement stm = null;
        ResultSet result = null;
        try {
            stm = connection.createStatement();
            result = stm.executeQuery(q);
            while (result.next()) {
                Object[] row = new Object[2];
                row[0] = f.parse(result.getString(1));
                row[1] = result.getDouble(2);
                datum.add(row);
            }
        } catch (ParseException e) {
            System.err.println(e);
        } catch (SQLException e) {
            System.err.println(e);
        } finally {
            try {
                result.close();
                stm.close();
            } catch (SQLException se) {
                System.err.println(se);
            }
        }
        datum.trimToSize();
        tableModel = new CustomTableModel();
        tableModel.addTableModelListener(this);
        table = new JTable(tableModel) {

            /**
       * Cria um table header que fornece textos de ajuda contextual
       * para os headers das colunas.
       *
       * @return Componente JTableHeader especializado.
      */
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {

                    /**
           * Obtêm o texto de ajuda contextual a ser exibido quando o mouse
           * paira sobre um header da tabela.
           *
           * @param e Evento de mouse pairando sobre um header da tabela.
          */
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        String[] headerToolTips = { "datas no formato dia/mês/ano", "taxas mensais de correção monetária" };
                        int ndx = columnModel.getColumnIndexAtX(e.getPoint().x);
                        ndx = columnModel.getColumn(ndx).getModelIndex();
                        return headerToolTips[ndx];
                    }

                    private static final long serialVersionUID = -6921792200568000158L;
                };
            }

            private static final long serialVersionUID = 5317136280874641962L;
        };
        table.getSelectionModel().addListSelectionListener(this);
        Font font = UIManager.getFont("FormattedTextField.font");
        FontMetrics metric = getFontMetrics(font);
        int w = 11 * metric.stringWidth("00/00/0000") / 10;
        int h = (int) (w * (1 + Math.sqrt(5d)) / 2);
        table.setPreferredScrollableViewportSize(new Dimension(2 * w, 2 * h));
        table.setRowHeight(metric.getMaxAscent() + metric.getMaxDescent() + metric.getAscent() / 4);
        if (!System.getProperty("java.version").startsWith("1.5")) {
            table.setFillsViewportHeight(true);
        }
        table.setDefaultRenderer(java.util.Date.class, new DateRenderer());
        @SuppressWarnings("deprecation") java.util.Date minDate = new java.util.Date(0, Calendar.JANUARY, 1);
        @SuppressWarnings("deprecation") java.util.Date maxDate = new java.util.Date(199, Calendar.DECEMBER, 31);
        table.setDefaultEditor(java.util.Date.class, new DateEditor(minDate, maxDate));
        table.setDefaultRenderer(Double.class, new DoubleRenderer());
        table.setDefaultEditor(Double.class, new DoubleEditor(-1d, 1d));
        newBtn = buildButton("images/add01.png", "images/add02.png", "<html><center>adiciona novo registro no final da tabela<br>ou após o último selecionado</center></html>", true);
        eraseBtn = buildButton("images/remove01.png", "images/remove02.png", "apaga o(s) registro(s) selecionado(s)", false);
        closeBtn = buildButton("images/exit01.png", "images/exit02.png", "encerra edição", true);
        JToolBar toolBar = new JToolBar();
        toolBar.setMargin(new Insets(0, 0, 0, 0));
        toolBar.setRollover(true);
        toolBar.add(newBtn);
        toolBar.add(eraseBtn);
        Component glue = Box.createHorizontalGlue();
        glue.setFocusable(false);
        toolBar.add(glue);
        toolBar.addSeparator();
        toolBar.add(closeBtn);
        font = font.deriveFont((5 * font.getSize2D()) / 6f);
        sizeLabel = new JLabel();
        sizeLabel.setFont(font);
        counterLabel = new JLabel();
        counterLabel.setFont(font.deriveFont(Font.ITALIC));
        JPanel statusPane = new JPanel();
        statusPane.setFont(font.deriveFont(11f));
        statusPane.setBackground(Color.WHITE);
        statusPane.setBorder(BorderFactory.createEmptyBorder(1, 2, 0, 2));
        statusPane.setLayout(new BoxLayout(statusPane, BoxLayout.X_AXIS));
        statusPane.add(sizeLabel);
        statusPane.add(Box.createGlue());
        statusPane.add(counterLabel);
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.add(toolBar, BorderLayout.NORTH);
        pane.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pane, BorderLayout.CENTER);
        add(statusPane, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent we) {
                for (int j = datum.size(); --j >= 0; ) datum.remove(j);
                heap.clear();
            }
        });
        heap = new HashSet<Object[]>();
        updateStatus();
        scrollToTarget();
    }

    /**
   * Monta botão conforme especificações e adiciona listener de ação.
   *
   * @param icoFilename Nome do arquivo resource do icone default.
   * @param rollIcoFilename Nome do arquivo resource do icone rollover.
   * @param toolTipText Texto de ajuda contextual.
   * @param enabled Habilitação de uso.
  */
    private JButton buildButton(String icoFilename, String rollIcoFilename, String toolTipText, boolean enabled) {
        JButton b = new JButton();
        b.setIcon(loadIcon(icoFilename));
        b.setRolloverIcon(loadIcon(rollIcoFilename));
        b.setToolTipText(toolTipText);
        b.setEnabled(enabled);
        b.addActionListener(this);
        return b;
    }

    /**
   * Carrega imagem como resource.
   *
   * @param filename Nome do arquivo de imagem.
  */
    private ImageIcon loadIcon(String filename) {
        return new ImageIcon(getClass().getClassLoader().getResource(filename));
    }

    /**
   * Cria um componente JRootPane associando teclas a ações.
   *
   * @return Instância de componente JRootPane especializada.
  */
    @Override
    protected JRootPane createRootPane() {
        JRootPane root = new JRootPane();
        String actionName = "EXIT";
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke keyStroke = KeyStroke.getKeyStroke("control shift ESCAPE");
        inputMap.put(keyStroke, actionName);
        ActionMap actionMap = root.getActionMap();
        actionMap.put(actionName, new AbstractAction() {

            public void actionPerformed(ActionEvent ae) {
                stop();
            }

            private static final long serialVersionUID = -7727201964869006422L;
        });
        actionName = "ADD_RECORD";
        keyStroke = KeyStroke.getKeyStroke("control shift INSERT");
        inputMap.put(keyStroke, actionName);
        actionMap.put(actionName, new AbstractAction() {

            public void actionPerformed(ActionEvent ae) {
                newBtn.doClick();
            }

            private static final long serialVersionUID = 3079299508249874494L;
        });
        actionName = "DEL_RECORD";
        keyStroke = KeyStroke.getKeyStroke("control shift DELETE");
        inputMap.put(keyStroke, actionName);
        actionMap.put(actionName, new AbstractAction() {

            public void actionPerformed(ActionEvent ae) {
                eraseBtn.doClick();
            }

            private static final long serialVersionUID = -7327207852895980389L;
        });
        return root;
    }

    /** Última chance de editar registros pendentes. */
    private void stop() {
        if (!heap.isEmpty()) {
            Object[] options = { "Continuar", "Descartar" };
            String msg = String.format("<html>Há <b>%d</b> registro(s) para inserção em pendência.<br>Continue a sessão de edição ou<br>descarte a(s) pendência(s).</html>", heap.size());
            int answer = JOptionPane.showOptionDialog(getContentPane(), msg, "Notificação do Editor", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            if (answer == 0) {
                Object[] row = heap.iterator().next();
                int rowIndex = datum.indexOf(row);
                table.setRowSelectionInterval(rowIndex, rowIndex);
                scrollToVisible(rowIndex, 0);
                return;
            }
        }
        setVisible(false);
    }

    /** Atualiza a barra de status. */
    private void updateStatus() {
        sizeLabel.setText(String.format("#Registros: %d", datum.size()));
        if (heap.isEmpty()) {
            counterLabel.setVisible(false);
        } else {
            int size = heap.size();
            char ch = (size > 1) ? 's' : ' ';
            counterLabel.setText(String.format("%d Pendente%c", size, ch));
            counterLabel.setVisible(true);
        }
    }

    /**
   * Altera a posição do viewport para visualizar a primeira linha
   * selecionada ou a última linha da tabela se não há linha selecionada.
  */
    private void scrollToTarget() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                int targetRow = table.getSelectedRow();
                if (targetRow < 0) targetRow = datum.size() - 1;
                scrollToCenter(targetRow, 0);
            }
        });
    }

    /**
   * Escorre o painel de visualização da tabela para que a célula
   * de índices fornecidos seja visível no centro.
   *
   * @param row Índice da linha da célula.
   * @param col Índice da coluna da célula.
  */
    private void scrollToCenter(int row, int col) {
        JViewport viewport = (JViewport) table.getParent();
        Rectangle rect = table.getCellRect(row, col, true);
        Rectangle viewRect = viewport.getViewRect();
        rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y);
        int centerX = (viewRect.width - rect.width) / 2;
        int centerY = (viewRect.height - rect.height) / 2;
        if (rect.x < centerX) centerX = -centerX;
        if (rect.y < centerY) centerY = -centerY;
        rect.translate(centerX, centerY);
        viewport.scrollRectToVisible(rect);
    }

    /**
   * Escorre o painel de visualização da tabela para que a célula
   * de índices fornecidos seja visível.
   *
   * @param row Índice da linha da célula.
   * @param col Índice da coluna da célula.
  */
    private void scrollToVisible(int row, int col) {
        JViewport viewport = (JViewport) table.getParent();
        Rectangle rect = table.getCellRect(row, col, true);
        Point pt = viewport.getViewPosition();
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        viewport.scrollRectToVisible(rect);
    }

    /**
   * Monta instrução SQL preparada conforme nome da expressão.
   *
   * @param task Nome da expressão.
   * @return Instrução SQL preparada
   *
   * @throws Excessão SQL.
  */
    private PreparedStatement buildStatement(String task) throws SQLException {
        PreparedStatement ps = null;
        String s = dbProperties.getProperty(task);
        if (s != null) {
            String q = String.format(s, tableName);
            ps = connection.prepareStatement(q);
        }
        return ps;
    }

    /**
   * Executa ação associada ao botão acionado.
   *
   * @param ae O evento de ação observado.
  */
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source == newBtn) {
            int[] rows = table.getSelectedRows();
            int iPos = (rows.length > 0) ? rows[rows.length - 1] + 1 : datum.size();
            datum.add(iPos, new Object[2]);
            heap.add(datum.get(iPos));
            tableModel.fireTableRowsInserted(iPos, iPos);
            table.setRowSelectionInterval(iPos, iPos);
            scrollToTarget();
            table.editCellAt(iPos, 0);
        } else if (source == eraseBtn) {
            int[] rows = table.getSelectedRows();
            java.sql.Date date;
            PreparedStatement ps = null;
            try {
                ps = buildStatement("deleteForDate");
                int count = 0;
                for (int j = rows.length; --j >= 0; ) {
                    Object[] tableRow = datum.get(rows[j]);
                    if (heap.contains(tableRow)) {
                        heap.remove(tableRow);
                    } else {
                        long millis = ((java.util.Date) tableRow[0]).getTime();
                        date = new java.sql.Date(millis);
                        if (useSQLite) ps.setString(1, date.toString()); else ps.setDate(1, date);
                        ps.addBatch();
                        count++;
                    }
                    datum.remove(rows[j]);
                }
                if (count > 0) {
                    connection.setAutoCommit(false);
                    ps.executeBatch();
                }
            } catch (SQLException e) {
                System.err.println(e);
            } finally {
                try {
                    ps.close();
                    connection.setAutoCommit(true);
                } catch (SQLException se) {
                    System.err.println(se);
                }
            }
            tableModel.fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
            if (table.isEditing()) {
                int r = table.getEditingRow();
                if (r >= datum.size()) r = datum.size() - 1;
                table.editingCanceled(new ChangeEvent(table));
                table.setRowSelectionInterval(r, r);
            }
        } else {
            stop();
        }
    }

    /**
   * Observador de eventos associados a modificações dos dados para
   * atualizar e/ou inserir registros diretamente no banco de dados.
   *
   * @param me O evento de modificação dos dados.
  */
    public void tableChanged(TableModelEvent me) {
        if (me.getType() == TableModelEvent.UPDATE) {
            int rowIndex = me.getLastRow();
            Object[] row = datum.get(rowIndex);
            if (heap.contains(row)) {
                if ((row[0] != null) && (row[1] != null)) {
                    long millis = ((java.util.Date) row[0]).getTime();
                    java.sql.Date date = new java.sql.Date(millis);
                    PreparedStatement ps = null;
                    try {
                        ps = buildStatement("insert");
                        if (useSQLite) ps.setString(1, date.toString()); else ps.setDate(1, date);
                        ps.setDouble(2, (Double) row[1]);
                        ps.executeUpdate();
                        heap.remove(row);
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(getContentPane(), "<html>Operação Cancelada.<br>A inserção não é possível pois<br>já existe registro com essa data.</html>", "Notificação do Editor", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        try {
                            ps.close();
                        } catch (SQLException se) {
                            System.err.println(se);
                        }
                    }
                }
                table.repaint();
            } else {
                int colIndex = me.getColumn();
                if (colIndex == 0) {
                    long millis = ((java.util.Date) row[0]).getTime();
                    if (millis != lastTime) {
                        java.sql.Date to = new java.sql.Date(millis);
                        java.sql.Date from = new java.sql.Date(lastTime);
                        PreparedStatement ps = null;
                        try {
                            ps = buildStatement("updateDate");
                            if (useSQLite) {
                                ps.setString(1, to.toString());
                                ps.setString(2, from.toString());
                            } else {
                                ps.setDate(1, to);
                                ps.setDate(2, from);
                            }
                            ps.executeUpdate();
                            lastTime = millis;
                        } catch (SQLException e) {
                            JOptionPane.showMessageDialog(getContentPane(), "<html>Operação Cancelada.<br>A atualização não é possível pois<br>já existe registro com essa data.</html>", "Notificação do Editor", JOptionPane.ERROR_MESSAGE);
                            ((java.util.Date) row[0]).setTime(lastTime);
                            datum.set(rowIndex, row);
                        } finally {
                            try {
                                ps.close();
                            } catch (SQLException se) {
                                System.err.println(se);
                            }
                        }
                    }
                } else {
                    if (((Double) row[1]).doubleValue() != lastValue) {
                        java.sql.Date date = new java.sql.Date(lastTime);
                        PreparedStatement ps = null;
                        try {
                            ps = buildStatement("updateValue");
                            ps.setDouble(1, (Double) row[1]);
                            if (useSQLite) ps.setString(2, date.toString()); else ps.setDate(2, date);
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            System.err.println(e);
                        } finally {
                            try {
                                ps.close();
                            } catch (SQLException se) {
                                System.err.println(se);
                            }
                        }
                    }
                }
            }
        }
        updateStatus();
    }

    /**
   * Observador de eventos de seleção de linhas da tabela habilitando ou
   * desabilitando o botão Apagar.
   *
   * @param se O evento de seleção de linhas.
  */
    public void valueChanged(ListSelectionEvent se) {
        if (!se.getValueIsAdjusting()) {
            int n = table.getSelectedRowCount();
            eraseBtn.setEnabled(n > 0);
            if (n == 1) {
                Object[] row = datum.get(table.getSelectedRow());
                if (!heap.contains(row)) {
                    lastTime = ((java.util.Date) row[0]).getTime();
                    lastValue = ((Double) row[1]).doubleValue();
                }
            }
        }
    }

    /** Classe de renderização das células do tipo Date. */
    private class DateRenderer extends DefaultTableCellRenderer {

        private DateFormat df;

        public DateRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.CENTER);
            df = DateFormat.getDateInstance(DateFormat.MEDIUM, NumberFormatFactory.getLocaleBR());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (heap.contains(datum.get(row))) {
                c.setFont(c.getFont().deriveFont(Font.ITALIC));
            }
            return c;
        }

        @Override
        public void setValue(Object value) {
            if (value == null) setText(""); else {
                @SuppressWarnings("deprecation") int day = ((java.util.Date) value).getDate();
                setForeground((day == 1) ? Color.BLACK : MARROM);
                setText(df.format(value));
            }
        }

        private static final long serialVersionUID = -1547698036031570204L;
    }

    /** Classe de renderização das células de valores numéricos. */
    private class DoubleRenderer extends DefaultTableCellRenderer {

        private NumberFormat nf;

        public DoubleRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.RIGHT);
            nf = NumberFormatFactory.getDecimalFormat("0.0000##");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (heap.contains(datum.get(row))) {
                c.setFont(c.getFont().deriveFont(Font.ITALIC));
            }
            return c;
        }

        @Override
        public void setValue(Object value) {
            if (value == null) setText(""); else {
                setForeground(((Number) value).doubleValue() >= 0 ? Color.BLACK : MARROM);
                setText(nf.format(value));
            }
        }

        private static final long serialVersionUID = 3649235752918533334L;
    }

    /** Classe gestora da apresentação dos dados da tabela. */
    private class CustomTableModel extends AbstractTableModel {

        String[] columnNames = { "Data", "Valor" };

        /**
     * Retorna o número de colunas/campos da tabela.
     *
     * @return Número de colunas/campos da tabela.
    */
        public int getColumnCount() {
            return 2;
        }

        /**
     * Retorna o número de linhas/registros da tabela.
     *
     * @return Número de linhas/registros da tabela.
    */
        public int getRowCount() {
            return datum.size();
        }

        /**
     * Retorna o nome da coluna/campo de ordem fornecida.
     *
     * @param Número de ordem da coluna/campo.
     * @return Nome da coluna/campo de ordem fornecida.
    */
        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        /**
     * Retorna o nome da classe do objeto associado a coluna/campo de
     * ordem fornecida.
     *
     * @param Número de ordem da coluna/campo.
     * @return Nome da classe do objeto associado a coluna/campo de
     * ordem fornecida.
    */
        @Override
        public Class<?> getColumnClass(int col) {
            return (col == 0) ? java.util.Date.class : Double.class;
        }

        /**
     * Checa se a célula na linha e coluna fornecida é editável.
     *
     * @param row Número da linha.
     * @param col Número da coluna.
     * @return Permissão de edição associda a célula.
    */
        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        /**
     * Acessa o valor da célula na linha e coluna fornecida.
     *
     * @param row Número da linha.
     * @param col Número da coluna.
     * @return Objeto associado à célula.
    */
        public Object getValueAt(int row, int col) {
            return datum.get(row)[col];
        }

        /**
     * Associa objeto à célula na linha e coluna fornecida.
     *
     * @param value Objeto a ser associado à célula.
     * @param row Número da linha.
     * @param col Número da coluna.
    */
        @Override
        public void setValueAt(Object value, int row, int col) {
            Object[] array = datum.get(row);
            array[col] = value;
            datum.set(row, array);
            fireTableCellUpdated(row, col);
        }

        private static final long serialVersionUID = -6970271420533522360L;
    }

    /** Instância do gestor de dados da tabela. */
    private CustomTableModel tableModel;

    /** Lista de arrays correspondentes a registros da tabela. */
    private ArrayList<Object[]> datum;

    /** Lista de arrays correspondentes a registros em inserção pendente */
    private HashSet<Object[]> heap;

    /** Milisegundos da data da linha selecionada mais recentemente. */
    private long lastTime;

    /** Valor da linha selecionada mais recentemente. */
    private double lastValue;

    private JTable table;

    private JButton newBtn;

    private JButton eraseBtn;

    private JButton closeBtn;

    /** Label no status bar para exibir o número de linhas da tabela. */
    private JLabel sizeLabel;

    /** Label no status bar para exibir o número de linhas pendentes. */
    private JLabel counterLabel;

    /** Conexão emprestada pelo aplicativo proprietário. */
    private Connection connection;

    /** Propriedades do banco de dados servido pela conexão. */
    private Properties dbProperties;

    /** Nome da tabela do banco de dados cujo conteúdo é editado. */
    private String tableName;

    /** Indicador de uso do SQLite. */
    private boolean useSQLite;

    private final Color MARROM = new Color(150, 0, 0);

    private static final long serialVersionUID = 923916801534276078L;
}

public class Test {    public UpdateView(ReadExcel excel, GenerateXML xml) throws Exception {
        this.xml = xml;
        this.excel = excel;
        this.setTitle("ILIAS User Import");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        this.add(panel, BorderLayout.CENTER);
        this.pack();
        this.setSize(400, 150);
        this.setLocation(500, 100);
        open = new JButton("Open");
        generate = new JButton("DOWNLOAD NOW");
        exit = new JButton("Exit");
        bug = new JButton("Bug/Issue Report");
        status = new JTextField();
        status.setHorizontalAlignment(JTextField.CENTER);
        status.setEditable(false);
        status.setText("NEW VERSION IS AVAILABLE");
        status.setForeground(Color.blue.darker());
        this.add(status, BorderLayout.NORTH);
        panel.setBorder(new TitledBorder("Generates XML File to Import in ILIAS e-Learning System"));
        panel.add(generate);
        panel.add(open);
        panel.add(bug);
        panel.add(exit);
        panel.setLayout(new GridLayout(2, 2));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        panel.setVisible(true);
        this.setVisible(true);
        generate.addActionListener(this);
        open.addActionListener(this);
        exit.addActionListener(this);
        bug.addActionListener(this);
        if (!d.isDesktopSupported()) bug.setEnabled(false);
    }
}
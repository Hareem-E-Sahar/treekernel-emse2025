public class Test {    public void init() {
        guie_filename = getParameter("guie");
        URL guie_url = null;
        try {
            guie_url = new URL(getCodeBase(), guie_filename);
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(guie_url.openStream()));
            XMLElement xe = new XMLElement();
            xe.parseFromReader(br);
            guieDef = new GuieDef(xe);
            System.out.println("GuieApplet init");
        } catch (Exception ex) {
            System.out.println("Error creating GuieDef: " + ex.getMessage());
            return;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    jbutton = new JButton("Click here to launch the GuiE demo");
                    getContentPane().add(jbutton);
                    Guie.setLookAndFeel();
                    ToolTipManager.sharedInstance().setDismissDelay(5 * 60 * 1000);
                    guie = new Guie(guieDef, new JFrame());
                    jbutton.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent _) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    if (guie != null) {
                                        guie.getFrame().setVisible(true);
                                    }
                                }
                            });
                        }
                    });
                }
            });
        } catch (Exception e) {
            System.out.println("cannot create GUI!");
        }
    }
}
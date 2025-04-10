public class Test {    @Override
    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
        }
        try {
            PopupFactory.setSharedInstance(new PopupFactory());
        } catch (Throwable e) {
        }
        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        String[] labels = getAppletInfo().split("\n");
        for (int i = 0; i < labels.length; i++) {
            c.add(new JLabel((labels[i].length() == 0) ? " " : labels[i]));
        }
        new Worker<Drawing>() {

            @Override
            public Drawing construct() throws IOException {
                Drawing result;
                System.out.println("getParameter.datafile:" + getParameter("datafile"));
                if (getParameter("data") != null) {
                    NanoXMLDOMInput domi = new NanoXMLDOMInput(new PertFactory(), new StringReader(getParameter("data")));
                    result = (Drawing) domi.readObject(0);
                } else if (getParameter("datafile") != null) {
                    URL url = new URL(getDocumentBase(), getParameter("datafile"));
                    InputStream in = url.openConnection().getInputStream();
                    try {
                        NanoXMLDOMInput domi = new NanoXMLDOMInput(new PertFactory(), in);
                        result = (Drawing) domi.readObject(0);
                    } finally {
                        in.close();
                    }
                } else {
                    result = null;
                }
                return result;
            }

            @Override
            protected void done(Drawing result) {
                Container c = getContentPane();
                c.setLayout(new BorderLayout());
                c.removeAll();
                c.add(drawingPanel = new PertPanel());
                initComponents();
                if (result != null) {
                    setDrawing(result);
                }
            }

            @Override
            protected void failed(Throwable value) {
                Container c = getContentPane();
                c.setLayout(new BorderLayout());
                c.removeAll();
                c.add(drawingPanel = new PertPanel());
                value.printStackTrace();
                initComponents();
                getDrawing().add(new TextFigure(value.toString()));
                value.printStackTrace();
            }

            @Override
            protected void finished() {
                Container c = getContentPane();
                initDrawing(getDrawing());
                c.validate();
            }
        }.start();
    }
}
public class Test {    private void createTransitionPanel() {
        JPanel transitionPanel;
        JComboBox R, G, B;
        JLabel rR, gG, bB, vel;
        int i;
        JSlider slVel;
        JLabel valor;
        transitionPanel = new JPanel();
        transitionPanel.setBounds(3 + 175 + 3 + 175 + 3 + 400 + 3, 3, 400, 180);
        transitionPanel.setLayout(null);
        transitionPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        rR = new JLabel("R");
        rR.setBounds(60, 30, 20, 20);
        gG = new JLabel("G");
        gG.setBounds(160, 30, 20, 20);
        bB = new JLabel("B");
        bB.setBounds(260, 30, 20, 20);
        R = new JComboBox();
        G = new JComboBox();
        B = new JComboBox();
        R.setBounds(80, 30, 40, 20);
        G.setBounds(180, 30, 40, 20);
        B.setBounds(280, 30, 40, 20);
        for (i = 0; i < 64; i++) {
            R.addItem(i + "");
            G.addItem(i + "");
            B.addItem(i + "");
        }
        R.setSelectedIndex(li.getChannelR());
        G.setSelectedIndex(li.getChannelG());
        B.setSelectedIndex(li.getChannelB());
        transitionPanel.add(rR);
        transitionPanel.add(gG);
        transitionPanel.add(bB);
        transitionPanel.add(R);
        transitionPanel.add(G);
        transitionPanel.add(B);
        vel = new JLabel("TRANSITION VELOCITY");
        vel.setBounds(80, 70, 200, 30);
        transitionPanel.add(vel);
        slVel = new JSlider();
        slVel.setBounds(76, 100, 249, 20);
        slVel.setMinimum(0);
        slVel.setMaximum(500);
        slVel.setValue((int) (li.getTransition() * 100));
        transitionPanel.add(slVel);
        valor = new JLabel("2.5s");
        valor.setBounds(300, 115, 20, 20);
        valor.setText(li.getTransition() + "s");
        transitionPanel.add(valor);
        getContentPane().add(transitionPanel);
    }
}
public class Test {    private void initialize() {
        channelLBL = new JLabel();
        channelLBL.setBounds(new Rectangle(35, 80, 51, 21));
        channelLBL.setText("Channel");
        bankSizeLBL = new JLabel();
        bankSizeLBL.setBounds(new Rectangle(30, 55, 56, 21));
        bankSizeLBL.setText("Bank Size");
        this.setSize(240, 190);
        this.setLayout(null);
        this.add(getPageLabel(), null);
        this.add(getNoteTF(), null);
        this.add(getRowCB(), null);
        this.add(getRowLBL(), null);
        this.add(getSaveBtn(), null);
        this.add(bankSizeLBL, null);
        this.add(getBankSizeTF(), null);
        this.add(channelLBL, null);
        this.add(getChannelTF(), null);
        this.add(getHoldModeLBL(), null);
        this.add(getHoldModeCB(), null);
        setName("MIDI Sequencer Poly");
        for (int i = 0; i < rowChoices.length; i++) {
            rowCB.addItem(rowChoices[i]);
        }
        getNoteTF().setText("C-1");
    }
}
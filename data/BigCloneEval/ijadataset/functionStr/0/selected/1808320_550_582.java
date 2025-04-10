public class Test {        public ChannelColorChooser(ChannelControl cControl) {
            super(new BorderLayout(5, 5));
            this.cControl = cControl;
            displayGrayBox = new Checkbox("Grayscale");
            displayGrayBox.setState(i5d.getChannelDisplayProperties(cControl.currentChannel).isDisplayedGray());
            displayGrayBox.addItemListener(this);
            add(displayGrayBox, BorderLayout.NORTH);
            cColorCanvas = new ChannelColorCanvas(this);
            add(cColorCanvas, BorderLayout.CENTER);
            Panel tempPanel = new Panel(new GridLayout(2, 1, 5, 2));
            editColorButton = new Button("Edit Color");
            editColorButton.addActionListener(this);
            tempPanel.add(editColorButton);
            editLUTButton = new Button("Edit LUT");
            editLUTButton.addActionListener(this);
            tempPanel.add(editLUTButton);
            add(tempPanel, BorderLayout.SOUTH);
            addKeyListener(win);
            displayGrayBox.addKeyListener(win);
            cColorCanvas.addKeyListener(win);
            editColorButton.addKeyListener(win);
            editLUTButton.addKeyListener(win);
            addKeyListener(ij);
            displayGrayBox.addKeyListener(ij);
            cColorCanvas.addKeyListener(ij);
            editColorButton.addKeyListener(ij);
            editLUTButton.addKeyListener(ij);
            addKeyListener(win);
            displayGrayBox.addKeyListener(win);
            cColorCanvas.addKeyListener(win);
            editColorButton.addKeyListener(win);
            editLUTButton.addKeyListener(win);
        }
}
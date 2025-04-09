package org.omnidoc.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.omnidoc.ArticleTransformer;

public class ArticleTransformPanel extends JPanel implements ActionListener {

    JLabel label;

    JButton selectButton;

    JButton selectButton2;

    JButton doIt;

    JButton exitButton;

    File source;

    File target;

    int filecount = 0;

    public ArticleTransformPanel() {
        this.setLayout(new BorderLayout());
        JPanel center = new JPanel(new FlowLayout());
        JPanel bottom = new JPanel(new FlowLayout());
        label = new JLabel("Selecione configura��o de transforma��o e arquivo");
        selectButton = new JButton("Procurar Configura��o...");
        selectButton.addActionListener(this);
        selectButton2 = new JButton("Procurar Arquivo...");
        selectButton2.addActionListener(this);
        exitButton = new JButton("Sair");
        exitButton.addActionListener(this);
        doIt = new JButton("Transformar");
        doIt.addActionListener(this);
        center.add(label);
        bottom.add(selectButton);
        bottom.add(selectButton2);
        bottom.add(doIt);
        bottom.add(exitButton);
        this.add(center, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        double y = (dim.getHeight() - this.getSize().height) / 2;
        double x = (dim.getWidth() - (this.getSize().width * 2)) / 2;
        this.setBounds((int) x, (int) y, this.getSize().width * 2, this.getSize().height);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == selectButton) {
            JFileChooser chooser = new JFileChooser(new File("."));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, "Select")) {
                source = chooser.getSelectedFile();
                label.setText(source.getAbsolutePath());
            }
        } else if (event.getSource() == selectButton2) {
            JFileChooser chooser = new JFileChooser(new File("."));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, "Select")) {
                target = chooser.getSelectedFile();
                label.setText(target.getAbsolutePath());
            }
        } else if (event.getSource() == doIt) {
            if (source != null && target != null) {
                try {
                    open(transform(source, target));
                    label.setText("Arquivo transformado");
                } catch (IOException e) {
                    e.printStackTrace();
                    label.setText("Ocorreu um erro durante a transformação.");
                }
            }
        } else {
            System.exit(0);
        }
    }

    private void open(File target) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(target.toURI());
        }
    }

    protected File transform(File config, File file) throws FileNotFoundException {
        ArticleTransformer uit = new ArticleTransformer();
        uit.setConfigFile(config);
        uit.setDataFile(file);
        return uit.transform();
    }
}

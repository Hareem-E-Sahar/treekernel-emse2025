public class Test {    private JButton getButtonImagen() {
        if (buttonImagen == null) {
            buttonImagen = new JButton();
            buttonImagen.setText(Messages.getString("gui.AdministracionResorces.6"));
            buttonImagen.setIcon(new ImageIcon(getClass().getResource("/es/unizar/cps/tecnoDiscap/data/icons/view_sidetree.png")));
            buttonImagen.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JFileChooser fc = new JFileChooser();
                    fc.addChoosableFileFilter(new ImageFilter());
                    fc.setAccessory(new ImagePreview(fc));
                    int returnVal = fc.showDialog(AdministracionResorces.this, Messages.getString("gui.AdministracionResorces.8"));
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        String rutaGlobal = System.getProperty("user.dir") + "/" + rutaDatos + "imagenes/" + file.getName();
                        String rutaRelativa = rutaDatos + "imagenes/" + file.getName();
                        try {
                            FileInputStream fis = new FileInputStream(file);
                            FileOutputStream fos = new FileOutputStream(rutaGlobal, true);
                            FileChannel canalFuente = fis.getChannel();
                            FileChannel canalDestino = fos.getChannel();
                            canalFuente.transferTo(0, canalFuente.size(), canalDestino);
                            fis.close();
                            fos.close();
                            imagen.setImagenURL(rutaRelativa);
                            gui.getEntrenamientoIzquierdaLabel().setIcon(gui.getProcesadorDatos().escalaImageIcon(((Imagen) gui.getComboBoxImagenesIzquierda().getSelectedItem()).getImagenURL()));
                            gui.getEntrenamientoDerechaLabel().setIcon(gui.getProcesadorDatos().escalaImageIcon(((Imagen) gui.getComboBoxImagenesDerecha().getSelectedItem()).getImagenURL()));
                            buttonImagen.setIcon(new ImageIcon(getClass().getResource("/es/unizar/cps/tecnoDiscap/data/icons/view_sidetreeOK.png")));
                            labelImagenPreview.setIcon(gui.getProcesadorDatos().escalaImageIcon(imagen.getImagenURL()));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                    }
                }
            });
        }
        return buttonImagen;
    }
}
public class Test {    public File gravaArquivoDeURL(URL url, String pathLocal, String verAntes, String verDepois) {
        try {
            String nomeArquivoLocal = "\\update.exe";
            Main m = new Main();
            JDialog dia = new JDialog();
            dia.setBounds(400, 300, 450, 250);
            dia.setContentPane(u);
            dia.setVisible(true);
            dia.toFront();
            JButton b = new JButton();
            JProgressBar dpb = new JProgressBar(0, 100);
            dia.add(BorderLayout.CENTER, dpb);
            u.setAtualiza("Atualizando da versão " + verAntes + " para " + verDepois);
            System.out.print("baixando|=");
            InputStream is = url.openStream();
            int i = 0;
            s = true;
            FileOutputStream fos = new FileOutputStream(pathLocal + "\\" + nomeArquivoLocal);
            int umByte = 0;
            while ((umByte = is.read()) != -1) {
                fos.write(umByte);
                i++;
                if (!dia.isVisible()) {
                    s = false;
                    break;
                }
                if (i > 10000) {
                    baixado++;
                    dpb.setValue(baixado);
                    System.out.print("=");
                    i = 0;
                    u.setProgresso(baixado);
                }
            }
            u.setProgresso(100);
            is.close();
            fos.close();
            System.out.println(">download, ok!!!");
            u.setVisible(false);
            dia.dispose();
            return new File(pathLocal + nomeArquivoLocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
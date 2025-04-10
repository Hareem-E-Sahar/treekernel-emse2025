public class Test {            public void actionPerformed(ActionEvent e) {
                ReportDescription[] rep = getAvailableReports();
                List<ReportDescription> descList = new ArrayList<ReportDescription>();
                for (int idx = 0; idx < rep.length; idx++) {
                    if (rep[idx].isInclude()) {
                        try {
                            IReport tr = rep[idx].getImpl();
                            if (tr.getRowCount() > 0) {
                                descList.add(rep[idx]);
                            }
                        } catch (InstantiationException e1) {
                        } catch (IllegalAccessException e1) {
                        }
                    }
                }
                rep = (ReportDescription[]) descList.toArray(new ReportDescription[descList.size()]);
                if (rep.length == 0) {
                    return;
                }
                ReportDescDialog rd = new ReportDescDialog(jtabbedpane);
                RefineryUtilities.centerFrameOnScreen(rd);
                rep = rd.show(rep);
                int cnt = 0;
                for (int idx = 0; idx < rep.length; idx++) {
                    if (rep[idx].isInclude()) {
                        cnt++;
                    }
                }
                if (rd.isCancel() || cnt == 0) {
                    return;
                }
                boolean combine = cnt == 1;
                if (cnt > 1) {
                    int value = JOptionPane.showConfirmDialog(jtabbedpane, "Do you want to save each report in a seperate file?", "Please Confirm", JOptionPane.YES_NO_OPTION);
                    combine = !(value == JOptionPane.YES_OPTION);
                }
                final JFileChooser fc = new JFileChooser();
                if (dataFile != null) {
                    if (dataFile.isFile()) {
                        fc.setSelectedFile(dataFile);
                    }
                }
                if (!combine && cnt > 1) {
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (dataFile != null) {
                        if (dataFile.isFile()) {
                            fc.setSelectedFile(dataFile.getParentFile());
                        } else {
                            fc.setSelectedFile(dataFile);
                        }
                    }
                }
                if (fc.showDialog(jtabbedpane, "Save Data") == JFileChooser.APPROVE_OPTION) {
                    dataFile = fc.getSelectedFile();
                    if (combine || cnt == 1) {
                        if (dataFile.isFile()) {
                            if (dataFile.exists()) {
                                int value = JOptionPane.showConfirmDialog(jtabbedpane, dataFile + " already exists.\n" + "Do you want to over write it?", "Please Confirm", JOptionPane.YES_NO_OPTION);
                                if (value != JOptionPane.YES_OPTION) {
                                    return;
                                }
                            }
                        }
                    } else {
                        if (!dataFile.exists()) {
                            int value = JOptionPane.showConfirmDialog(jtabbedpane, dataFile + " does not exists.\n" + "Do you want create it now?", "", JOptionPane.YES_NO_OPTION);
                            if (value != JOptionPane.YES_OPTION) {
                                return;
                            } else {
                                dataFile.mkdirs();
                            }
                        }
                    }
                    int value = JOptionPane.showConfirmDialog(jtabbedpane, "Format date as GMT?", "", JOptionPane.YES_NO_OPTION);
                    boolean gmt = value == JOptionPane.YES_OPTION;
                    if (cnt == 1 || combine) {
                        String data = getAnalysisData(gmt, rep);
                        OutputStream out = null;
                        try {
                            dataFile.getParentFile().mkdirs();
                            out = new FileOutputStream(dataFile);
                            out.write(data.getBytes());
                            out.flush();
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(jtabbedpane, e1.toString(), "An error occurred saving the data.", JOptionPane.ERROR_MESSAGE);
                        } finally {
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (Exception ex) {
                                }
                            }
                        }
                    } else {
                        saveDataToDir(dataFile, rep, gmt);
                    }
                }
            }
}
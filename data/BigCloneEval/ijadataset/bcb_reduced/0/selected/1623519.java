package org.paccman.ui.banks;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.paccman.controller.BankController;
import org.paccman.paccman.Bank;
import org.paccman.ui.form.BadInputException;
import org.paccman.ui.form.PaccmanForm;

/**
 *
 * @author  joao
 */
public class BankFormPanel extends PaccmanForm {

    /**
     * Creates new form BankFormPanel
     */
    public BankFormPanel() {
        initComponents();
        setEditMode(false);
    }

    public void setForm(org.paccman.controller.Controller controller) {
        Bank bank = ((BankController) controller).getBank();
        bankNameEdt.setText(bank.getName());
        agencyEdt.setText(bank.getAgency());
        addressLine1Edt.setText(bank.getAddressLine1());
        addressLine2Edt.setText(bank.getAddressLine2());
        addressNumberEdt.setText(bank.getAddressNumber());
        codeEdt.setText(bank.getAddressZip());
        cityEdt.setText(bank.getAddressCity());
        countryCmb.setSelectedItem(bank.getAddressCountry());
        phoneEdt.setText(bank.getTelephone());
        faxEdt.setText(bank.getFax());
        setInternetAddress(bank.getInternet());
        emailEdt.setText(bank.getEmail());
    }

    public void setEditMode(boolean editing) {
        bankNameEdt.setEnabled(editing);
        agencyEdt.setEnabled(editing);
        addressLine1Edt.setEnabled(editing);
        addressLine2Edt.setEnabled(editing);
        addressNumberEdt.setEnabled(editing);
        codeEdt.setEnabled(editing);
        cityEdt.setEnabled(editing);
        countryCmb.setEnabled(editing);
        phoneEdt.setEnabled(editing);
        faxEdt.setEnabled(editing);
        internetEdt.setEnabled(editing);
        goBtn.setEnabled(internetEdt.getText().length() != 0);
        emailEdt.setEnabled(editing);
    }

    public void registerToDocumentCtrl() {
    }

    public void clearForm() {
        bankNameEdt.setText("");
        agencyEdt.setText("");
        addressLine1Edt.setText("");
        addressLine2Edt.setText("");
        addressNumberEdt.setText("");
        codeEdt.setText("");
        cityEdt.setText("");
        countryCmb.setSelectedItem(null);
        phoneEdt.setText("");
        faxEdt.setText("");
        setInternetAddress("");
        emailEdt.setText("");
    }

    private void setInternetAddress(String address) {
        internetEdt.setText(address == null ? "" : address);
        goBtn.setEnabled((address != null) && (address.length() != 0));
    }

    public void getForm(org.paccman.controller.Controller controller) throws BadInputException {
        BankController bankCtrl = (BankController) controller;
        String name = bankNameEdt.getText();
        String agency = agencyEdt.getText();
        String number = addressNumberEdt.getText();
        String addressLine1 = addressLine1Edt.getText();
        String addressLine2 = addressLine2Edt.getText();
        String code = codeEdt.getText();
        String city = cityEdt.getText();
        String country = (String) countryCmb.getSelectedItem();
        String phone = phoneEdt.getText();
        String fax = faxEdt.getText();
        String email = emailEdt.getText();
        String internet = internetEdt.getText();
        bankCtrl.getBank().setName(name);
        bankCtrl.getBank().setAgency(agency);
        bankCtrl.getBank().setAddressNumber(number);
        bankCtrl.getBank().setAddressLine1(addressLine1);
        bankCtrl.getBank().setAddressLine2(addressLine2);
        bankCtrl.getBank().setAddressZip(code);
        bankCtrl.getBank().setAddressCity(city);
        bankCtrl.getBank().setAddressCountry(country);
        bankCtrl.getBank().setTelephone(phone);
        bankCtrl.getBank().setFax(fax);
        bankCtrl.getBank().setEmail(email);
        bankCtrl.getBank().setInternet(internet);
    }

    public org.paccman.controller.Controller getNewController() {
        return new BankController();
    }

    private void initComponents() {
        nameLbl = new javax.swing.JLabel();
        bankNameEdt = new javax.swing.JTextField();
        agencyLbl = new javax.swing.JLabel();
        agencyEdt = new javax.swing.JTextField();
        addressPanel = new javax.swing.JPanel();
        numberLbl = new javax.swing.JLabel();
        streetLbl = new javax.swing.JLabel();
        addressNumberEdt = new javax.swing.JTextField();
        addressLine1Edt = new javax.swing.JTextField();
        addressLine2Edt = new javax.swing.JTextField();
        codeLbl = new javax.swing.JLabel();
        codeEdt = new javax.swing.JTextField();
        cityLbl = new javax.swing.JLabel();
        cityEdt = new javax.swing.JTextField();
        countryLbl = new javax.swing.JLabel();
        countryCmb = new org.paccman.ui.common.CountryComboBox();
        numbersPanel = new javax.swing.JPanel();
        phoneLbl = new javax.swing.JLabel();
        phoneEdt = new javax.swing.JTextField();
        faxLbl = new javax.swing.JLabel();
        faxEdt = new javax.swing.JTextField();
        internetLbl = new javax.swing.JLabel();
        internetEdt = new javax.swing.JTextField();
        goBtn = new javax.swing.JButton();
        emailLbl = new javax.swing.JLabel();
        emailEdt = new javax.swing.JTextField();
        nameLbl.setText("Name");
        agencyLbl.setText("Agency");
        addressPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Address"));
        numberLbl.setText("Number");
        streetLbl.setText("Street");
        codeLbl.setText("Code");
        cityLbl.setText("City");
        countryLbl.setText("Country");
        javax.swing.GroupLayout addressPanelLayout = new javax.swing.GroupLayout(addressPanel);
        addressPanel.setLayout(addressPanelLayout);
        addressPanelLayout.setHorizontalGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(addressPanelLayout.createSequentialGroup().addContainerGap().addGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(codeLbl).addComponent(numberLbl).addComponent(countryLbl)).addGap(23, 23, 23).addGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(addressPanelLayout.createSequentialGroup().addGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(addressNumberEdt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(codeEdt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(streetLbl).addComponent(cityLbl)).addGap(4, 4, 4).addGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(cityEdt, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE).addComponent(addressLine2Edt, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE).addComponent(addressLine1Edt, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))).addComponent(countryCmb, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)).addContainerGap()));
        addressPanelLayout.setVerticalGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(addressPanelLayout.createSequentialGroup().addGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(numberLbl).addComponent(addressNumberEdt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(streetLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(addressLine1Edt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(addressLine2Edt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(codeEdt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(codeLbl).addComponent(cityLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(cityEdt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(countryCmb, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(countryLbl, javax.swing.GroupLayout.Alignment.TRAILING)).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        addressPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] { addressLine1Edt, addressLine2Edt, addressNumberEdt, cityEdt, cityLbl, codeEdt, codeLbl, countryCmb, countryLbl, numberLbl, streetLbl });
        numbersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Contact"));
        phoneLbl.setText("Telephone");
        faxLbl.setText("Fax");
        internetLbl.setText("Internet");
        goBtn.setText("Go");
        goBtn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goBtnActionPerformed(evt);
            }
        });
        emailLbl.setText("E-mail");
        javax.swing.GroupLayout numbersPanelLayout = new javax.swing.GroupLayout(numbersPanel);
        numbersPanel.setLayout(numbersPanelLayout);
        numbersPanelLayout.setHorizontalGroup(numbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(numbersPanelLayout.createSequentialGroup().addContainerGap().addGroup(numbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(emailLbl, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(faxLbl, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(internetLbl, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(phoneLbl, javax.swing.GroupLayout.Alignment.TRAILING)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(numbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(emailEdt, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE).addComponent(faxEdt, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE).addComponent(phoneEdt, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, numbersPanelLayout.createSequentialGroup().addComponent(internetEdt, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(goBtn))).addContainerGap()));
        numbersPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { emailLbl, faxLbl, internetLbl, phoneLbl });
        numbersPanelLayout.setVerticalGroup(numbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(numbersPanelLayout.createSequentialGroup().addGroup(numbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(phoneLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(phoneEdt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(numbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(faxEdt).addComponent(faxLbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(numbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(emailLbl).addComponent(emailEdt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(numbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(internetLbl).addComponent(goBtn, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(internetEdt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap()));
        numbersPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] { emailEdt, emailLbl, faxEdt, faxLbl, goBtn, internetEdt, internetLbl, phoneEdt, phoneLbl });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(numbersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(addressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup().addComponent(nameLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(bankNameEdt, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)).addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup().addComponent(agencyLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(agencyEdt, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(nameLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(bankNameEdt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(agencyLbl).addComponent(agencyEdt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(addressPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(numbersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(14, 14, 14)));
        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] { agencyEdt, agencyLbl, bankNameEdt, nameLbl });
    }

    private void goBtnActionPerformed(java.awt.event.ActionEvent evt) {
        Desktop desktop;
        if ((desktop = Desktop.getDesktop()).isDesktopSupported()) {
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                URI uri = null;
                try {
                    uri = new URI(internetEdt.getText());
                    desktop.browse(uri);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (URISyntaxException use) {
                    use.printStackTrace();
                }
            }
        }
    }

    private javax.swing.JTextField addressLine1Edt;

    private javax.swing.JTextField addressLine2Edt;

    private javax.swing.JTextField addressNumberEdt;

    private javax.swing.JPanel addressPanel;

    private javax.swing.JTextField agencyEdt;

    private javax.swing.JLabel agencyLbl;

    private javax.swing.JTextField bankNameEdt;

    private javax.swing.JTextField cityEdt;

    private javax.swing.JLabel cityLbl;

    private javax.swing.JTextField codeEdt;

    private javax.swing.JLabel codeLbl;

    private org.paccman.ui.common.CountryComboBox countryCmb;

    private javax.swing.JLabel countryLbl;

    private javax.swing.JTextField emailEdt;

    private javax.swing.JLabel emailLbl;

    private javax.swing.JTextField faxEdt;

    private javax.swing.JLabel faxLbl;

    private javax.swing.JButton goBtn;

    private javax.swing.JTextField internetEdt;

    private javax.swing.JLabel internetLbl;

    private javax.swing.JLabel nameLbl;

    private javax.swing.JLabel numberLbl;

    private javax.swing.JPanel numbersPanel;

    private javax.swing.JTextField phoneEdt;

    private javax.swing.JLabel phoneLbl;

    private javax.swing.JLabel streetLbl;
}

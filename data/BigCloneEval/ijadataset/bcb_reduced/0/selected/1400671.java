package org.web3d.x3d.actions.security;

import java.awt.Dialog;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.utils.XMLUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.web3d.x3d.BaseX3DEditAction;
import org.web3d.x3d.actions.conversions.ConversionsHelper;
import org.web3d.x3d.actions.security.ManageKeyStoreAction.OperationCancelledException;

@ActionID(id = "org.web3d.x3d.actions.security.EncryptDocumentAction", category = "Tools")
@ActionRegistration(displayName = "#CTL_EncryptDocumentAction")
@ActionReferences(value = { @ActionReference(path = "Menu/X3D/XML Security", position = 300), @ActionReference(path = "Editors/model/x3d+xml/Popup/XML Security", position = 300) })
public final class EncryptDocumentAction extends BaseX3DEditAction {

    private JFileChooser saveChooser;

    private JCheckBox openInEditorCB;

    @Override
    protected void performAction(Node[] activatedNodes) {
        BouncyCastleHelper.setup();
        super.performAction(activatedNodes);
    }

    @Override
    protected void doWork(Node[] activatedNodes) {
        try {
            DialogDescriptor descriptor;
            SelectKeyPanel keyPan;
            try {
                keyPan = BouncyCastleHelper.buildSelectKeyPanel(SelectKeyPanel.ENCRYPTING_KEY_TYPE);
                if (keyPan == null) return;
                descriptor = new DialogDescriptor(keyPan, NbBundle.getMessage(getClass(), "SelectEncryptKeyDialogTitle"));
            } catch (OperationCancelledException cex) {
                return;
            } catch (Exception ex) {
                String msg = "Keystore error: " + ex.getLocalizedMessage();
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                return;
            }
            Dialog dlg = null;
            try {
                dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.setResizable(true);
                dlg.pack();
                dlg.setVisible(true);
            } finally {
                if (dlg != null) dlg.dispose();
            }
            if (descriptor.getValue() == DialogDescriptor.CANCEL_OPTION) return;
            Document w3cDoc = getW3cDocument();
            NodeList nlist = w3cDoc.getElementsByTagName("X3D");
            Element w3cElem = (org.w3c.dom.Element) nlist.item(0);
            Document newdoc;
            Entry ent = keyPan.getSelectedEntry();
            if (ent instanceof KeyStore.SecretKeyEntry) {
                KeyStore.SecretKeyEntry secKeyEnt = (KeyStore.SecretKeyEntry) ent;
                org.apache.xml.security.Init.init();
                XMLCipher cipher = XMLCipher.getProviderInstance(XMLCipher.TRIPLEDES, "BC");
                cipher.init(XMLCipher.ENCRYPT_MODE, secKeyEnt.getSecretKey());
                newdoc = cipher.doFinal(w3cDoc, w3cElem);
            } else if (ent instanceof KeyStore.PrivateKeyEntry) {
                KeyStore.PrivateKeyEntry prKeyEnt = (KeyStore.PrivateKeyEntry) ent;
                org.apache.xml.security.Init.init();
                XMLCipher cipher = XMLCipher.getProviderInstance(XMLCipher.TRIPLEDES, "BC");
                cipher.init(XMLCipher.ENCRYPT_MODE, prKeyEnt.getCertificate().getPublicKey());
                newdoc = cipher.doFinal(w3cDoc, w3cElem);
            } else {
                throw new Exception(NbBundle.getMessage(getClass(), "MSG_SecretToEncrypt"));
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLUtils.outputDOMc14nWithComments(newdoc, baos);
            String xmlString = baos.toString("UTF-8");
            if (saveChooser == null) {
                saveChooser = new JFileChooser();
                saveChooser.setDialogTitle(NbBundle.getMessage(getClass(), "MSG_SaveEncryptedFileTitle"));
                saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                openInEditorCB = new JCheckBox(NbBundle.getMessage(getClass(), "MSG_OpenInEditor"));
                openInEditorCB.setSelected(true);
                saveChooser.setAccessory(openInEditorCB);
            }
            FileObject thisFo = x3dEditorSupport.getDataObject().getPrimaryFile();
            FileObject directory = thisFo.getParent();
            saveChooser.setCurrentDirectory(FileUtil.toFile(directory));
            String outputType = "xml";
            String outFileNm = FileUtil.findFreeFileName(directory, thisFo.getName(), outputType);
            saveChooser.setSelectedFile(new File(outFileNm + "." + outputType));
            if (saveChooser.showSaveDialog(null) == JFileChooser.CANCEL_OPTION) return;
            File outFile = saveChooser.getSelectedFile();
            FileWriter outFw = new FileWriter(outFile);
            outFw.write(xmlString);
            outFw.close();
            if (openInEditorCB.isSelected()) {
                ConversionsHelper.openInEditor(outFile);
            }
            InputOutput io = IOProvider.getDefault().getIO("Output", false);
            io.select();
            io.getOut().println(NbBundle.getMessage(getClass(), "MSG_EncryptOpComplete"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EncryptDocumentAction.class, "CTL_EncryptDocumentAction");
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] { DataObject.class };
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

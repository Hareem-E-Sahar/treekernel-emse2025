package bank.cnaps2.ccms.pageview;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import gneit.topbase.security.context.UserContextHolder;
import gneit.topbase.util.date.TopDateUtils;
import gneit.topface.api.ui.config.ViewConfig;
import gneit.topface.api.ui.view.PageViewConfiguration;
import gneit.topface.core.data.ViewDataset;
import gneit.topface.ui.component.TopButton;
import gneit.topface.ui.component.TopComboBox;
import gneit.topface.ui.component.TopForm;
import gneit.topface.ui.component.TopNotificationManager;
import gneit.topface.ui.component.TopTextEditor;
import gneit.topface.ui.component.TopTextLabel;
import gneit.topface.ui.component.TopUpload;
import gneit.topface.ui.component.layout.TopVFlowLayout;
import gneit.topface.ui.data.datamodule.DataModuleHolder;
import gneit.topface.ui.view.TopPageView;
import bank.cnaps2.common.tools.Tools;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.SucceededEvent;

@PageViewConfiguration(path = "classpath:bank/cnaps2/ccms/pageview/PageCIS.view")
public class PageCIS extends TopPageView {

    private static final long serialVersionUID = 1L;

    private static final int BUFFSIZE = 102400;

    private TopButton submitBtn;

    private TopForm inputForm;

    private TopTextEditor payerActnoEditor;

    private TopTextEditor payeeActnoEditor;

    private TopTextEditor payerAccBrnoEditor;

    private TopTextEditor payeeBrnoEditor;

    private TopTextEditor payeeAccBrnoEditor;

    private TopComboBox pkgTypeEditor;

    private TopComboBox bizTypeEditor;

    private boolean valueChangeflag = false;

    private ViewDataset inputDs;

    private TopTextLabel uploadLbl;

    private TopUpload uploadEditor;

    private TopComboBox inputTypeComb;

    private TopVFlowLayout uploadLt;

    public PageCIS(ViewConfig viewConfig) {
        super(viewConfig);
    }

    protected void afterInitControls() {
        super.afterInitControls();
        submitBtn = this.getControl("SubmitBtn", TopButton.class);
        inputForm = this.getControl("inputDatasetForm", TopForm.class);
        payeeActnoEditor = (TopTextEditor) inputForm.getField("PAYEE_ACTNO");
        payerActnoEditor = (TopTextEditor) inputForm.getField("PAYER_ACTNO");
        payerAccBrnoEditor = (TopTextEditor) this.getControl("inputDatasetForm", TopForm.class).getField("PAYER_ACC_BRNO");
        payeeBrnoEditor = (TopTextEditor) this.getControl("inputDatasetForm", TopForm.class).getField("PAYEE_BRNO");
        payeeAccBrnoEditor = (TopTextEditor) this.getControl("inputDatasetForm", TopForm.class).getField("PAYEE_ACC_BRNO");
        pkgTypeEditor = (TopComboBox) inputForm.getField("RT_STATU");
        bizTypeEditor = (TopComboBox) inputForm.getField("BIZ_TYPE_CODE");
        inputTypeComb = (TopComboBox) this.getControl("inputtypeForm", TopForm.class).getField("INPUT_TYPE");
        uploadLbl = this.getControl("uploadlbl", TopTextLabel.class);
        uploadEditor = (TopUpload) this.getControl("uploadeditor", TopUpload.class);
        inputDs = getDataset("inputDataset");
        uploadLt = this.getControl("uploadLt", TopVFlowLayout.class);
    }

    @Override
    protected void afterLoadData() {
        super.afterLoadData();
        Item current = this.getDataset("inputDataset").getCurrent();
        current.getItemProperty("BIZ_TYPE_CODE").setValue("A308");
        current.getItemProperty("BIZ_CTGY_CODE").setValue("05301");
        current.getItemProperty("CBFLAG").setValue("0");
        current.getItemProperty("PAYER_ACC_BRNO").setValue(UserContextHolder.getContext().getSys("cp2_brno"));
        current.getItemProperty("PAYER_ACC_BRNONAME").setValue(UserContextHolder.getContext().getSys("cp2_brname"));
        current.getItemProperty("PAYER_BRNO").setValue(UserContextHolder.getContext().getSys("cp2_brno"));
        current.getItemProperty("PAYER_BRNONAME").setValue(UserContextHolder.getContext().getSys("cp2_brname"));
        current.getItemProperty("CURCD").setValue("CNY");
        current.getItemProperty("INPUT_TYPE").setValue("0");
    }

    protected void bindEvent() {
        pkgTypeEditor.addListener(new TopComboBox.ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {
                Object rt_status = event.getProperty().getValue();
                if ("PR09".equals(rt_status)) {
                    inputForm.getField("REJECT_CODE").setReadOnly(false);
                    inputForm.getField("REJECT_CONTENT").setReadOnly(false);
                    inputForm.getField("REJECT_CODE").setRequired(true);
                    inputForm.getField("REJECT_CODE").setRequiredError("不能为空");
                    inputForm.getField("REJECT_CONTENT").setRequired(true);
                    inputForm.getField("REJECT_CONTENT").setRequiredError("不能为空");
                } else if ("PR02".equals(rt_status)) {
                    inputForm.getField("REJECT_CODE").setReadOnly(true);
                    inputForm.getField("REJECT_CONTENT").setReadOnly(true);
                    inputForm.getField("REJECT_CODE").setRequired(false);
                    inputForm.getField("REJECT_CONTENT").setRequired(false);
                }
                PageCIS.this.getDataset("inputDataset").getCurrent().getItemProperty("REJECT_CODE").setValue(null);
                PageCIS.this.getDataset("inputDataset").getCurrent().getItemProperty("REJECT_CONTENT").setValue("");
            }
        });
        inputTypeComb.addListener(new TopComboBox.ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {
                Object inputType = event.getProperty().getValue();
                if ("0".equals(inputType)) {
                    uploadLt.setVisible(true);
                } else if ("1".equals(inputType)) {
                    uploadLt.setVisible(false);
                }
            }
        });
        payerAccBrnoEditor.addListener(new TopTextEditor.ClickListener() {

            private static final long serialVersionUID = 6689373922136285130L;

            public void onClick(gneit.topface.ui.component.TopTextEditor.ClickEvent event) {
                if (event.isDoubleClick()) getCommand("refCommad").execute();
            }
        });
        payeeBrnoEditor.addListener(new TopTextEditor.ClickListener() {

            private static final long serialVersionUID = 2939706857440078609L;

            public void onClick(gneit.topface.ui.component.TopTextEditor.ClickEvent event) {
                if (event.isDoubleClick()) getCommand("refCommand2").execute();
            }
        });
        payeeAccBrnoEditor.addListener(new TopTextEditor.ClickListener() {

            private static final long serialVersionUID = 2939706857440078609L;

            public void onClick(gneit.topface.ui.component.TopTextEditor.ClickEvent event) {
                if (event.isDoubleClick()) getCommand("refCommand3").execute();
            }
        });
        payeeBrnoEditor.addListener(new Property.ValueChangeListener() {

            private static final long serialVersionUID = 4770965121817844780L;

            public void valueChange(ValueChangeEvent event) {
                Object payee_brno = event.getProperty().getValue();
                String inputType = (String) inputDs.getCurrentValue("INPUT_TYPE");
                Item inputss = getDataset("inputDataset").getCurrent();
                if (valueChangeflag == false) {
                    valueChangeflag = true;
                    if (!"".equals(payee_brno) && payee_brno != null) {
                        if (PageCIS.this.getCommand("7777_3_blur").execute().get("7777_3").getCallbackCount() == 0) {
                            if ("1".equals(inputType)) {
                                inputss.getItemProperty("PAYEE_BRNONAME").setValue("");
                                inputss.getItemProperty("PAYEE_ACC_BRNO").setValue("");
                                inputss.getItemProperty("PAYEE_ACC_BRNONAME").setValue("");
                            } else {
                                inputss.getItemProperty("PAYEE_BRNONAME").setValue("");
                            }
                        } else {
                            if ("1".equals(inputType)) {
                                inputss.getItemProperty("PAYEE_ACC_BRNO").setValue(inputss.getItemProperty("PAYEE_BRNO").getValue());
                                inputss.getItemProperty("PAYEE_ACC_BRNONAME").setValue(inputss.getItemProperty("PAYEE_BRNONAME").getValue());
                            }
                        }
                    } else {
                        if ("1".equals(inputType)) {
                            inputss.getItemProperty("PAYEE_BRNO").setValue("");
                            inputss.getItemProperty("PAYEE_BRNONAME").setValue("");
                            inputss.getItemProperty("PAYEE_ACC_BRNO").setValue("");
                            inputss.getItemProperty("PAYEE_ACC_BRNONAME").setValue("");
                        } else {
                            inputss.getItemProperty("PAYEE_BRNO").setValue("");
                            inputss.getItemProperty("PAYEE_BRNONAME").setValue("");
                        }
                    }
                    valueChangeflag = false;
                }
            }
        });
        submitBtn.addListener(new TopButton.ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent clickevent) {
                try {
                    inputForm.validate();
                } catch (InvalidValueException e) {
                    TopNotificationManager.error(PageCIS.this.getApplication().getMainWindow(), "", e.getMessage(), 2000);
                    return;
                } catch (Exception e) {
                    TopNotificationManager.error(PageCIS.this.getApplication().getMainWindow(), "", e.getMessage(), 2000);
                    return;
                }
                if ("0".equals(inputDs.getCurrentValue("INPUT_TYPE"))) {
                    if (inputDs.getCurrentValue("FILE_NAME") == null) {
                        Tools.alertWindows(PageCIS.this, "${bank.cnaps2.ccms.Page7106.beforeupload}");
                        return;
                    }
                }
                Tools.remarkSplit(PageCIS.this, "inputDataset", "REMARK", "REMARK2");
                Tools.remarkSplit(PageCIS.this, "inputDataset", "REMARKS", "REMARKS2");
                String datasetName = "inputDataset";
                String payerNameFieldName = "PAYER_NAME";
                String payeeNameFieldName = "PAYEE_NAME";
                String amountFieldName = "AMOUNT";
                String payerBrnoFieldName = "PAYER_BRNO";
                String payeeBrnoFieldName = "PAYEE_BRNO";
                String biztype = (String) inputDs.getCurrentValue("BIZ_TYPE_CODE");
                String rejectCode = (String) inputDs.getCurrentValue("REJECT_CODE");
                String rtstatus = (String) inputDs.getCurrentValue("RT_STATU");
                if ("PR09".equals(rtstatus)) {
                    if (rejectCode == null || "".equals(rejectCode)) {
                        Tools.alertWindows(PageCIS.this, "请至少选择一项退票代码");
                        return;
                    }
                }
                if (rejectCode != null) {
                    if (rejectCode.length() > 25) {
                        Tools.alertWindows(PageCIS.this, "${bank.cnaps2.REJECTCODE_MAXIS5}");
                        return;
                    }
                }
                int iRet;
                iRet = Tools.comPareBlackList_Actno(PageCIS.this, datasetName, payerNameFieldName, payeeNameFieldName, amountFieldName, payerBrnoFieldName, payeeBrnoFieldName, "commitevent", biztype, "hvps.130.001.01");
                if (0 != iRet) {
                    return;
                }
            }
        });
        uploadEditor.addListener(new TopUpload.SucceededListener() {

            public void uploadSucceeded(SucceededEvent event) {
                valueChangeflag = true;
                submitBtn.setEnabled(true);
                String fileName = event.getFilename();
                getDataset("inputDataset").getCurrent().getItemProperty("FILE_NAME").setValue(fileName);
                readFileByChars(uploadEditor.getPath() + fileName);
                uploadLbl.setReadOnly(true);
                uploadEditor.setReadOnly(true);
                Tools.alertWindows(PageCIS.this, "${bank.cnaps2.ccms.Page7106.uploadSucc}");
                valueChangeflag = false;
            }
        });
        payeeActnoEditor.addListener(new Property.ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {
                if (valueChangeflag == false) {
                    valueChangeflag = true;
                    Object payee_actnoValue = event.getProperty().getValue();
                    Item inputScur = getDataset("inputDataset").getCurrent();
                    if (inputScur.getItemProperty("PAYEE_ACTNO").getValue() != null) {
                        if (!"".equals(payee_actnoValue)) {
                            if (PageCIS.this.getCommand("payeeinfoCommand").execute().get("payeeinfocom").getTotalCount() == 0) {
                                inputScur.getItemProperty("PAYEE_NAME").setValue("");
                                inputScur.getItemProperty("PAYEE_BRNO").setValue("");
                                inputScur.getItemProperty("PAYEE_BRNONAME").setValue("");
                                inputScur.getItemProperty("PAYEE_ACC_BRNO").setValue("");
                                inputScur.getItemProperty("PAYEE_ACC_BRNONAME").setValue("");
                            }
                        } else {
                            inputScur.getItemProperty("PAYEE_NAME").setValue("");
                            inputScur.getItemProperty("PAYEE_BRNO").setValue("");
                            inputScur.getItemProperty("PAYEE_BRNONAME").setValue("");
                            inputScur.getItemProperty("PAYEE_ACC_BRNO").setValue("");
                            inputScur.getItemProperty("PAYEE_ACC_BRNONAME").setValue("");
                        }
                    }
                    valueChangeflag = false;
                }
            }
        });
        bizTypeEditor.addListener(new Property.ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {
                Item inputDsCurr = getDataset("inputDataset").getCurrent();
                Object bizTypeValue = event.getProperty().getValue();
                if ("A308".equals(bizTypeValue)) {
                    inputDsCurr.getItemProperty("BIZ_CTGY_CODE").setValue("05301");
                    ((TopComboBox) inputForm.getField("BIZ_CTGY_CODE")).setDropDownDataset(DataModuleHolder.getDataset("BIZ_CTGY_130_A308Dataset"));
                    ((TopComboBox) inputForm.getField("BIZ_CTGY_CODE")).bindProperty();
                } else if ("A309".equals(bizTypeValue)) {
                    inputDsCurr.getItemProperty("BIZ_CTGY_CODE").setValue("05302");
                    ((TopComboBox) inputForm.getField("BIZ_CTGY_CODE")).setDropDownDataset(DataModuleHolder.getDataset("BIZ_CTGY_130_A309Dataset"));
                    ((TopComboBox) inputForm.getField("BIZ_CTGY_CODE")).bindProperty();
                }
            }
        });
    }

    public void commitevent() {
        String rejectCode = (String) getDataset("inputDataset").getCurrentValue("REJECT_CODE");
        if (rejectCode != null) {
            String str = rejectCode.replaceAll(",", "");
            getDataset("inputDataset").setCurrentValue("REJECT_CODE1", str);
        }
        if (PageCIS.this.getCommand("submitCommand").execute().get("subcommand").hasException() == false) {
            inputForm.setFieldVisible("ID", true);
            submitBtn.setEnabled(false);
            inputForm.setReadOnly(true);
        }
    }

    private void readFileByChars(String fileName) {
        HashMap hmap = new HashMap();
        hmap.put(":D01:", "ORI_CIS_SEQNO");
        hmap.put(":30A:", "ORI_CIS_DATE");
        hmap.put(":CC5:", "PAYEE_ACC_BRNO");
        hmap.put(":59C:", "PAYEE_ACTNO");
        hmap.put(":59A:", "PAYEE_NAME");
        hmap.put(":D21:", "PAYER_ACC_BRNO");
        hmap.put(":D23:", "PAYER_ACTNO");
        hmap.put(":D22:", "PAYER_NAME");
        hmap.put(":32A:", "AMOUNT");
        hmap.put(":D01:", "ORI_CIS_NO");
        hmap.put(":0BC:", "ORI_CIS_SEQNO");
        int nRet = 0;
        File file = new File(fileName);
        Reader reader = null;
        try {
            CharBuffer tempchars = CharBuffer.allocate(BUFFSIZE);
            reader = new InputStreamReader(new FileInputStream(fileName));
            StringBuilder sb = new StringBuilder();
            while ((reader.read(tempchars)) != -1) {
                tempchars.flip();
                sb.append(tempchars.toString());
                nRet = tagFind(sb, hmap);
                if (nRet == -1) {
                    sb.delete(0, sb.length() - 5);
                } else if (nRet > 0) {
                    sb.delete(0, nRet - 5);
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    private int tagFind(StringBuilder sb, HashMap hmap) {
        Pattern pattag = Pattern.compile(":\\w{3}:");
        Matcher m = pattag.matcher(sb);
        int startpos = 0;
        int endpos = 0;
        String tagname = null;
        String tagfield = null;
        String sbcontent = null;
        if (m.find()) {
            startpos = m.start();
            endpos = m.end();
            tagname = m.group();
        }
        while (m.find()) {
            startpos = m.start();
            sbcontent = sb.substring(endpos, startpos);
            setFieldValue(hmap, tagname, sbcontent);
            endpos = m.end();
            tagname = m.group();
        }
        if (endpos == 0) {
            return 0;
        } else if ((startpos = sb.indexOf("}", endpos)) != -1) {
            setFieldValue(hmap, tagname, sb.substring(endpos, startpos));
            return -1;
        } else if ((startpos = sb.indexOf(":", endpos)) != -1) {
            setFieldValue(hmap, tagname, sb.substring(endpos, startpos));
            return -1;
        }
        return endpos;
    }

    private void setFieldValue(HashMap hmap, String tagname, String tagvalue) {
        Iterator iter;
        boolean changeflag = false;
        String tagfield = null;
        iter = hmap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (entry.getKey().equals(tagname)) {
                tagfield = (String) entry.getValue();
                hmap.remove(tagname);
                changeflag = true;
                break;
            }
        }
        if (changeflag) {
            if (":32A:".equals(tagname)) {
                inputDs.setCurrentValue(tagfield, Double.parseDouble(tagvalue.substring(3)) / 100);
            } else if (":30A:".equals(tagname)) {
                inputDs.setCurrentValue(tagfield, TopDateUtils.numberToDate(tagvalue));
            } else {
                inputDs.setCurrentValue(tagfield, tagvalue);
            }
        }
    }
}

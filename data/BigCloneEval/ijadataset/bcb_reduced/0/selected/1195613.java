package mipt.crec.lab.gui.startup.mdi.data;

import java.io.IOException;
import mipt.aaf.mdi.DocBuilderStub;
import mipt.aaf.mdi.DocController;
import mipt.aaf.mdi.DocModel;
import mipt.aaf.mdi.data.DataApplicationModel;
import mipt.aaf.mdi.data.DataDocModel;
import mipt.aaf.swing.mdi.MDICommandActionBuilder;
import mipt.common.Const;
import mipt.crec.lab.AbstractModule;
import mipt.crec.lab.DefaultEnvironment;
import mipt.crec.lab.Module;
import mipt.crec.lab.ModuleBuilder;
import mipt.crec.lab.ModulesDefinition;
import mipt.crec.lab.common.containers.data.MetaDataModulesDefinition;
import mipt.crec.lab.data.AbstractDataModuleBuilder;
import mipt.crec.lab.data.DataModule;
import mipt.crec.lab.data.DataTypes;
import mipt.crec.lab.gui.startup.mdi.LabBuilder;
import mipt.crec.lab.io.IO;
import mipt.data.Data;
import mipt.data.DataWrapper;
import mipt.data.event.DataModelAdapter;
import mipt.data.event.DataModelEvent;

/**
 * Builder for data-based docs.
 * If variant Data is not set to this object (in setDocInfo or in setVariantData()),
 *   it is loaded (by super) from  default file (it can be if empty lab is opening;
 *   default file name can be get from <labid>.xml file)
 * If lab Data is not set, it is often loaded from <labid>.xml file (by LabsApplicationModel)
 * Note: one of the Data mentioned should be set
 * All code from DataActionSwingDocBuilder is copied to this class
 *  but partId is Data itself, not its integer id
 * @author Evdokimov
 */
public class DataLabBuilder extends LabBuilder {

    protected Data docData;

    protected String labID;

    public DataLabBuilder(MDICommandActionBuilder actionBuilder) {
        super(actionBuilder);
    }

    /**
	 * @see mipt.aaf.mdi.AbstractDocBuilder#initDocController()
	 */
    protected DocController initDocController() {
        return new DocBuilderStub.ControllerStub();
    }

    /**
	 * @see mipt.aaf.mdi.AbstractDocBuilder#initDocModel()
	 */
    protected DocModel initDocModel() {
        LabDataDocModel model = new LabDataDocModel((DataApplicationModel) application.getModel());
        if (docData != null) model.setDocData(docData);
        return model;
    }

    /**
	 * @see mipt.crec.lab.gui.startup.mdi.LabBuilder#initTopModule(mipt.crec.lab.Module)
	 */
    protected void initTopModule(Module module) {
        super.initTopModule(module);
        AbstractDataModuleBuilder.link(((DataModule) module).getDataModel(), new DataModelAdapter() {

            public void dataChanged(DataModelEvent e) {
                ((LabDataDocModel) getDoc().getDocModel()).fireChanged();
            }
        });
    }

    /**
	 * 
	 */
    protected String getLabClassName() {
        Data lab = getDocParent();
        String name = lab == null ? null : lab.getString(DataTypes.LAB_builderClass);
        if (name != null) return name;
        return super.getLabClassName();
    }

    /**
	 * @see mipt.crec.lab.gui.startup.mdi.LabBuilder#initModuleBuilder(java.lang.String)
	 */
    protected ModuleBuilder initModuleBuilder(String className) throws Exception {
        Data lab = getDocParent();
        String file = lab == null ? null : lab.getString(DataTypes.LAB_builderFile);
        if (file == null) return super.initModuleBuilder(className);
        Class cls = Class.forName(className);
        Object arg = new MetaDataModulesDefinition(DefaultEnvironment.getTop().getModuleDirectory() + Const.fileSep + lab.getString(DataTypes.LAB_id) + Const.fileSep + file);
        try {
            return (ModuleBuilder) cls.getConstructor(new Class[] { ModulesDefinition.class }).newInstance(new Object[] { arg });
        } catch (Exception e) {
            return super.initModuleBuilder(className);
        }
    }

    /**
	 * @see mipt.crec.lab.gui.startup.mdi.LabBuilder#getVariantFile(mipt.crec.lab.Module)
	 */
    protected String getVariantFile(Module module) {
        String file = getDocParent().getString(DataTypes.LAB_defaultVariant);
        if (file != null) return file;
        return AbstractModule.DEFAULT_VARIANT;
    }

    /**
	 * Returns LAB (getDocData() - VARIANT)
	 */
    protected final Data getDocParent() {
        Object d = docData == null ? labID : docData;
        Data lab = getLabsApplicationModel().getDocParent(d);
        if (lab != null) return lab;
        try {
            lab = (Data) IO.getIO().load(DefaultEnvironment.getTop(), IO.applySuffix(getLabFileName(getLabId())));
            setLabData(lab);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return lab;
    }

    protected String getLabId() {
        return docData == null ? labID : docData.getString(DataTypes.VARIANT_labId);
    }

    protected String getLabFileName(String labId) {
        return labId + ".xml";
    }

    /**
	 * @see mipt.crec.lab.gui.startup.mdi.LabBuilder#getModuleID(mipt.crec.lab.Module)
	 */
    protected String getModuleID(Module module) {
        return getLabId();
    }

    /**
	 * @see mipt.crec.lab.gui.startup.mdi.LabBuilder#setVariant(mipt.crec.lab.Module)
	 */
    protected void setVariant(Module module) {
        if (docData == null) super.setVariant(module); else ((DataModule) module).setData((DataWrapper) docData);
    }

    /**
	 * @see mipt.aaf.mdi.AbstractDocBuilder#getPartId(java.lang.Object)
	 */
    public Object getPartId(Object docData) {
        return docData;
    }

    /**
	 * @see mipt.aaf.mdi.AbstractDocBuilder#setDocId()
	 */
    protected void setDocId() {
        getDoc().getPart().setId(getPartId(docData));
        DataDocModel model = (DataDocModel) getDoc().getDocModel();
        if (model.getDocData() != null) model.setDocData(docData);
    }

    /**
	 * @see mipt.aaf.mdi.DocBuilder#setDocInfo(java.lang.Object)
	 */
    public void setDocInfo(Object docInfo) {
        if (docInfo instanceof Data) setVariantData((Data) docInfo);
    }

    /**
	 * 
	 */
    public void setVariantData(Data data) {
        this.docData = data;
    }

    /**
	 *
	 */
    public void setLabData(Data data) {
        getLabsApplicationModel().setLabData(data);
        this.labID = data.getString(DataTypes.LAB_id);
    }

    protected final LabsApplicationModel getLabsApplicationModel() {
        return (LabsApplicationModel) getDoc().getMDIApplication().getModel();
    }
}

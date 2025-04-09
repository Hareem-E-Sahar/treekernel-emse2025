package audit.common;

import java.util.HashMap;
import java.lang.reflect.Constructor;
import javax.swing.JFrame;
import snaq.db.ConnectionPool;

/**
 * --------------------------------------------------------------------------
 * type: audit.common.DialogManager.java<br>
 * date: 13.7.2007<br>
 * info: trida obsluhujici dialogy aplikace<br>
 * @author Michal Rost
 * --------------------------------------------------------------------------
 */
public class DialogManager {

    protected ConnectionPool pool;

    protected HashMap<Integer, DialogData> map;

    /**
   * ========================================================================
   * DialogManager - konstruktor
   * @param pool
   * ========================================================================
   */
    public DialogManager(ConnectionPool pool) {
        this.pool = pool;
        init();
    }

    /**
   * ========================================================================
   * init - inicializace
   * ========================================================================
   */
    protected void init() {
        map = new HashMap<Integer, DialogData>();
    }

    /**
   * ========================================================================
   * addDialogType - prida novy typ dialogu do registru
   * @param dialogType
   * @param dialogClass
   * @param editAct
   * @param editSaveAct
   * ========================================================================
   */
    public void addDialogType(int dialogType, Class dialogClass, EditAct editAct, EditSaveAct editSaveAct) {
        DialogData tmp = new DialogData(dialogClass, editAct, editSaveAct);
        map.put(dialogType, tmp);
    }

    /**
   * ========================================================================
   * update - updatuje databazovy pool manazeru
   * @param pool
   * ========================================================================
   */
    public void update(ConnectionPool pool) {
        this.pool = pool;
    }

    /**
   * ========================================================================
   * showDialog - vytvori a zobrazi instanci daneho typu dialogu
   * @param dialogType
   * @param activeUser
   * @param itemId
   * @param parent
   * @return true pokud byl ulozen zaznam do db, false pokud nikoliv
   * @throws Exception
   * ========================================================================
   */
    public boolean showDialog(int dialogType, int activeUser, int itemId, JFrame parent) throws Exception {
        DialogData tmp = map.get(dialogType);
        Object frm = tmp.getEditAct().execute(pool, activeUser, itemId);
        Constructor dialogCon = tmp.getDialogClass().getConstructor(JFrame.class, DialogManager.class);
        EditDialog dialog = (EditDialog) dialogCon.newInstance(parent, this);
        frm = dialog.initAndShow(frm);
        return tmp.getEditSaveAct().execute(pool, frm);
    }

    /**
   * ========================================================================
   * getDialogForm - vybere zvoleny form bean pro zvoleny dialog
   * @param dialogType
   * @param activeUser
   * @param itemId
   * @return formularovy bean
   * @throws Exception
   * ========================================================================
   */
    public Object getDialogForm(int dialogType, int activeUser, int itemId) throws Exception {
        return map.get(dialogType).getEditAct().execute(pool, activeUser, itemId);
    }

    public class DialogData {

        protected Class dialogClass;

        protected EditAct editAct;

        protected EditSaveAct editSaveAct;

        public DialogData(Class dialogClass, EditAct editAct, EditSaveAct editSaveAct) {
            super();
            this.dialogClass = dialogClass;
            this.editAct = editAct;
            this.editSaveAct = editSaveAct;
        }

        public Class<?> getDialogClass() {
            return dialogClass;
        }

        public void setDialogClass(Class dialogClass) {
            this.dialogClass = dialogClass;
        }

        public EditAct getEditAct() {
            return editAct;
        }

        public void setEditAct(EditAct editAct) {
            this.editAct = editAct;
        }

        public EditSaveAct getEditSaveAct() {
            return editSaveAct;
        }

        public void setEditSaveAct(EditSaveAct editSaveAct) {
            this.editSaveAct = editSaveAct;
        }
    }
}

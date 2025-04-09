package no.ugland.utransprod.util;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.filechooser.FileFilter;
import no.ugland.utransprod.ProTransRuntimeException;
import no.ugland.utransprod.gui.FrontProductionWindow;
import no.ugland.utransprod.gui.GavlProductionWindow;
import no.ugland.utransprod.gui.JDialogAdapter;
import no.ugland.utransprod.gui.JFrameAdapter;
import no.ugland.utransprod.gui.JInternalFrameAdapter;
import no.ugland.utransprod.gui.Login;
import no.ugland.utransprod.gui.ProTransMain;
import no.ugland.utransprod.gui.VeggProductionWindow;
import no.ugland.utransprod.gui.WindowInterface;
import no.ugland.utransprod.gui.action.SetProductionUnitActionFactory;
import no.ugland.utransprod.gui.checker.DefaultProductionStatusChecker;
import no.ugland.utransprod.gui.checker.GulvsponStatusChecker;
import no.ugland.utransprod.gui.checker.OnStorageStatusChecker;
import no.ugland.utransprod.gui.checker.StatusCheckerInterface;
import no.ugland.utransprod.gui.checker.TaksteinAttributeStatusChecker;
import no.ugland.utransprod.gui.checker.TakstolStatusChecker;
import no.ugland.utransprod.gui.edit.EditViewable;
import no.ugland.utransprod.gui.handlers.AbstractProductionPackageViewHandler;
import no.ugland.utransprod.gui.handlers.DeviationViewHandlerFactory;
import no.ugland.utransprod.gui.handlers.OrderViewHandlerFactory;
import no.ugland.utransprod.gui.handlers.PackageViewHandler;
import no.ugland.utransprod.gui.handlers.PacklistViewHandler;
import no.ugland.utransprod.gui.handlers.ShowTakstolInfoActionFactory;
import no.ugland.utransprod.gui.handlers.TableEnum;
import no.ugland.utransprod.gui.handlers.TakstolPackageViewHandler;
import no.ugland.utransprod.gui.handlers.TakstolProductionViewHandler;
import no.ugland.utransprod.gui.model.GulvsponPackageApplyList;
import no.ugland.utransprod.gui.model.PacklistApplyList;
import no.ugland.utransprod.gui.model.ProductAreaGroupModel;
import no.ugland.utransprod.gui.model.TaksteinPackageApplyList;
import no.ugland.utransprod.gui.model.TakstolPackageApplyList;
import no.ugland.utransprod.gui.model.TakstolProductionApplyList;
import no.ugland.utransprod.gui.model.Transportable;
import no.ugland.utransprod.model.ApplicationUser;
import no.ugland.utransprod.model.ArticleType;
import no.ugland.utransprod.model.CostType;
import no.ugland.utransprod.model.CostUnit;
import no.ugland.utransprod.model.EmployeeType;
import no.ugland.utransprod.model.JobFunction;
import no.ugland.utransprod.model.OrderCost;
import no.ugland.utransprod.model.OrderLine;
import no.ugland.utransprod.model.ProductArea;
import no.ugland.utransprod.model.ProductAreaGroup;
import no.ugland.utransprod.model.Transport;
import no.ugland.utransprod.model.UserProductAreaGroup;
import no.ugland.utransprod.model.UserType;
import no.ugland.utransprod.service.ApplicationUserManager;
import no.ugland.utransprod.service.ArticleTypeManager;
import no.ugland.utransprod.service.EmployeeTypeManager;
import no.ugland.utransprod.service.GulvsponPackageVManager;
import no.ugland.utransprod.service.JobFunctionManager;
import no.ugland.utransprod.service.ManagerRepository;
import no.ugland.utransprod.service.OrderLineManager;
import no.ugland.utransprod.service.PacklistVManager;
import no.ugland.utransprod.service.ProductAreaGroupManager;
import no.ugland.utransprod.service.ProductAreaManager;
import no.ugland.utransprod.service.VismaFileCreator;
import no.ugland.utransprod.service.enums.LazyLoadEnum;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.SortOrder;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.toedter.calendar.JYearChooser;

/**
 * Hjelpeklasse.
 * 
 * @author atle.brekka
 */
public final class Util {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_hh_mm");

    public static final SimpleDateFormat DATE_FORMAT_SECONDS = new SimpleDateFormat("yyyyMMdd_hh_mm_ss");

    public static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");

    public static final SimpleDateFormat DATE_FORMAT_YYMMDD = new SimpleDateFormat("yyMMdd");

    public static final SimpleDateFormat DATE_FORMAT_YYYYWW = new SimpleDateFormat("yyyyww");

    public static final SimpleDateFormat DATE_FORMAT_YYYYMMDD = new SimpleDateFormat("yyyyMMdd");

    private static String sendesGgAttributeName;

    private static String steinArticleName;

    private static String gavlArticleName;

    private static String gulvsponArticleName;

    private static ArticleType gavlArticleType;

    private static ArticleType gulvsponArticleType;

    private static String takstolArticleName;

    private static String takstolColliName;

    private static ArticleType takstolArticleType;

    private static String frontArticleName;

    private static String veggArticleName;

    private static List<String> yesNoList;

    private static List<ProductArea> productAreaList;

    private static List<ProductAreaGroup> productAreaGroupList;

    private static ResourceBundle guiResources;

    private static File lastFileDir;

    private static List<JobFunction> jobFunctionList;

    private static List<EmployeeType> employeeTypeList;

    private static List<String> firstNameList;

    private static List<String> lastNameList;

    private static List<ArticleType> articleTypeList;

    private static String fileDirectory;

    private static JobFunctionManager jobFunctionManager;

    private static ProductAreaGroupManager productAreaGroupManager;

    private static ArticleTypeManager articleTypeManager;

    public Util() {
    }

    public static void setArticleTypeManager(ArticleTypeManager articleTypeManager) {
        Util.articleTypeManager = articleTypeManager;
    }

    public static void setJobFunctionManager(JobFunctionManager jobFunctionManager) {
        Util.jobFunctionManager = jobFunctionManager;
    }

    public static void setProductAreaGroupManager(ProductAreaGroupManager productAreaGroupManager) {
        Util.productAreaGroupManager = productAreaGroupManager;
    }

    /**
	 * Henter alle uker i et �r.
	 * 
	 * @return uker
	 */
    public static Object[] getWeeks() {
        Object[] weeks = new Object[53];
        for (int i = 0; i <= 52; i++) {
            weeks[i] = i + 1;
        }
        return weeks;
    }

    /**
	 * Henter gjeldende uke.
	 * 
	 * @return uke
	 */
    public static Integer getCurrentWeek() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    /**
	 * Henter gjeldende �r.
	 * 
	 * @return �r
	 */
    public static Integer getCurrentYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }

    public static String getCurrentYearWeekAsString() {
        Integer year = getCurrentYear();
        Integer week = getCurrentWeek();
        return String.format("%1$d%2$02d", year, week);
    }

    /**
	 * Lokaliserer komponent sentralt p� skjerm.
	 * 
	 * @param component
	 */
    public static void locateOnScreenCenter(final Component component) {
        Dimension paneSize = component.getSize();
        Dimension screenSize = component.getToolkit().getScreenSize();
        component.setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);
    }

    /**
	 * Lokaliserer komponent sentralt p� skjerm.
	 * 
	 * @param window
	 */
    public static void locateOnScreenCenter(final WindowInterface window) {
        Dimension paneSize = window.getSize();
        Dimension screenSize = window.getToolkit().getScreenSize();
        window.setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);
    }

    /**
	 * setter ventecursor.
	 * 
	 * @param window
	 */
    public static void setWaitCursor(final Component comp) {
        if (comp == null) {
            return;
        }
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Toolkit.getDefaultToolkit().sync();
    }

    public static void setWaitCursor(final WindowInterface window) {
        setWaitCursor(window.getComponent());
    }

    /**
	 * Setter default cursor.
	 * 
	 * @param window
	 */
    public static void setDefaultCursor(final Component comp) {
        if (comp == null) {
            return;
        }
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public static void setDefaultCursor(final WindowInterface window) {
        setDefaultCursor(window.getComponent());
    }

    /**
	 * Finner senter basert p� hovedvindu og vindu som skal vise.
	 * 
	 * @param mainFrameSize
	 * @param frameSize
	 * @return senter
	 */
    public static Point getCenter(final Dimension mainFrameSize, final Dimension frameSize) {
        if (frameSize.height > mainFrameSize.height) {
            frameSize.height = mainFrameSize.height;
        }
        if (frameSize.width > mainFrameSize.width) {
            frameSize.width = mainFrameSize.width;
        }
        Point tmpPoint = new Point((mainFrameSize.width - frameSize.width) / 2, (mainFrameSize.height - frameSize.height) / 2);
        return tmpPoint;
    }

    /**
	 * Viser vindu med melding som m� bekreftes.
	 * 
	 * @param askedBy
	 * @param heading
	 * @param msg
	 * @return true dersom ja
	 */
    public static boolean showConfirmFrame(final WindowInterface askedBy, final String heading, final String msg) {
        return showOptionFrame(askedBy.getComponent(), heading, msg, JOptionPane.QUESTION_MESSAGE);
    }

    /**
	 * Viser dialog som det m� svares p�.
	 * 
	 * @param askedBy
	 * @param heading
	 * @param msg
	 * @param msgType
	 * @return true dersom ja
	 */
    private static boolean showOptionFrame(final Component askedBy, final String heading, final String msg, final int msgType) {
        askedBy.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        Object[] options = new Object[] { "Ja", "Nei" };
        int answer = JOptionPane.showInternalOptionDialog(askedBy, msg, heading, JOptionPane.YES_NO_OPTION, msgType, null, options, options[0]);
        if (answer != JOptionPane.YES_OPTION) {
            return false;
        }
        return true;
    }

    /**
	 * Viser feilvindu.
	 * 
	 * @param askedBy
	 * @param heading
	 * @param message
	 */
    public static void showErrorMsgFrame(final Component askedBy, final String heading, final String message) {
        askedBy.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        JOptionPane.showInternalMessageDialog(askedBy, message, heading, JOptionPane.ERROR_MESSAGE);
    }

    /**
	 * Viser feildialog.
	 * 
	 * @param askedBy
	 * @param title
	 * @param message
	 */
    public static void showErrorDialog(final Component askedBy, final String title, final String message) {
        JOptionPane.showMessageDialog(askedBy, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
	 * Viser feildialog.
	 * 
	 * @param askedBy
	 * @param title
	 * @param message
	 */
    public static void showErrorDialog(final WindowInterface askedBy, final String title, final String message) {
        Component comp = null;
        if (askedBy != null) {
            comp = askedBy.getComponent();
        }
        showErrorDialog(comp, title, message);
    }

    public static boolean showConfirmDialog(final WindowInterface window, final String title, final String message) {
        return showConfirmDialog(window != null ? window.getComponent() : null, title, message);
    }

    /**
	 * Viser dialog det m� svares p�.
	 * 
	 * @param askedBy
	 * @param title
	 * @param message
	 * @return true dersom ja
	 */
    public static boolean showConfirmDialog(final Component askedBy, final String title, final String message) {
        Object[] options = { "Ja", "Nei" };
        int result = JOptionPane.showOptionDialog(askedBy, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "Ja");
        if (result == JOptionPane.OK_OPTION) {
            return true;
        }
        return false;
    }

    /**
	 * Viser melding.
	 * 
	 * @param askedBy
	 * @param heading
	 * @param message
	 */
    public static void showMsgFrame(final Component askedBy, final String heading, final String message) {
        if (askedBy != null) {
            askedBy.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        JOptionPane.showInternalMessageDialog(askedBy, message, heading, JOptionPane.INFORMATION_MESSAGE, no.ugland.utransprod.gui.IconEnum.ICON_UGLAND.getIcon());
    }

    /**
	 * Viser meldingsdialog.
	 * 
	 * @param askedBy
	 * @param heading
	 * @param message
	 */
    public static void showMsgDialog(final Component askedBy, final String heading, final String message) {
        JOptionPane.showMessageDialog(askedBy, message, heading, JOptionPane.INFORMATION_MESSAGE, no.ugland.utransprod.gui.IconEnum.ICON_UGLAND.getIcon());
    }

    /**
	 * Konverterer tall til boolsk verdi.
	 * 
	 * @param boolInteger
	 * @return false derom null eller 0
	 */
    public static boolean convertNumberToBoolean(final Integer boolInteger) {
        if (boolInteger == null || boolInteger == 0) {
            return false;
        }
        return true;
    }

    /**
	 * Konverterer boolsk verdi til tall.
	 * 
	 * @param bool
	 * @return 1 deroms true eller 0
	 */
    public static Integer convertBooleanToNumber(final Boolean bool) {
        if (bool != null && bool) {
            return 1;
        }
        return 0;
    }

    /**
	 * Finner differanse mellom to lister.
	 * 
	 * @param col1
	 * @param col2
	 * @return differanse
	 */
    @SuppressWarnings("unchecked")
    public static List getDiff(final Collection col1, final Collection col2) {
        ArrayList<Object> returnList = new ArrayList<Object>();
        if (col1 != null && col2 != null) {
            for (Object object : col1) {
                if (!col2.contains(object)) {
                    returnList.add(object);
                }
            }
        }
        return returnList;
    }

    /**
	 * Returnerer 1000 dersom tall er null.
	 * 
	 * @param integer
	 * @return 100 dersom tall er null ellers tallet
	 */
    public static Integer nullToInteger1000(final Integer integer) {
        if (integer == null) {
            return Integer.valueOf(1000);
        }
        return integer;
    }

    /**
	 * Returnerere tom streng dersom tall er null eller tallet selv.
	 * 
	 * @param integer
	 * @return streng
	 */
    public static String nullIntegerToString(final Integer integer) {
        if (integer == null) {
            return "0";
        }
        return integer.toString();
    }

    public static String convertIntegerToString(final Integer integer) {
        if (integer != null) {
            return integer.toString();
        }
        return "";
    }

    /**
	 * Kj�rerer metode i egen tr�d og viser et hjul for fremdrift.
	 * 
	 * @param component
	 * @param loadClass
	 * @param params
	 */
    public static void runInThreadWheel(final JRootPane component, final Threadable loadClass, final Object[] params) {
        loadClass.enableComponents(false);
        final InfiniteProgressPanel pane = new InfiniteProgressPanel();
        final JLabel infoLabel = new JLabel();
        PropertyConnector.connect(pane, "text", infoLabel, "text");
        if (component != null) {
            component.setGlassPane(pane);
            component.revalidate();
        }
        SwingWorker worker = new SwingWorker() {

            @Override
            public Object construct() {
                pane.start();
                return loadClass.doWork(params, infoLabel);
            }

            @Override
            public void finished() {
                pane.stop();
                loadClass.enableComponents(true);
                loadClass.doWhenFinished(this.get());
            }
        };
        worker.start();
    }

    /**
	 * Returnerer tom streng dersom streng er null.
	 * 
	 * @param string
	 * @return streng
	 */
    public static String nullToString(final String string) {
        if (string != null) {
            return string;
        }
        return "";
    }

    /**
	 * Henter dagens dato som streng.
	 * 
	 * @return dato som streng
	 */
    public static String getCurrentDateAsDateTimeString() {
        return DATE_FORMAT.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDateAsDateTimeStringWithSeconds() {
        return DATE_FORMAT_SECONDS.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDateAsDateString() {
        return SHORT_DATE_FORMAT.format(Calendar.getInstance().getTime());
    }

    public static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    /**
	 * Hemter short date.
	 * 
	 * @param date
	 * @return date
	 */
    public static Date getShortDate(final Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
        return date;
    }

    public static Date getShortDateLast(final Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 24);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
        return date;
    }

    /**
	 * Henter ja/nei liste.
	 * 
	 * @return liste
	 */
    public static List<String> getYesNoList() {
        if (yesNoList == null) {
            String[] yesNoArray = new String[] { "Ja", "Nei" };
            yesNoList = new ArrayList<String>(Arrays.asList(yesNoArray));
        }
        return yesNoList;
    }

    public static String showInputDialog(final WindowInterface window, final String heading, final String labelText) {
        OptionsPaneView optionsPaneView = new OptionsPaneView(null, true, false, false, null, labelText, true);
        showOptionsPane(window, heading, optionsPaneView);
        return optionsPaneView.getInputText();
    }

    public static Collection<?> showOptionsDialog(final WindowInterface window, final Collection<?> objects, final String heading, final boolean useOkButton, final boolean checkBoxAll) {
        OptionsPaneView optionsPaneView = new OptionsPaneView(objects, useOkButton, checkBoxAll, false, null, null, false);
        showOptionsPane(window, heading, optionsPaneView);
        return optionsPaneView.getSelectedObjects();
    }

    public static Object showOptionsDialogCombo(final WindowInterface window, final Collection<?> objects, final String heading, final boolean useOkButton, final Object defaultObject) {
        OptionsPaneView optionsPaneView = new OptionsPaneView(objects, useOkButton, false, true, defaultObject, null, false);
        showOptionsPane(window, heading, optionsPaneView);
        return optionsPaneView.getSelectedObject();
    }

    private static void showOptionsPane(final WindowInterface window, final String heading, final OptionsPaneView optionsPaneView) {
        JDialog dialog = getDialog(window, heading, true);
        dialog.setName(heading);
        WindowInterface dialogWindow = new JDialogAdapter(dialog);
        dialog.add(optionsPaneView.buildPanel(dialogWindow));
        dialog.pack();
        locateOnScreenCenter(dialogWindow);
        dialog.setVisible(true);
    }

    public static JDialog getDialog(final WindowInterface window, final String heading, final boolean modal) {
        JDialog dialog = null;
        if (window == null || window instanceof JInternalFrameAdapter) {
            dialog = new JDialog(ProTransMain.PRO_TRANS_MAIN, heading, modal);
        } else if (window instanceof JFrameAdapter) {
            dialog = new JDialog((JFrame) window.getComponent(), heading, modal);
        } else if (window instanceof JDialogAdapter) {
            dialog = new JDialog((JDialog) window.getComponent(), heading, modal);
        }
        return dialog;
    }

    public static Date getDate(final WindowInterface window) {
        JDialog dialog = null;
        if (window instanceof JFrameAdapter) {
            dialog = new JDialog((JFrame) window.getComponent(), "Velg dato", true);
        } else if (window instanceof JDialogAdapter) {
            dialog = new JDialog((JDialog) window.getComponent(), "Velg dato", true);
        } else if (window instanceof JInternalFrameAdapter) {
            dialog = new JDialog(ProTransMain.PRO_TRANS_MAIN, "Velg dato", true);
        }
        if (dialog != null) {
            WindowInterface dialogWindow = new JDialogAdapter(dialog);
            DateView dateView = new DateView();
            dialog.add(dateView.buildPanel(dialogWindow));
            dialog.pack();
            locateOnScreenCenter(dialog);
            dialog.setVisible(true);
            return dateView.getDate();
        }
        return null;
    }

    public static Map<String, String> createStatusMap(final String status) {
        Map<String, String> statusMap = new Hashtable<String, String>();
        if (status != null) {
            String[] statuses = status.split("\\$");
            if (statuses != null && statuses.length != 0) {
                List<String> statusStrings = Arrays.asList(statuses);
                String[] orderLineStatus;
                for (String statusString : statusStrings) {
                    orderLineStatus = statusString.split(";");
                    if (orderLineStatus != null && orderLineStatus.length != 0) {
                        if (orderLineStatus.length == 2) {
                            statusMap.put(orderLineStatus[0], orderLineStatus[1]);
                        } else {
                            statusMap.put(orderLineStatus[0], "");
                        }
                    }
                }
            }
        }
        return statusMap;
    }

    public static String statusMapToString(final Map<String, String> statusMap) {
        StringBuffer buffer = new StringBuffer();
        if (statusMap != null) {
            Set<String> keys = statusMap.keySet();
            for (String key : keys) {
                buffer.append("$").append(key).append(";").append(statusMap.get(key));
            }
            buffer.deleteCharAt(0);
        }
        return buffer.toString();
    }

    public static Map<String, StatusCheckerInterface<Transportable>> getStatusCheckersTransport(ManagerRepository managerRepository) {
        Map<String, StatusCheckerInterface<Transportable>> statusChekers = new Hashtable<String, StatusCheckerInterface<Transportable>>();
        if (gavlArticleName == null) {
            gavlArticleName = ApplicationParamUtil.findParamByName("gavl_artikkel");
        }
        if (gulvsponArticleName == null) {
            gulvsponArticleName = ApplicationParamUtil.findParamByName("gulvspon_artikkel");
        }
        if (gavlArticleType == null) {
            gavlArticleType = managerRepository.getArticleTypeManager().findByName(gavlArticleName);
        }
        if (gulvsponArticleType == null) {
            gulvsponArticleType = managerRepository.getArticleTypeManager().findByName(gulvsponArticleName);
        }
        statusChekers.put("Gavl", new DefaultProductionStatusChecker(gavlArticleType));
        StatusCheckerInterface<Transportable> takstolChecker = getTakstolChecker(managerRepository);
        statusChekers.put("Takstol", takstolChecker);
        StatusCheckerInterface<Transportable> steinChecker = getSteinChecker();
        statusChekers.put("Stein", steinChecker);
        statusChekers.put("Gulvspon", new GulvsponStatusChecker(gulvsponArticleType));
        return statusChekers;
    }

    public static String getGavlArticleName() {
        if (gavlArticleName == null) {
            gavlArticleName = ApplicationParamUtil.findParamByName("gavl_artikkel");
        }
        return gavlArticleName;
    }

    public static String getGulvsponArticleName() {
        if (gulvsponArticleName == null) {
            gulvsponArticleName = ApplicationParamUtil.findParamByName("gulvspon_artikkel");
        }
        return gulvsponArticleName;
    }

    public static String getTakstolArticleName() {
        if (takstolArticleName == null) {
            takstolArticleName = ApplicationParamUtil.findParamByName("takstol_artikkel");
        }
        return takstolArticleName;
    }

    public static String getTakstolColliName() {
        if (takstolColliName == null) {
            takstolColliName = ApplicationParamUtil.findParamByName("takstol_kollnavn");
        }
        return takstolColliName;
    }

    public static String getFrontArticleName() {
        if (frontArticleName == null) {
            frontArticleName = ApplicationParamUtil.findParamByName("front_artikkel");
        }
        return frontArticleName;
    }

    public static String getVeggArticleName() {
        if (veggArticleName == null) {
            veggArticleName = ApplicationParamUtil.findParamByName("vegg_artikkel");
        }
        return veggArticleName;
    }

    /**
	 * @return statussjekker
	 */
    public static StatusCheckerInterface<Transportable> getTakstolChecker(ManagerRepository managerRepository) {
        if (takstolArticleName == null) {
            takstolArticleName = ApplicationParamUtil.findParamByName("takstol_artikkel");
        }
        if (takstolArticleType == null) {
            takstolArticleType = managerRepository.getArticleTypeManager().findByName(takstolArticleName);
        }
        return new TakstolStatusChecker(takstolArticleType, managerRepository.getTakstolPackageVManager(), managerRepository.getTakstolProductionVManager());
    }

    public static StatusCheckerInterface<Transportable> getGavlChecker() {
        ArticleTypeManager articleTypeManager = (ArticleTypeManager) ModelUtil.getBean("articleTypeManager");
        if (gavlArticleType == null) {
            gavlArticleType = articleTypeManager.findByName(getGavlArticleName());
        }
        return new DefaultProductionStatusChecker(gavlArticleType);
    }

    public static StatusCheckerInterface<Transportable> getGulvsponChecker() {
        ArticleTypeManager articleTypeManager = getArticleTypeManager();
        if (gulvsponArticleType == null) {
            gulvsponArticleType = articleTypeManager.findByName(getGulvsponArticleName());
        }
        return new GulvsponStatusChecker(gulvsponArticleType);
    }

    private static ArticleTypeManager getArticleTypeManager() {
        return articleTypeManager != null ? articleTypeManager : (ArticleTypeManager) ModelUtil.getBean(ArticleTypeManager.MANAGER_NAME);
    }

    public static StatusCheckerInterface<Transportable> getFrontChecker() {
        ArticleTypeManager articleTypeManager = (ArticleTypeManager) ModelUtil.getBean("articleTypeManager");
        ArticleType frontArticleType = articleTypeManager.findByName(getFrontArticleName());
        return new OnStorageStatusChecker(frontArticleType);
    }

    public static StatusCheckerInterface<Transportable> getVeggChecker() {
        ArticleTypeManager articleTypeManager = (ArticleTypeManager) ModelUtil.getBean("articleTypeManager");
        ArticleType veggArticleType = articleTypeManager.findByName(getVeggArticleName());
        return new OnStorageStatusChecker(veggArticleType);
    }

    public static StatusCheckerInterface<Transportable> getSteinChecker() {
        if (sendesGgAttributeName == null) {
            sendesGgAttributeName = ApplicationParamUtil.findParamByName("gg_attributt");
        }
        if (steinArticleName == null) {
            steinArticleName = ApplicationParamUtil.findParamByName("stein_artikkel");
        }
        return new TaksteinAttributeStatusChecker(sendesGgAttributeName, steinArticleName, "Ja", "Taksteintype");
    }

    public static List<OrderLineWrapper> getOrderLineWrapperList(final Collection<OrderLine> list) {
        ArrayList<OrderLineWrapper> returnList = new ArrayList<OrderLineWrapper>();
        for (OrderLine orderLine : list) {
            returnList.add(new OrderLineWrapper(orderLine));
        }
        return returnList;
    }

    public static String removeNoAttributes(final String infoString) {
        StringBuffer buffer = new StringBuffer();
        if (infoString != null) {
            String[] attributeSplit = infoString.split(";");
            if (attributeSplit != null && attributeSplit.length > 1) {
                int size = attributeSplit.length;
                String[] valueSplit;
                for (int i = 0; i < size; i++) {
                    valueSplit = attributeSplit[i].split(":");
                    if (valueSplit != null && valueSplit.length > 1) {
                        if (!valueSplit[1].equalsIgnoreCase("Nei")) {
                            if (buffer.length() > 0) {
                                buffer.append(",");
                            }
                            buffer.append(valueSplit[0]);
                            if (!valueSplit[1].equalsIgnoreCase("Ja")) {
                                buffer.append(":");
                                for (int j = 1; j < valueSplit.length; j++) buffer.append(valueSplit[j]);
                            }
                        }
                    }
                }
            } else {
                buffer.append(infoString);
            }
        }
        return buffer.toString();
    }

    public static String upperFirstLetter(final String str) {
        if (str != null && str.length() > 0) {
            return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
        }
        return str;
    }

    public static boolean isNumber(final String number) {
        boolean returnValue = true;
        if (number != null) {
            try {
                Double.valueOf(number);
            } catch (NumberFormatException e) {
                returnValue = false;
            }
        }
        return returnValue;
    }

    public static boolean isListsSame(final List<String> list1, final List<String> list2) {
        if (list1 != null && list2 != null) {
            if (list1.size() == list2.size()) {
                Collections.sort(list1);
                Collections.sort(list2);
                for (String string : list1) {
                    if (!list2.contains(string)) {
                        return false;
                    }
                }
                return true;
            }
        } else if (list1 != null && list2 != null) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static void fillMap(final Map map, final Object[][] objects) {
        if (map != null && objects != null) {
            for (int i = 0; i < objects.length; i++) {
                map.put(objects[i][0], objects[i][1]);
            }
        }
    }

    /**
	 * Kloner dato.
	 * 
	 * @param orgDate
	 * @return dato
	 */
    public static Date cloneDate(final Date orgDate) {
        if (orgDate != null) {
            return new Date(orgDate.getTime());
        }
        return null;
    }

    public static Date getFirstDateInWeek(final Integer year, final Integer week) {
        Calendar cal = Calendar.getInstance(new Locale("NO"));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, week);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getFirstDateInYear(final Integer year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, 1);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getLastDateInWeek(final Integer year, final Integer week) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, week);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + 6);
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
        return cal.getTime();
    }

    public static Collection<OrderCost> cloneCosts(final Collection<OrderCost> orgCosts) {
        ArrayList<OrderCost> list = null;
        if (orgCosts != null) {
            list = new ArrayList<OrderCost>();
            for (OrderCost orderCost : orgCosts) {
                list.add(new OrderCost(orderCost.getOrderCostId(), orderCost.getOrder(), orderCost.getCostType(), orderCost.getCostUnit(), orderCost.getCostAmount(), orderCost.getInclVat(), orderCost.getSupplier(), orderCost.getInvoiceNr(), orderCost.getDeviation(), orderCost.getTransportCostBasis(), orderCost.getPostShipment(), orderCost.getComment()));
            }
        }
        return list;
    }

    public static YearWeek addWeek(final YearWeek yearWeek, final int numberOfWeeks) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, yearWeek.getYear());
        cal.set(Calendar.WEEK_OF_YEAR, yearWeek.getWeek());
        cal.set(Calendar.DAY_OF_WEEK, cal.getMaximum(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.WEEK_OF_YEAR, numberOfWeeks);
        int year = cal.get(Calendar.YEAR);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        if (numberOfWeeks < 0 && week > yearWeek.getWeek() && year == yearWeek.getYear()) {
            year = year - 1;
        }
        YearWeek newYearWeek = new YearWeek(year, week);
        return newYearWeek;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, AbstractProductionPackageViewHandler> getProductionPackageHandlers(final VismaFileCreator vismaFileCreator, final Login login, OrderViewHandlerFactory orderViewHandlerFactory, ManagerRepository managerRepository, DeviationViewHandlerFactory deviationViewHandlerFactory, ShowTakstolInfoActionFactory showTakstolInfoActionFactory, ArticleType articleTypeTakstol, TakstolPackageApplyList takstolPackageApplyList, TakstolProductionApplyList takstolProductionApplyList, SetProductionUnitActionFactory setProductionUnitActionFactory, CostType costTypeTross, CostUnit costUnitTross) {
        Map<String, AbstractProductionPackageViewHandler> map = new Hashtable<String, AbstractProductionPackageViewHandler>();
        addPacklist(map, orderViewHandlerFactory, managerRepository, deviationViewHandlerFactory, login, costTypeTross, costUnitTross);
        addVeggProduction(login, map, deviationViewHandlerFactory, managerRepository, setProductionUnitActionFactory);
        addFrontProduction(login, map, managerRepository, deviationViewHandlerFactory, setProductionUnitActionFactory);
        addGavlProduction(login, map, deviationViewHandlerFactory, managerRepository, setProductionUnitActionFactory);
        addTakstolProduction(map, login, deviationViewHandlerFactory, managerRepository, showTakstolInfoActionFactory, articleTypeTakstol, takstolProductionApplyList, setProductionUnitActionFactory);
        addTakstolPackage(map, login, managerRepository, deviationViewHandlerFactory, takstolPackageApplyList);
        addOrderLine(login, managerRepository, deviationViewHandlerFactory, map);
        addGulvsponPackage(login, managerRepository, deviationViewHandlerFactory, map);
        return map;
    }

    @SuppressWarnings("unchecked")
    private static void addGulvsponPackage(final Login login, final ManagerRepository managerRepository, final DeviationViewHandlerFactory deviationViewHandlerFactory, final Map<String, AbstractProductionPackageViewHandler> map) {
        GulvsponPackageVManager gulvsponPackageVManager = (GulvsponPackageVManager) ModelUtil.getBean("gulvsponPackageVManager");
        map.put("Gulvspon", (AbstractProductionPackageViewHandler) new PackageViewHandler(login, managerRepository, deviationViewHandlerFactory, new GulvsponPackageApplyList(login, gulvsponPackageVManager, managerRepository), "Pakking av gulvspon", TableEnum.TABLEPACKAGEGULVSPON, ApplicationParamUtil.findParamByName("gulvspon_attributt")));
    }

    @SuppressWarnings("unchecked")
    private static void addOrderLine(final Login login, final ManagerRepository managerRepository, final DeviationViewHandlerFactory deviationViewHandlerFactory, final Map<String, AbstractProductionPackageViewHandler> map) {
        OrderLineManager orderLineManager = (OrderLineManager) ModelUtil.getBean("orderLineManager");
        map.put("Takstein", (AbstractProductionPackageViewHandler) new PackageViewHandler(login, managerRepository, deviationViewHandlerFactory, new TaksteinPackageApplyList(login, orderLineManager, managerRepository), "Pakking takstein", TableEnum.TABLETAKSTEIN, ApplicationParamUtil.findParamByName("stein_artikkel")));
    }

    @SuppressWarnings("unchecked")
    private static void addTakstolPackage(final Map<String, AbstractProductionPackageViewHandler> map, final Login login, ManagerRepository managerRepository, DeviationViewHandlerFactory deviationViewHandlerFactory, TakstolPackageApplyList takstolPackageApplyList) {
        AbstractProductionPackageViewHandler abstractProductionPackageViewHandler = (AbstractProductionPackageViewHandler) new TakstolPackageViewHandler(takstolPackageApplyList, login, TableEnum.TABLEPACKAGETAKSTOL, ApplicationParamUtil.findParamByName("takstol_artikkel"), managerRepository, deviationViewHandlerFactory);
        map.put("TakstolPakking", abstractProductionPackageViewHandler);
    }

    @SuppressWarnings("unchecked")
    private static void addTakstolProduction(final Map<String, AbstractProductionPackageViewHandler> map, final Login login, DeviationViewHandlerFactory deviationViewHandlerFactory, ManagerRepository managerRepository, ShowTakstolInfoActionFactory showTakstolInfoActionFactory, ArticleType articleTypeTakstol, TakstolProductionApplyList takstolProductionApplyList, SetProductionUnitActionFactory setProductionUnitActionFactory) {
        AbstractProductionPackageViewHandler abstractProductionPackageViewHandler = (AbstractProductionPackageViewHandler) new TakstolProductionViewHandler(takstolProductionApplyList, login, managerRepository, deviationViewHandlerFactory, showTakstolInfoActionFactory, setProductionUnitActionFactory);
        map.put("TakstolProduksjon", abstractProductionPackageViewHandler);
    }

    @SuppressWarnings("unchecked")
    private static void addGavlProduction(final Login login, final Map<String, AbstractProductionPackageViewHandler> map, DeviationViewHandlerFactory deviationViewHandlerFactory, ManagerRepository managerRepository, SetProductionUnitActionFactory setProductionUnitActionFactory) {
        GavlProductionWindow gavlProductionWindow = new GavlProductionWindow(login, managerRepository, deviationViewHandlerFactory, setProductionUnitActionFactory);
        gavlProductionWindow.setLogin(login);
        map.put("Gavl", (AbstractProductionPackageViewHandler) gavlProductionWindow.getViewHandler());
    }

    @SuppressWarnings("unchecked")
    private static void addFrontProduction(final Login login, final Map<String, AbstractProductionPackageViewHandler> map, ManagerRepository managerRepository, DeviationViewHandlerFactory deviationViewHandlerFactory, SetProductionUnitActionFactory setProductionUnitActionFactory) {
        FrontProductionWindow frontProductionWindow = new FrontProductionWindow(login, managerRepository, deviationViewHandlerFactory, setProductionUnitActionFactory);
        frontProductionWindow.setLogin(login);
        map.put("Front", (AbstractProductionPackageViewHandler) frontProductionWindow.getViewHandler());
    }

    @SuppressWarnings("unchecked")
    private static void addVeggProduction(final Login login, final Map<String, AbstractProductionPackageViewHandler> map, DeviationViewHandlerFactory deviationViewHandlerFactory, ManagerRepository managerRepository, SetProductionUnitActionFactory setProductionUnitActionFactory) {
        VeggProductionWindow veggProductionWindow = new VeggProductionWindow(login, managerRepository, deviationViewHandlerFactory, setProductionUnitActionFactory);
        veggProductionWindow.setLogin(login);
        map.put("Vegg", (AbstractProductionPackageViewHandler) veggProductionWindow.getViewHandler());
    }

    @SuppressWarnings("unchecked")
    private static void addPacklist(final Map<String, AbstractProductionPackageViewHandler> map, OrderViewHandlerFactory orderViewHandlerFactory, ManagerRepository managerRepository, DeviationViewHandlerFactory deviationViewHandlerFactory, Login login, CostType costTypeTross, CostUnit costUnitTross) {
        PacklistVManager packlistVManager = (PacklistVManager) ModelUtil.getBean("packlistVManager");
        map.put("Pakkliste", (AbstractProductionPackageViewHandler) new PacklistViewHandler(login, managerRepository, deviationViewHandlerFactory, orderViewHandlerFactory, new PacklistApplyList(login, packlistVManager), costTypeTross, costUnitTross));
    }

    public static JComboBox getComboBoxProductAreaGroup(final ApplicationUser applicationUser, final UserType userType, final PresentationModel productAreaGroupModel) {
        List<ProductAreaGroup> groupList = new ArrayList<ProductAreaGroup>();
        ProductAreaGroup userProductAreaGroup = null;
        if (applicationUser != null && applicationUser.getProductArea() != null) {
            userProductAreaGroup = applicationUser.getProductArea().getProductAreaGroup();
        }
        if (!userType.isAdministrator()) {
            setProductAreaListForNotAdministrator(applicationUser, groupList, userProductAreaGroup);
        } else {
            initProductAreaGroupList();
            groupList.addAll(productAreaGroupList);
        }
        if (groupList.size() == 0) {
            throw new ProTransRuntimeException("Bruker har ikke definert noe produktomr�de");
        }
        JComboBox comboBox = new JComboBox(new ComboBoxAdapter(groupList, productAreaGroupModel.getModel(ProductAreaGroupModel.PROPERTY_PRODUCT_AREA_GROUP)));
        comboBox.setName("ComboBoxProductAreaGroup");
        comboBox.setSelectedItem(userProductAreaGroup);
        return comboBox;
    }

    private static void setProductAreaListForNotAdministrator(final ApplicationUser applicationUser, final List<ProductAreaGroup> aProductAreaGroupList, final ProductAreaGroup userProductAreaGroup) {
        if (userProductAreaGroup != null) {
            aProductAreaGroupList.add(userProductAreaGroup);
        }
        ApplicationUserManager applicationUserManager = (ApplicationUserManager) ModelUtil.getBean(ApplicationUserManager.MANAGER_NAME);
        applicationUserManager.lazyLoad(applicationUser, new LazyLoadEnum[][] { { LazyLoadEnum.USER_PRODUCT_AREA_GROUPS, LazyLoadEnum.NONE } });
        if (applicationUser.getUserProductAreaGroups() != null && applicationUser.getUserProductAreaGroups().size() != 0) {
            Set<UserProductAreaGroup> groups = applicationUser.getUserProductAreaGroups();
            for (UserProductAreaGroup group : groups) {
                aProductAreaGroupList.add(group.getProductAreaGroup());
            }
        }
    }

    public static void showEditViewable(final EditViewable editViewable, final WindowInterface window) {
        JDialog dialog = Util.getDialog(window, editViewable.getHeading(), true);
        dialog.setName(editViewable.getDialogName());
        WindowInterface dialogWindow = new JDialogAdapter(dialog);
        dialogWindow.add(editViewable.buildPanel(dialogWindow));
        dialog.pack();
        Util.locateOnScreenCenter(dialog);
        dialogWindow.setVisible(true);
    }

    private static void initProductAreaList() {
        if (productAreaList == null) {
            ProductAreaManager productAreaManager = (ProductAreaManager) ModelUtil.getBean("productAreaManager");
            productAreaList = productAreaManager.findAll();
        }
    }

    private static void initProductAreaGroupList() {
        if (productAreaGroupList == null) {
            productAreaGroupManager = productAreaGroupManager != null ? productAreaGroupManager : (ProductAreaGroupManager) ModelUtil.getBean("productAreaGroupManager");
            productAreaGroupList = productAreaGroupManager.findAll();
        }
    }

    public static JComboBox createComboBoxProductArea(final ValueModel valueModel, final boolean addEmpty) {
        initProductAreaList();
        List<ProductArea> tmpProductAreaList = new ArrayList<ProductArea>(productAreaList);
        if (addEmpty) {
            tmpProductAreaList.add(0, null);
        }
        return BasicComponentFactory.createComboBox(new SelectionInList(tmpProductAreaList, valueModel));
    }

    public static JComboBox createComboBoxProductAreaGroup(final ValueModel valueModel) {
        initProductAreaGroupList();
        return BasicComponentFactory.createComboBox(new SelectionInList(productAreaGroupList, valueModel));
    }

    public static List<ProductAreaGroup> getProductAreaGroupList() {
        initProductAreaGroupList();
        return productAreaGroupList;
    }

    public static String splitLongStringIntoLinesWithBr(final String text, final int lineSize) {
        StringBuilder builder = new StringBuilder();
        if (text != null && text.length() > lineSize) {
            splitIntoLines(text, lineSize, builder);
        } else {
            builder.append(text);
        }
        return builder.toString();
    }

    private static void splitIntoLines(final String text, final int lineSize, final StringBuilder builder) {
        String[] words = text.split(" ");
        int numberOfWords = words.length;
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < numberOfWords; i++) {
            line = addWordToLine(lineSize, builder, words, line, i);
        }
        if (line.length() > 0) {
            builder.append(line);
        }
    }

    private static StringBuilder addWordToLine(final int lineSize, final StringBuilder builder, final String[] words, final StringBuilder line, final int i) {
        StringBuilder lineBuilder = line;
        if (lineBuilder.length() > lineSize) {
            lineBuilder = makeNewLine(builder, words, line, i);
        } else {
            lineBuilder.append(" ");
            lineBuilder.append(words[i]);
        }
        return lineBuilder;
    }

    private static StringBuilder makeNewLine(final StringBuilder builder, final String[] words, final StringBuilder line, final int i) {
        StringBuilder lineBuilder;
        builder.append(line);
        builder.append("<br>");
        lineBuilder = new StringBuilder(words[i]);
        return lineBuilder;
    }

    public static void createDialogAndRunInThread(final Threadable threadable) {
        JDialog dialog = getDialog(null, "", false);
        dialog.setSize(new Dimension(250, 250));
        dialog.setVisible(true);
        WindowInterface dialogWindow = new JDialogAdapter(dialog);
        locateOnScreenCenter(dialogWindow);
        runInThreadWheel(dialogWindow.getRootPane(), threadable, new Object[] { dialogWindow });
    }

    public static String getFileName(final Component comp, final FileFilter filter, final String openDir) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName("fileChooser");
        fileChooser.setFileFilter(filter);
        String dir = openDir != null ? openDir : fileDirectory;
        if (dir != null) {
            lastFileDir = new File(dir);
        }
        fileChooser.setCurrentDirectory(lastFileDir);
        String fileName = null;
        if (fileChooser.showOpenDialog(comp) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            fileName = file.getAbsolutePath();
        }
        lastFileDir = fileChooser.getCurrentDirectory();
        return fileName;
    }

    public static boolean isDateInPeriode(final Date date, final Periode period) {
        Calendar calDate = Calendar.getInstance();
        Calendar calFirstDate = Calendar.getInstance();
        Calendar calLastDate = Calendar.getInstance();
        calDate.setTime(date);
        calFirstDate.setTime(getFirstDateInWeek(period.getYear(), period.getWeek()));
        calLastDate.setTime(getLastDateInWeek(period.getYear(), period.getToWeek()));
        if (calDate.compareTo(calFirstDate) >= 0 && calDate.compareTo(calLastDate) <= 0) {
            return true;
        }
        return false;
    }

    public static boolean isDateBeforePeriode(final Date date, final Periode period) {
        Calendar calDate = Calendar.getInstance();
        Calendar calFirstDate = Calendar.getInstance();
        calDate.setTime(date);
        calFirstDate.setTime(getFirstDateInWeek(period.getYear(), period.getWeek()));
        if (calDate.before(calFirstDate)) {
            return true;
        }
        return false;
    }

    public static BigDecimal convertNullToBigDecimal(final BigDecimal bigDecimal) {
        if (bigDecimal != null) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(0);
    }

    public static String getGuiProperty(final String key) {
        initGuiRecourses();
        return guiResources.getString(key);
    }

    private static void initGuiRecourses() {
        if (guiResources == null) {
            guiResources = ResourceBundle.getBundle("gui", new Locale("no", "NO", "B"));
        }
    }

    public static int compareTransport(final Transport transport1, final Transport transport2) {
        return new CompareToBuilder().append(transport1.getTransportYear(), transport2.getTransportYear()).append(transport1.getTransportWeek(), transport2.getTransportWeek()).toComparison();
    }

    public static Date convertIntToDate(Integer intValue) {
        return intValue != null && intValue != 0 ? new Date(Long.valueOf(String.valueOf(intValue) + "000")) : null;
    }

    public static Integer convertDateToInt(Date dateValue) {
        if (dateValue != null) {
            String streng = String.valueOf(dateValue.getTime());
            streng = streng.substring(0, streng.length() - 3);
            return Integer.valueOf(streng);
        }
        return null;
    }

    public static String convertBigDecimalToString(BigDecimal bigDecimal) {
        if (bigDecimal != null) {
            DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
            decimalFormat.setDecimalSeparatorAlwaysShown(false);
            return decimalFormat.format(bigDecimal);
        }
        return "0";
    }

    public static String formatDate(Date date, SimpleDateFormat dateFormat) {
        return date != null ? dateFormat.format(date) : "";
    }

    public static void main(String[] args) {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.WEEK_OF_YEAR, 52);
        now.set(Calendar.YEAR, 2008);
        now.set(Calendar.DAY_OF_WEEK, now.getMaximum(Calendar.DAY_OF_WEEK));
        System.out.println("Current date : " + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DATE) + "-" + now.get(Calendar.YEAR));
        System.out.println("Current week of month is : " + now.get(Calendar.WEEK_OF_MONTH));
        System.out.println("Current week of year is : " + now.get(Calendar.WEEK_OF_YEAR));
        System.out.println("Current year is : " + now.get(Calendar.YEAR));
        now.add(Calendar.WEEK_OF_YEAR, 1);
        System.out.println("Current date : " + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DATE) + "-" + now.get(Calendar.YEAR));
        System.out.println("Current week of month is : " + now.get(Calendar.WEEK_OF_MONTH));
        System.out.println("Current week of year is : " + now.get(Calendar.WEEK_OF_YEAR));
        System.out.println("Current year is : " + now.get(Calendar.YEAR));
    }

    public static void copySortOrder(JXTable fromTable, JXTable toTable) {
        int columnCount = fromTable.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            SortOrder sortOrder = fromTable.getSortOrder(i);
            if (sortOrder.isSorted()) {
                toTable.setSortOrder(i, fromTable.getSortOrder(i));
                break;
            }
        }
    }

    public static JComboBox getComboBoxJobFunction(final boolean addEmpty, final ValueModel valueModel) {
        initJobFunctionList();
        List<JobFunction> tmpJobFunctionList = new ArrayList<JobFunction>();
        tmpJobFunctionList.addAll(jobFunctionList);
        if (addEmpty) {
            tmpJobFunctionList.add(0, null);
        }
        return new JComboBox(new ComboBoxAdapter(tmpJobFunctionList, valueModel));
    }

    public static JComboBox getComboBoxEmployeeType(final boolean addEmpty, final ValueModel valueModel) {
        initEmployeeTypeList();
        List<EmployeeType> tmpEmployeeTypeList = new ArrayList<EmployeeType>();
        tmpEmployeeTypeList.addAll(employeeTypeList);
        if (addEmpty) {
            tmpEmployeeTypeList.add(0, null);
        }
        return new JComboBox(new ComboBoxAdapter(tmpEmployeeTypeList, valueModel));
    }

    private static void initJobFunctionList() {
        if (jobFunctionList == null) {
            jobFunctionManager = jobFunctionManager != null ? jobFunctionManager : (JobFunctionManager) ModelUtil.getBean("jobFunctionManager");
            jobFunctionList = jobFunctionManager.findAll();
        }
    }

    private static void initEmployeeTypeList() {
        if (employeeTypeList == null) {
            EmployeeTypeManager employeeTypeManager = (EmployeeTypeManager) ModelUtil.getBean("employeeTypeManager");
            employeeTypeList = employeeTypeManager.findAll();
        }
    }

    public static List<String> getFirstNameList() {
        if (firstNameList == null) {
            firstNameList = new ArrayList<String>();
            lastNameList = new ArrayList<String>();
            ApplicationUserManager applicationUserManager = (ApplicationUserManager) ModelUtil.getBean("applicationUserManager");
            List<ApplicationUser> users = applicationUserManager.findAllNotGroup();
            if (users != null) {
                for (ApplicationUser user : users) {
                    firstNameList.add(user.getFirstName());
                    lastNameList.add(user.getLastName());
                }
            }
        }
        return firstNameList;
    }

    public static List<String> getLastNameList() {
        if (lastNameList == null) {
            firstNameList = new ArrayList<String>();
            lastNameList = new ArrayList<String>();
            ApplicationUserManager applicationUserManager = (ApplicationUserManager) ModelUtil.getBean("applicationUserManager");
            List<ApplicationUser> users = applicationUserManager.findAllNotGroup();
            if (users != null) {
                for (ApplicationUser user : users) {
                    firstNameList.add(user.getFirstName());
                    lastNameList.add(user.getLastName());
                }
            }
        }
        return lastNameList;
    }

    public static Date convertDate(Date date, SimpleDateFormat dateFormat) {
        if (date != null) {
            try {
                return dateFormat.parse(dateFormat.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    public static String convertBooleanToString(Boolean bool) {
        if (bool != null && bool) {
            return "Ja";
        }
        return "Nei";
    }

    public static ProductArea getProductAreaFromProductAreaNr(Integer productAreaNr) {
        ProductAreaManager productAreaManager = (ProductAreaManager) ModelUtil.getBean("productAreaManager");
        return productAreaManager.getProductAreaForProductAreaNr(productAreaNr, true);
    }

    public static String getDatabaseField(String fieldName) {
        Pattern pattern = Pattern.compile("\\p{Upper}");
        Matcher matcher = pattern.matcher(fieldName);
        StringBuilder databaseField = new StringBuilder();
        int counter = 0;
        int matchIndex = 0;
        int lastMatchIndex = 0;
        while (matcher.find()) {
            matchIndex = matcher.start();
            if (counter > 1) {
                databaseField.append("_");
            }
            databaseField.append(fieldName.substring(lastMatchIndex, matchIndex));
            lastMatchIndex = matchIndex;
        }
        if (matchIndex != 0) {
            databaseField.append("_").append(fieldName.substring(lastMatchIndex, fieldName.length()));
            ;
        }
        return databaseField.toString();
    }

    public static Integer getWeekPart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    public static Integer getYearPart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static JYearChooser createYearChooser(Object mappingObject) {
        JYearChooser yearChooser = new JYearChooser();
        PropertyConnector conn = new PropertyConnector(yearChooser, "year", mappingObject, "value");
        conn.updateProperty2();
        return yearChooser;
    }

    public static Integer convertStringToInteger(String string) {
        if (string != null && string.length() != 0) {
            return Integer.valueOf(string);
        }
        return null;
    }

    public static List<String> convertStringListToList(String string) {
        List<String> list;
        if (string != null) {
            list = Arrays.asList(string.split(","));
        } else {
            list = new ArrayList<String>();
        }
        return list;
    }

    public static List<ArticleType> getArticleTypeList() {
        if (articleTypeList == null) {
            ArticleTypeManager articleTypeManager = getArticleTypeManager();
            articleTypeList = new ArrayList<ArticleType>(articleTypeManager.findAll());
        }
        return articleTypeList;
    }

    public static Integer convertBigDecimalToInteger(final BigDecimal number) {
        if (number != null) {
            return number.intValue();
        }
        return null;
    }

    public static <T> Set<T> getSet(Set<T> set) {
        return set != null ? set : new HashSet<T>();
    }

    public static boolean isAfter(YearWeek testWeek, YearWeek compareWeek) {
        Calendar testCal = Calendar.getInstance();
        Calendar compCal = Calendar.getInstance();
        testCal.set(Calendar.YEAR, testWeek.getYear());
        testCal.set(Calendar.WEEK_OF_YEAR, testWeek.getWeek());
        testCal.set(Calendar.DAY_OF_WEEK, testCal.getMinimum(Calendar.DAY_OF_WEEK));
        testCal.set(Calendar.HOUR, testCal.getMinimum(Calendar.HOUR));
        testCal.set(Calendar.MINUTE, testCal.getMinimum(Calendar.MINUTE));
        testCal.set(Calendar.SECOND, testCal.getMinimum(Calendar.SECOND));
        testCal.set(Calendar.MILLISECOND, testCal.getMinimum(Calendar.MILLISECOND));
        compCal.set(Calendar.YEAR, compareWeek.getYear());
        compCal.set(Calendar.WEEK_OF_YEAR, compareWeek.getWeek());
        compCal.set(Calendar.DAY_OF_WEEK, compCal.getMinimum(Calendar.DAY_OF_WEEK));
        compCal.set(Calendar.HOUR, testCal.getMinimum(Calendar.HOUR));
        compCal.set(Calendar.MINUTE, testCal.getMinimum(Calendar.MINUTE));
        compCal.set(Calendar.SECOND, testCal.getMinimum(Calendar.SECOND));
        compCal.set(Calendar.MILLISECOND, testCal.getMinimum(Calendar.MILLISECOND));
        return testCal.after(compCal);
    }

    public static BigDecimal convertStringToBigDecimal(final String stringValue) {
        String tmpStringValue = stringValue != null && stringValue.length() == 0 ? "0" : stringValue;
        return tmpStringValue != null ? BigDecimal.valueOf(Double.valueOf(tmpStringValue.replaceAll(",", "."))) : null;
    }

    public static void setFileDirectory(String aFileDirectory) {
        fileDirectory = aFileDirectory;
    }

    public static String getColliName(String articleName) {
        Map<String, String> colliSetup = ApplicationParamUtil.getColliSetup();
        String useColliName = null;
        if (colliSetup.containsValue(articleName)) {
            Set<String> colliNames = colliSetup.keySet();
            String tmpStr;
            for (String colliName : colliNames) {
                tmpStr = colliSetup.get(colliName);
                if (tmpStr.equalsIgnoreCase(articleName)) {
                    useColliName = colliName;
                    break;
                }
            }
        }
        return useColliName;
    }

    public static String getMonthByWeek(int week) {
        if (week > 51) {
            return "Desember";
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"), new Locale("no_NO"));
        cal.set(Calendar.WEEK_OF_YEAR, week);
        int month = cal.get(Calendar.MONTH);
        switch(month) {
            case Calendar.JANUARY:
                return "Januar";
            case Calendar.FEBRUARY:
                return "Februar";
            case Calendar.MARCH:
                return "Mars";
            case Calendar.APRIL:
                return "April";
            case Calendar.MAY:
                return "Mai";
            case Calendar.JUNE:
                return "Juni";
            case Calendar.JULY:
                return "Juli";
            case Calendar.AUGUST:
                return "August";
            case Calendar.SEPTEMBER:
                return "September";
            case Calendar.OCTOBER:
                return "Oktober";
            case Calendar.NOVEMBER:
                return "November";
            case Calendar.DECEMBER:
                return "Desember";
            default:
                return "Ukjent";
        }
    }

    public static JComboBox getComboBoxUsers(ValueModel valueModel, boolean sort) {
        return new JComboBox(new ComboBoxAdapter(ApplicationUserUtil.getUserList(sort), valueModel));
    }

    public static String getLastFridayAsString(Integer year, Integer week) {
        if (year == null || week == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, week - 1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        return DATE_FORMAT_YYYYMMDD.format(cal.getTime());
    }
}

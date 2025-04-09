package edu.udo.scaffoldhunter.view.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.table.BannerManager.BannerState;
import edu.udo.scaffoldhunter.view.util.SVG;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGLoadObserver;

/**
 * @author Michael Hesse
 *
 */
public class Model extends AbstractTableModel implements BannerManagerListener {

    private final DbManager db;

    private final BannerPool bannerPool;

    private BiMap<Molecule, Integer> molecules;

    private BiMap<Integer, Molecule> moleculesInverse;

    private boolean hasClusters = false;

    List<ColumnInfo> columnInfo;

    int svgColumnNumber;

    Subset subset;

    List<Integer> clusterNumberList = new ArrayList<Integer>();

    SVGCache svgCache;

    SVGObserver[] svgObserver;

    BannerManager bm;

    DataPump dataPump;

    static final int ROW_PRELOAD = 150;

    static final int COLUMN_PRELOAD = 20;

    ViewComponent vc = null;

    private static enum BannerChangeState {

        READY, STOP
    }

    ;

    BannerChangeState bannerChangeState;

    /** current state of the sorting process */
    public enum SortState {

        /** user requested sorting                 */
        START, /** loading values for the column(s)       */
        LOADING, /** actual sorting of the loaded values    */
        SORTING, /** no sorting related process running atm */
        READY
    }

    ;

    private SortState sortState;

    class ColumnInfo implements Comparable<ColumnInfo> {

        public String title;

        public String key;

        public Class<?> type;

        public PropertyDefinition propertyDefinition;

        public ColumnInfo() {
        }

        public ColumnInfo(String title, String key, Class<?> type, PropertyDefinition propertyDefinition) {
            this.title = title;
            this.key = key;
            this.type = type;
            this.propertyDefinition = propertyDefinition;
        }

        @Override
        public int compareTo(ColumnInfo b) {
            if ((title == null) && (b.title == null)) return 0;
            if (title == null) return 1;
            if (b.title == null) return -1;
            return title.compareTo(b.title);
        }
    }

    /**
     * internal class to assign the notification from the SVGCache
     * to a tablecell
     * @author Michael Hesse
     */
    class SVGObserver implements SVGLoadObserver {

        private int row, column;

        private Subset subset;

        public SVGObserver(int row, int column) {
            this.row = row;
            this.column = column;
            this.subset = getSubset();
        }

        @Override
        public void svgLoaded(SVG svg) {
            svg.removeObserver(this);
            if (getSubset() == subset) fireTableCellUpdated(row, column);
        }
    }

    class MoleculeComparator<T> implements Comparator<T> {

        @Override
        public int compare(T a, T b) {
            Molecule x = (Molecule) a;
            Molecule y = (Molecule) b;
            return x.getTitle().toLowerCase().compareTo(y.getTitle().toLowerCase());
        }
    }

    /**
     * the constructor
     * 
     * @param db
     *  the DB manager
     * @param hasClusters 
     *  true if there should be a column for clusternumbers
     * @param bannerPool 
     */
    public Model(DbManager db, boolean hasClusters, BannerPool bannerPool) {
        this.db = db;
        this.bannerPool = bannerPool;
        molecules = HashBiMap.create();
        moleculesInverse = molecules.inverse();
        columnInfo = new ArrayList<ColumnInfo>();
        this.hasClusters = hasClusters;
        dataPump = null;
        svgCache = new SVGCache(db);
        svgColumnNumber = -1;
        clusterNumberList.add(999999999);
        sortState = SortState.READY;
        setSubset(null, null);
        bannerChangeState = BannerChangeState.READY;
    }

    /**
     * @return  the DB manager
     */
    public DbManager getDbManager() {
        return db;
    }

    /**
     * sets the subset that this model should serve
     * @param subset
     * @param moleculeOrder 
     */
    public void setSubset(Subset subset, List<Molecule> moleculeOrder) {
        molecules.clear();
        columnInfo.clear();
        sortState = SortState.READY;
        this.subset = subset;
        if (subset != null) {
            if (dataPump != null) dataPump.destroy();
            dataPump = new DataPump(this);
            int moleculeIndex = 0;
            if (moleculeOrder == null) {
                List<Molecule> mo = new ArrayList<Molecule>();
                mo.addAll(subset.getMolecules());
                Collections.sort(mo, new MoleculeComparator<Molecule>());
                for (Molecule m : mo) {
                    molecules.put(m, moleculeIndex);
                    moleculeIndex++;
                }
            } else {
                for (Molecule m : moleculeOrder) {
                    molecules.put(m, moleculeIndex);
                    moleculeIndex++;
                }
            }
            {
                for (PropertyDefinition pd : subset.getSession().getDataset().getPropertyDefinitions().values()) {
                    if (!pd.isScaffoldProperty()) {
                        columnInfo.add(new ColumnInfo(pd.getTitle(), "key_pd_" + pd.getKey(), (pd.isStringProperty() ? String.class : Double.class), pd));
                    }
                }
                Collections.sort(columnInfo);
                columnInfo.add(0, new ColumnInfo("SMILES", "key_smiles", String.class, null));
                columnInfo.add(0, new ColumnInfo("SVG", "key_svg", SVG.class, null));
                columnInfo.add(0, new ColumnInfo(I18n.get("TableView.Model.PublicBanner"), "key_publicbanner", BannerManager.BannerState.class, null));
                columnInfo.add(1, new ColumnInfo(I18n.get("TableView.Model.PrivateBanner"), "key_privatebanner", BannerManager.BannerState.class, null));
                columnInfo.add(0, new ColumnInfo(I18n.get("TableView.Model.Title"), "key_title", String.class, null));
                if (hasClusters) {
                    columnInfo.add(0, new ColumnInfo(I18n.get("TableView.Model.Cluster"), "key_cluster", Integer.class, null));
                }
            }
            {
                svgColumnNumber = -1;
                for (int i = 0; i < columnInfo.size(); i++) if (columnInfo.get(i).key.equals("key_svg")) {
                    svgColumnNumber = i;
                    break;
                }
                svgObserver = new SVGObserver[molecules.size()];
                for (int i = 0; i < svgObserver.length; i++) svgObserver[i] = new SVGObserver(i, svgColumnNumber);
            }
        }
        bm = new BannerManager(subset, bannerPool);
        bm.addListener(this);
        this.fireTableStructureChanged();
    }

    /**
     * @return
     *  the subset that this model currently serves
     */
    public Subset getSubset() {
        return subset;
    }

    @Override
    public int getColumnCount() {
        return columnInfo.size();
    }

    @Override
    public int getRowCount() {
        return molecules.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        if (rowIndex < this.getRowCount()) {
            String key = columnInfo.get(columnIndex).key;
            if (key.equals("key_cluster")) {
                if (rowIndex < clusterNumberList.get(0)) value = 0; else {
                    int start = 0;
                    int end = clusterNumberList.size() - 1;
                    while (rowIndex < clusterNumberList.get(end - 1)) {
                        if (rowIndex < clusterNumberList.get((start + end) / 2)) end = (start + end) / 2; else start = (start + end) / 2;
                    }
                    value = end;
                }
            } else if (key.equals("key_title")) {
                value = getMoleculeAtRow(rowIndex).getTitle();
            } else if (key.equals("key_svg")) {
                value = svgCache.getSVG(getMoleculeAtRow(rowIndex), null, null, svgObserver[rowIndex]);
            } else if (key.equals("key_smiles")) {
                value = getMoleculeAtRow(rowIndex).getSmiles();
            } else if (key.equals("key_publicbanner")) {
                value = bm.getPublicBanner(getMoleculeAtRow(rowIndex));
            } else if (key.equals("key_privatebanner")) {
                value = bm.getPrivateBanner(getMoleculeAtRow(rowIndex));
            } else if (key.startsWith("key_pd_")) {
                Molecule m;
                Molecule mm = getMoleculeAtRow(rowIndex);
                PropertyDefinition pd = columnInfo.get(columnIndex).propertyDefinition;
                switch(getSortState()) {
                    case READY:
                        if (dataPump.contains(pd, mm)) m = mm; else m = null;
                        break;
                    case SORTING:
                        m = mm;
                        break;
                    default:
                        m = null;
                }
                if (columnInfo.get(columnIndex).type == String.class) {
                    if (m == null) {
                        value = "...";
                    } else {
                        value = m.getStringPropertyValue(pd);
                    }
                } else {
                    if (m == null) value = Double.NaN; else value = m.getNumPropertyValue(pd);
                }
            }
        }
        return value;
    }

    /**
     * @param row
     * @return molecule
     *  the molecule in the specified row
     */
    public Molecule getMoleculeAtRow(int row) {
        return moleculesInverse.get(row);
    }

    /**
     * @param column
     * @return
     *  the propertydefinition for the specified column
     */
    public PropertyDefinition getPropertyDefinitionAtColumn(int column) {
        return columnInfo.get(column).propertyDefinition;
    }

    /**
     * find the position of a molecule in the tablemodel. needed
     * for automatic selection of molecules
     * 
     * @param molecule
     *  the molecule for which the tablerow should be returned
     * @return
     *  the row in which the given molecule is stored in the model.
     *  -1 if the molecule isn't found in the model
     */
    public int getRowOfMolecule(Molecule molecule) {
        Integer row = molecules.get(molecule);
        return row != null ? row : -1;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnInfo.get(column).type;
    }

    @Override
    public String getColumnName(int column) {
        return columnInfo.get(column).title;
    }

    /**
     * returns the description of the moleculeproperty for this column
     * 
     * @param column
     * @return
     *  the propertydescription or null (if the column shows something else than a property)
     */
    public String getColumnDescription(int column) {
        if (columnInfo.get(column).propertyDefinition == null) return null;
        return columnInfo.get(column).propertyDefinition.getDescription();
    }

    /**
     * @param rows
     * @param columns
     */
    public void loadPropertyValues(List<Integer> rows, List<Integer> columns) {
        List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
        List<Molecule> molecules = new ArrayList<Molecule>();
        for (Integer column : columns) {
            PropertyDefinition pd = getPropertyDefinitionAtColumn(column);
            if (pd != null) propertyDefinitions.add(pd);
        }
        for (Integer row : rows) molecules.add(getMoleculeAtRow(row));
        if ((!propertyDefinitions.isEmpty()) & (!molecules.isEmpty())) {
            DataPumpBlock block = new DataPumpBlock(propertyDefinitions, molecules);
            dataPump.load(block);
        }
    }

    /**
     * sets the number of clusters and the number of elements in each cluster
     * @param clusterList
     * @return 
     *   true if the clusterlist is adopted, false if an error occured and the
     *   list is not adopted
     */
    public boolean setClusters(List<Integer> clusterList) {
        if (hasClusters == false) return false;
        {
            int length = 0;
            for (int clusterSize : clusterList) {
                length += clusterSize;
            }
            if (length != molecules.size()) return false;
        }
        {
            clusterNumberList.clear();
            int sum = 0;
            for (int clusterSize : clusterList) {
                sum += clusterSize;
                clusterNumberList.add(sum);
            }
        }
        if (getRowCount() != 0) fireTableRowsUpdated(0, getRowCount() - 1);
        return true;
    }

    /**
     * 
     */
    public void repaintTable() {
        TableModelListener[] tml = this.getTableModelListeners();
        for (int i = 0; i < tml.length; i++) {
            if (tml[i] instanceof ViewTable) {
                ((ViewTable) tml[i]).repaint();
            }
        }
    }

    /**
     * @return
     *  the state of the sorting process
     */
    public SortState getSortState() {
        return sortState;
    }

    /**
     * needed by the table to make this column unsortable.
     * and by the svg loading trigger.
     * @return
     *  index of the svg-column
     */
    public int getIndexOfSvgColumn() {
        return svgColumnNumber;
    }

    /**
     * @return
     *  the bannermanager
     */
    public BannerManager getBannerManager() {
        return bm;
    }

    @Override
    public void BannerStateChanged(Molecule molecule, boolean privateBanner, BannerState state) {
        int row = getRowOfMolecule(molecule);
        int column = -1;
        for (int i = 0; i < columnInfo.size(); i++) {
            if ((!privateBanner) & columnInfo.get(i).key.equals("key_publicbanner")) {
                column = i;
                break;
            }
            if (privateBanner & columnInfo.get(i).key.equals("key_privatebanner")) {
                column = i;
                break;
            }
        }
        if (column != -1) {
            this.fireTableCellUpdated(row, column);
        }
    }

    /**
     * @param c
     * @return
     *  the key of the tablecolumn
     */
    public String getColumnKey(int c) {
        if (c < columnInfo.size()) return columnInfo.get(c).key; else return "";
    }

    /**
     * @param vc
     */
    public void setViewComponent(ViewComponent vc) {
        this.vc = vc;
    }

    /**
     * 
     */
    public void reapplySelectionFromExtern() {
        if (vc != null) vc.applySelectionFromExtern();
    }

    /**
     * loads (and paints) the svg at the specified rowIndex
     * @param rowIndex
     */
    public void loadAnSvg(int rowIndex) {
        svgCache.getSVG(getMoleculeAtRow(rowIndex), null, null, svgObserver[rowIndex]);
    }

    /**
     * 
     */
    public void destroy() {
        bm.destroy();
        columnInfo.clear();
        columnInfo = null;
        clusterNumberList = null;
        subset = null;
        svgCache = null;
        for (int i = 0; i < svgObserver.length; i++) {
            svgObserver[i] = null;
        }
        if (dataPump != null) dataPump.destroy();
        dataPump = null;
        vc = null;
        molecules.clear();
        molecules = null;
        moleculesInverse.clear();
        moleculesInverse = null;
    }
}

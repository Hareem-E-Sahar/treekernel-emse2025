public class Test {    @Override
    public void update() {
        NumberFormat pf = I18NUtil.getPrecisionFormat();
        NumberFormat nf = I18NUtil.getGroupFormat();
        table.removeAll();
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, "Alchemy");
        item.setText(1, pf.format(getAlchemyBonus()));
        item.setText(2, nf.format(science.getAlchemy()));
        item.setText(3, "Income");
        item = new TableItem(table, SWT.NONE);
        item.setText(0, "Tools");
        item.setText(1, pf.format(getToolsBonus()));
        item.setText(2, nf.format(science.getTools()));
        item.setText(3, "Building Effectiveness");
        item = new TableItem(table, SWT.NONE);
        item.setText(0, "Housing");
        item.setText(1, pf.format(getHousingBonus()));
        item.setText(2, nf.format(science.getHousing()));
        item.setText(3, "Population Limits");
        item = new TableItem(table, SWT.NONE);
        item.setText(0, "Food");
        item.setText(1, pf.format(getFoodBonus()));
        item.setText(2, nf.format(science.getFood()));
        item.setText(3, "Food Production");
        item = new TableItem(table, SWT.NONE);
        item.setText(0, "Military");
        item.setText(1, pf.format(getMilitaryBonus()));
        item.setText(2, nf.format(science.getMilitary()));
        item.setText(3, "Gains in Combat");
        item = new TableItem(table, SWT.NONE);
        item.setText(0, "Crime");
        item.setText(1, pf.format(getCrimeBonus()));
        item.setText(2, nf.format(science.getCrime()));
        item.setText(3, "Thievery Effectiveness");
        item = new TableItem(table, SWT.NONE);
        item.setText(0, "Channeling");
        item.setText(1, pf.format(getChannelingBonus()));
        item.setText(2, nf.format(science.getChanneling()));
        item.setText(3, "Magic Effectiveness & Rune Production");
        table.update();
        super.update();
    }
}
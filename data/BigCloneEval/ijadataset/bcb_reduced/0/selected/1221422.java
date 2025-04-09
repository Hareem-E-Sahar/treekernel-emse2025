package org.jcrosstab;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

/** A generic Axis, could be either horizontal or vertical.  It contains
		one or more slices.
*/
public class Axis {

    private Vector<SliceDefinition> slices = new Vector<SliceDefinition>();

    private Slice first_slice = new Slice();

    private Map<String, Integer> map = null;

    private boolean is_horizontal = true;

    private boolean measures_on_this_axis = true;

    private String[][] axis_grid = null;

    public Axis(boolean horizontal) {
        is_horizontal = horizontal;
        measures_on_this_axis = horizontal;
    }

    /** Add a value to slice index 0.  This is included because it will be a very common, simple case.
	*/
    public void addValue(String s) {
        first_slice.addValue(s);
    }

    /** Add a new slice value under the first parameter.  The first param must already exist in Slice 0, the second will
		be added as a new sub-slice if it doesn't already exist.
	*/
    public void addValue(String s, String s2) {
        for (int i = 0; i < first_slice.sub_slice.size(); i++) {
            Slice slice = first_slice.sub_slice.get(i);
            if (slice.getValue().equals(s)) {
                slice.addValue(s2);
            }
        }
    }

    /** For slice value lists 3 items or longer.
	*/
    public void addValue(Vector<String> value_list) {
        first_slice.addValue(value_list);
    }

    /** Sort the slice given by the index parameter.
	*/
    public void sort(int slice_idx) {
        first_slice.sort(slice_idx);
    }

    public int getSliceDefinitionCount() {
        return slices.size();
    }

    public int size() {
        return map.size();
    }

    public int getHash(String s) {
        return map.get(s);
    }

    public int getColumnCount(int measure_columns) {
        return first_slice.getColumnCount(measure_columns);
    }

    public void setMap() {
        map = new HashMap<String, Integer>(getColumnCount(1));
        int current_val = 0;
        for (int i = 0; i < first_slice.sub_slice.size(); i++) {
            Slice slice = first_slice.sub_slice.get(i);
            String map_string = "map-";
            current_val = slice.getMapSlice(map_string, map, current_val);
        }
    }

    public void setGrid(AccumulatorDefinition ad) {
        int measure_metric_column_count = 0;
        Vector<MeasureDefinition> measures_list = null;
        if (ad != null) {
            measure_metric_column_count = ad.getMeasureMetricColumnCount();
            measures_list = ad.getMeasureDefinitions();
        }
        if (measures_on_this_axis) {
            if (ad.isMultiMetric() && ad.isMultiMeasure()) axis_grid = new String[slices.size() + 2][getColumnCount(measure_metric_column_count)]; else if (ad.isMultiMeasure() || ad.isMultiMetric()) axis_grid = new String[slices.size() + 1][getColumnCount(measure_metric_column_count)]; else axis_grid = new String[slices.size()][getColumnCount(measure_metric_column_count)];
        } else axis_grid = new String[slices.size()][getColumnCount(1)];
        int current_slice = 0;
        for (int i = 0; i < first_slice.sub_slice.size(); i++) {
            Slice slice = first_slice.sub_slice.get(i);
            Vector<String> grid_slice_values = new Vector<String>();
            if ((measures_on_this_axis) && (ad.isMultiMeasure() || ad.isMultiMetric())) current_slice = slice.getGridSlice(grid_slice_values, current_slice, axis_grid, ad); else current_slice = slice.getGridSlice(grid_slice_values, current_slice, axis_grid, null);
        }
        if (!is_horizontal) {
            String[][] rotated_grid;
            if (measures_on_this_axis) {
                if (ad.isMultiMeasure() && ad.isMultiMetric()) rotated_grid = new String[getColumnCount(measure_metric_column_count)][slices.size() + 2]; else rotated_grid = new String[getColumnCount(measure_metric_column_count)][slices.size() + 1];
            } else rotated_grid = new String[getColumnCount(1)][slices.size()];
            for (int i = 0; i < axis_grid.length; i++) {
                for (int j = 0; j < axis_grid[i].length; j++) {
                    rotated_grid[j][i] = axis_grid[i][j];
                }
            }
            axis_grid = rotated_grid;
        }
    }

    public Vector<String> getSliceValues(int slice_idx) {
        return first_slice.getSliceValues(slice_idx);
    }

    public Vector<Slice> getSliceElements(int slice_idx) {
        return first_slice.getSliceElements(slice_idx);
    }

    public String toString() {
        StringBuffer str = new StringBuffer("\n ---------- Axis Values ---------------- \n");
        str.append("\tSlice Definitions:\n");
        for (int i = 0; i < slices.size(); i++) {
            SliceDefinition defn = slices.get(i);
            str.append(defn.toString());
        }
        str.append("\n");
        str.append("map, size is " + map.size() + ": " + map.toString());
        str.append("\n");
        str.append(first_slice.toString(1));
        return str.toString();
    }

    public void clear() {
        System.out.println("Axis.java, 214: " + "Clearing axis");
        first_slice = new Slice();
        map = null;
    }

    public SliceDefinition getSliceDefinition(int i) {
        return slices.get(i);
    }

    public void addSlice(String s) {
        slices.add(new SliceDefinition(s));
    }

    public int getSliceCount() {
        return slices.size();
    }

    public int getSpan() {
        return first_slice.getSpan();
    }

    public void setValues(ResultSet rs) {
        try {
            Vector<String> slice_value_list = new Vector<String>();
            for (int i = 0; i < getSliceCount(); i++) {
                rs.beforeFirst();
                while (rs.next()) {
                    slice_value_list.clear();
                    for (int j = 0; j <= i; j++) {
                        slice_value_list.add(rs.getString(getSliceDefinition(j).getDatabaseColumn()));
                    }
                    addValue(slice_value_list);
                }
                sort(i);
            }
        } catch (SQLException sex) {
            System.out.println("Axis.java, 264: " + "SQLException: " + sex.getMessage());
            System.out.println("Axis.java, 265: " + "SQLState: " + sex.getSQLState());
            System.out.println("Axis.java, 266: " + "VendorError: " + sex.getErrorCode());
        }
    }

    public String getMap(ResultSet rs) {
        StringBuffer h = new StringBuffer("map-");
        try {
            for (int i = 0; i < slices.size(); i++) {
                h.append(rs.getString(getSliceDefinition(i).getDatabaseColumn()));
                if (i < (slices.size() - 1)) h.append("-");
            }
        } catch (SQLException sex) {
            System.out.println("Axis.java, 288: " + "SQLException: " + sex.getMessage());
            System.out.println("Axis.java, 289: " + "SQLState: " + sex.getSQLState());
            System.out.println("Axis.java, 290: " + "VendorError: " + sex.getErrorCode());
        }
        return h.toString();
    }

    public int getWidth() {
        return axis_grid[0].length;
    }

    public int getHeight() {
        return axis_grid.length;
    }

    public boolean measuresOnThisAxis() {
        return measures_on_this_axis;
    }

    public void setMeasuresOnThisAxis(boolean on_this_axis) {
        measures_on_this_axis = on_this_axis;
    }

    public int getSliceSize() {
        return slices.size();
    }

    public String[][] getAxisGrid() {
        return axis_grid;
    }
}

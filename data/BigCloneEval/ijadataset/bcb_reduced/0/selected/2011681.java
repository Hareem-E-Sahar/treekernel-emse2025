package org.butu.mapped;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.butu.core.entity.Equivalence;
import org.butu.core.lazy.Lazy;
import org.butu.utils.compare.ComparatorNullable;
import org.butu.utils.compare.CompareUtils;

/**
 * ������� ��������. ��������! � �������� ��������������� ������� ������� �������� ������������
 * ������, ��� ����, ������� �� ����������� (������ ��� ������ ����������� � ������ �������).
 * @author kbakaras
 *
 * @param <R>
 */
public class MappedTable<R extends IMappedTableRow> implements Iterable<R>, Serializable {

    private static final long serialVersionUID = 1L;

    public static class Key {

        public String property;

        public Object value;

        public Key(String property, Object value) {
            super();
            this.property = property;
            this.value = value;
        }
    }

    private static class ClassFields {

        private Class<?> pclass;

        private String[] fields;
    }

    private static Map<String, ClassFields> pclassMap = new HashMap<String, ClassFields>();

    private static ClassPool classPool;

    private boolean simple = true;

    private Class<? extends IMappedTableRow> iRow;

    private Class<?> pclass;

    private ArrayList<R> list;

    private LinkedHashSet<String> columns;

    public static class ColumnNotExistException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    public class MappedTableRow implements IMappedTableRow {

        private static final long serialVersionUID = 1L;

        private HashMap<String, Object> row;

        protected MappedTableRow() {
            row = new HashMap<String, Object>();
        }

        public void setValue(String columnName, Object value) throws ColumnNotExistException {
            columnName = columnName.toLowerCase();
            if (!columns.contains(columnName)) {
                throw new ColumnNotExistException();
            } else {
                row.put(columnName, value);
            }
        }

        public Object getValue(String columnName) throws ColumnNotExistException {
            columnName = columnName.toLowerCase();
            if (!columns.contains(columnName)) {
                throw new ColumnNotExistException();
            } else if (!row.containsKey(columnName)) {
                return null;
            } else {
                return row.get(columnName);
            }
        }
    }

    public class TableRowComparator implements Comparator<IMappedTableRow> {

        private String[] toSort;

        private ComparatorNullable nullable;

        public TableRowComparator(String... toSort) {
            this.toSort = toSort;
            nullable = ComparatorNullable.getInstance();
        }

        public int compare(IMappedTableRow o1, IMappedTableRow o2) {
            int result = 0;
            for (String col : toSort) {
                result = nullable.compare(o1.getValue(col), o2.getValue(col));
                if (result != 0) {
                    break;
                }
            }
            return result;
        }
    }

    public MappedTable() {
        columns = new LinkedHashSet<String>();
        list = new ArrayList<R>();
        iRow = IMappedTableRow.class;
    }

    private ClassFields createPClass() {
        String mappedTableClassName = this.getClass().getName();
        String newClassName = mappedTableClassName + "$$" + iRow.getSimpleName();
        ClassFields classFields = pclassMap.get(newClassName);
        if (classFields == null) {
            try {
                ClassPool pool = getClassPool();
                CtClass ctRowClass = pool.makeClass(newClassName, pool.get(MappedTableRow.class.getName()));
                ctRowClass.addInterface(pool.get(iRow.getName()));
                CtConstructor ctRowConstructor = new CtConstructor(new CtClass[] { classPool.get(mappedTableClassName) }, ctRowClass);
                ctRowConstructor.setBody("super($1);");
                ctRowClass.addConstructor(ctRowConstructor);
                Method[] methods = iRow.getMethods();
                List<String> list = new ArrayList<String>(methods.length / 2);
                for (Method method : methods) {
                    String name = method.getName();
                    if (!name.equals("setValue") && !name.equals("getValue")) {
                        if (name.startsWith("is")) {
                            String returnName = method.getReturnType().getName();
                            CtMethod ctMethod = new CtMethod(classPool.get(returnName), name, null, ctRowClass);
                            ctMethod.setBody("return (" + returnName + ")getValue(\"" + name.substring(2).toLowerCase() + "\");");
                            ctRowClass.addMethod(ctMethod);
                        } else if (name.startsWith("get")) {
                            String returnName = method.getReturnType().getName();
                            CtMethod ctMethod = new CtMethod(classPool.get(returnName), name, null, ctRowClass);
                            ctMethod.setBody("return (" + returnName + ")getValue(\"" + name.substring(3).toLowerCase() + "\");");
                            ctRowClass.addMethod(ctMethod);
                        } else if (name.startsWith("set")) {
                            String fName = name.substring(3).toLowerCase();
                            CtMethod ctMethod = new CtMethod(CtClass.voidType, name, new CtClass[] { classPool.get(method.getParameterTypes()[0].getName()) }, ctRowClass);
                            ctMethod.setBody("setValue(\"" + fName + "\", $1);");
                            ctRowClass.addMethod(ctMethod);
                            list.add(fName);
                        }
                    }
                }
                classFields = new ClassFields();
                classFields.pclass = ctRowClass.toClass(this.getClass().getClassLoader(), this.getClass().getProtectionDomain());
                classFields.fields = new String[list.size()];
                classFields.fields = list.toArray(classFields.fields);
                pclassMap.put(newClassName, classFields);
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (CannotCompileException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return classFields;
    }

    public MappedTable(Class<? extends IMappedTableRow> iRow) {
        columns = new LinkedHashSet<String>();
        list = new ArrayList<R>();
        this.iRow = iRow;
        if (!iRow.equals(IMappedTableRow.class)) {
            ClassFields classFields = createPClass();
            pclass = classFields.pclass;
            addColumns(classFields.fields);
            simple = false;
        }
    }

    public void addColumn(String columnName) {
        columns.add(columnName.toLowerCase());
    }

    public void addColumns(String... columns) {
        for (String column : columns) {
            addColumn(column);
        }
    }

    public R add() {
        R newRow = createRow();
        if (newRow != null) {
            list.add(newRow);
        }
        return newRow;
    }

    public R add(int index) {
        R newRow = createRow();
        if (newRow != null) {
            list.add(index, newRow);
        }
        return newRow;
    }

    @SuppressWarnings("unchecked")
    private R createRow() {
        R newRow = null;
        try {
            if (simple) {
                newRow = (R) new MappedTableRow();
            } else {
                newRow = (R) pclass.getConstructor(this.getClass()).newInstance(this);
            }
        } catch (InvocationTargetException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (NoSuchMethodException e) {
        }
        return newRow;
    }

    public void remove(int index) {
        list.remove(index);
    }

    public void remove(IMappedTableRow row) {
        list.remove(row);
    }

    public Iterator<R> iterator() {
        return list.iterator();
    }

    @SuppressWarnings("unchecked")
    public R[] toArray() {
        R[] array = (R[]) Array.newInstance(iRow, list.size());
        return list.toArray(array);
    }

    public void sort(String... toSort) {
        java.util.Collections.sort(list, new TableRowComparator(toSort));
    }

    public void sortDesc(String... toSort) {
        Comparator<IMappedTableRow> comparatorDesc = new TableRowComparator(toSort) {

            public int compare(IMappedTableRow o1, IMappedTableRow o2) {
                return -1 * super.compare(o1, o2);
            }
        };
        java.util.Collections.sort(list, comparatorDesc);
    }

    /**
     * @param index ������ ������
     * @return ���������� ������ � ��������� ��������
     */
    public R get(int index) {
        return list.get(index);
    }

    /**
     * @param obj ������� ������
     * @return ���������� ������ ������������� �������� ������
     */
    public int indexOf(R obj) {
        return list.indexOf(obj);
    }

    /**
     * ���� �������, ��� ������-���� ��������������, ���������� ���������� ������ �� ��������.
     * @param property
     * @param value
     * @return ������ �������, ���������� ������� �������� � ��������� �������, ���� �������.
     * ���� �������� �� �������, ������������ <b>null</b>.
     */
    public R find(String property, Object value) {
        for (R row : list) {
            if (CompareUtils.nullsOrEquals(row.getValue(property), value)) {
                return row;
            }
        }
        return null;
    }

    public List<R> findAll(Key... keies) {
        Lazy<List<R>> lList = new Lazy<List<R>>() {

            protected List<R> doGet() {
                return new ArrayList<R>();
            }
        };
        for (R row : list) {
            boolean flag = true;
            for (Key key : keies) {
                flag = flag && CompareUtils.nullsOrEquals(row.getValue(key.property), key.value);
                if (!flag) break;
            }
            if (flag) lList.get().add(row);
        }
        if (!lList.isVirgin()) {
            return lList.get();
        } else {
            return null;
        }
    }

    /**
     * ���������� ����� �� �������� �� ���������������.
     * @param property
     * @param value
     * @return
     */
    public R findEquivalent(String property, Equivalence value) {
        for (R row : list) {
            if (value.equivalent(row.getValue(property))) {
                return row;
            }
        }
        return null;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * �������� ���� ����� �������
     */
    public void clear() {
        list.clear();
    }

    public LinkedHashSet<String> getColumns() {
        return columns;
    }

    public Class<? extends IMappedTableRow> getIRow() {
        return iRow;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(simple);
        stream.writeObject(iRow);
        stream.writeObject(list);
        stream.writeObject(columns);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        simple = (Boolean) stream.readObject();
        iRow = (Class<? extends IMappedTableRow>) stream.readObject();
        pclass = createPClass().pclass;
        list = (ArrayList<R>) stream.readObject();
        columns = (LinkedHashSet<String>) stream.readObject();
    }

    private static ClassPool getClassPool() {
        if (classPool == null) {
            classPool = new ClassPool();
            classPool.insertClassPath(new ClassClassPath(MappedTable.class));
        }
        return classPool;
    }
}

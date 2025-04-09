package org.qsari.effectopedia.go;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.ListIterator;
import org.qsari.effectopedia.go.Layout.Details;

public class GraphicObjects implements ProcessableGO {

    public ArrayList<GraphicObject> getList() {
        return list;
    }

    public void setList(ArrayList<GraphicObject> list) {
        this.list = list;
        count = list.size();
    }

    public GraphicObjects() {
        list = new ArrayList<GraphicObject>();
    }

    public GOSynchronizer getSynchronizer() {
        return synchronizer;
    }

    public void setSynchronizer(GOSynchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    public void add(GraphicObject gObject) {
        list.add(gObject);
        count++;
    }

    public void insert(GraphicObject gObject, int index) {
        list.add(index, gObject);
        count++;
    }

    public void delete(int index) {
        list.remove(index);
        count--;
    }

    public void delete(GraphicObject gObject) {
        if (list.remove(gObject)) ;
        count--;
    }

    public void clear() {
        list.clear();
        count = 0;
    }

    public int getCount() {
        return count;
    }

    public int totalHeight() {
        int height = 0;
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) height += iterator.next().height;
        return height;
    }

    public void drawAll(Graphics2D onCanvas) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) iterator.next().draw(onCanvas);
    }

    public GraphicObject isOver(int x, int y) {
        ListIterator<GraphicObject> iterator = list.listIterator();
        GraphicObject activeObject = null;
        while ((activeObject == null) && iterator.hasNext()) {
            activeObject = iterator.next().isOver(x, y);
        }
        return activeObject;
    }

    public void selectBetween(GraphicObject go1, GraphicObject go2) {
        int index1 = list.indexOf(go1);
        int index2 = list.indexOf(go2);
        ListIterator<GraphicObject> iterator = list.listIterator(index1);
        while (iterator.nextIndex() < index2) iterator.next().setSelected(true);
        while (iterator.previousIndex() > index2) iterator.previous().setSelected(true);
        go2.setSelected(true);
    }

    public void setSelected(boolean selected) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) iterator.next().setSelected(selected);
    }

    public void setHeight(int height) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) iterator.next().setHeight(height);
    }

    public void synchronizeHeight(GraphicObjects gos) {
        int min = Math.min(gos.getCount(), count);
        for (int index = 0; index < min; index++) list.get(index).setHeight(gos.getGrapicObject(index).getHeight());
    }

    public void setWidth(int width) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) iterator.next().setWidth(width);
    }

    public void synchronizeWidth(GraphicObjects gos) {
        int min = Math.min(gos.getCount(), count);
        for (int index = 0; index < min; index++) list.get(index).setWidth(gos.getGrapicObject(index).getWidth());
    }

    public void setDetails(Details details) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) iterator.next().setDetails(details);
    }

    public void synchronizeDetails(GraphicObjects gos) {
        if (gos.getCount() == count) for (int index = count - 1; index >= 0; index--) list.get(index).setDetails(gos.getGrapicObject(index).getDetails());
    }

    public void synchronizeX(GraphicObjects gos) {
        if (gos.getCount() == count) for (int index = count - 1; index >= 0; index--) list.get(index).setX(gos.getGrapicObject(index).getX());
    }

    public void synchronizeY(GraphicObjects gos) {
        if (gos.getCount() == count) for (int index = count - 1; index >= 0; index--) list.get(index).setY(gos.getGrapicObject(index).getY());
    }

    public GraphicObject getGrapicObject(int index) {
        return list.get(index);
    }

    public GraphicObject getBaseObject() {
        return list.get(getBaseIndex());
    }

    public int getBaseIndex() {
        return baseIndex;
    }

    public void setBaseIndex(int baseIndex) {
        this.baseIndex = baseIndex;
    }

    public void setBaseIndex(GraphicObject baseObject) {
        this.baseIndex = list.indexOf(baseObject);
    }

    public void setBaseIndex() {
        this.baseIndex = list.size() / 2;
    }

    public int getGraphicObjectIndex(GraphicObject go) {
        return list.indexOf(go);
    }

    public void process(int action, Object value) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) iterator.next().process(action, value);
    }

    public void processSelected(int action, Object value) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) iterator.next().processSelected(action, value);
    }

    public void processAfterIndex(int action, Object value, int index) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(index); iterator.hasNext(); ) iterator.next().process(action, value);
    }

    public void processBeforeIndex(int action, Object value, int index) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(index); iterator.hasPrevious(); ) iterator.previous().process(action, value);
    }

    public void setSelected(int x, int y, int width, int height) {
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) iterator.next().setSelected(x, y, width, height);
    }

    public GraphicObject getNext(GraphicObject ofGO) {
        int index = list.indexOf(ofGO);
        return getGrapicObject(index + 1);
    }

    public GraphicObject getPrevious(GraphicObject ofGO) {
        int index = list.indexOf(ofGO);
        return getGrapicObject(index - 1);
    }

    public int maxY() {
        if (list.size() == 0) return 0;
        GraphicObject lastGO = list.get(list.size() - 1);
        return lastGO.getY() + lastGO.getHeight();
    }

    public int maxX() {
        if (list.size() == 0) return 0;
        GraphicObject lastGO = list.get(list.size() - 1);
        return lastGO.getX() + lastGO.getWidth();
    }

    public int lastIndexBeforeX(int x) {
        int low = 0;
        int high = list.size() - 1;
        int index = -1;
        while (low <= high) {
            index = (low + high) / 2;
            if (list.get(index).getX() < x) low = index + 1; else if (list.get(index).getX() > x) high = index - 1; else break;
        }
        return (list.get(index).getX() > x) ? index - 1 : index;
    }

    public int lastIndexBeforeY(int y) {
        int low = 0;
        int high = list.size() - 1;
        int index = -1;
        while (low <= high) {
            index = (low + high) / 2;
            if (list.get(index).getY() < y) low = index + 1; else if (list.get(index).getY() > y) high = index - 1; else break;
        }
        return (list.get(index).getY() > y) ? index - 1 : index;
    }

    public void removeSelected() {
        int fisrtSelectedIndex = -1;
        for (ListIterator<GraphicObject> iterator = list.listIterator(); iterator.hasNext(); ) if (iterator.next().isSelected()) {
            iterator.previous();
            fisrtSelectedIndex = iterator.nextIndex();
            break;
        }
        if (fisrtSelectedIndex != -1) {
            ArrayList<GraphicObject> updatedList = new ArrayList<GraphicObject>(list.size());
            updatedList.addAll(list.subList(0, fisrtSelectedIndex));
            for (ListIterator<GraphicObject> iterator = list.listIterator(fisrtSelectedIndex + 1); iterator.hasNext(); ) {
                GraphicObject go = iterator.next();
                if (!go.isSelected()) updatedList.add(go);
            }
            setList(updatedList);
        }
    }

    protected GOSynchronizer synchronizer = null;

    private ArrayList<GraphicObject> list;

    private int count = 0;

    private int baseIndex;
}

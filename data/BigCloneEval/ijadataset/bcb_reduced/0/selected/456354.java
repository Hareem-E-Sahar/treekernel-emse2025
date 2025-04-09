package com.dukesoftware.viewlon3.data.internal;

import java.util.ArrayList;
import java.util.List;
import com.dukesoftware.utils.math.SimpleVector3d;

/**
 * 実世界の物体(インスタンス)を表すクラスです。
 * 
 * @see DataObject
 * @see ClassObject
 * 
 * 
 *
 *
 */
public class RealObject extends DataObject {

    private static final long serialVersionUID = -5251985009041672923L;

    private List<Integer> sensor_list;

    protected String front_position_id;

    protected String back_position_id;

    protected String imagePath;

    private ClassObject class_object;

    public static final String SHAPE_COLUMN = "COLUMN";

    public static final String SHAPE_BOX = "BOX";

    public static final String SHAPE_UNKNOWN = "UNKNOWN";

    protected SimpleVector3d direction;

    private boolean check_pos;

    protected boolean check_position_sensor;

    protected double sizex;

    protected double sizey;

    protected double sizez;

    protected String shape;

    public RealObject(boolean use, String real_obj_name, ClassObject class_object) {
        super(use, real_obj_name);
        sensor_list = new ArrayList<Integer>();
        setSize(0, 0, 0);
        shape = SHAPE_UNKNOWN;
        front_position_id = null;
        back_position_id = null;
        direction = new SimpleVector3d(0, 0, 0);
        this.class_object = class_object;
        check_pos = true;
        check_position_sensor = false;
    }

    public void setFrontPositionID(String id) {
        front_position_id = id;
    }

    public void setBackPositionID(String id) {
        back_position_id = id;
    }

    public ClassObject getClassObject() {
        return class_object;
    }

    public String getFrontPositionID() {
        return (front_position_id);
    }

    public String getBackPositionID() {
        return (back_position_id);
    }

    public void setSize(double sizex, double sizey, double sizez) {
        this.sizex = sizex;
        this.sizey = sizey;
        this.sizez = sizez;
    }

    public void setDirection(double x, double y) {
        direction.set(x, y, 0, 1);
    }

    public double getSizeX() {
        return sizex;
    }

    public double getSizeY() {
        return sizey;
    }

    public double getSizeZ() {
        return sizez;
    }

    /**
	 * オブジェクトの形状を返します。
	 * @return
	 */
    public String getShape() {
        return shape;
    }

    /**
	 * オブジェクトの形状を設定します。
	 * @param shape
	 */
    public void setShape(String shape) {
        this.shape = shape;
    }

    public boolean checkCoveredRelation(double inx, double iny, double inz, double threshold) {
        if (shape.equals(SHAPE_COLUMN)) {
            double r = (sizex + threshold) / 2;
            return (((inx - x) * (inx - x) + (iny - y) * (iny - y) <= r * r) && (inz > z));
        } else if (shape.equals(SHAPE_BOX)) {
            double largex = x + (threshold + sizex) / 2;
            double smallx = x - (threshold + sizex) / 2;
            double largey = y + (threshold + sizey) / 2;
            double smally = y - (threshold + sizey) / 2;
            return ((inx < largex) && (inx > smallx) && (iny < largey) && (iny > smally) && (inz > z));
        }
        return false;
    }

    public List<Integer> returnSensorList() {
        return (sensor_list);
    }

    public boolean checkPositionSensor() {
        return check_position_sensor;
    }

    public SimpleVector3d getDirection() {
        return direction;
    }

    public void setPositionSensor(boolean flag) {
        this.check_position_sensor = flag;
    }

    public Object clone() {
        RealObject obj = (RealObject) super.clone();
        obj.sensor_list = new ArrayList<Integer>(sensor_list);
        obj.setSize(sizex, sizey, sizez);
        obj.shape = shape;
        obj.front_position_id = front_position_id;
        obj.back_position_id = back_position_id;
        obj.direction = (SimpleVector3d) direction.clone();
        obj.set(x, y, z);
        obj.class_object = class_object;
        obj.check_pos = check_pos;
        obj.check_position_sensor = check_position_sensor;
        return obj;
    }

    public void setClassObject(ClassObject cobj) {
        this.class_object = cobj;
    }

    public boolean isCheck_pos() {
        return check_pos;
    }

    public void setCheck_pos(boolean check_pos) {
        this.check_pos = check_pos;
    }

    public String getClassName() {
        if (class_object != null) return class_object.getName();
        return null;
    }
}

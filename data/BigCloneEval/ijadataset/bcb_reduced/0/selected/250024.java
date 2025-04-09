package com.mainatom.af;

import com.mainatom.utils.*;
import java.lang.reflect.*;

/**
 * Объявление объекта. Экземпляр такого класса содержит информацию,
 * с использованием которой можно создать экземпляр конкретного объекта
 */
public class DeclareObject implements INamed {

    private String _name;

    private Class _cls;

    private Object _ownerObject;

    private AObject _prototype;

    private ListObject<IInitObject> _initers;

    public DeclareObject makeClone() {
        DeclareObject res = new DeclareObject();
        res._cls = _cls;
        res._name = _name;
        res._ownerObject = _ownerObject;
        res._prototype = _prototype;
        if (_initers != null) {
            res._initers = new ListObject<IInitObject>();
            res._initers.addAll(_initers);
        }
        return res;
    }

    public String getName() {
        return _name == null ? "" : _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public Class getCls() {
        return _cls;
    }

    public void setCls(Class cls) {
        _cls = cls;
    }

    /**
     * Прототип объекта. Если присутсвует, то экземпляр создается как getPrototype().createSame()
     *
     * @return
     */
    public AObject getPrototype() {
        return _prototype;
    }

    public void setPrototype(AObject prototype) {
        _prototype = prototype;
    }

    /**
     * Объект владелец. Если класс - внутренний - то его экземпляр будет создан через
     * владельца.
     *
     * @return
     */
    public Object getOwnerObject() {
        return _ownerObject;
    }

    public void setOwnerObject(Object ownerObject) {
        _ownerObject = ownerObject;
    }

    public Object createInst() {
        try {
            Object z;
            if (_prototype != null) {
                z = _prototype.createSame();
            } else if (_ownerObject == null) {
                z = getCls().newInstance();
            } else {
                Constructor[] c = getCls().getConstructors();
                z = c[0].newInstance(_ownerObject);
            }
            if (_initers != null) {
                for (IInitObject initer : _initers) {
                    initer.initObject(z);
                }
            }
            return z;
        } catch (Exception e) {
            throw new MfError(e, "Ошибка при создании объекта класса [{0}]", getIdObject());
        }
    }

    public String getIdObject() {
        String s = getName();
        if (_cls != null) {
            s = s + "," + _cls;
        }
        if (_prototype != null) {
            s = s + "," + _prototype.getClass();
        }
        return s;
    }

    /**
     * Является ли владельцем тега
     *
     * @param tagName имя тега
     * @return
     */
    public boolean isOwnerTag(String tagName) {
        ADef.ClazzInfo ci = ADef.getInst().getClazzInfo(getCls());
        if (ci == null) {
            return false;
        }
        return ci.getOwnerTag(tagName) == getCls();
    }

    /**
     * Добавить инициализатор для объекта
     *
     * @param initer
     */
    public void addIniter(IInitObject initer) {
        if (_initers == null) {
            _initers = new ListObject<IInitObject>();
        }
        _initers.add(initer);
    }
}

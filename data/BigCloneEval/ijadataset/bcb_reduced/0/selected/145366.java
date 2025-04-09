package deduced.controller;

import java.lang.reflect.Constructor;
import java.util.*;
import assertion.AssertUtility;
import deduced.*;

public class DefaultControllerFactory implements ControllerFactory {

    public static final ControllerFactory DEFAULT = new DefaultControllerFactory();

    private Map _controllerCreationMap = new IdentityHashMap();

    private Map _listControllerCreationMap = new IdentityHashMap();

    private ModelFactory _modelFactory = new DefaultModelFactory();

    public PropertyCollection createModel(PropertyType type) {
        return _modelFactory.createModel(type);
    }

    public PropertyController createPropertyController(PropertyCollectionController parentController, Object key) {
        DirectPropertyController retVal = new DirectPropertyController();
        retVal.setPropertyKey(key);
        PropertyCollection controlledCollection = parentController.getControlledCollection();
        retVal.setControlledCollection(controlledCollection);
        boolean isChangeable = !controlledCollection.areKeysAssignedAutomatically();
        retVal.setIsRemoveable(parentController.isAddAvailable());
        retVal.setIsChangeable(isChangeable);
        Object addedObject = parentController.getChildPropertyControllerMap().addPropertyInstance(key, null, retVal);
        AssertUtility.assertSame(retVal, addedObject);
        return retVal;
    }

    public PropertyCollectionController createController(PropertyCollectionController parentController, PropertyCollection model) {
        if (model == null) {
            throw new NullPointerException();
        }
        Class controllerClass = (Class) _controllerCreationMap.get(model.getType());
        PropertyCollectionController createdController = null;
        if (controllerClass == null) {
            if (model instanceof PropertyMap) {
                PropertyMap mapModel = (PropertyMap) model;
                PropertyInstance fixedInstance = mapModel.getFixedInstance();
                if (fixedInstance != null) {
                    createdController = createListController(parentController, model, fixedInstance.getInstanceType());
                    return createdController;
                }
            }
        } else {
            try {
                Class[] classArgs = null;
                Constructor constructor = controllerClass.getConstructor(classArgs);
                Object[] objectArgs = null;
                createdController = (PropertyCollectionController) constructor.newInstance(objectArgs);
            } catch (Exception e) {
                AssertUtility.exception(e);
            }
        }
        if (createdController == null) {
            createdController = new DirectPropertyCollectionController();
        }
        Object addedObject = parentController.getChildCollectionControllerMap().addPropertyInstance(parentController.getControlledCollection().getInstanceKey(model), null, createdController);
        AssertUtility.assertSame(createdController, addedObject);
        createdController.setIsAddAvailable(false);
        createdController.setIsDeleteAvailable(true);
        createdController.setControlledCollection(model);
        return createdController;
    }

    public PropertyCollectionController createListController(PropertyCollectionController parentController, PropertyCollection model, PropertyType typeInList) {
        Class controllerClass = (Class) _listControllerCreationMap.get(typeInList);
        PropertyCollectionController createdController = null;
        if (controllerClass == null) {
            createdController = new DirectPropertyCollectionController();
        } else {
            try {
                Class[] classArg = null;
                Constructor constructor = controllerClass.getConstructor(classArg);
                Object[] objectArg = null;
                createdController = (PropertyCollectionController) constructor.newInstance(objectArg);
            } catch (Exception e) {
                AssertUtility.exception(e);
            }
        }
        if (createdController == null) {
            createdController = new DirectPropertyCollectionController();
        }
        Object addedObject = parentController.getChildCollectionControllerMap().addPropertyInstance(parentController.getControlledCollection().getInstanceKey(model), null, createdController);
        AssertUtility.assertSame(createdController, addedObject);
        createdController.setIsAddAvailable(true);
        createdController.setIsDeleteAvailable(false);
        createdController.setControlledCollection(model);
        return createdController;
    }

    /**
     * addControllerType
     * 
     * @param controllerClass
     * @param modelType
     */
    public void addControllerType(Class controllerClass, PropertyType modelType) {
        if (controllerClass == null) {
            throw new NullPointerException();
        }
        if (modelType == null) {
            throw new NullPointerException();
        }
        _controllerCreationMap.put(modelType, controllerClass);
    }

    /**
     * addControllerType
     * 
     * @param controllerClass
     * @param modelType
     */
    public void addListControllerType(Class controllerClass, PropertyType modelType) {
        if (controllerClass == null) {
            throw new NullPointerException();
        }
        if (modelType == null) {
            throw new NullPointerException();
        }
        _listControllerCreationMap.put(modelType, controllerClass);
    }

    public void addModelType(Class modelClass, PropertyType modelType) {
        _modelFactory.addModelType(modelClass, modelType);
    }

    public List getDerivedModelList(PropertyType type) {
        return _modelFactory.getDerivedModelList(type);
    }
}

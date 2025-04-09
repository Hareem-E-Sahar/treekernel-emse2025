package halo.web.action;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionMappingCreator {

    private DefActionFinder actionFinder = new DefActionFinder();

    /**
	 * ActionMapping缓存
	 */
    private final Map<String, ActionMapping> mappingMap = new HashMap<String, ActionMapping>();

    /**
	 * 寻找与uri对应的ActionMapper. uri与{@link ActionMapping} 一对一匹配.
	 * 
	 * @param mappingUri
	 * @return
	 * @throws NoActionException
	 * @throws NoSuchMethodException
	 */
    public ActionMapping getActionMapping(String mappingUri) throws NoActionException, NoSuchMethodException {
        ActionMapping mapping = mappingMap.get(mappingUri);
        if (mapping == null) {
            try {
                mapping = parseActionMapping(mappingUri);
                mappingMap.put(mappingUri, mapping);
            } catch (NoActionException e) {
                throw e;
            } catch (NoSuchMethodException e) {
                throw e;
            }
        }
        return mapping;
    }

    /**
	 * 解析uri,获得ActionMapping，"_"作为Action与方法名的分割.例如/user_list,
	 * 对应的action为UserAction，list是action中的一个方法。
	 * 
	 * @param mappingUri
	 * @return
	 * @throws NoActionException
	 * @throws NoSuchMethodException
	 */
    private synchronized ActionMapping parseActionMapping(String mappingUri) throws NoActionException, NoSuchMethodException {
        ActionMapping mapping = mappingMap.get(mappingUri);
        if (mapping != null) {
            return mapping;
        }
        int idx = mappingUri.lastIndexOf('_');
        mapping = new ActionMapping();
        if (idx != -1) {
            String methodName = mappingUri.substring(idx + 1, mappingUri.length());
            String actionName = mappingUri.substring(0, idx);
            mapping.setAction(this.actionFinder.findAction(actionName));
            mapping.setMappingUri(mappingUri);
            mapping.setActionName(actionName);
            mapping.setMethodName(methodName);
            mapping.setActionMethod(this.getMethod(mapping.getAction(), methodName));
            mapping.setAsmAction(this.createAsmAction(mapping));
        } else {
            mapping.setAction(this.actionFinder.findAction(mappingUri));
            mapping.setActionName(mappingUri);
        }
        return mapping;
    }

    private Method getMethod(Action action, String methodName) throws NoSuchMethodException {
        Method method = action.getClass().getMethod(methodName, new Class[] { HkRequest.class, HkResponse.class });
        return method;
    }

    /**
	 * 动态创建action子类，子类调用父类指定的方法
	 * 
	 * @param actionMapping
	 * @return
	 */
    private Action createAsmAction(ActionMapping actionMapping) {
        ASMActionCreater asmActionCreater = new ASMActionCreater();
        Class<Action> clazz = asmActionCreater.createASMAction(actionMapping);
        try {
            Object obj = clazz.getConstructor().newInstance();
            Action action = (Action) obj;
            Field field = action.getClass().getDeclaredField("action");
            field.setAccessible(true);
            field.set(action, actionMapping.getAction());
            return action;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setScanPathList(List<String> list) {
        this.actionFinder.setScanPathList(list);
    }
}

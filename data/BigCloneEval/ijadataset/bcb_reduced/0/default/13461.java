import com.nanosn.polls.Poll;
import com.nanosn.reflection.Conversion;
import com.nanosn.web.framework.rest.IRest;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Id;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author selkhateeb
 */
public class Polls implements IRest {

    private Class clazz = Poll.class;

    private Writer writer;

    public void WriteResource(List<String> uri) {
        if (uri.isEmpty()) {
            this.getAllDataAsJSON();
        } else {
            ProcessUri(uri);
        }
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    private void ProcessUri(List<String> uri) {
        System.out.println("ProcessURI " + uri.get(0));
        if (uri.get(0).equals("structure")) {
            writeStructure();
        } else {
            Select(uri.get(0));
        }
    }

    @SuppressWarnings("unchecked")
    private void getAllDataAsJSON() {
        List<Poll> polls = Poll.Select.All();
        List<Map> mapList = new ArrayList<Map>();
        for (Poll poll : polls) {
            Map map = new HashMap();
            for (Field classFeild : poll.getClass().getDeclaredFields()) {
                Annotation ann = classFeild.getAnnotation(Column.class);
                if (ann == null) {
                    continue;
                }
                String methodName = "get" + classFeild.getName().substring(0, 1).toUpperCase() + classFeild.getName().substring(1);
                Method m;
                try {
                    m = poll.getClass().getMethod(methodName, new Class[] {});
                    map.put(classFeild.getName(), m.invoke(poll, new Object[] {}));
                } catch (Exception ex) {
                    Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            mapList.add(map);
        }
        try {
            JSONValue.writeJSONString(mapList, this.writer);
        } catch (IOException ex) {
            Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeStructure() {
        List<Map> array = new ArrayList<Map>();
        Map field;
        for (Field classFeild : clazz.getDeclaredFields()) {
            Annotation ann = classFeild.getAnnotation(Column.class);
            if (ann == null) {
                continue;
            }
            Column column = (Column) ann;
            field = new HashMap();
            field.put("name", column.name());
            field.put("field", classFeild.getName());
            field.put("width", 8);
            array.add(field);
        }
        try {
            JSONValue.writeJSONString(array, this.writer);
        } catch (IOException ex) {
            Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Insert(String JSONitem) {
        Method[] methods = clazz.getDeclaredMethods();
        Poll poll = new Poll();
        try {
            Map obj = (Map) new JSONParser().parse(JSONitem);
            for (Field classFeild : clazz.getDeclaredFields()) {
                Annotation ann = classFeild.getAnnotation(Column.class);
                if (ann == null) {
                    continue;
                }
                if (null == obj.get(classFeild.getName())) {
                    continue;
                }
                String methodName = "set" + classFeild.getName().substring(0, 1).toUpperCase() + classFeild.getName().substring(1);
                Method m = null;
                try {
                    for (Method method : methods) {
                        if (method.getName().equals(methodName)) {
                            Logger.getLogger(Polls.class.getName()).info(methodName);
                            m = method;
                            break;
                        }
                    }
                    String s = obj.get(classFeild.getName()).toString();
                    Object parameterObject = Conversion.Cast(s, m.getParameterTypes()[0]);
                    m.invoke(poll, new Object[] { parameterObject });
                } catch (Exception ex) {
                    Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            poll.Save();
        } catch (Exception ex) {
            Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Update(String primaryKey, String JSONitem) {
        Method[] methods = clazz.getDeclaredMethods();
        Poll poll = new Poll();
        try {
            Map obj = (Map) new JSONParser().parse(JSONitem);
            for (Field classFeild : clazz.getDeclaredFields()) {
                Annotation ann = classFeild.getAnnotation(Column.class);
                if (ann == null) {
                    continue;
                }
                if (null == obj.get(classFeild.getName())) {
                    continue;
                }
                ann = classFeild.getAnnotation(Id.class);
                boolean isPrimaryKey = false;
                if (ann != null) {
                    isPrimaryKey = true;
                }
                String methodName = "set" + classFeild.getName().substring(0, 1).toUpperCase() + classFeild.getName().substring(1);
                Method m = null;
                try {
                    for (Method method : methods) {
                        if (method.getName().equals(methodName)) {
                            Logger.getLogger(Polls.class.getName()).info(methodName);
                            m = method;
                            break;
                        }
                    }
                    String s = obj.get(classFeild.getName()).toString();
                    Object parameterObject;
                    if (isPrimaryKey) {
                        parameterObject = Conversion.Cast(primaryKey, m.getParameterTypes()[0]);
                    } else {
                        parameterObject = Conversion.Cast(s, m.getParameterTypes()[0]);
                    }
                    m.invoke(poll, new Object[] { parameterObject });
                } catch (Exception ex) {
                    Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            poll.Update();
        } catch (Exception ex) {
            Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Delete(String itemPK) {
        Poll poll = new Poll();
        Field primaryKeyField = null;
        for (Field field : clazz.getDeclaredFields()) {
            Annotation ann = field.getAnnotation(Id.class);
            if (ann != null) {
                primaryKeyField = field;
                break;
            }
        }
        Method m = null;
        try {
            String methodName = "set" + primaryKeyField.getName().substring(0, 1).toUpperCase() + primaryKeyField.getName().substring(1);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    Logger.getLogger(Polls.class.getName()).info(methodName);
                    m = method;
                    break;
                }
            }
            Object parameterObject = Conversion.Cast(itemPK, m.getParameterTypes()[0]);
            m.invoke(poll, new Object[] { parameterObject });
        } catch (Exception ex) {
            Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            poll.Delete();
        } catch (Exception ex) {
            Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Select(String primaryKey) {
        List<Poll> polls = Poll.Select.Where("Poll.id=" + primaryKey);
        Poll poll = polls.get(0);
        Map map = new HashMap();
        for (Field classFeild : poll.getClass().getDeclaredFields()) {
            Annotation ann = classFeild.getAnnotation(Column.class);
            if (ann == null) {
                continue;
            }
            String methodName = "get" + classFeild.getName().substring(0, 1).toUpperCase() + classFeild.getName().substring(1);
            Method m;
            try {
                m = poll.getClass().getMethod(methodName, new Class[] {});
                map.put(classFeild.getName(), m.invoke(poll, new Object[] {}));
            } catch (Exception ex) {
                Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            JSONValue.writeJSONString(map, this.writer);
        } catch (IOException ex) {
            Logger.getLogger(Polls.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvokeMethod {

    /**
	 * 在这个类里面存在有copy（）方法，根据指定的方法的参数去 构造一个新的对象的拷贝 并将他返回
	 * 
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
    public Object copy(Object obj) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class classType = obj.getClass();
        System.out.println("该对象的类型是：" + classType.toString());
        Object objectCopy = classType.getConstructor(new Class[] {}).newInstance(new Object[] {});
        Field[] fields = classType.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            Field field = fields[i];
            String fieldName = field.getName();
            String stringLetter = fieldName.substring(0, 1).toUpperCase();
            String getName = "get" + stringLetter + fieldName.substring(1);
            String setName = "set" + stringLetter + fieldName.substring(1);
            Method getMethod = classType.getMethod(getName, new Class[] {});
            Method setMethod = classType.getMethod(setName, new Class[] { field.getType() });
            Object value = getMethod.invoke(obj, new Object[] {});
            System.out.println(fieldName + " :" + value);
            setMethod.invoke(objectCopy, new Object[] { value });
        }
        return objectCopy;
    }

    public static void main(String[] args) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Customer customer = new Customer();
        customer.setName("hejianjie");
        customer.setId(new Long(1234));
        customer.setAge(19);
        Customer customer2 = null;
        customer2 = (Customer) new InvokeMethod().copy(customer);
        System.out.println(customer.getName() + " " + customer2.getAge() + " " + customer2.getId());
        System.out.println(customer);
        System.out.println(customer2);
    }
}

class Customer {

    private Long id;

    private String name;

    private int age;

    public Customer() {
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

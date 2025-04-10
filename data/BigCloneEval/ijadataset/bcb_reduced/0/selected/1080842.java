package net.ponec.jworksheet.report;

import net.ponec.jworksheet.bo.Project;
import net.ponec.jworksheet.bo.TaskType;
import net.ponec.jworksheet.bo.WorkDay;
import net.ponec.jworksheet.bo.item.YearMonthDay;
import org.ujorm.UjoProperty;
import org.ujorm.implementation.map.MapUjo;
import net.ponec.jworksheet.bo.Event;

/**
 * Task Group
 * @author Pavel Ponec
 */
@SuppressWarnings("unchecked")
public class TaskGroup extends MapUjo {

    public static final UjoProperty<TaskGroup, Project> P_PROJ = newProperty(Event.P_PROJ, -3);

    public static final UjoProperty<TaskGroup, TaskType> P_TASK = newProperty(Event.P_TASK, -2);

    public static final UjoProperty<TaskGroup, YearMonthDay> P_DAY = newProperty(WorkDay.P_DATE, -1);

    public static final UjoProperty<TaskGroup, Integer> P_MONTH = newProperty("Month", Integer.class);

    public static final UjoProperty<TaskGroup, Integer> P_YEAR = newProperty("Year", Integer.class);

    /** A total time in minutes. */
    private int totalTime = 0;

    /** Creates a new instance of TaskGroup */
    public TaskGroup(Event event) {
        if (event != null) {
            init(event);
        }
    }

    void init(Event event) {
        writeValue(P_PROJ, event.readValue(Event.P_PROJ));
        writeValue(P_TASK, event.readValue(Event.P_TASK));
    }

    /** Add a new time. */
    public void addTime(short time) {
        totalTime += time;
    }

    /** Get total time */
    public int getTime() {
        return totalTime;
    }

    /** Get business time. */
    public int getBusinessTime() {
        Project proj = get(P_PROJ);
        return (proj != null && proj.get(Project.P_PRIVATE)) ? 0 : totalTime;
    }

    /** Read a value */
    @Override
    public Object readValue(UjoProperty property) {
        if (P_MONTH == property || P_YEAR == property) {
            YearMonthDay day = get(P_DAY);
            int type = P_MONTH == property ? YearMonthDay.TYPE_MONTH : YearMonthDay.TYPE_YEAR;
            return day != null ? day.get(type) : null;
        }
        return super.readValue(property);
    }

    @SuppressWarnings("unchecked")
    public <UJO extends TaskGroup, VALUE> VALUE get(UjoProperty<UJO, VALUE> up) {
        return up.getValue((UJO) this);
    }

    @SuppressWarnings("unchecked")
    public <UJO extends TaskGroup, VALUE> UJO set(UjoProperty<UJO, VALUE> up, VALUE value) {
        up.setValue((UJO) this, value);
        return (UJO) this;
    }
}

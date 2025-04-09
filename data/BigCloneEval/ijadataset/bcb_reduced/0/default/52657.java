import java.util.*;
import java.lang.reflect.*;

class TaskManager {

    private Task currentTask;

    private Person person;

    private VirtualMars mars;

    private Class[] generalTasks;

    public TaskManager(Person person, VirtualMars mars) {
        this.person = person;
        this.mars = mars;
        currentTask = null;
        try {
            generalTasks = new Class[] { TaskRelax.class, TaskDrive.class };
        } catch (Exception e) {
            System.out.println("TaskManager.constructor(): " + e.toString());
        }
    }

    public boolean hasCurrentTask() {
        if (currentTask != null) return true; else return false;
    }

    public String getCurrentTaskDescription() {
        if (currentTask != null) return currentTask.getDescription(); else return null;
    }

    public String getCurrentPhase() {
        if (currentTask != null) return currentTask.getPhase(); else return null;
    }

    public String getCurrentSubPhase() {
        if (currentTask != null) return currentTask.getSubPhase(); else return null;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void takeAction(int seconds) {
        if ((currentTask == null) || currentTask.isDone()) {
            getNewTask();
        }
        currentTask.doTask(seconds);
    }

    private void getNewTask() {
        Vector probableTasks = new Vector();
        Vector weights = new Vector();
        Class[] parametersForFindingMethod = { Person.class, VirtualMars.class };
        Object[] parametersForInvokingMethod = { person, mars };
        for (int x = 0; x < generalTasks.length; x++) {
            try {
                Method probability = generalTasks[x].getMethod("getProbability", parametersForFindingMethod);
                int weight = ((Integer) probability.invoke(null, parametersForInvokingMethod)).intValue();
                if (weight > 0) {
                    probableTasks.addElement(generalTasks[x]);
                    weights.addElement(new Integer(weight));
                }
            } catch (Exception e) {
                System.out.println("TaskManager.getNewTask() (1): " + e.toString());
            }
        }
        int totalWeight = 0;
        for (int x = 0; x < weights.size(); x++) totalWeight += ((Integer) weights.elementAt(x)).intValue();
        int r = (int) Math.round(Math.random() * (double) totalWeight);
        int tempWeight = ((Integer) weights.elementAt(0)).intValue();
        int taskNum = 0;
        while (tempWeight < r) {
            taskNum++;
            tempWeight += ((Integer) weights.elementAt(taskNum)).intValue();
        }
        try {
            Constructor construct = ((Class) probableTasks.elementAt(taskNum)).getConstructor(parametersForFindingMethod);
            currentTask = (Task) construct.newInstance(parametersForInvokingMethod);
        } catch (Exception e) {
            System.out.println("TaskManager.getNewTask() (2): " + e.toString());
        }
    }
}

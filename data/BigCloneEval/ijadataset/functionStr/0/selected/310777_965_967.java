public class Test {    public CreateAction getCreateAction() {
        return new CreateAction(new GraphicalViewer[] { getTasksViewer(), getActivitiesViewer() });
    }
}
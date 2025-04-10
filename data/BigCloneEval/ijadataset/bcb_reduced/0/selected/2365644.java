package org.remus.infomngmnt.ccalendar;

import java.util.Date;
import org.aspencloud.calypso.ui.workbench.views.calendar.actions.CreateAction;
import org.aspencloud.calypso.ui.workbench.views.calendar.actions.ShowGridAction;
import org.aspencloud.calypso.ui.workbench.views.calendar.actions.ZoomInAction;
import org.aspencloud.calypso.ui.workbench.views.calendar.actions.ZoomOutAction;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.remus.infomgmnt.provider.CalendarContentProvider;
import org.remus.infomngmnt.calendar.model.EndEvent;
import org.remus.infomngmnt.calendar.model.ModelFactory;
import org.remus.infomngmnt.calendar.model.StartEvent;
import org.remus.infomngmnt.calendar.model.Task;
import org.remus.infomngmnt.calendar.model.Tasklist;
import org.remus.infomngmnt.ccalendar.actions.EditModelAction;
import org.remus.infomngmnt.ccalendar.actions.MoveToTodayAction;
import org.remus.infomngmnt.ccalendar.actions.RemoveAction;

/**
 * @author jeremy
 * 
 */
public class CalendarView extends ViewPart implements ISelectionChangedListener {

    public static final String ID = "org.aspencloud.calypso.ui.workbench.views.calendar.CalendarView";

    private CCalendar calendar;

    private ZoomInAction zoomInAction;

    private ZoomOutAction zoomOutAction;

    private ShowGridAction gridAction;

    private CreateAction createAction;

    private EditModelAction editAction;

    private MoveToTodayAction moveAction;

    private RemoveAction removeAction;

    public CalendarView() {
    }

    @Override
    public void createPartControl(final Composite parent) {
        createContents(parent);
        createActions();
        createToolBar();
        createContextMenu();
        createPullDownMenu();
        Task task = ModelFactory.eINSTANCE.createTask();
        task.setName("TEST");
        Task task2 = ModelFactory.eINSTANCE.createTask();
        task2.setName("TEST2");
        task2.setProgress(4);
        StartEvent createStartEvent = ModelFactory.eINSTANCE.createStartEvent();
        createStartEvent.setDate(new Date());
        createStartEvent.setName("Event123");
        EndEvent createEndEvent = ModelFactory.eINSTANCE.createEndEvent();
        createEndEvent.setDate(new Date(System.currentTimeMillis() + 3600000));
        task.setStart(createStartEvent);
        task.setEnd(createEndEvent);
        task.setDetails("Hallo Welt...");
        task.setProgress(80);
        Tasklist createPerson = ModelFactory.eINSTANCE.createTasklist();
        StartEvent createStartEvent2 = ModelFactory.eINSTANCE.createStartEvent();
        createStartEvent2.setDate(new Date());
        createStartEvent2.setName("Event123");
        EndEvent createEndEvent2 = ModelFactory.eINSTANCE.createEndEvent();
        createEndEvent2.setDate(new Date(System.currentTimeMillis() + 2600));
        task2.setStart(createStartEvent2);
        task2.setEnd(createEndEvent2);
        createPerson.getTasks().add(task2);
        this.calendar.setInput(createPerson);
        this.calendar.addSelectionChangedListener(this);
    }

    private void createContents(final Composite parent) {
        parent.setLayout(new FillLayout());
        this.calendar = new CCalendar(parent, SWT.BORDER);
        this.calendar.setContentProvider(new CalendarContentProvider());
        this.calendar.setCalendarToWeekContaining(new Date());
        this.calendar.addOpenListener(new Listener() {

            public void handleEvent(final Event event) {
                Task task = (Task) event.data;
                if (task != null) {
                }
            }
        });
        getSite().setSelectionProvider(this.calendar);
    }

    private void createActions() {
        this.zoomInAction = new ZoomInAction();
        this.zoomOutAction = new ZoomOutAction();
        this.gridAction = new ShowGridAction();
        this.createAction = new CreateAction(new GraphicalViewer[] { this.calendar.getTasksViewer(), this.calendar.getActivitiesViewer() });
        this.editAction = new EditModelAction(this.calendar);
        this.removeAction = new RemoveAction(this.calendar);
        this.moveAction = new MoveToTodayAction(this.calendar);
    }

    private void createToolBar() {
        IActionBars ab = getViewSite().getActionBars();
        IToolBarManager manager = ab.getToolBarManager();
        manager.add(this.zoomInAction);
        manager.add(this.zoomOutAction);
        manager.add(new Separator());
        manager.add(this.createAction);
        manager.add(this.editAction);
        manager.add(new Separator());
        manager.add(this.removeAction);
        ab.updateActionBars();
    }

    private void createContextMenu() {
        GraphicalViewer[] gviewers = new GraphicalViewer[] { this.calendar.getActivitiesViewer(), this.calendar.getTasksViewer() };
        for (int i = 0; i < gviewers.length; i++) {
            if (gviewers[i].getControl() != null) {
                MenuManager manager = new MenuManager();
                Menu menu = manager.createContextMenu(gviewers[i].getControl());
                gviewers[i].getControl().setMenu(menu);
                manager.add(this.createAction);
                manager.add(this.editAction);
                manager.add(this.moveAction);
                manager.add(new Separator());
                manager.add(new Separator());
                manager.add(this.removeAction);
                manager.add(new Separator());
                manager.add(this.gridAction);
            }
        }
    }

    private void createPullDownMenu() {
        IActionBars ab = getViewSite().getActionBars();
        IMenuManager manager = ab.getMenuManager();
        manager.add(this.createAction);
        manager.add(this.editAction);
        manager.add(new Separator());
        manager.add(this.gridAction);
        ab.updateActionBars();
    }

    @Override
    public void setFocus() {
    }

    public void selectionChanged(final SelectionChangedEvent event) {
        this.removeAction.selectionChanged(event);
        updateActions();
    }

    private void updateActions() {
    }

    @Override
    public void dispose() {
        if ((this.calendar != null) && !this.calendar.isDisposed()) {
            this.calendar.removeSelectionChangedListener(this);
        }
        super.dispose();
    }
}

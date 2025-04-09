package q_impress.pmi.plugin.editors.pmiform;

import java.lang.reflect.Constructor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.FileEditorInput;
import q_impress.pmi.lib.atop.AtopTranslationTask;
import q_impress.pmi.lib.jmt.JmtTranslationTask;
import q_impress.pmi.lib.project.IResource;
import q_impress.pmi.lib.project.ModelingProject;
import q_impress.pmi.lib.project.ResourceException;
import q_impress.pmi.lib.tasks.AbstractTask;
import q_impress.pmi.lib.tasks.TaskException;
import q_impress.pmi.plugin.wizards.INewTaskWizard;
import q_impress.pmi.plugin.wizards.atop.CreateAtopTaskWizard;
import q_impress.pmi.plugin.wizards.jmt.CreateJmtTaskWizard;

public class TasksPage extends FormPage {

    public static final String PAGE_ID = "TasksPage";

    static final String PAGE_TITLE = "Tasks";

    /** The tasks list section */
    private SectionPart tasksListSectionPart = null;

    private Section tasksListSection = null;

    private TableViewer tasksListViewer = null;

    private Button removeTaskButton = null;

    private Button executeTaskButton = null;

    /** The task overview section */
    private SectionPart taskSectionPart = null;

    private Section taskSection = null;

    public TasksPage(FormEditor editor) {
        super(editor, PAGE_ID, PAGE_TITLE);
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        FormToolkit toolkit = this.getEditor().getToolkit();
        ScrolledForm form = managedForm.getForm();
        form.setText("Tasks");
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        form.getBody().setLayout(new ColumnLayout());
        Dialog.applyDialogFont(form.getBody());
        createTasksListSection(form, toolkit);
        tasksListSectionPart = new SectionPart(tasksListSection) {

            @Override
            public void refresh() {
                tasksListViewer.refresh();
                super.refresh();
            }
        };
        managedForm.addPart(tasksListSectionPart);
        createTaskSection(form, toolkit);
        taskSectionPart = new SectionPart(taskSection) {

            @Override
            public void refresh() {
                tasksListViewer.refresh();
                super.refresh();
            }
        };
        managedForm.addPart(taskSectionPart);
        Dialog.applyDialogFont(form.getBody());
    }

    private void createTasksListSection(final ScrolledForm form, FormToolkit toolkit) {
        tasksListSection = toolkit.createSection(form.getBody(), Section.EXPANDED | Section.TITLE_BAR | Section.DESCRIPTION);
        tasksListSection.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
        tasksListSection.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));
        tasksListSection.setText("Tasks");
        tasksListSection.setDescription("This section provides information about tasks available for the model.");
        tasksListSection.addExpansionListener(new ExpansionAdapter() {

            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(false);
            }
        });
        Composite client = toolkit.createComposite(tasksListSection, SWT.WRAP);
        tasksListSection.setClient(client);
        toolkit.paintBordersFor(client);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        client.setLayout(layout);
        GridData gd = new GridData();
        tasksListViewer = new TableViewer(client);
        gd.widthHint = 250;
        gd.heightHint = 250;
        tasksListViewer.getControl().setLayoutData(gd);
        tasksListViewer.setContentProvider(new ArrayContentProvider() {

            @Override
            public Object[] getElements(Object inputElement) {
                ModelingProject prj = (ModelingProject) inputElement;
                return prj.getAllResources(AbstractTask.class).values().toArray();
            }
        });
        tasksListViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((IResource) element).getName();
            }
        });
        tasksListViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection.size() == 1) {
                    removeTaskButton.setEnabled(true);
                    executeTaskButton.setEnabled(true);
                    if (selection.getFirstElement() instanceof AtopTranslationTask) {
                        AtopTaskForm taskForm = new AtopTaskForm(taskSection, SWT.WRAP, taskSectionPart, (AtopTranslationTask) selection.getFirstElement(), TasksPage.this);
                        taskForm.initialize();
                        replaceTaskForm(taskForm);
                    } else if (selection.getFirstElement() instanceof JmtTranslationTask) {
                        JmtTaskForm taskForm = new JmtTaskForm(taskSection, SWT.WRAP, taskSectionPart, (JmtTranslationTask) selection.getFirstElement(), TasksPage.this);
                        taskForm.initialize();
                        replaceTaskForm(taskForm);
                    }
                } else {
                    removeTaskButton.setEnabled(false);
                    executeTaskButton.setEnabled(false);
                    resetTaskForm();
                }
            }
        });
        tasksListViewer.setInput(((ModelingProjectEditorInput) getEditorInput()).getProject());
        Composite buttonComposite = toolkit.createComposite(client, SWT.WRAP);
        gd = new GridData(GridData.FILL_VERTICAL);
        buttonComposite.setLayoutData(gd);
        layout = new GridLayout();
        layout.numColumns = 1;
        buttonComposite.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        Button button = toolkit.createButton(buttonComposite, "Add", SWT.PUSH);
        button.setLayoutData(gd);
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ListDialog typeSelectionDialog = new ListDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
                typeSelectionDialog.setContentProvider(new ArrayContentProvider() {

                    @Override
                    public Object[] getElements(Object inputElement) {
                        Class<?>[] wizards = { CreateAtopTaskWizard.class, CreateJmtTaskWizard.class };
                        return wizards;
                    }
                });
                typeSelectionDialog.setLabelProvider(new LabelProvider() {

                    @Override
                    public String getText(Object element) {
                        if (element instanceof Class<?>) {
                            Class<? extends INewTaskWizard> wizardClass = (Class<? extends INewTaskWizard>) element;
                            if (wizardClass.equals(CreateAtopTaskWizard.class)) return "ATOP Task"; else if (wizardClass.equals(CreateJmtTaskWizard.class)) return "JMT Task"; else return "UNKNOWN";
                        } else return "UNKNOWN";
                    }
                });
                typeSelectionDialog.setTitle("Add New Task");
                typeSelectionDialog.setMessage("Select the type of task to create :");
                typeSelectionDialog.setInput(new Integer(1));
                if (typeSelectionDialog.open() == WizardDialog.OK) {
                    if (typeSelectionDialog.getResult().length == 1) {
                        Class<? extends INewTaskWizard> wizardClass = (Class<? extends INewTaskWizard>) typeSelectionDialog.getResult()[0];
                        try {
                            Constructor<? extends INewTaskWizard> constr = wizardClass.getConstructor(ModelingProject.class, String.class);
                            INewTaskWizard wizard = constr.newInstance(getInputProject(), getInputProjectFileName());
                            WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wizard);
                            if (dialog.open() == WizardDialog.OK) {
                                tasksListSectionPart.markStale();
                                tasksListSectionPart.markDirty();
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
        removeTaskButton = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
        removeTaskButton.setLayoutData(gd);
        removeTaskButton.setEnabled(false);
        removeTaskButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) tasksListViewer.getSelection();
                AbstractTask task = (AbstractTask) selection.getFirstElement();
                try {
                    getInputProject().removeResource(task.getId());
                    tasksListSectionPart.markStale();
                    tasksListSectionPart.markDirty();
                } catch (ResourceException e1) {
                }
            }
        });
        executeTaskButton = toolkit.createButton(buttonComposite, "Execute", SWT.PUSH);
        executeTaskButton.setLayoutData(gd);
        executeTaskButton.setEnabled(false);
        executeTaskButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!getEditor().isDirty()) {
                    IStructuredSelection selection = (IStructuredSelection) tasksListViewer.getSelection();
                    AbstractTask task = (AbstractTask) selection.getFirstElement();
                    if (task.canExecute()) {
                        try {
                            task.execute();
                            tasksListSectionPart.markStale();
                            tasksListSectionPart.markDirty();
                        } catch (TaskException e1) {
                            ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Task Execution", "Task execution failed", new Status(IStatus.ERROR, "q_impress", e1.getMessage(), e1));
                        }
                    } else ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Task Execution", "Task execution failed", new Status(IStatus.ERROR, "q_impress", "Task is not configured correctly"));
                } else ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Task Execution", "Task execution failed", new Status(IStatus.ERROR, "q_impress", "Model must be saved before task can be executed"));
            }
        });
    }

    private void createTaskSection(final ScrolledForm form, FormToolkit toolkit) {
        taskSection = toolkit.createSection(form.getBody(), Section.EXPANDED | Section.TITLE_BAR | Section.DESCRIPTION);
        taskSection.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
        taskSection.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));
        taskSection.setText("Task properties");
        taskSection.setDescription("This section provides information about task properties.");
        taskSection.addExpansionListener(new ExpansionAdapter() {

            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(false);
            }
        });
        Composite client = toolkit.createComposite(taskSection, SWT.WRAP);
        taskSection.setClient(client);
        toolkit.paintBordersFor(client);
    }

    private void resetTaskForm() {
        FormToolkit toolkit = getEditor().getToolkit();
        taskSection.getClient().dispose();
        Composite emptyComposite = toolkit.createComposite(taskSection);
        taskSection.setClient(emptyComposite);
    }

    private void replaceTaskForm(Composite newForm) {
        taskSection.getClient().dispose();
        taskSection.setClient(newForm);
        taskSection.setExpanded(true);
        getEditor().getToolkit().paintBordersFor(newForm);
    }

    private ModelingProject getInputProject() {
        return this.getEditorInput() != null ? ((ModelingProjectEditorInput) this.getEditorInput()).getProject() : null;
    }

    private String getInputProjectFileName() {
        if (this.getEditorInput() != null) {
            FileEditorInput originalInput = (FileEditorInput) ((ModelingProjectEditorInput) this.getEditorInput()).getOriginalEditorInput();
            return originalInput.getFile().getFullPath().toString();
        } else return null;
    }

    SectionPart getTasksListSectionPart() {
        return tasksListSectionPart;
    }
}

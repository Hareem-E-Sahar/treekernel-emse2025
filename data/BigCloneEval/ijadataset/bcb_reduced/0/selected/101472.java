package org.eclipse.mylyn.tasks.ui.editors;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.ui.TaskListColorsAndFonts;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTaskOutlineNode;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTaskSelection;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.TaskList;
import org.eclipse.mylyn.tasks.ui.DatePicker;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.themes.IThemeManager;

/**
 * An editor used to view a locally created bug that does not yet exist on a server.
 * 
 * @author Rob Elves (modifications)
 * @since 2.0
 */
public abstract class AbstractNewRepositoryTaskEditor extends AbstractRepositoryTaskEditor {

    private static final int DESCRIPTION_WIDTH = 79 * 7;

    private static final int DEFAULT_FIELD_WIDTH = 150;

    private static final int DEFAULT_ESTIMATED_TIME = 1;

    private static final String LABEL_CREATE = "Create New";

    private static final String ERROR_CREATING_BUG_REPORT = "Error creating bug report";

    protected DatePicker scheduledForDate;

    protected Spinner estimatedTime;

    protected String newSummary = "";

    protected Button addToCategory;

    protected CCombo categoryChooser;

    public AbstractNewRepositoryTaskEditor(FormEditor editor) {
        super(editor);
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) {
        if (!(input instanceof NewTaskEditorInput)) {
            return;
        }
        initTaskEditor(site, (RepositoryTaskEditorInput) input);
        setTaskOutlineModel(RepositoryTaskOutlineNode.parseBugReport(taskData, false));
        newSummary = taskData.getSummary();
    }

    @Override
    protected void createDescriptionLayout(Composite composite) {
        FormToolkit toolkit = this.getManagedForm().getToolkit();
        Section section = toolkit.createSection(composite, ExpandableComposite.TITLE_BAR);
        section.setText(getSectionLabel(SECTION_NAME.DESCRIPTION_SECTION));
        section.setExpanded(true);
        section.setLayout(new GridLayout());
        section.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite descriptionComposite = toolkit.createComposite(section);
        GridLayout descriptionLayout = new GridLayout();
        descriptionComposite.setLayout(descriptionLayout);
        GridData descriptionData = new GridData(GridData.FILL_BOTH);
        descriptionData.grabExcessVerticalSpace = true;
        descriptionComposite.setLayoutData(descriptionData);
        section.setClient(descriptionComposite);
        descriptionTextViewer = addTextEditor(repository, descriptionComposite, taskData.getDescription(), true, SWT.FLAT | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        descriptionTextViewer.setEditable(true);
        GridData descriptionTextData = new GridData(GridData.FILL_BOTH);
        descriptionTextViewer.getControl().setLayoutData(descriptionTextData);
        descriptionTextViewer.getControl().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
        addDuplicateDetection(descriptionComposite);
        toolkit.paintBordersFor(descriptionComposite);
    }

    @Override
    protected void createSummaryLayout(Composite composite) {
        Composite summaryComposite = getManagedForm().getToolkit().createComposite(composite);
        GridLayout summaryLayout = new GridLayout(2, false);
        summaryLayout.verticalSpacing = 0;
        summaryLayout.marginHeight = 2;
        summaryComposite.setLayout(summaryLayout);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(summaryComposite);
        RepositoryTaskAttribute attribute = taskData.getAttribute(RepositoryTaskAttribute.SUMMARY);
        if (attribute != null) {
            createLabel(summaryComposite, attribute);
            summaryText = createTextField(summaryComposite, attribute, SWT.FLAT);
            summaryText.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
            IThemeManager themeManager = getSite().getWorkbenchWindow().getWorkbench().getThemeManager();
            Font summaryFont = themeManager.getCurrentTheme().getFontRegistry().get(TaskListColorsAndFonts.TASK_EDITOR_FONT);
            summaryText.setFont(summaryFont);
            GridDataFactory.fillDefaults().grab(true, false).hint(DESCRIPTION_WIDTH, SWT.DEFAULT).applyTo(summaryText);
            summaryText.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    String sel = summaryText.getText();
                    if (!(newSummary.equals(sel))) {
                        newSummary = sel;
                        markDirty(true);
                    }
                }
            });
        }
        getManagedForm().getToolkit().paintBordersFor(summaryComposite);
    }

    @Override
    protected void createAttachmentLayout(Composite comp) {
    }

    @Override
    protected void createCommentLayout(Composite comp) {
    }

    @Override
    protected void createNewCommentLayout(Composite comp) {
        createPlanningLayout(comp);
    }

    protected void createPlanningLayout(Composite comp) {
        Section section = createSection(comp, "Personal Planning");
        section.setLayout(new GridLayout());
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        section.setExpanded(true);
        Composite sectionClient = getManagedForm().getToolkit().createComposite(section);
        section.setClient(sectionClient);
        GridLayout layout = new GridLayout();
        layout.numColumns = 7;
        layout.makeColumnsEqualWidth = false;
        sectionClient.setLayout(layout);
        GridData clientDataLayout = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        sectionClient.setLayoutData(clientDataLayout);
        getManagedForm().getToolkit().createLabel(sectionClient, "Scheduled for:");
        scheduledForDate = new DatePicker(sectionClient, SWT.FLAT, DatePicker.LABEL_CHOOSE);
        scheduledForDate.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        scheduledForDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
        Calendar newTaskSchedule = Calendar.getInstance();
        int scheduledEndHour = TasksUiPlugin.getDefault().getPreferenceStore().getInt(TasksUiPreferenceConstants.PLANNING_ENDHOUR);
        if (newTaskSchedule.get(Calendar.HOUR_OF_DAY) >= scheduledEndHour) {
            TasksUiPlugin.getTaskListManager().setSecheduledIn(newTaskSchedule, 1);
        } else {
            TasksUiPlugin.getTaskListManager().setScheduledEndOfDay(newTaskSchedule);
        }
        scheduledForDate.setDate(newTaskSchedule);
        ImageHyperlink clearReminder = getManagedForm().getToolkit().createImageHyperlink(sectionClient, SWT.NONE);
        clearReminder.setImage(TasksUiImages.getImage(TasksUiImages.REMOVE));
        clearReminder.setToolTipText("Clear");
        clearReminder.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                scheduledForDate.setDate(null);
            }
        });
        Label dummy = getManagedForm().getToolkit().createLabel(sectionClient, "");
        GridData dummyLabelDataLayout = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        dummyLabelDataLayout.horizontalSpan = 1;
        dummyLabelDataLayout.widthHint = 30;
        dummy.setLayoutData(dummyLabelDataLayout);
        getManagedForm().getToolkit().createLabel(sectionClient, "Estimated hours:");
        estimatedTime = new Spinner(sectionClient, SWT.FLAT);
        estimatedTime.setDigits(0);
        estimatedTime.setMaximum(100);
        estimatedTime.setMinimum(0);
        estimatedTime.setIncrement(1);
        estimatedTime.setSelection(DEFAULT_ESTIMATED_TIME);
        estimatedTime.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
        GridData estimatedDataLayout = new GridData();
        estimatedDataLayout.widthHint = 30;
        estimatedTime.setLayoutData(estimatedDataLayout);
        ImageHyperlink clearEstimated = getManagedForm().getToolkit().createImageHyperlink(sectionClient, SWT.NONE);
        clearEstimated.setImage(TasksUiImages.getImage(TasksUiImages.REMOVE));
        clearEstimated.setToolTipText("Clear");
        clearEstimated.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                estimatedTime.setSelection(0);
            }
        });
        getManagedForm().getToolkit().paintBordersFor(sectionClient);
    }

    @Override
    protected void addRadioButtons(Composite buttonComposite) {
    }

    @Override
    protected void createCustomAttributeLayout(Composite composite) {
    }

    @Override
    protected void saveTaskOffline(IProgressMonitor progressMonitor) {
        taskData.setSummary(newSummary);
        taskData.setDescription(descriptionTextViewer.getTextWidget().getText());
        updateEditorTitle();
    }

    /**
	 * A listener for selection of the summary textbox.
	 */
    protected class DescriptionListener implements Listener {

        public void handleEvent(Event event) {
            fireSelectionChanged(new SelectionChangedEvent(selectionProvider, new StructuredSelection(new RepositoryTaskSelection(taskData.getId(), taskData.getRepositoryUrl(), taskData.getRepositoryKind(), "New Description", false, taskData.getSummary()))));
        }
    }

    @Override
    protected void validateInput() {
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
	 * Creates the button layout. This displays options and buttons at the bottom of the editor to allow actions to be
	 * performed on the bug.
	 */
    @Override
    protected void createActionsLayout(Composite formComposite) {
        Section section = getManagedForm().getToolkit().createSection(formComposite, ExpandableComposite.TITLE_BAR);
        section.setText(getSectionLabel(SECTION_NAME.ACTIONS_SECTION));
        section.setExpanded(true);
        section.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(section);
        Composite buttonComposite = getManagedForm().getToolkit().createComposite(section);
        buttonComposite.setLayout(new GridLayout(4, false));
        buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        section.setClient(buttonComposite);
        addToCategory = getManagedForm().getToolkit().createButton(buttonComposite, "Add to Category", SWT.CHECK);
        categoryChooser = new CCombo(buttonComposite, SWT.FLAT | SWT.READ_ONLY);
        categoryChooser.setLayoutData(GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).create());
        getManagedForm().getToolkit().adapt(categoryChooser, true, true);
        categoryChooser.setFont(TEXT_FONT);
        TaskList taskList = TasksUiPlugin.getTaskListManager().getTaskList();
        List<AbstractTaskCategory> categories = taskList.getUserCategories();
        Collections.sort(categories, new Comparator<AbstractTaskContainer>() {

            public int compare(AbstractTaskContainer c1, AbstractTaskContainer c2) {
                if (c1.equals(TasksUiPlugin.getTaskListManager().getTaskList().getDefaultCategory())) {
                    return -1;
                } else if (c2.equals(TasksUiPlugin.getTaskListManager().getTaskList().getDefaultCategory())) {
                    return 1;
                } else {
                    return c1.getSummary().compareToIgnoreCase(c2.getSummary());
                }
            }
        });
        for (AbstractTaskContainer category : categories) {
            categoryChooser.add(category.getSummary());
        }
        categoryChooser.select(0);
        categoryChooser.setEnabled(false);
        categoryChooser.setData(categories);
        addToCategory.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                categoryChooser.setEnabled(addToCategory.getSelection());
            }
        });
        GridDataFactory.fillDefaults().hint(DEFAULT_FIELD_WIDTH, SWT.DEFAULT).span(3, SWT.DEFAULT).applyTo(categoryChooser);
        addActionButtons(buttonComposite);
        getManagedForm().getToolkit().paintBordersFor(buttonComposite);
    }

    /**
	 * Returns the {@link AbstractTaskContainer category} the new task belongs to
	 * 
	 * @return {@link AbstractTaskContainer category} where the new task must be added to, or null if it must not be
	 *         added to the task list
	 */
    @Override
    @SuppressWarnings("unchecked")
    protected AbstractTaskCategory getCategory() {
        int index = categoryChooser.getSelectionIndex();
        if (addToCategory.getSelection() && index != -1) {
            if (index == 0) {
                return TasksUiPlugin.getTaskListManager().getTaskList().getDefaultCategory();
            }
            return ((List<AbstractTaskCategory>) categoryChooser.getData()).get(index - 1);
        }
        return null;
    }

    @Override
    protected void addActionButtons(Composite buttonComposite) {
        FormToolkit toolkit = new FormToolkit(buttonComposite.getDisplay());
        submitButton = toolkit.createButton(buttonComposite, LABEL_CREATE, SWT.NONE);
        GridData submitButtonData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        submitButton.setLayoutData(submitButtonData);
        submitButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                submitToRepository();
            }
        });
        submitButton.setToolTipText("Submit to " + this.repository.getUrl());
    }

    protected boolean prepareSubmit() {
        submitButton.setEnabled(false);
        showBusy(true);
        if (summaryText != null && summaryText.getText().trim().equals("")) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openInformation(AbstractNewRepositoryTaskEditor.this.getSite().getShell(), ERROR_CREATING_BUG_REPORT, "A summary must be provided with new bug reports.");
                    summaryText.setFocus();
                    submitButton.setEnabled(true);
                    showBusy(false);
                }
            });
            return false;
        }
        if (descriptionTextViewer != null && descriptionTextViewer.getTextWidget().getText().trim().equals("")) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openInformation(AbstractNewRepositoryTaskEditor.this.getSite().getShell(), ERROR_CREATING_BUG_REPORT, "A summary must be provided with new reports.");
                    descriptionTextViewer.getTextWidget().setFocus();
                    submitButton.setEnabled(true);
                    showBusy(false);
                }
            });
            return false;
        }
        return true;
    }

    @Override
    protected void createPeopleLayout(Composite composite) {
    }

    @Override
    public AbstractTask updateSubmittedTask(String id, IProgressMonitor monitor) throws CoreException {
        final AbstractTask newTask = super.updateSubmittedTask(id, monitor);
        if (newTask != null) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    Calendar selectedDate = scheduledForDate.getDate();
                    if (selectedDate != null) {
                        TasksUiPlugin.getTaskListManager().setScheduledFor(newTask, selectedDate.getTime());
                    }
                    newTask.setEstimatedTimeHours(estimatedTime.getSelection());
                    Object selectedObject = null;
                    if (TaskListView.getFromActivePerspective() != null) selectedObject = ((IStructuredSelection) TaskListView.getFromActivePerspective().getViewer().getSelection()).getFirstElement();
                    if (selectedObject instanceof TaskCategory) {
                        TasksUiPlugin.getTaskListManager().getTaskList().moveToContainer(newTask, ((TaskCategory) selectedObject));
                    }
                }
            });
        }
        return newTask;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        new MessageDialog(null, "Operation not supported", null, "Save of un-submitted new tasks is not currently supported.\nPlease submit all new tasks.", MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0).open();
        monitor.setCanceled(true);
        return;
    }

    public static String getStackTraceFromDescription(String description) {
        String stackTrace = null;
        if (description == null) {
            return null;
        }
        String punct = "!\"#$%&'\\(\\)*+,-./:;\\<=\\>?@\\[\\]^_`\\{|\\}~\n";
        String lineRegex = " *at\\s+[\\w" + punct + "]+ ?\\(.*\\) *\n?";
        Pattern tracePattern = Pattern.compile(lineRegex);
        Matcher match = tracePattern.matcher(description);
        if (match.find()) {
            int start = match.start();
            int lastEnd = match.end();
            while (match.find()) {
                lastEnd = match.end();
            }
            if (start <= 0) {
                return null;
            }
            int stackStart = 0;
            int index = start - 1;
            while (index > 1 && description.charAt(index) == ' ') {
                index--;
            }
            stackStart = description.substring(0, index - 1).lastIndexOf("\n");
            stackStart = (stackStart == -1) ? 0 : stackStart + 1;
            stackTrace = description.substring(stackStart, lastEnd);
        }
        return stackTrace;
    }

    @Override
    public boolean searchForDuplicates() {
        this.saveTaskOffline(new NullProgressMonitor());
        return super.searchForDuplicates();
    }
}

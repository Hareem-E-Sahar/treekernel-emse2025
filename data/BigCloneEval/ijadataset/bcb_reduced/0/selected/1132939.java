package org.sss.eibs.design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.sss.eibs.rule.ContentAssistProcessor;
import org.sss.eibs.rule.PersistentDocument;
import org.sss.eibs.rule.RuleCodeScanner;
import org.sss.module.IModuleManager;
import org.sss.module.eibs.Module;
import org.sss.module.eibs.Rule;
import org.sss.module.eibs.file.I18nManagerImpl;
import org.sss.module.eibs.util.CodetextUtils;
import org.sss.module.eibs.util.ModulePathUtil;
import org.sss.module.exception.CheckerException;
import org.sss.util.ContainerUtils;

/**
 * @author Jason.Hoo (latest modification by $Author: hujianxin78728 $)
 * @version $Revision: 606 $ $Date: 2009-11-17 06:32:21 -0500 (Tue, 17 Nov 2009) $
 */
public class ShellRuleEditor extends AbstractShell {

    static final Log log = LogFactory.getLog(ShellRuleEditor.class);

    static final Map<Rule, ShellRuleEditor> editors = new HashMap<Rule, ShellRuleEditor>();

    private PersistentDocument document;

    private SourceViewer viewer;

    private IModuleManager manager;

    private Rule rule;

    private Module module;

    private StyledText textMemo;

    private StyledText textSource;

    private List<Integer> breakPoints = new ArrayList<Integer>();

    private List<String> i18nKeys = new ArrayList<String>();

    private int errorLine = -1;

    private ShellRuleEditor(Shell parent, IModuleManager manager, Rule rule) {
        super(parent, SWT.SHELL_TRIM);
        this.manager = manager;
        this.module = manager.getModule(rule.getModuleName());
        this.rule = rule;
        createContents();
        setLayout(new GridLayout());
        setSize(800, 600);
        editors.put(rule, this);
    }

    public static final ShellRuleEditor getInstance(Shell parent, IModuleManager manager, Rule rule) {
        ShellRuleEditor editor = editors.get(rule);
        if (editor != null) {
            if (!editor.isDisposed()) return editor;
            editors.remove(rule);
        }
        return new ShellRuleEditor(parent, manager, rule);
    }

    protected void createContents() {
        final ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
        toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        final ToolItem toolItemSave = new ToolItem(toolBar, SWT.PUSH);
        toolItemSave.setEnabled(false);
        toolItemSave.setImage(SWTResourceManager.getImage(ShellRuleEditor.class, "/save.gif"));
        toolItemSave.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                saveAction();
            }
        });
        Composite composite = new Composite(this, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new FillLayout());
        Composite compositeViewer = new Composite(this, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        data.heightHint = 80;
        compositeViewer.setLayoutData(data);
        compositeViewer.setLayout(new FillLayout());
        TextViewer textViewer = new TextViewer(compositeViewer, SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER);
        textMemo = textViewer.getTextWidget();
        document = new PersistentDocument();
        final CompositeRuler ruler = new CompositeRuler();
        ruler.addDecorator(0, new LineNumberRulerColumn() {

            @Override
            protected void paintLine(int line, int y, int lineHeight, GC gc, Display arg4) {
                super.paintLine(line, y, lineHeight, gc, arg4);
                if (breakPoints.contains(line) && line != errorLine) gc.drawImage(SWTResourceManager.getImage(ShellRuleEditor.class, "/greenblock.gif"), 0, 0, 16, 16, 2, y + 3, 16, 16);
                if (line == errorLine) gc.drawImage(SWTResourceManager.getImage(ShellRuleEditor.class, "/redblock.gif"), 0, 0, 16, 16, 0, y + 2, 16, 16);
            }
        });
        viewer = new SourceViewer(composite, ruler, SWT.V_SCROLL | SWT.H_SCROLL);
        textSource = viewer.getTextWidget();
        viewer.configure(new SourceViewerConfiguration() {

            @Override
            public int getTabWidth(ISourceViewer sourceViewer) {
                return 2;
            }

            @Override
            public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
                return new ContentFormatter();
            }

            @Override
            public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
                PresentationReconciler reconciler = new PresentationReconciler();
                DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new RuleCodeScanner());
                reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
                reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
                return reconciler;
            }

            @Override
            public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
                ContentAssistant contentAssistant = new ContentAssistant();
                contentAssistant.setContentAssistProcessor(new ContentAssistProcessor(manager, module, rule), IDocument.DEFAULT_CONTENT_TYPE);
                contentAssistant.enableAutoActivation(true);
                contentAssistant.setAutoActivationDelay(200);
                return contentAssistant;
            }
        });
        viewer.setDocument(document);
        viewer.appendVerifyKeyListener(new VerifyKeyListener() {

            public void verifyKey(VerifyEvent event) {
                if ((event.stateMask & SWT.CTRL) > 0) {
                    switch(event.character) {
                        case '/':
                            if (viewer.canDoOperation(SourceViewer.CONTENTASSIST_PROPOSALS)) viewer.doOperation(SourceViewer.CONTENTASSIST_PROPOSALS);
                            event.doit = false;
                            break;
                        case 'z' - 'a' + 1:
                            viewer.doOperation(SourceViewer.UNDO);
                            event.doit = false;
                            break;
                        case 'y' - 'a' + 1:
                            viewer.doOperation(SourceViewer.REDO);
                            event.doit = false;
                            break;
                        case 'a' - 'a' + 1:
                            viewer.doOperation(SourceViewer.SELECT_ALL);
                            event.doit = false;
                            break;
                        case 'f' - 'a' + 1:
                            if ((event.stateMask & SWT.SHIFT) > 0) formatAction();
                            event.doit = false;
                            break;
                        case 's' - 'a' + 1:
                            saveAction();
                            event.doit = false;
                            break;
                    }
                }
            }
        });
        String codeText = ModulePathUtil.getCodeText(manager, module, rule);
        if (!ContainerUtils.isEmpty(codeText)) textSource.setText(replaceI18nKeyToValue(codeText));
        viewer.addTextListener(new ITextListener() {

            public void textChanged(TextEvent event) {
                updateChanged();
            }
        });
        CursorLinePainter painterLine = new CursorLinePainter(viewer);
        painterLine.setHighlightColor(textSource.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
        viewer.addPainter(painterLine);
        MarginPainter painterMargin = new MarginPainter(viewer);
        painterMargin.setMarginRulerColor(textSource.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        viewer.addPainter(painterMargin);
        initCodeFont();
        super.addUpdateListener(new Widget[] { toolItemSave });
        this.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                if (modified) ((ShellMain) parent).refreshModuleTree();
            }
        });
    }

    private void initCodeFont() {
        FontData data = new FontData("Courier New", 12, SWT.NONE);
        Font font = new Font(this.getShell().getDisplay(), data);
        textSource.setFont(font);
    }

    private void saveAction() {
        if (checkAndFormat()) {
            ModulePathUtil.setCodeText(manager, module, rule, replaceI18nValueToKey(textSource.getText()));
            super.resetChanged();
        }
    }

    private void formatAction() {
        if (checkAndFormat()) super.updateChanged();
    }

    private boolean setSelectionQuietly(String codeText, Point p, boolean result) {
        try {
            textSource.setText(codeText);
            textSource.setSelection(p);
        } catch (Exception e) {
        }
        return true;
    }

    private boolean checkAndFormat() {
        String codeText = textSource.getText();
        Point p = textSource.getSelection();
        if ("".equals(codeText)) return false;
        textMemo.setText("");
        errorLine = -1;
        try {
            CodetextUtils.check(manager, module, codeText, rule);
            String text = CodetextUtils.format(codeText);
            return setSelectionQuietly(text, p, true);
        } catch (Exception e) {
            if (e.getMessage() != null) textMemo.setText(e.getMessage()); else textMemo.setText(e.getClass().getName());
            if (e instanceof CheckerException) errorLine = ((CheckerException) e).getLine() - 1; else log.error("", e);
            viewer.refresh();
            return setSelectionQuietly(codeText, p, false);
        }
    }

    private String replaceI18nKeyToValue(final String codetext) {
        i18nKeys.clear();
        String name = module.getName();
        Pattern pattern = Pattern.compile("#CT[0-9]{6}");
        Matcher matcher = pattern.matcher(codetext);
        String key;
        StringBuffer sb = new StringBuffer();
        int offset = 0;
        while (matcher.find()) {
            sb.append(codetext.substring(offset, matcher.start()));
            key = codetext.substring(matcher.start() + 1, matcher.end());
            i18nKeys.add(key);
            sb.append("`").append(I18nManagerImpl.getProperty(name, key)).append("`");
            offset = matcher.end();
        }
        sb.append(codetext.substring(offset));
        return sb.toString();
    }

    private String replaceI18nValueToKey(final String codetext) {
        String name = module.getName();
        Pattern pattern = Pattern.compile("`[^`]*`");
        Matcher matcher = pattern.matcher(codetext);
        String key, i18nValue;
        StringBuffer sb = new StringBuffer();
        int keyIndex = 0, offset = 0;
        while (matcher.find()) {
            sb.append(codetext.substring(offset, matcher.start()));
            i18nValue = codetext.substring(matcher.start() + 1, matcher.end() - 1);
            if (keyIndex < i18nKeys.size()) {
                key = i18nKeys.get(keyIndex++);
                I18nManagerImpl.setProperty(name, key, i18nValue);
            } else key = I18nManagerImpl.getI18nKey(module, "CT", i18nValue);
            sb.append("#").append(key);
            offset = matcher.end();
        }
        sb.append(codetext.substring(offset));
        return sb.toString();
    }
}

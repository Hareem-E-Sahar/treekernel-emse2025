package com.devutil.plugins.commit.checkin;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: pdeman
 * Date: 2 f�vr. 2010
 * Time: 16:18:31
 * <p/>
 * Class description HERE !!!
 */
public abstract class MyAbstractProcessor {

    private final Project myProject;

    private final Module myModule;

    private PsiDirectory directory = null;

    private PsiFile file = null;

    private PsiFile[] files = null;

    private boolean subdirs = false;

    private final String message;

    private final String title;

    protected abstract Runnable preprocessFile(PsiFile psifile) throws IncorrectOperationException;

    protected MyAbstractProcessor(Project project, String title, String message) {
        myProject = project;
        myModule = null;
        directory = null;
        subdirs = true;
        this.title = title;
        this.message = message;
    }

    protected MyAbstractProcessor(Project project, Module module, String title, String message) {
        myProject = project;
        myModule = module;
        directory = null;
        subdirs = true;
        this.title = title;
        this.message = message;
    }

    protected MyAbstractProcessor(Project project, PsiDirectory dir, boolean subdirs, String title, String message) {
        myProject = project;
        myModule = null;
        directory = dir;
        this.subdirs = subdirs;
        this.message = message;
        this.title = title;
    }

    protected MyAbstractProcessor(Project project, PsiFile file, String title, String message) {
        myProject = project;
        myModule = null;
        this.file = file;
        this.message = message;
        this.title = title;
    }

    protected MyAbstractProcessor(Project project, PsiFile[] files, String title, String message, Runnable runnable) {
        myProject = project;
        myModule = null;
        this.files = files;
        this.message = message;
        this.title = title;
    }

    public void run() {
        if (directory != null) {
            process(directory, subdirs);
        } else if (files != null) {
            process(files);
        } else if (file != null) {
            process(file);
        } else if (myModule != null) {
            process(myModule);
        } else if (myProject != null) {
            process(myProject);
        }
    }

    private void process(final PsiFile file) {
        final VirtualFile virtualFile = file.getVirtualFile();
        assert virtualFile != null;
        if (!ReadonlyStatusHandler.getInstance(myProject).ensureFilesWritable(virtualFile).hasReadonlyFiles()) {
            final Runnable[] resultRunnable = new Runnable[1];
            execute(new Runnable() {

                public void run() {
                    try {
                        resultRunnable[0] = preprocessFile(file);
                    } catch (IncorrectOperationException incorrectoperationexception) {
                        logger.error(incorrectoperationexception);
                    }
                }
            }, new Runnable() {

                public void run() {
                    if (resultRunnable[0] != null) {
                        resultRunnable[0].run();
                    }
                }
            });
        }
    }

    private Runnable prepareFiles(List<PsiFile> files) {
        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        String msg = null;
        double fraction = 0.0D;
        if (indicator != null) {
            msg = indicator.getText();
            fraction = indicator.getFraction();
            indicator.setText(message);
        }
        final Runnable[] runnables = new Runnable[files.size()];
        for (int i = 0; i < files.size(); i++) {
            PsiFile pfile = files.get(i);
            if (pfile == null) {
                logger.debug("Unexpected null file at " + i);
                continue;
            }
            if (indicator != null) {
                if (indicator.isCanceled()) {
                    return null;
                }
                indicator.setFraction((double) i / (double) files.size());
            }
            if (pfile.isWritable()) {
                try {
                    runnables[i] = preprocessFile(pfile);
                } catch (IncorrectOperationException incorrectoperationexception) {
                    logger.error(incorrectoperationexception);
                }
            }
            files.set(i, null);
        }
        if (indicator != null) {
            indicator.setText(msg);
            indicator.setFraction(fraction);
        }
        return new Runnable() {

            public void run() {
                ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                String msg = null;
                double fraction = 0.0D;
                if (indicator != null) {
                    msg = indicator.getText();
                    fraction = indicator.getFraction();
                    indicator.setText(message);
                }
                for (int j = 0; j < runnables.length; j++) {
                    if (indicator != null) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        indicator.setFraction((double) j / (double) runnables.length);
                    }
                    Runnable runnable = runnables[j];
                    if (runnable != null) {
                        runnable.run();
                    }
                    runnables[j] = null;
                }
                if (indicator != null) {
                    indicator.setText(msg);
                    indicator.setFraction(fraction);
                }
            }
        };
    }

    private void process(final PsiFile[] files) {
        final Runnable[] resultRunnable = new Runnable[1];
        execute(new Runnable() {

            public void run() {
                resultRunnable[0] = prepareFiles(new ArrayList<PsiFile>(Arrays.asList(files)));
            }
        }, new Runnable() {

            public void run() {
                if (resultRunnable[0] != null) {
                    resultRunnable[0].run();
                }
            }
        });
    }

    private void process(final PsiDirectory dir, final boolean subdirs) {
        final List<PsiFile> pfiles = new ArrayList<PsiFile>();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {

            public void run() {
                findFiles(pfiles, dir, subdirs);
            }
        }, title, true, myProject);
        handleFiles(pfiles);
    }

    private void process(final Project project) {
        final List<PsiFile> pfiles = new ArrayList<PsiFile>();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {

            public void run() {
                findFiles(project, pfiles);
            }
        }, title, true, project);
        handleFiles(pfiles);
    }

    private void process(final Module module) {
        final List<PsiFile> pfiles = new ArrayList<PsiFile>();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {

            public void run() {
                findFiles(module, pfiles);
            }
        }, title, true, myProject);
        handleFiles(pfiles);
    }

    private static void findFiles(Project project, List<PsiFile> files) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            findFiles(module, files);
        }
    }

    protected static void findFiles(final Module module, final List<PsiFile> files) {
        final ModuleFileIndex idx = ModuleRootManager.getInstance(module).getFileIndex();
        final VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        for (VirtualFile root : roots) {
            idx.iterateContentUnderDirectory(root, new ContentIterator() {

                public boolean processFile(final VirtualFile dir) {
                    if (dir.isDirectory()) {
                        final PsiDirectory psiDir = PsiManager.getInstance(module.getProject()).findDirectory(dir);
                        if (psiDir != null) {
                            findFiles(files, psiDir, false);
                        }
                    }
                    return true;
                }
            });
        }
    }

    private void handleFiles(final List<PsiFile> files) {
        final List<VirtualFile> vFiles = new ArrayList<VirtualFile>();
        for (PsiFile psiFile : files) {
            vFiles.add(psiFile.getVirtualFile());
        }
        if (!ReadonlyStatusHandler.getInstance(myProject).ensureFilesWritable(VfsUtil.toVirtualFileArray(vFiles)).hasReadonlyFiles()) {
            if (!files.isEmpty()) {
                final Runnable[] resultRunnable = new Runnable[1];
                execute(new Runnable() {

                    public void run() {
                        resultRunnable[0] = prepareFiles(files);
                    }
                }, new Runnable() {

                    public void run() {
                        if (resultRunnable[0] != null) {
                            resultRunnable[0].run();
                        }
                    }
                });
            }
        }
    }

    private static void findFiles(List<PsiFile> files, PsiDirectory directory, boolean subdirs) {
    }

    private void execute(final Runnable readAction, final Runnable writeAction) {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {

            public void run() {
                readAction.run();
                new WriteCommandAction(myProject, title, null) {

                    protected void run(Result result) throws Throwable {
                        writeAction.run();
                    }
                }.execute();
            }
        }, title, true, myProject);
    }

    private static final Logger logger = Logger.getInstance(MyAbstractProcessor.class.getName());
}

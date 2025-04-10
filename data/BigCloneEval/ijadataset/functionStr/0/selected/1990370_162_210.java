public class Test {    protected void fillDiagramPopupMenu(MenuManager manager) {
        manager.add(new Separator("align"));
        manager.add(autoLayoutAction);
        top = new AlignmentAction((IWorkbenchPart) this, PositionConstants.TOP);
        top.setSelectionProvider(getGraphicalViewer());
        midlle = new AlignmentAction((IWorkbenchPart) this, PositionConstants.MIDDLE);
        midlle.setSelectionProvider(getGraphicalViewer());
        bottom = new AlignmentAction((IWorkbenchPart) this, PositionConstants.BOTTOM);
        bottom.setSelectionProvider(getGraphicalViewer());
        left = new AlignmentAction((IWorkbenchPart) this, PositionConstants.LEFT);
        left.setSelectionProvider(getGraphicalViewer());
        center = new AlignmentAction((IWorkbenchPart) this, PositionConstants.CENTER);
        center.setSelectionProvider(getGraphicalViewer());
        right = new AlignmentAction((IWorkbenchPart) this, PositionConstants.RIGHT);
        right.setSelectionProvider(getGraphicalViewer());
        getActionRegistry().registerAction(top);
        getActionRegistry().registerAction(midlle);
        getActionRegistry().registerAction(bottom);
        getActionRegistry().registerAction(left);
        getActionRegistry().registerAction(center);
        getActionRegistry().registerAction(right);
        MenuManager alignmenu = new MenuManager(UMLPlugin.getDefault().getResourceString("menu.align"));
        alignmenu.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_TOP));
        alignmenu.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_MIDDLE));
        alignmenu.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_BOTTOM));
        alignmenu.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_LEFT));
        alignmenu.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_CENTER));
        alignmenu.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_RIGHT));
        manager.add(alignmenu);
        MenuManager filtermenu = new MenuManager(UMLPlugin.getDefault().getResourceString("menu.filter"));
        filtermenu.add(togglePublicAttr);
        filtermenu.add(toggleProtectedAttr);
        filtermenu.add(togglePackageAttr);
        filtermenu.add(togglePrivateAttr);
        filtermenu.add(new Separator());
        filtermenu.add(togglePublicOpe);
        filtermenu.add(toggleProtectedOpe);
        filtermenu.add(togglePackageOpe);
        filtermenu.add(togglePrivateOpe);
        manager.add(filtermenu);
        manager.add(new Separator("add"));
        manager.add(addAttributeAction);
        manager.add(addOperationAction);
        manager.add(upAction);
        manager.add(downAction);
        manager.add(new Separator("copy"));
        manager.add(copyAction);
        manager.add(pasteAction);
    }
}
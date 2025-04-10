public class Test {                public Iterable<Iterable<IAction>> buildToolbar(GraphicalViewer graphicalViewer) {
                    ArrayList<Iterable<IAction>> toolbar = new ArrayList<Iterable<IAction>>();
                    ArrayList<IAction> navigateActions = new ArrayList<IAction>();
                    navigateActions.add(new RefreshAction(myRefreshPerformer));
                    navigateActions.add(new ExpandAction(myModelProvider));
                    navigateActions.add(new CollapseAction(myModelProvider));
                    toolbar.add(navigateActions);
                    ArrayList<IAction> zoomActions = new ArrayList<IAction>();
                    zoomActions.add(new ZoomInAction(HierarchyEditor.this, graphicalViewer));
                    zoomActions.add(new ZoomOutAction(HierarchyEditor.this, graphicalViewer));
                    zoomActions.add(new ZoomFitAction(HierarchyEditor.this, graphicalViewer));
                    zoomActions.add(new ZoomOriginalAction(HierarchyEditor.this, graphicalViewer));
                    toolbar.add(zoomActions);
                    ArrayList<IAction> filterActions = new ArrayList<IAction>();
                    filterActions.add(new FilterAction(HierarchyEditor.this, FILTER_ITEMS));
                    toolbar.add(filterActions);
                    ArrayList<IAction> printActions = new ArrayList<IAction>();
                    printActions.add(new PrintAction(HierarchyEditor.this, graphicalViewer));
                    toolbar.add(printActions);
                    return toolbar;
                }
}
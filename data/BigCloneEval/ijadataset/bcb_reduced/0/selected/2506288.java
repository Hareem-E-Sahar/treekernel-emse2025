package com.wwfish.cmsui.dashboard.client;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import com.nexustar.gwt.dashboard.client.*;
import com.nexustar.gwt.widgets.client.asyn.IAsyncModelCallback;
import com.nexustar.gwt.widgets.client.ui.iprovider.ITreeProvider;
import com.nexustar.gwt.widgets.client.ui.tab.FishTabItem;
import com.nexustar.gwt.widgets.client.ui.tree.ViewTree;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom
 * Date: 2010-7-10
 * Time: 15:31:58
 * To change this template use File | Settings | File Templates.
 */
public class DashMenuBuilder {

    public static final String _PAGE_MOCK_FLAG = "#";

    private static Map pageModelMap = new HashMap();

    public static Map getPageModelMap() {
        return pageModelMap;
    }

    public static void constructMenu(StackPanel panel) {
        List menus = getAllMenu();
        for (Iterator it = menus.iterator(); it.hasNext(); ) {
            MenuModel temp = (MenuModel) it.next();
            panel.add(constructTree(temp.getPages()), temp.getText());
        }
        panel.showStack(0);
    }

    public static ViewTree constructTree(final List<PageModel> pages) {
        ViewTree tree = new ViewTree(new ITreeProvider() {

            public String getTreeItemText(Object entity) {
                pageModelMap.put(((PageModel) entity).getPageId(), entity);
                return ((PageModel) entity).getPageTitle();
            }

            public Image getTreeItemIcon(Object entity) {
                return null;
            }

            public boolean hasChildren(Object entity) {
                if (((PageModel) entity).getChildren() != null && ((PageModel) entity).getChildren().size() > 0) return true; else return false;
            }

            public void getChildren(Object parentEntity, IAsyncModelCallback asyn) {
                PageModel parent = (PageModel) parentEntity;
                asyn.setModelElments(parent.getChildren());
            }

            public void setInputData(Object[] parameters, IAsyncModelCallback asyn) {
                asyn.setModelElments(filterFunction(pages));
            }
        });
        tree.getModelManger().renderAsyncModel(null);
        tree.expandAll(true);
        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {

            public void onSelection(SelectionEvent<TreeItem> treeItemSelectionEvent) {
                final PageModel page = (PageModel) treeItemSelectionEvent.getSelectedItem().getUserObject();
                if (page.getChildren() != null) return;
                dispatchPage(page);
            }
        });
        return tree;
    }

    static void dispatchPage(PageModel page) {
        dispatchPage(page, true, null);
    }

    /**
     * 对pageModel的tabItem的初始化和显示。
     * 即对所有在栏目功能节点注册的页面model初始化。
     *
     * @param page
     * @param isRenderModel
     * @param client
     */
    static void dispatchPage(final PageModel page, final boolean isRenderModel, final PageClient client) {
        if (page.getPageId() != null && page.getPageId().indexOf(_PAGE_MOCK_FLAG) != -1) {
            String realPageId = page.getPageId().substring(page.getPageId().indexOf(_PAGE_MOCK_FLAG) + 1);
            PagesFactory.getInstance().getPage(page.getPageId(), realPageId, getDispatchPageClient(page, isRenderModel, client));
        } else PagesFactory.getInstance().getPage(page.getPageId(), getDispatchPageClient(page, isRenderModel, client));
    }

    private static PageClient getDispatchPageClient(final PageModel page, final boolean isRenderModel, final PageClient client) {
        return new PageClient() {

            public void success(AbstractPage pageWidget) {
                final FishTabItem is = getItem(page.getPageId());
                if (is == null) {
                    FishTabItem item;
                    if (pageWidget == null) {
                        item = new FishTabItem(CMSDashboard.getTabPanel(), new Label("正在建设中！"));
                        CMSDashboard.getTabPanel().setSelectItem(item);
                        CMSDashboard.getTabPanel().addTabItem(item, "150px");
                        return;
                    } else {
                        pageWidget.setMenuModel(page.getMenuModel());
                        item = new FishTabItem(CMSDashboard.getTabPanel(), pageWidget);
                        CMSDashboard.getTabPanel().setSelectItem(item);
                        if (isRenderModel) if (pageWidget.getModelManger().getProvider() != null) pageWidget.getModelManger().renderAsyncModel(new Object[] { page.getPageId() }); else pageWidget.getModelManger().renderModel(null);
                    }
                    item.setText(pageWidget.getPageTitle() == null || pageWidget.getPageTitle().trim().equals("") ? page.getPageTitle() : pageWidget.getPageTitle());
                    item.setUserObject(page.getPageId());
                    CMSDashboard.getTabPanel().addTabItem(item, "150px");
                } else {
                    is.setShowWidget(pageWidget);
                    is.setText(pageWidget.getPageTitle() == null || pageWidget.getPageTitle().trim().equals("") ? page.getPageTitle() : pageWidget.getPageTitle());
                    CMSDashboard.getTabPanel().setSelectItem(is);
                }
                if (client != null) client.success(pageWidget);
            }

            public void failure() {
                if (client != null) client.failure();
            }
        };
    }

    private static List<PageModel> filterFunction(List<PageModel> pages) {
        List result = new ArrayList();
        if (pages == null) return result;
        for (Iterator it = pages.iterator(); it.hasNext(); ) {
            PageModel page = (PageModel) it.next();
            boolean isAccess = true;
            if (page.getAccesses() != null) {
                for (Iterator ir = page.getAccesses().iterator(); ir.hasNext(); ) {
                    IsAccess access = (IsAccess) ir.next();
                    if (!access.isAccess()) {
                        isAccess = false;
                        break;
                    }
                }
            }
            if (isAccess) result.add(page);
        }
        return result;
    }

    private static List<MenuModel> filterMenu(List<MenuModel> menus) {
        List result = new ArrayList();
        if (menus == null) return result;
        for (Iterator it = menus.iterator(); it.hasNext(); ) {
            MenuModel menu = (MenuModel) it.next();
            boolean isAccess = true;
            if (menu.getAccesses() != null) {
                for (Iterator ir = menu.getAccesses().iterator(); ir.hasNext(); ) {
                    IsAccess access = (IsAccess) ir.next();
                    if (!access.isAccess()) {
                        isAccess = false;
                        break;
                    }
                }
            }
            if (isAccess) result.add(menu);
        }
        return result;
    }

    static FishTabItem getItem(String id) {
        for (Iterator it = CMSDashboard.getTabPanel().getItems().iterator(); it.hasNext(); ) {
            FishTabItem item = (FishTabItem) it.next();
            if (item.getUserObject() != null && item.getUserObject().equals(id)) return item;
        }
        return null;
    }

    private static List<MenuModel> getAllMenu() {
        List menus = MenusFactory.getInstance().getMenu();
        return sequenceMenus(filterMenu(mergeMenus(menus)));
    }

    public static List mergeMenus(List menus) {
        List result = new ArrayList();
        for (Iterator it = menus.iterator(); it.hasNext(); ) {
            MenuModel model = (MenuModel) it.next();
            int position = result.indexOf(model);
            if (position == -1) {
                MenuModel temp = new MenuModel(model.getText(), model.getCode());
                temp.setAccesses(model.getAccesses());
                temp.setIndex(model.getIndex());
                temp.setUserObject(model.getUserObject());
                List pages = new ArrayList(model.getPages());
                temp.setPages(pages);
                result.add(temp);
            } else {
                MenuModel old = (MenuModel) result.get(position);
                old.getPages().addAll(model.getPages());
            }
        }
        return result;
    }

    public static List sequenceMenus(List menus) {
        List result = new ArrayList();
        for (Iterator it = menus.iterator(); it.hasNext(); ) {
            MenuModel temp = (MenuModel) it.next();
            int index = temp.getIndex();
            int comparePosition;
            for (int minPosition = 0, maxPosition = result.size(); minPosition <= maxPosition; ) {
                if (result.size() == 0) {
                    result.add(0, temp);
                    break;
                }
                comparePosition = (minPosition + maxPosition) / 2;
                if (comparePosition == minPosition) {
                    if (((MenuModel) result.get(comparePosition)).getIndex() > index) result.add(comparePosition, temp); else result.add(comparePosition + 1, temp);
                    break;
                } else {
                    if (((MenuModel) result.get(comparePosition)).getIndex() > index) {
                        maxPosition = comparePosition;
                    } else {
                        minPosition = comparePosition;
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        MenuModel m1 = new MenuModel("1", 111);
        MenuModel m2 = new MenuModel("555", 555);
        MenuModel m3 = new MenuModel("122", 122);
        MenuModel m4 = new MenuModel("66", 66);
        MenuModel m5 = new MenuModel("88", 88);
        MenuModel m6 = new MenuModel("23", 23);
        MenuModel m7 = new MenuModel("27", 27);
        MenuModel m8 = new MenuModel("24", 24);
        MenuModel m9 = new MenuModel("999", 999);
        MenuModel m10 = new MenuModel("999", 999);
        MenuModel m11 = new MenuModel("32", 32);
        MenuModel m12 = new MenuModel("32", 32);
        List list = new ArrayList();
        list.add(m1);
        list.add(m2);
        list.add(m3);
        list.add(m4);
        list.add(m5);
        list.add(m6);
        list.add(m7);
        list.add(m8);
        list.add(m9);
        list.add(m10);
        list.add(m11);
        list.add(m12);
        list = sequenceMenus(list);
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            System.out.println(((MenuModel) it.next()).getIndex());
        }
    }
}

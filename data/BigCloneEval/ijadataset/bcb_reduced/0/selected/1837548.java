package com.wwg.market.ui.dashboard.client;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mobileares.midp.widgets.client.page.*;
import com.mobileares.midp.widgets.client.tab.FrameTabItem;
import com.mobileares.midp.widgets.client.tree.CTree;
import com.mobileares.midp.widgets.client.tree.CTreeItem;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom
 * Date: 2011-11-4
 * Time: 16:07:08
 * To change this template use File | Settings | File Templates.
 */
public class DashMenuHelper {

    public static final String _PAGE_MOCK_FLAG = "#";

    private static Map<String, PageModel> pageModelMap = new HashMap();

    private static Map<String, MenuModel> MenuModelMap = new HashMap();

    public static Map getPageModelMap() {
        return pageModelMap;
    }

    public static void constructMenu(StackPanel panel) {
        pageModelMap.clear();
        MenuModelMap.clear();
        List menus = getAllMenu();
        for (Iterator it = menus.iterator(); it.hasNext(); ) {
            MenuModel temp = (MenuModel) it.next();
            if (MenuModelMap.containsKey(temp.getCode())) {
                throw new IllegalArgumentException("MenuModel:" + temp.getText() + "code与" + MenuModelMap.get(temp.getCode()).getText() + "code重复,都为:" + temp.getCode() + "!");
            }
            MenuModelMap.put(temp.getCode(), temp);
            Widget vp;
            if (temp.getWidget() == null) {
                vp = constructTree(temp.getPages());
            } else vp = temp.getWidget();
            panel.add(vp, temp.getText());
        }
        if (panel.getWidgetCount() > 0) panel.showStack(0);
    }

    public static Widget constructTree(final List<PageModel> pages) {
        CTree tree = new CTree();
        tree.addSelectionHandler(new SelectionHandler<CTreeItem>() {

            public void onSelection(SelectionEvent<CTreeItem> cTreeItemSelectionEvent) {
                final PageModel page = (PageModel) cTreeItemSelectionEvent.getSelectedItem().getUserObject();
                if (page.getChildren() != null) return;
                dispatchPage(page);
            }
        });
        for (PageModel pm : filterFunction(pages)) {
            CTreeItem item = new CTreeItem(pm.getText());
            item.setUserObject(pm);
            processPage(pm, item);
            tree.addTreeItem(item);
        }
        tree.expandAll();
        return tree;
    }

    private static void processPage(PageModel parent, CTreeItem parentItem) {
        if (parent.getChildren() != null && parent.getChildren().size() > 0) {
            for (PageModel pm : filterFunction(parent.getChildren())) {
                CTreeItem item = new CTreeItem(pm.getText());
                item.setUserObject(pm);
                processPage(pm, item);
                parentItem.addTreeItem(item);
            }
        }
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

            public void success(final AbstractPage pageWidget) {
                FrameTabItem is = getItem(page.getPageId());
                if (is == null) {
                    if (pageWidget == null) {
                        Dashboard.tabPanel.addItem(page.getText(), page.getPageId(), "150px", new Label("正在建设中！"));
                        Dashboard.tabPanel.setSelectItem(page.getPageId());
                        return;
                    } else {
                        is = new FrameTabItem(Dashboard.tabPanel, pageWidget);
                        pageWidget.setMenuModel(page.getMenuModel());
                        String text = (pageWidget.getPageTitle() == null || pageWidget.getPageTitle().trim().equals("") ? page.getPageTitle() : pageWidget.getPageTitle());
                        if (pageWidget.getEvent() != null) is.setEvent(pageWidget.getEvent()); else is.setEvent(Dashboard.event);
                        is.setText(text);
                        is.setUserObject(page.getPageId());
                        Dashboard.tabPanel.addItem(is, "150px");
                        Dashboard.tabPanel.setSelectItem(page.getPageId());
                        if (client != null) client.success(pageWidget);
                        if (isRenderModel) pageWidget.ddOutModel(null, null);
                        pageWidget.setHeight(Dashboard.frame.getBodyHeight() - 30 + "px");
                    }
                } else if (pageWidget != null) {
                    is.setShowWidget(pageWidget);
                    is.setText(pageWidget.getPageTitle() == null || pageWidget.getPageTitle().trim().equals("") ? page.getPageTitle() : pageWidget.getPageTitle());
                    Dashboard.tabPanel.setSelectItem(page.getPageId());
                    pageWidget.setHeight(Dashboard.frame.getBodyHeight() - 30 + "px");
                }
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
            if (AccessHelper.isAccess(page, page)) {
                result.add(page);
                if (page.getBacks().size() > 0) for (ItemModel.CacheDateCallBack back : page.getBacks()) {
                    back.process();
                }
            }
        }
        return result;
    }

    private static List<MenuModel> filterMenu(List<MenuModel> menus) {
        List result = new ArrayList();
        if (menus == null) return result;
        for (Iterator it = menus.iterator(); it.hasNext(); ) {
            MenuModel menu = (MenuModel) it.next();
            if (AccessHelper.isAccess(menu, menu)) {
                result.add(menu);
                if (menu.getBacks().size() > 0) for (ItemModel.CacheDateCallBack back : menu.getBacks()) {
                    back.process();
                }
            }
        }
        return result;
    }

    public static boolean isExistModule(String id) {
        return MenuModelMap.get(id) != null;
    }

    public static FrameTabItem getItem(String id) {
        for (Iterator it = Dashboard.tabPanel.getItems().iterator(); it.hasNext(); ) {
            FrameTabItem item = (FrameTabItem) it.next();
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
                temp.setWidget(model.getWidget());
                temp.getBacks().addAll(model.getBacks());
                if (model.getPages() != null) {
                    List pages = new ArrayList(model.getPages());
                    temp.setPages(pages);
                }
                result.add(temp);
            } else {
                MenuModel old = (MenuModel) result.get(position);
                old.getPages().addAll(model.getPages());
                old.getBacks().addAll(model.getBacks());
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

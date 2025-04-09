package org.xblackcat.rojac.gui.popup;

import org.xblackcat.rojac.data.MessageData;
import org.xblackcat.rojac.gui.IAppControl;
import org.xblackcat.rojac.gui.view.model.FavoriteType;
import org.xblackcat.rojac.gui.view.model.Post;
import org.xblackcat.rojac.i18n.Message;
import org.xblackcat.rojac.util.LinkUtils;
import javax.swing.*;
import java.awt.*;

/**
 * @author xBlackCat
 */
final class MenuHelper {

    private MenuHelper() {
    }

    /**
     * Creates a sub-menu with copy-message-link actions. The actions are: <ul> <li>Copy link to the message</li>
     * <li>Copy link to whole thread. The thread layout is flat.</li> <li>Copy link to whole thread. The thread layout
     * is tree.</li> </ul>
     *
     * @param messageId message id to be used as base of target URLs.
     * @return sub-menu with registered actions to copy various URLs.
     */
    static JMenu copyLinkSubmenu(int messageId) {
        JMenu menu = new JMenu();
        menu.setText(Message.Popup_View_ThreadsTree_CopyUrl.get());
        JMenuItem copyUrl = new JMenuItem();
        copyUrl.setText(Message.Popup_View_ThreadsTree_CopyUrl_Message.get());
        copyUrl.addActionListener(new CopyUrlAction(LinkUtils.buildMessageLink(messageId)));
        menu.add(copyUrl);
        JMenuItem copyFlatUrl = new JMenuItem();
        copyFlatUrl.setText(Message.Popup_View_ThreadsTree_CopyUrl_Flat.get());
        copyFlatUrl.addActionListener(new CopyUrlAction(LinkUtils.buildFlatThreadLink(messageId)));
        menu.add(copyFlatUrl);
        JMenuItem copyThreadUrl = new JMenuItem();
        copyThreadUrl.setText(Message.Popup_View_ThreadsTree_CopyUrl_Thread.get());
        copyThreadUrl.addActionListener(new CopyUrlAction(LinkUtils.buildThreadLink(messageId)));
        menu.add(copyThreadUrl);
        return menu;
    }

    /**
     * Adds to an menu a 'open in browser' action if it supported by current OS.
     *
     * @param menu        target menu to be filled with the menu item.
     * @param linkMessage
     * @param url
     */
    static void addOpenLink(JPopupMenu menu, Message linkMessage, String url) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        final Desktop desktop = Desktop.getDesktop();
        if (url != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            JMenuItem openInBrowserItem = new JMenuItem(linkMessage.get());
            openInBrowserItem.addActionListener(new OpenUrlAction(url));
            menu.add(openInBrowserItem);
        }
    }

    static JMenuItem copyToClipboard(String url) {
        JMenuItem copyToClipboard = new JMenuItem(Message.Popup_Link_Copy_ToClipboard.get());
        copyToClipboard.addActionListener(new CopyUrlAction(url));
        return copyToClipboard;
    }

    static JMenuItem markReadUnreadSubmenu(Post message, Window owner) {
        JMenu menu = new JMenu(Message.Popup_View_ThreadsTree_Mark_Title.get());
        menu.add(new SetThreadReadMenuItem(Message.Popup_View_ThreadsTree_Mark_ThreadRead, message, true));
        menu.add(new SetThreadReadMenuItem(Message.Popup_View_ThreadsTree_Mark_ThreadUnread, message, false));
        menu.addSeparator();
        final MessageData messageData = message.getMessageData();
        menu.add(new SetForumReadMenuItem(Message.Popup_View_SetReadAll, messageData.getForumId(), true));
        menu.add(new SetForumReadMenuItem(Message.Popup_View_SetUnreadAll, messageData.getForumId(), false));
        menu.addSeparator();
        menu.add(new ExtendedMarkRead(Message.Popup_View_ThreadsTree_Mark_Extended, messageData, owner));
        return menu;
    }

    /**
     * Creates a submenu to add the
     *
     * @param messageData
     * @param appControl
     * @return
     */
    static JMenuItem favoritesSubmenu(MessageData messageData, IAppControl appControl) {
        JMenu menu = new JMenu(Message.Popup_Favorites_Add.get("..."));
        menu.add(new AddToFavoriteMenuItem(Message.Popup_Favorites_Add_Thread.get(), FavoriteType.Thread, messageData.getThreadRootId()));
        menu.addSeparator();
        String userName = messageData.getUserName();
        int userId = messageData.getUserId();
        menu.add(new AddToFavoriteMenuItem(Message.Popup_Favorites_Add_UserPosts.get(userName), FavoriteType.UserPosts, userId));
        menu.add(new AddToFavoriteMenuItem(Message.Popup_Favorites_Add_ToUserReplies.get(userName), FavoriteType.UserResponses, userId));
        return menu;
    }
}

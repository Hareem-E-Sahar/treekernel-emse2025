package org.td4j.swing.binding;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.td4j.core.binding.model.IndividualDataProxy;
import org.td4j.core.model.ChangeEvent;
import org.td4j.core.model.IObserver;
import org.td4j.core.model.ObservableTK;
import org.td4j.swing.workbench.Navigator;
import ch.miranet.commons.TK;

public class LinkController extends IndividualSwingWidgetController<JLabel> {

    private static final LinkHandler linkHandler = new LinkHandler();

    private final JLabel widget;

    private final Navigator navigator;

    private final LinkTargetObserver linkTargetObserver = new LinkTargetObserver(this);

    public LinkController(final JLabel widget, IndividualDataProxy proxy, final Navigator navigator) {
        super(proxy);
        this.widget = TK.Objects.assertNotNull(widget, "widget");
        this.navigator = TK.Objects.assertNotNull(navigator, "navigator");
        widget.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                widget.requestFocus();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        doNavigate();
                    }
                });
            }
        });
        setAccess();
        updateView();
    }

    public JLabel getWidget() {
        return widget;
    }

    protected void doNavigate() {
        final IndividualDataProxy dataProxy = getDataProxy();
        final Class<?> valueType = dataProxy.getValueType();
        final Object value = dataProxy.canRead() ? dataProxy.readValue() : null;
        if (value == null) return;
        if (linkHandler.canBrowse() && URL.class.isAssignableFrom(valueType)) {
            try {
                final URI uri = ((URL) value).toURI();
                linkHandler.browse(uri);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else if (linkHandler.canBrowse() && URI.class.isAssignableFrom(valueType)) {
            try {
                final URI uri = (URI) value;
                linkHandler.browse(uri);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else if (linkHandler.canOpen() && File.class.isAssignableFrom(valueType)) {
            try {
                final File file = (File) value;
                linkHandler.open(file);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            navigator.seek(value);
        }
    }

    protected void setAccess() {
    }

    protected void updateView0(Object newTarget) {
        updateLinkTargetChanged(newTarget);
    }

    protected Object readView0() {
        throw new IllegalStateException();
    }

    private void updateLinkTargetChanged(Object newTarget) {
        linkTargetObserver.detachFromTarget();
        linkTargetObserver.attachToTarget(newTarget);
        updateLinkTargetStateChanged(newTarget);
    }

    private void updateLinkTargetStateChanged(Object target) {
        if (target != null) {
            final String text = String.format("<html><font color='blue'><u>%s</u></font></html>", target);
            widget.setText(text);
            widget.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            widget.setText("<html><font color='gray'>&lt;null&gt;</font></html>");
            widget.setCursor(Cursor.getDefaultCursor());
        }
    }

    private static class LinkTargetObserver implements IObserver {

        private final LinkController controller;

        private Object target;

        private LinkTargetObserver(LinkController controller) {
            this.controller = TK.Objects.assertNotNull(controller, "controller");
        }

        private boolean attachToTarget(Object target) {
            if (this.target != null) throw new IllegalStateException("already attached");
            this.target = target;
            return ObservableTK.attachObserverToModel(target, this);
        }

        private boolean detachFromTarget() {
            final boolean success = ObservableTK.detachObserverFromModel(target, this);
            this.target = null;
            return success;
        }

        @Override
        public void observableChanged(ChangeEvent event) {
            controller.updateLinkTargetStateChanged(target);
        }
    }

    ;

    private static class LinkHandler {

        private static final String xdgOpenCmd = "xdg-open";

        private static enum BrowseHandler {

            Desktop, Xdg
        }

        private static enum OpenHandler {

            Desktop, Xdg
        }

        private final BrowseHandler browseHandler;

        private final OpenHandler openHandler;

        private LinkHandler() {
            browseHandler = initBrowseHandler();
            openHandler = initOpenHandler();
        }

        boolean canBrowse() {
            return browseHandler != null;
        }

        boolean canOpen() {
            return openHandler != null;
        }

        void browse(URI uri) throws IOException {
            switch(browseHandler) {
                case Desktop:
                    Desktop.getDesktop().browse(uri);
                    break;
                case Xdg:
                    invokeXdgOpen(uri.toASCIIString());
                    break;
                default:
                    throw new UnsupportedOperationException("browse not supported.");
            }
        }

        void open(File file) throws IOException {
            switch(openHandler) {
                case Desktop:
                    Desktop.getDesktop().open(file);
                    break;
                case Xdg:
                    invokeXdgOpen(file.getAbsolutePath());
                    break;
                default:
                    throw new UnsupportedOperationException("open not supported.");
            }
        }

        private void invokeXdgOpen(String argument) {
            try {
                final String cmd = xdgOpenCmd + " " + argument;
                System.out.println("Invoke: " + cmd);
                Runtime.getRuntime().exec(cmd);
            } catch (IOException ioex) {
                throw new RuntimeException(ioex);
            }
        }

        private BrowseHandler initBrowseHandler() {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                return BrowseHandler.Desktop;
            } else {
                try {
                    Runtime.getRuntime().exec(xdgOpenCmd);
                    return BrowseHandler.Xdg;
                } catch (IOException ioex) {
                }
            }
            return null;
        }

        private OpenHandler initOpenHandler() {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                return OpenHandler.Desktop;
            } else {
                try {
                    Runtime.getRuntime().exec(xdgOpenCmd);
                    return OpenHandler.Xdg;
                } catch (IOException ioex) {
                }
            }
            return null;
        }
    }
}

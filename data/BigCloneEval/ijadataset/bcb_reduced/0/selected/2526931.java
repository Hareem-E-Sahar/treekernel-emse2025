package com.rise.rois.ui.factories;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import com.rise.rois.ui.tabs.TabManager;
import com.rise.rois.ui.wizards.newuser.NewUserDialog;

public class MyGuideUser extends Individual {

    @Override
    public void create() {
        if (Desktop.isDesktopSupported()) {
            URI uri;
            try {
                uri = new URI("http://www.myguide.gov.uk/");
                Desktop.getDesktop().browse(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        TabManager.showTab("Users");
        new NewUserDialog();
    }

    @Override
    public String getMenuText() {
        return "MyGuide User...";
    }
}

// Copyright 2011 Konrad Twardowski
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.makagiga.plugins.screenshot;

import static org.makagiga.commons.UI._;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.makagiga.MainWindow;
import org.makagiga.commons.Config;
import org.makagiga.commons.MAction;
import org.makagiga.commons.TK;
import org.makagiga.commons.UI;
import org.makagiga.commons.form.DefaultFocus;
import org.makagiga.commons.form.Field;
import org.makagiga.commons.form.Form;
import org.makagiga.commons.form.FormPanel;
import org.makagiga.commons.form.IntegerRange;
import org.makagiga.commons.swing.MDialog;
import org.makagiga.commons.swing.MMenu;
import org.makagiga.commons.swing.MMessage;
import org.makagiga.fs.MetaInfo;
import org.makagiga.fs.tree.TreeFS;
import org.makagiga.plugins.GeneralPlugin;
import org.makagiga.tree.Tree;

public final class Plugin extends GeneralPlugin {

	// private
	
	private MAction newScreenshotAction;

	// public

	public Plugin() { }

	// PluginMenu

	@Override
	public void updateMenu(final String type, final MMenu menu) {
		if (type.equals(NEW_MENU) || type.equals(TRAY_MENU)) {
			if (newScreenshotAction == null)
				newScreenshotAction = new NewScreenshotAction();
			menu.add(newScreenshotAction);
		}
	}

	// private classes

	private final class NewScreenshotAction extends MAction {

		// public

		@Override
		public void onAction() {
			Config config = Plugin.this.getLocalConfig();
			ScreenshotSettingsForm form = new ScreenshotSettingsForm();
			form.delay = config.readInt("delay", ScreenshotSettingsForm.DEFAULT_DELAY, 0, ScreenshotSettingsForm.MAX_DELAY);
			form.hideMainWindow = config.read("hideMainWindow", form.hideMainWindow);
			FormPanel<ScreenshotSettingsForm> panel = new FormPanel<>(form);
			panel.setLabel("delay", _("Delay (in seconds):"));
			panel.setLabel("hideMainWindow", _("Hide Main Window"));
			MDialog dialog = panel.createDialog(this.getSourceWindow(), _("New Screenshot"), "ui/image");
			dialog.packFixed();
			
			if (!dialog.exec())
				return;

			config.write("delay", form.delay);
			config.write("hideMainWindow", form.hideMainWindow);
			config.sync();

			MainWindow mainWindow = MainWindow.getInstance();
			try {
				if (form.hideMainWindow)
					mainWindow.setVisible(false);

				if (form.delay > 0) {
// FIXME: blocks main window
					TK.sleep(form.delay * 1000);
				}

				Robot robot = new Robot();
				Rectangle area = new Rectangle();
				area.setSize(UI.getScreenSize());
				BufferedImage image = robot.createScreenCapture(area);
				
				if (form.hideMainWindow)
					mainWindow.setVisible(true);

				TreeFS treeFS = TreeFS.getInstance();
				MetaInfo folder = treeFS.getCurrentFolder(false);
				MetaInfo file = treeFS.createUniqueFile(folder, _("Screenshot"), "png");
				ImageIO.write(image, "png", file.getFile());
// TODO: add image format help/tips
				Tree.getInstance().open(file);
			}
			catch (Exception exception) {
				MMessage.error(this.getSourceWindow(), exception);
			}
			finally {
				if (form.hideMainWindow && !mainWindow.isVisible())
					mainWindow.setVisible(true);
			}
		}

		// private

		private NewScreenshotAction() {
			super(_("New Screenshot..."), "ui/image");
		}

	}

	@Form(order = { "delay", "hideMainWindow" })
	private static final class ScreenshotSettingsForm {

		// private

		private static final int DEFAULT_DELAY = 2;
		private static final int MAX_DELAY = 10;

		@DefaultFocus
		@Field
		@IntegerRange(minimum = 0, maximum = MAX_DELAY)
		private int delay;
		
		@Field
		private boolean hideMainWindow = true;

		// private

		private ScreenshotSettingsForm() { }

	}

}

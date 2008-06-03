package tigase.messenger.client;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;

public class MainToolBar extends ToolBar {

	public MainToolBar() {
		createAllMenu();
	}

	protected void createAllMenu() {
		add(getActionMenu());
	}

	protected ToolItem getActionMenu() {
		Menu x = new Menu();

		TextToolItem ti = new TextToolItem("Actions");
		ti.setMenu(x);

		MenuItem statusMenu = new MenuItem("Set status");
		x.add(statusMenu);
		Menu statusSubmenu = new Menu();
		statusMenu.setSubMenu(statusSubmenu);

		MenuItem statusOnlineItem = new MenuItem("Online");
		statusSubmenu.add(statusOnlineItem);
		MenuItem statusChatItem = new MenuItem("Free for chat");
		statusSubmenu.add(statusChatItem);
		MenuItem statusAwayItem = new MenuItem("Away");
		statusSubmenu.add(statusAwayItem);
		MenuItem statusXAItem = new MenuItem("Extended Away");
		statusSubmenu.add(statusXAItem);
		MenuItem statusDNDItem = new MenuItem("Do not Disturb");
		statusSubmenu.add(statusDNDItem);

		MenuItem logoutItem = new MenuItem("Logout");
		x.add(logoutItem);

		return ti;
	}

}

package tigase.messenger.client;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.TextMenuItem;
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

		TextMenuItem statusMenu = new TextMenuItem("Set status");
		x.add(statusMenu);
		Menu statusSubmenu = new Menu();
		statusMenu.setSubMenu(statusSubmenu);

		TextMenuItem statusOnlineItem = new TextMenuItem("Online");
		statusSubmenu.add(statusOnlineItem);
		TextMenuItem statusChatItem = new TextMenuItem("Free for chat");
		statusSubmenu.add(statusChatItem);
		TextMenuItem statusAwayItem = new TextMenuItem("Away");
		statusSubmenu.add(statusAwayItem);
		TextMenuItem statusXAItem = new TextMenuItem("Extended Away");
		statusSubmenu.add(statusXAItem);
		TextMenuItem statusDNDItem = new TextMenuItem("Do not Disturb");
		statusSubmenu.add(statusDNDItem);

		TextMenuItem logoutItem = new TextMenuItem("Logout");
		x.add(logoutItem);

		return ti;
	}

}

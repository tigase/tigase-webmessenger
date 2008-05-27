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

		TextMenuItem logoutItem = new TextMenuItem("Logout");
		x.add(logoutItem);

		return ti;
	}

}

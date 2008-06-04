package tigase.messenger.client;

import tigase.messenger.client.roster.component.RosterPresence;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;

public abstract class MainToolBar extends ToolBar {

	protected CheckMenuItem showOfflineMenu;

	public MainToolBar() {
		createAllMenu();
	}

	protected void createAllMenu() {
		add(getActionMenu());
		add(getViewMenu());
		add(getHelpMenu());
	}

	protected ToolItem getActionMenu() {

		TextToolItem actionsToolItem = new TextToolItem("Actions");
		Menu actionsMenu = new Menu();
		actionsToolItem.setMenu(actionsMenu);

		MenuItem statusMenu = new MenuItem("Set status");
		actionsMenu.add(statusMenu);
		Menu statusSubmenu = new Menu();
		statusMenu.setSubMenu(statusSubmenu);

		MenuItem statusOnlineItem = new MenuItem("Online");
		statusOnlineItem.setIconStyle("buddy-online");
		statusOnlineItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.ONLINE);
			}
		});
		statusSubmenu.add(statusOnlineItem);
		MenuItem statusChatItem = new MenuItem("Free for chat");
		statusChatItem.setIconStyle("buddy-chat");
		statusChatItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.READY_FOR_CHAT);
			}
		});
		statusSubmenu.add(statusChatItem);
		MenuItem statusAwayItem = new MenuItem("Away");
		statusAwayItem.setIconStyle("buddy-away");
		statusAwayItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.AWAY);
			}
		});
		statusSubmenu.add(statusAwayItem);
		MenuItem statusXAItem = new MenuItem("Extended Away");
		statusXAItem.setIconStyle("buddy-xa");
		statusXAItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.XA);
			}
		});
		statusSubmenu.add(statusXAItem);
		MenuItem statusDNDItem = new MenuItem("Do not Disturb");
		statusDNDItem.setIconStyle("buddy-dnd");
		statusDNDItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.DND);
			}
		});
		statusSubmenu.add(statusDNDItem);

		MenuItem logoutItem = new MenuItem("Logout");
		logoutItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.OFFLINE);
			}
		});
		actionsMenu.add(logoutItem);

		return actionsToolItem;
	}

	protected ToolItem getHelpMenu() {
		TextToolItem helpToolItem = new TextToolItem("Help");
		Menu actionsMenu = new Menu();
		helpToolItem.setMenu(actionsMenu);

		MenuItem statusOnlineItem = new MenuItem("About");
		statusOnlineItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				Messenger.instance().showAbout();
			}
		});
		actionsMenu.add(statusOnlineItem);

		return helpToolItem;
	}

	protected ToolItem getViewMenu() {
		TextToolItem helpToolItem = new TextToolItem("View");
		Menu actionsMenu = new Menu();
		helpToolItem.setMenu(actionsMenu);

		showOfflineMenu = new CheckMenuItem("Show offline contacts");
		showOfflineMenu.setChecked(true);
		actionsMenu.add(showOfflineMenu);
		showOfflineMenu.addSelectionListener(new SelectionListener<ComponentEvent>() {});
		showOfflineMenu.addListener(Events.CheckChange, new Listener<MenuEvent>() {

			public void handleEvent(MenuEvent be) {
				showOfflineChanged(showOfflineMenu.isChecked());
			}
		});

		return helpToolItem;
	}

	protected abstract void setNewPresence(RosterPresence presence);

	public void setShowOffline(boolean value) {
		if (showOfflineMenu != null) {
			showOfflineMenu.setChecked(value);
		}
	}

	protected abstract void showOfflineChanged(boolean newValue);
}

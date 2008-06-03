package tigase.messenger.client;

import tigase.messenger.client.roster.component.RosterPresence;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;

public abstract class MainToolBar extends ToolBar {

	public MainToolBar() {
		createAllMenu();
	}

	protected void createAllMenu() {
		add(getActionMenu());
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

		return helpToolItem;
	}

	protected abstract void setNewPresence(RosterPresence presence);
}

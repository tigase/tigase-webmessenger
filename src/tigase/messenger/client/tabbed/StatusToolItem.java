package tigase.messenger.client.tabbed;

import tigase.messenger.client.roster.component.RosterPresence;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;

public abstract class StatusToolItem extends TextToolItem {

	private MenuItem statusDNDItem;
	private MenuItem statusAwayItem;
	private MenuItem statusOnlineItem;
	private MenuItem statusChatItem;
	private MenuItem statusXAItem;
	private MenuItem statusOfflineItem;

	public StatusToolItem() {
		setText("online");
		setIconStyle("buddy-offline");

		Menu statusSubmenu = new Menu();
		setMenu(statusSubmenu);

		statusOnlineItem = new MenuItem("Online");
		statusOnlineItem.setIconStyle("buddy-online");
		statusOnlineItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.ONLINE);
				setPresence(RosterPresence.ONLINE);
			}
		});
		statusSubmenu.add(statusOnlineItem);
		statusChatItem = new MenuItem("Free for chat");
		statusChatItem.setIconStyle("buddy-chat");
		statusChatItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.READY_FOR_CHAT);
				setPresence(RosterPresence.READY_FOR_CHAT);
			}
		});
		statusSubmenu.add(statusChatItem);
		statusAwayItem = new MenuItem("Away");
		statusAwayItem.setIconStyle("buddy-away");
		statusAwayItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.AWAY);
				setPresence(RosterPresence.AWAY);
			}
		});
		statusSubmenu.add(statusAwayItem);
		statusXAItem = new MenuItem("Extended Away");
		statusXAItem.setIconStyle("buddy-xa");
		statusXAItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.XA);
				setPresence(RosterPresence.XA);
			}
		});
		statusSubmenu.add(statusXAItem);
		statusDNDItem = new MenuItem("Do not Disturb");
		statusDNDItem.setIconStyle("buddy-dnd");
		statusDNDItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.DND);
				setPresence(RosterPresence.DND);
			}
		});
		statusSubmenu.add(statusDNDItem);
		statusSubmenu.add(new SeparatorMenuItem());
		statusOfflineItem = new MenuItem("Offline");
		statusOfflineItem.setIconStyle("buddy-offline");
		statusOfflineItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				setNewPresence(RosterPresence.OFFLINE);
				setPresence(RosterPresence.OFFLINE);
			}
		});
		statusSubmenu.add(statusOfflineItem);

	}

	protected abstract void setNewPresence(RosterPresence presence);

	public void setPresence(RosterPresence presence) {
		switch (presence) {
			case ONLINE:
				setIconStyle(statusOnlineItem.getIconStyle());
				setText(statusOnlineItem.getText());
				break;
			case READY_FOR_CHAT:
				setIconStyle(statusChatItem.getIconStyle());
				setText(statusChatItem.getText());
				break;
			case AWAY:
				setIconStyle(statusAwayItem.getIconStyle());
				setText(statusAwayItem.getText());
				break;
			case XA:
				setIconStyle(statusXAItem.getIconStyle());
				setText(statusXAItem.getText());
				break;
			case DND:
				setIconStyle(statusDNDItem.getIconStyle());
				setText(statusDNDItem.getText());
				break;
			case OFFLINE:
				setIconStyle(statusOfflineItem.getIconStyle());
				setText(statusOfflineItem.getText());
				break;

			default:
				break;
		}
	};

}

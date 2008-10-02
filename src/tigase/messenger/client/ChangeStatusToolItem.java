package tigase.messenger.client;

import java.util.ArrayList;

import tigase.gwt.components.roster.client.RosterPresence;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;

public class ChangeStatusToolItem extends TextToolItem {

	public static interface ChangeStatusListener {
		void onNewStatusSelected(RosterPresence newStatus);
	}

	public final static String STATUS_KEY = "s";

	public static void prepareMenu(final Menu menu, final SelectionListener<MenuEvent> sl) {
		MenuItem onlineMenuItem = new MenuItem("Online", "buddy-online", sl);
		onlineMenuItem.setData(STATUS_KEY, RosterPresence.ONLINE);
		menu.add(onlineMenuItem);

		MenuItem chatMenuItem = new MenuItem("Ready for Chat", "buddy-chat", sl);
		chatMenuItem.setData(STATUS_KEY, RosterPresence.READY_FOR_CHAT);
		menu.add(chatMenuItem);

		MenuItem awayMenuItem = new MenuItem("Away", "buddy-away", sl);
		awayMenuItem.setData(STATUS_KEY, RosterPresence.AWAY);
		menu.add(awayMenuItem);

		MenuItem xaMenuItem = new MenuItem("XA", "buddy-xa", sl);
		xaMenuItem.setData(STATUS_KEY, RosterPresence.XA);
		menu.add(xaMenuItem);

		MenuItem dndMenuItem = new MenuItem("DND", "buddy-dnd", sl);
		dndMenuItem.setData(STATUS_KEY, RosterPresence.DND);
		menu.add(dndMenuItem);

		menu.add(new SeparatorMenuItem());

		MenuItem offlineMenuItem = new MenuItem("Offline", "buddy-offline", sl);
		offlineMenuItem.setData(STATUS_KEY, RosterPresence.OFFLINE);
		menu.add(offlineMenuItem);

	}

	private final ArrayList<ChangeStatusListener> listeners = new ArrayList<ChangeStatusListener>();

	private final Menu menu = new Menu();

	public ChangeStatusToolItem() {
		super();
		final SelectionListener<MenuEvent> sl = new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				RosterPresence rp = ce.item.getData(STATUS_KEY);
				for (ChangeStatusListener listener : listeners) {
					listener.onNewStatusSelected(rp);
				}
				// setNewStatus(rp);
			}
		};

		prepareMenu(menu, sl);
		setMenu(menu);
		setNewStatus(RosterPresence.OFFLINE);

		setWidth("100%");
	}

	public void addListener(ChangeStatusListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(ChangeStatusListener listener) {
		this.listeners.remove(listener);
	}

	public void setNewStatus(RosterPresence rp) {
		String text;
		String style;

		switch (rp) {
		case ONLINE:
			text = "Online";
			style = "buddy-online";
			break;
		case READY_FOR_CHAT:
			text = "Ready for Chat";
			style = "buddy-chat";
			break;
		case AWAY:
			text = "Away";
			style = "buddy-away";
			break;
		case XA:
			text = "XA";
			style = "buddy-xa";
			break;
		case DND:
			text = "DND";
			style = "buddy-dnd";
			break;
		case OFFLINE:
			text = "Offline";
			style = "buddy-offline";
			break;
		default:
			text = "unknown";
			style = "buddy-error";
			break;
		}
		setText(text);
		setIconStyle(style);
	}
}

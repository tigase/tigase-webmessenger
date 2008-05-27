package tigase.messenger.client.roster.component;

import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class Item extends SimplePanel {

	private final Group group;

	private final JID jid;

	private final Label label;

	private RosterItem rosterItem;

	Item(Group group, final JID jid, final RosterItem item) {
		super();
		this.rosterItem = item;
		this.group = group;
		this.jid = jid;
		sinkEvents(Event.ONMOUSEDOWN);
		sinkEvents(Event.ONDBLCLICK);

		String name = item.getName();
		if (name == null) {
			name = item.getJid();
		}

		label = new Label(name);
		add(label);
		setStyleName("buddy");
		Group.disableContextMenu(this.getElement());
	}

	public JID getJID() {
		return jid;
	}

	public String getName() {
		return this.label.getText();
	}

	public RosterItem getRosterItem() {
		return this.rosterItem;
	}

	@Override
	public void onBrowserEvent(Event event) {
		DOM.eventCancelBubble(event, true);
		DOM.eventPreventDefault(event);
		if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
			int button = DOM.eventGetButton(event);
			if (button == Event.BUTTON_LEFT) {
				group.select(jid, this);
			} else if (button == Event.BUTTON_RIGHT) {
				group.select(jid, this);
				group.getRoster().callContactContextMenu(event, this);
			}
		} else if (DOM.eventGetType(event) == Event.ONDBLCLICK) {
			group.getRoster().callContactDoubleClick(event, this);
		}
	}

	public boolean update(RosterItem item) {
		this.rosterItem = item;
		boolean changed = !item.getName().equals(this.label.getText());
		this.label.setText(item.getName());
		return changed;
	}

	public boolean update(final RosterPresence p) {
		boolean visible = this.group.showElement(p);
		setVisible(visible);
		setStyleName("buddy");
		switch (p) {
			case ONLINE:
				addStyleName("buddy-online");
				break;
			case READY_FOR_CHAT:
				addStyleName("buddy-chat");
				break;
			case AWAY:
				addStyleName("buddy-away");
				break;
			case XA:
				addStyleName("buddy-xa");
				break;
			case DND:
				addStyleName("buddy-dnd");
				break;
			case OFFLINE:
				addStyleName("buddy-offline");
				break;
			case ASK:
				addStyleName("buddy-ask");
				break;
			case ERROR:
				addStyleName("buddy-error");
				break;

			default:
				break;
		}
		return visible;
	}
}

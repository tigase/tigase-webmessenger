package tigase.gwt.components.roster.client;

import tigase.xmpp4gwt.client.JID;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class Item extends SimplePanel {

	private final Group group;

	private final JID jid;

	private final Label label;

	private Object data;

	public <T extends Object> void setData(T data) {
		this.data = data;
	}

	public <T extends Object> T getData() {
		return (T) data;
	}

	Item(Group group, final JID jid, final String displayedName) {
		super();
		this.group = group;
		this.jid = jid;
		sinkEvents(Event.ONMOUSEDOWN);
		sinkEvents(Event.ONDBLCLICK);

		label = new Label(displayedName);
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

	private Timer timer;

	@Override
	public void onBrowserEvent(final Event event) {
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
		} else if (DOM.eventGetType(event) == Event.ONMOUSEOVER) {
			if (timer == null) {
				timer = new Timer() {
					public void run() {
						group.getRoster().callItemToolTip(event, Item.this);
					}
				};
				timer.schedule(1000);
			}
		} else if (DOM.eventGetType(event) == Event.ONMOUSEOUT) {
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		}
	}

	public boolean update(String displayedName) {
		boolean changed = !displayedName.equals(this.label.getText());
		this.label.setText(displayedName);
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
		case NOAUTH:
			addStyleName("buddy-noauth");
			break;

		default:
			break;
		}
		return visible;
	}
}

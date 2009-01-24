package tigase.gwt.components.roster.client;

import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.JID;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Group extends Composite {

	static native void disableContextMenu(Element elem) /*-{
	   elem.oncontextmenu=function() {  return false; };
	 }-*/;

	private final Map<JID, Item> buddies = new HashMap<JID, Item>();

	private final VerticalPanel elements;

	private final SimplePanel header;

	private String name;

	private boolean open = true;

	private final VerticalPanel panel = new VerticalPanel();

	private final Roster roster;

	private boolean showOffline = true;

	private boolean staticGroup;

	private Timer timer;

	private int visibleContacts;

	private boolean visibleIfEmpty = true;

	Group(final Roster roster, String groupName) {
		this.roster = roster;
		this.name = groupName;
		header = new SimplePanel() {
			@Override
			public void onBrowserEvent(final Event event) {
				DOM.eventCancelBubble(event, true);
				DOM.eventPreventDefault(event);
				if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
					int button = DOM.eventGetButton(event);
					if (button == Event.BUTTON_LEFT) {
						Element target = DOM.eventGetCurrentTarget(event);
						int x = DOM.eventGetClientX(event) - DOM.getAbsoluteLeft(target);
						int y = DOM.eventGetClientY(event) - DOM.getAbsoluteTop(target);

						if (x > 0 && x < 16 && y > 0 && y < 24) {
							setOpen(!open);
						} else {
							Group.this.roster.select(Group.this, Group.this);
						}
					} else if (button == Event.BUTTON_RIGHT) {
						Element target = DOM.eventGetCurrentTarget(event);
						int x = DOM.eventGetClientX(event) - DOM.getAbsoluteLeft(target);
						int y = DOM.eventGetClientY(event) - DOM.getAbsoluteTop(target);

						if (!(x > 0 && x < 16 && y > 0 && y < 24)) {
							Group.this.roster.select(Group.this, Group.this);
							Group.this.roster.callGroupsContextMenu(event, Group.this);
						}
					}
				} else if (DOM.eventGetType(event) == Event.ONDBLCLICK) {
					setOpen(!open);
				} else if (DOM.eventGetType(event) == Event.ONMOUSEOVER) {
					if (timer == null) {
						timer = new Timer() {
							@Override
							public void run() {
								roster.callGropToolTip(event, Group.this);
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
		};
		header.sinkEvents(Event.ONMOUSEDOWN);
		header.sinkEvents(Event.ONDBLCLICK);
		header.sinkEvents(Event.ONMOUSEOVER);
		header.sinkEvents(Event.ONMOUSEOUT);

		elements = new VerticalPanel();

		Label h = new Label(groupName);
		header.add(h);
		header.setStyleName("group-header");

		panel.add(header);
		panel.add(elements);

		elements.setWidth("100%");

		initWidget(panel);
		disableContextMenu(header.getElement());
		setStyleName("roster-group");
		if (open)
			header.addStyleName("open");
		else
			header.addStyleName("close");
	}

	public int getBuddiesCount() {
		return this.buddies.size();
	}

	public String getName() {
		return this.name;
	}

	Roster getRoster() {
		return roster;
	}

	public boolean isShowOffline() {
		return this.showOffline;
	}

	public boolean isStaticGroup() {
		return this.staticGroup;
	}

	public boolean isVisibleIfEmpty() {
		return visibleIfEmpty;
	}

	public void remove(JID jid) {
		Item it = this.buddies.remove(jid);
		if (it != null)
			elements.remove(it);
	}

	void select(final JID jid, final Item panel) {
		this.roster.select(panel, jid, this);
	}

	public void setOpen(boolean open) {
		if (open == this.open)
			return;
		this.open = open;
		elements.setVisible(open);
		if (open) {
			header.addStyleName("open");
			header.removeStyleName("close");
		} else {
			header.removeStyleName("open");
			header.addStyleName("close");
		}
	}

	public void setShowOffline(boolean newValue) {
		this.showOffline = newValue;
		this.visibleContacts = 0;
		for (Item item : this.buddies.values()) {
			if (item.update(roster.getPresenceCallback().getRosterPresence(item.getJID()))) {
				visibleContacts++;
			}
		}
		setVisible(this.visibleIfEmpty || this.visibleContacts > 0);
	}

	void setStaticGroup(boolean staticGroup) {
		this.staticGroup = staticGroup;
	}

	void setVisibleIfEmpty(boolean visibleIfEmpty) {
		this.visibleIfEmpty = visibleIfEmpty;
		setVisible(this.visibleIfEmpty || this.visibleContacts > 0);
	}

	protected boolean showElement(RosterPresence p) {
		if (showOffline) {
			return true;
		} else {
			return p != null && p != RosterPresence.ERROR && p != RosterPresence.NOAUTH && p != RosterPresence.ASK && p != RosterPresence.OFFLINE;
		}
	}

	public void updatePresence(final JID jid, final RosterPresence p) {
		Item it = this.buddies.get(jid);
		if (it != null) {
			if (it.isVisible())
				this.visibleContacts--;
			if (it.update(p))
				this.visibleContacts++;
		}
		setVisible(this.visibleIfEmpty || this.visibleContacts > 0);
	}

	public void updateRosterItem(final JID jid, final String displayedName) {
		Item ri = this.buddies.get(jid);

		if (ri == null) {
			ri = new Item(this, jid, displayedName);
			ri.setVisible(showOffline);
			if (ri.update(roster.getPresenceCallback().getRosterPresence(jid)))
				this.visibleContacts++;
			int index = 0;
			for (int i = 0; i < this.elements.getWidgetCount(); i++) {
				Widget w = this.elements.getWidget(i);
				if (w instanceof Item) {
					if (this.roster.getContactComparator().compare((Item) w, ri) < 0) {
						index = i + 1;
					}
				}
			}
			elements.insert(ri, index);
			this.buddies.put(jid, ri);
		} else {
			boolean changed = ri.update(displayedName);
			if (changed) {
				elements.remove(ri);
				int index = 0;
				for (int i = 0; i < this.elements.getWidgetCount(); i++) {
					Widget w = this.elements.getWidget(i);
					if (w instanceof Item) {
						if (this.roster.getContactComparator().compare((Item) w, ri) < 0) {
							index = i + 1;
						}
					}
				}
				elements.insert(ri, index);
			}
		}

		setVisible(this.visibleIfEmpty || this.visibleContacts > 0);
	}
}

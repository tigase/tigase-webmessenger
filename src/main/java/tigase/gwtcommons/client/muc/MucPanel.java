package tigase.gwtcommons.client.muc;

import java.util.Date;

import tigase.gwtcommons.client.MessagePanel;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.MucEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.modules.muc.XMucUserElement;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.delay.XmppDelay;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;

public class MucPanel extends ContentPanel {

	private final MessagePanel messagePanel = new MessagePanel();

	private final OccupantsListPanel occupantsList;

	private int occupantsListSize = 150;

	private boolean occupantsListVisible = true;

	private Room room;

	private final Button sendButton;

	private final ContentPanel southPanel = new ContentPanel(new FitLayout());

	private final TextArea text = new TextArea();

	private int textAreaSize = 100;

	public MucPanel() {
		this(null);
	}

	public MucPanel(Room $room) {
		setHeaderVisible(false);
		setLayout(new BorderLayout());

		this.occupantsList = new OccupantsListPanel(room);

		text.setBorders(false);
		southPanel.setBodyBorder(false);
		southPanel.setBorders(false);
		southPanel.setFrame(false);
		southPanel.setHeaderVisible(false);
		southPanel.add(text);

		setPanelEnabled(false);

		this.text.addListener(Events.KeyPress, new Listener<FieldEvent>() {

			public void handleEvent(FieldEvent be) {
				if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
					be.setCancelled(true);
					be.preventDefault();
					sendMessage();
				}
			}
		});
		this.sendButton = new Button("Send", new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				sendMessage();
			}
		});

		ToolBar tb = new ToolBar();
		tb.add(new FillToolItem());
		tb.add(this.sendButton);
		southPanel.setBottomComponent(tb);

		setRoom($room);
	}

	public int getOccupantsListSize() {
		return occupantsListSize;
	}

	public Room getRoom() {
		return room;
	}

	public int getTextAreaSize() {
		return textAreaSize;
	}

	public void onPresenceReceived(String nickname, Presence presence) throws XMLException {
		if (room == null)
			return;
		XMucUserElement x = XMucUserElement.extract(presence);
		if (!text.isEnabled() && x != null && x.getStatuses().contains(110)) {
			setPanelEnabled(true);
		}
		if (presence.getType() == StanzaType.unavailable
				&& (x != null && x.getStatuses().contains(110) || nickname != null && nickname.equals(room.getNickname()))) {
			setPanelEnabled(false);
		}
	}

	@Override
	protected void onRender(Element parent, int pos) {
		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, textAreaSize);
		southData.setSplit(true);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setMargins(new Margins(5, 0, 0, 0));

		BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, occupantsListSize);
		eastData.setSplit(true);
		eastData.setCollapsible(true);
		eastData.setMargins(new Margins(0, 0, 0, 5));

		add(messagePanel, centerData);
		add(southPanel, southData);

		if (occupantsListVisible)
			add(this.occupantsList, eastData);

		super.onRender(parent, pos);
	}

	private String prepareStatus(Presence presence) throws XMLException {
		String r = " is now ";
		switch (presence.getShow()) {
		case online:
			r += "online";
			break;
		case away:
			r += "away";
			break;
		case chat:
			r += "free for chat";
			break;
		case dnd:
			r += "free for chat";
			break;
		case xa:
			r += "extended away";
			break;
		}
		if (presence.getStatus() != null) {
			r += " (" + presence.getStatus() + ")";
		}
		return r;
	}

	public void process(Message message) {
		if (room == null)
			return;
		try {
			String nick = message.getFrom().getResource();
			XMucUserElement x = XMucUserElement.extract(message);
			XmppDelay delay = XmppDelay.extract(message);
			Date d = delay == null ? new Date() : delay.getStamp();
			d = delay == null ? new Date() : d;
			if (message.getSubject() != null) {
				messagePanel.addAppMessage(d, "*" + nick + " has set the topic to: " + message.getSubject());
			}
			if (message.getBody() != null) {
				if (nick == null || nick.length() == 0)
					messagePanel.addAppMessage(d, message.getBody());
				else if (nick != null && nick.equals(room.getNickname()))
					messagePanel.addMineMessage(d, nick, message.getBody());
				else
					messagePanel.addHisMessage(nick.hashCode() % 5, d, nick, message.getBody());
			}
			show(x, "Now any occupant is allowed to see the user's full JID", 100);
			show(x, "Your affiliation changed while not in the room", 101);
			show(x, "Room now shows unavailable members", 102);
			show(x, "Room now does not show unavailable members", 103);
			show(x, "Non-privacy-related room configuration change has occurred", 104);
			show(x, "Room logging is now enabled", 170);
			show(x, "Room logging is now disabled", 171);
			show(x, "Room is now non-anonymous", 172);
			show(x, "Room is now semi-anonymous", 173);
			show(x, "Room is now fully-anonymous", 174);

			// TODO process messages statuses
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public void process(MucEvent event) throws XMLException {
		final XMucUserElement x = XMucUserElement.extract(event.getPresence());
		if (event.getType() == MucModule.OccupantComes) {
			String m = event.getNickname() + " join to room";
			if (x.getRole() != null) {
				m += " as " + x.getRole();
			}
			messagePanel.addAppMessage(m);
			show(x, "New chat room has been created", 110, 201);
			show(x, "Any occupant is allowed to see the your full JID", 100, 110);
			show(x, "Room logging is enabled", 110, 170);
			show(x, "Service has assigned or modified your nick", 110, 210);
			occupantsList.add(event.getOccupant(), event.getPresence());
			onPresenceReceived(event.getNickname(), event.getPresence());
		} else if (event.getType() == MucModule.OccupantLeaved) {
			// messagePanel.addAppMessage(event.getNickname() + " leaved room");
			occupantsList.remove(event.getOccupant());
			onPresenceReceived(event.getNickname(), event.getPresence());
			if (show(x, event.getNickname() + " has been banned from room", 301))
				;
			else if (show(x, event.getNickname() + " has been kicked from room", 307))
				;
			else
				messagePanel.addAppMessage(event.getNickname() + " leaved room");
		} else if (event.getType() == MucModule.OccupantChangedPresence) {
			occupantsList.update(event.getOccupant(), event.getPresence());
			messagePanel.addAppMessage(event.getNickname() + prepareStatus(event.getPresence()));
			onPresenceReceived(event.getNickname(), event.getPresence());
		} else if (event.getType() == MucModule.OccupantChangedNick) {
			occupantsList.update(event.getOccupant(), event.getPresence());
			messagePanel.addAppMessage(event.getOldNickname() + " is known as " + event.getNickname());
			onPresenceReceived(event.getNickname(), event.getPresence());
		}
	}

	private void sendMessage() {
		if (room == null)
			return;
		String v = text.getValue();
		text.clear();
		try {
			if (v != null)
				v = SafeHtmlUtils.fromString(v).asString();
			MucPanel.this.room.sendMessage(v);
		} catch (Exception e) {
			messagePanel.addErrorMessage(e.getMessage());
		}
	}

	public void setOccupantsListSize(int occupantsListSize) {
		this.occupantsListSize = occupantsListSize;
	}

	public void setOccupantsListVisible(boolean b) {
		this.occupantsListVisible = b;
	}

	public void setPanelEnabled(boolean b) {
		text.setEnabled(b);
		occupantsList.setEnabled(b);
	}

	public void setRoom(Room room) {
		this.room = room;
		if (this.room != null)
			messagePanel.addAppMessage("Joining to room " + room.getRoomJid().toString());

	}

	public void setTextAreaSize(int textAreaSize) {
		this.textAreaSize = textAreaSize;
	}

	private boolean show(XMucUserElement x, String message, int... statuses) {
		if (x == null)
			return false;

		for (int i : statuses)
			if (!x.getStatuses().contains(i))
				return false;

		messagePanel.addAppMessage(message);
		return true;
	}

	private void showStatuses(String nickname, XMucUserElement x, Presence presence, Message message) throws XMLException {
		if (x == null)
			return;

		if (x.getJID() != null) {
			nickname += " (" + x.getJID() + ")";
		}

		if (x.getStatuses().contains(100))
			messagePanel.addAppMessage("Any occupant is allowed to see your full JID");
		if (x.getStatuses().contains(101))
			messagePanel.addAppMessage("Your affiliation changed while not in the room");
		if (x.getStatuses().contains(102))
			messagePanel.addAppMessage("Room now shows unavailable members");
		if (x.getStatuses().contains(103))
			messagePanel.addAppMessage("Room now does not show unavailable members");
		if (x.getStatuses().contains(104))
			messagePanel.addAppMessage("Non-privacy-related room configuration change has occurred");
		if (x.getStatuses().contains(170))
			messagePanel.addAppMessage("Room logging is now enabled");
		if (x.getStatuses().contains(171))
			messagePanel.addAppMessage("Room logging is now disabled");
		if (x.getStatuses().contains(172))
			messagePanel.addAppMessage("Room is now non-anonymous");
		if (x.getStatuses().contains(173))
			messagePanel.addAppMessage("Room is now semi-anonymous");
		if (x.getStatuses().contains(174))
			messagePanel.addAppMessage("Room is now fully-anonymous");
		if (x.getStatuses().contains(201))
			messagePanel.addAppMessage("New room has been created");
		if (x.getStatuses().contains(210))
			messagePanel.addAppMessage("Service has assigned or modified your roomnick");
		if (x.getStatuses().contains(301))
			messagePanel.addAppMessage(nickname + " has been banned from the room");

		if (x.getStatuses().contains(303)) {
			messagePanel.addAppMessage(nickname + " is known as " + x.getNick());
		} else if (presence != null && presence.getType() == StanzaType.unavailable) {
			messagePanel.addAppMessage(nickname + " leaved room");
		} else if (presence != null && presence.getType() == null) {
			String m = nickname + " join to room";
			if (x.getRole() != null) {
				m += " as " + x.getRole();
			}
			messagePanel.addAppMessage(m);
		}

		if (x.getStatuses().contains(307))
			messagePanel.addAppMessage(nickname + " has been kicked from the room");

	}

}

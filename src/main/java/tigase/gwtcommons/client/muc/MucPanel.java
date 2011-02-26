package tigase.gwtcommons.client.muc;

import java.util.Date;

import tigase.gwtcommons.client.MessagePanel;
import tigase.gwtcommons.client.Translations;
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
		this.sendButton = new Button(Translations.instance.sendButton(), new SelectionListener<ButtonEvent>() {

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

	public MessagePanel getMessagePanel() {
		return messagePanel;
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
		String r = " " + Translations.instance.xmppMucIsNow() + " ";
		switch (presence.getShow()) {
		case online:
			r += Translations.instance.xmppPresenceOnline();
			break;
		case away:
			r += Translations.instance.xmppPresenceAway();
			break;
		case chat:
			r += Translations.instance.xmppPresenceChat();
			break;
		case dnd:
			r += Translations.instance.xmppPresenceDND();
			break;
		case xa:
			r += Translations.instance.xmppPresenceXA();
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

				messagePanel.addAppMessage(d, Translations.instance.mucMessageHasSetTopic(nick, message.getSubject()));
			}
			if (message.getBody() != null) {
				if (nick == null || nick.length() == 0)
					messagePanel.addAppMessage(d, message.getBody());
				else if (nick != null && nick.equals(room.getNickname()))
					messagePanel.addMineMessage(d, nick, message.getBody());
				else
					messagePanel.addHisMessage(nick.hashCode() % 5, d, nick, message.getBody());
			}
			show(x, Translations.instance.mucMessageX100(), 100);
			show(x, Translations.instance.mucMessageX101(), 101);
			show(x, Translations.instance.mucMessageX102(), 102);
			show(x, Translations.instance.mucMessageX103(), 103);
			show(x, Translations.instance.mucMessageX104(), 104);
			show(x, Translations.instance.mucMessageX170(), 170);
			show(x, Translations.instance.mucMessageX171(), 171);
			show(x, Translations.instance.mucMessageX172(), 172);
			show(x, Translations.instance.mucMessageX173(), 173);
			show(x, Translations.instance.mucMessageX174(), 174);

			// TODO process messages statuses
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public void process(MucEvent event) throws XMLException {
		final XMucUserElement x = XMucUserElement.extract(event.getPresence());
		if (x != null && event.getType() == MucModule.OccupantComes) {
			if (x.getRole() != null)
				messagePanel.addAppMessage(Translations.instance.mucMessageXJoinAs(event.getNickname(), x.getRole().name()));
			else
				messagePanel.addAppMessage(Translations.instance.mucMessageXJoin(event.getNickname()));
			show(x, Translations.instance.mucMessageX110201(), 110, 201);
			show(x, Translations.instance.mucMessageX100110(), 100, 110);
			show(x, Translations.instance.mucMessageX110170(), 110, 170);
			show(x, Translations.instance.mucMessageX110210(), 110, 210);
			occupantsList.add(event.getOccupant(), event.getPresence());
			onPresenceReceived(event.getNickname(), event.getPresence());
		} else if (x != null && event.getType() == MucModule.OccupantLeaved) {
			occupantsList.remove(event.getOccupant());
			onPresenceReceived(event.getNickname(), event.getPresence());
			if (show(x, Translations.instance.mucMessageX301(event.getNickname()), 301))
				;
			else if (show(x, Translations.instance.mucMessageX307(event.getNickname()), 307))
				;
			else
				messagePanel.addAppMessage(Translations.instance.mucMessageXLeaved(event.getNickname()));
		} else if (event.getType() == MucModule.OccupantChangedPresence) {
			occupantsList.update(event.getOccupant(), event.getPresence());
			messagePanel.addAppMessage(event.getNickname() + prepareStatus(event.getPresence()));
			onPresenceReceived(event.getNickname(), event.getPresence());
		} else if (event.getType() == MucModule.OccupantChangedNick) {
			occupantsList.update(event.getOccupant(), event.getPresence());
			messagePanel.addAppMessage(Translations.instance.mucMessageXRename(event.getOldNickname(), event.getNickname()));
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
			messagePanel.addAppMessage(Translations.instance.mucJoining(room.getRoomJid().toString()));

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

}

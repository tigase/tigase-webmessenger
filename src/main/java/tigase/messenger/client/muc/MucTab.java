package tigase.messenger.client.muc;

import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.MucEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.messenger.client.MessagePanel;
import tigase.messenger.client.Tab;

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

public class MucTab extends Tab {

	private final MessagePanel messagePanel = new MessagePanel();

	private final OccupantsListPanel occupantsList;

	private Room room;

	private final Button sendButton;

	private final ContentPanel southPanel = new ContentPanel(new FitLayout());

	private final TextArea text = new TextArea();

	public MucTab(Room room) {
		this.room = room;

		setText("MUC room ");
		setClosable(true);
		setLayout(new BorderLayout());

		this.occupantsList = new OccupantsListPanel(room);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setMargins(new Margins(5, 0, 0, 0));

		BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, 150);
		eastData.setSplit(true);
		eastData.setCollapsible(true);
		eastData.setMargins(new Margins(0, 0, 0, 5));

		text.setBorders(false);
		southPanel.setBodyBorder(false);
		southPanel.setBorders(false);
		southPanel.setFrame(false);
		southPanel.setHeaderVisible(false);
		southPanel.add(text);

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

		add(messagePanel, centerData);
		add(southPanel, southData);
		add(this.occupantsList, eastData);
	}

	public Room getRoom() {
		return room;
	}

	public void process(Message message) {
		try {
			String nick = message.getFrom().getResource();
			if (message.getBody() != null) {
				if (nick == null)
					messagePanel.addAppMessage(message.getBody());
				if (nick != null && nick.equals(room.getNickname()))
					messagePanel.addMineMessage(nick, message.getBody());
				else
					messagePanel.addHisMessage(nick.hashCode() % 5, nick, message.getBody());
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public void process(MucEvent event) throws XMLException {
		if (event.getType() == MucModule.OccupantComes) {
			messagePanel.addAppMessage(event.getNickname() + " join to room");
			occupantsList.add(event.getPresence().getFrom(), event.getPresence());
		} else if (event.getType() == MucModule.OccupantLeaved) {
			messagePanel.addAppMessage(event.getNickname() + " leaved room");
			occupantsList.remove(event.getPresence().getFrom());
		} else if (event.getType() == MucModule.OccupantChangedPresence) {
			occupantsList.update(event.getPresence().getFrom(), event.getPresence());
		}
	}

	private void sendMessage() {
		String v = text.getValue();
		text.clear();
		try {
			MucTab.this.room.sendMessage(v);
		} catch (Exception e) {
			messagePanel.addErrorMessage(e.getMessage());
		}
	}

}

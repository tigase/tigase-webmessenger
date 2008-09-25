package tigase.messenger.client;

import tigase.gwt.components.roster.client.Group;
import tigase.gwt.components.roster.client.Item;
import tigase.gwt.components.roster.client.Roster;
import tigase.gwt.components.roster.client.RosterListener;
import tigase.xmpp4gwt.client.Bosh2Connector;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.stanzas.Message;
import tigase.xmpp4gwt.client.xmpp.message.Chat;
import tigase.xmpp4gwt.client.xmpp.message.ChatListener;
import tigase.xmpp4gwt.client.xmpp.message.ChatManager;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

public class TabbedViewport extends Viewport implements ChatListener<ChatTab>, RosterListener {

	private ChatManager<ChatTab> chatManager;

	private final Listener<TabPanelEvent> chatTabCloseListener = new Listener<TabPanelEvent>() {

		public void handleEvent(TabPanelEvent be) {
			if (be.item instanceof ChatTab) {
				((ChatTab) be.item).getChatItem().remove();
			}
		}
	};

	private final Roster rosterComponent;

	private final TabPanel tabPanel = new TabPanel();

	private final ToolBar toolBar = new ToolBar();

	public TabbedViewport(Roster rosterComponent, ChatManager<ChatTab> chatManager) {
		this.rosterComponent = rosterComponent;
		this.chatManager = chatManager;
		this.chatManager.addListener(this);
	}

	public void afterRosterChange() {
		// TODO Auto-generated method stub

	}

	public void onContactContextMenu(Event event, Item item) {
		// TODO Auto-generated method stub

	}

	public void onContactDoubleClick(Item item) {
		Chat chat = this.chatManager.startChat(item.getJID());
	}

	public void onGroupContextMenu(Event event, Group group) {
		// TODO Auto-generated method stub

	}

	public void onMessageReceived(Chat<ChatTab> chat, Message message) {
		// TODO Auto-generated method stub
		System.out.println("mamy");
		ChatTab ct = chat.getUserData();
		if (ct != null) {
			ct.process(message);
			if (this.tabPanel.getSelectedItem() != ct) {
				ct.setUnread();
			}
		}
	}

	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		setLayout(new BorderLayout());

		ContentPanel north = new ContentPanel();
		north.setHeaderVisible(false);
		north.setBodyBorder(false);
		north.add(this.toolBar);

		TextToolItem item1 = new TextToolItem("Button w/ Menu");
		toolBar.add(item1);

		ContentPanel west = new ContentPanel();
		west.setScrollMode(Scroll.AUTO);
		west.add(this.rosterComponent);

		ContentPanel east = new ContentPanel();
		ContentPanel south = new ContentPanel();

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 26, 26, 26);
		northData.setCollapsible(false);
		northData.setFloatable(false);
		northData.setSplit(false);
		northData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200);
		westData.setSplit(true);
		westData.setCollapsible(true);
		westData.setMargins(new Margins(5));

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(5, 0, 5, 0));

		BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, 200);
		eastData.setSplit(true);
		eastData.setCollapsible(true);
		eastData.setMargins(new Margins(5));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setMargins(new Margins(0, 5, 5, 5));

		TabItem item = new TabItem();
		item.setText("Tigase Messenger");
		item.setIconStyle("icon-tabs");
		tabPanel.add(item);
		tabPanel.setSelection(item);

		if (Messenger.config().isDebugEnabled()) {
			DebugTab dt = new DebugTab((Bosh2Connector) Messenger.session().getConnector());
			tabPanel.add(dt);
			tabPanel.setSelection(dt);
			Messenger.session().getConnector().addListener(dt);
		}

		add(north, northData);
		add(west, westData);
		add(tabPanel, centerData);
		add(east, eastData);
		add(south, southData);

		this.rosterComponent.addListener(this);
	}

	public void onRosterItemSelect(JID jid) {
		// TODO Auto-generated method stub

	}

	public void onStartNewChat(Chat<ChatTab> chat) {
		if (chat.getUserData() == null) {
			ChatTab ct = new ChatTab(chat, Messenger.session().getRosterPlugin());
			ct.addListener(Events.Close, this.chatTabCloseListener);
			chat.setUserData(ct);
			this.tabPanel.add(ct);
			this.tabPanel.setSelection(ct);
		}
	}
}

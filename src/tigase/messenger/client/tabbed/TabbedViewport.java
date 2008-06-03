package tigase.messenger.client.tabbed;

import java.util.List;

import tigase.messenger.client.ChatManager;
import tigase.messenger.client.ChatSet;
import tigase.messenger.client.MainToolBar;
import tigase.messenger.client.Messenger;
import tigase.messenger.client.roster.component.PresenceCallback;
import tigase.messenger.client.roster.component.Roster;
import tigase.messenger.client.roster.component.RosterPresence;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.Session;
import tigase.xmpp4gwt.client.xmpp.message.Message;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceItem;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class TabbedViewport extends Viewport implements ChatManager {

	private final Roster rosterComponent;

	private final TabPanel chatTabFolder = new TabPanel();

	protected ContentPanel createRosterPanel() {
		ContentPanel panel = new ContentPanel();
		panel.getHeader().setText("Buddies");
		// panel.setHeaderVisible(false);

		panel.setLayout(new BorderLayout());
		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 30);
		southData.setSplit(false);
		southData.setMargins(new Margins(0, 0, 0, 0));

		ContentPanel center = new ContentPanel();
		center.setHeaderVisible(false);
		center.setScrollMode(Scroll.AUTO);

		center.add(this.rosterComponent);

		ToolBar toolBar = new ToolBar();

		ContentPanel south = new ContentPanel();
		south.setHeaderVisible(false);

		panel.add(center, centerData);
		panel.add(south, southData);

		return panel;

	}

	public TabbedViewport(Roster rosterComponent) {
		super();
		this.rosterComponent = rosterComponent;

		chatTabFolder.addListener(Events.BeforeRemove, new Listener<TabPanelEvent>() {

			public void handleEvent(TabPanelEvent be) {
				System.out.println("zmakło " + be.container + "  " + be.item + "   " + be.component);
				chats.removeChatData(be.item);
			}
		});

		BorderLayout layout = new BorderLayout();
		layout.setEnableState(false);
		setLayout(layout);

		ToolBar toolBar = getMainToolBar();
		ContentPanel center = new ContentPanel();
		ContentPanel south = new ContentPanel();

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 30);
		northData.setSplit(false);
		northData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200);
		westData.setSplit(true);
		westData.setCollapsible(true);
		westData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setMargins(new Margins(0, 0, 0, 0));

		Viewport x = new Viewport();

		chatTabFolder.setWidth("100%");
		chatTabFolder.setAutoWidth(true);
		chatTabFolder.setTabScroll(true);

		// tabFolder.add(new ChatTab());

		// x.add(tabFolder);

		// center.add(x);

		add(toolBar, northData);
		add(chatTabFolder, centerData);
		add(createRosterPanel(), westData);
		add(south, southData);

	}

	protected MainToolBar getMainToolBar() {
		return new MainToolBar();
	}

	private final ChatSet<ChatTab> chats = new ChatSet<ChatTab>();

	private PresenceCallback presenceCallback;

	public void openChatWith(final JID jid, final boolean focus) {
		final String threadId = Session.nextId();
		PresenceItem pi = null;
		if (jid.getResource() == null) {
			pi = Messenger.session().getPresencePlugin().getPresenceitemByBareJid(jid.toStringBare());
		}
		JID buddyJid = jid;
		if (pi != null) {
			buddyJid = pi.getJid();
		}
		ChatTab chatTab = new ChatTab(buddyJid, threadId);
		chats.addChatData(buddyJid, threadId, chatTab);
		chatTabFolder.add(chatTab);
		RosterPresence rp = this.presenceCallback.getRosterPresence(buddyJid);
		chatTab.setPresenceIcon(rp);
		if (focus) {
			chatTabFolder.setSelection(chatTab);
		}
		chatTabFolder.recalculate();
	}

	public void process(Message message) {
		ChatTab tab = chats.getChatData(message.getFrom(), message.getThread());
		if (tab == null) {
			tab = new ChatTab(message.getFrom(), message.getThread());
			chats.addChatData(message.getFrom(), message.getThread(), tab);
			RosterPresence rp = this.presenceCallback.getRosterPresence(message.getFrom());
			tab.setPresenceIcon(rp);
			chatTabFolder.add(tab);
			if (chatTabFolder.getSelectedItem() == null) {
				chatTabFolder.setSelection(tab);
			}
		}
		tab.process(message);
	}

	public void updatePresence(PresenceItem presenceItem) {
		List<ChatTab> cl = chats.getChatList(presenceItem.getJid());
		RosterPresence rp = this.presenceCallback.getRosterPresence(presenceItem.getJid());
		for (ChatTab chatTab : cl) {
			chatTab.setPresenceIcon(rp);
		}
	}

	public void setPresenceCallback(PresenceCallback presenceCallback) {
		this.presenceCallback = presenceCallback;
	}
}

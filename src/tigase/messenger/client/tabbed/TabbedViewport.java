package tigase.messenger.client.tabbed;

import tigase.messenger.client.ChatManager;
import tigase.messenger.client.ChatSet;
import tigase.messenger.client.MainToolBar;
import tigase.messenger.client.Messenger;
import tigase.messenger.client.roster.component.Roster;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.Session;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceItem;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
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

	public TabbedViewport(Roster rosterComponent) {
		super();
		this.rosterComponent = rosterComponent;

		chatTabFolder.addListener(Events.BeforeRemove, new Listener<TabPanelEvent>() {

			public void handleEvent(TabPanelEvent be) {
				System.out.println("zmakło " + be.container+ "  "+be.item+"   "+be.component);
				chats.removeChatData(be.item);
			}
		});

		BorderLayout layout = new BorderLayout();
		layout.setEnableState(false);
		setLayout(layout);

		ToolBar toolBar = getMainToolBar();
		ContentPanel west = new ContentPanel();
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

		west.add(this.rosterComponent);
		west.setScrollMode(Scroll.AUTO);

		Viewport x = new Viewport();

		chatTabFolder.setWidth("100%");
		chatTabFolder.setAutoWidth(true);
		chatTabFolder.setTabScroll(true);

		// tabFolder.add(new ChatTab());
		// tabFolder.add(new ChatTab());

		// x.add(tabFolder);

		// center.add(x);

		add(toolBar, northData);
		add(chatTabFolder, centerData);
		add(west, westData);
		add(south, southData);

	}

	protected MainToolBar getMainToolBar() {
		return new MainToolBar();
	}

	private final ChatSet<ChatTab> chats = new ChatSet<ChatTab>();

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
		if(focus){
			chatTabFolder.setSelection(chatTab);
		}
	}
}

package tigase.messenger.client;

import tigase.gwt.components.roster.client.Group;
import tigase.gwt.components.roster.client.Item;
import tigase.gwt.components.roster.client.Roster;
import tigase.gwt.components.roster.client.RosterListener;
import tigase.gwt.components.roster.client.RosterPresence;
import tigase.messenger.client.ChangeStatusToolItem.ChangeStatusListener;
import tigase.xmpp4gwt.client.Bosh2Connector;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.stanzas.Message;
import tigase.xmpp4gwt.client.stanzas.Presence.Show;
import tigase.xmpp4gwt.client.xmpp.message.Chat;
import tigase.xmpp4gwt.client.xmpp.message.ChatListener;
import tigase.xmpp4gwt.client.xmpp.message.ChatManager;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem.Subscription;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

public class TabbedViewport extends Viewport implements ChatListener<ChatTab>, RosterListener {

	public static Show rosterPresence2Show(RosterPresence rp) {
		switch (rp) {
		case ONLINE:
			return null;
		case READY_FOR_CHAT:
			return Show.chat;
		case AWAY:
			return Show.away;
		case XA:
			return Show.xa;
		case DND:
			return Show.dnd;
		default:
			return Show.notSpecified;
		}
	}

	private ChatManager<ChatTab> chatManager;

	private final Listener<TabPanelEvent> chatTabCloseListener = new Listener<TabPanelEvent>() {

		public void handleEvent(TabPanelEvent be) {
			if (be.item instanceof ChatTab) {
				((ChatTab) be.item).getChatItem().remove();
			}
		}
	};

	private DebugTab debugTab;

	private final Roster rosterComponent;

	private final ChangeStatusToolItem statusToolItem = new ChangeStatusToolItem();

	private final TabPanel tabPanel = new TabPanel();

	private final ToolBar toolBar = new ToolBar();

	public TabbedViewport(Roster rosterComponent, ChatManager<ChatTab> chatManager) {
		tabPanel.setTabScroll(true);
		this.rosterComponent = rosterComponent;
		this.chatManager = chatManager;
		this.chatManager.addListener(this);
		this.statusToolItem.addListener(new ChangeStatusListener() {

			public void onNewStatusSelected(RosterPresence newStatus) {
				newStatusSet(newStatus);
			}
		});
		this.tabPanel.addListener(Events.Select, new Listener<TabPanelEvent>() {

			public void handleEvent(TabPanelEvent be) {
				if (be.item instanceof ChatTab) {
					((ChatTab) be.item).setReaded();
				}
			}
		});
	}

	public void afterRosterChange() {

	}

	private void allowSubscription() {
		final JID jid = rosterComponent.getSelectedJID();
		if (jid != null) {
			Messenger.session().getPresencePlugin().subscribed(jid);
			MessageBox box = new MessageBox();
			box.setModal(false);
			box.setTitle("Authorization has been sent");
			box.setMessage("Now '" + jid.toStringBare() + "' will know your status.");
			box.setButtons(MessageBox.OK);
			box.setIcon(MessageBox.INFO);
			box.show();
		}
	}

	private void askSubscription() {
		final JID jid = rosterComponent.getSelectedJID();
		if (jid != null) {
			Messenger.session().getPresencePlugin().subscribe(jid);
			MessageBox box = new MessageBox();
			box.setModal(false);
			box.setTitle("Subscription request has been sent");
			box.setMessage("If '" + jid.toStringBare() + "' accepts this request you will know his or her status.");
			box.setButtons(MessageBox.OK);
			box.setIcon(MessageBox.INFO);
			box.show();
		}
	}

	private void forbidSubscription() {
		final JID jid = rosterComponent.getSelectedJID();
		if (jid != null) {
			Messenger.session().getPresencePlugin().unsubscribed(jid);
			MessageBox box = new MessageBox();
			box.setModal(false);
			box.setTitle("Authorization has been removed");
			box.setMessage("Now '" + jid.toStringBare() + "' will always see you as offline.");
			box.setButtons(MessageBox.OK);
			box.setIcon(MessageBox.INFO);
			box.show();
		}
	}

	public ChangeStatusToolItem getStatusToolItem() {
		return statusToolItem;
	}

	private void newStatusSet(RosterPresence newStatus) {
		if (newStatus == RosterPresence.OFFLINE) {
			Messenger.session().logout();
			statusToolItem.setNewStatus(RosterPresence.OFFLINE);
			rosterComponent.reset();
		} else if (Messenger.session().isActive()) {
			Messenger.session().getPresencePlugin().sendStatus(rosterPresence2Show(newStatus));
			statusToolItem.setNewStatus(newStatus);
		} else if (Messenger.session().getConnector().isDisconnected()) {
			Messenger.instance().openLoginDialog(rosterPresence2Show(newStatus));
		}
	}

	public void onContactContextMenu(Event event, Item item) {
	}

	public void onContactDoubleClick(Item item) {
		Chat chat = this.chatManager.startChat(item.getJID());
	}

	public void onGroupContextMenu(Event event, Group group) {

	}

	public void onMessageReceived(Chat<ChatTab> chat, Message message) {
		System.out.println("mamy");
		ChatTab ct = chat.getUserData();
		if (ct != null) {
			ct.process(message);
			if (this.tabPanel.getSelectedItem() != ct && message.getBody() != null) {
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

		TextToolItem actionToolItem = new TextToolItem("Actions");
		actionToolItem.setMenu(prepareActionMenu());

		TextToolItem contactsToolItem = new TextToolItem("Contacts");
		contactsToolItem.setMenu(prepareContactMenu());

		TextToolItem viewToolItem = new TextToolItem("View");
		viewToolItem.setMenu(prepareViewMenu());

		toolBar.add(actionToolItem);
		toolBar.add(contactsToolItem);
		toolBar.add(viewToolItem);

		ContentPanel west = prepareRosterPanel();

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
		item.getHeader().addStyleName("unread");

		tabPanel.add(item);
		tabPanel.setSelection(item);

		if (Messenger.config().isDebugEnabled()) {
			openDebugTab();
		}

		add(north, northData);
		add(west, westData);
		add(tabPanel, centerData);
		// add(east, eastData);
		// add(south, southData);

		this.rosterComponent.addListener(this);
	}

	public void onRosterItemSelect(JID jid) {

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

	private void openDebugTab() {
		if (debugTab == null) {
			debugTab = new DebugTab((Bosh2Connector) Messenger.session().getConnector());
			tabPanel.add(debugTab);
			tabPanel.setSelection(debugTab);
			Messenger.session().getConnector().addListener(debugTab);

			debugTab.addListener(Events.Close, new Listener<TabPanelEvent>() {

				public void handleEvent(TabPanelEvent be) {
					Messenger.session().getConnector().addListener(debugTab);
					debugTab = null;
				}
			});
		}
	}

	private Menu prepareActionMenu() {
		Menu menu = new Menu();
		Menu statusesSubMenu = new Menu();
		ChangeStatusToolItem.prepareMenu(statusesSubMenu, new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				RosterPresence rp = ce.item.getData(ChangeStatusToolItem.STATUS_KEY);
				newStatusSet(rp);
			}
		});

		MenuItem statusMenuItem = new MenuItem("Status");
		statusMenuItem.setSubMenu(statusesSubMenu);
		menu.add(statusMenuItem);

		final MenuItem debugMenuItem = new MenuItem("Open debug tab", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				openDebugTab();
			}
		});
		menu.add(debugMenuItem);

		menu.addListener(Events.BeforeShow, new Listener<MenuEvent>() {

			public void handleEvent(MenuEvent be) {
				debugMenuItem.setEnabled(debugTab == null);
			}
		});

		return menu;
	}

	private Menu prepareContactMenu() {
		Menu menu = new Menu();

		final MenuItem addContactMenuItem = new MenuItem("Add contact", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				AddContactDialog acd = new AddContactDialog();
				acd.show();
			}
		});

		final Menu subscriptionMenu = new Menu();

		final MenuItem allowSubscriptionMenuItem = new MenuItem("Allow him/her to see my status",
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						allowSubscription();
					}
				});
		final MenuItem askSubscriptionMenuItem = new MenuItem("Ask to see his/her status", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				askSubscription();
			}
		});
		final MenuItem forbidSubscriptionMenuItem = new MenuItem("Forbid him/her to see my status",
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						forbidSubscription();
					}
				});

		subscriptionMenu.add(allowSubscriptionMenuItem);
		subscriptionMenu.add(askSubscriptionMenuItem);
		subscriptionMenu.add(forbidSubscriptionMenuItem);

		final MenuItem subscriptionMenuItem = new MenuItem("Subscription");
		subscriptionMenuItem.setSubMenu(subscriptionMenu);

		final MenuItem removeMenuItem = new MenuItem("Remove from roster", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				removeFromRoster();
			}
		});

		final MenuItem editContactMenuItem = new MenuItem("Edit contact", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				final JID jid = rosterComponent.getSelectedJID();
				if (jid != null) {
					EditContactDialog rcd = new EditContactDialog(jid);
					rcd.show();
				}
			}
		});

		final MenuItem vcardMenuItem = new MenuItem("Show VCard", new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				final JID jid = rosterComponent.getSelectedJID();
				if (jid != null) {
					(new VCardDialog(jid)).show();
				}
			}
		});

		menu.add(addContactMenuItem);
		menu.add(new SeparatorMenuItem());
		menu.add(subscriptionMenuItem);
		menu.add(removeMenuItem);
		menu.add(editContactMenuItem);
		menu.add(vcardMenuItem);

		menu.addListener(Events.BeforeShow, new Listener<MenuEvent>() {

			public void handleEvent(MenuEvent be) {
				boolean selected = rosterComponent.getSelectedJID() != null;
				subscriptionMenuItem.setEnabled(selected);
				removeMenuItem.setEnabled(selected);
				editContactMenuItem.setEnabled(selected);
				vcardMenuItem.setEnabled(selected);

				if (selected) {
					RosterItem ri = Messenger.session().getRosterPlugin().getRosterItem(rosterComponent.getSelectedJID());
					Subscription s = ri.getSubscription();

					allowSubscriptionMenuItem.setEnabled(s == Subscription.to);
					askSubscriptionMenuItem.setEnabled(s == Subscription.from || s == Subscription.none);
					forbidSubscriptionMenuItem.setEnabled(s == Subscription.both || s == Subscription.from);
				}

			}
		});

		return menu;
	}

	private ContentPanel prepareRosterPanel() {
		ContentPanel panel = new ContentPanel();
		panel.setLayout(new BorderLayout());

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0, 0, 0, 0));

		ContentPanel center = new ContentPanel();
		center.setHeaderVisible(false);
		ContentPanel south = new ContentPanel();
		south.setHeaderVisible(false);

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 26, 26, 26);
		southData.setSplit(false);
		southData.setCollapsible(false);
		southData.setFloatable(false);
		southData.setMargins(new Margins(0, 0, 0, 0));

		center.setScrollMode(Scroll.AUTO);
		center.add(this.rosterComponent);

		ToolBar toolBar = new ToolBar();
		toolBar.add(statusToolItem);

		south.add(toolBar);
		south.setWidth(26);

		panel.add(center, centerData);
		panel.add(south, southData);

		return panel;
	}

	private Menu prepareViewMenu() {
		Menu menu = new Menu();

		final CheckMenuItem showAllOffline = new CheckMenuItem("Show offline contacts");
		showAllOffline.addListener(Events.CheckChange, new Listener<MenuEvent>() {

			public void handleEvent(MenuEvent be) {
				rosterComponent.setGlobalShowOfflineContacts(showAllOffline.isChecked());
			}
		});
		menu.add(showAllOffline);

		final CheckMenuItem showGroupOffline = new CheckMenuItem("Show offline contacts in group");
		showGroupOffline.addListener(Events.CheckChange, new Listener<MenuEvent>() {

			public void handleEvent(MenuEvent be) {
				Group group = rosterComponent.getSelectedGroup();
				if (group != null) {
					group.setShowOffline(showGroupOffline.isChecked());
				}
			}
		});
		menu.add(showGroupOffline);

		menu.addListener(Events.BeforeShow, new Listener<MenuEvent>() {

			public void handleEvent(MenuEvent be) {
				showAllOffline.setChecked(rosterComponent.getGlobalShowOfflineContacts(), true);

				Group group = rosterComponent.getSelectedGroup();
				showGroupOffline.setEnabled(group != null && rosterComponent.getSelectedJID() == null);
				if (group != null) {
					showGroupOffline.setChecked(group.isShowOffline(), true);
				}

			}
		});

		return menu;
	}

	private void removeFromRoster() {
		final JID jid = rosterComponent.getSelectedJID();
		if (jid != null) {
			// XXX translation
			Messenger.session().getPresencePlugin().subscribe(jid);
			MessageBox box = new MessageBox();
			box.setModal(false);
			box.setTitle("Remove contact");
			box.setMessage("Are You sure you want to remove '" + jid.toStringBare() + "' from Your roster?");
			box.setButtons(MessageBox.YESNO);
			box.setIcon(MessageBox.QUESTION);
			box.addCallback(new Listener<com.extjs.gxt.ui.client.event.WindowEvent>() {

				public void handleEvent(com.extjs.gxt.ui.client.event.WindowEvent be) {
					System.out.println(be.buttonClicked.getItemId());
					if (Dialog.YES.equals(be.buttonClicked.getItemId())) {
						Messenger.session().getRosterPlugin().removeRosterItem(jid);
					}
				}
			});
			box.show();

		}
	}
}

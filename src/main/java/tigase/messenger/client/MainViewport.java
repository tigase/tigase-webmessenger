package tigase.messenger.client;

import tigase.gwtcommons.client.CustomPresenceStatusDialog;
import tigase.gwtcommons.client.LoginDialog;
import tigase.gwtcommons.client.StatusTab;
import tigase.gwtcommons.client.Translations;
import tigase.gwtcommons.client.XmlConsoleTab;
import tigase.gwtcommons.client.XmppService;
import tigase.gwtcommons.client.chat.ChatManagerModule;
import tigase.gwtcommons.client.muc.JoinRoomDialog;
import tigase.gwtcommons.client.muc.MucManagerModule;
import tigase.gwtcommons.client.roster.ContactEditDialog;
import tigase.gwtcommons.client.roster.RosterPanel;
import tigase.gwtcommons.client.roster.RosterPanel.SortMethod;
import tigase.gwtcommons.client.roster.SubscriptionRequestDialog;
import tigase.jaxmpp.core.client.Connector.State;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.JaxmppCore.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule.SaslEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class MainViewport extends Viewport {

	private final TabPanel center = new TabPanel();

	private final ChatManagerModule chatManager;

	private final ContentPanel cp = new ContentPanel(new BorderLayout());

	private final MucManagerModule mucManager;

	private String presenceStatus = null;

	private final RosterPanel rosterPanel = new RosterPanel();

	private Show selectedShow = null;

	private final Status status = new Status();

	private final StatusTab statusTab = new StatusTab();

	public MainViewport() {
		setLayout(new FitLayout());
		cp.setHeaderVisible(false);

		XmppService.get().getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.BeforeInitialPresence,
				new Listener<PresenceModule.PresenceEvent>() {

					public void handleEvent(PresenceEvent be) {
						onBeforeInitialPresence(be);
					}
				});

		chatManager = new ChatManagerModule(center);
		chatManager.init();

		mucManager = new MucManagerModule(center);
		mucManager.init();

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 270);
		westData.setSplit(true);
		westData.setCollapsible(true);
		westData.setMargins(new Margins(0, 5, 0, 0));

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0));

		rosterPanel.init();
		cp.add(rosterPanel, westData);
		cp.add(center, centerData);

		if (!XmppService.get().isConnected()) {
			LoginDialog l = new LoginDialog();
			l.show();
		}

		ToolBar tb = new ToolBar();

		Button statusButton = createStatusButton();
		tb.add(statusButton);

		Menu actionMenu = new Menu();
		actionMenu.add(new MenuItem(Translations.instance.clientMenuActionAddContact(), new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				ContactEditDialog dialog = new ContactEditDialog(null);
				dialog.show();
			}
		}));
		final MenuItem editContactMI = new MenuItem("Edit contact", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				RosterItem ri = rosterPanel.getSelectedData();
				ContactEditDialog dialog = new ContactEditDialog(ri);
				dialog.show();
			}
		});
		actionMenu.add(editContactMI);
		actionMenu.add(new MenuItem("Join room", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				JoinRoomDialog d = new JoinRoomDialog() {
					@Override
					protected void onSubmit(String roomName, String server, String nickname, String password) {
						try {
							mucManager.join(roomName, server, nickname, password);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				d.show();
			}
		}));

		actionMenu.addListener(Events.Show, new com.extjs.gxt.ui.client.event.Listener<MenuEvent>() {

			public void handleEvent(MenuEvent be) {
				RosterItem ri = rosterPanel.getSelectedData();
				editContactMI.setEnabled(ri != null);
			}
		});

		final CheckMenuItem sortUnsortedMenuItem = new CheckMenuItem("Unsorted");
		sortUnsortedMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				rosterPanel.setSortMethod(SortMethod.unsorted);
			}
		});
		final CheckMenuItem sortNameMenuItem = new CheckMenuItem("Sort by name");
		sortNameMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				rosterPanel.setSortMethod(SortMethod.name);
			}
		});
		final CheckMenuItem sortOnNameMenuItem = new CheckMenuItem("Sort by name (online first)");
		sortOnNameMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				rosterPanel.setSortMethod(SortMethod.onlineName);
			}
		});
		final CheckMenuItem sortJidMenuItem = new CheckMenuItem("Sort by jid");
		sortJidMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				rosterPanel.setSortMethod(SortMethod.jid);
			}
		});
		final CheckMenuItem sortOnJidMenuItem = new CheckMenuItem("Sort by jid (online first)");
		sortOnJidMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				rosterPanel.setSortMethod(SortMethod.onlineJid);
			}
		});

		final CheckMenuItem showOfflineMenuItem = new CheckMenuItem("Show offline");
		showOfflineMenuItem.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				rosterPanel.setShowOffline(!rosterPanel.isShowOffline());
			}
		});
		Menu viewMenu = new Menu();
		viewMenu.addListener(Events.BeforeShow, new com.extjs.gxt.ui.client.event.Listener<MenuEvent>() {

			public void handleEvent(MenuEvent be) {
				SortMethod sm = rosterPanel.getSortMethod();
				showOfflineMenuItem.setChecked(rosterPanel.isShowOffline(), true);
				sortUnsortedMenuItem.setChecked(sm == SortMethod.unsorted, true);
				sortNameMenuItem.setChecked(sm == SortMethod.name, true);
				sortOnNameMenuItem.setChecked(sm == SortMethod.onlineName, true);
				sortJidMenuItem.setChecked(sm == SortMethod.jid, true);
				sortOnJidMenuItem.setChecked(sm == SortMethod.onlineJid, true);
			}
		});
		viewMenu.add(showOfflineMenuItem);
		viewMenu.add(new SeparatorMenuItem());
		viewMenu.add(sortUnsortedMenuItem);
		viewMenu.add(sortNameMenuItem);
		viewMenu.add(sortOnNameMenuItem);
		viewMenu.add(sortJidMenuItem);
		viewMenu.add(sortOnJidMenuItem);

		Button viewButton = new Button("View");
		viewButton.setMenu(viewMenu);
		tb.add(viewButton);

		Button actionButton = new Button("Action");
		actionButton.setMenu(actionMenu);
		tb.add(actionButton);

		cp.setTopComponent(tb);

		tb = new ToolBar();
		tb.setBorders(false);
		tb.add(status);
		cp.setBottomComponent(tb);

		XmppService.get().addListener(JaxmppCore.Connected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				status.setText(Translations.instance.stateConnected());
			}
		});
		XmppService.get().addListener(JaxmppCore.Disconnected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				status.setText(Translations.instance.stateDisconnected());
			}
		});
		XmppService.get().getModulesManager().getModule(SaslModule.class).addListener(SaslModule.SaslStart,
				new Listener<SaslModule.SaslEvent>() {

					public void handleEvent(SaslEvent be) {
						status.setText(Translations.instance.stateAuthenticating());
					}
				});
		XmppService.get().getModulesManager().getModule(SaslModule.class).addListener(SaslModule.SaslSuccess,
				new Listener<SaslModule.SaslEvent>() {

					public void handleEvent(SaslEvent be) {
						status.setText(Translations.instance.stateAuthenticated());
					}
				});
		XmppService.get().getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.SubscribeRequest,
				new Listener<PresenceModule.PresenceEvent>() {

					public void handleEvent(PresenceEvent be) {
						SubscriptionRequestDialog dialog = new SubscriptionRequestDialog(be.getPresence());
						dialog.show();
					}
				});

		add(cp);
		if (XmppService.get().isConnected())
			status.setText(Translations.instance.stateConnected());
		else
			status.setText(Translations.instance.stateDisconnected());

		center.add(statusTab);
		statusTab.init();

		XmlConsoleTab xmlConsole = new XmlConsoleTab();
		center.add(xmlConsole);
		xmlConsole.init();

	}

	private Button createStatusButton() {

		MenuItem onlineMI = new MenuItem(Translations.instance.menuPresenceOnline(),
				IconHelper.create("presences/user-online.png"), new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.online);
					}
				});
		MenuItem customMI = new MenuItem("Custom status...", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				CustomPresenceStatusDialog dialog = new CustomPresenceStatusDialog(selectedShow, presenceStatus) {

					@Override
					public void onSubmit(Show show, String status) {
						setStatus(show, status);
					}
				};
				dialog.show();
			}
		});

		MenuItem chatMI = new MenuItem(Translations.instance.menuPresenceChat(), IconHelper.create("presences/user-chat.png"),
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.chat);

					}
				});
		MenuItem awayMI = new MenuItem(Translations.instance.menuPresenceAway(), IconHelper.create("presences/user-away.png"),
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.away);

					}
				});
		MenuItem xaMI = new MenuItem(Translations.instance.menuPresenceXA(), IconHelper.create("presences/user-xa.png"),
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.xa);
					}
				});
		MenuItem dndMI = new MenuItem(Translations.instance.menuPresenceDND(), IconHelper.create("presences/user-dnd.png"),
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.dnd);
					}
				});

		final MenuItem logoutMI = new MenuItem(Translations.instance.menuPresenceLogout(),
				IconHelper.create("presences/user-offline.png"), new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						try {
							XmppService.get().disconnect();
						} catch (JaxmppException e) {
							e.printStackTrace();
						}
					}
				});

		Menu statusMenu = new Menu();
		statusMenu.addListener(Events.BeforeShow, new com.extjs.gxt.ui.client.event.Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				logoutMI.setEnabled(!(XmppService.get().getConnector() == null || XmppService.get().getConnector().getState() == State.disconnected));
			}
		});
		statusMenu.add(onlineMI);
		statusMenu.add(customMI);
		statusMenu.add(new SeparatorMenuItem());
		statusMenu.add(chatMI);
		statusMenu.add(awayMI);
		statusMenu.add(xaMI);
		statusMenu.add(dndMI);
		statusMenu.add(new SeparatorMenuItem());
		statusMenu.add(logoutMI);
		Button statusButton = new Button("Status");
		statusButton.setMenu(statusMenu);
		return statusButton;
	}

	protected void onBeforeInitialPresence(PresenceEvent be) {
		be.setShow(this.selectedShow);
		be.setStatus(this.presenceStatus);
	}

	protected void setStatus(Show show) {
		setStatus(show, null);
	}

	protected void setStatus(final Show show, final String status) {
		this.selectedShow = show;
		this.presenceStatus = status;
		if (XmppService.get().getConnector() == null || XmppService.get().getConnector().getState() == State.disconnected) {
			LoginDialog l = new LoginDialog();
			l.show();
		} else {
			try {
				XmppService.get().getModulesManager().getModule(PresenceModule.class).setPresence(show, status, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

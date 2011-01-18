package tigase.messenger.client;

import tigase.gwtcommons.client.LoginDialog;
import tigase.gwtcommons.client.StatusTab;
import tigase.gwtcommons.client.XmlConsoleTab;
import tigase.gwtcommons.client.XmppService;
import tigase.gwtcommons.client.chat.ChatManagerModule;
import tigase.gwtcommons.client.muc.JoinRoomDialog;
import tigase.gwtcommons.client.muc.MucManagerModule;
import tigase.gwtcommons.client.roster.RosterPanel;
import tigase.gwtcommons.client.roster.RosterPanel.SortMethod;
import tigase.jaxmpp.core.client.Connector.State;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.JaxmppCore.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule.SaslEvent;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
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

	private final RosterPanel rosterPanel = new RosterPanel();

	private final Status status = new Status();

	private final StatusTab statusTab = new StatusTab();

	public MainViewport() {
		setLayout(new FitLayout());
		cp.setHeaderVisible(false);

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

		LoginDialog l = new LoginDialog();
		l.show();

		ToolBar tb = new ToolBar();

		Menu statusMenu = new Menu();

		statusMenu.add(new MenuItem("Online", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				if (XmppService.get().getConnector() == null
						|| XmppService.get().getConnector().getState() == State.disconnected) {
					LoginDialog l = new LoginDialog();
					l.show();
				}
			}
		}));
		statusMenu.add(new MenuItem("Logout", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				try {
					XmppService.get().disconnect();
				} catch (JaxmppException e) {
					e.printStackTrace();
				}
			}
		}));
		Button statusButton = new Button("Status");
		statusButton.setMenu(statusMenu);
		tb.add(statusButton);

		Menu actionMenu = new Menu();
		actionMenu.add(new MenuItem("Join room", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				JoinRoomDialog d = new JoinRoomDialog() {
					@Override
					protected void onSubmit(String roomName, String server, String nickname) {
						try {
							mucManager.join(roomName, server, nickname);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				d.show();
			}
		}));

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
				status.setText("Connected");
			}
		});
		XmppService.get().addListener(JaxmppCore.Disconnected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				status.setText("Disconnected");
			}
		});
		XmppService.get().getModulesManager().getModule(SaslModule.class).addListener(SaslModule.SaslStart,
				new Listener<SaslModule.SaslEvent>() {

					public void handleEvent(SaslEvent be) {
						status.setText("Authenticating...");
					}
				});
		XmppService.get().getModulesManager().getModule(SaslModule.class).addListener(SaslModule.SaslSuccess,
				new Listener<SaslModule.SaslEvent>() {

					public void handleEvent(SaslEvent be) {
						status.setText("Authenticated");
					}
				});

		add(cp);
		status.setText("Disconnected");

		center.add(statusTab);
		statusTab.init();

		XmlConsoleTab xmlConsole = new XmlConsoleTab();
		center.add(xmlConsole);
		xmlConsole.init();

	}

}

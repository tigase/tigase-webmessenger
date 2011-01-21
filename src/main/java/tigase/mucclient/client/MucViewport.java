package tigase.mucclient.client;

import tigase.gwtcommons.client.CustomPresenceStatusDialog;
import tigase.gwtcommons.client.LoginDialog;
import tigase.gwtcommons.client.XmppService;
import tigase.gwtcommons.client.muc.MucPanel;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.State;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.JaxmppCore.JaxmppEvent;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector.BoshConnectorEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.MucEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule.SaslEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class MucViewport extends Viewport {

	private final ContentPanel cp = new ContentPanel(new BorderLayout());

	private MessageBox errorMessageBox;

	private final ToolBar menuBar = new ToolBar();

	private final MucPanel mucPanel = new MucPanel();

	private String presenceStatus = null;

	private Show selectedShow = null;

	private final Status status = new Status();

	private final ToolBar statusBar = new ToolBar();

	public MucViewport() {
		setLayout(new FitLayout());
		cp.setHeaderVisible(false);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(5, 5, 5, 5));

		cp.add(mucPanel, centerData);
		cp.setTopComponent(menuBar);

		add(cp);

		XmppService.get().getModulesManager().getModule(MucModule.class).addListener(new Listener<MucModule.MucEvent>() {

			public void handleEvent(MucEvent be) {
				try {
					onMucEvent(be);
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		});

		XmppService.get().getModulesManager().getModule(ResourceBinderModule.class).addListener(
				ResourceBinderModule.ResourceBindSuccess, new Listener<ResourceBinderModule.ResourceBindEvent>() {

					public void handleEvent(ResourceBindEvent be) {
						String nickname = XmppService.get().getProperties().getUserProperty(SessionObject.NICKNAME);

						try {
							String r = XmppService.config().get("mucRoomJid");
							JID roomJID = JID.jidInstance(r);
							Room room = XmppService.get().getModulesManager().getModule(MucModule.class).join(
									roomJID.getLocalpart(), roomJID.getDomain(), nickname);
							mucPanel.setRoom(room);
						} catch (XMLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JaxmppException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

		XmppService.get().getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.BeforeInitialPresence,
				new Listener<PresenceEvent>() {

					public void handleEvent(PresenceEvent be) {
						be.setPriority(-1);
					}
				});

		Button statusButton = createStatusButton();
		menuBar.add(statusButton);

		status.setText("Disconnected");
		statusBar.setBorders(false);
		statusBar.add(status);
		cp.setBottomComponent(statusBar);

		XmppService.get().addListener(JaxmppCore.Connected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				status.setText("Connected");
			}
		});
		XmppService.get().addListener(JaxmppCore.Disconnected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				status.setText("Disconnected");
				mucPanel.setPanelEnabled(false);
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
		XmppService.get().getModulesManager().getModule(SaslModule.class).addListener(SaslModule.SaslFailed,
				new Listener<SaslModule.SaslEvent>() {

					public void handleEvent(SaslEvent be) {
						showErrorAndLogin("Bad credentials");
					}
				});
		XmppService.get().getConnector().addListener(Connector.Error, new Listener<BoshConnectorEvent>() {

			public void handleEvent(BoshConnectorEvent be) {
				try {
					String m = "Connector error";

					if (be.getErrorElement() != null && be.getErrorElement().getText() != null) {
						m = be.getErrorElement().getText();
					} else if (be.getCaught() != null && be.getCaught().getMessage() != null) {
						m = be.getCaught().getMessage();
					}

					showErrorAndLogin(m);
				} catch (Exception e) {
					showErrorAndLogin(e.getMessage());
				}
			}
		});

		showLogin();
	}

	private Button createStatusButton() {

		MenuItem onlineMI = new MenuItem("Online", IconHelper.create("presences/user-online.png"),
				new SelectionListener<MenuEvent>() {

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

		MenuItem chatMI = new MenuItem("Free for chat", IconHelper.create("presences/user-chat.png"),
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.chat);

					}
				});
		MenuItem awayMI = new MenuItem("Away", IconHelper.create("presences/user-away.png"),
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.away);

					}
				});
		MenuItem xaMI = new MenuItem("eXtended Away", IconHelper.create("presences/user-xa.png"),
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.xa);
					}
				});
		MenuItem dndMI = new MenuItem("Do Not Disturb", IconHelper.create("presences/user-dnd.png"),
				new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.dnd);
					}
				});

		final MenuItem logoutMI = new MenuItem("Logout", IconHelper.create("presences/user-offline.png"),
				new SelectionListener<MenuEvent>() {

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

	protected void onMucEvent(MucEvent be) throws XMLException {
		if (be.getRoom() != mucPanel.getRoom())
			return;
		if (be.getType() == MucModule.MessageReceived)
			mucPanel.process(be.getMessage());
		else {
			mucPanel.process(be);
		}
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

	protected void showErrorAndLogin(String message) {
		if (errorMessageBox != null)
			return;
		errorMessageBox = MessageBox.alert("Error", message, new com.extjs.gxt.ui.client.event.Listener<MessageBoxEvent>() {

			public void handleEvent(MessageBoxEvent be) {
				errorMessageBox = null;
				showLogin();
			}
		});
	}

	protected void showLogin() {
		LoginDialog loginDialog = new LoginDialog();
		loginDialog.show();
	}

}

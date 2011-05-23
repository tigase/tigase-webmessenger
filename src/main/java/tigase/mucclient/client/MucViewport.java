package tigase.mucclient.client;

import tigase.gwtcommons.client.CustomPresenceStatusDialog;
import tigase.gwtcommons.client.LoginDialog;
import tigase.gwtcommons.client.LoginDialog.LoginType;
import tigase.gwtcommons.client.Translations;
import tigase.gwtcommons.client.XmppService;
import tigase.gwtcommons.client.chat.ChatPanel;
import tigase.gwtcommons.client.muc.MucManagerModule;
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
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule.AuthEvent;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.MucEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.ErrorElement;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.gwt.client.Jaxmpp;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
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
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;

public class MucViewport extends Viewport {

	private static boolean getBooleanValue(Dictionary config, String name, boolean defaultValue) {
		try {
			String x = config.get(name);
			return Boolean.parseBoolean(x);
		} catch (Exception e) {
			return defaultValue;
		}

	}

	private static int getIntValue(Dictionary config, String name, int defaultValue) {
		try {
			String x = config.get(name);
			return Integer.parseInt(x);
		} catch (Exception e) {
			return defaultValue;
		}

	}

	private final ContentPanel cp = new ContentPanel(new BorderLayout());

	private MessageBox errorMessageBox;

	private final ToolBar menuBar = new ToolBar();

	private final MucPanel mucPanel;

	private String presenceStatus = null;

	private final ChatPanel privateChatPanel;

	private Show selectedShow = null;

	private final Status status = new Status();

	private final ToolBar statusBar = new ToolBar();

	public MucViewport(ChatRestoreModule chatRestorer) {
		setLayout(new FitLayout());
		cp.setHeaderVisible(false);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(5, 5, 5, 5));

		mucPanel = new MucPanel();
		chatRestorer.setMucPanel(mucPanel);

		Dictionary config = Dictionary.getDictionary("Config");
		boolean prv;
		try {
			String x = config.get("privateMessageNickname");
			prv = x != null;
		} catch (Exception e) {
			prv = false;
		}

		final MucModule mucModule = XmppService.get().getModulesManager().getModule(MucModule.class);

		final JID roomJID = JID.jidInstance(XmppService.config().get("mucRoomJid"));

		if (prv) {
			JID j = JID.jidInstance(roomJID.getBareJid(), config.get("privateMessageNickname"));
			try {
				final Chat c = XmppService.get().createChat(j);
				privateChatPanel = new ChatPanel(c);
				XmppService.get().getModulesManager().getModule(MucModule.class).addListener(MessageModule.MessageReceived,
						new Listener<MessageModule.MessageEvent>() {

							public void handleEvent(MessageEvent be) throws JaxmppException {
								System.out.println("RECV: " + be.getMessage().getFrom());
								if (be.getChat() == c) {
									privateChatPanel.add(be.getMessage());
								}
							}
						});
			} catch (JaxmppException e) {
				GWT.log("Problems", e);
				throw new RuntimeException(e);
			}

		} else {
			privateChatPanel = null;
		}

		boolean showOccupantsList;

		try {
			showOccupantsList = Boolean.parseBoolean(config.get("showOccupantsList"));
		} catch (Exception e) {
			showOccupantsList = true;
		}

		mucPanel.setOccupantsListVisible(showOccupantsList);
		mucPanel.setTextAreaSize(getIntValue(config, "textAreaSize", 122) + 28);

		if (privateChatPanel != null)
			cp.add(privateChatPanel, centerData);
		else
			cp.add(mucPanel, centerData);

		add(cp);

		mucModule.addListener(new Listener<MucModule.MucEvent>() {

			public void handleEvent(MucEvent be) {
				try {
					onMucEvent(be);
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		});

		XmppService.get().addListener(Jaxmpp.BeforeSessionResoting, new Listener<JaxmppCore.JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				String nickname = XmppService.get().getProperties().getUserProperty(SessionObject.NICKNAME);

				Room room = new Room(XmppService.get().getWriter(), roomJID.getBareJid(), nickname);
				mucModule.register(room);
				mucPanel.setRoom(room);
				try {
					mucModule.enable(room);
				} catch (JaxmppException e) {
					MessageBox.alert("Error", e.getMessage(), null);
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
							Room room = mucModule.join(roomJID.getLocalpart(), roomJID.getDomain(), nickname);
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

		mucModule.addListener(MucModule.YouJoined, new Listener<MucEvent>() {

			public void handleEvent(MucEvent be) {
				mucPanel.getMessagePanel().addAppMessage(Translations.instance.mucWelcome(be.getRoom().getRoomJid().toString()));
				String t = null;
				for (String nick : be.getRoom().getPresences().keySet()) {
					if (t == null)
						t = nick;
					else
						t += ", " + nick;
				}
				if (t != null)
					mucPanel.getMessagePanel().addAppMessage(Translations.instance.mucAlreadyHere(t));
			}
		});

		XmppService.get().getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.BeforeInitialPresence,
				new Listener<PresenceEvent>() {

					public void handleEvent(PresenceEvent be) {
						be.setPriority(-1);
					}
				});

		if (getBooleanValue(config, "showTopMenu", true)) {
			Button statusButton = createStatusButton();
			menuBar.add(statusButton);
		}

		if (XmppService.get().isConnected())
			status.setText(Translations.instance.stateConnected());
		else
			status.setText(Translations.instance.stateDisconnected());

		statusBar.add(new Button(Translations.instance.menuPresenceLogout(), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				try {
					XmppService.get().disconnect();
				} catch (JaxmppException e) {
					e.printStackTrace();
				}
			}
		}));

		statusBar.setBorders(false);
		statusBar.add(status);
		cp.setBottomComponent(statusBar);

		XmppService.get().addListener(JaxmppCore.Connected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				status.setText(Translations.instance.stateConnected());
			}
		});
		XmppService.get().addListener(JaxmppCore.Disconnected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				status.setText(Translations.instance.stateDisconnected());
				mucPanel.setPanelEnabled(false);
				showLogin();
			}
		});
		XmppService.get().getModulesManager().getModule(AuthModule.class).addListener(AuthModule.AuthStart,
				new Listener<AuthEvent>() {

					public void handleEvent(AuthEvent be) {
						status.setText(Translations.instance.stateAuthenticating());
					}
				});
		XmppService.get().getModulesManager().getModule(AuthModule.class).addListener(AuthModule.AuthSuccess,
				new Listener<AuthEvent>() {

					public void handleEvent(AuthEvent be) {
						status.setText(Translations.instance.stateAuthenticated());
					}
				});
		XmppService.get().getModulesManager().getModule(AuthModule.class).addListener(AuthModule.AuthFailed,
				new Listener<AuthEvent>() {

					public void handleEvent(AuthEvent be) {
						showErrorAndLogin(Translations.instance.errorBadCredentials());
					}
				});
		XmppService.get().getConnector().addListener(Connector.Error, new Listener<BoshConnectorEvent>() {

			public void handleEvent(BoshConnectorEvent be) {
				try {
					String m = Translations.instance.errorConnectionError();

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

		if (menuBar.getItemCount() != 0)
			cp.setTopComponent(menuBar);
		if (XmppService.get().getSessionObject().getProperty(Connector.CONNECTOR_STAGE_KEY) != Connector.State.connected) {
			showLogin();
		}

	}

	private Button createStatusButton() {

		MenuItem onlineMI = new MenuItem(Translations.instance.menuPresenceOnline(),
				IconHelper.create("presences/user-online.png"), new SelectionListener<MenuEvent>() {

					@Override
					public void componentSelected(MenuEvent ce) {
						setStatus(Show.online);
					}
				});
		MenuItem customMI = new MenuItem(Translations.instance.menuPresenceCustom(), new SelectionListener<MenuEvent>() {

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
		Button statusButton = new Button(Translations.instance.menuPresence());
		statusButton.setMenu(statusMenu);
		return statusButton;
	}

	private LoginDialog.LoginType getLoginType() {
		try {
			String x = XmppService.config().get("loginType");
			if (x == null)
				return LoginType.both;
			return LoginType.valueOf(x);
		} catch (Exception e) {
			return LoginType.both;
		}
	}

	protected void onBeforeInitialPresence(PresenceEvent be) {
		be.setShow(this.selectedShow);
		be.setStatus(this.presenceStatus);
	}

	protected void onMucEvent(MucEvent be) throws XMLException {
		if (be.getRoom() != mucPanel.getRoom())
			return;
		if (be.getType() == MucModule.RoomClosed) {
			try {
				XmppService.get().disconnect();
			} catch (JaxmppException e) {
			}

			ErrorElement ee = ErrorElement.extract(be.getPresence());
			String msg = MucManagerModule.getErrorMessage(ee);
			mucPanel.getMessagePanel().addErrorMessage(msg);
			MessageBox.alert(Translations.instance.error(), msg, new com.extjs.gxt.ui.client.event.Listener<MessageBoxEvent>() {

				public void handleEvent(MessageBoxEvent be) {
					showLogin();
				}
			});
		} else if (be.getType() == MucModule.MucMessageReceived)
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
			LoginDialog l = new LoginDialog(getLoginType());
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
		errorMessageBox = MessageBox.alert(Translations.instance.error(), message,
				new com.extjs.gxt.ui.client.event.Listener<MessageBoxEvent>() {

					public void handleEvent(MessageBoxEvent be) {
						errorMessageBox = null;
						showLogin();
					}
				});
	}

	protected void showLogin() {
		LoginDialog loginDialog = new LoginDialog(getLoginType());
		loginDialog.show();
	}

}

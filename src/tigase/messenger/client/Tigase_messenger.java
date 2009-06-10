package tigase.messenger.client;

import java.util.List;

import tigase.gwt.components.roster.client.ContactComparator;
import tigase.gwt.components.roster.client.Item;
import tigase.gwt.components.roster.client.PresenceCallback;
import tigase.gwt.components.roster.client.Roster;
import tigase.gwt.components.roster.client.RosterPresence;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.ConnectorListener;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.Session;
import tigase.jaxmpp.core.client.StreamEventDetails;
import tigase.jaxmpp.core.client.User;
import tigase.jaxmpp.core.client.events.Events;
import tigase.jaxmpp.core.client.events.EventsManager;
import tigase.jaxmpp.core.client.events.Listener;
import tigase.jaxmpp.core.client.packets.Packet;
import tigase.jaxmpp.core.client.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.ErrorCondition;
import tigase.jaxmpp.core.client.xmpp.ResourceBindEvenet;
import tigase.jaxmpp.core.client.xmpp.message.ChatManager;
import tigase.jaxmpp.core.client.xmpp.presence.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.roster.RosterEvent;
import tigase.jaxmpp.core.client.xmpp.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.sasl.AnonymousMechanism;
import tigase.jaxmpp.core.client.xmpp.sasl.PlainMechanism;
import tigase.jaxmpp.core.client.xmpp.sasl.SaslEvent;
import tigase.jaxmpp.xmpp4gwt.client.GWTSession;
import tigase.messenger.client.login.LoginDialog;
import tigase.messenger.client.login.LoginDialogListener;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.util.Theme;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Tigase_messenger implements ConnectorListener, EntryPoint, LoginDialogListener {

	private static Tigase_messenger instance;

	private final static float MAX_ELEMENTS = 7f;

	private boolean focused = true;

	public static void onFocus() {
		instance.focused = true;
		System.out.println("focus");
	}

	public static void onBlur() {
		instance.focused = false;
		System.out.println("blur");
	}

	private native void registerBlurFocus() /*-{
		$wnd.onblur = new function() {
			@tigase.messenger.client.Tigase_messenger::onBlur()();
		}

		$wnd.onfocus = function() {
			@tigase.messenger.client.Tigase_messenger::onFocus()();
		   };
	}-*/;

	public static Config config() {
		return instance.config;
	}

	public static EventsManager eventsManager() {
		return instance.session.getEventsManager();
	}

	public static Tigase_messenger instance() {
		return instance;
	}

	public static Session session() {
		return instance.session;
	}

	private final ChatManager<ChatTab> chatManager;

	private final Config config = new Config();

	private Show defaultPresenceShow = Show.notSpecified;

	private String nickname = null;

	public final PresenceCallback presenceCallback;

	private final Roster rosterComponent;

	public final Session session;

	private TabbedViewport tabbedViewport;

	private VersionInfo versionInfo = GWT.create(VersionInfo.class);

	private MessageBox waitDialog;

	public Tigase_messenger() {
		// GXT.setDefaultTheme(new Slate(), true);
		GXT.setDefaultTheme(Theme.BLUE, true);
		instance = this;

		User user = new User();
		this.session = new GWTSession(user);
		this.session.getSoftwareVersionPlugin().setName("Tigase Messenger");
		this.session.getSoftwareVersionPlugin().setVersion(versionInfo.versionNumber());

		String http = config.getHTTPBase();
		this.session.getConnector().setHttpBase(http == null ? "/bosh" : http);

		this.presenceCallback = new PresenceCallbackImpl(session.getPresencePlugin(), session.getRosterPlugin());
		this.rosterComponent = new Roster(this.presenceCallback);
		this.rosterComponent.setContactComparator(new ContactComparator() {
			public int compare(Item o1, Item o2) {

				return x(o1).compareToIgnoreCase(x(o2));
			}

			private String x(Item i) {
				return i.getName();
			}
		});

		this.session.getConnector().addListener(this);

		this.chatManager = new ChatManager<ChatTab>(this.session.getChatPlugin());
		this.session.getChatPlugin().setChatManager(chatManager);

		session.addEventListener(Events.saslFail, new Listener<SaslEvent>() {

			public void handleEvent(SaslEvent event) {
				hideWaitDialog();
				tabbedViewport.getStatusToolItem().setNewStatus(RosterPresence.OFFLINE);
				MessageBox.alert("Login failed", event.getCause(), null).show();
			}
		});
		session.addEventListener(Events.saslSuccess, new Listener<SaslEvent>() {

			public void handleEvent(SaslEvent event) {
				updateWaitDialog("Authenticated.");

			}
		});
		session.addEventListener(Events.saslStartAuth, new Listener<SaslEvent>() {

			public void handleEvent(SaslEvent event) {
				updateWaitDialog("Authentication...");

			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.beforeSendInitialPresence,
				new Listener<tigase.jaxmpp.core.client.events.Event>() {
					public void handleEvent(final tigase.jaxmpp.core.client.events.Event event) {
						Timer t = new Timer() {

							@Override
							public void run() {
								JID[] directPresences = config.getDirectPresenceAdressees();
								if (directPresences != null) {
									for (JID jid : directPresences) {
										session.getPresencePlugin().sendDirectPresence(jid, defaultPresenceShow, nickname);
									}
								}

								updateWaitDialog("Resource binded.");

								RosterPresence rp;
								switch (defaultPresenceShow) {
								case away:
									rp = RosterPresence.AWAY;
									break;
								case chat:
									rp = RosterPresence.READY_FOR_CHAT;
									break;
								case dnd:
									rp = RosterPresence.DND;
									break;
								case notSpecified:
									rp = RosterPresence.ONLINE;
									break;
								default:
									rp = RosterPresence.ONLINE;
									break;
								}

								
								rosterComponent.setOwnJid(session.getBindedJID());

								tabbedViewport.getStatusToolItem().setNewStatus(rp);
								Timer x = new Timer() {

									@Override
									public void run() {
										hideWaitDialog();
									}
								};
								x.schedule(500);
							}
						};
						t.schedule(750);
					}
				});
		Tigase_messenger.session().getEventsManager().addListener(Events.subscribe, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				(new SubscriptionRequestDialog(event.getPresence().getFrom().getBareJID())).show();
			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.beforeSendInitialPresence,
				new Listener<PresenceEvent>() {
					public void handleEvent(PresenceEvent event) {
						event.getPresence().setShow(defaultPresenceShow);
						if (nickname != null)
							event.getPresence().setExtNick(nickname);
						System.out.println("!!!!! " + defaultPresenceShow);
						updateWaitDialog("Sending presence...");
					}
				});

		Tigase_messenger.session().getEventsManager().addListener(Events.presenceChange, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				rosterComponent.updatePresence(event.getPresence());
			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.presenceChange, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				rosterComponent.updatePresence(event.getPresence());
			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.contactAvailable, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				String name = event.getPresence().getFrom().toStringBare();
				RosterItem ri = session.getRosterPlugin().getRosterItem(event.getPresence().getFrom());

				if (ri != null && ri.getName() != null && ri.getName().length() > 0) {
					name = ri.getName();
				}

				Info.display("Contact connected", "{0} is now available.", name);
			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.contactUnavailable, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				String name = event.getPresence().getFrom().toStringBare();
				RosterItem ri = session.getRosterPlugin().getRosterItem(event.getPresence().getFrom());

				if (ri != null && ri.getName() != null && ri.getName().length() > 0) {
					name = ri.getName();
				}

				Info.display("Contact disconnected", "{0} is now unavailable.", name);
			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.rosterItemRemoved, new Listener<RosterEvent>() {
			public void handleEvent(RosterEvent event) {
				rosterComponent.removedFromRoster(event.getRosterItem());

			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.subscribed, new Listener<RosterEvent>() {
			public void handleEvent(RosterEvent event) {
				JID jid = JID.fromString(event.getRosterItem().getJid());
				MessageBox box = new MessageBox();
				box.setModal(false);
				box.setTitle("Authorization granted");
				box.setMessage("The contact '" + jid.toStringBare() + "' has authorized you to see his or her status");
				box.setButtons(MessageBox.OK);
				box.setIcon(MessageBox.INFO);
				box.show();
			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.rosterItemUpdated, new Listener<RosterEvent>() {
			public void handleEvent(RosterEvent event) {
				rosterComponent.updatedRosterItem(event.getRosterItem());
			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.rosterItemAdded, new Listener<RosterEvent>() {
			public void handleEvent(RosterEvent event) {
				rosterComponent.updatedRosterItem(event.getRosterItem());
			}
		});
		Tigase_messenger.session().getEventsManager().addListener(Events.unsubscribed, new Listener<RosterEvent>() {
			public void handleEvent(RosterEvent event) {
				JID jid = JID.fromString(event.getRosterItem().getJid());
				MessageBox box = new MessageBox();
				box.setModal(false);
				box.setTitle("Authorization has been removed");
				box.setMessage("The contact '" + jid.toStringBare()
						+ "' withdrew their permission for you to see their status.");
				box.setButtons(MessageBox.OK);
				box.setIcon(MessageBox.INFO);
				box.show();
			}
		});
	}

	public String getNickname() {
		return nickname;
	}

	private void hideWaitDialog() {
		if (this.waitDialog != null) {
			waitDialog.hide();
			waitDialog = null;
		}
	}

	public void onBodyReceive(StreamEventDetails details, String body) {
	}

	public void onBodySend(String body) {
	}

	public void onConnect(Connector con) {
		updateWaitDialog("Connecting...");
	}

	public void onConnectionError(final Connector con, final ErrorCondition errorCondition, final StreamEventDetails details,
			final String message) {

		rosterComponent.reset();
		if (errorCondition == ErrorCondition.item_not_found) {
			hideWaitDialog();
			tabbedViewport.getStatusToolItem().setNewStatus(RosterPresence.OFFLINE);
			DeferredCommand.addCommand(new Command() {

				public void execute() {
					MessageBox.alert("Connection failed", "Item not found error", null).show();
				}
			});
		} else {
			hideWaitDialog();
			tabbedViewport.getStatusToolItem().setNewStatus(RosterPresence.OFFLINE);
			DeferredCommand.addCommand(new Command() {

				public void execute() {
					MessageBox.alert("Connection failed", message, null).show();
				}
			});
		}
	}

	public void onLogin(LoginDialog loginDialog) {
		session.reset();
		nickname = null;
		if (loginDialog.isAnonymous()) {
			nickname = loginDialog.getNickname();
			System.out.println("!!! " + nickname);

			session.getUser().setDomainname(config.getDefaultHostname());
			session.getAuthPlugin().setMechanism(new AnonymousMechanism());
		} else {
			session.getAuthPlugin().setMechanism(new PlainMechanism(session.getUser()));
			final JID jid = JID.fromString(loginDialog.getJID());

			session.getUser().setPassword(loginDialog.getPassword());
			session.getUser().setDomainname(jid.getDomain());
			session.getUser().setUsername(jid.getNode());
			String resource = config.getDefaultResource();
			if (jid.getResource() != null) {
				resource = jid.getResource();
			}
			session.getUser().setResource(resource);
		}
		session.login();
		if (!config.isDebugEnabled()) {
			waitDialog = MessageBox.progress("Please wait", "Loading items...", "Initializing...");
		}
		this.rosterComponent.reset();
	}

	public void onModuleLoad() {
		registerBlurFocus();
		tabbedViewport = new TabbedViewport(this.rosterComponent, this.chatManager);
		RootPanel.get().add(tabbedViewport);
		openLoginDialog();
	}

	public void onStanzaReceived(List<? extends Packet> nodes) {
	}

	public void openLoginDialog() {
		openLoginDialog(Show.notSpecified);
	}

	public void openLoginDialog(Show defaultPresenceShow) {
		this.defaultPresenceShow = defaultPresenceShow == null ? Show.notSpecified : defaultPresenceShow;
		LoginDialog d = new LoginDialog();
		d.addListener(this);
		d.show();
	}

	private void updateWaitDialog(final String message) {
		if (this.waitDialog != null) {
			final ProgressBar bar = this.waitDialog.getProgressBar();
			double x = bar.getValue() + 1.0 / MAX_ELEMENTS;
			System.out.println(x);
			System.out.println("=== " + message + "   (" + x + ")");
			bar.updateProgress(x, message);
		}
	}

	public boolean isFocused() {
		return focused;
	}

}

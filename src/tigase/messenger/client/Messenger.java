package tigase.messenger.client;

import java.util.List;

import tigase.gwt.components.roster.client.PresenceCallback;
import tigase.gwt.components.roster.client.Roster;
import tigase.gwt.components.roster.client.RosterPresence;
import tigase.messenger.client.login.LoginDialog;
import tigase.messenger.client.login.LoginDialogListener;
import tigase.xmpp4gwt.client.Connector;
import tigase.xmpp4gwt.client.ConnectorListener;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.Session;
import tigase.xmpp4gwt.client.User;
import tigase.xmpp4gwt.client.Connector.BoshErrorCondition;
import tigase.xmpp4gwt.client.events.Listener;
import tigase.xmpp4gwt.client.packet.Packet;
import tigase.xmpp4gwt.client.stanzas.Presence.Show;
import tigase.xmpp4gwt.client.xmpp.ErrorCondition;
import tigase.xmpp4gwt.client.xmpp.ImSessionListener;
import tigase.xmpp4gwt.client.xmpp.ResourceBindListener;
import tigase.xmpp4gwt.client.xmpp.message.ChatManager;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceEvent;
import tigase.xmpp4gwt.client.xmpp.presence.PresencePlugin;
import tigase.xmpp4gwt.client.xmpp.roster.RosterEvent;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;
import tigase.xmpp4gwt.client.xmpp.roster.RosterPlugin;
import tigase.xmpp4gwt.client.xmpp.sasl.SaslAuthPluginListener;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Messenger implements ImSessionListener, ConnectorListener, EntryPoint, LoginDialogListener, SaslAuthPluginListener,
		ResourceBindListener {

	private static Messenger instance;

	private final static float MAX_ELEMENTS = 7f;

	public static Config config() {
		return instance.config;
	}

	public static Messenger instance() {
		return instance;
	}

	public static Session session() {
		return instance.session;
	}

	private final ChatManager<ChatTab> chatManager;

	private final Config config = new Config();

	private Show defaultPresenceShow = Show.notSpecified;

	public final PresenceCallback presenceCallback;

	private final Roster rosterComponent;

	public final Session session;

	private TabbedViewport tabbedViewport;

	private MessageBox waitDialog;

	public Messenger() {
		instance = this;

		User user = new User();
		this.session = new Session(user);

		String http = config.getHTTPBase();
		this.session.getConnector().setHttpBase(http == null ? "/bosh" : http);

		this.presenceCallback = new PresenceCallbackImpl(session.getPresencePlugin(), session.getRosterPlugin());
		this.rosterComponent = new Roster(this.presenceCallback);

		this.session.getAuthPlugin().addSaslAuthListener(this);
		this.session.getConnector().addListener(this);
		this.session.getBindPlugin().addResourceBindListener(this);
		this.session.getImSessionEstablishPlugin().addListener(this);

		this.chatManager = new ChatManager<ChatTab>(this.session.getChatPlugin());

		Messenger.session().getEventsManager().addListener(PresencePlugin.Events.subscribe, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				(new SubscriptionRequestDialog(event.getPresence().getFrom().getBareJID())).show();
			}
		});
		Messenger.session().getEventsManager().addListener(PresencePlugin.Events.beforeSendInitialPresence,
				new Listener<PresenceEvent>() {
					public void handleEvent(PresenceEvent event) {
						event.getPresence().setShow(defaultPresenceShow);
						System.out.println("!!!!! " + defaultPresenceShow);
						updateWaitDialog("Sending presence...");
					}
				});

		Messenger.session().getEventsManager().addListener(PresencePlugin.Events.presenceChange, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				rosterComponent.updatePresence(event.getPresence());
			}
		});
		Messenger.session().getEventsManager().addListener(PresencePlugin.Events.presenceChange, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				rosterComponent.updatePresence(event.getPresence());
			}
		});
		Messenger.session().getEventsManager().addListener(PresencePlugin.Events.contactAvailable, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				String name = event.getPresence().getFrom().toStringBare();
				RosterItem ri = session.getRosterPlugin().getRosterItem(event.getPresence().getFrom());

				if (ri != null && ri.getName() != null && ri.getName().length() > 0) {
					name = ri.getName();
				}

				Info.display("Contact connected", "{0} is now available.", name);
			}
		});
		Messenger.session().getEventsManager().addListener(PresencePlugin.Events.contactUnavailable, new Listener<PresenceEvent>() {
			public void handleEvent(PresenceEvent event) {
				String name = event.getPresence().getFrom().toStringBare();
				RosterItem ri = session.getRosterPlugin().getRosterItem(event.getPresence().getFrom());

				if (ri != null && ri.getName() != null && ri.getName().length() > 0) {
					name = ri.getName();
				}

				Info.display("Contact disconnected", "{0} is now unavailable.", name);
			}
		});
		Messenger.session().getEventsManager().addListener(RosterPlugin.Events.rosterItemRemoved, new Listener<RosterEvent>() {
			public void handleEvent(RosterEvent event) {
				rosterComponent.removedFromRoster(event.getRosterItem());

			}
		});
		Messenger.session().getEventsManager().addListener(RosterPlugin.Events.subscribed, new Listener<RosterEvent>() {
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
		Messenger.session().getEventsManager().addListener(RosterPlugin.Events.rosterItemUpdated, new Listener<RosterEvent>() {
			public void handleEvent(RosterEvent event) {
				rosterComponent.updatedRosterItem(event.getRosterItem());
			}
		});
		Messenger.session().getEventsManager().addListener(RosterPlugin.Events.rosterItemAdded, new Listener<RosterEvent>() {
			public void handleEvent(RosterEvent event) {
				rosterComponent.updatedRosterItem(event.getRosterItem());
			}
		});
		Messenger.session().getEventsManager().addListener(RosterPlugin.Events.unsubscribed, new Listener<RosterEvent>() {
			public void handleEvent(RosterEvent event) {
				JID jid = JID.fromString(event.getRosterItem().getJid());
				MessageBox box = new MessageBox();
				box.setModal(false);
				box.setTitle("Authorization has been removed");
				box.setMessage("The contact '" + jid.toStringBare() + "' withdrew their permission for you to see their status.");
				box.setButtons(MessageBox.OK);
				box.setIcon(MessageBox.INFO);
				box.show();
			}
		});
	}

	private void hideWaitDialog() {
		if (this.waitDialog != null) {
			waitDialog.hide();
			waitDialog = null;
		}
	}

	public void onBindResource(JID newJid) {
		updateWaitDialog("Resource binded.");

		RosterPresence rp;
		switch (this.defaultPresenceShow) {
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

		this.tabbedViewport.getStatusToolItem().setNewStatus(rp);
	}

	public void onBodyReceive(Response code, String body) {
	}

	public void onBodySend(String body) {
	}

	public void onBoshError(ErrorCondition errorCondition, BoshErrorCondition boshErrorCondition, final String message) {
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

	public void onBoshTerminate(Connector con, BoshErrorCondition boshErrorCondition) {
		rosterComponent.reset();
		hideWaitDialog();
		tabbedViewport.getStatusToolItem().setNewStatus(RosterPresence.OFFLINE);
	}

	public void onConnect(Connector con) {
		updateWaitDialog("Connecting...");
	}

	public void onFail(String message) {
		hideWaitDialog();
		tabbedViewport.getStatusToolItem().setNewStatus(RosterPresence.OFFLINE);
		MessageBox.alert("Login failed", message, null).show();
	}

	public void onLogin(LoginDialog loginDialog) {
		final JID jid = JID.fromString(loginDialog.getJID());

		session.reset();
		session.getUser().setPassword(loginDialog.getPassword());
		session.getUser().setDomainname(jid.getDomain());
		session.getUser().setUsername(jid.getNode());
		String resource = config.getDefaultResource();
		if (jid.getResource() != null) {
			resource = jid.getResource();
		}
		session.getUser().setResource(resource);

		session.login();
		if (!config.isDebugEnabled()) {
			waitDialog = MessageBox.progress("Please wait", "Loading items...", "Initializing...");
		}
		this.rosterComponent.reset();
	}

	public void onModuleLoad() {
		tabbedViewport = new TabbedViewport(this.rosterComponent, this.chatManager);
		RootPanel.get().add(tabbedViewport);
		openLoginDialog();
	}

	public void onSessionEstablished() {
		updateWaitDialog("Session established.");
		Timer x = new Timer() {

			@Override
			public void run() {
				hideWaitDialog();
			}
		};
		x.schedule(500);
	}

	public void onSessionEstablishingError() {
	}

	public void onStanzaReceived(List<? extends Packet> nodes) {
	}

	public void onStartAuth() {
		updateWaitDialog("Authentication...");
	}

	public void onStartSessionEstablishing() {
		updateWaitDialog("Session establishing...");
	}

	public void onSuccess() {
		updateWaitDialog("Authenticated.");
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
}

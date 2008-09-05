package tigase.messenger.client;

import java.util.List;

import tigase.messenger.client.login.LoginDialog;
import tigase.messenger.client.login.LoginDialogListener;
import tigase.messenger.client.roster.component.PresenceCallback;
import tigase.messenger.client.roster.component.Roster;
import tigase.xmpp4gwt.client.Connector;
import tigase.xmpp4gwt.client.ConnectorListener;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.Session;
import tigase.xmpp4gwt.client.User;
import tigase.xmpp4gwt.client.packet.Packet;
import tigase.xmpp4gwt.client.stanzas.Presence;
import tigase.xmpp4gwt.client.xmpp.ImSessionListener;
import tigase.xmpp4gwt.client.xmpp.ResourceBindListener;
import tigase.xmpp4gwt.client.xmpp.message.ChatManager;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceListener;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;
import tigase.xmpp4gwt.client.xmpp.roster.RosterListener;
import tigase.xmpp4gwt.client.xmpp.sasl.SaslAuthPluginListener;

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
public class Messenger implements RosterListener, ImSessionListener, PresenceListener, ConnectorListener, EntryPoint,
		LoginDialogListener, SaslAuthPluginListener, ResourceBindListener {

	private final VersionInfo versionInfo;

	private final Config config = new Config();

	private final Roster rosterComponent;

	public final PresenceCallback presenceCallback;

	private final ChatManager<ChatTab> chatManager;

	public final Session session;

	private static Messenger instance;

	public Messenger() {
		instance = this;
		this.versionInfo = GWT.create(VersionInfo.class);

		User user = new User();
		this.session = new Session(user);

		this.presenceCallback = new PresenceCallbackImpl(session.getPresencePlugin(), session.getRosterPlugin());
		this.rosterComponent = new Roster(this.presenceCallback);

		this.session.getAuthPlugin().addSaslAuthListener(this);
		this.session.getConnector().addListener(this);
		this.session.getBindPlugin().addResourceBindListener(this);
		this.session.getImSessionEstablishPlugin().addListener(this);
		this.session.getPresencePlugin().addPresenceListener(this);
		this.session.getRosterPlugin().addRosterListener(this);

		this.chatManager = new ChatManager<ChatTab>(this.session.getChatPlugin());
	}

	public void onModuleLoad() {

		final TabbedViewport tvp = new TabbedViewport(this.rosterComponent, this.chatManager);
		RootPanel.get().add(tvp);

		LoginDialog d = new LoginDialog();
		d.addListener(this);
		d.show();

	}

	public void onLogin(LoginDialog loginDialog) {
		final JID jid = JID.fromString(loginDialog.getJID());

		session.reset();
		session.getUser().setPassword(loginDialog.getPassword());
		session.getUser().setDomainname(jid.getDomain());
		session.getUser().setUsername(jid.getNode());
		String resource = "messenger";
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

	private MessageBox waitDialog;

	private void hideWaitDialog() {
		if (this.waitDialog != null) {
			waitDialog.hide();
			waitDialog = null;
		}
	}

	public void onFail(String message) {
		hideWaitDialog();
		MessageBox.alert("Login failed", message, null).show();
	}

	private final static float MAX_ELEMENTS = 7f;

	public void onStartAuth() {
		updateWaitDialog("Authentication...");
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

	public void onSuccess() {
		updateWaitDialog("Authenticated.");
	}

	public void onBindResource(JID newJid) {
		updateWaitDialog("Resource binded.");
	}

	public void onBodyReceive(Response code, String body) {
	}

	public void onBodySend(String body) {
	}

	public void onConnect(Connector con) {
		updateWaitDialog("Connecting...");
	}

	public void onDisconnectByServer(Connector con) {
		hideWaitDialog();
	}

	public void onError(final String message) {
		hideWaitDialog();

		DeferredCommand.addCommand(new Command() {

			public void execute() {
				MessageBox.alert("Connection failed", message, null).show();
			}
		});
	}

	public void onItemNotFoundError() {
		hideWaitDialog();
		DeferredCommand.addCommand(new Command() {

			public void execute() {
				MessageBox.alert("Connection failed", "Item not found error", null).show();
			}
		});
	}

	public void onStanzaReceived(List<? extends Packet> nodes) {
	}

	public void beforeSendInitialPresence() {
		updateWaitDialog("Sending presence...");
	}

	public void onContactAvailable(Presence presenceItem) {
	}

	public void onContactUnavailable(Presence presenceItem) {
	}

	public void onPresenceChange(Presence presenceItem) {
		rosterComponent.updatePresence(presenceItem);
	}

	public void onSessionEstablished() {
		updateWaitDialog("Session established.");
		Timer x = new Timer() {

			public void run() {
				hideWaitDialog();
			}
		};
		x.schedule(500);
	}

	public void onSessionEstablishingError() {
	}

	public void onStartSessionEstablishing() {
		updateWaitDialog("Session establishing...");
	}

	public void beforeAddItem(JID jid, String name, List<String> groupsNames) {

	}

	public void onAddItem(RosterItem item) {
		rosterComponent.updatedRosterItem(item);
	}

	public void onEndRosterUpdating() {

	}

	public void onRemoveItem(RosterItem item) {
		rosterComponent.removedFromRoster(item);
	}

	public void onStartRosterUpdating() {

	}

	public void onUpdateItem(RosterItem item) {
		rosterComponent.updatedRosterItem(item);
	}

	public static Session session() {
		return instance.session;
	}

	public static Config config() {
		return instance.config;
	}
}

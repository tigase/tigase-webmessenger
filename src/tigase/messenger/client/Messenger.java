package tigase.messenger.client;

import java.util.List;

import tigase.messenger.client.login.LoginDialog;
import tigase.messenger.client.login.LoginDialogListener;
import tigase.messenger.client.roster.component.Group;
import tigase.messenger.client.roster.component.Item;
import tigase.messenger.client.roster.component.PresenceCallback;
import tigase.messenger.client.roster.component.Roster;
import tigase.messenger.client.tabbed.TabbedViewport;
import tigase.xmpp4gwt.client.BoshConnection;
import tigase.xmpp4gwt.client.BoshConnectionListener;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.Session;
import tigase.xmpp4gwt.client.User;
import tigase.xmpp4gwt.client.xmpp.ResourceBindListener;
import tigase.xmpp4gwt.client.xmpp.message.Message;
import tigase.xmpp4gwt.client.xmpp.message.MessageListener;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceItem;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceListener;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceType;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;
import tigase.xmpp4gwt.client.xmpp.roster.RosterListener;
import tigase.xmpp4gwt.client.xmpp.sasl.SaslAuthPluginListener;

import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.WindowManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.xml.client.Node;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Messenger implements EntryPoint, LoginDialogListener, SaslAuthPluginListener, BoshConnectionListener,
		ResourceBindListener, RosterListener, MessageListener, PresenceListener {

	private static Messenger instance;

	protected static int PROGRESS_ELEMENTS = 4;

	public static Session session() {
		return instance.session;
	}

	protected ChatManager chatManager;

	private LoginDialog loginDialog = null;

	protected PresenceCallback presenceCallback;

	private MessageBox progressBox = null;

	protected final Roster rosterComponent;

	private final Session session;

	protected final User user;

	public Messenger() {
		instance = this;

		ThemeManager.register(Slate.SLATE);
		GXT.setDefaultTheme(Slate.SLATE, true);

		this.user = new User();
		this.session = new Session(user);

		this.session.getAuthPlugin().addSaslAuthListener(this);
		this.session.getConnector().addBoshListener(this);
		this.session.getBindPlugin().addResourceBindListener(this);
		this.session.getRosterPlugin().addRosterListener(this);
		this.session.getChatPlugin().addMessageListener(this);
		this.session.getPresencePlugin().addPresenceListener(this);

		this.presenceCallback = new PresenceCallbackImpl(this.session.getPresencePlugin(),
				this.session.getRosterPlugin());

		this.rosterComponent = new Roster(presenceCallback);

		rosterComponent.addListener(new tigase.messenger.client.roster.component.RosterListener() {

			public void afterRosterChange() {}

			public void onContactContextMenu(Event event, Item item) {
			// TODO Auto-generated method stub

			}

			public void onContactDoubleClick(Item item) {
				chatManager.openChatWith(item.getJID().getBareJID(), true);
			}

			public void onGroupContextMenu(Event event, Group group) {
			// TODO Auto-generated method stub

			}

			public void onRosterItemSelect(JID jid) {
			// TODO Auto-generated method stub

			}
		});
	}

	public void beforeAddItem(JID jid, String name, List<String> groupsNames) {}

	public void beforeSendInitialPresence() {}

	protected void closeLoginDialog() {
		if (loginDialog != null) {
			loginDialog.removeLoginDialogListener(this);
			loginDialog.hide();
			loginDialog = null;
		}
	}

	public void onAddItem(RosterItem item) {
		rosterComponent.updatedRosterItem(item);
	}

	public void onBindResource(JID newJid) {
		updateProgress("Resource binded...");
		if (progressBox != null) {
			progressBox.hide();
			progressBox = null;
		}
	}

	public void onBodyReceive(Response code, String body) {}

	public void onBodySend(String body) {}

	public void onConnect(BoshConnection con) {
		updateProgress("Connected...");

	}

	public void onContactAvailable(PresenceItem presenceItem) {}

	public void onContactUnavailable(PresenceItem presenceItem) {}

	public void onDisconnectByServer(BoshConnection con) {
		rosterComponent.reset();
		MessageBox.alert("Error", "POłączenie zerwane", null);
		showLoginDialog();
	}

	public void onEndRosterUpdating() {}

	public void onError(String message) {
		showLoginDialog();
	}

	public void onFail(String message) {
		showLoginDialog();

	}

	public void onItemNotFoundError() {
		System.out.println("fuck");

		final Dialog d = new Dialog();
		// d.setModal(true);

		d.show();

		DeferredCommand.addCommand(new Command() {

			public void execute() {
				WindowManager.get().bringToFront(d);
			}
		});

		showLoginDialog();
	}

	public void onMessageReceived(Message message) {
		chatManager.process(message);
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		rosterComponent.addAlwaysVisibleGroups("General");

		final TabbedViewport tvp = new TabbedViewport(rosterComponent);
		tvp.setPresenceCallback(this.presenceCallback);
		this.chatManager = tvp;
		RootPanel.get().add(tvp);
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				tvp.layout();
			}
		});

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				showLoginDialog();
			}
		});
	}

	public void onPresenceChange(PresenceItem presenceItem) {
		System.out.println("pch");
		if (presenceItem.getType() == PresenceType.UNSUBSCRIBED) {
			InfoConfig config = new InfoConfig("Unsubscribed", "Zostałeś unsubskrybowany");
			Info.display(config);
		} else {
			if (presenceItem.getType() == PresenceType.SUBSCRIBE) {
				SubscriptionRequestDialog di = new SubscriptionRequestDialog(presenceItem);
				di.show();
			}
		}
		rosterComponent.updatePresence(presenceItem);
		chatManager.updatePresence(presenceItem);
	}

	public void onPressCancel() {}

	public void onPressLogin() {
		JID jid = this.loginDialog.getJID();
		if (jid != null) {
			user.setPassword(this.loginDialog.getPassword());
			user.setUsername(jid.getNode());

			closeLoginDialog();

			String domain = "";
			String resource = "messenger";
			if (jid.getDomain() != null) domain = jid.getDomain();
			if (jid.getResource() != null) resource = jid.getResource();
			user.setDomainname(domain);
			user.setResource(resource);

			this.progressBox = MessageBox.progress("Logging in", "Please wait...", "Conncting...");

			Timer timer = new Timer() {

				@Override
				public void run() {

				}
			};
			timer.schedule(2250);

			this.session.login();
		}
	}

	public void onRemoveItem(RosterItem item) {
		rosterComponent.removedFromRoster(item);
	}

	public void onStanzaReceived(Node[] nodes) {}

	public void onStartAuth() {
		updateProgress("Authenticating...");
	}

	public void onStartRosterUpdating() {}

	public void onSuccess() {
		updateProgress("Authenticated");

	}

	public void onUpdateItem(RosterItem item) {
		rosterComponent.updatedRosterItem(item);
	}

	protected void showLoginDialog() {
		if (loginDialog == null) {
			session.reset();
			loginDialog = new LoginDialog();
			loginDialog.addLoginDialogListener(this);
			loginDialog.show();
			loginDialog.toFront();
		}
	}

	protected void updateProgress(String message) {
		System.out.println(">" + message);
		if (progressBox != null) {
			ProgressBar bar = progressBox.getProgressBar();
			double x = bar.getValue() * PROGRESS_ELEMENTS + 1;
			bar.updateProgress(x / PROGRESS_ELEMENTS, message);
		}
	}

}

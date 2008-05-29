package tigase.messenger.client;

import tigase.messenger.client.login.LoginDialog;
import tigase.messenger.client.login.LoginDialogListener;
import tigase.messenger.client.roster.component.Group;
import tigase.messenger.client.roster.component.Item;
import tigase.messenger.client.roster.component.Roster;
import tigase.messenger.client.tabbed.TabbedViewport;
import tigase.xmpp4gwt.client.BoshConnection;
import tigase.xmpp4gwt.client.BoshConnectionListener;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.Session;
import tigase.xmpp4gwt.client.User;
import tigase.xmpp4gwt.client.xmpp.ResourceBindListener;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;
import tigase.xmpp4gwt.client.xmpp.roster.RosterListener;
import tigase.xmpp4gwt.client.xmpp.sasl.SaslAuthPluginListener;

import com.extjs.gxt.ui.client.widget.Dialog;
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
		ResourceBindListener, RosterListener {

	protected final User user;

	protected final Roster rosterComponent;

	private static Messenger instance;

	public static Session session() {
		return instance.session;
	}

	protected ChatManager chatManager;

	public Messenger() {
		instance = this;
		this.user = new User();
		this.session = new Session(user);

		this.session.getAuthPlugin().addSaslAuthListener(this);
		this.session.getConnector().addBoshListener(this);
		this.session.getBindPlugin().addResourceBindListener(this);
		this.session.getRosterPlugin().addRosterListener(this);

		this.rosterComponent = new Roster(new PresenceCallbackImpl(this.session.getPresencePlugin(),
				this.session.getRosterPlugin()));

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

	private final Session session;

	private LoginDialog loginDialog = null;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		rosterComponent.addAlwaysVisibleGroups("General");

		final TabbedViewport tvp = new TabbedViewport(rosterComponent);
		this.chatManager = tvp;
		RootPanel.get("app").add(tvp);
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				tvp.layout(true);
			}
		});
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				showLoginDialog();
			}
		});
	}

	public void onPressCancel() {}

	private MessageBox progressBox = null;

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

	protected static int PROGRESS_ELEMENTS = 4;

	protected void updateProgress(String message) {
		System.out.println(">" + message);
		if (progressBox != null) {
			ProgressBar bar = progressBox.getProgressBar();
			double x = bar.getValue() * PROGRESS_ELEMENTS + 1;
			bar.updateProgress(x / PROGRESS_ELEMENTS, message);
		}
	}

	public void onFail(String message) {
		showLoginDialog();

	}

	public void onStartAuth() {
		updateProgress("Authenticating...");
	}

	public void onSuccess() {
		updateProgress("Authenticated");

	}

	public void onBodyReceive(Response code, String body) {}

	public void onBodySend(String body) {}

	public void onConnect(BoshConnection con) {
		updateProgress("Connected...");

	}

	public void onDisconnectByServer(BoshConnection con) {
		rosterComponent.reset();
		MessageBox.alert("Error", "POłączenie zerwane", null);
		showLoginDialog();
	}

	public void onError(String message) {
		showLoginDialog();
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

	protected void closeLoginDialog() {
		if (loginDialog != null) {
			loginDialog.removeLoginDialogListener(this);
			loginDialog.close();
			loginDialog = null;
		}
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

	public void onStanzaReceived(Node[] nodes) {}

	public void onBindResource(JID newJid) {
		updateProgress("Resource binded...");
		if (progressBox != null) {
			progressBox.hide();
			progressBox = null;
		}
	}

	public void onAddItem(RosterItem item) {
		rosterComponent.updatedRosterItem(item);
	}

	public void onEndRosterUpdating() {}

	public void onRemoveItem(RosterItem item) {
		rosterComponent.removedFromRoster(item);
	}

	public void onStartRosterUpdating() {}

	public void onUpdateItem(RosterItem item) {
		rosterComponent.updatedRosterItem(item);
	}

}

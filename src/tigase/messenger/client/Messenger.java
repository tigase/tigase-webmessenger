package tigase.messenger.client;

import tigase.messenger.client.roster.component.PresenceCallback;
import tigase.messenger.client.roster.component.Roster;
import tigase.xmpp4gwt.client.Session;
import tigase.xmpp4gwt.client.User;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Messenger implements EntryPoint {

	private final VersionInfo versionInfo;

	private final Roster rosterComponent;

	public final PresenceCallback presenceCallback;

	public final Session session;

	public Messenger() {
		this.versionInfo = GWT.create(VersionInfo.class);

		User user = new User();
		this.session = new Session(user);

		this.presenceCallback = new PresenceCallbackImpl(session.getPresencePlugin(), session.getRosterPlugin());
		this.rosterComponent = new Roster(this.presenceCallback);

	}

	public void onModuleLoad() {

		final TabbedViewport tvp = new TabbedViewport(this.rosterComponent);
		RootPanel.get().add(tvp);

	}

}

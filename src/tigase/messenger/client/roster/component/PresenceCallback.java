package tigase.messenger.client.roster.component;

import tigase.xmpp4gwt.client.JID;

public interface PresenceCallback {

	RosterPresence getRosterPresence(JID jid);

}

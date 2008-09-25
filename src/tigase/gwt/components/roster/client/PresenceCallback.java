package tigase.gwt.components.roster.client;

import tigase.xmpp4gwt.client.JID;

public interface PresenceCallback {

	RosterPresence getRosterPresence(JID jid);

}

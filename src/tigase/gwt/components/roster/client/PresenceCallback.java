package tigase.gwt.components.roster.client;

import tigase.jaxmpp.core.client.JID;

public interface PresenceCallback {

	RosterPresence getRosterPresence(JID jid);

}

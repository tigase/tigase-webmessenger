package tigase.messenger.client;

import tigase.gwt.components.roster.client.PresenceCallback;
import tigase.gwt.components.roster.client.RosterPresence;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.stanzas.Presence;
import tigase.jaxmpp.core.client.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.presence.PresencePlugin;
import tigase.jaxmpp.core.client.xmpp.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.roster.RosterPlugin;

public class PresenceCallbackImpl implements PresenceCallback {

	private final PresencePlugin presencePlugin;

	private final RosterPlugin rosterPlugin;

	public PresenceCallbackImpl(PresencePlugin presencePlugin, RosterPlugin rosterPlugin) {
		this.rosterPlugin = rosterPlugin;
		this.presencePlugin = presencePlugin;
	}

	public RosterPresence getRosterPresence(JID jid) {
		Presence pi = presencePlugin.getPresenceitemByBareJid(jid.toStringBare());
		RosterItem ri = rosterPlugin.getRosterItem(jid);

		if (ri != null && ri.isAsk()) {
			return RosterPresence.ASK;
		} else if (ri != null && ri.getSubscription() != null
				&& (RosterItem.Subscription.none == ri.getSubscription() || RosterItem.Subscription.from == ri.getSubscription())) {
			return RosterPresence.NOAUTH;
		} else if (pi != null) {
			System.out.println(jid + " :: " + pi.getType() + " | " + pi.getShow());
			if (pi.getType() == Presence.Type.error) {
				return RosterPresence.ERROR;
			} else if (pi.getType() == Presence.Type.unavailable || pi.getType() == Presence.Type.subscribe) {
				return RosterPresence.OFFLINE;
			} else if (pi.getType() == Presence.Type.unsubscribed || pi.getType() == Presence.Type.unsubscribe) {
				return RosterPresence.NOAUTH;
			} else if (pi.getShow() == Show.notSpecified) {
				return RosterPresence.ONLINE;
			} else if (pi.getShow() == Show.away) {
				return RosterPresence.AWAY;
			} else if (pi.getShow() == Show.chat) {
				return RosterPresence.READY_FOR_CHAT;
			} else if (pi.getShow() == Show.dnd) {
				return RosterPresence.DND;
			} else if (pi.getShow() == Show.xa) {
				return RosterPresence.XA;
			}
		}

		return RosterPresence.OFFLINE;
	}
}

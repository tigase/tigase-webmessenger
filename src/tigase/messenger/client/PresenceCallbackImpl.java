package tigase.messenger.client;

import tigase.messenger.client.roster.component.PresenceCallback;
import tigase.messenger.client.roster.component.RosterPresence;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.stanzas.Presence;
import tigase.xmpp4gwt.client.xmpp.presence.PresencePlugin;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;
import tigase.xmpp4gwt.client.xmpp.roster.RosterPlugin;

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
		} else if (pi != null) {
			System.out.println(jid + " :: " + pi.getType() + " | " + pi.getShow());
			if (pi.getType() == Presence.Type.error) {
				return RosterPresence.ERROR;
			} else if (pi.getType() == Presence.Type.unavailable || pi.getType() == Presence.Type.subscribe
					|| pi.getType() == Presence.Type.unsubscribed || pi.getType() == Presence.Type.unsubscribe) {
				return RosterPresence.OFFLINE;
			} else if (pi.getShow() == Presence.Show.notSpecified) {
				return RosterPresence.ONLINE;
			} else if (pi.getShow() == Presence.Show.away) {
				return RosterPresence.AWAY;
			} else if (pi.getShow() == Presence.Show.chat) {
				return RosterPresence.READY_FOR_CHAT;
			} else if (pi.getShow() == Presence.Show.dnd) {
				return RosterPresence.DND;
			} else if (pi.getShow() == Presence.Show.xa) {
				return RosterPresence.XA;
			}
		}

		return RosterPresence.OFFLINE;
	}
}

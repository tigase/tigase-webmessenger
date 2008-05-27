package tigase.messenger.client;

import tigase.messenger.client.roster.component.PresenceCallback;
import tigase.messenger.client.roster.component.RosterPresence;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceItem;
import tigase.xmpp4gwt.client.xmpp.presence.PresencePlugin;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceType;
import tigase.xmpp4gwt.client.xmpp.presence.Show;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;
import tigase.xmpp4gwt.client.xmpp.roster.RosterPlugin;

public class PresenceCallbackImpl implements PresenceCallback {

	private final RosterPlugin rosterPlugin;

	private final PresencePlugin presencePlugin;

	public PresenceCallbackImpl(PresencePlugin presencePlugin, RosterPlugin rosterPlugin) {
		this.rosterPlugin = rosterPlugin;
		this.presencePlugin = presencePlugin;
	}

	public RosterPresence getRosterPresence(JID jid) {
		PresenceItem pi = presencePlugin.getPresenceitemByBareJid(jid.toStringBare());
		RosterItem ri = rosterPlugin.getRosterItem(jid);

		if (ri != null && ri.isAsk()) {
			return RosterPresence.ASK;
		} else if (pi != null) {
			System.out.println(jid + " :: " + pi.getType() + " | " + pi.getShow());
			if (pi.getType() == PresenceType.ERROR) {
				return RosterPresence.ERROR;
			} else if (pi.getType() == PresenceType.UNAVAILABLE || pi.getType() == PresenceType.SUBSCRIBE
					|| pi.getType() == PresenceType.UNSUBSCRIBED || pi.getType() == PresenceType.UNSUBSCRIBE) {
				return RosterPresence.OFFLINE;
			} else if (pi.getShow() == null) {
				return RosterPresence.ONLINE;
			} else if (pi.getShow() == Show.AWAY) {
				return RosterPresence.AWAY;
			} else if (pi.getShow() == Show.CHAT) {
				return RosterPresence.READY_FOR_CHAT;
			} else if (pi.getShow() == Show.DND) {
				return RosterPresence.DND;
			} else if (pi.getShow() == Show.XA) {
				return RosterPresence.XA;
			}
		}

		return RosterPresence.OFFLINE;
	}
}

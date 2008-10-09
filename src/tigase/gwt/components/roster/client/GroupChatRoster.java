package tigase.gwt.components.roster.client;

import java.util.Arrays;

import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.stanzas.Presence;
import tigase.xmpp4gwt.client.stanzas.Presence.Type;

public class GroupChatRoster extends Roster {

	public static interface GroupNamesCallback {
		String[] getGroupsOf(JID jid);
	}

	private final GroupNamesCallback groupNamesCallback;

	public GroupChatRoster(PresenceCallback presenceCallback, GroupNamesCallback groupNamesCallback) {
		super(presenceCallback);
		this.groupNamesCallback = groupNamesCallback;
	}

	public void updatePresence(Presence presenceItem) {
		if (presenceItem == null)
			return;
		JID jid = presenceItem.getFrom();
		String[] groups = this.groupNamesCallback.getGroupsOf(jid);
		RosterPresence p = presenceCallback.getRosterPresence(jid);

		if (p == null)
			return;

		if (presenceItem.getType() == Type.unavailable || p == RosterPresence.OFFLINE) {
			removedFromRoster(jid);
		} else {
			String displayedName = presenceItem.getFrom().getResource();

			update(presenceItem.getFrom(), p, displayedName, Arrays.asList(groups), true);
		}
		fireAfterRosterChange();
	}
}

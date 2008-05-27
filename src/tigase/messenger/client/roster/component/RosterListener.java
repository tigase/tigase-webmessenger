package tigase.messenger.client.roster.component;

import tigase.xmpp4gwt.client.JID;

import com.google.gwt.user.client.Event;

public interface RosterListener {

	void afterRosterChange();

	void onContactContextMenu(final Event event, final Item item);

	void onContactDoubleClick(final Item item);

	void onGroupContextMenu(final Event event, final Group group);

	void onRosterItemSelect(final JID jid);

}

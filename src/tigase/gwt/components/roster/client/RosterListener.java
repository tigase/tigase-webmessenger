package tigase.gwt.components.roster.client;

import tigase.jaxmpp.core.client.JID;

import com.google.gwt.user.client.Event;

public interface RosterListener {

	void afterRosterChange();

	void onContactContextMenu(final Event event, final Item item);

	void onContactDoubleClick(final Item item);

	void onGroupContextMenu(final Event event, final Group group);

	void onGroupToolTip(final Event event, final Group group);

	void onItemToolTip(final Event event, final Item group);

	void onRosterItemSelect(final JID jid);

}

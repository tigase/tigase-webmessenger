package tigase.messenger.client.muc;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.messenger.client.roster.BasicRosterPanel;

public class OccupantsListPanel extends BasicRosterPanel<JID> {

	private final Room room;

	public OccupantsListPanel(Room room) {
		this.room = room;
	}

	@Override
	protected String getItemName(RosterItem<JID> model) throws XMLException {
		return ((Presence) model.getData()).getFrom().getResource();
	}

	@Override
	protected tigase.messenger.client.roster.BasicRosterPanel.RosterShow getShowOf(RosterItem<JID> model) throws XMLException {
		Presence p = model.getData();
		if (p == null || p.getType() == StanzaType.unavailable)
			return RosterShow.offline;
		else if (p.getShow() == Show.online)
			return RosterShow.online;
		else if (p.getShow() == Show.away)
			return RosterShow.away;
		else if (p.getShow() == Show.chat)
			return RosterShow.chat;
		else if (p.getShow() == Show.dnd)
			return RosterShow.dnd;
		else if (p.getShow() == Show.xa)
			return RosterShow.xa;
		else
			return RosterShow.error;
	}

	@Override
	protected void onDoubleClick(RosterItem<JID> item) {
		// TODO Auto-generated method stub

	}

}

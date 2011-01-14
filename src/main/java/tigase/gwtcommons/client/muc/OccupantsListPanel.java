package tigase.gwtcommons.client.muc;

import tigase.gwtcommons.client.roster.BasicRosterPanel;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Occupant;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class OccupantsListPanel extends BasicRosterPanel<Occupant> {

	private final Room room;

	public OccupantsListPanel(Room room) {
		this.room = room;
	}

	@Override
	protected String getItemName(RosterItem<Occupant> model) throws XMLException {
		return ((Presence) model.getData()).getFrom().getResource();
	}

	@Override
	protected tigase.gwtcommons.client.roster.BasicRosterPanel.RosterShow getShowOf(RosterItem<Occupant> model)
			throws XMLException {
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
	protected void onDoubleClick(RosterItem<Occupant> item) {
		// TODO Auto-generated method stub

	}

}

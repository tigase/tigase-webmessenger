package tigase.messenger.client.roster;

import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.JaxmppCore.JaxmppEvent;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem.Subscription;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule.RosterEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.messenger.client.XmppService;

public class RosterPanel extends BasicRosterPanel<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> {

	public static RosterShow getShowOfRosterItem(tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem item)
			throws XMLException {
		if (item.isAsk())
			return RosterShow.requested;
		if (item.getSubscription() == Subscription.none || item.getSubscription() == Subscription.to)
			return RosterShow.offline_nonauth;
		Map<String, Presence> x = XmppService.get().getModulesManager().getModule(PresenceModule.class).getPresence().getPresences(
				item.getJid());
		RosterShow r = RosterShow.offline;
		if (x != null)
			for (Presence p : x.values()) {
				RosterShow tmp = RosterShow.notinroster;
				if (p.getType() == StanzaType.unavailable)
					tmp = RosterShow.offline;
				else if (p.getShow() == Show.online)
					tmp = RosterShow.online;
				else if (p.getShow() == Show.away)
					tmp = RosterShow.away;
				else if (p.getShow() == Show.chat)
					tmp = RosterShow.chat;
				else if (p.getShow() == Show.dnd)
					tmp = RosterShow.dnd;
				else if (p.getShow() == Show.xa)
					tmp = RosterShow.xa;

				r = r.getWeight() > tmp.getWeight() ? r : tmp;
			}
		return r;
	}

	private final Listener<PresenceEvent> presenceListener;

	private final PresenceModule presenceModule;

	private final Listener<RosterEvent> rosterListener;

	private RosterModule rosterModule;

	public RosterPanel() {

		this.rosterModule = XmppService.get().getModulesManager().getModule(RosterModule.class);
		this.presenceModule = XmppService.get().getModulesManager().getModule(PresenceModule.class);

		this.presenceListener = new Listener<PresenceModule.PresenceEvent>() {

			public void handleEvent(PresenceEvent be) {
				onPresenceEvent(be);
			}
		};
		this.rosterListener = new Listener<RosterModule.RosterEvent>() {

			public void handleEvent(RosterEvent be) {
				onRosterEvent(be);
			}
		};
	}

	@Override
	protected String getItemName(
			tigase.messenger.client.roster.BasicRosterPanel.RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> model)
			throws XMLException {
		Presence presence = presenceModule.getPresence().getBestPresence(model.getId().getJid());
		String x = null;
		if (presence != null)
			x = presence.getNickname();
		x = model.getId().getName();
		if (x == null || x.isEmpty())
			x = model.getId().getJid().toString();

		return x;
	}

	@Override
	protected tigase.messenger.client.roster.BasicRosterPanel.RosterShow getShowOf(
			tigase.messenger.client.roster.BasicRosterPanel.RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> model)
			throws XMLException {
		return getShowOfRosterItem(model.getId());
	}

	public void init() {

		rosterModule.addListener(RosterModule.ItemAdded, this.rosterListener);
		rosterModule.addListener(RosterModule.ItemRemoved, this.rosterListener);
		rosterModule.addListener(RosterModule.ItemUpdated, this.rosterListener);

		presenceModule.addListener(PresenceModule.ContactAvailable, this.presenceListener);
		presenceModule.addListener(PresenceModule.ContactChangedPresence, this.presenceListener);
		presenceModule.addListener(PresenceModule.ContactUnavailable, this.presenceListener);

		XmppService.get().addListener(JaxmppCore.Disconnected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				onDisconnect();
			}
		});

	}

	protected void onDisconnect() {
		for (RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> m : getStore().getModels()) {
			update(m.getId());
		}
	}

	@Override
	protected void onDoubleClick(
			tigase.messenger.client.roster.BasicRosterPanel.RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> item) {
		XmppService.get().createChat(JID.jidInstance(item.getId().getJid()));
	}

	protected void onPresenceEvent(PresenceEvent be) {
		BareJID bareJid = be.getJid().getBareJid();

		for (RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> m : getStore().getModels()) {
			if (m.getId().getJid().equals(bareJid)) {
				update(m.getId());
			}
		}
	}

	protected void onRosterEvent(RosterEvent be) {
		if (be.getType() == RosterModule.ItemAdded) {
			add(be.getItem());
		} else if (be.getType() == RosterModule.ItemRemoved) {
			remove(be.getItem());
		} else if (be.getType() == RosterModule.ItemAdded) {
			update(be.getItem());
		}
	}

}

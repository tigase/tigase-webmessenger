package tigase.gwtcommons.client.roster;

import tigase.gwtcommons.client.Translations;
import tigase.gwtcommons.client.XmppService;
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

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class RosterPanel extends BasicRosterPanel<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> {

	public static enum SortMethod {
		jid,
		name,
		onlineJid,
		onlineName,
		unsorted
	}

	private final static String getRosterShowWeight(RosterShow rs) {
		String s = "0000" + (200 - rs.getWeight());
		return s.substring(s.length() - 4);
	}

	public static RosterShow getShowOfRosterItem(tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem item)
			throws XMLException {
		if (item.isAsk())
			return RosterShow.requested;
		if (item.getSubscription() == Subscription.none || item.getSubscription() == Subscription.to)
			return RosterShow.offline_nonauth;
		Presence p = XmppService.get().getModulesManager().getModule(PresenceModule.class).getPresence().getBestPresence(
				item.getJid());
		RosterShow r = RosterShow.offline;
		if (p != null) {
			if (p.getType() == StanzaType.unavailable)
				r = RosterShow.offline;
			else if (p.getShow() == Show.online)
				r = RosterShow.online;
			else if (p.getShow() == Show.away)
				r = RosterShow.away;
			else if (p.getShow() == Show.chat)
				r = RosterShow.chat;
			else if (p.getShow() == Show.dnd)
				r = RosterShow.dnd;
			else if (p.getShow() == Show.xa)
				r = RosterShow.xa;
		}
		return r;
	}

	private final Listener<PresenceEvent> presenceListener;

	private final PresenceModule presenceModule;

	private final Listener<RosterEvent> rosterListener;

	private RosterModule rosterModule;

	private SortMethod sortMethod = SortMethod.name;

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

		Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {

			public boolean execute() {
				if (isNotSorted())
					doSort();
				return true;
			}
		}, 5000);

		final Menu contextMenu = new Menu();

		final MenuItem editContactMI = new MenuItem("Edit contact", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> it = grid.getSelectionModel().getSelectedItem();
				if (it != null)
					(new ContactEditDialog(it.getId())).show();
			}
		});
		contextMenu.add(editContactMI);

		final Menu authMenu = new Menu();
		final MenuItem resendAuthMI = new MenuItem("Resend Authorization To", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem ri = getSelectedData();
				if (ri != null)
					try {
						XmppService.get().getModulesManager().getModule(PresenceModule.class).subscribed(
								JID.jidInstance(ri.getJid()));
					} catch (Exception e) {
						MessageBox.alert(Translations.instance.error(), "Can't send stanza", null);
					}
			}
		});
		final MenuItem rerequestAuthMI = new MenuItem("Rerequest Authorization From", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem ri = getSelectedData();
				if (ri != null)
					try {
						XmppService.get().getModulesManager().getModule(PresenceModule.class).subscribe(
								JID.jidInstance(ri.getJid()));
					} catch (Exception e) {
						MessageBox.alert(Translations.instance.error(), "Can't send stanza", null);
					}

			}
		});
		final MenuItem removeAuthMI = new MenuItem("Remove Authorization From", new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				// send: unsubscribed
				tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem ri = getSelectedData();
				if (ri != null)
					try {
						XmppService.get().getModulesManager().getModule(PresenceModule.class).unsubscribed(
								JID.jidInstance(ri.getJid()));
					} catch (Exception e) {
						MessageBox.alert(Translations.instance.error(), "Can't send stanza", null);
					}

			}
		});
		authMenu.add(resendAuthMI);
		authMenu.add(rerequestAuthMI);
		authMenu.add(removeAuthMI);
		final MenuItem authorizationMI = new MenuItem("Authorization");
		authorizationMI.setSubMenu(authMenu);
		contextMenu.add(authorizationMI);

		grid.setContextMenu(contextMenu);

		contextMenu.addListener(Events.Show, new com.extjs.gxt.ui.client.event.Listener<MenuEvent>() {

			public void handleEvent(MenuEvent be) {
				tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem ri = getSelectedData();
				editContactMI.setEnabled(ri != null);
				authorizationMI.setEnabled(ri != null);
			}
		});

	}

	@Override
	protected String getItemName(
			tigase.gwtcommons.client.roster.BasicRosterPanel.RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> model)
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
	protected String getQuickTip(
			tigase.gwtcommons.client.roster.BasicRosterPanel.RosterShow show,
			tigase.gwtcommons.client.roster.BasicRosterPanel.RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> model) {
		BareJID jid = model.getId().getJid();
		String r = jid.toString() + "<br/>";
		r += "Status: <b>" + show.name() + "</b>";

		try {
			Presence p = XmppService.get().getModulesManager().getModule(PresenceModule.class).getPresence().getBestPresence(
					jid);
			if (p != null && p.getStatus() != null) {
				r += "<br/>" + SafeHtmlUtils.fromString(p.getStatus()).asString();
			}
		} catch (XMLException e) {
		}
		return r;
	}

	@Override
	protected tigase.gwtcommons.client.roster.BasicRosterPanel.RosterShow getShowOf(
			tigase.gwtcommons.client.roster.BasicRosterPanel.RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> model)
			throws XMLException {
		return getShowOfRosterItem(model.getId());
	}

	public SortMethod getSortMethod() {
		return sortMethod;
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
		doFilters();
	}

	@Override
	protected void onDoubleClick(
			tigase.gwtcommons.client.roster.BasicRosterPanel.RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> item) {
		XmppService.get().createChat(JID.jidInstance(item.getId().getJid()));
	}

	protected void onPresenceEvent(PresenceEvent be) {
		doFilters();
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

	@Override
	protected int rosterItemCompare(
			tigase.gwtcommons.client.roster.BasicRosterPanel.RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> o1,
			tigase.gwtcommons.client.roster.BasicRosterPanel.RosterItem<tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem> o2) {
		try {
			if (sortMethod == SortMethod.unsorted)
				return 0;
			String w1 = "";
			String w2 = "";

			if (sortMethod == SortMethod.onlineName || sortMethod == SortMethod.onlineJid) {
				w1 += getRosterShowWeight(getShowOf(o1));
				w2 += getRosterShowWeight(getShowOf(o2));
			}

			if (sortMethod == SortMethod.name || sortMethod == SortMethod.onlineName) {
				w1 += ":" + o1.getId().getName().toLowerCase();
				w2 += ":" + o2.getId().getName().toLowerCase();
			} else if (sortMethod == SortMethod.jid || sortMethod == SortMethod.onlineJid) {
				w1 += ":" + o1.getId().getJid();
				w2 += ":" + o2.getId().getJid();
			}

			return w1.compareTo(w2);
		} catch (Exception e) {
			return 0;
		}
	}

	public void setSortMethod(SortMethod sortMethod) {
		this.sortMethod = sortMethod;
		doSort();
	}

}

package tigase.messenger.client;

import java.util.ArrayList;
import java.util.List;
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

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;

public class RosterPanel extends ContentPanel {

	public static class RosterItem extends BaseModelData {

		private static final long serialVersionUID = 1L;

		private final tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem item;

		public RosterItem(tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem item) {
			this.item = item;
			set("jid", item.getJid());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RosterItem)
				return item.equals(((RosterItem) obj).item);
			else
				return false;
		}

		public ArrayList<String> getGroups() {
			return item.getGroups();
		}

		public BareJID getJid() {
			return item.getJid();
		}

		public String getName() {
			return item.getName();
		}

		public Subscription getSubscription() {
			return item.getSubscription();
		}

		@Override
		public int hashCode() {
			return item.hashCode();
		}

		public boolean isAsk() {
			return item.isAsk();
		}

	}

	public enum RosterShow {
		away(99),
		chat(101),
		dnd(97),
		error(50),
		invisible(1),
		notinroster(0),
		offline(10),
		offline_nonauth(11),
		online(100),
		requested(51),
		xa(98);

		private final int weight;

		private RosterShow(int w) {
			this.weight = w;
		}

		public int getWeight() {
			return weight;
		}

	}

	public static RosterShow getShowOf(tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem item) throws XMLException {
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

				r = r.weight > tmp.weight ? r : tmp;
			}
		return r;
	}

	private final Grid<RosterItem> grid;

	private final Listener<PresenceEvent> presenceListener;

	private final PresenceModule presenceModule;

	private final Listener<RosterEvent> rosterListener;

	private RosterModule rosterModule;

	private final ListStore<RosterItem> store = new ListStore<RosterItem>();

	public RosterPanel() {
		setHeading("Roster");
		setLayout(new FitLayout());

		this.rosterModule = XmppService.get().getModulesManager().getModule(RosterModule.class);
		this.presenceModule = XmppService.get().getModulesManager().getModule(PresenceModule.class);

		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		ColumnConfig c = new ColumnConfig("presence", "Presence", 24);

		c.setRenderer(new GridCellRenderer<RosterPanel.RosterItem>() {

			public Object render(RosterItem model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<RosterItem> store, Grid<RosterItem> grid) {
				String x = GWT.getHostPageBaseURL();
				try {
					RosterShow r;
					if (XmppService.get().isConnected())
						r = getShowOf(model.item);
					else
						r = RosterShow.offline;
					return "<img src='" + (x == null ? "/" : x) + "presences/user-" + r.name() + ".png'/>";
				} catch (XMLException e) {
					return "<img src='" + (x == null ? "/" : x) + "presences/user-error.png'/>";
				}
			}
		});
		columns.add(c);

		c = new ColumnConfig("name", "Name", 200);
		c.setRenderer(new GridCellRenderer<RosterPanel.RosterItem>() {

			public Object render(RosterItem model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<RosterItem> store, Grid<RosterItem> grid) {
				try {
					return getItemName(model.item);
				} catch (XMLException e) {
					return "???";
				}
			}
		});
		columns.add(c);

		grid = new Grid<RosterPanel.RosterItem>(store, new ColumnModel(columns));
		grid.setAutoExpandColumn("name");
		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		grid.setColumnLines(false);
		grid.setHideHeaders(true);
		grid.setStripeRows(true);

		grid.addListener(Events.RowDoubleClick,
				new com.extjs.gxt.ui.client.event.Listener<GridEvent<RosterPanel.RosterItem>>() {

					public void handleEvent(GridEvent<RosterItem> be) {
						XmppService.get().createChat(JID.jidInstance(be.getModel().getJid()));
					}
				});

		add(grid);
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

	public String getItemName(tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem item) throws XMLException {
		Presence presence = presenceModule.getPresence().getBestPresence(item.getJid());
		String x = null;
		if (presence != null)
			x = presence.getNickname();
		x = item.getName();
		if (x == null || x.isEmpty())
			x = item.getJid().toString();

		return x;
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
		for (RosterItem m : store.getModels()) {
			store.update(m);
		}
	}

	protected void onPresenceEvent(PresenceEvent be) {
		BareJID bareJid = be.getJid().getBareJid();
		RosterItem m = store.findModel("jid", bareJid);
		if (m != null)
			store.update(m);
	}

	protected void onRosterEvent(RosterEvent be) {
		if (be.getType() == RosterModule.ItemAdded) {
			store.add(new RosterItem(be.getItem()));
		} else if (be.getType() == RosterModule.ItemRemoved) {
			store.remove(new RosterItem(be.getItem()));
		} else if (be.getType() == RosterModule.ItemAdded) {
			store.update(new RosterItem(be.getItem()));
		}
	}
}

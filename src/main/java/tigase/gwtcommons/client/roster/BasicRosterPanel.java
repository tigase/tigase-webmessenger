package tigase.gwtcommons.client.roster;

import java.util.ArrayList;
import java.util.List;

import tigase.gwtcommons.client.XmppService;
import tigase.jaxmpp.core.client.xml.XMLException;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.google.gwt.core.client.GWT;

public abstract class BasicRosterPanel<M> extends ContentPanel {

	public static class RosterItem<M> extends BaseModelData {

		private static final long serialVersionUID = 1L;

		private Object data;

		private M id;

		public RosterItem(M id) {
			setId(id);
		}

		public <T> RosterItem(M id, T data) {
			set("id", id);
			this.id = id;
			setData(data);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RosterItem)
				return id.equals(((RosterItem<?>) obj).id);
			else
				return false;
		}

		@SuppressWarnings("unchecked")
		public <T> T getData() {
			return (T) data;
		}

		public M getId() {
			return id;
		}

		public <T> void setData(T data) {
			set("data", data);
			this.data = data;
		}

		public void setId(M id) {
			set("id", id);
			this.id = id;
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

	protected final Grid<RosterItem<M>> grid;

	private boolean notSorted = true;

	private boolean showOffline = false;

	private final ListStore<RosterItem<M>> store = new ListStore<RosterItem<M>>();

	public BasicRosterPanel() {
		setHeading("Roster");
		setLayout(new FitLayout());

		store.setStoreSorter(new StoreSorter<BasicRosterPanel.RosterItem<M>>(null) {
			@Override
			public int compare(Store<RosterItem<M>> store, RosterItem<M> m1, RosterItem<M> m2, String property) {
				return rosterItemCompare(m1, m2);
			}
		});
		store.addFilter(new StoreFilter<BasicRosterPanel.RosterItem<M>>() {

			public boolean select(Store<RosterItem<M>> store, RosterItem<M> parent, RosterItem<M> item, String property) {
				return filterSelect(store, parent, item, property);
			}
		});

		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		ColumnConfig c = new ColumnConfig("presence", "Presence", 24);

		c.setRenderer(new GridCellRenderer<BasicRosterPanel.RosterItem<M>>() {

			public Object render(RosterItem<M> model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<RosterItem<M>> store, Grid<RosterItem<M>> grid) {
				String x = GWT.getHostPageBaseURL();
				try {
					RosterShow r;
					if (XmppService.get().isConnected())
						r = getShowOf(model);
					else
						r = RosterShow.offline;

					String tip = getQuickTip(r, model);

					String res = "";
					if (tip != null)
						res += "<span qtip='" + tip + "'>";
					res += "<img src='" + (x == null ? "/" : x) + "presences/user-" + r.name() + ".png'/>";
					if (tip != null)
						res += "</span>";
					return res;
				} catch (XMLException e) {
					return "<span qtip='Error'><img src='" + (x == null ? "/" : x) + "presences/user-error.png'/></span>";
				}
			}
		});
		columns.add(c);

		c = new ColumnConfig("name", "Name", 200);
		c.setRenderer(new GridCellRenderer<BasicRosterPanel.RosterItem<M>>() {

			public Object render(RosterItem<M> model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<RosterItem<M>> store, Grid<RosterItem<M>> grid) {
				try {
					RosterShow r;
					if (XmppService.get().isConnected())
						r = getShowOf(model);
					else
						r = RosterShow.offline;

					String tip = getQuickTip(r, model);

					String res = "";
					if (tip != null)
						res += "<span qtip='" + tip + "'>";
					res += getItemName(model);
					if (tip != null)
						res += "</span>";
					return res;
				} catch (XMLException e) {
					return "???";
				}
			}
		});
		columns.add(c);

		grid = new Grid<BasicRosterPanel.RosterItem<M>>(store, new ColumnModel(columns));
		grid.setAutoExpandColumn("name");
		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		grid.setColumnLines(false);
		grid.setHideHeaders(true);
		grid.setStripeRows(true);

		new QuickTip(grid);

		grid.addListener(Events.RowDoubleClick,
				new com.extjs.gxt.ui.client.event.Listener<GridEvent<BasicRosterPanel.RosterItem<M>>>() {

					public void handleEvent(GridEvent<RosterItem<M>> be) {
						onDoubleClick(be.getModel());
					}
				});

		add(grid);

	}

	public void add(M item) {
		RosterItem<M> m = new RosterItem<M>(item);
		store.add(m);
		doFilters();
	}

	public <T> void add(M item, T data) {
		RosterItem<M> m = new RosterItem<M>(item, data);
		store.add(m);
		doFilters();
	}

	protected void doFilters() {
		store.applyFilters("");
		notSorted = true;
	}

	protected void doSort() {
		store.sort("", SortDir.ASC);
		notSorted = false;
	}

	protected boolean filterSelect(Store<RosterItem<M>> store, RosterItem<M> parent, RosterItem<M> item, String property) {
		boolean result = true;
		if (showOffline)
			result = true;
		else
			try {
				RosterShow r;
				if (XmppService.get().isConnected())
					r = getShowOf(item);
				else
					r = RosterShow.offline;
				result = r == RosterShow.away || r == RosterShow.chat || r == RosterShow.dnd || r == RosterShow.online
						|| r == RosterShow.xa;
			} catch (XMLException e) {
				e.printStackTrace();
				result = showOffline;
			}
		return result;
	}

	protected abstract String getItemName(RosterItem<M> model) throws XMLException;

	protected abstract String getQuickTip(RosterShow r, RosterItem<M> model);

	protected abstract RosterShow getShowOf(RosterItem<M> model) throws XMLException;

	private ListStore<RosterItem<M>> getStore() {
		return store;
	}

	protected boolean isNotSorted() {
		return notSorted;
	}

	public boolean isShowOffline() {
		return showOffline;
	}

	protected abstract void onDoubleClick(RosterItem<M> item);

	public void remove(M item) {
		RosterItem<M> m = getStore().findModel("id", item);
		store.remove(m);
		doFilters();
	}

	protected int rosterItemCompare(BasicRosterPanel.RosterItem<M> o1, BasicRosterPanel.RosterItem<M> o2) {
		return 0;
	}

	public void setShowOffline(boolean showOffline) {
		this.showOffline = showOffline;
		doFilters();
	}

	public void update(M item) {
		RosterItem<M> m = getStore().findModel("id", item);
		store.update(m);
		doFilters();
	}

	public <T> void update(M item, T data) {
		RosterItem<M> m = getStore().findModel("id", item);
		m.setData(data);
		store.update(m);
		doFilters();
	}

}

package tigase.gwtcommons.client.roster;

import java.util.ArrayList;
import java.util.List;

import tigase.gwtcommons.client.XmppService;
import tigase.jaxmpp.core.client.xml.XMLException;

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

	private final ListStore<RosterItem<M>> store = new ListStore<RosterItem<M>>();

	public BasicRosterPanel() {
		setHeading("Roster");
		setLayout(new FitLayout());

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
					return "<img src='" + (x == null ? "/" : x) + "presences/user-" + r.name() + ".png'/>";
				} catch (XMLException e) {
					return "<img src='" + (x == null ? "/" : x) + "presences/user-error.png'/>";
				}
			}
		});
		columns.add(c);

		c = new ColumnConfig("name", "Name", 200);
		c.setRenderer(new GridCellRenderer<BasicRosterPanel.RosterItem<M>>() {

			public Object render(RosterItem<M> model, String property, ColumnData config, int rowIndex, int colIndex,
					ListStore<RosterItem<M>> store, Grid<RosterItem<M>> grid) {
				try {
					return getItemName(model);
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
	}

	public <T> void add(M item, T data) {
		RosterItem<M> m = new RosterItem<M>(item, data);
		store.add(m);
	}

	protected abstract String getItemName(RosterItem<M> model) throws XMLException;

	protected abstract RosterShow getShowOf(RosterItem<M> model) throws XMLException;

	public ListStore<RosterItem<M>> getStore() {
		return store;
	}

	protected abstract void onDoubleClick(RosterItem<M> item);

	public void remove(M item) {
		RosterItem<M> m = getStore().findModel("id", item);
		store.remove(m);
	}

	public void update(M item) {
		RosterItem<M> m = getStore().findModel("id", item);
		store.update(m);
	}

	public <T> void update(M item, T data) {
		RosterItem<M> m = getStore().findModel("id", item);
		m.setData(data);
		store.update(m);
	}

}

package tigase.messenger.client;

import tigase.messenger.client.roster.component.Roster;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

public class TabbedViewport extends Viewport {

	private final Roster rosterComponent;

	private final ToolBar toolBar = new ToolBar();

	public TabbedViewport(Roster rosterComponent) {
		this.rosterComponent = rosterComponent;
	}

	private final TabPanel tabPanel = new TabPanel();

	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		setLayout(new BorderLayout());

		ContentPanel north = new ContentPanel();
		north.setHeaderVisible(false);
		north.setBodyBorder(false);
		north.add(this.toolBar);

		TextToolItem item1 = new TextToolItem("Button w/ Menu");
		toolBar.add(item1);

		ContentPanel west = new ContentPanel();
		west.add(this.rosterComponent);

		ContentPanel east = new ContentPanel();
		ContentPanel south = new ContentPanel();

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH,10,0,0);
		northData.setCollapsible(false);
		northData.setFloatable(false);
		northData.setSplit(false);
		northData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200);
		westData.setSplit(true);
		westData.setCollapsible(true);
		westData.setMargins(new Margins(5));

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(5, 0, 5, 0));

		BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, 200);
		eastData.setSplit(true);
		eastData.setCollapsible(true);
		eastData.setMargins(new Margins(5));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setMargins(new Margins(0, 5, 5, 5));

		TabItem item = new TabItem();
		item.setText("GWT");
		item.setIconStyle("icon-tabs");
		tabPanel.add(item);
		tabPanel.setSelection(item);

		add(north, northData);
		add(west, westData);
		add(tabPanel, centerData);
		add(east, eastData);
		add(south, southData);
	}
}

package tigase.messenger.client.tabbed;

import tigase.messenger.client.MainToolBar;
import tigase.messenger.client.roster.component.Roster;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.state.StateManager;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class TabbedViewport extends Viewport {

	private final Roster rosterComponent;

	public TabbedViewport(Roster rosterComponent) {
		super();
		this.rosterComponent = rosterComponent;

		BorderLayout layout = new BorderLayout();
		layout.setEnableState(false);
		setLayout(layout);

		ToolBar toolBar = getMainToolBar();
		ContentPanel west = new ContentPanel();
		ContentPanel center = new ContentPanel();
		ContentPanel south = new ContentPanel();

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 30);
		northData.setSplit(false);
		northData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200);
		westData.setSplit(true);
		westData.setCollapsible(true);
		westData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setMargins(new Margins(0, 0, 0, 0));

		west.add(this.rosterComponent);
		west.setScrollMode(Scroll.AUTO);

		TabPanel tabFolder = new TabPanel();
		tabFolder.setAutoWidth(true);
		tabFolder.setTabScroll(true);

		tabFolder.add(new ChatTab());
		tabFolder.add(new ChatTab());

		center.add(tabFolder);

		add(toolBar, northData);
		add(center, centerData);
		add(west, westData);
		add(south, southData);

	}

	protected MainToolBar getMainToolBar() {
		return new MainToolBar();
	}

}

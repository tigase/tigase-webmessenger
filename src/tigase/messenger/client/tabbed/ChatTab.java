package tigase.messenger.client.tabbed;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class ChatTab extends TabItem {

	private final HTML conversation = new HTML("<span>asdasdasd</span>");

	private final TextArea message = new TextArea();

	public ChatTab() {
		setText("Chat");
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);

		setLayout(new BorderLayout());

		ContentPanel north = new ContentPanel();
		ContentPanel west = new ContentPanel();
		ContentPanel center = new ContentPanel();
		ContentPanel east = new ContentPanel();
		ContentPanel south = new ContentPanel();

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 100);
		northData.setSplit(true);
		northData.setMargins(new Margins(5, 5, 0, 5));

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
		southData.setMargins(new Margins(0, 5, 5, 5));

		add(north, northData);
		add(west, westData);
		add(center, centerData);
		add(east, eastData);
		add(south, southData);

	}
}

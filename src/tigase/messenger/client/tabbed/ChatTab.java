package tigase.messenger.client.tabbed;

import tigase.messenger.client.Messenger;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
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
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData.FillType;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class ChatTab extends TabItem {

	private final HTML conversation = new HTML("<span>asdasdasd</span>");

	private final TextArea message = new TextArea();

	private String nickname;

	public ChatTab(JID buddyJid, String threadId) {
		setClosable(true);
		RosterItem ri = Messenger.session().getRosterPlugin().getRosterItem(buddyJid);
		if (ri == null || ri.getName() == null) {
			nickname = buddyJid.toString();
		} else {
			nickname = ri.getName();
		}
		setText("chat " + nickname);
	}

	protected Component createInputPanel() {
		LayoutContainer panel = new LayoutContainer();
		panel.setLayout(new FillLayout());

		BorderLayout layout = new BorderLayout();
		layout.setEnableState(false);
		panel.setLayout(layout);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 30);
		northData.setSplit(false);
		northData.setMargins(new Margins(0, 0, 0, 0));

		ToolBar toolbar = new ToolBar();

		message.setWidth("100%");

		panel.add(toolbar, northData);
		panel.add(message, centerData);

		return panel;
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);

		setLayout(new BorderLayout());

		ContentPanel center = new ContentPanel();
		ContentPanel south = new ContentPanel();

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(5, 0, 5, 0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setMargins(new Margins(0, 5, 5, 5));

		add(center, centerData);
		add(createInputPanel(), southData);

	}
}

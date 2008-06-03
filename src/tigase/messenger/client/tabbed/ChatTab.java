package tigase.messenger.client.tabbed;

import java.util.Date;

import tigase.messenger.client.Messenger;
import tigase.messenger.client.roster.component.RosterPresence;
import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.xmpp.message.Message;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;

public class ChatTab extends TabItem {

	private final HTML conversation = new HTML();

	private final TextArea message = new TextArea();

	private String nickname;

	private String threadid;

	private JID jid;

	public ChatTab(JID buddyJid, String threadId) {
		this.jid = buddyJid;
		this.threadid = threadId;
		setClosable(true);
		center.setHeaderVisible(false);
		setIconStyle("buddy-chat");

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

		message.addListener(Events.KeyPress, new Listener<FieldEvent>() {

			public void handleEvent(FieldEvent be) {
				// TODO Auto-generated method stub
				if (be.getKeyCode() == KeyboardListener.KEY_ENTER) {
					send();
				}
			}
		});

		return panel;
	}

	protected void addLine(String body) {
		String html = this.conversation.getHTML();
		html += body + "<br/>";
		this.conversation.setHTML(html);
		center.setVScrollPosition(this.conversation.getOffsetHeight());
	}

	protected void addLine(String styleName, String body) {
		String x = "<span";
		if (styleName != null) {
			x += " class='" + styleName + "'";
		}
		x += ">" + body + "</span>";
		addLine(x);
	}

	protected void addLine(String styleName, String date, String nickname, String body) {
		String x = "<span";
		if (styleName != null) {
			x += " class='" + styleName + "'";
		}
		x += ">[" + date + "] " + nickname + ":</span>&nbsp;" + body;
		addLine(x);
	}

	protected DateTimeFormat dtf = DateTimeFormat.getFormat("HH:mm:ss");

	protected void send() {
		String msg = message.getValue().toString();
		message.setRawValue("");

		Messenger.session().getChatPlugin().sendChatMessage(jid, msg, threadid, null);

		addLine("me", dtf.format(new Date()), "Me", msg);
	}

	private ContentPanel center = new ContentPanel();

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);

		setLayout(new BorderLayout());

		ContentPanel south = new ContentPanel();

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(5, 5, 5, 5));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setMargins(new Margins(0, 5, 5, 5));

		center.add(this.conversation);
		center.setScrollMode(Scroll.AUTO);

		add(center, centerData);
		add(createInputPanel(), southData);

	}

	public void process(Message message) {
		String nickname;
		RosterItem ri = Messenger.session().getRosterPlugin().getRosterItem(message.getFrom());
		if (ri != null) {
			nickname = ri.getName();
		} else if (message.getExtNick() != null) {
			nickname = message.getExtNick();
		} else {
			nickname = null;
		}

		if (nickname == null || nickname.trim().length() < 1) {
			nickname = message.getFrom().toStringBare();
		}

		addLine("me", dtf.format(new Date()), nickname, message.getBody());
	}

	public void setPresenceIcon(RosterPresence rp) {
		switch (rp) {
			case ONLINE:
				setIconStyle("buddy-online");
				break;
			case READY_FOR_CHAT:
				setIconStyle("buddy-chat");
				break;
			case AWAY:
				setIconStyle("buddy-away");
				break;
			case XA:
				setIconStyle("buddy-xa");
				break;
			case DND:
				setIconStyle("buddy-dnd");
				break;
			case OFFLINE:
				setIconStyle("buddy-offline");
				break;
			case ASK:
				setIconStyle("buddy-ask");
				break;
			case ERROR:
				setIconStyle("buddy-error");
				break;
			default:
				break;
		}
	}
}

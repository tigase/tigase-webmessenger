package tigase.messenger.client;

import java.util.Date;

import tigase.xmpp4gwt.client.TextUtils;
import tigase.xmpp4gwt.client.stanzas.IQ;
import tigase.xmpp4gwt.client.stanzas.Message;
import tigase.xmpp4gwt.client.xmpp.ErrorCondition;
import tigase.xmpp4gwt.client.xmpp.message.Chat;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;
import tigase.xmpp4gwt.client.xmpp.roster.RosterPlugin;
import tigase.xmpp4gwt.client.xmpp.xeps.vcard.VCard;
import tigase.xmpp4gwt.client.xmpp.xeps.vcard.VCardResponseHandler;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Popup;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class ChatTab extends TabItem {

	private static String linkhtml(String body) {
		body = body == null ? body : body.replaceAll("([^>/\";]|^)(www\\.[^ ]+)",
				"$1<a href=\"http://$2\" target=\"_blank\">$2</a>");
		body = body == null ? body : body.replaceAll("([^\">;]|^)(http://[^ ]+)", "$1<a href=\"$2\" target=\"_blank\">$2</a>");
		return body;
	}

	private ContentPanel center = new ContentPanel();

	private final Html chat = new Html();

	private DateTimeFormat dtf = DateTimeFormat.getFormat("HH:mm:ss");

	private Chat<ChatTab> item;

	private final TextArea message = new TextArea();

	private String nick;

	private final RosterPlugin rosterPlugin;

	private boolean unread = false;

	private final Label title = new Label("");

	private final Label description = new Label("");

	private ContentPanel prepareChatHeader() {
		ContentPanel result = new ContentPanel();
		result.addStyleName("chatHeader");
		result.setLayout(new RowLayout(Orientation.HORIZONTAL));
		result.setHeaderVisible(false);

		title.setStyleName("chatTitle");

		description.setStyleName("chatDescription");

		Image i = new Image("chat-big.png");

		result.add(i, new RowData(58, 58));

		ContentPanel textPanel = new ContentPanel();
		textPanel.setHeight("100%");
		textPanel.setWidth("100%");
		textPanel.setHeaderVisible(false);
		textPanel.setBodyBorder(false);
		textPanel.setFrame(false);

		textPanel.setLayout(new RowLayout(Orientation.VERTICAL));

		textPanel.add(title, new RowData());
		textPanel.add(description, new RowData());

		result.add(textPanel, new RowData());

		return result;
	}

	private boolean titleSetted = false;

	public ChatTab(Chat<ChatTab> chat, RosterPlugin rosterPlugin) {
		this.item = chat;
		this.rosterPlugin = rosterPlugin;
		this.nick = chat.getJid().toString();
		addStyleName("chatTabItem");

		RosterItem ri = rosterPlugin.getRosterItem(chat.getJid());
		if (ri != null && ri.getName() != null && ri.getName().trim().length() > 0) {
			titleSetted = true;
			this.nick = ri.getName();
		}
		setText("Chat with " + this.nick);
		setIconStyle("chat-icon");
		setClosable(true);

		setLayout(new BorderLayout());

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 60, 60, 60);
		northData.setSplit(false);
		northData.setCollapsible(false);
		northData.setFloatable(false);
		northData.setMargins(new Margins(0, 0, 5, 0));

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0, 0, 0, 0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setMargins(new Margins(5, 0, 0, 0));

		center.setHeaderVisible(false);
		center.add(this.chat);
		center.setScrollMode(Scroll.AUTO);

		ContentPanel north = prepareChatHeader();

		ContentPanel south = new ContentPanel();
		FlowLayout layout = new FlowLayout();
		south.setLayout(layout);
		south.setHeaderVisible(false);
		this.message.setSize("100%", "100%");
		ToolBar tb = new ToolBar();
		tb.add(new FillToolItem());
		tb.add(new TextToolItem("Send", new SelectionListener<ToolBarEvent>() {

			public void componentSelected(ToolBarEvent ce) {
				send();
			}
		}));

		south.add(tb);

		south.add(this.message);

		add(north, northData);
		add(center, centerData);
		add(south, southData);

		this.message.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ENTER) {
					message.cancelKey();
					send();
				}
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
			}
		});

		title.setText(this.nick);
		description.setText(item.getJid().toString());
		this.title.setTitle(item.getJid().toString());

		if (!titleSetted)
			Messenger.session().getVCardPlugin().vCardRequest(chat.getJid().getBareJID(), new VCardResponseHandler() {

				public void onSuccess(VCard vcard) {
					String n = vcard.getName();
					if (n != null && n.trim().length() > 0) {
						title.setText(n);
						titleSetted = true;
					}
				}

				public void onError(IQ iq, ErrorType errorType, ErrorCondition errorCondition, String text) {
				}
			});
	}

	private void add(String x) {
		String m = this.chat.getHtml();
		m = (m == null ? "" : m) + x + "<br/>";
		this.chat.setHtml(m);
		center.setVScrollPosition(this.chat.getHeight());
	}

	private void add(String style, Date date, String nick, String message) {
		String x = "[" + dtf.format(date) + "]&nbsp; <span class='" + style + "'>" + nick + ": "
				+ linkhtml(TextUtils.escape(message)) + "</span>";
		System.out.println(x);
		add(x);
	}

	public Chat<ChatTab> getChatItem() {
		return item;
	}

	public void process(Message message) {
		if (!titleSetted && message.getExtNick() != null && message.getExtNick().trim().length() > 0) {
			title.setText(message.getExtNick());
			titleSetted = true;
		}
		if (!item.getJid().toString().equals(this.description.getText())) {
			this.description.setText(item.getJid().toString());
			this.title.setTitle(item.getJid().toString());
		}
		final String body = message.getBody();
		if (message.getFrom() != null) {
			RosterItem ri = this.rosterPlugin.getRosterItem(message.getFrom().getBareJID());
			if (ri != null && ri.getName() != null) {
				this.nick = ri.getName();
			}
		}
		if (body != null) {
			add("peer", new Date(), nick, body);
		}

	}

	private void send() {
		final String message = this.message.getText();
		this.message.setText("");
		add("me", new Date(), "Me", message);
		this.item.send(message);
	}

	public void setReaded() {
		center.setVScrollPosition(this.chat.getHeight());
		if (unread) {
			unread = false;
			getHeader().removeStyleName("unread");
			setText("Chat with " + this.nick);
		}
	}

	public void setUnread() {
		if (!unread) {
			getHeader().addStyleName("unread");
			setText("* Chat with " + this.nick);
			unread = true;
		}
	}
}

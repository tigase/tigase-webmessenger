package tigase.messenger.client;

import java.util.Date;

import tigase.xmpp4gwt.client.TextUtils;
import tigase.xmpp4gwt.client.stanzas.Message;
import tigase.xmpp4gwt.client.xmpp.message.Chat;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;
import tigase.xmpp4gwt.client.xmpp.roster.RosterPlugin;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.KeyboardListener;
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

	public ChatTab(Chat<ChatTab> chat, RosterPlugin rosterPlugin) {
		this.item = chat;
		this.rosterPlugin = rosterPlugin;
		this.nick = chat.getJid().toString();

		RosterItem ri = rosterPlugin.getRosterItem(chat.getJid());
		if (ri.getName() != null)
			this.nick = ri.getName();

		setText("Chat with " + this.nick);
		setIconStyle("chat-icon");
		setClosable(true);

		setLayout(new BorderLayout());

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(5, 0, 0, 0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setMargins(new Margins(5, 0, 0, 0));

		center.setHeaderVisible(false);
		center.add(this.chat);
		center.setScrollMode(Scroll.AUTO);

		ContentPanel south = new ContentPanel();
		south.setHeaderVisible(false);
		this.message.setSize("100%", "100%");
		south.add(this.message);

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

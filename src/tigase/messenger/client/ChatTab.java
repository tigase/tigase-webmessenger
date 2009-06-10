package tigase.messenger.client;

import java.util.Date;

import tigase.jaxmpp.core.client.TextUtils;
import tigase.jaxmpp.core.client.events.Event;
import tigase.jaxmpp.core.client.stanzas.IQ;
import tigase.jaxmpp.core.client.stanzas.Message;
import tigase.jaxmpp.core.client.stanzas.Message.Type;
import tigase.jaxmpp.core.client.xmpp.ErrorCondition;
import tigase.jaxmpp.core.client.xmpp.message.Chat;
import tigase.jaxmpp.core.client.xmpp.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.roster.RosterPlugin;
import tigase.jaxmpp.core.client.xmpp.xeps.vcard.VCard;
import tigase.jaxmpp.core.client.xmpp.xeps.vcard.VCardResponseHandler;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class ChatTab extends TabItem {

	public static class ChatTabEvent extends Event {

		private final Chat<ChatTab> item;

		private final String message;

		public ChatTabEvent(Chat<ChatTab> item, String message) {
			this.item = item;
			this.message = message;
		}

		public Chat<ChatTab> getItem() {
			return item;
		}

		public String getMessage() {
			return message;
		}

	}

	public static enum Events {
		MESSAGE_SENT
	}

	private static String linkhtml(String body) {
		body = body == null ? body : body.replaceAll("([^>/\";]|^)(www\\.[^ ]+)",
				"$1<a href=\"http://$2\" target=\"_blank\">$2</a>");
		body = body == null ? body : body.replaceAll("([^\">;]|^)(http(s)?://[^ ]+)",
				"$1<a href=\"$2\" target=\"_blank\">$2</a>");
		return body;
	}

	private ContentPanel center = new ContentPanel();

	private final Html chat = new Html();

	private final Label description = new Label("");

	private DateTimeFormat dtf = DateTimeFormat.getFormat("HH:mm:ss");

	private Chat<ChatTab> item;

	private final TextArea message = new TextArea();

	private String nick;

	private final RosterPlugin rosterPlugin;

	private final Label title = new Label("");

	private boolean titleSetted = false;

	private boolean unread = false;

	private int unreadCount = 0;

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
		tb.add(new Button("Send", new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
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
			Tigase_messenger.session().getVCardPlugin().vCardRequest(chat.getJid().getBareJID(), new VCardResponseHandler() {

				public void onError(IQ iq, ErrorType errorType, ErrorCondition errorCondition, String text) {
				}

				@Override
				public void onSuccess(VCard vcard) {
					String n = vcard.getName();
					if (n != null && n.trim().length() > 0) {
						title.setText(n);
						titleSetted = true;
					}
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

	public int getUnreadCount() {
		return unreadCount;
	}

	private ContentPanel prepareChatHeader() {
		ContentPanel result = new ContentPanel();
		result.addStyleName("chatHeader");
		result.setLayout(new RowLayout(Orientation.HORIZONTAL));
		result.setHeaderVisible(false);

		title.setStyleName("chatTitle");

		description.setStyleName("chatDescription");

		String t = "data:image/jpg;base64,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";
		
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
		final Date date = new Date();

		if (message.getType() == Type.error) {
			ErrorCondition condition = ErrorDialog.getErrorCondition(message.getFirstChild("error"));
			String text = ErrorDialog.getErrorText(message.getFirstChild("error"));
			String x = "<div class='error'>[" + dtf.format(date) + "]&nbsp; <span class='error'>Error: "
					+ condition.name().replace('_', ' ');
			if (text != null) {
				x += "<br/>" + TextUtils.escape(text);
			}
			if (body != null)
				x += "<br/>----<br/>" + TextUtils.escape(body);
			x += "</span></div>";
			add(x);
			if (unread) {
				unreadCount++;
				setText("(" + unreadCount + ") Chat with " + this.nick);
			}
		} else if (message.getFrom() != null) {
			RosterItem ri = this.rosterPlugin.getRosterItem(message.getFrom().getBareJID());
			if (ri != null && ri.getName() != null) {
				this.nick = ri.getName();
			}
		}
		if (body != null) {
			add("peer", date, nick, body);
			if (unread) {
				unreadCount++;
				setText("(" + unreadCount + ") Chat with " + this.nick);
			}
		}

	}

	private void send() {
		final String message = this.message.getText();
		final ChatTabEvent event = new ChatTabEvent(item, message);
		this.message.setText("");
		add("me", new Date(), "Me", message);
		this.item.send(message);
		Tigase_messenger.eventsManager().fireEvent(Events.MESSAGE_SENT, event);
	}

	public void setReaded() {
		center.setVScrollPosition(this.chat.getHeight());
		if (unread) {
			unread = false;
			unreadCount = 0;
			getHeader().removeStyleName("unread");
			setText("Chat with " + this.nick);
		}
	}

	public void setUnread() {
		if (!unread) {
			getHeader().addStyleName("unread");
			// setText("* Chat with " + this.nick);
			unread = true;
		}
	}
}

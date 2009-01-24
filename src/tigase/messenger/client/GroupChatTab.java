package tigase.messenger.client;

import java.util.Date;

import tigase.gwt.components.roster.client.GroupChatRoster;
import tigase.gwt.components.roster.client.PresenceCallback;
import tigase.gwt.components.roster.client.Roster;
import tigase.gwt.components.roster.client.RosterPresence;
import tigase.gwt.components.roster.client.GroupChatRoster.GroupNamesCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.TextUtils;
import tigase.jaxmpp.core.client.stanzas.Message;
import tigase.jaxmpp.core.client.stanzas.Presence;
import tigase.jaxmpp.core.client.stanzas.Message.Type;
import tigase.jaxmpp.core.client.xmpp.ErrorCondition;
import tigase.jaxmpp.core.client.xmpp.xeps.muc.GroupChat;
import tigase.jaxmpp.core.client.xmpp.xeps.muc.Role;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class GroupChatTab extends TabItem {

	private static String linkhtml(String body) {
		body = body == null ? body : body.replaceAll("([^>/\";]|^)(www\\.[^ ]+)", "$1<a href=\"http://$2\" target=\"_blank\">$2</a>");
		body = body == null ? body : body.replaceAll("([^\">;]|^)(http://[^ ]+)", "$1<a href=\"$2\" target=\"_blank\">$2</a>");
		return body;
	}

	private ContentPanel center = new ContentPanel();

	private final Html chat = new Html();

	private final Label description = new Label("");

	private DateTimeFormat dtf = DateTimeFormat.getFormat("HH:mm:ss");

	private GroupChat item;

	private final TextArea message = new TextArea();

	private final GroupChatRoster rosterComponent;

	private final Label title = new Label("");

	private boolean unread = false;

	public GroupChatTab(final GroupChat groupChat) {
		this.item = groupChat;
		addStyleName("chatTabItem");
		this.message.setEnabled(false);

		setText("Room: " + groupChat.getRoomJid().getNode());
		setIconStyle("group-chat-icon");
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

		BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, 200);
		eastData.setSplit(true);
		eastData.setCollapsible(true);
		eastData.setMargins(new Margins(5));

		this.rosterComponent = new GroupChatRoster(new PresenceCallback() {

			public RosterPresence getRosterPresence(JID jid) {
				Presence presence = groupChat.getPresence(jid);
				if (presence != null) {
					return Roster.rosterPresenceFromPresence(presence);
				} else {
					return RosterPresence.OFFLINE;
				}
			}
		}, new GroupNamesCallback() {

			public String[] getGroupsOf(JID jid) {
				Role role = groupChat.getRole(jid);
				switch (role) {
				case moderator:
					return new String[] { "Moderators" };
				case participant:
					return new String[] { "Participants" };
				case visitor:
					return new String[] { "Visitors" };
				case none:
					return new String[] { "None" };
				}
				return null;
			}
		});

		ContentPanel east = new ContentPanel();
		east.setHeaderVisible(false);
		east.setScrollMode(Scroll.AUTO);
		east.add(rosterComponent);

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

			@Override
			public void componentSelected(ToolBarEvent ce) {
				send();
			}
		}));

		south.add(tb);

		south.add(this.message);

		add(north, northData);
		add(center, centerData);
		add(south, southData);
		add(east, eastData);

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

		title.setText(item.getRoomJid().toStringBare());
		description.setText(" ");
		this.title.setTitle(item.getRoomJid().toStringBare());
	}

	private void add(String x) {
		String m = this.chat.getHtml();
		m = (m == null ? "" : m) + x + "<br/>";
		this.chat.setHtml(m);
		center.setVScrollPosition(this.chat.getHeight());
	}

	private void add(String style, Date date, String nick, String message) {
		String x = "[" + dtf.format(date) + "]&nbsp; <span class='" + style + "'>" + nick + ": " + linkhtml(TextUtils.escape(message)) + "</span>";
		add(x);
	}

	public GroupChat getGroupChat() {
		return this.item;
	}

	private ContentPanel prepareChatHeader() {
		ContentPanel result = new ContentPanel();
		result.addStyleName("chatHeader");
		result.setLayout(new RowLayout(Orientation.HORIZONTAL));
		result.setHeaderVisible(false);

		title.setStyleName("chatTitle");

		description.setStyleName("chatDescription");

		Image i = new Image("group-chat-big.png");

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
		final String body = message.getBody();
		final String nick = message.getFrom().getResource();
		final Date date = new Date();
		if (message.getType() == Type.error) {
			ErrorCondition condition = ErrorDialog.getErrorCondition(message.getFirstChild("error"));
			String text = ErrorDialog.getErrorText(message.getFirstChild("error"));
			String x = "<div class='error'>[" + dtf.format(date) + "]&nbsp; <span class='error'>Error: " + condition.name().replace('_', ' ');
			if (text != null) {
				x += "<br/>" + TextUtils.escape(text);
			}
			if (body != null)
				x += "<br/>----<br/>" + TextUtils.escape(body);
			x += "</span></div>";
			add(x);
		} else if (body != null) {
			add("peer", date, nick, body);
		}

	}

	public void process(Presence presence) {
		System.out.println(presence);
		rosterComponent.updatePresence(presence);
	}

	private void send() {
		final String message = this.message.getText();
		this.message.setText("");
		this.item.send(message);
	}

	@Override
	public void setEnabled(boolean enabled) {
		message.setEnabled(enabled);
	}

	public void setReaded() {
		center.setVScrollPosition(this.chat.getHeight());
		if (unread) {
			unread = false;
			getHeader().removeStyleName("unread");
			setText("Room: " + item.getRoomJid().getNode());
		}
	}

	public void setUnread() {
		if (!unread) {
			getHeader().addStyleName("unread");
			setText("* Room: " + item.getRoomJid().getNode());
			unread = true;
		}
	}
}

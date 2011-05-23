package tigase.gwtcommons.client.chat;

import tigase.gwtcommons.client.Tab;
import tigase.gwtcommons.client.Translations;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.logger.LoggerFactory;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class ChatTab extends Tab {

	private final ChatPanel chatPanel;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public ChatTab(Chat chat) {
		this.chatPanel = new ChatPanel(chat) {
			@Override
			protected void markUnread(boolean b) {
				ChatTab.this.markUnread(b);
				super.markUnread(b);
			}
		};
		this.chatPanel.setBodyBorder(false);
		setText(Translations.instance.chatWithTabName(chat.getJid().getBareJid().toString()));
		setClosable(true);
		setLayout(new FitLayout());

		add(chatPanel);
	}

	public void add(final Message message) {
		chatPanel.add(message);
	}

	public Chat getChat() {
		return chatPanel.getChat();
	}

	public int getTextAreaSize() {
		return chatPanel.textAreaSize;
	}

	protected void markUnread(boolean b) {
		if (chatPanel.hasUnread != b && b) {
			setText("* " + Translations.instance.chatWithTabName(chatPanel.getChat().getJid().getBareJid().toString()));
		} else if (chatPanel.hasUnread != b && !b) {
			setText(Translations.instance.chatWithTabName(chatPanel.getChat().getJid().getBareJid().toString()));
		}
	}

	public void onDeselect() {
		chatPanel.onDeselect();
	}

	public void onSelect() {
		chatPanel.onSelect();
	}
}

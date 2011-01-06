package tigase.messenger.client.chat;

import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.messenger.client.XmppService;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;

public class ChatManagerModule {

	private final Listener<MessageEvent> listener;

	private final TabPanel tabPanel;

	public ChatManagerModule(TabPanel center) {
		this.tabPanel = center;
		this.listener = new Listener<MessageModule.MessageEvent>() {

			public void handleEvent(MessageEvent be) {
				if (be.getType() == MessageModule.ChatCreated) {
					onChatCreated(be);
				} else if (be.getType() == MessageModule.MessageReceived) {
					onMessageReceived(be);
				}
			}
		};
	}

	private ChatTab getChatTab(Chat c) {
		for (TabItem i : this.tabPanel.getItems()) {
			if (i instanceof ChatTab && ((ChatTab) i).getChat().equals(c))
				return (ChatTab) i;
		}
		return null;
	}

	public void init() {
		tabPanel.addListener(Events.Remove, new com.extjs.gxt.ui.client.event.Listener<TabPanelEvent>() {

			public void handleEvent(TabPanelEvent be) {
				if (be.getItem() instanceof ChatTab)
					onChatTabClose((ChatTab) be.getItem());
			}
		});
		tabPanel.addListener(Events.Select, new com.extjs.gxt.ui.client.event.Listener<TabPanelEvent>() {

			public void handleEvent(TabPanelEvent be) {
				TabItem item = be.getItem();
				for (TabItem i : tabPanel.getItems()) {
					if (i instanceof ChatTab && i == item)
						((ChatTab) i).onSelect();
					else if (i instanceof ChatTab && i != item)
						((ChatTab) i).onDeselect();
				}
			}
		});
		XmppService.get().getModulesManager().getModule(MessageModule.class).addListener(MessageModule.ChatCreated, listener);
		XmppService.get().getModulesManager().getModule(MessageModule.class).addListener(MessageModule.MessageReceived,
				listener);
	}

	protected void onChatCreated(MessageEvent be) {
		ChatTab tab = new ChatTab(be.getChat());
		tabPanel.add(tab);
	}

	protected void onChatTabClose(final ChatTab item) {
		XmppService.get().getModulesManager().getModule(MessageModule.class).getChatManager().close(item.getChat());
	}

	protected void onMessageReceived(MessageEvent be) {
		ChatTab ct = getChatTab(be.getChat());
		ct.add(be.getMessage());
	}
}

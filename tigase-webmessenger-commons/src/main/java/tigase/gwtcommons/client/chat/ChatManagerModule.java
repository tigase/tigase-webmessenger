package tigase.gwtcommons.client.chat;

import tigase.gwtcommons.client.XmppService;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.MessageEvent;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

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
		final ChatTab tab = new ChatTab(be.getChat());
		tabPanel.add(tab);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			public void execute() {
				tabPanel.setSelection(tab);
			}
		});
	}

	protected void onChatTabClose(final ChatTab item) {
		try {
			XmppService.get().getModulesManager().getModule(MessageModule.class).getChatManager().close(item.getChat());
		} catch (JaxmppException e) {
			e.printStackTrace();
		}
	}

	protected void onMessageReceived(MessageEvent be) {
		ChatTab ct = getChatTab(be.getChat());
		ct.add(be.getMessage());
	}
}

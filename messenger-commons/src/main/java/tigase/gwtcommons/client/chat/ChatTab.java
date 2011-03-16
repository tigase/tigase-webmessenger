package tigase.gwtcommons.client.chat;

import tigase.gwtcommons.client.MessagePanel;
import tigase.gwtcommons.client.Tab;
import tigase.gwtcommons.client.Translations;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;

public class ChatTab extends Tab {

	private final Chat chat;

	private boolean hasUnread = false;

	private final MessagePanel messagePanel = new MessagePanel();

	private boolean selected;

	private final Button sendButton;

	private final ContentPanel southPanel = new ContentPanel(new FitLayout());

	private final TextArea text = new TextArea();

	private int textAreaSize = 100;

	public ChatTab(Chat chat) {
		this.chat = chat;
		setText(Translations.instance.chatWithTabName(chat.getJid().getBareJid().toString()));
		setClosable(true);
		setLayout(new BorderLayout());

		text.setBorders(false);
		southPanel.setBodyBorder(false);
		southPanel.setBorders(false);
		southPanel.setFrame(false);
		southPanel.setHeaderVisible(false);
		southPanel.add(text);

		this.text.addListener(Events.KeyPress, new Listener<FieldEvent>() {

			public void handleEvent(FieldEvent be) {
				if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
					be.setCancelled(true);
					be.preventDefault();
					sendMessage();
				}
			}
		});
		this.sendButton = new Button(Translations.instance.sendButton(), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				sendMessage();
			}
		});

		ToolBar tb = new ToolBar();
		tb.add(new FillToolItem());
		tb.add(this.sendButton);
		southPanel.setBottomComponent(tb);

	}

	public void add(final Message message) {
		try {
			messagePanel.addHisMessage(chat.getJid().toString(), message.getBody());
			if (!selected) {
				markUnread(true);
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public Chat getChat() {
		return chat;
	}

	public int getTextAreaSize() {
		return textAreaSize;
	}

	private void markUnread(boolean b) {
		if (this.hasUnread != b && b) {
			setText("* " + Translations.instance.chatWithTabName(chat.getJid().getBareJid().toString()));
		} else if (this.hasUnread != b && !b) {
			setText(Translations.instance.chatWithTabName(chat.getJid().getBareJid().toString()));
		}
		this.hasUnread = b;
	}

	public void onDeselect() {
		this.selected = false;
	}

	@Override
	protected void onRender(Element parent, int pos) {
		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, textAreaSize);
		southData.setSplit(true);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setMargins(new Margins(5, 0, 0, 0));

		add(messagePanel, centerData);
		add(southPanel, southData);

		super.onRender(parent, pos);
	}

	public void onSelect() {
		this.selected = true;
		markUnread(false);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			public void execute() {
				text.focus();
			}
		});
	}

	private void sendMessage() {
		String v = text.getValue();
		text.clear();
		messagePanel.addMineMessage(Translations.instance.me(), v);
		try {
			if (v != null)
				v = SafeHtmlUtils.fromString(v).asString();
			ChatTab.this.chat.sendMessage(v);
		} catch (Exception e) {
			messagePanel.addErrorMessage(e.getMessage());
		}
	}

	public void setTextAreaSize(int textAreaSize) {
		this.textAreaSize = textAreaSize;
	}
}

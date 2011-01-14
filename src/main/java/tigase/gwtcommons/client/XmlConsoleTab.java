package tigase.gwtcommons.client;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector.BoshConnectorEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.gwt.client.xml.GwtElement;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
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
import com.google.gwt.xml.client.XMLParser;

public class XmlConsoleTab extends TabItem {

	private boolean hasUnread = false;

	private final MessagePanel messagePanel = new MessagePanel();

	private boolean selected;

	private final Button sendButton;

	private final ContentPanel southPanel = new ContentPanel(new FitLayout());

	private final TextArea text = new TextArea();

	public XmlConsoleTab() {
		setText("XML Console");
		setClosable(true);
		setLayout(new BorderLayout());
		messagePanel.setNicknameSeparator("");

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0));

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 100);
		southData.setSplit(true);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setMargins(new Margins(5, 0, 0, 0));

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
		this.sendButton = new Button("Send", new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				sendMessage();
			}
		});

		ToolBar tb = new ToolBar();
		tb.add(new FillToolItem());
		tb.add(this.sendButton);
		southPanel.setBottomComponent(tb);

		add(messagePanel, centerData);
		add(southPanel, southData);
	}

	public void init() {
		XmppService.get().getConnector().addListener(Connector.StreamTerminated,
				new tigase.jaxmpp.core.client.observer.Listener<Connector.ConnectorEvent>() {

					public void handleEvent(ConnectorEvent be) {
						try {
							if (be instanceof BoshConnectorEvent) {
								String x = ((BoshConnectorEvent) be).getBody() != null ? ((BoshConnectorEvent) be).getBody().getAsString()
										: "";
								messagePanel.addHisMessage("<<", x);
							}
						} catch (Exception e) {
							messagePanel.addErrorMessage(e.getMessage());
						}

					}
				});
		XmppService.get().getConnector().addListener(Connector.BodyReceived,
				new tigase.jaxmpp.core.client.observer.Listener<Connector.ConnectorEvent>() {

					public void handleEvent(ConnectorEvent be) {
						try {
							if (be instanceof BoshConnectorEvent) {
								String x = ((BoshConnectorEvent) be).getBody() != null ? ((BoshConnectorEvent) be).getBody().getAsString()
										: "";
								messagePanel.addHisMessage("<<", x);
							}
						} catch (Exception e) {
							messagePanel.addErrorMessage(e.getMessage());
						}

					}
				});
		XmppService.get().getConnector().addListener(Connector.StanzaSending,
				new tigase.jaxmpp.core.client.observer.Listener<Connector.ConnectorEvent>() {

					public void handleEvent(ConnectorEvent be) {
						try {
							if (be instanceof BoshConnectorEvent) {
								String x = ((BoshConnectorEvent) be).getBody() != null ? ((BoshConnectorEvent) be).getBody().getAsString()
										: "";
								messagePanel.addMineMessage(">>", x);
							}
						} catch (Exception e) {
							messagePanel.addErrorMessage(e.getMessage());
						}

					}
				});
	}

	private void markUnread(boolean b) {
		if (this.hasUnread != b && b) {
			setText("* XML Console");
		} else if (this.hasUnread != b && !b) {
			setText("XML Console");
		}
		this.hasUnread = b;
	}

	public void onDeselect() {
		this.selected = false;
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
		try {
			GwtElement x = new GwtElement(XMLParser.parse(v).getDocumentElement());
			XmppService.get().send(Stanza.create(x));
		} catch (Exception e) {
			messagePanel.addErrorMessage(e.getMessage());
		}
	}
}

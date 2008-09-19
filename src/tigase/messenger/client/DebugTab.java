package tigase.messenger.client;

import java.util.Date;
import java.util.List;

import tigase.xmpp4gwt.client.Bosh2Connector;
import tigase.xmpp4gwt.client.BoshConnection;
import tigase.xmpp4gwt.client.Connector;
import tigase.xmpp4gwt.client.ConnectorListener;
import tigase.xmpp4gwt.client.TextUtils;
import tigase.xmpp4gwt.client.packet.Packet;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class DebugTab extends TabItem implements ConnectorListener {

	private ContentPanel center = new ContentPanel();

	private final Html chat = new Html();

	private DateTimeFormat dtf = DateTimeFormat.getFormat("HH:mm:ss");

	private final TextArea message = new TextArea();

	public DebugTab(Bosh2Connector connector) {
		this.connector = connector;
		setText("Debug window");
		setIconStyle("icon-tabs");
		setClosable(false);

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

	private final Bosh2Connector connector;

	private void send() {
		final String message = this.message.getText();
		this.message.setText("");
		connector.sendStanza(message);
	}

	enum Type {
		in, out
	}

	private void add(String x) {
		String m = this.chat.getHtml();
		m = m + x + "<br/>";
		this.chat.setHtml(m);
		center.setVScrollPosition(this.chat.getBounds(true).width);
	}

	private void add(Type type, String message) {
		String style;
		String n;
		if (type == Type.in) {
			style = "peer";
			n = "IN:";
		} else {
			style = "me";
			n = "OUT:";
		}
		String x = "[" + dtf.format(new Date()) + "]&nbsp;" + n + "&nbsp;<span class='" + style + "'>" + TextUtils.escape(message)
				+ "</span><br/>";
		add(x);
	}

	public void onBodyReceive(Response code, String body) {
		add(Type.in, body);
	}

	public void onBodySend(String body) {
		add(Type.out, body);
	}

	public void onConnect(Connector con) {
	}

	public void onDisconnectByServer(Connector con) {
	}

	public void onError(String message) {
	}

	public void onItemNotFoundError() {
	}

	public void onStanzaReceived(List<? extends Packet> nodes) {
	}

}

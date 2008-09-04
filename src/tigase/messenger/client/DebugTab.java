package tigase.messenger.client;

import java.util.List;

import tigase.xmpp4gwt.client.BoshConnection;
import tigase.xmpp4gwt.client.BoshConnectionListener;
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

public class DebugTab extends TabItem implements BoshConnectionListener {

	private ContentPanel center = new ContentPanel();

	private final Html chat = new Html();

	public DebugTab() {
		setText("Debug window");
		setIconStyle("icon-tabs");
		setClosable(false);

		setLayout(new BorderLayout());

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(5, 0, 0, 0));

		center.setHeaderVisible(false);
		center.add(this.chat);
		center.setScrollMode(Scroll.AUTO);

		add(center, centerData);
	}

	public void onBodyReceive(Response code, String body) {
		add(Type.in, body);
	}

	public void onBodySend(String body) {
		add(Type.out, body);
	}

	public void onConnect(BoshConnection con) {
	}

	public void onDisconnectByServer(BoshConnection con) {
	}

	public void onError(String message) {
	}

	public void onItemNotFoundError() {
	}

	public void onStanzaReceived(List<? extends Packet> nodes) {
	}

	private void add(String x) {
		String m = this.chat.getHtml();
		m = m + x + "<br/>";
		this.chat.setHtml(m);
		center.setVScrollPosition(this.chat.getBounds(true).width);
	}

	enum Type {
		in, out
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
		String x = n + "&nbsp;<span class='" + style + "'>" + TextUtils.escape(message) + "</span><br/>";
		System.out.println(x);
		add(x);
	}

}

package tigase.gwtcommons.client;

import java.util.Date;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class MessagePanel extends ContentPanel {

	private static String linkhtml(String body) {
		body = body == null ? body : body.replaceAll("([^>/\";]|^)(www\\.[^ ]+)",
				"$1<a href=\"http://$2\" target=\"_blank\">$2</a>");
		body = body == null ? body : body.replaceAll("([^\">;]|^)(http://[^ ]+)", "$1<a href=\"$2\" target=\"_blank\">$2</a>");
		return body;
	}

	private final DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd");

	private String lastMessageTimestamp;

	private String nicknameSeparator = ":";

	private final DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm:ss");

	private final VerticalPanel vp = new VerticalPanel();

	public MessagePanel() {
		addStyleName("messagePanel");
		setScrollMode(Scroll.AUTO);
		vp.setScrollMode(Scroll.NONE);
		layout();
		vp.setBorders(false);
		vp.addStyleName("msgs");
		setHeaderVisible(false);
		setBorders(false);

		setBodyBorder(false);

		add(vp);
	}

	public void addAppMessage(Date date, String message) {
		String msg = buildSafeHtml(message);

		String t = "<div  class='line appMessage ts" + date.getTime() + "'>";
		t += "<span class='timestamp'>[" + timeFormat.format(date) + "]</span>&nbsp;";
		t += "<span class='msg'>" + msg + "</span>";
		t += "</div>";

		addLine(date, t);
	}

	public void addAppMessage(String message) {
		addAppMessage(new Date(), message);
	}

	public void addErrorMessage(Date date, String message) {
		String msg = buildSafeHtml(message);

		String t = "<div class='line errorMessage ts" + date.getTime() + "'>";
		t += "<span class='timestamp'>[" + timeFormat.format(date) + "]</span>&nbsp;";
		t += "<span class='msg'>" + msg + "</span>";
		t += "</div>";

		addLine(date, t);
	}

	public void addErrorMessage(String message) {
		addErrorMessage(new Date(), message);
	}

	public void addHisMessage(Date date, String nickname, String message) {
		addHisMessage(0, date, nickname, message);
	}

	public void addHisMessage(int number, Date date, String nickname, String message) {
		String msg = buildSafeHtml(message);

		String t;
		if (msg.startsWith("/me ")) {
			msg = msg.substring(4);
			t = "<div class='line hisMessage a" + number + " ts" + date.getTime() + "'>";
			t += "<span class='timestamp'>[" + timeFormat.format(date) + "]</span>&nbsp;";
			t += "<span class='nick'>*" + nickname + "&nbsp;" + msg + "</span>";
			t += "</div>";
		} else {
			t = "<div class='line hisMessage a" + " ts" + date.getTime() + "'>";
			t += "<span class='timestamp'>[" + timeFormat.format(date) + "]</span>&nbsp;";
			t += "<span class='nick'>" + nickname + nicknameSeparator + "&nbsp;</span>";
			t += "<span class='msg'>" + msg + "</span>";
			t += "</div>";
		}
		addLine(date, t);
	}

	public void addHisMessage(int number, String nickname, String message) {
		addHisMessage(number, new Date(), nickname, message);
	}

	public void addHisMessage(String nickname, String message) {
		addHisMessage(0, new Date(), nickname, message);
	}

	private void addLine(Date date, String line) {
		String fd = dateFormat.format(date);
		if (this.lastMessageTimestamp == null || !this.lastMessageTimestamp.equals(fd)) {
			Html h = new Html("<div class='line newDay'>*** " + fd + "</div>");
			// insertLine(dateFormat.parse(fd), h);
		}
		Html h = new Html(line);
		insertLine(date, h);

		this.lastMessageTimestamp = fd;
	}

	public void addMineMessage(Date date, String nickname, String message) {
		if (message == null)
			return;
		String msg = buildSafeHtml(message);

		String t;
		if (msg.startsWith("/me ")) {
			msg = msg.substring(4);
			t = "<div class='line mineMessage ts" + date.getTime() + "'>";
			t += "<span class='timestamp'>[" + timeFormat.format(date) + "]</span>&nbsp;";
			t += "<span class='nick'>*" + nickname + "&nbsp;" + msg + "</span>";
			t += "</div>";
		} else {
			t = "<div class='line mineMessage ts" + date.getTime() + "'>";
			t += "<span class='timestamp'>[" + timeFormat.format(date) + "]</span>&nbsp;";
			t += "<span class='nick'>" + nickname + nicknameSeparator + "&nbsp;</span>";
			t += "<span class='msg'>" + msg + "</span>";
			t += "</div>";
		}
		addLine(date, t);

	}

	public void addMineMessage(String nickname, String message) {
		addMineMessage(new Date(), nickname, message);
	}

	private String buildSafeHtml(String message) {
		String msg = SafeHtmlUtils.fromString(message).asString();
		msg = linkhtml(msg);
		return msg;
	}

	public String getNicknameSeparator() {
		return nicknameSeparator;
	}

	private void insertLine(Date date, Html h) {
		h.setData("TIMESTAMP", date);

		boolean added = false;
		Component lastItem = vp.getItemCount() > 0 ? vp.getItem(vp.getItemCount() - 1) : null;
		lastItem = lastItem == null || lastItem.getData("TIMESTAMP") == null ? null : lastItem;

		if (lastItem != null && ((Date) lastItem.getData("TIMESTAMP")).getTime() <= date.getTime()) {
			added = true;
			vp.add(h);
		} else if (vp.getItemCount() == 0) {
			added = true;
			vp.add(h);
		} else
			for (int i = vp.getItemCount() - 1; i >= 0; i--) {
				Component item = vp.getItem(i);
				Date d = item.getData("TIMESTAMP");
				if (d != null && d.getTime() <= date.getTime()) {
					added = true;
					vp.insert(h, i + 1);
					break;
				} else if (i == 0) {
					added = true;
					vp.insert(h, 0);
					break;
				}
			}

		if (!added)
			vp.add(h);

		vp.layout();
		if (isRendered()) {
			int w1 = vp.getHeight();
			setVScrollPosition(w1);
		}

	}

	public void setNicknameSeparator(String nicknameSeparator) {
		this.nicknameSeparator = nicknameSeparator;
	}
}

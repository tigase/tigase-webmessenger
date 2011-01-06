package tigase.messenger.client;

import java.util.Date;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class MessagePanel extends ContentPanel {

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
		String msg = SafeHtmlUtils.fromString(message).asString();

		String t = "<div class='line appMessage'>";
		t += "<span class='timestamp'>[" + timeFormat.format(date) + "]</span>&nbsp;";
		t += "<span class='msg'>" + msg + "</span>";
		t += "</div>";

		addLine(date, t);
	}

	public void addAppMessage(String message) {
		addAppMessage(new Date(), message);
	}

	public void addErrorMessage(Date date, String message) {
		String msg = SafeHtmlUtils.fromString(message).asString();

		String t = "<div class='line errorMessage'>";
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
		String msg = SafeHtmlUtils.fromString(message).asString();

		String t = "<div class='line hisMessage a" + number + "'>";
		t += "<span class='timestamp'>[" + timeFormat.format(date) + "]</span>&nbsp;";
		t += "<span class='nick'>" + nickname + nicknameSeparator + "&nbsp;</span>";
		t += "<span class='msg'>" + msg + "</span>";
		t += "</div>";

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
			Html h = new Html("<div class='line newDay'>" + fd + "</div>");
			vp.add(h);
		}
		Html h = new Html(line);
		vp.add(h);

		vp.layout();
		if (isRendered()) {
			int w1 = vp.getHeight();
			setVScrollPosition(w1);
		}
		this.lastMessageTimestamp = fd;
	}

	public void addMineMessage(Date date, String nickname, String message) {
		if (message == null)
			return;
		String msg = SafeHtmlUtils.fromString(message).asString();

		String t = "<div class='line mineMessage'>";
		t += "<span class='timestamp'>[" + timeFormat.format(date) + "]</span>&nbsp;";
		t += "<span class='nick'>" + nickname + nicknameSeparator + "&nbsp;</span>";
		t += "<span class='msg'>" + msg + "</span>";
		t += "</div>";

		addLine(date, t);

	}

	public void addMineMessage(String nickname, String message) {
		addMineMessage(new Date(), nickname, message);
	}

	public String getNicknameSeparator() {
		return nicknameSeparator;
	}

	public void setNicknameSeparator(String nicknameSeparator) {
		this.nicknameSeparator = nicknameSeparator;
	}
}

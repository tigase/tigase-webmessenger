package tigase.messenger.client;

import java.util.List;

import tigase.xmpp4gwt.client.packet.Packet;
import tigase.xmpp4gwt.client.xmpp.ErrorCondition;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.ui.Label;

public class ErrorDialog extends Dialog {

	public static String getErrorText(Packet errorPacket) {
		if (errorPacket != null) {
			Packet p = errorPacket.getFirstChild("text");
			return p == null ? null : p.getCData();
		}
		return null;
	}

	public static ErrorCondition getErrorCondition(Packet error) {
		List<? extends Packet> kids = error.getChildren();
		if (kids != null)
			for (Packet k : kids) {
				String xmlns = k.getAtribute("xmlns");
				if (xmlns != null && xmlns.equals("urn:ietf:params:xml:ns:xmpp-stanzas")) {
					try {
						return ErrorCondition.valueOf(k.getName().replace('-', '_'));
					} catch (Exception e) {
						return ErrorCondition.undefined_condition;
					}
				}
			}
		return ErrorCondition.undefined_condition;
	}

	public ErrorDialog(final String title, final String message, final Packet stanza) {
		super();
		setButtons(OK);
		setHideOnButtonClick(true);
		setHeading(title);
		final Packet error = stanza.getName().equals("error") ? stanza : stanza.getFirstChild("error");

		ErrorCondition condition = getErrorCondition(error);
		String text = getErrorText(error);

		Label reason = new Label("Reason: " + condition.name());

		Label txt = new Label(text != null ? text : message);

		add(reason);
		add(txt);

		// title: ERROR
		// "Unable to join groupchat"
		// reason: conflict
		// Access cannot be granted because an existing resource or session
		// exists with the same name or address
	}

}

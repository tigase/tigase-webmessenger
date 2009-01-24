package tigase.messenger.client;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.stanzas.Presence;
import tigase.jaxmpp.core.client.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.stanzas.Presence.Type;

import com.google.gwt.i18n.client.Dictionary;

public class Config {

	public static void main(String[] args) {
		Presence p = new Presence(Type.available);
		p.setShow(Show.notSpecified);

		System.out.println(p);
	}

	private final Dictionary c;

	public Config() {
		Dictionary tmp;
		try {
			tmp = Dictionary.getDictionary("Config");
		} catch (MissingResourceException e) {
			tmp = null;
		}
		c = tmp;
	}

	public String getDefaultHostname() {
		if (c != null && c.keySet().contains("hostname"))
			return c.get("hostname");
		else
			return null;
	}

	public JID getDefaultMucRoomName() {
		if (c != null && c.keySet().contains("roomname"))
			return JID.fromString(c.get("roomname"));
		else
			return null;
	}

	public String getDefaultResource() {
		if (c != null && c.keySet().contains("resource"))
			return c.get("resource");
		else
			return null;
	}

	public JID[] getDirectPresenceAdressees() {
		if (c != null && c.keySet().contains("directPresences")) {
			String x = c.get("directPresences");
			String[] source = x.split(",");
			List<JID> result = new ArrayList<JID>();
			if (source != null) {
				for (int i = 0; i < source.length; i++) {
					JID jid = JID.fromString(source[i]);
					if (jid.isValid())
						result.add(jid);
				}

			}
			return result.size() == 0 ? null : result.toArray(new JID[] {});
		} else
			return null;
	}

	public String getHTTPBase() {
		if (c != null && c.keySet().contains("httpBase"))
			return c.get("httpBase");
		else
			return "/bosh";
	}

	public JID getJid() {
		if (c != null && c.keySet().contains("jid"))
			return JID.fromString(c.get("jid"));
		else
			return null;
	}

	public String getPassword() {
		if (c != null && c.keySet().contains("password"))
			return c.get("password");
		else
			return null;
	}

	public boolean isDebugEnabled() {
		boolean result = c != null && c.keySet().contains("debug") && "true".equalsIgnoreCase(c.get("debug"));
		return result;
	}

	public boolean isDefaultAnonymous() {
		boolean result = c != null && c.keySet().contains("anonymous") && "true".equalsIgnoreCase(c.get("anonymous"));
		return result;
	}
}

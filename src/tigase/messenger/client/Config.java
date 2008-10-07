package tigase.messenger.client;

import java.util.ArrayList;
import java.util.List;

import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.stanzas.Presence;
import tigase.xmpp4gwt.client.stanzas.Presence.Show;
import tigase.xmpp4gwt.client.stanzas.Presence.Type;

import com.google.gwt.i18n.client.Dictionary;

public class Config {

	private final Dictionary c = Dictionary.getDictionary("Config");

	public boolean isDefaultAnonymous() {
		boolean result = c.keySet().contains("anonymous") && "true".equalsIgnoreCase(c.get("anonymous"));
		return result;
	}

	public JID[] getDirectPresenceAdressees() {
		if (c.keySet().contains("directPresences")) {
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

	public static void main(String[] args) {
	Presence p = new Presence(Type.available);
	p.setShow(Show.notSpecified);
	
	System.out.println(p);
	}

	public String getDefaultHostname() {
		if (c.keySet().contains("hostname"))
			return c.get("hostname");
		else
			return null;
	}

	public String getDefaultResource() {
		if (c.keySet().contains("resource"))
			return c.get("resource");
		else
			return null;
	}

	public String getHTTPBase() {
		return c.get("httpBase");
	}

	public JID getJid() {
		if (c.keySet().contains("jid"))
			return JID.fromString(c.get("jid"));
		else
			return null;
	}

	public String getPassword() {
		if (c.keySet().contains("password"))
			return c.get("password");
		else
			return null;
	}

	public boolean isDebugEnabled() {
		boolean result = c.keySet().contains("debug") && "true".equalsIgnoreCase(c.get("debug"));
		return result;
	}
}

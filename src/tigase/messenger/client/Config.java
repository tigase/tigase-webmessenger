package tigase.messenger.client;

import tigase.xmpp4gwt.client.JID;

import com.google.gwt.i18n.client.Dictionary;

public class Config {

	private final Dictionary c = Dictionary.getDictionary("Config");

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

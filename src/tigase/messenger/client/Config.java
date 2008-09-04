package tigase.messenger.client;

import com.google.gwt.i18n.client.Dictionary;

public class Config {

	private final Dictionary c = Dictionary.getDictionary("Config");

	public String getDefaultResource() {
		return c.get("resource");
	}

	public String getHTTPBase() {
		return c.get("httpBase");
	}

	public boolean isDebugEnabled() {
		boolean result = c.keySet().contains("debug") && "true".equalsIgnoreCase(c.get("debug"));
		return result;
	}
}

package tigase.gwtcommons.client;

import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.gwt.client.Jaxmpp;

import com.google.gwt.i18n.client.Dictionary;

public class XmppService {

	private static XmppService service;

	public static Dictionary config() {
		if (service == null)
			service = new XmppService();
		return service.config;
	}

	public static Jaxmpp get() {
		if (service == null)
			service = new XmppService();
		return service.jaxmpp;
	}

	private Dictionary config;

	private Jaxmpp jaxmpp;

	public XmppService() {
		this.config = Dictionary.getDictionary("Config");
		this.jaxmpp = new Jaxmpp();

		this.jaxmpp.getProperties().setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY, this.config.get("httpBase"));
	}

}

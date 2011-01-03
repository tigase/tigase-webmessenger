package tigase.messenger.client;

import tigase.jaxmpp.gwt.client.DefaultLoggerSpi;
import tigase.jaxmpp.gwt.client.Jaxmpp;

public class XmppService {

	private static XmppService service;

	public static Jaxmpp get() {
		if (service == null)
			service = new XmppService();
		return service.jaxmpp;
	}

	private Jaxmpp jaxmpp;

	public XmppService() {
		this.jaxmpp = new Jaxmpp(new DefaultLoggerSpi());

	}

}

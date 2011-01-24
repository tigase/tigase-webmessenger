package tigase.gwtcommons.client.muc;

import tigase.gwtcommons.client.Tab;
import tigase.gwtcommons.client.XmppService;
import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.logger.LoggerFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.MucEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;

public class MucTab extends Tab {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final MucPanel mucPanel;

	private Room room;

	public MucTab(Room room) {
		this.room = room;
		log.fine("MucmTab " + room.getRoomJid() + " is created");
		setText("MUC room ");
		setClosable(true);
		setLayout(new FitLayout());
		this.mucPanel = new MucPanel(room);
		add(this.mucPanel);
	}

	public Room getRoom() {
		return room;
	}

	protected void onNewRoomCreated(MucEvent be) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.setTo(JID.jidInstance(be.getRoom().getRoomJid()));
		Element query = new DefaultElement("query", null, "http://jabber.org/protocol/muc#owner");
		iq.addChild(query);
		Element x = new DefaultElement("x", null, "jabber:x:data");
		x.setAttribute("type", "submit");
		query.addChild(x);

		GWT.log("Enabling room");
		XmppService.get().send(iq, new AsyncCallback() {

			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				MessageBox.alert("Error", "" + error, null);
			}

			public void onSuccess(Stanza responseStanza) throws XMLException {
			}

			public void onTimeout() throws XMLException {
				MessageBox.alert("Error", "Timeout", null);
			}
		});
	}

	public void process(Message message) {
		this.mucPanel.process(message);
	}

	public void process(MucEvent event) throws XMLException {
		if (event.getType() == MucModule.NewRoomCreated) {
			try {
				onNewRoomCreated(event);
			} catch (JaxmppException e) {
			}
		} else
			this.mucPanel.process(event);
	}

}

package tigase.mucclient.client;

import java.util.Date;

import tigase.gwtcommons.client.muc.MucPanel;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

public class ChatRestoreModule extends AbstractIQModule {

	private final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.name("chat", "urn:xmpp:tmp:archive"));

	private final DateTimeFormat dtf = new DateTimeFormat();

	private MucPanel mucPanel;

	public ChatRestoreModule(SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
	}

	public Criteria getCriteria() {
		return CRIT;
	}

	public String[] getFeatures() {
		return null;
	}

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException, JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	@Override
	protected void processSet(final IQ iq) throws XMPPException, XMLException, JaxmppException {
		if (mucPanel == null)
			return;
		final Element chat = iq.getChildrenNS("chat", "urn:xmpp:tmp:archive");
		final long start = dtf.parse(chat.getAttribute("start")).getTime();
		System.out.println(" !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + start);
		for (Element item : chat.getChildren()) {
			if ("to".equals(item.getName())) {
				Element body = item.getFirstChild();
				long sec = start + (Long.parseLong(item.getAttribute("secs")) * 1000l);
				Date d = new Date(sec);
				mucPanel.getMessagePanel().addMineMessage(d, mucPanel.getRoom().getNickname(), body.getValue());
			} else if ("from".equals(item.getName())) {
				String nick = JID.jidInstance(chat.getAttribute("with")).getResource();
				if (nick.equals(mucPanel.getRoom().getNickname()))
					continue;
				Element body = item.getFirstChild();
				long sec = start + (Long.parseLong(item.getAttribute("secs")) * 1000l);
				Date d = new Date(sec);
				mucPanel.getMessagePanel().addHisMessage(d, nick, body.getValue());
			}
		}

	}

	/*
	 * <iq reload-counter="4" type="set" packet-counter="3"
	 * xmlns="jabber:client" id="1298701366175"> <chat
	 * with="tigase@muc.tigase.org" xmlns="urn:xmpp:tmp:archive"
	 * start="2011-02-26T07:22:46+0100"> <to secs="0"><body>1</body></to> <to
	 * secs="0"><body>2</body></to> <to secs="0"><body>3</body></to> <to
	 * secs="1"><body>4</body></to> </chat> </iq> <iq reload-counter="4"
	 * type="set" packet-counter="4" xmlns="jabber:client" id="1298701366184">
	 * <chat with="tigase@muc.tigase.org/asd" xmlns="urn:xmpp:tmp:archive"
	 * start="2011-02-26T07:22:46+0100"> <from secs="0"><body>1</body></from>
	 * <from secs="0"><body>2</body></from> <from secs="0"><body>3</body></from>
	 * <from secs="1"><body>4</body></from></chat></iq>
	 */

	public void setMucPanel(MucPanel mucPanel) {
		this.mucPanel = mucPanel;
	}

}

package tigase.gwtcommons.client.muc;

import tigase.gwtcommons.client.Tab;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.MucEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;

import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class MucTab extends Tab {

	private final MucPanel mucPanel;

	private Room room;

	public MucTab(Room room) {
		this.room = room;
		setText("MUC room ");
		setClosable(true);
		setLayout(new FitLayout());
		this.mucPanel = new MucPanel(room);
		add(this.mucPanel);
	}

	public Room getRoom() {
		return room;
	}

	protected void onPresenceReceived(String nickname, Presence presence) throws XMLException {
		this.mucPanel.onPresenceReceived(nickname, presence);
	}

	public void process(Message message) {
		this.mucPanel.process(message);
	}

	public void process(MucEvent event) throws XMLException {
		this.mucPanel.process(event);
	}

}

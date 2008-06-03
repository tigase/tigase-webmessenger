package tigase.messenger.client;

import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.xmpp.message.Message;
import tigase.xmpp4gwt.client.xmpp.presence.PresenceItem;

public interface ChatManager {

	void openChatWith(final JID jid, final boolean focus);

	void process(Message message);

	void reset();

	void updatePresence(PresenceItem presenceItem);

}

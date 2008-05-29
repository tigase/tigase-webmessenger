package tigase.messenger.client;

import tigase.xmpp4gwt.client.JID;

public interface ChatManager {

	void openChatWith(final JID jid, final boolean focus);

}

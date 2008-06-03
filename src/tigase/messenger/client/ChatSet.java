package tigase.messenger.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import tigase.messenger.client.tabbed.ChatTab;
import tigase.xmpp4gwt.client.JID;

public class ChatSet<T> {

	private static class Envelope<T> {
		JID jid;
		String threadId;
		T data;
	}

	private final Set<Envelope<T>> chats = new HashSet<Envelope<T>>();

	public void addChatData(JID jid, String threadId, T data) {
		Envelope<T> env = new Envelope<T>();
		env.data = data;
		env.jid = jid;
		env.threadId = threadId;

		chats.add(env);
	}

	public void clear() {
		this.chats.clear();
	}

	public T getChatData(final JID jid, final String threadId) {
		for (Envelope<T> env : this.chats) {
			if (!env.jid.getBareJID().equals(jid.getBareJID())) continue;
			if (threadId != null && !threadId.equals(env.threadId)) continue;
			if (env.threadId != null && threadId != null && threadId.equals(env.threadId)) {
				return env.data;
			} else if (env.jid.equals(jid)) {
				return env.data;
			} else if (env.jid.getResource() == null) {
				return env.data;
			}
		}
		return null;
	}

	public void removeChatData(Object data) {
		Iterator<Envelope<T>> it = this.chats.iterator();
		while (it.hasNext()) {
			Envelope<T> element = it.next();
			if (element.data == data) {
				it.remove();
				System.out.println("ChatData removed.");
			}
		}

	}

	public List<T> getChatList(JID jid) {
		ArrayList<T> result = new ArrayList<T>();

		for (Envelope<T> env : this.chats) {
			if (env.jid.equals(jid)) {
				result.add(env.data);
			} else if (jid.getResource() == null && jid.equals(env.jid.getBareJID())) {
				result.add(env.data);
			}
		}

		return result;
	}
}

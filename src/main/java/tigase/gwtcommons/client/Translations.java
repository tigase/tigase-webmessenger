package tigase.gwtcommons.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface Translations extends Messages {

	public static final Translations instance = GWT.create(Translations.class);

	@Key("client.chat.chatWith")
	String chatWithTabName(String roomName);

	@Key("client.dialog.contact.title")
	String clientContactEditDialog();

	@Key("client.dialog.contact.groups")
	String clientContactEditDialogGroups();

	@Key("client.dialog.contact.jid")
	String clientContactEditDialogJid();

	@Key("client.dialog.contact.name")
	String clientContactEditDialogName();

	@Key("client.login.dialog.anonymous")
	String clientLoginAnonymous();

	@Key("client.login.dialog.anonymous.no")
	String clientLoginAnonymousNo();

	@Key("client.login.dialog.anonymous.yes")
	String clientLoginAnonymousYes();

	@Key("client.login.dialog.title")
	String clientLoginDialogTitle();

	@Key("client.login.dialog.jid")
	String clientLoginJID();

	@Key("client.menu.action.addContact")
	String clientMenuActionAddContact();

	@Key("client.message.isAvailable")
	String clientMessageIsAvailable(String string);

	@Key("client.message.isNow")
	String clientMessageIsNow(String string, String string2);

	@Key("client.message.isUnavailable")
	String clientMessageIsUnavailable(String string);

	@Key("client.message.loggedInAs")
	String clientMessageLoggedInAs(String string);

	@Key("client.customStatus.dialog.status")
	String clientStatusCustomDialogStatus();

	@Key("client.customStatus.dialog.text")
	String clientStatusCustomDialogText();

	@Key("client.customStatus.dialog.title")
	String clientStatusCustomDialogTitle();

	@Key("client.status.tab.title")
	String clientStatusTabTitle();

	@Key("client.error")
	String error();

	@Key("client.errors.badCredentials")
	String errorBadCredentials();

	@Key("client.errors.connectionError")
	String errorConnectionError();

	@Key("client.error.timeout")
	String errorTimeout();

	@Key("client.chat.selfnick")
	String me();

	@Key("client.menu.presence")
	String menuPresence();

	@Key("client.menu.presence.away")
	String menuPresenceAway();

	@Key("client.menu.presence.chat")
	String menuPresenceChat();

	@Key("client.menu.presence.custom")
	String menuPresenceCustom();

	@Key("client.menu.presence.dnd")
	String menuPresenceDND();

	@Key("client.menu.presence.logout")
	String menuPresenceLogout();

	@Key("client.menu.presence.online")
	String menuPresenceOnline();

	@Key("client.menu.presence.xa")
	String menuPresenceXA();

	@Key("mucclient.room.alreadyHere")
	String mucAlreadyHere(String t);

	@Key("client.muc.error.accessDenied")
	String mucErrorAccessDenied();

	@Key("client.muc.error.conflict")
	String mucErrorConflict();

	@Key("client.muc.error.unknown")
	String mucErrorUnknown();

	@Key("client.muc.room.joining")
	String mucJoining(String string);

	@Key("client.muc.message.topic.change")
	String mucMessageHasSetTopic(String nick, String subject);

	@Key("client.muc.message.m.100")
	String mucMessageX100();

	@Key("client.muc.message.p.100110")
	String mucMessageX100110();

	@Key("client.muc.message.m.101")
	String mucMessageX101();

	@Key("client.muc.message.m.102")
	String mucMessageX102();

	@Key("client.muc.message.m.103")
	String mucMessageX103();

	@Key("client.muc.message.m.104")
	String mucMessageX104();

	@Key("client.muc.message.p.110170")
	String mucMessageX110170();

	@Key("client.muc.message.p.110201")
	String mucMessageX110201();

	@Key("client.muc.message.p.110210")
	String mucMessageX110210();

	@Key("client.muc.message.m.170")
	String mucMessageX170();

	@Key("client.muc.message.m.171")
	String mucMessageX171();

	@Key("client.muc.message.m.172")
	String mucMessageX172();

	@Key("client.muc.message.m.173")
	String mucMessageX173();

	@Key("client.muc.message.m.174")
	String mucMessageX174();

	@Key("client.muc.message.p.301")
	String mucMessageX301(String nickname);

	@Key("client.muc.message.p.307")
	String mucMessageX307(String nickname);

	@Key("client.muc.message.p.join")
	String mucMessageXJoin(String nickname);

	@Key("client.muc.message.p.joinAs")
	String mucMessageXJoinAs(String nickname, String role);

	@Key("client.muc.message.p.leaved")
	String mucMessageXLeaved(String nickname);

	@Key("client.muc.message.p.rename")
	String mucMessageXRename(String oldNickname, String newNickname);

	@Key("client.muc.roomName")
	String mucRoomName();

	@Key("client.muc.server")
	String mucServer();

	@Key("mucclient.room.welcome")
	String mucWelcome(String roomJid);

	@Key("client.nickname")
	String nickname();

	@Key("client.password")
	String password();

	@Key("client.button.send")
	String sendButton();

	@Key("client.state.authenticated")
	String stateAuthenticated();

	@Key("client.state.authenticating")
	String stateAuthenticating();

	@Key("client.state.connected")
	String stateConnected();

	@Key("client.state.connecting")
	String stateConnecting();

	@Key("client.state.disconnected")
	String stateDisconnected();

	@Key("client.state.disconnecting")
	String stateDisconnecting();

	@Key("client.muc.isnow")
	String xmppMucIsNow();

	@Key("xmpp.presence.away")
	String xmppPresenceAway();

	@Key("xmpp.presence.chat")
	String xmppPresenceChat();

	@Key("xmpp.presence.dnd")
	String xmppPresenceDND();

	@Key("xmpp.presence.online")
	String xmppPresenceOnline();

	@Key("xmpp.presence.xa")
	String xmppPresenceXA();

}

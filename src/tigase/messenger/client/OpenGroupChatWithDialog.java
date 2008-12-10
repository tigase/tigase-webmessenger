package tigase.messenger.client;

import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.xmpp.message.ChatManager;
import tigase.xmpp4gwt.client.xmpp.xeps.muc.MultiUserChatPlugin;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.user.client.Random;

public class OpenGroupChatWithDialog extends Dialog {

	private final FormPanel form = new FormPanel();

	private final TextField<String> jidField = new TextField<String>();

	private final TextField<String> nicknameField = new TextField<String>();

	private final TextField<String> passwordField = new TextField<String>();

	private final MultiUserChatPlugin multiUserChatPlugin;

	public OpenGroupChatWithDialog(MultiUserChatPlugin multiUserChatPlugin) {
		super();
		this.multiUserChatPlugin = multiUserChatPlugin;
		setButtons(Dialog.OKCANCEL);
		setHeading("Join to groupchat");
		setHideOnButtonClick(false);

		setWidth(370);
		setAutoHeight(true);

		form.setHeaderVisible(false);
		form.setFrame(false);
		form.setBorders(false);
		form.setBodyBorder(false);
		form.setDeferHeight(true);

		add(form);

		jidField.setFieldLabel("Room ID");
		jidField.setEmptyText("Enter room name here");
		jidField.setAllowBlank(false);
		jidField.setValidator(new Validator<String, Field<String>>() {
			public String validate(Field<String> field, String value) {
				JID jid = JID.fromString(value);
				if (!jid.isValid() || jid.getNode() == null || jid.getResource() != null) {
					return "Please enter a valid room name";
				} else
					return null;
			}
		});

		nicknameField.setFieldLabel("Nickname");
		nicknameField.setAllowBlank(false);
		nicknameField.setEmptyText("Enter Your nickname here");

		passwordField.setPassword(true);
		passwordField.setFieldLabel("Password");
		passwordField.setAllowBlank(true);
		passwordField.setEmptyText("Enter room password here");

		form.add(jidField);
		form.add(nicknameField);
		form.add(passwordField);

		JID defRoomName = Messenger.config().getDefaultMucRoomName();
		if (defRoomName != null) {
			jidField.setValue(defRoomName.toStringBare());
		}

		nicknameField.setValue(Messenger.instance().getNickname());
	}

	@Override
	protected void onButtonPressed(Button button) {
		if (button.getItemId().equals(Dialog.OK)) {
			if (form.isValid()) {
				final JID jid = JID.fromString(jidField.getValue());
				close();
				multiUserChatPlugin.createGroupChat(jid.getBareJID(), nicknameField.getValue(), passwordField.getValue());
			}
		} else
			close();
	}

}

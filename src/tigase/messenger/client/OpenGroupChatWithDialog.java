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

	private final MultiUserChatPlugin multiUserChatPlugin;

	public OpenGroupChatWithDialog(MultiUserChatPlugin multiUserChatPlugin) {
		super();
		this.multiUserChatPlugin = multiUserChatPlugin;
		setButtons(Dialog.OKCANCEL);
		setHeading("Open chat with");
		setHideOnButtonClick(false);

		setWidth(370);
		setAutoHeight(true);

		form.setHeaderVisible(false);
		form.setFrame(false);
		form.setBorders(false);
		form.setBodyBorder(false);
		form.setDeferHeight(true);

		add(form);

		jidField.setAllowBlank(false);
		jidField.setValidator(new Validator<String, Field<String>>() {

			public String validate(Field<String> field, String value) {
				JID jid = JID.fromString(value);
				if (!jid.isValid() || jid.getNode() == null) {
					return "Please enter a valid Room ID";
				} else
					return null;
			}
		});

		jidField.setFieldLabel("Room ID");
		jidField.setEmptyText("Enter RoomID: room@host/YOURNICKNAME");
		form.add(jidField);

		jidField.setValue("jabber@confrence.jabber.org/"
				+ (Messenger.instance().getNickname() == null ? ("Guest" + (Math.round(Math.random() * 1000)))
						: Messenger.instance().getNickname()));
	}

	@Override
	protected void onButtonPressed(Button button) {
		if (button.getItemId().equals(Dialog.OK)) {
			if (form.isValid()) {
				final JID jid = JID.fromString(jidField.getValue());
				close();
				multiUserChatPlugin.createGroupChat(jid.getBareJID(), jid.getResource());
			}
		} else
			close();
	}

}

package tigase.messenger.client;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xmpp.message.ChatManager;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;

public class OpenChatWithDialog extends Dialog {

	private final ChatManager<ChatTab> chatManager;

	private final FormPanel form = new FormPanel();

	private final TextField<String> jidField = new TextField<String>();

	public OpenChatWithDialog(ChatManager<ChatTab> chatManager) {
		super();
		this.chatManager = chatManager;
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
		jidField.setValidator(new Validator() {

			public String validate(Field<?> field, String value) {
				JID jid = JID.fromString(value);
				if (!jid.isValid() || jid.getNode() == null) {
					return "Please enter a valid Jabber ID";
				} else
					return null;
			}
		});

		jidField.setFieldLabel("Jabber ID");
		form.add(jidField);
	}

	@Override
	protected void onButtonPressed(Button button) {
		if (button.getItemId().equals(Dialog.OK)) {
			if (form.isValid()) {
				final JID jid = JID.fromString(jidField.getValue());
				close();
				chatManager.startChat(jid);
			}
		} else
			close();
	}

}

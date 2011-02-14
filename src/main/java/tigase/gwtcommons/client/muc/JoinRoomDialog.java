package tigase.gwtcommons.client.muc;

import tigase.gwtcommons.client.Translations;
import tigase.gwtcommons.client.XmppService;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class JoinRoomDialog extends Dialog {

	private final FormPanel form = new FormPanel();

	private final TextField<String> nickname = new TextField<String>();

	private final TextField<String> password = new TextField<String>();

	private final TextField<String> roomName = new TextField<String>();

	private final TextField<String> server = new TextField<String>();

	public JoinRoomDialog() {
		setButtons(OKCANCEL);
		setLayout(new FitLayout());

		form.setHeaderVisible(false);
		form.setAutoHeight(true);

		roomName.setFieldLabel(Translations.instance.mucRoomName());
		roomName.setAllowBlank(false);
		roomName.setValue("tigase");
		form.add(roomName);

		server.setFieldLabel(Translations.instance.mucServer());
		server.setAllowBlank(false);
		server.setValue("muc.tigase.org");
		form.add(server);

		nickname.setFieldLabel(Translations.instance.nickname());
		nickname.setAllowBlank(false);
		if (XmppService.get().getSessionObject().getProperty(SessionObject.NICKNAME) == null
				|| ((String) XmppService.get().getSessionObject().getProperty(SessionObject.NICKNAME)).length() == 0) {
			nickname.setValue(((JID) XmppService.get().getSessionObject().getProperty(ResourceBinderModule.BINDED_RESOURCE_JID)).getLocalpart());
		} else
			nickname.setValue((String) XmppService.get().getSessionObject().getProperty(SessionObject.NICKNAME));
		form.add(nickname);

		password.setFieldLabel(Translations.instance.password());
		password.setAllowBlank(true);
		form.add(password);

		add(form);

	}

	@Override
	protected void onButtonPressed(Button button) {
		if (button.getItemId().equals(OK) && form.isValid()) {
			String p = password.getValue();
			onSubmit(roomName.getValue(), server.getValue(), nickname.getValue(), p == null || p.length() == 0 ? null : p);
			hide();
		} else if (button.getItemId().equals(CANCEL)) {
			hide();
		}
	}

	protected void onSubmit(String roomName, String server, String nickname, String password) {
	}
}

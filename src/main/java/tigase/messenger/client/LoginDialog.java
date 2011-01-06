package tigase.messenger.client;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class LoginDialog extends Dialog {

	private final RadioGroup anonymous = new RadioGroup();

	private final FormPanel form = new FormPanel();

	private final TextField<String> jid = new TextField<String>();

	private final TextField<String> nickname = new TextField<String>();

	private final Radio noRadio = new Radio();

	private final TextField<String> password = new TextField<String>();

	private final Radio yesRadio = new Radio();

	public LoginDialog() {
		setLayout(new FitLayout());
		setHeading("Login");
		setButtons(OKCANCEL);
		setWidth(340);
		setModal(true);

		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setBorders(false);
		form.setFrame(false);
		form.setAutoHeight(true);

		yesRadio.setBoxLabel("yes");
		noRadio.setBoxLabel("no");
		anonymous.setFieldLabel("Anonymous");
		anonymous.add(yesRadio);
		anonymous.add(noRadio);
		anonymous.setValue(noRadio);
		anonymous.addListener(Events.Change, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				check();
			}
		});
		form.add(anonymous);

		jid.setFieldLabel("User JID");
		jid.setAllowBlank(false);
		form.add(jid);

		password.setFieldLabel("Password");
		password.setAllowBlank(false);
		password.setPassword(true);
		form.add(password);

		nickname.setFieldLabel("Nickname");
		form.add(nickname);

		add(form);
		check();
	}

	private void check() {
		if (anonymous.getValue() == yesRadio) {
			jid.clearInvalid();
			jid.setEnabled(false);
			password.clearInvalid();
			password.setEnabled(false);
			nickname.clearInvalid();
			nickname.setAllowBlank(false);
		} else {
			jid.clearInvalid();
			jid.setEnabled(true);
			password.clearInvalid();
			password.setEnabled(true);
			nickname.clearInvalid();
			nickname.setAllowBlank(true);
		}

	};

	@Override
	protected void onButtonPressed(Button button) {
		if (button.getItemId().equals(OK)) {
			if (form.isValid()) {

				onSubmit(anonymous.getValue() == yesRadio, jid.getValue(), password.getValue(), nickname.getValue());
			}
		} else if (button.getItemId().equals(CANCEL)) {
			form.clear();
			anonymous.setValue(noRadio);
			onCancel();
		}
	};

	protected void onCancel() {
	}

	protected void onSubmit(boolean anonymous, String userJID, String password, String nickname) {
		try {
			if (anonymous)
				XmppService.get().getProperties().setUserProperty(SessionObject.SERVER_NAME, "tigase.org");
			else
				XmppService.get().getProperties().setUserProperty(SessionObject.SERVER_NAME,
						JID.jidInstance(userJID).getDomain());

			XmppService.get().getProperties().setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL, "/bosh");

			XmppService.get().getProperties().setUserProperty(SessionObject.USER_JID,
					!anonymous ? JID.jidInstance(userJID) : null);
			XmppService.get().getProperties().setUserProperty(SessionObject.PASSWORD, !anonymous ? password : null);

			XmppService.get().getProperties().setUserProperty(SessionObject.PASSWORD, !anonymous ? password : null);

			XmppService.get().getProperties().setUserProperty(SessionObject.NICKNAME, nickname);

			XmppService.get().login();

			hide();
		} catch (Throwable e) {
			MessageBox.alert("Error", e.getMessage(), null);
		}

	}

}

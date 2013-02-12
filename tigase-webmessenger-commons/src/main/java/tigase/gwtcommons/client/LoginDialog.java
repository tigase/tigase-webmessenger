package tigase.gwtcommons.client;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.KeyCodes;

public class LoginDialog extends Dialog {

	public static enum LoginType {
		anonymous,
		both,
		nonAnonymous
	}

	private final RadioGroup anonymous = new RadioGroup();

	private final FormPanel form = new FormPanel();

	private final TextField<String> jid = new TextField<String>();

	private final TextField<String> nickname = new TextField<String>();

	private final Radio noRadio = new Radio();

	private final TextField<String> password = new TextField<String>();

	private final Radio yesRadio = new Radio();

	public LoginDialog() {
		this(LoginType.both);
	}

	public LoginDialog(final LoginType type) {
		setLayout(new FitLayout());
		setHeading(Translations.instance.clientLoginDialogTitle());
		setButtons(OKCANCEL);
		setWidth(240);
		setModal(true);

		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setBorders(false);
		form.setFrame(false);
		form.setAutoHeight(true);

		form.setLabelWidth(64);
		form.setFieldWidth(128);

		yesRadio.setBoxLabel(Translations.instance.clientLoginAnonymousYes());
		noRadio.setBoxLabel(Translations.instance.clientLoginAnonymousNo());
		anonymous.setFieldLabel(Translations.instance.clientLoginAnonymous());
		anonymous.add(yesRadio);
		anonymous.add(noRadio);
		anonymous.setValue(noRadio);
		anonymous.addListener(Events.Change, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				check();
			}
		});
		if (type == LoginType.both)
			form.add(anonymous);

		jid.setFieldLabel(Translations.instance.clientLoginJID());
		jid.setAllowBlank(false);
		if (type == LoginType.both || type == LoginType.nonAnonymous)
			form.add(jid);

		password.setFieldLabel(Translations.instance.password());
		password.setAllowBlank(false);
		password.setPassword(true);
		if (type == LoginType.both || type == LoginType.nonAnonymous)
			form.add(password);

		nickname.setFieldLabel(Translations.instance.nickname());
		form.add(nickname);

		Listener<FieldEvent> keyListener = new Listener<FieldEvent>() {

			public void handleEvent(FieldEvent be) {
				if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
					trySubmit();
				}
			}
		};
		jid.addListener(Events.KeyPress, keyListener);
		password.addListener(Events.KeyPress, keyListener);
		nickname.addListener(Events.KeyPress, keyListener);
		anonymous.addListener(Events.KeyPress, keyListener);

		if (type == LoginType.anonymous) {
			anonymous.setValue(yesRadio);
		} else if (type == LoginType.nonAnonymous) {
			anonymous.setValue(noRadio);
		} else {
			anonymous.setValue(noRadio);
		}

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
			trySubmit();
		} else if (button.getItemId().equals(CANCEL)) {
			form.clear();
			anonymous.setValue(noRadio);
			onCancel();
		}
	}

	protected void onCancel() {
	};

	protected void onSubmit(boolean anonymous, String userJID, String password, String nickname) {
		try {
			if (anonymous) {
				String a;
				try {
					a = XmppService.config().get("anonymousHost");
				} catch (Exception e) {
					a = "tigase.org";
				}
				XmppService.get().getProperties().setUserProperty(SessionObject.SERVER_NAME, a);
			} else
				XmppService.get().getProperties().setUserProperty(SessionObject.SERVER_NAME,
						JID.jidInstance(userJID).getDomain());

			String httpBase;
			try {
				httpBase = XmppService.config().get("httpBase");
			} catch (Exception e) {
				httpBase = "/bosh";
			}

			XmppService.get().getProperties().setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY, httpBase);

			XmppService.get().getProperties().setUserProperty(SessionObject.USER_BARE_JID,
					!anonymous ? BareJID.bareJIDInstance(userJID) : null);
			XmppService.get().getProperties().setUserProperty(SessionObject.PASSWORD, !anonymous ? password : null);

			XmppService.get().getProperties().setUserProperty(SessionObject.PASSWORD, !anonymous ? password : null);

			XmppService.get().getProperties().setUserProperty(SessionObject.NICKNAME, nickname);

			XmppService.get().login();

			hide();
		} catch (Throwable e) {
			MessageBox.alert(Translations.instance.error(), e.getMessage(), null);
		}

	}

	private void trySubmit() {
		if (form.isValid()) {
			onSubmit(anonymous.getValue() == yesRadio, jid.getValue(), password.getValue(), nickname.getValue());
		}
	}

}

package tigase.messenger.client.login;

import java.util.ArrayList;
import java.util.List;

import tigase.messenger.client.Messenger;
import tigase.xmpp4gwt.client.JID;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;

public class LoginDialog extends Dialog {

	private final CheckBox anonumousLogin = new CheckBox();

	private final TextField<String> jid;

	private final List<LoginDialogListener> listeners = new ArrayList<LoginDialogListener>();

	private final TextField<String> password;

	private TextField<String> nickname;

	private Button loginButton;

	public LoginDialog() {
		super();
		setHeading("Login");
		setWidth(380);
		setClosable(false);
		setModal(true);
		setHideOnButtonClick(false);

		final FormPanel panel = new FormPanel();
		// panel.setFieldWidth(230);
		// panel.setLabelWidth(70);
		panel.setHeaderVisible(false);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setButtonAlign(HorizontalAlignment.CENTER);
		panel.setDeferHeight(true);

		setBodyBorder(false);

		this.jid = new TextField<String>();
		this.jid.setEmptyText("Enter Your Jabber ID here");
		this.jid.setAllowBlank(false);
		this.jid.setFieldLabel("JID");
		this.jid.setValidator(new Validator<String, Field<String>>() {

			public String validate(Field<String> field, String value) {
				boolean anonymous = anonumousLogin.getValue() == null ? false : anonumousLogin.getValue();
				if (!anonymous) {
					JID jid = JID.fromString(value);
					if (!jid.isValid() || jid.getNode() == null) {
						return "Please enter a valid Jabber ID";
					}
				}
				return null;
			}
		});

		this.password = new TextField<String>();
		this.password.setAllowBlank(true);
		this.password.setEmptyText("Enter Your password here");
		this.password.setPassword(true);
		this.password.setFieldLabel("Password");

		this.nickname = new TextField<String>();
		this.nickname.setAllowBlank(true);
		this.nickname.setEmptyText("Enter Your nickname here");
		this.nickname.setFieldLabel("Nickname");
		this.nickname.setValidator(new Validator<String, Field<String>>() {

			public String validate(Field<String> field, String value) {
				boolean anonymous = anonumousLogin.getValue() == null ? false : anonumousLogin.getValue();
				if (anonymous && (value == null || value.length() < 3)) {
					return "Please enter Your nickname";
				}
				return null;
			}
		});

		this.anonumousLogin.setValue((Messenger.config().isDefaultAnonymous()));
		this.anonumousLogin.setFieldLabel("Anonumous");
		this.anonumousLogin.addListener(Events.Change, new Listener<FieldEvent>() {

			public void handleEvent(FieldEvent be) {
				jid.setEnabled(!anonumousLogin.getValue());
				nickname.setEnabled(anonumousLogin.getValue());
				password.setEnabled(!anonumousLogin.getValue());
				jid.clearInvalid();
				nickname.clearInvalid();
				password.clearInvalid();
			}
		});

		jid.setEnabled(!anonumousLogin.getValue());
		nickname.setEnabled(anonumousLogin.getValue());
		password.setEnabled(!anonumousLogin.getValue());

		setButtonBar(new ButtonBar());
		getButtonBar().removeAll();
		this.loginButton = new Button("Login", new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				if (panel.isValid()) {
					close();
					fireLogin();
				}
			}
		});
		getButtonBar().add(loginButton);
		getButtonBar().add(new Button("Cancel", new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				close();
			}
		}));
		panel.add(this.anonumousLogin);
		panel.add(this.jid);
		panel.add(this.nickname);
		panel.add(this.password);

		nickname.setAllowBlank(false);

		/*
		 * Listener<FieldEvent> validInvalidListener = new
		 * Listener<FieldEvent>() {
		 * 
		 * public void handleEvent(FieldEvent be) {
		 * System.out.println(panel.isValid()); } };
		 * 
		 * jid.addListener(Events.Valid, validInvalidListener);
		 * jid.addListener(Events.Invalid, validInvalidListener);
		 * nickname.addListener(Events.Valid, validInvalidListener);
		 * nickname.addListener(Events.Invalid, validInvalidListener);
		 * password.addListener(Events.Valid, validInvalidListener);
		 * nickname.addListener(Events.Invalid, validInvalidListener);
		 */
		JID defaultJID = Messenger.config().getJid();
		if (defaultJID != null)
			this.jid.setValue(defaultJID.toString());
		if (Messenger.config().getPassword() != null)
			this.password.setValue(Messenger.config().getPassword());

		add(panel);
		// panel.isValid();
	}

	public void addListener(LoginDialogListener listener) {
		this.listeners.add(listener);
	}

	protected void fireLogin() {
		for (LoginDialogListener listener : this.listeners) {
			listener.onLogin(this);
		}
	}

	public String getJID() {
		return this.jid.getRawValue();
	}

	public String getPassword() {
		return this.password.getRawValue();
	}

	public String getNickname() {
		String x = this.nickname.getRawValue();
		x = x.trim().length() == 0 ? null : x;
		return x;
	}

	public boolean isAnonymous() {
		Boolean x = this.anonumousLogin.getValue();
		return x == null ? false : x.booleanValue();
	}

	public void removeListener(LoginDialogListener listener) {
		this.listeners.remove(listener);
	}
}

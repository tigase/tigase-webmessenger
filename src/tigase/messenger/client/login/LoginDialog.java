package tigase.messenger.client.login;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class LoginDialog extends Dialog {

	private final TextField<String> jid;

	public String getJID() {
		return this.jid.getRawValue();
	}

	public String getPassword() {
		return this.password.getRawValue();
	}

	public boolean isAnonymous() {
		Boolean x = this.anonumousLogin.getValue();
		return x == null ? false : x.booleanValue();
	}

	private final TextField<String> password;

	private final CheckBox anonumousLogin;

	private final List<LoginDialogListener> listeners = new ArrayList<LoginDialogListener>();

	public void addListener(LoginDialogListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(LoginDialogListener listener) {
		this.listeners.remove(listener);
	}

	public LoginDialog() {
		super();
		setHeading("Login");
		setWidth(360);
		setClosable(false);
		setModal(true);
		setHideOnButtonClick(true);

		FormPanel panel = new FormPanel();
		panel.setFieldWidth(230);
		panel.setLabelWidth(70);
		panel.setHeaderVisible(false);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setButtonAlign(HorizontalAlignment.CENTER);

		setBodyBorder(false);

		this.jid = new TextField<String>();
		this.jid.setEmptyText("Enter Your Jabber ID here");
		this.jid.setAllowBlank(false);
		this.jid.setFieldLabel("JID");

		this.password = new TextField<String>();
		this.password.setEmptyText("Enter Your password here");
		this.password.setPassword(true);
		this.password.setFieldLabel("Password");

		this.anonumousLogin = new CheckBox();
		this.anonumousLogin.setFieldLabel("Anonumous");
		this.anonumousLogin.addListener(Events.Change, new Listener<FieldEvent>() {

			public void handleEvent(FieldEvent be) {
				jid.setEnabled(!anonumousLogin.getValue());
				password.setEnabled(!anonumousLogin.getValue());
			}
		});

		setButtonBar(new ButtonBar());
		getButtonBar().removeAll();
		getButtonBar().add(new Button("Login", new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				fireLogin();
			}
		}));
		getButtonBar().add(new Button("Cancel", new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
			}
		}));

		panel.add(this.anonumousLogin);
		panel.add(this.jid);
		panel.add(this.password);

		this.jid.setValue("alice@sphere/www");
		this.password.setValue("a");
		
		add(panel);
	}

	protected void fireLogin() {
		for (LoginDialogListener listener : this.listeners) {
			listener.onLogin(this);
		}
	}
}

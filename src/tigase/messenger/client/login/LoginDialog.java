package tigase.messenger.client.login;

import java.util.ArrayList;

import tigase.xmpp4gwt.client.JID;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginDialog extends Dialog {

	private final TextBox jidInput = new TextBox();

	private final PasswordTextBox passwordInput = new PasswordTextBox();

	private final LabelField jidLabel;

	private final LabelField passwordLabel;

	public boolean isAnonymousChecked() {
		return anonymous.isChecked();
	}

	private final CheckBox anonymous;

	public LoginDialog() {
		jidInput.setText("bob@sphere");
		passwordInput.setText("b");

		jidLabel = new LabelField("JID:");
		passwordLabel = new LabelField("Password:");

		setModal(true);
		setBodyBorder(false);
		// setInsetBorder(false);
		setButtons(Dialog.OKCANCEL);
		setClosable(false);
		// setIconStyle("icon-app-side");
		setHeading("Login");
		setAutoHeight(true);
		setAutoWidth(true);

		TableLayout layout = new TableLayout(2);
		layout.setCellPadding(4);
		layout.setCellSpacing(4);
		setLayout(layout);

		anonymous = new CheckBox("Anonymous login");
		anonymous.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				System.out.println(anonymous.isChecked());
				jidInput.setEnabled(!anonymous.isChecked());
				passwordInput.setEnabled(!anonymous.isChecked());
				jidLabel.setEnabled(!anonymous.isChecked());
				passwordLabel.setEnabled(!anonymous.isChecked());
			}
		});

		add(new Label());
		add(anonymous);
		add(jidLabel);
		add(jidInput);

		add(passwordLabel);
		add(passwordInput);

	}

	public String getPassword() {
		return this.passwordInput.getText();
	}

	public JID getJID() {
		return JID.fromString(this.jidInput.getText());
	}

	@Override
	public void setEnabled(boolean enabled) {
	/*
	 * super.setEnabled(enabled); this.jidInput.setEnabled(enabled);
	 * this.passwordInput.setEnabled(enabled);
	 * getButtonBar().setEnabled(enabled);
	 */
	}

	private final ArrayList<LoginDialogListener> listeners = new ArrayList<LoginDialogListener>();

	public void reset() {
		this.jidInput.setText("");
		this.passwordInput.setText("");
	}

	public void addLoginDialogListener(LoginDialogListener listener) {
		this.listeners.add(listener);
	}

	public void removeLoginDialogListener(LoginDialogListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	protected void onButtonPressed(Button button) {
		try {
			super.onButtonPressed(button);
			if (button.getItemId().equals("ok")) {
				// setEnabled(false);
				for (LoginDialogListener listener : listeners) {
					listener.onPressLogin();
				}
			} else if (button.getItemId().equals("cancel")) {
				reset();
				for (LoginDialogListener listener : listeners) {
					listener.onPressCancel();
				}
			}
		} catch (Exception e) {}
	}

	@Override
	protected void afterShow() {
		super.afterShow();
		jidInput.setFocus(true);
	}
}

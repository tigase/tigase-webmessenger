package tigase.messenger.client.login;

import java.util.ArrayList;

import tigase.xmpp4gwt.client.JID;

import com.extjs.gxt.ui.client.widget.Button;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

public class LoginDialog extends Dialog {

	private final TextBox jidInput = new TextBox();

	private final PasswordTextBox passwordInput = new PasswordTextBox();

	public LoginDialog() {
		jidInput.setText("bob@sphere");
		passwordInput.setText("b");

		setModal(false);
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

		add(new Label("JID:"));
		add(jidInput);

		add(new Label("Password:"));
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

package tigase.messenger.client;

import tigase.xmpp4gwt.client.JID;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

public class SubscriptionRequestDialog extends VCardDialog {

	public static final String AUTHORIZE = "authorize";

	public static final String DENY = "deny";

	protected CheckBox sendRequest;

	public SubscriptionRequestDialog(JID jid) {
		super(jid);
	}

	@Override
	protected String getWindowHeading() {
		return "Subscription request from: " + jid.toStringBare();
	}

	@Override
	protected void init() {
		super.init();

		FormPanel fp = new FormPanel();
		fp.setHeaderVisible(false);
		fp.setFrame(false);
		fp.setBorders(false);
		fp.setBodyBorder(false);
		fp.setDeferHeight(true);
		sendRequest = new CheckBox();
		sendRequest.setBoxLabel("Ask to see his/her status");
		sendRequest.setLabelSeparator("");
		sendRequest.setValue(true);
		fp.add(sendRequest);

		add(fp);
	}

	@Override
	protected void onButtonPressed(Button button) {
		if (AUTHORIZE.equals(button.getItemId())) {
			Messenger.session().getPresencePlugin().subscribed(jid);
			if (sendRequest.getValue()) {
				Messenger.session().getPresencePlugin().subscribe(jid);
			}
		} else if (DENY.equals(button.getItemId())) {
			Messenger.session().getPresencePlugin().unsubscribed(jid);
		}
		super.onButtonPressed(button);
	}

	@Override
	protected void prepareButtonBar() {
		Button authorizeButton = new Button("Authorize");
		authorizeButton.setItemId(AUTHORIZE);
		getButtonBar().add(authorizeButton);

		Button denyButton = new Button("Deny");
		denyButton.setItemId(DENY);
		getButtonBar().add(denyButton);

		Button cancelButton = new Button("Cancel");
		cancelButton.setItemId(Dialog.CANCEL);
		getButtonBar().add(cancelButton);
	}

}

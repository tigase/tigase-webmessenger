package tigase.messenger.client;

import tigase.xmpp4gwt.client.xmpp.presence.PresenceItem;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;

public class SubscriptionRequestDialog extends Dialog {

	private Button authorizeButton;

	public SubscriptionRequestDialog(final PresenceItem item) {

		setHeading("Subscription request");
		setButtons("");

		addText("User " + item.getJid().toString() + " would like to add You to his roster.");

		final CheckBox auto = new CheckBox();
		auto.setBoxLabel("Send subscription request");

		add(auto);

		addButton(new Button("Cancel", new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				hide();
			}
		}));

		this.authorizeButton = new Button("Authorize", new SelectionListener<ComponentEvent>() {
			public void componentSelected(ComponentEvent ce) {
				Messenger.session().getPresencePlugin().subscribed(item.getJid());
				if (auto.getValue()) {
					Messenger.session().getPresencePlugin().subscribe(item.getJid());
				}
				hide();
			}
		});
		addButton(authorizeButton);

	}

}

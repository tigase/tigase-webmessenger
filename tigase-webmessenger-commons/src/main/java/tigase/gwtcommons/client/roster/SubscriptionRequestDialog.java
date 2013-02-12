package tigase.gwtcommons.client.roster;

import tigase.gwtcommons.client.Translations;
import tigase.gwtcommons.client.XmppService;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class SubscriptionRequestDialog extends Dialog {

	private FormPanel form = new FormPanel();

	private final TextField<String> jid = new TextField<String>();

	private final TextField<String> nickname = new TextField<String>();

	private final Presence presence;

	public SubscriptionRequestDialog(final Presence $presence) {
		this.presence = $presence;
		setButtons(Dialog.YESNOCANCEL);
		setHeading("Subscription request");
		setWidth(350);

		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setBorders(false);
		form.setFrame(false);
		form.setAutoHeight(true);

		jid.setFieldLabel(Translations.instance.clientContactEditDialogJid());
		jid.setReadOnly(true);
		try {
			jid.setValue(presence.getFrom().toString());
		} catch (XMLException e) {
			e.printStackTrace();
		}
		form.add(jid);

		nickname.setFieldLabel("Nickname");
		nickname.setReadOnly(true);
		try {
			nickname.setValue(presence.getNickname());
		} catch (XMLException e) {
			e.printStackTrace();
		}
		form.add(nickname);

		add(form);
	}

	@Override
	protected void onButtonPressed(Button button) {
		if (button.getItemId().equals(YES)) {
			try {
				XmppService.get().getModulesManager().getModule(PresenceModule.class).subscribed(presence.getFrom());
				hide();
			} catch (Exception e) {
				MessageBox.alert(Translations.instance.error(), "Can't accept request", null);
			}
		} else if (button.getItemId().equals(NO)) {
			try {
				XmppService.get().getModulesManager().getModule(PresenceModule.class).unsubscribed(presence.getFrom());
				hide();
			} catch (Exception e) {
				MessageBox.alert(Translations.instance.error(), "Can't reject request", null);
			}
		} else if (button.getItemId().equals(CANCEL)) {
			hide();
		} else
			super.onButtonPressed(button);
	}

}

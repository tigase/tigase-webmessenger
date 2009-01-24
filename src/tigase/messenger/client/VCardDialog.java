package tigase.messenger.client;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.ErrorCondition;
import tigase.jaxmpp.core.client.xmpp.xeps.vcard.VCard;
import tigase.jaxmpp.core.client.xmpp.xeps.vcard.VCardResponseHandler;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class VCardDialog extends Dialog {
	private final TextField<String> countryField = new TextField<String>();
	private final TextField<String> fullNameField = new TextField<String>();
	protected final JID jid;
	private final TextField<String> localityField = new TextField<String>();

	private final TextField<String> nicknameField = new TextField<String>();

	public VCardDialog(final JID jid) {
		this.jid = jid;
		setHeading(getWindowHeading());
		setButtons("");

		setWidth(350);
		setAutoHeight(true);

		init();

		Messenger.session().getVCardPlugin().vCardRequest(jid.getBareJID(), new VCardResponseHandler() {

			public void onError(IQ iq, ErrorType errorType, ErrorCondition errorCondition, String text) {
			}

			@Override
			public void onSuccess(VCard vcard) {
				showVCard(vcard);
			}
		});
	}

	protected String getWindowHeading() {
		return "Information about: " + jid.toStringBare();
	}

	protected void init() {
		prepareButtonBar();
		TabPanel tabPanel = prepareTabPanel();
		add(tabPanel);
	}

	@Override
	protected void onButtonPressed(Button button) {
		super.onButtonPressed(button);
		close();
	}

	protected void prepareButtonBar() {
		Button okButton = new Button("Ok");
		okButton.setItemId(Dialog.OK);
		getButtonBar().add(okButton);
	}

	protected TabItem prepareInformationTabItem() {
		TabItem infoTabItem = new TabItem("Information");

		FormPanel fp = new FormPanel();
		infoTabItem.add(fp);
		fp.setHeaderVisible(false);
		fp.setFrame(false);
		fp.setBorders(false);
		fp.setBodyBorder(false);
		fp.setDeferHeight(true);

		fullNameField.setFieldLabel("Full name");

		fp.add(fullNameField);

		nicknameField.setFieldLabel("Nickname");
		fp.add(nicknameField);

		localityField.setFieldLabel("Locality");
		fp.add(localityField);

		countryField.setFieldLabel("Country");
		fp.add(countryField);

		fp.setReadOnly(true);

		return infoTabItem;
	}

	protected TabPanel prepareTabPanel() {
		TabPanel tabPanel = new TabPanel();
		tabPanel.add(prepareInformationTabItem());

		return tabPanel;
	}

	protected void showVCard(final VCard vcard) {
		fullNameField.setValue(vcard.getName());
		nicknameField.setValue(vcard.getNickname());
		localityField.setValue(vcard.getLocality());
		countryField.setValue(vcard.getCountry());
	}

}

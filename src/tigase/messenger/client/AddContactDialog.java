package tigase.messenger.client;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.ResponseHandler;
import tigase.jaxmpp.core.client.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.ErrorCondition;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;

public class AddContactDialog extends Dialog {

	private static class Protocol extends BaseModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5485694596528581813L;

		public Protocol(String name, String jid) {
			set("name", name);
			set("jid", jid);
		}

		public String getJid() {
			return get("jid");
		}

		public String getName() {
			return get("name");
		}

		public void setName(String name) {
			set("name", name);
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	private final Button addButton;

	private final Button cancelButton;

	private final TextField<String> jid = new TextField<String>();

	private final TextField<String> name = new TextField<String>();

	private final CheckBox sendRequest = new CheckBox();

	public AddContactDialog() {
		setButtons("");
		setHeading("Add contact");

		addButton = new Button("Add");
		addButton.setItemId(Dialog.OK);
		cancelButton = new Button("Cancel");
		cancelButton.setItemId(Dialog.CANCEL);

		getButtonBar().add(addButton);
		getButtonBar().add(cancelButton);

		setWidth(390);

		createForm2();

		// FormPanel fp = new FormPanel();
		// fp.setHeaderVisible(false);
		// fp.setFrame(false);
		// fp.setBorders(false);
		// fp.setBodyBorder(false);
		// fp.setDeferHeight(true);
		//
		// add(fp);
		//
		// Protocol jabberProtocol = new Protocol("Jabber", null);
		//
		// ListStore<Protocol> store = new ListStore<Protocol>();
		// store.add(jabberProtocol);
		// store.add(new Protocol("GG", null));
		// ComboBox<Protocol> services = new ComboBox<Protocol>();
		// services.setDisplayField("name");
		// services.setEditable(false);
		// services.setFieldLabel("Protocol:");
		// services.setStore(store);
		// services.setValue(jabberProtocol);
		// fp.add(services);
		// // --
		// FieldSet fieldSet = new FieldSet();
		// fieldSet.setVisible(true);
		// fieldSet.setHeading("Phone Numbers");
		// fieldSet.setCollapsible(true);
		//
		// FormLayout layout = new FormLayout();
		// layout.setLabelWidth(75);
		// layout.setPadding(4);
		// fieldSet.setLayout(layout);
		//
		// TextField<String> field = new TextField<String>();
		// field.setFieldLabel("Home");
		// fieldSet.add(field);
		//
		// fp.add(fieldSet);
		// // --
		// TextField<String> jid = new TextField<String>();
		// jid.setFieldLabel("Jabber ID");
		// fp.add(jid);
		//
		// TextField<String> name = new TextField<String>();
		// name.setFieldLabel("Name");
		// fp.add(name);

	}

	private void createForm2() {
		FormPanel baseForm = new FormPanel();
		baseForm.setFrame(true);
		baseForm.setBodyBorder(false);
		baseForm.setHeaderVisible(false);
		baseForm.setHeading("Simple Form with FieldSets");
		// form2.setWidth(350);
		baseForm.setLayout(new FlowLayout());

		FormPanel protocolForm = new FormPanel();
		protocolForm.setFrame(false);
		protocolForm.setBodyBorder(false);
		protocolForm.setHeaderVisible(false);

		final Protocol jabberProtocol = new Protocol("Jabber", null);
		ListStore<Protocol> store = new ListStore<Protocol>();
		store.add(jabberProtocol);
		// store.add(new Protocol("GG", null));
		final ComboBox<Protocol> services = new ComboBox<Protocol>();
		services.setDisplayField("name");
		services.setEditable(false);
		services.setFieldLabel("Protocol:");
		services.setStore(store);
		services.setValue(jabberProtocol);
		protocolForm.add(services);
		baseForm.add(protocolForm);

		final FieldSet serviceTranslationFieldSet = new FieldSet();
		serviceTranslationFieldSet.setHeading("Service ID translation");
		serviceTranslationFieldSet.setCollapsible(true);

		FormPanel serviceTranslationForm = new FormPanel();
		serviceTranslationForm.setHeaderVisible(false);
		serviceTranslationForm.setFrame(false);
		serviceTranslationForm.setBodyBorder(false);
		serviceTranslationFieldSet.add(serviceTranslationForm);

		// FormLayout layout = new FormLayout();
		// layout.setLabelWidth(75);
		// layout.setPadding(4);
		// fieldSet.setLayout(layout);

		TextField<String> foreignName = new TextField<String>();
		foreignName.setFieldLabel("Name");
		serviceTranslationForm.add(foreignName);
		baseForm.add(serviceTranslationFieldSet);

		serviceTranslationForm.setButtonAlign(HorizontalAlignment.RIGHT);
		serviceTranslationForm.addButton(new Button("Convert to JabberID"));

		FormPanel jidNameForm = new FormPanel();
		jidNameForm.setFrame(false);
		jidNameForm.setBodyBorder(false);
		jidNameForm.setHeaderVisible(false);

		jid.setAllowBlank(false);
		jid.setValidator(new Validator() {

			public String validate(Field<?> field, String value) {
				try {
					JID jid = JID.fromString(value);
					if (!jid.isValid()) {
						return "JID mallformed";
					}
					if (jid.getResource() != null) {
						return "Resource not allowed";
					}
					if (jid.getNode() == null) {
						return "Username must be presented";
					}
					return null;
				} catch (Exception e) {
					return "JID mallformed";
				}
			}
		});
		jid.addListener(Events.Invalid, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				addButton.setEnabled(false);
			}
		});
		jid.addListener(Events.Valid, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				addButton.setEnabled(true);
			}
		});
		jid.setFieldLabel("Jabber ID");
		jid.setValidationDelay(50);
		jidNameForm.add(jid);

		name.setFieldLabel("Name");
		jidNameForm.add(name);

		sendRequest.setBoxLabel("Ask to see his/her status");
		sendRequest.setLabelSeparator("");
		sendRequest.setValue(true);
		jidNameForm.add(sendRequest);

		baseForm.add(jidNameForm);
		add(baseForm);
		serviceTranslationFieldSet.setEnabled(!services.getSelection().contains(jabberProtocol));
		serviceTranslationFieldSet.setExpanded(serviceTranslationFieldSet.isEnabled());
		services.addListener(Events.Select, new Listener<FieldEvent>() {

			public void handleEvent(FieldEvent be) {
				serviceTranslationFieldSet.setEnabled(!services.getSelection().contains(jabberProtocol));
				serviceTranslationFieldSet.setExpanded(serviceTranslationFieldSet.isEnabled());
			}
		});
	}

	@Override
	protected void onButtonPressed(Button button) {
		if (button.getItemId().equals(Dialog.OK)) {
			final JID jid = JID.fromString(this.jid.getValue());
			Tigase_messenger.session().getRosterPlugin().addItem(jid, this.name.getValue(), (String[]) null, new ResponseHandler() {

				public void onError(IQ iq, ErrorType errorType, ErrorCondition errorCondition, String text) {
					// TODO Auto-generated method stub

				}

				public void onResult(IQ iq) {
					if (sendRequest.getValue()) {
						Tigase_messenger.session().getPresencePlugin().subscribe(jid);
					}
					close();
				}
			});
		} else if (button.getItemId().equals(Dialog.CANCEL)) {
			close();
		}
	}

	@Override
	public void show() {
		addButton.setEnabled(jid.validate());
		super.show();
	}
}

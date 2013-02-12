package tigase.gwtcommons.client.roster;

import java.util.ArrayList;

import tigase.gwtcommons.client.Translations;
import tigase.gwtcommons.client.XmppService;
import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class ContactEditDialog extends Dialog {

	private static class Group extends BaseModelData {
		private String name;

		public Group(String value) {
			setName(value);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			set("name", name);
			this.name = name;
		}

	}

	private final FormPanel form = new FormPanel();

	private final ListField<Group> groups = new ListField<ContactEditDialog.Group>();

	private final ListStore<Group> groupsStore = new ListStore<ContactEditDialog.Group>();

	private RosterItem item;

	private final TextField<String> jid = new TextField<String>();

	private final TextField<String> name = new TextField<String>();

	public ContactEditDialog(final RosterItem item) {
		this.item = item;
		setLayout(new FitLayout());
		setHeading(Translations.instance.clientContactEditDialog());
		setButtons(OKCANCEL);
		setWidth(324);
		setModal(true);
		setResizable(false);

		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setBorders(false);
		form.setFrame(false);
		form.setAutoHeight(true);

		jid.setFieldLabel(Translations.instance.clientContactEditDialogJid());
		jid.setAllowBlank(false);
		form.add(jid);

		name.setFieldLabel(Translations.instance.clientContactEditDialogName());
		name.setAllowBlank(true);
		form.add(name);

		ContentPanel cp = new ContentPanel(new FitLayout());
		cp.setBodyBorder(false);
		cp.setFrame(false);
		cp.setBorders(false);
		cp.setHeaderVisible(false);

		groups.setStore(groupsStore);
		groups.setDisplayField("name");
		cp.add(groups);

		ToolBar tb = new ToolBar();
		tb.add(new Button("Add", new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				MessageBox mb = MessageBox.prompt("Group", "Enter group name", new Listener<MessageBoxEvent>() {

					public void handleEvent(MessageBoxEvent be) {
						groupsStore.add(new Group(be.getValue()));
					}
				});

			}
		}));
		final Button removeButton = new Button("Remove", new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				for (Group g : groups.getSelection()) {
					groupsStore.remove(g);
				}
			}
		});
		removeButton.setEnabled(false);
		groups.getListView().getSelectionModel().addSelectionChangedListener(
				new SelectionChangedListener<ContactEditDialog.Group>() {

					@Override
					public void selectionChanged(SelectionChangedEvent<Group> se) {
						removeButton.setEnabled(se.getSelectedItem() != null);
					}
				});
		tb.add(removeButton);
		cp.setTopComponent(tb);

		AdapterField af = new AdapterField(cp);
		af.setFieldLabel(Translations.instance.clientContactEditDialogGroups());
		form.add(af);

		add(form);

		if (item != null) {
			jid.setValue(item.getJid().toString());
			jid.setReadOnly(true);

			name.setValue(item.getName());

			if (item.getGroups() != null)
				for (String gr : item.getGroups()) {
					groupsStore.add(new Group(gr));
				}
		}
	}

	@Override
	protected void onButtonPressed(Button button) {
		if (button.getItemId().equals(Dialog.OK) && form.isValid()) {
			onSubmit();
		} else if (!button.getItemId().equals(Dialog.OK)) {
			hide();
		}
	}

	protected void onSubmit() {
		try {
			ArrayList<String> gr = new ArrayList<String>();
			for (Group g : groupsStore.getModels()) {
				gr.add(g.getName());
			}
			XmppService.get().getRoster().add(BareJID.bareJIDInstance(jid.getValue()), name.getValue(), gr,
					new AsyncCallback() {

						public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
							MessageBox.alert(Translations.instance.error(), "Cant add contact.\n" + error, null);
						}

						public void onSuccess(Stanza responseStanza) throws XMLException {
							hide();
							if (item == null)
								try {
									XmppService.get().getModulesManager().getModule(PresenceModule.class).subscribe(
											JID.jidInstance(jid.getValue()));
								} catch (Exception e) {
									MessageBox.alert(Translations.instance.error(), "Can't subscribe contact", null);
								}

						}

						public void onTimeout() throws XMLException {
							MessageBox.alert(Translations.instance.error(), "Cant add contact. Timeout!", null);
						}
					});
			hide();
		} catch (Exception e) {
			MessageBox.alert(Translations.instance.error(), "Cant add contact", null);
			e.printStackTrace();
		}
	}

}

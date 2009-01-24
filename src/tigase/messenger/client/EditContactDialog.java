package tigase.messenger.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xmpp.roster.RosterItem;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class EditContactDialog extends Dialog {

	private final DataList groups = new DataList();

	private final TextField<String> name = new TextField<String>();

	final RosterItem ri;

	public EditContactDialog(JID jid) {
		super();
		ri = Messenger.session().getRosterPlugin().getRosterItem(jid);
		setHeading("Edit contact");
		setButtons(OKCANCEL);

		setWidth(350);
		setAutoHeight(true);

		FormPanel fp = new FormPanel();
		fp.setHeaderVisible(false);
		fp.setFrame(false);
		fp.setBorders(false);
		fp.setBodyBorder(false);

		fp.setDeferHeight(true);

		LabelField lf = new LabelField(jid.toStringBare());
		lf.setFieldLabel("Contact");

		fp.add(lf);

		name.setFieldLabel("Name");
		if (ri != null)
			name.setValue(ri.getName());
		fp.add(name);

		groups.setHeight(127);
		groups.setScrollMode(Scroll.AUTO);
		groups.setCheckable(true);

		Set<String> groupsNames = new HashSet<String>();
		for (RosterItem $ri : Messenger.session().getRosterPlugin().getAllRosteritems()) {
			String[] g = $ri.getGroups();
			if (g != null)
				for (String string : g) {
					groupsNames.add(string);
				}
		}

		final Set<String> buddyGroups = new HashSet<String>();
		if (ri.getGroups() != null) {
			for (String gn : ri.getGroups()) {
				buddyGroups.add(gn);
			}
		}

		for (String group : groupsNames) {
			DataListItem dli = new DataListItem(group);
			dli.setChecked(buddyGroups.contains(group));
			dli.setData("name", group);
			groups.add(dli);
		}

		AdapterField groupsA = new AdapterField(groups);
		groupsA.setFieldLabel("Groups");
		fp.add(groupsA);

		HorizontalPanel hp = new HorizontalPanel();

		final TextField<String> newGroupName = new TextField<String>();
		hp.add(newGroupName);
		Button addNewGroup = new Button("Add", new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				String n = newGroupName.getValue();
				newGroupName.reset();
				if (n != null && n.length() > 0) {
					DataListItem dli = new DataListItem(n);
					dli.setChecked(buddyGroups.contains(n));
					dli.setData("name", n);
					groups.add(dli);
				}
			}
		});
		hp.add(addNewGroup);
		AdapterField ahp = new AdapterField(hp);
		ahp.setLabelSeparator("");
		fp.add(ahp);

		add(fp);
	}

	@Override
	protected void onButtonPressed(Button button) {
		super.onButtonPressed(button);
		if (Dialog.OK.equals(button.getItemId())) {
			java.util.List<DataListItem> selectedGroups = groups.getChecked();
			List<String> groups = new ArrayList<String>();
			for (DataListItem dataListItem : selectedGroups) {
				groups.add((String) dataListItem.getData("name"));
			}
			Messenger.session().getRosterPlugin().addItem(JID.fromString(ri.getJid()), name.getValue(), groups, null);
		}
		close();
	}
}

package tigase.gwtcommons.client;

import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

public abstract class CustomPresenceStatusDialog extends Dialog {

	public abstract void onSubmit(Show show, String status);

	private final FormPanel form = new FormPanel();

	private final TextField<String> status = new TextField<String>();

	public static class SimpleComboBox extends BaseModelData {

		private static final long serialVersionUID = 1L;

		private Show show;

		private String label;

		public SimpleComboBox(Show show, String label) {
			setShow(show);
			setLabel(label);
		}

		public Show getShow() {
			return show;
		}

		public void setShow(Show show) {
			set("show", show);
			this.show = show;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			set("label", label);
			this.label = label;
		}

	}

	private final ComboBox<SimpleComboBox> shows = new ComboBox<SimpleComboBox>();

	public CustomPresenceStatusDialog(Show currentShow, String currentPresence) {
		setHeading("Custom status");
		// setLayout(new FitLayout());
		setButtons(Dialog.OKCANCEL);
		setAutoHeight(true);
		setResizable(false);
		setWidth(350);

		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setBorders(false);
		form.setFrame(false);
		form.setAutoWidth(true);

		shows.setStore(new ListStore<SimpleComboBox>());
		shows.setTriggerAction(TriggerAction.ALL);
		shows.setFieldLabel("Status");
		shows.setDisplayField("label");
		shows.setEditable(false);
		shows.setAllowBlank(false);
		shows.getStore().add(new SimpleComboBox(Show.online, "Online"));
		shows.getStore().add(new SimpleComboBox(Show.chat, "Free for chat"));
		shows.getStore().add(new SimpleComboBox(Show.away, "Away"));
		shows.getStore().add(new SimpleComboBox(Show.xa, "Extended Away"));
		shows.getStore().add(new SimpleComboBox(Show.dnd, "Do Not Disturb"));
		Show cs = currentShow == null ? Show.online : currentShow;

		for (SimpleComboBox x : shows.getStore().getModels()) {
			if (x.getShow().equals(cs))
				shows.setValue(x);
		}

		form.add(shows);

		status.setFieldLabel("Text");
		status.setValue(currentPresence);
		form.add(status);

		add(form);
	}

	@Override
	protected void onButtonPressed(Button button) {
		if (button.getItemId().equals(Dialog.OK) && form.isValid()) {
			String x = status.getValue();
			onSubmit(shows.getValue().getShow(), x == null || x.length() == 0 ? null : x);
			hide();
		} else if (button.getItemId().equals(Dialog.CANCEL)) {
			hide();
		}
	}

}

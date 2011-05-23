package tigase.gwtcommons.client.muc;

import tigase.gwtcommons.client.Translations;
import tigase.gwtcommons.client.XmppService;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.logger.LoggerFactory;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule;
import tigase.jaxmpp.core.client.xmpp.modules.muc.MucModule.MucEvent;
import tigase.jaxmpp.core.client.xmpp.modules.muc.Room;
import tigase.jaxmpp.core.client.xmpp.stanzas.ErrorElement;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class MucManagerModule {

	public static String getErrorMessage(ErrorElement ee) throws XMLException {
		String msg;
		if (ee != null && ee.getCondition() == ErrorCondition.forbidden)
			msg = Translations.instance.mucErrorAccessDenied();
		if (ee != null && ee.getCondition() == ErrorCondition.conflict)
			msg = Translations.instance.mucErrorConflict();
		else if (ee != null && ee.getText() != null)
			msg = ee.getText();
		else if (ee != null && ee.getCondition() != null)
			msg = ee.getCondition().toString();
		else
			msg = Translations.instance.mucErrorUnknown();
		// "Unknown reason.";
		return msg;
	}

	private final Listener<MucModule.MucEvent> listener;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final TabPanel tabPanel;

	public MucManagerModule(TabPanel center) {
		this.tabPanel = center;
		this.listener = new Listener<MucModule.MucEvent>() {

			public void handleEvent(MucEvent be) {
				onMucEvent(be);
			}
		};
	}

	private MucTab getMucTab(Room c) {
		for (TabItem i : this.tabPanel.getItems()) {
			if (i instanceof MucTab && ((MucTab) i).getRoom() == c)
				return (MucTab) i;
		}
		return null;
	}

	public void init() {
		tabPanel.addListener(Events.Remove, new com.extjs.gxt.ui.client.event.Listener<TabPanelEvent>() {

			public void handleEvent(TabPanelEvent be) {
				if (be.getItem() instanceof MucTab)
					onMucTabClose((MucTab) be.getItem());
			}
		});
		XmppService.get().getModulesManager().getModule(MucModule.class).addListener(listener);
	}

	public void join(String roomName, String mucServer, String nickName, String password) throws XMLException, JaxmppException {
		Room room = XmppService.get().getModulesManager().getModule(MucModule.class).join(roomName, mucServer, nickName,
				password);

		MucTab mt = getMucTab(room);
		if (mt == null) {
			mt = new MucTab(room);
			tabPanel.add(mt);
		}
		final MucTab m = mt;
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			public void execute() {
				tabPanel.setSelection(m);
			}
		});
	}

	protected void onMucEvent(MucEvent be) {
		if (be.getType() == MucModule.MucMessageReceived)
			onMucMessageEvent(be);
		else {
			onPresenceEvent(be);
		}
	}

	protected void onMucMessageEvent(MucEvent be) {
		MucTab ct = getMucTab(be.getRoom());
		ct.process(be.getMessage());
	}

	protected void onMucTabClose(MucTab item) {
		try {
			XmppService.get().getModulesManager().getModule(MucModule.class).leave(item.getRoom());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void onPresenceEvent(MucEvent be) {
		MucTab ct = getMucTab(be.getRoom());
		try {
			if (be.getType() == MucModule.RoomClosed) {
				ErrorElement ee = ErrorElement.extract(be.getPresence());
				String msg = getErrorMessage(ee);
				MessageBox.alert(Translations.instance.error(), msg, null);

				ct.close();
			} else
				ct.process(be);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

}

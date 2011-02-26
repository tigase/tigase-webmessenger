package tigase.mucclient.client;

import tigase.gwtcommons.client.XmppService;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.RootPanel;

public class MucClient implements EntryPoint {

	public void onModuleLoad() {
		final ChatRestoreModule chatRestorer = XmppService.get().getModulesManager().register(
				new ChatRestoreModule(XmppService.get().getSessionObject(), XmppService.get().getWriter()));

		try {
			XmppService.get().restoreSession();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Window.addWindowClosingHandler(new ClosingHandler() {

			public void onWindowClosing(ClosingEvent event) {
				System.out.println("CLOSING!!!!");
				if (XmppService.get().isConnected()) {
					XmppService.get().storeSession();
				}
			}
		});

		Window.addCloseHandler(new CloseHandler<Window>() {

			public void onClose(CloseEvent<Window> event) {
				System.out.println(" CLOSE !!!! ");
				if (XmppService.get().isConnected()) {
					XmppService.get().storeSession();
				}
			}
		});

		MucViewport mvp = new MucViewport(chatRestorer);
		RootPanel.get().add(mvp);
	}

}

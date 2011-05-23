package tigase.messenger.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TigaseMessenger2 implements EntryPoint {

	public void onModuleLoad() {
		MainViewport mvp = new MainViewport();
		RootPanel.get().add(mvp);
	}
}

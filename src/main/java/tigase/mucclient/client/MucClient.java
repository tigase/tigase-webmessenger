package tigase.mucclient.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class MucClient implements EntryPoint {

	public void onModuleLoad() {
		MucViewport mvp = new MucViewport();
		RootPanel.get().add(mvp);
	}

}

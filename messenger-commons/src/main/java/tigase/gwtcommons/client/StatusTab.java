package tigase.gwtcommons.client;

import tigase.gwtcommons.client.roster.RosterPanel;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.Connector.State;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.JaxmppCore.JaxmppEvent;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule.AuthEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class StatusTab extends TabItem {

	private final MessagePanel vp = new MessagePanel();

	public StatusTab() {
		setText(Translations.instance.clientStatusTabTitle());
		setClosable(false);
		setLayout(new FitLayout());
		add(vp);
	}

	public void init() {
		XmppService.get().getConnector().addListener(Connector.Error, new Listener<AbstractBoshConnector.BoshConnectorEvent>() {

			public void handleEvent(AbstractBoshConnector.BoshConnectorEvent be) {
				String m = "Connector error. code=" + be.getResponseCode();
				if (be.getCaught() != null) {
					m += ",  message=" + be.getCaught().getMessage();
				}
				if (be.getResponseData() != null && be.getResponseData().length() > 0) {
					m += ",  responseData=" + be.getResponseData();
				}
				vp.addErrorMessage(m);
			}
		});
		XmppService.get().getConnector().addListener(Connector.StateChanged, new Listener<Connector.ConnectorEvent>() {

			public void handleEvent(ConnectorEvent be) {
				if (XmppService.get().getConnector().getState() == State.connecting) {
					vp.addAppMessage(Translations.instance.stateConnecting());
				} else if (XmppService.get().getConnector().getState() == State.disconnecting) {
					vp.addAppMessage(Translations.instance.stateDisconnecting());
				}
			}
		});
		XmppService.get().addListener(JaxmppCore.Connected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				vp.addAppMessage(Translations.instance.stateConnected());
			}
		});
		XmppService.get().getModulesManager().getModule(ResourceBinderModule.class).addListener(
				ResourceBinderModule.ResourceBindSuccess, new Listener<ResourceBinderModule.ResourceBindEvent>() {

					public void handleEvent(ResourceBindEvent be) {
						vp.addAppMessage(Translations.instance.clientMessageLoggedInAs(be.getJid().toString()));
					}
				});
		XmppService.get().addListener(JaxmppCore.Disconnected, new Listener<JaxmppEvent>() {

			public void handleEvent(JaxmppEvent be) {
				if (be.getCaught() != null) {
					vp.addErrorMessage("Error: " + be.getCaught().getMessage());
				}
				vp.addAppMessage(Translations.instance.stateDisconnected());
			}
		});
		XmppService.get().getModulesManager().getModule(AuthModule.class).addListener(AuthModule.AuthStart,
				new Listener<AuthModule.AuthEvent>() {

					public void handleEvent(AuthEvent be) {
						vp.addAppMessage(Translations.instance.stateAuthenticating());
					}
				});
		XmppService.get().getModulesManager().getModule(AuthModule.class).addListener(AuthModule.AuthSuccess,
				new Listener<AuthModule.AuthEvent>() {

					public void handleEvent(AuthEvent be) {
						vp.addAppMessage(Translations.instance.stateAuthenticated());
					}
				});
		XmppService.get().getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.ContactAvailable,
				new Listener<PresenceEvent>() {

					public void handleEvent(PresenceEvent be) {
						vp.addAppMessage(Translations.instance.clientMessageIsAvailable(be.getJid().toString()));
					}
				});
		XmppService.get().getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.ContactUnavailable,
				new Listener<PresenceEvent>() {

					public void handleEvent(PresenceEvent be) {
						vp.addAppMessage(Translations.instance.clientMessageIsUnavailable(be.getJid().toString()));
					}
				});
		XmppService.get().getModulesManager().getModule(PresenceModule.class).addListener(
				PresenceModule.ContactChangedPresence, new Listener<PresenceEvent>() {

					public void handleEvent(PresenceEvent be) {
						RosterItem item = XmppService.get().getSessionObject().getRoster().get(be.getJid().getBareJid());
						if (item != null)
							try {
								vp.addAppMessage(Translations.instance.clientMessageIsNow(be.getJid().toString(),
										RosterPanel.getShowOfRosterItem(item).toString()));
							} catch (XMLException e) {
								e.printStackTrace();
							}
					}
				});
	}
}

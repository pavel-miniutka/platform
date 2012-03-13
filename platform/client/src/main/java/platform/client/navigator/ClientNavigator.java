package platform.client.navigator;

import platform.interop.navigator.RemoteNavigatorInterface;

import java.io.IOException;

public abstract class ClientNavigator {
    public final RemoteNavigatorInterface remoteNavigator;

    public final RelevantFormNavigatorPanel relevantFormNavigator;
    public final RelevantClassNavigatorPanel relevantClassNavigator;

    public ClientNavigator(RemoteNavigatorInterface remoteNavigator) {
        this.remoteNavigator = remoteNavigator;

        relevantFormNavigator = new RelevantFormNavigatorPanel(this);
        relevantClassNavigator = new RelevantClassNavigatorPanel(this);
    }

    public void openModalForm(ClientNavigatorForm form) throws ClassNotFoundException, IOException {
        openForm(form);
    }

    public abstract void openForm(ClientNavigatorForm element) throws IOException, ClassNotFoundException;
    public abstract void openAction(ClientNavigatorAction action);
}
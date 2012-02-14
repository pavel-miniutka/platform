package platform.client.form;

import platform.client.ClientResourceBundle;
import platform.interop.form.RemoteDialogInterface;
import platform.interop.remote.SelectedObject;

import java.awt.*;
import java.io.IOException;
import java.rmi.RemoteException;

public class ClientDialog extends ClientModalForm {
    public final static int NOT_CHOSEN = 0;
    public final static int VALUE_CHOSEN = 1;

    public int result = NOT_CHOSEN;
    public Object dialogValue;
    public Object displayValue;

    public boolean showQuickFilterOnStartup = true;

    private RemoteDialogInterface remoteDialog;

    public ClientDialog(Component owner, final RemoteDialogInterface dialog, boolean showQuickFilterOnStartup) throws IOException, ClassNotFoundException {
        super(owner, dialog, false); // обозначаем parent'а и модальность

        this.showQuickFilterOnStartup = showQuickFilterOnStartup;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Boolean undecorated = dialog.isUndecorated();
        if (undecorated == null || undecorated) {
            setResizable(false);
            // делаем, чтобы не выглядел как диалог
            setUndecorated(true);
        }
    }

    @Override
    public void windowActivatedFirstTime() {
        int initialFilterPropertyDrawID = -1;
        try {
            Integer filterPropertyDraw = remoteDialog.getInitFilterPropertyDraw();
            if (filterPropertyDraw != null) {
                initialFilterPropertyDrawID = filterPropertyDraw;
            }
        } catch (RemoteException ignored) {
        }

        if (initialFilterPropertyDrawID > 0) {
            form.selectProperty(initialFilterPropertyDrawID);
        }

        if (showQuickFilterOnStartup && initialFilterPropertyDrawID > 0) {
            form.quickEditFilter(initialFilterPropertyDrawID);
        } else {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(form.getComponent());
        }
    }

    // необходим чтобы в диалоге менять формы (панели)
    protected ClientFormController createFormController() throws IOException, ClassNotFoundException {
        remoteDialog = (RemoteDialogInterface) remoteForm;

        return new ClientFormController(remoteDialog, null, true, true, false) {
            @Override
            void nullPressed() {
                result = VALUE_CHOSEN;
                dialogValue = null;
                displayValue = null;
                hideDialog();
            }

            @Override
            public void okPressed() {
                result = VALUE_CHOSEN;
                try {
                    SelectedObject selectedObject = remoteDialog.getSelectedObject();
                    dialogValue = selectedObject.value;
                    displayValue = selectedObject.displayValue;
                } catch (RemoteException e) {
                    throw new RuntimeException(ClientResourceBundle.getString("errors.error.getting.value.of.dialogue"), e);
                }
                hideDialog();
            }

            @Override
            void closePressed() {
                hideDialog();
            }
        };
    }
}

package lsfusion.gwt.server.form.handlers;

import lsfusion.gwt.server.MainDispatchServlet;
import lsfusion.http.provider.form.FormSessionObject;
import lsfusion.gwt.server.form.FormServerResponseActionHandler;
import lsfusion.gwt.client.controller.remote.action.form.ExecuteNotification;
import lsfusion.gwt.client.controller.remote.action.form.ServerResponseResult;
import net.customware.gwt.dispatch.server.ExecutionContext;

import java.rmi.RemoteException;

public class ExecuteNotificationHandler extends FormServerResponseActionHandler<ExecuteNotification> {
    public ExecuteNotificationHandler(MainDispatchServlet servlet) {
        super(servlet);
    }

    @Override
    public ServerResponseResult executeEx(ExecuteNotification action, ExecutionContext context) throws RemoteException {
        FormSessionObject form = getFormSessionObject(action.formSessionID);
        return getServerResponseResult(form, form.remoteForm.executeNotificationAction(action.requestIndex, defaultLastReceivedRequestIndex, action.idNotification));
    }
}
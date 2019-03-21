package lsfusion.gwt.server.form.handlers;

import lsfusion.gwt.server.MainDispatchServlet;
import lsfusion.gwt.server.form.FormServerResponseActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import lsfusion.http.provider.form.FormSessionObject;
import lsfusion.gwt.client.controller.remote.action.form.ClearPropertyOrders;
import lsfusion.gwt.client.controller.remote.action.form.ServerResponseResult;

import java.rmi.RemoteException;

public class ClearPropertyOrdersHandler extends FormServerResponseActionHandler<ClearPropertyOrders> {
    public ClearPropertyOrdersHandler(MainDispatchServlet servlet) {
        super(servlet);
    }

    @Override
    public ServerResponseResult executeEx(ClearPropertyOrders action, ExecutionContext context) throws RemoteException {
        FormSessionObject form = getFormSessionObject(action.formSessionID);
        return getServerResponseResult(
                form,
                form.remoteForm.clearPropertyOrders(action.requestIndex, defaultLastReceivedRequestIndex, action.groupObjectID)
        );
    }
}

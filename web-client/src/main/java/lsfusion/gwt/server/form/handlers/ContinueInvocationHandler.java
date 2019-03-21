package lsfusion.gwt.server.form.handlers;

import lsfusion.gwt.server.MainDispatchServlet;
import lsfusion.gwt.server.form.FormServerResponseActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import lsfusion.http.provider.form.FormSessionObject;
import lsfusion.gwt.server.convert.GwtToClientConverter;
import lsfusion.gwt.client.controller.remote.action.form.ContinueInvocation;
import lsfusion.gwt.client.controller.remote.action.form.ServerResponseResult;

import java.rmi.RemoteException;

public class ContinueInvocationHandler extends FormServerResponseActionHandler<ContinueInvocation> {
    private final GwtToClientConverter gwtConverter = GwtToClientConverter.getInstance();

    public ContinueInvocationHandler(MainDispatchServlet servlet) {
        super(servlet);
    }

    @Override
    public ServerResponseResult executeEx(ContinueInvocation action, ExecutionContext context) throws RemoteException {
        FormSessionObject form = getFormSessionObject(action.formSessionID);

        Object actionResults[] = new Object[action.actionResults.length];
        for (int i = 0; i < actionResults.length; ++i) {
            actionResults[i] = gwtConverter.convertOrCast(action.actionResults[i]);
        }

        return getServerResponseResult(form, form.remoteForm.continueServerInvocation(-1, defaultLastReceivedRequestIndex, -1, actionResults));
    }
}

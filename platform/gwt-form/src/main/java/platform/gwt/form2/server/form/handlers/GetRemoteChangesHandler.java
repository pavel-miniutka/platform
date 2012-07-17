package platform.gwt.form2.server.form.handlers;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;
import platform.gwt.base.server.FormSessionObject;
import platform.gwt.form2.server.RemoteServiceImpl;
import platform.gwt.form2.shared.actions.form.GetRemoteChanges;
import platform.gwt.form2.shared.actions.form.ServerResponseResult;

import java.io.IOException;

public class GetRemoteChangesHandler extends ServerResponseActionHandler<GetRemoteChanges> {
    public GetRemoteChangesHandler(RemoteServiceImpl servlet) {
        super(servlet);
    }

    @Override
    public ServerResponseResult executeEx(GetRemoteChanges action, ExecutionContext context) throws DispatchException, IOException {
        FormSessionObject form = getFormSessionObject(action.formSessionID);
        return getServerResponseResult(form, form.remoteForm.getRemoteChanges(action.requestIndex));
    }
}

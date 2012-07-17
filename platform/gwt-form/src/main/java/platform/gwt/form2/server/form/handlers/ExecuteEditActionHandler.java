package platform.gwt.form2.server.form.handlers;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;
import platform.gwt.base.server.FormSessionObject;
import platform.gwt.form2.server.RemoteServiceImpl;
import platform.gwt.form2.shared.actions.form.ExecuteEditAction;
import platform.gwt.form2.shared.actions.form.ServerResponseResult;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import static platform.base.BaseUtils.serializeObject;

public class ExecuteEditActionHandler extends ServerResponseActionHandler<ExecuteEditAction> {
    public ExecuteEditActionHandler(RemoteServiceImpl servlet) {
        super(servlet);
    }

    @Override
    public ServerResponseResult executeEx(ExecuteEditAction action, ExecutionContext context) throws DispatchException, IOException {
        FormSessionObject form = getFormSessionObject(action.formSessionID);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream outStream = new DataOutputStream(byteStream);

        outStream.writeInt(action.key.size());
        for (Map.Entry<Integer, Object> entry : action.key.entrySet()) {
            outStream.writeInt(entry.getKey());
            serializeObject(outStream, entry.getValue());
        }

        return getServerResponseResult(form, form.remoteForm.executeEditAction(action.requestIndex, action.propertyId, byteStream.toByteArray(), action.actionSID));
    }
}

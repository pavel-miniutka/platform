package lsfusion.server.logics.form.interactive.dialogedit;

import lsfusion.server.data.sql.exception.SQLHandledException;
import lsfusion.server.data.value.ObjectValue;
import lsfusion.server.logics.form.interactive.instance.FormInstance;
import lsfusion.server.logics.form.interactive.instance.object.ObjectInstance;
import lsfusion.server.logics.form.struct.object.ObjectEntity;

import java.sql.SQLException;

public abstract class DialogRequestAdapter implements DialogRequest {
    protected ObjectEntity dialogObject;
    private ObjectInstance dialogObjectInstance;

    public ObjectValue getValue() {
        assert dialogObjectInstance != null;
        return dialogObjectInstance.getObjectValue();
    }

    public final FormInstance createDialog() throws SQLException, SQLHandledException {
        FormInstance result = doCreateDialog();
        if (result != null) {
            dialogObjectInstance = result.instanceFactory.getInstance(dialogObject);
        }
        return result;
    }

    protected abstract FormInstance doCreateDialog() throws SQLException, SQLHandledException;
}

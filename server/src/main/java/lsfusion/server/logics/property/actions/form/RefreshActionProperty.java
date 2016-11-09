package lsfusion.server.logics.property.actions.form;

import lsfusion.server.data.SQLHandledException;
import lsfusion.server.logics.BaseLogicsModule;
import lsfusion.server.logics.property.ClassPropertyInterface;
import lsfusion.server.logics.property.ExecutionContext;

import java.sql.SQLException;

public class RefreshActionProperty extends FormToolbarActionProperty {

    public RefreshActionProperty(BaseLogicsModule lm) {
        super(lm, false);
    }

    public void executeCustom(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        if (context.getFormInstance() != null)
            context.getFormInstance().formRefresh();
    }

    @Override
    public boolean ignoreReadOnlyPolicy() {
        return true;
    }
}

package lsfusion.server.physics.admin.service;

import lsfusion.server.logics.classes.ValueClass;
import lsfusion.server.data.SQLHandledException;
import lsfusion.server.physics.exec.DBManager;
import lsfusion.server.logics.ServiceLogicsModule;
import lsfusion.server.logics.property.ClassPropertyInterface;
import lsfusion.server.logics.action.ExecutionContext;
import lsfusion.server.language.ScriptingActionProperty;

import java.sql.Connection;
import java.sql.SQLException;

public class SetRepeatableReadDisableTILModeActionProperty extends ScriptingActionProperty {

    public SetRepeatableReadDisableTILModeActionProperty(ServiceLogicsModule LM, ValueClass... classes) {
        super(LM, classes);
    }

    protected void executeCustom(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        Object value = context.getSingleKeyObject();
        if(value!=null)
            DBManager.SESSION_TIL =  - 1;
        else
            DBManager.SESSION_TIL = Connection.TRANSACTION_REPEATABLE_READ;
    }

    @Override
    protected boolean allowNulls() {
        return true;
    }
}
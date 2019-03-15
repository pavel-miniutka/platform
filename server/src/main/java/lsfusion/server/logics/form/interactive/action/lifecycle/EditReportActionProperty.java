package lsfusion.server.logics.form.interactive.action.lifecycle;

import lsfusion.interop.action.RunEditReportClientAction;
import lsfusion.server.data.SQLHandledException;
import lsfusion.server.language.property.LP;
import lsfusion.server.logics.BaseLogicsModule;
import lsfusion.server.logics.action.controller.context.ExecutionContext;
import lsfusion.server.logics.form.struct.FormEntity;
import lsfusion.server.logics.property.Property;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;

import java.sql.SQLException;

public class EditReportActionProperty extends ReportClientActionProperty {
    private static LP showIf = createShowIfProperty(new Property[] {FormEntity.isDebug, FormEntity.isFloat}, new boolean[] {false, true});

    public EditReportActionProperty(BaseLogicsModule lm) {
        super(lm, false);
    }

    public void executeCustom(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        context.delayUserInterfaction(new RunEditReportClientAction(context.getFormInstance(true, true).getCustomReportPathList()));
    }

    @Override
    protected LP getShowIf() {
        return showIf;
    }
}

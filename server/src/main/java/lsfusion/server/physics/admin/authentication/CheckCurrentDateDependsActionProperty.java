package lsfusion.server.physics.admin.authentication;

import lsfusion.interop.action.ConfirmClientAction;
import lsfusion.server.logics.classes.ValueClass;
import lsfusion.server.data.SQLHandledException;
import lsfusion.server.logics.BusinessLogics;
import lsfusion.server.data.DataObject;
import lsfusion.server.logics.property.CalcProperty;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.logics.action.ExecutionContext;
import lsfusion.server.logics.property.oraction.ActionOrProperty;
import lsfusion.server.language.ScriptingActionProperty;

import javax.swing.*;
import java.sql.SQLException;
import java.util.Iterator;

public class CheckCurrentDateDependsActionProperty extends ScriptingActionProperty {

    private final ClassPropertyInterface propertyInterface;

    public CheckCurrentDateDependsActionProperty(SecurityLogicsModule LM, ValueClass... classes) {
        super(LM, classes);
        Iterator<ClassPropertyInterface> i = interfaces.iterator();
        propertyInterface = i.next();
    }

    @Override
    public void executeCustom(final ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {

        BusinessLogics BL = context.getBL();

        DataObject propertyObject = context.getDataKeyValue(propertyInterface);

        boolean allow = true;
        String dbNameProperty = (String) BL.reflectionLM.dbNameProperty.read(context, propertyObject);
        if (dbNameProperty != null) {
            for (ActionOrProperty property : context.getBL().getPropertyList())
                if (dbNameProperty.equals(property.getDBName())) {
                    if (property instanceof CalcProperty && ((CalcProperty) property).getRecDepends().contains(BL.timeLM.currentDate.property)) {
                        allow = JOptionPane.YES_OPTION == (Integer) context.requestUserInteraction(
                                new ConfirmClientAction("Свойство зависит от текущей даты",
                                        String.format("Свойство %s зависит от текущей даты. Вы уверены, что хотите его залогировать?", property.getDBName())));
                    }
                    break;
                }
        }
        if (!allow) {
            try(ExecutionContext.NewSession<ClassPropertyInterface> newContext = context.newSession()) {
                BL.reflectionLM.userLoggableProperty.change((Boolean) null, newContext, propertyObject);
                newContext.apply();
            }
        }
    }
}
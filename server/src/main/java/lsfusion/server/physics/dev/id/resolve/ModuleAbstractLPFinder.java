package lsfusion.server.physics.dev.id.resolve;

import lsfusion.server.language.property.LP;
import lsfusion.server.logics.LogicsModule;
import lsfusion.server.logics.property.Property;
import lsfusion.server.logics.property.cases.CaseUnionProperty;
import lsfusion.server.logics.property.oraction.ActionOrProperty;

public class ModuleAbstractLPFinder extends ModuleAbstractLAPFinder<LP<?>> {
    
    @Override
    protected boolean isAbstract(ActionOrProperty property) {
        assert property instanceof Property;
        return property instanceof CaseUnionProperty && ((CaseUnionProperty) property).isAbstract();
    }

    @Override
    protected Iterable<LP<?>> getSourceList(LogicsModule module, String name) {
        return module.getNamedProperties(name);
    }
}

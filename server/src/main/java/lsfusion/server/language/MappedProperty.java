package lsfusion.server.language;

import lsfusion.base.col.interfaces.immutable.ImOrderSet;
import lsfusion.server.logics.form.struct.object.ObjectEntity;
import lsfusion.server.language.linear.LP;
import lsfusion.server.logics.property.oraction.ActionOrProperty;
import lsfusion.server.logics.property.oraction.PropertyInterface;

public final class MappedProperty {
    public LP<PropertyInterface, ?> property;
    public ImOrderSet<ObjectEntity> mapping;

    public MappedProperty(LP property, ImOrderSet<ObjectEntity> mapping) {
        this.property = property;
        this.mapping = mapping;
    }

    public ActionOrProperty<PropertyInterface> getProperty() {
        return property.property;
    }
}
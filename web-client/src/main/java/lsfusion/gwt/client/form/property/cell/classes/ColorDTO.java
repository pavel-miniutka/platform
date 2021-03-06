package lsfusion.gwt.client.form.property.cell.classes;

import java.io.Serializable;

public class ColorDTO implements Serializable {
    public String value;

    @SuppressWarnings("UnusedDeclaration")
    public ColorDTO() {}

    public ColorDTO(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}

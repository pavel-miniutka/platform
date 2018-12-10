package lsfusion.gwt.shared.view.classes;

import com.google.gwt.i18n.client.DateTimeFormat;
import lsfusion.gwt.client.MainFrameMessages;
import lsfusion.gwt.client.form.ui.grid.EditManager;
import lsfusion.gwt.client.form.ui.grid.editor.DateGridCellEditor;
import lsfusion.gwt.client.form.ui.grid.editor.GridCellEditor;
import lsfusion.gwt.client.form.ui.grid.renderer.DateGridCellRenderer;
import lsfusion.gwt.client.form.ui.grid.renderer.GridCellRenderer;
import lsfusion.gwt.shared.GwtSharedUtils;
import lsfusion.gwt.shared.view.GEditBindingMap;
import lsfusion.gwt.shared.view.GPropertyDraw;
import lsfusion.gwt.shared.view.changes.dto.GDateDTO;

import java.text.ParseException;
import java.util.Date;

import static lsfusion.gwt.shared.GwtSharedUtils.getDateFormat;

public class GDateType extends GFormatType<DateTimeFormat> {

    public static GDateType instance = new GDateType();

    @Override
    public DateTimeFormat getFormat(String pattern) {
        return GwtSharedUtils.getDateFormat(pattern);
    }

    @Override
    public GridCellEditor createGridCellEditor(EditManager editManager, GPropertyDraw editProperty) {
        return new DateGridCellEditor(editManager, editProperty);
    }

    @Override
    public GDateDTO parseString(String value, String pattern) throws ParseException {
        return value.isEmpty() ? null : GDateDTO.fromDate(parseDate(value, getDateFormat(pattern)));
    }

    @Override
    public GridCellRenderer createGridCellRenderer(GPropertyDraw property) {
        return new DateGridCellRenderer(property);
    }

    @Override
    protected Object getDefaultWidthValue() {
        return GDateTimeType.getWideFormattableDateTime();
    }

    @Override
    public String toString() {
        return MainFrameMessages.Instance.get().typeDateCaption();
    }

    @Override
    public GEditBindingMap.EditEventFilter getEditEventFilter() {
        return GEditBindingMap.numberEventFilter;
    }

    public static Date parseDate(String value, DateTimeFormat... formats) throws ParseException {
        for (DateTimeFormat format : formats) {
            try {
                return format.parse(value);
            } catch (IllegalArgumentException ignore) {
            }
        }
        throw new ParseException("string " + value + "can not be converted to date", 0);
    }
}

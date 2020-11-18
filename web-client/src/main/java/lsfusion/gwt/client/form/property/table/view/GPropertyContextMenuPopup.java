package lsfusion.gwt.client.form.property.table.view;

import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import lsfusion.gwt.client.ClientMessages;
import lsfusion.gwt.client.base.GwtClientUtils;
import lsfusion.gwt.client.base.view.PopupDialogPanel;
import lsfusion.gwt.client.classes.data.GIntegralType;
import lsfusion.gwt.client.form.controller.GFormController;
import lsfusion.gwt.client.form.object.table.grid.controller.GGridController;
import lsfusion.gwt.client.form.property.GPropertyDraw;

import java.util.LinkedHashMap;
import java.util.Map;

public class GPropertyContextMenuPopup {
    private static final ClientMessages messages = ClientMessages.Instance.get();

    public interface ItemSelectionListener {
        void onMenuItemSelected(String actionSID);
    }

    public static void show(GFormController formController, GPropertyDraw property, int x, int y, final ItemSelectionListener selectionListener) {
        if (property == null) {
            return;
        }

        LinkedHashMap<String, String> contextMenuItems = property.getContextMenuItems();

        boolean hasContextMenuItems = contextMenuItems != null && !contextMenuItems.isEmpty();
        boolean showCountQuantity = property.groupObject != null && property.groupObject.toolbar.showCountQuantity;
        boolean showCalculateSum = property.groupObject != null && property.groupObject.toolbar.showCalculateSum;

        if (hasContextMenuItems || showCountQuantity || showCalculateSum) {
            final PopupDialogPanel popup = new PopupDialogPanel();
            final MenuBar menuBar = new MenuBar(true);

            if (hasContextMenuItems) {
                for (final Map.Entry<String, String> item : contextMenuItems.entrySet()) {
                    final String actionSID = item.getKey();
                    String caption = item.getValue();
                    MenuItem menuItem = new MenuItem(caption, () -> {
                        popup.hide();
                        selectionListener.onMenuItemSelected(actionSID);
                    });

                    menuBar.addItem(menuItem);
                }
            }

            if (showCountQuantity) {
                menuBar.addItem(new MenuItem(messages.formQueriesNumberOfEntries(), () -> {
                    popup.hide();
                    formController.countRecords(property.groupObject, x, y);
                }));
            }

            if (showCalculateSum) {
                menuBar.addItem(new MenuItem(messages.formQueriesCalculateSum(), () -> {
                    popup.hide();
                    GGridController gridController = formController.getGridController(property.groupObject);
                    if (property.baseType instanceof GIntegralType) {
                        formController.calculateSum(property.groupObject, property, gridController.getTableCurrentColumnKey(), x, y);
                    } else {
                        gridController.showSum(null, property, x, y);
                    }
                }));
            }

            GwtClientUtils.showPopupInWindow(popup, menuBar, x, y);
        }
    }
}

package platform.client.form;

import platform.client.form.grid.GridController;
import platform.client.form.panel.PanelController;
import platform.client.form.showtype.ShowTypeController;
import platform.client.logics.*;
import platform.interop.ClassViewType;
import platform.interop.Order;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupObjectController implements GroupObjectLogicsSupplier {

    private final ClientGroupObjectImplementView groupObject;
    private final LogicsSupplier logicsSupplier;
    private final ClientForm form;

    private final PanelController panel;
    private GridController grid;
    private ShowTypeController showType;
    private final Map<ClientObjectImplementView, ObjectController> objects = new HashMap<ClientObjectImplementView, ObjectController>();

    private ClientGroupObjectValue currentObject;

    private Byte classView;

    public GroupObjectController(ClientGroupObjectImplementView igroupObject, LogicsSupplier ilogicsSupplier, ClientForm iform, ClientFormLayout formLayout) throws IOException {

        groupObject = igroupObject;
        logicsSupplier = ilogicsSupplier; 
        form = iform;

        panel = new PanelController(this, form, formLayout) {

            protected void addGroupObjectActions(JComponent comp) {
                GroupObjectController.this.addGroupObjectActions(comp);
            }
        };

        if (groupObject != null) {

            // Grid идет как единый неделимый JComponent, поэтому смысла передавать туда FormLayout нет
            grid = new GridController(groupObject.gridView, this, form);
            addGroupObjectActions(grid.getView());

            grid.addView(formLayout);

            for (ClientObjectImplementView object : groupObject) {

                objects.put(object, new ObjectController(object, form));
                objects.get(object).addView(formLayout);
            }

            showType = new ShowTypeController(groupObject.showTypeView, this, form) {

                protected void needToBeShown() {
                    GroupObjectController.this.showViews();
                }

                protected void needToBeHidden() {
                    GroupObjectController.this.hideViews();
                }
            };

            showType.setBanClassView(groupObject.banClassView);

            showType.addView(formLayout);
        }

    }

    private void hideViews() {

        panel.hideViews();

        if (grid != null)
            grid.hideViews();

        if (groupObject != null)
            for (ClientObjectImplementView object : groupObject)
                objects.get(object).hideViews();

        if (showType != null)
            showType.hideViews();

        // нет смысла вызывать validate или invalidate, так как setVisible услышит сам SimplexLayout и сделает главному контейнеру invalidate
    }

    private void showViews() {

        panel.showViews();

        if (grid != null)
            grid.showViews();

        if (groupObject != null)
            for (ClientObjectImplementView object : groupObject)
                objects.get(object).showViews();

        if (showType != null)
            showType.showViews();

    }

    public void setClassView(Byte setClassView) {

        if (classView == null || !classView.equals(setClassView)) {

            classView = setClassView;
            if (classView.equals(ClassViewType.GRID)) {
                panel.removeGroupObjectCells();
                grid.addGroupObjectCells();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        grid.requestFocusInWindow();
                    }
                });
            } else if (classView.equals(ClassViewType.PANEL)) {
                panel.addGroupObjectCells();
                grid.removeGroupObjectCells();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        panel.requestFocusInWindow();
                    }
                });
//                    panel.requestFocusInWindow();
            } else {
                panel.removeGroupObjectCells();
                grid.removeGroupObjectCells();
            }


            for (ClientObjectImplementView object : groupObject) {
                objects.get(object).changeClassView(classView);
            }

            if (showType != null)
                showType.changeClassView(classView);
        }

    }

    public void addPanelProperty(ClientPropertyView property) {

        if (grid != null)
            grid.removeProperty(property);
        
        panel.addProperty(property);

    }

    public void addGridProperty(ClientPropertyView property) {

        panel.removeProperty(property);
        grid.addProperty(property);

    }

    public void dropProperty(ClientPropertyView property) {

        panel.removeProperty(property);
        grid.removeProperty(property);

    }

    public ClientGroupObjectValue getCurrentObject() {
        return currentObject;
    }

    public void setGridObjects(List<ClientGroupObjectValue> gridObjects) {
        grid.setGridObjects(gridObjects);
    }

    public void setGridClasses(List<ClientGroupObjectClass> gridClasses) {
        grid.setGridClasses(gridClasses);
    }

    public void setCurrentGroupObject(ClientGroupObjectValue value, Boolean userChange) {

        boolean realChange = !value.equals(currentObject);

/*            if (currentObject != null)
            System.out.println("view - oldval : " + currentObject.toString() + " ; userChange " + userChange.toString() );
        if (value != null)
            System.out.println("view - newval : " + value.toString() + " ; userChange " + userChange.toString());*/

        currentObject = value;

        if (realChange) {

            panel.selectObject(currentObject);
            if (!userChange) // если не сам изменил, а то пойдет по кругу
                grid.selectObject(currentObject);
        }

    }

    public void setCurrentObject(ClientObjectImplementView object, Object value) {

        if (currentObject == null) return;

        ClientGroupObjectValue curValue = (ClientGroupObjectValue) currentObject.clone();

        curValue.put(object, value);
        setCurrentGroupObject(curValue, false);
    }

    public void setCurrentGroupObjectClass(ClientGroupObjectClass value) {
        panel.setCurrentClass(value);
    }

    public void setPanelPropertyValue(ClientPropertyView property, Object value) {

        panel.setPropertyValue(property, value);
    }

    public void setGridPropertyValues(ClientPropertyView property, Map<ClientGroupObjectValue,Object> values) {

        grid.setPropertyValues(property, values);
    }

    public void changeGridOrder(ClientCellView property, Order modiType) throws IOException {
        grid.changeGridOrder(property, modiType);
    }
    
    // приходится делать именно так, так как логика отображения одного GroupObject може не совпадать с логикой Container-Component
    void addGroupObjectActions(JComponent comp) {

        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_DOWN_MASK), "switchClassView");
        comp.getActionMap().put("switchClassView", new AbstractAction() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    form.switchClassView(groupObject);
                } catch (IOException e) {
                    throw new RuntimeException("Ошибка при изменении вида", e);
                }
            }
        });

        // вот так вот приходится делать, чтобы "узнавать" к какому GroupObject относится этот Component
        comp.putClientProperty("groupObject", groupObject);

    }

    // реализация GroupObjectLogicsSupplier

    public List<ClientObjectImplementView> getObjects() {
        return logicsSupplier.getObjects();
    }

    public List<ClientPropertyView> getProperties() {
        return logicsSupplier.getProperties();
    }

    public List<ClientCellView> getCells() {
        return logicsSupplier.getCells();
    }

    public ClientGroupObjectImplementView getGroupObject() {
        return groupObject;
    }

    public List<ClientPropertyView> getGroupObjectProperties() {

        ArrayList<ClientPropertyView> properties = new ArrayList<ClientPropertyView>();
        for (ClientPropertyView property : getProperties()) {
            if (groupObject.equals(property.groupObject))
                properties.add(property);
        }

        return properties;
    }

    public ClientPropertyView getDefaultProperty() {

        ClientCellView currentCell = grid.getCurrentCell();
        if (currentCell instanceof ClientPropertyView)
            return (ClientPropertyView) currentCell;
        else
            return null;
    }

    public Object getSelectedValue(ClientPropertyView cell) {
        return grid.getSelectedValue(cell);
    }

    public ClientForm getForm() {
        return form;
    }

    public String getSaveMessage() {

        String message = "";
        for (ClientObjectImplementView object : groupObject) {
            if (object.addOnTransaction) {
                message += "Создать новый " + object.caption + " ?";
            }
        }

        return message;
    }
}
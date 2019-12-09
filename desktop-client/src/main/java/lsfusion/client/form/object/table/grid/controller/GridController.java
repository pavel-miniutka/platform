package lsfusion.client.form.object.table.grid.controller;

import com.google.common.base.Throwables;
import lsfusion.base.BaseUtils;
import lsfusion.base.col.heavy.OrderedMap;
import lsfusion.client.ClientResourceBundle;
import lsfusion.client.base.view.FlatRolloverButton;
import lsfusion.client.classes.data.ClientIntegralClass;
import lsfusion.client.controller.remote.RmiQueue;
import lsfusion.client.form.ClientFormChanges;
import lsfusion.client.form.controller.ClientFormController;
import lsfusion.client.form.design.view.ClientFormLayout;
import lsfusion.client.form.filter.user.FilterView;
import lsfusion.client.form.filter.user.controller.FilterController;
import lsfusion.client.form.object.ClientGroupObject;
import lsfusion.client.form.object.ClientGroupObjectValue;
import lsfusion.client.form.object.panel.controller.PanelController;
import lsfusion.client.form.object.panel.controller.PropertyController;
import lsfusion.client.form.object.table.controller.AbstractTableController;
import lsfusion.client.form.object.table.grid.user.design.GridUserPreferences;
import lsfusion.client.form.object.table.grid.user.design.view.UserPreferencesDialog;
import lsfusion.client.form.object.table.grid.user.toolbar.view.*;
import lsfusion.client.form.object.table.grid.view.ClientTableView;
import lsfusion.client.form.object.table.grid.view.GridTable;
import lsfusion.client.form.object.table.grid.view.GridView;
import lsfusion.client.form.property.ClientPropertyDraw;
import lsfusion.client.form.property.ClientPropertyReader;
import lsfusion.client.form.property.cell.classes.view.link.ImageLinkPropertyRenderer;
import lsfusion.client.view.MainFrame;
import lsfusion.interop.form.UpdateMode;
import lsfusion.interop.form.object.table.grid.user.design.GroupObjectUserPreferences;
import lsfusion.interop.form.object.table.grid.user.toolbar.FormGrouping;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static lsfusion.client.ClientResourceBundle.getString;

public class GridController extends AbstractTableController {
    private static final ImageIcon PRINT_XLS_ICON = new ImageIcon(FilterView.class.getResource("/images/excelbw.png"));
    private static final ImageIcon PRINT_GROUP_ICON = new ImageIcon(FilterView.class.getResource("/images/reportbw.png"));
    private static final ImageIcon GROUP_CHANGE_ICON = new ImageIcon(FilterView.class.getResource("/images/groupchange.png"));
    public static final ImageIcon USER_PREFERENCES_ICON = new ImageIcon(FilterView.class.getResource("/images/userPreferences.png"));
    private static final ImageIcon UPDATE_ICON = new ImageIcon(FilterView.class.getResource("/images/update.png"));
    private static final ImageIcon OK_ICON = new ImageIcon(FilterView.class.getResource("/images/ok.png"));

    private final ClientGroupObject groupObject;

    private GridView view;
    public ClientTableView table;

    protected CalculationsView calculationsView;

    private ToolbarGridButton userPreferencesButton;
    private ToolbarGridButton manualUpdateTableButton;
    private FlatRolloverButton forceUpdateTableButton;

    private boolean forceHidden = false;

    public GridController(ClientGroupObject igroupObject, ClientFormController formController, final ClientFormLayout formLayout, GridUserPreferences[] userPreferences) {
        super(formController, formLayout, igroupObject == null ? null : igroupObject.toolbar);
        groupObject = igroupObject;

        panel = new PanelController(GridController.this.formController, formLayout) {
            protected void addGroupObjectActions(final JComponent comp) {
                GridController.this.registerGroupObject(comp);
                if(filter != null) {
                    filter.getView().addActionsToPanelInputMap(comp);
                }
            }
        };

        if (groupObject != null) {
            calculationsView = new CalculationsView();
            formLayout.add(groupObject.calculations, calculationsView);
            
            if (groupObject.filter.visible) {
                filter = new FilterController(this, groupObject.filter) {
                    protected void remoteApplyQuery() {
                        RmiQueue.runAction(() -> {
                            try {
                                GridController.this.formController.changeFilter(groupObject, getConditions());
                                table.requestFocusInWindow();
                            } catch (IOException e) {
                                throw new RuntimeException(ClientResourceBundle.getString("errors.error.applying.filter"), e);
                            }
                        });
                    }
                };

                filter.addView(formLayout);
            }

            view = new GridView(this, formController, userPreferences, groupObject.grid.tabVertical, groupObject.grid.groupObject.needVerticalScroll);
            table = view.getTable();

            registerGroupObject(view);
            if (filter != null && table instanceof GridTable) {
                filter.getView().addActionsToInputMap((GridTable) table);
            }

            formLayout.add(groupObject.grid, view);

            configureToolbar();
        }

        update(null);
    }

    private void configureToolbar() {
        if (filter != null) {
            addToToolbar(filter.getToolbarButton());
        }

        if (groupObject.toolbar.showGroupChange && table instanceof GridTable) {
            addToolbarSeparator();
            ToolbarGridButton groupChangeButton = new ToolbarGridButton(GROUP_CHANGE_ICON, getString("form.grid.group.groupchange") + " (F12)") {
                @Override
                public void addListener() {
                    addActionListener(e -> {
                        table.groupChange();
                    });

                    ((GridTable) table).addFocusListener(new FocusListener() {
                        @Override
                        public void focusGained(FocusEvent e) {
                            setEnabled(true);
                        }

                        @Override
                        public void focusLost(FocusEvent e) {
                            setEnabled(false);
                        }
                    });
                }
            };
            groupChangeButton.setEnabled(false);
            addToToolbar(groupChangeButton);
        }

        if (groupObject.toolbar.showCountRows || groupObject.toolbar.showCalculateSum || groupObject.toolbar.showGroupReport) {
            addToolbarSeparator();
        }

        if (groupObject.toolbar.showCountRows || groupObject.toolbar.showCalculateSum || groupObject.toolbar.showGroupReport) {
            addToolbarSeparator();
        }

        if (groupObject.toolbar.showCountRows) {
            addToToolbar(new CountQuantityButton() {
                public void addListener() {
                    addActionListener(e -> RmiQueue.runAction(() -> {
                        try {
                            showPopupMenu(formController.countRecords(getGroupObject().getID()));
                        } catch (Exception ex) {
                            throw Throwables.propagate(ex);
                        }
                    }));
                }
            });
        }

        if (groupObject.toolbar.showCalculateSum && table instanceof GridTable) {
            addToToolbar(new CalculateSumButton() {
                public void addListener() {
                    addActionListener(e -> RmiQueue.runAction(() -> {
                        try {
                            ClientPropertyDraw property = table.getCurrentProperty();
                            String caption = property.getCaption();
                            if (property.baseType instanceof ClientIntegralClass) {
                                ClientGroupObjectValue columnKey = ((GridTable) table).getTableModel().getColumnKey(Math.max(((GridTable) table).getSelectedColumn(), 0));
                                Object sum = formController.calculateSum(property.getID(), columnKey.serialize());
                                showPopupMenu(caption, sum);
                            } else {
                                showPopupMenu(caption, null);
                            }
                        } catch (Exception ex) {
                            throw Throwables.propagate(ex);
                        }
                    }));
                }
            });
        }

        if (groupObject.toolbar.showPrint || groupObject.toolbar.showXls) {
            addToolbarSeparator();
        }

        if (groupObject.toolbar.showPrint) {
            addToToolbar(new ToolbarGridButton(PRINT_GROUP_ICON, getString("form.grid.print.grid")) {
                @Override
                public void addListener() {
                    addActionListener(e -> RmiQueue.runAction(() -> formController.runSingleGroupReport(GridController.this)));
                }
            });
        }

        if (groupObject.toolbar.showXls) {
            addToToolbar(new ToolbarGridButton(PRINT_XLS_ICON, getString("form.grid.export.to.xlsx")) {
                @Override
                public void addListener() {
                    addActionListener(e -> RmiQueue.runAction(() -> formController.runSingleGroupXlsExport(GridController.this)));
                }
            });
        }

        if (groupObject.toolbar.showGroupReport && table instanceof GridTable) {
            addToolbarSeparator();
            addToToolbar(new GroupingButton((GridTable) table) {
                @Override
                public List<FormGrouping> readGroupings() {
                    return formController.readGroupings(getGroupObject().getSID());
                }

                @Override
                public Map<List<Object>, List<Object>> groupData(Map<Integer, List<byte[]>> groupMap, Map<Integer,
                        List<byte[]>> sumMap, Map<Integer, List<byte[]>> maxMap, boolean onlyNotNull) {
                    return formController.groupData(groupMap, sumMap, maxMap, onlyNotNull);
                }

                @Override
                public void savePressed(FormGrouping grouping) {
                    formController.saveGrouping(grouping);
                }
            });
        }

        if (groupObject.toolbar.showSettings && table instanceof GridTable) {
            addToolbarSeparator();
            ((GridTable) table).getTableHeader().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    for (int i = 0; i < ((GridTable) table).getTableModel().getColumnCount(); ++i) {
                        ((GridTable) table).setUserWidth(((GridTable) table).getTableModel().getColumnProperty(i), ((GridTable) table).getColumnModel().getColumn(i).getWidth());
                    }
                }
            });
            userPreferencesButton = new ToolbarGridButton(USER_PREFERENCES_ICON, getUserPreferencesButtonTooltip());
            userPreferencesButton.showBackground(table.hasUserPreferences());

            userPreferencesButton.addActionListener(e -> {
                if(table instanceof GridTable) {
                    UserPreferencesDialog dialog = new UserPreferencesDialog(MainFrame.instance, (GridTable) table, this, getFormController().hasCanonicalName()) {
                        @Override
                        public void preferencesChanged() {
                            RmiQueue.runAction(() -> {
                                userPreferencesButton.showBackground((((GridTable) table).generalPreferencesSaved() || ((GridTable) table).userPreferencesSaved()));
                                userPreferencesButton.setToolTipText(getUserPreferencesButtonTooltip());
                            });
                        }
                    };
                    dialog.setVisible(true);
                }
            });

            addToToolbar(userPreferencesButton);
        }

        manualUpdateTableButton = new ToolbarGridButton(UPDATE_ICON, getString("form.grid.manual.update")) {
            @Override
            public void addListener() {
                addActionListener(e -> RmiQueue.runAction(() -> {
                    setUpdateMode(!manual);
                    formController.changeMode(groupObject, manual ? UpdateMode.MANUAL : UpdateMode.AUTO);
                }));
            }
        };
        addToToolbar(manualUpdateTableButton);

        forceUpdateTableButton = new FlatRolloverButton(getString("form.grid.update"), OK_ICON);
        forceUpdateTableButton.setAlignmentY(Component.TOP_ALIGNMENT);
        forceUpdateTableButton.addActionListener(e -> RmiQueue.runAction(() -> {
            formController.changeMode(groupObject, UpdateMode.FORCE);
        }));
        forceUpdateTableButton.setFocusable(false);
        forceUpdateTableButton.setVisible(false);
        addToToolbar(forceUpdateTableButton);
    }

    private boolean manual;
    private void setUpdateMode(boolean manual) {
        this.manual = manual;
        if(manual) {
            forceUpdateTableButton.setVisible(true);
            forceUpdateTableButton.setEnabled(false);
        } else
            forceUpdateTableButton.setVisible(false);
        manualUpdateTableButton.showBackground(manual);
    }

    private String getUserPreferencesButtonTooltip() {
        String tooltip = getString("form.grid.preferences") + " (";
        if (((GridTable) table).userPreferencesSaved()) {
            tooltip += getString("form.grid.preferences.saved.for.current.users");
        } else if (((GridTable) table).generalPreferencesSaved()) {
            tooltip += getString("form.grid.preferences.saved.for.all.users");
        } else {
            tooltip += getString("form.grid.preferences.not.saved");
        }
        return tooltip + ")";
    }

    public void processFormChanges(ClientFormChanges fc,
                                   Map<ClientGroupObject, List<ClientGroupObjectValue>> cachedGridObjects
    ) {

        // Сначала меняем виды объектов
        for (ClientPropertyReader read : fc.properties.keySet()) // интересуют только свойства
        {
            if (read instanceof ClientPropertyDraw) {
                ClientPropertyDraw property = (ClientPropertyDraw) read;
                if (property.groupObject == groupObject && property.shouldBeDrawn(formController) && !fc.updateProperties.contains(property)) {
                    ImageLinkPropertyRenderer.clearChache(property);

                    addDrawProperty(property);

                    OrderedMap<ClientGroupObject, List<ClientGroupObjectValue>> groupColumnKeys = new OrderedMap<>();
                    for (ClientGroupObject columnGroupObject : property.columnGroupObjects) {
                        if (cachedGridObjects.containsKey(columnGroupObject)) {
                            groupColumnKeys.put(columnGroupObject, cachedGridObjects.get(columnGroupObject));
                        }
                    }

                    updateDrawColumnKeys(property, ClientGroupObject.mergeGroupValues(groupColumnKeys));
                }
            }
        }

        for (ClientPropertyDraw property : fc.dropProperties) {
            if (property.groupObject == groupObject) {
                dropProperty(property);
            }
        }

        if (isGrid()) {
            if (fc.gridObjects.containsKey(groupObject)) {
                setRowKeysAndCurrentObject(fc.gridObjects.get(groupObject), fc.objects.get(groupObject));
            }
        }

        // Затем их свойства
        for (Map.Entry<ClientPropertyReader, Map<ClientGroupObjectValue, Object>> readProperty : fc.properties.entrySet()) {
            ClientPropertyReader propertyRead = readProperty.getKey();
            if (propertyRead.getGroupObject() == groupObject && propertyRead.shouldBeDrawn(formController)) {
                propertyRead.update(readProperty.getValue(), fc.updateProperties.contains(propertyRead), this);
            }
        }

        Boolean updateState = null;
        if(isGrid())
            updateState = fc.updateStateObjects.get(groupObject);

        update(updateState);
    }

    public void addDrawProperty(ClientPropertyDraw property) {
        if (property.grid) {
            table.addProperty(property);
        } else {
            panel.addProperty(property);
        }
    }

    public void dropProperty(ClientPropertyDraw property) {
        if (table != null) {
            table.removeProperty(property);
        }

        panel.removeProperty(property);
    }

    public void setRowKeysAndCurrentObject(List<ClientGroupObjectValue> gridObjects, ClientGroupObjectValue newCurrentObject) {
        table.setRowKeysAndCurrentObject(gridObjects, newCurrentObject);
    }

    public void modifyGroupObject(ClientGroupObjectValue gridObject, boolean add, int position) {
        assert isGrid();

        table.modifyGroupObject(gridObject, add, position); // assert что grid!=null

        updateTable(null);
    }

    public ClientGroupObjectValue getCurrentObject() {
        return table != null && table.getCurrentObject() != null ? table.getCurrentObject() : ClientGroupObjectValue.EMPTY;
    }
    
    public int getCurrentRow() {
        return table != null ? table.getCurrentRow() : -1;
    }

    public void updateDrawColumnKeys(ClientPropertyDraw property, List<ClientGroupObjectValue> groupColumnKeys) {
        if (panel.containsProperty(property)) {
            panel.updateColumnKeys(property, groupColumnKeys);
        } else {
            table.updateColumnKeys(property, groupColumnKeys);
        }
    }

    public void updateDrawPropertyCaptions(ClientPropertyDraw property, Map<ClientGroupObjectValue, Object> captions) {
        if (panel.containsProperty(property)) {
            panel.updatePropertyCaptions(property, captions);
        } else {
            table.updatePropertyCaptions(property, captions);
        }
    }

    public void updateShowIfs(ClientPropertyDraw property, Map<ClientGroupObjectValue, Object> showIfs) {
        if (panel.containsProperty(property)) {
            panel.updateShowIfs(property, showIfs);
        } else {
            table.updateShowIfValues(property, showIfs);
        }
    }

    @Override
    public void updateReadOnlyValues(ClientPropertyDraw property, Map<ClientGroupObjectValue, Object> values) {
        if (panel.containsProperty(property)) {
            panel.updateReadOnlyValues(property, values);
        } else {
            table.updateReadOnlyValues(property, values);
        }
    }

    public void updateCellBackgroundValues(ClientPropertyDraw property, Map<ClientGroupObjectValue, Object> cellBackgroundValues) {
        if (panel.containsProperty(property)) {
            panel.updateCellBackgroundValues(property, cellBackgroundValues);
        } else {
            table.updateCellBackgroundValues(property, cellBackgroundValues);
        }
    }

    public void updateCellForegroundValues(ClientPropertyDraw property, Map<ClientGroupObjectValue, Object> cellForegroundValues) {
        if (panel.containsProperty(property)) {
            panel.updateCellForegroundValues(property, cellForegroundValues);
        } else {
            table.updateCellForegroundValues(property, cellForegroundValues);
        }
    }

    public void updateDrawPropertyValues(ClientPropertyDraw property, Map<ClientGroupObjectValue, Object> values, boolean updateKeys) {
        if (panel.containsProperty(property)) {
            panel.updatePropertyValues(property, values, updateKeys);
        } else {
            table.updatePropertyValues(property, values, updateKeys);
        }
    }

    public void updateRowBackgroundValues(Map<ClientGroupObjectValue, Object> rowBackground) {
        if (isGrid()) {
            table.updateRowBackgroundValues(rowBackground);
        } else {
            panel.updateRowBackgroundValue((Color)BaseUtils.singleValue(rowBackground));
        }
    }

    public boolean isGrid() {
        return groupObject != null && groupObject.classView.isGrid();
    }
    
    public void updateRowForegroundValues(Map<ClientGroupObjectValue, Object> rowForeground) {
        if (isGrid()) {
            table.updateRowForegroundValues(rowForeground);
        } else {
            panel.updateRowForegroundValue((Color)BaseUtils.singleValue(rowForeground));
        }
    }

    @Override
    public boolean changeOrders(ClientGroupObject groupObject, LinkedHashMap<ClientPropertyDraw, Boolean> orders, boolean alreadySet) {
        assert this.groupObject.equals(groupObject);
        if(isGrid()) {
            return changeOrders(orders, alreadySet);
        }
        return false; // doesn't matter
    }

    public boolean changeOrders(LinkedHashMap<ClientPropertyDraw, Boolean> orders, boolean alreadySet) {
        assert isGrid();
        return table.changePropertyOrders(orders, alreadySet);
    }

    public OrderedMap<ClientPropertyDraw, Boolean> getUserOrders() {
        boolean hasUserPreferences = isGrid() && table.hasUserPreferences();
        if (hasUserPreferences) return table.getUserOrders(getGroupObjectProperties());
        return null;
    }

    public OrderedMap<ClientPropertyDraw, Boolean> getDefaultOrders() {
        return formController.getDefaultOrders(groupObject);
    }
    
    public GroupObjectUserPreferences getUserGridPreferences() {
        return table.getCurrentUserGridPreferences();
    }

    public GroupObjectUserPreferences getGeneralGridPreferences() {
        return table.getGeneralGridPreferences();
    }

    public void registerGroupObject(JComponent comp) {
        comp.putClientProperty("groupObject", groupObject);
    }

    public List<ClientPropertyDraw> getPropertyDraws() {
        return formController.form.getPropertyDraws();
    }

    public ClientGroupObject getGroupObject() {
        return groupObject;
    }

    public ClientGroupObject getSelectedGroupObject() {
        return getGroupObject();
    }

    public List<ClientPropertyDraw> getGroupObjectProperties() {
        ArrayList<ClientPropertyDraw> properties = new ArrayList<>();
        for (ClientPropertyDraw property : getPropertyDraws()) {
            if (groupObject.equals(property.groupObject)) {
                properties.add(property);
            }
        }

        return properties;
    }
    
    public boolean isPropertyInGrid(ClientPropertyDraw property) {
        return table != null && table.containsProperty(property);
    }
    
    public boolean isPropertyInPanel(ClientPropertyDraw property) {
        return panel.containsProperty(property);
    }

    public ClientPropertyDraw getSelectedProperty() {
        return table.getCurrentProperty();
    }
    public ClientGroupObjectValue getSelectedColumn() {
        return table.getCurrentColumn();
    }

    public Object getSelectedValue(ClientPropertyDraw cell, ClientGroupObjectValue columnKey) {
        return table.getSelectedValue(cell, columnKey);
    }

    public void quickEditFilter(KeyEvent initFilterKeyEvent, ClientPropertyDraw propertyDraw, ClientGroupObjectValue columnKey) {
        if (filter != null) {
            filter.quickEditFilter(initFilterKeyEvent, propertyDraw, columnKey);
        }
    }

    public void selectProperty(ClientPropertyDraw propertyDraw) {
        table.focusProperty(propertyDraw);
    }

    public void focusProperty(ClientPropertyDraw propertyDraw) {
        PropertyController propertyController = panel.getPropertyController(propertyDraw);
        if (propertyController != null) {
            propertyController.requestFocusInWindow();
        } else {
            table.focusProperty(propertyDraw);
            table.requestFocusInWindow();
        }
    }

    public void updateSelectionInfo(int quantity, String sum, String avg) {
        if (calculationsView != null) {
            calculationsView.updateSelectionInfo(quantity, sum, avg);
        }
    }

    private void update(Boolean updateState) {
        if (groupObject != null) {
            if(updateState != null)
                forceUpdateTableButton.setEnabled(updateState);
            updateTable(updateState);

            if (toolbarView != null) {
                toolbarView.setVisible(isVisible());
            }

            if (filter != null) {
                filter.setVisible(isVisible());
            }

            if (calculationsView != null) {
                calculationsView.setVisible(isVisible());
            }

            formController.setFiltersVisible(groupObject, isVisible());
        }

        panel.update();
        panel.setVisible(true);
    }

    public GridView getGridView() {
        return view;
    }

    public boolean getAutoSize() {
        return groupObject.grid.autoSize;
    }

    public void setForceHidden(boolean forceHidden) {
        this.forceHidden = forceHidden;
    }

    public boolean isVisible() {
        return !forceHidden && isGrid();
    }

    public void updateTable(Boolean updateState) {
        table.update(updateState);
        view.setVisible(isVisible());
    }
}
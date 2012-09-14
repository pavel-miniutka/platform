package platform.gwt.form2.client.form.ui;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.SelectionChangeEvent;
import platform.gwt.base.shared.GOrder;
import platform.gwt.form2.shared.view.GForm;
import platform.gwt.form2.shared.view.GGroupObject;
import platform.gwt.form2.shared.view.GPropertyDraw;
import platform.gwt.form2.shared.view.changes.GGroupObjectValue;
import platform.gwt.form2.shared.view.grid.GridEditableCell;

import java.util.*;

public class GTreeTable extends GGridPropertyTable {
    private GTreeTableTree tree;

    private List<String> createdFields = new ArrayList<String>();

    private GTreeGridRecord selectedRecord;

    private Set<GTreeTableNode> expandedNodes;

    public GTreeTable(GFormController iformController, GForm iform) {
        super(iformController);

        tree = new GTreeTableTree(iform);
        Column<GTreeGridRecord, Object> column = new Column<GTreeGridRecord, Object>(new GTreeGridControlCell(this)) {
            @Override
            public Object getValue(GTreeGridRecord object) {
                return object.getAttribute("treeColumn");
            }
        };
        GridHeader header = new GridHeader("Дерево");
        createdFields.add("treeColumn");
        headers.add(header);
        addColumn(column, header);
        setColumnWidth(column, "150px");

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                GTreeGridRecord selectedRecord = (GTreeGridRecord) selectionModel.getSelectedRecord();
                if (selectedRecord != null && !selectedRecord.equals(GTreeTable.this.selectedRecord)) {
                    setCurrentRecord(selectedRecord);
                    form.changeGroupObject(selectedRecord.getGroup(), selectedRecord.key);
                }
            }
        });

        sortableHeaderManager = new GGridSortableHeaderManager<GPropertyDraw>(this, true) {
            @Override
            protected void orderChanged(GPropertyDraw columnKey, GOrder modiType) {
                form.changePropertyOrder(columnKey, GGroupObjectValue.EMPTY, modiType);
            }

            @Override
            protected GPropertyDraw getColumnKey(int column) {
                return tree.getColumnProperty(column);
            }
        };
    }

    public void removeProperty(GGroupObject group, GPropertyDraw property) {
        dataUpdated = true;
        int index = tree.removeProperty(group, property);
        if (index > 0) {
            removeColumn(index);
//            hideField(property.sID);
        }
    }

    public void addProperty(GGroupObject group, GPropertyDraw property) {
        dataUpdated = true;

        int index = tree.addProperty(group, property);

        if (index > -1) {
            if (createdFields.contains(property.sID)) {
//                showField(property.sID);
            } else {
                GridHeader header = new GridHeader(property.getCaptionOrEmpty());
                Column<GTreeGridRecord, Object> gridColumn = createGridColumn(property);

                headers.add(index, header);
                insertColumn(index, gridColumn, header);
                createdFields.add(index, property.sID);

                setColumnWidth(gridColumn, "150px");
            }
        }
    }

    private Column<GTreeGridRecord, Object> createGridColumn(final GPropertyDraw property) {
        return new Column<GTreeGridRecord, Object>(new GridEditableCell(this)) {
            @Override
            public Object getValue(GTreeGridRecord record) {
                int column = tree.columnProperties.indexOf(property);
                return tree.getValue(record.getGroup(), column, record.key);
            }
        };
    }

    public void setKeys(GGroupObject group, ArrayList<GGroupObjectValue> keys, ArrayList<GGroupObjectValue> parents) {
        tree.setKeys(group, keys, parents);
        dataUpdated = true;
    }

    public void updatePropertyValues(GPropertyDraw property, Map<GGroupObjectValue, Object> propValues, boolean updateKeys) {
        if (propValues != null) {
            dataUpdated = true;
            tree.setPropertyValues(property, propValues, updateKeys);
        }
    }

    public void update() {
        GTreeGridRecord selectedRecord = (GTreeGridRecord) selectionModel.getSelectedRecord();

        int oldKeyScrollTop = 0;
        GTreeGridRecord oldRecord = null;
        if (selectedRecord != null) {
            int oldKeyInd = currentRecords.indexOf(selectedRecord);

            if (oldKeyInd != -1) {
                oldRecord = selectedRecord;
                TableRowElement rowElement = getRowElement(oldKeyInd);
                oldKeyScrollTop = rowElement.getAbsoluteTop() - getScrollPanel().getAbsoluteTop();
            }
        }

        if (dataUpdated) {
            restoreVisualState();

            currentRecords = tree.getUpdatedRecords();
            updatePropertyReaders();
            setRowData(currentRecords);

            dataUpdated = false;
        }

        int currentInd = this.selectedRecord == null ? -1 : currentRecords.indexOf(this.selectedRecord);
        if (currentInd != -1) {
            if (this.selectedRecord.equals(oldRecord)) {
                scrollRowToVerticalPosition(currentInd, oldKeyScrollTop);
            } else {
                getRowElement(currentInd).scrollIntoView();
            }
            selectionModel.setSelected(currentRecords.get(currentInd), true);
            setKeyboardSelectedRow(currentInd, false);
        }

        updateHeader();
    }

    protected void updatePropertyReaders() {
        if (currentRecords != null &&
                //раскраска в дереве - редкое явление, поэтому сразу проверяем есть ли она
                (rowBackgroundValues.size() != 0 || rowForegroundValues.size() != 0 || cellBackgroundValues.size() != 0 || cellForegroundValues.size() != 0)) {
            for (GridDataRecord record : currentRecords) {
                GGroupObjectValue key = record.key;

                Object rBackground = rowBackgroundValues.get(key);
                Object rForeground = rowForegroundValues.get(key);

                List<GPropertyDraw> columnProperties = getColumnProperties();
                for (int j = 0; j < columnProperties.size(); j++) {
                    GPropertyDraw property = columnProperties.get(j);

                    Object background = rBackground;
                    if (background == null) {
                        Map<GGroupObjectValue, Object> propBackgrounds = cellBackgroundValues.get(property);
                        if (propBackgrounds != null) {
                            background = propBackgrounds.get(key);
                        }
                    }

                    Object foreground = rForeground;
                    if (foreground == null) {
                        Map<GGroupObjectValue, Object> propForegrounds = cellForegroundValues.get(property);
                        if (propForegrounds != null) {
                            foreground = propForegrounds.get(key);
                        }
                    }

                    record.setBackground(j + 1, background);
                    record.setForeground(j + 1, foreground);
                }
            }
        }
    }

    protected void updateHeader() {
        boolean needsHeaderRefresh = false;
        for (GPropertyDraw property : getColumnProperties()) {
            Map<GGroupObjectValue, Object> captions = propertyCaptions.get(property);
            if (captions != null) {
                Object value = captions.values().iterator().next();
                headers.get(getColumnIndex(property)).setCaption(value == null ? "" : value.toString().trim());
                needsHeaderRefresh = true;
            }
        }
        if (needsHeaderRefresh) {
            redrawHeaders();
        }
    }

    public void fireExpandNode(GTreeGridRecord record) {
        saveVisualState();
        GTreeTableNode node = tree.getNodeByRecord(record);
        if (node != null) {
            expandedNodes.add(node);
            form.expandGroupObject(node.getGroup(), node.getKey());
        }
    }

    public void fireCollapseNode(GTreeGridRecord record) {
        saveVisualState();
        GTreeTableNode node = tree.getNodeByRecord(record);
        if (node != null) {
            expandedNodes.remove(node);
            form.collapseGroupObject(node.getGroup(), node.getKey());
        }
    }

    public void saveVisualState() {
        expandedNodes = new HashSet<GTreeTableNode>();
        expandedNodes.addAll(getExpandedChildren(tree.root));
    }

    private List<GTreeTableNode> getExpandedChildren(GTreeTableNode node) {
        List<GTreeTableNode> exChildren = new ArrayList<GTreeTableNode>();
        for (GTreeTableNode child : node.getChildren()) {
            if (child.isOpen()) {
                exChildren.add(child);
                exChildren.addAll(getExpandedChildren(child));
            }
        }
        return exChildren;
    }

    public void restoreVisualState() {
        for (GTreeTableNode node :tree.root.getChildren()) {
            expandNode(node);
        }
    }

    private void setCurrentRecord(GTreeGridRecord record) {
        this.selectedRecord = record;
    }

    public GGroupObjectValue getCurrentKey() {
        return selectedRecord == null ? new GGroupObjectValue() : selectedRecord.key;
    }

    private void expandNode(GTreeTableNode node) {
        if (expandedNodes != null && expandedNodes.contains(node) && !tree.hasOnlyExpandningNodeAsChild(node)) {
            node.setOpen(true);
            for (GTreeTableNode child : node.getChildren()) {
                expandNode(child);
            }
        } else {
            node.setOpen(false);
        }
    }

    @Override
    public void setValueAt(Cell.Context context, Object value) {
        int row = context.getIndex();
        int column = context.getColumn();

        GridDataRecord rowRecord = (GridDataRecord) context.getKey();

        GPropertyDraw property = getProperty(row, column);
        rowRecord.setAttribute(property.sID, value);

        tree.putValue(property, rowRecord.key, value);


        setRowData(row, Arrays.asList(rowRecord));
    }

    public List<GPropertyDraw> getColumnProperties() {
        return tree.columnProperties;
    }

    public int getColumnIndex(GPropertyDraw property) {
        return getColumnProperties().indexOf(property) + 1;
    }

    private GGroupObject getRowGroup(int row) {
        return ((GTreeGridRecord) currentRecords.get(row)).getGroup();
    }

    @Override
    public Object getValueAt(Cell.Context context) {
        GTreeGridRecord record = (GTreeGridRecord) context.getKey();
        return tree.getValue(record.getGroup(), context.getColumn() - 1, record.key);
    }

    @Override
    public GPropertyDraw getProperty(int row, int column) {
        return tree.getProperty(getRowGroup(row), column - 1);
    }

    @Override
    public GGroupObjectValue getColumnKey(int row, int column) {
        return currentRecords.get(row).key;
    }

    public void changeOrder(GPropertyDraw property, GOrder modiType) {
        int propertyIndex = tree.getPropertyColumnIndex(property);
        if (propertyIndex > 0) {
            sortableHeaderManager.changeOrder(property, modiType);
        } else {
            //меняем напрямую для верхних groupObjects
            form.changePropertyOrder(property, GGroupObjectValue.EMPTY, modiType);
        }
    }
}

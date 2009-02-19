/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package platformlocal;

import net.sf.jasperreports.engine.JRAlignment;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.text.*;
import java.io.Serializable;
import java.lang.*;

class ClientGroupObjectImplement extends ArrayList<ClientObjectImplement>
                                 implements Serializable {

    Integer ID = 0;

    Boolean singleViewType = false;

    ClientGridView gridView = new ClientGridView();
    ClientFunctionView addView = new ClientFunctionView();
    ClientFunctionView changeClassView = new ClientFunctionView();
    ClientFunctionView delView = new ClientFunctionView();

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.ID != null ? this.ID.hashCode() : 0);
        return hash;
    }
    
    public ClientGroupObjectImplement() {
    }
}

class ClientGroupObjectMap<T> extends LinkedHashMap<ClientObjectImplement,T>
                              implements Serializable {

   
/*  На самом деле не надо - так как сравнивать как раз надо именно по значениям
    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    // Здесь по хорошему нужно hashcode когда новые свойства появятся перегрузить
    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    } */

}

class ClientGroupObjectValue extends ClientGroupObjectMap<Integer>
                             implements Serializable {
    
}

class ClientObjectImplement implements Serializable {
    
    Integer ID = 0;

    ClientGroupObjectImplement groupObject;
 
    String caption = "";
    
    ClientObjectView objectIDView = new ClientObjectView();
    ClientClassView classView = new ClientClassView();
    
    public ClientObjectImplement() {
    }

    public String toString() { return caption; }
}

class ClientComponentView implements Serializable {
    
    ClientContainerView container;
    SimplexConstraints constraints = new SimplexConstraints();
 
    String outName = "";
    
}

class ClientContainerView extends ClientComponentView {

    String title;

    LayoutManager layout;
}

class ClientGridView extends ClientComponentView {

}

class ClientClassView extends ClientComponentView {
    
}

class ClientFunctionView extends ClientComponentView {
    
}

class ClientRegularFilterGroupView extends ClientFunctionView {
    RegularFilterGroup filterGroup;
}

abstract class ClientCellView extends ClientComponentView {
    
    Integer ID = 0;
    // символьный идентификатор, нужен для обращению к свойствам в печатных формах
    String sID;

    public ClientClass baseClass;

    ClientGroupObjectImplement groupObject;
    
    Dimension minimumSize;
    Dimension maximumSize;
    Dimension preferredSize;

    String caption;

    public int getMinimumWidth() {
        return baseClass.getMinimumWidth();
    }

    public int getMinimumHeight() {
        return getPreferredHeight();
    }

    public Dimension getMinimumSize() {

        if (minimumSize != null) return minimumSize;
        return new Dimension(getMinimumWidth(), getMinimumHeight());
    }

    public int getPreferredWidth() {
        return baseClass.getPreferredWidth();
    }
    
    public int getPreferredHeight() {
        return 15;
    }
    
    public Dimension getPreferredSize() {
        
        if (preferredSize != null) return preferredSize;
        return new Dimension(getPreferredWidth(), getPreferredHeight());
    }

    public int getMaximumWidth() {
        return baseClass.getMaximumWidth();
    }

    public int getMaximumHeight() {
        return getPreferredHeight();
    }

    public Dimension getMaximumSize() {

        if (maximumSize != null) return maximumSize;
        return new Dimension(getMaximumWidth(), getMaximumHeight());
    }

    transient protected PropertyRendererComponent renderer;
    public PropertyRendererComponent getRendererComponent(ClientForm form) {

        if (renderer == null) renderer = baseClass.getRendererComponent(getFormat());

        return renderer;
    }

    public PropertyEditorComponent getEditorComponent(ClientForm form, Object value, boolean isDataChanging, boolean externalID) {

        ClientObjectValue objectValue;
        if (isDataChanging)
            objectValue = getEditorObjectValue(form, value, externalID);
        else
            objectValue = new ClientObjectValue(baseClass, value);

        if (objectValue == null) return null;

        return objectValue.cls.getEditorComponent(form, this, objectValue.object, getFormat());
    }

    protected ClientObjectValue getEditorObjectValue(ClientForm form, Object value, boolean externalID) {
        if (externalID) return null;
        return new ClientObjectValue(baseClass, value);
    }

    Format format;
    public Format getFormat() {
        if (format == null) return baseClass.getDefaultFormat();
        return format;
    }

    public String toString() { return caption; }

    public void fillReportDrawField(ReportDrawField reportField) {

        reportField.sID = sID;
        reportField.caption = caption;

        reportField.minimumWidth = getMinimumWidth();
        reportField.preferredWidth = getPreferredWidth();

        Format format = getFormat();
        if (format instanceof DecimalFormat) {
            reportField.pattern = ((DecimalFormat)format).toPattern();
        }
        if (format instanceof SimpleDateFormat) {
            reportField.pattern = ((SimpleDateFormat)format).toPattern();
        }

        baseClass.fillReportDrawField(reportField);
    }
}

class ClientPropertyView extends ClientCellView {

    protected ClientObjectValue getEditorObjectValue(ClientForm form, Object value, boolean externalID) {

        ClientChangeValue changeValue = ByteArraySerializer.deserializeClientChangeValue(form.remoteForm.getPropertyEditorObjectValueByteArray(this.ID, externalID));
        if (changeValue == null) return null;
        
        return changeValue.getObjectValue(value);
    }
}

class ClientObjectView extends ClientCellView {

    ClientObjectImplement object;

    public int getMaximumWidth() {
        return getPreferredWidth();
    }

    public PropertyEditorComponent getEditorComponent(ClientForm form, Object value, boolean isDataChanging, boolean externalID) {

        if (externalID) return null;

        if (!groupObject.singleViewType) {
            form.switchClassView(groupObject);
            return null;
        } else
            return super.getEditorComponent(form, value, isDataChanging, externalID);
    }

}

class ClientFormChanges extends AbstractFormChanges<ClientGroupObjectImplement,ClientGroupObjectValue,ClientPropertyView> {

}


class ClientFormView implements Serializable {
    
    List<ClientGroupObjectImplement> groupObjects = new ArrayList();
    List<ClientObjectImplement> objects = new ArrayList();
    List<ClientPropertyView> properties = new ArrayList();

    List<ClientContainerView> containers = new ArrayList();

    LinkedHashMap<ClientPropertyView,Boolean> defaultOrders = new LinkedHashMap();
    List<ClientRegularFilterGroupView> regularFilters = new ArrayList();   

    ClientFunctionView printView = new ClientFunctionView();
    ClientFunctionView refreshView = new ClientFunctionView();
    ClientFunctionView applyView = new ClientFunctionView();
    ClientFunctionView cancelView = new ClientFunctionView();
    ClientFunctionView okView = new ClientFunctionView();
    ClientFunctionView closeView = new ClientFunctionView();

    List<ClientCellView> order = new ArrayList();

    public ClientFormView() {
    }

    public ClientGroupObjectImplement getGroupObject(int id) {
        for (ClientGroupObjectImplement groupObject : groupObjects)
            if (groupObject.ID == id) return groupObject;
        return null;
    }

    public ClientPropertyView getPropertyView(int id) {
        for (ClientPropertyView property : properties)
            if (property.ID == id) return property;
        return null;
    }
}

class DefaultClientFormView extends ClientFormView {

    private transient Map<GroupObjectImplement, ClientGroupObjectImplement> mgroupObjects = new HashMap();
    public ClientGroupObjectImplement get(GroupObjectImplement groupObject) { return mgroupObjects.get(groupObject); }

    private transient Map<ObjectImplement, ClientObjectImplement> mobjects = new HashMap();
    public ClientObjectImplement get(ObjectImplement object) { return mobjects.get(object); }

    private transient Map<PropertyView, ClientPropertyView> mproperties = new HashMap();
    public ClientPropertyView get(PropertyView property) { return mproperties.get(property); }

    private transient Map<ClientGroupObjectImplement, ClientContainerView> groupObjectContainers = new HashMap();
    private transient Map<ClientGroupObjectImplement, ClientContainerView> panelContainers = new HashMap();
    private transient Map<ClientGroupObjectImplement, ClientContainerView> buttonContainers = new HashMap();
    private transient Map<ClientGroupObjectImplement, Map<AbstractGroup, ClientContainerView>> groupPropertyContainers = new HashMap();

    public DefaultClientFormView(NavigatorForm navigatorForm) {

        ClientContainerView mainContainer = new ClientContainerView();
        mainContainer.outName = "mainContainer";
        mainContainer.constraints.childConstraints = SingleSimplexConstraint.TOTHE_BOTTOM;

        containers.add(mainContainer);

        for (GroupObjectImplement group : (List<GroupObjectImplement>)navigatorForm.Groups) {

            ClientGroupObjectImplement clientGroup = new ClientGroupObjectImplement();
            clientGroup.ID = group.ID;
            clientGroup.singleViewType = group.singleViewType;

            mgroupObjects.put(group, clientGroup);
            groupObjects.add(clientGroup);

            ClientContainerView groupContainer = new ClientContainerView();
            groupContainer.outName = "groupContainer " + group.get(0).caption;
            groupContainer.container = mainContainer;
            groupContainer.constraints.order = navigatorForm.Groups.indexOf(group);
            groupContainer.constraints.childConstraints = SingleSimplexConstraint.TOTHE_BOTTOM;
            groupContainer.constraints.insetsInside = new Insets(0,0,4,0);

            groupObjectContainers.put(clientGroup, groupContainer);
            containers.add(groupContainer);

            ClientContainerView gridContainer = new ClientContainerView();
            gridContainer.outName = "gridContainer " + group.get(0).caption;
            gridContainer.container = groupContainer;
            gridContainer.constraints.order = 0;
            gridContainer.constraints.childConstraints = SingleSimplexConstraint.TOTHE_RIGHT;

            containers.add(gridContainer);

            ClientContainerView panelContainer = new ClientContainerView();
            panelContainer.outName = "panelContainer " + group.get(0).caption;
            panelContainer.container = groupContainer;
            panelContainer.constraints.order = 1;
            panelContainer.constraints.childConstraints = SingleSimplexConstraint.TOTHE_RIGHTBOTTOM;

            panelContainer.title = group.get(0).caption;

            panelContainers.put(clientGroup, panelContainer);
            containers.add(panelContainer);

            ClientContainerView buttonContainer = new ClientContainerView();
            buttonContainer.outName = "buttonContainer " + group.get(0).caption;
            buttonContainer.container = groupContainer;
            buttonContainer.constraints.order = 2;
            buttonContainer.constraints.childConstraints = SingleSimplexConstraint.TOTHE_RIGHT;

            buttonContainers.put(clientGroup, buttonContainer);
            containers.add(buttonContainer);

            clientGroup.gridView.container = gridContainer;
            clientGroup.gridView.constraints.order = 1;
            clientGroup.gridView.constraints.fillVertical = 1;
            clientGroup.gridView.constraints.fillHorizontal = 1;

            clientGroup.addView.container = buttonContainer;
            clientGroup.addView.constraints.order = 0;

            clientGroup.delView.container = buttonContainer;
            clientGroup.delView.constraints.order = 1;

            clientGroup.changeClassView.container = buttonContainer;
            clientGroup.changeClassView.constraints.order = 2;

            for (ObjectImplement object : group) {

                ClientObjectImplement clientObject = new ClientObjectImplement();
                clientObject.ID = object.ID;
                clientObject.groupObject = clientGroup;

                clientObject.objectIDView.ID = object.ID;
                clientObject.objectIDView.sID = object.getSID();
                clientObject.objectIDView.groupObject = clientGroup;
                clientObject.objectIDView.object = clientObject;

//                clientObject.objectIDView.container = panelContainer;
                clientObject.objectIDView.constraints.order = -1000 + group.indexOf(object);
                clientObject.objectIDView.constraints.insetsSibling = new Insets(0,0,2,2);

                clientObject.caption = object.caption;
                clientObject.objectIDView.caption = object.caption;

                clientObject.objectIDView.baseClass = ByteArraySerializer.deserializeClientClass(
                                           ByteArraySerializer.serializeClass(object.baseClass));

                addComponent(clientGroup, clientObject.objectIDView, object.baseClass.getParent());

                clientObject.classView.container = gridContainer;
                clientObject.classView.constraints.order = 0;
                clientObject.classView.constraints.fillVertical = 1;
                clientObject.classView.constraints.fillHorizontal = 0.2;

                clientGroup.add(clientObject);

                mobjects.put(object, clientObject);
                objects.add(clientObject);

                order.add(clientObject.objectIDView);
            }
        }

        for (PropertyView property : (List<PropertyView>)navigatorForm.propertyViews) {

            ClientGroupObjectImplement groupObject = mgroupObjects.get(property.toDraw);

            ClientPropertyView clientProperty = new ClientPropertyView();
            clientProperty.ID = property.ID;
            clientProperty.sID = property.getSID();

            clientProperty.groupObject = groupObject;
            clientProperty.constraints.order = navigatorForm.propertyViews.indexOf(property);
            clientProperty.constraints.insetsSibling = new Insets(0,0,2,2);

            //временно
            clientProperty.caption = property.view.property.caption;
            clientProperty.baseClass = ByteArraySerializer.deserializeClientClass(
                                       ByteArraySerializer.serializeClass(property.view.property.getBaseClass().getCommonClass()));

            mproperties.put(property, clientProperty);
            properties.add(clientProperty);

            addComponent(groupObject, clientProperty, property.view.property.getParent());
            order.add(clientProperty);
        }

        for (RegularFilterGroup filterGroup : (List<RegularFilterGroup>)navigatorForm.regularFilterGroups) {

            // ищем самый нижний GroupObjectImplement, к которому применяется фильтр
            GroupObjectImplement groupObject = null;
            int order = -1;
            for (RegularFilter regFilter : filterGroup.filters) {
                // Если просто кнопка - отменить фильтр
                if (regFilter.filter == null) continue;
                GroupObjectImplement propGroupObject = regFilter.filter.property.getApplyObject();
                if (propGroupObject.Order > order) {
                    order = propGroupObject.Order;
                    groupObject = propGroupObject;
                }
            }

            if (groupObject == null) continue;

            ClientRegularFilterGroupView filterGroupView = new ClientRegularFilterGroupView();
            filterGroupView.filterGroup = filterGroup;
            filterGroupView.container = buttonContainers.get(mgroupObjects.get(groupObject));
            filterGroupView.constraints.order = 3 + navigatorForm.regularFilterGroups.indexOf(filterGroup);
            filterGroupView.constraints.insetsSibling = new Insets(0,4,2,4);

            regularFilters.add(filterGroupView);
        }

        ClientContainerView formButtonContainer = new ClientContainerView();
        formButtonContainer.container = mainContainer;
        formButtonContainer.constraints.order = navigatorForm.Groups.size();
        formButtonContainer.constraints.childConstraints = SingleSimplexConstraint.TOTHE_RIGHT;

        containers.add(formButtonContainer);

        printView.container = formButtonContainer;
        printView.constraints.order = 0;
        printView.constraints.directions = new SimplexComponentDirections(0,0.01,0.01,0);

        refreshView.container = formButtonContainer;
        refreshView.constraints.order = 1;
        refreshView.constraints.directions = new SimplexComponentDirections(0,0,0.01,0.01);

        applyView.container = formButtonContainer;
        applyView.constraints.order = 2;
        applyView.constraints.directions = new SimplexComponentDirections(0,0,0.01,0.01);

        applyView.constraints.insetsSibling = new Insets(0, 8, 0, 0);

        cancelView.container = formButtonContainer;
        cancelView.constraints.order = 3;
        cancelView.constraints.directions = new SimplexComponentDirections(0,0,0.01,0.01);

        okView.constraints.insetsSibling = new Insets(0, 8, 0, 0);
        
        okView.container = formButtonContainer;
        okView.constraints.order = 4;
        okView.constraints.directions = new SimplexComponentDirections(0,0,0.01,0.01);

        closeView.container = formButtonContainer;
        closeView.constraints.order = 5;
        closeView.constraints.directions = new SimplexComponentDirections(0,0,0.01,0.01);

    }

    private void addComponent(ClientGroupObjectImplement groupObject, ClientComponentView childComponent, AbstractGroup groupAbstract) {

        int order = childComponent.constraints.order;

        while (groupAbstract != null) {

            if (!groupPropertyContainers.containsKey(groupObject))
                groupPropertyContainers.put(groupObject, new HashMap());

            ClientContainerView groupPropertyContainer;
            if (groupPropertyContainers.get(groupObject).containsKey(groupAbstract))
                groupPropertyContainer = groupPropertyContainers.get(groupObject).get(groupAbstract);
            else {

                groupPropertyContainer = new ClientContainerView();
                groupPropertyContainers.get(groupObject).put(groupAbstract, groupPropertyContainer);
                groupPropertyContainer.constraints.order = order;
                groupPropertyContainer.constraints.childConstraints = SingleSimplexConstraint.TOTHE_RIGHTBOTTOM;
                groupPropertyContainer.constraints.fillVertical = -1;
                groupPropertyContainer.constraints.fillHorizontal = -1;
                groupPropertyContainer.title = groupAbstract.caption;
                containers.add(groupPropertyContainer);
            }

            childComponent.container = groupPropertyContainer;
            childComponent = groupPropertyContainer;
            groupAbstract = groupAbstract.getParent();
        }

        childComponent.container = panelContainers.get(groupObject);

    }

}

// -------------------------------------- Классы ------------------------------ //

abstract class ClientClass implements Serializable {

    int ID;
    String caption;

    boolean hasChilds;

    public String toString() { return caption; }

    public int getMinimumWidth() {
        return getPreferredWidth();
    }
    public int getPreferredWidth() {
        return 50;
    }
    public int getMaximumWidth() {
        return Integer.MAX_VALUE;
    }

    abstract Format getDefaultFormat();

    abstract public PropertyRendererComponent getRendererComponent(Format format);
    abstract public PropertyEditorComponent getEditorComponent(ClientForm form, ClientCellView property, Object value, Format format);

    abstract public java.lang.Class getJavaClass() ;

    public void fillReportDrawField(ReportDrawField reportField) {
        reportField.valueClass = getJavaClass();
        reportField.alignment = JRAlignment.HORIZONTAL_ALIGN_LEFT;
    };

}

class ClientObjectClass extends ClientClass {

    public int getPreferredWidth() { return 45; }
    public int getMaximumWidth() { return getPreferredWidth(); }

    Format getDefaultFormat() {
        return NumberFormat.getInstance();
    }

    public PropertyRendererComponent getRendererComponent(Format format) { return new IntegerPropertyRenderer(format); }
    public PropertyEditorComponent getEditorComponent(ClientForm form, ClientCellView property, Object value, Format format) { return new ObjectPropertyEditor(form, property, this, value); }

    public java.lang.Class getJavaClass() {
        return java.lang.Integer.class;
    }

    public void fillReportDrawField(ReportDrawField reportField) {
        super.fillReportDrawField(reportField);

        reportField.alignment = JRAlignment.HORIZONTAL_ALIGN_RIGHT;
    }
}

abstract class ClientIntegralClass extends ClientClass {

    public int getMinimumWidth() { return 45; }
    public int getPreferredWidth() { return 80; }

    Format getDefaultFormat() {
        return NumberFormat.getInstance();
    }

    public PropertyRendererComponent getRendererComponent(Format format) { return new IntegerPropertyRenderer(format); }
    public PropertyEditorComponent getEditorComponent(ClientForm form, ClientCellView property, Object value, Format format) { return new IntegerPropertyEditor(value, (NumberFormat)format, getJavaClass()); }

    public void fillReportDrawField(ReportDrawField reportField) {
        super.fillReportDrawField(reportField);

        reportField.alignment = JRAlignment.HORIZONTAL_ALIGN_RIGHT;
    }

}

class ClientIntegerClass extends ClientIntegralClass {

    public java.lang.Class getJavaClass() {
        return java.lang.Integer.class;
    }
}

class ClientLongClass extends ClientIntegralClass {

    public java.lang.Class getJavaClass() {
        return java.lang.Long.class;
    }
}

class ClientDoubleClass extends ClientIntegralClass {

    public java.lang.Class getJavaClass() {
        return java.lang.Double.class;
    }
}

class ClientStringClass extends ClientClass {

    public int getMinimumWidth() { return 30; }
    public int getPreferredWidth() { return 250; }

    Format getDefaultFormat() {
        return null;
    }

    public PropertyRendererComponent getRendererComponent(Format format) { return new StringPropertyRenderer(format); }
    public PropertyEditorComponent getEditorComponent(ClientForm form, ClientCellView property, Object value, Format format) { return new StringPropertyEditor(value); }

    public java.lang.Class getJavaClass() {
        return java.lang.String.class;
    }
}

class ClientDateClass extends ClientClass {

    public int getPreferredWidth() { return 70; }

    Format getDefaultFormat() {
        return DateFormat.getDateInstance(DateFormat.SHORT);
    }

    public PropertyRendererComponent getRendererComponent(Format format) { return new DatePropertyRenderer(format); }
    public PropertyEditorComponent getEditorComponent(ClientForm form, ClientCellView property, Object value, Format format) { return new DatePropertyEditor(value, (SimpleDateFormat) format); }

    public java.lang.Class getJavaClass() {
        return java.util.Date.class;
    }

    public void fillReportDrawField(ReportDrawField reportField) {
        super.fillReportDrawField(reportField);

        reportField.alignment = JRAlignment.HORIZONTAL_ALIGN_RIGHT;
    }
}

class ClientBitClass extends ClientClass {

    public int getPreferredWidth() { return 35; }

    Format getDefaultFormat() {
        return null;
    }

    public PropertyRendererComponent getRendererComponent(Format format) { return new BitPropertyRenderer(); }
    public PropertyEditorComponent getEditorComponent(ClientForm form, ClientCellView property, Object value, Format format) { return new BitPropertyEditor(value); }

    public java.lang.Class getJavaClass() {
        return java.lang.Boolean.class;
    }

    public void fillReportDrawField(ReportDrawField reportField) {
        super.fillReportDrawField(reportField);

        reportField.alignment = JRAlignment.HORIZONTAL_ALIGN_CENTER;
    }
}

class ClientObjectValue {

    ClientClass cls;
    Object object;

    public ClientObjectValue() {
    }

    public ClientObjectValue(ClientClass icls, Object iobject) {
        cls = icls;
        object = iobject;
    }
}

abstract class ClientChangeValue {
    ClientClass cls;

    ClientChangeValue(ClientClass icls) {
        cls = icls;
    }

    abstract public ClientObjectValue getObjectValue(Object value);
}

class ClientChangeObjectValue extends ClientChangeValue {
    Object value;

    ClientChangeObjectValue(ClientClass icls, Object ivalue) {
        super(icls);
        value = ivalue;
    }

    public ClientObjectValue getObjectValue(Object ivalue) {
        return new ClientObjectValue(cls, value);
    }
}

class ClientChangeCoeffValue extends ClientChangeValue {
    Integer coeff;

    ClientChangeCoeffValue(ClientClass icls, Integer icoeff) {
        super(icls);
        coeff = icoeff;
    }

    public ClientObjectValue getObjectValue(Object value) {

        Object newValue = value;

        if (coeff.equals(1))
            newValue = value;
        else
            newValue = BaseUtils.multiply(value, coeff);

        return new ClientObjectValue(cls, newValue);
    }
}


// -------------------------------------- Фильтры ------------------------------ //

class ClientFilter {

    ClientPropertyView property;
    ClientValueLink value;

    int compare;
    
}

abstract class ClientValueLink {

}

class ClientUserValueLink extends ClientValueLink {

    Object value;

    public String toString() { return "Значение"; }
}

class ClientObjectValueLink extends ClientValueLink {

    ClientObjectImplement object;

    public String toString() { return "Объект"; }
}

class ClientPropertyValueLink extends ClientValueLink {

    ClientPropertyView property;

    public String toString() { return "Свойство"; }
}
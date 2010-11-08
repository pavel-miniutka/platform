package platform.client.descriptor;

import platform.base.BaseUtils;
import platform.client.Main;
import platform.client.descriptor.filter.FilterDescriptor;
import platform.client.descriptor.filter.RegularFilterGroupDescriptor;
import platform.client.descriptor.increment.IncrementDependency;
import platform.client.descriptor.increment.IncrementView;
import platform.client.descriptor.property.PropertyDescriptor;
import platform.client.descriptor.property.PropertyInterfaceDescriptor;
import platform.client.descriptor.nodes.PropertyDrawNode;
import platform.client.logics.ClientComponent;
import platform.client.logics.ClientContainer;
import platform.client.logics.ClientForm;
import platform.client.logics.ClientGroupObject;
import platform.client.logics.classes.ClientClass;
import platform.client.serialization.ClientIdentitySerializable;
import platform.client.serialization.ClientSerializationPool;
import platform.interop.form.layout.ContainerFactory;
import platform.interop.form.layout.GroupObjectContainerSet;
import platform.interop.serialization.RemoteDescriptorInterface;

import java.io.*;
import java.util.*;

public class FormDescriptor extends IdentityDescriptor implements ClientIdentitySerializable {

    public ClientForm client = new ClientForm();

    public String caption;
    public boolean isPrintForm;

    public List<GroupObjectDescriptor> groupObjects = new ArrayList<GroupObjectDescriptor>();
    public List<PropertyDrawDescriptor> propertyDraws = new ArrayList<PropertyDrawDescriptor>();
    public Set<FilterDescriptor> fixedFilters = new HashSet<FilterDescriptor>();
    public List<RegularFilterGroupDescriptor> regularFilterGroups = new ArrayList<RegularFilterGroupDescriptor>();
    public Map<PropertyDrawDescriptor, GroupObjectDescriptor> forceDefaultDraw = new HashMap<PropertyDrawDescriptor, GroupObjectDescriptor>();

    // по сути IncrementLazy
    IncrementView allPropertiesLazy;
    private List<PropertyObjectDescriptor> allProperties;
    public List<PropertyObjectDescriptor> getAllProperties() {
        if (allProperties == null)
            allProperties = getProperties(groupObjects, null);
        return allProperties;
    }

    IncrementView propertyObjectConstraint;
    IncrementView toDrawConstraint;
    IncrementView columnGroupConstraint;
    IncrementView propertyCaptionConstraint;

    IncrementView containerController;

    public FormDescriptor() {
        
    }

    public FormDescriptor(int ID) {
        setID(ID);
        setCaption("Новая форма (" + ID + ")");
    }

    public List<PropertyDrawDescriptor> getAddPropertyDraws(GroupObjectDescriptor group) {
        List<PropertyDrawDescriptor> result = new ArrayList<PropertyDrawDescriptor>();
        for(PropertyDrawDescriptor propertyDraw : propertyDraws) // добавим новые свойства, предполагается что оно одно
            if(propertyDraw.getPropertyObject()==null && (group==null || group.equals(propertyDraw.addGroup)))
                result.add(propertyDraw);
        return result;
    }

    public List<PropertyDrawDescriptor> getGroupPropertyDraws(GroupObjectDescriptor group) {
        List<PropertyDrawDescriptor> result = new ArrayList<PropertyDrawDescriptor>();
        for (PropertyDrawDescriptor propertyDraw : propertyDraws)
            if (group == null || group.equals(propertyDraw.getGroupObject(groupObjects)))
                result.add(propertyDraw);
        return result;
    }

    private abstract class IncrementPropertyConstraint implements IncrementView {

        public abstract boolean updateProperty(PropertyDrawDescriptor property);

        public void update(Object updateObject, String updateField) {
            List<PropertyDrawDescriptor> checkProperties;
            if (updateObject instanceof PropertyDrawDescriptor)
                checkProperties = Collections.singletonList((PropertyDrawDescriptor) updateObject);
            else
                checkProperties = new ArrayList<PropertyDrawDescriptor>(propertyDraws);

            for(PropertyDrawDescriptor checkProperty : checkProperties)
                if(!updateProperty(checkProperty)) // удаляем propertyDraw
                    removeFromPropertyDraws(checkProperty);
        }
    }

    IncrementView containerMover;

    // класс, который отвечает за автоматическое перемещение компонент внутри контейнеров при каких-либо изменениях структуры groupObject
    private class ContainerMover implements IncrementView {
        public void update(Object updateObject, String updateField) {
            moveContainer(propertyDraws);
            moveContainer(regularFilterGroups);
        }

        private <T extends ContainerMovable> void moveContainer(List<T> objects) {

            ClientContainer mainContainer = client.mainContainer;

            for (T object : objects) {
                ClientContainer newContainer = object.getDestinationContainer(mainContainer, groupObjects);
                if (newContainer != null && !newContainer.isAncestorOf(object.getClientComponent(mainContainer).container)) {
                    int insIndex = -1;
                    // сначала пробуем вставить перед объектом, который идет следующим в этом контейнере
                    for (int propIndex = objects.indexOf(object) + 1; propIndex < objects.size(); propIndex++) {
                        ClientComponent comp = objects.get(propIndex).getClientComponent(mainContainer);
                        if (newContainer.equals(comp.container)) {
                            insIndex = newContainer.children.indexOf(comp);
                            if (insIndex != -1)
                                break;
                        }
                    }
                    if (insIndex == -1) {
                        // затем пробуем вставить после объекта, который идет перед вставляемым в этом контейнере
                        for (int propIndex = objects.indexOf(object) - 1; propIndex >= 0; propIndex--) {
                            ClientComponent comp = objects.get(propIndex).getClientComponent(mainContainer);
                            if (newContainer.equals(comp.container)) {
                                insIndex = newContainer.children.indexOf(comp);
                                if (insIndex != -1) {
                                    insIndex++;
                                    break;
                                }
                            }
                        }
                    }
                    if (insIndex == -1) insIndex = newContainer.children.size();  
                    newContainer.addToChildren(insIndex, object.getClientComponent(mainContainer));
                }
            }
        }
    }

    public void customSerialize(ClientSerializationPool pool, DataOutputStream outStream, String serializationType) throws IOException {
        pool.writeString(outStream, caption);
        outStream.writeBoolean(isPrintForm);

        pool.serializeCollection(outStream, groupObjects);
        pool.serializeCollection(outStream, propertyDraws);
        pool.serializeCollection(outStream, fixedFilters);
        pool.serializeCollection(outStream, regularFilterGroups);
        pool.serializeMap(outStream, forceDefaultDraw);
    }

    public void customDeserialize(ClientSerializationPool pool, DataInputStream inStream) throws IOException {
        caption = pool.readString(inStream);
        isPrintForm = inStream.readBoolean();

        groupObjects = pool.deserializeList(inStream);
        propertyDraws = pool.deserializeList(inStream);
        fixedFilters = pool.deserializeSet(inStream);
        regularFilterGroups = pool.deserializeList(inStream);
        forceDefaultDraw = pool.deserializeMap(inStream);

        client = pool.context;

        allPropertiesLazy = new IncrementView() {
            public void update(Object updateObject, String updateField) {
                allProperties = null;
            }
        };
        IncrementDependency.add("baseClass", allPropertiesLazy);
        IncrementDependency.add("objects", allPropertiesLazy);
        IncrementDependency.add(this, "groupObjects", allPropertiesLazy);

        // propertyObject подходит по интерфейсу и т.п.
        propertyObjectConstraint = new IncrementPropertyConstraint() {
            public boolean updateProperty(PropertyDrawDescriptor property) {
                return getAllProperties().contains(property.getPropertyObject());
            }
        };
        IncrementDependency.add("propertyObject", propertyObjectConstraint);
        IncrementDependency.add("baseClass", propertyObjectConstraint);
        IncrementDependency.add("objects", propertyObjectConstraint);
        IncrementDependency.add(this, "groupObjects", propertyObjectConstraint);

        // toDraw должен быть из groupObjects (можно убрать)
        toDrawConstraint = new IncrementPropertyConstraint() {
            public boolean updateProperty(PropertyDrawDescriptor property) {
                GroupObjectDescriptor toDraw = property.getToDraw();
                if (toDraw != null && property.getPropertyObject() != null && !property.getPropertyObject().getGroupObjects().contains(toDraw))
                    property.setToDraw(null);
                return true;
            }
        };
        IncrementDependency.add("toDraw", toDrawConstraint);
        IncrementDependency.add("propertyObject", toDrawConstraint);

        // toDraw должен быть из groupObjects (можно убрать)
        columnGroupConstraint = new IncrementPropertyConstraint() {
            public boolean updateProperty(PropertyDrawDescriptor property) {
                List<GroupObjectDescriptor> upGroups = property.getUpGroupObjects(groupObjects);
                List<GroupObjectDescriptor> columnGroups = property.getColumnGroupObjects();

                List<GroupObjectDescriptor> constrainedColumnGroups = BaseUtils.filterList(columnGroups, upGroups);
                if (!constrainedColumnGroups.equals(columnGroups))
                    property.setColumnGroupObjects(constrainedColumnGroups);
                return true;
            }
        };
        IncrementDependency.add("propertyObject", columnGroupConstraint);
        IncrementDependency.add("toDraw", columnGroupConstraint);
        IncrementDependency.add("objects", columnGroupConstraint);
        IncrementDependency.add(this, "groupObjects", columnGroupConstraint); // порядок тоже важен


        // propertyObject подходит по интерфейсу и т.п.
        propertyCaptionConstraint = new IncrementPropertyConstraint() {
            public boolean updateProperty(PropertyDrawDescriptor property) {
                PropertyObjectDescriptor propertyCaption = property.getPropertyCaption();
                if (propertyCaption != null && !getProperties(property.getColumnGroupObjects(), null).contains(propertyCaption))
                    property.setPropertyCaption(null);
                return true;
            }
        };
        IncrementDependency.add("propertyObject", propertyCaptionConstraint);
        IncrementDependency.add("propertyCaption", propertyCaptionConstraint);
        IncrementDependency.add("columnGroupObjects", propertyCaptionConstraint);
        IncrementDependency.add("baseClass", propertyCaptionConstraint);
        IncrementDependency.add("objects", propertyCaptionConstraint);
        IncrementDependency.add(this, "groupObjects", propertyCaptionConstraint);

        containerMover = new ContainerMover();
        IncrementDependency.add("groupObjects", containerMover);
        IncrementDependency.add("toDraw", containerMover);
        IncrementDependency.add("filters", containerMover);
        IncrementDependency.add("filter", containerMover);
        IncrementDependency.add("propertyDraws", containerMover);
        IncrementDependency.add("property", containerMover);
    }

    @Override
    public String toString() {
        return client.caption;
    }

    public ObjectDescriptor getObject(int objectID) {
        for (GroupObjectDescriptor group : groupObjects)
            for (ObjectDescriptor object : group)
                if (object.getID() == objectID)
                    return object;
        return null;
    }

    public List<PropertyObjectDescriptor> getProperties(GroupObjectDescriptor groupObject) {
        if (groupObject == null) return getAllProperties();
        return getProperties(groupObjects.subList(0, groupObjects.indexOf(groupObject) + 1), groupObject);
    }

    public static List<PropertyObjectDescriptor> getProperties(Collection<GroupObjectDescriptor> groupObjects, GroupObjectDescriptor toDraw) {
        Collection<ObjectDescriptor> objects = new ArrayList<ObjectDescriptor>();
        Map<Integer, Integer> objectMap = new HashMap<Integer, Integer>();
        for (GroupObjectDescriptor groupObject : groupObjects) {
            objects.addAll(groupObject);
            for (ObjectDescriptor object : groupObject) {
                objectMap.put(object.getID(), groupObject.getID());
            }
        }
        return getProperties(objects, toDraw == null ? new ArrayList<ObjectDescriptor>() : toDraw, Main.remoteLogics, objectMap, false, false);
    }

    public static List<PropertyObjectDescriptor> getProperties(Collection<GroupObjectDescriptor> groupObjects, RemoteDescriptorInterface remote, ArrayList<GroupObjectDescriptor> toDraw, boolean isCompulsory, boolean isAny) {
        Collection<ObjectDescriptor> objects = new ArrayList<ObjectDescriptor>();
        Map<Integer, Integer> objectMap = new HashMap<Integer, Integer>();
        for (GroupObjectDescriptor groupObject : groupObjects) {
            objects.addAll(groupObject);
            for (ObjectDescriptor object : groupObject) {
                objectMap.put(object.getID(), groupObject.getID());
            }
        }
        ArrayList<ObjectDescriptor> objList = new ArrayList<ObjectDescriptor>();
        for (GroupObjectDescriptor groupObject : toDraw) {
            objList.addAll(groupObject);
        }
        return getProperties(objects, objList, remote, objectMap, isCompulsory, isAny);
    }

    public static List<PropertyObjectDescriptor> getProperties(Collection<ObjectDescriptor> objects, Collection<ObjectDescriptor> atLeastOne, RemoteDescriptorInterface remote, Map<Integer, Integer> objectMap, boolean isCompulsory, boolean isAny) {
        Map<Integer, ObjectDescriptor> idToObjects = new HashMap<Integer, ObjectDescriptor>();
        Map<Integer, ClientClass> classes = new HashMap<Integer, ClientClass>();
        for (ObjectDescriptor object : objects) {
            ClientClass cls = object.getBaseClass();
            if (cls != null) {
                idToObjects.put(object.getID(), object);
                classes.put(object.getID(), object.getBaseClass());
            }
        }

        List<PropertyObjectDescriptor> result = new ArrayList<PropertyObjectDescriptor>();
        for (PropertyDescriptorImplement<Integer> implement : getProperties(remote, classes, BaseUtils.filterValues(idToObjects, atLeastOne).keySet(), objectMap, isCompulsory, isAny))
            result.add(new PropertyObjectDescriptor(implement.property, BaseUtils.join(implement.mapping, idToObjects)));
        return result;
    }

    public static <K> Collection<PropertyDescriptorImplement<K>> getProperties(RemoteDescriptorInterface remote, Map<K, ClientClass> classes) {
        // todo:
        return new ArrayList<PropertyDescriptorImplement<K>>();
    }

    public static Collection<PropertyDescriptorImplement<Integer>> getProperties(RemoteDescriptorInterface remote, Map<Integer, ClientClass> classes, Collection<Integer> atLeastOne, Map<Integer, Integer> objectMap, boolean isCompulsory, boolean isAny) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            DataOutputStream dataStream = new DataOutputStream(outStream);

            dataStream.writeInt(classes.size());
            for (Map.Entry<Integer, ClientClass> intClass : classes.entrySet()) {
                dataStream.writeInt(intClass.getKey());
                intClass.getValue().serialize(dataStream);
                if (atLeastOne.contains(intClass.getKey())) {
                    dataStream.writeInt(objectMap.get(intClass.getKey()));
                } else {
                    dataStream.writeInt(-1);
                }
            }

            DataInputStream inStream = new DataInputStream(new ByteArrayInputStream(remote.getPropertyObjectsByteArray(outStream.toByteArray(), isCompulsory, isAny)));
            ClientSerializationPool pool = new ClientSerializationPool();

            List<PropertyDescriptorImplement<Integer>> result = new ArrayList<PropertyDescriptorImplement<Integer>>();
            int size = inStream.readInt();
            for (int i = 0; i < size; i++) {
                PropertyDescriptor implementProperty = (PropertyDescriptor) pool.deserializeObject(inStream);
                Map<PropertyInterfaceDescriptor, Integer> mapInterfaces = new HashMap<PropertyInterfaceDescriptor, Integer>();
                for (int j = 0; j < implementProperty.interfaces.size(); j++)
                    mapInterfaces.put((PropertyInterfaceDescriptor) pool.deserializeObject(inStream), inStream.readInt());
                result.add(new PropertyDescriptorImplement<Integer>(implementProperty, mapInterfaces));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean moveGroupObject(GroupObjectDescriptor groupFrom, GroupObjectDescriptor groupTo) {
        return moveGroupObject(groupFrom, groupObjects.indexOf(groupTo) + (groupObjects.indexOf(groupFrom) > groupObjects.indexOf(groupTo) ? 0 : 1));
    }

    public boolean moveGroupObject(GroupObjectDescriptor groupFrom, int index) {

        moveClientComponent(groupFrom.getClientComponent(client.mainContainer), getElementTo(groupObjects, groupFrom, index).getClientComponent(client.mainContainer));

        BaseUtils.moveElement(groupObjects, groupFrom, index);
        BaseUtils.moveElement(client.groupObjects, groupFrom.client, index);

        IncrementDependency.update(this, "groupObjects");

        return true;
    }

    public boolean movePropertyDraw(PropertyDrawDescriptor propFrom, PropertyDrawDescriptor propTo) {
        return movePropertyDraw(propFrom, propertyDraws.indexOf(propTo) + (propertyDraws.indexOf(propFrom) > propertyDraws.indexOf(propTo) ? 0 : 1));
    }

    public boolean movePropertyDraw(PropertyDrawDescriptor propFrom, int index) {

        moveClientComponent(propFrom.client, getElementTo(propertyDraws, propFrom, index).client);

        BaseUtils.moveElement(propertyDraws, propFrom, index);
        BaseUtils.moveElement(client.propertyDraws, propFrom.client, index);

        IncrementDependency.update(this, "propertyDraws");
        return true;
    }

    private static <T> T getElementTo(List<T> list, T elemFrom, int index) {
        if (index == -1) {
            return list.get(list.size() - 1);
        } else {
            return list.get(index + (list.indexOf(elemFrom) >= index ? 0 : -1));
        }
    }

    public void setCaption(String caption) {
        this.caption = caption;
        client.caption = caption;

        IncrementDependency.update(this, "caption");
    }

    public String getCaption() {
        return caption;
    }

    private static void moveClientComponent(ClientComponent compFrom, ClientComponent compTo) {
        if (compFrom.container.equals(compTo.container)) {
            compFrom.container.moveChild(compFrom, compTo);
        }
    }

    public boolean addToPropertyDraws(PropertyDrawDescriptor propertyDraw) {
        propertyDraws.add(propertyDraw);
        client.propertyDraws.add(propertyDraw.client);

        IncrementDependency.update(this, "propertyDraws");
        return true;
    }

    public boolean removeFromPropertyDraws(PropertyDrawDescriptor propertyDraw) {
        propertyDraws.remove(propertyDraw);
        client.removePropertyDraw(propertyDraw.client);

        IncrementDependency.update(this, "propertyDraws");
        return true;
    }

    public boolean addToGroupObjects(GroupObjectDescriptor groupObject) {
        groupObjects.add(groupObject);
        client.groupObjects.add(groupObject.client);

        addGroupObjectDefaultContainers(groupObject, groupObjects);

        IncrementDependency.update(this, "groupObjects");
        return true;
    }

    private void addGroupObjectDefaultContainers(GroupObjectDescriptor group, List<GroupObjectDescriptor> groupObjects) {

        GroupObjectContainerSet<ClientContainer, ClientComponent> set = GroupObjectContainerSet.create(group.client,
                new ContainerFactory<ClientContainer>() {
                    public ClientContainer createContainer() {
                        return new ClientContainer(Main.generateNewID());
                    }
                });

        // вставляем контейнер после предыдущего
        int groupIndex = groupObjects.indexOf(group);
        int index = -1;
        if (groupIndex > 0) {
            index = client.mainContainer.children.indexOf(groupObjects.get(groupIndex-1).getClientComponent(client.mainContainer));
            if (index != -1)
                index++;
        }
        if (index == -1) index = client.mainContainer.children.size();

        client.mainContainer.add(index, set.getGroupContainer());
    }

    public boolean removeFromGroupObjects(GroupObjectDescriptor groupObject) {
        groupObjects.remove(groupObject);
        client.groupObjects.add(groupObject.client);

        IncrementDependency.update(this, "groupObjects");
        return true;
    }

    public List<RegularFilterGroupDescriptor> getRegularFilterGroups() {
        return regularFilterGroups;
    }

    public void addToRegularFilterGroups(RegularFilterGroupDescriptor filterGroup) {
        regularFilterGroups.add(filterGroup);
        client.addToRegularFilterGroups(filterGroup.client);
        IncrementDependency.update(this, "regularFilterGroups");
    }

    public void removeFromRegularFilterGroups(RegularFilterGroupDescriptor filterGroup) {
        regularFilterGroups.remove(filterGroup);
        client.removeFromRegularFilterGroups(filterGroup.client);
        IncrementDependency.update(this, "regularFilterGroups");
    }


    public static byte[] serialize(FormDescriptor form) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(outStream);
        new ClientSerializationPool(form.client).serializeObject(dataStream, form);
        new ClientSerializationPool(form.client).serializeObject(dataStream, form.client);

        return outStream.toByteArray();
}

    public static FormDescriptor deserialize(byte[] formByteArray) throws IOException {
        DataInputStream inStream = new DataInputStream(new ByteArrayInputStream(formByteArray));

        ClientForm richDesign = new ClientSerializationPool().deserializeObject(inStream);

        return new ClientSerializationPool(richDesign).deserializeObject(inStream);
    }

    public static FormDescriptor deserialize(byte[] richDesignByteArray, byte[] formEntityByteArray) throws IOException {
        ClientForm richDesign = new ClientSerializationPool()
                .deserializeObject(
                        new DataInputStream(
                                new ByteArrayInputStream(richDesignByteArray)));

        return new ClientSerializationPool(richDesign)
                .deserializeObject(
                        new DataInputStream(
                                new ByteArrayInputStream(formEntityByteArray)));
    }
}

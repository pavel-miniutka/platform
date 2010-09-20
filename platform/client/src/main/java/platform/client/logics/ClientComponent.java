package platform.client.logics;

import platform.interop.ComponentDesign;
import platform.interop.form.layout.DoNotIntersectSimplexConstraint;
import platform.interop.form.layout.SimplexConstraints;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class ClientComponent implements Serializable {

    public int compID; // ID есть и у свойст и у объектов, так что чтобы не путаться

    public ComponentDesign design;

    public ClientContainer container;
    public SimplexConstraints<Integer> constraints;

    public boolean defaultComponent = false;

    public boolean show;

    //пришлось сделать "конструктор копирования" для ремаппинга
    protected ClientComponent(ClientComponent original) {
        this.design = original.design;
        this.container = original.container;
        this.constraints = original.constraints;
        this.defaultComponent = original.defaultComponent = false;
        this.show = original.show;
    }
    
    ClientComponent(DataInputStream inStream, Collection<ClientContainer> containers) throws IOException, ClassNotFoundException {

        compID = inStream.readInt();

        design = (ComponentDesign) new ObjectInputStream(inStream).readObject();

        if(!inStream.readBoolean()) {
            int containerID = inStream.readInt();
            for(ClientContainer parent : containers)
                if(parent.getID()==containerID) {
                    container = parent;
                    break;
                }
        }

        constraints = (SimplexConstraints<Integer>) new ObjectInputStream(inStream).readObject();

        constraints.intersects = new HashMap<Integer, DoNotIntersectSimplexConstraint>();
        int count = inStream.readInt();
        for (int i = 0; i < count; i++) {
            constraints.intersects.put(inStream.readInt(), (DoNotIntersectSimplexConstraint) new ObjectInputStream(inStream).readObject());
        }
        constraints.ID = compID;

        defaultComponent = inStream.readBoolean();

        show = inStream.readBoolean();
    }
}

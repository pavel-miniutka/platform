package lsfusion.server.logics.property.actions.integration.hierarchy;

import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.server.form.entity.ObjectEntity;

public abstract class GroupParseNode extends ParseNode {
    public final ImSet<ParseNode> children;

    public GroupParseNode(ImSet<ParseNode> children) {
        this.children = children;
    }
    
    protected <T extends Node<T>> void importChildrenNodes(T node, ImMap<ObjectEntity, Object> upValues, ImportData importData){
        for(ParseNode child : children) {
            child.importNode(node, upValues, importData);
        }
    }
    protected <T extends Node<T>> void exportChildrenNodes(T node, ImMap<ObjectEntity, Object> upValues, ExportData importData) {
        for(ParseNode child : children) {
            child.exportNode(node, upValues, importData);
        }
    }
}

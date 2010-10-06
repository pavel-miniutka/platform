package platform.client.descriptor.view;

import platform.client.ClientTree;
import platform.client.descriptor.FormDescriptor;
import platform.client.descriptor.nodes.EditingTreeNode;
import platform.client.descriptor.nodes.FormNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;

public class FormDescriptorView extends JPanel {

    FormDescriptor form;

    JTree tree;
    TreeModel model;

    JPanel view;

    public FormDescriptorView() {

        setLayout(new BorderLayout());

        view = new JPanel();

        tree = new ClientTree() {

            @Override
            protected void changeCurrentElement() {

                DefaultMutableTreeNode node = getSelectionNode();
                if (node instanceof EditingTreeNode) {
                    view.removeAll();
                    view.add(((EditingTreeNode) node).createEditor());
                    view.validate();
                }
            }
        };

        JScrollPane pane = new JScrollPane(tree);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pane, view);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
    }

    public void setModel(FormDescriptor form) {
        this.form = form;
        update();
    }

    private void update() {
        model = new DefaultTreeModel(new FormNode(form)); 
        tree.setModel(model);
    }
}

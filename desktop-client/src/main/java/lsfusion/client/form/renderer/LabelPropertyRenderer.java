package lsfusion.client.form.renderer;

import lsfusion.client.form.PropertyRenderer;
import lsfusion.client.logics.ClientPropertyDraw;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.Format;

import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static lsfusion.client.form.ClientFormController.colorPreferences;

public abstract class LabelPropertyRenderer extends PropertyRenderer {
    protected Color defaultForeground = NORMAL_FOREGROUND;
    
    private JLabel label;
    protected Format format;

    protected LabelPropertyRenderer(ClientPropertyDraw property) {
        super(property);
        
        if (property != null) {
            format = property.getFormat();
            getComponent().setOpaque(true);
            
            defaultForeground = getComponent().getForeground();
        }
    }

    @Override
    public JLabel getComponent() {
        if (label == null) {
            label = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    paintLabelComponent(g);
                }
            };
        }
        return label;
    }
    
    public void paintLabelComponent(Graphics g) {}

    public void setFormat(Format format) {
        this.format = format != null || property == null ? format : property.getFormat();
    }

    @Override
    protected void drawForeground(Color conditionalForeground) {
        if (value == null) {
            if (property != null && property.isEditableNotNull()) {
                getComponent().setForeground(REQUIRED_FOREGROUND);
            }
        } else {
            getComponent().setForeground(conditionalForeground != null ? conditionalForeground : defaultForeground);
        }
    }

    @Override
    protected Border getDefaultBorder() {
        return createEmptyBorder(1, 2, 1, 2);
    }

    @Override
    protected void drawBorder(boolean isInFocusedRow, boolean hasFocus) {
        if (hasFocus) {
            getComponent().setBorder(createCompoundBorder(colorPreferences.getFocusedCellBorder(), createEmptyBorder(0, 1, 0, 1)));
        } else if (isInFocusedRow) {
            getComponent().setBorder(createCompoundBorder(colorPreferences.getSelectedRowBorder(), getDefaultBorder()));
        } else {
            getComponent().setBorder(getDefaultBorder());
        }
    }

    public void setValue(Object value) {
        super.setValue(value);
        if (value == null && property != null && property.isEditableNotNull()) {
            getComponent().setText(REQUIRED_STRING);
        }
    }
}

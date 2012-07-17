package platform.client.navigator;

import platform.gwt.view2.GNavigatorElement;
import platform.interop.navigator.FormShowType;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientNavigatorForm extends ClientNavigatorElement {
    
    public boolean isPrintForm;
    public FormShowType showType;

    public ClientNavigatorForm() {

    }

    public ClientNavigatorForm(int ID, String sID, String caption) {
        super(ID, sID, caption, false);
    }

    public ClientNavigatorForm(DataInputStream inStream) throws IOException {
        super(inStream);
        isPrintForm = inStream.readBoolean();
        showType = FormShowType.valueOf(inStream.readUTF());
    }

    private GNavigatorElement gwtNavigatorElement;
    public GNavigatorElement getGwtElement() {
        if (gwtNavigatorElement == null) {
            gwtNavigatorElement = super.getGwtElement();
            gwtNavigatorElement.icon = "form.png";
            gwtNavigatorElement.isForm = true;
        }
        return gwtNavigatorElement;
    }
}

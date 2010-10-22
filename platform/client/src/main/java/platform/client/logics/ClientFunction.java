package platform.client.logics;

import platform.client.descriptor.editor.FunctionEditor;
import platform.client.serialization.ClientSerializationPool;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientFunction extends ClientComponent {

    public String caption;

    public ClientFunction() {
    }

    @Override
    public void customSerialize(ClientSerializationPool pool, DataOutputStream outStream, String serializationType) throws IOException {
        super.customSerialize(pool, outStream, serializationType);

        pool.writeString(outStream, caption);
    }

    @Override
    public void customDeserialize(ClientSerializationPool pool, DataInputStream inStream) throws IOException {
        super.customDeserialize(pool, inStream);

        caption = pool.readString(inStream);
    }

    @Override
    public String toString() {
        return caption;
    }

    @Override
    public JComponent getPropertiesEditor() {
        return new FunctionEditor(this);
    }
}

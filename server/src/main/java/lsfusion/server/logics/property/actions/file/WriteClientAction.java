package lsfusion.server.logics.property.actions.file;

import com.google.common.base.Throwables;
import lsfusion.base.FileDialogUtils;
import lsfusion.base.RawFileData;
import lsfusion.interop.action.ClientActionDispatcher;
import lsfusion.interop.action.ExecuteClientAction;

import java.io.IOException;

public class WriteClientAction extends ExecuteClientAction {
    RawFileData file;
    String path;
    String extension;
    boolean append;
    boolean isDialog;

    public WriteClientAction(RawFileData file, String path, String extension, boolean append, boolean isDialog) {
        this.file = file;
        this.path = path;
        this.extension = extension;
        this.append = append;
        this.isDialog = isDialog;
    }

    @Override
    public void execute(ClientActionDispatcher dispatcher) throws IOException {
        try {
            if (isDialog) {
                path = FileDialogUtils.showSaveFileDialog(WriteUtils.appendExtension(path, extension), file);
                extension = null;
            }
            if(path != null) {
                WriteUtils.write(file, path, extension, append);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}